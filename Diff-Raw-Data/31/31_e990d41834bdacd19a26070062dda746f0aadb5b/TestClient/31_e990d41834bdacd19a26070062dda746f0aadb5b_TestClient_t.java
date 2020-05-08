 import edu.entitymaster.logic.Client;
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Антон
  * Date: 13.11.12
  * Time: 22:14
  * To change this template use File | Settings | File Templates.
  */
 public class TestClient {
    Client client = new Client(0, "test", false);
 
     @Test
     public void testClientName(){
         client.setName("test");
         assertThat(client.getName(), is("test"));
     }
 }
