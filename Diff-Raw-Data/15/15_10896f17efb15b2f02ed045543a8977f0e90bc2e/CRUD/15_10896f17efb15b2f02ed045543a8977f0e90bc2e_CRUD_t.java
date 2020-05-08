 package com.nickotter.randomname;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class CRUD {
 	
 	private String LOGTAG = "CRUD";
 	
 	SQLiteOpenHelper dbhelper;
 	SQLiteDatabase database;
 	
 	private static final String[] GROUP_COLUMNS = {
 		Sqlite.GROUP_ID,
 		Sqlite.GROUP_NAME
 	};
 	
 	private static final String[] ITEM_COLUMNS = {
 		Sqlite.ITEM_ID,
 		Sqlite.ITEM_LIST_ID,
 		Sqlite.ITEM_NAME
 	};
 	
 	private static final String[] LIST_COLUMNS = {
 		Sqlite.LIST_GROUP_ID,
 		Sqlite.LIST_ID,
 		Sqlite.LIST_NAME
 	};
 
 	public CRUD(Context context){
 		dbhelper = new Sqlite(context);
 	}
 	
 	public void open(){
 		database = dbhelper.getWritableDatabase();
 	}
 	
 	public void close(){
 		dbhelper.close();
 	}
 	
 	public void add_item(MyList list, Item item){
 		ContentValues values = new ContentValues();
 		values.put(Sqlite.ITEM_NAME, item.getName());
 		values.put(Sqlite.ITEM_LIST_ID, list.getID());
 		database.insert(Sqlite.DATABASE_ITEM, null, values);
 	}
 	
 	public void add_group(Group group){
 		Log.v(LOGTAG, "add_group e");
 		
 		Log.v(LOGTAG, "\tinitializing values");
 		ContentValues values = new ContentValues();
 		
		Log.v(LOGTAG, "\tadding provided group name: " + group.getName() + " to values");
 		values.put(Sqlite.GROUP_NAME, group.getName());
 		
 		Log.v(LOGTAG, "\tinserting values into database table=" + Sqlite.DATABASE_GROUP);
 		int lastId = (int) database.insert(Sqlite.DATABASE_GROUP, null, values);
 		group.setId(lastId);
 		
 		Log.v(LOGTAG, "\tlast insert id=" + lastId);
 		
 		Log.v(LOGTAG, "add_group x");
 	}
 	
 	public void add_list(Group group, MyList list){
 		Log.v(LOGTAG, "add_list e");
 		ContentValues values = new ContentValues();
 		values.put(Sqlite.LIST_NAME, list.getName());
 		values.put(Sqlite.LIST_GROUP_ID, group.getID());
 		
 		Log.v(LOGTAG, "inserting with group_id=" + group.getID());
 		database.insert(Sqlite.DATABASE_LIST, null, values);
 		
 		Log.v(LOGTAG, "add_list x");
 	}
 	
 	public List<Item> query_item(MyList list){
 		Log.v(LOGTAG, "query_item e");
 		
 		Log.v(LOGTAG, "initializing List<item>");
 		List<Item> items = new ArrayList<Item>();
 		
 		Log.v(LOGTAG, "creating cursor from database.query()");
 		Cursor cursor = database.query(Sqlite.DATABASE_ITEM, ITEM_COLUMNS, "listID = " + list.getID(), null, null, null, null);
 		
 		Log.v(LOGTAG, "checking that the cursor is not empty");
 		if(cursor.getCount() > 0 ){
 			
 			Log.v(LOGTAG, "iterating through cursor");
 			while(cursor.moveToNext()){
 				
 				Log.v(LOGTAG, "initializing item to be added to list");
 				Item item = new Item(
 					cursor.getColumnIndex(Sqlite.ITEM_ID),
 					cursor.getColumnIndex(Sqlite.ITEM_LIST_ID),
 					cursor.getString(cursor.getColumnIndex(Sqlite.ITEM_NAME))
 						);
 				
 				Log.v(LOGTAG, "Adding item to list");
 				items.add(item);
 				
 			}
 		} else {
 			Log.v(LOGTAG, "\t query_item cursor is empty");
 		}
 		
 		Log.v(LOGTAG, "query_item x");
 		return items;
 		
 	}
 	
 	public List<MyList> query_list(Group group){
 		Log.v(LOGTAG, "query_list e");
 		
 		List<MyList> lists = new ArrayList<MyList>();
 		
 		Cursor cursor = database.query(Sqlite.DATABASE_LIST, LIST_COLUMNS, "groupID = " + group.getID(), null, null, null, null);
 		
 		if(cursor.getCount() > 0 ){
 			Log.v(LOGTAG, "\t The cursor retrieved items");
 			while(cursor.moveToNext()){
 				
 				MyList list = new MyList(
 						cursor.getColumnIndex(Sqlite.LIST_ID),
 						cursor.getColumnIndex(Sqlite.LIST_GROUP_ID),
 						cursor.getString(cursor.getColumnIndex(Sqlite.LIST_NAME))
 						);
 				Log.v(LOGTAG, "\tLIST_NAME=" + list.getName());
 				
 				lists.add(list);
 				
 			}
 		} else {
 			Log.v(LOGTAG, "\t The cursor did not retrieve items");
 		}
 		
 		Log.v(LOGTAG, "query_list x");
 		
 		return lists;
 		
 	}
 	
 	public List<Group> query_group(){
 		List<Group> groups = new ArrayList<Group>();
 		
 		Cursor cursor = database.query(Sqlite.DATABASE_GROUP, GROUP_COLUMNS, null, null, null, null, null);
 		
 		if(cursor.getCount() > 0){
 			while(cursor.moveToNext()){
 				Group g = new Group(cursor.getString(cursor.getColumnIndex(Sqlite.GROUP_ID)));
 				g.setId(cursor.getColumnIndex(Sqlite.GROUP_ID));
 				
 				groups.add(g);
 			}
 		}
 		return groups;
 	}
 	
 	public void update_item(){
 		
 	}
 	
 	public void update_group(){
 		
 	}
 	
 	public void update_list(){
 		
 	}
 	
 	public void delete_item(){
 		
 	}
 	
 	public void delete_list(){
 		
 	}
 	
 	public void delete_group(){
 		
 	}
 
 }
