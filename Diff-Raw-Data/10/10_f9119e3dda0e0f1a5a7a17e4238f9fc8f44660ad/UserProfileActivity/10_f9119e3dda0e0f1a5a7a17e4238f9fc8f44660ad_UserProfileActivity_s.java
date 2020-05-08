 /*
  * Copyright (C) 2013 Andy Klay, Christoph ott
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.fhb.mi.paperfly.user;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.Fragment;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.location.Location;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.common.GooglePlayServicesUtil;
 
 import de.fhb.mi.paperfly.HelpActivity;
 import de.fhb.mi.paperfly.R;
 import de.fhb.mi.paperfly.SettingsActivity;
 import de.fhb.mi.paperfly.dto.AccountDTO;
 import de.fhb.mi.paperfly.service.BackgroundLocationService;
 import de.fhb.mi.paperfly.service.BackgroundLocationService.LocationBinder;
 import de.fhb.mi.paperfly.service.RestConsumerService;
 
 /**
  * This activity show detail information of a user
  *
  * @author Christoph Ott
  * @author Andy Klay (klay@fh-brandenburg.de)
  */
 public class UserProfileActivity extends Activity {
 
     public static final String ARGS_USER = "user";
     /*
      * Define a request code to send to Google Play services
      * This code is returned in Activity.onActivityResult
      */
     private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
     private static final String TAG = "UserProfileActivity";
 
     private BackgroundLocationService mBackgroundLocationService;
     private boolean mBoundLocationService = false;
     private ServiceConnection mConnectionLocationService = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className, IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
             LocationBinder binder = (LocationBinder) service;
             mBackgroundLocationService = binder.getServerInstance();
             mBoundLocationService = true;
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             mBoundLocationService = false;
         }
     };
 
 
     private Intent getMapsIntent() {
         //TODO java.lang.Object irgendwo an der FH
         double latitude = 52.411433;
         double longitude = 12.536933;
 
         Location currentLocation = mBackgroundLocationService.getCurrentLocation();
 
         double currentLatitude = currentLocation.getLatitude();
         double currentLongitude = currentLocation.getLongitude();
 
         String url = "http://maps.google.com/maps?saddr=" + currentLatitude + "," + currentLongitude + "&daddr=" + latitude + "," + longitude + "&dirflg=w";
         Intent intent = new Intent(Intent.ACTION_VIEW);
         intent.setData(Uri.parse(url));
         return intent;
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         // Decide what to do based on the original request code
         switch (requestCode) {
             case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                 /*
                  * If the result code is Activity.RESULT_OK, try
                  * to connect again
                  */
                 switch (resultCode) {
                     case Activity.RESULT_OK:
                         break;
                 }
         }
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_profile);
 
         if (savedInstanceState == null) {
             getFragmentManager().beginTransaction()
                     .add(R.id.container, new PlaceholderFragment())
                     .commit();
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.user_profile, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle action bar item clicks here. The action bar will
         // automatically handle clicks on the Home/Up button, so long
         // as you specify a parent activity in AndroidManifest.xml.
         switch (item.getItemId()) {
             case R.id.action_settings:
                 startActivity(new Intent(this, SettingsActivity.class));
                 return true;
             case R.id.action_help:
                 startActivity(new Intent(this, HelpActivity.class));
                 return true;
             case R.id.action_maps:
                 if (BackgroundLocationService.servicesAvailable(this)) {
                     startActivity(getMapsIntent());
                 } else {
                     int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
 
                     // Display an error dialog
                     Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
                     if (dialog != null) {
                         ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                         errorFragment.setDialog(dialog);
                         errorFragment.show(getFragmentManager(), TAG);
                     }
                 }
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         Intent serviceIntent = new Intent(this, BackgroundLocationService.class);
         bindService(serviceIntent, mConnectionLocationService, Context.BIND_AUTO_CREATE);
     }
 
     @Override
     protected void onStop() {
         // Disconnecting the client invalidates it.
         super.onStop();
         if (mBoundLocationService) {
             unbindService(mConnectionLocationService);
             mBoundLocationService = false;
         }
     }
 
     public static class ErrorDialogFragment extends DialogFragment {
         // Global field to contain the error dialog
         private Dialog mDialog;
 
         // Default constructor. Sets the dialog field to null
         public ErrorDialogFragment() {
             super();
             mDialog = null;
         }
 
         // Return a Dialog to the DialogFragment.
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             return mDialog;
         }
 
         // Set the dialog to display
         public void setDialog(Dialog dialog) {
             mDialog = dialog;
         }
     }
 
 //    @Override
 //    public void onBackPressed() {
 //
 //        if(getIntent().getExtras().getString(FROM).equals(NavKey.FRIENDLIST.toString())){
 //            //TODO
 //            Toast.makeText(this, "navigate back to friendslist", Toast.LENGTH_LONG).show();
 //            Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
 //            intent.putExtra(MainActivity.FRAGMENT_BEFORE, NavKey.FRIENDLIST);
 //            startActivity(intent);
 //            finish();
 //        }else{
 //            super.onBackPressed();
 //        }
 //    }
 
     /**
      * A placeholder fragment containing a simple view.
      */
     public class PlaceholderFragment extends Fragment {
 
         View rootView;
         private TextView profileUsername;
         private TextView profileFirstname;
         private TextView profileLastname;
 
         private GetAccountTask mAccountTask = null;
         AccountDTO account = null;
         String username = null;
 
         /**
          * Begin *************************************** Rest-Connection ****************************** *
          */
         private boolean mBound = false;
         private RestConsumerService mRestConsumerService;
         private ServiceConnection mConnectionRestService = new ServiceConnection() {
 
             @Override
             public void onServiceConnected(ComponentName className, IBinder service) {
                 RestConsumerService.RestConsumerBinder binder = (RestConsumerService.RestConsumerBinder) service;
                 mRestConsumerService = binder.getServerInstance();
                 mBound = true;
                 Toast.makeText(rootView.getContext(), "RestConsumerService Connected", Toast.LENGTH_SHORT)
                         .show();
 
                 mAccountTask = new GetAccountTask();
                 mAccountTask.execute(username);
             }
 
             @Override
             public void onServiceDisconnected(ComponentName arg0) {
                 Toast.makeText(rootView.getContext(), "RestConsumerService Disconnected", Toast.LENGTH_SHORT)
                         .show();
                 mBound = false;
                 mRestConsumerService = null;
             }
         };
 
         /**
          * End *************************************** Rest-Connection ****************************** *
          */
 
 
         public PlaceholderFragment() {
         }
 
         private void initViews(View rootView) {
             profileUsername = (TextView) rootView.findViewById(R.id.profileUsername);
             profileFirstname = (TextView) rootView.findViewById(R.id.profileFirstname);
             profileLastname = (TextView) rootView.findViewById(R.id.profileLastname);
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                  Bundle savedInstanceState) {
             rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);
             initViews(rootView);
             username = getActivity().getIntent().getExtras().getString(ARGS_USER);
             return rootView;
         }
 
         @Override
         public void onStop() {
             super.onStop();
             Log.d(TAG, "onStop");
 
             super.onStop();
             if (mBound) {
                 rootView.getContext().unbindService(mConnectionRestService);
                 mBound = false;
             }
         }
 
         @Override
         public void onAttach(Activity activity) {
             super.onAttach(activity);
             Log.d(TAG, "onAttach");
             Intent serviceIntent = new Intent(activity.getBaseContext(), RestConsumerService.class);
             mBound = activity.getBaseContext().bindService(serviceIntent, mConnectionRestService, Context.BIND_IMPORTANT);
         }
 
         /**
          * Represents an asynchronous GetAccountTask used to get an user
          */
         public class GetAccountTask extends AsyncTask<String, Void, Boolean> {
 
             @Override
             protected Boolean doInBackground(String... params) {
                 String username = params[0];
 
                account = mRestConsumerService.getAccountByUsername(username);
 
                 if (account != null) {
                     return true;
                 } else {
                     return false;
                 }
             }
 
             @Override
             protected void onPostExecute(final Boolean success) {
                 mAccountTask = null;
 
                 if (success) {
                     Log.d("onPostExecute", "success");
 
                     if (mRestConsumerService != null) {
                         Log.d(TAG, "mRestConsumerService exists");
 
                         if (account != null) {
                             profileUsername.append(account.getUsername());
                             profileFirstname.append(account.getFirstName());
                             profileLastname.append(account.getLastName());
                         }
                     } else {
                         Log.d(TAG, "create dummy entries");
 
                         profileUsername.append("unknown user");
                         profileFirstname.append("unknown user");
                         profileLastname.append("unknown user");
                     }
 
                 } else {
                     Log.d("onPostExecute", "no success");
                 }
             }
         }
     }
 
 
 }
