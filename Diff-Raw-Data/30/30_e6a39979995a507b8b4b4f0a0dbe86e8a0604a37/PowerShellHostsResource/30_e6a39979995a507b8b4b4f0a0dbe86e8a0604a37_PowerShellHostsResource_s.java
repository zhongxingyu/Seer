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
 
 import com.redhat.rhevm.api.model.Host;
 import com.redhat.rhevm.api.model.Hosts;
 import com.redhat.rhevm.api.resource.HostResource;
 import com.redhat.rhevm.api.resource.HostsResource;
 import com.redhat.rhevm.api.common.util.LinkHelper;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
 
 
 public class PowerShellHostsResource
     extends AbstractPowerShellCollectionResource<Host, PowerShellHostResource>
     implements HostsResource {
 
     @Override
     public Hosts list(UriInfo uriInfo) {
         Hosts ret = new Hosts();
        for (Host host : PowerShellHostResource.runAndParse(getSelectCommand("select-host", uriInfo, Hosts.class))) {
             ret.getHosts().add(LinkHelper.addLinks(host));
         }
         return ret;
     }
 
     @Override
     public Response add(UriInfo uriInfo, Host host) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("add-host");
 
         buf.append(" -name " + PowerShellUtils.escape(host.getName()));
         buf.append(" -address " + PowerShellUtils.escape(host.getAddress()));
 
         // It appears that the root password is not really needed here
         buf.append(" -rootpassword notneeded");
 
         host = PowerShellHostResource.runAndParseSingle(buf.toString());
         host = LinkHelper.addLinks(host);
 
         UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(host.getId());
 
         return Response.created(uriBuilder.build()).entity(host).build();
     }
 
     @Override
     public void remove(String id) {
         PowerShellCmd.runCommand("remove-host -hostid " + PowerShellUtils.escape(id));
         removeSubResource(id);
     }
 
     @Override
     public HostResource getHostSubResource(UriInfo uriInfo, String id) {
         return getSubResource(id);
     }
 
     protected PowerShellHostResource createSubResource(String id) {
         return new PowerShellHostResource(id, getExecutor());
     }
 }
