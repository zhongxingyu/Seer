 package csci498.tthrailk.lunchlistfix;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.R.color;
 import android.app.TabActivity;
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.widget.CursorAdapter;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RadioGroup;
 import android.widget.TabHost;
 import android.widget.TextView;
 
 public class LunchList extends TabActivity {
 	
 	Cursor restaurantList;
 	RestaurantAdapter adapter;
 	//AutoCompleteTextView oRestaurantsAddresses;
 	RadioGroup types;
 	EditText name;
 	EditText address;
 	EditText notes;
	Restaurant current;
 	RestaurantHelper helper;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         helper 		= new RestaurantHelper(this);
         types 		= (RadioGroup) 	findViewById(R.id.types);
         name		= (EditText) 	findViewById(R.id.name);
         address		= (EditText) 	findViewById(R.id.addr);
         notes 		= (EditText) 	findViewById(R.id.notes);
         Button save = (Button) 		findViewById(R.id.save);
         
         save.setOnClickListener(onSave);
         
         createListView();
         createTabs();
         
     }
     
     @Override
     public void onDestroy() {
     	super.onDestroy();
     	helper.close();
     }
     
     private void createListView() {
     	ListView list 	= (ListView) findViewById(R.id.restaurants);
     	restaurantList = helper.getAll();
     	startManagingCursor(restaurantList);
     	adapter 		= new RestaurantAdapter(restaurantList);
     	list.setAdapter(adapter);
     	list.setOnItemClickListener(onListClick);
     }
     
     private void createTabs() {
     	TabHost.TabSpec tSpec = getTabHost().newTabSpec("tag1");
     	tSpec.setContent(R.id.restaurants);
     	tSpec.setIndicator("List", getResources().getDrawable(R.drawable.list));
     	getTabHost().addTab(tSpec);
     	
     	tSpec = getTabHost().newTabSpec("tag2");
     	tSpec.setContent(R.id.details);
     	tSpec.setIndicator("Details", getResources().getDrawable(R.drawable.restaurant));
     	
     	getTabHost().addTab(tSpec);
     	getTabHost().setCurrentTab(1);
     }
     
     private View.OnClickListener onSave = new View.OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			String type = null;
 			
 			switch (types.getCheckedRadioButtonId()) {
 				case R.id.sit_down:
					current.setType("sit_down");
 					break;
 				case R.id.take_out:
					current.setType("take_out");
 					break;
 				case R.id.delivery:
					current.setType("delivery");
 					break;
 			}
 			
 			helper.insert(name.getText().toString(), address.getText().toString(), type, notes.getText().toString());
 		}
 	};
 
 	private AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			
 			restaurantList.moveToPosition(position);
 			name.setText(helper.getName(restaurantList));
 			address.setText(helper.getAddress(restaurantList));
 			notes.setText(helper.getNotes(restaurantList));
 			
			if (current.getType().equals("sit_down")) {
 				types.check(R.id.sit_down);
 			}
			else if (current.getType().equals("take_out")) {
 				types.check(R.id.take_out);
 			}
 			else {
 				types.check(R.id.delivery);
 			}
 			getTabHost().setCurrentTab(1);
 		}
 	};
 
 	class RestaurantAdapter extends CursorAdapter {
 		
 		RestaurantAdapter(Cursor c) {
 			super(LunchList.this, c);
 		}
 		
 		@Override
 		public void bindView(View row, Context context, Cursor c) {
 			RestaurantHolder holder = (RestaurantHolder) row.getTag();
 		}
 		
 		@Override 
 		public View newView(Context context, Cursor c, ViewGroup parent) {
 			LayoutInflater inflater = getLayoutInflater();
 			View row = inflater.inflate(R.layout.row, parent, false);
 			RestaurantHolder holder = new RestaurantHolder(row);
 			row.setTag(holder);
 			return row;
 		}
 	}
 	
 	static class RestaurantHolder {
 		private TextView name 		= null;
 		private TextView address 	= null;
 		private ImageView icon 		= null;
 		
 		RestaurantHolder(View row) {
 			name 	=	(TextView) row.findViewById(R.id.title);
 			address =	(TextView) row.findViewById(R.id.address);
 			icon 	= 	(ImageView) row.findViewById(R.id.icon);
 		}
 		
 		void populateFrom(Cursor c, RestaurantHelper helper) {
 			name.setText(helper.getName(c));
 			address.setText(helper.getAddress(c));
 			
 			if (helper.getType(c).equals("sit_down")) {
 				icon.setImageResource(R.drawable.ball_red);
 			}
 			else if (helper.getType(c).equals("take_out")) {
 				icon.setImageResource(R.drawable.ball_yellow);
 			}
 			else {
 				icon.setImageResource(R.drawable.ball_green);
 			}
 		}
 	}
 }
 	
