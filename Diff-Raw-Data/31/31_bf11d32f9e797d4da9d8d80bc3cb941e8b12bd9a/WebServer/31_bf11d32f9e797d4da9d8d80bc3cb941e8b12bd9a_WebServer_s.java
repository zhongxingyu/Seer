 package com.torandi.intnet13.http;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.torandi.intnet13.net.ServerSocket;
 import com.torandi.intnet13.net.Socket;
 import com.torandi.intnet13.net.SocketListener;
 
 public class WebServer implements SocketListener {
 
 	private static final Pattern view_attr_pattern = Pattern.compile("<\\?:(.+?)\\?>");
 	
 	private ServerSocket server;
 	private LinkedList<Request> requests;
 	private File root;
 	private File public_dir;
 	private File error_dir;
 	private File view_dir;
 	private HashMap<String, MimeType> mime_types;
 	
 	private SecureRandom random;
 
 	/*
 	 * Session id => { var => val }
 	 */
 	private HashMap<String, HashMap<String,String>> data_store;
 	
 	public WebServer(int port, String root_dir) throws IOException, Exception {
 		server = new ServerSocket(port);
 		root = new File(root_dir);
 		if(!root.isDirectory()) {
 			throw new Exception("Root directory "+root.getAbsolutePath()+" does not exist, or is not a directory");
 		}
 		public_dir = new File(root, "public");
 		error_dir = new File(root, "error");
 		view_dir = new File(root, "views");
 		if(!public_dir.isDirectory()) {
 			public_dir.mkdir();
 		}
 		if(!error_dir.isDirectory()) {
 			error_dir.mkdir();
 		}
 		if(!view_dir.isDirectory()) {
 			view_dir.mkdir();
 		}
 	
 		requests = new LinkedList<Request>();
 		data_store = new HashMap<String, HashMap<String, String>>();
 		
 		random = new SecureRandom();
 		
 		mime_types = new HashMap<String, MimeType>();
 		load_mimetypes();
 	}
 	
 	public void run() {
 		server.listen(this, true); //Start blocking listen
 	}
 	
 	private HashMap<String, String> client_data_store(String session_id) {
 		HashMap<String, String> ds = data_store.get(session_id);
 		if(ds == null) {
 			ds = new HashMap<String, String>();
 			data_store.put(session_id, ds);
 		}
 		return ds;
 	}
 	
 	private void load_mimetypes() {
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(new File(root, "mime-types")));
 			String line = null;
 			while((line = reader.readLine()) != null) {
 				String split[] = line.split(" ");
 				if(split.length == 2) {
 					mime_types.put(split[0], new MimeType(split[1], false));
 				} else if(split.length == 3) {
 					mime_types.put(split[0], new MimeType(split[1], split[2].equals("b")));
 				} else {
 					System.out.println("Invalid mine type pair: "+line);
 					System.exit(-1);
 				}
 			}
 		} catch (IOException e) {
 			
 		}
 	}
 	
 	private String parse_view(File file, Page page) throws HTTPError {
 		String content = file_content(file);
 		if(content == null) return null;
 		
 		Matcher m = view_attr_pattern.matcher(content);
 		while(m.find()) {
 			String attr = m.group(1);
 			String val = page.attr(attr);
 			if(val == null) {
 				throw new HTTPError(500, "Unknown attributes", "In view "+file.getAbsolutePath()+": Unknown attribute \""+attr+"\"");
 			}
 			content = content.replace(m.group(), val);
 			m = view_attr_pattern.matcher(content);
 		}
 		
 		return content;
 	}
 	
 	private String file_content(File f) {
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(f));
 			StringBuilder str = new StringBuilder();
 			String line = null;
 			while((line = reader.readLine()) != null) {
 				str.append(line + "\r\n");
 			}
 			reader.close();
 			return str.toString();
 		} catch (Exception e) {
 			System.err.println("Failed to read file "+f.getAbsolutePath()+" "+e.getMessage());
 			return null;
 		}
 		
 	}
 	
 	private Page page_from_file(String filename) throws HTTPError {
 		filename = filename.substring(0, filename.lastIndexOf(".class") );
 		String as_classname = filename.replace("/", ".");
 		WebPageClassLoader loader = new WebPageClassLoader(public_dir);
 		Class<?> page_class = loader.findClass(as_classname);
 		try {
 			Page page = (Page) page_class.newInstance();
 			return page;
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new HTTPError(500, "Internal Server Error", "Failed to load class "+as_classname+"<br/>"+stacktrace_to_string(e));
 		}
 	}
 	
 	private void cleanup_requests() {
 		Iterator<Request> it = requests.iterator();
 	
 		while(it.hasNext()) {
 			Request r = it.next();
 			if(!r.update()) {
 				it.remove();
 			}
 		}
 	}
 	
 	@Override
 	public void dataRecived(String data, Socket sck) { }
 
 	@Override
 	public void newClient(Socket client, ServerSocket srvr) {
 		cleanup_requests();
 		Request c = new Request(client);
 		
 		requests.push(c);
 	}
 	
 	public boolean file_exists(String file) {
 		return new File(public_dir, file).exists();
 	}
 	
 	private String stacktrace_to_string(Exception e) {
 		StringBuilder str = new StringBuilder();
 		str.append("<pre>");
 		String message = e.getMessage();
 		if(message == null) message = "";
 		else message = ": " + message;
 		str.append(e.getClass().getName() + message+"\r\n");
 		for(StackTraceElement st : e.getStackTrace()) {
 			str.append("\t at "+st.toString()+"\r\n");
 		}
 		str.append("</pre>");
 		return str.toString();
 	}
 
 	@Override
 	public void connectionClosed(Socket sck) { }
 	
 	public class Request implements SocketListener {
 	
 		private static final long timeout = 10000;
 		private final SimpleDateFormat date_format = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss z");
 		
 		private Socket socket;
 		private long timestamp;
 		private boolean closed = false;
 		private boolean ignore = false;
 		private boolean keep_alive = true;
 		
 		private int response_code = 200;
 		private String response_message = "OK";
 		private HashMap<String, String> headers;
 		private String response_body = "";
 		
 		private String request_addr;
 		private String verb = null;
 	
 		private HashMap<String, String> request_headers;
 		private HashMap<String, String> cookies;
 		private HashMap<String, String> request_data;
 	
 		private String session_id = null;
 		
 		private HashMap<String, String> data_store = null;
 		
 		public Request(Socket socket) {
 			this.socket = socket;
 			timestamp = System.currentTimeMillis();
 			this.socket.receive(this);
 			headers = new HashMap<String, String>();
 			request_headers = new HashMap<String, String>();
 			request_data = new HashMap<String, String>();
 			cookies = new HashMap<String, String>();
 		}
 
 		public String request(String var) {
 			return request_data.get(var);
 		}
 		
 		public String get_data(String var) {
 			if(data_store == null) {
 				data_store = client_data_store(session_id);
 			}
 			return data_store.get(var);
 		}
 		
 		public void set_data(String var, String val) {
 			if(data_store == null) {
 				data_store = client_data_store(session_id);
 			}
 			data_store.put(var, val);
 		}
 		
 		private void generate_session_id() {
 			session_id = new BigInteger(130, random).toString(32);
 		}
 	
 		private void reset_request() {
 			cookies.clear();
 			request_addr = "";
 			verb = null;
 			request_data.clear();
 			request_headers.clear();
 			ignore = false;
 		}
 		
 		private void reset_response() {
 			set_response(200, "OK");
 			response_body = "";
 			headers.clear();
 			headers.put("Date", date_format.format(new Date()));
 			headers.put("Server", "Tarandi Java Web Crap");
 			headers.put("Connection", "keep-alive");
 			headers.put("Content-Type", "text/html");
 		}
 	
 		private void write_headers() {
 			StringBuilder response = new StringBuilder();
 			response.append("HTTP/1.1 "+response_code+" "+response_message+"\r\n");
 			for(String key : headers.keySet()) {
 				response.append(key + ": "+headers.get(key) + "\r\n");
 			}
 			response.append("\r\n");
 			socket.out().print(response.toString());
 		}
 		
 		private void write_response() {
 			set_header("Content-Length", "" + response_body.getBytes().length);
 			write_headers();
 			try {
 				socket.out().write(response_body.getBytes());
 			} catch (IOException e) {
 				System.err.println("Failed to write bytes to socket");
 				e.printStackTrace();
 			}
 			socket.out().flush();
 			if(!keep_alive) {
 				try { socket.close(); } catch (IOException e) { }
 				closed = true;
 			}
 			reset_request();
 			reset_response();
 		}
 		
 		private void write_binary(String file) {
 			byte[] b = null;
 			try {
 	            FileInputStream is = new FileInputStream(new File(public_dir, file));
 	            int bytes = is.available();
 	            b = new byte[bytes];
 	            is.read(b);
 	            is.close();
 	            
 	            set_header("Content-Length", "" + bytes);
             
 			} catch (IOException e) {
 				error_page(new HTTPError(500, "Internal Server Error", "Failed to read "+file+": <hr/>"+stacktrace_to_string(e)));
 				return;
 			}
 			
 			write_headers();
 		
 			try { socket.out().write(b); } catch (IOException e) {
 				System.err.println("Failed to write bytes to socket");
 				e.printStackTrace();
 			}
 			socket.out().flush();
 			if(!keep_alive) {
 				try { socket.close(); } catch (IOException e) { }
 				closed = true;
 			}
 			reset_request();
 			reset_response();
 		}
 		
 		/*
 		 * Check if the client have timed out
 		 * @return boolean Indicating if the client is still alive
 		 */
 		public boolean update() {
 			if(closed) return false;
 			if(System.currentTimeMillis() - timestamp > timeout ) {
 				try { socket.close(); } catch (IOException e) { }
 				closed = true;
 				return false;
 			} else {
 				return true;
 			}
 		}
 	
 		public void set_response(int code, String msg) {
 			response_code = code;
 			response_message = msg;
 		}
 		
 		public void set_header(String key, String value) {
 			headers.put(key, value);
 		}
 		
 		private void error_page(HTTPError error) {
 			ignore = true;
 			reset_response();
 			String error_content = error_page_content(error);
 			response_body = error_content;
 			write_response();
 		}
 	
 		private String error_page_content(HTTPError error) {
 			set_response(error.code, "ERROR");
 			File f = new File(error_dir, error.code + ".jhtml");
 			if(!f.isFile()) {
 				f = new File(error_dir, "default.jhtml");
 			}
 			try {
 				String result = parse_view(f, new ErrorPage(error));
 				if(result == null) {
 					return error.code + error.code_description + " :" + error.getMessage();
 				} else {
 					return result;
 				}
 			} catch (HTTPError e) {
 				set_response(e.code, "ERROR");
 				return e.code + e.code_description + " :" + e.getMessage();
 			}
 		}
 	
 		public String view(String view, Page page) throws HTTPError {
 			File file = new File(view_dir, view + ".jhtml");
 			String result = parse_view(file, page);
 			if(result == null) {
 				throw new HTTPError(500, "View not found", "View "+file.getAbsolutePath()+ " not found");
 			} else {
 				return result;
 			}
 		}
 		
 		@Override
 		public void dataRecived(String data, Socket sck) {
 			if(!ignore) {
 				timestamp = System.currentTimeMillis();
 				
 				if(verb == null) {
 					String split[] = data.split(" ");
 					if(split.length != 3) {
 						error_page(new HTTPError(400, "Bad Request","Invalid split size"));
 						return;
 					}
 					verb = split[0];
 					request_addr = split[1];
 					if(!split[2].equals("HTTP/1.1")) {
 						error_page(new HTTPError(400, "Bad Request","Wrong protocol"));
 						return;
 					}
 					if(!verb.equals("GET")) {
 						error_page(new HTTPError(405, "Method Not Allowed"));
 						return;
 					}
 					return;
 				}
 			
 				if(data.isEmpty()) {
 					handle_request();
 				} else {
 					String split[] = data.split(":");
 					request_headers.put(split[0], split[1]);
 				}
 			} else if(data.isEmpty()) {
 				reset_request();
 				reset_response();
 			}
 		}
 		
 		private void parse_data(String data) {
 			for(String pair : data.split("&")) {
 				String split[] = pair.split("=", 2);
 				if(split.length == 2) {
 					request_data.put(split[0], split[1]);
 				} else {
 					request_data.put(pair, "");
 				}
 			}
 		}
 		
 		private void handle_request() {
 			//Parse cookies:
 			String cookie_string = request_headers.get("Cookie");
 			if(cookie_string != null) {
 				for(String cookie : cookie_string.split(";")) {
 					String split[] = cookie.split("=", 2);
 					cookies.put(split[0].trim(), split[1].trim());
 				}
 			}
 		
 			session_id = cookies.get("SESSIONID");
 		
 			if(session_id == null) {
 				generate_session_id();
 			}
 			
 			set_header("Set-Cookie", "SESSIONID="+session_id);
 	
 			String url = request_addr;
 			//Parse request address
 			int start_get = url.lastIndexOf('?');
 			if(start_get >= 0) {
 				parse_data(url.substring(start_get + 1));
 				url = url.substring(0, start_get);
 			}
 			
 			
 			String file = url.trim();
 			
			if(file.startsWith("/")) {
				file = file.substring(1);
			}
		
 			File file_obj = new File(public_dir, file);
 			
 			if(file_obj.isDirectory()) {
 				if(!file.endsWith("/")) {
 					file += "/";
 				}
 				if(new File(file_obj,"Default.class").exists()) {
 					file_obj = new File(file_obj,"Default.class");
 					file += "Default.class";
 				} else {
 					file_obj = new File(file_obj,"index.html");
 					file += "index.html";
 				}
 			}
 			
 			try {
 				if(!file_exists(file)) {
 					System.err.println("Couldn't find file "+ file_obj.getAbsolutePath());
 					throw new HTTPError(404, "File Not Found", "The path " + url + " does not exist on this server");
 				} else {
 					set_header("Last-Modified", date_format.format(new Date(file_obj.lastModified())));
 					String file_ending = file.substring(file.lastIndexOf(".") + 1);
 					if(file_ending.equals("class")) {
 						Page page = page_from_file(file);
 						try {
 							response_body = page.html(this);
 						} catch (HTTPError e) {
 							throw new HTTPError(e.code, e.code_description, e.getMessage() +" <hr/> Stack trace: "+stacktrace_to_string(e));
 						}
 					} else {
 						MimeType mime = mime_types.get(file_ending);
 						if(mime != null) {
 							set_header("Content-Type", mime.mime);
 							if(mime.binary) {
 								write_binary(file);
 								return;
 							}
 						}
 						response_body = file_content(new File(public_dir, file));
 					}
 				}
 			} catch (HTTPError e) {
 				error_page(e);
 				return;
 			}
 			
 			write_response();
 		}
 
 		@Override
 		public void connectionClosed(Socket sck) {
 			closed = true;
 		}
 		
 		@Override
 		public void newClient(Socket client, ServerSocket srvr) { }
 
 	}
 	
 	private static class MimeType {
 		public String mime;
 		public boolean binary;
 		
 		public MimeType(String mime, boolean binary) {
 			this.mime = mime;
 			this.binary = binary;
 		}
 	}
 }
