 package ar.edu.utn.tacs.group5.controller;
 
import org.slim3.tester.ControllerTestCase;
 import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
 
public class AddTorrentControllerTest extends ControllerTestCase {
 
     @Test
     public void run() throws Exception {
        tester.start("/AddTorrent");
         AddTorrentController controller = tester.getController();
         assertThat(controller, is(notNullValue()));
         assertThat(tester.isRedirect(), is(false));
         assertThat(tester.getDestinationPath(), is(nullValue()));
     }
 }
