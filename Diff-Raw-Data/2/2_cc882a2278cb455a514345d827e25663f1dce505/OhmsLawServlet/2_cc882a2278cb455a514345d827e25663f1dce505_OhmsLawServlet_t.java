 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package servlets;
 
 import java.io.IOException;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import utilities.OhmsLaw;
 
 /**
  *
  * @author Joshua
  */
 public class OhmsLawServlet extends HttpServlet {
 
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
        
        String amps = request.getParameter("amps").replaceAll("[\\s]", "");
        String ohms = request.getParameter("ohms").replaceAll("[\\s]", "");
        String volts = request.getParameter("volts").replaceAll("[\\s]", "");
        String url = "/index.jsp";
       
        double a;
        double o;
        double v;
        
        if((amps.length() > 0 && ohms.length () > 0 
        || amps.length() > 0 && volts.length() > 0 
        || ohms.length() > 0 && volts.length() > 0))
        {
            try{
                 if(amps.length() == 0)
                 {
                     o = Double.parseDouble(ohms);
                     v = Double.parseDouble(volts);
                     if(!( o > -.000000001 && o < .000000001))
                     {
                         amps = String.format("%12.8f", OhmsLaw.calcAmps(o, v));
                     }
                     else
                     {
                         amps = "DNE";
                     }
                 }
                 else if(ohms.length() == 0)
                 {
                    a = Double.parseDouble(amps);
                     v = Double.parseDouble(volts);
                     
                     if(!( a > -.000000001 && a < .000000001))
                     {
                         ohms = String.format("%12.8f", OhmsLaw.calcOhms(a, v));
                     }
                     else
                     {
                         amps = "DNE";
                     }
                     
                 }
                 else if(volts.length() == 0)
                 {
                     volts = String.format("%12.8f",
                     OhmsLaw.calcVolts(Double.parseDouble(ohms), Double.parseDouble(amps)));
                 }
                 else
                 {
                     amps = "";
                     ohms = "";
                     volts = "";
                 }
            }
            catch(Exception e)
            {
                System.out.println(e.toString());
                if(amps.replaceAll("[\\D]", "").length() 
                   + amps.replaceAll("[^.]", "").length() < amps.length()
                   || amps.replaceAll("[.]", "").length() < amps.length() - 1)
                {
                    amps = "invalid entry";
                }
                
                if(ohms.replaceAll("[\\D]", "").length() 
                   + ohms.replaceAll("[^.]", "").length() < ohms.length()
                   || ohms.replaceAll("[.]", "").length() < ohms.length() - 1)
                {
                    ohms = "invalid entry";
                }
                
                if(volts.replaceAll("[\\D]|[.]{2,}", "").length() 
                   + volts.replaceAll("[^.]", "").length() < volts.length()
                   || volts.replaceAll("[.]", "").length() < volts.length() - 1)
                {
                    volts = "invalid entry";
                }
            }
        }
        else
        {
            if(amps.isEmpty())
            {
                amps = "DNE";
            }
            if(ohms.isEmpty())
            {
                ohms = "DNE";
            }
            if(volts.isEmpty())
            {
                volts = "DNE";
            }
        }
        
        request.setAttribute("amps", amps);
        request.setAttribute("ohms", ohms);
        request.setAttribute("volts", volts);
        
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
        dispatcher.forward(request, response);
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
