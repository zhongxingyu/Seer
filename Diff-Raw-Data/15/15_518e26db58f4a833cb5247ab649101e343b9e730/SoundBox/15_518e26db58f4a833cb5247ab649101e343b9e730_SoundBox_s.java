 package com.jgaunt.Soundbox;
 
// SoundBox imports
import com.jgaunt.Soundbox.R;
//import com.jgaunt.Soundbox.SoundBoxAsyncFetcher.CompletionHandler;
import com.jgaunt.Soundbox.SoundBoxLoginFragment;

 // Android imports
 import android.app.Activity;
 import android.app.FragmentTransaction;
 import android.app.FragmentManager;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Toast;
 
 // Dropbox imports
 import com.dropbox.client2.android.AndroidAuthSession;
 import com.dropbox.client2.DropboxAPI;
 import com.dropbox.client2.session.AccessTokenPair;
 import com.dropbox.client2.session.AppKeyPair;
 import com.dropbox.client2.session.Session.AccessType;
//import com.dropbox.client2.session.TokenPair;
 
 
 public class SoundBox extends Activity
                       implements SoundBoxAsyncFetcher.CompletionHandler,
                                  FragmentManager.OnBackStackChangedListener
 {
     // TODO: Obfuscate/encrypt this
     final static private String APP_KEY = "f0bb7lauf2hwczh";
     final static private String APP_SECRET = "rdlit805hwt3wke";
 
     // To get to the root music folder, full Dropbox access is needed.
     final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;
 
     // The name of the shared preferences file for account related data
     final static private String ACCOUNT_PREFS_NAME = "soundbox-account-prefs";
 
     // Default location we'll search from 
     final static private String DROPBOX_MUSIC_ROOT = "/Music";
 
     // Key names for prefs held in the shared preferences file
     final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
     final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
 
     // Do the network requests on an AsyncTask
     static private SoundBoxAsyncFetcher mFetcher = null;
 
     // TODO: if this is static can we keep from doing yet another connection?
     // member var for hanging on to the AuthSession
     private DropboxAPI<AndroidAuthSession> mDBApi;
 
     // track the login stated
     private boolean mLoggedIn;
 
     // mCurrentPath needs to be static to persist when we rotate
     static private String mCurrentPath = DROPBOX_MUSIC_ROOT;
     static private String mListTag = "krList";
 
     // handle to the UI so we can change the text
     private Button mSubmit;
 
     /* ********************************************************************* *
      *                 Android Activity lifetime mgmt methods                *
      * ********************************************************************* */
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.i("SoundBox", "onCreate()");
         setContentView(R.layout.main);
 
         // Create a session
         AndroidAuthSession session = buildSession();
         mDBApi = new DropboxAPI<AndroidAuthSession>(session);
 
         setLoggedIn(mDBApi.getSession().isLinked());
         if (mLoggedIn) {
             // We are logged in so load the last known path we were looking at
             //Log.i("SoundBox", "in onCreate, calling ShowMusicFolder with " + mCurrentPath);
             showMusicFolder(mCurrentPath);
         } else {
             // We are not logged in so load the fragment for the link button
             showLogin();
         }
     } // onCreate
 
     @Override
     protected void onStart() {
         super.onStart();
         Log.i("SoundBox", "onStart()");
     } // onStart
 
     /** Called after being paused or stopped */
     @Override
     protected void onResume() {
         super.onResume();
         Log.i("SoundBox", "onResume()");
 
         FragmentManager fragMngr = getFragmentManager();
         fragMngr.addOnBackStackChangedListener(this);
 
         AndroidAuthSession session = mDBApi.getSession();
 
         if (session.authenticationSuccessful()) {
             try {
                 // MANDATORY call to complete auth.
                 // Sets the access token on the session
                 session.finishAuthentication();
     
                 AccessTokenPair tokens = session.getAccessTokenPair();
 
                 storeKeys(tokens.key, tokens.secret);
                 setLoggedIn(true);
 
             } catch (IllegalStateException e) {
                 showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                 Log.i("SoundBox", "Error authenticating", e);
             }
         }
 
         if (null == mFetcher) {
             // a null fetcher here means we need to show some content
             //Log.i("SoundBox", "in onResume, calling ShowMusicFolder with " + mCurrentPath);
             showMusicFolder(mCurrentPath);
         } else {
             // If we've just been paused there should be a fetcher and we need
             // to reconnect to it; we've already attempted to showMusicFolder().
             mFetcher.setDropboxAPI(mDBApi);
             mFetcher.registerCompletionHandler(this);
         }
     } // onResume
 
     @Override
     protected void onPause() {
         super.onPause();
         Log.i("SoundBox", "onPause()");
         if (null != mFetcher) {
             mFetcher.setDropboxAPI(null);
             mFetcher.registerCompletionHandler(null);
         }
     } // onPause
 
     @Override
     protected void onRestart() {
         super.onRestart();
         Log.i("SoundBox", "onRestart()");
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         Log.i("SoundBox", "onStop()");
         FragmentManager fragMngr = getFragmentManager();
         fragMngr.removeOnBackStackChangedListener(this);
     } // onStop
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         Log.i("SoundBox", "onDestroy()");
     } // onDestroy
 
     @Override
     public void onSaveInstanceState(Bundle aSavedInstanceState) {
         super.onSaveInstanceState(aSavedInstanceState);
         Log.i("SoundBox", "onSaveInstanceState()");
     } // onSaveInstanceState
 
     @Override
     public void onRestoreInstanceState(Bundle aSavedInstanceState) {
         super.onRestoreInstanceState(aSavedInstanceState);
         Log.i("SoundBox", "onRestoreInstanceState()");
     } // onRestoreInstanceState
 
     /* ********************************************************************* *
      *                           Fragment Listener                           *
      * ********************************************************************* */
 
     public void onBackStackChanged() {
         Log.i("SoundBox", "onBackStackChanged()");
         FragmentManager fragMngr = getFragmentManager();
         Log.i("SoundBox", "onBackStackChanged size is: " + fragMngr.getBackStackEntryCount());
     }
 
     /* ********************************************************************* *
      *                           UI Modification                             *
      * ********************************************************************* */
 
     /**
       * Sets the login state to the value passed in; adjusts
       *  the text on the Button accordingly.
       */
     private void setLoggedIn(boolean loggedIn) {
         mLoggedIn = loggedIn;
         // TODO: figure out where to store the link message for the fragment
         String message = "Default";
         if (loggedIn) {
             message = getString(R.string.unlink_message);
         } else {
             message = getString(R.string.link_message);
         }
         //mSubmit.setText(message);
     }
 
     /** Convenience method to pop a toast message on the screen */
     private void showToast(String msg) {
         Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
         error.show();
     }
 
     private void showLogin() {
         Log.i("SoundBox", "showLogin()");
         // currently only show this when the user is logged out
         // eventually allow calling this when the user is logged in so they
         // can log out.
         if (mLoggedIn) {
             // in this case we need to do a replace, not an add transaction
             return;
         }
 
         SoundBoxLoginFragment loginFrag = SoundBoxLoginFragment.newInstance();
         FragmentTransaction ft = getFragmentManager().beginTransaction();
         ft.add(R.id.main_frame, loginFrag);
         ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
         ft.addToBackStack(null);
         ft.commit();
 
         // Get the UI for the button and set an event listener
         mSubmit = (Button)findViewById(R.id.auth_button);
         mSubmit.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 // This logs you out if you're logged in, or vice versa
                 if (mLoggedIn) {
                     logOut();
                 } else {
                     // Start the remote authentication
                     mDBApi.getSession().startAuthentication(SoundBox.this);
                 }
             }
         });
     }
     
     /* ********************************************************************* *
      *                          Dropbox API calls                            *
      * ********************************************************************* */
 
     public void showMusicFolder(String aPath) {
         Log.i("SoundBox", "showMusicFolder()");
         // TODO: check the input param, if null set to / or /Music
         // TODO: handle the case where path is a file
         // Metadata
 
         Log.i("SoundBox", "Showing Folder: " + aPath);
 
         // if there is a fetcher we're already grabbing something, wait for it...
         if (mFetcher != null) {
             Log.i("SoundBox", "still have mFetcher, aborting");
             return;
         }
         mCurrentPath = aPath;
         // TODO replace this with a DropboxService
         mFetcher = new SoundBoxAsyncFetcher();
         mFetcher.setDropboxAPI(mDBApi);
         mFetcher.registerCompletionHandler(this);
         mFetcher.execute(mCurrentPath);
     }
 
     public void onTaskComplete(String [] aContentArray) {
         Log.i("SoundBox", "onTaskComplete()");
         // drop our handle on the AsyncFetcher
         mFetcher = null;
         Log.i("SoundBox", "Task Complete, have dropped mFetcher");
 
         // TODO: Handle the case where we are first loading the app; and resuming from a stopped state
 
         // Make new fragment to show this selection.
         SoundBoxListFragment newListFrag = SoundBoxListFragment.newInstance(mCurrentPath);
 
         Log.i("SoundBox", "Fragment created: " + newListFrag);
 
         // Start the fragment transaction
         FragmentTransaction ft = getFragmentManager().beginTransaction();
 
         // Bind it to a data adapter
         newListFrag.setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, aContentArray));
 
         // XXXredfive - broken ATM, will fix next
         //ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
 
         // We may be replacing an existing fragement, check to see if it's there
         // Search by tag because dynamic fragments can't be found by id
         SoundBoxListFragment oldListFrag = (SoundBoxListFragment) getFragmentManager().findFragmentByTag(mListTag);
         if (oldListFrag == null) {
             Log.i("SoundBox", "No existing Fragment, adding a new list ************");
             // We didn't have a list already, create a new one and proceed.
             ft.add(R.id.main_frame, newListFrag, mListTag);
         } else {
             Log.i("SoundBox", "Existing Fragment, replacing with a new list: %%%%%%%%%%%%%%%%");
             // We did have a list already, replace it.
             ft.remove(oldListFrag);
             ft.add(R.id.main_frame, newListFrag, mListTag);
             //ft.replace(R.id.main_frame, newListFrag, mListTag);
         }
 
         // Stack it and commit it
         ft.addToBackStack(null);
         ft.commit();
         Log.i("SoundBox", "Fragment Transaction commited.");
     }
 
     /* ********************************************************************* *
      *                         Authentication mgmt                           *
      * ********************************************************************* */
 
     // BIG TODO: move the Dropbox specific pieces into a service that includes
     //           the fetching of folders and handing back Entry arrays, instead
     //           of arrays of strings.
 
     /** Create a session; use the persisted session token if it exists */
     private AndroidAuthSession buildSession() {
         AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
         AndroidAuthSession session;
 
         String [] stored = getKeys();
         if (stored != null) {
             AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
             session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
         } else {
             session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
         }
 
         return session;
     }
 
     /** store the access token and secret linking the session to the dropbox app (?) */
     private void storeKeys(String key, String secret) {
         // Save the access key for later
         SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
         Editor edit = prefs.edit();
         edit.putString(ACCESS_KEY_NAME, key);
         edit.putString(ACCESS_SECRET_NAME, secret);
         edit.commit();
     }
 
     /** retrieve the access token and secret that links the session to the dropbox app (?) */
     private String[] getKeys() {
         SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
         String key = prefs.getString(ACCESS_KEY_NAME, null);
         String secret = prefs.getString(ACCESS_SECRET_NAME, null);
         if (key != null && secret != null) {
                 String[] ret = new String[2];
                 ret[0] = key;
                 ret[1] = secret;
                 return ret;
         } else {
                 return null;
         }
     }
 
     /** disable this session from the dropbox app - forces a re-authentication */
     private void logOut() {
         // Remove credentials from the session
         mDBApi.getSession().unlink();
 
         // Clear our stored keys
         clearKeys();
         // Change UI state to display logged out version
         setLoggedIn(false);
     }
 
     // TODO: change this to erase only the key & secret
     /** remove the stored access token and secret */
     private void clearKeys() {
         SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
         Editor edit = prefs.edit();
         edit.clear();
         edit.commit();
     }
 }
