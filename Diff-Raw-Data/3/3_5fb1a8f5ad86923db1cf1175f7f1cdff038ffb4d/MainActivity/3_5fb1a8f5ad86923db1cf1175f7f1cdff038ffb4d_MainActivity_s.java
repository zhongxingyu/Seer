 package ru.spravka42.panoShooter;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import ru.spravka42.panoShooter.R;
 
 import android.widget.ArrayAdapter;
  
 public class MainActivity extends Activity { 
 
 	public TextView tvProgress;
 	private ListView lstPresets; 
 	private ProgressBar pbProgress;
 	private Preset preset;
 	private Head head; 
 	private boolean connected = false;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
        
         final MainActivity activity = this;
         
         tvProgress = (TextView)findViewById(R.id.tvProgress);
         lstPresets = (ListView)findViewById(R.id.lstPresets);
         pbProgress = (ProgressBar)findViewById(R.id.pbProgress);
         ArrayList<String> presets = new ArrayList<String>();
         
         File sdcard = Environment.getExternalStorageDirectory();
         String path =  sdcard.getAbsolutePath() + "/panoShoot/preset.xml";
         try {
 			PresetParser parser = new PresetParser();
 			ArrayList<Shoot> shoots = parser.parse(new FileInputStream(path));
 			presets.add(path);
 			preset = new Preset(shoots);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         
         
         lstPresets.setAdapter(new ArrayAdapter<String>(
         		activity, 
         		android.R.layout.simple_list_item_1, 
         		presets));        
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
 //    public void onBackPressed() {
 //    	
 //    }
     
     public void Connect(View v) {
     	
     	if (connected) {
     		if (shootTask != null) shootTask.cancel(true);
     		head.stop();
     		Button btnConnect = (Button)findViewById(R.id.btnConnect);
     		btnConnect.setText("Connect");
     		connected = false;
     	}
     	
     	Button btnConnect = (Button)findViewById(R.id.btnConnect);
     	btnConnect.setEnabled(false);
     	head = new Head();
     	String _status = head.init();
     	if (_status != "ok") {
     		tvProgress.setText(_status);
     		btnConnect.setEnabled(true);
     		return;
     	}
     	connected = true;
     	tvProgress.setText("Press shoot");
     	Button btnShoot = (Button)findViewById(R.id.btnShoot);
     	btnShoot.setEnabled(true);
     	btnConnect.setEnabled(true);
     	btnConnect.setText("Stop");
     }
     
     
     private class ShootTask extends AsyncTask<Void, Integer, Void> {
 		protected Void doInBackground(Void... params) {
 			int shootsCount = preset.getShootsCount();
 			int currentShoot = 0;
 			publishProgress(currentShoot, shootsCount);
 			for (Shoot shoot : preset.shoots) {
 				if (isCancelled()) break;
 	    		currentShoot++;
 	            head.driveTo(shoot.getYaw(), shoot.getPitch());
 	            head.shoot();
 	            publishProgress(currentShoot, shootsCount);
 	        }
 			head.stop();
 			return null;
 		}
 		protected void onProgressUpdate(Integer... progress) {
 			if (progress[0] == 0) {
 				Button btnShoot = (Button)findViewById(R.id.btnShoot);
 		    	btnShoot.setEnabled(false);
 		    	pbProgress.setMax(progress[1]);
 			}
 			if (progress[0] == progress[1]) {
 				Button btnShoot = (Button)findViewById(R.id.btnShoot);
 				Button btnConnect = (Button)findViewById(R.id.btnConnect);
 		    	btnShoot.setEnabled(true);
 		    	btnConnect.setText("Connect");
 			}
 			tvProgress.setText(progress[0]+"/"+progress[1]);
             pbProgress.setProgress(progress[0]);
 		}
 		
 //		@SuppressWarnings("unused")
 //		protected void onCancelled(Void... result) {
 //			head.stop();
 //		}
     	
     }
     
     private ShootTask shootTask = null;
     public void Shoot(View v) {		
 		shootTask = new ShootTask();
 		shootTask.execute();
     }
     
 }
