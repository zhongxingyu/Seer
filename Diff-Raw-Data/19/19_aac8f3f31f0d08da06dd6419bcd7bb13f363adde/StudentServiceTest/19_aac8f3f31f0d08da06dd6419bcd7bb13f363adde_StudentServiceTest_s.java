 package org.sukrupa.student;
 
 import com.google.common.collect.Sets;
 import org.hamcrest.Matchers;
 import org.joda.time.LocalDate;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import static org.mockito.Matchers.argThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.sukrupa.platform.date.DateManipulation.freezeDateToMidnightOn_31_12_2010;
 import static org.sukrupa.platform.date.DateManipulation.unfreezeTime;
 import static org.sukrupa.platform.hamcrest.SchoolAdminMatchers.hasNote;
 import static org.sukrupa.student.StudentService.NUMBER_OF_STUDENTS_TO_LIST_PER_PAGE;
 
 public class StudentServiceTest {
 
     private static final String MUSIC = "Music";
     private static final String SPORT = "Sport";
     private static final String COOKING = "Cooking";
 
     private final Talent music = new Talent(MUSIC);
     private final Talent sport = new Talent(SPORT);
     private final Talent cooking = new Talent(COOKING);
 
     private final StudentSearchParameter all = new StudentSearchParameterBuilder().build();
 
     @Mock
     private StudentRepository studentRepository;
     @Mock
     private TalentRepository talentRepository;
 
     private StudentService service;
 
     @Mock
     private StudentFactory studentFactory;
 
     @Before
     public void setUp() throws Exception {
         initMocks(this);
         service = new StudentService(studentRepository, talentRepository, null, studentFactory);
         freezeDateToMidnightOn_31_12_2010();
     }
 
     @After
     public void tearDown() throws Exception {
         unfreezeTime();
     }
 
     @Test
     public void shouldAddANoteToStudent() {
         String studentId = "42";
         Note note = new Note("Fish like plankton!");
 
         when(studentRepository.findByStudentId(studentId)).thenReturn(new StudentBuilder().build());
 
         service.addNoteFor(studentId, note.getMessage());
 
         verify(studentRepository).put(argThat(hasNote(note)));
     }
 
     @Test
     public void shouldRetrievePageOneOfOne() {
         when(studentRepository.getCountBasedOn(org.mockito.Matchers.<StudentSearchParameter>anyObject())).thenReturn(NUMBER_OF_STUDENTS_TO_LIST_PER_PAGE);
         assertThat(service.getPage(all, 1, "").isNextEnabled(), is(false));
         Mockito.verify(studentRepository).findBySearchParameter(all, 0, NUMBER_OF_STUDENTS_TO_LIST_PER_PAGE);
     }
 
     @Test
     public void shouldRetrievePageOneOfMultiple() {
         when(studentRepository.getCountBasedOn(org.mockito.Matchers.<StudentSearchParameter>anyObject())).thenReturn(NUMBER_OF_STUDENTS_TO_LIST_PER_PAGE + 1);
         assertThat(service.getPage(all, 1, "").isNextEnabled(), is(true));
         Mockito.verify(studentRepository).findBySearchParameter(all, 0, NUMBER_OF_STUDENTS_TO_LIST_PER_PAGE);
     }
 
     @Test
     public void shouldRetrievePageTwoOfTwo() {
         when(studentRepository.getCountBasedOn(org.mockito.Matchers.<StudentSearchParameter>anyObject())).thenReturn(NUMBER_OF_STUDENTS_TO_LIST_PER_PAGE + 1);
         assertThat(service.getPage(all, 2, "page=2").isNextEnabled(), is(false));
         Mockito.verify(studentRepository).findBySearchParameter(all, NUMBER_OF_STUDENTS_TO_LIST_PER_PAGE, NUMBER_OF_STUDENTS_TO_LIST_PER_PAGE);
     }
 
     @Test
     public void shouldIncrementClassOfEachStudentByOne() {
         // given
         List<Student> students = new ArrayList<Student>();
 
         Student sahil = new StudentBuilder().name("sahil").studentClass("1 Std").build();
         students.add(sahil);
 
         Student mark = new StudentBuilder().name("mark").studentClass("1 Std").build();
         students.add(mark);
 
         when(studentRepository.findAll()).thenReturn(students);
 
         // when
         service.promoteStudentsToNextClass();
 
         // then
         Student promotedSahil = new StudentBuilder().name("sahil").studentClass("2 Std").build();
         Student promotedMark = new StudentBuilder().name("mark").studentClass("2 Std").build();
 
         Mockito.verify(studentRepository).put(promotedSahil);
         Mockito.verify(studentRepository).put(promotedMark);
     }
 
     @Test
     public void shouldUpdateStudent() {
         Student philOld = new StudentBuilder().studentId("12345")
                 .name("Phil").studentClass("1 Std").gender("Male").religion("Hindu").area("Bhuvaneshwari Slum")
                 .caste("SC").subCaste("AD").talents(Sets.newHashSet(cooking, sport)).dateOfBirth(new LocalDate(2000, 05, 03)).status(StudentStatus.EXISTING_STUDENT).build();
         Student philNew = new StudentBuilder().studentId("12345")
                 .name("Philippa").studentClass("2 Std").gender("Female").religion("Catholic").area("Chamundi Nagar")
                 .caste("ST").subCaste("AK").talents(Sets.newHashSet(music, sport)).dateOfBirth(new LocalDate(2000, 02, 03)).status(StudentStatus.EXISTING_STUDENT).build();
         when(studentRepository.findByStudentId(philOld.getStudentId())).thenReturn(philOld);
         when(studentRepository.update(philNew)).thenReturn(philNew);
         when(talentRepository.findTalents(Sets.newHashSet(MUSIC, SPORT))).thenReturn(Sets.newHashSet(music, sport));
 
         StudentProfileForm updateParameters = new StudentCreateOrUpdateParameterBuilder().studentId(philOld.getStudentId())
                 .area("Chamundi Nagar")
                 .caste("ST")
                 .subCaste("AK")
                 .religion("Catholic")
                 .name("Philippa")
                 .gender("Female")
                 .studentClass("2 Std")
                 .dateOfBirth("03-02-2000")
                 .talents(Sets.<String>newHashSet(MUSIC, SPORT)).status(StudentStatus.EXISTING_STUDENT).build();
         Student updatedStudent = service.update(updateParameters);
         assertThat(updatedStudent, Matchers.is(philNew));
     }
 
     @Test
     public void shouldUpdateStudentStatus() {
         Student philOld = new StudentBuilder().studentId("12345")
                 .name("Phil").studentClass("1 Std").gender("Male").religion("Hindu").area("Bhuvaneshwari Slum")
                 .caste("SC").subCaste("AD").talents(Sets.newHashSet(cooking, sport)).dateOfBirth(new LocalDate(2000, 05, 03)).status(StudentStatus.EXISTING_STUDENT).build();
         Student philNew = new StudentBuilder().studentId("12345")
                 .name("Philippa").studentClass("2 Std").gender("Female").religion("Catholic").area("Chamundi Nagar")
                 .caste("ST").subCaste("AK").talents(Sets.newHashSet(music, sport)).dateOfBirth(new LocalDate(2000, 02, 03)).status(StudentStatus.ALUMNI).build();
         when(studentRepository.findByStudentId(philOld.getStudentId())).thenReturn(philOld);
         when(studentRepository.update(philNew)).thenReturn(philNew);
         when(talentRepository.findTalents(Sets.newHashSet(MUSIC, SPORT))).thenReturn(Sets.newHashSet(music, sport));
 
         StudentProfileForm updateParameters = new StudentCreateOrUpdateParameterBuilder().studentId(philOld.getStudentId())
                 .area("Chamundi Nagar")
                 .caste("ST")
                 .subCaste("AK")
                 .religion("Catholic")
                 .name("Philippa")
                 .gender("Female")
                 .studentClass("2 Std")
                 .dateOfBirth("03-02-2000")
                 .talents(Sets.<String>newHashSet(MUSIC, SPORT))
                 .status(StudentStatus.ALUMNI).build();
         Student updatedStudent = service.update(updateParameters);
         assertThat(updatedStudent, Matchers.is(philNew));
     }
 
     @Test
     public void shouldFailToUpdateNonexistantStudent() {
         assertThat(service.update(new StudentCreateOrUpdateParameterBuilder().build()), Matchers.<Object>nullValue());
     }
 
     @Test
     public void shouldCreateStudent() {
         StudentProfileForm studentParam = new StudentProfileForm();
         studentParam.setStudentId("SK20091001");
         studentParam.setName("Yael");
         studentParam.setDateOfBirth("06-03-1982");
         studentParam.setGender("Female");
 
         Student expectedStudent = mock(Student.class);
         when(studentFactory.create(studentParam.getStudentId(),
                 studentParam.getName(),
                 studentParam.getDateOfBirth(),
                 studentParam.getGender())).thenReturn(expectedStudent);
 
         Student student = service.create(studentParam);
 
         verify(studentRepository).put(expectedStudent);
         assertEquals(expectedStudent, student);
     }
 
 
 }
