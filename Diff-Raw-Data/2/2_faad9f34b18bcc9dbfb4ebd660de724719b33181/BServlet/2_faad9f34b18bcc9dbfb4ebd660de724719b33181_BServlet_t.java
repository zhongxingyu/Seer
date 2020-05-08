 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.web.bokningstjanst;
 
 
 import com.mycompany.booking.core.IDepartureCatalogue;
 import com.mycompany.booking.core.ITicketCatalogue;
 import java.io.IOException;
 import java.sql.Time;
 import java.util.Date;
 import java.util.logging.Logger;
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
                     Logger logger = Logger.getLogger(getClass().getName());
                     //TODO Lägg till all tåg  info till request här
                     IDepartureCatalogue d =  Booking.INSTANCE.getDepartureCatalogue();
                     
                     //Departure dep = d.getAll(emf.createEntityMnager().getAl);
                     request.getSession().setAttribute("DEPARTURES", Booking.INSTANCE.getDepartureCatalogue().getAll());
                     for(Object o :d.getAll()){
                         logger.severe(o.toString());
                     }
                     request.getRequestDispatcher("WEB-INF/jsp/ticket/train.jspx").forward(request, response);
                     break;
             }
         }
         if (tickets != null) {
             switch (tickets) {
                 case "chooseDeparture":
                     //HITTA alla avgångar från departure request.getParameter(departure_city)
                     //och till request.getParameter(arrival_city)
                     Logger logger = Logger.getLogger(getClass().getName());
                     String tmp = request.getParameter("destination");
                     logger.severe("Destination: "+tmp);
                     IDepartureCatalogue d =  Booking.INSTANCE.getDepartureCatalogue();
                     logger.severe(d.getByDestination(tmp).toString());
                     //logger.severe(JPABookingFactory.getBooking(Persistence.createEntityManagerFactory("test")).getDepartureCatalogue().getByDestination("Ostersund").toString());
                     request.getSession().setAttribute("MATCHING_DEST", Booking.INSTANCE.getDepartureCatalogue()
                             .getMatchingDeparture(request.getParameter("departure"),request.getParameter("destination")));
                     //logger.severe(d.getMatchingDeparture(request.getParameter("departure"),request.getParameter("destination")).toString());
                     request.getRequestDispatcher("WEB-INF/jsp/ticket/allDepartures.jspx").forward(request, response);  
                     break;
                 case "Confirm":
                     
                     //Resan som valts  är 
                    //ID till departure = request.getParameter(id)
                     HttpSession session = request.getSession();
                     session.setMaxInactiveInterval(60);
                     IDepartureCatalogue dc = Booking.INSTANCE.getDepartureCatalogue();
                     
                     request.getSession().setAttribute("DEP", Booking.INSTANCE.getDepartureCatalogue().getById(Long.valueOf(request.getParameter("id"))));
                     request.getRequestDispatcher("WEB-INF/jsp/ticket/ticketValidation.jspx").forward(request, response);
                     break;
 
                 case "Payment":
                     //Check so the session has not died
                     if (request.getSession(false) != null) {
                         ITicketCatalogue tc =  Booking.INSTANCE.getTicketCatalogue();
                         //tc.add(new Ticket(/*CustomerId*/,, ticketPrice));
                         request.getSession().setAttribute("DEP", Booking.INSTANCE.getDepartureCatalogue().getById(Long.valueOf(request.getParameter("id"))));
                         request.getRequestDispatcher("WEB-INF/jsp/ticket/payment.jspx").forward(request, response);
                     } else {
                        request.getRequestDispatcher("WEB-INF/jsp/ticket/allDepartures.jspx").forward(request, response);
                     }
                     break;
                 case "PaymentSuccess":
                     
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
