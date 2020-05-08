 package fi.cie.chiru.servicefusionar;
 
 import android.os.Bundle;
 import android.app.Activity;
 import java.io.*;
 
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.util.Log;
 
 public class MainActivity extends Activity {
 	private static final String TAG = "fi.cie.chiru.servicefusionar";
 	private ServiceFusionSetup serviceFusionSetup;
 //	private Intent scriptIntent;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 		super.onCreate(savedInstanceState);
 		
         serviceFusionSetup = new ServiceFusionSetup();
         serviceFusionSetup.run(this);
         
 //		File f = extractScript("scripts", "feedparser.py");
 //        //f = extractScript("scripts", "hello.py");
 //		
 //		f = extractScript("scripts", "sfkit.py");
 //		f = extractScript("scripts", "sftest_ylenews.py");
 //        
 //		if (scriptIntent == null)
 //		{
 //	        scriptIntent = buildStartInTerminalIntent(f); // /mnt/sdcard/sl4a/scripts/hello_world.py")); //scripts/sftest_ylenews.py")); //
 //	        startActivity(scriptIntent);
 //		}
 
 	}
    
     private File extractScript(String folder, String filename)
     {
     	File f = new File("/mnt/sdcard/" + TAG + "/" + filename); // getCacheDir()+"/script.py"); new FileOutputStream(f); //
     	try
     	{
     		//if (f.exists())
     		//{
     		//	f.delete();
     		//	Log.d(TAG, "tiedosto poistettu");
     		//}
     		/*else
     		{
     			f.createNewFile();
     		}*/
 
     		InputStream is = getAssets().open(folder + "/" + filename);
 			int size = is.available();
 			byte[] buffer = new byte[size];
 			is.read(buffer);
 			is.close();
 			
 			FileOutputStream fos = new FileOutputStream(f.getAbsolutePath());
 
 			//FileOutputStream fos = openFileOutput("/mnt/sdcard/Temp/Scripts/script.py", MODE_WORLD_READABLE); //
 			fos.write(buffer);
 			fos.close();
 
     	}
     	catch (IOException e)
     	{
     		Log.d(TAG, e.getMessage());
     	}
     	
     	return f;
     }
     
     public static Intent buildStartInTerminalIntent(File script) 
     {
     	final ComponentName componentName = Constants.SL4A_SERVICE_LAUNCHER_COMPONENT_NAME;
 	    Intent intent = new Intent();
 	    intent.setComponent(componentName);
 	    intent.setAction(Constants.ACTION_LAUNCH_BACKGROUND_SCRIPT); //
 	    intent.putExtra(Constants.EXTRA_SCRIPT_PATH, script.getAbsolutePath());
 	    
 	    return intent;
     } // buildStartInTerminalIntent
     
 	@Override
 	protected void onRestart() 
 	{
 		if (serviceFusionSetup != null)
 			serviceFusionSetup.onRestart(this);
		super.onRestart();
 	}
 
 	@Override
 	protected void onResume() 
 	{
 		if (serviceFusionSetup != null)
 			serviceFusionSetup.onResume(this);
		super.onResume();
 	}
 
 	@Override
 	protected void onStart() 
 	{
 		if (serviceFusionSetup != null)
 			serviceFusionSetup.onStart(this);
		super.onStart();
 	}
 
 	@Override
 	protected void onStop() 
 	{
 		if (serviceFusionSetup != null)
 			serviceFusionSetup.onStop(this);
		super.onStop();
 	}
 
 	@Override
 	protected void onPause() 
 	{
 		if (serviceFusionSetup != null)
 			serviceFusionSetup.onPause(this);
		super.onPause();
 	}
     
     @Override
     protected void onDestroy() 
     {
     	if (serviceFusionSetup != null)
     	{
 	    	serviceFusionSetup.stopServer();
 			serviceFusionSetup.onDestroy(this);
     	}

        super.onDestroy();
     }
     
     
 }
