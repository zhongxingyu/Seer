 package com.minecraftdimensions.bungeesuite.commands;
 
 import com.minecraftdimensions.bungeesuite.BungeeSuite;
 import com.minecraftdimensions.bungeesuite.managers.*;
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.config.ServerInfo;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.plugin.Command;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 /**
  * User: Bloodsplat
  * Date: 13/10/13
  */
 public class BSVersionCommand extends Command {
 
     public BSVersionCommand() {
        super( "bsversion", "bsversions" );
     }
 
     @Override
     public void execute( CommandSender sender, String[] args ) {
         if ( !( sender.hasPermission( "bungeesuite.version" ) || sender.hasPermission( "bungeesuite.admin" ) ) ) {
             ProxiedPlayer p = ( ProxiedPlayer ) sender;
             p.chat( "/bsversion" );
         } else {
             ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b );
             try {
                 out.writeUTF( "GetVersion" );
 
             } catch ( IOException e ) {
                 e.printStackTrace();
             }
             sender.sendMessage( ChatColor.RED + "BungeeSuite version - " + ChatColor.GOLD + BungeeSuite.instance.getDescription().getVersion() );
             if ( sender instanceof ProxiedPlayer ) {
                 ProxiedPlayer p = ( ProxiedPlayer ) sender;
                 try {
                     out.writeUTF( p.getName() );
                 } catch ( IOException e ) {
                     e.printStackTrace();
                 }
                 ServerInfo s = p.getServer().getInfo();
                 ChatManager.sendPluginMessageTaskChat( s, b );
                 BansManager.sendPluginMessageTaskBans( s, b );
                 HomesManager.sendPluginMessageTaskHomes( s, b );
                 PortalManager.sendPluginMessageTaskPortals( s, b );
                 SpawnManager.sendPluginMessageTaskSpawns( s, b );
                 TeleportManager.sendPluginMessageTaskTP( s, b );
                 WarpsManager.sendPluginMessageTaskTP( s, b );
             } else {
                 if ( args.length == 0 ) {
                     return;
                 } else {
                     ServerInfo s = ProxyServer.getInstance().getServerInfo( args[0] );
                     if ( s == null ) {
                         sender.sendMessage( ChatColor.RED + "Server does not exist" );
                         return;
                     }
                     if ( s.getPlayers().size() == 0 ) {
                         sender.sendMessage( ChatColor.RED + "That server is either offline or there are no players on it" );
                         return;
                     }
                     ChatManager.sendPluginMessageTaskChat( s, b );
                     BansManager.sendPluginMessageTaskBans( s, b );
                     HomesManager.sendPluginMessageTaskHomes( s, b );
                     PortalManager.sendPluginMessageTaskPortals( s, b );
                     SpawnManager.sendPluginMessageTaskSpawns( s, b );
                     TeleportManager.sendPluginMessageTaskTP( s, b );
                     WarpsManager.sendPluginMessageTaskTP( s, b );
                 }
             }
         }
 
     }
 }
