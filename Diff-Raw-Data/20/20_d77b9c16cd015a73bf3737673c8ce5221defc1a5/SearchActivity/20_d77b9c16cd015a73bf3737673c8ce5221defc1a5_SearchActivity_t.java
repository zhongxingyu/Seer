 package ie.udaltsoft.ethnodroid;
 
 import ie.udaltsoft.ethnodroid.parsers.data.GroupedCodes;
 import ie.udaltsoft.ethnodroid.parsers.data.NamedCode;
 import ie.udaltsoft.ethnodroid.tasks.ExtractCountryTask;
 import ie.udaltsoft.ethnodroid.tasks.ExtractLanguageTask;
import ie.udaltsoft.ethnodroid.tasks.LoadCountryListTask;
import ie.udaltsoft.ethnodroid.tasks.LoadLanguageListTask;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class SearchActivity extends EthnodroidActivity {
 
 	public static final String SEARCH_TYPE = "searchType";
 
 	private EditText searchPattern;
 
 	public enum SearchType implements Serializable {
 		LANGUAGE, COUNTRY
 	};
 
 	private SearchType searchType;
 
 	private OnItemSelectedListener primaryListListener = new OnItemSelectedListener() {
 
 		public void onItemSelected(AdapterView<?> parent, View view, int pos,
 				long id) {
 			final Spinner searchList2 = (Spinner) findViewById(R.id.searchList2);
 
 			final String region = (String) parent.getItemAtPosition(pos);
 			final ArrayList<NamedCode> countryRefs = groupedCodes.getGroups()
 					.get(region);
 			final ArrayAdapter<NamedCode> lcla = new ArrayAdapter<NamedCode>(
 					SearchActivity.this,
 					android.R.layout.simple_spinner_dropdown_item, countryRefs);
 			searchList2.setAdapter(lcla);
 		}
 
 		public void onNothingSelected(AdapterView<?> arg0) {
 			clearListener.onClick(arg0);
 		}
 	};
 
 	private GroupedCodes groupedCodes;
 
 	private OnItemSelectedListener secondaryListListener = new OnItemSelectedListener() {
 
 		public void onItemSelected(AdapterView<?> parent, View view, int pos,
 				long id) {
 			final NamedCode code = (NamedCode) parent.getItemAtPosition(pos);
 			searchPattern.setText(code.getCode());
 		}
 
 		public void onNothingSelected(AdapterView<?> arg0) {
 			clearListener.onClick(arg0);
 		}
 	};
 
 	public SearchActivity() {
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Inflate our UI from its XML layout description.
 		setContentView(R.layout.search_layout);
 		setBrowserLink();
 
 		searchType = (SearchType) getIntent().getSerializableExtra(SEARCH_TYPE);
 
 		// Find the text editor view inside the layout, because we
 		// want to do various programmatic things with it.
 		searchPattern = (EditText) findViewById(R.id.searchPattern);
 
 		((Button) findViewById(R.id.search)).setOnClickListener(searchListener);
 		((Button) findViewById(R.id.clear)).setOnClickListener(clearListener);
 		searchPattern.setOnKeyListener(searchKeyListener);
 		searchPattern.setText("");
 
 		final Spinner searchList1 = (Spinner) findViewById(R.id.searchList1);
 		final Spinner searchList2 = (Spinner) findViewById(R.id.searchList2);
 		groupedCodes = (GroupedCodes) getIntent().getSerializableExtra(
 				RESULTS_EXTRAS);
 		final String[] regions = groupedCodes.getGroups().keySet()
 				.toArray(new String[0]);
 		Arrays.sort(regions);
 
 		final ArrayAdapter<String> lcla = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_dropdown_item, regions);
 		searchList1.setAdapter(lcla);
 		searchList1.setOnItemSelectedListener(primaryListListener);
 		searchList2.setOnItemSelectedListener(secondaryListListener);
 
 		TextView searchList1Title = (TextView) findViewById(R.id.searchList1Title);
 		TextView searchList2Title = (TextView) findViewById(R.id.searchList2Title);
 		switch (searchType) {
 		case LANGUAGE:
 			searchList1Title.setText(R.string.initial_letter_title);
 			searchList2Title.setText(R.string.language_name_title);
 			break;
 		case COUNTRY:
 			searchList1Title.setText(R.string.region_title);
 			searchList2Title.setText(R.string.country_name_title);
 			break;
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		switch (searchType) {
 		case LANGUAGE:
 			menu.add(0, Menu.FIRST, 0, R.string.search_countries).setShortcut(
 					'1', 'c');
 			break;
 		case COUNTRY:
 			menu.add(0, Menu.FIRST, 0, R.string.search_languages).setShortcut(
 					'1', 'l');
 			break;
 		}
 
 		return true;
 	}
 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Single menu item - no need to analyse what item was pressed
		switch (searchType) {
		case LANGUAGE:
			new LoadCountryListTask(SearchActivity.this).execute("all");
			return true;
		case COUNTRY:
			new LoadLanguageListTask(SearchActivity.this).execute("all");
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

 	private final OnKeyListener searchKeyListener = new OnKeyListener() {
 		public boolean onKey(View view, int keyCode, KeyEvent event) {
 			if (keyCode == KeyEvent.KEYCODE_ENTER) {
 				searchListener.onClick(view);
 				return true;
 			}
 			return false;
 		}
 	};
 
 	private final OnClickListener searchListener = new OnClickListener() {
 		public void onClick(View v) {
 
 			final String searchString = (searchPattern.getText().toString());
 			switch (searchType) {
 			case LANGUAGE:
 				new ExtractLanguageTask(SearchActivity.this)
 						.execute(searchString);
 				break;
 			case COUNTRY:
 				new ExtractCountryTask(SearchActivity.this)
 						.execute(searchString);
 				break;
 			}
 		}
 	};
 
 	private final OnClickListener clearListener = new OnClickListener() {
 		public void onClick(View v) {
 			searchPattern.setText("");
 		}
 	};
 }
