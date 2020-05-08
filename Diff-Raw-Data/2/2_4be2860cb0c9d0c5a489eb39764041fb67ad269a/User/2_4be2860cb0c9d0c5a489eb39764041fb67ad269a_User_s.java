 /*  Ahmet Aktay and Nathan Griffith
  *  DarkChat
  *  CS435: Final Project
  */
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List; 
 
 public class User {
 	public String name;
 	public HashMap<InetSocketAddress,Session> sessions;
 	public Boolean checked = false;
 	public UserList knownUsers;
 	public UserList globalUsers;
 	
 	public User()
 	{
 		this("unknown user");
 	}
 	public User(String userName)
 	{
 		this(userName, new HashMap<InetSocketAddress,Session>());
 	}
 	public User(String userName, HashMap<InetSocketAddress,Session> sessionList)
 	{
 		this.name = userName;
 		this.sessions = sessionList;
 		this.knownUsers = new UserList();
 	}
 	
 	// online offline
 	public Boolean isOnline()
 	{
 			return hasOnlineSession();
 	}
 	public Boolean hasOnlineSession()
 	{
 		Boolean online = false;
 		for (Session session : sessions.values()) {
 			online = online || session.online;
 		}
 		return online;
 	}
 	
 	// known users
 	public void meetUser(User user)
 	{
 		knownUsers.put(user);
 		user.knownUsers.put(this);
 		MyUtils.dPrintLine(String.format("%s met %s",this.name,user.name));
 	}
 	public Boolean knowUser(String name)
 	{
		return knownUsers.get(name, true) == null;
 	}
 	public Boolean knowUser(User user)
 	{
 		return knowUser(user.name);
 	}
 	
 	// session get & put
 	public void putSession(Session session)
 	{
 		this.sessions.put(session.address, session);
 	}
 	public void putSession(InetSocketAddress inetSocketAddress)
 	{
 		putSession(new Session(this, inetSocketAddress, true));
 	}
 	public void putSession(InetSocketAddress inetSocketAddress, Boolean online)
 	{
 		putSession(new Session(this, inetSocketAddress, new Date(), online));
 	}
 	public void putSession(InetSocketAddress inetSocketAddress, Boolean online, Date lastValid)
 	{
 		putSession(new Session(this, inetSocketAddress, lastValid, online));
 	}
 	public Session getSession(InetSocketAddress inetSocketAddress)
 	{
 		return sessions.get(inetSocketAddress);
 	}
 	
 	// session modify
 	public void updateSession(InetSocketAddress inetSocketAddress, Boolean online)
 	{
 		Session session = getSession(inetSocketAddress);
 		session.online = online;
 	}
 	public void updateSession(InetSocketAddress inetSocketAddress, Date lastValid)
 	{
 		Session session = getSession(inetSocketAddress);
 		session.lastValid = lastValid;
 	}
 	
 	// session pruning
 	public void pruneSessions(){
 		Date d = MyUtils.nowPlusSeconds(60 * 60 * 1);
 		pruneSessions(5,d,10);	
 	}
 	public void pruneSessions(int minToKeep, Date tresholdDate , int maxToKeep)
 	{
 		ArrayList<Session> sessionsList = new ArrayList<Session>(sessions.values());
 		Collections.sort(sessionsList);
 		int keptCounter = sessionsList.size();
 		MyUtils.dPrintLine(name);
 		for (int i = 0; i < sessionsList.size() - minToKeep; i++)
 		{
 			Session session = sessionsList.get(i);
 			MyUtils.dPrintLine("considering "+session.lastValid.toString());
 			if ((!session.online) && (tresholdDate.after(session.lastValid) || (keptCounter > maxToKeep - minToKeep)))
 			{
 				session.pruneFlag = true;
 				MyUtils.dPrintLine("pruned "+session.lastValid.toString()+" was "+session.online.toString()+ " away.");
 			}
 			else
 			{
 				keptCounter++;
 			}
 		}
 		deletePrunedSessions();
 	}
 	public void deletePrunedSessions()
 	{
 		HashMap<InetSocketAddress,Session> keptSessions = new HashMap<InetSocketAddress,Session>();
 		
 		for (Session session : sessions.values()) {
 			if (!session.pruneFlag) {
 				keptSessions.put(session.address,session);
 			}
 		}
 		sessions = keptSessions;
 	}
 	
 	
 } // end of class
 