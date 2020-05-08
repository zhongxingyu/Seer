 package server.operations;
 
 import javax.persistence.EntityManager;
 
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import org.junit.Test;
 
import server.entities.EmailAddress;
 import server.entities.Form;
 import server.entities.Newsletter;
 import server.entities.Replacement;
 import server.exceptions.EmailSendingException;
 import server.exceptions.ScheduleCreationException;
 import server.operations.email.EmailJobHelper;
 import server.persistence.HibernateUtil;
import server.queries.LoginQuery;
import server.queries.NewsletterQuery;
import server.resources.FormResource;
 
 /**
  * Test for exception email sending.
  * 
  * @author dennis.markmann
  * @since JDK.1.7.0_25
  * @version 1.0
  */
 
 public class EmailSendingTest extends TestCase {
 
 	private EmailJobHelper helper;
 	private Form form;
 	private EntityManager em;
	private String email;
 
 	@Override
 	@Before
 	public void setUp() {
 		this.helper = new EmailJobHelper();
		this.form = FormResource.getForms().get(0);
 
 		this.em = HibernateUtil.getEntityManager();
		email="test@localhost";
 	}
 
 	@Test
 	public void testEmailSending() {
 		try {
 			try {
 				this.helper.sendNewsLetterMail(this.getExistingReplacement());
 			} catch (final ScheduleCreationException e) {
 				fail();
 			}
			this.helper.sendConfirmationMail(this.form, email); //Registrierungs Bestätigung
			this.helper.sendCreationMail(email, "test", "test"); // Erstell Bestätigung
			this.helper.sendPasswordChangeMail(email, "test", "test"); // Password Änderungs bestätigung
 			this.helper.sendRemoveRegistrationMail(this.getExistingNewsletter());
 		} catch (final EmailSendingException e) {
 			EmailSendingTest.fail();
 		}
 	}
 
 	private Replacement getExistingReplacement() {
 		return this.em.find(Replacement.class, 1);
 	}
 
 	private Newsletter getExistingNewsletter() {
		EmailAddress emailAdress = new NewsletterQuery().getEmail(email);
		return new NewsletterQuery().getNewsletter(emailAdress.getId(), form.getId());
 	}
 }
