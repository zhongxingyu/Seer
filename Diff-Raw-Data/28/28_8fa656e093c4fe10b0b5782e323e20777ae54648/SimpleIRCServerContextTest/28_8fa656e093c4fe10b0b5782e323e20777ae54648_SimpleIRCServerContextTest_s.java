 /**
  * SimpleIRCServerContextTest.java
  */
 package de.ekdev.ekirc.core.test;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import de.ekdev.ekevent.EventException;
 import de.ekdev.ekevent.EventHandler;
 import de.ekdev.ekevent.EventListener;
 import de.ekdev.ekirc.core.AsIRCMessage;
 import de.ekdev.ekirc.core.IRCChannel;
 import de.ekdev.ekirc.core.IRCChannelList;
 import de.ekdev.ekirc.core.IRCIdentity;
 import de.ekdev.ekirc.core.IRCManager;
 import de.ekdev.ekirc.core.IRCMessage;
 import de.ekdev.ekirc.core.IRCNetwork;
 import de.ekdev.ekirc.core.IRCNicknameFormatException;
 import de.ekdev.ekirc.core.IRCUsernameFormatException;
 import de.ekdev.ekirc.core.event.ActionMessageToChannelEvent;
 import de.ekdev.ekirc.core.event.ActionMessageToUserEvent;
 import de.ekdev.ekirc.core.event.ChannelListUpdateEvent;
 import de.ekdev.ekirc.core.event.ChannelModeChangeEvent;
 import de.ekdev.ekirc.core.event.ChannelModeUpdateEvent;
 import de.ekdev.ekirc.core.event.NickChangeEvent;
 import de.ekdev.ekirc.core.event.NoticeToChannelEvent;
 import de.ekdev.ekirc.core.event.NoticeToUserEvent;
 import de.ekdev.ekirc.core.event.PrivateMessageToChannelEvent;
 import de.ekdev.ekirc.core.event.PrivateMessageToUserEvent;
 import de.ekdev.ekirc.core.event.UserModeChangeEvent;
 import de.ekdev.ekirc.core.event.listener.AutoReconnector;
 import de.ekdev.ekirc.core.event.listener.UserConnectionRegistrator;
 
 /**
  * @author ekDev
  */
 public class SimpleIRCServerContextTest
 {
     public SimpleIRCServerContextTest()
     {
     }
 
     public static void main(String[] args) throws NullPointerException, IRCNicknameFormatException,
             IRCUsernameFormatException
     {
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
         IRCManager ircManager = new IRCManager();
         // IRCNetwork inet = new IRCNetwork(ircManager, new IRCIdentity("rea ree", "pass"), "irc.irchighway.net", 6667);
         // IRCNetwork inet = new IRCNetwork(ircManager, new IRCIdentity("rea ree", "pass"), "irc.chatzona.org", 6667);
         IRCNetwork inet = new IRCNetwork(ircManager, new IRCIdentity("rea ree", "pass"), "irc.webchat.org", 6667);
 
         final String nick = "coor";
         // final String nick = "mike";
 
         try
         {
             // registrator
             ircManager.getEventManager().register(new UserConnectionRegistrator(inet, nick, "uerles", true));
 
             // nickname change
             ircManager.getEventManager().register(new EventListener() {
                 @EventHandler
                 public void onNickChange(NickChangeEvent nce)
                 {
                     nce.getIRCNetwork()
                             .getIRCConnectionLog()
                             .message(
                                     (nce.isMe() ? "MY " : "") + "NICK CHANGE FROM '" + nce.getOldNick() + "' TO '"
                                             + nce.getNewNick() + "'!");
                 }
             });
 
             // channel list
             ircManager.getEventManager().register(new EventListener() {
                 @EventHandler
                 public void newChannelList(ChannelListUpdateEvent ucle)
                 {
                     if (ucle.hasOldIRCChannelList())
                     {
                         ucle.getIRCNetwork().getIRCConnectionLog()
                                 .message("old channel list: size = " + ucle.getOldIRCChannelList().size());
                     }
                     ucle.getIRCNetwork().getIRCConnectionLog()
                             .message("new channel list: size = " + ucle.getNewIRCChannelList().size());
                     // ucle.getIRCNetwork().getIRCConnectionLog()
                     // .object("ucle.getNewIRCChannelList()", ucle.getNewIRCChannelList());
                     int totalUsers = 0;
                     for (IRCChannelList.Entry le : ucle.getNewIRCChannelList())
                     {
                         totalUsers += le.getNumberOfUsers();
                     }
                     ucle.getIRCNetwork()
                             .getIRCConnectionLog()
                             .message(
                                     "new channel list: delta users/channel = " + totalUsers
                                             / (float) ucle.getNewIRCChannelList().size());
                 }
             });
 
             // messages
             ircManager.getEventManager().register(new EventListener() {
                 @EventHandler
                 public void onPrivMsgToMe(PrivateMessageToUserEvent event)
                 {
                     event.getIRCNetwork()
                             .getIRCConnectionLog()
                             .object("PRIVMSG to user [" + event.getTargetIRCUser().getNickname() + "]",
                                     event.getMessage());
                 }
 
                 @EventHandler
                 public void onPrivMsgToChan(PrivateMessageToChannelEvent event)
                 {
                     event.getIRCNetwork()
                             .getIRCConnectionLog()
                             .object("PRIVMSG to chan [" + event.getTargetIRCChannel().getName() + "]",
                                     event.getMessage());
                 }
 
                 @EventHandler
                 public void onNoticeToMe(NoticeToUserEvent event)
                 {
                     event.getIRCNetwork()
                             .getIRCConnectionLog()
                             .object("NOTICE to user [" + event.getTargetIRCUser().getNickname() + "]",
                                     event.getMessage());
                 }
 
                 @EventHandler
                 public void onNoticeToChan(NoticeToChannelEvent event)
                 {
                     event.getIRCNetwork()
                             .getIRCConnectionLog()
                             .object("NOTICE to chan [" + event.getTargetIRCChannel().getName() + "]",
                                     event.getMessage());
                 }
 
                 @EventHandler
                 public void onActionToUser(ActionMessageToUserEvent event)
                 {
                     event.getIRCNetwork()
                             .getIRCConnectionLog()
                             .object("ACTION to chan [" + event.getTargetIRCUser().getNickname() + "]",
                                     event.getActor().getNickname() + " " + event.getMessage());
                 }
 
                 @EventHandler
                 public void onActionToChan(ActionMessageToChannelEvent event)
                 {
                     event.getIRCNetwork()
                             .getIRCConnectionLog()
                             .object("ACTION to chan [" + event.getTargetIRCChannel().getName() + "]",
                                     event.getActor().getNickname() + " " + event.getMessage());
                 }
             });
 
             // mode changes
             ircManager.getEventManager().register(new EventListener() {
                 @EventHandler
                 public void onUserModeChange(UserModeChangeEvent event)
                 {
                     event.getIRCNetwork()
                             .getIRCConnectionLog()
                             .object("USERMODE change [" + event.getTargetIRCUser().getNickname() + "]", event.getMode());
                 }
 
                 @EventHandler
                 public void onChannelModeChange(ChannelModeChangeEvent event)
                 {
                     event.getIRCNetwork()
                             .getIRCConnectionLog()
                             .message(
                                     "CHANNELMODE change [" + event.getTargetIRCChannel().getName() + "] : '"
                                             + event.getOldMode() + "' + '" + event.getModeChange() + "' => '"
                                             + event.getNewMode() + "'");
                 }
 
                 @EventHandler
                 public void onChannelModeSet(ChannelModeUpdateEvent event)
                 {
                     event.getIRCNetwork().getIRCConnectionLog()
                             .object("CHANNEL-MODE [" + event.getIRCChannel().getName() + "]", event.getMode());
                 }
 
             });
 
             // reconnect
             // only on inet for 3 successful times
             ircManager.getEventManager().register(new AutoReconnector(inet, 15 * 1000, 3));
 
             // on all nets forever trying
             // ircManager.getEventManager().register(new AutoReconnector(null, 15 * 1000, 0));
         }
         catch (EventException e)
         {
             e.printStackTrace();
         }
 
         inet.connect();
        
         waitForInput(br);
 
         // inet.send(new IRCNickCommand("nickles"));
         // inet.send(new IRCListCommand());
         // inet.send(new IRCWhoCommand());
 
         waitForInput(br);
 
         // inet.disconnect();
         //
         // inet.reconnect();
 
         // inet.getIRCConnectionLog().tryMoveLogFile("src/newlog.txt");
 
         // inet.send(new IRCNickCommand("nick"));
 
         send(inet, "JOIN #ebooks");
         send(inet, "PRIVMSG #ebooks :@search Hohlbein Genesis");
 
         waitForInput(br);
 
         for (IRCChannel ircChannel : inet.getIRCChannelManager().getIRCChannels())
         {
             System.out.println(ircChannel.getName() + " - " + ircChannel.getTopic() + " - " + ircChannel.getMode());
         }
 
         // inet.send(new IRCNickCommand("coor"));
         // inet.send(new IRCChannelModeCommand(inet.getIRCChannelManager().getIRCChannel("#ebooks")));
 
         waitForInput(br);
 
         inet.quit("-)");
 
         sleep(3000);
 
         if (inet.isConnected()) inet.disconnect(false);
         inet.closeConnectionLog();
     }
 
     // ------------------------------------------------------------------------
 
     public static void send(IRCNetwork inet, final String line)
     {
         inet.send(new AsIRCMessage() {
             @Override
             public String asIRCMessageString()
             {
                 return line;
             }
 
             @Override
             public IRCMessage asIRCMessage()
             {
                 return null;
             }
         });
     }
 
     public static void send(IRCNetwork inet, AsIRCMessage ircMessage)
     {
         inet.send(ircMessage);
     }
 
     public static void sleep(long millisec)
     {
         try
         {
             Thread.sleep(millisec);
         }
         catch (InterruptedException e)
         {
         }
     }
 
     public static String waitForInput(BufferedReader br)
     {
         String line;
         try
         {
             while ((line = br.readLine()) != null)
             {
                 return line;
             }
         }
         catch (IOException e)
         {
         }
 
         return null;
     }
 
 }
