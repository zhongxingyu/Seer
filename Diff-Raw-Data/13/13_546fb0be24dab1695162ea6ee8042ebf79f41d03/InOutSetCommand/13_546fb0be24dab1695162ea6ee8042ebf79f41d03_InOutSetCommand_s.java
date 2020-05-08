 package com.ikkerens.worldedit.commands;
 
 import com.ikkerens.worldedit.WorldEditPlugin;
 import com.ikkerens.worldedit.handlers.AbstractCommand;
 import com.ikkerens.worldedit.model.Selection;
 
 import com.mbserver.api.game.Location;
 import com.mbserver.api.game.Player;
 
 public class InOutSetCommand extends AbstractCommand< WorldEditPlugin > {
 
     public InOutSetCommand( final WorldEditPlugin plugin ) {
         super( plugin );
     }
 
     @Override
     protected void execute( final String label, final Player player, final String[] args ) {
         if ( args.length > 2 ) {
             player.sendMessage( "Usage: /" + label + " -[h|v] <amount>" );
             return;
         }
 
         int it = -1;
 
         if ( args.length == 2 )
             if ( !args[ ++it ].equalsIgnoreCase( "-h" ) && !args[ it ].equalsIgnoreCase( "-v" ) ) {
                 player.sendMessage( "Usage: /" + label + " -[h|v] <amount>" );
                 return;
             }
 
         int amount;
 
         try {
             amount = Integer.parseInt( args[ ++it ] );
         } catch ( final NumberFormatException e ) {
             player.sendMessage( String.format( "%s is not a valid number.", args[ it ] ) );
             return;
         }
 
         final Selection sel = this.getSession( player ).getSelection();
         if ( sel.isValid() ) {
             final Location lowest = sel.getMinimumPosition();
             final Location highest = sel.getMaximumPosition();
 
            int modX = ( args.length == 2 ) && !args[ 0 ].equalsIgnoreCase( "-h" ) ? amount : 0;
            int modY = ( args.length == 2 ) && !args[ 0 ].equalsIgnoreCase( "-v" ) ? amount : 0;
 
             if ( label.equalsIgnoreCase( "/inset" ) ) {
                 modX *= -1;
                 modY *= -1;
             }
 
             lowest.add( -modX, -modY, -modX, false );
             highest.add( modX, modY, modX, false );
 
             sel.setPositions( lowest, highest );
             sel.inform();
         } else
             player.sendMessage( NEED_SELECTION );
     }
 
 }
