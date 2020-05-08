 package com.view;
 
 import com.view.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 import database.DbHelper;
 
 public class MainActivity extends Activity {
 
 	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
 	public final static Integer COLUMN_REQ_AMOUNT = 0;
 	public final static String TELEPHONE_NUMBER = "000019";
 	//public final static String TELEPHONE_NUMBER = "15555215556";
 	//public final static String TELEPHONE_NUMBER = "15555215556";
 	// public final static String TELEPHONE_NUMBER = "+79244360943";
 
 	public static String my_text = null;
 	public static String text = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	/** Called when the user clicks the Send button */
 	public void showMessages(View view) {
 
 		// Intent intent = new Intent(this, DisplayMessageActivity.class);
 		Intent intent = new Intent(this, Test.class);
 		/*
 		 * EditText editText = (EditText) findViewById(R.id.edit_message);
 		 * Integer rowNumReq = 0; try { rowNumReq =
 		 * Integer.parseInt(editText.getText().toString()); } catch
 		 * (NumberFormatException ex) {
 		 * 
 		 * } Test testClass = new Test(); String message =
 		 * testClass.getSMS(this, rowNumReq); intent.putExtra(EXTRA_MESSAGE,
 		 * message);
 		 */
 		startActivity(intent);
 		// Toast.makeText(this, String.valueOf(SmsReceiver.sms) + " = " +
 		// String.valueOf(SmsReceiver.db), Toast.LENGTH_LONG).show();
 
 	}
 
 	public void showTable(View view) {
 
 		Intent intent = new Intent(this, DisplayTableActivity.class);
 		startActivity(intent);
 	}
 
 	public void deleteAll(View view) {
 
 		DbHelper dbHelper = new DbHelper(this, null, null,
 				DbHelper.getDBVersion());
 		Integer deletedRows = dbHelper.deleteAll();
 		Toast.makeText(
 				this,
 				deletedRows > 0 ? "deleted " + deletedRows.toString() + " rows"
 						: "table already empty", Toast.LENGTH_LONG).show();
 
 	}
 
	public void addAll(View view) {
 
 		DbHelper dbHelper = new DbHelper(this, null, null,
 				DbHelper.getDBVersion());
 
		//EditText editText = (EditText) findViewById(R.id.edit_message);
 		Integer rowsAdded = 0;
 
		/*try {
 			Integer smsNumber = Integer.valueOf(editText.getText().toString());
 			if (smsNumber > 0) {
 				rowsAdded = dbHelper.addAll(this, smsNumber);
 			} else {
 				rowsAdded = dbHelper.addAll(this);
 			}
		} catch (NumberFormatException ex) {*/
 			rowsAdded = dbHelper.addAll(this);
		//}
 
 		Toast.makeText(this, "added " + rowsAdded + " rows", Toast.LENGTH_LONG)
 				.show();
 
 	}
 
	/*public void deleteSms(View view) {
 
 		EditText editText = (EditText) findViewById(R.id.edit_message);
 		Integer reqId = 0;
 		try {
 			reqId = Integer.parseInt(editText.getText().toString());
 		} catch (NumberFormatException ex) {
 
 		}
 
 		DbHelper dbHelper = new DbHelper(this, null, null,
 				DbHelper.getDBVersion());
 
 		Integer rowsDeleted = 0;
 
 		if (reqId > 0) {
 			rowsDeleted = dbHelper.deleteBySmsId(reqId);
 		}
 
 		Toast.makeText(this, "deleted " + rowsDeleted + " rows",
 				Toast.LENGTH_LONG).show();
 
 	}
 
 	public void deleteRecord(View view) {
 
 		EditText editText = (EditText) findViewById(R.id.edit_message);
 		Integer reqId = 0;
 		try {
 			reqId = Integer.parseInt(editText.getText().toString());
 		} catch (NumberFormatException ex) {
 
 		}
 
 		DbHelper dbHelper = new DbHelper(this, null, null,
 				DbHelper.getDBVersion());
 
 		Integer rowsDeleted = 0;
 
 		if (reqId > 0) {
 			rowsDeleted = dbHelper.deleteById(reqId);
 		}
 
 		Toast.makeText(this, "deleted " + rowsDeleted + " rows",
 				Toast.LENGTH_LONG).show();
 
 	}*/
 
 	public void dropDatabase(View view) {
 
 		DbHelper dbHelper = new DbHelper(this, null, null,
 				DbHelper.getDBVersion());
 		try {
 
 			dbHelper.recreateDatabase();
 			Toast.makeText(this, "database successfully recreated",
 					Toast.LENGTH_LONG).show();
 
 		} catch (Exception ex) {
 
 			Toast.makeText(this,
 					"some exception was occured on recreate database",
 					Toast.LENGTH_LONG).show();
 
 		}
 
 	}
 
 	public void findByName(View view) {
 
 		DbHelper dbHelper = new DbHelper(this, null, null,
 				DbHelper.getDBVersion());
 
 	}
 
 	public void showPanes(View view) {
 
 		Intent intent = new Intent(this, DisplayPanesActivity.class);
 		startActivity(intent);
 
 	}
 
 	public void showCodes(View view) {
 
 		Intent intent = new Intent(this, TestShowCodes.class);
 
 		startActivity(intent);
 	}
 
 }
