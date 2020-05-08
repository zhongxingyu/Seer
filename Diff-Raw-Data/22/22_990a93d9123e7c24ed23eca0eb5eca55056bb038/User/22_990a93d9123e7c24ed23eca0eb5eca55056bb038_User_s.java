 package server.components;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import org.json.*;
 
 public class User extends Component {
 
 	Socket		userSocket;
 	Component	parent;
 	DataInputStream		input;
 	DataOutputStream	output;
 	
 	public User(Socket userSocket) {
 		this.userSocket = userSocket;
 		try {
 			input = new DataInputStream(userSocket.getInputStream());
 			output = new DataOutputStream(userSocket.getOutputStream());
 		} catch (IOException e) {
 			log("swallowed ioexception\tUser constructor");
 		}
 		this.parent = null;
 	}
 	
 	@Override
 	public void runOnce() {
 		Object msg = receiveMessage(50L);
 		if ( msg == null ) {
 			handleNoMessage();
 		} else if ( msg instanceof ChatMessage ) {
 			handleChatMessage((ChatMessage)msg);
 		} else if ( msg instanceof ChatServer.JoinedServerMessage ) {
 			handleJoinServerMessage((ChatServer.JoinedServerMessage)msg);
 		}
 	}
 	
 	public void handleNoMessage() {
 		if ( parent != null ) {
 			try {
 				String s = input.readUTF();
 				JSONObject jsonMsg = new JSONObject(s);
 				if (jsonMsg.getString("type").equals("message")) {
 					parent.sendMessage(new ChatMessage(jsonMsg.getString("content")));
 				}
 			} catch ( IOException e ) {
 				parent.sendMessage(new UserDisconnectMessage(this));
 				signalError();
 			} catch ( JSONException e) {
 				logAndPost("swallowed jsonexception\tUser no message");
 			}
 		}
 	}
 	
 	public void handleChatMessage(ChatMessage msg) {
 		JSONObject out = new JSONObject();
 		try {
 			out.put("type", "message");
 			out.put("content", msg.contents);
 			output.writeUTF(out.toString());
 		} catch ( JSONException e ) {
 			logAndPost("swallowed JSONException in handleChatMessage");
 		} catch ( IOException e ) {
 			logAndPost("swallowed IOException in handleChatMessage");
 		}
 	}
 	
 	public void handleJoinServerMessage(ChatServer.JoinedServerMessage msg) {
 		parent = msg.server;
 	}
 	
 	public String toString() {
 		return "User#{ IP" + userSocket.getInetAddress().toString() + " }";
 	}
 	
 	public enum UserCommand {
 		JOIN_SERVER
 	}
 	
 	public class PostedCommand {
 		public final UserCommand cmd;
 		public final String[] args;
 		
 		private PostedCommand(UserCommand cmd, String[] args) {
 			this.cmd = cmd;
 			this.args = args;
 		}
 	}
 	
 	public class ChatMessage {
 		public final String contents;
 		
 		private ChatMessage(String contents) {
 			this.contents = contents;
 		}
 		
 		public String toString() {
 			return "ChatMessage#{ " + contents + " }";
 		}
 	}
 	
 	public class UserDisconnectMessage {
 		public final User user;
 		
 		private UserDisconnectMessage(User user) {
 			this.user = user;
 		}
 		
 		public String toString() {
 			return "UserDisconnectMessage#{ " + user.toString() + " }";
 		}
 	}
 
 }
