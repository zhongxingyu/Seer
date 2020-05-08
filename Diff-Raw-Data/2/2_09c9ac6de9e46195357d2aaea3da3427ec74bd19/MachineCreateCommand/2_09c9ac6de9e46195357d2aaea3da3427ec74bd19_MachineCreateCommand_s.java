 /**
  *
  * SIROCCO
  * Copyright (C) 2011 France Telecom
  * Contact: sirocco@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  * USA
  *
  *  $Id$
  *
  */
 package org.ow2.sirocco.apis.rest.cimi.tools;
 
 import java.util.List;
 
 import org.ow2.sirocco.apis.rest.cimi.sdk.CimiClient;
 import org.ow2.sirocco.apis.rest.cimi.sdk.CimiException;
 import org.ow2.sirocco.apis.rest.cimi.sdk.CreateResult;
 import org.ow2.sirocco.apis.rest.cimi.sdk.Machine;
 import org.ow2.sirocco.apis.rest.cimi.sdk.MachineCreate;
 import org.ow2.sirocco.apis.rest.cimi.sdk.MachineTemplate;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 
 @Parameters(commandDescription = "create machine")
 public class MachineCreateCommand implements Command {
     @Parameter(names = "-template", description = "id of the template", required = false)
     private String templateId;
 
     @Parameter(names = "-config", description = "id of the config", required = false)
     private String configId;
 
     @Parameter(names = "-image", description = "id of the image", required = false)
     private String imageId;
 
     @Parameter(names = "-credential", description = "id of the credential", required = false)
     private String credId;
 
     @Parameter(names = "-userData", description = "user data", required = false)
     private String userData;
 
     @Parameter(names = "-name", description = "name of the template", required = false)
     private String name;
 
     @Parameter(names = "-description", description = "description of the template", required = false)
     private String description;
 
     @Parameter(names = "-properties", variableArity = true, description = "key value pairs", required = false)
     private List<String> properties;
 
     @Override
     public String getName() {
         return "machine-create";
     }
 
     @Override
     public void execute(final CimiClient cimiClient) throws CimiException {
        if (this.templateId == null && (this.configId == null && this.imageId == null)) {
             throw new CimiException("You need to specify either a template id or both a config id and an image id");
         }
         MachineCreate machineCreate = new MachineCreate();
         MachineTemplate machineTemplate;
         if (this.templateId != null) {
             machineCreate.setMachineTemplateRef(this.templateId);
             machineTemplate = machineCreate.getMachineTemplate();
         } else {
             machineTemplate = new MachineTemplate();
             machineTemplate.setMachineConfigRef(this.configId);
             machineTemplate.setMachineImageRef(this.imageId);
             if (this.credId != null) {
                 machineTemplate.setCredentialRef(this.credId);
             }
         }
         machineTemplate.setUserData(this.userData);
         machineCreate.setMachineTemplate(machineTemplate);
         machineCreate.setName(this.name);
         machineCreate.setDescription(this.description);
         if (this.properties != null) {
             for (int i = 0; i < this.properties.size() / 2; i++) {
                 machineCreate.addProperty(this.properties.get(i * 2), this.properties.get(i * 2 + 1));
             }
         }
         CreateResult<Machine> result = Machine.createMachine(cimiClient, machineCreate);
         if (result.getJob() != null) {
             System.out.println("Machine " + result.getJob().getTargetResourceRef() + " being created");
             JobListCommand.printJob(result.getJob());
         } else {
             MachineShowCommand.printMachine(result.getResource());
         }
 
     }
 }
