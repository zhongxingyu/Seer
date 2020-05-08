 package be.uclouvain.sinf1225.gourmet;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import be.uclouvain.sinf1225.gourmet.models.Dish;
 import be.uclouvain.sinf1225.gourmet.models.Restaurant;
 
 public class ReservationCreateView extends Activity
 {
 
 	/* date and time */
 	protected static Calendar dateTime = Calendar.getInstance();
 	// TODO check if we should use SimpleDateFormat.getDate/TimeInstance() or not.
 	protected static SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
 	protected static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm a", Locale.getDefault());
 	protected static Button datePicker;
 	protected static Button timePicker;
 
 	/* input Controls */
 	private ListView list;
 	private TextView restaurant;
 	private EditText nbrReservation;
 	private AutoCompleteTextView addDish;
 	private ArrayList<String> listDish = new ArrayList<String>();
 
 	/* adapters */
 	private ArrayAdapter<String> adapter; // list of dishes
 	private ArrayAdapter<String> adapterAddDishes; // auto-completion
 
 	/* */
 	private static final String RESTAURANT = "restaurant"; // intent's key - restoID
 	private static final String DISH = "dish"; // intent's key - dishID
 
 	private static Restaurant resto;
 	private static Dish dish;
 	private List<String> dishesList;						// List of dishes from 'resto'
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_reservation_edit);
 
 		/* date picker - choose the date of the reservation */
 		datePicker = (Button) findViewById(R.id.datepicker);
 		datePicker.setText(dateFormatter.format(dateTime.getTime()));
 
 		/* date picker - choose the hour of the reservation */
 		timePicker = (Button) findViewById(R.id.timepicker);
 		timePicker.setText(timeFormatter.format(dateTime.getTime()));
 
 		/* auto-completion - make the choose of the dish easier */
 		/* adapter for the auto-completion */
 		addDish = (AutoCompleteTextView) findViewById(R.id.dish);
		// dishesList = Dish.getDishName(Dish.getDishInRestaurant(resto));
		// String[] dishesResto = (String[]) dishesList.toArray();
		// adapterAddDishes = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dishesResto);
		// addDish.setAdapter(adapterAddDishes);
 
 		/* input items */
 		list = (ListView) findViewById(R.id.listDish);
 		restaurant = (TextView) findViewById(R.id.restaurant);
 		nbrReservation = (EditText) findViewById(R.id.nbrReservation);
 
 		/* Receive the data */
		// getDataTransfer(getIntent());
 
 		/* adapter for the list of dishes */
 		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, listDish);
 		list.setAdapter(adapter);
 	}
 
 	public void showTimePickerDialog(View v)
 	{
 		DialogFragment newFragment = new TimePickerFragment();
 		newFragment.show(getFragmentManager(), "timePicker");
 	}
 
 	public void showDatePickerDialog(View v)
 	{
 		DialogFragment newFragment = new DatePickerFragment();
 		newFragment.show(getFragmentManager(), "datePicker");
 	}
 
 	public void addItems(View v)
 	{
 		String message = addDish.getText().toString();
 		listDish.add(message);
 		adapter.notifyDataSetChanged();
 	}
 
 	public void sendReservation(View view)
 	{
 		long nbr = Long.parseLong(nbrReservation.getText().toString());
 		restaurant.setText(String.valueOf(nbr));
 		// User user = User.getUserConnected();
 		// TODO
 	}
 
 	public void getDataTransfer(Intent intent)
 	{
 		int restoID = intent.getIntExtra(RESTAURANT, 0);
 		int dishID = intent.getIntExtra(DISH, 0);
 
 		if ((dishID == 0 && restoID == 0) || (dishID != 0 && restoID != 0))
 		{
 			finish();
 		}
 		else if (dishID != 0)
 		{
 			dish = Dish.getDish(dishID);
 
 			// add item to the listDish
 			listDish.add(dish.getName());
 			adapter.notifyDataSetChanged();
 
 			// display the restaurant's name
 			restoID = dish.getRestoId();
 		}
 		resto = Restaurant.getRestaurant(restoID);
 		restaurant.setText(resto.getName());
 	}
 
 }
 
