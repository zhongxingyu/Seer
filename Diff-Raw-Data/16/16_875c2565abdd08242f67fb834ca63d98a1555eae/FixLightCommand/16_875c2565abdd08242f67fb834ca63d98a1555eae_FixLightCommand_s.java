 package com.ikkerens.worldedit.commands;
 
import java.util.HashSet;
 
 import com.ikkerens.worldedit.WorldEditPlugin;
 import com.ikkerens.worldedit.handlers.AbstractCommand;
 import com.ikkerens.worldedit.model.Selection;
 import com.ikkerens.worldedit.model.Session;
 import com.ikkerens.worldedit.model.events.WorldEditActionEvent;
 
 import com.mbserver.api.Constructors;
 import com.mbserver.api.game.Chunk;
 import com.mbserver.api.game.Location;
 import com.mbserver.api.game.Player;
 import com.mbserver.api.game.World;
 
 public class FixLightCommand extends AbstractCommand< WorldEditPlugin > {
 
     public FixLightCommand( final WorldEditPlugin plugin ) {
         super( plugin );
     }
 
     @Override
     protected void execute( final String label, final Player player, final String[] args ) {
         final Session session = WorldEditPlugin.getSession( player );
         final Selection sel = session.getSelection();
         if ( sel.isValid() ) {
             final Location lowest = sel.getMinimumPosition();
             final Location highest = sel.getMaximumPosition();
             final World world = lowest.getWorld();
 
             final FixLightEvent event = new FixLightEvent( player );
             this.getPlugin().getPluginManager().triggerEvent( event );
 
             if ( !event.isCancelled() ) {
                 final long start = System.currentTimeMillis();
 
                final HashSet< Chunk > chunks = new HashSet< Chunk >();
                for ( int x = lowest.getBlockX(); x < highest.getBlockX(); x += 16 )
                    for ( int y = lowest.getBlockY(); y < highest.getBlockY(); y += 16 )
                        for ( int z = lowest.getBlockZ(); z < highest.getBlockZ(); z += 16 )
                            chunks.add( Constructors.newLocation( world, x, y, z ).getChunk() );
 
                 for ( final Chunk chunk : chunks )
                     chunk.recalculateLight();
 
                 player.sendMessage( String.format( "Action completed in %s seconds.", ( System.currentTimeMillis() - start ) / 1000f ) );
             }
         } else
             player.sendMessage( NEED_SELECTION );
     }
 
     public static class FixLightEvent extends WorldEditActionEvent {
 
         public FixLightEvent( final Player player ) {
             super( player, null );
         }
     }
 
 }
