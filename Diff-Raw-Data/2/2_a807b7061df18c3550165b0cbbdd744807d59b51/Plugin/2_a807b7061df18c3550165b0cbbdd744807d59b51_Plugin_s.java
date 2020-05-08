 // Copyright (C) 2011 by Danny de Jong
 //
 // Permission is hereby granted, free of charge, to any person obtaining a copy
 // of this software and associated documentation files (the "Software"), to deal
 // in the Software without restriction, including without limitation the rights
 // to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 // copies of the Software, and to permit persons to whom the Software is
 // furnished to do so, subject to the following conditions:
 // 
 // The above copyright notice and this permission notice shall be included in
 // all copies or substantial portions of the Software.
 // 
 // THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 // IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 // FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 // AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 // LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 // OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 // THE SOFTWARE.
 
 package com.dannycrafts.myTitles;
 
 import java.io.*;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.dannycrafts.myTitles.Title.InvalidNameException;
 
 public class Plugin extends JavaPlugin {
 	
 	public String usagePluginId = "";
 	
 	private PlayerListener playerListener = new PlayerListener( this );
 	protected MyTitles mainInterface = getInterface( "" );
 	
 	protected void copyFile( File original, File copy ) throws IOException, FileNotFoundException
 	{
 		FileInputStream fis = new FileInputStream( original );
 		FileOutputStream fos = new FileOutputStream( copy );
 		
 		byte[] buffer = new byte[1024];
 		while ( true )
 		{
 			int read = fis.read( buffer );
 			if ( read < 1024 )
 				break;
 			
 			fos.write( buffer );
 		}
 		
 		fos.close();
 		fis.close();
 	}
 	
 	protected String format( String message, String varName, String value )
 	{
 		return message.replace( "$" + varName + "$", value );
 	}
 	
 	public MyTitles getInterface( String usagePluginId )
 	{
 		return new MyTitles( this, usagePluginId );
 	}
 	
 	public boolean onCommand( CommandSender sender, Command cmd, String commandLabel, String[] args )
 	{
 		org.bukkit.entity.Player player = null;
 		if ( sender instanceof org.bukkit.entity.Player ) player = (org.bukkit.entity.Player)sender;
 		
 		if ( args.length == 0 )
 		{
 			if ( sender.hasPermission( "mytitles.player.list" ) )
 			{
 				try
 				{
 					Player myTitlesPlayer = mainInterface.getPlayer( player );
 					ArrayList<Title> titles = myTitlesPlayer.getOwnedTitles();
 					if ( titles.size() != 0 )
 					{
 						for ( int i = 0; i < titles.size(); i++ )
 						{
 							Title.Info titleInfo = titles.get( i ).getInfo();
 							sender.sendMessage( "#" + ( i + 1 ) + ": " + titleInfo.name + ": \"" + myTitlesPlayer.getDisplayName( titleInfo.affixes ) + "\"" );
 						}
 					}
 					else
 						sendMessage( sender, Messages.noTitles );
 				}
 				catch ( Player.DoesntExistException e )
 				{
 					printError( e, "to list titles for player \"" + sender.getName() + "\"" );
 					sendMessage( sender, Messages.internalError );
 				}
 				catch ( SQLException e )
 				{
 					printSqlError( e, "to list titles for player \"" + sender.getName() + "\"" );
 					sendMessage( sender, Messages.internalError );
 				}
 			}
 			else if ( player == null )
 				sender.sendMessage( this.getDescription().getName() + " v" + this.getDescription().getVersion() );
 			else
 				sendMessage( sender, Messages.noPermissions );
 		}
 		else if ( args.length > 0 )
 		{
 			if ( args[0].equalsIgnoreCase( "use" ) )
 			{
 				if ( sender.hasPermission( "mytitles.player.use" ) )
 				{
 					if ( args.length >= 2 )
 					{
 						try
 						{
 							int affected = Database.update( "UPDATE players SET title_id = ( SELECT id FROM titles WHERE name = '" + args[1] + "' ) WHERE name = '" + player.getName() + "';" );
 							if ( affected == 0 )
 								sendMessage( sender, format( Messages.noTitle, "title_name", args[1] ) );
 							else
 								sendMessage( sender, format( format( Messages.titleUse, "display_name",  mainInterface.getPlayer( player ).getDisplayName() ), "title_name", args[1] ) );
 						}
 						catch ( Player.DoesntExistException e )
 						{
 							printError( e, "to list titles for player \"" + player.getName() + "\"" );
 						}
 						catch ( SQLException e )
 						{
 							printSqlError( e, "to list titles for player \"" + player.getName() + "\"" );
 							sendMessage( sender, Messages.internalError );
 						}
 					}
 					else
 						sender.sendMessage( commandLabel + " \"use\" title_name" );
 				}
 				else if ( player == null )
 					sendMessage( sender, Messages.commandPlayerOnly );
 				else
 					sendMessage( sender, Messages.noPermissions );
 			}
 			
 			else if ( args[0].equalsIgnoreCase( "usenr" ) )
 			{
 				if ( sender.hasPermission( "mytitles.player.use" ) )
 				{
 					if ( args.length >= 2 )
 					{
 						try
 						{
 							int selectNr = Integer.parseInt( args[1] );
 							ArrayList<Title> titles = mainInterface.getPlayer( player ).getOwnedTitles();
 							if ( selectNr > 0 && selectNr <= titles.size() )
 							{
 								Title selectedTitle = titles.get( selectNr - 1 );
 								
 								int affected = Database.update( "UPDATE players SET title_id = " + selectedTitle.id + " WHERE name = '" + player.getName() + "';" );
 								if ( affected == 0 )
 									sendMessage( sender, format( Messages.noTitle, "title_name", selectedTitle.getName() ) );
 								else
 									sendMessage( sender, format( format( Messages.titleUse, "display_name",  mainInterface.getPlayer( player ).getDisplayName() ), "title_name", selectedTitle.getName() ) );
 							}
 							else
 								player.sendMessage( "Number is out of range." );
 						}
 						catch ( Player.DoesntExistException e )
 						{
 							printError( e, "to list titles for player \"" + player.getName() + "\"" );
 						}
 						catch ( SQLException e )
 						{
 							printSqlError( e, "to list titles for player \"" + player.getName() + "\"" );
 							sendMessage( sender, Messages.internalError );
 						}
 					}
 					else
 						sender.sendMessage( commandLabel + " \"usenr\" title_number" );
 				}
 				else if ( player == null )
 					sendMessage( sender, Messages.commandPlayerOnly );
 				else
 					sendMessage( sender, Messages.noPermissions );
 			}
 			
 			else if ( args[0].equalsIgnoreCase( "register" ) )
 			{
 				if ( player == null || sender.hasPermission( "mytitles.admin.register" ) )
 				{
 					if ( args.length >= 3 )
 					{
 						if ( args[2].contains( ":" ) )
 						{
 							String prefix = args[2].substring( 0, args[2].indexOf( ':' ) );
 							String postfix = args[2].substring( prefix.length() + 1 );
 								
 							try
 							{
 								mainInterface.registerTitle( args[1], prefix, postfix );
 								sendMessage( sender, format( Messages.titleRegistered, "title_name", args[1] ) );
 							}
 							catch ( Title.AlreadyExistsException e )
 							{
 								sendMessage( sender, format( Messages.titleExists, "title_name", args[1] ) );
 							}
 							catch (InvalidNameException e)
 							{
 								sendMessage( sender, format( Messages.invalidTitleName, "name", args[1] ) );
 							}
 							catch ( SQLException e )
 							{
 								printSqlError( e, "to register title \"" + args[1] + "\"" );
 								sendMessage( sender, Messages.internalError );
 							}
 						}
 						else
 							sender.sendMessage( commandLabel + " \"register\" title_name prefix \":\" postfix" );
 					}
 					else
 						sender.sendMessage( commandLabel + " \"register\" title_name prefix \":\" postfix" );
 				}
 				else
 					sendMessage( sender, Messages.noPermissions );
 			}
 			
 			else if ( args[0].equalsIgnoreCase( "unregister" ) )
 			{
 				if ( player == null || sender.hasPermission( "mytitles.admin.register" ) )
 				{
 					if ( args.length >= 2 )
 					{
 						try
 						{
 							mainInterface.unregisterTitle( args[1] );
 							sendMessage( sender, format( Messages.titleUnregistered, "title_name", args[1] ) );
 						}
 						catch ( Title.DoesntExistException e )
 						{
 							sendMessage( sender, format( Messages.titleNotExists, "title_name", args[1] ) );
 						}
 						catch ( SQLException e )
 						{
 							printSqlError( e, "to unregister title \"" + args[1] + "\"" );
 							sendMessage( sender, Messages.internalError );
 						}
 					}
 					else
 						sender.sendMessage( commandLabel + " \"unregister\" title_name" );
 				}
 				else
 					sendMessage( sender, Messages.noPermissions );
 			}
 			
 			else if ( args[0].equalsIgnoreCase( "give" ) )
 			{
 				if ( player == null || sender.hasPermission( "mytitles.admin.distribute" ) )
 				{
 					if ( args.length >= 3 )
 					{
 						try
 						{
 							mainInterface.getPlayer( args[1] ).giveTitle( mainInterface.getTitle( args[2] ) );
 
 								
 							sender.sendMessage( format( format( Messages.titleGiven, "title_name", args[2] ), "player_name", args[1] ) );
 						}
 						catch ( Player.DoesntExistException e )
 						{
 							sendMessage( sender, format( Messages.playerNotExists, "player_name", args[1] ) );
 						}
 						catch ( Title.DoesntExistException e )
 						{
 							sendMessage( sender, format( Messages.titleNotExists, "title_name", args[2] ) );
 						}
 						catch ( Player.AlreadyOwnsTitleException e )
 						{
 							sendMessage( sender, format( format( Messages.playerOwnsTitle, "title_name", args[2] ), "player_name", args[1] ) );
 						}
 						catch ( SQLException e )
 						{
 							printSqlError( e, "to give title \"" + args[2] + "\" to player \"" + args[1] + "\"" );
 							sendMessage( sender, Messages.internalError );
 						}
 					}
 					else
 						sender.sendMessage( commandLabel + " \"give\" player_name title_name" );
 				}
 				else
 					sendMessage( sender, Messages.noPermissions );
 			}
 			
 			else if ( args[0].equalsIgnoreCase( "take" ) )
 			{
 				if ( player == null || sender.hasPermission( "mytitles.admin.distribute" ) )
 				{
 					if ( args.length >= 3 )
 					{ 
 						try
 						{
 							mainInterface.getPlayer( args[1] ).takeTitle( mainInterface.getTitle( args[2] ) );
 							
 							sender.sendMessage( format( format( Messages.titleTaken, "title_name", args[2] ), "player_name", args[1] ) );
 						}
 						catch ( Player.DoesntExistException e )
 						{
 							sendMessage( sender, format( Messages.playerNotExists, "player_name", args[1] ) );
 						}
 						catch ( Title.DoesntExistException e )
 						{
 							sendMessage( sender, format( Messages.titleNotExists, "title_name", args[2] ) );
 						}
 						catch ( Player.DoesntOwnTitleException e )
 						{
 							sendMessage( sender, format( format( Messages.playerNotOwnsTitle, "title_name", args[2] ), "player_name", args[1] ) );
 						}
 						catch ( SQLException e )
 						{
 							printSqlError( e, "to take title \"" + args[2] + "\" to player \"" + args[1] + "\"" );
 							sendMessage( sender, Messages.internalError );
 						}
 					}
 					else
 						sender.sendMessage( commandLabel + " \"take\" player_name title_name" );
 				}
 			}
 			
 			else
 				sender.sendMessage( "mt \"help\"" );
 		}
 		
 		return true;
 	}
 	
 	public void onDisable() {
 		
 		try {
 			
 			Database.disconnect();
 			
 			print( "Gracefully disabled." );
 		}
 		catch ( Exception e ) {
 			
 			printError( e, "enabling MyTitles" );
 		}
 	}
 	
 	public void onEnable() {
 		
 		try {
 			
 			// Load MySQL db driver
 			Class.forName("com.mysql.jdbc.Driver");
 			
 			// TODO: Install config.yml
 
 			// Load config.yml
 			Configuration config = new Configuration( new File( this.getDataFolder() + "/config.yml" ) );
 			config.load();
 			
 			String sqlHost = config.getString( "sql_host", "localhost" );
 			String sqlPort = config.getString( "sql_port", "3306" );
 			String sqlUsername = config.getString( "sql_username", "root" );
 			String sqlPassword = config.getString( "sql_password", "" );
 			String sqlDatabase = config.getString( "sql_database", "my_titles" );
 
 			Messages.commandPlayerOnly = config.getString( "message_command_player_only", null );
 			Messages.internalError = config.getString( "message_internal_error", null );
 			Messages.invalidTitleName = config.getString( "message_invalid_title_name", null );
 			Messages.noPermissions = config.getString( "message_no_permissions", null );
 			Messages.noTitle = config.getString( "message_no_title", null );
 			Messages.noTitles = config.getString( "message_no_titles", null );
 			Messages.playerOwnsTitle = config.getString( "message_player_owns_title", null );
 			Messages.playerNotExists = config.getString( "message_player_not_exists", null );
 			Messages.playerNotOwnsTitle = config.getString( "message_player_not_owns_title", null );
 			Messages.titleExists = config.getString( "message_title_exists", null );
 			Messages.titleGiven = config.getString( "message_title_given", null );
 			Messages.titleNotExists = config.getString( "message_title_not_exists", null );
 			Messages.titleRegistered = config.getString( "message_title_registered", null );
 			Messages.titleTaken = config.getString( "message_title_taken", null );
 			Messages.titleUnregistered = config.getString( "message_title_unregistered", null );
 			Messages.titleUse = config.getString( "message_title_use", null );
 			
 			// Connect to database:
 			Database.connect( sqlHost, sqlPort, sqlDatabase, sqlUsername, sqlPassword );
 			
 			installDatabase();
 			
 			// Register events:
 			PluginManager pluginManager = this.getServer().getPluginManager();
 			pluginManager.registerEvent( Type.PLAYER_JOIN, playerListener, Priority.Normal, this );
 			pluginManager.registerEvent( Type.PLAYER_QUIT, playerListener, Priority.Normal, this );
 			pluginManager.registerEvent( Type.PLAYER_CHAT, playerListener, Priority.Highest, this );
 			
 			print( "Enabled." );
 		}
 		catch ( SQLException e )
 		{
 			printSqlError( e, "enable MyTitles" );
 			print( "This plugin has not been enabled due to errors, did you configure the database connection details correctly?" );
 		}
 		catch ( Exception e ) {
 			
 			printError( e, "enable MyTitles" );
 			print( "This plugin has not been enabled due to errors." );
 		}
 	}
 	
 	public void installDatabase() throws SQLException
 	{
 		try
 		{
 			Database.update( "CREATE TABLE collections (" +
 					"id bigint unsigned NOT NULL AUTO_INCREMENT," +
 					"player_id bigint unsigned NOT NULL," +
 					"title_id bigint unsigned DEFAULT NULL," +
 					"title_variation_id BIGINT UNSIGNED," +
 					"PRIMARY KEY (id)," +
 					"UNIQUE KEY uni (player_id,title_id)" +
 				");"
 			);
 		}
 		catch ( SQLException e )
 		{
 			if ( e.getErrorCode() != 1050 )
 				throw e;
 		}
 		
 		try
 		{
 			Database.update( "CREATE TABLE players (" +
 					"id bigint unsigned NOT NULL AUTO_INCREMENT," +
 					"name varchar(16) NOT NULL," +
					"title_id bigint unsigned NOT NULL," +
 					"PRIMARY KEY (id)," +
 					"UNIQUE KEY uni (name)" +
 				");"
 			);
 		}
 		catch ( SQLException e )
 		{
 			if ( e.getErrorCode() != 1050 )
 				throw e;
 		}
 		
 		try
 		{			
 			Database.update( "CREATE TABLE titles (" +
 					"id bigint unsigned NOT NULL AUTO_INCREMENT," +
 					"plugin_id varchar(32) NOT NULL DEFAULT ''," +
 					"name varchar(16) NOT NULL," +
 					"prefix varchar(32) DEFAULT NULL," +
 					"suffix varchar(32) DEFAULT NULL," +
 					"PRIMARY KEY (id)," +
 					"UNIQUE KEY uni (name)" +
 				");"
 			);
 		}
 		catch ( SQLException e )
 		{
 			if ( e.getErrorCode() != 1050 )
 				throw e;
 		}
 		
 		try
 		{
 			Database.update( "CREATE TABLE title_variations (" +
 					"id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT," +
 					"title_id BIGINT UNSIGNED NOT NULL," +
 					"name VARCHAR(16) NOT NULL," +
 					"prefix VARCHAR(32)," +
 					"suffix VARCHAR(32)," +
 					"CONSTRAINT PRIMARY KEY( id )," +
 					"CONSTRAINT UNIQUE uni ( title_id, name )" +
 				");"
 			);
 		}
 		catch ( SQLException e )
 		{
 			if ( e.getErrorCode() != 1050 )
 				throw e;
 		}
 	}
 	
 	public void onLoad() {
 		
 		print( "v" + this.getDescription().getVersion() );
 	}
 	
 	public void print( String message ) {
 		
 		System.out.println( "[" + this.getDescription().getName() + "] " + message );
 	}
 	
 	public void printError( Exception e, String whatFailed ) {
 		
 		print( "Failed " + whatFailed + ": " + e.getMessage() + "." );
 		e.printStackTrace();
 	}
 	
 	public void printSqlError( SQLException e, String whatFailed ) {
 		
 		print( "[SQL Error " + e.getErrorCode() + "] Failed " + whatFailed + ": " + e.getMessage() + "." );
 		e.printStackTrace();
 	}
 	
 	protected void sendMessage( CommandSender receiver, String message )
 	{
 		if ( message != null )
 			receiver.sendMessage( message );
 	}
 	
 	protected static class Messages
 	{
 		public static String commandPlayerOnly;
 		public static String internalError;
 		public static String invalidTitleName;
 		public static String noPermissions;
 		public static String noTitle;
 		public static String noTitles;
 		public static String playerOwnsTitle;
 		public static String playerNotExists;
 		public static String playerNotOwnsTitle;
 		public static String titleExists;
 		public static String titleGiven;
 		public static String titleNotExists;
 		public static String titleRegistered;
 		public static String titleTaken;
 		public static String titleUnregistered;
 		public static String titleUse;
 	}
 }
