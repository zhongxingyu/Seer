 package org.gemsjax.server.module;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.servlet.http.HttpSession;
 
 import org.gemsjax.server.communication.channel.handler.LogoutChannelHandler;
 import org.gemsjax.server.communication.servlet.LiveWebSocketConnection;
 import org.gemsjax.server.persistence.dao.UserDAO;
 import org.gemsjax.server.persistence.dao.exception.NotFoundException;
 import org.gemsjax.server.persistence.dao.hibernate.HibernateUserDAO;
 import org.gemsjax.shared.communication.CommunicationConnection;
 import org.gemsjax.shared.communication.CommunicationConnection.ClosedListener;
 import org.gemsjax.shared.communication.channel.OutputChannel;
 import org.gemsjax.shared.communication.message.system.LogoutMessage;
 import org.gemsjax.shared.communication.message.system.LogoutMessage.LogoutReason;
 import org.gemsjax.shared.user.User;
 
 
 /**
  * This singleton class handles all {@link User} that are actually online.
  * That means, that every {@link User} that is online authenticated and has a {@link OnlineUser} Wrapper-Class that wraps
  * and maps a {@link User} and his corresponding {@link OutputChannel}.
  * <br />
  * <b>NOTE:</b> Since this is a singleton use the method {@link #getInstance()} to access this singleton instance
  * @author Hannes Dorfmann
  *
  */
 public class OnlineUserManager implements LogoutChannelHandler, ClosedListener {
 
 	/**
 	 * The singleton instance
 	 */
 	private static OnlineUserManager INSTANCE = new OnlineUserManager();
 	
 	/**
 	 * This Map maps the {@link User} id ({@link User#getId()}) to his {@link OnlineUser} Wrapper, which contains the {@link OutputChannel}
 	 */
 	private Map<Integer, OnlineUser> onlineUserIdMap;
 	
 	private Map<CommunicationConnection, OnlineUser> connectionOnlineUserMap;
 	
 	/**
 	 * This Map maps a HttpSession id ({@link HttpSession#getId()}) to his {@link OnlineUser}  
 	 */
 	private Map<String, OnlineUser > onlineUserSessionMap;
 	
 	private UserDAO dao ;
 	
 	/**
 	 * The private constructor, used to create the singleton instance
 	 */
 	private OnlineUserManager()
 	{
 		onlineUserIdMap = new ConcurrentHashMap<Integer, OnlineUser>();
 		onlineUserSessionMap = new ConcurrentHashMap<String, OnlineUser>();
 		connectionOnlineUserMap = new ConcurrentHashMap<CommunicationConnection, OnlineUser>();
 		dao = new HibernateUserDAO();
 	}
 	
 	
 
 	public static OnlineUserManager getInstance()
 	{
 		return INSTANCE;
 	}
 	
 	
 	/**
 	 * Get the outputChannel for a given User or <code>null</code>  if the user is not Online
 	 * @param u
 	 * @return the {@link OutputChannel} or <code>null</code>  if the user is not Online
 	 */
 	public OutputChannel getOutputChannel(User u)
 	{
 		OnlineUser o = onlineUserIdMap.get(u.getId());
 		if (o == null) 
 			return null;
 	
 		return o.getStandardOutputChannel();
 	}
 	
 	
 	/**
 	 * Get the {@link OnlineUser} object
 	 * @param u
 	 * @return The {@link OnlineUser} object or <code>null</code> if the user is not online
 	 */
 	public OnlineUser getOnlineUser( User u)
 	{
 		return onlineUserIdMap.get(u.getId());
 	}
 	
 	/**
 	 * Get the OnlineUser by its unique User id
 	 * @param id
 	 * @return the {@link OnlineUser} or null (if not online now)
 	 */
 	public OnlineUser getOnlineUser(int id)
 	{
 		return onlineUserIdMap.get(id);
 	}
 	
 	/**
 	 * Get a {@link OnlineUser} by his {@link HttpSession}
 	 * @param httpSession
 	 * @return
 	 */
 	public OnlineUser getOnlineUser(HttpSession httpSession)
 	{
 		return onlineUserSessionMap.get(httpSession.getId());
 	}
 	
 	
 	public void addOnlineUser(OnlineUser user)
 	{
 		
 		
 		OnlineUser existingOnlineUser = onlineUserIdMap.get(user.getId());
 		
 		if (existingOnlineUser != null)
 		{
 			
 			// The User is already logged in with another connection, so logout and close the old connection
 			// and use the new connection
 			try {
 				existingOnlineUser.getLogoutChannel().send(new LogoutMessage(LogoutReason.SERVER_OTHER_CONNECTION));
 			} catch (IOException e) {
 				// TODO What to do if could not sent logout message to the client
 				e.printStackTrace();
 			}
 			finally
 			{
 				doLogout(existingOnlineUser);
 				connectionOnlineUserMap.remove(existingOnlineUser.getCollaborationChannel().getConnection());
 			}
 			
 		}
 		
 		
 		onlineUserIdMap.put(user.getId(), user);
 		onlineUserSessionMap.put(user.getHttpSession().getId(), user);
 		connectionOnlineUserMap.put(user.getCollaborationChannel().getConnection(), user);
 		
 	}
 
 
 	private void doLogout(OnlineUser ou)
 	{
 		onlineUserIdMap.remove(ou.getId());
 		onlineUserSessionMap.remove(ou.getHttpSession().getId());
 		// TODO, check if needed?
 		//ou.getHttpSession().invalidate();
 		
 		// unbind Channel Handlers
		ou.getLogoutChannel().removeLogoutChannelHandler(this);
		ou.getFriendChannel().removeFriendsChanneslHandler(FriendModule.getInstance());
 		
 		CollaborationModule.getInstance().unSubscribeAllOf(ou);
 		
 		
 	}
 	
 
 	@Override
 	public void onLogoutReceived(OnlineUser user) {
 		doLogout(user);
 		
 	}
 
 
 
 	@Override
 	public void onClose(CommunicationConnection connection) {
 		if (connection instanceof LiveWebSocketConnection){
 			OnlineUser ou = connectionOnlineUserMap.get(connection);
 			
 			if (ou != null)
 			{
 				doLogout(ou);
 			}
 			
 			CollaborationModule.getInstance().unSubscribeAllOf(ou);
 			
 		}
 		
 	}
 	
 	
 	public User getOrLoadUser(int userId) throws NotFoundException{
 		OnlineUser ou = getOnlineUser(userId);
 		
 		if (ou != null)
 			return ou.getUser();
 		else
 			return dao.getUserById(userId);
 			
 	}
 	
 	
 	
 }
