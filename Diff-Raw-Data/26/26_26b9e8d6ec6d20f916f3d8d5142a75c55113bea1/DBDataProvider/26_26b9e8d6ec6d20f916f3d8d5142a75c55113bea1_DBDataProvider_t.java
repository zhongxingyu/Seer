 package great.team;
 
 import great.team.entity.Catalog;
 import great.team.entity.Item;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DBDataProvider extends SQLiteOpenHelper implements IDataProvider{
 
 	// Database Version
     private static final int DATABASE_VERSION = 1;
  
     // Database Name
     private static final String DATABASE_NAME = "smart_tagging_db";
     
     // Datatables
     private static final String TABLE_CATALOGS = "catalogs";
     private static final String TABLE_ITEMS = "items";
     private static final String TABLE_TERMS = "terms";
     
     // Catalogs fields
     private static final String FIELD_CATALOG_ID = "id";
     private static final String FIELD_CATALOG_PARENT_ID = "parent_id";
     private static final String FIELD_CATALOG_NAME = "name";
     private static final String FIELD_CATALOG_WEIGHT = "weight";
     
     // Item fields
     private static final String FIELD_ITEM_ID = "id";
     private static final String FIELD_ITEM_PATH = "path";
     private static final String FIELD_ITEM_CONTEXT = "context";
     
     // Term fields
     private static final String FIELD_TERM_ID = "id";
     private static final String FIELD_TERM_NAME = "name";
     private static final String FIELD_TERM_WEIGHT = "weight";
 
     public DBDataProvider(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public List<Item> find(String substring) {
 		return null;
 	}
 
 	@Override
 	public List<String> autocomplete(String substring) {
 		return null;
 	}
 
 	@Override
 	public List<Catalog> getChildCatalogs(Long id) {
 		return null;
 	}
 
 	@Override
 	public Long findTermByName(String term) {
 		return null;
 	}
 
 	@Override
 	public void addTerm(String name, String term, Long catalog_id) {
 	}
 
 	
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		String CREATE_CATALOG_TABLE = "CREATE TABLE " + TABLE_CATALOGS + 
 				"("
 				+ FIELD_CATALOG_ID + " INTEGER PRIMARY KEY,"
 				+ FIELD_CATALOG_PARENT_ID + " INTEGER," 
 				+ FIELD_CATALOG_NAME + " TEXT, " 
 				+ FIELD_CATALOG_WEIGHT + " INTEGER" 
 				+ ")";
 		String CREATE_TERM_TABLE = "CREATE TABLE " + TABLE_TERMS + 
 				"("
 				+ FIELD_TERM_ID + " INTEGER PRIMARY KEY," 
 				+ FIELD_TERM_NAME + " TEXT, " 
 				+ FIELD_TERM_WEIGHT + " INTEGER" 
 				+ ")";
 		String CREATE_ITEM_TABLE = "CREATE TABLE " + TABLE_ITEMS + 
 				"("
 				+ FIELD_ITEM_ID + " INTEGER PRIMARY KEY," 
 				+ FIELD_ITEM_PATH + " TEXT, " 
 				+ FIELD_ITEM_CONTEXT + " TEXT" 
 				+ ")";
         
         db.execSQL(CREATE_CATALOG_TABLE);
         db.execSQL(CREATE_TERM_TABLE);
         db.execSQL(CREATE_ITEM_TABLE);
//        fillTestData();
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         // Drop older table if existed
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATALOGS);
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_TERMS);
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
         // Create tables again
         onCreate(db);	
     }
 	
 	public void addCatalog(Catalog catalog) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(FIELD_CATALOG_ID, catalog.getId()); 					// Catalog id
 		values.put(FIELD_CATALOG_PARENT_ID, catalog.getParent_id());	// Catalog parent_id
 		values.put(FIELD_CATALOG_NAME, catalog.getName()); 				// Contact name
 		values.put(FIELD_CATALOG_WEIGHT, catalog.getWeight());			// Contact weight
 		// Inserting Row
 		db.insert(TABLE_CATALOGS, null, values);
 		db.close(); // Closing database connection
 	}
 	
 	@Override
 	public List<Catalog> getRootCatalogs() {
 		List<Catalog> catalogList = new ArrayList<Catalog>();
 		// Select All Query
 		String selectQuery = "SELECT  * FROM " + TABLE_CATALOGS;
 
 		SQLiteDatabase db = this.getWritableDatabase();
 		Cursor cursor = db.rawQuery(selectQuery, null);
 
 		// looping through all rows and adding to list
 		if (cursor.moveToFirst()) {
 			do {
 				Catalog catalog = new Catalog();
 				catalog.setId(Long.parseLong(cursor.getString(0)));
 				if (cursor.getString(1) != null && !cursor.getString(1).equals(""))
 					catalog.setParent_id(Long.parseLong(cursor.getString(1)));
 				catalog.setName(cursor.getString(2));
 				if (cursor.getString(3) != null && !cursor.getString(3).equals(""))
 					catalog.setWeight(Long.parseLong(cursor.getString(3)));
 				// Adding contact to list
 				catalogList.add(catalog);
 			} while (cursor.moveToNext());
 		}
 		// return contact list
 		return catalogList;
 	}
 	
	public void fillTestData(){
 		addCatalog(new Catalog(Long.parseLong("11"), Long.parseLong("1"), Long.parseLong("40"), "test1"));
 		addCatalog(new Catalog(Long.parseLong("12"), Long.parseLong("1"), Long.parseLong("40"), "test2"));
 		addCatalog(new Catalog(Long.parseLong("13"), Long.parseLong("1"), Long.parseLong("40"), "test3"));
 		addCatalog(new Catalog(Long.parseLong("14"), Long.parseLong("1"), Long.parseLong("40"), "test4"));
 		addCatalog(new Catalog(Long.parseLong("15"), Long.parseLong("1"), Long.parseLong("40"), "test5"));
 		addCatalog(new Catalog(Long.parseLong("16"), Long.parseLong("1"), Long.parseLong("40"), "test6"));
 	}
 }
