 package net.holyc.host;
 
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Set;
 
 import android.util.Log;
 
 /**
  * The class to record all the information about OVS
  * 
  * @author Te-Yuan Huang (huangty@stanford.edu)
  */
 
 class VirtualSwitch{	
 	String TAG = "HOLYC.VirtualSwitch";
 	private static HashMap<String, LinkedList<String>> dptable;
 	VirtualSwitch(){		
 		dptable = new HashMap<String, LinkedList<String>>();
 		loadKernelModule();
 	}
 	public void loadKernelModule(){		
 		//String result = NativeCallWrapper.getResultByCommand("lsmod | grep openvswitch");
 		String result = "";
		ArrayList<String> resList = Utility.runRootCommand("lsmod | grep openvswitch", true);
 		if(resList.size() >0){
 			result = resList.get(0);
 		}
 		StringBuffer sb = new StringBuffer("openvswitch");
 		if(!result.contains(sb.subSequence(0, sb.length()))){			
 	   		Utility.runRootCommand("insmod /data/local/lib/openvswitch_mod.ko" , false);
 		}else{
 			Log.d(TAG, "no need to insmod, since lsmod result = " + result);
 		}
 	}
 	public void addDP(String dp){
 		if(!dptable.containsKey(dp)){			
 	   		Utility.runRootCommand("/data/local/bin/ovs-dpctl add-dp "+ dp , false);
 			Log.d(TAG, "OVS add-dp " + dp);
 		}else{ 
 			//no need to add datapatch
 			Log.d(TAG, "no need to add dp0, since it's already there");
 			return;
 		}
 		dptable.put(dp, new LinkedList<String>());		
 	}
 	public void addIF(String dp, String intf){
 		if(dptable.containsKey(dp)){
 			Utility.runRootCommand("/data/local/bin/ovs-dpctl add-if " + dp + " " + intf , false);
 			dptable.get(dp).add(intf);
 		}else{
 			Log.e(TAG, "datapatch not exists");
 		}
 	}
 	public String getDP(int index){
 		Set<String> dps = dptable.keySet();
 		String dpname = "";
 		Iterator<String> it = dps.iterator();
 		int i = 0;
 		while(it.hasNext()){
 			if(i== index){
 				dpname = it.next();
 				break;
 			}
 			i++;
 		}
 		return dpname;
 	}
 	public String getDPs(){
 		Set<String> dps = dptable.keySet();
 		String dpname = "";
 		Iterator<String> it = dps.iterator();
 		while(it.hasNext()){
 			dpname = dpname + it.next() + " ";
 		}
 		return dpname;
 	}
 }
