 package edu.uw.cs.cse461.sp12.OSConsoleApps;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import org.json.JSONObject;
 
 import edu.uw.cs.cse461.sp12.OS.RPCCallerSocket;
 import edu.uw.cs.cse461.sp12.util.Log;
 
 public class Echo implements OSConsoleApp {
 	// I'm using the android-like Log.x() functions for debugging output, even for console apps
 	private static final String TAG="EchoConsole";
 	
 	// OSConsoleApp's must have a constructor taking no arguments
 	public Echo() {
 	}
 	
 	public String appname() {
 		return "echo";
 	}
 	
 	public void run() {
 		try {
 			// Eclipse doesn't support System.console()
 			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
 			System.out.println("Enter lines like <target> <msg> to have <msg> echoed back");
 			while ( true ) {
 				try {
 					System.out.print("Enter a host ip, or exit to exit: ");
 					String targetIP = console.readLine();
 					if ( targetIP == null ) targetIP = "";
 					else if ( targetIP.equals("exit")) break;
 
 					System.out.print("Enter the RPC port, or empty line to exit: ");
 					String targetPort = console.readLine();
 					if ( targetPort == null || targetPort.isEmpty() ) continue;
 
 					System.out.print("Enter message to be echoed: ");
 					String msg = console.readLine();
					//System.out.println(targetIP + " " + targetPort + " " + msg);
 					RPCCallerSocket socket = new RPCCallerSocket(targetIP, targetIP, targetPort);
 					JSONObject response = socket.invoke("echo", "echo", new JSONObject().put("msg", msg) );
 					
 					System.out.println(response.getString("msg"));
 					
 				} catch (Exception e) {
 					System.out.println("Exception: " + e.getMessage());
 				}
 			}
 		} catch (Exception e) {
 			Log.e(TAG, "EchoConsole.run() caught exception: " +e.getMessage());
 		}
 	}
 	
 	public void shutdown() {
 	}
 	
 }
