 import java.net.*;
 import java.io.*;
 import java.util.*;
 class LogQueryServerThread extends Thread {
 	private Socket clientSock;
 	private String logFilePath;
 
 	LogQueryServerThread(Socket client_sock, String log_file_path) {
 		if (client_sock == null) {
 			System.out.println("client_sock can not be null.");
 			System.exit(-1);
 		}
 		this.clientSock = client_sock;
 		this.logFilePath = log_file_path;
 	}
 
 	private void exceptionHandler(Exception ex) {
 		System.out.println(ex.getMessage());
 	}	
 
 	private List< String > grep(List< String > args) {
 		Runtime runtime = Runtime.getRuntime();
 		Process process = null;
 		try {
 			LinkedList< String > cmd_array = new LinkedList< String >(args);
 			cmd_array.add(logFilePath);
 			cmd_array.add(0, "grep");
 			process = runtime.exec(cmd_array.toArray(new String[cmd_array.size()]));
			process.waitFor();
		} catch(Exception ex) {
			exceptionHandler(ex);
		}
		ArrayList< String > result_list = new ArrayList< String >();
		BufferedReader result =
			new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		try {
 			while((line = result.readLine()) != null) {
 				result_list.add(line);
 			}
 		} catch(IOException ex) {
 			exceptionHandler(ex);
 		}
 		return result_list;
 	}
 
 	public void run() {
 		try {	
 			ObjectOutputStream output = new ObjectOutputStream(clientSock.getOutputStream());
 			ObjectInputStream input = new ObjectInputStream(clientSock.getInputStream());
 			List< String > args = (List< String >) input.readObject();
 			System.out.println("Received request: " + args.toString());
 			output.writeObject(grep(args));
 			output.close();
 			input.close();
 			clientSock.close();
 		} catch(IOException ex) {
 			exceptionHandler(ex);
 		} catch(ClassNotFoundException ex) {
 			exceptionHandler(ex);
 		}
 	}
 }
