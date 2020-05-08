 package org.worldvision.sierraleone.listener;
 
 import org.joda.time.DateTime;
 import org.joda.time.Days;
 import org.motechproject.commcare.domain.CaseInfo;
 import org.motechproject.commcare.domain.CommcareForm;
 import org.motechproject.commcare.domain.FormValueElement;
 import org.motechproject.commcare.events.constants.EventDataKeys;
 import org.motechproject.commcare.events.constants.EventSubjects;
 import org.motechproject.commcare.service.CommcareCaseService;
 import org.motechproject.commcare.service.CommcareFormService;
 import org.motechproject.event.MotechEvent;
 import org.motechproject.event.listener.EventRelay;
 import org.motechproject.event.listener.annotations.MotechListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.worldvision.sierraleone.FormChecker;
 import org.worldvision.sierraleone.Utils;
 import org.worldvision.sierraleone.constants.Commcare;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import static org.worldvision.sierraleone.constants.Commcare.CASE;
 import static org.worldvision.sierraleone.constants.Commcare.CASE_ID;
 import static org.worldvision.sierraleone.constants.Commcare.CASE_TYPE;
 import static org.worldvision.sierraleone.constants.Commcare.CREATE_REFERRAL;
 import static org.worldvision.sierraleone.constants.Commcare.NAME;
 import static org.worldvision.sierraleone.constants.Commcare.PARENT;
 import static org.worldvision.sierraleone.constants.Commcare.POST_PARTUM_VISIT;
 import static org.worldvision.sierraleone.constants.Commcare.REFERRAL_ID;
 import static org.worldvision.sierraleone.constants.EventKeys.ATTENDED_POSTNATAL;
 import static org.worldvision.sierraleone.constants.EventKeys.CHILD_CASE_ID;
 import static org.worldvision.sierraleone.constants.EventKeys.CHILD_VISIT_10_DATE;
 import static org.worldvision.sierraleone.constants.EventKeys.CHILD_VISIT_11_DATE;
 import static org.worldvision.sierraleone.constants.EventKeys.CHILD_VISIT_5A_DATE;
 import static org.worldvision.sierraleone.constants.EventKeys.CHILD_VISIT_5B_DATE;
 import static org.worldvision.sierraleone.constants.EventKeys.CHILD_VISIT_5C_DATE;
 import static org.worldvision.sierraleone.constants.EventKeys.CHILD_VISIT_5D_DATE;
 import static org.worldvision.sierraleone.constants.EventKeys.CHILD_VISIT_6_DATE;
 import static org.worldvision.sierraleone.constants.EventKeys.CHILD_VISIT_7_DATE;
 import static org.worldvision.sierraleone.constants.EventKeys.CHILD_VISIT_8_DATE;
 import static org.worldvision.sierraleone.constants.EventKeys.CHILD_VISIT_9_DATE;
 import static org.worldvision.sierraleone.constants.EventKeys.CHILD_VISIT_FORM_SUBJECT;
 import static org.worldvision.sierraleone.constants.EventKeys.DATE_OF_BIRTH;
 import static org.worldvision.sierraleone.constants.EventKeys.DATE_OF_VISIT;
 import static org.worldvision.sierraleone.constants.EventKeys.DAYS_SINCE_BIRTH;
 import static org.worldvision.sierraleone.constants.EventKeys.GAVE_BIRTH;
 import static org.worldvision.sierraleone.constants.EventKeys.MOTHER_CASE_ID;
 import static org.worldvision.sierraleone.constants.EventKeys.MOTHER_REFERRAL_SUBJECT;
 import static org.worldvision.sierraleone.constants.EventKeys.PLACE_OF_BIRTH;
 import static org.worldvision.sierraleone.constants.EventKeys.POST_PARTUM_FORM_SUBJECT;
 import static org.worldvision.sierraleone.constants.EventKeys.REFERRAL_CASE_ID;
 import static org.worldvision.sierraleone.constants.EventKeys.SECOND_CONSECUTIVE_POST_PARTUM_VISIT_DATE;
 import static org.worldvision.sierraleone.constants.EventKeys.VITAMIN_A;
 
 @Component
 public class CommCareFormStubListener {
     private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
     @Autowired
     private CommcareFormService commcareFormService;
 
     @Autowired
     private CommcareCaseService commcareCaseService;
 
     @Autowired
     private EventRelay eventRelay;
 
     @MotechListener(subjects = EventSubjects.FORM_STUB_EVENT)
     public void handle(MotechEvent event) {
         logger.info("MotechEvent received on " + EventSubjects.FORM_STUB_EVENT);
 
         if (commcareFormService == null) {
             logger.error("CommCare service is not available!");
             return;
         }
 
         // read event payload
         Map<String, Object> eventParams = event.getParameters();
         String formId = (String) eventParams.get(EventDataKeys.FORM_ID);
         if (formId == null) {
             logger.error("No " + EventDataKeys.FORM_ID + " key in event: " + event.toString());
             return;
         }
 
         // Get the form from CommCare
         CommcareForm form = commcareFormService.retrieveForm(formId);
         if (form == null) {
             logger.error("Unable to load form " + formId + " from CommCare");
             return;
         }
 
         String formName = form.getForm().getAttributes().get(NAME);
         logger.info("form name " + formName);
 
         List<String> caseIds = (List<String>) event.getParameters().get(EventDataKeys.CASE_IDS);
 
         // Create MOTECH event, populate subject and params below
         List<MotechEvent> events = new ArrayList<>(0);
 
         switch (formName) {
             case "Post-Partum Visit":
                 events.addAll(convertPostPartumFormToEvents(form, caseIds));
                 break;
 
             case "Pregnancy Visit":
                 events.addAll(convertPregnancyVisitFormToEvents(form, caseIds));
                 break;
 
             case "Child Visit":
                 events.addAll(convertChildVisitFormToEvents(form));
                 break;
 
             default:
                 logger.info("Ignoring commcare forwarded form of type: " + formName);
                 break;
         }
 
         // publish event
         for (MotechEvent e : events) {
             if (null != e) {
                 eventRelay.sendEventMessage(e);
             }
         }
 
     }
 
     private List<MotechEvent> convertChildVisitFormToEvents(CommcareForm form) {
         FormValueElement element = form.getForm().getElement(CASE);
         String childCaseId = element.getAttributes().get(CASE_ID);
         String motherCaseId = "";
 
         CaseInfo childCase = commcareCaseService.getCaseByCaseId(childCaseId);
         if (childCase.getIndices().containsKey(PARENT)) {
             Map<String, String> parent = childCase.getIndices().get(PARENT);
 
             if (parent.containsKey(CASE_TYPE) && "mother".equals(parent.get(CASE_TYPE))) {
                 motherCaseId = childCase.getIndices().get(PARENT).get(CASE_ID);
             } else {
                 logger.error("Parent of childcase " + childCaseId + " is not a mothercase (" + parent.get(CASE_TYPE) + ")");
                 return Collections.<MotechEvent>emptyList();
             }
         } else {
             logger.error("No parent case for childcase: " + childCaseId);
             return Collections.<MotechEvent>emptyList();
         }
 
         String vitaminA = childCase.getFieldValues().get(Commcare.VITAMIN_A);
 
         DateTime dateOfBirth = getDateField(childCase, Commcare.DATE_OF_BIRTH);
         DateTime dateOfVisit = getDateField(childCase, Commcare.DATE_OF_VISIT);
 
         logger.info("Child Case Id: " + childCaseId);
         logger.info("Mother Case Id: " + motherCaseId);
         logger.info("dateOfBirth: " + dateOfBirth);
         logger.info("vitaminA: " + vitaminA);
 
         FormChecker checker = new FormChecker();
         checker.addMetadata("type", form.getId());
         checker.addMetadata("name", form.getForm().getAttributes().get(NAME));
         checker.addMetadata("id", form.getId());
 
         checker.checkFieldExists("dateOfBirth", dateOfBirth);
         checker.checkFieldExists("dateOfVisit", dateOfVisit);
         checker.checkFieldExists(CASE_ID, childCaseId);
         checker.checkFieldExists(MOTHER_CASE_ID, motherCaseId);
 
         if (!checker.check()) {
             return Collections.<MotechEvent>emptyList();
         }
 
         List<MotechEvent> ret = new ArrayList<>();
         ret.add(createChildVisitFormEvent(childCaseId, motherCaseId, childCase));
 
         return ret;
     }
 
     private MotechEvent createChildVisitFormEvent(String childCaseId, String motherCaseId, CaseInfo childCase) {
         MotechEvent event = new MotechEvent(CHILD_VISIT_FORM_SUBJECT);
 
         event.getParameters().put(DATE_OF_BIRTH, getDateField(childCase, Commcare.DATE_OF_BIRTH));
         event.getParameters().put(MOTHER_CASE_ID, motherCaseId);
         event.getParameters().put(CHILD_CASE_ID, childCaseId);
         event.getParameters().put(VITAMIN_A, childCase.getFieldValues().get(Commcare.VITAMIN_A));
         event.getParameters().put(CHILD_VISIT_5A_DATE, getDateField(childCase, Commcare.CHILD_VISIT_5A_DATE));
         event.getParameters().put(CHILD_VISIT_5B_DATE, getDateField(childCase, Commcare.CHILD_VISIT_5B_DATE));
         event.getParameters().put(CHILD_VISIT_5C_DATE, getDateField(childCase, Commcare.CHILD_VISIT_5C_DATE));
         event.getParameters().put(CHILD_VISIT_5D_DATE, getDateField(childCase, Commcare.CHILD_VISIT_5D_DATE));
         event.getParameters().put(CHILD_VISIT_6_DATE, getDateField(childCase, Commcare.CHILD_VISIT_6_DATE));
         event.getParameters().put(CHILD_VISIT_7_DATE, getDateField(childCase, Commcare.CHILD_VISIT_7_DATE));
         event.getParameters().put(CHILD_VISIT_8_DATE, getDateField(childCase, Commcare.CHILD_VISIT_8_DATE));
         event.getParameters().put(CHILD_VISIT_9_DATE, getDateField(childCase, Commcare.CHILD_VISIT_9_DATE));
         event.getParameters().put(CHILD_VISIT_10_DATE, getDateField(childCase, Commcare.CHILD_VISIT_10_DATE));
         event.getParameters().put(CHILD_VISIT_11_DATE, getDateField(childCase, Commcare.CHILD_VISIT_11_DATE));
 
         return event;
     }
 
     private DateTime getDateField(CaseInfo caseInfo, String fieldName) {
         String d = caseInfo.getFieldValues().get(fieldName);
         DateTime dateTime = Utils.dateTimeFromCommcareDateString(d);
 
         return dateTime;
     }
 
     private List<MotechEvent> convertPregnancyVisitFormToEvents(CommcareForm form, List<String> caseIds) {
         String dov = null;
         String createReferral = null;
         String referralId = null;
         String motherCaseId = null;
 
         FormValueElement element = form.getForm().getElement(CREATE_REFERRAL);
         createReferral = ((element != null) ? element.getValue() : null);
 
         element = form.getForm().getElement(REFERRAL_ID);
         referralId = ((element != null) ? element.getValue() : null);
 
         element = form.getForm().getElement(CASE);
         motherCaseId = element.getAttributes().get(CASE_ID);
 
         element = form.getForm().getElement(Commcare.DATE_OF_VISIT);
         dov = ((element != null) ? element.getValue() : null);
 
         DateTime dateOfVisit = Utils.dateTimeFromCommcareDateString(dov);
 
         logger.info("createReferral: " + createReferral);
         logger.info("referralId: " + referralId);
         logger.info("Mother Case Id: " + motherCaseId);
         logger.info("dateOfVisit: " + dateOfVisit);
 
         List<MotechEvent> ret = new ArrayList<>();
         MotechEvent event = null;
 
         if ("yes".equals(createReferral)) {
             event = getReferralEvent(motherCaseId, form, caseIds, dateOfVisit);
 
             ret.add(event);
         }
 
         return ret;
     }
 
     // Pulls data out of the commcare form and constructs events used by rule handlers
     private List<MotechEvent> convertPostPartumFormToEvents(CommcareForm form, List<String> caseIds) {
         String dov = getValue(form.getForm(), Commcare.DATE_OF_VISIT);
         String gaveBirth = getValue(form.getForm(), Commcare.GAVE_BIRTH);
         String createReferral = getValue(form.getForm(), Commcare.CREATE_REFERRAL);
         String referralId = getValue(form.getForm(), Commcare.REFERRAL_ID);
         String nextVisitDatePlus1 = getValue(form.getForm(), Commcare.NEXT_VISIT_DATE_PLUS_1);
 
         FormValueElement postPartumVisit = form.getForm().getElement(POST_PARTUM_VISIT);
         String dob = getValue(postPartumVisit, Commcare.DATE_OF_BIRTH);
         String attendedPostnatal = getValue(postPartumVisit, Commcare.ATTENDED_POSTNATAL);
         String placeOfBirth = getValue(postPartumVisit, Commcare.PLACE_OF_BIRTH);
 
        String motherCaseId = getValue(form.getForm().getElement(CASE), Commcare.CASE_ID);
 
         DateTime dateOfVisit = Utils.dateTimeFromCommcareDateString(dov);
         DateTime dateOfBirth = Utils.dateTimeFromCommcareDateString(dob);
         int daysSinceBirth = Days.daysBetween(dateOfBirth, new DateTime()).getDays();
 
         DateTime secondConsecutiveVisitDate = Utils.dateTimeFromCommcareDateString(nextVisitDatePlus1);
 
         // TODO: Get all the pp_v*_date fields and add to event
 
         logger.info("gaveBirth: " + gaveBirth);
         logger.info("createReferral: " + createReferral);
         logger.info("referralId: " + referralId);
         logger.info("dob: " + dob);
         logger.info("daysSinceBirth: " + daysSinceBirth);
         logger.info("attendedPostnatal: " + attendedPostnatal);
         logger.info("placeOfBirth: " + placeOfBirth);
         logger.info("Mother Case Id: " + motherCaseId);
         logger.info("Second Consecutive Visit Date: " + secondConsecutiveVisitDate);
 
         FormChecker checker = new FormChecker();
         checker.addMetadata("type", form.getId());
         checker.addMetadata("name", form.getForm().getAttributes().get(NAME));
         checker.addMetadata("id", form.getId());
 
         checker.checkFieldExists(MOTHER_CASE_ID, motherCaseId);
         checker.checkFieldExists(DATE_OF_VISIT, dateOfVisit);
         checker.checkFieldExists(SECOND_CONSECUTIVE_POST_PARTUM_VISIT_DATE, secondConsecutiveVisitDate);
 
         List<MotechEvent> ret = new ArrayList<>();
 
         if (checker.check()) {
             MotechEvent event = createPostPartumFormEvent(
                     gaveBirth, attendedPostnatal, placeOfBirth, motherCaseId, dateOfVisit, dateOfBirth,
                     daysSinceBirth, secondConsecutiveVisitDate
             );
 
             ret.add(event);
 
             if ("yes".equals(createReferral)) {
                 event = getReferralEvent(motherCaseId, form, caseIds, dateOfVisit);
 
                 ret.add(event);
             }
         }
 
         return ret;
     }
 
     private String getValue(FormValueElement form, String key) {
         FormValueElement element = form.getElement(key);
 
         return element != null ? element.getValue() : null;
     }
 
     private MotechEvent createPostPartumFormEvent(String gaveBirth, String attendedPostnatal,
                                                   String placeOfBirth, String motherCaseId,
                                                   DateTime dateOfVisit, DateTime dateOfBirth,
                                                   int daysSinceBirth,
                                                   DateTime secondConsecutiveVisitDate) {
         MotechEvent event = new MotechEvent(POST_PARTUM_FORM_SUBJECT);
 
         event.getParameters().put(GAVE_BIRTH, gaveBirth);
         event.getParameters().put(DATE_OF_BIRTH, dateOfBirth);
         event.getParameters().put(DATE_OF_VISIT, dateOfVisit);
         event.getParameters().put(DAYS_SINCE_BIRTH, daysSinceBirth);
         event.getParameters().put(ATTENDED_POSTNATAL, attendedPostnatal);
         event.getParameters().put(PLACE_OF_BIRTH, placeOfBirth);
         event.getParameters().put(MOTHER_CASE_ID, motherCaseId);
         event.getParameters().put(SECOND_CONSECUTIVE_POST_PARTUM_VISIT_DATE, secondConsecutiveVisitDate);
 
         return event;
     }
 
     // The commcare stub form event contains a list of the cases affected by the form.  This method iterates
     // over that list finding the one that is the case.  This requires loading the case from commcare, so
     // we filter out the mother case so we can eliminate one remote call
     private MotechEvent getReferralEvent(String motherCaseId, CommcareForm form, List<String> caseIds, DateTime dateOfVisit) {
         String referralId = null;
 
         for (String caseId : caseIds) {
             if (!motherCaseId.equals(caseId)) {
                 CaseInfo caseInfo = commcareCaseService.getCaseByCaseId(caseId);
 
                 if ("referral".equals(caseInfo.getFieldValues().get(CASE_TYPE))) {
                     referralId = caseId;
                 }
             }
         }
 
         FormChecker checker = new FormChecker();
         checker.addMetadata("type", "referral");
         checker.addMetadata("name", form.getForm().getAttributes().get(NAME));
         checker.addMetadata("id", form.getId());
 
         checker.checkFieldExists(Commcare.DATE_OF_VISIT, dateOfVisit);
         checker.checkFieldExists(REFERRAL_CASE_ID, referralId);
         checker.checkFieldExists(MOTHER_CASE_ID, motherCaseId);
 
         if (!checker.check()) {
             return null;
         }
 
         MotechEvent event;
         event = new MotechEvent(MOTHER_REFERRAL_SUBJECT);
 
         event.getParameters().put(MOTHER_CASE_ID, motherCaseId);
         event.getParameters().put(REFERRAL_CASE_ID, referralId);
         event.getParameters().put(DATE_OF_VISIT, dateOfVisit);
 
         return event;
     }
 }
