 package stressTests;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
 * This servlet sends information to the client that is not expexted and not valid
 * in order to check if the client will handle the errors gracefully
  *
  * @author Noam Szpiro
  */
 
 @SuppressWarnings("serial")
 public class SendData extends HttpServlet {
 	
     public void doGet(HttpServletRequest request,
                       HttpServletResponse response)
         throws IOException, ServletException
     {
     	response.setContentType("text/html");
 	    PrintWriter out = response.getWriter();
 	    out.println("AISDFG@*#&NRC(!@#CR{\"OPP{OIPUHGP'OQI}|{:'>?ASDF:LKASDF");
 	    out.close();
     }
 }
