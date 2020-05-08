 /**
  *  Copyright (C) 2009 Progress Software, Inc. All rights reserved.
  *  http://fusesource.com
  *
  *  The software in this package is published under the terms of the AGPL license
  *  a copy of which has been included with this distribution in the license.txt file.
  */
 package org.fusesource.meshkeeper.cloudmix;
 
 import static org.fusesource.meshkeeper.control.ControlServer.ControlEvent.SHUTDOWN;
 
 import java.io.ByteArrayInputStream;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.UUID;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.fusesource.cloudmix.agent.RestGridClient;
 import org.fusesource.cloudmix.common.CloudmixHelper;
 import org.fusesource.cloudmix.common.GridClient;
 import org.fusesource.cloudmix.common.ProcessClient;
 import org.fusesource.cloudmix.common.dto.AgentDetails;
 import org.fusesource.cloudmix.common.dto.Dependency;
 import org.fusesource.cloudmix.common.dto.DependencyStatus;
 import org.fusesource.cloudmix.common.dto.FeatureDetails;
 import org.fusesource.cloudmix.common.dto.ProfileDetails;
 import org.fusesource.cloudmix.common.dto.ProfileStatus;
 import org.fusesource.cloudmix.common.dto.PropertyDefinition;
 import org.fusesource.meshkeeper.MeshEvent;
 import org.fusesource.meshkeeper.MeshKeeper;
 import org.fusesource.meshkeeper.MeshKeeperFactory;
 import org.fusesource.meshkeeper.RegistryWatcher;
 import org.fusesource.meshkeeper.control.ControlServer;
 import org.fusesource.meshkeeper.distribution.PluginClassLoader;
 import org.fusesource.meshkeeper.distribution.provisioner.Provisioner;
 import org.fusesource.meshkeeper.distribution.registry.RegistryClient;
 import org.fusesource.meshkeeper.distribution.registry.RegistryFactory;
 import org.fusesource.meshkeeper.launcher.LaunchAgent;
 
 import com.sun.jersey.api.client.UniformInterfaceException;
 
 /**
  * CloudMixSupport
  * <p>
  * Description:
  * </p>
  * 
  * @author cmacnaug
  * @version 1.0
  */
 public class CloudMixProvisioner implements Provisioner {
 
     Log LOG = LogFactory.getLog(CloudMixProvisioner.class);
 
     private static final String MESH_KEEPER_CONTROL_PROFILE_ID = "MeshKeeperControl";
     private static final String MESH_KEEPER_CONTROL_FEATURE_ID = MESH_KEEPER_CONTROL_PROFILE_ID + ":Control-Server";
     private static final String MESH_KEEPER_AGENT_PROFILE_ID = "MeshKeeperAgent";
     private static final String MESH_KEEPER_AGENT_FEATURE_ID = MESH_KEEPER_AGENT_PROFILE_ID + ":Launcher";
 
     private String controllerUrl;
     private String preferredControlControlHost;
     private String[] requestedAgentHosts;
     private RestGridClient gridClient;
     private String cachedRegistryConnectUri = null;
 
     private boolean machineOwnerShip = false;
 
     private int maxAgents = 100;
 
     private int registryPort = 0;
     private long provisioningTimeout = 90000;
 
     public void dumpStatus() throws MeshProvisioningException {
         StringBuffer buf = new StringBuffer(1024);
         getStatus(buf);
         LOG.info(buf.toString());
     }
 
     protected boolean isProvisioned(String profileId) throws MeshProvisioningException {
 
         ProfileStatus profileStatus = getGridClient().getProfileStatus(profileId);
         if (profileStatus != null) {
             List<DependencyStatus> dependencyStatus = profileStatus.getFeatures();
             for (DependencyStatus status : dependencyStatus) {
                 if (!status.isProvisioned()) {
                     return false;
                 }
             }
         } else {
             return false;
         }
         return true;
     }
 
     /**
      * Asserts that all the requested features have been provisioned properly
      */
     protected void assertProvisioned(String profileId, int min) throws MeshProvisioningException {
         long start = System.currentTimeMillis();
 
         Set<String> provisionedFeatures = new TreeSet<String>();
         Set<String> failedFeatures = null;
         while (true) {
             failedFeatures = new TreeSet<String>();
             long now = System.currentTimeMillis();
 
             ProfileStatus profileStatus = getGridClient().getProfileStatus(profileId);
             if (profileStatus != null) {
                 List<DependencyStatus> dependencyStatus = profileStatus.getFeatures();
                 for (DependencyStatus status : dependencyStatus) {
                     String featureId = status.getFeatureId();
                     if (status.isProvisioned()) {
                         if (provisionedFeatures.add(featureId)) {
                             System.out.println("Provisioned feature: " + featureId);
                         }
                     } else {
                         failedFeatures.add(featureId);
                     }
                 }
             } else {
                 throw new RuntimeException("Profile status not found!");
             }
             if (failedFeatures.isEmpty()) {
                 return;
             }
 
             long delta = now - start;
             if (delta > 20000) {
                 throw new MeshProvisioningException("Provision failure. Not enough instances of features: " + failedFeatures + " after waiting " + (20000 / 1000) + " seconds");
             } else {
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException e) {
                     // ignore
                 }
             }
         }
     }
 
     public RestGridClient getGridClient() throws MeshProvisioningException {
         if (gridClient == null) {
             gridClient = createGridController();
 
         }
         return gridClient;
     }
 
     /**
      * Returns a newly created client. Factory method
      */
     protected RestGridClient createGridController() throws MeshProvisioningException {
         System.out.println("About to create RestGridClient for: " + controllerUrl);
         return new RestGridClient(controllerUrl);
     }
 
     private String getMeshKeeperVersion() {
         return PluginClassLoader.getModuleVersion();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.fusesource.meshkeeper.distribution.provisioner.Provisioner#deploy()
      */
     public void deploy() throws MeshProvisioningException {
 
         LOG.info("Deploying MeshKeeper");
         RestGridClient controller = getGridClient();
 
         if (isDeployed()) {
             return;
         }
 
         //Set up the controller:
         ProfileDetails controlProfile = new ProfileDetails();
         controlProfile.setId(MESH_KEEPER_CONTROL_PROFILE_ID);
         controlProfile.setDescription("This Profile hosts MeshKeeper control server instances");
 
         FeatureDetails controlFeature = new FeatureDetails();
         controlFeature.setId(MESH_KEEPER_CONTROL_FEATURE_ID);
         controlFeature.setMaximumInstances("1");
         if (preferredControlControlHost != null) {
             controlFeature.preferredMachine(preferredControlControlHost);
         }
         String provisionerId = UUID.randomUUID().toString();
         controlFeature.setResource("mop:update includeOptional exec org.fusesource.meshkeeper:meshkeeper-api:" + getMeshKeeperVersion() + " " + org.fusesource.meshkeeper.control.Main.class.getName()
                 + " --jms activemq:tcp://0.0.0.0:0" + " --registry zk:tcp://0.0.0.0:" + registryPort + " --provisionerId " + provisionerId);
         controlFeature.setOwnedByProfileId(controlProfile.getId());
         controlFeature.setOwnsMachine(false);
         controlFeature.validContainerType("mop");
         controlFeature.addProperty(MESHKEEPER_PROVISIONER_ID_PROPERTY, provisionerId);
         controller.addFeature(controlFeature);
         controlProfile.getFeatures().add(new Dependency(controlFeature.getId()));
         controller.addProfile(controlProfile);
 
         //Wait for the control profile to be provisioned:
         assertProvisioned(controlProfile.getId(), 1);
 
         LOG.info("Waiting " + provisioningTimeout / 1000 + "s for MeshKeeper control server to come on line.");
 
         //Find the registry connect uri:
         //If an explicit port was specified simply construct the registry connect uri from 
         //from the agent's host id:
         if (registryPort != 0) {
             List<String> agents = controller.getAgentsAssignedToFeature(MESH_KEEPER_CONTROL_FEATURE_ID);
             AgentDetails details = controller.getAgentDetails(agents.get(0));
             details.getHostname();
             String controlHost = details.getHostname();
             cachedRegistryConnectUri = "zk:tcp://" + controlHost + ":" + registryPort;
         } else {
             cachedRegistryConnectUri = findMeshRegistryUri();
         }
 
         LOG.info("MeshKeeper controller deployed at: " + cachedRegistryConnectUri);
 
         RegistryClient registry = null;
         try {
             registry = new RegistryFactory().create(cachedRegistryConnectUri + "?connectTimeout=" + provisioningTimeout);
 
         } catch (Exception e) {
             try {
                 if (registry != null) {
                     registry.destroy();
                 }
             } catch (Exception e2) {
                 LOG.warn("Error closing regisry", e);
             }
             unDeploy(true);
             throw new MeshProvisioningException("Unable to connect to deployed MeshKeeper controller", e);
         }
 
         LOG.info("MeshKeeper controller is online, deploying MeshKeeper agent profile");
         try {
             ProfileDetails agentProfile = new ProfileDetails();
             agentProfile.setId(MESH_KEEPER_AGENT_PROFILE_ID);
             agentProfile.setDescription("MeshKeeper launch agent");
             FeatureDetails agentFeature = new FeatureDetails();
             //agentFeature.addProperty(MeshKeeperFactory.MESHKEEPER_REGISTRY_PROPERTY, registyConnect);
             agentFeature.setId(MESH_KEEPER_AGENT_FEATURE_ID);
             agentFeature.depends(controlFeature);
             agentFeature.setOwnsMachine(machineOwnerShip);
             agentFeature.setResource("mop:update includeOptional exec org.fusesource.meshkeeper:meshkeeper-api:" + getMeshKeeperVersion() + " "
                     + org.fusesource.meshkeeper.launcher.Main.class.getName() + " --registry " + cachedRegistryConnectUri);
 
             int expectedAgentCount = 1;
             if (requestedAgentHosts != null && requestedAgentHosts.length > 0) {
                 agentFeature.setPreferredMachines(new HashSet<String>(Arrays.asList(requestedAgentHosts)));
                 expectedAgentCount = requestedAgentHosts.length;
             }
 
             agentFeature.setOwnsMachine(false);
             agentFeature.setMaximumInstances("" + maxAgents);
             agentFeature.validContainerType("mop");
             agentFeature.setOwnedByProfileId(agentProfile.getId());
             controller.addFeature(agentFeature);
             agentProfile.getFeatures().add(new Dependency(agentFeature.getId()));
             controller.addProfile(agentProfile);
 
             assertProvisioned(agentProfile.getId(), expectedAgentCount);
 
             final int agentsDeployed = controller.getProcessClientsForFeature(agentFeature.getId()).size();
             final CountDownLatch latch = new CountDownLatch(1);
 
             LOG.info("Deployed " + agentsDeployed + " launch agents. Waiting " + provisioningTimeout / 1000 + "s for them to come online");
             try {
                 registry.addRegistryWatcher(LaunchAgent.LAUNCH_AGENT_REGISTRY_PATH, new RegistryWatcher() {
 
                     public void onChildrenChanged(String path, List<String> children) {
                         if (children.size() >= agentsDeployed) {
                             latch.countDown();
                         }
                     }
 
                 });
                 latch.await(provisioningTimeout, TimeUnit.MILLISECONDS);
                 LOG.info("Launch Agents came online");
             } catch (TimeoutException e) {
                 LOG.warn("Timed out waiting for deployed agents", e);
             } catch (Exception e) {
                 throw new MeshProvisioningException("Error waiting for launch agents to come on line", e);
             }
         } finally {
             if (registry != null) {
                 try {
                     registry.destroy();
                 } catch (Exception e) {
                     LOG.warn("Error closing regisry", e);
                 }
             }
         }
 
         //TODO: should perhaps use our Registry created above to watch for launch agents:
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @seeorg.fusesource.meshkeeper.distribution.provisioner.Provisioner#
      * findMeshRegistryUri()
      */
     public String findMeshRegistryUri() throws MeshProvisioningException {
         if (cachedRegistryConnectUri == null) {
             RestGridClient controller = getGridClient();
 
             FeatureDetails fd = controller.getFeature(MESH_KEEPER_CONTROL_FEATURE_ID);
             if (fd == null) {
                 throw new MeshProvisioningException("MeshKeeper is not deployed");
             }
 
             String provisionerId = null;
             for (PropertyDefinition pd : fd.getProperties()) {
                 if (pd.getId().equals(Provisioner.MESHKEEPER_PROVISIONER_ID_PROPERTY)) {
                     provisionerId = pd.getExpression();
                 }
             }
 
             long timeout = System.currentTimeMillis() + provisioningTimeout;
             while (true) {
                 try {
                     if (System.currentTimeMillis() > timeout) {
                         throw new TimeoutException();
                     }
 
                     List<? extends ProcessClient> clients = controller.getProcessClientsForFeature(MESH_KEEPER_CONTROL_FEATURE_ID);
                     if (clients == null || clients.isEmpty()) {
                         LOG.warn("No processes found running: " + MESH_KEEPER_CONTROL_FEATURE_ID);
                         throw new MeshProvisioningException("MeshKeeper is not deployed");
                     }
 
                     ProcessClient pc = clients.get(0);
                    byte[] controllerProps = pc.directoryResource("meshkeeper/server/" + ControlServer.CONTROLLER_PROP_FILE_NAME).get(byte[].class);
                     Properties p = new Properties();
                     p.load(new ByteArrayInputStream(controllerProps));
 
                     //Make sure the provisionerId matches that of the feature (e.g. we don't want to be looking at a
                     //stale properties file:
                     if (provisionerId == null || provisionerId.equals(p.getProperty(MESHKEEPER_PROVISIONER_ID_PROPERTY))) {
                         LOG.debug("Provisioned provisionerId doesn't match");
 
                         cachedRegistryConnectUri = (String) p.get(MeshKeeperFactory.MESHKEEPER_REGISTRY_PROPERTY);
                         if (cachedRegistryConnectUri == null) {
                            throw new Exception(MeshKeeperFactory.MESHKEEPER_REGISTRY_PROPERTY + " not found in " + "meshkeeper/server/" + ControlServer.CONTROLLER_PROP_FILE_NAME);
                         }
                         return cachedRegistryConnectUri;
                     }
 
                 } catch (UniformInterfaceException uie) {
                     //if we get a 404 retry 
                     if (uie.getResponse().getStatus() != 404) {
                         throw new MeshProvisioningException("Error retrieving controller properties", uie);
                     }
                 } catch (Exception e) {
                     throw new MeshProvisioningException("Error retrieving controller properties", e);
                 }
 
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException e) {
                     throw new MeshProvisioningException("Error retrieving controller properties", e);
                 }
             }
 
             //            List<String> agents = controller.getAgentsAssignedToFeature(MESH_KEEPER_CONTROL_FEATURE_ID);
             //            if (agents != null) {
             //                
             //                getProcessClientsForFeature(featureId)
             //                AgentDetails details = controller.getAgentDetails(agents.get(0));
             //                details.getHostname();
             //                String controlHost = details.getHostname();
             //                details.get
             //                for (Process p : details.getProcesses().getProcesses())
             //                {
             //                    
             //                }
             //                
             //                cachedRegistryConnectUri = "zk:tcp://" + controlHost + ":4040";
             //            } else {
             //                throw new MeshProvisioningException("MeshKeeper is not deployed");
             //            }
         }
 
         return cachedRegistryConnectUri;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @seeorg.fusesource.meshkeeper.distribution.provisioner.Provisioner#
      * getDeploymentUri()
      */
     public String getDeploymentUri() {
         return controllerUrl;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @seeorg.fusesource.meshkeeper.distribution.provisioner.Provisioner#
      * getPreferredControlHost()
      */
     public String getPreferredControlHost() {
         return preferredControlControlHost;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @seeorg.fusesource.meshkeeper.distribution.provisioner.Provisioner#
      * getRequestedAgentHosts()
      */
     public String[] getRequestedAgentHosts() {
         return requestedAgentHosts;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.fusesource.meshkeeper.distribution.provisioner.Provisioner#getStatus
      * (java.lang.StringBuffer)
      */
     public StringBuffer getStatus(StringBuffer buffer) throws MeshProvisioningException {
         GridClient controller = getGridClient();
 
         if (buffer == null) {
             buffer = new StringBuffer(1024);
         }
 
         boolean foundProfile = false;
         for (String profile : new String[] { MESH_KEEPER_AGENT_PROFILE_ID, MESH_KEEPER_CONTROL_PROFILE_ID }) {
             ProfileStatus status = controller.getProfileStatus(profile);
             if (status != null) {
                 foundProfile = true;
                 buffer.append("Found profile: " + status.getId() + "\n");
             }
         }
 
         if (!foundProfile) {
             buffer.append("No MeshKeeper profiles found\n");
         }
 
         boolean foundFeatures = false;
         for (String feature : new String[] { MESH_KEEPER_CONTROL_FEATURE_ID, MESH_KEEPER_AGENT_FEATURE_ID }) {
             List<String> agents = controller.getAgentsAssignedToFeature(feature);
             if (agents != null && !agents.isEmpty()) {
                 foundFeatures = true;
                 buffer.append("Found agents running " + feature + ": " + agents + "\n");
             }
         }
 
         if (!foundFeatures) {
             buffer.append("MeshKeeper not currently deployed\n");
         }
 
         return buffer;
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.fusesource.meshkeeper.distribution.provisioner.Provisioner#isDeployed
      * ()
      */
     public boolean isDeployed() throws MeshProvisioningException {
 
         //Check that mesh profiles are deployed
         for (String profile : new String[] { MESH_KEEPER_AGENT_PROFILE_ID, MESH_KEEPER_CONTROL_PROFILE_ID }) {
             if (!isProvisioned(profile)) {
                 return false;
             }
 
         }
         return true;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.fusesource.meshkeeper.distribution.provisioner.Provisioner#reDeploy
      * (boolean)
      */
     public void reDeploy(boolean force) throws MeshProvisioningException {
         unDeploy(force);
 
         try {
             Thread.sleep(1000);
         } catch (InterruptedException ie) {
             Thread.currentThread().interrupt();
             throw new MeshProvisioningException(ie.getMessage(), ie);
         }
 
         deploy();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @seeorg.fusesource.meshkeeper.distribution.provisioner.Provisioner#
      * setDeploymentUri()
      */
     public void setDeploymentUri(String uri) {
         controllerUrl = uri;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @seeorg.fusesource.meshkeeper.distribution.provisioner.Provisioner#
      * setPreferredControlHost()
      */
     public void setPreferredControlHost(String preferredControlServerAgent) {
         this.preferredControlControlHost = preferredControlServerAgent;
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @seeorg.fusesource.meshkeeper.distribution.provisioner.Provisioner#
      * setRequestedAgentHosts(java.lang.String[])
      */
     public void setRequestedAgentHosts(String[] requestedAgentHosts) {
         this.requestedAgentHosts = requestedAgentHosts;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @seeorg.fusesource.meshkeeper.distribution.provisioner.Provisioner#
      * getAgentMachineOwnership()
      */
     public boolean getAgentMachineOwnership() {
         return machineOwnerShip;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.fusesource.meshkeeper.distribution.provisioner.Provisioner#getMaxAgents
      * ()
      */
     public int getMaxAgents() {
         return -1;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @seeorg.fusesource.meshkeeper.distribution.provisioner.Provisioner#
      * setRegistryPort(int)
      */
     public void setRegistryPort(int port) {
         this.registryPort = port;
     }
 
     /**
      * 
      * @return The time allows to wait for each provisioned component to come
      *         online.
      */
     public long getProvisioningTimeout() {
         return provisioningTimeout;
     }
 
     /**
      * sets the time allows to wait for each provisioned component to come
      * online.
      * 
      * @param provisioningTimeout
      *            the time allows to wait for each provisioned component to come
      *            online.
      */
     public void setProvisioningTimeout(long provisioningTimeout) {
         this.provisioningTimeout = provisioningTimeout;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @seeorg.fusesource.meshkeeper.distribution.provisioner.Provisioner#
      * setAgentMachineOwnership(boolean)
      */
     public void setAgentMachineOwnership(boolean machineOwnerShip) {
         this.machineOwnerShip = machineOwnerShip;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.fusesource.meshkeeper.distribution.provisioner.Provisioner#setMaxAgents
      * (int)
      */
     public void setMaxAgents(int maxAgents) {
         this.maxAgents = maxAgents;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.fusesource.meshkeeper.distribution.provisioner.Provisioner#unDeploy
      * (boolean)
      */
     public void unDeploy(boolean force) throws MeshProvisioningException {
         RestGridClient controller = getGridClient();
 
         boolean removed = false;
 
 //        MeshKeeper mesh = null;
 //        try {
 //            String reg = findMeshRegistryUri();
 //            if (reg != null) {
 //                mesh = MeshKeeperFactory.createMeshKeeper(reg);
 //                mesh.eventing().sendEvent(ControlServer.ControlEvent.SHUTDOWN.createEvent("CloudmixProvisioner",null), ControlServer.CONTROL_TOPIC);
 //                Thread.sleep(5000);
 //            }
 //        } catch (Exception e) {
 //
 //        } finally {
 //            if (mesh != null) {
 //                try {
 //                    mesh.destroy();
 //                } catch (Exception e) {
 //                }
 //            }
 //        }
 
         for (String profile : new String[] { MESH_KEEPER_AGENT_PROFILE_ID, MESH_KEEPER_CONTROL_PROFILE_ID }) {
             ProfileDetails existing = controller.getProfile(profile);
             if (existing != null) {
 
                 LOG.info("Removing existing meshkeeper profile: " + profile);
                 removed = true;
                 controller.removeProfile(existing);
             }
         }
 
         if (!removed) {
             LOG.info("No existing meshkeeper profiles to remove");
         }
     }
 
     private static final void printUsage() {
         System.out.println("Usage:");
         System.out.println("[deploy|redploy|findUri|undeploy|status] [cloudmix-control-url] [preferedMeskKeeperControlAgent]");
     }
 
     public static final void main(String[] args) {
 
         //String command = "findUri";
        String command = "undeploy";
 
         if (args.length > 0) {
             command = args[0];
         }
 
         CloudMixProvisioner provisioner = new CloudMixProvisioner();
         provisioner.setDeploymentUri(CloudmixHelper.getDefaultRootUrl());
 
         if (args.length > 1) {
             provisioner.setDeploymentUri(args[1]);
         }
 
         if (args.length > 2) {
             provisioner.setPreferredControlHost(args[2]);
         }
 
         try {
             if (command.equalsIgnoreCase("deploy")) {
                 provisioner.reDeploy(true);
             } else if (command.equalsIgnoreCase("redeploy")) {
                 provisioner.reDeploy(true);
             } else if (command.equalsIgnoreCase("findUri")) {
                 System.out.println("Registry Uri Found: " + provisioner.findMeshRegistryUri());
             } else if (command.equalsIgnoreCase("status")) {
                 provisioner.dumpStatus();
             } else if (command.equalsIgnoreCase("undeploy")) {
                 provisioner.unDeploy(true);
             } else {
                 printUsage();
             }
 
         } catch (Throwable e) {
             System.err.println("Error running MeshKeeper CloudMix provisionner: " + e.getMessage());
             e.printStackTrace();
         }
 
         System.exit(0);
 
     }
 
 }
