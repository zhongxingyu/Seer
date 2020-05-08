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
 			MAuthorizer auth = ( (ultravision)getParent() ).getAuthorizer();
 			if( auth == null || !( (ultravision)getParent() ).IsUsingAuthorizer() )
 			{
 				return suc( p, ChatColor.YELLOW + "The Authorizer system is not used." );
 			}
 
 			if( numArgs() < 1 )
 			{
 				return err( p, "Too few arguments" );
 			}
 
 			String thePlayer = getArg( 0 );
 			MResult res;
 			if( ( res = auth.unregister( thePlayer, getServer().getPlayer( thePlayer ) ) ) == MResult.RES_SUCCESS )
 			{
 				return suc( p, "Unregistered player " + thePlayer + " successfully." );
 			}
			else if( res == MResult.RES_ALREADY )
			{
				return suc( p, ChatColor.YELLOW + "This player is not registered." );
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
