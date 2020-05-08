 package shipper;
 
 import java.io.IOException;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.net.SocketAppender;
 
 /**
  * Monitors a file path for changes and forwards changed lines via log4j
  * {@link SocketAppender}.
  */
 public class Main {
 	/**
 	 * Command line arguments.
 	 */
 	private enum arg {
 		/**
 		 * Recipient name.
 		 */
 		HOST("Target hostname"),
 		/**
 		 * Recipient port.
 		 */
 		PORT("Target port", "4560"),
 		/**
 		 * File path.
 		 */
 		FILE("Path to monitored file"),
 		/**
 		 * Whether to sent all lines or just added lines.
 		 */
 		SKIP("Skip over existing data", "true");
 
 		/**
 		 * Hint, displayed in usage message.
 		 */
 		private String hint;
 
 		/**
 		 * Default value for parameter.
 		 */
 		private String defaultValue;
 
 		arg(String hint) {
 			this(hint, null);
 		}
 
 		arg(String hint, String defaultValue) {
 			this.hint = hint;
 			this.defaultValue = defaultValue;
 		}
 
 		/**
 		 * @return Command line option in long-option style.
 		 */
 		private String getCommandLineName() {
 			return "--" + this.name().toLowerCase();
 		}
 	}
 
 	/**
 	 * Categories of messages to user.
 	 */
 	private enum MessageCategory {
 		SENDING, NO_SUCH_FILE, FILE_ROTATED
 	}
 
 	static Logger logger = Logger.getLogger(Main.class);
 
 	/**
 	 * Command line arguments (unparsed).
 	 */
 	private static List<String> arguments;
 
 	public static void main(String[] args) throws IOException {
 		arguments = Arrays.asList(args);
 
 		Logger root = Logger.getRootLogger();
 		// Configure target or log messages according to command line.
 		SocketAppender socketAppender = new SocketAppender(get(arg.HOST),
 				Integer.valueOf(get(arg.PORT)));
 		root.addAppender(socketAppender);
 
 		// Monitor a single file.
 		new FileMonitor(Paths.get(get(arg.FILE)),
 				new FileModificationListener() {
 					/**
 					 * If {@code true} encountered messages are ignored.
 					 */
 					boolean skip = Boolean.valueOf(get(arg.SKIP));
 
 					/**
 					 * Category of last emitted message.
 					 */
 					private MessageCategory lastCategory = MessageCategory.SENDING;
 
 					@Override
 					public void noSuchFile(Path path) {
 						println(MessageCategory.NO_SUCH_FILE,
 								"File at "
 										+ path.toAbsolutePath()
 										+ " is not existent. Path will be monitored for newly added files.");
 					}
 
 					@Override
 					public void lineAdded(Path path, String lineContent) {
 						if (!skip) {
 							println(MessageCategory.SENDING,
 									"Sending lines of " + path.toAbsolutePath()
 											+ " (after non-normal state).");
 
 							// Send encountered message to target host.
 							logger.info(lineContent);
 						}
 					}
 
 					@Override
 					public void fileRotated(Path path) {
 						println(MessageCategory.FILE_ROTATED,
 								"File at "
 										+ path.toAbsolutePath()
 										+ " was rotated. Will send all lines of new file.");
 					}
 
 					@Override
 					public void completelyRead(Path file) {
 						// File end was reached, disable skipping to begin
 						// sending newly added messages.
 						skip = false;
 					}
 
 					/**
 					 * Shows message to user if category changes.
 					 * 
 					 * @param newCategory
 					 *            Message category.
 					 * @param message
 					 *            Text to display.
 					 */
 					private void println(MessageCategory newCategory,
 							String message) {
 						if (!lastCategory.equals(newCategory)) {
 							System.out.println(message);
 							lastCategory = newCategory;
 						}
 					}
 				});
 	}
 
 	/**
 	 * Parses command line. Prints usage info and exits if argument without
 	 * default was not specified.
 	 * 
 	 * @param argument
 	 *            Command line argument.
 	 * @return Current value or default value.
 	 */
 	private static String get(arg argument) {
 		int nameIndex = arguments.indexOf(argument.getCommandLineName());
 		int valueIndex = nameIndex + 1;
 		if (nameIndex == -1 || valueIndex >= arguments.size()) {
 			// Fall back to default value if argument not on command line and
 			// default value exists.
 			if (argument.defaultValue != null && nameIndex == -1) {
 				return argument.defaultValue;
 			} else {
 				printUsageAndExit(argument);
 				return null;
 			}
 		} else {
 			// Take value from command line.
 			return arguments.get(valueIndex);
 		}
 	}
 
 	/**
 	 * Prints usage instruction.
 	 * 
 	 * @param argument
 	 *            The argument that could not be read whilst trying to parse the
 	 *            command line.
 	 */
 	private static void printUsageAndExit(arg argument) {
 		System.err.println("Missing value for " + argument.getCommandLineName()
 				+ ": " + argument.hint);
 		System.err.print("Usage: java -jar log4j-shipper.jar");
 		for (arg option : arg.values()) {
 			String defaultValue;
 			if (option.defaultValue != null) {
 				defaultValue = option.defaultValue;
 			} else {
 				defaultValue = "…";
 			}
			System.err.print(" " + argument.getCommandLineName() + " "
 					+ defaultValue);
 
 		}
 		System.err.println();
 
 		// Abort as there is no sensible way to continue with incomplete setup.
 		System.exit(1);
 	}
 
 }
