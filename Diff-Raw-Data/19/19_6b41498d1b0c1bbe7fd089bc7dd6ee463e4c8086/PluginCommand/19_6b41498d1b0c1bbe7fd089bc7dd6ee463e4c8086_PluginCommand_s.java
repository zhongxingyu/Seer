 package roujo.emily.plugins.basic.commands;
 
 import java.io.File;
 
 import roujo.emily.core.MessageContext;
 import roujo.emily.core.commands.Command;
 import roujo.emily.core.extensibility.PluginManager;
 
 public class PluginCommand extends Command {	
 	public PluginCommand() {
 		super("plugin", "plugin (?<args>.*)", "Manages plugins", "plugin (load|unload|reload) pluginName", true);
 	}
 	
 	@Override
 	public boolean execute(MessageContext context) {
 		// Validating arguments
 		String rawArguments = getArguments(context);
 		if(rawArguments == "") {
 			sendUsageBack(context);
 			return false;
 		}
 		
 		String[] arguments = rawArguments.split(" ");
 		if(arguments.length < 2) {
 			sendUsageBack(context);
 			return false;
 		}
 		
 		switch(arguments[0].toLowerCase()) {
 		case "load":
 			if(arguments.length != 3) {
 				sendUsageBack(context);
 				return false;
 			}
 			return loadPlugin(context, arguments);
 		case "unload":
 			return unloadPlugin(context, arguments);
 		case "reload":
			if(arguments.length != 3) {
				sendUsageBack(context);
				return false;
			}
			return unloadPlugin(context, arguments) && loadPlugin(context, arguments);
 		default:
 			sendUsageBack(context);
 			return false;
 		}
 	}
 
 	private boolean loadPlugin(MessageContext context, String[] arguments) {
 		String pluginName = arguments[1];
 		String pluginFileName = arguments[2];
 		File pluginFile = new File("plugins/" + pluginFileName);
 		if(!pluginFile.exists()) {
 			sendMessageBack(context, "The file " + pluginFileName + " isn't in the \"plugins\" directory.");
 			return false;
 		}
 		
 		// So the file exists, let's try to load it
 		sendMessageBack(context, "Loading plugin " + pluginName + " from " + pluginFileName + "...");
 		if(PluginManager.getInstance().loadPlugin(pluginName, pluginFile)) {
 			sendMessageBack(context, "Plugin " + pluginName + " loaded!");
 			return true;
 		} else {
 			sendMessageBack(context, "Plugin " + pluginName + " failed to load.");
 			return false;
 		}
 	}
 
 	private boolean unloadPlugin(MessageContext context, String[] arguments) {
 		String pluginName = arguments[1];
 		sendMessageBack(context, "Unloading plugin " + pluginName + "...");
 		if(PluginManager.getInstance().unloadPlugin(pluginName)) {
 			sendMessageBack(context, "Plugin " + pluginName + " unloaded!");
 			return true;
 		} else {
 			sendMessageBack(context, "Plugin " + pluginName + " failed to unload.");
 			return false;
 		}
 	}
 }
