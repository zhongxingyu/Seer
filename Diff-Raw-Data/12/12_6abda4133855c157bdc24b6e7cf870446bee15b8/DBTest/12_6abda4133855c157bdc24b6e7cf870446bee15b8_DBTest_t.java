 package com.nyaruka.db;
 
 import java.util.ArrayList;
 
 import com.nyaruka.db.dev.DevDB;
 import com.nyaruka.json.JSON;
 
 import junit.framework.TestCase;
 
 public class DBTest extends TestCase {
 	
 	public void testDBCreation() throws Exception {
 		DB db = new DevDB();
 		db.open();
 		db.init();
 		
 		// no collection yet
 		assertFalse(db.collectionExists("test"));
 		
 		// create it
 		Collection type = db.ensureCollection("test");
 		assertNotNull(type);
 		assertEquals(1, type.getId());
 		
 		// ensure it is there
 		assertTrue(db.collectionExists("test"));
 		
 		// reload from our tables
 		db.load();
 		
 		// ensure it is still there
 		assertTrue(db.collectionExists("test"));
 		
 		// clear the db
 		db.clear();
 		
 		// assert it's all gone
 		assertFalse(db.collectionExists("test"));
 		
 		// try to reload from the tables
 		db.load();
 		
 		// still gone
 		assertFalse(db.collectionExists("test"));
 		
 		// recreate it
 		Collection coll = db.ensureCollection("test");
 		assertTrue(db.collectionExists("test"));
 		
 		// remove it
 		db.deleteCollection(coll);
 		assertFalse(db.collectionExists("test"));
 		
 		// double delete shouldn't fail
 		db.deleteCollection(coll);
 	}
 	
 	public void testInvalidNames() throws Exception {
		DB db = new DevDB();
 		db.open();
 		db.init();
 		
 		try{
 			db.ensureCollection("hello/world");
 			fail("Should have thrown due to invalid name");
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 		
 		try{
 			db.ensureCollection("hello world");
 			fail("Should have thrown due to invalid name");
 		} catch (Exception e){}		
 	}
 	
 	public void testSavingTypes() throws Exception {
 		DB db = new DevDB();
 		db.open();
 		db.init();
 		
 		Collection collection = db.ensureCollection("test");
 		collection.ensureStrIndex("strIndex");
 		assertEquals(0, collection.getStrIndex("strIndex"));
 		assertEquals(-1, collection.getStrIndex("notThere"));
 		
 		collection.ensureIntIndex("intIndex");
 		assertEquals(0, collection.getIntIndex("intIndex"));
 		assertEquals(-1, collection.getIntIndex("notThere"));
 		
 		db.saveCollection(collection);
 		
 		// reload
 		db.load();
 		
 		collection = db.ensureCollection("test");
 	
 		assertEquals(0, collection.getStrIndex("strIndex"));
 		assertEquals(0, collection.getIntIndex("intIndex"));
 		
 		// add another str index
 		collection.ensureStrIndex("str2Index");
 		assertEquals(1, collection.getStrIndex("str2Index"));
 		
 		// and remove our original
 		collection.deleteIndex("strIndex");
 		assertEquals(-1, collection.getStrIndex("strIndex"));
 		
 		// save and reload
 		db.saveCollection(collection);
 		
 		db.load();
 		
 		assertEquals(1, collection.getStrIndex("str2Index"));
 		assertEquals(-1, collection.getStrIndex("strIndex"));
 		
 		// new index should fill gap now
 		collection.ensureStrIndex("str3Index");
 		assertEquals(0, collection.getStrIndex("str3Index"));
 	}
 	
 	public void testAddingRecords() throws Exception {
 		DB db = new DevDB();
 		db.open();
 		db.init();
 		
 		Collection col = db.ensureCollection("test");
 		
 		JSON simple = new JSON();
 		simple.put("string", "mystring");
 		simple.put("int", 10);
 		simple.put("long", 10000000000l);
 		simple.put("double", 1.234d);
 		simple.put("boolean", false);
 		
 		// add the record to our collection
 		Record rec = col.save(simple);
 		
 		assertNotNull(rec);
 		assertEquals(1, rec.getId());
 		
 		// fetch it from the db
 		rec = col.getRecord(rec.getId());
 		
 		// make sure what we got it is the same as we put in
 		JSON data = rec.getData();
 		assertEquals("mystring", data.getString("string"));
 		assertEquals(10, data.getInt("int"));
 		assertEquals(10000000000l, data.getLong("long"));
 		assertEquals(1.234d, data.getDouble("double"));
 		assertEquals(false, data.getBoolean("boolean"));
 		
 		// not there
 		assertNull(col.getRecord(100));
 	}
 	
 	public void testGenerateSQL() throws Exception {
 		ArrayList<String> params = new ArrayList<String>();
 		
 		String sql = DB.buildClause("age", new JSON("{ $gt: 10 }"), params);
 		assertEquals("age > ?", sql);
 		assertEquals(1, params.size());
 		assertEquals("10", params.get(0));
 		
 		params.clear();
 		
 		sql = DB.buildClause("age", new JSON("{ $gt: 10, $lt: 30 }"), params);
 		assertEquals("age > ? AND age < ?", sql);
 		assertEquals(2, params.size());
 		assertEquals("10", params.get(0));		
 		assertEquals("30", params.get(1));				
 	}
 	
 	public void testFiltering() throws Exception {
 		DB db = new DevDB();
 		db.open();
 		db.init();
 		
 		Collection col = db.ensureCollection("contacts");
 		col.ensureIntIndex("age");
 		
 		Record eric = col.save("{ name: 'Eric Newcomer', age: 32 }");
 		Record nic = col.save("{ name: 'Nic Pottier', age: 34 }");		
 		Record stevo = col.save("{ name: 'Steve Jobs', age: 55 }");
 
 		// create another collection to make sure we are filtering by collection
 		Collection col2 = db.ensureCollection("messages");
 		col2.save("{ text: 'can anybody hear me', number: '0788383382' }");
 		col2.save("{ text: 'is anybody out there', number: '0788383383' }");		
 		
 		// query for everybody over 50
 		Cursor cursor = col.find("{ age: 55 }");
 		
 		assertEquals(1, cursor.count());
 		assertTrue(cursor.hasNext());
 		assertTrue(cursor.hasNext());		
 		Record match = cursor.next(); 
 		assertEquals(stevo.getId(), match.getId());
 		assertFalse(cursor.hasNext());
 		assertFalse(cursor.hasNext());
 
 		// update a record, stevo is actually 58
 		long oldId = stevo.getId();
 		stevo.setData("{name: 'Steve Jobs', age: 58 }");
 		col.save(stevo.toString());
 		
 		// query for him
 		cursor = col.find("{ age: 58 }");
 		match = cursor.next();
 		assertEquals(oldId, match.getId());
 		
 		// test that indexes are actually being persisted in the DB
 		col.ensureStrIndex("name");
 		db.load();
 		col = db.ensureCollection("contacts");
 		
 		cursor = col.find("{name: 'Eric Newcomer'}");
 		
 		assertEquals(1, cursor.count());
 		assertTrue(cursor.hasNext());
 		match = cursor.next();
 		assertEquals(eric.getId(), match.getId());
 		assertFalse(cursor.hasNext());
 		
 		cursor = col.find("{age: { $gt: 40 } }");
 		assertEquals(1, cursor.count());
 		assertEquals(stevo.getId(), cursor.next().getId());
 		
 		cursor = col.find("{age: { $gt: 30, $lt: 50 } }");
 		assertEquals(2, cursor.count());
 		assertEquals(eric.getId(), cursor.next().getId());				
 		assertEquals(nic.getId(), cursor.next().getId());
 		assertFalse(cursor.hasNext());
 		
 		cursor = col.find("{age: 100 }");
 		assertEquals(0, cursor.count());
 		assertFalse(cursor.hasNext());
 		
 		// remove a record
 		col.delete(stevo.getId());
 		
 		cursor = col.find("{}");
 		assertEquals(2, cursor.count());
 		assertEquals(eric.getId(), cursor.next().getId());				
 		assertEquals(nic.getId(), cursor.next().getId());		
 	}
 }
