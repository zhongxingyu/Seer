 package appbreeder.netupdate;
 
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.ByteArrayEntity;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.util.Log;
 import appbreeder.activity.R;
 import appbreeder.controls.gadget.ABTabRecord;
 
 public class RequestBuilder {
 	private static String serviceHost;
 
 	public static String getServiseHost(Context mContext) {
 		if (serviceHost == null || serviceHost.length() == 0) {
 			if (mContext != null) {
 				serviceHost = mContext.getResources().getString(
 						R.string.service_host);
 			} else
 				serviceHost = "";
 		}
 
 		return serviceHost;
 	}
 
 	public static void setRequestBaseHeader(HttpUriRequest request) {
 		
 		 request.setHeader("Accept", "application/json");
 		 request.setHeader("Content-Type", "application/json; charset=UTF-8");
 	}
 
 	public static HttpUriRequest buildReqest_GetSQLiteDatabase(int appID) {
 		HttpPost request = new HttpPost(getServiseHost(null)
 				+"/GetSQLiteDatabase");
 		setRequestBaseHeader(request);
 		JSONObject json = new JSONObject();
 		   try {
 			json .put("ID", appID);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		   try {
 			request.setEntity(new ByteArrayEntity(json.toString().getBytes("UTF8")));
 		} catch (UnsupportedEncodingException e) {	
 			e.printStackTrace();
		}
     
         
 		return request;
 	}
 	public static HttpUriRequest buildReqest_checkABTab(int appID,List<ABTabRecord> listTabs) {
 		HttpPost request = new HttpPost(getServiseHost(null)
 				+"/GetSQLiteDatabase");
 		setRequestBaseHeader(request);
 		JSONObject json = new JSONObject();
 		   try {
 			json .put("AppID", appID);
 			JSONArray jsonArrayTabs=new JSONArray();
 			for(ABTabRecord tab:listTabs)
 			{
 				JSONObject jsonTab = new JSONObject();
 				jsonTab.put("ID",tab.getID());
 				jsonTab.put("TimeStamp",tab.getServerTimeStamp());
 				jsonArrayTabs.put(jsonTab);
 			}
 			json.put("jsonArray",jsonArrayTabs);
 			
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		   Log.i("buildReqest_checkABTab","resalt"+json);
 		   try {
 			request.setEntity(new ByteArrayEntity(json.toString().getBytes("UTF8")));
 		} catch (UnsupportedEncodingException e) {	
 			e.printStackTrace();
 		}
 		return request;
 	}
 	
 	public static HttpUriRequest buildReqest_LoadDB(String urlDB) {
 		HttpGet request = new HttpGet(urlDB);//?ID="+appID);
 		return request;
 	}
 	public static HttpUriRequest buildReqest_CheckAppForUpdate(String appData, String skinData) {
 		HttpUriRequest request = new HttpGet(getServiseHost(null)
 				+ "update-profile");
 		setRequestBaseHeader(request);
 		return request;
 	}
 }
