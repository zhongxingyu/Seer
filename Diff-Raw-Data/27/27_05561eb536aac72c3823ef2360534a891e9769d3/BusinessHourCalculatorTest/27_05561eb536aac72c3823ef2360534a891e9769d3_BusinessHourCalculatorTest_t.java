 package strawberries.drycleaner;
 
 import static org.junit.Assert.assertEquals;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import strawberries.drycleaner.BusinessHourCalculator.DayOfWeek;
 
 public class BusinessHourCalculatorTest {
 
 	private BusinessHourCalculator businessHourCalculator;
 	private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
 
 	@Before
 	public void classSetup() throws ParseException {
 		businessHourCalculator = new BusinessHourCalculator("09:00", "15:00");
 		businessHourCalculator.setOpeningHours(DayOfWeek.FRIDAY, "10:00","17:00");
 		businessHourCalculator.setClosed("2010-12-25");
 	}
 
 	@Test
 	public void testDeliverySameDay() throws ParseException {		
 		Date expectedVal = sdf.parse("07/06/2010 11:10");
 		Date retVal = businessHourCalculator.calculateDeadline(2*60*60, "2010-06-07 09:10");
 		assertEquals(expectedVal, retVal);
 	}
 	
 	@Test
	public void testDeliverySameDayDropOffAfterClosing() throws ParseException {		
		Date expectedVal = sdf.parse("08/06/2010 11:00");
		Date retVal = businessHourCalculator.calculateDeadline(2*60*60, "2010-06-07 18:10");
		assertEquals(expectedVal, retVal);
	}
	
	@Test
 	public void testDeliverySameDayDropOffBeforeOpening() throws ParseException {		
 		Date expectedVal = sdf.parse("07/06/2010 11:00");
 		Date retVal = businessHourCalculator.calculateDeadline(2*60*60, "2010-06-07 08:40");
 		assertEquals(expectedVal, retVal);
 	}
 
 	@Test
 	public void testDeliveryNextDay() throws ParseException {
 		businessHourCalculator.setClosed(DayOfWeek.SUNDAY, DayOfWeek.WEDNESDAY);	
 		Date expectedVal = sdf.parse("10/06/2010 09:03");
		Date retVal = businessHourCalculator.calculateDeadline(15*60, "2010-06-08 14:48");
 		assertEquals(expectedVal, retVal);
 	}
 
 	@Test
 	public void testLongerDeliveryTimeBecauseOfHoliday() throws ParseException {
 		businessHourCalculator.setOpeningHours("2010-12-24", "8:00", "13:00");
 		businessHourCalculator.setClosed(DayOfWeek.SUNDAY, DayOfWeek.WEDNESDAY,  DayOfWeek.MONDAY);
 		businessHourCalculator.setClosed("2010-12-25");
 		Date expectedVal = sdf.parse("28/12/2010 11:00");
 		Date retVal = businessHourCalculator.calculateDeadline(7 * 60 * 60,	"2010-12-24 6:45");
 		assertEquals(expectedVal, retVal);
 	}
 
 }
