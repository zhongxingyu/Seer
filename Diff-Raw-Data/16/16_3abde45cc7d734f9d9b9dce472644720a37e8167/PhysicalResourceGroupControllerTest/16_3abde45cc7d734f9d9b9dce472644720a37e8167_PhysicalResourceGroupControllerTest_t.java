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
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.service.PhysicalResourceGroupService;
 import nl.surfnet.bod.support.ModelStub;
 import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
 import nl.surfnet.bod.support.RichUserDetailsFactory;
 import nl.surfnet.bod.web.manager.PhysicalResourceGroupController.UpdateEmailCommand;
 import nl.surfnet.bod.web.security.Security;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.ui.Model;
 import org.springframework.validation.BeanPropertyBindingResult;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 @RunWith(MockitoJUnitRunner.class)
 public class PhysicalResourceGroupControllerTest {
 
   @InjectMocks
   private PhysicalResourceGroupController subject;
 
   @Mock
   private PhysicalResourceGroupService physicalResourceGroupServiceMock;
 
   @Before
   public void loginUser() {
     Security.setUserDetails(new RichUserDetailsFactory().addUserGroup("urn:ict-manager").create());
   }
 
   @Test
   public void whenEmailHasChangedShouldCallService() {
     Model model = new ModelStub();
     RedirectAttributes requestAttributes = new ModelStub();
 
     PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(1L).setManagerEmail("old@mail.com")
         .setAdminGroup("urn:ict-manager").create();
  
     UpdateEmailCommand command = new PhysicalResourceGroupController.UpdateEmailCommand(group);
     command.setManagerEmail("new@mail.com");
 
     when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);
 
     String page = subject.update(command, new BeanPropertyBindingResult(command, "updateEmailCommand"), model,
         requestAttributes);
 
     assertThat(requestAttributes.getFlashAttributes(), hasKey("infoMessages"));
    assertThat(page, is("redirect:/manager/index"));
     assertThat(group.getManagerEmail(), is(command.getManagerEmail()));
     verify(physicalResourceGroupServiceMock).sendActivationRequest(group);
   }
 
   @Test
   public void whenUserIsNotAnIctManagerShouldNotUpdate() {
     Model model = new ModelStub();
     RedirectAttributes redirectAttributes = new ModelStub();
     PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(1L).setManagerEmail("old@mail.com")
         .setAdminGroup("urn:no-ict-manager").create();
 
     UpdateEmailCommand command = new PhysicalResourceGroupController.UpdateEmailCommand(group);
     command.setManagerEmail("new@mail.com");
 
     when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);
 
     String page = subject.update(command, new BeanPropertyBindingResult(command, "updateEmailCommand"), model,
         redirectAttributes);
 
     assertThat(page, is("redirect:manager/index"));
 
     verify(physicalResourceGroupServiceMock, never()).sendActivationRequest(group);
   }
 
   @Test
   public void whenEmailDidNotChangeShouldNotUpdate() {
     Model model = new ModelStub();
     RedirectAttributes redirectAttributes = new ModelStub();
 
     PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(1L).setManagerEmail("old@mail.com")
         .setAdminGroup("urn:ict-manager").create();
 
     UpdateEmailCommand command = new PhysicalResourceGroupController.UpdateEmailCommand(group);
 
     when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);
 
     String page = subject.update(command, new BeanPropertyBindingResult(command, "updateEmailCommand"), model,
         redirectAttributes);
 
    assertThat(page, is("redirect:/manager/index"));
 
     verify(physicalResourceGroupServiceMock, never()).sendActivationRequest(group);
   }
 
   @Test
   public void whenGroupNotFoundDontCrashOrUpdate() {
     Model model = new ModelStub();
     RedirectAttributes redirectAttributes = new ModelStub();
 
     UpdateEmailCommand command = new PhysicalResourceGroupController.UpdateEmailCommand();
     command.setId(1L);
 
     when(physicalResourceGroupServiceMock.find(1L)).thenReturn(null);
 
     String page = subject.update(command, new BeanPropertyBindingResult(command, "updateEmailCommand"), model,
         redirectAttributes);
 
     assertThat(page, is("redirect:manager/index"));
 
     verify(physicalResourceGroupServiceMock, never()).sendActivationRequest(any(PhysicalResourceGroup.class));
   }
 
   @SuppressWarnings("serial")
   @Test
   public void whenGroupHasErrors() {
     Model model = new ModelStub();
     RedirectAttributes redirectAttributes = new ModelStub();
 
     UpdateEmailCommand command = new PhysicalResourceGroupController.UpdateEmailCommand();
     command.setId(1L);
     PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(1L).setAdminGroup("urn:ict-manager").create();
 
     when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);
 
     BeanPropertyBindingResult result = new BeanPropertyBindingResult(command, "updateEmailCommand") {
       @Override
       public boolean hasErrors() {
         return true;
       }
     };
 
     String page = subject.update(command, result, model,
         redirectAttributes);
 
     assertThat(page, is("manager/physicalresourcegroups/update"));
 
     assertThat(model.asMap(), hasKey("physicalResourceGroup"));
     verify(physicalResourceGroupServiceMock, never()).sendActivationRequest(any(PhysicalResourceGroup.class));
   }
 
 }
