 package de.fhb.mi.paperfly;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.SearchManager;
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.res.Configuration;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.fhb.mi.paperfly.auth.AuthHelper;
 import de.fhb.mi.paperfly.auth.LoginActivity;
 import de.fhb.mi.paperfly.chat.ChatFragment;
 import de.fhb.mi.paperfly.dto.AccountDTO;
 import de.fhb.mi.paperfly.navigation.NavItemModel;
 import de.fhb.mi.paperfly.navigation.NavKey;
 import de.fhb.mi.paperfly.navigation.NavListAdapter;
 import de.fhb.mi.paperfly.navigation.NavListAdapter.ViewHolder;
 import de.fhb.mi.paperfly.service.RestConsumerException;
 import de.fhb.mi.paperfly.service.RestConsumerSingleton;
 import de.fhb.mi.paperfly.user.FriendListFragment;
 import de.fhb.mi.paperfly.user.UserProfileFragment;
 import de.fhb.mi.paperfly.user.UserSearchActivity;
 
 /**
  * The Activity with the navigation and some Fragments.
  *
  * @author Christoph Ott
  * @author Andy Klay   klay@fh-brandenburg.de
  */
 public class MainActivity extends Activity {
     private static final String TAG = MainActivity.class.getSimpleName();
     private static final String TITLE_LEFT_DRAWER = "Navigation";
     private static final String TITLE_RIGHT_DRAWER = "Status";
     private static final int REQUESTCODE_QRSCAN = 100;
     public static final int REQUESTCODE_SEARCH_USER = 101;
     private DrawerLayout drawerLayout;
     private ListView drawerRightList;
     private ListView drawerLeftList;
     private List<String> drawerRightValues;
     private ActionBarDrawerToggle drawerToggle;
     private CharSequence mTitle;
     private UserLoginTask mAuthTask = null;
     private UserLogoutTask mLogoutTask = null;
     private View progressLayout;
     private boolean roomAdded = false;
     private int roomNavID;
     private String actualRoom;
     private GetAccountsInRoomTask mGetAccountsInRoomTask = null;
     private List<AccountDTO> usersInRoom = new ArrayList<AccountDTO>();
     private boolean appStarted = false;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(TAG, "onCreate");
         setContentView(R.layout.activity_main);
         initViewsById();
 
         // DUMMY DATA
         drawerRightValues = new ArrayList<String>();
 //        for (int i = 0; i < 50; i++) {
 //            drawerRightValues.add("User" + i + TITLE_RIGHT_DRAWER);
 //        }
 
         //fill accounts in room, standard is global
         mTitle = getTitle();
 
         drawerToggle = createActionBarDrawerToggle();
         // Set the drawer toggle as the DrawerListener
         drawerLayout.setDrawerListener(drawerToggle);
 
         // enable ActionBar app icon to behave as action to toggle nav drawer
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
 
         // Set the adapter for the list view
        drawerRightList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, drawerRightValues));
 
         // Set the list's click listener
         drawerRightList.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 openUserProfile(drawerRightList.getItemAtPosition(position).toString(), false);
                 drawerLayout.closeDrawers();
             }
         });
 
         // Set the list's click listener
         drawerLeftList.setOnItemClickListener(new DrawerItemClickListener());
 
         generateNavigation();
 //        navigateTo(NavKey.GLOBAL);
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         Log.d(TAG, "onNewIntent");
         if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
             String query = intent.getStringExtra(SearchManager.QUERY);
             // manually launch the real search activity
             final Intent searchIntent = new Intent(getApplicationContext(), UserSearchActivity.class);
             // add query to the Intent Extras
             searchIntent.putExtra(SearchManager.QUERY, query);
             searchIntent.setAction(Intent.ACTION_SEARCH);
             drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
             startActivityForResult(searchIntent, REQUESTCODE_SEARCH_USER);
         }
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         Log.d(TAG, "onStart");
         Bundle bundle = getIntent().getExtras();
         if (bundle != null) {
             boolean loginSuccessful = getIntent().getExtras().getBoolean(LoginActivity.LOGIN_SUCCESFUL);
             if (!loginSuccessful) {
                 showProgress(true);
                 bundle.clear();
                 mAuthTask = new UserLoginTask();
                 mAuthTask.execute();
             } else if (!appStarted) {
                 // if the app was started select GlobalChat
             } else if (!appStarted) {
                 // if the app was started select GlobalChat
                 navigateTo(NavKey.GLOBAL);
 //                // TODO select global
                 //fill accounts in room, standard is global
 //                mGetAccountsInRoomTask = new GetAccountsInRoomTask();
 //                mGetAccountsInRoomTask.execute();
                 appStarted = true;
                 // TODO select global
                 appStarted = true;
                 // TODO select global
             }
         } else {
             showProgress(true);
             mAuthTask = new UserLoginTask();
             mAuthTask.execute();
         }
     }
 
     /**
      * Shows the progress UI and hides the login form.
      *
      * @param show true if the progress UI should be shown, false if not
      */
     @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
     private void showProgress(final boolean show) {
         // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
         // for very easy animations. If available, use these APIs to fade-in
         // the progress spinner.
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
             int shortAnimTime = getResources().getInteger(android.R.integer.config_longAnimTime);
 
             progressLayout.setVisibility(View.VISIBLE);
             progressLayout.animate()
                     .setDuration(shortAnimTime)
                     .alpha(show ? 1 : 0)
                     .setListener(new AnimatorListenerAdapter() {
                         @Override
                         public void onAnimationEnd(Animator animation) {
                             progressLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                         }
                     });
 
             drawerLayout.setVisibility(View.VISIBLE);
             drawerLayout.animate()
                     .setDuration(shortAnimTime)
                     .alpha(show ? 0 : 1)
                     .setListener(new AnimatorListenerAdapter() {
                         @Override
                         public void onAnimationEnd(Animator animation) {
                             drawerLayout.setVisibility(show ? View.GONE : View.VISIBLE);
                         }
                     });
         } else {
             // The ViewPropertyAnimator APIs are not available, so simply show
             // and hide the relevant UI components.
             progressLayout.setVisibility(show ? View.VISIBLE : View.GONE);
             drawerLayout.setVisibility(show ? View.GONE : View.VISIBLE);
         }
     }
 
     /**
      * Generates the NavigationList on the left side.
      */
     private void generateNavigation() {
         Log.d(TAG, "generateNavigation");
         NavListAdapter mAdapter = new NavListAdapter(this);
         mAdapter.addHeader(this.getResources().getString(R.string.nav_header_general));
         mAdapter.addItem(NavKey.MY_ACCOUNT, this.getResources().getString(R.string.nav_item_my_account), R.drawable.ic_action_person);
         mAdapter.addItem(NavKey.CHECK_PRESENCE, this.getResources().getString(R.string.nav_item_check_presence), -1);
         mAdapter.addItem(NavKey.FRIENDLIST, this.getResources().getString(R.string.nav_item_open_friendlist), android.R.drawable.ic_menu_share);
 
         mAdapter.addHeader(this.getResources().getString(R.string.nav_header_chats));
         mAdapter.addItem(NavKey.GLOBAL, this.getResources().getString(R.string.nav_item_global), -1);
         mAdapter.addItem(NavKey.ENTER_ROOM, this.getResources().getString(R.string.nav_item_enter_room), android.R.drawable.ic_menu_camera);
 
         drawerLeftList.setAdapter(mAdapter);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         Log.d(TAG, "onCreateOptionsMenu");
         // Inflate the menu; this adds items to the action bar if it is present.
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
 
         return super.onCreateOptionsMenu(menu);
     }
 
     /**
      * Initializes all views in the layout.
      */
     private void initViewsById() {
         Log.d(TAG, "initViewsById");
         drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         progressLayout = findViewById(R.id.login_status);
         drawerRightList = (ListView) findViewById(R.id.right_drawer);
         drawerLeftList = (ListView) findViewById(R.id.left_drawer);
     }
 
     /**
      * Creates a {@link android.support.v4.app.ActionBarDrawerToggle} which can show the navigation.
      *
      * @return the ActionBarDrawerToggle
      */
     private ActionBarDrawerToggle createActionBarDrawerToggle() {
         Log.d(TAG, "createActionBarDrawerToggle");
 
         drawerRightValues.clear();
         List<AccountDTO> usersInRoom = ((PaperFlyApp) getApplication()).getUsersInRoom();
         for (AccountDTO current : usersInRoom) {
             drawerRightValues.add(current.getUsername());
         }
 
         return new ActionBarDrawerToggle(this, drawerLayout,
                 R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
 
             @Override
             public boolean onOptionsItemSelected(MenuItem item) {
                 if (item != null && item.getItemId() == android.R.id.home && drawerToggle.isDrawerIndicatorEnabled()) {
                     openDrawerAndCloseOther(Gravity.LEFT);
                     return true;
                 } else {
                     return false;
                 }
             }
 
             /** Called when a drawer has settled in a completely closed state. */
             public void onDrawerClosed(View view) {
                 setTitle(mTitle);
                 invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
 
             /** Called when a drawer has settled in a completely open state. */
             public void onDrawerOpened(View drawerView) {
                 if (drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                     getActionBar().setTitle(TITLE_RIGHT_DRAWER);
 //                    this.changeDrawerRight();
                 }
                 if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                     getActionBar().setTitle(TITLE_LEFT_DRAWER);
                 }
                 invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
 
 //            /**
 //             * change values in drawer right, set actual users in room
 //             */
 //            private void changeDrawerRight() {
 //
 //                drawerRightValues.clear();
 //                List<AccountDTO> usersInRoom = ((PaperFlyApp) getApplication()).getUsersInRoom();
 //                for (AccountDTO current : usersInRoom) {
 //                    drawerRightValues.add(current.getUsername());
 //                }
 //            }
         };
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         drawerToggle.syncState();
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         Log.d(TAG, "onRestoreInstanceState");
         super.onRestoreInstanceState(savedInstanceState);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         Log.d(TAG, "onSaveInstanceState");
         super.onSaveInstanceState(outState);
     }
 
     @Override
     public void onAttachFragment(Fragment fragment) {
         Log.d(TAG, "onAttachFragment");
         super.onAttachFragment(fragment);
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         Log.d(TAG, "onRestoreInstanceState");
         super.onConfigurationChanged(newConfig);
         drawerToggle.onConfigurationChanged(newConfig);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Log.d(TAG, "onOptionsItemSelected");
         switch (item.getItemId()) {
             case android.R.id.home:
                 drawerToggle.onOptionsItemSelected(item);
                 return true;
             case R.id.action_websockettest:
                 Intent intent = new Intent(this, WebSocketTestMainActivity.class);
                 startActivity(intent);
                 return true;
             case R.id.action_settings:
                 startActivity(new Intent(this, SettingsActivity.class));
                 return true;
             case R.id.action_help:
                 startActivity(new Intent(this, HelpActivity.class));
                 return true;
             case R.id.action_logout:
                 deleteFile(AuthHelper.FILE_NAME);
                 mLogoutTask = new UserLogoutTask();
                 mLogoutTask.execute();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     /**
      * Opens the specified drawer and closes the other one, if it is visible
      *
      * @param drawerGravity the drawer to be opened
      */
     private void openDrawerAndCloseOther(int drawerGravity) {
         Log.d(TAG, "openDrawerAndCloseOther");
         switch (drawerGravity) {
             case Gravity.LEFT:
                 if (drawerLayout.isDrawerVisible(Gravity.LEFT)) {
                     drawerLayout.closeDrawer(Gravity.LEFT);
                 } else if (drawerLayout.isDrawerVisible(Gravity.RIGHT)) {
                     drawerLayout.closeDrawer(Gravity.RIGHT);
                     drawerLayout.openDrawer(Gravity.LEFT);
                 } else {
                     drawerLayout.openDrawer(Gravity.LEFT);
                 }
                 break;
             case Gravity.RIGHT:
                 if (drawerLayout.isDrawerVisible(Gravity.RIGHT)) {
                     drawerLayout.closeDrawer(Gravity.RIGHT);
                 } else if (drawerLayout.isDrawerVisible(Gravity.LEFT)) {
                     drawerLayout.closeDrawer(Gravity.LEFT);
                     drawerLayout.openDrawer(Gravity.RIGHT);
                 } else {
                     drawerLayout.openDrawer(Gravity.RIGHT);
                 }
                 break;
         }
     }
 
     /**
      * Opens a new Intent for QR scan.
      *
      * @return true if the scan was successful, false if not
      */
     private boolean doQRScan() {
         Log.d(TAG, "doQRScan");
         PackageManager pm = this.getPackageManager();
         if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
             Intent intent = new Intent("com.google.zxing.client.android.SCAN");
             intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
             try {
                 startActivityForResult(intent, REQUESTCODE_QRSCAN);
             } catch (ActivityNotFoundException e) {
                 Toast.makeText(this, "You have no QR Scanner", Toast.LENGTH_LONG).show();
                 Log.e(TAG, e.getMessage(), e);
             }
             return true;
         } else {
             Toast.makeText(this, "There is no camera for this device.", Toast.LENGTH_SHORT).show();
             return false;
         }
     }
 
     /**
      * Creates a new Fragment for FriendList.
      *
      * @return true if the fragment is shown
      */
     private boolean openFriendList() {
         Log.d(TAG, "openFriendList");
 
         FragmentManager fragmentManager = getFragmentManager();
         Fragment fragmentByTag = fragmentManager.findFragmentByTag(FriendListFragment.TAG);
         if (fragmentByTag == null) {
             Fragment fragment = new FriendListFragment();
             fragmentManager.beginTransaction()
                     .replace(R.id.content_frame, fragment, FriendListFragment.TAG)
                     .commit();
         } else {
             fragmentManager.beginTransaction()
                     .attach(fragmentByTag)
                     .commit();
         }
 
         return true;
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
         switch (requestCode) {
             case REQUESTCODE_SEARCH_USER:
                 Log.d(TAG, "onActivityResult: REQUESTCODE_SEARCH_USER");
                 if (resultCode == RESULT_OK) {
                     String user = intent.getStringExtra(UserProfileFragment.ARGS_USER);
                     openUserProfile(user, false);
                 }
                 break;
             case REQUESTCODE_QRSCAN:
                 Log.d(TAG, "onActivityResult: REQUESTCODE_QRSCAN");
                 if (resultCode == RESULT_OK) {
                     String room = intent.getStringExtra("SCAN_RESULT");
                     //TODO gucken ob Raum existiert via. Restconsumer
 
                     ((PaperFlyApp) getApplication()).setCurrentChatRoomID(room);
 
                     String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                     switchToNewChatRoom(room);
                     Toast.makeText(this, room, Toast.LENGTH_SHORT).show();
                 } else if (resultCode == RESULT_CANCELED) {
                     Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
                 }
                 break;
         }
     }
 
     /**
      * Opens a new fragment for the given chat.
      *
      * @param room the room to open
      */
     private void switchToNewChatRoom(String room) {
         FragmentManager fragmentManager = getFragmentManager();
         Fragment fragmentByTag = fragmentManager.findFragmentByTag(ChatFragment.TAG_ROOM);
 
         Fragment newFragment = new ChatFragment();
         Bundle args = new Bundle();
         args.putString(ChatFragment.ARG_CHAT_ROOM, room);
         newFragment.setArguments(args);
 
 //        if (fragmentByTag == null) {
         // Insert the fragment by replacing any existing fragment
         fragmentManager.beginTransaction()
                 .replace(R.id.content_frame, newFragment, ChatFragment.TAG_ROOM)
                 .commit();
         NavListAdapter adapter = (NavListAdapter) drawerLeftList.getAdapter();
         if (!roomAdded) {
             roomNavID = drawerLeftList.getCheckedItemPosition();
 
             // change navigation drawer
             NavItemModel enterRoomNav = adapter.getItem(roomNavID);
             enterRoomNav.setKey(NavKey.ROOM);
             enterRoomNav.setTitle(room);
             enterRoomNav.setIconID(-1);
 
             adapter.addItem(NavKey.ENTER_ROOM, this.getResources().getString(R.string.nav_item_enter_room), android.R.drawable.ic_menu_camera);
             drawerLeftList.setAdapter(adapter);
             roomAdded = true;
         } else {
             NavItemModel enterRoomNav = adapter.getItem(roomNavID);
             enterRoomNav.setKey(NavKey.ROOM);
             enterRoomNav.setTitle(room);
             enterRoomNav.setIconID(-1);
             adapter.notifyDataSetChanged();
         }
         actualRoom = room;
 //        } else {
 //            // there already was a room selected, that's why there is no need to add a new navItem
 //            // but remove the old fragment
 //            fragmentManager.beginTransaction()
 //                    .remove(fragmentByTag)
 //                    .replace(R.id.content_frame, newFragment, ChatFragment.TAG_ROOM)
 //                    .commit();
 //        }
 
         this.updateUsersInRoomOnDrawer(actualRoom);
     }
 
     /**
      * Switch to the chat room which was earlier selected
      */
     private void switchToChatRoom() {
         FragmentManager fragmentManager = getFragmentManager();
         Fragment fragment = fragmentManager.findFragmentByTag(ChatFragment.TAG);
 
         // Attach fragment that was previously attached
         if (fragment != null) {
             fragmentManager.beginTransaction()
                     .attach(fragment)
                     .commit();
         } else {
             Fragment newFragment = new ChatFragment();
             Bundle args = new Bundle();
             args.putString(ChatFragment.ARG_CHAT_ROOM, actualRoom);
             newFragment.setArguments(args);
 
 //        if (fragmentByTag == null) {
             // Insert the fragment by replacing any existing fragment
             fragmentManager.beginTransaction()
                     .replace(R.id.content_frame, newFragment, ChatFragment.TAG_ROOM)
                     .commit();
         }
         this.updateUsersInRoomOnDrawer(ChatFragment.ARG_CHAT_ROOM);
     }
 
     /**
      * Opens the global chat in a new fragment.
      */
     private void switchToGlobalChat() {
         FragmentManager fragmentManager = getFragmentManager();
         Fragment fragmentByTag = fragmentManager.findFragmentByTag(ChatFragment.TAG_GLOBAL);
 
         if (fragmentByTag == null) {
             Fragment fragment = new ChatFragment();
             Bundle args = new Bundle();
             args.putString(ChatFragment.ARG_CHAT_ROOM, ChatFragment.ROOM_GLOBAL);
             fragment.setArguments(args);
 
             // Insert the fragment by replacing any existing fragment
             fragmentManager.beginTransaction()
                     .replace(R.id.content_frame, fragment, ChatFragment.TAG_GLOBAL)
                     .commit();
 
         } else {
             // attach fragment that was previously attached
             fragmentManager.beginTransaction()
                     .attach(fragmentByTag)
                     .commit();
         }
 
         this.updateUsersInRoomOnDrawer(ChatFragment.ROOM_GLOBAL);
     }
 
    private void updateUsersInRoomOnDrawer(String roomID) {
         ((PaperFlyApp) getApplication()).setCurrentChatRoomID(roomID);
         mGetAccountsInRoomTask = new GetAccountsInRoomTask();
         mGetAccountsInRoomTask.execute();
 
         drawerRightValues.clear();
         List<AccountDTO> usersInRoom = ((PaperFlyApp) getApplication()).getUsersInRoom();
         for (AccountDTO current : usersInRoom) {
             drawerRightValues.add(current.getUsername());
         }
     }
 
     /**
      * Swaps fragments in the main content view.
      *
      * @param navkey the navigation key
      */
     private void navigateTo(NavKey navkey) {
         Log.d(TAG, "navigateTo: " + navkey);
         switch (navkey) {
             case ROOM:
                 switchToChatRoom();
                 break;
             case ENTER_ROOM:
                 doQRScan();
                 break;
             case GLOBAL:
                 switchToGlobalChat();
                 break;
             case MY_ACCOUNT:
                 openUserProfile(((PaperFlyApp) getApplication()).getAccount().getUsername(), true);
                 break;
             case CHECK_PRESENCE:
                 new InfoDialog().show(getFragmentManager(), TAG);
                 checkPresence();
                 break;
             case FRIENDLIST:
                 openFriendList();
                 break;
         }
         drawerLayout.closeDrawer(Gravity.LEFT);
     }
 
     private void checkPresence() {
 
         mGetAccountsInRoomTask = new GetAccountsInRoomTask();
         mGetAccountsInRoomTask.execute();
 
         //Daten umwandeln in String
         StringBuilder output = new StringBuilder();
         ArrayList<AccountDTO> usersInRoom = new ArrayList<AccountDTO>();
         usersInRoom.add(((PaperFlyApp) getApplication()).getAccount());
         for (AccountDTO current : usersInRoom) {
             output.append(current.getFirstName() + " " + current.getLastName() + "\n");
         }
 
         //Daten weiterleiten an andere App
         Intent intent = new Intent(Intent.ACTION_SEND);
         intent.setType("plain/text");
         intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"some@email.address"});
         intent.putExtra(Intent.EXTRA_SUBJECT, "Attendance in room " + ((PaperFlyApp) getApplication()).getCurrentChatRoomID());
         intent.putExtra(Intent.EXTRA_TEXT, output.toString());
 
         startActivity(Intent.createChooser(intent, "send Mail"));
     }
 
     private void openUserProfile(String user, boolean isMyAccount) {
         Log.d(TAG, "openUserProfile");
         Fragment fragment = new UserProfileFragment();
         Bundle args = new Bundle();
         args.putString(UserProfileFragment.ARGS_USER, user);
         args.putBoolean(UserProfileFragment.ARGS_MY_ACCOUNT, isMyAccount);
         fragment.setArguments(args);
 
         // Insert the fragment by replacing any existing fragment
         FragmentManager fragmentManager = getFragmentManager();
         fragmentManager.beginTransaction()
                 .replace(R.id.content_frame, fragment)
                 .commit();
     }
 
     @Override
     public void setTitle(CharSequence title) {
         mTitle = title;
         getActionBar().setTitle(mTitle);
     }
 
     /**
      * The OnItemClickListener for the navigation.
      */
     private class DrawerItemClickListener implements ListView.OnItemClickListener {
 
         @Override
         public void onItemClick(AdapterView parent, View view, int position, long id) {
             Log.d(TAG, "onItemClick Navigation");
             ViewHolder vh = (ViewHolder) view.getTag();
             drawerLeftList.setSelection(position);
             drawerLayout.closeDrawer(Gravity.LEFT);
             navigateTo(vh.key);
         }
     }
 
     /**
      * The LoginTask which checks if a token is available.
      */
     private class UserLoginTask extends AsyncTask<String, Void, Boolean> {
         @Override
         protected Boolean doInBackground(String... params) {
             return RestConsumerSingleton.getInstance().getConsumer() != null;
         }
 
         @Override
         protected void onPostExecute(final Boolean success) {
             mAuthTask = null;
             showProgress(false);
 
             if (success) {
                 Log.d(TAG, "navigateTo Global");
                 navigateTo(NavKey.GLOBAL);
                 // TODO select global
             } else {
                 Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                 startActivity(intent);
                 finish();
             }
         }
     }
 
     /**
      * The task to logout and delete the saved token.
      */
     private class UserLogoutTask extends AsyncTask<Void, Void, Boolean> {
 
         @Override
         protected Boolean doInBackground(Void... params) {
             try {
                 RestConsumerSingleton.getInstance().logout();
                 return true;
             } catch (RestConsumerException e) {
                 e.printStackTrace();
             }
             return false;
         }
 
         @Override
         protected void onPostExecute(final Boolean success) {
             mAuthTask = null;
             if (success) {
                 finish();
             }
         }
     }
 
     /**
      * Represents an asynchronous GetAccountsInRoomTask used to get the accounts in a room
      */
     public class GetAccountsInRoomTask extends AsyncTask<String, Void, Boolean> {
 
         @Override
         protected Boolean doInBackground(String... params) {
             List<AccountDTO> usersInRoom = null;
 
             try {
                 String roomID = ((PaperFlyApp) getApplication()).getCurrentChatRoomID();
                 usersInRoom = RestConsumerSingleton.getInstance().getUsersInRoom(roomID);
 
                 ((PaperFlyApp) getApplication()).setUsersInRoom(usersInRoom);
 
             } catch (RestConsumerException e) {
                 e.printStackTrace();
             }
 
             return usersInRoom != null;
         }
 
         @Override
         protected void onPostExecute(final Boolean success) {
             mGetAccountsInRoomTask = null;
 
             if (success) {
                 Log.d("onPostExecute", "success");
                 Toast.makeText(getApplicationContext(), "UserInRoom successful!", Toast.LENGTH_SHORT).show();
             }
 
         }
     }
 
 }
