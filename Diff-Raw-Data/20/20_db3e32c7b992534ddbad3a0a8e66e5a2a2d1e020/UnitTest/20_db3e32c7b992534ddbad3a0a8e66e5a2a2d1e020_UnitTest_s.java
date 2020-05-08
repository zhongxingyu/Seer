 package test;
 
 import static org.junit.Assert.*;
 
 import model.UnitFactory;
 import model.UnitFactoryDragon;
 import model.UnitFactoryPegasus;
 
 import org.junit.Test;
 
 public class UnitTest {
 
     @Test
     public void testPowerActivate() {
 	UnitFactory u;
 	u = new UnitFactoryPegasus();
 	for (String str : u.getNamesOfUnits()) {
	    u.getUnit(str).activatePower();
	    assertTrue(u.getUnit(str).isPowActivate());
 	}
	    
 	u = new UnitFactoryDragon();
 	for (String str : u.getNamesOfUnits()) {
	    u.getUnit(str).activatePower();
	    assertTrue(u.getUnit(str).isPowActivate());
 	}
     }
 
 }
