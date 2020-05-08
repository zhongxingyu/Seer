 package gps.tasks.task3663;
 
 import java.io.FileInputStream;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.dbunit.Assertion;
 import org.dbunit.IDatabaseTester;
 import org.dbunit.JdbcDatabaseTester;
 import org.dbunit.database.IDatabaseConnection;
 import org.dbunit.dataset.IDataSet;
 import org.dbunit.dataset.ITable;
 import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
 import org.dbunit.operation.DatabaseOperation;
 
 import org.joda.time.LocalDate;
 import org.junit.Test;
 import org.junit.After;
 import org.junit.Before;
 import static org.junit.Assert.assertTrue;
 
 import static org.junit.Assert.*;
 import static org.hamcrest.core.Is.is;
 
 
 public class TestRegistry
 {
 
 /*  Pick a working directory
   private String path = "src/gps/tasks/task3663/";  // eclipse
   private String path = "";                         // make
 */
   private String path = "src/gps/tasks/task3663/";  // eclipse
 
 
 
 //------------------------------------------------------------------------------
 //  Setup
 //------------------------------------------------------------------------------
 
   private IDatabaseTester dbtester;
   IDatabaseConnection dbuconn;
 
   protected IDataSet getDataSet(String f) throws Exception  //{{{
   {
     return
       new FlatXmlDataSetBuilder().build(
         new FileInputStream(path + f));
   } //}}}
 
   @Before
   public void setUp() throws Exception  //{{{
   {
 
     // set up connection
     dbtester = new JdbcDatabaseTester("com.mysql.jdbc.Driver",
                                       "jdbc:mysql://localhost/cal",
                                       "cal",
                                       "cal");
     //initialise dataset
     IDataSet dataSet = getDataSet("dataset.xml");
     dbtester.setDataSet(dataSet);
 
     dbuconn = dbtester.getConnection();
 
     // call default setUpOperation
     DatabaseOperation.TRUNCATE_TABLE.execute(dbuconn, getDataSet("dataset.xml"));
     dbtester.onSetup();
 
     // reset registry aka database cache
     CalendarRegistry.eventBeans = null;
     CalendarRegistry.eventBeans = new HashMap<Integer, Map<String, String>>();
 
     CalendarRegistry.guestBeans = null;
     CalendarRegistry.guestBeans = new HashMap<Integer, Map<String, String>>();
   } //}}}
 
   @After
   public void tearDown() throws Exception //{{{
   {
     dbtester.onTearDown();
   } //}}}
 
 
 
 //------------------------------------------------------------------------------
 //  tests for cache helpers
 //------------------------------------------------------------------------------
   @Test
   public void test_fetchEvents() throws SQLException  //{{{
   {
     // expected value of CalendarRegistry.eventBeans
 
     Map<String, String> ev0 = new HashMap<String, String>();
     Map<String, String> ev1 = new HashMap<String, String>();
     Map<String, String> ev2 = new HashMap<String, String>();
     Map<Integer, Map<String, String>> expected = new HashMap<Integer, Map<String, String>>();
 
     ev0.put("id",           "5");
     ev0.put("day",          "2012-05-05");
     ev0.put("timeStart",    "15:00");
     ev0.put("timeEnd",      "18:00");
     ev0.put("kind",         "other");
     ev0.put("description",  "cinco de mayo happy hour");
     ev0.put("guests",       "");
     expected.put(5, ev0);
 
     ev1.put("id",           "1");
     ev1.put("day",          "");
     ev1.put("timeStart",    "");
     ev1.put("timeEnd",      "");
     ev1.put("kind",         "");
     ev1.put("description",  "");
     ev1.put("guests",       "");
     expected.put(1, ev1);
 
     ev2.put("id",           "3");
     ev2.put("day",          "0000-00-00");
     ev2.put("timeStart",    "");
     ev2.put("timeEnd",      "");
     ev2.put("kind",         "other");
     ev2.put("description",  "beginning of common era");
     ev2.put("guests",       "");
     expected.put(3, ev2);
 
 
     // use the method
 
     CalendarRegistry.fetchEvents(Arrays.asList(5,1,3));
 
 
     // capture side effect
 
     Map<Integer, Map<String, String>> result = CalendarRegistry.eventBeans;
 
 
     // will it blend?
 
     assertThat(result, is(expected));
   } //}}}
 
   @Test
   public void test_fetchGuests_weirdName() throws SQLException  //{{{
   {
     //  expected value of CalendarRegistry.guestBeans
 
     Map<String, String> guest0 = new HashMap<String, String>();
     Map<Integer, Map<String, String>> expected = new HashMap<Integer, Map<String, String>>();
 
     guest0.put("id",     "1");
     guest0.put("name",   " null 0m*[2~*[2~*[2~*l*OQ0's'S'F2498 7*[5~*[3~*[4~df asdf[sd fw erfj203409faf; dsf;lsk dfas;df j2-sa;fsdf s\"i$(*%$R*$#(*R $(*R@J#R*@R$# R( $#*) $ R@# R$($* R(i   \"\"\"j;sdf asdfasfsadfs m1f93mf 933f134- f4398 `9t834p14-r039qtp3pq38t1tp3tq3 pgjasd;lfksdj fas");
     guest0.put("email",  "foo@bar.biz");
     expected.put(1, guest0);
 
 
     //  use the method
 
     CalendarRegistry.fetchGuests(Arrays.asList(1));
 
 
     //  capture side effect
 
     Map<Integer, Map<String, String>> result = CalendarRegistry.guestBeans;
 
 
     //  will it blend?
 
     assertThat(result, is(expected));
   } //}}}
 
   @Test
   public void test_fetchGuests() throws SQLException  //{{{
   {
     // expected value of CalendarRegistry.GuestBeans
 
     Map<String, String> guest0 = new HashMap<String, String>();
     Map<String, String> guest1 = new HashMap<String, String>();
     Map<String, String> guest2 = new HashMap<String, String>();
     Map<Integer, Map<String, String>> expected = new HashMap<Integer, Map<String, String>>();
 
     guest0.put("id",     "3");
     guest0.put("name",   "Dweezil");
     guest0.put("email",  "bugs@zappa.fam");
     expected.put(3, guest0);
 
     guest1.put("id",     "2");
     guest1.put("name",   "Moon Unit");
     guest1.put("email",  "lazers@zappa.fam");
     expected.put(2, guest1);
 
     guest2.put("id",     "5");
     guest2.put("name",   "Diva Thin Muffin Pigeen");
     guest2.put("email",  "pdgn@zappa.fam");
     expected.put(5, guest2);
 
 
     // use the method
 
     CalendarRegistry.fetchGuests(Arrays.asList(3,2,5));
 
 
     // capture side effect
 
     Map<Integer, Map<String, String>> result = CalendarRegistry.guestBeans;
 
 
     // will it blend?
 
     assertThat(result, is(expected));
   } //}}}
 
 
   @Test
   public void test_unregEvents() throws SQLException  //{{{
   {
     // modify eventBeans to test that method ignores existing eids
 
     Map<String, String> ev1 = new HashMap<String, String>();
 
     ev1.put("id",           "1");
     ev1.put("day",          "");
     ev1.put("timeStart",    "");
     ev1.put("timeEnd",      "");
     ev1.put("kind",         "");
     ev1.put("description",  "");
     ev1.put("guests",       "");
 
     CalendarRegistry.eventBeans.put(1, ev1);
 
 
     // expected
 
     List<Integer> expected = Arrays.asList(5,3);
 
 
     // result
 
     List<Integer> result = CalendarRegistry.unregEvents(Arrays.asList(5,1,3));
 
 
     // will it blend?
 
     assertThat(result, is(expected));
   } //}}}
 
   @Test
   public void test_unregGuests()
   {
     assertTrue("test unwritten", false);
   }
 
 
   @Test
   public void test_updateEventBeans() throws SQLException//{{{
   {
     // expected value of CalendarRegistry.eventBeans
 
     Map<String, String> ev0 = new HashMap<String, String>();
     Map<String, String> ev1 = new HashMap<String, String>();
     Map<String, String> ev2 = new HashMap<String, String>();
     Map<Integer, Map<String, String>> expected = new HashMap<Integer, Map<String, String>>();
 
     ev0.put("id",           "5");
     ev0.put("day",          "2012-05-05");
     ev0.put("timeStart",    "15:00");
     ev0.put("timeEnd",      "18:00");
     ev0.put("kind",         "other");
     ev0.put("description",  "cinco de mayo happy hour");
     ev0.put("guests",       "");
     expected.put(5, ev0);
 
     ev1.put("id",           "1");
     ev1.put("day",          "");
     ev1.put("timeStart",    "");
     ev1.put("timeEnd",      "");
     ev1.put("kind",         "");
     ev1.put("description",  "");
     ev1.put("guests",       "");
     expected.put(1, ev1);
 
     ev2.put("id",           "3");
     ev2.put("day",          "0000-00-00");
     ev2.put("timeStart",    "");
     ev2.put("timeEnd",      "");
     ev2.put("kind",         "other");
     ev2.put("description",  "beginning of common era");
     ev2.put("guests",       "");
     expected.put(3, ev2);
 
 
     // modify eventBeans to test that method ignores existing eids
 
     CalendarRegistry.eventBeans.put(1, ev1);
 
 
     // use method
 
     CalendarRegistry.updateEventBeans(Arrays.asList(5,1,3));
 
 
     // capture side effect
 
     Map<Integer, Map<String, String>> result = CalendarRegistry.eventBeans;
 
 
     // will it blend?
 
     assertThat(result, is(expected));
   }//}}}
 
   @Test
   public void test_updateGuestBeans()
   {
     assertTrue("test unwritten", false);
   }
 
 
 
 //------------------------------------------------------------------------------
 //  misc generic (aka multipath)
 //------------------------------------------------------------------------------
 
   @Test
   public void test_send_event() throws Exception//{{{
   {
     // expected table values
 
     IDataSet expectedDs = getDataSet("expected_modRow_update.xml");
     ITable expectedTable = expectedDs.getTable("events");
 
 
     // use the function
 
     Event e = new Event();
     e.setId("3");
     e.setTimeStart("00:00");
 
     CalendarRegistry.send("event", e.getMap());
 
 
     // capture side effects
 
     IDataSet actualDs = dbuconn.createDataSet();
     ITable actualTable = actualDs.getTable("events");
 
 
     // will it blend?
 
     Assertion.assertEquals(expectedTable, actualTable);
   }//}}}
 
   @Test
  public void test_save_event() throws SQLException//{{{
   {
     // use the function
 
     Event e = new Event();
     e.setId("3");
     e.setTimeStart("00:00");
 
    CalendarRegistry.save("event", e.getMap());
 
     /*
      * test passes if nothing borks.
      *
      * i thought this test wasn't needed, but then i found a bug where
      * ModifyDb.modRow() was abusing the bean pointer it gets; namely deleting
      * the id. naturally this pissed off CalendarRegistry.send(), who gets a
      * null pointer instead of the id it wants.
      */
   }//}}}
 
   @Test
   public void test_send_guest() throws SQLException
   {
     assertTrue("test unwritten", false);
   }
 
   @Test
   public void test_save_guest() throws SQLException
   {
     assertTrue("test unwritten", false);
   }
 
 
 
 //------------------------------------------------------------------------------
 //  tests guest ui methods
 //------------------------------------------------------------------------------
 
 
   @Test
   public void test_getGuests() throws SQLException
   {
     // expected
 
     List<Integer> expected = Arrays.asList(1,2,3,4,5);
 
 
     // result
 
     List<Integer> result = CalendarRegistry.getGuests();
 
 
     // will it blend?
 
     assertThat(result, is(expected));
   }
 
   @Test
   public void test_getGuestNames() throws SQLException
   {
     // expected
 
     Map<Integer, String> expected = new HashMap<Integer, String>();
     expected.put(3, "Dweezil");
     expected.put(2, "Moon Unit");
 
 
     // result
 
     Map<Integer, String> result = CalendarRegistry.getGuestNames(Arrays.asList(2,3));
 
 
     // will it blend?
 
     assertThat(result, is(expected));
   }
 
 
 
 //------------------------------------------------------------------------------
 //  tests event ui methods
 //------------------------------------------------------------------------------
 
   @Test
   public void test_getEventTagLines() throws SQLException //{{{
   {
     // expected
 
     Map<Integer, String> expected = new HashMap<Integer, String>();
 
     expected.put(2, "all hallows eve, when ghouls moan and banshees scr");
     expected.put(6, "cinco de mayo");
 
 
     // result
 
     Map<Integer, String> result = CalendarRegistry.getEventTaglines(Arrays.asList(2, 6));
 
 
     // will it blend?
 
     assertThat(result, is(expected));
   } //}}}
 
   @Test
   public void test_getEvent_cached() throws SQLException//{{{
   {
     // make map for cache
 
     Map<String, String> cache = new HashMap<String, String>();
     cache.put("id",           "5");
     cache.put("day",          "2012-05-05");
     cache.put("timeStart",    "15:00");
     cache.put("timeEnd",      "18:00");
     cache.put("kind",         "other");
     cache.put("description",  "cinco de mayo happy hour");
     cache.put("guests",       "");
 
 
     // add map to event bean list aka cache
 
     CalendarRegistry.eventBeans.put(5, cache);
 
 
     // expected
 
     Event expected = new Event(5);
     expected.setDay(          "2012-05-05");
     expected.setDescription(  "cinco de mayo happy hour");
     expected.setGuests(       "");
     expected.setKind(         "other");
     expected.setTimeEnd(      "18:00");
     expected.setTimeStart(    "15:00");
 
 
     // result
 
     Event result = CalendarRegistry.getEvent(5);
 
 
     // will it blend?
 
     assertThat(result.getMap(), is(expected.getMap()));
   }//}}}
 
   @Test
   public void test_getEvent_uncached() throws SQLException//{{{
   {
     // expected
 
     Event expected = new Event(5);
     expected.setDay(          "2012-05-05");
     expected.setDescription(  "cinco de mayo happy hour");
     expected.setGuests(       "");
     expected.setKind(         "other");
     expected.setTimeEnd(      "18:00");
     expected.setTimeStart(    "15:00");
 
 
     // result
 
     Event result = CalendarRegistry.getEvent(5);
 
 
     // will it blend?
 
     assertThat(result.getMap(), is(expected.getMap()));
   }//}}}
 
   @Test
   public void test_getDatesInRange()//{{{
   {
     // expected
 
     List<LocalDate> expected = Arrays.asList(
       new LocalDate(2012, 05, 28),
       new LocalDate(2012, 05, 29),
       new LocalDate(2012, 05, 30),
       new LocalDate(2012, 05, 31),
       new LocalDate(2012, 06, 01),
       new LocalDate(2012, 06, 02),
       new LocalDate(2012, 06, 03));
 
 
     // actual
 
     List<LocalDate> result = CalendarRegistry.getDatesInRange(
       new LocalDate(2012, 05, 28),
       new LocalDate(2012, 06, 03));
 
 
     // will it blend?
 
     assertThat(result, is(expected));
   }//}}}
 
   @Test
   public void test_eventDatesFor() throws SQLException//{{{
   {
     // expected
 
     List<LocalDate> expected = Arrays.asList(
       new LocalDate(2012, 05, 05),
       new LocalDate(2012, 05, 05),
       new LocalDate(2012, 05, 13),
       new LocalDate(2012, 05, 28));
 
 
     // actual
 
     List<LocalDate> result = CalendarRegistry.eventDatesFor(
       new LocalDate(2012, 05, 01),
       new LocalDate(2012, 05, 31));
 
 
     // will it blend?
 
     assertThat(result, is(expected));
   }//}}}
 
   @Test
   public void test_getEventsFor() throws SQLException//{{{
   {
     // expected
 
     List<Integer> expected = Arrays.asList(5, 6);
 
 
     // actual
 
     List<Integer> result = CalendarRegistry.getEventsFor(new LocalDate(2012,05,05));
 
 
     // will it blend?
 
     assertThat(result, is(expected));
   }//}}}
 
   @Test
   public void test_getEventsFor_empty() throws SQLException//{{{
   {
     // expected
 
     List<Integer> expected = Arrays.asList();
 
 
     // result
 
     List<Integer> result = CalendarRegistry.getEventsFor(new LocalDate(2012,05,06));
 
 
     // will it blend?
 
     assertThat(result, is(expected));
   }//}}}
 
 
 }
