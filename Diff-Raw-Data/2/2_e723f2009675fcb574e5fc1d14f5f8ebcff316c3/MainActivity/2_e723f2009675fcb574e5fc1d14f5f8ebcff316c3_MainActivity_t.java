 package com.oanda.APISample;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class MainActivity extends Activity {
 
     String baseURLString = "http://api-sandbox.oanda.com/";
     GetInstrumentsTask instrumentsTask;
     GetInstrumentPriceTask priceTask;
     ProgressDialog dialog;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         Button refreshButton = (Button) findViewById(R.id.refresh);
         refreshButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 instrumentsTask.cancel(true);
                 priceTask.cancel(true);
                 startInstrumentsTask();
 
             }
         });
 
         startInstrumentsTask();
     }
 
     private void startInstrumentsTask() {
         dialog = ProgressDialog.show(MainActivity.this, "",
                 "Loading. Please wait...", true);
         instrumentsTask = new GetInstrumentsTask();
         instrumentsTask.execute((Void) null);
     }
 
     private void initializeListView(List<String> data) {
         dialog.dismiss();
         ListView listView = (ListView) findViewById(R.id.listview);
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                 R.layout.row, data);
         listView.setAdapter(adapter);
     }
 
     private JSONArray httpGetCall(String urlString, String jsonArrayName) {
         JSONArray result = null;
         HttpClient client = new DefaultHttpClient();
         HttpGet request = new HttpGet(urlString);
         try {
             HttpResponse getResponse = client.execute(request);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(
                             getResponse.getEntity().getContent(), "UTF-8"));
             StringBuilder builder = new StringBuilder();
             for (String line = null; (line = reader.readLine()) != null;) {
                 builder.append(line).append("\n");
             }
             result = new JSONObject(builder.toString()).getJSONArray(jsonArrayName);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return result;
     }
 
     public class GetInstrumentPriceTask extends
             AsyncTask<Void, Void, JSONArray> {
         String urlString = baseURLString + "v1/quote?instruments=";
         String pair;
 
         public GetInstrumentPriceTask(List<String> pairs) {
  
 /*
 Commented out because instruments returns invalid pairs for quote (fix in progress)
             StringBuffer buffer = new StringBuffer();
             for (String pair : pairs) {
                 buffer.append(pair).append("%2C");
             }
             urlString += buffer.toString();
 */
            urlString += "EUR_USD%2CEUR_CAD%2CUSD_CAD%2CUSD_JPY%2CEUR_GBP%2CCAD_JPY%2CGBP_USD%2CGBP_CAD";
         }
 
 
 
         @Override
         protected JSONArray doInBackground(Void... arg0) {
             return httpGetCall(urlString, "prices");
         }
 
         @Override
         protected void onPostExecute(JSONArray result) {
             ArrayList<String> listCellText = new ArrayList<String>();
             try {
                 for (int i = 0; i < result.length(); i++) {
                     JSONObject obj = result.getJSONObject(i);
                     listCellText.add(obj.getString("instrument")
                             + "\nBid Price:"
                             + obj.getString("bid") + "\nAsk Price:"
                             + obj.getString("ask"));
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
             initializeListView(listCellText);
         }
     }
 
     public class GetInstrumentsTask extends AsyncTask<Void, Void, JSONArray> {
         String urlString = baseURLString + "v1/instruments";
 
         @Override
         protected JSONArray doInBackground(Void... arg0) {
             return httpGetCall(urlString, "instruments");
         }
         @Override
         protected void onPostExecute(JSONArray instrumentResult) {
             final ArrayList<String> instruments = new ArrayList<String>();
             for (int i = 0; i < instrumentResult.length(); i++) {
                 try {
                     JSONObject obj = instrumentResult.getJSONObject(i);
                     instruments.add(obj.getString("instrument"));
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             }
             priceTask = new GetInstrumentPriceTask(instruments);
             priceTask.execute((Void) null);
         }
     }
 }
