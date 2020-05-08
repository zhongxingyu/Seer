 package mobi.monaca.framework;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import mobi.monaca.framework.util.MyLog;
 
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
 
 public class MonacaSplashActivity extends Activity {
 	private static final String TAG = MonacaSplashActivity.class.getSimpleName();
     protected static final String SPLASH_IMAGE_PATH = "android/splash_default.png";
     public static final String SHOWS_SPLASH_KEY = "showSplashAtFirst";
 	protected ImageView splashView;
 	protected JSONObject appJson;
 
 	protected Handler handler;
 	protected Runnable pageLauncher;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         loadAppJson();
 
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
 
            overridePendingTransition(mobi.monaca.framework.psedo.R.anim.monaca_none, mobi.monaca.framework.psedo.R.anim.monaca_slide_close_exit);

             handler = new Handler();
             pageLauncher = new Runnable() {
                 @Override
                 public void run() {
                     Intent intent = createActivityIntent();
                     startActivity(intent);
                    finish();
                 }
             };
 
             handler.postDelayed(pageLauncher, 1000);
         } else {
         	goNextActivityWithoutSplash();
         }
     }
 
     protected Intent createActivityIntent() {
 		Intent intent = new Intent(MonacaSplashActivity.this,
                 MonacaPageActivity.class);
 		return intent;
 	}
 
     protected void goNextActivityWithoutSplash() {
         Intent intent = createActivityIntent();
         try {
 			intent.putExtra(SHOWS_SPLASH_KEY, !appJson.getJSONObject("splash").getJSONObject("android").getBoolean("autoHide"));
 		} catch (JSONException e) {
 			MyLog.e(TAG, e.getMessage());
 		}
         startActivity(intent);
         finish();
     }
 
     protected void loadAppJson() {
     	try {
     		InputStream stream = getResources().getAssets().open("app.json");
 			byte[] buffer = new byte[stream.available()];
 			stream.read(buffer);
 			appJson = new JSONObject(new String(buffer,"UTF-8"));
 			return;
 		} catch (IOException e) {
 			MyLog.e(TAG, e.getMessage());
 		} catch (JSONException e) {
 			MyLog.e(TAG, e.getMessage());
 		} catch (IllegalArgumentException e) {
 			MyLog.e(TAG, e.getMessage());
 		}
     	appJson = new JSONObject();
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
 
     @Override
     public void onBackPressed() {
     	if (handler != null && pageLauncher != null) {
     		handler.removeCallbacks(pageLauncher);
     	}
     	super.onBackPressed();
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
