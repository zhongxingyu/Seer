 /**
  * Licensed to jclouds, Inc. (jclouds) under one or more
  * contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  jclouds licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.jclouds.cloudstack.features;
 
import static org.jclouds.cloudstack.options.ListTemplatesOptions.Builder.zoneId;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Random;
import java.util.Set;

 import org.jclouds.cloudstack.domain.*;
 import org.jclouds.cloudstack.options.CreateTemplateOptions;
 import org.jclouds.cloudstack.options.ListNetworksOptions;
 import org.jclouds.cloudstack.options.ListVolumesOptions;
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.Test;
 
import com.google.common.collect.Iterables;
 
 /**
  * Tests behavior of {@code TemplateClientLiveTest}
  * 
  * @author Adrian Cole
  */
 @Test(groups = "live", singleThreaded = true, testName = "TemplateClientLiveTest")
 public class TemplateClientLiveTest extends BaseCloudStackClientLiveTest {
 
    private VirtualMachine vm;
    private Template template;
 
    public void testListTemplates() throws Exception {
       Set<Template> response = client.getTemplateClient().listTemplates();
       assert null != response;
       long templateCount = response.size();
       assertTrue(templateCount >= 0);
       for (Template template : response) {
          Template newDetails = Iterables.getOnlyElement(client.getTemplateClient().listTemplates(
                zoneId(template.getZoneId()).id(template.getId())));
          assertEquals(template, newDetails);
          assertEquals(template, client.getTemplateClient().getTemplateInZone(template.getId(), template.getZoneId()));
          assert template.getId() > 0 : template;
          assert template.getName() != null : template;
          assert template.getDisplayText() != null : template;
          assert template.getCreated() != null : template;
          assert template.getFormat() != null && template.getFormat() != Template.Format.UNRECOGNIZED : template;
          assert template.getOSType() != null : template;
          assert template.getOSTypeId() > 0 : template;
          assert template.getAccount() != null : template;
          assert template.getZone() != null : template;
          assert template.getZoneId() > 0 : template;
          assert template.getStatus() == null : template;
          assert template.getType() != null && template.getType() != Template.Type.UNRECOGNIZED : template;
          assert template.getHypervisor() != null : template;
          assert template.getDomain() != null : template;
          assert template.getDomainId() > 0 : template;
       }
    }
 
    public void testCreateTemplate() throws Exception {
       Zone zone = Iterables.getFirst(client.getZoneClient().listZones(), null);
       assertNotNull(zone);
      Network network = Iterables.getFirst(client.getNetworkClient().listNetworks(ListNetworksOptions.Builder.zoneId(zone.getId()).isDefault(true)), null);
       assertNotNull(network);
 
       // Create a VM and stop it
       Long templateId = (imageId != null && !"".equals(imageId)) ? new Long(imageId) : null;
       vm = VirtualMachineClientLiveTest.createVirtualMachineInNetwork(network, templateId, client, jobComplete, virtualMachineRunning);
       assert jobComplete.apply(client.getVirtualMachineClient().stopVirtualMachine(vm.getId())) : vm;
 
       // Work out the VM's volume
       Set<Volume> volumes = client.getVolumeClient().listVolumes(ListVolumesOptions.Builder.virtualMachineId(vm.getId()));
       assertEquals(volumes.size(), 1);
       Volume volume = Iterables.getOnlyElement(volumes);
 
       // Create a template
       String tmplName = "jclouds-" + Integer.toHexString(new Random().nextInt());
       CreateTemplateOptions options = CreateTemplateOptions.Builder.volumeId(volume.getId());
      AsyncCreateResponse response = client.getTemplateClient().createTemplate(TemplateMetadata.builder().name(tmplName).osTypeId(vm.getGuestOSId()).displayText("jclouds live testCreateTemplate").build());
       assert jobComplete.apply(response.getJobId()) : vm;
       template = client.getTemplateClient().getTemplateInZone(response.getId(), vm.getZoneId());
 
       // Assertions
       assertNotNull(template);
    }
 
    @AfterGroups(groups = "live")
    protected void tearDown() {
       if (vm != null) {
          assert jobComplete.apply(client.getVirtualMachineClient().stopVirtualMachine(vm.getId())) : vm;
          assert jobComplete.apply(client.getVirtualMachineClient().destroyVirtualMachine(vm.getId())) : vm;
          assert virtualMachineDestroyed.apply(vm);
       }
       if (template != null) {
          client.getTemplateClient().deleteTemplate(template.getId());
       }
       super.tearDown();
    }
 
 }
