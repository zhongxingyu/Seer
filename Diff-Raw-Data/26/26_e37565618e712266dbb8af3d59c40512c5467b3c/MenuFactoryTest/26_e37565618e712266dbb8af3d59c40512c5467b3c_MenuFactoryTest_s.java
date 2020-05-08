 package com.visionarysoftwaresolutions.menu;
 
 import static org.junit.Assert.*;
 
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.visionarysoftwaresolutions.menu.MenuFactory;
 import com.visionarysoftwaresolutions.menu.composite.MenuComponent;
 import com.visionarysoftwaresolutions.menu.composite.MenuLeaf;
 
 public class MenuFactoryTest {
 	MenuFactory toTest;
 	MenuComponent initial;
 	
 	@Before
 	public void setup(){
 		toTest = new MenuFactory();
 		initial = toTest.createMenu("MyMenu");
 	}
 	@Test
 	public void testMenuCreation() {
 		MenuComponent result = toTest.createMenu("My menu");
 		assertEquals("My menu", result.getText());
 	}
 	
 	@Test(expected=UnsupportedOperationException.class)
 	public void testCannotCreateMenuWithNull(){
 		toTest.createMenu(null);
 	}
 	
 	@Test
 	public void testCreateMenuAddItem(){
 		MenuComponent result = toTest.addItemToMenu(initial, "Booze");
 		assertEquals(result,initial);
 		assertTrue(result.hasChildren());
 		assertNotNull(result.getChild("Booze"));
 	}
 	
 	@Test(expected=UnsupportedOperationException.class)
 	public void testCannotAddItemToNullMenu(){
 		toTest.addItemToMenu(null, "Booze");
 	}
 	
 	@Test(expected=UnsupportedOperationException.class)
 	public void testCannotAddNullItemToMenu(){
 		toTest.addItemToMenu(initial, null);
 	}
 	
 	@Test
 	public void testCreateMenuAddSubMenu(){
 		MenuComponent newMenu = toTest.createMenu("Wines");
 		newMenu = toTest.addSubMenu(initial, newMenu);
 		assertTrue(initial.hasChildren());
 	}
 	
 	@Test(expected=UnsupportedOperationException.class)
 	public void testCannotAddNullSubMenu(){
 		toTest.addSubMenu(initial, null);
 	}
 	
 	@Test(expected=UnsupportedOperationException.class)
 	public void testCannotAddItemsToMenuItems(){
 		initial = toTest.addItemToMenu(new MenuLeaf("Boogie"), "Booze");
 	}
 	
 	@Test
 	public void testGetSubMenus(){
 		MenuComponent sandwiches = toTest.createMenu("Sandwiches and More");
 		toTest.addSubMenu(initial, sandwiches);
 		toTest.addItemToMenu(initial, "Tomato Bisque");
 		assertTrue(initial.hasChildren());
		Set<MenuComponent> subMenus =toTest.getSubMenus(initial);
 		assertEquals(1, subMenus.size());
 	}
 	
 	@Test(expected=UnsupportedOperationException.class)
 	public void testCannotGetSubMenusWhenNullPassedIn(){
 		toTest.getSubMenus(null);
 	}
 	
 	@Test
 	public void testAddMoreItemsToSubMenu(){
 		MenuComponent sandwiches = toTest.createMenu("Sandwiches and More");
 		assertFalse(sandwiches.hasChildren());
 		toTest.addSubMenu(initial, sandwiches);
 		toTest.addItemToMenu(sandwiches, "Tomato Bisque");
 		assertTrue(initial.hasChildren());
 		assertTrue(sandwiches.hasChildren());
 	}
 }
