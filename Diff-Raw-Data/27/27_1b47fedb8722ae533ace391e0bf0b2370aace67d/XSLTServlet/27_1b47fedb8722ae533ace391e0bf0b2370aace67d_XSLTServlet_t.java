 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package xmlExercises.xslt;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import nu.xom.*;
 import nu.xom.xslt.XSLException;
 import nu.xom.xslt.XSLTransform;
 import xmlExercises.*;
 
 /**
  * @author Matous Jobanek
  */
 @WebServlet(name = "XSLTServlet", urlPatterns = {Constants.URL_XSLT_TASK, Constants.URL_XSLT_RESULT})
 public class XSLTServlet extends HttpServlet {
 
     private static final Logger LOGGER = Logger.getLogger(XSLTServlet.class.getPackage().toString());
 
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
 
         if (request.getServletPath().contains(Constants.TASK)) {
             task(request, response);
         } else if (request.getServletPath().contains(Constants.RESULT)) {
             result(request, response);
         } else {
             throw new RuntimeException("Unknown operation: " + request.getServletPath());
         }
 
     }
 
     private void task(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 
        Assignment assignment = null;
         try {
             String id = request.getParameter("id");
            assignment = XSLTUtils.getAssignment(id);
 
         } catch (XSLException ex) {
             Logger.getLogger(XSLTServlet.class.getName()).log(Level.SEVERE, null, ex);
            assignment = new Assignment();
            assignment.setAssignmentText("There has occured some problem.");
 
         } catch (SyntaxErorException ex) {
            assignment = new Assignment();
            assignment.setAssignmentText("There has occured some problem.");
         }
        request.setAttribute(Assignment.class.getSimpleName(), assignment);
        request.getRequestDispatcher(Constants.JSP_ASSIGNMENT).forward(request, response);
     }
 
     private void result(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 
         String userSolution = request.getParameter("userSolution");
         if (userSolution != null && !"".equals(userSolution)) {
             try {
 
                 ServletContext context = getServletContext();
                 String id = request.getParameter("id");
                 XSLTResult result;
 
                 result = XSLTUtils.evaluate(userSolution, id);
 
                 request.setAttribute(XSLTResult.class.getSimpleName(), result);
                 request.getRequestDispatcher(Constants.JSP_XSLT_RESULT).forward(request, response);
 
             } catch (XSLException ex) {
                 returnError(request, response, "There is a problem with your input! Please correct it.");
             } catch (SyntaxErorException ex) {
                 returnError(request, response, "There is a problem with your input! Please correct it.");
             } catch (RuntimeException ex) {
                 returnError(request, response, "There is a problem with your input! Please correct it.");
             }
 
         } else {
             returnError(request, response, "The input is empty!");
         }
     }
 
     private void returnError(HttpServletRequest request, HttpServletResponse response, String message) throws ServletException, IOException {
         request.setAttribute(Constants.ATTRIBUTE_ERROR, message);
         request.getRequestDispatcher(Constants.JSP_ERROR).forward(request, response);
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
