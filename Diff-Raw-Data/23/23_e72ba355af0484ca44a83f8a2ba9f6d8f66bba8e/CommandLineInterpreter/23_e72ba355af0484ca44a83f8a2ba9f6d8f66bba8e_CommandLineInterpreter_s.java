 import java.sql.SQLException;
 import java.util.Scanner;
   
 public class CommandLineInterpreter
 {  
 	//private static Scanner input;
 
 	public static String parseCommand(final String[] commandLineArguments) throws ClassNotFoundException, SQLException{  
 		
 		String result = "";
 			
 		if (isTheCommandName(commandLineArguments, "upload divergence") || isTheCommandName(commandLineArguments, "updiv")){
 			result = CallCommands.uploadCommand(commandLineArguments, "updiv");
 		}
 
		if (isTheCommandName(commandLineArguments, "upload annotation") || isTheCommandName(commandLineArguments, "upano")){
 			result = CallCommands.uploadCommand(commandLineArguments, "upano");
 		}
 		
		if (isTheCommandName(commandLineArguments, "allele frequency spectra") || isTheCommandName(commandLineArguments, "afs")){
 			result = CallCommands.vcfCommand(commandLineArguments);
 		}
 		
		if (isTheCommandName(commandLineArguments, "delete") || isTheCommandName(commandLineArguments, "del")){						
 			return CallCommands.deleteCommand(commandLineArguments);
 		}
 		
		if(isTheCommandName(commandLineArguments, "view")){
 			return CallCommands.viewCommand(commandLineArguments);
 		}
 		
		if(isTheCommandName(commandLineArguments, "create filter") || isTheCommandName(commandLineArguments, "crefil")){
 			CallCommands.createFilterLoop(commandLineArguments);
 		}
 		
		if(isTheCommandName(commandLineArguments, "filter") || isTheCommandName(commandLineArguments, "fil")){
 			result = CallCommands.filterCommand(commandLineArguments);
 		}
 		
 		return result;
 	}
 
 	//Ask about delete vcfs
 	/*private static String viewCommand(String[] args) {
 		String type = "";
 		
 		for(int i = 0; i < args.length; i++){
 			if(args[i].equals("divergence")){type = "divergence";}
 			if(args[i].equals("annotation")){type = "annotation";}
 			if(args[i].equals("vcf")){type = "vcf";}
 		}
 		
 		if(type.equals("")){return "Please include the table type you would lik to view.";}
 		
 		View view = new View(type);
 		return view.execute();
 	}
 
 	//Again, ask about vcf
 	public static String deleteCommand(final String[] args) {
 		String type = "";
 		String name = "";
 		
 		for(int i = 0; i < args.length; i++){
 			if(args[i].equals("divergence")){type = "divergence";}
 			if(args[i].equals("annotation")){type = "annotation";}
 			if(args[i].equals("vcf")){type = "vcf";}
 			if(args[i].equals("name") && i != args.length - 1){name = args[i+1];}
 		}
 		
 		Command delete = new DeleteCommand(type, name);
 		
 		return delete.execute();
 	}
 
 	public static void createFilterLoop(final String[] commandLineArguments)
 			throws ClassNotFoundException, SQLException {
 		FilterCreator filter = null;
 		String name = "";
 		
 		for(int i = 0; i < commandLineArguments.length; i++){
 			if(commandLineArguments[i].equals("name") && i != commandLineArguments.length - 1){name = commandLineArguments[i+1];}
 		}
 		
 		if(name.equals("")){
 			System.out.println("Please input a name");
 			return;
 		}
 			input = new Scanner(System.in);
 			ArrayList<String> additionalArguments = new ArrayList<String>();
 			System.out.println("Please input additional arguments for creating a filter. Enter 'done' or hit enter twice when finished.");
 			
 			while(true){
 				System.out.print(">> ");
 				String line = input.nextLine().trim();
 				if(line.equals("done") || line.equals("")){
 					break;
 				}
 				additionalArguments.add(line);
 			}
 			
 			if(additionalArguments.size() == 0){
 				System.out.println("Error: Please input arguments");
 			}else{
 			
 				String[] arguments = new String[additionalArguments.size()];
 				arguments = additionalArguments.toArray(arguments);
 				filter = new FilterCreator(name,arguments);
 				filter.uploadEntries();
 			}
 		
 	}  
 	
 	private static String filterCommand(String[] args) {
 		FilterApplier applier  = null;
 		
 		String result = "";
 		boolean store = false;
 		String write = "";
 		String by = "";
 		String vcf = "";
 		
 		for(int i = 0; i < args.length; i++){
 			if(args[i].equals("store")){store = true;}
 			if(args[i].equals("write") && i != args.length - 1){write = args[i+1];}
 			if(args[i].equals("filterby") && i != args.length - 1){by = args[i+1];}
 			if(args[i].equals("vcf") && i != args.length - 1){vcf = args[i+1];}
 		}
 		
 		if(vcf.equals("")){return "Please include the vcf that is being used.";}
 		if(by.equals("")){return "Please include what this is being filtered by";}
 		
 		if(!write.equals("")){
 			applier = new FilterWriteApplier(vcf, by, write);
 			result = applier.execute();
 		}
 		if(store == true){
 			applier = new FilterStoreApplier(vcf, by);
 			result = applier.execute();
 		}
 		if(store == false && write.equals("")){
 			result = "Please include write or store.";
 		}
 				
 		return result;
 	}
 
 	private static String vcfCommand(String[] args) throws ClassNotFoundException, SQLException {
 		Command command = null;
 		
 		String result = "";
 		String fileLocation = "";
 		String fileName = "";
 		String filterName = "";
 		
 		for(int i = 0; i < args.length; i++){
 			if(args[i].equals("vcf") && i != args.length - 1){fileName = args[i+1];}
 			if(args[i].equals("write") && i != args.length - 1){fileLocation = args[i+1];}
 			if(args[i].equals("filterby") && i != args.length - 1){filterName = args[i+1];}
 		}
 		
 		if(fileLocation.equals("") && fileName.equals("") && filterName.equals("")){return "Please input proper arguments";}
 		
 		command = new AFSCommand(fileName, fileLocation, filterName);
 		result = command.execute();
 		
 		return result;
 	}
 	
 	public static String uploadCommand(final String[] args, String type){
 		Command command = null;
 
 		String result = "";
 		String fileLocation = "";
 		String fileName = "";
 		
 		for(int i = 0; i < args.length; i++){
 			if(args[i].equals("name") && i != args.length - 1){fileName = args[i+1];}
 			if(args[i].equals("file") && i != args.length - 1){fileLocation = args[i+1];}
 		}
 		
 		if(fileLocation.equals("") && fileName.equals("")){return "Please input proper arguments";}
 		if(fileLocation.equals("")){return "Please include a file location.";}
 		
 		if(type=="updiv"){command = new UploadDivergenceCommand(fileLocation, null,fileName);}
 		if(type=="upano"){command = new UploadAnnotationCommand(fileLocation, null,fileName);}
 			
 		result = command.execute();
 		
 		return result;
 	}*/
 	
 	public static boolean isTheCommandName(String[] command, String commandName){
 		String[] commandNameArray = commandName.split("\\s+");
 		for(int i = 0; i < commandNameArray.length; i++){
 			if(!commandNameArray[i].equals(command[i])){
 				return false;
 			}
 		}
 		return true;
 	}
    
    public static void displayInput(final String[] commandLineArguments){  
 	   
 	   int length = commandLineArguments.length;
 	   String output = "";
 	   
 	   for(int i = 0; i < length; i++){
 		   output += commandLineArguments[i];
 		   output += " ";
 	   }
 	   
 	   System.out.println(output);
    }
    
    
    public static String interpreter(String[] commandLineArguments) throws ClassNotFoundException, SQLException{
 	    if (commandLineArguments.length < 1) {System.out.println("Please input help");}
 	      return parseCommand(commandLineArguments);  
    }
    
 	public static void main(String[] args) throws ClassNotFoundException, SQLException{
 		System.out.println(interpreter(args));
 	}
 } 
