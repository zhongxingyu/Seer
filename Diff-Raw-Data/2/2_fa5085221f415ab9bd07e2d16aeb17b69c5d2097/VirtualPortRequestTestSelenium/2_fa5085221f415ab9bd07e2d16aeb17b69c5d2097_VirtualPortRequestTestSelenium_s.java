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
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class VirtualPortRequestTestSelenium extends TestExternalSupport {
 
   @Before
   public void setup() {
     getNocDriver().createNewPhysicalResourceGroup("2COLLEGE", ICT_MANAGERS_GROUP, "test@test.nl");
     getNocDriver().createNewPhysicalResourceGroup("SURFnet bv", ICT_MANAGERS_GROUP, "test@test.nl");
     getNocDriver().linkPhysicalPort(NMS_PORT_ID_1, "Request a virtual port", "SURFnet bv");
     getNocDriver().linkPhysicalPort(NMS_PORT_ID_2, "Request a virtual port", "2COLLEGE");
 
     getWebDriver().clickLinkInLastEmail();
   }
 
   @Test
   public void requestAVirtualPortAndDecline() {
     getManagerDriver().switchToUser();
 
     getUserDriver().requestVirtualPort("selenium-users");
 
     getUserDriver().selectInstituteAndRequest("SURFnet bv", 1000, "Doe mijn een nieuw poort...");
 
     getUserDriver().switchToManager("SURFnet");
 
     getWebDriver().clickLinkInLastEmail();
 
     getManagerDriver().declineVirtualPort("Sorry but I cannot accept your request.");
 
     getWebDriver().verifyLastEmailRecipient("Selenium Test User <selenium@test.com>");
 
     getWebDriver().verifyLastEmailSubjectContains("declined");
   }
 
   @Test
   public void requestVirtualPortAndCheckRequestCanOnlyBeUsedOnce() {
     getManagerDriver().switchToUser();
 
     getUserDriver().requestVirtualPort("selenium-users");
 
     getUserDriver().verifyRequestVirtualPortInstituteInactive("2COLLEGE");
 
     getUserDriver().selectInstituteAndRequest("SURFnet bv", "Mijn nieuwe poort", 1200,
         "I would like to have a new port");
 
     getUserDriver().switchToManager("SURFnet");
 
     getWebDriver().clickLinkInLastEmail();
 
     getManagerDriver().verifyNewVirtualPortHasProperties("SURFnet bv", "Mijn nieuwe poort", 1200);
 
     getManagerDriver().createVirtualPort("Your vport");
 
    getManagerDriver().verifyVirtualPortExists("Your vport", "selenium-users", "1200", "Request a virtual port");
 
     getManagerDriver().verifyVirtualResourceGroupExists("selenium-users", "1");
 
     getManagerDriver().switchToManager("2COLLEGE");
 
     getManagerDriver().verifyVirtualResourceGroupsEmpty();
 
     // requester has email about port creation
     getWebDriver().verifyLastEmailRecipient("Selenium Test User <selenium@test.com>");
 
     getManagerDriver().switchToUser();
 
     getWebDriver().clickLinkInBeforeLastEmail();
 
     // should be manager again and have a message link is already used
     getWebDriver().verifyPageHasMessage("already processed");
 
     // physical resource group should have one physical port
     getManagerDriver().verifyPhysicalResourceGroupExists("SURFnet bv", "test@test.nl", "1");
 
     getManagerDriver().editVirtualPort("Your vport", "Edited vport", 1000, "20");
 
     getManagerDriver().verifyVirtualPortExists("Edited vport", "1000", "selenium-users");
 
     getManagerDriver().switchToUser();
 
     getUserDriver().verifyVirtualPortExists("Edited vport", "1000", "selenium-users");
 
     getUserDriver().editVirtualPort("Edited vport", "User label");
 
     getUserDriver().verifyVirtualPortExists("User label", "1000", "selenium-users");
 
     getUserDriver().switchToManager("SURFnet");
 
     getManagerDriver().verifyVirtualPortExists("Edited vport", "1000", "selenium-users");
   }
 
   @Test
   public void requestAVirtualPortUsingButtonOnListPage() {
     getManagerDriver().switchToUser();
 
     getUserDriver().selectTeamInstituteAndRequest("selenium-users", "SURFnet bv", "myVP", 1000,
         "Doe mijn een nieuw poort...");
 
     getUserDriver().switchToManager("SURFnet");
 
     getWebDriver().clickLinkInLastEmail();
 
     getManagerDriver().acceptVirtualPort("New VP");
 
     getManagerDriver().switchToUser();
 
     getUserDriver().verifyVirtualPortExists("myVP", "1000", "selenium-users", "SURFnet bv");
   }
 
 }
