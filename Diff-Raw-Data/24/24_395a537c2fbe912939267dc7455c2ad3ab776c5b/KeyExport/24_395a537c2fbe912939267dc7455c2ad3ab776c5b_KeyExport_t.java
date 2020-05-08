 package de.pentabarf.cryptmessaging;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
import android.os.Binder;
 import android.os.Bundle;
 import android.os.Environment;
import android.os.Process;
 import android.util.Log;
 import android.widget.Toast;
 
 public class KeyExport extends Activity {
	private static final String KEYCHAIN_PACKAGE = "com.android.keychain";
 	private String TAG = "KeyExport";
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Uri internalUri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
		
		if (internalUri == null) {
		    	finish();
		    	return;
		}
		
//		this does not seem to work, I'll work on this as soon as I put this code in the CertInstaller 
//		if (!KEYCHAIN_PACKAGE.equals(getCallingPackage())) {
//		    	Log.e(TAG, "user " + getCallingPackage() + " not allowed to export files");
//		    	setResult(RESULT_CANCELED);
//		    	finish();
//			return;
//		}
		Log.i(TAG, "user is clear to export");
 
 		InputStream inStream = null;
 		OutputStream outStream = null;
 
 		try{
 
 			File keyFile = new File(internalUri.getPath());
 
 			File expKeyFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + keyFile.getName());
 
 			if(expKeyFile.exists())
 				Toast.makeText(this, "Key already exported",
 						Toast.LENGTH_SHORT).show();
 			else {
 				Log.d(TAG, "Key size: " + keyFile.length());
 
 				inStream = new FileInputStream(keyFile);
 				outStream = new FileOutputStream(expKeyFile);
 
 				byte[] buffer = new byte[(int) keyFile.length()];
 
 				int length;
 				while ((length = inStream.read(buffer)) > 0)
 					outStream.write(buffer, 0, length);
 
 				inStream.close();
 				outStream.close();
 
 				//delete the extracted key
 				keyFile.delete();
 
 				Toast.makeText(this, "Key successfully exported",
 						Toast.LENGTH_SHORT).show();
 			}
 		}catch(IOException e){
		    Log.e(TAG, "exception while exporting key", e);
			Toast.makeText(this, "Key not exported: "+ e.getLocalizedMessage(),
 					Toast.LENGTH_SHORT).show();
 		}
 
 
 		finish();
 	}
 
 
 }
