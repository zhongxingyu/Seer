 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package processors;
 
 import Data.Admin;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
import java.sql.PreparedStatement;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author seth
  */
 public class StudentDelete extends HttpServlet {
 
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
 	Connection cn;
 	try {
 	    HttpSession session = request.getSession(true);
 	    Admin admin = (Admin)session.getAttribute("admin");
 	    if (!admin.isLoggedIn())
 		throw new Exception("You are not logged in!");
	    int studentId = Integer.parseInt(request.getParameter("id"));
	    cn = dbutils.DBUtils.conn();
 	    
	    PreparedStatement st = cn.prepareStatement("DELETE FROM student "
		    + "WHERE id = ?;");
	    st.setInt(1, studentId);
	    st.executeUpdate();
	    st = cn.prepareStatement("DELETE FROM test_attempt WHERE student_id = ?;");
	    st.setInt(1, studentId);
	    st.executeUpdate();
	    st = cn.prepareStatement("DELETE FROM student_answer WHERE student_id = ?");
	    st.setInt(1, studentId);
	    st.executeUpdate();
	    st.close();	    
	    cn.close();
 	    response.sendRedirect("admin_dashboard.jsp");
	} catch (Exception ex) {
	    out.write(ex.getMessage());
	} finally {
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
