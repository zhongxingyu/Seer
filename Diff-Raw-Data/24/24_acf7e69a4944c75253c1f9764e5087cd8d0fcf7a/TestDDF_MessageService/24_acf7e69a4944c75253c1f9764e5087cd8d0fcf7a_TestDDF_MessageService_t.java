 /**
  * Copyright (C) 2011-2012 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.feed.ddf.message.provider;
 
 import static com.barchart.util.ascii.ASCII.ASCII_CHARSET;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.barchart.feed.ddf.message.api.DDF_BaseMessage;
 import com.barchart.feed.ddf.message.api.DDF_MarketBook;
 import com.barchart.feed.ddf.message.api.DDF_MarketBookTop;
 import com.barchart.feed.ddf.message.api.DDF_MarketCuvol;
 import com.barchart.feed.ddf.message.api.DDF_MarketParameter;
 import com.barchart.feed.ddf.message.api.DDF_MarketQuote;
 import com.barchart.feed.ddf.message.api.DDF_MarketSnapshot;
 import com.barchart.feed.ddf.message.api.DDF_MarketTrade;
 import com.barchart.feed.ddf.util.provider.DDF_ClearVal;
 import com.barchart.feed.ddf.util.provider.DDF_NulVal;
 import com.barchart.util.values.provider.ValueConst;
 
 /**
  * The Class TestDDF_MessageService.
  */
 public class TestDDF_MessageService {
 
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
 
 	// 20
 	final static byte[] ba20 = "2SF0,02B1010533,D0Q "
 			.getBytes(ASCII_CHARSET);
 
 	// 21
 	final static byte[] ba21 = "2SF0,12B10,,,,,-,-,,,,,,,,,Q "
 			.getBytes(ASCII_CHARSET);
 
 	// 22
 	final static byte[] ba22 =
 			"2HIG,2AN15,2445,2604,2404,2582,,,,2481,,,2582,,,12196949,3 "
 					.getBytes(ASCII_CHARSET);
 
 	// 26
 	final static byte[] ba26 =
 			"2$DJUBSSO,6Ao10,5515,5642,5501,5631,,,,,,,,,,,S "
 					.getBytes(ASCII_CHARSET);
 
 	final static byte[] ba27 = "2HOZ9,7CJ1021371,5,SG"
 			.getBytes(ASCII_CHARSET);
 
 	// 28
 	final byte[] ba28 = "2HOZ9,8CJ1020911,5,20919,1,SG"
 			.getBytes(ASCII_CHARSET);
 
 	// 3B
 	final byte[] ba3B =
 			"3XIZ9,BBX55,63795K25,63790L5,63780M1000,63775N35,63765O5,63800J20,63815I5,63820H10,63825G5,63830F7"
 					.getBytes(ASCII_CHARSET);
 
 	// XB
 	final byte[] baXB =
 			"%<BOOK askcount=\"10\" askprices=\"110000,110025,110050,110075,110100,110125,110150,110175,110200,110225\" asksizes=\"247,794,646,868,811,1049,830,1244,2287,694\" basecode=\"A\" bidcount=\"10\" bidprices=\"109975,109950,109925,109900,109875,109850,109825,109800,109775,109750\" bidsizes=\"162,582,692,698,953,680,819,931,958,1166\" symbol=\"ESM0\"/>"
 					.getBytes(ASCII_CHARSET);
 
 	// XC
 	final byte[] baXC =
			"%<CV symbol=\"GEM0\" basecode=\"A\" tickincrement=\"25\" last=\"109975\" lastsize=\"2\" lastcvol=\"12\" date=\"20100615123857\" count=\"54\" data=\"109275,1076:108900,437:109950,3813:109325,1429:108950,899:109925,6629:109875,5131:109225,1007:109800,5046:109125,1327:109075,1865:109750,5931:110050,2579:109025,1616:109975,2886:109200,1619:109550,875:109300,2010:109525,1691:109375,1504:109850,5318:109450,1163:109500,1899:109000,1906:110150,209:109625,2055:108850,38:109700,3787:110175,219:109350,1901:108975,476:109600,3659:109575,1799:110025,3751:110075,3451:110000,5387:109825,5224:109900,10427:109775,4809:109650,2887:109050,1817:109725,4396:109175,1807:109100,1835:108875,110:109400,1777:109425,1617:109150,1662:109475,938:110125,446:109250,1215:110100,2035:108925,751:109675,2493\"/>"
 					.getBytes(ASCII_CHARSET);
 
 	// XQ
 	final byte[] baXQ =
			"%<QUOTE symbol=\"GEM0\" name=\"E-Mini S&amp;P 500\" exchange=\"GBLX\" basecode=\"A\" pointvalue=\"50.0\" tickincrement=\"25\" ddfexchange=\"M\" lastupdate=\"20100615144110\" bid=\"109975\" bidsize=\"162\" ask=\"110000\" asksize=\"248\" mode=\"R\"><SESSION day=\"E\" session=\"G\" timestamp=\"20100615094112\" open=\"109025\" high=\"110175\" low=\"108850\" last=\"109975\" previous=\"109050\" tradesize=\"2\" volume=\"362318\" tradetime=\"20100615094111\" id=\"combined\"/><SESSION day=\"D\" session=\"G\" timestamp=\"20100614205606\" open=\"109050\" high=\"110600\" low=\"108875\" last=\"109050\" previous=\"108925\" tradesize=\"1\" openinterest=\"1482925\" volume=\"887313\" tradetime=\"20100614151457\" id=\"previous\"/></QUOTE>"
 					.getBytes(ASCII_CHARSET);
 
 	/**
 	 * Test decode.
 	 * 
 	 * @throws Exception
 	 *             the exception
 	 */
 	@Test
 	public void testDecode() throws Exception {
 
 		DDF_BaseMessage msg;
 
 		msg = DDF_MessageService.decode(ba20);
 		assertTrue(msg instanceof DDF_MarketParameter);
 
 		msg = DDF_MessageService.decode(ba21);
 		assertTrue(msg instanceof DDF_MarketSnapshot);
 
 		msg = DDF_MessageService.decode(ba22);
 		assertTrue(msg instanceof DDF_MarketSnapshot);
 
 		msg = DDF_MessageService.decode(ba26);
 		assertTrue(msg instanceof DDF_MarketSnapshot);
 
 		msg = DDF_MessageService.decode(ba27);
 		assertTrue(msg instanceof DDF_MarketTrade);
 
 		msg = DDF_MessageService.decode(ba28);
 		assertTrue(msg instanceof DDF_MarketBookTop);
 
 		msg = DDF_MessageService.decode(ba3B);
 		assertTrue(msg instanceof DDF_MarketBook);
 
 		msg = DDF_MessageService.decode(baXB);
 		assertTrue(msg instanceof DDF_MarketBook);
 
 		msg = DDF_MessageService.decode(baXC);
 		assertTrue(msg instanceof DDF_MarketCuvol);
 
 		msg = DDF_MessageService.decode(baXQ);
 		assertTrue(msg instanceof DDF_MarketQuote);
 
 	}
 
 	/**
 	 * Test constants.
 	 * 
 	 * @throws Exception
 	 *             the exception
 	 */
 	@Test
 	public void testConstants() throws Exception {
 
 		assertTrue(DDF_ClearVal.DECIMAL_CLEAR != DDF_NulVal.DECIMAL_EMPTY);
 		assertTrue(DDF_ClearVal.PRICE_CLEAR != DDF_NulVal.PRICE_EMPTY);
 		assertTrue(DDF_ClearVal.SIZE_CLEAR != DDF_NulVal.SIZE_EMPTY);
 		assertTrue(DDF_ClearVal.TIME_CLEAR != DDF_NulVal.TIME_EMPTY);
 
 		assertTrue(DDF_ClearVal.DECIMAL_CLEAR != ValueConst.NULL_DECIMAL);
 		assertTrue(DDF_ClearVal.PRICE_CLEAR != ValueConst.NULL_PRICE);
 		assertTrue(DDF_ClearVal.SIZE_CLEAR != ValueConst.NULL_SIZE);
 		assertTrue(DDF_ClearVal.TIME_CLEAR != ValueConst.NULL_TIME);
 
 		assertTrue(DDF_NulVal.DECIMAL_EMPTY != ValueConst.NULL_DECIMAL);
 		assertTrue(DDF_NulVal.PRICE_EMPTY != ValueConst.NULL_PRICE);
 		assertTrue(DDF_NulVal.SIZE_EMPTY != ValueConst.NULL_SIZE);
 		assertTrue(DDF_NulVal.TIME_EMPTY != ValueConst.NULL_TIME);
 
 	}
 
 }
