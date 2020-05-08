 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.servlet.container;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 
 import org.apache.commons.httpclient.HttpStatus;
 
 import org.xins.common.Log;
 import org.xins.common.Utils;
 
 /**
  * HTTP Server used to invoke the XINS Servlet.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 public class HTTPServletHandler {
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Starts the Servlet container for the specific API.
     * 
     * @param args
     *    The command line arguments, the first argument should be the location
     *    of the WAR file, the optional second argument is the port number. 
     *    If no port number is specified, 8080 is used as default.
     */
    public static void main(String[] args) {
       if (args.length < 1) {
          System.err.println("Please, pass the location of the WAR file as argument.");
          System.exit(-1);
       }
       File warFile = new File(args[0]);
       if (!warFile.exists()) {
          System.err.println("WAR file \"" + args[0] + "\" not found.");
          System.exit(-1);
       }
       int port = DEFAULT_PORT_NUMBER;
       if (args.length > 1) {
          try {
             port = Integer.parseInt(args[1]);
          } catch (NumberFormatException nfe) {
             System.err.println("Warning: Incorrect port number \"" + args[1] + 
                   "\", using " + DEFAULT_PORT_NUMBER + " as port number.");
          }
       }
       
       try {
          // Starts the server
          new HTTPServletHandler(warFile, port);
       } catch (Exception ioe) {
          ioe.printStackTrace();
       }
    }
    
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The default port number.
     */
    public final static int DEFAULT_PORT_NUMBER = 8080;
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Creates a new HTTPSevletHandler. This Servlet handler starts a web server
     * on port 8080 and wait for calls from the XINSServiceCaller.
     *
     * @param warFile
     *    the war file of the application to deploy, cannot be <code>null</code>.
     *
     * @throws IOException
     *    if the servlet cannot be initialized.
     *
     * @throws IOException
     *    if the servlet cannot be started.
     */
    public HTTPServletHandler(File warFile)
    throws IOException, ServletException {
       this(warFile, DEFAULT_PORT_NUMBER);
    }
 
    /**
     * Creates a new HTTPSevletHandler. This Servlet handler starts a web server
     * and wait for calls from the XINSServiceCaller.
     *
     * @param warFile
     *    the war file of the application to deploy, cannot be <code>null</code>.
     *
     * @param port
     *    the port of the web server, cannot be <code>null</code>.
     *
     * @throws IOException
     *    if the servlet cannot be initialized.
     *
     * @throws IOException
     *    if the servlet cannot be started.
     */
    public HTTPServletHandler(File warFile, int port)
    throws IOException, ServletException {
 
       // Create the servlet
       _servletHandler = LocalServletHandler.getInstance(warFile);
 
       // Start the HTTP server.
       startServer(port);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The servlet.
     */
    private LocalServletHandler _servletHandler;
 
    /**
     * The web server.
     */
    private ServerSocket _serverSocket;
 
    /**
     * The thread that waits for connections from the client.
     */
    private SocketAcceptor _acceptor;
 
    /**
     * flag indicating if the server should wait for other connections or stop.
     */
    private boolean _running;
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Starts the web server.
     *
     * @param port
     *    The port of the servle server.
     *
    * @throw IOException
    *    If the web server cannot be created.
     */
    public void startServer(int port) throws IOException {
       // Create the server socket
       _serverSocket = new ServerSocket(port, 5);
       _running = true;
 
       _acceptor = new SocketAcceptor();
       _acceptor.start();
    }
 
    /**
     * Dispose the servlet and stops the web server.
     */
    public void close() {
       _running = false;
       _servletHandler.close();
       try {
          _serverSocket.close();
       } catch (IOException ioe) {
          Log.log_1502(ioe);
       }
    }
 
    /**
     * This method is invoked when a client connects to the server.
     *
     * @param client
     *    the connection with the client.
     */
    public void serviceClient(Socket client) throws IOException {
       BufferedOutputStream outbound = null;
       try {
          // Acquire the streams for IO
          BufferedReader inbound = new BufferedReader(new InputStreamReader(client.getInputStream()));
          outbound = new BufferedOutputStream(client.getOutputStream());
 
          // Get the output
          String httpResult = httpQuery(inbound);
 
          outbound.write(httpResult.getBytes("ASCII"), 0, httpResult.length());
 
       } finally{
          // Clean up
          outbound.close();
 
          // The following close statements doesn't work on Unix.
          // inbound.close();
          // outbound.close();
          // client.close();
       }
    }
 
    /**
     * This method parses the data sent from the client to get the input
     * parameters and format the result as a compatible HTTP result.
     *
     * @param input
     *    the input stream that contains the data send by the client.
     *
     * @return
     *    the HTTP result to send back to the client.
     */
    public String httpQuery(BufferedReader input) throws IOException {
       String inputLine;
       String query = null;
 
       while (query == null && (inputLine = input.readLine()) != null) {
          if (inputLine.startsWith("GET ")) {
             int questionPos = inputLine.indexOf('?');
             if (questionPos !=-1) {
                int lastSpace = inputLine.indexOf(' ', questionPos);
                if (lastSpace != -1) {
                   inputLine = inputLine.substring(questionPos + 1, lastSpace);
                } else {
                   inputLine = inputLine.substring(questionPos + 1);
                }
             }
             query = inputLine.replace(',', '&');
          }
 
          // POST method
          if (inputLine.startsWith("Content-Length: ")) {
             int postLength = Integer.parseInt(inputLine.substring(16));
             input.readLine();
             input.readLine();
             char[] data = new char[postLength];
             input.read(data);
             query = new String(data);
          }
       }
       if (query != null) {
          XINSServletResponse response = _servletHandler.query(query);
          String result = response.getResult();
          if (result == null) {
             result = "HTTP/1.1 " + response.getStatus() + " " + HttpStatus.getStatusText(response.getStatus()).replace(' ', '_') + "\n\n";
             return result;
          }
          String httpResult = "HTTP/1.1 " + response.getStatus() + " " + HttpStatus.getStatusText(response.getStatus()) + "\n";
          httpResult += "Content-type: " + response.getContentType() + "\n";
          int length = result.length() + 1;
          httpResult += "Content-Length: " + length + "\n";
          httpResult += "Connection: close\n";
          httpResult += "\n";
          httpResult += result + "\n";
          httpResult += "\n";
          return httpResult;
       }
       return "HTTP/1.1 400 BAD_REQUEST\n\n";
    }
 
    /**
     * Thread waiting for connection from the client.
     */
    private class SocketAcceptor extends Thread {
 
       /**
        * Create the thread.
        */
       public SocketAcceptor() {
          setDaemon(true);
       }
 
       /**
        * Executes the thread.
        */
       public void run() {
          Log.log_1500(_serverSocket.getLocalPort());
          try {
             while (_running) {
                // Wait for a connection
                Socket clientSocket = _serverSocket.accept();
 
                try {
                   // Service the connection
                   serviceClient(clientSocket);
                } catch (Exception ex) {
                   // If anything goes wrong still continue to listen to the port.
                   Utils.logProgrammingError(getClass().getName(), "run", "", "", "", ex);
                }
             }
          } catch (SocketException ie) {
             // fall through
          } catch (IOException ioe) {
             Log.log_1501(ioe);
          }
       }
    }
 }
