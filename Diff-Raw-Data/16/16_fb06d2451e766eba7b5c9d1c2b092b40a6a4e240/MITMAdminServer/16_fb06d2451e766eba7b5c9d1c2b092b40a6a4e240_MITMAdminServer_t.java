 /**
  * CS255 project 2
  */
 
 package mitm;
 
 import java.net.*;
 import java.io.*;
 import java.security.GeneralSecurityException;
 import java.util.*;
 import java.util.regex.*;
 
 class MITMAdminServer implements Runnable
 {
     private ServerSocket m_serverSocket;
     private Socket m_socket = null;
     private HTTPSProxyEngine m_engine;
     
     public MITMAdminServer( String localHost, int adminPort, HTTPSProxyEngine engine ) throws IOException,GeneralSecurityException {
 	MITMSSLSocketFactory socketFactory = new MITMSSLSocketFactory();
 				
 	m_serverSocket = socketFactory.createServerSocket( localHost, adminPort, 0 );
 	m_engine = engine;
     }
 
     public void run() {
 	System.out.println("Admin server initialized, listening on port " + m_serverSocket.getLocalPort());
 	while( true ) {
 	    try {
 		m_socket = m_serverSocket.accept();
 
 		byte[] buffer = new byte[40960];
 
 		Pattern userPwdPattern =
 		    Pattern.compile("password:(\\S+)\\s+command:(\\S+)\\sCN:(\\S*)\\s");
 		
 		BufferedInputStream in =
 		    new BufferedInputStream(m_socket.getInputStream(),
 					    buffer.length);
 
 		// Read a buffer full.
 		int bytesRead = in.read(buffer);
 
 		String line =
 		    bytesRead > 0 ?
 		    new String(buffer, 0, bytesRead) : "";
 
 		Matcher userPwdMatcher =
 		    userPwdPattern.matcher(line);
 
 		// parse username and pwd
 		if (userPwdMatcher.find()) {
 		    String password = userPwdMatcher.group(1);
 
                     /*
                      * CS255
                      *
                      * Need to Authenticate the user here. The user entered password is got
                      * and needs to be compared with what we have as the right password. The
                      * salted password hash is stored in the PWD_FILE.
                      *
                      * We use BCrypt library to create a salted hash and also for comparison
                      *
                      */
 
                     /*
                      * A salted password hash created using BCrypt library
                      *     String hashed = BCrypt.hashpw("admin", BCrypt.gensalt());
                      *     System.out.println("admin: " + hashed);
                      */
 
                     String hashed = "";
                     boolean authenticated = false;
 
                     // Get the hased password from our file.
                     try {
                         hashed = new Scanner(new File(JSSEConstants.PWD_FILE)).useDelimiter("\\Z").next();
                     } catch (FileNotFoundException e) {
                         sendString("Required password file not found\n");
                         authenticated = false;
                     }
 
                     // Check if the user entered password is the right one
                     try {
                         if (BCrypt.checkpw(password, hashed)) {
                             authenticated = true;
                         } else {
                             authenticated = false;
                         }
                     } 
                     catch (Exception e) {
                         sendString("Passwords dont match\n");
                         authenticated = false;
                     }
 
 		    // if authenticated, do the command
 		    if( authenticated ) {
 			String command = userPwdMatcher.group(2);
 			String commonName = userPwdMatcher.group(3);
 			doCommand( command );
 		    } else {
                         sendString("Authentication failed!\n");
                         m_socket.close();
                     }
 		}	
 	    }
 	    catch( InterruptedIOException e ) {
 	    }
 	    catch( Exception e ) {
 		e.printStackTrace();
 	    }
 	}
     }
 
     private void sendString(final String str) throws IOException {
 	PrintWriter writer = new PrintWriter( m_socket.getOutputStream() );
 	writer.println(str);
 	writer.flush();
     }
     
     private void doCommand( String cmd ) throws IOException {
 
         /*
          * CS255
          *
          * We support only two actions:
          *   stats     To display the number of proxy connections made
          *   shutdown  Shutdown the proxy
          */
         String c = cmd.toLowerCase();
         if (c.equals("stats")) {
             //
             // stats gives the total number of connections made by the proxy. We use a simple
             // file based counter. Each time a proxy connection is made the count is captured
             // in the file. When a request is issued to read the stats, we display the info
             // from the file
             //
             File statsFile = new File(JSSEConstants.STATS_COUNT_FILE);
            if (! statsFile.exists()) {
                sendString("Total number of requests proxied: 0\n");
            } else {
                int requests = 0;
                Scanner s = new Scanner(statsFile);
                while (s.hasNextInt()) {
                    requests = s.nextInt();
                }
                sendString("Total number of requests proxied: " + requests + "\n");
             }
         } else if (c.equals("shutdown")) {
             // Shutdown is a simple exit for us
             sendString("Shutting down proxy server\n");
             System.exit(0);
         } else {
             // Be nice
             sendString("Unknown command: " + c);
             sendString("Expected: stats | shutdown\n");
         }
 
 	m_socket.close();
 	
     }
 
 }
