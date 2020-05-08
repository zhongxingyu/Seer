 package com.htb.cnk.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 import com.htb.cnk.lib.Http;
 import com.htb.constant.Server;
 
 public class Notifications {
 	public class NotificationItem {
 		protected String mType[];
 		protected int mId;
 
 		public NotificationItem(int id, String type[]) {
 			mType = type;
 			mId = id;
 		}
 
 		public int getId() {
 			return mId;
 		}
 
 		public String[] getTypeAll() {
 			return mType;
 		}
 		
 		public String getType(int index){
 			return mType[index];
 		}
 	}
 	static List<NotificationItem> notifications = new ArrayList<NotificationItem>();
 //	private NotificationTypes mNotificationType = new NotificationTypes();
 
 	public Notifications() {
 
 	}
 
 	private void add(NotificationItem item) {
 		notifications.add(item);
 	}
 
 	public int size() {
 		return notifications.size()-1;
 	}
 	
 	public void clear(){
 		notifications.clear();
 	}
 	public int getId(int index) {
 		if(index >= notifications.size()){
 			return -1;
 		}
 		return notifications.get(index).getId();
 	}
 
 	public String[] getTypeAll(int index) {
 		int i;
 //		if(index >= notifications.size()){
 //			return null;
 //		}
 		
 		for(i = 0;i< notifications.size()-1;i++){
 			if(index == notifications.get(i).getId()){
 				break;
 			}
 		}
 //		Log.d("getId","getId:"+notifications.get(i).getId()+" index:"+index);
 		return notifications.get(i).getTypeAll();
 	}
 	
 	public String getType(int notification,int type){
 		return notifications.get(notification).getType(type);
 	}
 
 	public int getNotifiycations() {
 		String notificationPkg = Http.get(Server.GET_NOTIFICATION, null);
 		if (notificationPkg == null) {
 			return -1;
 		}
 		try {
 			Log.d("pkg", notificationPkg);
 			JSONArray tableList = new JSONArray(notificationPkg);
 			int length = tableList.length();
 			NotificationItem asItem;
 			Notifications setting = new Notifications();
 			for (int i = 0; i < length; i++) {// 遍历JSONArray
 				JSONObject item = tableList.getJSONObject(i);
 				int id = item.getInt("tid");
 				String notifications = item
 						.getString("notifications");
 				String types = notifications.substring(1, notifications.indexOf(']'));
 				String typesTemp[] = null; 
 				typesTemp= types.split(",");
 				asItem = new NotificationItem(id,typesTemp);
 				setting.add(asItem);
 				Log.d("settings", "settings:"+asItem.getId());
 			}
 			return 0;
 		} catch (Exception e) {
			// TODO: handle exception
 		}
 
 		return -1;
 	}
 
 	public List <String> getNotifiycationsType(int index){
 		Notifications setting = new Notifications();
 		String types[] = setting.getTypeAll(index);
 		Log.d("type", types[0]);
 		List <String> temp = new ArrayList<String>();
 		for(int i = 0; i < types.length;i++){
 			Log.d("type", types[i]);
 			temp.add(NotificationTypes.getName(Integer.parseInt(types[i]))) ;
 		}
 		return temp;
 	}
 	
 	public int cleanNotifications(int index){
 		Log.d("index", "index "+index);
 		String notificationPkg = Http.get(Server.CLEANNOTIFICATION, "TID=" + index);
 		
 		if (notificationPkg == null) {
 			return -1;
 		}
 		return 0;
 	}
 
 }
