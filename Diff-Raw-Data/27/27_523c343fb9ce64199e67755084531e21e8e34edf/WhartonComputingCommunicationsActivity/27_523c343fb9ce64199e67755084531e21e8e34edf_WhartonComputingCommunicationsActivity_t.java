 package edu.upenn.cis350;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 
 public class WhartonComputingCommunicationsActivity extends Activity {
 	
 	// fields for changing activities
 	public static final int ACTIVITY_Home = 0;
 	public static final int ACTIVITY_CreateNewEvent = 1;
 	private static final int ACTIVITY_ShowEvent = 2;
 	private static final int ACTIVITY_Agenda = 3;
 	public static final int ACTIVITY_ShowComments = 4;
 	public static final int ACTIVITY_Register = 5;
 
 
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);   
     }
     
     public void login(View view) {
 		AndroidOpenDbHelper dbHelper = new AndroidOpenDbHelper(this);
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		dbHelper.createUsersTable(db);
 
 		String uname = ((EditText)findViewById(R.id.loginUsername)).getText().toString();
 		String pw = ((EditText)findViewById(R.id.loginPassword)).getText().toString();
 		
 		if (uname.equals("") || pw.equals("")) {
 			Toast.makeText(this, "Login failed.", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		
 		String[] columns = {dbHelper.COLUMN_NAME_USER_NAME, dbHelper.COLUMN_NAME_USER_PW};
 		
 		Cursor cursor = db.query(dbHelper.TABLE_NAME_USERS, columns, 
 				dbHelper.COLUMN_NAME_USER_NAME + " = '" + uname + "' AND " +
 				dbHelper.COLUMN_NAME_USER_PW + " = '" + pw + "'", null, null, null, null);
 		startManagingCursor(cursor);
 		int count = cursor.getCount();
 		db.close();
 		
 		if(count > 0) {
     		Toast.makeText(this, "Login Successful.", Toast.LENGTH_SHORT).show();
 			Intent i = new Intent(this, Home.class);
 			i.putExtra("user", uname);
 			startActivityForResult(i, ACTIVITY_Home);
 		} else {
 			Toast.makeText(this, "Login failed.", Toast.LENGTH_SHORT).show();
 		}
     }
     
     public void clickRegister(View view) {
     	Intent i = new Intent(this, Register.class);
     	startActivityForResult(i, ACTIVITY_ShowComments);
     }
     
     @Override
     public void onBackPressed() {
        Intent i = new Intent(this, WhartonComputingCommunicationsActivity.class);
        startActivity(i);
     }   }
