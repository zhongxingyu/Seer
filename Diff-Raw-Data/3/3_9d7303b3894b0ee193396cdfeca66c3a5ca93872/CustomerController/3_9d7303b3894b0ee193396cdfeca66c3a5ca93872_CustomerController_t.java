 package com.ryliu.j2ee.lab03;
 
 import com.ryliu.j2ee.utils.Helper;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.sql.SQLException;
 
/**
 *  The controller of customer information lab.
 */
 public class CustomerController extends HttpServlet {
 
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         if (request.getParameter("form") != null) {
             form(request, response);
         } else if (request.getParameter("delete") != null) {
             delete(request, response);
         } else {
             list(request, response);
         }
     }
 
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         if (request.getParameter("update") != null) {
             update(request, response);
         } else if (request.getParameter("insert") != null) {
             insert(request, response);
         } else {
             list(request, response);
         }
     }
 
     /**
      * Insert the customer.
      *
      * @param request the HTTP servlet request
      * @param response the HTTP servlet response
      * @throws IOException if any IO error occurred.
      * @throws ServletException if any error occurred.
      */
     private void insert(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         try {
             CustomerDAO dao = new CustomerDAO();
             Customer customer = Helper.getFromRequest(Customer.class, request);
             dao.insert(customer);
             response.sendRedirect(request.getContextPath() + "/lab03/customer");
         } catch (SQLException e) {
             throw new ServletException("SQL error occurred.", e);
         }
     }
 
     /**
      * Delete the customer according to its cid.
      *
      * @param request the HTTP servlet request
      * @param response the HTTP servlet response
      * @throws IOException if any IO error occurred.
      * @throws ServletException if any error occurred.
      */
     private void delete(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         try {
             if (request.getParameter("cid") == null) {
                 response.getWriter().println("Invalid request!");
             } else {
                 CustomerDAO dao = new CustomerDAO();
                 dao.delete(request.getParameter("cid"));
                 response.sendRedirect(request.getContextPath() + "/lab03/customer");
             }
         } catch (SQLException e) {
             throw new ServletException("SQL error occurred.", e);
         }
     }
 
     /**
      * The form control method.
      *
      * @param request the HTTP servlet request
      * @param response the HTTP servlet response
      * @throws ServletException if any issue occurred.
      * @throws IOException if any IO issue occurred.
      */
     private void form(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         try {
             if (request.getParameter("cid") != null) {
                 CustomerDAO dao = new CustomerDAO();
                 request.setAttribute("customer", dao.get(request.getParameter("cid")));
             }
             RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/lab03/form.jsp");
             dispatcher.forward(request, response);
         } catch (SQLException e) {
             throw new ServletException("SQL error occurred.", e);
         }
     }
 
     /**
      * The update control method.
      *
      * @param request the HTTP servlet request
      * @param response the HTTP servlet response
      * @throws ServletException if any issue occurred.
      * @throws IOException if any IO issue occurred.
      */
     private void update(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         try {
             CustomerDAO dao = new CustomerDAO();
             Customer customer = Helper.getFromRequest(Customer.class, request);
             dao.update(customer);
             response.sendRedirect(request.getContextPath() + "/lab03/customer");
         } catch (SQLException e) {
             throw new ServletException("SQL error occurred.", e);
         }
     }
 
     /**
      * The list control method.
      *
      * @param request the HTTP servlet request
      * @param response the HTTP servlet response
      * @throws ServletException if any issue occurred.
      * @throws IOException if any IO issue occurred.
      */
     private void list(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         try {
             CustomerDAO dao = new CustomerDAO();
             request.setAttribute("list", dao.list());
             RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/lab03/list.jsp");
             dispatcher.forward(request, response);
         } catch (SQLException e) {
             throw new ServletException("SQL error occurred.", e);
         }
     }
 }
