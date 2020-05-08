 package ar.edu.utn.tacs.group5.controller;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
public class GetFeedControllerTest extends AbstractAfterLoginControllerTest {
 
     @Test
     public void run() throws Exception {
        tester.param(Constants.LINK, "http://www.foo.com");
         tester.start("/GetFeed");
         GetFeedController controller = tester.getController();
         assertThat(controller, is(notNullValue()));
         assertThat(tester.isRedirect(), is(false));
         assertThat(tester.getDestinationPath(), is(nullValue()));
     }
 }
