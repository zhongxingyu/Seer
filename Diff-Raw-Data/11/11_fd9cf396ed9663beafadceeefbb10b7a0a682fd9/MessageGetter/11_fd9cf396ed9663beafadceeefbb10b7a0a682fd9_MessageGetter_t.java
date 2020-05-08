 package com.gmail.andreasmartinmoerch.danandchat.utils;
 
 import org.bukkit.util.config.Configuration;
 
 import com.gmail.andreasmartinmoerch.danandchat.DanAndChat;
 import com.gmail.andreasmartinmoerch.danandchat.parsing.DanAndParser;
 
 /**
  * 
  * @author Huliheaden
  * 
  */
 public class MessageGetter {
 	/**
 	 * Fallback prefix.
 	 */
 	public static final String DEFAULT_PREFIX = "default";
 
 	private final DanAndChat plugin;
 	private final Configuration configuration;
 	private String prefix;
 	public static boolean debug = false;
 	
 	public static void writeDefaultMessagesToConfig(Configuration config, boolean overwrite){
 		for (Messages message: Messages.values()){
 			if (config.getString(DEFAULT_PREFIX + "." + message.toString(), null) == null || overwrite){
				config.setProperty(DEFAULT_PREFIX + "." + message.toString(), message.getFallback());
 			}
 			
 		}
 		config.save();
 		config.load();
 	}
 
 	/**
 	 * Constructor with pre-defined prefix.
 	 * 
 	 * @param configuration
 	 * @param prefix
 	 */
 	public MessageGetter(DanAndChat plugin, Configuration configuration, String prefix) {
 		this.plugin = plugin;
 		this.configuration = configuration;
 		this.prefix = prefix;
 	}
 	
 
 	/**
 	 * Uses fallback prefix.
 	 * 
 	 * @param configuration
 	 */
 	public MessageGetter(DanAndChat plugin, Configuration configuration) {
 		this.plugin = plugin;
 		this.configuration = configuration;
 		this.prefix = DEFAULT_PREFIX;
 	}
 	
 	/**
 	 * 
 	 * @return used Configuration.
 	 */
 	public Configuration getConfiguration() {
 		return this.configuration;
 	}
 
 	/**
 	 * 
 	 * @return used prefix.
 	 */
 	public String getPrefix() {
 		return this.prefix;
 	}
 	
 	/**
 	 * 
 	 * @param prefix
 	 */
 	public void setPrefix(String prefix){
 		this.prefix = prefix;
 	}
 	
 	public String getMessageWithArgs(Messages message, String... args){
 		String customMessage = getMessage(message);
 		int i = 0;
 		for (String s: args){
 			customMessage = customMessage.replaceFirst("%ARG" + String.valueOf(i), s);
 		}
 		
 		return customMessage;
 	}
 	
 	/**
 	 * Not done.
 	 * @param message
 	 * @return
 	 */
 	public String getMessage(Messages message){
 		String customMessage = "";
 		
 		if ((customMessage = this.configuration.getString(this.prefix.toLowerCase() + "." + message.toString(), null)) == null){
 			customMessage = message.getFallback();
 		}
 		
 		if (debug){
 			customMessage += "(" + message.toString() + ")";
 		}
 		
 		DanAndParser dap = new DanAndParser(this.plugin);
 		customMessage = dap.parseColours(customMessage);
 		customMessage = dap.parseAmpersands(customMessage);
 		
 		return customMessage;
 	}
 }
