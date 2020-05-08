 package org.atlasapi.feeds.interlinking.outputting;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.URL;
 
 import org.atlasapi.feeds.interlinking.InterlinkBrand;
 import org.atlasapi.feeds.interlinking.InterlinkBroadcast;
 import org.atlasapi.feeds.interlinking.InterlinkEpisode;
 import org.atlasapi.feeds.interlinking.InterlinkFeed;
 import org.atlasapi.feeds.interlinking.InterlinkOnDemand;
 import org.atlasapi.feeds.interlinking.InterlinkSeries;
 import org.atlasapi.feeds.interlinking.InterlinkFeed.InterlinkFeedAuthor;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.junit.Test;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.Resources;
 import com.thaiopensource.util.PropertyMap;
 import com.thaiopensource.util.PropertyMapBuilder;
 import com.thaiopensource.validate.Schema;
 import com.thaiopensource.validate.SchemaReader;
 import com.thaiopensource.validate.ValidateProperty;
 import com.thaiopensource.validate.Validator;
 import com.thaiopensource.validate.rng.CompactSchemaReader;
 import com.thaiopensource.validate.rng.RngProperty;
 
 public class InterlinkFeedOutputterTest {
 
 	private final static InterlinkFeedOutputter outputter = new InterlinkFeedOutputter();
 	
 	@Test
 	public void testSerialisationOfAFeed() throws Exception {
 		
 		InterlinkBrand brand = new InterlinkBrand("1")
 			.withTitle("Lark Rise to Candleford");
 		
 		// add an episode directly to the brand
 		InterlinkEpisode episodeWithoutASeries = new InterlinkEpisode("notInASeries", 2).withTitle("Episode not in a series");
 		
 		episodeWithoutASeries.addBroadcast(new InterlinkBroadcast("broadcastNotInASeries"));
 		episodeWithoutASeries.addOnDemand(new InterlinkOnDemand("odNotInASeries"));
 		
 		brand.addEpisodeWithoutASeries(episodeWithoutASeries);
 		
 		InterlinkSeries series = new InterlinkSeries("series2", 2)
 			.withTitle("Lark Rise to Candleford Series 2")
 			.withSummary("Adaption of Flora Thompson's");
 		
 		brand.addSeries(series);
 		
 		InterlinkOnDemand onDemand = new InterlinkOnDemand("ondemand5");
 		
 		InterlinkBroadcast broadcast = new InterlinkBroadcast("broadcast4")
 			.withBroadcastStart(new DateTime("2010-01-10T21:00:00Z"))
 			.withDuration(Duration.standardMinutes(45));
 		
 		InterlinkEpisode episode = new InterlinkEpisode("episode3", 3)
 			.withTitle("Lark Rise to Candleford Episode 3")
 			.addBroadcast(broadcast)
 			.addOnDemand(onDemand);
 		
 		series.addEpisode(episode);
 		
 		InterlinkFeed feed = new InterlinkFeed("https://www.bbc.co.uk/interlinking/20100115")
 			.withTitle("BBC Daily Change Feed")
 			.withSubtitle("All metadata changes for on demand BBC content")
 			.withUpdatedAt(new DateTime("2010-01-15T14:51:10Z"))
 			.withAuthor(new InterlinkFeedAuthor("a partner", "a supplier"))
 			.addBrand(brand);
 		
 		String generated = output(feed);
 		assertEquals(expectedFeed("feed.atom"), generated);
 		
 		// for now just print out errors since the feed is not finished and will not validate
 		validatesAgainstSchema(generated);
 	}
 
 	public boolean validatesAgainstSchema(String xml) throws Exception {
 		URL schemaUrl = null;
 		try {
 			schemaUrl = Resources.getResource("org/atlasapi/feeds/interlinking/outputting/interlinking.rnc");
 		} catch (IllegalArgumentException e) {
 			System.out.println("WARN: Could not find schema to validate feed");
 			return false;
 		}
 		SchemaReader schemaReader = CompactSchemaReader.getInstance();
 		Schema schema = schemaReader.createSchema(new InputSource(schemaUrl.openStream()), PropertyMap.EMPTY);
 
 		PropertyMapBuilder properties = new PropertyMapBuilder();
 		RngProperty.CHECK_ID_IDREF.add(properties);
 		PrintingErrorHandler printingErrorHandler = new PrintingErrorHandler();
 		ValidateProperty.ERROR_HANDLER.put(properties, printingErrorHandler);
 		Validator validator = schema.createValidator(properties.toPropertyMap());
 
 		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
 
 		xmlReader.setContentHandler(validator.getContentHandler());
 		xmlReader.setDTDHandler(validator.getDTDHandler());
 
 		try {
 			xmlReader.parse(new InputSource(new StringReader(xml)));
 		} catch (SAXException e) {
 			return false;
 		}
 		return !printingErrorHandler.thereWasAnError;
 	}
 	 
 	 private static class PrintingErrorHandler implements ErrorHandler {
 
 		private boolean thereWasAnError = false;
 		 
 		@Override
 		public void error(SAXParseException exception) throws SAXException {
			printError(exception);
 			thereWasAnError = true;
 		}
 
 		@Override
 		public void fatalError(SAXParseException exception) throws SAXException {
			printError(exception);
 			thereWasAnError = true;
 		}
 
 		@Override
 		public void warning(SAXParseException exception) throws SAXException {
			printError(exception);
 			thereWasAnError = true;
 		}

		private static void printError(SAXParseException exception) {
			System.out.println(exception + " on line " + exception.getLineNumber() + ":" + exception.getColumnNumber());
		}
 	 }
 
 	private String output(InterlinkFeed feed) throws IOException {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		outputter.output(feed, out);
 		return out.toString(Charsets.UTF_8.toString());
 	}
 
 	private String expectedFeed(String filename) throws IOException {
 		return Resources.toString(Resources.getResource("org/atlasapi/feeds/interlinking/outputting/" + filename), Charsets.UTF_8);
 	}	
 }
