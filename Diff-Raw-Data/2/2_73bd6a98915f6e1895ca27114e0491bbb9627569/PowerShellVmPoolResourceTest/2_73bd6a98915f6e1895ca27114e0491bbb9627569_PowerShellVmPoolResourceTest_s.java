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
 
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.common.resource.UriInfoProvider;
 import com.redhat.rhevm.api.model.Fault;
 import com.redhat.rhevm.api.model.VmPool;
 
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellParser;
 import com.redhat.rhevm.api.powershell.util.PowerShellPool;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 
 import org.junit.Test;
 
 import static org.easymock.classextension.EasyMock.expect;
 
 import static org.powermock.api.easymock.PowerMock.createMock;
 import static org.powermock.api.easymock.PowerMock.mockStatic;
 import static org.powermock.api.easymock.PowerMock.replayAll;
 
 
 public class PowerShellVmPoolResourceTest extends AbstractPowerShellResourceTest<VmPool, PowerShellVmPoolResource> {
 
     private static final String POOL_NAME = "fionnula";
     private static final String POOL_ID = Integer.toString(POOL_NAME.hashCode());
     private static final String BAD_ID = "98765";
     private static final String NEW_NAME = "fidelma";
 
     private static final String GET_COMMAND = "get-vmpool -vmpoolid \"" + POOL_ID + "\"";
    private static final String UPDATE_COMMAND = "$v = get-vmpool \"" + POOL_ID + "\";$v.name = \"" + NEW_NAME + "\";update-vmpool -vmpoolobject $v";
 
     protected PowerShellVmPoolResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
         return new PowerShellVmPoolResource(POOL_ID, executor, uriProvider, poolMap, parser);
     }
 
     protected String formatVmPool(String name) {
         return formatVmPool("vmpool", name);
     }
 
     protected String formatVmPool(String type, String name) {
         return formatXmlReturn(type,
                                new String[] { name },
                                new String[] { "" },
                                PowerShellVmPoolsResourceTest.extraArgs);
     }
 
     protected String formatCluster(String name) {
         return formatCluster("cluster", name);
     }
 
     protected String formatCluster(String type, String name) {
         return formatXmlReturn("cluster",
                                new String[] { name },
                                new String[] { "" },
                                PowerShellClustersResourceTest.extraArgs);
     }
 
     protected String formatTemplate(String name) {
         return formatXmlReturn("template",
                                new String[] { name },
                                new String[] { "" },
                                PowerShellTemplatesResourceTest.extraArgs);
     }
 
     @Test
     public void testGet() throws Exception {
         String [] commands = { GET_COMMAND,
                                PowerShellVmPoolsResourceTest.LOOKUP_CLUSTER_COMMAND,
                                PowerShellVmPoolsResourceTest.LOOKUP_TEMPLATE_COMMAND };
         String [] returns  = { formatVmPool(POOL_NAME),
                                formatCluster(PowerShellVmPoolsResourceTest.CLUSTER_NAME),
                                formatTemplate(PowerShellVmPoolsResourceTest.TEMPLATE_NAME) };
 
         setUriInfo(setUpVmPoolExpectations(commands, returns, POOL_NAME));
         verifyVmPool(resource.get(), POOL_NAME);
     }
 
     @Test
     public void testGet22() throws Exception {
         String [] commands = { GET_COMMAND,
                                PowerShellVmPoolsResourceTest.LOOKUP_CLUSTER_COMMAND,
                                PowerShellVmPoolsResourceTest.LOOKUP_TEMPLATE_COMMAND };
         String [] returns  = { formatVmPool("vmpool22", POOL_NAME),
                                formatCluster("cluster22", PowerShellVmPoolsResourceTest.CLUSTER_NAME),
                                formatTemplate(PowerShellVmPoolsResourceTest.TEMPLATE_NAME) };
 
         setUriInfo(setUpVmPoolExpectations(commands, returns, POOL_NAME));
         verifyVmPool(resource.get(), POOL_NAME);
     }
 
     @Test
     public void testGoodUpdate() throws Exception {
         String [] commands = { UPDATE_COMMAND,
                                PowerShellVmPoolsResourceTest.LOOKUP_CLUSTER_COMMAND,
                                PowerShellVmPoolsResourceTest.LOOKUP_TEMPLATE_COMMAND };
         String [] returns  = { formatVmPool(NEW_NAME),
                                formatCluster(PowerShellVmPoolsResourceTest.CLUSTER_NAME),
                                formatTemplate(PowerShellVmPoolsResourceTest.TEMPLATE_NAME) };
 
         setUriInfo(setUpVmPoolExpectations(commands, returns, NEW_NAME));
         verifyVmPool(resource.update(getVmPool(NEW_NAME)), NEW_NAME);
     }
 
     @Test
     public void testBadUpdate() throws Exception {
         try {
             setUriInfo(createMock(UriInfo.class));
             replayAll();
             resource.update(getVmPool(BAD_ID, NEW_NAME));
             fail("expected WebApplicationException on bad update");
         } catch (WebApplicationException wae) {
             verifyUpdateException(wae);
         }
     }
 
     private UriInfo setUpVmPoolExpectations(String commands[], String[] rets, String name) throws Exception {
         if (commands != null) {
             mockStatic(PowerShellCmd.class);
             int times = Math.min(commands.length, rets.length);
             PowerShellPool pool = setUpPoolExpectations(times - 1);
             for (int i = 0 ; i < times ; i++) {
                 if (commands[i] != null) {
                     expect(PowerShellCmd.runCommand(pool, commands[i])).andReturn(rets[i]);
                 }
             }
         }
         UriInfo uriInfo = setUpBasicUriExpectations();
         replayAll();
         return uriInfo;
     }
 
     private VmPool getVmPool(String name) {
         return getVmPool(POOL_ID, name);
     }
 
     private VmPool getVmPool(String id, String name) {
         VmPool pool = new VmPool();
         pool.setId(id);
         pool.setName(name);
         return pool;
     }
 
     private void verifyVmPool(VmPool pool, String name) {
         assertNotNull(pool);
         assertEquals(pool.getId(), Integer.toString(name.hashCode()));
         assertEquals(pool.getName(), name);
     }
 
     private void verifyUpdateException(WebApplicationException wae) {
         assertEquals(409, wae.getResponse().getStatus());
         Fault fault = (Fault)wae.getResponse().getEntity();
         assertNotNull(fault);
         assertEquals("Broken immutability constraint", fault.getReason());
         assertEquals("Attempt to set immutable field: id", fault.getDetail());
     }
 }
