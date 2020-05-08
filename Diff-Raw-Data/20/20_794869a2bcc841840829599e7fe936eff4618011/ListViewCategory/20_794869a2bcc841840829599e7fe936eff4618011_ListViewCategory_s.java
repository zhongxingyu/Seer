 package com.example.mobileindia;
 
 import java.util.ArrayList;
 import java.util.List;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import android.annotation.SuppressLint;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 
 public class ListViewCategory extends ListActivity {
     //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    static ArrayList<ArrayList<String>> listItems=new ArrayList<ArrayList<String>>();
    public static String CATEGORY = "";
    public static String CITY = "";
    public static boolean hideAdd = false;
    public static boolean forceHome = false;
    public static List<ParseObject> parsePostList =  new ArrayList<ParseObject>();
    public static boolean forceSearch;
    
     
     //DEFINING STRING ADAPTER WHICH WILL HANDLE DATA OF LISTVIEW
     Item_Adapter adapter0;
   
     int clickCounter=0;
 
     @SuppressLint("NewApi")
 	@Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	try {
     		if(ListViewCategory.parsePostList != null){
     			populate_list(ListViewCategory.parsePostList);
     	    }else{
     	    	populate_list(null);
     	    }
 		} catch (ParseException e) {
 			/// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	//Log.v("Post", "POST : listView built  APPCLASS ");
         super.onCreate(savedInstanceState);      
         setContentView(R.layout.activity_list_view_category);
        // Log.v("Post", "POST : content set  APPCLASS ");
        adapter0 = new Item_Adapter(this, R.layout.list, listItems);
         setListAdapter(adapter0);
       //  Log.v("Post", "POST : adapter set APPCLASS ");
         
         // Make sure we're running on Honeycomb or higher to use ActionBar APIs
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             // Show the Up button in the action bar.
             getActionBar().setDisplayHomeAsUpEnabled(true);
         }
     }
     protected void onPostCreate (Bundle savedInstanceState){
  		super.onPostCreate(savedInstanceState);
  		if(ListViewCategory.hideAdd){
  			findViewById(R.id.addBtn).setVisibility(8);
  		}
     }
     
    public static void  populate_list(List<ParseObject> parsePostList2) throws ParseException{
 	    if(parsePostList2 == null){
 	    	defaultPopulateList();
 	    }else{
 			populateYourPosts(parsePostList2);
 	    }
 
     }
 
 private static void defaultPopulateList() throws ParseException {
 	ParseQuery get = new ParseQuery("Post");
 	get.whereEqualTo("category", ListViewCategory.CATEGORY);
 	get.whereEqualTo("city", ListViewCategory.CITY);
 				List<ParseObject> objects = get.find();
 				listItems.clear();
 				for (ParseObject parseObject : objects) {
 					ArrayList<String> temp = new ArrayList<String>();
 					temp.add(parseObject.getString("title"));
 					temp.add(parseObject.getString("summary"));
 					temp.add(parseObject.getString("author"));
 					String num_string = Integer.toString(parseObject.getInt("post_num"));
 					temp.add(num_string);
 					//change to object ID
 					//temp.add(parseObject.getObjectId());
 					//add automatic date
 					temp.add(parseObject.getString("date"));
 					listItems.add(temp);	
 				}
 }
 
 private static void populateYourPosts(List<ParseObject> parsePostList2) {
 	listItems.clear();
 	for (ParseObject parseObject : parsePostList2) {
 		ArrayList<String> temp = new ArrayList<String>();
 		temp.add(parseObject.getString("title"));
 		temp.add(parseObject.getString("summary"));
 		temp.add(parseObject.getString("author"));
 		String num_string = Integer.toString(parseObject.getInt("post_num"));
 		temp.add(num_string);
 		temp.add(parseObject.getString("date"));
 		Log.v("Post", "POST : post_num LIST   APPCLASS = " + num_string);
 		listItems.add(temp);			
 	}
	ListViewCategory.parsePostList = null;
 }
 
     //METHOD WHICH WILL HANDLE DYNAMIC INSERTION
     public void addItems(View v) {
         Intent intent = new Intent(this, Add_Post.class);
 		startActivity(intent);
         adapter0.notifyDataSetChanged();
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	Intent back = new Intent(this,Categories2.class);
         startActivity(back);
         return super.onOptionsItemSelected(item);
     }
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (ListViewCategory.forceHome) {
         	Intent back = new Intent(this,MainActivity.class);
             startActivity(back);
         }else if (forceSearch) {
         	Intent back = new Intent(this,SearchPostActivity.class);
             startActivity(back);
 		}else if(keyCode == KeyEvent.KEYCODE_BACK){
         	Intent back = new Intent(this,Categories2.class);
             startActivity(back);
         }
         ListViewCategory.forceSearch = false;
         ListViewCategory.forceHome = false;
         return super.onKeyDown(keyCode, event);
     }
 
     @Override
 	public void onPause(){
 		super.onPause();
 	}
 	
 	@Override
 	public void onResume(){
 		super.onResume();		
 	}
 	
 	@Override
 	public void onStop(){
 		super.onStop();
 	}
 	
 	public void SinglePost(View view){
 		 	Button title = (Button) view.findViewById(R.id.full_post_button);
 		 	Single_Post.NUM =  (String) title.getHint();
 		 	if(Single_Post.NUM != null){
 		 		Log.v("List", "hint SINGLE APPCLASS = " +  Single_Post.NUM  + "     end");
 		 		Intent intent = new Intent(this, Single_Post.class);
 				startActivity(intent);
 		 	}
 		}
 
 
 }
 
