 package com.nightshadelabs.motoblurhome;
 
 import java.util.ArrayList;
 import java.util.List;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.os.Bundle;
 import android.widget.Toast;
 
 public class BlurHome extends Activity {
 	/**
 	 * @see android.app.Activity#onCreate(Bundle)
 	 */
 	Context context;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 	}
 
 	@Override
 	protected void onResume() {
 		
 		context = this;
 		
 		Intent intent = new Intent();
 		intent.setComponent(new ComponentName(
 					"com.motorola.blur.home",
 					"com.motorola.blur.home.HomeActivity"));
 		/*intent.setComponent(new ComponentName(
 				"com.android.launcher",
 				"com.android.launcher.Launcher"));*/
 		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 		
		//intent.putExtras(getIntent().getExtras());
 		
 		if(isIntentAvailable(this,intent))
 		{
 			startActivity(intent);
 		}
 		else if(getHomePackageCount(this) == 1)
 		{
 			// no other home packages are install. Fairly unlikely but possible.
 			launchMarketDialog();
 		}
 		else{
 			Toast.makeText(this, "This is not a MotoBlur device", Toast.LENGTH_LONG).show();
 			getPackageManager().clearPackagePreferredActivities(context.getPackageName());
 		}
 		finish();
 		super.onResume();
 	}
 
 	private void launchMarketDialog() {
 		
 		new AlertDialog.Builder(context)  
 		.setMessage("Wow, you are in a real pickle.\nYou managed to delete all the Home Apps.\nYou should probably install one.\n")  
 		.setTitle("No Home Apps?")  
 		.setCancelable(false)
 		.setPositiveButton("Market", new DialogInterface.OnClickListener() {  
 			public void onClick(DialogInterface dialog, int whichButton){
 				
 				 Intent intent = new Intent(Intent.ACTION_MAIN);
 				 intent.setComponent(new ComponentName("com.android.vending", "com.android.vending.AssetBrowserActivity"));
 				 startActivity(intent);
 			}
 		})
 		.setNeutralButton("System Settings", new DialogInterface.OnClickListener() {  
 			public void onClick(DialogInterface dialog, int whichButton){
 				
 				 startActivity(new Intent("android.settings.SETTINGS"));
 			}
 		})
 		.show();
 
 	}
 
 	public static boolean isIntentAvailable(Context context, Intent intent) {
 	    final PackageManager packageManager = context.getPackageManager();
 	    
 	    List<ResolveInfo> list =
 	            packageManager.queryIntentActivities(intent, 0);
 	    //Log.e("isIntentAvailable", ""+list.size());
 	    return list.size() > 0;
 	}
 	public static int getHomePackageCount(Context context) {
 		
 		Intent homeIntent = new Intent(Intent.ACTION_MAIN);
 		homeIntent.addCategory(Intent.CATEGORY_HOME);
         
         List<ResolveInfo> mApps;
         mApps = context.getPackageManager().queryIntentActivities(homeIntent, 0);
             
  		return  mApps.size();
 	}
 	
 }
