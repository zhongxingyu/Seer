 package org.softwaresynthesis.mytalk.server.authentication.servlet;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.IOException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpSession;
 
 /**
  * Testa la servlet di autenticazione, invocata tramite una richiesta HTTP POST
  * che contiene come parametri lo username e la password di un utente registrato
  * al sistema.
  * 
  * @author Diego Beraldin
  */
 public class LoginServletTest {
 
 	// oggetto da testare
 	private static LoginServlet tester;
 	// stub di richiesta HTTP
 	private HttpServletRequest request;
 	// stub di risposta HTTP
 	private HttpServletResponse response;
 	// base per recuperare il testo della rispota
 	private StringWriter writer;
 
 	/**
 	 * Inizializza l'oggetto da testare (per tutti i metodi di test)
 	 * 
 	 * @author Diego Beraldin
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() {
 		tester = new LoginServlet();
 	}
 
 	/**
 	 * Prima di ogni test, ricrea gli stub di richiesta e risposta e azzera il
 	 * buffer interno del writer che riceverà la risposta
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
 	 * Verifica l'accesso di un utente che è effettivamente registrato
 	 * 
 	 * @throws ServletException
 	 * @throws IOException
 	 * @author Diego Beraldin
 	 */
 	@Test
 	public void testLoginCorrectUser() throws ServletException, IOException {
 		// configura il comportamento della richiesta
 		when(request.getParameter("username")).thenReturn(
 				"indirizzo5@dominio.it");
 		when(request.getParameter("password")).thenReturn("password");
		when(request.getSession(true)).thenReturn(mock(HttpSession.class));
 		// configura il comportamento della risposta
 		when(response.getWriter()).thenReturn(new PrintWriter(writer));
 
 		// invoca il metodo da testare
 		tester.doPost(request, response);
 
 		// verifica l'output della servlet
 		writer.flush();
 		String responseText = writer.toString();
 		assertFalse(responseText.length() == 0);
 		
 		String toCompare = "{\"name\":\"luigi\", \"surname\":\"mannoio\", \"email\":\"indirizzo5@dominio.it\", "
 				+ "\"id\":\"5\", \"picturePath\":\"img/contactImg/Default.png\"}";
 		assertEquals(toCompare, responseText);
 	}
 
 	/**
 	 * Verifica l'impossibilità di accedere al sistema da parte di un utente che
 	 * non è registrato (mediante credenziali non valide)
 	 * 
 	 * @throws ServletException
 	 * @throws IOException
 	 * @author Diego Beraldin
 	 */
 	@Test
 	public void testLoginNotExistUser() throws IOException, ServletException {
 		// configura il comportamento della richiesta
 		when(request.getParameter("username")).thenReturn("dummy@dummy.du");
 		when(request.getParameter("password")).thenReturn("dummy");
		when(request.getSession(true)).thenReturn(mock(HttpSession.class));
 		// configura il comportamento della risposta
 		when(response.getWriter()).thenReturn(new PrintWriter(writer));
 
 		// invoca il metodo da testare
 		tester.doPost(request, response);
 
 		// verifica l'output della servlet
 		writer.flush();
 		String responseText = writer.toString();
 		assertFalse(responseText.length() == 0);
 		assertEquals("null", responseText);
 	}
 
 	/**
 	 * Verifica l'impossibilità di effettuare il login nel caso in cui non siano
 	 * forniti tutti i dati necessari all'autenticazione di un utente
 	 * 
 	 * @throws ServletException
 	 * @throws IOException
 	 * @author Diego Beraldin
 	 */
 	@Test
 	public void testLoginWrongUser() throws IOException, ServletException {
 		// configura il comportamento della richiesta
 		when(request.getParameter("username")).thenReturn("dummy@dummy.du");
 		// configura il comportamento della risposta
 		when(response.getWriter()).thenReturn(new PrintWriter(writer));
 
 		// invoca il metodo da testare
 		tester.doPost(request, response);
 
 		// verifica l'output della servlet
 		writer.flush();
 		String responseText = writer.toString();
 		assertFalse(responseText.length() == 0);
 		assertEquals("null", responseText);
 	}
 
 }
