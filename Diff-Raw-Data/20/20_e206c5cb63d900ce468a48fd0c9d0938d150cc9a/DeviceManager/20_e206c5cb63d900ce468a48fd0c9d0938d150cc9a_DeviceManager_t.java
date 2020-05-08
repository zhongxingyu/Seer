 package com.iped.ipcam.gui;
 
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 
 import com.iped.ipcam.engine.CamMagFactory;
 import com.iped.ipcam.engine.ICamManager;
 import com.iped.ipcam.pojo.Device;
 import com.iped.ipcam.utils.CamCmdListHelper;
 import com.iped.ipcam.utils.Constants;
 import com.iped.ipcam.utils.DeviceAdapter;
 
 public class DeviceManager extends ListActivity implements OnClickListener {
 
 	private ListView listView = null;
 	
 	private DeviceAdapter adapter = null;
 	
 	private final int MENU_EDIT = Menu.FIRST;
 	
 	private final int MENU_DEL = Menu.FIRST + 1;
 	
 	private final int MENU_PREVIEW = Menu.FIRST + 2;
 	
 	private Button autoSearchButton = null;
 
 	private Button manulAddButton = null;
 	
 	private Button claerCamButton = null;
 	
 	private ICamManager camManager = null;
 	
 	private ProgressDialog progressDialog = null;
 	
 	private AlertDialog dlg = null;
 	
 	private int lastSelected = 0;
 	
 	private static final String TAG = "DeviceManager";
 	
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message message) {
 			switch (message.what) {
 			case Constants.UPDATEAUTOSEARCH:
 				int value = message.arg1;
 				progressDialog.setProgress(message.arg1);
 				if(value >= 100){
 					hideProgress();
 				}
 				break;
 			case Constants.HIDETEAUTOSEARCH:
 				hideProgress();
 				break;
 			case Constants.UPDATEDEVICELIST:
 				adapter.notifyDataSetChanged();
 				break;
 			case Constants.DEFAULTUSERSELECT:
 				listView.requestFocusFromTouch();
 				listView.setSelection(lastSelected);	
 				break;
 			default:
 				break;
 			}
 		}
 	};
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.device_manager);
 		autoSearchButton = (Button) findViewById(R.id.auto_search_button);
 		manulAddButton = (Button) findViewById(R.id.manul_add_button);
 		claerCamButton = (Button) findViewById(R.id.clear_all_button);
 		camManager = CamMagFactory.getCamManagerInstance();
 		adapter = new DeviceAdapter(camManager.getCamList(), this);
 		listView = getListView();
 		listView.setAdapter(adapter);
 		listView.setOnItemClickListener(itemClickListener);
 		registerForContextMenu(getListView());
 		autoSearchButton.setOnClickListener(this);
 		manulAddButton.setOnClickListener(this);
 		claerCamButton.setOnClickListener(this);
 	}
 	
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		AdapterView.AdapterContextMenuInfo info = null;
 		try{
 			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 		} catch (Exception e) {
 			return;
 		}
 		Device device = camManager.getDevice(info.position);
 		menu.setHeaderTitle(device.getDeviceName());
 		menu.add(0, MENU_EDIT, 1, getString(R.string.device_edit_str));
 		menu.add(0, MENU_DEL, 2, getString(R.string.device_del_str));
 		menu.add(0, MENU_PREVIEW, 3, getString(R.string.device_preview_str));
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo infor = (AdapterContextMenuInfo) item.getMenuInfo();
 		Device device = camManager.getDevice(infor.position);
 		switch (item.getItemId()) {
 		case MENU_EDIT:
 			editDevice(device);
 			break;
 
 		case MENU_DEL:
 			camManager.delCam(infor.position);
 			handler.sendEmptyMessage(Constants.UPDATEDEVICELIST);
 			break;
 		case MENU_PREVIEW:
			WebTabWidget.tabHost.setCurrentTabByTag(Constants.VIDEOPREVIEW);
			Intent intent = new Intent();
			intent.putExtra("IPPLAY", device.getDeviceIp());
			intent.setAction(Constants.ACTION_IPPLAY);
			sendBroadcast(intent);
 			break;
 		default:
 			break;
 		}
 		return super.onContextItemSelected(item);
 		
 	}
 	
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.auto_search_button:
 			showProgress();
 			camManager.startThread(handler);
 			break;
 		case R.id.manul_add_button:
 			addNewDevice();
 			break;
 		case R.id.clear_all_button:
 			camManager.clearCamList();
 			handler.sendEmptyMessage(Constants.UPDATEDEVICELIST);
 		default:
 			break;
 		}
 	}
 	
 	public void test() {
 		byte [] tem = CamCmdListHelper.QueryCmd_Online.getBytes();
 		DatagramSocket datagramSocket = null;
 		try {
 			datagramSocket = new DatagramSocket();
 			datagramSocket.setSoTimeout(Constants.DEVICESEARCHTIMEOUT);
 			DatagramPacket datagramPacket = new DatagramPacket(tem, tem.length, InetAddress.getByName("192.168.1." + 141), Constants.UDPPORT);
 			datagramSocket.send(datagramPacket);
 			DatagramPacket rece = new DatagramPacket(tem, tem.length);
 			datagramSocket.receive(rece);
 			Log.d(TAG, "receive inof   9090909090 ");
 		} catch (SocketException e) {
 			Log.d(TAG, "CamManagerImp isOnline : " + e.getLocalizedMessage());
 		} catch (UnknownHostException e) {
 			Log.d(TAG, "CamManagerImp isOnline : " + e.getLocalizedMessage());
 		} catch (IOException e) {
 			Log.d(TAG, "CamManagerImp isOnline : " + e.getLocalizedMessage());
 		} finally {
 			if(datagramSocket != null) {
 				datagramSocket.close();
 				datagramSocket = null;
 			}
 		}
 	}
 	
 	private void showProgress() {
 		progressDialog = new ProgressDialog(DeviceManager.this);
 		progressDialog.setTitle(getResources().getString(R.string.auto_search_tips_str));
 		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 		progressDialog.setCancelable(false);
 		progressDialog.setProgress(1);
 		progressDialog.show();
 	}
 	
 	public void hideProgress() {
 		if(progressDialog != null && progressDialog.isShowing()) {
 			progressDialog.dismiss();
 		}
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if(keyCode == KeyEvent.KEYCODE_BACK) {
 			hideProgress();
 			camManager.stopThread();
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 	
 	private OnItemClickListener itemClickListener = new OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View arg1, int index,
 				long arg3) {
 			listView.requestFocusFromTouch();
 			lastSelected = index;
 			listView.setSelection(index);
 			camManager.setSelectInde(index);
 		}
 	};
 	
 	private void editDevice(final Device device) {
 		final View addDeviceView = initAddNewDeviceView();
 		final EditText newDeviceNameEditText = (EditText)addDeviceView.findViewById(R.id.device_manager_add_name_id);
 		newDeviceNameEditText.setText(device.getDeviceName());
		final EditText newDeviceIPEditText = (EditText)addDeviceView.findViewById(R.id.device_manager_new_addr_id);
 		newDeviceIPEditText.setText(device.getDeviceIp());
		final EditText newDeviceGatewayEditText = (EditText)addDeviceView.findViewById(R.id.device_manager_new_gateway_addr_id);
 		newDeviceGatewayEditText.setText(device.getDeviceGateWay());
 		//final EditText newDeviceSubnetEditText = (EditText)addDeviceView.findViewById(R.id.device_manager_add_name_id);
 		//newDeviceSubnetEditText.setText(device.get());
 		dlg = new AlertDialog.Builder(DeviceManager.this).setTitle(getResources().getString(R.string.device_manager_add_title_str))
 		.setView(addDeviceView)
 		.setPositiveButton(getResources().getString(R.string.device_manager_add_sure_str), new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				String newDiviceName  = newDeviceIPEditText.getText().toString();
 				String newDiviceIP  = newDeviceIPEditText.getText().toString();
 				if(newDiviceIP== null || newDiviceIP.length() <=0) {
 					unCloseDialog(dlg, R.string.device_manager_add_not_null_str, false);
 				} else {
 					String newDiviceGateway  =  newDeviceGatewayEditText.getText().toString();
 					//String newDiviceSubNet  = ((EditText)addDeviceView.findViewById(R.id.device_manager_new_sub_net_addr_id)).getText().toString();
 					Device deviceNew = new Device(newDiviceName, "IP Camera", newDiviceIP, Constants.TCPPORT, Constants.UDPPORT, newDiviceGateway);
 					//device.setDeviceName(newDiviceName);
 					//device.setDeviceIp(newDiviceIP);
					System.out.println("old=" + device);
					System.out.println("new old=" + deviceNew);
 					 if(camManager.editCam(device, deviceNew)) {
 						 handler.sendEmptyMessage(Constants.UPDATEDEVICELIST);
 						 unCloseDialog(dlg, -1, true);
 					 } else {
 						 unCloseDialog(dlg, R.string.device_manager_add_same_ip_str, false);
 					 }
 				}
 			}
 		})
 		.setNegativeButton(getResources().getString(R.string.device_manager_add_cancle_str), new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				unCloseDialog(dlg, -1, true);
 			}
 		})
 		.create();
 		dlg.show();
 	}
 	
 	
 	private void addNewDevice() {
 		final View addDeviceView = initAddNewDeviceView();
 		dlg = new AlertDialog.Builder(DeviceManager.this).setTitle(getResources().getString(R.string.device_manager_add_title_str))
 		.setView(addDeviceView)
 		.setPositiveButton(getResources().getString(R.string.device_manager_add_sure_str), new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				String newDiviceName  = ((EditText)addDeviceView.findViewById(R.id.device_manager_add_name_id)).getText().toString();
 				String newDiviceIP  = ((EditText)addDeviceView.findViewById(R.id.device_manager_new_addr_id)).getText().toString();
 				if(newDiviceIP== null || newDiviceIP.length() <=0) {
 					unCloseDialog(dlg, R.string.device_manager_add_not_null_str, false);
 				} else {
 					String newDiviceGateway  = ((EditText)addDeviceView.findViewById(R.id.device_manager_new_gateway_addr_id)).getText().toString();
 					//String newDiviceSubNet  = ((EditText)addDeviceView.findViewById(R.id.device_manager_new_sub_net_addr_id)).getText().toString();
 					Device device = new Device(newDiviceName, "IP Camera", newDiviceIP, Constants.TCPPORT, Constants.UDPPORT, newDiviceGateway);
 					 if(camManager.addCam(device)) {
 						 handler.sendEmptyMessage(Constants.UPDATEDEVICELIST);
 						 unCloseDialog(dlg, -1, true);
 					 } else {
 						 unCloseDialog(dlg, R.string.device_manager_add_same_ip_str, false);
 					 }
 				}
 			}
 		})
 		.setNegativeButton(getResources().getString(R.string.device_manager_add_cancle_str), new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				unCloseDialog(dlg, -1, true);
 			}
 		})
 		.create();
 		dlg.show();
 	}
 	
 	private void unCloseDialog(AlertDialog dialog, int id, boolean flag) {
 		if(id>0) {
 			Toast.makeText(DeviceManager.this, getResources().getString(id), Toast.LENGTH_LONG).show();
 		}
 		try {
 			Field field  =  dlg.getClass().getSuperclass().getDeclaredField("mShowing");
 			field.setAccessible( true );
 			field.set(dialog, flag);
 			dialog.dismiss();
 		} catch  (Exception e) {
 			Log.v(TAG, e.getMessage());
 		}		
 	}
 	
 	private View initAddNewDeviceView() {
 		LayoutInflater inflater =  LayoutInflater.from(DeviceManager.this);
 		View addDeviceView =  inflater.inflate(R.layout.device_manager_add, null);
 		((EditText)addDeviceView.findViewById(R.id.device_manager_new_tcp_port_id)).setText(Constants.TCPPORT+"");
 		((EditText)addDeviceView.findViewById(R.id.device_manager_new_udp_port_id)).setText(Constants.UDPPORT+"");
 		return addDeviceView;
 	}
 	
 	protected void onResume() {
 		super.onResume();
 		handler.sendMessageDelayed(handler.obtainMessage(Constants.DEFAULTUSERSELECT), 50);
 	};
 }
