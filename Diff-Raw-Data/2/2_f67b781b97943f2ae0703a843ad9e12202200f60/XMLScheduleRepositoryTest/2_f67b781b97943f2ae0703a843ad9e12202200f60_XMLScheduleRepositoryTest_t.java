 package cours.ulaval.glo4003.persistence;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 
 import cours.ulaval.glo4003.domain.Schedule;
 import cours.ulaval.glo4003.domain.Semester;
 
 public class XMLScheduleRepositoryTest {
 
 	private static int NB_OF_SCHEDULE = 10;
 	private static String AN_ID = "anId";
 
 	@Mock
	private XMLSerializer<ScheduleXMLWrapper> mockedSerializer;
	@Mock
 	private Schedule mockedSchedule;
 	@InjectMocks
 	private XMLScheduleRepository scheduleRepo;
 
 	@Before
 	public void setUp() {
 		MockitoAnnotations.initMocks(this);
 	}
 
 	@Test
 	public void canFindAll() throws Exception {
 		addAFewSchedule();
 
 		assertTrue(scheduleRepo.findAll().size() >= NB_OF_SCHEDULE);
 	}
 
 	@Test
 	public void canFindByYear() throws Exception {
 		addTwoScheduleWithTwoDifferentYear();
 
 		assertEquals(1, scheduleRepo.findBy("2012").size());
 	}
 
 	@Test
 	public void canFindById() throws Exception {
 		Schedule schedule = mock(Schedule.class);
 		when(schedule.getId()).thenReturn(AN_ID);
 		scheduleRepo.store(schedule);
 
 		assertEquals(schedule, scheduleRepo.findById(AN_ID));
 	}
 
 	@Test
 	public void canGetId() throws Exception {
 		String id = scheduleRepo.getId("2012", Semester.Automne);
 
 		assertEquals("2012-Automne-1", id);
 	}
 
 	@Test
 	public void canStoreASchedule() throws Exception {
 		scheduleRepo.store(mockedSchedule);
 
 		assertEquals(mockedSchedule, scheduleRepo.findAll().get(0));
 	}
 
 	@Test
 	public void cannotStoreADuplicateSchedule() throws Exception {
 		when(mockedSchedule.getId()).thenReturn(AN_ID);
 		when(mockedSchedule.getYear()).thenReturn("2012");
 
 		scheduleRepo.store(mockedSchedule);
 		scheduleRepo.store(mockedSchedule);
 
 		assertEquals(1, scheduleRepo.findBy("2012").size());
 	}
 
 	@Test
 	public void canDeleteASchedule() throws Exception {
 		when(mockedSchedule.getId()).thenReturn(AN_ID);
 
 		scheduleRepo.store(mockedSchedule);
 		scheduleRepo.delete(AN_ID);
 
 		assertNull(scheduleRepo.findById(AN_ID));
 	}
 
 	@Test
 	public void doesntDeleteAnythingIfTheScheduleDoesntExist() throws Exception {
 		Map<String, Schedule> schedules = mock(Map.class);
 		when(schedules.containsKey(anyString())).thenReturn(false);
 
 		scheduleRepo.delete("uneClé");
 
 		verify(schedules, never()).remove(anyString());
 	}
 
 	private void addAFewSchedule() throws Exception {
 		for (Integer i = 0; i < NB_OF_SCHEDULE; i++) {
 			Schedule schedule = mock(Schedule.class);
 			when(schedule.getId()).thenReturn(AN_ID + i.toString());
 			scheduleRepo.store(schedule);
 		}
 	}
 
 	private void addTwoScheduleWithTwoDifferentYear() throws Exception {
 		Schedule schedule2011 = mock(Schedule.class);
 		Schedule schedule2012 = mock(Schedule.class);
 		when(schedule2011.getId()).thenReturn(AN_ID + "2011");
 		when(schedule2012.getId()).thenReturn(AN_ID + "2012");
 		when(schedule2011.getYear()).thenReturn("2011");
 		when(schedule2012.getYear()).thenReturn("2012");
 
 		scheduleRepo.store(schedule2011);
 		scheduleRepo.store(schedule2012);
 	}
 
 }
