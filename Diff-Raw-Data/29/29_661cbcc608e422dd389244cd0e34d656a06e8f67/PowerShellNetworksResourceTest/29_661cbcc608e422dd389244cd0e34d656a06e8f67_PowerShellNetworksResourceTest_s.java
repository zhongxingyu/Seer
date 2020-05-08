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
 
 import com.redhat.rhevm.api.model.DataCenter;
 import com.redhat.rhevm.api.model.Network;
 
 import org.junit.Test;
 
 public class PowerShellNetworksResourceTest extends AbstractPowerShellCollectionResourceTest<Network, PowerShellNetworkResource, PowerShellNetworksResource> {
 
     public static final String DATA_CENTER_ID = "54321";
 
     public static final String[] extraArgs = new String[] { DATA_CENTER_ID };
 
    private static final String ADD_COMMAND_EPILOG = "-datacenterid \"" + DATA_CENTER_ID + "\"";
 
     private static final String REMOVE_COMMAND = "$n = get-networks;foreach ($i in $n) {  if ($i.networkid -eq \"" + Integer.toString(NAMES[1].hashCode()) + "\") {    remove-network -networkobject $i -datacenterid $i.datacenterid  }}";
 
     public PowerShellNetworksResourceTest() {
         super(new PowerShellNetworkResource("0", null, null, null), "networks", "network", extraArgs);
     }
 
     @Test
     public void testList() throws Exception {
         setUpResourceExpectations("get-networks", getSelectReturn(), NAMES);
 
         verifyCollection(
             resource.list().getNetworks(),
             NAMES, DESCRIPTIONS);
     }
 
     @Test
     public void testAdd() throws Exception {
         verifyResponse(
             resource.add(setUpAddResourceExpectations(getAddCommand() + ADD_COMMAND_EPILOG,
                                                       getAddReturn(),
                                                       NEW_NAME),
                          getModel(NEW_NAME, NEW_DESCRIPTION)),
             NEW_NAME, NEW_DESCRIPTION);
     }
 
     @Test
     public void testAddIncompleteParameters() throws Exception {
         Network model = new Network();
         model.setName(NEW_NAME);
         try {
             resource.add(setUpResourceExpectations(new String[]{}, new String[]{}, false, null), model);
             fail("expected WebApplicationException on incomplete parameters");
         } catch (WebApplicationException wae) {
              verifyIncompleteException(wae, "Network", "add", "dataCenter.id");
         }
     }
 
     @Test
     public void testRemove() throws Exception {
         setUpResourceExpectations(REMOVE_COMMAND, null);
         resource.remove(Integer.toString(NAMES[1].hashCode()));
     }
 
     @Test
     public void testGetSubResource() throws Exception {
         verifyResource(
             (PowerShellNetworkResource)resource.getNetworkSubResource(setUpResourceExpectations(null, null),
                                                                       Integer.toString(NEW_NAME.hashCode())),
             NEW_NAME);
     }
 
     protected PowerShellNetworksResource getResource() {
         return new PowerShellNetworksResource();
     }
 
     protected void populateModel(Network network) {
         DataCenter dataCenter = new DataCenter();
         dataCenter.setId("54321");
         network.setDataCenter(dataCenter);
     }
 }
