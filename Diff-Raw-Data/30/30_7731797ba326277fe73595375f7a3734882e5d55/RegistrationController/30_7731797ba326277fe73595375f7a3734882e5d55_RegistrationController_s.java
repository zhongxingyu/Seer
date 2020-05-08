 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller;
 
 import Datastore.Entities.Registeredclient;
 import controller.Utils.Common;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Enumeration;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import model.RegisteredclientJpaController;
 import model.exceptions.PreexistingEntityException;
 import model.exceptions.RollbackFailureException;
 
 /**
  *
  * @author root
  */
 public class RegistrationController extends HttpServlet {
 
     /** 
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
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
 
             RegisteredclientJpaController userJPA = new RegisteredclientJpaController();
             
             
             //phone, nif, street, flat, surname, number, password, city
             //email, name, pc, province, letter, card
             
             String name = request.getParameter("name");
             String surname = request.getParameter("surname");
             String phone = request.getParameter("phone");
             String nif = request.getParameter("nif");
             String street = request.getParameter("street");
             String flat = request.getParameter("flat");
             String number = request.getParameter("number");
             String password = request.getParameter("password");
             String city = request.getParameter("city");
             String email = request.getParameter("email");
             String pc = request.getParameter("pc");
             String leter = request.getParameter("leter");
             String card = request.getParameter("card");
             String province = request.getParameter("province");
             
             int PK = Common.generateUserID(email);
             /*Registeredclient(Integer clientID, String nif, int phone,
                     String email, String nam, String surname, String passwor,
                             long creditCard, String nationality, int pc,
                                     String city, String province, String street, int numbe,
                                             int flat, int fav, int anuncio)*/
             Registeredclient newClient = new Registeredclient(PK,nif,Integer.valueOf(phone),
                     email, name, surname, password, Long.valueOf(card), "NA", Integer.valueOf(pc),
                     city, province, street, Integer.valueOf(number),Integer.valueOf(flat),
                     -1, -1);
             newClient.setLeter(leter.charAt(0));
             
             
             try {
                 int count = userJPA.getRegisteredclientCount();
                 userJPA.create(newClient);
                 count = userJPA.getRegisteredclientCount();
                 
                 
                 HttpSession session = request.getSession(true);
                 //Log in will expire every 20 minutes
                 session.setMaxInactiveInterval(20 * 60);
                 //Store user credential
                 session.setAttribute("userid", PK);
                 request.getRequestDispatcher("/UserProfile").forward(request, response);
                 
             } catch (PreexistingEntityException ex) {
                 Logger.getLogger(RegistrationController.class.getName()).log(Level.SEVERE, null, ex);
             } catch (RollbackFailureException ex) {
                 Logger.getLogger(RegistrationController.class.getName()).log(Level.SEVERE, null, ex);
             } catch (Exception ex) {
                 Logger.getLogger(RegistrationController.class.getName()).log(Level.SEVERE, null, ex);
             }            
             
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
