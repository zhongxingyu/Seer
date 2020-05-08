 /**
  * IRCChannel.java
  */
 package de.ekdev.ekirc.core;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Objects;
 import java.util.Set;
 
 import de.ekdev.ekirc.core.commands.channel.IRCChannelModeCommand;
 import de.ekdev.ekirc.core.commands.channel.IRCJoinCommand;
 import de.ekdev.ekirc.core.commands.channel.IRCNamesCommand;
 import de.ekdev.ekirc.core.commands.channel.IRCPartCommand;
 import de.ekdev.ekirc.core.commands.message.IRCNoticeCommand;
 import de.ekdev.ekirc.core.commands.message.IRCPrivateMessageCommand;
 import de.ekdev.ekirc.core.commands.user.IRCWhoCommand;
 
 /**
  * @author ekDev
  */
 public class IRCChannel
 {
     public final static int MAX_CHANNEL_NAME_LENGTH = 50;
     public final static String CHANNEL_PREFIXES = "&#+!";
     public final static String NO_CHANNEL = "*";
 
     private final IRCChannelManager ircChannelManager;
 
     private final String name;
     private String topic;
 
     // http://webtoman.com/opera/panel/ircdmodes.html
     private String mode; // -> EnumSet?
 
     private final Set<IRCUser> users;
 
     // TODO: more Sets for different statuses of users? Map<User, Status?>
     // for set operations are different sets better
 
     // ------------------------------------------------------------------------
 
     public IRCChannel(IRCChannelManager ircChannelManager, String name, String topic) throws NullPointerException
     {
         Objects.requireNonNull(ircChannelManager, "ircChannelManager must not be null!");
         Objects.requireNonNull(name, "name must not be null!");
 
         this.ircChannelManager = ircChannelManager;
         this.name = name;
         this.topic = topic;
 
         this.users = Collections.synchronizedSet(new HashSet<IRCUser>());
 
         // automatically add to manager
         this.ircChannelManager.addIRCChannel(this);
     }
 
     public IRCChannel(IRCChannelManager ircChannelManager, String name) throws NullPointerException
     {
         this(ircChannelManager, name, null);
     }
 
     // ------------------------------------------------------------------------
 
     public final IRCChannelManager getIRCChannelManager()
     {
         return this.ircChannelManager;
     }
 
     // ------------------------------------------------------------------------
     // Fields
 
     public String getName()
     {
         return this.name;
     }
 
     public char getType()
     {
         return this.name.charAt(0);
     }
 
     public String getTopic()
     {
         return this.topic;
     }
 
     protected void setTopic(String topic)
     {
         this.topic = topic;
     }
 
     // --------------------------------
 
     public boolean hasMode()
     {
         return this.mode != null;
     }
 
    protected String getMode(boolean autoRefreshIfNotAvailable)
     {
        if (this.mode == null && autoRefreshIfNotAvailable)
         {
             this.refreshMode();
         }
 
         return this.mode;
     }
 
    public String getMode()
    {
        return this.getMode(true);
    }

     protected void setMode(String mode)
     {
         // as response to 324
         if (mode == null) return;
 
         this.mode = mode;
     }
 
     protected void updateMode(String mode, List<String> modeparams)
     {
         // as response to MODE
         // TODO: parse mode ...
         this.ircChannelManager.getIRCNetwork().getIRCConnectionLog().object(this.name + ".setMode ", mode);
 
         this.mode += mode;
     }
 
     // --------------------------------
 
     public Set<IRCUser> getUsers()
     {
         return Collections.unmodifiableSet(this.users);
     }
 
     // ------------------------------------------------------------------------
     // Update
 
     protected void addIRCUser(IRCUser ircUser)
     {
         if (ircUser == null) return;
 
         // TODO: add to different sets (MODE dependent)
 
         this.users.add(ircUser);
 
         // add channel reference in user
         ircUser.addIRCChannel(this);
     }
 
     protected void removeIRCUser(IRCUser ircUser)
     {
         if (ircUser == null) return;
 
         // TODO: remove from all other sets too
 
         this.users.remove(ircUser);
 
         // remove channel reference from user
         ircUser.removeIRCChannel(this);
     }
 
     protected void removeAllIRCUsers()
     {
         // remove the channel reference from all users of this channel
         for (IRCUser user : this.users)
         {
             user.removeIRCChannel(this);
         }
 
         this.users.clear();
     }
 
     // ------------------------------------------------------------------------
     // Actions
 
     public void sendPrivateMessage(String message) throws NullPointerException
     {
         AsIRCMessage ircMessage = new IRCPrivateMessageCommand(this, message);
 
         this.ircChannelManager.getIRCNetwork().send(ircMessage);
     }
 
     public boolean sendPrivateMessageSilent(String message)
     {
         boolean ret = true;
 
         try
         {
             this.sendPrivateMessage(message);
         }
         catch (Exception e)
         {
             this.ircChannelManager.getIRCNetwork().getIRCConnectionLog().exception(e); // TODO: log?
             ret = false;
         }
 
         return ret;
     }
 
     public void sendNotice(String message) throws NullPointerException
     {
         AsIRCMessage ircMessage = new IRCNoticeCommand(this, message);
 
         this.ircChannelManager.getIRCNetwork().send(ircMessage);
     }
 
     public boolean sendNoticeSilent(String message)
     {
         boolean ret = true;
 
         try
         {
             this.sendNotice(message);
         }
         catch (Exception e)
         {
             this.ircChannelManager.getIRCNetwork().getIRCConnectionLog().exception(e); // TODO: log?
             ret = false;
         }
 
         return ret;
     }
 
     // --------------------------------
 
     protected void changeMode(String mode)
     {
         this.changeMode(mode, new ArrayList<String>());
     }
 
     public void changeMode(String modes, List<String> modeparams)
     {
         if (modes == null || modes.trim().length() == 0)
         {
             this.refreshMode();
             return;
         }
 
         this.ircChannelManager.getIRCNetwork().send(new IRCChannelModeCommand(this, modes, modeparams));
     }
 
     public void changeMode(String modes, String... modeparams)
     {
         List<String> mlist = new ArrayList<>();
         if (modes != null && modeparams != null)
         {
             for (String modeparam : modeparams)
             {
                 mlist.add(modeparam);
             }
         }
 
         this.changeMode(modes, mlist);
     }
 
     public void refreshMode()
     {
         this.ircChannelManager.getIRCNetwork().send(new IRCChannelModeCommand(this));
     }
 
     public void refreshIRCUserList() throws NullPointerException
     {
         // TODO: which one?
         this.ircChannelManager.getIRCNetwork().send(new IRCNamesCommand(this));
         this.ircChannelManager.getIRCNetwork().send(new IRCWhoCommand(this));
     }
 
     // --------------------------------
 
     public void join(String key)
     {
         // TODO: store key?
         this.ircChannelManager.getIRCNetwork().send(new IRCJoinCommand(this.name, key));
     }
 
     public void part(String reason)
     {
         this.ircChannelManager.getIRCNetwork().send(new IRCPartCommand(this, reason));
     }
 
     public void part()
     {
         this.ircChannelManager.getIRCNetwork().send(new IRCPartCommand(this));
     }
 
     // ------------------------------------------------------------------------
 
     public static String validateChannelname(String channelname) throws NullPointerException,
             IRCChannelNameFormatException
     {
         Objects.requireNonNull(channelname, "Invalid channelname format: channelname must not be null!");
         if (channelname.length() == 0)
         {
             throw new IRCChannelNameFormatException("channelname must not be empty!");
         }
         if (channelname.length() > IRCChannel.MAX_CHANNEL_NAME_LENGTH)
         {
             throw new IRCChannelNameFormatException("channelname must not be longer than 50 characters!");
         }
 
         if (IRCChannel.CHANNEL_PREFIXES.indexOf(channelname.charAt(0)) == -1)
         {
             throw new IRCChannelNameFormatException("channelname must start with the prefixes '&', '#', '+' or '!' !");
         }
 
         // TODO: more special with '!' and chanid, chanmasks?
 
         if (channelname.indexOf(0x00) != -1)
         {
             throw new IRCChannelNameFormatException("channelname must not contain a 0x00 (null) character!");
         }
         if (channelname.indexOf(0x07) != -1)
         {
             throw new IRCChannelNameFormatException("channelname must not contain a 0x07 (^G) character!");
         }
         if (channelname.indexOf(0x0A) != -1)
         {
             throw new IRCChannelNameFormatException("channelname must not contain a 0x0A (carriage return) character!");
         }
         if (channelname.indexOf(0x0D) != -1)
         {
             throw new IRCChannelNameFormatException("channelname must not contain a 0x0D (line feed) character!");
         }
         if (channelname.indexOf(0x20) != -1)
         {
             throw new IRCChannelNameFormatException("channelname must not contain a 0x20 (space) character!");
         }
         if (channelname.indexOf(0x2C) != -1)
         {
             throw new IRCChannelNameFormatException("channelname must not contain a 0x2C (comma) character!");
         }
         // if (channelname.indexOf(0x3A) != -1)
         // {
         // throw new IRCChannelNameFormatException("channelname must not contain a 0x3A (colon) character!");
         // }
 
         return channelname;
     }
 
     public static String validateChannelkey(String channelkey) throws NullPointerException,
             IRCChannelKeyFormatException
     {
         Objects.requireNonNull(channelkey, "Invalid channelname format: channelname must not be null!");
         if (channelkey.length() == 0)
         {
             throw new IRCChannelKeyFormatException("channelkey must not be empty!");
         }
         if (channelkey.length() > 23)
         {
             throw new IRCChannelKeyFormatException("channelkey must not be longer than 23 characters!");
         }
 
         // TODO: check key codes (rfc contradictory)
 
         if (channelkey.indexOf(0x00) != -1)
         {
             throw new IRCChannelKeyFormatException("channelkey must not contain a 0x00 (null) character!");
         }
         if (channelkey.indexOf(0x09) != -1)
         {
             throw new IRCChannelKeyFormatException("channelkey must not contain a 0x09 (hor. tab) character!");
         }
         if (channelkey.indexOf(0x0A) != -1)
         {
             throw new IRCChannelKeyFormatException("channelkey must not contain a 0x0A (carriage return) character!");
         }
         if (channelkey.indexOf(0x0B) != -1)
         {
             throw new IRCChannelKeyFormatException("channelkey must not contain a 0x0A (vert. tab) character!");
         }
         if (channelkey.indexOf(0x0C) != -1)
         {
             throw new IRCChannelKeyFormatException("channelkey must not contain a 0x0c (form feed) character!");
         }
         if (channelkey.indexOf(0x0D) != -1)
         {
             throw new IRCChannelKeyFormatException("channelkey must not contain a 0x0D (line feed) character!");
         }
         if (channelkey.indexOf(0x20) != -1)
         {
             throw new IRCChannelKeyFormatException("channelkey must not contain a 0x20 (space) character!");
         }
 
         return channelkey;
     }
 
     // ------------------------------------------------------------------------
 }
