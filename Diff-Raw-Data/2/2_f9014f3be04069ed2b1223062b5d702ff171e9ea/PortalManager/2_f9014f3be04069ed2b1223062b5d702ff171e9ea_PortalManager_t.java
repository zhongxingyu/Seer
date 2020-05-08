 package com.minecraftdimensions.bungeesuite.managers;
 
 import com.minecraftdimensions.bungeesuite.BungeeSuite;
 import com.minecraftdimensions.bungeesuite.objects.*;
 import com.minecraftdimensions.bungeesuite.tasks.SendPluginMessage;
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.config.ServerInfo;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class PortalManager {
     static HashMap<ServerInfo, ArrayList<Portal>> portals = new HashMap<>();
    public static String OUTGOING_CHANNEL = "BungeeSuitePorts";
 
     public static void loadPortals() throws SQLException {
         ResultSet res = SQLManager.sqlQuery( "SELECT * FROM BungeePortals" );
         while(res.next()){
         String name = res.getString("portalname");
         String server = res.getString("server");
         String type = res.getString("type");
         String dest = res.getString("destination");
         String world = res.getString("world");
         String fill = res.getString("filltype");
         double xmax = res.getDouble("xmax");
         double xmin = res.getDouble("xmin");
         double ymax = res.getDouble("ymax");
         double ymin = res.getDouble("ymin");
         double zmax = res.getDouble("zmax");
         double zmin = res.getDouble("zmin");
         Portal p = new Portal(name, server, fill, type, dest, new Location(server,world, xmax,ymax,zmax), new Location(server,world, xmin,ymin,zmin));
         	ArrayList<Portal> list = portals.get( p.getServer() );
         if ( list == null ) {
             list = new ArrayList<>();
         }
         list.add( p );
         }
         res.close();
     }
 
     public void getPortals( ServerInfo s ) {
         for ( Portal p : portals.get( s ) ) {
             sendPortal( p );
         }
     }
 
 
     public static void setPortal( BSPlayer sender, String name, String type, String dest, String fillType, Location max, Location min ) throws SQLException {
         if ( !( type.equalsIgnoreCase( "warp" ) || type.equalsIgnoreCase( "server" ) ) ) {
             sender.sendMessage( Messages.INVALID_PORTAL_TYPE );
             return;
         }
         fillType = fillType.toUpperCase();
         if ( !( fillType.equals( "AIR" ) || fillType.equals( "LAVA" ) || fillType.equals( "WATER" ) || fillType.equals( "WEB" ) || fillType.equals( "SUGAR_CANE" ) || fillType.equals( "PORTAL" ) || fillType.equals( "END_PORTAL" ) ) ) {
             sender.sendMessage( Messages.PORTAL_FILLTYPE );
             return;
         }
         if ( dest.equalsIgnoreCase( "warp" ) ) {
             if ( !WarpsManager.doesWarpExist( dest ) ) {
                 sender.sendMessage( Messages.PORTAL_DESTINATION_NOT_EXIST );
                 return;
             }
         } else {
             if ( BungeeSuite.proxy.getServerInfo( dest ) == null ) {
                 sender.sendMessage( Messages.PORTAL_DESTINATION_NOT_EXIST );
                 return;
             }
         }
         Portal p;
         ArrayList<Portal> list = portals.get( max.getServer() );
         if ( list == null ) {
             list = new ArrayList<>();
         }
         if ( doesPortalExist( name ) ) {
             p = getPortal( name );
             for ( ArrayList<Portal> l : portals.values() ) {
                 if ( l.contains( p ) ) {
                     l.remove( p );
                     removePortal( p );
                 }
             }
 
             SQLManager.standardQuery( "UPDATE BungeePortals SET server='" + max.getServer() + "', world='" + max.getWorld() + "', type ='" + type + "' filltype = '" + fillType + "', destination = '" + dest + "', xmax=" + max.getX() + ", ymax=" + max.getY() + ", zmax=" + max.getZ() + ", xmin = " + min.getX() + ", ymin = " + min.getY() + ", zmin =" + min.getZ() + " WHERE portalname='" + name + "'" );
 
             sender.sendMessage( Messages.PORTAL_UPDATED );
         } else {
             SQLManager.standardQuery( "INSERT INTO BungeePortals VALUES('" + name + "','" + max.getServer() + "','" + type + "', '" + dest + "', '" + max.getWorld() + "', '" + fillType + "', " + max.getX() + ", " + min.getX() + "," + max.getY() + "," + min.getY() + "," + max.getZ() + ", " + min.getZ() + ")" );
 
             sender.sendMessage( Messages.PORTAL_CREATED );
             p = new Portal( name, max.getServer().getName(), fillType, type, dest, max, min );
 
 
         }
         sendPortal( p );
         list.add( p );
     }
 
     public static void sendPortal( Portal p ) {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "SendPortal" );
             out.writeUTF( p.getName() );
             out.writeUTF( p.getType() );
             out.writeUTF( p.getDest() );
             out.writeUTF( p.getFillType() );
             Location max = p.getMax();
             out.writeUTF( max.getWorld() );
             out.writeDouble( max.getX() );
             out.writeDouble( max.getY() );
             out.writeDouble( max.getZ() );
             Location min = p.getMin();
             out.writeUTF( min.getWorld() );
             out.writeDouble( min.getX() );
             out.writeDouble( min.getY() );
             out.writeDouble( min.getZ() );
         } catch ( IOException e ) {
             e.printStackTrace();
         }
         sendPluginMessageTaskPortals( p.getServer(), b );
     }
 
     public static void removePortal( Portal p ) {
 
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "DeletePortal" );
             out.writeUTF( p.getName() );
         } catch ( IOException e ) {
             e.printStackTrace();
         }
         sendPluginMessageTaskPortals( p.getServer(), b );
     }
 
     public static void deletePortal( BSPlayer sender, String portal ) throws SQLException {
         if ( !doesPortalExist( portal ) ) {
             sender.sendMessage( Messages.PORTAL_DOES_NOT_EXIST );
             return;
         }
         Portal p = getPortal( portal );
         ArrayList<Portal> list = portals.get( p.getServer() );
         list.remove( p );
         SQLManager.standardQuery( "DELETE FROM BungeePortals WHERE portalname ='" + portal + "'" );
         removePortal( p );
 
         sender.sendMessage( Messages.PORTAL_DELETED );
     }
 
     public static Portal getPortal( String name ) {
         for ( ArrayList<Portal> list : portals.values() ) {
             for ( Portal p : list ) {
                 if ( p.getName().equalsIgnoreCase( name ) ) {
                     return p;
                 }
             }
         }
         return null;
     }
 
     public static boolean doesPortalExist( String name ) {
         for ( ArrayList<Portal> list : portals.values() ) {
             for ( Portal p : list ) {
                 if ( p.getName().equalsIgnoreCase( name ) ) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public static void sendPluginMessageTaskPortals( ServerInfo server, ByteArrayOutputStream b ) {
         BungeeSuite.proxy.getScheduler().runAsync( BungeeSuite.instance, new SendPluginMessage( OUTGOING_CHANNEL, server, b ) );
     }
 
     public static void teleportPlayer( BSPlayer p, String type, String dest, boolean perm ) {
         if ( !perm ) {
             p.sendMessage( Messages.PORTAL_NO_PERMISSION );
             return;
         }
 
         if ( type.equalsIgnoreCase( "warp" ) ) {
             if ( !WarpsManager.doesWarpExist( dest ) ) {
                 p.sendMessage( Messages.PORTAL_DESTINATION_NOT_EXIST );
             } else {
                 Warp w = WarpsManager.getWarp( dest );
                 Location loc = w.getLocation();
                 ByteArrayOutputStream b = new ByteArrayOutputStream();
                 DataOutputStream out = new DataOutputStream( b );
                 try {
                     out.writeUTF( "TeleportPlayer" );
                     out.writeUTF( p.getName() );
                     out.writeUTF( loc.getWorld() );
                     out.writeDouble( loc.getX() );
                     out.writeDouble( loc.getY() );
                     out.writeDouble( loc.getZ() );
                     out.writeFloat( loc.getYaw() );
                     out.writeFloat( loc.getPitch() );
                 } catch ( IOException e ) {
                     e.printStackTrace();
                 }
                 sendPluginMessageTaskPortals( loc.getServer(), b );
                 if ( !loc.getServer().equals( p.getServer().getInfo() ) ) {
                     p.sendToServer( loc.getServer().getName() );
                 }
             }
         } else {
             if ( BungeeSuite.proxy.getServerInfo( dest ) == null ) {
                 p.sendMessage( Messages.PORTAL_DESTINATION_NOT_EXIST );
                 return;
             }
             ServerInfo s = BungeeSuite.proxy.getServerInfo( dest );
             if ( !s.equals( p.getServer().getInfo() ) ) {
                 p.connectTo( s );
             }
         }
 
     }
 
     public static void listPortals( BSPlayer p ) {
         for ( ServerInfo s : portals.keySet() ) {
             String message = "";
             message += ChatColor.GOLD + s.getName() + ": " + ChatColor.RESET;
             ArrayList<Portal> list = portals.get( s );
             for ( Portal portal : list ) {
                 message += portal.getName() + ", ";
             }
             p.sendMessage( message );
         }
     }
 }
