 import java.util.ResourceBundle;
 
 
 public class Main {
 	
 	public static void checkOver(String[] args) throws Exception {
 		if (args.length == 0) {
 			throw new Exception("No Files");
 		}
 		int i,j; //loops counters
 		for (i=0; i < args.length; i++){
 			for(j = i+1; j < args.length; j++){
 				if (args[i].equals(args[j])){
 					throw new Exception("2 queried files are the same file");
 				}
 			}
 		}
 	}
 	
 	/**
 	 * main method of the history system
 	 * @param args command line arguments supplied by the user
 	 */
 	public static void main(String[] args){
 		long start = System.currentTimeMillis();
 		
 		ResourceBundle bundle = ResourceBundle.getBundle("config");
 		
 		try {
 			checkOver(args);
 			//information is parsed and printed here using a HistoryParser instance
 		    HistoryParser history = new HistoryParser(args);
 		    history.printHistoryInformation();  
 		}
 		//catches any exception thrown
 		catch (Exception e) {
 			e.printStackTrace(); //prints out the issue caught
 		}
 		long end = System.currentTimeMillis();
		if (bundle.getString("timeToggle").equals("true")) {
 			System.out.println("\n("+(end-start)/1000.00+" seconds for completion)");
 		}
 	}
 	
 }
