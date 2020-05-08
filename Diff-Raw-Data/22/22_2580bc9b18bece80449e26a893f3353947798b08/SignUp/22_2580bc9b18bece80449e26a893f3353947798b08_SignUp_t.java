 package stressTests;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
/**
 * This servlets accepts signups and send data back that is not expected and not valid to the client
 * in order to check if it handles it correctely
 *
 * @author Noam Szpiro
 */
 
 @SuppressWarnings("serial")
 public class SignUp extends HttpServlet {
 	private Logger logger = Logger.getLogger(SignUp.class.getName()); 
 
     public void doGet(HttpServletRequest request,
                       HttpServletResponse response)
         throws IOException, ServletException
     {
     	response.setContentType("text/html");
 	    PrintWriter out = response.getWriter();
 	    out.println("AISDFG@*#&NRC(!@#CR{\"OPP{OIPUHGP'OQI}|{:'>?ASDF:LKASDF");
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
