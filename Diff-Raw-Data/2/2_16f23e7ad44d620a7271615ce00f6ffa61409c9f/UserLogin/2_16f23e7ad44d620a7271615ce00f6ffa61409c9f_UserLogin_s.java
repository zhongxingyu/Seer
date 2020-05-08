 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package UserInterface;
 
 import java.sql.Connection;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author Joseph
  */
 public class UserLogin extends HttpServlet
 {
     protected final String ERROR_MESSAGE = "<div class=\"alert alert-error\">"
             + "<button type=\"button\" class=\"close\" "
             + "data-dismiss=\"alert\">×</button>%s</div>";
 
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException
     {
     }
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException
     {
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try
         {
             String dbName = request.getParameter("dbName");
             String dbAddr = request.getParameter("dbAddress");
             String dbUser = request.getParameter("dbUser");
             String dbPW = request.getParameter("dbPW");
 
             Connection conn = null;
             try
             {
                 String userConnect = "jdbc:mysql://"
                         + dbAddr + "/" + dbName + "?user=" + dbUser
                         + "&password=" + dbPW;
 
                 System.out.println("Loaded driver.");
                 Class.forName("com.mysql.jdbc.Driver").newInstance();
                 conn = DriverManager.getConnection(userConnect);
             }
             catch(SQLException ex)
             {
                 System.out.println(ex.getMessage());
                 ex.printStackTrace();
                 request.setAttribute("errorMessage", String.format(ERROR_MESSAGE, ex.getMessage()));
                 request.getRequestDispatcher("Error.jsp").forward(request, response);

                //response.sendRedirect("Error.jsp");
                 return;
             }
             catch(ClassNotFoundException ex)
             {
                 System.out.println(ex.getMessage());
                 out.printf(ERROR_MESSAGE, ex.getMessage());
                 ex.printStackTrace();
                 response.sendRedirect("Error.jsp");
                 return;
             }
             catch(Exception ex)
             {
                 System.out.println(ex.getMessage());
                 out.printf(ERROR_MESSAGE, ex.getMessage());
                 ex.printStackTrace();
                 response.sendRedirect("Error.jsp");
                 return;
             }
             finally
             {
                 if(conn != null)
                 {
                     try
                     {
                         conn.close();
                     }
                     catch(Exception ex)
                     {
                         System.out.println(ex.getMessage());
                         out.printf(ERROR_MESSAGE, ex.getMessage());
                         ex.printStackTrace();
                     }
                 }
             }
 
             request.getSession().setAttribute("pw", dbPW);
             request.getSession().setAttribute("user", dbUser);
             request.getSession().setAttribute("dbname", dbName);
             request.getSession().setAttribute("addr", dbAddr);
 
             response.sendRedirect("MainInterface.jsp");
         }
         finally
         {
             out.close();
         }
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
             throws ServletException, IOException
     {
         // processRequest(request, response);
     }
 
     /**
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo()
     {
         return "Short description";
     }
 }
