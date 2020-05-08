 package com.github.childphonologylabmcgill.android.alltests.content;
 
 import android.util.Log;
 import android.widget.Toast;
 import ca.ilanguage.oprime.content.JavaScriptInterface;
 
 public class DyslexDysorthJavaScriptInterface extends JavaScriptInterface {
 
 	private static final long serialVersionUID = -8186639717009199855L;
 
 	public DyslexDysorthJavaScriptInterface(boolean d, String tag,
 			String outputDir) {
 		super(d, tag, outputDir);
 	}
 
 	public DyslexDysorthJavaScriptInterface() {
 		super();
 	}
	public void showToast(String toast) {
		Toast.makeText(mContext, toast+" in extended JSI", Toast.LENGTH_LONG).show();
 		if (D)
			Log.d(TAG, "Showing toast " + toast);

 	}
 
 }
