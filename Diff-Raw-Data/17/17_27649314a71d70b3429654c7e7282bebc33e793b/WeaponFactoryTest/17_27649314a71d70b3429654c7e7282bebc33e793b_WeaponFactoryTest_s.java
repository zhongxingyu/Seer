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
		assertTrue(WeaponFactory.getInstance() == null);
 		WeaponFactory.initialize(manager);
 		assertFalse(WeaponFactory.getInstance() == null);
 	}
 
 	@Test
 	public void testGetNewWeapon() {
		//TODO Finish this test
		WeaponFactory.getInstance().getNewWeapon(ObjectName.BASIC_WEAPON);
 	}
 
 }
