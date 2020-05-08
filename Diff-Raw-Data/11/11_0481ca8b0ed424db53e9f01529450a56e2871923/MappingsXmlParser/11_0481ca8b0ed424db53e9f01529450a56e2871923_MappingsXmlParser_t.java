 package ee.stacc.transformer.client.mapping;
 
 import java.util.*;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.xml.client.Document;
 import com.google.gwt.xml.client.Element;
 import com.google.gwt.xml.client.Node;
 import com.google.gwt.xml.client.NodeList;
 
 import ee.stacc.transformer.client.data.DataFrame;
 import ee.stacc.transformer.client.data.InstanceFactory;
 
 /**
  * @author Rainer Villido
  *
  */
 public class MappingsXmlParser {
 	
 	//Element names in the mappings XML document 
 	public static final String FRAME = "frame";
 	public static final String FRAMES = "frames";
 	public static final String TOPIC = "topic";
 	public static final String FORMAT = "format";
 	public static final String SCHEMA = "schema";
 	public static final String MAPPINGS = "mappings";
 	public static final String MAPPING = "mapping";
 	public static final String GLOBAL_REF = "global_ref";
 	public static final String PATH = "path";
 	public static final String REPEATING_ELEMENT_GROUP = "repeating_element_group";	
 	public static final String OUTGOING_ONLY = "outgoing_only";
 	public static final String TRUE = "true";
 	public static final String CONSTANT = "constant";
 	public static final String VALUE = "value";
 	public static final String DEFAULT = "default";
 	
 	/**
 	 * Method for loading content from the mappings configuration file to the mappings data structure.
 	 * 
 	 * @param xml	xml document containing mappings.
 	 * @return	map of data frames. (Key is topic).
 	 */
 	public Map<String, DataFrame> loadDataFrames(Document xml) {
 		GWT.log("Starting to parse XML", null);
 		
 		Map<String, DataFrame> dataFrames = new HashMap<String, DataFrame>();
 		NodeList frames = xml.getElementsByTagName(FRAME);
 		
 		//Go through all the frame elements in mappings.
 		for(int i = 0; i < frames.getLength();i++) {
 			
 			if(frames.item(i).getNodeType() == Node.TEXT_NODE)
 				continue;	//If it's just spam then don't process the node
 			
 			Element frame = (Element)frames.item(i);
 			
 			//Load all the data from the frame element
 			
 			//Get the data type of the messages specified in the mapping configuration.  
 			String dataType = frame.getElementsByTagName(FORMAT).item(0).getFirstChild().getNodeValue();
 			
 			//Instantiate the data frame according to the data type.
 			DataFrame dataFrame = InstanceFactory.getDataFrame(dataType);
 			
 			//Get the topic
 			Element topicNode = (Element)frame.getElementsByTagName(TOPIC).item(0);
 			String topic = topicNode.getFirstChild().getNodeValue();
 			dataFrame.setTopic(topic);
 			
 			if(topicNode.getAttribute(OUTGOING_ONLY) != null && topicNode.getAttribute(OUTGOING_ONLY).equalsIgnoreCase(TRUE)) {
 				dataFrame.setOutputOnly(true);
 			}
 			
 			//Schema element is optional. Load the schema
 			if(frame.getElementsByTagName(SCHEMA).getLength() > 0) {
 				String schemaUrl = frame.getElementsByTagName(SCHEMA).item(0).getFirstChild().getNodeValue();
 				dataFrame.setSchemaURL(schemaUrl);
 			}
 			
 			//Load the mapping elements
 			NodeList nodes = frame.getElementsByTagName(MAPPINGS).item(0).getChildNodes();
 			loadMappingsToDataFrame(nodes, dataFrame);
 			
 			//dataFrame.setMappings(mappings);
 			dataFrame.updateMappingsSet();
 			dataFrames.put(dataFrame.getTopic(), dataFrame);
 		}
 		
 		return dataFrames;
 	}
 	
 	/**
 	 * To process a list of mapping nodes and load all the data in the nodes.
 	 * @param nodes	list of mapping nodes.
 	 * @param dataFrame	data frame that the nodes belong to.
 	 */
 	private void loadMappingsToDataFrame(NodeList nodes, DataFrame dataFrame) {
 		Map<String, Mapping> mappings = dataFrame.getMappings();
 		
 		for(int i = 0;i < nodes.getLength();i++) {
 			
 			if(nodes.item(i).getNodeType() != Node.ELEMENT_NODE)
 				continue;	//If it's just spam then don't process the node
 			
 			Element node = (Element)nodes.item(i);
 			
 			if(node.getNodeName().equalsIgnoreCase(MAPPING)) {
 				//If the node is a mapping element
 				MappingElement mappingElement = retrieveMappingElementFromNode(node, dataFrame);
         for (String globalRef : mappingElement.getGlobalReference()) {
           mappings.put(globalRef, mappingElement);
         }
 			}
 			else if(node.getNodeName().equalsIgnoreCase(REPEATING_ELEMENT_GROUP)) {
 				//If the node is a repeating element group
 				loadRepeatingElementGroupToDataFrame(node, dataFrame);
 			}
 			else if (node.getNodeName().equalsIgnoreCase(CONSTANT)) {
 				//If the node is a constant.
 				processConstant(node, dataFrame);
 			}
 		}
 		
 	}
 	
 	private void loadRepeatingElementGroupToDataFrame(Element repeatingGroupNode, DataFrame dataFrame) {
 		loadRepeatingElementGroupToDataFrame(repeatingGroupNode, null, dataFrame);
 	}
 	
 	/**
 	 * To load a constant value.
 	 * @param node	node of the constant value.
 	 * @param dataFrame	the data frame that the constant belongs to.
 	 */
 	private void processConstant(Element node, DataFrame dataFrame) {
 		String path = node.getAttribute(PATH);
 		String value = node.getAttribute(VALUE);
 		
 		dataFrame.getConstantValues().put(path, value);
 	}
 
 	/**
 	 * To load mappings from a repeating element group.
 	 * 
 	 * @param repeatingGroupNode	repeating element group node.
 	 * @param parentMappingsGroup	mappings of the parent repeating mapping group.
 	 * @param dataFrame	data frame the mappings belong to.
 	 */
 	private void loadRepeatingElementGroupToDataFrame(Element repeatingGroupNode, RepeatingMappingsGroup parentMappingsGroup, DataFrame dataFrame) {
 		
    String path = null;
    if (repeatingGroupNode.hasAttribute(PATH)) {
      path = repeatingGroupNode.getAttribute(PATH);
    }
 
 		RepeatingMappingsGroup mappingsGroup = new RepeatingMappingsGroup(path, dataFrame, parentMappingsGroup);
 		
 		//Go through every child node in the repeating element group.
 		NodeList nodes = repeatingGroupNode.getChildNodes();
 		for(int i = 0; i < nodes.getLength(); i++) {
 			
 			if(nodes.item(i).getNodeType() != Node.ELEMENT_NODE)
 				continue;	//If it's just spam then don't process the node
 			
 			Element node = (Element)nodes.item(i);
 			
 			if(node.getNodeName().equalsIgnoreCase(MAPPING)) {
 				//If the node is a regular mapping node
 				MappingElement mappingElement = retrieveMappingElementFromNode(node, dataFrame);
 				String globalRef = mappingElement.getFirstGlobalReference();
 				
 				//Put the mapping element to the element group's mappings map
 				mappingsGroup.propagateMapping(mappingElement, globalRef);
 			}
 			else if(node.getNodeName().equalsIgnoreCase(REPEATING_ELEMENT_GROUP)) {
 				//If the node is also (recursively) a repeating element group.
 				loadRepeatingElementGroupToDataFrame(node, mappingsGroup, dataFrame);
 			}
 			else if (node.getNodeName().equalsIgnoreCase(CONSTANT)) {
 				//if the node is a constant.
 				processConstant(node, dataFrame);
 			}
 		}
 	}
 
 	/**
 	 * To load a regular mapping element from a node.
 	 * @param node	node representing the mapping element.
 	 * @param dataFrame	the data frame the mapping belongs to.
 	 * @return	the mapping element in the node.
 	 */
 	private MappingElement retrieveMappingElementFromNode(Element node, DataFrame dataFrame) {
 		
 		//Get the global reference
 		String globalRef;
 		if(node.getElementsByTagName(GLOBAL_REF).getLength() > 0)
 			globalRef =  node.getElementsByTagName(GLOBAL_REF).item(0).getFirstChild().getNodeValue();
 		else
 			globalRef =  node.getAttribute(GLOBAL_REF);
 		
 		//Get the path. Path is optional if the data doesn't have any structure.
 		String path =  "";
 		if(node.getElementsByTagName(PATH).getLength() > 0) {
 			path =  node.getElementsByTagName(PATH).item(0).getFirstChild().getNodeValue();
 		}
 		else if(node.getAttribute(PATH) != null)
 			path = node.getAttribute(PATH);
 		
 		MappingElement mapping = new MappingElement(path, getGlobalReferences(globalRef));
 		
 		//Load a default value if set.
 		if(node.getElementsByTagName(DEFAULT).getLength() > 0) {
 			String defaultValue = node.getElementsByTagName(DEFAULT).item(0).getFirstChild().getNodeValue();
 			processDefaultValue(defaultValue, mapping, dataFrame);
 		}
 		else if(node.getAttribute(DEFAULT) != null) {
 			String defaultValue = node.getAttribute(DEFAULT);
 			processDefaultValue(defaultValue, mapping, dataFrame);
 		}
 		
 		return mapping;
 	}
 
   /**
    *
    * @param globalRef string of space-separated global references
    * @return list of global references
    */
   private List<String> getGlobalReferences(String globalRef) {
     if (globalRef == null) {
       return new ArrayList<String>();
     }
     // split string by space
     return Arrays.asList(globalRef.split("\\s+"));
   }
 
 
   private void processDefaultValue(String defaultValue, MappingElement mapping, DataFrame dataFrame) {
 		mapping.setHasDefaultValue(true);
 		dataFrame.getConstantValues().put(mapping.getPath(), defaultValue);
 	}
 }
