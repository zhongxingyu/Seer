 package sw.app_250grice.pojoTest;
 
 import static org.junit.Assert.*;
 
 
 import sw.app_250grice.Item;
 import sw.app_250grice.ItemNotFoundException;
 import sw.app_250grice.Page;
 import sw.app_250grice.Units;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class PageTest {
 	
 	Page uut;
 
 	public PageTest() {
 	}
 	
 	@Before
 	public void setUp() throws Exception {
 		uut = new Page("Page1");
 		uut.addItem(new Item("i1", 4.5));
 		uut.addItem(new Item("i2", 3.333, Units.LITRE));
 		uut.addItem(new Item("i3", 0.52, Units.GRAMM));
 		uut.addItem(new Item("i4", 1, Units.PINCH));
 		uut.addItem(new Item("i5", 25, Units.PIECE));
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		uut = null;
 	}
 	
 	@Test
 	public void testAddSameItemNameAndUnit() throws ItemNotFoundException{
 		Item toCompare = new Item("i1", 6.5);
 		Item toAdd = new Item("i1", 2);
 
 		uut.addItem(toAdd);
 		
 		Item toCheck = uut.getItemByNameAndUnit("i1", Units.NONE);
 		
 		assertEquals(toCompare, toCheck);
 	}
 	
 	@Test
 	public void testAddSameItemNameOtherUnit() throws ItemNotFoundException{
 		Item toCompare = new Item("i1", 2, Units.LITRE);
 		Item toAdd = new Item("i1", 2, Units.LITRE);
 
 		uut.addItem(toAdd);
 		
 		Item toCheck = uut.getItemByNameAndUnit("i1", Units.LITRE);
 		
 		assertEquals(toCompare, toCheck);
 		
         toCheck = uut.getItemByNameAndUnit("i1", Units.NONE);
         toCompare = new Item("i1", 4.5, Units.NONE);
         
 		assertEquals(toCompare, toCheck);
 	}
 	
 	@Test
 	public void testRemoveItem() throws ItemNotFoundException{
 		Item toDelete = new Item("i6", 3.4, Units.GRAMM);
 		
 		uut.addItem(toDelete);
 		
 		uut.removeItemByNameAndUnit(toDelete.getName(), toDelete.getUnit());
 	}
 	
 	@Test(expected=ItemNotFoundException.class)
 	public void testGetItemByNameAndUnitThrows() throws ItemNotFoundException{
 		
 	uut.getItemByNameAndUnit("i6", Units.NONE);
 	}
 	
 	@Test(expected=ItemNotFoundException.class)
 	public void testtestRemoveItemThrows() throws ItemNotFoundException{
 		
 	uut.removeItemByNameAndUnit("i6", Units.NONE);
 	}
 	
 	
 	@Test
 	public void testAddItemSetNullAfterwards() throws ItemNotFoundException{
 		Item toAdd  = new Item("i6", 3);
 		String toSearchName = toAdd.getName();
 		Units toSearchUnit = toAdd.getUnit();
 		Item toCheck;
 		
 		uut.addItem(toAdd);
 		
 		toAdd = null;
 		
 		toCheck = uut.getItemByNameAndUnit(toSearchName, toSearchUnit);
 		
 		assertEquals(toCheck.getName(), toSearchName);
 		assertEquals(toCheck.getUnit(), toSearchUnit);
 	}	
 
 	@Test
 	public void testEqualityFail() {
 		Page uut2 = new Page("Page1");
 		
 		boolean areEqual = uut.equals(uut2);
 		
 		assertFalse(areEqual);
 	}
 	
 	@Test
 	public void testEquality() {
 		Page uut2 = uut.clone();
 		
 		assertEquals(uut, uut2);
 	}
 	
 	@Test
 	public void testToString() {
 		String toCompare = "Name:Page1 , Count:5";
 		String toCheck = uut.toString();
 		
 		assertEquals(toCompare, toCheck);
 		
 	}
 	
 	@Test
 	public void testClone() throws ItemNotFoundException{
 		Page deepCopy = uut.clone();
 		
 		Item toCheck = deepCopy.getItemByNameAndUnit("i1", Units.NONE);
 		
 		toCheck.addValue(2);
 		
 		Item toCompare = uut.getItemByNameAndUnit("i1", Units.NONE);
 		
 		boolean areEqual = toCompare.equals(toCheck);
 		
 		assertFalse(areEqual);
 		
 		areEqual = deepCopy.equals(uut);
 		
 		assertFalse(areEqual);
 	}
	
	@Test
	public void testAddAndModifyItemToTwoLists() throws ItemNotFoundException{
		Page uut2 = uut.clone();
		
		Item toAdd = new Item("Rice", 20);
		
		Item toCheckuut = new Item("Rice", 60);
		Item toCheckuut2 = new Item("Rice", 40);
		
		uut.addItem(toAdd);
		uut.addItem(toAdd);
		uut2.addItem(toAdd);
		uut.addItem(toAdd);
		uut2.addItem(toAdd);
		
        String name = "Rice";
        Units unit = Units.NONE;
		
		assertEquals(toCheckuut, uut.getItemByNameAndUnit(name, unit));
		assertEquals(toCheckuut2, uut2.getItemByNameAndUnit(name, unit));
		
	}
	
 }
