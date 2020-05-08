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
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.PorterDuff;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class VoltageControl extends Activity {
 	
 	// Commands
 	protected static final String C_UV_MV_TABLE = "cat /sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table";
 	protected static final String C_LIST_INIT_D = "ls /etc/init.d/";
 	// Checks
 	boolean isSuAvailable = ShellInterface.isSuAvailable();
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	//HIDE KEYBOARD UNTIL A TEXT FIELD IS CLICKED
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 		  	
         super.onCreate(savedInstanceState);
 
         SharedPreferences settings = getSharedPreferences("protonSavedPrefs", 0);
         int choosenTheme = settings.getInt(ProtonPrefs.THEME_SETTING, 1);
         if(choosenTheme == 0)
 			setContentView(R.layout.main); 
 		else if(choosenTheme == 1)
 			setContentView(R.layout.main_proton_theme);
                 
         //setContentView(R.layout.main);
         
         if (isSuAvailable = false) {
         	Toast.makeText(getBaseContext(), "ERROR: No Root Access!", Toast.LENGTH_LONG).show();
         	finish();
        } else {
        	//load existing voltatges with assumption that user is using a compatiable kernel
        	getExistingVoltages();
         }
         
     	Button applyVoltagesButton = (Button) findViewById(R.id.button1);
     	Button existingVoltagesButton = (Button) findViewById(R.id.button2);
     	Button defaultVoltagesButton = (Button) findViewById(R.id.button3);
     	Button recommendedVoltagesButton = (Button) findViewById(R.id.button4);
     	Button removeBootSettingsButton = (Button) findViewById(R.id.button5);
     	final CheckBox saveOnBootCheckBox = (CheckBox) findViewById(R.id.checkBox1);
     	
     	if(choosenTheme == 1) {
 	    	//change the bg color of the lower buttons
 	    	removeBootSettingsButton.getBackground().setColorFilter(0xFF8d2122, PorterDuff.Mode.MULTIPLY);
 	    	applyVoltagesButton.getBackground().setColorFilter(0xFF8d2122, PorterDuff.Mode.MULTIPLY);
 	    	existingVoltagesButton.getBackground().setColorFilter(0xFF8d2122, PorterDuff.Mode.MULTIPLY);
 	    	defaultVoltagesButton.getBackground().setColorFilter(0xFF8d2122, PorterDuff.Mode.MULTIPLY);
 	    	recommendedVoltagesButton.getBackground().setColorFilter(0xFF8d2122, PorterDuff.Mode.MULTIPLY);
     	}
     	
         applyVoltagesButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	EditText placeholder = (EditText) findViewById(R.id.editText1400);
             		if ((placeholder.getText().toString().equals(""))) { Toast.makeText(getBaseContext(), "Error: No Voltage Entered for 1.4ghz", Toast.LENGTH_LONG).show(); }
             		String finalVoltage = placeholder.getText().toString();
             	placeholder = (EditText) findViewById(R.id.editText1300);
             		if ((placeholder.getText().toString().equals(""))) { Toast.makeText(getBaseContext(), "Error: No Voltage Entered for 1.3ghz", Toast.LENGTH_LONG).show(); }
             		finalVoltage = finalVoltage + " " + placeholder.getText().toString();
             	placeholder = (EditText) findViewById(R.id.editText1200);
             		if ((placeholder.getText().toString().equals(""))) { Toast.makeText(getBaseContext(), "Error: No Voltage Entered for 1.2ghz", Toast.LENGTH_LONG).show(); }
             		finalVoltage = finalVoltage + " " + placeholder.getText().toString();
             	placeholder = (EditText) findViewById(R.id.editText1000);
             		if ((placeholder.getText().toString().equals(""))) { Toast.makeText(getBaseContext(), "Error: No Voltage Entered for 1ghz", Toast.LENGTH_LONG).show(); }
             		finalVoltage = finalVoltage + " " + placeholder.getText().toString();
             	placeholder = (EditText) findViewById(R.id.editText800);
             		if ((placeholder.getText().toString().equals(""))) { Toast.makeText(getBaseContext(), "Error: No Voltage Entered for 800mhz", Toast.LENGTH_LONG).show(); }
             		finalVoltage = finalVoltage + " " + placeholder.getText().toString();
             	placeholder = (EditText) findViewById(R.id.editText400);
             		if ((placeholder.getText().toString().equals(""))) { Toast.makeText(getBaseContext(), "Error: No Voltage Entered for 400mhz", Toast.LENGTH_LONG).show(); }
             		finalVoltage = finalVoltage + " " + placeholder.getText().toString();
             	placeholder = (EditText) findViewById(R.id.editText200);
             		if ((placeholder.getText().toString().equals(""))) { Toast.makeText(getBaseContext(), "Error: No Voltage Entered for 200mhz", Toast.LENGTH_LONG).show(); }
             		finalVoltage = finalVoltage + " " + placeholder.getText().toString();
             	placeholder = (EditText) findViewById(R.id.editText100);
             		if ((placeholder.getText().toString().equals(""))) { Toast.makeText(getBaseContext(), "Error: No Voltage Entered for 100mhz", Toast.LENGTH_LONG).show(); }
             		finalVoltage = finalVoltage + " " + placeholder.getText().toString();
             		
             
             	if (saveOnBootCheckBox.isChecked()) {
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
     	
         existingVoltagesButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 getExistingVoltages();
             }
         });
         
         defaultVoltagesButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	defaultVoltages();
             }
         });
         
         recommendedVoltagesButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 recommendedVoltages();     		
             }
         });
         
         removeBootSettingsButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 removeBootSettings();
             }
         });
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
 			Intent intent = new Intent();
 			intent.setClass(this, ProtonPrefs.class);
 			startActivity(intent);
 			return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
     
     private void defaultVoltages() {
     	// Edit text boxes
         EditText cpu1400 = (EditText)findViewById(R.id.editText1400);	// 1400mhz
         EditText cpu1300 = (EditText)findViewById(R.id.editText1300);	// 1300mhz
         EditText cpu1200 = (EditText)findViewById(R.id.editText1200);	// 1200mhz
         EditText cpu1000 = (EditText)findViewById(R.id.editText1000);	// 1000mhz
         EditText cpu800 = (EditText)findViewById(R.id.editText800);		// 800mhz
         EditText cpu400 = (EditText)findViewById(R.id.editText400);		// 400mhz
         EditText cpu200 = (EditText)findViewById(R.id.editText200);		// 200mhz
         EditText cpu100 = (EditText)findViewById(R.id.editText100);		// 100mhz
         
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
 
     private void recommendedVoltages() {
     	// Edit text boxes
         EditText cpu1400 = (EditText)findViewById(R.id.editText1400);	// 1400mhz
         EditText cpu1300 = (EditText)findViewById(R.id.editText1300);	// 1300mhz
         EditText cpu1200 = (EditText)findViewById(R.id.editText1200);	// 1200mhz
         EditText cpu1000 = (EditText)findViewById(R.id.editText1000);	// 1000mhz
         EditText cpu800 = (EditText)findViewById(R.id.editText800);		// 800mhz
         EditText cpu400 = (EditText)findViewById(R.id.editText400);		// 400mhz
         EditText cpu200 = (EditText)findViewById(R.id.editText200);		// 200mhz
         EditText cpu100 = (EditText)findViewById(R.id.editText100);		// 100mhz
         
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
         String existingVoltagesValue = null;
         String[] tableValues;
         StringBuilder voltages = new StringBuilder();
         
         existingVoltagesValue = ShellInterface.getProcessOutput(C_UV_MV_TABLE);
         
         tableValues = existingVoltagesValue.split(" ");
         for (int i = 1; i < tableValues.length; i += 3) {
         	
         	//1400mhz
         	if (i == 1) { 		
                 EditText cpu1400 = (EditText)findViewById(R.id.editText1400);
         		cpu1400.setText(tableValues[i]);
         	}
         	
         	//1300mhz
         	if (i == 4) { 		
                 EditText cpu1300 = (EditText)findViewById(R.id.editText1300);
         		cpu1300.setText(tableValues[i]);
         	}
         	
         	//1200mhz
         	if (i == 7) { 		
                 EditText cpu1200 = (EditText)findViewById(R.id.editText1200);
         		cpu1200.setText(tableValues[i]);
         	}
         	
         	//1000mhz
         	if (i == 10) { 		
                 EditText cpu1000 = (EditText)findViewById(R.id.editText1000);
         		cpu1000.setText(tableValues[i]);
         	}
         	
         	//800mhz
         	if (i == 13) { 		
                 EditText cpu800 = (EditText)findViewById(R.id.editText800);
         		cpu800.setText(tableValues[i]);
         	}
         	
         	//400mhz
         	if (i == 16) { 		
                 EditText cpu400 = (EditText)findViewById(R.id.editText400);
         		cpu400.setText(tableValues[i]);
         	}
         	
         	//200mhz
         	if (i == 19) { 		
                 EditText cpu200 = (EditText)findViewById(R.id.editText200);
         		cpu200.setText(tableValues[i]);
         	}
         	
         	//100mhz
         	if (i == 22) { 		
                 EditText cpu100 = (EditText)findViewById(R.id.editText100);
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
 	
 	private void removeBootSettings() {
 		if (!ShellInterface.getProcessOutput(C_LIST_INIT_D).contains("proton_voltage_control")) {
 			Toast.makeText(getBaseContext(), "Error: No Saved Boot Settings Present", Toast.LENGTH_LONG).show();
 		}
 		else {
 			ShellInterface.runCommand("busybox mount -o remount,rw  /system");
 			ShellInterface.runCommand("rm /etc/init.d/proton_voltage_control");
 			ShellInterface.runCommand("busybox mount -o remount,ro  /system");
 			Toast.makeText(this, "Removed settings saved in file \"/etc/init.d/proton_voltage_control\"", Toast.LENGTH_LONG).show();
 		}
 	}
 	
 }
