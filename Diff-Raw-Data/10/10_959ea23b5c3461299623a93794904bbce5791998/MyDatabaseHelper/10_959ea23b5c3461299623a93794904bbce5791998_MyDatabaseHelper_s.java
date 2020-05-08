 package ufit.DatabaseUtilities;
 
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class MyDatabaseHelper extends SQLiteOpenHelper {
 
 	//needs to be updated to our actual path /data/data/packageName/databases/
 	private static String dbPath = "/data/data/UFit.namespace/databases/";
 	 
    private static String dbName = "eDB.db";
  
     private SQLiteDatabase myDataBase; 
  
     private final Context myContext;
     
     
     private static final String DB_CREATE =
     		        "CREATE TABLE exerciseDB ( " +
     		        "_id integer PRIMARY KEY AUTOINCREMENT, " +
     		        "parent_id integer NOT NULL, " +
     		        "exercise text NOT NULL, " +
     		        "muscle_int integer NOT NULL, " +
     		        "type integer NOT NULL, " +
     		        "equipment text NOT NULL, " +
     		        "skill_level integer NOT NULL, " +
     		        "description text NOT NULL, " +
     		        "image_name_1 text NOT NULL DEFAULT noimage, " +
     		        "image_name_2 text NOT NULL DEFAULT noimage, " +
     		        "image_name_3 text NOT NULL DEFAULT noimage" +
     		        ");";
 
     private static final String DB_DESTROY =
         "DROP TABLE IF EXISTS exerciseDB";
     
 	// database fields
 	public static final String tName = "exerciseDB";
 	public static final String col_id = "_id";
 	public static final String col_pId = "parent_id";
 	public static final String col_exerc = "exercise";
 	public static final String col_muscle = "muscle_int";
 	public static final String col_type = "type";
 	public static final String col_equip = "equipment";
 	public static final String col_skill = "skill_level";
 	public static final String col_desc = "description";
 	public static final String col_iloc = "image_name_1";
 	public static final String col_iloc2 = "image_name_2";
 	public static final String col_iloc3 = "image_name_3";
 	
 
 
 	
 	
 	
 	
 	// common projections
 	public static final String[] proj_all = new String[] 
 			{col_id, col_pId, col_exerc, col_muscle, col_type, col_equip, col_skill, col_desc, col_iloc, col_iloc2,col_iloc3};
 	
 	public static final String[] proj_ids = new String[]
 			{col_id, col_pId};
 	
 	public MyDatabaseHelper(Context context) {
 		
 		super(context, dbName, null, 1);
 		this.myContext = context;
 
 	}
 	
 	  /**
      * Creates a empty database on the system and rewrites it with your own database.
      * */
     public void createDataBase() throws IOException{
  
     	boolean dbExist = checkDataBase();
  
     	if(dbExist){
     		//do nothing - database already exist
     	}else{
  
     		//By calling this method and empty database will be created into the default system path
                //of your application so we are going to be able to overwrite that database with our database.
         	this.getReadableDatabase();
         	this.close();
  
         	try {
  
     			copyDataBase();
  
     		} catch (IOException e) {
  
         		throw new Error("Error copying database");
  
         	}
     	}
  
     }
  
     /**
      * Check if the database already exist to avoid re-copying the file each time you open the application.
      * @return true if it exists, false if it doesn't
      */
     private boolean checkDataBase(){
  
     	SQLiteDatabase checkDB = null;
  
     	try{
     		File f = new File(dbPath);
     		if (!f.exists()) {
     		f.mkdir();
     		}
     		String myPath = dbPath + dbName;
     		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
     		
     	}catch(SQLiteException e){
  
     		//database does't exist yet.
  
     	}
  
     	if(checkDB != null){
  
     		checkDB.close();
  
     	}
  
     	return checkDB != null ? true : false;
     }
  
     /**
      * Copies your database from your local assets-folder to the just created empty database in the
      * system folder, from where it can be accessed and handled.
      * This is done by transferring byte-stream.
      * */
     private void copyDataBase() throws IOException{
  
     	//Open your local db as the input stream
     	InputStream myInput = myContext.getAssets().open(dbName);
  
     	// Path to the just created empty db
     	String outFileName = dbPath + dbName;
  
     	//Open the empty db as the output stream
     	OutputStream myOutput = new FileOutputStream(outFileName);
  
     	//transfer bytes from the inputfile to the outputfile
     	byte[] buffer = new byte[1024];
     	int length;
     	while ((length = myInput.read(buffer))>0){
     		myOutput.write(buffer, 0, length);
     	}
  
     	//Close the streams
     	myOutput.flush();
     	myOutput.close();
     	myInput.close();
  
     }
  
     public void openDataBase() throws SQLiteException{
  
     	//Open the database
         String myPath = dbPath + dbName;
     	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
     			//OPENING READ ONLY MAY CAUSE PROBLEMS WITH UPDATING AND CREATING
     }
     
     public void openDataBaseWritable() throws SQLiteException{
     	 
     	//Open the database
         String myPath = dbPath + dbName;
     	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
     			//OPENING READ ONLY MAY CAUSE PROBLEMS WITH UPDATING AND CREATING
     }
  
     @Override
 	public synchronized void close() {
  
     	    if(myDataBase != null)
     		    myDataBase.close();
  
     	    super.close();
  
 	}
  
 	@Override
 	public void onCreate(SQLiteDatabase db) {
  
 	}
  
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  
 	}
 	
 	public long insertExercise(int pid, String exerc, int musc, int type, String equip, int skill, String i1, String i2, String i3 ) {
 		return myDataBase.insert(tName, null, buildRow(pid,exerc,musc,type,equip,skill,i1,i2,i3));
 	}
 	
 	public long insertExercise(int pid, String exerc, int musc, int type, String equip, int skill) {
 		return myDataBase.insert(tName, null, buildRow(pid,exerc,musc,type,equip,skill));
 	}
 
 	public long updateExercise(int id, int pid, String exerc, int musc, int type, String equip, int skill, String i1, String i2, String i3 ) {
 		return myDataBase.update(tName, buildRow(pid,exerc,musc,type,equip,skill,i1,i2,i3), col_id + "=" + id, null);
 	}
 	
 	public long updateExercise(int id, int pid, String exerc, int musc, int type, String equip, int skill) {
 		return myDataBase.update(tName, buildRow(pid,exerc,musc,type,equip,skill), col_id + "=" + id, null);
 	}
 	
     public long deleteExercise(int id) {
         return myDataBase.delete(tName, col_id + "=" + id, null);
     }
    
     //col_id, col_pId, col_exerc, col_muscle, col_type, col_equip, col_skill, col_desc, col_iloc, col_iloc2,col_iloc3
     private ContentValues buildRow(int pid, String exerc, int musc, int type, String equip, int skill, String i1, String i2, String i3 ){
     	ContentValues row = new ContentValues();
     	row.put(col_pId, pid);
     	row.put(col_exerc, exerc);
     	row.put(col_muscle, musc);
     	row.put(col_type, type);
     	row.put(col_equip, equip);
     	row.put(col_skill, skill);
     	row.put(col_iloc, i1);
     	row.put(col_iloc2, i2);
     	row.put(col_iloc3, i3);	
 
     	return row;
     }
     
     private ContentValues buildRow(int pid, String exerc, int musc, int type, String equip, int skill){
     	ContentValues row = new ContentValues();
     	row.put(col_pId, pid);
     	row.put(col_exerc, exerc);
     	row.put(col_muscle, musc);
     	row.put(col_type, type);
     	row.put(col_equip, equip);
     	row.put(col_skill, skill);
     	return row;
     }
     
     //Query
     public Cursor queryH (String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy)
     {
     	return myDataBase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy); 
     }
     	
 
 }
