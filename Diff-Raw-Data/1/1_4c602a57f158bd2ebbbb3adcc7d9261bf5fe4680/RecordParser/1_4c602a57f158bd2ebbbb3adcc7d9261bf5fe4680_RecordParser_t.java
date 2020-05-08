 package eu.europeana.uim.common.parse;
 
 import java.io.InputStream;
 import java.util.List;
 import java.util.Stack;
 import java.util.logging.Logger;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import org.apache.commons.lang.time.DurationFormatUtils;
 
 import com.ctc.wstx.stax.WstxInputFactory;
 
 import eu.europeana.uim.common.ProgressMonitor;
 import eu.europeana.uim.common.ProgressMonitorAdapter;
 
 /**
  * XML parser for documents compliant with a list of simple records. Fields within a 
  * single record are of form <fieldName>value</fieldName>.
  *
  * @author andreas.juffinger@kb.nl
  */
 public class RecordParser {
 
 	private static final Logger log = Logger.getLogger(RecordParser.class.getName());
 
 	/** Parse method which takes an input stream an the name of the record element. The
 	 * results are returned as a list of Map's whereby each map represents the content of
 	 * a single record.
 	 * 
 	 * @param stream
 	 * @param record
 	 * @return
 	 * @throws XMLStreamParserException
 	 */
 	public List<RecordMap> parse(InputStream stream, String record) throws XMLStreamParserException {
 		MemoryRecordHandler memory = new MemoryRecordHandler(record);
 		parse(stream, memory, new ProgressMonitorAdapter());
 		return memory.getMemory();
 	}
 
 	/** Parse method which takes a stream and a record handler. The record handler is informed
 	 * about every record read from the stream.
 	 * 
 	 * @param stream
 	 * @param handler
 	 * @throws XMLStreamParserException
 	 */
 	public void parse(InputStream stream, RecordHandler handler) throws XMLStreamParserException {
 		parse(stream, handler, new ProgressMonitorAdapter());
 	}
 
 	/** Parse method which takes a stream, record handler, and a progress monitor to report found
 	 * records and the overall progress. Note that due to the stream based parsing, it is not possible to actually
 	 * inform the monitor about the total amount of work.
 	 * 
 	 * @param inputStream
 	 * @param handler
 	 * @param monitor
 	 * @throws XMLStreamParserException
 	 */
 	public void parse(InputStream inputStream, RecordHandler handler, ProgressMonitor monitor) throws XMLStreamParserException {
 		XMLInputFactory inFactory = new WstxInputFactory();
 
 		XMLStreamReader xml;
 		try {
 			xml = inFactory.createXMLStreamReader(inputStream);
			monitor.worked(1);
 		} catch (XMLStreamException e) {
 			throw new XMLStreamParserException("Failed to create XML Stream reader from input stream:" + inputStream);
 		}
 
 		int recordCount = 0;
 		long startTime = System.currentTimeMillis();
 
 		monitor.beginTask("Parsing stream for records <" + handler.getRecordElement() + ">", 0);
 
 		Stack<String> path = new Stack<String>();
 		RecordMap record = null;
 		try {
 			while (xml.hasNext() && !monitor.isCancelled()) {
 				xml.next();
 
 				switch (xml.getEventType()) {
 				case XMLStreamConstants.START_DOCUMENT:
 					log.info("Document started");
 					break;
 
 				case XMLStreamConstants.START_ELEMENT:
 					String local = xml.getLocalName();
 					String prefix = xml.getPrefix();
 					String qualified = local;
 					if (prefix != null && prefix.length() > 0) {
 						qualified = prefix + ":" + local;
 					}
 					path.push(qualified);
 
 					if (record != null) {
 						String language = null;
 						if (xml.getAttributeCount() > 0) {
 							for (int i = 0; i < xml.getAttributeCount(); i++) {
 								String l = xml.getAttributeLocalName(i);
 								if ("lang".equals(l)) {
 									language = xml.getAttributeValue(i);
 								}
 							}
 						}
 
 						String text = xml.getElementText();
 						record.put(new RecordField(prefix, local, language), text);
 						path.pop();
 					} else {
 						if (isRecordElement(path, handler)) {
 							record = new RecordMap();
 						} else {
 							log.fine("Skipping:" + local);
 						}
 					}
 					break;
 
 				case XMLStreamConstants.END_ELEMENT:
 					if (isRecordElement(path, handler)) {
 						if (++recordCount % 25 == 0) {
 							monitor.worked(25);
 							log.fine(String.format("imported %d records in %s", recordCount, DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - startTime)));
 						}
 
 
 						handler.record(record);
 						monitor.worked(1);
 
 						record = null;
 					}
 
 					path.pop();
 					break;
 
 				case XMLStreamConstants.END_DOCUMENT:
 					log.info(String.format("Document ended, imported %d records", recordCount));
 					monitor.done();
 					break;
 				}
 			}
 			inputStream.close();
 		} catch (Exception e) {
 			throw new XMLStreamParserException("Failed to parse document.", e);
 		}
 	}
 
 
 	private boolean isRecordElement(Stack<String> stack, RecordHandler handler) {
 		if (handler.getRecordElement().contains("|")) {
 			StringBuilder path = new StringBuilder();
 			for (String element : stack) {
 				if (path.length() > 0) {
 					path.append("|");
 				}
 				path.append(element);
 			}
 			return path.toString().equals(handler.getRecordElement());
 		}
 		return stack.peek().equals(handler.getRecordElement());
 	}
 
 }
