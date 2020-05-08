 package darep;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 
 import darep.Command.ActionType;
 import darep.parser.ArgConstraint;
 import darep.parser.CommandSyntax;
 import darep.parser.ParseException;
 import darep.parser.Parser;
 import darep.repos.Repository;
 import darep.repos.RepositoryException;
 
 /**
  * Controls the flow of the program. Contains the main-method.
  * 
  */
 public class DarepController {
 	
 	public static final String RESOURCES = "resources";
 	public static final String HELPFILE = "help.txt";
 	
 	/**
 	 * The {@link CommandSyntax}-Array given to the parser. Defines the allowed
 	 * syntax when calling the program. The Format for a Command is:
 	 * (Name,#Options with Value,Flags)
 	 */
 	public static final CommandSyntax[] syntax = new CommandSyntax[] {
 			new CommandSyntax(ActionType.add, 1,
 					new String[] { "r", "n", "d" }, new String[] { "m" }),
 			new CommandSyntax(ActionType.delete, 1, new String[] { "r" },
 					new String[0]),
 			new CommandSyntax(ActionType.replace, 2, new String[] { "r", "d" },
 					new String[] { "m" }),
 			new CommandSyntax(ActionType.help, 0, new String[0], new String[0]),
 			new CommandSyntax(ActionType.list, 0, new String[] { "r" },
 					new String[] { "p" }),
 			new CommandSyntax(ActionType.export, 2, new String[] { "r" },
 					new String[0]) };
 
 	/**
 	 * Map with {@link ArgConstraint}s for the Parser. Values are added in
 	 * static initializer since there is no short syntax for Maps in Java.
 	 */
 	private static Map<String, ArgConstraint> constraints;
 
 	/**
 	 * Adds anonymous child objects of ArgConstraint to the constraints-map.
 	 */
 	private static void createConstraints() {
 
 		constraints = new HashMap<String, ArgConstraint>();
 
 		// name only consists of word chars (digit, letter, _) and -
 		// and is no longer than 40 chars long
 		constraints.put("n", new ArgConstraint() {
 			@Override
 			public boolean isValid(String arg) {
 				return (arg.matches("[A-Z0-9_-]*") && arg.length() <= 40);
 			}
 
 			@Override
 			public String getDescription() {
 				return "Argument must only contain"
 						+ " digits, uppercase letters, \"_\" or \"-\""
 						+ " and must not be longer than 40 characters";
 			}
 		});
 
 		// Description does not contain control characters and is no longer
 		// than 1000 chars
 		constraints.put("d", new ArgConstraint() {
 			@Override
 			public boolean isValid(String arg) {
 				return (arg.matches("[^\\p{Cntrl}]*") && arg.length() <= 1000);
 			}
 
 			@Override
 			public String getDescription() {
 				return "Argument must not contain ISO control characters"
 						+ " and must not be longer than 1000 characters";
 			}
 		});
 	}
 
 	public static Map<String, ArgConstraint> getConstraints() {
 		if (constraints == null) {
 			createConstraints();
 		}
 		return constraints;
 	}
 
 	private Parser parser;
 	private Repository repository;
 
 	public static void main(String[] args) {
 		DarepController controller = new DarepController();
 		try {
 			controller.processCommand(args);
 		} catch (ParseException e) {
 			quitWithException(e);
 		} catch (RepositoryException e) {
 			quitWithException(e);
 		}
 			
 	}
 
 	private static void quitWithException(Exception e) {
 		System.err.println(e.getMessage());
 		e.printStackTrace();
 		System.exit(1);
 	}
 
 	public void processCommand(String[] args) throws ParseException, RepositoryException {
 		Command command = parser.parse(args);
 
 		// check if command=help => dont need to load the repo, just print help
 		if (command.getAction() == ActionType.help) {
 			printHelp();
 			return;
 		}
 		
 		// check if option -r is set or default repos, and setup repos.
 		if (command.isSet("r")) {
 			//Check if Repository exists
 			if (!new File(command.getOptionParam("r")).exists()
 					&& !command.getAction().equals(ActionType.add)) {
 				throw new RepositoryException("Repository does not Exist");
 			}
 			repository = new Repository(command.getOptionParam("r"));
 		} else {
 			//Check if Repository exists
 			if (!new File(Repository.getDefaultLocation()).exists()
 					&& !command.getAction().equals(ActionType.add)) {
 				throw new RepositoryException("Repository does not Exist");
 			}
 			repository = new Repository();
 		}
 		switch (command.getAction()) {
 		case add:
 			repository.add(command);
 			break;
 		case delete:
 			repository.delete(command);
 			break;
 		case replace:
 			repository.replace(command);
 			break;
 		case export:
 			repository.export(command);
 			break;
 		case list:
 			System.out.println(repository.getList(command));
 			break;
 		}
 
 	}
 
 	public DarepController() {
 		this.parser = new Parser(DarepController.syntax,
 				DarepController.getConstraints(), ActionType.help);
 	}
 	
 	private void printHelp() throws RepositoryException{
 
 		String path = RESOURCES+"/"+HELPFILE; 
 		try {
 			InputStream is = this.getClass().getClassLoader().getResourceAsStream(HELPFILE);
 			
 			if(is == null) {
 				is = new FileInputStream(new File(path));
 			}
			System.out.println(Helper.fileToString(is));
 		} catch (FileNotFoundException e) {
 			throw new RepositoryException("could not find the helpfile: " + path, e);
 		} catch (IOException e) {
 			throw new RepositoryException("could not print the helpfile:" + HELPFILE, e);
 		} catch (NullPointerException e) {
 			throw new RepositoryException("could not load helpfile " + HELPFILE +" from .jar Archive", e);
 		}
 	}
 
 }
