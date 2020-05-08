 package bigmac;
  
 import java.io.*;
 import javax.servlet.*;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.*;
 import java.sql.*;
 @WebServlet("/dorun")
 public class PostCandidateServlet extends HttpServlet {
  
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
       // Set the response message's MIME type
       response.setContentType("text/html; charset=UTF-8");
       
       try {
          // Prepare the values
          String SQLString;
     	  
    	 int person =  137;
          String party = request.getParameter("erakonnad");
          String region = request.getParameter("piirkond");
          if (party !=null && region != null){
 	         DB db = new DB();
 	         db.connect();
 	         
 	         SQLString = "INSERT INTO candidate (person,party,region) VALUES ('" +
 	         	person + "','" + party + "','" + region + "');";
 	         System.out.println(SQLString);
 	         Statement stat = db.getConnection().createStatement();
 	         stat.executeUpdate(SQLString);
 	        db.disconnect();
 	        String success= "Olete edukalt kandidaadiks lisatud!";
 	        request.setAttribute("message", success);
 	        request.getRequestDispatcher("/message.jsp").forward(request, response);
          }
       } catch (SQLException e) {
     	  throw new RuntimeException("Statement execution failed miserably!", e);
       }
 
    }
 }
