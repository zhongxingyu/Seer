 package server;
 
 import interfaces.Message;
 import interfaces.MessageType;
 import interfaces.RoomPOA;
 import interfaces.User;
 import interfaces.UserMood;
 import interfaces.UserSex;
 import interfaces.UserSize;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class RoomImpl extends RoomPOA {
 
 	private String name;
 	private List<User> users = new ArrayList<User>();
	private int x;
	private int y;
 
 	public RoomImpl(String name, int x, int y) {
 		this.name = name;
		this.x = x;
		this.y = y;
 	}
 
 	@Override
 	public User[] users() {
 		return (User[]) users.toArray();
 	}
 
 	@Override
 	public void sendMessage(Message message) {
 		if (message.type.equals(MessageType._BROADCAST)) {
 			for (User user : this.users()) {
 				System.out.println("Broadcast Message not sended");
 			}
 		} else {
 			System.out.println("Singlecast Message not sended");
 		}
 
 	}
 
 	@Override
 	public User changePassword(User user, String password) {
 		// TODO Auto-generated method stub
 		return user;
 	}
 
 	@Override
 	public User changeSize(User user, UserSize size) {
 		user.size = size;
 		return user;
 	}
 
 	@Override
 	public User changeMood(User user, UserMood mood) {
 		user.mood = mood;
 		return user;
 	}
 
 	@Override
 	public User changeSex(User user, UserSex sex) {
 		user.sex = sex;
 		return user;
 	}
 
 	@Override
 	public String name() {
 		return name;
 	}
 
	public int getX() {
 		return x;
 	}
 
	public int getY() {
 		return y;
 	}
 }
