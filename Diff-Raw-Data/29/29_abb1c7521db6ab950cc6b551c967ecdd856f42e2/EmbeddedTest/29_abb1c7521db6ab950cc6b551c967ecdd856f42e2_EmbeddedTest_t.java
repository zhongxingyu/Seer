 package de.lbe.sandbox.openejb;
 
 import javax.annotation.ManagedBean;
 import javax.ejb.EJB;
 
 import org.junit.Test;
 
 /**
  * @ManagedBean? Remove it and run the test... 
  */
 @ManagedBean
 public class EmbeddedTest extends AbstractLanguageTest {
 
 	@EJB
 	private LanguageService languageService;
 
 	/**
 	 * 
 	 */
 	@Test
 	public void testLanguage() throws Exception {
 		Language lang = languageService.getLanguage(1);
 		assertNotNull(lang);
 		
 		lang = languageService.getLanguage(123456);
 		assertNull(lang);
 	}
 }
