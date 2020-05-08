 package eel.seprphase2.Simulator;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 import eel.seprphase2.GameOverException;
 import eel.seprphase2.Utilities.Energy;
 import eel.seprphase2.Utilities.Percentage;
 import eel.seprphase2.Utilities.Pressure;
 import eel.seprphase2.Utilities.Temperature;
 import static eel.seprphase2.Utilities.Units.*;
 import java.util.ArrayList;
 import java.util.Random;
 
 /**
  * Manages software and hardware failures.
  *
  * The FailureModel is responsible for inducing software and hardware failures in the plant.
  *
  * Software failures are currently unimplemented; however, as all calls to the PhysicalModel are delegated through the
  * FailureModel, it is easy to alter this delegation in arbitrary ways to simulate control and/or reporting failures.
  *
  * Hardware failures are determined by inspecting the PhysicalModel to determine the status, wear, and operating
  * conditions of the various components, and then instructing the PhysicalModel to mark certain components as having
  * failed.
  *
  * Failures are constrained to one per timestep, which is enabled by the delegation of the step() method through the
  * FailureModel.
  *
  * @author Marius Dumitrescu
  */
 public class FailureModel implements PlantController, PlantStatus {
 
     @JsonProperty
     PlantController controller;
     @JsonProperty
     PlantStatus status;
     private Random failChance = new Random();
     private final Pressure condenserMaxPressure = new Pressure(30662500);
 
     private FailureModel() {
     }
 
     public FailureModel(PlantController plantController,
                         PlantStatus plantStatus) {
         this.controller = plantController;
         this.status = plantStatus;
     }
 
     /**
      * Step the PhysicalModel and determine any failures.
      *
      * Also implements reactor safety rules.
      *
      */
     public void step() throws GameOverException {
         controller.step(1);
         failStateCheck();
         randomWearCheck();
         checkReactorWaterLevel();
         checkCondenserPressure();
         checkTurbineFailure();
     }
 
     /**
      * Determine if a FailableComponent has failed, which occurs when its wear is 100, if it has, its state is set to failed
      *
      */
     public void failStateCheck() {
              
         for (FailableComponent component: status.components()) {  
             if (component.wear().toString().equals("100%")) {
                 component.fail();               
             }
         }
     }
      /**
      * Determines if random damage has occurred to any of the functioning(non-failed) components.
      *
      */
     public void randomWearCheck(){
         ArrayList<FailableComponent> failingComponents = new ArrayList<FailableComponent>();
         int failValue = 1000;
         int componentFailChance = 0;   
         int faults = 0;
         
         for (FailableComponent component: status.components()) {
             if(component.wear().toString().equals("100%") || component instanceof Reactor ) {
             }                       
             else{
                 componentFailChance = failChance.nextInt(1050);
                 if(componentFailChance>failValue) {
                     failingComponents.add(component);
 		    faults++;
                 }                
             }
         }
         
         if(faults > 0) {
 			int selection = failChance.nextInt(faults);
 			FailableComponent failedComponent = failingComponents.get(selection);
                         Percentage damage = new Percentage(20);
 			failedComponent.addWear(damage);
 	}
     }  
         
    
     @Override
     public String[] listFailedComponents() {
         return status.listFailedComponents();
     }
 
     @Override
     public void moveControlRods(Percentage extracted) {
         controller.moveControlRods(extracted);
     }
 
     @Override
     public void changeValveState(int valveNumber, boolean isOpen) throws KeyNotFoundException {
         controller.changeValveState(valveNumber, isOpen);
     }
 
     @Override
     public void changePumpState(int pumpNumber, boolean isPumping) throws CannotControlException, KeyNotFoundException {
         controller.changePumpState(pumpNumber, isPumping);
     }
 
     @Override
     public void repairPump(int pumpNumber) throws KeyNotFoundException, CannotRepairException {
         controller.repairPump(pumpNumber);
     }
 
     @Override
     public void repairCondenser() throws CannotRepairException {
         controller.repairCondenser();
     }
 
     @Override
     public void repairTurbine() throws CannotRepairException {
         controller.repairTurbine();
     }
 
     @Override
     public Percentage controlRodPosition() {
         return status.controlRodPosition();
     }
 
     @Override
     public Pressure reactorPressure() {
         return status.reactorPressure();
     }
 
     @Override
     public Temperature reactorTemperature() {
         return status.reactorTemperature();
     }
 
     @Override
     public Percentage reactorWaterLevel() {
         return status.reactorWaterLevel();
     }
 
     @Override
     public Energy energyGenerated() {
         return status.energyGenerated();
     }
 
     @Override
     public void setReactorToTurbine(boolean open) {
         controller.setReactorToTurbine(open);
     }
 
     @Override
     public boolean getReactorToTurbine() {
         return status.getReactorToTurbine();
     }
 
     @Override
     public Temperature condenserTemperature() {
         return status.condenserTemperature();
     }
 
     @Override
     public Pressure condenserPressure() {
         return status.condenserPressure();
     }
 
     @Override
     public Percentage condenserWaterLevel() {
         return status.condenserWaterLevel();
     }
 
     @Override
     public Percentage reactorMinimumWaterLevel() {
         return status.reactorMinimumWaterLevel();
     }
 
     @Override
     public void failCondenser() {
         controller.failCondenser();
     }
 
     @Override
     public void wearReactor() {        
         controller.wearReactor();
     }
 
     @Override
     public void step(int i) throws GameOverException {
         controller.step(i);
     }
 
     @Override
     public boolean turbineHasFailed() {
         return status.turbineHasFailed();
     }
 
     @Override
     public ArrayList<FailableComponent> components() {
         return status.components();
     }
 
     private void checkReactorWaterLevel() {
         if (status.reactorWaterLevel().points() < status.reactorMinimumWaterLevel().points()) {
                 controller.wearReactor();  
         }
     }
 
     private void checkCondenserPressure() {
         if (status.condenserPressure().greaterThan(condenserMaxPressure)) {
             controller.failCondenser();
         }
     }
 
     private void checkTurbineFailure() {
         if (status.turbineHasFailed()) {
             controller.moveControlRods(percent(0));
         }
     }
 }
