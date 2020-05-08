 package roujo.emily.core.commands;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import roujo.emily.core.MessageContext;
 import roujo.emily.core.util.InternalUser;
 import roujo.emily.core.util.StringHelper;
 
 public abstract class Command {
 	private final String name;
 	private final Pattern pattern;
 	private final String description;
 	private final String usage;
 	private final boolean isSuperUserOnly;
 
 	protected Command(String name, String pattern, String description,
 			String usage, boolean isSuperUserOnly) {
 		this.name = name;
 		this.pattern = Pattern.compile(pattern);
 		this.description = description;
 		this.usage = usage;
 		this.isSuperUserOnly = isSuperUserOnly;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public Pattern getPattern() {
 		return pattern;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public String getUsage() {
 		return usage;
 	}
 
 	public boolean isSuperUserOnly() {
 		return isSuperUserOnly;
 	}
 
 	public abstract boolean execute(MessageContext context);
 
 	protected String getArguments(MessageContext context) {
 		String strippedMessage;
		if (context.wasPrefixed()) {
 			strippedMessage = context.getProcessedMessage();
 		} else {
 			Matcher matcher = StringHelper
 					.getPatternFromNick(context.getBot().getNick())
 					.matcher(context.getMessage());
 			if(matcher.matches())
 				strippedMessage = matcher.group(1);
 			else
 				strippedMessage = context.getMessage();
 			context.setProcessedMessage(strippedMessage);
 		}
 		Matcher matcher = getPattern().matcher(strippedMessage);
 		if(matcher.matches())
 			return matcher.group("args");
 		else
 			return "";
 	}
 
 	protected boolean isValidSender(MessageContext context) {
 		InternalUser internalUser = context.getInternalUser();
 		if (isSuperUserOnly) {
 			return internalUser != null && internalUser.isSuper();
 		} else {
 			return internalUser == null || !internalUser.isBlackListed();
 		}
 	}
 
 	protected void logError(MessageContext context, String message) {
 		context.getBot().sendMessage(context.getUser(), message);
 	}
 
 	protected void sendMessageBack(MessageContext context, String message) {
 		// Add a little love
 		InternalUser user = context.getInternalUser();
 		if (user != null && user.isOwner())
 			message += " <3";
 
 		if (context.isPrivateMessage())
 			context.getBot().sendMessage(context.getUser(), message);
 		else
 			context.getBot().sendMessage(context.getChannel(),
 					context.getUser().getNick() + ": " + message);
 	}
 
 	protected void sendUsageBack(MessageContext context) {
 		sendMessageBack(context, usage);
 	}
 }
