 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 
 public class SingleEventTest {
	private void testCase0() {
		String expectedEventName = "Swimming";
		String expectedEventPlace = "Chun Jian";
		String expectedEventTime = "2012-3-3";
		float expectedEventCost = 30.0f;
		float expectedEventAvgCost = 0.0f;
		int expectedTotalAttendee = 0;
		
		Event event = new Event(expectedEventName, expectedEventPlace, expectedEventTime, expectedEventCost);
		
		Assert.assertEquals(expectedEventName, event.getName());
		Assert.assertEquals(expectedEventPlace, event.getPlace());
		Assert.assertEquals(expectedEventTime, event.getDate());
		Assert.assertEquals(expectedEventCost, event.getTotalCost());
		
		Assert.assertEquals(expectedEventAvgCost, event.getAvgCost());
		Assert.assertEquals(expectedTotalAttendee, event.getTotalNumberOfAttendee());	
	}
	
 	private void testCase1() {
 		Event event1 = new Event("FB", "KFC", "2012-3-1", 30);		
 		Attendee ds = new Attendee("Damon Song");
 		Attendee az = new Attendee("Alain Zhu");
 		Attendee dd = new Attendee("David Dong");
 		final float expectedTotalCost = 30.0f;
 		final float expectedAvgCost = 10.0f;
 		final int expectedNumberOfAttendee = 3;
 		final String expectedWhoPaid = "Alain Zhu";
 		Record rc_ds = new Record();
 		Record rc_az = new Record();
 		Record rc_dd = new Record();
 		
 		rc_ds.setAttendee(ds);
 		rc_ds.setPaidMoney(-10.0f);
 		rc_az.setAttendee(az);
 		rc_az.setPaidMoney(30.0f);
 		rc_dd.setAttendee(dd);
 		rc_dd.setPaidMoney(-10.0f);
 		
 		float actualTotalCost = 0.0f;
 		float actualAvgCost = 0.0f;
 		int actualNumberOfAttendee = 0;
 		String actualWhoPaid = "Nobody";
 		int index;
 		
 		
 		event1.AddRecord(ds, 0, 0);
 		event1.AddRecord(az, 0, 30);
 		event1.AddRecord(dd, 0, 0);
 		
 		ds.summaryAll();
 		az.summaryAll();
 		dd.summaryAll();
 		
 		actualTotalCost = event1.getTotalCost();
 		actualAvgCost = event1.getAvgCost();
 		actualNumberOfAttendee = event1.getTotalNumberOfAttendee();
 		actualWhoPaid = event1.getWhoPaid();
 		
 		Assert.assertEquals(expectedTotalCost, actualTotalCost);
 		Assert.assertEquals(expectedAvgCost, actualAvgCost);
 		Assert.assertEquals(expectedNumberOfAttendee, actualNumberOfAttendee);
 		Assert.assertEquals(expectedWhoPaid, actualWhoPaid);
 		
 		Assert.assertEquals(3, event1.getRecordList().size());
 
 		Assert.assertEquals(rc_ds.getAttendee(), event1.getRecord(0).getAttendee());
 		Assert.assertEquals(rc_az.getAttendee(), event1.getRecord(1).getAttendee());
 		Assert.assertEquals(rc_dd.getAttendee(), event1.getRecord(2).getAttendee());
 		
 		String dsLord = ds.getWhoIsMyLord(event1);
 		String azLord = az.getWhoIsMyLord(event1);
 		String ddLord = dd.getWhoIsMyLord(event1);
 		String dsExpectedLord = "Alain Zhu";
 		String azExpectedLord = "self";
 		String ddExpectedLord = "Alain Zhu";
 		
 		Assert.assertEquals(dsExpectedLord, dsLord);
 		Assert.assertEquals(azExpectedLord, azLord);
 		Assert.assertEquals(ddExpectedLord, ddLord);
 		
 		int howManyPersonOwnDamonOnEvent1 = ds.getNumberOfWhoOwnMe(event1);
 		int howManyPersonOwnAlainOnEvent1 = az.getNumberOfWhoOwnMe(event1);
 		int howManyPersonOwnDavidOnEvent1 = dd.getNumberOfWhoOwnMe(event1);
 		final int expectedPersonNumberOwnDamon = 0;
 		final int expectedPersonNumberOwnAlain = 2;
 		final int expectedPersonNumberOwnDavid = 0;
 		
 		Assert.assertEquals(expectedPersonNumberOwnDamon, howManyPersonOwnDamonOnEvent1);
 		Assert.assertEquals(expectedPersonNumberOwnAlain, howManyPersonOwnAlainOnEvent1);
 		Assert.assertEquals(expectedPersonNumberOwnDavid, howManyPersonOwnDavidOnEvent1);
 
 		String OwnedList[] = {"Damon Song", "David Dong"};
 		final float expectedOwnedMoney = 10.0f;
 		
 		for(index = 0; index < howManyPersonOwnAlainOnEvent1; index++) {
 			Assert.assertEquals(OwnedList[index], az.getWhoOwnMe(event1, index));
 			Assert.assertEquals(expectedOwnedMoney, az.getShouldPayMe(event1, index));
 		}
 	}
 	
 
 	@Test
 	public void test() {
		testCase0();
 		testCase1();
 		//fail("Not yet implemented");
 	}
 
 }
