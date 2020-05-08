 package csci498.laberle.lunchlist;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.app.Activity;
import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 
 public class LunchListActivity extends Activity {
 
 	List<Restaurant> restaurants = new ArrayList<Restaurant>();
 	RestaurantAdapter adapter = null;
 	ArrayAdapter<String> addressAdapter = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_lunch_list);
 
 		configureButton();
 		configureRestaurantList();
 		configureAddressField();
 	}
 
 
 	private void configureAddressField() {
 		List<String> addresses = new ArrayList<String>();
 		for (Restaurant r : restaurants) {
 			addresses.add(r.getAddress());
 		}
 
 		addressAdapter = new ArrayAdapter<String>(
 				this,
 				android.R.layout.simple_dropdown_item_1line,
 				addresses);
 
 		AutoCompleteTextView address = (AutoCompleteTextView)
 			findViewById(R.id.addr);
 		address.setAdapter(addressAdapter);
 	}
 
 	private void configureRestaurantList() {
 		/*Spinner spinner = (Spinner) findViewById(R.id.spinner);
 		restaurantAdapter = new ArrayAdapter<Restaurant>(
 				this, 
 				android.R.layout.simple_spinner_item, 
 				restaurants);
 		restaurantAdapter.setDropDownViewResource(
 				android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(restaurantAdapter);*/
 
 		ListView list = (ListView) findViewById(R.id.restaurants);
 		adapter = new RestaurantAdapter();
 		list.setAdapter(adapter);
 	}
 
 	private void addRadioButtons() {
 		RadioGroup types = (RadioGroup) findViewById(R.id.types);
 		RadioButton button = new RadioButton(this);
 		button.setText("Drive-Thru");
 		types.addView(button);
 		button = new RadioButton(this);
 		button.setText("Fancy Dining");
 		types.addView(button);
 		button = new RadioButton(this);
 		button.setText("Self-Serve");
 		types.addView(button);
 		button = new RadioButton(this);
 		button.setText("Drive In");
 		types.addView(button);
 		button = new RadioButton(this);
 		button.setText("Take and Bake");
 		types.addView(button);
 	}
 
 	private void configureButton() {
 		Button save = (Button) findViewById(R.id.save);
 		save.setOnClickListener(onSave);
 	}
 
 	private View.OnClickListener onSave = new OnClickListener() {
 		public void onClick(View v) {
 			Restaurant restaurant = new Restaurant();
 
 			EditText name = (EditText) findViewById(R.id.name);
 			EditText address = (EditText) findViewById(R.id.addr);
 			RadioGroup types = (RadioGroup) findViewById(R.id.types);
 
 			restaurant.setName(name.getText().toString());
 			restaurant.setAddress(address.getText().toString());
 			restaurant.setType(getTypeFromRadioButtons(types));
 
 			adapter.add(restaurant);
 			addressAdapter.add(restaurant.getAddress());
 		}
 	};
 
 	private RestaurantType getTypeFromRadioButtons(RadioGroup types) {
 		switch (types.getCheckedRadioButtonId()) {
 		case R.id.sit_down:
 			return RestaurantType.SIT_DOWN;
 		case R.id.take_out:
 			return RestaurantType.TAKE_OUT;
 		case R.id.delivery:
 			return RestaurantType.DELIVERY;
 		default:
 			return null;
 		}
 	}
 	
 	public class RestaurantAdapter extends ArrayAdapter<Restaurant> {
 		RestaurantAdapter() {
 			super(LunchListActivity.this,
 					R.layout.row, 
 					restaurants);
 		}		
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			return initializeRow(position, parent, convertView);
 		}
 
 		private View initializeRow(int position, ViewGroup parent, View row) {
 			RestaurantHolder holder;
 			if (row == null) {
 				LayoutInflater inflater = getLayoutInflater();
 				row = inflater.inflate(R.layout.row, parent, false);
 				holder = new RestaurantHolder(row);
 				row.setTag(holder);
 			}
 			else {
 				holder = (RestaurantHolder) row.getTag();
 			}
 
 			holder.populateFrom(restaurants.get(position));
 			return row;
 		}
 		
 	}
 
 	static class RestaurantHolder {
 		private TextView name = null;
 		private TextView address = null;
 		private ImageView icon = null;
 
 		RestaurantHolder(View row) {
 			name = (TextView)row.findViewById(R.id.title);
 			address = (TextView)row.findViewById(R.id.address);
 			icon = (ImageView)row.findViewById(R.id.icon);
 		}
 
 		void populateFrom(Restaurant r) {
 			name.setText(r.getName());
 			address.setText(r.getAddress());
 			switch (r.getType()) {
 			case SIT_DOWN:
 				icon.setImageResource(R.drawable.sit_down);
				name.setTextColor(Color.RED);
				break;
 			case TAKE_OUT:
 				icon.setImageResource(R.drawable.take_out);
				name.setTextColor(Color.YELLOW);
				break;
 			case DELIVERY:
 				icon.setImageResource(R.drawable.delivery);
				name.setTextColor(Color.GREEN);
				break;
 			}
 		}
 
 	}
 
 }
