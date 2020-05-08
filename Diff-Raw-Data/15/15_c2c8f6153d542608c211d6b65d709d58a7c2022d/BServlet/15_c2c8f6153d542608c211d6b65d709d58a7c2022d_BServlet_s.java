 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.web.bokningstjanst;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author Patrik
  */
 @WebServlet(name = "BookingServlet", urlPatterns = {"/ticket/*"})
 public class BServlet extends HttpServlet {
 
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
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         response.setContentType("text/html;charset=UTF-8");
 
         //Get the view parameter.
         String view = request.getParameter("view");
 
         //Used when ticket of some sort should be handeled
         String tickets = request.getParameter("tickets");
 
         if (view != null) {
             switch (view) {
 
                 case "train":
                    //TODO Lgg till all tg  info till request hr
                    List <String> myList = new ArrayList<String>();
                    myList.add("stad1");
                    myList.add("stad2");
                     request.setAttribute("train_departure", view);
                     request.getRequestDispatcher("WEB-INF/jsp/ticket/train.jspx").forward(request, response);
                     break;
             }
         }
         if (tickets != null) {
             switch (tickets) {
                 case "chooseDeparture":
                    //HITTA alla avgngar frn departure request.getParameter(departure_city)
                     //och till request.getParameter(arrival_city)
                     request.getRequestDispatcher("WEB-INF/jsp/ticket/allDepartures.jspx").forward(request, response);
                     
                     break;
                 case "Confirm":
                     
                    //Resan som valts  r 
                    //ID till departure = request.getParameter(id)
                     
                     
                     HttpSession session = request.getSession();
                     session.setMaxInactiveInterval(30);
 
                    //ndra request.getParam hr till vrdena frn databasen i stllet
                  //   session.setAttribute("departure_city", request.getParameter("departure_city"));
                    // session.setAttribute("departure_time", request.getParameter("departure_time"));
                     //session.setAttribute("departure_date", request.getParameter("departure_date"));
                     //session.setAttribute("arrival_city", request.getParameter("arrival_city"));
                     //session.setAttribute("arrival_date", request.getParameter("arrival_date"));
                     //session.setAttribute("arrival_time", request.getParameter("arrival_time"));
 
                     request.getRequestDispatcher("WEB-INF/jsp/ticket/ticketValidation.jspx").forward(request, response);
 
 
                     break;
 
                 case "Payment":
                     //Check so the session has not died
                     if (request.getSession(false) != null) {
                         request.getRequestDispatcher("WEB-INF/jsp/ticket/payment.jspx").forward(request, response);
                     } else {
                         request.getRequestDispatcher("WEB-INF/jsp/ticket/ticketValidation.jspx").forward(request, response);
                     }
                     break;
 
                 case "PaymentComplete":
                     request.getRequestDispatcher("WEB-INF/jsp/ticket/paymentSuccess.jspx").forward(request, response);
                     break;
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
