 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Servlets;
 
 import restaurante.Utilities.SingletonGson;
 import com.google.gson.Gson;
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import restaurante.Beans.Users;
 
 /**
  *
  * @author Alan
  */
 public class slTestLogin extends HttpServlet {
 
     /** 
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         response.setContentType("application/json;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             //Creating a temporary user to create the json
             Users user = new Users();
             user.setName("admin");
            user.setPassword("admin");
             
             //Creating json with the user
             Gson gson = SingletonGson.getInstance();
             String json = gson.toJson(user);
             
             //Set attribute and redirect to login
             out.print(json);
             request.setAttribute("json", json);
             RequestDispatcher rd = request.getRequestDispatcher("/login");
             rd.forward(request, response);
         } catch (Exception e) {
             out.print(e);
         } finally {
             out.close();
         }
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
