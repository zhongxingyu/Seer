 package b.reader;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.view.Menu;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	Globals g;
 	TextView tv;
 	WebView wv;
 	Button prev;
 	Button next;
 	Button star;
 	int storyIndex;
 	List<Story> stories;
 	String bookmark;
 	boolean more;
 	int unreadCount;
 	HashMap<String, Boolean> readStories;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		g = ((Globals) this.getApplication());
 		storyIndex = -1;
 		more = false;
 		unreadCount = 0;
 		readStories = new HashMap<String, Boolean>();
 		stories = new ArrayList<Story>();
 		tv = (TextView) findViewById(R.id.main_text);
 		wv = (WebView) findViewById(R.id.webView1);
 		prev = (Button) findViewById(R.id.previous);
 		prev.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				doPrevious();
 			}
 		});
 		next = (Button) findViewById(R.id.next);
 		next.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				doNext();
 			}
 		});
 		star = (Button) findViewById(R.id.star);
 		star.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				doStar();
 			}
 		});
 		getAccount();
 		getStories();
 		wv.loadData("<center>Click next to begin</center>", "text/html", null);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	private void getAccount() {
 		final Context c = this;
 		class SendPostReqAsyncTask extends AsyncTask<String, Void, JSONObject>{
 			ProgressDialog progressDialog;
 
 			@Override
 			protected void onPreExecute(){
 				progressDialog = ProgressDialog.show(c, "", "Networking shit...", false);
 				super.onPreExecute();
 			}
 
 			@Override
 			protected JSONObject doInBackground(String... p) {
 				Map<String, String> args = new HashMap<String, String>();
 				args.put("api_key", g.getApiKey());
 				return g.doPostSingle("/api/account", args);
 			}
 
 			@Override
 			protected void onPostExecute(JSONObject result) {
 				try{
 					String count = result.getString("unread_count");
 					unreadCount = Integer.parseInt(count);
 					updateCount();
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 				progressDialog.dismiss();
 				super.onPostExecute(result);
 			}           
 		}
 
 		SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
 		sendPostReqAsyncTask.execute();
 	}
 	
 	private void updateCount() {
 		String c = "" + unreadCount;
 		tv.setText(c + " unread");
 	}
 
 	private void getStories() {
 		final Context c = this;
 		class SendPostReqAsyncTask extends AsyncTask<String, Void, JSONObject>{
 			ProgressDialog progressDialog;
 
 			@Override
 			protected void onPreExecute(){
 				progressDialog = ProgressDialog.show(c, "", "Networking shit...", false);
 				super.onPreExecute();
 			}
 
 			@Override
 			protected JSONObject doInBackground(String... p) {
 				Map<String, String> args = new HashMap<String, String>();
 				args.put("api_key", g.getApiKey());
 				if (bookmark != null) {
 					args.put("bookmark", bookmark);
 				}
 				return g.doPostSingle("/api/next", args);
 			}
 
 			@Override
 			protected void onPostExecute(JSONObject result) {
 				try{
 					bookmark = result.getString("bookmark");
 					more = result.getBoolean("more");
 					JSONArray jArray = result.getJSONArray("stories");
 					JSONObject row = null;
 					for (int i = 0; i < jArray.length(); i++) {
 						row = jArray.getJSONObject(i);
 						Story s = new Story(row);
 						stories.add(s);
 					}
 					progressDialog.dismiss();
 					super.onPostExecute(result);
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}           
 		}
 
 		SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
 		sendPostReqAsyncTask.execute();
 	}
 	
 	private void markRead(String key) {
 		final Toast fuckedUp = Toast.makeText(this, "Error marking post read", Toast.LENGTH_LONG);
 		final Context c = this;
 		final String f_key = key;
 		class SendPostReqAsyncTask extends AsyncTask<String, Void, JSONObject>{
 			ProgressDialog progressDialog;
 
 			@Override
 			protected void onPreExecute(){
 				progressDialog = ProgressDialog.show(c, "", "Networking shit...", false);
 				super.onPreExecute();
 			}
 
 			@Override
 			protected JSONObject doInBackground(String... p) {
 				Map<String, String> args = new HashMap<String, String>();
 				args.put("api_key", g.getApiKey());
 				args.put("key", f_key);
 				return g.doPostSingle("/api/read", args);
 			}
 
 			@Override
 			protected void onPostExecute(JSONObject result) {
 				try{
 					String status = result.getString("status");
 					if (!status.equals("OK")) {
 						fuckedUp.show();
 					}
 					progressDialog.dismiss();
 					super.onPostExecute(result);
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}           
 		}
 
 		SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
 		sendPostReqAsyncTask.execute();
 	}
 	
 	private void doPrevious() {
 		storyIndex--;
 		if (storyIndex >= 0) {
 			wv.loadData(stories.get(storyIndex).description, "text/html", null);
 		} else {
 			wv.loadData("Beginning of list", "text/html", null);
 		}
 		if (storyIndex < -1) {
 			storyIndex = -1;
 		}
 	}
 
 	private void doNext() {
 		storyIndex++;
 		if ((storyIndex + 1) < stories.size()) {
 			Story s = stories.get(storyIndex);
 			wv.loadData(formatStory(s), "text/html", null);
 			if (!(readStories.containsKey(s.key))) {
 				unreadCount--;
 				readStories.put(s.key, true);
 				markRead(stories.get(storyIndex).key);
 				if (more) {
 					if ((storyIndex + 8) > stories.size()) {
 						getStories();
 					}
 				}
 			}
 			if (unreadCount < 0) {
 				unreadCount = 0;
 			}
 			updateCount();
 		} else {
 			wv.loadData("End of list", "text/html", null);
 		}
 		if (storyIndex >= stories.size()) {
 			storyIndex = stories.size();
 		}
 	}
 
 	private void doStar() {
 		if (storyIndex == -1) {
 			return;
 		}
		final Toast fuckedUp = Toast.makeText(this, "Error marking post read", Toast.LENGTH_LONG);
 		final Toast done = Toast.makeText(this, "Starred", Toast.LENGTH_SHORT);
 		final Context c = this;
 		class SendPostReqAsyncTask extends AsyncTask<String, Void, JSONObject>{
 			ProgressDialog progressDialog;
 
 			@Override
 			protected void onPreExecute(){
 				progressDialog = ProgressDialog.show(c, "", "Networking shit...", false);
 				super.onPreExecute();
 			}
 
 			@Override
 			protected JSONObject doInBackground(String... p) {
 				Map<String, String> args = new HashMap<String, String>();
 				args.put("api_key", g.getApiKey());
 				args.put("key", stories.get(storyIndex).key);
 				return g.doPostSingle("/api/mark", args);
 			}
 
 			@Override
 			protected void onPostExecute(JSONObject result) {
 				try{
 					progressDialog.dismiss();
 					String status = result.getString("status");
 					if (status.equals("OK")) {
 						done.show();
 					}
 					else{
 						fuckedUp.show();
 					}
 					super.onPostExecute(result);
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}           
 		}
 
 		SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
 		sendPostReqAsyncTask.execute();
 	}
 	
 	private String formatStory(Story s) {
 		String html = "<h3><a href='" + s.link + "'>" + s.title + "</a></h3>";
 		html += "<p><small><a href='" + s.feed_url + "'>" + s.feed_name
 				+ "</a> on " + s.pub_date + "</small></p>";
 		html += s.description;
 		return html;
 	}
 
 }
