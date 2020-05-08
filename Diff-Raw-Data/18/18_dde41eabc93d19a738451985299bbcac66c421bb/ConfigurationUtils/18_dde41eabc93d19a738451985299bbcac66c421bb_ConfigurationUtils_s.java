 package com.malcom.library.android.module.config;
 
 import java.util.regex.Pattern;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.util.Log;
 
 
 /**
  * This class has some utility methods to be used by configuration module. 
  * 
  * @author Malcom Ventures, S.L.
  * @since  2012
  *
  */
 public class ConfigurationUtils {
 
 	
 	/**
 	 * Creates an alert dialog of the specified type in the configuration. Available ones
 	 * are INFO, BLOCK, FORCE and SUGGEST.
 	 * 
 	 * @param context
 	 * @param configuration
 	 * @param activity
 	 * @return
 	 */
 	public static AlertDialog createAlertDialog(Activity context, Configuration configuration){
     	
 		Log.d("Create alert", configuration.getAlertMsg());
 		System.out.println("___________________Tipo de alerta: "+configuration.getAlertType());
     	if (configuration.getAlertType().equals("INFO")) {						
     		return generateDialogINFO(context, configuration.getAlertMsg());
 				
 		} else if (configuration.getAlertType().equals("BLOCK")) {
 			return generateDialogBLOCK(context, configuration.getAlertMsg());
 				
 		} else if (configuration.getAlertType().equals("FORCE")) {				
 			return generateDialogFORCE(context, configuration.getAlertMsg(), configuration.getAlertUrlAppStore());
 			
 		} else if (configuration.getAlertType().equals("SUGGEST")) {			
 			return generateDialogSUGGESTS(context, configuration.getAlertMsg(), configuration.getAlertUrlAppStore());
 			
 		} else{
 			return null;
 		}
     }    
 	
 	/**
 	 * Normalises the version string with the specified format.
 	 * 
 	 * @param version
 	 * @param sep
 	 * @param maxWidth
 	 * @return
 	 */
 	public static String normalisedVersion(String version, String sep, int maxWidth) {
     	
         String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
         StringBuilder sb = new StringBuilder();
         for (String s : split) {
             sb.append(String.format("%" + maxWidth + 's', s));
         }
         
         return sb.toString();
     }
 	
 	
 	
 	
 	
 	// AUXILIAR METHODS 
 	
 	
     private static AlertDialog generateDialogINFO(final Activity context, final String msg){
     	return new AlertDialog.Builder(context).setTitle("")
 		 			 .setMessage(msg).setNeutralButton("Aceptar", new DialogInterface.OnClickListener() {
 				           public void onClick(DialogInterface dialog, int id) {
 				        	   //context.startActivity(activity);
 				        	   //context.finish();
 				           }
 					}).show();
     }
     
     private static AlertDialog generateDialogBLOCK(final Activity context, final String msg){
     	return new AlertDialog.Builder(context).setTitle("")
 					 .setMessage(msg).setNegativeButton("Close", new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {					        	   
 								context.finish();
 							}
 						}).show();
     }
     
     private static AlertDialog generateDialogFORCE(final Activity context, final String msg, final String urlAppStore){
     	return new AlertDialog.Builder(context).setTitle("")
 					.setMessage(msg).setNegativeButton("Close", new DialogInterface.OnClickListener() {
 				           public void onClick(DialogInterface dialog, int id) {
 				        	   context.finish();
 				           }
 					})
 					.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
 				           public void onClick(DialogInterface dialog, int id) {
 				        	   Intent intent = new Intent(Intent.ACTION_VIEW);
				        	   intent.setData(Uri.parse(extractMarketAppInfo(urlAppStore))); 
 				        	   context.startActivity(intent);
 				        	   //context.finish();
 				           }
 					}).show();
     }
     
     private static AlertDialog generateDialogSUGGESTS(final Activity context, final String msg, final String urlAppStore){
     	return new AlertDialog.Builder(context).setTitle("")
 					.setMessage(msg).setNegativeButton("Aceptar", new DialogInterface.OnClickListener() {
 				           public void onClick(DialogInterface dialog, int id) {
 				        	   //context.startActivity(activity);
 				        	   //context.finish();
 				           }
 					})
 					.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
 				           public void onClick(DialogInterface dialog, int id) {
 				        	   Intent intent = new Intent(Intent.ACTION_VIEW);
				        	   intent.setData(Uri.parse(extractMarketAppInfo(urlAppStore)));
 				        	   context.startActivity(intent);
 				        	   //context.finish();
 				           }
 					}).show();	
     }
    
    private static String extractMarketAppInfo(String appUrl){
    	//Market URL format: 
    	// TODO En Malcom la verificación de URL debería cambiar.
    	//	market://details?id=<application.package.id> 
    	if (appUrl != null) {
        	return appUrl.substring((appUrl.indexOf("details?id=")+"details?id=".length()),appUrl.length());
    	} else {
    		return "";
    	}
    }
	
 }
