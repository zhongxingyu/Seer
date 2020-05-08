 package vs.piratenpartei.ch.app;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.xmlpull.v1.XmlPullParserException;
 
 import vs.piratenpartei.ch.app.redmine.IssueDetailItem;
 import vs.piratenpartei.ch.app.redmine.IssueDetailItem.JournalItem;
 import vs.piratenpartei.ch.app.redmine.JournalArrayAdapter;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class ProjectActivity extends FragmentActivity 
 {
 	private static final String TAG = "vs.piratenpartei.ch.app.FragmentActivity";
 
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
 
 	private int _xml_id;
 	private IssueDetailItem _data;
 	private JournalArrayAdapter _adapter_journal;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		Log.d(TAG, "onCreate()");
 		
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		
 		setContentView(R.layout.activity_project);
 
 		// Show the Up button in the action bar.
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		// Create the adapter that will return a fragment for each of the three
 		// primary sections of the app.
 		mSectionsPagerAdapter = new SectionsPagerAdapter(
 				getSupportFragmentManager());
 
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
 		Bundle params = getIntent().getExtras();
 		this._xml_id = params.getInt("issue_id");
 		
 		TextView id_text = (TextView)findViewById(R.id.project_detail_id);
 		id_text.setText(this._xml_id + "");
 		
 		TextView subject_text = (TextView)findViewById(R.id.project_detail_title);
 		subject_text.setText(params.getString("issue_subject"));
 
 		setProgressBarIndeterminateVisibility(true);
 		new ProjectsDetailLoaderTask().execute();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) 
 	{
 		Log.d(TAG, "onCreateOptionsMenu(" + menu.toString() + ")");
 		getMenuInflater().inflate(R.menu.activity_project, menu);
 		menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() 
 		{
 			@Override
 			public boolean onMenuItemClick(MenuItem item) 
 			{
 				setProgressBarIndeterminateVisibility(true);
 				new ProjectsDetailLoaderTask().execute();
 				return true;
 			}
 		});
 		return true;
 	}
 	
 	public void updateView(int position) {
 		Log.d(TAG, "updateView(" + position + ")");
 		if(_data != null)
 		{
 			switch(position)
 			{
 			case 0:
 				TextView author = (TextView)findViewById(R.id.text_project_detail_author);
 				author.setText(_data.getAuthor());
 				
 				TextView asignee = (TextView)findViewById(R.id.text_project_detail_assignee);
 				asignee.setText(_data.getAssignedTo());
 				
 				TextView priority = (TextView)findViewById(R.id.text_project_detail_priority);
 				priority.setText(_data.getPriority());
 				
 				TextView startDate = (TextView)findViewById(R.id.text_project_detail_startdate);
 				startDate.setText(DateFormat.getInstance().format(_data.getStartDate()));
 				
 				TextView dueDate = (TextView)findViewById(R.id.text_project_detail_duedate);
 				dueDate.setText(DateFormat.getInstance().format(_data.getDueDate()));
 				
 				TextView createdOn = (TextView)findViewById(R.id.text_project_detail_created);
 				createdOn.setText(DateFormat.getInstance().format(_data.getCreatedOn()));
 				
 				TextView updatedOn = (TextView)findViewById(R.id.text_project_detail_updated);
 				updatedOn.setText(DateFormat.getInstance().format(_data.getUpdatedOn()));
 				break;
 			case 1:
 				TextView description = (TextView)findViewById(R.id.text_project_desc);
 				description.setText(_data.getDescription());
 				break;
 			case 2:
 				ProgressBar progressbar = (ProgressBar)findViewById(R.id.progress_project_detail_progress);
 				progressbar.setProgress(_data.getProgress());
 				
 				TextView progressText = (TextView)findViewById(R.id.text_project_detail_progress);
 				progressText.setText(_data.getProgress() + "%");
 				
 				TextView status = (TextView)findViewById(R.id.text_project_detail_status);
 				status.setText(_data.getStatus());
 				
 				TextView estHours = (TextView)findViewById(R.id.text_project_detail_esthours);
 				estHours.setText(_data.getEstimatedHours());
 				break;
 			case 3:
 				ArrayList<JournalItem> journal_list = _data.getJournal();
 				JournalItem[] data_journal = new JournalItem[journal_list.size()];
 				journal_list.toArray(data_journal);
 				_adapter_journal = new JournalArrayAdapter(this, R.layout.journal_list_item, data_journal);
				_adapter_journal.notifyDataSetChanged();
 				break;
 			}
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Log.d(TAG, "onOptionsItemSelected(" + item.toString() + ")");
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 	 * one of the sections/tabs/pages.
 	 */
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 		
 		private static final String TAG_EXT = ".SectionsPagerAdapter";
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 			Log.d(TAG + TAG_EXT, "new SectionsPagerAdapter(" + fm.toString() + ")");
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			Log.d(TAG + TAG_EXT, "getItem(" + position + ")");
 			// getItem is called to instantiate the fragment for the given page.
 			// Return a DummySectionFragment (defined as a static inner class
 			// below) with the page number as its lone argument.
 			Fragment fragment;
 			switch(position)
 			{
 			case 0:
 				fragment = new OverviewFragment();
 				break;
 			case 1:
 				fragment = new DescriptionFragment();
 				break;
 			case 2:
 				fragment = new StatusFragment();
 				break;
 			case 3:
 				fragment = new JournalFragment();
 				break;
 			default:
 				fragment = new DummySectionFragment();
 				Bundle args = new Bundle();
 				args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
 				fragment.setArguments(args);
 				break;
 			}
 			return fragment;
 		}
 
 		@Override
 		public int getCount() {
 			Log.d(TAG + TAG_EXT, "getCount()");
 			return 4;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			switch (position) {
 			case 0:
 				return getString(R.string.issue_overview);
 			case 1:
 				return getString(R.string.issue_description);
 			case 2:
 				return getString(R.string.issue_status);
 			case 3:
 				return getString(R.string.issue_journal);
 			}
 			return null;
 		}
 	}
 
 
 	public static class OverviewFragment extends Fragment
 	{
 		private static final String TAG_EXT = ".OverviewFragment";
 		
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container, 
 				Bundle savedInstanceState)
 		{
 			Log.d(TAG + TAG_EXT, "onCreateView()");
 			return inflater.inflate(R.layout.project_overview_fragment, container, false);
 		}
 		
 		public void onResume()
 		{
 			super.onResume();
 			Log.d(TAG + TAG_EXT, "onResume()");
 			((ProjectActivity) getActivity()).updateView(0);
 		}
 	}
 	
 	public static class DescriptionFragment extends Fragment
 	{
 		private static final String TAG_EXT = ".DescriptionFragment";
 		
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container, 
 				Bundle savedInstanceState)
 		{
 			Log.d(TAG + TAG_EXT, "onCreateView()");
 			return inflater.inflate(R.layout.project_description_fragment, container, false);
 		}
 
 		public void onResume()
 		{
 			super.onResume();
 			Log.d(TAG + TAG_EXT, "onResume()");
 			((ProjectActivity) getActivity()).updateView(1);
 		}
 	}
 	
 	public static class StatusFragment extends Fragment
 	{
 		private static final String TAG_EXT = ".StatusFragment";
 		
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container, 
 				Bundle savedInstanceState)
 		{
 			Log.d(TAG + TAG_EXT, "onCreateView()");
 			return inflater.inflate(R.layout.project_status_fragment, container, false);
 		}
 
 		public void onResume()
 		{
 			super.onResume();
 			Log.d(TAG + TAG_EXT, "onResume()");
 			((ProjectActivity) getActivity()).updateView(2);
 		}
 	}
 	
 	public static class JournalFragment extends ListFragment
 	{
 		private static final String TAG_EXT = ".JournalFragment";
 
 		public void onResume()
 		{
 			super.onResume();
 			Log.d(TAG + TAG_EXT, "onResume()");
 			ProjectActivity parent = ((ProjectActivity) getActivity());
 			parent.updateView(3);
 			setListAdapter(parent._adapter_journal);
 			setListShown(true);
 		}
 	}
 	
 	private class ProjectsDetailLoaderTask extends AsyncTask<Void, Void, Void>
 	{
 		private static final String TAG_EXT = ".ProjectsDetailLoaderTask";
 		
 		@Override
 		protected Void doInBackground(Void... params) 
 		{
 			Log.d(TAG + TAG_EXT, "doInBackground()");
 			try 
 			{
 				HttpClient client = new DefaultHttpClient();
 				HttpGet httpget = new HttpGet(getString(R.string.config_issues_detail_xml) + _xml_id + ".xml?include=journals");
 				HttpResponse response;
 				response = client.execute(httpget);
 				if(response.getStatusLine().getStatusCode() == 200)
 				{
 					HttpEntity entity = response.getEntity();
 					if(entity != null)
 					{
 						InputStream in = entity.getContent();
 						_data = IssueDetailItem.readRedmineXml(in);
 						in.close();
 					}
 				}
 			} 
 			catch (ClientProtocolException e) 
 			{
 				Log.e("[PPVS App]:ProjectsFragment -> ClientProtocolException", e.getMessage());
 			}
 			catch (IOException e) 
 			{
 				Log.e("[PPVS App]:ProjectsFragment -> IOException", e.getMessage());
 			} catch (XmlPullParserException e) {
 				Log.e("[PPVS App]:ProjectsFragment -> XmlPullParserException", e.getMessage());
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(Void result)
 		{
 			Log.d(TAG + TAG_EXT, "onPostExecute()");
 			int index = mViewPager.getCurrentItem();
			Log.i(TAG + TAG_EXT, "pager page: " + index);
 			updateView(index);
 			updateView(index+1);
 			updateView(index-1);
 			setProgressBarIndeterminateVisibility(false);
 		}
 		
 	}
 
 	/**
 	 * A dummy fragment representing a section of the app, but that simply
 	 * displays dummy text.
 	 */
 	public static class DummySectionFragment extends Fragment {
 		/**
 		 * The fragment argument representing the section number for this
 		 * fragment.
 		 */
 		public static final String ARG_SECTION_NUMBER = "section_number";
 		
 		private static final String TAG_EXT = ".DummySectionFragment";
 
 		public DummySectionFragment() 
 		{
 			Log.d(TAG + TAG_EXT, "new DummySectionFragment()");
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			Log.d(TAG + TAG_EXT, "onCreateView(" + inflater.toString() + ", " + container.toString() + ", " + savedInstanceState.toString() + ")");
 			TextView textView = new TextView(getActivity());
 			textView.setGravity(Gravity.CENTER);
 			textView.setText(Integer.toString(getArguments().getInt(
 					ARG_SECTION_NUMBER)));
 			return textView;
 		}
 	}
 
 }
