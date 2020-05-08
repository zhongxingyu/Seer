 package org.worldvision.sierraleone.rules.four;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.motechproject.commcare.CommcareDataProvider;
 import org.motechproject.commcare.domain.CaseInfo;
 import org.motechproject.commcare.domain.CommcareForm;
 import org.motechproject.commcare.domain.FormValueElement;
import org.motechproject.commons.date.util.DateUtil;
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
 
 public abstract class SetReminderChildDateTest extends RuleTest {
     private static final String COMMCARE_PROVIDER_ID = "3980fa00249eb3bf73e200bd85122e72";
     private static final String FORM_ID_VALUE = "formId";
     private static final String CASE_ID_VALUE = "caseId";
     private static final String MOTHER_CASE_ID_VALUE = "motherCaseId";
    private static final DateTime CHILD_DATE = DateUtil.today().toDateTimeAtStartOfDay().plusDays(5);
     private static final String SUBJECT = "org.worldvision.sierraleone.child-visit." + CHILD_DATE.toString("yyyy-MM-dd HH:mm Z");
 
     @Mock
     private CommcareDataProvider commcareDataProvider;
 
     @Mock
     private BundleContext bundleContext;
 
     @Mock
     private ServiceReference serviceReference;
 
     @Mock
     private MotechSchedulerActionProxyService actionProxyService;
 
     private CommcareForm commcareForm;
 
     private CaseInfo caseInfo;
 
     @Before
     public void setUp() throws Exception {
         initMocks(this);
         super.setUp();
 
         handler.addDataProvider(COMMCARE_PROVIDER_ID, commcareDataProvider);
         handler.setBundleContext(bundleContext);
 
         setTask(4, String.format("set_reminder_child_%s_date.json", getChildDate()), String.format("rule-4-set-reminder-child_%s_date", getChildDate()));
     }
 
     public abstract String getChildDate();
 
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
 
         FormValueElement caseElement = new FormValueElement();
         caseElement.setElementName("case");
         caseElement.addAttribute(Commcare.CASE_ID, CASE_ID_VALUE);
 
         FormValueElement form = new FormValueElement();
         form.addAttribute(Commcare.NAME, "Child Visit");
         form.addFormValueElement(Commcare.CASE, caseElement);
 
         commcareForm = new CommcareForm();
         commcareForm.setForm(form);
 
         Map<String, Map<String, String>> indices = new HashMap<>();
 
         Map<String, String> parent = new HashMap<>();
         parent.put("case_id", MOTHER_CASE_ID_VALUE);
         parent.put("case_type", "mother");
 
         indices.put("parent", parent);
 
         caseInfo = new CaseInfo();
         caseInfo.setIndices(indices);
         caseInfo.setFieldValues(new HashMap<String, String>());
         caseInfo.getFieldValues().put(String.format("child_%s_date", getChildDate()), CHILD_DATE.toString("yyyy-MM-dd"));
 
         when(commcareDataProvider.lookup("CommcareForm", formLookup)).thenReturn(commcareForm);
         when(commcareDataProvider.lookup("CaseInfo", caseLookup)).thenReturn(caseInfo);
 
         Map<Object, Object> map = new HashMap<>();
         map.put("child_case_id", CASE_ID_VALUE);
         map.put("mother_case_id", MOTHER_CASE_ID_VALUE);
 
         when(bundleContext.getServiceReference(unscheduleJobs.getServiceInterface())).thenReturn(serviceReference);
         when(bundleContext.getServiceReference(scheduleRunOnceJob.getServiceInterface())).thenReturn(serviceReference);
 
         when(bundleContext.getService(serviceReference)).thenReturn(actionProxyService);
 
         handler.handle(commcareFormstub());
 
         verify(activityService, never()).addWarning(any(Task.class), anyString(), anyString());
 
         verify(actionProxyService).unscheduleJobs(SUBJECT);
         verify(actionProxyService).scheduleRunOnceJob(SUBJECT, map, CHILD_DATE.plusDays(1));
     }
 
     @Test
     public void shouldNotExecuteTaskIfChildDateIfNotAfterNow() throws Exception {
         Map<String, String> formLookup = new HashMap<>();
         formLookup.put("id", FORM_ID_VALUE);
 
         Map<String, String> caseLookup = new HashMap<>();
         caseLookup.put("id", CASE_ID_VALUE);
 
         FormValueElement caseElement = new FormValueElement();
         caseElement.setElementName("case");
         caseElement.addAttribute(Commcare.CASE_ID, CASE_ID_VALUE);
 
         FormValueElement form = new FormValueElement();
         form.addAttribute(Commcare.NAME, "Child Visit");
         form.addFormValueElement(Commcare.CASE, caseElement);
 
         commcareForm = new CommcareForm();
         commcareForm.setForm(form);
 
         Map<String, Map<String, String>> indices = new HashMap<>();
 
         Map<String, String> parent = new HashMap<>();
         parent.put("case_id", MOTHER_CASE_ID_VALUE);
         parent.put("case_type", "mother");
 
         indices.put("parent", parent);
 
         caseInfo = new CaseInfo();
         caseInfo.setIndices(indices);
         caseInfo.setFieldValues(new HashMap<String, String>());
         caseInfo.getFieldValues().put(String.format("child_%s_date", getChildDate()), "2013-06-27");
 
         when(commcareDataProvider.lookup("CommcareForm", formLookup)).thenReturn(commcareForm);
         when(commcareDataProvider.lookup("CaseInfo", caseLookup)).thenReturn(caseInfo);
 
         handler.handle(commcareFormstub());
 
         verify(actionProxyService, never()).unscheduleJobs(anyString());
         verify(actionProxyService, never()).scheduleRunOnceJob(anyString(), anyMap(), any(DateTime.class));
     }
 
     @Test
     public void shouldNotExecuteTaskIfCaseTypeIsWrong() throws Exception {
         Map<String, String> formLookup = new HashMap<>();
         formLookup.put("id", FORM_ID_VALUE);
 
         Map<String, String> caseLookup = new HashMap<>();
         caseLookup.put("id", CASE_ID_VALUE);
 
         FormValueElement caseElement = new FormValueElement();
         caseElement.setElementName("case");
         caseElement.addAttribute(Commcare.CASE_ID, CASE_ID_VALUE);
 
         FormValueElement form = new FormValueElement();
         form.addAttribute(Commcare.NAME, "Child Visit");
         form.addFormValueElement(Commcare.CASE, caseElement);
 
         commcareForm = new CommcareForm();
         commcareForm.setForm(form);
 
         Map<String, Map<String, String>> indices = new HashMap<>();
 
         Map<String, String> parent = new HashMap<>();
         parent.put("case_id", MOTHER_CASE_ID_VALUE);
         parent.put("case_type", "parent");
 
         indices.put("parent", parent);
 
         caseInfo = new CaseInfo();
         caseInfo.setIndices(indices);
 
         when(commcareDataProvider.lookup("CommcareForm", formLookup)).thenReturn(commcareForm);
         when(commcareDataProvider.lookup("CaseInfo", caseLookup)).thenReturn(caseInfo);
 
         handler.handle(commcareFormstub());
 
         verify(actionProxyService, never()).unscheduleJobs(anyString());
         verify(actionProxyService, never()).scheduleRunOnceJob(anyString(), anyMap(), any(DateTime.class));
     }
 
     @Test
     public void shouldNotExecuteTaskIfFormNameIsWrong() throws Exception {
         Map<String, String> formLookup = new HashMap<>();
         formLookup.put("id", FORM_ID_VALUE);
 
         FormValueElement form = new FormValueElement();
         form.addAttribute(Commcare.NAME, "something");
 
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
