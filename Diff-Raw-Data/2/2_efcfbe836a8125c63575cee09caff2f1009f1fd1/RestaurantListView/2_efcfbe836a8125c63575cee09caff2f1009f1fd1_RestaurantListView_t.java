 package be.uclouvain.sinf1225.gourmet;
 
import java.util.List;

 import be.uclouvain.sinf1225.gourmet.models.City;
 import be.uclouvain.sinf1225.gourmet.models.Restaurant;
 import be.uclouvain.sinf1225.gourmet.utils.GourmetLocationListener;
 import be.uclouvain.sinf1225.gourmet.utils.GourmetLocationReceiver;
 import be.uclouvain.sinf1225.gourmet.utils.GourmetUtils;
 import android.app.Activity;
 import android.content.Intent;
 import android.location.Location;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class RestaurantListView extends Activity //implements GourmetLocationReceiver
 {
 
 	private City city = null;
 	
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 	    GourmetUtils.createMenu(menu, this, R.id.search);
 	    return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		return GourmetUtils.onMenuItemSelected(item, this);
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_restaurant_list);
 		
 		// Initialisation des services de localisation
 		//locationListener = new GourmetLocationListener(this,this).init();
 		// Récupération de la ville sur laquelle on a cliqué et les restaurant qui lui appartiennent		
 		city = City.getCity(getIntent().getExtras().getString("name"), getIntent().getExtras().getString("country"));
 		
 		List<Restaurant> restaurants = Restaurant.getAllRestaurants(city);
 		((TextView)findViewById(R.id.Test)).setText((restaurants.get(0)).getName());
 		//On recupere la vue "liste"
 		ListView RestaurantList = (ListView) this.findViewById(R.id.RestaurantListView);
 	
 		//On cree un adapter qui va mettre dans la liste les donnes adequates des villes
 		RestaurantAdapter adapter = new RestaurantAdapter(this, R.layout.restaurant_list_row, restaurants);
 		RestaurantList.setAdapter(adapter);
 		RestaurantList.setOnItemClickListener(new OnItemClickListener()
 		{
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
 			{
 				final ListView RestaurantList = (ListView) findViewById(R.id.RestaurantListView);
 				final RestaurantAdapter adapter = (RestaurantAdapter)RestaurantList.getAdapter();
 
 				Restaurant restaurant = adapter.getItem(position);
 					
 				Intent intent = new Intent(RestaurantListView.this, RestaurantView.class);
 			    intent.putExtra("name", restaurant.getName());
 			    startActivity(intent);
 			}
 		});
 		final Button button = (Button)findViewById(R.id.RestaurantListRetour);
 		button.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View arg0)
 			{
 				finish();
 			}
 		});
 	}
 
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 	}
 	
 	@Override
 	public void onStop()
 	{
 		super.onStop();
 	}
 	
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 	}
 	
 }
