 package com.horeca;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class HorecaListActivity extends MyActivity {
 	GPSTracker gps;
	Horeca[] horecas;
 	
 	private Ville ville;
 	private HorecaType horecaType = null;
 	private PlatType platType = null;
 	private Ingredient ingredient = null;
 	
 	private TextView gps_warning = null;
 	private ListView horeca_list = null;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_horeca_list);
 		
 		// Open the db
 		MySqliteHelper sqliteHelper = new MySqliteHelper(this);
 		SQLiteDatabase db = sqliteHelper.getReadableDatabase();
 		
 		// Get the ville
 		Bundle b = getIntent().getExtras();
 		ville = new Ville(b.getLong(MainActivity.VILLE_ID_EXTRA), db);
 		if (b.containsKey(MainActivity.HORECATYPE_ID_EXTRA)) {
 			horecaType = new HorecaType(b.getLong(MainActivity.HORECATYPE_ID_EXTRA), db);
 		}
 		if (b.containsKey(MainActivity.PLATTYPE_ID_EXTRA)) {
 			platType = new PlatType(b.getLong(MainActivity.PLATTYPE_ID_EXTRA), db);
 		}
 		if (b.containsKey(MainActivity.INGREDIENT_NAME_EXTRA)) {
 			ingredient = new Ingredient(b.getString(MainActivity.INGREDIENT_NAME_EXTRA), db);
 		}
 		setTitle(String.format(getResources().getString(R.string.title_activity_horeca_list), ville.getName()));
 		
 		Filter filter = new Filter();
 		filter.setVille(ville);
 		if (horecaType != null) {
 			filter.setHorecaType(horecaType);
 		}
 		if (platType != null) {
 			filter.setPlatType(platType);
 		}
 		if (ingredient != null) {
 			filter.setIngredient(ingredient);
 		}
 		if (b.containsKey(MainActivity.PRICE_MIN_EXTRA)) {
 			filter.setMinPrice(b.getDouble(MainActivity.PRICE_MIN_EXTRA));
 		}
 		if (b.containsKey(MainActivity.PRICE_MAX_EXTRA)) {
 			filter.setMaxPrice(b.getDouble(MainActivity.PRICE_MAX_EXTRA));
 		}
 		if (b.containsKey(MainActivity.DISTANCE_MAX_EXTRA)) {
 			filter.setMaxDistance(b.getDouble(MainActivity.DISTANCE_MAX_EXTRA));
 		}
 		
 		gps = new GPSTracker(this);
 		gps_warning = (TextView) findViewById(R.id.gps_warning);
 		if (gps.canGetLocation()) {
 			gps_warning.setVisibility(View.GONE);
			horecas = filter.getMatchingHorecas(db, this, gps);
 		} else {
 			// make sure it is visible even if it should be
 			gps_warning.setVisibility(View.VISIBLE);
 			gps.showSettingsAlert();
			horecas = filter.getMatchingHorecas(db, this, null);
 		}
 
 		// Create the List of restaurants to choose from
 		/*SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, //this context
 				android.R.layout.simple_list_item_1, //id of the item layout used by default for the individual rows (this id is pre-defined by Android)
 				cursor,
 				new String[] { HorecaContract.Horeca.NAME },
 				new int[] { android.R.id.text1 });*/ // the list of objects to be adapted
 		// to remove deprecation warning, I need to add ", 0" but it is only in API 11 and we need 2.3.3 which is API 10
 		//db.close(); // too early, the adapter still uses it apparently
 		
 		horeca_list = (ListView) findViewById(R.id.horeca_list);
		horeca_list.setAdapter(new HorecaListArrayAdapter(this, horecas));
 		db.close();
 		
 		horeca_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
 			{
         		Intent i = new Intent(HorecaListActivity.this, HorecaActivity.class);
         		// Tell PlatListActivity which horeca to take plats from
        		i.putExtra("horeca_id", horecas[position].getId());
         		startActivity(i);
 			}
 		});
 	}
 	
 	public class HorecaListArrayAdapter extends ArrayAdapter<Horeca> {
 		public HorecaListArrayAdapter(Context context, Horeca[] horecas) {
 			super(context, R.layout.horeca_item, horecas);
 		}
 	 
 		@Override
 		public View getView(int i, View view, ViewGroup parent) {
 			if (view == null) {
 				LayoutInflater inflater = HorecaListActivity.this.getLayoutInflater();
 				view = inflater.inflate(R.layout.horeca_item, parent, false);
 				TextView name = (TextView) view.findViewById(R.id.horeca_item_name);
 				name.setText(horecas[i].getName());
 				TextView distance = (TextView) view.findViewById(R.id.horeca_item_distance);
 				if (gps.getLocation() != null) {
 					distance.setText(Utils.distanceToString(horecas[i].getDistance(gps)));
 				} else {
 					distance.setVisibility(View.GONE);
 				}
 			}
 			return view;
 		}
 	 
 	}
 
 }
