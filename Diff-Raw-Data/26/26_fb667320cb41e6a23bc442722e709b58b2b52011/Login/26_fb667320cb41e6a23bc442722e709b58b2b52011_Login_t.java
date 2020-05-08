 package ctrl;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import model.FRUModel;
 
 /**
  * Servlet implementation class Login, used for login 
  */
 @WebServlet("/Login")
 public class Login extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public Login() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String login = request.getParameter("login");
 		String target;
 
 		if (login==null)
 		{
 			target = "/login.jspx";
 		}
 		else
 		{
 			if (login.equals("login"))
 			{
 				String clientID = request.getParameter("ClientID");
 				String password =request.getParameter("Password");
 				
 				//check db and login
 				FRUModel model = (FRUModel) this.getServletContext().getAttribute("fru"); 
 				
 				if(clientID!=null && password!=null)
 				{
 					try 
 					{
 						request.setAttribute("login", "n");	
 
 						String ClientName = model.validatePassword(clientID, password);
 						if (! ClientName.equals("NotFound"))
 						{
 							request.setAttribute("login", "y");	
 							request.setAttribute("ClientName", ClientName);
 							target = "/index.jspx";
 						}
 						else
 						{
 							request.setAttribute("loginError", "Login credential is incorrect");
 							target = "/login.jspx";
 
 						}
 					} 
 					catch (Exception e) 
 					{
 						e.printStackTrace();
 						request.setAttribute("loginError", e.getMessage());
 						target = "/login.jspx";
 
 					}
 
 				}
 				else
 				{
 					request.setAttribute("loginError", "ClientID or Password can not be empty");
 					target = "/login.jspx";
 				}
 			}
 			
 			else
 			{
				System.out.println("check logout");	
				target = "/index.jspx";		
 			}
 		}	
 		request.getRequestDispatcher(target).forward(request, response);
 
 	}
 		
 
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		doGet(request, response);
 	}
 
 }
