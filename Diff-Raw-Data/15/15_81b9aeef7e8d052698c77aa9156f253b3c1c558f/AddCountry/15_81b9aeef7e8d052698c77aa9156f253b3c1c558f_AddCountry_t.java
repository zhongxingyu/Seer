 package com.lnu;
 
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 import com.lnu.mycountries.db.Country;
 import com.lnu.mycountries.db.CountryDAO;
 
 public class AddCountry extends Activity {
 	
 	private CountryDAO	countryDAO;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_add_country);
 		countryDAO = new CountryDAO(this);
 		//
 		final EditText countryEditText = (EditText) findViewById(R.id.countryEditText);
 		final EditText yearEditText = (EditText) findViewById(R.id.yearEditText);
 		final Button addOrUpdateCountryButton = (Button) findViewById(R.id.addOrUpdateCountryButton);
 		final Intent intent = getIntent();
 		
 		final Object oldCountry = intent.getExtras() != null ? intent.getExtras().get(MyCountries.OLD_COUNTRY) : null;
 		if (oldCountry != null) {
 			addOrUpdateCountryButton.setText(R.string.updateCountry);
 			yearEditText.setText(((Country) oldCountry).getYear() + "");
 			countryEditText.setText(((Country) oldCountry).getName());
 		}
 		
 		addOrUpdateCountryButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
				String year = yearEditText.getText().toString();
 				String countryName = countryEditText.getText().toString();
 				try {
 					validate(year, countryName);
 					Country newCountry = null;
 					if (oldCountry != null) {
 						newCountry = (Country) oldCountry;
 						newCountry.setName(countryName);
						newCountry.setYear(Integer.parseInt(year));
 						countryDAO.update(newCountry);
 					} else {
						newCountry = new Country(Integer.parseInt(year), countryName);
 						countryDAO.create(newCountry);
 					}
 					intent.putExtra(MyCountries.COUNTRY, newCountry);
 					setResult(Activity.RESULT_OK, intent);
 					finish();
 				} catch (IllegalArgumentException e) {
 					AlertDialog alertDialog = new AlertDialog.Builder(AddCountry.this).create();
 					alertDialog.setMessage(e.getMessage());
 					alertDialog.show();
 				}
 				
 			}
 		});
 		
 	}
 	
	private void validate(String year, String country) {
 		if (year == null || country.trim().isEmpty()) {
 			throw new IllegalArgumentException("Please enter both year and country.");
 		}
 		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		Integer y = Integer.parseInt(year);
		if (y < 1900 || y > currentYear) {
 			throw new IllegalArgumentException("Year must be between (1900 and " + currentYear + ").");
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_add_country, menu);
 		return true;
 	}
 }
