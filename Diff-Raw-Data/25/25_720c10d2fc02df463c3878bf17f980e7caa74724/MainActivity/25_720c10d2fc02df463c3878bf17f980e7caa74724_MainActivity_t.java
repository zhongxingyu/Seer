 package tengwa.djvu;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
import android.provider.Settings;
 import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
     public static final int GO_TO_DIALOG = 0;
     public static final int ABOUT_DIALOG = 1;
     public static final int SETTINGS_ACTIVITY = 2;
     public static final int OPEN_FILE_ACTIVITY = 3;
 
     private RecentDbAdapter mDbAdapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState){
         /*TODO: if savedInstanceState == null then go to OpenFileActivity*/
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         setTitle(R.string.app_name);
         loadPreferences();
         mDbAdapter = new RecentDbAdapter(getApplicationContext());
         mDbAdapter.open();
         Djvulibre.contextCreate();
         int cachesize = Djvulibre.cacheGetSize();
         Djvulibre.cacheSetSize(5 * (1 << 20));
         Djvulibre.cacheClear();
         Djvulibre.contextRelease();
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState){
         super.onSaveInstanceState(outState);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu){
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item){
         switch (item.getItemId()){
         case R.id.menu_item_about:
             showDialog(ABOUT_DIALOG);
             break;
         case R.id.menu_item_open:
             startActivityForResult(new Intent(this, OpenFileActivity.class), OPEN_FILE_ACTIVITY);
             break;
         case R.id.menu_item_goto:
             showDialog(GO_TO_DIALOG);
             break;
         case R.id.menu_item_settings:
             startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_ACTIVITY);
             break;
         }
         return true;
     }
 
     @Override
     protected Dialog onCreateDialog(int id){
         AlertDialog.Builder builder;
         LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
         switch (id){
         case ABOUT_DIALOG:
             builder = new AlertDialog.Builder(this);
             builder.setMessage(R.string.about);
             builder.setView(inflater.inflate(R.layout.about_dialog,
                     (ViewGroup) findViewById(R.layout.main)));
             builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
                         public void onClick(DialogInterface dialogInterface, int i) {
                         dialogInterface.cancel();
                   }
 
             });
             return builder.create();
         case GO_TO_DIALOG:
             GoToDialog dialog = new GoToDialog(this);
             dialog.setGoToPageAction(new GoToDialog.GoToPageAction(){
                 public void goToPage(int page) {
                     //TODO: implement this method
                 }
             });
             return dialog;
         }
         return null;
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent intent){
         switch (requestCode){
         case SETTINGS_ACTIVITY:
             loadPreferences();
             break;
         case OPEN_FILE_ACTIVITY:
             if (resultCode == RESULT_OK){
                 Bundle data = intent.getExtras();
                 String path = data.getString(RecentDbAdapter.KEY_PATH);
                 String name = data.getString(RecentDbAdapter.KEY_NAME);
                 int page = data.getInt(RecentDbAdapter.KEY_PAGE, -1);
                 {   //TODO: save path, name, page parameters and open file
                     // this is stub, remove it
                     if (name == null){
                         int first = path.lastIndexOf('/'), last = path.lastIndexOf('.');
                         name = path.substring(first + 1, last);
                     }
                     Toast.makeText(this, name, 300).show();
                     mDbAdapter.create(name, path, 1);
                 }
             }
             break;
         }
     }
 
     private void loadPreferences(){
         SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
         View toolbar = findViewById(R.id.toolbar);
         if (sp.getBoolean("toolbar_on", true)){
             toolbar.setVisibility(View.VISIBLE);
         } else {
             toolbar.setVisibility(View.GONE);
         }
     }
}
