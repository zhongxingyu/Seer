 package org.meteorologaaguascalientes.presentation;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.meteorologaaguascalientes.businesslogic.facade.ServiceFacade;
 import org.meteorologaaguascalientes.control.forecast.ForecastsFactory;
 
 @WebServlet(name = "History", urlPatterns = {"/history"})
 public class History extends HttpServlet {
 
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
         Properties prop = new Properties();
         prop.load(getServletContext().getResourceAsStream("/WEB-INF/config.properties"));
         response.setContentType("text/csv;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
             String variableName = request.getParameter("variable");
             if (variableName != null) {
                 List<SortedMap<Date, Double>> dataList;
                 SortedMap<Date, Double> data;
                 ServiceFacade serviceFacade = new ServiceFacade();
                 dataList = serviceFacade.getData(variableName, ForecastsFactory.DEFAULT);
                out.println(prop.getProperty("date") + "," + prop.getProperty( variableName) + "," + prop.getProperty("forecast"));
                 data = dataList.get(0);
                 for (Map.Entry<Date, Double> e : data.entrySet()) {
                     out.println(formatter.format(e.getKey())
                             + "," + e.getValue() + ",");
                 }
                 if (!data.isEmpty()) {
                     out.println(formatter.format(data.lastKey()) + ",," + data.get(data.lastKey()) + ",");
                 }
                 data = dataList.get(1);
                 for (Map.Entry<Date, Double> e : data.entrySet()) {
                     out.println(formatter.format(e.getKey())
                             + ",," + e.getValue());
                 }
             }
         } finally {
             out.close();
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
