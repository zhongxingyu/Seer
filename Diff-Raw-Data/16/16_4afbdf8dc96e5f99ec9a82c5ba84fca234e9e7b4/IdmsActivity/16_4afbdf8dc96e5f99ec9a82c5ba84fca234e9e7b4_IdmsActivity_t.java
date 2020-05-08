 package autobahn.android;
 
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.*;
 import com.example.autobahn.R;
 
 import java.util.List;
 
 
 public class IdmsActivity extends BasicActiviy {
 
     private List<String> domains;
     private ListView domainList;
     private ArrayAdapter<String> adapter;
 
 
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         showData();
     }
 
     private void showData() {
 
         domains = NetCache.getInstance().getIdms();
 
         if (domains == null) {
             Log.d(TAG, "Domains is NULL...");
             return;
         }
 
         if (domains.isEmpty()) {
             Log.d(TAG, "Domains is Empty");
             setContentView(R.layout.no_data);
             ((TextView) findViewById(R.id.header)).setText(R.string.no_domains);
 
             Button menuButton = (Button) findViewById(R.id.menuButton);
             menuButton.setText(R.string.back_to_menu);
             menuButton.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     finish();
                 }
             });
         } else {
             setContentView(R.layout.domain_reservation_list);
            ((TextView) findViewById(R.id.header)).setText(R.string.idm_title);;
 
             domainList = (ListView) findViewById(R.id.listView);
             adapter = new ArrayAdapter<String>(this, R.layout.list_item, domains);
 
             domainList.setAdapter(adapter);
             domainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                 @Override
                 public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                     TextView item = (TextView) view;
                     String domain = item.getText().toString();
                     Intent domainActivity = new Intent();
                     domainActivity.setClass(getApplicationContext(), TrackCircuitActivity.class);
                     domainActivity.putExtra("DOMAIN_NAME", domain);
                     startActivity(domainActivity);
                 }
             });
         }
     }
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(TAG, "Creating Domain Selection Activity...");
         getData(Call.DOMAINS,null);
     }
 
     @Override
     protected synchronized void showData(Object data,Call c,String param) {
         showData();
     }
 
     @Override
     protected synchronized void showError(AutobahnClientException e,Call c,String param) {
         setContentView(R.layout.error_layout);
        ((TextView)findViewById(R.id.errorText)).setText(e.getMessage());
     }
 
 }
