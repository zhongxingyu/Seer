 package com.frienemy.activities;
 
 import java.util.ArrayList;
 
 import com.frienemy.adapters.FriendAdapter;
 import com.frienemy.models.Friend;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class EnemyActivity extends ListActivity implements OnClickListener { 
 
 	private static final String TAG = EnemyActivity.class.getSimpleName();
 	FriendAdapter adapter;
 	ListView list;
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
         super.onCreate(savedInstanceState);
         setContentView(R.layout.frienemy);
         TextView v = (TextView) findViewById(R.id.title);
        v.setText("Frienemies");
         updateView();
         setUpListeners();
 	}
 	
 	private void setUpListeners() {
     	 View v;
  		v = findViewById( R.id.btnFriends );
  		v.setBackgroundResource( R.drawable.button_selector );
  		v.setOnClickListener( this );
 
  		v = findViewById( R.id.btnFrienemies );
 		v.setBackgroundResource( R.drawable.gray_gradient);
  		v.setOnClickListener( this );
 
  		v = findViewById( R.id.stalkers );
  		v.setBackgroundResource( R.drawable.button_selector );
  		v.setOnClickListener( this );
 	}
 	
     protected void updateView() {
     	try{
 		ArrayList<Friend> friends = Friend.query(this, Friend.class, null, "frienemyStatus==1 AND isCurrentUser==0", "name ASC");
 		
 		list=(ListView)findViewById(android.R.id.list);
 		adapter=new FriendAdapter(this, friends);
         list.setAdapter(adapter);
         adapter.notifyDataSetChanged();
 		Log.i(TAG, "Friends count: " + friends.size());
 		}catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 
 	}
 
     public void onClick(View v) {
     	Intent i;
 		switch (v.getId())
 		{
 			case R.id.btnFriends:
 				i = new Intent(EnemyActivity.this, FrienemyActivity.class);
 				startActivity(i);
 				break;
 			case R.id.btnFrienemies:
 				
 				break;
 			case R.id.stalkers:
 				i = new Intent(EnemyActivity.this, StalkerActivity.class);
 				startActivity(i);
 				break;
 			//case R.id.postbutton:
 			//	post();
 				//break;
 			default:
 		
 	}
 }
 }
