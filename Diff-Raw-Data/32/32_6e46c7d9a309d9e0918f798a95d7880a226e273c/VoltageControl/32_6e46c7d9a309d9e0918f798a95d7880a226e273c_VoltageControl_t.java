 package com.jonathongrigg.proton.voltagecontrol;
 
 /*
 ** Copyright Jonathon Grigg <http://www.jonathongrigg.com> May 2011
 ** Email me at jonathongrigg@gmail.com if needed for some reason
 ** 
 ** Source code licensed under the Open Software License version 3.0
 ** 	http://www.opensource.org/licenses/osl-3.0
 */
 
 import java.io.OutputStreamWriter;
 import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.PorterDuff;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
import android.widget.PopupWindow;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.Toast;
 
 public class VoltageControl extends Activity {
 	// user entered custom voltages 100mhz-1.4ghz
 	int seekBar100;
 	int seekBar200;
 	int seekBar400;
 	int seekBar800;
 	int seekBar1000;
 	int seekBar1200;
 	int seekBar1300;
 	int seekBar1400;
 	
 	// Commands
 	protected static final String C_UV_MV_TABLE = "cat /sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table";
 	//protected static final String C_LIST_INIT_D = "ls /etc/init.d/";    // moved to ProtonPrefs
 	// Checks
 	boolean isSuAvailable = ShellInterface.isSuAvailable();
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	//HIDE KEYBOARD UNTIL A TEXT FIELD IS CLICKED
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 		  	
         super.onCreate(savedInstanceState);
         
         //lookup, then load selected theme OR default is 'proton red'
         SharedPreferences settings = getSharedPreferences("protonSavedPrefs", 0);
         int choosenTheme = settings.getInt(ProtonPrefs.THEME_SETTING, 1);
         if(choosenTheme == 1)
 			setContentView(R.layout.main);
 		else  // using else will guarantee that proton is "default" 
 			setContentView(R.layout.main_proton_theme);
                 
 
         // Error checking, if pass SU check then load kernel voltages
         if (isSuAvailable = false) {
         	Toast.makeText(getBaseContext(), "ERROR: No Root Access!", Toast.LENGTH_LONG).show();
         	finish();
         } else {
         	//load existing voltages with assumption that user is using a compatible kernel
         	getExistingVoltages();
         }
         
         //declare all buttons
     	Button applyVoltagesButton = (Button) findViewById(R.id.button1);
     	Button existingVoltagesButton = (Button) findViewById(R.id.button2);
     	Button defaultVoltagesButton = (Button) findViewById(R.id.button3);
     	Button recommendedVoltagesButton = (Button) findViewById(R.id.button4);
     	Button customVoltagesButton = (Button) findViewById(R.id.custom_button);
 
 
     	//change the bg color on buttons to match proton red IF selected
     	if(choosenTheme == 0) {
 	    	applyVoltagesButton.getBackground().setColorFilter(0xFF8d2122, PorterDuff.Mode.MULTIPLY);
 	    	existingVoltagesButton.getBackground().setColorFilter(0xFF8d2122, PorterDuff.Mode.MULTIPLY);
 	    	defaultVoltagesButton.getBackground().setColorFilter(0xFF8d2122, PorterDuff.Mode.MULTIPLY);
 	    	recommendedVoltagesButton.getBackground().setColorFilter(0xFF8d2122, PorterDuff.Mode.MULTIPLY);
 	    	customVoltagesButton.getBackground().setColorFilter(0xFF8d2122, PorterDuff.Mode.MULTIPLY);
     	}
 
     	
         applyVoltagesButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	TextView placeholder = (TextView) findViewById(R.id.viewText1400);
             		if ((placeholder.getText().toString().equals(""))) { Toast.makeText(getBaseContext(), "Error: No Voltage Entered for 1.4ghz", Toast.LENGTH_LONG).show(); }
             		String finalVoltage = placeholder.getText().toString();
             	placeholder = (TextView) findViewById(R.id.viewText1300);
             		if ((placeholder.getText().toString().equals(""))) { Toast.makeText(getBaseContext(), "Error: No Voltage Entered for 1.3ghz", Toast.LENGTH_LONG).show(); }
             		finalVoltage = finalVoltage + " " + placeholder.getText().toString();
             	placeholder = (TextView) findViewById(R.id.viewText1200);
             		if ((placeholder.getText().toString().equals(""))) { Toast.makeText(getBaseContext(), "Error: No Voltage Entered for 1.2ghz", Toast.LENGTH_LONG).show(); }
             		finalVoltage = finalVoltage + " " + placeholder.getText().toString();
             	placeholder = (TextView) findViewById(R.id.viewText1000);
             		if ((placeholder.getText().toString().equals(""))) { Toast.makeText(getBaseContext(), "Error: No Voltage Entered for 1ghz", Toast.LENGTH_LONG).show(); }
             		finalVoltage = finalVoltage + " " + placeholder.getText().toString();
             	placeholder = (TextView) findViewById(R.id.viewText800);
             		if ((placeholder.getText().toString().equals(""))) { Toast.makeText(getBaseContext(), "Error: No Voltage Entered for 800mhz", Toast.LENGTH_LONG).show(); }
             		finalVoltage = finalVoltage + " " + placeholder.getText().toString();
             	placeholder = (TextView) findViewById(R.id.viewText400);
             		if ((placeholder.getText().toString().equals(""))) { Toast.makeText(getBaseContext(), "Error: No Voltage Entered for 400mhz", Toast.LENGTH_LONG).show(); }
             		finalVoltage = finalVoltage + " " + placeholder.getText().toString();
             	placeholder = (TextView) findViewById(R.id.viewText200);
             		if ((placeholder.getText().toString().equals(""))) { Toast.makeText(getBaseContext(), "Error: No Voltage Entered for 200mhz", Toast.LENGTH_LONG).show(); }
             		finalVoltage = finalVoltage + " " + placeholder.getText().toString();
             	placeholder = (TextView) findViewById(R.id.viewText100);
             		if ((placeholder.getText().toString().equals(""))) { Toast.makeText(getBaseContext(), "Error: No Voltage Entered for 100mhz", Toast.LENGTH_LONG).show(); }
             		finalVoltage = finalVoltage + " " + placeholder.getText().toString();
             	
             	//check the prefs to see if the user wants voltages in the init
             	SharedPreferences settings = getSharedPreferences("protonSavedPrefs", 0);
             	boolean saveBoot = settings.getBoolean(ProtonPrefs.SAVE_ON_BOOT, false);
             	if (saveBoot) {   //if user wants voltages saved
             		if (finalVoltage.length() > 27) {
                 		ShellInterface.runCommand(buildUvCommand(finalVoltage));
                 		saveBootSettings(finalVoltage);
                 		}
                 		else {
                 			Toast.makeText(getBaseContext(), "Error: Missing Voltages", Toast.LENGTH_LONG).show();
                 		}
             	}
             	else {
             		if (finalVoltage.length() > 27) {
             		ShellInterface.runCommand(buildUvCommand(finalVoltage));
             		Toast.makeText(getBaseContext(), "Voltages Applied Successfully", Toast.LENGTH_SHORT).show();
             		}
             		else {
             			Toast.makeText(getBaseContext(), "Error: Missing Voltages", Toast.LENGTH_LONG).show();
             		}
             	}  
             }
         });
     	
         
         View.OnClickListener listener = new View.OnClickListener() {
             public void onClick(View v) {
             	switch(v.getId()) {
             	  case R.id.button2: 
             		  getExistingVoltages();
             	      break;
             	  case R.id.button3:
             		  defaultVoltages();
             	      break;
             	  case R.id.button4:
             		  recommendedVoltages();
             	      break;
             	  case R.id.custom_button:
             		  customVoltages();
             	      break;
             	}
             }
         };
                 
     	existingVoltagesButton.setOnClickListener(listener);
     	defaultVoltagesButton.setOnClickListener(listener);
     	recommendedVoltagesButton.setOnClickListener(listener);
     	customVoltagesButton.setOnClickListener(listener);
     	
     }
 
     private void loadSliderData() {
     	//load slider data from VoltageControlSlider
     	SharedPreferences mySharedPreferences = getSharedPreferences(
                         "protonVoltages", Activity.MODE_PRIVATE);
         seekBar100 = mySharedPreferences.getInt("seekBar100", 0);
         seekBar200 = mySharedPreferences.getInt("seekBar200", 0);
         seekBar400 = mySharedPreferences.getInt("seekBar400", 0);
         seekBar800 = mySharedPreferences.getInt("seekBar800", 0);
         seekBar1000 = mySharedPreferences.getInt("seekBar1000", 0);
         seekBar1200 = mySharedPreferences.getInt("seekBar1200", 0);
         seekBar1300 = mySharedPreferences.getInt("seekBar1300", 0);
         seekBar1400 = mySharedPreferences.getInt("seekBar1400", 0);
 }
     
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
   
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
         case R.id.menuEmergencyBoot:
             downloadEmergencyBoot();
             return true;
         case R.id.menuSettings:
 			//setContentView(R.layout.settings);
 			Intent prefs = new Intent();
 			prefs.setClass(this, ProtonPrefs.class);
 			startActivity(prefs);
 			return true;
         case R.id.menuCustomVoltage:
 			//setContentView(R.layout.seekbardialogpreference_layout);
 			Intent custVolt = new Intent();
 			custVolt.setClass(this, VoltageControlSlider.class);
 			startActivity(custVolt);
 			return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
     
     private void defaultVoltages() {
     	findViewById(R.id.button1).setVisibility(View.VISIBLE);  // show apply button
     	// Edit text boxes
         TextView cpu1400 = (TextView)findViewById(R.id.viewText1400);	// 1400mhz
         TextView cpu1300 = (TextView)findViewById(R.id.viewText1300);	// 1300mhz
         TextView cpu1200 = (TextView)findViewById(R.id.viewText1200);	// 1200mhz
         TextView cpu1000 = (TextView)findViewById(R.id.viewText1000);	// 1000mhz
         TextView cpu800 = (TextView)findViewById(R.id.viewText800);		// 800mhz
         TextView cpu400 = (TextView)findViewById(R.id.viewText400);		// 400mhz
         TextView cpu200 = (TextView)findViewById(R.id.viewText200);		// 200mhz
         TextView cpu100 = (TextView)findViewById(R.id.viewText100);		// 100mhz
         
         // Strings
         String dvString = this.getString(R.string.stock_voltages);
         String[] dv = dvString.split(" ");
         
         // Applying code
         cpu1400.setText(dv[0]);
         cpu1300.setText(dv[1]);
         cpu1200.setText(dv[2]);
         cpu1000.setText(dv[3]);
         cpu800.setText(dv[4]);
         cpu400.setText(dv[5]);
         cpu200.setText(dv[6]);
         cpu100.setText(dv[7]);
     }  
     
     private void customVoltages() {
     	loadSliderData();
	    	if(seekBar1400 == 0 || seekBar1300 == 0 || seekBar1200 == 0 || seekBar1000 == 0 || seekBar800 == 0 || seekBar400 == 0 || seekBar200 == 0 || seekBar100 == 0) {
	    		AlertWindow(getString(R.string.alert_no_custom_uv));
	    	} else {
	    		findViewById(R.id.button1).setVisibility(View.VISIBLE);  // show apply button
	    	}
     	
     	// Edit text boxes
     	TextView cpu1400 = (TextView)findViewById(R.id.viewText1400);
     	TextView cpu1300 = (TextView)findViewById(R.id.viewText1300);
     	TextView cpu1200 = (TextView)findViewById(R.id.viewText1200);
     	TextView cpu1000 = (TextView)findViewById(R.id.viewText1000);
     	TextView cpu800 = (TextView)findViewById(R.id.viewText800);
     	TextView cpu400 = (TextView)findViewById(R.id.viewText400);
     	TextView cpu200 = (TextView)findViewById(R.id.viewText200);
     	TextView cpu100 = (TextView)findViewById(R.id.viewText100);
         
         
         
         // Load data from custom voltage menu
         cpu1400.setText(Integer.toString(seekBar1400));
         cpu1300.setText(Integer.toString(seekBar1300));
         cpu1200.setText(Integer.toString(seekBar1200));
         cpu1000.setText(Integer.toString(seekBar1000));
         cpu800.setText(Integer.toString(seekBar800));
         cpu400.setText(Integer.toString(seekBar400));
         cpu200.setText(Integer.toString(seekBar200));
         cpu100.setText(Integer.toString(seekBar100));
     } 
 
     private void recommendedVoltages() {
     	findViewById(R.id.button1).setVisibility(View.VISIBLE);  // show apply button
     	// Edit text boxes
         TextView cpu1400 = (TextView)findViewById(R.id.viewText1400);	// 1400mhz
         TextView cpu1300 = (TextView)findViewById(R.id.viewText1300);	// 1300mhz
         TextView cpu1200 = (TextView)findViewById(R.id.viewText1200);	// 1200mhz
         TextView cpu1000 = (TextView)findViewById(R.id.viewText1000);	// 1000mhz
         TextView cpu800 = (TextView)findViewById(R.id.viewText800);		// 800mhz
         TextView cpu400 = (TextView)findViewById(R.id.viewText400);		// 400mhz
         TextView cpu200 = (TextView)findViewById(R.id.viewText200);		// 200mhz
         TextView cpu100 = (TextView)findViewById(R.id.viewText100);		// 100mhz
         
         // Strings
         String rvString = this.getString(R.string.recommended_voltages);
         String[] rv = rvString.split(" ");
         
         // Applying code
         cpu1400.setText(rv[0]);
         cpu1300.setText(rv[1]);
         cpu1200.setText(rv[2]);
         cpu1000.setText(rv[3]);
         cpu800.setText(rv[4]);
         cpu400.setText(rv[5]);
         cpu200.setText(rv[6]);
         cpu100.setText(rv[7]);
     }
     
     private void getExistingVoltages() {
     	findViewById(R.id.button1).setVisibility(View.GONE);  // existing values do not need button
         String existingVoltagesValue = null;
         String[] tableValues;
         StringBuilder voltages = new StringBuilder();
         
         existingVoltagesValue = ShellInterface.getProcessOutput(C_UV_MV_TABLE);
         
         tableValues = existingVoltagesValue.split(" ");
         for (int i = 1; i < tableValues.length; i += 3) {
         	
         	//1400mhz
         	if (i == 1) { 		
         		TextView cpu1400 = (TextView)findViewById(R.id.viewText1400);
         		cpu1400.setText(tableValues[i]);
         	}
         	
         	//1300mhz
         	if (i == 4) { 		
         		TextView cpu1300 = (TextView)findViewById(R.id.viewText1300);
         		cpu1300.setText(tableValues[i]);
         	}
         	
         	//1200mhz
         	if (i == 7) { 		
         		TextView cpu1200 = (TextView)findViewById(R.id.viewText1200);
         		cpu1200.setText(tableValues[i]);
         	}
         	
         	//1000mhz
         	if (i == 10) { 		
         		TextView cpu1000 = (TextView)findViewById(R.id.viewText1000);
         		cpu1000.setText(tableValues[i]);
         	}
         	
         	//800mhz
         	if (i == 13) { 		
         		TextView cpu800 = (TextView)findViewById(R.id.viewText800);
         		cpu800.setText(tableValues[i]);
         	}
         	
         	//400mhz
         	if (i == 16) { 		
         		TextView cpu400 = (TextView)findViewById(R.id.viewText400);
         		cpu400.setText(tableValues[i]);
         	}
         	
         	//200mhz
         	if (i == 19) { 		
         		TextView cpu200 = (TextView)findViewById(R.id.viewText200);
         		cpu200.setText(tableValues[i]);
         	}
         	
         	//100mhz
         	if (i == 22) { 		
         		TextView cpu100 = (TextView)findViewById(R.id.viewText100);
         		cpu100.setText(tableValues[i]);
         	}
 
         	voltages.append(tableValues[i]);
         	voltages.append(" ");
         }
 	}
     
 	private String buildUvCommand(String et) {
 		StringBuilder command = new StringBuilder();
 		String values = et;
 		
 		command.append("echo \"");
 		command.append(values);
 		command.append("\" > /sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table");//
 	
 		return command.toString();
 	}
 	
 	private void downloadEmergencyBoot() {
 		Intent downloadIntent = new Intent(Intent.ACTION_VIEW,
 			Uri.parse("http://dl.andro1d.org/proton_emergency_boot.zip"));
 			startActivity(downloadIntent); 
 	}
 	
 	private void saveBootSettings(String et) {
 		try {
 			OutputStreamWriter out = new OutputStreamWriter(openFileOutput(
 					"proton_voltage_control", 0));
 			String tmp = "#!/system/bin/sh\n"
 					+"# Proton Voltage Control 'Set on Boot' file \n"
 					+ buildUvCommand(et);
 			out.write(tmp);
 			out.close();
 		} catch (java.io.IOException e) {
 			Toast.makeText(this, "Error: file not saved!", Toast.LENGTH_LONG).show();
 		}
 
 		ShellInterface.runCommand("chmod 777 /data/data/com.jonathongrigg.proton.voltagecontrol/files/proton_voltage_control");
 		ShellInterface.runCommand("busybox mount -o remount,rw  /system");
 		ShellInterface.runCommand("busybox cp /data/data/com.jonathongrigg.proton.voltagecontrol/files/proton_voltage_control /etc/init.d/proton_voltage_control");
 		ShellInterface.runCommand("busybox mount -o remount,ro  /system");
 		Toast.makeText(this, "Settings saved in file \"/etc/init.d/proton_voltage_control\"", Toast.LENGTH_LONG).show();
 	}
 	
	public void AlertWindow(String et){
		String AlertMsg = et;
		
        AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
        alertbox.setMessage(AlertMsg);
        alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });
        alertbox.show();
	}
	
 	
 }
