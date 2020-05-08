 package mobi.monaca.framework;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import mobi.monaca.framework.util.MyLog;
 import mobi.monaca.utils.gcm.GCMPushDataset;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 
 import com.google.android.gcm.GCMRegistrar;
 
 public class MonacaSplashActivity extends Activity {
 	private static final String TAG = MonacaSplashActivity.class.getSimpleName();
     protected static final String SPLASH_IMAGE_PATH = "android/splash_default.png";
     public static final String SHOWS_SPLASH_KEY = "showSplashAtFirst";
 	protected ImageView splashView;
 	protected JSONObject appJson;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         loadAppJson();
         registerGCM();
         if (hasSplashScreenExists()) {
             splashView = new ImageView(this);
             splashView.setScaleType(ScaleType.FIT_CENTER);
             InputStream stream = getSplashFileStream();
             splashView.setImageBitmap(BitmapFactory.decodeStream(stream));
 
             splashView.setBackgroundColor(getBackgroundColor());
 
             try {
                 stream.close();
             } catch (Exception e) {
             }
             setContentView(splashView);
 
             new Handler().postDelayed(new Runnable() {
                 @Override
                 public void run() {
                     Intent intent = createActivityIntent();
                     startActivity(intent);
                     new Handler().postDelayed(new Runnable() {
                         @Override
                         public void run() {
                             finish();
                         }
                     }, 500);
                 }
 
             }, 1000);
         } else {
         	goNextActivityWithoutSplash();
         }
     }
 
     protected void loadAppJson() {
     	this.appJson = ((MonacaApplication)getApplication()).getAppJson();
     }
 
     protected void registerGCM() {
 		try {
			String senderId = appJson.getJSONObject("pushNotification").getJSONObject("android").getString("senderId");
 			// GCM registration process
 			GCMRegistrar.checkDevice(this);
 			GCMRegistrar.checkManifest(this);
 			final String regId = GCMRegistrar.getRegistrationId(this);
 			if (regId.equals("")) {
 				GCMRegistrar.register(this, senderId);
 			} else {
 				((MonacaApplication)getApplication()).sendGCMRegisterIdToAppAPI(regId);
 			}
 
 		} catch (Exception e) {
 			MyLog.d(TAG, "this device or application does not support GCM");
 			e.printStackTrace();
 		}
     }
 
     protected Intent createActivityIntent() {
 		Intent intent = new Intent(MonacaSplashActivity.this,
                 MonacaPageActivity.class);
 
     	Intent i = getIntent();
     	Bundle b;
     	if (i != null && (b = i.getExtras()) != null) {
     		GCMPushDataset pushdata = (GCMPushDataset)b.get(GCMPushDataset.KEY);
     		if (pushdata != null) {
         		intent.putExtra(GCMPushDataset.KEY, pushdata);
     		}
     	}
 		return intent;
 	}
 
     protected void goNextActivityWithoutSplash() {
         Intent intent = createActivityIntent();
         try {
 			intent.putExtra(SHOWS_SPLASH_KEY, !appJson.getJSONObject("splash").getJSONObject("android").getBoolean("autoHide"));
 		} catch (JSONException e) {
 			// TODO 自動生成された catch ブロック
 			MyLog.e(TAG, e.getMessage());
 		}
         startActivity(intent);
         finish();
     }
 
     protected int getBackgroundColor() {
 		try {
 			String backgroundColorString = appJson.getJSONObject("splash").getJSONObject("android").getString("background");
 			if(!backgroundColorString.startsWith("#")){
 				backgroundColorString = "#" + backgroundColorString;
 			}
 			int backbroundColor = Color.parseColor(backgroundColorString);
 			return backbroundColor;
 		} catch (JSONException e) {
 			MyLog.e(TAG, e.getMessage());
 		}catch (IllegalArgumentException e) {
 			MyLog.e(TAG, e.getMessage());
 		}
 		return Color.TRANSPARENT;
 	}
 
     protected boolean hasSplashScreenExists() {
     	try {
     		MyLog.d(TAG, "autoHide is :" + Boolean.toString(appJson.getJSONObject("splash").getJSONObject("android").getBoolean("autoHide")));
     		if (!appJson.getJSONObject("splash").getJSONObject("android").getBoolean("autoHide")) {
     			return false;
     		}
     	} catch (JSONException e) {
 		}
 
         try {
             InputStream stream = getResources().getAssets().open(
                     SPLASH_IMAGE_PATH);
             stream.close();
             return true;
         } catch (IOException e) {
             return false;
         }
     }
 
     protected InputStream getSplashFileStream() {
         try {
             return getResources().getAssets().open(SPLASH_IMAGE_PATH);
         } catch (IOException e) {
             return null;
         }
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
     }
 
 }
