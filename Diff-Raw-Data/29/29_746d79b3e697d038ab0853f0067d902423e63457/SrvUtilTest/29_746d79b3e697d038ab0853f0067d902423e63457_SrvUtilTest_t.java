 package org.littleshoot.util;
 
import static org.junit.Assert.assertFalse;
 
 import java.net.InetSocketAddress;
 import java.util.Collection;
 
 import org.junit.Test;
 
 
public class SrvUtilTest {
 
    @Test
    public void testSrv() throws Exception {
        // if (true) return;
         final SrvUtil util = new SrvUtilImpl();
        final Collection<InetSocketAddress> addresses = util
                .getAddresses("_stun._udp.littleshoot.org");// "_sip._tcp.littleshoot.org");

         System.out.println(addresses);

        assertFalse(addresses.isEmpty());
        // assertEquals(2, addresses.size());
     }
}
