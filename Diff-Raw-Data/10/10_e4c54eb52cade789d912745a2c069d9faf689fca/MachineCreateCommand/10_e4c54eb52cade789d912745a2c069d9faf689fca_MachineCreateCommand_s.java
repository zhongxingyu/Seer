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
 package org.ow2.sirocco.cimi.tools;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.ow2.sirocco.cimi.sdk.CimiClient;
 import org.ow2.sirocco.cimi.sdk.CimiClientException;
 import org.ow2.sirocco.cimi.sdk.CreateResult;
 import org.ow2.sirocco.cimi.sdk.Credential;
 import org.ow2.sirocco.cimi.sdk.Machine;
 import org.ow2.sirocco.cimi.sdk.MachineConfiguration;
 import org.ow2.sirocco.cimi.sdk.MachineCreate;
 import org.ow2.sirocco.cimi.sdk.MachineImage;
 import org.ow2.sirocco.cimi.sdk.MachineTemplate;
 import org.ow2.sirocco.cimi.sdk.MachineTemplate.NetworkInterface;
 import org.ow2.sirocco.cimi.sdk.QueryParams;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.ParameterException;
 import com.beust.jcommander.Parameters;
 
 @Parameters(commandDescription = "create machine")
 public class MachineCreateCommand implements Command {
     @Parameter(names = "-template", description = "id of the template", required = false)
     private String templateId;
 
     @Parameter(names = "-config", description = "id of the config", required = false)
     private String configId;
 
     @Parameter(names = "-image", description = "id of the image", required = false)
     private String imageId;
 
     @Parameter(names = "-providerAccountId", description = "id of the provider account", required = false)
     private String providerAccountId;
 
     @Parameter(names = "-location", description = "location", required = false)
     private String location;
 
     @Parameter(names = "-credential", description = "id of the credential", required = false)
     private String credId;
 
     @Parameter(names = "-userData", description = "user data", required = false)
     private String userData;
 
     @Parameter(names = "-nic", description = "attach nic to network id", required = false)
     private List<String> networkIds;
 
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
     public void execute(final CimiClient cimiClient) throws CimiClientException {
         if (this.templateId == null && (this.configId == null || this.imageId == null)) {
             throw new ParameterException("You need to specify either a template id or both a config id and an image id");
         }
         MachineCreate machineCreate = new MachineCreate();
         MachineTemplate machineTemplate;
         if (this.templateId != null) {
             if (!CommandHelper.isResourceIdentifier(this.templateId)) {
                 List<MachineTemplate> templates = MachineTemplate.getMachineTemplates(cimiClient,
                     QueryParams.builder().filter("name='" + this.templateId + "'").select("id").build());
                 if (templates.isEmpty()) {
                     System.err.println("No machine template with name " + this.templateId);
                     System.exit(-1);
                 }
                 this.templateId = templates.get(0).getId();
             }
             machineCreate.setMachineTemplateRef(this.templateId);
             machineTemplate = machineCreate.getMachineTemplate();
         } else {
             machineTemplate = new MachineTemplate();
             if (!CommandHelper.isResourceIdentifier(this.configId)) {
                 List<MachineConfiguration> configs = MachineConfiguration.getMachineConfigurations(cimiClient, QueryParams
                     .builder().filter("name='" + this.configId + "'").select("id").build());
                 if (configs.isEmpty()) {
                     System.err.println("No machine config with name " + this.configId);
                     System.exit(-1);
                 }
                 this.configId = configs.get(0).getId();
             }
             machineTemplate.setMachineConfigRef(this.configId);
             if (!CommandHelper.isResourceIdentifier(this.imageId)) {
                 List<MachineImage> images = MachineImage.getMachineImages(cimiClient,
                     QueryParams.builder().filter("name='" + this.imageId + "'").select("id").build());
                 if (images.isEmpty()) {
                     System.err.println("No machine image with name " + this.imageId);
                     System.exit(-1);
                 }
                 this.imageId = images.get(0).getId();
             }
             machineTemplate.setMachineImageRef(this.imageId);
             if (this.credId != null) {
                 if (!CommandHelper.isResourceIdentifier(this.credId)) {
                     List<Credential> creds = Credential.getCredentials(cimiClient,
                         QueryParams.builder().filter("name='" + this.credId + "'").select("id").build());
                     if (creds.isEmpty()) {
                         System.err.println("No credential with name " + this.credId);
                         System.exit(-1);
                     }
                     this.credId = creds.get(0).getId();
                 }
                 machineTemplate.setCredentialRef(this.credId);
             }
             List<NetworkInterface> nics = new ArrayList<>();
            for (String networkId : this.networkIds) {
                NetworkInterface nic = new NetworkInterface();
                nic.setNetworkRef(networkId);
                nics.add(nic);
             }
             machineTemplate.setNetworkInterface(nics);
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
         machineCreate.setProviderAccountId(this.providerAccountId);
         machineCreate.setLocation(this.location);
         CreateResult<Machine> result = Machine.createMachine(cimiClient, machineCreate);
         if (result.getJob() != null) {
             System.out.println("Job:");
             JobShowCommand.printJob(result.getJob(), new ResourceSelectExpandParams());
         }
         System.out.println("Machine:");
         MachineShowCommand.printMachine(result.getResource(), new ResourceSelectExpandParams());
     }
 }
