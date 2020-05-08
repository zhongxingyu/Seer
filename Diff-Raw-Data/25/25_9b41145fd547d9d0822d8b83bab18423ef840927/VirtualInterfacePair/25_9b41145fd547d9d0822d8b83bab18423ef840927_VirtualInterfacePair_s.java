 package net.holyc.host;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.util.Log;
 
 /**
 * The class to manage each pair of veth
 * 
 * @author Te-Yuan Huang (huangty@stanford.edu)
 */
 class VirtualInterfacePair{
 	private Context context = null;
 	private VirtualInterface veth0 = null;
 	private VirtualInterface veth1 = null;
 	private String TAG = "HOLYC.VirtualInterfacePair";
 	
 	public VirtualInterfacePair() {
 		init();
 	}
 	
 	public void init(){
 		//String result = NativeCallWrapper.getResultByCommand("/data/local/bin/busybox ip link | grep veth0");
 		String result = "";
		ArrayList<String> resList = Utility.runRootCommand("/data/local/bin/busybox ip link | grep veth0", true);
 		if(resList.size() > 0){
 			result = resList.get(0);
 		}
 		
 		StringBuffer sb = new StringBuffer("veth0");
 		if(!result.contains(sb.subSequence(0, sb.length()))){ //the veth0/1 pair hasn't been created
 			Utility.runRootCommand("/data/local/bin/busybox ip link add type veth", true);
 		}else{
 			Log.d(TAG, "the veth0/1 pair is already been created, and the result was = " + result);
 		}		
 		veth0 = new VirtualInterface("veth0");
 		veth0.setInterfaceEnable(true);
 		veth1 = new VirtualInterface("veth1");
 		veth1.setInterfaceEnable(true);
 	}
 	
 	public String getNames(){
 		String names = veth0.getName() + " " + veth1.getName();
 		return names;
 	}
 	public String getIPs(){
 		String ips =  "veth0: " + veth0.getIP() + " veth1: " + veth1.getIP();
 		return ips;
 	}
 	public String getMacs(){
 		String macs =  "veth0: " + veth0.getMac() + " veth1: " + veth1.getMac();
 		return macs;
 	}
 	public void setIP(String dev, String ip, String mask){
 		if(dev == "veth1"){
 			veth1.setIP(ip, mask);
 		}else if (dev == "veth0"){
 			veth0.setIP(ip, mask);
 		}else{
 			Log.d(TAG, "trying to set ip to an non-existing virtual interface");
 		}
 	}
 	
 	public VirtualInterface getVeth0(){
 		return veth0;
 	}
 	public VirtualInterface getVeth1(){
 		return veth1;
 	}	
 }
