 package we.should.test;
 
 import java.util.*;
 
 import junit.framework.TestCase;
 
 import we.should.WeShouldActivity;
 import we.should.database.WSdb;
 import we.should.list.*;
 import android.location.Address;
 import android.test.ActivityInstrumentationTestCase2;
 import android.util.Log;
 
 public class ListTest extends ActivityInstrumentationTestCase2<WeShouldActivity>  {
 	Category C;
 	Item it;
 	List<Field> fieldSet;
 	WSdb db;
 
 	public ListTest() {
 		super("we.should.WeShouldActivity", WeShouldActivity.class);
 		
 	}
 	
 	@Override
 	protected void setUp(){
 		List<Field> fields = Field.getDefaultFields();
 		//Log.v("Set Up List Test", fields.toString());
 		C = new GenericCategory("Test", fields, null);
 		db = new WSdb(getActivity());
 		db.open();
 		db.rebuildTables();
 		db.close();
 		C.save();
 		it = C.newItem();
 	}
 	public void testItems(){
 		assertTrue(C.getItems().size() == 0);
 		it.save();
 		assertTrue(C.getItems().size() == 1);
 		it.save();
 		assertTrue(C.getItems().size() == 1);
 		it.delete();
 		assertTrue(C.getItems().size() == 0);
 	}
 	public void testItemGetSetWBadArgument(){
 		Field badField = new Field("Bad Field", FieldType.TextField);
 		try {
 			it.set(badField, "This shouldn't be set");
 			fail("Should Throw illegal argument exception");
 		}catch(IllegalArgumentException success) {}
 		try {
 			it.get(badField);
 			fail("Should Throw illegal argument exception");
 		} catch(IllegalArgumentException success) {}
 		
 	}
 	public void testItemSetGetWGoodArgument(){
 		String testAd = "test address";
 		String testAd1 = "another Test Address";
 		it.set(Field.ADDRESS, testAd);
 		assertTrue(it.get(Field.ADDRESS).equals(testAd));
 		assertTrue(it.get(Field.ADDRESS).equals(testAd));
 		it.set(Field.ADDRESS, testAd1);
 		assertTrue(it.get(Field.ADDRESS).equals(testAd1));
 		assertTrue(it.get(Field.ADDRESS).equals(testAd1));
 		assertFalse(it.get(Field.ADDRESS).equals(testAd));
 	}
 	public void testGetItems(){
 		List<Item> items = C.getItems();
 		List<Item> testItems = new LinkedList<Item>();
 		assertEquals(items.size(), 0);
 		assertEquals(items, testItems);
 		it.set(Field.NAME, "Test1");
 		it.save();
 		testItems.add(it);
 		items = C.getItems();
 		assertEquals(1, items.size());
 		assertEquals(items, testItems);
 		Item it1 = C.newItem();
 		it1.set(Field.NAME, "Test2");
 		it1.save();
 		testItems.add(it1);
 		items = C.getItems();
 		assertEquals(2, items.size());
 		assertEquals(items, testItems);
 		Item it2 = C.newItem();
 		it2.set(Field.NAME, "Test3");
 		it2.save();
 		testItems.add(it2);
 		items = C.getItems();
 		assertEquals(3, items.size());
 		assertEquals(items, testItems);
 		
 	}
 	public void testGetFields(){
 		List<Field> fields = C.getFields();
 		List<Field> testFields = Field.getDefaultFields();
 		assertEquals(testFields, fields);
 		List<Field> addField = new LinkedList<Field>();
 		addField.add(new Field("New Field", FieldType.Rating));
 		testFields.add(new Field("New Field", FieldType.Rating));
 		C = new GenericCategory("test W New Field", addField, null);
		assertEquals(testFields, C.getFields());
 	}
 	public void testGetComment(){
 		Category Ctest = new GenericCategory("Default", Field.getDefaultFields(), getActivity());
 		Item testItem = Ctest.newItem();
 		testItem.set(Field.COMMENT, "test Comment");
 		assertEquals("test Comment", testItem.getComment());
 	}
 	public void testGetPhoneNo(){
 		it.set(Field.PHONENUMBER, "testNumber");
 		assertEquals("testNumber", it.getPhoneNo());
 	}
 	public void testGetName(){
 		it.set(Field.NAME, "testName");
 		assertEquals("testName", it.getName());
 	}
 	public void testEquals(){
 		it.set(Field.NAME, "testName");
 		Item it2 = C.newItem();
 		it2.set(Field.NAME, "testName");
 		assertTrue(it2.equals(it));
 		assertTrue(it.equals(it));
 	}
 	public void testGetAddresses(){
 		C = new GenericCategory("With Context", Field.getDefaultFields(), getActivity());
 		it = C.newItem();
 		it.set(Field.ADDRESS, "4012 NE 58th St, 98105");
 		Set<Address> add = it.getAddresses();
 		for(Address a : add){
 			try{
 				assertEquals(47.671645, a.getLatitude(), .001);
 				assertEquals(-122.284233, a.getLongitude(), .001);
 			}catch(IllegalStateException e){
 				assertEquals(a.getAddressLine(0), "4012 NE 58th St, 98105");
 			}
 		}
 	}
 	public void testTags(){
 		Set<Tag> tags = it.getTags();
 		assertEquals(0, tags.size());
 		it.addTag("AWESOME");
 		tags = it.getTags();
 		assertEquals(1, tags.size());
 		assertEquals("AWESOME", tags.iterator().next().toString());
 		it.addTag("AWESOME");
 		tags = it.getTags();
 		assertEquals(1, tags.size());
 		it.addTag("Cool  realy     cool");
 		it.addTag("testTag");
 		tags = it.getTags();
 		assertEquals(3, tags.size());
 	}
 	
 }
