 package net.dlogic.kryonet.server.event.handler;
 
 import java.util.Iterator;
 
 import net.dlogic.kryonet.common.entity.Room;
 import net.dlogic.kryonet.common.entity.User;
 import net.dlogic.kryonet.common.manager.RoomManagerInstance;
 import net.dlogic.kryonet.common.response.ErrorResponse;
 import net.dlogic.kryonet.common.response.GetRoomsResponse;
 import net.dlogic.kryonet.common.response.JoinRoomFailureResponse;
 import net.dlogic.kryonet.common.response.JoinRoomSuccessResponse;
 import net.dlogic.kryonet.common.response.LeaveRoomResponse;
 import net.dlogic.kryonet.common.response.LoginFailureResponse;
 import net.dlogic.kryonet.common.response.LoginSuccessResponse;
 import net.dlogic.kryonet.common.response.LogoutResponse;
 import net.dlogic.kryonet.common.response.PrivateMessageResponse;
 import net.dlogic.kryonet.common.response.PublicMessageResponse;
 import net.dlogic.kryonet.server.KryonetServerException;
 import net.dlogic.kryonet.server.KryonetServerInstance;
 
 import com.esotericsoftware.kryonet.Server;
 import com.esotericsoftware.minlog.Log;
 
 public abstract class BaseEventHandler {
 	public Server endpoint;
 	public User sender;
 	public BaseEventHandler() {
 		try {
 			endpoint = KryonetServerInstance.getInstance().endpoint;
 		} catch (KryonetServerException e) {
 			e.printStackTrace();
 		}
 	}
 	public void sendErrorResponse(String errorMessage) {
 		ErrorResponse response = new ErrorResponse();
 		response.errorMessage = errorMessage;
 		endpoint.sendToTCP(sender.id, response);
 	}
 	public void sendGetRoomsResponse(Room[] rooms) {
 		GetRoomsResponse response = new GetRoomsResponse();
 		response.rooms = rooms;
 		endpoint.sendToTCP(sender.id, response);
 	}
 	public void sendJoinRoomSuccessResponse(User joinedUser, Room joinedRoom) {
		Iterator<User> it = joinedRoom.users.values().iterator();
 		while (it.hasNext()) {
 			JoinRoomSuccessResponse response = new JoinRoomSuccessResponse();
 			response.joinedUser = joinedUser;
 			response.joinedRoom = joinedRoom;
 			endpoint.sendToTCP(it.next().id, response);
 		}
 	}
 	public void sendJoinRoomFailureResponse(String errorMessage) {
 		JoinRoomFailureResponse response = new JoinRoomFailureResponse();
 		response.errorMessage = errorMessage;
 		endpoint.sendToTCP(sender.id, response);
 	}
 	public void sendLeaveRoomResponse() {
 		Iterator<Room> it = RoomManagerInstance.manager.map.values().iterator();
 		while (it.hasNext()) {
 			Room room = it.next();
			if (room.users.containsKey(sender.id)) {
 				sendLeaveRoomResponse(room);
 			}
 		}
 	}
 	public void sendLeaveRoomResponse(Room roomToLeave) {
		Iterator<User> it = roomToLeave.users.values().iterator();
 		while (it.hasNext()) {
 			LeaveRoomResponse response = new LeaveRoomResponse();
 			response.userToLeave = sender;
 			response.roomToLeave = roomToLeave;
 			endpoint.sendToTCP(it.next().id, response);
 		}
 	}
 	public final void sendLoginSuccessResponse() {
 		Log.info("BaseEventHandler.sendLoginSuccessResponse()");
 		LoginSuccessResponse response = new LoginSuccessResponse();
 		response.myself = sender;
 		endpoint.sendToTCP(sender.id, response);
 	}
 	public final void sendLoginFailureResponse(String errorMessage) {
 		LoginFailureResponse response = new LoginFailureResponse();
 		response.errorMessage = errorMessage;
 		endpoint.sendToTCP(sender.id, response);
 	}
 	public void sendLogoutResponse() {
 		LogoutResponse response = new LogoutResponse();
 		endpoint.sendToTCP(sender.id, response);
 	}
 	public void sendPrivateMessageResponse(User targetUser, String message) {
 		PrivateMessageResponse response = new PrivateMessageResponse();
 		response.sender = sender;
 		response.message = message;
 		endpoint.sendToTCP(targetUser.id, response);
 	}
 	public void sendPublicMessageResponse(Room targetRoom, String message) {
		Iterator<User> it = targetRoom.users.values().iterator();
 		while (it.hasNext()) {
 			PublicMessageResponse response = new PublicMessageResponse();
 			response.sender = sender;
 			response.message = message;
 			User user = it.next();
 			if (sender.id != user.id) {
 				endpoint.sendToTCP(user.id, response);
 			}
 		}
 	}
 }
