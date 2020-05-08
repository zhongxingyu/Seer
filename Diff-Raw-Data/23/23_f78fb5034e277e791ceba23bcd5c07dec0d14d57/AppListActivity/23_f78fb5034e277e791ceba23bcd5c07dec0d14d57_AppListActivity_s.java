 package edu.channel4.mm.db.android.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.salesforce.androidsdk.app.ForceApp;
 import com.salesforce.androidsdk.rest.ClientManager.LoginOptions;
 import com.salesforce.androidsdk.rest.RestClient;
 import com.salesforce.androidsdk.ui.NativeMainActivity;
 
 import edu.channel4.mm.db.android.R;
 import edu.channel4.mm.db.android.callback.AppDescriptionCallback;
 import edu.channel4.mm.db.android.model.description.AppDescription;
 import edu.channel4.mm.db.android.network.RestClientAccess;
 import edu.channel4.mm.db.android.network.SalesforceConn;
 import edu.channel4.mm.db.android.util.Keys;
 import edu.channel4.mm.db.android.util.Log;
 
 /**
  * Main activity. For us, this is functions as what the old AppListActivity did,
  * with LoginActivity built in.
  */
 public class AppListActivity extends NativeMainActivity implements
          AppDescriptionCallback {
 
    protected ListView listViewAppList;
 
    protected List<AppDescription> appList;
    protected AppDataArrayAdapater arrayAdapter;
    protected SalesforceConn sfConn;
 
    protected ProgressBar progressBar;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_app_list);
 
       // Initialization
       sfConn = new SalesforceConn(getApplicationContext());
 
       // Fill up appList
       appList = new ArrayList<AppDescription>();
 
       // Setup ListView
       listViewAppList = (ListView) findViewById(R.id.listViewAppList);
       arrayAdapter = new AppDataArrayAdapater(this, R.id.cellAppList);
       listViewAppList
                .setOnItemClickListener(new AdapterView.OnItemClickListener(){
                   @Override
                   // When app clicked, start Dashboard for that app.
                            public
                            void onItemClick(AdapterView<?> parent, View view,
                                     int position, long id) {
                      Intent intent = new Intent(getApplicationContext(),
                               DashboardActivity.class);
 
                      // Place chosen App Label into SharedPrefs for later
                      // access
                      SharedPreferences.Editor editor = getSharedPreferences(
                               Keys.PREFS_NS, 0).edit();
                      editor.putString(Keys.APP_LABEL, appList.get(position)
                               .getAppName());
                      editor.commit();
 
                      startActivity(intent);
                   }
                });
 
       // Hook up the array adapter to the ListView
       listViewAppList.setAdapter(arrayAdapter);
 
       // Reference the ProgressBar so we can turn it off later
       progressBar = (ProgressBar) findViewById(R.id.progressBarAppList);
    }
 
    /**
     * Don't use Android's regular onResume() in this class. Instead, use this
     * onResume(). This one comes from Salesforce's native Android SDK and gives
     * you the {@link RestClient} object exactly once.
     */
    @Override
    public void onResume(RestClient client) {
       // Save a reference to the rest client for use throughout the app
       RestClientAccess.getInstance().setRestClient(client);
 
      // Retrieve the app list from Salesforce
       getAppList(null);
    }
 
    @Override
    protected LoginOptions getLoginOptions() {
       LoginOptions loginOptions = new LoginOptions(
                null, // login host is chosen by user through the server picker
                ForceApp.APP.getPasscodeHash(),
                getString(R.string.oauth_callback_url),
                getString(R.string.oauth_client_id), new String[] {"api"});
       return loginOptions;
    }
 
    private class AppDataArrayAdapater extends ArrayAdapter<AppDescription> {
 
       public AppDataArrayAdapater(Context context, int textViewResourceId) {
          super(context, textViewResourceId, appList);
       }
 
       @Override
       public View getView(int position, View convertView, ViewGroup parent) {
 
          if (appList == null) {
             Log.e("appList List is null: will not draw cell");
             return convertView;
          }
 
          // Grab the pertinent AppData object
          AppDescription appData = appList.get(position);
 
          // Inflate the cell
          LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                   .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
          convertView = inflater.inflate(R.layout.cell_app_list, null);
 
          // Reference the Views
          TextView appName = (TextView) convertView
                   .findViewById(R.id.textViewAppName);
          TextView packageName = (TextView) convertView
                   .findViewById(R.id.textViewPackageName);
 
          // Modify their text.
          appName.setText(appData.getAppName());
          packageName.setText(appData.getPackageName());
 
          return convertView;
       }
    }
 
    public void getAppList(View v) {
       Log.d("Retrieving app list via network");
       sfConn.getAppListViaNetwork(this);
    }
 
    public void about(View v) {
       startActivity(new Intent(getApplicationContext(), AboutActivity.class));
    }
 
    public void logout(View v) {
       ForceApp.APP.logout(this);
    }
 
    @Override
    public void onAppDescriptionChanged(List<AppDescription> newAppDescriptions) {
       // Hide the ProgressBar
       progressBar.setVisibility(View.GONE);
 
       // Sanity check, then update the ArrayAdapter and its array
       if (newAppDescriptions != null) {
          appList.clear();
          appList.addAll(newAppDescriptions);
          arrayAdapter.notifyDataSetChanged();
       }
       else {
          Toast.makeText(getApplicationContext(), "Null app list...",
                   Toast.LENGTH_SHORT).show();
       }
    }
 }
