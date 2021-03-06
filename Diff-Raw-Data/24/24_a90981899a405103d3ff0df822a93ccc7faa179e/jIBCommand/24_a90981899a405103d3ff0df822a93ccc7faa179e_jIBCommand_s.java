 package org.hive13.jircbot.commands;
 
 import org.hive13.jircbot.jIRCBot;
 import org.hive13.jircbot.jIRCBot.eLogLevel;
 import org.hive13.jircbot.support.jIRCTools.eMsgTypes;
 import org.hive13.jircbot.support.jIRCUser.eAuthLevels;
 
 /**
  * A basic framework for implementing commands.
  * 
  * @author vincentp
  * 
  */
 public abstract class jIBCommand {
 	protected eAuthLevels reqAuthLevel;
 
 	/**
 	 * This member variable is of pretty limited use. It basically exists for
 	 * commands that we might use on the backend (like Linkify) which the user
 	 * does not need to ever know about, but are actually implemented as
 	 * commands.
 	 */
 	protected boolean hideCommand = false;
 
 	public jIBCommand() {
 		this(eAuthLevels.unauthorized);
 	}
 
 	public jIBCommand(eAuthLevels reqAuthLevel) {
 		this.reqAuthLevel = reqAuthLevel;
 	}
 
 	public eAuthLevels getReqAuthLevel() {
 		return reqAuthLevel;
 	}
 
 	public boolean isHidden() {
 		return hideCommand;
 	}
 
 	/**
 	 * This method returns a unique name for the command.
 	 * 
 	 * @return A unique name for the command.
 	 */
 	public abstract String getCommandName();
 
 	/**
 	 * This method returns instructions on how to use the command.
 	 * 
 	 * @return Help instructions on how to use the command.
 	 */
 	public abstract String getHelp();
 
 	/**
 	 * This method is called to activate the command.
 	 * 
 	 * @param bot
 	 *            The bot that the command will be run against.
 	 * @param channel
 	 *            The channel that this command will run in.
 	 * @param sender
 	 *            The user that caused this command to be activated.
 	 * @param message
 	 *            The parameters (including the commandName) that were received
 	 *            when this command was activated.
 	 */
 	public void runCommand(jIRCBot bot, String channel, String sender,
 			String message) {
 		eAuthLevels userAuthLevel = bot.userListGetSafe(sender).getAuthLevel();
		if (userAuthLevel.ordinal() >= getReqAuthLevel().ordinal())
 			if (message.trim().equalsIgnoreCase("help")
 					|| message.trim().equalsIgnoreCase("h"))
 				bot.sendMessage(sender, getHelp(), eMsgTypes.LogFreeMsg);
 			else
 				new Thread(new jIBCommandRunnable(bot, channel, sender, message)).start();
 		else {
 			bot.sendMessage(sender,
 					"You do not have permission to activate this command.", eMsgTypes.LogFreeMsg);
 			bot.log(sender + " just tried to use " + getCommandName()
 					+ " but their AuthLevel is only " + userAuthLevel,
 					eLogLevel.warning);
 		}
 	}

 	/**
 	 * This method is internal to the commands. It is handled in a separate
 	 * thread so any calls from this method MUST be thread safe.
 	 * 
 	 * @param bot
 	 *            The bot that the command will run against.
 	 * @param channel
 	 *            The channel that this command will run in.
 	 * @param sender
 	 *            The user that caused this command to be activated.
 	 * @param message
 	 *            The parameters (including the commandName) that were received
 	 *            when this command was activated.
 	 */
 	protected abstract void handleMessage(jIRCBot bot, String channel,
 			String sender, String message);
 
 	/**
 	 * This class is used to launch off an asynchronous thread to prevent
 	 * Commands from blocking the main thread.
 	 */
 	protected class jIBCommandRunnable implements Runnable {
 		private jIRCBot bot;
 		private String channel;
 		private String sender;
 		private String message;
 
 		public jIBCommandRunnable(jIRCBot bot, String channel, String sender,
 				String message) {
 			super();
 			this.bot = bot;
 			this.channel = channel;
 			this.sender = sender;
 			this.message = message;
 		}
 
 		@Override
 		public void run() {
 			handleMessage(bot, channel, sender, message);
 		}
 
 	}
 }
