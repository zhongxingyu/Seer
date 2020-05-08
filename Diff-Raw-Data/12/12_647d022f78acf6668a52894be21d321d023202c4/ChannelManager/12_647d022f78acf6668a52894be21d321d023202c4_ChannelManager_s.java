 package com.minecraftdimensions.bungeesuitechat.managers;
 
 import com.massivecraft.factions.Rel;
 import com.massivecraft.factions.entity.Faction;
 import com.massivecraft.factions.entity.FactionColls;
 import com.massivecraft.factions.entity.UPlayer;
 import com.minecraftdimensions.bungeesuitechat.BungeeSuiteChat;
 import com.minecraftdimensions.bungeesuitechat.objects.BSPlayer;
 import com.minecraftdimensions.bungeesuitechat.objects.Channel;
 import com.minecraftdimensions.bungeesuitechat.objects.ServerData;
 import com.minecraftdimensions.bungeesuitechat.tasks.PluginMessageTask;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.*;
 
 
 public class ChannelManager {
     public static boolean receivedChannels = false;
     private static ArrayList<Channel> channels = new ArrayList<>();
 
     public static void addChannel( String channel ) {
         Channel c = new Channel( channel );
         if ( channelExists( c.getName() ) ) {
             removeChannel( c.getName() );
         }
         channels.add( c );
        if ( !receivedChannels ) {
            receivedChannels = true;
        }
     }
 
     private static void removeChannel( String name ) {
         Iterator<Channel> it = channels.iterator();
         while ( it.hasNext() ) {
             Channel c = it.next();
             if ( c.getName().equals( name ) ) {
                 it.remove();
                 return;
             }
         }
     }
 
     public static ArrayList<Channel> getDefaultChannels() {
         ArrayList<Channel> chan = new ArrayList<>();
         for ( Channel c : channels ) {
             if ( c.isDefault ) {
                 chan.add( c );
             }
         }
         return chan;
     }
 
     public static Channel getChannel( String channel ) {
         for ( Channel c : channels ) {
             if ( c.getName().
                     equals( channel ) ) {
                 return c;
             }
         }
         return null;
     }
 
     public static boolean channelExists( String channel ) {
         for ( Channel c : channels ) {
             if ( c.getName().equals( channel ) ) {
                 return true;
             }
         }
         return false;
     }
 
     public static void requestChannels() {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "GetServerChannels" );
         } catch ( IOException s ) {
             s.printStackTrace();
         }
         new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteChat.instance );
         System.out.println( "Getting default channels" );
     }
 
     public static void cleanChannels() {
         ArrayList<Channel> chans = new ArrayList<>();
         chans.addAll( getDefaultChannels() );
         for ( BSPlayer p : PlayerManager.getOnlinePlayers() ) {
             Channel pc = p.getChannel();
             if ( !chans.contains( pc ) ) {
                 chans.add( pc );
             }
         }
         channels = chans;
     }
 
     public static boolean isLocal( Channel channel ) {
         return channel.getName().equals( ServerData.getServerName() + " Local" );
     }
 
     public static boolean isServer( Channel channel ) {
         return channel.getName().equals( ServerData.getServerName() );
     }
 
     public static boolean isGlobal( Channel channel ) {
         return channel.getName().equals( "Global" );
     }
 
     public static boolean isAdmin( Channel channel ) {
         return channel.getName().equals( "Admin" );
     }
 
     public static boolean isFactionChannel( Channel channel ) {
         return channel.getName().equals( "Faction" ) || channel.getName().equals( "FactionAlly" );
     }
 
     public static boolean isFaction( Channel channel ) {
         return channel.getName().equals( "Faction" );
     }
 
     public static boolean isFactionAlly( Channel channel ) {
         return channel.getName().equals( "FactionAlly" );
     }
 
     public static Collection<Player> getNonLocal( Player player ) {
         Collection<Player> nonLocals = new ArrayList<>();
         for ( Player p : Bukkit.getOnlinePlayers() ) {
             if ( p.getLocation().distance( player.getLocation() ) > ServerData.getLocalDistance() ) {
                 nonLocals.add( p );
             } else if ( !p.hasPermission( "bungeesuite.chat.channel.local" ) ) {
                 nonLocals.add( p );
             }
         }
         return nonLocals;
     }
 
     public static Collection<Player> getServerPlayers() {
         Collection<Player> serverPlayers = new ArrayList<>();
         for ( Player p : Bukkit.getOnlinePlayers() ) {
             if ( p.hasPermission( "bungeesuite.chat.channel.server" ) ) {
                 serverPlayers.add( p );
             }
         }
         return serverPlayers;
     }
 
     public static Collection<Player> getGlobalPlayers() {
         Collection<Player> globalPlayers = new ArrayList<>();
         for ( Player p : Bukkit.getOnlinePlayers() ) {
             if ( p.hasPermission( "bungeesuite.chat.channel.global" ) ) {
                 globalPlayers.add( p );
             }
         }
         return globalPlayers;
     }
 
     public static Collection<Player> getFactionPlayers( Player p ) {
         Collection<Player> factionPlayers = new ArrayList<>();
         UPlayer uplayer = UPlayer.get( p );
         for ( Player ps : uplayer.getFaction().getOnlinePlayers() ) {
             if ( ps.hasPermission( "bungeesuite.chat.channel.faction" ) ) {
                 factionPlayers.add( p );
             }
         }
         return factionPlayers;
     }
 
     public static Collection<Player> getFactionAllyPlayers( Player p ) {
         Collection<Player> factionPlayers = new ArrayList<>();
         UPlayer uplayer = UPlayer.get( p );
         Map<Rel, List<String>> rels = uplayer.getFaction().getFactionNamesPerRelation( uplayer.getFaction() );
         for ( Player ps : uplayer.getFaction().getOnlinePlayers() ) {
             if ( ps.hasPermission( "bungeesuite.chat.channel.factionally" ) ) {
                 factionPlayers.add( ps );
             }
         }
         for ( String data : rels.get( Rel.ALLY ) ) {
             Faction f = FactionColls.get().getForUniverse( uplayer.getFaction().getUniverse() ).getByName( ChatColor.stripColor( data ) );
             for ( Player ps : f.getOnlinePlayers() ) {
                 if ( ps.hasPermission( "bungeesuite.chat.channel.factionally" ) ) {
                     factionPlayers.add( ps );
                 }
             }
         }
         return factionPlayers;
     }
 
     public static Collection<BSPlayer> getBSGlobalPlayers() {
         Collection<BSPlayer> globalPlayers = new ArrayList<>();
         for ( Player p : Bukkit.getOnlinePlayers() ) {
             if ( p.hasPermission( "bungeesuite.chat.channel.global" ) ) {
                 globalPlayers.add( PlayerManager.getPlayer( p ) );
             }
         }
         return globalPlayers;
     }
 
     public static Collection<Player> getAdminPlayers() {
         Collection<Player> serverPlayers = new ArrayList<>();
         for ( Player p : Bukkit.getOnlinePlayers() ) {
             if ( p.hasPermission( "bungeesuite.chat.channel.admin" ) ) {
                 serverPlayers.add( p );
             }
         }
         return serverPlayers;
     }
 
     public static Collection<BSPlayer> getBSAdminPlayers() {
         Collection<BSPlayer> serverPlayers = new ArrayList<>();
         for ( Player p : Bukkit.getOnlinePlayers() ) {
             if ( p.hasPermission( "bungeesuite.chat.channel.admin" ) ) {
                 serverPlayers.add( PlayerManager.getPlayer( p ) );
             }
         }
         return serverPlayers;
     }
 
     public static Collection<Player> getIgnores( Player player ) {
         Collection<Player> ignoringPlayers = new ArrayList<>();
         for ( BSPlayer p : PlayerManager.getOnlinePlayers() ) {
             if ( p.ignoringPlayer( player.getName() ) ) {
                 ignoringPlayers.add( p.getPlayer() );
             }
         }
         return ignoringPlayers;
     }
 
     public static Collection<BSPlayer> getBSIgnores( String player ) {
         Collection<BSPlayer> ignoringPlayers = new ArrayList<>();
         for ( BSPlayer p : PlayerManager.getOnlinePlayers() ) {
             if ( p.ignoringPlayer( player ) ) {
                 ignoringPlayers.add( p );
             }
         }
         return ignoringPlayers;
     }
 
     public static void reload() {
         receivedChannels = false;
         channels.clear();
         PlayerManager.reload();
         PrefixSuffixManager.reload();
         getDefaultChannels();
     }
 
     public static void togglePlayersChannel( CommandSender sender ) {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "TogglePlayersChannel" );
             out.writeUTF( sender.getName() );
             out.writeBoolean( sender.hasPermission( "bungeesuite.chat.toggle.bypass" ) );
         } catch ( IOException s ) {
             s.printStackTrace();
         }
         new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteChat.instance );
 
     }
 
     public static void togglePlayerToChannel( CommandSender sender, String channel ) {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "TogglePlayerToChannel" );
             out.writeUTF( sender.getName() );
             out.writeUTF( channel );
             out.writeBoolean( sender.hasPermission( "bungeesuite.chat.toggle.bypass" ) );
         } catch ( IOException s ) {
             s.printStackTrace();
         }
         new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteChat.instance );
     }
 
     public static void sendGlobalChat( String name, String message ) {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "GlobalChat" );
             out.writeUTF( name );
             out.writeUTF( message );
         } catch ( IOException s ) {
             s.printStackTrace();
         }
         new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteChat.instance );
 
     }
 
     public static void getGlobalChat( String player, String message ) {
         Collection<BSPlayer> recipients = new ArrayList<BSPlayer>();
         recipients.addAll( ChannelManager.getBSGlobalPlayers() );
         recipients.removeAll( getBSIgnores( player ) );
         for ( BSPlayer p : recipients ) {
             p.sendMessage( message );
         }
     }
     //
     //	public static void sendChannelMessage(CommandSender sender, String channel, String message) {
     //
     //	}
 
     public static void sendAdminChat( String message ) {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "AdminChat" );
             out.writeUTF( message );
         } catch ( IOException s ) {
             s.printStackTrace();
         }
         new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteChat.instance );
     }
 
     public static void getAdminlChat( String message ) {
         Collection<BSPlayer> recipients = new ArrayList<>();
         recipients.addAll( ChannelManager.getBSAdminPlayers() );
         for ( BSPlayer p : recipients ) {
             p.sendMessage( message );
         }
     }
 
     public static void togglePlayersFactionChannels( CommandSender sender ) {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "TogglePlayersFactionsChannel" );
             out.writeUTF( sender.getName() );
         } catch ( IOException s ) {
             s.printStackTrace();
         }
         new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteChat.instance );
     }
 
     public static boolean playerHasPermissionToTalk( BSPlayer p ) {
         Channel c = p.getChannel();
         if ( c.isDefault )
             if ( ChannelManager.isGlobal( c ) ) {
                 return p.getPlayer().hasPermission( "bungeesuite.chat.channel.global" );
             } else if ( ChannelManager.isServer( c ) ) {
                 return p.getPlayer().hasPermission( "bungeesuite.chat.channel.server" );
             } else if ( ChannelManager.isLocal( c ) ) {
                 return p.getPlayer().hasPermission( "bungeesuite.chat.channel.local" );
             } else {
                 return ChannelManager.isAdmin( c ) && p.getPlayer().hasPermission( "bungeesuite.chat.channel.admin" );
             }
         else
             return p.getPlayer().hasPermission( "bungeesuite.chat.channel.custom" );
 
     }
 
     public static void toggleToPlayersFactionChannel( CommandSender sender, String channel ) {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         UPlayer uplayer = UPlayer.get( sender );
         try {
             out.writeUTF( "ToggleToPlayersFactionChannel" );
             out.writeUTF( sender.getName() );
             out.writeUTF( channel );
             out.writeBoolean( !uplayer.getFaction().isDefault() );
         } catch ( IOException s ) {
             s.printStackTrace();
         }
         new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteChat.instance );
 
     }
 
     public static void getChannelInfo( CommandSender sender, String channel ) {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "GetChannelInfo" );
             out.writeUTF( sender.getName() );
             out.writeUTF( channel );
             out.writeBoolean( sender.hasPermission( "bungeesuite.chat.command.channelinfo.format" ) );
         } catch ( IOException s ) {
             s.printStackTrace();
         }
         new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteChat.instance );
     }
 
     public static void setChannelFormat( CommandSender sender, String channel ) {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "SetChannelFormat" );
             out.writeUTF( sender.getName() );
             out.writeUTF( channel );
             out.writeBoolean( sender.hasPermission( "bungeesuite.chat.command.setformat.bypass" ) );
         } catch ( IOException s ) {
             s.printStackTrace();
         }
         new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuiteChat.instance );
 
     }
 
 
 }
