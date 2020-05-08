 package org.softwaresynthesis.mytalk.server.authentication.servlet;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import javax.security.auth.login.LoginContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Verifica l'effettivo logout tramite la servlet dedicata, cui vengono inviate
  * le richieste HTTP POST da parte dei client
  * 
  * @author Diego Beraldin
  */
 public class LogoutServletTest {
 	// oggetto da testare
 	private static LogoutServlet tester;
 	// stub di richiesta HTTP
 	private HttpServletRequest request;
 	// stub di risposta
 	private HttpServletResponse response;
 	// base per verificare il testo della risposta
 	private StringWriter writer;
 
 	/**
 	 * Inizializza l'oggetto da testare in tutti i metodi di test
 	 * 
 	 * @author Diego Beraldin
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() {
 		tester = new LogoutServlet();
 	}
 
 	/**
 	 * Prima di ogni test, ricrea gli stub e azzera il buffer in cui sarà
 	 * memorizzato il testo della risposta
 	 * 
 	 * @author Diego Beraldin
 	 */
 	@Before
 	public void setUp() {
 		request = mock(HttpServletRequest.class);
 		response = mock(HttpServletResponse.class);
 		writer = new StringWriter();
 	}
 
 	/**
 	 * Verifica il corretto logout di un utente autenticato al sistema, vale a
 	 * dire un utente cui è associata una sessione in cui è memorizzato il
 	 * contesto di autenticazione
 	 * 
 	 * @throws IOException
 	 * @throws ServletException
 	 * @author Diego Beraldin
 	 */
 	@Test
 	public void testLogoutCorrectUser() throws IOException, ServletException {
 		// imposta la richiesta per restituire una sessione che ha successo
 		HttpSession successSession = mock(HttpSession.class);
		when(successSession.getAttribute("context")).thenReturn(
				mock(LoginContext.class));
 		when(request.getSession(false)).thenReturn(successSession);
 
 		// configura il comportamento della risposta
 		when(response.getWriter()).thenReturn(new PrintWriter(writer));
 
 		// invoca il metodo da testare
 		tester.doPost(request, response);
 
 		writer.flush();
 		String responseText = writer.toString();
 		assertFalse(responseText.length() == 0);
 		assertEquals(responseText, "true");
 
 	}
 
 	/**
 	 * Verifica il corretto trattamento di una richiesta da parte di un utente
 	 * che non è autenticato al sistema (sessione HTTP non valida)
 	 * 
 	 * @throws IOException
 	 * @throws ServletException
 	 * @author Diego Beraldin
 	 */
 	@Test
 	public void testLogoutWrongUser() throws IOException, ServletException {
 		// imposta la richiesta per restituire una sessione che non ha successo
 		HttpSession failSession = mock(HttpSession.class);
 		// lo stub ritrorna null dunque ha il comportamento desiderato
 		when(request.getSession(false)).thenReturn(failSession);
 
 		// configura il comportamento della risposta
 		when(response.getWriter()).thenReturn(new PrintWriter(writer));
 
 		// invoca il metodo da testare
 		tester.doPost(request, response);
 		
 		// verifica l'output
 		writer.flush();
 		String responseText = writer.toString();
 		assertFalse(responseText.length() == 0);
 		assertEquals(responseText, "false");
 	}
 
 }
