 import java.io.* ;
 import java.net.* ;
 import java.util.* ; 
 import com.google.gson.*;
 
 final class HttpRequest implements Runnable {
     Socket socket;
     Map<String, String> header = new HashMap<String, String>();
     private Queue<String> request = new LinkedList<String>();
     static Map<String, Integer> agents = new HashMap<String, Integer>();
 	private HttpResponse response;
 	private InputStream is;
 	private BufferedReader br;
     
     // Constructor
     public HttpRequest(Socket socket) throws Exception {
         this.socket = socket;
     }
     
     // Implement the run() method of the Runnable interface.
     public void run() {
         processRequest();
     }
     
     private void processRequest() {
         try {
 			
 			// Get a reference to the socket's input stream.
 			is = new DataInputStream(socket.getInputStream());
 			br = new BufferedReader(new InputStreamReader(is, "UTF8"));
 
 			// Process the request
             try {
                 String headerLine;
                 String cookiestr = null;
 				String requestLine = null;
                 while ((headerLine = br.readLine()) != null) {
                     if (headerLine.length() < 1) break;
                     
 					if (requestLine == null) requestLine = headerLine;
                     int jj = headerLine.indexOf(':');
                     if (jj == -1) continue;
                     String field = headerLine.substring(0, jj).trim();
                     String value = headerLine.substring(jj+1).trim();
                     if (field.equals("Cookie")) {
                         cookiestr = value;
                     }
                 }
 				
 				// If there's no requestline, then just ignore it as a bad request
 				if (requestLine == null) {
 					tidyUp();
 					return;
 				}
 								
                 // Extract the path from the request line.
                 StringTokenizer tokens = new StringTokenizer(requestLine);
                 String method = tokens.nextToken().trim();
                 String path = tokens.nextToken().trim();
 				
 				// Extract the get paramters from the path
                 Map<String, String> get = new HashMap<String, String>();
 				int ii = path.indexOf('?');
 				if (ii > -1) {
 					String[] getstring = path.substring(ii+1).split("&");
 					for (String key : getstring) {
 						int jj = key.indexOf('=');
 						String field;
 						if ( jj > -1) {
 							field = key.substring(jj+1);
 							key = key.substring(0, jj);
 						} else {
 								field = "true";
 						}
 						key = URLDecoder.decode(key, "UTF-8");
 						field = URLDecoder.decode(field, "UTF-8");
 						get.put(key, field);
 					}
 					path = path.substring(0, ii);
 				}
 
 				// Extract the cookies from the request
 				Map<String, String> cookies = new HashMap<String, String>();
 				if (cookiestr != null) {
 				String[] cookiestrs = cookiestr.split(";");
 					for (String key : cookiestrs) {
 						int jj = key.indexOf('=');
 						String field;
 						if ( jj > -1) {
 							field = key.substring(jj+1);
 							key = key.substring(0, jj);
 						} else {
 								field = "true";
 						}
 						key = URLDecoder.decode(key, "UTF-8").trim();
 						field = URLDecoder.decode(field, "UTF-8").trim();
 						cookies.put(key, field);
 					}
 				}
 				
 				
 				// Get a reference to the socket and create a response.
 				final Socket socket = this.socket;
 				response = new HttpResponse(socket);
 				
 				// Authenticate the request
                 String token = get.get("token");
                 if (token == null) token = cookies.get("token");
                 
                 // An agentid of null means the user hasn't authenticated - an agentid of zero indicates a problem retrieving the agentid from the authentication service
                 Integer agentid = null;
                 if (token != null) {
                     agentid = agents.get(token);
                     if (agentid == null && Manager.authRunning()) {
 						String authurl = "http://"+Manager.authDomain()+"/data?token="+URLEncoder.encode(token, "utf8");
                         try{
                             URL dataurl = new URL(authurl);
                             Gson gson = new Gson();
                             BufferedReader datain = new BufferedReader(new InputStreamReader(dataurl.openStream()));
                             String data = "";
                             String datastr;
                             while ((datastr = datain.readLine()) != null) {
                                 data += datastr;
                             }
                             datain.close();
                             AuthData ad = gson.fromJson(data, AuthData.class);
                             agentid = ad.getId();
                             if (agentid > 0) {
                                 agents.put(token, agentid);
 								response.setHeader("Set-Cookie", "token=" + URLEncoder.encode(token, "utf8"));
                             }
                         } catch (FileNotFoundException e) {
 							Manager.logErr("Auth Error: Can't connect to "+authurl);
                         } catch (IOException e) {
							Manager.logErr("Auth Error: Problem reading from "+authurl);
                         }
                     }
                 }
                 
 				String[] pathParts = path.split("\\/");
                 if (pathParts.length < 2 || path.equals("/services")) {
                     response.redirect("/services/");
 				} else if (pathParts[1].equals("services")) {
 					if (isAuthorised(agentid, method, "http://"+Manager.servicesDomain()+path)) {
 						if (pathParts.length == 2) {
 							response.setBody(Service.getIndexTemplate());
 						} else {
 							Service service = null;
 							String id = pathParts[2];
 							if (id.length() > 0) {
 								try {
 									service = Service.getById(id);
 									if (pathParts.length == 3) {
 										response.setBody(service.getFullTemplate());
 									} else {
 										if (method.equalsIgnoreCase("POST")) {
 											service.execCommand(pathParts[3]);
 										}
 										response.redirect("/services/"+service.getId());
 									}
 								} catch (RuntimeException e) {
 									response.notFound("Service");
 								}
 							}
 						}
 						
 					}
 				} else if (pathParts[1].equals("api")) {
 					if (pathParts.length == 2) {
 						response.setJson("// TODO: write some API documentation");
 					} else if (pathParts[2].equals("hosts")) {
 						response.setJson(Service.getHosts());
 					} else {
 						response.notFound();
 					}
                 } else {
 					
 					if (path.equals("/icon")) path = "/icon.png";
 					String fileName = "./data" + path;
 					fileName.replaceAll("/\\.\\./","");
 					
                     // Open the requested file.
                     FileInputStream fis = null;
                     try {
 						response.setBody(new FileInputStream(fileName));
                     } catch (FileNotFoundException e) {
 						response.notFound();
                     }
                 }
                 
 				response.send();
 				tidyUp();
             
 				
 			} catch (SocketException e) {
 				// Don't do anything if there's a socketexception - it's probably just the client disconnecting before it's received the full response
             } catch (Exception e) {
                 Manager.logErr("Server Error (HttpRequest):");
                 Manager.logErr(e);
             }
             
         } catch (IOException e) {
             Manager.logErr("Server Error (HttpRequest):");
             Manager.logErr(e);
             
         }
     }
     private boolean isAuthorised(Integer agentid, String method, String uri) throws Exception {
         
         // Luke is authorised
         if (agentid != null && agentid.intValue() == 2) return true;
             
         // If the auth service is running then make sure the user has authenticated
         if (Manager.authRunning() && agentid == null) {
             response.redirect("http://"+Manager.authDomain()+"/authenticate?redirect_uri="+URLEncoder.encode(uri, "utf8"));
             return false;
         }
         
         // If the user has successfully authenticated, but isn't authorised, return a 403
         if (agentid != null && agentid > 0) {
             response.setError(403, "Permission Denied");
             return false;
         }
         
         /* Ideally never go past this point - this means either the authentication server isn't running or has returned an invalid agentid */
         if (!Manager.authRunning()) Manager.logErr("Auth service isn't running, using fallback auth rules");
         else Manager.logErr("Auth service returned invalid agentid, using fallback auth rules");
             
         // Allow GET requests so that whatever is causing the problem can be debugged
         if (method.equalsIgnoreCase("GET")) return true;
         
         // Don't allow any other requests as the user hasn't been authenticated
         response.setError(403, "Authentication Error");
         return false;
     }
 
     static class AuthData {
         private int id;
         public AuthData() {
         }
         public int getId() {
              return id;
         }
     }
 	
 	private void tidyUp() {
 		try {
 			br.close();
 			is.close();
 			socket.close();
 		} catch (IOException e) {
 			
 		}
 	}
 
 }
