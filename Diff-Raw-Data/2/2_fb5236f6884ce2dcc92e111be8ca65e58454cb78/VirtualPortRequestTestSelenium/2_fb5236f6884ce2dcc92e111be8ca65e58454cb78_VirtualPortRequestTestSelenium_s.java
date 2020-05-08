 /**
  * Copyright (c) 2012, SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod;
 
 import nl.surfnet.bod.support.TestExternalSupport;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class VirtualPortRequestTestSelenium extends TestExternalSupport {
 
   @Before
   public void setup() {
     getNocDriver().createNewPhysicalResourceGroup("SIDN", ICT_MANAGERS_GROUP, "test@test.nl");
    getNocDriver().createNewPhysicalResourceGroup("SURFnet bv", ICT_MANAGERS_GROUP, "test@test.nl");
     getNocDriver().linkPhysicalPort(NMS_PORT_ID_1, "Request a virtual port", "SURFnet bv");
     getNocDriver().linkPhysicalPort(NMS_PORT_ID_2, "Request a virtual port", "SIDN");
 
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
 
     getUserDriver().verifyRequestVirtualPortInstituteInactive("SIDN");
 
     getUserDriver().selectInstituteAndRequest("SURFnet bv", "Mijn nieuwe poort", 1200,
         "I would like to have a new port");
 
     getUserDriver().switchToManager("SURFnet");
 
     getWebDriver().clickLinkInLastEmail();
 
     getManagerDriver().verifyNewVirtualPortHasProperties("SURFnet bv", "Mijn nieuwe poort", 1200);
 
     getManagerDriver().createVirtualPort("Your vport");
 
     getManagerDriver().verifyVirtualPortExists("Your vport", "selenium-users", "1200");
 
     getManagerDriver().verifyVirtualResourceGroupExists("selenium-users", "1");
 
     getManagerDriver().switchToManager("SIDN");
 
     getManagerDriver().verifyVirtualResourceGroupsEmpty();
 
     // requester has email about port creation
     getWebDriver().verifyLastEmailRecipient("Selenium Test User <selenium@test.com>");
 
     getManagerDriver().switchToUser();
 
     getWebDriver().clickLinkInBeforeLastEmail();
 
     // should be manager again and have a message link is already used
     getWebDriver().verifyPageHasMessage("already processed");
 
     // physical resource group should have one physical port
     getManagerDriver().switchToNoc();
     getNocDriver().verifyPhysicalResourceGroupExists("SURFnet bv", "test@test.nl", "1");
 
     getNocDriver().switchToManager("SURFnet");
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
