 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lamprey.seprphase3.DynSimulator;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 import eel.seprphase2.Simulator.Condenser;
 import eel.seprphase2.Simulator.HeatSink;
 import eel.seprphase2.Simulator.Pump;
 import eel.seprphase2.Simulator.Reactor;
 import eel.seprphase2.Simulator.Turbine;
 import eel.seprphase2.Simulator.Valve;
 import eel.seprphase2.Utilities.Energy;
 import static eel.seprphase2.Utilities.Units.joules;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 
 /**
  *
  * @author will
  */
 public class PlantModel {
 
     @JsonProperty
     private Reactor reactor;
     @JsonProperty
     private Turbine turbine;
     @JsonProperty
     private Condenser condenser;
     @JsonProperty
     private Valve reactorToTurbine;
     @JsonProperty
     private Valve bypassValve;
     @JsonProperty
     private Pump condenserToReactor;
     @JsonProperty
     private Pump heatsinkToCondenser;
     @JsonProperty
     private Junction splitAfterReactor;
     @JsonProperty
     private Junction joinBeforeCondenser;
     @JsonProperty
     private HashMap<Integer, Pump> allPumps;
     @JsonProperty
     private HashMap<Integer, Valve> allValves;
     @JsonProperty
     private ArrayList<Junction> allJunctions;
     @JsonProperty
     private ArrayList<FlowThroughComponent> allComponents;
     @JsonProperty
     private ArrayList<BlockableComponent> allBlockable;
     @JsonProperty
     private Energy energyGenerated = joules(0);
     private String currentWornComponent = "";
    private int softwareFailureTimeRemaining = 0;
 
     public PlantModel() {
         instantiateComponents();
         setupLists();
         setupComponentReferences();
     }
 
     private void instantiateComponents() {
         reactor = new Reactor();
         turbine = new Turbine();
         condenser = new Condenser();
         reactorToTurbine = new Valve();
         bypassValve = new Valve();
         condenserToReactor = new Pump();
         heatsinkToCondenser = new Pump();
         splitAfterReactor = new Junction();
         joinBeforeCondenser = new Junction();
     }
     
     
     private void setupLists() {
         allPumps = new HashMap<Integer, Pump>();
         allPumps.put(1, condenserToReactor);
         allPumps.put(2, heatsinkToCondenser);
         
         allValves = new HashMap<Integer, Valve>();
         allValves.put(1, reactorToTurbine);
         allValves.put(2, bypassValve);
         
         allJunctions = new ArrayList<Junction>();
         allJunctions.add(splitAfterReactor);
         allJunctions.add(joinBeforeCondenser);
         
         // All components involved in the flow around the system (i.e. not the heatsink pump)
         allComponents = new ArrayList<FlowThroughComponent>();
         allComponents.add(reactor);
         allComponents.add(condenser);
         allComponents.add(turbine);
         allComponents.addAll(allPumps.values());
         allComponents.addAll(allValves.values());
         allComponents.addAll(allJunctions);
         
     }
 
     private void setupComponentReferences() {
         setupInputOutputReferences(reactor, splitAfterReactor);
         // Path through turbine
         setupInputOutputReferences(splitAfterReactor, reactorToTurbine);
         setupInputOutputReferences(reactorToTurbine, turbine);
         setupInputOutputReferences(turbine, joinBeforeCondenser);
         // Path through bypass valve
         setupInputOutputReferences(splitAfterReactor, bypassValve);
         setupInputOutputReferences(bypassValve, joinBeforeCondenser);
         // Condenser through to reactor
         setupInputOutputReferences(joinBeforeCondenser, condenser);
         setupInputOutputReferences(condenser, condenserToReactor);
         setupInputOutputReferences(condenserToReactor, reactor);
     }
 
     /**
      * Takes two FlowThroughComponents and creates the input/output references between them.
      *
      * @param from FlowThroughComponent that flow is coming out of.
      * @param to   FlowThroughComponent that flow is moving into.
      */
     private void setupInputOutputReferences(FlowThroughComponent from, FlowThroughComponent to) {
         if (from instanceof Junction) {
             ((Junction)from).addOutput(to);
         } else {
             from.output = to;
         }
         if (to instanceof Junction) {
             ((Junction)to).addInput(from);
         } else {
             to.input = from;
         }
     }
 
     public Reactor reactor() {
         return reactor;
     }
 
     public Turbine turbine() {
         return turbine;
     }
 
     public Condenser condenser() {
         return condenser;
     }
     
     public HashMap<Integer, Pump> pumps() {
         return allPumps;
     }
     
     public HashMap<Integer, Valve> valves() {
         return allValves;
     }
     
     public ArrayList<Junction> junctions() {
         return allJunctions;
     }
     
     public ArrayList<FlowThroughComponent> components() {
         return allComponents;
     }
     
     /**
      * Returns all blockable components in the plant.
      * If the list is null then we build it first, then return it.
      * @return a list of all blockable components in the plant.
      */
     public ArrayList<BlockableComponent> blockables() {
         if (allBlockable == null) {
             allBlockable = new ArrayList<BlockableComponent>();
             for (FlowThroughComponent c : allComponents) {
                 if (c instanceof BlockableComponent) {
                     allBlockable.add((BlockableComponent)c);
                 }
             }
         }
         return allBlockable;
     }
     
     public Energy energyGenerated() {
         return energyGenerated;
     }
     
     public void setCurrentWornComponent(String wornComponent) {
         currentWornComponent = wornComponent;
     }
     
    public void setSoftwareFailureTimeRemaining(int failureTime) {
        if(softwareFailureTimeRemaining == 0) {
            softwareFailureTimeRemaining = failureTime;
        }
    }
    
    public void reduceSoftwareFailureTime() {
        if(softwareFailureTimeRemaining>0) {
            softwareFailureTimeRemaining--;
        }
    }
    
     public String getCurrentWornComponent() {
         return currentWornComponent;
     }
     
     public void increaseEnergyGenerated(Energy delta) {
         energyGenerated = energyGenerated.plus(delta);
     }
 }
