 package org.softwaresynthesis.mytalk.server.authentication.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.security.auth.login.LoginContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Servlet che ha il compito di terminare
  * l'accesso al sistema mytalk di un utente
  * 
  * @author	Andrea Meneghinello
  * @version	1.0
  */
 public final class LogoutServlet extends HttpServlet 
 {
 	private static final long serialVersionUID = 10000L;
 	
 	/**
 	 * Inizializza la servlet
 	 */
 	public LogoutServlet()
 	{
 		super();
 	}
 
 	/**
 	 * Esegue la richiesta di logout ricevuta
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
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
 	{
 		this.doPost(request, response);
 	}
 
 	/**
 	 * Esegue la richiesta di logout ricevuta
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
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
 	{
 		HttpSession session = null;
 		LoginContext context = null;
 		PrintWriter writer = response.getWriter();
 		String result = null;
 		try
 		{
 			session = request.getSession(false);
 			context = (LoginContext)session.getAttribute("context");
 			if (context != null)
 			{
 				context.logout();
 				result = "true";
 			}
 			else
 			{
 				result = "false";
 			}
 		}
 		catch (Exception ex)
 		{
 			result = "false";
 		}
 		finally
 		{
 			if (session != null)
 			{
 				session.invalidate();
 			}
 		}
		writer.write(result);
 	}
 }
