 /**
  * @author Edouard DUPIN
  *
  * @copyright 2011, Edouard DUPIN, all right reserved
  *
  * @license BSD v3 (see license file)
  */
 package org.ewol;
 
 import android.app.ActivityManager;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.ConfigurationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.Context;
 import android.opengl.GLSurfaceView;
 import android.opengl.GLSurfaceView.Renderer;
 import android.os.Build;
 import android.service.wallpaper.WallpaperService;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import org.ewol.EwolSurfaceViewGL;
 import android.view.MotionEvent;
 
 import static org.ewol.Ewol.EWOL;
 
 public abstract class EwolWallpaper extends WallpaperService implements EwolCallback, EwolConstants
 {
 	private GLEngine mGLView;
 	static {
 		System.loadLibrary("ewol");
 	}
 	
 	protected void initApkPath(String org, String vendor, String project) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(org).append(".");
 		sb.append(vendor).append(".");
 		sb.append(project);
 		String apkFilePath = null;
 		ApplicationInfo appInfo = null;
 		PackageManager packMgmr = getPackageManager();
 		try {
 			appInfo = packMgmr.getApplicationInfo(sb.toString(), 0);
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 			throw new RuntimeException("Unable to locate assets, aborting...");
 		}
 		apkFilePath = appInfo.sourceDir;
 		Ewol.paramSetArchiveDir(0, apkFilePath);
 	}
 	
 	@Override public Engine onCreateEngine() {
 		
 		// set the java evironement in the C sources :
 		Ewol.setJavaVirtualMachineStartWallpaperEngine(this);
 		
 		// Load the application directory
 		Ewol.paramSetArchiveDir(1, getFilesDir().toString());
 		Ewol.paramSetArchiveDir(2, getCacheDir().toString());
 		// to enable extarnal storage: add in the manifest the restriction needed ...
 		//packageManager.checkPermission("android.permission.READ_SMS", myPackage) == PERMISSION_GRANTED; 
 		//Ewol.paramSetArchiveDir(3, getExternalCacheDir().toString());
 		
 		
 		//! DisplayMetrics metrics = new DisplayMetrics();
 		//! getWindowManager().getDefaultDisplay().getMetrics(metrics);
 		//! EWOL.displayPropertyMetrics(metrics.xdpi, metrics.ydpi);
 		
 		// call C init ...
 		EWOL.onCreate();
 		
 		// create bsurface system
 		mGLView = new GLEngine();
 		
 		return mGLView;
 	}
 	
 	
 	
 	public class GLEngine extends Engine
 	{
 		class WallpaperGLSurfaceView extends EwolSurfaceViewGL
 		{
 			private static final String TAG = "WallpaperGLSurfaceView";
 			WallpaperGLSurfaceView(Context context)
 			{
 				super(context);
 				Log.d(TAG, "WallpaperGLSurfaceView(" + context + ")");
 			}
 			@Override
 			public SurfaceHolder getHolder()
 			{
 				Log.d(TAG, "getHolder(): returning " + getSurfaceHolder());
 				return getSurfaceHolder();
 			}
 			public void onDestroy()
 			{
 				Log.d(TAG, "onDestroy()");
 				super.onDetachedFromWindow();
 			}
 		}
 		
 		private static final String TAG = "GLEngine";
 		private WallpaperGLSurfaceView glSurfaceView;
 		
 		@Override public void onCreate(SurfaceHolder surfaceHolder)
 		{
 			Log.d(TAG, "onCreate(" + surfaceHolder + ")");
 			super.onCreate(surfaceHolder);
 			
 			glSurfaceView = new WallpaperGLSurfaceView(EwolWallpaper.this);
 			
 			// Check if the system supports OpenGL ES 2.0.
 			final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
 			final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
 			final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
 			
 			if (false==supportsEs2) {
 				Log.d("LiveWallpaper", "does not support board with only open GL ES 1");
 				return;
 			}
 			// Request an OpenGL ES 2.0 compatible context.
 			//setEGLContextClientVersion(2);
 			
 			// On Honeycomb+ devices, this improves the performance when
 			// leaving and resuming the live wallpaper.
			setPreserveEGLContextOnPause(true);
 			
 		}
 		
 		@Override public void onTouchEvent(MotionEvent event)
 		{
 			glSurfaceView.onTouchEvent(event);
 		}
 		
 		@Override public void onVisibilityChanged(boolean visible)
 		{
 			Log.d(TAG, "onVisibilityChanged(" + visible + ")");
 			super.onVisibilityChanged(visible);
 			if (true==visible) {
 				glSurfaceView.onResume();
 				// call C
 				EWOL.onResume();
 			} else {
 				glSurfaceView.onPause();
 				// call C
 				EWOL.onPause();
 			}
 		}
 		
 		@Override public void onDestroy()
 		{
 			Log.d(TAG, "onDestroy()");
 			super.onDestroy();
 			// call C
 			EWOL.onStop();
 			EWOL.onDestroy();
 			glSurfaceView.onDestroy();
 		}
 		
 		protected void setPreserveEGLContextOnPause(boolean preserve)
 		{
 			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 				Log.d(TAG, "setPreserveEGLContextOnPause(" + preserve + ")");
 				glSurfaceView.setPreserveEGLContextOnPause(preserve);
 			}
 		}
 		
 		protected void setEGLContextClientVersion(int version)
 		{
 			Log.d(TAG, "setEGLContextClientVersion(" + version + ")");
 			glSurfaceView.setEGLContextClientVersion(version);
 		}
 	}
 	
 	public void keyboardUpdate(boolean show)
 	{
 		// never display keyboard on wallpaer...
 	}
 	
 	public void eventNotifier(String[] args)
 	{
 		// just for the test ...
 		EWOL.touchEvent();
 	}
 	
 	public void orientationUpdate(int screenMode)
 	{
 		
 	}
 }
 
