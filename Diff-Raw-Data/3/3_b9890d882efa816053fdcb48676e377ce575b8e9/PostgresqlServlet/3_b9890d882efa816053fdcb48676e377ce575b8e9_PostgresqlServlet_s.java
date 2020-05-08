 /*
  * To change this template, choose Tools | Templates and open the template in
  * the editor.
  */
 package com.cloudbees.servlet;
 
 import com.cloudbees.entity.Countries;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.Collection;
 import java.util.Iterator;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.DataSource;
 
 /**
  *
  * @author harpreet
  */
 public class PostgresqlServlet extends HttpServlet {
 
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
             prefixHTML(out);
 
             /*
              * TODO output your page here out.println("<html>");
              * out.println("<head>"); out.println("<title>Servlet
              * HelloJPACloudBees</title>"); out.println("</head>");
              * out.println("<body>"); out.println("<h1>Servlet HelloJPACloudBees
              * at " + request.getContextPath () + "</h1>");
              * out.println("</body>"); out.println("</html>");
              *
              *
              */
            /* Context ctx = new InitialContext();
             DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/postgresql");
 
             Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rst = stmt.executeQuery("select * from countries");
             while (rst.next()) {
                 int id = rst.getInt(1);
                 String foo = rst.getString(2);
                 String bar = rst.getString (3);
                 out.println (id + foo + bar);
             }
             conn.close();
 */
             EntityManagerFactory emf = Persistence.createEntityManagerFactory("PostgresqlPU");
             EntityManager em = emf.createEntityManager();
             em.getTransaction().begin();
 
             Query queryAll = em.createNamedQuery("Countries.findAll");
             out.println("Getting results");
             Collection c = queryAll.getResultList();
 
             out.println("DB Size :" + c.size() + "<br/>");
             Iterator iterator = c.iterator();
            postfixHTML(out);
             while (iterator.hasNext()) {
                 Countries country = (Countries) iterator.next();
                 out.println("<b>" + country.getCaptial() + "</b> is the capital of " + country.getCountry() + "<br/>");
             }
         } catch (Exception e) {
             out.println(e.toString());
 
         } finally {
             out.close();
         }
     }
 
     void prefixHTML(PrintWriter out) {
         out.println("<html><head><title>Hello JPA CloudBees</title></head><body>");
     }
 
     void postfixHTML(PrintWriter out) {
         out.println("</body></html>");
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
