 package org.softwaresynthesis.mytalk.server.authentication.controller;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.security.auth.login.LoginContext;
 import javax.security.auth.login.LoginException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.softwaresynthesis.mytalk.server.AbstractController;
 import org.softwaresynthesis.mytalk.server.abook.IUserData;
 import org.softwaresynthesis.mytalk.server.authentication.CredentialLoader;
 import org.softwaresynthesis.mytalk.server.authentication.security.AESAlgorithm;
 import org.softwaresynthesis.mytalk.server.authentication.security.ISecurityStrategy;
 import org.softwaresynthesis.mytalk.server.dao.DataPersistanceManager;
 
 /**
  * Controller che gestisce il login 
  * al sistema Mytalk
  * 
  * @author 	Marco Schivo
  * @version 3.0
  */
 public final class LoginController extends AbstractController
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
 		CredentialLoader loader = null;
 		DataPersistanceManager dao = null;
 		HttpSession session = null;
 		ISecurityStrategy strategy = null;
 		IUserData user = null;
 		LoginContext context = null;
 		PrintWriter writer = null;
 		String email = null;
 		String result = null;		
 		try
 		{
 			strategy = super.getSecurityStrategyFactory();
 			loader = this.loaderFactory(request, strategy);
 			context = this.contextFactory("Configuration", loader);
 			context.login();
 			dao = super.getDAOFactory();
 			email = request.getParameter("username");
 			user = dao.getUserData(email);
 			if(user != null)
 			{
 				session = request.getSession(true);
 				session.setAttribute("context", context);
 				session.setAttribute("username", user.getMail());
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
 	
 	/**
 	 * Metodo factory per la creazione dell'algoritmo
 	 * di crittografia usato durante la procedura di
 	 * login
 	 * 
 	 * @return	{@link ISecurityStrategy} algoritmo di
 	 * 			crittografia
 	 */
 	ISecurityStrategy strategyFactory()
 	{
 		return new AESAlgorithm();
 	}
 	
 	/**
 	 * Metodo factory per la creazione del caricatore
 	 * per le credenziali utente
 	 * 
 	 * @param 	request 	{@link HttpServletRequest} parametri di input
 	 * 						con i dati forniti dall'utente
 	 * @param 	strategy	{@link ISecurityStrategy} algoritmo di crittografia
 	 * @return
 	 */
 	CredentialLoader loaderFactory(HttpServletRequest request, ISecurityStrategy strategy)
 	{
 		return new CredentialLoader(request, strategy);
 	}
 	
 	/**
 	 * Metodo factory per la creazione del contesto per
 	 * la procedura di login
 	 * 
 	 * @param 	ruleName	{@link String} nome del contesto da istanziare
 	 * @param 	loader		{@link CredentialLoader} caricatore per le credenziali
 	 * @return
 	 * @throws 	LoginException	se non si riesce a configurare l'ambiente per
 	 * 							il login
 	 */
 	LoginContext contextFactory(String ruleName, CredentialLoader loader) throws LoginException
 	{
 		return new LoginContext(ruleName, loader);
 	}
 }
