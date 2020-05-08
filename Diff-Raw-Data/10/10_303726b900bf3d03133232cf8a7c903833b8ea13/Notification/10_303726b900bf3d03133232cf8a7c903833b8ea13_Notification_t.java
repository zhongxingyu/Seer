 package com.censoredsoftware.demigods.engine.player;
 
 import java.util.Set;
 
 import org.bukkit.entity.Player;
 
 import redis.clients.johm.*;
 
 import com.censoredsoftware.demigods.engine.Demigods;
 import com.censoredsoftware.demigods.engine.language.TranslationManager;
 
 @Model
 public class Notification
 {
 	@Id
 	private Long id;
 	@Attribute
 	private long expiration;
 	@Indexed
 	@Reference
 	private DCharacter receiver;
 	@Indexed
 	@Reference
 	private DCharacter sender;
 	@Attribute
 	private String senderType;
 	@Attribute
 	private String danger;
 	@Attribute
 	private String name;
 	@Attribute
 	private String message;
 
 	void setExpiration(int minutes)
 	{
 		this.expiration = System.currentTimeMillis() + (minutes * 60000);
 	}
 
 	void setDanger(Danger danger)
 	{
 		this.danger = danger.name();
 	}
 
 	void setSenderType(Sender senderType)
 	{
 		this.senderType = senderType.name();
 	}
 
 	void setSender(DCharacter sender)
 	{
 		this.sender = sender;
 	}
 
 	void setReceiver(DCharacter receiver)
 	{
 		this.receiver = receiver;
 	}
 
 	void setName(String name)
 	{
 		this.name = name;
 	}
 
 	void setMessage(String message)
 	{
 		this.message = message;
 	}
 
 	public long getExpiration()
 	{
 		return this.expiration;
 	}
 
 	public Sender getSenderType()
 	{
 		return Sender.valueOf(this.senderType);
 	}
 
 	public Danger getDanger()
 	{
 		return Danger.valueOf(this.danger);
 	}
 
 	public DCharacter getReceiver()
 	{
 		return this.receiver;
 	}
 
 	public DCharacter getSender()
 	{
 		return this.sender;
 	}
 
 	public String getName()
 	{
 		return this.name;
 	}
 
 	public String getMessage()
 	{
 		return this.message;
 	}
 
 	public long getId()
 	{
 		return this.id;
 	}
 
 	public boolean hasExpiration()
 	{
 		return this.expiration != 0L;
 	}
 
 	public static void save(Notification notification)
 	{
 		JOhm.save(notification);
 	}
 
 	public static void remove(Notification notification)
 	{
 		JOhm.delete(Notification.class, notification.getId());
 	}
 
 	public static DCharacter load(Long id)
 	{
 		return JOhm.get(Notification.class, id);
 	}
 
 	public static Set<Notification> loadAll()
 	{
 		return JOhm.getAll(Notification.class);
 	}
 
 	public static class Util
 	{
 		public static Notification create(Sender sender, DCharacter receiver, Danger danger, String name, String message)
 		{
 			Notification notification = new Notification();
 			notification.setReceiver(receiver);
 			notification.setDanger(danger);
 			notification.setSenderType(sender);
 			notification.setName(name);
 			notification.setMessage(message);
 			Notification.save(notification);
 			return notification;
 		}
 
 		public static Notification create(Sender sender, DCharacter receiver, Danger danger, int minutes, String name, String message)
 		{
 			Notification notification = create(sender, receiver, danger, name, message);
 			notification.setExpiration(minutes);
 			Notification.save(notification);
 			return notification;
 		}
 
 		public static Notification create(DCharacter sender, DCharacter receiver, Danger danger, String name, String message)
 		{
 			Notification notification = create(Sender.CHARACTER, receiver, danger, name, message);
 			notification.setSender(sender);
 			Notification.save(notification);
 			return notification;
 		}
 
 		public static Notification create(DCharacter sender, DCharacter receiver, Danger danger, int minutes, String name, String message)
 		{
 			Notification notification = create(sender, receiver, danger, name, message);
 			notification.setExpiration(minutes);
 			Notification.save(notification);
 			return notification;
 		}
 
 		public static void updateNotifications()
 		{
 			for(Notification notification : loadAll())
 			{
 				if(notification.getExpiration() <= System.currentTimeMillis())
 				{
 					Notification.remove(notification);
					notification.getReceiver().removeNotification(notification);
 				}
 			}
 		}
 
 		public static void sendNotification(DCharacter character, Notification notification)
 		{
 			// Add the notification
 			character.addNotification(notification);
 
 			// Message them if possible
 			if(character.getOfflinePlayer().isOnline())
 			{
 				Player player = character.getOfflinePlayer().getPlayer();
 				for(String message : Demigods.text.getTextBlock(TranslationManager.Text.NOTIFICATION_RECEIVED))
 				{
 					player.sendMessage(message);
 				}
 			}
 		}
 	}
 
 	public enum Sender
 	{
 		PLUGIN, QUEST, CHARACTER;
 	}
 
 	public enum Danger
 	{
 		GOOD, NEUTRAL, BAD;
 	}
 }
