 package com.ikkerens.worldedit.commands;
 
 import com.ikkerens.worldedit.WorldEditPlugin;
 import com.ikkerens.worldedit.handlers.AbstractCommand;
 import com.ikkerens.worldedit.model.Selection;
 import com.ikkerens.worldedit.model.events.SelectionCommandEvent;
 
 import com.mbserver.api.game.Player;
 
 public class PositionCommand extends AbstractCommand< WorldEditPlugin > {
 
     public PositionCommand( final WorldEditPlugin plugin ) {
         super( plugin );
     }
 
     @Override
     protected void execute( final String label, final Player player, final String[] args ) {
         this.getPlugin();
         final Selection sel = WorldEditPlugin.getSession( player ).getSelection();
 
         final PositionCommandEvent event = new PositionCommandEvent( player, label.equalsIgnoreCase( "/pos1" ) );
         this.getPlugin().getPluginManager().triggerEvent( event );
 
        if ( !event.isCancelled() )
             if ( label.equalsIgnoreCase( "/pos1" ) )
                sel.setPosition1( player.getLocation() );
             else
                sel.setPosition2( player.getLocation() );
     }
 
     public static class PositionCommandEvent extends SelectionCommandEvent {
         private final boolean isPos1;
 
         public PositionCommandEvent( final Player player, final boolean isPos1 ) {
             super( player );
             this.isPos1 = isPos1;
         }
 
         public boolean isPos1() {
             return this.isPos1;
         }
 
         public boolean isPos2() {
             return !this.isPos1;
         }
 
     }
 }
