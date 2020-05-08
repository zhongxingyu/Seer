 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package processors;
 
 import com.mysql.jdbc.Statement;
 import java.io.*;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.Part;
 
 /**
  *
  * @author seth
  */
 public class QuestionsUpload extends HttpServlet {
    private final String REGEX_NEWLINE = "(\\r)?\\n";
    private final String REGEX_DOUBLE_NEWLINE = "((\\r)?\\n){2,}";
 
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
 	int topicId = -1;
         try {
             String questionList = null;
             for (Part p : request.getParts()) {
                 String value = readStream(p.getInputStream());
		out.write(value);
                 if (p.getName().equals("questionListFile")) {
                     questionList = value;
                 } else if (p.getName().equals("topicId")) {
                     topicId = Integer.parseInt(value);
                 }
             }
            if (questionList != null && topicId > 0) {
                 addQuestions(topicId, questionList);
		response.sendRedirect("question_list.jsp?topic=" + Integer.toString(topicId));
	    }
         } catch (Exception ex) {
             out.println(ex.getMessage());
         } finally {
             out.close();	    
         }
     }
 
     private String readStream(InputStream strm) throws IOException {
         StringBuilder sb = new StringBuilder();
         BufferedReader rdr = new BufferedReader(new InputStreamReader(strm, "UTF-8"));
         char[] buf = new char[2048];
         for (int length = 0; (length = rdr.read(buf)) > 0;) {
             sb.append(buf, 0, length);
         }
         rdr.close();
         return sb.toString();
     }
 
     private void addQuestions(int topicId, String questionList) throws SQLException {
         Connection cn = dbutils.DBUtils.conn();
         
         String[] questionBlocks = questionList.split(REGEX_DOUBLE_NEWLINE);
         for (String s : questionBlocks)
             addQuestion(topicId, s, cn);
         
         cn.close();
     }
     
     private void addQuestion(int topicId, String questionBlock, Connection cn) {
         String[] lines = questionBlock.split(REGEX_NEWLINE);
         String question = lines[0];
 	try {
 	    PreparedStatement stCheckQuestionExistence = cn.prepareStatement("SELECT "
 		    + "CASE WHEN EXISTS (SELECT * FROM question WHERE topic_id = ? AND text = ?) "
 		    + "THEN 1 ELSE 0 END AS questionExists;");
 	    stCheckQuestionExistence.setInt(1, topicId);
 	    stCheckQuestionExistence.setString(2, question);
 	    ResultSet rsQuestionExistence = stCheckQuestionExistence.executeQuery();
 	    rsQuestionExistence.next();
 	    boolean questionExists = rsQuestionExistence.getBoolean("questionExists");
 	    rsQuestionExistence.close();
 	    stCheckQuestionExistence.close();
 	    if (questionExists)
 		return;
 	} catch (SQLException ex) {
 	    Logger.getLogger(QuestionsUpload.class.getName()).log(Level.SEVERE, null, ex);
 	}
         String comment = null;
         boolean hasComment = (lines[lines.length-1].startsWith("//"));
         boolean isMultiSelect = false;
         int correctQuestionCount = 0;        
         String[] answers = Arrays.copyOfRange(lines, 1, lines.length-(hasComment ? 1 : 0));
         if (hasComment)
             comment = lines[lines.length-1].substring(2);
         
         for (int i = 0; i < answers.length; i++) {
             String answer = answers[i];
             if (answer.startsWith("*"))
                 correctQuestionCount++;
             if (correctQuestionCount > 1) {
                 isMultiSelect = true;
                 break;
             }
         }
         try {
             PreparedStatement stQuestion = cn.prepareStatement("INSERT INTO question "
                     + "(topic_id, text, comment, multiselect) "
                     + "VALUES (?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
             stQuestion.setInt(1, topicId);
             stQuestion.setString(2, question);
             stQuestion.setString(3, comment);
             stQuestion.setBoolean(4, isMultiSelect);
             stQuestion.executeUpdate();
 	    ResultSet rs = stQuestion.getGeneratedKeys();
 	    rs.next();
 	    int questionId = rs.getInt(1);
             
             PreparedStatement stAnswer = cn.prepareStatement("INSERT INTO answer "
                     + "(question_id, text, correct) "
                     + "VALUES (?, ?, ?);");
             for (int i = 0; i < answers.length; i++) {
                 String answer = answers[i];
                 boolean isCorrect = answer.startsWith("*");
                 if (isCorrect)
                     answer = answer.substring(1);
                 stAnswer.setInt(1, questionId);
                 stAnswer.setString(2, answer);
                 stAnswer.setBoolean(3, isCorrect);
                 stAnswer.execute();
             }
 	    stAnswer.close();
 	    stQuestion.close();
         
         } catch (SQLException ex) {
             Logger.getLogger(QuestionsUpload.class.getName()).log(Level.SEVERE, null, ex);
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
