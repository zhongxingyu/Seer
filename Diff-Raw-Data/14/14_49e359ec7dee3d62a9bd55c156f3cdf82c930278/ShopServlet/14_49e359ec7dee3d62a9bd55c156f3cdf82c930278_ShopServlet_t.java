 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package servlets;
 
 import beans.ComputerBean;
 import beans.ComputerListBean;
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author Martin
  */
 public class ShopServlet extends HttpServlet {
     private static String startpage = null;
     private static String jdbcURL = null;
     private static String detailspage = null;
    private static String errorpage = null;
     
     private ComputerListBean cList = null;
     
     /**
      * Initializer for the servlet.
      */
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         
         /*Update all jsp strings from config*/
         startpage = config.getInitParameter("SHOW_PAGE");
         jdbcURL = config.getInitParameter("JDBC_URL");
         detailspage = config.getInitParameter("DETAIL_PAGE");
        errorpage = config.getInitParameter("ERROR_PAGE");
         
         try{
             cList = new ComputerListBean(jdbcURL); }
         catch(Exception e) {
             throw new ServletException(e);
         }
         
         ServletContext sc = getServletContext();
         sc.setAttribute("computerList",cList);
                 
     }
 
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
         
         HttpSession sess = request.getSession();
         RequestDispatcher rd = null;
         
         if(request.getParameter("action") == null || 
            request.getParameter("action").equals("show")){	    
             rd = request.getRequestDispatcher(startpage); 
             rd.forward(request,response);
         }
         else if (request.getParameter("action").equals("details")) {
             if(request.getParameter("cid") != null) {
                 
                 ComputerBean comp = 
                         cList.getById(
                         Integer.parseInt(request.getParameter("cid")));
                 
                 request.setAttribute("comp", comp);
             }
             else {
                 throw new ServletException("No cid when viewing detail");
             }
            if(request.getAttribute("comp") != null) {
                rd = request.getRequestDispatcher(detailspage);
                rd.forward(request,response);
            } else {
                rd = request.getRequestDispatcher(errorpage);
                rd.forward(request, response);
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
