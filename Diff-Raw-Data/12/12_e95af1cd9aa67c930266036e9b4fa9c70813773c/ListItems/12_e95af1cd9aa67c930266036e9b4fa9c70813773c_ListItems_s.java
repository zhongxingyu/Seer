 package edu.ua.moundville;
 
 import java.util.ArrayList;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import edu.ua.moundville.DBHandler.DBResult;
 
 public class ListItems extends ListActivity implements DBResult {
 	
 	private static final String TAG = "ListItems";
 	protected String selectQuery;
 	protected ArrayList<String> items;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    
 	    DBHandler db = new DBHandler();
 	    ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
 	    nvp.add(new BasicNameValuePair("case", "8"));
 	    db.sendQuery(this, nvp);
 	
 	    setListAdapter(ArrayAdapter.createFromResource(getApplicationContext(), R.array.tut_titles, R.layout.simple_textview));
 	    
 	    getListView().setOnItemClickListener(new OnItemClickListener() {
 	    	 
 	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 	        	final String[] links = getResources().getStringArray(R.array.tut_links);
 	        	String content = links[position];
 	            Intent showContent = new Intent(getApplicationContext(), WebViewerActivity.class);
 	            showContent.setData(Uri.parse(content));
 	            startActivity(showContent);
 	        }
 	    });
 	}
 	
 	protected void getItems() {
 		
 	}
 	
 	protected void launchArticle() {
 		
 	}
 	
	public void recieveResult(JSONArray jArray) {
 	    Log.d(TAG, jArray.toString());
 	}
 }
