 package servlets;
 
 import html.HTMLWriter;
 import items.User;
 
 import java.io.IOException;
 
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import database.Database;
 
 /**
  * Denna klassen bygger ut ServletBase och renderar inloggningssidan, via
  * HTMLWriter, där man fyller i användarnamn och lösenord. Den innehåller
  * dessutom funktionaliteten för att logga in och ut ur system och används av
  * alla som försöker ansluta till systemet.
  */
 @WebServlet("/login")
 public class LogIn extends ServletBase {
 
 	private boolean logginError = false;
 
 	/**
 	 * Anropas när /login får ett <code>POST</code> anrop.
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
 		String username = request.getParameter(HTMLWriter.LOGIN_USERNAME);
 		String password = request.getParameter(HTMLWriter.LOGIN_PASSWORD);
 		if (database.login(username, password)) {
 			HttpSession session = request.getSession();
 			User loggedIn = database.getUser(username);
 			session.setAttribute(USER, loggedIn);
 			session.setAttribute(LOGGEDIN, true);
 			try {
 				if (Database.ADMIN.equals(loggedIn.getUsername())) {
 					response.sendRedirect("administration");
 				} else {
 					response.sendRedirect("mainpage");
 				}
 				return;
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		logginError = true;
 
 		doGet(request, response);
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
 		HttpSession session = request.getSession();
 		session.setAttribute(USER, null);
 		session.setAttribute(LOGGEDIN, false);
		session.setAttribute(PROJECT, null);
 
 		try {
 			HTMLWriter writer = new HTMLWriter(response.getWriter());
 			writer.printHead((User) request.getSession().getAttribute(USER));
 			if (logginError) {
 				writer.printErrorMessage("Felaktigt användarnamn eller lösenord!");
 			}
 			doWork(request, writer);
 			writer.printFoot();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	protected void doWork(HttpServletRequest request, HTMLWriter html) {
 		Statistics.done=true;
 		html.printLoginRequestForm();
 	}
 }
