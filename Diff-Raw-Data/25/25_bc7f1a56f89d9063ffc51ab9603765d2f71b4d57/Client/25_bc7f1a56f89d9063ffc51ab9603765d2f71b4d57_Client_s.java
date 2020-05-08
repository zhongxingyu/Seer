 package net.ropelato.meetpoint.data;
 
 import net.ropelato.meetpoint.server.Util;
 
 import java.util.*;
 
 public class Client
 {
 	public static final int CHARACTER_RED = 0;
 	public static final int CHARACTER_YELLOW = 1;
 	public static final int CHARACTER_GREEN = 2;
 
 	private final String clientId;
 	private final int characterId;
 	private double xPosition = 0;
 	private double zPosition = 0;
 	private double angle = 0;
 	private long lastAccess;
 
 	private Queue<ChatMessage> messageQueue = new LinkedList<ChatMessage>();
 
 	public Client(String clientId, int characterId)
 	{
 		this.clientId = clientId;
 		this.characterId = characterId;
 		this.lastAccess = System.currentTimeMillis();
 	}
 
 	public synchronized String getInformationString()
 	{
 		return clientId + ";" + characterId + ";" + Util.formatDouble(xPosition) + ";" + Util.formatDouble(zPosition) + ";" + (int)angle;
 	}
 
 	public synchronized double getAngle()
 	{
 		return angle;
 	}
 
 	public synchronized void setAngle(double angle)
 	{
 		this.angle = angle;
 	}
 
 	public synchronized long getLastAccess()
 	{
 		return lastAccess;
 	}
 
 	public synchronized void setLastAccess(long lastAccess)
 	{
 		this.lastAccess = lastAccess;
 	}
 
 	public synchronized double getXPosition()
 	{
 		return xPosition;
 	}
 
 	public synchronized void setXPosition(double xPosition)
 	{
 		this.xPosition = xPosition;
 	}
 
 	public synchronized double getZPosition()
 	{
 		return zPosition;
 	}
 
 	public synchronized void setZPosition(double zPosition)
 	{
 		this.zPosition = zPosition;
 	}
 
 	public synchronized void addMessage(ChatMessage message)
 	{
 		this.messageQueue.offer(message);
 	}
 
 	public synchronized ChatMessage getNextMessage()
 	{
 		if(messageQueue.isEmpty())
 		{
 			return null;
 		}
 		else
 		{
 			return this.messageQueue.remove();
 		}
 	}
 
 	public synchronized List<ChatMessage> getAllMessages()
 	{
 		List<ChatMessage> messages = new ArrayList<ChatMessage>();
 		while(!messageQueue.isEmpty())
 		{
 			messages.add(messageQueue.remove());
 		}
 		return messages;
 	}
 
 	public synchronized String getClientId()
 	{
 		return this.clientId;
 	}
 }
 
