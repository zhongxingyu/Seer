 package jisssea.controller.commands;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import jisssea.bot.Bot;
 import jisssea.bot.BotRegistry;
 import jisssea.controller.Controller;
 import jisssea.controller.messages.UserMessage;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * @author richard
  * 
  *         /quote PRIVMSG mulletron :rawtest
  */
 public class RawCommand extends RegexCommand {
 
 	private static final Log log = LogFactory.getLog(RawCommand.class);
 
	private static final Pattern p = Pattern.compile("/quote (.*)");
 
 	@Override
 	protected Pattern pattern() {
 		return p;
 	}
 
 	@Override
 	protected void guardedAct(Matcher m, UserMessage msg, BotRegistry irc, final Controller ctrl) {
 		Bot bot = irc.getContext();
 		bot.sendRawLine(m.group(1));
 	}
 }
