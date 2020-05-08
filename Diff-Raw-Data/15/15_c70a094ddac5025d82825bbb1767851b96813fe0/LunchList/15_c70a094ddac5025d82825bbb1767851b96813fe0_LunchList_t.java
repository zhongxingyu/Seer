 package csci498.bidixon.lunchlist;
 
 import java.util.*;
 import java.util.concurrent.atomic.AtomicBoolean;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.app.*;
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.*;
 
 @SuppressWarnings("deprecation")
 public class LunchList extends TabActivity {
 	
 	RestaurantAdapter restaurantAdapter;
 	RestaurantHelper helper;
 	Cursor model;
 	Restaurant current = null;
 	EditText name;
 	EditText address;
 	EditText notes;
 	RadioGroup types;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_lunch_list);
         
         helper = new RestaurantHelper(this);
         name = (EditText) findViewById(R.id.name);
 		address = (EditText) findViewById(R.id.addr);
 		notes = (EditText) findViewById(R.id.notes);
 		types = (RadioGroup) findViewById(R.id.types);
 
         Button saveButton = (Button) findViewById(R.id.save);
         saveButton.setOnClickListener(onSave);
         
         ListView list = (ListView) findViewById(R.id.restaurants);
         model = helper.getAll();
         startManagingCursor(model);
         restaurantAdapter = new RestaurantAdapter(model);
         list.setAdapter(restaurantAdapter);
         
         TabHost.TabSpec spec = getTabHost().newTabSpec("tag1");
         
         spec.setContent(R.id.restaurants);
         spec.setIndicator("List", getResources().getDrawable(R.drawable.list));
         
         getTabHost().addTab(spec);
         
         spec = getTabHost().newTabSpec("tag2");
         spec.setContent(R.id.details);
         spec.setIndicator("Details", getResources().getDrawable(R.drawable.restaurant));
         
         getTabHost().addTab(spec);
         getTabHost().setCurrentTab(0);
         
         list.setOnItemClickListener(onListClick);
     }
     
     private View.OnClickListener onSave = new View.OnClickListener() {
 		public void onClick(View v) {
 			String type = restaurantTypeFromRadioGroup(types);
 			helper.insert(name.getText().toString(), address.getText().toString(), type, notes.getText().toString());
 		}
 	};
 	
 	private AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			model.moveToPosition(position);
 			
			name.setText(helper.getName(model));
			address.setText(helper.getAddress(model));
			notes.setText(helper.getNotes(model));
 			
			if (helper.getType(model).equals(R.string.sit_down)){
 				types.check(R.id.sit_down);
			} else if (helper.getType(model).equals(R.string.take_out)){
 				types.check(R.id.take_out);
 			} else {
 				types.check(R.id.delivery);
 			}
 			
 			getTabHost().setCurrentTab(1);
 		}
 	};
 	
 	private String restaurantTypeFromRadioGroup(RadioGroup group){
 		if (group == null){
 			return "";
 		}
 		
 		switch (group.getCheckedRadioButtonId()){
 			case R.id.sit_down:
 				return "sit_down";
 			case R.id.take_out:
 				return "take_out";
 			case R.id.delivery:
 				return "delivery";
 		}
 		return "";
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		helper.close();
 	}
 
     class RestaurantAdapter extends CursorAdapter {
 		RestaurantAdapter(Cursor c) {
 			super(LunchList.this, c);
 		}
 
 		@Override
 		public void bindView(View row, Context ctxt, Cursor c) {
 			RestaurantHolder holder = (RestaurantHolder) row.getTag();
 			holder.populateFrom(c, helper);
 		}
 
 		@Override
 		public View newView(Context ctxt, Cursor c, ViewGroup parent) {
 			LayoutInflater inflater = getLayoutInflater();
 			View row = inflater.inflate(R.layout.row, parent, false);
 			RestaurantHolder holder = new RestaurantHolder(row);
 			
 			row.setTag(holder);
 			return row;
 		}
 	}
 	
 	static class RestaurantHolder {
 		private TextView name;
 		private TextView address;
 		private ImageView icon;
 		
 		RestaurantHolder(View row) {
 			name = (TextView) row.findViewById(R.id.title);
 			address = (TextView) row.findViewById(R.id.address);
 			icon = (ImageView) row.findViewById(R.id.icon);
 		}
 		
 		void populateFrom(Cursor c, RestaurantHelper helper){
 			name.setText(helper.getName(c));
 			address.setText(helper.getAddress(c));
 			
 			if (helper.getType(c).equals(R.string.sit_down)) {
 				name.setTextColor(Color.RED);
 				icon.setImageResource(R.drawable.ball_red);
 			} else if (helper.getType(c).equals(R.string.take_out)) {
 				name.setTextColor(Color.YELLOW);
 				icon.setImageResource(R.drawable.ball_yellow);
 			} else {
 				name.setTextColor(Color.GREEN);
 				icon.setImageResource(R.drawable.ball_green);
 			}
 		}
 	}
 }
