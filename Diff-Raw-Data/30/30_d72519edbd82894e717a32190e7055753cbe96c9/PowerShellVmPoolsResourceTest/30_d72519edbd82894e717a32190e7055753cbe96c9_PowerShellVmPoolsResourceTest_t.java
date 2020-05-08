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
 
 import com.redhat.rhevm.api.model.Cluster;
 import com.redhat.rhevm.api.model.Template;
 import com.redhat.rhevm.api.model.VmPool;
 import com.redhat.rhevm.api.model.VmPools;
 
 import org.junit.Test;
 
 
 public class PowerShellVmPoolsResourceTest extends AbstractPowerShellCollectionResourceTest<VmPool, PowerShellVmPoolResource, PowerShellVmPoolsResource> {
 
     public static final String CLUSTER_NAME = "Default";
     public static final String CLUSTER_ID = "54321";
     public static final String TEMPLATE_NAME = "foo";
     public static final String TEMPLATE_ID = "12345";
 
     public static final String GET_RETURN_EPILOG = "\nvmcount: 15\ncluster: " + CLUSTER_NAME + "\ntemplate: " + TEMPLATE_NAME + "\n";
 
     private static final String ADD_COMMAND = "add-vmpool " + "-vmpoolname '" + NEW_NAME + "' -templateid '" + TEMPLATE_ID + "' -hostclusterid '" + CLUSTER_ID + "' -pooltype Automatic";
     private static final String REMOVE_COMMAND = "$p = get-vmpool -vmpoolid '" + NAMES[1].hashCode() + "'\nremove-vmpool -name $p.name";
 
     public static final String LOOKUP_CLUSTER_COMMAND = "select-cluster -searchtext 'name = " + CLUSTER_NAME + "'";
     public static final String LOOKUP_CLUSTER_RETURN = "clusterid: " + CLUSTER_ID + "\nname: " + CLUSTER_NAME + "\ndatacenterid: 666";
     public static final String LOOKUP_TEMPLATE_COMMAND = "select-template -searchtext 'name = " + TEMPLATE_NAME + "'";
     public static final String LOOKUP_TEMPLATE_RETURN = "templateid: " + TEMPLATE_ID + "\nname: " + TEMPLATE_NAME;
 
     public PowerShellVmPoolsResourceTest() {
 	super(new PowerShellVmPoolResource("0"), "vmpools", "vmpool");
     }
 
     @Test
     public void testList() throws Exception {
         String [] commands = { getSelectCommand(),
                                LOOKUP_CLUSTER_COMMAND, LOOKUP_TEMPLATE_COMMAND,
                                LOOKUP_CLUSTER_COMMAND, LOOKUP_TEMPLATE_COMMAND,
                                LOOKUP_CLUSTER_COMMAND, LOOKUP_TEMPLATE_COMMAND };
         String [] returns = { getSelectReturn(GET_RETURN_EPILOG),
                               LOOKUP_CLUSTER_RETURN, LOOKUP_TEMPLATE_RETURN,
                               LOOKUP_CLUSTER_RETURN, LOOKUP_TEMPLATE_RETURN,
                               LOOKUP_CLUSTER_RETURN, LOOKUP_TEMPLATE_RETURN };
         verifyCollection(
             resource.list(setUpResourceExpectations(commands, returns, null, NAMES)).getVmPools(),
             NAMES);
     }
 
 
     @Test
     public void testQuery() throws Exception {
        String [] commands = { getQueryCommand(VmPool.class),
                                LOOKUP_CLUSTER_COMMAND, LOOKUP_TEMPLATE_COMMAND,
                                LOOKUP_CLUSTER_COMMAND, LOOKUP_TEMPLATE_COMMAND };
         String [] returns = { getQueryReturn(GET_RETURN_EPILOG),
                               LOOKUP_CLUSTER_RETURN, LOOKUP_TEMPLATE_RETURN,
                               LOOKUP_CLUSTER_RETURN, LOOKUP_TEMPLATE_RETURN };
         verifyCollection(
             resource.list(setUpResourceExpectations(commands,
                                                     returns,
                                                     getQueryParam(),
                                                     NAMES_SUBSET)).getVmPools(),
             NAMES_SUBSET);
     }
 
     @Test
     public void testAdd() throws Exception {
         String [] commands = { ADD_COMMAND, LOOKUP_CLUSTER_COMMAND, LOOKUP_TEMPLATE_COMMAND };
         String [] returns = { getAddReturn(GET_RETURN_EPILOG), LOOKUP_CLUSTER_RETURN, LOOKUP_TEMPLATE_RETURN };
         verifyResponse(
             resource.add(setUpAddResourceExpectations(commands, returns, NEW_NAME),
                          getModel(NEW_NAME)),
             NEW_NAME);
     }
 
     @Test
     public void testRemove() throws Exception {
         setUpResourceExpectations(REMOVE_COMMAND, null);
         resource.remove(Integer.toString(NAMES[1].hashCode()));
     }
 
     @Test
     public void testGetSubResource() throws Exception {
         verifyResource(
             (PowerShellVmPoolResource)resource.getVmPoolSubResource(setUpResourceExpectations(null, null),
                                                                     Integer.toString(NEW_NAME.hashCode())),
             NEW_NAME);
     }
 
     protected PowerShellVmPoolsResource getResource() {
 	return new PowerShellVmPoolsResource();
     }
 
     protected void populateModel(VmPool pool) {
         Cluster cluster = new Cluster();
         cluster.setId(CLUSTER_ID);
         pool.setCluster(cluster);
 
         Template template = new Template();
         template.setId(TEMPLATE_ID);
         pool.setTemplate(template);
     }
 }
