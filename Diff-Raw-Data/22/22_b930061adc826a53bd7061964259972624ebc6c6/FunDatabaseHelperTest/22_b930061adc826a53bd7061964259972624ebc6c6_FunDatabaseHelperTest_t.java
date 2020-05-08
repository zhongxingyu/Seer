 package net.creativelift.sqlitefun.test;
 
import net.creativelift.sqlitefun.FunOpenHelper;
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.test.AndroidTestCase;
 
 public class FunDatabaseHelperTest extends AndroidTestCase {
 	
 	private Context testContext;
     private SQLiteDatabase database;
     private FunOpenHelper dbHelper;
 	
 	@Override
     protected void setUp() throws Exception {
         super.setUp();
         testContext = this.getContext();
 		dbHelper = new FunOpenHelper(testContext);
     }
 	
 	public void testGettingAWritableDatabase() {
         database = dbHelper.getWritableDatabase();
         assertTrue(database instanceof SQLiteDatabase);
 	}
 
 }
