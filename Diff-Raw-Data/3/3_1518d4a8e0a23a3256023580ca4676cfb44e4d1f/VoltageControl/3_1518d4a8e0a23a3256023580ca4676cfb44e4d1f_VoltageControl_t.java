 package com.jonathongrigg.proton.voltagecontrol;
 
 /*
 ** Copyright Jonathon Grigg <http://www.jonathongrigg.com> May 2011
 ** Email me at jonathongrigg@gmail.com if needed for some reason
 ** 
 ** Source code licensed under the Open Software License version 3.0
 ** 	http://www.opensource.org/licenses/osl-3.0
 */
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.Editable;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class VoltageControl extends Activity {
 	// Commands
 	protected static final String C_UV_MV_TABLE = "cat /sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         final EditText newVoltages = (EditText) findViewById(R.id.editText1);
     	Button applyVoltagesButton = (Button) findViewById(R.id.button1);
     	Button existingVoltagesButton = (Button) findViewById(R.id.button2);
     	Button defaultVoltagesButton = (Button) findViewById(R.id.button3);
     	
         applyVoltagesButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
         		ShellInterface.runCommand(buildUvCommand(newVoltages));
             }
         });
     	
         existingVoltagesButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 getExistingVoltages(newVoltages);
             }
         });
         
         defaultVoltagesButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 newVoltages.setText(R.string.stock_voltages);
             }
         });
     }
     
     private void getExistingVoltages(EditText et) {
         String existingVoltagesValue = null;
         String[] tableValues;
         StringBuilder voltages = new StringBuilder();
         
         if (ShellInterface.isSuAvailable()) {
         	existingVoltagesValue = ShellInterface.getProcessOutput(C_UV_MV_TABLE);
         }
         tableValues = existingVoltagesValue.split(" ");
         for (int i = 1; i < tableValues.length; i += 3) {
         	voltages.append(tableValues[i]);
        	voltages.append(" ");
         }
         et.setText(voltages.toString());
 	}
     
 	private String buildUvCommand(EditText et) {
 		StringBuilder command = new StringBuilder();
 		Editable values = et.getText();
 		
 		command.append("echo \"");
 		command.append(values);
 		command.append("\" > /sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table");//
 	
 		return command.toString();
 	}
 }
