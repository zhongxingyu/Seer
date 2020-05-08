 package autobahn.android;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.*;
 import com.example.autobahn.R;
 
 import java.util.List;
 
 
 public class IdmsActivity extends Activity {
 
     List<String> domains;
     ListView domainList;
     ArrayAdapter<String> adapter;
 
     private void showData() {
         domains = AutobahnClient.getInstance().getIdms();
 
         if(exception != null) {
             Log.d("WARN","idms error");
             Toast toast  = Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG);
             toast.show();
         }
         if (domains.isEmpty()) {
             setContentView(R.layout.no_domains);
             Button menuButton = (Button) findViewById(R.id.menuButton);
             menuButton.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     Intent menu = new Intent();
                     menu.setClass(getApplicationContext(), MainMenu.class);
                     startActivity(menu);
                     finish();
                 }
             });
         } else {
             setContentView(R.layout.idm_selection_activity);
             domainList = (ListView) findViewById(R.id.listView);
             adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, domains);
             domainList.setAdapter(adapter);
             domainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                 @Override
                 public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                     TextView item = (TextView)view;
                     String domain  = item.getText().toString();
                     Intent domainActivity = new Intent();
                     domainActivity.setClass(getApplicationContext(),TrackCircuitActivity.class);
                     domainActivity.putExtra("DOMAIN_NAME",domain);
                     startActivity(domainActivity);
                 }
             });
 
         }
     }
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d("WARN","create");
         AsyncTask<Void, Void, Void> async = new AsyncTask<Void, Void, Void>() {
             @Override
             protected Void doInBackground(Void... type) {
 
                 try {
                     AutobahnClient.getInstance().fetchIdms();
                 } catch (AutobahnClientException e) {
                     exception=e;
                 }
                 return null;
             }
 
             @Override
             protected void onProgressUpdate(Void... progress) {
                 super.onProgressUpdate(progress);
             }
 
             @Override
             protected void onPostExecute(Void result) {
                 Log.d("WARN","Done Fetching Domains!");
                 showData();
             }
         };
         async.execute();
 
 
     }
 }
