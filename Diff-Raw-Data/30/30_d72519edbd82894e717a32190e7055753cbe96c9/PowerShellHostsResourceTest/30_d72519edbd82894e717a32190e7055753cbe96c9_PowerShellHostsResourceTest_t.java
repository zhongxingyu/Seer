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
 
 import com.redhat.rhevm.api.model.Host;
 import com.redhat.rhevm.api.model.Hosts;
 
 import org.junit.Test;
 
 public class PowerShellHostsResourceTest extends AbstractPowerShellCollectionResourceTest<Host, PowerShellHostResource, PowerShellHostsResource> {
 
     private static final String ADD_COMMAND_EPILOG = "-address '127.0.0.1' -rootpassword notneeded";
     private static final String ADD_RETURN_EPILOG = "\nstatus: up";
 
     private static final String SELECT_RETURN_EPILOG = ADD_RETURN_EPILOG;
 
 
     public PowerShellHostsResourceTest() {
         super(new PowerShellHostResource("0", null), "hosts", "host");
     }
 
     @Test
     public void testList() throws Exception {
         verifyCollection(
             resource.list(setUpResourceExpectations(getSelectCommand(),
                                                     getSelectReturn(SELECT_RETURN_EPILOG),
                                                     NAMES)).getHosts(),
             NAMES);
     }
 
     @Test
     public void testQuery() throws Exception {
         verifyCollection(
            resource.list(setUpResourceExpectations(getQueryCommand(Host.class),
                                                     getQueryReturn(SELECT_RETURN_EPILOG),
                                                     getQueryParam(),
                                                     NAMES_SUBSET)).getHosts(),
             NAMES_SUBSET);
     }
 
     @Test
     public void testAdd() throws Exception {
         verifyResponse(
             resource.add(setUpAddResourceExpectations(getAddCommand() + ADD_COMMAND_EPILOG,
                                                       getAddReturn(ADD_RETURN_EPILOG),
                                                       NEW_NAME),
                          getModel(NEW_NAME)),
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
             (PowerShellHostResource)resource.getHostSubResource(setUpResourceExpectations(null, null),
                                                                 Integer.toString(NEW_NAME.hashCode())),
             NEW_NAME);
     }
 
     protected PowerShellHostsResource getResource() {
         return new PowerShellHostsResource();
     }
 
     protected void populateModel(Host host) {
         host.setAddress("127.0.0.1");
     }
 }
