 package pds.label;
 
 import java.io.File;
 import java.io.PrintStream;
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.StringReader;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamSource;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Comment;
 import org.w3c.dom.Text;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * PDSLabel is a class that contains all information regarding a
  * PDS label entity. A PDS label entity consists of one or more 
  * elements as specified in the PDS Object Definition Language (ODL). 
  * Each element may be a simple line of text, a block of 
  * commented text, or a keyword/value pair. Comments and values 
  * may extend over more than one physical line.
  *
  * @author      Todd King
  * @author      Planetary Data System
  * @version     1.0, 02/21/03
  * @since       1.0
  */
  	
 // Container for a label 
 public class PDSLabel {
 	/** The list of elements in the label */
  	public ArrayList<PDSElement> mElement	= new ArrayList<PDSElement>();
  	
 	/** The list of files referenced in the label */
  	public ArrayList<String> mProductFile	= new ArrayList<String>();
  	
  	/** The path and file name used when loading a label from a file.*/
  	public String	mPathName	= "";
  	
  	/** Current line being parsed */
 	int		mLineCount = 0;
  	
  	/** Stream to write messages - Default System.out */
 	PrintStream		mLog = System.out;
  	
  	/** Creates an instance of a PDSLabel */
  	public PDSLabel() {
  	}
  	
  	/** Creates an instance of a PDSLabel */
  	public PDSLabel(PrintStream log) {
  		setLog(log);
  	}
  	
  	/** 
      * Returns a string with the release information for this compilation.
      *
      * @return	a string containing the release information for this compilation.
      * @since           1.0
      */
 	public String version() {
 		return "1.0.0.3";
  	}
  	
     /** 
      * Reset all internal variables to the initial state.
 	 *
      * @since           1.0
      */
  	public void reset() {
  		mElement = new ArrayList<PDSElement>();
  	}
  	
  	/**
  	 * Entry point for testing
  	 **/
     public static void main(String args[]) 
     {
     	int		output = 0;	// Default is Dump
     	
     	if(args.length == 0) {
     		System.out.println("Proper usage: pds.label.PDSLabel pathname [dump|xml]");
     		return;
     	}
     	ArrayList<String>	files = null;
     	
     	PDSLabel label = new PDSLabel();
     	try {
     		if(label.isLabel(args[0])) {
     			System.out.println("Parsing label: " + args[0]);
     			label.parse(args[0]);
     		} else {
     			System.out.println("Parsing XML: " + args[0]);
     			label.parseXML(args[0]);
     		}
     	} catch(Exception e) {
     		label.printMessage(e.getMessage());
     		e.printStackTrace(System.out);
     		return;
     	}
     	
     	if(args.length > 1) {
     		if(args[1].compareToIgnoreCase("dump") == 0) output = 0;	// Dump
     		if(args[1].compareToIgnoreCase("xml") == 0) output = 1;	// XML
     	}
     	
     	switch(output) {
 		case 1:	// XML
 			label.printXML(System.out);
 			break;
 		case 0:	// Dump as label
 		default:
 	    	files = label.filePointers();
 	    	if(files == null) {
 	    		System.out.println("No file pointers.");
 	    	} else {
 	    		Iterator<String> i = files.iterator();
 	    		while(i.hasNext()) {
 	    			System.out.println(i.next());
 	    		}
 	    	}
 	    	
 			System.out.println("----------");
 			label.print();
 			break;
     	}
     }
     
     /** 
      * Determines if a file contains a PDS label.
      * If the first 14 characters are either "PDS_VERSION_ID" 
      * or "CCSD3ZF0000100" then the file is considered 
      * to contain a PDS label.
 	 *
      * @param pathName  the fully qualified path and name of the file to parse.
      *
      * @return          <code>true</code> if the file contains a label.
      *                  <code>false</code> otherwise.
      *
      * @since           1.0
      */
  	public boolean isLabel(String pathName) {
 
 		FileInputStream file;
 		String	buffer	= "";
 		int		i;
 		int		c		= 0;
 		boolean	label	=	false;
 				
 		try {
 			file = new FileInputStream(pathName);
 			i = 0;
 			while((c = file.read()) != -1) {	// Read a little of the file
 				buffer += (char) c;
 				i++;
 				if(i == 14) break;
 			}
 			if(buffer.compareTo("CCSD3ZF0000100") == 0) label = true;
 			if(buffer.compareTo("PDS_VERSION_ID") == 0) label = true;
 		} catch(Exception e) {
 			System.out.println("Unable to open file: " + pathName);
 			System.out.println("    Reason: " + e.getMessage());
 		}
 		return label;
  	}
  	
     /** 
      * Determines if an item is a valid.
 	 *
      * @param item  the item to check.
      *
      * @return          <code>true</code> if the item is valid.
      *                  <code>false</code> otherwise.
      *
      * @since           1.0
      */
  	public boolean isValidItem(PDSItem item) 
  	{
 		if(item == null) return false;
 		return item.valid();
  	}
  	
  	
     /** 
      * Parses a file containing a PDS label into its constitute elements.
 	 * The path and name of the file are passed to the method which is
 	 * opened and parsed.
 	 *
      * @param pathName  the fully qualified path and name of the file to parse.
      *
      * @return          <code>true</code> if the file could be opened;
      *                  <code>false</code> otherwise.
      * @since           1.0
      */
  	public boolean parse(String pathName) 
  		throws PDSException 
  	{
 		FileInputStream file;
 		BufferedReader	reader;
 		boolean			status;
 		
 		mPathName = pathName;
  		
 		try {
 			file = new FileInputStream(mPathName);
 			reader = new BufferedReader(new InputStreamReader(file));
 			status = parse(reader);
 			reader.close();
 			file.close();
 		} catch(Exception e) {
 			throw(new PDSException(e.getMessage()) );
 		}
 		
 		return status;
  	}
  	
     /** 
      * Parses a file containing a PDS label into its constitute elements.
 	 * The file to parse must be previously opened and a InputStream
 	 * pointing to the file is passed.
 	 *
      * @param reader		a connection to a pre-opened file.
      *
      * @return          <code>true</code> if the file could be read;
      *                  <code>false</code> otherwise.
      * @since           1.0
      */
  	public boolean parse(InputStream stream) 	
  		throws PDSException
  	{
 		BufferedReader	reader;
 		boolean			status;
 
 		try {
 			reader = new BufferedReader(new InputStreamReader(stream));
 			status = parse(reader);
 			reader.close();
 		} catch(Exception e) {
 			throw(new PDSException(e.getMessage()) );
 		}
 		
 		return status;
  	}
 
     /** 
      * Parses a file containing a PDS label into its constitute elements.
 	 * The file to parse must be previously opened and a BufferedReader
 	 * pointing to the file is passed.
 	 *
      * @param reader		a connection to a pre-opened file.
      *
      * @return          <code>true</code> if the file could be read;
      *                  <code>false</code> otherwise.
      * @since           1.0
      */
  	public boolean parse(BufferedReader reader) 	
  		throws PDSException
  	{
 		boolean	more = true;
 		boolean good = true;
 		PDSElement element;
 		
 		reset();
 		
 		try {
 			good = true;
 			while(more) {
 				element = new PDSElement(mLineCount);
 				more = element.parse(reader);
 				if(more) {
 					mElement.add(element);
 				}
 				mLineCount = element.mLineCount;
 			}
 		} catch(Exception e) {
 			// e.printStackTrace();
 			throw(new PDSException(e.getMessage()) );
 		}
 		
 		return good;
  	}
 
     /** 
      * Parses a file containing XML into its constitute elements.
 	 * The path and name of the file are passed to the method which is
 	 * opened and parsed.
 	 *
      * @param pathName  the fully qualified path and name of the file to parse.
      *
      * @return          <code>true</code> if the file could be opened;
      *                  <code>false</code> otherwise.
      * @since           1.0
      */
  	public boolean parseXML(String pathName) 
  		throws PDSException 
  	{
 		FileInputStream file;
 		boolean			status;
 		
 		mPathName = pathName;
  		
 		try {
 			file = new FileInputStream(mPathName);
 			status = parseXML(file);
 			file.close();
 		} catch(Exception e) {
 			throw(new PDSException(e.getMessage()) );
 		}
 		
 		return status;
  	}
  	
     /** 
      * Parses a file containing XML into its constitute elements.
 	 * The file to parse must be previously opened and a InputStream
 	 * pointing to the file is passed.
 	 *
      * @param reader		a connection to a pre-opened file.
      *
      * @return          <code>true</code> if the file could be read;
      *                  <code>false</code> otherwise.
      * @since           1.0
      */
  	public boolean parseXML(InputStream stream) 	
  		throws PDSException
  	{
  		Document	doc;
         DocumentBuilderFactory factory =
             DocumentBuilderFactory.newInstance();
         //factory.setValidating(true);   
         //factory.setNamespaceAware(true);
         try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(stream);
         } catch (Exception e) { // Error generated during parsing
            throw new PDSException(e.getMessage());
         }
         
         // Now transform the DOM into a "label" representation
         pushNode(doc, null);
         
         return true;
  	}
 
     /** 
      * Pushes a DOM node as a label Element. If the node has children all 
      * children are also pushed. To push an entire DOM pass the root node.
      * The following rules are applied in the conversion:
      * <ul>
      * <li>Any element in the DOM becomes an element in the label.</li> 
      * <li>If an element contains other elements it is promoted to an object.</li>
      * <li>Comments are preserved</li>
      * <li>All other node types are discarded (attributes, entity, etc)</li>
      * </ul>
 	 *
      * @return          true if the push was successful,
      *					false otherwise.
      * @since           1.0
      */
  	public boolean pushNode(Node node, PDSElement parentElement)
  		throws PDSException
  	{
  		PDSElement	element = parentElement;
  		String		buffer;
  		NodeList	list;
 
 		/*
  		switch(node.getNodeType()) {
 		case Node.ATTRIBUTE_NODE: // The node is an Attr. 
 		case Node.CDATA_SECTION_NODE :	// The node is a CDATASection. 
 		case Node.DOCUMENT_FRAGMENT_NODE: 	// The node is a DocumentFragment. 
 		case Node.DOCUMENT_NODE:	// The node is a Document. 
 		case Node.DOCUMENT_TYPE_NODE:	// The node is a DocumentType. 
 		case Node.ENTITY_NODE:		// The node is an Entity. 
 		case Node.ENTITY_REFERENCE_NODE:	// The node is an EntityReference. 
 		case Node.NOTATION_NODE:	// The node is a Notation. 
 		case Node.PROCESSING_INSTRUCTION_NODE:	// The node is a ProcessingInstruction. 
 			System.out.println(node.getNodeName());
 			break;
 		case Node.ELEMENT_NODE:		// The node is an Element. 
 		case Node.COMMENT_NODE:	// The node is a Comment. 
 		case Node.TEXT_NODE:		// The node is Text		
 			System.out.println(node.getNodeName());
 			break;	
  		}
 		*/
 		
  		mLineCount++;
  		// Determine if to create a new element
  		switch(node.getNodeType()) {
 		case Node.COMMENT_NODE:	// The node is a Comment. 
 			element = new PDSElement(mLineCount);
 			element.mType = PDSElement.TYPE_COMMENT;
 			element.parseValue(node.getNodeValue());
 			mElement.add(element);
 			break;
 		case Node.ELEMENT_NODE:		// The node is an Element. 
 			if(parentElement != null) {	// Make an object
 				if(!parentElement.isObject()) {
 					parentElement.setValue(parentElement.mKeyword);
 					parentElement.mKeyword = "OBJECT";
 				}
 			}
 			element = new PDSElement(mLineCount);
 			element.mKeyword = node.getNodeName();
 			mElement.add(element);
 			break;
 		case Node.TEXT_NODE:
 			buffer = node.getNodeValue();
 			buffer = buffer.trim();
 			if(buffer.length() > 0 && parentElement != null) {
 				if(!parentElement.isObject()) parentElement.setValue(buffer);
 			}
 			break;
  		}
  		
  		// Now handle all children
     	list = node.getChildNodes();
     	for(int i = 0; i < list.getLength(); i++) {
     		pushNode(list.item(i), element);
     	}
     	
     	if(node.getNodeType() == Node.ELEMENT_NODE && element != null) {
     		if(element.isObject()) {	// Add END_OBJECT
 				element = new PDSElement(mLineCount);
 				element.mKeyword = "END_OBJECT";
 				element.setValue(node.getNodeName());
 				mElement.add(element);
     		}
     	}
     	
  		return true;
 	}
 	
     /** 
      * Returns the fully qualified path and name of the file
      * which was parsed.
 	 *
      * @return          the path and file name to the file most recently parsed. The returned value
      *					will be blank if no file has been parsed or if a FileInputStream
      *					was used when parsing the file.
      * @since           1.0
      */
  	public String pathName() { return mPathName; }
  	
     /** 
      * Returns the path portion of the fully qualified name of the file
      * which was parsed.
 	 *
      * @return          the path to the file most recently parsed. The returned value
      *					will be blank if no file has been parsed or if a FileInputStream
      *					was used when parsing the file.
      * @since           1.0
      */
  	public String path() {
  		int		n;
  		
  		// Try with unix style
  		n = mPathName.lastIndexOf('/');
  		if(n != -1) return mPathName.substring(0, n+1);
  		
  		// Try with DOS style
  		n = mPathName.lastIndexOf('\\');
  		if(n != -1) return mPathName.substring(0, n+1);
 
  		return "";
  	}
  	
     /** 
  	 * Find the object with the given name.
  	 * Looks for elements with the keyword "OBJECT" and a value of the
  	 * given name. If such an element is found then the corresponding 
  	 * element with the keyword "END_OBJECT" is located. The passed name 
  	 * can contain regular expressions. Any occurrence of a "*" in the 
  	 * string is converted to the regular expression ".*" to match 
  	 * any number of characters. 
  	 * The search begins at the first element in the label and extends 
  	 * to the last.
 	 *
 	 * @param name	the name of the object to find. This can contain regular expressions.
 	 *
      * @return          a {@link PDSItem} the indicates the start and end of the object within
      *					the label.
      *
      * @since           1.0
      */
  	public PDSItem findObject(String name) {
  		return findObject(name, 0, -1);
  	}
  	
     /** 
  	 * Find the object with the given name within a portion of a label.
  	 * Looks for elements with the keyword "OBJECT" and a value of the
  	 * given name. If such an element is found then the corresponding 
  	 * element with the keyword "END_OBJECT" is located. The passed name 
  	 * can contain regular expressions. Any occurrence of a "*" in the 
  	 * string is converted to the regular expression ".*" to match 
  	 * any number of characters. 
  	 * The search begins at the first element indicated the {@link PDSItem} 
  	 * and extends to the last.
 	 *
 	 * @param name		the name of the element to find. This can contain regular expressions.
 	 * @param item		the item the search is to constrained within. If item is null then the
 	 *					search will span the entire label.
 	 *
      * @return          a {@link PDSItem} the indicates the start and end of the object within
      *					the label.
      *
      * @since           1.0
      */
  	public PDSItem findObject(String name, PDSItem item) 
  	{
  		if(item == null) return findObject(name);
  		return findObject(name, item.mStart, item.mEnd);
  	}
  	
     /** 
  	 * Find the next object with the given name occurring after the passed item.
  	 * Looks for elements with the keyword "OBJECT" and a value of the
  	 * given name. If such an element is found then the corresponding 
  	 * element with the keyword "END_OBJECT" is located. The passed name 
  	 * can contain regular expressions. Any occurrence of a "*" in the 
  	 * string is converted to the regular expression ".*" to match 
  	 * any number of characters. 
  	 * The search begins at the element at the endAt position in the 
  	 * list and extends to the end of the label.
 	 *
 	 * @param name		the name of the element to find. This can contain regular expressions.
 	 * @param item		the item the search is to constrained within. If item is null then the
 	 *					search will span the entire label.
 	 *
      * @return          a {@link PDSItem} the indicates the start and end of the object within
      *					the label.
      *
      * @since           1.0
      */
  	public PDSItem findNextObject(String name, PDSItem item) 
  	{
  		return findNextObject(name, item, null);
  	}
  	
     /** 
  	 * Find the next object with the given name ocurring after the passed item
  	 * and within the passed object.
  	 * Looks for elements with the keyword "OBJECT" and a value of the
  	 * given name. If such an element is found then the corresponding 
  	 * element with the keyword "END_OBJECT" is located. The passed name 
  	 * can contain regular expressions. Any occurance of a "*" in the 
  	 * string is converted to the regular expression ".*" to match 
  	 * any number of characters. 
  	 * The search begins at the element at the endAt position in the 
  	 * list and extends to the end of the label.
 	 *
 	 * @param name		the name of the element to find. This can contain regular expressions.
 	 * @param item		the item the search is to constrained within. If item is null then the
 	 *					search will span the object.
 	 * @param object	the object the search is to constrained within. If object is null then the
 	 *					search will span the entire label.
 	 *
      * @return          a {@link PDSItem} the indicates the start and end of the object within
      *					the label.
      *
      * @since           1.0
      */
  	public PDSItem findNextObject(String name, PDSItem item, PDSItem object) 
  	{
  		if(item == null) return findObject(name, object);
  		if(item.mEnd == -1) return null;	// Can't start after end of label
  		
  		if(object == null) return findObject(name, item.mEnd, -1);
  		return findObject(name, item.mEnd, object.mEnd);
  	}
  	
     /** 
  	 * Find the object with the given name within a partion of a label.
  	 * Looks for elements with the keyword "OBJECT" and a value of the
  	 * given name. If such an element is found then the corresponding 
  	 * element with the keyword "END_OBJECT" is located. The passed name 
  	 * can contain regular expressions. Any occurance of a "*" in the 
  	 * string is converted to the regular expression ".*" to match 
  	 * any number of characters. 
  	 * The search begins at the element at the startAt position in the 
  	 * list and extends to the element at the endAt position.
 	 *
 	 * @param name		the name of the element to find. This can contain regular expressions.
 	 * @param startAt	the index of the element at which to start the search.
 	 * @param endAt		the index of the element at which to end the search.
 	 *
      * @return          a {@link PDSItem} the indicates the start and end of the object within
      *					the label.
      *
      * @since           1.0
      */
  	public PDSItem findObject(String name, int startAt, int endAt) 
  	{
  		int			i, k;
  		PDSElement	element;
  		PDSValue	value;
  		PDSItem		item = new PDSItem();
  		String		buffer;
  		
  		if(startAt == -1) startAt = 0;
  		if(endAt == -1) endAt = mElement.size();
  		
  		if(startAt >= mElement.size()) return null;
  		if(endAt > mElement.size()) endAt = mElement.size();
  	
  		name = name.replaceAll("\\*", ".*");
  		
  		// Search for start of object
  		for(i = startAt; i < endAt; i++) {
  			element = (PDSElement) mElement.get(i);
  			if(element.mKeyword.compareTo("OBJECT") == 0) {
  				if(element.mValue.size() == 0) continue;
  				value = (PDSValue) element.mValue.get(0);
  				buffer = value.mValue.trim();
  				if(buffer.matches(name)) { item.mStart = i; break; }
  			}
  		}
  		if(item.mStart == -1) return null;	// Not found
 
 		// Search for matching end of object
 		k = 0;
  		for(i = item.mStart; i < endAt; i++) {
  			element = (PDSElement) mElement.get(i);
  			if(element.mKeyword.compareTo("OBJECT") == 0) k++;
  			if(element.mKeyword.compareTo("END_OBJECT") == 0) {
  				k--;
  				if(k <= 0) { item.mEnd = i+1; break; }
  			}
  		}
  		
  		return item;	// found object
  	}
  	
     /** 
  	 * Find the value assocated with an element with the given name.
  	 * The passed name can contain regular expressions. Any occurance
  	 * of a "*" in the string is converted to the regular expression ".*"
  	 * to match any number of characters. Also any occurance of "^" is converted
  	 * to a literal "^" since it is a valid character in keywords.
  	 * The search begins at the first element in the label and extends to the last.
 	 *
 	 * @param name		the name of the element to find. This can contain regular expressions.
 	 *
      * @return          a {@link String} containing the value associated with the
      *					named element. If the element was not found the value returned is empty.
      *
      * @since           1.0
      */
  	public String getElementValue(String name) 
  	{
  		return getElementValue(name, false);
  	}
  	
     /** 
  	 * Find the value assocated with an element with the given name.
  	 * The passed name can contain regular expressions. Any occurance
  	 * of a "*" in the string is converted to the regular expression ".*"
  	 * to match any number of characters. Also any occurance of "^" is converted
  	 * to a literal "^" since it is a valid character in keywords.
  	 * The search begins at the first element in the label and extends to the last.
 	 *
 	 * @param name		the name of the element to find. This can contain regular expressions.
 	 * @param plain		flag indicating if the value is not to be adorned
  	 *                  with appropriate quotation marks.
 	 *
      * @return          a {@link String} containing the value associated with the
      *					named element. If the element was not found the value returned is empty.
      *
      * @since           1.0
      */
  	public String getElementValue(String name, boolean plain) 
  	{
  		return getElementValue(name, null, plain);
  	}
  	
     /** 
  	 * Find the value assocated with an element with the given name within 
  	 * a section of the label.
  	 * The passed name can contain regular expressions. Any occurance
  	 * of a "*" in the string is converted to the regular expression ".*"
  	 * to match any number of characters. Also any occurance of "^" is converted
  	 * to a literal "^" since it is a valid character in keywords.
  	 * The search begins at the first element in the label and extends to the last.
 	 *
 	 * @param name		the name of the element to find. This can contain regular expressions.
 	 * @param section	the section to search for the element.
 	 * @param plain		flag indicating if the value is not to be adorned
  	 *                  with appropriate quotation marks.
 	 *
      * @return          a {@link String} containing the value associated with the
      *					named element. If the element was not found the value returned is empty.
      *
      * @since           1.0
      */
  	public String getElementValue(String name, PDSItem section, boolean plain) {
  		PDSItem		item;
  		PDSElement	element;
  		
  		item = findItem(name, section);
  		if(item == null) return "";
  		if(!item.valid()) return "";
  		
  		element = getElement(item);
  		return element.valueString(plain);
  	}
  	
     /** 
  	 * Find the value associated with an element with the given name within 
  	 * implied object of the a label.
  	 * The passed name can contain regular expressions. Any occurrence
  	 * of a "*" in the string is converted to the regular expression ".*"
  	 * to match any number of characters. Also any occurrence of "^" is converted
  	 * to a literal "^" since it is a valid character in keywords.
  	 * The search begins at the first element in the label and extends to the last.
 	 *
 	 * @param name		the name of the element to find. This can contain regular expressions.
 	 *
      * @return          a {@link String} containing the value associated with the
      *					named element. If the element was not found the value returned is empty.
      *
      * @since           1.0
      */
  	public String getElementValueInObject(String name) 
  	{
  		return getElementValueInObject(name, null, true);
  	}
  	
     /** 
  	 * Find the value associated with an element with the given name within 
  	 * a section of the label without descending into sub-objects.
  	 * The passed name can contain regular expressions. Any occurrence
  	 * of a "*" in the string is converted to the regular expression ".*"
  	 * to match any number of characters. Also any occurrence of "^" is converted
  	 * to a literal "^" since it is a valid character in keywords.
  	 * The search begins at the first element in the label and extends to the last.
 	 *
 	 * @param name		the name of the element to find. This can contain regular expressions.
 	 * @param section	the section to search for the element.
 	 * @param plain		flag indicating if the value is not to be adorned
  	 *                  with appropriate quotation marks.
 	 *
      * @return          a {@link String} containing the value associated with the
      *					named element. If the element was not found the value returned is empty.
      *
      * @since           1.0
      */
  	public String getElementValueInObject(String name, PDSItem section, boolean plain) {
  		PDSItem		item;
  		PDSItem		object = new PDSItem();
  		PDSElement	element;
  		
  		if(section != null) { 
  			object.mStart = section.mStart; 
  			object.mEnd = section.mEnd; 
  		}
  		item = findItemInObject(name, object);
  		if(item == null) return "";
  		if(!item.valid()) return "";
  		
  		element = getElement(item);
  		return element.valueString(plain);
  	}
  	
     /** 
  	 * Find the item with the given name.
  	 * The passed name can contain regular expressions. Any occurrence
  	 * of a "*" in the string is converted to the regular expression ".*"
  	 * to match any number of characters. Also any occurrence of "^" is converted
  	 * to a literal "^" since it is a valid character in keywords.
  	 * The search begins at the first element in the label and extends to the last.
 	 *
 	 * @param name		the name of the item to find. This can contain regular expressions.
 	 *
      * @return          a {@link PDSItem} the indicates the start and end of the object within
      *					the label. If there is no other objects the PDSItem is invalid.
      *
      * @since           1.0
      */
  	public PDSItem findItem(String name) {
  		return findItem(name, 0, -1);
  	}
  	
     /** 
  	 * Find the item with the given name following the passed item in the
  	 * in the object containing the passed item.
  	 * The passed name can contain regular expressions. Any occurrence
  	 * of a "*" in the string is converted to the regular expression ".*"
  	 * to match any number of characters. Also any occurrence of "^" is converted
  	 * to a literal "^" since it is a valid character in keywords.
  	 * The search begins at the first element in the label and extends to the last.
 	 *
 	 * @param name		the name of the item to find. This can contain regular expressions.
 	 * @param item		a {@link PDSItem} that the search is to begin after. If <i>item</i> is <b>null</b>
 	 *					then search the entire section.
 	 * @param section		a {@link PDSItem} to limit the search within. If <i>section</i> is <b>null</b>
 	 *					then search the entire label.
 	 *
      * @return          a {@link PDSItem} the indicates the start and end of the object within
      *					the label. If there is no other objects the PDSItem is invalid.
      *
      * @since           1.0
      */
  	public PDSItem findNextItemInObject(String name, PDSItem item, PDSItem section) 
  	{
  		int	startAt = 0;
  		int	endAt = -1;
  		
  		if(section != null) {
 	 		startAt = section.mStart+1;
  			endAt = section.mEnd;
  		}
  		if(item != null) {
  			startAt = item.mEnd;
  		}
  		
  		return findItem(name, startAt, endAt, false);
  	}
  	
     /** 
  	 * Find the item with the given name in the object containing the passed item.
  	 * The passed name can contain regular expressions. Any occurrence
  	 * of a "*" in the string is converted to the regular expression ".*"
  	 * to match any number of characters. Also any occurrence of "^" is converted
  	 * to a literal "^" since it is a valid character in keywords.
  	 * The search begins at the first element in the label and extends to the last.
 	 *
 	 * @param name		the name of the item to find. This can contain regular expressions.
 	 * @param context		a {@link PDSItem} to limit the search within. If <i>item</i> is <b>null</b>
 	 *					then search the entire label.
 	 *
      * @return          a {@link PDSItem} the indicates the start and end of the object within
      *					the label. If there is no other objects the PDSItem is invalid.
      *
      * @since           1.0
      */
  	public PDSItem findItemInObject(String name, PDSItem context) 
  	{
  		return findItem(name, context.mStart, context.mEnd, false);
  	}
  	
     /** 
  	 * Find the item with the given name constrained to some portion of the label.
  	 * The passed name can contain regular expressions. Any occurrence
  	 * of a "*" in the string is converted to the regular expression ".*"
  	 * to match any number of characters. Also any occurrence of "^" is converted
  	 * to a literal "^" since it is a valid character in keywords.
  	 * The search begins at the first element indicated in the passed {@link PDSItem}
  	 * and extends to the last item.
 	 *
 	 * @param name		the name of the item to find. This can contain regular expressions.
 	 * @param item		a {@link PDSItem} to limit the search within. If <i>item</i> is <b>null</b>
 	 *					then search the entire label.
 	 *
      * @return          a {@link PDSItem} that indicates the location of the item 
      *					within the label. If there is no other objects the PDSItem is invalid.
      *
      * @since           1.0
      */
  	public PDSItem findItem(String name, PDSItem item) {
  		if(item == null) return findItem(name);
  		return findItem(name, item.mStart, item.mEnd, true);
  	}
  	
     /** 
  	 * Find the item with the given name constrained to some portion of the label.
  	 * The passed name can contain regular expressions. Any occurrence
  	 * of a "*" in the string is converted to the regular expression ".*"
  	 * to match any number of characters. Also any occurrence of "^" is converted
  	 * to a literal "^" since it is a valid character in keywords.
  	 * The search begins at the first element indicated in the passed {@link PDSItem}
  	 * and extends to the last item.
 	 *
 	 * @param name		the name of the item to find. This can contain regular expressions.
 	 * @param startAt	the index of the item to begin the search. If <i>startAt</i> 
 	 *					is -1 then search from the beginning of the label.
 	 * @param endAt		the index of the item to end the search. If <i>endAt</i> 
 	 *					is -1 then search to the end of the label.
 	 *
      * @return          a {@link PDSItem} that indicates the location of the item 
      *					within the label. If there is no other objects the PDSItem is invalid.
      *
      * @since           1.0
      */
  	public PDSItem findItem(String name, int startAt, int endAt) 
  	{
  		return findItem(name, startAt, endAt, true);
  	}
  	
     /** 
  	 * Find the item with the given name constrained to some portion of the label.
  	 * The passed name can contain regular expressions. Any occurrence
  	 * of a "*" in the string is converted to the regular expression ".*"
  	 * to match any number of characters. Also any occurrence of "^" is converted
  	 * to a literal "^" since it is a valid character in keywords.
  	 * The search begins at the first element indicated in the passed {@link PDSItem}
  	 * and extends to the last item.
 	 *
 	 * @param name		the name of the item to find. This can contain regular expressions.
 	 * @param startAt	the index of the item to begin the search. If <i>startAt</i> 
 	 *					is -1 then search from the beginning of the label.
 	 * @param endAt		the index of the item to end the search. If <i>endAt</i> 
 	 *					is -1 then search to the end of the label.
 	 * @param global	controls whether the search is through all elements or constrained
 	 *                  to the containing object 
 	 *
      * @return          a {@link PDSItem} that indicates the location of the item 
      *					within the label. If there is no other objects the PDSItem is invalid.
      *
      * @since           1.0
      */
  	public PDSItem findItem(String name, int startAt, int endAt, boolean global) {
  		int			i;
  		PDSElement	element;
  		PDSItem		item = new PDSItem();
  		PDSItem		object;
  		
  		if(startAt == -1) startAt = 0;
  		if(endAt == -1) endAt = mElement.size();
  		
  		name = name.replaceAll("\\^", "\\\\^");
  		name = name.replaceAll("\\*", ".*");
  		
  		// Search for start of object
  		for(i = startAt; i < endAt; i++) {
  			element = (PDSElement) mElement.get(i);
  			if(element.mKeyword.matches(name)) { item.mStart = i; item.mEnd = i+1; break; }
  			if(!global) {	// Limit to elements in current object
 	 			if(element.mKeyword.matches("OBJECT") && i != startAt) {	// Skip object
 	 				object = findObject(element.valueString(true), i, -1);
 	 				if(isValidItem(object)) i += (object.mEnd - object.mStart);
 	 			}
 	 		}
  		}
  		
  		return item;	// Not found
  	}
  	
     /** 
  	 * Find the next item with the given name starting at some point within the label.
  	 * The passed name can contain regular expressions. Any occurrence
  	 * of a "*" in the string is converted to the regular expression ".*"
  	 * to match any number of characters. Also any occurrence of "^" is converted
  	 * to a literal "^" since it is a valid character in keywords.
  	 * The search begins at the first element indicated in the passed {@link PDSItem} and 
  	 * extends to the end of the label.
 	 *
 	 * @param name		the name of the item to find. This can contain regular expressions.
 	 * @param item		a {@link PDSItem} indicating where to begin the search.
 	 *
      * @return          a {@link PDSItem} the indicates the start and end of the object within
      *					the label. If there is no other objects the PDSItem is invalid.
      *
      * @since           1.0
      */
  	public PDSItem findNextItem(String name, PDSItem item) {
  		if(item == null) return findItem(name);
  		return findItem(name, item.mEnd, -1);
  	}
  	
     /** 
  	 * Return the next item after the given item.
 	 *
 	 * @param item		a {@link PDSItem} indicating where to begin the search.
 	 *
      * @return          a {@link PDSItem} the indicates the start and end of the object within
      *					the label. If there is no other objects the PDSItem is invalid.
      *
      * @since           1.0
      */
  	public PDSItem nextItem(PDSItem item) {
  		PDSItem		nextItem = new PDSItem();
  		if(item.mEnd < mElement.size()) { nextItem.mStart = item.mEnd; nextItem.mEnd = nextItem.mStart+1; }
  		
  		return nextItem;
  	}
  	
     /** 
  	 * Return the element data associated with an item.
  	 * 
 	 * @param item		a {@link PDSItem} to return the element data for. The data associated with the
 	 *					first element of the item is returned.
 	 *
      * @return          a {@link PDSElement} associated with the first element of the item.
      *					the label. If no element is associated with the item <b>null</b>
      *					is returned.
      *
      * @since           1.0
      */
  	public PDSElement getElement(PDSItem item)
  	{
  		if(item.valid() && item.mStart < mElement.size()) return (PDSElement) mElement.get(item.mStart);
  		return null;
  	}
  	
  	
     /** 
  	 * Find the element with the given name.
  	 * The passed name can contain regular expressions. Any occurrence
  	 * of a "*" in the string is converted to the regular expression ".*"
  	 * to match any number of characters. Also any occurrence of "^" is converted
  	 * to a literal "^" since it is a valid character in keywords.
  	 * The search begins at the first element in the label and extends to the last.
 	 *
 	 * @param name		the name of the element to find. This can contain regular expressions.
 	 *
      * @return          a {@link PDSElement} associated with the first element of the item.
      *					the label. If no element is associated with the item <b>null</b>
      *					is returned.
      *
      * @since           1.0
      */
  	public PDSElement getElement(String name)
  	{
  		PDSItem		item;
  		PDSElement	element;
  		
  		item = findItem(name, 0, -1);
  		if(!item.valid()) return null;
  		
  		element = getElement(item);
  		return element;
  	}
  	
     /** 
  	 * Find the element with the given name within a section (usually an object).
  	 * The passed name can contain regular expressions. Any occurrence
  	 * of a "*" in the string is converted to the regular expression ".*"
  	 * to match any number of characters. Also any occurrence of "^" is converted
  	 * to a literal "^" since it is a valid character in keywords.
  	 * The search begins at the first element in the label and extends to the last.
 	 *
 	 * @param name		the name of the element to find. This can contain regular expressions.
 	 * @param section	the item defining the section to search.
 	 *
      * @return          a {@link PDSElement} associated with the first element of the item.
      *					the label. If no element is associated with the item <b>null</b>
      *					is returned.
      *
      * @since           1.0
      */
  	public PDSElement getElement(String name, PDSItem section)
  	{
  		PDSItem		item;
  		PDSElement	element;
  		
  		item = findItem(name, section);
  		if(!item.valid()) return null;
  		
  		element = getElement(item);
  		return element;
  	}
  	
     /** 
  	 * Replace an item in a label with another label.
  	 * 
 	 * @param item		a {@link PDSItem} indicating the elements to replace.
 	 * @param label		the label to place where <i>item</i> is currently.
 	 *
      * @since           1.0
      */
  	public void replace(PDSItem item, PDSLabel label) {
  		insertAfter(item, label);
  		remove(item);
  	}
  	
     /** 
  	 * Add an element to the end of a label.
  	 * 
 	 * @param element	the element to add to the end of the label.
 	 *
      * @since           1.0
      */
  	public void add(PDSElement element) {
  		mElement.add(element);
  	}
  	
     /** 
  	 * Add a label  to the end of a label.
  	 * 
 	 * @param label		the label to add to the end of the label.
 	 *
      * @since           1.0
      */
  	public void add(PDSLabel label) {
  		mElement.addAll(label.mElement);
  	}
  	
     /** 
  	 * Insert a label before another element in this label. 
  	 * If the element location is invalid nothing is done.
  	 * 
 	 * @param item		the location of the element before which the label is inserted.
 	 * @param label		the label to place before the passed item.
 	 *
      * @since           1.0
      */
  	public void insertBefore(PDSItem item, PDSLabel label) {
  		if(!item.valid()) return;
  		mElement.addAll(item.mStart, label.mElement);
  	}
  	
     /** 
  	 * Insert a label after another element in this label. 
  	 * If the location of the element is passed the end of 
  	 * this label the label is appended to this label. 
  	 * If the element location is invalid nothing is done.
  	 * 
 	 * @param item		the location of the element after which the label is inserted.
 	 * @param label		the label to place after the passed item.
 	 *
      * @since           1.0
      */
  	public void insertAfter(PDSItem item, PDSLabel label) {
  		if(!item.valid()) return;
 		if(item.mEnd >= mElement.size()) add(label);
 		else mElement.addAll(item.mEnd, label.mElement);
  	}
  	
     /** 
  	 * Insert an element before another element in this label. 
  	 * If the element location is invalid nothing is done.
  	 * 
 	 * @param item		the location of the element before which the element is inserted.
 	 * @param element	the element to place before the passed item.
 	 *
      * @since           1.0
      */
  	public void insertBefore(PDSItem item, PDSElement element) {
  		if(!item.valid()) return;
  		mElement.add(item.mStart, element);
  	}
  	
     /** 
  	 * Insert an element after another element in this label. 
  	 * If the location of the element is passed the end of 
  	 * this label the label is appended to this label. 
  	 * If the element location is invalid nothing is done.
  	 * 
 	 * @param item		the location of the element after which the element is inserted.
 	 * @param element	the element to place after the passed item.
 	 *
      * @since           1.0
      */
  	public void insertAfter(PDSItem item, PDSElement element) {
  		if(!item.valid()) return;
 		if(item.mEnd >= mElement.size()) add(element);
 		else mElement.add(item.mEnd, element);
  	}
  	
     /** 
  	 * Remove a range of elements from the label.
  	 * If the range of elements in the passed item is invalid nothing is done.
  	 * 
 	 * @param item		the location of the elements to remove.
 	 *
      * @since           1.0
      */
  	public void remove(PDSItem item) {
  		if(!item.valid()) return;
  		if(item.mEnd > mElement.size()) item.mEnd = mElement.size();
  		for(int i = item.mStart; i < item.mEnd; i++) mElement.remove(item.mStart);
  	}
  	
     /** 
  	 * Extract a portion of a label into a new instance of label.
  	 * The range of elements to extract is given in a {@link PDSItem}
  	 * 
 	 * @param item		the location of the elements to extract.
 	 *
      * @since           1.0
      */
  	public PDSLabel extract(PDSItem item) {
  		PDSLabel	label = new PDSLabel();
  		PDSElement	element;
  		PDSElement	newElement;
  		
  		if(item.valid()) {
 	 		for(int i = item.mStart; i < item.mEnd; i++) {
 	 			element = (PDSElement) mElement.get(i);
 	 			newElement = element.copy();
 	 			label.mElement.add(newElement);
 	 		}
  		}
 		return label;
  	}
  	
  	/** 
      * Search the label and return a list to all points to files.
      * A pointer to a file is defined as any pointer keyword.
      * The first element of the value is treated as a file name.
      *
      * @return		a list of file names pointed to within the label. 
      *				The list is in the form of an {@link ArrayList} of 
      *				{@link String} objects. If no pointers are found then
      *				<code>null</code> is returned.
      * @since           1.0
      */
 	public ArrayList<String> filePointers() {
 		PDSItem		item = new PDSItem();
 		ArrayList<String>	list = new ArrayList<String>();
 		PDSElement	element;
 		PDSValue	value;
 		boolean		add;
 		String		temp;
 		Iterator<String> 	li ;
 		
 		item.empty();
 		item = findNextItem("^*", item);
 		while(item.valid()) {
 			element = getElement(item);
 			for(int i = 0; i < element.mValue.size(); i++) {
 				value = (PDSValue) element.mValue.get(i);
 				if(value.mType == PDSValue.TYPE_STRING) {
 					String buffer = new String();
 					buffer = value.mValue;
 					// Check if already in list
 					add = true;
 					li = list.iterator();
 					while(li.hasNext()) {
 						temp = (String) li.next();
 						if(temp.compareTo(buffer) == 0) { add = false; break; }
 					}
 					if(add)list.add(buffer);
 				}
 			}
 			item = findNextItem("^*", item);
 		}
 		if(list.size() == 0) return null;
 		
 		return list;
  	}
  	
  	/** 
      * Expand INCLUDE and STRUCTURE pointers.
      * For each INCLUDE or STRCUTURE pointer treat the value as the name of the file 
      * to include. The contents of the file are read, parsed and inserted in place of the pointer.
      *
      * @return		a new PDSLabel with all include and structure pointers expanded. 
      * @since           1.0.2
      */
 	public PDSLabel expandPointers(String path) 
 		throws PDSException
 	{
  		PDSElement	element;
  		int			i;
  		
  		int endAt = mElement.size();
 
  		PDSLabel label = new PDSLabel();
  		
  		label.mPathName = mPathName;
  		
  		for(i = 0; i < endAt; i++) {
  			element = (PDSElement) mElement.get(i);
 			if(element.mKeyword.compareTo("^INCLUDE") == 0 || element.mKeyword.compareTo("^STRUCTURE") == 0 ) {
 				PDSLabel sublabel = new PDSLabel();
 				try {
 					String fileName = element.valueString(true);
 					File check1 = new File(fileName);
 					if(! check1.exists() && path != null) {
 						fileName = path + "/" + fileName;
 					}
 					sublabel.parse(fileName);
 					int subendat = sublabel.mElement.size();
 					for(int n = 0; n < subendat; n++) {
 						PDSElement elem = (PDSElement) sublabel.mElement.get(n);
 						label.add(elem.copy());
 					}
 				}  catch(Exception e) {
 					throw(new PDSException(e.getMessage()) );
 				}
 			} else {
 				label.add(element.copy());
 			}
 		}
  		
  		return label;
  	}
  	
     /** 
      * Print all elements in the label according to PDS specifications 
      * for label files using default indent and equal sign placement. 
 	 * 
      * @since           1.0
      */
  	public void print() {
  		print(System.out);
  	}
  	
     /** 
      * Print all elements in the label according to PDS specifications 
      * for label files using default indent and equal sign placement. 
 	 * 
 	 * @param	pathName	the path and name of the file to write the output to.
 	 *
      * @since           1.0
      */
  	public void print(String pathName) {
 		FileOutputStream	file;
 		PrintStream			out;
  		mPathName = pathName;
  		
 		try {
 			file = new FileOutputStream(mPathName);
 			out = new PrintStream(file);
 	 		print(out);
 		} catch(IOException e) {
 			System.out.println("Unable to open file: " + mPathName);
 			System.out.println("    Reason: " + e.getMessage());
 			return;
 		}
  	}
  	
     /** 
      * Print all elements in the label according to PDS specifications 
      * for label files using default indent and equal sign placement. 
 	 * 
 	 * @param	pathName	the path and name of the file to write the output to.
 	 * @param	indent		the number of spaces to indent each line at each level.
 	 * @param	equal		the position to align equal signs following keywords.
 	 *
      * @since           1.0
      */
  	public void print(String pathName, int indent, int equal) {
 		FileOutputStream	file;
 		PrintStream			out;
  		mPathName = pathName;
  		
 		try {
 			file = new FileOutputStream(mPathName);
 			out = new PrintStream(file);
 	 		print(out, indent, equal);
 		} catch(IOException e) {
 			System.out.println("Unable to open file: " + mPathName);
 			System.out.println("    Reason: " + e.getMessage());
 			return;
 		}
  	}
  	
     /** 
      * Print all elements in the label according to PDS specifications 
      * for label files using default indent and equal sign placement. 
 	 * 
      * @param out    	the stream to print the element to.
      *
      * @since           1.0
      */
  	public void print(PrintStream out) {
  		print(out, 2, 29, 0, -1);
  		out.print("END\r\n");
  	}
  	
     /** 
      * Print all elements in the label according to PDS specifications 
      * for label files. Each line that is output can be indented with the equal sign
      * (when present) placed at a fixed position. Output printed to System.out.
      * Each occurrence of an OBJECT is indented on level.
 	 * 
      * @param out    	the stream to print the element to.
      * @param indent    the number of spaces to indent for each level.
      * @param equal		the number of spaces from the end of the indent
      *					to align the equal sign for elements which have 
      *					a keyword and value.
      *
      * @since           1.0
      */
  	public void print(PrintStream out, int indent, int equal) {
  		print(out, indent, equal, 0, -1);
  		out.print("END\r\n");
  	}
  	
     /** 
      * Print a range of elements in the label according to PDS specifications 
      * for label files. Each line that is output can be indented with the equal sign
      * (when present) placed at a fixed position. Output is printed to the print stream.
      * Each occurrence of an OBJECT is indented on level.
 	 * 
      * @param out    	the stream to print the element to.
      * @param indent    the number of spaces to indent for each level.
      * @param equal		the number of spaces from the end of the indent
      *					to align the equal sign for elements which have 
      *					a keyword and value.
      * @param item		the item to output.
      *
      * @since           1.0
      */
  	public void print(PrintStream out, int indent, int equal, PDSItem item) {
  		if(item == null) print(out, indent, equal);
  		else print(out, indent, equal, item.mStart, item.mEnd);
  	}
  	
  	/** 
      * Print a range of elements in the label according to PDS specifications 
      * for label files. Each line that is output can be indented with the equal sign
      * (when present) placed at a fixed position. Output is printed to the print stream.
      * Each occurrence of an OBJECT is indented on level.
 	 * 
      * @param out    	the stream to print the element to.
      * @param indent    the number of spaces to indent for each level.
      * @param equal		the number of spaces from the end of the indent
      *					to align the equal sign for elements which have 
      *					a keyword and value.
      * @param startAt	the first element to output.
      * @param endAt		the last element to output.
      *
      * @since           1.0
      */
 	public void print(PrintStream out, int indent, int equal, int startAt, int endAt) {
  		PDSElement	element;
  		int			i;
  		int			level = 0;
  		
  		if(startAt == -1) startAt = 0;
  		if(endAt == -1) endAt = mElement.size();
  		
  		for(i = startAt; i < endAt; i++) {
  			element = (PDSElement) mElement.get(i);
 			if(element.mKeyword.compareTo("END_OBJECT") == 0) level--;
  			element.print(out, indent, equal, level);
 			if(element.mKeyword.compareTo("OBJECT") == 0) level++;
 		}
  	}
  	
  	/** 
      * Prints out the label as a set of variable definition in the PPI Ruleset language.
      * Each keyword is preceded by a "$" to make it a variable definition and given
      * a suffix corresponding to the sequential order of the object that contains it.
      * For example the keyword "DESCRIPTION" in the first object will have the variable
      * definition of "$DESCRIPTION_1", the occurrence in the second object with have
      * a definition of "$DESCRIPTION_2". The "OBJECT" and "END_OBJECT" elements are not
      * printed. There is an implicit "FILE" around any label definition, so keywords appearing
      * outside any explicit object have the suffix of "_1", the first explicit object
      * will have a suffix of "_2".
 	 * 
      * @param out    	the stream to print the element to.
      *
      * @since           1.0
      */
 	public void printVariable(PrintStream out) {
  		PDSElement	element;
  		PDSElement	temp;
  		int			i;
  		int			occurance = 1;
  		int			startAt = 0;
  		int			endAt = mElement.size();
  		
  		for(i = startAt; i < endAt; i++) {
  			element = (PDSElement) mElement.get(i);
 			if(element.mKeyword.compareTo("OBJECT") == 0) { occurance++; }
 			if(element.mKeyword.compareTo("END") == 0) { continue; }
  			else {
  				temp = element.copy();
  				if(temp.mKeyword.length() != 0) {	// Not empty
  					temp.mKeyword = "$" + temp.mKeyword.replace('^', 'p') + "_" + occurance;
  					temp.print(out, 0, 29, 0);
  				}
  			}
 		}
  	}
  	
  	/** 
      * Display the passed text as a message to System.out
      * Precedes the text with a standard phrase.
 	 * 
      * @param text    	the variable portion of the message text.
      *
      * @since           1.0
      */
 	public void printMessage(String text) 
 	{
 		mLog.println("Unable to parse file: " + mPathName);
 		mLog.println("   Reason: " + text);
  	}
  	
  	/** 
      * Set the log print stream
 	 * 
      * @param stream    	the print stream.
      *
      * @since           1.0
      */
 	public void setLog(PrintStream stream) 
 	{
 		mLog = stream;
  	}
  	
  	/** 
      * Creates a DOM (Document Object Model) representation of the label.
      * Keywords in the label are elements in the DOM. 
      * Values in the label are placed between the element tags. 
      * Units are made attributes of the element.
 	 * 
      * @return		A {@link Document} object containing a representation of the label. 
      *
      * @since           1.0
      */
 	public Document getDocument() 
 	{
 		Document	doc = null;
 		
 		try {
 	        //We need a Document
 	        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
 	        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
 	        
 	        doc = docBuilder.newDocument();
 	        
             // Create the XML tree
             // create the root "label" element and begin pushing elements
 			pushObject(doc, null, "LABEL", 0);
 		} catch (Exception e) {
 			// e.printStackTrace();
 			doc = null;
 		}
         
 		return doc;
  	}
 
  	/** 
      * Generates an XML representation of the label and streams it to the 
      * print stream.
 	 * 
      * @param out    	the stream to print the element to.
      *
      * @since           1.0
      */
 	public void printXML(PrintStream out)
 	{
 		try {
 			Document doc = getDocument();
 			
 	        //set up a transformer
 	        TransformerFactory transfac = TransformerFactory.newInstance();
 			try {	// Need for Java 1.5 to work properly
 				transfac.setAttribute("indent-number", new Integer(4));
 			} catch(Exception ie) {
 			}
 	        Transformer trans = transfac.newTransformer(getDefaultStyleSheet());
 	        
 	        // Set up desired output format
 	        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
 	        trans.setOutputProperty(OutputKeys.INDENT, "yes");
 	
 	        //create string from xml tree
 	        // StringWriter sw = new StringWriter();
 	        StreamResult result = new StreamResult(out);
 	        DOMSource source = new DOMSource(doc);
 	        
 	        trans.transform(source, result);
 	        // String xmlString = sw.toString();
 	
 	        //print xml
 	        // out.println(xmlString);
 		} catch (Exception e) {
 			// e.printStackTrace();
 		}
 	}
 
  	/** 
      * Obtain a StreamSource to the default XML Style Sheet.
 	 * 
      * @return          a {@link StreamSource} which can be used to read the default
      *					style sheet.
      *
      * @since           1.0
      */
 	public StreamSource getDefaultStyleSheet()
 	{
 		StringReader reader = new StringReader(
 			  "<!DOCTYPE stylesheet ["
 			+ "   <!ENTITY cr \"<xsl:text> </xsl:text>\">"
 			+ "]>"
  			+ "<xsl:stylesheet"
     		+ "   xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\""
     		+ "   xmlns:xalan=\"http://xml.apache.org/xslt\""
     		+ "   version=\"1.0\""
     		+ ">"
     		+ ""
     		+ "<xsl:output method=\"xml\" indent=\"yes\" xalan:indent-amount=\"4\"/>"
     		+ ""
       		+ "<!-- copy out the xml -->"
     		+ "<xsl:template match=\"* | @*\">"
         	+ "   <xsl:copy><xsl:copy-of select=\"@*\"/><xsl:apply-templates/></xsl:copy>"
     		+ "</xsl:template>"
 			+ "</xsl:stylesheet>"
 			);
 			
 			return new StreamSource(reader);
 	}
 
  	/** 
      * Pushes (adds) a PDS label object to the document tree (DOM). The index 
      * of the first element of of the object is passed. PDS Label elements are 
      * added until an END_OBJECT is encounter with the given "name" or the end of 
      * list in reached. The index of the last element pushed is returned. 
      * This permits the calling method to walk through
      * element list for the PDS label and create the proper hierarchy.
 	 * 
      * @param doc    	the {@link Document} to add the elements to.
      * @param parent    the {@link Element} in doc under which to add the elements.
      * @param name    	the name to give the group of elements. 
      * @param start    	the index on the first element in PDS label element list to add. 
 	 *
      * @return          the index of the last element added to the document.
      *
      * @since           1.0
      */
 	public int pushObject(Document doc, Element parent, String name, int start)
 	{
 		Element	elem;
 		Element	object;
 		Comment	comment;
 		String	value;
 		String	keyword;
 		String	prefix;
 		Text	text;
 		PDSElement	element;
 		int		i;
 		
 		object = doc.createElement(name);
 		if(parent == null) doc.appendChild(object);
         else parent.appendChild(object);
         
  		for(i = start; i < mElement.size(); i++) {
  			element = (PDSElement) mElement.get(i);
  			value = element.valueString(true, false);
  			if(element.mKeyword.matches("OBJECT")) {	// Object is special
  				i = pushObject(doc, object, value, i+1);
  			} else {
 	 			if(element.mKeyword.matches("END_OBJECT") && value.matches(name)) return i++;
 	 			switch(element.mType) {
 	 			case PDSElement.TYPE_COMMENT:
 	 				comment = doc.createComment(element.mComment);
 	 				object.appendChild(comment);
 	 				break;
 	 			case PDSElement.TYPE_BLANK_LINE:
 	 				// Do nothing
 	 				break;
 	 			default:
 	 				keyword = element.mKeyword;
 	 				
 	 				// Check for SFDU "keyword"
 	 				prefix = keyword.substring(0, 4);
 	 				if(prefix.compareToIgnoreCase("CCSD") == 0) {
 	 					value = keyword;
 	 					keyword = "SFDU";
 	 				}
 	 				
 	 				// Now add element
 	 				// Check if pointer - adjust accordingly
 	 				if(keyword.charAt(0) == '^') { // If pointer handle a little differently
 	 					keyword = keyword.substring(1); 
 	 					elem = doc.createElement("POINTER");
 	 					elem.setAttribute("object", keyword);
 	 				} else {	// Ordinary element
 		 				elem = doc.createElement(keyword);
 	 				
 		 				if(element.mType == PDSElement.TYPE_ORDERED) elem.setAttribute("list", "ordered");
 		 				if(element.mType == PDSElement.TYPE_UNORDERED) elem.setAttribute("list", "unordered");
 	 				}
 	 				
 	 				object.appendChild(elem);
 	 				
 	 				// Add value to child element
 	 				text = doc.createTextNode(value);
 	 				elem.appendChild(text);
 	 				break;
 	 			}
  			}
  		}
  		return i;
  	}
 
  	/** 
      * Generate a nested HashMap for the label that includes product information.
      * The {@link HashMap} is a keyword, value map. Each OBJECT is stored
      * in a ArrayList with a HashMap of the object. The value assigned
      * to the OBJECT keyword is mapped to "OBJECT_NAME" in the {@link HashMap}.
      * The first file referenced by an object pointer is assigned to "PRODUCT_FILE"
      * and the MD5 checksum for the file is assigned to "PRODUCT_MD5".
      *
      * @since           1.0
      */
 	public HashMap<String, Object> getHashMap(int startAt) {
 		mProductFile.clear();
 		HashMap<String, Object> map = getLabelHashMap(startAt);
 		File file = new File(mPathName);
 		if(mProductFile.isEmpty()) {
 			mProductFile.add(file.getName());
 		}
 		map.put("PRODUCT_FILE", mProductFile.get(0));
 		try {
 			String fileName = file.getParent() + "/" + mProductFile.get(0);
 			map.put("PRODUCT_FILE_MD5", igpp.util.Digest.digestFile(fileName));
 		} catch(Exception e) {
 			map.put("PRODUCT_FILE_MD5", "");
 			
 		}
 		
 		return map;
 	}
 	
  	/** 
      * Generate a nested HashMap for the label.
      * The {@link HashMap} is a keyword, value map. Each OBJECT is stored
      * in a ArrayList with a HashMap of the object. The value assigned
      * to the OBJECT keyword is mapped to "OBJECT_NAME" in the {@link HashMap}.
      * 
      *
      * @since           1.0
      */
 	public HashMap<String, Object> getLabelHashMap(int startAt) {
  		if(startAt < 0) startAt = 0;
  		int endAt = mElement.size();
  		
  		HashMap<String, Object> map = new HashMap<String, Object>();
  		HashMap<String, ArrayList<HashMap<String, Object>>> object = new HashMap<String, ArrayList<HashMap<String, Object>>>();
  		
  		String[]  className = { "TABLE", "SPREADSHEET", "IMAGE", "SERIES", "SPECTRUM", "QUBE", "HISTOGRAM", "PALETTE", "HEADER" };
  		
  		// Use external variable mLineCount to walk the element stack.
  		// This allows us to handle nested OBJECT definitions properly.
  		for(mLineCount = startAt; mLineCount < endAt; mLineCount++) {
  			PDSElement element = (PDSElement) mElement.get(mLineCount);
  			
 			if(element.mKeyword.compareTo("END_OBJECT") == 0) { return(map); }
 			
 			if(element.mKeyword.compareTo("OBJECT") == 0) {
 				// Get object class
 				String name = element.valueString(true, false);
 				int n = name.lastIndexOf('_');
 				if(n != -1) { // Check if a named class
 					String cname = name.substring(n+1);
 					if(igpp.util.Text.isInList(cname, className)) {
 						name = cname;
 					}
 				}
 				
 				ArrayList<HashMap<String, Object>> stack = object.get(name);
 				if(stack == null) {	// One does not exist - create it
 					stack = new ArrayList<HashMap<String, Object>>();
 					object.put(name, stack);	// Store ArrayList in local object list.
 					map.put(name, stack);	// Put ArrayList in map
 				}
 				
 				// Get Object HashMap and save
 				HashMap<String, Object> submap = getLabelHashMap(mLineCount+1);	// Get Object HashMap
 				String objectName = element.valueString(true, false);
 				submap.put("OBJECT_NAME", objectName);	// Add OBJECT_NAME
 				// Check if there is a pointer to this object
 				PDSElement elem = getElement("^" + objectName);
 				if(elem != null) {
 					String fileName = elem.value(0);
 					for(String pname : mProductFile) {
 						if(pname.compareTo(fileName) == 0) { break; }
 					}
 					mProductFile.add(fileName);
 					submap.put("OBJECT_FILE", fileName);
 					try {
 						File file = new File(mPathName);
 						String pathName = file.getParent() + "/" + fileName;
 						submap.put("OBJECT_FILE_MD5", igpp.util.Digest.digestFile(pathName));
 					} catch(Exception e) {
 						submap.put("OBJECT_FILE_MD5", "");				
 					}
 					if(elem.valueSize() > 1) {
 						submap.put("OBJECT_LOCATION", elem.value(1));
 					}
 				}
 				stack.add(submap);	// Put object Map in ArrayList
 			} else {
 				if(element.mKeyword.length() > 0) {
 					ArrayList<String> list = element.valueList(true);
 					if(list.size() == 1) map.put(element.mKeyword, element.valueString(true, false));
 					else map.put(element.mKeyword, list);
 				}
 			}
 		}
  		return map;
  	}
 }
 
  
