 package org.softwaresynthesis.mytalk.server.authentication.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.security.auth.login.LoginContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.softwaresynthesis.mytalk.server.abook.IUserData;
 import org.softwaresynthesis.mytalk.server.dao.UserDataDAO;
 import org.softwaresynthesis.mytalk.server.authentication.AESAlgorithm;
 import org.softwaresynthesis.mytalk.server.authentication.AuthenticationData;
 import org.softwaresynthesis.mytalk.server.authentication.CredentialLoader;
 import org.softwaresynthesis.mytalk.server.authentication.IAuthenticationData;
 import org.softwaresynthesis.mytalk.server.authentication.ISecurityStrategy;
 
 /**
  * Servlet cha ha il compito di autorizzare l'accesso
  * al sistema mytalk richiesto da un utente
  * 
  * @author 	Andrea Meneghinello
  * @version	1.0
  */
 public final class LoginServlet extends HttpServlet 
 {
 	private static final long serialVersionUID = 10001L;
 	
 	/**
 	 * Crea la servlet inizializzandole
 	 * i parametri per il corretto funzionamento
 	 */
 	public LoginServlet()
 	{
 		super();
 		String path = System.getenv("MyTalkConfiguration");
 		path += "\\MyTalk\\Conf\\LoginConfiguration.conf";
 		System.setProperty("java.security.auth.login.config", path);
 	}
 	
 	/**
 	 * Esegue la richiesta di login per un utente ricevuta
 	 * tramite richiesta HTTP GET
 	 * 
 	 * @param	request		contiene i parametri di input
 	 * 						(username, password) per il
 	 * 						corretto svolgimento dell'operazione	
 	 * @param	response	contiene le risposte prodotte dalla
 	 * 						servlet che verranno inviate ai client
 	 * @throws	{@link IOException} se si verificano errori durante
 	 * 			operazioni di IO
 	 * @throws	{@link ServletException} se si verificano errori
 	 * 			interni alla servlet
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
 	{
 		this.doPost(request, response);
 	}
 	
 	/**
 	 * Esegue la richiesta di login per un utente ricevuta
 	 * tramite richiesta HTTP POST
 	 * 
 	 * @param	request		contiene i parametri di input
 	 * 						(username, password) per il
 	 * 						corretto svolgimento dell'operazione	
 	 * @param	response	contiene le risposte prodotte dalla
 	 * 						servlet che verranno inviate ai client
 	 * @throws	{@link IOException} se si verificano errori durante
 	 * 			operazioni di IO
 	 * @throws	{@link ServletException} se si verificano errori
 	 * 			interni alla servlet
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
 	{
 		ISecurityStrategy algorithm = null;
 		CredentialLoader loader = null;
 		HttpSession session = null;
 		IAuthenticationData credential = null;
 		IUserData user = null;
 		LoginContext context = null;
 		PrintWriter writer = null;
 		String password = null;
 		String result = null;
 		String username = null;
 		UserDataDAO userDAO = null;
 		try
 		{
 			session = request.getSession(true);
 			username = request.getParameter("username");
 			password = request.getParameter("password");
 			credential = new AuthenticationData(username, password);
 			algorithm = new AESAlgorithm();
 			loader = new CredentialLoader(credential, algorithm);
 			context = new LoginContext("Configuration", loader);
 			context.login();
 			userDAO = new UserDataDAO();
 			user = userDAO.getByEmail(username);
 			if (user != null)
 			{
 				session.setAttribute("context", context);
 				session.setAttribute("username", user.getMail());
 				result = user.toJson();
 			}
 			else
 			{
 				result = "null";
 			}
 		}
 		catch (Exception ex)
 		{
 			if (session != null)
 			{
 				session.invalidate();
 			}
 			result = "null";
 		}
 		finally
 		{
 			context = null;
 			credential = null;
 			loader = null;
 			writer = response.getWriter();
 			writer.write(result);
 		}
 	}
 }
