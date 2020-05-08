 package ultraextreme.model.entity;
 
 import org.junit.Test;
 
 import ultraextreme.model.util.Constants;
 import ultraextreme.model.util.ObjectName;
 import ultraextreme.model.util.Rotation;
 
 /**
  * 
  * @author Daniel Jonsson
  * 
  */
 public class EnemyShipTest extends AbstractEntityTest {
 
 	private EnemyShip enemyShip;
 
 	@Override
 	public void setUp() {
 		resetShip(0, 0, 100, 50, 0, 10);
 	}
 
 	private void resetShip(double x, double y, int width, int height,
 			double rotation, int hitpoints) {
 		enemyShip = new EnemyShip(x, y, width, height, new Rotation(rotation),
 				hitpoints, ObjectName.BASIC_ENEMYSHIP);
 	}
 
 	@Override
 	public void testGetSpeedMod() {
		assertEquals(enemyShip.getSpeedMod(), Constants.getInstance()
				.getEnemySpeedModifier());
 	}
 
 	@Test
 	public void testReceiveDamage() {
 		// Create an enemy ship and kill it
 		resetShip(0, 0, 100, 100, 0, 100);
 		enemyShip.receiveDamage(100);
 		assertTrue(enemyShip.isDestroyed());
 
 		// Create a new enemy ship and kill it more slowly
 		resetShip(0, 0, 100, 100, 0, 1000);
 		enemyShip.receiveDamage(100);
 		assertFalse(enemyShip.isDestroyed());
 		enemyShip.receiveDamage(899);
 		assertFalse(enemyShip.isDestroyed());
 		enemyShip.receiveDamage(1);
 		assertTrue(enemyShip.isDestroyed());
 
 		// Kill a third enemy even more slowly
 		resetShip(0, 0, 100, 100, 0, 1000);
 		for (int i = 999; i > 0; --i) {
 			enemyShip.receiveDamage(1);
 			assertFalse(enemyShip.isDestroyed());
 		}
 		enemyShip.receiveDamage(1);
 		assertTrue(enemyShip.isDestroyed());
 	}
 
 	@Override
 	protected AbstractEntity getNewAbstractEntity(double x, double y,
 			int width, int height, Rotation direction) {
 		resetShip(x, y, width, height, 0, 5);
 		return enemyShip;
 	}
 
 }
