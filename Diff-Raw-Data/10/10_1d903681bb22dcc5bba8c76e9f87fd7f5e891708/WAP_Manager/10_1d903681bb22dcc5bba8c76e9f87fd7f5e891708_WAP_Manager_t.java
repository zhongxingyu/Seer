 package edu.ucsb.geog.blakeregalia;
 
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiManager;
 
 public class WAP_Manager {
 
 	protected static Hashtable<String, Integer> ssid_names;
 
 	ucsb_wap_activity main;
 	Context context;
 
 	WifiManager wifi;
 	WAP_Listener scan_callback;
 	List<ScanResult> scan_result;
 
 	private static final int INITIAL_HASHTABLE_SIZE 			= 64;
 	private static final int WIFI_SIGNAL_NUM_PRECISION_LEVELS 	= 45;
 	private static final int WIFI_SIGNAL_MAX_DATA_LEVEL 		= 255;
 	private static final double WIFI_SIGNAL_MAX_DATA_LEVEL_INV 	= 1.0 / WIFI_SIGNAL_MAX_DATA_LEVEL;
 	private static final double WIFI_SIGNAL_NUM_LEVELS_FACTOR 	= ((double) WIFI_SIGNAL_MAX_DATA_LEVEL) / WIFI_SIGNAL_NUM_PRECISION_LEVELS;
 
 	public WAP_Manager(ucsb_wap_activity activity) {
 		main = activity;
 		context = (Context) main;
 		
 		if(ssid_names == null) {
 			ssid_names = new Hashtable<String, Integer>(INITIAL_HASHTABLE_SIZE);
 		}
 
 		wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 	}
 
 	/**
 	 * binds a listener for broadcast events; notifies that wifi scan results have become available 
 	 */
 	private void listen_for_scans() {
 		IntentFilter intent = new IntentFilter();
 		intent.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
 		context.registerReceiver(new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context c, Intent i) {
 				receive_waps(((WifiManager) c.getSystemService(Context.WIFI_SERVICE)).getScanResults());
 			}
 		}, intent);
 	}
 
 	/**
 	 * prepares the scan results to be fetched by the accessing class
 	 */
 	private void receive_waps(List<ScanResult> waps) {
 		scan_result = waps;
 		scan_callback.onComplete(scan_result.size(), (new Date()).getTime());
 	}
 
 	/**
 	 * initiates the scan for wireless access points
 	 */
 	private void scan_waps() {
 		wifi.startScan();
 	}
 
 	/**
 	 * public functions
 	 * 
 	 */
 
 	public void enableWifi(final Hardware_Ready_Listener callback) {
 		/* if wifi is enabled... */
 		if(wifi.isWifiEnabled() == true) { 
 			/* notify the listener */
 			callback.onReady();
 		}
 		/* wifi is disabled.. */
 		else {
 			/* wait for wifi to enable **/
 			IntentFilter intent = new IntentFilter();
 			intent.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
 			context.registerReceiver(new BroadcastReceiver() {
 				public void onReceive(Context c, Intent i) {
 					wifi = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
 
 					/* if wifi was enabled successfully... */
 					if(wifi.isWifiEnabled()) {
 
 						/* notify the listener */
 						callback.onReady();
 					}
 					/* hardware could not be enabled */
 					else {
 
 						/* notify the listener */
 						callback.onFail();
 					}
 				}
 			}
 			, intent);
 
 			/* enable the wifi */
 			wifi.setWifiEnabled(true);
 		}
 	}
 
 	/**
 	 * initiates the scanning process
 	 * @param callback		the method to be called every time a scan completes
 	 */
 	public void startScanning(WAP_Listener callback) {
 		scan_callback = callback;
 
 		/* get ready to be notified about all future scans */
 		listen_for_scans();
 
 		/* begin the scanning process */
 		scan_waps();
 	}
 
 	/**
 	 * continue the scanning process
 	 * @param callback		the method to be called every time a scan completes
 	 */
 	public void continueScanning() {
 		scan_waps();
 	}
 	
 	public byte[] encodeSSIDs() {
 		//ByteBuilder
 		StringBuilder data = new StringBuilder();
 		int i = 0;
 		ByteBuilder zero = new ByteBuilder(1);
 		zero.append((byte) (0 & 0xff));
 		data.append(new String(zero.getBytes()));
 
 		Iterator<Entry<String, Integer>> set = ssid_names.entrySet().iterator();
 		while(set.hasNext()) {
 			Entry<String, Integer> e = set.next();
 //			data.append(Encoder.encode_int(((Integer) e.getValue()).intValue()));
 //			data.append(((String) e.getKey()).getBytes());
 
 			int v = ((Integer) e.getValue()).intValue();
 			String ssid = (String) e.getKey();
 			
 			ByteBuilder bytes = new ByteBuilder(5);
 			
 			bytes.append_4(Encoder.encode_int( v ));
 			bytes.append(Encoder.encode_byte(ssid.length()));
 			
			// don't allow extended ascii codes to screw up the encoding
 			data.append(new String(bytes.getBytes()));
			int c = ssid.length();
			while(c-- != 0) {
				if(ssid.charAt(c) > 255) {
					ssid = ssid.replace(ssid.charAt(c), '?');
				}
			}
			data.append(ssid);
 		}
 
 		return data.toString().getBytes();
 	}
 
 	public AccessPointIncident getWAP(int index) {
 		if(index > scan_result.size()) {
 			main.debug("ERROR: "+index+" out of range. Dying in 10 seconds");
 			try {
 				Thread.sleep(10000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return new AccessPointIncident(scan_result.get(index));
 	}
 
 	public class AccessPointIncident {
 		ScanResult access_point;
 		int signal_level;
 
 		public AccessPointIncident(ScanResult wap) {
 			access_point = wap;
 			signal_level = (int) Math.round((WifiManager.calculateSignalLevel(access_point.level, WIFI_SIGNAL_NUM_PRECISION_LEVELS) * WIFI_SIGNAL_NUM_LEVELS_FACTOR))+1;
 		}
 
 		public String toString() {
 			StringBuffer info = new StringBuffer();
 
 			/* signal */
 			int signal_percentage = (int) (signal_level * WIFI_SIGNAL_MAX_DATA_LEVEL_INV * 100);
 			info.append(signal_percentage+"%");
 
 			/* bssid */
 			info.append("  "+access_point.BSSID);
 
 			/* asterik */
 			if(access_point.SSID.equals("UCSB Wireless Web")) {
 				info.append(" *");
 			}
 
 			return info.toString();
 		}
 
 		/**
 		 * 
 		 * @return		a fixed n-byte string of this encoded access point incident
 		 */
 		public byte[] encode() {
 			ByteBuilder encoded = new ByteBuilder(Encoder.DATA_ENTRY_LENGTH);
 			
 			encoded.append_6(encode_hw_addr());
 			encoded.append(encode_signal());
 			encoded.append(encode_ssid_name());
 			
 			return encoded.getBytes();
 		}
 		
 		private byte encode_ssid_name() {
 			int key = 0;
 			Integer map = ssid_names.get(access_point.SSID);
 			if(map == null) {
 				key = ssid_names.size();
 				ssid_names.put(access_point.SSID, new Integer(key));
 			}
 			else {
 				key = map.intValue();
 			}
 			return (byte) (key & 0xff);
 		}
 		
 		private byte encode_signal() {
 			if(signal_level < 0) signal_level = 255;
 			if(signal_level > 255) signal_level = 255;
 			return (byte) (signal_level & 0xff);
 		}
 		
 		private byte[] hex_to_byte(String s) {
 		    int len = s.length();
 		    byte[] data = new byte[len / 2];
 		    for (int i = 0; i < len; i += 2) {
 		        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
 		                             + Character.digit(s.charAt(i+1), 16));
 		    }
 		    return data;
 		}
 
 		/**
 		 * to make the storage of BSSIDs efficient, turn the 34-byte string into a 6-byte array
 		 * to make the encoding of the BSSIDs quick, don't use a loop
 		 */ 
 		private byte[] encode_hw_addr() {
 			String hw_addr = access_point.BSSID;
 			
 			return hex_to_byte(hw_addr.replaceAll(":", ""));
 			/*
 			byte[] b = new byte[6];
 			char d, e;
 
 			d = hw_addr.charAt(0);
 			if(d > 96) d -= 'W';
 			else d -= '0';
 
 			e = hw_addr.charAt(1);
 			if(e > 96) d -= 'W';
 			else e -= '0';
 
 			b[0] = (byte) (((d & 0x0f) << 4) | (e & 0x0f));
 
 
 			d = hw_addr.charAt(3);
 			if(d > 96) d -= 'W';
 			else d -= '0';
 
 			e = hw_addr.charAt(4);
 			if(e > 96) e -= 'W';
 			else e -= '0';
 
 			b[1] = (byte) (((d & 0x0f) << 4) | (e & 0x0f));
 
 
 			d = hw_addr.charAt(6);
 			if(d > 96) d -= 'W';
 			else d -= '0';
 			b[4] = (byte) d;
 
 			e = hw_addr.charAt(7);
 			if(e > 96) e -= 'W';
 			else e -= '0';
 
 			b[2] = (byte) (((d & 0x0f) << 4) | (e & 0x0f));
 
 
 			d = hw_addr.charAt(9);
 			if(d > 96) d -= 'W';
 			else d -= '0';
 
 			e = hw_addr.charAt(10);
 			if(e > 96) e -= 'W';
 			else e -= '0';
 
 			b[3] = (byte) (((d & 0x0f) << 4) | (e & 0x0f));
 
 
 			d = hw_addr.charAt(12);
 			if(d > 96) d -= 'W';
 			else d -= '0';
 
 			e = hw_addr.charAt(13);
 			if(e > 96) e -= 'W';
 			else e -= '0';
 
 			b[4] = (byte) (((d & 0x0f) << 4) | (e & 0x0f));
 
 
 			d = hw_addr.charAt(15);
 			if(d > 96) d -= 'W';
 			else d -= '0';
 
 			e = hw_addr.charAt(16);
 			if(e > 96) e -= 'W';
 			else e -= '0';
 
 			b[5] = (byte) (((d & 0x0f) << 4) | (e & 0x0f));
 
 
 			return b;*/
 		}
 	}
 
 	public interface WAP_Listener {
 		public void onComplete(int size, long time);
 	}
 
 }
