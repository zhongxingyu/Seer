 package org.cichonski.ontviewer.parse;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.cichonski.ontviewer.model.OwlClass;
 import org.cichonski.ontviewer.model.OwlClassBuilder;
 import org.cichonski.ontviewer.model.Property;
 import org.cichonski.ontviewer.model.PropertyBuilder;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * Simple handler that pulls out only a subset of the RDF/XML encoded OWL ontology.
  * @author Paul Cichonski
  *
  */
 public class OwlSaxHandler extends DefaultHandler {
     private static Logger log = Logger.getLogger(OwlSaxHandler.class.getName());
     private static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
     private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
     private static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
     
     // all classes for easy access
     private final Map<URI, OwlClass> classCache = new HashMap<URI, OwlClass>();
     
     // preserves the tree structure of the classes
     private final Set<OwlClass> classTree = new TreeSet<OwlClass>(); 
     
     
     // ***************************
     // stream state 
     // ***************************
     
     private final Map<URI, OwlClassBuilder> classBuilders = new HashMap<URI, OwlClassBuilder>();    
     private final Map<URI, URI> subClassMap = new HashMap<URI, URI>(); // key=subclass URI, value = superclass URI.
     private final Set<Property> objectProperties = new HashSet<Property>();
     private final Set<Property> dataTypeProperties = new HashSet<Property>();
     
     
 
     private String xmlBase;
     
     private OwlClassBuilder currentClassBuilder = null;
     
     private PropertyBuilder currentPropertyBuilder = null;
     
     private StringBuilder charBuffer = new StringBuilder();
 
     private boolean root = true; //assume root until first element is past
     private Stack<Integer> currentClasses = new Stack<Integer>(); //place holder for nested classes
     private Stack<Integer> unknonwElements = new Stack<Integer>(); //keep track of unknown elements we are traversing.
     private boolean property = false;
     
     public OwlSaxHandler() {
     }
 
     
     @Override
     public void startElement(
             String uri,
             String localName,
             String qName,
             Attributes attributes) throws SAXException {
         if (charBuffer.length() > 0){
             charBuffer.setLength(0); 
         }
         if (root){
             root = false;
             xmlBase = attributes.getValue("xml:base");
         } else if (isOwlClass(uri, localName, qName)){
             currentClasses.push(1);
             if (currentClasses.size() == 1){
                 startOwlClass(attributes);
             } else {
             	//assume subclasses only nest 1 deep
             	parseSubClass(attributes);
             }
         } else if (isUnknownElementInOwlClass(uri, localName, qName)){ 
 				// in an element somewhere (n-depth) under owl:Class that we don't know about...but it could have labels and comments...need to ignore them
 				unknonwElements.push(1);
         } else if (isObjectProperty(uri, localName, qName) || isDataTypeProperty(uri, localName, qName)) {
         	property = true;
         	startProperty(attributes);
         } else if (isPropertyDomain(uri, localName, qName)){
         	parseDomain(attributes);
         } else if (isPropertyRange(uri, localName, qName)){
         	parseRange(attributes);
         }
     }
     
     @Override
     public void characters(char[] ch, int start, int length)
             throws SAXException {
     	charBuffer.append(ch, start, length);
     }
     
     @Override
     public void endElement(String uri, String localName, String qName)
             throws SAXException {
         if (isOwlClass(uri, localName, qName)){
             currentClasses.pop();
             if (currentClasses.isEmpty()){
                 currentClassBuilder = null;  
             }
         } else if (isUnknownElementInOwlClass(uri, localName, qName)){
         	unknonwElements.pop();
     	} else if (isTopLevelOwlClassLabel(uri, localName, qName)){
             currentClassBuilder.setLabel(charBuffer.toString());
         } else if (isTopLevelOwlClassComment(uri, localName, qName)){
             currentClassBuilder.setDescription(charBuffer.toString());
         } else if (isObjectProperty(uri, localName, qName)) {
         	property = false;
         	objectProperties.add(currentPropertyBuilder.build());
         	currentPropertyBuilder = null;
         } else if (isDataTypeProperty(uri, localName, qName)){
         	property = false;
         	dataTypeProperties.add(currentPropertyBuilder.build());
         	currentPropertyBuilder = null;
     	} else if (isPropertyLabel(uri, localName, qName)){
         	currentPropertyBuilder.setLabel(charBuffer.toString());
         } else if (isPropertyComment(uri, localName, qName)){
         	currentPropertyBuilder.setDescription(charBuffer.toString());
         } 
     }
     
     
     
     @Override
     public void endDocument() throws SAXException {
         /* TODO:
          * 1. subClass mappings are in the subClasses Map, need to assemble into the build set
          * 2. build all builders and add to both tree and cache
          */
 
     	
     	for (Property p : objectProperties){
     		populateProperties(p, PropertyType.OBJECT);
     	}
     	for (Property p : dataTypeProperties){
     		populateProperties(p, PropertyType.DATA);
     	}
         
         //just for testing....need to redo
         for (OwlClassBuilder builder : classBuilders.values()){
             OwlClass owlClass = builder.build();
             classCache.put(owlClass.getURI(), owlClass);
             
         }
     }
     
 //***********************************
 // Methods for building out OwlClass and Predicates
 //***********************************
     private void startOwlClass(Attributes attributes){
         URI uri = resolveFullUriIdentifier(attributes);
         OwlClassBuilder builder = new OwlClassBuilder();
         builder.setUri(uri);
         currentClassBuilder = builder;
         classBuilders.put(uri, builder);
     }
     
     private void parseSubClass(Attributes attributes){
     	// **** !!!! Important: This assumes that a class is only a subClass of one other class !!! ****
     	// **** !!!! While not valid according to the spec, we shouldn't be seeing any ontologies that break this rule !!!! ****
     	URI uri = resolveFullUriIdentifier(attributes);
 		if (subClassMap.containsKey(uri)) {
 			throw new RuntimeException(
 					currentClassBuilder.getUri().toString()
 							+ " seems to declare two subclassOf relationships. This is not currently supported");
 		}
     	subClassMap.put(currentClassBuilder.getUri(), uri); // the class being built is the actual subclass
     }
     
     private void populateProperties(Property p, PropertyType type){
 		Set<URI> domains = p.getDomains();
 		for (URI domain : domains){
 			OwlClassBuilder classBuilder = classBuilders.get(domain);
 			if (classBuilder != null){
 				if (type.equals(PropertyType.OBJECT)) {
 					classBuilder.addObjectProperty(p);
 				} else if (type.equals(PropertyType.DATA)){
 					classBuilder.addDataTypeProperty(p);
 				}
 			}
 		}
     }
     
 
     
     private void startProperty(Attributes attributes){
     	URI uri = resolveFullUriIdentifier(attributes);
     	PropertyBuilder builder = new PropertyBuilder();
     	builder.setUri(uri);
     	currentPropertyBuilder = builder;
     }
     
     private void parseDomain(Attributes attributes){
     	URI uri = resolveFullUriIdentifier(attributes);
     	currentPropertyBuilder.addDomain(uri);
     }
     
     private void parseRange(Attributes attributes){
     	URI uri = resolveFullUriIdentifier(attributes);
     	currentPropertyBuilder.addRange(uri);
     }
     
 
     
 
     
     /**
      * Logic modeled off of spec (http://www.w3.org/TR/REC-rdf-syntax/#section-Syntax-ID-xml-base).
      * @param attributes - the attributes that contain either the rdf:ID or rdf:about signifying the identifier of the resource.
      * @return
      */
     //right now this is assuming resource identifiers are in the xml:base..
     private URI resolveFullUriIdentifier(Attributes attributes){
         if (xmlBase == null || xmlBase.isEmpty()){
            throw new RuntimeException("no xml:base specified");
         }
         String resourceName = attributes.getValue(RDF_NS, "ID");  // rdf:ID gives a relative URI index without the #
         try {
             if (resourceName != null && !resourceName.isEmpty()){ 
                 String fullUri = xmlBase + "#" + resourceName;
                 return new URI(fullUri);
             } 
             //assume rdf:about, relative URI with prepended #
             resourceName = attributes.getValue(RDF_NS, "about");
             if (resourceName != null && !resourceName.isEmpty()){ 
                 String fullUri = xmlBase + resourceName;
                 return new URI(fullUri);
             } 
             // assume rdf:resource, relative URI with prepended #
             resourceName = attributes.getValue(RDF_NS, "resource");
             if (resourceName != null && !resourceName.isEmpty()){ 
             	if (resourceName.startsWith("#")){
                     String fullUri = xmlBase + resourceName;
                     return new URI(fullUri);
             	} else {
             		//assume full URI alread (i.e., xsd datatype)
             		return new URI(resourceName);
             	}
             } 
         } catch (URISyntaxException e){
             log.log(Level.WARNING, "class: " + resourceName, e);
         }
         throw new RuntimeException("could not build URI"); // if it didn't work, everything else is dead
     }
     
 
 //***********************************
 // centralize element inspection logic
 //***********************************
     
     private boolean isOwlClass(String uri, String localName, String qName){
         return OWL_NS.equals(uri) && "class".equals(localName.toLowerCase());
     }
     
     private boolean isUnknownElementInOwlClass(String uri, String localName, String qName){
     	// in an element somewhere (n-depth) under owl:Class that we don't know about..
     	return currentClasses.size() == 1 && !isTopLevelOwlClassLabel(uri, localName, qName) && !isTopLevelOwlClassComment(uri, localName, qName);
     }
 
     private boolean isTopLevelOwlClassLabel(String uri, String localName, String qName){
         return unknonwElements.isEmpty() && !currentClasses.isEmpty() && isRdfsLabel(uri, localName, qName); // assume subClass statements don't have comments
     }
     
     private boolean isTopLevelOwlClassComment(String uri, String localName, String qName){
         return unknonwElements.isEmpty() && !currentClasses.isEmpty() && isRdfsComment(uri, localName, qName); // assume subClass statements don't have comments
     }
     
     private boolean isObjectProperty(String uri, String localName, String qName){
     	return OWL_NS.equals(uri) && "objectproperty".equals(localName.toLowerCase());
     }
     
     private boolean isDataTypeProperty(String uri, String localName, String qName){
     	return OWL_NS.equals(uri) && "datatypeproperty".equals(localName.toLowerCase());
     }
     
     private boolean isPropertyComment(String uri, String localName, String qName){
     	return property && isRdfsComment(uri, localName, qName);
     }
     
     private boolean isPropertyLabel(String uri, String localName, String qName){
     	return property && isRdfsLabel(uri, localName, qName);
     }
     
     private boolean isPropertyDomain(String uri, String localName, String qName){
     	return property && RDFS_NS.equals(uri) && "domain".equals(localName.toLowerCase());
     }
     
     private boolean isPropertyRange(String uri, String localName, String qName){
     	return property && RDFS_NS.equals(uri) && "range".equals(localName.toLowerCase());
     }
     
     private boolean isRdfsComment(String uri, String localName, String qName){
         return RDFS_NS.equals(uri) && "comment".equals(localName.toLowerCase());
     }
     
     private boolean isRdfsLabel(String uri, String localName, String qName){
     	return RDFS_NS.equals(uri) && "label".equals(localName.toLowerCase());
     }
     
     private static enum PropertyType {
     	OBJECT, DATA;
     }
     
     
     public Map<URI, OwlClass> getClassCache() {
         return Collections.unmodifiableMap(classCache);
     }
     
     public Set<OwlClass> getClassTree() {
     	 return Collections.unmodifiableSet(classTree);
 	}
 
 
 }
