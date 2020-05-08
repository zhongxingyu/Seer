 package com.scurab.web.drifmaps.database;
 
 import java.io.File;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.scurab.web.drifmaps.database.Database.Structure;
 import com.scurab.web.drifmaps.shared.DataComparator;
 import com.scurab.web.drifmaps.shared.datamodel.Detail;
 import com.scurab.web.drifmaps.shared.datamodel.MapItem;
 import com.scurab.web.drifmaps.shared.datamodel.Star;
 import com.scurab.web.drifmaps.shared.utils.AppUtils;
 import com.scurab.web.drifmaps.shared.utils.RandomGenerator;
 
 public class DatabaseTest extends TestCase
 {
 	private Database db = null;
 	private String file = null;
 	private Random r = new Random();
 	
 	private class DatabaseT extends Database
 	{
 		public DatabaseT(String file) throws SQLException
 		{
 			super(file);
 		}
 	}
 	@Override
 	@Before
 	public void setUp() throws Exception
 	{
 		file = String.format("c:\\Temporary Files\\DrifMapsTest%s.sqlite",System.currentTimeMillis());
 		db = new DatabaseT(file);
 	}
 	
 	@Override
 	protected void tearDown() throws Exception
 	{
 		db.close();
 		File f = new File(file);
 		f.delete();
 	}
 
 
 	/**
 	 * Test creation and deletion of tables
 	 * @throws SQLException
 	 */
 	@Test
 	public void testCreateAndDropTables() throws SQLException
 	{
 		String countColumnName = "Cnt";/* Database.Structure#Queries.SELECT_COUNT_FROM_SQLITE_MASTER}*/
 		ResultSet rs = db.executeQuery(Database.Structure.Queries.SELECT_TABLES_COUNT_FROM_SQLITE_MASTER);
 		assertTrue(rs.next());		
 		int rowsBefore = rs.getInt(countColumnName);
 		rs.close();
 		assertEquals(rowsBefore,0);
 		db.createTables();
 		rs = db.executeQuery(Database.Structure.Queries.SELECT_TABLES_COUNT_FROM_SQLITE_MASTER);
 		int rowsAfter = rs.getInt(countColumnName);
 		rs.close();
 		assertFalse(rowsBefore == rowsAfter);
 		
 		int expectedDifference = 4 * Database.Structure.Tables.ALL_TABLES.length; //every fulltext table has 1 for own and 3 for support
 		assertEquals(rowsAfter,expectedDifference); //rowsbefore = 0
 		
 		db.dropAll();
 		rs = db.executeQuery(Database.Structure.Queries.SELECT_TABLES_COUNT_FROM_SQLITE_MASTER);
 		int rowsAfterDrop = rs.getInt(countColumnName);
 		rs.close();
 		assertEquals(rowsBefore,rowsAfterDrop);
 	}
 
 	@Test
 	public void testAddUpdateRemoveGetStars() throws SQLException
 	{
 		db.createTables();
 		
 		int size = 20;
 		List<Star> stars = new ArrayList<Star>();
 		HashMap<Long,Star> starsMap = new HashMap<Long, Star>();
 		for(int i = 0;i<size;i++)
 			stars.add(RandomGenerator.genRandomStar());
 
 		//test add and check id
 		for(Star s : stars)
 		{
 			db.addNewStar(s);
 			assertTrue(s.getId() > 0);
 			starsMap.put(s.getId(), s);
 		}
 		
 		List<Star> fromDb = db.getStars();//get from db
 		assertEquals(size,fromDb.size());
 		
 		for(int i = 0;i<size;i++)
 			DataComparator.assertEquals(stars.get(i), fromDb.get(i), true);
 		
 		//test for update
 		for(int i = 0;i<size;i++)
 		{
 			Star s = stars.get(i);
 			if(i % 2 == 0)
 				s.setNote("Updated " + i);
 			else
 				s.setNote(null);
 			s.setType(AppUtils.generateRandomString());
 			db.updateStar(s);
 		}
 		
 		fromDb = db.getStars();
 		assertEquals(size,fromDb.size());
 		for(int i = 0;i<size;i++)
 		{					
 			Star db = fromDb.get(i); 
 			Star local = starsMap.get(db.getId());
 			assertNotNull(local);
 			DataComparator.assertEquals(local, db, true);
 		}
 		
 		//test for delete
 		Star toDelete = stars.get(r.nextInt(size));
 		db.deleteStar(toDelete);
 		fromDb = db.getStars();
 		assertEquals(fromDb.size(),size-1);
 		for(Star s : fromDb)
 		{
 			if(s.getId() == toDelete.getId())
 				fail("Should not be same!");
 		}
 		
 		//test all delete
 		for(Star s : stars)
 			db.deleteStar(s);
 		
 		assertEquals(0,db.getStars().size());
 		
 		db.dropAll();
 	}
 
 	@Test
 	public void testGetFilteredStars() throws SQLException
 	{
 		db.createTables();
 		Star s1 = RandomGenerator.genRandomStar();
 		Star s2 = RandomGenerator.genRandomStar();
 		
 		s1.setX(10f);
 		s1.setY(10f);
 		
 		s2.setX(20f);
 		s2.setY(20f);
 		
 		db.addNewStar(s1);
 		db.addNewStar(s2);
 		
 		List<Star> fromDb = db.getStars(0, 0, 15, 15);
 		assertEquals(fromDb.size(),1);
 		DataComparator.assertEquals(fromDb.get(0), s1, true);
 		
 		fromDb = db.getStars(15, 15, 25, 25);
 		assertEquals(fromDb.size(),1);
 		DataComparator.assertEquals(fromDb.get(0), s2, true);
 		
 		fromDb = db.getStars(0, 0, 25, 25);
 		assertEquals(fromDb.size(),2);
 		DataComparator.assertEquals(fromDb.get(0), s1, true);
 		DataComparator.assertEquals(fromDb.get(1), s2, true);
 		
 		fromDb = db.getStars(-10, -10, 9, 9);
 		assertEquals(fromDb.size(),0);
 		
 		try
 		{
 			db.getStars(20,20,0,0);
 			fail("Should throw exception");
 		}
 		catch(Exception e){}
 		
 		db.dropAll();
 	}
 	
 	@Test
 	public void testFlatGet() throws SQLException
 	{
 		int size = 10;
 		db.createTables();
 		for(int i = 0;i<size;i++)
 		{
 			MapItem mi = RandomGenerator.genMapItem(true);
 			db.addNewMapItem(mi);
 			assertTrue(mi.getId() > 0);
 		}
 		
 		List<MapItem> mis = db.get(false);
 		for(MapItem mi : mis)
 		{
 			assertNull(mi.getPros());
 			assertNull(mi.getCons());
 			assertNull(mi.getDetails());
 		}
 	}
 	
 	@Test
 	public void testGetFilteredFlat() throws SQLException
 	{
 		db.createTables();
 		MapItem mi1 = RandomGenerator.genMapItem(false);
 		mi1.setX(10);
 		mi1.setY(10);
 		db.addNewMapItem(mi1);
 		
 		MapItem mi2 = RandomGenerator.genMapItem(false);
 		mi2.setX(20);
 		mi2.setY(20);
 		db.addNewMapItem(mi2);
 		
 		List<MapItem> data;
 		
 		data = db.get(0, 0, 15, 15, false);
 		assertEquals(1,data.size());
 		DataComparator.assertEquals(mi1, data.get(0), true, false);
 		
 		data = db.get(15,15, 25, 25, false);
 		assertEquals(1,data.size());
 		DataComparator.assertEquals(mi2, data.get(0), true, false);
 		
 		data = db.get(10,10, 20, 20, false);
 		assertEquals(2,data.size());
 		DataComparator.assertEquals(mi1, data.get(0), true, false);
 		DataComparator.assertEquals(mi2, data.get(1), true, false);
 		
 		data = db.get(-10, -10, 0, 0, false);
 		assertEquals(0,data.size());
 		
 		try
 		{
 			db.get(20,20,0,0,false);
 			fail("Should throw exception");
 		}
 		catch(Exception e){}
 		
 		db.dropAll();
 	}
 	
 	@Test
 	public void testAddUpdateDeleteMapItem() throws SQLException
 	{
 		int size = 10;
 		db.createTables();
 		List<MapItem> data = new ArrayList<MapItem>();
 		HashMap<Long,MapItem> dataMap = new HashMap<Long, MapItem>();
 		for(int i = 0;i<size;i++)
 		{
 			MapItem mi = RandomGenerator.genMapItem(true);
 			data.add(mi);
 			db.addNewMapItem(mi);
 			assertTrue(mi.getId() > 0);
 			dataMap.put(mi.getId(), mi);
 			
 			for(Detail d : mi.getDetails())
 				assertTrue(d.getId() > 0);
 		}
 		
 		//check adding
 		List<MapItem> fromDb = db.get(true);
 		assertEquals(size, fromDb.size());
 		for(int i = 0;i<size;i++)
 		{
 			MapItem db = fromDb.get(i);
 			MapItem local = dataMap.get(db.getId());
 			DataComparator.assertEquals(db, local, true);
 		}
 		
 		
 		//update
 		List<MapItem> oldData = data;
 		data = new ArrayList<MapItem>();
 		dataMap.clear();
 		for(int i = 0;i<size;i++)
 		{
 			long id = oldData.get(i).getId();
 			MapItem mi = RandomGenerator.genMapItem(true);
 			mi.setId(id);
 			db.updateMapItem(mi);
 			data.add(mi);
 			dataMap.put(mi.getId(), mi);
 		}
 		
 		//update check
 		fromDb = db.get(true);
 		assertEquals(size, fromDb.size());
 		for(int i = 0;i<size;i++)
 		{
 			MapItem db = fromDb.get(i);
 			MapItem local = dataMap.get(db.getId());
 			DataComparator.assertEquals(db, local, true);
 		}
 		
 		//delete
 		MapItem mi = data.get(r.nextInt(data.size()));
 		db.deleteMapItem(mi);
 		fromDb = db.get(true);
 		assertEquals(size-1,fromDb.size());
 		
 		for(int i = 0;i<fromDb.size();i++)
 		{
 			if(fromDb.get(i).getId() == mi.getId())
 				fail("Should be deleted!");
 		}
 		//delete all
 		for(int i = 0;i<data.size();i++)
 		{
 			db.deleteMapItem(data.get(i));
 		}
 		assertEquals(0,db.get(true).size());
 		
 		//check if everythink is deleted
 		String countColumn = "Cnt";
 		for (String table : Structure.Tables.ALL_TABLES)
 		{
 			
 			String qry = String.format("SELECT COUNT(*) as %s FROM %s",countColumn,table);
 			ResultSet rs = db.executeQuery(qry);
 			assertTrue(rs.next());
 			int rowCount = rs.getInt(countColumn);
 			assertEquals(0, rowCount);
 		}
 		
 		db.dropAll();
 	}
 	
 	@Test
 	public void testAddUpdateDeleteSingleDetail() throws SQLException
 	{
 		db.createTables();
 		
 		List<MapItem> data = new ArrayList<MapItem>();
 		HashMap<Long,MapItem> dataMap = new HashMap<Long, MapItem>();
 		for(int i = 0;i<1;i++)
 		{
 			MapItem mi = RandomGenerator.genMapItem(true);
 			data.add(mi);
 			db.addNewMapItem(mi);
 			assertTrue(mi.getId() > 0);
 			dataMap.put(mi.getId(), mi);
 			
 			for(Detail d : mi.getDetails())
 				assertTrue(d.getId() > 0);
 		}
 		
 		
 		MapItem  randomItem;
 		do//potencially infinite loop!
 		{
 			randomItem = data.get(r.nextInt(data.size()));
 		}
 		while(randomItem.getDetails().size() == 0);
 			
 		Detail randomDetail = randomItem.getDetails().get(r.nextInt(randomItem.getDetails().size()));
 		
 		int startDetailsSize = randomItem.getDetails().size();
 		
 		Detail newDetail = RandomGenerator.genRandomDetail();
 		
 		db.addNewDetail(randomItem.getId(), newDetail);
 		assertTrue(newDetail.getId() > 0);
 		
 		List<MapItem> fromDb = db.get(true);
 		//find and check new inserted detail 
 		boolean checked = false;
 		for(int i = 0;i<fromDb.size();i++)
 		{
 			MapItem item = fromDb.get(i); 
 			if(item.getId() == randomItem.getId())
 			{
 				assertEquals(startDetailsSize+1, item.getDetails().size());
 				for(int j = 0;j<item.getDetails().size();j++)
 				{
 					Detail d = item.getDetails().get(j);
 					if(d.getId() == newDetail.getId())
 					{
 						checked = true;
 						DataComparator.assertEquals(d, newDetail, true);
 					}
 				}
 			}
 		}
 		assertTrue("Check for new added item", checked);
 		
 		//check for update
 		Detail toUpdate = RandomGenerator.genRandomDetail();
 		toUpdate.setId(randomDetail.getId());
 		db.updateDetail(toUpdate);
 		fromDb = db.get(true);
 		//find and check new inserted detail 
 		checked = false;
 		for(int i = 0;i<fromDb.size();i++)
 		{
 			MapItem item = fromDb.get(i); 
 			if(item.getId() == randomItem.getId())
 			{
 				assertEquals(startDetailsSize+1, item.getDetails().size());
 				for(int j = 0;j<item.getDetails().size();j++)
 				{
 					Detail d = item.getDetails().get(j);
 					if(d.getId() == newDetail.getId())
 					{
 						checked = true;
 						DataComparator.assertEquals(d, newDetail, true);
 					}
 				}
 			}
 		}
 		
 		
 		//check for delete
 		db.deleteDetail(newDetail);
 		fromDb = db.get(true);
 		boolean isIncluded = false;
 		for(int i = 0;i<fromDb.size();i++)
 		{
 			MapItem item = fromDb.get(i); 
 			if(item.getId() == randomItem.getId())
 			{
 				assertEquals(startDetailsSize, item.getDetails().size());
 				for(int j = 0;j<item.getDetails().size();j++)
 				{
 					Detail d = item.getDetails().get(j);
 					isIncluded |= (d.getId() == newDetail.getId());
 				}
 			}
 		}
 		
 		assertFalse(isIncluded);
 		
 		db.dropAll();
 	}
 	
 	@Test
 	public void testAddDeleteMapItemTypes() throws SQLException
 	{
 		db.createTables();
 		int size = 10;
 		List<String> types = new ArrayList<String>();
 		for(int i = 0;i<size;i++)
 		{
 			String value = AppUtils.generateRandomString(); 
 			types.add(value);
 			db.addNewMapItemItem(value);
 		}
 		
 		//test adding
 		HashSet<String> localHash = new HashSet<String>(types);
 		List<String> fromDb = db.getMapItemTypes();
 		assertEquals(size, fromDb.size());
 		for(int i = 0;i<fromDb.size();i++)
 			assertTrue(localHash.contains(fromDb.get(i)));
 		
 		
 		//testdelete
 		String randomItem = types.get(r.nextInt(types.size()));
 		db.deleteMapItemType(randomItem);
 		fromDb = db.getMapItemTypes();
 		assertEquals(size-1, fromDb.size());
 		HashSet<String> dbHash = new HashSet<String>(fromDb);
 		assertFalse(dbHash.contains(randomItem));
 
 		//delete all
 		for(int i = 0;i<fromDb.size();i++)
 			db.deleteMapItemType(fromDb.get(i));
 		
 		assertEquals(0,db.getMapItemTypes().size());
 		
 		
 		
 		db.dropAll();
 	}
 	
 	public void testAddMapItemTypesDistinctValues() throws SQLException
 	{
 		db.createTables();
 		//check for distinct values
 		String value = "abcde";
 		db.addNewMapItemItem(value);
 		assertEquals(1,db.getMapItemTypes().size());
 		db.addNewMapItemItem(value);
 		List<String> fromDb = db.getMapItemTypes();
 		assertEquals(1,fromDb.size());
 		assertEquals(value,fromDb.get(0));
 		
 		db.dropAll();
 	}
 	
 	@Test
 	public void testConvertEmptyStringToNull()
 	{
 		String s = "";
 		String expected = null;
 		String value = db.convertEmptyStringToNull(s, false);
 		assertEquals(expected,value);
 		
 		s = "abc";
 		expected = "abc";
 		value = db.convertEmptyStringToNull(s, false);
 		assertEquals(expected,value);
 		
 		s = null;
 		expected = null;
 		value = db.convertEmptyStringToNull(s, false);
 		assertEquals(expected,value);
 		
 		
 		//true
 		s = "";
 		expected = null;
 		value = db.convertEmptyStringToNull(s, true);
 		assertEquals(expected,value);
 		
 		s = "abc";
 		expected = "'abc'";
 		value = db.convertEmptyStringToNull(s, true);
 		assertEquals(expected,value);
 		
 		s = null;
 		expected = null;
 		value = db.convertEmptyStringToNull(s, true);
 		assertEquals(expected,value);
 		
		s = "Baby's";
		expected = "'Baby''s'";
		value = db.convertEmptyStringToNull(s, true);
		assertEquals(expected,value);
		
		s = "Baby's";
		expected = "Baby''s";
		value = db.convertEmptyStringToNull(s, false);
		assertEquals(expected,value);
		
 	}
 }
