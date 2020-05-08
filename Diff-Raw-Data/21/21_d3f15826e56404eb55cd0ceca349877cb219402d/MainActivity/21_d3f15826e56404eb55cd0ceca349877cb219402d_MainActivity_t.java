 package se.purplescout.purplemow.onboard;
 
 import java.io.IOException;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import se.purplescout.R;
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.XmlResourceParser;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.android.future.usb.UsbAccessory;
 import com.android.future.usb.UsbManager;
 
 public class MainActivity extends Activity {
 
 	private static final String MAIN = "android.intent.action.MAIN";
 	private static final String ACTION_USB_PERMISSION = "se.purplescout.purplemow.USB_PERMISSION";
 	private static final String ACTION_LOG_MSG = "se.purplescout.purplemow.LOG_MSG";
 
 	public interface Display {
 
 		Button getStartBtn();
 
 		Button getStopBtn();
 
 		View getLogView();
 
 		TextView getLogText();
 
 		View getLoaderSpinner();
 
 		void showNotConnectedPopup();
 
 		void hideNotConnectedPopup();
 	}
 
 	Display display;
 	Intent usbAccessoryIntent;
 	private boolean permissionRequestPending;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		Log.i("PurpleMow",
 				"¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤ Nu startar det! ¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤");
 
 		display = new MainDisplay(this);
 
 		bind();
 
 		if (getIntent().getAction().equals(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)) {
 			usbAccessoryIntent = getIntent();
 			display.getStartBtn().setEnabled(true);
 		} else if (getIntent().getAction().equals(MAIN)) {
 			display.showNotConnectedPopup();
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 	}
 
 	private void bind() {
 		display.getStartBtn().setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				startFSM();
 			}
 		});
 
 		display.getStopBtn().setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				stopFSM();
 			}
 		});
 
 		this.registerReceiver(new BroadcastReceiver() {
 
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				display.hideNotConnectedPopup();
 				setupUsbConnection();
 			}
 		}, new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_ATTACHED));
 
 		this.registerReceiver(new BroadcastReceiver() {
 
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				stopFSM();
 			}
 		}, new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED));
 
 		this.registerReceiver(new BroadcastReceiver() {
 
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				determineUsbPermission(intent);
 			}
 		}, new IntentFilter(ACTION_USB_PERMISSION));
 
 		this.registerReceiver(new BroadcastReceiver() {
 
 			@Override
 			public void onReceive(Context context, Intent intent) {
				String log = display.getLogText().getText().toString();
				if (log.length() > 500) {
					log = log.substring(0, 500);  // Truncate log to minimize resource usage 
				}
 				log = intent.getExtras().getString(ACTION_LOG_MSG) + "\n" + log;
 				display.getLogText().setText(log);
 			}
 		}, new IntentFilter(ACTION_LOG_MSG));
 	}
 
 	private void startFSM() {
 		display.getLogView().setVisibility(View.GONE);
 		display.getLoaderSpinner().setVisibility(View.VISIBLE);
 		display.getStartBtn().setEnabled(false);
 		display.getStopBtn().setEnabled(true);
 
 		Intent serviceIntent = new Intent(MainActivity.this, MainService.class);
 		serviceIntent.fillIn(usbAccessoryIntent, 0);
 		Log.d(this.getClass().getCanonicalName(), "Starting service: " + MainService.class.getCanonicalName());
 		startService(serviceIntent);
 		display.getLogView().setVisibility(View.VISIBLE);
 		display.getLoaderSpinner().setVisibility(View.GONE);
 	}
 
 	private void stopFSM() {
 		display.getLogView().setVisibility(View.VISIBLE);
 		display.getLoaderSpinner().setVisibility(View.GONE);
 		display.getStartBtn().setEnabled(true);
 		display.getStopBtn().setEnabled(false);
 
 		Intent intent = new Intent(this, MainService.class);
 		stopService(intent);
 	}
 
 	private UsbAccessory findUsbAccessory(UsbManager manager) {
 		UsbAccessory[] accessoryList = manager.getAccessoryList();
 		Accessory accessory = parseAccessoryFilter();
 		if (accessoryList != null) {
 			for (UsbAccessory usbAccessory : accessoryList) {
 				if (usbAccessory.getManufacturer().equals(accessory.manufacturer)) {
 					return usbAccessory;
 				}
 			}
 		}
 
 		return null;
 	}
 
 	private class Accessory {
 		String manufacturer;
 		String model;
 		String version;
 	}
 
 	private Accessory parseAccessoryFilter() {
 		Accessory accessory = new Accessory();
 		int eventType;
 		try {
 			XmlResourceParser xpp = getResources().getXml(R.xml.accessory_filter);
 			xpp.next();
 			eventType = xpp.getEventType();
 
 			while (eventType != XmlPullParser.END_DOCUMENT) {
 				if (eventType == XmlPullParser.START_TAG) {
 					if (xpp.getName().equals("usb-accessory")) {
 						for (int i = 0; i < xpp.getAttributeCount(); i++) {
 							if (xpp.getAttributeName(i).equals("manufacturer")) {
 								accessory.manufacturer = xpp.getAttributeValue(i);
 							} else if (xpp.getAttributeName(i).equals("model")) {
 								accessory.model = xpp.getAttributeValue(i);
 							} else if (xpp.getAttributeName(i).equals("version")) {
 								accessory.version = xpp.getAttributeValue(i);
 							}
 						}
 					}
 				}
 				eventType = xpp.next();
 			}
 		} catch (XmlPullParserException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 
 		return accessory;
 	}
 
 	private void setupUsbConnection() {
 		UsbManager usbManager = UsbManager.getInstance(this);
 		final UsbAccessory accessory = findUsbAccessory(usbManager);
 
 		if (accessory != null) {
 			if (!usbManager.hasPermission(accessory)) {
 				if (!permissionRequestPending) {
 					PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
 					usbManager.requestPermission(accessory, permissionIntent);
 					permissionRequestPending = true;
 				}
 			}
 		} else {
 			display.showNotConnectedPopup();
 		}
 	}
 
 	private void determineUsbPermission(Intent intent) {
 		String action = intent.getAction();
 		if (ACTION_USB_PERMISSION.equals(action)) {
 			synchronized (this) {
 				if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
 					usbAccessoryIntent = intent;
 					display.getStartBtn().setEnabled(true);
 				} else {
 					Log.d(MainActivity.this.getClass().getCanonicalName(), "permission denied for accessory ");
 				}
 			}
 		}
 	}
 }
