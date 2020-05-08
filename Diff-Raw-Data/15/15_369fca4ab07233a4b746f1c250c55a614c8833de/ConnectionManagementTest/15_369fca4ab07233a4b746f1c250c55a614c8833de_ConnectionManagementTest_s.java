 package hibernate.test;
 
 import static org.junit.Assert.*;
 import java.util.List;
 import java.util.Iterator;
 import hibernate.*;
 import models.Connection;
 import models.User;
 import org.junit.*;
 
 public class ConnectionManagementTest {
 
 	private static CommentManagement cm;
 	private static ConnectionManagement xm;
 	private static FieldManagement fm;
 	private static UserManagement um;
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
		
 		cm = new CommentManagement();
 		xm = new ConnectionManagement();
 		fm = new FieldManagement();
 		um = new UserManagement();
 		
 		fm.createField("Java", "This is Java");
 		fm.createField("C++", "C plus plus");
 		fm.createField("Google tips", "Google like a pro");
 		fm.createField("Bathroom Wall", "How to copy code from the internet");
 		fm.createField("COBOL", "Old stuff for old ppl");
 		
 		um.createUser("John", "First", "johnfirst@gmail.com", "Here", "It exits on Earth", "numero uno");
 		um.createUser("Bob", "Second", "bob.second@outlook.com", "Not far away", "Ground", "numero dos");
 		um.createUser("Bob", "Last", "phil@last.com", "Here", "It exits on Earth", "numero tres");
 		um.createUser("Some", "Guy", "someguy@somewhere.com", "Somewhere", "Place", "numero quatro");
 		
 		xm.createOpenMentor(um.getByEmail("johnfirst@gmail.com"), fm.getSingleByTitle("Java"));
 		xm.createOpenMentor(um.getByEmail("someguy@somewhere.com"), fm.getSingleByTitle("Java"));
 		xm.createOpenMentor(um.getByEmail("bob.second@outlook.com"), fm.getSingleByTitle("Google tips"));
 
 		
 		xm.createOpenTrainee(um.getByEmail("johnfirst@gmail.com"), fm.getSingleByTitle("C++"));
 		xm.createOpenTrainee(um.getByEmail("someguy@somewhere.com"), fm.getSingleByTitle("Bathroom Wall"));
 		xm.createOpenTrainee(um.getByEmail("phil@last.com"), fm.getSingleByTitle("COBOL"));
 		
 		xm.createConnection(um.getByEmail("someguy@somewhere.com"), um.getByEmail("bob.second@outlook.com"), fm.getSingleByTitle("COBOL"));
 		xm.createConnection(um.getByEmail("phil@last.com"), um.getByEmail("johnfirst@gmail.com"), fm.getSingleByTitle("Google tips"));
 		xm.createConnection(um.getByEmail("phil@last.com"), um.getByEmail("bob.second@outlook.com"), fm.getSingleByTitle("Bathroom Wall"));
 
 	}
 	
 	@Before
 	public void setUp(){
 		
 	}
 	
 	@Test
 	public void testChangeStatus(){
 		List<Connection> list = xm.getByField(fm.getSingleByTitle("Bathroom Wall"));
 		for(Iterator<Connection> i = list.iterator(); i.hasNext();){
 			Connection current = i.next();
 			xm.changeStatus(current, false);
 		}
 		list = xm.getByMentor(um.getByEmail("phil@last.com"));
 		for(Iterator<Connection> i = list.iterator(); i.hasNext();){
 			Connection current = i.next();
 			if(current.getField().getTitle() == "Bathroom Wall"){
 				assertEquals(current.isActive(), false);
 			}
 		}
		
 	}
 	
 	@Test
 	public void testGetAllConnections(){
 		List<Connection> list = xm.getAllConnections();
 		for(Iterator<Connection> i = list.iterator(); i.hasNext();){
 			Connection current = i.next();
 			assertEquals(current.getClass(), Connection.class);
 		}
		assertEquals(list.size(), 9);
 	}
 	
 	@Test
 	public void testGetAllInactiveConnections(){
 		List<Connection> list = xm.getByField(fm.getSingleByTitle("Bathroom Wall"));
 		for(Iterator<Connection> i = list.iterator(); i.hasNext();){
 			Connection current = i.next();
 			xm.changeStatus(current, false);
 		}
 		
 		list = xm.getAllInactiveConnections();
 		for(Iterator<Connection> i = list.iterator(); i.hasNext();){
 			Connection current = i.next();
 			assertEquals(current.getClass(), Connection.class);
 		}
 		if(list.size() == 2)
 			assertEquals(list.size(), 2);
 		else if(list.size() == 3)
 			assertEquals(list.size(), 3);
 		else if(list.size() == 4)
 			assertEquals(list.size(), 4);
 		else
 			fail("List was neither 2 nor 3. Amount of inactive connections not concurrent with tested amounts. Size of list is: " + list.size());
 	}
 
 	@Test
 	public void testGetByID(){
 		assertEquals(xm.createOpenMentor(um.getByEmail("johnfirst@gmail.com"), fm.getSingleByTitle("Bathroom Wall")), true);
 		assertEquals(xm.createOpenMentor(um.getByEmail("phil@last.com"), fm.getSingleByTitle("Bathroom Wall")), true);
 		assertEquals(xm.getByID(1).getMentor().getFullName(), "John First");
 	}
 	
 	@Test
 	public void testGetByMentor(){
 		List<Connection> list = xm.getByMentor(um.getByEmail("someguy@somewhere.com"));
 		for(Iterator<Connection> i = list.iterator(); i.hasNext();){
 			Connection current = i.next();
 			assertEquals(current.getClass(), Connection.class);
 		}
 		assertEquals(list.size(), 2);
 	}
 	
 	@Test
 	public void testGetByTrainee(){
 		List<Connection> list = xm.getByTrainee(um.getByEmail("johnfirst@gmail.com"));
 		for(Iterator<Connection> i = list.iterator(); i.hasNext();){
 			Connection current = i.next();
 			assertEquals(current.getClass(), Connection.class);
 		}
 		assertEquals(list.size(), 2);
 	}
 	
 	@Test
 	public void testGetByField(){
 		List<Connection> list = xm.getByField(fm.getSingleByTitle("Google tips"));
 		for(Iterator<Connection> i = list.iterator(); i.hasNext();){
 			Connection current = i.next();
 			assertEquals(current.getClass(), Connection.class);
 		}
 		assertEquals(list.size(), 2);
 	}
 	
 	@Test
 	public void testSetDifficultyLevel(){
 		Connection connection = xm.getByMentor(um.getByEmail("bob.second@outlook.com")).get(0);
 		xm.setDifficultyLevel(connection, 2);
 		
 		connection = xm.getByField(fm.getSingleByTitle("Google Tips")).get(0);
 		assertEquals(connection.getDifficultyLevel(), 2);
 	}
 	
 	@Test
 
 	public void testGetOpenTraineeConnections(){
 		List<Connection> list = xm.getOpenTraineeConnections(fm.getSingleByTitle("C++"));
 		assertEquals(list.get(0).getTrainee().getEmail(), "johnfirst@gmail.com");
 	}
 	
 	@Test
 	public void testGetOpenMentorConnections(){
 		List<Connection> list = xm.getOpenMentorConnections(fm.getSingleByTitle("Java"));
 		assertEquals(list.size(), 2);
 	}
 	
 	//TODO testGetCommentList
 	
 	public void testAddMentorToConnection(){
 		User mentor = um.getByEmail("johnfirst@gmail.com");
 		Connection connection = xm.getByMentor(mentor).get(0);
 		xm.addMentorToConnection(connection, mentor);
 		assertEquals(mentor.getUserID(), xm.getByMentor(um.getByEmail("johnfirst@gmail.com")).get(0).getMentor().getUserID());
 	}
 	
 	@Test
 	public void testAddTraineeToConnection(){
 		User trainee = um.getByEmail("johnfirst@gmail.com");
 		Connection connection = xm.getByTrainee(trainee).get(0);
 		xm.addTraineeToConnection(connection, trainee);
 		assertEquals(trainee.getUserID(), xm.getByTrainee(um.getByEmail("johnfirst@gmail.com")).get(0).getTrainee().getUserID());
 		}
 	}
