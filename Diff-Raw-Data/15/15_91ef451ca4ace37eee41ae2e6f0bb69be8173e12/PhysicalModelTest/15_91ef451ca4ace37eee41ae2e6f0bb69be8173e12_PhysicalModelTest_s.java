 package eel.seprphase2.Simulator;
 
 
 import eel.seprphase2.GameOverException;
 import eel.seprphase2.Utilities.Percentage;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static eel.seprphase2.Utilities.Units.*;
 import java.util.ArrayList;
 import static org.hamcrest.Matchers.*;
 
 /**
  *
  * @author Marius
  */
 public class PhysicalModelTest {
 
     @Test
     public void runningStepIncreasesReactorTemperature() throws GameOverException {
         PhysicalModel model = new PhysicalModel();
         model.moveControlRods(percent(100));
         model.step(10);
         assertThat(model.reactorTemperature().inKelvin(), greaterThan(350.0));
     }
 
     @Test
     public void reactorMovesTurbine() throws GameOverException {
         PhysicalModel model = new PhysicalModel();
         model.moveControlRods(percent(100));
         model.step(100);
         assertThat(model.energyGenerated().inJoules(), greaterThan(0.0));
     }
     
      
     @Test
     public void shouldIncreaseReactorWear() {
         PhysicalModel model = new PhysicalModel();
         model.wearReactor();
         assertEquals("10%", model.reactorWear().toString());
     }
     
     @Test
     public void shouldIncreaseCondenserWear() {
         PhysicalModel model = new PhysicalModel();
         model.wearCondenser();
         assertEquals("10%", model.condenserWear().toString());
     }
 
 
     
     @Test
     public void shouldSetControlRodPosition() throws GameOverException {
         PhysicalModel model = new PhysicalModel();
         model.moveControlRods(percent(100));
         assertTrue(model.controlRodPosition().equals(percent(100)));
     }
     
     @Test
     public void shouldSetConnectionToOpena() {
         PhysicalModel model = new PhysicalModel();
         model.setReactorToTurbine(true);
         assertEquals(true, model.getReactorToTurbine());
     }
     
     @Test
     public void shouldSetConnectionToClosed() {
         PhysicalModel model = new PhysicalModel();
         model.setReactorToTurbine(false);
         assertEquals(false, model.getReactorToTurbine());
     }
 
     @Test
     public void shouldSetConnectionToOpen() {
         PhysicalModel model = new PhysicalModel();
         model.setReactorToTurbine(true);
         assertEquals(true, model.getReactorToTurbine());
 
     }
      /**
     @Test
     public void shouldSetCondenserBackToNormalFailureState() {
         PhysicalModel model = new PhysicalModel();
         model.failCondenser();
         try {
             model.repairCondenser();
         } catch (CannotRepairException e) {
             fail(e.getMessage());
         }
         assertFalse(model.components().get(2).hasFailed());
     }
 
     @Test
     public void shouldSetTurbineBackToNormalFailureState() {
         PhysicalModel model = new PhysicalModel();       
         model.components().get(0).fail();
         try {
             model.repairTurbine();
         } catch (CannotRepairException e) {
             fail(e.getMessage());
         }
         assertFalse(model.components().get(0).hasFailed());
     }
     **/
 
     @Test(expected = CannotRepairException.class)
     public void shouldNotSetCondenserBackToNormalFailureState() throws CannotRepairException {
         PhysicalModel model = new PhysicalModel();
 
 
         model.repairCondenser();
 
     }
 
     @Test(expected = CannotRepairException.class)
     public void shouldNotSetTurbineBackToNormalFailureState() throws CannotRepairException {
         PhysicalModel model = new PhysicalModel();
 
 
         model.repairCondenser();
 
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
         PhysicalModel pm = new PhysicalModel();
         assertEquals(0, pm.listFailedComponents().length);
     }
 
     @Test
     public void listSeveralFailures() {
         PhysicalModel pm = new PhysicalModel();
         pm.failCondenser();
         String[] expected = {"Condenser"};
         assertArrayEquals(expected, pm.listFailedComponents());
     }
 
     @Test(expected = CannotRepairException.class)
     public void shouldNotSetPumpBackToNormalFailureState() throws CannotRepairException, KeyNotFoundException {
         PhysicalModel model = new PhysicalModel();
         model.repairPump(1);
     }
 
     @Test
     public void shouldInitializePump2ToPumping() {
         PhysicalModel model = new PhysicalModel();
         assertTrue(model.getPumpStatus(2));
     }
 
     @Test
     public void shouldInitializePump1ToPumping() {
         PhysicalModel model = new PhysicalModel();
         assertTrue(model.getPumpStatus(1));
 
     }
      @Test
     public void shouldSetPumpStateToOff() throws CannotControlException, KeyNotFoundException {
         PhysicalModel model = new PhysicalModel();
         assertTrue(model.getPumpStatus(1));
         model.changePumpState(1, false);
         assertFalse(model.getPumpStatus(1));
     }
 
     @Test
     public void shouldSetPumpStateToOn() throws CannotControlException, KeyNotFoundException {
         PhysicalModel model = new PhysicalModel();
         assertTrue(model.getPumpStatus(1));
         model.changePumpState(1, false);
         assertFalse(model.getPumpStatus(1));
         model.changePumpState(1, true);
         assertTrue(model.getPumpStatus(1));
     }
     
     @Test(expected = KeyNotFoundException.class)
     public void shouldRefuseToRepairInvalidPump() throws KeyNotFoundException, CannotRepairException {
         PhysicalModel model = new PhysicalModel();
         model.repairPump(100);
     }
 
     @Test(expected = KeyNotFoundException.class)
     public void shouldRefuseToControlInvalidPump() throws CannotControlException, KeyNotFoundException {
         PhysicalModel model = new PhysicalModel();
         model.changePumpState(100, true);
     }
 
     @Test(expected = KeyNotFoundException.class)
     public void shouldRefuseToControlInvalidValve() throws KeyNotFoundException {
         PhysicalModel model = new PhysicalModel();
         model.changeValveState(100, true);
     }
     
     @Test
     public void doesReturnCondenserToReactorWear() {
         PhysicalModel model = new PhysicalModel();   
         ArrayList<FailableComponent> c = model.components();
         Percentage wear = new Percentage(40);
         c.get(3).addWear(wear); // 3 is reference of condenserToReactor
        assertEquals(percent(40), model.condenserToReactorWear());
     }
     
     @Test
     public void doesReturnHeatsinkToCondenserWear() {
         PhysicalModel model = new PhysicalModel();   
         ArrayList<FailableComponent> c = model.components();
         Percentage wear = new Percentage(67);
         c.get(4).addWear(wear); // 4 is reference of heatsinkToCondenser
        assertEquals(percent(67), model.heatsinkToCondenserWear());
     }
     
     @Test
     public void doesReturnTurbineWear() {
         PhysicalModel model = new PhysicalModel();
         ArrayList<FailableComponent> c = model.components();       
         Percentage wear = new Percentage(15);
         c.get(0).addWear(wear); // 0 is reference of turbine
         assertEquals(percent(15), model.turbineWear());
     }
     
     @Test
     public void doesReturnCondenserWear() {
         PhysicalModel model = new PhysicalModel();    
         ArrayList<FailableComponent> c = model.components();
         Percentage wear = new Percentage(50);
         c.get(2).addWear(wear); // 2 is reference of condenser
         assertEquals(percent(50), model.condenserWear());
     }
     
     @Test
     public void doesReturnReactorWear() {
         PhysicalModel model = new PhysicalModel();   
         ArrayList<FailableComponent> c = model.components();
         Percentage wear = new Percentage(30);
         c.get(1).addWear(wear); // 1 is reference of reactor
         assertEquals(percent(30), model.reactorWear());        
     }
     
     @Test 
     public void doesNotReturnReactorWear() {
         PhysicalModel model = new PhysicalModel();   
         ArrayList<FailableComponent> c = model.components();
         Percentage wear = new Percentage(30);
         c.get(0).addWear(wear); // 0 is reference of turbine, hence this should not work
         assertTrue( model.reactorWear() != percent(30));          
     }
     
     @Test
     public void setsCurrentWornComponentBlank() { 
         PhysicalModel model = new PhysicalModel();
         Reactor reactor = new Reactor(percent(0), percent(100),
                                                 kelvin(350), pascals(101325));
         model.setWornComponent(reactor);        //Reactor cannot be randomly worn, this checks it just sets it is blank if this is passed to it
         assertEquals("", model.wornComponent());
     }
     
     @Test
      public void setsCurrentWornComponentTurbine() { 
         PhysicalModel model = new PhysicalModel();
         Turbine turbine = new Turbine();
         model.setWornComponent(turbine);        //Checks to see if this method correctly updates the variable currentWornComponent in model to Turbine.
         assertEquals("Turbine", model.wornComponent());
     }
     
     @Test
     public void returnsCurrentWornComponent() {
         PhysicalModel model = new PhysicalModel();
         Condenser condenser = new Condenser();
         model.setWornComponent(condenser);
         assertEquals("Condenser", model.wornComponent());
     }
 }
