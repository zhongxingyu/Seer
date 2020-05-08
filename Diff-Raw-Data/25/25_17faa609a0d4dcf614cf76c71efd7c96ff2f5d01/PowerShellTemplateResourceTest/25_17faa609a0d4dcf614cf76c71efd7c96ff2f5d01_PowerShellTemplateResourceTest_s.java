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
 
 import java.net.URI;
 import java.util.concurrent.Executor;
 
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.Fault;
 import com.redhat.rhevm.api.model.Template;
 
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellParser;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 
 import org.junit.Test;
 
 import static org.easymock.classextension.EasyMock.expect;
 
 import static org.powermock.api.easymock.PowerMock.createMock;
 import static org.powermock.api.easymock.PowerMock.mockStatic;
 import static org.powermock.api.easymock.PowerMock.replayAll;
 
 
 public class PowerShellTemplateResourceTest extends AbstractPowerShellResourceTest<Template, PowerShellTemplateResource> {
 
     private static final String TEMPLATE_NAME = "sedna";
     private static final String TEMPLATE_ID = Integer.toString(TEMPLATE_NAME.hashCode());
     private static final String TEMPLATE_DESCRIPTION = "this is a template";
     private static final String NEW_NAME = "eris";
     private static final String BAD_ID = "98765";
 
     private static final String GET_COMMAND = "get-template -templateid \"" + TEMPLATE_ID + "\"";
     private static final String UPDATE_COMMAND = "$t = get-template \"" + TEMPLATE_ID + "\";$t.name = \"" + NEW_NAME + "\";update-template -templateobject $t";
 
     protected PowerShellTemplateResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser) {
         return new PowerShellTemplateResource(TEMPLATE_ID, executor, poolMap, parser);
     }
 
     protected String formatTemplate(String name) {
         return formatXmlReturn("template",
                                new String[] { name },
                                new String[] { TEMPLATE_DESCRIPTION },
                                PowerShellTemplatesResourceTest.extraArgs);
     }
 
     @Test
     public void testGet() throws Exception {
         verifyTemplate(resource.get(setUpTemplateExpectations(GET_COMMAND,
                                                               formatTemplate(TEMPLATE_NAME))),
                        TEMPLATE_NAME);
     }
 
     @Test
     public void testGoodUpdate() throws Exception {
         verifyTemplate(
             resource.update(setUpTemplateExpectations(UPDATE_COMMAND,
                                                       formatTemplate(NEW_NAME)),
                             getTemplate(NEW_NAME)),
             NEW_NAME);
     }
 
     @Test
     public void testBadUpdate() throws Exception {
         try {
             UriInfo uriInfo = createMock(UriInfo.class);
             replayAll();
             resource.update(uriInfo,
                             getTemplate(BAD_ID, NEW_NAME));
             fail("expected WebApplicationException on bad update");
         } catch (WebApplicationException wae) {
             verifyUpdateException(wae);
         }
     }
 
     private UriInfo setUpTemplateExpectations(String command, String ret) throws Exception {
         mockStatic(PowerShellCmd.class);
         expect(PowerShellCmd.runCommand(setUpShellExpectations(), command)).andReturn(ret);
         UriInfo uriInfo = createMock(UriInfo.class);
         UriBuilder uriBuilder = createMock(UriBuilder.class);
         expect(uriInfo.getRequestUriBuilder()).andReturn(uriBuilder).anyTimes();
        expect(uriBuilder.build()).andReturn(new URI(URI_ROOT + "/templates/" + TEMPLATE_ID)).anyTimes();
         replayAll();
 
         return uriInfo;
     }
 
     private Template getTemplate(String name) {
         return getTemplate(TEMPLATE_ID, name);
     }
 
     private Template getTemplate(String id, String name) {
         Template template = new Template();
         template.setId(id);
         template.setName(name);
         return template;
     }
 
     private void verifyTemplate(Template template, String name) {
         assertNotNull(template);
         assertEquals(template.getId(), Integer.toString(name.hashCode()));
         assertEquals(template.getName(), name);
         assertEquals(template.getDescription(), TEMPLATE_DESCRIPTION);
     }
 
     private void verifyUpdateException(WebApplicationException wae) {
         assertEquals(409, wae.getResponse().getStatus());
         Fault fault = (Fault)wae.getResponse().getEntity();
         assertNotNull(fault);
         assertEquals("Broken immutability constraint", fault.getReason());
         assertEquals("Attempt to set immutable field: id", fault.getDetail());
     }
 }
