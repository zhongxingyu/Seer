 package edu.uw.cs.cse461.sp12.OSConsoleApps;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import org.json.JSONArray;
 
 
 import edu.uw.cs.cse461.sp12.OS.OS;
 import edu.uw.cs.cse461.sp12.OS.SnetController;
 import edu.uw.cs.cse461.sp12.util.Log;
 
 public class Snet implements OSConsoleApp {
 	private static String TAG = "Snet";
 	@Override
 	public String appname() {
 		return "snet";
 	}
 
 	@Override
 	public void run() throws Exception {
 		//fetchUpdates from root
 		//fetchUpdates from all online
 		//Find all photos I don't have
 		//all fetchUpdates again with neededPhotos
 		//fetchPhotos
 		//
 		
 		try {
 			// Eclipse doesn't support System.console()
 			SnetController snet = (SnetController) OS.getService("snet");
 			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
 			System.out.println("This is console Snet at your service!");
 			while ( true ) {
 				try {
 					System.out.print("Enter a command or exit to quit: ");
 					String command = console.readLine();
 					if	(command.toLowerCase().equals("exit")) break;
 					if (command.toLowerCase().equals("fetchupdates")){
 						System.out.print("Enter a name to fetchupdates from ");
 						command = console.readLine();
						snet.fetchUpdates(command, false);
						System.out.println(snet.toString());
 						continue;
 
 					}
 					//System.out.println(targetIP + " " + targetPort + " " + msg);
 					//RPCCallerSocket socket = new RPCCallerSocket(targetIP, targetIP, targetPort);
 					//JSONObject response = socket.invoke("echo", "echo", new JSONObject().put("msg", msg) );
 					//socket.close();
 					//System.out.println(response.getString("msg"));
 					
 				} catch (Exception e) {
 					System.out.println("Exception: " + e.getMessage());
 				}
 			}
 		} catch (Exception e) {
 			Log.e(TAG, "SnetConsole.run() caught exception: " +e.getMessage());
 		}
 	}
 
 	@Override
 	public void shutdown() throws Exception {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
