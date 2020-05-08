 package cmov.pa;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.ProtocolException;
 import java.util.ArrayList;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.HttpConnectionParams;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import cmov.pa.utils.HouseInfo;
 
 import android.app.Application;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 
 
 public class Api extends Application{
 	
 	public static final String IP = "http://95.92.19.188:3001";
 	public static cmov.pa.database.DatabaseAdapter dbAdapter;
 	
 	public final static String c2dmAccount = "cmov.c2dm@gmail.com"; 
 	private static final String TAG = "Real Estate App";
 	
 	
 	public static NotificationManager mNotificationManager;
 	public static Context mcontext;
 	
 	
 	public static ArrayList<HouseInfo> notifications_new_list = new ArrayList<HouseInfo>(); ;
 	public static ArrayList<HouseInfo> notifications_updated_list = new ArrayList<HouseInfo>();	
 	
 	public static ArrayList<HouseInfo> available_new_list = new ArrayList<HouseInfo>(); ;
 	public static ArrayList<HouseInfo> available_updated_list = new ArrayList<HouseInfo>();
 	
 	public static int count = 1;
 	
 	
 	public final String MODE_FAVOURITE = "favourites";
 	public final String MODE_NOTIFICATION_NEW = "notification_new";
 	public final String MODE_NOTIFICATION_UPDATE = "notification_update";
 	public final String MODE_AVAILABLE_NEW= "available_new";
 	public final String MODE_AVAILABLE_UPDATE= "available_update";
 
 	
 	
 	///////////////////////////////////////////////////////////////////////
 	//						CHAMADAS AO VERVIDOR						//
 	///////////////////////////////////////////////////////////////////////
 	
 	public int registerKey(String key) throws ClientProtocolException, IOException{
 		
 		final HttpClient httpClient =  new DefaultHttpClient();
 		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 3000);
 		HttpResponse response=null;
 	   	
 		String url = IP + "/registrations/create?name="+key;       
 		System.out.println(url);
 		
 		HttpGet httpget = new HttpGet(url);		   
 		httpget.setHeader("Accept", "application/json");
 		   
 		response = httpClient.execute(httpget);
    
 		if(response.getStatusLine().getStatusCode() == 200){
 			return 0;
 		}else{
 			return -1;
 		}
 		   
            
        
 	}
 	
 	
 	
 	public HouseInfo getHouseInfo(int id) throws ClientProtocolException, IOException, JSONException{
 		
 		final HttpClient httpClient =  new DefaultHttpClient();
 		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 3000);
 		HttpResponse response=null;
 	   	
 		String url = IP + "/properties/"+id;
 		System.out.println(url);
 		
 			HttpGet httpget = new HttpGet(url);		   
 			httpget.setHeader("Accept", "application/json");
 	   
 			response = httpClient.execute(httpget);
 			
 			
 			HouseInfo house = new HouseInfo();
 			
 			if(response.getStatusLine().getStatusCode() == 200){
 				
 				InputStream instream = response.getEntity().getContent();
 	            String tmp = read(instream);
 	            System.out.println(tmp);
 				
 	            JSONObject messageReceived = new JSONObject(tmp);
 	            
 	            house.setAddress(messageReceived.getString("address"));
 	            house.setWcs(messageReceived.getString("bathrooms"));
 	            house.setCity(messageReceived.getString("city"));
 	            house.setExtras(messageReceived.getString("extras"));
 	            house.setFor_sale(messageReceived.getBoolean("for_sale"));
 	            house.setId(messageReceived.getInt("id"));
 	            house.setKind(messageReceived.getString("kind"));
 	            
 	            String photo = IP+"/system/photos/"+messageReceived.getInt("id")
 	            				+"/original/"+messageReceived.getString("photo_file_name");
 	            
 	            house.setPhoto(photo);
 	            house.setPrice(messageReceived.getString("price"));
 	            house.setBedrooms(messageReceived.getString("rooms"));
 	            
 				return house;
 			}else
 				
 				return house;
 		
 			
 	}
 
 	
 	public void updateList(String date) throws ClientProtocolException, IOException, JSONException{
 		
 		final HttpClient httpClient =  new DefaultHttpClient();
 		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 3000);
 		HttpResponse response=null;
 		String url;
 				
 		if(date == null)
 			url = IP + "/properties/get_update";
 		else
 			url = IP + "/properties/get_update?date="+date;
 		
 		System.out.println(url);
 		
 		
 		HttpGet httpget = new HttpGet(url);		   
 		httpget.setHeader("Accept", "application/json");
 	   
 		response = httpClient.execute(httpget);
    
 		System.out.println(response.getStatusLine().getStatusCode());
 		if(response.getStatusLine().getStatusCode() == 200){
 			InputStream instream = response.getEntity().getContent();
             String tmp = read(instream);
         	
 	        JSONArray messageReceived = new JSONArray(tmp.toString());
         	System.out.println(messageReceived.toString());
         	
         	for(int i = 0; i < messageReceived.length()-1; i++){
         		JSONObject jo = messageReceived.getJSONObject(i);
         		
         		 String photo = IP+"/system/photos/"+jo.getInt("id")
          				+"/original/"+jo.getString("photo_file_name");
    		
         		HouseInfo h = new HouseInfo(jo.getInt("id"), 
         				jo.getString("kind"), 
         				jo.getString("address"), 
         				jo.getString("city"), 
         				jo.getString("rooms"), 
         				jo.getString("bathrooms"),
         				jo.getString("extras"),
         				photo,
         				jo.getBoolean("for_sale"),
         				jo.getString("price")); 
         		
        		if(hasFavourite(h.getId())){
         			updateFavourite(h);
         			available_updated_list.add(h);
         		}else{
         			available_new_list.add(h);
         		}
         		
         	}
         	
         	
         	updateVersion(messageReceived.getString(messageReceived.length()-1));
         	
 		}
 
 
 	}
 	
 	
 	private String read(InputStream in) throws IOException {
         StringBuilder sb = new StringBuilder();
         BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
         for (String line = r.readLine(); line != null; line = r.readLine()) {
             sb.append(line);
         }
         in.close();
         return sb.toString();
 	}
 	
 	
 	///////////////////////////////////////////////////////////////////////
 	//								NOTIFICACOES						//
 	///////////////////////////////////////////////////////////////////////
 	
 	
 	
 	public boolean updateNotificationPendingLists(String operation, int id, String kind, String city){
 		if(operation.equals("update") || operation.equals("destroy")){
 			if(hasFavourite(id)){
 				HouseInfo h = new HouseInfo(id,kind,city);
 				if(operation.equals("destroy")){
 					System.out.println("vai destruir-> " + id);
 					h.setFor_removal(true);
 				}
 				notifications_updated_list.add(h);
 				return true;
 			}else
 				return false;
 		}else if(operation.equals("new")){
 			HouseInfo h = new HouseInfo(id,kind,city);
 			notifications_new_list.add(h);
 			return true;
 		}
 		
 		return false;
 	}
 	
 	
 	
 	public void displayNotificationMessage(Context context) {
 		
 		//updateVersion();
 		
 		String message = "";
 		if(notifications_new_list.size() > 0){
 			message+= notifications_new_list.size()+ " New ";
 			
 			if (notifications_updated_list.size() == 0) 
 				message+= " Real Estates";
 		}
 		
 		if(notifications_updated_list.size() > 0){
 			if(notifications_new_list.size() > 0)
 				message+= "and " + notifications_updated_list.size()+ " Updated Real Estates";
 			else
 				message+= notifications_updated_list.size()+ " Updated Real Estates";
 		}
 		
   	    Notification notification = new Notification(R.drawable.notification_icon, message, System.currentTimeMillis());
   	    notification.flags = Notification.FLAG_AUTO_CANCEL;
   	    
   	    notification.number = count;
   	    count++;
   	    
   	    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, NotificationList.class), 0);
   	    notification.setLatestEventInfo(context, TAG, message, contentIntent);
   	    mNotificationManager.notify(0, notification);
   	  }
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////
 	//								CHAMADAS A DB						//
 	///////////////////////////////////////////////////////////////////////
 
 	
 	public void deleteFavourite(int id){
 		dbAdapter.open();
 		dbAdapter.removeFavourite(id);
 		dbAdapter.close();
 	}
 	
 	
 	public void inserFavourite(HouseInfo house){
 		dbAdapter.open();
 		
 		dbAdapter.createFavourite(house.getId(), house.getKind(), house.getAddress(), house.getCity(), 
 									house.getBedrooms(), house.getWcs(), house.getExtras(), 
 									house.getPhoto(), house.isFor_sale(), house.getPrice());
 		
 		dbAdapter.close();
 	}
 	
 	
 	public void updateFavourite(HouseInfo house){
 		dbAdapter.open();
 		
 		dbAdapter.updateFavourite(house);
 		
 		dbAdapter.close();
 	}
 	
 	public boolean hasFavourite(int id){
 		boolean ret;
 		
 		dbAdapter.open();
 		ret = dbAdapter.hasFavourite(id);
 		dbAdapter.close();
 		
 		return ret;
 		
 	}
 	
 	public HouseInfo getFavourite(int id){
 		dbAdapter.open();
 		
 		HouseInfo h= dbAdapter.getFavourite(id);
 		
 		dbAdapter.close();
 		
 		return h;
 	}
 	
 	
 	
 	public ArrayList<HouseInfo> getFavourites(){
 		
 		dbAdapter.open();
 		
 		ArrayList<HouseInfo> h = dbAdapter.getFavourites();
 		
 		dbAdapter.close();
 		
 		return h;
 	}
 	
 	public void updateVersion(String date){
 		dbAdapter.open();
 		dbAdapter.updateVersion(date);
 		dbAdapter.close();
 	}
 	
 	
 	public String getLastUpdateDate(){
 		
 		dbAdapter.open();
 		String ret = dbAdapter.getLastUpdateDate();
 		dbAdapter.close();
 		
 		return ret;
 	}
 	
 	
 	
 
 }
