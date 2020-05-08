 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 import org.xml.sax.helpers.XMLFilterImpl;
 
 /** Replace any found {@code pages} XML element in the 
  * dblp file with the corresponding {@code from}  and {@code to} elements.
  *  
  * @author Michael Weichselbaumer
  *
  */
 public class PagesFilter extends XMLFilterImpl {
 	
 	private boolean workingOnPagesElement = false;
 	private String pageFrom;
 	private String pageTo;
 	
 	private static final String PAGES_TAG = "pages";
 	private static final String FROM_TAG = "from";
 	private static final String TO_TAG = "to";
 
 	/** Checks if element is {@code pages} element and initiates transformation into {@code from} 
 	 * and {@code to} elements.
 	 * <p>
 	 * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
 	 */
 	@Override
 	public void startElement(String uri, String localName, String qName,
 			Attributes atts) throws SAXException {
 		if (PAGES_TAG.equalsIgnoreCase(localName)) {
 			workingOnPagesElement = true;
 		} else {
 			super.startElement(uri, localName, qName, atts);
 		}
 	}
 
 	/** Checks if ending element is {@code pages} element and passes its replacement elements to the specified {@code ContentHandler}. <p>
 	 * 
 	 * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public void endElement(String uri, String localName, String qName)
 			throws SAXException {
 		if (PAGES_TAG.equalsIgnoreCase(localName)) {			
 			generateNewElement(uri, FROM_TAG, FROM_TAG, pageFrom, new AttributesImpl());
 			generateNewElement(uri, TO_TAG, TO_TAG, pageTo, new AttributesImpl());
 			workingOnPagesElement = false;
 		} else {
 			super.endElement(uri, localName, qName);
 		}
 	}
 	
 	/** Creates a new element with given parameters by passing it to super instance.
 	 * 
 	 * @param uri
 	 * @param localName
 	 * @param qName
 	 * @param characters
 	 * @param attrs
 	 * @throws SAXException
 	 */
 	private void generateNewElement(String uri, String localName, String qName, String characters, Attributes attrs) throws SAXException {
 		super.startElement(uri, localName, qName, attrs);
 		char [] charArray = characters.toCharArray();
 		super.characters(charArray, 0, charArray.length);
 		super.endElement(uri, localName, qName);
 	}
 
 	/** If character sequence belongs to {@code pages} element, page information is extracted
 	 *  in order to be put in replacement elements {@code from} and {@code to}.
 	 * <p>
 	 * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
 	 */
 	@Override
 	public void characters(char[] ch, int start, int length) throws SAXException {
 		if (workingOnPagesElement) {
 			String content = String.valueOf(ch, start, length);
 			int separatorIndex = content.indexOf('-'); 
			if(separatorIndex != -1){
				pageFrom = content.substring(0, separatorIndex);
				pageTo = content.substring(separatorIndex+1);
			}else{
				pageFrom = content;
				pageTo = content;
			}
 		} else {
 			super.characters(ch, start, length);
 		}
 	}
 
 }
