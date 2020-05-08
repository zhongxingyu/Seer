 package ultraextreme.model.entity;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 import ultraextreme.model.util.Constants;
 import ultraextreme.model.util.ObjectName;
 import ultraextreme.model.util.Position;
 
 public class WeaponPickupTest extends TestCase{
 	WeaponPickup pickup;
 	
 	@Override
 	public void setUp() {
 		pickup = new WeaponPickup(0, 0, ObjectName.BASIC_WEAPON);
 	}
 	
 	@Test
 	public void testGetSpeedMod() {
 		assertEquals(pickup.getSpeedMod(),
				Constants.getInstance().getPickupSpeedModifier());
 	}
 
 	@Test
 	public void testWeaponPickupDoubleDoubleObjectName() {
 		assertEquals(pickup.getPosition(), new Position(0,0));
 		assertEquals(pickup.getHeight(), pickup.getWidth());
 		assertEquals(pickup.getObjectName(), ObjectName.BASIC_WEAPON);
 	}
 
 	@Test
 	public void testWeaponPickupPositionObjectName() {
 		pickup = new WeaponPickup(new Position(0, 0), ObjectName.BASIC_WEAPON);
 		testWeaponPickupDoubleDoubleObjectName();
 	}
 }
