 package com.dzebsu.acctrip;
 
 import java.util.List;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.dzebsu.acctrip.adapters.EventCurrenciesListViewAdapter;
 import com.dzebsu.acctrip.db.datasources.CurrencyPairDataSource;
 import com.dzebsu.acctrip.db.datasources.EventDataSource;
 import com.dzebsu.acctrip.models.CurrencyPair;
 import com.dzebsu.acctrip.models.Event;
 
 public class EventCurrenciesListActivity extends Activity {
 
 	private static final String RESTORE_KEY_SECOND_VALUES = "secondValues";
 
 	private static final String RESTORE_KEY_FIST_VALUES = "fistValues";
 
 	private static final String RESTORE_KEY_LEFT_ORI = "leftOri";
 
 	private static final String INTENT_EXTRA_EVENT_ID = "eventId";
 
 	private EventCurrenciesListViewAdapter adapterZ = null;
 
 	private Event event;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_event_currencies_list);
 		event = new EventDataSource(this).getEventById(getIntent().getLongExtra(INTENT_EXTRA_EVENT_ID, -1));
 		setupActionBar();
 		fillCurrenciesList();
 		if (savedInstanceState != null && savedInstanceState.containsKey(RESTORE_KEY_LEFT_ORI)) {
 			adapterZ.setLeftOri(savedInstanceState.getBooleanArray(RESTORE_KEY_LEFT_ORI));
 			adapterZ.setFirstValues(savedInstanceState.getDoubleArray(RESTORE_KEY_FIST_VALUES));
 			adapterZ.setSecondValues(savedInstanceState.getDoubleArray(RESTORE_KEY_SECOND_VALUES));
 			adapterZ.notifyDataSetChanged();
 		}
 		((Button) findViewById(R.id.ev_cur_cancel)).setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				onCancelBtn();
 
 			}
 		});
 		((Button) findViewById(R.id.ev_cur_apply)).setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				onSaveBtn();
 			}
 		});
 	}
 
 	public void onCancelBtn() {
 		finish();
 
 	}
 
 	public void onSaveBtn() {
 		saveRates();
 		Toast.makeText(getApplicationContext(), R.string.currency_rates_edited, Toast.LENGTH_SHORT).show();
 		finish();
 	}
 
 	private void fillCurrenciesList() {
 		if (adapterZ == null) {
 			CurrencyPairDataSource dataSource = new CurrencyPairDataSource(this);
 			List<CurrencyPair> cps = dataSource.getCurrencyPairListByEventId(event.getId());
 			adapterZ = new EventCurrenciesListViewAdapter(this, cps, event.getPrimaryCurrency());
 		}
 		ListAdapter adapter = adapterZ;
 		ListView listView = (ListView) findViewById(R.id.currencylist);
 		// trigger filter to it being applied on resume
 		listView.setAdapter(adapter);
 
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 			getActionBar().setTitle(R.string.event_currencies_activity_title);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.edit_all_event_currencies, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.save_all_currs:
 				onSaveBtn();
 				return true;
 			case R.id.cancel_all_currs:
 				onCancelBtn();
 				return true;
 			case android.R.id.home:
 				finish();
 				return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putBooleanArray(RESTORE_KEY_LEFT_ORI, adapterZ.getLeftOri());
 		outState.putDoubleArray(RESTORE_KEY_FIST_VALUES, adapterZ.getFirstValues());
 		outState.putDoubleArray(RESTORE_KEY_SECOND_VALUES, adapterZ.getSecondValues());
 	}
 
 	protected void saveRates() {
 		double firstValues[] = adapterZ.getFirstValues();
 		double rates[] = adapterZ.getSecondValues();
 		for (int i = 0; i < firstValues.length; i++)
 			rates[i] /= firstValues[i];
 		new CurrencyPairDataSource(this).updateRatesBunch(adapterZ.getObjects(), rates);
 	}
 }
