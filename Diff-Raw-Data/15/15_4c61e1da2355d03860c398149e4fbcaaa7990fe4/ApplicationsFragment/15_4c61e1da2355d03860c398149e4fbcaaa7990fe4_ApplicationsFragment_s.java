 package com.Akkad.AndroidBackup;
 
 import java.util.Collections;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class ApplicationsFragment extends SherlockFragment {
 
 	private static Dialog appPopupDialog;
 	static ListView backupList;
 	private static final String TAG = "ApplicationsActivity";
 
 	public static void displayBackupOnAppPopup(String packageName) {
 		backupList = (ListView) appPopupDialog.findViewById(R.id.lvbackups);
 		BackupListAdapter backupListAdapter = new BackupListAdapter(appPopupDialog.getContext(), BackupStore.getPackageBackupInformation(packageName)); // create new adapter
 		backupList.setAdapter(backupListAdapter); // set adapter to list view
 	}
 
 	/**
 	 * Get all installed application on device and return a list
 	 * 
 	 * @param c
 	 *            Context of application
 	 * 
 	 * @return list of installed applications
 	 */
	public static List<ApplicationInfo> getInstalledApplication(Context context) {
		PackageManager packageManager = context.getPackageManager();
 		List<ApplicationInfo> apps = packageManager.getInstalledApplications(0);
 
 		/* Loops through the app list and removes Android Backup & Android System */
 		boolean androidBackupRemoved = false, androidSystemRemoved = false;
 		for (int i = 0; i < apps.size(); i++) {
			if (apps.get(i).loadLabel(packageManager).equals("Android Backup")) {
				apps.remove(i);
 				androidBackupRemoved = true;
 				if (androidBackupRemoved && androidSystemRemoved) {
 					break;
 				}
 			} else if ((apps.get(i).loadLabel(packageManager).equals("Android System"))) {
				apps.remove(i);
 				androidSystemRemoved = true;
 				if (androidBackupRemoved && androidSystemRemoved) {
 					break;
 				}
 			}
 		}
 		Collections.sort(apps, new ApplicationInfo.DisplayNameComparator(packageManager));
 		return apps;
 	}
 
 	/**
 	 * Launch an application
 	 * 
 	 * @param c
 	 *            Context of application
 	 * 
 	 * @param pm
 	 *            the related package manager of the context
 	 * 
 	 * @param pkgName
 	 *            Name of the package to run
 	 */
 	public static boolean launchApp(Context c, PackageManager pm, String pkgName) {
 		Intent intent = pm.getLaunchIntentForPackage(pkgName); // query the intent for launching
 		if (intent != null) { // if intent is available
 			try {
 				c.startActivity(intent); // launch application
 				return true; // if succeed
 			} catch (ActivityNotFoundException ex) { // if application failed to launch
 				Toast.makeText(c, R.string.toast_notification_apprun_notfound, Toast.LENGTH_LONG).show(); // quick message notification
 			}
 		}
 		return false; // by default, fail to launch
 	}
 
 	public void createApplicationInformationDialog(String appName, String packageName, String appVersion, String apkPath, String dataPath) {
 		appPopupDialog.hide();
 		final Dialog applicationInformationDialog = new Dialog(getActivity());
 		applicationInformationDialog.setTitle(getString(R.string.applications_information_current_version_info_title));
 		applicationInformationDialog.setContentView(R.layout.application_info_dialog);
 
 		TextView tvAppName = (TextView) applicationInformationDialog.findViewById(R.id.appNameValue);
 		tvAppName.setText(appName);
 
 		TextView tvPackageName = (TextView) applicationInformationDialog.findViewById(R.id.packageNameValue);
 		tvPackageName.setText(packageName);
 
 		TextView tvappVersion = (TextView) applicationInformationDialog.findViewById(R.id.appVersionValue);
 		tvappVersion.setText(appVersion);
 
 		TextView tvApkPath = (TextView) applicationInformationDialog.findViewById(R.id.appApkPathValue);
 		tvApkPath.setText(apkPath);
 
 		TextView tvDataPath = (TextView) applicationInformationDialog.findViewById(R.id.appDataPathValue);
 		tvDataPath.setText(dataPath);
 
 		applicationInformationDialog.setCanceledOnTouchOutside(true);
 		applicationInformationDialog.show();
 
 		applicationInformationDialog.setOnDismissListener(new OnDismissListener() {
 			@Override
 			public void onDismiss(DialogInterface dialog) {
 				appPopupDialog.show();
 			}
 		});
 	}
 
 	private Core core = new Core();
 
 	private ListView mListAppInfo;
 
 	private ApplicationInfo selectedApp;
 
 	public void createCurrentApplicationInformationDialog(ApplicationInfo app) {
 		String appVersion;
 		try {
 			PackageInfo info = getActivity().getPackageManager().getPackageInfo(app.packageName, 0);
 			appVersion = info.versionName + " (" + info.versionCode + ")";
 		} catch (NameNotFoundException e) {
 			appVersion = getActivity().getResources().getString(R.string.application_version_not_available);
 		}
 
 		createApplicationInformationDialog("" + app.loadLabel(getActivity().getPackageManager()), app.packageName, appVersion, app.sourceDir, app.dataDir);
 	}
 
 	View view;
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
 		view = inflater.inflate(R.layout.applications_layout, container, false);
 
 		refreshAppList(); // Populates the application list
 
 		mListAppInfo.setOnItemClickListener(new OnItemClickListener() { // implement event when an item on list view is selected
 
 					public void onItemClick(final AdapterView parent, View view, int pos, long id) {
 						// get the list adapter
 						AppInfoAdapter appInfoAdapter = (AppInfoAdapter) parent.getAdapter(); // get selected item on the list
 						selectedApp = (ApplicationInfo) appInfoAdapter.getItem(pos);
 
 						// custom dialog
 						appPopupDialog = new Dialog(getActivity());
 						appPopupDialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
 
 						appPopupDialog.setContentView(R.layout.applications_popup);
 						appPopupDialog.setFeatureDrawable(Window.FEATURE_LEFT_ICON, selectedApp.loadIcon(getActivity().getPackageManager()));
 						appPopupDialog.setCanceledOnTouchOutside(true);
 
 						final TextView currentAppInfo = (TextView) appPopupDialog.findViewById(R.id.applicationsDialogCurrentAppInfo);
 
 						try {
 							PackageInfo info = getActivity().getPackageManager().getPackageInfo(selectedApp.packageName, 0);
 							String appAndVersion = selectedApp.loadLabel(getActivity().getPackageManager()) + " " + info.versionName;
 							appPopupDialog.setTitle(appAndVersion);
 							currentAppInfo.setText(appAndVersion + " (" + info.versionCode + ")");
 						} catch (NameNotFoundException e) {
 							appPopupDialog.setTitle(selectedApp.loadLabel(getActivity().getPackageManager()));
 						}
 
 						displayBackupOnAppPopup(selectedApp.packageName);
 
 						currentAppInfo.setOnClickListener(new OnClickListener() {
 							@Override
 							public void onClick(View v) {
 								createCurrentApplicationInformationDialog(selectedApp);
 							}
 						});
 
 						final Button dialogRunButton = (Button) appPopupDialog.findViewById(R.id.applicationsDialogButtonRun);
 						dialogRunButton.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								launchApp(parent.getContext(), getActivity().getPackageManager(), selectedApp.packageName); // launches the selected application
 								appPopupDialog.dismiss(); // closes the dialog
 							}
 						});
 
 						final Button dialogUninstallButton = (Button) appPopupDialog.findViewById(R.id.applicationsDialogButtonUninstall);
 						dialogUninstallButton.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								if (InformationFragment.isRooted()) {
 									if (core.applicationsType(selectedApp.sourceDir) == 1) { // A System App
 										final AlertDialog.Builder uninstallSystemAppWarningDialog = new AlertDialog.Builder(getActivity());
 										uninstallSystemAppWarningDialog.setTitle(getString(R.string.uninstall_system_app_warning_dialog_title));
 										uninstallSystemAppWarningDialog.setMessage(selectedApp.loadLabel(getActivity().getPackageManager()) + " "
 												+ getString(R.string.uninstall_system_app_warning_dialog_text));
 										uninstallSystemAppWarningDialog.setIcon(android.R.drawable.ic_dialog_alert);
 										uninstallSystemAppWarningDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
 											public void onClick(DialogInterface dialog, int id) {
 												core.UninstallAppRoot(selectedApp.packageName, selectedApp.sourceDir);
 												refreshAppList();
 												dialog.dismiss();
 											}
 										});
 										uninstallSystemAppWarningDialog.setNegativeButton(getString(R.string.no), null);
 										uninstallSystemAppWarningDialog.show();
 
 									} else {
 										core.UninstallAppRoot(selectedApp.packageName, selectedApp.sourceDir);
 										refreshAppList();
 										appPopupDialog.dismiss();
 									}
 								} else {
 									Uri packageURI = Uri.parse("package:" + selectedApp.packageName);
 									Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
 									startActivity(uninstallIntent);
 									refreshAppList();
 								}
 							}
 						});
 
 						Button dialogWipeDataButton = (Button) appPopupDialog.findViewById(R.id.applicationsDialogButtonWipeData);
 						// TODO this check is not needed
 						if (InformationFragment.isRooted()) {
 							dialogWipeDataButton.setOnClickListener(new OnClickListener() {
 								@Override
 								public void onClick(View v) {
 									if (Core.wipeAppData(selectedApp.packageName)) {
 										Toast.makeText(getActivity(), selectedApp.loadLabel(getActivity().getPackageManager()) + "'s data deleted", Toast.LENGTH_LONG).show();
 									} else {
 										Toast.makeText(getActivity(), selectedApp.loadLabel(getActivity().getPackageManager()) + "'s data not deleted", Toast.LENGTH_LONG).show();
 									}
 								}
 							});
 						} else {
 							dialogWipeDataButton.setEnabled(false); // disable the wipe data button
 							dialogWipeDataButton = null; // Set dialogWipeDataButton to null to allow for garbage collection
 
 							if (core.applicationsType(selectedApp.sourceDir) == 1) // disable uninstall button if the app is a system app and the device is not rooted
 							{
 								dialogUninstallButton.setEnabled(false); // disable the wipe data button
 							}
 						}
 
 						Button dialogBackupButton = (Button) appPopupDialog.findViewById(R.id.applicationsDialogButtonBackup);
 						dialogBackupButton.setOnClickListener(new OnClickListener() {
 							@Override
 							public void onClick(View v) {
 								String appName = selectedApp.loadLabel(getActivity().getPackageManager()).toString();
 
 								// core.backupApplicationApk(selectedApp.loadLabel(getActivity().getPackageManager()).toString(), selectedApp.packageName, selectedApp.sourceDir);
 								core.backupApplication(selectedApp, getActivity().getPackageManager());
 								refreshAppList();
 								displayBackupOnAppPopup(selectedApp.packageName);
 								Toast.makeText(getActivity(), appName + " has been backed successfully", Toast.LENGTH_LONG).show();
 							}
 						});
 
 						dialogRunButton.setEnabled(getActivity().getPackageManager().getLaunchIntentForPackage(selectedApp.packageName) != null);
 						dialogBackupButton.setEnabled((core.applicationsType(selectedApp.sourceDir) == 1 && InformationFragment.isRooted() || core.applicationsType(selectedApp.sourceDir) != 1));
 
 						appPopupDialog.show();
 					}
 				});
 		return view;
 	}
 
 	@Override
 	public void onResume() {
 		refreshAppList();
 		super.onResume();
 	}
 
 	/**
 	 * Refreshes the application list
 	 */
 	private void refreshAppList() {
 		mListAppInfo = (ListView) view.findViewById(R.id.lvApps); // load list application
		AppInfoAdapter appInfoAdapter = new AppInfoAdapter(getActivity(), getInstalledApplication(getActivity()), getActivity().getPackageManager()); // create new adapter
 
 		mListAppInfo.setAdapter(appInfoAdapter); // set adapter to list view
 	}
 }
