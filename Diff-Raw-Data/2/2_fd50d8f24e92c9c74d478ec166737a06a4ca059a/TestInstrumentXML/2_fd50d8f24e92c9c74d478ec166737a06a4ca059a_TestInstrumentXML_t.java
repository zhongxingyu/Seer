 package com.barchart.feed.ddf.instrument.provider;
 
 import static com.barchart.feed.api.fields.InstrumentField.*;
 
 import static com.barchart.feed.ddf.util.HelperXML.XML_STOP;
 import static com.barchart.feed.ddf.util.HelperXML.xmlFirstChild;
 import static com.barchart.util.values.provider.ValueBuilder.*;
 import static org.junit.Assert.assertTrue;
 
 import java.io.ByteArrayInputStream;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.barchart.feed.api.enums.BookLiquidityType;
 import com.barchart.feed.api.enums.BookStructureType;
 import com.barchart.feed.api.enums.MarketCurrency;
 import com.barchart.feed.api.enums.SecurityType;
 import com.barchart.feed.api.inst.Instrument;
 import com.barchart.util.values.provider.ValueConst;
 
 public class TestInstrumentXML {
 
 	private static final String IBM = "<instruments status=\"200\" count=\"1\">	<instrument lookup=\"IBM\" status=\"200\" guid=\"IBM\" id=\"1298146\" symbol_realtime=\"IBM\" symbol_ddf=\"IBM\" symbol_historical=\"IBM\" "+
 		"symbol_description=\"International Business Machines Corp.\" symbol_cfi=\"EXXXXX\" exchange=\"XNYS\" exchange_channel=\"NYSE\" exchange_description=\"New York Stock Exchange\" exchange_ddf=\"N\" time_zone_ddf=\"America/New_York\" " + 
 		"tick_increment=\"1\" unit_code=\"2\" base_code=\"A\" point_value=\"1\"/> </instruments>";
 	
 	@Test
 	public void testXML() throws Exception {
 		
 		final DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
 		final DocumentBuilder builder = fac.newDocumentBuilder();
 		
 		final Document document = builder.parse(new ByteArrayInputStream(IBM.getBytes()));
 		final Element root = document.getDocumentElement();
 		final Element tag = xmlFirstChild(root, XmlTagExtras.TAG, XML_STOP);
 		final Instrument IBMInst = InstrumentXML.decodeXML(tag);
 		
 		System.out.println(IBMInst.toString());
 		
 		assertTrue(IBMInst.get(MARKET_GUID).equals("1298146"));
 		assertTrue(IBMInst.get(SECURITY_TYPE) == SecurityType.NULL_TYPE);
 		assertTrue(IBMInst.get(BOOK_LIQUIDITY) == BookLiquidityType.NONE);
 		assertTrue(IBMInst.get(BOOK_STRUCTURE) == BookStructureType.NONE);
 		assertTrue(IBMInst.get(BOOK_DEPTH) == ValueConst.NULL_SIZE);
 		assertTrue(IBMInst.get(VENDOR).equals(newText("Barchart")));
 		assertTrue(IBMInst.get(SYMBOL).equals(newText("IBM")));
 		assertTrue(IBMInst.get(DESCRIPTION).equals("International Business Machines Corp."));
 		assertTrue(IBMInst.get(CFI_CODE).equals(newText("EXXXXX")));
 		assertTrue(IBMInst.get(CURRENCY_CODE) == MarketCurrency.USD);
		assertTrue(IBMInst.get(EXCHANGE_CODE).equals(newText("N")));
 		assertTrue(IBMInst.get(PRICE_STEP).equals(newPrice(0.01)));
 		assertTrue(IBMInst.get(POINT_VALUE).equals(newPrice(1)));
 		assertTrue(IBMInst.get(DISPLAY_FRACTION).equals(newFraction(10, -2)));
 		assertTrue(IBMInst.get(LIFETIME) == (ValueConst.NULL_TIME_INTERVAL));
 		assertTrue(IBMInst.get(MARKET_HOURS).size() == 0);
 		assertTrue(IBMInst.get(TIME_ZONE_OFFSET).equals(newSize(-18000000)));
 		assertTrue(IBMInst.get(TIME_ZONE_NAME).equals(newText("NEW_YORK")));
 		assertTrue(IBMInst.get(COMPONENT_LEGS).size() == 0);
 		
 	}
 	
 	
 	
 	
 }
