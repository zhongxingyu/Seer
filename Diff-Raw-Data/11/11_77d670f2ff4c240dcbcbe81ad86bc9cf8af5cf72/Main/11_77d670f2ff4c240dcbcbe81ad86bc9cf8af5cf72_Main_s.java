package com.sec.android.allshare.devicediscovery;
 
 import java.util.ArrayList;
 import com.sec.android.allshare.Device;
 import com.sec.android.allshare.Device.DeviceDomain;
 import com.sec.android.allshare.Device.DeviceType;
 import com.sec.android.allshare.DeviceFinder;
 import com.sec.android.allshare.DeviceFinder.IDeviceFinderEventListener;
 import com.sec.android.allshare.ERROR;
 import com.sec.android.allshare.ServiceConnector;
 import com.sec.android.allshare.ServiceConnector.IServiceConnectEventListener;
 import com.sec.android.allshare.ServiceConnector.ServiceState;
 import com.sec.android.allshare.ServiceProvider;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.widget.TextView;
 
 
 
 public class Main extends Activity {
 	/** Called when the activity is first created. */
 
 	ServiceProvider mServiceProvider = null;
 	TextView mText = null;
 	Handler mHandler = new Handler();
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		mText = (TextView) findViewById(R.id.txtLog);
 		mText.append("\n\n" + "Creating service provider!"  + "\r\n\n");
 
 		ERROR err = ServiceConnector.createServiceProvider(this, new IServiceConnectEventListener()
 		{
 			@Override
 			public void onCreated(ServiceProvider sprovider, ServiceState state)
 			{
 				mServiceProvider = sprovider;
 				showDeviceList();
 
 			}
 			@Override
 			public void onDeleted(ServiceProvider sprovider)
 			{
 				mServiceProvider = null;
 			}
 		});
 		
 		if (err == ERROR.FRAMEWORK_NOT_INSTALLED)
 		{
 			// AllShare Framework Service is not installed.
 		}
 		else if (err == ERROR.INVALID_ARGUMENT)
 		{
 			// Input argement is invalid. Check and try again
 		}
 		else
 		{
 			// Success on calling the function.
 		}		
 	}
 
 	private final DeviceFinder.IDeviceFinderEventListener mDeviceDiscoveryListener = new IDeviceFinderEventListener()
 	{
 		@Override
 		public void onDeviceRemoved(DeviceType deviceType, Device device, ERROR err)
 		{
			mText.append("AVPlayer: " + device.getName() + " [" + device.getIPAdress() + "] is removed" + "\r\n");
 		}
 
 		@Override
 		public void onDeviceAdded(DeviceType deviceType, Device device, ERROR err)
 		{
			mText.append("AVPlayer: " + device.getName() + " [" + device.getIPAdress() + "] is found" + "\r\n");
 		}
 	};
 
 	private void showDeviceList()
 	{
 		if (mServiceProvider == null)
 			return;
 
 		DeviceFinder mDeviceFinder = mServiceProvider.getDeviceFinder();
 		mDeviceFinder.setDeviceFinderEventListener(DeviceType.DEVICE_AVPLAYER, mDeviceDiscoveryListener);
 		ArrayList<Device> mDeviceList = mDeviceFinder.getDevices(DeviceDomain.LOCAL_NETWORK, DeviceType.DEVICE_AVPLAYER);
 
 		if (mDeviceList != null)
 		{
 			for (int i = 0; i < mDeviceList.size(); i++)
 			{
				mText.append("AVPlayer: " + mDeviceList.get(i).getName() + " [" + mDeviceList.get(i).getIPAdress() + "] is found" + "\r\n");
 			}
 		}
 	}
 
 
 	@Override
 	protected void onDestroy()
 	{
 		if (mServiceProvider != null)
 			ServiceConnector.deleteServiceProvider(mServiceProvider);
 		super.onDestroy();
 	}
 
 }
