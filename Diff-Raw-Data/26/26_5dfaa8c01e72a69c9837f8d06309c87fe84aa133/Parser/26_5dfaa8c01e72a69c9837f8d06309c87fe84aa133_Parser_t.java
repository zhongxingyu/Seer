 package darep.parser;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import darep.Command;
 
 /**
  * Parses an args-array like the one given to the main-method and produces
  * a {@link Command} object. Uses a {@link CommandSyntax}-array to parse it.
  */
 public class Parser {
 
 	private CommandSyntax[] allowedSyntax;
 	private Map<String, ArgConstraint> constraints;
 	private Command.ActionType defaultAction;
 
 	public Parser(CommandSyntax[] allowedSyntax, Map<String, ArgConstraint> constraints, Command.ActionType defaultAction) {
 		this.allowedSyntax = allowedSyntax;
 		this.constraints = constraints;
 		this.defaultAction = defaultAction;
 	}
 
 	public Command parse(String[] args) throws ParseException {
 		
 		// if no args are given, do defaultAction
 		if (args.length == 0) {
 			args = new String[] {defaultAction.toString()};
 		}
 
 		// determine action and get syntax for it
 		CommandSyntax syntax = parseAction(args);
 		Command.ActionType action = syntax.getAction();
 
 		// Make List from Array without action-name
 		List<String> argList = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
 
 		// Datastructures to hand over to Command-Object
 		ArrayList<String> flags = new ArrayList<String>();
 		HashMap<String, String> options = new HashMap<String, String>();
 		ArrayList<String> arguments = new ArrayList<String>();
 
 		// Number of arguments that are passed
 		int numParams = 0;
 
 		// Iterate over args List
 		Iterator<String> iterator = argList.iterator();
 		while (iterator.hasNext()) {
 			String currentArg = iterator.next();
 
 			// If arg starts with "-" it is a flag or an option
 			if (currentArg.startsWith("-")) {
 
 				String optName = currentArg.substring(1);
 
 				// Add Option
 				if (syntax.allowsOption(optName)) {
					if (!iterator.hasNext()) {
						throw new ParseException("Option \"" + optName + "\"" +
								" needs a parameter");
					}
 					this.insertOptionToMap(optName, iterator.next(), options);
 
 				// Add Flag
 				} else if (syntax.allowsFlag(optName)) {
 					flags.add(optName);
 
 				// option/flag not allowed => throw Exception
 				} else {
 					throw new ParseException("Action \"" + action
 							+ "\" doesn't allow option \"" + currentArg + "\"");
 				}
 
 			// String doesn't start with "-" => normal argument
 			} else {
 				numParams++;
 				arguments.add(currentArg);
 			}
 		}
 
 		// If given arguments doesn't match syntax, throw error
 		if (numParams != syntax.getNumParams()) {
 			throw new ParseException("Action \"" + action + "\" "
 					+ "takes " + syntax.getNumParams() + " argument(s)");
 		}
 
 		// Make variables that can be passed to the Command-Constructor
 		String[] argumentsArray = arguments.toArray(new String[0]);
 		String[] flagsArray = flags.toArray(new String[0]);
 		
 		return new Command(action, argumentsArray, options, flagsArray);
 		
 	}
 
 	/**
 	 * Inserts an option to a map if its valid according to constraints-map
 	 * and not already present inside the map.
 	 * 
 	 * Throws a {@link ParseException} if input is not valid or already present.
 	 * @param optName
 	 * @param value
 	 * @param map
 	 * @throws ParseException
 	 */
 	private void insertOptionToMap(String optName, String value, Map<String, String> map)
 					throws ParseException {
 		
 		// check for constraints
 		ArgConstraint constraint = constraints.get(optName);
 		if ((constraint != null)
 				&& !constraint.isValid(value)) {
 			throw new ParseException("Option \"" + optName + "\" was given" +
 					" an invalid value (" + constraint.getDescription() + ")");
 		}
 		
 		// check if already included
 		if (map.containsKey(optName)) {
 			throw new ParseException("Option \"" + optName + "\" was given" +
 					" more than once");
 		}
 		
 		map.put(optName, value);
 	}
 
 	private CommandSyntax parseAction(String[] args) 
 							throws ParseException {
 
 		if (args.length == 0) {
 			throw new ParseException("args contains no items");
 		} 
 		String actionName = args[0];
 
 		// get CommandSyntax
 		CommandSyntax syntax = getSyntax(actionName);
 		if (syntax == null) {
 			throw new ParseException("Unknown action \""
 						+ actionName +"\"");
 		}
 
 		return syntax;
 	}
 
 	private CommandSyntax getSyntax(String actionName){
 		Command.ActionType action;
 		try {
 			action = Command.ActionType.valueOf(actionName);
 		} catch (IllegalArgumentException e) {
 			return null;
 		}
 		for (CommandSyntax currSyntax: allowedSyntax) {
 			if (currSyntax.getAction() == action) {
 				return currSyntax;
 			}
 		}
 		return null;
 	}
 
 }
