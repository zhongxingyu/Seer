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
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 
 public class PowerShellHostsResource
     extends AbstractPowerShellCollectionResource<Host, PowerShellHostResource>
     implements HostsResource {
 
     /* FIXME: would like to do:
      * private @Context UriInfo uriInfo;
      */
 
     @Override
     public Hosts list(UriInfo uriInfo) {
         Hosts ret = new Hosts();
         for (Host host : PowerShellHostResource.runAndParse("select-host")) {
             UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(host.getId());
             ret.getHosts().add(PowerShellHostResource.addLinks(host, uriBuilder));
         }
         return ret;
     }
 
 /* FIXME: move this
    @Override
    public VMs search(String criteria) {
    return runAndParse("select-host " + criteria);
    }
 */
 
     @Override
     public Response add(UriInfo uriInfo, Host host) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("add-host");
 
         if (host.getName() != null) {
             buf.append(" -name " + host.getName());
         }
 
         if (host.getAddress() != null) {
             buf.append(" -hostname " + host.getAddress());
         }
 
         if (host.getRootPassword() != null) {
             buf.append(" -rootpassword " + host.getRootPassword());
         }
 
         host = PowerShellHostResource.runAndParseSingle(buf.toString());
 
         UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().path(host.getId());
 
         host = PowerShellHostResource.addLinks(host, uriBuilder);
 
        return Response.created(uriBuilder.build()).entity(uriBuilder).build();
     }
 
     @Override
     public void remove(String id) {
         PowerShellCmd.runCommand("remove-host -hostid " + id);
         removeSubResource(id);
     }
 
     @Override
     public HostResource getHostSubResource(UriInfo uriInfo, String id) {
         return getSubResource(id);
     }
 
     protected PowerShellHostResource createSubResource(String id) {
         return new PowerShellHostResource(id, getExecutor());
     }
 
 /*
     @Override
     public void connectStorage(String id, String storageDevice) {
     }
 */
 }
