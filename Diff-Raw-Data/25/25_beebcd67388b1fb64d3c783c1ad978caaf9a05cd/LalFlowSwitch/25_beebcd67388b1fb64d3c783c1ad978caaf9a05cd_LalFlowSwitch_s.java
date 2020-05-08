 package edu.stanford.lal;
 
 import java.util.HashMap;
 
 import net.holyc.HolyCIntent;
 import net.holyc.host.Utility;
 import net.holyc.openflow.handler.FlowSwitch;
 
 import org.openflow.protocol.OFMatch;
 import org.openflow.util.U8;
 import org.openflow.util.U16;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 /**
  * Customized L2 learning switch
  * 
  * @author ykk
  */
 public class LalFlowSwitch extends FlowSwitch {
 	/**
 	 * Log name
 	 */
 	private String TAG = "Lal.FlowSwitch";
 	/**
 	 * Hashmap for app name
 	 */
 	public static HashMap<String, Object> appNames = new HashMap<String, Object>();
 
 	public int getCookie(OFMatch ofm, Context context) {
	    Utility.sendQueryRequest();
 	    return 0;
 	}
 
 	protected static String ipToString(int ip) {
 		return Integer.toString(U8.f((byte) ((ip & 0xff000000) >> 24))) + "."
 				+ Integer.toString((ip & 0x00ff0000) >> 16) + "."
 				+ Integer.toString((ip & 0x0000ff00) >> 8) + "."
 				+ Integer.toString(ip & 0x000000ff);
 	}
 
 }
