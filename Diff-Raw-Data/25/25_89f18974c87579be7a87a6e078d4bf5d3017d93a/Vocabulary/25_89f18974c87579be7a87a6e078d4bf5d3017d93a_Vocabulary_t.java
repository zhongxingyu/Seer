 package com.hp.hpl.deli;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.hp.hpl.jena.datatypes.DatatypeFormatException;
 import com.hp.hpl.jena.datatypes.TypeMapper;
 import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.NodeIterator;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.ResIterator;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.ResourceFactory;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 /**
  * This class processes the CC/PP vocabulary definition file.
  */
 class Vocabulary extends Utils implements Serializable {
 	/** Logging. */
 	private static Log log = LogFactory.getLog(ProcessHttpRequest.class);
 
 	/** Comment for <code>serialVersionUID</code> */
 	private static final long serialVersionUID = 1L;
 
 	/** A mapping of aliased schema namespaces on to real schema namespaces. */
 	protected Map<String, String> namespaceLookup = new HashMap<String, String>();
 
 	/** A mapping of UAProf properties onto a vector describing each property. */
 	private Map<Resource, Map<String, Resource>> propertyDescription = new HashMap<Resource, Map<String, Resource>>();
 
 	/** Datatyping information. */
 	 protected Map<List<String>, String> datatypesLookup = new HashMap<List<String>, String>();
 
 	/**
 	 * The constructor reads in a vocabulary definition file and then processess
 	 * the set of vocabularies it references.
 	 * 
 	 * @param configFile The filename of the configuration file.
 	 */
 	protected Vocabulary(String configFile) {
 		Model configData = ModelFactory.createDefaultModel();
 
 		try {
			configData.read(Workspace.getInstance().getResource(configFile), "", "N3");
 		} catch (Exception e) {
 			log.error("DELI Failed to load namespace/vocabulary configuration file from: " + configFile, e);
 		}
 		ResIterator definitions = configData.listSubjectsWithProperty(RDF.type, DeliSchema.NamespaceDefinition);
 		if (!definitions.hasNext()) {
 			log.info("Vocabulary: No schemas defined.");
 		} else {
 			// process namespace definitions
 			while (definitions.hasNext()) {
 				processNamespaceDefinition(definitions.nextResource());
 			}
 		}
 	}
 
 	/**
 	 * Process a namespace definition.
 	 * 
 	 * @param defn The resource for the namespace definition.
 	 */
 	private void processNamespaceDefinition(Resource defn) {
 		if (defn.hasProperty(DeliSchema.uri)) {
 			List<String> datatypesDef = null;
 			// add namespace
 			String URI = getPropertyUri(defn, DeliSchema.uri);
 			namespaceLookup.put(URI, URI);
 			// add alias namespaces
 			if (defn.hasProperty(DeliSchema.aliasUri)) {
 				StmtIterator stmts = defn.listProperties(DeliSchema.aliasUri);
 				while (stmts.hasNext()) {
 					String aliasUri = ((Resource) stmts.nextStatement().getObject()).getURI();
 					namespaceLookup.put(aliasUri, URI);
 				}
 			}
 			// process datatype definitions for UAProf 2.0 schemas
 			if (defn.hasProperty(DeliSchema.datatypeUri)) {
 				String datatypeUri = getPropertyUri(defn, DeliSchema.datatypeUri);
 				if (defn.hasProperty(DeliSchema.datatypeFile)) {
 					datatypesDef = loadDatatypeDef(defn, datatypeUri);
 					datatypesLookup.put(datatypesDef, URI);
 
 					log.debug("VocabularyConfig: Defined types are: ");
 					for (String it : datatypesDef) {
 						log.debug(" - " + it);
 					}
 				}
 			}
 			// load and process the schema
 			if (defn.hasProperty(DeliSchema.schemaVocabularyFile)) {
 
 				String file = getPropertyString(defn, DeliSchema.schemaVocabularyFile);
 				try {
 					log.debug("Vocabulary: Processing UAProf schema vocabulary file: " + file);
 					new SchemaProcess(Workspace.getInstance().getResource(file), URI, URI, datatypesDef);
 				} catch (Exception e) {
 					log.error("Vocabulary: Cannot load and process vocabulary schema from " + file, e);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Get a datatype definition
 	 * 
 	 * @param defn the resource corresponding to the namespace definition.
 	 * @param datatypeUri the datatype URI.
 	 * @return the datatype definition
 	 */
 	private List<String> loadDatatypeDef(Resource defn, String datatypeUri) {
 		String datatypeFile = getPropertyString(defn, DeliSchema.datatypeFile);
 		List<String> result = null;
 		// if it is a UAProf 2.0 schema, load in datatype
 		// definitions
 		try {
 			TypeMapper tm = TypeMapper.getInstance();
 			FileReader fr = new FileReader(Workspace.getPath() + datatypeFile);
 			result = XSDDatatype.loadUserDefined(datatypeUri, fr, null, tm);
 		} catch (DatatypeFormatException e) {
 			log.error("DELI could not process datatype configuration file: " + datatypeFile, e);
 		} catch (FileNotFoundException e) {
 			log.error("DELI Failed to load datatype configuration file: " + datatypeFile, e);
 		}
 		return result;
 	}
 
 	/**
 	 * This method returns a given value for an attribute property where the
 	 * attribute is specified by the attribute qname and its component qname.
 	 * 
 	 * @param attributeQName The QName of the attribute.
 	 * @param paramName The required Parameter
 	 */
 	protected Resource getAttributeProperty(Resource attributeQName, String paramName) {
 		Resource alias = getRealQName(attributeQName);
 		if (propertyDescription.containsKey(alias)) {
 			return getRealQName(propertyDescription.get(alias).get(paramName));
 		}
 
 		return null;
 	}
 
 	/**
 	 * Does this namespace use RDF datatyping?
 	 * 
 	 * @param prfUri schema namespace
 	 * @return does this schema use RDF datatyping?
 	 */
 	public boolean usesRDFDatatyping(String prfUri) {
 		return datatypesLookup.containsValue(prfUri);
 	}
 
 	/*
 	 * This method takes a URI representing a QName and returns the
 	 * namespace-resolved URI based on DELI's namespace lookup table.
 	 * 
 	 * @param theUri The unresolved URI
 	 */
 	private Resource getRealQName(Resource theUri) {
 		Resource aliasQName = null;
 		if (theUri != null) {
 			String theUristring = theUri.toString();
 			String realUri = getRealNamespace(theUristring);
 
 			if (realUri != null) {
 				aliasQName = ResourceFactory.createResource(realUri);
 			} else {
 				log.error("Fatal error: cannot find " + theUristring);
 			}
 
 		}
 
 		return aliasQName;
 	}
 
 	/**
 	 * This method returns a Vector of Maps representing the properties of
 	 * Vocabulary Attributes with a given Attribute Name. The attribute name can
 	 * either be qualified (in which case it will contain a hash character) or
 	 * unqualified.
 	 * 
 	 * @param name The attribute name as a string (eg 'ColorCapable').
 	 */
 	protected Vector<Map<String, Resource>> getAttPropertiesWithAttName(String name) {
 		Vector<Map<String, Resource>> v = new Vector<Map<String, Resource>>();
 
 		if (name.lastIndexOf("#") > 0) {
 			// Qualified attribute name
 			Resource aliasQName = null;
 
 			String namespace = getRealNamespace(name);
 
 			if (namespace != null) {
 				aliasQName = ResourceFactory.createResource(namespace);
 			}
 
 			if (aliasQName != null) {
 				v.add(propertyDescription.get(aliasQName));
 			} else {
 				return null;
 			}
 		}
 		// Unqualified attribute name
 
 		for (Resource qn : propertyDescription.keySet()) {
 			if (qn.getLocalName().equals(name)) {
 				v.add(propertyDescription.get(qn));
 			}
 		}
 
 		return v;
 	}
 
 	/*
 	 * This method attempts to get an attributes property set, given only a
 	 * QName. It therefore assumes that there is only one set of properties for
 	 * the attribute name specified. If there are infact more, the first set
 	 * encountered is all that is returned anyway.
 	 * 
 	 * @param attributeQName The attribute QName as a URI
 	 */
 	protected Map<String, Resource> getAttribute(Resource attributeQName) {
 		Resource aliasAttributeQName = getRealQName(attributeQName);
 		return propertyDescription.get(aliasAttributeQName);
 	}
 
 	/**
 	 * This method allows the processor to add a new attribute to the
 	 * vocabulary, when it encounters attributes that are not defined in the
 	 * vocabulary definition.
 	 * 
 	 * @param qualifiedAttribute
 	 * @param currentComponent The qualified name of the component, if known.
 	 * @param collectionType The collection type, if known.
 	 */
 	protected void addAttributeToVocabulary(String qualifiedAttribute, String currentComponent, String collectionType) {
 		// NOTE: since we cannot add a null-valued entry to a hashmap, we simply
 		// do not add
 		// the component if it has a null value. otherwise, when .get(key) is
 		// called, it returns
 		// null. which is the same as adding the entry with the null value.
 		qualifiedAttribute = getRealNamespace(qualifiedAttribute);
 
 		Resource temp = ResourceFactory.createResource(qualifiedAttribute);
 		String theURI = temp.getNameSpace();
 
 		if (theURI == null) {
 			theURI = getRealNamespace(currentComponent);
 		}
 
 		if (theURI != null) {
 			currentComponent = (currentComponent == null) ? (theURI + "Unknown") : getRealNamespace(currentComponent);
 			collectionType = (collectionType == null) ? Constants.SIMPLE : collectionType;
 
 			HashMap<String, Resource> properties = new HashMap<String, Resource>();
 
 			properties.put(Constants.ATTRIBUTE, ResourceFactory.createResource(qualifiedAttribute));
 			properties.put(Constants.COMPONENT, getRealQName(ResourceFactory.createResource(currentComponent)));
 			properties.put(Constants.type, getRealQName(ResourceFactory.createResource(theURI + Constants.LITERAL)));
 			properties.put(Constants.RESOLUTION, getRealQName(ResourceFactory.createResource(theURI
 					+ Constants.OVERRIDE)));
 			properties.put(Constants.COLLECTIONTYPE, getRealQName(ResourceFactory.createResource(theURI
 					+ collectionType)));
 
 			propertyDescription.put(properties.get(Constants.ATTRIBUTE), properties);
 		} else {
 			log.error("Fatal error: cannot create new attribute when namespace is null");
 		}
 	}
 
 	/**
 	 * Get the actual namespace from a given alias.
 	 * 
 	 * @param alias the aliased namespace
 	 * @return The _actual_ namespace being used for the vocabulary.
 	 */
 	protected String getRealNamespace(String alias) {
 		if (alias == null) {
 			return null;
 		}
 
 		Resource temp = ResourceFactory.createResource(alias);
 		String fragment = temp.getLocalName();
 		String aliasNamespace = temp.getNameSpace();
 
 		if (aliasNamespace != null) {
 			aliasNamespace = aliasNamespace.endsWith("#") ? aliasNamespace : (aliasNamespace + "#");
 
 			if (namespaceLookup.containsKey(aliasNamespace)) {
 				String namespace = (String) namespaceLookup.get(aliasNamespace);
 
 				if (fragment != null) {
 					return namespace + fragment;
 				}
 				return namespace;
 			}
 		}
 		return alias;
 	}
 
 	private class SchemaProcess {
 
 		private Property resolutionRuleProperty = null;
 
 		private Model vocabularySchema = null;
 
 		private String prfUri = null;
 
 		private String schemaName = null;
 
 		/**
 		 * This method traverses a UAProf RDF schema vocabulary definition file
 		 * using Jena.
 		 * 
 		 * @param in The schema file to be processed.
 		 * @param prfUri The URI of the namespace to be associated with the
 		 *            schema.
 		 * @param datatypesDef
 		 * @throws Exception
 		 */
 		private SchemaProcess(InputStream in, String prfUri, String schemaName, List<String> datatypesDef)
 				throws Exception {
 			this.prfUri = prfUri;
 			this.schemaName = schemaName;
 			vocabularySchema = ModelFactory.createDefaultModel();
 			vocabularySchema.read(in, prfUri);
 
 			// loop over all the schema URIs being used by this vocabulary
 
 			// are we processing a UAProf 2.0 vocabulary?
 			if (datatypesLookup.containsValue(prfUri)) {
 				resolutionRuleProperty = vocabularySchema.createProperty(prfUri + "ResolutionRule");
 
 				// temporarily attach instances of these datatypes to the
 				// vocabulary schema so we can retrieve datatype information
 				Iterator<String> datatypesDefIterator = datatypesDef.iterator();
 				while (datatypesDefIterator.hasNext()) {
 					vocabularySchema.createTypedLiteral(datatypesDefIterator.next());
 				}
 			}
 			// process the properties
 			ResIterator attributeList = vocabularySchema.listSubjectsWithProperty(RDFS.domain);
 			while (attributeList.hasNext()) {
 				processProperty((Resource) attributeList.next());
 			}
 		}
 
 		private void processProperty(Resource attribute) {
 			HashMap<String, Resource> properties = new HashMap<String, Resource>();
 			String attributeName = attribute.getLocalName();
 
 			if ((!attributeName.equals("Defaults")) && (!attributeName.equals("ResolutionRule"))) {
 				String collectionTypeName = Constants.SIMPLE;
 				String resolutionRule = Constants.OVERRIDE;
 				String attributeType = Constants.DEFAULT_ATTRIBUTE_TYPE;
 				boolean ccppTypeFound = false;
 
 				String componentName = getComponentName(attribute);
 				NodeIterator rangeList = vocabularySchema.listObjectsOfProperty(attribute, RDFS.range);
 				while (rangeList.hasNext()) {
 					Resource rangeEntry = (Resource) rangeList.next();
 					String sRangeEntry = rangeEntry.toString();
 
 					if (sRangeEntry.endsWith(Constants.BAG) || sRangeEntry.endsWith(Constants.SEQ)) {
 						collectionTypeName = rangeEntry.getLocalName();
 					}
 
 					if (sRangeEntry.endsWith(Constants.BOOLEAN) || sRangeEntry.endsWith(Constants.DIMENSION)
 							|| sRangeEntry.endsWith(Constants.NUMBER) || sRangeEntry.endsWith(Constants.LITERAL)) {
 						properties.put(Constants.type, ResourceFactory.createResource(sRangeEntry));
 						ccppTypeFound = true;
 					}
 				}
 
 				if (datatypesLookup.containsValue(prfUri)) {
 					resolutionRule = getUAProf2ResolutionRule(attribute);
 				} else {
 					// UAProf 1.0 - get the comment field
 					String commentString = getCommentString(attribute);
 					collectionTypeName = getCollectionTypeFromComments(commentString, attributeName, collectionTypeName);
 					StringTokenizer str = new StringTokenizer(commentString, " \t|\n");
 
 					while (str.hasMoreTokens()) {
 						String current = str.nextToken();
 
 						if (current.equals("Type:")) {
 							if (attributeType.equals(Constants.DEFAULT_ATTRIBUTE_TYPE)) {
 								attributeType = str.nextToken().trim();
 								if (!attributeType.equals(Constants.BOOLEAN)
 										&& !attributeType.equals(Constants.LITERAL)
 										&& !attributeType.equals(Constants.NUMBER)
 										&& !attributeType.equals("Dimension")) {
 									log.error("FATAL ERROR LOADING VOCABULARY: " + attributeName
 											+ " does not have a valid type " + attributeType);
 								}
 							}
 
 							properties.put(Constants.type, ResourceFactory.createResource(prfUri + attributeType));
 							ccppTypeFound = true;
 						}
 
 						if (current.equals("Resolution:")) {
 							resolutionRule = str.nextToken().trim();
 						}
 					}
 				}
  
 				if (!ccppTypeFound) {
 					properties.put(Constants.type, ResourceFactory.createResource(prfUri + attributeType));
 				}
 				Resource attName = ResourceFactory.createResource(prfUri + attributeName);
 				properties.put(Constants.ATTRIBUTE, attName);
 				properties.put(Constants.COMPONENT, ResourceFactory.createResource(componentName));
 				properties.put(Constants.RESOLUTION, ResourceFactory.createResource(prfUri + resolutionRule));
 				properties.put(Constants.COLLECTIONTYPE, ResourceFactory.createResource(prfUri + collectionTypeName));
 				if (propertyDescription.containsKey(attName)) {
 					log.error(attName.getURI() + " is already defined");
 				} else {
 					propertyDescription.put(attName, properties);
 				}
 			}
 		}
 		
 		private String getUAProf2ResolutionRule(Resource attribute) {
 			// UAProf 2.0 - get the resolution Rule
 			NodeIterator resolutionRuleList = vocabularySchema.listObjectsOfProperty(attribute,
 					resolutionRuleProperty);
 
 			if (resolutionRuleList.hasNext()) {
 				Literal resolution = (Literal) resolutionRuleList.next();
 				return resolution.getValue().toString();
 			}
 			return null;
 		}
 
 		private String getComponentName(Resource attribute) {
 			// Get the component name
 			NodeIterator componentList = vocabularySchema.listObjectsOfProperty(attribute, RDFS.domain);
 			if (componentList.hasNext()) {
 				return ((Resource) componentList.nextNode()).getURI();
 			}
 
 			return null;
 		}
 
 		private String getCollectionTypeFromComments(String commentString, String attributeName,
 				String collectionTypeName) {
 			if (commentString.indexOf("(bag)") > 0) {
 				if (!collectionTypeName.equals(Constants.BAG)) {
 					log.error("Vocabulary inconsistency warning: " + attributeName + " only declares it is a "
 							+ Constants.BAG + " in the comments field for schema " + schemaName);
 				}
 				collectionTypeName = Constants.BAG;
 			} else if (commentString.indexOf("(sequence)") > 0) {
 				if (!collectionTypeName.equals(Constants.SEQ)) {
 					log.error("Vocabulary inconsistency warning: " + attributeName + " only declares it is a "
 							+ Constants.SEQ + " in the comments field for schema" + schemaName);
 				}
 				collectionTypeName = Constants.SEQ;
 			}
 			return collectionTypeName;
 		}
 
 		private String getCommentString(Resource attribute) {
 			StringBuffer comment = new StringBuffer();
 			NodeIterator commentList = vocabularySchema.listObjectsOfProperty(attribute, RDFS.comment);
 			while (commentList.hasNext()) {
 				comment.append(commentList.nextNode().toString());
 			}
 			return comment.toString();
 		}
 	}
 }
