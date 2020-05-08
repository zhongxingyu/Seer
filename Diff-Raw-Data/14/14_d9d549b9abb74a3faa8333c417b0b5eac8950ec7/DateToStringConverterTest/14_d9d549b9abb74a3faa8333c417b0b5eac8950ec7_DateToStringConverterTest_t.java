 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets.databinding;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 import junit.framework.TestCase;
 
 import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
 
 /**
  * Tests for the classes {@link DateToStringConverter} and
  * {@link StringToDateConverter}.
  */
 @NonUITestCase
 public class DateToStringConverterTest extends TestCase {
 
 	public void testConvertToString() {
 		final DateToStringConverter converter = new DateToStringConverter("MM/dd/yyyy");
 
 		Object result = converter.convert(null);
 		assertEquals(null, result);
 
 		result = converter.convert(localize(0L));
 		assertEquals("01/01/1970", result); //$NON-NLS-1$
 
 		final Date date = localize(1221011682194L);
 		result = converter.convert(date);
 		assertEquals("09/10/2008", result);
 	}
 
 	public void testConvertToDateNoTimezone() {
 		final StringToDateConverter converter = new StringToDateConverter("MM/dd/yyyy");
 
 		Date result = (Date) converter.convert(null);
 		assertEquals(null, result);
 
 		result = (Date) converter.convert("01/01/1970");
 		@SuppressWarnings("deprecation")
 		final int offset = result.getTimezoneOffset() * 60 * 1000;
 		assertEquals(0 + offset, result.getTime());
 	}
 
 	public void testConvertToDateGMT() {
 		final StringToDateConverter converter = new StringToDateConverter("MM/dd/yyyy zzz");
 
 		Date result = (Date) converter.convert(null);
 		assertEquals(null, result);
 
 		result = (Date) converter.convert("01/01/1970 GMT");
 		assertEquals(0, result.getTime());
 	}
 
 	public void testConvertToDateGMTPlusTwo() {
 		final StringToDateConverter converter = new StringToDateConverter("MM/dd/yyyy zzzzz");
 
 		Date result = (Date) converter.convert(null);
 		assertEquals(null, result);
 
 		result = (Date) converter.convert("01/01/1970 +0200");
 		assertEquals(3600000 * -2, result.getTime());
 	}
 
 	public void testConvertToDateExact() {
 		final StringToDateConverter converter = new StringToDateConverter("MM/dd/yyyy HH:mm:ss:SSS zzz");
 
 		final Date result1 = (Date) converter.convert("07/10/1996 16:05:28:046 PDT");
 		assertEquals(837039928046L, result1.getTime());
 
 		final Date result2 = (Date) converter.convert("01/01/1970 00:00:00:000 GMT");
 		assertEquals(0L, result2.getTime());
 	}
 
 	public void testToStringAndBack() {
 		final DateToStringConverter toString = new DateToStringConverter("MM/dd/yyyy");
 		final StringToDateConverter toDate = new StringToDateConverter("MM/dd/yyyy");
 
 		final Date input = localize(0L);
 		final Date result = (Date) toDate.convert(toString.convert(input));
 		assertEquals(input.getTime(), result.getTime());
 	}
 
 	public void testToDateAndBack() {
 		final DateToStringConverter toString = new DateToStringConverter("MM/dd/yyyy");
 		final StringToDateConverter toDate = new StringToDateConverter("MM/dd/yyyy");
 
 		final String result = (String) toString.convert(toDate.convert("01/01/1970"));
 		assertEquals("01/01/1970", result);
 	}
 
 	public void testDateToStringLocal1() {
 		final Calendar calendarInstance = Calendar.getInstance();
 		calendarInstance.set(2010, 10, 12, 0, 0, 0);
 		calendarInstance.setTimeZone(TimeZone.getDefault());
 		final Date date = calendarInstance.getTime();
 
 		final DateToStringConverter dateToStringConverter = new DateToStringConverter("dd.MM.yyyy HH:mm:ss");
 		final String dateAsString = (String) dateToStringConverter.convert(date);
 		assertEquals("12.11.2010 00:00:00", dateAsString);
 	}
 
 	public void testDateToStringLocal2() {
 		final Calendar calendarInstance = Calendar.getInstance();
 		calendarInstance.set(2010, 10, 12, 0, 0, 0);
 		calendarInstance.setTimeZone(TimeZone.getDefault());
 		final Date date = calendarInstance.getTime();
 
		final DateToStringConverter dateToStringConverter = new DateToStringConverter("dd.MM.yyyy HH:mm:ss"); //$NON-NLS-1$
 		final String dateAsString = (String) dateToStringConverter.convert(date);
		//		if ("America/Los_Angeles".equals(TimeZone.getDefault().getID())) { //$NON-NLS-1$
		//			assertEquals("12.11.2010 00:00:00 PST", dateAsString);
		//		} else {
		//			assertEquals("12.11.2010 00:00:00 CET", dateAsString);
		//		}
		assertEquals("12.11.2010 00:00:00", dateAsString);
 	}
 
 	// helping methods
 	//////////////////
 
 	@SuppressWarnings("deprecation")
 	private Date localize(final long msSinceEpochUtc) {
 		final Date localDate = new Date(msSinceEpochUtc);
 		return new Date(localDate.getTime() + (60 * 1000 * localDate.getTimezoneOffset()));
 	}
 }
