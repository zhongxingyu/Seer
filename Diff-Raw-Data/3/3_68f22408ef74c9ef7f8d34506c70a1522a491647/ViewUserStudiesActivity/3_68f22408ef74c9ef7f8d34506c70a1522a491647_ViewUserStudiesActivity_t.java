 package moses.client;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Observable;
 import java.util.Observer;
 
 import moses.client.abstraction.apks.APKInstalled;
 import moses.client.abstraction.apks.ApkDownloadManager;
 import moses.client.abstraction.apks.ApkInstallManager;
 import moses.client.abstraction.apks.ExternalApplication;
 import moses.client.com.NetworkJSON.BackgroundException;
 import moses.client.com.ReqTaskExecutor;
 import moses.client.com.requests.RequestGetApkInfo;
 import moses.client.service.MosesService;
 import moses.client.service.helpers.Executor;
 import moses.client.userstudy.UserStudyNotification;
 import moses.client.userstudy.UserstudyNotificationManager;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Viewing and installing apks from the server
  * 
  * @author Simon L
  */
 public class ViewUserStudiesActivity extends Activity {
 
 	public static final String EXTRA_USER_STUDY_APK_ID = "UserStudyApkId";
 	private UserStudyNotification handleSingleNotificationData;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		//TODO: remove userstudy NOTIFICATION if it still exists in the bar, because this could've been called from the "later" list
 		String studyApkId = getIntent().getExtras().getString(EXTRA_USER_STUDY_APK_ID);
 		if (studyApkId != null) {
 			
 			if (PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance())
 				.getString("username_pref", "").equals("")
 				|| PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance())
 					.getString("password_pref", "").equals("")) {
 				//if either username or password are not set, redirect to moses ui for login
 
 				Intent intent = new Intent(this, MosesActivity.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				intent.putExtra(ViewUserStudiesActivity.EXTRA_USER_STUDY_APK_ID, studyApkId);
 				startActivity(intent);
 				finish();
 			} else {
 				UserStudyNotification notification = UserstudyNotificationManager.getInstance().getNotificationForApkId(
 					studyApkId);
 				showActivityForNotification(notification);
 			}
 			
 		} else {
 			showActivityForNotification(null);
 		}
 	}
 
 	private void showActivityForNotification(UserStudyNotification notification) {
 		if (notification != null) {
 			this.handleSingleNotificationData = notification;
 			requestApkInfo(notification.getApplication().getID());
 		} else {
 			Log.e("MoSeS.Userstudy", "aborting userstudy operation; no data");
 			cancelActivity();
 		}
 	}
 
 	/**
 	 * sets result:cancelled and finishes.
 	 */
 	private void cancelActivity() {
 		setResult(Activity.RESULT_CANCELED);
 		finish();
 	}
 
 	protected void finishActivityOK() {
 		setResult(Activity.RESULT_OK);
 		finish();
 	}
 
 	private void requestApkInfo(final String id) {
 
 		if (MosesService.getInstance() != null) MosesService.getInstance().executeLoggedIn(new Executor() {
 
 			@Override
 			public void execute() {
 				final RequestGetApkInfo r = new RequestGetApkInfo(new ReqTaskExecutor() {
 					@Override
 					public void updateExecution(BackgroundException c) {
 					}
 
 					@Override
 					public void postExecution(String s) {
 						try {
 							JSONObject j = new JSONObject(s);
 							if (RequestGetApkInfo.isInfoRetrived(j)) {
 								String name = j.getString("NAME");
 								String descr = j.getString("DESCR");
 								handleSingleNotificationData.getApplication().setName(name);
 								handleSingleNotificationData.getApplication().setDescription(descr);
 								UserstudyNotificationManager.getInstance().updateNotification(handleSingleNotificationData);
 
 								showDescisionDialog(handleSingleNotificationData);
 							} else {
 								Log.e("MoSeS.UserStudy",
 									"user study info request: Server returned negative" + j.toString());
 								Toast.makeText(getApplicationContext(),
 									"user study info request: Server returned negative" + j.toString(),
 									Toast.LENGTH_LONG).show();
 								cancelActivity();
 								// TODO: handle better but for now...
 							}
 						} catch (JSONException e) {
 							Log.e("MoSeS.UserStudy", "requesting study information: json exception" + e.getMessage());
 							Toast.makeText(getApplicationContext(),
 								"requesting study information: json exception" + e.getMessage(), Toast.LENGTH_LONG)
 								.show();
 							cancelActivity();
 							// TODO: handle better but for now...
 						}
 					}
 
 					@Override
 					public void handleException(Exception e) {
 						Log.e("MoSeS.UserStudy", "couldn't load user study information" + e.getMessage());
 						Toast.makeText(getApplicationContext(),
 							"couldn't load user study information" + e.getMessage(), Toast.LENGTH_LONG).show();
 					}
 				}, id, MosesService.getInstance().getSessionID());
 
 				r.send();
 			}
 		});
 	}
 
 	public void dialogClickLater(View view) {
 		Log.i("MoSeS.Userstudy", "click listener works");
 	}
 
 	protected void showDescisionDialog(final UserStudyNotification notification) {
 		Log.i("MoSeS.Userstudy", notification.getApplication().getID());
 		final Dialog myDialog = new Dialog(this);
 		myDialog.setContentView(R.layout.userstudynotificationdialog);
 		myDialog.setTitle("A new user study \"" + notification.getApplication().getName() + "\" is available for you");
 		((TextView) myDialog.findViewById(R.id.userstudydialog_name)).setText("Name: "
 			+ notification.getApplication().getName());
 		((TextView) myDialog.findViewById(R.id.userstudydialog_descr)).setText(""
 			+ notification.getApplication().getDescription());
 		((Button) myDialog.findViewById(R.id.userstudydialog_btn_yay)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Log.i("MoSes.Userstudy", "starting download process...");
 				installUserstudyApp(notification);
				myDialog.dismiss();
 			}
 		});
 		((Button) myDialog.findViewById(R.id.userstudydialog_btn_nay)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				UserstudyNotificationManager.getInstance().removeNotificationWithApkId(notification.getApplication().getID());
 				myDialog.dismiss();
 				cancelActivity();
 			}
 		});
 		((Button) myDialog.findViewById(R.id.userstudydialog_btn_later)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO: !implement later functionality
 				myDialog.dismiss();
 				cancelActivity();
 			}
 		});
 
 		myDialog.setOwnerActivity(this);
 		myDialog.show();
 
 	}
 
 	protected void installUserstudyApp(UserStudyNotification notification) {
 		final ApkDownloadManager downloader = new ApkDownloadManager(notification.getApplication(),
 			getApplicationContext());
 		Observer observer = new Observer() {
 			@Override
 			public void update(Observable observable, Object data) {
 				if (downloader.getState() == ApkDownloadManager.State.ERROR) {
 					// TODO: error msgs shouldve been already shown, still..
 					// something is to be done here still
 					cancelActivity();
 				} else if (downloader.getState() == ApkDownloadManager.State.FINISHED) {
 					installDownloadedApk(downloader.getDownloadedApk(), downloader.getExternalApplicationResult());
 				}
 			}
 		};
 		downloader.addObserver(observer);
 		downloader.start();
 	}
 
 	private void installDownloadedApk(final File result, final ExternalApplication externalAppRef) {
 		final ApkInstallManager installer = new ApkInstallManager(result, externalAppRef);
 		installer.addObserver(new Observer() {
 			@Override
 			public void update(Observable observable, Object data) {
 				if (installer.getState() == ApkInstallManager.State.ERROR) {
 					// TODO:errors shouldve been shown already by the installer;
 					// still, something is to be done here..
 					cancelActivity();
 				} else if (installer.getState() == ApkInstallManager.State.INSTALLATION_CANCELLED) {
 					// TODO:how to handle if the user cancels the installation?
 					cancelActivity();
 				} else if (installer.getState() == ApkInstallManager.State.INSTALLATION_COMPLETED) {
 					new APKInstalled(externalAppRef.getID());
 					try {
 						ApkInstallManager.registerInstalledApk(result, externalAppRef,
 							ViewUserStudiesActivity.this.getApplicationContext(), true);
 						UserstudyNotificationManager.getInstance().removeNotificationWithApkId(externalAppRef.getID());
 						//TODO: refresh userstudy list?
 					} catch (IOException e) {
 						Log.e(
 							"MoSeS.Install",
 							"Problems with extracting package name from apk, or problems with the InstalledExternalApplicationsManager after installing an app");
 					}
 					finishActivityOK();
 				}
 			}
 		});
 		installer.start();
 	}
 
 	public static String concatStacktrace(Exception e) {
 		String stackTrace = "";
 		for (int i = 0; i < e.getStackTrace().length; i++) {
 			stackTrace += e.getStackTrace()[i];
 		}
 		return stackTrace;
 	}
 
 }
