 /**
  * SOEN 490
  * Capstone 2011
  * Test for MessageMapper.
  * Team members: 	
  * 			Sotirios Delimanolis
  * 			Filipe Martinho
  * 			Adam Harrison
  * 			Vahe Chahinian
  * 			Ben Crudo
  * 			Anthony Boyer
  * 
  * @author Capstone 490 Team Moving Target
  *
  */
 
 package tests.domain.message;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 
 import domain.message.Message;
 import domain.message.MessageFactory;
 import domain.message.mappers.MessageInputMapper;
 import domain.message.mappers.MessageOutputMapper;
 import domain.user.IUser;
 import domain.user.UserFactory;
 import domain.user.UserType;
 import exceptions.MapperException;
 
 import junit.framework.TestCase;
 
 public class MessageInputMapperTest extends TestCase {
 
 	// Data members for a Message
 	private BigInteger mid = new BigInteger("159949857935");
 	private final byte[] message = {0,1,2,3,4,5};
 	private final float speed = 10.0f;
 	private final double latitude = 20.0;
 	private final double longitude = 10.0;
 	private Timestamp createdDate = new Timestamp(new GregorianCalendar(2011, 9, 10).getTimeInMillis());
 	private final int userRating = -1;
 	
 	// Data members for a User
 	private final BigInteger uid = new BigInteger("3425635465657");
 	private final String email = "example@example.com";
 	private final String password = "password";
 	private final UserType userType = UserType.USER_NORMAL;
 	private final int version = 1;
 	
 	public void testFind() throws IOException, SQLException {
 		// Make sure that the message does not already exist in the database
 		try {
 			MessageInputMapper.find(mid);
 			fail("Mapper Exception should have been thrown");
 		}
 		catch(MapperException e) {
 			// If we make it here the test passes
 		}
 
 		// Create a new message using the factory createClean method, ensure it does not do the insertion into the database
 		Message newMessage = MessageFactory.createClean(mid, uid, message, speed, latitude, longitude, createdDate, userRating);
 		
 		// Insert the message into the database
 		MessageOutputMapper.insert(newMessage);
 		
 		Message oldMessage = null;
 		
 		try {
 			// Find the message in the database and compare it to the original
 			oldMessage = MessageInputMapper.find(mid);
 			assertEquals(newMessage.equals(oldMessage), true);
 		} 
 		catch(MapperException e) {
 			fail("MapperException thrown when it should not have");
 		}
 		
 		// Remove the message from the database
 		assertEquals(MessageOutputMapper.delete(oldMessage), 1);
 		
 	}
 	
 
 	public void testFindByUser() throws NoSuchAlgorithmException, IOException, SQLException {
 		// Create some custom data for the messages
 		final byte[] message1 = {0,1,2,3,4};
 		final byte[] message2 = {5,6,7,8,9};
 
 		// Alter the time created, otherwise duplicated keys are created
 		Timestamp createdDate1 = new Timestamp(new GregorianCalendar(2011, 9, 10).getTimeInMillis());
 		Timestamp createdDate2 = new Timestamp(new GregorianCalendar(2011, 10, 10).getTimeInMillis());
 		
 		// Create a user object to associate the messages we are about to create to
 		IUser user = UserFactory.createClean(uid, email, password, userType, version);
 		
 		// Create some messages
 		Message m1 = MessageFactory.createNew(uid, message1, speed, latitude, longitude, createdDate1, userRating);
 		Message m2 = MessageFactory.createNew(uid, message2, speed, latitude, longitude, createdDate2, userRating);
 		
 		//  Save them to the database so we can find them
 		MessageOutputMapper.insert(m1);
 		MessageOutputMapper.insert(m2);
 		
 		// Find the messages from the database and make sure they match the ones we created
 		List<Message> messages = MessageInputMapper.findByUser(user);
 		
 		// Two message should be returned
 		assertEquals(messages.size() == 2, true);
 		
 		// Make sure that the message were persisted by deleting them
 		assertEquals(MessageOutputMapper.delete(m1), 1);
 		assertEquals(MessageOutputMapper.delete(m2), 1);
 	}
 	
 	public void testFindAll() throws IOException, NoSuchAlgorithmException, SQLException {
 		// Create some custom data for the messages
 		final byte[] message1 = {0,1,2,3,4};
 		final byte[] message2 = {5,6,7,8,9};
 
 		// Alter the time created, otherwise duplicated keys are created
 		Timestamp createdDate1 = new Timestamp(new GregorianCalendar(2011, 9, 10).getTimeInMillis());
 		Timestamp createdDate2 = new Timestamp(new GregorianCalendar(2011, 10, 10).getTimeInMillis());
 		
 		// Create some messages and save them to the database so we can find them
 		Message m1 = MessageFactory.createNew(uid, message1, speed, latitude, longitude, createdDate1, userRating);
 		Message m2 = MessageFactory.createNew(uid, message2, speed, latitude, longitude, createdDate2, userRating);
 		
 		MessageOutputMapper.insert(m1);
 		MessageOutputMapper.insert(m2);
 		
 		// Find all of the messages from the database and make sure they match the ones we created
 		List<Message> messages = MessageInputMapper.findAll();
 		
 		// Two message should be returned
		assertEquals(messages.size() == 2, true);
 		
 		// Make sure that the message were persisted by deleting them
 		assertEquals(MessageOutputMapper.delete(m1), 1);
 		assertEquals(MessageOutputMapper.delete(m2), 1);
 	}
 	
 	public void testFindInProximity() {
 
 	}
 	
 }
