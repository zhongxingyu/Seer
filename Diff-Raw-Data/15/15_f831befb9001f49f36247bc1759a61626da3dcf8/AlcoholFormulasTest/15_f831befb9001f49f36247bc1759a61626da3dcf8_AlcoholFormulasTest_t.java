 package org.blh.core.formulas.alcohol;
 
 import org.blh.core.units.alcohol.ABV;
 import org.blh.core.units.gravity.SpecificGravity;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Created by Erik Larkö at 6/28/13 1:26 PM
  */
 public class AlcoholFormulasTest {
 
 	private SpecificGravity og = new SpecificGravity(1.040);
 	private SpecificGravity fg = new SpecificGravity(1.010);
 
 	@Test
 	public void simpleBrewersFriendTest() {
 		BrewersFriendSimple f = new BrewersFriendSimple();
 		ABV actual = f.calc(og, fg);
		ABV expected = new ABV(3.9375);

		Assert.assertEquals(expected.value(), actual.value());
 	}
 
 	@Test
 	public void BYOSimpleTest() {
 		BYOSimple f = new BYOSimple();
 		ABV actual = f.calc(og, fg);
		ABV expected = new ABV(3.87);
		Assert.assertEquals(expected.value(), actual.value());
 	}
 
 	@Test
 	public void danielsTest() {
 		Daniels f = new Daniels();
 		ABV actual = f.calc(og, fg);
		ABV expected = new ABV(3.95);
		Assert.assertEquals(expected.value(), actual.value());
 	}
 }
