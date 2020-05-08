 package com.joy.launcher2.network.handler;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.joy.launcher2.util.Constants;
 import com.joy.launcher2.util.Util;
 
 import android.R.integer;
 import android.util.Log;
 /**
  * 游戏、应用列表
  */
 public class AppListHandler {
 	private static final String TAG = "AppListHandler";
 	private static final Boolean DEBUG = true;
 
 	public static int index = 1;
 	public static int num = 1;
 	public ArrayList<List<Map<String, Object>>> getAppList(String string,int row,int type) {
 		if (string == null) {
 			string = getAppLisLocalInfo(type);
 		}
 		ArrayList<List<Map<String, Object>>> arrayList =new ArrayList<List<Map<String, Object>>>();
 		try {
			if (string == null) {
				return null;
			}
 			JSONObject json = new JSONObject(string);
 			if (json == null) {
 				return null;
 			}else if (json.getInt("state") != 1) {
				return null;
 			}
 			JSONArray jsonarry = json.getJSONArray("item");
 		
 		int length = jsonarry.length();
 
 		int sum = length;
 		int page = sum/row;
 		if (sum%row!=0) {
 			page +=1;
 		}
 
 		for (int j = 0; j < page; j++) {
 			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
 			for(int k=j*row;k<j*row+row;k++){
 				int i = k;
 				if (k>=sum) {
 					i = k%sum;
 				}
 				Map<String, Object> map = new HashMap<String, Object>();
 				JSONObject item = jsonarry.getJSONObject(i);
 				map.put("id", item.getInt("id"));
 				map.put("icon", item.getString("icon"));
 				map.put("name", item.getString("name"));
 				map.put("size", item.getInt("size"));
 				map.put("url", item.getString("url"));
 				list.add(map);
 			}
 			 arrayList.add(list);
 		}
 		JSONObject pageJsonObject = json.getJSONObject("page");
 		index = pageJsonObject.getInt("pi");
 		num = pageJsonObject.getInt("pn");
 		index = index+1;
 		if (index > num) {
 			index = 1;
 		}
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 		if (arrayList.size()>0) {
 			saveAppLisLocalInfo(type, string);
 		}
 		return arrayList;
 	}
 	
 	public static String getAppLisLocalInfo(int type){
 
 		String string = Util.readString(Constants.DOWNLOAD_JSON_DIR+"/"+type+"-"+Constants.FILENAME_APP_LIST);
 		return string;
 	}
 	
 	private static void saveAppLisLocalInfo(int type,String string){
 		if (string != null&&!string.equals("")) {
 			Util.saveString(Constants.DOWNLOAD_JSON_DIR+"/"+type+"-"+Constants.FILENAME_APP_LIST, string);
 		}
 	}
 }
