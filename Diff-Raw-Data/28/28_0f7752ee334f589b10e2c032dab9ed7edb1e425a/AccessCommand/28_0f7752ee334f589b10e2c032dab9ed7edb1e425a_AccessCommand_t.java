 /*
  * @author Kyle Kemp
  * @description This module allows for the changing of user permissions on the bot.
  * @basecmd access
  * @category core
  */
 package commands;
 
 import org.pircbotx.Channel;
 import org.pircbotx.User;
 
 import backend.Bot;
 import backend.Util;
 
 public class AccessCommand extends Command {
 
 	@Override
 	public void execute(Bot bot, Channel chan, User user, String message) {
 		if(!Util.hasArgs(message, 3)) {
 			invalidFormat(bot, chan, user);
 			return;
 		}
 		String[] args = Util.getArgs(message, 3);
 		String nickname = args[1] = args[1].toLowerCase();
 		String status = args[2] = args[2].toLowerCase();
 		switch(status) {
 		case "banned":
 			if(Bot.getBanned().contains(nickname)) {
 				Bot.removeBanned(nickname);
 				passMessage(bot, chan, user, nickname + " is no longer bot-banned.");
 			} else {
 				Bot.addBanned(nickname);
 				passMessage(bot, chan, user, nickname + " is now bot-banned.");
 			}
 			break;
 		case "elevated":
 			if(Bot.getElevated().contains(nickname)) {
 				Bot.removeElevated(nickname);
 				passMessage(bot, chan, user, nickname + " no longer has elevated access.");
 			} else {
 				Bot.addElevated(nickname);
 				passMessage(bot, chan, user, nickname + " now has elevated access.");
 			}
 			break;
 		case "owner":
 			if(Bot.getOwners().contains(nickname)) {
 				Bot.removeOwner(nickname);
 				passMessage(bot, chan, user, nickname + " is no longer an owner of me.");
 			} else {
 				Bot.addOwner(nickname);
 				passMessage(bot, chan, user, nickname + " is now an owner of mine.");
 			}
 			break;
 		default:
 			passMessage(bot, chan, user, "That level of access doesn't exist, you know?");
 			break;
 		}
 				
 	}
 
 	@Override
 	protected void initialize() {
 		addAlias("access");
 		setHelpText("Change how accessible I am to someone ;)");
 		setName("AccessChange");
 		setAccessLevel(LEVEL_OWNER);
 		setUsableInPM(true);
 	}
 	
 	protected String format() {
		return super.format() + " {user} [banned | elevated | owner]";
 	}
 
 }
