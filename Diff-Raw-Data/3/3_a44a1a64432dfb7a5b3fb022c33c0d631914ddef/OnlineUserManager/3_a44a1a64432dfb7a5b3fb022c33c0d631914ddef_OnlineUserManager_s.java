 /**
  * 
  */
 package com.zero.chichi.mem.user;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.jboss.netty.channel.Channel;
 
 import com.zero.chichi.storage.dao.user.UserDAO;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 /**
  * 在线玩家的管理器
  * @author zero
  */
 public enum OnlineUserManager
 {
 	INSTANCE;
 	
 	OnlineUserManager()
 	{
 		mem = new ConcurrentHashMap<String, User>();
 	}
 	
 	/**
 	 * 玩家池
 	 */
 	Map<String, User> mem;
 	
 	public User getUser(String userId)
 	{
 		checkNotNull(userId);
 		return mem.get(userId);
 	}
 	
 	public void addUser(User user)
 	{
 		checkNotNull(user);
 		mem.put(user.getUserId(), user);
 	}
 	
 	public void mapChannel2User(User user, Channel channel)
 	{
 		user.setChannel(channel);
 		channel.setAttachment(user.getUserId());
 	}
 	
 	public void disconnect(String userId)
 	{
 		if (null == userId)
 			return;
 		User user = mem.remove(userId);
		UserDAO.updateUser(user);
 	}
 }
