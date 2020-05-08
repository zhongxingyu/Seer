 package org.cistern.appender.log4j;
 
 import org.apache.log4j.AppenderSkeleton;
 import org.apache.log4j.Level;
 import org.apache.log4j.spi.LoggingEvent;
 // import org.apache.log4j.spi.ErrorCode; - Used for log4j exceptions
 import org.cistern.appender.log4j.LogObject;
 import org.cistern.appender.log4j.Util;
 
 import java.security.NoSuchAlgorithmException;
 
 import java.io.*;
 import java.net.*;
 
 
 
 
 public class TCPAppender extends AppenderSkeleton {
 	
 	//Setup variables
 	String serverhost;
 	int serverport = 9845;
 	String agent_id;
 	String logtype_id;
 	String authkey;
 	int timeout = 3000;
 	
 	static String eq = "/000/";
 	static String newval = "/111/";
 	Socket sock;
 
 	//Setup loglevels
 	String fatal = "1";
 	String error = "2";
 	String warn = "3";
 	String info = "4";
 	String debug = "5";
 	String unknown = "6";
 	String level;
 
 	protected void append(LoggingEvent arg0) {
 		// Log4j calls this with a log event
 		String s;
 		String data = null;
 		LoggingEvent entry = arg0;
 		
 		//Convert any throwable data in to a string to be sent.
 		if (entry.getThrowableStrRep() == null){
 			s = "";
 		} else {
			String[] array = entry.getThrowableStrRep();
			s = "";
			for(int i=0; i < array.length; i++) {
				s = s + array[i] + "\n";
			}
 		}
 		
 		//Create a cistern LogObject from the log4j LoggerEvent
 		LogObject event = new LogObject("<<<MESSAGE>>> " + s, "MESSAGE" + eq + entry.getMessage(), this.authkey, this.logtype_id, this.agent_id);
 		
 		//Set the LogObject loglevel from the LoggerEvent
 		//TODO: Make better level setting
		if (entry.getLevel() .equals(Level.DEBUG)) {
 			level = debug;
		}else if (entry.getLevel() .equals(Level.INFO)){
 			level = info;
		}else if (entry.getLevel() .equals(Level.WARN)){
 			level = warn;
		}else if (entry.getLevel() .equals(Level.ERROR)){
 			level = error;
		}else if (entry.getLevel() .equals(Level.FATAL)){
 			level = fatal;
 		}else{
 			level = unknown;
 		}
 		event.setLoglevel_id(level);
 		
 		//System.out.println(entry.getLevel());
 		
 		//Set LogObject etime to the original Log4j event time
 		//TODO: fix timestamp set from log4j. Doesn't seem to be compatable with some versions of log4j
 		//event.setEtime(Long.toString(entry.getTimeStamp()/1000));
 		
 		//Send the LogObject to Cistern Server
 		try {
 			//Make a string for Cistern Server
 			data = Util.getSendableString(event);
 		} catch (UnsupportedEncodingException e) {
 			//If encoding is not supported (UTF-8) do this:
 			e.printStackTrace();
 		} catch (NoSuchAlgorithmException e) {
 			//If MD5 is not supported do this:
 			e.printStackTrace();
 		}
 
 		//System.out.println(data);
 		
 		//Do Send
 		try {
 			sendData(data);
 		} catch (IOException e) {
 			//If the pipe is broken do this:
 			//TODO: Handle broken pipe gracefully.
 			e.printStackTrace();
 		}
 	}
 
 	public void sendData(String data) throws IOException {
 		//This method sends a string to Cistern Server
 		PrintWriter out = null;
 		
 		//TODO: if the socket is down, all log events need to be dropped quietly.
 		try {
 			out = new PrintWriter(sock.getOutputStream(), true);		
 			out.println(data);
 		} catch (SocketTimeoutException e) {
 			//If the socket is not respoding do this:
 			e.printStackTrace();
 		}
 	}
 
 	public void close() {
 		//Log4j calls this on shutdown
 		try {
 			//Close out the socket connection cleanly
 			sock.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 
 	public boolean requiresLayout() {
 		//Log4j calls this to find out if the appender needs a layout configured.
 		//no layout required. all formatting is done in the appender.
 		return false;
 	}
 	
 	public void activateOptions(){ 
 		//Log4j calls this at startup to initialize things
 		Socket socket = new Socket();
 		try {
 			socket.connect(new InetSocketAddress(serverhost, serverport), timeout);
 			this.setSock(socket);
 		} catch (SocketTimeoutException e) {
 			//do this if the socket connection times out
 			//TODO: if this fails we should try and reconnect
 			e.printStackTrace();
 		} catch (IOException e) {
 			//do this if the pipe is broken
 			//TODO: handle broken pipe gracefully
 			e.printStackTrace();
 		}
 	}
 	
 	//Getters and Setters
 	public String getServerhost() {
 		return serverhost;
 	}
 
 	public void setServerhost(String serverhost) {
 		this.serverhost = serverhost;
 	}
 
 	public int getServerport() {
 		return serverport;
 	}
 
 	public void setServerport(int serverport) {
 		this.serverport = serverport;
 	}
 
 	public String getAgent_id() {
 		return agent_id;
 	}
 
 	public void setAgent_id(String agent_id) {
 		this.agent_id = agent_id;
 	}
 
 	public String getLogtype_id() {
 		return logtype_id;
 	}
 
 	public void setLogtype_id(String logtype_id) {
 		this.logtype_id = logtype_id;
 	}
 
 	public String getAuthkey() {
 		return authkey;
 	}
 
 	public void setAuthkey(String authkey) {
 		this.authkey = authkey;
 	}
 
 	public int getTimeout() {
 		return timeout;
 	}
 
 	public void setTimeout(int timeout) {
 		this.timeout = timeout;
 	}
 	
 	public Socket getSock() {
 		return sock;
 	}
 
 	public void setSock(Socket sock) {
 		this.sock = sock;
 	}
 	
 }
