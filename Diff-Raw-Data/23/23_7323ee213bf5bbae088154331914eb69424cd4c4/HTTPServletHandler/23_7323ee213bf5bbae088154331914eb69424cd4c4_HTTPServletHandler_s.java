 /*
  * $Id$
  *
  * Copyright 2003-2006 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.servlet.container;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.FileNameMap;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.servlet.ServletException;
 
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.log4j.helpers.NullEnumeration;
 
 import org.xins.common.Library;
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.text.ParseException;
 
 /**
  * HTTP server used to invoke the XINS servlet.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ft.com">Anthony Goubard</a>
  * @author <a href="mailto:ernst.dehaan@orange-ft.com">Ernst de Haan</a>
  */
 public class HTTPServletHandler {
 
    // TODO: This class should be cleaned up further
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Initializes the logging subsystem with fallback default settings.
     */
    private static final void configureLoggerFallback() {
       Properties settings = new Properties();
       settings.setProperty("log4j.rootLogger",                                "ALL, console");
       settings.setProperty("log4j.appender.console",                          "org.apache.log4j.ConsoleAppender");
       settings.setProperty("log4j.appender.console.layout",                   "org.apache.log4j.PatternLayout");
       settings.setProperty("log4j.appender.console.layout.ConversionPattern", "%16x %6c{1} %-6p %m%n");
       settings.setProperty("log4j.logger.org.xins.",                          "INFO");
       PropertyConfigurator.configure(settings);
    }
 
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The default port number is 8080.
     */
    public final static int DEFAULT_PORT_NUMBER = 8080;
 
    /**
     * The map containing the MIME type information. Never <code>null</code>
     */
    private final static FileNameMap MIME_TYPES_MAP = URLConnection.getFileNameMap();
 
    private final static String REQUEST_ENCODING = "ISO-8859-1";
 
    private final static String CRLF = "\r\n";
 
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Creates a new HTTPSevletHandler with no Servlet. Use the addServlet
     * methods to add the WAR files or the Servlets.
     *
     * @param port
     *    The port of the servlet server.
     *
     * @param daemon
     *    <code>true</code> if the thread listening to connection should be a
     *    daemon thread, <code>false</code> otherwise.
     *
     * @throws IOException
     *    if the servlet container cannot be started.
     */
    public HTTPServletHandler(int port, boolean daemon) throws IOException {
 
       // Configure log4j if not already done.
       Enumeration appenders = LogManager.getLoggerRepository().getRootLogger().getAllAppenders();
       if (appenders instanceof NullEnumeration) {
          configureLoggerFallback();
       }
 
       // Start the HTTP server.
       startServer(port, daemon);
    }
 
    /**
     * Creates a new <code>HTTPServletHandler</code>. This servlet handler
     * starts a web server on port 8080 and wait for calls from the
     * <code>XINSServiceCaller</code>.
     *
     * <p>Note that all the libraries used by this WAR file should already be
     * in the classpath.
     *
     * @param warFile
     *    the war file of the application to deploy, cannot be
     *    <code>null</code>.
     *
     * @throws ServletException
     *    if the servlet cannot be initialized.
     *
     * @throws IOException
     *    if the servlet container cannot be started.
     */
    public HTTPServletHandler(File warFile)
    throws ServletException, IOException {
       this(DEFAULT_PORT_NUMBER, true);
       addWAR(warFile, "/");
    }
 
    /**
     * Creates a new <code>HTTPSevletHandler</code>. This servlet handler
     * starts a web server on the specified port and waits for calls from the XINSServiceCaller.
     * Note that all the libraries used by this WAR file should already be in
     * the classpath.
     *
     * @param warFile
     *    the war file of the application to deploy, cannot be
     *    <code>null</code>.
     *
     * @param port
     *    The port of the servlet server.
     *
     * @param daemon
     *    <code>true</code> if the thread listening to connection should be a
     *    daemon thread, <code>false</code> otherwise.
     *
     * @throws ServletException
     *    if the servlet cannot be initialized.
     *
     * @throws IOException
     *    if the servlet container cannot be started.
     */
    public HTTPServletHandler(File warFile, int port, boolean daemon)
    throws ServletException, IOException {
       this(port, daemon);
       addWAR(warFile, "/");
    }
 
    /**
     * Creates a new HTTPSevletHandler. This Servlet handler starts a web server
     * and wait for calls from the XINSServiceCaller.
     *
     * @param servletClassName
     *    The name of the servlet's class to load, cannot be <code>null</code>.
     *
     * @throws ServletException
     *    if the servlet cannot be initialized.
     *
     * @throws IOException
     *    if the servlet container cannot be started.
     */
    public HTTPServletHandler(String servletClassName) throws ServletException, IOException {
       this(DEFAULT_PORT_NUMBER, true);
       addServlet(servletClassName, "/");
    }
 
    /**
     * Creates a new HTTPSevletHandler. This Servlet handler starts a web server
     * and wait for calls from the XINSServiceCaller.
     *
     * @param servletClassName
     *    The name of the servlet's class to load, cannot be <code>null</code>.
     *
     * @param port
     *    The port of the servlet server.
     *
     * @param daemon
     *    <code>true</code> if the thread listening to connection should be a
     *    daemon thread, <code>false</code> otherwise.
     *
     * @throws ServletException
     *    if the servlet cannot be initialized.
     *
     * @throws IOException
     *    if the servlet container cannot be started.
     */
    public HTTPServletHandler(String servletClassName, int port, boolean daemon) throws ServletException, IOException {
       this(port, daemon);
       addServlet(servletClassName, "/");
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The web server.
     */
    private ServerSocket _serverSocket;
 
    /**
     * The thread that waits for connections from the client.
     */
    private SocketAcceptor _acceptor;
 
    /**
     * Flag indicating if the server should wait for other connections or stop.
     */
    private boolean _running;
 
    /**
     * Mapping between the path and the servlet.
     */
    private Map _servlets = new HashMap();
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Adds a WAR file to the server.
     * The servlet with the virtual path "/" will be the default one.
     * Note that all the libraries used by this WAR file should already be in
     * the classpath.
     *
     * @param warFile
     *    The war file of the application to deploy, cannot be <code>null</code>.
     *
     * @param virtualPath
     *    The virtual path of the HTTP server that links to this WAR file, cannot be <code>null</code>.
     *
     * @throws ServletException
     *    if the servlet cannot be initialized.
     */
    public void addWAR(File warFile, String virtualPath) throws ServletException {
       LocalServletHandler servlet = new LocalServletHandler(warFile);
       _servlets.put(virtualPath, servlet);
    }
 
    /**
     * Adds a new servlet.
     * The servlet with the virtual path "/" will be the default one.
     *
     * @param servletClassName
     *    The name of the servlet's class to load, cannot be <code>null</code>.
     *
     * @param virtualPath
     *    The virtual path of the HTTP server that links to this WAR file, cannot be <code>null</code>.
     *
     * @throws ServletException
     *    if the servlet cannot be initialized.
     */
    public void addServlet(String servletClassName, String virtualPath) throws ServletException{
       LocalServletHandler servlet = new LocalServletHandler(servletClassName);
       _servlets.put(virtualPath, servlet);
    }
 
    /**
     * Remove a servlet from the server.
     *
     * @param virtualPath
     *    The virtual path of the servlet to remove, cannot be <code>null</code>.
     */
    public void removeServlet(String virtualPath) {
       LocalServletHandler servlet = (LocalServletHandler) _servlets.get(virtualPath);
       servlet.close();
       _servlets.remove(virtualPath);
    }
 
    /**
     * Starts the web server.
     *
     * @param port
     *    the port of the servlet server.
     *
     * @param daemon
     *    <code>true</code> if the thread listening to connection should be a
     *    daemon thread, <code>false</code> otherwise.
     *
     * @throws IOException
     *    if the web server cannot be started.
     */
    public void startServer(int port, boolean daemon) throws IOException {
       // Create the server socket
       _serverSocket = new ServerSocket(port, 5);
       _running = true;
 
       _acceptor = new SocketAcceptor(daemon);
       _acceptor.start();
    }
 
    /**
     * Returns the port the server is accepting connections on.
     *
     * @return
     *    the server socket, e.g. <code>8080</code>.
     *
     * @throws IllegalStateException
     *    if the port cannot be determined, for example because the server is
     *    not started.
     *
     * @since XINS 1.5.0
     */
    public int getPort() throws IllegalStateException {
       int port;
       try {
          port = _serverSocket.getLocalPort();
       } catch (NullPointerException exception) {
          port = -1;
       }
 
       if (port < 0) {
          throw new IllegalStateException("Unable to determine port.");
       }
 
       return port;
    }
 
    /**
     * Disposes the servlet and stops the web server.
     */
    public void close() {
       _running = false;
       Iterator itServlets = _servlets.values().iterator();
       while (itServlets.hasNext()) {
          LocalServletHandler servlet = (LocalServletHandler) itServlets.next();
          servlet.close();
       }
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
     *    the connection with the client, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>client == null</code>.
     *
     * @throws IOException
     *    if the query is not handled correctly.
     */
    public void serviceClient(Socket client)
    throws IllegalArgumentException, IOException {
 
       // Check argument
       MandatoryArgumentChecker.check("client", client);
 
       BufferedReader       inbound  = null;
       BufferedOutputStream outbound = null;
       try {
          // Acquire the streams for IO
 /* TODO
          inbound  = new BufferedReader(new InputStreamReader(client.getInputStream()));
          outbound = new BufferedOutputStream(client.getOutputStream());
 
          httpQuery(inbound, outbound);
 */
          httpQuery(client.getInputStream(), client.getOutputStream());
 
       } finally{
 
          // Clean up
          if (inbound != null) {
             try {
                inbound.close();
             } catch (Throwable exception) {
                // ignore
             }
          }
 
          if (outbound != null) {
             try {
                outbound.close();
             } catch (Throwable exception) {
                // ignore
             }
          }
       }
    }
 
    /**
     * This method parses the data sent from the client to get the input
     * parameters and format the result as a compatible HTTP result.
     * This method will used the servlet associated with the passed virtual
     * path. If no servlet is associated with the virtual path, the servlet with
     * the virtual path "/" is used as default. If there is no servlet then with
     * the virtual path "/" is found then HTTP 404 is returned.
     *
     * @param in
     *    the input byte stream that contains the request sent by the client.
     *
     * @param out
     *    the output byte stream that must be fed the response towards the
     *    client.
     *
     * @throws IOException
     *    if the query is not handled correctly.
     */
    public void httpQuery(InputStream  in, OutputStream out)
    throws IOException {
 
       // Read the input
       // XXX: Buffer size determines maximum request size
       byte[] buffer = new byte[16384];
       int length = in.read(buffer);
       String request = new String(buffer, 0, length, REQUEST_ENCODING);
 
       // Read the first line
       int eolIndex = request.indexOf(CRLF);
       if (eolIndex < 0) {
          sendBadRequest(out);
          return;
       }
 
      // The first line must end with "HTTP/1.1"
       String line = request.substring(0, eolIndex);
       request = request.substring(eolIndex + 2);
      if (! line.endsWith(" HTTP/1.1")) {
          sendBadRequest(out);
          return;
       }
 
       // Cut off the last part
       line = line.substring(0, line.length() - 9);
 
       // Find the space
       int spaceIndex = line.indexOf(' ');
       if (spaceIndex < 1) {
          sendBadRequest(out);
          return;
       }
 
       // Determine the method
       String method = line.substring(0, spaceIndex);
 
       // Determine the query string
       String url = line.substring(spaceIndex + 1);
       if (url == null || "".equals(url)) {
          sendBadRequest(out);
          return;
       } else if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
          url = url.replace(',', '&');
       }
 
       // Normalize the query string
       if (url.endsWith("/") && getClass().getResource(url + "index.html") != null) {
          url += "index.html";
       }
 
       // Read the headers
       HashMap inHeaders = new HashMap();
       boolean done = false;
       while (! done) {
          int nextEOL = request.indexOf(CRLF);
          if (nextEOL <= 0) {
             done = true;
          } else {
             try {
                parseHeader(inHeaders, request.substring(0, nextEOL));
             } catch (ParseException exception) {
                sendBadRequest(out);
                return;
             }
             request = request.substring(nextEOL + 2);
          }
       }
 
       // Determine the body contents
       String body = "".equals(request)
                   ? ""
                   : request.substring(2);
 
 
       // Response encoding defaults to request encoding
       String responseEncoding = REQUEST_ENCODING;
 
       // Handle the case that a web page is requested
       boolean getMethod = method.equals("GET") || method.equals("HEAD");
       String httpResult;
       if (getMethod && url.indexOf('?') == -1 && !url.endsWith("/") && !"*".equals(url)) {
          httpResult = readWebPage(url);
 
       // No web page requested
       } else {
 
          // Determine the content type
          String inContentType = getHeader(inHeaders, "Content-Type");
 
          // If www-form encoded, then append the body to the query string
          if ((inContentType == null || inContentType.startsWith("application/x-www-form-urlencoded")) && body != null && body.length() > 0) {
             // XXX: What if the URL already contains a question mark?
             url += '?' + body;
             body = null;
          }
 
          // Locate the path of the URL
          String virtualPath = url;
          if (virtualPath.indexOf('?') != -1) {
             virtualPath = virtualPath.substring(0, url.indexOf('?'));
          }
          if (virtualPath.endsWith("/") && virtualPath.length() > 1) {
             virtualPath = virtualPath.substring(0, virtualPath.length() - 1);
          }
 
          // Get the Servlet according to the path
          LocalServletHandler servlet = (LocalServletHandler) _servlets.get(virtualPath);
 
          // If not found the root Servlet is used
          if (servlet == null) {
             servlet = (LocalServletHandler) _servlets.get("/");
          }
 
          // If no servlet is found return 404
          if (servlet == null) {
             sendError(out, "404 Not Found");
             return;
          } else {
 
             // Query the Servlet
             XINSServletResponse response = servlet.query(method, url, body, inHeaders);
 
             // Create the HTTP answer
             httpResult = "HTTP/1.1 " + response.getStatus() + " " + HttpStatus.getStatusText(response.getStatus()) + CRLF;
             PropertyReader outHeaders = response.getHeaders();
             Iterator itHeaderNames = outHeaders.getNames();
             while (itHeaderNames.hasNext()) {
                String nextHeader = (String) itHeaderNames.next();
                String headerValue = outHeaders.get(nextHeader);
                if (headerValue != null) {
                   httpResult += nextHeader + ": " + headerValue + "\r\n";
                }
             }
 
             String result = response.getResult();
             if (result != null) {
                responseEncoding = response.getCharacterEncoding();
               length = result.getBytes(responseEncoding).length + 1;
                httpResult += "Content-Length: " + length + "\r\n";
                httpResult += "Connection: close\r\n";
                httpResult += "\r\n";
               httpResult += result + "\n";
             }
            httpResult += "\n";
          }
       }
 
       byte[] bytes = httpResult.getBytes(responseEncoding);
       out.write(bytes, 0, bytes.length);
       out.flush();
    }
 
    private void sendError(OutputStream out, String status)
    throws IOException {
       String httpResult = "HTTP/1.1 " + status + CRLF + CRLF;
       byte[] bytes = httpResult.getBytes(REQUEST_ENCODING);
       out.write(bytes, 0, bytes.length);
       out.flush();
    }
 
    private void sendBadRequest(OutputStream out)
    throws IOException {
       sendError(out, "400 Bad Request");
    }
 
    private static void parseHeader(HashMap headers, String header)
    throws ParseException{
       int index = header.indexOf(':');
       if (index < 1) {
          throw new ParseException();
       }
 
       // Get key and value
       String key   = header.substring(0, index);
       String value = header.substring(index + 1);
 
       // Always convert the key to upper case
       key = key.toUpperCase();
 
       // Always trim the value
       value = value.trim();
 
       // XXX: Only one header supported
       if (headers.get(key) != null) {
          throw new ParseException();
       }
 
       // Store the key-value combo
       headers.put(key, value);
    }
 
    String getHeader(HashMap headers, String key) {
       return (String) headers.get(key.toUpperCase());
    }
 
    /**
     * Reads the content of a web page.
     *
     * @param url
     *    the location of the content, cannot be <code>null</code>.
     *
     * @return
     *    the HTTP response to return, never <code>null</code>.
     *
     * @throws IOException
     *    if an error occcurs when reading the URL.
     */
    private String readWebPage(String url) throws IOException {
       String httpResult = null;
       if (getClass().getResource(url) != null) {
          InputStream urlInputStream = getClass().getResourceAsStream(url);
          ByteArrayOutputStream contentOutputStream = new ByteArrayOutputStream();
          byte[] buf = new byte[8192];
          int len;
          while ((len = urlInputStream.read(buf)) > 0) {
             contentOutputStream.write(buf, 0, len);
          }
          contentOutputStream.close();
          urlInputStream.close();
          String content = contentOutputStream.toString("ISO-8859-1");
 
          httpResult = "HTTP/1.1 200 OK\r\n";
          String fileName = url.substring(url.lastIndexOf('/') + 1);
          httpResult += "Content-Type: " + MIME_TYPES_MAP.getContentTypeFor(fileName) + "\r\n";
         int length = content.getBytes("ISO-8859-1").length + 1;
          httpResult += "Content-Length: " + length + "\r\n";
          httpResult += "Connection: close\r\n";
          httpResult += "\r\n";
         httpResult += content + "\n";
         httpResult += "\n";
       } else {
          httpResult = "HTTP/1.1 404 Not Found\r\n";
       }
       return httpResult;
    }
 
    /**
     * Thread waiting for connection from the client.
     */
    private class SocketAcceptor extends Thread {
 
       /**
        * Create the thread.
        *
        * @param daemon
        *    <code>true</code> if the server should be a daemon thread,$
        *    <code>false</code> otherwise.
        */
       public SocketAcceptor(boolean daemon) {
          setDaemon(daemon);
          setName("XINS " + Library.getVersion() + " Servlet container.");
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
                   // If anything goes wrong still continue accepting clients
                   Utils.logIgnoredException("SocketAcceptor", "serviceClient", "SocketAcceptor", "run", ex);
                } finally {
                   try {
                      clientSocket.close();
                   } catch (Throwable exception) {
                      // ignore
                   }
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
