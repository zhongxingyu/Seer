 package ufly.frs;
 
 import java.io.IOException;
 import java.util.Enumeration;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import ufly.entities.Customer;
 import ufly.entities.User;
 
 @SuppressWarnings("serial")
 public class UflyServlet extends HttpServlet {
 	public User getLoggedInUser(HttpSession s)
 	{
 		User localUser =null;
 		String email = (String) s.getAttribute("loggedInUser");
 		if (email == null)
 			return null;
 		localUser = Customer.getCustomer(email);
 		//add check for admin if localuser is null
 		return localUser;			
 	}
 	public void login(String email,HttpSession s)
 	{
 		s.setAttribute("loggedInUser", email);	
 	}
 	/**
 	 *  print out GET/POST key/values, Useful for debugging
 	 * @param req
 	 * @param resp
 	 * @throws IOException 
 	 */
 	public void printParam(HttpServletRequest req, HttpServletResponse resp) throws IOException
 	{
 		Enumeration e=req.getParameterNames();
 		String s;
 		for( ; e.hasMoreElements();)
 		{
 			s=(String)e.nextElement();
 			
 			resp.getWriter().println(s+"="+((String)req.getParameter(s)=="" ?"_EMPTY_":(String)req.getParameter(s) ));
 			
 		}
 	}
 }
