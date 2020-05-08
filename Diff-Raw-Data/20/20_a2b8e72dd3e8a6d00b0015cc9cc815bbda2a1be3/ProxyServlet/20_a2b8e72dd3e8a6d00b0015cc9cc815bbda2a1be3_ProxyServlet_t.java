 package com.gvls2downloader.gvls2proxy;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.PoolingClientConnectionManager;
 
 /**
  *
  */
 @WebServlet(name = "ProxyServlet", urlPatterns = {"/ProxyServlet"})
 public class ProxyServlet extends HttpServlet {
 
     private boolean requestIsRunning = false;
     
     private static final String KEY_IP = "ip";
     private static final String KEY_USER = "user";
     private static final String KEY_PASSWORD = "password";
 
     protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
         if (requestIsRunning) {
             resp.setContentType("text/plain");
             resp.sendError(503);
             resp.getWriter().write("Request already in progress!");
            return;
         }
         
         requestIsRunning = true;
         try {
             String ipParam = req.getParameter(KEY_IP);
             if (ipParam == null || ipParam.trim().equals("")) {
                 ipParam = "192.168.8.156";
             }
             final String ip = ipParam;
 
             String user = req.getParameter(KEY_USER);
             String pw = req.getParameter(KEY_PASSWORD);
 
             if (user == null || user.trim().equals("")) {
                 user = "camuser";
             }
             if (pw == null || pw.trim().equals("")) {
                 pw = "password";
             }
 
             resp.setContentType("video/mpeg");
             SchemeRegistry schemeRegistry = new SchemeRegistry();
             schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
             ClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
 
             final DefaultHttpClient httpClient = new DefaultHttpClient(cm);
             UsernamePasswordCredentials creds = new UsernamePasswordCredentials(user, pw);
             httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, creds);
 
             // 1. Authenticate
             HttpGet httpGet = new HttpGet("http://" + ip + "/php/session_start_user.php");
             //CredentialsProvider credsProvider = new BasicCredentialsProvider();
             //credsProvider.setCredentials(AuthScope.ANY, creds);
             HttpResponse response = httpClient.execute(httpGet);
             System.out.println("Headers from auth response:");
             printHeaders(response);
             InputStream is = response.getEntity().getContent();
             writeStreamToFile(is, "authentication.txt");
 
             // 2. Send Hello
             httpGet = new HttpGet("http://" + ip + "/cgi-bin/hello.cgi");
             response = httpClient.execute(httpGet);
             System.out.println("Headers from hello response:");
             printHeaders(response);
             is = response.getEntity().getContent();
             writeStreamToFile(is, "hello.txt");
 
             // 3. Send first command
             HttpPost httpPost = new HttpPost("http://" + ip + "/cgi-bin/cmd.cgi");
             StringEntity stringEntity = new StringEntity("\"Command\":\"SetCamCtrl\",\"Params\":{\"Ctrl\":\"ModeMonitor\"}}");
             httpPost.setEntity(null);
             response = httpClient.execute(httpPost);
             System.out.println("Headers from cmd response:");
             printHeaders(response);
             is = response.getEntity().getContent();
             writeStreamToFile(is, "cmd.txt");
 
             Thread newThread = new Thread() {
                 @Override
                 public void run() {
                     while (requestIsRunning) {
                         try {
                             HttpGet httpGet = new HttpGet("http://" + ip + "/php/session_continue.php");
                             HttpResponse response = httpClient.execute(httpGet);
                             System.out.println("Headers from session continuation:");
                             printHeaders(response);
                             InputStream is = response.getEntity().getContent();
                             writeStreamToFile(is, "session_continue.txt");
                         } catch (Throwable t) {
                             t.printStackTrace();
                         }
                         try {
                             Thread.sleep(10000);
                         } catch (Throwable t) {
                             t.printStackTrace();
                         }
                     }
                 }
             };
             newThread.start();
 
             // 4. Grab MPEG-TS
             // /cgi-bin/movie_sp.cgi
            try {
                httpGet = new HttpGet("http://" + ip + "/cgi-bin/movie_sp.cgi");
                response = httpClient.execute(httpGet);
                System.out.println("Headers from video stream response:");
                printHeaders(response);
                is = response.getEntity().getContent();
                writeStreamToOutput(is, resp.getOutputStream());
                System.out.println("MPEG-TS stream complete!");
            } catch (Exception e) {
                System.out.println("Error transferring stream... shutting down thread!");
            }
 
             // 5. Disconnect session
             httpGet = new HttpGet("http://" + ip + "/php/session_finish.php");
             response = httpClient.execute(httpGet);
             System.out.println("Headers from Session completion:");
             printHeaders(response);
             is = response.getEntity().getContent();
             writeStreamToFile(is, "session_complete.txt");
         } finally {
             requestIsRunning = false;
         }
     }
 
     private static void printHeaders(HttpResponse response) {
         for (Header header : response.getAllHeaders()) {
             System.out.println("Header: " + header.getName() + ": " + header.getValue());
         }
         System.out.println();
         System.out.println();
     }
 
     private static void writeStreamToFile(InputStream is, String filename) throws IOException {
         File file = new File(filename);
         System.out.println("Writing to: " + file.getAbsolutePath());
         FileOutputStream fos = new FileOutputStream(file);
         byte[] buffer = new byte[32768];
 
         int bytesRead;
         while ((bytesRead = is.read(buffer)) != -1) {
             fos.write(buffer, 0, bytesRead);
         }
         fos.close();
         is.close();
     }
 
     private void writeStreamToOutput(InputStream is, OutputStream os) throws IOException {
         byte[] buffer = new byte[32768];
 
         int bytesRead;
         while ((bytesRead = is.read(buffer)) != -1) {
             os.write(buffer, 0, bytesRead);
             os.flush();
             if (!requestIsRunning) {
                 break;
             }
         }
         os.close();
         is.close();
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /**
      * Handles the HTTP
      * <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 }
