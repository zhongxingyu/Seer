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
 		HttpSession session = request.getSession();
 		System.out.println(session.getId());
 
 		System.out.println("the value is session is :" +session.getAttribute("test") );
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
 				request.setAttribute("ClientID", clientID);
 				request.setAttribute("Password", password);
 
 				//check db and login
 				FRUModel model = (FRUModel) this.getServletContext().getAttribute("fru"); 
 				
 				if(clientID!=null && password!=null)
 				{
 					try 
 					{
 						session.setAttribute("login", "n");	
 
 						String ClientName = model.validatePassword(clientID, password);
 						if (! ClientName.equals("NotFound"))
 						{
 							session.setAttribute("login", "y");	
 							session.setAttribute("ClientName", ClientName);
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
 				target = "/index.jspx";		
 			}
 			
 		}	
 		
 		
 		System.out.println("login : " + session.getAttribute("login"));
 		System.out.println("errorMsg : " + session.getAttribute("loginError"));
 		request.getRequestDispatcher(target).forward(request, response);
 
 	}
 		
 
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		doGet(request, response);
 	}
 
 }
