 package pl.shockah.shocky2.module.botcontrol;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.pircbotx.Channel;
 import org.pircbotx.PircBotX;
 import org.pircbotx.User;
 import pl.shockah.shocky2.Command;
 import pl.shockah.shocky2.CommandCallback;
 import pl.shockah.shocky2.Data;
 import pl.shockah.shocky2.ETarget;
 import pl.shockah.shocky2.Module;
 import pl.shockah.shocky2.Util;
 import pl.shockah.shocky2.smodule.login.LoginData;
 import com.mongodb.BasicDBObject;
 
 public class CommandSet extends Command {
 	public CommandSet(Module module) {
 		super(module);
 	}
 	
 	public String command() {return "set";}
 	public String help() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(".set [.|channel] {key} {type:value} - sets a value to bot's config (. = current channel)\n");
 		return sb.toString();
 	}
 	public void call(PircBotX bot, ETarget target, CommandCallback callback, Channel channel, User sender, String message) {
 		if (callback.target != ETarget.Console) callback.target = ETarget.Notice;
 		
 		String aChannel = null, aKey = null, aValue = null;
 		
 		String[] split = message.split("\\s");
 		if (split.length < 3) {
 			callback.append(help());
 			return;
 		}
 		
 		if (split.length == 3) {
 			if (".#".contains(""+split[1].charAt(0))) {
 				callback.append(help());
 				return;
 			} else {
 				aKey = split[1];
 				aValue = Util.implode(split,2," ");
 			}
 		} else {
 			aChannel = split[1].equals(".") ? channel.getName() : split[1];
 			aKey = split[2];
 			aValue = Util.implode(split,3," ");
 		}
 		
		LoginData ld = sender == null ? null : LoginData.getLoginData(sender.getNick());
		if ((aChannel == null && (ld == null || ld.isController())) || (aChannel != null && ld != null && ld.isOp(aChannel))) {
 			String key = aKey;
 			if (aChannel != null) key = aChannel+"->"+key;
 			
 			if (aValue.equals("-")) {
 				Data.getCollection().remove(Data.document("key",key));
 				callback.append("Removed "+aKey+" "+(aChannel == null ? "" : "for channel "+aChannel+" "));
 			} else {
 				Pattern pattern = Pattern.compile("([bilfds]):(.*)");
 				Matcher matcher = pattern.matcher(aValue);
 				if (matcher.find()) {
 					char type = matcher.group(1).toLowerCase().charAt(0);
 					BasicDBObject doc = new BasicDBObject();
 					
 					doc.append("key",key);
 					switch (type) {
 						case 'b': doc.append("value",Boolean.parseBoolean(matcher.group(2))); break;
 						case 'i': doc.append("value",Integer.parseInt(matcher.group(2))); break;
 						case 'l': doc.append("value",Long.parseLong(matcher.group(2))); break;
 						case 'f': doc.append("value",Float.parseFloat(matcher.group(2))); break;
 						case 'd': doc.append("value",Double.parseDouble(matcher.group(2))); break;
 						case 's': doc.append("value",matcher.group(2)); break;
 						default: {
 							callback.append("Unknown type: "+type);
 							return;
 						}
 					}
 					Data.getCollection().update(Data.document("key",key),doc,true,false);
 					callback.append("Set "+aKey+" "+(aChannel == null ? "" : "for channel "+aChannel+" ")+"to "+matcher.group(2));
 				} else callback.append("Incorrect value, required format: {type}:{value}");
 			}
 		} else callback.append("Restricted command.");
 	}
 }
