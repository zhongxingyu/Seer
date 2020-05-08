 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package processors;
 
import com.sun.jndi.cosnaming.CNCtx;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author seth
  */
 public class TestAdd extends HttpServlet {
 
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
 	Connection cn = null;
 	try {
 	    cn = dbutils.DBUtils.conn();
 	    cn.setAutoCommit(false);
 	    String name = ServletUtils.decodeParameter(request.getParameter("name"));
 	    String[] topicIds = request.getParameterValues("topics");
 	    String topics = "";
 	    for (int i = 0; i < topicIds.length; i++) {
 		topics += topicIds[i];
 		if (i < topicIds.length - 1)
 		    topics += ",";
 	    }
 	    int questionCount = Integer.parseInt(request.getParameter("questionCount"));
 	    String type = request.getParameter("type");
 	    boolean isFinal = (type.equals("final"));
 	    
 	    //Create the test -----------------------------------------------------------------
 	    PreparedStatement st = cn.prepareStatement("INSERT INTO test (name, final, questionCount) "
 		    + "VALUES (?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
 	    st.setString(1, name);
 	    st.setBoolean(2, isFinal);
 	    st.setInt(3, questionCount);
 	    st.executeUpdate();
 	    ResultSet rs = st.getGeneratedKeys();
 	    rs.next();
 	    int testId = rs.getInt(1);  
 	    
 	    PreparedStatement stTopics = cn.prepareStatement("INSERT INTO test_topics "
 		    + "(test_id, topic_id) VALUES (?, ?);");
 	    stTopics.setInt(1, testId);
 	    for (String s : topicIds) {
 		stTopics.setInt(2, Integer.parseInt(s));
 		stTopics.executeUpdate();
 	    }
 	    
 	    if (isFinal) {
 		Statement stStudents = cn.createStatement();
 		ResultSet rsStudents = stStudents.executeQuery("SELECT id FROM student;");
 		while (rsStudents.next())
 		    generateSequence(testId, rsStudents.getInt("id"), questionCount, topics, cn);
 	    } else
 		generateSequence(testId, null, questionCount, topics, cn);
 	    
 	    cn.commit();
 	    if (cn != null)
 		cn.close();
 	} catch (SQLException ex) {
 	    Logger.getLogger(TestAdd.class.getName()).log(Level.SEVERE, null, ex);
 	    if (cn != null)
 		try {
 		cn.rollback();
 	    } catch (SQLException ex1) {
 		Logger.getLogger(TestAdd.class.getName()).log(Level.SEVERE, null, ex1);
 	    }
 	} finally {	    
 	    response.sendRedirect("admin_dashboard.jsp");
 	    out.close();
 	}
     }
     
     private static void generateSequence(int testId, Integer studentId, int questionCount, String topicIds, Connection cn) throws SQLException {
 	PreparedStatement st = cn.prepareStatement("INSERT INTO question_sequence (test_id, student_id) "
 		+ "VALUES (?, ?);", Statement.RETURN_GENERATED_KEYS);
 	st.setInt(1, testId);
 	if (studentId != null)
 	    st.setInt(2, studentId);
 	else
 	    st.setNull(2, -1);
 	st.executeUpdate();
 	ResultSet rs = st.getGeneratedKeys();
 	rs.next();
 	int sequenceId = rs.getInt(1);
 	rs.close();
 	st.close();
 		
 	PreparedStatement stQuestions = cn.prepareStatement("INSERT INTO question_sequence_questions (sequence_id, question_id, `order`) "
 		+ "SELECT ?, id, @order := @order + 1 AS `order` FROM "
 		    + "( SELECT id FROM question "
 		    + "WHERE topic_id IN (" + topicIds + ") ORDER BY RAND() LIMIT ?) "
 		+ "qs, (SELECT @order := 0) o");
 	stQuestions.setInt(1, sequenceId);
 	//stQuestions.setString(2, topicIds);
 	stQuestions.setInt(2, questionCount);
 	stQuestions.executeUpdate();
 	stQuestions.close();
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
