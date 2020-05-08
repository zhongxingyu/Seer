 package com.barchart.feed.ddf.instrument.provider;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.ByteArrayInputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.junit.Test;
 import org.openfeed.proto.inst.InstrumentDefinition;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import com.barchart.feed.api.model.meta.Instrument;
 import com.barchart.feed.inst.provider.InstrumentFactory;
 import com.barchart.util.value.ValueFactoryImpl;
 import com.barchart.util.value.api.Size;
 import com.barchart.util.value.api.TimeInterval;
 import com.barchart.util.value.api.ValueFactory;
 
 public class TestInstrumentXML {
 
 	private static final ValueFactory vals = new ValueFactoryImpl();
 
 	private static final String IBM =
 			"<instruments status=\"200\" count=\"1\">	<instrument lookup=\"IBM\" status=\"200\" guid=\"IBM\" id=\"1298146\" symbol_realtime=\"IBM\" symbol_ddf=\"IBM\" symbol_historical=\"IBM\" "
 					+ "symbol_description=\"International Business Machines Corp.\" symbol_cfi=\"EXXXXX\" exchange=\"XNYS\" exchange_channel=\"NYSE\" exchange_description=\"New York Stock Exchange\" exchange_ddf=\"N\" time_zone_ddf=\"America/New_York\" "
 					+ "tick_increment=\"1\" unit_code=\"2\" base_code=\"A\" point_value=\"1\"/> </instruments>";
 
 	@Test
 	public void testXML() throws Exception {
 
 		final SAXParserFactory factory = SAXParserFactory.newInstance();
 		final SAXParser parser = factory.newSAXParser();
 		final List<InstrumentDefinition> result = new ArrayList<InstrumentDefinition>();
 		final DefaultHandler handler = handler(result);
 
 		parser.parse(new ByteArrayInputStream(IBM.getBytes()), handler);
 
 		final Instrument IBMInst = InstrumentFactory.instrument(result.get(0));
 
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
 		assertTrue(IBMInst.tickSize().equals(vals.newPrice(1, -2)));
 		assertTrue(IBMInst.pointValue().equals(vals.newPrice(1, 0)));
 		assertTrue(IBMInst.displayFraction().equals(vals.newFraction(10, -2)));
 		assertTrue(IBMInst.lifetime() == TimeInterval.NULL);
 		assertTrue(IBMInst.marketHours().size() == 0);
 		assertTrue(IBMInst.timeZoneOffset() == -18000000);
 		assertTrue(IBMInst.timeZoneName().equals("America/New_York"));
 
 	}
 
 	protected static DefaultHandler handler(final List<InstrumentDefinition> result) {
 		return new DefaultHandler() {
 
 			@Override
 			public void startElement(final String uri,
 					final String localName, final String qName,
 					final Attributes ats) throws SAXException {
 
 				if (qName != null && qName.equals("instrument")) {
 
 					try {
 
 						result.add(InstrumentXML.decodeSAX(ats));
 
 					} catch (final SymbolNotFoundException se) {
 						throw new RuntimeException(se); // would be nice to add to map
 					} catch (final Exception e) {
 						throw new RuntimeException(e);
 					}
 
 				}
 
 			}
 
 		};
 	}
 
 }
