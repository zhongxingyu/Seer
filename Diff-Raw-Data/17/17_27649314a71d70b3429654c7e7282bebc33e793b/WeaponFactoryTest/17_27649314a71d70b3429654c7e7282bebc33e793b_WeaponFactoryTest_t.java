 package ultraextreme.model.item;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 import ultraextreme.model.util.ObjectName;
 
 public class WeaponFactoryTest extends TestCase {
 	BulletManager manager;
 	@Override
 	public void setUp() {
 		manager = new BulletManager();
 	}
 	
 	@Test
 	public void testInitalize() {
		boolean exceptionHasBeenThrown = false;
		try {
		WeaponFactory.getInstance();
		} catch (IllegalStateException e) {
			exceptionHasBeenThrown = true;
		}
		
		assertTrue(exceptionHasBeenThrown);
 		WeaponFactory.initialize(manager);
 		assertFalse(WeaponFactory.getInstance() == null);
 	}
 
 	@Test
 	public void testGetNewWeapon() {
		WeaponFactory.initialize(manager);
		AbstractWeapon weapon = WeaponFactory.getInstance().getNewWeapon(ObjectName.BASIC_WEAPON);
		assertTrue(weapon != null);
		assertEquals(weapon.getName(), ObjectName.BASIC_WEAPON);
		assertTrue(weapon.getBulletManager().equals(manager));
 	}
 
 }
