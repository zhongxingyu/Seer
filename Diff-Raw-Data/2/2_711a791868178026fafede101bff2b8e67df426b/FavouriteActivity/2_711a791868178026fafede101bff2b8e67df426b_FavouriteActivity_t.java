 package com.engine9;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 
 public class FavouriteActivity extends Activity {
 
 	private ListView favList;
 	private  FavouriteAdapter adapter;
 	
 	protected void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_favourite);
		
 		
 		favList = (ListView) findViewById(R.id.listView1);
 		adapter = new FavouriteAdapter(getApplicationContext(), 
 				FavouriteManager.getFavourites(getApplicationContext()));
 		Log.d("DEBUG", String.valueOf(adapter.getCount()));
 		favList.setAdapter(adapter);
 		
 		final EditText favText = (EditText) findViewById(R.id.fav_text); 
 		Button favButton = (Button) findViewById(R.id.ffav_button);
 		favButton.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				FavouriteManager.AddFavourite(favText.getText().toString(), getApplicationContext());
 				adapter.clear();
 				adapter.addAll(FavouriteManager.getFavourites(getApplicationContext()));
 				adapter.notifyDataSetChanged();
 			}
 			
 		});
 	}
 }
