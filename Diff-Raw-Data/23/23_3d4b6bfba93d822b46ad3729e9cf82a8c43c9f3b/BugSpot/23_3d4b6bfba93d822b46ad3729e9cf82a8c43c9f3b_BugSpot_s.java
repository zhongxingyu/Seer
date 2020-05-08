 package com.github.r1j0.bugspot;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.github.r1j0.bugspot.repository.LogEntries;
 import com.github.r1j0.bugspot.repository.Repository;
 import com.github.r1j0.bugspot.repository.SvnRepository;
 
 
 public class BugSpot {
 
 	public static void main(String[] args) {
 		String url = "http://svn.apache.org/repos/asf/hadoop/common/trunk/hadoop-common-project";
 		String username = "anonymous";
 		String password = "anonymous";
 
 		Repository svnRepository = new SvnRepository(url, username, password);
		// List<LogEntries> logEntries = svnRepository.checkout(500180,
		// 1241260);
 		List<LogEntries> logEntries = svnRepository.checkout(1200180, 1241260);
 
 		Computate computate = new Computate(logEntries);
 		computate.compute();
 		Map<String, Double> hotspots = computate.getHotspots();
 
 		for (Entry<String, Double> entrySet : hotspots.entrySet()) {
 			System.out.println("PATH: " + entrySet.getKey() + " VALUE: " + entrySet.getValue());
 		}	
 
 	/**
 	 * <code>
  		Options options = new Options();
 		options.addOption(new Option("h", "help", false, "print this message"));
 		options.addOption(new Option("version",
 				"print the version information and exit"));
 		OptionBuilder.withArgName("u");
 		OptionBuilder.withDescription("repository username");
 		OptionBuilder.hasArg();
 		OptionBuilder.withArgName("USERNAME");
 		options.addOption(OptionBuilder.create("u"));
 
 		// create the parser
 		CommandLineParser parser = new PosixParser();
 
 		args = new String[] { "-h", "--version" };
 
 		// parse the command line arguments
 		CommandLine line = null;
 
 		try {
 			line = parser.parse(options, args);
 		} catch (ParseException e) {
 			System.out.println(e.getMessage() + "\n");
 			HelpFormatter formatter = new HelpFormatter();
 			formatter.printHelp("BugSpot", options);
 			System.exit(1);
 		}
 
 		if (line.hasOption("h")) {
 			HelpFormatter formatter = new HelpFormatter();
 			formatter.printHelp("BugSpot", options);
 		}
 
 		if (line.hasOption("version")) {
 			System.out.println("Version is crap.");
 		}
 		
 		if (line.hasOption("u")) {
 			System.out.println("username: " + line.getOptionValue("u"));
 		}
 	</code>
 	 */
     }
 }
