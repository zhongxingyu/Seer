 package upenn.pennapps2012f;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 /**
  * Settings activity that can be reached from icon
  * on SilenceActivity. Allows user to set favorites
  * (exceptions to the silence rule) and to customize
  * additional features (exception rules, custom
  * auto-reply message, etc.)
  * 
  * @author hoconnie, jinyan, ckong
  *
  */
 
 public class SettingsActivity extends Activity {
 	private ListView favoritesListView;
 	private ListView rulesListView;
 	private ListView eventsListView;
 	
 	private ArrayAdapter<String> favoritesListAdapter;
 	private ArrayAdapter<String> rulesListAdapter;
 	private ArrayAdapter<String> eventsListAdapter;	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Remove title bar
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.settings_activity);
 		
 		// Get ListView resources
 		favoritesListView = (ListView) findViewById(R.id.favorites_list);
 		rulesListView = (ListView) findViewById(R.id.rules_list);
 		eventsListView = (ListView) findViewById(R.id.upcoming_list);
 		
 		// Add headers to each list
 		LayoutInflater inflater = getLayoutInflater();
 		View favoritesHeader = inflater.inflate(
 				R.layout.settings_favorites_header,
 				favoritesListView, false);
 		favoritesListView.addHeaderView(favoritesHeader, null, false);
 		View rulesHeader = inflater.inflate(
 				R.layout.settings_rules_header,
 				rulesListView, false);
 		rulesListView.addHeaderView(rulesHeader, null, false);
 		View eventsHeader = inflater.inflate(
 				R.layout.settings_events_header,
 				eventsListView, false);
 		eventsListView.addHeaderView(eventsHeader, null, false);
 		
 		
 		// Populate list of favorites
 		String[] favorites = new String[] {
 				"Amalia Hawkins",
 				"Charles Kong",
 				"Cynthia Mai",
				"Jason Mow"
 		};
 		ArrayList<String> favoritesList = new ArrayList<String>();
 		favoritesList.addAll(Arrays.asList(favorites));
 		favoritesListAdapter = 
 				new ArrayAdapter<String>(this, R.layout.settings_row, favoritesList);
 		favoritesListView.setAdapter(favoritesListAdapter);
 		
 		// Populate list of rules
 		String[] rules = new String[] {
				"Allow texts if more than 3 are received from the " +
 				"same person within 2 minutes",
 				"Allow phone calls if more than 1 are received from the " +
 				"same person within 2 minutes",
 				"Allow e-mail if on a starred thread"
 		};
 		ArrayList<String> rulesList = new ArrayList<String>();
 		rulesList.addAll(Arrays.asList(rules));
 		rulesListAdapter = 
 				new ArrayAdapter<String>(this, R.layout.settings_row, rulesList);
 		rulesListView.setAdapter(rulesListAdapter);
 		
 		// Populate list of upcoming calendar events to be silenced during
 		// TODO populate this from gcal
 		String[] events = new String[] {
 				"PennApps demos",
 				"Daily Pennsylvanian board meeting",
 				"CIS 121"
 		};
 		ArrayList<String> eventsList = new ArrayList<String>();
 		eventsList.addAll(Arrays.asList(events));
 		eventsListAdapter = 
 				new ArrayAdapter<String>(this, R.layout.settings_row, eventsList);
 		eventsListView.setAdapter(eventsListAdapter);
 	}
 }

