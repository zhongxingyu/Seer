 package com.tamagohan.cloudnote_android;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 
 
 public class NoteListActivity extends ListActivity {
 	private JSONArray _jsonArr   = null;
 	private Context context      = this;
 	Bundle extras                = null;
 	AlertDialog.Builder builder  = null;
 	NoteListAdapter adapter      = null;
 	DefaultHttpClient httpClient = null;
 	MyCloudNote app              = null;
 
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d("activity", "onCreate of NoteListActivity was called.");
         extras  = getIntent().getExtras();
 
         builder = new AlertDialog.Builder(this);
         builder.setTitle(R.string.notification_dialog);
         builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
         	public void onClick(DialogInterface dialog, int which) {
         	setResult(RESULT_OK);
         	}
         });
         
         final Map<String, String> params = new HashMap<String, String>();
         params.put("page", "1");
         app = (MyCloudNote) this.getApplication();
         app.clearCurrentNotePage();
         httpClient = app.getHttpClient();
 	    exec_get("notes", params, httpClient);
 	}
 	
     // GETʐMs
     private void exec_get(String url, Map<String, String> params, final DefaultHttpClient httpClient) {
 		
 
         // 񓯊^XN`
         HttpGetTask task = new HttpGetTask(
             this,
             Constants.SERVER_URL + url,
 
             // ^XNɌĂ΂UĨnh
             new HttpPostHandler(){
 
                 @Override
                 public void onPostCompleted(String response, Integer status) {
                 	// Ȃ΃m[g̃Xgʂ֑J
                 	Log.v("http request success", "post success");
                 	try {
                 		_jsonArr = new JSONArray(response);
                 		for (int i = 0; i < _jsonArr.length(); i++) {
                 			JSONObject jsonObject = _jsonArr.getJSONObject(i);
                 			Log.d("response",jsonObject.getString("title"));
                 			Log.d("response",jsonObject.getString("body"));
                 		}
                 		@SuppressWarnings("rawtypes")
                 		ArrayList<ArrayList> list = jsonArrayToArrayList(_jsonArr);
                 		adapter = new NoteListAdapter(context, R.layout.note_row, list);
                 		ListView lv = getListView();
                 		LayoutInflater inflater = getLayoutInflater();
                 		ViewGroup header = (ViewGroup)inflater.inflate(R.layout.note_list_header, lv, false);
                 		ViewGroup footer = (ViewGroup)inflater.inflate(R.layout.note_list_footer, lv, false);
                 		lv.addHeaderView(header, null, false);
                 		lv.addFooterView(footer, null, false);
                 		setListAdapter(adapter);
         		          		  
                 		// VK쐬{^
                 		Button buttonNew = (Button) findViewById(R.id.button_new);
                 		buttonNew.setOnClickListener(new View.OnClickListener() {
                 			public void onClick(View view) {
                 				Intent intent = new Intent(NoteListActivity.this, NoteNewActivity.class);
                 				view.getContext().startActivity(intent);
                 			}
                 		});
         	  	  
                 		// OAEg{^
                 		Button buttonLogout = (Button) findViewById(R.id.logout);
                 		buttonLogout.setOnClickListener(new View.OnClickListener() {
                 			public void onClick(View view) {
                 				Intent intent = new Intent(NoteListActivity.this, LoginActivity.class);
                 				view.getContext().startActivity(intent);
                 				app.clearCurrentNotePage();
                 			}
                 		});
         	      
                 		// m[g̒ǉǂݍ݃{^
                 		Button buttonMoreNotes = (Button) findViewById(R.id.button_more_notes);
                 		buttonMoreNotes.setOnClickListener(new View.OnClickListener() {
                 			public void onClick(View view) {
                 				Integer pageNumber = app.getCurrentNotePage();
                 				final Map<String, String> params = new HashMap<String, String>();
                 				params.put("page", Integer.toString(pageNumber));
                 				get_more_notes("notes", params, httpClient);
                 				app.incrementCurrentNotePage();
                 			}
                 		});
         	      
                 		// ʒʒm_CAO
                 		if(extras != null && extras.getString("MESSAGE") != null){
                 			builder.setMessage(extras.getString("MESSAGE"));
                 			builder.create();
                 			builder.show();
                 		}
         	  	
                 	} catch (JSONException e) {
                 		Log.d("error", "parse fail");
                 		e.printStackTrace();
                 	}
                 }
 
                 @Override
                 public void onPostFailed(String response, Integer status) {
                 	Log.v("http request error", Integer.toString(status));
                 	Log.d("http request error", response);
                 	String err_msg = "ʐMG[܂B";
                 	if (status == 403){
                		err_msg = "m[g̓ǂݍ݂Ɏs܂B";
                 	}
                 	Toast.makeText(
                 			getApplicationContext(), 
                 			err_msg, 
                 			Toast.LENGTH_LONG
                 			).show();
                 }
             },
             httpClient
         		);
 
         task.setParamsToUrl( params );
         // ^XNJn
         task.execute();
     }
 	
 	@SuppressLint({ "HandlerLeak", "HandlerLeak" })
 	private void get_more_notes(String url, Map<String, String> params, DefaultHttpClient httpClient) {
 		// 񓯊^XN`
 		HttpGetTask task = new HttpGetTask(
 				this,
 				Constants.SERVER_URL + url,
 
 				// ^XNɌĂ΂UĨnh
 				new HttpPostHandler(){
 
 					@Override
 					public void onPostCompleted(String response, Integer status) {
 						// Ȃ΃m[g̃Xgʂ֑J
 						Log.v("http request success", "post success");
 						try {
 							_jsonArr = new JSONArray(response);
 							for (int i = 0; i < _jsonArr.length(); i++) {
 								JSONObject jsonObject = _jsonArr.getJSONObject(i);
 								Log.d("response",jsonObject.getString("title"));
 								Log.d("response",jsonObject.getString("body"));
 							}
 							@SuppressWarnings("rawtypes")
 							ArrayList<ArrayList> items = jsonArrayToArrayList(_jsonArr);
 							for (int i = 0; i < items.size(); i++) {
 								adapter.add(items.get(i));
 				        	}
 							adapter.debug_items();
 	        	      
 	        	      // ʒʒm_CAO
 	        	      if(extras != null && extras.getString("MESSAGE") != null){
 	        			  builder.setMessage(extras.getString("MESSAGE"));
 	      				  builder.create();
 	      				  builder.show();
 	        		  }
 	        	  	
 	        	  } catch (JSONException e) {
 	        		  Log.d("error", "parse fail");
 	        		  e.printStackTrace();
 	        	  }
 	          }
 
 	          @Override
 	          public void onPostFailed(String response, Integer status) {
 	        	  Log.v("http request error", Integer.toString(status));
 	        	  Log.d("http request error", response);
 	        	  String err_msg = "ʐMG[܂B";
 	        	  if (status == 403){
	        		  err_msg = "m[g̓ǂݍ݂Ɏs܂B";
 	        	  }
 	            Toast.makeText(
 	              getApplicationContext(), 
 	              err_msg, 
 	              Toast.LENGTH_LONG
 	            ).show();
 	          }
 	        },
 	        httpClient
 	      );
 
 	      task.setParamsToUrl( params );
 	      // ^XNJn
 	      task.execute();
 	    }
 	
 	@SuppressWarnings("rawtypes")
 	public ArrayList<ArrayList> jsonArrayToArrayList(JSONArray ja)
     {
         ArrayList<ArrayList> listItems = new ArrayList<ArrayList>();
         try {
         	for (int i = 0; i < ja.length(); i++) {
         		ArrayList<String> listItem = new ArrayList<String>();
         		JSONObject jo = (JSONObject) ja.get(i);
         		listItem.add(jo.getString("title"));
         		listItem.add(jo.getString("body"));
         		listItem.add(jo.getString("id"));
         		listItems.add(listItem);
         	}
         } catch (JSONException e) {
             e.printStackTrace();
         }
         return listItems;
     }
 }
