 package org.freecode.irc.votebot.api;
 
 import org.freecode.irc.Privmsg;
 import org.freecode.irc.Transmittable;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public abstract class CommandModule extends FVBModule {
 
 	private static final Pattern COMMAND_PATTERN = Pattern.compile("(!(.+))|(!(.+?) (.+))");
 
 	@Override
 	public boolean canRun(Transmittable trns) {
 		if (!trns.isPrivmsg())
 			return false;
 		String msg = trns.asPrivmsg().getMessage();
 		Matcher matcher = COMMAND_PATTERN.matcher(msg);
 		if (matcher.matches()) {
 			int count = matcher.groupCount();
			String command = matcher.group(2);
			if (matcher.group(3) == null || matcher.group(3).isEmpty()) {
 				return getName().matches(command);
			} else
 				return getName().matches(command) && getParameterRegex().matches(matcher.group(3));

 		}
 		return false;
 	}
 
 	@Override
 	public void process(Transmittable trns) {
 		processMessage((Privmsg) trns);
 	}
 
 	public abstract void processMessage(Privmsg privmsg);
 
 	protected String getParameterRegex() {
 		return ".*";
 	}
 }
