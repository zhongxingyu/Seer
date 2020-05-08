 package org.openmrs.module.emr.fragment.controller.visit;
 
 import org.hamcrest.Matcher;
 import org.junit.Test;
 import org.mockito.ArgumentMatcher;
 import org.openmrs.*;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.context.UserContext;
 import org.openmrs.module.emr.EmrContext;
 import org.openmrs.module.emr.TestUiUtils;
 import org.openmrs.ui.framework.SimpleObject;
 import org.openmrs.ui.framework.UiFrameworkConstants;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.util.OpenmrsUtil;
 
 import java.text.ParseException;
 import java.util.Date;
 import java.util.LinkedHashSet;
 import java.util.List;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsNull.notNullValue;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class VisitDetailsFragmentControllerTest {
 
     @Test
     public void shouldReturnEncountersForVisit() throws ParseException {
         EmrContext emrContext = mock(EmrContext.class);
         UserContext userContext = mock(UserContext.class);
         User authenticatedUser = new User();
         when(userContext.getAuthenticatedUser()).thenReturn(authenticatedUser);
         when(emrContext.getUserContext()).thenReturn(userContext);
 
         AdministrationService administrationService = mock(AdministrationService.class);
         when(administrationService.getGlobalProperty(UiFrameworkConstants.GP_FORMATTER_DATETIME_FORMAT)).thenReturn("dd.MMM.yyyy, HH:mm:ss");
 
         Visit visit = new Visit();
         Location visitLocation = new Location();
         visitLocation.setName("Visit Location");
         visit.setLocation(visitLocation);
         visit.setStartDatetime(new Date());
         visit.setStopDatetime(new Date());
         Location encounterLocation = new Location();
         encounterLocation.setName("Location");
         EncounterType encounterType = new EncounterType();
         encounterType.setName("Encounter Type");
         encounterType.setUuid("abc-123-def-456");
         Provider provider = new Provider();
         provider.setName("Provider");
         EncounterProvider encounterProvider = new EncounterProvider();
         encounterProvider.setProvider(provider);
         encounterProvider.setEncounterRole(new EncounterRole());
 
         Encounter encounter = new Encounter();
         encounter.setEncounterId(7);
         encounter.setEncounterDatetime(new Date());
         encounter.setLocation(encounterLocation);
         encounter.setEncounterType(encounterType);
         encounter.setEncounterProviders(new LinkedHashSet<EncounterProvider>());
         encounter.getEncounterProviders().add(encounterProvider);
        encounter.setCreator(authenticatedUser);
 
         visit.addEncounter(encounter);
 
         UiUtils uiUtils = new TestUiUtils(administrationService);
         VisitDetailsFragmentController controller = new VisitDetailsFragmentController();
 
         SimpleObject response = controller.getVisitDetails(administrationService, visit, uiUtils, emrContext);
         List<SimpleObject> actualEncounters = (List<SimpleObject>) response.get("encounters");
         SimpleObject actualEncounter = actualEncounters.get(0);
 
         assertThat(response.get("startDatetime"), notNullValue());
         assertThat(response.get("stopDatetime"), notNullValue());
         assertThat((String) response.get("location"), is("Visit Location"));
 
         assertThat(actualEncounters.size(), is(1));
         assertThat((Integer) actualEncounter.get("encounterId"), is(7));
         assertThat((String) actualEncounter.get("location"), is("Location"));
         assertThat((SimpleObject) actualEncounter.get("encounterType"), isSimpleObjectWith("uuid", encounterType.getUuid(), "name", encounterType.getName()));
         assertThat(actualEncounter.get("encounterDatetime"), notNullValue());
         assertThat(actualEncounter.get("encounterDate"), notNullValue());
         assertThat(actualEncounter.get("encounterTime"), notNullValue());
         List<SimpleObject> actualProviders = (List<SimpleObject>) actualEncounter.get("encounterProviders");
         assertThat(actualProviders.size(), is(1));
         assertThat((String) actualProviders.get(0).get("provider"), is("Provider"));
     }
 
     private Matcher<SimpleObject> isSimpleObjectWith(final Object... propertiesAndValues) {
         return new ArgumentMatcher<SimpleObject>() {
             @Override
             public boolean matches(Object o) {
                 SimpleObject so = (SimpleObject) o;
                 for (int i = 0; i < propertiesAndValues.length; i += 2) {
                     String property = (String) propertiesAndValues[i];
                     Object expectedValue = propertiesAndValues[i + 1];
                     if (!OpenmrsUtil.nullSafeEquals(so.get(property), expectedValue)) {
                         return false;
                     }
                 }
                 return true;
             }
         };
     }
 
 
 }
