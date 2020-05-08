 package com.janrain.android.quicksignin;
 
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.*;
 import android.widget.*;
 import com.janrain.android.engage.types.JRDictionary;
 
 import java.util.ArrayList;
 
 /**
  * Created by IntelliJ IDEA.
  * User: lillialexis
  * Date: 4/6/11
  * Time: 3:46 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ProfileDetailActivity extends ListActivity {
     /**
      * Array adapter used to render individual providers in list view.
      */
     private class ProfileAdapter extends ArrayAdapter<String> {
         private int mResourceId;
 
         public ProfileAdapter(Context context, int resId, ArrayList<String> items) {
             super(context, -1, items);
 
             mResourceId = resId;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View v = convertView;
             if (v == null) {
                 LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 v = li.inflate(mResourceId, null);
                 Log.i(TAG, "[getView] with null converView");
             } else Log.i(TAG, "[getView] with non null convertView");
 
             TextView keyLabel = (TextView)v.findViewById(R.id.row_profile_detail_key);
             TextView valueLabel = (TextView)v.findViewById(R.id.row_profile_detail_value);
 
             String key = getItem(position);
 
             keyLabel.setText(key);
 
            // TODO: Flatten subdictionaries
            if (key.equals("name") || key.equals("address"))
                valueLabel.setText("TODO: FLATTEN SUBDICTIONARIES");
            else
                valueLabel.setText(mProfile.getAsString(key));

             return v;
         }
     }
 
     // ------------------------------------------------------------------------
     // STATIC FIELDS
     // ------------------------------------------------------------------------
 
     private static final String TAG = ProfileDetailActivity.class.getSimpleName();
 
     // ------------------------------------------------------------------------
     // STATIC INITIALIZERS
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // STATIC METHODS
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // FIELDS
     // ------------------------------------------------------------------------
 
     private ProfileAdapter mAdapter;
     private ProfileData mProfileData;
     private ArrayList<String> mProfileKeys;
     private JRDictionary mProfile;
 
     // ------------------------------------------------------------------------
     // INITIALIZERS
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // CONSTRUCTORS
     // ------------------------------------------------------------------------
 
     public ProfileDetailActivity() {
     }
 
     // ------------------------------------------------------------------------
     // METHODS
     // ------------------------------------------------------------------------
 
     /**
      * Called when the activity is first created.
      *
      * @param savedInstanceState
      *      If the activity is being re-initialized after previously being shut down then this
      *      Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
      *      Note: Otherwise it is null.
      */
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.profile_detail_listview);
 
 
         mProfileData = ProfileData.getInstance();
         mProfile = mProfileData.getCurrentProfile();
 
         mProfileKeys = new ArrayList<String>(mProfile.keySet());
 
         if (mProfileKeys == null) {
             mProfileKeys = new ArrayList<String>();
         }
 
         mAdapter = new ProfileAdapter(this, R.layout.profile_detail_listview_row, mProfileKeys);
         setListAdapter(mAdapter);
     }
 
     public void onResume () {
         super.onResume();
     }
 
     @Override
     protected void onStart() {
         super.onStart();
 
         Log.d(TAG, "onStart");
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
     }
 
     /**
      * This method will be called when an item in the list is selected.
      */
     @Override
     protected void onListItemClick(ListView l, View v, int pos, long id) {
     }
 
     /**
      * Initialize the contents of the Activity's standard options menu.
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // use the shared menu
         return true;
     }
 
     /**
      * This hook is called whenever an item in your options menu is selected.
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         return super.onOptionsItemSelected(item);
     }
 
     /**
      * Callback for creating dialogs that are managed.
      */
     protected Dialog onCreateDialog(int id) {
         return null;
     }
 
     public void tryToFinishActivity() {
         Log.i(TAG, "[tryToFinishActivity]");
         finish();
     }
 }
