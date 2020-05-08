 package be.uclouvain.sinf1225.gourmet;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.MenuItem;
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
 import android.widget.Toast;
 import android.widget.ToggleButton;
 import be.uclouvain.sinf1225.gourmet.models.Dish;
 import be.uclouvain.sinf1225.gourmet.models.Preference;
 import be.uclouvain.sinf1225.gourmet.models.Restaurant;
 import be.uclouvain.sinf1225.gourmet.models.User;
 import be.uclouvain.sinf1225.gourmet.utils.GourmetUtils;
 
 public class DishListView extends Activity
 {
 	private Restaurant restaurant = null;
 	ListView DishListEntree;
 
 	/* true if the ReservationCreateView launches the activity */
 	/* false otherwise */
 	private boolean goToReservation;
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		GourmetUtils.createMenu(menu, this, R.id.search);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		return GourmetUtils.onMenuItemSelected(item, this);
 	}
 
 	public void onLetsGo()
 	{
 		setContentView(R.layout.activity_dish_list);
 
 		// Initialisation des services de localisation
 		// locationListener = new GourmetLocationListener(this,this).init();
 		// Récupération du restaurant sur lequel on a cliqué et les plats qui lui appartiennent
 		restaurant = Restaurant.getRestaurant(getIntent().getExtras().getInt("restoId"));
 
 		goToReservation = getIntent().hasExtra("key");
 
 		List<Dish> dishes = restaurant.getDishes();
 		Preference prefs = Preference.getPrefByUserEmail(User.getUserConnected().getEmail());
 		if (prefs != null)
 		{
 			String[] allergens = prefs.getAllergens();
 			List<Dish> finalDishes = new ArrayList<Dish>();
 			for (Dish dish : dishes)
 			{
 				String[] dish_allergens = dish.getAllergens();
				boolean ok = prefs.isVegeterian() && !dish.getVegan();
 				for (int i = 0; i < dish_allergens.length && ok; i++)
 				{
 					for (int j = 0; j < allergens.length && ok; j++)
 					{
 						if (dish_allergens[i].equals(allergens[j]))
 							ok = false;
 					}
 				}
 				if (ok)
 					finalDishes.add(dish);
 			}
 			dishes = finalDishes;
 		}
 
 		// On recupere les boutons pour le tri
 		final Spinner sortType = (Spinner) findViewById(R.id.DishListSort);
 		final RadioGroup sortDirection = (RadioGroup) findViewById(R.id.DishListSortDirection);
 
 		// On recupere la vue "liste"
 		DishListEntree = (ListView) this.findViewById(R.id.DishListView);
 
 		// On cree un adapter qui va mettre dans la liste les donnes adequates des plats
 		DishAdapter adapter = new DishAdapter(this, R.layout.dish_list_row, dishes);
 		adapter.setSort("menu"); // on trie par nom
 		sortType.setSelection(0); // le premier est le nom
 		sortDirection.check(R.id.DishListSortDirectionAsc); // asc
 		DishListEntree.setAdapter(adapter);
 		DishListEntree.setOnItemClickListener(new OnItemClickListener()
 		// MARKER
 				{
 					@Override
 					public void onItemClick(AdapterView<?> parent, View view, int position, long id)
 					{
 						final ListView DishList = (ListView) findViewById(R.id.DishListView);
 						// MARKER
 						final DishAdapter adapter = (DishAdapter) DishList.getAdapter();
 
 						Dish dish = adapter.getItem(position);
 
 						if (goToReservation)
 						{
 							if (dish.getAvailable() > 0)
 							{
 								/* decrement the values of available */
 								dish.setAvailable(dish.getAvailable() - 1);
 								dish.updateDish();
 
 								Intent intent = new Intent(DishListView.this, ReservationCreateView.class);
 								intent.putExtra("dishId", dish.getDishId());
 								setResult(RESULT_OK, intent);
 								finish();
 							}
 							else
 							{
 								Toast.makeText(getApplicationContext(), "plat puis", Toast.LENGTH_SHORT).show();
 							}
 						}
 						else
 						{
 							Intent intent = new Intent(DishListView.this, DishView.class);
 							intent.putExtra("dishId", dish.getDishId());
 							startActivity(intent);
 						}
 					}
 				});
 
 		// Les filtres
 		final EditText filter = (EditText) findViewById(R.id.DishListFilter);
 		filter.addTextChangedListener(new TextWatcher()
 		{
 			@Override
 			public void afterTextChanged(Editable s)
 			{
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
 			{
 			}
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count)
 			{
 				final ListView dishList = (ListView) findViewById(R.id.DishListView);
 				// MARKER
 				final DishAdapter adapter = (DishAdapter) dishList.getAdapter();
 				adapter.getFilter().filter(s);
 			}
 		});
 
 		final ToggleButton sortActivate = (ToggleButton) findViewById(R.id.DishListSortActivate);
 		sortActivate.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener()
 		{
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
 			{
 				final LinearLayout layout = (LinearLayout) findViewById(R.id.DishListSortContainer);
 				layout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
 			}
 		});
 
 		sortType.setOnItemSelectedListener(new OnItemSelectedListener()
 		{
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
 			{
 				final ListView dishList = (ListView) findViewById(R.id.DishListView);
 
 				// MARKER
 				final DishAdapter adapter = (DishAdapter) dishList.getAdapter();
 				if (pos == 0)
 					adapter.setSort("name");
 				if (pos == 1)
 					adapter.setSort("price");
 
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0)
 			{
 			}
 		});
 
 		sortDirection.setOnCheckedChangeListener(new OnCheckedChangeListener()
 		{
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId)
 			{
 				final ListView dishList = (ListView) findViewById(R.id.DishListView);
 				// MARKER
 				final DishAdapter adapter = (DishAdapter) dishList.getAdapter();
 
 				if (checkedId == R.id.DishListSortDirectionAsc)
 					adapter.setSortOrder(true);
 				else if (checkedId == R.id.DishListSortDirectionDesc)
 					adapter.setSortOrder(false);
 
 			}
 		});
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		this.onLetsGo();
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
 		this.onLetsGo();
 	}
 
 	@Override
 	public void onBackPressed()
 	{
 		if (goToReservation)
 		{
 			Intent intent = new Intent(DishListView.this, ReservationCreateView.class);
 			intent.putExtra("dishId", 0);
 			setResult(RESULT_CANCELED, intent);
 			finish();
 		}
 		else
 		{
 			super.onBackPressed();
 		}
 		return;
 	}
 
 }
