 package com.sapienter.jbilling.server.mediation;
 
 import java.rmi.RemoteException;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import com.sapienter.jbilling.common.JNDILookup;
 
 public class MediationTest extends TestCase {
 
     private MediationSession remoteMediation = null;
 
     protected void setUp() throws Exception {
         super.setUp();
         
         MediationSessionHome mediationHome =
             (MediationSessionHome) JNDILookup.getFactory(true).lookUpHome(
             MediationSessionHome.class,
             MediationSessionHome.JNDI_NAME);
         remoteMediation = mediationHome.create();
 
     }
 
     public void testTrigger() {
         try {
             remoteMediation.trigger();
            List all = remoteMediation.getAll(1);
             assertNotNull("process list can't be null", all);
             assertEquals("There should be one process after running the mediation process", 1, all.size());
 
             List allCfg = remoteMediation.getAllConfigurations(1);
             assertNotNull("config list can't be null", allCfg);
            assertEquals("There should be one process after running the mediation process", 1, allCfg.size());
 
         } catch (RemoteException e) {
             fail("Exception!");
             e.printStackTrace();
         }
     }
 
 }
