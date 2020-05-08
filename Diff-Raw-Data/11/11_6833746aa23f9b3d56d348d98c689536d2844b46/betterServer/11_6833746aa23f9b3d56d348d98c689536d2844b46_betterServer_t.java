 package net.geohhot.betterhttpserver;
 
 import java.io.*;
 import java.util.*;
 import java.net.*;
 import java.text.SimpleDateFormat;
 
 // config loading stuff
 import net.geohhot.jsonLoader.*;
 import org.json.*;
 
 /**
 * Runs simple yet HTTP server
 * @author geohhot
 * @version 0.1
 */
 
 class betterServer {
 	private static String notFoundResource = "notFound.html";
 	private static File root = new File(new File(".").getAbsolutePath());
 	private static String[] imageExtensions = {"ico", "png", "jpg"};
 	private static String[] textExtensions = {"html", "xml", "txt"};
 	private static String[] musicExtensions = {"mp3", "mp2"};
 
	private static String notFoundString = "<h1>Error 404: Not found</h1><p>The resource you were trying to get is not available</p>";

 	public static void main(String[] args) {	
 		System.out.println("Better HTTP server, by geohhot");
 		System.out.println("Loading config file: default.cong");
 		JSONLoader config = new JSONLoader ("default.conf");
 		try {
 			config.loadFile();
 		} catch (Exception e) {
 			System.err.println("Err. Exception was thrown: "+e);
 			System.exit (1);
 		}
 		JSONObject root = config.getRoot();
 		String hostname = root.getString("hostname");
 		int port        = root.getInt("port");
 		String rootDir  = root.getString("root");
 		System.out.println("Trying to start server...");
 		try {
 			ServerSocket serv = new ServerSocket(port, 10, InetAddress.getByName(hostname));
 			System.out.println("Server started! Running on "+serv.getInetAddress()+":"+serv.getLocalPort());
 
 			while (true) {
 				Socket inc = serv.accept();
 				InputStream inStream = inc.getInputStream();
 				OutputStream outStream = inc.getOutputStream();
 
 				Scanner in = new Scanner (inStream);
 				PrintWriter out = new PrintWriter (outStream, true);
 
 				String firstLine = "";
 				if (in.hasNextLine()) {
 					firstLine = in.nextLine();
 				} else {
 					inc.close();
 					continue;
 				}
 
 				String params[] = firstLine.split(" ");
 				String method = params[0];
 				String resource = params[1];
 				String getParams = "";
 				try {
 					resource = params[1].substring(0, params[1].indexOf("?"));
 					getParams = params[1].substring(params[1].indexOf("?")+1);
 				} catch (StringIndexOutOfBoundsException e) {
 
 				}
 				String protocol = params[2];
 
 				/// DEBUG
 				System.out.println(method + " " + resource + " " + protocol);
 
 				if ( method.equals("GET") ) {
 					// GET requests
 					if (resource.substring(resource.length() - 1).equals("/")) {
 						resource += "index.html";
 					}
 					String replyResourcePath = rootDir + resource;
 					File replyResource = new File (replyResourcePath);
 
 					/// DEBUG
 					//System.out.println("Requested resource: "+replyResourcePath);
 
 					if (!replyResource.exists()) {
 						// file does not exist
 						// return 404 error
 						out.println(protocol+" 404 Not Found");
 						replyResourcePath = rootDir + "/" + notFoundResource;
						if (! new File(replyResourcePath).exists()) {
							// no not found file.
							out.println();
							out.print(notFoundString+"\n");
							out.println();
							inc.close();
							continue;
						}
 						replyResource = new File (replyResourcePath);
 					} 
 					else {
 						// reply code - 200 OK
 						out.println(protocol+" 200 OK");
 					}
 
 					String extension = replyResourcePath.substring (replyResourcePath.lastIndexOf("."));
 
 					// returning Content-Length
 					long length = replyResource.length();
 					out.println("Content-Length: "+length);
 					// returning name of server
 					out.println("Server: Better server (by geohhot)");
 					
 					// returning last modified date of resource
 					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
 					out.println("Last-Modified: "+sdf.format(replyResource.lastModified()));
 
 					if (Arrays.asList(textExtensions).contains(extension)) {
 						// requested TEXT file
 						// return Content Type
 						out.println("Content-Type: text/"+extension);						
 					}
 					else if (Arrays.asList(imageExtensions).contains(extension)) {
 						// requested IMAGE file
 						out.println("Content-Type: image/"+extension);
 					}
 					else if (Arrays.asList(musicExtensions).contains(extension)) {
 						// return Content Type for music
 						out.println("Content-Type: application/force-download");
 					}
 
 					out.println();
 					// starting printing resource byte by byte to output stream
 					boolean brokenPipe = false;
 					InputStream resourceInputStream = new FileInputStream(replyResource);
 					for (long i = 0; i<length; i++) {
 						byte t;
 						t = (byte)resourceInputStream.read();
 						try {
 							outStream.write(t);
 						} catch (SocketException se) {
 							System.err.println("Oops... Broken pipe.");
 							brokenPipe = true;
 							break;
 						}
 					}
 					if (brokenPipe) {
 						continue;
 					}
 					out.println();
 					inc.close();
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
