import java.util.concurrent.TimeUnit;

 import org.junit.Test;
 import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.PageFactory;
 
 public class PesquisandoNoGoogleStoryTest {
 
 	@Test
 	public void testPesquisandoNoGoogleAPalavraGroovy() throws Exception {
		final FirefoxDriver firefox = new FirefoxDriver();
		firefox.manage().timeouts().implicitlyWait(6, TimeUnit.SECONDS);
		
		final GoogleSearchPage googleSearchPage = PageFactory.initElements(firefox, GoogleSearchPage.class);
 		googleSearchPage.visit();
 		googleSearchPage.search("groovy");
 		googleSearchPage.shouldSee("Groovy - Home");
 	}
 
 }
