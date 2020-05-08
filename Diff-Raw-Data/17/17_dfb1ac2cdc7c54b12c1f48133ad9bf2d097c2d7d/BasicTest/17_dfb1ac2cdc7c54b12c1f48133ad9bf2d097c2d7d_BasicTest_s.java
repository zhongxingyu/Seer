 import org.junit.*;
 
 import controllers.Hash;
 import controllers.Login;
 
 import java.util.*;
 import play.test.*;
 import models.*;
 
 public class BasicTest extends UnitTest {
 
 	String demoAvatar = "http://www.colourbox.com/preview/2578170-616024-graphic-illustration-of-man-in-business-suit-as-user-icon-avatar.jpg";
 	
 	@Before
     public void setup() {
         Fixtures.deleteAll();
     }
 	
 	
 	@Test
 	public void createAndRetrieveUser() {
 		User newUser = new User("Bob","Meyer",Hash.createPassword("secret"),"bob@gmail.com",demoAvatar);
 		
 		assertNotNull(newUser);
 		assertEquals("Bob",newUser.firstName);
 		
 		// check of Hash class
 		assertTrue(Hash.checkPassword("secret", newUser.password));
 		assertFalse(Hash.checkPassword("badpassword", newUser.password));
 		
 	}
 	
 	@Test
 	public void createAndRetrieveItem() {
 		Item newItem = new Item("Schuhe","sportlich","Men","Nike","http://images.asos-media.com/inv/media/5/3/1/6/3016135/black/image1xl.jpg",125.0,"demo@mytum.de");
 		
 		assertNotNull(newItem);
 		assertEquals("Schuhe",newItem.title);
 	}
 	
 	@Test
 	public void createAndRetrieveBoard() {
 		Item newItem = new Item("Schuhe","sportlich","Men","Nike","http://images.asos-media.com/inv/media/5/3/1/6/3016135/black/image1xl.jpg",125.0,"demo@mytum.de");
		Board newBoard = new Board("Sportlicher Look für Männer","AF423G");
 		
 		newBoard.items.add(newItem);
 		
 		assertNotNull(newItem);
 		assertNotNull(newBoard);
 		
 		assertEquals("Sportlicher Look für Männer",newBoard.title);
 		
 		// Item got successful added to board
 		assertEquals(newItem.title,newBoard.items.get(0).title);
 	}
 
 }
