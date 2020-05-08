 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.web.manager;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.hasKey;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.Collection;
 
 import nl.surfnet.bod.domain.PhysicalPort;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.service.InstituteService;
 import nl.surfnet.bod.service.PhysicalPortService;
import nl.surfnet.bod.service.PhysicalResourceGroupService;
 import nl.surfnet.bod.service.VirtualPortService;
 import nl.surfnet.bod.support.*;
 import nl.surfnet.bod.web.manager.PhysicalPortController.PhysicalPortView;
 import nl.surfnet.bod.web.manager.PhysicalPortController.UpdateManagerLabelCommand;
 import nl.surfnet.bod.web.security.RichUserDetails;
 import nl.surfnet.bod.web.security.Security;
 import nl.surfnet.bod.web.view.VirtualPortJsonView;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.data.domain.Sort;
 import org.springframework.validation.BeanPropertyBindingResult;
 
 import com.google.common.collect.Lists;
 
 @RunWith(MockitoJUnitRunner.class)
 public class PhysicalPortControllerTest {
 
   @InjectMocks
   private PhysicalPortController subject;
 
   @Mock
   private PhysicalPortService physicalPortServiceMock;
 
   @Mock
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Mock
   private InstituteService instituteService;
 
   @Mock
   private VirtualPortService virtualPortServiceMock;
 
   private RichUserDetails user;
 
  private PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();

   @Before
   public void setAuthenticatedUser() {
     user = new RichUserDetailsFactory().addUserGroup("urn:manager-group").create();
     Security.setUserDetails(user);
   }
 
   @SuppressWarnings("unchecked")
   @Test
   public void listPorts() {
     ModelStub model = new ModelStub();
 
    when(
        physicalPortServiceMock.findAllocatedEntriesForPhysicalResourceGroupAndUser(eq(physicalResourceGroup),
            eq(user), eq(0), anyInt(), any(Sort.class))).thenReturn(
         Lists.newArrayList(new PhysicalPortFactory().setId(2L).create()));
 
    when(physicalResourceGroupService.findAllForManager(eq(user)))
        .thenReturn(Lists.newArrayList(physicalResourceGroup));

    subject.list(null, null, null, null, model);
 
     assertThat(model.asMap(), hasKey("list"));
     assertThat(model.asMap(), hasKey("maxPages"));
 
     Collection<PhysicalPortView> ports = (Collection<PhysicalPortView>) model.asMap().get("list");
     assertThat(ports, hasSize(1));
     assertThat(ports.iterator().next().getId(), is(2L));
   }
 
   @Test
   public void listVirtualPortsForPhysicalPortInJson() {
     PhysicalPort port = new PhysicalPortFactory().create();
 
     when(physicalPortServiceMock.find(101L)).thenReturn(port);
     when(virtualPortServiceMock.findAllForPhysicalPort(port)).thenReturn(
         Lists.newArrayList(new VirtualPortFactory().setManagerLabel("manager label").create()));
 
     Collection<VirtualPortJsonView> portsViews = subject.listVirtualPortsJson(101L);
 
     assertThat(portsViews, hasSize(1));
     assertThat(portsViews.iterator().next().getManagerLabel(), is("manager label"));
   }
 
   @Test
   public void listVirtualPortsForPhysicalPortInJsonForNonExistingId() {
     when(physicalPortServiceMock.find(101L)).thenReturn(null);
     when(virtualPortServiceMock.findAllForPhysicalPort(null)).thenThrow(new NullPointerException());
 
     subject.listVirtualPortsJson(101L);
   }
 
   @Test
   public void updateForNullPort() {
     UpdateManagerLabelCommand command = new PhysicalPortController.UpdateManagerLabelCommand();
     command.setId(234L);
 
     when(physicalPortServiceMock.find(234L)).thenReturn(null);
 
     subject.update(command, new BeanPropertyBindingResult(command, "updateCommand"), new ModelStub());
 
     verify(physicalPortServiceMock, never()).update(any(PhysicalPort.class));
   }
 
   @Test
   public void updateForIllegalPort() {
     PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setAdminGroupName("urn:manager-group").create();
     PhysicalPort port = new PhysicalPortFactory().setPhysicalResourceGroup(group).create();
     PhysicalResourceGroup illegalGroup = new PhysicalResourceGroupFactory().setAdminGroupName("urn:illegal-group")
         .create();
     PhysicalPort illegalPort = new PhysicalPortFactory().setPhysicalResourceGroup(illegalGroup).create();
 
     UpdateManagerLabelCommand command = new PhysicalPortController.UpdateManagerLabelCommand(port);
     command.setId(234L);
 
     when(physicalPortServiceMock.find(234L)).thenReturn(illegalPort);
 
     subject.update(command, new BeanPropertyBindingResult(command, "updateCommand"), new ModelStub());
 
     verify(physicalPortServiceMock, never()).update(any(PhysicalPort.class));
   }
 
   @Test
   public void updateForPort() {
     PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setAdminGroupName("urn:manager-group").create();
     PhysicalPort port = new PhysicalPortFactory().setId(1L).setPhysicalResourceGroup(group).create();
 
     UpdateManagerLabelCommand command = new PhysicalPortController.UpdateManagerLabelCommand(port);
 
     when(physicalPortServiceMock.find(1L)).thenReturn(port);
 
     subject.update(command, new BeanPropertyBindingResult(command, "updateCommand"), new ModelStub());
 
     verify(physicalPortServiceMock).update(port);
   }
 
   @Test
   public void updateFormForNonExistingPort() {
     when(physicalPortServiceMock.find(101L)).thenReturn(null);
 
     String page = subject.updateForm(101L, new ModelStub());
 
     assertThat(page, is("manager/physicalports"));
   }
 
   @Test
   public void updateFormForIllegalPort() {
     PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setAdminGroupName("urn:illegal-group").create();
     PhysicalPort port = new PhysicalPortFactory().setPhysicalResourceGroup(group).create();
 
     when(physicalPortServiceMock.find(101L)).thenReturn(port);
 
     String page = subject.updateForm(101L, new ModelStub());
 
     assertThat(page, is("manager/physicalports"));
   }
 
   @Test
   public void updateFormForPort() {
     PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setAdminGroupName("urn:manager-group").create();
     PhysicalPort port = new PhysicalPortFactory().setPhysicalResourceGroup(group).create();
 
     when(physicalPortServiceMock.find(101L)).thenReturn(port);
 
     ModelStub model = new ModelStub();
     String page = subject.updateForm(101L, model);
 
     assertThat(page, is("manager/physicalports/update"));
     assertThat(model.asMap(), hasKey("physicalPort"));
   }
 
 }
