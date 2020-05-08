 package com.sample.redditapp;
 
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.android.volley.Request;
 import com.android.volley.RequestQueue;
 import com.android.volley.Response;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.JsonObjectRequest;
 import com.android.volley.toolbox.Volley;
 
 public class RedditMainList extends SherlockFragmentActivity {
 	// final se usa para decirle al metodo que ese objeto no va a cambiar como
 	// las firmas anonimas como la de los listeners abajo
 	private RedditItemAdapter adapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_reddit_main_list);
 
 		ListView list = (ListView) findViewById(R.id.list);
 		adapter = new RedditItemAdapter(this);
 		list.setAdapter(adapter);
 
 		RequestQueue queue = Volley.newRequestQueue(this);
 
 		String url = "http://www.reddit.com/r/funny/.json";
 
 		JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
 				url, null, new Response.Listener<JSONObject>() {
 
 					@Override
 					public void onResponse(JSONObject response) {
 						try {
 
							List<RedditItem> items = Processor.process(response);
 							// actualizando items que obtenemos y asignando al
 							// adapter
 							adapter.setItems(items);
 							Log.d("OCFS - Fine", "list: " + items);
 
 						} catch (JSONException e) {
 
 							e.printStackTrace();
 						}
 
 					}
 				}, new Response.ErrorListener() {
 
 					@Override
 					public void onErrorResponse(VolleyError error) {
 						Log.d("OCFS - Error",
 								"This error happend: "
 										+ error.getLocalizedMessage());
 						Toast.makeText(RedditMainList.this, "Error happened",
 								Toast.LENGTH_LONG).show();
 					}
 				});
 
 		queue.add(request);
 
 	}
 }
