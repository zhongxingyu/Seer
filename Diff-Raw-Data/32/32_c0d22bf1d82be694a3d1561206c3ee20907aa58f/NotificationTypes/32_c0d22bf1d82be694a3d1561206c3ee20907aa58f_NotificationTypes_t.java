 package com.htb.cnk.data;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.htb.cnk.lib.Http;
 import com.htb.constant.Server;
 
 public class NotificationTypes {
 	static ArrayList<HashMap<Integer, String>> notificationTypes = new ArrayList<HashMap<Integer, String>>();
 
 	public NotificationTypes() {
 		
 	}
 	
 	public static String getName(int index) {
 		return notificationTypes.get(0).get(index);
 	}
 	
 	public int getNotifiycationsType() {
 		String notificationTypePkg = Http.get(Server.GET_NOTIFICATIONTYPES,
 				null);
 		if (notificationTypePkg == null) {
 			return -1;
 		}
 		try {
 			JSONArray tableList = new JSONArray(notificationTypePkg);
 			int length = tableList.length();
 			HashMap<Integer, String> map = new HashMap<Integer, String>();
 			for (int i = 0; i < length; i++) {// 遍历JSONArray
 				JSONObject item = tableList.getJSONObject(i);
 				int id = item.getInt("nid");
 				String value = item.getString("value");
 				map.put(id, value);
 			}
 			notificationTypes.add(map);
 			return 0;
 		} catch (Exception e) {
			e.printStackTrace();
 		}
 
 		return -1;
 	}
 }
