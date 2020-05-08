 package com.cesco.tetherhack;
 
 import java.io.File;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.cesco.tetherhack.util.*;
 
 public class MainActivity extends Activity {
 	Button button1;
 	TextView editInt;
 	TextView editSub;
 	CheckBox checkBox1;
 	Button button2;
 	
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		
 		button1 = (Button) findViewById(R.id.button1);
 		button2 = (Button) findViewById(R.id.button2);
 		final EditText Interface_text = (EditText) this.findViewById(R.id.editInt);
 		final EditText Port_text = (EditText) this.findViewById(R.id.editSub);
 		checkBox1 = (CheckBox) findViewById(R.id.checkBox1);
 		
 		File file = new File("/system/etc/init.d/50tetherhack");
 		 
 		if(file.exists()) { 
 	    	  	checkBox1.setChecked(true);
 	    	  	if (checkBox1.isChecked()) {
 	    			button1.setEnabled(false);
 	    		}
 	    		else{
 	    			button1.setEnabled(true);
 	    		}
 	    	
 	      }
 	      else {
 	    	  //Toast.makeText(getApplicationContext(),"file does not exist!!!" , Toast.LENGTH_LONG).show();
 	      
 		};
 		OnClickListener oclButton1 = new OnClickListener() {
 			@SuppressWarnings("deprecation")
 			@Override
 		       public void onClick(View v) {
 				
 				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
 				String value = Port_text.getText().toString() + Interface_text.getText().toString();
 				if (value.matches("")) {
 				// set title
 				alertDialogBuilder.setTitle("Warning!");
 	 
 				// set dialog message
 				alertDialogBuilder
 					.setMessage("Input Fields are Empty! Fill Them!")
 					.setCancelable(false)
 					.setNeutralButton("OK",new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,int id) {
 							dialog.cancel();
 						}
 					});
 				}
 				else {
 					// set title
 					alertDialogBuilder.setTitle("Done!");
 		 
 					// set dialog message
 					alertDialogBuilder
 						.setMessage("Hack Enabled!")
 						.setCancelable(false)
 						.setNeutralButton("OK",new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,int id) {
 								dialog.cancel();
 							}
 						});
 				}
 				AlertDialog alertDialog = alertDialogBuilder.create();
 				
 				
 				if ( value.matches("")) {
 					alertDialog.show();
 					//Toast.makeText(getApplicationContext(), "Input fields are Empty!!", Toast.LENGTH_SHORT).show();
 				}
 				else {
 		    	   CMDProcessor cmd = new CMDProcessor();
 		    	   cmd.su.runWaitFor("busybox iptables -tnat -A natctrl_nat_POSTROUTING -s 192.168.42.0/" + Port_text.getText().toString() + " -o " + Interface_text.getText().toString() + " -j MASQUERADE");  
 		    	   cmd.su.runWaitFor("busybox iptables -tnat -A natctrl_nat_POSTROUTING -s 192.168.43.0/" + Port_text.getText().toString() + " -o " + Interface_text.getText().toString() + " -j MASQUERADE"); 
 		    	   cmd.su.runWaitFor("busybox iptables -tnat -A natctrl_nat_POSTROUTING -s 192.168.44.0/" + Port_text.getText().toString() + " -o " + Interface_text.getText().toString() + " -j MASQUERADE"); 
 		    	   //Toast.makeText(getApplicationContext(), "Hack Enabled!", Toast.LENGTH_SHORT).show();
 		    	   alertDialog.show();
 		    	   button1.setEnabled(false);
 		    	   button2.setEnabled(true);
 				}
 		    	   //debug with Toast messages LOL! :D
 		    	   //Toast.makeText(getApplicationContext(),"busybox iptables -tnat -A natctrl_nat_POSTROUTING -s 192.168.0.0/" + Port_text.getText().toString() + " -o " + Interface_text.getText().toString() + " -j MASQUERADE" , Toast.LENGTH_LONG).show();
 			}
 			
 		};
 		OnClickListener oclButton2 = new OnClickListener() {
 			@SuppressWarnings("deprecation")
 			@Override
 		       public void onClick(View v) {
 				
 				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
 				String value = Port_text.getText().toString() + Interface_text.getText().toString();   
 				if (value.matches("")) {
 				// set title
 				alertDialogBuilder.setTitle("Warning!");
 	 
 				// set dialog message
 				alertDialogBuilder
 					.setMessage("Input Fields are Empty! Fill Them!")
 					.setCancelable(false)
 					.setNeutralButton("OK",new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,int id) {
 							// if this button is clicked, just close
 							// the dialog box and do nothing
 							dialog.cancel();
 						}
 					});
 				}
 				else {
 					// set title
 					alertDialogBuilder.setTitle("Done!");
 		 
 					// set dialog message
 					alertDialogBuilder
 						.setMessage("Hack Disabled!")
 						.setCancelable(false)
 						.setNeutralButton("OK",new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,int id) {
 								// if this button is clicked, just close
 								// the dialog box and do nothing
 								dialog.cancel();
 							}
 						});
 				}
 				AlertDialog alertDialog = alertDialogBuilder.create();
 				
 				
 				if ( value.matches("")) {
 					alertDialog.show();
 				}
 				else {
 		    	   CMDProcessor cmd = new CMDProcessor();
 		    	   cmd.su.runWaitFor("busybox iptables -tnat -D natctrl_nat_POSTROUTING -s 192.168.42.0/" + Port_text.getText().toString() + " -o " + Interface_text.getText().toString() + " -j MASQUERADE");  
 		    	   cmd.su.runWaitFor("busybox iptables -tnat -D natctrl_nat_POSTROUTING -s 192.168.43.0/" + Port_text.getText().toString() + " -o " + Interface_text.getText().toString() + " -j MASQUERADE"); 
 		    	   cmd.su.runWaitFor("busybox iptables -tnat -D natctrl_nat_POSTROUTING -s 192.168.44.0/" + Port_text.getText().toString() + " -o " + Interface_text.getText().toString() + " -j MASQUERADE"); 
 		    	   File file = new File("/system/etc/init.d/50tetherhack");
 		   			if(file.exists()) { 
 		   				CMDProcessor cmd1 = new CMDProcessor();
 						cmd1.su.runWaitFor("busybox mount -o remount,rw /system");
 						cmd1.su.runWaitFor("busybox rm -f /system/etc/init.d/50tetherhack");
 						cmd1.su.runWaitFor("busybox mount -o remount,ro /system");
 						checkBox1.setChecked(false);
 		   		}
 		   			alertDialog.show();
 		   			button1.setEnabled(true);
 		    	   //Toast.makeText(getApplicationContext(), "Hack Disabled!", Toast.LENGTH_SHORT).show();
 				}
 			}
 			
 		};
 		OnClickListener chblistener = new OnClickListener() {
 			
 			@SuppressWarnings("deprecation")
 			@Override
 			public void onClick(View v) {
 				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
 				String value = Port_text.getText().toString() + Interface_text.getText().toString();
 				if (value.matches("")) {
 				// set title
 					alertDialogBuilder.setTitle("Warning!");
 					 
 					// set dialog message
 					alertDialogBuilder
 						.setMessage("Input Fields are Empty! Fill Them!")
 						.setCancelable(false)
 						.setNeutralButton("OK",new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,int id) {
 								// if this button is clicked, just close
 								// the dialog box and do nothing
 								dialog.cancel();
 							}
 						});
 				}
 				else {
 					alertDialogBuilder.setTitle("Done!");
 					 
 					// set dialog message
 					alertDialogBuilder
 						.setMessage("hack set ot Boot! Please reboot your phone to activate it!")
 						.setCancelable(false)
 						.setNeutralButton("OK",new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,int id) {
 								dialog.cancel();
 							}
 						});
 				}
 				AlertDialog alertDialog = alertDialogBuilder.create();
 				
 				
 			if (checkBox1.isChecked()==true) {
 				if (value.matches("")) {
 					alertDialog.show();
					checkBox1.setChecked(false);
 				}
 				else {
             	CMDProcessor cmd = new CMDProcessor();
             	   cmd.su.runWaitFor("busybox mount -o remount,rw /system");
             	   cmd.su.runWaitFor("busybox echo '#!/system/bin/sh' > /system/etc/init.d/50tetherhack");
 		    	   cmd.su.runWaitFor("busybox echo 'sleep 30' >> /system/etc/init.d/50tetherhack");
 		    	   cmd.su.runWaitFor("busybox echo '#USB' >> /system/etc/init.d/50tetherhack");
 		    	   cmd.su.runWaitFor("busybox echo 'iptables -tnat -A natctrl_nat_POSTROUTING -s 192.168.42.0/" + Port_text.getText().toString() + " -o " + Interface_text.getText().toString() + " -j MASQUERADE' >> /system/etc/init.d/50tetherhack");
 		    	   cmd.su.runWaitFor("busybox echo '#WLAN' >> /system/etc/init.d/50tetherhack");
 		    	   cmd.su.runWaitFor("busybox echo 'iptables -tnat -A natctrl_nat_POSTROUTING -s 192.168.43.0/" + Port_text.getText().toString() + " -o " + Interface_text.getText().toString() + " -j MASQUERADE' >> /system/etc/init.d/50tetherhack");
 		    	   cmd.su.runWaitFor("busybox echo '#BT' >> /system/etc/init.d/50tetherhack");
 		    	   cmd.su.runWaitFor("busybox echo 'iptables -tnat -A natctrl_nat_POSTROUTING -s 192.168.44.0/" + Port_text.getText().toString() + " -o " + Interface_text.getText().toString() + " -j MASQUERADE' >> /system/etc/init.d/50tetherhack");
 		    	   cmd.su.runWaitFor("busybox chmod 755 /system/etc/init.d/50tetherhack");
 		    	   cmd.su.runWaitFor("busybox mount -o remount,ro /system");
 		    	   button1.setEnabled(false);
 		    	   alertDialog.show();
 		    	   
 				}
             }
 			if (checkBox1.isChecked()==false) {
 				File file = new File("/system/etc/init.d/50tetherhack");
 				 
 				if(file.exists()) {
 				CMDProcessor cmd = new CMDProcessor();
 				cmd.su.runWaitFor("busybox mount -o remount,rw /system");
 				cmd.su.runWaitFor("busybox rm -f /system/etc/init.d/50tetherhack");
 				cmd.su.runWaitFor("busybox mount -o remount,ro /system");
 				cmd.su.runWaitFor("busybox iptables -tnat -D natctrl_nat_POSTROUTING -s 192.168.42.0/" + Port_text.getText().toString() + " -o " + Interface_text.getText().toString() + " -j MASQUERADE");  
 		    	cmd.su.runWaitFor("busybox iptables -tnat -D natctrl_nat_POSTROUTING -s 192.168.43.0/" + Port_text.getText().toString() + " -o " + Interface_text.getText().toString() + " -j MASQUERADE"); 
 		    	cmd.su.runWaitFor("busybox iptables -tnat -D natctrl_nat_POSTROUTING -s 192.168.44.0/" + Port_text.getText().toString() + " -o " + Interface_text.getText().toString() + " -j MASQUERADE");
 		    	Toast.makeText(getApplicationContext(), "Hack completely disabled! ", Toast.LENGTH_LONG).show();
 		    	button1.setEnabled(true);
 		    	button2.setEnabled(false);
 				}
 				else {
 					//do nothing
 				}
 			}
 			}
 		};
 		button1.setOnClickListener(oclButton1);
 		button2.setOnClickListener(oclButton2);
 		checkBox1.setOnClickListener(chblistener);
 		
 	}
 };
