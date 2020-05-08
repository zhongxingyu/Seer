 package sorbet;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import log.PrintLogger;
 
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
			commander.setProgramName("Main");
 			
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
 		
		sorbet.setLogger(new PrintLogger());
 		
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
