 package com.gentics.cr.lucene.indexer.transformer.xlsx;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 
 import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
 import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
 import org.apache.poi.openxml4j.opc.Package;
 import org.apache.poi.xssf.eventusermodel.XSSFReader;
 import org.apache.poi.xssf.model.SharedStringsTable;
 import org.apache.poi.xssf.usermodel.XSSFRichTextString;
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
 
 public class XLSXContentTransformer extends ContentTransformer {
 
 	private static final String TRANSFORMER_ATTRIBUTE_KEY = "attribute";
 	private String attribute = "";
 
 	/**
	 * Get new instance of XLSXContentTransformer.
 	 * @param config
 	 */
 	public XLSXContentTransformer(GenericConfiguration config) {
 		super(config);
 		attribute = (String) config.get(TRANSFORMER_ATTRIBUTE_KEY);
 	}
 
 	@Override
 	public void processBean(CRResolvableBean bean) throws CRException {
 		if (this.attribute != null) {
 			Object obj = bean.get(this.attribute);
 			if (obj != null) {
 				String newString = getStringContents(obj);
 				if (newString != null) {
 					bean.set(this.attribute, newString);
 				}
 			}
 		} else {
 			log.error("Configured attribute is null. Bean will not be processed");
 		}
 	}
 
 	/**
	 * Converts a byte array that contains a excel file into a string with its contents.
 	 * @param obj
 	 * @return
 	 */
 	private String getStringContents(Object obj) throws CRException {
 		String contents = "";
 		ByteArrayInputStream is;
 		if (obj instanceof byte[]) {
 			is = new ByteArrayInputStream((byte[]) obj);
 		} else {
 			throw new IllegalArgumentException("Parameter must be instance of byte[]");
 		}
 		try {
 			Package pkg = Package.open(is);
 
 			XSSFReader r = new XSSFReader(pkg);
 			SharedStringsTable sst = r.getSharedStringsTable();
 			SheetHandler handler = new SheetHandler(sst);
 
 			XMLReader parser = fetchSheetParser(handler);
 
 			Iterator<InputStream> sheets = r.getSheetsData();
 			while (sheets.hasNext()) {
 				InputStream sheet = sheets.next();
 				InputSource sheetSource = new InputSource(sheet);
 				parser.parse(sheetSource);
 				sheet.close();
 			}
 
 			contents = handler.getResult();
 		} catch (IOException io) {
 			throw new CRException(io);
 		} catch (SAXException e) {
 			throw new CRException(e);
 		} catch (InvalidFormatException e) {
 			throw new CRException(e);
 		} catch (OpenXML4JException e) {
 			throw new CRException(e);
 		}
 
 		return contents;
 	}
 
 	private XMLReader fetchSheetParser(ContentHandler sst) throws SAXException {
 		XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
 
 		parser.setContentHandler(sst);
 		return parser;
 	}
 
 	@Override
 	public void destroy() {
 		// TODO Auto-generated method stub
 
 	}
 
 	/** 
	 * See org.xml.sax.helpers.DefaultHandler javadocs.
 	 */
 	private static class SheetHandler extends DefaultHandler {
 		private SharedStringsTable sst;
 		private String lastContents;
 		private boolean nextIsString;
 
 		private StringBuffer buffer = null;
 
 		private void addToBuffer(String str) {
 			if (buffer == null) {
 				buffer = new StringBuffer();
 			}
 			buffer.append(str);
 		}
 
 		private SheetHandler(SharedStringsTable sst) {
 			this.sst = sst;
 		}
 
 		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
 			// c => cell
 			if (name.equals("c")) {
 				// Print the cell reference
 				// WE DO NOT NEED CELL INFORMATION IN OUR TEXT
 				//addToBuffer(attributes.getValue("r") + " - ");
 				// Figure out if the value is an index in the SST
 				String cellType = attributes.getValue("t");
 				if (cellType != null && cellType.equals("s")) {
 					nextIsString = true;
 				} else {
 					nextIsString = false;
 				}
 			}
 			// Clear contents cache
 			lastContents = "";
 		}
 
 		public void endElement(String uri, String localName, String name) throws SAXException {
 			// Process the last contents as required.
 			// Do now, as characters() may be called more than once
 			if (nextIsString && !"".equals(lastContents)) {
 				int idx = Integer.parseInt(lastContents);
 				lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
 			}
 
 			// v => contents of a cell
 			// Output after we've seen the string contents
 			if (name.equals("v")) {
 				addToBuffer(lastContents);
 			}
 			lastContents = "";
 		}
 
 		public String getResult() {
 			if (buffer == null) {
 				return null;
 			}
 			return buffer.toString();
 		}
 
 		public void characters(char[] ch, int start, int length) throws SAXException {
 			lastContents += new String(ch, start, length);
 		}
 	}
 
 }
