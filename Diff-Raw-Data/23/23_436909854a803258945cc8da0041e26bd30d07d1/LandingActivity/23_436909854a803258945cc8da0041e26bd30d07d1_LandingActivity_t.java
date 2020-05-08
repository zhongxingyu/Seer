 package cc.rainwave.android;
 
 import java.io.IOException;
import java.net.HttpURLConnection;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.PixelFormat;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.EditText;
 import cc.rainwave.android.api.Session;
 import cc.rainwave.android.api.types.RainwaveException;
 
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 
 /**
  * First thing the user sees when starting the app.
  * Provides an easier prompt for getting identified.
  * @author pkilgo
  *
  */
 public class LandingActivity extends Activity {
 	
 	private VerifyCredentials mVerifyCredentialsTask;
 	
     private Session mSession;
 
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		preLayout();
 		setContentView(R.layout.activity_landing);
 		postLayout();
 	}
 	
 	public void onActivityResult(int request, int result, Intent data) {
 		IntentResult ir = IntentIntegrator.parseActivityResult(request, result, data);
 		if(ir == null) return;
 		
 		String raw = ir.getContents();
 		if(raw == null) return;
 		
 		Uri uri = Uri.parse(raw);
 		String userInfo = uri.getUserInfo();
 		String user = Rainwave.extractUserId(userInfo);
 		String key = Rainwave.extractKey(userInfo);
 		
 		((EditText)findViewById(R.id.land_userId)).setText(user);
 		((EditText)findViewById(R.id.land_apiKey)).setText(key);
 	}
 	
 	private void postLayout() {
 		findViewById(R.id.land_login).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				String user = ((EditText)findViewById(R.id.land_userId)).getText().toString();
 				String key = ((EditText)findViewById(R.id.land_apiKey)).getText().toString();
 				
 				if(user != null && user.length() > 0 && key != null & key.length() > 0) {
 					verifyUserInfo(user,key);
 				}
 				else {
 					Rainwave.showError(LandingActivity.this, R.string.msg_nullFieldError);
 				}
 			}
 		});
 		
 		findViewById(R.id.land_later).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				startNowPlaying();
 			}
 		});
 		
 		findViewById(R.id.land_never).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Rainwave.setSkipLanding(LandingActivity.this, true);
 				startNowPlaying();
 			}
 		});
 		
 		findViewById(R.id.land_qrButton).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				IntentIntegrator.initiateScan(LandingActivity.this);
 			}
 		});
 	}
 
 	private void preLayout() {
 		if(Rainwave.hasUserInfo(this) || Rainwave.skipLanding(this)) {
 			startNowPlaying();
 		}
 		
 		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		mSession = Session.getInstance();
 		mSession.unpickle(this);
 	}
 	
 	private void verifyUserInfo(String user, String key) {
 		if(mVerifyCredentialsTask == null) {
 			mVerifyCredentialsTask = new VerifyCredentials();
 			mVerifyCredentialsTask.execute(user,key);
 		}
 	}
 	
 	private void setInfoAndStart(String user, String key) {
 		Rainwave.putUserId(this, user);
 		Rainwave.putKey(this, key);
 		startNowPlaying();
 	}
 	
 	private void startNowPlaying() {
 		Intent i = new Intent(this, NowPlayingActivity.class);
 		startActivity(i);
 		finish();
 	}
 
 	@Override
 	public void onAttachedToWindow() {
 	    super.onAttachedToWindow();
 	    Window window = getWindow();
 	    window.setFormat(PixelFormat.RGBA_8888);
 	}
 	
 	 /**
      * Fetches the now playing info.
      * Expects one argument to <code>execute(Object...params)</code> which
      * is the flag to indicate if this is an initializing (e.g., non-longpoll)
      * fetch of the schedule data.
      * @author pkilgo
      *
      */
     protected class VerifyCredentials extends AsyncTask<String, Integer, Boolean> {
         private String TAG = "VerifyCredentials";
 
         private String mUser, mKey;
         
         @Override
         protected Boolean doInBackground(String ... args) {
             mUser = args[0];
             mKey = args[1];
             
             mSession.setUserInfo(mUser, mKey);
             dispatchThrobberVisibility(true);
             
             try {
             	mSession.info();
                 return true;
             } catch (RainwaveException e) {
            	switch(e.getStatusCode()) {
            	case HttpURLConnection.HTTP_FORBIDDEN:
            		Rainwave.showError(LandingActivity.this, R.string.msg_authenticationFailure);
            		break;
            	default:
            		Rainwave.showError(LandingActivity.this, e);
            		break;
            	}
             	return false;
             }
         }
         
         protected void onPostExecute(Boolean result) {
             super.onPostExecute(result);
             dispatchThrobberVisibility(false);
             mVerifyCredentialsTask = null;
             
             if(!result || !mSession.isAuthenticated()) {
            	mSession.clearUserInfo();
             	return;
             }
             
             setInfoAndStart(mUser, mKey);
         }
     }
     
     private void dispatchThrobberVisibility(boolean state) {
     	Message msg = mHandler.obtainMessage(HANDLER_SET_INDETERMINATE);
     	Bundle data = msg.getData();
     	data.putBoolean("bool", state);
     	msg.sendToTarget();
     }
     
     private Handler mHandler = new Handler() {
     	public void handleMessage(Message msg) {
     		Bundle data = msg.getData();
     		switch(msg.what) {
     		case HANDLER_SET_INDETERMINATE:
     			setProgressBarIndeterminateVisibility( data.getBoolean("value") );
     			break;
     		}
     	}
     };
     
     public static final int
     	HANDLER_SET_INDETERMINATE = 0x1D373;
 }
