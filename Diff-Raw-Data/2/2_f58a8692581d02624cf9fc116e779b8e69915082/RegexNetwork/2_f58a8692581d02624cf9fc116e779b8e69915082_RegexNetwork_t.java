 package jiunling.pass.wifi;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import jiunling.pass.root.SuperUser;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class RegexNetwork {
 	/***	Debugging	***/
 //	private static final String TAG = "RegexNetwork";
 //	private static final boolean D = true;
 	
 	private static final String GetNetWorkShell = "cat /data/misc/wifi/wpa_supplicant.conf;";
 	
 	private SuperUser mSuperUser 	= null;
 	
 	private String psk 				= null;
 	
 	private boolean verify 			= true;
 	
 	public RegexNetwork() {
 		if( mSuperUser == null) mSuperUser = new SuperUser();
 	}
 	
 	public void getNetwork(String SSID) {
 		if(mSuperUser.runShell(GetNetWorkShell)) {
 			String command = mSuperUser.getResult();
 			
 			try {
 				JSONObject json = findSpecified(command, SSID);
 				if(json != null) {
 					if(!json.isNull("psk")) psk = json.getString("psk");
 				}
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private JSONObject findSpecified(String command, String SSID) {
 		Pattern mPattern = Pattern.compile("network=[{][^}]+[}]", Pattern.DOTALL | Pattern.MULTILINE);
 		Matcher mMatcher = mPattern.matcher(command);
 		
 		while (mMatcher.find()) {
         	String group = mMatcher.group();
         	group = group.replace("network=","").replace("\n",",").replace(",}","}").replaceFirst(",","");
         	
 			try {
 				JSONObject json = new JSONObject(group);
 				if(SSID.equals(json.getString("ssid"))) {
 					return json;
 				} else { 
 					continue;
 				}
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
//				e.printStackTrace();
 				verify = false;
 			}
         }
 		return null;
 	}
 	
 	public boolean verify() {
 		return verify;
 	}
 	
 	public String getPSk() {
 		return psk;
 	}
 }
