 package pleocmd.itfc.cli;
 
 import java.io.File;
 
 import pleocmd.Log;
 import pleocmd.Log.Type;
 import pleocmd.cfg.Configuration;
 import pleocmd.exc.ConfigurationException;
 import pleocmd.exc.PipeException;
 import pleocmd.pipe.Pipe;
 
 /**
  * @author oliver
  */
 public final class CommandLine {
 
 	private static CommandLine commandLine;
 
 	private final Pipe pipe;
 
 	private CommandLine() {
 		commandLine = this;
 		pipe = new Pipe(Configuration.getMain());
 	}
 
 	public static CommandLine the() {
 		if (commandLine == null) new CommandLine();
 		return commandLine;
 	}
 
 	public void parse(final String[] args) {
 		try {
 			Log.setMinLogType(Type.Info);
 			for (final String arg : args) {
 				final File file = new File(arg);
 				if (file.isFile())
 					if (file.getName().endsWith(".pca"))
 						executePipe(file);
 					else
 						throw new RuntimeException(String.format(
 								"Cannot recognize filetype of '%s'", file));
 				else
 					parseArgument(arg);
 			}
 		} catch (final Throwable throwable) { // CS_IGNORE
 			// we need to print the Exception additionally to
 			// logging it because logging itself may have caused
 			// the exception or it just is not yet initialized
 			throwable.printStackTrace(); // CS_IGNORE
 			Log.error(throwable, "Failed to parse command line arguments");
 		}
 	}
 
 	private void parseArgument(final String arg) {
 		if ("-d".equals(arg) || "--detailed".equals(arg))
 			Log.setMinLogType(Type.Detail);
 		if (arg.startsWith("--configure=")) {
 			// final String ppName = arg.substring(12);
 			// final PipePart pp = Pipe.the().find(ppName);
 			// TODO ENH CommandLine
 		} else
 			throw new RuntimeException(String.format(
 					"Cannot recognize argument '%s'", arg));
 	}
 
 	public void executePipe(final File pcaFile) {
 		try {
 			Configuration.getMain().readFromFile(pcaFile);
 			pipe.configure();
 			pipe.pipeAllData();
 		} catch (final ConfigurationException e) {
 			Log.error(e);
 		} catch (final PipeException e) {
 			Log.error(e);
 		} catch (final InterruptedException e) {
 			Log.error(e);
 		}
 	}
 
 }
