 package edu.uw.cs.cse461.sp12.OSConsoleApps;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.json.JSONObject;
 
 import edu.uw.cs.cse461.sp12.OS.RPCCallerSocket;
 import edu.uw.cs.cse461.sp12.util.Log;
 
 public class Ping implements OSConsoleApp {
 
 	private static final String TAG="PingConsole";
 	
 	@Override
 	public String appname() {
 		return "ping";
 	}
 
 	@Override
 	public void run() throws Exception {
 		// TODO Auto-generated method stub
 		try {
 			// Eclipse doesn't support System.console()
 			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
 			System.out.println("Enter lines like <target> <msg> to have <msg> echoed back");
 			while ( true ) {
 				try {
 					//System.out.print("Enter a host ip, or exit to exit: ");
 					String targetIP = "cse461.cs.washington.edu";//console.readLine();
 					if ( targetIP == null ) targetIP = "";
 					else if ( targetIP.equals("exit")) break;
 
 					//System.out.print("Enter the RPC port, or empty line to exit: ");
 					String targetPort = "46120";//console.readLine();
 					if ( targetPort == null || targetPort.isEmpty() ) continue;
 
 					
 					RPCCallerSocket socket = new RPCCallerSocket(targetIP, targetIP, targetPort);
 					long time = System.currentTimeMillis();
 					long newTime = time;
 					long overall = 0;
 					int runs = 5;
 					
 					for(int i = 0; i < runs; i++){
 						time = newTime;
 						socket.invoke("echo", "echo", new JSONObject().put("msg", "") );
 						newTime = System.currentTimeMillis();
 						long diff = newTime - time;
 						overall += diff;
 						System.out.println("Run #" + i + " (msec): " + diff);
 						//socket.close();
 						//socket = new RPCCallerSocket(targetIP, targetIP, targetPort);
 					}
 					System.out.println("Average (msec): " + ((double)overall) / runs);
 					break;
 				} catch (Exception e) {
 					System.out.println("Exception: " + e.getMessage());
 				}
 			}
 		} catch (Exception e) {
 			Log.e(TAG, TAG + ".run() caught exception: " + e.getMessage());
 		}
 	}
 
 	@Override
 	public void shutdown() throws Exception {
 	}
 	
 }
