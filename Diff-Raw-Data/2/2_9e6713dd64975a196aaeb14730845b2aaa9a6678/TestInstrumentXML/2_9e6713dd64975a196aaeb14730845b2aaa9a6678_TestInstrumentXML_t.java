 package com.barchart.feed.ddf.instrument.provider;
 
 import static com.barchart.feed.ddf.util.HelperXML.XML_STOP;
 import static com.barchart.feed.ddf.util.HelperXML.xmlFirstChild;
 import static org.junit.Assert.assertTrue;
 
 import java.io.ByteArrayInputStream;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.barchart.feed.api.model.meta.Instrument;
 import com.barchart.feed.inst.provider.InstrumentFactory;
 import com.barchart.util.value.ValueFactoryImpl;
 import com.barchart.util.value.api.ValueFactory;
 import com.barchart.util.value.api.Size;
 import com.barchart.util.value.api.TimeInterval;
 
 public class TestInstrumentXML {
 
 	private static final ValueFactory factory = new ValueFactoryImpl();
 	
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
 		final Instrument IBMInst = InstrumentFactory.instrument(InstrumentXML.decodeXML(tag));
 		
 		assertTrue(IBMInst.marketGUID().equals("IBM"));
		assertTrue(IBMInst.securityType() == Instrument.SecurityType.EQUITY);
 		assertTrue(IBMInst.liquidityType() == Instrument.BookLiquidityType.NONE);
 		assertTrue(IBMInst.bookStructure() == Instrument.BookStructureType.NONE);
 		assertTrue(IBMInst.maxBookDepth() == Size.NULL);
 		assertTrue(IBMInst.instrumentDataVendor().equals("Barchart"));
 		assertTrue(IBMInst.symbol().equals("IBM"));
 		assertTrue(IBMInst.description().equals("International Business Machines Corp."));
 		assertTrue(IBMInst.CFICode().equals("EXXXXX"));
 		assertTrue(IBMInst.exchangeCode().equals("N"));
 		assertTrue(IBMInst.tickSize().equals(factory.newPrice(1, -2)));
 		assertTrue(IBMInst.pointValue().equals(factory.newPrice(1, 0)));
 		assertTrue(IBMInst.displayFraction().equals(factory.newFraction(10, -2)));
 		assertTrue(IBMInst.lifetime() == TimeInterval.NULL);
 		assertTrue(IBMInst.marketHours().size() == 0);
 		assertTrue(IBMInst.timeZoneOffset() == -18000000);
 		assertTrue(IBMInst.timeZoneName().equals("NEW_YORK"));
 		
 	}
 	
 	
 	
 	
 }
