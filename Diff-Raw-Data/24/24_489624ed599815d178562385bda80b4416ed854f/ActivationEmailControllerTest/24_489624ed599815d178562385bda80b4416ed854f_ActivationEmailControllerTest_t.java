 package nl.surfnet.bod.web.manager;
 
 import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import nl.surfnet.bod.domain.ActivationEmailLink;
import nl.surfnet.bod.service.InstituteService;
 import nl.surfnet.bod.service.PhysicalResourceGroupService;
 import nl.surfnet.bod.support.ActivationEmailLinkFactory;
import nl.surfnet.bod.support.ModelStub;
 
 import org.joda.time.LocalDateTime;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 @RunWith(MockitoJUnitRunner.class)
 public class ActivationEmailControllerTest {
 
   @InjectMocks
   private ActivationEmailController subject;
 
   @Mock
   private PhysicalResourceGroupService physicalResourceGroupServiceMock;
 
  @SuppressWarnings("unused")
  @Mock
  private InstituteService instituteService;

   @Test
   public void physicalResourceGroupShouldBeActivated() {
     ActivationEmailLink link = new ActivationEmailLinkFactory().setCreationDateTime(
         LocalDateTime.now().minusMinutes(10)).create();
    ModelStub model = new ModelStub();
 
     when(physicalResourceGroupServiceMock.findActivationLink("1234567890")).thenReturn(link);
 
    String page = subject.activateEmail("1234567890", model);
 
     assertThat(page, is("manager/emailConfirmed"));
 
    assertThat(model.asMap(), hasEntry("physicalResourceGroup", Object.class.cast(link.getPhysicalResourceGroup())));

     verify(physicalResourceGroupServiceMock).activate(link.getPhysicalResourceGroup());
   }
 
   @Test
   public void activationLinkIsNotValidAnymore() {
     ActivationEmailLink link = new ActivationEmailLinkFactory().setCreationDateTime(
         LocalDateTime.now().minusDays(10)).create();
 
     when(physicalResourceGroupServiceMock.findActivationLink("1234567890")).thenReturn(link);
 
    String page = subject.activateEmail("1234567890", new ModelStub());
 
     assertThat(link.getPhysicalResourceGroup().isActive(), is(false));
     assertThat(page, is("manager/linkNotValid"));
   }
 
   @Test
   public void activationLinkIsNotValid() {
     when(physicalResourceGroupServiceMock.findActivationLink("1234567890")).thenReturn(null);
 
    String page = subject.activateEmail("1234567890", new ModelStub());
 
     assertThat(page, is("index"));
   }
 }
