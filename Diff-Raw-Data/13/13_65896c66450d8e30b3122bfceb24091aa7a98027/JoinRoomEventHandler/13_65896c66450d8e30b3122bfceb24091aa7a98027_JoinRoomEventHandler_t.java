 package net.dlogic.kryonet.server.event.handler;
 
import java.util.Iterator;

 import net.dlogic.kryonet.common.entity.Room;
import net.dlogic.kryonet.common.entity.User;
 import net.dlogic.kryonet.common.response.JoinRoomFailureResponse;
 import net.dlogic.kryonet.common.response.JoinRoomSuccessResponse;
 
 public abstract class JoinRoomEventHandler extends BaseEventHandler {
 	public abstract void onJoinRoom(Room roomToJoin, String password);
 	public void sendJoinRoomSuccessResponse(Room joinedRoom) {
 		JoinRoomSuccessResponse response = new JoinRoomSuccessResponse();
 		response.joinedRoom = joinedRoom;
		Iterator<User> it = joinedRoom.getUserList().iterator();
		while (it.hasNext()) {
			it.next().getConnection().sendTCP(response);
		}
 	}
 	public void sendJoinRoomFailureResponse(String errorMessage) {
 		JoinRoomFailureResponse response = new JoinRoomFailureResponse();
 		response.errorMessage = errorMessage;
		sender.getConnection().sendTCP(response);
 	}
 }
