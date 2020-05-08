 package edu.ucsb.cs.cs185.moneysaver;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.NumberPicker;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class Transactions extends Activity {
 
 	private DataBaseWrapper m_database;
 	
 	//if passed to transaction then edit, else new
 	private long m_trans_id;
 	private String m_date_str;
 	
 	private NumberPicker np_dollars;
 	private NumberPicker np_cents;
 
 	private ImageView iv_name_error;
 	
 	private EditText et_name;
 	private EditText et_description;
 	
 	private Spinner spinner_categories;
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		Intent i;
     	switch(item.getItemId())
     	{
     		case R.id.home:    			
     			i = new Intent(this, MoneySaverActivity.class);
     			startActivity(i);
     			finish();
     			return true;
     		case R.id.tran:
     			return true;
     		case R.id.pie:
     			i = new Intent(this, Charts.class);
     			startActivity(i);
     			finish();
     			return true;
     		case R.id.settings:
     			i = new Intent(this, Settings.class);
     			startActivity(i);
     			finish();
     			return true;
     		case R.id.help:
     			i = new Intent(this, Help.class);
     			startActivity(i);
     			finish();
     			return true;    			
     		default:
     			return true;	
     	}
 	}
 	
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.transactions);
      
         //EditText fields
         et_name = (EditText) findViewById(R.id.et_name);
         
         ActionBar actionbar = getActionBar();
         actionbar.setDisplayShowHomeEnabled(false);
         actionbar.setDisplayShowTitleEnabled(false);
 
         np_dollars = (NumberPicker) findViewById(R.id.np_dollars);
         np_cents = (NumberPicker) findViewById(R.id.np_cents);
         np_dollars.setMinValue(0);
         np_dollars.setMaxValue(10000);
         np_cents.setMinValue(0);
         np_cents.setMaxValue(99);
         
         
     	et_description = (EditText) findViewById(R.id.et_description);
         spinner_categories = (Spinner) findViewById(R.id.spinner_categories);
         
         
         //connect to database
         m_database = new DataBaseWrapper(getApplicationContext());
         List<Category> category_list = m_database.getAllCategories();
         
         int num_categories = category_list.size();
 
         String[] categories = new String[num_categories];
         
         for(int index = 0; index < num_categories; index++) {
         	categories[index] = category_list.get(index).getCategory();
         }
         
         /*
          * Set spinner to categories from Database
          */
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(Transactions.this, android.R.layout.simple_list_item_1, categories);
         
         spinner_categories.setAdapter(adapter);
         
     	iv_name_error = (ImageView) findViewById(R.id.iv_name_error);
     	iv_name_error.setVisibility(View.INVISIBLE);
     	
     	
     	 /*
          * Check to see if bundle is passed indicating this is editing a transaction
          */
     	m_trans_id = -1;
     	
     	Bundle extras = getIntent().getExtras(); 
         if(extras != null) {
         	String name = extras.getString(MoneySaverActivity.TRANS_NAME);
 			float amount = extras.getFloat(MoneySaverActivity.TRANS_AMOUNT);
 			
 			String category = extras.getString(MoneySaverActivity.TRANS_CATEGORY);
 			String description = extras.getString(MoneySaverActivity.TRANS_DESCRIPTION);
 			
 			
 			m_date_str = extras.getString(MoneySaverActivity.TRANS_DATE);
         	m_trans_id = extras.getLong(MoneySaverActivity.TRANS_ID);
 			
 			int dollars = (int) amount;
         	int cents = (int)((amount - dollars) * 100);
         	
         	et_name.setText(name);
         	
         	/*
         	 * Set category by looping through list and comparing string values
         	 */
         	for(int index=0; index < num_categories; index++) {
         		if(category.equalsIgnoreCase(categories[index])) {
         			spinner_categories.setSelection(index);
         			break;
         		}
         	}
         	et_description.setText(description);
         	
         	Button b = (Button) findViewById(R.id.b_plus_minus);
         	if(dollars>0)
         		b.setText("+");
         	np_dollars.setValue(Math.abs(dollars));
         	np_cents.setValue(cents);
         }
     }
     
     
     public void buttonClick(View view)
     {
     	Button b_clicked = (Button) view;
     	int id = b_clicked.getId();
     	
     	switch(id) {
     		case R.id.b_plus_minus:
     			String text = b_clicked.getText().toString();
     			if(text.compareTo("-")==0)
     	    	{
     				b_clicked.setText("+");
     	    	}
     	    	else
     	    	{
     	    		b_clicked.setText("-");
     	    	}
     			break;
     		case  R.id.b_add:
     			//check that all fields are populated
     			
     			returnTransaction();
     			break;
     		case R.id.b_advanced:
     			Toast.makeText(Transactions.this, "TODO: Adv.", Toast.LENGTH_SHORT).show();
     			break;
     		default:
     			Toast.makeText(Transactions.this, "UNKNOWN BUTTON PRESSED", Toast.LENGTH_SHORT).show();
     	}
     }
     
     public void returnTransaction() {
     	//create a new transaction bundle
     	String name = ""; 
     	
     	//String description = "";
     	
     	float amount, dollars, cents = 0;
     	
     	name = et_name.getText().toString();
     	
     	if(name.compareTo("") == 0) {
     		Toast.makeText(Transactions.this, "Please insert a name!", Toast.LENGTH_SHORT).show();
     		iv_name_error.setVisibility(View.VISIBLE);
     		return;
     	}
     	String category = (String) spinner_categories.getSelectedItem().toString();
     	String description = et_description.getText().toString();
     
     	//Get amount of transaction
     	dollars = np_dollars.getValue();
    	cents = (float)np_cents.getValue() / 10;
     	amount = dollars + cents;
     	
     	//determine if positive or negative amount
     	Button b_plus_minus = (Button)findViewById(R.id.b_plus_minus);
     	String button_text = b_plus_minus.getText().toString();
     	if(button_text.compareTo("-")==0) {
     		amount = amount * -1;
     	}
     	
     	//Get current date
     	//http://www.mkyong.com/java/java-how-to-get-current-date-time-date-and-calender/
     	DateFormat date_format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
     	Date date = new Date();
     	
     	
     	if(m_trans_id==-1) {
     		m_date_str = date_format.format(date);
     		Toast.makeText(Transactions.this, "Added New Transaction: " + name + " amount: " + amount + " on: " + m_date_str, Toast.LENGTH_SHORT).show();
     	} else {
     		m_date_str = date_format.format(date);
     		Toast.makeText(Transactions.this, "Editted New Transaction: " + name + " amount: " + amount + " on: " + m_date_str, Toast.LENGTH_SHORT).show();
     	}
     	/*
     	 * Create bundle to return to home activity
     	 */
     	Intent intent = this.getIntent();
     	
     	intent.putExtra(MoneySaverActivity.TRANS_NAME, name);
     	intent.putExtra(MoneySaverActivity.TRANS_AMOUNT, amount);
     	
     	intent.putExtra(MoneySaverActivity.TRANS_CATEGORY, category);
     	intent.putExtra(MoneySaverActivity.TRANS_DESCRIPTION, description);
     	
     	intent.putExtra(MoneySaverActivity.TRANS_DATE, m_date_str);
     	intent.putExtra(MoneySaverActivity.TRANS_ID, m_trans_id);
     	
 		this.setResult(RESULT_OK, intent);
     	
 		finish();
     }
     
     //http://stackoverflow.com/questions/2000102/android-override-back-button-to-act-like-home-button
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
     	//make back button work
         if (keyCode == KeyEvent.KEYCODE_BACK) {
         	this.setResult(RESULT_CANCELED);
             finish();
             return false;
         }
         return super.onKeyDown(keyCode, event);
     }
 }
