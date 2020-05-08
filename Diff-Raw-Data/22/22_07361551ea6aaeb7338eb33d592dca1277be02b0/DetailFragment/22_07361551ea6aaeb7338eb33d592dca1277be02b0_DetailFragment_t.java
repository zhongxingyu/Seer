 package com.example.toniotest;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import com.example.toniotest.utils.BoilerPipeTask;
 import com.example.toniotest.utils.DownloadWebTask;
 import com.example.toniotest.utils.RSSParseTask;
 
 /**
  * Created by gnuton on 5/18/13.
  */
 public class DetailFragment extends Fragment implements BoilerPipeTask.OnBoilerplateRemovedListener {
     private static final String TAG = "DETAIL_FRAGMENT";
     private RSSParseTask.Entry entry = null;
    private AsyncTask task = null;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         Log.d(TAG, "Create view");
         //return super.onCreateView(inflater, container, savedInstanceState);
        // Created when FAsyncTask<String,Void,String>ragment needs to crerate its UI
         View view = inflater.inflate(R.layout.detail_fragment, container, false);
 
         return view;
     }
 
     @Override
     public void onStart() {
         super.onStart();
         Log.d(TAG, "START");
         // called when fragment is visible
         if (entry != null) {
             setEntry(entry);
         }
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
        if (this.task != null)
            this.task.cancel(true);
         Log.d(TAG, "DESTROY");
     }
 
     @Override
     public void onDetach() {
         super.onDetach();
         Log.d(TAG, "DETACH");
     }
 
     public void setEntry(RSSParseTask.Entry entry) {
         Log.d(TAG,"Set entry");
         if (getView() == null) {
             this.entry = entry;
             return;
         }
 
         //Set Title
         TextView view = (TextView) getView().findViewById(R.id.TitleTextView);
 
         view.setText(entry.title);
 
         //Load page
         Context c = getActivity().getApplicationContext();
         ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
         if (networkInfo != null && networkInfo.isConnected()) {
             // fetch data
            this.task = new BoilerPipeTask(this).execute(entry.link);
         } else {
             Log.w(TAG, "Device not connected");
             //TODO display error (use notification API?)
         }
     }
 
     @Override
     public void onBoilerplateRemoved(String buffer) {
         Log.d(TAG, "Page Downloaded");
        View v = getView();
        if (v == null)
            return;

        TextView view = (TextView) v.findViewById(R.id.ContentTextView);
         if (view != null)
             view.setText(buffer);

        this.task = null;
     }
 }
