 package net.holyc.host;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import android.util.Log;
 
 /**
  * The interface class is a super class, which defines
  * the basic attributes of an interface and provides 
  * API to get/set these attributes.
  * 
  * @author Yongqiang Liu (yliu78@stanford.edu)
  * @author Te-Yuan Huang (huangty@stanford.edu)
  */
 
 public abstract class HostInterface {
 	static final String TAG = "HostInterface";
 	private String name = null;
 	private String IP = null;
 	private String mask = null;
 	private String mac = null;
 	private HostInterface gateway;
 	private boolean isVirtual = false;
 	
 	public abstract void setInterfaceEnable(boolean enable);	
 	public abstract boolean getInterfaceEnable();
 	
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public void setIP(String IP) {
 		this.IP = IP;
 	}
 	
 	public void setMask(String mask) {
 		this.mask = mask;
 	}
 	
 	public void setMac(String mac) {
 		this.mac = mac;
 	}
 	
 	public void setGateway(HostInterface gateway) {
 		this.gateway = gateway;
 	}
 	
 	public void setIsVirtual(boolean virtual){
 		this.isVirtual = virtual;
 	}
 	
 	public String getName() {
 		if (name == null) name = searchName();
 		return name;
 	}
 	
 	public boolean getIsVirtual(){
 		return this.isVirtual;
 	}
 	
 	public String getIP() {
 		if (IP == null) IP = searchIP();
 		return IP;
 	}
 	
 	public String getMask() {
 		if (mask == null) mask = searchMask();
 		return mask;
 	}
 	
 	public String getMac() {
 		if (mac == null) mac = searchMac();
 		return mac;
 	}
 	
 	public HostInterface getGateway() {
 		if (gateway == null) gateway = searchGateway();
 		return gateway;
 	}
 	
 	/**
 	 * It must be overwritten by derived class.
 	 * (Te-Yuan: since it must be overwritten, I made it abstract) 
 	 */
 	public abstract String searchName();
 	
 	public void removeIP(){		
    		Utility.runRootCommand("/data/local/bin/busybox ifconfig " + getName() + " 0.0.0.0" , false);
 	}
 	/**
 	 * the super class provides a set of methods to 
 	 * retrieve IP address by using busybox output.
 	 * If the device does not have busybox, the derived
 	 * class should implement its own search methods 
 	 */
 	public String searchIP() {
 		if (name == null) return null;
		String command = "/data/local/bin/busybox ifconfig " + name + " | /data/local/bin/busybox grep inet";
 		String token = "addr:";
 		return this.getValueByBusyBox(command, token);
 	}
 	
 	public String searchMask() {
 		if (name == null) return null;
		String command = " /data/local/bin/busybox ifconfig " + name + " | /data/local/bin/busybox grep Mask";
 		String token = "Mask:";
 		return this.getValueByBusyBox(command, token);
 	}
 	
 	public String searchMac() {
 		if (name == null) return null;
		String command = " /data/local/bin/busybox ifconfig " + name + " | /data/local/bin/busybox grep HWaddr";
 		String token = "HWaddr ";
 		return this.getValueByBusyBox(command, token);
 	}
 	
 	public HostInterface searchGateway() {
 		return null;
 	}
 	
 	
 	public String getMacFromIPByPing(String IP) {
 		String mac = null;		
 		Utility.runRootCommand("/data/local/bin/busybox ping " + IP + " -c 1 -w 1", false);
		ArrayList<String> result = Utility.runRootCommand("/data/local/bin/busybox arp -a -n | /data/local/bin/busybox grep " + IP, true);
 		if(result.size() > 0){
 			Iterator<String> rit = result.iterator();
 			//the result only has one line		
 			String resLine = rit.next();
 			
 			String[] items = resLine.split("\t| +");
 			for (int i = 0; i < items.length; i++) {
 				//Log.d(TAG, "items[" + i + "]: " + items[i]);
 				if (items[i].contains(":") == true) {
 					mac = items[i];
 					break;
 				}
 			}		
 		}
 		return mac;
 	}
 	
 	/*public String getMacFromIPByArpRequest(String IP){
 		String mac = null;
 		String command = "su -c \"/data/local/bin/busybox arping -I "+ getName()+" -c 1 "+ IP + " | awk 'NR==2{print\\$5}' | sed 's/\\[//g' | sed 's/\\]//g' \"";
 		while(mac == null || !isValidMAC(mac)){
 			mac = NativeCallWrapper.getResultByCommand(command);
 			Log.d(TAG, "ARP Reply:" + mac);
 			mac.replace("\n", "");
 			mac.replace("\r", "");
 		}		
 		return patchMAC(mac);
 	}*/
 	
 	private String getValueByBusyBox(String command, String token) {
 		String value = null;		
 		ArrayList<String> result = Utility.runRootCommand(command, true);
 		if(result.size() > 0){
 			Iterator<String> rit = result.iterator();
 			//the result only has one line
 			String resLine = rit.next();
 	
 			//Log.d(TAG, "resLine is " + resLine);
 			String[] items = resLine.split("  ");
 			for (int i = 0; i < items.length; i++) {
 				int index = items[i].indexOf(token);
 				//Log.d(TAG, "items[" + i + "]: " + items[i] + "; index = " + index);
 				if (index >= 0) {
 					index += token.length();
 					//Log.d(TAG, "target is " + items[i] + "; index = " + index);
 					value = items[i].substring(index);
 					break;
 				} 
 			}
 		}
 		return value;
 	}
 	
 	/** The function to patch the returned MAC address from "2:50:f3:0:0:0" to "02:50:f3:00:00:00" */
 	public String patchMAC(String mac){
 		String patched = null;
 		if(isValidMAC(mac)){
 			String[] segments = mac.split(":");
 			
 			for (int i = 0 ; i< segments.length ; i++){
 				String seg = segments[i].trim();
 				if(seg.length()<2){
 					seg = "0"+ seg;
 				}
 				if(patched == null){
 					patched = seg;
 				}else{
 					patched = patched + ":" + seg;
 				}
 			}
 		}
 		return patched;
 	}
 	
 	/** The function to check if the MAC address is valid */
 	public boolean isValidMAC(String mac){
 		boolean valid = true;
 		String[] segments = mac.split(":");
 		Log.d(TAG, "original MAC = " + mac);
 	    if(segments.length != 6){
 	    	valid = false;	    	
 	    }else{
 			for (int i = 0 ; i< segments.length ; i++){
 				try{
 					Integer.parseInt(segments[i].trim(), 16);
 				}catch(Exception e){
 					valid = false;
 				}
 			}
 	    }
 	    if(valid == false){
 	    	Log.d(TAG, mac + " is an invalid MAC address");
 	    }
 		return valid;
 	}
 }
