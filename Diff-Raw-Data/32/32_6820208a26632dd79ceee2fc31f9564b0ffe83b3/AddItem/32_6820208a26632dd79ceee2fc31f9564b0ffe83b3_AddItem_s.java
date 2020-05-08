 package com.nickotter.randomname.crudActivities;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.EditText;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.nickotter.randomname.CRUD;
 import com.nickotter.randomname.Item;
 import com.nickotter.randomname.MyList;
 import com.nickotter.randomname.R;
 
 public class AddItem extends SherlockFragmentActivity {
 
 	final String LOGTAG = "AddListActivity";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.add_item);
 		
 		ActionBar bar = getSupportActionBar();
         bar.setDisplayHomeAsUpEnabled(true);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getSupportMenuInflater();
 	    inflater.inflate(R.menu.edit_general, menu);
 	    return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) 
     {    
        switch (item.getItemId()) 
        {        
           case android.R.id.home:
           case R.id.cancelButton:
         	  finish();
               return true;  
               
           case R.id.doneButton:
         	  CRUD databaseCRUD = new CRUD(this);
         	  databaseCRUD.open();
         	  
         	  //Item check
         	  EditText itemField = (EditText)findViewById(R.id.itemName);
         	  if(itemField == null)
         	  {
         		  Log.v(LOGTAG, "Error: item was blank");    	
         		  finish();
         	  }
         	  
         	  //list check + return
         	  EditText listField = (EditText)findViewById(R.id.listName);
         	  MyList list = databaseCRUD.get_list(listField.getText().toString());
         	  
         	  if(list == null)
         	  {
         		  Log.v(LOGTAG, "No list found throwing error and finishing");
         		  finish();
         	  }
         	  else 
         	  {
         		  Log.v(LOGTAG, "A list was selected with name and ID: " + list.getName() + " " + list.getID());
         		  Item i1 = new Item(0, list.getID(), itemField.getText().toString());
         		  databaseCRUD.add_item(list, i1);
         	  }
 
         	  
         	  //finish
         	  Log.v(LOGTAG, "New list entry sucessful");
         	  databaseCRUD.close();
         	  
         	  finish();
         	  return true;
         	  
           default:            
              return super.onOptionsItemSelected(item);    
        }
     }
 
 }
