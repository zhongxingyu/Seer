 package iftode.bogdan.m8events;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 
 public class EventsListActivity extends Activity {
 
 	//The ProgressDialog and the reference to the background task
 	private ProgressDialog pd;
 	private AsyncTask<?, ?, ?> PopulateEventsListViewTask = null;
 	
 	//This code is executed when the Activity loses focus 
 	@Override
 	public void onStop() {
 	    super.onStop();  // Always call the superclass method first
 
 		//Dismiss the ProgressDialog and kill the background task
	    pd.dismiss();
 	    if(PopulateEventsListViewTask != null)
 	    	PopulateEventsListViewTask.cancel(true);
 	}
 	
 	//This code is executed when the Activity is first created on when it comes back to the foreground
 	@Override
 	public void onStart() {
 	    super.onStart();  // Always call the superclass method first
 	    
 	    drawUI();
 	}
 	
 	//This code is executed when the Activity is first created
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_events_list);
 	}
 	
 	//This method draws the UI
 	private void drawUI() {
 	    //Check if we have network connection
 	    ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
 	    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 	    if (networkInfo != null && networkInfo.isConnected()) {
 	    	//If we have then show a progress dialog and run a background thread which attempts to
 	    	//connect to the remote server and populate our ListView with data from an XML file
         	pd = ProgressDialog.show(
         			this,
         			"",
         			getString(R.string.progress_dialog_loading_message),
         			true,
         			true);
         	pd.setOnCancelListener(new DialogInterface.OnCancelListener(){
                 @Override
                 public void onCancel(DialogInterface dialog){
                 	if(PopulateEventsListViewTask != null)
             	    	PopulateEventsListViewTask.cancel(true);
                 	finish();
                 }
             });
             PopulateEventsListViewTask = new PopulateEventsListViewAsyncTask().execute();
 	    } else {
 	    	//If not then show an AlertDialog and finish() the Activity
 	    	new AlertDialog.Builder(this).setMessage(getString(R.string.alert_dialog_message)) 
 	    	.setTitle(getString(R.string.alert_dialog_title)) 
 	    	.setCancelable(true) 
 	    	.setNeutralButton(android.R.string.ok, 
 	    	new DialogInterface.OnClickListener() { 
 		    	public void onClick(DialogInterface dialog, int whichButton){
 		    		finish();
 		    	} 
 	    	}) 
 	    	.show();
 	    }
 
 	}
 	
 	//This class extends AsyncTask in order to run in a background thread
 	private class PopulateEventsListViewAsyncTask extends AsyncTask<Object, Object, Object> {
 
 		//This code is executed when execute() is called on this object
 		@Override
 		protected Object doInBackground(Object... params) {
 			//Attempt to get the XML file from the remote server
 			String xml = XmlFunctions.getXML(getString(R.string.eventslist_xml_url));
 			return (Object)xml;
 		}
 		
 		//This code is executed after execute() has finished
 		@Override
         protected void onPostExecute(Object result) {
 			//Attempt to populate the ListView using the XML file we got earlier
 			populateEventsListView((String)result);
 			//The ProgressDialog can now be dismissed
			pd.dismiss();
        }
 	}
 	
 	//This method attempts to populate our ListView using the XML file it gets as a parameter
 	private void populateEventsListView(String xml) {
 		//Attempt to parse the XML file
 		List<Map<String, String>> eventsList = getEventsList(xml);
 		//If unsuccessful then return
 		if(eventsList == null)
 			return;
 		
 		//Else start populating the ListView
 		SimpleAdapter adapter = new SimpleAdapter(this, eventsList,
                 android.R.layout.simple_list_item_2,
                 new String[] {"title", "date"},
                 new int[] {android.R.id.text1,
                            android.R.id.text2});
     	
     	ListView listView = (ListView) findViewById(R.id.eventsList);
     	listView.setAdapter(adapter);
 
     	//This will handle clicking the items in the ListView
     	listView.setOnItemClickListener(mMessageClickedHandler);
 	}
 	
 	//This method attempts to parse the XML file
 	private List<Map<String, String>> getEventsList(String xml) {		
 		//In case of bad URL
         if(xml == null) {
             Toast toast = Toast.makeText(
             		getApplicationContext(),
             		getString(R.string.toast_xml_bad_url),
             		Toast.LENGTH_LONG);
             toast.show();
         	return null;
         }
         
       //In case of empty file
         if(xml.length() == 0) {
             Toast toast = Toast.makeText(
             		getApplicationContext(),
             		getString(R.string.toast_xml_empty),
             		Toast.LENGTH_LONG);
             toast.show();
         	return null;
         }
         
 		Document doc = XmlFunctions.XMLfromString(xml);
 		
 		//In case of parse error
         if(doc == null) {
             Toast toast = Toast.makeText(
             		getApplicationContext(),
             		getString(R.string.toast_xml_parse_error),
             		Toast.LENGTH_LONG);
             toast.show();
         	return null;
         }
         
         NodeList nodes = doc.getElementsByTagName("Event");
         
         //In case of 0 events
         if(nodes.getLength() == 0)
 		{
 			Toast toast = Toast.makeText(
 					getApplicationContext(),
 					getString(R.string.toast_no_events),
 					Toast.LENGTH_LONG);
 	    	toast.show();
 	    	finish();
 		}
 
   		//Fill in the list items from the XML document
 		List<Map<String, String>> eventsList = new ArrayList<Map<String, String>>();
 		for(int i = 0; i < nodes.getLength(); i++)
     	{
 			Element e = (Element)nodes.item(i);
     		Map<String, String> event = new HashMap<String, String>(3);
     		event.put("eid", XmlFunctions.getValue(e, "EventID"));
     	    event.put("title", XmlFunctions.getValue(e, "Title"));
     	    event.put("date", XmlFunctions.getValue(e, "Date"));
     	    eventsList.add(event);
     	}
 		
 		return eventsList;
 	}
 	
 	// Create a message handling object as an anonymous class.
 	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
 	    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 	    	Toast toast = Toast.makeText(
 	    			getApplicationContext(),
 	    			(CharSequence)("Clicked " + ((HashMap<String,String>)parent.getItemAtPosition(position)).get("eid")),
 	    			Toast.LENGTH_SHORT);
 	    	toast.show();
 	    }
 	};
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_events_list, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	        case R.id.menu_refresh:
 	            drawUI();
 	            return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 }
