 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.ac.dundee.computing.fordyce.nwj.mrblabby;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import uk.ac.dundee.computing.fordyce.nwj.mrblabby.bean.MessageList;
 import uk.ac.dundee.computing.fordyce.nwj.mrblabby.bean.User;
 import uk.ac.dundee.computing.fordyce.nwj.mrblabby.dataservice.MessageService;
 
 /**
  *
  * @author Neil
  */
 @WebServlet(urlPatterns = {"/message", "/message/*"})
 public class MessageServlet extends HttpServlet {
 
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
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         //System.out.println(request.getPathTranslated());
         //System.out.println(request.getPathInfo().startsWith("/create"));
 
         if ((User) request.getSession().getAttribute("user") == null) {
             response.sendRedirect("/MrBlabby/login");
         } else {
 
             int startIndex = 0;
             int maxMessages = 10;
             String messageListIndex = request.getParameter("messageListIndex");
             if(messageListIndex != null){
                 startIndex = Integer.parseInt(messageListIndex);
             }
             
             MessageList messageList = new MessageList((User) request.getSession().getAttribute("user"), startIndex, maxMessages);
             
             request.setAttribute("messageList", messageList);
             
            if(messageListIndex == null)
                 request.getRequestDispatcher("/messageList.jsp").forward(request, response);
             else{
                //request.getRequestDispatcher("/messageList.jsp").include(request, response);
                 request.getRequestDispatcher("/messageFragment.jsp").forward(request, response);
             }
            //request.getRequestDispatcher("/profile.jsp").forward(request, response);
         }
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
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         MessageService ms = new MessageService();
         ms.createMessage((User) request.getSession().getAttribute("user"), request.getParameter("message"));
         doGet(request, response);
     }
 }
