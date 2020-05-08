 package info.ilyaraz.passwordgenerator;
 
 import info.ilyaraz.passwordgenerator.util.Closure;
 import info.ilyaraz.passwordgenerator.util.StringCallback;
 
 import java.security.MessageDigest;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 
 public class MainActivity extends Activity {
 
     private static final String MASTER_HASH = "MASTER_HASH";
     
     private String masterPasswordHash = null;
 
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         final SharedPreferences settings = this.getSharedPreferences("UnpredictablePasswordGenerator", 0);
         String masterPasswordHash = settings.getString(MASTER_HASH, null);
         if (masterPasswordHash == null) {
         	final MainActivity activity = this;
         	
         	MasterPasswordEditor.editMasterPassword(this, "Set Master Password", "Please set master password.", 
         			new StringCallback() {
 						@Override
 						public void Run(String value) {
 							activity.masterPasswordHash = value;
 							setContentView(R.layout.activity_main);
 						}
 					}, 
         			new Closure() {
 						@Override
 						public void Run() {
 							moveTaskToBack(true);
 						}
 					});
         	return;
         }
         
         this.masterPasswordHash = masterPasswordHash;
         
         setContentView(R.layout.activity_main);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     
    public void changeMasterPasswordMenuItem(View view) {
     	final MainActivity activity = this;
     	
     	MasterPasswordEditor.editMasterPassword(this, "Change Master Password", "Please set new master password.", 
     			new StringCallback() {
 					@Override
 					public void Run(String value) {
 						activity.masterPasswordHash = value;
 						setContentView(R.layout.activity_main);
 					}
 				}, 
     			new Closure() {
 					@Override
 					public void Run() {
 					}
 				});
     }
 }
