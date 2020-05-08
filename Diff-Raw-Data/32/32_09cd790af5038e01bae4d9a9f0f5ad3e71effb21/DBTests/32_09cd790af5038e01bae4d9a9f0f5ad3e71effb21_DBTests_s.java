 package com.fernferret.android.fortywinks.test;
 
 import java.util.HashMap;
 import java.util.List;
 
 import android.test.ActivityInstrumentationTestCase2;
 
 import com.fernferret.android.fortywinks.Alarm;
 import com.fernferret.android.fortywinks.DBAdapter;
 import com.fernferret.android.fortywinks.FortyWinks;
 import com.fernferret.android.fortywinks.SQLiteAdapter;
 
 public class DBTests extends ActivityInstrumentationTestCase2<FortyWinks> {
     
     private DBAdapter mDba;
 
     public DBTests() {
         super("com.fernferret.android.fortywinks", FortyWinks.class);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         mDba = new SQLiteAdapter(this.getActivity());
         mDba.resetDatabase();
     }
     
     public void testPreconditions() {
         assertNotNull(mDba);
     }
     
     private void testBasicEquality(Alarm a, Alarm b) {
         assertEquals(a.getDaysOfWeek(), b.getDaysOfWeek());
         assertEquals(a.getEnabled(), b.getEnabled());
         assertEquals(a.getHour(), b.getHour());
         assertEquals(a.getMinute(), b.getMinute());
         assertEquals(a.getNumFollowups(), b.getNumFollowups());
         assertEquals(a.getIntervalStart(), b.getIntervalStart());
         assertEquals(a.getIntervalEnd(), b.getIntervalEnd());
     }
     
     public void testSaveSimpleAlarm() {
         Alarm a = new Alarm(-1);
         
         a.setDaysOfWeek(1);
         a.setEnabled(false);
         a.setHour(4);
         a.setMinute(6);
         a.setNumFollowups(7);
         a.setIntervalStart(3);
         a.setIntervalEnd(3);
         
         mDba.saveAlarm(a);
         assertFalse(a.getId() == -1);
         Alarm result = mDba.getAlarm(a.getId());
         testBasicEquality(a, result);
     }
     
     public void testSavePowerNap() {
         Alarm a = new Alarm(-1);
         
         a.setDaysOfWeek(1);
         a.setEnabled(false);
         a.setHour(4);
         a.setMinute(6);
         a.setIsPowerNap(true);
         
         mDba.saveAlarm(a);
         assertFalse(a.getId() == -1);
         
         Alarm result = mDba.getPowerNap();
         assertNotNull(result);
         testBasicEquality(a, result);
     }
     
     public void testOverridePowerNap() {
         Alarm a = new Alarm(-1);
         
         a.setDaysOfWeek(1);
         a.setEnabled(false);
         a.setHour(4);
         a.setMinute(6);
         a.setIsPowerNap(true);
         a.setNumFollowups(7);
         
         Alarm b = new Alarm(-1);
         
         b.setDaysOfWeek(2);
         b.setEnabled(false);
         b.setHour(5);
         b.setMinute(7);
         b.setIsPowerNap(true);
         b.setNumFollowups(9);
         
         mDba.saveAlarm(a);
         assertFalse(a.getId() == -1);
         
         mDba.saveAlarm(b);
         assertFalse(b.getId() == -1);
         
         assertFalse(mDba.alarmExists(a.getId()));
         
         Alarm result = mDba.getPowerNap();
         assertNotNull(result);
         testBasicEquality(b, result);
     }
     
     public void testSetQuikAlarms() {
         Alarm a = new Alarm();
         
         a.setDaysOfWeek(127);
         a.setEnabled(false);
         a.setHour(4);
         a.setMinute(6);
         a.setIsQuikAlarm(true);
         
         Alarm b = new Alarm();
         
         b.setDaysOfWeek(127);
         b.setEnabled(false);
         b.setHour(4);
         b.setMinute(7);
         b.setIsQuikAlarm(true);
         
         Alarm c = new Alarm();
         
         c.setDaysOfWeek(127);
         c.setEnabled(false);
         c.setHour(4);
         c.setMinute(5);
         c.setIsQuikAlarm(true);
         
         mDba.saveAlarm(a);
         assertFalse(a.getId() == -1);
         
         mDba.saveAlarm(b);
         assertFalse(b.getId() == -1);
         
         mDba.saveAlarm(c);
         assertFalse(c.getId() == -1);
         
         List<Alarm> quikAlarms = mDba.getQuikAlarmsAndAlarms();
         assertEquals(quikAlarms.size(), 3);
         testBasicEquality(quikAlarms.get(0), c);
         testBasicEquality(quikAlarms.get(1), a);
         testBasicEquality(quikAlarms.get(2), b);
         
     }
     
     public void testCreateNumFollowups() {
         Alarm a = new Alarm();
         
         a.setNumFollowups(4);
         a.setIntervalStart(3);
         a.setIntervalEnd(3);
         a.enableDay(Alarm.Day.SUNDAY);
         a.enableDay(Alarm.Day.MONDAY);
         a.enableDay(Alarm.Day.TUESDAY);
         a.enableDay(Alarm.Day.WEDNESDAY);
         a.enableDay(Alarm.Day.THURSDAY);
         a.enableDay(Alarm.Day.FRIDAY);
         a.enableDay(Alarm.Day.SATURDAY);
         
         mDba.saveAlarm(a);
         
         HashMap<Integer, Long> followups = a.getFollowups();
         assertEquals(followups.keySet().size(), 4);
     }
     
     public void testRetrieveFollowupsByRawNumber() {
         Alarm a = new Alarm();
         
         a.setNumFollowups(4);
         a.setIntervalStart(3);
         a.setIntervalEnd(3);
         a.enableDay(Alarm.Day.SUNDAY);
         a.enableDay(Alarm.Day.MONDAY);
         a.enableDay(Alarm.Day.TUESDAY);
         a.enableDay(Alarm.Day.WEDNESDAY);
         a.enableDay(Alarm.Day.THURSDAY);
         a.enableDay(Alarm.Day.FRIDAY);
         a.enableDay(Alarm.Day.SATURDAY);
         
         mDba.saveAlarm(a);
         
         Alarm result = mDba.getAlarm(a.getId());
         
         HashMap<Integer, Long> followups = result.getFollowups();
         assertEquals(followups.keySet().size(), 4);
     }
     
     public void testSetIsPowerNap() {
         Alarm a = new Alarm();
         Alarm b = new Alarm();
         
         a.setIsPowerNap(true);
         b.setIsPowerNap(false);
         
         mDba.saveAlarm(a);
         mDba.saveAlarm(b);
         
         Alarm resultA = mDba.getAlarm(a.getId());
         Alarm resultB = mDba.getAlarm(b.getId());
         
         assertTrue(resultA.isPowerNap());
         assertFalse(resultB.isPowerNap());
     }
     
     public void testSetIsQuikAlarm() {
         Alarm a = new Alarm();
         Alarm b = new Alarm();
         
         a.setIsQuikAlarm(true);
         b.setIsQuikAlarm(false);
         
         mDba.saveAlarm(a);
         mDba.saveAlarm(b);
         
         Alarm resultA = mDba.getAlarm(a.getId());
         Alarm resultB = mDba.getAlarm(b.getId());
         
         assertTrue(resultA.isQuikAlarm());
         assertFalse(resultB.isQuikAlarm());
     }
 }
