 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import org.apache.commons.cli.CommandLine;  
 import org.apache.commons.cli.CommandLineParser;  
 import org.apache.commons.cli.GnuParser;  
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;  
 import org.apache.commons.cli.ParseException;
   
 public class CommandLineInterpreter
 {  
 	private static Scanner input;
 
 	/**
      * Use GNU Parser
 	 * Interprets commands given and carries out the proper functions.
 	 * If you want to add a command, add it here as an if statement and
 	 * in the constructGnuOptions function for the program to recognize it.
 	 * @param commandLineArguments
 	 * @throws SQLException 
 	 * @throws ClassNotFoundException 
 	 */
 
 	public static String useGnuParser(final String[] commandLineArguments) throws ClassNotFoundException, SQLException{  
 		final CommandLineParser cmdLineGnuParser = new GnuParser();  
   
 		final Options gnuOptions = new CommandLineConstructors().getCommands();  
 		CommandLine commandLine;  
 		
 		String result = "";
 		
 		try{  
 			commandLine = cmdLineGnuParser.parse(gnuOptions, commandLineArguments);
 			
 			if (commandLine.hasOption("updiv")){
 				result = uploadCommand(commandLineArguments, commandLine, "updiv");
 			}
 			
 			if (commandLine.hasOption("upano")){
 				result = uploadCommand(commandLineArguments, commandLine, "upano");
 			}
 			
 			//allows for three arguments, afs(vcfname, filename, filtername)
 			if (commandLine.hasOption("asf")){
 				String[] args = commandLine.getOptionValues("asf");
 				Command command = null;
 				if(args.length == 1) command = new AFSCommand(args[0], "");
 				if(args.length == 2) command = new AFSCommand(args[0], args[1]);
 				result = command.execute();
 			}
 			
 			//Allow for two optional arguments
 			if (commandLine.hasOption("filterWrite")){
 				String[] args = commandLine.getOptionValues("filterWrite");
 				Command command = null;
 				if(args.length == 3) command = new FilterWriteApplier(args[0], args[1], args[2]);
 				result = command.execute();
 			}
 			
 			if (commandLine.hasOption("filterStore")){
 				String[] args = commandLine.getOptionValues("filterStore");
 				Command command = null;
 				if(args.length == 2) command = new FilterStoreApplier(args[0], args[1]);
 				result = command.execute();
 			}
 			
 			if(commandLine.hasOption("createfilter")){
 				String[] args = commandLine.getOptionValues("createfilter");
 				FilterCreator filter = null;
 				if(args == null){
 					input = new Scanner(System.in);
 					ArrayList<String> additionalArguments = new ArrayList<String>();
 					System.out.println("Please input additional arguments for creating a filter. Enter 'done' or hit enter twice when finished.");
 					while(true){
 						System.out.print(">> ");
 						String line = input.nextLine().trim();
 						if(line.equals("done") || line.equals("")){
 							break;
 						}
 						System.out.println(line);
 						additionalArguments.add(line);
 					}
					String[] arguments = (String[]) additionalArguments.toArray();
 					filter = new FilterCreator(args[0],arguments);
 				}else{
 					String[] additionalArguments = new String[args.length-1];
 					
 					for(int i = 0; i < additionalArguments.length; i++){
 						additionalArguments[i] = args[i+1];
 					}
 					
 					filter = new FilterCreator(args[0],additionalArguments);
 				}
 				filter.uploadEntries();
 			}
 			
 			if (commandLine.hasOption("sum")){
 				
 				String[] stringNumbers = commandLine.getOptionValues("sum");
 				int sum = 0;
 
 				for(int i = 0; i < stringNumbers.length; i++){
 					sum += Integer.parseInt(stringNumbers[i]);
 				}
         	 
 				System.out.println(sum);
 			}
 			
 			if (commandLine.hasOption("help")){
 				
 				/*
 				 * Expand to a more general help function
 				 */
 				
 				System.out.println("hello\nn <arg>\nsum <arg0> <arg1> <arg2> ...");
 			}
 		}
       
 		catch (ParseException parsingException){  
 			System.err.println("Could not find argument: " + parsingException.getMessage());  
 		}
 		
 		return result;
 	}  
 	
 	/**
 	 * Uploads either divergence file or annotation file
 	 * @param commandLineArguments
 	 * @param commandLine
 	 * @param type
 	 * @return the name of the upload
 	 */
 	
 	public static String uploadCommand(final String[] commandLineArguments, CommandLine commandLine, String type){
 		String[] args;
 		Command command = null;
 		
 		args = commandLine.getOptionValues(type);
 
 		String result = "";
 		
 		if(args == null){
 			result = "Please input arguments";
 		}else if(args.length == 1){
 			if(type=="updiv")command = new UploadDivergenceCommand(args[0], null,"");
 			if(type=="upano")command = new UploadAnnotationCommand(args[0], null,"");
 			
 			result = command.execute();
 		}else if(args.length == 2){
 			if(type=="updiv")command = new UploadDivergenceCommand(args[0], null,args[1]);
 			if(type=="upano")command = new UploadAnnotationCommand(args[0], null,args[1]);
 			
 			result = command.execute();
 		}else{
 			result = "Incorrect number of arguments";
 		}
 		
 		return result;
 	}
   
 
    /**
     * Prints out the commands the user input.
     */
    
    public static void displayInput(final String[] commandLineArguments){  
 	   
 	   int length = commandLineArguments.length;
 	   String output = "";
 	   
 	   for(int i = 0; i < length; i++){
 		   output += commandLineArguments[i];
 		   output += " ";
 	   }
 	   
 	   System.out.println(output);
    }
    
    /**
     * This is the method that should be called by outside methods and classes
     * to run all commands.
  * @throws SQLException 
  * @throws ClassNotFoundException 
     */
    
    public static String interpreter(String[] commandLineArguments) throws ClassNotFoundException, SQLException{
 	    if (commandLineArguments.length < 1)  
 	      {  
 	         System.out.println("Please input help"); 
 	      }  
 	      displayInput(commandLineArguments);
 	      System.out.println("");
 	      return useGnuParser(commandLineArguments);  
    }
   
    /** 
     * Main executable method used to demonstrate Apache Commons CLI. 
     *  
     * @param commandLineArguments Commmand-line arguments. 
  * @throws SQLException 
  * @throws ClassNotFoundException 
     */  
    public static void main(final String[] commandLineInput) throws ClassNotFoundException, SQLException{  
 	   System.out.println("Test Parser");
 	   System.out.println("Developed for the Gene-E project\n");
 	   
 	   interpreter(commandLineInput);
    }  
 } 
