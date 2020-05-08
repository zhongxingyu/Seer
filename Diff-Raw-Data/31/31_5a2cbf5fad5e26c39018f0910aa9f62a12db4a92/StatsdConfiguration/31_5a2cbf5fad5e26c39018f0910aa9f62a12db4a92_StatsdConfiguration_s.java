 package com.github.r1j0.statsd.server;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.r1j0.statsd.backend.Backend;
 
 public class StatsdConfiguration {
 
 	private final Logger logger = LoggerFactory.getLogger(getClass());
 
 	private Options options;
 	private Properties properties = new Properties();
 	private List<Backend> backends = new ArrayList<Backend>();
 
 
 	public StatsdConfiguration(String[] args) {
 		options = OptionsBuilder.build();
 		CommandLineParser parser = new PosixParser();
 		CommandLine line = null;
 
 		try {
 			line = parser.parse(options, args);
 			parseCommandLineOptions(line);
 		} catch (org.apache.commons.cli.ParseException e) {
 			System.err.println(e.getMessage());
 			printHelp(options);
 			System.exit(1);
 		}
 	}
 
 
 	public String getValue(String key) {
 		return properties.getProperty(key);
 	}
 
 
 	public boolean isDebugEnabled() {
		return Boolean.parseBoolean(getValue("server.debug"));
 	}
 
 
 	public int getListeningPort() {
		return Integer.parseInt(getValue("server.listen_port"));
 	}
 
 
 	public int getFlushIntervall() {
		return Integer.parseInt(getValue("server.flush_intervall"));
 	}
 
 
 	public List<Backend> getBackends() {
 		return backends;
 	}
 
 
 	private void parseCommandLineOptions(final CommandLine line) {
 		if (line.hasOption("h")) {
 			printHelp(options);
 			System.exit(0);
 		}
 
 		if (line.hasOption("version")) {
 			logger.info("Version is and will be infinite.");
 			System.exit(0);
 		}
 
 		String propertiesFile = line.getOptionValue("c", "resources/statsd.properties");
 
 		try {
 			properties.load(new FileInputStream(propertiesFile));
 		} catch (FileNotFoundException e) {
 			logger.error("File not found: " + propertiesFile);
 			System.exit(1);
 		} catch (IOException e) {
 			logger.error("Unable to read file: " + propertiesFile);
 			System.exit(1);
 		}
 
 		if (line.hasOption("debug")) {
			properties.setProperty("server.debug", line.getOptionValue("debug"));
 		}
 
 		if (line.hasOption("p")) {
			properties.setProperty("server.listen_port", line.getOptionValue("p"));
 		}
 
		String[] backendsToUse = properties.getProperty("backend.use").split(",");
 
 		if (isDebugEnabled()) {
			backendsToUse = new String[] { "DebugBackend" };
 		}
 
 		for (String backendToUse : backendsToUse) {
 			try {
 				Backend backend = (Backend) Class.forName("com.github.r1j0.statsd.backend." + StringUtils.capitalize(backendToUse.trim().toLowerCase()) + "Backend").newInstance();
 				backends.add(backend);
 
 				logger.info("Added backend: " + backend.getClass().getSimpleName() + ".");
 			} catch (Exception e) {
 				e.printStackTrace();
 				logger.error(e.getMessage());
 			}
 		}
 	}
 
 
 	private void printHelp(Options options) {
 		HelpFormatter formatter = new HelpFormatter();
 		formatter.printHelp(StatsdServer.class.getSimpleName(), options, true);
 	}
 }
