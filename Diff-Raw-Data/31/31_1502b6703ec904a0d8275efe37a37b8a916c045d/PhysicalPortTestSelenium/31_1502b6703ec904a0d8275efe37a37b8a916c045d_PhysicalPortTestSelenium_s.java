 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod;
 
 import nl.surfnet.bod.support.TestExternalSupport;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class PhysicalPortTestSelenium extends TestExternalSupport {
 
   private final String groupName = "2COLLEGE";
 
   @Before
   public void setup() {
     getNocDriver().createNewPhysicalResourceGroup(groupName, ICT_MANAGERS_GROUP, "test@example.com");
   }
 
   @Test
   public void allocatePhysicalPortFromInstitutePage() {
    getNocDriver().addPhysicalPortToInstitute(groupName, "Mock_ETH10G-1-13-1", "NOC label");
 
     getNocDriver().verifyPhysicalPortWasAllocated(NETWORK_ELEMENT_PK, "NOC label");
 
     getNocDriver().unlinkPhysicalPort(NETWORK_ELEMENT_PK);
   }
 
   @Test
   public void createRenameAndDeleteAPhysicalPort() {
     String nocLabel = "My Selenium Port (Noc)";
     String managerLabel1 = "My Selenium Port (Manager 1st)";
     String managerLabel2 = "My Selenium Port (Manager 2nd)";
 
     getNocDriver().linkPhysicalPort(NETWORK_ELEMENT_PK, nocLabel, managerLabel1, groupName);
 
     getNocDriver().verifyPhysicalPortWasAllocated(NETWORK_ELEMENT_PK, nocLabel);
 
     getNocDriver().gotoEditPhysicalPortAndVerifyManagerLabel(NETWORK_ELEMENT_PK, managerLabel1);
 
     getNocDriver().switchToManager();
 
     getManagerDriver().changeManagerLabelOfPhyiscalPort(NETWORK_ELEMENT_PK, managerLabel2);
 
     getManagerDriver().verifyManagerLabelChanged(NETWORK_ELEMENT_PK, managerLabel2);
 
     getManagerDriver().switchToNoc();
 
     getNocDriver().gotoEditPhysicalPortAndVerifyManagerLabel(NETWORK_ELEMENT_PK, managerLabel2);
 
     getNocDriver().unlinkPhysicalPort(NETWORK_ELEMENT_PK);
   }
 
   @After
   public void teardown() {
     getNocDriver().deletePhysicalResourceGroup(groupName);
   }
 
 }
