 package org.softwaresynthesis.mytalk.server;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.fail;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.Hashtable;
 import java.util.Set;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.mockito.stubbing.Answer;
 
 /**
  * Verifica della classe {@link ControllerManager}
  * 
  * @author Diego Beraldin
  * @version 2.0
  */
 @RunWith(MockitoJUnitRunner.class)
 public class ControllerManagerTest {
 	private final String operation = "login";
 	private final String className = "org.softwaresynthesis.mytalk.server.authentication.controller.LoginController";
 	@Mock
 	private IController controller;
 	@Mock
 	private HttpServletRequest request;
 	@Mock
 	private HttpServletResponse response;
 	@Mock
 	private ServletConfig configuration;
 	private Writer writer;
 	private ControllerManager tester;
 
 	/**
 	 * Inizializza l'oggetto da sottoporre a verifica prima di tutti i test in
 	 * modo da caricare la mappa interna dei nomi qualificati dei controller.
 	 * 
 	 * @author Diego Beraldin
 	 * @version 2.0
 	 */
 	@Before
 	public void setUp() throws Exception {
 		// configura il comportamento della richiesta
 		when(request.getParameter("operation")).thenReturn(operation);
 		// inizializza il writer
 		writer = new StringWriter();
 		// inizializza il comportamento della risposta
 		when(response.getWriter()).thenReturn(new PrintWriter(writer));
 		// configura il comportamento del mock del controller
 		doAnswer(new Answer<Void>() {
 			@Override
 			public Void answer(InvocationOnMock invocation) {
 				Object[] args = invocation.getArguments();
 				HttpServletResponse mockResponse = (HttpServletResponse) args[1];
 				try {
 					mockResponse.getWriter().write("ciao");
 				} catch (IOException e) {
 					fail(e.getMessage());
 				}
 				return null;
 			}
 		}).when(controller)
 				.execute(any(HttpServletRequest.class), eq(response));
 		// inizializza l'oggetto da testare
		tester = spy(new ControllerManager());
		// inizializza la mappa dei controller
		tester.init(configuration);
 	}
 
 	/**
 	 * Verifica la corretta inizializzazione della servlet e il fatto che, a
 	 * inizializzazione avvenuta, sia possibile ottenere la mappa contenente i
 	 * nomi dei controller. Si verifica inoltre che la mappa ottenuta abbia la
 	 * struttura appropriata rispetto al file .properties a partire dal quale
 	 * deve essere configurata la mappa.
 	 * 
 	 * @author Diego Beraldin
 	 * @version 2.0
 	 */
 	@Test
 	public void testInit() {
 		// invoca il metodo da testare
 		Hashtable<String, String> controllers = tester.getControllers();
 
 		// verifica l'output ottenuto
 		assertNotNull(controllers);
 		assertFalse(controllers.isEmpty());
 		Set<String> keys = controllers.keySet();
 		assertEquals(23, keys.size());
 	}
 
 	/**
 	 * Verifica il comportamento della servlet nel momento in cui viene invocato
 	 * il metodo doPost. In particolare, si verifica che sia estratto il
 	 * parametro 'operation' dalla richiesta HTTP, e che sia invocato
 	 * correttamente il metodo execute sul controller associato all'operazione
 	 * passando a quest'ultimo la richiesta e la risposta con cui è invocato il
 	 * metodo da testare. Inoltre si verifica che sulla risposta HTTP sia
 	 * effettivamente stampata la stringa che il mock del controller utilizzato
 	 * dal test è stato configurato per stampare.
 	 * 
 	 * @author Diego Beraldin
 	 * @version 2.0
 	 */
 	@Test
 	public void testDoPost() {
 		try {
 			// bypassa il metodo createController ;)
 			when(tester.createController(className)).thenReturn(controller);
 
 			// invoca il metodo da testare
 			tester.doPost(request, response);
 
 			// verifica l'output ottenuto
 			writer.flush();
 			String result = writer.toString();
 			assertNotNull(result);
 			assertEquals("ciao", result);
 
 			// verifica il corretto utilizzo dei mock
 			verify(request).getParameter("operation");
 			verify(controller).execute(request, response);
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 	}
 
 	// TODO questo deve essere fatto
 	@Test
 	public void testCreateWebSocket() {
 		fail("Non posso farlo fino a quando non ci sarà il codice");
 	}
 
 	/**
 	 * Verifica il comportamento della classe nel momento in cui viene invocato
 	 * il metodo destroy() su un'istanza della classe {@link ControllerManager}.
 	 * In particolare il test sia assicura che sia impostata a <code>null</code>
 	 * la variabile di istanza che memorizza la mappa che associa le operazioni
 	 * ai nomi dei controller.
 	 * 
 	 * @author Diego Beraldin
 	 * @version 2.0
 	 */
 	@Test
 	public void testDestroy() {
 		// invoca il metodo da testare
 		tester.destroy();
 
 		// verifica che sia ottenuto il risultato desiderato
 		Hashtable<String, String> controllers = tester.getControllers();
 		assertNull(controllers);
 	}
 }
