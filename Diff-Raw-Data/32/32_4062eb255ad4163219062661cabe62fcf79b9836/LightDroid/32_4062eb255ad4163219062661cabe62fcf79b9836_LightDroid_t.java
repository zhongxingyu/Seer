 package com.mettadore.lightdroid;
 
import java.net.InetAddress;
import java.net.UnknownHostException;

 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.ToggleButton;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.Toast;
 
 //import com.mettadore.*;
 
 public class LightDroid extends Activity implements OnSeekBarChangeListener 
 {
 
 	SharedPreferences settings;
 	int seek_bar_value;
 	int[] channel_values;
 	ToggleButton[] toggles;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		final SeekBar slider = (SeekBar) findViewById(R.id.slider);
 		slider.setOnSeekBarChangeListener(this);
 		slider.setMax(255);
 
 		final ToggleButton togglebutton1 = (ToggleButton) findViewById(R.id.togglebutton1);
 		final ToggleButton togglebutton2 = (ToggleButton) findViewById(R.id.togglebutton2);
 		final ToggleButton togglebutton3 = (ToggleButton) findViewById(R.id.togglebutton3);
 		final ToggleButton togglebutton4 = (ToggleButton) findViewById(R.id.togglebutton4);
 		final ToggleButton togglebutton5 = (ToggleButton) findViewById(R.id.togglebutton5);
 		final ToggleButton togglebutton6 = (ToggleButton) findViewById(R.id.togglebutton6);
 		final ToggleButton togglebutton7 = (ToggleButton) findViewById(R.id.togglebutton7);
 		final ToggleButton togglebutton8 = (ToggleButton) findViewById(R.id.togglebutton8);
 		final ToggleButton togglebutton9 = (ToggleButton) findViewById(R.id.togglebutton9);
 		final ToggleButton togglebutton10 = (ToggleButton) findViewById(R.id.togglebutton10);
 		final ToggleButton togglebutton11 = (ToggleButton) findViewById(R.id.togglebutton11);
 		final ToggleButton togglebutton12 = (ToggleButton) findViewById(R.id.togglebutton12);
 		final ToggleButton togglebutton13 = (ToggleButton) findViewById(R.id.togglebutton13);
 		final ToggleButton togglebutton14 = (ToggleButton) findViewById(R.id.togglebutton14);
 		final ToggleButton togglebutton15 = (ToggleButton) findViewById(R.id.togglebutton15);
 		final ToggleButton togglebutton16 = (ToggleButton) findViewById(R.id.togglebutton16);
 		final ToggleButton togglebutton17 = (ToggleButton) findViewById(R.id.togglebutton17);
 		final ToggleButton togglebutton18 = (ToggleButton) findViewById(R.id.togglebutton18);
 		final ToggleButton togglebutton19 = (ToggleButton) findViewById(R.id.togglebutton19);
 		final ToggleButton togglebutton20 = (ToggleButton) findViewById(R.id.togglebutton20);
 		final ToggleButton togglebutton21 = (ToggleButton) findViewById(R.id.togglebutton21);
 		final ToggleButton togglebutton22 = (ToggleButton) findViewById(R.id.togglebutton22);
 		final ToggleButton togglebutton23 = (ToggleButton) findViewById(R.id.togglebutton23);
 		final ToggleButton togglebutton24 = (ToggleButton) findViewById(R.id.togglebutton24);
 		final ToggleButton togglebutton25 = (ToggleButton) findViewById(R.id.togglebutton25);
 		final ToggleButton togglebutton26 = (ToggleButton) findViewById(R.id.togglebutton26);
 		final ToggleButton togglebutton27 = (ToggleButton) findViewById(R.id.togglebutton27);
 		final ToggleButton togglebutton28 = (ToggleButton) findViewById(R.id.togglebutton28);
 		final ToggleButton togglebutton29 = (ToggleButton) findViewById(R.id.togglebutton29);
 		final ToggleButton togglebutton30 = (ToggleButton) findViewById(R.id.togglebutton30);
 		final ToggleButton togglebutton31 = (ToggleButton) findViewById(R.id.togglebutton31);
 		final ToggleButton togglebutton32 = (ToggleButton) findViewById(R.id.togglebutton32);
 		final ToggleButton togglebutton33 = (ToggleButton) findViewById(R.id.togglebutton33);
 		final ToggleButton togglebutton34 = (ToggleButton) findViewById(R.id.togglebutton34);
 		final ToggleButton togglebutton35 = (ToggleButton) findViewById(R.id.togglebutton35);
 		final ToggleButton togglebutton36 = (ToggleButton) findViewById(R.id.togglebutton36);
 
 		toggles = new ToggleButton[] {
 				togglebutton1,togglebutton2,togglebutton3, togglebutton4,togglebutton5,togglebutton6,
 				togglebutton7,togglebutton8,togglebutton9,togglebutton10,togglebutton11,togglebutton12,
 				togglebutton13,togglebutton14,togglebutton15,togglebutton16,togglebutton17,togglebutton18,
 				togglebutton19,togglebutton20,togglebutton21,togglebutton22,togglebutton23,togglebutton24,
 				togglebutton25,togglebutton26,togglebutton27,togglebutton28,togglebutton29,togglebutton30,
 				togglebutton31,togglebutton32,togglebutton33,togglebutton34,togglebutton35,togglebutton36,
 		};
 		channel_values = new int[36];
 		for (int i=0; i<36; i++) {
 			channel_values[i] = 0;
 		}
 
 		OnClickListener toggleclick = new OnClickListener() {
 			public void onClick(View v) {
 			}
 		};
 
 		for (ToggleButton toggle : toggles) {
 			toggle.setOnClickListener(toggleclick);
 			}
 
 		final Button gobutton = (Button) findViewById(R.id.gobutton);
 		gobutton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				updateValues(percentage());
 			}
 		});
 		final Button allbutton = (Button) findViewById(R.id.allbutton);
 		allbutton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				for (int i=0; i<36; i++) {
 					toggles[i].setChecked(true);
 				}
 				Toast.makeText(LightDroid.this, "Select All", Toast.LENGTH_SHORT).show();
 			}
 		});
 		final Button nonebutton = (Button) findViewById(R.id.nonebutton);
 		nonebutton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				for (int i=0; i<36; i++) {
 					toggles[i].setChecked(false);
 				}
 				Toast.makeText(LightDroid.this, "Select None", Toast.LENGTH_SHORT).show();
 			}
 		});
 		final Button resetbutton = (Button) findViewById(R.id.resetbutton);
 		resetbutton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				for (int i=0; i<36; i++) {
 					toggles[i].setChecked(true);
 				}
 				updateValues(0);
 				slider.setProgress(0);
 				for (int i=0; i<36; i++) {
 					toggles[i].setChecked(false);
 				}
 				Toast.makeText(LightDroid.this, "All Channels Reset", Toast.LENGTH_SHORT).show();
 			}
 		});
 	}
 
 	public int percentage() {
 		int value = (int) ((seek_bar_value / 255.0) * 100.0);
 		return value;
 	}
 	public void updateValues(int update_value) {
 		String channels = "0";
 		String ch;
 		for (int i=0; i<36; i++) {
 			if (toggles[i].isChecked()) {
 				ch = String.format("+%d", i+1);
 				channels = channels.concat(ch);
 			}
 		}
 		String it;
 		it = String.format("%s @ %d%%", channels, update_value);
 		//Toast.makeText(LightDroid.this, it, Toast.LENGTH_SHORT).show();
 		sendTelnetCommand(it);
 	}
 
 	public void sendTelnetCommand(String command) {
 		Toast.makeText(LightDroid.this, command, Toast.LENGTH_SHORT).show();
		try {
			byte[] b = new byte[] {(byte)192,(byte)168,(byte)2,(byte)9};
			InetAddress addr = null;
			addr = InetAddress.getByAddress(b);
			InetAddress server = InetAddress.getByName("192.168.2.9");
			TelnetSample telnet = new TelnetSample(addr);
			telnet.sendCommand(command);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
 	public void onProgressChanged(SeekBar seekBar, int progress,
 			boolean fromUser) {
 		//		freq_bar_value = progress;
 	}
 
 	public void onStartTrackingTouch(SeekBar seekBar) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void onStopTrackingTouch(SeekBar seekBar) {
 		seek_bar_value = seekBar.getProgress();
 	}
 
 
 }
 
