 package org.airportinternet.conn;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.airportinternet.Setting;
 
 import android.os.Handler;
 import android.util.Log;
 
 public class ForkConnector extends Connector {
 	private static final String IODINE_PATH =
 			"/data/data/org.airportinternet/iodine";
 
 	private Handler mHandler = new Handler();
 
 	private Setting s;
 	private Process proc = null;
 	private BufferedReader in = null;
 
 	private List<String> cmdc;
 
 	private Thread watchdog;
 	/*
 	 * We want to assign buffered readers and stuff only after watchdog 
 	 * starts the process and starts monitoring it
 	 */
 	private Lock watchdogLock = new ReentrantLock();
 	private Condition watchdogCond = watchdogLock.newCondition();
 	
 	/*
 	 * Refresh UI every 100ms before "connected", every 1000ms when connected 
 	 */
 	private int refreshEvery = 100;
 	
 	@Override
 	public void stop() {
 		if (proc != null)
 			proc.destroy();
 		try {
 			watchdog.join();
 		} catch (InterruptedException e) {
 			Log.e("ForkConnector:stop",
 					"Interrupted while waiting for watchdog");
 			e.printStackTrace();
 		}
 	}
 	
 	/* Avoiding race condition below */
 	private boolean watchDogStartedTheProcess = false;
 	
 	@Override
 	protected void start(Setting setting) {
 		s = setting;
 
 		cmdc = s.cmdarray();
 		cmdc.add(0, IODINE_PATH);
 		Log.d("CommandLine", cmdc.toString() + "; size: " + cmdc.size());
 		
 		watchdog = new Thread() {
 			public void run() {
				Log.d("watchdog", "watchdog started, locking watchdogLock");
 				watchdogLock.lock();
 				try {
 					proc = new ProcessBuilder(cmdc).redirectErrorStream(
 							true).start();
 					in = new BufferedReader(new InputStreamReader(
 							proc.getInputStream()));
 				} catch (IOException e) {
 					sendLog("Failed to start iodine");
 					e.printStackTrace();
 				}
 				watchDogStartedTheProcess = true;
 				watchdogCond.signal();
 				Log.d("watchdog", "unlocking watchdogLock");
 				watchdogLock.unlock();
 
 				try {
 					if (proc != null) // if loaded successfully
 						proc.waitFor();
 					running = false;
 				} catch (InterruptedException e) {
 					e.printStackTrace(); // shouldn't ever happen
 				}
 				
 				Log.d("watchdog", "watchdog stopped");
 			}
 		};
 		watchdog.start();
 		connecting();
 		
 		Log.d("ForkConnector:start", "before locking watchdogLock");
 		watchdogLock.lock();
 		Log.d("ForkConnector:start", "locked watchdogLock");
 		try {
 			while(!watchDogStartedTheProcess)
 				watchdogCond.await();
 		} catch (InterruptedException e) {
 			// should never happen
 			e.printStackTrace();
 		} finally {
 			watchdogLock.unlock();
 			Log.d("ForkConnector:start", "unlocked watchdogLock");
 		}
 		running = true;
 		mHandler.post(poller);
 	}
 
 	private Runnable poller = new Runnable() {
 		public void run() {
 			/* If executable failed to start */
 			if (in == null) {
 				running = false;
 				disconnected();
 				return;
 			}
 			
 			char buf[] = new char[1024];
 			StringBuilder ret = new StringBuilder();
 			int cnt = 0;
 			
 			try {
 				while (in.ready())
 					if((cnt = in.read(buf)) != -1) {
 						ret.append(buf, 0, cnt);
 					}
 			} catch (IOException e) {
 				ret.append("Read interrupted");
 				e.printStackTrace();
 			}
 			if (ret.length() > 0) {
 				sendLog(ret.toString());
 			}
 			if (!isConnected()) {
 				if (fullLog.lastIndexOf("setup complete, ") != -1)
 					connected();
 				if (isConnected()) {
 					// TODO: set up routing
 					refreshEvery = 1000;
 				}
 			}
    		if (running) mHandler.postDelayed(this, refreshEvery);
    		else disconnected();
 		}
 	};
 }
