 package ch.ethz.mlmq.main;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import ch.ethz.mlmq.logging.LoggerUtil;
 
 public class Main {
 	private static final Logger logger = Logger.getLogger(Main.class.getSimpleName());
 
 	public static void main(String[] args) throws IOException {
 		if (args.length == 0) {
 			showHelpAndExit();
 			return;
 		}
 
 		Map<String, String> argList = parseArgs(args);
 
 		initLogging(argList);
 
 		switch (args[0]) {
 		case "client":
 			mainClient(argList);
 			break;
 		case "broker":
 			mainBroker(argList);
 			break;
 		case "dbscript":
 			mainDbScript(argList);
 			break;
 		default:
 			showHelpAndExit();
 			return;
 		}
 
 	}
 
 	private static void initLogging(Map<String, String> argList) throws IOException {
 		String loggerConfigFile = argList.remove("l");
 		if (loggerConfigFile == null) {
 			LoggerUtil.initDefault();
 			logger.info("Logger initialized with default configuration");
 		} else {
 			LoggerUtil.initFromFile(loggerConfigFile);
 			logger.info("Logger initialized from " + loggerConfigFile);
 		}
 	}
 
 	private static void mainClient(Map<String, String> argList) {
 		String config = argList.remove("config");
 
 		if (!argList.isEmpty()) {
 			System.out.println("Parameters not understood " + argList);
 		}
 
 		if (config == null) {
 			System.out.println("Missing Parameter -config");
 		}
 
 		ClientMain clientMain = new ClientMain();
 		clientMain.run(config);
 	}
 
 	private static int mainBroker(Map<String, String> argList) {
 		String config = argList.remove("config");
 
 		if (!argList.isEmpty()) {
 			System.out.println("Parameters not understood " + argList);
 		}
 
 		if (config == null) {
 			System.out.println("Missing Parameter -config");
 		}
 
 		BrokerMain main = new BrokerMain();
 		return main.run(config);
 	}
 
 	private static Map<String, String> parseArgs(String[] args) {
 		Map<String, String> result = new HashMap<String, String>();
 
 		String keyPrefix = "-";
 
 		// skip <type> argument
 		for (int i = 1; i < args.length; i++) {
 
 			String argsI = args[i];
 			if (argsI.startsWith(keyPrefix)) {
 
 				String key = argsI.substring(1, argsI.length());
 				String value = null;
 				if (args.length > i + 1 && !args[i + 1].startsWith(keyPrefix)) {
 					value = args[i + 1];
 				} else {
 					value = "true";
 				}
 
 				result.put(key, value);
 			}
 		}
 		return result;
 	}
 
 	private static void showHelpAndExit() {
 
 		//@formatter:off
 		System.out.println("usage: java -jar target.jar <type>");
 		System.out.println();
 		System.out.println("Types:");
 		System.out.println("\tclient_sender\tStarts a client instance\n\t\t\t-config [ConfigFilePath]");
 		System.out.println("\tclient_receiver\tStarts a client instance\n\t\t\t-config [ConfigFilePath]");
 		System.out.println("\tbroker\tStarts a broker instance (for the middleware)"
 				+ "\n\t\t\t-config [ConfigFilePath] Broker Configuration Property file"
 				+ "\n\t\t\t-l [Logger Configuration] Logger Configuration Property file (optional) overrides default configuration"
 				);
 		System.out.println("\tdbscript"
 				+ "\n\t\t\t-file [ScriptFilePath] optional parameter"
 				+ "\n\t\t\t-url [jdbc:postgresql://host:port]"
 				+ "\n\t\t\t-db [database name]"
 				+ "\n\t\t\t-user [usename]"
				+ "\n\t\t\t-password [password]"
 				+ "\n\t\t\t-createDatabase optional flag"
 				+ "\n\t\t\t-createTables optional flag"
 				+ "\n\t\t\t-dropDatabase optional flag"
 				+ "\n\t\t\t-l [Logger Configuration] logger config property file"
 				);
 		//@formatter:on
 
 		System.exit(2);
 	}
 
 	private static int mainDbScript(Map<String, String> argList) {
 		try {
 
 			String file = argList.remove("file");
 			String url = argList.remove("url");
 			String user = argList.remove("user");
 			String password = argList.remove("password");
 			String db = argList.remove("db");
 			boolean createDatabase = Boolean.parseBoolean(argList.remove("createDatabase"));
 			boolean createTables = Boolean.parseBoolean(argList.remove("createTables"));
 			boolean dropDatabase = Boolean.parseBoolean(argList.remove("dropDatabase"));
 
 			if (!argList.isEmpty()) {
 				System.out.println("Parameters not understood " + argList);
 			}
 
 			if (url == null) {
 				System.out.println("Missing Parameter -url");
 			}
 
 			if (user == null) {
 				System.out.println("Missing Parameter -user");
 			}
 
 			if (password == null) {
 				System.out.println("Missing Parameter -password");
 			}
 
 			if (db == null) {
 				System.out.println("Missing Parameter -db");
 			}
 
 			DbScriptMain.run(file, url, db, user, password, createDatabase, createTables, dropDatabase);
 			return 0;
 		} catch (IOException | SQLException e) {
 			logger.severe("Exception " + LoggerUtil.getStackTraceString(e));
 			return -1;
 		}
 	}
 }
