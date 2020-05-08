 /*
 * Copyright (c) 2013 Daniël W. Crompton <info+straw@specialbrands.net>
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 /**
  * 
  */
 package net.specialbrands.strawberry;
 
 import static org.junit.Assert.assertEquals;
 
 import java.text.ParseException;
 import java.util.Date;
 
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author danielcrompton
  * 
  */
 public class BusinessHourCalculatorTest {
 	Logger logger = LoggerFactory.getLogger(getClass());
 	private BusinessHourCalculator businessHourCalculator;
 
 	/**
 	 * 
 	 */
 	public BusinessHourCalculatorTest() {
 		businessHourCalculator = new BusinessHourCalculator("09:00", "15:00");
 		businessHourCalculator.setOpeningHours(DayOfWeek.FRIDAY, "10:00",
 				"17:00");
 		businessHourCalculator.setOpeningHours("2010-12-24", "8:00", "13:00");
 		businessHourCalculator.setClosed(DayOfWeek.SUNDAY, DayOfWeek.WEDNESDAY);
 		businessHourCalculator.setClosed("2010-12-25");
 
 	}
 
 	@Test
 	public void testExampleOne() {
 		Date expected = new Date("Mon Jun 07 11:10:00 2010");
 		Date actual = null;
 		try {
 			actual = businessHourCalculator.calculateDeadline(2 * 60 * 60,
 					"2010-06-07 09:10");
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		logger.debug("{} <----", expected.toGMTString());
 		assertEquals(expected, actual);
 		logger.debug("==========================================");
 	}
 
 	@Test
 	public void testExampleTwo() {
 		Date expected = new Date("Thu Jun 10 09:03:00 2010");
 		Date actual = null;
 		try {
 			actual = businessHourCalculator.calculateDeadline(15 * 60,
 					"2010-06-08 14:48");
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		logger.debug("{} <----", expected.toGMTString());
 		assertEquals(expected, actual);
 		logger.debug("==========================================");
 	}
 
 	@Test
 	public void testExampleThree() {
 		Date expected = new Date("Mon Dec 27 11:00:00 2010");
 		Date actual = null;
 		try {
 			actual = businessHourCalculator.calculateDeadline(7 * 60 * 60,
 					"2010-12-24 6:45");
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		logger.debug("{} <----", expected.toGMTString());
 		assertEquals(expected, actual);
 		logger.debug("==========================================");
 	}
 
 	@Test
 	public void testSetOpenClose() {
 		OpeningHours open = new OpeningHours("09:11", "15:23");
 		assertEquals(open.getOpen()[0], "09");
 		assertEquals(open.getOpen()[1], "11");
 		assertEquals(open.getClose()[0], "15");
 		assertEquals(open.getClose()[1], "23");
 	}
 }
