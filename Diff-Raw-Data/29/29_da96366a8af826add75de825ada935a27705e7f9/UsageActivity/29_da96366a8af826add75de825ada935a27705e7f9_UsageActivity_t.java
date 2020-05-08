 package edu.channel4.mm.db.android.activity;
 
 import java.util.ArrayList;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.ContentView;
 import roboguice.inject.InjectResource;
 import roboguice.inject.InjectView;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.google.inject.Inject;
 
 import edu.channel4.mm.db.android.R;
 import edu.channel4.mm.db.android.model.request.CustomGraphRequest;
 import edu.channel4.mm.db.android.model.request.EventOverTimeGraphRequest;
 import edu.channel4.mm.db.android.model.request.GraphRequest;
 import edu.channel4.mm.db.android.model.request.SessionOverTimeGraphRequest;
 import edu.channel4.mm.db.android.util.Keys;
 import edu.channel4.mm.db.android.util.Log;
 import edu.channel4.mm.db.android.view.GraphRequestArrayAdapter;
 
 @ContentView(R.layout.activity_usage)
 public class UsageActivity extends RoboActivity {
 
    @InjectView(R.id.listviewUsageActivity) private ListView listView;
    @InjectView(R.id.usageHeader) private TextView textViewUsageHeader;
    @InjectResource(R.string.usage) private String activityTitle;
    @Inject private ArrayList<GraphRequest> graphRequests;
    @Inject private SharedPreferences prefs;
    private GraphRequestArrayAdapter adapter;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       
       // Hide the action bar 
       getActionBar().hide();
 
       // Set the activity's titles
       String appLabel = prefs.getString(Keys.APP_LABEL, null);
       textViewUsageHeader.setText(appLabel);
       setTitle(activityTitle);
 
       listView.setOnItemClickListener(new OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view,
                   int position, long id) {
 
             // Grab the GraphRequest for the selected item.
             GraphRequest graphRequest = graphRequests.get(position);
 
             // Construct the correct Intent for the selected
             // UsageGraphRequest
             Intent intent = graphRequest
                      .constructGraphRequestIntent(getApplicationContext());
 
             // Try to start the activity
             if (intent != null) {
                startActivity(intent);
             }
             else {
                Log.toastD(getApplicationContext(), graphRequest.toString()
                         + " not yet implemented.");
             }
          }
       });
 
       if (graphRequests.isEmpty()) {
          fillGraphRequests();
       }
 
       adapter = new GraphRequestArrayAdapter(getApplicationContext(),
                graphRequests.toArray(new GraphRequest[0]));
 
       listView.setAdapter(adapter);
    }
 
    private void fillGraphRequests() {
       graphRequests.add(new SessionOverTimeGraphRequest());
       graphRequests.add(new EventOverTimeGraphRequest());
      graphRequests.add(new CustomGraphRequest("Session Length", "", "sessionLengthSeconds", R.drawable.ic_launcher));
    }
 }
