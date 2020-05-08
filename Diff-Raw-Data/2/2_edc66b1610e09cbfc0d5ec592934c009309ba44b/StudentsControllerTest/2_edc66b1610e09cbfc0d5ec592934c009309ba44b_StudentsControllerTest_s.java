 package org.sukrupa.app.students;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.springframework.validation.Errors;
 import org.sukrupa.student.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertNull;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class StudentsControllerTest {
 
 
     @Mock
     private StudentService service;
 
     private StudentsController controller;
 
     private HashMap<String, Object> studentModel = new HashMap<String, Object>();
     private Student pat = new StudentBuilder().name("sahil").studentClass("Nursery").build();
 
     private FakeStudentValidator studentValidator;
 
     @Before
     public void setUp() throws Exception {
         initMocks(this);
         studentValidator = new FakeStudentValidator();
         controller = new StudentsController(service, studentValidator);
     }
 
     @Test
     public void shouldPopulateModelWithAStudent() {
         when(service.load("123")).thenReturn(pat);
         controller.view("123", false, studentModel);
         assertThat((Student)studentModel.get("student"),is(pat));
     }
 
     @Test
     public void shouldPickStudentViewForDisplayingSingleStudent() {
 	    when(service.load("123")).thenReturn(pat);
         assertThat(controller.view("123", false, studentModel),is("students/view"));
     }
 
     @Test
     public void shouldDisplayingErrorWhenAskedForInvalidStudentID() {
         assertThat(controller.view("0987ihuyi", false, studentModel),is("students/viewFailed"));
     }
 
     @Test
     public void shouldDirectToNewStudentForm() {
         assertThat(controller.newStudent(studentModel), is("students/create"));
     }
 //    @Test
 //    public void shouldDirectToSearchStudentBySponsorForm() {
//        assertThat(controller.(studentModel), is("students/searchStudentsBySponsor"));
 //    }
 
     @Test
     public void shouldCreateANewStudent () {
         StudentForm studentToCreate = new StudentForm();
         studentToCreate.setDateOfBirth("11-10-1982");
 
         Student studentThatGetsCreated = new Student("SK111","", "01-01-2001", "Male");
         when(service.create(any(StudentForm.class))).thenReturn(studentThatGetsCreated);
 
         String result = controller.create(studentToCreate, null);
 
         assertThat(result, is("redirect:/students/SK111/edit"));
     }
 
     @Test
     public void shouldAddNameErrorIfTheUserDoesNotEnterAName() {
         studentValidator.addErrorTo("name");
         Map<String, Object> model = new HashMap<String, Object>();
         StudentForm userDidNotEnterName = mock(StudentForm.class);
 
         controller.create(userDidNotEnterName, model);
 
         assertNotNull(model.get("nameError"));
     }
 
     @Test
     public void createShouldShowErrorForGenderIfNotSelected() {
         studentValidator.addErrorTo("gender");
         Map<String,Object> model = new HashMap<String, Object>();
         StudentForm userWithoutGender = mock(StudentForm.class);
 
         controller.create(userWithoutGender, model);
         assertNotNull(model.get("genderError"));
     }
 
     @Test
     public void shouldDefineIfStudentIsActiveOnViewStudent() throws Exception {
         Map<String,Object> model = new HashMap<String, Object>();
         Student student = mock(Student.class);
         when(service.load("id")).thenReturn(student);
         when(student.getStatus()).thenReturn(StudentStatus.EXISTING_STUDENT);
 
         controller.view("id", false, model);
 
         assertThat((String) model.get("statusType"), is("existing"));
     }
 
     @Test
     public void shouldDefineIfStudentIsInactiveOnViewStudent() throws Exception {
         Map<String, Object> model = new HashMap<String, Object>();
         Student student = mock(Student.class);
         when(service.load("id")).thenReturn(student);
         when(student.getStatus()).thenReturn(StudentStatus.DROPOUT);
 
         controller.view("id", false, model);
 
         assertThat((String) model.get("statusType"), is("dropout"));
     }
 
     @Test
     public void shouldDefineIfStudentStatusIsAlumniOrNotSetOnViewStudent() throws Exception {
         Map<String, Object> model = new HashMap<String, Object>();
         Student student = mock(Student.class);
         when(service.load("id")).thenReturn(student);
         when(student.getStatus()).thenReturn(StudentStatus.ALUMNI);
 
         controller.view("id", false, model);
 
         assertThat((String) model.get("statusType"), is("alumni"));
     }
 
     @Test
     public void shouldReturnDefaultIfStatusIsNotDefinedOnViewStudent() throws Exception {
         Map<String, Object> model = new HashMap<String, Object>();
         Student student = mock(Student.class);
         when(service.load("id")).thenReturn(student);
         when(student.getStatus()).thenReturn(null);
 
         controller.view("id", false, model);
 
         assertThat((String) model.get("statusType"), is("default"));
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
