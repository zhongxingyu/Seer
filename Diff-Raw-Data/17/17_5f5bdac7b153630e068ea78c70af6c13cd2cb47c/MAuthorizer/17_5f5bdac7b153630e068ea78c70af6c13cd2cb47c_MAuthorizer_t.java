 /*
  * Handles passwords and logins on a bukkit server
  */
 package com.prosicraft.ultravision.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author prosicraft
  */
 public class MAuthorizer
 {
 	private HashMap<String, String> a = null;
 	private List<String> c = null;
 	private File b = null;
 	private boolean loaded = false;
 
 	public MAuthorizer( String a )
 	{
 		try
 		{
 			b = new File( a );
 			this.a = new HashMap<>();
 			this.c = new ArrayList<>();
 			if( !b.exists() )
 			{
 				b.mkdirs();
 				b.createNewFile();
 				MCrypt.saveHashes( a, this.a );
 			}
 		}
 		catch( IOException ioex )
 		{
 			MLog.e( "(MAuth) Authorization file invalid." );
 		}
 	}
 
 	/**
 	 * Loads all hashes from passwort file
 	 */
 	public void a()
 	{
 		if( loaded )
 			MLog.w( "(MAuth) Loading again from Hashmap file... means: Reload without saving!" );
 		if( ( a = MCrypt.loadHashes( b.getAbsolutePath() ) ) == null )
 			a = new HashMap<>();
 		//MLog.d("(MAuth) Releasing MAuthorizer.a");
 		loaded = true;
 	}
 
 	/**
 	 * Checks if player is registered with the given password
 	 *
 	 * @param p The Player
 	 * @param pass The Password
 	 * @return true: Player registered and correct password, false: Anything
 	 * went wrong, or no access
 	 */
 	public boolean b( Player p, String pass )
 	{
 		if( !loaded )
 		{
 			MLog.e( "(MAuth) Can't do check on " + p.getName() + ": HashMaps are not loaded." );
 			return false;
 		}
 		if( !isRegistered( p.getName() ) )
 		{
 			MLog.e( "(MAuth) Can't check player " + p.getName() + ": Never registered." );
 		}
 		return d( p.getName() ).equals( MCrypt.getHash( 1000, pass, "i8765rtghjklo987654redfghjukiloi8u7z654e34r56789ikjhgf87654rfghzjui876tghjkioi8u7z6trer456z7uj" ) );
 	}
 
 	private boolean c( Player p, String pass )
 	{
 		if( !loaded )
 		{
 			MLog.e( "(MAuth) Can't register player " + p.getName() + ": HashMaps are not initialized." );
 			return false;
 		}
 		if( isRegistered( p.getName() ) )
 		{
 			MLog.e( "(MAuth) Can't register player " + p.getName() );
 			return false;
 		}
 		a.put( p.getName(), MCrypt.getHash( 1000, pass, "i8765rtghjklo987654redfghjukiloi8u7z654e34r56789ikjhgf87654rfghzjui876tghjkioi8u7z6trer456z7uj" ) );
 		MCrypt.saveHashes( b.getAbsolutePath(), a );
 		return true;
 	}
 
 	private String d( String pName )
 	{
 		for( String pn : a.keySet() )
 		{
 			if( pName.equalsIgnoreCase( pn ) )
 				return a.get( pn );
 		}
 		return "";
 	}
 
 	/**
 	 * Logs a player in
 	 *
 	 * @param p The Player
 	 * @param pass The Password
 	 * @return SUCCESS: Player logged in successfully, ERROR: Wrong password
 	 * or system error
 	 */
 	public MResult login( Player p, String pass )
 	{
 		if( b( p, pass ) )
 		{
 			if( c.contains( p.getName() ) )
 				return MResult.RES_ALREADY;
 			c.add( p.getName() );
 			return MResult.RES_SUCCESS;
 		}
 		return MResult.RES_NOACCESS;
 	}
 
 	/**
 	 * Logs a player out
 	 *
 	 * @param p The Player
 	 * @return SUCCESS: Player logged ou successfully, ERROR: or system
 	 * error, ALREADY: Not logged in
 	 */
 	public MResult logout( Player p )
 	{
 		if( !c.contains( p.getName() ) )
 			return MResult.RES_ALREADY;
 		c.remove( p.getName() );
 		return MResult.RES_SUCCESS;
 	}
 
 	public MResult logout( String pName )
 	{
 		if( !c.contains( pName ) )
 			return MResult.RES_ALREADY;
 		c.remove( pName );
 		return MResult.RES_SUCCESS;
 	}
 
 	/**
 	 * Registers a new player.
 	 *
 	 * @param p The Player
 	 * @param pass The password
 	 * @return SUCCESS: All did fine, ERROR: Something went wrong. Check
 	 * log.
 	 */
 	public MResult register( Player p, String pass )
 	{
 		if( c.contains( p.getName() ) && !b( p, pass ) )
 			return MResult.RES_NOACCESS;
 		if( a.containsKey( p.getName() ) )
 			return MResult.RES_ALREADY;
 		if( c( p, pass ) )
 			return MResult.RES_SUCCESS;
 		MLog.e( "Something went wrong while registering new player :(" );
 		return MResult.RES_ERROR;
 	}
 
 	public MResult unregister( String pName, Player p )
 	{
		if( pName.equals( "" ) )
			return MResult.RES_NOTGIVEN;

		if( !isRegistered( p ) )
			return MResult.RES_ALREADY;

 		a.remove( pName );
 		if( p != null )
 		{
 			logout( p );
 			p.sendMessage( ChatColor.GOLD + "You have been unregistered from login system." );
 		}
 		else
 			logout( pName );
 		MCrypt.saveHashes( b.getAbsolutePath(), a );
 		return MResult.RES_SUCCESS;
 	}
 
 	public boolean isRegistered( Player p )
 	{
 		return isRegistered( p.getName() );
 	}
 
 	public boolean isRegistered( String pName )
 	{
 		for( String pn : a.keySet() )
 		{
 			if( pn.equalsIgnoreCase( pName ) )
 				return true;
 		}
 		return false;
 	}
 
 	public boolean loggedIn( Player p )
 	{
 		boolean contains = false;
 		for( String pn : c )
 			if( pn.equalsIgnoreCase( p.getName() ) )
 				contains = true;
 		return ( ( isRegistered( p ) ) ? contains : true );
 	}
 
 	public void save()
 	{
 		MCrypt.saveHashes( b.getAbsolutePath(), a );
 	}
 
 	public static char[] getCharArray( String s, int i )
 	{
 		char[] res = new char[ i ];
 		for( int n = 0; n < i; n++ )
 			res[n] = ( ( s.length() > n ) ? s.charAt( n ) : 0 );
 		return res;
 	}
 
 	public static byte[] getCharArrayB( String s, int i )
 	{
 		byte[] res = new byte[ i ];
 		for( int n = 0; n < i; n++ )
 			res[n] = ( byte ) ( ( s.length() > n ) ? s.charAt( n ) : 0 );
 		return res;
 	}
 }
