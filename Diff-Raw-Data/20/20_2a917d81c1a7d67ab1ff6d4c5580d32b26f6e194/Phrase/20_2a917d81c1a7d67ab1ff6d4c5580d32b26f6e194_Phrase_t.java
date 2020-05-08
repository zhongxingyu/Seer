 package org.melonbrew.fee;
 
 public enum Phrase {
 	VAULT_HOOK_FAILED("Could not hook into Vault - disabling Fee."),
 	COMMAND_WILL_COST("This command will cost $1. Type /yes to use this command."),
	NEED_MONEY("You need $1 to use this command."),
 	NO_PENDING_COMMAND("You don't have a pending command."),
 	YOU_ARE_NOT_A_PLAYER("Silly console, you aren't a player!"),
 	YOU_ARE_NOT_A_PLAYER_TWO("Silly console, Trix are for kids!");
 	
 	private static Fee plugin;
 	
 	private final String defaultMessage;
 	
 	private final boolean categorized;
 
 	private String message;
 	
 	private Phrase(String defaultMessage){
 		this(defaultMessage, false);
 	}
 
 	private Phrase(String defaultMessage, boolean categorized){
 		this.defaultMessage = defaultMessage;
 		
 		this.categorized = categorized;
 
 		message = defaultMessage + "";
 	}
 
 	public void setMessage(String message){
 		this.message = message;
 	}
 
 	private String getMessage(){
 		return message;
 	}
 
 	public void reset(){
 		message = defaultMessage + "";
 	}
 
 	public String getConfigName(){
 		String name = name();
 		
 		if (categorized){
 			name = name.replaceFirst("_", ".");
 		}
 		
 		return name().toLowerCase();
 	}
 
 	public String parse(String... params){
 		String parsedMessage = getMessage();
 
 		if (params != null){
 			for (int i = 0; i < params.length; i++){
 				parsedMessage = parsedMessage.replace("$" + (i + 1), params[i]);
 			}
 		}
 
 		return parsedMessage;
 	}
 	
 	public String parseWithoutSpaces(String... params){
 		return parse(params).replace(" ", "");
 	}
 	
 	public String parseWithPrefix(String... params){
 		return plugin.getMessagePrefix() + parse(params);
 	}
 	
 	public static void init(Fee instance){
 		plugin = instance;
 	}
 }
