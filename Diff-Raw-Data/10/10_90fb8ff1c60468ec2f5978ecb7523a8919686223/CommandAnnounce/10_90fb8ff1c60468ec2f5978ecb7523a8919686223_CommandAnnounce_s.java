 package pl.shockah.shocky2.module.botcontrol;
 
 import org.pircbotx.Channel;
 import org.pircbotx.PircBotX;
 import org.pircbotx.User;
 import pl.shockah.shocky2.Command;
 import pl.shockah.shocky2.CommandCallback;
 import pl.shockah.shocky2.ETarget;
 import pl.shockah.shocky2.Module;
 import pl.shockah.shocky2.Shocky;
 import pl.shockah.shocky2.Util;
 import pl.shockah.shocky2.smodule.login.LoginData;
 
 public class CommandAnnounce extends Command {
 	public CommandAnnounce(Module module) {
 		super(module);
 	}
 	
 	public String command() {return "announce";}
 	public String help() {
 		return ".announce <message> - sends <message> to all channels";
 	}
 	public void call(PircBotX bot, ETarget target, CommandCallback callback, Channel channel, User sender, String message) {
 		if (sender != null && !LoginData.getLoginData(sender.getNick()).isController()) {
 			if (callback.target != ETarget.Console) callback.target = ETarget.Notice;
 			callback.append("Restricted command.");
 			return;
 		}
 		
 		String[] split = message.split("\\s");
 		if (split.length >= 2) {
 			String msg = Util.implode(split,1," ");
 			for (String channelName : Shocky.botManager.getChannels()) {
 				Shocky.send(Shocky.botManager.getBotForChannel(channelName),ETarget.Channel,Shocky.botManager.getChannelWithName(channelName),null,msg);
 			}
 		}
 		
 		if (callback.target != ETarget.Console) callback.target = ETarget.Notice;
 		callback.append(help());
 	}
 }
