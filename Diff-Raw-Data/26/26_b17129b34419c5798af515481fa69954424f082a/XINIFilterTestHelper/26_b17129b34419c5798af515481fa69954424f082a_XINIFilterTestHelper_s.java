 package net.sf.okapi.filters.xini;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.ByteArrayOutputStream;
 import java.io.Serializable;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.exceptions.OkapiBadFilterParametersException;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.filters.xini.jaxb.Element;
 import net.sf.okapi.filters.xini.jaxb.Field;
 import net.sf.okapi.filters.xini.jaxb.INITD;
 import net.sf.okapi.filters.xini.jaxb.INITR;
 import net.sf.okapi.filters.xini.jaxb.Page;
 import net.sf.okapi.filters.xini.jaxb.Seg;
 import net.sf.okapi.filters.xini.jaxb.TD;
 import net.sf.okapi.filters.xini.jaxb.TR;
 import net.sf.okapi.filters.xini.jaxb.TextContent;
 import net.sf.okapi.filters.xini.jaxb.Xini;
 import net.sf.okapi.filters.xini.jaxb.Element.ElementContent;
 
 public class XINIFilterTestHelper implements Serializable {
 
 	private static Pattern STARTING_TAG = Pattern.compile("<(\\w+ [^>]+?)(/?)>");
 	private static Pattern ATTRIBUTES = Pattern.compile("\\w+?=\".*?\"");
 	private XINIFilter filter;
 	private LocaleId locEN;
 	private LocaleId locDE;
 	private Marshaller m;
 
 	public XINIFilterTestHelper() {
 		filter =  new XINIFilter();
 		locEN = LocaleId.fromString("en");
 		locDE = LocaleId.fromString("de");
 		JAXBContext jc;
 		try {
 			jc = JAXBContext.newInstance(Xini.class.getPackage().getName());
 			m = jc.createMarshaller();
 		}
 		catch (JAXBException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void assertEquivalent(String expected, String actual) {
 		expected = normalize(expected);
 		actual = normalize(actual);
 		assertEquals(expected, actual);
 	}
 
 	private String normalize(String str) {
 		String result = str;
 		Matcher startTagMatcher = STARTING_TAG.matcher(str);
 
 		while (startTagMatcher.find()) {
 			String tagContent = startTagMatcher.group(1);
 			String suffix = startTagMatcher.group(2);
 			String newTagContent = orderAttributes(tagContent);
 			newTagContent += suffix;
 
 			String before = result.substring(0, startTagMatcher.start());
 			String after = result.substring(startTagMatcher.end(), result.length());
 			result = before + "<" + newTagContent + ">" + after;
 		}
 		return result;
 	}
 
 	private String orderAttributes(String tagContent) {
 		String tagContentWithOrderedAttributes = "";
 		Matcher attributeMatcher = ATTRIBUTES.matcher(tagContent);
 		List<String> attributeDeclarations = new ArrayList<String>();
 		boolean firstMatch = true;
 		while (attributeMatcher.find()) {
 			if (firstMatch) {
 				tagContentWithOrderedAttributes += tagContent.substring(0, attributeMatcher.start());
 				tagContentWithOrderedAttributes = tagContentWithOrderedAttributes.trim();
 				firstMatch = false;
 			}
 			String attDeclaration = attributeMatcher.group();
 			attributeDeclarations.add(attDeclaration);
 		}
 		Collections.sort(attributeDeclarations);
 		for (String attributeDeclaration : attributeDeclarations) {
 			tagContentWithOrderedAttributes += " " + attributeDeclaration;
 		}
 		return tagContentWithOrderedAttributes;
 	}
 
 	/**
 	 * Converts {@link TextContent} into an xml string.
 	 *
 	 * @param xiniTextContent
 	 * @return
 	 */
 	public String serializeTextContent(TextContent xiniTextContent) {
 		String serializedSeg = null;
 
 		try {
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			m.marshal(xiniTextContent, baos);
 			serializedSeg = baos.toString("UTF-8");
 			serializedSeg = removeXmlDeclaration(serializedSeg);
 		}
 		catch (JAXBException e) {
 			throw new RuntimeException(e);
 		}
 		catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		}
 
 		int beginOffset = serializedSeg.indexOf(">") + 1;
 		int endOffset = serializedSeg.lastIndexOf("<");
 
 		serializedSeg = serializedSeg.substring(beginOffset, endOffset);
 
 		return serializedSeg;
 	}
 
 	private String removeXmlDeclaration(String content) {
 		if (content.startsWith("<?xml"))
 			content = content.substring(content.indexOf(">") + 1);
 		return content;
 	}
 
 	public String getStartSnippet() {
 		return 	getStartSnippetXiniMain() +
 				"		<Page PageID=\"1\">" +
 				"			<Elements>" +
 				"				<Element ElementID=\"10\" Size=\"50\">" +
 				"					<ElementContent>";
 	}
 
 	public String getStartSnippetXiniMain() {
 		return 	"<?xml version=\"1.0\" ?>" +
 				"<Xini SchemaVersion=\"1.0\" xsi:noNamespaceSchemaLocation=\"http://www.ontram.com/xsd/xini.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
 				"	<Main>";
 	}
 
 	public String getEndSnippet() {
 		return 	"					</ElementContent>" +
 				"				</Element>" +
 				"			</Elements>" +
 				"		</Page>" +
 				"	</Main>" +
 				"</Xini>";
 	}
 
 	public List<Event> toEvents(String snippet) {
 		return toEvents(snippet, filter, locEN, locDE);
 	}
 
 	public Xini toXini(List<Event> events) {
 		return toXini(events, filter);
 	}
 
 	public Xini toXini(List<Event> events, IFilter filter) {
 		XINIWriter writer = (XINIWriter) filter.createFilterWriter();
 		try {
 			for (Event event : events) {
 				writer.handleEvent(event);
 			}
 		}
 		catch (OkapiBadFilterParametersException e) {
 			// Output path is not set when tests are run. That's ok here.
 		}
 		return writer.getXini();
 	}
 
 	private List<Event> toEvents(String snippet, IFilter filter, LocaleId inputLoc, LocaleId outputLoc) {
 		ArrayList<Event> list = new ArrayList<Event>();
 		filter.open(new RawDocument(snippet, inputLoc, outputLoc));
 		while (filter.hasNext()) {
 			Event event = filter.next();
 			list.add(event);
 		}
 		filter.close();
 		return list;
 	}
 
 	public List<INITR> getINITableRowsByPageIdAndElementId(Xini xini, int pageId, int elementId) {
 		return getElementContentByPageIdAndElementId(xini, pageId, elementId).getINITable().getTR();
 	}
 
 	public List<TR> getTableRowsByPageIdAndElementId(Xini xini, int pageId, int elementId) {
 		return getElementContentByPageIdAndElementId(xini, pageId, elementId).getTable().getTR();
 	}
 
 	public ElementContent getElementContentByPageIdAndElementId(Xini xini, int pageId, int elementId) {
 		for (Page eachPage : xini.getMain().getPage()) {
 			if (eachPage.getPageID() == pageId) {
 				for (Element eachElement : eachPage.getElements().getElement()) {
 					if (eachElement.getElementID() == elementId) {
 						return eachElement.getElementContent();
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	public String getSegContentBySegId(TD td, int segId) {
 		Seg seg = getSegBySegId(td, segId);
 		return contentOf(seg);
 	}
 
 	public Seg getSegBySegId(TD td, int segId) {
 		return td.getSeg().get(segId);
 	}
 
 	public String getSegContentBySegId(INITD td, int segId) {
 		Seg seg = getSegBySegId(td, segId);
 		return contentOf(seg);
 	}
 
 	public Seg getSegBySegId(INITD td, int segId) {
 		return td.getSeg().get(segId);
 	}
 
 	public String contentOf(TextContent tc) {
 		return serializeTextContent(tc);
 	}
 
 	public List<Field> getFieldsByPageIdAndElementId(Xini xini, int pageId, int elementId) {
 		for (Page eachPage : xini.getMain().getPage()) {
 			if (eachPage.getPageID() == pageId) {
 				for (Element eachElement : eachPage.getElements().getElement()) {
 					if (eachElement.getElementID() == elementId) {
 						return eachElement.getElementContent().getFields().getField();
 					}
 				}
 			}
 		}
 		return new ArrayList<Field>();
 	}
 
 	public String getSegContentBySegId(Field field, int segId) {
 		Seg seg = getSegBySegId(field, segId);
 		return contentOf(seg);
 	}
 
 	public Seg getSegBySegId(Field field, int segId) {
 		for (Seg each: field.getSeg()) {
 			if (each.getSegID() == segId)
 				return each;
 		}
 		return null;
 	}
 }
