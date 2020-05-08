 package es.upm.dit.gsi.beast.platform.jade;
 
 import jade.core.Agent;
 import jade.core.Profile;
 import jade.core.ProfileImpl;
 import jade.core.Runtime;
 import jade.wrapper.AgentController;
 import jade.wrapper.ContainerController;
 import jade.wrapper.StaleProxyException;
 
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import es.upm.dit.gsi.beast.platform.Connector;
 
 /**
  * Project: beast
  * File: es.upm.dit.gsi.beast.platform.jade.JadeConnector.java
  * 
  * Grupo de Sistemas Inteligentes
  * Departamento de Ingeniería de Sistemas Telemáticos
  * Universidad Politécnica de Madrid (UPM)
  * 
  * @author alvarocarrera
  * @email a.carrera@gsi.dit.upm.es
  * @twitter @alvarocarrera
  * @date 28/12/2011
  * @version 0.1
  * 
  */
 public class JadeConnector implements Connector {
 
     private Logger logger;
 
     private Runtime runtime;
     private ContainerController mainContainer;
     private HashMap<String, ContainerController> platformContainers;
 
     private HashMap<String, AgentController> createdAgents;
 
     public final String BEAST_MESSENGER = "BeastMessenger";
     
     public final String TRUE = "true";
     public final String PLATFORM_ID = "BEAST";
     public final String MAIN_HOST = "localhost";
     public final String MAIN_PORT = "2099";
     public final String AGENTS = "rma:jade.tools.rma.rma";
     public final String SERVICES = "jade.core.messaging.TopicManagementService;jade.core.mobility.AgentMobilityService;jade.core.event.NotificationService;jade.core.replication.MainReplicationService";
 
     private long MILLIS_TO_WAIT_FOR_AGENT_STARTING = 200;
 
     /**
      * @param logger
      */
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
 
         // TODO make this configurable
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
 
         this.createdAgents = new HashMap<String, AgentController>();
         this.platformContainers = new HashMap<String, ContainerController>();
         this.platformContainers.put("Main-Container", mainContainer);
 
         this.createAgent(BEAST_MESSENGER, "es.upm.dit.gsi.beast.platform.jade.agent.MessengerAgent");
 
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
             agentController.start();
             logger.fine("Agent " + agent_name
                     + " created and started in Main Container");
             try {
                 Thread.sleep(MILLIS_TO_WAIT_FOR_AGENT_STARTING);
             } catch (InterruptedException e) {
                 logger.warning("It was not possible to wait for agent starting... Maybe, the agent is not completely started yet.");
             }
         } catch (StaleProxyException e) {
             logger.warning("Exception creating or starting agent in MainContainer... "
                     + e);
         }
         // Register the agent
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
         if (arguments == null) {
             logger.finest("No arguments for agent " + agentName
                     + " in container " + containerName);
            Object reference = new Object();
            arguments = new Object[1];
            arguments[0] = reference;
         }
         try {
             logger.fine("Creating agent " + agentName + " in container "
                     + containerName);
             AgentController agentController = containerController
                    .createNewAgent(agentName, path, arguments);
             this.createdAgents.put(agentName, agentController);
             agentController.start();
             logger.fine("Agent " + agentName
                     + " created and started in Container " + containerName);
             try {
                 Thread.sleep(MILLIS_TO_WAIT_FOR_AGENT_STARTING);
             } catch (InterruptedException e) {
                 logger.warning("It was not possible to wait for agent starting... Maybe, the agent is not completely started yet.");
             }
         } catch (StaleProxyException e) {
             logger.warning("Exception creating or starting agent " + agentName
                     + " in container " + containerName + "... Exception:" + e);
         } catch (Exception e) {
             logger.warning("Exception creating or starting agent " + agentName
                     + " in container " + containerName + "... Exception:" + e);
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
 
             // TODO make this configurable
             Profile p = new ProfileImpl();
             p.setParameter(Profile.PLATFORM_ID, PLATFORM_ID);
             p.setParameter(Profile.MAIN_HOST, MAIN_HOST);
             p.setParameter(Profile.MAIN_PORT, MAIN_PORT);
             p.setParameter(Profile.LOCAL_HOST, MAIN_HOST);
             int port = Integer.parseInt(MAIN_PORT);
             port = port + 1 + this.platformContainers.size();
             p.setParameter(Profile.LOCAL_PORT, Integer.toString(port));
             p.setParameter(Profile.CONTAINER_NAME, container);
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
     public AgentController getAgentID(String agent_name) {
         return this.createdAgents.get(agent_name);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see es.upm.dit.gsi.beast.platform.Connector#getMessageService()
      */
     @Override
     public Agent getMessageService() {
         return JadeAgentIntrospector.getInstance().getAgent(BEAST_MESSENGER);
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
 
     /*
      * (non-Javadoc)
      * 
      * @see es.upm.dit.gsi.beast.platform.Connector#getLogger()
      */
     @Override
     public Logger getLogger() {
         return this.logger;
     }
 
 }
