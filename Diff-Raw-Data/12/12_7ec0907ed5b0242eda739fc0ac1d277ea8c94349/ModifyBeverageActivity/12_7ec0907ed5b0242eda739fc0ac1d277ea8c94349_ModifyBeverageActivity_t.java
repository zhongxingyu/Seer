 package ws.wiklund.guides.activities;
 
 import java.util.Date;
 import java.util.List;
 
 import ws.wiklund.guides.R;
 import ws.wiklund.guides.db.BeverageDatabaseHelper;
 import ws.wiklund.guides.model.Beverage;
 import ws.wiklund.guides.model.BeverageType;
 import ws.wiklund.guides.model.Category;
 import ws.wiklund.guides.model.Country;
 import ws.wiklund.guides.model.Producer;
 import ws.wiklund.guides.model.Provider;
 import ws.wiklund.guides.util.ViewHelper;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public abstract class ModifyBeverageActivity extends BaseActivity {
 	protected Beverage beverage;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.modify);
 
         beverage = (Beverage) getIntent().getSerializableExtra("ws.wiklund.guides.model.Beverage");
 
 		Log.d(ModifyBeverageActivity.class.getName(), "Beverage: " + (beverage != null ? beverage.toString() : null));
 
 		populateSpinnersAndCountryView();
 		
 		if (beverage != null) {
 			setTitle(beverage.getName());
 			populateUI();
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.modify_beverage_menu, menu);
 		
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.menuSaveBeverage) {
 			Beverage b = getBeverageFromUI();
 			if(ViewHelper.validateBeverage(this, b)) {
 				saveBeverage(getDatabaseHelper(), b);
 				showBeverageList();					
 			}
 			return true;
 		} else if (item.getItemId() == R.id.menuCancel) {
 			showBeverageList();
 			return true;
 		}
 		
 		return false;
 	}	
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		//Must be called after super or else the image will not be loaded
 		if (bitmap != null) {
 			bitmap = Bitmap.createScaledBitmap(bitmap, 50, 100, true);
 			ImageView imageView = (ImageView) (findViewById(android.R.id.content).findViewById(R.id.Image_thumbUrl));
 			imageView.setImageBitmap(bitmap);
 		}
 	}
 
 	public void changePicture(View view) {
 		Uri outputFileUri = Uri.fromFile(getTempFile());
 		
 		Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );		
 		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
 		startActivityForResult(intent, REQUEST_FROM_CAMERA);
 	}
 
 	private void populateUI() {
     	ViewHelper.setThumbFromUrl((ImageView) (findViewById(android.R.id.content).findViewById(R.id.Image_thumbUrl)), beverage.getThumb());
 		TextView no = (TextView) findViewById(R.id.Text_no);
 		no.setText(String.valueOf(beverage.getNo()));
 		
 		EditText name = (EditText) findViewById(R.id.Edit_name);
 		ViewHelper.setText(name, beverage.getName());
 		
 		setTypeSpinner(beverage.getBeverageType());
 		
 		Country c = beverage.getCountry();
 		if (c != null) {
 			ViewHelper.setCountryThumbFromUrl((ImageView) (findViewById(android.R.id.content).findViewById(R.id.Image_country_thumbUrl)), c);
 			AutoCompleteTextView country = (AutoCompleteTextView) findViewById(R.id.Edit_country);
 			ViewHelper.setText(country, c.getName());
 		}
 		
 		setYearSpinner(beverage.getYear());
 		
 		Producer p = beverage.getProducer();
 		if (p != null) {
 			EditText producer = (EditText) findViewById(R.id.Edit_producer);
 			ViewHelper.setText(producer, p.getName());
 		}
 		
 		setStrengthSpinner(beverage.getStrength());
 		
 		EditText price = (EditText) findViewById(R.id.Edit_price);
 		ViewHelper.setText(price, String.valueOf(beverage.getPrice()));
 
 		EditText usage = (EditText) findViewById(R.id.Edit_usage);
 		ViewHelper.setText(usage, beverage.getUsage());
 		
 		EditText taste = (EditText) findViewById(R.id.Edit_taste);
 		ViewHelper.setText(taste, beverage.getTaste());
 		
 		Provider p1 = beverage.getProvider();
 		if (p1 != null) {
 			EditText provider = (EditText) findViewById(R.id.Edit_provider);
 			ViewHelper.setText(provider, p1.getName());
 		}
 		
 		TextView added = (TextView) findViewById(R.id.Text_added);
 		added.setText(ViewHelper.getDateAsString((beverage.getAdded() != null ? beverage.getAdded() : new Date())));
 	}
 
 	private void populateSpinnersAndCountryView() {
 		AutoCompleteTextView country = (AutoCompleteTextView) findViewById(R.id.Edit_country);
 		addAdaptertoCountryView(country, getDatabaseHelper());
 		
 		Spinner type = (Spinner) findViewById(R.id.Spinner_type);
 		ArrayAdapter<BeverageType> typeAdapter = new ArrayAdapter<BeverageType>(this, android.R.layout.simple_spinner_dropdown_item, getDatabaseHelper().getAllBeverageTypes());
 		type.setAdapter(typeAdapter);
 		
 		Spinner year = (Spinner) findViewById(R.id.Spinner_year);
 		ArrayAdapter<Integer> yearAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, ViewHelper.getYears());
 		year.setAdapter(yearAdapter);
 
 		Spinner strength = (Spinner) findViewById(R.id.Spinner_strength);
 		ArrayAdapter<String> strengthAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getStrengths());
 		strength.setAdapter(strengthAdapter);
 	}
 
 	private void setStrengthSpinner(double strength) {
 		Spinner strengthSpinner = (Spinner) findViewById(R.id.Spinner_strength);
 
 		if (strength > 0) {
 			@SuppressWarnings("unchecked")
 			ArrayAdapter<String> adapter = (ArrayAdapter<String>) strengthSpinner.getAdapter();
 			strengthSpinner.setSelection(adapter.getPosition(ViewHelper.getDecimalStringFromNumber(strength) + " %"));
 		}
 	}
 
 	private void setTypeSpinner(BeverageType type) {
 		Spinner typeSpinner = (Spinner) findViewById(R.id.Spinner_type);
 
 		if (type != null) {
 			@SuppressWarnings("unchecked")
 			ArrayAdapter<BeverageType> adapter = (ArrayAdapter<BeverageType>) typeSpinner.getAdapter();
 			typeSpinner.setSelection(adapter.getPosition(type));
 		}
 	}
 
 	private void setYearSpinner(int year) {
 		Spinner yearSpinner = (Spinner) findViewById(R.id.Spinner_year);
 
 		if(year >= 1900 && year <= ViewHelper.getCurrentYear()) {
 			@SuppressWarnings("unchecked")
 			ArrayAdapter<Integer> adapter = (ArrayAdapter<Integer>) yearSpinner.getAdapter();
 			yearSpinner.setSelection(adapter.getPosition(year));
 		}
 	}
 
 	private void showBeverageList() {
     	startActivityForResult(new Intent(getApplicationContext(), getIntentClass()), 0);
     	finish();
 	}
 
 	private Beverage getBeverageFromUI() {
     	String name = ((EditText) findViewById(R.id.Edit_name)).getText().toString();		
     	BeverageType type = (BeverageType) ((Spinner) findViewById(R.id.Spinner_type)).getSelectedItem();
 		String country = ((EditText) findViewById(R.id.Edit_country)).getText().toString();
 		
 		int	year = (Integer) ((Spinner) findViewById(R.id.Spinner_year)).getSelectedItem();
 
 		String producer = ((EditText) findViewById(R.id.Edit_producer)).getText().toString();
 		String strength = getStrenghtFromSpinner();
 		String price = ((EditText) findViewById(R.id.Edit_price)).getText().toString();
 		String usage = ((EditText) findViewById(R.id.Edit_usage)).getText().toString();
 		String taste = ((EditText) findViewById(R.id.Edit_taste)).getText().toString();
 		String provider = ((EditText) findViewById(R.id.Edit_provider)).getText().toString();
 		String comment = ((EditText) findViewById(R.id.Edit_comment)).getText().toString();
 		
 		Category category = (Category) ((Spinner) findViewById(R.id.Spinner_category)).getSelectedItem();
 		//TODO handle removal of cat for wine
 		if(beverage == null) {
 			beverage = new Beverage();
 
 			String noStr = ((TextView) findViewById(R.id.Text_no)).getText().toString();
 			if (noStr.length() > 0) {
 				beverage.setNo(Integer.valueOf(noStr));
 			}
     	}
 		
 		beverage.setName(name);
 		beverage.setBeverageType(type);		
 		
 		if (year != 1900) {
 			beverage.setYear(year);
 		}
 		
 		beverage.setStrength(ViewHelper.getDoubleFromDecimalString(strength));
 		
 		if (price.length() > 0) {
 			beverage.setPrice(ViewHelper.getDoubleFromDecimalString(price));
 		}
 		
 		beverage.setUsage(usage);
 		beverage.setTaste(taste);
 		beverage.setCategory(category);
 		beverage.setComment(comment);
 		updateCountry(country);
 		updateProducer(producer);
 		updateProvider(provider);
 
 		return beverage;
 	}
 
 	private void updateCountry(String country) {
 		if(beverage != null && country.length() > 0) {
 			Country c = beverage.getCountry();
 			
			if(c == null || c.getName() == null || (!c.getName().equals(country))) {
 				beverage.setCountry(new Country(country, null));
 			}
 		}
 	}
 
 	private void updateProducer(String producer) {
 		if(beverage != null && producer.length() > 0) {
 			Producer p = beverage.getProducer();
 			
			if(p == null || p.getName() == null || (!p.getName().equals(producer))) {
 				beverage.setProducer(new Producer(producer));
 			}
 		}
 	}
 
 	private void updateProvider(String provider) {
 		if(beverage != null && provider.length() > 0) {
 			Producer p = beverage.getProducer();
 			
 			if(p == null || p.getName() == null || (!p.getName().equals(provider))) {
 				beverage.setProvider(new Provider(provider));
 			}
 		}
 	}
 
 	private String getStrenghtFromSpinner() {
 		String s = (String) ((Spinner) findViewById(R.id.Spinner_strength)).getSelectedItem();
 		return s.substring(0, s.indexOf(" "));
 	}
 	
 	protected abstract BeverageDatabaseHelper getDatabaseHelper();
 	protected abstract Class<?> getIntentClass();
 	protected abstract List<String> getStrengths();
 
 }
