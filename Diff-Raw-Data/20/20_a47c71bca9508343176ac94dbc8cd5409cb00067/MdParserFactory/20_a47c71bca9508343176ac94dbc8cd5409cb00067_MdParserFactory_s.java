 package runwayMdParsingClasses;
 
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 import com.runwaysdk.eclipse.plugin.schema.importer.XMLTags;
 import com.runwaysdk.eclipse.plugin.runway.MetaData;
 
 public class MdParserFactory {
 
 
 	public MdParserFactory(){}
 
 
 	/**
 	 * The polymorphic parser 
 	 * @param node
 	 * @return The instantiated and parsed EMF object for that node
 	 */
 	public MetaData getContentFromNode(Node node){
 		String nodeName = node.getNodeName();
 		NamedNodeMap nodeMap = node.getAttributes();
 
 		System.out.println("Inside MdParserFactory");
 
 		MdMetaDataParser contentParser = null; 
 
 		//Try to keep these in Alphabetical Order
 		if(nodeName == XMLTags.MD_BUSINESS_TAG){
 			System.out.println("Reading an MD_BUSINESS_TAG");
 			contentParser = new MdBusinessParser(nodeMap);
 			System.out.println("Done reading an MD_BUSINESS_TAG");
 		}
 
 		else if (nodeName == XMLTags.BLOB_TAG) {
 			contentParser = new MdAttributeBlobParser(nodeMap); 
 		}
 
 		else if (nodeName == XMLTags.BOOLEAN_TAG) {
 			contentParser = new MdAttributeBooleanParser(nodeMap); 
 		}
 
 		else if (nodeName == XMLTags.CHARACTER_TAG) {
 			contentParser = new MdAttributeCharacterParser(nodeMap); 
 		}
 
 		else if (nodeName == XMLTags.DATE_TAG) {
 			contentParser = new MdAttributeDateParser(nodeMap);
 		}
 
 		else if (nodeName == XMLTags.DATETIME_TAG) {
 			contentParser = new MDAttributeDateTimeParser(nodeMap);
 		}
 
 		else if (nodeName == XMLTags.DECIMAL_ATTRIBUTE) {
 			contentParser = new MdAttributeDecimalParser(nodeMap);
 		}
 
 		else if (nodeName == XMLTags.DOUBLE_TAG) {
 			contentParser = new MdAttributeDoubleParser(nodeMap);
 		}
 
 		else if (nodeName == XMLTags.ENUMERATION_TAG) {
 			contentParser = new MdAttributeEnumerationParser(nodeMap);
 		}
 
 		else if (nodeName == XMLTags.FLOAT_TAG) {
 			contentParser = new MdAttributeFloatParser(nodeMap);
 		}
 
 		//TODO is this the right "hash" tag? 
 		else if (nodeName == XMLTags.HASH_METHOD_ATTRIBUTE) {
 			contentParser = new MdAttributeHashParser(nodeMap);
 		}
 
 		else if (nodeName == XMLTags.INTEGER_TAG) {
 			contentParser = new MdAttributeIntegerParser(nodeMap);
 		}
 
 		else if (nodeName == XMLTags.LONG_TAG) {
 			contentParser = new MdAttributeLongParser(nodeMap);
 		}
 
 		else if (nodeName == XMLTags.TEXT_TAG) {
 			contentParser = new MdAttributeTextParser(nodeMap);
 		}
 		if(contentParser != null){
 			return contentParser.parse();
 		}
 		else{
			System.out.println("No contentParser for: " + nodeName);
			throw new NullPointerException("Find me in Parser Factory");
 		}
 	}
 }
 
