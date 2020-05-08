 /**
  * 
  */
 package uk.co.craigwarren.gascalc.blender;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import uk.co.craigwarren.gascalc.UnitConverter;
 import uk.co.craigwarren.gascalc.exception.ImpossibleMixException;
 import uk.co.craigwarren.gascalc.gaslaw.IdealGasLaw;
 import uk.co.craigwarren.gascalc.model.Cylinder;
 import uk.co.craigwarren.gascalc.model.Element;
 import uk.co.craigwarren.gascalc.model.FillRequirement;
 import uk.co.craigwarren.gascalc.model.Gas;
 import uk.co.craigwarren.gascalc.model.filling.FillAction;
 import uk.co.craigwarren.gascalc.model.filling.FillPlan;
 import uk.co.craigwarren.gascalc.model.filling.FillStep;
 
 /**
  * Integration Test for the gas law blender
  * 
  * @author craig
  *
  */
 public class GasLawBlenderIntTest {
     
     @Test
     public void testNitroxMixWithIdealGasLaws() throws ImpossibleMixException{
         GasBlender blender = new GasLawBlender(new IdealGasLaw());
         FillRequirement requirement = new FillRequirement(new Cylinder(12), 230.0, new Gas(30,70));
         FillPlan plan = blender.blendGas(requirement);
         Assert.assertEquals("There should be two steps in the plan",2,plan.size());
         FillStep step1 = plan.get(0);
         Assert.assertEquals("The first step should be a fill",FillAction.FILL,step1.getAction());
         Assert.assertEquals("The First action should use Oxygen", new Gas(Element.OXYGEN),step1.getGas());
         Assert.assertEquals("The First step pressure is incorrect", 26.2, step1.getPressure(),0.1);
         FillStep step2 = plan.get(1);
         Assert.assertEquals("The second step should be a fill",FillAction.FILL,step2.getAction());
         Assert.assertEquals("The second action should use Air", Gas.air(),step2.getGas());
         Assert.assertEquals("The second step pressure is incorrect",230.0, step2.getPressure(),0.1);
         
     }
     
     @Test(expected=ImpossibleMixException.class)
     public void testHypoxicMixWithIdealGasLaws() throws ImpossibleMixException {
         GasBlender blender = new GasLawBlender(new IdealGasLaw());
         FillRequirement requirement = new FillRequirement(new Cylinder(12), 230.0, new Gas(20,80));
         blender.blendGas(requirement);
     }
     
     @Test
     public void testTopUpNitroxWithIdealGasLaws() throws ImpossibleMixException {
         GasBlender blender = new GasLawBlender(new IdealGasLaw());
         FillRequirement requirement = new FillRequirement(new Cylinder(12.0,120.0), 230.0, new Gas(30,70));
         FillPlan plan = blender.blendGas(requirement);
         Assert.assertEquals("There should be two steps in the plan",2,plan.size());
         FillStep step1 = plan.get(0);
         Assert.assertEquals("The first step should be a fill",FillAction.FILL,step1.getAction());
         Assert.assertEquals("The First action should use Oxygen", new Gas(Element.OXYGEN),step1.getGas());
         Assert.assertEquals("The First step pressure is incorrect", 146.2, step1.getPressure(),0.01);
         FillStep step2 = plan.get(1);
         Assert.assertEquals("The second step should be a fill",FillAction.FILL,step2.getAction());
         Assert.assertEquals("The second action should use Air", Gas.air(),step2.getGas());
         Assert.assertEquals("The second step pressure is incorrect",230.0, step2.getPressure(),0.01);
         
     }
     
     @Test
     public void testDrainFirstNitroxWithIdealGasLaws() throws ImpossibleMixException {
         GasBlender blender = new GasLawBlender(new IdealGasLaw());
         FillRequirement requirement = new FillRequirement(new Cylinder(12.0,200.0,new Gas(40,60),UnitConverter.celciusToKelvin(20)), 230.0, new Gas(30,70));
         FillPlan plan = blender.blendGas(requirement);
         Assert.assertEquals("There should be two steps in the plan",2,plan.size()); 
         FillStep step1 = plan.get(0);
         Assert.assertEquals("The first step should be a Drain",FillAction.DRAIN,step1.getAction());
         Assert.assertEquals("The First step pressure is incorrect", 108.9, step1.getPressure(),0.01);
            
         FillStep step2 = plan.get(1);
         Assert.assertEquals("The third step should be a fill",FillAction.FILL,step2.getAction());
         Assert.assertEquals("The third action should use Air", Gas.air(),step2.getGas());
         Assert.assertEquals("The third step pressure is incorrect",230.0, step2.getPressure(),0.01);
 
     }
 
 }
