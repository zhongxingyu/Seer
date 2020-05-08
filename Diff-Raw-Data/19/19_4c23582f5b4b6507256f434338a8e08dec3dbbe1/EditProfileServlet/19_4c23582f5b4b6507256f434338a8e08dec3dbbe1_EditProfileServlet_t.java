 package ycp.edu.seniordesign.webapp;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ycp.edu.seniordesign.controller.LoginController;
 import ycp.edu.seniordesign.model.User;
 
 public class EditProfileServlet extends HttpServlet
 {
 private static final long serialVersionUID = 1L;
 	
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
 	{
		if(req.getSession().getAttribute("user") == null)
		{
			req.getRequestDispatcher("/view/login.jsp").forward(req, resp);
		}
		else
		{
			req.getRequestDispatcher("/view/editProfile.jsp").forward(req, resp);
		}
 	}
 	
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
 	{
 		String emailAddress = req.getParameter("newEmailAddressBox");
 		String major = req.getParameter("newMajorBox");
 				
 		if(req.getParameter("ChangeFieldsButton") != null)
 		{
 			try
 			{
 				if (emailAddress != null) {
 					//Set new email address
 				}
 				
 				if (major != null) {
 					//Set new major
 				}
 			}
 			catch(Exception e)
 			{
 				System.out.println("Changing fields failed");
 				e.printStackTrace();
 			}
 		}
 		
 		else if(req.getParameter("ChangePasswordButton") != null)
 		{
 			System.out.println("Changing password");
 			req.getRequestDispatcher("/view/changePassword.jsp").forward(req, resp);
		}	
 	}
 }
