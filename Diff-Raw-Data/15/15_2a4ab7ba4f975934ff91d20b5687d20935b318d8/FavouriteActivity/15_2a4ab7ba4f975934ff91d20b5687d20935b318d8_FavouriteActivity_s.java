 package com.engine9;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 
 /**
  * Edit and save the favourite services
  * */
 public class FavouriteActivity extends Activity {
 
 	private ListView favList;  //create a list to store the favourite services
 	private FavouriteAdapter adapter; 
 	private EditText favText;
 	
 	protected void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_favourite);
 		favList = (ListView) findViewById(R.id.abstract_list);
 		adapter = new FavouriteAdapter(getApplicationContext(), 
 				FavouriteManager.getFavourites(getApplicationContext()));
 		favList.setAdapter(adapter);
 		
 		favList.setOnItemLongClickListener(new OnItemLongClickListener(){
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
 					int pos, long arg3) {
 				// TODO Auto-generated method stub
				Log.d("DEBUG", "TEST");
 				FavouriteDialog fd = new FavouriteDialog();
 				fd.fi = FavouriteManager.getFavourites(getApplicationContext()).get(pos);
 				fd.title += fd.fi.name;
 				fd.adapter = adapter;
 				fd.show(getFragmentManager(), "edit");
				
 				return false;
 			}
 			
 		});
 		
 		//favText = (EditText) findViewById(R.id.fav_text1); 
 	}
 	
 	/*public void onAddButtonPush(View view) {
 		if (!FavouriteManager.inFavourites(getApplicationContext(), favText.getText().toString())) {
 			FavouriteManager.AddFavourite(favText.getText().toString(), getApplicationContext());
 			adapter.clear();
 			adapter.addAll(FavouriteManager.getFavourites(getApplicationContext()));
 			adapter.notifyDataSetChanged();
 		}
 		
 	}*/
 }
