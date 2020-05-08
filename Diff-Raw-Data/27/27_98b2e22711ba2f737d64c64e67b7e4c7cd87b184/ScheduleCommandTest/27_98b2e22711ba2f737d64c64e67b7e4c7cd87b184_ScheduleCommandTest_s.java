 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Occurred;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createParticipant;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createStudySite;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createScheduledEvent;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.addEvents;
 import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledEventDao;
 import edu.northwestern.bioinformatics.studycalendar.service.ParticipantCoordinatorDashboardService;
 
 import java.util.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 import static org.easymock.EasyMock.expect;
 
 
 public class ScheduleCommandTest extends StudyCalendarTestCase {
     private User user;
     private String userName;
     private UserDao userDao;
     private ScheduledEventDao scheduledEventDao;
 
     private ScheduleCommand command = new ScheduleCommand();
     private ParticipantCoordinatorDashboardService paService;
 
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
 
         user = new User();
         userName = "USER NAME";
         user.setName(userName);
         userDao = registerDaoMockFor(UserDao.class);
         userDao = registerMockFor(UserDao.class);
         scheduledEventDao = registerMockFor(ScheduledEventDao.class);
 
         command.setScheduledEventDao(scheduledEventDao);
         command.setUserDao(userDao);
         command.setUser(user);
         command.setToDate(3);
 
         paService = new ParticipantCoordinatorDashboardService();
         paService.setScheduledEventDao(scheduledEventDao);
 
     }
 
     public void testShiftStartDayByNumberOfDays() throws Exception {
         Date startDate = new Date();
         Integer numberOfDaysToShift = 3;
         Calendar rightNow = Calendar.getInstance();
         rightNow.add(Calendar.DATE, numberOfDaysToShift);
         Date expectedDate = rightNow.getTime();
         DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
         String dateString = df.format(expectedDate);
         expectedDate = df.parse(dateString);
         replayMocks();
         Date actualDate = paService.shiftStartDayByNumberOfDays(startDate, numberOfDaysToShift);
         verifyMocks();
         assertEquals("Expected and Actual Dates are different ", expectedDate, actualDate);
     }
 
     public void testFormatDateToString() throws Exception {
         Calendar rightNow = Calendar.getInstance(); 
         DateFormat df = new SimpleDateFormat("MM/dd");
         String expectedDateString = df.format(new Date());
         replayMocks();
         String actualDateString = paService.formatDateToString(rightNow.getTime());
         verifyMocks();
         assertEquals("Expected and Actual formats are not equals ", expectedDateString, actualDateString);
     }
 
     public void testGetMapOfCurrentEvents() throws Exception {
         Participant participant = setId(11, createParticipant("Fred", "Jones"));
         StudySite studySite = setId(14, createStudySite(null, null));
         StudyParticipantAssignment assignment = new StudyParticipantAssignment();
         ScheduledCalendar calendar;
 
         ScheduledEvent e1, e2, e3;
 
         calendar = setId(6, new ScheduledCalendar());
         calendar.addArm(new ScheduledArm());
         calendar.addArm(new ScheduledArm());
 
         Calendar c = Calendar.getInstance();
         int day = c.get(Calendar.DAY_OF_MONTH);
         int month = c.get(Calendar.MONTH);
         int year = c.get(Calendar.YEAR);
 
         e1 =  createScheduledEvent("C", year, month, day);
         e2 =  createScheduledEvent("O", year, month, day+1);
         e3 =  createScheduledEvent("S", year, month, day+2);
 
         addEvents(calendar.getScheduledArms().get(0), e1, e2, e3);
 
         assignment.setParticipant(participant);
         assignment.setStudySite(studySite);
         assignment.setStartDateEpoch(new Date());
         assignment.setScheduledCalendar(calendar);
         List<StudyParticipantAssignment> studyParticipantAssignment = new ArrayList<StudyParticipantAssignment>();
         studyParticipantAssignment.add(assignment);
 
         Collection<ScheduledEvent> events = new ArrayList<ScheduledEvent>();
         events.add(e1);
         Date startDate = new Date();
         Date tempStartDateOne = paService.shiftStartDayByNumberOfDays(startDate, 0);
         expect(scheduledEventDao.getEventsByDate(calendar, tempStartDateOne, tempStartDateOne)).andReturn(events);
 
         Collection<ScheduledEvent> eventsTwo = new ArrayList<ScheduledEvent>();
         eventsTwo.add(e2);
         Date tempStartDateTwo = paService.shiftStartDayByNumberOfDays(startDate, 1);
         expect(scheduledEventDao.getEventsByDate(calendar, tempStartDateTwo, tempStartDateTwo)).andReturn(eventsTwo);
 
         Collection<ScheduledEvent> eventsThree = new ArrayList<ScheduledEvent>();
         eventsThree.add(e3);
         Date tempStartDateThree = paService.shiftStartDayByNumberOfDays(startDate, 2);
         expect(scheduledEventDao.getEventsByDate(calendar, tempStartDateThree, tempStartDateThree)).andReturn(eventsThree);
 
         replayMocks();
         Map<String, Object> map = paService.getMapOfCurrentEvents(studyParticipantAssignment, 3);
         verifyMocks();
 
         assertNotNull("Map is null", map);
         assertEquals("Map size is incorrect", 3, map.size());
 
         Set<String> keys = map.keySet();
         Date today = new Date();
         Date todayPlusOne = paService.shiftStartDayByNumberOfDays(today, 1);
         Date todayPlusTwo = paService.shiftStartDayByNumberOfDays(today, 2);
 
         String todayKey = paService.formatDateToString(today) + " - " + paService.convertDateKeyToString(today);
         String todayPlusOneKey = paService.formatDateToString(todayPlusOne) + " - " + paService.convertDateKeyToString(todayPlusOne);
         String todayPlusTwoKey = paService.formatDateToString(todayPlusTwo) + " - " + paService.convertDateKeyToString(todayPlusTwo);
 
         assertTrue("Keys don't contain today's date", keys.contains(todayKey));
         assertTrue("Keys don't contain next day ", keys.contains(todayPlusOneKey));
         assertTrue("Keys don't contain day after next ", keys.contains(todayPlusTwoKey));
 
         Map <String, Object> valueOne = (Map<String, Object>) map.get(todayKey);
         Map <String, Object> valueTwo = (Map<String, Object>) map.get(todayPlusOneKey);
         Map <String, Object> valueThree = (Map<String, Object>) map.get(todayPlusTwoKey);
         assertTrue("Value doesn't contain the right event", ((ArrayList)valueOne.values().iterator().next()).contains(e1));
         assertTrue("ValueTwo doesn't contain the right event", ((ArrayList)valueTwo.values().iterator().next()).contains(e2));
         assertTrue("ValueThree doesn't contain the right event", ((ArrayList)valueThree.values().iterator().next()).contains(e3));
     }
 
 
     public void testGetMapOfCurrentEventsForSpecificActivity() throws Exception {
         Participant participant = setId(11, createParticipant("Fred", "Jones"));
         StudySite studySite = setId(14, createStudySite(null, null));
         StudyParticipantAssignment assignment = new StudyParticipantAssignment();
         ScheduledCalendar calendar;
 
         ScheduledEvent e1, e2, e3;
 
         calendar = setId(6, new ScheduledCalendar());
         calendar.addArm(new ScheduledArm());
         calendar.addArm(new ScheduledArm());
 
         Calendar c = Calendar.getInstance();
         int day = c.get(Calendar.DAY_OF_MONTH);
         int month = c.get(Calendar.MONTH);
         int year = c.get(Calendar.YEAR);
 
         e1 =  createScheduledEvent("C", year, month, day);
         e1.setActivity(Fixtures.createActivity("Activity1"));
         e2 =  createScheduledEvent("O", year, month, day+1);
         e2.setActivity(Fixtures.createActivity("Activity2"));
         e3 =  createScheduledEvent("S", year, month, day+2);
         e3.getActivity().setType(ActivityType.INTERVENTION);
 
         addEvents(calendar.getScheduledArms().get(0), e1, e2, e3);
 
         assignment.setParticipant(participant);
         assignment.setStudySite(studySite);
         assignment.setStartDateEpoch(new Date());
         assignment.setScheduledCalendar(calendar);
         List<StudyParticipantAssignment> studyParticipantAssignment = new ArrayList<StudyParticipantAssignment>();
         studyParticipantAssignment.add(assignment);
 
         Map<ActivityType, Boolean> activities = new HashMap<ActivityType, Boolean>();
         activities.put(ActivityType.DISEASE_MEASURE, true);
         activities.put(ActivityType.LAB_TEST, false);
         activities.put(ActivityType.INTERVENTION, true);
 
 
         Collection<ScheduledEvent> events = new ArrayList<ScheduledEvent>();
         events.add(e1);
         Date startDate = new Date();
         Date tempStartDateOne = paService.shiftStartDayByNumberOfDays(startDate, 0);
         expect(scheduledEventDao.getEventsByDate(calendar, tempStartDateOne, tempStartDateOne)).andReturn(events);
 
         Collection<ScheduledEvent> eventsTwo = new ArrayList<ScheduledEvent>();
         eventsTwo.add(e2);
         Date tempStartDateTwo = paService.shiftStartDayByNumberOfDays(startDate, 1);
         expect(scheduledEventDao.getEventsByDate(calendar, tempStartDateTwo, tempStartDateTwo)).andReturn(eventsTwo);
 
         Collection<ScheduledEvent> eventsThree = new ArrayList<ScheduledEvent>();
         eventsThree.add(e3);
         Date tempStartDateThree = paService.shiftStartDayByNumberOfDays(startDate, 2);
         expect(scheduledEventDao.getEventsByDate(calendar, tempStartDateThree, tempStartDateThree)).andReturn(eventsThree);
 
         replayMocks();
         Map<String, Object> map = paService.getMapOfCurrentEventsForSpecificActivity(studyParticipantAssignment, 3, activities);
         verifyMocks();
 
         assertNotNull("Map is null", map);
        assertEquals("Map size is incorrect", 3, map.size());
 
         Set<String> keys = map.keySet();
         Date today = new Date();
         Date todayPlusOne = paService.shiftStartDayByNumberOfDays(today, 1);
         Date todayPlusTwo = paService.shiftStartDayByNumberOfDays(today, 2);
 
         String todayKey = paService.formatDateToString(today) + " - " + paService.convertDateKeyToString(today);
         String todayPlusOneKey = paService.formatDateToString(todayPlusOne) + " - " + paService.convertDateKeyToString(todayPlusOne);
         String todayPlusTwoKey = paService.formatDateToString(todayPlusTwo) + " - " + paService.convertDateKeyToString(todayPlusTwo);
 
        assertTrue("Keys don't contain today's date", keys.contains(todayKey));
        assertTrue("Keys don't contain next day ", keys.contains(todayPlusOneKey));
         assertTrue("Keys don't contain day after next ", keys.contains(todayPlusTwoKey));
 
         Map <String, Object> valueOne = (Map<String, Object>) map.get(todayKey);
         Map <String, Object> valueTwo = (Map<String, Object>) map.get(todayPlusOneKey);
         Map <String, Object> valueThree = (Map<String, Object>) map.get(todayPlusTwoKey);
        assertEquals("Value is not empty", 0, valueOne.values().size());
        assertEquals("ValueTwo is not empty", 0, valueTwo.values().size());
         assertEquals("ValueThree is empty", 1, valueThree.values().size());
         assertTrue("ValueThree doesn't contain the right event", ((ArrayList)valueThree.values().iterator().next()).contains(e3));
     }
 
 
     public void testExecute() throws Exception {
         Participant participantOne = setId(1, createParticipant("Kate", "Kateson"));
         Participant participantTwo = setId(2, createParticipant("Bill", "Billman"));
 
         StudySite studySite = setId(14, createStudySite(null, null));
         StudyParticipantAssignment assignment = new StudyParticipantAssignment();
         StudyParticipantAssignment assignmentTwo = new StudyParticipantAssignment();
         ScheduledCalendar calendar;
         ScheduledCalendar calendarTwo;
 
         ScheduledEvent e1, e2, e3, e5, e6, e7;
 
         Map<ActivityType, Boolean> activities = new HashMap<ActivityType, Boolean>();
         activities.put(ActivityType.LAB_TEST, true);
 
         calendar = setId(6, new ScheduledCalendar());
         calendar.addArm(new ScheduledArm());
 
         calendarTwo = setId(7, new ScheduledCalendar());
         calendarTwo.addArm(new ScheduledArm());
 
 
         Calendar c = Calendar.getInstance();
         int day = c.get(Calendar.DAY_OF_MONTH);
         int month = c.get(Calendar.MONTH);
         int year = c.get(Calendar.YEAR);
 
         e1 =  createScheduledEvent("C", year, month, day);
         e2 =  createScheduledEvent("O", year, month, day+1);
         e3 =  createScheduledEvent("S", year, month, day+2);
 
         addEvents(calendar.getScheduledArms().get(0), e1, e2, e3);
 
 
         e5 = createScheduledEvent("C", year, month, day +1, new Canceled());
         e6 = createScheduledEvent("O", year, month, day +2, new Occurred());
         e7 = createScheduledEvent("S", year, month, day +3);
 
         addEvents(calendarTwo.getScheduledArms().get(0), e5, e6, e7);
 
         assignment.setParticipant(participantOne);
         assignment.setStudySite(studySite);
         assignment.setStartDateEpoch(new Date());
         assignment.setScheduledCalendar(calendar);
 
         assignmentTwo.setParticipant(participantTwo);
         assignmentTwo.setStudySite(studySite);
         assignmentTwo.setStartDateEpoch(paService.shiftStartDayByNumberOfDays(new Date(), 2));
         assignmentTwo.setScheduledCalendar(calendarTwo);
 
         List<StudyParticipantAssignment> studyParticipantAssignments = new ArrayList<StudyParticipantAssignment>();
         studyParticipantAssignments.add(assignment);
         studyParticipantAssignments.add(assignmentTwo);
 
         expect(userDao.getAssignments(user)).andReturn(studyParticipantAssignments);
 
 
         Collection<ScheduledEvent> eventsForKate = new ArrayList<ScheduledEvent>();
         eventsForKate.add(e1);
         Collection<ScheduledEvent> eventsForBill = new ArrayList<ScheduledEvent>();
         Date startDate = new Date();
         Date tempStartDateOne = paService.shiftStartDayByNumberOfDays(startDate, 0);
         expect(scheduledEventDao.getEventsByDate(calendar, tempStartDateOne, tempStartDateOne)).andReturn(eventsForKate);
         expect(scheduledEventDao.getEventsByDate(calendarTwo, tempStartDateOne, tempStartDateOne)).andReturn(eventsForBill);
 
         Collection<ScheduledEvent> eventsTwoForKate = new ArrayList<ScheduledEvent>();
         eventsTwoForKate.add(e2);
         Collection<ScheduledEvent> eventsTwoForBill = new ArrayList<ScheduledEvent>();
         eventsTwoForBill.add(e5);
         Date tempStartDateTwo = paService.shiftStartDayByNumberOfDays(startDate, 1);
         expect(scheduledEventDao.getEventsByDate(calendar, tempStartDateTwo, tempStartDateTwo)).andReturn(eventsTwoForKate);
         expect(scheduledEventDao.getEventsByDate(calendarTwo, tempStartDateTwo, tempStartDateTwo)).andReturn(eventsTwoForBill);
 
         Collection<ScheduledEvent> eventsThreeForKate = new ArrayList<ScheduledEvent>();
         eventsThreeForKate.add(e3);
         Collection<ScheduledEvent> eventsThreeForBill = new ArrayList<ScheduledEvent>();
         eventsThreeForBill.add(e6);
 
         Date tempStartDateThree = paService.shiftStartDayByNumberOfDays(startDate, 2);
         expect(scheduledEventDao.getEventsByDate(calendar, tempStartDateThree, tempStartDateThree)).andReturn(eventsThreeForKate);
         expect(scheduledEventDao.getEventsByDate(calendarTwo, tempStartDateThree, tempStartDateThree)).andReturn(eventsThreeForBill);
 
         command.setActivityTypes(activities);
 
         replayMocks();
         Map<String, Object> map = command.execute(paService);
         verifyMocks();
         assertNotNull("Map is null", map);
         assertEquals("Map size is incorrect", 1, map.size());
         Set<String> keys = map.keySet();
         assertEquals("Keys of the outter map are not the same", "mapOfUserAndCalendar", keys.toArray()[0]);
 
         Map<String, Object> values = (Map<String, Object>) map.get(keys.toArray()[0]);
         Set<String> dates = values.keySet();
         Date today = new Date();
         Date todayPlusOne = paService.shiftStartDayByNumberOfDays(today, 1);
         Date todayPlusTwo = paService.shiftStartDayByNumberOfDays(today, 2);
 
         String todayKey = paService.formatDateToString(today) + " - " + paService.convertDateKeyToString(today);
         String todayPlusOneKey = paService.formatDateToString(todayPlusOne) + " - " + paService.convertDateKeyToString(todayPlusOne);
         String todayPlusTwoKey = paService.formatDateToString(todayPlusTwo) + " - " + paService.convertDateKeyToString(todayPlusTwo);
 
         assertTrue("Keys don't contain today's date", dates.contains(todayKey));
         assertTrue("Keys don't contain next day ", dates.contains(todayPlusOneKey));
         assertTrue("Keys don't contain day after next ", dates.contains(todayPlusTwoKey));
 
         Map <String, Object> valueOne = (Map<String, Object>) values.get(todayKey);
         Map <String, Object> valueTwo = (Map<String, Object>) values.get(todayPlusOneKey);
         Map <String, Object> valueThree = (Map<String, Object>) values.get(todayPlusTwoKey);
 
         assertEquals("ValueOne doesn't contain the right number of events", 1, valueOne.values().size());
         assertEquals("ValueTwo doesn't contain the right number of events", 2, valueTwo.values().size());
         assertEquals("ValueThree doesn't contain the right number of events", 2, valueTwo.values().size());
 
 
         Set<String> valueOneKey = valueOne.keySet();
         assertEquals("Wrong number of participants ", 1, valueOneKey.size());
         String participantKey = (String) valueOneKey.toArray()[0];
         String expectedParticipantKeyOne = participantOne.getFullName();
         assertEquals("Participants are not the same", expectedParticipantKeyOne, participantKey);
         assertEquals("Date " + today + "has more than one event", 1, valueOne.values().size());
 
         Set<String> valueTwoKey = valueTwo.keySet();
         assertEquals("Wrong number of participants ", 2, valueTwoKey.size());
         assertEquals("Date " + todayPlusOne + "has more than one event", 2, valueTwo.values().size());
 
         Set<String> valueThreeKey = valueThree.keySet();
         assertEquals("Wrong number of participants ", 2, valueThreeKey.size());
         assertEquals("Date " + todayPlusTwo + "has more than one event", 2, valueThree.values().size());
 
     }
 
 
     public void testExecuteWithDifferentActivityTypes() throws Exception {
         Participant participantOne = setId(1, createParticipant("Kate", "Kateson"));
         Participant participantTwo = setId(2, createParticipant("Bill", "Billman"));
 
         StudySite studySite = setId(14, createStudySite(null, null));
         StudyParticipantAssignment assignment = new StudyParticipantAssignment();
         StudyParticipantAssignment assignmentTwo = new StudyParticipantAssignment();
         ScheduledCalendar calendar;
         ScheduledCalendar calendarTwo;
 
         ScheduledEvent e1, e2, e3, e5, e6, e7;
 
         Map<ActivityType, Boolean> activities = new HashMap<ActivityType, Boolean>();
         activities.put(ActivityType.DISEASE_MEASURE, true);
         activities.put(ActivityType.LAB_TEST, false);
         activities.put(ActivityType.INTERVENTION, true);
         activities.put(ActivityType.PROCEDURE, true);
 
         calendar = setId(6, new ScheduledCalendar());
         calendar.addArm(new ScheduledArm());
 
         calendarTwo = setId(7, new ScheduledCalendar());
         calendarTwo.addArm(new ScheduledArm());
 
 
         Calendar c = Calendar.getInstance();
         int day = c.get(Calendar.DAY_OF_MONTH);
         int month = c.get(Calendar.MONTH);
         int year = c.get(Calendar.YEAR);
 
         e1 =  createScheduledEvent("C", year, month, day);
         e1.getActivity().setType(ActivityType.PROCEDURE);
         e2 =  createScheduledEvent("O", year, month, day+1);
         e2.getActivity().setType(ActivityType.INTERVENTION);
         e3 =  createScheduledEvent("S", year, month, day+2);
 
         addEvents(calendar.getScheduledArms().get(0), e1, e2, e3);
 
 
         e5 = createScheduledEvent("C", year, month, day +1);
         e6 = createScheduledEvent("O", year, month, day +2);
         e6.getActivity().setType(ActivityType.INTERVENTION);
         e7 = createScheduledEvent("S", year, month, day +3);
 
 
         addEvents(calendarTwo.getScheduledArms().get(0), e5, e6, e7);
 
         assignment.setParticipant(participantOne);
         assignment.setStudySite(studySite);
         assignment.setStartDateEpoch(new Date());
         assignment.setScheduledCalendar(calendar);
 
         assignmentTwo.setParticipant(participantTwo);
         assignmentTwo.setStudySite(studySite);
         assignmentTwo.setStartDateEpoch(paService.shiftStartDayByNumberOfDays(new Date(), 2));
         assignmentTwo.setScheduledCalendar(calendarTwo);
 
         List<StudyParticipantAssignment> studyParticipantAssignments = new ArrayList<StudyParticipantAssignment>();
         studyParticipantAssignments.add(assignment);
         studyParticipantAssignments.add(assignmentTwo);
 
         expect(userDao.getAssignments(user)).andReturn(studyParticipantAssignments);
 
 
         Collection<ScheduledEvent> eventsForKate = new ArrayList<ScheduledEvent>();
         eventsForKate.add(e1);
         Collection<ScheduledEvent> eventsForBill = new ArrayList<ScheduledEvent>();
         Date startDate = new Date();
         Date tempStartDateOne = paService.shiftStartDayByNumberOfDays(startDate, 0);
         expect(scheduledEventDao.getEventsByDate(calendar, tempStartDateOne, tempStartDateOne)).andReturn(eventsForKate);
         expect(scheduledEventDao.getEventsByDate(calendarTwo, tempStartDateOne, tempStartDateOne)).andReturn(eventsForBill);
 
         Collection<ScheduledEvent> eventsTwoForKate = new ArrayList<ScheduledEvent>();
         eventsTwoForKate.add(e2);
         Collection<ScheduledEvent> eventsTwoForBill = new ArrayList<ScheduledEvent>();
         eventsTwoForBill.add(e5);
         Date tempStartDateTwo = paService.shiftStartDayByNumberOfDays(startDate, 1);
         expect(scheduledEventDao.getEventsByDate(calendar, tempStartDateTwo, tempStartDateTwo)).andReturn(eventsTwoForKate);
         expect(scheduledEventDao.getEventsByDate(calendarTwo, tempStartDateTwo, tempStartDateTwo)).andReturn(eventsTwoForBill);
 
         Collection<ScheduledEvent> eventsThreeForKate = new ArrayList<ScheduledEvent>();
         eventsThreeForKate.add(e3);
         Collection<ScheduledEvent> eventsThreeForBill = new ArrayList<ScheduledEvent>();
         eventsThreeForBill.add(e6);
 
         Date tempStartDateThree = paService.shiftStartDayByNumberOfDays(startDate, 2);
         expect(scheduledEventDao.getEventsByDate(calendar, tempStartDateThree, tempStartDateThree)).andReturn(eventsThreeForKate);
         expect(scheduledEventDao.getEventsByDate(calendarTwo, tempStartDateThree, tempStartDateThree)).andReturn(eventsThreeForBill);
 
         command.setActivityTypes(activities);
 
         replayMocks();
         Map<String, Object> map = command.execute(paService);
         verifyMocks();
         assertNotNull("Map is null", map);
         assertEquals("Map size is incorrect", 1, map.size());
         Set<String> keys = map.keySet();
         assertEquals("Keys of the outter map are not the same", "mapOfUserAndCalendar", keys.toArray()[0]);
 
         Map<String, Object> values = (Map<String, Object>) map.get(keys.toArray()[0]);
         Set<String> dates = values.keySet();
         Date today = new Date();
         Date todayPlusOne = paService.shiftStartDayByNumberOfDays(today, 1);
         Date todayPlusTwo = paService.shiftStartDayByNumberOfDays(today, 2);
 
         String todayKey = paService.formatDateToString(today) + " - " + paService.convertDateKeyToString(today);
         String todayPlusOneKey = paService.formatDateToString(todayPlusOne) + " - " + paService.convertDateKeyToString(todayPlusOne);
         String todayPlusTwoKey = paService.formatDateToString(todayPlusTwo) + " - " + paService.convertDateKeyToString(todayPlusTwo);
 
         assertTrue("Keys don't contain today's date", dates.contains(todayKey));
         assertTrue("Keys don't contain next day ", dates.contains(todayPlusOneKey));
         assertTrue("Keys don't contain day after next ", dates.contains(todayPlusTwoKey));
 
         Map <String, Object> valueOne = (Map<String, Object>) values.get(todayKey);
         Map <String, Object> valueTwo = (Map<String, Object>) values.get(todayPlusOneKey);
         Map <String, Object> valueThree = (Map<String, Object>) values.get(todayPlusTwoKey);
 
         assertEquals("ValueOne doesn't contain the right number of events", 1, valueOne.values().size());
         assertEquals("ValueTwo doesn't contain the right number of events", 1, valueTwo.values().size());
         assertEquals("ValueThree doesn't contain the right number of events", 1, valueTwo.values().size());
 
         Set<String> valueOneKey = valueOne.keySet();
         assertEquals("Wrong number of participants ", 1, valueOneKey.size());
         String participantKey = (String) valueOneKey.toArray()[0];
         String expectedParticipantKeyOne = participantOne.getFullName() ;
         assertEquals("Participants are not the same", expectedParticipantKeyOne, participantKey);
         assertEquals("Date " + today + "has more than one event", 1, valueOne.values().size());
 
         Set<String> valueTwoKey = valueTwo.keySet();
         assertEquals("Wrong number of participants ", 1, valueTwoKey.size());
         assertEquals("Date " + todayPlusOne + "has more than one event", 1, valueTwo.values().size());
 
         Set<String> valueThreeKey = valueThree.keySet();
         assertEquals("Wrong number of participants ", 1, valueThreeKey.size());
         assertEquals("Date " + todayPlusTwo + "has more than one event", 1, valueThree.values().size());
 
     }
 
 
 }
 
