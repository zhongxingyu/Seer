 // Using Session
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.*;
 
 public class RegistrationServlet extends HttpServlet {
 
 	public void doPost( HttpServletRequest request,
 						HttpServletResponse response )
 		throws ServletException, IOException {
 		PrintWriter output;
 		  
 		String fname = request.getParameter( "fname" );
 		String sname = request.getParameter( "sname" );
 		String option = request.getParameter( "option" );
 		String address;
 		
 		HttpSession session = request.getSession(true);
 		
		if ( fname.length() == 0 || sname.length() == 0 || option == null ) {
 
 			response.sendRedirect("http://localhost:8080/bobcat/?error=1");
 
 		}
 
 		else { 
 			session.setAttribute( "fname", fname );
 			session.setAttribute( "sname", sname );
 		  
 			response.setContentType( "text/html" );
 		  
 
 			if ( option.equals("books") ) {
 			
 				address="WEB-INF/books.jsp";
 			
 			}
 			
 			else {
 				
 				address="WEB-INF/records.jsp";
 				
 			}
 			/*
 				output = response.getWriter();
 				output.println("<html><body>");
 			output.println("<p>name:"+fname);
 			output.println("</body></html>");
 			*/
 			RequestDispatcher dispatcher =
 			request.getRequestDispatcher(address);
 			dispatcher.forward(request, response);
 		}
 	}
 }
