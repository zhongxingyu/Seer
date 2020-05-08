 package br.com.provaServer.domain.model;
 
 import static br.com.provaServer.infrastructure.util.TestUtil.i18n;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import br.com.caelum.vraptor.util.test.MockValidator;
 import br.com.provaServer.infrastructure.fixture.TemplateLoader;
 import br.com.six2six.fixturefactory.Fixture;
 
 public class TemplateTest {
 	private MockValidator validator = new MockValidator();
 	
 	@BeforeClass
 	public static void setUp() {
 		TemplateLoader.loadTemplates();
 	}
 	
 	@Test
 	public void titleShouldBeRequired() {
 		Template template = Fixture.from(Template.class).gimme("valid");
 		template.setTitle(null);
 		
 		template.validate(validator);
 		
 		assertTrue("title should be required", validator.hasErrors());
		validator.containsMessage("validation.required", i18n("template.title"));
 	}
 }
