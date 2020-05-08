 package cognems;
 
 import com.google.common.base.Joiner;
 import com.sun.istack.internal.NotNull;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author A.Sirenko
  * Date: 8/8/13
  */
 public class WikitionaryDumpHandler extends DefaultHandler {
 
 	Logger LOG = LoggerFactory.getLogger(this.getClass());
 
 	private enum STATE {
 		NONE,
 		INSIDE_PAGE,
 		INSIDE_TITLE,
 		INSIDE_TEXT
 	}
 
 	private STATE state;
 	private String title;
 	private BufferedWriter bw;
 	private boolean firstLine = true;
 	private StringBuilder sb = new StringBuilder();
 
 	public WikitionaryDumpHandler(@NotNull BufferedWriter bw) {
 		resetState();
 		this.bw = bw;
 	}
 
 	public void startElement(String uri, String localName, String qName, Attributes attributes)
 			throws SAXException {
 		switch (qName) {
 			case "page":
 				state = STATE.INSIDE_PAGE;
 				break;
 			case "title":
 				state = STATE.INSIDE_TITLE;
 				break;
 			case "text":
 				state = STATE.INSIDE_TEXT;
 				break;
 			default:
 				state = STATE.NONE;
 				break;
 		}
 	}
 
 	public void characters(char ch[], int start, int length) throws SAXException {
 		if (state == STATE.INSIDE_TITLE) {
 			String part = new String(Arrays.copyOfRange(ch, start, start + length));
 			title = title == null ? part : title + part;
 		} else if (state == STATE.INSIDE_TEXT) {
 			sb.append(new String(Arrays.copyOfRange(ch, start, start + length)));
 		}
 	}
 
 	private final Joiner joiner = Joiner.on(',');
 
 	public void endElement(String uri, String localName, String qName) throws SAXException {
 		switch (qName) {
 			case "page":
 				resetState();
 				break;
 			case "text":
 				try {
 					List<Cognem> cns = WikitionaryParser.parseDescription(title, sb.toString());
 					if (cns.size() > 0) {
 						if (firstLine) {
 							firstLine = false;
 						} else {
 							bw.newLine();
 						}
						bw.write("# " + title);
 						for (Cognem c : cns) {
 							bw.newLine();
 							bw.write(c.context.isEmpty() ? "---" : joiner.join(c.context));
 							bw.write("\t");
 							bw.write(c.sense);
 						}
 					}
 					sb.setLength(0);
 				} catch (IOException e) {
 					LOG.error(e.toString());
 					throw new RuntimeException(e);
 				}
 
 				break;
 			default:
 				state = STATE.NONE;
 				break;
 		}
 	}
 
 	private void resetState() {
 		state = STATE.NONE;
 		title = null;
 		sb.setLength(0);
 	}
 
 }
