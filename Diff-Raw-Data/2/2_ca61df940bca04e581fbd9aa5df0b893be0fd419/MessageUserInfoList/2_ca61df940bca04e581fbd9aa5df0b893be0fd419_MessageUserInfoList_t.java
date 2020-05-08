 package btwmod.centralchat.message;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.java_websocket.WebSocket;
 
 import btwmod.centralchat.IGateway;
 import btwmod.centralchat.IServer;
 import btwmod.centralchat.ResourceConfig;
 import btwmods.ChatAPI;
 import btwmods.ModLoader;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 
 /**
  * A list of users connected to the server.
  * Only sent from server to client when the client first connects.
  * 
  * @author amekkawi
  */
 public class MessageUserInfoList extends Message {
 	
 	public static final String TYPE = "userlist";
 
 	@Override
 	public String getType() {
 		return TYPE;
 	}
 	
 	public final MessageUserInfo[] users;
 
 	public MessageUserInfoList(MessageUserInfo[] users) {
 		this.users = users;
 	}
 	
 	public MessageUserInfoList(JsonObject json) {
 		JsonArray usersArray = json.get("users").getAsJsonArray();
 		
 		users = new MessageUserInfo[usersArray.size()];
 		for (int i = 0, len = usersArray.size(); i < len; i++) {
			users[i] = new MessageUserInfo(usersArray.get(i).getAsJsonObject());
 		}
 	}
 	
 	@Override
 	public JsonObject toJson() {
 		JsonObject obj = super.toJson();
 		
 		JsonArray userArray = new JsonArray();
 		for (MessageUserInfo user : users) {
 			userArray.add(user.toJson());
 		}
 		obj.add("users", userArray);
 		
 		return obj;
 	}
 
 	@Override
 	public void handleAsServer(IServer server, WebSocket conn, ResourceConfig config) {
 		// Does not handle this kind of message.
 	}
 	
 	@Override
 	public void handleAsGateway(IGateway gateway) {
 		Set<String> uniqueUsers = new HashSet<String>();
 		
 		for (MessageUserInfo user : users) {
 			user.handleAsGateway(gateway);
 			uniqueUsers.add(user.username.toLowerCase());
 		}
 		
 		int len = uniqueUsers.size();
 		ModLoader.outputInfo("Connected to central chat server.");
 		ChatAPI.sendChatToAllPlayers("Connected to chat server" + (len > 0 ? " making " + len + " user" + (len == 1 ? "" : "s") + " available for chat" : "") + ".");
 		gateway.onSuccessfulConnect();
 	}
 }
