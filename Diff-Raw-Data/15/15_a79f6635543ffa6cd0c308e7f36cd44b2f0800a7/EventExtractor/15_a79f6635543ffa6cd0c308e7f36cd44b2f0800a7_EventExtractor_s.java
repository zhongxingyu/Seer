 /*
  * This file is public domain
  */
 
 package net.boreeas.irc;
 
 import net.boreeas.irc.events.*;
 import org.apache.commons.lang.ArrayUtils;
 
 /**
  * Extracts events from a received line and notifies all EventListeners
  * registered to a specific EventPump.
  * @author Boreeas
  */
 class EventExtractor {
 
     static void checkAndFireEvents(String[] parts, EventPump eventPump) {
         if (parts[0].equals("PING")) {
 
             eventPump.onPingReceived(new PingEvent(parts[1]));
         } else if (parts.length >= 2) {
 
             if (parts[1].equalsIgnoreCase("JOIN")) {
 
                 extractJoinChannel(parts, eventPump);
             } else if (parts[1].equalsIgnoreCase("PART")) {
 
                 extractPartChannel(parts, eventPump);
             } else if (parts[1].equalsIgnoreCase("PRIVMSG")) {
 
                 extractMessage(parts, eventPump);
             } else if (parts[1].equalsIgnoreCase("NOTICE")) {
 
                 extractNotice(parts, eventPump);
             } else if (parts[1].equalsIgnoreCase("QUIT")) {
 
                 extractQuit(parts, eventPump);
             } else if (parts[1].equalsIgnoreCase("MODE")) {
 
                 extractModeChange(parts, eventPump);
             } else if (parts[1].equalsIgnoreCase("NICK")) {
 
                 extractNickChange(parts, eventPump);
             } else if (parts[1].equals("001")) {
 
                 eventPump.onWelcomeReceived(new WelcomeReceivedEvent());
             } else if (parts[1].equals("005")) {
 
                 extractSupports(parts, eventPump);
             } else {
                 eventPump.onUnknownLine(new UnknownLineEvent(parts));
             }
 
         } else {
             eventPump.onUnknownLine(new UnknownLineEvent(parts));
         }
     }
 
     private static void extractNickChange(String[] parts, EventPump eventPump) {
 
         User user = new User(parts[0]);
         String newNick = parts[2];
 
         eventPump.onUserChangedNick(new UserChangedNickEvent(user, newNick));
     }
 
     private static void extractJoinChannel(String[] parts, EventPump eventPump) {
 
         User user = new User(parts[0]);
         String channel = parts[2];
 
         eventPump.onUserJoinedChannel(new UserJoinedChannelEvent(user, channel));
     }
 
     private static void extractPartChannel(String[] parts, EventPump eventPump) {
 
         User user = new User(parts[0]);
         String channel = parts[2];
 
         UserLeftChannelEvent evt;
 
         if (parts.length > 3) {
             evt = new UserLeftChannelEvent(user, channel, parts[3]);
         } else {
             evt = new UserLeftChannelEvent(user, channel);
         }
 
         eventPump.onUserLeftChannel(evt);
     }
 
     private static void extractMessage(String[] parts, EventPump eventPump) {
 
         User user = new User(parts[0]);
        String target = parts[1];
         String msg = parts[3];
 
         MessageReceivedEvent evt = new MessageReceivedEvent(user, target, msg);
         eventPump.onMessageReceived(evt);
     }
 
     private static void extractNotice(String[] parts, EventPump eventPump) {
 
         if (parts[0].contains("!") && parts[0].contains("@s")) {
 
             User user = new User(parts[0]);
            String target = parts[1];
             String msg = parts[3];
 
             MessageReceivedEvent evt =
                                  new MessageReceivedEvent(user, target, msg);
             eventPump.onNoticeReceived(evt);
         } else {
 
             eventPump.onServerNotice(new ServerNoticeEvent(parts[0], parts[3]));
         }
     }
 
     private static void extractQuit(String[] parts, EventPump eventPump) {
 
         User user = new User(parts[0]);
         UserQuitNetworkEvent evt =
                              new UserQuitNetworkEvent(user, parts.length > 2
                                                             ? parts[2]
                                                             : "");  // No message
         eventPump.onUserQuitNetwork(evt);
     }
 
     private static void extractModeChange(String[] parts, EventPump eventPump) {
 
         if (parts[0].contains("!") && parts[0].contains("@")) {
 
             User user = new User(parts[0]);
             String channel = parts[2];
             String modes = parts[3];
             String[] modeArgs =
                      (String[]) ArrayUtils.subarray(parts, 4, parts.length);
 
             ChannelModeChangeEvent evt =
                                    new ChannelModeChangeEvent(user, channel, modes, modeArgs);
             eventPump.onChannelModeChange(evt);
         } else {
 
             String[] modeArgs =
                      (String[]) ArrayUtils.subarray(parts, 4, parts.length);
             eventPump.onSelfModeChange(new SelfModeChangeEvent(parts[3], modeArgs));
         }
     }
 
     private static void extractSupports(String[] parts, EventPump eventPump) {
 
         int endIndex =
             parts.length - ((parts[parts.length - 1].equalsIgnoreCase("are supported by this server"))
                             ? 1     // Cut off last element ("are supported...")
                             : 0);
 
         String[] supports = (String[]) ArrayUtils.subarray(parts, 3, endIndex);
 
         SupportListReceivedEvent evt = new SupportListReceivedEvent(supports);
         eventPump.onSupportListReceived(evt);
     }
 
     private EventExtractor() {
     }
 }
