 package com.nexus.client;
 
 import java.net.URLDecoder;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import com.google.gson.Gson;
 import com.nexus.MySQLHelper;
 import com.nexus.NexusServer;
 import com.nexus.Packet;
 import com.nexus.client.pushnotification.PushNotificationButton;
 import com.nexus.client.pushnotification.PushNotificationButtonManager;
 import com.nexus.users.User;
 import com.nexus.webserver.WebServerStatus;
 
 public abstract class NexusClient {
 	
 	private int ClientID = -1;
 	private String ClientName = "";
 	
 	public User AuthenticatedUser;
 	
 	public String Token = "";
 	
 	public ClientSendQueue SendQueue;
 	
 	public boolean HasTimer = false;
 	public NexusTimer Timer;
 
 	public boolean RedirectNotifyMessages = false;
 	public NexusClient RedirectedNotifyMessagesDestination;
 	public boolean RedirectAllPackages = false;
 	public NexusClient RedirectedPacketsDestination;
 	
 	private PushNotificationButtonManager PushNotificationButtonManager;
 	
 	protected NexusServer Server = NexusServer.Instance;
 	
 	public NexusClient(){}
 	
 	public void Init(String ClientName, String Token){
 		this.ClientName = ClientName;
 		this.SendQueue = new ClientSendQueue(EnumProtocolType.FETCH, this);
 		this.Token = Token;
 		this.AuthenticatedUser = new User().FromToken(Token);
 		this.PushNotificationButtonManager = NexusServer.Instance.PushNotificationButtonManager;
 		Logger.getLogger("ClientManager").fine("New client with type '" + this.GetClientTypeName() + "' and name '" + this.GetName() + "' connected with id " + this.GetClientID());
 		this.OnConnect();
 	}
 
 	public void SetClientID(int ID) throws IllegalAccessException{
 		if(this.ClientID != -1){
 			throw new IllegalAccessException();
 		}
 		this.ClientID = ID;
 	}
 	
 	public int GetClientID(){
 		return ClientID;
 	}
 	
 	public String GetName(){
 		return ClientName;
 	}
 	
 	public void SetName(String s){
 		ClientName = s;
 	}
 	
 	public abstract int GetClientTypeID();
 	
 	public abstract String GetClientTypeName();
 	
 	public void OnConnect(){
 		/*final NexusClient c = this;
 		Server.Scheduler.ScheduleTask(new IScheduled(){
 			public void run() {
 				if(c.HasTimer){
 					c.Timer.StartTimerCountdown(10, 0, 0);
 					c.Timer.StartLive();
 					c.SendPushNotification("Uitzending gestart!", "Je mag nu dus uitzenden ofzo", "");
 				}
 			}
 		}, new DateTime(2012,DateTime.NOVEMBER,25,9,50,0));*/
 		//TESTING
 	}
 	
 	public void SendRedirectedNotifyMessage(Packet data){
 		try{
 			if(RedirectNotifyMessages){
 				RedirectedNotifyMessagesDestination.SendRedirectedNotifyMessage(data);
 			}else{
 				this.OnDataReceived(data);
 			}
 		}catch(Exception e){}
 	}
 	
 	public void SendNotifyMessage(User Sender, String Message, boolean SentToUser){
 		Packet data = new Packet();
 		data.DestinationClient = this;
 		data.IsNotify = true;
 		data.NotifySender = Sender;
 		data.NotifyMessage = Message;
 		try{
 			if(RedirectNotifyMessages && !SentToUser){
 				RedirectedNotifyMessagesDestination.SendRedirectedNotifyMessage(data);
 			}else{
 				this.OnDataReceived(data);
 			}
 		}catch(Exception e){}
 	}
 	
 	public void SendPushNotification(String Title, String Description, String ImageURL){
 		if(RedirectAllPackages){
 			RedirectedPacketsDestination.SendPushNotification(Title, Description, ImageURL);
 		}else{
 			this.SendQueue.addToSendQueue("{\"PushNotification\":{\"Title\":\"" + Title + "\",\"Description\":\"" + Description + "\",\"ImageURL\":\"" + ImageURL + "\"}}");
 		}
 	}
 	
 	public void SendPushNotification(String Title, String Description, String ImageURL, ArrayList<PushNotificationButton> Buttons){
 		if(RedirectAllPackages){
 			RedirectedPacketsDestination.SendPushNotification(Title, Description, ImageURL, Buttons);
 		}else{
 			String Output = "[";
 			for(PushNotificationButton Element : Buttons){
 				Output += Element.toJson() + ",";
 			}
 			Output = Output.substring(0, Output.length() - 1) + "]";
 			this.SendQueue.addToSendQueue("{\"PushNotification\":{\"Title\":\"" + Title + "\",\"Description\":\"" + Description + "\",\"ImageURL\":\"" + ImageURL + "\", \"Buttons\":" + Output + "}}");
 		}
 	}
 	
 	public void OnDataReceived(Packet Package) throws Exception{
 		if(Package.IsNotify){
 			HashMap<String, Object> map = new HashMap<String, Object>();
 			HashMap<String, Object> notifymap = new HashMap<String, Object>();
 			notifymap.put("Sender",Package.NotifySender.toString());
 			notifymap.put("Message", Package.NotifyMessage);
 			map.put("Notify", notifymap);
 			this.SendQueue.addToSendQueue(new Gson().toJson(map));
 		}else if(Package.Data.split("/")[2].equalsIgnoreCase("setProtocol")){
 			if(!Package.Internal){
 				Package.Response.SendHeaders(WebServerStatus.Forbidden);
 				Package.Response.SendError("Cannot change protocol of other clients");
 				Package.Response.Close();
 				Logger.getLogger("ClientManager").warning("Client with id " + Package.SenderClient.ClientID + " tried to change the protocol from client " + Package.DestinationClient.ClientID + " to " + Package.Data.split("/")[3].toUpperCase().trim());
 				return;
 			}
 			String q = Package.Request.GetParameter("type");
 			if(q.equalsIgnoreCase("TCP")){
 				Package.SenderClient.SendQueue.Protocol = EnumProtocolType.TCP;
 			}else if(q.equalsIgnoreCase("UDP")){
 				Package.SenderClient.SendQueue.Protocol = EnumProtocolType.UDP;
 			}else if(q.equalsIgnoreCase("HTTP")){
 				Package.SenderClient.SendQueue.Protocol = EnumProtocolType.HTTP;
 				Package.SenderClient.SendQueue.HTTPPath = "http://" + Package.Request.Address + ":" + Package.Data.split("/")[4].toLowerCase() + "/";
 			}else if(q.equalsIgnoreCase("WEBSOCKET")){
 				Package.SenderClient.SendQueue.PrepareWebsocketCommunication();
 			}else if(q.equalsIgnoreCase("FETCH")){
 				Package.SenderClient.SendQueue.Protocol = EnumProtocolType.FETCH;
 			}
 			Logger.getLogger("ClientManager").info("Client with id " + Package.SenderClient.GetClientID() + " changed his protocol type to " + Package.SenderClient.SendQueue.Protocol);
 			
 			Package.Response.SendHeaders(WebServerStatus.OK);
 			Package.Response.SendError("none");
 			Package.Response.Close();
 		}else if(Package.Data.split("/")[2].equalsIgnoreCase("FetchData")){
 			if(!Package.Internal){
 				Package.Response.SendHeaders(WebServerStatus.Forbidden);
 				Package.Response.SendError("You cannot fetch data from other users!");
 				Package.Response.Close();
 				Logger.getLogger("ClientManager").warning("Client with id " + Package.SenderClient.ClientID + " tried to fetch data from another user");
 				return;
 			}
 			String s = Package.DestinationClient.SendQueue.GetFetchData();
 			Package.Response.SetHeader("Content-Type", "application/json");
 			Package.Response.SendHeaders(WebServerStatus.OK);
 			Package.Response.SendData(s);
 			Package.Response.Close();
 		}else if(Package.Data.split("/")[2].equalsIgnoreCase("UserInfo")){
 			Package.Response.SendHeaders(WebServerStatus.OK);
 			Package.Response.SendData(AuthenticatedUser.toString());
 			Package.Response.Close();
 		}else if(Package.Data.split("/")[2].equalsIgnoreCase("ListClients")){
 			if(Package.Request.Parameters.containsKey("id")){
 				int TypeID = Integer.parseInt(Package.Request.Parameters.get("id"));
 				HashMap<String, Object> InfoMap = new HashMap<String, Object>();
 				ArrayList<Object> ClientsMap = new ArrayList<Object>();
 				InfoMap.put("error", "none");
 				
 				for(NexusClient client : Server.ClientManager.NexusClients){
 					if(client.GetClientTypeID() == TypeID){
 						HashMap<String, Object> map = new HashMap<String, Object>();
 						HashMap<String, Object> typemap = new HashMap<String, Object>();
 						
 						typemap.put("ID", client.GetClientTypeID());
 						typemap.put("Name", client.GetClientTypeName());
 						
 						map.put("ClientID", client.GetClientID());
 						map.put("ClientName", client.GetName());
 						map.put("Type", typemap);
 						map.put("User", client.AuthenticatedUser.toString());
 						
 						ClientsMap.add(map);
 					}
 				}
 				InfoMap.put("NexusClients", ClientsMap);
 				Package.Response.SendHeaders(WebServerStatus.OK);
 				Package.Response.SendData(InfoMap);
 				Package.Response.Close();
 			}else{
 				HashMap<String, Object> InfoMap = new HashMap<String, Object>();
 				ArrayList<Object> ClientsMap = new ArrayList<Object>();
 				InfoMap.put("error", "none");
 				
 				for(NexusClient client : Server.ClientManager.NexusClients){
 					HashMap<String, Object> map = new HashMap<String, Object>();
 					HashMap<String, Object> typemap = new HashMap<String, Object>();
 					
 					typemap.put("ID", client.GetClientTypeID());
 					typemap.put("Name", client.GetClientTypeName());
 					
 					map.put("ClientID", client.GetClientID());
 					map.put("ClientName", client.GetName());
 					map.put("Type", typemap);
 					map.put("User", client.AuthenticatedUser.toString());
 					
 					ClientsMap.add(map);
 				}
 				InfoMap.put("NexusClients", ClientsMap);
 				Package.Response.SendHeaders(WebServerStatus.OK);
 				Package.Response.SendData(InfoMap);
 				Package.Response.Close();
 			}
 		}else if(Package.Data.split("/")[2].equalsIgnoreCase("ListUsers")){
 			try{
 				Connection conn = MySQLHelper.GetConnection();
 				Statement stmt = conn.createStatement();
 				ResultSet rs = stmt.executeQuery("SELECT * FROM users");
 
 				HashMap<String, Object> InfoMap = new HashMap<String, Object>();
 				ArrayList<Object> UsersList = new ArrayList<Object>();
 				
 				while(!rs.isLast()){
 					rs.next();
 					UsersList.add(new User().FromUsername(rs.getString("Username")).toJsonMap());
 				}
 				InfoMap.put("Users", UsersList);
 				Package.Response.SendHeaders(WebServerStatus.OK);
 				Package.Response.SendData(InfoMap);
 				Package.Response.Close();
 				
 				rs.close();
 				stmt.close();
 				conn.close();
 			}catch(Exception e){}
 		}else if(Package.Data.split("/")[2].equalsIgnoreCase("SendNotify")){
 			User Sender = new User().FromToken(Package.Request.GetParameter("token"));
 			if(Package.Internal){
 				//Send to specific user
 				User Destination = new User().FromUsername(URLDecoder.decode(Package.Request.GetParameter("toUser"),"UTF-8"));
 				Destination.SendNotifyMessage(Sender, URLDecoder.decode(Package.Request.GetParameter("message"),"UTF-8"));
 				Package.Response.SendHeaders(WebServerStatus.OK);
 				Package.Response.SendError("none");
 				Package.Response.Close();
 			}else{
 				//Send to this client
 				this.SendNotifyMessage(Sender, URLDecoder.decode(Package.Request.GetParameter("message"),"UTF-8"), false);
 				Package.Response.SendHeaders(WebServerStatus.OK);
 				Package.Response.SendError("none");
 				Package.Response.Close();
 			}
 		}else if(Package.Data.split("/")[2].equalsIgnoreCase("RedirectNotify")){
 			if(Package.Internal){
 				int DestID = Integer.parseInt(Package.Request.GetParameter("id"));
 				if(DestID == this.GetClientID()){
 					Package.Response.SendHeaders(WebServerStatus.Forbidden);
 					Package.Response.SendError("Cannot redirect data to yourself, turned off redirecting");
 					Package.Response.Close();
 					this.RedirectNotifyMessages = false;
 				}else{
 					this.RedirectNotifyMessages = true;
 					this.RedirectedNotifyMessagesDestination = Server.ClientManager.GetClientByID(DestID);
 					Package.Response.SendHeaders(WebServerStatus.OK);
 					Package.Response.SendError("none");
 					Package.Response.Close();
 				}
 			}else{
 				Package.Response.SendHeaders(WebServerStatus.Forbidden);
 				Package.Response.SendError("Cannot redirect notify messages for other clients");
 				Package.Response.Close();
 			}
 		}else if(Package.Data.split("/")[2].equalsIgnoreCase("RedirectData")){
 			if(Package.Internal){
 				int DestID = Integer.parseInt(Package.Request.GetParameter("id"));
 				if(DestID == this.GetClientID()){
 					Package.Response.SendHeaders(WebServerStatus.Forbidden);
 					Package.Response.SendError("Cannot redirect data to yourself, turned off redirecting");
 					Package.Response.Close();
 					this.RedirectAllPackages = false;
 				}else{
 					this.RedirectAllPackages = true;
 					this.RedirectedPacketsDestination = Server.ClientManager.GetClientByID(DestID);
 					Package.Response.SendHeaders(WebServerStatus.OK);
 					Package.Response.SendError("none");
 					Package.Response.Close();
 				}
 			}else{
 				Package.Response.SendHeaders(WebServerStatus.Forbidden);
 				Package.Response.SendError("Cannot redirect data for other clients");
 				Package.Response.Close();
 			}
 		}else if(Package.Data.split("/")[2].equalsIgnoreCase("AddTimer")){
 			this.HasTimer = true;
 			this.Timer = new NexusTimer(this);
 			Package.Response.SendHeaders(WebServerStatus.OK);
 			Package.Response.SendError("none");
 			Package.Response.Close();
 		}else if(Package.Data.split("/")[2].equalsIgnoreCase("PNButtonPress")){
 			int ButtonID = Integer.parseInt(Package.Request.GetParameter("id"));
 			this.PushNotificationButtonManager.OnButtonPress(ButtonID);
 			Package.Response.SendHeaders(WebServerStatus.OK);
 			Package.Response.SendError("none");
 			Package.Response.Close();
 		}
		Package.Response.Close();
 	}
 	
 	public void OnTick(){
 		
 	}
 	
 }
