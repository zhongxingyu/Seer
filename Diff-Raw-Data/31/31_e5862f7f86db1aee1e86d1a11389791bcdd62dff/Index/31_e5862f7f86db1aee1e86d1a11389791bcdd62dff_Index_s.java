 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.cart.web;
 
 import com.cart.model.Cart;
 import com.cart.model.CartItem;
 import com.cart.model.Catalog;
 import com.cart.model.Product;
 import com.cart.model.ProductsLoad;
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author SanelisoEmmah
  */
 @WebServlet(name = "Index", urlPatterns = {"/Index"})
 public class Index extends HttpServlet {
 
      
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         
         
         HttpSession session = request.getSession(true);
         
         Cart cart = (Cart)session.getAttribute("cart");
         if (cart != null) {
              
          } else {
            
         
              cart = new Cart();
           
              session.setAttribute("cart",cart);
             }
         
        new ProductsLoad();
         
         
       
         RequestDispatcher view = request.getRequestDispatcher("index.jsp");
         view.forward(request, response);
     }
 
   
 
     
 }
