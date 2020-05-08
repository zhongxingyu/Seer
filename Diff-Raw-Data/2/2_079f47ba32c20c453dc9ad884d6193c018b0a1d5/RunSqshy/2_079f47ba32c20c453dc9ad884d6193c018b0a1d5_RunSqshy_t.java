 package com.internetitem.sqshy;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import jline.Terminal;
 import jline.TerminalFactory;
 import jline.console.ConsoleReader;
 
 import com.internetitem.sqshy.command.Commands;
 import com.internetitem.sqshy.command.ConnectCommand;
 import com.internetitem.sqshy.command.DisconnectCommand;
 import com.internetitem.sqshy.command.EchoCommand;
 import com.internetitem.sqshy.command.ReconnectCommand;
 import com.internetitem.sqshy.command.SetCommand;
 import com.internetitem.sqshy.config.Configuration;
 import com.internetitem.sqshy.config.DatabaseConnectionConfig;
 import com.internetitem.sqshy.config.DriverMatch;
 import com.internetitem.sqshy.config.args.CommandLineArgument.ArgumentType;
 import com.internetitem.sqshy.config.args.CommandLineParseException;
 import com.internetitem.sqshy.config.args.CommandLineParser;
 import com.internetitem.sqshy.config.args.ParsedCommandLine;
 import com.internetitem.sqshy.settings.Settings;
 
 public class RunSqshy {
 
 	public static CommandLineParser buildCommandLineParser() {
 		CommandLineParser parser = new CommandLineParser();
 		parser.addArg("help", "help", "h", ArgumentType.NoArg, "View help message");
 		parser.addArg("connect", "connect", "c", ArgumentType.RequiredArg, "Connect to saved alias\n(other connection settings override those in the alias");
 		parser.addArg("driver", "driver", "d", ArgumentType.RequiredArg, "JDBC Driver Class\nIf not specified, will be guessed based on URL");
 		parser.addArg("url", "url", "u", ArgumentType.RequiredArg, "JDBC URL");
 		parser.addArg("username", "username", "U", ArgumentType.RequiredArg, "Database Username");
 		parser.addArg("password", "password", "P", ArgumentType.RequiredArg, "Database Password\nIf the string @ is used, the user will be prompted");
 		parser.addArg("property", "property", "p", ArgumentType.List, "JDBC Properties (key=value)");
 		parser.addArg("settings", "settings", null, ArgumentType.RequiredArg, "Load saved settings from file (defaults to ~/.sqshyrc)\nMissing files are ignored");
 		parser.addArg("set", "set", "s", ArgumentType.List, "Set variables");
 		return parser;
 	}
 
 	public static void main(String[] args) throws Exception {
 		CommandLineParser parser = buildCommandLineParser();
 		ParsedCommandLine cmdline;
 		try {
 			cmdline = parser.parse(args);
 		} catch (CommandLineParseException e) {
 			System.err.println("Error: " + e.getMessage());
 			System.err.println();
 			System.err.println(parser.getUsageString());
 			System.exit(1);
 			return;
 		}
 
 		if (cmdline.getBoolValue("help")) {
 			System.out.println(parser.getUsageString());
 			System.exit(0);
 			return;
 		}
 
 		Settings settings = new Settings();
 		Configuration globalConfig = Configuration.loadFromResource("/defaults.json");
 		settings.getVariableManager().addVariables(globalConfig.getVariables());
 
 		String settingsFilename = cmdline.getStringValue("settings");
 		File settingsFile;
 		if (settingsFilename != null) {
 			settingsFile = new File(settingsFilename);
 		} else {
 			settingsFile = new File(System.getProperty("user.home"), ".sqshyrc");
 		}
 
 		List<DriverMatch> driverInfos = new ArrayList<>(globalConfig.getDrivers());
 		List<DatabaseConnectionConfig> dcc = new ArrayList<>();
 
 		Configuration config = null;
 		if (settingsFile.isFile()) {
 			String filename = settingsFile.getAbsolutePath();
 			System.err.println("Loading settings file from " + filename);
 			config = Configuration.loadFromFile(settingsFile);
 			settings.getVariableManager().addVariables(config.getVariables());
 			if (config.getDrivers() != null) {
 				driverInfos.addAll(config.getDrivers());
 			}
 			List<DatabaseConnectionConfig> connections = config.getConnections();
 			if (connections != null) {
 				dcc.addAll(connections);
 			}
 		} else {
 			System.err.println("Warning: No settings file found in " + settingsFile.getAbsolutePath());
 		}
 
 		String driverClass = cmdline.getStringValue("driver");
 		String url = cmdline.getStringValue("url");
 		String username = cmdline.getStringValue("username");
 		String password = cmdline.getStringValue("password");
 		if (password != null && password.equals("@")) {
 			System.out.print("Password: ");
 			password = new String(System.console().readPassword());
 		}
 		List<String> properties = cmdline.getListValues("properties");
 		Map<String, String> connectionProperties = listToMap(properties);
 		String alias = cmdline.getStringValue("connect");
 
 		settings.getVariableManager().addVariables(listToMap(cmdline.getListValues("set")));
 
 		Terminal terminal = TerminalFactory.create();
 		ConsoleReader reader = new ConsoleReader("sqshy", System.in, System.out, terminal);
 		ConsoleLogger logger = new ConsoleLogger(settings, reader);
 		ConnectionManager connectionManager = new ConnectionManager(settings, driverInfos, dcc);
 		settings.init(logger, connectionManager);
 		Commands commands = new Commands(settings);
 		commands.addCommand("\\connect", ConnectCommand.class);
 		commands.addCommand("\\disconnect", DisconnectCommand.class);
 		commands.addCommand("\\reconnect", ReconnectCommand.class);
 		commands.addCommand("\\set", SetCommand.class);
 		commands.addCommand("\\echo", EchoCommand.class);
		if (url != null || alias != null) {
 			connectionManager.connect(alias, driverClass, url, username, password, connectionProperties);
 		}
 		SqshyRepl repl = new SqshyRepl(reader, settings, commands);
 		repl.repl();
 	}
 
 	private static Map<String, String> listToMap(List<String> properties) {
 		if (properties == null || properties.isEmpty()) {
 			return null;
 		}
 		Map<String, String> map = new HashMap<>();
 		for (String s : properties) {
 			String[] parts = s.split("=", 2);
 			if (parts.length == 2) {
 				map.put(parts[0], parts[1]);
 			} else {
 				map.put(s, "true");
 			}
 		}
 		return map;
 	}
 
 }
