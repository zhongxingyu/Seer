 package com.eseteam9.shoppyapp;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class EditListActivity extends Activity {
 	private int listid;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_edit_list);
 		Intent intent = getIntent();
 		this.listid = intent.getIntExtra("LIST_ID", 0);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		
 		getMenuInflater().inflate(R.menu.edit_list, menu);
 		return true;
 	}
 	public void editList(View view){
 
 	    EditText editText = (EditText) findViewById(R.id.list_edit);
 	    String listname = editText.getText().toString();
 		
 		//Check if List has name or already exists
 		if (listname.length() == 0)
 	    	Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
 		else if (!new ShoppingListHandler(this).existsEntry(listname)){
		    //Add Entry in DB
		    new ShoppingListHandler(this).update(listid, listname);
 		    
 		    //Switch to DisplayListsActivity
 		    Intent intent = new Intent(this, DisplayListsActivity.class);
 		    startActivity(intent);
 	    } 
 	    else
 	    	Toast.makeText(this, "This list already exists", Toast.LENGTH_SHORT).show();
 	}
 }
 
 
