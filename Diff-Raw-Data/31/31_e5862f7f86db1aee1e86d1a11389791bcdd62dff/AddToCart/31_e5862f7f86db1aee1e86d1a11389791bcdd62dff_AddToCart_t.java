 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.cart.web;
 
 import com.cart.model.Cart;
 import com.cart.model.CartItem;
import com.cart.model.Catalog;
 import com.cart.model.Product;
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author Sarah
  */
 public class AddToCart extends HttpServlet {
 
     
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
        
         int id = Integer.parseInt((String)request.getParameter("id"));
         int amount = Integer.parseInt((String)request.getParameter("amount"));
         
         HttpSession session = request.getSession(true);
        Catalog catalog = Catalog.getInstance();
        ArrayList<Product> list = catalog.getCatalog();
         CartItem cartitem = new CartItem();
         Cart cart =  (Cart)session.getAttribute("cart");
        
         
         
         for (Product p : list){
             if(p.getProductID() == id){
                 cartitem.setProduct(p);
                 cartitem.setNumberOfItems(amount);
                 
                 cart.addItemToCart(cartitem, amount);
                 
                 session.setAttribute("cart", cart);
             }
         }
         
         response.sendRedirect("Index");
         
         
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
