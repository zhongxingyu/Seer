 package com.aha.services;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Iterator;
 
 
 import android.os.Build;
 
 public class Device {
 	
 	HashMap<String, Integer> deviceMap = new HashMap<String, Integer>();
 	public static final int DROID2 = 0;
 	public static final int NEXUSONE = 1;
 	public static final int ERIS = 2;	
		
 	public Device() {		
 		deviceMap.put("DROID2", new Integer(0));
 		deviceMap.put("Nexus One", new Integer(1));
 		deviceMap.put("Eris FroshedYo v11", new Integer(2));
 	}
 	
 	public void connectDevice() {
 		String model = Build.MODEL;		
		int device = (Integer)deviceMap.get(model).intValue();		
 		System.out.println("" + device + " " + model);
 		
         switch (device) {	    	        
 	        case DROID2: 
 	        	initDroid();
 		        break; 
 	        case NEXUSONE: 
 	        	initNexus();
 		        break; 
 	        case ERIS: 
 	        	initEris();
 		        break;     
         }		
 	}	
 	
 	public void disconnectDevice() {
 		String model = Build.MODEL;		
 		int device = (Integer)deviceMap.get(model).intValue();		
 		System.out.println("" + device + " " + model);
 		
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
 		System.out.println("" + device + " " + model);
 		boolean status = false;
 		
         switch (device) {	    	        
         case DROID2: 
         	status = getStatusDroid();
 	        break; 
         case NEXUSONE: 
        	disconnectNexus();
 	        break; 
         case ERIS: 
        	disconnectEris();
 	        break;     
     }		
 		return status;
 	}
 	
 	public void changeIP (String ip) {
 		String model = Build.MODEL;		
 		int device = (Integer)deviceMap.get(model).intValue();		
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
 	
 	public void initDroid() {		
 
 		try {
 			
 			Process ps = Runtime.getRuntime().exec("su");
 			DataOutputStream out = new DataOutputStream(ps.getOutputStream());
 			out.writeBytes("sh /data/jay/connect.sh\n");
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
             System.out.println("<OUTPUT>");
             
         	line = br.readLine();
         	System.out.println("----------------------" + line);
         	
 			br.close();
 			out.writeBytes("exit\n");
 			out.flush();
 			out.close();        	
         	
         	String[] array = line.split(" "); 
 			
         	System.out.println("----------------------" + "" + array.length);
         	System.out.println("----------------------" + array[0]);
         	
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
 
 	public void initNexus() {		
 
 		try {
 			Process ps = Runtime.getRuntime().exec("su");
 			DataOutputStream out = new DataOutputStream(ps.getOutputStream());
 			out.writeBytes("insmod /system/lib/modules/bcm4329.ko\n");
 			out.writeBytes("sleep 5\n");
 			out.writeBytes("ifconfig eth0 192.168.0.4 netmask 255.255.255.0\n");
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
 	
 	public void initEris() {		
 
 		try {
 			Process ps = Runtime.getRuntime().exec("su");
 			DataOutputStream out = new DataOutputStream(ps.getOutputStream());
 			out.writeBytes("insmod /system/lib/modules/wlan.ko\n");
 			out.writeBytes("sleep 5\n");
 			out.writeBytes("wlan_loader -f /system/etc/wifi/Fw1251r1c.bin -e /proc/calibration -i /system/etc/wifi/tiwlan.ini\n");
 			out.writeBytes("sleep 2\n");
 			out.writeBytes("ifconfig tiwlan0 192.168.0.3 netmask 255.255.255.0 up\n");
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
