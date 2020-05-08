 package tests.domain.message;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.GregorianCalendar;
 
 import domain.message.Message;
 import domain.message.MessageFactory;
 import domain.message.MessageIdentityMap;
 import domain.message.mappers.MessageOutputMapper;
 import domain.user.IUser;
 import domain.user.UserProxy;
 
 import junit.framework.TestCase;
 
 public class MessageFactoryTest extends TestCase {
 
 	// Attributes for a User
 	private final BigInteger uid = new BigInteger("3425635465657");	
 
 	// Attributes for a Message
 	private BigInteger mid = new BigInteger("158749857935");
 	private IUser owner = new UserProxy(uid);
 	private byte[] message = { 1, 2, 3, 4, 5, 6 };
 	private float speed = 5.5f;
 	final double latitude = 29.221;
 	final double longitude = 35.134;
 	private Timestamp createdAt = new Timestamp(new GregorianCalendar(2011, 9, 10).getTimeInMillis());
 	private int userRating = 7;
 	
 	public void testCreateClean() {
 		Message newMsg = new Message(mid, owner, message, speed, latitude, longitude, createdAt, userRating);
 		Message factoryMsg = MessageFactory.createClean(mid, uid, message, speed, latitude, longitude, createdAt, userRating);
 		assertEquals("Message created using constructor should be identical to that the factory produces", newMsg.equals(factoryMsg), true);
		assertEquals("Message created using constructor should be identical to that the factory produces and stores in the MessageIdentityMap", newMsg.equals(factoryMsg), true);
 	}
 	
 	public void testCreateNew() throws NoSuchAlgorithmException, IOException, SQLException {
 		// create a a new message
 		Message msg = MessageFactory.createNew(uid, message, speed, latitude, longitude, createdAt, userRating);
 		
 		// test all of the attributes
 		assertEquals(msg.getLatitude(), latitude);
 		assertEquals(msg.getLongitude(), longitude);
 		assertEquals(msg.getSpeed(), speed);
 		assertEquals(msg.getUserRating(), userRating);
 		assertEquals(msg.getCreatedAt(), createdAt);
 		assertEquals(msg.getMessage(), message);
 		assertEquals(msg.getOwner(), owner);
 		
 		// Keep the database clean and make sure we delete a single record
 		//assertEquals(MessageOutputMapper.delete(msg),1);
 	}
 	
 }
