 /*
 ** Copyright 2013 Software Composition Group, University of Bern. All rights reserved.
 */
 package ch.unibe.scg.lexica;
 
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.List;
 import java.util.Objects;
 
 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
 import joptsimple.OptionSpec;
 
 public class Configuration {
 
 	private static final Configuration instance = new Configuration();
 	
 	public IOperationMode mode = null;
 	public String filePattern = null;
 	
 	private Configuration() {
 	}
 	
 	public static Configuration getInstance() {
 		return instance;
 	}
 	
 	/**
 	 * Parse the arguments in the form:
 	 * 
 	 * <scan|analyze> [path] [-f <file pattern>]
 	 * 
 	 * @param args the arguments
 	 * @throws IOException if an I/O error occurs
 	 */
 	public void parseArguments(String[] args) throws IOException {
 		Objects.requireNonNull(args);
 		
 		OptionParser parser = new OptionParser();
 		OptionSpec<String> filePatternArg = parser.accepts("f").withRequiredArg().defaultsTo("*");
 		
 		// Parse the arguments
 		OptionSet options = parser.parse(args);
 		
 		// Check the non-option arguments
 		List<String> nonOptionArgs = options.nonOptionArguments();
 		if (nonOptionArgs.size() < 1) {
 			throw new OptionException("Please specify an operation mode");
 		} else if (nonOptionArgs.size() > 2) {
 			throw new OptionException("Unknown option: " + nonOptionArgs.get(2));
 		}
 		
 		// Get the path
 		Path path = null;
 		if (nonOptionArgs.size() == 2) {
 			path = Paths.get(nonOptionArgs.get(1)).toRealPath();
 			if (!Files.exists(path)) {
 				throw new OptionException("Directory does not exist: " + path.toString());
 			} else if (!Files.isDirectory(path)) {
 				throw new OptionException("Path is not a directory: " + path.toString());
 			}
 		} else {
 			path = Paths.get(".").toRealPath();
 			assert Files.exists(path);
 			assert Files.isDirectory(path);
 		}
 		
 		// Get the operation mode
 		if (nonOptionArgs.get(0).equalsIgnoreCase("scan")) {
 			mode = new ScanMode(path);
 		} else if (nonOptionArgs.get(0).equalsIgnoreCase("analyze")) {
 			mode = new AnalyzeMode(path);
 		} else {
 			throw new OptionException("Unknown operation mode: " + nonOptionArgs.get(0));
 		}
 
 		// Get the file pattern
		filePattern = filePatternArg.value(options);
 	}
 
 }
