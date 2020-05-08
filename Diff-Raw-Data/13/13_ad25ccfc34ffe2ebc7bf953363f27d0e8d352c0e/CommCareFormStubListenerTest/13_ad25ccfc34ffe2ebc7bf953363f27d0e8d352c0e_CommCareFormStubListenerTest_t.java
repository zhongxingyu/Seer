 package org.worldvision.sierraleone.listener;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.motechproject.commcare.domain.CaseInfo;
 import org.motechproject.commcare.domain.CommcareForm;
 import org.motechproject.commcare.domain.FormValueElement;
 import org.motechproject.commcare.events.constants.EventDataKeys;
 import org.motechproject.commcare.events.constants.EventSubjects;
 import org.motechproject.commcare.service.CommcareCaseService;
 import org.motechproject.commcare.service.CommcareFormService;
 import org.motechproject.event.MotechEvent;
 import org.motechproject.event.listener.EventRelay;
 import org.worldvision.sierraleone.constants.Commcare;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 
 /**
  * These tests only validate that the appropriate number of events are published.  They
  * do not validate the data contained in the events..
  */
 public class CommCareFormStubListenerTest {
     @Mock
     CommcareCaseService commcareCaseService;
 
     @Mock
     private CommcareFormService commcareFormService;
 
     @Mock
     private EventRelay eventRelay;
 
     @InjectMocks
     private CommCareFormStubListener commCareFormStubListener = new CommCareFormStubListener();
 
     @Before
     public void setUp() {
         initMocks(this);
     }
 
     @Test
     public void shouldNotPublishEventFromFormWeDontNeed() {
         String formId = "formId";
         CommcareForm form = CommcareForm("A Form We Don't Care About");
 
         when(commcareFormService.retrieveForm(formId)).thenReturn(form);
 
         commCareFormStubListener.handle(CommcareFormStubEvent(formId));
 
         verify(eventRelay, never()).sendEventMessage(null);
     }
 
     @Test
     public void postPartumVisitWithoutReferralShouldPublishOneEvent() {
         String formId = "formId";
         CommcareForm form = CommcareForm("Post-Partum Visit");
 
         AddSingleValueFormField(form, Commcare.STILL_ALIVE, "yes");
         AddSingleValueFormField(form, Commcare.GAVE_BIRTH, "yes");
         AddSingleValueFormField(form, Commcare.CREATE_REFERRAL, "no");
         AddSingleValueFormField(form, Commcare.REFERRAL_ID, null);
         AddSingleValueFormField(form, Commcare.DATE_OF_VISIT, "2013-01-03");
 
         FormValueElement aCase = AddSingleValueFormField(form, Commcare.CASE, "case");
         aCase.addAttribute(Commcare.CASE_ID, "caseId");
 
         FormValueElement postPartumVisit = new FormValueElement();
         postPartumVisit.setElementName(Commcare.POST_PARTUM_VISIT);
 
         AddSubelementValueFormField(postPartumVisit, Commcare.DATE_OF_BIRTH, "2013-01-03");
         AddSubelementValueFormField(postPartumVisit, Commcare.ATTENDED_POSTNATAL, "yes");
         AddSubelementValueFormField(postPartumVisit, Commcare.PLACE_OF_BIRTH, "home");
 
         form.getForm().addFormValueElement(Commcare.POST_PARTUM_VISIT, postPartumVisit);
 
         when(commcareFormService.retrieveForm(formId)).thenReturn(form);
 
         commCareFormStubListener.handle(CommcareFormStubEvent(formId));
 
         verify(eventRelay, times(1)).sendEventMessage(Matchers.any(MotechEvent.class));
     }
 
     @Test
     public void postPartumVisitWithReferralShouldPublishTwoEvents() {
         String formId = "formId";
         String referralId = "referralId";
         String referralCaseId = "referralCaseId";
         String motherCaseId = "motherCaseId";
         CommcareForm form = CommcareForm("Post-Partum Visit");
 
         AddSingleValueFormField(form, Commcare.STILL_ALIVE, "yes");
         AddSingleValueFormField(form, Commcare.GAVE_BIRTH, "yes");
         AddSingleValueFormField(form, Commcare.CREATE_REFERRAL, "yes");
         AddSingleValueFormField(form, Commcare.REFERRAL_ID, "referralId");
         AddSingleValueFormField(form, Commcare.DATE_OF_VISIT, "2013-01-03");
 
         FormValueElement aCase = AddSingleValueFormField(form, Commcare.CASE, "case");
         aCase.addAttribute(Commcare.CASE_ID, motherCaseId);
 
         FormValueElement postPartumVisit = new FormValueElement();
         postPartumVisit.setElementName(Commcare.POST_PARTUM_VISIT);
 
         AddSubelementValueFormField(postPartumVisit, Commcare.DATE_OF_BIRTH, "2013-01-03");
         AddSubelementValueFormField(postPartumVisit, Commcare.ATTENDED_POSTNATAL, "yes");
         AddSubelementValueFormField(postPartumVisit, Commcare.PLACE_OF_BIRTH, "home");
 
         form.getForm().addFormValueElement(Commcare.POST_PARTUM_VISIT, postPartumVisit);
 
         CaseInfo referralCase = new CaseInfo();
         Map<String, String> fieldValues = new HashMap<String, String>();
         fieldValues.put(Commcare.CASE_TYPE, "referral");
 
         referralCase.setFieldValues(fieldValues);
 
         when(commcareCaseService.getCaseByCaseId(referralCaseId)).thenReturn(referralCase);
 
         CaseInfo motherCase = new CaseInfo();
         fieldValues = new HashMap<String, String>();
         fieldValues.put(Commcare.CASE_TYPE, "mother");
 
         motherCase.setFieldValues(fieldValues);
 
         when(commcareCaseService.getCaseByCaseId(motherCaseId)).thenReturn(motherCase);
 
         when(commcareFormService.retrieveForm(formId)).thenReturn(form);
 
         List<String> list = Arrays.asList(motherCaseId, referralCaseId);
         commCareFormStubListener.handle(CommcareFormStubEvent(formId, list));
 
         verify(eventRelay, times(2)).sendEventMessage(Matchers.any(MotechEvent.class));
     }
 
     @Test
     public void postPartumVisitWithMissingFieldShouldNotPublishEvent() {
         String formId = "formId";
         CommcareForm form = CommcareForm("Post-Partum Visit");
 
         AddSingleValueFormField(form, Commcare.GAVE_BIRTH, "yes");
         AddSingleValueFormField(form, Commcare.CREATE_REFERRAL, "no");
         AddSingleValueFormField(form, Commcare.REFERRAL_ID, null);
         AddSingleValueFormField(form, Commcare.DATE_OF_VISIT, "2013-01-03");
 
         FormValueElement aCase = AddSingleValueFormField(form, Commcare.CASE, "case");
//        aCase.addAttribute(Commcare.CASE_ID, "caseId");
 
         FormValueElement postPartumVisit = new FormValueElement();
         postPartumVisit.setElementName(Commcare.POST_PARTUM_VISIT);
 
         AddSubelementValueFormField(postPartumVisit, Commcare.DATE_OF_BIRTH, "2013-01-03");
         AddSubelementValueFormField(postPartumVisit, Commcare.ATTENDED_POSTNATAL, "yes");
         AddSubelementValueFormField(postPartumVisit, Commcare.PLACE_OF_BIRTH, "home");
 
         form.getForm().addFormValueElement(Commcare.POST_PARTUM_VISIT, postPartumVisit);
 
         when(commcareFormService.retrieveForm(formId)).thenReturn(form);
 
         commCareFormStubListener.handle(CommcareFormStubEvent(formId));
 
         verify(eventRelay, never()).sendEventMessage(Matchers.any(MotechEvent.class));
     }
 
     @Test
     public void pregnancyVisitWithoutReferralShouldPublishNoEvent() {
         String formId = "formId";
         CommcareForm form = CommcareForm("Pregnancy Visit");
 
         AddSingleValueFormField(form, Commcare.CREATE_REFERRAL, "no");
         AddSingleValueFormField(form, Commcare.REFERRAL_ID, null);
         AddSingleValueFormField(form, Commcare.DATE_OF_VISIT, "2013-01-03");
 
         FormValueElement aCase = AddSingleValueFormField(form, Commcare.CASE, "case");
         aCase.addAttribute(Commcare.CASE_ID, "caseId");
 
         when(commcareFormService.retrieveForm(formId)).thenReturn(form);
 
         commCareFormStubListener.handle(CommcareFormStubEvent(formId));
 
         verify(eventRelay, never()).sendEventMessage(Matchers.any(MotechEvent.class));
     }
 
     @Test
     public void pregnancyVisitWithReferralShouldPublishOneEvent() {
         String formId = "formId";
         String referralId = "referralId";
         String referralCaseId = "referralCaseId";
         String motherCaseId = "motherCaseId";
         CommcareForm form = CommcareForm("Pregnancy Visit");
 
         AddSingleValueFormField(form, Commcare.CREATE_REFERRAL, "yes");
         AddSingleValueFormField(form, Commcare.REFERRAL_ID, "referralId");
         AddSingleValueFormField(form, Commcare.DATE_OF_VISIT, "2013-01-03");
 
         FormValueElement aCase = AddSingleValueFormField(form, Commcare.CASE, "case");
         aCase.addAttribute(Commcare.CASE_ID, motherCaseId);
 
         CaseInfo referralCase = new CaseInfo();
         Map<String, String> fieldValues = new HashMap<String, String>();
         fieldValues.put(Commcare.CASE_TYPE, "referral");
 
         referralCase.setFieldValues(fieldValues);
 
         when(commcareCaseService.getCaseByCaseId(referralCaseId)).thenReturn(referralCase);
 
         CaseInfo motherCase = new CaseInfo();
         fieldValues = new HashMap<String, String>();
         fieldValues.put(Commcare.CASE_TYPE, "mother");
 
         motherCase.setFieldValues(fieldValues);
 
         when(commcareCaseService.getCaseByCaseId(motherCaseId)).thenReturn(motherCase);
 
         when(commcareFormService.retrieveForm(formId)).thenReturn(form);
 
         List<String> list = Arrays.asList(motherCaseId, referralCaseId);
         commCareFormStubListener.handle(CommcareFormStubEvent(formId, list));
 
         verify(eventRelay, times(1)).sendEventMessage(Matchers.any(MotechEvent.class));
     }
 
     @Test
     public void pregnancyVisitWithReferralWithMissingFieldShouldNotPublishEvent() {
         String formId = "formId";
         String referralId = "referralId";
         String referralCaseId = "referralCaseId";
         String motherCaseId = "motherCaseId";
         CommcareForm form = CommcareForm("Pregnancy Visit");
 
         AddSingleValueFormField(form, Commcare.CREATE_REFERRAL, "yes");
         AddSingleValueFormField(form, Commcare.DATE_OF_VISIT, "2013-01-03");
 
         FormValueElement aCase = AddSingleValueFormField(form, Commcare.CASE, "case");
         aCase.addAttribute(Commcare.CASE_ID, motherCaseId);
 
         CaseInfo referralCase = new CaseInfo();
         Map<String, String> fieldValues = new HashMap<String, String>();
         fieldValues.put(Commcare.CASE_TYPE, "referral");
 
         referralCase.setFieldValues(fieldValues);
 
         when(commcareCaseService.getCaseByCaseId(referralCaseId)).thenReturn(referralCase);
 
         CaseInfo motherCase = new CaseInfo();
         fieldValues = new HashMap<String, String>();
         fieldValues.put(Commcare.CASE_TYPE, "mother");
 
         motherCase.setFieldValues(fieldValues);
 
         when(commcareCaseService.getCaseByCaseId(motherCaseId)).thenReturn(motherCase);
 
         when(commcareFormService.retrieveForm(formId)).thenReturn(form);
 
         List<String> list = Arrays.asList(motherCaseId, referralCaseId);
         commCareFormStubListener.handle(CommcareFormStubEvent(formId, list));
 
         verify(eventRelay, times(1)).sendEventMessage(Matchers.any(MotechEvent.class));
     }
 
     @Test
     public void childVisitFormShouldPublishEvent() {
         String formId = "formId";
         CommcareForm form = CommcareForm("Child Visit");
 
         String childCaseId = "childCaseId";
         FormValueElement formValueElement = new FormValueElement();
         formValueElement.setElementName(Commcare.CASE);
         formValueElement.addAttribute(Commcare.CASE_ID, childCaseId);
 
         form.getForm().addFormValueElement(Commcare.CASE, formValueElement);
 
         String motherCaseId = "motherCaseId";
         Map<String, String> parent = new HashMap<String, String>();
         parent.put(Commcare.CASE_TYPE, "mother");
         parent.put(Commcare.CASE_ID, motherCaseId);
 
         Map<String, Map<String, String>> indices = new HashMap<String, Map<String, String>>();
         indices.put(Commcare.PARENT, parent);
 
         CaseInfo childCase = new CaseInfo();
         Map<String, String> fieldValues = new HashMap<String, String>();
         fieldValues.put(Commcare.VITAMIN_A, "yes");
         fieldValues.put(Commcare.DATE_OF_BIRTH, "2013-01-03");
        fieldValues.put(Commcare.DATE_OF_VISIT, "2013-01-03");
 
         childCase.setFieldValues(fieldValues);
         childCase.setIndices(indices);
 
         when(commcareFormService.retrieveForm(formId)).thenReturn(form);
         when(commcareCaseService.getCaseByCaseId(childCaseId)).thenReturn(childCase);
 
         commCareFormStubListener.handle(CommcareFormStubEvent(formId));
 
         verify(eventRelay, times(1)).sendEventMessage(Matchers.any(MotechEvent.class));
     }
 
     @Test
     public void childVisitFormMissingFieldShouldNotPublishEvent() {
         String formId = "formId";
         CommcareForm form = CommcareForm("Child Visit");
 
         String childCaseId = "childCaseId";
         FormValueElement formValueElement = new FormValueElement();
         formValueElement.setElementName(Commcare.CASE);
         formValueElement.addAttribute(Commcare.CASE_ID, childCaseId);
 
         form.getForm().addFormValueElement(Commcare.CASE, formValueElement);
 
         String motherCaseId = "motherCaseId";
         Map<String, String> parent = new HashMap<String, String>();
         parent.put(Commcare.CASE_TYPE, "mother");
         parent.put(Commcare.CASE_ID, motherCaseId);
 
         Map<String, Map<String, String>> indices = new HashMap<String, Map<String, String>>();
         indices.put(Commcare.PARENT, parent);
 
         CaseInfo childCase = new CaseInfo();
         Map<String, String> fieldValues = new HashMap<String, String>();
         fieldValues.put(Commcare.DATE_OF_BIRTH, "2013-01-03");
 
         childCase.setFieldValues(fieldValues);
         childCase.setIndices(indices);
 
         when(commcareFormService.retrieveForm(formId)).thenReturn(form);
         when(commcareCaseService.getCaseByCaseId(childCaseId)).thenReturn(childCase);
 
         commCareFormStubListener.handle(CommcareFormStubEvent(formId));
 
         verify(eventRelay, never()).sendEventMessage(Matchers.any(MotechEvent.class));
     }
 
     private void AddSubelementValueFormField(FormValueElement formValueElement, String elementName, String value) {
         FormValueElement child = new FormValueElement();
 
         child.setElementName(elementName);
         child.setValue(value);
 
         formValueElement.addFormValueElement(elementName, child);
     }
 
     private FormValueElement AddSingleValueFormField(CommcareForm form, String elementName, String value) {
         FormValueElement formValueElement = new FormValueElement();
 
         formValueElement.setElementName(elementName);
         formValueElement.setValue(value);
 
         form.getForm().addFormValueElement(elementName, formValueElement);
 
         return formValueElement;
     }
 
     private CommcareForm CommcareForm(String formName) {
         FormValueElement formValueElement = new FormValueElement();
         formValueElement.addAttribute(Commcare.NAME, formName);
 
         CommcareForm form = new CommcareForm();
         form.setForm(formValueElement);
         return form;
     }
 
     private MotechEvent CommcareFormStubEvent(String formId) {
         MotechEvent event = new MotechEvent(EventSubjects.FORM_STUB_EVENT);
         event.getParameters().put(EventDataKeys.FORM_ID, formId);
         return event;
     }
 
     private MotechEvent CommcareFormStubEvent(String formId, List<String> cases) {
         MotechEvent event = CommcareFormStubEvent(formId);
 
         event.getParameters().put(EventDataKeys.CASE_IDS, cases);
 
         return event;
     }
 }
