 /*
  * simple, multi-threaded HTTP server
  */
 
 package simpleWebServer;
  
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 import static simpleWebServer.HttpConstants.*;
 import simpleWebServer.FileExtensionToContentTypeMapper;
 
 
 class Runner {
     public static void main(String[] args) throws Exception {
         int port = 8080;
         if (args.length > 0) {
             port = Integer.parseInt(args[0]);
         }
 
         ConfigDefaults defaults = new ConfigDefaults();
         Logger logger = new SimpleLogger();
         Config config = new Config(defaults, logger);
         config.load();
         config.list();
 
         WebServer webServer = new WebServer(config);
         webServer.start();
     }
 }
 
 
 class WebServer {
 
     Config settings = null;
 
     /* Where worker threads stand idle */
     static Vector threads = new Vector();
 
     /* the web server's virtual root */
     static File root;
 
     /* timeout on client connections */
     static int timeout = 0;
 
     /* max # worker threads */
     static int workers = 5;
 
 
     public WebServer(Config config) {
         this.settings = config;
     }
 
     public void start() throws Exception {
 
         /* start worker threads */
         for (int i = 0; i < workers; ++i) {
             Worker w = new Worker(settings);
             (new Thread(w, "worker #"+i)).start();
             threads.addElement(w);
         }
 
         ServerSocket serverSocket = new ServerSocket(settings.port);
         while (true) {
 
             Socket s = serverSocket.accept();
 
             Worker w = null;
             synchronized (threads) {
                 if (threads.isEmpty()) {
                     Worker ws = new Worker(settings);
                     ws.setSocket(s);
                     (new Thread(ws, "additional worker")).start();
                 } else {
                     w = (Worker) threads.elementAt(0);
                     threads.removeElementAt(0);
                     w.setSocket(s);
                 }
             }
         }
     }
 
     public void stop() {
         return;
     }
 }
 
 
 class Worker extends WebServer implements Runnable {
 
     final static int BUF_SIZE = 2048;
     static final byte[] EOL = {(byte)'\r', (byte)'\n' };
 
     /* buffer to use for requests */
     byte[] buf;
     int index;
     int nread;
     /* Socket to client we're handling */
     private Socket s;
 
 
    Worker(Config config) {
         buf = new byte[BUF_SIZE];
         s = null;
     }
 
     synchronized void setSocket(Socket s) {
         this.s = s;
         notify();
     }
 
     public synchronized void run() {
         while(true) {
             if (s == null) {
                 /* nothing to do */
                 try {
                     wait();
                 } catch (InterruptedException e) {
                     /* should not happen */
                     continue;
                 }
             }
 
             try {
                 handleClient();
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             /* go back in wait queue if there's fewer
              * than numHandler connections.
              */
             s = null;
             Vector pool = WebServer.threads;
             synchronized (pool) {
                 if (pool.size() >= WebServer.workers) {
                     /* too many threads, exit this one */
                     return;
                 } else {
                     pool.addElement(this);
                 }
             }
         }
     }
 
     void handleClient() throws IOException {
 
         /* we will only block in read for this many milliseconds
          * before we fail with java.io.InterruptedIOException,
          * at which point we will abandon the connection.
          */
         s.setSoTimeout(WebServer.timeout);
         s.setTcpNoDelay(true);
         String hostAddress = s.getInetAddress().getHostAddress();
 
         resetBuffer();
 
         try {
             /* only support for HTTP GET/HEAD
              * HTTP options are ignored
              */
             InputStream is = new BufferedInputStream(s.getInputStream());
             int nread = 0, r = 0;
 
 outerloop:
             while (nread < BUF_SIZE) {
                 r = is.read(buf, nread, BUF_SIZE - nread);
                 if (r == -1) {
                     /* EOF */
                     return;
                 }
                 int i = nread;
                 nread += r;
                 for (; i < nread; i++) {
                     if (buf[i] == (byte)'\n' || buf[i] == (byte)'\r') {
                         /* read one line */
                         break outerloop;
                     }
                 }
             }
 
             PrintStream ps = new PrintStream(s.getOutputStream());
             index = 0;
             boolean doingGet = extractMethod(buf, ps);
             String fname = extractFilename(buf);
             File targ = openFile(fname);
 
             StaticContentReverse proxy = new StaticContentReverse(settings.logger);
             proxy.deliverContent(doingGet, targ, ps, hostAddress);
 
         } finally {
             s.close();
         }
 
     }
 
     protected void resetBuffer() {
         /* zero out the buffer from last time */
         for (int i = 0; i < BUF_SIZE; i++) {
             buf[i] = 0;
         }
     }
 
     protected boolean extractMethod(byte[] buf, PrintStream ps) {
         /* are we doing a GET or just a HEAD */
         boolean doingGet = false;
         /* beginning of file name */
         if (buf[0] == (byte)'G' &&
             buf[1] == (byte)'E' &&
             buf[2] == (byte)'T' &&
             buf[3] == (byte)' ') {
             doingGet = true;
             index = 4;
 
         } else if (buf[0] == (byte)'H' &&
                    buf[1] == (byte)'E' &&
                    buf[2] == (byte)'A' &&
                    buf[3] == (byte)'D' &&
                    buf[4] == (byte)' ') {
             doingGet = false;
             index = 5;
 
         } else {
             /* we don't support this method */
             ps.print("HTTP/1.0 " + HTTP_BAD_METHOD +
                        " unsupported method type: ");
             ps.write(buf, 0, 5);
             ps.write(EOL);
             ps.flush();
             s.close();
         }
         return doingGet;
     }
 
     protected String extractFilename(byte[] buf) {
         /* find the file name, from:
          * GET /foo/bar.html HTTP/1.0
          * extract "/foo/bar.html"
          */
         int fnameBegin = index;
         int fnameLen = 0;
         for (int i = index; i < nread; i++) {
             if (buf[i] == (byte)' ') {
                 fnameLen = i - fnameBegin;
                 break;
             }
         }
 
         String fname = (new String(buf, 0, fnameBegin,
                   fnameLen)).replace('/', File.separatorChar);
         if (fname.startsWith(File.separator)) {
             fname = fname.substring(1);
         }
 
         return fname;
     }
 
     protected File openFile(String fname) {
         File targ = new File(WebServer.root, fname);
         if (targ.isDirectory()) {
             File ind = new File(targ, "index.html");
             if (ind.exists()) {
                 targ = ind;
             }
         }
     }
 }
 
 
 
 class StaticContentReverse {
 
     Logger logger = null;
 
     public StaticContentReverse(Logger logger) {
         this.logger = logger;
     }
 
     void deliverContent(boolean doingGet, File targ, PrintStream ps, String hostAddress) {
         boolean OK = printHeaders(targ, ps, hostAddress);
         if (doingGet) {
             if (OK) {
                 sendFile(targ, ps);
             } else {
                 send404(targ, ps);
             }
         }
     }
 
     boolean printHeaders(File targ, PrintStream ps, String hostAddress)
         throws IOException
     {
         boolean ret = false;
         int rCode = 0;
 
         if (!targ.exists()) {
             rCode = HTTP_NOT_FOUND;
             ps.print("HTTP/1.0 " + HTTP_NOT_FOUND + " not found");
             ps.write(EOL);
             ret = false;
         }  else {
             rCode = HTTP_OK;
             ps.print("HTTP/1.0 " + HTTP_OK+" OK");
             ps.write(EOL);
             ret = true;
         }
         logger.log("From " + hostAddress + ": GET " +
             targ.getAbsolutePath() + "-->" + rCode);
 
         ps.print("Server: Simple java");
         ps.write(EOL);
         ps.print("Date: " + (new Date()));
         ps.write(EOL);
         if (ret) {
             if (!targ.isDirectory()) {
                 ps.print("Content-length: "+targ.length());
                 ps.write(EOL);
                 ps.print("Last Modified: " + (new
                               Date(targ.lastModified())));
                 ps.write(EOL);
                 String name = targ.getName();
                 int ind = name.lastIndexOf('.');
                 String ct = null;
                 if (ind > 0) {
                    ct = (String) extensionToContent.get(name.substring(ind));
                 }
                 if (ct == null) {
                     ct = "unknown/unknown";
                 }
                 ps.print("Content-type: " + ct);
                 ps.write(EOL);
             } else {
                 ps.print("Content-type: text/html");
                 ps.write(EOL);
             }
         }
         return ret;
     }
 
     void send404(File targ, PrintStream ps) throws IOException {
         ps.write(EOL);
         ps.write(EOL);
         ps.println("Not Found\n\n"+
                    "The requested resource was not found.\n");
     }
 
     void sendFile(File targ, PrintStream ps) throws IOException {
         InputStream is = null;
         ps.write(EOL);
         if (targ.isDirectory()) {
             listDirectory(targ, ps);
             return;
         } else {
             is = new FileInputStream(targ.getAbsolutePath());
         }
 
         byte[] buf = new byte[BUF_SIZE];
         try {
             int n;
             while ((n = is.read(buf)) > 0) {
                 ps.write(buf, 0, n);
             }
         } finally {
             is.close();
         }
     }
 
     void listDirectory(File dir, PrintStream ps) throws IOException {
         ps.println("<TITLE>Directory listing</TITLE><P>\n");
         ps.println("<A HREF=\"..\">Parent Directory</A><BR>\n");
         String[] list = dir.list();
         for (int i = 0; list != null && i < list.length; i++) {
             File f = new File(dir, list[i]);
             if (f.isDirectory()) {
                 ps.println("<A HREF=\""+list[i]+"/\">"+list[i]+"/</A><BR>");
             } else {
                 ps.println("<A HREF=\""+list[i]+"\">"+list[i]+"</A><BR");
             }
         }
         ps.println("<P><HR><BR><I>" + (new Date()) + "</I>");
     }
 }
 
