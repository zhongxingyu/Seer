 package com.example.quickshop;
 
import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.SQLException;
 import android.text.Editable;
import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.Spinner;
 import android.support.v4.app.NavUtils;
 import com.example.quickshop.R;
 
 public class TestExpand extends Activity implements OnItemSelectedListener {
 	List<String> Cat = new ArrayList<String>();
 	public final static String EXTRA_MESSAGE = "com.example.quickshop.MESSAGE";
 	String message;
 	
 	private ExpandListAdapter ExpAdapter;
 	private ArrayList<ExpandListGroup> ExpListItems;
 	private ExpandableListView ExpandList;
 	Spinner spinner;
 	private String itemChild;
     private String dropDownCat;
     private String addItemButtonClickflag = "false";
     DatabaseHandler db = new DatabaseHandler(this);
     
 	@Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_test_expand);
         String noChild = "";
         String noDrop = "";
         
         
         try {
         	db.createDataBase();
         } catch (IOException ioe) {
         	throw new Error("Unable to create DB");
         }
         
         try {
         	db.openDataBase();
         } catch (SQLException sqle){
         	throw sqle;
         }
         
         // INSERT STATEMENTS go here for the first time . You need to remove them after inserting to avoid Key constraint. 
         
         ExpandList = (ExpandableListView) findViewById(R.id.ExpList);
         ExpListItems = SetStandardGroups(noChild, noDrop, addItemButtonClickflag);
         ExpAdapter = new ExpandListAdapter(TestExpand.this, ExpListItems);
         ExpandList.setAdapter(ExpAdapter);
         
         Intent intent = getIntent();
         message = intent.getStringExtra(TestExpand.EXTRA_MESSAGE);
         // Loading spinner data
         spinner = (Spinner) findViewById(R.id.spinner);
        
         spinner.setOnItemSelectedListener(this);
         
         loadSpinnerData();
         
     }
  
 
 	/** 
 	 * LOADING THE DROP DOWN LIST */
	@SuppressWarnings("null")
 	private void loadSpinnerData() {
 	
 		//DatabaseHandler dbspin = new DatabaseHandler(this);
 		List<Category> catData = db.getAllCategories();
 		
 		ArrayList<String> catStringData = new ArrayList<String>();
 		
 			for(Category c : catData){
 				
 				String var = c.getCatName();
 				catStringData.add(var);
 				}
 			
 		
 		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                 android.R.layout.simple_spinner_item, catStringData);
 	
 	   // drop down loyout style - listview with radiobuttons
 		dataAdapter
         .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 		// attaching dataAdapter to Spinner
 		spinner.setAdapter(dataAdapter);
 		
 	
 	}
 
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_test_expand, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
             case R.id.refresh_sort:
             	sort_categories();
             	return true;
         }
         return super.onOptionsItemSelected(item);
     }
     
     private void sort_categories() {
     	return;
     }
    
    
     public ArrayList<ExpandListGroup> SetStandardGroups(String ch , String drop, String btnClickflag) {
     	
     	ArrayList<ExpandListGroup> groupList = new ArrayList<ExpandListGroup>();
     	  
     	//DatabaseHandler db = new DatabaseHandler(this);
     	List<Category> categories = db.getAllCategories();
     	
     	for(Category c: categories){
     		
     		ExpandListGroup group = new ExpandListGroup();
     		String var = c.getCatName();
     		group.setName(var);
     		
     		if(btnClickflag.equals("true")){
     			
     				List<ItemCatNew> items = (List<ItemCatNew>) db.getItemsNewList(var);
     	    		ArrayList<ExpandListChild> childList = new ArrayList<ExpandListChild>();
     	        	
     	    		for(ItemCatNew itc : items){
     	        		ExpandListChild child = new ExpandListChild();
     	        		child.setName(itc.getItemName());
     	        		childList.add(child);
     	           	}
     	            	
     	        	group.setItems(childList);
     	   		}
         	groupList.add(group);
       	}
    
     	return groupList;
     }
     
     public void btnAddItem (View view){
     	
     	EditText editText = (EditText) findViewById(R.id.editText2);
     	Editable temp = editText.getText();
     	itemChild = temp.toString();
     	addItemButtonClickflag = "true";
    
     	//DatabaseHandler db = new DatabaseHandler(this);
     	db.addItemCatNew(new ItemCatNew(itemChild, dropDownCat));
     	
     	ExpListItems = SetStandardGroups(itemChild , dropDownCat, addItemButtonClickflag);
     	ExpAdapter = new ExpandListAdapter(TestExpand.this, ExpListItems);
         ExpandList.setAdapter(ExpAdapter);
     	
     }
 
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int position,
 			long id) {
 		
 		dropDownCat = parent.getItemAtPosition(position).toString();
 		 
 	}
 
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 }
 
 
