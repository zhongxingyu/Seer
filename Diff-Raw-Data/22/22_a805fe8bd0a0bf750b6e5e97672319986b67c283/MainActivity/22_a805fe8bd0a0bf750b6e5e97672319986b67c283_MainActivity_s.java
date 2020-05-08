 package com.jsanc623.adclick.prototype;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class MainActivity extends ListActivity {
 	@SuppressWarnings("unused")
 	private ListView LV;
 	static final private String sURL = "http://git.juanleonardosanchez.com/AdClick-Prototype/Service.php";
 	
     static final String[] PENS = new String[]{
     	"MONT Blanc", "Gucci", "Parker", "Sailor",
     	"Porsche Design", "Rotring", "Sheaffer", "Waterman"
     };
     
     @Override
     protected void onCreate(Bundle savedInstanceState){
     	super.onCreate(savedInstanceState);
     	
     	// change to our configure view
     	setContentView(R.layout.activity_main);
     	
     	Toast.makeText(getApplicationContext(), "Loading your settings.", Toast.LENGTH_SHORT).show();
    	new RequestTask().execute(MainActivity.sURL);
     	
    	LV = (ListView)findViewById(android.R.id.list);
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, PENS));
        getListView().setTextFilterEnabled(true);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     protected void onListItemClick(ListView L, View V, int Position, long ID) {
     	// get clicked item on list and it's url
     	super.onListItemClick(L, V, Position, ID);
     	Object O = this.getListAdapter().getItem(Position);
     	String url = O.toString();
     	
     	// Send to browser via intent
     	Intent i = new Intent(Intent.ACTION_VIEW);
     	i.setData(Uri.parse(url));
     	startActivity(i);
     }
 }
 
 class RequestTask extends AsyncTask<String, String, String>{
     @Override
     protected String doInBackground(String... uri) {
         HttpClient httpclient = new DefaultHttpClient();
         HttpResponse response;
         String responseString = null;
         try {
             response = httpclient.execute(new HttpGet(uri[0]));
             StatusLine statusLine = response.getStatusLine();
             if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                 ByteArrayOutputStream out = new ByteArrayOutputStream();
                 response.getEntity().writeTo(out);
                 out.close();
                 responseString = out.toString();
             } else{
                 //Closes the connection.
                 response.getEntity().getContent().close();
                 throw new IOException(statusLine.getReasonPhrase());
             }
         } catch (ClientProtocolException e) {
             //TODO Handle problems..
         } catch (IOException e) {
             //TODO Handle problems..
         }
         return responseString;
     }
 
     @Override
     protected void onPostExecute(String result) {
         super.onPostExecute(result);
         //Do anything with response..
     }
 }
