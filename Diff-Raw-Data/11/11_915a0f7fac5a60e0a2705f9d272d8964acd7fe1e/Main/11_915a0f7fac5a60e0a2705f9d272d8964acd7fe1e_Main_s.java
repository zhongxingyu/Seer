 package sorbet;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Map;
 
 import com.sun.jdi.Bootstrap;
 import com.sun.jdi.VirtualMachine;
 import com.sun.jdi.VirtualMachineManager;
 import com.sun.jdi.connect.Connector;
 import com.sun.jdi.connect.IllegalConnectorArgumentsException;
 import com.sun.jdi.connect.LaunchingConnector;
 
 public class Main {	
 	public static void main(String args[]) {		
 		VirtualMachineManager manager = Bootstrap.virtualMachineManager();
 		
 		LaunchingConnector connector = manager.defaultConnector();
 		
 		Map<String, Connector.Argument> arguments = connector.defaultArguments();
 		
 		Connector.Argument mainArgument = arguments.get("main");
 		mainArgument.setValue("client.Main");
 		
 		try {
 			VirtualMachine vm = connector.launch(arguments);
 			
 			vm.resume();
 			
 			InputStream errorStream = vm.process().getErrorStream();
 			InputStream outputStream = vm.process().getInputStream();
			
 			while (true) {
 				int error = errorStream.read();
 				if (error != -1) {
 					System.out.print((char)error);
 				}
 				
 				int output = outputStream.read();
 				if (output != -1) {
 					System.out.print((char)output);
				}
 			}
 		}
 		catch(IllegalConnectorArgumentsException e)	{
 			for (String argName : e.argumentNames()) {
 				System.out.println(argName);
 			}
 		}
 		catch(Exception e) {
 			System.out.println(e.toString());
 		}
 	}	
 }
