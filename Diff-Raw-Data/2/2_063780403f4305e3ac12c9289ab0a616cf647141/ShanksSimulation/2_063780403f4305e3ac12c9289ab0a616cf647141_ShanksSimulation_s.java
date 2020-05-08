 package es.upm.dit.gsi.shanks;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import sim.engine.Schedule;
 import sim.engine.SimState;
 import es.upm.dit.gsi.shanks.agent.ShanksAgent;
 import es.upm.dit.gsi.shanks.agent.exception.DuplicatedActionIDException;
 import es.upm.dit.gsi.shanks.exception.DuplicatedAgentIDException;
 import es.upm.dit.gsi.shanks.exception.UnkownAgentException;
 import es.upm.dit.gsi.shanks.model.ScenarioManager;
 import es.upm.dit.gsi.shanks.model.element.exception.TooManyConnectionException;
 import es.upm.dit.gsi.shanks.model.element.exception.UnsupportedNetworkElementStatusException;
 import es.upm.dit.gsi.shanks.model.scenario.Scenario;
 import es.upm.dit.gsi.shanks.model.scenario.exception.DuplicatedIDException;
 import es.upm.dit.gsi.shanks.model.scenario.exception.ScenarioNotFoundException;
 import es.upm.dit.gsi.shanks.model.scenario.exception.UnsupportedScenarioStatusException;
 import es.upm.dit.gsi.shanks.model.scenario.portrayal.ScenarioPortrayal;
 import es.upm.dit.gsi.shanks.model.scenario.portrayal.exception.DuplicatedPortrayalIDException;
 
 /**
  * Model class
  * 
  * This class represents the model which manage all the simulation
  * 
  * @author Daniel Lara
  * @version 0.1
  * 
  */
 
 public class ShanksSimulation extends SimState {
 
     private static final long serialVersionUID = -2238530527253654867L;
 
     public Logger logger = Logger.getLogger(ShanksSimulation.class.getName());
 
     private ScenarioManager scenarioManager;
 
     private HashMap<String, ShanksAgent> agents;
 
     private int numOfResolvedFailures;
 
     /**
      * @param seed
      * @param scenarioClass
      * @param scenarioID
      * @param initialState
      * @param properties
      * @throws SecurityException
      * @throws IllegalArgumentException
      * @throws NoSuchMethodException
      * @throws InstantiationException
      * @throws IllegalAccessException
      * @throws InvocationTargetException
      * @throws UnsupportedNetworkElementStatusException
      * @throws TooManyConnectionException
      * @throws UnsupportedScenarioStatusException
      * @throws DuplicatedIDException
      * @throws DuplicatedPortrayalIDException
      * @throws ScenarioNotFoundException
      * @throws DuplicatedActionIDException
      * @throws DuplicatedAgentIDException
      */
     public ShanksSimulation(long seed, Class<? extends Scenario> scenarioClass,
             String scenarioID, String initialState, Properties properties)
             throws SecurityException, IllegalArgumentException,
             NoSuchMethodException, InstantiationException,
             IllegalAccessException, InvocationTargetException,
             UnsupportedNetworkElementStatusException,
             TooManyConnectionException, UnsupportedScenarioStatusException,
             DuplicatedIDException, DuplicatedPortrayalIDException,
             ScenarioNotFoundException, DuplicatedAgentIDException,
             DuplicatedActionIDException {
         super(seed);
         this.scenarioManager = this.createScenarioManager(scenarioClass,
                 scenarioID, initialState, properties);
         this.agents = new HashMap<String, ShanksAgent>();
         this.registerShanksAgents();
     }
 
     /**
      * Register all agents to the scenario
      * 
      * @throws DuplicatedActionIDException
      * @throws DuplicatedAgentIDException
      */
     public void registerShanksAgents() throws DuplicatedAgentIDException,
             DuplicatedActionIDException {
         logger.info("No agents to add...");
     }
 
     /**
      * This method will set all required information about Scenario
      * 
      * @return the completed Scenario object
      * @throws DuplicatedIDException
      * @throws UnsupportedScenarioStatusException
      * @throws TooManyConnectionException
      * @throws UnsupportedNetworkElementStatusException
      * @throws NoSuchMethodException
      * @throws SecurityException
      * @throws InvocationTargetException
      * @throws IllegalAccessException
      * @throws InstantiationException
      * @throws IllegalArgumentException
      * @throws DuplicatedPortrayalIDException
      * @throws ScenarioNotFoundException
      */
     private ScenarioManager createScenarioManager(
             Class<? extends Scenario> scenarioClass, String scenarioID,
             String initialState, Properties properties)
             throws UnsupportedNetworkElementStatusException,
             TooManyConnectionException, UnsupportedScenarioStatusException,
             DuplicatedIDException, SecurityException, NoSuchMethodException,
             IllegalArgumentException, InstantiationException,
             IllegalAccessException, InvocationTargetException,
             DuplicatedPortrayalIDException, ScenarioNotFoundException {
 
         Constructor<? extends Scenario> c = scenarioClass
                 .getConstructor(new Class[] { String.class, String.class,
                         Properties.class });
 
         Scenario s = c.newInstance(scenarioID, initialState, properties);
         logger.fine("Scenario created");
         ScenarioPortrayal sp = s.createScenarioPortrayal();
         if (sp == null) {
             logger.warning("ScenarioPortrayals is null");
         }
         ScenarioManager sm = new ScenarioManager(s, sp);
         return sm;
     }
 
     /**
      * @return ScenarioManager of the simulation
      */
     public ScenarioManager getScenarioManager() {
         return this.scenarioManager;
     }
 
     /**
      * @return Scenario of the simulation
      */
     public Scenario getScenario() {
         return this.scenarioManager.getScenario();
     }
 
     /**
      * @return ScenarioPortrayal of the scenario of the simulation
      * @throws DuplicatedPortrayalIDException
      * @throws ScenarioNotFoundException
      */
     public ScenarioPortrayal getScenarioPortrayal()
             throws DuplicatedPortrayalIDException, ScenarioNotFoundException {
         ScenarioPortrayal sp = this.scenarioManager.getPortrayal();
        while (sp == null) {
             sp = this.scenarioManager.getScenario().createScenarioPortrayal();
             this.scenarioManager.setPortrayal(sp);
         }
         return sp;
     }
 
     /**
      * @return the numOfResolvedFailures
      */
     public int getNumOfResolvedFailures() {
         return numOfResolvedFailures;
     }
 
     /**
      * @param numOfResolvedFailures
      *            the numOfResolvedFailures to set
      */
     public void setNumOfResolvedFailures(int numOfResolvedFailures) {
         this.numOfResolvedFailures = numOfResolvedFailures;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see sim.engine.SimState#start()
      */
     @Override
     public void start() {
         super.start();
         logger.finer("-> start method");
         try {
             try {
                 startSimulation();
             } catch (DuplicatedActionIDException e) {
                 logger.severe("DuplicatedActionIDException: " + e.getMessage());
                 e.printStackTrace();
             }
         } catch (DuplicatedAgentIDException e) {
             logger.warning("DuplicatedAgentIDException: " + e.getMessage()
                     + ". Older agent has survived, new agent was not started.");
         }
     }
 
     /**
      * The initial configuration of the scenario
      * 
      * @throws DuplicatedAgentIDException
      * @throws DuplicatedActionIDException
      */
     public void startSimulation() throws DuplicatedAgentIDException,
             DuplicatedActionIDException {
         schedule.scheduleRepeating(Schedule.EPOCH, 0, this.scenarioManager, 2);
         this.addAgents();
         this.addSteppables();
 
     }
 
     /**
      * Add ShanksAgent's to the simulation using registerShanksAgent method
      * 
      * @throws DuplicatedActionIDException
      */
     private void addAgents() throws DuplicatedAgentIDException,
             DuplicatedActionIDException {
         for (Entry<String, ShanksAgent> agentEntry : this.agents.entrySet()) {
             schedule.scheduleRepeating(Schedule.EPOCH, 2, agentEntry.getValue(), 1);
         }
     }
 
     /**
      * This method adds and registers the ShanksAgent
      * 
      * @param agent
      *            The ShanksAgent
      * @param order
      *            The agent will be executed in this order
      * @param interval
      *            The agent will be executed every "x=interval" steps
      * @throws DuplicatedAgentIDException
      */
     public void registerShanksAgent(ShanksAgent agent)
             throws DuplicatedAgentIDException {
         if (!this.agents.containsKey(agent.getID())) {
             this.agents.put(agent.getID(), agent);
         } else {
             throw new DuplicatedAgentIDException(agent.getID());
         }
     }
 
     /**
      * @param agentID
      * @return ShanksAgent with ID equals to agentID
      * @throws UnkownAgentException
      */
     public ShanksAgent getAgent(String agentID) throws UnkownAgentException {
         if (this.agents.containsKey(agentID)) {
             return this.agents.get(agentID);
         } else {
             throw new UnkownAgentException(agentID);
         }
     }
     
     /**
      * @return A collection with all agents
      */
     public Collection<ShanksAgent> getAgents() {
         return this.agents.values();
     }
 
     /**
      * It is required to add Steppables
      * 
      * @return Schedule of the simulation
      */
     public Schedule getSchedule() {
         return schedule;
     }
 
     /**
      * This method is called during the start phase of the simulation. The
      * command: schedule.scheduleRepeating(Schedule.EPOCH, 0,
      * this.scenarioManager, 2); is always executed in the first place.
      * 
      * In this method, for example, the steppable responsible of print graphics
      * can be added.
      */
     public void addSteppables() {
         logger.info("No steppables added...");
     }
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         doLoop(ShanksSimulation.class, args);
         System.exit(0);
     }
 
 }
