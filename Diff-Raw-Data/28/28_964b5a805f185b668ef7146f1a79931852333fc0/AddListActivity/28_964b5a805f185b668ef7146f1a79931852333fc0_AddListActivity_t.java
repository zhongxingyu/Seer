 package com.eseteam9.shoppyapp;
 
 import com.eseteam9.shoppyapp.R;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class AddListActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_add_list);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.add_list, menu);
 		return true;
 	}
 
 	//Called when Savebutton is pressed
 	public void addList(View view){
 
 	    EditText editText = (EditText) findViewById(R.id.list_add);
 	    String listname = editText.getText().toString();
 
 		LocalDatabaseHandler db = new LocalDatabaseHandler(this);
 		
 		//Check if List has name or already exists
 		if (listname.length() == 0)
 	    	Toast.makeText(this, "Please enter a name", 1000).show();
 		else if (!db.existsShoppingList(listname)){
 		    //Add Entry in DB
		    db.addShoppingList(new ShoppingList(listname, "Owner"));
 		    
 		    //Switch to DisplayListsActivity
 		    Intent intent = new Intent(this, DisplayListsActivity.class);
 		    startActivity(intent);
 	    } 
 	    else
 	    	Toast.makeText(this, "This list already exists", 1000).show();
 	}
 }
