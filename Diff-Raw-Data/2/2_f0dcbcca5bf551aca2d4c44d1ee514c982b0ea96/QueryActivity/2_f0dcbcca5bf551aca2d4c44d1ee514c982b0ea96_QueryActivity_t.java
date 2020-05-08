 package com.frontcast;
 
 import java.io.IOException;
 
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.api.client.http.GenericUrl;
 import com.google.api.client.http.HttpRequest;
 import com.google.api.client.http.HttpRequestFactory;
 import com.google.api.client.http.HttpRequestInitializer;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.apache.ApacheHttpTransport;
 import com.google.api.client.http.json.JsonHttpContent;
 import com.google.api.client.http.json.JsonHttpParser;
 import com.google.api.client.json.jackson.JacksonFactory;
 
 
 
 public class QueryActivity extends MapActivity {
 	
 	private static final String SERVER_URL = "http://frontcast-server.appspot.com/rpc";
 	private static final HttpTransport transport = new ApacheHttpTransport();
 	
 	private AutoCompleteTextView townsName;
 	private Button queryButton;
 	
 	@Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.query);
 		findViews();
 		setListeners();
 		townNameAutoCompelete();
 		
 		MapView mapView = (MapView) findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
 	}
 	
 	private void setListeners() {
 		queryButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Log.d("Send towns name", townsName.getText().toString());
 				new PostTownNameTask().execute();
 			}
     	});
 	}
 	
 	private void findViews() {
 		townsName = (AutoCompleteTextView) findViewById(R.id.location_input);
 		queryButton = (Button) findViewById(R.id.location_query_button);
 	}
 	
 	private void townNameAutoCompelete() {
 		String[] towns = getResources().getStringArray(R.array.towns_array);
 		ArrayAdapter<String> adapter = 
		        new ArrayAdapter<String>(this, R.layout.list_item, towns);
 		townsName.setAdapter(adapter);
 	}
 	
 	private class PostTownNameTask extends AsyncTask<Void, Void, Void> {
     	private ProgressDialog Dialog = new ProgressDialog(QueryActivity.this);
     	String message;
     	
     	@Override
     	protected void onPreExecute() {
     		super.onPreExecute();
     		Dialog.setMessage("Sending your query...");
     		Dialog.show();
     		
     		message = townsName.getText().toString();
     	}
     	
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			String[] data = {"PostTownName", message};
 			JsonHttpContent json = new JsonHttpContent(new JacksonFactory(), data);
 			
 			HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
 			HttpRequest request;
 			try {
 				request = httpRequestFactory.buildPostRequest(
 						new GenericUrl(SERVER_URL), json);
 			System.out.println("request = " + request.getUrl());
 			System.out.println("content = " + json.getData().toString());
 			String result = request.execute().parseAsString();
 			System.out.println("status: " + result);
 			return null;
 			} catch (IOException e) {
 				
 				e.printStackTrace();
 			}
 			return null;
 		}
     	
 		@Override
 		protected void onPostExecute(Void unused) {
 			Dialog.dismiss();
 			Toast.makeText(QueryActivity.this, "done!", Toast.LENGTH_LONG).show();
 		}
     }
 	
 	public static HttpRequestFactory createRequestFactory(final HttpTransport transport) {
 		   
 		return transport.createRequestFactory(new HttpRequestInitializer() {
 			public void initialize(HttpRequest request) {
 			    JsonHttpParser parser = new JsonHttpParser(new JacksonFactory());
 				request.addParser(parser);
 			}
 		});
 	}
 	
 }
