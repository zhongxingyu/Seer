 package org.blep.spring.sql.stats;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import javax.management.MBeanServer;
 import javax.management.MBeanServerInvocationHandler;
 import javax.management.ObjectName;
 import javax.sql.DataSource;
 import java.lang.management.ManagementFactory;
 import java.sql.Connection;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 
 /**
  * @author blep
  *         Date: 04/04/12
  *         Time: 07:42
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:sql-stats-context.xml", "classpath:test-application-context.xml"})
 public class StatControllerTest {
     
     @Autowired
     private DataSource dataSource;
 
     @Test
     public void testShouldRetrieveMBean() throws Exception {
 
 
         MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
         ObjectName name = new ObjectName("org.blep.spring.sql.stats:name=statController,type=StatController");
         StatControllerMBean statController = MBeanServerInvocationHandler.newProxyInstance(mbs, name, StatControllerMBean.class, false);
 
         assertNotNull(statController);
 
//        assertEquals(0,statController.getActiveConnectionCount());
 
         statController.startRecording();
        statController.resetConnectionCounts();

        long activeConnectionCount = statController.getActiveConnectionCount();
        System.out.println("activeConnectionCount = " + activeConnectionCount);

         Connection connection = dataSource.getConnection();
 
        assertEquals(activeConnectionCount+1,statController.getActiveConnectionCount());
 
         connection.close();
 
        assertEquals(activeConnectionCount, statController.getActiveConnectionCount());
 
     }
 }
