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
 
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.model.Attachments;
 import com.redhat.rhevm.api.model.DataCenter;
 import com.redhat.rhevm.api.model.Link;
 import com.redhat.rhevm.api.resource.DataCenterResource;
 import com.redhat.rhevm.api.resource.IsosResource;
 import com.redhat.rhevm.api.common.util.LinkHelper;
 import com.redhat.rhevm.api.powershell.model.PowerShellDataCenter;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellParser;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
 
 
 public class PowerShellDataCenterResource extends AbstractPowerShellActionableResource<DataCenter> implements DataCenterResource {
 
     public PowerShellDataCenterResource(String id,
                                         Executor executor,
                                         PowerShellPoolMap shellPools,
                                         PowerShellParser parser) {
         super(id, executor, shellPools, parser);
     }
 
     public static List<DataCenter> runAndParse(PowerShellCmd shell, PowerShellParser parser, String command) {
         return PowerShellDataCenter.parse(parser, PowerShellCmd.runCommand(shell, command));
     }
 
     public static DataCenter runAndParseSingle(PowerShellCmd shell, PowerShellParser parser, String command) {
         List<DataCenter> dataCenters = runAndParse(shell, parser, command);
 
         return !dataCenters.isEmpty() ? dataCenters.get(0) : null;
     }
 
     public List<DataCenter> runAndParse(String command) {
         return runAndParse(getShell(), getParser(), command);
     }
 
     public DataCenter runAndParseSingle(String command) {
         return runAndParseSingle(getShell(), getParser(), command);
     }
 
     public static DataCenter addLinks(PowerShellCmd shell, PowerShellParser parser, DataCenter dataCenter) {
         dataCenter = LinkHelper.addLinks(dataCenter);
 
         Attachments attachments = PowerShellAttachmentsResource.getAttachmentsForDataCenter(shell,
                                                                                             parser,
                                                                                             dataCenter.getId());
         dataCenter.setAttachments(attachments);
 
         Link link = new Link();
         link.setRel("isos");
         link.setHref(LinkHelper.getUriBuilder(dataCenter).path("isos").build().toString());
         dataCenter.getLinks().clear();
         dataCenter.getLinks().add(link);
 
         return dataCenter;
     }
 
     public DataCenter addLinks(DataCenter dataCenter) {
         return addLinks(getShell(), getParser(), dataCenter);
     }
 
     @Override
     public DataCenter get(UriInfo uriInfo) {
         return addLinks(runAndParseSingle("get-datacenter " + PowerShellUtils.escape(getId())));
     }
 
     @Override
     public DataCenter update(UriInfo uriInfo, DataCenter dataCenter) {
         validateUpdate(dataCenter);
 
         StringBuilder buf = new StringBuilder();
 
         buf.append("$h = get-datacenter " + PowerShellUtils.escape(getId()) + ";");
 
         if (dataCenter.getName() != null) {
             buf.append("$h.name = " + PowerShellUtils.escape(dataCenter.getName()) + ";");
         }
         if (dataCenter.getDescription() != null) {
             buf.append("$h.description = " + PowerShellUtils.escape(dataCenter.getDescription()) + ";");
         }
 
        buf.append("update-datacenter -datacenterobject $h");
 
         return addLinks(runAndParseSingle(buf.toString()));
     }
 
     public IsosResource getIsosResource() {
         return new PowerShellIsosResource(getId(), shellPools, getParser());
     }
 }
