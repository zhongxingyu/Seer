 package org.softwaresynthesis.mytalk.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.softwaresynthesis.mytalk.server.authentication.security.AESAlgorithm;
 import org.softwaresynthesis.mytalk.server.authentication.security.ISecurityStrategy;
 import org.softwaresynthesis.mytalk.server.dao.DataPersistanceManager;
 
 /**
  * Implementa la struttura di esecuzione di
  * ogni singolo controller.
  * 
  * @author 	Andrea Meneghinello
  * @version 3.0
  */
 public abstract class AbstractController implements IController
 {
 	private String userMail;
 	
 	@Override
 	public final void execute(HttpServletRequest request, HttpServletResponse response) throws IOException
 	{
 		PrintWriter writer = null;
 		if (this.check(request))
 		{
 			this.doAction(request, response);
 		}
 		else
 		{
 			writer = response.getWriter();
 			writer.write("null");
 		}
 	}
 	
 	/**
 	 * Verifica se l'utente è autenticato presso il
 	 * sistema e possa di conseguenza portare a termine
 	 * l'operazione richiesta
 	 * 
 	 * @param 	request	{@link HttpServletRequest} richiesta giunta dal client
 	 * @return	true se l'utente è autorizzato a procedere, false altrimenti
 	 */
 	protected boolean check(HttpServletRequest request)
 	{
 		boolean result = false;
 		HttpSession session = request.getSession(false);
 		if (session != null)
 		{
 			String mail = (String)session.getAttribute("username");
 			if (mail != null && mail.equals("") == false)
 			{
				this.userMail = mail;
 				result = true;
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * Esegue l'operazione richiesta
 	 * 
 	 * @param 	request		{@link HttpServletRequest} parametri in input dall'utente
 	 * @param 	response	{@link HttpServletResponse} parametri in output dal controller
 	 */
 	protected abstract void doAction(HttpServletRequest request, HttpServletResponse response) throws IOException;
 	
 	/**
 	 * Restituisce l'indirizzo mail dell'utente che
 	 * ha fatto il login
 	 * 
 	 * @return	{@link String} indirizzo mail dell'utente
 	 * 			autenticato
 	 */
 	protected String getUserMail()
 	{
 		return this.userMail;
 	}
 	
 	/**
 	 * Metodo factory per la creazione di un punto di
 	 * accesso verso il database del sistema MyTalk
 	 * 
 	 * @return	{@link DataPersistanceManager} restituisce
 	 * 			un punto di accesso alla base di dati
 	 */
 	protected DataPersistanceManager getDAOFactory()
 	{
 		return new DataPersistanceManager();
 	}
 	
 	/**
 	 * Metodo factory per la creazione dell'algoritmo
 	 * di crittografia usato durante la procedura di
 	 * login
 	 * 
 	 * @return	{@link ISecurityStrategy} algoritmo di
 	 * 			crittografia
 	 */
 	protected ISecurityStrategy getSecurityStrategyFactory()
 	{
 		return new AESAlgorithm();
 	}
 }
