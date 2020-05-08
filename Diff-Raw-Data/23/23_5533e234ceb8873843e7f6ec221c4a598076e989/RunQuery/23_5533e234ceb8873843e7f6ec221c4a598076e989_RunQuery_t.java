 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package UserInterface;
 
 import java.sql.Connection;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author Joseph
  */
 public class RunQuery extends HttpServlet
 {
     protected final String ERROR_MESSAGE = "<div class=\"alert alert-error\">"
             + "<button type=\"button\" class=\"close\" "
             + "data-dismiss=\"alert\">×</button>%s</div>";
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException
     {
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try
         {
             String dbName = (String) request.getSession().getAttribute("dbname");
             String dbAddr = (String) request.getSession().getAttribute("addr");
             String dbUser = (String) request.getSession().getAttribute("user");
             String dbPW = (String) request.getSession().getAttribute("pw");
 
             String query = (String) request.getParameter("query");
             System.out.println("Query: " + query);
             Connection conn = null;
             try
             {
                 String userConnect = "jdbc:mysql://"
                         + dbAddr + "/" + dbName + "?user=" + dbUser
                         + "&password=" + dbPW;
 
                 System.out.println("LOADED DRIVER.");
                 Class.forName("com.mysql.jdbc.Driver").newInstance();
                 conn = DriverManager.getConnection(userConnect);
 
                 System.out.println("Made connection.");
 
                 java.sql.PreparedStatement statement = conn.prepareStatement(query);
                 ResultSet rs = statement.executeQuery();
 
                 StringBuilder sb = new StringBuilder();
                 sb.append("<table id=\"rsTbl\" class=\"table table-striped table-hover table-bordered\">");
                 java.sql.ResultSetMetaData rsMeta = rs.getMetaData();
                 sb.append(this.getTableHeader(rsMeta));
                 sb.append("<tbody>");
                 while(rs.next())
                 {
                     sb.append("<tr>");
                     for(int i = 0; i < rsMeta.getColumnCount(); i++)
                     {
                         sb.append("<td>");
                        if(rsMeta.getColumnTypeName(i + 1).equalsIgnoreCase("blob"))
                            sb.append("[BLOB Not Shown]");
                        else
                            sb.append(rs.getString(i + 1));
                         sb.append("</td>");
                     }
                     sb.append("</tr>");
                 }
                 sb.append("</tbody>");
                 sb.append("</table>");
                 out.println(sb.toString());
 
             }
             catch(SQLException ex)
             {
                 System.out.println(ex.getMessage());
                 out.printf(ERROR_MESSAGE, ex.getMessage());
                 ex.printStackTrace();
             }
             catch(ClassNotFoundException ex)
             {
                 System.out.println(ex.getMessage());
                 out.printf(ERROR_MESSAGE, ex.getMessage());
                 ex.printStackTrace();;
             }
             catch(Exception ex)
             {
                 System.out.println(ex.getMessage());
                 out.printf(ERROR_MESSAGE, ex.getMessage());
                 ex.printStackTrace();
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
                         out.println("<div class=\"alert alert-error\">" + ex.getMessage() + "</div>");
                         ex.printStackTrace();
                     }
                 }
             }
         }
         finally
         {
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
             throws ServletException, IOException
     {
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
             throws ServletException, IOException
     {
         processRequest(request, response);
     }
 
     protected String getTableHeader(java.sql.ResultSetMetaData rsMeta) throws SQLException
     {
         StringBuilder sb = new StringBuilder();
         sb.append("<thead><tr>");
         for(int i = 0; i < rsMeta.getColumnCount(); i++)
         {
             sb.append("<th>");
             sb.append(rsMeta.getColumnName(i + 1));
             sb.append("</th>");
         }
         sb.append("</tr></thead>");
         return sb.toString();
     }
 
     /**
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo()
     {
         return "Short description";
     }// </editor-fold>
 }
