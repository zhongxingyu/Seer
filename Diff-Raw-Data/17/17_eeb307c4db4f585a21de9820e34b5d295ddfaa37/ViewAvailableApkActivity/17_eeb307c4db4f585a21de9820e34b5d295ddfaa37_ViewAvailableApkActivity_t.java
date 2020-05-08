 package moses.client;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.EmptyStackException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 
import org.json.JSONArray;
import org.json.JSONException;

 import moses.client.abstraction.ApkListRequestObserver;
 import moses.client.abstraction.ApkMethods;
 import moses.client.abstraction.HardwareAbstraction;
 import moses.client.abstraction.apks.APKInstalled;
 import moses.client.abstraction.apks.ApkDownloadManager;
 import moses.client.abstraction.apks.ApkInstallManager;
 import moses.client.abstraction.apks.ExternalApplication;
 import moses.client.service.MosesService;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.PendingIntent;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Viewing and installing apks from the server
  * 
  * @author Simon L
  */
 public class ViewAvailableApkActivity extends ListActivity implements ApkListRequestObserver {
 
 	private static enum LayoutState {
 		NORMAL_LIST, SENSORS_HINT, EMPTYLIST_HINT, PENDINGREQUEST, NOCONNECTIVITY;
 	}
 	
 	private static final String PREFKEY_SHOW_SET_SENSORS_HINT = "showInitialSetSensorsHint";
 	private static final int REFRESH_THRESHHOLD = 800;
 	private ListView listView;
 	private List<ExternalApplication> externalApps;
 	private Long lastListRefreshTime = null;
 	LayoutState lastSetLayout = null; 
 
 	public void setLastSetLayout(LayoutState lastSetLayout) {
 		this.lastSetLayout = lastSetLayout;
 		Log.d("MoSeS.UI", "Layouted showAvailableApkList to state " + lastSetLayout);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.availableapklist);
 
 		initControls();
 	}
 
 	public void apkInstallClickHandler(View v) {
 		if(MosesService.isOnline(getApplicationContext())) {
 			int pos = listView.getPositionForView(v);
 			final ExternalApplication app = externalApps.get(pos);
 			showAppInfo(app);
 		} else {
 			showNoConnectionInfoBox();
 		}
 	}
 
 	private void showNoConnectionInfoBox() {
 		 AlertDialog alertDialog = new AlertDialog.Builder(this)
 	      .setMessage("Cannot display the app information because no internet connection seems to be present")
 	      .setTitle("No connection")
 	      .setCancelable(true)
 //	      .setNeutralButton("OK",
 //	         new DialogInterface.OnClickListener() {
 //	         public void onClick(DialogInterface dialog, int whichButton){}
 //	         })
 	      .show();
 	}
 
 	private void showAppInfo(final ExternalApplication app) {
 		final Dialog myDialog = new Dialog(this);
 		myDialog.setContentView(R.layout.view_app_info_layout);
 		myDialog.setTitle("App informations:");
 		((TextView) myDialog.findViewById(R.id.appinfodialog_name)).setText("Name: "
 			+ app.getName());
 		((TextView) myDialog.findViewById(R.id.appinfodialog_descr)).setText(""
 			+ app.getDescription());
 		((Button) myDialog.findViewById(R.id.appinfodialog_installbtn)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Log.i("MoSes.Install", "starting install process for app " + app.toString());
 				myDialog.dismiss();
 				handleInstallApp(app);
 			}
 		});
 		((Button) myDialog.findViewById(R.id.appinfodialog_cancelbtn)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				myDialog.dismiss();
 			}
 		});
 
 		myDialog.setOwnerActivity(this);
 		myDialog.show();
 	}
 
 	/**
 	 * Inits the controls.
 	 */
 	private void initControls() {
 		initControlsOnRequestApks();
 		requestExternalApplications();
 	}
 
 	private boolean appsLocallyInCacheStillAvailable() {
 		return externalApps != null && externalApps.size() > 0;
 	}
 	
 	private void initControlsOnRequestApks() {
 		if(showInitialSensorHint()) {
 			initControlsShowSensorsHint();
 		} else {
 			if(appsLocallyInCacheStillAvailable()) {
 				initControlsNormalList(externalApps);
 			} else {
 				if(MosesService.isOnline(getApplicationContext())) {
 					initControlsPendingListRequest();
 				} else {
 					initControlsNoConnectivity();
 				}
 			}
 		}
 	}
 
 	private void initControlsNoConnectivity() {
 		if(lastSetLayout != LayoutState.NOCONNECTIVITY) {
 			LinearLayout emptylistCtrls = (LinearLayout) findViewById(R.id.apklist_emptylistLayout);
 			emptylistCtrls.setVisibility(LinearLayout.VISIBLE);
 			LinearLayout apkListCtrls = (LinearLayout) findViewById(R.id.apklist_mainListLayout);
 			apkListCtrls.setVisibility(LinearLayout.GONE);
 			
 			TextView mainHint = (TextView) findViewById(R.id.apklist_emptylistHintMain);
 			mainHint.setText(R.string.apklist_hint_noconnectivity);
 			
 			final Button actionBtn1 = (Button) findViewById(R.id.apklist_emptylistActionBtn1);
 			final Button actionBtn2 = (Button) findViewById(R.id.apklist_emptylistActionBtn2);
 			actionBtn1.setText("Refresh");
 			actionBtn2.setVisibility(Button.GONE);
 			
 			refreshResfreshBtnTimeout(actionBtn1, "Retry", LayoutState.NOCONNECTIVITY);
 			
 			actionBtn1.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					refreshResfreshBtnTimeout(actionBtn1, "Retry", LayoutState.NOCONNECTIVITY);
 					requestExternalApplications();
 				}
 			});
 			setLastSetLayout(LayoutState.NOCONNECTIVITY);
 		}
 	}
 
 	private void initControlsPendingListRequest() {
 		if(lastSetLayout != LayoutState.PENDINGREQUEST) {
 			LinearLayout emptylistCtrls = (LinearLayout) findViewById(R.id.apklist_emptylistLayout);
 			emptylistCtrls.setVisibility(LinearLayout.VISIBLE);
 			LinearLayout apkListCtrls = (LinearLayout) findViewById(R.id.apklist_mainListLayout);
 			apkListCtrls.setVisibility(LinearLayout.GONE);
 			
 			TextView mainHint = (TextView) findViewById(R.id.apklist_emptylistHintMain);
 			mainHint.setText(R.string.apklist_hint_pendingrequest);
 			
 			final Button actionBtn1 = (Button) findViewById(R.id.apklist_emptylistActionBtn1);
 			final Button actionBtn2 = (Button) findViewById(R.id.apklist_emptylistActionBtn2);
 			actionBtn1.setText("Refresh");
 			actionBtn2.setVisibility(Button.GONE);
 			
 			refreshResfreshBtnTimeout(actionBtn1, "Refresh", LayoutState.PENDINGREQUEST);
 			
 			actionBtn1.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					refreshResfreshBtnTimeout(actionBtn1, "Refresh", LayoutState.PENDINGREQUEST);
 					requestExternalApplications();
 				}
 			});
 			setLastSetLayout(LayoutState.PENDINGREQUEST);
 		}
 	}
 	
 	private void refreshResfreshBtnTimeout(final Button refreshButton, final String minimalString, final LayoutState parentLayout) {
 		refreshButton.setEnabled(false);
 		refreshButton.setText(minimalString);
 		Handler enableRefreshHandler = new Handler();
 		enableRefreshHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				if(!isPaused && lastSetLayout == parentLayout) {
 					refreshButton.setText(minimalString +".");
 				}
 			}
 		}, 800);
 		enableRefreshHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				if(!isPaused && lastSetLayout == parentLayout) {
 					refreshButton.setText(minimalString +"..");
 				}
 			}
 		}, 1600);
 		enableRefreshHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				if(!isPaused && lastSetLayout == parentLayout) {
 					refreshButton.setText(minimalString +"...");
 				}
 			}
 		}, 2400);
 		enableRefreshHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				if(!isPaused && lastSetLayout == parentLayout) {
 					refreshButton.setEnabled(true);
 				}
 			}
 		}, 3000);
 	}
 
 	private void initLayoutFromArrivedList(
 			List<ExternalApplication> applications) {
 		if(showInitialSensorHint()) {
 			// even if there is an arrived list, do not show it, but show the sensors hint
 			initControlsShowSensorsHint();
 		} else {
 			if(applications.size() > 0) {
 				initControlsNormalList(applications);
 			} else {
 				initControlsEmptyArrivedList(false);
 			}
 		}
 		populateList(applications);
 	}
 
 	private void initControlsEmptyArrivedList(boolean mayShowSensorsList) {
 		if(mayShowSensorsList) {
 			initControlsShowSensorsHint();
 		} else {
 			if(lastSetLayout != LayoutState.EMPTYLIST_HINT) {
 				LinearLayout emptylistCtrls = (LinearLayout) findViewById(R.id.apklist_emptylistLayout);
 				emptylistCtrls.setVisibility(LinearLayout.VISIBLE);
 				LinearLayout apkListCtrls = (LinearLayout) findViewById(R.id.apklist_mainListLayout);
 				apkListCtrls.setVisibility(LinearLayout.GONE);
 				
 				TextView mainHint = (TextView) findViewById(R.id.apklist_emptylistHintMain);
 				mainHint.setText(R.string.availableApkList_emptyHint);
 				
 				Button actionBtn1 = (Button) findViewById(R.id.apklist_emptylistActionBtn1);
 				Button actionBtn2 = (Button) findViewById(R.id.apklist_emptylistActionBtn2);
 				actionBtn1.setVisibility(Button.GONE);
 				actionBtn2.setVisibility(Button.GONE);
 				setLastSetLayout(LayoutState.EMPTYLIST_HINT);
 			}
 		}
 	}
 
 	private void initControlsNormalList(List<ExternalApplication> applications) {
 		LinearLayout emptylistCtrls = (LinearLayout) findViewById(R.id.apklist_emptylistLayout);
 		emptylistCtrls.setVisibility(LinearLayout.GONE);
 		LinearLayout apkListCtrls = (LinearLayout) findViewById(R.id.apklist_mainListLayout);
 		apkListCtrls.setVisibility(LinearLayout.VISIBLE);
 		
 		setLastSetLayout(LayoutState.NORMAL_LIST);
 		populateList(applications);
 	}
 
 	
 	private void initControlsShowSensorsHint() {
 		if(lastSetLayout != LayoutState.SENSORS_HINT) {
 			LinearLayout emptylistCtrls = (LinearLayout) findViewById(R.id.apklist_emptylistLayout);
 			emptylistCtrls.setVisibility(LinearLayout.VISIBLE);
 			LinearLayout apkListCtrls = (LinearLayout) findViewById(R.id.apklist_mainListLayout);
 			apkListCtrls.setVisibility(LinearLayout.GONE);
 			
 			TextView mainHint = (TextView) findViewById(R.id.apklist_emptylistHintMain);
 			mainHint.setText(R.string.apklist_hint_sensors_main);
 			
 			Button actionBtn1 = (Button) findViewById(R.id.apklist_emptylistActionBtn1);
 			Button actionBtn2 = (Button) findViewById(R.id.apklist_emptylistActionBtn2);
 			actionBtn1.setText("Yes");
 			actionBtn2.setText("No, Thanks");
 			actionBtn2.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					PreferenceManager.getDefaultSharedPreferences(ViewAvailableApkActivity.this)
 					.edit().putBoolean(PREFKEY_SHOW_SET_SENSORS_HINT, false).commit();
 					initControls();
 				}
 			});
 			actionBtn1.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					PreferenceManager.getDefaultSharedPreferences(ViewAvailableApkActivity.this)
 					.edit().putBoolean(PREFKEY_SHOW_SET_SENSORS_HINT, false).commit();
 					invokeSensorDialog();
 				}
 			});
 			setLastSetLayout(LayoutState.SENSORS_HINT);
 		}
 	}
 
 	protected void invokeSensorDialog() {
 		// TODO !implement
 	}
 
 	private boolean showInitialSensorHint() {
		boolean enoughEnabledSensors = false;
		try {
			JSONArray sensors = new JSONArray(PreferenceManager.getDefaultSharedPreferences(this).getString("sensor_data", "[]"));
			enoughEnabledSensors = ! (sensors!=null && sensors.length()<1);
		} catch (JSONException e) {
			enoughEnabledSensors = false;
		}
 		boolean doShow = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREFKEY_SHOW_SET_SENSORS_HINT, true);
		if(enoughEnabledSensors) {
			doShow = false;
			PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(PREFKEY_SHOW_SET_SENSORS_HINT, true).commit();
		}
		
 		return doShow;
 	}
 
 	private void handleInstallApp(ExternalApplication app) {
 		final ApkDownloadManager downloader = new ApkDownloadManager(app, getApplicationContext());
 		Observer observer = new Observer() {
 			@Override
 			public void update(Observable observable, Object data) {
 				if (downloader.getState() == ApkDownloadManager.State.ERROR) {
 				} else if (downloader.getState() == ApkDownloadManager.State.FINISHED) {
 					installDownloadedApk(downloader.getDownloadedApk(), downloader.getExternalApplicationResult());
 				}
 			}
 		};
 		downloader.addObserver(observer);
 		downloader.start();
 	}
 
 	private void installDownloadedApk(final File originalApk, final ExternalApplication externalAppRef) {
 		final ApkInstallManager installer = new ApkInstallManager(originalApk, externalAppRef);
 		installer.addObserver(new Observer() {
 			@Override
 			public void update(Observable observable, Object data) {
 				if (installer.getState() == ApkInstallManager.State.ERROR) {
 				} else if (installer.getState() == ApkInstallManager.State.INSTALLATION_CANCELLED) {
 					// TODO:how to handle if the user cancels the installation?
 				} else if (installer.getState() == ApkInstallManager.State.INSTALLATION_COMPLETED) {
 					new APKInstalled(externalAppRef.getID());
 					try {
 						ApkInstallManager.registerInstalledApk(originalApk, externalAppRef,
 							ViewAvailableApkActivity.this.getApplicationContext(), false);
 					} catch (IOException e) {
 						Log.e(
 							"MoSeS.Install",
 							"Problems with extracting package name from apk, or problems with the InstalledExternalApplicationsManager after installing an app");
 					}
 				}
 			}
 		});
 		installer.start();
 	}
 
 	int requestListRetries = 0;
 	private boolean isPaused;
 	private void requestExternalApplications() {
 		if(MosesService.getInstance() == null) {
 			if(requestListRetries < 5) {
 				Handler delayedRetryHandler = new Handler();
 				delayedRetryHandler.postDelayed(new Runnable() {
 					@Override
 					public void run() {
 						requestExternalApplications();
 					}
 				}, 1000);
 				requestListRetries++;
 			} else {
 				//TODO:show error when all retries didn't work?
 			}
 		} else {
 			requestListRetries = 0;
 			lastListRefreshTime = System.currentTimeMillis();
 			ApkMethods.getExternalApplications(ViewAvailableApkActivity.this);
 			initControlsOnRequestApks();
 		}
 	}
 
 	@Override
 	public void apkListRequestFinished(List<ExternalApplication> applications) {
 		externalApps = applications;
 		initLayoutFromArrivedList(applications);
 	}
 
 	@Override
 	public void apkListRequestFailed(Exception e) {
 		//TODO: receive failures that point out no connection, too.
 		//TODO: show user some hint about this
 		Log.w("MoSeS.APKMETHODS", "invalid response for apk list request: " + e.getMessage());
 	}
 
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		if(hasFocus && (lastListRefreshTime == null)?true:(System.currentTimeMillis()-lastListRefreshTime>REFRESH_THRESHHOLD)) {
 			requestExternalApplications();
 		}
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		this.isPaused = true;
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		this.isPaused = false;
 		if(lastSetLayout == LayoutState.SENSORS_HINT) {
 			initControls();
 		} else {
 			if((lastListRefreshTime == null)?true:(System.currentTimeMillis()-lastListRefreshTime>REFRESH_THRESHHOLD)) {
 				requestExternalApplications();
 			}
 		}
 		
 		Handler secondTryConnect = new Handler();
 		secondTryConnect.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				if(!isPaused) {
 					requestExternalApplications();
 				}
 			}
 		}, 2500);
 	}
 	
 	private void populateList(List<ExternalApplication> applications) {
 		listView = getListView();
 		String[] items = new String[applications.size()];
 		int counter = 0;
 		for (ExternalApplication app : applications) {
 			items[counter] = app.getName();
 			counter++;
 		}
 		
 		TextView instructionsView = (TextView) findViewById(R.id.availableApkHeaderInstructions);
 		if(instructionsView != null) {
 			if(applications.size() == 0) {
 				instructionsView.setText(R.string.availableApkList_emptyHint);
 			} else {
 				instructionsView.setText(R.string.availableApkList_defaultHint);
 			}
 		}
 		
 		List<Map<String, String>> listContent = new LinkedList<Map<String, String>>();
 		for(ExternalApplication app: applications) {
 			HashMap<String, String> rowMap = new HashMap<String, String>();
 			rowMap.put("name", app.getName());
 			rowMap.put("description", app.getDescription());
 			listContent.add(rowMap);
 
 		}
 		SimpleAdapter contentAdapter = new SimpleAdapter( 
 			this, 
 			listContent,
 			R.layout.availableabkslistitem,
 			new String[] { "name","description" },
 			new int[] { R.id.apklistitemtext, R.id.apklistitemdescription } );
 		
 		listView.setAdapter(contentAdapter);
 	}
 
 	public static String concatStacktrace(Exception e) {
 		String stackTrace = "";
 		for (int i = 0; i < e.getStackTrace().length; i++) {
 			stackTrace += e.getStackTrace()[i];
 		}
 		return stackTrace;
 	}
 
 }
