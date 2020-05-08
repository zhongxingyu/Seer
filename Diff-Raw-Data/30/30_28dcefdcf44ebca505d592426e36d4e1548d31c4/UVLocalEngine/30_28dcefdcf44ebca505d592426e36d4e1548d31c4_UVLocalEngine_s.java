 /*  ============================================================================
  *
  *      U L T R A V I S I O N  ---  P l a y e r   S u p e r v i s i o n
  *
  *           L  O  C  A  L        B  A  N  -   E  N  G  I  N  E
  *
  *                              by prosicraft  ,   (c) 2013
  *
  *  ============================================================================
  */
 package com.prosicraft.ultravision.local;
 
 import com.prosicraft.ultravision.base.*;
 import com.prosicraft.ultravision.commands.timeInterpreter;
 import com.prosicraft.ultravision.ultravision;
 import com.prosicraft.ultravision.util.*;
 import java.io.*;
 import java.sql.Time;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.minecraft.server.v1_6_R2.Packet255KickDisconnect;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.craftbukkit.v1_6_R2.CraftServer;
 import org.bukkit.entity.Player;
 
 /**
  *
  * Engine Main Class
  *
  * @author prosicraft
  *
  *
  */
 public class UVLocalEngine implements UltraVisionAPI
 {
 	private MAuthorizer authorizer		= null;	// The authorizer
 	private ultravision ultravisionPlugin	= null; // The UltraVision Plugin
 	private List<UVLocalPlayer> players	= null;	// player memory
 	private String pluginDirectory		= "";	// The path to the plugin Directory
 
 	/**********************************************************************/
 	/**
 	 * Constructor
 	 * @param pluginDir
 	 */
 	public UVLocalEngine( String pluginDir, ultravision plugin )
 	{
 		this.players = new ArrayList<>();
 		this.pluginDirectory = pluginDir;
 		this.ultravisionPlugin = plugin;
 	}
 
 	/**********************************************************************/
 	@Override
 	public boolean shutdown()
 	{
 		boolean result = true;
 		for( UVLocalPlayer player : players )
 		{
 			if( savePlayer( player.craftPlayer.getName() ) != MResult.RES_SUCCESS )
 				result = false;
 		}
 
 		players.clear();
 		return result;
 	}
 
 	/**********************************************************************/
 	/**
 	 * Search for a UVLocalPlayer that matches given name
 	 *
 	 * @param playerName players name
 	 */
 	public UVLocalPlayer getUVLocalPlayer( String playerName )
 	{
		MLog.d( "Trying to retrieve Local Player..." );
 		for( UVLocalPlayer player : players )
 		{
 			if( player.getCraftPlayer().getName().equalsIgnoreCase( playerName ) )
 			{
 				return player;
 			}
 		}
 
		MLog.d( "Player not there. Try to load from file." );
 		if( null == readPlayer( playerName, true ) )
 			return null;
 
 		for( UVLocalPlayer player : players )
 		{
 			if( player.getCraftPlayer().getName().equalsIgnoreCase( playerName ) )
 			{
 				MLog.d( "Got player successfully." );
 				return player;
 			}
 		}
 
		MLog.d( "Cannot find player though..." );
 		return null;
 	}
 
 	/**********************************************************************/
 	@Override
 	public Player getPlayer( String playerName )
 	{
 		UVLocalPlayer localPlayer = getUVLocalPlayer( playerName );
 		if( localPlayer != null )
 		{
 			return (Player)localPlayer.getCraftPlayer();
 		}
 		return null;
 	}
 
 	/**********************************************************************/
 	@Override
 	public UVPlayerInfo getPlayerInfo( String playerName )
 	{
 		UVLocalPlayer localPlayer = getUVLocalPlayer( playerName );
 		if( localPlayer != null )
 		{
 			return localPlayer.i;
 		}
 		return null;
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult savePlayer( String playerName )
 	{
 		try
 		{
 			// Get the Local Player and its information
 			UVLocalPlayer player = null;
 			for( UVLocalPlayer playerIterator : players )
 			{
 				if( playerIterator.getCraftPlayer().getName().equalsIgnoreCase( playerName ) )
 				{
 					player = playerIterator;
 					break;
 				}
 			}
 
 			if( player == null )
 			{
 				return MResult.RES_NOTGIVEN;
 			}
 
 			// Create file if not there already
 			File ud = new File( pluginDirectory + UltraVisionAPI.userDataDir, playerName + ".usr" );
 			if( !ud.exists() )
 			{
 				MLog.d( "File doesn't exist at " + MConfiguration.normalizePath( ud ) + ". Trying to create new one..." );
 				try
 				{
 					File uf = new File( pluginDirectory + UltraVisionAPI.userDataDir );
 					if( !uf.exists() )
 					{
 						uf.mkdirs();
 					}
 
 					ud.createNewFile();
 					MLog.i( "Created new player data file at '" + MConfiguration.normalizePath( ud ) + "'" );
 				}
 				catch( IOException ioex )
 				{
 					MLog.e( "Can't create new file at " + MConfiguration.normalizePath( ud ) );
 					return MResult.RES_ERROR;
 				}
 			}
 
 			DataOutputStream fod;
 			try
 			{
 				fod = new DataOutputStream( new FileOutputStream( ud ) );
 			}
 			catch( FileNotFoundException fnfex )
 			{
 				MLog.e( "(flushUD) Can't load UserData file: File not found" );
 				return null;
 			}
 
 			fod.write( MAuthorizer.getCharArrayB( "ouvplr", 6 ) );
 			fod.write( MAuthorizer.getCharArrayB( "uvinfo", 6 ) );
 			fod.write( UVFileInformation.uVersion );  // The Version
 			fod.write( MAuthorizer.getCharArrayB( playerName, 16 ) );  // Write player name
 			fod.write( player.i.isMute ? 1 : 0 ); // Write mute state
 			try
 			{
 				long temp = 0;
 				if( player.i.lastOnline != null )
 					temp = player.i.lastOnline.getTime();
 
 				fod.writeLong( temp );
 				fod.writeLong( player.i.onlineTime.getTime() );
 			}
 			catch( IOException ex )
 			{
 				MLog.e( "Can't write times to database!" );
 			}
 
 			fod.write( player.i.praise );   // Write praise
 
 			//=== Write praisers
 			if( !player.i.praiser.isEmpty() )
 			{
 				for( String praiser : player.i.praiser )
 				{
 					fod.write( MAuthorizer.getCharArrayB( "oprais", 6 ) );
 					fod.write( MAuthorizer.getCharArrayB( praiser, 16 ) );
 				}
 			}
 			else
 			{
 				fod.write( MAuthorizer.getCharArrayB( "nprais", 6 ) );
 			}
 
 			//=== Write bans
 			fod.write( MAuthorizer.getCharArrayB( "theban", 6 ) );
 			if( player.i.ban != null )
 			{
 				player.i.ban.write( fod );
 			}
 			else
 			{
 				UVBan.writeNull( fod );
 			}
 
 			if( !player.i.banHistory.isEmpty() )
 			{
 				for( UVBan b : player.i.banHistory )
 				{
 					fod.write( MAuthorizer.getCharArrayB( "oneban", 6 ) );
 					b.write( fod );
 				}
 			}
 			else
 			{
 				fod.write( MAuthorizer.getCharArrayB( "nooban", 6 ) );
 			}
 
 			//=== Write Warnings
 			fod.write( MAuthorizer.getCharArrayB( "thwarn", 6 ) );
 			if( player.i.warning != null )
 			{
 				player.i.warning.write( fod );
 			}
 			else
 			{
 				UVWarning.writeNull( fod );
 			}
 
 			if( !player.i.warnHistory.isEmpty() )
 			{
 				for( UVWarning b : player.i.warnHistory )
 				{
 					fod.write( MAuthorizer.getCharArrayB( "onwarn", 6 ) );
 					b.write( fod );
 				}
 			}
 			else
 			{
 				fod.write( MAuthorizer.getCharArrayB( "nowarn", 6 ) );
 			}
 
 			//=== Write Kick History
 			if( !player.i.kickHistory.isEmpty() )
 			{
 				for( UVKick k : player.i.kickHistory )
 				{
 					fod.write( MAuthorizer.getCharArrayB( "onkick", 6 ) );
 					k.write( fod );
 				}
 			}
 			else
 			{
 				fod.write( MAuthorizer.getCharArrayB( "nokick", 6 ) );
 			}
 
 			//=== Write Friends
 			if( !player.i.friends.isEmpty() )
 			{
 				for( String friend : player.i.friends )
 				{
 					fod.write( MAuthorizer.getCharArrayB( "friend", 6 ) );
 					fod.write( MAuthorizer.getCharArrayB( friend, 16 ) );
 				}
 			}
 			else
 			{
 				fod.write( MAuthorizer.getCharArrayB( "nofrie", 6 ) );
 			}
 
 			//=== Write notes
 			if( !player.i.notes.isEmpty() )
 			{
 				for( String devil : player.i.notes.keySet() )
 				{
 					fod.write( MAuthorizer.getCharArrayB( "onnote", 6 ) );
 					fod.write( MAuthorizer.getCharArrayB( devil, 16 ) );
 					fod.write( MAuthorizer.getCharArrayB( player.i.notes.get( devil ), 60 ) );
 				}
 			}
 			else
 			{
 				fod.write( MAuthorizer.getCharArrayB( "nonote", 6 ) );
 			}
 
 			//=== Write additional chunks
 			/*if ( addch != null )
 			 {
 			 for (UVPlayerInfoChunk pic : addch)
 			 {
 			 pic.write(fod);
 			 }
 			 }*/
 
 			//=== Write Player end
 			fod.write( MAuthorizer.getCharArrayB( "theend", 6 ) );
 			fod.flush();
 			fod.close();
 
 			return MResult.RES_SUCCESS;
 		}
 		catch( IOException ex )
 		{
 			MLog.e( "An error occured while attempting to save player file" );
 			ex.printStackTrace( System.out );
 			return MResult.RES_ERROR;
 		}
 	}
 
 	/**********************************************************************/
 	/**
 	 * Read the header of a chunk
 	 * @param in
 	 * @return
 	 */
 	private String readChunkHeader( DataInputStream in )
 	{
 		return readString( in, 6 );
 	}
 
 	/**
 	 * read a String from stream
 	 * @param in
 	 * @param bytes
 	 * @return
 	 */
 	private String readString( DataInputStream in, int bytes )
 	{
 		byte[] buf = new byte[ bytes ];
 		try
 		{
 			in.read( buf );
 			return new String( buf );
 		}
 		catch( IOException ioex )
 		{
 			MLog.e( "(fetchDB) Error while reading chars: " + ioex.getMessage() );
 			ioex.printStackTrace( System.out );
 			return "";
 		}
 	}
 
 	/**
 	 * Get an additional Chunk instance
 	 * @param <T>
 	 * @param c
 	 * @return
 	 */
 	public <T extends UVPlayerInfoChunk> T getAdditionalChunkInstance( Class<T> c )
 	{
 		try
 		{
 			return c.newInstance();
 		}
 		catch( InstantiationException | IllegalAccessException ex )
 		{
 			Logger.getLogger( UVLocalEngine.class.getName() ).log( Level.SEVERE, null, ex );
 		}
 		return null;
 	}
 
 
 	@Override
 	public UVPlayerInfo readPlayer( String playerName, boolean forceNewFile )
 	{
 		MLog.d( "Start fetching Player Info from player '" + playerName + "' ..." );
 
 		UVPlayerInfo resultInformation;
 		File ud = new File( pluginDirectory + UltraVisionAPI.userDataDir, playerName + ".usr" );
 		if( !ud.exists() )
 		{
 			if( !forceNewFile )
 			{
 				return null;
 			}
 
 			MLog.i( "File doesn't exist at " + MConfiguration.normalizePath( ud ) + ". Trying to create new one..." );
 			try
 			{
 				File uf = new File( pluginDirectory + UltraVisionAPI.userDataDir );
 				if( !uf.exists() )
 				{
 					uf.mkdirs();
 				}
 
 				ud.createNewFile();
 				MLog.d( "Created new file at " + MConfiguration.normalizePath( ud ) );
 			}
 			catch( IOException ioex )
 			{
 				MLog.e( "Can't create new file at " + MConfiguration.normalizePath( ud ) );
 				if( MConst._DEBUG_ENABLED )
 				{
 					ioex.printStackTrace( System.out );
 				}
 
 				return null;
 			}
 		}
 
 		// Check if the file exists but it is empty (means we recently created a new one)
 		if( ud.length() == 0 )
 		{
 			MLog.d( "File is empty." );
 
 			// then add Default player information chunks
 			resultInformation = new UVPlayerInfo();
 
 			// check if somehow already have a player with this name
 			boolean playerAlreadyLoaded = false;
 			MResult savePlayerResult = MResult.RES_SUCCESS;
 			for( UVLocalPlayer player : players )
 			{
 				if( player.getCraftPlayer().getName().equalsIgnoreCase( playerName ) )
 				{
 					// then set information to this data
 					resultInformation = player.i;
 					playerAlreadyLoaded = true;
 					break;
 				}
 			}
 
 			// otherwise add new player and save
 			if( !playerAlreadyLoaded )
 			{
 				Player bukkitPlayer;
 				if( null == ( bukkitPlayer = ultravisionPlugin.getServer().getPlayer( playerName ) ) )
 				{
 					bukkitPlayer = ultravisionPlugin.getServer().getOfflinePlayer( playerName ).getPlayer();
 				}
 
 				if( null == bukkitPlayer )
 				{
 					MLog.d( "Cannot create a new file for player '" + playerName + "' as he was never on the server." );
 					return null;
 				}
 
 				UVLocalPlayer newLocalPlayer = new UVLocalPlayer( bukkitPlayer, pluginDirectory, resultInformation);
 				players.add( newLocalPlayer );
 				savePlayerResult = savePlayer( playerName );
 			}
 
 			// Now print result
 			if( savePlayerResult == MResult.RES_SUCCESS )
 			{
 				MLog.i( "Created new Player Data File for player '" + playerName + "'" );
 			}
 			else
 			{
 				MLog.e( "Could not properly create new player data file for player '" + playerName + "'" );
 			}
 			return resultInformation;
 		}
 
 		// Get the Datastream for inputting data
 		DataInputStream fid;
 		try
 		{
 			fid = new DataInputStream( new FileInputStream( ud ) );
 		}
 		catch( FileNotFoundException fnfex )
 		{
 			MLog.e( "(fetchUD) Can't load UserData file: File not found" );
 			return null;
 		}
 
 		try
 		{
 			UVFileInformation fi = new UVFileInformation( UVFileInformation.uVersion );
 
 			resultInformation = new UVPlayerInfo();
 
 			String ch = "nochnk";
 			while( !ch.equalsIgnoreCase( "theend" ) )
 			{
 				ch = readChunkHeader( fid );
 				if( !ch.equalsIgnoreCase( "ouvplr" ) )
 				{
 					MLog.w( "User Data File damaged at " + MConfiguration.normalizePath( ud ) + ". Backup..." );
 					fid.close();
 					ud.renameTo( new File( pluginDirectory + UltraVisionAPI.userDataDir, playerName + ".dmg" ) );
 					return resultInformation;
 				}
 
 				ch = readChunkHeader( fid );
 				if( !ch.equalsIgnoreCase( "uvinfo" ) )
 				{
 					MLog.w( "User Data File damaged at " + MConfiguration.normalizePath( ud ) + ". Backup..." );
 					fid.close();
 					ud.renameTo( new File( pluginDirectory + UltraVisionAPI.userDataDir, playerName + ".dmg" ) );
 					return resultInformation;
 				}
 				else
 				{
 					fi.setVersion( fid.read() );
 					//MLog.d("File version is '" + fi.getVersion() + "' at " + MConfiguration.normalizePath(ud));
 				}
 
 				if( !( readString( fid, 16 ).trim() ).equalsIgnoreCase( playerName ) )
 				{
 					return null;
 				}
 
 				//MLog.d("Read Player [name = '" + nm + "'] ...");
 
 				int isMute = fid.read();
 				resultInformation.isMute = ( ( isMute == 0 ) ? false : true );
 
 				try
 				{
 					if( fi.getVersion() >= 3 )
 						resultInformation.lastOnline = new Time( fid.readLong() );
 				}
 				catch( EOFException eofex )
 				{
 					MLog.e( "File critically damaged at " + MConfiguration.normalizePath( ud ) + ". Backup..." );
 					fid.close();
 					ud.renameTo( new File( pluginDirectory + UltraVisionAPI.userDataDir, playerName + ".dmg" ) );
 					return resultInformation;
 				}
 				catch( Exception ex )
 				{
 					MLog.e( "File damaged at " + MConfiguration.normalizePath( ud ) + "!" );
 				}
 
 				resultInformation.onlineTime = new Time( fid.readLong() );
 				resultInformation.praise = fid.read();
 
 				// Now read chunks
 				boolean isPlayerChunk = true;
 				int to = 0;
 				while( isPlayerChunk && to < 1000 )
 				{
 					to++;
 					if( ( ch = readChunkHeader( fid ) ).equalsIgnoreCase( "theend" ) )
 					{
 						break;
 					}
 
 					if( ch.equalsIgnoreCase( "oprais" ) )
 					{
 						resultInformation.praiser.add( readString( fid, 16 ) );
 					}
 					else if( ch.equalsIgnoreCase( "nprais" ) )
 					{
 						continue;
 					}
 					else if( ch.equalsIgnoreCase( "theban" ) )
 					{
 						resultInformation.ban = new UVBan();
 						if( !resultInformation.ban.read( fid, fi ) )
 						{
 							resultInformation.ban = null;
 						}
 					}
 					else if( ch.equalsIgnoreCase( "oneban" ) )
 					{
 						UVBan b = new UVBan();
 						b.read( fid, fi );
 						resultInformation.banHistory.add( b );
 					}
 					else if( ch.equalsIgnoreCase( "nooban" ) )
 					{
 						continue;
 					}
 					else if( ch.equalsIgnoreCase( "thwarn" ) )
 					{
 						resultInformation.warning = new UVWarning();
 						if( !resultInformation.warning.read( fid, fi ) )
 						{
 							resultInformation.warning = null;
 						}
 					}
 					else if( ch.equalsIgnoreCase( "onwarn" ) )
 					{
 						UVWarning w = new UVWarning();
 						w.read( fid, fi );
 						resultInformation.warnHistory.add( w );
 					}
 					else if( ch.equalsIgnoreCase( "nowarn" ) )
 					{
 						continue;
 					}
 					else if( ch.equalsIgnoreCase( "onkick" ) )
 					{
 						UVKick k = new UVKick();
 						k.read( fid, fi );
 						resultInformation.kickHistory.add( k );
 					}
 					else if( ch.equalsIgnoreCase( "nokick" ) )
 					{
 						continue;
 					}
 					else if( ch.equalsIgnoreCase( "frireq" ) )
 					{
 						resultInformation.friendRequests.add( MStream.readString( fid, 16 ) );
 					}
 					else if( ch.equalsIgnoreCase( "friend" ) )
 					{
 						resultInformation.friends.add( MStream.readString( fid, 16 ) );
 					}
 					else if( ch.equalsIgnoreCase( "nofrie" ) )
 					{
 						continue;
 					}
 					else if( ch.equalsIgnoreCase( "onnote" ) )
 					{
 						resultInformation.notes.put( MStream.readString( fid, 16 ), MStream.readString( fid, 60 ) );
 					}
 					else if( ch.equalsIgnoreCase( "nonote" ) )
 					{
 						continue;
 					}
 					else
 					{
 						isPlayerChunk = false;
 					}
 				}
 
 				if( to >= 1000 )
 				{
 					MLog.e( "Whoops there was too much data in the base (Overflow)." );
 				}
 
 			}
 
 			// Add player recently read if not there already
 			boolean playerFound = false;
 			for( UVLocalPlayer player : players  )
 			{
 				if( player.getCraftPlayer().getName().equalsIgnoreCase( playerName ) )
 				{
 					playerFound = true;
 					break;
 				}
 			}
 
 			if( !playerFound )
 			{
 				Player bukkitPlayer = null;
 				if( null == ( bukkitPlayer = ultravisionPlugin.getServer().getPlayer( playerName ) ) )
 				{
 					bukkitPlayer = ultravisionPlugin.getServer().getOfflinePlayer( playerName ).getPlayer();
 				}
 
 				if( bukkitPlayer == null )
 					MLog.d( "Cannot add player to memory as he never has been on the server." );
 				else
 				{
 					UVLocalPlayer newPlayer = new UVLocalPlayer( bukkitPlayer, pluginDirectory, resultInformation );
 					players.add( newPlayer );
 					MLog.i( "Added new player named '" + playerName + "' to memory." );
 				}
 			}
 
 			fid.close();
 
 		}
 		catch( IOException ioex )
 		{
 			MLog.e( "Can't read user data file: " + ioex.getMessage() );
 			ioex.printStackTrace( System.out );
 			return null;
 		}
 
 		return resultInformation;
 	}
 
 	/**********************************************************************/
 	@Override
 	public void onPlayerJoin( Player p )
 	{
 		UVLocalPlayer localPlayer = getUVLocalPlayer( p.getName() );
 
 		// check if found - if not create a new one
 		if( localPlayer == null )
 		{
 			UVPlayerInfo localPlayerInfo = readPlayer( p.getName(), true );
 			localPlayer = new UVLocalPlayer( p, pluginDirectory, localPlayerInfo );
 			players.add( localPlayer );
 			savePlayer( localPlayer.getCraftPlayer().getName() );
 			MLog.d( "Added Player '" + p.getName() + "' to memory due to join!" );
 		}
 
 		if( localPlayer.i.lastOnline == null )
 		{
 			localPlayer.i.lastOnline = localPlayer.i.lastLogin;
 		}
 	}
 
 	/**********************************************************************/
 	@Override
 	public void onPlayerLogin( Player p )
 	{
 		if( p == null )
 		{
 			MLog.e( "Parameter p is null in UVLocalEngine.onPlayerLogin()!" );
 			return;
 		}
 
 		UVLocalPlayer localPlayer = getUVLocalPlayer( p.getName() );
 		if( localPlayer != null )
 		{
 			localPlayer.i.lastLogin = new Time( Calendar.getInstance().getTime().getTime() );
 			localPlayer.i.offline = false;
 		}
 		else
 		{
 			MLog.e( "Could not find logging in user '" + p.getName() + "'." );
 			MLog.e( "This seems to be a bug OR might be hacking!" );
 		}
 	}
 
 	/**********************************************************************/
 	@Override
 	public void onPlayerLeave( Player p )
 	{
 		if( p == null )
 		{
 			return;
 		}
 
 		UVLocalPlayer localPlayer = getUVLocalPlayer( p.getName() );
 		if( localPlayer != null )
 		{
 			Time t = new Time( Calendar.getInstance().getTime().getTime() );
 			localPlayer.i.lastOnline = t;
 			localPlayer.i.offline = true;
 			MResult res;
 			if( ( res = addPlayerOnlineTime( new Time( t.getTime() - localPlayer.i.lastLogin.getTime() ), p.getName() ) ) == MResult.RES_SUCCESS )
 			{
 				if( p.getAddress() != null )
 				{
 					localPlayer.log( "** Left successfully. (ip " + p.getAddress().toString() + ")" );
 				}
 			}
 			else
 			{
 				localPlayer.log( "[ERROR] ** Left with error: " + res.toString() );
 			}
 
 			localPlayer.quitlog();
 			savePlayer( p.getName() );
 		}
 		else
 		{
 			MLog.d( "Player never joined: " + p.getName() );
 		}
 	}
 
 	/**********************************************************************/
 	@Override
 	public Map<String, String> getAllPlayerInformation( String playerName )
 	{
 		throw new UnsupportedOperationException( "Not supported yet." );
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult setAuthorizer( MAuthorizer authorizer )
 	{
 		if( authorizer == null )
 		{
 			return MResult.RES_NOTGIVEN;
 		}
 
 		if( this.authorizer != null && this.authorizer.equals( authorizer ) )
 		{
 			return MResult.RES_ALREADY;
 		}
 
 		this.authorizer = authorizer;
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public MAuthorizer getAuthorizer()
 	{
 		return this.authorizer;
 	}
 
 	/**********************************************************************/
 	@Override
 	public boolean isAuthorizerEnabled()
 	{
 		return ( this.authorizer != null );
 	}
 
 	/**********************************************************************/
 	@Override
 	public boolean isPlayerBanned( String playerName )
 	{
 		UVPlayerInfo ui = getPlayerInfo( playerName );
 		if( ui != null && ui.ban != null )
 		{
 			MLog.d( "isBanned::getTimeRemain() = '" + ui.ban.getTimeRemain() + "'" );
 			if( ui.ban.getTimeRemain() == null )
 			{
 				return true;
 			}
 			if( ui.ban.getTimeRemain().getTime() <= 0 )
 			{
 				MLog.d( "reset ban as time <= 0" );
 				ui.ban = null;
 				return false;
 			}
 			else
 			{
 				return true;
 			}
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult banPlayerLocally( CommandSender cs, String playerName, String reason )
 	{
 		return banPlayer( cs, playerName, reason, false );
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult banPlayer( CommandSender cs, String playerName, String reason, boolean global )
 	{
 		return banPlayerTemporarily( cs, playerName, reason, null, global );
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult banPlayerTemporarily( CommandSender cs, String playerName, String reason, Time time, boolean global )
 	{
 		if( global )
 		{
 			MLog.w( "Can't global ban " + playerName + " in local mode. Set to local." );
 			global = false;
 		}
 
 		UVLocalPlayer localPlayer = getUVLocalPlayer( playerName );
 		if( localPlayer == null )
 		{
 			return MResult.RES_NOTINIT;
 		}
 
 		UVPlayerInfo localPlayerInformation = localPlayer.i;
 		if( authorizer.isRegistered( (Player)cs ) && !authorizer.loggedIn( (Player)cs ) )
 		{
 			return MResult.RES_NOACCESS;
 		}
 
 		// Check if this user is already banned
 		if( localPlayerInformation.ban != null )
 		{
 			return MResult.RES_ALREADY;
 		}
 
 		// Get the ban performer
 		UVLocalPlayer uPBanner = getUVLocalPlayer( cs.getName() );
 
 		// add the ban
 		localPlayerInformation.ban = new UVBan( reason, uPBanner.craftPlayer, global, time );
 		localPlayerInformation.banHistory.add( localPlayerInformation.ban );
 
 		// save player file
 		if( savePlayer( playerName ) != MResult.RES_SUCCESS )
 		{
 			MLog.e( "Can't save userdata for player '" + localPlayer.craftPlayer.getName() + "'" );
 			return MResult.RES_ERROR;
 		}
 
 		// Add ban to log
 		localPlayer.log( MLog.real( ChatColor.DARK_GRAY + "[UltraVision " + ChatColor.DARK_AQUA + ( ( time == null ) ? "B" : "Tempb" )
 			+ "an" + ChatColor.DARK_GRAY + "] " + ChatColor.AQUA + reason + " (local" + ( ( time == null ) ? "" : ", for " + timeInterpreter.getText( time.getTime() ) ) + ") BY " + uPBanner.craftPlayer.getName() ) );
 
 		// And kick player
 		localPlayer.craftPlayer.getHandle().playerConnection.player.extinguish();
 		localPlayer.craftPlayer.getHandle().playerConnection.sendPacket( new Packet255KickDisconnect( MLog.real( ChatColor.DARK_GRAY + "[UltraVision " + ChatColor.DARK_AQUA + ( ( time == null ) ? "B" : "Tempb" ) + "an" + ChatColor.DARK_GRAY + "] " + ChatColor.AQUA + reason + " (local" + ( ( time == null ) ? "" : ", for " + timeInterpreter.getText( time.getTime() ) ) + ")" ) ) );
 		localPlayer.craftPlayer.getHandle().playerConnection.networkManager.d();
 		( ( CraftServer ) localPlayer.craftPlayer.getServer() ).getHandle().disconnect( localPlayer.craftPlayer.getHandle() );
 		localPlayer.craftPlayer.getHandle().playerConnection.disconnected = true;
 
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult pardonPlayer( CommandSender cs, String playerName, String note )
 	{
 		if( authorizer.isRegistered( (Player) cs ) && !authorizer.loggedIn( (Player) cs ) )
 		{
 			return MResult.RES_NOACCESS;
 		}
 
 		UVLocalPlayer localPlayer = getUVLocalPlayer( playerName );
 		if( localPlayer == null )
 		{
 			return MResult.RES_NOTGIVEN;
 		}
 
 		UVPlayerInfo localPlayerInformation = localPlayer.i;
 
 		// Check if player is banned at all
 		if( localPlayerInformation.ban == null )
 		{
 			return MResult.RES_ALREADY;
 		}
 
 		// Prevent pardon from globalban (use pardonrequest instead)
 		if( localPlayerInformation.ban.isGlobal() )
 		{
 			return MResult.RES_NOTGIVEN;
 		}
 
 		localPlayerInformation.ban = null;
 		savePlayer( playerName );
 
 		MLog.i( "Player '" + playerName + "' pardoned by " + cs.getName() );
 
 		return MResult.RES_SUCCESS;
 
 	}
 
 	/**********************************************************************/
 	@Override
 	public UVBan getPlayerBan( String playerName, String serverName )
 	{
 		UVLocalPlayer localPlayer = getUVLocalPlayer( playerName );
 		if( localPlayer != null )
 		{
 			return localPlayer.i.ban;
 		}
 
 		return null;
 	}
 
 	/**********************************************************************/
 	@Override
 	public List<UVBan> getPlayerBans( String playerName )
 	{
 		List<UVBan> res = new ArrayList<>();
 		UVLocalPlayer localPlayer = getUVLocalPlayer( playerName );
 
 		if( localPlayer == null )
 			return res;
 
 		if( localPlayer.i.ban != null )
 		{
 			res.add( localPlayer.i.ban );
 		}
 
 		return res;
 	}
 
 	/**********************************************************************/
 	@Override
 	public List<UVBan> getPlayerBanHistory( String playerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return null;
 		}
 
 		return ( ( uP.i.banHistory != null ) ? uP.i.banHistory : new ArrayList<UVBan>() );
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult kickPlayer( CommandSender cs, String playerName, String reason )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return MResult.RES_NOTINIT;  // or RES_ALREADY
 		}
 
 		if( cs == null )
 			return MResult.RES_NOTINIT;
 
 		MLog.real( ChatColor.DARK_GRAY + "[UltraVision " + ChatColor.DARK_AQUA + "Kick" + ChatColor.DARK_GRAY + "] " + ChatColor.AQUA + reason + " BY " + cs.getName() );
 		uP.log( ChatColor.DARK_GRAY + "[UltraVision " + ChatColor.DARK_AQUA + "Kick" + ChatColor.DARK_GRAY + "] " + ChatColor.AQUA + reason + " BY " + cs.getName() );
 
 		( uP.getCraftPlayer() ).getHandle().playerConnection.player.extinguish();
 		( uP.getCraftPlayer() ).getHandle().playerConnection.sendPacket( new Packet255KickDisconnect( MLog.real( ChatColor.DARK_GRAY + "[UltraVision " + ChatColor.DARK_AQUA + "Kick" + ChatColor.DARK_GRAY + "] " + ChatColor.AQUA + reason ) ) );
 		( uP.getCraftPlayer() ).getHandle().playerConnection.networkManager.d();
 		( (CraftServer)uP.getCraftPlayer().getServer() ).getHandle().disconnect( uP.getCraftPlayer().getHandle() );
 		( uP.getCraftPlayer() ).getHandle().playerConnection.disconnected = true;
 
 		if( !( cs instanceof Player ) )
 		{
 			return MResult.RES_SUCCESS;
 		}
 
 		uP.quitlog();
 		uP.i.kickHistory.add( new UVKick( reason, ( Player ) cs, new Time( ( new Date() ).getTime() ) ) );
 		if( savePlayer( playerName ) != MResult.RES_SUCCESS )
 		{
 			MLog.e( "Can't save userdata for player '" + uP.craftPlayer.getName() + "'" );
 		}
 
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult kickPlayerHard( String playerName, String reason )
 	{
 		MLog.real( ChatColor.DARK_GRAY + "[UltraVision " + ChatColor.DARK_AQUA + "BackendKick" + ChatColor.DARK_GRAY + "] " + ChatColor.AQUA + reason );
 
 		// Save Log quit
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) != null )
 		{
 			uP.quitlog();
 		}
 
 		( uP.getCraftPlayer() ).getHandle().playerConnection.player.extinguish();
 		( uP.getCraftPlayer() ).getHandle().playerConnection.sendPacket( new Packet255KickDisconnect( MLog.real( ChatColor.DARK_GRAY + "[UltraVision " + ChatColor.DARK_AQUA + "Kick" + ChatColor.DARK_GRAY + "] " + ChatColor.AQUA + reason ) ) );
 		( uP.getCraftPlayer() ).getHandle().playerConnection.networkManager.d();
 		( ( CraftServer )uP.getCraftPlayer().getServer() ).getHandle().disconnect( uP.getCraftPlayer().getHandle() );
 		( uP.getCraftPlayer() ).getHandle().playerConnection.disconnected = true;
 
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public List<UVKick> getPlayerKickHistory( String playerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return null;
 		}
 
 		return ( ( uP.i.kickHistory != null ) ? uP.i.kickHistory : new ArrayList<UVKick>() );
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult warnPlayer( CommandSender cs, String playerName, String reason )
 	{
 		return warnPlayerTemporarily( cs, playerName, reason, null );
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult warnPlayerTemporarily( CommandSender cs, String playerName, String reason, Time time )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return null;
 		}
 
 		if( uP.i.warning != null )
 		{
 			return MResult.RES_ALREADY;
 		}
 
 		uP.i.warning = new UVWarning( reason, ( Player ) cs, false, time );
 
 		uP.log( "[UltraVision] warned by " + cs.getName() + ( time == null ? "" : "for" + timeInterpreter.getText( time.getTime() ) ) );
 
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult unwarnPlayer( CommandSender cs, String playerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return MResult.RES_NOTGIVEN;
 		}
 
 		if( uP.i.warning == null )
 		{
 			return MResult.RES_ALREADY;
 		}
 
 		uP.i.warning = null;
 
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public boolean isPlayerWarned( String playerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return false;
 		}
 
 		if( uP.i.warning == null )
 			return false;
 
 		if( uP.i.warning.getRemainingWarnTime() == null ) // Perma warn
 		{
 			return true;
 		}
 
 		if( uP.i.warning.getRemainingWarnTime().getTime() <= 0 )
 		{
 			uP.i.warning = null;
 			return false;
 		}
 
 		return true;
 	}
 
 	/**********************************************************************/
 	@Override
 	public String getPlayerWarnReason( String playerName )
 	{
 		if( !isPlayerWarned( playerName ) )
 		{
 			return "";
 		}
 
 		UVLocalPlayer uP = getUVLocalPlayer( playerName );
 
 		return uP.i.warning.getReason();
 	}
 
 	/**********************************************************************/
 	@Override
 	public UVWarning getPlayerWarning( String playerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return null;
 		}
 
 		return uP.i.warning;
 	}
 
 	/**********************************************************************/
 	@Override
 	public List<UVWarning> getPlayerWarnHistory( String playerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return null;
 		}
 
 		return ( ( uP.i.warnHistory != null ) ? uP.i.warnHistory : new ArrayList<UVWarning>() );
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult praisePlayer( CommandSender cs, String playerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return MResult.RES_NOTGIVEN;
 		}
 
 		if( cs instanceof ConsoleCommandSender )
 		{
 			return MResult.RES_NOACCESS;
 		}
 
 		if( uP.i.praiser.contains( cs.getName() ) )
 		{
 			return MResult.RES_ALREADY;
 		}
 
 		uP.i.praise++;
 		uP.i.praiser.add( cs.getName() );
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult unpraisePlayer( CommandSender cs, String playerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return MResult.RES_NOTGIVEN;
 		}
 
 		if( cs instanceof ConsoleCommandSender )
 		{
 			return MResult.RES_NOACCESS;
 		}
 
 		if( !uP.i.praiser.contains( cs.getName() ) )
 		{
 			return MResult.RES_ALREADY;
 		}
 
 		uP.i.praise--;
 		uP.i.praiser.remove( cs.getName() );
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public boolean isPlayerPraisedBy( String playerName, String otherPlayerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return false;
 		}
 
 		return uP.i.praiser.contains( otherPlayerName );
 	}
 
 	/**********************************************************************/
 	@Override
 	public int getPlayerPraiseCount( String playerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return -1;
 		}
 
 		return uP.i.praise;
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult addPlayerNote( CommandSender cs, String playerName, String note )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return MResult.RES_NOTGIVEN;
 		}
 
 		if( cs instanceof ConsoleCommandSender )
 		{
 			return MResult.RES_NOACCESS;
 		}
 
 		uP.i.notes.put( cs.getName(), note );
 
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult delPlayerNote( CommandSender cs, String playerName, int id )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return MResult.RES_NOTGIVEN;
 		}
 
 		if( cs instanceof ConsoleCommandSender )
 		{
 			return MResult.RES_NOACCESS;
 		}
 
 		if( uP.i.notes.size() <= id )
 		{
 			return MResult.RES_NOTINIT;
 		}
 
 		uP.i.notes.remove( String.valueOf( uP.i.notes.keySet().toArray()[id] ) );
 
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public Map<String, String> getPlayerNotes( String playerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return null;
 		}
 
 		if( uP.i.notes == null || uP.i.notes.isEmpty() )
 		{
 			return new HashMap<>();
 		}
 
 		return uP.i.notes;
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult mutePlayer( CommandSender cs, String playerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return MResult.RES_NOTGIVEN;
 		}
 
 		if( cs instanceof ConsoleCommandSender )
 		{
 			return MResult.RES_NOACCESS;
 		}
 
 		if( uP.i.isMute )
 		{
 			return MResult.RES_ALREADY;
 		}
 
 		uP.i.isMute = true;
 
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public boolean isPlayerMuted( String playerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return false;
 		}
 
 		return uP.i.isMute;
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult setPlayerOnlineTime( Time time, String playerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return MResult.RES_NOTGIVEN;
 		}
 
 		uP.i.onlineTime = time;
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult addPlayerOnlineTime( Time time, String playerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return MResult.RES_NOTGIVEN;
 		}
 
 		if( uP.i.onlineTime == null )
 		{
 			return MResult.RES_NOTINIT;
 		}
 
 		uP.i.onlineTime.setTime( uP.i.onlineTime.getTime() + time.getTime() );
 
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult subPlayerOnlineTime( Time time, String playerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return MResult.RES_NOTGIVEN;
 		}
 
 		if( uP.i.onlineTime == null )
 		{
 			return MResult.RES_NOTINIT;
 		}
 
 		uP.i.onlineTime.setTime( uP.i.onlineTime.getTime() - time.getTime() );
 
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public Time getPlayerOnlineTime( String playerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return null;
 		}
 
 		return uP.i.onlineTime;
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult addPlayerLogLine( String playerName, String message )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return MResult.RES_NOTGIVEN;
 		}
 
 		uP.log( message );
 
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult clearPlayerLog( String playerName )
 	{
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public List<String> getPlayerLog( String playerName, Time timefrom, Time timeto )
 	{
 		throw new UnsupportedOperationException( "Not supported yet." );
 	}
 
 	/**********************************************************************/
 	@Override
 	public List<String> getPlayerLog( String playerName, String pluginfilter )
 	{
 		throw new UnsupportedOperationException( "Not supported yet." );
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult rejectFriendship( String requestedPlayerName, String performingPlayerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( requestedPlayerName ) ) == null )
 		{
 			return MResult.RES_NOTGIVEN;
 		}
 
 		if( !uP.i.friendRequests.contains( performingPlayerName ) )
 		{
 			return MResult.RES_ALREADY;
 		}
 
 		uP.i.friendRequests.remove( performingPlayerName );
 
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult requestFriendship( String requestedPlayerName, String performingPlayerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( requestedPlayerName ) ) == null )
 		{
 			return MResult.RES_NOTGIVEN;
 		}
 
 		if( uP.i.friendRequests.contains( performingPlayerName ) )
 		{
 			return MResult.RES_ALREADY;
 		}
 
 		uP.i.friendRequests.add( performingPlayerName );
 
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult acceptFriendship( String requestedPlayerName, String performingPlayerName )
 	{
 		UVLocalPlayer performingPlayer;
 		UVLocalPlayer requestedPlayer;
 		if( ( performingPlayer = getUVLocalPlayer( performingPlayerName ) ) == null )
 		{
 			return MResult.RES_NOTGIVEN;
 		}
 
 		if( ( requestedPlayer = getUVLocalPlayer( requestedPlayerName ) ) == null )
 		{
 			return MResult.RES_NOTGIVEN;
 		}
 
 		if( performingPlayer.i.friends.contains( requestedPlayer.craftPlayer.getName() ) )
 		{
 			return MResult.RES_ALREADY;
 		}
 
 		performingPlayer.i.friends.add( requestedPlayer.craftPlayer.getName() );
 		requestedPlayer.i.friends.add( performingPlayer.craftPlayer.getName() );
 
 		performingPlayer.i.friendRequests.remove( requestedPlayerName );
 		requestedPlayer.i.friendRequests.remove( performingPlayerName );
 
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult delPlayerFriend( String requestedPlayerName, String perfomingPlayerName )
 	{
 		// Remove friendship on the side of the perfomring Player
 		UVLocalPlayer performingPlayer;
 		if( ( performingPlayer = getUVLocalPlayer( perfomingPlayerName ) ) == null )
 		{
 			return MResult.RES_NOTGIVEN;
 		}
 
 		if( performingPlayer.i.friends.contains( requestedPlayerName ) )
 		{
 			performingPlayer.i.friends.remove( requestedPlayerName );
 		}
 
 		// remove friendship on the side of the requested Player
 		UVLocalPlayer requestedPlayer;
 		if( ( requestedPlayer = getUVLocalPlayer( requestedPlayerName ) ) == null )
 		{
 			return MResult.RES_NOTGIVEN;
 		}
 
 		if( requestedPlayer.i.friends.contains( perfomingPlayerName ) )
 		{
 			requestedPlayer.i.friends.remove( perfomingPlayerName );
 		}
 
 		return MResult.RES_SUCCESS;
 	}
 
 	/**********************************************************************/
 	@Override
 	public List<String> getPlayerFriends( String playerName )
 	{
 		UVLocalPlayer uP;
 		if( ( uP = getUVLocalPlayer( playerName ) ) == null )
 		{
 			return null;
 		}
 
 		return ( ( uP.i.friends == null ) ? new ArrayList<String>() : uP.i.friends );
 	}
 
 	/**********************************************************************/
 	@Override
 	public MResult setPlayerProperty( String playerName, String prop )
 	{
 		return MResult.RES_SUCCESS;     // Shouldn't we better use notes for this?
 	}
 
 	/**********************************************************************/
 	@Override
 	public List<String> getPlayerProperties( String playerName )
 	{
 		return new ArrayList<>(); // Shouldn't we better use notes for this?
 	}
 }
