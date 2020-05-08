 package test;
 
 import static org.junit.Assert.*;
 
import model.Player;
 import model.UnitFactory;
 import model.UnitFactoryDragon;
 import model.UnitFactoryPegasus;
 
 import org.junit.Test;
 
 public class UnitTest {
 
     @Test
     public void testPowerActivate() {
 	UnitFactory u;
	Player p = new Player();
 	u = new UnitFactoryPegasus();
 	for (String str : u.getNamesOfUnits()) {
	    p.addUnit(str);
	    assertFalse(p.getUnit(str).isPowActivate());
	    p.getUnit(str).activatePower();
	    assertTrue(p.getUnit(str).isPowActivate());
 	}
	p = new Player("Dragon"); 
 	u = new UnitFactoryDragon();
 	for (String str : u.getNamesOfUnits()) {
	    p.addUnit(str);
	    assertFalse(p.getUnit(str).isPowActivate());
	    p.getUnit(str).activatePower();
	    assertTrue(p.getUnit(str).isPowActivate());
 	}
     }
 
 }
