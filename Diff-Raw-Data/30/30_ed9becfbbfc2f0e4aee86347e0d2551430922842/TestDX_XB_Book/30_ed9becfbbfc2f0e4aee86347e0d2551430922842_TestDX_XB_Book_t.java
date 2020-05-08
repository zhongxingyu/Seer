 /**
  * Copyright (C) 2011-2012 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.feed.ddf.message.provider;
 
 import static com.barchart.feed.api.model.data.Book.Type.*;
 import static com.barchart.feed.base.book.enums.MarketBookAction.MODIFY;
 import static com.barchart.util.common.ascii.ASCII.ASCII_CHARSET;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.barchart.feed.api.model.data.Book;
 import com.barchart.feed.base.book.api.MarketBookEntry;
 import com.barchart.feed.base.provider.DefBookEntry;
 import com.barchart.feed.base.values.provider.ValueBuilder;
 import com.barchart.feed.ddf.message.enums.DDF_MessageType;
 import com.barchart.feed.ddf.message.enums.DDF_Session;
 import com.barchart.feed.ddf.message.enums.DDF_TradeDay;
 import com.barchart.feed.ddf.symbol.enums.DDF_Exchange;
 import com.barchart.feed.ddf.symbol.enums.DDF_SpreadType;
 import com.barchart.feed.ddf.util.enums.DDF_Fraction;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class TestDX_XB_Book.
  */
 public class TestDX_XB_Book extends TestDDFBase {
 
 	/**
 	 * Sets the up.
 	 * 
 	 * @throws Exception
 	 *             the exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	/**
 	 * Tear down.
 	 * 
 	 * @throws Exception
 	 *             the exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	// XB spread
 	final byte[] baXBsp = "%<BOOK askcount=\"10\" askprices=\"110000,110025,110050,110075,110100,110125,110150,110175,110200,110225\" asksizes=\"247,794,646,868,811,1049,830,1244,2287,694\" basecode=\"A\" bidcount=\"10\" bidprices=\"109975,109950,109925,109900,109875,109850,109825,109800,109775,109750\" bidsizes=\"162,582,692,698,953,680,819,931,958,1166\" symbol=\"ESM0_ESZ0\"/>"
 			.getBytes(ASCII_CHARSET);
 
 	/**
 	 * Test decode spread xml.
 	 */
 	@Test
 	public void testDecodeSpreadXML() {
 
 		final DX_XB_Book msg = new DX_XB_Book();
 
 		final ByteBuffer buffer = ByteBuffer.wrap(baXBsp);
 
 		buffer.get(); // position + 1
 
 		msg.decodeXML(buffer);
 
 		assertEquals(msg.getId(), "ESM0_ESZ0");
 		assertEquals(msg.getSpreadType(), DDF_SpreadType.DEFAULT);
 		assertEquals(msg.getFraction(), DDF_Fraction.N2);
 		assertEquals(msg.getExchange(), DDF_Exchange.UNKNOWN);
 		assertEquals(msg.getMessageType(), DDF_MessageType.BOOK_SNAP_XML);
 		assertEquals(msg.getSession(), DDF_Session.FUT_COMBO);
 		assertEquals(msg.getDelay(), 0);
 
 	}
 
 	// XB
 	final byte[] baXB = "%<BOOK askcount=\"10\" askprices=\"110000,110025,110050,110075,110100,110125,110150,110175,110200,110225\" asksizes=\"247,794,646,868,811,1049,830,1244,2287,694\" basecode=\"A\" bidcount=\"10\" bidprices=\"109975,109950,109925,109900,109875,109850,109825,109800,109775,109750\" bidsizes=\"162,582,692,698,953,680,819,931,958,1166\" symbol=\"ESM0\"/>"
 			.getBytes(ASCII_CHARSET);
 
 	/**
 	 * Test decode xml.
 	 */
 	@Test
 	public void testDecodeXML() {
 
 		final DX_XB_Book msg = new DX_XB_Book();
 		MarketBookEntry entry;
 
 		final ByteBuffer buffer = ByteBuffer.wrap(baXB);
 
 		buffer.get(); // position + 1
 
 		msg.decodeXML(buffer);
 
 		assertEquals(msg.getId(), "ESM0");
 		assertEquals(msg.getSpreadType(), DDF_SpreadType.UNKNOWN);
 		assertEquals(msg.getFraction(), DDF_Fraction.N2);
 		assertEquals(msg.getExchange(), DDF_Exchange.UNKNOWN);
 		assertEquals(msg.getMessageType(), DDF_MessageType.BOOK_SNAP_XML);
		assertEquals(msg.getTradeDay(), DDF_TradeDay.D01);
 		assertEquals(msg.getSession(), DDF_Session.FUT_COMBO);
 		assertEquals(msg.getDelay(), 0);
 
 		//
 
 		final MarketBookEntry[] entries = msg.entries();
 
 		assertEquals(20, entries.length);
 
 		/*
 		 * bidprices=\
 		 * "109975,109950,109925,109900,109875,109850,109825,109800,109775,109750\"
 		 * 
 		 * bidsizes=\"162,582,692,698,953,680,819,931,958,1166\"
 		 */
 		entry = new DefBookEntry(MODIFY, Book.Side.BID, DEFAULT, 1,
 				ValueBuilder.newPrice(109975, -2), ValueBuilder.newSize(162));
 		assertEquals(entry, entries[0]);
 		entry = new DefBookEntry(MODIFY, Book.Side.BID, DEFAULT, 2,
 				ValueBuilder.newPrice(109950, -2), ValueBuilder.newSize(582));
 		assertEquals(entry, entries[1]);
 		entry = new DefBookEntry(MODIFY, Book.Side.BID, DEFAULT, 3,
 				ValueBuilder.newPrice(109925, -2), ValueBuilder.newSize(692));
 		assertEquals(entry, entries[2]);
 		entry = new DefBookEntry(MODIFY, Book.Side.BID, DEFAULT, 4,
 				ValueBuilder.newPrice(109900, -2), ValueBuilder.newSize(698));
 		assertEquals(entry, entries[3]);
 		entry = new DefBookEntry(MODIFY, Book.Side.BID, DEFAULT, 5,
 				ValueBuilder.newPrice(109875, -2), ValueBuilder.newSize(953));
 		assertEquals(entry, entries[4]);
 		entry = new DefBookEntry(MODIFY, Book.Side.BID, DEFAULT, 6,
 				ValueBuilder.newPrice(109850, -2), ValueBuilder.newSize(680));
 		assertEquals(entry, entries[5]);
 		entry = new DefBookEntry(MODIFY, Book.Side.BID, DEFAULT, 7,
 				ValueBuilder.newPrice(109825, -2), ValueBuilder.newSize(819));
 		assertEquals(entry, entries[6]);
 		entry = new DefBookEntry(MODIFY, Book.Side.BID, DEFAULT, 8,
 				ValueBuilder.newPrice(109800, -2), ValueBuilder.newSize(931));
 		assertEquals(entry, entries[7]);
 		entry = new DefBookEntry(MODIFY, Book.Side.BID, DEFAULT, 9,
 				ValueBuilder.newPrice(109775, -2), ValueBuilder.newSize(958));
 		assertEquals(entry, entries[8]);
 		entry = new DefBookEntry(MODIFY, Book.Side.BID, DEFAULT, 10,
 				ValueBuilder.newPrice(109750, -2), ValueBuilder.newSize(1166));
 		assertEquals(entry, entries[9]);
 
 		/*
 		 * askprices=\
 		 * "110000,110025,110050,110075,110100,110125,110150,110175,110200,110225\"
 		 * 
 		 * asksizes=\"247,794,646,868,811,1049,830,1244,2287,694\"
 		 */
 		entry = new DefBookEntry(MODIFY, Book.Side.ASK, DEFAULT, 1,
 				ValueBuilder.newPrice(110000, -2), ValueBuilder.newSize(247));
 		assertEquals(entry, entries[10]);
 		entry = new DefBookEntry(MODIFY, Book.Side.ASK, DEFAULT, 2,
 				ValueBuilder.newPrice(110025, -2), ValueBuilder.newSize(794));
 		assertEquals(entry, entries[11]);
 		entry = new DefBookEntry(MODIFY, Book.Side.ASK, DEFAULT, 3,
 				ValueBuilder.newPrice(110050, -2), ValueBuilder.newSize(646));
 		assertEquals(entry, entries[12]);
 		entry = new DefBookEntry(MODIFY, Book.Side.ASK, DEFAULT, 4,
 				ValueBuilder.newPrice(110075, -2), ValueBuilder.newSize(868));
 		assertEquals(entry, entries[13]);
 		entry = new DefBookEntry(MODIFY, Book.Side.ASK, DEFAULT, 5,
 				ValueBuilder.newPrice(110100, -2), ValueBuilder.newSize(811));
 		assertEquals(entry, entries[14]);
 		entry = new DefBookEntry(MODIFY, Book.Side.ASK, DEFAULT, 6,
 				ValueBuilder.newPrice(110125, -2), ValueBuilder.newSize(1049));
 		assertEquals(entry, entries[15]);
 		entry = new DefBookEntry(MODIFY, Book.Side.ASK, DEFAULT, 7,
 				ValueBuilder.newPrice(110150, -2), ValueBuilder.newSize(830));
 		assertEquals(entry, entries[16]);
 		entry = new DefBookEntry(MODIFY, Book.Side.ASK, DEFAULT, 8,
 				ValueBuilder.newPrice(110175, -2), ValueBuilder.newSize(1244));
 		assertEquals(entry, entries[17]);
 		entry = new DefBookEntry(MODIFY, Book.Side.ASK, DEFAULT, 9,
 				ValueBuilder.newPrice(110200, -2), ValueBuilder.newSize(2287));
 		assertEquals(entry, entries[18]);
 		entry = new DefBookEntry(MODIFY, Book.Side.ASK, DEFAULT, 10,
 				ValueBuilder.newPrice(110225, -2), ValueBuilder.newSize(694));
 		assertEquals(entry, entries[19]);
 
 	}
 
 	/**
 	 * Test decode encode xml.
 	 */
 	@Test
 	public void testDecodeEncodeXML() {
 
 		final DX_XB_Book msg = new DX_XB_Book();
 
 		final ByteBuffer source = ByteBuffer.wrap(baXB);
 
 		source.get(); // position + 1
 
 		msg.decodeXML(source);
 
 		final ByteBuffer target = ByteBuffer.allocate(baXB.length);
 
 		target.put((byte) '%'); // position + 1
 
 		msg.encodeXML(target);
 
 		// System.out.println(new String(target.array()));
 
 		final byte[] arraySource = source.array();
 		final byte[] arrayTarget = target.array();
 
 		// System.out.println("source=" + new String(arraySource));
 		// System.out.println("target=" + new String(arrayTarget));
 
 		assertTrue(Arrays.equals(arraySource, arrayTarget));
 
 	}
 
 	static MarketBookEntry newEntry(final Book.Side side, final int place,
 			final long mant, final int exp, final int size) {
 		return new DefBookEntry(MODIFY, side, DEFAULT, place,
 				ValueBuilder.newPrice(mant, exp), ValueBuilder.newSize(size));
 	}
 
 	/**
 	 * Test to string.
 	 */
 	@Test
 	public void testToString() {
 
 		final DX_XB_Book msg = new DX_XB_Book();
 
 		System.out.println(msg);
 
 		final String string = msg.toString();
 
 		final String result = "<BOOK askcount=\"0\" askprices=\"\" asksizes=\"\" basecode=\"?\" bidcount=\"0\" bidprices=\"\" bidsizes=\"\" symbol=\"\"/>";
 
 		assertEquals(string, result);
 
 	}
 
 	final static byte[] baXB_1 = "%<BOOK askcount=\"10\" askprices=\"117025,117050,117075,117100,117125,117150,117175,117200,117225,117250\" asksizes=\"389,798,1356,1382,1251,2036,2009,1628,2234,1335\" basecode=\"A\" bidcount=\"10\" bidprices=\"117000,116975,116950,116925,116900,116875,116850,116825,116800,116775\" bidsizes=\"1895,1761,1744,1207,1589,221,367,268,570,167\" symbol=\"ESZ1\"/>"
 			.getBytes();
 
 	/**
 	 * Test to string_1.
 	 */
 	@Test
 	public void testToString_1() {
 
 		final DX_XB_Book msg = new DX_XB_Book();
 
 		final ByteBuffer buffer = ByteBuffer.wrap(baXB_1);
 
 		buffer.get(); // position + 1
 
 		msg.decodeXML(buffer);
 
 		msg.toString();
 
 	}
 
 	/**
 	 * Test type.
 	 */
 	@Test
 	public void testType() {
 
 		final Base message = DDF_MessageService.newInstance(new DX_XB_Book()
 				.getMessageType());
 
 		assertTrue(message instanceof DX_XB_Book);
 
 	}
 
 	final static byte[] baXB_empty = "%<BOOK symbol=\"KCZ1\" basecode=\"A\" askcount=\"0\" bidcount=\"0\"/>"
 			.getBytes();
 
 	/**
 	 * Test empty.
 	 */
 	@Test
 	public void testEmpty() {
 
 		final DX_XB_Book msg = new DX_XB_Book();
 
 		final ByteBuffer buffer = ByteBuffer.wrap(baXB_empty);
 
 		buffer.get(); // position + 1
 
 		msg.decodeXML(buffer);
 
 	}
 
 }
