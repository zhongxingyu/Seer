 package net.himavat.koma.restmock;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 /**
  * Servlet mocking the REST service.
  * @author koma
  */
 public class RestServlet extends HttpServlet {
 
     public static String PARAM_JSON_FOLDER = "JSON_FOLDER";
 
     private String jsonFolder = "";
 
     @Override
     public void init(ServletConfig config) {
         jsonFolder = (String) config.getServletContext().getAttribute(PARAM_JSON_FOLDER);
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         sendFile(request, response);
     }
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         sendFile(request, response);
     }
 
     @Override
     protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         sendFile(request, response);
     }
 
     @Override
     protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         sendFile(request, response);
     }
 
     /**
      * Sends the file to response
      * @param request request
      * @param response response
      * @throws IOException
      */
     private void sendFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
 
         File jsonFile = new File(jsonFolder + request.getPathInfo() + File.separator + request.getMethod() + ".json");
 
         if (! jsonFile.exists()) {
             // response file not found
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
         }
 
         response.setContentType("application/json");
         response.setContentLength((int) jsonFile.length());
 
         // copy file to response
         FileInputStream in = new FileInputStream(jsonFile);
         OutputStream out = response.getOutputStream();
         byte[] buf = new byte[1024];
         int count = 0;
         while ((count = in.read(buf)) >= 0) {
             out.write(buf, 0, count);
         }
         in.close();
         out.close();
     }
 
 }
