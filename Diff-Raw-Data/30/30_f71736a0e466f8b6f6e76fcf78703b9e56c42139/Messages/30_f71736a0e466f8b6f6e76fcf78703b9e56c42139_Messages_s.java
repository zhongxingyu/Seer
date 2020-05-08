 package com.nomad.nomadclient;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class Messages extends ListActivity{
 	ListView mListView;	//the listview for the message entries
 	MessageListAdapter mListAdapter; //the adapter for the listview
 	FoodTruck foodTruck; //the truck who's schedule is represented
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.messages);
 
 		//if the activity resumes and the global data has been garbage collected, finish()
 		if(((NomadClientApplication)this.getApplication()).getTrucks().size() == 0)
 			finish();
 
 
 		//if they click the back button, finish the activity
 		Button backButton = (Button)findViewById(R.id.backButton);
 		backButton.setOnClickListener(new View.OnClickListener(){
 			public void onClick(View v) {
 				finish();
 			}
 		});
 
 		//initialize the listview variable
 		mListView = this.getListView();
 
 		//fetch the foodtruck who's menu will be shown
 		int truckIndex = getIntent().getExtras().getInt("truckIndex");
 		foodTruck = ((NomadClientApplication)this.getApplication()).getTrucks().get(truckIndex); 
 
 
 		((TextView)findViewById(R.id.messagePrompt)).setText(foodTruck.name + "'s Recent Messages");
 
 		//set up the adapter
 		mListAdapter = new MessageListAdapter(foodTruck,this.getApplicationContext());
 		setListAdapter(mListAdapter);  
 
 
 	}
 
 	private class MessageListAdapter extends ArrayAdapter<MessageEntry>{
 		ArrayList<MessageEntry> messages;
 		Context c;
 
 		public MessageListAdapter(FoodTruck t, Context context){
 			super(context,t.messages.size());
 			messages = t.messages;
 			c = context;
 		}
 
 
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			LayoutInflater inflater = (LayoutInflater) c.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
 			View v = convertView;
 
 			final MessageEntry i = messages.get(position);
 
 
 			v = inflater.inflate(R.layout.messagerow, null);
 			final TextView dateTextView = (TextView)v.findViewById(R.id.messageRowTime);
 			final TextView messageTextView = (TextView)v.findViewById(R.id.messageRowMessage);
 

 			dateTextView.setText(i.timeCreated);
 			messageTextView.setText(i.message);
 
 			return v;
 		}
 
 		@Override 
 		public int getItemViewType(int position){
 			return 0;
 		}
 
 		@Override
 		public int getCount() {
 			return messages.size();
 		}
 
 		@Override
 		public MessageEntry getItem(int position) {
 			return messages.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 
 
 
 	}
 }
