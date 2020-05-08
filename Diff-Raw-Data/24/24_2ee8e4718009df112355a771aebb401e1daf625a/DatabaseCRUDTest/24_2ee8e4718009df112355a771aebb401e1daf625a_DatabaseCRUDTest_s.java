 package com.nickotter.randomname.test;
 
 import java.util.List;
 
 import com.nickotter.randomname.CRUD;
 import com.nickotter.randomname.Group;
 import com.nickotter.randomname.MyList;
 import com.nickotter.randomname.Sqlite;
 import android.content.Context;
 
 import android.test.AndroidTestCase;
 import android.util.Log;
 
 public class DatabaseCRUDTest extends AndroidTestCase {
 	
 	private String LOGTAG = "DatabaseCRUDTest";
 	
 	Context context = null;
 	CRUD databaseCRUD = null;
 	
 	public DatabaseCRUDTest(String name) {
 		this.setName(name);
 	}
 
 	public void setUp() {
 		Log.v(LOGTAG, "setUp e");
 		
 		Log.v(LOGTAG, "Deleting DATABASE_NAME="+ Sqlite.DATABASE_NAME);
 		getContext().deleteDatabase(Sqlite.DATABASE_NAME);
 
 		Log.v(LOGTAG, "Initializing CRUD object");
 		
 		this.databaseCRUD = new CRUD(getContext());
 		this.databaseCRUD.open();
 		
 		Log.v(LOGTAG, "setUp x");
 	}
 	
 	public void tearDown() {
 		this.databaseCRUD.close();
 	}
 	
 	//GROUP TESTS
 	
 	/**
 	 * Tests that databaseCRUd can insert a Group
 	 */
 	public void testAddGroup() {
 		
 		Group temp = new Group("CS480");
 		this.databaseCRUD.add_group(temp);
 		
 		assertEquals(1, temp.getID());
 	}
 	
 	/**
 	 * Tests that databaseCRUD can find Groups
 	 */
 	public void testQueryGroup() {
 		List<Group> groupList = this.databaseCRUD.query_group();
 		
 		String[] expectedName = {"CS480"};
 		int[] expectedID = {1};
 		
 		for (int i = 0; i < groupList.size(); i++) {
 			assertEquals(expectedID[i], groupList.get(i).getID()); //tests that it has the expected id
 			assertEquals(expectedName[i], groupList.get(i).getName()); //tests that it has the expected name
 		}
 	}
 	
 	//List Test
 	
 	public void testAddList() {
 		
 		Group tempGroup = new Group("CS480");
 		
 		this.databaseCRUD.add_group(tempGroup);
 		
 		MyList temp = new MyList(0, tempGroup.getID(), "Test List Name");
 		
 		databaseCRUD.add_list(tempGroup, temp); //add the list to the database
 		
 		assertEquals(1, temp.getID());
 	}
 	
 	public void testQueryList() {
 		
 		Group tempGroup = new Group("CS428");
 		
 		this.databaseCRUD.add_group(tempGroup);
 		
		assertEquals(1, tempGroup.getID());
 		
		MyList temp = new MyList(0, tempGroup.getID(), "Test List Name");
 		
 		databaseCRUD.add_list(tempGroup, temp); //add the list to the database
 		
 		List<MyList> tempList = databaseCRUD.query_list(tempGroup);
 		
		String[] expectedName = {"Test List Name"};
		int[] expectedID = {1};
		int[] expectedGroupID = {1};
		
 		for (int i = 0; i < tempList.size(); i++) {
 			assertEquals(expectedID[i], tempList.get(i).getID());
 			assertEquals(expectedName[i], tempList.get(i).getName());
			assertEquals(expectedGroupID[i], tempList.get(i).getGroupID());
 		}
 		
 	}
 
 }
