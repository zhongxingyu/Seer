 package nl.surfnet.bod.web;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.util.Collection;
 import java.util.List;
 
 import nl.surfnet.bod.domain.Institution;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
 import nl.surfnet.bod.service.InstitutionService;
 import nl.surfnet.bod.support.PhysicalResourceGroupFactory;
 
 import org.junit.Test;
 
 public class InstitutionControllerTest {
 
   private InstitutionService institutionServiceMock = mock(InstitutionService.class);
   private PhysicalResourceGroupRepo prgRepoMock = mock(PhysicalResourceGroupRepo.class);
 
   private InstitutionController subject = new InstitutionController(institutionServiceMock, prgRepoMock);
 
   @Test
   public void instutionsShouldBeFilteredBySearchParamIgnoringCase() {
     Collection<Institution> unfilteredInstitutions = newArrayList(
         new Institution("Universiteit Utrecht"),
         new Institution("Universiteit Amsterdam"));
 
     when(institutionServiceMock.getInstitutions()).thenReturn(unfilteredInstitutions);
 
     Collection<Institution> institutionsInAmsterdam = subject.jsonList("amsterdam");
 
     assertThat(institutionsInAmsterdam, hasSize(1));
     assertThat(institutionsInAmsterdam.iterator().next().getName(), containsString("Amsterdam"));
   }
 
   @Test
   public void existingInstitutionNamesShouldBeFiltered() {
     Collection<Institution> unfilteredInstitutions = newArrayList(
         new Institution("Utrecht"),
         new Institution("Amsterdam"));
     List<PhysicalResourceGroup> existingGroups = newArrayList(
         new PhysicalResourceGroupFactory().setInstitution("Amsterdam").create());
 
     when(institutionServiceMock.getInstitutions()).thenReturn(unfilteredInstitutions);
     when(prgRepoMock.findAll()).thenReturn(existingGroups);
 
     Collection<Institution> institutions = subject.jsonList("");
 
     assertThat(institutions, hasSize(1));
     assertThat(institutions.iterator().next().getName(), is("Utrecht"));
   }

  @Test
  public void institutionsJsonListShouldAcceptNullAsQueryParam() {
    Collection<Institution> institutions = newArrayList(new Institution("Utrecht"));
    when(institutionServiceMock.getInstitutions()).thenReturn(institutions);

    Collection<Institution> result = subject.jsonList(null);

    assertThat(result, hasSize(1));
  }
 }
