 package org.openengsb.opencit.ui.web;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.Is.is;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.wicket.extensions.wizard.WizardButton;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
 import org.apache.wicket.util.tester.FormTester;
 import org.apache.wicket.util.tester.WicketTester;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Matchers;
 import org.mockito.Mockito;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.openengsb.core.common.Domain;
 import org.openengsb.core.common.ServiceManager;
 import org.openengsb.core.common.context.ContextCurrentService;
 import org.openengsb.core.common.descriptor.AttributeDefinition;
 import org.openengsb.core.common.descriptor.ServiceDescriptor;
 import org.openengsb.core.common.l10n.LocalizableString;
 import org.openengsb.core.common.service.DomainService;
 import org.openengsb.core.common.validation.FieldValidator;
 import org.openengsb.core.common.validation.FormValidator;
 import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
 import org.openengsb.core.common.validation.SingleAttributeValidationResult;
 import org.openengsb.domain.notification.NotificationDomain;
 import org.openengsb.domain.report.ReportDomain;
 import org.openengsb.domain.scm.ScmDomain;
 import org.openengsb.opencit.core.projectmanager.ProjectManager;
 import org.openengsb.opencit.core.projectmanager.model.Project;
 import org.openengsb.ui.web.editor.ServiceEditorPanel;
 
 public class ProjectWizardTest extends AbstractCitPageTest {
 
     private WicketTester tester;
     private ProjectManager projectManager;
     private ContextCurrentService contextSerice;
     private DomainService domainService;
 
     @Before
     public void setUp() {
         Locale.setDefault(new Locale("en"));
         tester = getTester();
     }
 
     @Override
     protected Map<String, Object> getBeansForAppContextAsMap() {
         Map<String, Object> mockedBeansMap = new HashMap<String, Object>();
         contextSerice = mock(ContextCurrentService.class);
         projectManager = mock(ProjectManager.class);
         domainService = mock(DomainService.class);
         projectManager = Mockito.mock(ProjectManager.class);
         mockedBeansMap.put("contextCurrentService", contextSerice);
         mockedBeansMap.put("domainService", domainService);
         mockedBeansMap.put("projectManager", projectManager);
         mockedBeansMap.put("reportDomain", mock(ReportDomain.class));
         return mockedBeansMap;
     }
 
     @Test
     public void testFirstStep_ShouldCreateNewProject() {
         tester.startPage(new Index());
         tester.clickLink("newProject");
         tester.assertContains("newProject.title");
         FormTester formTester = tester.newFormTester("wizard:form");
         formTester.setValue("view:project.id", "testID");
        formTester.setValue("view:project.notificationRecipient", "someMail");
 
         nextStep(formTester);
         ProjectWizard wizard = (ProjectWizard) tester.getComponentFromLastRenderedPage("wizard");
         Project project = wizard.getProject();
         assertThat(project.getId(), is("testID"));
     }
 
     @Test
     @SuppressWarnings("rawtypes")
     public void testChoseDomainToSetup_ShouldShowDropDownWithSCMNotificationBuildTestDeployReport() {
         mockSetupForWizard();
         tester.startPage(new Index());
         tester.clickLink("newProject");
         tester.assertContains("newProject.title");
         FormTester formTester = tester.newFormTester("wizard:form");
         formTester.setValue("view:project.id", "testID");
 
         // Step to domain selection
         nextStep(formTester);
 
         Label newHeader = (Label) tester.getComponentFromLastRenderedPage("wizard:form:header:title");
         String o = newHeader.getDefaultModelObject().toString();
         assertThat(o, is("Domain selection"));
 
         DropDownChoice ddc = (DropDownChoice) tester
             .getComponentFromLastRenderedPage("wizard:form:view:domainDropDown");
         List choices = ddc.getChoices();
         // should contain :
         // scm, build, test, deploy, notification, report
         assertThat(choices.size(), is(6));
     }
 
     @Test
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public void testSCMStep_ShouldShowDropDownWithPossibleSCM() {
         mockSetupForWizard();
         tester.startPage(new Index());
         tester.clickLink("newProject");
         tester.assertContains("newProject.title");
         FormTester formTester = tester.newFormTester("wizard:form");
         formTester.setValue("view:project.id", "testID");
 
         // Step to domain selection
         nextStep(formTester);
 
         Label newHeader = (Label) tester.getComponentFromLastRenderedPage("wizard:form:header:title");
         String o = newHeader.getDefaultModelObject().toString();
         assertThat(o, is("Domain selection"));
 
         DropDownChoice ddc = (DropDownChoice) tester
             .getComponentFromLastRenderedPage("wizard:form:view:domainDropDown");
 
         formTester = tester.newFormTester("wizard:form");
         formTester.select("view:domainDropDown", 0); // should be scm
 
         // Step to SCM selection
         nextStep(formTester);
 
         formTester = tester.newFormTester("wizard:form");
         newHeader = (Label) tester.getComponentFromLastRenderedPage("wizard:form:header:title");
         o = newHeader.getDefaultModelObject().toString();
         assertThat(o, is("Service Selection"));
 
         ddc = (DropDownChoice) tester.getComponentFromLastRenderedPage("wizard:form:view:serviceDescriptor");
         List<String> choices = ddc.getChoices();
         assertThat(choices.size(), is(1));
         assertThat(choices.get(0), is("SCMMDomain"));
     }
 
     @Test
     public void testSCMSetupStep_ShouldShowSomeInputFieldsForSCMSetup() {
         mockSetupForWizard();
         tester.startPage(new Index());
         tester.clickLink("newProject");
         tester.assertContains("newProject.title");
         FormTester formTester = tester.newFormTester("wizard:form");
         formTester.setValue("view:project.id", "testID");
 
         // Step to domain selection
         nextStep(formTester);
 
         formTester = tester.newFormTester("wizard:form");
         formTester.select("view:domainDropDown", 0); // should be scm
 
         // Step to SCM selection
         nextStep(formTester);
 
         formTester = tester.newFormTester("wizard:form");
         formTester.select("view:serviceDescriptor", 0); // should be git
 
         // step to attribute setup
         nextStep(formTester);
         formTester = tester.newFormTester("wizard:form");
 
         Label newHeader = (Label) tester.getComponentFromLastRenderedPage("wizard:form:header:title");
         assertThat(newHeader.getDefaultModelObjectAsString(), is("Attribute definition"));
 
         SimpleFormComponentLabel attributName = (SimpleFormComponentLabel) tester
             .getComponentFromLastRenderedPage("wizard:form:view:editor:form:fields:2:row:name");
         assertThat(attributName.getDefaultModelObjectAsString(), is("attName"));
         FormTester attributeFormTester = tester.newFormTester("wizard:form:view:editor:form");
         attributeFormTester.setValue("fields:1:row:field", "ID1");
         attributeFormTester.setValue("fields:2:row:field", "attribute1Value1");
         attributeFormTester.setValue("validate", false);
         // step next
         ServiceEditorPanel comp = (ServiceEditorPanel) tester
             .getComponentFromLastRenderedPage("wizard:form:view:editor");
         comp.onSubmit();
         nextStep(formTester);
         newHeader = (Label) tester.getComponentFromLastRenderedPage("wizard:form:header:title");
         assertThat(newHeader.getDefaultModelObjectAsString(), is("Domain selection"));
     }
 
     private void nextStep(FormTester formTester) {
 
         String nextFulltBtnPath = "wizard:form:buttons:next";
         tester.assertComponent(nextFulltBtnPath, WizardButton.class);
         WizardButton nextButton = (WizardButton) tester.getComponentFromLastRenderedPage(nextFulltBtnPath);
         formTester.submit();
         nextButton.onSubmit();
     }
 
     private void mockSetupForWizard() {
         ServiceManager scmServiceManager = mock(ServiceManager.class);
         ServiceManager notificationServiceManager = mock(ServiceManager.class);
 
         List<ServiceManager> scmManagers = new ArrayList<ServiceManager>();
         scmManagers.add(scmServiceManager);
 
         List<ServiceManager> notificationManagers = new ArrayList<ServiceManager>();
         notificationManagers.add(scmServiceManager);
 
         ServiceDescriptor scmDescriptor = mockingSetupForConnector("SCM", ScmDomain.class);
         ServiceDescriptor notificationDescriptor = mockingSetupForConnector("Notification", NotificationDomain.class);
         MultipleAttributeValidationResult multipleattru = mock(MultipleAttributeValidationResult.class);
         when(multipleattru.isValid()).thenReturn(true);
         when(scmServiceManager.update(any(String.class), Matchers.<Map<String, String>> any()))
             .thenReturn(multipleattru);
         when(scmServiceManager.getDescriptor()).thenReturn(scmDescriptor);
         when(notificationServiceManager.getDescriptor()).thenReturn(notificationDescriptor);
         when(domainService.serviceManagersForDomain(ScmDomain.class)).thenReturn(scmManagers);
         when(domainService.serviceManagersForDomain(NotificationDomain.class)).thenReturn(notificationManagers);
     }
 
     @SuppressWarnings("rawtypes")
     private ServiceDescriptor mockingSetupForConnector(String type, final Class<? extends Domain> scmDomainClass) {
 
         ServiceDescriptor serviceDescriptor = mock(ServiceDescriptor.class);
         LocalizableString localizableString = mock(LocalizableString.class);
         when(localizableString.getString(any(Locale.class))).thenReturn(type + "MDomain");
         when(serviceDescriptor.getName()).thenReturn(localizableString);
         FormValidator formValidator = mock(FormValidator.class);
         List<String> validateFields = new ArrayList<String>();
         validateFields.add("attributeId");
         when(formValidator.fieldsToValidate()).thenReturn(validateFields);
         when(serviceDescriptor.getFormValidator()).thenReturn(formValidator);
         MultipleAttributeValidationResult validate = mock(MultipleAttributeValidationResult.class);
         when(validate.isValid()).thenReturn(true);
 
         when(formValidator.validate(Mockito.<Map<String, String>> any())).thenReturn(validate);
         LocalizableString description = mock(LocalizableString.class);
         when(description.getString(any(Locale.class))).thenReturn(type + " Description");
         when(serviceDescriptor.getDescription()).thenReturn(description);
         List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();
         AttributeDefinition attribute = mock(AttributeDefinition.class);
         LocalizableString attLocalizer = mock(LocalizableString.class);
         when(attLocalizer.getString(any(Locale.class))).thenReturn("attName");
         when(attribute.getId()).thenReturn("attributeId");
         when(attribute.getName()).thenReturn(attLocalizer);
         FieldValidator attributeValidator = mock(FieldValidator.class);
         SingleAttributeValidationResult attVRes = mock(SingleAttributeValidationResult.class);
         when(attVRes.isValid()).thenReturn(true);
         when(attributeValidator.validate(any(String.class))).thenReturn(attVRes);
         when(attribute.getValidator()).thenReturn(attributeValidator);
         attributes.add(attribute);
         when(serviceDescriptor.getAttributes()).thenReturn(attributes);
 
         when(serviceDescriptor.getServiceType()).thenAnswer(new Answer<Class>() {
             @Override
             public Class answer(InvocationOnMock invocation) throws Throwable {
                 return scmDomainClass;
             }
         });
         return serviceDescriptor;
     }
 
 }
