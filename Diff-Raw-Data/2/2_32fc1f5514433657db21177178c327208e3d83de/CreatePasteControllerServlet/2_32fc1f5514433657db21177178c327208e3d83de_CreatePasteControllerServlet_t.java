 package org.ow2.jonas.azure.pastebean.control;
 
 import java.io.IOException;
 
 import javax.ejb.EJB;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.ow2.jonas.azure.pastebean.model.Paste;
 import org.ow2.jonas.azure.pastebean.service.PasteService;
 
 /**
  * A {@code ${NAME}} is ...
  *
  * @author Guillaume Sauthier
  */
 public class CreatePasteControllerServlet extends HttpServlet {
     
     @EJB
     private PasteService pasteService;
     
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         execute(request, response);
     }
 
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         execute(request, response);
     }
 
     private void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         
         String author = request.getParameter("author");
        String description = request.getParameter("desc");
         String content = request.getParameter("content");
         
         Paste paste = pasteService.createPaste(author, description, content);
 
         response.sendRedirect(getUrl(paste));
     }
 
     private String getUrl(Paste paste) {
         return paste.getHash().substring(0, 8);
     }
 }
