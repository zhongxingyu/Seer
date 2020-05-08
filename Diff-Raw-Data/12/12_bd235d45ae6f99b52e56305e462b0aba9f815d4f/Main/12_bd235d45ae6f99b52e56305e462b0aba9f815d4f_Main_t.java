 
 package org.projectvoodoo.gstandroid.activities;
 
 import org.projectvoodoo.gstandroid.R;
 import org.projectvoodoo.gstandroid.utils.GstPluginsAdapter;
 import org.projectvoodoo.gstandroid.utils.Utils;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.widget.ExpandableListView;
 
 public class Main extends Activity {
 
     private static final String TAG = "Gstreamer Android Main";
 
     private ExpandableListView mGstPluginsListView;
     private GstPluginsAdapter mGstPluginsAdapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         mGstPluginsAdapter = new GstPluginsAdapter();
         mGstPluginsListView = (ExpandableListView) findViewById(R.id.gst_plugins);
         mGstPluginsListView.setAdapter(mGstPluginsAdapter);
         mGstPluginsListView.setFastScrollEnabled(true);
 
         new InstallBinariesTask().execute();
 
     }
 
     private class InstallBinariesTask extends AsyncTask<Void, Void, Boolean> {
 
         @Override
         protected Boolean doInBackground(Void... params) {
             return Utils.installBinaries();
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
            if (result && mGstPluginsAdapter != null)
                 mGstPluginsAdapter.notifyDataSetChanged();
         }
     }
 }
