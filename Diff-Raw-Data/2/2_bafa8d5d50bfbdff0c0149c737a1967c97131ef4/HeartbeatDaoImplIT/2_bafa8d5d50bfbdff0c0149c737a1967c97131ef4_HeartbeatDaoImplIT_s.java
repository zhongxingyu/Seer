 package org.zenoss.zep.dao.impl;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
 import org.zenoss.protobufs.zep.Zep.DaemonHeartbeat;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.dao.HeartbeatDao;
 
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 /**
  * Unit tests for HeartbeatDao.
  */
 @ContextConfiguration({ "classpath:zep-config.xml" })
 public class HeartbeatDaoImplIT extends AbstractTransactionalJUnit4SpringContextTests {
     @Autowired
     public HeartbeatDao heartbeatDao;
 
     private void findHeartbeat(DaemonHeartbeat hb, List<DaemonHeartbeat> heartbeats) throws ZepException {
         boolean found = false;
         for (DaemonHeartbeat heartbeat : heartbeats) {
             if (hb.getMonitor().equals(heartbeat.getMonitor()) &&
                 hb.getDaemon().equals(heartbeat.getDaemon())) {
                 assertEquals(hb, heartbeat);
                 found = true;
             }
         }
         assertTrue(found);
     }
 
     @Before
     public void init() {
         this.simpleJdbcTemplate.update("DELETE FROM daemon_heartbeat");
     }
 
     @Test
     public void testCreate() throws ZepException {
         DaemonHeartbeat.Builder hbBuilder = DaemonHeartbeat.newBuilder();
         hbBuilder.setMonitor("localhost");
         hbBuilder.setDaemon("zenactiond");
         hbBuilder.setTimeoutSeconds(90);
         DaemonHeartbeat hb = hbBuilder.build();
         heartbeatDao.createHeartbeat(hb);
         findHeartbeat(hb, heartbeatDao.findAll());
 
         // Change timeout - tests ON DUPLICATE KEY UPDATE
         hbBuilder.setTimeoutSeconds(900);
         hb = hbBuilder.build();
         heartbeatDao.createHeartbeat(hb);
         findHeartbeat(hb, heartbeatDao.findAll());
     }
 }
