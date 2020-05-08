 package de.da_sense.moses.client.com.requests;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.preference.PreferenceManager;
 import de.da_sense.moses.client.abstraction.HardwareAbstraction;
 import de.da_sense.moses.client.com.NetworkJSON;
 import de.da_sense.moses.client.com.ReqTaskExecutor;
 import de.da_sense.moses.client.preferences.MosesPreferences;
 import de.da_sense.moses.client.service.MosesService;
 
 public class RequestUninstalledAPK {
 	ReqTaskExecutor e;
 	JSONObject j;
 
 	public RequestUninstalledAPK(ReqTaskExecutor e, String sessionID, String appId) {
 		j = new JSONObject();
 		this.e = e;
 		try {
 			String userid = PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).getString(
 					MosesPreferences.PREF_EMAIL, "");
 			j.put("MESSAGE", "APK_UNINSTALLED");
 			j.put("SESSIONID", sessionID);
 			j.put("DEVICEID", HardwareAbstraction.extractDeviceIdFromSharedPreferences());
 			j.put("USERID", userid);
 			j.put("APKID", appId);
 		} catch (JSONException ex) {
 			e.handleException(ex);
 		}
 	}
 
 	public void send() {
 		NetworkJSON task = new NetworkJSON();
 		NetworkJSON.APIRequest req;
 		req = task.new APIRequest();
 		req.request = j;
 		req.reqTaskExecutor = this.e;
 		task.execute(req);
 	}
 }
