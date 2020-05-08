 package we.should.test;
 
 import java.io.IOException;
 import java.util.*;
 
 import org.json.JSONException;
 
 import we.should.WeShouldActivity;
 import we.should.database.WSdb;
 import we.should.list.*;
 import android.database.Cursor;
 import android.location.Address;
 import android.location.Geocoder;
 import android.test.ActivityInstrumentationTestCase2;
 import android.util.Log;
 /**
  * This tests the interaction between the Database and List packages.
  * 
  * TODO:IMPLEMENT only has trivial tests currently
  * @author Davis
  *
  */
 public class ListDBIntegrationTests extends
 		ActivityInstrumentationTestCase2<WeShouldActivity> {
 	WSdb db;
 	Category c;
 
 	public ListDBIntegrationTests() {
 		super("we.should.WeShouldActivity", WeShouldActivity.class);
 		
 		// TODO Auto-generated constructor stub
 	}
 	@Override
 	protected void setUp(){
 		c = new GenericCategory("test", Field.getDefaultFields(), getActivity());
 	}
 	
 	public void testGetCategories(){
 		db = new WSdb(getActivity());
 		db.open();
 		db.rebuildTables();
 		db.close();
 		List<Field> dF = Field.getDefaultFields();
 		for(int i= 0; i < 100; i++){
 			c = new GenericCategory(i+"", dF, getActivity());
 			c.save();
 		}
 		Set<Category> cats = Category.getCategories(getActivity());
 		assertEquals(100, cats.size());
 		for(int i = 0; i < 100; i++){
 			assertTrue(cats.contains(new GenericCategory(i+"", dF, getActivity())));
 		}
 		assertTrue(true);
 	}
 	public void testGetItems(){
 		c = new GenericCategory("master", Field.getDefaultFields(), getActivity());
 		c.save();
 		Item i = c.newItem();
 		i.set(Field.ADDRESS, "4012 NE 58th");
 		i.set(Field.NAME, "BIKE HOUSE");
 		i.set(Field.PHONENUMBER, "555-5555");
 		i.save();
 		i = c.newItem();
 		i.set(Field.ADDRESS, "4014 NE 58th");
 		i.set(Field.NAME, "NOT BIKE HOUSE");
 		i.set(Field.PHONENUMBER, "1-800-555-5555");
 		i.addTag("TestTag1");
 		i.addTag("TestTag2");
 		i.save();
 		c = null;
 		Set<Category> cats = Category.getCategories(getActivity());
 		assertEquals(101, cats.size());
 		List<Item> its = null;
 		for(Category cat : cats){
 			if(cat.getName().equals("master")){
 				its = cat.getItems();
 				assertEquals(its, cat.getItems());
 				break;
 			}
 		}
 		assertEquals(2, its.size());
 		Item it = its.get(0);
 		assertEquals("BIKE HOUSE", it.get(Field.NAME));
 		assertEquals("4012 NE 58th", it.get(Field.ADDRESS));
 		assertEquals("555-5555", it.get(Field.PHONENUMBER));
 		it = its.get(1);
 		assertEquals("NOT BIKE HOUSE", it.get(Field.NAME));
 		assertEquals("4014 NE 58th", it.get(Field.ADDRESS));
 		assertEquals("1-800-555-5555", it.get(Field.PHONENUMBER));
 		assertEquals(2, it.getTags().size());
 	}
 	public void testGetItemsDuplicates(){
 		c = new GenericCategory("master1", Field.getMovieFields(), getActivity());
 		c.save();
 		Item i = c.newItem();
 		i.set(Field.NAME, "New BIKE HOUSE");
 		i.save();
 		i.set(Field.COMMENT, "This is new.");
 		i.save();
 		c = null;
 		Set<Category> cats = Category.getCategories(getActivity());
 		assertEquals(102, cats.size());
 		List<Item> its = null;
 		for(Category cat : cats){
 			if(cat.getName().equals("master1")){
 				its = cat.getItems();
 				assertEquals(its, cat.getItems());
 				break;
 			}
 		}
 		assertEquals(1, its.size());
 		Item it = its.get(0);
		assertEquals("New BIKE HOUSE", it.get(Field.NAME));
 	}
 }
