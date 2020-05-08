 import com.google.common.io.Files;
 import edu.entitymaster.logic.Client;
 import edu.entitymaster.logic.ClientService;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertTrue;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Антон
  * Date: 10.11.12
  * Time: 21:58
  * To change this template use File | Settings | File Templates.
  */
 public class TestClientService {
     ClientService clientService;
    Client client = new Client("test");
    Client client2 = new Client("2test2");
 
     @Before
     public void setUp() {
         try {
             Files.write("",
                     new File(System.getProperty("user.dir") + "\\ClientRepositoryLog.csv"),
                     Charset.defaultCharset());
         } catch (IOException e) {
             e.printStackTrace();
         }
         clientService = new ClientService();
     }
 
     @After
     public void tearDown() {
     }
 
     @Test
     public void testCreateClient() {
         clientService.createClient(client);
         assertThat(clientService.getClients().size(), is(1));
         assertTrue(clientService.getClients().containsValue(client));
     }
 
     @Test
     public void testUpdateClient() {
         clientService.createClient(client);
         clientService.updateClient(client, client2);
         assertThat(clientService.getClients().size(), is(1));
         assertEquals("2test2", clientService.getClients().get(0).getName());
     }
 
     @Test
     public void testDeleteClient() {
         clientService.createClient(client);
         clientService.deleteClient(client);
         assertThat(clientService.getClients().size(), is(0));
         assertFalse(clientService.getClients().containsValue(client));
     }
 
     @Test
     public void testReadOldRepo() {
         clientService.createClient(client);
         ClientService clientService2 = new ClientService();
         assertThat(clientService.getClients().size(), is(1));
         assertEquals("test", clientService2.getClients().get(0).getName());
     }
 
     @Test
     public void testDeleteUnknownClient() {
         clientService.createClient(client);
         clientService.deleteClient(client2);
         assertThat(clientService.getClients().size(), is(1));
         assertTrue(clientService.getClients().containsValue(client));
     }
 
 }
