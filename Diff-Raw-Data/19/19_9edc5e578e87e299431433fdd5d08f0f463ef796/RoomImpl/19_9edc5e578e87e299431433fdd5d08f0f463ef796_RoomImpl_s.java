 package server;
 
 import interfaces.Message;
 import interfaces.MessageType;
 import interfaces.RoomPOA;
 import interfaces.User;
 import interfaces.UserMood;
 import interfaces.UserService;
 import interfaces.UserSex;
 import interfaces.UserSize;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import dao.MessageDao;
 import dao.UserDao;
 
 public class RoomImpl extends RoomPOA {
 
 	private String name;
 	private Map<String, UserService> users = new HashMap<String, UserService>();
 	private int x;
 	private int y;
 
 	public RoomImpl(String name, int x, int y) {
 		this.name = name;
 		this.x = x;
 		this.y = y;
 	}
 
 	@Override
 	public void sendMessage(Message message) {
 		if (message.type.equals(MessageType.BROADCAST)) {
 			for (String key : this.users().keySet()) {
 				if (!key.equals(message.sender))
 					this.users().get(key).notifyMessage(message);
 			}
 		} else {
 			if (!message.receiver.equals(message.sender)) {
 				UserService userService = this.users().get(message.receiver);
 				if (userService != null) {
 					userService.notifyMessage(message);
 				} else {
 					MessageDao.saveMessage(message);
 				}
 			}
 		}
 
 	}
 
 	@Override
 	public void changePassword(User user, String password) {
 		UserDao.setPassword(user, password);
 	}
 
 	@Override
 	public User changeSize(User user, UserSize size) {
 		user.size = size;
 		UserDao.setSize(user, size);
 		for (String key : this.users().keySet()) {
 			if (!key.equals(user.login))
 				this.users().get(key).notifyChangeSize(user, size);
 		}
 		return user;
 	}
 
 	@Override
 	public User changeMood(User user, UserMood mood) {
 		user.mood = mood;
 		UserDao.setMood(user, mood);
 		for (String key : this.users().keySet()) {
 			if (!key.equals(user.login))
 				this.users().get(key).notifyChangeMood(user, mood);
 		}
 		return user;
 	}
 
 	@Override
 	public User changeSex(User user, UserSex sex) {
 		user.sex = sex;
 		UserDao.setSex(user, sex);
 		for (String key : this.users().keySet()) {
 			if (!key.equals(user.login))
 				this.users().get(key).notifyChangeSex(user, sex);
 		}
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
 
 	public void login(User user, UserService newUserService) {
 		this.users.put(user.login, newUserService);
 		for (String key : this.users().keySet()) {
 			if (!key.equals(user.login)) {
 				this.users().get(key).notifyConnection(user);
 			}
 		}
 	}
 
 	public void logout(User user) {
 		this.users.remove(user.login);
 		for (String key : this.users().keySet()) {
 			this.users().get(key).notifyLogout(user);
 		}
		System.out.println(user.login + " logout");
 	}
 
 	public Map<String, UserService> users() {
 		return users;
 	}
 
 	@Override
 	public String[] loginList() {
 		Set<String> loginList = this.users().keySet();
 		String[] loginListToReturn = new String[loginList.size()];
 		int i = 0;
 		for (String login : loginList) {
 			loginListToReturn[i] = login;
 			i++;
 		}
 		return loginListToReturn;
 	}
 
 }
