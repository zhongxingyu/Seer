 package comm;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import jira.Jira;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 
 import bugzilla.Bugzilla;
 import db.SocialDb;
 import db.LinkerDb;
 import db.Resources;
 
 public class Main
 {
 	public static SocialDb db;
 	public static LinkerDb linkdb;
 	public static Jira jira;
 	
 	@SuppressWarnings("static-access")
 	public static void main(String[] args)
 	{	
 		System.out.println("Com2Pgsql tool developed by eggnet at UVic.");
 		CommandLineParser parser = new GnuParser();
 		try {
 			if (args.length < 1)
 			{
 				printMan();
 				return;
 			}
 			else
 			{
 				Options options = new Options();
 				
 				Option bugzilla = OptionBuilder.withArgName("b")
 											   .hasArg()
 											   .create("b");
 				
 				Option database = OptionBuilder.withArgName("d")
 						   					   .hasArg()
 						   					   .create("d");
 				
 				Option email 	= OptionBuilder.withArgName("e")
 											   .hasArg()
 											   .create("e");
 				
 				Option jiraOpt 	= OptionBuilder.withArgName("j")
 											   .hasArgs(3)
 											   .create("j");
 				
 				options.addOption(bugzilla);
 				options.addOption(database);
 				options.addOption(email);
 				options.addOption(jiraOpt);
 				
 				CommandLine line = parser.parse(options, args);
 				
 				db = new SocialDb();
 				linkdb = new LinkerDb();
 				
 				// Check for database
 				if(line.hasOption("d")) {
 				    String[] values = line.getOptionValues("d");
 				    if(values.length != 1) {
 				    	System.out.println("-d flag used incorrectly.");
 				    	printMan();
 				    	return;
 				    }
 				    else {
 				    	// Create the DB
 				    	System.out.println("Creating database");
 				    	ComResources.log(ComResources.dbUrl);
 						db.connect(Resources.EGGNET_DB_NAME, ComResources.COM_QUEUE_WORKER_LIMIT);
 						db.createDb(line.getOptionValue("d"));
 				    }
 				}
 				
 				// Check for bugzilla
 				if(line.hasOption("b")) {
 				    String[] values = line.getOptionValues("b");
 				    if(values.length != 1) {
 				    	System.out.println("-b flag used incorrectly.");
 				    	printMan();
 				    	return;
 				    }
 				    else {
 				    	Bugzilla bugz = new Bugzilla(db, line.getOptionValue("b"));
 				    	bugz.parseBugzilla();
 				    }
 				}
 				
 				// Check for email
 				if(line.hasOption("e")) {
 				    String[] values = line.getOptionValues("e");
 				    if(values.length != 1) {
 				    	System.out.println("-e flag used incorrectly.");
 				    	printMan();
 				    	return;
 				    }
 				    else {
 				    	
 				    }
 				}
 				
 				// Check for jira
 				if (line.hasOption("j")) {
 					String[] values = line.getOptionValues("j");
 					if (values.length > 4 || values.length < 3) {
 						System.out.println("-j flag used incorrectly");
 						printMan();
 						return;
 					}
 					else {
 						jira = new Jira();
 						if (values[0].equals("-l"))
 						{
 							// only link.
 							db.connect(values[1], ComResources.COM_QUEUE_WORKER_LIMIT);
 							linkdb.connect(values[2]);
 							linkdb.setBranchName("master");
 							jira.linkJira(null, linkdb, db);
 							
 						}
 						else
 						{
 							System.out.println("Running Jira parser on " + line.getOptionValue("j"));
 							db.connect(values[0], ComResources.COM_QUEUE_WORKER_LIMIT);
 							linkdb.connect(values[1]);
 							linkdb.setBranchName("master");
 							jira.parseJira(values[2], linkdb, db);
 						}
 					}
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			printMan();
 		}
 	}
 	
 	private static void printMan() {
 		try {
 			// Print the man page
 			BufferedReader in = new BufferedReader(new InputStreamReader(ComResources.class.getResourceAsStream("man.txt")));
 			String line;
 			while ((line = in.readLine()) != null) {
 				System.out.println(line);          
 			}         
 			in.close();
 		}
 		catch (Exception ex) {
 			ex.printStackTrace();
 			System.out.println("There was an error printing the man page");
 		}
 	}
 }
