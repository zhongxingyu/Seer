package event.monitor.models;
 
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.*;
 import android.util.Log;
 
 
 public class User extends SQLiteOpenHelper {
 	private static final String DB_TABLE_NAME = "users";
 	private static final String DB_COLUMN_1_NAME = "name";
 	private String sql;
 	SQLiteDatabase db;
 	private Object sqliteDBInstance;
 	
	public User(Context context, String name, CursorFactory factory, int version) {
 		super(context, name, factory, version);
 	}
 
 	public void createTable(SQLiteDatabase db) {
 		db.execSQL("CREATE TABLE users (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT, phone TEXT, address TEXT, gender TEXT, workshop_id INTEGER)");
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void saveAttributes(String name, String email, String phone, String address, String gender){
 		db=getWritableDatabase();
 		sql="INSERT INTO users (name, email, phone, address, gender) VALUES ('" + name + "','" + email + "','" + phone + "','" + address + "','" + gender + "')";
 		db.execSQL(sql);
 		db.close();
 	}
 	
 	public void updateAttributes(int userId, String name, String email, String phone, String address, String gender){
 		db=getWritableDatabase();
 		String strFilter = "_id=" + userId;
 		ContentValues args = new ContentValues();
 		args.put("name", name);
 		args.put("email", email);
 		args.put("phone", phone);
 		args.put("address", address);
 		args.put("gender", gender);
 		db.update("users", args, strFilter, null);
 		db.close();
 	}
 	
 	public void destroy(String name){
 		db=getWritableDatabase();
 		db.delete("users", "name='" + name + "'", null);
 		db.close();
 	}
 	
 	public ArrayList<CUser> all(){
 		db = getWritableDatabase();
 		sql = "SELECT _id,name,email FROM users";
 		Cursor cursor = db.rawQuery(sql, null);
 		ArrayList<CUser> users = new ArrayList<CUser>();
 
 		while (cursor.moveToNext())
 		{
 			CUser user = new CUser();
 			user._id = cursor.getString(0);
 			user.name = cursor.getString(1);
 			user.email = cursor.getString(2);
 			users.add(user);
 		}
 		db.close();
 		cursor.close();
 		return users;
 	}
 	
 	public String[] getUserId(String name){
 		db = getWritableDatabase();
 		String result[] = new String [3];
 		sql = "SELECT _id,name FROM users WHERE name='" + name + "'";
 		Cursor c = this.getReadableDatabase().rawQuery(sql, null);
 		int _id;
 		_id =c.getColumnIndex("_id");
 		c.moveToLast();
 		result[0] = c.getString(_id)/*+ " "+c.getString(inam)+ "\n"*/;
 		db.close();
 		c.close();
 		return result;
 	}
 	
 	public String[] getUsersData(String name){
 		db = getWritableDatabase();
 		String result[] = new String [5];
 		sql = "SELECT _id,name,phone,email,address FROM users WHERE name='" + name + "'";
 		Cursor c = this.getReadableDatabase().rawQuery(sql, null);
		int _id, name1, email, phone, address;
 		_id =c.getColumnIndex("_id");
		name1 =c.getColumnIndex("name");
 		phone =c.getColumnIndex("phone");
 		email =c.getColumnIndex("email");
 		address =c.getColumnIndex("address");
 		c.moveToLast();
 		result[0] = c.getString(_id);
		result[1] = c.getString(name1);
 		result[2] = c.getString(phone);
 		result[3] = c.getString(email);
 		result[4] = c.getString(address);
 		db.close();
 		c.close();
 		return result;
 	}
 	
 	
 	public void openDB() throws SQLException
     {
         Log.i("openDB", "Checking sqliteDBInstance...");
         if(this.sqliteDBInstance == null)
         {
             Log.i("openDB", "Creating sqliteDBInstance...");
             this.sqliteDBInstance = this.getWritableDatabase();
         }
     }
  
     public void closeDB()
     {
         if(this.sqliteDBInstance != null)
         {
             if(((SQLiteDatabase) this.sqliteDBInstance).isOpen())
                 ((Cursor) this.sqliteDBInstance).close();
         }
     }

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
	}
  
 	
 }
