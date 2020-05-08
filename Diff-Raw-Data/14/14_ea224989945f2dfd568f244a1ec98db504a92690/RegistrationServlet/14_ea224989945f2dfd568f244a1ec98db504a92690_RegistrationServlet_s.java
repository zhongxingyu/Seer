 package dashboard.servlet;
 
 import java.io.IOException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import dashboard.error.*;
 import dashboard.registry.StudentRegistry;
 import dashboard.model.Student;
 
 public class RegistrationServlet extends HttpServlet {
 	
 	private static final long serialVersionUID = -5081331444892620046L;
 
 	/**
 	 * Called when a user is trying to register
 	 * @param req
 	 * @param resp
 	 * @throws IOException
 	 */
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		String username = req.getParameter("username");
 		String email = req.getParameter("mail");
 		String firstName = req.getParameter("firstname");
 		String lastName = req.getParameter("lastname");
 		String password = req.getParameter("password");
 		
 		try{
 			StudentRegistry.addUser(new Student(firstName,lastName,username,email,password));//add the user to the list of existing users
 			resp.sendRedirect("/login.jsp?msg=Registration succes!");
 		} catch (UserNameInUseException e){
			resp.sendRedirect("/error.jsp");
 		} catch (InvalidUserNameException e){
			resp.sendRedirect("/error.jsp");
 		} catch (EmailInUseException e){
			resp.sendRedirect("/error.jsp");
 		} catch (InvalidEmailException e){
			resp.sendRedirect("/error.jsp");
 		} catch (InvalidPasswordException e){
			resp.sendRedirect("/error.jsp");
 		}
 	}
 }
