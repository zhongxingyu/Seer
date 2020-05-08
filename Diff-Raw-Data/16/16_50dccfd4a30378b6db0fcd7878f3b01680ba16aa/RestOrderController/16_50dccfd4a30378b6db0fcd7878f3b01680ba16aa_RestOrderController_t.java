 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wctc.distjava.ch3.controller;
 
 import edu.wctc.distjava.ch3.model.OrderData;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author Spark
  */
 public class RestOrderController extends HttpServlet {
    private static final String RESULT_PAGE = "/orderResult.jsp";
     
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException{
         
     }
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException{
         response.setContentType("text/html");
         
        /**
         //test to make sure servlet is working
         PrintWriter out = response.getWriter();
         out.println("Order Selection<br>");
         String c = request.getParameter("sweet1");
         out.println("<br>Sweet selection " + c);
         **/
         
         //retreiving user order data from orderForm.html
         String s1 = request.getParameter("sweet1");
         String c1 = request.getParameter("cream1");
         //out.println("<br> S1 = " + s1);
         
         OrderData or = new OrderData();
         List<String> orderPlaced = new ArrayList<String>();
         orderPlaced.add(or.getOrderInfo(s1));
         orderPlaced.add(or.getOrderInfo(c1));
         //String sweetResult = or.getOrderInfo(s1.trim());
         
         request.setAttribute("itemsOrdered", orderPlaced);
         
         RequestDispatcher view = request.getRequestDispatcher(RESULT_PAGE);
         view.forward(request, response);
         
     }
   
 }
