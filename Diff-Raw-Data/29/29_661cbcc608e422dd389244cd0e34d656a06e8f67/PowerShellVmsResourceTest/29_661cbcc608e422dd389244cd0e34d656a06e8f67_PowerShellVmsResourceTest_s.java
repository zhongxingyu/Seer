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
 
 import javax.ws.rs.WebApplicationException;
 
 import com.redhat.rhevm.api.model.Cluster;
 import com.redhat.rhevm.api.model.Display;
 import com.redhat.rhevm.api.model.DisplayType;
 import com.redhat.rhevm.api.model.Template;
 import com.redhat.rhevm.api.model.VM;
 import com.redhat.rhevm.api.powershell.enums.PowerShellBootSequence;
 
 import org.junit.Test;
 
 public class PowerShellVmsResourceTest extends AbstractPowerShellCollectionResourceTest<VM, PowerShellVmResource, PowerShellVmsResource> {
 
     public static String TEMPLATE_ID = "template1";
     public static String TEMPLATE_NAME = "cookiecutter";
     public static String CLUSTER_ID = "1";
     public static String CLUSTER_NAME = "pleiades";
 
     public static int MEMSIZE = 1024;
     public static int BOOTSEQUENCE = PowerShellBootSequence.CDN.getValue();
     public static int N_SOCKETS = 2;
     public static int N_CPUS = 4;
     public static String ISO_NAME = "foo.iso";
     public static String HOST_ID = "2";
     public static String POOL_ID = "3";
 
     public static final String[] extraArgs = new String[] { CLUSTER_ID, TEMPLATE_ID, Integer.toString(MEMSIZE), Integer.toString(BOOTSEQUENCE), Integer.toString(N_SOCKETS), Integer.toString(N_CPUS), ISO_NAME, HOST_ID, POOL_ID };
 
     private static final String ADD_COMMAND_PROLOG =
         "$templ = get-template -templateid \"" + TEMPLATE_ID + "\";";
     private static final String ADD_COMMAND_EPILOG =
        "-templateobject $templ -hostclusterid \"" + CLUSTER_ID + "\"";
 
     private static final String TEMPLATE_BY_NAME_ADD_COMMAND_PROLOG =
         "$t = select-template -searchtext \"name=" + TEMPLATE_NAME + "\";" +
         "$templ = get-template -templateid $t.TemplateId;";
 
     private static final String CLUSTER_BY_NAME_ADD_COMMAND_PROLOG =
         "$t = select-template -searchtext \"name=" + TEMPLATE_NAME + "\";" +
         "$c = select-cluster -searchtext \"name=" + CLUSTER_NAME + "\";" +
         "$templ = get-template -templateid $t.TemplateId;";
 
     private static final String CLUSTER_BY_NAME_ADD_COMMAND_EPILOG =
        "-templateobject $templ -hostclusterid $c.ClusterId";
 
     private static final String DISPLAY_ADD_COMMAND_EPILOG =
        "-numofmonitors 4 -displaytype VNC " + ADD_COMMAND_EPILOG;
 
     public PowerShellVmsResourceTest() {
         super(new PowerShellVmResource("0", null, null, null), "vms", "vm", extraArgs);
     }
 
     @Test
     public void testList() throws Exception {
         verifyCollection(
             resource.list(setUpResourceExpectations(getSelectCommand(),
                                                     getSelectReturn(),
                                                     null,
                                                     NAMES)).getVMs(),
             NAMES, DESCRIPTIONS);
     }
 
     @Test
     public void testQuery() throws Exception {
         verifyCollection(
             resource.list(setUpResourceExpectations(getQueryCommand(VM.class),
                                                     getQueryReturn(),
                                                     getQueryParam(),
                                                     NAMES_SUBSET)).getVMs(),
             NAMES_SUBSET, DESCRIPTIONS_SUBSET);
     }
 
     @Test
     public void testAddWithTemplateId() throws Exception {
         verifyResponse(
             resource.add(setUpAddResourceExpectations(ADD_COMMAND_PROLOG + getAddCommand() + ADD_COMMAND_EPILOG,
                                                       getAddReturn(),
                                                       NEW_NAME),
                          getModel(NEW_NAME, NEW_DESCRIPTION)),
             NEW_NAME, NEW_DESCRIPTION);
     }
 
     @Test
     public void testAddWithTemplateName() throws Exception {
         VM model = getModel(NEW_NAME, NEW_DESCRIPTION);
         model.getTemplate().setId(null);
         model.getTemplate().setName(TEMPLATE_NAME);
 
         verifyResponse(
             resource.add(setUpAddResourceExpectations(TEMPLATE_BY_NAME_ADD_COMMAND_PROLOG + getAddCommand() + ADD_COMMAND_EPILOG,
                                                       getAddReturn(),
                                                       NEW_NAME),
                          model),
             NEW_NAME, NEW_DESCRIPTION);
     }
 
     @Test
     public void testAddWithClusterName() throws Exception {
         VM model = getModel(NEW_NAME, NEW_DESCRIPTION);
         model.getTemplate().setId(null);
         model.getTemplate().setName(TEMPLATE_NAME);
         model.getCluster().setId(null);
         model.getCluster().setName(CLUSTER_NAME);
 
         verifyResponse(
             resource.add(setUpAddResourceExpectations(CLUSTER_BY_NAME_ADD_COMMAND_PROLOG + getAddCommand() + CLUSTER_BY_NAME_ADD_COMMAND_EPILOG,
                                                       getAddReturn(),
                                                       NEW_NAME),
                          model),
             NEW_NAME, NEW_DESCRIPTION);
     }
 
     @Test
     public void testAddWithDisplay() throws Exception {
         verifyResponse(
             resource.add(setUpAddResourceExpectations(ADD_COMMAND_PROLOG + getAddCommand() + DISPLAY_ADD_COMMAND_EPILOG,
                                                       getAddReturn(),
                                                       NEW_NAME),
                          updateDisplay(getModel(NEW_NAME, NEW_DESCRIPTION))),
             NEW_NAME, NEW_DESCRIPTION);
     }
 
     @Test
     public void testAddIncompleteParameters() throws Exception {
         VM model = new VM();
         model.setName(NEW_NAME);
         try {
             resource.add(setUpResourceExpectations(new String[]{}, new String[]{}, false, null), model);
             fail("expected WebApplicationException on incomplete parameters");
         } catch (WebApplicationException wae) {
              verifyIncompleteException(wae, "VM", "add", "template.id|name", "cluster.id|name");
         }
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
 
     private VM updateDisplay(VM vm) {
         vm.setDisplay(new Display());
         vm.getDisplay().setType(DisplayType.VNC);
         vm.getDisplay().setMonitors(4);
         return vm;
     }
 }
