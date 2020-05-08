 
 package ca.nickpresta.android.myguelph;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
import android.content.DialogInterface;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.about_contents);
        AlertDialog alert = builder.create();
        alert.setTitle(R.string.about_title);
        alert.setButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });
         alert.show();
     }
 }
