 package ultraextreme.model.enemy;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 import ultraextreme.model.enemyspawning.EnemySpawner;
 import ultraextreme.model.entity.EnemyShip;
 import ultraextreme.model.item.BulletManager;
 
 /**
  * 
  * @author Daniel Jonsson
  * 
  */
 public class EnemyManagerTest extends TestCase {
 
 	private EnemyManager enemyManager;
 
 	@Override
 	public void setUp() {
 		enemyManager = new EnemyManager();
 	}
 
 	/**
 	 * Add a single enemy to the manager.
 	 */
 	@Test
 	public void testAddEnemy() {
 		EnemyCollector collector = new EnemyCollector();
 		AbstractEnemy enemy = new BasicEnemy(0, 0, new BulletManager());
 		enemyManager.addPropertyChangeListener(collector);
 		enemyManager.addEnemy(enemy);
 		assertEquals(enemy, enemyManager.getEnemies().get(0));
 		assertEquals(enemy.getShip(),
				collector.getEnemyShips().get(EnemyManager.NEW_ENEMY));
 	}
 
 	/**
 	 * Test to add a lot of enemies to the manager.
 	 */
 	@Test
 	public void testAddEnemy2() {
 		EnemyCollector collector = new EnemyCollector();
 		List<IEnemy> addedEnemies = new ArrayList<IEnemy>();
 		List<EnemyShip> addedShips = new ArrayList<EnemyShip>();
 		BulletManager bulletManager = new BulletManager();
 		enemyManager.addPropertyChangeListener(collector);
 		// Add a lot of enemies to the enemy manager and to a local list.
 		for (int i = 0; i < 10000; i++) {
 			AbstractEnemy enemy = new BasicEnemy(0, 0, bulletManager);
 			enemyManager.addEnemy(enemy);
 			addedEnemies.add(enemy);
 			addedShips.add(enemy.getShip());
 		}
 		// Check so the enemy manager fired events for all added ships.
 		assertTrue(addedShips.containsAll(collector.getEnemyShips().values()));
 		// Check so the enemy manager contains the enemies.
 		assertTrue(addedEnemies.containsAll(enemyManager.getEnemies()));
 	}
 
 	@Test
 	public void testClearDeadEnemies() {
 		EnemyCollector collector = new EnemyCollector();
 		AbstractEnemy enemy = new BasicEnemy(0, 0, new BulletManager());
 		enemyManager.addPropertyChangeListener(collector);
 		enemyManager.addEnemy(enemy);
 
 		// kill the ship
 		enemy.getShip().receiveDamage(10000);
 
 		enemyManager.clearDeadEnemies();
 
 		assertEquals(collector.getEnemyShips().size(), 2);
 		assertEquals(enemy,
				collector.getEnemyShips().get(EnemyManager.REMOVED_ENEMY));
 		collector.getEnemyShips().clear();
 
 		// Now check if an enemy gets removed when it is outside the map
 		enemy = new BasicEnemy(0, 0, new BulletManager());
 		enemyManager.addEnemy(enemy);
 
 		enemy.getShip().move(-500, -500);
 
 		enemyManager.clearDeadEnemies();
 
 		assertEquals(collector.getEnemyShips().size(), 2);
 		assertEquals(enemy,
				collector.getEnemyShips().get(EnemyManager.REMOVED_ENEMY));
 	}
 
 	/**
 	 * Test to add an enemy through the manager's propertyChange method.
 	 */
 	@Test
 	public void testPropertyChange() {
 		IEnemy enemy = new BasicEnemy(0, 0, new BulletManager());
 		PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 		pcs.addPropertyChangeListener(enemyManager);
 		pcs.firePropertyChange(EnemySpawner.NEW_ENEMY, null, enemy);
 		assertEquals(enemy, enemyManager.getEnemies().get(0));
 	}
 
 	/**
 	 * Add this as a listener to the enemy manager and collects its enemies.
 	 * 
 	 * @author Daniel Jonsson
 	 * 
 	 */
 	public class EnemyCollector implements PropertyChangeListener {
 
 		private Map<String, EnemyShip> map;
 
 		public EnemyCollector() {
 			map = new HashMap<String, EnemyShip>();
 		}
 
 		@Override
 		public void propertyChange(PropertyChangeEvent event) {
 			map.put(event.getPropertyName(), (EnemyShip) event.getNewValue());
 		}
 
 		public Map<String, EnemyShip> getEnemyShips() {
 			return map;
 		}
 
 	}
 
 }
