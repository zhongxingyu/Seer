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
 package org.ow2.sirocco.apis.rest.cimi.manager;
 
import java.util.ArrayList;
import java.util.List;
 
 import org.easymock.EasyMock;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiMachineConfiguration;
 import org.ow2.sirocco.apis.rest.cimi.domain.ExchangeType;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiContext;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiContextImpl;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiRequest;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiResponse;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiSelect;
 import org.ow2.sirocco.apis.rest.cimi.request.IdRequest;
 import org.ow2.sirocco.apis.rest.cimi.request.RequestParams;
 import org.ow2.sirocco.apis.rest.cimi.utils.Constants;
 import org.ow2.sirocco.apis.rest.cimi.utils.ConstantsPath;
 import org.ow2.sirocco.cloudmanager.core.api.IMachineManager;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineConfiguration;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  * Basic tests "end to end" for managers MachineConfiguration.
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"/context/managerContext.xml"})
 public class CimiManagersMachineConfigurationTest {
 
     @Autowired
     @Qualifier("IMachineManager")
     private IMachineManager service;
 
     @Autowired
     @Qualifier("CimiManagerCreateMachineConfiguration")
     private CimiManager managerCreate;
 
     @Autowired
     @Qualifier("CimiManagerDeleteMachineConfiguration")
     private CimiManager managerDelete;
 
     @Autowired
     @Qualifier("CimiManagerReadMachineConfiguration")
     private CimiManager managerRead;
 
     @Autowired
     @Qualifier("CimiManagerUpdateMachineConfiguration")
     private CimiManager managerUpdate;
 
     private CimiRequest request;
 
     private CimiResponse response;
 
     private CimiContext context;
 
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception {
 
         this.request = new CimiRequest();
         this.request.setBaseUri("/");
         RequestParams header = new RequestParams();
         header.setCimiSelect(new CimiSelect());
         this.request.setParams(header);
 
         this.response = new CimiResponse();
         this.context = new CimiContextImpl(this.request, this.response);
     }
 
     /**
      * @throws java.lang.Exception
      */
     @After
     public void tearDown() throws Exception {
         EasyMock.reset(this.service);
     }
 
     @Test
     public void testCreate() throws Exception {
         MachineConfiguration create = new MachineConfiguration();
         create.setId(456);
 
         EasyMock.expect(this.service.createMachineConfiguration(EasyMock.anyObject(MachineConfiguration.class))).andReturn(
             create);
         EasyMock.replay(this.service);
 
         CimiMachineConfiguration cimi = new CimiMachineConfiguration();
         cimi.setCpu(11);
         cimi.setMemory(22);
 
         this.request.setCimiData(cimi);
         this.managerCreate.execute(this.context);
 
         Assert.assertEquals(201, this.response.getStatus());
         Assert.assertNotNull(this.response.getHeaders());
         Assert.assertEquals(ConstantsPath.MACHINE_CONFIGURATION_PATH + "/456",
             this.response.getHeaders().get(Constants.HEADER_LOCATION));
         Assert.assertEquals(ConstantsPath.MACHINE_CONFIGURATION_PATH + "/456",
             ((CimiMachineConfiguration) this.response.getCimiData()).getId());
         EasyMock.verify(this.service);
     }
 
     @Test
     public void testCreateWithRef() throws Exception {
 
         MachineConfiguration ref = new MachineConfiguration();
         ref.setId(13);
         ref.setName("nameValue");
 
         MachineConfiguration create = new MachineConfiguration();
         create.setId(456);
 
         EasyMock.expect(this.service.getMachineConfigurationById("13")).andReturn(ref);
         EasyMock.expect(this.service.createMachineConfiguration(EasyMock.anyObject(MachineConfiguration.class))).andReturn(
             create);
         EasyMock.replay(this.service);
 
         CimiMachineConfiguration cimi = new CimiMachineConfiguration(this.request.getBaseUri()
             + ExchangeType.MachineConfiguration.getPathType().getPathname() + "/13");
         this.request.setCimiData(cimi);
         this.managerCreate.execute(this.context);
 
         Assert.assertEquals(201, this.response.getStatus());
         Assert.assertNotNull(this.response.getHeaders());
         Assert.assertEquals(ConstantsPath.MACHINE_CONFIGURATION_PATH + "/456",
             this.response.getHeaders().get(Constants.HEADER_LOCATION));
         Assert.assertEquals(ConstantsPath.MACHINE_CONFIGURATION_PATH + "/456",
             ((CimiMachineConfiguration) this.response.getCimiData()).getId());
         EasyMock.verify(this.service);
     }
 
     @Test
     public void testRead() throws Exception {
 
         MachineConfiguration machine = new MachineConfiguration();
         machine.setId(1);
         EasyMock.expect(this.service.getMachineConfigurationById("1")).andReturn(machine);
         EasyMock.replay(this.service);
 
         this.request.setIds(new IdRequest("1"));
         this.managerRead.execute(this.context);
 
         Assert.assertEquals(200, this.response.getStatus());
         Assert.assertEquals(ConstantsPath.MACHINE_CONFIGURATION_PATH + "/1",
             ((CimiMachineConfiguration) this.response.getCimiData()).getId());
         EasyMock.verify(this.service);
     }
 
     @Test
     public void testDelete() throws Exception {
 
         this.service.deleteMachineConfiguration("1");
         EasyMock.replay(this.service);
 
         this.request.setIds(new IdRequest("1"));
         this.managerDelete.execute(this.context);
 
         Assert.assertEquals(200, this.response.getStatus());
         EasyMock.verify(this.service);
     }
 
     @Test
     public void testUpdate() throws Exception {
 
         this.service.updateMachineConfiguration(EasyMock.anyObject(MachineConfiguration.class));
         EasyMock.replay(this.service);
 
         CimiMachineConfiguration cimi = new CimiMachineConfiguration();
         cimi.setName("foo");
         this.request.setIds(new IdRequest("1"));
         this.request.setCimiData(cimi);
 
         this.managerUpdate.execute(this.context);
 
         Assert.assertEquals(200, this.response.getStatus());
         EasyMock.verify(this.service);
     }
 
     @Test
     public void testUpdateWithCimiSelect() throws Exception {
 
        List<String> attrs = new ArrayList<String>();
        attrs.add("name");
        attrs.add("description");
        MachineConfiguration machineConfig = new MachineConfiguration();
        machineConfig.setName("fooName");
        machineConfig.setDescription("fooDescription");
 
        this.service.updateMachineConfigurationAttributes(EasyMock.eq("1"), EasyMock.anyObject(MachineConfiguration.class),
            EasyMock.eq(attrs));
         EasyMock.replay(this.service);
 
         CimiMachineConfiguration cimi = new CimiMachineConfiguration();
         cimi.setName("fooName");
         cimi.setDescription("fooDescription");
         this.request.setIds(new IdRequest("1"));
         this.request.setCimiData(cimi);
         this.request.getParams().getCimiSelect().setInitialValues(new String[] {"name", "description"});
 
         this.managerUpdate.execute(this.context);
 
         Assert.assertEquals(200, this.response.getStatus());
         EasyMock.verify(this.service);
     }
 
 }
