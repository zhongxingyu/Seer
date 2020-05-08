 package com.SifterReader.android;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.widget.ListAdapter;
 import android.widget.SimpleAdapter;
 
 public class IssuesActivity extends ListActivity {
 
 	public static final String NUMBER = "number";
	public static final String STATUS = "status";
	public static final String PRIORITY = "priority";
 	public static final String SUBJECT = "subject";
 	
 	// Members
 	private JSONObject mIssues;
 	private JSONObject[] mAllIssues;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		registerForContextMenu(getListView());
 		
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			try {
 				JSONObject issues = new JSONObject(extras.getString(SifterReader.ISSUES));
 				if (issues != null) {
 					mIssues = issues; 
 					getIssues();
 					fillData();
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void getIssues() {
 		JSONObject[] allIssues = null;
 		try {
 			// array of issues on first page
 			JSONArray issuesArray = mIssues.getJSONArray(SifterReader.ISSUES);
 			int numberIssues = issuesArray.length();
 			allIssues = new JSONObject[numberIssues];
 
 			// issues
 			for (int i = 0; i < numberIssues; i++) {
 				allIssues[i] = issuesArray.getJSONObject(i);
 			}
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		mAllIssues = allIssues;
 	}
 	
 	private void fillData() {
 		int iNum = mAllIssues.length;
 		List<Map<String,String>> issuesList = new ArrayList<Map<String,String>>(iNum); 
 		try {
 			for (int j = 0; j < iNum; j++) {
 				Map<String,String> map = new HashMap<String,String>();
 				map.put(NUMBER,mAllIssues[j].getString(NUMBER));
				map.put(STATUS,mAllIssues[j].getString(STATUS));
				map.put(PRIORITY,mAllIssues[j].getString(PRIORITY));
 				map.put(SUBJECT,mAllIssues[j].getString(SUBJECT));
 				issuesList.add(map);
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
         ListAdapter adapter = new SimpleAdapter(this, issuesList,
        		R.layout.issue_row,
                new String[] {NUMBER, STATUS, PRIORITY, SUBJECT},
                new int[] {R.id.issue_number, R.id.issue_status, R.id.issue_priority, R.id.issue_subject});
         setListAdapter(adapter);
 	}
 }
