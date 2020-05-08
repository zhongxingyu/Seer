 package ca.wasabistudio.chat.rs;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.core.Response;
 
 import static org.testng.Assert.*;
 
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import ca.wasabistudio.chat.dto.MessageDTO;
 import ca.wasabistudio.chat.entity.Client;
 import ca.wasabistudio.chat.entity.Room;
 import ca.wasabistudio.chat.support.Session;
 
 public class TestRoomResource {
 
 	private ClassPathXmlApplicationContext context;
 	private EntityManagerFactory emf;
 	private HttpSession httpSession;
 
 	@BeforeMethod
 	public void setup() {
 		String[] paths = new String[] {
 				"META-INF/spring-jpa.xml",
 				"META-INF/connector.xml",
 				"META-INF/support.xml",
 				"META-INF/services.xml"
 		};
 		httpSession = new MockHttpSession("testSession");
 		context = new ClassPathXmlApplicationContext(paths);
 		emf = (EntityManagerFactory)context.getBean(EntityManagerFactory.class);
 		EntityManager em = emf.createEntityManager();
 		em.getTransaction().begin();
 		Client client = new Client("moneycash");
 		client.setChatSessionId(httpSession.getId());
 		em.persist(client);
 		Room room = new Room("room");
		Session session = context.getBean(Session.class);
 		session.setClient(client);
 		em.persist(room);
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	@AfterMethod
 	public void tearDown() {
 		emf.close();
 		context.close();
 	}
 
 	@Test
 	public void testJoinRoom() {
		RoomResource resource = context.getBean(RoomResource.class);
 		resource.joinRoom("room", new MockHttpServletRequest(httpSession));
 
 		EntityManager em = emf.createEntityManager();
 		em.getTransaction().begin();
 		Room room = em.find(Room.class, "room");
 		Client client = em.find(Client.class, "moneycash");
 		assertTrue(room.getClients().contains(client));
 		assertNotNull(client.getRoomSetting(room));
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testGetMessages() {
 		// join room first
		RoomResource resource = context.getBean(RoomResource.class);
 		resource.joinRoom("room", new MockHttpServletRequest(httpSession));
 
 		// start a request to get messages...
 		HttpServletRequest mockRequest = new MockHttpServletRequest(httpSession);
 		MockAsyncResponse mockResponse = new MockAsyncResponse();
 		resource.getMessagesAsync("room", mockRequest, mockResponse);
 
		// add message
 		MessageDTO message = new MessageDTO();
 		message.setBody("test message");
		resource.addMessage("room", message, mockRequest);
 
 		// validate result now
 		try {
 			Thread.sleep(500);
 			assertTrue(mockResponse.isResponseSet());
 			Response response = mockResponse.getResponse();
 			List<MessageDTO> messages = (List<MessageDTO>)response.getEntity();
 			assertEquals(messages.size(), 1);
 		} catch (InterruptedException exception) {
 			throw new RuntimeException(exception);
 		}
 	}
 
 }
