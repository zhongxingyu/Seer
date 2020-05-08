 package nl.napauleon.sabber.history;
 
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.app.SherlockListFragment;
 import nl.napauleon.sabber.ContextHelper;
 import nl.napauleon.sabber.MainActivity;
 import nl.napauleon.sabber.R;
 import nl.napauleon.sabber.http.DefaultErrorCallback;
 import nl.napauleon.sabber.http.HttpGetMockTask;
 import nl.napauleon.sabber.http.HttpGetTask;
 import nl.napauleon.sabber.http.SabNzbConnectionHelper;
 import org.json.JSONException;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class HistoryFragment extends SherlockListFragment{
 
     private List<HistoryInfo> historyItems;
     private ContextHelper contextHelper;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        return inflater.inflate(R.layout.itemlist, container, false);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         contextHelper = new ContextHelper(getActivity());
         retrieveData();
     }
 
     public void retrieveData() {
     	SharedPreferences preferences = contextHelper.checkAndGetSettings();
         ((MainActivity) getSherlockActivity()).setRefreshing(true);
     	executeRequest(preferences);
     }
 
 	private void executeRequest(SharedPreferences preferences) {
 		if (preferences != null) {
             if (contextHelper.isMockEnabled()) {
 	    		new HttpGetMockTask(new HistoryFragmentCallback()).execute("history/historyresult");
 	    	} else {
 	    		new HttpGetTask(new HistoryFragmentCallback()).execute(new SabNzbConnectionHelper(preferences).createHistoryConnectionString());
 	    	}
         }
 	}
 
     public class HistoryFragmentCallback extends DefaultErrorCallback {
         public HistoryFragmentCallback() {
             super();
         }
 
 		public void handleError(String error) {
         	stopSpinner();
 			super.handleError(HistoryFragment.this.getActivity(), error);
 		}
 
 		public void handleTimeout() {
 			stopSpinner();
 			super.handleTimeout(HistoryFragment.this.getActivity());
 		}
 		
 		public void handleResponse(String response) {
 			stopSpinner();
 			handleResult(response);
 		}
 
         private void handleResult(String response) {
             try {
                 historyItems = HistoryInfo.createHistoryList(response);
                 contextHelper.updateLastPollingEvent(System.currentTimeMillis());
                 if (getActivity() != null) {
                     setListAdapter(new HistoryListAdapter(getActivity(), historyItems));
                 }
             } catch (JSONException e) {
                 contextHelper.handleJsonException(response, e);
             }
         }
     }
 
 
 
     private void stopSpinner() {
         MainActivity activity = (MainActivity) getActivity();
         if (activity != null) {
             activity.setRefreshing(false);
         }
     }
 
     //for test purposes
     public ArrayList<HistoryInfo> getHistoryItems() {
         return new ArrayList<HistoryInfo>(historyItems);
     }
 }
