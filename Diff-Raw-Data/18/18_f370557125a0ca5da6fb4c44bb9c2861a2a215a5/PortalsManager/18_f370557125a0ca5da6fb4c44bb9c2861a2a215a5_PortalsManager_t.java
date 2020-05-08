 package com.minecraftdimensions.bungeesuiteportals.managers;
 
 import com.minecraftdimensions.bungeesuiteportals.BungeeSuitePortals;
 import com.minecraftdimensions.bungeesuiteportals.objects.Portal;
 import com.minecraftdimensions.bungeesuiteportals.tasks.PluginMessageTask;
 import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class PortalsManager {
 
     public static HashMap<World, ArrayList<Portal>> PORTALS = new HashMap<>();
 
     public static void deletePortal( String name, String string ) {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "DeletePortal" );
             out.writeUTF( name );
             out.writeUTF( string );
 
         } catch ( IOException e ) {
             e.printStackTrace();
         }
         new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuitePortals.INSTANCE );
 
     }
 
     public static void removePortal( String name ) {
         Portal p = getPortal( name );
         if ( p != null ) {
             for ( ArrayList<Portal> list : PORTALS.values() ) {
                 if ( list.contains( p ) ) {
                     list.remove( p );
                 }
             }
         }
     }
 
     public static Portal getPortal( String name ) {
         for ( ArrayList<Portal> list : PORTALS.values() ) {
             for ( Portal p : list ) {
                 if ( p.getName().equals( name ) ) {
                     return p;
                 }
             }
         }
         return null;
     }
 
     public static void getPortalsList( String name ) {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "ListPortals" );
             out.writeUTF( name );
 
         } catch ( IOException e ) {
             e.printStackTrace();
         }
         new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuitePortals.INSTANCE );
 
     }
 
     public static void teleportPlayer( Player p, Portal portal ) {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "TeleportPlayer" );
             out.writeUTF( p.getName() );
             out.writeUTF( portal.getType() );
             out.writeUTF( portal.getDestination() );
             out.writeBoolean( p.hasPermission( "bungeesuite.portals.portal." + portal.getName() ) || p.hasPermission( "bungeesuite.portals.portal.*" ) );
 
         } catch ( IOException e ) {
             e.printStackTrace();
         }
         new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuitePortals.INSTANCE );
     }
 
     public static void setPortal( CommandSender sender, String name, String type, String dest, String fill ) {
 
         Player p = ( Player ) sender;
         Selection sel = BungeeSuitePortals.WORLDEDIT.getSelection( p );
 
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream( b );
         try {
             out.writeUTF( "SetPortal" );
             out.writeUTF( sender.getName() );
             if ( sel == null || !( sel instanceof CuboidSelection ) ) {
                 out.writeBoolean( false );
             } else {
                 out.writeBoolean( true );
                 out.writeUTF( name );
                 out.writeUTF( type );
                 out.writeUTF( dest );
                 out.writeUTF( fill );
                 Location max = sel.getMaximumPoint();
                 Location min = sel.getMinimumPoint();
                 out.writeUTF( max.getWorld().getName() );
                 out.writeDouble( max.getX() );
                 out.writeDouble( max.getY() );
                 out.writeDouble( max.getZ() );
                 out.writeUTF( min.getWorld().getName() );
                 out.writeDouble( min.getX() );
                 out.writeDouble( min.getY() );
                 out.writeDouble( min.getZ() );
             }
         } catch ( IOException e ) {
             e.printStackTrace();
         }
         new PluginMessageTask( b ).runTaskAsynchronously( BungeeSuitePortals.INSTANCE );
 
     }
 
     public static void addPortal( String name, String type, String dest, String filltype, Location max, Location min ) {
        Portal portal = new Portal( name, type, dest, filltype, max, min );
         ArrayList<Portal> ps = PORTALS.get( max.getWorld() );
         if ( ps == null ) {
             ps = new ArrayList<>();
         }
        ps.add( portal );
        System.out.println( "Created portal " + portal.getName() );
        portal.fillPortal();
     }
 
 }
