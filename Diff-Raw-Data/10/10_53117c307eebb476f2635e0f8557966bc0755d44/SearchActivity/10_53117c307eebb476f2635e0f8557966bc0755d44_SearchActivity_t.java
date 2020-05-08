 package edu.berkeley.cs160.billiterate;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.SpannableString;
 import android.text.style.UnderlineSpan;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class SearchActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_search);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_search, menu);
 		return true;
 	}
 	
 	public void search(View view) {
 		System.out.println("You just clicked SEARCH");
 		String searchText = ((EditText) findViewById(R.id.search_box)).getText().toString();
 		if (searchText == null) {
 			return;
 		}
 		
 		SearchTask search = new SearchTask(this);
 		search.execute(searchText);
 		
 	}
 	
 	private class BillClickListener implements OnClickListener {
 
 		Context context;
 		String title;
 		String summary;
 		String rep;
 		int id;
 
 		public BillClickListener(Context context, String title,
 				String summary, String rep, int id) {
 			this.context = context;
 			this.title = title;
 			this.summary = summary;
 			this.rep = rep;
 			this.id = id;
 		}
 
 		@Override
 		public void onClick(View v) {
 			Intent i = new Intent(this.context, BillInfoActivity.class);
 			i.putExtra("title", this.title);
 			i.putExtra("summary", this.summary);
 			i.putExtra("representative", this.rep);
 			i.putExtra("id", this.id);
 			startActivity(i);
 		}
 
 	}
 
 	public class SearchTask extends AsyncTask<String, Void, JSONArray> {
 
 		Context context;
 		
 		public SearchTask(Context context) {
 			this.context = context;
 		}
 		
 		@Override
 		protected JSONArray doInBackground(String... params) {
			String search = params[0];
			search = search.replace(' ', '+');
			System.out.println("Search term = " + search);
			String url = "http://billiterate.pythonanywhere.com/billapp/search/?term=" + search;
 			System.out.println("searching at " + url);
 			HttpResponse response;
 			String responseString = "";
 			HttpClient client = new DefaultHttpClient();
 			try {
 				response = client.execute(new HttpGet(url));
 				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 					ByteArrayOutputStream out = new ByteArrayOutputStream();
 					response.getEntity().writeTo(out);
 					out.close();
 					responseString = out.toString();
 				} else {
 					response.getEntity().getContent().close();
 				}
 			} catch (ClientProtocolException cpe) {
 				cpe.printStackTrace();
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 			try {
 				System.out.println(responseString);
 				JSONArray messages = new JSONArray(responseString);
 				return messages;
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 		
 		protected void onPostExecute(JSONArray messageList) {
 			LinearLayout results = (LinearLayout) findViewById(R.id.search_results);
 			results.removeAllViews();
 			
			if (messageList.length() == 0) {
 				TextView tv = new TextView(this.context);
 				tv.setText("There are no results that match your search terms");
				tv.setTextSize(25f);
 				results.addView(tv);
 			} else {
 				for (int i = 0; i < messageList.length(); i++) {
 					String billTitle ="";
 					String summary = "";
 					String rep = "";
 					int id = 0;
 					try {
 						JSONObject current = messageList.getJSONObject(i);
 						id = current.getInt("pk");
 						JSONObject fields = current.getJSONObject("fields");
 						billTitle = fields.getString("title");
 						summary = fields.getString("summary");
 						rep = fields.getString("representative");
 					} catch (JSONException e) {
 						System.err.println(messageList.toString());
 						e.printStackTrace();
 					}
 					TextView result = (TextView) LayoutInflater.from(
 							SearchActivity.this).inflate(
 							R.layout.search_result, results, false);
 					SpannableString ssTitle = new SpannableString(billTitle);
 					ssTitle.setSpan(new UnderlineSpan(), 0, ssTitle.length(), 0);
 					result.setText(billTitle);
 					result.setPadding(10, 10, 10, 10);
 					result.setOnClickListener(new BillClickListener(this.context, billTitle, summary, rep, id));
 					results.addView(result);
 				}
 			}
 		}
 		
 	}
 
 }
