 package be.kuleuven.cs.gridlock.dmas.coordination.vehicle.behaviors;
 
import be.kuleuven.cs.gridlock.case1.coordination.vehicle.behaviors.AdvisedEVChargingBehavior;
 import be.kuleuven.cs.gridlock.dmas.coordination.delegate.model.IDMASModelAPI;
 import be.kuleuven.cs.gridlock.dmas.coordination.delegate.pheromones.PheromoneFactory;
 import be.kuleuven.cs.gridlock.simulation.SimulationContext;
 import be.kuleuven.cs.gridlock.simulation.events.EventController;
 import be.kuleuven.cs.gridlock.simulation.instrumentation.InstrumentationComponent;
 
 /**
  *
  * @author Kristof Coninx <kristof.coninx at student.kuleuven.be>
  */
 public class ElecVehicleBehaviorFactory implements InstrumentationComponent {
 
     private double defaultPercent;
     private int period;
     private IDMASModelAPI api;
     private PheromoneFactory pf;
     private double pessimism;
     public static final String STEPTIME_KEY = "value.dmas.behavior.steptime";
     public static final String PESSIMISM_KEY = "value.dmas.intention.pessimism";
     private EventController eventcontroller;
 
     @Override
     public void initialize(SimulationContext simulationContext) {
         //TODO read from config
         this.defaultPercent = 0.6;
         this.period = simulationContext.getConfiguration().getInt(STEPTIME_KEY, 10);
         api = simulationContext.getSimulationComponent(IDMASModelAPI.class);
         pf = simulationContext.getInstrumentationComponent(PheromoneFactory.class);
         this.pessimism = simulationContext.getConfiguration().getDouble(PESSIMISM_KEY, 0);
         this.eventcontroller = simulationContext.getEventController();
     }
 
     public IElecVehicleBehavior createNaiveChargingBehavior() {
         return new NaiveEVChargingBehavior();
     }
 
     public IElecVehicleBehavior createPostponingChargingBehavior() {
         return new PostponingEVChargingBehavior(defaultPercent);
     }
 
     public IElecVehicleBehavior createEmptyTankChargingBehavior() {
         return new PostponingEVChargingBehavior(0);
     }
 
     public IElecVehicleBehavior createReferenceChargingBehavior() {
         return new ReferenceEVChargingBehavior();
     }
 
     public IElecVehicleBehavior createIntentionDMASBehavior() {
         return new IntentionDMASBehavior(period, this.api, pf, pessimism, eventcontroller);
     }
 
     public IElecVehicleBehavior createExplorationDMASBehavior() {
         return new ExplorationDMASBehavior(period, this.api);
     }
 
     public IElecVehicleBehavior createAdvisedChargingBehavior() {
         return new AdvisedEVChargingBehavior(eventcontroller);
     }
 }
