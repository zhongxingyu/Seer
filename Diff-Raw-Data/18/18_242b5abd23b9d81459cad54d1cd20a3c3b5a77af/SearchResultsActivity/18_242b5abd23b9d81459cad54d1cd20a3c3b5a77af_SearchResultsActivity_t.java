 
 package org.walkingarchive.app;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
import org.walkingarchive.app.ui.SearchResult;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class SearchResultsActivity extends Activity {
 	ListView resultsListView;
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.search_result);
 
         String resultString = getIntent().getExtras().getString("resultString");
         JSONArray json;
 		try
 		{
 			json = new JSONArray(resultString);
 		}
 		catch(JSONException e)
 		{
 			// Hmm...
 			return;
 		}
 		
         resultsListView = (ListView) findViewById(R.id.resultsListView);
        ArrayList<SearchResult> listViewArray = new ArrayList<SearchResult>();
         for(int i = 0; i < json.length(); i++)
         {
         	JSONObject cardJson;
         	try
         	{
 				cardJson = json.getJSONObject(i);
 			}
         	catch (JSONException e)
         	{
         		// TODO: Log/whatever this
         		continue;
 			}
        	listViewArray.add(new SearchResult(cardJson));
         }
         
         // Use some arbitrary TextView as a template
        resultsListView.setAdapter(new ArrayAdapter<SearchResult>(this, android.R.layout.simple_list_item_1, listViewArray));
         
         resultsListView.setOnItemClickListener(
 			new OnItemClickListener()
 			{
 				public void onItemClick(AdapterView<?> adapter, View view, int position, long id)
 				{
					SearchResult result = (SearchResult) adapter.getItemAtPosition(position);
 					Intent cardViewerIntent = new Intent(SearchResultsActivity.this, CardViewerActivity.class);
					cardViewerIntent.putExtra("cardJson", result.toJson());
 			    	SearchResultsActivity.this.startActivity(cardViewerIntent);
 				}
 			});
     }
 	
 	
 }
