 package org.sukrupa.app.students;
 
 import org.hamcrest.Matchers;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.springframework.validation.Errors;
 import org.sukrupa.app.services.EmailService;
 import org.sukrupa.student.*;
 
 import javax.mail.MessagingException;
 import javax.servlet.http.HttpServletRequest;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static java.util.Arrays.asList;
 import static junit.framework.Assert.assertNotNull;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.hasKey;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.sukrupa.platform.hamcrest.CollectionMatchers.hasEntry;
 
 public class StudentsControllerTest {
 
 
     @Mock
     private StudentService service;
 
     @Mock
     private StudentProfile studentProfile;
 
     @Mock
     private EmailService emailService;
 
     private StudentsController studentController;
 
     private HashMap<String, Object> studentModel = new HashMap<String, Object>();
     private Student pat = new StudentBuilder().name("sahil").studentClass("Nursery").build();
 
     private FakeStudentValidator studentValidator;
 
     @Before
     public void setUp() throws Exception {
         initMocks(this);
         studentValidator = new FakeStudentValidator();
         studentController = new StudentsController(service, studentValidator, emailService);
     }
 
     @Test
     public void shouldPopulateModelWithAStudent() {
         when(service.load("123")).thenReturn(pat);
         studentController.view("123", false, studentModel);
         assertThat((Student) studentModel.get("student"), is(pat));
     }
 
     @Test
     public void shouldPickStudentViewForDisplayingSingleStudent() {
         when(service.load("123")).thenReturn(pat);
         assertThat(studentController.view("123", false, studentModel), is("students/view"));
     }
 
     @Test
     public void shouldDisplayErrorWhenAskedForInvalidStudentID() {
         assertThat(studentController.view("0987ihuyi", false, studentModel), is("students/viewFailed"));
     }
 
     @Test
     public void shouldDirectToNewStudentForm() {
         assertThat(studentController.newStudent(studentModel), is("students/create"));
     }
 
     @Test
     public void shouldDisplayResultsWhenSearchingBySponsor() {
         StudentSearchParameter searchParam = mock(StudentSearchParameter.class);
         HttpServletRequest request = mock(HttpServletRequest.class);
         StudentListPage students = mock(StudentListPage.class);
         List<String> validCriteria = asList("someCriteria");
         Student student = mock(Student.class);
 
         when(searchParam.getValidCriteria()).thenReturn(validCriteria);
         when(request.getQueryString()).thenReturn("TestQueryString");
         when(service.getPage(searchParam, 23, "TestQueryString")).thenReturn(students);
         when(students.getStudents()).thenReturn(asList(student));
 
         String view = studentController.listForStudentsBySponsor(23, searchParam, studentModel, request);
 
         assertThat(view, is("students/listsponsorsearch"));
         assertThat(studentModel.get("page"), is((Object) students));
         assertThat(studentModel.get("searchCriteria"), is((Object) validCriteria));
     }
 
     @Test
     public void shouldDisplayEmptyResultsPageWhenNothingFoundWhileSearchingBySponsor() {
         StudentSearchParameter searchParam = mock(StudentSearchParameter.class);
         HttpServletRequest request = mock(HttpServletRequest.class);
         StudentListPage students = mock(StudentListPage.class);
 
         when(service.getPage(any(StudentSearchParameter.class), anyInt(), anyString())).thenReturn(students);
         when(students.getStudents()).thenReturn(new ArrayList<Student>());
 
         String view = studentController.listForStudentsBySponsor(23, searchParam, studentModel, request);
 
         assertThat(view, is("students/listsponsorempty"));
         assertThat(studentModel, hasKey("searchCriteria"));
     }
 
     @Test
     public void shouldDirectToPublicStudentProfilePage() {
         Student student = mock(Student.class);
         when(service.load("123")).thenReturn(student);
 
         assertThat(studentController.publicStudentProfile("123", studentModel), is("students/profileView"));
         assertThat(studentModel, hasEntry("student", student));
     }
 
     @Test
     public void shouldCreateANewStudent() {
         StudentForm studentToCreate = new StudentForm();
         studentToCreate.setDateOfBirth("11-10-1982");
 
         Student studentThatGetsCreated = new Student("SK111", "", "01-01-2001", "Male");
         when(service.create(any(StudentForm.class))).thenReturn(studentThatGetsCreated);
 
         String result = studentController.create(studentToCreate, null);
 
         assertThat(result, is("redirect:/students/SK111/edit"));
     }
 
     @Test
     public void shouldAddNameErrorIfTheUserDoesNotEnterAName() {
         studentValidator.addErrorTo("name");
         Map<String, Object> model = new HashMap<String, Object>();
         StudentForm userDidNotEnterName = mock(StudentForm.class);
 
         studentController.create(userDidNotEnterName, model);
 
         assertNotNull(model.get("nameError"));
     }
 
     @Test
     public void createShouldShowErrorForGenderIfNotSelected() {
         studentValidator.addErrorTo("gender");
         Map<String, Object> model = new HashMap<String, Object>();
         StudentForm userWithoutGender = mock(StudentForm.class);
 
         studentController.create(userWithoutGender, model);
         assertNotNull(model.get("genderError"));
     }
 
     @Test
     public void shouldDefineIfStudentIsActiveOnViewStudent() throws Exception {
         Map<String, Object> model = new HashMap<String, Object>();
         Student student = mock(Student.class);
         when(service.load("id")).thenReturn(student);
         when(student.getStatus()).thenReturn(StudentStatus.EXISTING_STUDENT);
 
         studentController.view("id", false, model);
 
         assertThat((String) model.get("statusType"), is("existing"));
     }
 
     @Test
     public void shouldDefineIfStudentIsInactiveOnViewStudent() throws Exception {
         Map<String, Object> model = new HashMap<String, Object>();
         Student student = mock(Student.class);
         when(service.load("id")).thenReturn(student);
         when(student.getStatus()).thenReturn(StudentStatus.DROPOUT);
 
         studentController.view("id", false, model);
 
         assertThat((String) model.get("statusType"), is("dropout"));
     }
 
     @Test
     public void shouldDefineIfStudentStatusIsAlumniOrNotSetOnViewStudent() throws Exception {
         Map<String, Object> model = new HashMap<String, Object>();
         Student student = mock(Student.class);
         when(service.load("id")).thenReturn(student);
         when(student.getStatus()).thenReturn(StudentStatus.ALUMNI);
 
         studentController.view("id", false, model);
 
         assertThat((String) model.get("statusType"), is("alumni"));
     }
 
     @Test
     public void shouldReturnDefaultIfStatusIsNotDefinedOnViewStudent() throws Exception {
         Map<String, Object> model = new HashMap<String, Object>();
         Student student = mock(Student.class);
         when(service.load("id")).thenReturn(student);
         when(student.getStatus()).thenReturn(null);
 
         studentController.view("id", false, model);
 
         assertThat((String) model.get("statusType"), is("default"));
     }
 
    @Test
    public void shouldDisplayThankyouPageWhenEmailHasBeenSent() throws MessagingException {
        Map<String, Object> model = new HashMap<String, Object>();
        String htmlString = "<html><body>A Html Message</body></html>";
        String email = "me@mydomain.com";
        String subject = "Testing Send Profile View";
        when(studentProfile.composeHtmlMessage()).thenReturn(htmlString);
        when(emailService.sendEmail( anyString(), anyString(), anyString())).thenReturn(true);
 
        String redirectPath = studentController.sendProfileView(email, subject, studentProfile,  model);
 
        assertThat(model.get("errorMessage").toString(), is(""));
       assertThat(redirectPath, is("/students/thankyou"));
    }
 
     @Test
     public void shouldDisplayAnErrorMessageWhenEmailSendingFailed() throws MessagingException {
        Map<String, Object> model = new HashMap<String, Object>();
        String htmlString = "<html><body>A Html Message</body></html>";
        String email = "me@mydomain.com";
        String subject = "Testing Send Profile View";
        when(studentProfile.composeHtmlMessage()).thenReturn(htmlString);
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(false);
        studentController.sendProfileView(email, subject, studentProfile, model);
        assertThat(model.get("errorMessage").toString(), is("Error sending email!"));
     }
 
 
     private class FakeStudentValidator extends StudentValidator {
         private List<String> errorFields;
 
         public FakeStudentValidator() {
             super(null);
             errorFields = new ArrayList<String>();
         }
 
         @Override
         public void validate(Object target, Errors errors) {
             for (String field : errorFields) {
                 errors.rejectValue(field, "", "made up error");
             }
         }
 
         public void addErrorTo(String field) {
             errorFields.add(field);
         }
     }
 }
