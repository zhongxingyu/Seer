 package com.warfactory.dpscalc.model;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class DualfDpsTest {
 
 	private DualDps dps;
 
 	@Before
 	public void setUp() throws Exception {
 		dps = new DualDps();
 	}
 
 	@Test
 	public void testDps() {
 		double wp1Dps = 400;
 		double wp2Dps = 500;
 		int primaryAttrib = 500;
 		double iasPercent = 20;
 		double critChance = 0.4;
 		double critDam = 0.5;
 
 		dps.setWeaponDps(wp1Dps);
 		dps.setWeapon2Dps(wp2Dps);
 		dps.setPrimaryAttribute(primaryAttrib);
 		dps.setIasPercent(iasPercent);
 		dps.setCritChance(critChance);
 		dps.setCritDamage(critDam);
 
 		double expectedWeaponDps = (wp1Dps + wp2Dps) * 0.5;
		double expectedDps = expectedWeaponDps * (1 + primaryAttrib / 100.0) * (1 + iasPercent / 100.0 + 0.15)
 				* (1 + (critChance * critDam));
 
 		assertEquals(expectedDps, dps.getDps(), 1);
 	}
 }
