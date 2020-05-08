 package ru.spb.cupchinolabs.androidlocator;
 
 import android.content.Context;
 import android.location.Location;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * Created with IntelliJ IDEA.
  * User: VladimirK
  * Date: 03.03.13
  * Time: 12:21
  */
 public class Utils {
 
     private static final Boolean TOASTS_ENABLED = true;
 
     private static final Boolean LOGS_ENABLED = true;
 
     private static final int TWO_MINUTES = 1000 * 60 * 2;
 
     public static void print(String text, Context context, String tag) {
         if (TOASTS_ENABLED && context != null) {
             Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
         }
         if (LOGS_ENABLED) {
             Log.d(tag, text);
         }
     }
 
     /**
     **********************************************************
     * THIS METHOD WAS COPY-PASTED FROM GOOGLE REFERENCE DOCS *
     **********************************************************
     *
      * Determines whether one Location reading is better than the current Location fix
      *
      * @param location            The new Location that you want to evaluate
      * @param currentBestLocation The current Location fix, to which you want to compare the new one
      */
     public static boolean isBetterLocation(Location location, Location currentBestLocation) {
         if (currentBestLocation == null) {
             // A new location is always better than no location
             return true;
         }
 
         // Check whether the new location fix is newer or older
         long timeDelta = location.getTime() - currentBestLocation.getTime();
         boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
         boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
         boolean isNewer = timeDelta > 0;
 
         // If it's been more than two minutes since the current location, use the new location
         // because the user has likely moved
         if (isSignificantlyNewer) {
             return true;
             // If the new location is more than two minutes older, it must be worse
         } else if (isSignificantlyOlder) {
             return false;
         }
 
         // Check whether the new location fix is more or less accurate
         int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
         boolean isLessAccurate = accuracyDelta > 0;
         boolean isMoreAccurate = accuracyDelta < 0;
         boolean isSignificantlyLessAccurate = accuracyDelta > 200;
 
         // Check if the old and new location are from the same provider
         boolean isFromSameProvider = isSameProvider(location.getProvider(),
                 currentBestLocation.getProvider());
 
         // Determine location quality using a combination of timeliness and accuracy
         if (isMoreAccurate) {
             return true;
         } else if (isNewer && !isLessAccurate) {
             return true;
         } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
             return true;
         }
         return false;
     }
 
     /**
      * Checks whether two providers are the same
      */
     private static boolean isSameProvider(String provider1, String provider2) {
         if (provider1 == null) {
             return provider2 == null;
         }
         return provider1.equals(provider2);
     }
 
 }
