 package com.aha.services;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import com.aha.models.Constants;
 import com.aha.models.NetworkInfo;
 
 
 import android.os.Build;
 
 public class Device {
 	
 	HashMap<String, Integer> deviceMap = new HashMap<String, Integer>();
 	public static final int DROID2 = 0;
 	public static final int NEXUSONE = 1;
 	public static final int ERIS = 2;	
 	private int device;
 
 	public Device() {		
 		deviceMap.put("DROID2", new Integer(0));
 		deviceMap.put("Nexus One", new Integer(1));
 		deviceMap.put("Eris FroshedYo v11", new Integer(2));
 	}
 	
 	public int getDevice() {
 		return device;
 	}
 
 	public void setDevice(int device) {
 		this.device = device;
 	}	
 	
 	public void connectDevice() {
 		String model = Build.MODEL;		
 		device = (Integer)deviceMap.get(model).intValue();		
 		int ip = NetworkInfo.getInstance().getMyIP();
 		
         switch (device) {	    	        
 	        case DROID2: 
 	        	initDroid(ip);
 		        break; 
 	        case NEXUSONE: 
 	        	initNexus(ip);
 		        break; 
 	        case ERIS: 
 	        	initEris(ip);
 		        break;     
         }		
 	}	
 	
 	public void disconnectDevice() {
 		String model = Build.MODEL;		
 		int device = (Integer)deviceMap.get(model).intValue();		
 		
         switch (device) {	    	        
 	        case DROID2: 
 	        	disconnectDroid();
 		        break; 
 	        case NEXUSONE: 
 	        	disconnectNexus();
 		        break; 
 	        case ERIS: 
 	        	disconnectEris();
 		        break;     
         }		
 	}	
 	
 	public boolean doesNetworkExist() {
 		String model = Build.MODEL;		
 		int device = (Integer)deviceMap.get(model).intValue();		
 		boolean status = false;
 		
         switch (device) {	    	        
         case DROID2: 
         	status = getStatusDroid();
 	        break; 
         case NEXUSONE: 
         	status = true;
 	        break; 
         case ERIS: 
         	status = true;
 	        break;     
     }		
 		return status;
 	}
 	
	public void changeIP (int ipint) {
 		String model = Build.MODEL;		
		int device = (Integer)deviceMap.get(model).intValue();	
		
		String ip = Constants.BASE_ADDRESS + ipint;
 		//System.out.println("" + device + " " + model);
 		
 		try {
 			
 			Process ps = Runtime.getRuntime().exec("su");
 			DataOutputStream out = new DataOutputStream(ps.getOutputStream());
 			
 	        switch (device) {	    	        
 	        case DROID2: 
 	        	out.writeBytes("ifconfig tiwlan0 " + ip + "\n");
 		        break; 
 	        case NEXUSONE: 
 	        	out.writeBytes("ifconfig tiwlan0 " + ip + "\n");
 		        break; 
 	        case ERIS: 
 	        	out.writeBytes("ifconfig tiwlan0 " + ip + "\n");
 		        break;     
 	        }			
 			out.writeBytes("exit\n");
 			out.flush();
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException se) {
 			// TODO Auto-generated catch block
 			se.printStackTrace();
 		} 		
 	}	
 	
 	public void initDroid(int ip) {		
 
 		try {
 			
 			
 			
 			//NetworkInfo.getInstance().setMyIP("192.168.0.5");
 			
 			Process ps = Runtime.getRuntime().exec("su");
 			DataOutputStream out = new DataOutputStream(ps.getOutputStream());
 			out.writeBytes("sh /data/jay/connect.sh " + Constants.BASE_ADDRESS + ip + "\n");
 			out.writeBytes("exit\n");
 			out.flush();
 			
 			
 			
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException se) {
 			// TODO Auto-generated catch block
 			se.printStackTrace();
 		} 
 	}	
 	
 	public void disconnectDroid() {
 
 		try {
 			Process ps = Runtime.getRuntime().exec("su");
 			DataOutputStream out = new DataOutputStream(ps.getOutputStream());
 			out.writeBytes("ifconfig tiwlan0 down\n");
 			out.writeBytes("stop wpa_supplicant\n");
 			out.writeBytes("rmmod tiwlan_drv\n");
 			out.writeBytes("exit\n");
 			out.flush();
 		
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException se) {
 			// TODO Auto-generated catch block
 			se.printStackTrace();
 		} 
 	}	
 	
 	public boolean getStatusDroid() {
 
 		try {
 			Process ps = Runtime.getRuntime().exec("su");
 			DataOutputStream out = new DataOutputStream(ps.getOutputStream());
 			//out.writeBytes("ls\n");
 			//out.writeBytes("cd /system/etc/wifi\n");
 //			out.writeBytes("wlan_cu -itiwlan0 -s /data/jay/stat.sh\n");
 			out.writeBytes("ifconfig tiwlan0\n");
 			out.flush();
             BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
             String line = null;
             
         	line = br.readLine();
         	
 			br.close();
 			out.writeBytes("exit\n");
 			out.flush();
 			out.close();        	
         	
         	String[] array = line.split(" "); 
         	
         	if (array.length == 10)
         		return true;
         	else
         		return false;
 		
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException se) {
 			// TODO Auto-generated catch block
 			se.printStackTrace();
 		} 
 		
 		return false;
 		
 	}
 
 	public void initNexus(int ip) {		
 
 		try {
 			Process ps = Runtime.getRuntime().exec("su");
 			DataOutputStream out = new DataOutputStream(ps.getOutputStream());
 			out.writeBytes("insmod /system/lib/modules/bcm4329.ko\n");
 			out.writeBytes("sleep 5\n");
 			out.writeBytes("ifconfig eth0 " + Constants.BASE_ADDRESS + ip + " netmask 255.255.255.0\n");
 			out.writeBytes("ifconfig eth0 up\n");
 			out.writeBytes("./data/data/android.tether/bin/iwconfig eth0 mode ad-hoc\n");
 			out.writeBytes("./data/data/android.tether/bin/iwconfig eth0 essid hope\n");
 			out.writeBytes("exit\n");
 			out.flush();
 		
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException se) {
 			// TODO Auto-generated catch block
 			se.printStackTrace();
 		} 
 	}	
 	
 	public void disconnectNexus() {
 
 		try {
 			Process ps = Runtime.getRuntime().exec("su");
 			DataOutputStream out = new DataOutputStream(ps.getOutputStream());
 			out.writeBytes("ifconfig eth0 down\n");
 			out.writeBytes("rmmod bcm4329\n");
 			out.writeBytes("exit\n");
 			out.flush();
 		
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException se) {
 			// TODO Auto-generated catch block
 			se.printStackTrace();
 		} 
 	}	
 	
 	public void initEris(int ip) {		
 
 		try {
 			Process ps = Runtime.getRuntime().exec("su");
 			DataOutputStream out = new DataOutputStream(ps.getOutputStream());
 			out.writeBytes("insmod /system/lib/modules/wlan.ko\n");
			out.writeBytes("sleep 8\n");
 			out.writeBytes("wlan_loader -f /system/etc/wifi/Fw1251r1c.bin -e /proc/calibration -i /system/etc/wifi/tiwlan.ini\n");
 			out.writeBytes("sleep 2\n");
 			out.writeBytes("ifconfig tiwlan0 " + Constants.BASE_ADDRESS + ip + " netmask 255.255.255.0 up\n");
 			out.writeBytes("exit\n");
 			out.flush();
 		
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException se) {
 			// TODO Auto-generated catch block
 			se.printStackTrace();
 		} 
 	}	
 
 	public void disconnectEris() {
 
 		try {
 			Process ps = Runtime.getRuntime().exec("su");
 			DataOutputStream out = new DataOutputStream(ps.getOutputStream());
 			out.writeBytes("ifconfig tiwlan0 down\n");
 			out.writeBytes("rmmod wlan\n");
 			out.writeBytes("exit\n");
 			out.flush();
 		
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException se) {
 			// TODO Auto-generated catch block
 			se.printStackTrace();
 		} 
 	}	
 	
 	
 }
