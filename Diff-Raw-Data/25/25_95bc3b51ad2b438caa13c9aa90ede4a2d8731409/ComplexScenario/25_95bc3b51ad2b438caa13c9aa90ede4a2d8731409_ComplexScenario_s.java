 /** es.upm.dit.gsi.shanks
  20/12/2011
 
  */
 package es.upm.dit.gsi.shanks.model.scenario;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import es.upm.dit.gsi.shanks.ShanksSimulation;
 import es.upm.dit.gsi.shanks.model.element.NetworkElement;
 import es.upm.dit.gsi.shanks.model.element.device.Device;
 import es.upm.dit.gsi.shanks.model.element.exception.TooManyConnectionException;
 import es.upm.dit.gsi.shanks.model.element.exception.UnsupportedNetworkElementStatusException;
 import es.upm.dit.gsi.shanks.model.element.link.Link;
 import es.upm.dit.gsi.shanks.model.failure.Failure;
 import es.upm.dit.gsi.shanks.model.failure.exception.NoCombinationForFailureException;
 import es.upm.dit.gsi.shanks.model.failure.exception.UnsupportedElementInFailureException;
 import es.upm.dit.gsi.shanks.model.scenario.exception.AlreadyConnectedScenarioException;
 import es.upm.dit.gsi.shanks.model.scenario.exception.DuplicatedIDException;
 import es.upm.dit.gsi.shanks.model.scenario.exception.NonGatewayDeviceException;
 import es.upm.dit.gsi.shanks.model.scenario.exception.ScenarioNotFoundException;
 import es.upm.dit.gsi.shanks.model.scenario.exception.UnsupportedScenarioStatusException;
 
 /**
  * @author a.carrera
  * 
  */
 public abstract class ComplexScenario extends Scenario {
 
     private HashMap<Scenario, List<Link>> scenarios;
 
     /**
      * Create a complex scenario to compose the whole scenario adding other
      * scenarios.
      * 
      * @param type
      * @param initialState
      * @param properties
      * @throws UnsupportedNetworkElementStatusException
      * @throws TooManyConnectionException
      * @throws UnsupportedScenarioStatusException
      * @throws DuplicatedIDException
      * @throws NonGatewayDeviceException
      * @throws AlreadyConnectedScenarioException
      * @throws InvocationTargetException 
      * @throws IllegalAccessException 
      * @throws InstantiationException 
      * @throws NoSuchMethodException 
      * @throws IllegalArgumentException 
      * @throws SecurityException 
      */
     public ComplexScenario(String type, String initialState,
             Properties properties)
             throws UnsupportedNetworkElementStatusException,
             TooManyConnectionException, UnsupportedScenarioStatusException,
             DuplicatedIDException, NonGatewayDeviceException,
             AlreadyConnectedScenarioException, SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
         super(type, initialState, properties);
         this.scenarios = new HashMap<Scenario, List<Link>>();
 
         this.addScenarios();
     }
 
     /**
      * Add all scenarios to the simulation using addScenario method
      * 
      * @throws DuplicatedIDException
      * @throws UnsupportedScenarioStatusException
      * @throws TooManyConnectionException
      * @throws UnsupportedNetworkElementStatusException
      * @throws AlreadyConnectedScenarioException
      * @throws NonGatewayDeviceException
      * @throws InvocationTargetException 
      * @throws IllegalAccessException 
      * @throws InstantiationException 
      * @throws NoSuchMethodException 
      * @throws IllegalArgumentException 
      * @throws SecurityException 
      */
     abstract public void addScenarios()
             throws UnsupportedNetworkElementStatusException,
             TooManyConnectionException, UnsupportedScenarioStatusException,
             DuplicatedIDException, NonGatewayDeviceException,
             AlreadyConnectedScenarioException, SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException;
 
     /**
      * Add the scenario to the complex scenario.
      * 
      * @param scenarioClass
      * @param scenarioID
      * @param initialState
      * @param properties
      * @param gatewayDeviceID
      * @param externalLinkID
      * @throws NonGatewayDeviceException
      * @throws TooManyConnectionException
      * @throws DuplicatedIDException
      * @throws AlreadyConnectedScenarioException
      * @throws SecurityException
      * @throws NoSuchMethodException
      * @throws IllegalArgumentException
      * @throws InstantiationException
      * @throws IllegalAccessException
      * @throws InvocationTargetException
      */
     public void addScenario(Class<? extends Scenario> scenarioClass, String scenarioID, String initialState, Properties properties, String gatewayDeviceID, String externalLinkID)
             throws NonGatewayDeviceException, TooManyConnectionException,
             DuplicatedIDException, AlreadyConnectedScenarioException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
         Constructor<? extends Scenario> c  = scenarioClass.getConstructor(new Class[]{String.class,String.class,Properties.class});
         
         Scenario scenario = c.newInstance(scenarioID,initialState,properties);
 
         Device gateway = (Device) scenario.getNetworkElement(gatewayDeviceID);
         Link externalLink = (Link) this.getNetworkElement(externalLinkID);
         if (!scenario.getCurrentElements().containsKey(gatewayDeviceID)) {
             throw new NonGatewayDeviceException(scenario, gateway, externalLink);
         } else {
             gateway.connectToLink(externalLink);
             if (!this.getCurrentElements().containsKey(externalLink.getID())) {
                 this.addNetworkElement(externalLink);
             }
             if (!this.scenarios.containsKey(scenario)) {
                 this.scenarios.put(scenario, new ArrayList<Link>());
             } else {
                 List<Link> externalLinks = this.scenarios.get(scenario);
                 if (!externalLinks.contains(externalLink)) {
                     externalLinks.add(externalLink);
                 } else if (externalLink.getLinkedDevices().contains(gateway)) {
                     throw new AlreadyConnectedScenarioException(scenario,
                             gateway, externalLink);
                 }
             }
         }
     }
 
     /**
      * @return set of the scenarios that are in the complex scenario
      */
     public Set<Scenario> getScenarios() {
         return this.scenarios.keySet();
     }
 
     /**
      * @param scenarioID
      * @return null if the ComplexScenario does not contains the searched
      *         scenarioID
      * @throws ScenarioNotFoundException 
      */
     public Scenario getScenario(String scenarioID) throws ScenarioNotFoundException {
         for (Scenario s : this.scenarios.keySet()) {
             if (s.getID().equals(scenarioID)) {
                 return s;
             }
         }
         throw new ScenarioNotFoundException(scenarioID,this.getID());
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see es.upm.dit.gsi.shanks.model.scenario.Scenario#generateFailures()
      */
     public void generateNetworkElementsEvents(ShanksSimulation sim) throws UnsupportedScenarioStatusException,
             NoCombinationForFailureException,
             UnsupportedElementInFailureException, InstantiationException,
             IllegalAccessException, SecurityException, IllegalArgumentException, UnsupportedNetworkElementStatusException, NoSuchMethodException, InvocationTargetException {
         super.generateFailures();
         for (Scenario scenario : this.scenarios.keySet()) {
             scenario.generateNetworkElementEvents(sim);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * es.upm.dit.gsi.shanks.model.scenario.Scenario#checkResolvedFailures()
      */
     public List<Failure> checkResolvedFailures() {
         List<Failure> fullList = new ArrayList<Failure>();
 
         fullList.addAll(super.checkResolvedFailures());
 
         for (Scenario scenario : this.scenarios.keySet()) {
             fullList.addAll(scenario.checkResolvedFailures());
         }
 
         return fullList;
     }
 
     /**
      * @return All failures classified by scenario of the complex scenario
      */
     public HashMap<Scenario, Set<Failure>> getCurrentFailuresByScenario() {
         HashMap<Scenario, Set<Failure>> maps = new HashMap<Scenario, Set<Failure>>();
         maps.put(this, this.getCurrentFailures());
         for (Scenario s : this.getScenarios()) {
             maps.put(s, s.getCurrentFailures());
         }
 
         return maps;
     }
     
     /* (non-Javadoc)
      * @see es.upm.dit.gsi.shanks.model.scenario.Scenario#getCurrentFailures()
      */
     public Set<Failure> getCurrentFailures() {
         Set<Failure> failures = new HashSet<Failure>();
         failures.addAll(this.getFullCurrentFailures().keySet());
         for(Scenario s : this.getScenarios()) {
             failures.addAll(s.getCurrentFailures());
         }
         return failures;
     }
 
     /**
      * @return All elements classified by scenario of the complex scenario
      */
     public HashMap<Scenario, HashMap<String, NetworkElement>> getCurrentElementsByScenario() {
         HashMap<Scenario, HashMap<String, NetworkElement>> maps = new HashMap<Scenario, HashMap<String, NetworkElement>>();
         maps.put(this, this.getCurrentElements());
         for (Scenario s : this.getScenarios()) {
             maps.put(s, s.getCurrentElements());
         }
 
         return maps;
     }

 }
