 package co.mwater.clientapp;
 
 import org.apache.cordova.CordovaChromeClient;
 import org.apache.cordova.CordovaWebView;
 import org.apache.cordova.api.CordovaInterface;
 
 import android.util.Log;
 import android.webkit.ConsoleMessage;
 import android.webkit.ConsoleMessage.MessageLevel;
import android.webkit.GeolocationPermissions;
 
 public class SpecialChromeClient extends CordovaChromeClient {
     private String TAG = "CordovaLog2";
     CordovaInterface cordova;
     CordovaWebView app;
 
 	public SpecialChromeClient(CordovaInterface cordova, CordovaWebView app) {
 		super(cordova, app);
 		this.cordova = cordova;
 		this.app = app;
 	}
 
 	static String cleanParam(String p) {
 		return p.replace("\"", "\\\"").replace("\n","\\n").replace("\r","\\r");
 	}
 	
 	@Override
 	public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
 		// If uncaught exception, write to window.onerror
 		if (consoleMessage != null && consoleMessage.messageLevel() == MessageLevel.ERROR) {
 			if (consoleMessage.message().startsWith("Uncaught ")) {
 				String javascript = "if (window.onerror) window.onerror(\"" 
 						+ cleanParam(consoleMessage.message()) + "\", \""
 						+ cleanParam(consoleMessage.sourceId()) + "\", "
 						+ consoleMessage.lineNumber() + ");";
 				Log.e(TAG, javascript);
 				app.sendJavascript(javascript);
 				return true;
 			}
 		}
 		
 		return super.onConsoleMessage(consoleMessage);
 	}

	/**
	 * Always allow Geolocation
	 */
	@Override
	public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        callback.invoke(origin, true, false);
    }
 }
