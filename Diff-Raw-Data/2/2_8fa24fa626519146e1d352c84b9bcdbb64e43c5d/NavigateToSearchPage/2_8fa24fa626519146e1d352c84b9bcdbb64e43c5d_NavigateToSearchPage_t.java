 
 import net.sf.sahi.client.Browser;
 
 public class NavigateToSearchPage {
 
 	private Browser browser;
 
 	public NavigateToSearchPage(Browser browser) {
 		this.browser = browser;
 	}
 
 	public void setUp() throws Exception {
		browser.navigateTo("http://localhost:8080/students/search");
 	}
 
 	public void tearDown() throws Exception {
 		
 
 	}
 
 }
