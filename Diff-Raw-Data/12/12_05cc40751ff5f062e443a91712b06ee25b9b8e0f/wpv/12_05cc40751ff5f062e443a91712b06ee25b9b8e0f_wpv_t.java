 /**
  * This is the "main" class of the wpv program. Execution starts here. This
  * section of the code is responsible for carrying out all of the steps in
  * validating the specified html code.
  *
  * Author: Tyler Vodak
  * Date: 11/6/2011
  */
  
  import java.util.*;
  
  public class wpv {
  
  	/* These are specific return code for the wpv class.
  	 * Don't use them in any other class.
  	 */
  	private static short ARG_COUNT = 1; //you need at least this many arguments
  	private static short TOO_FEW_ARGS = 1;
  	private static short SHOW_HELP_MSG = 2;
  	private static short FILE_NOT_EXIST = 3;
  	 
  	private static void printUsage() {
  		System.out.println("usage: wpv [options] <args>");
  	}
  	
  	private static boolean showHelp(String[] args) {
  		if(0 < args.length) {
  			//we will only check the first argument for any help request
  			if(args[0].equals("-h") || args[0].equals("-help") ||
  				args[0].equals("--help")) {
  					return true;
  				}
  		}
  		return false;
  	}
  
  	public static void main(String[] args) {
  		if(wpv.ARG_COUNT > args.length) {
  			wpv.printUsage();
  			System.exit(TOO_FEW_ARGS);
  		}
  		
  		if(wpv.showHelp(args)) {
  			wpv.printUsage();
  			System.exit(SHOW_HELP_MSG);
  		}
  		
  		//TASK 1: Open the file(s)
  		FileUtil fData = new FileUtil(args[args.length - 1]);
  		if(!fData.doesSourceExist()) {
  			System.out.println("\"" + args[args.length - 1] + "\" does not exist");
  			System.exit(FILE_NOT_EXIST);
  		}
  		
  		//TASK 2: Read and track the tag data in the file(s)
  		Tags n_tags = new Tags(fData);
  		n_tags.validate_tags();
  		
  		//TASK 3: Get the list of errors and print them out
  		ArrayList<ResultSet> r_set = n_tags.getResultSet();
 			System.out.println("R SET SIZE: " + r_set.size());
  		for(int i = 0; i < r_set.size(); i++) {
  			System.out.print(r_set.get(i));
  		}
  	}
  }
