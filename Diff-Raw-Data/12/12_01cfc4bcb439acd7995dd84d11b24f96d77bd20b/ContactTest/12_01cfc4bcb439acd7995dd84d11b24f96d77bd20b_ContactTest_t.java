 import org.junit.Test;
 import org.junit.Before;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
 
 public class ContactTest {
	private ContactImpl contact, contactSame, contactNotSame;
 
 	@Before
 	public void buildUp() {
 		contact = new ContactImpl(4, "Tanyel", "TestNote");
 	}
 	@Test
 	public void testsGetId() {
 		assertEquals(contact.getId(), 4);
 	}
 	@Test
 	public void testsGetName() {
 		assertEquals(contact.getName(), "Tanyel");
 	}
 	@Test
 	public void testsGetNotes() {
 		assertEquals(contact.getNotes(), "TestNote\n");
 	}
 	@Test
 	public void testsAddNotes() {
 		contact.addNotes("&MoreNotes");
 		assertEquals(contact.getNotes(), "TestNote\n&MoreNotes\n");
 	}
 	@Test
 	public void testsEquals() {
 		contactSame = new ContactImpl(contact);
		contact.addNotes("");	//Replicates the additional (+ "\n") of contactSame's notes
 		assertTrue(contact.equals(contactSame));
 	}
	@Test
	public void testsFalseEquals() {
		contactNotSame = new ContactImpl(5, "Tanyel", contact.getNotes());
		assertFalse(contact.equals(contactNotSame));
	}
 }
