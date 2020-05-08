 package com.warfactory.dpscalc.model;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class DpsTest {
 
 	private Dps dps;
 
 	@Before
 	public void setUp() throws Exception {
 		dps = new Dps();
 	}
 
 	@Test
 	public void testDps() {
 		double wpDps = 300;
 		int primaryAttrib = 500;
 		double iasPercent = 20;
 		double critChance = 0.4;
 		double critDam = 0.5;
 
 		dps.setWeaponDps(wpDps);
 		dps.setPrimaryAttribute(primaryAttrib);
 		dps.setIasPercent(iasPercent);
 		dps.setCritChance(critChance);
 		dps.setCritDamage(critDam);
 
		double expectedDps = wpDps * (1 + primaryAttrib / 100.0) * (1 + iasPercent / 100.0)
 				* (1 + (critChance * critDam));
 
 		assertEquals(expectedDps, dps.getDps(), 1);
 	}
 }
