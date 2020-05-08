 package uk.co.darkliquid.android.slidescreenplugins.clanforge;
 
 import android.content.ComponentName;
 import android.content.Intent;
 import android.graphics.*;
 import android.net.Uri;
 import android.util.Log;
 
 import com.larvalabs.slidescreen.PluginReceiver;
 
 /**
  *
  * @author darkliquid
  *
  */
 public class ClanforgePluginReceiver extends PluginReceiver {
 	private static final String TAG = ClanforgePluginReceiver.class.getSimpleName();
 
     @Override
     public int getColor() {
        return Color.parseColor("#329cc2");
     }
 
     @Override
     public Uri getContentProviderURI() {
         return ClanforgeContentProvider.CONTENT_URI;
     }
 
     @Override
     public String getName() {
         return "Clanforge";
     }
 
     @Override
     public int getIconResourceId() {
         return R.raw.icon;
     }
 
     @Override
     public Intent[] getSingleTapShortcutIntents() {
         return new Intent[]{new Intent(Intent.ACTION_VIEW, Uri.parse("http://clanforge.multiplay.co.uk"))};
     }
 
     @Override
     public Intent[] getLongpressShortcutIntents() {
         return new Intent[]{new Intent(Intent.ACTION_VIEW, Uri.parse("http://clanforge.multiplay.co.uk"))};
     }
 
     @Override
     public Intent getPreferenceActivityIntent() {
         Intent prefsIntent = new Intent(Intent.ACTION_MAIN);
         prefsIntent.setComponent(new ComponentName(
         	"uk.co.darkliquid.android.slidescreenplugins.clanforge",
         	"uk.co.darkliquid.android.slidescreenplugins.clanforge.ClanforgePluginPreferences"
         ));
         return prefsIntent;
     }
 
     @Override
     public void markedAsRead(String itemId) {
     	Log.d(TAG, "Received item marked as read: " + itemId);
     }
 }
