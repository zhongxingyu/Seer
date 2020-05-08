 /*
  * jason.java
  *
  * Created on March 22, 2006, 1:23 PM
  */
 package org.vle.aid.client;
 
 import java.io.*;
 import java.util.Map;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.servlet.*;
 import javax.servlet.http.*;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
 import org.vle.aid.lucene.tools.Thumbnails;
 /**
  *
  * @author emeij
  * @version
  */
 public class item extends HttpServlet {
   
   
   /** logger for Commons logging. */
   private static Logger log = Logger.getLogger("item.class.getName()");    
 
  private static Tika tika = new Tika();

   /**
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
   @SuppressWarnings("unchecked")
   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException {
     
     final String indexdir = System.getenv("INDEXDIR") + File.separator;
     Map    param    = request.getParameterMap();
     
     boolean thumbnail = false;
     if (param.containsKey("thumbnail"))
       thumbnail = true;
     
     String index    = ((String[]) param.get("index"))[0];
     String file = "";
     if (param.containsKey("file")) {
       file = ((String[]) param.get("file"))[0];
     } else {
       file = java.net.URLDecoder.decode(
               request.getRequestURI().replaceAll(request.getServletPath(), "").replaceAll(request.getContextPath(), "").replaceFirst("/", ""), "UTF-8");
     }
     if (index.equalsIgnoreCase("medline") || index.equalsIgnoreCase("medline_new")) {
       response.sendRedirect(
               response.encodeRedirectURL(
               "http://www.ncbi.nlm.nih.gov/sites/entrez?Db=pubmed&Cmd=ShowDetailView&TermToSearch=" + file));
 
       return;
     }
     
     if (index.equalsIgnoreCase("my experiment")) {
       Pattern p = Pattern.compile("\\(\\d*\\)");
       Matcher m = p.matcher(file);
       String id = "";
       if (m.find()) {
         id = m.group();
         id = id.substring(1, id.length()-1);
       }
       if (id.length() > 0) { 
         response.sendRedirect(
               response.encodeRedirectURL(
               "http://www.myexperiment.org/workflows/" + id));
       } else {  
         return;
       }
     }
 
     File _file = new File(indexdir + File.separator + index + File.separator + "cache" + File.separator + file);
 
     if (!_file.exists()) {
       displayError(response, "File '" + _file + " 'not found.");
       return;
     }

	  MediaType fileType = MediaType.parse(tika.detect(_file));
	  response.setContentType(fileType.toString());
     
     if (thumbnail) {
      if (fileType.compareTo(MediaType.application("pdf")) == 0) {
       
         response.setContentType("image/jpeg");
         
         if (!new File(_file.getAbsolutePath() + ".jpg").exists())
           Thumbnails.createthumbnail(_file);
         
         _file = new File(_file.getAbsolutePath() + ".jpg");
         
       } else {
         return;
       }
	}	
 
     // Uncomment this for an attachment
     // response.setHeader("Content-disposition","attachment; filename=" + file);
     BufferedOutputStream bos = null;
     BufferedInputStream bis = null;
 
     try {
       bis = new BufferedInputStream(new FileInputStream(_file));
 
       // PrintWriter out = response.getWriter();
       ServletOutputStream out = response.getOutputStream();
 
       bos = new BufferedOutputStream(out);
 
       byte[] buff = new byte[2048];
       int bytesRead;
 
       // Simple read/write loop.
       while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
         bos.write(buff, 0, bytesRead);
       }
     } catch (final IOException e) {
       log.severe("IOException! " + e.toString());
       throw e;
     } finally {
       if (bis != null) {
         bis.close();
       }
 
       if (bos != null) {
         bos.close();
       }
     }
   }
   
   private void displayError(HttpServletResponse response, String msg) throws IOException {
     response.setContentType("text/plain");
     PrintWriter out = response.getWriter();
     out.println(msg);
     out.close();
   }
   
  
   
   
   // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
   /**
    * Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    */
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
     processRequest(request, response);
   }
 
   /**
    * Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
     processRequest(request, response);
   }
 }
 
 
 //~ Formatted by Jindent --- http://www.jindent.com
