 package me.eighth.suitcase.util;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.logging.Logger;
 
 import me.eighth.suitcase.Suitcase;
 
 public class SuitcaseConsole {
 	
 	private Suitcase plugin;
 	private final Logger mcLogger = Logger.getLogger("Minecraft");
 	
 	public enum actionType {
 		
 		// plugin startup
 		PLUGIN_ENABLE_START,
 		PLUGIN_ENABLE_FINISH,
 		PLUGIN_ENABLE_ERROR,
 		
 		// plugin reload
 		PLUGIN_RELOAD_START,
 		PLUGIN_RELOAD_FINISH,
 		PLUGIN_RELOAD_ERROR,
 		
 		// plugin unload
 		PLUGIN_DISABLE_START,
 		PLUGIN_DISABLE_FINISH,
 		PLUGIN_DISABLE_ERROR,
 		
 		// player command
 		PLAYER_COMMAND_EXECUTED,
 		PLAYER_COMMAND_DENIED,
 		PLAYER_COMMAND_INVALID,
 		PLAYER_COMMAND_ERROR,
 		
 		// config property handling
 		PROPERTY_MISSING,
 		PROPERTY_BAD_TYPE,
 		
 		// file handling
 		FILE_NOT_FOUND,
 		FILE_SAVE_ERROR,
 		
 		// other actions
 		ARGUMENTS_INVALID,
 		TYPE_NOT_HANDLED
 		
 	}
 	
 	public SuitcaseConsole(Suitcase plugin) {
 		this.plugin = plugin;
 	}
 
 	private boolean checkArguments(actionType action, ArrayList<String> arguments, int size) {
 		if (arguments.size() == size) {
 			return true;
 		}
 		else {
 			sendAction(actionType.ARGUMENTS_INVALID, new ArrayList<String>(Arrays.asList(action.toString(), plugin.message.getString(arguments, true))));
 			return false;
 		}
 	}
 	
 	public void sendAction(actionType action) {
 		sendAction(action, new ArrayList<String>());
 	}
 	
 	public void sendAction(actionType action, ArrayList<String> arguments) {
 		// log actions to console
 		switch (action) {
 		
 			// no arguments
 		case PLUGIN_ENABLE_START:
 			if (!checkArguments(action, arguments, 0)) break;
 			mcLogger.info(plugin.tag + plugin.name + " " + plugin.getDescription().getFullName() + " by " + plugin.message.getString(plugin.getDescription().getAuthors(), true) + " enabling...");
 			break;
 			// argument format 0 -> 'errorName'
 		case PLUGIN_ENABLE_ERROR:
 			if (!checkArguments(action, arguments, 1)) break;
 			mcLogger.severe(plugin.tag + "Error '" + arguments.get(0) + "' occured while enabling plugin!");
 			break;
 			// no arguments
 		case PLUGIN_ENABLE_FINISH:
 			if (!checkArguments(action, arguments, 0)) break;
 			mcLogger.info(plugin.tag + "Suitcase successfully enabled.");
 			break;
 
 			
 			// no arguments
 		case PLUGIN_RELOAD_START:
 			if (!checkArguments(action, arguments, 0)) break;
 			mcLogger.info(plugin.tag + "Reloading plugin...");
 			break;
 			// argument format 0 -> 'errorName'
 		case PLUGIN_RELOAD_ERROR:
 			if (!checkArguments(action, arguments, 1)) break;
 			mcLogger.severe(plugin.tag + "Error '" + arguments.get(0) + "' occured while reloading plugin!");
 			break;
 			// no arguments
 		case PLUGIN_RELOAD_FINISH:
 			if (!checkArguments(action, arguments, 0)) break;
 			mcLogger.info(plugin.tag + "Suitcase successfully reloaded.");
 			break;
 				
 			
 			// no arguments
 		case PLUGIN_DISABLE_START:
 			if (!checkArguments(action, arguments, 0)) break;
 			mcLogger.info(plugin.tag + "Disabling plugin...");
 			break;
 			// argument format 0 -> 'errorName'
 		case PLUGIN_DISABLE_ERROR:
 			if (!checkArguments(action, arguments, 1)) break;
 			mcLogger.severe(plugin.tag + "Error '" + arguments.get(0) + "' occured while disabling plugin!");
 			break;
 			// no arguments
 		case PLUGIN_DISABLE_FINISH:
 			if (!checkArguments(action, arguments, 0)) break;
 			mcLogger.info(plugin.tag + "Suitcase successfully disabled.");
 			break;
 			
 
 			// argument format 0 -> 'Player'
 			// argument format 1 -> '/suitcase help rate'
 		case PLAYER_COMMAND_EXECUTED:
 			if (!checkArguments(action, arguments, 2)) break;
 			mcLogger.info(plugin.tag + "[PLAYER_COMMAND] '" + arguments.get(0) + "' used command '" + arguments.get(1) + "'.");
 			break;
 			// argument format ^
 		case PLAYER_COMMAND_DENIED:
 			if (!checkArguments(action, arguments, 2)) break;
 			mcLogger.info(plugin.tag + "[PLAYER_COMMAND] '" + arguments.get(0) + "' was denied command '" + arguments.get(1) + "'.");
 			break;
 			// argument format ^
 		case PLAYER_COMMAND_INVALID:
 			if (!checkArguments(action, arguments, 2)) break;
 			mcLogger.info(plugin.tag + "[PLAYER_COMMAND] '" + arguments.get(0) + "' tried invalid command '" + arguments.get(1) + "'.");
 			break;
 			// argument format ^
 			// argument format 2 -> 'errorName'
 		case PLAYER_COMMAND_ERROR:
 			if (!checkArguments(action, arguments, 3)) break;
 			mcLogger.warning(plugin.tag + "[PLAYER_COMMAND] '" + arguments.get(0) + "' caused error '" + arguments.get(2) + "' by executing '" + arguments.get(1) + "'!");
 			break;
 			
 			
 			// argument format 0 -> 'property.path'
 			// argument format 1 -> 'filename.ext'
 			// argument format 2 -> 'default value'
 		case PROPERTY_MISSING:
 			if (!checkArguments(action, arguments, 3)) break;
			mcLogger.warning(plugin.tag + "Missing property '" + arguments.get(0) + "' in '" + arguments.get(1) + "'! Set to default: " + arguments.get(2));
 			break;
 			// argument format ^
 			// argument format 2 -> 'type'
 			// argument format 3 -> 'expected type'
 		case PROPERTY_BAD_TYPE:
 			if (!checkArguments(action, arguments, 4)) break;
 			mcLogger.warning(plugin.tag + "Bad type of '" + arguments.get(0) + "' in '" + arguments.get(1) + "'! Parsed " + arguments.get(2) + " instead of " + arguments.get(3));
 			break;
 			
 			// argument format 0 -> 'filename.ext'
 		case FILE_NOT_FOUND:
 			if (!checkArguments(action, arguments, 1)) break;
 			mcLogger.warning(plugin.tag + "File '" + arguments.get(0) + "' was not found. Attempting to create default...");
 			break;
 			// argument format ^
 			// argument format 1 -> 'error type'
 		case FILE_SAVE_ERROR:
 			if (!checkArguments(action, arguments, 2)) break;
 			mcLogger.severe(plugin.tag + "Error while saving '" + arguments.get(0) + "'!");
 			break;
 			
 			// argument format 0 -> 'ACTION_TYPE' 
 			// argument format 1 -> 'argument1, argument2'
 		case ARGUMENTS_INVALID:
 			if (!checkArguments(action, arguments, 2)) break; // this could cause an infinite loop of errors
 			mcLogger.severe(plugin.tag + "Action '" + arguments.get(0) + "' was passed an invalid amount of arguments: '" + arguments.get(1) + "'!");
 			break;
 			// argument format ^
 		case TYPE_NOT_HANDLED:
 			if (!checkArguments(action, arguments, 2)) break;
 			mcLogger.severe(plugin.tag + "Type '" + action.toString() + "' was not handled! Arguments: '" + plugin.message.getString(arguments, true) + "'");
 			break;
 			
 			
 			// type was not handled
 		default:
 			arguments.add(0, action.toString());
 			sendAction(actionType.TYPE_NOT_HANDLED, arguments);
 			plugin.disable(plugin);
 			return;
 		}
 	}
 	
 }
