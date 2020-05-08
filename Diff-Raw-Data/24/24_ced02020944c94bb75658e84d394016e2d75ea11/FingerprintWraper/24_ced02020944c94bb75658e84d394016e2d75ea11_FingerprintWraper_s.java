 package cc.omusic.FingerprintDecodeJin;
 
 import java.io.File;
 
 import android.util.Log;
 
 
 public class FingerprintWraper {
 	String TAG = "FingerprintWraper";
 	public native byte[] getFingerprint( String filestring);
 	
     /* this is used to load the 'hello-jni' library on application
      * startup. The library has already been unpacked into
      * /data/data/com.example.hellojni/lib/libhello-jni.so at
      * installation time by the package manager.
      */
 	
 	
	
 	static 
 	{	
         try{
 			//shared lib called libfingerprint-jin.so
         	Log.i("lib","try to load library : FingerprintJin.so ");
         	
         	System.loadLibrary("FingerprintJin");
         	
 		}catch( UnsatisfiedLinkError use ){
 			Log.e("lib", "could not load native library FingerprintJin.so");
 		}
 	}
 	
 	
 	public byte[]generate(String filestring)
 	{
 		Log.d("lib", "Use a new method to call native method");
 		byte[] fingerprint = getFingerprint(filestring );
 		return fingerprint;
 				
 	}
 	
 	
 }
