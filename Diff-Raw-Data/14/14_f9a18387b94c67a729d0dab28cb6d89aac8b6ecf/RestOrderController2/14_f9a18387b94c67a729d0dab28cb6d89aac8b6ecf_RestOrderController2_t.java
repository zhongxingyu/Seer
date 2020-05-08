 
 package edu.wctc.distjava.ch3.controller;
 
 import edu.wctc.distjava.ch3.model.MenuItem;
 import edu.wctc.distjava.ch3.model.OrderService;
 import java.io.IOException;
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
         
     }
         
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException{
         response.setContentType("text/html");
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
         //responseCommitted = outputConfirmation(request, response, menuList, orderList, responseCommitted);
        //if(!responseCommitted) {
        request.setAttribute("menuList", menuList);
        request.setAttribute("orderList", orderList);

        // Redirect to destination page
        RequestDispatcher dispatcher = 
                getServletContext().getRequestDispatcher("/orderResult.jsp");
        dispatcher.forward(request, response);
        //}
     }
    
 }
