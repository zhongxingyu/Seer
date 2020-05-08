 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.chl.hajo.sshop;
 
 import edu.chl.hajo.shop.core.Product;
 import java.io.IOException;
 import javax.persistence.metamodel.SetAttribute;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author anno
  */
 @WebServlet(name = "ProductServlet",
         urlPatterns = {"/products/*"})
 public class ProductServlet extends HttpServlet {
 
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
         HttpSession session = request.getSession(true);
         if (session.getAttribute("container") == null) {
             session.setAttribute("container", new ContainerNavigator(0, 2, Shop.INSTANCE.getProductCatalogue()));
         }
 
         String view = request.getParameter("view");
         String action = request.getParameter("action");
         String id = request.getParameter("id");
         ContainerNavigator temp = (ContainerNavigator) session.getAttribute("container");
         // View handling
         if (view != null) {
             switch (view) {
                 case "next":
                     session.setAttribute("PRODUCT_LIST", temp.next());
                     break;
                 case "prev":
                     session.setAttribute("PRODUCT_LIST", temp.previous());
                     break;
                 case "add":
                     request.getRequestDispatcher("/WEB-INF/jsp/products/addProduct.jspx").forward(request, response);
                     return;
             }
         }
         if (action != null) {
             switch (action) {
                 case "remove":
                     if (id != null) {
                         Long k = Long.parseLong(id);
                         Shop.INSTANCE.getProductCatalogue().remove(k);
                     }
                     break;
                 case "edit":
                     if (id != null) {
                         session.setAttribute("PRODUCT", Shop.INSTANCE.getProductCatalogue().find(Long.parseLong(id)));
                         request.getRequestDispatcher("/WEB-INF/jsp/products/editProducts.jspx").forward(request, response);
                         return;
                     }
                 case "update":
                    String a = request.getParameter("id");
                    String b = request.getParameter("name");
                    String c = request.getParameter("price");
                    Product product = new Product(Long.parseLong(a), b, Double.parseDouble(c));
                     Shop.INSTANCE.getProductCatalogue().update(product);
                 case "add":
                     Shop.INSTANCE.getProductCatalogue().add(new Product(
                             request.getParameter("name"), 
                             Double.parseDouble(request.getParameter("price"))
                             ));
                     response.sendRedirect("/servlet_shop/products");
                     return;
             }
         }
         request.setAttribute("PRODUCT_LIST", temp.getRange());
 
         request.getRequestDispatcher("/WEB-INF/jsp/products/products.jspx").forward(request, response);
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
