 package nl.napauleon.sabber.queue;
 
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentActivity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.app.SherlockListFragment;
 import nl.napauleon.sabber.Constants;
 import nl.napauleon.sabber.ContextHelper;
 import nl.napauleon.sabber.MainActivity;
 import nl.napauleon.sabber.R;
 import nl.napauleon.sabber.http.*;
 import nl.napauleon.sabber.search.GlobalInfo;
 import org.apache.commons.lang3.StringUtils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class DownloadingFragment extends SherlockListFragment {
 
     private TextView timeLeftView, speedView, sizeView, etaView;
     private QueueClickListener itemClickListener;
     private List<QueueInfo> queueItems;
     private Handler backgroundHandler = new Handler();
     private Runnable backgroundUpdater = new Runnable() {
         public void run() {
             if (refreshrate > 0) {
                 retrieveQueueData();
                 backgroundHandler.postDelayed(this, refreshrate);
             }
         }
     };
     private int refreshrate;
     private HttpGetTask httpGetTask;
     private ContextHelper contextHelper;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         itemClickListener = new QueueClickListener();
         httpGetTask = new HttpGetTask(new DownloadingCallback());
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.downloading_fragment, container, false);
         timeLeftView = (TextView) view.findViewById(R.id.timeleft);
         speedView = (TextView) view.findViewById(R.id.speed);
         sizeView = (TextView) view.findViewById(R.id.size);
         etaView = (TextView) view.findViewById(R.id.eta);
         return view;
     }
 
     @Override
     public void onStart() {
         super.onStart();
         getListView().setOnItemClickListener(itemClickListener);
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
     }
 
     @Override
     public void onResume() {
         super.onResume();
         contextHelper = new ContextHelper(getActivity());
         backgroundHandler.removeCallbacks(backgroundUpdater);
         backgroundHandler = new Handler();
        retrieveQueueData();
        
         String refreshratePref = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString(Constants.SCREEN_REFRESHRATE_PREF, "0");
         if (StringUtils.isNotBlank(refreshratePref) && Integer.parseInt(refreshratePref) > 0) {
                 refreshrate = Integer.parseInt(refreshratePref) * 1000;
         		backgroundHandler.postDelayed(backgroundUpdater, refreshrate);
         }
     }
 
     @Override
     public void onPause() {
         super.onPause();
         backgroundHandler.removeCallbacks(backgroundUpdater);
     }
 
     @Override
     public void onStop() {
         super.onStop();
         backgroundHandler.removeCallbacks(backgroundUpdater);
     }
 
     public void retrieveQueueData() {
         SharedPreferences preferences = new ContextHelper(getActivity()).checkAndGetSettings();
         if (preferences != null) {
             getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
             if (httpGetTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                 httpGetTask.cancel(true);
             }
         	httpGetTask = new HttpGetTask(new DownloadingCallback());
             if (contextHelper.isMockEnabled()) {
 	    		new HttpGetMockTask(new DownloadingCallback()).execute("queue/queueresult");
 	    	} else {
 	            httpGetTask.execute(new SabNzbConnectionHelper(preferences).createQueueConnectionString());
 	    	}
         }
     }
 
     public class DownloadingCallback extends DefaultErrorCallback {
         public DownloadingCallback() {
             super();
         }
         
 		public void handleError(String error) {
 			super.handleError(DownloadingFragment.this.getActivity(), error);
 			stopSpinner();
 		}
 
 		public void handleTimeout() {
 			super.handleTimeout(DownloadingFragment.this.getActivity());
 			stopSpinner();
 		}
 
 		public void handleResponse(String response) {
 			stopSpinner();
 			handleResult(response);
 			
 		}
 
         private void handleResult(String result) {
             SabnzbResultHelper sabnzbResultHelper = new SabnzbResultHelper(result);
             togglePause(sabnzbResultHelper.isPaused());
 
             queueItems = sabnzbResultHelper.parseQueueItems();
             FragmentActivity activity = getActivity();
             if (queueItems != null && activity != null) {
                 setListAdapter(new QueueListAdapter(activity, queueItems));
                 itemClickListener.setCategories(sabnzbResultHelper.parseCategories());
                 itemClickListener.setQueueItems(queueItems);
             }
 
             populateGlobalInformation(sabnzbResultHelper.parseGlobalInfo());
         }
     }
 
     private void togglePause(boolean paused) {
         if (getActivity() instanceof MainActivity) {
             ((MainActivity) getActivity()).setPaused(paused);
             if (Build.VERSION.SDK_INT >= 11) {
                 getActivity().invalidateOptionsMenu();
             }
         }
     }
 
     private void stopSpinner() {
         SherlockFragmentActivity sherlockActivity = getSherlockActivity();
         if (sherlockActivity != null) {
             sherlockActivity.setSupportProgressBarIndeterminateVisibility(false);
         }
     }
 
     private void populateGlobalInformation(GlobalInfo globalInfo) {
         if (timeLeftView != null) {
             timeLeftView.setText(globalInfo.getTimeleft());
         }
         if (sizeView != null) {
             sizeView.setText(globalInfo.getSize());
         }
         if (speedView != null) {
             speedView.setText(globalInfo.getSpeed());
         }
         if (etaView != null) {
             etaView.setText(globalInfo.getEta());
         }
     }
 
     //for test purposes
     public List<QueueInfo> getQueueItems() {
         return new ArrayList<QueueInfo>(queueItems);
     }
 }
