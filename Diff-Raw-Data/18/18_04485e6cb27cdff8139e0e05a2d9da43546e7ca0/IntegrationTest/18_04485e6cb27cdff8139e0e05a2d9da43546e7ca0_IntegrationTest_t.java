 package de.htwg.seapal.maps.app;
 
 import org.junit.*;
 
 import play.mvc.*;
 import play.test.*;
 import play.libs.F.*;
 
 import static play.test.Helpers.*;
 import static org.fest.assertions.Assertions.*;
 
 import static org.fluentlenium.core.filter.FilterConstructor.*;
 
 public class IntegrationTest {
	private final int SERVER_PORT = 3333;
	
     /**
      * add your integration test here
      * in this example we just check if the welcome page is being shown
      */   
     @Test
     public void test() {
        running(testServer(SERVER_PORT, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
             public void invoke(TestBrowser browser) {
                browser.goTo("http://localhost:" + SERVER_PORT);
                assertThat(browser.pageSource()).contains("Index page of MAPS");
             }
         });
     }
   
 }
