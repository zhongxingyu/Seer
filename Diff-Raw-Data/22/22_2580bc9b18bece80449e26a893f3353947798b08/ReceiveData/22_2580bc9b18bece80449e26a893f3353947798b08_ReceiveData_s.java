 package stressTests;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
 * This servlet receives data from the user and stores it in the db 
  *
  * @author Noam Szpiro
  */
 
 @SuppressWarnings("serial")
 public class ReceiveData extends HttpServlet {
 	private Logger logger = Logger.getLogger(ReceiveData.class.getName()); 
 
     public void doGet(HttpServletRequest request,
                       HttpServletResponse response)
         throws IOException, ServletException
     {
         response.setContentType("text/html");
         PrintWriter out = response.getWriter();
     	out.println("AJS@#$%@#$%@#$%ILQJDFLIQWE ILF{}U@!#*C'!lka\"sdjhfkuayufi HDFKJAYW*EOQRUQ*WRUYIOQWE Q");
         out.close();
     }
     
     public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
         throws IOException, ServletException
 	{
     	logger.finest("inside do post");
     	doGet(request, response);
 	}
 }
