 package de.snertlab.wlanconfigsaver;
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 public class ConfigSaver extends Activity {
     /** Called when the activity is first created. */
 	
 	private static final String WPA_SUPPLICANT_FILENAME = "wpa_supplicant.conf";
 	private static final String WPA_SUPPLICANT_PATH = "/data/misc/wifi/" + WPA_SUPPLICANT_FILENAME;
 	private static final String BACKUP_FILENAME = "wpa_supplicant.conf.bak";
 	private static final String BACKUP_PATH = "/sdcard/" + BACKUP_FILENAME;
 	
 	private TextView txtViewInfo;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         txtViewInfo = (TextView) findViewById(R.id.TextView01);
     }
     
     public void btnClickHandlerBackup(View view) throws IOException, InterruptedException {
     	Runtime r=Runtime.getRuntime();
     	Process p2 = r.exec("su");
     	DataOutputStream d=new DataOutputStream(p2.getOutputStream());
     	d.writeBytes("cp " + WPA_SUPPLICANT_PATH + " " + BACKUP_PATH + "\n");
     	d.writeBytes("exit\n");
     	d.flush();
     	p2.waitFor();
     	File file = new File(BACKUP_PATH);
     	txtViewInfo.setText("backup " + (file.exists() ? "successful " + BACKUP_PATH : "failed"));
     }
     
     public void btnClickHandlerRestore(View view) throws IOException, InterruptedException {
     	File file = new File(BACKUP_PATH);
     	if(!file.exists()){
     		txtViewInfo.setText("backup file :" + BACKUP_PATH + " not found");
     		return;
     	}
     	Runtime r=Runtime.getRuntime();
     	Process p2 = r.exec("su");
     	DataOutputStream d=new DataOutputStream(p2.getOutputStream());
     	d.writeBytes("cp " + BACKUP_PATH + " " + WPA_SUPPLICANT_PATH + "\n");
    	d.writeBytes("chmod u=rw,g=rw,o-r-x " + WPA_SUPPLICANT_PATH + "\n");
     	d.writeBytes("exit\n");
     	d.flush();
     	p2.waitFor();
     	txtViewInfo.setText("restore successful"); //TODO: Richtige Prfung einbauen ob successful oder nicht
     }
 }
