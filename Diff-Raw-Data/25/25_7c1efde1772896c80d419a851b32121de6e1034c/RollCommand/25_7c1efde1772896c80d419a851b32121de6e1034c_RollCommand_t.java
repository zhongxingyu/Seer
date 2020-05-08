 package roujo.emily.plugins.basic.commands;
 
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import roujo.emily.core.MessageContext;
 import roujo.emily.core.commands.Command;
 
 public class RollCommand extends Command {
 	private final Pattern dicePattern;
 	
 	public RollCommand() {
		super("roll", "roll (?<args>[0-9]+d[1-9][0-9]*((?:\\+|-)[0-9]+)?)",
 				"Rolls a certain amount of dice and gives the result.",
 				"dice 2d20", false);
		dicePattern = Pattern.compile("([0-9]+)d([1-9][0-9]*)((?:\\+|-)[0-9]+)?");
 	}
 
 	@Override
 	public boolean execute(MessageContext context) {
 		String arguments = getArguments(context);
 		if (arguments.equals("")) {
 			sendUsageBack(context);
 			return false;
 		}
 
 		Matcher matcher = dicePattern.matcher(arguments);
 		if (!matcher.matches()) {
 			sendUsageBack(context);
 			return false;
 		}
 
 		int diceNumber = Integer.parseInt(matcher.group(1));
 		int diceType = Integer.parseInt(matcher.group(2));
 		int total = 0;
		if(matcher.groupCount() > 2 && matcher.group(3) != null) {
			total += Integer.parseInt(matcher.group(3));
		}
		int[] results = new int[diceNumber];
		Random random = new Random();
 		for (int i = 0; i < diceNumber; ++i) {
			results[i] = random.nextInt(diceType) + 1; 
			total += results[i];
 		}
 		String response = "Results of " + arguments + ": " + total;
		StringBuilder rolls = new StringBuilder();
		rolls.append(results[0]);
		for(int i = 1; i < results.length; ++i)
			rolls.append(", " + results[i]);
		String details = "Rolls: " + rolls.toString();
 		sendMessageBack(context, response);
		sendMessageBack(context, details);
 		return true;
 	}
 
 }
