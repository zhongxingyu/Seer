 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.ext.DefaultHandler2;
 
 /**
  * Splits content of given dblp xml file into multiple subfiles. One for each
  * category of publication.
  * <p>
  * e.g. all article-elements are written to article.xml, all book-elements to
  * book.xml
  * 
  * @see http://www.informatik.uni-trier.de/~ley/db/
  * @author Michael Weichselbaumer
  * @author Manuel Hochreiter
  * 
  */
 public class DblpHandler extends DefaultHandler2 {
 
 	// contains content of "main element" node
 	private StringBuffer nodeContent = new StringBuffer(1024);
 
 	private FileOutputStream output;
 
 	// maps document type and file location for output
 	private Map<String, File> docTypes;
 
 	/** 
 	 * 
 	 */
 	DblpHandler() {
 		super();
 		docTypes = new HashMap<String, File>();
 		docTypes.put("article", new File("article.xml"));
 		docTypes.put("book", new File("book.xml"));
 		docTypes.put("incollection", new File("incollection.xml"));
 		docTypes.put("inproceedings", new File("inproceedings.xml"));
 		docTypes.put("masterthesis", new File("masterthesis.xml"));
 		docTypes.put("phdthesis", new File("phdthesis.xml"));
 		docTypes.put("proceedings", new File("proceedings.xml"));
 		docTypes.put("publication", new File("publication.xml"));
 		docTypes.put("www", new File("www.xml"));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 */
 	@Override
 	public void startDocument() {
 		// delete existing xml files
 		for (Map.Entry<String, File> d : docTypes.entrySet()) {
 			d.getValue().delete();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * In case of an element interesting, newDocumetType method is called
 	 */
 	@Override
 	public void startElement(String uri, String name, String qName,
 			Attributes atts) {
 		if (docTypes.get(name) != null) {
 			newDocumentType(name, atts);
 		} else {
 			// for every element (all sub-elements) - just add to currently
 			// selected buffer
 			nodeContent.append("<" + name + ">");
 		}
 	}
 
 	/**
 	 * resets StringBuffer for node content and writes first tag with attributes
 	 * 
 	 * @param name
 	 * @param atts
 	 */
 	private void newDocumentType(String name, Attributes atts) {
 		// reset content of buffer
 		nodeContent.setLength(0);
 		// put element in buffer
 		nodeContent.append("<" + name);
 		// put attributes in buffer
 		for (int i = 0; i < atts.getLength(); i++) {
 			nodeContent.append(" " + atts.getLocalName(i) + "=\""
 					+ atts.getValue(i) + "\"");
 		}
 		nodeContent.append(">");
 	}
 
 	/**
 	 * if element closes document node, write StringBuffer to file
 	 */
 	@Override
 	public void endElement(String uri, String localName, String qName)
 			throws SAXException {
 		// add closing tag for current element
 		nodeContent.append("</" + localName + ">");
 
 		// if element is one of the "main elements" - write to appropriate file
 		if (docTypes.get(localName) != null) {
 			writeToFile(docTypes.get(localName));
 		}
 	}
 
 	/**
 	 * Appends content in {@code nodeContent} to {@code file} given as
 	 * parameter.
 	 * 
 	 * @param file
 	 *            content is appended to.
 	 */
 	private void writeToFile(File file) {
 		nodeContent.append("\n");
 		try {
 			// if file does not exists add root tag
 			if (file.exists())
 				output = new FileOutputStream(file, true);
 			else {
 				output = new FileOutputStream(file, true);
 				String start = "<?xml version=\"1.0\" encoding=\"ISO-8859-15\"?> \n<dblp>\n ";
 				output.write(start.getBytes());
 			}
 			output.write(nodeContent.toString().getBytes()); 
 			nodeContent.setLength(0);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				output.close();
 			} catch (IOException e) {
 				// nothing to do here
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
 	 * 
<<<<<<< HEAD
	 * adds content between two tags to buffer
=======
	 *      adds content between the tags to buffer
>>>>>>> branch 'master' of https://github.com/ClemensDaCoder/DKEPraktikum.git
 	 */
 	@Override
 	public void characters(char[] ch, int start, int length)
 			throws SAXException {
 		// write content of element into buffer
 		String escapedXMLString = StringEscapeUtils.escapeXml(String.valueOf(ch, start, length));
 		nodeContent.append(escapedXMLString);
 	}
 
 	/**
	 * 
 	 */
 	@Override
 	public void endDocument() throws SAXException {
 		// write closing tags
 		for (Map.Entry<String, File> d : docTypes.entrySet()) {
 			if (d.getValue().exists()) {
 				try {
 					output = new FileOutputStream(d.getValue(), true);
 					output.write("</dblp>".getBytes());
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} finally {
 					try {
 						output.close();
 					} catch (IOException e) {
 						// nothing to do
 					}
 				}
 			}
 
 		}
 	}
 }
