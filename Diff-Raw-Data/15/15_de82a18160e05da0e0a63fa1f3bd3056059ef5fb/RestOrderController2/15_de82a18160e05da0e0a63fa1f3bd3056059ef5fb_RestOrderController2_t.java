 
 package edu.wctc.distjava.ch3.controller;
 
 import edu.wctc.distjava.ch3.model.MenuItem;
 import edu.wctc.distjava.ch3.model.OrderService;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 /**
  * This class is a controller that creates the check boxes form
  * @author Todd Mueller
  * @version 1.0
  */
 
 public class RestOrderController2 extends HttpServlet {
     private final boolean DEBUG = false;
     private OrderService orderService;
     /**
      * Constructor
      */
     public RestOrderController2(){
         
     }
     
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException{

        List<MenuItem> menuList = orderService.getMenuList();
        request.setAttribute("menuList", menuList);
        
        // Redirect to destination page
        RequestDispatcher dispatcher = 
                getServletContext().getRequestDispatcher("/orderForm.jsp");
        dispatcher.forward(request, response);
         
     }
         
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException{
         boolean responseCommitted = false;
         //Database connection
         List<MenuItem> menuList = orderService.getMenuList();
         List<MenuItem> orderList = orderService.getOrderList();
         String[] orderItems = null;
         
         //checkboxes
         orderItems = request.getParameterValues("menuItems");
         orderList.clear();
         for(String item : orderItems){
             for(MenuItem menuItem : menuList){
                 if(menuItem.getMenuDescription().equals(item)){
                     orderList.add(menuItem);
                     break;
                 } 
             }
         }
         if(orderList.size() > 0){
             orderService.placeOrder();
         }
  
         // We need to get the value of responseCommitted back from
         // the method because the original is passed by value (a copy)                    
         responseCommitted = 
                 outputConfirmation(request, response, menuList, orderList, responseCommitted);
         //if(!responseCommitted) {
         request.setAttribute("menuList", menuList);
         request.setAttribute("orderList", orderList);
 
         // Redirect to destination page
         RequestDispatcher dispatcher = 
                 getServletContext().getRequestDispatcher("/orderResult.jsp");
         dispatcher.forward(request, response);
         //}
     }
 
     private boolean outputConfirmation(HttpServletRequest request, HttpServletResponse response, List<MenuItem> menuList, List<MenuItem> orderList, boolean responseCommitted) throws IOException {
         
         response.setContentType("text/html");
         PrintWriter out = response.getWriter();
         out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
         out.println("<HTML>");
         out.println("  <HEAD><TITLE>Order Receipt</TITLE></HEAD>");
         out.println("  <BODY>");
         out.print("    <h1>Todd's Sweets & Creamery Order</h2> ");
         out.println("<p>You ordered the following:</p>");
         out.println("<ul>");
         for(MenuItem item : orderList) {
                 out.println("<li>" + item.getMenuDescription() + "</li>");
         }
         out.println("</ul>");
         out.println("<a href='index.jsp'>Place a new order</a>");
         out.println("  </BODY>");
         out.println("</HTML>");
         out.flush();
         out.close();
         menuList.addAll(orderList);
         orderList.removeAll(orderList);
         responseCommitted = true;
         
         return responseCommitted;
         
     }
     /**
      * Initialize the servlet
      * @throws ServletException 
      */
     @Override
     public void init()throws ServletException{
         orderService = new OrderService();
     }
     
 }
