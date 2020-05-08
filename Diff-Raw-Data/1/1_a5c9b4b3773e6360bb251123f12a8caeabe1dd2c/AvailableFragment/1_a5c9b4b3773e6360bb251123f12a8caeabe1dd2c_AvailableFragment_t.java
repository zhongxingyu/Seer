 package de.da_sense.moses.client;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.ListFragment;
 import android.util.SparseIntArray;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import de.da_sense.moses.client.abstraction.ApkListRequestObserver;
 import de.da_sense.moses.client.abstraction.ApkMethods;
 import de.da_sense.moses.client.abstraction.apks.APKInstalled;
 import de.da_sense.moses.client.abstraction.apks.ApkDownloadManager;
 import de.da_sense.moses.client.abstraction.apks.ApkInstallManager;
 import de.da_sense.moses.client.abstraction.apks.ExternalApplication;
 import de.da_sense.moses.client.abstraction.apks.HistoryExternalApplicationsManager;
 import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplication;
 import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
 import de.da_sense.moses.client.service.MosesService;
 import de.da_sense.moses.client.service.helpers.ExecutableForObject;
 import de.da_sense.moses.client.util.InternetConnectionChangeListener;
 import de.da_sense.moses.client.util.Log;
 import de.da_sense.moses.client.util.Toaster;
 
 /**
  * Responsible for displaying the available APKs which get fetched from the
  * server.
  * 
  * @author Sandra Amend, Simon L
  * @author Zijad Maksuti
  */
 public class AvailableFragment extends ListFragment implements
 		ApkListRequestObserver, InternetConnectionChangeListener {
 	/**
 	 * Enums for the state of the layout.
 	 */
 	public static enum LayoutState {
 		NORMAL_LIST, EMPTYLIST_HINT, PENDING_REQUEST, NO_CONNECTIVITY;
 	}
 
 	/** saves the current position in the list */
 	private int mCurAvaiPosition = 0;
 	/** The current instance is saved in here. */
 	private static AvailableFragment thisInstance = null;
 
 	/** Threshold for the refresh time. */
 	private static final int REFRESH_THRESHHOLD = 800;
 	/** Listing of the applications */
 	private List<ExternalApplication> externalApps;
 	/** Variable to save when the last refresh of the list was. */
 	private Long lastListRefreshTime = null;
 	/** Save the last layout which was set. */
 	LayoutState lastSetLayout = null;
 
 	/** Size of the the APK being downloaded */
 	private int totalSize = -1;
 
 	/** variable for requestExternalApplications */
 	protected int requestListRetries = 0;
 	/** variable to check if the app is paused */
 	private boolean isPaused;
 	/** a log tag for this class */
 	private final static String TAG = "AvailableFragment";
 	/** mapping from filtered (displayed) to non-filtered (externalApps) list */
 	private SparseIntArray listIndex = new SparseIntArray();
 	
 	
 	/**
 	 * The activity containing this fragment
 	 */
 	private Activity mActivity;
 
 	/** Returns the current instance (singleton) */
 	public static AvailableFragment getInstance() {
 		return thisInstance;
 	}
 
 	/**
 	 * @return the externalApps
 	 */
 	public List<ExternalApplication> getExternalApps() {
 		return externalApps;
 	}
 
 	/**
 	 * @param externalApps
 	 *            the externalApps to set
 	 */
 	public void setExternalApps(List<ExternalApplication> externalApps) {
 		this.externalApps = externalApps;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		initControls();
 
 		if (savedInstanceState != null) {
 			// restore last state
 			mCurAvaiPosition = savedInstanceState.getInt("curChoice", 0);
 		}
 	}
 
 	/**
 	 * Helper method for showing the details of a userstudy.
 	 * 
 	 * @param index
 	 *            the index of the userstudy to show the details for
 	 */
 	protected void showDetails(int index, Activity baseActivity,
 			final Runnable installAppClickAction,
 			final Runnable cancelClickAction) {
 		if (MosesService.isOnlineOrIsConnecting(mActivity
 				.getApplicationContext())) {
 			if (getListView() != null) {
 				if (getExternalApps() != null) {
 					final ExternalApplication app = getExternalApps()
 							.get(index);
 
 					// dual mode: we can display everything on the screen
 					// update list to highlight the selected item and show data
 					getListView().setItemChecked(index, true);
 
 					// otherwise launch new activity to display the fragment
 					// with selected text
 					Intent intent = new Intent();
 					intent.setClass(mActivity, DetailActivity.class);
 					intent.putExtra("de.da_sense.moses.client.index", index);
 					intent.putExtra("de.da_sense.moses.client.belongsTo",
 							DetailFragment.AVAILABLE);
 					intent.putExtra("de.da_sense.moses.client.appname",
 							app.getName());
 					intent.putExtra("de.da_sense.moses.client.description",
 							app.getDescription());
 					intent.putExtra(ExternalApplication.KEY_APK_ID,
 							app.getID());
 					intent.putExtra("de.da_sense.moses.client.apkVersion",
 							app.getApkVersion());
 					intent.putExtra("de.da_sense.moses.client.startDate",
 							app.getStartDateAsString());
 					intent.putExtra("de.da_sense.moses.client.endDate",
 							app.getEndDateAsString());
 					startActivity(intent);
 				}
 			} else {
 				showNoConnectionInfoBox();
 			}
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putInt("curChoice", mCurAvaiPosition);
 		Log.d("AvailableFragment", "onSaveInstanceState called");
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		this.isPaused = true;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		thisInstance = this;
 		Log.d("AvailableFragment", "onCreate: parentActivity = "
 				+ mActivity.getClass().getSimpleName());
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		Log.d("AvailableFragment", "onCreateView about to inflate View");
 		View availbleFragmentView = inflater.inflate(R.layout.tab_available,
 				container, false);
 		container.setBackgroundColor(getResources().getColor(
 				android.R.color.background_light));
 
 		return availbleFragmentView;
 	}
 
 	/**
 	 * Method to save the last layout which was set.
 	 * 
 	 * @param lastSetLayout
 	 *            the last layout
 	 */
 	public void setLastSetLayout(LayoutState lastSetLayout) {
 		this.lastSetLayout = lastSetLayout;
 		Log.d("MoSeS.UI", "Layouted showAvailableApkList to state "
 				+ lastSetLayout);
 	}
 
 	/**
 	 * Show a dialog to inform that there is no internet connection.
 	 */
 	private void showNoConnectionInfoBox() {
 		new AlertDialog.Builder(mActivity)
 				.setMessage(
 						getString(R.string.availableTab_noInternetConnection))
 				.setTitle(getString(R.string.noInternetConnection_title))
 				.setCancelable(true).show();
 	}
 
 	/**
 	 * Initialize the controls in the gui.
 	 */
 	private void initControls() {
 		initControlsOnRequestApks();
 		requestExternalApplications();
 	}
 
 	/**
 	 * Checks if the app list is still in the cache available.
 	 * 
 	 * @return true if the apps are still available
 	 */
 	private boolean appsLocallyInCacheStillAvailable() {
 		return getExternalApps() != null && getExternalApps().size() > 0;
 	}
 
 	/**
 	 * Initialize the controls to request the APKs. This Method calls a
 	 * different initControls... depending on the state of the application.
 	 */
 	private void initControlsOnRequestApks() {
 		if (appsLocallyInCacheStillAvailable()) {
 			// if the App list is still cached just display it
 			initControlsNormalList(getExternalApps());
 		} else {
 			if (MosesService.isOnlineOrIsConnecting(WelcomeActivity
 					.getInstance())) {
 				initControlsPendingListRequest();
 			} else {
 				initControlsNoConnectivity();
 			}
 		}
 	}
 
 	/**
 	 * Controls what to show and what to do when there is no connection.
 	 */
 	private void initControlsNoConnectivity() {
 		/*
 		 * Following statements need to be executed regardless if we the last
 		 * layout state NO_CONNECTIVITY or not
 		 */
 		LinearLayout apkListCtrls = (LinearLayout) mActivity.findViewById(
 				R.id.apklist_mainListLayout);
 		apkListCtrls.setVisibility(View.GONE);
 		// display button to refresh and set action to perform
 		final Button actionBtn1 = (Button) mActivity.findViewById(
 				R.id.apklist_emptylistActionBtn1);
 		final String tryToReconnectMessage = getString(R.string.retry);
 
 		actionBtn1.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				refreshResfreshBtnTimeout(actionBtn1, tryToReconnectMessage,
 						LayoutState.NO_CONNECTIVITY);
 				requestExternalApplications();
 			}
 		});
 		if (lastSetLayout != LayoutState.NO_CONNECTIVITY) {
 			// no connection so show an empty list
 			LinearLayout emptylistCtrls = (LinearLayout) mActivity
 					.findViewById(R.id.apklist_emptylistLayout);
 			emptylistCtrls.setVisibility(View.VISIBLE);
 
 			// set hint that there is no connection
 			TextView mainHint = (TextView) mActivity.findViewById(
 					R.id.apklist_emptylistHintMain);
 			mainHint.setText(R.string.apklist_hint_noconnectivity);
 
 			// set the last layout
 			setLastSetLayout(LayoutState.NO_CONNECTIVITY);
 
 			refreshResfreshBtnTimeout(actionBtn1, tryToReconnectMessage,
 					LayoutState.NO_CONNECTIVITY);
 		}
 	}
 
 	/**
 	 * Controls what to show and what to do during a pending request.
 	 */
 	private void initControlsPendingListRequest() {
 
 		// during a pending request show an empty list
 		LinearLayout emptylistCtrls = (LinearLayout) mActivity
 				.findViewById(R.id.apklist_emptylistLayout);
 		emptylistCtrls.setVisibility(View.VISIBLE);
 		LinearLayout apkListCtrls = (LinearLayout) mActivity.findViewById(
 				R.id.apklist_mainListLayout);
 		apkListCtrls.setVisibility(View.GONE);
 
 		// display hint that there is a pending request
 		TextView mainHint = (TextView) mActivity.findViewById(
 				R.id.apklist_emptylistHintMain);
 		mainHint.setText(R.string.apklist_hint_pendingrequest);
 
 		// show a refresh button and add an action
 		final Button actionBtn1 = (Button) mActivity.findViewById(
 				R.id.apklist_emptylistActionBtn1);
 		String refreshButtonMessage = getString(R.string.refresh);
 		actionBtn1.setText(refreshButtonMessage);
 
 		actionBtn1.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				refreshResfreshBtnTimeout(actionBtn1,
 						getString(R.string.refresh),
 						LayoutState.PENDING_REQUEST);
 				requestExternalApplications();
 			}
 		});
 
 		if (lastSetLayout != LayoutState.PENDING_REQUEST) {
 
 			refreshResfreshBtnTimeout(actionBtn1, refreshButtonMessage,
 					LayoutState.PENDING_REQUEST);
 
 			// set the last layout
 			setLastSetLayout(LayoutState.PENDING_REQUEST);
 		}
 	}
 
 	/**
 	 * Controls the appearance of the button during a refresh. It disables the
 	 * button and animates its text by periodically appending full stops. At the
 	 * end of the animation, the button gets enabled with the default text.
 	 * 
 	 * @param refreshButton
 	 *            the button which appearance will change
 	 * @param minimalString
 	 *            a String for the starting message
 	 * @param parentLayout
 	 *            the current state of the layout (it may change during the
 	 *            refresh)
 	 */
 	private void refreshResfreshBtnTimeout(final Button refreshButton,
 			final String minimalString, final LayoutState parentLayout) {
 		// disable the button during a refresh
 		refreshButton.setEnabled(false);
 		refreshButton.setText(minimalString);
 		// changes the text on the button over time
 		Handler enableRefreshHandler = new Handler();
 		enableRefreshHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				if (!isPaused && lastSetLayout == parentLayout) {
 					refreshButton.setText(minimalString + ".");
 				}
 			}
 		}, 800);
 		enableRefreshHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				if (!isPaused && lastSetLayout == parentLayout) {
 					refreshButton.setText(minimalString + "..");
 				}
 			}
 		}, 1600);
 		enableRefreshHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				if (!isPaused && lastSetLayout == parentLayout) {
 					refreshButton.setText(minimalString + "...");
 				}
 			}
 		}, 2400);
 		// and then enables the button
 		enableRefreshHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				if (!isPaused && lastSetLayout == parentLayout) {
 					refreshButton.setText(minimalString);
 					refreshButton.setEnabled(true);
 				}
 			}
 		}, 3000);
 	}
 
 	/**
 	 * TODO: Javadoc
 	 */
 	private void initLayoutFromArrivedList(
 			List<ExternalApplication> applications) {
 		if (applications.size() > 0) {
 			initControlsNormalList(applications);
 		} else {
 			initControlsEmptyArrivedList(false);
 		}
 		populateList(applications); // TODO : Why should we execute this method
 									// when we do this in initControlsNormalList
 									// ?
 	}
 
 	/**
 	 * Controls what to do and what to show in case we get an empty APK list.
 	 * 
 	 * @param mayShowSensorsList
 	 *            true to show the sensor hint
 	 */
 	private void initControlsEmptyArrivedList(boolean mayShowSensorsList) {
 		if (lastSetLayout != LayoutState.EMPTYLIST_HINT) {
 			// show an empty list, because the list we got was empty
 			LinearLayout emptylistCtrls = (LinearLayout) mActivity
 					.findViewById(R.id.apklist_emptylistLayout);
 			emptylistCtrls.setVisibility(View.VISIBLE);
 			LinearLayout apkListCtrls = (LinearLayout) mActivity
 					.findViewById(R.id.apklist_mainListLayout);
 			apkListCtrls.setVisibility(View.GONE);
 
 			// show a hint, that there are no apks
 			TextView mainHint = (TextView) mActivity.findViewById(
 					R.id.apklist_emptylistHintMain);
 			mainHint.setText(R.string.availableApkList_emptyHint);
 
 			// we don't need any buttons here
 			Button actionBtn1 = (Button) mActivity.findViewById(
 					R.id.apklist_emptylistActionBtn1);
 			actionBtn1.setVisibility(View.GONE);
 
 			// set last layout
 			setLastSetLayout(LayoutState.EMPTYLIST_HINT);
 		}
 	}
 
 	/**
 	 * Controls what to do and what to show in case we get a non empty list of
 	 * applications.
 	 * 
 	 * @param applications
 	 *            list of the applications to show
 	 */
 	private void initControlsNormalList(List<ExternalApplication> applications) {
 		// show a "normal" non-empty list
 		LinearLayout emptylistCtrls = (LinearLayout) mActivity
 				.findViewById(R.id.apklist_emptylistLayout);
 		// TODO: fast switching between tabs causes NullPointerExc, where
 		// emptylistCtrls and apkListCtrls will be here NULL
 		if (emptylistCtrls != null)
 			emptylistCtrls.setVisibility(View.GONE);
 
 		LinearLayout apkListCtrls = (LinearLayout) mActivity.findViewById(
 				R.id.apklist_mainListLayout);
 
 		if (apkListCtrls != null)
 			apkListCtrls.setVisibility(View.VISIBLE);
 
 		// set last layout
 		setLastSetLayout(LayoutState.NORMAL_LIST);
 		// and show the applications in the list
 		populateList(applications);
 	}
 
 	/**
 	 * FIXME: The ProgressDialog doesn't show up. Handles installing APK from
 	 * the Server.
 	 * 
 	 * @param app
 	 *            the App to download and install
 	 */
 	protected void handleInstallApp(ExternalApplication app) {
 
 		final ProgressDialog progressDialog = new ProgressDialog(
 				WelcomeActivity.getInstance());
 
 		Log.d(TAG, "progressDialog = " + progressDialog);
 
 		final ApkDownloadManager downloader = new ApkDownloadManager(app,
 				WelcomeActivity.getInstance().getApplicationContext(),// getActivity().getApplicationContext(),
 				new ExecutableForObject() {
 					@Override
 					public void execute(final Object o) {
 						if (o instanceof Integer) {
 							WelcomeActivity.getInstance().runOnUiThread(
 									new Runnable() {
 										@Override
 										public void run() {
 											if (totalSize == -1) {
 												totalSize = (Integer) o / 1024;
 												progressDialog
 														.setMax(totalSize);
 											} else {
 												progressDialog.incrementProgressBy(((Integer) o / 1024)
 														- progressDialog
 																.getProgress());
 											}
 										}
 									});
 							/*
 							 * They were : Runnable runnable = new Runnable() {
 							 * Integer temporary = (Integer) o / 1024;
 							 * 
 							 * @Override public void run() { if (totalSize ==
 							 * -1) { totalSize = temporary;
 							 * progressDialog.setMax(totalSize); } else {
 							 * progressDialog .incrementProgressBy( temporary -
 							 * progressDialog.getProgress()); } } };
 							 * getActivity().runOnUiThread(runnable);
 							 */
 						}
 					}
 				});
 
 		progressDialog.setTitle(getString(R.string.downloadingApp));
 		progressDialog.setMessage(getString(R.string.pleaseWait));
 		progressDialog.setMax(0);
 		progressDialog.setProgress(0);
 		progressDialog.setOnCancelListener(new OnCancelListener() {
 			@Override
 			public void onCancel(DialogInterface dialog) {
 				downloader.cancel();
 			}
 		});
 
 		progressDialog.setCancelable(true);
 		progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						if (progressDialog.isShowing())
 							progressDialog.cancel();
 					}
 				});
 		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 
 		Observer observer = new Observer() {
 			@Override
 			public void update(Observable observable, Object data) {
 				if (downloader.getState() == ApkDownloadManager.State.ERROR) {
 					// error downloading
 					if (progressDialog.isShowing()) {
 						progressDialog.dismiss();
 					}
 					showMessageBoxErrorDownloading(downloader);
 				} else if (downloader.getState() == ApkDownloadManager.State.ERROR_NO_CONNECTION) {
 					// error with connection
 					if (progressDialog.isShowing()) {
 						progressDialog.dismiss();
 					}
 					showMessageBoxErrorNoConnection(downloader);
 				} else if (downloader.getState() == ApkDownloadManager.State.FINISHED) {
 					// success
 					if (progressDialog.isShowing()) {
 						progressDialog.dismiss();
 					}
 					installDownloadedApk(downloader.getDownloadedApk(),
 							downloader.getExternalApplicationResult());
 				}
 			}
 		};
 		downloader.addObserver(observer);
 		totalSize = -1;
 		// progressDialog.show(); FIXME: commented out in case it throws an
 		// error
 		downloader.start();
 	}
 
 	/**
 	 * Shows an AlertDialog which informs the user about a missing internet
 	 * connection encountered while downloading an app.
 	 * 
 	 * @param downloader
 	 *            the download manager which encountered the error
 	 */
 	protected void showMessageBoxErrorNoConnection(ApkDownloadManager downloader) {
 		new AlertDialog.Builder(WelcomeActivity.getInstance())
 				.setMessage(getString(R.string.noInternetConnection_message))
 				.setTitle(getString(R.string.noInternetConnection_title))
 				.setCancelable(true)
 				.setNeutralButton(getString(R.string.ok), null).show();
 	}
 
 	/**
 	 * Shows an AlertDialog which informs the user about an error which occured
 	 * while downloading an app.
 	 * 
 	 * @param downloader
 	 *            the download manager which encountered the error
 	 */
 	protected void showMessageBoxErrorDownloading(ApkDownloadManager downloader) {
 		new AlertDialog.Builder(WelcomeActivity.getInstance())
 				.setMessage(
 						getString(R.string.downloadApk_errorMessage,
 								downloader.getErrorMsg()))
 				.setTitle(getString(R.string.error)).setCancelable(true)
 				.setNeutralButton(getString(R.string.ok), null).show();
 	}
 
 	/**
 	 * Install an APK file on the device.
 	 * 
 	 * @param originalApk
 	 *            the APK file
 	 * @param externalAppRef
 	 *            the reference to the app on the MoSeS server
 	 */
 	private void installDownloadedApk(final File originalApk,
 			final ExternalApplication externalAppRef) {
 		final ApkInstallManager installer = new ApkInstallManager(originalApk,
 				externalAppRef, mActivity.getApplicationContext());
 		installer.addObserver(new Observer() {
 			@Override
 			public void update(Observable observable, Object data) {
 				if (installer.getState() == ApkInstallManager.State.ERROR) {
 					// nothing?
 				} else if (installer.getState() == ApkInstallManager.State.INSTALLATION_CANCELLED) {
 					// TODO: how to handle if the user cancels the installation?
 				} else if (installer.getState() == ApkInstallManager.State.INSTALLATION_COMPLETED) {
 					new APKInstalled(externalAppRef.getID());
 					try {
 						ApkInstallManager.registerInstalledApk(originalApk,
 								externalAppRef, WelcomeActivity.getInstance() // It
 																				// was
 																				// :
 																				// AvailableFragment.this.getActivity())
 										.getApplicationContext(), false);
 					} catch (IOException e) {
 						Log.e("MoSeS.Install",
 								"Problems with extracting package name from "
 										+ "apk, or problems with the "
 										+ "InstalledExternalApplicationsManager after "
 										+ "installing an app");
 					}
 				}
 			}
 		});
 		installer.start();
 	}
 
 	/**
 	 * Request the list of available apps from the server and initialize the
 	 * request APK controls.
 	 */
 	private void requestExternalApplications() {
 		if (MosesService.getInstance() == null) {
 			if (requestListRetries < 5) {
 				Handler delayedRetryHandler = new Handler();
 				delayedRetryHandler.postDelayed(new Runnable() {
 					@Override
 					public void run() {
 						requestExternalApplications();
 					}
 				}, 1000);
 				requestListRetries++;
 			} else {
 				// TODO: show error when all retries didn't work?
 			}
 		} else {
 			requestListRetries = 0;
 			lastListRefreshTime = System.currentTimeMillis();
 			ApkMethods.getExternalApplications(AvailableFragment.this);
 			initControlsOnRequestApks();
 		}
 	}
 
 	/**
 	 * This Method gets called after the Fragment received a message, that the
 	 * list request is finished. It then sets the eternal applications and
 	 * initializes the necessary controls.
 	 * 
 	 * @see de.da_sense.moses.client.abstraction.ApkListRequestObserver#apkListRequestFinished(java.util.List)
 	 */
 	@Override
 	public void apkListRequestFinished(List<ExternalApplication> applications) {
 		setExternalApps(applications);
 		initLayoutFromArrivedList(applications);
 	}
 
 	/**
 	 * This method gets called after the Fragment received a message, that the
 	 * list request failed. So far it only creates a log entry. TODO: receive
 	 * failures that point out no connection, too; show user some hint about
 	 * this
 	 * 
 	 * @see de.da_sense.moses.client.abstraction.ApkListRequestObserver#apkListRequestFailed(java.lang.Exception)
 	 */
 	@Override
 	public void apkListRequestFailed(Exception e) {
 		Toaster.showBadServerResponseToast();
 		Log.w("MoSeS.APKMETHODS",
 				"invalid response for apk list request: " + e.getMessage());
 	}
 
 	/**
 	 * Gets called from MainActivity, because Fragments don't override
 	 * onWindowFocusChanged. TODO: Is there a better way? Reason this got added:
 	 * to reload the list after a focus change.
 	 */
 	public void onWindowFocusChangedFragment(boolean hasFocus) {
 		if (hasFocus && (lastListRefreshTime == null) ? true
 				: (System.currentTimeMillis() - lastListRefreshTime > REFRESH_THRESHHOLD)) {
 			requestExternalApplications();
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		this.isPaused = false;
 		boolean checkRefreshTime = (lastListRefreshTime == null) ? true
 				: (System.currentTimeMillis() - lastListRefreshTime > REFRESH_THRESHHOLD);
 		if (checkRefreshTime) {
 			requestExternalApplications();
 		}
 
 		Handler secondTryConnect = new Handler();
 		secondTryConnect.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				if (!isPaused) {
 					requestExternalApplications();
 				}
 			}
 		}, 2500);
 	}
 
 	/**
 	 * Return the mapping of the element from the non-filtered list to the
 	 * filtered list. To access the right ExternalApplication when clicking on
 	 * an element in the displayed (filtered) available list.
 	 * 
 	 * @param id
 	 *            the element position in the filtered list
 	 * @return the position in the non-filtered list
 	 */
 	public Integer getListIndexElement(Integer id) {
 		return listIndex.get(id);
 	}
 
 	/**
 	 * Populate the application list with the app names and their descriptions.
 	 * 
 	 * @param applications
 	 */
 	private void populateList(List<ExternalApplication> applications) {
 		// we don't want to display already installed apps / participated user
 		// studies
 		if (InstalledExternalApplicationsManager.getInstance() == null)
 			InstalledExternalApplicationsManager.init(mActivity);
 		LinkedList<InstalledExternalApplication> installedApps = InstalledExternalApplicationsManager
 				.getInstance().getApps();
 		// and history apps
 		if (HistoryExternalApplicationsManager.getInstance() == null)
 			HistoryExternalApplicationsManager.init(mActivity);
 
 		HashSet<String> hashAppIDs = new HashSet<String>();
 		// collect all IDs from installed apps
 		for (InstalledExternalApplication installedApp : installedApps) {
 			hashAppIDs.add(installedApp.getID());
 		}
 		// we want to get the real number of apps to show
 		// special care has to be taken for history apps which might not anymore
 		// be in the externalApps list received from server
 		// thats why we check which external apps are in the history and not how
 		// many apps are in history
 		HashSet<ExternalApplication> realHistApps = new HashSet<ExternalApplication>();
 		for (ExternalApplication app : applications) {
 			if (HistoryExternalApplicationsManager.getInstance().containsApp(
 					app)) {
 				realHistApps.add(app);
 			}
 		}
 		// we set everything up, so that the installed apps
 		// and the history apps are (should be) disjoint
 		int numberOfApps = applications.size() - hashAppIDs.size()
 				- realHistApps.size();
 
 		Log.d(TAG,
 				"installed: " + hashAppIDs.size() + " history: "
 						+ realHistApps.size() + " available before: "
 						+ applications.size() + " available now: "
 						+ numberOfApps);
 
 		TextView instructionsView = (TextView) mActivity.findViewById(
 				R.id.availableApkHeaderInstructions);
 		// show hint depending the number of available apps
 		if (instructionsView != null) {
 			if (numberOfApps <= 0) { // changed from: if (applications.size() ==
 										// 0)
 				instructionsView.setText(R.string.availableApkList_emptyHint);
 			} else {
 				instructionsView.setText(R.string.availableApkList_defaultHint);
 			}
 		}
 
 		List<Map<String, String>> listContent = new LinkedList<Map<String, String>>();
 		int i = 0, j = 0;
 		for (ExternalApplication app : applications) {
 			HashMap<String, String> rowMap = new HashMap<String, String>();
 			// only add it to displayed list, if it's not installed or in
 			// history
 			if (!hashAppIDs.contains(app.getID())
 					&& !realHistApps.contains(app)) {
 				rowMap.put("name", app.getName());
 				listContent.add(rowMap);
 				listIndex.put(j, i);
 				j++;
 			}
 			i++;
 		}
 
 		MosesListAdapter contentAdapter = new MosesListAdapter(mActivity,
 				listContent, R.layout.availableapkslistitem,
 				new String[] { "name" }, new int[] { R.id.apklistitemtext });
 
 		setListAdapter(contentAdapter);
 
 	}
 
 	/**
 	 * Concatenate the stack trace of an exception to one String.
 	 * 
 	 * @param e
 	 *            the exception to concatenate
 	 * @return the concatenated String of the exception
 	 */
 	public static String concatStacktrace(Exception e) {
 		String stackTrace = "";
 		for (int i = 0; i < e.getStackTrace().length; i++) {
 			stackTrace += e.getStackTrace()[i];
 		}
 		return stackTrace;
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		mActivity = activity;
 	}
 
 	@Override
 	public void onDetach() {
 		super.onDetach();
 	}
 
 	@Override
 	public void onConnectionLost() {
 		// TODO Auto-generated method stub
 		// not interested in this. FOR NOW! muhahahahaha!
 		// act now I am crazy
 	}
 
 	@Override
 	public void onConnectionEstablished() {
 		// if the fragment is visible and is in NO_CONNECTIVITY state,
 		// request the list from the server
 		if (this.isVisible()
 				&& lastSetLayout.equals(LayoutState.NO_CONNECTIVITY))
 			requestExternalApplications();
 
 	}
 
 }
