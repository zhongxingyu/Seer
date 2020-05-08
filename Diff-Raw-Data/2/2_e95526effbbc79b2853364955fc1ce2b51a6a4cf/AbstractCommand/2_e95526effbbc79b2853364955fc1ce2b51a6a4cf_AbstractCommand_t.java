 package com.ikkerens.worldedit.handlers;
 
 import com.mbserver.api.CommandExecutor;
 import com.mbserver.api.CommandSender;
 import com.mbserver.api.MBServerPlugin;
 import com.mbserver.api.Manifest;
 import com.mbserver.api.game.Player;
 
 public abstract class AbstractCommand< P extends MBServerPlugin > extends AbstractHandler< P > implements CommandExecutor {
     protected final static String NEED_SELECTION = "You need a valid selection to do this.";
     protected final static String FINISHED_DONE  = "Action of %s blocks completed in %s seconds.";
     protected final static String FINISHED_LIMIT = "Hit limit of %s blocks after %s seconds.";
 
     private static String         permissionName;
 
     public AbstractCommand( final P plugin ) {
         super( plugin );
         if ( permissionName == null )
             permissionName = plugin.getClass().getAnnotation( Manifest.class ).name().toLowerCase().replaceFirst( "mb", "" );
     }
 
     @Override
     public void execute( final String command, final CommandSender sender, final String[] args, final String label ) {
         if ( !( sender instanceof Player ) ) {
             sender.sendMessage( "WorldEdit can only be used by players." );
             return;
         }
 
        if ( !sender.hasPermission( String.format( "ikkerens.%s.%s", permissionName, command.replaceFirst( "/", "" ) ) ) && !sender.hasPermission( String.format( "ikkerens.%s.*", permissionName ) ) ) {
             sender.sendMessage( "You do not have permission to use /" + label );
             return;
         }
 
         this.execute( label, (Player) sender, args );
     }
 
     protected abstract void execute( String label, Player player, String[] args );
 }
