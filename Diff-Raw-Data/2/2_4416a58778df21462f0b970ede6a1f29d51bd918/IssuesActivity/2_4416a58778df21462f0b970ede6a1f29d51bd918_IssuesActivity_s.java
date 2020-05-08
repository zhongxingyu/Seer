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
 import android.widget.TextView;
 
 public class IssuesActivity extends ListActivity {
 
 	public static final String NUMBER = "number";
 	public static final String STATUS = "status";
 	public static final String PRIORITY = "priority";
 	public static final String SUBJECT = "subject";
 	public static final String PAGE = "page";
 	public static final String TOTAL_PAGES = "total_pages";
 	public static final String NEXT_PAGE_URL = "next_page_url";
 	public static final String PREVIOUS_PAGE_URL = "previous_page_url";
 	
 	// Members
 	private JSONObject mIssues;
 	private JSONObject[] mAllIssues;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.issues_footer);
 		registerForContextMenu(getListView());
 		
 		TextView pages = (TextView)findViewById(R.id.page);
 //		Button prevPageButton = (Button)findViewById(R.id.previous_page);
 //		Button nextPageButton = (Button)findViewById(R.id.next_page);
 		
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			try {
 				JSONObject issues = new JSONObject(extras.getString(SifterReader.ISSUES));
 				if (issues != null) {
 					mIssues = issues; 
 					getIssues();
 					pages.setText(getResources().getString(R.string.page) +
 							" " + String.valueOf(mIssues.getInt(PAGE))+ " / " +
 							String.valueOf(mIssues.getInt(TOTAL_PAGES)));
 					
 //					prevPageButton.setOnClickListener(new View.OnClickListener() {
 //
 //						// anonymous inner class
 //						public void onClick(View view) {
 //							String issueURL = null;
 //					    	// get issues url from project
 //					    	try {
 //					    		issueURL = mIssues.getString(PREVIOUS_PAGE_URL);
 //					    	} catch (JSONException e) {
 //					    		e.printStackTrace();
 //					    	}
 //					    	// get url connection
 //					    	URLConnection sifterConnection = SifterReader.getSifterConnection(issueURL);
 //							if (sifterConnection == null)
 //								return;
 //							// get issues
 //							JSONObject issues = loadIssues(sifterConnection);
 //							// intent for PeopleActivity
 //					    	Intent intent = new Intent(this, IssuesActivity.class);
 //							intent.putExtra(ISSUES, issues.toString());
 //							startActivity(intent);
 //						}
 //					});
 					
 					
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
