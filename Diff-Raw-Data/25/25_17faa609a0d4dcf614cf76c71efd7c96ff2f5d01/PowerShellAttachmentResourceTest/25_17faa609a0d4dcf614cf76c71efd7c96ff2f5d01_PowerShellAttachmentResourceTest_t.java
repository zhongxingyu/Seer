 /*
  * Copyright © 2010 Red Hat, Inc.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.redhat.rhevm.api.powershell.resource;
 
 import java.util.concurrent.Executor;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.Attachment;
 import com.redhat.rhevm.api.model.StorageDomainStatus;
 
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellParser;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 
 import org.junit.Test;
 
 import static org.easymock.classextension.EasyMock.expect;
 
 import static org.powermock.api.easymock.PowerMock.mockStatic;
 import static org.powermock.api.easymock.PowerMock.replayAll;
 
 
 public class PowerShellAttachmentResourceTest extends AbstractPowerShellResourceTest<Attachment, PowerShellAttachmentResource> {
 
     private static final String DATA_CENTER_NAME = "d12345";
     private static final String DATA_CENTER_ID = Integer.toString(DATA_CENTER_NAME.hashCode());
     private static final String STORAGE_DOMAIN_NAME = "d12345";
     private static final String STORAGE_DOMAIN_ID = Integer.toString(STORAGE_DOMAIN_NAME.hashCode());
    private static final String DATA_CENTER_URI = "datacenters/" + DATA_CENTER_ID;
    private static final String STORAGE_DOMAIN_URI = "storagedomains/" + STORAGE_DOMAIN_ID;
     private static final String ATTACHMENT_URI = STORAGE_DOMAIN_URI + "/attachments/" + DATA_CENTER_ID;
     private static final String DEACTIVATE_ACTION_URI = ATTACHMENT_URI + "/deactivate";
     private static final String DATA_CENTER_ARG = " -datacenterid \"" + DATA_CENTER_ID + "\"";
     private static final String STORAGE_DOMAIN_ARG = " -storagedomainid \"" + STORAGE_DOMAIN_ID + "\"";
     private static final String DC_AND_SD_ARGS = DATA_CENTER_ARG + STORAGE_DOMAIN_ARG;
     private static final String GET_COMMAND = "get-storagedomain" + DC_AND_SD_ARGS;
     private static final String ACTION_RETURN = "replace with realistic powershell return";
 
     protected PowerShellAttachmentResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser) {
         return new PowerShellAttachmentResource(DATA_CENTER_ID, STORAGE_DOMAIN_ID, executor, poolMap, parser);
     }
 
     protected String formatStorageDomain(String name) {
         return formatXmlReturn("storagedomain", new String[] { name }, new String[] { "" }, new String[] {});
     }
 
     @Test
     public void testGet() throws Exception {
         verifyAttachment(resource.get(setUpAttachmentExpectations()));
     }
 
     @Test
     public void testActivate() throws Exception {
         verifyActionResponse(
             resource.activate(setUpActionExpectation("activate", "activate-storagedomain"), getAction()),
             false);
     }
 
     @Test
     public void testDeactivate() throws Exception {
         verifyActionResponse(
             resource.deactivate(setUpActionExpectation("deactivate", "deactivate-storagedomain"), getAction()),
             false);
     }
 
     @Test
     public void testActivateAsync() throws Exception {
         verifyActionResponse(
             resource.activate(setUpActionExpectation("activate", "activate-storagedomain"), getAction(true)),
             true);
     }
 
     @Test
     public void testDeactivateAsync() throws Exception {
         verifyActionResponse(
             resource.deactivate(setUpActionExpectation("deactivate", "deactivate-storagedomain"), getAction(true)),
             true);
     }
 
     private UriInfo setUpAttachmentExpectations() throws Exception {
         mockStatic(PowerShellCmd.class);
         expect(PowerShellCmd.runCommand(setUpShellExpectations(),
                                         GET_COMMAND)).andReturn(formatStorageDomain(STORAGE_DOMAIN_NAME));
         replayAll();
         return null;
     }
 
     private UriInfo setUpActionExpectation(String verb, String command) throws Exception {
         return setUpActionExpectation(ATTACHMENT_URI, verb, command + DC_AND_SD_ARGS, ACTION_RETURN);
     }
 
     private void verifyAttachment(Attachment attachment) {
         assertNotNull(attachment);
         assertEquals(attachment.getId(), DATA_CENTER_ID);
         assertEquals(attachment.getName(), DATA_CENTER_ID);
         assertNotNull(attachment.getDataCenter());
         assertEquals(attachment.getDataCenter().getId(), DATA_CENTER_ID);
         assertNotNull(attachment.getStorageDomain());
         assertEquals(attachment.getStorageDomain().getId(), STORAGE_DOMAIN_ID);
         assertEquals(attachment.getStatus(), StorageDomainStatus.ACTIVE);
         assert(attachment.isMaster());
     }
 
     private void verifyActionResponse(Response r, boolean async) throws Exception {
         verifyActionResponse(r, ATTACHMENT_URI, async);
     }
 }
