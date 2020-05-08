 package com.example.mobileindia;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.FindCallback;
 import com.parse.ParseQuery;
 
 import android.annotation.SuppressLint;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
import android.widget.ArrayAdapter;
import com.parse.ParseObject;
 
 public class ListViewCategory extends ListActivity {
     //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    static ArrayList<ArrayList<String>> listItems=new ArrayList<ArrayList<String>>();
    static String CATEGORY = "";
    
    
     
     //DEFINING STRING ADAPTER WHICH WILL HANDLE DATA OF LISTVIEW
     Item_Adapter adapter0;
   
     int clickCounter=0;
 
     @SuppressLint("NewApi")
 	@Override
     public void onCreate(Bundle savedInstanceState) {
     	try {
 			populate_list();
 		} catch (ParseException e) {
 			/// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	Log.v("LIST", "LIST : create    APPCLASS");
         super.onCreate(savedInstanceState);      
         setContentView(R.layout.activity_list_view_category);
     	
         adapter0 = new Item_Adapter(this, R.layout.list, listItems);
         setListAdapter(adapter0);
     	
         
         // Make sure we're running on Honeycomb or higher to use ActionBar APIs
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             // Show the Up button in the action bar.
             getActionBar().setDisplayHomeAsUpEnabled(true);
         }
     }
     
    public static void  populate_list() throws ParseException{
 	   
     	ParseQuery get = new ParseQuery("Post");
     	get.whereEqualTo("category", ListViewCategory.CATEGORY);
 //    	get.findInBackground(new FindCallback(){
 //    		public void done(List<ParseObject> objects, ParseException e){
 //    			if(e == null){
     				List<ParseObject> objects = get.find();
     				listItems.clear();
     				
     				for (ParseObject parseObject : objects) {
     					ArrayList<String> temp = new ArrayList<String>();
     					temp.add(parseObject.getString("title"));
     					temp.add(parseObject.getString("summary"));
     					temp.add(parseObject.getString("author"));
     					listItems.add(temp);
     					//Invalidate();
 					}
 //    			}else{
 //    				
 //    			}
 //    		}
 //    	});
     }
 
     //METHOD WHICH WILL HANDLE DYNAMIC INSERTION
     public void addItems(View v) {
     	//Log.v("LIST", "LIST : ADD    APPCLASS");
     	ArrayList<String> temp = new ArrayList<String>();
         temp.add("Test "+clickCounter++);
         temp.add("This is a test post");
         temp.add("Jeff Walsh");
         //listItems.add(temp);
        
         Intent intent = new Intent(this, Add_Post.class);
 		startActivity(intent);
         adapter0.notifyDataSetChanged();
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case android.R.id.home:
             NavUtils.navigateUpFromSameTask(this);
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
 	public void onPause(){
 		super.onPause();
 		Log.v("LIST", "PAUSED LIST APPCLASS");
 
 	}
 	
 	@Override
 	public void onResume(){
 		Log.v("List", "RESUMED List APPCLASS");
 		super.onResume();
 		//adapter0.notifyDataSetChanged();
 		
 	}
 	
 	@Override
 	public void onStop(){
 		super.onStop();
 		Log.v("LIST", "Stopped LIST APPCLASS");
 	}
	
	public void addPost(){
		
		ParseObject Post = new ParseObject("Post");
		Post.put("author", "Jeff");
		Post.put("description", "This is some really nice junk I am selling!!!");
		Post.put("title", "Junk Sale");
		Post.saveInBackground();
		
		
	}
 
 
 
 
 
 
 
 
 }
 
