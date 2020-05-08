 package ch.epfl.bbcf.psd;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Map;
 import java.util.zip.DataFormatException;
 
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 
 
 
 
 public class Main {
 
 	private static final int[] zooms = {1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000};
 	public static final int TAB_WIDTH = 100;
 	public static final int LIMIT_QUERY_SIZE = 1000000;
 
 	public static final String decimals = "%.2f";
 
 	public static Level loglevel = Level.INFO;
 
 	public static Logger logger;
 
 	public static void usage(){
 		String usage = "USAGE\n" +
 		"\targs[0] database : the sqlite file\n" +
 		"\targs[1] sah1 : the sha1 of this database\n" +
 		"\targs[2] output_dir : the output directory\n" +
 		"\targs[3] loglevel : (optionnal) can be TRACE, DEBUG, INFO, OFF, WARN, ERROR, FATAL. Default is INFO";
 		logger.warn(usage);
 	}
 
 	public static void main (String[] args) {
 
 
 		if(args.length < 3){
 			logger = initLogger("psd", Level.ERROR);
 			logger.error("no enought args");
 			usage();
 		} else {
 			File database = new File(args[0]);
 			String sha1 = args[1];
 			String outputDir = args[2];
 			if (args.length == 4){
 				loglevel = Level.toLevel(args[3]);
 			};
 			logger = initLogger("psd", loglevel);
 
 
 
 
 			try {
 				logger.info("processing " + database + " with sha1(" + sha1 + ") on directory : " + outputDir);
 				long start = System.currentTimeMillis();
 				process(database, sha1, outputDir);
 
 				long end = System.currentTimeMillis();
 				logger.debug("Execution time was ~"+(int)((end-start) / 1000) +"s.");
 			} catch (ClassNotFoundException e) {
 				logger.error(e);
 				for (StackTraceElement el : e.getStackTrace()){
 					logger.debug(el.getFileName() + " .. " +
 							el.getClassName() + "::" + el.getMethodName() 
 							+" at line " + el.getLineNumber());
 				}
 				System.err.println("Class not found Exception");
 				System.exit(1);
 			} catch (SQLException e) {
 				logger.error(e);
 				for (StackTraceElement el : e.getStackTrace()){
 					logger.debug(el.getFileName() + " .. " +
 							el.getClassName() + "::" + el.getMethodName() 
 							+" at line " + el.getLineNumber());
 				}
 				System.err.println("Java.sql.SQLException " + e.getMessage());
 			} catch (DataFormatException e) {
 				for (StackTraceElement el : e.getStackTrace()){
 					logger.debug(el.getFileName() + " .. " +
 							el.getClassName() + "::" + el.getMethodName() 
 							+" at line " + el.getLineNumber());
 				}
 				System.err.println("DataFormatException " + e.getMessage());
 				System.exit(1);
 			}
 		}
 	}
 	/**
 	 * Calculate scores at different zoom level for an sqlite database and create one database for each.
 	 * @param database : the sqlite file
 	 * @param sha1 : the sha1 of this database
 	 * @param outputDir : the output directory
 	 * @throws SQLException 
 	 * @throws ClassNotFoundException 
 	 * @throws DataFormatException 
 	 */
 	private static void process(File database, String sha1, String outputDir) throws ClassNotFoundException, SQLException, DataFormatException {
 
 		Map<String, Integer> chromosomes; // contains all chromosomes and length in the database
 		Connection principal; // the connection to the main database
 
 		principal = SQLiteConnector.getConnection(database.getAbsolutePath());
 		chromosomes = SQLiteConnector.getChromosomesAndLength(principal);
 
 		File output = new File(outputDir + File.separator + sha1);
 		output.mkdir();
 		logger.trace("output is " + output);
 
 		for(Map.Entry<String, Integer> entry : chromosomes.entrySet()){
 
 			String chromosome = entry.getKey();
 			logger.debug("doing chromosome " + chromosome);
 
 			ConnectionStore connectionStore = SQLiteConnector.createOutputDatabases(output.getAbsolutePath(), chromosome, zooms);
 
 			ResultSet scores = SQLiteConnector.getScores(principal, chromosome);
 			Tree tree = new Tree(connectionStore, chromosome, output.getAbsolutePath()	);
 			tree.process(scores);
 			scores.close();
 			connectionStore.destruct();
 
 		}
 
 		principal.close();
 	}	
 
 
 	public static boolean floatEquals(float a,float b){
 		float epsilon = 0.0001f;
 		boolean z = (Math.abs(a-b)<epsilon);
 		return z;
 	}
 
 
 	public static Logger initLogger(String name, Level level) {
 		Logger out = Logger.getLogger(name);
 		out.setAdditivity(false);
 		out.setLevel(level);
 		PatternLayout layout = new PatternLayout("%d [%t] %-5p %c - %m%n");
 		ConsoleAppender appender = new ConsoleAppender(layout);
 		out.addAppender(appender);
 		return out;
 	}
 
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
