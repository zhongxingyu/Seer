 package uk.ac.cam.cl.xf214.blackadderSettings;
 
 import java.io.IOException;
 import java.util.Vector;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 public class BlackadderSettingsActivity extends Activity {
 	public static final String TAG = "BlackadderSettingsActivity";
 	public static final String CLICK_HOME = "/data/click";
 	public static final String CLICK_EXEC = CLICK_HOME + "/bin/click";
 	public static final String CLICK_LIB = CLICK_HOME + "/lib";
 	public static final String DEFAULT_CONF_DIR = "/sdcard/blackadder/";
 	
 	private String confFilePath;
 	private TextView lblCurConfFile;
 	private ListView confList;
 	private CheckBox baSwitch;
 	
 	private SuShell su;
 	
 	 /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         try {
 			su = new SuShell();
 			setLdPath();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			this.finish();
 		}
 	    initBASwitch();
     }
     
     @Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 		if (su != null) {
 			su.finish();
 		}
 	}
 
 	private void initBASwitch() {
     	lblCurConfFile = (TextView)findViewById(R.id.lbl_cur_conf_file);
     	confList = (ListView)findViewById(R.id.conf_list);
     	 
     	/* get conf file list */
     	Vector<String> files = getConfList();
     	ArrayAdapter<String> adp = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, files);
     	confList.setAdapter(adp);
     	confList.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 				setConfFilePath(DEFAULT_CONF_DIR + (String)confList.getItemAtPosition(arg2));
 			}
     	});
     	if (!files.isEmpty()) {
     		// select the first config by default
     		setConfFilePath(DEFAULT_CONF_DIR + files.get(0));
     	}
     	
     	baSwitch = (CheckBox)findViewById(R.id.chk_ba_switch);
     	
     	if (isBlackadderStarted()) {
     		baSwitch.setChecked(true);
     	}
     	
     	baSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView,
 					boolean isChecked) {
 				if (isChecked) {
 					startBlackadder();
 				} else {
 					killBlackadder();
 				}
 			}
     	});
     }
 	
 	private Vector<String> getConfList() {
 		Vector<String> files = null;
 		try {
 			files = su.execSync("ls -1 /sdcard/blackadder");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return files;
 	}
     
     private boolean isBlackadderStarted() {
     	try {
			Vector<String> echos = su.execSync("ps | grep click");
			Log.i(TAG, "Is Blackadder started? size = " + echos.size());
			return echos.size() > 0;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
     	
     }
     
     private void startBlackadder() {
 		try {
 			su.execSync(CLICK_EXEC + " " + confFilePath + "&");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		toastMessage("Blackadder started!");
     }
     
     private void killBlackadder() {
     	try {
 			su.execSync("killall click");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	toastMessage("Blackadder stopped!");
     }
     
     private void setLdPath() {
     	try {
 			su.execSync("LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/data/click/lib/");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
     
     private void setConfFilePath(String path) {
     	confFilePath = path;
     	lblCurConfFile.setText(path);
     }
     
     private void toastMessage(String msg) {
     	Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
     }
 }
