 package org.qooxdoo.charless.buildtool;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.concurrent.TimeUnit;
 
 import javax.script.ScriptException;
 
 import org.apache.log4j.Logger;
 import org.python.util.InteractiveConsole;
 import org.qooxdoo.charless.buildtool.config.Config;
 import org.qooxdoo.charless.buildtool.config.QbtConfig;
 
 
 /**
  * The Generator command line interface
  * 
  * @author charless
  * 
  */
 public class QooxdooBuildTool  {
 	
 	public static File qooxdooSdkPath;
     private final static Logger logger = Logger.getLogger(QbtConfig.class);
 
 	
 	public static void main(String[] args) throws ScriptException {
 		// Init config
 		QbtConfig cfg = QbtConfig.init();
		qooxdooSdkPath = new File(cfg.getQooxdooPath());
 		if (qooxdooSdkPath == null) {
 			logger.error(
 					"Can not find Qooxdoo sdk path; set the \'QOOXDOO_PATH\' environment variable"
 			);	
 			System.exit(1);
 		}
 		if (! qooxdooSdkPath.isDirectory()) {
 			logger.error(
 					"Qooxdoo path \'"+qooxdooSdkPath.getAbsolutePath()+"\' is not a directory or does not exists"
 			);	
 			System.exit(1);
 		}
 		if (! Config.isaQxApplicationDirectory(new File("."))) { 
 			try {
 				cfg.write();
 			} catch (Exception e) {
 				logger.warn("Could not write configuration in file \'"+QbtConfig.QBT_JSON_FILE+"\':"+e);
 			}
 		}
 		// Parse args
 		if (args.length ==0 || args[0].startsWith("-")) {
 			// Console must be started from an app root
 			if (! Config.isaQxApplicationDirectory(new File("."))) { 
 				logger.error(
 						"Could not find the \'"+Config.APPLICATION_JSON_FILE+"\' file; the console must be launched from a qooxdoo application root directory"
 				);
 				logger.info("Type \'qbt help\' for getting started");
 				System.exit(1);
 			}
 			System.exit(console(args));
 		}
 		// Help
 		if ("help".equals(args[0].toLowerCase())) {
 			help();
 			System.exit(0);
 		}
 		// Action
 		String pythonScriptName = args[0];
 		if ("create-application".equals(args[0].toLowerCase())) {
 			pythonScriptName = "create-application.py";
 			args[0] = pythonScriptName;
 		} else if(! args[0].endsWith("py")) {
 			// jobs to be run
 			pythonScriptName = "generator.py";
 		}
 		// Args
 		if (pythonScriptName.equals(args[0])) {
 			// Remove scriptname from args
 			String [] pythonScriptArgs = new String[args.length-1];
 			for (int i=1;i<args.length;i++) {
 				pythonScriptArgs[i-1] = args[i];
 			}
 			args = pythonScriptArgs;
 		}
 		String pyScriptWithArgs = pythonScriptName;
 		for (int i=0; i<args.length;i++) {
 			pyScriptWithArgs += " "+args[i];
 		}
 		// Check script existence
 		File pythonScript = QxEmbeddedJython.resolvePythonScriptPath(qooxdooSdkPath,pythonScriptName);
 		if (! pythonScript.exists() || ! pythonScript.canRead()) {
 			logger.error(
 					"The python script \'"
 					+ pythonScript.getAbsolutePath()
 					+"\' does not exist or is not readable !"
 			);
 			System.exit(1);
 		}
 		long starts = System.currentTimeMillis();
 		logger.info("Initializing Jython...");
 		QxEmbeddedJython qx = new QxEmbeddedJython(qooxdooSdkPath);
         long passedTimeInSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis() - starts, TimeUnit.MILLISECONDS);
         logger.info("Jython initialized in "+passedTimeInSeconds+" seconds");
         starts = System.currentTimeMillis();
 		logger.info("Starting \'"+pyScriptWithArgs+"\'");
 		try {
 			qx.run(pythonScriptName,args);
 		} catch (Exception e) {
 			// Ignore exception at the moment
 			// TODO: Add a java property to turn stack trace on
 		}
         passedTimeInSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis() - starts, TimeUnit.MILLISECONDS);
         logger.info("DONE in "+passedTimeInSeconds+" seconds");
 	}
 			
 	public static void help () {
 		System.out.println("====================");
 		System.out.println(" qOOXDOO bUILD tOOL");
 		System.out.println("====================");
 		System.out.println();
 		System.out.println("NEW PROJECT");
 		System.out.println("    $ qbt create-application -t <type> -n <appName> [other_options]");
 		System.out.println();
 		System.out.println("JOBS");
 		System.out.println(" Go to a qooxdoo application root directory and type:");
 		System.out.println("    $ qbt \"job1,job2\" [generator_options]");
 		System.out.println(" Or by using the interactive console");
 		System.out.println("    $ qbt [generator_options]");
 		System.out.println("      >>> job1()");
 		System.out.println("      >>> job2()");
 		System.out.println("      >>> jobs(\"job1,job2\") # in one shot");
 		System.out.println("      >>> jobs(\"x\")         # full list of available jobs");
 		System.out.println();
 		System.out.println("EXAMPLES");
 		System.out.println(" - Create a qooxdoo ria application");
 		System.out.println("    $ qbt create-application -t gui -n myRiaApp");
 		System.out.println(" - Compile a qooxdoo application (from it's root directory)");
 		System.out.println("    $ qbt build");
 		System.out.println(" or by using the interactive console:");
 		System.out.println("    $ qbt console");
 		System.out.println("      >>> build()");
 		System.out.println();
 		System.out.println("OTHERS");
 		System.out.println("    $ qbt help");
 		System.out.println("    $ qbt <qxPythonScriptName> [script_options]");
 		
 		
 	}
 	
 	public static int console(String[] args) {
 		logger.info("Entering interactive console, please wait...");
 		try {
 			QxEmbeddedJython qxjython = new QxEmbeddedJython(qooxdooSdkPath);
 			InteractiveConsole c = qxjython.getQxInteractiveConsole(args);
 			c.interact();
 		} catch(Exception e) {
 			System.out.println(e);
 			return(1);
 		}
 		return(0);
 		
 	}
 	
 	
 
 }
 
