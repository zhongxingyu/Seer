 package projectrts.model.entities;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import projectrts.model.GameModel;
 import projectrts.model.world.Position;
 
 public class EntityManagerTest {
 
	// TODO Complete EntityManagerTest!!!
 	@Test
 	public void testSelect() {
 		// This test requires the player to instantiate a unit in the constructor
 		new GameModel();
 		Player player = new Player(null);
 		EntityManager.INSTANCE.resetData();
 		EntityManager.INSTANCE.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(10, 10));
 		Position onUnit = new Position(10, 10);
 		Position closeToUnit = new Position(9.5f, 9.5f);
 		Position farFromUnit = new Position(5, 5);
 		EntityManager.INSTANCE.update(1);
 
 		assertTrue(EntityManager.INSTANCE.getSelectedEntities().size() == 0);
 
 		EntityManager.INSTANCE.select(onUnit, player);
 		assertTrue(EntityManager.INSTANCE.getSelectedEntities().size() != 0);
 
 		EntityManager.INSTANCE.select(closeToUnit, player);
 		assertTrue(EntityManager.INSTANCE.getSelectedEntities().size() != 0);
 
 		EntityManager.INSTANCE.select(farFromUnit, player);
 		assertTrue(EntityManager.INSTANCE.getSelectedEntities().size() == 0);
 
 	}
 
 	@Test
 	public void testGetEntitiesOfPlayer() {
 		new GameModel();
 		Player player = new Player(null);
 		EntityManager.INSTANCE.resetData();
 		EntityManager.INSTANCE.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(10, 10));
 		EntityManager.INSTANCE.update(1);
 		List<IPlayerControlledEntity> entities = EntityManager.INSTANCE
 				.getEntitiesOfPlayer(player);
 
 		assertTrue(entities.size() == 1);
 		for (IPlayerControlledEntity e : entities) {
 			assertTrue(e.getOwner().equals(player));
 		}
 
 		EntityManager.INSTANCE.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(11, 10));
 		EntityManager.INSTANCE.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(12, 10));
 		EntityManager.INSTANCE.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(13, 10));
 		EntityManager.INSTANCE.addNewPCE(Worker.class.getSimpleName(),
 				new Player(null), new Position(10, 13));
 		EntityManager.INSTANCE.update(1);
 		entities = EntityManager.INSTANCE.getEntitiesOfPlayer(player);
 
 		assertTrue(entities.size() == 4);
 		for (IPlayerControlledEntity e : entities) {
 			assertTrue(e.getOwner().equals(player));
 		}
 	}
 
 	@Test
 	public void testGetAllEntities() {
 		new GameModel();
 		Player player = new Player(null);
 		EntityManager.INSTANCE.resetData();
 		EntityManager.INSTANCE.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(10, 10));
 		EntityManager.INSTANCE.update(1);
 		List<IEntity> entities = EntityManager.INSTANCE.getAllEntities();
 
 		assertTrue(entities.size() == 1);
 
 		EntityManager.INSTANCE.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(11, 10));
 		EntityManager.INSTANCE.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(12, 10));
 		EntityManager.INSTANCE.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(13, 10));
 		EntityManager.INSTANCE.addNewPCE(Worker.class.getSimpleName(),
 				new Player(null), new Position(10, 13));
 		EntityManager.INSTANCE.update(1);
 		entities = EntityManager.INSTANCE.getAllEntities();
 
 		assertTrue(entities.size() == 5);
 	}
 
 	@Test
 	public void testRequestNewEntityID() {
 		List<Integer> numbers = new ArrayList<Integer>();
 		int testAmount = 1000;
 
 		for (int i = 0; i < testAmount; i++) {
 			int nr = EntityManager.INSTANCE.requestNewEntityID();
 			assertTrue(!numbers.contains(nr));
 			numbers.add(nr);
 		}
 	}
 
 	@Test
 	public void testGetNearbyEntities() {
 		new GameModel();
 		Player player = new Player(null);
 		EntityManager.INSTANCE.resetData();
 		EntityManager.INSTANCE.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(10, 10));
 		EntityManager.INSTANCE.update(1);
 		List<AbstractEntity> entities = EntityManager.INSTANCE
 				.getNearbyEntities(new Position(15, 10), 2);
 		assertTrue(entities.isEmpty());
 
 		entities = EntityManager.INSTANCE.getNearbyEntities(
 				new Position(15, 10), 6);
 		assertTrue(entities.size() == 1);
 
 		EntityManager.INSTANCE.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(11, 10));
 		EntityManager.INSTANCE.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(12, 10));
 		EntityManager.INSTANCE.addNewPCE(Worker.class.getSimpleName(), player,
 				new Position(13, 10));
 		EntityManager.INSTANCE.addNewPCE(Worker.class.getSimpleName(),
 				new Player(null), new Position(14, 10));
 		EntityManager.INSTANCE.update(1);
 		entities = EntityManager.INSTANCE.getNearbyEntities(
 				new Position(15, 10), 3);
 		assertTrue(entities.size() == 3);
 	}
 }
