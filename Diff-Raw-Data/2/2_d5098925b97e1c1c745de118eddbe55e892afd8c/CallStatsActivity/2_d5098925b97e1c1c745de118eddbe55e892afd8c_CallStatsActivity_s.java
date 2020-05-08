 package com.rogicrew.callstats;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Stack;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import com.rogicrew.callstats.components.HeaderComponent;
 import com.rogicrew.callstats.components.HeaderComponent.IHeaderComponentChanged;
 import com.rogicrew.callstats.components.PerformanceOutgoingListAdapter;
 import com.rogicrew.callstats.models.CallElement;
 import com.rogicrew.callstats.models.CallModel;
 import com.rogicrew.callstats.models.CallsFilter;
 import com.rogicrew.callstats.models.SimpleDate;
 import com.rogicrew.callstats.models.SortByEnum;
 import com.rogicrew.callstats.utils.Utils;
 
 public class CallStatsActivity extends Activity implements IHeaderComponentChanged, OnItemClickListener {
 	private static final String breadcrumbsPrefixFirst = "<< ";
 	private static final String breadcrumbsPrefixNext  = "<< ... ";
 	
 	private HeaderComponent mHeaderComponent;
 	private CallModel mCallModel;
 	private TextView mTextViewDuration;
 	private TextView mBreadcrumb;
 	private ListView mListView;
 	private ProgressBar mProgressBar;
 	private volatile boolean isRefreshing = false;
 	private String mMinutesFormater;
 	
 	private CallsFilter mCurrentFilter;
 	private Stack<CallsFilter> mStackFilters;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.setContentView(R.layout.outgoing);
         //set prerefences for utils
         Utils.preferences = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
         mCallModel = new CallModel();
         mCurrentFilter = new CallsFilter();
         mStackFilters = new Stack<CallsFilter>();
         
         mListView = (ListView)findViewById(R.id.listviewOutgoingCalls);   
         mListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
         mListView.setOnItemClickListener(this);
         mTextViewDuration = (TextView)findViewById(R.id.textviewDurationSum); 
         mProgressBar = (ProgressBar)findViewById(R.id.progressBarList);
         mBreadcrumb = (TextView)findViewById(R.id.breadcrumbs);
         
         //init header component last - it will call this activity callback after init to pick initial filter params
         mHeaderComponent = (HeaderComponent)findViewById(R.id.outgoingHeaderComponent);
         mHeaderComponent.initOnStart();
         mHeaderComponent.mHeaderComponentChangedCallback = this;
     }
     
     public void onClick(View view) {
     	
     }
     
     @Override
     public void onStart() {
     	super.onStart();
     	mMinutesFormater = getString(R.string.minutes_formater);
     }
     
     //called after user navigates back to actvity
     @Override
     public void onRestart() {
     	super.onRestart();    		
     	mHeaderComponent.initOnRestart();
     	populateList();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	Bundle bundle;
     	Intent newIntent;
         switch (item.getItemId()) {
             case R.id.setingsMenuItem:
             	Intent preferencesActivityIntent = new Intent(this, MyPreferencesActivity.class);
             	this.startActivity(preferencesActivityIntent);
             	return true;
             case R.id.chartDayMenuItem:
             	bundle = new Bundle();
             	bundle.putSerializable("filter", mCurrentFilter);
             	
             	newIntent = new Intent(this.getApplicationContext(), ChartDayActivity.class);
             	newIntent.putExtras(bundle);
             	startActivity(newIntent);
             	return true;
             case R.id.chartMonthMenuItem:
             	bundle = new Bundle();
             	bundle.putSerializable("filter", mCurrentFilter);
             	
             	newIntent = new Intent(this.getApplicationContext(), ChartMonthActivity.class);
             	newIntent.putExtras(bundle);
             	startActivity(newIntent);
             	return true;	
             case R.id.aboutMenuItem:
             	Utils.simpleDialog(this, getString(R.string.about_text), false);
             	return true;
             	
             case R.id.exitMenuItem:
             	finish();
             	return true;
         	default:
         		return false;
         }
     }
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
     	    if (mStackFilters.size() > 0) {
     	    	//current filter is previous one now
     	    	mCurrentFilter = mStackFilters.pop();
     	    	//update ui
     	    	mHeaderComponent.setSortBy(mCurrentFilter.sortBy);
             	mHeaderComponent.setDateInterval(mCurrentFilter.fromDate, mCurrentFilter.toDate);
             	populateList();
     	    }        	
         	else {
         		finish();
         	}
         	
         	return true;
         }
         
         return super.onKeyDown(keyCode, event);
     }
 
 	@Override
 	public void onHeaderComponentChanged(SimpleDate from, SimpleDate to,
 			SortByEnum sortBy) {
 		mCurrentFilter.sortBy = sortBy;
 		mCurrentFilter.fromDate = from;
 		mCurrentFilter.toDate = to;
 		populateList();
 	}
 
 	private void populateList() {
 		if (!isRefreshing) {
 		
 			isRefreshing = true;
 			mProgressBar.setVisibility(View.VISIBLE);
 			mListView.setVisibility(View.GONE);
 			
 			new AsyncTask<Object, Integer, Long>() {
 				private CallStatsActivity thisActivity = null;
 				
 				@Override
 				protected void onPreExecute() {
 					if (mCurrentFilter.tag == null)
 					{
 						mBreadcrumb.setText("");
 					}
 					else
 					{
 						String prefix = mStackFilters.size() > 1 ? breadcrumbsPrefixNext : breadcrumbsPrefixFirst;
 						mBreadcrumb.setText(prefix + (String)mCurrentFilter.tag);
 					}
 				}
 				
 				@Override
 				protected Long doInBackground(Object... params) {
 					thisActivity = (CallStatsActivity)params[0];
 					mCallModel.load(thisActivity, mCurrentFilter);
 					return null;
 				}
 				
 				@Override
 				protected void onPostExecute(Long result) {
 					PerformanceOutgoingListAdapter la = new PerformanceOutgoingListAdapter(thisActivity, thisActivity.getSortBy(), 
 							R.id.listviewOutgoingCalls,
 							mCallModel.getElements());
 					mListView.setAdapter(la);
 					
 					long dur = mCallModel.getDurationSum();
 					int hours = (int)(dur / 3600);
 					int minutes = (int)(dur / 60 % 60);
 					String d = Utils.getFormatedTime(hours, minutes, (int)(dur % 60));
 					d += String.format(mMinutesFormater, Integer.toString(hours * 60 + minutes));
 					mTextViewDuration.setText(d);
 					
 					isRefreshing = false;
 					mProgressBar.setVisibility(View.GONE);
 					mListView.setVisibility(View.VISIBLE);
 			    }
 			}.execute(this);
 		}
 		
 	}
 	
 	public SortByEnum getSortBy() {
 		return mHeaderComponent.getSortBy();
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {		
 		//push current filter to stack
 		mStackFilters.push(new CallsFilter(mCurrentFilter));
 		
 		List<CallElement> elems = mCallModel.getElements();
 		CallElement el = elems.get(position);
 		if (!mCurrentFilter.isByDayOrMonthFilter()) {
 			mHeaderComponent.setSortBy(mCurrentFilter.sortBy = SortByEnum.DurationDesc);
 			//if user has name then filter by name otherwise by phone
			if (Utils.isNullOrEmpty(el.getName())) {
 				mCurrentFilter.tag = mCurrentFilter.phone = el.getPhone();
 				mCurrentFilter.contactName = null;				
 			}
 			else {
 				mCurrentFilter.phone = null;
 				mCurrentFilter.tag = mCurrentFilter.contactName = el.getName();
 			}		
 		}
 		else {
 			mCurrentFilter.phone = mCurrentFilter.contactName = null;
 			mCurrentFilter.fromDate = new SimpleDate(new Date(el.getDateOfCall()));
 			mCurrentFilter.toDate = new SimpleDate(new Date(el.getDateOfCall()));
 			switch (mCurrentFilter.sortBy) {
 			case ByDays: case ByDaysDurationAsc : case ByDaysDurationDesc:
 				mCurrentFilter.tag = el.getPhone();
 				mHeaderComponent.setSortBy(mCurrentFilter.sortBy = SortByEnum.DurationDesc);
 				break;
 			default:
 				mCurrentFilter.tag = el.getName();
 				mHeaderComponent.setSortBy(mCurrentFilter.sortBy = SortByEnum.DurationDesc);
 				mCurrentFilter.toDate = SimpleDate.getNextMonth(mCurrentFilter.toDate);
 				break;
 			}
 			
 			mHeaderComponent.setDateInterval(mCurrentFilter.fromDate, mCurrentFilter.toDate);
 		}
 		
 		populateList();
 	}
 }
