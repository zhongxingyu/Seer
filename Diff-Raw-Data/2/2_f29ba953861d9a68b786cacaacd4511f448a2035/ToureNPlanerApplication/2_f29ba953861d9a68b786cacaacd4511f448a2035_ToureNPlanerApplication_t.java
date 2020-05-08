 package de.uni.stuttgart.informatik.ToureNPlaner;
 
 import android.app.Application;
 import android.content.Context;
 import android.os.Build;
 import android.util.Log;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;
 
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManagerFactory;
 import java.io.File;
 import java.security.KeyStore;
 import java.util.Date;
 
 public class ToureNPlanerApplication extends Application {
 	private static Context context;
 	private static KeyStore keyStore;
 	private static SSLContext sslContext;
 
 	public static SSLContext getSslContext() {
        if(sslContext == null)
            setupSsl();
 		return sslContext;
 	}
 
 	public static void setupSsl() {
         TrustManagerFactory tmf;
 		try {
 			sslContext = SSLContext.getInstance("TLS");
             keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
             tmf = TrustManagerFactory.getInstance("X509");
         } catch (Exception e) {
             Log.e("TP", "SSL", e);
             return;
         }
         try {
 			keyStore.load(ToureNPlanerApplication.context.openFileInput("keystore"), null);
         } catch (Exception e) {
             Log.e("TP", "SSL", e);
             keyStore = null;
         }
         try {
             tmf.init(keyStore);
 			sslContext.init(null, tmf.getTrustManagers(), null);
 		} catch (Exception e) {
 			Log.e("TP", "SSL", e);
 		}
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		context = getApplicationContext();
 		disableConnectionReuseIfNecessary();
 		new Thread() {
 			@Override
 			public void run() {
 				performCleanUp();
 			}
 		}.start();
 	}
 
 	public static Context getContext() {
 		return context;
 	}
 
 	/**
 	 * Cleans up old Session Files
 	 */
 	private static void performCleanUp() {
 		File[] files = Session.openCacheDir().listFiles();
 
 		if (files == null)
 			return;
 
 		long currentTime = new Date().getTime();
 
 		for (File file : files) {
 			// If file is older than a day
 			if (currentTime - file.lastModified() >= 24 * 60 * 60 * 1000) {
 				file.delete();
 			}
 		}
 	}
 
 	private static void disableConnectionReuseIfNecessary() {
 		// HTTP connection reuse which was buggy pre-froyo
 		// Build.VERSION_CODES.FROYO
 		if (Build.VERSION.SDK_INT < 8) {
 			System.setProperty("http.keepAlive", "false");
 			Log.i("ToureNPlaner", "HTTP keep-alive disabled");
 		}
 	}
 }
