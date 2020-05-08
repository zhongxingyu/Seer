 package ch9k.network;
 
 import ch9k.eventpool.*;
 import java.net.InetAddress;
 
 import org.junit.Test;
 import org.junit.After;
 import org.junit.Before;
 import static org.junit.Assert.*;
 
 public class RealLifeTest {
     
     @Test
     public void letTheBeastGo() throws Exception {
         EventPool pool = EventPool.getAppPool();
         pool.raiseEvent(new TestNetworkEvent(InetAddress.getByName("10.1.1.129")));
         
         while(true) {
            Thread.sleep(100);
         }
         
     }
     
 }
