 package org.data2semantics.filters;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.XMLFilterImpl;
 
 /**
  * XML Filter class that will shift the index/offset from/to of annotations
  * found in annotation beans result from bioportal. That is needed when original
  * file is split into smaller parts.
  * 
  * @author wibisono
  * 
  */
 public class D2S_PrefixShiftXMLFilter extends XMLFilterImpl {
 
 	int splitOffset;
 
 	public D2S_PrefixShiftXMLFilter(int splitOffset) {
 
 		this.splitOffset = splitOffset;
 	}
 
 	String currentQName;
 
 	@Override
 	public void startElement(String uri, String localName, String qName,
 			Attributes atts) throws SAXException {
 
 		currentQName = qName;
 		super.startElement(uri, localName, qName, atts);
 
 	}
 
 	@Override
 	public void endElement(String uri, String localName, String qName)
 			throws SAXException {
 		
 		
 		currentQName = "";
 		super.endElement(uri, localName, qName);
 
 	}
 
 	/**
 	 * Here is where actually transformation/offset happened
 	 */
 	@Override
 	public void characters(char[] ch, int start, int length)
 			throws SAXException {
 
 		// We are going to pass the integer value of this character, add it with
 		// split offset and return it back to the serializer
 
 		if (currentQName.equalsIgnoreCase("from")
 				|| currentQName.equalsIgnoreCase("to")) {
 			Integer curValue = new Integer(new String(ch, start, length));
 
 			// Shift the current position
 			curValue += splitOffset;
 
 			// Write back the new offset to character arrays and let the
 			// serializer do the rest.
 			ch = curValue.toString().toCharArray();
 			start = 0;
 			length = ch.length;
 
 		}
 		super.characters(ch, start, length);
 	}
 
 }
