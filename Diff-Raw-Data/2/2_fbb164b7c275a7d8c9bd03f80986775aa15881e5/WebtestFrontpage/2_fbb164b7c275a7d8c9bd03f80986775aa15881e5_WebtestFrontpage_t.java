 package no.steria.swhrs;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.webapp.WebAppContext;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 
 public class WebtestFrontpage {
 
 	@Test
 	public void shouldDisplayFrontpage() throws Exception {
 		Server server = new Server(0);
 		server.setHandler(new WebAppContext("src/main/webapp", "/"));
 		server.start();
 		int localPort = server.getConnectors()[0].getLocalPort();
 		WebDriver browser = createBrowser();
		browser.get("http://localhost:" + localPort + "/mainPerson.html");
 		browser.findElement(By.linkText("Create person")).click();
 		browser.findElement(By.name("Name")).sendKeys("Darth");
 		browser.findElement(By.name("CreatePersonButton")).click();
 		browser.findElement(By.linkText("Find person")).click();
 		assertThat(browser.getPageSource()).contains("<li>Darth</li>");
 		
 		
 	}
 
 	private HtmlUnitDriver createBrowser() {
 		return new HtmlUnitDriver() {
 			@Override
 			public WebElement findElement(By by) {
 				try {
 					return super.findElement(by);
 				} catch (NoSuchElementException e) {
 					throw new NoSuchElementException("Did not find " + by + " in " +getPageSource());
 				}
 			}
 		};
 	}
 	
 }
