 
 package test.axirassa.webapp.pages;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import org.apache.tapestry5.dom.Document;
 import org.apache.tapestry5.dom.Node;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.hibernate.Session;
import org.jaxen.JaxenException;
 import org.junit.Test;
 
 import axirassa.dao.UserDAO;
 import axirassa.model.UserEntity;
 import axirassa.model.flows.CreateUserFlow;
 import axirassa.util.test.TapestryPageTest;
 
 import com.formos.tapestry.xpath.TapestryXPath;
 
 public class TestRegisterPage extends TapestryPageTest {
 	@Inject
 	Session session;
 
 	@Inject
 	UserDAO userDAO;
 
 	@Inject
 	CreateUserFlow createUser;
 
 
 	@Test
 	public void registerPage() throws Exception {
 		Document page = tester.renderPage("user/register");
 
 		List<Node> textNodes = TapestryXPath.xpath("//input[@type='text']").selectNodes(page);
 		assertEquals(3, textNodes.size()); // an extra node for feedback
 
 		List<Node> passwordNodes = TapestryXPath.xpath("//input[@type='password']").selectNodes(page);
 		assertEquals(2, passwordNodes.size());
 
 		Document resultPage = clickSubmitByValue(page, "Register", new LinkedHashMap<String, String>() {
 			{
 				put("txtfield", "who@foo.com");
 				put("txtfield_0", "who@foo.com");
 				put("txtfield_1", "123");
 				put("txtfield_2", "123");
 			}
 		});
 
 		ensureNoErrors(resultPage);
 
 		// did we save this user?
 		UserEntity user = userDAO.getUserByEmail("who@foo.com");
 		assertNotNull(user);
 
 		// password should match
 		assertTrue(user.matchPassword("123"));
 	}
 
 
 	@Test
	public void emptyFields() throws JaxenException {
 		Document page = tester.renderPage("user/register");
 
		Document result = clickSubmitByValue(page, "Register", Collections.<String, String> emptyMap());

		ensureErrorOnField(result, "txtfield");
		ensureErrorOnField(result, "txtfield_0");
		ensureErrorOnField(result, "txtfield_1");
		ensureErrorOnField(result, "txtfield_2");
 	}
 }
