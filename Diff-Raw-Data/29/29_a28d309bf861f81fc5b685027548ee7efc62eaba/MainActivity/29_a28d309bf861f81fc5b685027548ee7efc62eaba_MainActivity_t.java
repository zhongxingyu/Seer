 package org.nexyu.nexyuAndroid;
 
 import org.nexyu.nexyuAndroid.service.NexyuService;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements View.OnClickListener
 {
 	private Messenger			mService	= null;
 	private boolean				mBound		= false;
 	private ServiceConnection	mConnection	= new ServiceConnection() {
 
 												@Override
 												public void onServiceConnected(ComponentName name,
 														IBinder service)
 												{
 													mService = new Messenger(service);
 													mBound = true;
 												}
 
 												@Override
 												public void onServiceDisconnected(ComponentName name)
 												{
 													mService = null;
 													mBound = false;
 												}
 
 											};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		Button startServButton = (Button) findViewById(R.id.startServBut);
 		Button connectButton = (Button) findViewById(R.id.ConnectBut);
 		startServButton.setOnClickListener(this);
 		connectButton.setOnClickListener(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public void onDestroy()
 	{
 		if (mBound)
 			unbindService(mConnection);
 		super.onDestroy();
 	}
 
 	/**
 	 * @see android.view.View.OnClickListener#onClick(android.view.View)
 	 */
 	@Override
 	public void onClick(View v)
 	{
 		Log.d("NEX", "onclick triggered");
 		if (v.getId() == R.id.startServBut)
 		{
 			Log.d("NEX", "start triggered");
 			bindService(new Intent(this, NexyuService.class), mConnection, BIND_AUTO_CREATE);
 		}
 		else if (v.getId() == R.id.ConnectBut)
 		{
 			if (mBound)
 			{
 				Message connect = Message.obtain(null, NexyuService.MSG_CONNECT);
 				Bundle data = new Bundle();
				data.putString("ip", "192.168.1.14");
 				data.putInt("port", NexyuService.DEF_PORT);
 				connect.setData(data);
 				try
 				{
 					mService.send(connect);
 				}
 				catch (RemoteException e)
 				{
 					e.printStackTrace();
 				}
 			}
 			else
 			{
 				Toast.makeText(this, "Not bound to the service", Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 }
