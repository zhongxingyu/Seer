 package server.operations;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import server.entities.Form;
 import server.entities.Newsletter;
 import server.exceptions.EmailSendingException;
 import server.queries.NewsletterQuery;
 import server.resources.FormResource;
 
 /**
  * Test for the generation of the newsLetter and URLs.
  * 
  * @author dennis.markmann
  * @since 1.0
  * @version 1.0
  */
 
 public class NewsLetterHandlerTest extends TestCase {
 
 	private NewsLetterHandler handler;
 
 	private String eMailAddress;
 	private String url;
 	private Form form;
 
 	@Override
 	@Before
 	public void setUp() {
 		this.handler = new NewsLetterHandler();
		this.form = FormResource.getForms().get(0);
 
 		this.eMailAddress = "test2@localhost";
 	}
 
 	@Test
 	public void testUrlCreation() {
 
 		this.url = "http://localhost:8080/server/newsletter/confirm?id=" + this.form.getId() + "&email=" + this.eMailAddress;
 
 		NewsLetterHandlerTest.assertEquals(this.url, this.handler.generateRegistrationLink(this.form, this.eMailAddress));
 
 	}
 
 	@Test
 	public void testAddressCreation() {
 
 		boolean success = false;
 
 		NewsLetterHandlerTest.assertTrue(this.handler.confirmRegistration(this.form, this.eMailAddress));
 
 		final List<Newsletter> newsLetterList = new NewsletterQuery().getAllNewsletters(this.form);
 
 		for (final Newsletter newsLetter : newsLetterList) {
 			if (newsLetter.getEmail().getEMailAddress().equals(this.eMailAddress)) {
 				success = true;
 			}
 		}
 		NewsLetterHandlerTest.assertEquals(true, success);

 		try {
			assertTrue(this.handler.removeAddress(newsLetterList.get(1)));
 		} catch (final EmailSendingException e) {
 			fail();
 		}
		assertTrue(this.handler.removeRegistration(newsLetterList.get(1)));
 	}
 }
