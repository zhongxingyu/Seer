 package com.example.ultistats;
 
 import com.example.ultistats.model.Player;
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class PlayerEditActivity extends FragmentActivity {
 	
 	private String playerId;
 	private EditText fnameEditText;
 	private EditText lnameEditText;
 	private EditText numberEditText;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Intent intent = getIntent();
         setContentView(R.layout.player_edit);
         
         playerId = intent.getStringExtra(PlayerListActivity.PLAYER_ID);
         fnameEditText = (EditText) findViewById(R.id.edit_fname);
         lnameEditText = (EditText) findViewById(R.id.edit_lname);
         numberEditText = (EditText) findViewById(R.id.edit_number);
         
         //Creating a new player
         if (playerId == null)
         	return;
         
         Cursor cursor = getContentResolver().query(
 	        Uri.withAppendedPath(Player.CONTENT_URI, playerId), null, null, null, null);
         
         cursor.moveToFirst();
         String fname = cursor.getString(cursor.getColumnIndex("fname"));
         fnameEditText.setText(fname);
         
         String lname = cursor.getString(cursor.getColumnIndex("lname"));
         lnameEditText.setText(lname);
         
         String number = cursor.getString(cursor.getColumnIndex("number"));
         numberEditText.setText(number);
         
         cursor.close();
     }
     
     public void savePlayer(View view) {
     	ContentValues playerValues = new ContentValues();
 
     	String fname = fnameEditText.getText().toString();
     	String lname = lnameEditText.getText().toString();
     	String number = numberEditText.getText().toString();
     	
     	if (fname.length() == 0 && lname.length() == 0) {
    		EditText focus = null;
    		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
    		
    		//find a way to get the error to display in only the focused box
    		if (fnameEditText.hasFocus())
    			focus = fnameEditText;
    		else if (lnameEditText.hasFocus())
    			focus = lnameEditText;
    		
    		focus.setError("First or last name required");
     		
 //    		alertDialog.setTitle("Error");
 //    		alertDialog.setMessage("First name or last name required");
 //    		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
 //    		   public void onClick(DialogInterface dialog, int which) {
 //    		   }
 //    		});
 //    		alertDialog.show();
     		return;
     	}
     	
 		playerValues.put("fname", fname); 
 		playerValues.put("lname", lname);
 		playerValues.put("number", number);
 		
     	if (playerId == null) {
 	    	getContentResolver().insert(
 	    		Uri.withAppendedPath(Player.CONTENT_URI, Player.NEW_URI), playerValues
     		);
     	} else {
 	    	String selectionClause = "_id = ?";
 	    	String[] selectionArgs = {playerId};
 	    	getContentResolver().update(
 	    		Uri.withAppendedPath(Player.CONTENT_URI, playerId), playerValues, selectionClause, selectionArgs 
 	    	); 
     	}
     	
     	finish();
     }
 }
