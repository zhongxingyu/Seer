 package cours.ulaval.glo4003.controller;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.springframework.web.servlet.ModelAndView;
 
 import cours.ulaval.glo4003.controller.model.CalendarModel;
 import cours.ulaval.glo4003.controller.model.SectionModel;
 import cours.ulaval.glo4003.domain.Course;
 import cours.ulaval.glo4003.domain.Offering;
 import cours.ulaval.glo4003.domain.Role;
 import cours.ulaval.glo4003.domain.Schedule;
 import cours.ulaval.glo4003.domain.ScheduleGenerator;
 import cours.ulaval.glo4003.domain.Section;
 import cours.ulaval.glo4003.domain.Semester;
 import cours.ulaval.glo4003.domain.TeachMode;
 import cours.ulaval.glo4003.domain.Time;
 import cours.ulaval.glo4003.domain.TimeDedicated;
 import cours.ulaval.glo4003.domain.TimeSlot;
 import cours.ulaval.glo4003.domain.TimeSlot.DayOfWeek;
 import cours.ulaval.glo4003.domain.User;
 import cours.ulaval.glo4003.domain.repository.CourseRepository;
 import cours.ulaval.glo4003.domain.repository.OfferingRepository;
 import cours.ulaval.glo4003.domain.repository.ScheduleRepository;
 import cours.ulaval.glo4003.domain.repository.UserRepository;
 
 public class ScheduleControllerTest {
 
 	private final String AN_ACRONYM = "IFT-2002";
 	private final String A_YEAR = "2012";
 	private final Semester A_SEMESTER = Semester.Automne;
 	private final String A_SCHEDULE_ID = "ScheduleId";
 	private static final String SELECTED_SCHEDULE_ID = "ID";
 	private final String A_SECTION_NRC = "86758";
 	private final String AN_IDUL = "HABBA";
 	private final String A_USERNAME = "Hello";
 
 	@Mock
 	private CourseRepository mockedCourseRepository;
 	@Mock
 	private OfferingRepository mockedOfferingRepository;
 	@Mock
 	private ScheduleRepository mockedScheduleRepository;
 	@Mock
 	private UserRepository mockedUserRepository;
 	@Mock
 	private ObjectMapper mapper;
 	@Mock
 	private ScheduleGenerator generator;
	// @Mock
	// private ConflictDetector conflictDetector;
 
 	@InjectMocks
 	private ScheduleController controller;
 
 	private Course course;
 	private List<Course> courses;
 	private Schedule schedule;
 	private User user;
 	private Principal principal;
 
 	@Before
 	public void setUp() {
 		MockitoAnnotations.initMocks(this);
 		user = new User();
 		user.setName(A_USERNAME);
 		user.addRole(Role.ROLE_Responsable);
 		user.addRole(Role.ROLE_Enseignant);
 
 		Map<String, Section> sections = new HashMap<String, Section>();
 		course = mock(Course.class);
 		courses = Arrays.asList(course);
 
 		schedule = mock(Schedule.class);
 		when(schedule.getSections()).thenReturn(sections);
 		when(schedule.getSectionsList()).thenReturn(new ArrayList<Section>(sections.values()));
 		when(schedule.getSemester()).thenReturn(Semester.Automne);
 		sections.put(A_SECTION_NRC, createSection());
 
 		principal = mock(Principal.class);
 		when(principal.getName()).thenReturn(AN_IDUL);
 
 		Offering offering = mock(Offering.class);
 
 		when(mockedOfferingRepository.findYears()).thenReturn(Arrays.asList(A_YEAR));
 		when(mockedOfferingRepository.find(A_YEAR)).thenReturn(offering);
 		when(mockedCourseRepository.findByOffering(offering)).thenReturn(courses);
 		when(mockedCourseRepository.findByAcronym(AN_ACRONYM)).thenReturn(course);
 		when(mockedScheduleRepository.findById(A_SCHEDULE_ID)).thenReturn(schedule);
 		when(mockedScheduleRepository.findById(SELECTED_SCHEDULE_ID)).thenReturn(schedule);
 		when(mockedScheduleRepository.findAll()).thenReturn(Arrays.asList(schedule));
 		when(mockedUserRepository.findByIdul(AN_IDUL)).thenReturn(user);
 		when(mockedUserRepository.findByRole(Role.ROLE_Enseignant)).thenReturn(Arrays.asList(user));
 
 	}
 
 	@Test
 	public void canInstantiateController() {
 		assertNotNull(controller);
 	}
 
 	@Test
 	public void canGetAllSchedules() {
 		ModelAndView mv = controller.schedule(principal);
 
 		assertTrue(mv.getModel().containsKey("schedules"));
 		assertTrue(mv.getModel().containsKey("statuses"));
 	}
 
 	@Test
 	public void scheduleViewListReturnsTheCorrectModelAndView() throws Exception {
 		ModelAndView mv = controller.scheduleView(A_SCHEDULE_ID, "list", principal);
 
 		assertTrue(mv.getModel().get("schedule") instanceof CalendarModel);
 		assertEquals("schedulelist", mv.getViewName());
 	}
 
 	@Test
 	public void scheduleViewCalendarReturnsTheCorrectModelAndView() throws Exception {
 		ModelAndView mv = controller.scheduleView(A_SCHEDULE_ID, "calendar", principal);
 
 		assertTrue(mv.getModel().containsKey("schedule"));
 		assertEquals("schedulecalendar", mv.getViewName());
 	}
 
 	@Test
 	public void addScheduleReturnsTheCorrectModelAndView() throws Exception {
 		ModelAndView mv = controller.addSchedule();
 
 		assertEquals(mockedOfferingRepository.findYears(), mv.getModel().get("years"));
 	}
 
 	@Test
 	public void addScheduleWithYearReturnsTheCorrectModelAndView() throws Exception {
 		ModelAndView mv = controller.addSchedule(A_YEAR, A_SEMESTER);
 
 		assertEquals(A_YEAR, mv.getModel().get("year"));
 		assertEquals(courses, mv.getModel().get("courses"));
 	}
 
 	@Test
 	public void addSectionReturnsTheCorrectModelAndView() {
 		ModelAndView mv = controller.addSection(A_SCHEDULE_ID, A_YEAR, A_SEMESTER, AN_ACRONYM);
 
 		assertEquals(AN_ACRONYM, mv.getModel().get("acronym"));
 		assertEquals(course, mv.getModel().get("course"));
 		assertEquals(A_YEAR, mv.getModel().get("year"));
 		assertEquals(A_SEMESTER, mv.getModel().get("semester"));
 		assertEquals(A_SCHEDULE_ID, mv.getModel().get("id"));
 		assertTrue(mv.getModel().containsKey("teachers"));
 	}
 
 	@Test
 	public void editSectionReturnsTheCorrectModelAndViewFromListView() {
 		ModelAndView mv = controller.editSection(A_SCHEDULE_ID, A_YEAR, A_SEMESTER, A_SECTION_NRC, "list");
 
 		verify(mockedScheduleRepository).findById(A_SCHEDULE_ID);
 		verify(schedule).getSections();
 
 		assertEquals(A_YEAR, mv.getModel().get("year"));
 		assertEquals(A_SEMESTER, mv.getModel().get("semester"));
 		assertEquals(A_SCHEDULE_ID, mv.getModel().get("id"));
 		assertTrue(mv.getModel().containsKey("section"));
 	}
 
 	@Test
 	public void canEditASectionWithinListView() throws Exception {
 		SectionModel model = mock(SectionModel.class);
 		Principal principal = mock(Principal.class);
 		when(principal.getName()).thenReturn(AN_IDUL);
 		ModelAndView mv = controller.postEditSection(A_SCHEDULE_ID, A_YEAR, A_SEMESTER, A_SECTION_NRC, "list", model, principal);
 
 		assertEquals(A_YEAR, mv.getModel().get("year"));
 		assertEquals(A_SEMESTER, mv.getModel().get("semester"));
 		assertEquals(A_SCHEDULE_ID, mv.getModel().get("id"));
 		assertTrue(mv.getModel().containsKey("courses"));
 		assertTrue(mv.getModel().containsKey("sections"));
 		verify(mockedScheduleRepository).findById(A_SCHEDULE_ID);
 		verify(schedule).delete(A_SECTION_NRC);
 		verify(schedule).add(any(Section.class));
 		verify(mockedScheduleRepository).store(schedule);
 	}
 
 	@Test
 	public void canPostASection() throws Exception {
 		SectionModel model = mock(SectionModel.class);
 		Section section = createSection();
 		when(model.convertToSection()).thenReturn(section);
 		doNothing().when(schedule).add(section);
 		Map<String, Section> sections = new HashMap<String, Section>();
 		sections.put(A_SCHEDULE_ID, section);
 		when(schedule.getSections()).thenReturn(sections);
 
 		ModelAndView mv = controller.postSection(A_SCHEDULE_ID, A_YEAR, A_SEMESTER, model);
 		verify(schedule).add(section);
 		verify(mockedScheduleRepository).store(schedule);
 
 		assertEquals(A_YEAR, mv.getModel().get("year"));
 		assertEquals(courses, mv.getModel().get("courses"));
 		assertEquals(A_SEMESTER, mv.getModel().get("semester"));
 		assertEquals(A_SCHEDULE_ID, mv.getModel().get("id"));
 		assertTrue(mv.getModel().containsKey("sections"));
 	}
 
 	@Test
 	public void canDeleteASection() throws Exception {
 		ModelAndView mv = controller.deleteSection(A_SCHEDULE_ID, A_SECTION_NRC, A_YEAR, A_SEMESTER);
 
 		verify(mockedScheduleRepository).findById(A_SCHEDULE_ID);
 		verify(schedule).delete(A_SECTION_NRC);
 		verify(mockedScheduleRepository).store(schedule);
 
 		assertEquals(A_YEAR, mv.getModel().get("year"));
 		assertEquals(courses, mv.getModel().get("courses"));
 		assertEquals(A_SEMESTER, mv.getModel().get("semester"));
 		assertEquals(A_SCHEDULE_ID, mv.getModel().get("id"));
 		assertTrue(mv.getModel().containsKey("sections"));
 		assertTrue(mv.getModel().containsKey("courses"));
 	}
 
 	@Test
 	public void canDeleteASchedule() throws Exception {
 		controller.deleteSchedule(A_SCHEDULE_ID, principal);
 
 		verify(mockedScheduleRepository).delete(A_SCHEDULE_ID);
 	}
 
 	@Test
 	public void canSelectAScheduleToReuse() throws Exception {
 		ModelAndView mv = controller.reuseSchedule(A_SCHEDULE_ID, principal);
 
 		verify(mockedScheduleRepository).findAll();
 		assertTrue(mv.getModel().containsKey("schedules"));
 		assertTrue(mv.getModel().containsKey("id"));
 	}
 
 	@Test
 	public void canReuseASelectedSchedule() throws Exception {
 		ModelAndView mv = controller.reuseSelectedSchedule(A_SCHEDULE_ID, SELECTED_SCHEDULE_ID, principal);
 
 		verify(mockedScheduleRepository).findById(A_SCHEDULE_ID);
 		verify(mockedScheduleRepository).findById(SELECTED_SCHEDULE_ID);
 		verify(schedule).copySectionsFromOtherSchedule(schedule);
 		assertTrue(mv.getModel().containsKey("schedule"));
 		assertEquals("schedulelist", mv.getViewName());
 	}
 
 	@Test
 	public void canProposeCourseSection() throws Exception {
 		String teachers = "teacher";
 		when(mapper.readValue(any(String.class), any(TypeReference.class))).thenReturn(Arrays.asList(teachers));
 		when(generator.proposeTimeSlotsForSectionForCourses(any(Section.class), any(Schedule.class))).thenReturn(new ArrayList<TimeSlot>());
 		ModelAndView mv = controller.proposeSection(A_SCHEDULE_ID, A_YEAR, A_SEMESTER, teachers, 3, AN_ACRONYM);
 
 		assertEquals(false, mv.getModel().get("isLab"));
 		assertTrue(mv.getModel().containsKey("timeSlots"));
 	}
 
 	@Test
 	public void canProposeLabSection() throws Exception {
 		String teachers = "teacher";
 		when(mapper.readValue(any(String.class), any(TypeReference.class))).thenReturn(Arrays.asList(teachers));
 		when(generator.proposeTimeSlotsForSectionForLab(any(Section.class), any(Schedule.class))).thenReturn(new ArrayList<TimeSlot>());
 		ModelAndView mv = controller.proposeLabSection(A_SCHEDULE_ID, A_YEAR, A_SEMESTER, teachers, 3, AN_ACRONYM);
 
 		assertEquals(true, mv.getModel().get("isLab"));
 		assertTrue(mv.getModel().containsKey("timeSlots"));
 	}
 
 	@Test
 	public void canAcceptSchedule() throws Exception {
 		String message = controller.acceptSchedule(A_SCHEDULE_ID, principal, "Accepted");
 
 		verify(mockedScheduleRepository).findById(A_SCHEDULE_ID);
 		verify(mockedUserRepository).findByIdul(AN_IDUL);
 		verify(mockedScheduleRepository).store(schedule);
 
 		assertEquals(message, ControllerMessages.SUCCESS);
 	}
 
 	private Section createSection() {
 		Section section = new Section();
 		section.setNrc(A_SECTION_NRC);
 		section.setTimeDedicated(new TimeDedicated(2, 3, 4));
 		section.setTeachMode(TeachMode.InCourse);
 		TimeSlot slot = new TimeSlot(new Time(9, 30), 2, DayOfWeek.THURSDAY);
 		slot.setDayOfWeek(DayOfWeek.FRIDAY);
 		section.setLabTimeSlot(slot);
 		Time time = new Time(8, 30);
 		section.setCourseTimeSlots(Arrays.asList(new TimeSlot(time, 2, DayOfWeek.TUESDAY)));
 
 		return section;
 	}
 }
