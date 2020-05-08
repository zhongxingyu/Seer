 package servlets;
 
 import java.util.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import beans.*;
 /**
  *
  * @author  Alexander Fougner
  * @version 1.0
  */
 public class TestServlet extends HttpServlet {
     private static String showPage=null;
     private static String checkoutPage = null;
     private static String thankyouPage = null;
     private static String byePage = null;
     private static String profilePage = null;
     private static String userPage = null;
     private static String jdbcURL = null;
     private static String detailPage=null;
     private static String redirectPage = null;
     private BookListBean bookList = null;
     /** Initializes the servlet.
      */
     public void init(ServletConfig config) throws ServletException {
        Super.init(config);
 
      }
     
     /** Destroys the servlet.
      */
     public void destroy() {
         
     }
     
     /** Processes requests for both HTTP <code>GET</code> 
         and <code>POST</code> methods.
      * @param request servlet request
      * @param response servlet response
      */
     protected void processRequest(HttpServletRequest request, 
                                   HttpServletResponse response)
     throws ServletException, java.io.IOException {
 
         HttpSession sess = request.getSession();
         RequestDispatcher rd = null;
 
         if(request.getParameter("action") == null || 
            request.getParameter("action").equals("show")){
 	    
             // A request dispatcher that's connected to the page.
 	    
             rd = request.getRequestDispatcher("index.jsp"); 
             rd.forward(request,response);
         }
     }
     
     /** Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      */
     protected void doGet(HttpServletRequest request, 
                          HttpServletResponse response)
 	throws ServletException, java.io.IOException {
         processRequest(request, response);
     }
     
     /** Handles the HTTP <code>POST</code> method.
      * @param request servlet request
      * @param response servlet response
      */
     protected void doPost(HttpServletRequest request, 
                           HttpServletResponse response)
 	throws ServletException, java.io.IOException {
         processRequest(request, response);
     }
     
     /** Returns a short description of the servlet.
      */
     public String getServletInfo() {
         return "The TestServlet";
     }
 }
 
 
 
 
