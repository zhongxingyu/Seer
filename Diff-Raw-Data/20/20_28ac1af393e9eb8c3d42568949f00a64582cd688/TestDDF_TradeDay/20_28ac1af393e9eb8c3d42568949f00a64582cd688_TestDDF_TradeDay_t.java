 /**
  * Copyright (C) 2011-2012 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.feed.ddf.message.enums;
 
 import static org.junit.Assert.*;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.barchart.feed.ddf.message.enums.DDF_TradeDay;
 import com.barchart.util.values.api.TimeValue;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class TestDDF_TradeDay.
  */
 public class TestDDF_TradeDay {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(TestDDF_TradeDay.class);
 
 	// same month past day
 	/**
 	 * Test trade date from0.
 	 */
 	@Test
 	public void testTradeDateFrom0() {
 
 		final DDF_TradeDay day = DDF_TradeDay.D15;
 
 		final DateTime today = new DateTime("2012-01-18T10:39:44.647-06:00");
 
 		final TimeValue value = DDF_TradeDay.tradeDateFrom(day, today);
 
 		final DateTime trade = value.asDateTime();
 
 		logger.debug("day=" + day);
 		logger.debug("today=" + today);
 		logger.debug("trade=" + trade);
 
 		assertEquals(trade.getYear(), 2012);
 		assertEquals(trade.getMonthOfYear(), 01);
 		assertEquals(trade.getDayOfMonth(), 15);
 
 	}
 
 	// same month next day
 	/**
 	 * Test trade date from1.
 	 */
 	@Test
 	public void testTradeDateFrom1() {
 
 		final DDF_TradeDay day = DDF_TradeDay.D15;
 
 		final DateTime today = new DateTime("2012-01-12T10:39:44.647-06:00");
 
 		final TimeValue value = DDF_TradeDay.tradeDateFrom(day, today);
 
 		final DateTime trade = value.asDateTime();
 
 		logger.debug("day=" + day);
 		logger.debug("today=" + today);
 		logger.debug("trade=" + trade);
 
 		assertEquals(trade.getYear(), 2012);
 		assertEquals(trade.getMonthOfYear(), 01);
 		assertEquals(trade.getDayOfMonth(), 15);
 
 	}
 
 	// past month past day
 	/**
 	 * Test trade date from2.
 	 */
 	@Test
 	public void testTradeDateFrom2() {
 
 		final DDF_TradeDay day = DDF_TradeDay.D30;
 
 		final DateTime today = new DateTime("2012-01-01T10:39:44.647-06:00");
 
 		final TimeValue value = DDF_TradeDay.tradeDateFrom(day, today);
 
 		final DateTime trade = value.asDateTime();
 
 		logger.debug("day=" + day);
 		logger.debug("today=" + today);
 		logger.debug("trade=" + trade);
 
 		assertEquals(trade.getYear(), 2011);
 		assertEquals(trade.getMonthOfYear(), 12);
 		assertEquals(trade.getDayOfMonth(), 30);
 
 	}
 
 	// next month next day
 	/**
 	 * Test trade date from3.
 	 */
 	@Test
 	public void testTradeDateFrom3() {
 
 		final DDF_TradeDay day = DDF_TradeDay.D03;
 
 		final DateTime today = new DateTime("2011-12-30T10:39:44.647-06:00");
 
 		final TimeValue value = DDF_TradeDay.tradeDateFrom(day, today);
 
 		final DateTime trade = value.asDateTime();
 
 		logger.debug("day=" + day);
 		logger.debug("today=" + today);
 		logger.debug("trade=" + trade);
 
 		assertEquals(trade.getYear(), 2012);
 		assertEquals(trade.getMonthOfYear(), 01);
 		assertEquals(trade.getDayOfMonth(), 03);
 
 	}
 
 	// same month same day
 	/**
 	 * Test trade date from5.
 	 */
 	@Test
 	public void testTradeDateFrom5() {
 
 		final DDF_TradeDay day = DDF_TradeDay.D18;
 
 		final DateTime today = new DateTime("2012-01-18T10:39:44.647-06:00");
 
 		final TimeValue value = DDF_TradeDay.tradeDateFrom(day, today);
 
 		final DateTime trade = value.asDateTime();
 
 		logger.debug("day=" + day);
 		logger.debug("today=" + today);
 		logger.debug("trade=" + trade);
 
 		assertEquals(trade.getYear(), 2012);
 		assertEquals(trade.getMonthOfYear(), 01);
 		assertEquals(trade.getDayOfMonth(), 18);
 
 	}
	
	@Test
	public void testTradeDateFrom6() {
		
		final DateTime today = new DateTime("2013-06-30T00:00:00.000");
		
		final TimeValue value = DDF_TradeDay.tradeDateFrom(DDF_TradeDay.D31, today);
		System.out.println(" " + value.toString());
		
	}
 
 }
