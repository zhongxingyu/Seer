 package edu.mines.csci498.bwisdom.lunchlist;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.app.Activity;
 import android.app.TabActivity;
 import android.content.Context;
 import android.database.Cursor;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.Adapter;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CursorAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RadioGroup;
 import android.widget.TabHost;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends TabActivity {
 
 	Restaurant r = new Restaurant();
 	Restaurant current;
 	Cursor model;
 	RestaurantAdapter adapter; 
 	RestaurantHelper helper;
 	EditText name;
 	EditText address;
 	RadioGroup types;
 	EditText notes;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_PROGRESS);
 		setContentView(R.layout.activity_main);
 
 		name = (EditText)findViewById(R.id.name);
 		address = (EditText)findViewById(R.id.addr);
 		types = (RadioGroup)findViewById(R.id.types);
 		notes = (EditText)findViewById(R.id.notes);
 		
 		Button save = (Button) findViewById(R.id.save);
 
 		save.setOnClickListener(onSave);
 		
 		ListView list = (ListView) findViewById(R.id.restaurants);
 		
 		model = helper.getAll();
 		startManagingCursor(model);
 		
 		adapter = new RestaurantAdapter(model);
 		list.setAdapter(adapter);
 		
 		TabHost.TabSpec spec = getTabHost().newTabSpec("tag1");
 		
 		spec.setContent(R.id.restaurants);
 		spec.setIndicator("List", getResources().getDrawable(R.drawable.list));
 		getTabHost().addTab(spec);
 		
 		spec = getTabHost().newTabSpec("tag2");
 		spec.setContent(R.id.details);
 		
 		spec.setIndicator("Details", getResources().getDrawable(R.drawable.restaurant));
 		getTabHost().addTab(spec);
 		
 		getTabHost().setCurrentTab(0);
 		
 		list.setOnItemClickListener( onListClick);
 		
		helper = new RestaurantHelper(this);
 	}
 
 	private View.OnClickListener onSave = new View.OnClickListener() {
 		public void onClick(View v) {
			EditText name = (EditText) findViewById(R.id.name);
			EditText address = (EditText) findViewById(R.id.addr);
			EditText notes = (EditText) findViewById(R.id.notes);
			String type = null; 
			current.setName(name.getText().toString());
			current.setAddress(address.getText().toString());
			current.setNotes(notes.getText().toString());
 			
			RadioGroup types = (RadioGroup) findViewById(R.id.types);
 			
 			switch(types.getCheckedRadioButtonId()){
 				case R.id.sit_down:
 					type = "sit_down";
 					break;
 				case R.id.takeout:
 					type = "takeout";
 					break;
 				case R.id.delivery:
 					type = "delivery";
 					break;
 			}	
 			helper.insert(name.getText().toString(), address.getText()
 					.toString(), type, notes.getText().toString());
 		}
 	};
 	
 	private AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			
 			model.moveToPosition(position);
 			
 			name.setText(helper.getName(model));
 			address.setText(helper.getAddress(model));
 			notes.setText(helper.getNotes(model));
 			
 			if(helper.getType(model).equals("sit_down")){
 				types.check(R.id.sit_down);
 			}else if(helper.getType(model).equals("takeout")){
 				types.check(R.id.takeout);
 			}else{
 				types.check(R.id.delivery);
 			}
 			getTabHost().setCurrentTab(1);
 
 		}
 	};
 	
 	class RestaurantAdapter extends CursorAdapter {
 		RestaurantAdapter(Cursor c){
 			super(MainActivity.this, c);
 		}
 		
 		@Override
 		public void bindView(View row, Context ctxt, Cursor c) {
 
 			RestaurantHolder holder = (RestaurantHolder) row.getTag();
 
 			holder.populateFrom(c, helper);
 		}
 		
 		@Override
 		public View newView(Context ctxt, Cursor c, ViewGroup parent){
 			
 			LayoutInflater inflater = getLayoutInflater();
 			View row = inflater.inflate(R.layout.row, parent, false);
 			
 			RestaurantHolder holder = new RestaurantHolder(row);
 			
 			row.setTag(holder);
 			
 			return(row);
 		}
 	}
 	
 	static class RestaurantHolder{
 		private TextView name = null;
 		private TextView address = null;
 		private ImageView icon = null; 
 		
 		RestaurantHolder(View row) {
 			name = (TextView) row.findViewById(R.id.title);
 			address = (TextView) row.findViewById(R.id.address);
 			icon = (ImageView) row.findViewById(R.id.icon);
 		}
 		
 		void populateFrom(Cursor c, RestaurantHelper helper) {
 			name.setText(helper.getName(c));
 			address.setText(helper.getAddress(c));
 
 			if (helper.getType(c).equals("sit_down")) {
 				icon.setImageResource(R.drawable.sit_down);
 			} else if (helper.getType(c).equals("takeout")) {
 				icon.setImageResource(R.drawable.takeout);
 			} else {
 				icon.setImageResource(R.drawable.delivery);
 			}
 		}		
 	}
 	
 	@Override 
 	public void onDestroy(){
 		super.onDestroy();
 		
 		helper.close();
 	}
 }
