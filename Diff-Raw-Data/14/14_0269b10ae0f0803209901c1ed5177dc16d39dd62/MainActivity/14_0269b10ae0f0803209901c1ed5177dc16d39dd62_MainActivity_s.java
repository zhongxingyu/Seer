 package de.laxu.apps.commonurllist;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.app.ListFragment;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class MainActivity extends FragmentActivity {
 
 	/**
 	 * The {@link android.support.v4.view.PagerAdapter} that will provide
 	 * fragments for each of the sections. We use a
 	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
 	 * will keep every loaded fragment in memory. If this becomes too memory
 	 * intensive, it may be best to switch to a
 	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
 	 */
 	SectionsPagerAdapter mSectionsPagerAdapter;
 
 	/**
 	 * The {@link ViewPager} that will host the section contents.
 	 */
 	ViewPager mViewPager;
 	
 	static HashMap<String, ArrayList<UrlListEntry>> hostURLList;
 	static ArrayList<String> hostnames;
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		//dummy data, will be overwritten onload
 		hostnames=new ArrayList<String>();
 		hostURLList = new HashMap<String, ArrayList<UrlListEntry>>();
 		
 		setContentView(R.layout.activity_main);
 
 		// Create the adapter that will return a fragment for each of the three
 		// primary sections of the app.
 		mSectionsPagerAdapter = new SectionsPagerAdapter(
 				getSupportFragmentManager());
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
 		updateLists();
 	}
 	public void updateLists(){
 		hostURLList = new HashMap<String, ArrayList<UrlListEntry>>();
 		String errors = Util.getSettingsErrors(this);
 		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(sharedPrefs.getString("pref_serverurl", "").equals("")){
 			requestToken();
 			// we cannot proceed with updating, because the user needs to accept the token first.
 		}else{
			if(errors != null){
 				new LoadUrlListTask(this).execute();
 			}else{
 				errorMessage(errors);
 			}
 		}
 	}
 	public void requestToken(){
 		String errors=Util.getSettingsErrors(this);
		if(errors != null){
 			new requestTokenTast(this).execute();
 		}else{
 			errorMessage(errors);
 		}
 	}
 	public void errorMessage(String errormessage){
 		TextView messageTextView =  (TextView) findViewById(R.id.messageTextView);
 		messageTextView.setText(errormessage);
 		messageTextView.setVisibility(TextView.VISIBLE);
 		messageTextView.setTextColor(Color.RED);
 	}
 	public void infoMessage(String infomessage){
 		TextView messageTextView =  (TextView) findViewById(R.id.messageTextView);
 		messageTextView.setText(infomessage);
 		messageTextView.setVisibility(TextView.VISIBLE);
 		messageTextView.setTextColor(Color.BLACK);
 	}
 	public void hideMessage(){
 		TextView messageTextView =  (TextView) findViewById(R.id.messageTextView);
 		messageTextView.setVisibility(TextView.GONE);
 		// reset to defaults
 		messageTextView.setTextColor(Color.BLACK);
 		messageTextView.setText("");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		
 		MenuItem refreshMenuItem = menu.findItem(R.id.refresh_menuitem);
 		refreshMenuItem.setOnMenuItemClickListener(new OnRefreshMenuItemClickListener(this, this));
 		
 		MenuItem settingsMenu = menu.findItem(R.id.settingsmenu);
 		settingsMenu.setOnMenuItemClickListener(new OnSettingsMenuItemClickListener(this, this));
 		
 		MenuItem requestTokenMenuItem = menu.findItem(R.id.requesttoken_menuitem);
 		requestTokenMenuItem.setOnMenuItemClickListener(new OnRequestTokenMenuItemClickListener(this, this));
 		return true;
 	}
 
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 	 * one of the sections/tabs/pages.
 	 */
 	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			Fragment fragment = new UrlTab();
 			Bundle args = new Bundle();
 			args.putInt("position", position);
 			fragment.setArguments(args);
 			return fragment;
 		}
 
 		@Override
 		public int getCount() {
 			return hostnames.size();
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			return hostnames.get(position);
 		}
 	}
 
 	public static class UrlTab extends ListFragment {
 		/**
 		 * The fragment argument representing the section number for this
 		 * fragment.
 		 */
 
 		@Override
 		public void onActivityCreated(Bundle savedInstanceState){
 			super.onActivityCreated(savedInstanceState);
 			UrlArrayAdapter arrayAdapter = new UrlArrayAdapter(getActivity(), R.layout.urllist_entry, new ArrayList<UrlListEntry>());
 			setListAdapter(arrayAdapter);
 			int position = ((Integer) this.getArguments().get("position")).intValue();
 			String hostname = hostnames.get(position);
 			assert hostURLList != null;
 			assert hostname != null;
 			assert arrayAdapter != null;
 			ArrayList<UrlListEntry> list = hostURLList.get(hostname);
 			if(list != null){
 				arrayAdapter.addAll(list);
 				arrayAdapter.notifyDataSetChanged();
 			}
 		}
 	}
 }
 class UrlArrayAdapter extends ArrayAdapter<UrlListEntry>{
 	Context context;
 	ArrayList<UrlListEntry> arrayList;
 	public UrlArrayAdapter(Context context, int layoutId, ArrayList<UrlListEntry> arrayList){
 		super(context, layoutId, arrayList);
 		this.arrayList = arrayList;
 		this.context = context;
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent){
 		View view;
 		/*if(convertView != null){
 			view = convertView;
 		}else{*/
 			LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			view = inflater.inflate(R.layout.urllist_entry, parent, false);
 			UrlListEntry entry = (UrlListEntry)arrayList.get(position);
 			String url = entry.getUrl();
 			String created = entry.getCreated();
 			((TextView) view.findViewById(R.id.urlListEntryLink)).setText((CharSequence)url);
 			((TextView) view.findViewById(R.id.urlListEntryCreatedDate)).setText((CharSequence)created);
 		//}
 		return view;
 	}	
 }
