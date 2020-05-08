 package com.minecraftdimensions.bungeesuite.objects;
 
 import com.minecraftdimensions.bungeesuite.configs.ChatConfig;
 import com.minecraftdimensions.bungeesuite.managers.ChatManager;
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.config.ServerInfo;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.connection.Server;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 
 public class BSPlayer {
     private String playername;
     private String channel;
     private boolean muted;
     private String nickname = null;
     private String tempname;
     private boolean chatspying;
     private boolean dnd;
     private boolean afk;
     private boolean acceptingTeleports;
     private ArrayList<String> ignores = new ArrayList<>();
     private ArrayList<Channel> channels = new ArrayList<>();
     private HashMap<String, ArrayList<Home>> homes = new HashMap<>();
     private Location deathBackLocation;
     private Location teleportBackLocation;
     private boolean lastBack; //true = death false = teleport
     private String replyPlayer;
     private boolean firstConnect = true;
 
     public BSPlayer( String name, String nickname, String channel, boolean muted, boolean chatspying, boolean dnd, boolean tps ) {
         this.playername = name;
         this.nickname = nickname;
         this.channel = channel;
         this.muted = muted;
         this.chatspying = chatspying;
         this.dnd = dnd;
         this.acceptingTeleports = tps;
     }
 
     public String getName() {
         return playername;
     }
 
     public void setPlayerName( String name ) {
         this.playername = name;
     }
 
     public ProxiedPlayer getProxiedPlayer() {
         return ProxyServer.getInstance().getPlayer( playername );
     }
 
     public void sendMessage( String message ) {
         for ( String line : message.split( "\n" ) ) {
             getProxiedPlayer().sendMessage( line );
         }
     }
 
     public String getChannel() {
         return channel;
     }
 
     public void setChannel( String channel ) {
         this.channel = channel;
     }
 
     public boolean isMuted() {
         return muted;
     }
 
     public void setMute( boolean mute ) {
         this.muted = mute;
         updatePlayer();
     }
 
     public boolean hasNickname() {
         return nickname != null;
     }
 
     public String getNickname() {
         if ( nickname == null ) {
             return "";
         }
         return nickname;
     }
 
     public void setNickname( String nick ) {
         this.nickname = nick;
     }
 
     public boolean isChatSpying() {
         return chatspying;
     }
 
     public void setChatSpying( boolean spy ) {
         this.chatspying = spy;
         updatePlayer();
     }
 
     public boolean isDND() {
         return dnd;
     }
 
     public void setDND( boolean dnd ) {
         this.dnd = dnd;
     }
 
     public boolean acceptingTeleports() {
         return this.acceptingTeleports;
     }
 
     public void setAcceptingTeleports( boolean tp ) {
         this.acceptingTeleports = tp;
     }
 
     public void addIgnore( String player ) {
         this.ignores.add( player );
     }
 
     public void removeIgnore( String player ) {
         this.ignores.remove( player );
     }
 
     public boolean ignoringPlayer( String player ) {
         return ignores.contains( player );
     }
 
     public void joinChannel( Channel channel ) {
         this.channels.add( channel );
     }
 
     public void leaveChannel( Channel channel ) {
         this.channels.remove( channel );
     }
 
     public boolean isInChannel( Channel channel ) {
         return this.channels.contains( channel );
     }
 
     public void joinChannel( String channel ) {
         this.channels.add( ChatManager.getChannel( channel ) );
     }
 
     public void leaveChannel( String channel ) {
         this.channels.remove( ChatManager.getChannel( channel ) );
     }
 
     public boolean isInChannel( String channel ) {
         return this.channels.contains( this.channels.add( ChatManager.getChannel( channel ) ) );
     }
 
     public Channel getPlayersChannel() {
         return ChatManager.getChannel( channel );
     }
 
     public ArrayList<Channel> getPlayersChannels() {
         return channels;
     }
 
     public Channel getPlayersSimilarChannel( String channel ) {
         for ( Channel chan : channels ) {
             if ( chan.getName().contains( channel ) ) {
                 return chan;
             }
         }
         return null;
     }
 
     public void setDeathBackLocation( Location loc ) {
         deathBackLocation = loc;
         lastBack = true;
     }
 
     public boolean hasDeathBackLocation() {
         return deathBackLocation != null;
     }
 
     public void setTeleportBackLocation( Location loc ) {
         teleportBackLocation = loc;
         lastBack = false;
     }
 
     public Location getLastBackLocation() {
         if ( lastBack ) {
             return deathBackLocation;
         } else {
             return teleportBackLocation;
         }
     }
 
     public ServerData getServerData() {
         return ChatManager.getServerData( getServer() );
     }
 
     public boolean hasTeleportBackLocation() {
         return teleportBackLocation != null;
     }
 
     public Location getDeathBackLocation() {
         return deathBackLocation;
     }
 
     public Location getTeleportBackLocation() {
         return teleportBackLocation;
     }
 
     public boolean hasReply() {
         return replyPlayer != null;
     }
 
     public String getReplyPlayer() {
         return replyPlayer;
     }
 
     public boolean isAFK() {
         return afk;
     }
 
     public void setAFK( boolean afk ) {
         this.afk = afk;
     }
 
     public void updateDisplayName() {
         String name = getDisplayingName();
         if ( name.length() > 16 ) {
             name = getDisplayingName().substring( 0, 16 );
         }
         if ( ChatConfig.updateNicknamesOnTab ) {
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer( playername );
            if ( p != null && name != null ) {
                p.setDisplayName( name );
            }
         }
     }
 
     public String getDisplayingName() {
         if ( tempname != null ) {
             return tempname;
         } else if ( nickname != null ) {
             return nickname;
         } else {
             return playername;
         }
     }
 
     public void setTempName( String name ) {
         tempname = name;
         updatePlayer();
         updateDisplayName();
     }
 
     public void revertName() {
         tempname = null;
         updatePlayer();
         updateDisplayName();
     }
 
     public void updatePlayer() {
         try {
            ChatManager.sendPlayer( playername, getServer(), false );
        } catch ( SQLException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
     }
 
     public void sendMessageToPlayer( BSPlayer target, String message ) {
         target.sendMessage( Messages.PRIVATE_MESSAGE_RECEIVE.replace( "{player}", getDisplayingName() ).replace( "{message}", message ) );
     }
 
     public void sendToServer( String targetName ) {
         getProxiedPlayer().connect( ProxyServer.getInstance().getServerInfo( targetName ) );
     }
 
     public Server getServer() {
         return ProxyServer.getInstance().getPlayer( playername ).getServer();
     }
 
     public boolean isIgnoring( String ignore ) {
         return ignores.contains( ignore );
     }
 
     public ArrayList<String> getIgnores() {
         return ignores;
     }
 
     public String getTempName() {
         if ( tempname == null ) {
             return "";
         }
         return tempname;
     }
 
     public boolean hasIgnores() {
         return !ignores.isEmpty();
     }
 
     public void setReplyPlayer( String name ) {
         replyPlayer = name;
     }
 
     public HashMap<String, ArrayList<Home>> getHomes() {
         return homes;
     }
 
     public boolean firstConnect() {
         return firstConnect;
     }
 
     public void connected() {
         firstConnect = false;
     }
 
     public void connectTo( ServerInfo s ) {
         getProxiedPlayer().connect( s );
     }
 }
