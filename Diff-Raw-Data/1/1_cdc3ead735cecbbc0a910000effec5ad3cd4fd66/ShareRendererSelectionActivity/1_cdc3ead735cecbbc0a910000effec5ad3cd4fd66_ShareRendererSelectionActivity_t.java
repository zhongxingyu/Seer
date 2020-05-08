 package fr.spaz.upnp.activities;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.teleal.cling.android.AndroidUpnpService;
 import org.teleal.cling.model.action.ActionInvocation;
 import org.teleal.cling.model.message.UpnpResponse;
 import org.teleal.cling.model.meta.Device;
 import org.teleal.cling.model.meta.Service;
 import org.teleal.cling.model.types.UDAServiceType;
 import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
 import org.teleal.cling.support.avtransport.callback.Play;
 import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.provider.MediaStore;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 import fr.spaz.upnp.R;
 import fr.spaz.upnp.fragments.ShareControlFragment;
 import fr.spaz.upnp.fragments.ShareRendererSelectionFragment;
 import fr.spaz.upnp.upnp.UPnPDeviceDisplay;
 import fr.spaz.upnp.upnp.UPnPService;
 import fr.spaz.upnp.utils.NanoHTTPD;
 import fr.spaz.upnp.utils.NetworkUtils;
 
 public class ShareRendererSelectionActivity extends SherlockFragmentActivity implements IRenderSelection, OnItemClickListener
 {
 
 	private static final String TAG = "SharePictureBroadcastReceiver";
 
 	private AndroidUpnpService mUpnpService;
 	private ServiceConnection mServiceConnection;
 
 	private NanoHTTPD mHttpd;
 	private String mPath;
 
 	private Device<?, ?, ?> mCurrentDevice;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.main);
 
 		final Bundle callBundle = getIntent().getExtras();
 		final Uri uri = (Uri) callBundle.get(Intent.EXTRA_STREAM);
 		Log.i(TAG, "uri: " + uri.toString());
 
 		if (null != uri && uri.getScheme().startsWith("content"))
 		{
 			// Get file path
 			final String[] proj = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.MIME_TYPE};
 			final Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
 			if (cursor.moveToFirst())
 			{
 				mPath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
 				// final String mimetype = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
 
 			}
 			if (null != cursor)
 			{
 				cursor.close();
 			}
 		}
 		else
 		{
 			mPath = uri.toString();
 		}
 
 		try
 		{
 			mHttpd = new NanoHTTPD(0, new File("/"));
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 
 		// Init
 		mServiceConnection = new UpnpBrowseServiceConnection();
 
 		// Start upnp browse service
 		Intent intent = new Intent(this, UPnPService.class);
 		getApplicationContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
 	}
 
 	@Override
 	protected void onDestroy()
 	{
 		if (null != mHttpd)
 		{
 			mHttpd.stop();
 			mHttpd = null;
 		}
 		getApplicationContext().unbindService(mServiceConnection);
 		super.onDestroy();
 	}
 
 	@Override
 	public AndroidUpnpService getUPnPService()
 	{
 		return mUpnpService;
 	}
 
 	@Override
 	public Device<?, ?, ?> getCurrentRenderer()
 	{
 		return mCurrentDevice;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> listview, View view, int position, long id)
 	{
 		if (null != mUpnpService)
 		{
 			final UPnPDeviceDisplay deviceDisplay = (UPnPDeviceDisplay) listview.getItemAtPosition(position);
 			final Device<?, ?, ?> renderer = deviceDisplay.getDevice();
 			final Service<?, ?> avTransportService = renderer.findService(new UDAServiceType("AVTransport"));
 
 			if (null != avTransportService)
 			{
 
 				// execute setAvTransportURI
 				Log.i(TAG, "launch setAVTransportURI");
 				final String url = String.format("http://%s:%d%s", NetworkUtils.getIp(getBaseContext()), mHttpd.getPort(), mPath);
 				Log.i(TAG, "url: " + url);
 				mUpnpService.getControlPoint().execute(new SetAVTransportURI(new UnsignedIntegerFourBytes(0), avTransportService, url, "NO METADATA")
 				{
 					@SuppressWarnings("rawtypes")
 					@Override
 					public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
 					{
 						Log.i(TAG, "setAVTransportURI failure");
 						Log.i(TAG, "invocation: " + invocation.getFailure().getMessage());
 						// Log.i(TAG, "operation: " + operation.getStatusCode() + " " + operation.getStatusMessage());
 					}
 
 					@SuppressWarnings("rawtypes")
 					@Override
 					public void success(ActionInvocation invocation)
 					{
 						super.success(invocation);
 
 						Log.i(TAG, "setAVTransportURI success");
 						Log.i(TAG, "launch play");
 						mUpnpService.getControlPoint().execute(new Play(new UnsignedIntegerFourBytes(0), avTransportService, "1")
 						{
 							@Override
 							public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
 							{
 								Log.i(TAG, "play failure");
 								Log.i(TAG, "invocation: " + invocation.getFailure().getMessage());
 								// Log.i(TAG, "operation: " + operation.getStatusCode() + " " + operation.getStatusMessage());
 							}
 
 							@Override
 							public void success(ActionInvocation invocation)
 							{
 								super.success(invocation);
 								Log.i(TAG, "play success");
 
 								mCurrentDevice = renderer;
 
 								FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 								ft.replace(R.id.content, new ShareControlFragment());
 								ft.commit();
 							}
 						});
 					}
 				});
 			}
 
 		}
 	}
 
 	private class UpnpBrowseServiceConnection implements ServiceConnection
 	{
 
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder service)
 		{
 			mUpnpService = (AndroidUpnpService) service;
 
 			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 			ft.replace(R.id.content, new ShareRendererSelectionFragment());
 			ft.commit();
 
 			// // Refresh the list with all known devices
 			// for (Device<?, ?, ?> device : mUpnpService.getRegistry().getDevices(new UDADeviceType("MediaRenderer", 1)))
 			// {
 			// mListener.deviceAdded(device);
 			// }
 			//
 			// // Getting ready for future device advertisements
 			// mUpnpService.getRegistry().addListener(mListener);
 			//
 			// // Search asynchronously for all devices
 			// mUpnpService.getControlPoint().search();
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName className)
 		{
 			mUpnpService = null;
 		}
 
 	}
 }
