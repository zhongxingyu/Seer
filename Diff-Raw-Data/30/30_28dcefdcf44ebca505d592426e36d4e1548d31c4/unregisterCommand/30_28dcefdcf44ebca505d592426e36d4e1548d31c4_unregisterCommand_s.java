 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.prosicraft.ultravision.commands;
 
 import com.prosicraft.ultravision.base.UVClickAuth;
 import com.prosicraft.ultravision.ultravision;
 import com.prosicraft.ultravision.util.MAuthorizer;
 import com.prosicraft.ultravision.util.MLog;
 import com.prosicraft.ultravision.util.MResult;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author prosicraft
  */
 public class unregisterCommand extends extendedCommand
 {
 	public unregisterCommand( ultravision uv, String[] args )
 	{
 		super( uv, args );
 	}
 
 	@Override
 	public commandResult run( Player p )
 	{
 		try
 		{
 			// /unregister <player>
 			this.ev( p );
			UVClickAuth clickauth = ( (ultravision)getParent() ).getClickAuth();
			if( clickauth == null )
 			{
				return suc( p, ChatColor.YELLOW + "The UV-ClickAuth system is not used." );
 			}
 
 			if( numArgs() < 1 )
 			{
 				return err( p, "Too few arguments" );
 			}
 
 			String thePlayer = getArg( 0 );
 			MResult res;
			MAuthorizer auth = ( (ultravision)getParent() ).getAuthorizer();
 			if( ( res = auth.unregister( thePlayer, getServer().getPlayer( thePlayer ) ) ) == MResult.RES_SUCCESS )
 			{
 				return suc( p, "Unregistered player " + thePlayer + " successfully." );
 			}
 			else
 			{
 				return err( p, "Couldn't unregister player " + thePlayer + ": " + String.valueOf( res ) );
 			}
 		}
 		catch( wrongParentException | wrongPlayerException ex )
 		{
 			MLog.e( "[UNREGISTERCMD] " + ex.getMessage() );
 			ex.printStackTrace( System.out );
 			return err( p, "Failed to execute command." );
 		}
 	}
 }
