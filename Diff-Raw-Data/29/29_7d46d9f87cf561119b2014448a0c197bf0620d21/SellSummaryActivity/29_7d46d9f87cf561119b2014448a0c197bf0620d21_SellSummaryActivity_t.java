 package com.swap;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.SimpleDateFormat;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class SellSummaryActivity extends FragmentActivity implements
 ActionBar.TabListener {
 	
 	SectionsPagerAdapter mSectionsPagerAdapter;
 	ViewPager mViewPager;
 	User userinfo;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_sell_summary);
		getActionBar().setDisplayShowTitleEnabled(false);
 
 		userinfo = new User();
 		getUserInfo();
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		actionBar.setDisplayHomeAsUpEnabled(true);	
 	        
 		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
 		mViewPager
 				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 					@Override
 					public void onPageSelected(int position) {
 						actionBar.setSelectedNavigationItem(position);
 					}
 				});
 
 		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
 
 			actionBar.addTab(actionBar.newTab()
 					.setText(mSectionsPagerAdapter.getPageTitle(i))
 					.setTabListener(this));
 		}
 	}
 
 	private void getUserInfo() {
 		new GetUserInfoTask().execute(getPhoneNumber(10));
 	}
 
 	private String getPhoneNumber(int digitCount) {
 		TelephonyManager mTelephonyMgr;
 	    mTelephonyMgr = (TelephonyManager)
 	        getSystemService(Context.TELEPHONY_SERVICE); 
 	    String num = mTelephonyMgr.getLine1Number();
 	    if(num!=null && num.length()>digitCount)
 	    {
 	    	num= num.substring(num.length()-digitCount);
 	    }
 	    return num;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_sell_summary, menu);
 		return true;
 	}
 	
 	public void btnSellItemClicked(View view)
 	{
 		Intent intent = new Intent(this, SellActivity.class);
 		startActivity(intent);
 		//finish();
 	}
 
 	@Override
 	public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		mViewPager.setCurrentItem(tab.getPosition());		
 	}
 
 	@Override
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 //	@Override
 //	public void onBackPressed() {
 //		//Intent home = new Intent(this, HomeActivity.class);
 //        //startActivity(home);
 //		//finish();
 //		return;
 //	}
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			// getItem is called to instantiate the fragment for the given page.
 			// Return a DummySectionFragment (defined as a static inner class
 			// below) with the page number as its lone argument.
 			Fragment fragment = new SellSummaryFragment();
 			Bundle args = new Bundle();
 			args.putInt(SellSummaryFragment.ARG_SECTION_NUMBER, position + 1);
 			args.putString("userid",getPhoneNumber(10));
 			fragment.setArguments(args);
 			return fragment;
 		}
 
 		private String getPhoneNumber(int digitCount) {
 			TelephonyManager mTelephonyMgr;
 		    mTelephonyMgr = (TelephonyManager)
 		        getSystemService(Context.TELEPHONY_SERVICE); 
 		    String num = mTelephonyMgr.getLine1Number();
 		    if(num!=null && num.length()>digitCount)
 		    {
 		    	num= num.substring(num.length()-digitCount);
 		    }
 		    return num;
 		}
 
 		@Override
 		public int getCount() {
 			return 2;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			switch (position) {
 			case 0:
 				return getString(R.string.title_tab1).toUpperCase();
 			case 1:
 				return getString(R.string.title_tab2).toUpperCase();
 			}
 			return null;
 		}
 	}
 	
 	class GetUserInfoTask extends AsyncTask<String, Void, String> {
 		public static final String API_URL = "http://purple.dotgeek.org/swapapi.php";
 		
 		@Override
 		protected String doInBackground(String... params) {
 			
 			StringBuilder builder = new StringBuilder();
 			
 		    HttpClient client = new DefaultHttpClient();
 		    HttpGet httpGet = new HttpGet(API_URL+"?action=getUserInfo&id="+params[0]);
 		    
 		       
 		    try {
 		    	HttpResponse response = client.execute(httpGet);
 		    	StatusLine statusLine = response.getStatusLine();
 		      
 		    	int statusCode = statusLine.getStatusCode();
 		    	if (statusCode == 200) {
 		    		HttpEntity entity = response.getEntity();
 		    		InputStream content = entity.getContent();
 		    		
 		    		BufferedReader reader = new BufferedReader(new InputStreamReader(content));
 		    		String line;
 		    		while ((line = reader.readLine()) != null) {
 		    			builder.append(line);
 		    		}
 		    	}
 			      else {
 			    	  Log.e("HTTPDownloadTask", "Http Response Error :(");
 			      }
 			    }
 			    catch (ClientProtocolException e) {
 			    	e.printStackTrace();
 			    }
 			    catch (Exception e) {
 			    	e.printStackTrace();
 			    }
 			    
 			    return builder.toString();
 		}
 		
 		@Override
 	    protected void onPostExecute(String result) {
 			
 			try
 			{
 
 				JSONObject jsonObject = new JSONObject(result);
 				
 				userinfo.phone = jsonObject.getString("phone");
 				int x = jsonObject.getInt("premium");
 				if(x ==0)
 				{
 					userinfo.isPremium = false;
 				}
 				else
 				{
 					userinfo.isPremium = true;
 				}
 				if(jsonObject.getString("startdate") != null)
 				{
 					userinfo.membershipStartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jsonObject.getString("startdate"));
 				}
 				else
 				{
 					userinfo.membershipStartDate = null;
 				}
 				
 			}
 			catch (Exception e) {
 				Log.e("Userinfo", "User info retrieval failed" + e.getMessage());
 				e.printStackTrace();
 			}
 			
 			if(userinfo.isPremium)
 			{
 				//hide controls
 				TextView txt = (TextView) findViewById(R.id.txtMessage);
 				txt.setVisibility(View.GONE);
 
 				Button btn = (Button) findViewById(R.id.btnSubscribe);
 				btn.setVisibility(View.GONE);
 			}
 			else
 			{
 				//show controls
 				TextView txt = (TextView) findViewById(R.id.txtMessage);
 				txt.setVisibility(View.VISIBLE);
 
 				Button btn = (Button) findViewById(R.id.btnSubscribe);
 				btn.setVisibility(View.VISIBLE);
 			}
 	    }
 	}
 
 }
