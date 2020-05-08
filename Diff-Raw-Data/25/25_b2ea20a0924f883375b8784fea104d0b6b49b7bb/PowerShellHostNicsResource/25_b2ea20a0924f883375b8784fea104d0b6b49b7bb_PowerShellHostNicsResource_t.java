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
 
 import java.util.List;
 import java.util.concurrent.Executor;
 
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.resource.MediaType;
 
 import com.redhat.rhevm.api.model.HostNIC;
 import com.redhat.rhevm.api.model.HostNics;
 import com.redhat.rhevm.api.model.Link;
 import com.redhat.rhevm.api.model.Network;
 import com.redhat.rhevm.api.model.Slaves;
 import com.redhat.rhevm.api.common.util.JAXBHelper;
 import com.redhat.rhevm.api.common.util.LinkHelper;
 import com.redhat.rhevm.api.powershell.model.PowerShellHostNIC;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellParser;
 import com.redhat.rhevm.api.powershell.util.PowerShellPool;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
 import com.redhat.rhevm.api.resource.HostNicResource;
 import com.redhat.rhevm.api.resource.HostNicsResource;
 
 import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;
 
 @Produces(MediaType.APPLICATION_XML)
 public class PowerShellHostNicsResource implements HostNicsResource {
 
     protected String hostId;
     protected Executor executor;
     protected PowerShellPoolMap shellPools;
     protected PowerShellParser parser;
 
     public PowerShellHostNicsResource(String hostId,
                                       Executor executor,
                                       PowerShellPoolMap shellPools,
                                       PowerShellParser parser) {
         this.hostId = hostId;
         this.executor = executor;
         this.shellPools = shellPools;
         this.parser = parser;
     }
 
     public Executor getExecutor() {
         return executor;
     }
 
     public PowerShellPool getPool() {
         return shellPools.get();
     }
 
     public PowerShellParser getParser() {
         return parser;
     }
 
     public String getHostId() {
         return hostId;
     }
 
     public List<PowerShellHostNIC> runAndParse(String command) {
         return PowerShellHostNIC.parse(getParser(), hostId, PowerShellCmd.runCommand(getPool(), command));
     }
 
     public PowerShellHostNIC runAndParseSingle(String command) {
         List<PowerShellHostNIC> nics = runAndParse(command);
         return !nics.isEmpty() ? nics.get(0) : null;
     }
 
     /* Map the network names to network ID on the supplied host network
      * interfaces. The powershell output only includes the network name.
      *
      * @param nic  the host NIC to modify
      * @return     the modified host NIC
      */
     private HostNIC lookupNetworkId(HostNIC nic) {
         if (!nic.isSetNetwork()) {
             return nic;
         }
 
         StringBuilder buf = new StringBuilder();
 
         buf.append("$n = get-networks; ");
         buf.append("foreach ($i in $n) { ");
         buf.append("if ($i.name -eq " + PowerShellUtils.escape(nic.getNetwork().getName()) + ") { ");
         buf.append("$i; break ");
         buf.append("} ");
         buf.append("}");
 
         Network network = new Network();
         network.setId(PowerShellNetworkResource.runAndParseSingle(getPool(), getParser(), buf.toString()).getId());
         nic.setNetwork(network);
 
         return nic;
     }
 
     private PowerShellHostNIC lookupBonds(PowerShellHostNIC nic) {
         if (nic.getBondName() == null && nic.getBondInterfaces().size() == 0) {
             return nic;
         }
 
         StringBuilder buf = new StringBuilder();
 
         buf.append("$h = get-host " + PowerShellUtils.escape(hostId) + "; ");
         buf.append("foreach ($n in $h.getnetworkadapters()) { ");
         if (nic.getBondName() != null) {
             buf.append("if ($n.name -eq " + PowerShellUtils.escape(nic.getBondName()) + ") { $n } ");
         } else {
             for (String name : nic.getBondInterfaces()) {
                 buf.append("if ($n.name -eq " + PowerShellUtils.escape(name) + ") { $n } ");
             }
         }
         buf.append("}");
 
         List<PowerShellHostNIC> bondNics = runAndParse(buf.toString());
 
         if (nic.getBondName() != null) {
             String masterId = bondNics.get(0).getId();
 
             UriBuilder uriBuilder = LinkHelper.getUriBuilder(nic.getHost()).path("nics");
 
             Link master = new Link();
             master.setRel("master");
             master.setHref(uriBuilder.clone().path(masterId).build().toString());
             nic.getLinks().add(master);
         } else {
             nic.setSlaves(new Slaves());
             for (PowerShellHostNIC bond : bondNics) {
                 HostNIC slave = new HostNIC();
                 slave.setId(bond.getId());
                 slave.setHost(bond.getHost());
                nic.getSlaves().getSlaves().add(LinkHelper.addLinks(slave));
                slave.setHost(null);
             }
         }
 
         return nic;
     }
 
     public HostNIC addLinks(PowerShellHostNIC nic) {
         nic = lookupBonds(nic);
 
         HostNIC ret = JAXBHelper.clone("host_nic", HostNIC.class, nic);
 
        return LinkHelper.addLinks(lookupNetworkId(ret));
     }
 
     public PowerShellHostNIC getHostNic(String nicId) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("$h = get-host " + PowerShellUtils.escape(hostId) + "; ");
         buf.append("foreach ($n in $h.getnetworkadapters()) { ");
         buf.append("if ($n.id -eq " + PowerShellUtils.escape(nicId) + ") { ");
         buf.append("$n; break ");
         buf.append("} ");
         buf.append("}");
 
         return runAndParseSingle(buf.toString());
     }
 
     @Override
     public HostNics list() {
         StringBuilder buf = new StringBuilder();
 
         buf.append("$h = get-host " + PowerShellUtils.escape(hostId) + "; ");
         buf.append("$h.getnetworkadapters()");
 
         HostNics ret = new HostNics();
         for (PowerShellHostNIC nic : runAndParse(buf.toString())) {
             ret.getHostNics().add(addLinks(nic));
         }
         return ret;
     }
 
     @Override
     public Response add(UriInfo uriInfo, HostNIC nic) {
         validateParameters(nic, "name", "network.id|name", "slaves.id|name");
 
         StringBuilder buf = new StringBuilder();
 
         buf.append("$h = get-host " + PowerShellUtils.escape(hostId) + "; ");
 
         buf.append("foreach ($n in get-networks) { ");
         if (nic.getNetwork().isSetId()) {
             buf.append("if ($n.networkid -eq " + PowerShellUtils.escape(nic.getNetwork().getId()) + ") { ");
         } else {
             buf.append("if ($n.name -eq " + PowerShellUtils.escape(nic.getNetwork().getName()) + ") { ");
         }
         buf.append("$net = $n; break ");
         buf.append("} ");
         buf.append("} ");
 
         buf.append("$nics = @(); ");
         buf.append("foreach ($nic in $h.getnetworkadapters()) { ");
         for (HostNIC slave : nic.getSlaves().getSlaves()) {
             if (slave.isSetId()) {
                 buf.append("if ($nic.id -eq " + PowerShellUtils.escape(slave.getId()) + ") { ");
             } else {
                 buf.append("if ($nic.name -eq " + PowerShellUtils.escape(slave.getName()) + ") { ");
             }
             buf.append("$nics += $nic ");
             buf.append("} ");
         }
         buf.append("} ");
 
         buf.append("$h = add-bond");
         buf.append(" -bondname " + PowerShellUtils.escape(nic.getName()));
         buf.append(" -hostobject $h");
         buf.append(" -network $net");
         buf.append(" -networkadapters $nics");
         buf.append("; ");
         buf.append("foreach ($nic in $h.getnetworkadapters()) { ");
         buf.append("if ($nic.name -eq " + PowerShellUtils.escape(nic.getName()) + ") { ");
         buf.append("$nic; break ");
         buf.append("} ");
         buf.append("}");
 
         nic = addLinks(runAndParseSingle(buf.toString()));
 
         UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(nic.getId());
 
         return Response.created(uriBuilder.build()).entity(nic).build();
     }
 
     @Override
     public void remove(String id) {
         StringBuilder buf = new StringBuilder();
 
         buf.append("$h = get-host " + PowerShellUtils.escape(hostId) + "; ");
 
         buf.append("foreach ($nic in $h.getnetworkadapters()) { ");
         buf.append("if ($nic.id -eq " + PowerShellUtils.escape(id) + ") { ");
         buf.append("$bond = $nic; break ");
         buf.append("} ");
         buf.append("} ");
 
         buf.append("remove-bond");
         buf.append(" -hostobject $h");
         buf.append(" -bondobject $bond");
 
         PowerShellCmd.runCommand(getPool(), buf.toString());
     }
 
     @Override
     public HostNicResource getHostNicSubResource(String id) {
         return new PowerShellHostNicResource(id, getExecutor(), shellPools, getParser(), this);
     }
 }
