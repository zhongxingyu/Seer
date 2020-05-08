 package com.dephi.bosojowo;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 import android.os.Bundle;
 import android.content.Intent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 public class ActivityCat extends SherlockActivity implements OnItemClickListener{
 	private ArrayList<HashMap<String, String>> resultDB;
 	private DatabaseHelper db;
 	
 	private ListView _list;
 	private AdapterList _adapter;
 	
 	private String _nextSource;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_sub_cat);
 		
 		// Inisiasi variabel member
 		db = new DatabaseHelper(this);
 		_list = (ListView)findViewById(R.id.listContent);
 		
 		// Ambil Intent Ektra dari pemanggilan sebelumnya
 		Intent intent = getIntent();
 		String source = intent.getStringExtra("SOURCE");
 		int id = getIntent().getIntExtra("ID", 0);
 		
 		// Lakukan pengambilan data di database dan set untuk intent berikutnya
 		if(source.equals("PEPAK_ID")){
 			resultDB = db.getCat(id);
 			_nextSource = "CATEGORY_ID";
 		} else if(source.equals("CATEGORY_ID")){
 			resultDB = db.getSubCat(id);
 			_nextSource = "SUBCATEGORY_ID";
 		} else if(source.equals("SUBCATEGORY_ID")){
 			resultDB = db.getPostsBySubCat(id);
 			_nextSource = "POSTS_ID";
 		} else if(source.equals("POSTS_ID")){
 			resultDB = db.getPostsByID(id);
 		}
 		
 		// Set adapter ke listView
 		if(source.equals("PEPAK_ID") || source.equals("CATEGORY_ID")){
 			_adapter=new AdapterList(this, resultDB, AdapterList.AIM.Category);
 		} else if(source.equals("SUBCATEGORY_ID")){
 			_adapter=new AdapterList(this, resultDB, AdapterList.AIM.Post);
 		} else if(source.equals("POSTS_ID")){
 			_adapter=new AdapterList(this, resultDB, AdapterList.AIM.Detail);
 		}
 
         _list.setAdapter(_adapter);		
         _list.setOnItemClickListener(this);
 	}
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	new Utilities(_adapter).createActionBarWholeApp(this, menu);
         return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {    	
     	new Utilities(this).actionBarResponseWholeApp(this, item);
     	return super.onOptionsItemSelected(item);
     }
 
 
 	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		Intent intent = new Intent(this, ActivityCat.class);
     	intent.putExtra("SOURCE", _nextSource);
    	intent.putExtra("ID", arg2+1);
     	startActivity(intent);
 	}
 }
