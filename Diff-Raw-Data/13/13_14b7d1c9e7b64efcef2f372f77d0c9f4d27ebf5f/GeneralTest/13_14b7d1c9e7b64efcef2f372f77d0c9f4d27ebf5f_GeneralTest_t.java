 package gt.general;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.mock;
 import gt.BaseTest;
 import junit.framework.Assert;
 
import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
 
@RunWith(PowerMockRunner.class)
@PrepareForTest(Bukkit.class)
 public class GeneralTest  extends BaseTest {
 
 	/**
 	 * Tests Inventory Mechanics of hero
 	 */
 	@Test
 	public void simpleInventoryTest() {
 		//Initialization
 		PortableItem item1 = new PortableItem(null, null, null);
 		item1.setDropable(false);
 
 		PortableItem item2 = new PortableItem(null, null, null);
 		item2.setTool(true);
 		item2.setDropable(true);
 
 		Hero hero = new Hero(mock(Player.class));
 
 		hero.setActiveItem(item2);
 		Assert.assertEquals(item2, hero.getActiveItem());
 		Assert.assertEquals(true, hero.activeItemDropable());
 
 		hero.setActiveItem(item1);
 		Assert.assertEquals(item1, hero.getActiveItem());
 
 		Assert.assertEquals(item2, hero.getPassivItem());
 		Assert.assertEquals(false, hero.activeItemDropable());
 
 	}
 
 
 	//Test of remaining Hero Mechanics
 	@Test
 	public void newHeroTest() {
 		// Create hero and see if he has an inventory
 
 		Player mockPlayer = mock(Player.class);
 
 		Hero hero = new Hero(mockPlayer);
 		assertEquals(1, hero.getDefaultSpeed(), 0);
 
 		//TODO Aura Test
 	}
 
 }
