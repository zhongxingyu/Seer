 package ch.epfl.data.distribdb.app;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Scanner;
 
 import ch.epfl.data.distribdb.lowlevel.AbstractDatabaseManager;
 import ch.epfl.data.distribdb.lowlevel.DatabaseManager;
 import ch.epfl.data.distribdb.parsing.Parser;
 import ch.epfl.data.distribdb.parsing.QueryRelation;
 import ch.epfl.data.distribdb.queryexec.ExecStep;
 import ch.epfl.data.distribdb.queryexec.GraphProcessor;
 import ch.epfl.data.distribdb.queryexec.StepExecutor;
 import ch.epfl.data.distribdb.queryexec.TableManager;
 import ch.epfl.data.distribdb.querytackling.QueryGraph;
 
 public class CommandLine extends AbstractApp {
 
 	DatabaseManager dbManager;
 	
 	@Override
 	public void run(String[] args) throws SQLException, InterruptedException {
 		
 		if (args.length < 1) {
 			System.out.println("Arguments: config-file");
 			System.exit(1);
 		}
 		
         dbManager = createDatabaseManager(args[0]);
         dbManager.setResultShipmentBatchSize(5000);
 		TableManager tableManager = new TableManager();
 		
 		System.out.println(
 				"Welcome to Distributed SQL Query Engine!\n" +
				"Please enter your query, or type 'exit' when you are finished."
 				);
 		
 		Scanner input = new Scanner(System.in);
 		
 		while (true) {
			System.out.print("\n>>> ");
 			
 			String query = input.nextLine();
 			if (query.equalsIgnoreCase("exit")) break;
 			else if (query.trim().isEmpty()) continue;
 			else if (command(query)) continue;
 			
 			try {
 				QueryRelation queryTree = Parser.parse(query);
				if(DEBUG) System.out.println("\nQUERY: " + queryTree);
 				QueryGraph queryGraph = new QueryGraph(queryTree);
				if(DEBUG) System.out.println("\nQUERY GRAPH:\n" + queryGraph);
 				
 				GraphProcessor queryGraphProcessor = new GraphProcessor(tableManager, queryGraph);
 				List<ExecStep> execSteps = queryGraphProcessor.processGraph();
				if(DEBUG) System.out.println("QUERY PLAN:\n" + Arrays.toString(execSteps.toArray()));
 				StepExecutor queryStepExecutor = new StepExecutor(dbManager, tableManager, allNodes);
 				ResultSet finalResultSet = queryStepExecutor.executeSteps(execSteps);
				System.out.println();
 				TablePrinter.printTableData(System.in, System.out, finalResultSet);
 				
 			} catch (Exception e) {
 				//e.printStackTrace();
				System.out.println(e);
 			} finally {
 				tableManager.cleanTempTables(dbManager, allNodes);
 			}
 		}
 		input.close();
 		dbManager.shutDown();
 	}
 	
 	private boolean command(String query) throws SQLException, InterruptedException {
 		if(!query.startsWith("+"))
 			return false;
 		query = query.substring(1);
 		if (query.equalsIgnoreCase("cleanall")) { 
 			new TableManager().cleanAllTmpTables(dbManager, allNodes);
 			System.out.println("Cleaned all");
 		} else if (query.equalsIgnoreCase("dbdebugon")) {
 			AbstractDatabaseManager.DEBUG = true;
 			System.out.println("DB DEBUG ON");
 		} else if (query.equalsIgnoreCase("dbdebugoff")) {
 			AbstractDatabaseManager.DEBUG = false;
 			System.out.println("DB DEBUG OFF");
 		} else if (query.equalsIgnoreCase("debugon")) {
 			DEBUG = true;
 			System.out.println("DEBUG ON");
 		} else if (query.equalsIgnoreCase("debugoff")) {
 			DEBUG = false;
 			System.out.println("DEBUG OFF");
 		}
 		return true;
 	}
 	
 	public static void main(String[] args) throws SQLException, InterruptedException {
 		new CommandLine().run(args);
 	}
 }
