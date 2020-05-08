 package net.jiehou.tools;
 
 import java.io.IOException;
 import java.sql.*;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import org.sqlite.*;
 
 import net.jiehou.tools.ServiceHelper;
 import net.jiehou.tools.ServiceExecutor;
 
 public class App 
 {
 	/**
 	 * Args for fetching:
 	 *     [dbFilePath] [key] 
 	 * Args for reading:
 	 * 	   [dbFilePath] getAllTimeSum [columnName]
 	 * @param args
 	 * @throws IOException
 	 * @throws ExecutionException
 	 * @throws InterruptedException
 	 * @throws SQLException
 	 * @throws ClassNotFoundException
 	 */
     public static void main( String[] args ) throws IOException, ExecutionException, InterruptedException, SQLException, ClassNotFoundException
     {
 		
		if(args.length <= 2)
 			throw new RuntimeException("Arguments is too few");
 		
 		DatabaseExecutor dbExecutor = new DatabaseExecutor(args[0]);
 		
 		if(args.length == 2) { // fetch data from Kanbanery and write the data to database
 	    	String key = args[1];
 			ServiceHelper service = new ServiceHelper(key);
 			ServiceExecutor svExecutor = new ServiceExecutor(service);
 			
 			// fetch the sums
 			float sumAll = svExecutor.calculatePointsForFinishedTasks();
 			float sumS = svExecutor.calculatelPointsForFinishedTasksOfType("Secure Your Position");
 			float sumT = svExecutor.calculatelPointsForFinishedTasksOfType("20% Project");
 			float sumP = svExecutor.calculatelPointsForFinishedTasksOfType("Personal Project");
 			float sumI = svExecutor.calculatelPointsForFinishedTasksOfType("IP Review");
 			float sumO = svExecutor.calculatelPointsForFinishedTasksOfType("Outta Here!");
 			float sumM = svExecutor.calculatelPointsForFinishedTasksOfType("Mental Support");
 			float sumMM = svExecutor.calculatelPointsForFinishedTasksOfType("Make Ends Meet");
 			float sumGW = svExecutor.calculatelPointsForFinishedTasksOfType("Government-Forced Work");
 			float sumLI = svExecutor.calculatelPointsForFinishedTasksOfType("Life Improvement");
 			float sumBC = svExecutor.calculatelPointsForFinishedTasksOfType("Body Care");
 			float sumSA = svExecutor.calculatelPointsForFinishedTasksOfType("Social Activities");
 			float sumRM = svExecutor.calculatelPointsForFinishedTasksOfType("Relationship Matters");
 			
 			
 			SumsTableEntry entry = new SumsTableEntry(sumAll, sumS, sumT, sumP, sumI, sumO,
 					sumM, sumMM, sumGW, sumLI, sumBC, sumSA, sumRM);
 			
 			// append the sums into table "sums"
 			dbExecutor.appendSumsTable(entry);
 			
 			// fetch finished tasks
 			List<FinishedTask> tasks = svExecutor.calculateAllFinishedTasks();
 			
 			// overwrite finished tasks into table "tasks"
 			dbExecutor.updateTasksTable(tasks);
 			
 			service.close();
 		}
 		else if(args[1].equals("getAllTimeSum")) {
 			System.out.print(dbExecutor.getAllTimeSum(args[2]));
 		}
 			
 		dbExecutor.close();
 		System.exit(0);
     }
 }
