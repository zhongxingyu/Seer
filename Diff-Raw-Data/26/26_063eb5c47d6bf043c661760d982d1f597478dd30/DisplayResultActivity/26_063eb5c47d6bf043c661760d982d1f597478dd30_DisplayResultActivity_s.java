 package com.ashoksm.pinfinder;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.widget.GridView;
 import android.widget.LinearLayout;
 
 import com.ashoksm.pinfinder.adapter.CustomOfficeAdapter;
 import com.ashoksm.pinfinder.logic.SAXXMLParser;
 import com.ashoksm.pinfinder.to.Office;
 import com.google.ads.AdRequest;
 import com.google.ads.AdView;
 
 public class DisplayResultActivity extends Activity {
 
	private static final Map<String, List<Office>> OFFICEHOLDER = new HashMap<String, List<Office>>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setProgressBarIndeterminateVisibility(true);
 		setContentView(R.layout.activity_display_result);
 		AdView adView = (AdView) this.findViewById(R.id.ad_1);
 		adView.loadAd(new AdRequest());
 		new AsyncTask<Void, Void, Void>() {
 			LinearLayout linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
 			CustomOfficeAdapter adapter;
 
 			@Override
 			protected void onPreExecute() {
 				// SHOW THE SPINNER WHILE LOADING FEEDS
 				linlaHeaderProgress.setVisibility(View.VISIBLE);
 			}
 
 			@Override
 			protected Void doInBackground(Void... params) {
 				// Get the message from the intent
 				Intent intent = getIntent();
 				String stateName = intent.getStringExtra(PinFinderMainActivity.EXTRA_STATE).toLowerCase()
 						.replaceAll(" ", "");
 				String districtName = intent.getStringExtra(PinFinderMainActivity.EXTRA_DISTRICT).toLowerCase();
 				String officeName = intent.getStringExtra(PinFinderMainActivity.EXTRA_OFFICE).toLowerCase();
 				try {
 					List<Office> state = new ArrayList<Office>();
					if (!containsKey(stateName)) {
 						String[] fileNames = getAssets().list("");
 						for (String name : fileNames) {
 							if (name.endsWith(".xml") && name.indexOf(stateName) >= 0
 									&& !OFFICEHOLDER.containsKey(name)) {
 								InputStream in_s = getApplicationContext().getAssets().open(name);
 								List<Office> offices = SAXXMLParser.parse(in_s);
 								state.addAll(offices);
 								OFFICEHOLDER.put(name, offices);
							} else if (OFFICEHOLDER.containsKey(name)) {
 								state.addAll(OFFICEHOLDER.get(name));
 							}
 						}
 					} else {
 						for (Iterator<?> iterator = OFFICEHOLDER.entrySet().iterator(); iterator.hasNext();) {
 							Map.Entry<String, List<Office>> entry = (Map.Entry<String, List<Office>>) iterator.next();
 							if (entry.getKey().indexOf(stateName) >= 0) {
 								state.addAll(entry.getValue());
 							}
 						}
 					}
 					ArrayList<Office> matchingOffices = findMatchingOffices(state, officeName, districtName);
 					if (matchingOffices.size() > 0) {
 						adapter = new CustomOfficeAdapter(DisplayResultActivity.this, matchingOffices);
 					}
 				} catch (Exception e) {
					e.printStackTrace();
 				}
 				return null;
 			}
 
 			private boolean containsKey(String fileName) {
 				for (Iterator<?> iterator = OFFICEHOLDER.entrySet().iterator(); iterator.hasNext();) {
 					Map.Entry<String, List<Office>> entry = (Map.Entry<String, List<Office>>) iterator.next();
 					if (entry.getKey().indexOf(fileName) >= 0) {
 						return true;
 					}
 				}
 				return false;
 			}
 
 			@Override
 			protected void onPostExecute(Void result) {
 				if (adapter != null) {
 					GridView gridview = (GridView) findViewById(R.id.gridview);
 					gridview.setAdapter(adapter);
 				} else {
 					LinearLayout noMatchingLayout = (LinearLayout) findViewById(R.id.noMatchingLayout);
 					noMatchingLayout.setVisibility(View.VISIBLE);
 				}
 				// HIDE THE SPINNER AFTER LOADING FEEDS
 				linlaHeaderProgress.setVisibility(View.GONE);
 			}
 
 		}.execute();
 
 		setProgressBarIndeterminateVisibility(false);
 	}
 
 	private ArrayList<Office> findMatchingOffices(List<Office> offices, String officeName, String districtName) {
 		ArrayList<Office> matchingOffices = new ArrayList<Office>();
 		for (Office office : offices) {
 			try {
 				if (office.getDistrict().indexOf(districtName) >= 0
 						&& (office.getOfficeName().toLowerCase().indexOf(officeName) >= 0 || officeName.equals(office
 								.getPinCode()))) {
 					matchingOffices.add(office);
 				}
 			} catch (Exception ex) {
 				Log.e("Failed Office", "Office Name :" + office.getPinCode());
 			}
 		}
 		return matchingOffices;
 	}
 }
