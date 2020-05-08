 /***************************************************************************
  * Copyright (c) 2011, 2012 by Sonr Labs Inc (http://www.sonrlabs.com)
  *
  *You can redistribute this program and/or modify it under the terms of the GNU General Public License v. 2.0 as published by the Free Software Foundation
  *This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
  *
  **************************************************************************/
 
 package com.sonrlabs.test.sonr;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.InflateException;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.sonrlabs.prod.sonr.R;
 
 public class SonrActivity extends ListActivity {
 
    public static final String DISCONNECT_ACTION = "android.intent.action.DISCONNECT_DOCK";
    public static final String VOICE_COMMAND_ACTION = "android.intent.action.VOICE_COMMAND";
    public static final String SPEECH_RECOGNIZER_ACTION = "android.intent.action.SPEECH_RECOGNIZER";
    public static final String GOOGLE_VOICE_SEARCH_PACKAGE_NAME = "com.google.android.voicesearch";
    public static final int GOOGLE_VOICE_SEARCH_REQUEST_CODE = 1235;
 
    static final String TAG = SonrActivity.class.getSimpleName();
    static final int USER_HAD_SEEN_INTRO_SCREEN = 0; 
 
    private ProgressDialog progressDialog;
    private boolean isRegistered;
    private int currentlySelectedApplicationInfoIndex;
 
    private boolean mBound;
    private Messenger mService;
 
    class IncomingHandler extends Handler {
       @Override
       public void handleMessage(Message msg) {
          progressDialog.dismiss();
          if (hasWindowFocus()) {
             Toast.makeText(getApplicationContext(), getString(R.string.DOCK_NOT_FOUND), Toast.LENGTH_SHORT).show();
             Dialogs.quickPopoutDialog(SonrActivity.this, false, getString(R.string.DOCK_NOT_FOUND_TRY_AGAIN), getString(R.string.OK));
          }
       }
    }
    private final Messenger mMessenger = new Messenger(new IncomingHandler());
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.music_select_main);
 
       registerForContextMenu(this.getListView());
 
       if (progressDialog == null) {
          progressDialog = new ProgressDialog(this);
          progressDialog.setMessage(getString(R.string.connectingToSonrDock));
       }
 
       Preferences.savePreference(this, getString(R.string.PLAYER_SELECTED), false);
 
       currentlySelectedApplicationInfoIndex = -1;
    }
 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       super.onActivityResult(requestCode, resultCode, data);
 
       switch (requestCode) {
          case USER_HAD_SEEN_INTRO_SCREEN:
             if (resultCode == Activity.RESULT_OK) {
                Preferences.savePreference(this, getString(R.string.DEFAULT_PLAYER_SELECTED), false);
                Preferences.savePreference(this, getString(R.string.FIRST_LAUNCH), false);
             }
             break;
          case GOOGLE_VOICE_SEARCH_REQUEST_CODE:
             SonrLog.d(TAG, "got here");
             //reconnectSONR();
          break;
          default:
             break;
       }
    }
 
    /**
     *   Bind to service, fill the list with music apps, wait for 
     */
    private void completeStartUp() {
       progressDialog.show();
 
       SonrLog.d(TAG, "binding to service");
 
       if (!mBound) {
          bindService(new Intent(this, SonrService.class), mConnection, 
                      Context.BIND_AUTO_CREATE);
       }
 
       //TODO: move this to a thread
       setListAdapter(new AppInfoAdapter(this));
 
       //TODO: some inconsistency here, if USER saved his preferred volume pull it out, otherwise don't load any defaults
       AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
       int savedNotificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
 
       Preferences.savePreference(this, getString(R.string.SAVED_NOTIFICATION_VOLUME), savedNotificationVolume);
 
       progressDialog.hide();
    }
 
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
       super.onCreateContextMenu(menu, v, menuInfo);
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.context_menu, menu);
    }
 
    @Override
    public boolean onContextItemSelected(MenuItem item) {
       AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
       
       switch (item.getItemId()) {
          case R.id.makeDefaultPlayer:            
             ApplicationInfo ai = (ApplicationInfo) this.getListView().getAdapter().getItem(info.position);
             List<ResolveInfo> rinfos = AppUtils.findActivitiesForPackage(this, ai.packageName);
 
             String defaultPlayerToastMessage = new String();
             if (!rinfos.isEmpty()) {
                ResolveInfo ri = rinfos.get(0);
              
                Preferences.savePreference(this, getString(R.string.DEFAULT_PLAYER_PACKAGE_NAME), ri.activityInfo.packageName);
                Preferences.savePreference(this, getString(R.string.APP_FULL_NAME), ri.activityInfo.name);
                Preferences.savePreference(this, getString(R.string.DEFAULT_PLAYER_SELECTED), true);
                Preferences.savePreference(this, getString(R.string.PLAYER_SELECTED), true);
 
                defaultPlayerToastMessage = ri.activityInfo.name + " is now the default player";
             }
 
             Toast.makeText(getApplicationContext(), defaultPlayerToastMessage, Toast.LENGTH_LONG).show();
             return true;
          default:
             return super.onContextItemSelected(item);
       }
    }
 
    @Override
    protected void onListItemClick(ListView listView, View clickedView, int position, long id) {
       super.onListItemClick(listView, clickedView, position, id);
 
       ListAdapter adapter = getListAdapter();
 
       if (adapter != null && !adapter.isEmpty()) {
 
          progressDialog.show();
 
          ApplicationInfo ai = (ApplicationInfo) adapter.getItem(position);
          //TODO: another opp for a thread mister
          List<ResolveInfo> rinfos = AppUtils.findActivitiesForPackage(this, ai.packageName);
 
          if (!rinfos.isEmpty()) {
             ResolveInfo ri = rinfos.get(0);
 
             Preferences.savePreference(this, getString(R.string.APP_PACKAGE_NAME), ri.activityInfo.packageName);
             Preferences.savePreference(this, getString(R.string.APP_FULL_NAME), ri.activityInfo.name);
             Preferences.savePreference(this, getString(R.string.PLAYER_SELECTED), true);
 
             currentlySelectedApplicationInfoIndex = position;
             listView.invalidateViews();
 
             if (mService != null && mBound) {
                Message msg = Message.obtain();
                msg.arg1 = SonrService.START_SELECTED_PLAYER;
                msg.replyTo = mMessenger;
 
                try {
                   mService.send(msg);
                } catch (RemoteException e) {
                   SonrLog.e(TAG, e.toString());
                }
             }
          }
       }
    }
 
    @Override
    protected void onStart() {
       super.onStart();
 
       SonrLog.d(TAG, "onStart()");
 
       boolean firstLaunch = Preferences.getPreference(this, getString(R.string.FIRST_LAUNCH), true);
       if (firstLaunch) {
          SonrLog.d(TAG, "First run, user info screen...");   
          startActivityForResult(new Intent(this, IntroScreen.class), USER_HAD_SEEN_INTRO_SCREEN);
       } else {
          if (!isRegistered) {
             registerReceiver(stopReceiver, new IntentFilter(DISCONNECT_ACTION));
             isRegistered = true;
          }
 
          completeStartUp();
       }
 
       //FlurryAgent.onStartSession(this, "NNCR41GZ52ZYBXPZPTGT");
    }
    
 //   private void reconnectSONR(){
 //      Intent connectDock = new Intent(Intent.ACTION_HEADSET_PLUG);
 //      connectDock.putExtra("state", 1);
 //      connectDock.putExtra("name", "fake headset connect");
 //      connectDock.putExtra("microphone", 0);
 //      this.sendBroadcast(connectDock);
 //   }
    
    @Override
    protected void onStop() {
       SonrLog.d(TAG, "onStop()");
 
       if (progressDialog.isShowing()) {
          progressDialog.dismiss();
       }
 
       if (mBound) {
          SonrLog.d(TAG, "unbinding from service");
          unbindService(mConnection);
          mBound = false;
       }
       super.onStop();
       //FlurryAgent.onEndSession(this);
    }
 
    @Override
    protected void onDestroy() {
       if (isRegistered) {
          unregisterReceiver(stopReceiver);
          isRegistered = false;
       }
       super.onDestroy();
    }
 
    @Override
    public void onBackPressed() {
       // Do nothing, default implementation finishes
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       boolean showMenu = super.onCreateOptionsMenu(menu);
       try {
          getMenuInflater().inflate(R.menu.main, menu);
       } catch (InflateException e) {
          Log.d(TAG, "Unable to inflate menu: " + e);
          showMenu = false;
       }
       return showMenu;
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       boolean consumeResult = super.onOptionsItemSelected(item);
 
       if (R.id.quitOption == item.getItemId()) {
          if (mBound) {
             SonrLog.d(TAG, "unbinding from service");
             unbindService(mConnection);
             mBound = false;
          }
          stopService(new Intent(this, SonrService.class));
          finish();
          consumeResult = true;
       }
       else if (R.id.feedbackOption == item.getItemId()) {
          Intent feedbackActivityIntent = new Intent(this, FeedbackActivity.class);
          startActivity(feedbackActivityIntent);
          consumeResult = true;
       }
 
       return consumeResult;
    }
 
    public void onNoneClicked(View noneButton) {
       Intent startMain = new Intent(Intent.ACTION_MAIN);
       startMain.addCategory(Intent.CATEGORY_HOME);
       startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       startActivity(startMain);
    }
 
    private class AppInfoAdapter extends BaseAdapter {
 
       private final List<ApplicationInfo> appInformation;
       private final LayoutInflater mInflater;
       private final PackageManager pm;
 
       public AppInfoAdapter(Context c) {
          Context appContext = c.getApplicationContext();
          mInflater = (LayoutInflater) appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
          pm = c.getPackageManager();
          appInformation = AppUtils.convert(appContext, AppUtils.findActivities(appContext)); //TODO: refactor this
       }
 
       @Override
       public int getCount() {
          return appInformation.size();
       }
 
       @Override
       public Object getItem(int position) {
          return appInformation.get(position);
       }
 
       @Override
       public long getItemId(int position) {
          return appInformation.get(position).hashCode();
       }
 
       @Override
       public View getView(int position, View convertView, ViewGroup parent) {
 
          ApplicationInfo info = appInformation.get(position);
 
          if (convertView == null) {
             convertView = mInflater.inflate(R.layout.manage_applications_item, null);
          }
 
          TextView name = (TextView) convertView.findViewById(R.id.app_name);
          name.setText(info.loadLabel(pm));
 
          ImageView icon = (ImageView) convertView.findViewById(R.id.app_icon);
          icon.setImageDrawable(info.loadIcon(pm));
          TextView description = (TextView) convertView.findViewById(R.id.app_size);
          description.setText(info.loadDescription(pm));
 
          convertView.setBackgroundColor(currentlySelectedApplicationInfoIndex == position ? 0xFF666666 : 0xFF444444);
 
          return convertView;
       }
    }
 
    private final BroadcastReceiver stopReceiver = new BroadcastReceiver() {
       @Override
       public void onReceive(Context context, Intent intent) {
          if (intent != null && DISCONNECT_ACTION.equals(intent.getAction())) {
             finish();
          }
       }
    };
 
    private ServiceConnection mConnection = new ServiceConnection() {
       @Override
       public void onServiceConnected(ComponentName className, IBinder service) {
          mService = new Messenger(service);
          mBound = true;
       }
 
       @Override
       public void onServiceDisconnected(ComponentName className) {
          mService = null;
          mBound = false;
       }
    };
 
 }
