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
 import com.redhat.rhevm.api.model.VM;
 import com.redhat.rhevm.api.model.VMs;
 
 import org.junit.Test;
 
 public class PowerShellVmsResourceTest extends AbstractPowerShellCollectionResourceTest<VM, PowerShellVmResource, PowerShellVmsResource> {
 
     private static String TEMPLATE_ID = "template1";
     private static String TEMPLATE_NAME = "cookiecutter";
     private static String CLUSTER_ID = "cluster1";
     private static String CLUSTER_NAME = "pleiades";
 
     private static final String ADD_COMMAND_PROLOG =
         "$templ = get-template -templateid '" + TEMPLATE_ID + "'\n";
     private static final String ADD_COMMAND_EPILOG =
         "-templateobject $templ -hostclusterid '" + CLUSTER_ID + "'";
 
     private static final String TEMPLATE_BY_NAME_ADD_COMMAND_PROLOG =
         "$t = select-template -searchtext name=" + TEMPLATE_NAME + "\n" +
         "$templ = get-template -templateid $t.TemplateId\n";
 
     private static final String CLUSTER_BY_NAME_ADD_COMMAND_PROLOG =
         "$t = select-template -searchtext name=" + TEMPLATE_NAME + "\n" +
          "$c = select-cluster -searchtext name=" + CLUSTER_NAME + "\n" +
         "$templ = get-template -templateid $t.TemplateId\n";
 
     private static final String CLUSTER_BY_NAME_ADD_COMMAND_EPILOG =
         "-templateobject $templ -hostclusterid $c.ClusterId";
 
     private static final String OTHER_PROPS = "memorysize: 1024\ndefaultbootsequence: CDN\nnumofsockets: 2\nnumofcpuspersocket: 4\npoolid: -1\n";
 
     private static final String SELECT_RETURN_EPILOG = "\nhostclusterid: " + CLUSTER_ID + "\ntemplateid: " + TEMPLATE_ID + "\n" + OTHER_PROPS;
     private static final String ADD_RETURN_EPILOG    = "\nhostclusterid: " + CLUSTER_ID + "\ntemplateid: " + TEMPLATE_ID + "\n" + OTHER_PROPS;
 
     public PowerShellVmsResourceTest() {
         super(new PowerShellVmResource("0", null), "vms", "vm");
     }
 
     @Test
     public void testList() throws Exception {
         verifyCollection(
             resource.list(setUpResourceExpectations(getSelectCommand(),
                                                     getSelectReturn(SELECT_RETURN_EPILOG),
                                                     null,
                                                     NAMES)).getVMs(),
             NAMES);
     }
 
     @Test
     public void testQuery() throws Exception {
         verifyCollection(
            resource.list(setUpResourceExpectations(getQueryCommand(VMs.class),
                                                     getQueryReturn(SELECT_RETURN_EPILOG),
                                                     getQueryParam(),
                                                     NAMES_SUBSET)).getVMs(),
             NAMES_SUBSET);
     }
 
     @Test
     public void testAddWithTemplateId() throws Exception {
         verifyResponse(
             resource.add(setUpAddResourceExpectations(ADD_COMMAND_PROLOG + getAddCommand() + ADD_COMMAND_EPILOG,
                                                       getAddReturn(ADD_RETURN_EPILOG),
                                                       NEW_NAME),
                          getModel(NEW_NAME)),
             NEW_NAME);
     }
 
     @Test
     public void testAddWithTemplateName() throws Exception {
         VM model = getModel(NEW_NAME);
         model.getTemplate().setId(null);
         model.getTemplate().setName(TEMPLATE_NAME);
 
         verifyResponse(
             resource.add(setUpAddResourceExpectations(TEMPLATE_BY_NAME_ADD_COMMAND_PROLOG + getAddCommand() + ADD_COMMAND_EPILOG,
                                                       getAddReturn(ADD_RETURN_EPILOG),
                                                       NEW_NAME),
                          model),
             NEW_NAME);
     }
 
     @Test
     public void testAddWithClusterName() throws Exception {
         VM model = getModel(NEW_NAME);
         model.getTemplate().setId(null);
         model.getTemplate().setName(TEMPLATE_NAME);
         model.getCluster().setId(null);
         model.getCluster().setName(CLUSTER_NAME);
 
         verifyResponse(
             resource.add(setUpAddResourceExpectations(CLUSTER_BY_NAME_ADD_COMMAND_PROLOG + getAddCommand() + CLUSTER_BY_NAME_ADD_COMMAND_EPILOG,
                                                       getAddReturn(ADD_RETURN_EPILOG),
                                                       NEW_NAME),
                          model),
             NEW_NAME);
     }
 
     @Test
     public void testRemove() throws Exception {
         setUpResourceExpectations(getRemoveCommand(), null);
         resource.remove(Integer.toString(NAMES[1].hashCode()));
     }
 
     @Test
     public void testGetSubResource() throws Exception {
         verifyResource(
             (PowerShellVmResource)resource.getVmSubResource(setUpResourceExpectations(null, null),
                                                             Integer.toString(NEW_NAME.hashCode())),
             NEW_NAME);
     }
 
     protected PowerShellVmsResource getResource() {
         return new PowerShellVmsResource();
     }
 
     protected void populateModel(VM vm) {
         Template template = new Template();
         template.setId(TEMPLATE_ID);
         vm.setTemplate(template);
 
         Cluster cluster = new Cluster();
         cluster.setId(CLUSTER_ID);
         vm.setCluster(cluster);
     }
 }
