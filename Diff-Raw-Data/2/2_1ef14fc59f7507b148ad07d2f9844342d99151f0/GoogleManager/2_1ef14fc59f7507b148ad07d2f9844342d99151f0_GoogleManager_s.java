 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package capstone.server;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author luke
  */
 public class GoogleManager extends HttpServlet {
 
     /**
      * Processes requests for both HTTP
      * <code>GET</code> and
      * <code>POST</code> methods.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         if(request.getParameter("form").equals("delink")) {
             String userName = request.getParameter("userName");
             if(databaseAccess.removeGID(userName)) {
                 this.getServletContext().getRequestDispatcher("/accountManagement.jsp").forward(request, response);
             } else {
                String message = "delinkerror";
                 this.getServletContext().getRequestDispatcher("/accountManagement.jsp?error="+message).forward(request, response);
             }
         } else if (request.getParameter("form").equals("link")) {
             String userName = request.getParameter("userName");
             String gid = request.getParameter("gid");
             String gName = request.getParameter("gName");
             String gLink = request.getParameter("gLink");
             if(databaseAccess.addGID(userName, gid, gName, gLink)) {
                 this.getServletContext().getRequestDispatcher("/accountManagement.jsp").forward(request, response);
             } else {
                 String message = "gLinkError";
                 this.getServletContext().getRequestDispatcher("/accountManagement.jsp?error="+message).forward(request,response);
             }
             
             
             
         } else if(request.getParameter("form").equals("update")) {
             //add google details to existing account
             String userName = request.getParameter("userName");
             String password = request.getParameter("password");
             String gid = request.getParameter("fbid");
             String referer = request.getParameter("referer");
             String gName = request.getParameter("gName");
             String link = request.getParameter("link");
             String email = request.getParameter("email");
             if(userName.equals(databaseAccess.checkLoginCredentials(userName, password))) {
                 if(databaseAccess.addGID(userName, gid, gName, link)) {
                     GameManager.newPlayer(request.getSession(), userName);
                     this.getServletContext().getRequestDispatcher("/lobby.jsp").forward(request, response);
                 } else {
                     String message = "exception1";
                     this.getServletContext().getRequestDispatcher("/googleLogin.jsp?error="+message+"&gid="+gid+"&name="+userName+"&gemail="+email+"&gName="+gName+"&link="+link+"&referer="+referer).forward(request, response);
                 }
             } else {
                 String message = "login";
                 this.getServletContext().getRequestDispatcher("/googleLogin.jsp?error="+message+"&gid="+gid+"&name="+userName+"&gemail="+email+"&gName="+gName+"&link="+link+"&referer="+referer).forward(request, response);
             }
         } else {
             //create new account linked to google id
             String userName = request.getParameter("userName");
             String email = request.getParameter("email");
             String password = request.getParameter("password");
             String gid = request.getParameter("gid");
             String gName = request.getParameter("gName");
             String link = request.getParameter("link");
     
             Map details = databaseAccess.getPlayerDetails(userName);
             if(details.isEmpty()) {
                 details = new HashMap();
                 details.put("userName", userName);
                 details.put("email", email);
                 details.put("password", password);
                 details.put("gid", gid);
                 details.put("gName", gName);
                 details.put("gLink", link);
                 if(databaseAccess.addPlayer(details)) {
                     GameManager.newPlayer(request.getSession(), userName);
                     this.getServletContext().getRequestDispatcher("/lobby.jsp").forward(request, response);
                 } else {
                     String message = "exception";
                     this.getServletContext().getRequestDispatcher("/googleLogin.jsp?error="+message+"&gid="+gid+"&name="+userName+"&gemail="+email+"&gName="+gName+"&link="+link+"&referer=both").forward(request, response);
                 }
             } else {
                 if(details.get("userName").equals(userName)) {
                     String message = "userName";
                     this.getServletContext().getRequestDispatcher("/googleLogin.jsp?error="+message+"&gid="+gid+"&name="+userName+"&gemail="+email+"&gName="+gName+"&link="+link+"&referer=both").forward(request, response);
                 } else if(details.get("email").equals(email)) {
                     String message = "email";
                     this.getServletContext().getRequestDispatcher("/googleLogin.jsp?error="+message+"&gid="+gid+"&name="+userName+"&gemail="+email+"&gName="+gName+"&link="+link+"&referer=both").forward(request, response);
                 }
             }
             
         }
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
