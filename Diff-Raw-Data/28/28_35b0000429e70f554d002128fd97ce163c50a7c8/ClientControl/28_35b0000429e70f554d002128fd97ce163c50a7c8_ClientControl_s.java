 package client;
 
 import java.io.DataOutputStream;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 
 public class ClientControl {
 		
 	public static final String IP_ADDRESS = "localhost";
 	public static final int PORT = 65534;
 	public static final String ADDRESS = "http://" + IP_ADDRESS + ":" + PORT;
 	
 	protected String[] paths;
 	
 	// TESTING //
 	
 	public static int index = 0;
 	public static String testData = "lolol lolol";
 	
 	// END //
 	
 	public ClientControl() {
 		String retrievedPaths = connect("/help", GsonConverter.objectArrayToGson(new Object[] {"Paths please!"}));
 
 		if (retrievedPaths != "") {
 			try {
 				Object[] serverData = GsonConverter.gsonToObjectArray(retrievedPaths);
 				ArrayList<String> tempPaths = (ArrayList<String>) Packager.getReturnValue(serverData);
 				paths = tempPaths.toArray(new String[tempPaths.size()]);
 			} catch(Exception e) {
 				paths = new String[0];
 				
 				System.out.println("Request from Server for paths FAILED.");
 				e.printStackTrace();
 			}
 		}
 		
 		// DEBUGGING //
 		
 		connect("/ping", GsonConverter.objectArrayToGson(new Object[] {System.currentTimeMillis()}));
 		
 		boolean gn = false;
 		int g = -1;
 		while(!gn) {
			String guess = connect("/guess", GsonConverter.objectArrayToGson(new Object[] {++g}));
			
			Object[] packRecieve = GsonConverter.gsonToObjectArray(guess);
			gn = (Boolean) Packager.getReturnValue(packRecieve);
 		}
 		
 		System.out.println("Guessed Number! " + g);
 		
 		// END //
 	}
 	
 	public static String connect(String path, Object[] outputData) {
 		return connect(path, GsonConverter.objectArrayToGson(outputData));
 	}
 	
	public static String connect(String path, String outputData) {
		
 		System.out.println("Client attempting a connection to : " + ADDRESS + path); // Debugging
 		
 		HttpURLConnection connection;
 		URL link;
 		InputStream input;
 		String data = "";
 		DataOutputStream output;
 		
 		try {
 		    link = new URL(ADDRESS + path);
 		    connection = (HttpURLConnection) link.openConnection();
 		    connection.setAllowUserInteraction(true);
 		    connection.setRequestMethod("POST");
 		    connection.setDoOutput(true);
 		    connection.setDoInput(true);
 		    connection.setInstanceFollowRedirects(false);
 
 		    output = new DataOutputStream(connection.getOutputStream());
 		    output.writeBytes(outputData);
 		    output.flush();
 		    output.close();
 
 		    try {  //Try to read the data from server
 				input = connection.getInputStream();
 				while (input.available() > 0) {
 					int number = input.read();
 					data += (char) number;
 				}
 			    input.close();
 			} catch (Exception e) {
				data = "";
 			}
 		    
 		    connection.disconnect();
 		    
 		    System.out.println("Client succesfully connected to   : " + ADDRESS + path); // Debugging
 		    
 		} catch (Exception e) { e.printStackTrace(); }
 		
 		// DEBUGGING //
 		
 		System.out.println("Data To Server: " + outputData);
 		System.out.println("Data From Server: " + data);
 		System.out.println("---------------------------------------");
 		
 		// END //
 		
 		return data;
 	}
 	
 	public static void main(String[] args) {
 		new ClientControl();
 	}
 }
