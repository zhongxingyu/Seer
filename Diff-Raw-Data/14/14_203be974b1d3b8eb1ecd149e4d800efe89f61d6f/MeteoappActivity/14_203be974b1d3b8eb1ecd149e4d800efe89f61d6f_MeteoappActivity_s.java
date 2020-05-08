 package pl.sznapka.meteoapp;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import pl.sznapka.meteo.fetcher.CityFetcher;
 import pl.sznapka.meteo.fetcher.FetcherException;
 import pl.sznapka.meteo.fetcher.StateFetcher;
 import pl.sznapka.meteo.http.HttpClient;
 import pl.sznapka.meteo.valueobject.City;
 import pl.sznapka.meteo.valueobject.Forecast;
 import pl.sznapka.meteo.valueobject.State;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.Spinner;
 
 public class MeteoappActivity extends Activity {
 
 	public ArrayList<State> states;
 	public HashMap<String, ArrayList<City>> cities;
 	public ArrayList<Integer> checkboxes;
 	CacheManager cache;
 	ChoosenForecast choosenForecast;
 	boolean onCreateCalled = false;
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.form);
         
         cache = new CacheManager(getCacheDir());
    	 	cities = (HashMap<String, ArrayList<City>>) cache.loadFromCache("cities");
    	 	if (null == cities) {
    	 		cities = new HashMap<String, ArrayList<City>>();
    	 	}
         initStateSpinner();
 
         attachCheckboxListeners();
         findViewById(R.id.buttonShowMeteo).setOnClickListener(
         		new ShowMeteoButtonListener());
         
         choosenForecast = (ChoosenForecast) cache.loadFromCache("forecast");
         if (choosenForecast != null) {
         	Intent intent = new Intent(this, ForecastActivity.class);
 			Bundle bundle = new Bundle();
 			bundle.putSerializable("choosenForecast", choosenForecast);
 	     	intent.putExtras(bundle);
 			startActivity(intent);
         }
         onCreateCalled = true;
     }
     
     protected void initStateSpinner() {
     	
          ((Spinner)findViewById(R.id.city)).setEnabled(false);
          
          ArrayList<String> items = new ArrayList<String>();
          items.add("Wybierz województwo");
          states = (new StateFetcher()).fetch();
          for (State state : states) {
  			items.add(state.name);
  		 }
          
          Spinner statesSpinner = (Spinner)findViewById(R.id.state);
          ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
          		android.R.layout.simple_spinner_item, items);
          statesSpinner.setAdapter(adapter);
          statesSpinner.setOnItemSelectedListener(new StatesSpinnerListener());
     }
 
     @Override
     protected void onResume() {
     	System.out.println("on resume, oncratecalled: " + onCreateCalled);
     	if (!onCreateCalled && choosenForecast != null) {
     		System.out.println("Choosen forecast for city " + choosenForecast.forecast.city.name + " state " + choosenForecast.forecast.city.state.name);
     		Spinner statesSpinner = (Spinner)findViewById(R.id.state);
     		
     		statesSpinner.setSelection(
     				((ArrayAdapter<String>) statesSpinner.getAdapter()).getPosition(choosenForecast.forecast.city.state.name));
     		setSelectedTypes(choosenForecast.types);
     	}
     	onCreateCalled = false;
     	super.onResume();
     }
 
 	/**
      * Attaches checkboxes listener
      */
     protected void attachCheckboxListeners() {
     	
     	checkboxes = new ArrayList<Integer>();
     	checkboxes.add(R.id.checkBoxTemperature);
     	checkboxes.add(R.id.checkBoxRain);
     	checkboxes.add(R.id.checkBoxPressure);
     	checkboxes.add(R.id.checkBoxWind);
     	checkboxes.add(R.id.checkBoxVisibility);
     	checkboxes.add(R.id.checkBoxClouds);
     	for (Integer id : checkboxes) {
 			CheckBox checkbox = (CheckBox)findViewById(id);
 			checkbox.setOnCheckedChangeListener(new ChecboxListener());
 		}
     }
     
     protected void setButtonEnabledIfAllCriteriaSatisfied(boolean initial) {
 		
     	boolean allCriteriaSatisfied = initial;
 		if (!initial) {
 			for (Integer id : checkboxes) {
 				CheckBox checkbox = (CheckBox)findViewById(id);
 				if (checkbox.isChecked()) {
 					allCriteriaSatisfied = true;
 					break;
 				}
 			}
 		}
 		Spinner city = (Spinner)findViewById(R.id.city);
 		allCriteriaSatisfied = allCriteriaSatisfied & (city.getSelectedItemPosition()
 				!= Spinner.INVALID_POSITION);
 		findViewById(R.id.buttonShowMeteo).setEnabled(allCriteriaSatisfied);    	
     }
 
 	private class ChecboxListener implements OnCheckedChangeListener {
 
 		@Override
 		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 
 			setButtonEnabledIfAllCriteriaSatisfied(isChecked);
 		}
 	}
     
 	private class StatesSpinnerListener implements OnItemSelectedListener {
     	
     	State state;
     	
 		@Override
 		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
 
 			if (pos > 0) {
 				state = states.get(pos - 1);
 				System.out.println("Selected state: " + state.name);
 		        ((Spinner)findViewById(R.id.city)).setEnabled(false);
 				new FetchCitiesTask(choosenForecast).execute(state);
 			}
 		}
 
 		@Override
 		public void onNothingSelected(AdapterView<?> arg0) {
 		}
 	}
 
 	private class ShowMeteoButtonListener implements OnClickListener {
 
 		@Override
 		public void onClick(View button) {
 			
			State state = states.get(((Spinner)findViewById(R.id.state)).getSelectedItemPosition() - 1);
			ArrayList<City> citiesInState = cities.get(state.name);
			City city = citiesInState.get(((Spinner)findViewById(R.id.city)).getSelectedItemPosition());
			
			new FetchForecastTask(MeteoappActivity.this, getSelectedTypes(), cache).execute(city);
 		}
 	}
 	
 	protected ArrayList<String> getSelectedTypes() {
 		
 		ArrayList<String> types = new ArrayList<String>();
 		if (((CheckBox)findViewById(R.id.checkBoxTemperature)).isChecked()) {
 			types.add(Forecast.TEMPERATURE);
 		}
 		if (((CheckBox)findViewById(R.id.checkBoxRain)).isChecked()) {
 			types.add(Forecast.RAIN);
 		}
 		if (((CheckBox)findViewById(R.id.checkBoxPressure)).isChecked()) {
 			types.add(Forecast.PRESSURE);
 		}
 		if (((CheckBox)findViewById(R.id.checkBoxWind)).isChecked()) {
 			types.add(Forecast.WIND);
 		}
 		if (((CheckBox)findViewById(R.id.checkBoxVisibility)).isChecked()) {
 			types.add(Forecast.VISIBILITY);
 		}
 		if (((CheckBox)findViewById(R.id.checkBoxClouds)).isChecked()) {
 			types.add(Forecast.CLOUDS);
 		}
 		
 		return types;
 	}
 	
 	protected void setSelectedTypes(ArrayList<String> types) {
 		
 		if (types.contains(Forecast.TEMPERATURE)) {
 			((CheckBox)findViewById(R.id.checkBoxTemperature)).setChecked(true);
 		}
 		if (types.contains(Forecast.RAIN)) {
 			((CheckBox)findViewById(R.id.checkBoxRain)).setChecked(true);
 		}
 		if (types.contains(Forecast.PRESSURE)) {
 			((CheckBox)findViewById(R.id.checkBoxPressure)).setChecked(true);
 		}
 		if (types.contains(Forecast.WIND)) {
 			((CheckBox)findViewById(R.id.checkBoxWind)).setChecked(true);
 		}
 		if (types.contains(Forecast.VISIBILITY)) {
 			((CheckBox)findViewById(R.id.checkBoxVisibility)).setChecked(true);
 		}
 		if (types.contains(Forecast.CLOUDS)) {
 			((CheckBox)findViewById(R.id.checkBoxClouds)).setChecked(true);
 		}
 	}
 	
 	private class FetchCitiesTask extends AsyncTask<State, Void, ArrayList<City>> {
 
 		protected ProgressDialog progressDialog;
 		protected FetcherException exception;
 		protected ChoosenForecast choosenForecast;
 		
 		public FetchCitiesTask(ChoosenForecast choosenForecast) {
 			
 			this.choosenForecast = choosenForecast;
 		}
 		
 		@Override
 		protected ArrayList<City> doInBackground(State... states) {
 
 			State state = states[0];
 			try {
 				if (!cities.containsKey(state.name)) {
 					CityFetcher fetcher = new CityFetcher(state, new HttpClient());
 					cities.put(state.name, fetcher.fetch());
 					cache.storeObjectInCache("cities", cities);
 				}
 				return cities.get(state.name);
 			} catch (FetcherException e) {
 				System.out.println("Exception: " + e.getMessage());
 				exception = e;
 			}
 			return null;
 		}
 		
 		protected void onPreExecute() {
 
 			super.onPreExecute();
 			progressDialog = new ProgressDialog(MeteoappActivity.this);
 			progressDialog.setTitle("Pobieram");
 			progressDialog.setMessage("Proszę czekać, pobieranie listy miast");
 			progressDialog.show();
 		}
 		
 		protected void onPostExecute(ArrayList<City> cities) {
 
 			if (exception == null) {
 		        ArrayList<String> items = new ArrayList<String>();
 		        for (City city : cities) {
 					items.add(city.name);
 				}
 		        Spinner citiesSpinner = (Spinner)findViewById(R.id.city);
 		        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MeteoappActivity.this,
 		        		android.R.layout.simple_spinner_item, items);
 		        citiesSpinner.setAdapter(adapter);
 		        ((Spinner)findViewById(R.id.city)).setEnabled(true);
 		        setButtonEnabledIfAllCriteriaSatisfied(false);
 		        
 		        if (choosenForecast != null && choosenForecast.forecast.city.state.symbol.equals(cities.get(0).state.symbol)) {
 		    		citiesSpinner.setSelection(items.indexOf(choosenForecast.forecast.city.name));
 		        }
 			} else {
 				new ExceptionHandler(MeteoappActivity.this, exception).handle();
 			}
 	        progressDialog.dismiss();
 		}
 	}
 }
