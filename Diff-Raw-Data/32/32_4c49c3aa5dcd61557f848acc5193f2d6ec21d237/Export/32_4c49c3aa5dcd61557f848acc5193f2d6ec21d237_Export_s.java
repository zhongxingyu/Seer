 package gov.usgs.cida.communication;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URLDecoder;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.StringUtils;
 
 /**
  *
  * @author isuftin
  */
 public class Export extends HttpServlet {
 
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 
         String filename = request.getParameter("filename");
         String data =  URLDecoder.decode(request.getParameter("data"), "UTF-8");
 
         if (StringUtils.isBlank(filename) || StringUtils.isBlank(data)) {
             response.sendError(500, "Either 'filename' or 'data' elements were empty.");
             return;
         }
         
        ServletOutputStream out = response.getOutputStream();
        InputStream in = null;
         
         try {
             response.setContentType("application/octet-stream");
             response.setHeader("Content-Disposition", "attachment;filename=" + filename);
             response.setHeader("Pragma", "no-cache");
             response.setHeader("Expires", "0");


            StringBuilder sb = new StringBuilder(data);
            in = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));

             byte[] outputByte = new byte[4096];
            //copy binary contect to output stream
             while (in.read(outputByte, 0, 4096) != -1) {
                 out.write(outputByte, 0, 4096);
             }
        } finally {
            in.close();
             out.flush();
             out.close();
         }
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /** 
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Exports posted data via a file back to the client";
     }// </editor-fold>
 }
