 package com.thechange.projects.quickboot;
 
 import com.thechange.projects.quickboot.CommandUtil.Command;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class BootActivity extends Activity implements OnClickListener {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_boot);
 
 		findViewById(R.id.reboot).setOnClickListener(this);
 		findViewById(R.id.reboot_recovery).setOnClickListener(this);
 		findViewById(R.id.reboot_bootloader).setOnClickListener(this);
 		findViewById(R.id.power_off).setOnClickListener(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.boot, menu);
 		return true;
 	}
 
 	@Override
 	public void onClick(View v) {
 		Command command = null;
 		switch (v.getId()) {
 		case R.id.reboot:
 			command = Command.reboot;
 			break;
 		case R.id.reboot_recovery:
 			command = Command.reboot_recovery;
 		case R.id.reboot_bootloader:
 			command = Command.reboot_bootloader;
 			break;
 		case R.id.power_off:
 			command = Command.power_off;
 			break;
 		default:
 			break;
 		}
 		if (command != null) {
 			CommandUtil.exeCommand(command);
 		}
 
 	}
 
 }
