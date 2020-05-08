 /* By:
  * 		Ivo Daniel Venhuizen Correia, no 2008110814
  * 		Joo Pedro Gaioso Barbosa, no 2008111830
  * 
  * Distributed Systems, October 2010 
  */
 
 package serverWeb;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.rmi.AccessException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import users.User;
 import server.ClientOperations;
 
 import clientRMI.Client;
 import clientRMI.RMIClient;
 import clientRMI.ServerOperations;
 
 
 public class WebServer extends HttpServlet{
 
 	private static final long serialVersionUID = 1L;
 	
 	private final String HTML_START = "<html><head></head><body>";
 	private final String HTML_END = "</body></html>";
 
 	private Registry registry;
 	private ClientOperations mainServer;
 	private ServerOperations webClient;
 
 	@Override
 	public void init() throws ServletException
 	{
 		try
 		{
 			//registry = LocateRegistry.getRegistry(Constants.FIRST_RMI_SERVER_PORT);
 			registry = LocateRegistry.getRegistry(12000);
 			mainServer = (ClientOperations) registry.lookup("BetAndUinServer");
 		}catch (AccessException e)
 		{
 			throw new ServletException(e);
 			
 		}
 		catch (RemoteException e)
 		{
 			throw new ServletException(e);
 		}
 		catch (NotBoundException e)
 		{
 			throw new ServletException(e);
 		}
 	}
 
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
 	{
 		String username = request.getParameter("username");
 		String password= request.getParameter("password");
 		String msg = null;
 
 		response.setContentType("text/html");
 		
 		if (username == null)
 		{
 			msg += "name parameter not found";
 		}
 		else
 		{
 			String value = mainServer.clientLogin(username, password, webClient);
 			
 			msg = value;
 
 		}
 
 		RequestDispatcher dispatcher;
 		
 		if (msg.equals("log successful")){
 			webClient = new Client(registry, mainServer, username);
 			HttpSession session = request.getSession(true);
			User userData = new User(username);
		    session.setAttribute("user", userData);
 		    session.setAttribute("status", msg);
 			dispatcher = request.getRequestDispatcher("/Pages/Bet.html");
 			dispatcher.forward(request, response);
 		}
 		else{
 			HttpSession session = request.getSession(true);
 			session.setAttribute("status", msg);
 			dispatcher = request.getRequestDispatcher("/Pages/Login.jsp");
 			dispatcher.forward(request, response);
 		}
 		
 	}
 
 	@Override
 	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
 	{
 		doGet(request, response);
 	}
 }
