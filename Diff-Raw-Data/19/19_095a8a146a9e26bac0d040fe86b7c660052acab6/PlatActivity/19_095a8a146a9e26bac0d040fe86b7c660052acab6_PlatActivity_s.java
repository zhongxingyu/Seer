 package com.horeca;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 public class PlatActivity extends Activity implements OnClickListener {
 
 	private TextView horeca_name = null;
 	private TextView plat_name = null;
 	private TextView plat_price = null;
 	private TextView plat_description = null;
 	private TextView plat_stock = null;
 	private TextView plat_ingredients = null;
 	
 	private DatePicker commande_date = null;
 	private TimePicker commande_time = null;
 	private EditText commande_nombre = null;
 	private Button commande_button = null;
 	private Button current_commandes_button = null;
 	
 	private Plat plat = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// Get the id of the plat to display provided by PlatListActivity
 		Bundle b = getIntent().getExtras();
 		long id = b.getLong("_id");
 		
 		// Open the db
 		MySqliteHelper sqliteHelper = new MySqliteHelper(this);
 		SQLiteDatabase db = sqliteHelper.getReadableDatabase();
 		
 		// Get the plat from the db
 		plat = new Plat(id, db);
 		
 		// close the db, exerything has been loaded in the constructor of Plat
 		db.close();
 		
 		// Set the title of the activity
 		setTitle(plat.getHoreca().getName() + " - " + plat.getName());
 		
 		// Set TextView's content
 		setContentView(R.layout.activity_plat);
 		horeca_name = (TextView) findViewById(R.id.horeca_name);
 		horeca_name.setText(plat.getHoreca().getName());
 		
 		plat_name = (TextView) findViewById(R.id.plat_name);
 		plat_name.setText(plat.getName());
 		
 		plat_price = (TextView) findViewById(R.id.plat_price);
 		plat_price.setText(((Double) plat.getPrice()).toString() + getResources().getString(R.string.plat_price_currency));
 		
 		plat_description = (TextView) findViewById(R.id.plat_description);
 		if (plat.hasDescription()) {
 			plat_description.setText(plat.getDescription());
 		} else {
 			findViewById(R.id.plat_description_label).setVisibility(View.GONE);
 			plat_description.setVisibility(View.GONE);
 		}
 		
 		plat_stock = (TextView) findViewById(R.id.plat_stock);
 		if (plat.hasStock()) {
 			plat_stock.setText(((Long) plat.getStock()).toString());
 		} else {
 			plat_stock.setText(R.string.plat_stock_unknown);
 		}
 		
 		plat_ingredients = (TextView) findViewById(R.id.plat_ingredients);
 		Ingredient[] ingredients = plat.getIngredients();
 		if (ingredients.length == 0) {
 			plat_ingredients.setText(R.string.plat_ingredients_none);
 		} else {
 			StringBuilder text = new StringBuilder();
 			for (int i = 0; i < ingredients.length; i++) {
 				if (i != 0) {
 					text.append(", ");
 				}
 				text.append(ingredients[i].getName());
 			}
 			plat_ingredients.setText(text.toString());
 		}
 		
 		Calendar cal=Calendar.getInstance();
 
 		int year=cal.get(Calendar.YEAR);
 		int month=cal.get(Calendar.MONTH);
 		int day=cal.get(Calendar.DAY_OF_MONTH);
 		int hour=cal.get(Calendar.HOUR_OF_DAY);
 		int minute=cal.get(Calendar.MINUTE);
 
 		commande_date = (DatePicker) findViewById(R.id.commande_date);
 		commande_date.updateDate(year, month, day);
 		commande_time = (TimePicker) findViewById(R.id.commande_time);
 		commande_time.setCurrentHour(hour);
 		commande_time.setCurrentMinute(minute);
 		commande_nombre = (EditText) findViewById(R.id.commande_nombre);
 		commande_button = (Button) findViewById(R.id.commande_button);
 		commande_button.setOnClickListener(this);
 		current_commandes_button = (Button) findViewById(R.id.current_commandes_button);
 		current_commandes_button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				Intent i = new Intent(PlatActivity.this, CommandesActivity.class);
 				i.putExtra("_id", plat.getId());
 				startActivity(i);
 			}
 		});
 	}
 	
 	private void refreshStock(SQLiteDatabase db) {
 		if (plat.hasStock()) {
 			plat.reloadStock(db);
 			plat_stock.setText(String.valueOf(plat.getStock()));
 		}
 	}
 	
 	protected void onRestart() {
 		super.onRestart();
 		MySqliteHelper sqliteHelper = new MySqliteHelper(this);
 		SQLiteDatabase db = sqliteHelper.getReadableDatabase();
 		
 		refreshStock(db);
 		
 		db.close();
 	}
 
 	@Override
 	public void onClick(View view) {
		long nombre = Long.parseLong(commande_nombre.getText().toString());
 		if (nombre <= 0 || (plat.hasStock() && nombre > plat.getStock())) {
 			Toast.makeText(this, R.string.invalid_amount_warning, Toast.LENGTH_SHORT).show();
 		} else {
 			MySqliteHelper sqliteHelper = new MySqliteHelper(this);
 			SQLiteDatabase db = sqliteHelper.getWritableDatabase();
 		
 			Commande.createCommande(db, plat,
 					new GregorianCalendar(commande_date.getYear(), commande_date.getMonth(),
 							commande_date.getDayOfMonth(), commande_time.getCurrentHour(),
 							commande_time.getCurrentMinute()).getTime(), nombre);
 			
 			refreshStock(db);
 		
 			db.close();
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.plat, menu);
 		return true;
 	}
 
 }
