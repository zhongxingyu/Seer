 package es.upm.dit.gsi.beast.platform.jade;
 
 import jade.core.AID;
 import jade.core.ContainerID;
 import jade.core.Profile;
 import jade.core.ProfileImpl;
 import jade.core.Runtime;
 import jade.wrapper.AgentController;
 import jade.wrapper.ContainerController;
 import jade.wrapper.ControllerException;
 import jade.wrapper.PlatformController;
 import jade.wrapper.PlatformController.Listener;
 import jade.wrapper.PlatformEvent;
 import jade.wrapper.StaleProxyException;
 
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import es.upm.dit.gsi.beast.platform.Connector;
 
 /**
  * @author a.carrera
  * 
  */
 public class JadeConnector implements Connector {
 
     private Logger logger;
 
     private Runtime runtime;
     private PlatformController platform;
     private ContainerController mainContainer;
     private HashMap<String, ContainerController> platformContainers;
 
     private HashMap<String, AgentController> createdAgents;
 
     private final String TRUE = "true";
     private final String PLATFORM_ID = "BEAST";
     private final String MAIN_HOST = "localhost";
     private final String MAIN_PORT = "2099";
     private final String AGENTS = "rma:jade.tools.rma.rma;sniffer:jade.tools.sniffer.Sniffer";
     private final String SERVICES = "jade.core.messaging.TopicManagementService;jade.core.mobility.AgentMobilityService;jade.core.event.NotificationService;jade.core.replication.MainReplicationService";
 
     public JadeConnector(Logger logger) {
         this.logger = logger;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see es.upm.dit.gsi.beast.platform.Connector#launchPlatform()
      */
     @Override
     public void launchPlatform() {
         logger.fine("Launching Jade Platform...");
 
         this.runtime = Runtime.instance();
 
         //TODO hacer que los profiles sean configurables
         Profile p = new ProfileImpl();
         p.setParameter(Profile.GUI, TRUE);
         p.setParameter(Profile.NO_MTP, TRUE);
         p.setParameter(Profile.PLATFORM_ID, PLATFORM_ID);
         p.setParameter(Profile.LOCAL_HOST, MAIN_HOST);
         p.setParameter(Profile.LOCAL_PORT, MAIN_PORT);
         p.setParameter(Profile.AGENTS, AGENTS);
         p.setParameter(Profile.SERVICES, SERVICES);
 
         this.mainContainer = this.runtime.createMainContainer(p);
         logger.fine("Main container launched");
         try {
             platform = mainContainer.getPlatformController();
             logger.fine("Platform contoller obtained");
         } catch (ControllerException e) {
             logger.warning("Exception getting the platform... " + e);
         }
 
         this.createdAgents = new HashMap<String, AgentController>();
         this.platformContainers = new HashMap<String, ContainerController>();
         this.platformContainers.put("Main-Container", mainContainer);
 
         logger.finer("Adding listener to the platform...");
 
         try {
             Listener listener = new JadeListener();
             logger.finest("Listener created");
             this.platform.addPlatformListener(listener);
             logger.finer("Added listener to the platform");
         } catch (ControllerException e) {
             logger.severe("Impossible to add listener to the platform: " + e);
         }
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * es.upm.dit.gsi.beast.platform.Connector#createAgent(java.lang.String,
      * java.lang.String)
      */
     @Override
     public void createAgent(String agent_name, String path) {
         logger.fine("Creating agent " + agent_name + " in Main Container");
         Object reference = new Object();
         Object empty[] = new Object[1];
         empty[0] = reference;
         try {
             AgentController agentController = mainContainer.createNewAgent(
                     agent_name, path, empty);
             this.createdAgents.put(agent_name, agentController);
             logger.fine("Agent " + agent_name + " created in Main Container");
         } catch (StaleProxyException e) {
             logger.warning("Exception creating agent in MainContainer... " + e);
         }
     }
 
     /**
      * @param agentName
      * @param path
      * @param containerName
      * @param arguments
      */
     public void createAgent(String agentName, String path,
             String containerName, Object[] arguments) {
         
         ContainerController containerController = this.platformContainers
                 .get(containerName);
         if (containerController == null) {
             this.createContainer(containerName);
             containerController = this.platformContainers.get(containerName);
         }
         if (arguments==null) {
             logger.finest("No arguments for agent " + agentName + " in container " + containerName);
             Object reference = new Object();
             arguments= new Object[1];
             arguments[0] = reference;
         }
         try {
             logger.fine("Creating agent " + agentName + " in container "
                     + containerName);
             AgentController agentController = containerController
                     .createNewAgent(agentName, path, arguments);
             this.createdAgents.put(agentName, agentController);
             logger.fine("Agent " + agentName + " created in Container " + containerName);
         } catch (StaleProxyException e) {
             logger.warning("Exception creating agent " + agentName + " in container " + containerName + "... Exception:" + e);
         } catch (Exception e) {
             logger.warning("Exception creating agent " + agentName + " in container " + containerName + "... Exception:" + e);
         }
     }
 
     /**
      * Create a container in the platform
      * 
      * @param container
      *            The name of the container
      */
     public void createContainer(String container) {
 
         ContainerController controller = this.platformContainers.get(container);
         if (controller == null) {
 
             //TODO hacer que los profiles sean configurables
             Profile p = new ProfileImpl();
             p.setParameter(Profile.PLATFORM_ID, PLATFORM_ID);
             p.setParameter(Profile.MAIN_HOST, MAIN_HOST);
             p.setParameter(Profile.MAIN_PORT, MAIN_PORT);
             p.setParameter(Profile.LOCAL_HOST, MAIN_HOST);
             int port = Integer.parseInt(MAIN_PORT);
             port = port + 1 + this.platformContainers.size();
             p.setParameter(Profile.LOCAL_PORT, Integer.toString(port));
             logger.fine("Creating container " + container + "...");
             ContainerController agentContainer = this.runtime
                     .createAgentContainer(p);
             this.platformContainers.put(container, agentContainer);
             logger.fine("Container " + container + " created successfully.");
         } else {
             logger.fine("Container " + container + " is already created.");
         }
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see es.upm.dit.gsi.beast.platform.Connector#getAgentID(java.lang.String)
      */
     @Override
     public AID getAgentID(String agent_name) {
         //TODO no sé si se va a poder...
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see es.upm.dit.gsi.beast.platform.Connector#getMessageService()
      */
     @Override
     public Object getMessageService() {
         // TODO Auto-generated method stub
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * es.upm.dit.gsi.beast.platform.Connector#getAgentsExternalAccess(java.
      * lang.String)
      */
     @Override
     public AgentController getAgentsExternalAccess(String agent_name) {
         return this.createdAgents.get(agent_name);
     }
 
     /**
      * Inner class AgentManagerListenerAdapter This utility class provides a
      * dummy implementation of all the methods of the
      * jade.core.AgentManager.Listener interface
      */
     class JadeListener implements PlatformController.Listener {
 
         public JadeListener() {
             logger.fine(">> PlatformListener constructor");
         }
 
         // public void addedContainer(jade.core.event.PlatformEvent ev) {
         // ContainerID cid = ev.getContainer();
         // containerIDs.put(cid.getName(),cid);
         // }
         //
         // public void removedContainer(jade.core.event.PlatformEvent ev) {
         // ContainerID cid = ev.getContainer();
         // containerIDs.remove(cid.getName());
         // remoteContainers.remove(cid.getName());
         // try {
         // if (cid.getName().equals(mainContainer.getContainerName())) {
         // // The local main container is terminating -->
         // // The whole platform is terminating
         // logger.severe("Platform state: KILLED");
         // }
         // }
         // catch (ControllerException ce) {
         // // Should never happen
         // ce.printStackTrace();
         // }
         // }
         //
         // public void bornAgent(jade.core.event.PlatformEvent ev) {
         // }
         //
         // public void deadAgent(jade.core.event.PlatformEvent ev) {
         // AID agentID = ev.getAgent();
         // String agentName = agentID.getLocalName();
         // agentIDs.remove(agentName);
         // }
         //
         // public void movedAgent(jade.core.event.PlatformEvent ev) {
         // //TODO
         // }
         //
         // public void suspendedAgent(jade.core.event.PlatformEvent ev) {
         // }
         // //TODO
         //
         // public void resumedAgent(jade.core.event.PlatformEvent ev) {
         // //TODO
         // }
         //
         // public void frozenAgent(jade.core.event.PlatformEvent ev) {
         // //TODO
         // }
         //
         // public void thawedAgent(jade.core.event.PlatformEvent ev) {
         // //TODO
         // }
         //
         // public void addedMTP(jade.core.event.MTPEvent ev) {
         // //TODO
         // }
         //
         // public void removedMTP(jade.core.event.MTPEvent ev) {
         // //TODO
         // }
         //
         // public void messageIn(jade.core.event.MTPEvent ev) {
         // //TODO
         // }
         //
         // public void messageOut(jade.core.event.MTPEvent ev) {
         // //TODO
         // }
 
         @Override
         public void bornAgent(PlatformEvent arg0) {
             // TODO Auto-generated method stub
         }
 
         @Override
         public void deadAgent(PlatformEvent arg0) {
             // TODO Auto-generated method stub
 
         }
 
         @Override
         public void killedPlatform(PlatformEvent arg0) {
             // TODO Auto-generated method stub
 
         }
 
         @Override
         public void resumedPlatform(PlatformEvent arg0) {
             // TODO Auto-generated method stub
 
         }
 
         @Override
         public void startedPlatform(PlatformEvent arg0) {
             // TODO Auto-generated method stub
 
         }
 
         @Override
         public void suspendedPlatform(PlatformEvent arg0) {
             // TODO Auto-generated method stub
 
         }
     } // END of inner class AgentManagerListenerAdapter
 }
