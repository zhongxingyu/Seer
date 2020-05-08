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
 
 import com.redhat.rhevm.api.common.resource.UriInfoProvider;
 import com.redhat.rhevm.api.model.NIC;
 import com.redhat.rhevm.api.model.Nics;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellParser;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
 import com.redhat.rhevm.api.resource.DevicesResource;
 
 import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;
 
 
 public class PowerShellNicsResource
     extends PowerShellReadOnlyNicsResource
     implements DevicesResource<NIC, Nics> {
 
     public PowerShellNicsResource(String parentId,
                                   PowerShellPoolMap shellPools,
                                   PowerShellParser parser,
                                   String getCommand,
                                   UriInfoProvider uriProvider) {
         super(parentId, shellPools, parser, getCommand, uriProvider);
     }
 
     @Override
     public Response add(NIC nic) {
         validateParameters(nic, "name", "network.id");
         StringBuilder buf = new StringBuilder();
 
         buf.append("$v = get-vm " + PowerShellUtils.escape(parentId) + ";");
         buf.append("foreach ($i in get-networks) {");
         buf.append("  if ($i.networkid -eq " + PowerShellUtils.escape(nic.getNetwork().getId()) + ") {");
         buf.append("    $n = $i");
         buf.append("  }");
         buf.append("}");
 
         buf.append("add-networkadapter");
         buf.append(" -vmobject $v");
         buf.append(" -interfacename " + PowerShellUtils.escape(nic.getName()));
         buf.append(" -networkname $n.name");
         if (nic.getType() != null) {
            buf.append(" -interfacetype " + nic.getType().toString().toLowerCase());
         }
         if (nic.getMac() != null && nic.getMac().getAddress() != null) {
             buf.append(" -macaddress " + PowerShellUtils.escape(nic.getMac().getAddress()));
         }
 
         nic = addLinks(runAndParseSingle(buf.toString()));
 
         UriBuilder uriBuilder = getUriInfo().getAbsolutePathBuilder().path(nic.getId());
 
         return Response.created(uriBuilder.build()).entity(nic).build();
     }
 
     @Override
     public void remove(String id) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("$v = get-vm " + PowerShellUtils.escape(parentId) + ";");
 
         buf.append("foreach ($i in $v.GetNetworkAdapters()) {");
         buf.append("  if ($i.id -eq " + PowerShellUtils.escape(id) + ") {");
         buf.append("    $n = $i");
         buf.append("  }");
         buf.append("}");
         buf.append("remove-networkadapter -vmobject $v -networkadapterobject $n");
 
         PowerShellCmd.runCommand(getPool(), buf.toString());
     }
 
     @Override
     public PowerShellDeviceResource<NIC, Nics> getDeviceSubResource(String id) {
         return new PowerShellDeviceResource<NIC, Nics>(this, id);
     }
 
 }
