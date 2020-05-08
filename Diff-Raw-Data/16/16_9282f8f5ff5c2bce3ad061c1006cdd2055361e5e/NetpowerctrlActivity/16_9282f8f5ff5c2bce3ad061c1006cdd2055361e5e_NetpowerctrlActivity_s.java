 package oly.netpowerctrl;
 
 import java.util.ArrayList;
 import java.util.UUID;
 
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.app.TabActivity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.content.LocalBroadcastManager;
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
 
 @SuppressWarnings("deprecation")
 public class NetpowerctrlActivity extends TabActivity implements OnItemClickListener, DeviceConfigureEvent {
 
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
     	adpConfiguredDevices = new DeviceListAdapter(this, alConfiguredDevices);
   		lvConfiguredDevices.setAdapter(adpConfiguredDevices);
 
     	alDiscoveredDevices = new ArrayList<DeviceInfo>();
     	adpDiscoveredDevices = new DeviceListAdapter(this, alDiscoveredDevices);
   		lvDiscoveredDevices.setAdapter(adpDiscoveredDevices);
 
         ReadConfiguredDevices();
 
         lvConfiguredDevices.setOnItemClickListener(this);
         lvDiscoveredDevices.setOnItemClickListener(this);
         
         registerForContextMenu(lvConfiguredDevices);
         registerForContextMenu(lvDiscoveredDevices);
         
         adpConfiguredDevices.setDeviceConfigureEvent(this);
         adpDiscoveredDevices.setDeviceConfigureEvent(this);
         
     }
     
     /*
     @Override
     protected void onDestroy() {
     	for (DiscoveryThread thr: discoveryThreads)
     		thr.interrupt();
     	discoveryThreads.clear();
     	
     	super.onDestroy();
     }
     */
     
     @Override
     protected void onResume() {
     	super.onResume();
     	
     	IntentFilter itf= new IntentFilter(DiscoveryThread.BROADCAST_DEVICE_DISCOVERED);
         LocalBroadcastManager.getInstance(this).registerReceiver(onDeviceDiscovered, itf);
     	
     	DeviceQuery.sendBroadcastQuery(this);
     }
     
     @Override
     protected void onPause() {
     	LocalBroadcastManager.getInstance(this).unregisterReceiver(onDeviceDiscovered);
     	super.onPause();
 	}
     
     
 	@SuppressLint("NewApi")
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
     	super.onCreateOptionsMenu(menu);
 		menu.add(0, R.id.menu_add_device, 0, R.string.menu_add_device).setIcon(R.drawable.ic_menu_add);
 		menu.add(0, R.id.menu_delete_all_devices, 0, R.string.menu_delete_all).setIcon(R.drawable.ic_menu_delete);
 		menu.add(0, R.id.menu_requery, 0, R.string.requery).setIcon(R.drawable.ic_menu_refresh);
 		menu.add(0, R.id.menu_preferences, 0, R.string.menu_preferences).setIcon(R.drawable.ic_menu_preferences);
 		menu.add(0, R.id.menu_about, 0, R.string.menu_about).setIcon(R.drawable.ic_menu_info_details);
 		if (Build.VERSION.SDK_INT >= 11) {
 			menu.findItem(R.id.menu_add_device).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 			menu.findItem(R.id.menu_requery).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		}
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
 	    	DeviceQuery.sendBroadcastQuery(this);
 			return true;
 		}
 		
 		case R.id.menu_preferences: {
 			Intent it = new Intent(this, Preferences.class);
 			startActivityForResult(it, R.id.request_code_preferences);
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
 	    	String device_mac = prefs.getString("setting_device_mac", "");
 	    	boolean nonstandard_ports = prefs.getBoolean("setting_nonstandard_ports", false);
 	        int send_udp = DeviceQuery.getDefaultSendPort(this);
 	        int recv_udp = DeviceQuery.getDefaultRecvPort(this);
 	        try { send_udp = Integer.parseInt(prefs.getString("setting_send_udp", "")); } catch (NumberFormatException e) { /*nop*/ }
 	        try { recv_udp = Integer.parseInt(prefs.getString("setting_recv_udp", "")); } catch (NumberFormatException e) { /*nop*/ }
 			String username = prefs.getString("setting_username", "");
 			String password = prefs.getString("setting_password", "");
 	    
 			DeviceInfo device_info;
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
 		        UUID uuid = (UUID) data.getExtras().get("uuid");
 		        device_info = adpConfiguredDevices.findDevice(uuid);
 		        if (device_info == null)
 			        device_info = adpDiscoveredDevices.findDevice(uuid);
 		        if (device_info == null) {
 					Toast.makeText(getBaseContext(), getResources().getText(R.string.error_edited_device_not_found), Toast.LENGTH_SHORT).show();
 					return;
 		        }
 		        	
 			}
 				
 			device_info.DeviceName = device_name;
 			device_info.HostName = device_ip;
 			device_info.MacAddress = device_mac;
 			device_info.UserName = username;
 			device_info.Password = password;
 			device_info.DefaultPorts = ! nonstandard_ports;
 			if (nonstandard_ports) {
 				device_info.SendPort = send_udp;
 				device_info.RecvPort = recv_udp;
 			} else {
 				device_info.SendPort = DeviceQuery.getDefaultSendPort(this);
 				device_info.RecvPort = DeviceQuery.getDefaultRecvPort(this);
 			}
 			device_info.Outlets.clear();
 			for (int i=0; i<prefs.getInt("num_outlets", 0); i++) {
 				OutletInfo oi = new OutletInfo();
 				oi.Description = prefs.getString(String.format("outlet_name%d",i), "?");
 				oi.OutletNumber = prefs.getInt(String.format("outlet_number%d",i), 1);
 				device_info.Outlets.add(oi);
 			}
 
 			if (requestCode == R.id.request_code_new_device) {
 				alConfiguredDevices.add(device_info);
 				adpConfiguredDevices.getFilter().filter("");
 			} else {
 				adpConfiguredDevices.notifyDataSetChanged();
 				adpDiscoveredDevices.notifyDataSetChanged();
 			}
 			SaveConfiguredDevices();
 		}
 		
 		
 		if (requestCode == R.id.request_code_preferences) {
 			// update devices that are configured to use default ports
 	        int send_udp = DeviceQuery.getDefaultSendPort(this);
 	        int recv_udp = DeviceQuery.getDefaultRecvPort(this);
   			for (DeviceInfo di: alConfiguredDevices) {
   				if (di.DefaultPorts) {
   					di.SendPort = send_udp;
   					di.RecvPort = recv_udp;
   				}
   			}
   			SaveConfiguredDevices();
 		}
 
 	}
 	
     public void ReadConfiguredDevices() {
     	alConfiguredDevices = SharedPrefs.ReadConfiguredDevices(this);
     	adpConfiguredDevices.setDevices(alConfiguredDevices);
         adpConfiguredDevices.getFilter().filter("");
         ((AppMain)getApplicationContext()).restartDiscoveryThreads(this);
     }
     
     public void SaveConfiguredDevices() {
     	SharedPrefs.SaveConfiguredDevices(alConfiguredDevices, this);
         ((AppMain)getApplicationContext()).restartDiscoveryThreads(this);
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
 	  		DeviceInfo di = (DeviceInfo) adpConfiguredDevices.getItem(info.position);
 			onConfigureDevice(di);
 			return true;
   		}
 
   	    case R.id.menu_edit_discovered_device: {
 	  		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 	  		DeviceInfo di = (DeviceInfo) adpDiscoveredDevices.getItem(info.position);
 			onConfigureDevice(di);
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
 
 	public void onConfigureDevice(DeviceInfo device_info) {
 		Intent it = new Intent(this, DevicePreferences.class);
 		it.putExtra("new_device", false);
 		it.putExtra("device_info", device_info);
 		startActivityForResult(it, R.id.request_code_modify_device);
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
 	
 	
 	private BroadcastReceiver onDeviceDiscovered= new BroadcastReceiver() {
 	    @Override
	    public void onReceive(Context context, Intent intent) {
 	    	DeviceInfo device_info = null;
 			Bundle extra = intent.getExtras();
 			if (extra != null) {
 				Object o = extra.get("device_info");
 				if (o != null) {
 					device_info = (DeviceInfo) o; 
 				}
 			}
 			if (device_info == null)
 				return;
 
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
 				if (device_info.MacAddress.equals(di.MacAddress)) {
 					updateOutletInfo(di, device_info);
 					break;
 				}
 			}
 			
 			// if it's visible in the listView, flash it
 			flashGreen(device_info.MacAddress, lvConfiguredDevices, adpConfiguredDevices);
 			flashGreen(device_info.MacAddress, lvDiscoveredDevices, adpDiscoveredDevices);
 	    }
 	    
 	    public void flashGreen(String macAddress, ListView lstv, DeviceListAdapter adapter) {
 			if (Build.VERSION.SDK_INT >= 11) {
 				for (int i=0; i<lstv.getChildCount(); i++) {
 					View child = lstv.getChildAt(i);
 					if (child != null) {
 						DeviceInfo di = (DeviceInfo)adapter.getItem((Integer)child.getTag());
 						if (di.MacAddress.equals(macAddress)) {
 							GreenFlasher.flashBgColor(child);
 						}
 					}	
 				}
 			}
 	    }
 	};
 
 	
 }
