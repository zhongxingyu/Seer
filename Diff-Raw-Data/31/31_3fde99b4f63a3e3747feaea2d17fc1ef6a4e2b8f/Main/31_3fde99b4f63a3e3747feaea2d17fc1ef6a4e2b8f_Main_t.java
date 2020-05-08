 package sorbet;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
import log.Logger;
 import log.PrintLogger;
import log.SQLiteLogger;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.MissingCommandException;
 import com.beust.jcommander.ParameterException;
 import com.sun.jdi.Bootstrap;
 import com.sun.jdi.VirtualMachine;
 import com.sun.jdi.VirtualMachineManager;
 import com.sun.jdi.connect.Connector;
 import com.sun.jdi.connect.IllegalConnectorArgumentsException;
 import com.sun.jdi.connect.LaunchingConnector;
 import com.sun.jdi.connect.VMStartException;
 
 import sourceparser.SourceParser;
 
 public class Main {	
 	public static void main(String args[]) {
 		
 		SorbetArguments arguments = new SorbetArguments();
 		try {
 			JCommander commander = new JCommander(arguments, args);
			commander.setProgramName("Sorbet");
 			
 			if (arguments.help) {
 				commander.usage();
 				return;
 			}
 		} catch (MissingCommandException e) {
 			System.out.println(e.getMessage());
 			return;
 		} catch (ParameterException e) {
 			System.out.println(e.getMessage());
 			return;			
 		}
 		
 		Sorbet sorbet = new Sorbet();
 
 		sorbet.setSourceParser(createSourceParser(arguments.source));
 		
		sorbet.setLogger(createLogger(arguments.main, arguments.arguments, flattenList(arguments.whitelist), flattenList(arguments.blacklist)));
 		
 		sorbet.setVirtualMachine(createVirtualMachine(arguments.main, arguments.arguments));
 		
 		sorbet.run();
 	}
 	
 	public static SourceParser createSourceParser(List<String> sources) {		
 		StringBuilder sourcesString = new StringBuilder();
 		
 		for (String source : sources) {
 			if (sourcesString.length() != 0) {
 				sourcesString.append(File.separator);
 			}			
 			sourcesString.append(source);
 		}
 		
 		SourceParser sourceParser = new SourceParser();
 		sourceParser.addRootPaths(sourcesString.toString());
 		
 		return sourceParser;
 	}
 	
	private static String flattenList(List<String> list) {
		// TODO: Fix up the regex for this
		
		StringBuilder flat = new StringBuilder();
		
		for (String s : list) {
			if (flat.length() != 0) {
				flat.append(" AND ");
			}
			flat.append(s);
		}
		
		return flat.toString();
	}
	
	public static Logger createLogger(String main, String args, String whitelist, String blacklist) {
		Logger logger = new SQLiteLogger();
		logger.logProgramStart(main, args, whitelist, blacklist);
		return logger;
	}
	
 	public static VirtualMachine createVirtualMachine(String main, String args) {
 		
 		VirtualMachineManager manager = Bootstrap.virtualMachineManager();
 		
 		LaunchingConnector connector = manager.defaultConnector();
 		
 		Map<String, Connector.Argument> arguments = connector.defaultArguments();
 		 	
 		arguments.get("main").setValue(main);
 		if (args != null) {
 			arguments.get("options").setValue(args);
 		}
 		
 		try {
 			VirtualMachine vm = connector.launch(arguments);
 			
 			// Forward standard out and standard error
 			new StreamTunnel(vm.process().getInputStream(), System.out);
 			new StreamTunnel(vm.process().getErrorStream(), System.err);
 			
 			return vm;
 		} catch (IOException e) {
             throw new Error("Error: Could not launch target VM: " + e.getMessage());
         } catch (IllegalConnectorArgumentsException e) {
 			StringBuffer illegalArguments = new StringBuffer();
 			
 			for (String arg : e.argumentNames()) {
 				illegalArguments.append(arg);
 			}
 			
             throw new Error("Error: Could not launch target VM because of illegal arguments: " + illegalArguments.toString());
         } catch (VMStartException e) {
             throw new Error("Error: Could not launch target VM: " + e.getMessage());
         }
 	}
 }
