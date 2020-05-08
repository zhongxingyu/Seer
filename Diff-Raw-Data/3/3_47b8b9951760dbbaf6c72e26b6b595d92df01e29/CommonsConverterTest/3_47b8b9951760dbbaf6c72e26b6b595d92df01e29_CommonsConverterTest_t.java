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
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  *
  * $Id$
  *
  */
 package org.ow2.sirocco.cimi.server.converter;
 
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Marshaller;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.ow2.sirocco.cimi.domain.CimiAddress;
 import org.ow2.sirocco.cimi.domain.CimiAddressTemplate;
 import org.ow2.sirocco.cimi.domain.CimiCloudEntryPoint;
 import org.ow2.sirocco.cimi.domain.CimiCommon;
 import org.ow2.sirocco.cimi.domain.CimiCredential;
 import org.ow2.sirocco.cimi.domain.CimiCredentialTemplate;
 import org.ow2.sirocco.cimi.domain.CimiDataCommon;
 import org.ow2.sirocco.cimi.domain.CimiDiskConfiguration;
 import org.ow2.sirocco.cimi.domain.CimiEvent;
 import org.ow2.sirocco.cimi.domain.CimiEventLog;
 import org.ow2.sirocco.cimi.domain.CimiEventLogTemplate;
 import org.ow2.sirocco.cimi.domain.CimiForwardingGroup;
 import org.ow2.sirocco.cimi.domain.CimiForwardingGroupNetwork;
 import org.ow2.sirocco.cimi.domain.CimiForwardingGroupTemplate;
 import org.ow2.sirocco.cimi.domain.CimiJob;
 import org.ow2.sirocco.cimi.domain.CimiMachine;
 import org.ow2.sirocco.cimi.domain.CimiMachineConfiguration;
 import org.ow2.sirocco.cimi.domain.CimiMachineDisk;
 import org.ow2.sirocco.cimi.domain.CimiMachineImage;
 import org.ow2.sirocco.cimi.domain.CimiMachineNetworkInterface;
 import org.ow2.sirocco.cimi.domain.CimiMachineNetworkInterfaceAddress;
 import org.ow2.sirocco.cimi.domain.CimiMachineTemplate;
 import org.ow2.sirocco.cimi.domain.CimiMachineTemplateVolume;
 import org.ow2.sirocco.cimi.domain.CimiMachineTemplateVolumeTemplate;
 import org.ow2.sirocco.cimi.domain.CimiMachineVolume;
 import org.ow2.sirocco.cimi.domain.CimiNetwork;
 import org.ow2.sirocco.cimi.domain.CimiNetworkConfiguration;
 import org.ow2.sirocco.cimi.domain.CimiNetworkNetworkPort;
 import org.ow2.sirocco.cimi.domain.CimiNetworkPort;
 import org.ow2.sirocco.cimi.domain.CimiNetworkPortConfiguration;
 import org.ow2.sirocco.cimi.domain.CimiNetworkPortTemplate;
 import org.ow2.sirocco.cimi.domain.CimiNetworkTemplate;
 import org.ow2.sirocco.cimi.domain.CimiOperation;
 import org.ow2.sirocco.cimi.domain.CimiResource;
 import org.ow2.sirocco.cimi.domain.CimiSystem;
 import org.ow2.sirocco.cimi.domain.CimiSystemCredential;
 import org.ow2.sirocco.cimi.domain.CimiSystemForwardingGroup;
 import org.ow2.sirocco.cimi.domain.CimiSystemMachine;
 import org.ow2.sirocco.cimi.domain.CimiSystemNetwork;
 import org.ow2.sirocco.cimi.domain.CimiSystemNetworkPort;
 import org.ow2.sirocco.cimi.domain.CimiSystemSystem;
 import org.ow2.sirocco.cimi.domain.CimiSystemTemplate;
 import org.ow2.sirocco.cimi.domain.CimiSystemVolume;
 import org.ow2.sirocco.cimi.domain.CimiVolume;
 import org.ow2.sirocco.cimi.domain.CimiVolumeConfiguration;
 import org.ow2.sirocco.cimi.domain.CimiVolumeImage;
 import org.ow2.sirocco.cimi.domain.CimiVolumeTemplate;
 import org.ow2.sirocco.cimi.domain.CimiVolumeVolumeImage;
 import org.ow2.sirocco.cimi.domain.CloudEntryPointAggregate;
 import org.ow2.sirocco.cimi.domain.ExchangeType;
 import org.ow2.sirocco.cimi.domain.collection.CimiAddressCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiAddressTemplateCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiCredentialCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiCredentialTemplateCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiEventCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiEventLogCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiEventLogTemplateCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiForwardingGroupCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiForwardingGroupNetworkCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiForwardingGroupTemplateCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiJobCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiMachineCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiMachineConfigurationCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiMachineDiskCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiMachineImageCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiMachineNetworkInterfaceAddressCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiMachineNetworkInterfaceCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiMachineTemplateCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiMachineVolumeCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiNetworkCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiNetworkConfigurationCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiNetworkNetworkPortCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiNetworkPortCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiNetworkPortConfigurationCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiNetworkPortTemplateCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiNetworkTemplateCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiSystemCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiSystemCredentialCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiSystemForwardingGroupCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiSystemMachineCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiSystemNetworkCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiSystemNetworkPortCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiSystemSystemCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiSystemTemplateCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiSystemVolumeCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiVolumeCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiVolumeConfigurationCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiVolumeImageCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiVolumeTemplateCollection;
 import org.ow2.sirocco.cimi.domain.collection.CimiVolumeVolumeImageCollection;
 import org.ow2.sirocco.cimi.server.request.CimiContext;
 import org.ow2.sirocco.cimi.server.request.CimiContextImpl;
 import org.ow2.sirocco.cimi.server.request.CimiExpand;
 import org.ow2.sirocco.cimi.server.request.CimiRequest;
 import org.ow2.sirocco.cimi.server.request.CimiResponse;
 import org.ow2.sirocco.cimi.server.request.CimiSelect;
 import org.ow2.sirocco.cimi.server.request.IdRequest;
 import org.ow2.sirocco.cimi.server.request.RequestParams;
 import org.ow2.sirocco.cloudmanager.model.cimi.Address;
 import org.ow2.sirocco.cloudmanager.model.cimi.AddressTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.Credentials;
 import org.ow2.sirocco.cloudmanager.model.cimi.CredentialsTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.DiskTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.ForwardingGroup;
 import org.ow2.sirocco.cloudmanager.model.cimi.ForwardingGroupNetwork;
 import org.ow2.sirocco.cloudmanager.model.cimi.ForwardingGroupTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.Identifiable;
 import org.ow2.sirocco.cloudmanager.model.cimi.Job;
 import org.ow2.sirocco.cloudmanager.model.cimi.Machine;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineConfiguration;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineDisk;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineImage;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineNetworkInterface;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineNetworkInterfaceAddress;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineVolume;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineVolumeTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.Network;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkConfiguration;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkNetworkPort;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkPort;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkPortConfiguration;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkPortTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.Volume;
 import org.ow2.sirocco.cloudmanager.model.cimi.VolumeConfiguration;
 import org.ow2.sirocco.cloudmanager.model.cimi.VolumeImage;
 import org.ow2.sirocco.cloudmanager.model.cimi.VolumeTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.event.Event;
 import org.ow2.sirocco.cloudmanager.model.cimi.event.EventLog;
 import org.ow2.sirocco.cloudmanager.model.cimi.event.EventLogTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.system.System;
 import org.ow2.sirocco.cloudmanager.model.cimi.system.SystemCredentials;
 import org.ow2.sirocco.cloudmanager.model.cimi.system.SystemForwardingGroup;
 import org.ow2.sirocco.cloudmanager.model.cimi.system.SystemMachine;
 import org.ow2.sirocco.cloudmanager.model.cimi.system.SystemNetwork;
 import org.ow2.sirocco.cloudmanager.model.cimi.system.SystemNetworkPort;
 import org.ow2.sirocco.cloudmanager.model.cimi.system.SystemSystem;
 import org.ow2.sirocco.cloudmanager.model.cimi.system.SystemTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.system.SystemVolume;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Converters tests of common data.
  */
 public class CommonsConverterTest {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(CommonsConverterTest.class);
 
     private CimiRequest request;
 
     private CimiContext context;
 
     @Before
     public void setUp() throws Exception {
 
         this.request = new CimiRequest();
         this.request.setBaseUri("http://www.test.org/");
         RequestParams header = new RequestParams();
         header.setCimiSelect(new CimiSelect());
         header.setCimiExpand(new CimiExpand());
         this.request.setParams(header);
 
         this.context = new CimiContextImpl(this.request, new CimiResponse());
     }
 
     @Test
     public void testCimiDiskConfiguration() throws Exception {
         CimiDiskConfiguration cimi;
         DiskTemplate service;
 
         // Empty Cimi -> Service
         service = (DiskTemplate) this.context.convertToService(new CimiDiskConfiguration());
         Assert.assertNull(service.getCapacity());
         Assert.assertNull(service.getInitialLocation());
 
         // Empty Service -> Cimi
         cimi = (CimiDiskConfiguration) this.context.convertToCimi(new DiskTemplate(), CimiDiskConfiguration.class);
         Assert.assertNull(cimi.getCapacity());
         Assert.assertNull(cimi.getInitialLocation());
 
         // Full Cimi -> Service
         cimi = new CimiDiskConfiguration();
         cimi.setCapacity(5);
         cimi.setFormat("format");
         cimi.setInitialLocation("initialLocation");
 
         service = (DiskTemplate) this.context.convertToService(cimi);
         Assert.assertEquals(5, service.getCapacity().intValue());
         Assert.assertEquals("format", service.getFormat());
         Assert.assertEquals("initialLocation", service.getInitialLocation());
 
         // Full Service -> Cimi
         service = new DiskTemplate();
         service.setCapacity(7);
         service.setFormat("format");
         service.setInitialLocation("initialLocation");
 
         cimi = (CimiDiskConfiguration) this.context.convertToCimi(service, CimiDiskConfiguration.class);
         Assert.assertEquals(7, cimi.getCapacity().intValue());
         Assert.assertEquals("format", cimi.getFormat());
         Assert.assertEquals("initialLocation", cimi.getInitialLocation());
 
     }
 
     @Test
     public void testCimiCommonFill() throws Exception {
         CimiDataCommon cimi;
         MachineImage service;
         CommonConverter converter = new CommonConverter();
 
         // Empty
         cimi = new CimiCommon();
         service = new MachineImage();
 
         converter.fill(cimi, service);
         Assert.assertNull(service.getDescription());
         Assert.assertNull(service.getName());
         Assert.assertNull(service.getProperties());
 
         converter.fill(service, cimi);
         Assert.assertNull(cimi.getDescription());
         Assert.assertNull(cimi.getName());
         Assert.assertNull(cimi.getProperties());
 
         // Full
         cimi = new CimiCommon();
         cimi.setDescription("description");
         cimi.setName("name");
         Map<String, String> props = new HashMap<String, String>();
         props.put("keyOne", "valueOne");
         props.put("keyTwo", "valueTwo");
         props.put("keyThree", "valueThree");
         cimi.setProperties(props);
         service = new MachineImage();
 
         converter.fill(cimi, service);
         Assert.assertEquals("description", service.getDescription());
         Assert.assertEquals("name", service.getName());
         Assert.assertNotNull(service.getProperties());
         Assert.assertEquals(3, service.getProperties().size());
         Assert.assertEquals("valueOne", service.getProperties().get("keyOne"));
         Assert.assertEquals("valueTwo", service.getProperties().get("keyTwo"));
         Assert.assertEquals("valueThree", service.getProperties().get("keyThree"));
 
         cimi = new CimiCommon();
         converter.fill(service, cimi);
         Assert.assertEquals("description", cimi.getDescription());
         Assert.assertEquals("name", cimi.getName());
         Assert.assertNotNull(cimi.getProperties());
         Assert.assertEquals(3, cimi.getProperties().size());
         Assert.assertEquals("valueOne", cimi.getProperties().get("keyOne"));
         Assert.assertEquals("valueTwo", cimi.getProperties().get("keyTwo"));
         Assert.assertEquals("valueThree", cimi.getProperties().get("keyThree"));
     }
 
     @Test
     public void testObjectCommon() throws Exception {
         CimiMachineImage cimi;
         MachineImage service;
 
         // Empty
         service = (MachineImage) this.context.convertToService(new CimiMachineImage());
         Assert.assertNull(service.getId());
         Assert.assertNull(service.getCreated());
         Assert.assertNull(service.getUpdated());
 
         cimi = (CimiMachineImage) this.context.convertToCimi(new MachineImage(), CimiMachineImage.class);
         Assert.assertNull(cimi.getId());
         Assert.assertNull(cimi.getHref());
         Assert.assertNull(cimi.getCreated());
         Assert.assertNull(cimi.getUpdated());
 
         // Full
         cimi = new CimiMachineImage();
         cimi.setId(this.request.getBaseUri() + ExchangeType.MachineImage.getPathname() + "/13");
         cimi.setHref(this.request.getBaseUri() + ExchangeType.MachineImage.getPathname() + "/13");
         cimi.setCreated(new Date());
         cimi.setUpdated(new Date());
         cimi.setOperations(new CimiOperation[] {new CimiOperation("rel", "href")});
         service = new MachineImage();
 
         service = (MachineImage) this.context.convertToService(cimi);
         Assert.assertEquals(13, service.getId().intValue());
         Assert.assertNull(service.getCreated());
         Assert.assertNull(service.getUpdated());
 
         cimi = new CimiMachineImage();
         service = new MachineImage();
         service.setId(29);
         Date created = new Date();
         service.setCreated(created);
         Date updated = new Date();
         service.setUpdated(updated);
 
         cimi = (CimiMachineImage) this.context.convertToCimi(service, CimiMachineImage.class);
         Assert.assertEquals(this.request.getBaseUri() + ExchangeType.MachineImage.getPathname() + "/29", cimi.getId());
         Assert.assertNull(cimi.getHref());
         Assert.assertEquals(created, cimi.getCreated());
         Assert.assertEquals(updated, cimi.getUpdated());
 
         // Full in collection
         CimiMachineImageCollection cimiCollection;
         List<MachineImage> serviceCollection = new ArrayList<MachineImage>();
         serviceCollection.add(service);
 
         cimiCollection = (CimiMachineImageCollection) this.context.convertToCimi(serviceCollection,
             CimiMachineImageCollection.class);
         Assert.assertEquals(this.request.getBaseUri() + ExchangeType.MachineImageCollection.getPathname(),
             cimiCollection.getId());
         Assert.assertNull(cimiCollection.getHref());
 
         cimi = cimiCollection.getCollection().get(0);
         Assert.assertNotNull(cimi.getId());
         Assert.assertNull(cimi.getHref());
         Assert.assertEquals(this.request.getBaseUri() + ExchangeType.MachineImage.getPathname() + "/29", cimi.getId());
         Assert.assertNotNull(cimi.getCreated());
         Assert.assertNotNull(cimi.getUpdated());
     }
 
     @Test
     public void testId() throws Exception {
         CimiResource cimi = null;
         Object service = null;
         Class<? extends CimiResource> cimiClass = null;
 
         for (ExchangeType type : ExchangeType.values()) {
 
             // Removes ids in request
             this.request.setIds(new IdRequest());
 
             switch (type) {
             case Action:
                 service = null;
                 break;
             case Address:
                 service = new Address();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiAddress.class;
                 break;
             case AddressCollection:
                 service = new ArrayList<Address>();
                 cimiClass = CimiAddressCollection.class;
                 break;
             case AddressCreate:
                 service = null;
                 break;
             case AddressTemplate:
                 service = new AddressTemplate();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiAddressTemplate.class;
                 break;
             case AddressTemplateCollection:
                 service = new ArrayList<AddressTemplate>();
                 cimiClass = CimiAddressTemplateCollection.class;
                 break;
             case CloudEntryPoint:
                 service = new CloudEntryPointAggregate();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiCloudEntryPoint.class;
                 break;
             case Credential:
                 service = new Credentials();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiCredential.class;
                 break;
             case CredentialCollection:
                 service = new ArrayList<Credentials>();
                 cimiClass = CimiCredentialCollection.class;
                 break;
             case CredentialCreate:
                 service = null;
                 break;
             case CredentialTemplate:
                 service = new CredentialsTemplate();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiCredentialTemplate.class;
                 break;
             case CredentialTemplateCollection:
                 service = new ArrayList<CredentialsTemplate>();
                 cimiClass = CimiCredentialTemplateCollection.class;
                 break;
             case Disk:
                 service = new MachineDisk();
                 ((MachineDisk) service).setId(111);
                 cimiClass = CimiMachineDisk.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case DiskCollection:
                 service = new ArrayList<MachineDisk>();
                 cimiClass = CimiMachineDiskCollection.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case Event:
                 service = new Event();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiEvent.class;
                 break;
             case EventCollection:
                 service = new ArrayList<Event>();
                 cimiClass = CimiEventCollection.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case EventLog:
                 service = new EventLog();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiEventLog.class;
                 break;
             case EventLogCollection:
                 service = new ArrayList<EventLog>();
                 cimiClass = CimiEventLogCollection.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case EventLogCreate:
                 service = null;
                 break;
             case EventLogTemplate:
                 service = new EventLogTemplate();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiEventLogTemplate.class;
                 break;
             case EventLogTemplateCollection:
                 service = new ArrayList<EventLogTemplate>();
                 cimiClass = CimiEventLogTemplateCollection.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case ForwardingGroup:
                 service = new ForwardingGroup();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiForwardingGroup.class;
                 break;
             case ForwardingGroupCollection:
                 service = new ArrayList<ForwardingGroup>();
                 cimiClass = CimiForwardingGroupCollection.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case ForwardingGroupCreate:
                 service = null;
                 break;
             case ForwardingGroupNetwork:
                 service = new ForwardingGroupNetwork();
                 ((ForwardingGroupNetwork) service).setId(111);
                 cimiClass = CimiForwardingGroupNetwork.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case ForwardingGroupNetworkCollection:
                 service = new ArrayList<ForwardingGroupNetwork>();
                 cimiClass = CimiForwardingGroupNetworkCollection.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case ForwardingGroupTemplate:
                 service = new ForwardingGroupTemplate();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiForwardingGroupTemplate.class;
                 break;
             case ForwardingGroupTemplateCollection:
                 service = new ArrayList<ForwardingGroupTemplate>();
                 cimiClass = CimiForwardingGroupTemplateCollection.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case Job:
                 service = new Job();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiJob.class;
                 break;
             case JobCollection:
                 service = new ArrayList<Job>();
                 cimiClass = CimiJobCollection.class;
                 break;
             case Machine:
                 service = new Machine();
                 ((Identifiable) service).setId(11);
                 ((Machine) service).setState(Machine.State.STARTED);
                 cimiClass = CimiMachine.class;
                 break;
             case MachineCollection:
                 service = new ArrayList<Machine>();
                 cimiClass = CimiMachineCollection.class;
                 break;
             case MachineConfiguration:
                 service = new MachineConfiguration();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiMachineConfiguration.class;
                 break;
             case MachineConfigurationCollection:
                 service = new ArrayList<MachineConfiguration>();
                 cimiClass = CimiMachineConfigurationCollection.class;
                 break;
             case MachineCreate:
                 service = null;
                 break;
             case MachineImage:
                 service = new MachineImage();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiMachineImage.class;
                 break;
             case MachineImageCollection:
                 service = new ArrayList<MachineImage>();
                 cimiClass = CimiMachineImageCollection.class;
                 break;
             case MachineNetworkInterface:
                 service = new MachineNetworkInterface();
                 ((MachineNetworkInterface) service).setId(111);
                 cimiClass = CimiMachineNetworkInterface.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case MachineNetworkInterfaceCollection:
                 service = new ArrayList<MachineNetworkInterface>();
                 cimiClass = CimiMachineNetworkInterfaceCollection.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case MachineNetworkInterfaceAddress:
                 service = new MachineNetworkInterfaceAddress();
                 ((MachineNetworkInterfaceAddress) service).setId(111);
                 cimiClass = CimiMachineNetworkInterfaceAddress.class;
                 // Add id grandparent and parent in request
                 this.request.setIds(new IdRequest(null, "999", "7777"));
                 break;
             case MachineNetworkInterfaceAddressCollection:
                 service = new ArrayList<MachineNetworkInterfaceAddress>();
                 cimiClass = CimiMachineNetworkInterfaceAddressCollection.class;
                 // Add id grandparent and parent in request
                 this.request.setIds(new IdRequest(null, "999", "7777"));
                 break;
             case MachineTemplate:
                 service = new MachineTemplate();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiMachineTemplate.class;
                 break;
             case MachineTemplateCollection:
                 service = new ArrayList<MachineTemplate>();
                 cimiClass = CimiMachineTemplateCollection.class;
                 break;
             case MachineTemplateVolume:
                 service = new MachineVolume();
                 ((MachineVolume) service).setVolume(new Volume());
                 ((MachineVolume) service).getVolume().setId(11);
                 cimiClass = CimiMachineTemplateVolume.class;
                 break;
             case MachineTemplateVolumeTemplate:
                 service = new MachineVolumeTemplate();
                 ((MachineVolumeTemplate) service).setVolumeTemplate(new VolumeTemplate());
                 ((MachineVolumeTemplate) service).getVolumeTemplate().setId(11);
                 cimiClass = CimiMachineTemplateVolumeTemplate.class;
                 break;
             case MachineVolume:
                 service = new MachineVolume();
                 ((MachineVolume) service).setId(111);
                 cimiClass = CimiMachineVolume.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case MachineVolumeCollection:
                 service = new ArrayList<MachineVolume>();
                 cimiClass = CimiMachineVolumeCollection.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case Network:
                 service = new Network();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiNetwork.class;
                 break;
             case NetworkCollection:
                 service = new ArrayList<Network>();
                 cimiClass = CimiNetworkCollection.class;
                 break;
             case NetworkConfiguration:
                 service = new NetworkConfiguration();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiNetworkConfiguration.class;
                 break;
             case NetworkConfigurationCollection:
                 service = new ArrayList<NetworkConfiguration>();
                 cimiClass = CimiNetworkConfigurationCollection.class;
                 break;
             case NetworkCreate:
                 service = null;
                 break;
             case NetworkNetworkPort:
                 service = new NetworkNetworkPort();
                 ((Identifiable) service).setId(111);
                 cimiClass = CimiNetworkNetworkPort.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case NetworkNetworkPortCollection:
                 service = new ArrayList<NetworkPort>();
                 cimiClass = CimiNetworkNetworkPortCollection.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case NetworkTemplate:
                 service = new NetworkTemplate();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiNetworkTemplate.class;
                 break;
             case NetworkTemplateCollection:
                 service = new ArrayList<NetworkTemplate>();
                 cimiClass = CimiNetworkTemplateCollection.class;
                 break;
             case NetworkPort:
                 service = new NetworkPort();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiNetworkPort.class;
                 break;
             case NetworkPortCollection:
                 service = new ArrayList<NetworkPort>();
                 cimiClass = CimiNetworkPortCollection.class;
                 break;
             case NetworkPortConfiguration:
                 service = new NetworkPortConfiguration();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiNetworkPortConfiguration.class;
                 break;
             case NetworkPortConfigurationCollection:
                 service = new ArrayList<NetworkPortConfiguration>();
                 cimiClass = CimiNetworkPortConfigurationCollection.class;
                 break;
             case NetworkPortCreate:
                 service = null;
                 break;
             case NetworkPortTemplate:
                 service = new NetworkPortTemplate();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiNetworkPortTemplate.class;
                 break;
             case NetworkPortTemplateCollection:
                 service = new ArrayList<NetworkPortTemplate>();
                 cimiClass = CimiNetworkPortTemplateCollection.class;
                 break;
             case Volume:
                 service = new Volume();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiVolume.class;
                 break;
             case VolumeCollection:
                 service = new ArrayList<Volume>();
                 cimiClass = CimiVolumeCollection.class;
                 break;
             case VolumeConfiguration:
                 service = new VolumeConfiguration();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiVolumeConfiguration.class;
                 break;
             case VolumeConfigurationCollection:
                 service = new ArrayList<VolumeConfiguration>();
                 cimiClass = CimiVolumeConfigurationCollection.class;
                 break;
             case VolumeCreate:
                 service = null;
                 break;
             case VolumeImage:
                 service = new VolumeImage();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiVolumeImage.class;
                 break;
             case VolumeImageCollection:
                 service = new ArrayList<VolumeImage>();
                 cimiClass = CimiVolumeImageCollection.class;
                 break;
             case VolumeTemplate:
                 service = new VolumeTemplate();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiVolumeTemplate.class;
                 break;
             case VolumeTemplateCollection:
                 service = new ArrayList<VolumeTemplate>();
                 cimiClass = CimiVolumeTemplateCollection.class;
                 break;
             case VolumeVolumeImage:
                 service = new VolumeImage();
                 ((Identifiable) service).setId(111);
                 cimiClass = CimiVolumeVolumeImage.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case VolumeVolumeImageCollection:
                 service = new ArrayList<VolumeImage>();
                 cimiClass = CimiVolumeVolumeImageCollection.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case System:
                 service = new System();
                 ((Identifiable) service).setId(11);
                 ((System) service).setState(System.State.STARTED);
                 cimiClass = CimiSystem.class;
                 break;
             case SystemCollection:
                 service = new ArrayList<System>();
                 cimiClass = CimiSystemCollection.class;
                 break;
             case SystemCreate:
                 service = null;
                 break;
             case SystemCredential:
                 service = new SystemCredentials();
                 ((Identifiable) service).setId(111);
                 cimiClass = CimiSystemCredential.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case SystemCredentialCollection:
                 service = new ArrayList<SystemCredentials>();
                 cimiClass = CimiSystemCredentialCollection.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case SystemMachine:
                 service = new SystemMachine();
                 ((Identifiable) service).setId(111);
                 cimiClass = CimiSystemMachine.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case SystemMachineCollection:
                 service = new ArrayList<SystemMachine>();
                 cimiClass = CimiSystemMachineCollection.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case SystemSystem:
                 service = new SystemSystem();
                 ((Identifiable) service).setId(111);
                 cimiClass = CimiSystemSystem.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case SystemSystemCollection:
                 service = new ArrayList<SystemSystem>();
                 cimiClass = CimiSystemSystemCollection.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case SystemTemplate:
                 service = new SystemTemplate();
                 ((Identifiable) service).setId(11);
                 cimiClass = CimiSystemTemplate.class;
                 break;
             case SystemTemplateCollection:
                 service = new ArrayList<SystemTemplate>();
                 cimiClass = CimiSystemTemplateCollection.class;
                 break;
             case SystemVolume:
                 service = new SystemVolume();
                 ((Identifiable) service).setId(111);
                 cimiClass = CimiSystemVolume.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case SystemVolumeCollection:
                 service = new ArrayList<SystemVolume>();
                 cimiClass = CimiSystemVolumeCollection.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case SystemAddress:
                 // FIXME SystemAddress
                 service = null;
                 // service = new SystemAddress();
                 // ((Identifiable) service).setId(111);
                 // cimiClass = CimiSystemAddress.class;
                 // // Add idParent in request
                 // this.request.setIds(new IdRequest(null, "999"));
                 break;
             case SystemAddressCollection:
                 // FIXME SystemAddress
                 service = null;
                 // service = new ArrayList<SystemAddress>();
                 // cimiClass = CimiSystemAddressCollection.class;
                 // // Add idParent in request
                 // this.request.setIds(new IdRequest(null, "999"));
                 break;
             case SystemForwardingGroup:
                 service = new SystemForwardingGroup();
                 ((Identifiable) service).setId(111);
                 cimiClass = CimiSystemForwardingGroup.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case SystemForwardingGroupCollection:
                 service = new ArrayList<SystemForwardingGroup>();
                 cimiClass = CimiSystemForwardingGroupCollection.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case SystemNetwork:
                 service = new SystemNetwork();
                 ((Identifiable) service).setId(111);
                 cimiClass = CimiSystemNetwork.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case SystemNetworkCollection:
                 service = new ArrayList<SystemNetwork>();
                 cimiClass = CimiSystemNetworkCollection.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case SystemNetworkPort:
                 service = new SystemNetworkPort();
                 ((Identifiable) service).setId(111);
                 cimiClass = CimiSystemNetworkPort.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case SystemNetworkPortCollection:
                 service = new ArrayList<SystemNetworkPort>();
                 cimiClass = CimiSystemNetworkPortCollection.class;
                 // Add idParent in request
                 this.request.setIds(new IdRequest(null, "999"));
                 break;
             case ResourceMetadata:
             case ResourceMetadataCollection:
                 // TODO
                 continue;
             default:
                 Assert.fail(type.name());
                 break;
             }
             if (null != service) {
                 cimi = (CimiResource) this.context.convertToCimi(service, cimiClass);
                 switch (type.getPathType().getParentDepth()) {
                 case 0:
                     if (true == type.hasIdInReference()) {
                         Assert.assertEquals("in " + type, type.makeHref(this.request.getBaseUri(), "11"), cimi.getId());
                     } else {
                         Assert.assertEquals("in " + type, type.makeHref(this.request.getBaseUri()), cimi.getId());
                     }
                     break;
                 case 1:
                     if (true == type.hasIdInReference()) {
                         Assert.assertEquals("in " + type, type.makeHref(this.request.getBaseUri(), "999", "111"), cimi.getId());
                     } else {
                         Assert.assertEquals("in " + type, type.makeHref(this.request.getBaseUri(), "999"), cimi.getId());
                     }
                     break;
 
                 case 2:
                     if (true == type.hasIdInReference()) {
                         Assert.assertEquals("in " + type, type.makeHref(this.request.getBaseUri(), "7777", "999", "111"),
                             cimi.getId());
                     } else {
                         Assert
                             .assertEquals("in " + type, type.makeHref(this.request.getBaseUri(), "7777", "999"), cimi.getId());
                     }
                     break;
                 default:
                     Assert.fail("Depth not provided");
                     break;
                 }
 
                 Assert.assertEquals("in " + type, type.getResourceURI(), cimi.getResourceURI());
             }
         }
     }
 
     @Test
     public void testIdParentHierarchy() throws Exception {
         CimiMachine cimi;
 
         // Prepare serialized trace
         Writer strWriter;
         ObjectMapper mapper = new ObjectMapper();
         JAXBContext context = JAXBContext.newInstance(CimiMachine.class);
         Marshaller m = context.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 
         // Force ALL expand
         this.context.setConvertedExpand(true);
 
         // Builds resources for a machine
         Machine sMachine = new Machine();
         sMachine.setId(9999);
         sMachine.setState(Machine.State.STARTED);
         sMachine.setDisks(new ArrayList<MachineDisk>());
         sMachine.getDisks().add(new MachineDisk());
         sMachine.getDisks().get(0).setId(111);
         sMachine.getDisks().add(new MachineDisk());
         sMachine.getDisks().get(1).setId(222);
         sMachine.getDisks().add(new MachineDisk());
         sMachine.getDisks().get(2).setId(333);
 
         sMachine.setNetworkInterfaces(new ArrayList<MachineNetworkInterface>());
         sMachine.getNetworkInterfaces().add(new MachineNetworkInterface());
         sMachine.getNetworkInterfaces().get(0).setId(7111);
 
         sMachine.getNetworkInterfaces().get(0).setAddresses(new ArrayList<MachineNetworkInterfaceAddress>());
         sMachine.getNetworkInterfaces().get(0).getAddresses().add(new MachineNetworkInterfaceAddress());
         sMachine.getNetworkInterfaces().get(0).getAddresses().get(0).setId(711191);
         sMachine.getNetworkInterfaces().get(0).getAddresses().get(0).setAddress(new Address());
         sMachine.getNetworkInterfaces().get(0).getAddresses().get(0).getAddress().setId(7111911);
         sMachine.getNetworkInterfaces().get(0).getAddresses().add(new MachineNetworkInterfaceAddress());
         sMachine.getNetworkInterfaces().get(0).getAddresses().get(1).setId(711192);
         sMachine.getNetworkInterfaces().get(0).getAddresses().get(1).setAddress(new Address());
         sMachine.getNetworkInterfaces().get(0).getAddresses().get(1).getAddress().setId(7111921);
 
         // Convert
         cimi = (CimiMachine) this.context.convertToCimi(sMachine, CimiMachine.class);
 
         // Trace
         strWriter = new StringWriter();
         mapper.writeValue(strWriter, cimi);
         CommonsConverterTest.LOGGER.debug("JSON:\n\t{}", strWriter);
         strWriter = new StringWriter();
         m.marshal(cimi, strWriter);
         CommonsConverterTest.LOGGER.debug("XML:\n\t{}", strWriter);
 
         // Machine
         Assert.assertEquals("in " + cimi.getExchangeType(), ExchangeType.Machine.makeHref(this.request.getBaseUri(), "9999"),
             cimi.getId());
         // MachineDisk
         Assert.assertEquals("in " + ExchangeType.Disk, ExchangeType.Disk.makeHref(this.request.getBaseUri(), "9999", "111"),
             cimi.getDisks().getCollection().get(0).getId());
         Assert.assertEquals("in " + ExchangeType.Disk, ExchangeType.Disk.makeHref(this.request.getBaseUri(), "9999", "222"),
             cimi.getDisks().getCollection().get(1).getId());
         Assert.assertEquals("in " + ExchangeType.Disk, ExchangeType.Disk.makeHref(this.request.getBaseUri(), "9999", "333"),
             cimi.getDisks().getCollection().get(2).getId());
         // MachineNetworkInterface
         Assert.assertEquals("in " + ExchangeType.MachineNetworkInterface,
             ExchangeType.MachineNetworkInterface.makeHref(this.request.getBaseUri(), "9999", "7111"), cimi
                 .getNetworkInterfaces().getCollection().get(0).getId());
         // MachineNetworkInterfaceAddress and inner Address
         Assert.assertEquals("in " + ExchangeType.MachineNetworkInterfaceAddress,
             ExchangeType.MachineNetworkInterfaceAddress.makeHref(this.request.getBaseUri(), "9999", "7111", "711191"), cimi
                 .getNetworkInterfaces().getCollection().get(0).getAddresses().getCollection().get(0).getId());
         Assert.assertEquals("in " + ExchangeType.Address, ExchangeType.Address.makeHref(this.request.getBaseUri(), "7111911"),
             cimi.getNetworkInterfaces().getCollection().get(0).getAddresses().getCollection().get(0).getAddress().getId());
         Assert.assertEquals("in " + ExchangeType.MachineNetworkInterfaceAddress,
             ExchangeType.MachineNetworkInterfaceAddress.makeHref(this.request.getBaseUri(), "9999", "7111", "711192"), cimi
                 .getNetworkInterfaces().getCollection().get(0).getAddresses().getCollection().get(1).getId());
         Assert.assertEquals("in " + ExchangeType.Address, ExchangeType.Address.makeHref(this.request.getBaseUri(), "7111921"),
             cimi.getNetworkInterfaces().getCollection().get(0).getAddresses().getCollection().get(1).getAddress().getId());
     }
 }
