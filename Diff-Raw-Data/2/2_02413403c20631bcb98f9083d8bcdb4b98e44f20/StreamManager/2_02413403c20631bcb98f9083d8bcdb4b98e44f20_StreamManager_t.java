 package com.infrno.multiplayer;
 
 import com.wowza.wms.amf.AMFDataObj;
 
 public class StreamManager 
 {
 	private Application 	main_app;
 	private Boolean 		use_peer_connection;
 	
 	public StreamManager(Application app) 
 	{
 		main_app = app;
 	}
 	
 	public void checkStreamSupport()
 	{
 		use_peer_connection = true;
 		
 		AMFDataObj user_obj = main_app.userManager.users_obj;
 		for(Object i : user_obj.getKeys()){
 			AMFDataObj curr_user_info = main_app.userManager.getClientInfo(i.toString());
 			main_app.log(curr_user_info.getString("peer_connection_status"));
 			
			if(!curr_user_info.getString("peer_connection_status").equals("peer_netconnection_connected"))
 				use_peer_connection=false;
 			
 		}
 		
 		main_app.app_instance.broadcastMsg("usePeerConnection", use_peer_connection);
 		
 	}
 	
 }
