 package net.unit8.sastruts.easyapi.xstream.io;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.apache.commons.lang.StringUtils;
 
 public class CsvXmlStreamWriter implements XMLStreamWriter {
 	private Writer writer;
 	private NamespaceContext namespaceContext;
 	private String prefix;
 	private String rootName;
 	private List<String> titleColumns = new ArrayList<String>();
 	private Map<String, String> columns = new HashMap<String, String>();
 	private boolean firstRow = true;
 	private Stack<String> nodeStack = new Stack<String>();
 	private char delimiter = ',';
 	private static final char QUOTE = '"';
 	private String currentValue;
 
 	public CsvXmlStreamWriter(Writer writer, String rootName) {
 		this.writer = writer;
 		this.rootName = rootName;
 	}
 
 	public void close() throws XMLStreamException {
 		try {
 			writer.close();
 		} catch (IOException e) {
 			throw new XMLStreamException(e);
 		}
 	}
 
 	public void flush() throws XMLStreamException {
 		try {
 			writer.flush();
 		} catch (IOException e) {
 			throw new XMLStreamException(e);
 		}
 	}
 
 	public NamespaceContext getNamespaceContext() {
 		return namespaceContext;
 	}
 
 	public String getPrefix(String uri) throws XMLStreamException {
 		return prefix;
 	}
 
 	public Object getProperty(String arg0) throws IllegalArgumentException {
 		return null;
 	}
 
 	public void setDefaultNamespace(String arg0) throws XMLStreamException {
 	}
 
 	public void setNamespaceContext(NamespaceContext namespaceContext)
 			throws XMLStreamException {
 		this.namespaceContext = namespaceContext;
 
 	}
 
 	public void setPrefix(String prefix, String uri) throws XMLStreamException {
 		this.prefix = prefix;
 	}
 
 	public void writeAttribute(String localName, String value)
 			throws XMLStreamException {
 		writeAttribute(null, localName, value);
 
 	}
 
 	public void writeAttribute(String namespaceURI, String localName, String value)
 			throws XMLStreamException {
 		writeAttribute(null, namespaceURI, localName, value);
 	}
 
 	public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
 			throws XMLStreamException {
 		if (nodeStack.size() > 2 && !firstRow)
 			columns.put(localName, value);
 	}
 
 	public void writeCData(String text) throws XMLStreamException {
 		currentValue = text;
 	}
 
 	public void writeCharacters(String text) throws XMLStreamException {
 		currentValue = text;
 	}
 
 	public void writeCharacters(char[] text, int start, int len) {
 		currentValue = new String(text, start, len);
 	}
 
 	public void writeComment(String comment) throws XMLStreamException {
 		// do nothing
 	}
 
 	public void writeDTD(String dtd) throws XMLStreamException {
 		// do nothing
 	}
 
 	public void writeDefaultNamespace(String namespace)
 			throws XMLStreamException {
 		// do nothing
 	}
 
 	public void writeEmptyElement(String localName) throws XMLStreamException {
 		writeEmptyElement(null, localName);
 	}
 
 	public void writeEmptyElement(String namespaceURI, String localName)
 			throws XMLStreamException {
		writeEmptyElement(localName, namespaceURI);
 	}
 
 	public void writeEmptyElement(String prefix, String localName, String namespaceURI)
 			throws XMLStreamException {
 		// do nothing
 	}
 
 	public void writeEndDocument() throws XMLStreamException {
 		// do nothing
 
 	}
 
 	public void writeEndElement() throws XMLStreamException {
 		String localName = nodeStack.pop();
 		if (StringUtils.equals(localName, rootName)) {
 			try {
 				if (firstRow) {
 					StringBuilder sb = new StringBuilder(512);
 					for(String title : titleColumns) {
 						sb.append(escape(title)).append(delimiter);
 					}
 					if (sb.length() > 0)
 						sb.deleteCharAt(sb.length() - 1);
 					sb.append("\r\n");
 					writer.write(sb.toString());
 					firstRow = false;
 				}
 				StringBuilder line = new StringBuilder(512);
 				for(String title : titleColumns) {
 					line.append(escape(columns.get(title))).append(delimiter);
 				}
 				if (line.length() > 0)
 					line.deleteCharAt(line.length() - 1);
 				line.append("\r\n");
 				writer.write(line.toString());
 			} catch (IOException e) {
 				throw new XMLStreamException(e);
 			}
 		} else if (nodeStack.size() > 0) {
 			if (firstRow) {
 				titleColumns.add(currentValue);
 			} else {
 				columns.put(localName, currentValue);
 			}
 		}
 		currentValue = null;
 	}
 
 	public void writeEntityRef(String arg0) throws XMLStreamException {
 		// do nothing
 	}
 
 	public void writeNamespace(String arg0, String arg1)
 			throws XMLStreamException {
 		// do nothing
 	}
 
 	public void writeProcessingInstruction(String arg0)
 			throws XMLStreamException {
 		// do nothing
 	}
 
 	public void writeProcessingInstruction(String arg0, String arg1)
 			throws XMLStreamException {
 		// do nothing
 	}
 
 	public void writeStartDocument() throws XMLStreamException {
 		// do nothing
 
 	}
 
 	public void writeStartDocument(String version) throws XMLStreamException {
 		// do nothing
 
 	}
 
 	public void writeStartDocument(String encoding, String version)
 			throws XMLStreamException {
 		// do nothing
 
 	}
 
 	public void writeStartElement(String localName) throws XMLStreamException {
 		writeStartElement(null, localName);
 	}
 
 	public void writeStartElement(String namespaceURI, String localName)
 			throws XMLStreamException {
 		writeStartElement(null, localName, namespaceURI);
 	}
 
 	public void writeStartElement(String prefix, String localName, String namespaceURI)
 			throws XMLStreamException {
 		nodeStack.push(localName);
 		if (StringUtils.equals(localName, rootName)) {
 			columns.clear();
 		}
 	}
 
 	/**
 	 * Escape string following RFC4180.
 	 *
 	 * @param src
 	 * @return escaped string.
 	 */
 	private String escape(String src) {
 		if (src == null) return "";
 		int len = src.length();
 		StringBuilder sb = new StringBuilder(len + 32);
 		boolean escaped = false;
 		for (int i=0; i < len; i++) {
 			if (src.charAt(i) == delimiter) {
 				escaped = true;
 				sb.append(delimiter);
 			} else if (src.charAt(i) == QUOTE) {
 				escaped = true;
 				sb.append(QUOTE).append(QUOTE);
 			} else if (src.charAt(i) == '\n' || src.charAt(i) == '\r') {
 				escaped = true;
 				sb.append(src.charAt(i));
 			} else {
 				sb.append(src.charAt(i));
 			}
 		}
 		if (escaped) {
 			sb.insert(0, delimiter);
 			sb.append(delimiter);
 		}
 		return sb.toString();
 	}
 }
