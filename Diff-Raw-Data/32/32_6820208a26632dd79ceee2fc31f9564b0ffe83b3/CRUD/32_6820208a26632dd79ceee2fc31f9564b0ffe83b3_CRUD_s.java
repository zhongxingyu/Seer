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
 	
 	private static final String[] SHAKE_COLUMN = {
 		Sqlite.SHAKE_SHIFT
 	};
 	
 	private static final String[] VERBAL_COLUMN = {
 		Sqlite.VERBAL_SHIFT
 	};
 	
 	private static final String[] EXCLUSION_COLUMN = {
 		Sqlite.EXCLUSION_SHIFT
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
 		
 		int lastId = (int) database.insert(Sqlite.DATABASE_ITEM, null, values);
 		
 		item.setId(lastId);
 	}
 	
 	public void add_group(Group group){
 		Log.v(LOGTAG, "add_group function start");
 		
 		Log.v(LOGTAG, "\tInitializing values");
 		ContentValues values = new ContentValues();
 		
 		Log.v(LOGTAG, "\tAdding provided group name: " + group.getName() + " to values");
 		values.put(Sqlite.GROUP_NAME, group.getName());
 		
 		Log.v(LOGTAG, "\tinserting values into database table=" + Sqlite.DATABASE_GROUP);
 		int lastId = (int) database.insert(Sqlite.DATABASE_GROUP, null, values);
 		group.setId(lastId);
 		
 		Log.v(LOGTAG, "\tlast insert id=" + lastId);
 		
 		Log.v(LOGTAG, "add_group function end");
 	}
 	
 	public void add_list(Group group, MyList list){
 		Log.v(LOGTAG, "add_list function start");
 		ContentValues values = new ContentValues();
 		Log.v(LOGTAG, "\tList name added: " + list.getName() + "\n\tIncluded ID: " + list.getID());
 		values.put(Sqlite.LIST_NAME, list.getName());
 		
 		values.put(Sqlite.LIST_GROUP_ID, group.getID());
 		
 		Log.v(LOGTAG, "inserting with group_id=" + group.getID());
 		int lastId = (int) database.insert(Sqlite.DATABASE_LIST, null, values);
 		
 		list.setId(lastId);
 		
 		Log.v(LOGTAG, "add_list function end");
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
 		
 		Log.v(LOGTAG, "query_group function start");
 		Cursor cursor = database.query(Sqlite.DATABASE_GROUP, GROUP_COLUMNS, null, null, null, null, null);
 		
 		if(cursor.getCount() > 0){
 			while(cursor.moveToNext()){
 				Log.v(LOGTAG, "Group name and id respectively: " + cursor.getString(cursor.getColumnIndex(Sqlite.GROUP_NAME)) + " && " + cursor.getInt(cursor.getColumnIndex(Sqlite.GROUP_ID)));
 				Group g = new Group(cursor.getString(cursor.getColumnIndex(Sqlite.GROUP_NAME)));
 				g.setId(cursor.getInt(cursor.getColumnIndex(Sqlite.GROUP_ID)));
 				
 				groups.add(g);
 			}
 		}
 		
 		Log.v(LOGTAG, "query_group function end");
 		return groups;
 	}
 	
 	public Group get_group(String name){
 		
		Cursor cursor = database.query(Sqlite.DATABASE_GROUP, GROUP_COLUMNS, "groupName = " + name, null, null, null, null);
 		cursor.moveToNext();
 		
 		Group g = new Group(cursor.getString(cursor.getColumnIndex(Sqlite.GROUP_NAME)));
 		
 		return g;
 	}
 	
 	public MyList get_list(String name){
 		
		Cursor cursor = database.query(Sqlite.DATABASE_LIST, LIST_COLUMNS, "listName = " + name, null, null, null, null);
 		cursor.moveToNext();
 		
		MyList l = new MyList(0, cursor.getInt(cursor.getColumnIndex(Sqlite.LIST_GROUP_ID)), cursor.getString(cursor.getColumnIndex(Sqlite.LIST_NAME)));
 		
 		return l;
 	}
 	
 	public void update_item(Item item){
 		ContentValues values = new ContentValues();
 		values.put(Sqlite.ITEM_NAME, item.getName());
 		
 		database.update(Sqlite.DATABASE_ITEM, values, Sqlite.ITEM_ID + "= ?", new String[]{
 				String.valueOf(item.getID())
 		});
 	}
 	
 	public void update_group(Group group){
 		ContentValues values = new ContentValues();
 		values.put(Sqlite.GROUP_NAME, group.getName());
 		
 		database.update(Sqlite.DATABASE_GROUP, values, Sqlite.GROUP_ID + "= ?", new String[]{
 				String.valueOf(group.getID())
 		});
 	}
 	
 	public void update_list(MyList list){
 		ContentValues values = new ContentValues();
 		values.put(Sqlite.LIST_NAME, list.getName());
 		
 		database.update(Sqlite.DATABASE_LIST, values, Sqlite.LIST_ID + "= ?", new String[]{
 				String.valueOf(list.getID())
 		});
 	}
 	
 	public void delete_item(Item item){
 		database.delete(Sqlite.DATABASE_ITEM, Sqlite.ITEM_ID + "= ? ", new String[]{
 				String.valueOf(item.getID())
 		});
 	}
 	
 	public void delete_list(MyList list){
 		database.delete(Sqlite.DATABASE_LIST, Sqlite.LIST_ID + "= ? ", new String[]{
 				String.valueOf(list.getID())
 		});
 	}
 	
 	public void delete_group(Group group){
 		database.delete(Sqlite.DATABASE_GROUP, Sqlite.GROUP_ID + "= ? ", new String[]{
 				String.valueOf(group.getID())
 		});
 	}
 	
 	
 	public int query_Shake(){
 		Cursor cursor = database.query(Sqlite.DATABASE_SHIFT, SHAKE_COLUMN, null, null, null, null, null);
 		
 		cursor.moveToNext();
 		return cursor.getInt(cursor.getColumnIndex(Sqlite.SHAKE_SHIFT));
 		
 	}
 	
 	public int query_Verbal(){
 		Cursor cursor = database.query(Sqlite.DATABASE_SHIFT, VERBAL_COLUMN, null, null, null, null, null);
 		
 		cursor.moveToNext();
 		return cursor.getInt(cursor.getColumnIndex(Sqlite.VERBAL_SHIFT));
 		
 	}
 	
 	public int query_Exclusion(){
 		Cursor cursor = database.query(Sqlite.DATABASE_SHIFT, EXCLUSION_COLUMN, null, null, null, null, null);
 		
 		
 		cursor.moveToNext();
 		return cursor.getInt(cursor.getColumnIndex(Sqlite.EXCLUSION_SHIFT));
 	}
 	
 	public boolean toggle_Shake(){
 		ContentValues values = new ContentValues();
 		int x = query_Shake();
 		
 		if (x == 0){
 			values.put(Sqlite.SHAKE_SHIFT, 1);
 			database.update(Sqlite.DATABASE_SHIFT, values, Sqlite.SHIFT_ID + "= ? ", new String[]{
 					String.valueOf(1)
 			});
 			return false;
 		}else
 		{
 			values.put(Sqlite.SHAKE_SHIFT, 0);
 			database.update(Sqlite.DATABASE_SHIFT, values, Sqlite.SHIFT_ID + "= ? ", new String[]{
 					String.valueOf(1)
 			});
 			return true;
 		}
 		
 	}
 	
 	public boolean toggle_Verbal(){
 		ContentValues values = new ContentValues();
 		int x = query_Verbal();
 		
 		if (x == 0){
 			values.put(Sqlite.VERBAL_SHIFT, 1);
 			database.update(Sqlite.DATABASE_SHIFT, values, Sqlite.SHIFT_ID + "= ? ", new String[]{
 					String.valueOf(1)
 			});
 			return true;
 		}else
 		{
 			values.put(Sqlite.VERBAL_SHIFT, 0);
 			database.update(Sqlite.DATABASE_SHIFT, values, Sqlite.SHIFT_ID + "= ? ", new String[]{
 					String.valueOf(1)
 			});
 			return false;
 		}
 			
 	}
 	
 	public boolean toggle_Exculison(){
 		ContentValues values = new ContentValues();
 		int x = query_Exclusion();
 		
 		if (x == 0){
 			values.put(Sqlite.EXCLUSION_SHIFT, 1);
 			database.update(Sqlite.DATABASE_SHIFT, values, Sqlite.SHIFT_ID + "= ? ", new String[]{
 					String.valueOf(1)
 			});
 			return false;
 		}else
 		{
 			values.put(Sqlite.EXCLUSION_SHIFT, 0);
 			database.update(Sqlite.DATABASE_SHIFT, values, Sqlite.SHIFT_ID + "= ? ", new String[]{
 					String.valueOf(1)
 			});
 			return true;
 		}
 		
 	}
 	
 	
 	public void initExtraFunctions(){
 		ContentValues values = new ContentValues();
 		values.put(Sqlite.SHIFT_ID, 1);
 		values.put(Sqlite.EXCLUSION_SHIFT, 0);
 		values.put(Sqlite.SHAKE_SHIFT, 0);
 		values.put(Sqlite.VERBAL_SHIFT, 0);
 		
 		database.insert(Sqlite.DATABASE_SHIFT, null, values);
 		
 	}
 
 }
