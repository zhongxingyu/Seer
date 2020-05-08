 package com.nickotter.randomname.crudActivities;
 
 import android.os.Bundle;
import android.widget.EditText;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
import com.nickotter.randomname.CRUD;
import com.nickotter.randomname.Group;
 import com.nickotter.randomname.R;
 import com.nickotter.randomname.R.layout;
 import com.nickotter.randomname.R.menu;
 
 public class AddGroup extends SherlockFragmentActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.add_group);
 		
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
        	  
        	  EditText groupElement = (EditText)findViewById(R.id.groupName);
        	  
        	  Group g1 = new Group(groupElement.getText().toString());
        	  
        	  databaseCRUD.add_group(g1);
        	  databaseCRUD.close();
        	  
        	  finish();
        	  return true;
        	  
           default:            
              return super.onOptionsItemSelected(item);    
        }
     }
 
 }
