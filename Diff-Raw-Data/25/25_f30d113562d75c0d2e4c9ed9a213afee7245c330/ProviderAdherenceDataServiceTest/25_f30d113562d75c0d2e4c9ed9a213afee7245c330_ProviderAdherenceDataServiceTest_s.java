 package org.motechproject.whp.reports.service;
 
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.motechproject.testing.utils.BaseUnitTest;
 import org.motechproject.whp.reports.contract.adherence.ProviderAdherenceSummaries;
 import org.motechproject.whp.reports.contract.adherence.ProviderAdherenceSummary;
 import org.motechproject.whp.reports.dao.ProviderAdherenceQueryDAO;
 import org.motechproject.whp.reports.domain.TreatmentWeek;
 import org.motechproject.whp.reports.model.AdherenceStatus;
 
 import java.util.List;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.hasItem;
 import static org.hamcrest.core.Is.is;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.util.DateUtil.today;
 import static org.motechproject.whp.reports.date.WHPDate.toSqlDate;
 
 public class ProviderAdherenceDataServiceTest extends BaseUnitTest{
 
     @Mock
     ProviderAdherenceQueryDAO providerAdherenceQueryDAO;
 
     ProviderAdherenceDataService providerAdherenceDataService;
 
     @Before
     public void setUp() {
         initMocks(this);
         LocalDate today = today();
         mockCurrentDate(today);
         providerAdherenceDataService = new ProviderAdherenceDataService(providerAdherenceQueryDAO);
     }
 
     @Test
     public void shouldReturnProviderAdherenceSummary() {
         ProviderAdherenceSummary adherenceSummary = createAdherenceSummary("providerId");
         adherenceSummary.setAdherenceGiven(true);
 
         List<ProviderAdherenceSummary> providerAdherenceSummaryList = asList(adherenceSummary);
         String district = "district";
 
        LocalDate referenceDate = today().minusWeeks(7);
         TreatmentWeek treatmentWeek = new TreatmentWeek(referenceDate);
 
         when(providerAdherenceQueryDAO.getProviderAdherenceSummaries(district)).thenReturn(providerAdherenceSummaryList);
 
         List<List<AdherenceStatus>> allAdherenceStatuses = asList(
           asList(new AdherenceStatus("providerId", false)),
           asList(new AdherenceStatus("providerId", true)),
           asList(new AdherenceStatus("providerId", false)),
           asList(new AdherenceStatus("providerId", true)),
           asList(new AdherenceStatus("providerId", false)),
           asList(new AdherenceStatus("providerId", true)),
           asList(new AdherenceStatus("providerId", false)),
           asList(new AdherenceStatus("providerId", true))
         );
 
         for(int i=0; i < 8; i++){
             when(providerAdherenceQueryDAO.getAdherenceGivenStatus(district,
                     toSqlDate(treatmentWeek.startDate()),
                     toSqlDate(treatmentWeek.endDate())))
                     .thenReturn(allAdherenceStatuses.get(i));
             treatmentWeek.moveToNextWeek();
         }
 
         ProviderAdherenceSummaries adherenceSummaries = providerAdherenceDataService.getAdherenceSummary(district);
 
         ProviderAdherenceSummary expectedSummary = createAdherenceSummary("providerId");
         expectedSummary.setAdherenceGiven(true);
         expectedSummary.setAdherenceMissingWeeks(4);
 
         assertThat(adherenceSummaries.getAdherenceSummaryList().size(), is(1));
         assertThat(adherenceSummaries.getAdherenceSummaryList(), hasItem(expectedSummary));
     }
 
     private ProviderAdherenceSummary createAdherenceSummary(String providerId) {
         ProviderAdherenceSummary adherenceSummary = new ProviderAdherenceSummary();
         adherenceSummary.setProviderId(providerId);
         adherenceSummary.setPrimaryMobile("primary");
         adherenceSummary.setSecondaryMobile("secondary");
         adherenceSummary.setTertiaryMobile("tertiary");
         return adherenceSummary;
     }
 }
