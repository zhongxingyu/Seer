 package org.epstudios.morbidmeter.test;
 
 import junit.framework.TestCase;
 
import org.epstudios.morbidmeter.MorbidMeter;
 
 public class NotificationTest extends TestCase {
 
 	public NotificationTest(String name) {
 		super(name);
 	}
 
 	public void testIsMilestone() {
		assert (MorbidMeter.isEvenHour("Jan 4, 2012 5:00:03 AM 414 msec"));
		assert (!MorbidMeter.isEvenHour("Jan 4, 2012 5:01:03 AM 414 msec"));
		assert (!MorbidMeter.isEvenHour("Jan 4, 2012 5:01:00 AM 414 msec"));
		assert (MorbidMeter.isEvenMinute("5:00:00 PM"));
		assert (!MorbidMeter.isEvenMinute("4:01:01"));
		assert (MorbidMeter.isEvenPercentage("99.01111"));
		assert (!MorbidMeter.isEvenPercentage("99.100000"));
		assert (MorbidMeter.isEvenMillion("5,000,011 years"));
		assert (MorbidMeter.isEvenMillion("12,000,101,000,111 years"));
		assert (!MorbidMeter.isEvenMillion("14,001,001,000 years"));
 		// test with carriage returns
		assert (MorbidMeter.isEvenHour("Jan 4\n 2012\n 1:00:00 PM 333 msec"));
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 }
