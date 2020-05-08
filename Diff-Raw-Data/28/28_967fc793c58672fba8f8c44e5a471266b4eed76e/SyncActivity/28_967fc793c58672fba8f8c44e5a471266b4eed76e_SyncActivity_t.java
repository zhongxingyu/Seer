 package de.iweinzierl.passsafe.android.activity.sync;
 
 import android.app.Activity;
 
 import android.content.Intent;
 
 import android.os.Bundle;
 
 import de.iweinzierl.passsafe.android.R;
 import de.iweinzierl.passsafe.android.logging.Logger;
 import de.iweinzierl.passsafe.android.sync.gdrive.GoogleDriveSync;
 import de.iweinzierl.passsafe.android.util.Constants;
 
 public class SyncActivity extends Activity implements GoogleDriveSync.Callback {
 
     public static final int GDRIVE_LOGIN_REQUEST = 1001;
 
     private static final Logger LOGGER = new Logger("SyncActivity");
 
     @Override
     protected void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_sync);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
 
        GoogleDriveSync sync = new GoogleDriveSync(this);
         sync.sync();
     }
 
     @Override
     protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
 
         switch (requestCode) {
 
             case GDRIVE_LOGIN_REQUEST :
                 onGDriveLoginResult(resultCode, data);
                 break;
 
             default :
                 super.onActivityResult(requestCode, resultCode, data);
         }
     }
 
     @Override
     public void onSyncFinished() {
         LOGGER.info("Finish activity with successful result");
 
         setResult(Constants.ACTIVITY_SYNC_FINISHED_SUCCESSFUL);
         finish();
     }
 
    @Override
    public void onSyncFailed() {
        LOGGER.info("Finish activity with failed result");

        GoogleDriveSync sync = new GoogleDriveSync(this);
        sync.sync();
    }

     private void onGDriveLoginResult(final int resultCode, final Intent data) {
         LOGGER.info("GoogleDrive login result: " + resultCode);
         for (String key : data.getExtras().keySet()) {
             LOGGER.info("   Extra: " + key + " = " + data.getStringExtra(key));
         }
     }
 }
