 package callgraphanalyzer;
 
 import java.util.List;
 
 import models.Change;
 
 import db.CallGraphDb;
 public class Main {
 	/**
 	 * @param [dbname] [commit_range_start] [commit_range_end] 
 	 */
 	public static void main(String[] args) {
 		System.out.println("CallGraphAnalyzer tool developed by eggnet.");
		System.out.println();
 		CallGraphDb db = new CallGraphDb();
 		try {
 			if (args.length < 4 )
 			{
 				System.out.println("Retry: callGraphAnalyzer [dbname] [branchname] [commit_range_start] [commit_range_end]");
 				throw new ArrayIndexOutOfBoundsException();
 			}
 			else
 			{
 				try 
 				{
 					db.connect(args[0]);
 					db.setBranchName(args[1]);
 					NetworkBuilder networkBuilder = new NetworkBuilder(db, args[2], args[3]);
 					networkBuilder.buildAllNetworks();
 				} 
 				catch (Exception e) 
 				{
 					e.printStackTrace();
 				} 
 			}
 		}
 		catch (ArrayIndexOutOfBoundsException e)
 		{
 			System.out.println("Usage callGraphAnalyzer <input postgres sql>");
 		}	
 	}
 }
 
