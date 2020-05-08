 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lib;
 
 import java.io.IOException;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author Diarmuid
  */
 public class ProductServlet extends HttpServlet {
 
    
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException{
         
         HttpSession session = request.getSession();
         User currentUser;      
         
         if(session.getAttribute("currentUser")==null){
             currentUser = new User();
         }else{
             currentUser = (User) session.getAttribute("currentUser");
         }
         String urlToUse = "";
         String productURL = "/product.jsp";
         String browseURL;
         
         if(request.getParameter("productID")!=null){
             String productID = request.getParameter("productID");
             urlToUse = productURL;
            ProductBean bean = new ProductBean(productID);
            Product product = bean.getProduct();
            request.setAttribute("product", product);
         }
         
         RequestDispatcher dispatcher = 
                 getServletContext().getRequestDispatcher(urlToUse);
         dispatcher.forward(request, response);
         
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /** 
      * Handles the HTTP <code>GET</code> method.
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
      * Handles the HTTP <code>POST</code> method.
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
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 }
