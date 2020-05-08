 /*
  * 無所不在學習架構與學習導引機制
  * A Hybrid Ubiquitous Learning Framework and its Navigation Support Mechanism
  * 
  * FileName:	UserLoginActivity.java
  *
  * Description: 程式進入點，這是使用者的登入畫面
  * 
  */
 package tw.edu.chu.csie.e_learning.ui;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.HttpHostConnectException;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.DefaultClientConnection;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import tw.edu.chu.csie.e_learning.R;
 import tw.edu.chu.csie.e_learning.R.id;
 import tw.edu.chu.csie.e_learning.R.layout;
 import tw.edu.chu.csie.e_learning.R.menu;
 import tw.edu.chu.csie.e_learning.R.string;
 import tw.edu.chu.csie.e_learning.config.Config;
 import tw.edu.chu.csie.e_learning.provider.ClientDBProvider;
 import tw.edu.chu.csie.e_learning.server.exception.HttpException;
 import tw.edu.chu.csie.e_learning.server.exception.LoginCodeException;
 import tw.edu.chu.csie.e_learning.server.exception.LoginException;
 import tw.edu.chu.csie.e_learning.server.exception.PostNotSameException;
 import tw.edu.chu.csie.e_learning.server.exception.ServerException;
 import tw.edu.chu.csie.e_learning.util.AccountUtils;
 import tw.edu.chu.csie.e_learning.util.HelpUtils;
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Activity which displays a login screen to the user, offering registration as
  * well.
  */
 @SuppressWarnings("unused")
 public class UserLoginActivity extends Activity {
 	/**
 	 * A dummy authentication store containing known user names and passwords.
 	 * TODO: remove after connecting to a real authentication system.
 	 */
 	private static final String[] DUMMY_CREDENTIALS = new String[] {
 			"foo@example.com:hello", "bar@example.com:world" };
 
 	/**
 	 * The default email to populate the email field with.
 	 */
 	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";
 
 	/**
 	 * Keep track of the login task to ensure we can cancel it if requested.
 	 */
 	private UserLoginTask mAuthTask = null;
 
 	// Values for email and password at the time of the login attempt.
 	private String mId;
 	private String mPassword;
 
 	// UI references.
 	private EditText mIdView;
 	private EditText mPasswordView;
 	private View mLoginFormView;
 	private View mLoginStatusView;
 	private TextView mLoginStatusMessageView;
 	private TextView mLoginErrMsgView;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_user_login);
 
 		// Set up the login form.
 		mId = getIntent().getStringExtra(EXTRA_EMAIL);
 		mIdView = (EditText) findViewById(R.id.id);
 		mIdView.setText(mId);
 
 		mPasswordView = (EditText) findViewById(R.id.password);
 		mPasswordView
 				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 					@Override
 					public boolean onEditorAction(TextView textView, int id,
 							KeyEvent keyEvent) {
 						if (id == R.id.login || id == EditorInfo.IME_NULL) {
 							attemptLogin();
 							return true;
 						}
 						return false;
 					}
 				});
 
 		mLoginFormView = findViewById(R.id.login_form);
 		mLoginStatusView = findViewById(R.id.login_status);
 		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
 		mLoginErrMsgView = (TextView) findViewById(R.id.login_error_msg);
 		findViewById(R.id.login_in_button).setOnClickListener(
 				new View.OnClickListener() {
 					@Override
 					public void onClick(View view) {
 						attemptLogin();
 					}
 				});
 		
 		//自動填入預設帳號密碼
 		if(Config.AUTO_FILL_LOGIN){
 			mIdView.setText(Config.DEFAULT_LOGIN_ID);
 			mPasswordView.setText(Config.DEFAULT_LOGIN_PASSWORD);
 		}
 		//自動登入
 		if(Config.AUTO_NO_ID_LOGIN) attemptLogin();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		getMenuInflater().inflate(R.menu.user_login, menu);
 		return true;
 	}
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch(item.getItemId()){
 		case R.id.menu_about:
 			HelpUtils.showAboutDialog(this);
 			break;
 		case R.id.menu_material_downloader:
 			Intent toTextbookDownloader = new Intent(UserLoginActivity.this, MaterialDownloaderActivity.class);
 			startActivity(toTextbookDownloader);
 			break;
 		}
 		
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	/**
 	 * 驗證是否輸入正確，若無誤就登入
 	 * 
 	 * Attempts to sign in or register the account specified by the login form.
 	 * If there are form errors (invalid email, missing fields, etc.), the
 	 * errors are presented and no actual login attempt is made.
 	 */
 	public void attemptLogin() {
 		if (mAuthTask != null) {
 			return;
 		}
 
 		// Reset errors.
 		mIdView.setError(null);
 		mPasswordView.setError(null);
 
 		// Store values at the time of the login attempt.
 		mId = mIdView.getText().toString();
 		mPassword = mPasswordView.getText().toString();
 
 		boolean cancel = false;
 		View focusView = null;
 
 		// Check for a valid password.
 		if (TextUtils.isEmpty(mPassword)) {
 			mPasswordView.setError(getString(R.string.error_field_required));
 			focusView = mPasswordView;
 			cancel = true;
 		} else if (mPassword.length() < 4) {
 			mPasswordView.setError(getString(R.string.error_invalid_password));
 			focusView = mPasswordView;
 			cancel = true;
 		}
 
 		// Check for a valid email address.
 		if (TextUtils.isEmpty(mId)) {
 			mIdView.setError(getString(R.string.error_field_required));
 			focusView = mIdView;
 			cancel = true;
 		} 
 		if (cancel) {
 			// There was an error; don't attempt login and focus the first
 			// form field with an error.
 			focusView.requestFocus();
 		} else {
 			// Show a progress spinner, and kick off a background task to
 			// perform the user login attempt.
 			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
 			showProgress(true);
 			mAuthTask = new UserLoginTask();
 			mAuthTask.execute(mId,mPassword);
 		}
 	}
 
 	/**
 	 * 顯示登入中畫面
 	 * 
 	 * Shows the progress UI and hides the login form.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 	private void showProgress(final boolean show) {
 		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
 		// for very easy animations. If available, use these APIs to fade-in
 		// the progress spinner.
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 			int shortAnimTime = getResources().getInteger(
 					android.R.integer.config_shortAnimTime);
 
 			mLoginStatusView.setVisibility(View.VISIBLE);
 			mLoginStatusView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 1 : 0)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mLoginStatusView.setVisibility(show ? View.VISIBLE
 									: View.GONE);
 						}
 					});
 
 			mLoginFormView.setVisibility(View.VISIBLE);
 			mLoginFormView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 0 : 1)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mLoginFormView.setVisibility(show ? View.GONE
 									: View.VISIBLE);
 						}
 					});
 		} else {
 			// The ViewPropertyAnimator APIs are not available, so simply show
 			// and hide the relevant UI components.
 			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
 			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
 		}
 	}
 
 	/**
 	 * 進行登入的動作
 	 * 
 	 * Represents an asynchronous login/registration task used to authenticate
 	 * the user.
 	 */
 	public class UserLoginTask extends AsyncTask<String, Void, Boolean> 
 	{
 		Bundle bundle = new Bundle();
 		
 		private AccountUtils check = new AccountUtils();
 		@Override
 		protected Boolean doInBackground(String... params) {
 			// DEBUG 當啟用無連線登入，進入學習畫面
 			if(Config.DEBUG_NO_CONNECT_LOGIN) {
 				return true;
 			}
 			else {
 				try {
 					check.loginUser(params[0], params[1]);
 				} catch (ClientProtocolException e) {
 					bundle.putString("exception", "ClientProtocolException");
 					bundle.putString("exception-msg", e.getMessage());
 					e.printStackTrace();
 				} catch (IOException e) {
 					bundle.putString("exception", "IOException");
 					bundle.putString("exception-msg", e.getMessage());
 					e.printStackTrace();
 				} catch (JSONException e) {
 					bundle.putString("exception", "JSONException");
 					bundle.putString("exception-msg", e.getMessage());
 					e.printStackTrace();
 				} catch (LoginException e) {
 					// TODO Auto-generated catch block
 					bundle.putString("exception", "LoginException");
 					bundle.putString("exception-msg", e.getMessage());
 					bundle.putInt("LoginException-ID", e.getID());
 					e.printStackTrace();
 				} catch (PostNotSameException e) {
 					// TODO Auto-generated catch block
 					bundle.putString("exception", "PostNotSameException");
 					bundle.putString("exception-msg", e.getMessage());
 					e.printStackTrace();
 				} catch (HttpException e) {
 					// TODO Auto-generated catch block
 					bundle.putString("exception", "HttpException");
 					bundle.putString("exception-msg", e.getMessage());
 					bundle.putInt("HttpException-ID", e.getStatusCode());
 					Log.d(null, Integer.toString(e.getStatusCode()));
 					e.printStackTrace();
 				} catch (ServerException e) {
 					bundle.putString("exception", "ServerException");
 					bundle.putString("exception-msg", e.getMessage());
 					bundle.putInt("ServerException-ID", e.getID());
 					e.printStackTrace();
 				} catch (LoginCodeException e) {
 					// TODO Auto-generated catch block
 					bundle.putString("exception", "LoginCodeException");
 					bundle.putString("exception-msg", e.getMessage());
 				}
 				if(check.islogin()) return true;
 				else return false;
 			}
 		}
 
 		@Override
 		protected void onPostExecute(final Boolean success) {
 			mAuthTask = null;
 			showProgress(false);
 
 			if (success) {
 				Intent toLogin = new Intent(UserLoginActivity.this, MainFunctionActivity.class);
 				startActivity(toLogin);
 				//finish();
 			} else {
 				String failKind = bundle.getString("exception");
 				
 				if(failKind == "LoginException") {
 					switch(bundle.getInt("LoginException-ID")) {
 					// 無此使用者
 					case LoginException.NO_FOUND:
 						mIdView.setError(getString(R.string.error_user_no_found));
 						mIdView.requestFocus();
 					// 密碼錯誤
 					case LoginException.PASSWORD_ERROR:
 						mPasswordView
 						.setError(getString(R.string.error_incorrect_password));
 						mPasswordView.requestFocus();
 						break;
 					// 登入的帳號已停用
 					case LoginException.NO_ACTIVE:
 						mIdView.setError(getString(R.string.error_user_no_active));
 						mIdView.requestFocus();
 						break;
 					// 其他錯誤
 					default:
 						mLoginErrMsgView.setText(getString(R.string.other_error));
 					}
 				}
 				else if(failKind == "PostNotSameException") {
 					mLoginErrMsgView.setText(getString(R.string.server_receive_not_same));
 				}
 				else if(failKind == "HttpException") {
 					switch(bundle.getInt("HttpException-ID")) {
 					case 404:
 						mLoginErrMsgView.setText(getString(R.string.server_404));
 					default:
 						mLoginErrMsgView.setText(getString(R.string.server_connect_fail));
 					}
 				}
 				else if(failKind == "ClientProtocolException") {
 					mLoginErrMsgView.setText(getString(R.string.server_connect_fail));
 				}
 				else {
 					mLoginErrMsgView.setText(getString(R.string.other_error));
 				}
 				
 				if(Config.DEBUG_SHOW_MESSAGE) {
 					Toast.makeText(getBaseContext(), bundle.getString("exception-msg"), Toast.LENGTH_SHORT).show();						
 				}
 				
 				
 			}
 		}
 
 		@Override
 		protected void onCancelled() {
 			mAuthTask = null;
 			showProgress(false);
 		}
 	}
 }
