 package eel.seprphase2.Simulator;
 
 import eel.seprphase2.GameOverException;
 import eel.seprphase2.Utilities.Percentage;
 import eel.seprphase2.Utilities.Pressure;
 import eel.seprphase2.Utilities.Temperature;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static eel.seprphase2.Utilities.Units.*;
 import java.util.ArrayList;
 import lamprey.seprphase3.DynSimulator.FluidFlowController;
 import lamprey.seprphase3.DynSimulator.PlantModel;
 import static org.hamcrest.Matchers.*;
 
 /**
  *
  * @author Marius
  */
 public class PhysicalModelTest {
 
     PlantModel plant = new PlantModel();
     FluidFlowController controller = new FluidFlowController(plant);
     PhysicalModel model = new PhysicalModel(plant);
     @Test
     public void runningStepIncreasesReactorTemperature() throws GameOverException {
         
         controller.moveControlRods(percent(100));
         controller.step(10);
         assertThat(model.reactorTemperature().inKelvin(), greaterThan(350.0));
     }
 
     @Test
     public void reactorMovesTurbine() throws GameOverException {
         
         controller.moveControlRods(percent(100));
         controller.step(100);
         assertThat(model.energyGenerated().inJoules(), greaterThan(0.0));
     }
     
      
     @Test
     public void shouldIncreaseReactorWear() {
         
         controller.wearReactor();
         assertEquals("10%", model.reactorWear().toString());
     }
     
     @Test
     public void shouldIncreaseCondenserWear() {
         
         controller.wearCondenser();
         assertEquals("10%", model.condenserWear().toString());
     }
 
 
     
     @Test
     public void shouldSetControlRodPosition() throws GameOverException {
         
         controller.moveControlRods(percent(100));
         assertTrue(model.controlRodPosition().equals(percent(100)));
     }
     /**
     @Test
     public void shouldSetConnectionToOpena() {
         try{
         controller.changePumpState(1,true);
         assertEquals(true, model.getPumpState(1));
         }
         catch(Exception e) {
             
         }
     }
     
     @Test
     public void shouldSetConnectionToClosed() {
         
         controller.changePumpState(2,false);
         assertEquals(false, model.getReactorToTurbine());
     }
 
     @Test
     public void shouldSetConnectionToOpen() {
         
         controller.setReactorToTurbine(true);
         assertEquals(true, model.getReactorToTurbine());
 
     }
    
      
     @Test
     public void shouldSetCondenserBackToNormalFailureState() {
         
         controller.failCondenser();
         try {
             controller.repairCondenser();
         } catch (CannotRepairException e) {
             fail(e.getMessage());
         }
         assertFalse(model.failableComponents().get(2).hasFailed());
     }
 
     @Test
     public void shouldSetTurbineBackToNormalFailureState() {
         
         model.failableComponents().get(0).fail();
         try {
             controller.repairTurbine();
         } catch (CannotRepairException e) {
             fail(e.getMessage());
         }
         assertFalse(model.failableComponents().get(0).hasFailed());
     }
    */
 
     @Test(expected = CannotRepairException.class)
     public void shouldNotSetCondenserBackToNormalFailureState() throws CannotRepairException {
         
         controller.repairCondenser();
 
     }
 
     @Test(expected = CannotRepairException.class)
     public void shouldNotSetTurbineBackToNormalFailureState() throws CannotRepairException {
        
         controller.repairCondenser();
 
     }
 
     /*@Test
      public void shouldSetPumpBackToPumping() {
      PhysicalModel model = new PhysicalModel();
      model.changePumpState(1, false);
      model.repairPump(1);
      assertEquals(true, model.);
      }*/
     @Test
     public void listNoFailures() {
         
         assertEquals(0, model.listFailedComponents().length);
     }
 
     @Test
     public void listSeveralFailures() {
         
         controller.failCondenser();
         String[] expected = {"Condenser"};
         assertArrayEquals(expected, model.listFailedComponents());
     }
 
     @Test(expected = CannotRepairException.class)
     public void shouldNotSetPumpBackToNormalFailureState() throws CannotRepairException, KeyNotFoundException {
         
         controller.repairPump(1);
     }
 
     @Test
     public void shouldInitializePump2ToPumping() {
         try{
         assertTrue(model.getPumpState(2));
         }
         catch(Exception e) {
             
         }
     
     
     }
 
     @Test
     public void shouldInitializePump1ToPumping() {
         try {
         assertTrue(model.getPumpState(1));
         }
         catch(Exception e) {
             
         }
 
     }
 
     @Test
     public void shouldSetPumpStateToOff() throws CannotControlException, KeyNotFoundException {
         
         assertTrue(model.getPumpState(1));
         controller.changePumpState(1, false);
         assertFalse(model.getPumpState(1));
     }
 
     @Test
     public void shouldSetPumpStateToOn() throws CannotControlException, KeyNotFoundException {
         
         assertTrue(model.getPumpState(1));
         controller.changePumpState(1, false);
         assertFalse(model.getPumpState(1));
         controller.changePumpState(1, true);
         assertTrue(model.getPumpState(1));
     }
     
     @Test(expected = KeyNotFoundException.class)
     public void shouldRefuseToRepairInvalidPump() throws KeyNotFoundException, CannotRepairException {
         
         controller.repairPump(100);
     }
 
     @Test(expected = KeyNotFoundException.class)
     public void shouldRefuseToControlInvalidPump() throws CannotControlException, KeyNotFoundException {
         
         controller.changePumpState(100, true);
     }
 
     @Test(expected = KeyNotFoundException.class)
     public void shouldRefuseToControlInvalidValve() throws KeyNotFoundException {
         
         controller.changeValveState(100, true);
     }
     
     @Test
     public void doesReturnValveState() throws KeyNotFoundException {
                   
         try {       
             controller.changeValveState(1, true); 
             assertTrue(model.getValveState(1));
             controller.changeValveState(1, false); 
             assertFalse(model.getValveState(1));
         }
         catch (Exception e){
             
         }
     }
     
     @Test(expected = KeyNotFoundException.class)
     public void shouldRefuseToGiveStateOfInvalidValve() throws KeyNotFoundException {
         
         model.getValveState(100);
     }    
    
     @Test
     public void doesReturnPumpState() throws KeyNotFoundException {
                   
         try {       
             controller.changePumpState(1, true); 
             assertTrue(model.getPumpState(1));
             controller.changePumpState(1, false); 
             assertFalse(model.getPumpState(1));
         }
         catch (Exception e){
             
         }
     }
     
     @Test(expected = KeyNotFoundException.class)
     public void shouldRefuseToGiveStateOfInvalidPump() throws KeyNotFoundException {
         
         model.getPumpState(100);
     }
     
     @Test
     public void doesReturnPumpWear() throws KeyNotFoundException {
          
         ArrayList<FailableComponent> c = model.failableComponents();
         Percentage wear = new Percentage(40);
         c.get(3).addWear(wear); // 3 is reference of condenserToReactor
         try {       
             assertEquals(percent(40), model.getPumpWear(1));
         }
         catch (Exception e){
             
         }
     }
     
     @Test(expected = KeyNotFoundException.class)
     public void shouldRefuseToGiveWearOfInvalidPump() throws KeyNotFoundException {
         
         model.getPumpWear(100);
     }
     
     @Test
     public void doesReturnTurbineWear() {
         
         ArrayList<FailableComponent> c = model.failableComponents();       
         Percentage wear = new Percentage(15);
         c.get(0).addWear(wear); // 0 is reference of turbine
         assertEquals(percent(15), model.turbineWear());
     }
     
     @Test
     public void doesReturnCondenserWear() {
             
         ArrayList<FailableComponent> c = model.failableComponents();
         Percentage wear = new Percentage(50);
         c.get(2).addWear(wear); // 2 is reference of condenser
         assertEquals(percent(50), model.condenserWear());
     }
     
     @Test
     public void doesReturnReactorWear() {
           
         ArrayList<FailableComponent> c = model.failableComponents();
         Percentage wear = new Percentage(30);
         c.get(1).addWear(wear); // 1 is reference of reactor
         assertEquals(percent(30), model.reactorWear());        
     }
     
     @Test 
     public void doesNotReturnReactorWear() {
            
         ArrayList<FailableComponent> c = model.failableComponents();
         Percentage wear = new Percentage(30);
         c.get(0).addWear(wear); // 0 is reference of turbine, hence this should not work
         assertTrue( model.reactorWear() != percent(30));          
     }
     
     @Test
     public void setsCurrentWornComponentBlank() { 
         
         Reactor reactor = new Reactor(percent(0), percent(100),
                                                 kelvin(350), pascals(101325));
         controller.setWornComponent(reactor);        //Reactor cannot be randomly worn, this checks it just sets it is blank if this is passed to it
         assertEquals("", model.wornComponent());
     }
     
     @Test
      public void setsCurrentWornComponentTurbine() { 
         
         Turbine turbine = new Turbine();
         controller.setWornComponent(turbine);        //Checks to see if this method correctly updates the variable currentWornComponent in model to Turbine.
         assertEquals("Turbine", model.wornComponent());
     }
     
     @Test
     public void returnsCurrentWornComponent() {
         
         Condenser condenser = new Condenser();
         controller.setWornComponent(condenser);
         assertEquals("Condenser", model.wornComponent());
     }
 }
