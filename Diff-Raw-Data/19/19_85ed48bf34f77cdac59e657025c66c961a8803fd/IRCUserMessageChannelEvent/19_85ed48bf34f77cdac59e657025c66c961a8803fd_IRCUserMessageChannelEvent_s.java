 package com.titankingdoms.dev.TitanIRC.api.event;
 
 import com.titankingdoms.dev.TitanIRC.api.IRCChannel;
 import com.titankingdoms.dev.TitanIRC.api.IRCServer;
 import org.bukkit.event.Event;
 import org.bukkit.event.HandlerList;
 
 /**
 * Created with IntelliJ IDEA.
 * User: 8381111
 * Date: 17/04/12
 * Time: 6:25 AM
 * To change this template use File | Settings | File Templates.
  */
 public class IRCUserMessageChannelEvent extends Event {
     public static HandlerList handlerList = new HandlerList();
     public static HandlerList getHandlerList()
     {
         return handlerList;
     }
     public HandlerList getHandlers()
     {
         return handlerList;
     }
 
     public IRCUserMessageChannelEvent(String hostmask, String message, IRCChannel chan)
     {
         this.chan = chan;
         this.hostmask = hostmask;
         this.message = message;
     }
 
     private String hostmask;
     private IRCChannel chan;
     private String message;
 
     public String getMessage()
     {
         return message;
     }
     public String getUserNick()
     {
         return hostmask.substring(1).split("!")[0];
     }
     public String getUserIdent()
     {
         return hostmask.split("!")[1].split("@")[0];
     }
     public String getUserHostname()
     {
         return hostmask.split("@")[1].split(" ")[0];
     }
     public String getUserHostmask()
     {
         return hostmask;
     }
 
     public IRCServer getServer()
     {
         return chan.getServer();
     }
 
     public IRCChannel getChannel()
     {
         return chan;
     }
 }
