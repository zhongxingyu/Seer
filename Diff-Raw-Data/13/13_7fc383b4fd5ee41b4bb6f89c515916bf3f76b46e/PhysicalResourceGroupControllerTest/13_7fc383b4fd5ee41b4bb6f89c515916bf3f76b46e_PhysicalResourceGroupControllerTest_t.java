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
         .setAdminGroupName("urn:ict-manager").create();
 
     UpdateEmailCommand command = new PhysicalResourceGroupController.UpdateEmailCommand(group);
     command.setManagerEmail("new@mail.com");
 
     when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);
 
     String page = subject.update(command, new BeanPropertyBindingResult(command, "updateEmailCommand"), model,
         requestAttributes);
 
     assertThat(requestAttributes.getFlashAttributes(), hasKey("infoMessages"));
     assertThat(page, is("redirect:physicalresourcegroups"));
    assertThat(group.getManagerEmail(), is(command.getManagerEmail()));
     verify(physicalResourceGroupServiceMock).sendAndPersistActivationRequest(group);
   }
 
   @Test
   public void whenUserIsNotAnIctManagerShouldNotUpdate() {
     Model model = new ModelStub();
     RedirectAttributes redirectAttributes = new ModelStub();
     PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(1L).setManagerEmail("old@mail.com")
         .setAdminGroupName("urn:no-ict-manager").create();
 
     UpdateEmailCommand command = new PhysicalResourceGroupController.UpdateEmailCommand(group);
     command.setManagerEmail("new@mail.com");
 
     when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);
 
     String page = subject.update(command, new BeanPropertyBindingResult(command, "updateEmailCommand"), model,
         redirectAttributes);
 
     assertThat(page, is("redirect:physicalresourcegroups"));
 
     verify(physicalResourceGroupServiceMock, never()).sendAndPersistActivationRequest(group);
   }
 
   @Test
   public void whenEmailDidNotChangeShouldNotUpdate() {
     Model model = new ModelStub();
     RedirectAttributes redirectAttributes = new ModelStub();
 
     PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(1L).setManagerEmail("old@mail.com")
         .setAdminGroupName("urn:ict-manager").create();
 
     UpdateEmailCommand command = new PhysicalResourceGroupController.UpdateEmailCommand(group);
 
     when(physicalResourceGroupServiceMock.find(1L)).thenReturn(group);
 
     String page = subject.update(command, new BeanPropertyBindingResult(command, "updateEmailCommand"), model,
         redirectAttributes);
 
     assertThat(page, is("redirect:physicalresourcegroups"));
 
     verify(physicalResourceGroupServiceMock, never()).sendAndPersistActivationRequest(group);
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
 
     assertThat(page, is("redirect:physicalresourcegroups"));
 
     verify(physicalResourceGroupServiceMock, never()).sendAndPersistActivationRequest(any(PhysicalResourceGroup.class));
   }
 
   @SuppressWarnings("serial")
   @Test
   public void whenGroupHasErrors() {
     Model model = new ModelStub();
     RedirectAttributes redirectAttributes = new ModelStub();
 
     UpdateEmailCommand command = new PhysicalResourceGroupController.UpdateEmailCommand();
     command.setId(1L);
     PhysicalResourceGroup group = new PhysicalResourceGroupFactory().setId(1L).setAdminGroupName("urn:ict-manager").create();
 
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
     verify(physicalResourceGroupServiceMock, never()).sendAndPersistActivationRequest(any(PhysicalResourceGroup.class));
   }
 
 }
