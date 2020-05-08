 package com.shaboozey.realms.util;
 
 public class Constants {
 
 	public final static String[] commands =
 	{
 			"/sr",
 			"/srcreate <name> <environment>",
 			"/srimport <name> <environment>",
 			"/srtp <dest> (name)",
 			"/srdelete <name>",
 			"/shart <name>",
 			"/srload <name>",
 			"/srmobs <worldname> <mob> <value>", 
 			"/srunload <name>",
 			"/srsetspawn",
 			"/srlist",
 			"/srinfo",
 			"/srwho <player>",
 	};
 	
 	public final static String[] descriptions =
 	{
 			"Lists all commands",
 			"Creates a new world",
 			"Imports a world",
 			"Teleports to a world",
 			"Deletes a world",
 			"Shart a player",
 			"Loads a world",
 			"Turn mobs on/off in a world",
 			"Unloads a world",
 			"Sets the world spawn",
 			"Lists all the worlds",
 			"Provides info about the world",
			"Shows world name of a player"
 	};
 	
 	public final static String[] errorMessages =
 	{
 			"Insufficent permissions!",
 			"Invalid arguments: /srcreate <worldname> <worldtype>",
 			"Cannot create this world as it already exists.",
 			"Cannot create this world as it the default world!",
 			
 			"Invalid arguments: /srdelete <worldname>",
 			"Cannot delete this world as it doesn't exist",
 			"Cannot delete this world as it the default world!",
 			
 			"Invalid arguments: /sr",
 			
 			"Invalid arguments: /srimport <worldname> <worldtype>",
 			"Cannot import this world as there is no map data.",
 			"Cannot import this world as it the default world!",
 			
 			"Invalid arguments: /srinfo <worldname>",
 			"No world configuration exists!",
 			
 			"Invalid arguments: /srlist <worldname>",
 			
 			"Invalid arguments: /srload <worldname>",
 			"Cannot load this world as it does not exist!",
 			"No config exists for this world... try /srcreate <name> <environment>",
 			"Cannot load this world as it the default world!",
 			"This world is already loaded!",
 			
 			"Invalid arguments: /srmobs <worldname> <mob> <value>",
 			"Invalid arguments: /srmobs <worldname> <mob> <value>",
 			"That is not a valid mob type",
 			
 			"Consoles cannot set the spawn for worlds!",
 			"Invalid arguments: /srsetspawn",
 			
 			"Invalid arguments: /shart <name>... someone needs to shart themselves.",
 			
 			"Invalid arguments: /srtp <dest> (name)",
 			"The console cannot teleport itself on the server.",
 			
 			"Invalid arguments: /srunload <worldname>",
 			"Cannot unload this world as it doesn't exist",
 			"Cannot unload this world as it is already unloaded!",
 			"Cannot unload this world as it the default world!",
 			
 			"Invalid arguments: /srwho <player>",
 			
 		
 	};
 	
 	
 	public final static String[] permissions =
 	{
 			"srealms.create",
 			"srealms.delete",
 			"srealms.help",
 			"srealms.import",
 			"srealms.info",
 			"srealms.list",
 			"srealms.load",
 			"srealms.mobs",
 			"srealms.setspawn",
 			"srealms.shart",
 			"srealms.teleport.others",
 			"srealms.teleport",
 			"srealms.unload",
 			"srealms.who",
 			
 	};
 }
