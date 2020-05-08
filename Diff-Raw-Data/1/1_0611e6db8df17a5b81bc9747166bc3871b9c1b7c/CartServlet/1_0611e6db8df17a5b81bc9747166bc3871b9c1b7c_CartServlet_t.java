 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package servlet;
 
 import model.ShoppingCart;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 
 /**
  *
  * @author valeh
  */
 
 public class CartServlet extends HttpServlet {
 
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
         
         // match url "~/cart"
         String userPath = request.getRequestURI().substring(request.getContextPath().length());
         String url = "/WEB-INF/views" + userPath + ".jsp";
         
         // create session 
         HttpSession session = request.getSession();
         // store current user path for redirect
         session.setAttribute("lastUserPath", userPath);
         
         request.getRequestDispatcher(url).forward(request, response);      
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
         
         String userPath = request.getRequestURI().substring(request.getContextPath().length());
         System.out.println("user path = " + userPath);
         
         String url = null;        
             
         // create session 
         HttpSession session = request.getSession();
         if(session.getAttribute("cart") == null) {
             session.setAttribute("cart", new ShoppingCart());
         }
         ShoppingCart cart = (ShoppingCart)session.getAttribute("cart");
         
         // match url "~/cart/add"
         if (userPath.equals("/cart/add")) {
         
             /* Get all the data from the form */
             String id = request.getParameter( "cd_id" );            
             int cdId = Integer.parseInt(id);
             productcatalogue.business.Cd cd = getProductInfo(cdId);
             cart.add(cd.getId(), cd.getTitle(), cd.getPrice());
             
             //Message sent to the view
             String mess = "Add cd to cart";
             request.setAttribute( "notice", mess );
         
             url = request.getContextPath() + (String)session.getAttribute("lastUserPath");
             response.sendRedirect(url);
             // request.getRequestDispatcher(url).forward(request, response);
             
         // match url "~/cart/remove"   
         } else if(userPath.equals("/cart/remove")) {
         
             /* Get all the data from the form */
             String id = request.getParameter( "cd_id" );            
             int cdId = Integer.parseInt(id);
             cart.remove(cdId);
             
             //Message sent to the view
             String mess = "Remove cd from cart";
             request.setAttribute( "notice", mess );
             
             url = request.getContextPath() + (String)session.getAttribute("lastUserPath");
             response.sendRedirect(url);
             // request.getRequestDispatcher(url).forward(request, response);
             
         // match url "~/cart/checkout"
         } else if(userPath.equals("/cart/checkout")) {
             
             // fill out order
             orderprocess.business.Order order = new orderprocess.business.Order(); 
             GregorianCalendar cal = new GregorianCalendar();
             cal.setTime(new Date());
             try {
                 order.setDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
             } catch (DatatypeConfigurationException ex) {
                 Logger.getLogger(CartServlet.class.getName()).log(Level.SEVERE, null, ex);
             }
             order.setStatus("created");
             // fill out customer in confirm order
             // order.setCustomer(new orderprocess.business.Customer());
             ArrayList<orderprocess.business.Cd> ocart = new ArrayList<orderprocess.business.Cd>();            
             for(int i = 0; i < cart.getSize(); i++){
                 
                 orderprocess.business.Cd cd = new orderprocess.business.Cd();
                 cd.setId(cart.getCDId(i));
                 cd.setTitle(cart.getCDTitle(i));
                 cd.setPrice(cart.getCDPrice(i));
                 cd.setAmount(cart.getCDAmount(i));
                 ocart.add(cd);                
             }
             // ArrayList is not allowed to set
             // order.setCart(ocart);
             
             // create order by calling web service operations
             int orderId = createOrder(order); 
             if(orderId >= 0) {
                 // delete cart if successful
                 session.setAttribute("cart", null);
                session.setAttribute("orderId", orderId);
                 
                 //Message sent to the view
                 String mess = "Order submitted";
                 request.setAttribute( "notice", mess );                
                 url = "/WEB-INF/views/pay.jsp";
                 response.sendRedirect(request.getContextPath() + "/order/pay");
             } else {
                 
                 //Message sent to the view
                 String mess = "Fail to submit order";
                 request.setAttribute( "error", mess );                
                 url = "/WEB-INF/views/cart.jsp";
                 request.getRequestDispatcher(url).forward(request, response);
             }
         } 
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
 
     private static productcatalogue.business.Cd getProductInfo(int productId) {
         productcatalogue.business.CatalogService service = new productcatalogue.business.CatalogService();
         productcatalogue.business.ProductCatalogueService port = service.getProductCatalogueServicePort();
         return port.getProductInfo(productId);
     }
 
     private static int createOrder(orderprocess.business.Order order) {
         orderprocess.business.CommOrderProcess service = new orderprocess.business.CommOrderProcess();
         orderprocess.business.OrderProcessService port = service.getOrderProcessServicePort();
         return port.createOrder(order);
     }
 }
