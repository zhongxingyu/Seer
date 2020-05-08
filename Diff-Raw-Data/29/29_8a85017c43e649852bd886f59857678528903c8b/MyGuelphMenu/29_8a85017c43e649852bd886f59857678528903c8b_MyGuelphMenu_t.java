 
 package ca.nickpresta.android.myguelph;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
import android.text.Html;
import android.text.method.LinkMovementMethod;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
import android.widget.TextView;
 
 public class MyGuelphMenu extends Activity {
 
     private static final int DIALOG_ABOUT = 2;
 
     public static void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.globalmenu, menu);
     }
 
     public static boolean onOptionsItemSelected(MenuItem item, Context context) {
 
         int itemId = item.getItemId();
         if (itemId == R.id.menuAbout) {
             showDialog(DIALOG_ABOUT, context);
             return true;
         } else if (itemId == R.id.menuPreferences) {
             Intent prefsIntent = new Intent(context, MyGuelphPrefsActivity.class);
             context.startActivity(prefsIntent);
             return true;
         } else {
             return false;
         }
     }
 
     private static void showDialog(int id, Context context) {
         switch (id) {
             case DIALOG_ABOUT:
                 showAboutDialog(context);
                 break;
         }
     }
 
     private static void showAboutDialog(Context context) {

        final AlertDialog alert = new AlertDialog.Builder(context)
                .setPositiveButton(android.R.string.ok, null)
                .setMessage(Html.fromHtml(context.getString(R.string.about_contents)))
                .setTitle(R.string.about_title)
                .create();
         alert.show();

        ((TextView) alert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod
                .getInstance());
     }
 }
