 package org.softwaresynthesis.mytalk.server.authentication.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.softwaresynthesis.mytalk.server.abook.Group;
 import org.softwaresynthesis.mytalk.server.abook.IGroup;
 import org.softwaresynthesis.mytalk.server.abook.IUserData;
 import org.softwaresynthesis.mytalk.server.abook.UserData;
 import org.softwaresynthesis.mytalk.server.authentication.AESAlgorithm;
 import org.softwaresynthesis.mytalk.server.authentication.ISecurityStrategy;
 import org.softwaresynthesis.mytalk.server.dao.GroupDAO;
 import org.softwaresynthesis.mytalk.server.dao.UserDataDAO;
 
 /**
  * Servlet cha ha il compito di registrare
  * un nuovo utente nel sistema mytalk
  * 
  * @author 	Andrea Meneghinello
  * @version	1.0
  */
 public final class RegisterServlet extends HttpServlet 
 {
 	private static final long serialVersionUID = 10002L;
 	
 	/**
 	 * Inizializza la servlet
 	 */
 	public RegisterServlet()
 	{
 		super();
 	}
 	
 	/**
 	 * Esegue la richiesta di registrazione per un nuovo utente
 	 * ricevuta tramite richiesta HTTP GET
 	 * 
 	 * @param	request		contiene i parametri di input
 	 * 						per il corretto svolgimento dell'operazione	
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
 	 * Esegue la richiesta di registrazione per un nuovo utente
 	 * ricevuta tramite richiesta HTTP POST
 	 * 
 	 * @param	request		contiene i parametri di input
 	 * 						per il corretto svolgimento dell'operazione	
 	 * @param	response	contiene le risposte prodotte dalla
 	 * 						servlet che verranno inviate ai client
 	 * @throws	{@link IOException} se si verificano errori durante
 	 * 			operazioni di IO
 	 * @throws	{@link ServletException} se si verificano errori
 	 * 			interni alla servlet
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
 	{
 		GroupDAO groupDAO = null;
 		IGroup group = null;
 		ISecurityStrategy strategy = null;
 		IUserData user = null;
 		PrintWriter writer = null;
 		String answer = null;
 		String mail = null;
 		String name = null;
 		String password = null;
 		String path = null;
 		String question = null;
 		String result = null;
 		String surname = null;
 		UserDataDAO userDAO = null;
 		try
 		{
 			strategy = new AESAlgorithm();
 			mail = request.getParameter("username");
 			password = request.getParameter("password");
 			password = strategy.encode(password);
 			question = request.getParameter("question");
 			answer = request.getParameter("answer");
 			answer = strategy.encode(answer);
 			name = this.getParameter(request, "name");
 			surname = this.getParameter(request, "surname");
 			path = this.getParameter(request, "picturePath");
 			user = new UserData();
 			user.setMail(mail);
 			user.setPassword(password);
 			user.setQuestion(question);
 			user.setAnswer(answer);
 			user.setName(name);
 			user.setSurname(surname);
 			group = new Group();
 			group.setOwner(user);
 			group.setName("addrBookEntry");
 			if (path.equals("") == true)
 			{
 				path = "img/contactImg/Default.png";
 			}
 			else
 			{
 				//TODO salvare l'immagine nel server e memorizzare la path
 			}
 			groupDAO = new GroupDAO();
 			userDAO = new UserDataDAO();
 			userDAO.insert(user);
 			groupDAO.insert(group);
 			result = "true";
 		}
 		catch (Exception ex)
 		{
 			result = "false";
 		}
		writer = response.getWriter();
		writer.write(result);
 	}
 	
 	private String getParameter(HttpServletRequest request, String parameterName)
 	{
 		String result = request.getParameter(parameterName);
 		if (result == null)
 		{
 			result = "";
 		}
 		return result;
 	}
 }
