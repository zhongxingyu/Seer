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
 
 import com.redhat.rhevm.api.model.DataCenter;
 import com.redhat.rhevm.api.model.Network;
 import com.redhat.rhevm.api.model.Networks;
 
 import org.junit.Test;
 
 public class PowerShellNetworksResourceTest extends AbstractPowerShellCollectionResourceTest<Network, PowerShellNetworkResource, PowerShellNetworksResource> {
 
     private static final String ADD_COMMAND_EPILOG = "-datacenterid 54321";
     private static final String ADD_RETURN_EPILOG = "\ndatacenterid: 54321";
 
    private static final String REMOVE_COMMAND = "$n = get-networks\nforeach ($i in $n) {  if ($i -eq '3121815') {    remove-network -networkobject $i -datacenterid $i.datacenterid  }}";
 
     public PowerShellNetworksResourceTest() {
         super(new PowerShellNetworkResource("0", null), "networks", "network");
     }
 
     @Test
     public void testList() throws Exception {
         verifyCollection(
             resource.list(setUpResourceExpectations("get-networks",
                                                     getSelectReturn(ADD_RETURN_EPILOG),
                                                     3,
                                                     NAMES)).getNetworks(),
             NAMES);
     }
 
     @Test
     public void testAdd() throws Exception {
         verifyResponse(
             resource.add(setUpResourceExpectations(getAddCommand() + ADD_COMMAND_EPILOG,
                                                    getAddReturn(ADD_RETURN_EPILOG),
                                                    1,
                                                    NEW_NAME),
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
