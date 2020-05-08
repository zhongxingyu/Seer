 package net.qlun.ab1;
 
 import jp.mixi.android.sdk.CallbackListener;
 import jp.mixi.android.sdk.Config;
 import jp.mixi.android.sdk.ErrorInfo;
 import jp.mixi.android.sdk.MixiContainer;
 import jp.mixi.android.sdk.MixiContainerFactory;
 import net.qlun.ab1.task.DownloadTask;
 import net.qlun.ab1.task.UnZipTask;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TestActivity extends Activity implements OnClickListener {
 
 	private static final String TAG = "ab1";
 
 	private static final int AUTHORIZE_REQUEST_CODE = 30960;
 
 	private ProgressBar progressBar;
 
 	private Button buttonDownload;
 	private Button buttonView;
 
 	private TextView loginStatusText;
 
 	private MixiContainer mContainer;
 
 	@Override
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		setContentView(R.layout.test);
 
 		progressBar = (ProgressBar) findViewById(R.id.progress1);
 
 		buttonDownload = (Button) findViewById(R.id.buttonDownload);
 		buttonDownload.setOnClickListener(this);
 		buttonView = (Button) findViewById(R.id.buttonView);
 		buttonView.setOnClickListener(this);
 
 		Button buttonLogin = (Button) findViewById(R.id.buttonLogin);
 		buttonLogin.setOnClickListener(this);
 		Button buttonLogout = (Button) findViewById(R.id.buttonLogout);
 		buttonLogout.setOnClickListener(this);
 
 		loginStatusText = (TextView) findViewById(R.id.textMixiUser);
 
 		Button buttonPreference = (Button) findViewById(R.id.buttonPreference);
 		buttonPreference.setOnClickListener(this);
 
 		Config config = new Config();
 		config.clientId = "mixiapp_30960";
 		config.selector = Config.GRAPH_API;
 
 		mContainer = MixiContainerFactory.getContainer(config);
 		mContainer.init(this);
 
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		Log.v(TAG, "activityResult: " + requestCode + ", " + resultCode + ","
 				+ data);
 		mContainer.authorizeCallback(requestCode, resultCode, data);
 	}
 
 	@Override
 	public void onDestroy() {
 		mContainer.close(this);
 		super.onDestroy();
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 
 		SharedPreferences sp = PreferenceManager
 				.getDefaultSharedPreferences(this);
 
 		String indexUrl = sp.getString("indexUrl", null);
 		if (indexUrl != null) {
 			TextView indexUrlView = (TextView) findViewById(R.id.indexUrl);
 			indexUrlView.setText(indexUrl);
 		}
 
 		String packageUrl = sp.getString("packageUrl", null);
 		if (packageUrl != null) {
 			TextView packageUrlView = (TextView) findViewById(R.id.packageUrl);
 			packageUrlView.setText(packageUrl);
 		}
 	}
 
 	protected void mixiLogin() {
 
 		loginStatusText.setText("login...");
 
 		mContainer.authorize(this, new String[] { "mixi_apps" },
 				AUTHORIZE_REQUEST_CODE, new CallbackListener() {
 
 					@Override
 					public void onComplete(Bundle values) {
 						Log.v(TAG, "authorize complete: " + values);
 						Log.v(TAG, "accessToken: " + mContainer.getAccessToken());
 
 						alert("Mixi login complete.");
 
 						loginStatusText.setText("login complete...");
 
 						mixiWhoAmI();
 					}
 
 					@Override
 					public void onCancel() {
 						Log.v(TAG, "authorize cancel.");
 
 						loginStatusText.setText("login cancel");
 					}
 
 					@Override
 					public void onFatal(ErrorInfo e) {
 						Log.v(TAG, "authorize fatal: " + e.getErrorCode()
 								+ ", " + e.getMessage());
 
 						alert("Mixi login Fatal: " + e.getMessage());
 						loginStatusText.setText("login fatal");
 					}
 
 					@Override
 					public void onError(ErrorInfo e) {
 						Log.v(TAG, "authorize error: " + e.getErrorCode()
 								+ ", " + e.getMessage());
 						alert("Mixi login Error: " + e.getMessage());
 						loginStatusText.setText("login error");
 					}
 
 				});
 	}
 
 	protected void mixiLogout() {
 		loginStatusText.setText("logout...");
 
 		mContainer.logout(this, AUTHORIZE_REQUEST_CODE, new CallbackListener() {
 
 			@Override
 			public void onComplete(Bundle values) {
 				Log.v(TAG, "logout complete: " + values);
 
 				alert("Mixi logout complete.");
 				loginStatusText.setText("logout complete");
 			}
 
 			@Override
 			public void onCancel() {
 				Log.v(TAG, "logout cancel.");
 
 				alert("Mixi logout cancel.");
 				loginStatusText.setText("logout cancel");
 			}
 
 			@Override
 			public void onFatal(ErrorInfo e) {
 				Log.v(TAG,
 						"logout fatal: " + e.getErrorCode() + ", "
 								+ e.getMessage());
 
 				alert("Mixi logout Fatal: " + e.getMessage());
 				loginStatusText.setText("logout fatal: " + e.getErrorCode());
 			}
 
 			@Override
 			public void onError(ErrorInfo e) {
 				Log.v(TAG,
 						"logout error: " + e.getErrorCode() + ", "
 								+ e.getMessage());
 				alert("Mixi logout Error: " + e.getMessage());
 				loginStatusText.setText("login error: " + e.getErrorCode());
 			}
 
 		});
 	}
 
 	protected void mixiWhoAmI() {
 
 		mContainer.send("/people/@me/@self", new CallbackListener() {
 
 			@Override
 			public void onComplete(Bundle values) {
 
 				Log.v(TAG, "whoami: " + values);
 				String response = values.getString("response");
 				Log.v(TAG, response);
 
 				String displayName = null;
 				try {
 					JSONObject resp = (JSONObject) new JSONTokener(response)
 							.nextValue();
 					JSONObject entry = resp.getJSONObject("entry");
 					displayName = entry.getString("displayName");
 
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 
 					displayName = "/Json Error/.";
 				}
 				loginStatusText.setText(displayName);
 			}
 
 			@Override
 			public void onCancel() {
 				Log.v(TAG, "whoami cancel.");
 			}
 
 			@Override
 			public void onFatal(ErrorInfo e) {
 				Log.v(TAG,
 						"whoami fatal: " + e.getErrorCode() + ", "
 								+ e.getMessage());
 			}
 
 			@Override
 			public void onError(ErrorInfo e) {
 				Log.v(TAG,
 						"whoami error: " + e.getErrorCode() + ", "
 								+ e.getMessage());
 
 			}
 		});
 
 	}
 
 	protected void alert(String message) {
 
 		new AlertDialog.Builder(this)
 				.setIcon(android.R.drawable.ic_dialog_alert).setTitle("...")
 				.setMessage(message).setPositiveButton(R.string.yes, null)
 				.show();
 
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			// Ask the user if they want to quit
 			new AlertDialog.Builder(this)
 					.setIcon(android.R.drawable.ic_dialog_alert)
 					.setTitle(R.string.quit)
 					.setMessage(R.string.really_quit)
 					.setPositiveButton(R.string.yes,
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 
 									// Stop the activity
 									TestActivity.this.finish();
 								}
 
 							}).setNegativeButton(R.string.no, null).show();
 
 			return true;
 		} else {
 			return super.onKeyDown(keyCode, event);
 		}
 
 	}
 
 	@Override
 	public void onClick(View view) {
 		switch (view.getId()) {
 		case R.id.buttonDownload:
 			Log.d("ab1", "buttonDownload click.");
 			{
 				SharedPreferences sp = PreferenceManager
 						.getDefaultSharedPreferences(this);
 
 				String packageUrl = sp.getString("packageUrl", null);
 				if (packageUrl != null) {
 
 					// disable button1 now
 					buttonDownload.setEnabled(false);
 
 					download(packageUrl);
 				} else {
 
 					Toast.makeText(TestActivity.this, "null packageUrl",
 							Toast.LENGTH_SHORT).show();
 
 				}
 
 			}
 			break;
 		case R.id.buttonView:
 			Log.d("ab1", "buttonView click.");
 			{
 				SharedPreferences sp = PreferenceManager
 						.getDefaultSharedPreferences(this);
 
 				String indexUrl = sp.getString("indexUrl", null);
 				if (indexUrl != null) {
 
 					Intent intent = new Intent(this, GameWebActivity.class);
 					intent.putExtra("AccessToken", mContainer.getAccessToken());
 					startActivity(intent);
 				} else {
 
 					Toast.makeText(TestActivity.this, "null indexUrl",
 							Toast.LENGTH_SHORT).show();
 				}
 			}
 			break;
 		case R.id.buttonPreference:
 			Log.d("ab1", "buttonPreference click.");
 			{
 				Intent intent = new Intent(this, SettingActivity.class);
 				startActivity(intent);
 			}
 			break;
 		case R.id.buttonLogin:
 			Log.d("ab1", "buttonLogin click.");
 			{
 				mixiLogin();
 			}
 			break;
 		case R.id.buttonLogout:
 			Log.d("ab1", "buttonLogout click.");
 			{
 				mixiLogout();
 			}
 			break;
 		default:
 			Log.d("ab1", "unknown view.");
 			break;
 		}
 	}
 
 	private void download(String url) {
 		Log.i("ab1", "download " + url);
 
 		TDownloadTask downloadFile = new TDownloadTask(this);
 		downloadFile.execute(url, "download.temp");
 
 	}
 
 	private void unzipWebFile(String filename) {
 		Log.i("ab1", "unzip " + filename);
 
 		String unzipLocation = getFilesDir().getAbsolutePath() + "/web";
 		String zipfile = getFilesDir().getAbsolutePath() + "/" + filename;
 
 		TUnZipTask unzip = new TUnZipTask();
 		unzip.execute(zipfile, unzipLocation);
 
 	}
 
 	private class TDownloadTask extends DownloadTask {
 
 		public TDownloadTask(Context c) {
 			super(c);
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... progress) {
 			progressBar.setProgress(progress[0]);
 
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 
 			Log.v("ab1", "download result: " + result);
 			if (result) {
 				Toast.makeText(TestActivity.this, "download completed.",
 						Toast.LENGTH_SHORT).show();
 
 				unzipWebFile("download.temp");
 
 			} else {
 				Toast.makeText(TestActivity.this, "download failed.",
 						Toast.LENGTH_SHORT).show();
 
 				// enable button
 				buttonDownload.setEnabled(true);
 			}
 
 		}
 
 	}
 
 	private class TUnZipTask extends UnZipTask {
 
 		@Override
 		protected void onProgressUpdate(Integer... progress) {
 			progressBar.setProgress(progress[0]);
 
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 
 			if (result) {
 				Toast.makeText(TestActivity.this, "unpack completed.",
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(TestActivity.this, "unpack failed.",
 						Toast.LENGTH_SHORT).show();
 			}
 
 			// enable button
 			buttonDownload.setEnabled(true);
 		}
 
 	}
 
 }
