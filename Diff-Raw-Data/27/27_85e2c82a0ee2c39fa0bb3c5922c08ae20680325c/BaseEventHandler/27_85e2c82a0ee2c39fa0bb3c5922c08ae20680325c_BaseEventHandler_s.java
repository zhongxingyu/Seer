 package net.dlogic.kryonet.server.event.handler;
 
 import java.util.Iterator;
 
 import net.dlogic.kryonet.common.entity.Room;
 import net.dlogic.kryonet.common.entity.User;
 import net.dlogic.kryonet.common.response.JoinRoomFailureResponse;
 import net.dlogic.kryonet.common.response.JoinRoomSuccessResponse;
 import net.dlogic.kryonet.common.response.LeaveRoomResponse;
 import net.dlogic.kryonet.common.response.LoginFailureResponse;
 import net.dlogic.kryonet.common.response.LoginSuccessResponse;
 import net.dlogic.kryonet.common.response.LogoutResponse;
 import net.dlogic.kryonet.common.response.PrivateMessageResponse;
 import net.dlogic.kryonet.common.response.PublicMessageResponse;
 
 import com.esotericsoftware.kryonet.Server;
 
 public abstract class BaseEventHandler {
 	public Server server;
 	public User sender;
 	public void sendJoinRoomSuccessResponse(User joinedUser, Room joinedRoom) {
 		Iterator<User> it = joinedRoom.userList.iterator();
 		while (it.hasNext()) {
 			JoinRoomSuccessResponse response = new JoinRoomSuccessResponse();
 			response.joinedUser = joinedUser;
 			response.joinedRoom = joinedRoom;
			server.getConnections()[it.next().id].sendTCP(response);
 		}
 	}
 	public void sendJoinRoomFailureResponse(String errorMessage) {
 		JoinRoomFailureResponse response = new JoinRoomFailureResponse();
 		response.errorMessage = errorMessage;
		server.getConnections()[sender.id].sendTCP(response);
 	}
 	public void sendLeaveRoomResponse(User userToLeave, Room roomToLeave) {
 		Iterator<User> it = roomToLeave.userList.iterator();
 		while (it.hasNext()) {
 			LeaveRoomResponse response = new LeaveRoomResponse();
 			response.userToLeave = userToLeave;
 			response.roomToLeave = roomToLeave;
			server.getConnections()[it.next().id].sendTCP(response);
 		}
 	}
 	public final void sendLoginSuccessResponse() {
 		LoginSuccessResponse response = new LoginSuccessResponse();
 		response.myself = sender;
		server.getConnections()[sender.id].sendTCP(response);
 	}
 	public final void sendLoginFailureResponse(String errorMessage) {
 		LoginFailureResponse response = new LoginFailureResponse();
 		response.errorMessage = errorMessage;
		server.getConnections()[sender.id].sendTCP(response);
 	}
 	public void sendLogoutResponse() {
 		LogoutResponse response = new LogoutResponse();
		server.getConnections()[sender.id].sendTCP(response);
 	}
 	public void sendPrivateMessageResponse(User targetUser, String message) {
 		PrivateMessageResponse response = new PrivateMessageResponse();
 		response.sender = sender;
 		response.message = message;
		server.getConnections()[targetUser.id].sendTCP(response);
 	}
 	public void sendPublicMessageResponse(String message, User sender, Room targetRoom) {
 		Iterator<User> it = targetRoom.userList.iterator();
 		while (it.hasNext()) {
 			PublicMessageResponse response = new PublicMessageResponse();
 			response.sender = sender;
 			response.message = message;
			server.getConnections()[it.next().id].sendTCP(response);
 		}
 	}
 }
