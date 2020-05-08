 package org.cagrid.transfer.test.system;
 
 import gov.nih.nci.cagrid.testing.system.deployment.ServiceContainerFactory;
 import gov.nih.nci.cagrid.testing.system.deployment.ServiceContainerType;
 import gov.nih.nci.cagrid.testing.system.haste.Story;
 
import java.io.IOException;

 import org.junit.Test;
 
 
 public class TransferServiceTestsStoryBook {
 
 
     @Test
     public void systemTestTransferService() throws Throwable {
     	Story s1 = new TransferServiceTest(ServiceContainerFactory
            .createContainer(ServiceContainerType.TOMCAT_CONTAINER));
     	
     	s1.runBare();
 
     	Story s2 = new TransferServiceTest(ServiceContainerFactory
            .createContainer(ServiceContainerType.SECURE_TOMCAT_CONTAINER));
 
     	s2.runBare();
     }

 }
