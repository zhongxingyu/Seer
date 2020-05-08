 package be.uclouvain.sinf1225.gourmet;
 
 import java.util.List;
 
 import be.uclouvain.sinf1225.gourmet.models.City;
 import be.uclouvain.sinf1225.gourmet.models.Dish;
 import be.uclouvain.sinf1225.gourmet.models.Restaurant;
 import be.uclouvain.sinf1225.gourmet.utils.GourmetUtils;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class DishListView extends Activity // implements GourmetLocationReceiver
 {
   private Restaurant restaurant = null;
 
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
 		setContentView(R.layout.activity_dish_list);
 
 		// Initialisation des services de localisation
 		// locationListener = new GourmetLocationListener(this,this).init();
 		// Récupération du restaurant sur lequel on a cliqué et les plats qui lui appartiennent
 		restaurant = Restaurant.getRestaurant(getIntent().getExtras().getInt("restoId"));
 
 		List<Dish> dishes = Dish.getDishInRestaurant(restaurant);
 		// On recupere la vue "liste"
 		ListView DishList = (ListView) this.findViewById(R.id.DishListView);
 
 		// On cree un adapter qui va mettre dans la liste les donnes adequates des villes
 		DishAdapter adapter = new DishAdapter(this, R.layout.dish_list_row, dishes);
 		DishList.setAdapter(adapter);
 		DishList.setOnItemClickListener(new OnItemClickListener()
 		{
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
 			{
 				final ListView DishList = (ListView) findViewById(R.id.DishListView);
 				final DishAdapter adapter = (DishAdapter) DishList.getAdapter();
 
 				Dish dish = adapter.getItem(position);
 
 				Intent intent = new Intent(DishListView.this, DishView.class);
				intent.putExtra("dishId", dish.getId());
 				startActivity(intent);
 			}
 		});
 		final Button button = (Button) findViewById(R.id.DishListRetour);
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
