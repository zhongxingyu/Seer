 package oly.netpowerctrl;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.TabActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TabHost;
 import android.widget.Toast;
 
 public class NetpowerctrlActivity extends TabActivity implements OnItemClickListener, DeviceConfigureEvent, DeviceFoundEvent {
 
 	DiscoveryThread discoveryThread = null;
 	
 	ListView lvConfiguredDevices;
 	ListView lvDiscoveredDevices;
 	
 	ArrayList<DeviceInfo> alConfiguredDevices;
 	ArrayList<DeviceInfo> alDiscoveredDevices;
 	DeviceListAdapter adpConfiguredDevices;
 	DeviceListAdapter adpDiscoveredDevices;
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         TabHost th = (TabHost)findViewById(android.R.id.tabhost);
         th.setup();
         th.addTab(th.newTabSpec("conf").setIndicator(getResources().getString(R.string.configured_devices)).setContent(R.id.lvConfiguredDevices));
         th.addTab(th.newTabSpec("found").setIndicator(getResources().getString(R.string.discovered_devices)).setContent(R.id.lvDiscoveredDevices));
 
   		lvConfiguredDevices = (ListView)findViewById(R.id.lvConfiguredDevices);
   		lvDiscoveredDevices = (ListView)findViewById(R.id.lvDiscoveredDevices);
 
     	alConfiguredDevices = new ArrayList<DeviceInfo>();
     	adpConfiguredDevices = new DeviceListAdapter(this, alConfiguredDevices, ConfType.ConfiguredDevice);
   		lvConfiguredDevices.setAdapter(adpConfiguredDevices);
 
     	alDiscoveredDevices = new ArrayList<DeviceInfo>();
     	adpDiscoveredDevices = new DeviceListAdapter(this, alDiscoveredDevices, ConfType.DiscoveredDevice);
   		lvDiscoveredDevices.setAdapter(adpDiscoveredDevices);
 
         ReadConfiguredDevices();
 
         lvConfiguredDevices.setOnItemClickListener(this);
         lvDiscoveredDevices.setOnItemClickListener(this);
         
         registerForContextMenu(lvConfiguredDevices);
         registerForContextMenu(lvDiscoveredDevices);
         
         adpConfiguredDevices.setDeviceConfigureEvent(this);
         adpDiscoveredDevices.setDeviceConfigureEvent(this);
         
 		discoveryThread = null;
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	if (discoveryThread == null) {
 	    	discoveryThread = new DiscoveryThread(this, this);
 	    	discoveryThread.start();
     	}
     	sendQuery();
     }
     
     @Override
     protected void onPause() {
     	super.onPause();
     	if (discoveryThread != null) {
     		discoveryThread.interrupt();
     		discoveryThread = null;
     	}
 	}
     
     @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, R.id.menu_add_device, 0, R.string.menu_add_device).setIcon(android.R.drawable.ic_menu_add);
 		menu.add(0, R.id.menu_delete_all_devices, 0, R.string.menu_delete_all).setIcon(android.R.drawable.ic_menu_delete);
 		menu.add(0, R.id.menu_requery, 0, R.string.requery).setIcon(android.R.drawable.ic_menu_compass);
 		menu.add(0, R.id.menu_about, 0, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_add_device: {
 			Intent it = new Intent(this, DevicePreferences.class);
 			it.putExtra("new_device", true);
 			startActivityForResult(it, R.id.request_code_new_device);
 			return true;
 		}
 		
 		case R.id.menu_delete_all_devices: {
 			new AlertDialog.Builder(this)
 				.setTitle(R.string.delete_all_devices)
 				.setMessage(R.string.confirmation_delete_all_devices)
 				.setIcon(android.R.drawable.ic_dialog_alert)
 				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog, int whichButton) {
 				    	deleteAllDevices();
 				    }})
 				 .setNegativeButton(android.R.string.no, null).show();
 			return true;
 		}
 		
 		case R.id.menu_requery: {
 			sendQuery();
 			return true;
 		}
 		
 		case R.id.menu_about: {
 			AboutDialog about = new AboutDialog(this);
 			about.setTitle(R.string.app_name);
 			about.show();
 			return true;
 		}
 		}
 		return false;
 	}
 	
 	@Override
 	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
 		if (resultCode == RESULT_CANCELED)
 			return;
 		
 		if ((requestCode == R.id.request_code_new_device) || (requestCode == R.id.request_code_modify_device)) {
 	        String prefName = data.getExtras().getString("SharedPreferencesName");
 	        SharedPreferences prefs = getSharedPreferences(prefName, MODE_PRIVATE);
 	    	String device_name = prefs.getString("setting_device_name", "ERROR");
 	    	String device_ip = prefs.getString("setting_device_ip", "");
 	    	boolean standard_ports = prefs.getBoolean("setting_standard_ports", false);
 	        int default_send_port = getResources().getInteger(R.integer.default_send_port);
 	        int default_recv_port = getResources().getInteger(R.integer.default_send_port);
 	    	int send_udp = Integer.getInteger(prefs.getString("setting_send_udp", ""), default_send_port);
 	    	int recv_udp = Integer.getInteger(prefs.getString("setting_recv_udp", ""), default_recv_port);
 			String username = prefs.getString("setting_username", "");
 			String password = prefs.getString("setting_password", "");
 	    
 			DeviceInfo device_info;
 			DeviceConfigureEvent.ConfType conf_type = null;
 			if (requestCode == R.id.request_code_new_device) {
 				if ((device_name.equals("")) &&
 					(device_ip.equals("")) &&
 					(username.equals("")) &&
 					(password.equals(""))) {
 					// editing was cancelled by user
 					return;
 				} else {
 					device_info = new DeviceInfo(this);
 				}
 			} else {
 				// requestCode == edit device
 		        int position = data.getExtras().getInt("position");
 		        conf_type = (DeviceConfigureEvent.ConfType)data.getExtras().get("configure_type");
 		        if (conf_type == DeviceConfigureEvent.ConfType.ConfiguredDevice)
 		        	device_info = (DeviceInfo)adpConfiguredDevices.getItem(position);
 		        else device_info = (DeviceInfo)adpDiscoveredDevices.getItem(position);
 			}
 				
 			device_info.DeviceName = device_name;
 			device_info.HostName = device_ip;
 			device_info.UserName = username;
 			device_info.Password = password;
 			if (standard_ports) {
 				device_info.SendPort = getResources().getInteger(R.integer.default_send_port);
 				device_info.RecvPort = getResources().getInteger(R.integer.default_recv_port);
 			} else {
 				device_info.SendPort = send_udp;
 				device_info.RecvPort = recv_udp;
 			}
 
 			if (requestCode == R.id.request_code_new_device) {
 				alConfiguredDevices.add(device_info);
 				adpConfiguredDevices.getFilter().filter("");
 			} else {
 				if (conf_type == DeviceConfigureEvent.ConfType.ConfiguredDevice)
 					adpConfiguredDevices.notifyDataSetChanged();
 				else adpDiscoveredDevices.notifyDataSetChanged();
 			}
 			SaveConfiguredDevices();
 		}
 	}
 	
     public void ReadConfiguredDevices() {
     	alConfiguredDevices.clear();
 
 		SharedPreferences prefs = getSharedPreferences("oly.netpowerctrl", MODE_PRIVATE);
 		String configured_devices_str = prefs.getString("configured_devices", "[]");
   		try {
 			JSONArray jdevices = new JSONArray(configured_devices_str);
 			
 			for (int i=0; i<jdevices.length(); i++) {
 				JSONObject jhost = jdevices.getJSONObject(i);
 				DeviceInfo di = new DeviceInfo(this);
 				di.DeviceName = jhost.getString("name");
 				di.HostName = jhost.getString("ip");
 				di.UserName= jhost.getString("username");
 				di.Password = jhost.getString("password");
 				di.SendPort = jhost.getInt("sendport");
 				di.RecvPort = jhost.getInt("recvport");
 				di.Outlets = new ArrayList<OutletInfo>();
 
 				JSONArray joutlets = jhost.getJSONArray("outlets");
 				for (int j=0; j<joutlets.length(); j++) {
 					JSONObject joutlet = joutlets.getJSONObject(j);
 					OutletInfo oi = new OutletInfo();
 					oi.OutletNumber = joutlet.getInt("number");
 					oi.Description = joutlet.getString("description");
 					di.Outlets.add(oi);
 				}
 
 				alConfiguredDevices.add(di);
 			}
 		}
 		catch (JSONException e) {
 			Toast.makeText(getBaseContext(), getResources().getText(R.string.error_reading_configured_devices) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
 		}
         adpConfiguredDevices.getFilter().filter("");
     }
     
     public void SaveConfiguredDevices() {
     	JSONArray jdevices = new JSONArray();
   		try {
   			for (DeviceInfo di: alConfiguredDevices) {
 				JSONObject jhost = new JSONObject();
 				jhost.put("name", di.DeviceName);
 				jhost.put("ip", di.HostName);
 				jhost.put("username", di.UserName);
 				jhost.put("password", di.Password);
 				jhost.put("sendport", di.SendPort);
 				jhost.put("recvport", di.RecvPort);
 
 				JSONArray joutlets = new JSONArray();
 	  			for (OutletInfo oi: di.Outlets) {
 					JSONObject joutlet = new JSONObject();
 					joutlet.put("number", oi.OutletNumber);
 					joutlet.put("description", oi.Description);
 					joutlets.put(joutlet);
 				}
 	  			jhost.put("outlets", joutlets);
 	  			jdevices.put(jhost);
   			}
 		}
 		catch (JSONException e) {
 			Toast.makeText(getBaseContext(), getResources().getText(R.string.error_saving_configured_devices) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
 			return;
 		}
 		SharedPreferences prefs = getSharedPreferences("oly.netpowerctrl", MODE_PRIVATE);
 		SharedPreferences.Editor prefEditor = prefs.edit();
 		prefEditor.putString("configured_devices", jdevices.toString());
 		prefEditor.commit();
     }
     
   	@Override
   	public void onCreateContextMenu(ContextMenu cm, View v, ContextMenuInfo cmi) {
   		super.onCreateContextMenu(cm, v, cmi);
   	    MenuInflater inflater = getMenuInflater();
   	    
   	    if (v == lvConfiguredDevices)
   	    	inflater.inflate(R.menu.configured_device_menu, cm);
   	    else
   	    	inflater.inflate(R.menu.discovered_device_menu, cm);
 
   	}
 
   	@Override
   	public boolean onContextItemSelected(MenuItem item) {
   	    switch (item.getItemId()) {
   	    case R.id.menu_edit_device: {
 	  		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 			onConfigureDevice(DeviceConfigureEvent.ConfType.ConfiguredDevice, info.position);
 			return true;
   		}
 
   	    case R.id.menu_edit_discovered_device: {
 	  		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 			onConfigureDevice(DeviceConfigureEvent.ConfType.DiscoveredDevice, info.position);
 			return true;
   		}
 
   	    case R.id.menu_delete_device: {
 	  		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 			new AlertDialog.Builder(this)
 				.setTitle(R.string.delete_device)
 				.setMessage(R.string.confirmation_delete_device)
 				.setIcon(android.R.drawable.ic_dialog_alert)
 				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog, int whichButton) {
 				    	deleteDevice(info.position);
 				    }})
 				 .setNegativeButton(android.R.string.no, null).show();
 			return true;
   		}
   	    
   	    case R.id.menu_copy_device: {
 	  		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 	  		DeviceInfo new_device = new DeviceInfo(alConfiguredDevices.get(info.position));
 	  		new_device.DeviceName = String.format(getResources().getString(R.string.copy_of), new_device.DeviceName);
 	  		alConfiguredDevices.add(new_device);
 	  		SaveConfiguredDevices();
 	  		adpConfiguredDevices.getFilter().filter("");
 			return true;
   		}
   	    
   	    case R.id.menu_add_to_configured_devices: {
 	  		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 	  		DeviceInfo new_device = new DeviceInfo(alDiscoveredDevices.get(info.position));
   	    	alConfiguredDevices.add(new_device);
 	  		SaveConfiguredDevices();
 	  		adpConfiguredDevices.getFilter().filter("");
 			Toast.makeText(getBaseContext(), R.string.suggest_enter_username_password, Toast.LENGTH_LONG).show();
   	    }
   	    
   	    default:
   	    	return super.onContextItemSelected(item);
   	    }
   	}
 
 	@Override
 	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
 		Object o = av.getItemAtPosition(position);
 		if (o != null) {
 			DeviceInfo di = (DeviceInfo)o;
 			if ((av == lvDiscoveredDevices) && (di.UserName.equals("")) && (di.Password.equals("")))
 				Toast.makeText(getBaseContext(), R.string.suggest_enter_username_password, Toast.LENGTH_LONG).show();
 			
 			Intent it = new Intent(this, DeviceControl.class);
 			it.putExtra("device", di);
 			startActivity(it);
 		}
 	}
 
 	@Override
 	public void onConfigureDevice(ConfType type, int position) {
 		
 		Object o = null;
 		if (type == ConfType.ConfiguredDevice) 
 			o = adpConfiguredDevices.getItem(position);
 		else o = adpDiscoveredDevices.getItem(position);
 		
 		if (o != null) {
 			DeviceInfo di = (DeviceInfo)o;
 			Intent it = new Intent(this, DevicePreferences.class);
 			it.putExtra("new_device", false);
 			it.putExtra("configure_type", type);
 			it.putExtra("position", position);
 			it.putExtra("device_info", di);
 			startActivityForResult(it, R.id.request_code_modify_device);
 		}
 	}    
 	
 	public void deleteDevice(int position) {
   		alConfiguredDevices.remove(position);
   		SaveConfiguredDevices();
   		adpConfiguredDevices.getFilter().filter("");
 	}
 	
 	public void deleteAllDevices() {
   		alConfiguredDevices.clear();
   		SaveConfiguredDevices();
   		adpConfiguredDevices.getFilter().filter("");
 	}
 
 	@Override
 	public void onDeviceFound(DeviceInfo device_info) {
 		// we may have this one in the list already
 		boolean found = false;
 		for (DeviceInfo di: alDiscoveredDevices) {
 			if ((device_info.DeviceName.equals(di.DeviceName)) && (device_info.HostName.equals(di.HostName))) {
 				found = true;
 				updateOutletInfo(di, device_info);
 				break;
 			}
 		}
 		
 		if (!found) {
 			alDiscoveredDevices.add(device_info);
 	  		adpDiscoveredDevices.getFilter().filter("");
 		}
 		
 		// if it matches a configured device, update it's outlet states
 		for (DeviceInfo di: alConfiguredDevices) {
 			if (device_info.HostName.equals(di.HostName)) {
 				updateOutletInfo(di, device_info);
 				break;
 			}
 		}
 	}
 	
 	public void updateOutletInfo(DeviceInfo target, DeviceInfo src) {
 		for (OutletInfo srcoi: src.Outlets) {
 			for (OutletInfo tgtoi: target.Outlets) {
 				if (tgtoi.OutletNumber == srcoi.OutletNumber) {
 					tgtoi.State = srcoi.State;
 					break;
 				}
 			}
 		}
 	}
 	
 	public void sendQuery() {
 		final Activity self = this;
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 			        String messageStr="wer da?\r\n";
 			        int port = getResources().getInteger(R.integer.default_send_port); //TODO: make configurable
 			        DatagramSocket s = new DatagramSocket();
 					s.setBroadcast(true);
 					InetAddress host = InetAddress.getByName("255.255.255.255"); //TODO: make configurable
 			        int msg_length=messageStr.length();
 			        byte[] message = messageStr.getBytes();
 			        DatagramPacket p = new DatagramPacket(message, msg_length, host, port);
 					s.send(p);
 			        s.close();
 				} catch (final IOException e) {
 					runOnUiThread(new Runnable() {
 					    public void run() {
 					    	Toast.makeText(self, getResources().getString(R.string.error_sending_inquiry) +": "+ e.getMessage(), Toast.LENGTH_LONG).show();
 					    }
 					});
 				}
 			}
 		}).start();
 	}
 	
 	
 }
