 package be.uclouvain.sinf1225.gourmet;
 
 import java.util.List;
 
 import be.uclouvain.sinf1225.gourmet.utils.GourmetDatabase;
 import be.uclouvain.sinf1225.gourmet.utils.GourmetLocationListener;
 import be.uclouvain.sinf1225.gourmet.utils.GourmetLocationReceiver;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.location.Location;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.Spinner;
 import android.widget.ToggleButton;
 
 public class CityListActivity extends Activity implements GourmetLocationReceiver
 {
 	GourmetLocationListener locationListener = null;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		
 		// Initialisation des services de localisation
 		locationListener = new GourmetLocationListener(this,this).init();
 		
 		//On dfini la vue en cours
 		setContentView(R.layout.activity_city_list);
 		
 		//Recuperons les villes.
 		GourmetDatabase db = new GourmetDatabase(this);
 		List<City> cities = db.getAllCities();
 		db.close();
 		
 		//On recupere les boutons pour le tri
 		final Spinner sortType = (Spinner) this.findViewById(R.id.CityListSort);
 		final RadioGroup sortDirection = (RadioGroup) findViewById(R.id.CityListSortDirection);
 		
 		//On rcupre la vue "liste"
 		final ListView cityList = (ListView) this.findViewById(R.id.CityListView);
 		//On cre un adapter qui va mettre dans la liste les donnes adequates des villes
 		CityAdapter adapter = new CityAdapter(this, R.layout.city_list_row, cities, locationListener.getLastLocation());
 		if(locationListener.getLastLocation() == null) //si on a pas de position GPS
 		{
 			adapter.setSort("name"); //on trie par nom
 			sortType.setSelection(0); //le premier est le nom
 			sortDirection.check(R.id.CityListSortDirectionAsc); //asc
 		}
 		else //si on en a une
 		{
			adapter.setSort("distance", false); //on trie par distance
 			sortType.setSelection(1); //le second est la distance
 			sortDirection.check(R.id.CityListSortDirectionAsc); //desc
 		}
 		cityList.setAdapter(adapter);
 		cityList.setTextFilterEnabled(true);
 		cityList.setOnItemClickListener(new OnItemClickListener()
 		{
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
 			{
 				final ListView cityList = (ListView) findViewById(R.id.CityListView);
 				final CityAdapter adapter = (CityAdapter)cityList.getAdapter();
 				
 			    City city = adapter.getItem(position);
 			    
 			    //On change d'activit
 			    Intent intent = new Intent(CityListActivity.this, RestaurantListActivity.class);
 			    intent.putExtra("name",city.getName());
 			    intent.putExtra("country", city.getCountry());
 			    startActivityForResult(intent, 0);
 			}
 		});
 		
 		//Les filtres
 		final EditText filter = (EditText) this.findViewById(R.id.CityListFilter);
 		filter.addTextChangedListener(new TextWatcher()
 		{
 			@Override
 			public void afterTextChanged(Editable s) { }
 			@Override
 			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count)
 			{
 				final ListView cityList = (ListView) findViewById(R.id.CityListView);
 				final CityAdapter adapter = (CityAdapter)cityList.getAdapter();
 				adapter.getFilter().filter(s);
 			}
 		});
 		
 		final ToggleButton sortActivate = (ToggleButton) this.findViewById(R.id.CityListSortActivate);
 		sortActivate.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener()
 		{
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
 			{
 				final LinearLayout layout = (LinearLayout) findViewById(R.id.CityListSortContainer);
 				layout.setVisibility(isChecked ? ToggleButton.VISIBLE : ToggleButton.GONE);
 			}
 		});
 		
 		sortType.setOnItemSelectedListener(new OnItemSelectedListener()
 		{
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
 			{
 				final ListView cityList = (ListView) findViewById(R.id.CityListView);
 				final CityAdapter adapter = (CityAdapter)cityList.getAdapter();
 				if(pos == 0) adapter.setSort("name");
 				else if(pos == 1) adapter.setSort("distance");
 				else if(pos == 2) adapter.setSort("restaurants");
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) { }
 		});
 		
 		sortDirection.setOnCheckedChangeListener(new OnCheckedChangeListener()
 		{
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId)
 			{
 				final ListView cityList = (ListView) findViewById(R.id.CityListView);
 				final CityAdapter adapter = (CityAdapter)cityList.getAdapter();
 				if(checkedId == R.id.CityListSortDirectionAsc) adapter.setSortOrder(true);
 				else if(checkedId == R.id.CityListSortDirectionDesc) adapter.setSortOrder(false);
 			}
 		});
 	}
 
 	@Override
 	protected void onStop()
 	{
 		locationListener.close();
 		locationListener = null;
 		super.onStop();
 	}
 	
 	@Override
 	protected void onRestart()
 	{
 		if(locationListener == null)
 			locationListener = new GourmetLocationListener(this,this);
 		super.onRestart();
 	}
 	
 	@Override
 	public void onLocationUpdate(Location loc)
 	{
 		final ListView cityList = (ListView) this.findViewById(R.id.CityListView);
 		CityAdapter a = (CityAdapter)cityList.getAdapter();
 		a.updateLocation(loc);
 	}
 }
