 package com.minecraftdimensions.bungeesuite.managers;
 
 import com.minecraftdimensions.bungeesuite.BungeeSuite;
 import com.minecraftdimensions.bungeesuite.Utilities;
 import com.minecraftdimensions.bungeesuite.configlibrary.Config;
 import com.minecraftdimensions.bungeesuite.configs.Channels;
 import com.minecraftdimensions.bungeesuite.configs.ChatConfig;
 import com.minecraftdimensions.bungeesuite.objects.BSPlayer;
 import com.minecraftdimensions.bungeesuite.objects.Channel;
 import com.minecraftdimensions.bungeesuite.objects.Messages;
 import com.minecraftdimensions.bungeesuite.objects.ServerData;
 import com.minecraftdimensions.bungeesuite.tasks.SendPluginMessage;
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.config.ServerInfo;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.connection.Server;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 public class ChatManager {
 
     public static ArrayList<Channel> channels = new ArrayList<Channel>();
     public static HashMap<String, ServerData> serverData = new HashMap<String, ServerData>();
     public static HashMap<String, ArrayList<Channel>> channelsSentToServers = new HashMap<String, ArrayList<Channel>>();
     public static boolean MuteAll;
 
     public static void loadChannels() {
         LoggingManager.log( ChatColor.GOLD + "Loading channels" );
         Config chan = Channels.channelsConfig;
         String server = ProxyServer.getInstance().getConsole().getName();
         //Load Global
         loadChannel( server, "Global", chan.getString( "Channels.Global", Messages.CHANNEL_DEFAULT_GLOBAL ), true, true );
         //Load Admin Channel
         loadChannel( server, "Admin", chan.getString( "Channels.Admin", Messages.CHANNEL_DEFAULT_ADMIN ), true, true );
         //Load Faction Channel
         loadChannel( server, "Faction", chan.getString( "Channels.Faction", Messages.CHANNEL_DEFAULT_FACTION ), true, true );
         //Load Faction Ally Channel
         loadChannel( server, "FactionAlly", chan.getString( "Channels.FactionAlly", Messages.CHANNEL_DEFAULT_FACTION_ALLY ), true, true );
         //Load Server Channels
         for ( String servername : ProxyServer.getInstance().getServers().keySet() ) {
             loadChannel( server, servername, chan.getString( "Channels.Servers." + servername + ".Server", Messages.CHANNEL_DEFAULT_SERVER ), true, true );
             loadChannel( server, servername + " Local", chan.getString( "Channels.Servers." + servername + ".Local", Messages.CHANNEL_DEFAULT_LOCAL ), true, true );
             loadServerData( servername, chan.getString( "Channels.Servers." + servername + ".Shortname", servername.substring( 0, 1 ) ), chan.getBoolean( "Channels.Servers." + servername + ".ForceChannel", false ), chan.getString( "Channels.Servers." + servername + ".ForcedChannel", "Server" ), chan.getBoolean( "Channels.Servers." + servername + ".UsingFactionChannels", false ), chan.getInt( "Channels.Servers." + servername + ".LocalRange", 50 ), chan.getBoolean( "Channels.Servers." + servername + ".DisableConnectionMessages", true ) );
         }
         //Load custom channels from db
         ResultSet res = SQLManager.sqlQuery( "SELECT * FROM BungeeCustomChannels" );
         try {
             while ( res.next() ) {
                 loadChannel( res.getString( "owner" ), res.getString( "channelname" ), res.getString( "format" ), false, res.getBoolean( "open" ) );
             }
         } catch ( SQLException e ) {
             e.printStackTrace();
         }
         LoggingManager.log( ChatColor.GOLD + "Channels loaded - " + ChatColor.DARK_GREEN + channels.size() );
     }
 
     private static void loadServerData( String name, String shortName, boolean forcingChannel, String forcedChannel, boolean usingFacs, int localDistance, boolean connectionMessages ) {
         ServerData d = new ServerData( name, shortName, forcingChannel, forcedChannel, usingFacs, localDistance, connectionMessages );
         if ( serverData.get( d ) == null ) {
             serverData.put( name, d );
         }
     }
 
     public static void loadChannel( String owner, String name, String format, boolean isDefault, boolean open ) {
         Channel c = new Channel( name, format, owner, false, isDefault, open );
         channels.add( c );
     }
 
     public static boolean usingFactions( Server server ) {
         return serverData.get( server.getInfo().getName() ).usingFactions();
     }
 
     public static void sendDefaultChannelsToServer( Server server ) {
         ArrayList<Channel> chans = getDefaultChannels( server.getInfo().getName() );
         for ( Channel c : chans ) {
             if ( !sentChannelToServer( server, c ) ) {
                 ByteArrayOutputStream b = new ByteArrayOutputStream();
                 DataOutputStream out = new DataOutputStream( b );
                 try {
                     out.writeUTF( "SendChannel" );
                     out.writeUTF( c.serialise() );
                 } catch ( IOException e ) {
                     e.printStackTrace();
                 }
                 sendPluginMessageTaskChat( server.getInfo(), b );
             }
         }
         channelsSentToServers.put( server.getInfo().getName(), chans );
     }
 
     public static void sendChannelToServer( Server server, Channel channel ) {
         if ( !sentChannelToServer( server, channel ) ) {
             ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b );
             try {
                 out.writeUTF( "SendChannel" );
                 out.writeUTF( channel.serialise() );
             } catch ( IOException e ) {
                 e.printStackTrace();
             }
             sendPluginMessageTaskChat( server.getInfo(), b );
             channelsSentToServers.get( server.getInfo().getName() ).add( channel );
         }
     }
 
     public static ArrayList<Channel> getDefaultChannels( String server ) {
         ArrayList<Channel> chans = new ArrayList<Channel>();
         for ( Channel c : channels ) {
             if ( c.getName().equals( "Global" ) || c.getName().equals( "Admin" ) || c.getName().equals( server ) || c.getName().equals( server + " Local" ) ) {
                 chans.add( c );
             } else if ( serverData.get( server ).usingFactions() && ( c.getName().equals( "Faction" ) || c.getName().equals( "FactionAlly" ) ) ) {
                 chans.add( c );
             }
         }
         return chans;
     }
 
     public void createNewCustomChannel( String owner, String name, String format, boolean open ) throws SQLException {
         SQLManager.standardQuery( "INSERT INTO BungeeCustomChannels VALUES('" + name + "','" + owner + "','" + format + "'," + open + ",)" );
         Channel c = new Channel( owner, name, format, false, false, false );
         channels.add( c );
     }
 
     public static boolean channelExists( String name ) {
         for ( Channel c : channels ) {
             if ( c.getName().equals( name ) ) {
                 return true;
             }
         }
         return false;
     }
 
     public static boolean channelSimilarExists( String name ) {
         for ( Channel c : channels ) {
             if ( c.getName().toLowerCase().contains( name.toLowerCase() ) ) {
                 return true;
             }
         }
         return false;
     }
 
     public static ArrayList<Channel> getPlayersChannels( BSPlayer p ) {
         return p.getPlayersChannels();
     }
 
     public static void loadPlayersChannels( ProxiedPlayer player, Server server ) throws SQLException {
         ResultSet res = SQLManager.sqlQuery( "SELECT channel FROM BungeeChannelMembers WHERE player = '" + player.getName() + "'" );
         while ( res.next() ) {
             getChannel( res.getString( "channel" ) ).addMember( player.getName() );
         }
         res.close();
         sendPlayersChannels( PlayerManager.getPlayer( player ), server );
     }
 
     private static void sendPlayersChannels( BSPlayer p, Server server ) {
         for ( Channel c : p.getPlayersChannels() ) {
             if ( !channelsSentToServers.get( server.getInfo().getName() ).contains( c ) ) {
                 sendChannelToServer( server, c );
             }
         }
 
     }
 
     public static boolean channelsSentToServer( Server server ) {
         return channelsSentToServers.containsKey( server.getInfo().getName() );
     }
 
     public static boolean sentChannelToServer( Server server, Channel channel ) {
         return channelsSentToServers.get( server.getInfo().getName() ) != null && channelsSentToServers.get( server.getInfo().getName() ).contains( channel );
     }
 
     public static Channel getChannel( String name ) {
         for ( Channel chan : channels ) {
             if ( chan.getName().equals( name ) ) {
                 return chan;
             }
         }
         return null;
     }
 
     public static Channel getSimilarChannel( String name ) {
         for ( Channel chan : channels ) {
             if ( chan.getName().toLowerCase().contains( name.toLowerCase() ) ) {
                 return chan;
             }
         }
         return null;
     }
 
     public static void sendPluginMessageTaskChat( ServerInfo server, ByteArrayOutputStream b ) {
         BungeeSuite.proxy.getScheduler().runAsync( BungeeSuite.instance, new SendPluginMessage( "BungeeSuiteChat", server, b ) );
     }
 
     public static void sendPlayer( String player, Server server, boolean serverConnect ) throws SQLException {
         BSPlayer p = PlayerManager.getPlayer( player );
         if ( serverConnect ) {
             setPlayerToForcedChannel( p, server );
         }
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "SendPlayer" );
             out.writeUTF( p.getName() );
             out.writeUTF( p.getChannel() );
             out.writeBoolean( p.isMuted() );
             out.writeUTF( p.getNickname() );
             out.writeUTF( p.getTempName() );
             out.writeBoolean( p.isChatSpying() );
             out.writeBoolean( p.isDND() );
             out.writeBoolean( p.isAFK() );
 
         } catch ( IOException e ) {
             e.printStackTrace();
         }
         sendPluginMessageTaskChat( server.getInfo(), b );
 
     }
 
     private static void setPlayerToForcedChannel( BSPlayer p, Server server ) throws SQLException {
     	
         Channel c = getChannel( p.getChannel() );
         
         ServerData sd = ChatManager.serverData.get( server.getInfo().getName() );
         if ( sd.forcingChannel() ) {
             String channel = sd.getForcedChannel();
             if ( channel.equalsIgnoreCase( "server" ) ) {
                 channel = sd.getServerName();
             } else if ( channel.equalsIgnoreCase( "local" ) ) {
                 channel = sd.getServerName() + " Local";
             } else if ( channel.equalsIgnoreCase( "global" ) ) {
                 channel = "Global";
             }
             c = getChannel(channel);
             setPlayersChannel(p, c, false);
             return;
         }
         if ( c == null ) {
            c = getChannel( ChatManager.getServersDefaultChannel( sd ));
            setPlayersChannel(p, c, false);
             return;
         }
         if ( !c.isDefault() ) {
             return;
         }
         if ( isFactionChannel( c ) && sd.usingFactions() ) {
             return;
         }
         if ( isServerChannel( c ) ) {
         	c = getChannel( sd.getServerName() );
         	setPlayersChannel(p, c, false);
         } else if ( isLocalChannel( c ) ) {
         	c = getChannel( sd.getServerName() + " Local" );
         	setPlayersChannel(p, c, false);
         } else if ( c.getName().equals( "Global" ) ) {
             return;
         } else {
         	c = getChannel( ChatManager.getServersDefaultChannel( sd ) );
         	setPlayersChannel(p, c, false);
             return;
         }
 
 
     }
 
     private static boolean isLocalChannel( Channel c ) {
         if ( c.isDefault() && BungeeSuite.proxy.getServers().containsKey( c.getName().split( " " )[0] ) ) {
             return true;
         }
         return false;
     }
 
     private static boolean isServerChannel( Channel c ) {
         if ( c.isDefault() && BungeeSuite.proxy.getServers().containsKey( c.getName() ) ) {
             return true;
         }
         return false;
     }
 
     public static boolean isFactionChannel( Channel c ) {
         return c.getName().equals( "Faction" ) || c.getName().equals( "FactionAlly" );
     }
 
     public static void checkServerEmpty( String server ) {
         for ( Channel c : channelsSentToServers.get( server ) ) {
             if ( c.isDefault() && c.getMembers().isEmpty() ) {
                 channelsSentToServers.remove( server );
             }
         }
     }
 
     public static void clearServersChannels( Server server ) {
         channelsSentToServers.remove( server.getInfo().getName() );
     }
 
     public static void setPlayerAFK( String player, boolean sendGlobal, boolean hasDisplayPerm ) {
         PlayerManager.setPlayerAFK( player, sendGlobal, hasDisplayPerm );
     }
 
     public static void setChatSpy( String player ) throws SQLException {
         BSPlayer p = PlayerManager.getPlayer( player );
         PlayerManager.setPlayerChatSpy( p );
 
     }
 
     public static void muteAll( String string ) {
         if ( MuteAll ) {
             MuteAll = false;
             PlayerManager.sendBroadcast( Messages.MUTE_ALL_DISABLED.replace( "{sender}", string ) );
         } else {
             MuteAll = true;
             PlayerManager.sendBroadcast( Messages.MUTE_ALL_ENABLED.replace( "{sender}", string ) );
         }
 
     }
 
     public static void nickNamePlayer( String sender, String target, String nickname, boolean on ) throws SQLException {
         BSPlayer s = PlayerManager.getPlayer( sender );
         BSPlayer t;
         nickname = Utilities.colorize( nickname );
         if ( nickname.length() > ChatConfig.nickNameLimit ) {
             s.sendMessage( Messages.NICKNAME_TOO_LONG );
             return;
         }
         if ( !sender.equals( target ) ) {
             if ( !PlayerManager.playerExists( target ) ) {
                 s.sendMessage( Messages.PLAYER_DOES_NOT_EXIST );
                 return;
             }
             t = PlayerManager.getSimilarPlayer( target );
             if ( t != null ) {
                 target = t.getName();
             }
         } else {
             t = s;
         }
         if ( on == false ) {
             PlayerManager.removeNickname( target );
             if ( s.getName().equals( target ) ) {
                 s.sendMessage( Messages.NICKNAME_REMOVED );
             } else {
                 s.sendMessage( Messages.NICKNAME_REMOVED_PLAYER.replace( "{player}", target ) );
                 if ( t != null ) {
                     t.sendMessage( Messages.NICKNAME_REMOVED );
                 }
             }
             return;
         }
         if ( PlayerManager.nickNameExists( nickname ) ) {
             s.sendMessage( Messages.NICKNAME_TAKEN );
             return;
         }
         if ( PlayerManager.playerExists( nickname ) && !t.getName().equals( nickname ) ) {
             s.sendMessage( Messages.NICKNAME_TAKEN );
             return;
         }
         PlayerManager.setPlayersNickname( target, nickname );
         if ( t != null && !t.equals( s ) ) {
             s.sendMessage( Messages.NICKNAMED_PLAYER.replace( "{player}", target ).replace( "{name}", nickname ) );
             if ( t != null ) {
                 t.sendMessage( Messages.NICKNAME_CHANGED.replace( "{name}", nickname ) );
             }
         } else {
             if ( target.equals( s.getName() ) ) {
                 s.sendMessage( Messages.NICKNAME_CHANGED.replace( "{name}", Utilities.colorize( nickname ) ) );
             } else {
                 s.sendMessage( Messages.NICKNAMED_PLAYER.replace( "{name}", Utilities.colorize( nickname ) ).replace( "{player}", target ) );
             }
         }
     }
 
     public static void replyToPlayer( String sender, String message ) {
         BSPlayer p = PlayerManager.getPlayer( sender );
         String reply = p.getReplyPlayer();
         if ( p.isMuted() && ChatConfig.mutePrivateMessages ) {
             p.sendMessage( Messages.MUTED );
             return;
         }
         if ( reply == null ) {
             p.sendMessage( Messages.NO_ONE_TO_REPLY );
             return;
         }
         PlayerManager.sendPrivateMessageToPlayer( p, reply, message );
     }
 
     public static void MutePlayer( String sender, String target, boolean command ) throws SQLException {
         BSPlayer p = PlayerManager.getPlayer( sender );
         if ( !PlayerManager.playerExists( target ) ) {
             p.sendMessage( Messages.PLAYER_DOES_NOT_EXIST );
             return;
         }
         BSPlayer t = PlayerManager.getSimilarPlayer( target );
         if ( t != null ) {
             target = t.getName();
         }
         if ( command ) {
             command = !PlayerManager.isPlayerMuted( target );
         } else {
             if ( !PlayerManager.isPlayerMuted( target ) ) {
                 p.sendMessage( Messages.PLAYER_NOT_MUTE );
                 return;
             }
         }
         PlayerManager.mutePlayer( target );
         if ( command ) {
             p.sendMessage( Messages.PLAYER_MUTED.replace( "{player}", target ) );
             return;
         } else {
             p.sendMessage( Messages.PLAYER_UNMUTED.replace( "{player}", target ) );
         }
 
     }
 
     public static void tempMutePlayer( String sender, String target, int minutes ) throws SQLException {
         BSPlayer p = PlayerManager.getPlayer( sender );
         BSPlayer t = PlayerManager.getSimilarPlayer( target );
         if ( t == null ) {
             p.sendMessage( Messages.PLAYER_NOT_ONLINE );
             return;
         }
         PlayerManager.tempMutePlayer( t, minutes );
         p.sendMessage( Messages.PLAYER_MUTED );
     }
 
     public static void reloadChat( String readUTF ) throws SQLException {
         for ( String s : channelsSentToServers.keySet() ) {
             ServerInfo si = BungeeSuite.proxy.getServerInfo( s );
             if ( si != null ) {
                 ByteArrayOutputStream b = new ByteArrayOutputStream();
                 DataOutputStream out = new DataOutputStream( b );
                 try {
                     out.writeUTF( "ReloadChat" );
                 } catch ( IOException e ) {
                     e.printStackTrace();
                 }
                 sendPluginMessageTaskChat( si, b );
             }
         }
         channels.clear();
         serverData.clear();
         channelsSentToServers.clear();
         PrefixSuffixManager.affixes.clear();
         ChatConfig.reload();
         PrefixSuffixManager.loadPrefixes();
         PrefixSuffixManager.loadSuffixes();
         loadChannels();
         for ( ProxiedPlayer p : BungeeSuite.proxy.getPlayers() ) {
             ChatManager.loadPlayersChannels( p, p.getServer() );
             ChatManager.sendPlayer( p.getName(), p.getServer(), true );
             IgnoresManager.sendPlayersIgnores( PlayerManager.getPlayer( p ), p.getServer() );
         }
     }
 
     public static Channel getPlayersChannel( BSPlayer p ) {
         return getChannel( p.getChannel() );
     }
 
     public static Channel getPlayersNextChannel( BSPlayer p, boolean bypass ) {
         Channel current = p.getPlayersChannel();
         if ( !p.getServerData().forcingChannel() || bypass ) {
             if ( current.getName().equals( "Global" ) ) {
                 return getChannel( p.getServer().getInfo().getName() );
             } else if ( current.getName().equals( p.getServer().getInfo().getName() ) ) {
                 return getChannel( p.getServer().getInfo().getName() + " Local" );
             } else if ( current.getName().equals( p.getServer().getInfo().getName() + " Local" ) ) {
                 if ( usingFactions( p.getServer() ) ) {
                     return getChannel( "Faction" );
                 }
             }
         } else if ( current.getName().equals( p.getServerData().getForcedChannel() ) ) {
             if ( usingFactions( p.getServer() ) ) {
                 return getChannel( "Faction" );
             }
         }
         if ( current.getName().equals( "Faction" ) ) {
             return getChannel( "FactionAlly" );
         }
         boolean found = false;
         Channel chan = null;
         Iterator<Channel> it = p.getPlayersChannels().iterator();
         while ( it.hasNext() ) {
             Channel next = it.next();
             if ( next.equals( current ) ) {
                 found = true;
             }
             if ( found ) {
                 chan = next;
             }
         }
         if ( chan == null ) {
             if ( p.getServerData().forcingChannel() && !bypass ) {
                 String forcedChannel = p.getServerData().getForcedChannel();
                 if ( forcedChannel.equalsIgnoreCase( "server" ) ) {
                     forcedChannel = p.getServerData().getServerName();
                 } else if ( forcedChannel.equalsIgnoreCase( "local" ) ) {
                     forcedChannel = p.getServerData().getServerName() + " Local";
                 }
                 chan = getChannel( forcedChannel );
             } else {
                 chan = getChannel( "Global" );
             }
         }
         return chan;
     }
 
     public static void setPlayersChannel( BSPlayer p, Channel channel, boolean message ) throws SQLException {
         p.setChannel( channel.getName() );
         p.updatePlayer();
         SQLManager.standardQuery( "UPDATE BungeePlayers SET channel ='" + channel.getName() + "' WHERE playername = '" + p.getName() + "'" );
         if(message){
         	p.sendMessage( Messages.CHANNEL_TOGGLE.replace( "{channel}", channel.getName() ) );
         }
     }
 
     public static ServerData getServerData( Server server ) {
         return serverData.get( server.getInfo().getName() );
     }
 
     public static boolean isPlayerChannelMember( BSPlayer p, Channel channel ) {
         return channel.getMembers().contains( p );
     }
 
     public static boolean canPlayerToggleToChannel( BSPlayer p, Channel channel ) {
         if ( channel.isDefault() ) {
             if ( p.getServerData().forcingChannel() ) {
                 String forcedChannel = p.getServerData().getForcedChannel();
                 if ( forcedChannel.equalsIgnoreCase( "local" ) ) {
                     if ( channel.getName().equals( p.getServer().getInfo().getName() + "Local" ) ) {
                         return true;
                     } else {
                         return false;
                     }
                 } else if ( forcedChannel.equalsIgnoreCase( "server" ) ) {
                     if ( channel.getName().equals( p.getServer().getInfo().getName() ) ) {
                         return true;
                     } else {
                         return false;
                     }
                 } else if ( forcedChannel.equalsIgnoreCase( channel.getName() ) ) {
                     return true;
                 } else {
                     return false;
                 }
             }
            return true;
         }
         return true;
     }
 
     public static void togglePlayersChannel( String player, boolean bypass ) throws SQLException {
         BSPlayer p = PlayerManager.getPlayer( player );
         setPlayersChannel( p, getPlayersNextChannel( p, bypass ), true );
     }
 
     public static void togglePlayerToChannel( String sender, String channel, boolean bypass ) throws SQLException {
         BSPlayer p = PlayerManager.getPlayer( sender );
         if ( channel.equals( "Local" ) ) {
             channel = p.getServer().getInfo().getName() + " Local";
         } else if ( channel.equals( "Server" ) ) {
             channel = p.getServer().getInfo().getName();
         }
         Channel c = getSimilarChannel( channel );
         if ( c == null ) {
             p.sendMessage( Messages.CHANNEL_DOES_NOT_EXIST );
             return;
         }
         if ( !bypass ) {
             if ( c.isDefault() || isPlayerChannelMember( p, c ) ) {
 
                 if ( canPlayerToggleToChannel( p, c ) ) {
                     setPlayersChannel( p, c, true);
                     return;
                 } else {
                     p.sendMessage( Messages.CHANNEL_UNTOGGLABLE.replace( "{channel}", c.getName() ) );
                     return;
                 }
             } else {
                 p.sendMessage( Messages.CHANNEL_NOT_A_MEMBER );
                 return;
             }
         } else {
             setPlayersChannel( p, c, true );
         }
 
     }
 
     public static void sendServerData( Server s ) {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "SendServerData" );
             out.writeUTF( serverData.get( s.getInfo().getName() ).serialise() );
         } catch ( IOException e ) {
             e.printStackTrace();
         }
         sendPluginMessageTaskChat( s.getInfo(), b );
     }
 
     public static void sendGlobalChat( String player, String message, Server server ) {
         if ( ChatConfig.logChat ) {
             LoggingManager.log( message );
         }
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "SendGlobalChat" );
             out.writeUTF( player );
             out.writeUTF( message );
         } catch ( IOException e ) {
             e.printStackTrace();
         }
         for ( ServerInfo s : BungeeSuite.proxy.getServers().values() ) {
             if ( !s.getName().equals( server.getInfo().getName() ) && s.getPlayers().size() > 0 ) {
                 sendPluginMessageTaskChat( s, b );
             }
         }
     }
 
     public static void sendAdminChat( String message, Server server ) {
         if ( ChatConfig.logChat ) {
             LoggingManager.log( message );
         }
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "SendAdminChat" );
             out.writeUTF( message );
         } catch ( IOException e ) {
             e.printStackTrace();
         }
         for ( ServerInfo s : BungeeSuite.proxy.getServers().values() ) {
             if ( !s.getName().equals( server.getInfo().getName() ) && s.getPlayers().size() > 0 ) {
                 sendPluginMessageTaskChat( s, b );
             }
         }
     }
 
     public static String getServersDefaultChannel( ServerData server ) {
         String channel = null;
         if ( server.forcingChannel() ) {
             channel = server.getForcedChannel();
         } else {
             channel = ChatConfig.defaultChannel;
         }
         if ( channel.equalsIgnoreCase( "Server" ) ) {
             return server.getServerName();
         } else if ( channel.equalsIgnoreCase( "Local" ) ) {
             return server.getServerName() + " Local";
         } else {
             return channel;
         }
     }
 
     public static void togglePlayersFactionsChannel( String player ) throws SQLException {
         BSPlayer p = PlayerManager.getPlayer( player );
         String channel = p.getChannel();
         String newchannel;
         if ( channel.equals( "Faction" ) ) {
             newchannel = "FactionAlly";
             p.sendMessage( Messages.FACTION_ALLY_TOGGLE );
         } else if ( channel.equals( "FactionAlly" ) ) {
             newchannel = getServersDefaultChannel( p.getServerData() );
             p.sendMessage( Messages.CHANNEL_TOGGLE.replace( "{channel}", newchannel ) );
         } else {
             newchannel = "Faction";
             p.sendMessage( Messages.FACTION_TOGGLE );
         }
         Channel c = getChannel(newchannel);
         setPlayersChannel(p, c, false);
     }
 
     public static void toggleToPlayersFactionChannel( String sender, String channel, boolean hasFaction ) throws SQLException {
         BSPlayer p = PlayerManager.getPlayer( sender );
         if ( !hasFaction ) {
             p.sendMessage( Messages.FACTION_NONE );
             return;
         }
         if ( p.getChannel().equals( channel ) ) {
             channel = getServersDefaultChannel( p.getServerData() );
             p.sendMessage( Messages.FACTION_OFF_TOGGLE );
         } else if ( channel.equals( "Faction" ) ) {
             p.sendMessage( Messages.FACTION_TOGGLE );
         } else {
             p.sendMessage( Messages.FACTION_ALLY_TOGGLE );
         }
         Channel c = getChannel(channel);
         setPlayersChannel(p, c, false);
     }
 
     public static void sendPlayerChannelInformation( String sender, String channel, boolean perm ) {
         Channel c = getSimilarChannel( channel );
         BSPlayer p = PlayerManager.getPlayer( sender );
         if ( c == null ) {
             p.sendMessage( Messages.CHANNEL_DOES_NOT_EXIST );
             return;
         }
         p.sendMessage( ChatColor.DARK_AQUA + "---------" + ChatColor.GOLD + "Channel Info" + ChatColor.DARK_AQUA + "---------" );
         p.sendMessage( " " );
         p.sendMessage( ChatColor.GOLD + "Channel name: " + ChatColor.AQUA + c.getName() );
         if ( !c.isDefault() ) {
             p.sendMessage( ChatColor.GOLD + "Channel status: " + ChatColor.AQUA + c.getStatus() );
         }
         if ( !c.isDefault() ) {
             p.sendMessage( ChatColor.GOLD + "Channel owner: " + ChatColor.AQUA + c.getOwner() );
             ArrayList<BSPlayer> members = c.getMembers();
             String players = ChatColor.GOLD + "Members: " + ChatColor.AQUA + "";
             for ( int i = 0; i < members.size() && i < 10; i++ ) {
                 players += members.get( i ) + ", ";
             }
             players = players.substring( 0, players.length() - 2 );
             if ( members.size() >= 10 ) {
                 players += "...";
             }
             p.sendMessage( players );
             if ( p.getName().equals( c.getOwner() ) || perm ) {
                 p.sendMessage( ChatColor.GOLD + "Format: " + ChatColor.AQUA + c.format() );
             }
         } else {
             p.sendMessage( ChatColor.GOLD + "Channel type: " + ChatColor.AQUA + "Server" );
             if ( perm ) {
                 p.sendMessage( ChatColor.GOLD + "Format: " + ChatColor.AQUA + c.format() );
             }
         }
     }
 
     public static void setChannelsFormat( String readUTF, String readUTF2, boolean readBoolean ) {
         // TODO Auto-generated method stub
         //update channel
         //sql update
         //resend to servers
 
     }
 }
