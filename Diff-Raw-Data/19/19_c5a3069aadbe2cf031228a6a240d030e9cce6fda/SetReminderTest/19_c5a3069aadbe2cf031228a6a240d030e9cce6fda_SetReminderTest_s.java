 package org.worldvision.sierraleone.rules.three;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.motechproject.commcare.domain.CaseInfo;
 import org.motechproject.commcare.domain.CommcareForm;
 import org.motechproject.commcare.domain.FormValueElement;
 import org.motechproject.commons.api.DataProvider;
 import org.motechproject.event.MotechEvent;
 import org.motechproject.scheduler.MotechSchedulerActionProxyService;
 import org.motechproject.tasks.domain.ActionEvent;
 import org.motechproject.tasks.domain.ActionParameter;
 import org.motechproject.tasks.domain.Task;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.worldvision.sierraleone.MotechEventBuilder;
 import org.worldvision.sierraleone.constants.Commcare;
 import org.worldvision.sierraleone.rules.RuleTest;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyMap;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.commons.date.util.DateUtil.newDateTime;
 import static org.motechproject.tasks.domain.ParameterType.DATE;
 import static org.motechproject.tasks.domain.ParameterType.MAP;
 
 public class SetReminderTest extends RuleTest {
     private static final String COMMCARE_PROVIDER_ID = "7f871474375dc43dfd8a534abd021841";
     private static final String FORM_ID_VALUE = "formId";
     private static final String CASE_ID_VALUE = "caseId";
    private static final DateTime START_DATE = newDateTime(2013, 8, 14);
     private static final String SUBJECT = "org.worldvision.sierraleone.post-partum." + CASE_ID_VALUE;
 
     @Mock
     private DataProvider commcareDataProvider;
 
     @Mock
     private BundleContext bundleContext;
 
     @Mock
     private ServiceReference serviceReference;
 
     @Mock
     private MotechSchedulerActionProxyService actionProxyService;
 
     private CommcareForm commcareForm;
 
     private CaseInfo caseInfo;
 
     @Override
     @Before
     public void setUp() throws Exception {
         initMocks(this);
         super.setUp();
 
        mockCurrentDate(new LocalDate(2013, 8, 14));

         handler.addDataProvider(COMMCARE_PROVIDER_ID, commcareDataProvider);
         handler.setBundleContext(bundleContext);
 
         setTask(3, "set_reminder.json", "rule-3-set-reminder");
     }
 
     @Test
     public void shouldExecuteTaskCorrectly() throws Exception {
         ActionEvent unscheduleJobs = new ActionEvent();
         unscheduleJobs.setServiceInterface(MotechSchedulerActionProxyService.class.getName());
         unscheduleJobs.setServiceMethod("unscheduleJobs");
         unscheduleJobs.addParameter(new ActionParameter("subject", "subject"), true);
 
         ActionEvent scheduleRunOnceJob = new ActionEvent();
         scheduleRunOnceJob.setServiceInterface(MotechSchedulerActionProxyService.class.getName());
         scheduleRunOnceJob.setServiceMethod("scheduleRunOnceJob");
         scheduleRunOnceJob.addParameter(new ActionParameter("motechEventSubject", "motechEventSubject"), true);
         scheduleRunOnceJob.addParameter(new ActionParameter("motechEventParameters", "motechEventParameters", MAP), true);
         scheduleRunOnceJob.addParameter(new ActionParameter("startDate", "startDate", DATE), true);
 
         when(taskService.getActionEventFor(task.getActions().get(0))).thenReturn(unscheduleJobs);
         when(taskService.getActionEventFor(task.getActions().get(1))).thenReturn(scheduleRunOnceJob);
 
         Map<String, String> formLookup = new HashMap<>();
         formLookup.put("id", FORM_ID_VALUE);
 
         Map<String, String> caseLookup = new HashMap<>();
         caseLookup.put("id", CASE_ID_VALUE);
 
         FormValueElement nextVisitDatePlus1 = new FormValueElement();
         nextVisitDatePlus1.setElementName("next_visit_date_plus_1");
         nextVisitDatePlus1.setValue(START_DATE.toString("yyyy-MM-dd"));
 
         FormValueElement caseElement = new FormValueElement();
         caseElement.setElementName("case");
         caseElement.addAttribute(Commcare.CASE_ID, CASE_ID_VALUE);
 
         FormValueElement form = new FormValueElement();
         form.addAttribute(Commcare.NAME, "Post-Partum Visit");
         form.addFormValueElement(Commcare.NEXT_VISIT_DATE_PLUS_1, nextVisitDatePlus1);
         form.addFormValueElement(Commcare.CASE, caseElement);
 
         commcareForm = new CommcareForm();
         commcareForm.setForm(form);
 
         when(commcareDataProvider.lookup("CommcareForm", formLookup)).thenReturn(commcareForm);
         when(commcareDataProvider.lookup("CaseInfo", caseLookup)).thenReturn(caseInfo);
 
         Map<Object, Object> map = new HashMap<>();
         map.put("mother_case_id", CASE_ID_VALUE);
 
         when(bundleContext.getServiceReference(unscheduleJobs.getServiceInterface())).thenReturn(serviceReference);
         when(bundleContext.getServiceReference(scheduleRunOnceJob.getServiceInterface())).thenReturn(serviceReference);
 
         when(bundleContext.getService(serviceReference)).thenReturn(actionProxyService);
 
         handler.handle(commcareFormstub());
 
         verify(activityService, never()).addWarning(any(Task.class), anyString(), anyString());
 
         verify(actionProxyService).unscheduleJobs(SUBJECT);
         verify(actionProxyService).scheduleRunOnceJob(SUBJECT, map, START_DATE.plusDays(1));
     }
 
     @Test
     public void shouldNotExecuteTaskIfNextVisitDateNotExist() throws Exception {
         Map<String, String> formLookup = new HashMap<>();
         formLookup.put("id", FORM_ID_VALUE);
 
         FormValueElement nextVisitDatePlus1 = new FormValueElement();
         nextVisitDatePlus1.setElementName("next_visit_date_plus_1");
 
         FormValueElement caseElement = new FormValueElement();
         caseElement.setElementName("case");
         caseElement.addAttribute(Commcare.CASE_ID, CASE_ID_VALUE);
 
         FormValueElement form = new FormValueElement();
         form.addAttribute(Commcare.NAME, "Post-Partum Visit");
         form.addFormValueElement(Commcare.CASE, caseElement);
         form.addFormValueElement("next_visit_date_plus_1", nextVisitDatePlus1);
 
         commcareForm = new CommcareForm();
         commcareForm.setForm(form);
 
         when(commcareDataProvider.lookup("CommcareForm", formLookup)).thenReturn(commcareForm);
 
         handler.handle(commcareFormstub());
 
         verify(actionProxyService, never()).unscheduleJobs(anyString());
         verify(actionProxyService, never()).scheduleRunOnceJob(anyString(), anyMap(), any(DateTime.class));
     }
 
     @Test
     public void shouldNotExecuteTaskIfFormNameIsIncorrect() throws Exception {
         Map<String, String> formLookup = new HashMap<>();
         formLookup.put("id", FORM_ID_VALUE);
 
         FormValueElement nextVisitDatePlus1 = new FormValueElement();
         nextVisitDatePlus1.setElementName("next_visit_date_plus_1");
         nextVisitDatePlus1.setValue("2013-08-14");
 
         FormValueElement caseElement = new FormValueElement();
         caseElement.setElementName("case");
         caseElement.addAttribute(Commcare.CASE_ID, CASE_ID_VALUE);
 
         FormValueElement form = new FormValueElement();
         form.addAttribute(Commcare.NAME, "something");
         form.addFormValueElement(Commcare.CASE, caseElement);
 
         commcareForm = new CommcareForm();
         commcareForm.setForm(form);
 
         when(commcareDataProvider.lookup("CommcareForm", formLookup)).thenReturn(commcareForm);
 
         handler.handle(commcareFormstub());
 
         verify(actionProxyService, never()).unscheduleJobs(anyString());
         verify(actionProxyService, never()).scheduleRunOnceJob(anyString(), anyMap(), any(DateTime.class));
     }
 
     private MotechEvent commcareFormstub() {
         return new MotechEventBuilder()
                 .withSubject(getTriggerSubject())
                 .withParameter("formId", FORM_ID_VALUE)
                 .build();
     }
 }
