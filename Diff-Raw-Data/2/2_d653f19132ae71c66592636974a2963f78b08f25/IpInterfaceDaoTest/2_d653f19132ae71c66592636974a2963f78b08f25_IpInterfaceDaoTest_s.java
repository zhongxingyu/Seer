 package org.opennms.ng.persistence;
 
 import org.junit.Test;
 import org.opennms.netmgt.model.OnmsIpInterface;
 import org.opennms.ng.persistence.dao.OnmsIpInterfaceDao;
 
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 public class IpInterfaceDaoTest extends AbstractTestBase {
 
     @Test
     public void testWiring() throws Exception {
 
         OnmsIpInterfaceDao dao = (OnmsIpInterfaceDao) getBean("onmsIpInterfaceDaoJPA");
 
         assertNotNull(dao);
 
         OnmsIpInterface ipInterface = dao.read(0);
 
         assertNull(ipInterface);
 
         dao.findByServiceType("test");
        dao.findByNodeIdAndIpAddress(1,"127.0.0.1");
     }
 }
