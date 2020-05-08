 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package capstone.server;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author lowkeylukey
  */
 public class FriendManager extends HttpServlet {
 
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
         if(request.getParameter("form").equals("add")) {
             String player = request.getParameter("player");
             String friend = request.getParameter("friend");
             //check to see if friend exists
             if(!databaseAccess.playerExists(friend))
             {
                String message = "There is no player with the user name" + friend;
                 this.getServletContext().getRequestDispatcher("/accountManagement.jsp?requestmessage="+message).forward(request, response);
                 return;
             }
             if(player == friend)
             {
                 String message = "Cannot add yourself as a friend";
                 this.getServletContext().getRequestDispatcher("/accountManagement.jsp?requestmessage="+message).forward(request, response);
                 return;
             }
             //check to see if already friends
             if(databaseAccess.areFriends(player, friend)) {
                 String message = "You are already friends";
                 this.getServletContext().getRequestDispatcher("/accountManagement.jsp?requestmessage="+message).forward(request, response);
                 return;
             }
             if(databaseAccess.addFriend(player, friend)) {
                 String message = "Friend request sent";
                 this.getServletContext().getRequestDispatcher("/accountManagement.jsp?requestmessage="+message).forward(request, response);
             } else {
                 String message = "Friend request could not be sent";
                 this.getServletContext().getRequestDispatcher("/accountManagement.jsp?requestmessage="+message).forward(request, response);
             }
         } else if (request.getParameter("form").equals("requests")) {
             if(request.getParameter("submit").equals("Accept Request")) {
                 String player = request.getParameter("player");
                 String friend = request.getParameter("friendRequestsField");
                 if(databaseAccess.acceptFriend(player, friend)) {
                     String message = "Friend accepted";
                     this.getServletContext().getRequestDispatcher("/accountManagement.jsp?requestmessage="+message).forward(request, response);
                 } else {
                     String message = "Error: Please try again";
                     this.getServletContext().getRequestDispatcher("/accountManagement.jsp?requestmessage="+message).forward(request, response);
                 }
             } else if (request.getParameter("submit").equals("Decline Request")) {
                 String player = request.getParameter("player");
                 String friend = request.getParameter("friendRequestsField");
                 if(databaseAccess.declineRequest(player, friend)) {
                     this.getServletContext().getRequestDispatcher("/accountManagement.jsp").forward(request, response);
                 } else {
                     String message = "Error: Please try again";
                     this.getServletContext().getRequestDispatcher("/accountManagement.jsp?requestmessage="+message).forward(request, response);
                 }
             }
         } else if (request.getParameter("form").equals("friends")) {
             String player = request.getParameter("player");
             String friend = request.getParameter("friendsField");
             if(databaseAccess.removeFriend(player, friend)) {
                 this.getServletContext().getRequestDispatcher("/accountManagement.jsp").forward(request, response);
             } else {
                 String message = "Error: Please try again";
                 this.getServletContext().getRequestDispatcher("/accountManagement.jsp?requestmessage="+message).forward(request, response);
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
