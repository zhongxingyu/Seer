 package ca.ualberta.cmput301w13t11.FoodBook.model;
 
import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DbOpenHelper extends SQLiteOpenHelper {
 	
	public DbOpenHelper(Context context, String name) {
		super(context, name, null, 1);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
