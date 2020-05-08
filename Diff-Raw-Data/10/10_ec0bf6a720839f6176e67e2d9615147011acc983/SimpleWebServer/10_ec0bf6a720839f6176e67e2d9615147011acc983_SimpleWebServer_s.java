 /*
  * simple, multi-threaded HTTP server
  */
 
 package simpleWebServer;
  
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 import static simpleWebServer.HttpConstants.*;
 import simpleWebServer.FileExtensionToContentTypeMapper;
 
 
 class WorkerPool {
     Vector<Worker> workerPool = new Vector<Worker>();
     int maxWorkersInPool = 5;
 
    public WorkerPool() {
     }
     
     public void init() {
         for (int i = 0; i < settings.maxWorkersInPool; ++i) {
             Worker w = new Worker(workerPool, settings);
             (new Thread(w, "worker #"+i)).start();
             workerPool.addElement(w);
         }
     }
 
     public Worker hireWorker(Socket s) {
         Worker w = null;
         synchronized (workerPool) {
             if (workerPool.isEmpty()) {
                 w = new Worker(workerPool, settings);
                 w.youGotWorkWith(s);
                 (new Thread(w, "additional worker")).start();
             } else {
                 w = workerPool.elementAt(0);
                 workerPool.removeElementAt(0);
                 w.youGotWorkWith(s);
             }
         }
         return w;
     }
 
     public void giveBack(Worker worker) {
         synchronized (workerPool) {
            if (pool.size() < settings.maxWorkersInPool) {
                pool.addElement(worker);
             }
         }
     }
 }
 
 
 class WebServer {
 
     Config settings = null;
 
     Vector<Worker> workerPool = new Vector<Worker>();
     int maxWorkersInPool = 5;
 
     /* the web server's virtual root */
     File root;
 
     /* timeout on client connections */
     int timeout = 0;
 
 
     public WebServer(Config config) {
         this.settings = config;
     }
 
     public void start() throws Exception {
         initWorkerPool();
         ServerSocket serverSocket = new ServerSocket(settings.port);
         while (!isStopped()) {
             Socket serveThisSocket = serverSocket.accept();
             Worker w = hireWorkerFromPool(serveThisSocket);
         }
     }
 
     protected void initWorkerPool() {
         for (int i = 0; i < settings.maxWorkersInPool; ++i) {
             Worker w = new Worker(workerPool, settings);
             (new Thread(w, "worker #"+i)).start();
             workerPool.addElement(w);
         }
     }
 
     protected Worker hireWorkerFromPool(Socket s) {
         Worker w = null;
         synchronized (workerPool) {
             if (workerPool.isEmpty()) {
                 w = new Worker(workerPool, settings);
                 w.youGotWorkWith(s);
                 (new Thread(w, "additional worker")).start();
             } else {
                 w = workerPool.elementAt(0);
                 workerPool.removeElementAt(0);
                 w.youGotWorkWith(s);
             }
         }
         return w;
     }
 
     public void stop() {
         return;
     }
     protected boolean isStopped() {
         return true;
     }
 }
 
 
 class Worker implements Runnable {
 
     final static int BUF_SIZE = 2048;
     static final byte[] EOL = {(byte)'\r', (byte)'\n' };
 
     Config settings = null;
     Vector<Worker> workerPool = null;
 
     byte[] requestBuffer;
     int index;
     int nread;
     protected Socket currentClient = null;
 
 
     public Worker(Vector<Worker> coworkers, Config config) {
         this.workerPool = coworkers;
         this.settings = config;
         requestBuffer = new byte[BUF_SIZE];
     }
 
 
     public synchronized void youGotWorkWith(Socket newClient) {
         this.currentClient = newClient;
         notify();
     }
 
     protected boolean hasClient() {
         if (currentClient == null) {
             return false;
         } else {
             return true;
         }
     }
 
     protected void doneWithClient() {
         currentClient = null;
         Vector<Worker> pool = workerPool;
         synchronized (pool) {
             if (pool.size() < settings.maxWorkersInPool) {
                 pool.addElement(this);
             }
         }
     }
 
     public synchronized void run() {
         while(true) {
             if (!hasClient()) {
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
 
             doneWithClient();
         }
     }
 
 
     void handleClient() throws IOException {
 
         /* we will only block in read for this many milliseconds
          * before we fail with java.io.InterruptedIOException,
          * at which point we will abandon the connection.
          */
         currentClient.setSoTimeout(settings.timeout);
         currentClient.setTcpNoDelay(true);
         String hostAddress = currentClient.getInetAddress().getHostAddress();
 
         resetBuffer();
 
         try {
             /* only support for HTTP GET/HEAD
              * HTTP options are ignored
              */
             InputStream is = new BufferedInputStream(currentClient.getInputStream());
             int nread = 0, r = 0;
 
 outerloop:
             while (nread < BUF_SIZE) {
                 r = is.read(requestBuffer, nread, BUF_SIZE - nread);
                 if (r == -1) {
                     /* EOF */
                     return;
                 }
                 int i = nread;
                 nread += r;
                 for (; i < nread; i++) {
                     if (requestBuffer[i] == (byte)'\n' || requestBuffer[i] == (byte)'\r') {
                         /* read one line */
                         break outerloop;
                     }
                 }
             }
 
             PrintStream ps = new PrintStream(currentClient.getOutputStream());
             index = 0;
             boolean doingGet = extractMethod(requestBuffer, ps);
             String fname = extractFilename(requestBuffer);
             File targ = openFile(fname);
 
             StaticContentReverse proxy = new StaticContentReverse(settings.logger);
             proxy.deliverContent(doingGet, targ, ps, hostAddress);
 
         } finally {
             currentClient.close();
         }
     }
 
     protected void resetBuffer() {
         /* zero out the buffer from last time */
         for (int i = 0; i < BUF_SIZE; i++) {
             requestBuffer[i] = 0;
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
             try {
                 ps.print("HTTP/1.0 " + HTTP_BAD_METHOD +
                            " unsupported method type: ");
                 ps.write(buf, 0, 5);
                 ps.write(EOL);
                 ps.flush();
                 currentClient.close();
             } catch (IOException e) {}
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
         File targ = new File(settings.root, fname);
         if (targ.isDirectory()) {
             File ind = new File(targ, "index.html");
             if (ind.exists()) {
                 targ = ind;
             }
         }
         return targ;
     }
 }
 
 
 
 class StaticContentReverse {
 
     static final int BUF_SIZE = 2048;
     static final byte[] EOL = {(byte)'\r', (byte)'\n' };
 
     Logger logger = null;
     FileExtensionToContentTypeMapper mapper = new FileExtensionToContentTypeMapper();
 
     public StaticContentReverse(Logger logger) {
         this.logger = logger;
     }
 
     void deliverContent(boolean doingGet, File targ, PrintStream ps, String hostAddress) {
         try {
         boolean OK = printHeaders(targ, ps, hostAddress);
         if (doingGet) {
             if (OK) {
                 sendFile(targ, ps);
             } else {
                 send404(targ, ps);
             }
         }
         } catch (IOException e) {}
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
                     ct = mapper.extensionsToContent.get(name.substring(ind));
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
 
