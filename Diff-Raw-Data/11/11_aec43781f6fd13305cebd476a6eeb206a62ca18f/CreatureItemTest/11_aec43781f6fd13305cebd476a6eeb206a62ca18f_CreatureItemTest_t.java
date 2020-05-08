 package pl.edu.agh.megamud.tests.dao;
 
 import java.sql.SQLException;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import pl.edu.agh.megamud.dao.CreatureItem;
 
 public class CreatureItemTest extends TestBase{
 	
 	@Before
 	public void setUp() throws Exception {
 		prepareDatabase();
 		resetPlayer();
 		resetPlayerCreature();
 		resetItem();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		connectionSource.close();
 	}	
 	
 	@Test(expected = SQLException.class)
 	public void should_not_create_without_item() throws SQLException{
 		CreatureItem creatureItem = new CreatureItem();
 		creatureItem.setCreature(predefinedPlayerCreature);
 		creatureItemDao.create(creatureItem);
 	}
 	
	@Test
	public void should_create_without_player_creature() throws SQLException{
 		CreatureItem creatureItem = new CreatureItem();
 		creatureItem.setItem(predefinedItem);
		creatureItemDao.create(creatureItem);
		creatureItemDao.refresh(creatureItem);
		
		Assert.assertNotNull(creatureItem.getId());
		Assert.assertTrue(creatureItem.getId() != 0);
 	}
 	
 	@Test
 	public void should_create() throws SQLException{
 		CreatureItem creatureItem = new CreatureItem();
 		creatureItem.setCreature(predefinedPlayerCreature);
 		creatureItem.setItem(predefinedItem);
 		creatureItemDao.create(creatureItem);
 		
 		Assert.assertTrue(creatureItem.getId() != null && creatureItem.getId() != 0);
 	}
 }
