 import java.io.*;
 import java.text.*;
 import java.net.*;
 import java.util.*;
 import java.nio.file.*;
 import javax.activation.*;
 
 public class HttpServer {
 	// Defualt Port set to 8080	
   private static int port = 8080;
 	public static void main(String args[]) throws Exception{
 		/* Reads in the Args from the project call
 		*  Will read in all args, does not matter the order as
 		*  long as each call is preceded by the proper - identifier
 		* Will set the log file, doc root, and port properly
 		* if no port, port is preset to 8080.
 		*/ 
 		String docroot = System.getProperty("user.dir"); //set default directory
 		String logfile = "";
 		for(int x = 0; x < args.length; x++){
 			switch(args[x]){
 				case"-p":{
 					x++;
 					port = Integer.parseInt(args[x]);
 					break;
 				}case"-docroot":{
 					x++;
 					docroot = args[x];
 					break;
 				}case"-logfile":{
 					x++;
 					logfile = args[x];
 					break;
 				}default:{
 					System.out.println("Error reading args");
 				}
 			}
 		}
 		ServerSocket listener = new ServerSocket(port);
 		System.out.println("HTTP server is running on port " + port + ".");
 		
 		while(true){
 			Socket s = listener.accept();
 			Clienthandler c = new Clienthandler(s, docroot, logfile);
 			
 			Thread t = new Thread(c);
 			t.start();
 		}
 	}
 }
 
 class Clienthandler implements Runnable{
 	Socket connection;
 	int length;
 	String line;
 	String mimeType;
 	String date;
 	String lastMod;
 	String directory;
	File file;
	FileWriter filewrite;
	BufferedWriter out;
 	StringTokenizer st;
 	byte[] fileBytes;
 	BufferedReader clientRequest;
 	DataOutputStream clientReply;
 	
 	static SimpleDateFormat dateFormat = new SimpleDateFormat(
 			"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
 	
 	boolean valid = false;
 	
 	Clienthandler(Socket s, String docroot, String logfile){
 		connection = s;
		file = new File(logfile);
 		try {
			filewrite = new FileWriter(logfile);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
		out = new BufferedWriter(filewrite);
 		directory = docroot;
 		
		
 		try{
 			connection.setKeepAlive(false);
 		}catch(Exception e){
 			System.out.println("Error at socket creation");
 		}
 	}
 	
 	public void run(){
 		try{
 			boolean open = true;
 			clientRequest = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 			clientReply = new DataOutputStream(connection.getOutputStream());
 			line = clientRequest.readLine();
 			System.out.println(line); /////////////////
 			valid = line.startsWith("GET");
 			System.out.println(valid); ////////////////
 				
 			if(!valid) sendNotImplemented();
 			
 			if(!parseRequest()) sendNotFound();
 			
 			URLConnection url = new URLConnection("");//not working yet
 			mimeType = URLConnection.getContentType();
 			System.out.println("Type: " + mimeType);////////////
 			length = URLConnection.getContentLength();
 			System.out.println("Length: " + length);/////////
 			fileBytes = Files.readAllBytes(file.toPath());
 			lastMod = getLastModified();
 			System.out.println(lastMod); ///////////////
 			date = getServerTime();
 			System.out.println(date);/////////////////
 			System.out.println(mimeType + "  " + lastMod + " " + date);
 			sendValidResponse();
 			
 			connection.close();
 		}catch(Exception e){
 			System.out.println("Error running.");
 		}
 	}
 	
 	private synchronized void sendNotImplemented(){
 		try{
 			clientReply.writeBytes("501 Not Implemented");
 			clientReply.flush();
 			connection.close();
 		}catch(Exception e){
 			System.out.println("Error sending 501.");
 		}
 	}
 	
 	private synchronized void sendNotFound(){
 		try{
 			clientReply.writeBytes("HTTP/1.1 404 Not Found\r\n");
 			clientReply.writeBytes("Date: " + date + "\r\n");
 			clientReply.writeBytes("Connection: close\r\n");
 			clientReply.writeBytes("\r\n");
 			clientReply.writeBytes("404 File Not Found\n\n");
 			clientReply.writeBytes("The server was unable to find the file requested.");
 			clientReply.flush();
 			connection.close();
 		}catch(Exception e){
 			System.out.println("Error sending 404.");
 		}
 	}
 	
 	private synchronized void sendValidResponse(){
 		try{
 			clientReply.writeBytes("HTTP/1.1 200 OK\r\n");
 			writeToLog("HTTP/1.1 200 OK\r\n");
 			clientReply.writeBytes("Content-Type: " + mimeType + "\r\n");
 			writeToLog("Content-Type: " + mimeType + "\r\n");
 			clientReply.writeBytes("Last-Modified: " + lastMod + "\r\n");
 			writeToLog("Last-Modified: " + lastMod + "\r\n");
 			clientReply.writeBytes("Date: " + date + "\r\n");
 			writeToLog("Date: " + date + "\r\n");
 			clientReply.writeBytes("Length: " + length + "\r\n");
 			writeToLog("Length: " + length + "\r\n");
 			if(connection.getKeepAlive()){
 				clientReply.writeBytes("Connection: keep-alive\r\n");
 			}else{
 				clientReply.writeBytes("Connection: close\r\n");
 			}
 			clientReply.writeBytes("\r\n");
 			
 			for(int i = 0; i < fileBytes.length; i++){
 				clientReply.write(fileBytes[i]);
 			}
 			clientReply.flush();
 			connection.close();
 		}catch(Exception e){
 			System.out.println("Error sending 200.");
 		}
 	}
 	
 	private boolean parseRequest(){
 		String temp;
 		try{
 			line = line.substring(3);
 			System.out.println(line); /////////
 			st = new StringTokenizer(line);
 			temp = st.nextToken();
 			file = new File(temp.substring(1));
 			System.out.println(file.toString()); /////////
 			st.nextToken();
 			clientRequest.readLine();
 			line = clientRequest.readLine();
 			System.out.println(line); /////////
 			st = new StringTokenizer(line);
 			st.nextToken();
 			if(st.nextToken().equals("keep-alive")){
 				connection.setSoTimeout(20000);
 			}
 			writeToLog("Request: " + line);
 		}catch(Exception e){
 			System.out.println("Error parsing request.");
 		}
 		
 		return file.exists();
 	}
 	private synchronized void writeToLog(String s){		
		try {
			out.write(s);
		} catch (IOException e) {
			e.printStackTrace();
		} 
 		if(s.equals("Close Log")){
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
 		}
 	}
 	private String getLastModified(){
 		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
 		return dateFormat.format(file.lastModified());
 	}
 	
 	
 	private String getServerTime() {
 		Calendar calendar = Calendar.getInstance();
 		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
 		return dateFormat.format(calendar.getTime());
 	}
 }
