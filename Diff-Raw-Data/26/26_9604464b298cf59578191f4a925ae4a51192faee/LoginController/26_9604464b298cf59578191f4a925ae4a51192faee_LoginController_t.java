 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller;
 
 import Flipmotor.Entities.Registeredclient;
 import controller.Utils.Common;
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import Flipmotor.model.RegisteredclientJpaController;
 
 /**
  *
  * @author Antón García Dosil
  */
 public class LoginController extends HttpServlet {
 
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
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
 
             /*
              * Class that checks a list to see if the user is logged in.
              * Creates session attributes with userid
              */            
             
             String req_name = request.getParameter("username");
             String req_pass = request.getParameter("userpass");
             
             RegisteredclientJpaController userJPA = new RegisteredclientJpaController();
             
             //Generate ID for the given name-password pair
             int userid = Common.generateUserID(req_name);
                         
             //Check if the ID is in the database
             Registeredclient user = new Registeredclient();            
             user = userJPA.findRegisteredclient(userid);
             
                        
             if(null != user && user.getPasswor().equals(req_pass)){
                 HttpSession session = request.getSession(true);
                 //Log in will expire every 20 minutes
                 session.setMaxInactiveInterval(20 * 60);
                 //Store user credential
                 session.setAttribute("userid", userid);
             }else{
                 // SHOW WRONG LOGIN INPUT MESSAGE
                 System.out.println("error");
             }
             request.getRequestDispatcher("/Home").forward(request, response);
        }catch(Exception ex)
        {
            System.out.println(ex.toString());
        }finally {            
             out.close();
         }
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
