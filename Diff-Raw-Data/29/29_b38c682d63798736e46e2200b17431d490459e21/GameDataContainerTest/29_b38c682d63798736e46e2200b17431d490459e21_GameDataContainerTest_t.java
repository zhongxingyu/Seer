 package uk.co.richardgoater.stats.tests.view;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 
 import uk.co.richardgoater.stats.persistence.DefenseGameData;
 import uk.co.richardgoater.stats.persistence.GameDataContainer;
 import uk.co.richardgoater.stats.persistence.Player;
 import uk.co.richardgoater.stats.persistence.dao.gamedata.GameDataDAO;
 import uk.co.richardgoater.stats.tests.fake.FakeStatsDAO;
 
 public class GameDataContainerTest {
 
 	GameDataDAO dao;
 	GameDataContainer container;
 	DefenseGameData dgd;
 	Player p;
 	Map<Integer, Player> players;
 
 	@Before
 	public void setUp() {
 		dao = new FakeStatsDAO();
 		
 		container = new GameDataContainer(DefenseGameData.class);
 		
 		dgd = new DefenseGameData();
 		dgd.setGamedataid(1);
 		dgd.setWeeknum(1);
 		dgd.setPlayerid(1);
 		dgd.setTckl(5);
 		dgd.setInts(2);
 		
 		p = new Player("Richard Goater", 27, "K", false);
 		p.setPlayerid(1);
 		p.setDefense(true);
 		
 		players = new HashMap<Integer, Player>();
 		players.put(p.getPlayerid(), p);
 	}
 
 	@Test
 	public void testAddGameDataBeans() {
 		container.addGameDataBeans(dao.getGameDataForWeek(null), players);
 		Object p = container.getItem(dgd).getItemProperty("player").getValue();
 		assertNotNull(p);
 		assertEquals(p.getClass(), Player.class);
 		assertEquals(((Player) p).getNumber(), 27);
 		assertEquals(((Player) p).getPosition(), "K");
 	}
 	
 	@Test
 	public void testAddZeroRows() {
		populateZeroRows();
 		
 		container.addGameDataBeans(dao.getGameDataForWeek(null), players);
 		
		assertEquals(players.size()+1, container.size());
 		
 		DefenseGameData dgd2 = new DefenseGameData();
		dgd2.setPlayer(players.get(2));
 		
 		Object item = container.getItem(dgd2);
 		assertNotNull(item);
 		
 		Object p = ((Item)item).getItemProperty("player").getValue();
 		assertEquals(p.getClass(), Player.class);
 		assertEquals(((Player) p).getNumber(), 47);
 		assertEquals(((Player) p).getPosition(), "S");
 		
 		Property s1 = ((Item)item).getItemProperty("tckl");
 		assertEquals(0, s1.getValue());
 		Property s2 = ((Item)item).getItemProperty("ints");
 		assertEquals(0, s2.getValue());
 	}
 
	private void populateZeroRows() {
		
		for (int i = 2; i < 6; i++) {
			Player p = new Player("Mark Foster", 47, "S", false);
			p.setPlayerid(i);
			p.setDefense(true);
			players.put(p.getPlayerid(), p);
		}

	}

 }
