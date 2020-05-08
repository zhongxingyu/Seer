 package de.tudresden.inf.rn.mobilis.friendfinder;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.TwoLineListItem;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 
 import de.tudresden.inf.rn.mobilis.friendfinder.clientstub.ClientData;
 import de.tudresden.inf.rn.mobilis.friendfinder.clientstub.ClientLocation;
 import de.tudresden.inf.rn.mobilis.friendfinder.clientstub.IXMPPCallback;
 import de.tudresden.inf.rn.mobilis.friendfinder.clientstub.JoinServiceResponse;
 import de.tudresden.inf.rn.mobilis.friendfinder.proxy.MXAProxy;
 import de.tudresden.inf.rn.mobilis.friendfinder.service.BackgroundService;
 import de.tudresden.inf.rn.mobilis.friendfinder.service.ICallback;
 import de.tudresden.inf.rn.mobilis.friendfinder.service.ServiceConnector;
 import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
 import de.tudresden.inf.rn.mobilis.mxa.ConstMXA.MessageItems;
 import de.tudresden.inf.rn.mobilis.mxa.MXAController;
 import de.tudresden.inf.rn.mobilis.mxa.activities.PreferencesClient;
 import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
 import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.CreateNewServiceInstanceBean;
 import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.MobilisServiceDiscoveryBean;
 import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.MobilisServiceInfo;
 
 /**
  * the mainactivity is the entrypoint of the app
  */
 public class MainActivity extends Activity implements ICallback,
 		OnClickListener, OnItemClickListener,
 		JoinServiceInstanceDialog.SettingsDialogListener,
 		NewServiceInstanceDialog.NewServiceDialogListener {
 
 	public static final String TAG = "MainActivity";
 
 	/**
 	 * the backgroundservice holds the communication- and tracking-classes 
 	 */
 	protected BackgroundService mService;
 	
 	/**
 	 * GUI Elements
 	 */
 	private Button btn_create;
 	private Button btn_discover;
 	private Button btn_settings;
 	private ListView service_list;
 
 	/**
 	 * the list of available services at the server
 	 */
 	private ArrayList<ServiceInstanceData> services;
 	
 	/**
 	 * an adapter to show the services-list in the service_list view
 	 */
 	private ServiceInstanceDataAdapter adapter;
 
 	/**
 	 * connect the backgroundservice
 	 */
 	private ServiceConnector mServiceConnector;
 	/**
 	 * shown, when the app wait for connecting the xmpp-server or for mobilis-requests
 	 */
 	private ProgressDialog progressDialog;
 	/**
 	 * dialog for inform the user about the choosen service and input his username
 	 */
 	private JoinServiceInstanceDialog settingsDialog;
 	/**
 	 * dialog to input the name of the new service
 	 */
 	private NewServiceInstanceDialog newServiceInstanceDialog;
 	/**
 	 * temporary servicename
 	 */
 	private String tempServiceJID;
 
 	/**************** Handler *********************/
 	
 	/**
 	 * called, if the service-discovery finished
 	 */
 	private Handler mServiceDiscoveryHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			if (msg.what == 1) {
 				services.clear();
 				services.addAll((ArrayList<ServiceInstanceData>) msg.obj);
 				adapter.notifyDataSetChanged();
 			} else
 				Toast.makeText(MainActivity.this,
 						R.string.toast_svc_discover_err,
 						Toast.LENGTH_LONG).show();
 		}
 	};
 
 	/**
 	 * inform the user about the status of the created service and start service-discovery
 	 */
 	private Handler mServiceCreateHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case -1:
 				Toast.makeText(MainActivity.this,
 						R.string.toast_svc_create_err,
 						Toast.LENGTH_LONG).show();
 				break;
 			case 1:
 				Toast.makeText(MainActivity.this,
 						R.string.toast_svc_create_succ,
 						Toast.LENGTH_LONG).show();
 				break;
 			}
 			sendDiscoveryIQ();
 			if (progressDialog != null)
 				progressDialog.dismiss();
 		}
 	};
 
 	/**
 	 * this handler was called, if the service was bound successfully
 	 */
 	private Handler onServiceBoundHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			Log.v(TAG, "onServiceBoundHandler");
 
 			mService = mServiceConnector.getService();
 			mService.setCallbackClass(MainActivity.this);
 
 			if (!MXAController.get().checkSetupDone()) {
 				showMXASettingsDialog();
 			} else {
 				if(!mService.getMXAProxy().isConnected()){
 					progressDialog.setMessage("Connect to XMPP...");
 					connectToXMPP();
 				} else {
 					progressDialog.hide();
 				}
 			}
 		}
 	};
 
 	/**
 	 * called, if the connection to the xmpp-server is established
 	 */
 	private Handler onXMPPConnectHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			Log.v(TAG, "onXMPPConnectHandler");
 
 			mServiceConnector.getService().getIQProxy().registerCallbacks();
 			sendDiscoveryIQ();
 
 			btn_create.setEnabled(true);
 			btn_discover.setEnabled(true);
 			btn_create.setVisibility(View.VISIBLE);
 			btn_discover.setVisibility(View.VISIBLE);
 
 			progressDialog.hide();
 		}
 	};
 
 	/**
 	 * called, if the response of the joinservicerequest was received
 	 */
 	IXMPPCallback<JoinServiceResponse> onJoinService = new IXMPPCallback<JoinServiceResponse>() {
 		@Override
 		public void invoke(JoinServiceResponse xmppBean) {
 			Log.v(TAG, "onJoinService()");
 
 			BackgroundService service = mServiceConnector.getService();
 			service.setMucJID(xmppBean.getMucJID());
 			service.setMucPwd(xmppBean.getMucPwd());
 			service.setServiceJID(xmppBean.getFrom());
 			MXAProxy mxaProxy = mServiceConnector.getService().getMXAProxy();
 			ClientData cd = service.getClientData();
 
 			cd.setJid(mxaProxy.getXmppJid());
 			cd.setName(mxaProxy.getNickname());
 			cd.setColor(xmppBean.getColor());
 			cd.setClientLocation(new ClientLocation());
 
 			getContentResolver().delete(MessageItems.contentUri, "_id != -1",
 					null);
 			
 			service.getClientDataList().put(cd.getJid(), cd);
 
 			Intent intent2 = new Intent(MainActivity.this, MUCActivity.class);
 			startActivity(intent2);
 		}
 	};
 
 	/************************ Activity Methods **********************/
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		MXAController.get().setSharedPreferencesName(MainActivity.this,
 				"de.tudresden.inf.rn.mobilis.friendfinder.mxa");
 		if (!MXAController.get().checkSetupDone()) {
			//MXAController.get().setPort("5222");
 			MXAController.get().setHost("joyo.diskstation.org");
 			MXAController.get().setService("");
 			MXAController.get().setResource("MXA");
 			MXAController.get().setService("FriendFinder");
 
 			// temp
 			//MXAController.get().setUsername("android1");
 			//MXAController.get().setPassword("android11234");
 		}
 		Log.d(TAG, "onCreate");
 
 		mServiceConnector = new ServiceConnector(this);
 		initComponents();
 		isPlayServiceConnected();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			Intent i = new Intent(ConstMXA.INTENT_PREFERENCES);
 			startActivity(i);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onStart() {
 		Log.d(TAG, "onStart");
 
 		// if (!mServiceConnector.isServiceBound()){
 		mServiceConnector.bindService(onServiceBoundHandler);
 		progressDialog = ProgressDialog.show(this, "",
 				"Bind Service...", true);
 		progressDialog.setCancelable(true);
 		// }
 		super.onStart();
 	}
 
 	@Override
 	public void onStop() {
 		Log.d(TAG, "onStop");
 
 		if (progressDialog != null)
 			progressDialog.dismiss();
 		if (settingsDialog != null && settingsDialog.isAdded()) {
 			settingsDialog.dismiss();
 		}
 		if (newServiceInstanceDialog != null
 				&& newServiceInstanceDialog.isAdded()) {
 			newServiceInstanceDialog.dismiss();
 		}
 
 		super.onStop();
 	}
 
 	@Override
 	public void onDestroy() {
 		Log.d(TAG, "onDestroy");
 
 		// If local Service is up, unregister all IQ-Listeners and stop the
 		// local Service
 		if (mServiceConnector != null && mServiceConnector.getService() != null) {
 
 			BackgroundService service = mServiceConnector.getService();
 			service.getIQProxy().unregisterCallbacks();
 			service.stopSelf();
 			mServiceConnector.unbindService();
 		}
 		super.onDestroy();
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		if (arg0 != null)
 			switch (arg0.getId()) {
 			case R.id.main_btn_create:
 				String serviceName = "FriendFinder";
 
 				newServiceInstanceDialog.editNameString = serviceName;
 				newServiceInstanceDialog.show(
 						MainActivity.this.getFragmentManager(),
 						"NewServiceInstanceDialog");
 				return;
 			case R.id.main_btn_settings:
 				Intent i = new Intent(ConstMXA.INTENT_PREFERENCES);
 				startActivity(i);
 				return;
 			case R.id.main_btn_discover:
 				sendDiscoveryIQ();
 				return;
 			}
 
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		this.tempServiceJID = services.get(arg2).jid;
 
 		settingsDialog.serviceInfoString = "JID: " + tempServiceJID;
 		settingsDialog.editNameString = mServiceConnector.getService()
 				.getMXAProxy().getNickname();
 		settingsDialog.show(MainActivity.this.getFragmentManager(),
 				"JoinServiceInstanceDialog");
 	}
 
 	/****************** class-specific functions ************************/
 
 	/**
 	 * init the gui-components
 	 */
 	protected void initComponents() {
 		btn_create = (Button) findViewById(R.id.main_btn_create);
 		btn_create.setEnabled(false);
 		btn_create.setOnClickListener(this);
 		btn_create.setVisibility(View.INVISIBLE);
 
 		btn_discover = (Button) findViewById(R.id.main_btn_discover);
 		btn_discover.setEnabled(false);
 		btn_discover.setOnClickListener(this);
 		btn_discover.setVisibility(View.INVISIBLE);
 
 		btn_settings = (Button) findViewById(R.id.main_btn_settings);
 		btn_settings.setOnClickListener(this);
 
 		services = new ArrayList<ServiceInstanceData>();
 		service_list = (ListView) findViewById(R.id.main_listView);
 		adapter = new ServiceInstanceDataAdapter(this,
 				android.R.layout.simple_list_item_1, services);
 		adapter.setNotifyOnChange(true);
 		service_list.setAdapter(adapter);
 		service_list.setOnItemClickListener(this);
 
 		settingsDialog = new JoinServiceInstanceDialog();
 		settingsDialog.setCancelable(true);
 
 		newServiceInstanceDialog = new NewServiceInstanceDialog();
 		newServiceInstanceDialog.setCancelable(true);
 	}
 
 	/**
 	 * connect to the xmpp-server
 	 */
 	protected void connectToXMPP() {
 		MXAProxy mxap = mService.getMXAProxy();
 		mxap.registerXMPPConnectHandler(onXMPPConnectHandler);
 		try {
 			mxap.connect();
 		} catch (RemoteException e) {
 			Log.e(TAG, "connectToXMPP", e);
 		}
 	}
 
 	/**
 	 * send a discovery-iq to the mobilis server
 	 */
 	protected void sendDiscoveryIQ() {
 		mServiceConnector
 				.getService()
 				.getIQProxy()
 				.sendServiceDiscoveryIQ(
 						mServiceConnector.getService().getServiceNamespace());
 	}
 
 	/**
 	 * implements the interface ICallback
 	 * process the incoming packets from the discovery- and createservce-requests
 	 */
 	@Override
 	public void processPacket(XMPPBean inBean) {
 		if (inBean.getType() == XMPPBean.TYPE_ERROR) {
 			Log.e(TAG, "IQ Type ERROR: " + inBean.toXML());
 		}
 
 		if (inBean instanceof MobilisServiceDiscoveryBean) {
 			MobilisServiceDiscoveryBean bean = (MobilisServiceDiscoveryBean) inBean;
 
 			// If responded MobilisServiceDiscoveryBean is not of kind ERROR,
 			// check Mobilis-Server response for XHunt support
 			if (bean != null && bean.getType() != XMPPBean.TYPE_ERROR) {
 				if (bean.getDiscoveredServices() != null) {
 					ArrayList<ServiceInstanceData> services = new ArrayList<ServiceInstanceData>();
 
 					for (MobilisServiceInfo info : bean.getDiscoveredServices()) {
 						// todo: check namespace
 						services.add(new ServiceInstanceData(info
 								.getServiceName(), info.getJid()));
 					}
 
 					if (services.size() > 0) {
 						Message m = new Message();
 						m.obj = services;
 						m.what = 1;
 						mServiceDiscoveryHandler.sendMessage(m);
 					} else {
 						mServiceDiscoveryHandler.sendEmptyMessage(-1);
 					}
 				}
 			} else if (bean.getType() == XMPPBean.TYPE_ERROR) {
 				mServiceDiscoveryHandler.sendEmptyMessage(0);
 			}
 
 		} else if (inBean instanceof CreateNewServiceInstanceBean) {
 			if (inBean.getType() == XMPPBean.TYPE_ERROR) {
 				mServiceCreateHandler.sendEmptyMessage(-1);
 			} else {
 				mServiceCreateHandler.sendEmptyMessage(1);
 			}
 		}
 		// Other Beans of type get or set will be responded with an ERROR
 		else {
 			Log.e(TAG, "Unexpected Bean in MainActivity: " + inBean.toString());
 		}
 
 	}
 
 	/***************** Dialog *********************/
 
 	/**
 	 * show a dialog to input the correct xmpp-settings
 	 */
 	public void showMXASettingsDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 		builder.setTitle("Missing XMPP Settings");
 		builder.setMessage("Please input your XMPP-Login in the MXA-Settings");
 
 		builder.setPositiveButton("Settings",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						Intent xmppSetupIntent = new Intent(MainActivity.this,
 								PreferencesClient.class);
 						startActivity(xmppSetupIntent);
 					}
 				});
 		builder.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 					}
 				});
 		builder.create().show();
 	}
 
 	/**
 	 * process the positiv click on the JoinServiceInstanceDialog, send a JoinService-IQ
 	 */
 	@Override
 	public void onDialogPositiveClick(JoinServiceInstanceDialog dialog) {
 		String name = dialog.editName.getText().toString();
 
 		mServiceConnector.getService().getClientData().setName(name);
 		mServiceConnector.getService().getMXAProxy().setNickname(name);
 
 		String clientJID = mServiceConnector.getService().getMXAProxy()
 				.getXmppJid();
 		mServiceConnector.getService().getIQProxy().getProxy()
 				.JoinService(tempServiceJID, clientJID, onJoinService);
 
 		progressDialog.setMessage("Wait for Service-Response");
 		progressDialog.show();
 
 	}
 
 	/**
 	 * process the negative click on the JoinServiceInstanceDialog
 	 */
 	@Override
 	public void onDialogNegativeClick(JoinServiceInstanceDialog dialog) {
 		// Toast.makeText(this, "cancel", Toast.LENGTH_LONG).show();
 		tempServiceJID = "";
 	}
 	
 	/**
 	 * process the positive click on the NewServiceInstanceDialog, send a NewServiceInstanceIQ
 	 */
 	@Override
 	public void onDialogPositiveClick(NewServiceInstanceDialog dialog) {
 		mServiceConnector
 				.getService()
 				.getIQProxy()
 				.sendCreateNewServiceInstanceIQ(
 						mServiceConnector.getService().getServiceNamespace(),
 						dialog.editName.getText().toString(), "");
 		progressDialog = ProgressDialog.show(this, "Waiting",
 				"Create new service instance", true);
 		progressDialog.setCancelable(true);
 	}
 
 	/**
 	 * process the negative click on the NewServiceInstanceDialog
 	 */
 	@Override
 	public void onDialogNegativeClick(NewServiceInstanceDialog dialog) { }
 
 	private boolean isPlayServiceConnected() {
 		// Check that Google Play services is available
 		int resultCode = GooglePlayServicesUtil
 				.isGooglePlayServicesAvailable(this);
 		// If Google Play services is available
 		if (ConnectionResult.SUCCESS == resultCode) {
 			// In debug mode, log the status
 			Log.d("Activity Recognition", "Google Play services is available.");
 			// Continue
 			return true;
 			// Google Play services was not available for some reason
 		} else {
 			// Get the error code
 			GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0).show();
 			return false;
 		}
 	}
 
 	/***************** custom array adapter **************************/
 
 	/**
 	 * save the name and jid of a service-instance
 	 */
 	private class ServiceInstanceData {
 		public String name;
 		public String jid;
 
 		public ServiceInstanceData(String name, String jid) {
 			this.name = name;
 			this.jid = jid;
 		}
 
 		@Override
 		public String toString() {
 			return name + "(" + jid + ")";
 		}
 	}
 
 	/**
 	 * custom arrayadapter to display the ServiceInstanceData
 	 */
 	private class ServiceInstanceDataAdapter extends
 			ArrayAdapter<ServiceInstanceData> {
 
 		private ArrayList<ServiceInstanceData> objects;
 
 		public ServiceInstanceDataAdapter(Context context,
 				int textViewResourceId, ArrayList<ServiceInstanceData> objects) {
 			super(context, textViewResourceId, objects);
 			this.objects = objects;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			TwoLineListItem v = (TwoLineListItem) convertView;
 			if (v == null) {
 				LayoutInflater inflater = (LayoutInflater) getContext()
 						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = (TwoLineListItem) inflater.inflate(
 						android.R.layout.simple_list_item_2, null);
 			}
 
 			ServiceInstanceData i = objects.get(position);
 			if (i != null) {
 				TextView t1 = v.getText1();
 				t1.setText(i.name);
 
 				TextView t2 = v.getText2();
 				t2.setTextColor(Color.GRAY);
 				t2.setText(i.jid);
 			}
 			return v;
 
 		}
 
 	}
 
 }
