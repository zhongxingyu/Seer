 package de.blablubbabc.billboards;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 class Messages {
 	private static String[] messages;
 
 	// loads messages from the messages.yml configuration file into memory
 	static void loadMessages(String messagesFilePath) {
 		Message[] messageIDs = Message.values();
 		messages = new String[Message.values().length];
 
 		HashMap<String, CustomizableMessage> defaults = new HashMap<String, CustomizableMessage>();
 
 		// initialize default messages
 		addDefault(defaults, Message.YOU_HAVE_TO_SNEAK, "&7You have to sneak to remove this.", null);
 		addDefault(defaults, Message.SIGN_REMOVED, "&aBillboard sign was removed.", null);
 		addDefault(defaults, Message.ADDED_SIGN, "&aThis sign can now be rented from &f{2} &afor &8{0} $ &afor &8{1} days&a.", "0: price  1: duration  2: creator");
 		addDefault(defaults, Message.ALREADY_BILLBOARD_SIGN, "&7This sign is already a billboard sign.", null);
 		addDefault(defaults, Message.NO_TARGETED_SIGN, "&7You have to target a sign.", null);
 		addDefault(defaults, Message.ONLY_AS_PLAYER, "This only works as player.", null);
 		addDefault(defaults, Message.INFO_HEADER, "&3Billboard - Information", null);
 		addDefault(defaults, Message.INFO_CREATOR, "&5Creator: &2{0}", "0: creator");
 		addDefault(defaults, Message.INFO_OWNER, "&5Owner: &2{0}", "0: owner");
 		addDefault(defaults, Message.INFO_PRICE, "&5Price: &2{0} $", "0: price");
 		addDefault(defaults, Message.INFO_DURATION, "&5Duration: &2{0} days", "0: duration");
 		addDefault(defaults, Message.INFO_RENT_SINCE, "&5Rented since: &2{0}", "0: since date");
 		addDefault(defaults, Message.INFO_RENT_UNTIL, "&5Rented until: &2{0}", "0: until date");
 		addDefault(defaults, Message.INFO_TIME_LEFT, "&5Time remaining: &2{0}", "0: time left");
 		addDefault(defaults, Message.CLICK_TO_RENT, "&6Click the sign again, to rent it from &8{2} &6for &b{0} $ &6for &b{1} days&6.", "0: price  1: duration  2: creator");
 		addDefault(defaults, Message.YOU_HAVE_RENT_A_SIGN, "&aYou have rented this sign now from &8{2} &6for &b{1} days &a. \n&bTo edit it: &aright-click it with a sign", "0: price  1: duration  2: creator");
 		addDefault(defaults, Message.TRANSACTION_FAILURE, "&cSomething went wrong: {0}", "0: errorMessage");
 		addDefault(defaults, Message.NO_LONGER_AVAILABLE, "&cThis sign is no longer available!", null);
 		addDefault(defaults, Message.NOT_ENOUGH_MONEY, "&cYou have not enough money! \nYou need &8{0} $&c, but you only have &8{1} $&c!", "0: price  1: balance");
 		addDefault(defaults, Message.MAX_RENT_LIMIT_REACHED, "&cYou already own too many billboard signs &7(limit: &e{0}&7)&c!", "0: limit");
 		addDefault(defaults, Message.CANT_RENT_OWN_SIGN, "&cYou can't rent your own sign.", null);
 		addDefault(defaults, Message.NO_PERMISSION, "&cYou have no permission for that.", null);
 		addDefault(defaults, Message.SIGN_LINE_1, "&bRENT ME", "0: price  1: duration  2: creator");
 		addDefault(defaults, Message.SIGN_LINE_2, "&f(click!)", "0: price  1: duration  2: creator");
 		addDefault(defaults, Message.SIGN_LINE_3, "&8{0} $", "0: price  1: duration  2: creator");
 		addDefault(defaults, Message.SIGN_LINE_4, "&8{1} days", "0: price  1: duration  2: creator");
 		addDefault(defaults, Message.DATE_FORMAT, "dd/MM/yyyy HH:mm:ss", "Only change this if you know what you are doing..");
 		addDefault(defaults, Message.TIME_REMAINING_FORMAT, "%d days %d h %d min", "Only change this if you know what you are doing..");
 		addDefault(defaults, Message.INVALID_NUMBER, "&cInvalid number: {0}", "0: the invalid argument");
 		addDefault(defaults, Message.RENT_SIGN_LINE_1, "&aRent by", "0: price  1: duration  2: creator 3: new owner");
		addDefault(defaults, Message.RENT_SIGN_LINE_2, "&f{3}", "0: price  1: duration  2: creator 3: new owner");
 		addDefault(defaults, Message.RENT_SIGN_LINE_3, "&cRight-click", "0: price  1: duration  2: creator 3: new owner");
 		addDefault(defaults, Message.RENT_SIGN_LINE_4, "&cwith a sign!", "0: price  1: duration  2: creator 3: new owner");
 
 		// load the message file
 		FileConfiguration config = YamlConfiguration.loadConfiguration(new File(messagesFilePath));
 
 		// for each message ID
 		for (int i = 0; i < messageIDs.length; i++) {
 			// get default for this message
 			Message messageID = messageIDs[i];
 			CustomizableMessage messageData = defaults.get(messageID.name());
 
 			// if default is missing, log an error and use some fake data for
 			// now so that the plugin can run
 			if (messageData == null) {
 				Billboards.logger.severe("Missing message for " + messageID.name() + ".  Please contact the developer.");
 				messageData = new CustomizableMessage(messageID, "Missing message!  ID: " + messageID.name() + ".  Please contact a server admin.", null);
 			}
 
 			// read the message from the file, use default if necessary
 			messages[messageID.ordinal()] = config.getString("Messages." + messageID.name() + ".Text", messageData.text);
 			config.set("Messages." + messageID.name() + ".Text", messages[messageID.ordinal()]);
 			// translate colors
 			messages[messageID.ordinal()] = ChatColor.translateAlternateColorCodes('&', messages[messageID.ordinal()]);
 
 			if (messageData.notes != null) {
 				messageData.notes = config.getString("Messages." + messageID.name() + ".Notes", messageData.notes);
 				config.set("Messages." + messageID.name() + ".Notes", messageData.notes);
 			}
 		}
 
 		// save any changes
 		try {
 			config.save(messagesFilePath);
 		} catch (IOException exception) {
 			Billboards.logger.severe("Unable to write to the configuration file at \"" + messagesFilePath + "\"");
 		}
 
 		defaults.clear();
 		System.gc();
 	}
 
 	// helper for above, adds a default message and notes to go with a message
 	private static void addDefault(HashMap<String, CustomizableMessage> defaults, Message id, String text, String notes) {
 		CustomizableMessage message = new CustomizableMessage(id, text, notes);
 		defaults.put(id.name(), message);
 	}
 
 	// gets a message from memory
 	static String getMessage(Message messageID, String... args) {
 		String message = messages[messageID.ordinal()];
 
 		for (int i = 0; i < args.length; i++) {
 			String param = args[i];
 			message = message.replace("{" + i + "}", param);
 		}
 
 		return message;
 
 	}
 }
