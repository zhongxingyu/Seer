 package se.chalmers.dat255.sleepfighter.model;
 
 import java.util.Arrays;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 import net.engio.mbassy.listener.Handler;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeConstants;
 import org.joda.time.MutableDateTime;
 
import android.provider.ContactsContract.CommonDataKinds.Event;
 
 import se.chalmers.dat255.sleepfighter.model.Alarm.ScheduleChangeEvent;
 import se.chalmers.dat255.sleepfighter.model.Alarm.MetaChangeEvent;
 import se.chalmers.dat255.sleepfighter.utils.debug.Debug;
 import se.chalmers.dat255.sleepfighter.utils.message.Message;
 import se.chalmers.dat255.sleepfighter.utils.message.MessageBus;
 
 public class AlarmTest extends TestCase {
 
 
 	public void testConstructor() {
 		Alarm alarm = new Alarm(4, 3);
 		
 		assertEquals(4, alarm.getHour());
 		assertEquals(3, alarm.getMinute());
 	}
 	
 	public void testConstructorExceptions() {
 		// invalid hour
 		try {
 			new Alarm(-1, 3);
 			Assert.fail("Should have thrown IllegalArgumentException");
 		} catch (IllegalArgumentException e) {
 			// success
 		}
 		try {
 			new Alarm(24, 4);
 			Assert.fail("Should have thrown IllegalArgumentException");
 		} catch (IllegalArgumentException e) {
 			// success
 		}
 
 		// invalid minute
 		try {
 			new Alarm(4, -1);
 			Assert.fail("Should have thrown IllegalArgumentException");
 		} catch (IllegalArgumentException e) {
 			// success
 		}
 		try {
 			new Alarm(4, 60);
 			Assert.fail("Should have thrown IllegalArgumentException");
 		} catch (IllegalArgumentException e) {
 			// success
 		}
 
 		// invalid second
 		try {
 			new Alarm(4, 1, -1);
 			Assert.fail("Should have thrown IllegalArgumentException");
 		} catch (IllegalArgumentException e) {
 			// success
 		}
 		try {
 			new Alarm(4, 60, 61);
 			Assert.fail("Should have thrown IllegalArgumentException");
 		} catch (IllegalArgumentException e) {
 			// success
 		}
 	}
 	
 	public void testSetTime() {
 		Alarm alarm = new Alarm(4, 3);
 		alarm.setTime(3, 4);
 		
 		assertEquals(3, alarm.getHour());
 		assertEquals(4, alarm.getMinute());
 		assertEquals(0, alarm.getSecond());
 		
 		Alarm alarm2 = new Alarm(4, 3);
 		alarm2.setTime(3, 4, 2);
 		
 		assertEquals(3, alarm2.getHour());
 		assertEquals(4, alarm2.getMinute());
 		assertEquals(2, alarm2.getSecond());
 	}
 	
 	public class Subscriber3 {
 		
 		public boolean passed = false;
 		public Alarm alarm = new Alarm(1,2, 3);
 		
 		@Handler
 		public void handleMetaChange( ScheduleChangeEvent evt ) {
 			passed = (evt.getModifiedField() == Alarm.Field.TIME) && (alarm == evt.getAlarm());
 			// test failed
 			if(!passed) 
 				return;
 			
 			// else we will aslo test for old value
 			int[] old = (int[])evt.getOldValue();
 				
 			passed = 1 == old[0] && 2 == old[1] && 3 == old[2];
 		}
 		
 	}
 	
 	public void testSetTimeMessage() {
 		Subscriber3 sub = new Subscriber3();
 
 		MessageBus<Message> bus = new MessageBus<Message>();
 		bus.subscribe( sub );
 
 		sub.alarm.setMessageBus(bus);
 		sub.alarm.setTime(8, 7, 4);
 		assertTrue(sub.passed);
 	}
 	
 	public void testSetTimeExceptions() {
 		Alarm alarm = new Alarm(4, 3);
 		
 		// invalid hour
 		try {
 			alarm.setTime(-1, 4);
 			Assert.fail("Should have thrown IllegalArgumentException");
 		} catch(IllegalArgumentException e) {
 			// success
 		}
 		try {
 			alarm.setTime(24, 4);
 			Assert.fail("Should have thrown IllegalArgumentException");
 		} catch(IllegalArgumentException e) {
 			// success
 		}
 		
 		// invalid minute
 		try {
 			alarm.setTime(4, -1);
 			Assert.fail("Should have thrown IllegalArgumentException");
 		} catch(IllegalArgumentException e) {
 			// success
 		}
 		try {
 			alarm.setTime(4, 60);
 			Assert.fail("Should have thrown IllegalArgumentException");
 		} catch(IllegalArgumentException e) {
 			// success
 		}
 	}
 	
 	public void testEquals() {
 		Alarm alarm1 = new Alarm(3,2);
 		Alarm alarm2 = new Alarm(3,2);
 		alarm1.setId(0);
 		alarm2.setId(0);
 		
 		
 		assertEquals(alarm1, alarm2);
 		
 		
 		alarm2.setTime(3, 1);
 		
 		// equality is decided by ID.
 		assertTrue(alarm1.equals(alarm2));
 
 		alarm2.setId(1);
 		assertFalse(alarm1.equals(alarm2));
 		
 		// same reference test
 		Alarm alarmCopy = alarm1;
 		assertEquals(alarmCopy, alarm1);
 		
 		// compare with null
 		assertFalse(alarm1.equals(null));
 	}
 	
 	public void testCanHappen() {
 		Alarm alarm = new Alarm(3, 1);
 		alarm.setActivated(false);
 		// does it respect isActivated?
 		assertFalse(alarm.canHappen());
 		
 		alarm.setActivated(true);
 		
 		// does it respect enabledDays?
 		 boolean[] enabledDays =  { false, false, false, false, false, false, true };
 		 alarm.setEnabledDays(enabledDays);
 		 assertTrue(alarm.canHappen());
 		 
 		 boolean[] enabledDays2 =  { false, false, false, false, false, false, false };
 		 alarm.setEnabledDays(enabledDays2);
 		 assertFalse(alarm.canHappen());
 	}
 	
 	public void testGetNextMillis() {
 		Alarm alarm = new Alarm(1, 1);
 		
 		// does it respect canHappen?
 		alarm.setActivated(false);
 		long now = new DateTime(0, 1, 1, 0, 0).getMillis();
 		assertEquals(Alarm.NEXT_NON_REAL, alarm.getNextMillis(now));
 		alarm.setActivated(true);
 
 		// alarm is one minute after now.
 		Long test = new DateTime(0, 1, 1, 1, 1).getMillis();
 		assertEquals( test, alarm.getNextMillis(now) );
 
 		// test that it handles cases when alarm < now.
 		now = new DateTime(0, 1, 1, 10, 10).getMillis();
 		test = new DateTime(0, 1, 2, 1, 1).getMillis();
 		assertEquals( test, alarm.getNextMillis(now) );
 
 		// test that it handles cases when a weekday is disabled.
 		// monday (first) is disabled, alarm should ring on tuesday.
 		alarm.setEnabledDays( new boolean[] { false, true, true, true, true, true, true } );
 
 		MutableDateTime time = new MutableDateTime(0, 1, 1, 0, 0, 0, 0);
 		time.setDayOfWeek( DateTimeConstants.MONDAY );
 		now = time.getMillis();
 
 		time.addDays( 1 );
 		time.addHours( 1 );
 		time.addMinutes( 1 );
 		test = time.getMillis();
 		assertEquals( test, alarm.getNextMillis(now) );
 	}
 	
 	public class Subscriber1 {
 		
 		public boolean passed = false;
 		public Alarm alarm = new Alarm(1,2);
 		
 		@Handler
 		public void handleMetaChange( MetaChangeEvent evt ) {
 			passed = (evt.getModifiedField() == Alarm.Field.ID) && (alarm == evt.getAlarm()) &&
 					alarm.getId() == 2 &&
 					(Integer)evt.getOldValue() == 3;
 		}
 	}
 	
 	public void testSetId() {
 		Subscriber1 sub = new Subscriber1();
 
 		MessageBus<Message> bus = new MessageBus<Message>();
 		bus.subscribe( sub );
 
 		sub.alarm.setMessageBus(bus);
 		sub.alarm.setId(3);
 		sub.alarm.setId(2);
 		assertTrue(sub.passed);
 	}	
 
 	public class Subscriber2 {
 		
 		public boolean passed = false;
 		public Alarm alarm = new Alarm(1,2);
 		
 		@Handler
 		public void handleMetaChange( MetaChangeEvent evt ) {
 			passed = evt.getModifiedField() == Alarm.Field.NAME && 
 					alarm == evt.getAlarm() &&
 					alarm.getName().equals("hell world") && evt.getOldValue().equals("name");
 		}
 		
 	}
 	
 	public void testSetName() {
 		Subscriber2 sub = new Subscriber2();
 
 		MessageBus<Message> bus = new MessageBus<Message>();
 		bus.subscribe( sub );
 
 		sub.alarm.setMessageBus(bus);
 		
 		sub.alarm.setName("name");
 		sub.alarm.setName("hell world");
 		assertTrue(sub.passed);
 		
 		// a named alarm cannot be unnamed
 		try {
 			sub.alarm.setName(null);
 			Assert.fail("Should have thrown IllegalArgumentException");
 		} catch (IllegalArgumentException e) {
 		}
 	}	
 	
 	public class Subscriber4 {
 		
 		public boolean passed = false;
 		public Alarm alarm = new Alarm(1,2);
 		
 		@Handler
 		public void handleMetaChange( ScheduleChangeEvent evt ) {
 			passed = (evt.getModifiedField() == Alarm.Field.ACTIVATED) && (alarm == evt.getAlarm()
 					&& alarm.isActivated() == false && (Boolean)evt.getOldValue() == true);
 		}
 		
 	}
 	
 	public void testSetActivated() {
 		Subscriber4 sub = new Subscriber4();
 
 		MessageBus<Message> bus = new MessageBus<Message>();
 		bus.subscribe( sub );
 
 		sub.alarm.setMessageBus(bus);
 		
 		sub.alarm.setActivated(true);
 		sub.alarm.setActivated(false);
 		assertTrue(sub.passed);
 	}	
 
 	public class Subscriber5 {
 		
 		public boolean passed = false;
 		public Alarm alarm = new Alarm(1,2);
 		
 		@Handler
 		public void handleMetaChange( ScheduleChangeEvent evt ) {
 			passed = (evt.getModifiedField() == Alarm.Field.ENABLED_DAYS) && (alarm == evt.getAlarm());
 			if(!passed)
 				return;
 			
 			// now test the days. 
 			
 	
 			boolean[] old = { true, true, true, true, true, true, true };
 			boolean[] newdays = { true, true, false, true, true, true, true };
 	
 			Debug.d("old: " + Arrays.toString(old));
 			Debug.d("newdays: " + Arrays.toString(newdays));
 			
 			
 			
 			passed = (Arrays.equals(old, (boolean[])evt.getOldValue()));
 	    	if(!passed)
 	    		return;
 	    	
 	    	passed = (Arrays.equals(newdays, alarm.getEnabledDays()));
 	    	
 		}
 		
 	}
 	
 	public void testSetEnabledDays() {
 		Subscriber5 sub = new Subscriber5();
 
 		MessageBus<Message> bus = new MessageBus<Message>();
 		bus.subscribe( sub );
 
 		sub.alarm.setMessageBus(bus);
 		boolean[] enabledDays = { true, true, false, true, true, true, true };
 		
 		sub.alarm.setEnabledDays(enabledDays);
 		assertTrue(sub.passed);
 		
 		// test for a too long array.
 		Alarm alarm = new Alarm(1,2);
 		boolean[] enabledDays2 = { true, true, true, true, true, true, true, true };
 		
 		// invalid minute
 		try {
 			alarm.setEnabledDays(enabledDays2);
 			Assert.fail("Should have thrown IllegalArgumentException");
 		} catch (IllegalArgumentException e) {
 			// success
 		}
 	}
 	
 	public void testCopyContructor() {
 		Alarm alarm = new Alarm(8, 30);
 		Alarm copy = new Alarm(alarm);
 		
 		// Check some properties of Alarm
 		// More fields could be checked as they are added
 		assertEquals(alarm.getHour(), copy.getHour());
 		assertEquals(alarm.getMinute(), copy.getMinute());
 		assertEquals(alarm.getName(), copy.getName());
 		assertTrue(Arrays.equals(alarm.getEnabledDays(), copy.getEnabledDays()));
 	}
 	
 	public void testGetTimeString() {
 		Alarm alarm = new Alarm(5, 5);
 		assertEquals("05:05", alarm.getTimeString());
 		
 		alarm = new Alarm(10, 5);
 		assertEquals("10:05", alarm.getTimeString());
 		
 		alarm = new Alarm(5, 10);
 		assertEquals("05:10", alarm.getTimeString());
 		
 		alarm = new Alarm(13, 13);
 		assertEquals("13:13", alarm.getTimeString());
 	}
 	
 	public void testSetUnnamedPlacement() {
 		Alarm alarm = new Alarm();
 		alarm.setName( "eric" );
 		try {
 		
 			// alarm is actually named, so we want an exception.
 			alarm.setUnnamedPlacement(3);
 			Assert.fail("Should have thrown IllegalArgumentException");
 			
 		} catch (IllegalArgumentException e) {
 			// success
 		}
 		alarm = new Alarm();
 		alarm.setUnnamedPlacement(3);
 		assertEquals(3, alarm.getUnnamedPlacement());
 	}
 
 	public class Subscriber6 {
 
 		public boolean passed = false;
 		public Alarm alarm = new Alarm(1, 2);
 
 		@Handler
 		public void handleMetaChange(ScheduleChangeEvent evt) {
 			passed = (evt.getModifiedField() == Alarm.Field.REPEATING)
 					&& (alarm == evt.getAlarm()) && alarm.isRepeating() == true
 					&& (Boolean) evt.getOldValue() == false;
 		}
 	}
 	
 	public void testSetRepeat() {
 		Subscriber6 sub = new Subscriber6();
 		
 		MessageBus<Message> bus = new MessageBus<Message>();
 		bus.subscribe( sub );
 		sub.alarm.setMessageBus(bus);
 
 		// now test message.
 		sub.alarm.setRepeat(false);
 		sub.alarm.setRepeat(true);
 		
 		assertTrue(sub.passed);
 		
 	}
 }
