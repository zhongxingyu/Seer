 package tesla.app.ui;
 
import java.util.ArrayList;
import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import tesla.app.R;
 import tesla.app.command.provider.AppConfigProvider;
 import tesla.app.service.connect.ConnectionOptions;
 import android.app.Activity;
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 public class AppSelector extends ListActivity {
 
 	private List<Map<String, String>> providerList;
 	private ConnectionOptions config;
 	
 	
 	
 	protected void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         setContentView(R.layout.app_selector);
         
         providerList = AppConfigProvider.getAppDirectory();
 
         ListAdapter providerSelector = new SimpleAdapter(
         		this, providerList, R.layout.app_selector_entry, 
         		new String[] { "icon", "name" }, 
         		new int[] { R.id.selector_app_icon, R.id.selector_app_name });
         setListAdapter(providerSelector);
         
         config = new ConnectionOptions(this);
         
         if (!config.appSelection.equals("")) {
         	// Load the last saved settings
         	setSelection(findAppMatchingName(config.appSelection));
         }
         else {
     		// Default to Rhythmbox
         	setSelection(findAppMatchingName(AppConfigProvider.APP_RHYTHMBOX));
         }
     }
 
 	private int findAppMatchingName(String appSelection) {
 		Iterator<Map<String, String>> it = providerList.iterator();
 		boolean found = false;
 		int out = -1;
 		while (it.hasNext() && !found) {
 			out += 1;
 			Map<String, String> current = it.next();
 			if (current.get("ref").equalsIgnoreCase(appSelection)) {
 				found = true;
 			}
 		}
 		if (found == false) out = -1;
 		return out;
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Map<String, String> item = (Map<String, String>) getListAdapter().getItem(position);
 		config.appSelection = item.get("ref");
 		config.saveSettings();
 		setResult(Activity.RESULT_OK);
 		finish();
 	}
 }
