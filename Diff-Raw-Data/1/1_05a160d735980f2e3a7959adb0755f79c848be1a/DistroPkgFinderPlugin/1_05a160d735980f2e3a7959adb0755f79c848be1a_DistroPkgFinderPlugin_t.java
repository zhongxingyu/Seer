 package org.manalith.ircbot.plugin.distropkgfinder;
 
 import org.manalith.ircbot.ManalithBot;
 import org.manalith.ircbot.plugin.AbstractBotPlugin;
 import org.manalith.ircbot.resources.MessageEvent;
 
 public class DistroPkgFinderPlugin extends AbstractBotPlugin {
 
 	public DistroPkgFinderPlugin(ManalithBot bot) {
 		super(bot);
 	}
 
 	public String getNamespace() {
 		return "deb|ubu|fed|gen";
 	}
 
 	public String getName() {
 		return "패키지 검색";
 	}
 
 	public String getHelp() {
 		return "!deb (pkg_name) | !ubu (pkg_name) | !fed (pkg_name) | !gen (pkg_name)";
 	}
 
 	public void onMessage(MessageEvent event) {
 		String message = event.getMessage();
 		String channel = event.getChannel();
 		String[] command = message.split("\\s");
 
 		if ((command[0].equals("!deb") || command[0].equals("!ubu")
 				|| command[0].equals("!fed") || command[0].equals("!gen"))
 				&& command.length > 2) {
 			bot.sendLoggedMessage(channel, "검색 단어는 하나면 충분합니다.");
 			event.setExecuted(true);
			return;
 		}
 		if ((command[0].equals("!deb") || command[0].equals("!ubu")
 				|| command[0].equals("!fed") || command[0].equals("!gen"))
 				&& command.length == 1 ) {
 			bot.sendLoggedMessage(channel, this.getHelp());
 			event.setExecuted(true);
 		} else if (command[0].equals("!deb")) {
 			DebianPkgFinderJsoupRunner runner = new DebianPkgFinderJsoupRunner(
 					command[1]);
 			bot.sendLoggedMessage(channel, runner.run());
 			event.setExecuted(true);
 		} else if (command[0].equals("!ubu")) {
 			UbuntuPkgFinderJsoupRunner runner = new UbuntuPkgFinderJsoupRunner(
 					command[1]);
 			bot.sendLoggedMessage(channel, runner.run());
 			event.setExecuted(true);
 		} else if (command[0].equals("!fed")) {
 			FedoraPkgFinderRunner runner = new FedoraPkgFinderRunner(command[1]);
 			bot.sendLoggedMessage(channel, runner.run());
 			event.setExecuted(true);
 		} else if (command[0].equals("!gen")) {
 			GentooPkgFinderJsoupRunner runner = new GentooPkgFinderJsoupRunner(
 					command[1]);
 			bot.sendLoggedMessage(channel, runner.run());
 			event.setExecuted(true);
 		}
 		
 		
 	}
 }
