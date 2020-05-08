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
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class ChatManager {
 
     public static ArrayList<Channel> channels = new ArrayList();
     public static HashMap<String, ServerData> serverData = new HashMap();
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
         //Load Towny Channels
         loadChannel( server, "Town", chan.getString( "Channels.TownyTown", Messages.CHANNEL_DEFAULT_TOWN ), true, true );
         loadChannel( server, "Nation", chan.getString( "Channels.TownyNation", Messages.CHANNEL_DEFAULT_NATION ), true, true );
         //Load Server Channels
         for ( String servername : ProxyServer.getInstance().getServers().keySet() ) {
             loadChannel( server, servername, chan.getString( "Channels.Servers." + servername + ".Server", Messages.CHANNEL_DEFAULT_SERVER ), true, true );
             loadChannel( server, servername + " Local", chan.getString( "Channels.Servers." + servername + ".Local", Messages.CHANNEL_DEFAULT_LOCAL ), true, true );
             loadServerData( servername, chan.getString( "Channels.Servers." + servername + ".Shortname", servername.substring( 0, 1 ) ), chan.getBoolean( "Channels.Servers." + servername + ".ForceChannel", false ), chan.getString( "Channels.Servers." + servername + ".ForcedChannel", "Server" ), chan.getInt( "Channels.Servers." + servername + ".LocalRange", 50 ), chan.getBoolean( "Channels.Servers." + servername + ".DisableConnectionMessages", true ) );
         }
         //Load custom channels from db
 
         LoggingManager.log( ChatColor.GOLD + "Channels loaded - " + ChatColor.DARK_GREEN + channels.size() );
     }
 
     private static void loadServerData( String name, String shortName, boolean forcingChannel, String forcedChannel, int localDistance, boolean connectionMessages ) {
         ServerData d = new ServerData( name, shortName, forcingChannel, forcedChannel, localDistance, connectionMessages );
         if ( serverData.get( d ) == null ) {
             serverData.put( name, d );
         }
     }
 
     public static void loadChannel( String owner, String name, String format, boolean isDefault, boolean open ) {
         Channel c = new Channel( name, format, owner, false, isDefault, open );
         channels.add( c );
     }
 
     //    public static boolean usingFactions( Server server ) {
     //        return serverData.get( server.getInfo().getName() ).usingFactions();
     //    }
     //
     //    public static boolean usingTowny( Server server ) {
     //        return serverData.get( server.getInfo().getName() ).usingTowny();
     //    }
 
     public static void sendDefaultChannelsToServer( ServerInfo s ) {
         ArrayList<Channel> chans = getDefaultChannels( s.getName() );
         for ( Channel c : chans ) {
             sendChannelToServer( s, c );
         }
     }
 
     public static void sendFactionChannelsToServer( ServerInfo s ) {
         serverData.get( s.getName() ).useFactions();
         sendChannelToServer( s, getChannel( "Faction" ) );
         sendChannelToServer( s, getChannel( "FactionAlly" ) );
     }
 
     public static void sendTownyChannelsToServer( ServerInfo s ) {
         serverData.get( s.getName() ).useTowny();
         sendChannelToServer( s, getChannel( "Town" ) );
         sendChannelToServer( s, getChannel( "Nation" ) );
     }
 
     public static void checkForPlugins( ServerInfo server ) {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "PluginCheck" );
         } catch ( IOException e ) {
             e.printStackTrace();
         }
         sendPluginMessageTaskChat( server, b );
     }
 
     public static void sendChannelToServer( ServerInfo server, Channel channel ) {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "SendChannel" );
             out.writeUTF( channel.serialise() );
         } catch ( IOException e ) {
             e.printStackTrace();
         }
         sendPluginMessageTaskChat( server, b );
     }
 
     public static ArrayList<Channel> getDefaultChannels( String server ) {
         ArrayList<Channel> chans = new ArrayList();
         for ( Channel c : channels ) {
             if ( c.getName().equals( "Global" ) || c.getName().equals( "Admin" ) || c.getName().equals( server ) || c.getName().equals( server + " Local" ) ) {
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
 
     //getPlayersChannels    public static void loadPlayersChannels( ProxiedPlayer player, Server server ) throws SQLException {
     //        ResultSet res = SQLManager.sqlQuery( "SELECT channel FROM BungeeChannelMembers WHERE player = '" + player.getName() + "'" );
     //        while ( res.next() ) {
     //            getChannel( res.getString( "channel" ) ).addMember( player.getName() );
     //        }
     //        res.close();
     //        sendPlayersChannels( PlayerManager.getPlayer( player ), server );
     //    }
 
     //    private static void sendPlayersChannels( BSPlayer p, Server server ) {
     //        for ( Channel c : p.getPlayersChannels() ) {
     //            if ( !channelsSentToServers.get( server.getInfo().getName() ).contains( c ) ) {
     //                sendChannelToServer( server, c );
     //            }
     //       }
     //    }
 
 
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
             c = getChannel( sd.getForcedChannel() );
             setPlayersChannel( p, c, false );
             return;
         }
         if ( c == null ) {
             c = getChannel( ChatManager.getServersDefaultChannel( sd ) );
             setPlayersChannel( p, c, false );
             return;
         }
         if ( !c.isDefault() ) {
             return;
         }
         if ( isFactionChannel( c ) && sd.usingFactions() ) {
             return;
         }
         if ( isTownyChannel( c ) && sd.usingTowny() ) {
             return;
         }
 
         if ( isServerChannel( c ) ) {
             c = getChannel( sd.getServerName() );
             setPlayersChannel( p, c, false );
         } else if ( isLocalChannel( c ) ) {
             c = getChannel( sd.getServerName() + " Local" );
             setPlayersChannel( p, c, false );
         } else if ( c.getName().equals( "Global" ) ) {
             return;
         } else {
             c = getChannel( ChatManager.getServersDefaultChannel( sd ) );
             setPlayersChannel( p, c, false );
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
 
     public static boolean isTownyChannel( Channel c ) {
         return c.getName().equals( "Town" ) || c.getName().equals( "Nation" );
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
        p.sendMessage( Messages.PLAYER_MUTED.replace( "{player}", t.getDisplayingName() ) );
     }
 
     public static void reloadChat( String readUTF ) throws SQLException, IOException {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "Reload" );
         } catch ( IOException e ) {
             e.printStackTrace();
         }
         for ( ServerInfo s : BungeeSuite.proxy.getServers().values() ) {
             sendPluginMessageTaskChat( s, b );
         }
         channels.clear();
         serverData.clear();
         PrefixSuffixManager.affixes.clear();
         ChatConfig.reload();
         Channels.reload();
         PrefixSuffixManager.loadPrefixes();
         PrefixSuffixManager.loadSuffixes();
         loadChannels();
         for ( ServerInfo s : BungeeSuite.proxy.getServers().values() ) {
             ChatManager.sendServerData( s );
             ChatManager.sendDefaultChannelsToServer( s );
             PrefixSuffixManager.sendPrefixAndSuffixToServer( s );
         }
         for ( ProxiedPlayer p : BungeeSuite.proxy.getPlayers() ) {
             sendPlayer( p.getName(), p.getServer(), true );
         }
     }
 
     public static Channel getPlayersChannel( BSPlayer p ) {
         return getChannel( p.getChannel() );
     }
 
     public static Channel getPlayersNextChannel( BSPlayer p, boolean factionAccess, boolean townyAccess, boolean inNation, boolean bypass ) {
         Channel current = p.getPlayersChannel();
         String c = current.getName();
         ServerData sd = p.getServerData();
         if ( !bypass && sd.forcingChannel() ) {
             if ( sd.usingFactions() && factionAccess ) {
                 if ( c.equals( sd.getForcedChannel() ) ) {
                     return getChannel( "Faction" );
                 }
                 if ( c.equals( "Faction" ) ) {
                     return getChannel( "FactionAlly" );
                 }
             }
             if ( sd.usingTowny() && townyAccess ) {
                 if ( c.equals( sd.getForcedChannel() ) || c.equals( "FactionAlly" ) ) {
                     return getChannel( "Town" );
                 }
                 if ( c.equals( "Town" ) && inNation ) {
                     return getChannel( "Nation" );
                 }
             }
             return getChannel( sd.getForcedChannel() );
         }
         if ( sd.usingFactions() && factionAccess ) {
             if ( c.equals( p.getServerData().getServerName() + " Local" ) ) {
                 return getChannel( "Faction" );
             }
             if ( c.equals( "Faction" ) ) {
                 return getChannel( "FactionAlly" );
             }
         }
         if ( sd.usingTowny() && townyAccess ) {
             if ( c.equals( p.getServerData().getServerName() + " Local" ) || c.equals( "FactionAlly" ) ) {
                 return getChannel( "Town" );
             }
             if ( c.equals( "Town" ) && inNation ) {
                 return getChannel( "Nation" );
             }
         }
         if ( c.equals( "Global" ) ) {
             return getChannel( p.getServerData().getServerName() );
         }
         if ( c.equals( p.getServer().getInfo().getName() ) ) {
             return getChannel( p.getServerData().getServerName() + " Local" );
         }
         return getChannel( "Global" );
     }
 
     public static void setPlayersChannel( BSPlayer p, Channel channel, boolean message ) throws SQLException {
         p.setChannel( channel.getName() );
         p.updatePlayer();
         SQLManager.standardQuery( "UPDATE BungeePlayers SET channel ='" + channel.getName() + "' WHERE playername = '" + p.getName() + "'" );
         if ( message ) {
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
                 if ( channel.getName().equals( forcedChannel ) ) {
                     return true;
                 } else {
                     return false;
                 }
             }
             return true;
         }
         return true;
     }
 
     public static void togglePlayersChannel( String player, boolean factionAccess, boolean townyAccess, boolean inNation, boolean bypass ) throws SQLException {
         BSPlayer p = PlayerManager.getPlayer( player );
         setPlayersChannel( p, getPlayersNextChannel( p, factionAccess, townyAccess, inNation, bypass ), true );
     }
 
     public static void togglePlayerToChannel( String sender, String channel, boolean factionAccess, boolean townyAccess, boolean inNation, boolean bypass ) throws SQLException {
         BSPlayer p = PlayerManager.getPlayer( sender );
         if ( channel.equalsIgnoreCase( "Local" ) ) {
             channel = p.getServer().getInfo().getName() + " Local";
         } else if ( channel.equalsIgnoreCase( "Server" ) ) {
             channel = p.getServer().getInfo().getName();
         } else if ( channel.equalsIgnoreCase( "Global" ) ) {
             channel = "Global";
         }
         Channel c = getSimilarChannel( channel );
         if ( c == null ) {
             p.sendMessage( Messages.CHANNEL_DOES_NOT_EXIST );
             return;
         }
         if ( isFactionChannel( c ) && !factionAccess ) {
             p.sendMessage( Messages.CHANNEL_UNTOGGLABLE.replace( "{channel}", c.getName() ) );
             return;
         }
         if ( isTownyChannel( c ) && !townyAccess || ( c.getName().equals( "Nation" ) && !inNation ) ) {
             p.sendMessage( Messages.CHANNEL_UNTOGGLABLE.replace( "{channel}", c.getName() ) );
             return;
         }
         if ( !bypass ) {
             if ( c.isDefault() || isPlayerChannelMember( p, c ) ) {
                 if ( canPlayerToggleToChannel( p, c ) ) {
                     setPlayersChannel( p, c, true );
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
 
     public static void sendServerData( ServerInfo s ) {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "SendServerData" );
             ServerData sd = serverData.get( s.getName() );
             out.writeUTF( sd.getServerName() );
             out.writeUTF( sd.getServerShortName() );
             out.writeInt( sd.getLocalDistance() );
             out.writeBoolean( sd.usingConnectionMessages() );
             out.writeUTF( ChatConfig.globalChatRegex );
         } catch ( IOException e ) {
             e.printStackTrace();
         }
         sendPluginMessageTaskChat( s, b );
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
         return server.getForcedChannel();
     }
 
     public static void togglePlayersFactionsChannel( String player, Boolean inFaction ) throws SQLException {
         BSPlayer p = PlayerManager.getPlayer( player );
         String channel = p.getChannel();
         String newchannel;
         if ( !inFaction ) {
             p.sendMessage( Messages.FACTION_NONE );
             return;
         }
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
         Channel c = getChannel( newchannel );
         setPlayersChannel( p, c, false );
     }
 
     public static void togglePlayersTownyChannel( String player, Boolean inTown, Boolean inNation ) throws SQLException {
         BSPlayer p = PlayerManager.getPlayer( player );
         String channel = p.getChannel();
         String newchannel;
         if ( !inTown ) {
             p.sendMessage( Messages.TOWNY_NONE );
             return;
         }
         if ( channel.equals( "Town" ) && inNation ) {
             newchannel = "Nation";
             p.sendMessage( Messages.TOWNY_NATION_TOGGLE );
         } else if ( channel.equals( "Nation" ) || channel.equals( "Town" ) ) {
             newchannel = getServersDefaultChannel( p.getServerData() );
             p.sendMessage( Messages.CHANNEL_TOGGLE.replace( "{channel}", newchannel ) );
         } else {
             newchannel = "Town";
             p.sendMessage( Messages.TOWNY_TOGGLE );
         }
         Channel c = getChannel( newchannel );
         setPlayersChannel( p, c, false );
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
         Channel c = getChannel( channel );
         setPlayersChannel( p, c, false );
     }
 
     public static void toggleToPlayersTownyChannel( String sender, String channel, boolean hasTown, Boolean hasNation ) throws SQLException {
         BSPlayer p = PlayerManager.getPlayer( sender );
         if ( !hasTown ) {
             p.sendMessage( Messages.TOWNY_NONE );
             return;
         }
         if ( p.getChannel().equals( channel ) ) {
             channel = getServersDefaultChannel( p.getServerData() );
             p.sendMessage( Messages.TOWNY_OFF_TOGGLE );
         } else if ( channel.equals( "Town" ) ) {
             p.sendMessage( Messages.TOWNY_TOGGLE );
         } else if ( channel.equals( "Nation" ) ) {
             if ( hasNation ) {
                 p.sendMessage( Messages.TOWNY_NATION_TOGGLE );
             } else {
                 p.sendMessage( Messages.TOWNY_NATION_NONE );
                 return;
             }
         }
         Channel c = getChannel( channel );
         setPlayersChannel( p, c, false );
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
