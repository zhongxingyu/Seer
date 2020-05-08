 package ar.edu.utn.tacs.group5.controller;
 
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

 import org.junit.Test;
 
public class AddTorrentControllerTest extends AbstractAfterLoginControllerTest {
 
     @Test
     public void run() throws Exception {
    	tester.param(Constants.LINK, "http://www.foo.com");
        tester.start("/addtorrent");
         AddTorrentController controller = tester.getController();
         assertThat(controller, is(notNullValue()));
         assertThat(tester.isRedirect(), is(false));
         assertThat(tester.getDestinationPath(), is(nullValue()));
     }
 }
