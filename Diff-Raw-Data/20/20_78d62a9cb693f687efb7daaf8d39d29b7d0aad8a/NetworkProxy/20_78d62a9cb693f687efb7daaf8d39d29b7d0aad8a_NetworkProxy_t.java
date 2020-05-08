 package de.questmaster.fatremote;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 
 import android.app.Activity;
 import android.net.wifi.WifiManager;
 import android.os.Environment;
 import android.util.Log;
 import android.widget.ImageView;
 import android.widget.Toast;
 import de.questmaster.fatremote.FatRemoteSettings.AppSettings;
 import de.questmaster.fatremote.datastructures.FATDevice;
 import de.questmaster.fatremote.datastructures.FATRemoteEvent;
 
 public class NetworkProxy {
 
 	private static final String LOG_TAG = "NetworkProxy";
 
 	private volatile static NetworkProxy singleton = null;
 	private static FATDevice mFat = null;
 	private Activity context = null;
 	private BlockingQueue<FATRemoteEvent> mEventList = new ArrayBlockingQueue<FATRemoteEvent>(20, true);
 	private Thread mSendingThread = null;
 	
 	public static NetworkProxy getInstance(Activity c) {
 		if (singleton == null) {
 			
 			if (c == null) {
 				throw new IllegalArgumentException("Context was null");
 			}
 			
 			// Initialize proxy
 			NetworkProxy np = new NetworkProxy(); 
 			
 			singleton = np;
 		}
		
		// read current Fat data
		AppSettings settings = new FatRemoteSettings.AppSettings();
		settings.readSettings(c);
		mFat = settings.getFat();

 		singleton.context = c;
 
 		return singleton;
 	}
 
 	/**
 	 * @throws ConnectException
 	 */
 	public boolean isWifiEnabled() {
 		WifiManager wifiManager = (WifiManager) context.getSystemService(android.content.Context.WIFI_SERVICE);
 
 		if (!DebugHelper.ON_EMULATOR && !wifiManager.isWifiEnabled()) {
 			return false;
 		}
 
 		return true;
 	}
 
 	private List<FATDevice> sendDiscoveryMsg() {
 		Map<String, String> data = new HashMap<String, String>();
 		List<DatagramPacket> answers = new ArrayList<DatagramPacket>();
 		List<FATDevice> result = new ArrayList<FATDevice>();
 		DatagramSocket ds = null;
 
 		try {
 			ds = new DatagramSocket();
 			ds.setBroadcast(true);
 			ds.setSoTimeout(3000);
 
 			// Send Theater discovery
 			DatagramPacket dp = new DatagramPacket("Search Venus".getBytes(), 12, InetAddress.getByName("255.255.255.255"), 10000);
 			ds.send(dp);
 
 			// Receive answers
 			try {
 				while (ds.isBound()) {
 					byte[] buf = new byte[128];
 					DatagramPacket packet = new DatagramPacket(buf, buf.length);
 
 					ds.receive(packet);
 
 					answers.add(packet);
 				}
 			} catch (SocketTimeoutException e) {
 			}
 
 			// extract data
 			String entry = null;
 			for (DatagramPacket d : answers) {
 						
 				String adr = d.getAddress().getHostAddress();
 				
 				if (!data.containsKey(adr)) {
 					data.put(adr, "");
 				}
 				
 				entry = data.get(adr);
 				String msgData = new String(d.getData(), 0, d.getLength());
 				if (!entry.contains(msgData)) {
 					entry += msgData;
 
 					// message complete
 					if (entry.contains("\n")) {
 						entry = entry.trim();
 
 						String name = entry.substring(entry.lastIndexOf(':') + 1);
 						String ip = name.substring(name.indexOf(';') + 1).trim();
 						name = name.substring(0, name.indexOf(';'));
 						
 						result.add(new FATDevice(name, ip, true));
 					}
 				}
 				data.put(adr, entry);
 			}
 
 		} catch (IOException e) {
 			Log.e(LOG_TAG, e.getMessage());
 		} finally {
 			if (ds != null) {
 				ds.close();
 			}
 		}
 
 		
 		return result;
 	}
 
 //	private Vector<String> pingBroadcast() {
 //		Vector<String> ips = new Vector<String>();
 //
 //		try {
 //			// exec broadcast ping
 //			Runtime runtime = Runtime.getRuntime();
 //			Process proc = runtime.exec("/system/bin/ping -c 3 -b 255.255.255.255");
 //			proc.waitFor();
 //
 //			// retrieve results
 //			BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
 //			boolean done = false;
 //
 //			// parse output lines
 //			while (br.ready() && !done) {
 //				String line = br.readLine();
 //
 //				StringTokenizer st = new StringTokenizer(line, " ");
 //				String ip = null;
 //				int i = -1;
 //
 //				// check tokens 3 and 4
 //				while (st.hasMoreTokens()) {
 //					String token = st.nextToken();
 //					i++;
 //
 //					if (i < 3) {
 //						continue;
 //					} else if (i == 3) {
 //						ip = token.substring(0, token.length() - 1);
 //					} else if (i == 4) {
 //						if (token.contains("icmp_seq=1")) {
 //							ips.add(ip);
 //						} else if (token.contains("icmp_seq")) {
 //							done = true;
 //							break;
 //						}
 //					} else if (i > 4)
 //						break;
 //				}
 //			}
 //
 //			br.close();
 //		} catch (IOException e) {
 //			e.printStackTrace();
 //		} catch (InterruptedException e) {
 //			e.printStackTrace();
 //		}
 //
 //		return ips;
 //	}
 
 	public List<FATDevice> discoverFAT() throws ConnectException {
 		List<FATDevice> adr = null;
 
 		// Check wifi availability
 		if (isWifiEnabled()) {
 
 			// get detected fat's
 			adr = sendDiscoveryMsg();
 		} else {
 			throw new ConnectException(context.getResources().getString(R.string.app_err_enwifi));
 		}
 
 		return adr;
 	}
 
 	public void addRemoteEvent (FATRemoteEvent event) {
 		if (event == null) {
 			throw new IllegalArgumentException("null event.");
 		}
 		if (mFat == null) {
 			throw new IllegalStateException("FAT device not configured before using sendCode().");
 		}
 				
 			try {
 				mEventList.put(event);
 			} catch (InterruptedException e) {
 				Log.e(LOG_TAG, "Event not in queue (keyId): " + event.getRemoteCode()[3], e);
 			}
 		
 		if (mSendingThread == null) {
 			mSendingThread = new Thread(new Runnable() {
 				public void run() {
 					while (true) {
 						try {
 							sendCode();
 						} catch (final IOException e) {
 							context.runOnUiThread(new Runnable() {
 								public void run() {
 									Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
 							}});
 						}
 	
 						context.runOnUiThread(new Runnable() {
 							public void run() { // reset send image
 								ImageView sending = (ImageView) context.findViewById(R.id.sendLED);
 								if (sending != null) {
 									sending.setImageResource(R.drawable.light_normal);
 								}
 						}});
 					}
 				}
 			});
 			mSendingThread.start();
 		}
 	}
 	
 	private void sendCode() throws IOException {
 		Socket cnx = null;
 		BufferedOutputStream bos = null;
 		BufferedInputStream bis = null;
 		short[] keyCode;
 
 		assert mFat != null;
 		
 		// get event to send
 			FATRemoteEvent event;
 			try {
 				event = mEventList.take();
 				keyCode = event.getRemoteCode();
 			} catch (InterruptedException e) {
 				Log.e(LOG_TAG, e.getLocalizedMessage(), e);
 				throw new IOException(e);
 			}
 		
 		try {
 			// this makes sure wifi is up and running
 			if (isWifiEnabled() || DebugHelper.ON_EMULATOR) {
 				// Open Socket
 				cnx = new Socket(mFat.getInetAddress(), mFat.getPort()); 
 
 				// Open Streams
 				bos = new BufferedOutputStream(cnx.getOutputStream(), 20);
 				bis = new BufferedInputStream(cnx.getInputStream(), 4096);
 
 				// send command
 				Log.i(LOG_TAG, "Sending: " + keyCode[0] + ", " + keyCode[1] + ", " + keyCode[2] + ", " + keyCode[3] + ".");
 				for (int i = 0; i < keyCode.length; i++) {
 					bos.write(keyCode[i]);
 				}
 				bos.flush();
 
 				if (DebugHelper.ON_EMULATOR) { // FIXME: include output of incoming data in regular release!
 					writeIncomingData(bis);
 				}
 			} else {
 				throw new IOException(context.getResources().getString(R.string.app_err_fatoffline));
 			}
 		} catch (UnknownHostException e) {
 			Log.e(LOG_TAG, e.getMessage(), e);
 			throw new IOException(context.getResources().getString(R.string.app_err_wrongip), e);
 		} catch (IOException e) {
 			Log.e(LOG_TAG, e.getMessage(), e);
 			throw new IOException(context.getResources().getString(R.string.app_err_noconnection), e);
 		} finally {
 			try {
 				// close streams
 				if (bos != null) bos.close();
 				if (bis != null) bis.close();
 				if (cnx != null) cnx.close();
 			} catch (Exception e) {
 				Log.e(LOG_TAG, e.getMessage(), e);
 			}
 		}
 	}
 
 	/**
 	 * @param fout
 	 * @param bis
 	 * @throws InterruptedException
 	 * @throws IOException
 	 * @throws FileNotFoundException
 	 */
 	private void writeIncomingData(BufferedInputStream bis) throws IOException {
 		FileOutputStream fout = null;
 		try {
 		Thread.sleep(200);
 
 		int read = 0, in;
 		byte[] buf = new byte[4000];
 		while ((in = bis.read(buf)) > 0) {
 			read += in;
 			if (fout == null) {
 				Log.i(LOG_TAG, "Recived data from FAT: " + buf[0] + ", " + buf[1] + ", " + buf[2] + ", " + buf[3] + ". Buffersize: " + in);
 
 				if (read > 4) {
 					File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/sqlite.out");
 					if (f.createNewFile()) {
 						fout = new FileOutputStream(f);
 
 						// output and skip 4byte identifier
 						fout.write(buf, 4, buf.length - 4);
 					}
 				}
 			} else {
 				fout.write(buf);
 			}
 
 		}
 		if (read > 4 && fout != null) {
 			fout.flush();
 		}
 		} catch (InterruptedException e) {
 			Log.e(LOG_TAG, e.getMessage(), e);
 		} finally {
 			if (fout != null) {
 				fout.close();
 			}
 		}
 	}
 }
