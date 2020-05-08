 import org.junit.Test;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 public class PesquisandoNoGoogleStoryTest {
 
 	@Test
 	public void testPesquisandoNoGoogleAPalavraGroovy() throws Exception {
		final GoogleSearchPage googleSearchPage = new GoogleSearchPage(
				new FirefoxDriver());
 		googleSearchPage.visit();
 		googleSearchPage.search("groovy");
 		googleSearchPage.shouldSee("Groovy - Home");
 	}
 
 }
