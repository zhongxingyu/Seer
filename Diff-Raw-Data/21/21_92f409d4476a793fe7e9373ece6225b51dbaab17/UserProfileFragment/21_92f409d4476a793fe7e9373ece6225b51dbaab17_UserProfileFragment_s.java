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
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CompoundButton;
 import android.widget.Switch;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.common.GooglePlayServicesUtil;
 
 import de.fhb.mi.paperfly.HelpActivity;
 import de.fhb.mi.paperfly.PaperFlyApp;
 import de.fhb.mi.paperfly.R;
 import de.fhb.mi.paperfly.SettingsActivity;
 import de.fhb.mi.paperfly.dto.AccountDTO;
 import de.fhb.mi.paperfly.service.BackgroundLocationService;
 import de.fhb.mi.paperfly.service.RestConsumerException;
 import de.fhb.mi.paperfly.service.RestConsumerSingleton;
 import lombok.NoArgsConstructor;
 
 /**
  * A {@link android.app.Fragment} for showing a user profile.
  */
 @NoArgsConstructor
 public class UserProfileFragment extends Fragment {
 
     public static final String TAG = UserProfileFragment.class.getSimpleName();
     private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
     public static final String ARGS_USER = "user";
     public static final String ARGS_MY_ACCOUNT = "myaccount";
 
     private View rootView;
     private Switch friendSwitch;
     private TextView profileUsername;
     private TextView profileFirstname;
     private TextView profileLastname;
 
     private GetAccountTask mAccountTask = null;
     private AccountDTO account = null;
     private boolean myAccount;
 
     private String username = null;
     private BackgroundLocationService mBackgroundLocationService;
     private boolean mBoundLocationService = false;
     private ServiceConnection mConnectionLocationService = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className, IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
             BackgroundLocationService.LocationBinder binder = (BackgroundLocationService.LocationBinder) service;
             mBackgroundLocationService = binder.getServerInstance();
             mBoundLocationService = true;
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             mBoundLocationService = false;
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(TAG, "onCreate");
         setHasOptionsMenu(true);
         myAccount = getArguments().getBoolean(ARGS_MY_ACCOUNT, false);
         username = getArguments().getString(ARGS_USER);
 
         mAccountTask = new GetAccountTask();
         mAccountTask.execute(username);
     }
 
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
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
 
     private void initViews(View rootView) {
         friendSwitch = (Switch) rootView.findViewById(R.id.friendSwitch);
         profileUsername = (TextView) rootView.findViewById(R.id.profileUsername);
         profileFirstname = (TextView) rootView.findViewById(R.id.profileFirstname);
         profileLastname = (TextView) rootView.findViewById(R.id.profileLastname);
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         if (menu != null) {
             inflater.inflate(R.menu.user_profile, menu);
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle action bar item clicks here. The action bar will
         // automatically handle clicks on the Home/Up button, so long
         // as you specify a parent activity in AndroidManifest.xml.
         switch (item.getItemId()) {
             case R.id.action_settings:
                 startActivity(new Intent(getActivity(), SettingsActivity.class));
                 return true;
             case R.id.action_help:
                 startActivity(new Intent(getActivity(), HelpActivity.class));
                 return true;
             case R.id.action_maps:
                 if (BackgroundLocationService.servicesAvailable(getActivity())) {
                     startActivity(getMapsIntent());
                 } else {
                     int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
 
                     // Display an error dialog
                     Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), 0);
                     if (dialog != null) {
                         ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                         errorFragment.setDialog(dialog);
                         errorFragment.show(getActivity().getFragmentManager(), TAG);
                     }
                 }
                 return true;
             case R.id.action_edit_account:
                 Fragment fragment = new AccountEditFragment();
                 Bundle args = new Bundle();
                 args.putString(UserProfileFragment.ARGS_USER, username);
                 fragment.setArguments(args);
 
                 getFragmentManager().beginTransaction()
                         .replace(R.id.content_frame, fragment)
                         .commit();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);
         initViews(rootView);
         if (myAccount) {
             friendSwitch.setVisibility(View.INVISIBLE);
         } else {
             friendSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                 @Override
                 public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                     // TODO update account
                     if (isChecked) {
                        Toast.makeText(getActivity(), account.getUsername() + "is now your friend", Toast.LENGTH_SHORT).show();
                     } else {
                        Toast.makeText(getActivity(), account.getUsername() + "is not your friend", Toast.LENGTH_SHORT).show();
                     }
                 }
             });
         }
         return rootView;
     }
 
     @Override
     public void onStart() {
         super.onStart();
         if (((PaperFlyApp) getActivity().getApplication()).isMyServiceRunning(BackgroundLocationService.class)) {
             Intent serviceIntent = new Intent(getActivity(), BackgroundLocationService.class);
             getActivity().bindService(serviceIntent, mConnectionLocationService, Context.BIND_AUTO_CREATE);
         }
     }
 
     @Override
     public void onStop() {
         // Disconnecting the client invalidates it.
         super.onStop();
         if (mBoundLocationService) {
             getActivity().unbindService(mConnectionLocationService);
             mBoundLocationService = false;
         }
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         Log.d(TAG, "onAttach");
     }
 
     /**
      * Represents an asynchronous GetMyAccountTask used to get an user
      */
     public class GetAccountTask extends AsyncTask<String, Void, Boolean> {
 
         @Override
         protected Boolean doInBackground(String... params) {
             String username = params[0];
 
             try {
                 Log.d(TAG, "Searching for user:" + username);
                 account = RestConsumerSingleton.getInstance().getAccountByUsername(username);
             } catch (RestConsumerException e) {
                 Log.e(TAG, e.getMessage(), e);
             }
             return account != null;
         }
 
         @Override
         protected void onPostExecute(final Boolean success) {
             mAccountTask = null;
 
             if (success) {
                 Log.d("onPostExecute", "success");
 
                 if (account != null) {
                     profileUsername.append(account.getUsername());
                     profileFirstname.append(account.getFirstName());
                     profileLastname.append(account.getLastName());
                 }
 
             } else {
                 Toast.makeText(rootView.getContext(), "Failed to load account!", Toast.LENGTH_SHORT)
                         .show();
             }
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
 }
