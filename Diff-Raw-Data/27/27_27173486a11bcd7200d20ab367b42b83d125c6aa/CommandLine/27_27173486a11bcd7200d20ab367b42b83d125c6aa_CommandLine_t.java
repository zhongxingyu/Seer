 package ch.epfl.ad.app;
 
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Scanner;
 
 import ch.epfl.ad.AbstractQuery;
 import ch.epfl.ad.db.DatabaseManager;
 import ch.epfl.ad.db.parsing.Parser;
 import ch.epfl.ad.db.parsing.QueryRelation;
 import ch.epfl.ad.db.queryexec.ExecStep;
 import ch.epfl.ad.db.querytackling.GraphProcessor;
 import ch.epfl.ad.db.querytackling.QueryGraph;
 
 public class CommandLine extends AbstractQuery {
 
 	@Override
 	public void run(String[] args) throws SQLException, InterruptedException {
 		
		if (args.length < 1) {
 			System.out.println("Arguments: config-file");
 			System.exit(1);
 		}
		
        DatabaseManager dbManager = createDatabaseManager(args[0]);
         dbManager.setResultShipmentBatchSize(5000);
 		
 		System.out.println(
 				"Welcome to Distributed SQL Query Engine!\n" +
 				"Please enter your query, or type 'exit' when you are finished.\n"
 				);
 		
 		Scanner input = new Scanner(System.in);
 		
 		while (true) {
 			System.out.print(">>> ");
 			
 			String query = input.nextLine();
 			if (query.equalsIgnoreCase("exit")) break;
 			else if (query.trim().isEmpty()) continue;
 			
 			try {
 				QueryRelation queryTree = new Parser().parse(query);
 				System.out.println("QUERY: " + queryTree);
 				QueryGraph queryGraph = new QueryGraph(queryTree);
 				System.out.println("QUERY GRAPH:\n" + queryGraph);
 				System.out.println("QUERY EXECUTION:\n");
 				GraphProcessor queryGraphProcessor = new GraphProcessor(queryGraph);
 				queryGraphProcessor.executeSteps(dbManager, allNodes);
 				System.out.println();
 			} catch (Exception e) {
 				System.out.println(e.getMessage() + "\n");
 			}
 		}
 		input.close();
 	}
 	
 	public static void main(String[] args) throws SQLException, InterruptedException {
 		new CommandLine().run(args);
 	}
 }
