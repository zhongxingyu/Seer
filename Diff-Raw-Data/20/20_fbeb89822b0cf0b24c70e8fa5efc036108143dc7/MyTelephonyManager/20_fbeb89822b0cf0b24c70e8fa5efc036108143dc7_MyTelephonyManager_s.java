 package pl.tabhero.utils;
 
 import pl.tabhero.R;
 import pl.tabhero.TabHero;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 import android.telephony.TelephonyManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 
 @SuppressLint("NewApi")
 public class MyTelephonyManager {
 	private Context context;
 	private Activity activity;
 	
 	public MyTelephonyManager(Context context) {
 		this.context = context;
 		this.activity = (Activity) context;
 	}
 
 	public boolean isTablet() {
 		boolean isTablet;
 		TelephonyManager manager = (TelephonyManager)this.context.getSystemService(Context.TELEPHONY_SERVICE);
         if(manager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE){
             isTablet = true;
         } else {
             isTablet = false;
         }
         return isTablet;
 	}
 	
 	public void goHomeScreen() {
 		Intent intent = new Intent(this.context, TabHero.class);
     	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
     	this.activity.finish();
     	this.context.startActivity(intent);
     	this.activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
 	}
 	
 	public void setHomeButtonEnabledForICS() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
             this.activity.getActionBar().setHomeButtonEnabled(true);
         }
 	}
 	
 	public void hideKeyboard(EditText editText) {
 		InputMethodManager imm = (InputMethodManager)this.context.getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
 	}
 }
