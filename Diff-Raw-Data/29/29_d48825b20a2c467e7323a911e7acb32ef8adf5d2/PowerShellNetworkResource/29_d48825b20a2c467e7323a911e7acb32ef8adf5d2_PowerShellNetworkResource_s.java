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
 
 import java.util.ArrayList;
 import java.util.concurrent.Executor;
 
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.DataCenter;
 import com.redhat.rhevm.api.model.Network;
 import com.redhat.rhevm.api.resource.NetworkResource;
 import com.redhat.rhevm.api.common.resource.AbstractActionableResource;
 import com.redhat.rhevm.api.powershell.model.PowerShellNetwork;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 
 public class PowerShellNetworkResource extends AbstractActionableResource<Network> implements NetworkResource {
 
     public PowerShellNetworkResource(String id, Executor executor) {
         super(id, executor);
     }
 
     public static ArrayList<Network> runAndParse(String command) {
         return PowerShellNetwork.parse(PowerShellCmd.runCommand(command));
     }
 
     public static Network runAndParseSingle(String command) {
         ArrayList<Network> networks = runAndParse(command);
 
         return !networks.isEmpty() ? networks.get(0) : null;
     }
 
     public static Network addLinks(Network network, UriInfo uriInfo, UriBuilder uriBuilder) {
         network.setHref(uriBuilder.build().toString());
 
         UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
         DataCenter dataCenter = network.getDataCenter();
         dataCenter.setHref(PowerShellDataCentersResource.getHref(baseUriBuilder, dataCenter.getId()));
 
         return network;
     }
 
     @Override
     public Network get(UriInfo uriInfo) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("$n = get-networks\n");
         buf.append("foreach ($i in $n) {");
        buf.append("  if ($i -eq '" + getId() + "') {");
         buf.append("    $i");
         buf.append("  }");
         buf.append("}");
 
         return addLinks(runAndParseSingle(buf.toString()), uriInfo, uriInfo.getRequestUriBuilder());
     }
 
     @Override
     public Network update(HttpHeaders headers, UriInfo uriInfo, Network network) {
         validateUpdate(network, headers);
 
         StringBuilder buf = new StringBuilder();
 
         buf.append("foreach ($i in $n) {");
        buf.append("  if ($i -eq '" + getId() + "') {");
 
         if (network.getName() != null) {
             buf.append("    $i.name = '" + network.getName() + "'");
         }
 
         buf.append("    update-network");
         buf.append(" -networkobject $i");
         buf.append(" -datacenterid " + network.getDataCenter().getId());
 
         buf.append("  }");
         buf.append("}\n");
 
         return addLinks(runAndParseSingle(buf.toString()), uriInfo, uriInfo.getRequestUriBuilder());
     }
 }
