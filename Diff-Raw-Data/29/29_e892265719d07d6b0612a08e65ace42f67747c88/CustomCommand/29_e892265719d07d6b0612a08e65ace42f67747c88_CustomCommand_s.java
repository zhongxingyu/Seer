 package jcolin.commands.custom;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import jcolin.Shell;
 import jcolin.commands.Command;
 import jcolin.commands.ICommand;
 import jcolin.consoles.Console;
 import jcolin.consoles.IConsole;
 import jcolin.validators.Validator;
 
 public class CustomCommand extends Command {
    private String m_class;
 	private String m_description;
 	private ICommand m_command;
 	private List<Argument> m_args;
 	private List<String> m_argValues;
 	private List<Validator> m_outputValidators;
 	private boolean m_outputDisplay;
 
	public CustomCommand(String[] names, String clss) throws Exception {
 		super(names);
		m_class = clss;
		m_command = loadCommandClass(m_class, ICommand.class);
 		m_args = new ArrayList<Argument>();
 		m_argValues = new ArrayList<String>();
 		m_outputValidators = new ArrayList<Validator>();
 		m_outputDisplay = true;
 	}
 
	public CustomCommand(String[] names, String clss,
 			ICommand command,
 			List<Argument> args,
 			List<String> argValues,
 			List<Validator> outputValidators,
 			boolean outoutDisplay) throws Exception {
 		
 		super(names);
		m_class = clss;
 		m_command = command;
 		m_args = args;
 		m_argValues = argValues;
 		m_outputValidators = outputValidators;
 		m_outputDisplay = outoutDisplay;
 	}
 
     public int numArgs() {
     	return m_args.size();
     }
 
     public String commandLine() {
     	String str = name();
     	for (String argValue : m_argValues) {
     		str += " " + argValue;
     	}
     	return str;
     }
 
     public String description(boolean detailed) {
     	String str = name();
     	for (Argument arg : m_args) {
     		str += " <" + arg.getName() + ">";
     	}
         return str + ": " + m_description;
     }
 
     public Command clone(String[] args, int index) {    	    	
     	try {
     		
     		List<String> argumentValues = extractArgumentList(args, index);
     		
			return new CustomCommand(names(), m_class, m_command,
 					m_args, argumentValues, m_outputValidators, 
 					m_outputDisplay);
 
 		} catch (Exception ignore) {
 			return null;
 		}
     }
 
     public void execute(Shell shell, Object model, Console console) {
     	try {
     		if (validateArguments(console)) {
     			
     			// The console buffer is used to store the command output
     			// in order for the output validation to access it.
     			console.clearOutputBuffer();
     			
     			// Determines if the output from this command should be 
     			// visible to the user on the console.
     			console.setOutputDisplay(m_outputDisplay);
     			
             	m_command.execute(shell, model, console, getArgsMap()); 
 
             	console.setOutputDisplay(true);
             	
             	validateOutput(console);
     		}
     		
     	} catch (IllegalArgumentException e) {
             console.displayError("Illegal argument detected\n");    		
     	}
     }
     
     public void setDescription(String description) {
     	m_description = description;
     }
     
     public void addArgument(Argument arg) {
     	m_args.add(arg);
     }
     
     public void setOutputDisplay(boolean outputDisplay) {
     	m_outputDisplay = outputDisplay;
     }
     
     public void addOutputValidator(Validator validator) {
     	m_outputValidators.add(validator);
     }
     
     private <T> T loadCommandClass(final String className, final Class<T> type) 
     		throws Exception {
     	
         return type.cast(Class.forName(className).newInstance());
     }
     
     private Map<String, String> getArgsMap() {
     	Map<String, String> argsMap = new TreeMap<String, String>();
     	for (int i = 0; i < m_args.size(); ++i) {
     		argsMap.put(m_args.get(i).getName(), m_argValues.get(i));
     	}
     	return argsMap;
     }
     
     private List<String> extractArgumentList(String[] args, int index) {
     	List<String> argsList = Arrays.asList(args);
     	return argsList.subList(index + 1, index + 1 + numArgs());
     }
     
     private boolean validateArguments(IConsole console) {
     	boolean passed = true;
     	for (int i = 0; i < m_args.size(); ++i) {
     		Argument arg = m_args.get(i);
     		String argValue = m_argValues.get(i);
  
     		if (!arg.validate(argValue)) {				
 				console.displayError("Argument: " + arg.getName() + 
 						" is not valid for value: " + argValue + "\n");
 				passed = false;
 			}
 		}
 		return passed;    	
     }
 
     private boolean validateOutput(Console console) {
     	String output = console.getCommandOutput();
     	for (Validator validator : m_outputValidators) {
     		if (!validator.validate(output)) {				
 				console.displayError("Validation failed for the output of this command\n");
 				return false;
     		}
     	}
     	return true;
     }
 }
