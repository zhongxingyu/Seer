 package org.plasma.sdo.xml;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.Writer;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 
 import javanet.staxutils.IndentingXMLStreamWriter;
 
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 import javax.xml.transform.Result;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.plasma.sdo.PlasmaDataGraphEventVisitor;
 import org.plasma.sdo.PlasmaDataObject;
 import org.plasma.sdo.PlasmaNode;
 import org.plasma.sdo.PlasmaProperty;
 import org.plasma.sdo.PlasmaType;
 import org.plasma.sdo.helper.DataConverter;
 import org.plasma.sdo.helper.PlasmaTypeHelper;
 import org.plasma.sdo.helper.PlasmaXSDHelper;
 import org.plasma.sdo.profile.KeyType;
 import org.plasma.xml.schema.SchemaConstants;
 import org.plasma.xml.schema.SchemaUtil;
 
 import commonj.sdo.DataObject;
 import commonj.sdo.Property;
 import commonj.sdo.Type;
 import commonj.sdo.helper.XMLDocument;
 
 
 /**
  * A Streaming API for XML (StAX) based XML marshaler which converts/writes 
  * an SDO data graph to various supplied (stream-based) XML output sources. As the data graph
  * is traversed to generate output, containment and non containment reference 
  * (property) associations are detected and handled such that even though 
  * multiple references to the same data object are encountered, (where possible) no duplicate
  * XML data is written to the result. This not only reduces the size of the XML result, but
  * provides enough information related to containment and non containment references
  * such that the original data graph can be coalesced and re-constituted from the XML
  * back into a fully operational state.     
  * 
  * Of primary concern with regard to handling containment and non containment 
  * references is of course that the XML result be valid in relation to the associated 
  * XML Schema. XML Schema Instance (XSI) mechanisms are used to accommodate multiple XML
  * representations of the same (SDO) Type under both containment and non containment
  * scenarios.     
  *     
  */
 //FIXME: deal with change-summaries
 //FIXME: deal with metadata
 public class StreamMarshaller extends Marshaller {
 
     private static Log log = LogFactory.getFactory().getInstance(StreamMarshaller.class);
     private XMLOutputFactory factory;
     private String namespacePrefix = "tns";
     private PlasmaXSDHelper helper = PlasmaXSDHelper.INSTANCE;
     private PropertyComparator comparator = new PropertyComparator();		
 
     /**
      * Constructor. 
      * @param document the document containing the root data object
      * and other XML related values
      * @param options the XML marshaling options
      * @see XMLDocument
      */
     public StreamMarshaller(XMLDocument document) 
     {
     	super(MarshallerFlavor.STAX, document);    	
     	construct();
     }
     
     /**
      * Constructor. 
      * @param document the document containing the root data object
      * and other XML related values
      * @param options the XML marshaling options
      * @see XMLDocument
      * @see XMLOptions
      */
     public StreamMarshaller(XMLDocument document, 
     		XMLOptions options) 
     {
     	super(document, options);    	
     	construct();
     }
     
     private void construct() {
 		this.factory = XMLOutputFactory.newInstance();
 	    // Set namespace prefix defaulting for all created writers
 		//this.factory.setProperty("javax.xml.stream.isPrefixDefaulting",Boolean.TRUE);
     }
     
     private void setup() {
 		if (this.getOptions() != null && this.getOptions().getRootNamespacePrefix() != null
 				&& this.getOptions().getRootNamespacePrefix().length() > 0)
 			namespacePrefix = this.getOptions().getRootNamespacePrefix();
 		
     }
 
 	public void marshal(OutputStream stream) throws XMLStreamException, MarshallerException {
 		XMLStreamWriter writer =
 			factory.createXMLStreamWriter(stream);
 		
 		writer = new IndentingXMLStreamWriter(writer);
 		try {
 		    write(writer);
         }
 		finally {
 			writer.close();
 		}
 	}
 	
 	public void marshal(Writer outputWriter) throws XMLStreamException, MarshallerException {
 		XMLStreamWriter writer =
 			factory.createXMLStreamWriter(outputWriter);
 		
 		writer = new IndentingXMLStreamWriter(writer);
 		try {
 		    write(writer);
         }
 		finally {
 			writer.close();
 		}
 	}
 	
 	public void marshal(Result outputResult) throws XMLStreamException, MarshallerException {
 		XMLStreamWriter writer =
 			factory.createXMLStreamWriter(outputResult);
 		
 		writer = new IndentingXMLStreamWriter(writer);
 		try {
 		    write(writer);
         }
 		finally {
 			writer.close();
 		}
 	}	
 	
 	private void writeRootAttributes(XMLStreamWriter writer) throws XMLStreamException {
 		writer.writeAttribute("xmlns", 
 			this.document.getRootElementURI(), 
 			namespacePrefix, 
 			this.document.getRootElementURI());
 		writer.writeAttribute("xmlns", 
 			SchemaConstants.XMLSCHEMA_NAMESPACE_URI, 
 			"xs", SchemaConstants.XMLSCHEMA_NAMESPACE_URI);		
 		writer.writeAttribute("xmlns", 
 			XMLConstants.XMLSCHEMA_INSTANCE_NAMESPACE_URI,  
 			"xsi", XMLConstants.XMLSCHEMA_INSTANCE_NAMESPACE_URI);	
 		if (this.document.getSchemaLocation() != null)			
 		    writer.writeAttribute("xsi",
 			    XMLConstants.XMLSCHEMA_INSTANCE_NAMESPACE_URI,  
 			    "schemaLocation", 
 			    this.document.getSchemaLocation());	
 	}
 	
 	private void write(XMLStreamWriter writer) throws XMLStreamException {
     	setup();
 
     	if (this.document.isXMLDeclaration())
     		if (this.document.getEncoding() != null)
     	        writer.writeStartDocument(this.document.getEncoding(), 
     	    		this.document.getXMLVersion());
     		else
     			writer.writeStartDocument( 
         	    		this.document.getXMLVersion());
     	
 	    // Write a processing instruction
 		//writer.writeProcessingInstruction(
 	    //     "xml-stylesheet href='catalog.xsl' type='text/xsl'");	
 		
 		if (this.document.getRootElementName() != null) {
 			writer.writeDefaultNamespace(this.document.getRootElementURI());		    			
 			writer.writeStartElement(namespacePrefix, 
 				this.document.getRootElementName(), 
 				this.document.getRootElementURI());
 			writeRootAttributes(writer);
 		}
 		
 		EventVisitor visitor = new EventVisitor(writer);
 		((PlasmaDataObject)this.document.getRootObject()).accept(visitor);
 		
 		if (this.document.getRootElementName() != null) {
 			writer.writeEndElement();
 		}
 		
 		writer.writeEndDocument();
 	}
 
 	protected String fromObject(Type sourceType, Object value) {
 		return DataConverter.INSTANCE.toString(sourceType, value);
 	}	
 
 	class EventVisitor implements PlasmaDataGraphEventVisitor {
 		
 		private XMLStreamWriter writer;
 		
 		public EventVisitor(XMLStreamWriter writer) {
 			this.writer = writer;
 		}
 		
 		public void start(PlasmaDataObject target, PlasmaDataObject source, String sourcePropertyName, int level){
 			try {
 				PlasmaType sourceType = null;
 		        PlasmaProperty sourceProperty = null;
 		        if (source != null) {
 		        	sourceType = (PlasmaType)PlasmaTypeHelper.INSTANCE.getType(
 		        		document.getRootElementURI(), source.getType().getName());
 		        	sourceProperty = (PlasmaProperty)sourceType.getProperty(sourcePropertyName);
 		        }
 		        
 		        PlasmaType targetType = (PlasmaType)PlasmaTypeHelper.INSTANCE.getType(
 		        		document.getRootElementURI(), target.getType().getName()); 
 		        
 		        if (log.isDebugEnabled())
 		            if (source == null)
 		                log.debug("start: " + targetType.getName() 
 		                        + "("+((PlasmaNode)target).getUUIDAsString()+")");
 		            else
 		                log.debug("start: " + sourceType.getName() 
 		                        + "("+((PlasmaNode)source).getUUIDAsString()+ ")."
 		                        + sourceProperty.getName() + "->"
 		                        + targetType.getName() + "("+((PlasmaNode)target).getUUIDAsString()+")");
 		        if (source != null) {
         	        this.writer.writeStartElement(helper.getLocalName(sourceProperty));
 		        }
 		    	else { // its the root
 		    		if (document.getRootElementName() == null) {
 		    			// no passed in root element name, write namespace
 		    			// stuff into start tag of data-graph root
 		    			this.writer.writeStartElement(namespacePrefix, 
 		    					helper.getLocalName(targetType), 
 		    					document.getRootElementURI());
 		    			writeRootAttributes(writer);
 		    		}
 		        	else {
 			        	this.writer.writeStartElement(
 			        			helper.getLocalName(targetType));
 		        	}
 		    	}
 		        // root or containment reference
 	        	if (source == null || source.contains(target)) {
 	                writeContent(this.writer, target, source, 
 			        	    sourceProperty, targetType, sourceType, level);	
 	        	}
 	        	else {
 	        		writeNonContainmentReferenceContent(this.writer, target, source, 
 			        	    sourceProperty, targetType, sourceType, level);
 	        	}
 		        
 		    } catch (XMLStreamException e) {
 				throw new MarshallerRuntimeException(e);
 			} catch (IOException e) {
 				throw new MarshallerRuntimeException(e);
 			}
 		}
 		
 		public void end(PlasmaDataObject targetObject, PlasmaDataObject sourceObject, String sourcePropertyName, int level){
 			try {
 				this.writer.writeEndElement();
 			} catch (XMLStreamException e) {
 				throw new MarshallerRuntimeException(e);
 			}		
 		}			
 	}
 	
 	private void writeNonContainmentReferenceContent(XMLStreamWriter writer, DataObject dataObject, DataObject source, 
 			Property sourceProperty, 
 			PlasmaType targetType, PlasmaType sourceType,
 			int level) throws IOException, XMLStreamException {
 		
 		// create XSI type for all non-containment refs
 	    writer.writeAttribute("xsi", 
 	    	XMLConstants.XMLSCHEMA_INSTANCE_NAMESPACE_URI, "type", 
 	    	namespacePrefix + ":" 
 	    	+ SchemaUtil.getNonContainmentReferenceName(targetType));
 
 	    writer.writeAttribute(SchemaUtil.getSerializationAttributeName(), 
 	    		((PlasmaDataObject)dataObject).getUUIDAsString());
 	    /*
 	    int externKeyCount = 0;
 		for (Property property : targetType.getProperties()) {
 			PlasmaProperty prop = (PlasmaProperty)property;
 			if (!prop.getType().isDataType() || !prop.isKey(KeyType.external) || !prop.isXMLAttribute()) {
 			    continue; // not an external key attribute
 			}
 			Object value = dataObject.get(prop);
 			if (value == null)
 				continue;
 		    writer.writeAttribute(helper.getLocalName(prop),
 		    		fromObject(prop.getType(), value));
 		    externKeyCount++;
 		}
 		for (Property property : targetType.getProperties()) {
 			PlasmaProperty prop = (PlasmaProperty)property;
 			if (!prop.getType().isDataType() || !prop.isKey(KeyType.external) || prop.isXMLAttribute()) {
 			    continue; // not an external key element
 			}
 			Object value = dataObject.get(prop);
 			if (value == null)
 				continue;
         	writer.writeStartElement(helper.getLocalName(prop));
         	writer.writeCharacters(this.fromObject(prop.getType(), value));
         	writer.writeEndElement();
         	externKeyCount++;
 		}
 		*/
 	}
 
 	private void writeContent(XMLStreamWriter writer, DataObject dataObject, DataObject source, 
 			Property sourceProperty, 
 			PlasmaType targetType, PlasmaType sourceType,
 			int level) throws IOException, XMLStreamException {
 		
 		// create XSI type for all containment refs
 	    writer.writeAttribute("xsi", 
 	    	XMLConstants.XMLSCHEMA_INSTANCE_NAMESPACE_URI, "type", 
 	    	namespacePrefix + ":" 
 	    	+ SchemaUtil.getContainmentReferenceName(targetType));
 	    
 	    writer.writeAttribute(SchemaUtil.getSerializationAttributeName(), 
 	    		((PlasmaDataObject)dataObject).getUUIDAsString());
 
 	    int externKeyCount = 0;
 		for (Property property : targetType.getProperties()) {
 			PlasmaProperty prop = (PlasmaProperty)property;
 			if (prop.isKey(KeyType.external)) {
 				externKeyCount++;
 			}
 			if (!prop.getType().isDataType() || !prop.isXMLAttribute()) {
 			    continue;
 			}
 			// FIXME - what about pri-keys which are not sequences
 			//VisibilityKind visibility = (VisibilityKind)prop.get(PlasmaProperty.INSTANCE_PROPERTY_OBJECT_VISIBILITY);
 			//if (visibility != null && visibility.ordinal() == VisibilityKind.private_.ordinal())
 			//	continue; // for properties defined as private no XML or XML Schema property generated
 			Object value = dataObject.get(prop);
 			if (value == null)
 				continue;
 		    writer.writeAttribute(helper.getLocalName(prop),
 		    		fromObject(prop.getType(), value));
 		}
 		
 		// create XSI type on demand for containment refs
 		if (externKeyCount > 0)
 	        writer.writeAttribute("xsi", 
 	    	    XMLConstants.XMLSCHEMA_INSTANCE_NAMESPACE_URI, "type", 
 	    	    namespacePrefix + ":" 
 		    	+ SchemaUtil.getContainmentReferenceName(targetType));
 				
 		// add element properties
		List<Property> list = targetType.getDeclaredProperties();
     	PlasmaProperty[] properties = new PlasmaProperty[list.size()];
     	list.toArray(properties);
     	Arrays.sort(properties, this.comparator);		
 		for (Property property : properties) {
 			PlasmaProperty prop = (PlasmaProperty)property;
 			if (!prop.getType().isDataType() || prop.isXMLAttribute())
 				continue;
 			// FIXME - what about pri-keys which are not sequences
 			//VisibilityKind visibility = (VisibilityKind)prop.get(PlasmaProperty.INSTANCE_PROPERTY_OBJECT_VISIBILITY);
 			//if (visibility != null && visibility.ordinal() == VisibilityKind.private_.ordinal())
 			//	continue; // for properties defined as private no XML or XML Schema property generated
 			Object value = dataObject.get(prop);
 			if (value == null)
 				continue;
         	writer.writeStartElement(helper.getLocalName(prop));
         	writer.writeCharacters(this.fromObject(prop.getType(), value));
         	writer.writeEndElement();
 		}
 	}
 	
 	class PropertyComparator implements Comparator<PlasmaProperty> {
 		public int compare(PlasmaProperty p1, PlasmaProperty p2) {
 			if (p1.getSort() != null && p2.getSort() != null)
 				return p1.getSort().getKey().compareTo(p2.getSort().getKey());
 			else
 			    return p1.getName().compareTo(p2.getName());
 		}
 	}
 
 }
