 package com.mtgi.analytics.xml;
 
 import static com.mtgi.analytics.xml.BinaryToCSV.CsvField.*;
 import static com.mtgi.analytics.xml.BinaryToCSV.OutputMode.*;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.EnumMap;
import java.util.regex.Pattern;
 
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 
 import com.mtgi.analytics.xml.BinaryToXml.StreamingXmlContentHandler;
 import com.mtgi.csv.CSVUtil;
 import com.sun.xml.fastinfoset.sax.SAXDocumentParser;
 
 /**
  * Convert a binary behavior tracking log to CSV format appropriate for bulk loading.
  * Most event fields are serialized to CSV, except for event-data, which is written
  * as a CSV-quoted XML blob.
  */
 public class BinaryToCSV extends BinaryXmlProcessor {
 
 	private static final String HELP_TEXT = "Usage: -tool csv [input] [output]";
 	static enum CsvField {
 		id, parent_id, type, name, application, start, duration_ms, user_id, session_id, error, event_data;
 		public static CsvField forElement(String localName) {
 			localName = localName.toLowerCase().replace('-', '_');
 			return valueOf(localName);
 		}
 	};
 	private static final String HEADER;
 	static {
 		StringBuffer buf = new StringBuffer();
 		for (CsvField h : CsvField.values())
 			buf.append(CSVUtil.quoteCSV(h)).append(',');
 		buf.append('\n');
 		HEADER = buf.toString();
 	}
 
 	static enum OutputMode {
 		XML, CSV, Skip;
 	}
 	
	private static final Pattern XML_DATE = Pattern.compile("^(\\d\\d\\d\\d-\\d\\d-\\d\\d)T(\\d\\d:\\d\\d:\\d\\d).+$");
	
 	public BinaryToCSV() {
 		super(HELP_TEXT);
     }
     
     public void parse(InputStream finf, OutputStream csv) throws Exception {
     	
     	//for normal CSV fields we accumulate character data in this buffer before sending it
     	//escaped to the output stream.
     	final StringWriter buffer = new StringWriter();
     	final Writer output = new OutputStreamWriter(csv);
     	
     	//for event-data we delegate to the BinaryToXml content handler to accumulate the XML text
     	final XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(buffer);
     	final StreamingXmlContentHandler delegate = new StreamingXmlContentHandler(writer);
     	
     	try {
     		
     		SAXDocumentParser parser = new SAXDocumentParser();
     		parser.setParseFragments(true);
     		parser.setInputStream(finf);
     		parser.setContentHandler(new ContentHandler() {
     			
     			//dictates whether we're accumulating event-data, writing CSV text, or skipping markup.
     			private OutputMode mode = Skip;
     			//accumulate entries for the next row of output.
     			private EnumMap<CsvField,String> row = new EnumMap<CsvField,String>(CsvField.class);
     			
 				public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
 					switch (mode) {
 					case CSV:
 						//switch to XML mode.
 						if ("event-data".equals(localName))
 							mode = XML; //fall-thru to XML
 						else
 							break;
 					case XML:
 						//we're inside event-data, accumulate content
 						delegate.startElement(uri, localName, qName, atts);
 						break;
 					default:
 						//switch to CSV mode when we see an event element
 						if ("event".equals(localName)) {
 							mode = CSV;
 							row.clear();
 							row.put(id, atts.getValue(uri, "id"));
 							row.put(parent_id, atts.getValue(uri, "parent-id"));
 						}
 					}
 				}
 
 				public void characters(char[] ch, int start, int length) throws SAXException {
 					switch (mode) {
 					case XML:
 						delegate.characters(ch, start, length);
 						break;
 					case CSV:
 						buffer.append(new String(ch, start, length));
 						break;
 					}
 				}
 
 				public void endElement(String uri, String localName, String qName) throws SAXException {
 					try {
 						switch (mode) {
 						case XML:
 							delegate.endElement(uri, localName, qName);
 							if ("event-data".equals(localName)) {
 								mode = CSV; //switch back to flat mode.
 								writer.flush(); //flush accumulated XML into buffer.
 								//fall-through so that event data is written out.
 							} else {
 								break;
 							}
 	
 						case CSV:
 							if ("event".equals(localName)) {
 								mode = Skip;
 								for (CsvField field : CsvField.values())
 									CSVUtil.quoteCSV(row.get(field), output).append(',');
 								output.append('\n');
 							} else {
 								String str = buffer.toString();
 								buffer.getBuffer().setLength(0);
 								
 								CsvField field = CsvField.forElement(localName);
 								if (field != null) {
									if (field == start) {
										//strip incovenient 'T' char and extra trailing numbers out of XSD date field.
										str = XML_DATE.matcher(str).replaceFirst("$1 $2");
									}
 									row.put(field, str);
 								}
 							}
 							break;
 						}
 					} catch (Exception e) {
 						throw new SAXException("Error processing end of " + localName, e);
 					}
 					
 				}
 
 				public void endPrefixMapping(String prefix) throws SAXException {
 					if (mode == XML)
 						delegate.endPrefixMapping(prefix);
 				}
 
 				public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
 					if (mode == XML)
 						delegate.ignorableWhitespace(ch, start, length);
 				}
 
 				public void processingInstruction(String target, String data) throws SAXException {
 					if (mode == XML)
 						delegate.processingInstruction(target, data);
 				}
 
 				public void startPrefixMapping(String prefix, String uri) throws SAXException {
 					if (mode == XML)
 						delegate.startPrefixMapping(prefix, uri);
 				}
 				
 				public void setDocumentLocator(Locator locator) {}
 				public void skippedEntity(String name)  {}
 				public void startDocument() {}
 				public void endDocument() {}
 
     		});
     		
     		output.append(HEADER);
     		parser.parse();
     		
     	} finally {
     		output.flush();
     		writer.close();
     	}
     }
 }
