 package com.minecraftdimensions.bungeesuite.managers;
 
 import com.minecraftdimensions.bungeesuite.BungeeSuite;
 import com.minecraftdimensions.bungeesuite.objects.BSPlayer;
 import com.minecraftdimensions.bungeesuite.objects.Home;
 import com.minecraftdimensions.bungeesuite.objects.Location;
 import com.minecraftdimensions.bungeesuite.objects.Messages;
 import com.minecraftdimensions.bungeesuite.tasks.SendPluginMessage;
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.config.ServerInfo;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 public class HomesManager {
 
     public static String OUTGOING_CHANNEL = "BungeeSuiteHomes";
 
 
     public static void createNewHome( String player, int serverLimit, int globalLimit, String home, Location loc ) throws SQLException {
         BSPlayer p = PlayerManager.getPlayer( player );
 
         if ( getSimilarHome( p, home ) == null ) {
             int globalHomeCount = getPlayersGlobalHomeCount( p );
             int serverHomeCount = getPlayersServerHomeCount( p );
             if ( globalHomeCount >= globalLimit ) {
                 p.sendMessage( Messages.NO_HOMES_ALLOWED_GLOBAL );
                 return;
             }
             if ( serverHomeCount >= serverLimit ) {
                 p.sendMessage( Messages.NO_HOMES_ALLOWED_SERVER );
                 return;
             }
             if ( p.getHomes().get( p.getServer().getInfo().getName() ) == null ) {
                 p.getHomes().put( p.getServer().getInfo().getName(), new ArrayList<Home>() );
             }
             p.getHomes().get( p.getServer().getInfo().getName() ).add( new Home( p.getName(), home, loc ) );
             SQLManager.standardQuery( "INSERT INTO BungeeHomes (player,home_name,server,world,x,y,z,yaw,pitch) VALUES('" + player + "','" + home + "','" + loc.getServer().getName() + "','" + loc.getWorld() + "'," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch() + ")" );
             p.sendMessage( Messages.HOME_SET );
         } else {
             getSimilarHome( p, home ).setLoc( loc );
            SQLManager.standardQuery( "UPDATE BungeeHomes SET server = '" + loc.getServer().getName() + "', world = '" + loc.getWorld() + "', x = " + loc.getX() + ", y = " + loc.getY() + ", z = " + loc.getZ() + ", yaw = " + loc.getYaw() + ", pitch = " + loc.getPitch() + " WHERE player = '"+player+"'" );
             p.sendMessage( Messages.HOME_UPDATED );
             return;
         }
     }
 
     private static int getPlayersGlobalHomeCount( BSPlayer player ) {
         int count = 0;
         for ( ArrayList<Home> list : player.getHomes().values() ) {
             count += list.size();
         }
         return count;
     }
 
     private static int getPlayersServerHomeCount( BSPlayer player ) {
         ArrayList<Home> list = player.getHomes().get( player.getServer().getInfo().getName() );
         if ( list == null ) {
             return 0;
         } else {
             return list.size();
         }
     }
 
     public static void listPlayersHomes( BSPlayer player ) throws SQLException {
     	if(player.getHomes().isEmpty()){
     		player.sendMessage(Messages.NO_HOMES);
     		return;
     	}
     	boolean empty = true;
         for ( String server : player.getHomes().keySet() ) {
             String homes;
             if ( server.equals( player.getServer().getInfo().getName() ) ) {
                 homes = ChatColor.RED + server + ": " + ChatColor.BLUE;
             } else {
                 homes = ChatColor.GOLD + server + ": " + ChatColor.BLUE;
             }
             for ( Home h : player.getHomes().get( server ) ) {
                 homes += h.name + ", ";
                 empty = false;
             }
             if(empty){
             	player.sendMessage(Messages.NO_HOMES);
             	return;
             }
             player.sendMessage( homes.substring( 0, homes.length() - 2 ) );
         }
 
     }
 
     public static void loadPlayersHomes( BSPlayer player ) throws SQLException {
         ResultSet res = SQLManager.sqlQuery( "SELECT * FROM BungeeHomes WHERE player = '" + player.getName() + "'" );
         while ( res.next() ) {
             String server = res.getString( "server" );
             Location l = new Location( server, res.getString( "world" ), res.getDouble( "x" ), res.getDouble( "y" ), res.getDouble( "z" ), res.getFloat( "yaw" ), res.getFloat( "pitch" ) );
             Home h = new Home( player.getName(), res.getString( "home_name" ), l );
             if ( player.getHomes().get( server ) == null ) {
                 ArrayList<Home> list = new ArrayList<>();
                 list.add( h );
                 player.getHomes().put( server, list );
             } else {
                 player.getHomes().get( server ).add( h );
             }
         }
         res.close();
     }
 
 
     public static Home getSimilarHome( BSPlayer player, String home ) {
         for ( ArrayList<Home> list : player.getHomes().values() ) {
             for ( Home h : list ) {
                 if ( h.name.toLowerCase().contains( home.toLowerCase() ) ) {
                     return h;
                 }
             }
         }
         return null;
     }
 
     public static void sendPlayerToHome( BSPlayer player, String home ) {
         Home h = getSimilarHome( player, home );
         if ( h == null ) {
             player.sendMessage( Messages.HOME_DOES_NOT_EXIST );
             return;
         }
         Location l = h.loc;
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "TeleportToLocation" );
             out.writeUTF( player.getName() );
             out.writeUTF( l.getWorld() );
             out.writeDouble( l.getX() );
             out.writeDouble( l.getY() );
             out.writeDouble( l.getZ() );
             out.writeFloat( l.getYaw() );
             out.writeFloat( l.getPitch() );
         } catch ( IOException e ) {
             e.printStackTrace();
         }
         sendPluginMessageTaskHomes( l.getServer(), b );
         if ( !player.getServer().getInfo().equals( l.getServer() ) ) {
             player.getProxiedPlayer().connect( l.getServer() );
         }
         player.sendMessage( Messages.SENT_HOME );
     }
 
     public static void sendPluginMessageTaskHomes( ServerInfo server, ByteArrayOutputStream b ) {
         BungeeSuite.proxy.getScheduler().runAsync( BungeeSuite.instance, new SendPluginMessage( OUTGOING_CHANNEL, server, b ) );
     }
 
 
     public static void deleteHome( String player, String home ) {
         BSPlayer p = PlayerManager.getPlayer( player );
         Home h = getSimilarHome( p, home );
         if ( h == null ) {
             p.sendMessage( Messages.HOME_DOES_NOT_EXIST );
             return;
         }
         for ( ArrayList<Home> list : p.getHomes().values() ) {
             if ( list.contains( h ) ) {
                 list.remove( h );
                 break;
             }
         }
         try {
             SQLManager.standardQuery( "DELETE FROM BungeeHomes WHERE home_name = '" + h.name + "' AND player = '" + p.getName() + "'" );
         } catch ( SQLException e ) {
             e.printStackTrace();
         }
         p.sendMessage( Messages.HOME_DELETED );
     }
 }
 
