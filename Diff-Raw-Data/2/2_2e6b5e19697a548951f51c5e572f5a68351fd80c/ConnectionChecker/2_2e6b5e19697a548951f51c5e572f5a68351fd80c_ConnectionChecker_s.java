 package palganthony.mtg.prices.network;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 
 public final class ConnectionChecker {
 
 	public static boolean isOnline(Context pContext)
 	{
 		ConnectivityManager cm = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);
 	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
 	    
 	    return netInfo != null && netInfo.isConnectedOrConnecting();
 	}
 	
 	public static boolean isNotOnline(Context pContext)
 	{
		return isOnline(pContext);
 	}
 	
 	/**
 	 * Shows an AlertDialog telling the user there is no Internet connection.
 	 *  
 	 * @param pContext
 	 * @return
 	 */
 	public static AlertDialog noInternetDialog(Context pContext)
 	{
 		final AlertDialog.Builder builder = new AlertDialog.Builder(pContext)
 			.setTitle("No Internet Connection!")
 			.setMessage("An Internet connection is required to load card prices and images. Please connect your device to the Internet and try again.")
 			.setPositiveButton("OK", null);
 		
 		return builder.create();
 	}
 	
 	/**
 	 * This method checks for a connection, then shows a dialog if there's no connection.
 	 *  
 	 * @param pContext The Context used to build to the dialog
 	 * @return true if there is an Internet connection, false otherwise
 	 */
 	public static boolean noAndShow(Context pContext)
 	{
 		boolean result = isOnline(pContext);
 		
 		if(!result)
 			noInternetDialog(pContext).show();
 		
 		return result;
 	}
 }
