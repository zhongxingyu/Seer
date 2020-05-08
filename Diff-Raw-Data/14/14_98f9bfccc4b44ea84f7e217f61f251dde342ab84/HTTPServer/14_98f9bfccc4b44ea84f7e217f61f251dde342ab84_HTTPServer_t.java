 package week1;
 
 import java.net.*;
 import java.io.*;
 import java.util.*;
 import week2.*;
 /**
  *
  * @author Martin
  */
 public class HTTPServer {
 	int port;
 	String wwwhome;
 	Map<String,FileProcessingStrategy> fileStrategies;
 	Map<String,ObjectProcessingStrategy> objectStrategies;
 	
 	
 	Socket con;
 	BufferedReader in;
 	OutputStream out;
 	PrintStream pout;
 	
 	HTTPServer(int p, String www) {
 		port = p;
 		wwwhome = www;
 		fileStrategies = new HashMap<String,FileProcessingStrategy>();
 		fileStrategies.put("application/x-shar", new ShellFileProcessingStrategy());
 		GeneralFileProcessingStrategy general = new GeneralFileProcessingStrategy();
 		fileStrategies.put("default", general);
 		
		objectStrategies = new HashMap<String,ObjectProcessingStrategy>();
 		objectStrategies.put("default", new BankObjectProcessingStrrategy());
 	}
 	
 	public static void main(String[] args) {
 		if (args.length!=2) {
 			System.out.println("Usage: java HTTPServer <port> <wwwhome>");
 			System.exit(-1);
 		}
 		int port = Integer.parseInt(args[0]);
 		String wwwhome = args[1];
 		HTTPServer fs = new HTTPServer(port, wwwhome);
 		fs.run();
 	}
 	
 	private void run() {
 		ServerSocket ss = null;
 		try {
 			ss = new ServerSocket(port);
 		} catch (IOException e) {
 			System.err.println("Could not start server: "+e);
 			System.exit(-1);
 		}
 		System.out.println("AwesomeServer accepting connections on port "+ port);
 		
 		while (true) {
 			try {
 				con = ss.accept();
 				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
 				out = new BufferedOutputStream(con.getOutputStream());
 				pout = new PrintStream(out);
 				
 				String request = in.readLine();
 				con.shutdownInput(); //ignore the rest
 				log(con, request);
 				
 				processRequest(request);
 				pout.flush();
 			} catch (IOException e) {
 				System.err.println(e);
 			} catch (Exception e){
 				e.printStackTrace();
 				errorReport(pout, con, "500", "Internal Server Error",
 						"An error occured.");
 			}
 			
 			try {
 				if (con!= null) {
 					con.close();
 				}
 			} catch (IOException e) {
 				System.err.println(e);
 				}
 			}
 		}
 	
 	private void doGet(String request) {
 		String[] parts = request.split(" ");
 		String[] path = parts[1].split("\\?");
 		Map<String,String> map;
 		if(path.length > 1)
 			map = makeMap(path[1]);
 		else
 			map = new HashMap<String,String>();
 		
 		try {
 			File f = getFile(path[0]);
 			String inputtype = URLConnection.guessContentTypeFromName(f.getPath());
 			FileProcessingStrategy strategy;
 			if(fileStrategies.containsKey(inputtype))
 				strategy = fileStrategies.get(inputtype);
 			else{
 				strategy = fileStrategies.get("default");
 			}
 			String contenttype = strategy.getContentType(f);
 			
 			pout.print("HTTP/1.0 200 OK\r\n");
 			if (contenttype!=null) 
 				pout.print("Content-Type: "+contenttype+"\r\n");
 			pout.print("Date: "+new Date() + "\r\n"+
 					"Server: dDist HTTPServer 1.0\r\n\r\n");
 			strategy.processFile(f, map, out);
 			log(con, "200 OK");
 		}
 		catch(FileNotFoundException e){
 			ObjectProcessingStrategy strategy = objectStrategies.get("default");
 			strategy.process(path, map, out);
 		}
 	}
 	
 	private void doPost(String request) {
 		
 	}
 	
 	private Map<String,String> makeMap(String vars){
 		String[] parts = vars.split("&");
 		Map<String,String> map = new HashMap<String,String>();
 		for(String part : parts){
 			String[] keyValue = part.split("=");
 			if(keyValue.length > 1)
 				map.put(keyValue[0], URLDecoder.decode(keyValue[1]));
 			else
 				map.put(keyValue[0], "");
 		}
 		return map;
 	}
 	
 	private File getFile(String path) throws FileNotFoundException {
 		path = wwwhome+path;
 		File f = new File(path);
 		
 		// Check for directory and get index file
 		if(f.isDirectory()){
 			if(!path.endsWith("/"))
 				path = path + "/";
 			path = path + "index.html";
 			f = new File(path);
 		}
 		
 		if(f.exists())
 			return f;
 		
 		throw new FileNotFoundException();
 	}
 	
 	private void processRequest(String request) 
 			throws IOException {
 		
 		// Check for uri starting with /
 		String[] parts = request.split(" ");
 		if(!parts[1].startsWith("/")){
 			errorReport(pout, con, "400", "Bad Request", 
 					"Your Browser sent a request that this server could not understand.");
 		}
 		
 		// Check for unimplemented methods
 		if(!request.startsWith("GET"))
 			errorReport(pout, con, "501", "Not Implemented",
 					"Request method was not implemented.");
 		
 		if(parts[1].contains("../") || parts[1].startsWith("~"))
 			errorReport(pout, con, "403", "Forbidden",
 					"Forbidden resource requested.");
 		
 		if(request.startsWith("POST"))
 			doPost(request);
 		
 		if(request.startsWith("GET"))
 			doGet(request);
 	}
 	
 	private void log (Socket con, String msg) {
 		System.err.println(new Date()+" ["+
 				con.getInetAddress().getHostAddress()+
 				":"+con.getPort()+"] "+ msg);
 	}
 	
 	private void errorReport(PrintStream pout, Socket con, String code, String title, String msg) {
 		pout.print("HTTP/1.0 "+code+" "+title+"\r\n"+
 				"\r\n"+
 				"<!DOCTYPE html>\r\n"+
 				"<head>\r\n<title>"+code+" "+title+"</title>\r\n"+
 				"</head>\r\n<body>\r\n"+
 				"<h1>"+title+"</h1>\r\n<p>"+msg+"</p>\r\n"+
 				"<hr>\r\n<address>dDist HTTPServer 1.0 at "+
 				con.getLocalAddress().getHostName()+
 				" Port"+con.getLocalPort()+"</address>\r\n"+
 				"</body>\r\n</html>\r\n");
 		pout.flush();
 		log(con, code+" "+title);
 	}
 }
