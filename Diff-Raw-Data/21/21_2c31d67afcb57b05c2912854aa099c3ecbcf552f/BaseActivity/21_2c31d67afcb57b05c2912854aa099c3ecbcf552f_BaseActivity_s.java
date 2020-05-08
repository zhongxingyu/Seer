 package com.example.devicehive.android.client.sample;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.MenuItem;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.dataart.android.devicehive.network.DeviceHiveResultReceiver;
 import com.dataart.android.devicehive.network.NetworkCommand;
 import com.dataart.android.devicehive.network.NetworkCommandConfig;
 
 public class BaseActivity extends SherlockFragmentActivity {
 
 	private DeviceHiveResultReceiver resultReceiver = null;
 
 	private final DeviceHiveResultReceiver.ResultListener resultListener = new DeviceHiveResultReceiver.ResultListener() {
 		@Override
 		public void onReceiveResult(int code, int tag, Bundle data) {
 			BaseActivity.this.onReceiveResult(code, tag, data);
 		}
 	};
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		if (resultReceiver != null) {
 			resultReceiver.detachResultListener();
 			resultReceiver = null;
 		}
 	}
 
 	protected final <T extends NetworkCommand> void startCommand(
 			final T command) {
 		command.start(getApplicationContext(), getNetworkCommandConfig());
 	}
 
 	protected DeviceHiveResultReceiver getResultReceiver() {
 		if (null == resultReceiver) {
 			resultReceiver = new DeviceHiveResultReceiver();
 			resultReceiver.setResultListener(resultListener, true);
 		}
 		return resultReceiver;
 	}
 
 	protected NetworkCommandConfig getNetworkCommandConfig() {
 
 		final SampleClientPreferences prefs = new SampleClientPreferences(this);
 		String serverUrl = prefs.getServerUrl();
 		if (serverUrl == null) {
 			serverUrl = DeviceHiveConfig.API_ENDPOINT;
 			prefs.setServerUrlSync(serverUrl);
 		}
 		final NetworkCommandConfig config = new NetworkCommandConfig(serverUrl,
 				getResultReceiver(), BuildConfig.DEBUG);
 
 		config.setBasicAuthorisation(prefs.getUsername(), prefs.getPassword());
 		return config;
 	}
 
 	protected void onReceiveResult(final int resultCode, final int tagId,
 			final Bundle resultData) {
 
 	}
 
 	protected static final int getTagId(final Class<?> tag) {
 		return getTagId(tag.getName());
 	}
 
 	protected static final int getTagId(final String tag) {
 		return DeviceHiveResultReceiver.getIdForTag(tag);
 	}
 
 	private static final int MENU_ID_SETTINGS = 0x01;
 	private static final int MENU_ID_REFRESH = 0x02;
 	
 	private com.actionbarsherlock.view.Menu optionsMenu;
 
 	@Override
 	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
 		optionsMenu = menu;
 		if (showsSettingsActionItem()) {
 			menu.add(com.actionbarsherlock.view.Menu.NONE, MENU_ID_SETTINGS,
 					com.actionbarsherlock.view.Menu.NONE, "Settings")
 					.setIcon(R.drawable.ic_menu_settings)
 					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 		}
 		if (showsRefreshActionItem()) {
 			menu.add(com.actionbarsherlock.view.Menu.NONE, MENU_ID_REFRESH,
 					com.actionbarsherlock.view.Menu.NONE, "Refresh")
 					.setIcon(R.drawable.ic_menu_refresh)
 					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 		}
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(
 			com.actionbarsherlock.view.MenuItem item) {
 		if (item.getItemId() == MENU_ID_SETTINGS) {
 			onShowSettings();
 			return true;
 		} else if (item.getItemId() == MENU_ID_REFRESH) {
 			onRefresh();
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	protected boolean showsSettingsActionItem() {
 		return false;
 	}
 
 	protected boolean showsRefreshActionItem() {
 		return false;
 	}
 
 	protected void onShowSettings() {
 
 	}
 
 	protected void onRefresh() {
 
 	}
 	
 	protected boolean showsActionBarProgress() {
         return false;
     }
 
     private int progressOperationsCount = 0;
 
     protected void incrementActionBarProgressOperationsCount(int count) {
         this.progressOperationsCount += count;
         setActionBarProgressVisibility(count > 0);
     }
     
     protected void decrementActionBarProgressOperationsCount() {
     	progressOperationsCount -= 1;
         if (progressOperationsCount == 0) {
             setActionBarProgressVisibility(false);
         }
         if (progressOperationsCount < 0) {
             progressOperationsCount = 0;
         }
     }
     
     protected int getActionBarProgressOperationsCount() {
         return progressOperationsCount;
     }
     
     protected void setActionBarProgressVisibility(boolean visible) {
        final com.actionbarsherlock.view.MenuItem item = optionsMenu.findItem(MENU_ID_REFRESH);
        if (item != null) {
            if (visible) {
                item.setActionView(R.layout.menu_progress);
            } else {
                item.setActionView(null);
                progressOperationsCount = 0;
            }
        }
     }
     
     protected void showErrorDialog(String title, String message) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		final AlertDialog dialog = builder
 				.setTitle(title)
 				.setMessage(message)
 				.setPositiveButton("OK",
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								dialog.dismiss();
 							}
 						}).create();
 		dialog.show();
 	}
 
 }
