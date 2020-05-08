 package org.ktln2.android.callstat;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.*;
 
 import android.support.v4.app.Fragment;
 import android.content.Intent;
 import android.os.AsyncTask;
 
 import android.database.Cursor;
 
 import android.widget.TextView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.os.Bundle;
 import android.provider.CallLog.Calls;
 
 import android.animation.AnimatorListenerAdapter;
 import android.animation.Animator;
 
 import com.google.ads.*;
 
 
 public class MainActivity extends SherlockFragmentActivity {
     private AdView mAdView;
     private ContactsCallsFragment mContactsCallsFragment;
 
     public static StatisticsMap map;
 
     private final static String mMainHeaderFormatString = "%d calls from %d contacts";
     private final static String mSubHeaderFormatString = "%s (%s in average)";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.main);
 
         // if we came from a previous instance don't reattach
         // the fragments
         if (savedInstanceState != null)
             return;
 
         mContactsCallsFragment = new ContactsCallsFragment();
 
         getSupportFragmentManager()
             .beginTransaction()
             .add(R.id.main_container, mContactsCallsFragment)
             .commit();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         int ordering_type = 0;
         switch (item.getItemId()) {
             case R.id.ordering_total_duration:
                 ordering_type = CallStatAdapter.CALL_STAT_ADAPTER_ORDERING_TOTAL_DURATION;
                 break;
             case R.id.ordering_total_calls:
                 ordering_type = CallStatAdapter.CALL_STAT_ADAPTER_ORDERING_TOTAL_CALLS;
                 break;
             case R.id.ordering_avg_duration:
                 ordering_type = CallStatAdapter.CALL_STAT_ADAPTER_ORDERING_AVG_DURATION;
                 break;
             case R.id.ordering_max_duration:
                 ordering_type = CallStatAdapter.CALL_STAT_ADAPTER_ORDERING_MAX_DURATION;
                 break;
             case R.id.ordering_min_duration:
                 ordering_type = CallStatAdapter.CALL_STAT_ADAPTER_ORDERING_MIN_DURATION;
                 break;
 
             case R.id.graph:
                 Intent graphIntent = new Intent(this, GraphActivity.class);
                 startActivity(graphIntent);
         }
         // FIXME: we don't want to order if R.id.graph was selected
         mContactsCallsFragment.order(ordering_type);
 
         return super.onOptionsItemSelected(item);
     }
 
     public class ContactsCallsFragment extends Fragment {
         private CallStatAdapter mAdapter;
         private ListView mListView;
         private View mEmptyView;
         private ProgressBar mSpinner;
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
             View fragmentLayout = inflater.inflate(R.layout.contacts_calls, null);
 
             mListView = (ListView)fragmentLayout.findViewById(R.id.list);
             mEmptyView = fragmentLayout.findViewById(android.R.id.empty);
             mSpinner = (ProgressBar)fragmentLayout.findViewById(R.id.loading_spinner);
 
             mListView.setEmptyView(mEmptyView);
             mListView.setAdapter(mAdapter);
 
             loadData();
 
             return fragmentLayout;
         }
 
         private void loadData() {
             new CallLoader().execute();
         }
 
         private void toggleLoader() {
             if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR1) {
                 // Animate the loading view to 0% opacity. After the animation ends,
                 // set its visibility to GONE as an optimization step (it won't
                 // participate in layout passes, etc.)
                 mSpinner.animate()
                     .alpha(0f)
                     .setDuration(500)
                     .setListener(new AnimatorListenerAdapter() {
                             @Override
                             public void onAnimationEnd(Animator animation) {
                                 mSpinner.setVisibility(View.GONE);
                             }
                         });
             } else {
                 mSpinner.setVisibility(View.GONE);
             }
         }
 
         public void order(int ordering_type) {
             mAdapter.order(ordering_type);
         }
 
         public void update() {
             ((TextView)findViewById(R.id.n_calls)).setText(
                 String.format(mMainHeaderFormatString, map.getTotalCalls(), map.getTotalContacts())
             );
             ((TextView)findViewById(R.id.n_contacts)).setText(
                 String.format(
                     mSubHeaderFormatString,
                     DateUtils.formatElapsedTimeNG(map.getTotalDuration()),
                     map.getTotalCalls() > 0 ?
                         DateUtils.formatElapsedTimeNG(map.getTotalDuration()/map.getTotalCalls()) :
                         "0"
                 )
             );
             mListView.setAdapter(mAdapter);
             mAdapter.notifyDataSetChanged();
         }
 
         /*
          * This class is used to load data asyncronously.
          *
          * The loader is not sufficient since we need to parse the CursorLoader
          * result with time expensive operation that would block the UI thread.
          */
         private class CallLoader extends AsyncTask<Void, Void, StatisticsMap> {
             @Override
             protected StatisticsMap doInBackground(Void... params) {
                 Cursor cursor = getActivity().getContentResolver().query(
                     Calls.CONTENT_URI, null, null, null, null
                 );
 
                 if (cursor.getCount() == 0) {
                     return null;
                 }
 
                 StatisticsMap smap = new StatisticsMap(cursor, getActivity());
 
                 cursor.close();
 
                 return smap;
             }
 
             @Override
             public void onPostExecute(StatisticsMap map) {
                 MainActivity.this.map = map;
                 if (map != null) {
                     mAdapter = new CallStatAdapter(getActivity(), map);
                     update();
                 }
                 toggleLoader();
             }
         }
 
     }
 }
