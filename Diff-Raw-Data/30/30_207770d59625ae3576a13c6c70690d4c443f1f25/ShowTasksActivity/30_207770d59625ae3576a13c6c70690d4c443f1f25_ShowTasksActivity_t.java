 package com.axioma.taskmanager;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.ListFragment;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.util.Pair;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.axioma.taskmanager.async.AsyncCallback;
 import com.axioma.taskmanager.async.GetMatchingTasksInBackground;
 import com.axioma.taskmanager.util.IdentityName;
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Multimap;
 
 /**
  * @author rkannappan
  */
 public class ShowTasksActivity extends FragmentActivity implements AsyncCallback {
 
    public final static String SELECTED_TASK_TYPE = "com.axioma.showtasksactivity.selected_task_type";
    public final static String SELECTED_TASK_TYPE_DESC = "com.axioma.showtasksactivity.selected_task_type_desc";
    public final static String SELECTED_TASK_NAME = "com.axioma.showtasksactivity.selected_task_name";
    public final static String SELECTED_TASK_RAW_NAME = "com.axioma.showtasksactivity.selected_task_raw_name";
 
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
 
    BiMap<String, String> taskTypeDescToTaskTypeMap = HashBiMap.create();
 
    private final Map<String, String> tasksCleansedNameToRawNameMap = Maps.newHashMap();
 
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_show_tasks);
 
       taskTypeDescToTaskTypeMap.put("Import Axioma Data", "IMPORT_AXIOMA_DATA");
       taskTypeDescToTaskTypeMap.put("Import Attribute Data", "IMPORT_ATTRIBUTE_DATA");
       taskTypeDescToTaskTypeMap.put("Import Portfolio Data", "IMPORT_PORTFOLIO_DATA");
       taskTypeDescToTaskTypeMap.put("Import Composite Data", "IMPORT_COMPOSITE_DATA");
       taskTypeDescToTaskTypeMap.put("Import Classification Data", "IMPORT_CLASSIFICATION_DATA");
       taskTypeDescToTaskTypeMap.put("Factor Performance Attribution", "PERFORMANCE_ATTRIBUTION_FACTOR");
       taskTypeDescToTaskTypeMap.put("Returns Performance Attribution", "PERFORMANCE_ATTRIBUTION_RETURNS");
       taskTypeDescToTaskTypeMap.put("Risk Analysis", "RISK_ANALYSIS");
       taskTypeDescToTaskTypeMap.put("Risk Model Machine", "RISK_MODEL_MACHINE");
       taskTypeDescToTaskTypeMap.put("Report", "REPORT");
 
       // Get the message from the intent
       Intent intent = getIntent();
       String taskType = intent.getStringExtra(TaskFinderActivity.SELECTED_TASK_TYPE);
       System.out.println(taskType);
       String owner = intent.getStringExtra(TaskFinderActivity.SELECTED_OWNER);
       System.out.println(owner);
       String portfolio = intent.getStringExtra(TaskFinderActivity.SELECTED_PORTFOLIO);
       System.out.println(portfolio == null ? "" : portfolio);
       String benchmark = intent.getStringExtra(TaskFinderActivity.SELECTED_BENCHMARK);
       System.out.println(benchmark == null ? "" : benchmark);
       String riskmodel = intent.getStringExtra(TaskFinderActivity.SELECTED_RISKMODEL);
       System.out.println(riskmodel == null ? "" : riskmodel);
       String taskName = intent.getStringExtra(TaskFinderActivity.SELECTED_TASK_NAME);
       System.out.println(taskName);
 
       new GetMatchingTasksInBackground(ShowTasksActivity.this, taskType, owner, portfolio, benchmark, riskmodel, taskName,
                this.taskTypeDescToTaskTypeMap, this)
                .execute();
    }
 
    @Override
    public void postProcessing(String results) {
       // Create the adapter that will return a fragment for each of the three
       // primary sections of the app.
       mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), results);
 
       // Set up the ViewPager with the sections adapter.
       mViewPager = (ViewPager) findViewById(R.id.pager);
       mViewPager.setAdapter(mSectionsPagerAdapter);
    }
 
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
       private final Multimap<String, String> taskTypeToNameMap = ArrayListMultimap.create();
 
       public SectionsPagerAdapter(FragmentManager fm, String results) {
          super(fm);
 
          try {
             JSONArray entries = new JSONArray(results);
 
             for (int i = 0; i < entries.length(); i++) {
                JSONArray entry = (JSONArray) entries.get(i);
                final String taskType = entry.getString(0);
                final String rawName = entry.getString(1);
                String cleansedName = IdentityName.valueOf(rawName).getName();
                tasksCleansedNameToRawNameMap.put(cleansedName, rawName);
                taskTypeToNameMap.put(taskType, cleansedName);
             }
          } catch (JSONException e) {
             Log.e("JSON Parser", "Error parsing data " + e.toString());
          }
       }
 
       @Override
       public Fragment getItem(int position) {
          Pair<String, Collection<String>> fragmentTasks = getTasksForFragment(position);
          String taskType = fragmentTasks.first;
          Collection<String> tasks = fragmentTasks.second;
 
          Fragment fragment = new TasksSectionFragment();
          Bundle args = new Bundle();
          args.putString(TasksSectionFragment.TASK_TYPE, taskType);
          args.putString(TasksSectionFragment.TASK_TYPE_DESC, taskTypeDescToTaskTypeMap.inverse().get(taskType));
          args.putStringArrayList(TasksSectionFragment.TASK_NAMES, new ArrayList<String>(tasks));
          args.putStringArrayList(TasksSectionFragment.TASK_RAW_NAMES, new ArrayList<String>(this.getRawTaskNames(tasks)));
          fragment.setArguments(args);
          return fragment;
       }
 
       @Override
       public int getCount() {
          // Show 10 pages.
          return 10;
       }
 
       @Override
       public CharSequence getPageTitle(int position) {
          Locale l = Locale.getDefault();
          switch (position) {
             case 0:
                return getString(R.string.ra_tasks).toUpperCase(l);
             case 1:
                return getString(R.string.fbpa_tasks).toUpperCase(l);
             case 2:
                return getString(R.string.rbpa_tasks).toUpperCase(l);
             case 3:
                return getString(R.string.report_tasks).toUpperCase(l);
             case 4:
                return getString(R.string.rmm_tasks).toUpperCase(l);
             case 5:
                return getString(R.string.ax_import_tasks).toUpperCase(l);
             case 6:
                return getString(R.string.port_import_tasks).toUpperCase(l);
             case 7:
                return getString(R.string.att_import_tasks).toUpperCase(l);
             case 8:
                return getString(R.string.comp_import_tasks).toUpperCase(l);
             case 9:
                return getString(R.string.classif_import_tasks).toUpperCase(l);
          }
          return null;
       }
 
       private Pair<String, Collection<String>> getTasksForFragment(final int position) {
          switch (position) {
             case 0:
                return new Pair<String, Collection<String>>("RISK_ANALYSIS", this.taskTypeToNameMap.get("RISK_ANALYSIS"));
             case 1:
                return new Pair<String, Collection<String>>("PERFORMANCE_ATTRIBUTION_FACTOR",
                         this.taskTypeToNameMap.get("PERFORMANCE_ATTRIBUTION_FACTOR"));
             case 2:
                return new Pair<String, Collection<String>>("PERFORMANCE_ATTRIBUTION_RETURNS",
                         this.taskTypeToNameMap.get("PERFORMANCE_ATTRIBUTION_RETURNS"));
             case 3:
                return new Pair<String, Collection<String>>("REPORT", this.taskTypeToNameMap.get("REPORT"));
             case 4:
                return new Pair<String, Collection<String>>("RISK_MODEL_MACHINE", this.taskTypeToNameMap.get("RISK_MODEL_MACHINE"));
             case 5:
                return new Pair<String, Collection<String>>("IMPORT_AXIOMA_DATA", this.taskTypeToNameMap.get("IMPORT_AXIOMA_DATA"));
             case 6:
                return new Pair<String, Collection<String>>("IMPORT_PORTFOLIO_DATA",
                         this.taskTypeToNameMap.get("IMPORT_PORTFOLIO_DATA"));
             case 7:
                return new Pair<String, Collection<String>>("IMPORT_ATTRIBUTE_DATA",
                         this.taskTypeToNameMap.get("IMPORT_ATTRIBUTE_DATA"));
             case 8:
                return new Pair<String, Collection<String>>("IMPORT_COMPOSITE_DATA",
                         this.taskTypeToNameMap.get("IMPORT_COMPOSITE_DATA"));
             case 9:
                return new Pair<String, Collection<String>>("IMPORT_CLASSIFICATION_DATA",
                        this.taskTypeToNameMap.get("IMPORT_CLASSIFICATION_DATA"));
          }
 
          return null;
       }
 
       private List<String> getRawTaskNames(final Collection<String> cleansedTaskNames) {
          List<String> rawTaskNames = Lists.newArrayList();
          for (String cleansedName : cleansedTaskNames) {
             rawTaskNames.add(tasksCleansedNameToRawNameMap.get(cleansedName));
          }
 
          return rawTaskNames;
       }
    }
 
    public static class TasksSectionFragment extends ListFragment {
       public static final String TASK_NAMES = "task_names";
       public static final String TASK_RAW_NAMES = "task_raw_names";
       public static final String TASK_TYPE = "task_type";
       public static final String TASK_TYPE_DESC = "task_type_desc";
 
       private List<String> taskNames = Lists.newArrayList();
       private List<String> taskRawNames = Lists.newArrayList();
       private String taskType = null;
       private String taskTypeDesc = null;
 
       public TasksSectionFragment() {
       }
 
       @Override
       public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
          View rootView = inflater.inflate(R.layout.fragment_task_names, container, false);
 
          this.taskType = getArguments().getString(TasksSectionFragment.TASK_TYPE);
          this.taskTypeDesc = getArguments().getString(TasksSectionFragment.TASK_TYPE_DESC);
          this.taskNames = getArguments().getStringArrayList(TasksSectionFragment.TASK_NAMES);
          this.taskRawNames = getArguments().getStringArrayList(TasksSectionFragment.TASK_RAW_NAMES);
          //         List<Map<String, String>> taskNamesListForAdapter = Lists.newArrayList();
          //         for (String taskName : taskNames) {
          //            Map<String, String> entry = Maps.newHashMap();
          //            entry.put(TASK_NAME, taskName);
          //            taskNamesListForAdapter.add(entry);
          //         }
 
          //         ListView lv = (ListView) findViewById(android.R.id.list);
 
          ArrayAdapter<String> adapter =
                   new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,
                            taskNames.toArray(new String[taskNames.size()]));
 
          setListAdapter(adapter);
 
          return rootView;
       }
 
       @Override
       public void onListItemClick(ListView l, View v, int position, long id) {
          String taskName = this.taskNames.get(position);
          String taskRawName = this.taskRawNames.get(position);
 
          Intent intent = new Intent(getActivity(), ShowTaskDetailsActivity.class);
          intent.putExtra(ShowTasksActivity.SELECTED_TASK_TYPE, taskType);
          intent.putExtra(ShowTasksActivity.SELECTED_TASK_TYPE_DESC, taskTypeDesc);
          intent.putExtra(ShowTasksActivity.SELECTED_TASK_NAME, taskName);
          intent.putExtra(ShowTasksActivity.SELECTED_TASK_RAW_NAME, taskRawName);
          startActivity(intent);
       }
    }
 }
