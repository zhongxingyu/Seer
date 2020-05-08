 package csci498.bidixon.lunchlist;
 
 import java.util.*;
 import android.os.Bundle;
 import android.app.*;
 import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 
 public class LunchList extends TabActivity {
 	
 	List<Restaurant> restaurantsList = new ArrayList<Restaurant>();
	RestaurantAdapter restaurantAdapter = null;
	EditText name = null;
	EditText address = null;
	RadioGroup types = null;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_lunch_list);
         
         name = (EditText) findViewById(R.id.name);
		address = (AutoCompleteTextView) findViewById(R.id.addr);
 		types = (RadioGroup) findViewById(R.id.types);
 		
         Button saveButton = (Button) findViewById(R.id.save);
         saveButton.setOnClickListener(onSave);
         
         ListView list = (ListView) findViewById(R.id.restaurants);
         restaurantAdapter = new RestaurantAdapter();
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
 			Restaurant currentRestaurant = new Restaurant();
 			
 			currentRestaurant.setName(name.getText().toString());
 			currentRestaurant.setAddress(address.getText().toString());
 			currentRestaurant.setType(restaurantTypeFromRadioGroup(types));
 			
 			restaurantAdapter.add(currentRestaurant);
 		}
 	};
 	
 	private AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			Restaurant r = restaurantsList.get(position);
 			
 			name.setText(r.getName());
 			address.setText(r.getAddress());
 			
 			if (r.getType().equals("sit_down")){
 				types.check(R.id.sit_down);
 			} else if (r.getType().equals("take_out")){
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
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_lunch_list, menu);
         return true;
     }
     
     class RestaurantAdapter extends ArrayAdapter<Restaurant> {
 		RestaurantAdapter() {
 			super(LunchList.this, R.layout.row, restaurantsList);
 		}
 		
 		public View getView(int position, View convertView, ViewGroup parent){
 			View row = convertView;
 			RestaurantHolder holder = null;
 			
 			if (row == null){
 				LayoutInflater inflater = getLayoutInflater();
 				row = inflater.inflate(R.layout.row, parent, false);
 				
 				holder = new RestaurantHolder(row);
 				row.setTag(holder);
 			} else {
 				holder = (RestaurantHolder) row.getTag();
 			}
 			holder.populateFrom(restaurantsList.get(position));
 			
 			return row;
 		}
 	}
 	
 	static class RestaurantHolder {
		private TextView name = null;
		private TextView address = null;
		private ImageView icon = null;
 		
 		RestaurantHolder(View row) {
 			name = (TextView) row.findViewById(R.id.title);
 			address = (TextView) row.findViewById(R.id.address);
 			icon = (ImageView) row.findViewById(R.id.icon);
 		}
 		
 		void populateFrom(Restaurant restaurant){
 			name.setText(restaurant.getName());
 			address.setText(restaurant.getAddress());
 			
 			if (restaurant.getType().equals("sit_down")) {
 				name.setTextColor(Color.RED);
 				icon.setImageResource(R.drawable.ball_red);
 			} else if (restaurant.getType().equals("take_out")) {
 				name.setTextColor(Color.YELLOW);
 				icon.setImageResource(R.drawable.ball_yellow);
 			} else {
 				name.setTextColor(Color.GREEN);
 				icon.setImageResource(R.drawable.ball_green);
 			}
 		}
 	}
 }
