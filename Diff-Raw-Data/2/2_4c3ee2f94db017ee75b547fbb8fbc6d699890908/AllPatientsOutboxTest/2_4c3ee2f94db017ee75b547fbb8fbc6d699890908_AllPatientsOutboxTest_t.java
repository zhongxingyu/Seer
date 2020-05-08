 package org.motechproject.ghana.national.repository;
 
 import ch.lambdaj.function.convert.Converter;
 import org.joda.time.*;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.motechproject.ghana.national.domain.AlertType;
 import org.motechproject.ghana.national.domain.AlertWindow;
 import org.motechproject.ghana.national.domain.Patient;
 import org.motechproject.ghana.national.domain.ivr.AudioPrompts;
 import org.motechproject.ghana.national.domain.ivr.MobileMidwifeAudioClips;
 import org.motechproject.model.Time;
 import org.motechproject.mrs.model.MRSFacility;
 import org.motechproject.mrs.model.MRSPatient;
 import org.motechproject.mrs.model.MRSPerson;
 import org.motechproject.mrs.services.MRSPatientAdapter;
 import org.motechproject.outbox.api.contract.SortKey;
 import org.motechproject.outbox.api.domain.OutboundVoiceMessage;
 import org.motechproject.outbox.api.domain.OutboundVoiceMessageStatus;
 import org.motechproject.outbox.api.service.VoiceOutboxService;
 import org.motechproject.testing.utils.BaseUnitTest;
 import org.motechproject.util.DateUtil;
 
 import java.util.*;
 
 import static ch.lambdaj.Lambda.convert;
 import static java.util.Arrays.asList;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.ghana.national.configuration.ScheduleNames.ANC_DELIVERY;
 import static org.motechproject.ghana.national.configuration.ScheduleNames.TT_VACCINATION;
 import static org.motechproject.ghana.national.repository.AllPatientsOutbox.*;
 
 public class AllPatientsOutboxTest extends BaseUnitTest {
 
     @InjectMocks
     AllPatientsOutbox allPatientsOutbox = new AllPatientsOutbox();
     @Mock
     private VoiceOutboxService mockOutboxService;
     @Mock
     private MRSPatientAdapter mockMRSPatientAdapter;
     private LocalTime currentTime = DateUtil.now().toLocalTime();
     private LocalDate currentDate = DateUtil.today();
     private Patient patient;
 
     @Before
     public void setUp() {
         initMocks(this);
         dateTime(currentDate, currentTime);
         patient = new Patient(new MRSPatient("motechId", new MRSPerson(), new MRSFacility("facilityId")));
     }
 
     @Test
     public void shouldAddCareVoiceMessageToOutboxForAGivenExternalId() {
         final String clipName = "clip.wav";
         Date expirationTimeInMillis = DateUtil.newDateTime(new Date(DateTimeUtils.currentTimeMillis())).plus(Period.weeks(1)).toDate();
 
         final DateTime windowStart = DateUtil.newDateTime(2012, 1, 1);
         allPatientsOutbox.addCareMessage(patient.getMotechId(), clipName, Period.weeks(1), AlertWindow.DUE, windowStart);
 
         assertMessageAddedToOutbox(expirationTimeInMillis, new HashMap<String, Object>() {{
             put(AUDIO_CLIP_NAME, clipName);
             put(WINDOW, AlertWindow.DUE);
             put(WINDOW_START, windowStart);
             put(TYPE, AlertType.CARE);
         }});
     }
 
     private void assertMessageAddedToOutbox(Date expirationTimeInMillis, Map<String, Object> expectedParameters) {
         ArgumentCaptor<OutboundVoiceMessage> messageCaptor = ArgumentCaptor.forClass(OutboundVoiceMessage.class);
         verify(mockOutboxService).addMessage(messageCaptor.capture());
 
         OutboundVoiceMessage voiceMessage = messageCaptor.getValue();
 
         assertDate(voiceMessage.getCreationTime(), new Date(DateTime.now().getMillis()));
         assertDate(voiceMessage.getExpirationDate(), expirationTimeInMillis);
         assertThat(voiceMessage.getExternalId(), is(equalTo(patient.getMotechId())));
         assertThat(voiceMessage.getParameters(), is(equalTo(expectedParameters)));
     }
 
     @Test
     public void shouldAddAppointmentVoiceMessageToOutboxForAGivenExternalId() {
         final String clipName = "clip.wav";
         Date expirationTimeInMillis = DateUtil.newDateTime(new Date(DateTimeUtils.currentTimeMillis())).plus(Period.weeks(1)).toDate();
 
         allPatientsOutbox.addAppointmentMessage(patient.getMotechId(), clipName, Period.weeks(1));
 
         assertMessageAddedToOutbox(expirationTimeInMillis, new HashMap<String, Object>() {{
             put(AUDIO_CLIP_NAME, clipName);
             put(TYPE, AlertType.APPOINTMENT);
         }});
 
     }
 
     @Test
     public void shouldAddMobileMidwifeVoiceMessageToOutboxForAGivenExternalId() {
         final MobileMidwifeAudioClips clipName = MobileMidwifeAudioClips.PREGNANCY_WEEK_7;
         Date expirationTimeInMillis = DateUtil.newDateTime(new Date(DateTimeUtils.currentTimeMillis())).plus(Period.weeks(1)).toDate();
 
         allPatientsOutbox.addMobileMidwifeMessage(patient.getMotechId(), clipName, Period.weeks(1));
 
         assertMessageAddedToOutbox(expirationTimeInMillis, new HashMap<String, Object>() {{
             put(AUDIO_CLIP_NAME, clipName);
             put(TYPE, AlertType.MOBILE_MIDWIFE);
         }});
     }
 
     private void assertDate(Date date1, Date date2) {
         assertThat(date1.getYear(), is(equalTo(date2.getYear())));
         assertThat(date1.getMonth(), is(equalTo(date2.getMonth())));
         assertThat(date1.getMonth(), is(equalTo(date2.getMonth())));
         assertThat(date1.getHours(), is(equalTo(date2.getHours())));
         assertThat(date1.getMinutes(), is(equalTo(date2.getMinutes())));
     }
 
     @Test
     public void shouldReturnSortedClipNames_GivenAPatientId() {
         final String appointmentClip = "appointment_clip";
         final String mmClip = "mobilemidwife_clip";
         final String careDueClip_2000_11_21__10_10 = "careDueClip_2000_11_21__10_10";
         final String careDueClip_2000_11_25__10_10 = "careDueClip_2000_11_25__10_10";
         final String careDueClip_2000_11_25__8_00 = "careDueClip_2000_11_25__8_00";
         final String careLateClip_2000_11_26__10_10 = "careLateClip_2000_11_26__10_10";
         final String careLateClip_2000_11_28__10_10 = "careLateClip_2000_11_28__10_10";
         final String careEarlyClip_2000_11_18__10_10 = "careEarlyClip_2000_11_18__10_10";
         final String careEarlyClip_2000_11_17__10_10 = "careEarlyClip_2000_11_17__10_10";
         final String careMaxClip_2000_11_30__9_10 = "careMaxClip_2000_11_30__9_10";
         final String careMaxClip_2000_11_30__8_30 = "careMaxClip_2000_11_30__8_30";
 
         List<OutboundVoiceMessage> messages = new ArrayList<OutboundVoiceMessage>() {{
             add(careMessage(careEarlyClip_2000_11_18__10_10, AlertWindow.UPCOMING, DateUtil.newDateTime(DateUtil.newDate(2000, 11, 18), new Time(10, 10))));
             add(careMessage(careDueClip_2000_11_25__10_10, AlertWindow.DUE, DateUtil.newDateTime(DateUtil.newDate(2000, 11, 25), new Time(10, 10))));
             add(careMessage(careLateClip_2000_11_26__10_10, AlertWindow.OVERDUE, DateUtil.newDateTime(DateUtil.newDate(2000, 11, 26), new Time(10, 10))));
             add(mobileMidwifeMessage(mmClip));
             add(careMessage(careDueClip_2000_11_21__10_10, AlertWindow.DUE, DateUtil.newDateTime(DateUtil.newDate(2000, 11, 21), new Time(10, 10))));
             add(careMessage(careMaxClip_2000_11_30__8_30, AlertWindow.MAX, DateUtil.newDateTime(DateUtil.newDate(2000, 11, 30), new Time(8, 30))));
             add(careMessage(careLateClip_2000_11_28__10_10, AlertWindow.OVERDUE, DateUtil.newDateTime(DateUtil.newDate(2000, 11, 28), new Time(10, 10))));
             add(appointmentMessage(appointmentClip));
             add(careMessage(careEarlyClip_2000_11_17__10_10, AlertWindow.UPCOMING, DateUtil.newDateTime(DateUtil.newDate(2000, 11, 17), new Time(10, 10))));
             add(careMessage(careMaxClip_2000_11_30__9_10, AlertWindow.MAX, DateUtil.newDateTime(DateUtil.newDate(2000, 11, 30), new Time(9, 10))));
             add(careMessage(careDueClip_2000_11_25__8_00, AlertWindow.DUE, DateUtil.newDateTime(DateUtil.newDate(2000, 11, 25), new Time(8, 00))));
         }};
 
         String motechId = "motech_id";
         when(mockOutboxService.getMessages(motechId, OutboundVoiceMessageStatus.PENDING, SortKey.CreationTime)).thenReturn(messages);
 
         List<OutboundVoiceMessage> audioFiles = allPatientsOutbox.getAudioFileNames(motechId);
 
         List<String> audioClips = convert(audioFiles, new Converter<OutboundVoiceMessage, String>() {
             @Override
             public String convert(OutboundVoiceMessage outboundVoiceMessage) {
                 return (String) outboundVoiceMessage.getParameters().get(AUDIO_CLIP_NAME);
             }
         });
 
         assertThat(audioClips, is(asList(careMaxClip_2000_11_30__8_30, careMaxClip_2000_11_30__9_10, careLateClip_2000_11_26__10_10,
                 careLateClip_2000_11_28__10_10, careDueClip_2000_11_21__10_10, careDueClip_2000_11_25__8_00, careDueClip_2000_11_25__10_10,
                 careEarlyClip_2000_11_17__10_10, careEarlyClip_2000_11_18__10_10, appointmentClip, mmClip)));
     }
 
     @Test
     public void shouldRemoveMessagesFromOutboxBasedOnPatientIdAndType() {
         final String motechId = "1234567";
         String patientId = "patientId";
         MRSPatient mrsPatient = mock(MRSPatient.class);
        final OutboundVoiceMessage outboundVoiceMessage1 = buildOutboxMessage(motechId, AlertType.MOBILE_MIDWIFE.name(), MobileMidwifeAudioClips.CHILD_CARE_WEEK_1, "mm");
         final OutboundVoiceMessage outboundVoiceMessage2 = buildOutboxMessage(motechId, AlertType.CARE.name(), AudioPrompts.fileNameForCareSchedule(TT_VACCINATION.getName(), AlertWindow.DUE), "care");
         final OutboundVoiceMessage outboundVoiceMessage3 = buildOutboxMessage(motechId, AlertType.CARE.name(), AudioPrompts.fileNameForCareSchedule(ANC_DELIVERY.getName(), AlertWindow.DUE), "anc");
 
         when(mockMRSPatientAdapter.getPatient(patientId)).thenReturn(mrsPatient);
         when(mrsPatient.getMotechId()).thenReturn(motechId);
 
         when(mockOutboxService.getMessages(motechId, OutboundVoiceMessageStatus.PENDING, SortKey.CreationTime)).thenReturn(Arrays.asList(outboundVoiceMessage1, outboundVoiceMessage2, outboundVoiceMessage3));
         allPatientsOutbox.removeCareAndAppointmentMessages(patientId, TT_VACCINATION.getName());
         verify(mockOutboxService).getMessages(motechId, OutboundVoiceMessageStatus.PENDING, SortKey.CreationTime);
         verify(mockOutboxService).removeMessage("care");
 
         reset(mockOutboxService);
         when(mockOutboxService.getMessages(motechId, OutboundVoiceMessageStatus.PENDING, SortKey.CreationTime)).thenReturn(Arrays.asList(outboundVoiceMessage1, outboundVoiceMessage2, outboundVoiceMessage3));
         allPatientsOutbox.removeCareAndAppointmentMessages(patientId, ANC_DELIVERY.getName());
         verify(mockOutboxService).getMessages(motechId, OutboundVoiceMessageStatus.PENDING, SortKey.CreationTime);
         verify(mockOutboxService).removeMessage("anc");
 
         reset(mockOutboxService);
         when(mockOutboxService.getMessages(motechId, OutboundVoiceMessageStatus.PENDING, SortKey.CreationTime)).thenReturn(Arrays.asList(outboundVoiceMessage1, outboundVoiceMessage2, outboundVoiceMessage3));
         allPatientsOutbox.removeMobileMidwifeMessages(motechId);
         verify(mockOutboxService).getMessages(motechId, OutboundVoiceMessageStatus.PENDING, SortKey.CreationTime);
         verify(mockOutboxService).removeMessage("mm");
     }
 
     private OutboundVoiceMessage buildOutboxMessage(String motechId, final String alertType, final Object audioClipName, String id) {
         final OutboundVoiceMessage outboundVoiceMessage = new OutboundVoiceMessage();
         outboundVoiceMessage.setExternalId(motechId);
         outboundVoiceMessage.setId(id);
         outboundVoiceMessage.setParameters(new HashMap<String, Object>() {{
             put("TYPE", alertType);
             put("AUDIO_CLIP_NAME", audioClipName);
         }});
         return outboundVoiceMessage;
     }
 
     private OutboundVoiceMessage careMessage(final String careClip, final AlertWindow alertWindow, final DateTime windowStart) {
         OutboundVoiceMessage outboundVoiceMessage = new OutboundVoiceMessage();
         outboundVoiceMessage.setParameters(new HashMap<String, Object>() {{
             put(AUDIO_CLIP_NAME, careClip);
             put(TYPE, AlertType.CARE.name());
             put(WINDOW, alertWindow.name());
             put(WINDOW_START, windowStart.toString());
         }});
         return outboundVoiceMessage;
     }
 
     private OutboundVoiceMessage appointmentMessage(final String appointmentClip) {
         OutboundVoiceMessage outboundVoiceMessage = new OutboundVoiceMessage();
         outboundVoiceMessage.setParameters(new HashMap<String, Object>() {{
             put(AUDIO_CLIP_NAME, appointmentClip);
             put(TYPE, AlertType.APPOINTMENT.name());
         }});
         return outboundVoiceMessage;
     }
 
     private OutboundVoiceMessage mobileMidwifeMessage(final String mmClip) {
         OutboundVoiceMessage outboundVoiceMessage = new OutboundVoiceMessage();
         outboundVoiceMessage.setParameters(new HashMap<String, Object>() {{
             put(AUDIO_CLIP_NAME, mmClip);
             put(TYPE, AlertType.MOBILE_MIDWIFE.name());
         }});
         return outboundVoiceMessage;
     }
 
 
 }
