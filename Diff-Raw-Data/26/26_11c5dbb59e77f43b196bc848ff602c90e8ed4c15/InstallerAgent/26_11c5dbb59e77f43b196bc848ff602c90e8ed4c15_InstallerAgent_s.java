 /**
  *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
  *  http://fusesource.com
  *
  *  The software in this package is published under the terms of the AGPL license
  *  a copy of which has been included with this distribution in the license.txt file.
  */
 package org.fusesource.cloudmix.agent;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import com.sun.jersey.api.NotFoundException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.fusesource.cloudmix.agent.logging.LogHandler;
 import org.fusesource.cloudmix.agent.logging.LogParser;
 import org.fusesource.cloudmix.common.GridClient;
 import org.fusesource.cloudmix.common.dto.AgentCfgUpdate;
 import org.fusesource.cloudmix.common.dto.AgentDetails;
 import org.fusesource.cloudmix.common.dto.ConfigurationUpdate;
 import org.fusesource.cloudmix.common.dto.ProcessList;
 import org.fusesource.cloudmix.common.dto.ProvisioningAction;
 import org.fusesource.cloudmix.common.dto.ProvisioningHistory;
 import org.fusesource.cloudmix.common.util.FileUtils;
 import org.fusesource.cloudmix.common.util.ObjectHelper;
 import org.springframework.beans.factory.InitializingBean;
 
 
 /**
  * Polls for features that should be installed/uninstalled and executes those installations.
  * 
  * @version $Revision: 1.1 $
  */
 public class InstallerAgent implements Callable<Object>, InitializingBean {
 
     public static final String PERSISTABLE_PROPERTY_AGENT_ID = "agent.id";
     public static final String PERSISTABLE_PROPERTY_AGENT_NAME = "agent.name";
     public static final String PERSISTABLE_PROPERTY_PROFILE_ID = "agent.profile";
 
     public static final String CREATED_KEY = InstallerAgent.class.getName() + ".created";
     public static final String STARTED_KEY = InstallerAgent.class.getName() + ".started";
     public static final String TIMESTAMP_KEY = InstallerAgent.class.getName() + ".timestamp";
 
     public static final String PROP_ACCESS_LOCK = "agent.profile";
 
     private static final transient Log LOG = LogFactory.getLog(InstallerAgent.class);
     private static final String AGENT_STATE_FILE = "agent-state.dat";
 
     // Persistent properties.
     protected AgentState agentState = new AgentState();
 
     protected String propertyFilePath;
 
     protected String agentId;
     protected String agentName;
     protected String profile = "default";
     protected String agentType;
     protected String[] supportPackageTypes = {};
     protected String agentLink;
 
     protected AgentDetails agentDetails;
 
     protected boolean addedToClient;
 
     private Set<String> initialFeatures;
     private File workDirectory;
 
     private GridClient client;
     private String hostName;
     private int maxFeatures = 1;
 
     private ProvisioningHistory provisioningHistory;
     private Date lastAppliedHistory;
     private int lastActionsCount;
 
     private ExecutorService executor = Executors.newSingleThreadExecutor();
     private BlockingQueue<ProvisioningHistory> historyQueue = new LinkedBlockingQueue<ProvisioningHistory>();
     private String baseHref;
 
     private LogParser parser;
 
     public InstallerAgent() {
 
         lastActionsCount = 0;
 
         Runnable r = new Runnable() {
             public void run() {
                 try {
                     while (true) {
                         // TODO: could ignore all but the last entry in the queue.
                         ProvisioningHistory history = historyQueue.take();
                         onProvisioningHistoryChanged(history);
                     }
                 } catch (InterruptedException e) {
                     // TODO: do something?
                 }
             }
         };
         executor.execute(r);
     }
 
     @Override
     public String toString() {
        return "InstallerAgent[id: " + agentId + " hostName: " + hostName + " profile: " + profile + "]";
     }
 
     /**
      * Sets a custom LogParser
      * 
      * @param parser
      */
     public void setParser(LogParser parser) {
         this.parser = parser;
     }
 
     /**
      * Gets the current LogParser
      * 
      * @return LogParser
      */
     public LogParser getParser() {
         return parser;
     }
 
     /**
      * Creates a LogHandler
      * 
      * @param logPath points to a log file
      * @return LogHandler
      */
     public LogHandler getLogHandler(String logPath) {
         InputStream is = null;
 
         try {
             is = new FileInputStream(logPath);
         } catch (FileNotFoundException ex) {
             LOG.warn(logPath + " points to a non-existent log");
             throw new RuntimeException(ex);
         }
         return getLogHandler(is);
     }
 
     /**
      * Creates a LogHandler
      * 
      * @param logStream represents a log stream
      * @return LogHandler
      */
     public LogHandler getLogHandler(InputStream logStream) {
         LogHandler handler = getParser() == null ? new LogHandler(logStream) : new LogHandler(logStream,
                                                                                               getParser());
         return handler;
     }
 
     public Object call() throws Exception {
         String theAgentId = getAgentId();
 
         addToClient(getAgentDetails());
 
         if (theAgentId != null) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Polling agent: " + theAgentId);
             }
             try {
                 ProvisioningHistory value = getClient().pollAgentHistory(theAgentId);
                 if (value != null) {
                     asyncOnProvisioningHistoryChanged(value);
                     // This may take some time so its done in a thread.
                 }
             } catch (NotFoundException e) {
                 System.out.println(e);
                 forceAgentReregistration();
             }
         }
         return null;
     }
 
     protected void asyncOnProvisioningHistoryChanged(ProvisioningHistory value) {
         try {
             historyQueue.put(value);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 
     public void setWorkDirectory(File d) {
         workDirectory = d;
     }
 
     public File getWorkDirectory() {
         return workDirectory;
     }
 
     // Properties
     // -------------------------------------------------------------------------
     public AgentDetails getAgentDetails() {
         if (agentDetails == null) {
             agentDetails = new AgentDetails();
             loadPersistedAgentDetails();
             populateInitialAgentDetails(agentDetails);
         }
         return agentDetails;
     }
 
     public void setAgentDetails(AgentDetails agentDetails) {
         this.agentDetails = agentDetails;
     }
 
     public AgentDetails updateAgentDetails() {
 
         // TODO why would we flush the agent details each time???
         // agentDetails = null;
         agentDetails = getAgentDetails();
         loadPersistedAgentDetails();
         populateInitialAgentDetails(agentDetails);
 
         try {
             getClient().updateAgentDetails(getAgentId(), getAgentDetails());
         } catch (URISyntaxException e) {
             LOG.info("Problem updating agent information ", e);
             e.printStackTrace();
         }
         return agentDetails;
     }
 
     public GridClient getClient() {
         if (client == null) {
             client = new RestGridClient();
         }
         return client;
     }
 
     public void setClient(GridClient gridControlerClient) {
         this.client = gridControlerClient;
     }
 
     public int getMaxFeatures() {
         return maxFeatures;
     }
 
     public void setMaxFeatures(int max) {
         maxFeatures = max;
     }
 
     public String getProfile() {
         return profile;
     }
 
     public void setProfile(String p) {
         profile = p;
         if (agentDetails != null) {
             agentDetails.setProfile(p);
         }
     }
 
     public String getBaseHref() {
         return baseHref;
     }
 
     /**
      * Sets the base link to this agent's web application.
      * <p/>
      * If this is a different link to the previously registered one then this will be updated on the
      * controller
      */
     public void setBaseHref(String href) {
         this.baseHref = href;
         final String noHost = "http://0.0.0.0";
         if (baseHref.startsWith(noHost)) {
             String portSuffix = baseHref.substring(noHost.length());
             try {
                 baseHref = "http://" + InetAddress.getLocalHost().getCanonicalHostName() + portSuffix;
             } catch (UnknownHostException ex) {
                 baseHref = "http://localhost" + portSuffix;
             }
         }
         if (baseHref != null) {
             AgentDetails details = getAgentDetails();
             String oldHref = details.getHref();
             if (!ObjectHelper.equal(oldHref, baseHref)) {
                 details.setHref(baseHref);
 
                 LOG.debug("updating agent href to " + baseHref);
                 updateAgentDetails();
                 LOG.debug("href is now " + getAgentDetails().getHref());
             }
         }
     }
 
     public String getAgentName() {
         return agentName;
     }
 
     public void setAgentName(String aName) {
         agentName = aName;
         if (agentDetails != null) {
             agentDetails.setName(aName);
         }
     }
 
     public ProvisioningHistory getProvisioningHistory() {
         return provisioningHistory;
     }
 
     public String getAgentId() throws URISyntaxException {
         if (agentId == null) {
             agentId = addToClient(getAgentDetails());
         }
         return agentId;
     }
 
     protected synchronized String addToClient(AgentDetails details) throws URISyntaxException {
         if (addedToClient) {
             return details.getId();
         }
 
         if (provisioningHistory != null) {
             ProcessList processList = new ProcessList();
             provisioningHistory.populate(processList);
             details.setProcesses(processList);
         }
 
         String generatedId = getClient().addAgentDetails(details);
         agentId = generatedId;
         details.setId(generatedId);
 
         persistAgentDetails();
 
         addedToClient = true;
         return generatedId;
     }
 
     protected void forceAgentReregistration() {
         agentId = null;
         addedToClient = false;
     }
 
     public String getHostName() {
         if (hostName == null) {
             hostName = createHostName();
         }
         return hostName;
     }
 
     public void setHostName(String hn) {
         hostName = hn;
     }
 
     /**
      * sets the path to the property file storing the properties of the agent modifiable remotely
      * 
      * @param path to the Java property file
      */
     public void setDetailsPropertyFilePath(String path) {
         propertyFilePath = path;
     }
 
     /**
      * gets the path to the property file storing the properties of the agent modifiable remotely
      */
     public String getDetailsPropertyFilePath() {
         return propertyFilePath;
     }
 
     // Implementation methods
     // -------------------------------------------------------------------------
     protected void populateInitialAgentDetails(AgentDetails details) {
         details.setHostname(getHostName());
         details.setHref(getBaseHref());
         details.setPid(PidUtils.getPid());
         details.setMaximumFeatures(getMaxFeatures());
         details.setProfile(getProfile());
         details.setName(getAgentName());
         details.setOs(System.getProperty("os.name"));
 
         details.setAgentLink(null);
         details.setContainerType(null);
         details.setSupportPackageTypes(new String[] {});
 
         Map<String, String> m = new HashMap<String, String>(System.getProperties().size());
         for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
             m.put(entry.getKey().toString(), entry.getValue().toString());
         }
         details.setSystemProperties(m);
 
         details.setContainerType(getContainerType());
         details.setSupportPackageTypes(getSupportPackageTypes());
         details.setAgentLink(getAgentLink());
 
         initialFeatures = getFeatures();
         details.setCurrentFeatures(initialFeatures);
 
     }
 
     public void setAgentLink(String agentLink) {
         this.agentLink = agentLink;
     }
 
     public String getAgentLink() {
         return agentLink;
     }
 
     public void setContainerType(String anAgentType) {
         agentType = anAgentType;
     }
 
     public String getContainerType() {
         return agentType;
     }
 
     public void setSupportPackageTypes(String[] supportPackageTypes) {
         this.supportPackageTypes = supportPackageTypes;
     }
 
     public String[] getSupportPackageTypes() {
         return supportPackageTypes;
     }
 
     protected Set<String> getFeatures() {
         return agentState.getAgentFeatures().keySet();
     }
 
     /**
      * persist the agents details locally so they can be retrieved later after a shutdown
      * 
      * @return true if the details were persisted successfully, false otherwise
      */
     public boolean persistAgentDetails() {
 
         if (getDetailsPropertyFilePath() == null) {
             return false;
         }
 
         synchronized (PROP_ACCESS_LOCK) {
             Properties props = loadProperties(getDetailsPropertyFilePath());
             if (props == null) {
                 props = new Properties();
             }
 
             if (agentName != null) {
                 props.setProperty(PERSISTABLE_PROPERTY_AGENT_NAME, agentName);
             }
 
             if (profile != null) {
                 props.setProperty(PERSISTABLE_PROPERTY_PROFILE_ID, profile);
             }
 
             if (agentId != null && agentId.trim().length() > 0) {
                 props.setProperty(PERSISTABLE_PROPERTY_AGENT_ID, agentId);
             }
 
             return persistProperties(props, getDetailsPropertyFilePath());
         }
     }
 
     /**
      * load the persisted agents details from local storage
      * 
      * @return true if the details were loaded successfully, false otherwise
      */
     public boolean loadPersistedAgentDetails() {
         if (agentDetails == null) {
             agentDetails = new AgentDetails();
         }
 
         Properties props = loadProperties(getDetailsPropertyFilePath());
         if (props == null) {
             return false;
         }
 
         synchronized (PROP_ACCESS_LOCK) {
             String prop = props.getProperty(PERSISTABLE_PROPERTY_AGENT_ID);
             agentId = prop != null ? prop : agentId;
             agentDetails.setId(prop != null ? prop : agentDetails.getId());
 
             prop = readProp(props, PERSISTABLE_PROPERTY_AGENT_NAME);
             setAgentName(prop != null ? prop : agentName);
 
             prop = readProp(props, PERSISTABLE_PROPERTY_PROFILE_ID);
             setProfile(prop != null ? prop : profile);
 
             return true;
         }
     }
 
     private String readProp(Properties props, String propName) {
         String value = props.getProperty(propName);
         return (value == null || value.trim().length() == 0) ? null : value;
     }
 
     protected void onProvisioningHistoryChanged(ProvisioningHistory aProvisioningHistory) {
 
         // logProvisioningHistory(aProvisioningHistory);
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("timestamp of previous provisioning history: " + lastAppliedHistory);
             LOG.debug("timestamp of current provisioning history: "
                       + (aProvisioningHistory != null ? aProvisioningHistory.getLastModified() : "???"));
         }
 
         if (aProvisioningHistory != null
             && (lastAppliedHistory == null || (lastAppliedHistory.getTime() != aProvisioningHistory
                 .getLastModified().getTime()))) {
 
             if (LOG.isDebugEnabled()) {
                 LOG.debug("provisioning instructions changed since last poll: " + aProvisioningHistory);
             }
 
             if (!validateAgent()) {
                 return;
             }
 
             provisioningHistory = aProvisioningHistory;
 
             List<AgentCfgUpdate> cfgUpdates = aProvisioningHistory.getCfgUpdates();
             if (cfgUpdates != null && cfgUpdates.size() > 0) {
 
                 boolean cfgUpdated = false;
                 for (AgentCfgUpdate update : cfgUpdates) {
                     if (AgentCfgUpdate.PROPERTY_AGENT_NAME.equals(update.getProperty())
                         && changed(getAgentDetails().getName(), update.getValue())) {
                         setAgentName(update.getValue());
                         getAgentDetails().setName(update.getValue());
                         cfgUpdated = true;
 
                     } else if (AgentCfgUpdate.PROPERTY_PROFILE_ID.equals(update.getProperty())
                                && changed(getAgentDetails().getProfile(), update.getValue())) {
                         setProfile(update.getValue());
                         getAgentDetails().setProfile(update.getValue());
                         cfgUpdated = true;
 
                     } else if (AgentCfgUpdate.PROPERTY_AGENT_FORCE_REGISTER.equals(update.getProperty())
                                && "true".equals(update.getValue())) {
                         forceAgentReregistration();
                         cfgUpdated = true;
                     }
 
                 }
 
                 if (cfgUpdated) {
                     persistAgentDetails();
                 }
             }
 
             if (lastActionsCount != aProvisioningHistory.getActions().size()) {
 
                 LOG.debug("provisioning actions changed since last poll (was " + lastActionsCount
                           + " and now " + aProvisioningHistory.getActions().size());
                 try {
                     Map<String, ProvisioningAction> installActions 
                         = new HashMap<String, ProvisioningAction>();
                     Map<String, ProvisioningAction> uninstallActions 
                         = new HashMap<String, ProvisioningAction>();
                     getEffectiveActions(installActions, uninstallActions);
 
                     LOG.debug("onProvisioningHistoryChanged - uninstall " + uninstallActions);
                     for (ProvisioningAction action : uninstallActions.values()) {
                         String featureName = action.getFeature();
                         Feature f = agentState.getAgentFeatures().get(featureName);
                         if (f != null) {
                             uninstallFeature(f);
                         } else {
                             LOG.warn("Cannot find installed feature " + featureName + " to uninstall");
                         }
                     }
 
                     LOG.debug("onProvisioningHistoryChanged - install: " + installActions);
                     for (ProvisioningAction action : installActions.values()) {
                         String credentials = null;
                         if (getClient() instanceof RestGridClient) {
                             credentials = ((RestGridClient)getClient()).getCredentials();
                         }
                         String resource = action.getResource();
                         if (resource == null) {
                             LOG.debug("Action has no resource! " + action);
                         } else {
                             installFeatures(action, credentials, resource);
                         }
                     }
                 } catch (Exception e) {
                     LOG.error("Error executing provisioning instructions", e);
                 } finally {
                     persistState();
                     updateAgentDetails();
                 }
 
                 lastActionsCount = aProvisioningHistory.getActions().size();
             }
             lastAppliedHistory = aProvisioningHistory.getLastModified();
 
         } else {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("No new instructions... ");
             }
         }
 
     }
 
     protected void installFeatures(ProvisioningAction action, String credentials, String resource)
         throws Exception {
         FeatureList features = new FeatureList(resource, credentials);
         Feature feature = features.getFeature(action.getFeature());
         if (feature != null) {
             // Uninstall old version of feature if it still exists.
             Feature f = agentState.getAgentFeatures().get(feature.getName());
             if (f != null) {
                 uninstallFeature(f);
             }
 
             installFeature(feature, action.getCfgUpdates());
         }
     }
 
     protected void installFeature(Feature feature, List<ConfigurationUpdate> featureCfgOverrides)
         throws Exception {
 
         LOG.info("Installing feature " + feature.getName());
         installProperties(feature, featureCfgOverrides);
         Map<String, Object> featureProperties = feature.getAgentProperties();
         for (Bundle bundle : feature.getBundles()) {
             Map<String, Object> bundleProperties = bundle.getAgentProperties();
             if (installBundle(feature, bundle)) {
                 LOG.info("Successfully added bundle " + bundle);
                 bundleProperties.put(TIMESTAMP_KEY, new Date());
             }
         }
         featureProperties.put(TIMESTAMP_KEY, new Date());
         addAgentFeature(feature);
     }
 
     // TODO must we use Feature class???
     // TODO rename to better name
     protected void addAgentFeature(Feature feature) {
         String name = feature.getName();
         if (name == null) {
             throw new NullPointerException("Feature name is null!");
         }
         agentState.getAgentFeatures().put(name, feature);
         getAgentDetails().getCurrentFeatures().add(name);
         updateAgentDetails();
     }
 
     // TODO rename to better name
     protected void removeFeatureId(String featureId) {
         agentState.getAgentFeatures().remove(featureId);
         getAgentDetails().getCurrentFeatures().remove(featureId);
         updateAgentDetails();
     }
 
     protected void uninstallFeature(Feature feature) throws Exception {
         LOG.info("Uninstalling feature " + feature);
 
         for (Bundle bundle : feature.getBundles()) {
             if (uninstallBundle(feature, bundle)) {
                 LOG.info("Successfully removed bundle " + bundle);
             }
         }
         String featureId = feature.getName();
         removeFeatureId(featureId);
     }
 
     protected void installProperties(Feature feature, List<ConfigurationUpdate> featureCfgOverrides) {
         Properties applicationProperties = feature.getProperties("applicationProperties");
         Properties systemProperties = System.getProperties();
         boolean changed = false;
 
         // time to apply the application's configuration overrides
         if (featureCfgOverrides != null && featureCfgOverrides.size() > 0) {
             if (applicationProperties == null) {
                 applicationProperties = new Properties();
             }
             for (ConfigurationUpdate cfgUpdate : featureCfgOverrides) {
                 applicationProperties.put(cfgUpdate.getProperty(), cfgUpdate.getValue());
             }
         }
 
         if (applicationProperties != null) {
             systemProperties.putAll(applicationProperties);
             changed = true;
             LOG.info(" ===>> adding app props to system");
         }
 
         if (changed) {
             System.setProperties(systemProperties);
             LOG.info(" ===>> applying props updates");
         }
 
     }
 
     public List<Bundle> getFeatureBundles(String featureName) {
 
         Feature f = agentState.getAgentFeatures().get(featureName);
         if (f != null) {
             return f.getBundles();
         } else {
             return new ArrayList<Bundle>();
         }
     }
 
     protected String createHostName() {
         try {
             return Inet4Address.getLocalHost().getCanonicalHostName();

         } catch (UnknownHostException e) {
             LOG.warn("Could not find out the host name: " + e, e);
             return "localhost";
         }
     }
 
     /**
      * Calculates the effective actions to be taken by the agent. This is based on the history. The history
      * could instruct to install a feature and then uninstall it later, in that case the net effect is 0. This
      * method walks the history and computes the effective actions for install and uninstall.
      * 
      * @param installActions This map will be filled with the install actions to perform
      * @param uninstallActions This map will be fille with the uninstall actions to perform
      */
     public void getEffectiveActions(Map<String, ProvisioningAction> installActions,
                                     Map<String, ProvisioningAction> uninstallActions) {
 
         ProvisioningHistory history = getProvisioningHistory();
         if (history == null) {
             return;
         }
 
         List<ProvisioningAction> actions = history.getActions();
         if (actions == null) {
             return;
         }
 
         for (ProvisioningAction action : actions) {
             if (ProvisioningAction.INSTALL_COMMAND.equals(action.getCommand())) {
                 LOG.debug("install action :" + action.getFeature());
                 installActions.put(action.getFeature(), action);
                 uninstallActions.remove(action.getFeature());
             } else if (ProvisioningAction.UNINSTALL_COMMAND.equals(action.getCommand())) {
                 LOG.debug("uninstall action :" + action.getFeature());
                 uninstallActions.put(action.getFeature(), action);
                 installActions.remove(action.getFeature());
             }
         }
     }
 
     // convenience
     private synchronized Properties loadProperties(String filePath) {
 
         try {
             if (filePath == null || "".equals(filePath) || !(new File(filePath)).exists()) {
                 return null;
             }
             Properties properties = new Properties();
             properties.load(new FileInputStream(filePath));
             return properties;
         } catch (Exception e) {
             LOG.warn("error loading properties file " + filePath + ", exception " + e);
             return null;
         }
     }
 
     private synchronized boolean persistProperties(Properties props, String filePath) {
 
         try {
             if (props == null || filePath == null || "".equals(filePath)) {
                 return false;
             }
             File propFile = new File(filePath);
             if (!propFile.exists()) {
                 if (propFile.getParentFile() != null && !propFile.getParentFile().exists()) {
                     FileUtils.createDirectory(propFile.getParentFile());
                 }
                 propFile.createNewFile();
             }
 
             props.store(new FileOutputStream(propFile), "agent details as of " + new Date());
             return true;
         } catch (Exception e) {
             LOG.warn("error storing properties file " + filePath, e);
             return false;
         }
     }
 
     protected boolean installBundle(Feature feature, Bundle bundle) {
         return true;
     }
 
     protected boolean uninstallBundle(Feature feature, Bundle bundle) {
         return true;
     }
 
     protected boolean validateAgent() {
         return true;
     }
 
     protected boolean changed(String oldValue, String newValue) {
         return !((oldValue == null && newValue == null) || (oldValue != null && oldValue.equals(newValue)));
     }
 
     public void afterPropertiesSet() throws Exception {
         init();
     }
 
     public void init() throws Exception {
         File dir = getWorkDirectory();
 
         LOG.info("Starting CloudMix Agent with client: " + getClient() + " profile: " + getProfile()
                  + " workingDir: " + dir);
 
         if (dir == null) {
             LOG.warn("No work directory specified.  Not persisting agent state.");
             return;
         } else {
             if (FileUtils.createDirectory(dir) == null) {
                 LOG.error("Cannot create work directory " + dir);
                 throw new RuntimeException("Cannot create work directory " + dir);
             }
             loadState();
             agentState.getAgentProperties().put(STARTED_KEY, new Date());
 
             // TODO: (CM-2) Clean up previously installed features. This is currently
             // disabled as there are problems related to the order in which the agent
             // and its deployed features are started when restarting servicemix.
             // cleanInstalledFeatures();
         }
     }
 
     protected void cleanInstalledFeatures() {
         Map<String, Feature> features = agentState.getAgentFeatures();
         if (features != null) {
             for (String fn : features.keySet()) {
                 Feature feature = features.get(fn);
                 try {
                     uninstallFeature(feature);
                 } catch (Exception e) {
                     LOG.error("Exception uninstalling feature " + feature, e);
                 }
             }
             agentDetails = null;
             agentDetails = getAgentDetails();
         }
     }
 
     protected void persistState() {
         try {
             File dir = getWorkDirectory();
             if (dir == null || !dir.exists()) {
                 // Persistence is not enabled.
                 return;
             }
 
             File stateFile = new File(dir, AGENT_STATE_FILE);
             LOG.info("Saving agent state to " + stateFile);
             OutputStream os = new FileOutputStream(stateFile);
 
             ObjectOutputStream oos = new ObjectOutputStream(os);
             oos.writeObject(agentState);
             // TODO: (CM-4) Use XStream for serializing agent state. Currently disabled
             // until SMX4 OSGi bundle issues can be resolved.
             // xstream.toXML(agentState, os);
             oos.close();
             os.close();
 
         } catch (Throwable t) {
             LOG.error("Error persisting agent state", t);
             LOG.debug(t);
         }
     }
 
     protected void loadState() throws Exception {
 
         File dir = getWorkDirectory();
         if (dir == null || !dir.exists()) {
             // Persistence is not enabled.
             return;
         }
         File stateFile = new File(dir, AGENT_STATE_FILE);
         if (!stateFile.exists()) {
             LOG.info("agent state file " + stateFile + " does not exist");
             agentState.getAgentProperties().put(CREATED_KEY, new Date());
             persistState();
             return;
         }
 
         try {
             InputStream is = new FileInputStream(stateFile);
             // TODO: (CM-4) Use XStream for serializing agent state. Currently disabled
             // until SMX4 OSGi bundle issues can be resolved.
             // Object o = xstream.fromXML(is);
             ObjectInputStream ois = new ObjectInputStream(is);
             Object o = ois.readObject();
             agentState = (AgentState)o;
 
             is.close();
         } catch (Exception e) {
             LOG.error("Error reading agent state", e);
             throw e;
         }
     }
 
 }
