 package csci498.thevoiceless.lunchlist;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.TabActivity;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TabHost;
 import android.widget.TextView;
 
 public class LunchList extends TabActivity
 {
 	// Tab ID values
 	private static final int LIST_TAB_ID = 0;
 	private static final int DETAILS_TAB_ID = 1;
 	// ArrayList of restaurants and its associated adapter
 	List<Restaurant> restaurants = new ArrayList<Restaurant>();
 	RestaurantAdapter restaurantsAdapter = null;
 	// Data members from the view
 	DatePicker visited = null;
 	EditText name = null;
 	EditText address = null;
 	RadioGroup typeGroup = null;
 	EditText notes = null;
 	Button save = null;
 	ListView list = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_lunch_list);
 		
 		setDataMembers();
 		//setFonts();
 		//addMoreRadioButtons();
 		setListeners();
 		setAdapters();
 		createTabs();
 	}
 	
 	private View.OnClickListener onSave = new View.OnClickListener()
 	{
 		public void onClick(View v)
 		{
 			// TODO: Put this in a try/catch and show errors in a toast
 			Restaurant r = new Restaurant();
 			r.setName(name.getText().toString());
 			r.setAddress(address.getText().toString());
			r.setDateVisited(visited.getMonth(), visited.getDayOfMonth(), visited.getYear());
 			setRestaurantType(r);
 			restaurantsAdapter.add(r);
 		}
 	};
 	
 	private AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener()
 	{
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
 		{
 			Restaurant r = restaurants.get(position);
 			
 			visited.updateDate(r.getDateVisited().getYear(), r.getDateVisited().getMonth(), r.getDateVisited().getDate());
 			name.setText(r.getName());
 			address.setText(r.getAddress());
 			
 			if(r.getType().equals(Restaurant.Type.SIT_DOWN))
 			{
 				typeGroup.check(R.id.sitdownRadio);
 			}
 			else if(r.getType().equals(Restaurant.Type.TAKE_OUT))
 			{
 				typeGroup.check(R.id.takeoutRadio);
 			}
 			else
 			{
 				typeGroup.check(R.id.deliveryRadio);
 			}
 			
 			getTabHost().setCurrentTab(DETAILS_TAB_ID);
 		}
 	};
 	
 	class RestaurantAdapter extends ArrayAdapter<Restaurant>
 	{
 		RestaurantAdapter()
 		{
 			super(LunchList.this, android.R.layout.simple_list_item_1, restaurants);
 		}
 		
 		// I don't really know what's going on here
 		public View getView(int position, View convertView, ViewGroup parent)
 		{
 			View row = convertView;
 			RestaurantHolder holder = null;
 			
 			if(row == null)
 			{
 				LayoutInflater inflater = getLayoutInflater();
 				row = inflater.inflate(R.layout.row, null);
 				
 				holder = new RestaurantHolder(row);
 				row.setTag(holder);
 			}
 			else
 			{
 				holder = (RestaurantHolder) row.getTag();
 			}
 			
 			holder.populateForm(restaurants.get(position));
 			
 			return row;
 		}
 	}
 	
 	static class RestaurantHolder
 	{
 		private TextView rName = null;
 		private TextView rAddress = null;
 		private ImageView rIcon = null;
 		
 		RestaurantHolder(View row)
 		{
			rName = (TextView) row.findViewById(R.id.title);
			rAddress = (TextView) row.findViewById(R.id.address);
			rIcon = (ImageView) row.findViewById(R.id.icon);
 		}
 		
 		void populateForm(Restaurant r)
 		{
 			rName.setText(r.getName());
 			rAddress.setText(r.getAddress());
 			
 			if(r.getType().equals(Restaurant.Type.SIT_DOWN))
 			{
 				rIcon.setImageResource(R.drawable.green_circle);
 				rName.setTextColor(Color.rgb(0, 153, 0));
 			}
 			else if(r.getType().equals(Restaurant.Type.TAKE_OUT))
 			{
 				rIcon.setImageResource(R.drawable.blue_circle);
 				rName.setTextColor(Color.BLUE);
 			}
 			else
 			{
 				rIcon.setImageResource(R.drawable.lightblue_circle);
 				rName.setTextColor(Color.rgb(56, 178, 206));
 			}
 		}
 	}
 	
 	private void setDataMembers()
 	{
 		visited 	= (DatePicker) findViewById(R.id.visited);
 		name 		= (EditText) findViewById(R.id.name);
 		address 	= (EditText) findViewById(R.id.addr);
 		typeGroup 	= (RadioGroup) findViewById(R.id.typeGroup);
 		notes 		= (EditText) findViewById(R.id.notes);
 		save 		= (Button) findViewById(R.id.save);
 		list 		= (ListView) findViewById(R.id.restaurantsList);
 	}
 	
 	private void setListeners()
 	{
 		save.setOnClickListener(onSave);
 		list.setOnItemClickListener(onListClick);
 	}
 	
 	private void setFonts()
 	{
 		// TODO: See if it's possible to do all of this via iteration
 		
 		TextView nameLabel = (TextView) findViewById(R.id.nameLabel);
 		nameLabel.setTypeface(Typeface.createFromAsset(getAssets(), "Clemente-Bold.ttf"));
 		TextView addrLabel = (TextView) findViewById(R.id.addrLabel);
 		addrLabel.setTypeface(Typeface.createFromAsset(getAssets(), "Clemente-Bold.ttf"));
 		
 		name.setTypeface(Typeface.createFromAsset(getAssets(), "CaviarDreams.ttf"));
 		address.setTypeface(Typeface.createFromAsset(getAssets(), "CaviarDreams.ttf"));
 		
 		TextView typeLabel = (TextView) findViewById(R.id.typeLabel);
 		typeLabel.setTypeface(Typeface.createFromAsset(getAssets(), "Clemente-Bold.ttf"));
 		
 		// TODO: Figure out how to set the activity title font
 	}
 	
 	private void setRestaurantType(Restaurant r)
 	{
 		switch (typeGroup.getCheckedRadioButtonId())
 		{
 			case R.id.sitdownRadio:
 				r.setType(Restaurant.Type.SIT_DOWN);
 				break;
 			case R.id.takeoutRadio:
 				r.setType(Restaurant.Type.TAKE_OUT);
 				break;
 			case R.id.deliveryRadio:
 				r.setType(Restaurant.Type.DELIVERY);
 				break;
 		}
 	}
 	
 	/*
 	 * Add 10 more radio buttons to the "Type" button group
 	 * This was to practice adding radio buttons via code instead of XML
 	 */
 	private void addMoreRadioButtons()
 	{
 		for (int i = 0; i < 10; ++i)
 		{
 			RadioButton r = new RadioButton(this);
 			r.setText("Test" + i);
 			typeGroup.addView(r);
 		}
 	}
 	
 	private void setAdapters()
 	{
 		restaurantsAdapter = new RestaurantAdapter();
 		list.setAdapter(restaurantsAdapter);
 	}
 	
 	private void createTabs()
 	{
 		// https://developer.android.com/reference/android/widget/TabHost.TabSpec.html
 		// getTabHost() returns the TabHost that the activity is using to host its tabs
 		// TabHost holds two children: a set of tab labels that the user clicks to select a specific tab, and a FrameLayout object that displays the contents of that page
 		TabHost.TabSpec tab = getTabHost().newTabSpec("tag1");
 		
 		tab.setContent(R.id.restaurantsList);
 		tab.setIndicator("List", getResources().getDrawable(R.drawable.list));
 		getTabHost().addTab(tab);
 		
 		tab = getTabHost().newTabSpec("tag2");
 		tab.setContent(R.id.details);
 		tab.setIndicator("Details", getResources().getDrawable(R.drawable.restaurant));
 		getTabHost().addTab(tab);
 		
 		getTabHost().setCurrentTab(LIST_TAB_ID);
 		
 		// TODO: Figure out how to hide the keyboard when switching to the "list" tab
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		getMenuInflater().inflate(R.menu.activity_lunch_list, menu);
 		return true;
 	}
 }
