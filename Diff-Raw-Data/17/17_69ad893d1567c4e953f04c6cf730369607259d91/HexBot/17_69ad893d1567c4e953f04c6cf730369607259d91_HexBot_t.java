 package net.hexid.hexbot;
 
 import java.util.Arrays;
 import net.hexid.hexbot.bot.Bots;
 import net.hexid.hexbot.bot.BotGUI;
 import net.hexid.hexbot.bot.BotCLI;
 
 public class HexBot {
 	public HexBot() {
 		Bots.addBot("astral", "Astral WoW", "net.hexid.hexbot.bots.Astral", "net.hexid.hexbot.bots.AstralTab", "Astral.coffee");
 		Bots.addBot("bing", "Bing Rewards", "net.hexid.hexbot.bots.Bing", "net.hexid.hexbot.bots.BingTab", "Bing.coffee");
 		Bots.addBot("xbox", "Xbox Codes", "net.hexid.hexbot.bots.Xbox", "net.hexid.hexbot.bots.XboxTab", "Xbox.coffee");
 		Bots.addBot("imgur", "Imgur Albums", "net.hexid.hexbot.bots.Imgur", "net.hexid.hexbot.bots.ImgurTab", "Imgur.coffee");
 		Bots.addBot("molten", "Molten WoW", "net.hexid.hexbot.bots.Molten", "net.hexid.hexbot.bots.MoltenTab", "Molten.coffee");
 	}
 
 	public static void main(String[] args) {
 		new HexBot();
 		Bots.removeInvalidBots();
 
		// if more than 1 argument and the first
		// is `gui` -- or there are no arguments
		if((args.length >= 1 && args[0].equalsIgnoreCase("gui")) || args.length == 0) {
			BotGUI.init(Arrays.copyOfRange(args, (args.length > 0) ? 1 : 0, args.length));
		} else { // otherwise use the command line
 			BotCLI.init(args);
 		}
 	}
 }
