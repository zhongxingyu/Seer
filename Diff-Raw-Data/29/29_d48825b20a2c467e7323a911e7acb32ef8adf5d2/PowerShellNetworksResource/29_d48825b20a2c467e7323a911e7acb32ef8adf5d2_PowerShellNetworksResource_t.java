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
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.IP;
 import com.redhat.rhevm.api.model.Network;
 import com.redhat.rhevm.api.model.Networks;
 import com.redhat.rhevm.api.resource.NetworkResource;
 import com.redhat.rhevm.api.resource.NetworksResource;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 
 public class PowerShellNetworksResource
     extends AbstractPowerShellCollectionResource<Network, PowerShellNetworkResource>
     implements NetworksResource {
 
     @Override
     public Networks list(UriInfo uriInfo) {
         Networks ret = new Networks();
         for (Network network : PowerShellNetworkResource.runAndParse("get-networks")) {
             UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(network.getId());
             ret.getNetworks().add(PowerShellNetworkResource.addLinks(network, uriInfo, uriBuilder));
         }
         return ret;
     }
 
     @Override
     public Response add(UriInfo uriInfo, Network network) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("add-network");
         buf.append(" -name '" + network.getName() + "'");
         buf.append(" -datacenterid " + network.getDataCenter().getId());
 
         if (network.getDescription() != null) {
             buf.append(" -description '" + network.getDescription() + "'");
         }
 
         if (network.getIp() != null) {
             IP ip = network.getIp();
 
             if (ip.getAddress() != null) {
                 buf.append(" -address " + ip.getAddress());
             }
             if (ip.getNetmask() != null) {
                 buf.append(" -netmask " + ip.getNetmask());
             }
             if (ip.getGateway() != null) {
                 buf.append(" -gateway " + ip.getGateway());
             }
         }
 
         if (network.getVlan() != null) {
             buf.append(" -vlanid " + network.getVlan().getId());
         }
 
         if (network.isStp() != null && network.isStp()) {
             buf.append(" -stp");
         }
 
         network = PowerShellNetworkResource.runAndParseSingle(buf.toString());
 
         UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(network.getId());
 
         network = PowerShellNetworkResource.addLinks(network, uriInfo, uriBuilder);
 
         return Response.created(uriBuilder.build()).entity(network).build();
     }
 
     @Override
     public void remove(String id) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("$n = get-networks\n");
         buf.append("foreach ($i in $n) {");
        buf.append("  if ($i.networkid -eq '" + id + "') {");
         buf.append("    remove-network");
         buf.append(" -networkobject $i");
         buf.append(" -datacenterid $i.datacenterid");
         buf.append("  }");
         buf.append("}");
 
         PowerShellCmd.runCommand(buf.toString());
 
         removeSubResource(id);
     }
 
     @Override
     public NetworkResource getNetworkSubResource(UriInfo uriInfo, String id) {
         return getSubResource(id);
     }
 
     protected PowerShellNetworkResource createSubResource(String id) {
         return new PowerShellNetworkResource(id, getExecutor());
     }
 
     /**
      * Build an absolute URI for a given network
      *
      * @param baseUriBuilder a UriBuilder representing the base URI
      * @param id the network ID
      * @return an absolute URI
      */
     public static String getHref(UriBuilder baseUriBuilder, String id) {
         return baseUriBuilder.clone().path("networks").path(id).build().toString();
     }
 }
