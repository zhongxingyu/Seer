 /**
  * 
  */
 package ru.altruix.commons.android;
 
import ru.altruix.cb.android.common.IConnectivityManager;
import ru.altruix.cb.android.startup.IInternetAvailabilityChecker;
 import android.net.NetworkInfo;
 
 /**
  * @author DP118M
  *
  */
 public class InternetAvailabilityChecker implements
 		IInternetAvailabilityChecker {
 	private IConnectivityManager connectivityManager;
 	
 	public InternetAvailabilityChecker(final IConnectivityManager aConnectivityManager)
 	{
 		connectivityManager = aConnectivityManager;
 	}
 	
 	@Override
 	public boolean isInternetAvailable() {
 		final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
 		
 		return (networkInfo != null) && networkInfo.isConnected();
 	}
 }
