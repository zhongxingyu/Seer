 package org.softwaresynthesis.mytalk.server.authentication.controller;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.security.auth.login.LoginContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.softwaresynthesis.mytalk.server.AbstractController;
 import org.softwaresynthesis.mytalk.server.abook.IUserData;
 import org.softwaresynthesis.mytalk.server.authentication.CredentialLoader;
 import org.softwaresynthesis.mytalk.server.authentication.security.AESAlgorithm;
 import org.softwaresynthesis.mytalk.server.dao.DataPersistanceManager;
 
 /**
  * Controller che gestisce il login 
  * al sistema Mytalk
  * 
  * @author 	Marco Schivo
  * @version 3.0
  */
public class LoginController extends AbstractController
 {
 	/**
 	 * Inizializza il controller per eseguire
 	 * l'operazione di login
 	 */
 	public LoginController()
 	{
 		super();
 		String path = System.getenv("MyTalkConfiguration");
 		String separator = System.getProperty("file.separator");
 		path += separator + "MyTalk" + separator + "Conf" + separator + "LoginConfiguration.conf";
 		System.setProperty("java.security.auth.login.config", path);
 	}
 	
 	/**
 	 * Esegue la richiesta di login nel sistema MyTalk 
 	 * 
 	 * @author 	Marco Schivo
 	 * @version 3.0
 	 */
 	@Override
 	protected void doAction(HttpServletRequest request,	HttpServletResponse response) throws IOException
 	{
 		AESAlgorithm algorithm = null;
 		CredentialLoader loader = null;
 		DataPersistanceManager dao = null;
 		HttpSession session = null;
 		IUserData user = null;
 		LoginContext context = null;
 		PrintWriter writer = null;
 		String email = null;
 		String result = null;		
 		try
 		{
 			algorithm = new AESAlgorithm();
 			loader = new CredentialLoader(request, algorithm);
 			context = new LoginContext("Configuration", loader);
 			context.login();
 			dao = new DataPersistanceManager();
 			email = request.getParameter("username");
 			user = dao.getUserData(email);
 			if(user != null)
 			{
 				session = request.getSession(true);
 				session.setAttribute("context", context);
 				session.setAttribute("username", user.getMail());
 				this.setUserMail(user.getMail());
 				result = "{\"name\":\"" + user.getName() + "\"";
 				result += ", \"surname\":\"" + user.getSurname() + "\"";
 				result += ", \"email\":\"" + user.getMail() + "\"";
 				result += ", \"id\":\"" + user.getId() + "\"";
 				result += ", \"picturePath\":\"" + user.getPath() + "\"}";
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
 			loader = null;
 			email = null;
 			writer = response.getWriter();
 			writer.write(result);
 		}
 	}
 	
 	/**
 	 * Ridefinizione metodo check 
 	 * 
 	 * @author 	Marco Schivo
 	 * @version 3.0
 	 */
 	@Override
 	protected boolean check(HttpServletRequest request)
 	{
 		return true;
 	}
 }
