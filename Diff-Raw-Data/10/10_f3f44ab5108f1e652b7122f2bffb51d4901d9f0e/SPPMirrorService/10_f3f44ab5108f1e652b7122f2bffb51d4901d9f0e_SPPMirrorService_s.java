 /*
  * Copyright (c) 2012 Jeff Boody
  *
  * Permission is hereby granted, free of charge, to any person obtaining a
  * copy of this software and associated documentation files (the "Software"),
  * to deal in the Software without restriction, including without limitation
  * the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included
  * in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  *
  */
 
 package com.jeffboody.SPPMirror;
 
 import android.util.Log;
 import android.os.Binder;
 import android.os.IBinder;
 import android.content.Context;
 import android.content.Intent;
 import android.app.Service;
 import android.app.NotificationManager;
 import android.app.Notification;
 import android.app.PendingIntent;
 import com.jeffboody.BlueSmirf.BlueSmirfSPP;
 
 public class SPPMirrorService extends Service
 {
 	private static final String TAG = "SPPMirrorService";
 
 	private SPPMirrorServiceBinder mBinder;
 
 	private boolean      mIsConnected;
 	private boolean      mAutoReconnect;
 	private BlueSmirfSPP mSPP;
 	private SPPNetSocket mNet;
 	private int          mRxCount;
 	private int          mTxCount;
 	private String       mBluetoothAddress;
 	private int          mNetPort;
 
 	private static final int SPP_NOTIFICATION_ID = 42;
 
 	public SPPMirrorService()
 	{
 		mIsConnected   = false;
 		mAutoReconnect = false;
 		mBinder        = null;
		mSPP           = null;
		mNet           = null;
 		mRxCount       = 0;
 		mTxCount       = 0;
 	}
 
 	@Override
 	public void onCreate()
 	{
 		super.onCreate();
 		mBinder = new SPPMirrorServiceBinder(this);
		mSPP    = new BlueSmirfSPP();
		mNet    = new SPPNetSocket();
 
 		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		Notification n         = new Notification(R.drawable.notify, "SPPMirror", System.currentTimeMillis());
 		PendingIntent pi       = PendingIntent.getActivity(this, 0, new Intent(this, SPPMirror.class), 0);
 		n.setLatestEventInfo(this, "SPPMirror", "running", pi);
 		nm.notify(SPP_NOTIFICATION_ID, n);
 	}
 
 	@Override
 	public void onDestroy()
 	{
 		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		nm.cancel(SPP_NOTIFICATION_ID);
 		mNet.disconnect();
 		mSPP.disconnect();
		mNet    = null;
		mSPP    = null;
 		mBinder = null;
 		super.onDestroy();
 	}
 
 	/*
 	 * binder implementation
 	 */
 
 	public class SPPMirrorServiceBinder extends Binder
 	{
 		private SPPMirrorService mService;
 
 		SPPMirrorServiceBinder(SPPMirrorService service)
 		{
 			mService = service;
 		}
 
 		public void onSync()
 		{
 			mService.onSync();
 		}
 
 		public void onConnectLink(String addr, int port, boolean auto_reconnect)
 		{
 			mService.onConnectLink(addr, port, auto_reconnect);
 		}
 
 		public void onDisconnectLink()
 		{
 			mService.onDisconnectLink();
 		}
 	}
 
 	public void onSync()
 	{
 		Intent intent = new Intent("com.jeffboody.SPPMirror.action.STATUS");
 		String msg;
 		if(mIsConnected && mSPP.isConnected())
 		{
 			msg = "spp: " + (mSPP.isConnected() ? "connected\n" : "listening\n") +
 			      "net: " + (mNet.isConnected() ? "connected\n" : "listening\n") +
 			      "RX: " + mRxCount + "B\n" +
 			      "TX: " + mTxCount + "B";
 		}
 		else if(mIsConnected)
 		{
 			msg = "spp: " + (mSPP.isConnected() ? "connected\n" : "listening\n") +
 			      "RX: " + mRxCount + "B\n" +
 			      "TX: " + mTxCount + "B";
 		}
 		else
 		{
 			msg = "disconnected\n" +
 			      "RX: " + mRxCount + "B\n" +
 			      "TX: " + mTxCount + "B";
 		}
 		intent.putExtra("status", msg);
 		sendBroadcast(intent);
 	}
 
 	public void onConnectLink(String addr, int port, boolean auto_reconnect)
 	{
 		if(mIsConnected)
 		{
 			return;
 		}
 
 		// create TX/RX threads
 		mBluetoothAddress = addr;
 		mNetPort          = port;
 		mIsConnected      = true;
 		mAutoReconnect    = auto_reconnect;
 		ThreadTX tx       = new ThreadTX();
 		onSync();
 	}
 
 	public void onDisconnectLink()
 	{
 		mNet.disconnect();
 		mSPP.disconnect();
 		mBluetoothAddress = null;
 		mNetPort          = 0;
 		mIsConnected      = false;
 		onSync();
 	}
 
 	/*
 	 * thread implementation
 	 */
 
 	public class ThreadTX implements Runnable
 	{
 		ThreadTX()
 		{
 			Thread t = new Thread(this);
 			t.start();
 		}
 
 		public void run()
 		{
 			long t0       = System.currentTimeMillis();
 			byte[] buffer = new byte[4096];
 
 			do
 			{
 				mTxCount = 0;
 				mRxCount = 0;
 
 				// connect to SPP and Net before starting rx thread
 				onSync();
 				if(mSPP.connect(mBluetoothAddress) == false)
 				{
 					continue;
 				}
 				onSync();
 				if(mNet.connect(mNetPort) == false)
 				{
 					mSPP.disconnect();
 					continue;
 				}
 				onSync();
 				ThreadRX rx = new ThreadRX();
 
 				while(mIsConnected &&
 				      (mNet.isConnected()) &&
 				      (mSPP.isConnected()) &&
 				      (mNet.isError() == false) &&
 				      (mSPP.isError() == false))
 				{
 					int count = mNet.read(buffer, 0, 4096);
 					mSPP.write(buffer, 0, count);
 					mSPP.flush();
 					mTxCount += count;
 
 					// throttle the updates
 					long t1 = System.currentTimeMillis();
 					if((t1 - t0) > 250)
 					{
 						onSync();
 						t0 = t1;
 					}
 				}
 
 				// reached end-of-stream or error
 				if(mIsConnected)
 				{
 					mNet.disconnect();
 					mSPP.disconnect();
 				}
 				onSync();
 
 				if(mAutoReconnect && mIsConnected)
 				{
 					try { Thread.sleep((long) 1000); }
 					catch(InterruptedException e) { }
 				}
 			} while(mAutoReconnect && mIsConnected);
 		}
 	}
 
 	public class ThreadRX implements Runnable
 	{
 		ThreadRX()
 		{
 			Thread t = new Thread(this);
 			t.start();
 		}
 
 		public void run()
 		{
 			long t0       = System.currentTimeMillis();
 			byte[] buffer = new byte[4096];
 
 			while(mIsConnected &&
 			      (mNet.isConnected()) &&
 			      (mSPP.isConnected()) &&
 			      (mNet.isError() == false) &&
 			      (mSPP.isError() == false))
 			{
 				int count = mSPP.read(buffer, 0, 4096);
 				mNet.write(buffer, 0, count);
 				mNet.flush();
 				mRxCount += count;
 
 				// throttle the updates
 				long t1 = System.currentTimeMillis();
 				if((t1 - t0) > 250)
 				{
 					onSync();
 					t0 = t1;
 				}
 			}
 
 			// reached end-of-stream or error
 			if(mIsConnected)
 			{
 				mNet.disconnect();
 				mSPP.disconnect();
 			}
 			onSync();
 		}
 	}
 
 	/*
 	 * service implementation
 	 */
 
 	@Override
 	public IBinder onBind(Intent intent)
 	{
 		return mBinder;
 	}
 }
