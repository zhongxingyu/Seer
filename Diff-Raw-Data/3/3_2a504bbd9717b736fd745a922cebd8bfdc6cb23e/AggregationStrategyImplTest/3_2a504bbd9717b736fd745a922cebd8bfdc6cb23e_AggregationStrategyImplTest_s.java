 package org.motechproject.ghana.national.repository;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.motechproject.cmslite.api.model.ContentNotFoundException;
 import org.motechproject.cmslite.api.model.StringContent;
 import org.motechproject.cmslite.api.service.CMSLiteService;
 import org.motechproject.ghana.national.domain.AlertWindow;
 import org.motechproject.ghana.national.domain.Facility;
import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.ghana.national.domain.SmsTemplateKeys;
 import org.motechproject.ghana.national.messagegateway.domain.MessageRecipientType;
 import org.motechproject.ghana.national.messagegateway.domain.SMS;
 import org.motechproject.ghana.national.messagegateway.domain.SMSPayload;
 import org.motechproject.ghana.national.service.PatientService;
 import org.motechproject.mrs.model.MRSFacility;
 import org.motechproject.testing.utils.BaseUnitTest;
 import org.motechproject.util.DateUtil;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 
 import static ch.lambdaj.Lambda.join;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.ghana.national.domain.AlertWindow.DUE;
 import static org.motechproject.ghana.national.domain.AlertWindow.UPCOMING;
 
 public class AggregationStrategyImplTest extends BaseUnitTest {
 
     @Mock
     CMSLiteService mockCmsLiteService;
 
     @Mock
     AllFacilities mockAllFacilities;
 
     @InjectMocks
     AggregationStrategyImpl aggregationStrategy = new AggregationStrategyImpl();
     @Mock
     private PatientService mockPatientService;
 
     @Before
     public void setUp() throws Exception {
         initMocks(this);
         super.mockCurrentDate(DateUtil.now());
     }
 
     @Test
     public void shouldFilterDefaultMessagesFromListOfSmsIfThereAreValidMessages() throws ContentNotFoundException {
         final String defaultMessage = "default-message";
 
         final String motechId = "motechId";
         String phoneNumber = "phoneNumber";
         when(mockAllFacilities.getFacilityByMotechId(motechId)).thenReturn(new Facility(new MRSFacility(motechId)).phoneNumber(phoneNumber));
         when(mockCmsLiteService.getStringContent(Locale.getDefault().getLanguage(),
                 SmsTemplateKeys.FACILITIES_DEFAULT_MESSAGE_KEY)).thenReturn(new StringContent(null, null, defaultMessage));
         final SMSPayload defaultSMSPayload = SMSPayload.fromText(defaultMessage, "ph", null, null, MessageRecipientType.FACILITY);
         List<SMSPayload> messagesList = new ArrayList<SMSPayload>() {{
             add(SMSPayload.fromText(DUE.getName() + ",milestoneName1,motechId,serialNumber,firstName,lastName", motechId, null, null, MessageRecipientType.FACILITY));
             add(SMSPayload.fromText(DUE.getName() + ",milestoneName2,motechId,serialNumber,firstName,lastName", motechId, null, null, MessageRecipientType.FACILITY));
             add(defaultSMSPayload);
         }};
         List<SMS> filteredSMSPayloads = aggregationStrategy.aggregate(messagesList);
         assertThat(filteredSMSPayloads, not(hasItem(new SMS(defaultSMSPayload, phoneNumber))));
     }
 
     @Test
     public void shouldNotFilterDefaultMessagesFromListOfSmsIfThereAreNoValidMessages() throws ContentNotFoundException {
         final String defaultMessage = "default-message";
 
         final String motechId = "motechId";
         String phoneNumber = "phoneNumber";
         when(mockAllFacilities.getFacilityByMotechId(motechId)).thenReturn(new Facility(new MRSFacility(motechId)).phoneNumber(phoneNumber));
         when(mockCmsLiteService.getStringContent(Locale.getDefault().getLanguage(),
                 SmsTemplateKeys.FACILITIES_DEFAULT_MESSAGE_KEY)).thenReturn(new StringContent(null, null, defaultMessage));
         final SMSPayload defaultSMSPayload = SMSPayload.fromText(defaultMessage, motechId, null, null, MessageRecipientType.FACILITY);
         List<SMSPayload> messagesList = new ArrayList<SMSPayload>() {{
             add(defaultSMSPayload);
         }};
         List<SMS> filteredSMSPayloads = aggregationStrategy.aggregate(messagesList);
         assertThat(filteredSMSPayloads, hasItem(new SMS(defaultSMSPayload, phoneNumber)));
     }
 
     @Test
     public void shouldSendAggregatedSMSForPatient() throws ContentNotFoundException {
         final String defaultMessage = "Ashanti MEPS has no " + join(AlertWindow.ghanaNationalWindowNames(), ", ") + " cares for this week";
         String phoneNumber = "phoneNumber";
         final String motechId = "uniqueId";
         when(mockAllFacilities.getFacilityByMotechId(motechId)).thenReturn(new Facility(new MRSFacility(motechId)).phoneNumber(phoneNumber));
 
         when(mockCmsLiteService.getStringContent(Locale.getDefault().getLanguage(),
                 SmsTemplateKeys.FACILITIES_DEFAULT_MESSAGE_KEY)).thenReturn(new StringContent(null, null, "${facility} has no ${windowNames} cares for this week"));
         List<SMSPayload> messagesList = new ArrayList<SMSPayload>() {{
             add(SMSPayload.fromText(UPCOMING.getName() + ",milestoneName1,motechId,serialNumber,firstName,lastName", motechId, null, null, MessageRecipientType.FACILITY));
             add(SMSPayload.fromText(UPCOMING.getName() + ",milestoneName2,motechId,serialNumber,firstName,lastName", motechId, null, null, MessageRecipientType.FACILITY));
             add(SMSPayload.fromText(DUE.getName() + ",milestoneName,motechId,serialNumber,firstName,lastName", motechId, null, null, MessageRecipientType.FACILITY));
             add(SMSPayload.fromText(DUE.getName() + ",milestoneName,motechId2,serialNumber,firstName2,lastName3", motechId, null, null, MessageRecipientType.FACILITY));
             add(SMSPayload.fromText(DUE.getName() + ",milestoneName,motechId3,serialNumber,firstName2,lastName3", motechId, null, null, MessageRecipientType.FACILITY));
             add(SMSPayload.fromText(defaultMessage, motechId, null, null, MessageRecipientType.FACILITY));
         }};
 
         final List<SMS> aggregatedSMSList = aggregationStrategy.aggregate(messagesList);
         assertThat(aggregatedSMSList, hasItem(new SMS(SMSPayload.fromText(UPCOMING.getName() + ": firstName lastName, motechId, serialNumber, milestoneName1, milestoneName2", motechId, DateUtil.now(), null, MessageRecipientType.FACILITY), phoneNumber)));
         assertThat(aggregatedSMSList, hasItem(new SMS(SMSPayload.fromText(DUE.getName() + ": firstName lastName, motechId, serialNumber, milestoneName, firstName2 lastName3, motechId2, serialNumber, milestoneName, firstName2 lastName3, motechId3, serialNumber, milestoneName", motechId, DateUtil.now(), null, MessageRecipientType.FACILITY), phoneNumber)));
         assertThat(aggregatedSMSList, hasItem(new SMS(SMSPayload.fromText("Ashanti MEPS has no Overdue cares for this week", motechId, DateUtil.now(), null, MessageRecipientType.FACILITY), phoneNumber)));
     }
 
     @Test
     public void shouldAggregateManySMSBasedOnWindowNames() throws ContentNotFoundException {
         final String message1 = "patient due for anc 1";
         final String message2 = "U are late for measles";
         final String message3 = "this is the third alert";
 
 
         final String motechId = "motechId";
         String phoneNumber = "PhoneNumber";
         when(mockPatientService.getPatientPhoneNumber(motechId)).thenReturn(phoneNumber);
 
         List<SMSPayload> messagesList = new ArrayList<SMSPayload>() {{
             add(SMSPayload.fromText(message1, motechId, null, null, MessageRecipientType.PATIENT));
             add(SMSPayload.fromText(message2, motechId, null, null, MessageRecipientType.PATIENT));
             add(SMSPayload.fromText(message3, motechId, null, null, MessageRecipientType.PATIENT));
         }};
 
         final List<SMS> aggregatedSMSPayloadList = aggregationStrategy.aggregate(messagesList);
         assertThat(aggregatedSMSPayloadList.size(), is(1));
         assertThat(aggregatedSMSPayloadList, hasItem(new SMS(SMSPayload.fromText(message1 + AggregationStrategyImpl.SMS_SEPARATOR + message2
                 + AggregationStrategyImpl.SMS_SEPARATOR + message3 + AggregationStrategyImpl.SMS_SEPARATOR, motechId, DateUtil.now(), null, MessageRecipientType.PATIENT), phoneNumber)));
     }
 
     @Test
     public void shouldReturnEmptyListIfThereIsNothingToAggregate() {
         List<SMS> smsPayloadList = aggregationStrategy.aggregate(Collections.<SMSPayload>emptyList());
        assertThat(smsPayload        List<SMS> smsPayloadList = aggregationStrategy.aggregate(Collections.<SMSPayload>emptyList());
         assertThat(smsPayloadList.size(), is(0));
     }
 }
