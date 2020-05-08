 package com.cromiumapps.uwhub;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.ListView;
 
 import com.cromiumapps.uwhub.UWAPIWrapper.UWAPIWrapperListener;
 
 public class ExamInfo extends Activity implements UWAPIWrapperListener {
 
 	// Context
 	Context ctx = this;
 
 	// API Key
 	// Adrian's API. But this is to be used to avoid conflicts.
 	private String API_KEY;
 
 	// UWAPIWrapper
 	private static UWAPIWrapper apiWrapper;
 
 	// UI Elements
 	private ListView coursesInfoContent;
 
 	private ExamInfoListAdapter examInfoListadapter;
 	private ArrayList<ExamInfoData> exams = new ArrayList<ExamInfoData>();
 
 	// Others
 	private final String LOG_TAG = "ExamInfo";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_examinfo);
 
 		// UI Elements
 		ListView examInfoContent = (ListView) findViewById(R.id.examInfoContent);
 
 		examInfoListadapter = new ExamInfoListAdapter(ctx, exams);
 		examInfoContent.setAdapter(examInfoListadapter);
 
 		// Initialize an API wrapper object using the API key and this activity
 		// as the listener.
 		API_KEY = ((UWHUB) this.getApplication()).getAPI_KEY();
 		apiWrapper = new UWAPIWrapper(API_KEY, this, ctx);
 
 		apiWrapper.callService("ExamSchedule", ctx);
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onSaveInstanceState(savedInstanceState);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// TODO Auto-generated method stub
 		getMenuInflater().inflate(R.menu.activity_examinfo, menu);
 		return true;
 	}
 
 	@Override
 	public void onUWAPIRequestComplete(JSONObject jsonObject, boolean success) {	
 		try {
 			JSONObject jsonResponse = jsonObject.getJSONObject("response");
 			JSONObject jsonData = jsonResponse.getJSONObject("data");
 			JSONArray jsonExamInfoResult = jsonData.getJSONArray("result");
 			
 			for (int j = 0; j < jsonExamInfoResult.length(); j++) {
 				JSONObject examObject = jsonExamInfoResult.getJSONObject(j);
				exams.add(j, new ExamInfoData(examObject.getString("Course"), examObject.getString("Section"), examObject.getString("Day"), examObject.getString("Date"), examObject.getString("Start"), examObject.getString("End"), examObject.getString("Location")));
 			}
 			examInfoListadapter.notifyDataSetChanged();
 		} catch (JSONException e) {
 			Log.v(LOG_TAG, e.getMessage());
 		}
 	}
}
