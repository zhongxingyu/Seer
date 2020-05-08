 package xbrlcore.instance;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Namespace;
 import org.jdom.input.SAXBuilder;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import xbrlcore.constants.ExceptionConstants;
 import xbrlcore.constants.GeneralConstants;
 import xbrlcore.constants.NamespaceConstants;
 import xbrlcore.dimensions.MultipleDimensionType;
 import xbrlcore.dimensions.SingleDimensionType;
 import xbrlcore.exception.InstanceException;
 import xbrlcore.exception.XBRLException;
 import xbrlcore.taxonomy.Concept;
 import xbrlcore.taxonomy.DiscoverableTaxonomySet;
 import xbrlcore.taxonomy.DTSFactory;
 import xbrlcore.taxonomy.TaxonomySchema;
 
 /**
  * This class is responsible for creating an Instance object of an instance
  * file. <br/><br/>
  * 
  * @author Daniel Hamm
  * 
  */
 public class InstanceFactory {
 
 	private static InstanceFactory xbrlInstanceFactory;
 
 	private Instance instance;
 
 	private Document xmlInstance;
 
 	private Map contextMap;
 
 	private Map unitMap;
 
 	private String PATH;
 
 	private List schemaRefNamespaces;
 
 	/**
 	 * Constructor, private.
 	 * 
 	 */
 	private InstanceFactory() {
 		contextMap = new HashMap();
 		unitMap = new HashMap();
 		schemaRefNamespaces = new ArrayList();
 	}
 
 	/**
 	 * 
     * @return the only Instance of InstanceFactory object (singleton).
 	 */
 	public static synchronized InstanceFactory get() {
 		if (xbrlInstanceFactory == null) {
 			xbrlInstanceFactory = new InstanceFactory();
 		}
 		return xbrlInstanceFactory;
 	}
 
 	/**
 	 * Creates an xbrlcore.instance.Instance object.
 	 * 
 	 * @param instanceFile
 	 *            Instance file.
 	 * @return An object of xbrlcore.instance.Instance.
 	 * @throws JDOMException
 	 * @throws IOException
 	 * @throws XBRLException
 	 */
 	public Instance createInstance(File instanceFile) throws JDOMException,
 			IOException, InstanceException, XBRLException,
 			CloneNotSupportedException, SAXException,
 			ParserConfigurationException {
 		PATH = instanceFile.getAbsolutePath().substring(0,
 				instanceFile.getAbsolutePath().lastIndexOf(File.separator) + 1);
 
 		/* Initialise XML document with SAX of JDOM */
 		SAXBuilder builder = new SAXBuilder();
 		xmlInstance = builder.build(PATH + instanceFile.getName());
 
 		/* determine taxonomy names */
 		Set taxonomyNameSet = getReferencedSchemaNames(xmlInstance);
 
		/* now build the taxonomy */
 		xbrlcore.taxonomy.sax.SAXBuilder xbrlBuilder = new xbrlcore.taxonomy.sax.SAXBuilder();
 		Iterator taxonomyNameSetIterator = taxonomyNameSet.iterator();
 		Set dtsSet = new HashSet();
 		while (taxonomyNameSetIterator.hasNext()) {
 			String currTaxonomyName = (String) taxonomyNameSetIterator.next();
 			String path = PATH + currTaxonomyName;
 			String source = path.replaceAll("\\\\", "/");
 			DiscoverableTaxonomySet currTaxonomy = xbrlBuilder
 					.build(new InputSource(source));
 			dtsSet.add(currTaxonomy);
 		}
 		return getInstance(dtsSet, instanceFile.getName());
 	}
 
 	/**
 	 * Builds an instance.
 	 * 
 	 * @param dtsSet
 	 *            Set of discoverable taxonomy sets this instance refers to.
 	 * @return An object of xbrlcore.instance.Instance.
 	 * @throws InstanceException
 	 */
 	private Instance getInstance(Set dtsSet, String fileName)
 			throws InstanceException, CloneNotSupportedException {
 
 		instance = new Instance(dtsSet);
 		instance.setFileName(fileName);
 
 		/* set instance namespace */
 		setInstanceNamespace();
 
 		/* set additional namespaces of the root element */
 		setAdditionalNamespaces(xmlInstance.getRootElement()
 				.getAdditionalNamespaces());
 
 		/* set additional namespaces of the schemaRef elements */
 		setAdditionalNamespaces(schemaRefNamespaces);
 
 		/* determine the schema location */
 		setSchemaLocation();
 
 		/* set context elements */
 		setContextElements();
 
 		/* set unit elements */
 		setUnitElements();
 
 		/* set facts */
 		setFacts();
 
 		return instance;
 	}
 
 	/**
 	 * Determines which taxonomy an instance refers to.
 	 * 
 	 * @param currDocument
 	 *            Structure of the instance file.
 	 * @return Set of names of the taxonomy the instance refers to.
 	 */
 	private Set getReferencedSchemaNames(Document currDocument) {
 		Set referencedSchemaNameSet = new HashSet();
 		List elementList = currDocument.getRootElement().getChildren();
 		Iterator elementListIterator = elementList.iterator();
 		while (elementListIterator.hasNext()) {
 			Element currElement = (Element) elementListIterator.next();
 			if (currElement.getName().equals("schemaRef")) {
 				referencedSchemaNameSet.add(currElement.getAttributeValue(
 						"href", NamespaceConstants.XLINK_NAMESPACE));
 				/* set namespaces of schemaRef element */
 				schemaRefNamespaces = currElement.getAdditionalNamespaces();
 			}
 		}
 		return referencedSchemaNameSet;
 	}
 
 	/**
 	 * Sets the namespace of the instance.
 	 * 
 	 */
 	private void setInstanceNamespace() {
 		Namespace instanceNamespace = xmlInstance.getRootElement()
 				.getNamespace();
 		instance.setInstanceNamespace(instanceNamespace);
 	}
 
 	/**
 	 * Sets additional namespaces needed in this instance.
 	 * 
 	 */
 	private void setAdditionalNamespaces(List additionalNamespaces) {
 		Iterator additionalNamespacesIterator = additionalNamespaces.iterator();
 		while (additionalNamespacesIterator.hasNext()) {
 			Namespace currentNamespace = (Namespace) additionalNamespacesIterator
 					.next();
 			if (instance.getNamespace(currentNamespace.getURI()) == null) {
 				instance.addNamespace(currentNamespace);
 			}
 		}
 	}
 
 	/**
 	 * Sets schema location information defined in this instance.
 	 * 
 	 * 
 	 */
 	private void setSchemaLocation() {
 		if (xmlInstance.getRootElement().getAttributes().size() > 0
 				&& xmlInstance.getRootElement().getAttribute(
 						"schemaLocation",
 						instance.getNamespace(NamespaceConstants.XSI_NAMESPACE
 								.getURI())) != null) {
 			String schemaLocationValue = xmlInstance
 					.getRootElement()
 					.getAttributeValue(
 							"schemaLocation",
 							instance
 									.getNamespace(NamespaceConstants.XSI_NAMESPACE
 											.getURI()));
 			while (schemaLocationValue.indexOf(" ") > 0) {
 				String schemaLocationURI = schemaLocationValue.substring(0,
 						schemaLocationValue.indexOf(" "));
 				schemaLocationValue = schemaLocationValue.substring(
 						schemaLocationValue.indexOf(" "), schemaLocationValue
 								.length());
 				schemaLocationValue = schemaLocationValue.trim();
 				String schemaLocationPrefix = null;
 				if (schemaLocationValue.indexOf(" ") > 0) {
 					schemaLocationPrefix = schemaLocationValue.substring(0,
 							schemaLocationValue.indexOf(" "));
 					schemaLocationValue = schemaLocationValue.substring(
 							schemaLocationValue.indexOf(" "),
 							schemaLocationValue.length());
 					schemaLocationValue = schemaLocationValue.trim();
 				} else {
 					schemaLocationPrefix = schemaLocationValue;
 				}
 				instance.addSchemaLocation(schemaLocationURI,
 						schemaLocationPrefix);
 			}
 		}
 	}
 
 	/**
 	 * Sets unit elements defined in this instance.
 	 * 
 	 * @throws InstanceException
 	 */
 	private void setUnitElements() throws InstanceException {
 		List unitElementList = xmlInstance.getRootElement().getChildren("unit",
 				instance.getInstanceNamespace());
 		Iterator unitElementListIterator = unitElementList.iterator();
 		while (unitElementListIterator.hasNext()) {
 			Element currUnitElement = (Element) unitElementListIterator.next();
 			String id = currUnitElement.getAttributeValue("id");
 
 			if (id == null || id.length() == 0) {
 				throw new InstanceException(
 						ExceptionConstants.EX_INSTANCE_CREATION_NOID_UNIT);
 			}
 
 			InstanceUnit currUnit = new InstanceUnit(id);
 
 			/* set the value and its namespace */
 			Element measureElement = currUnitElement.getChild("measure",
 					instance.getInstanceNamespace());
 			String value = measureElement.getValue();
 
 			String namespacePrefix = value.substring(0, value.indexOf(":"));
 			String unitValue = value.substring(value.indexOf(":") + 1, value
 					.length());
 
 			currUnit.setNamespaceURI(instance.getNamespaceURI(namespacePrefix));
 			currUnit.setValue(unitValue);
 
 			unitMap.put(id, currUnit);
 		}
 	}
 
 	/**
 	 * @param name
 	 * @return Returns a Concept object to a given name from all the
 	 *         Discoverable Taxonomy Sets the instance refers to.
 	 */
 	private Concept getConceptByName(String name) {
 		Set dtsSet = instance.getDiscoverableTaxonomySet();
 		Iterator dtsSetIterator = dtsSet.iterator();
 		while (dtsSetIterator.hasNext()) {
 			DiscoverableTaxonomySet currDts = (DiscoverableTaxonomySet) dtsSetIterator
 					.next();
 			Concept currConcept = currDts.getConceptByName(name);
 			if (currConcept != null) {
 				return currConcept;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Sets context elements of this instance.
 	 * 
 	 * @throws InstanceException
 	 */
 	private void setContextElements() throws InstanceException,
 			CloneNotSupportedException {
 		List contextElementList = xmlInstance.getRootElement().getChildren(
 				"context", instance.getInstanceNamespace());
 		Iterator contextElementListIterator = contextElementList.iterator();
 		while (contextElementListIterator.hasNext()) {
 			Element currContextElement = (Element) contextElementListIterator
 					.next();
 			String id = currContextElement.getAttributeValue("id");
 
 			if (id == null || id.length() == 0) {
 				throw new InstanceException(
 						ExceptionConstants.EX_INSTANCE_CREATION_NOID_CONTEXT);
 			}
 
 			InstanceContext currContext = new InstanceContext(id);
 
 			/* set identifier scheme and identifier */
 			Element identifierElement = currContextElement.getChild("entity",
 					instance.getInstanceNamespace()).getChild("identifier",
 					instance.getInstanceNamespace());
 			currContext.setIdentifierScheme(identifierElement
 					.getAttributeValue("scheme"));
 			currContext.setIdentifier(identifierElement.getValue());
 
 			/* set period type and period */
 			Element periodElement = currContextElement.getChild("period",
 					instance.getInstanceNamespace());
 			if (periodElement != null) {
 				if (periodElement.getChild("startDate", instance
 						.getInstanceNamespace()) != null
 						&& periodElement.getChild("endDate", instance
 								.getInstanceNamespace()) != null) {
 					currContext.setPeriodStartDate(periodElement.getChild(
 							"startDate", instance.getInstanceNamespace())
 							.getText());
 					currContext.setPeriodEndDate(periodElement.getChild(
 							"endDate", instance.getInstanceNamespace())
 							.getText());
 				} else if (periodElement.getChild("instant", instance
 						.getInstanceNamespace()) != null) {
 					if (periodElement.getChild("instant",
 							instance.getInstanceNamespace()).getChild(
 							"forever", instance.getInstanceNamespace()) != null) {
 						currContext.setPeriodValue("forever");
 					} else {
 						currContext.setPeriodValue(periodElement.getChild(
 								"instant", instance.getInstanceNamespace())
 								.getText());
 					}
 				}
 			}
 
 			/*
 			 * set multidimensional information - parse both <scenario> and
 			 * <segment> element
 			 */
 			List scenSegElementList = new ArrayList();
 			Element scenarioElement = currContextElement.getChild("scenario",
 					instance.getInstanceNamespace());
 			/* <segment> is a child element of <entity> */
 			Element segmentElement = currContextElement.getChild("entity",
 					instance.getInstanceNamespace()).getChild("segment",
 					instance.getInstanceNamespace());
 			if (scenarioElement != null)
 				scenSegElementList.add(scenSegElementList.size(),
 						scenarioElement);
 			if (segmentElement != null)
 				scenSegElementList.add(scenSegElementList.size(),
 						segmentElement);
 
 			for (int i = 0; i < scenSegElementList.size(); i++) {
 				Element currElement = (Element) scenSegElementList.get(i);
 				List explicitMemberElementList = currElement
 						.getChildren(
 								"explicitMember",
 								instance
 										.getNamespace(NamespaceConstants.XBRLDI_NAMESPACE
 												.getURI()));
 				List typedMemberElementList = currElement
 						.getChildren(
 								"typedMember",
 								instance
 										.getNamespace(NamespaceConstants.XBRLDI_NAMESPACE
 												.getURI()));
 				Iterator explicitMemberElementListIterator = explicitMemberElementList
 						.iterator();
 				Iterator typedMemberElementListIterator = typedMemberElementList
 						.iterator();
 				MultipleDimensionType mdt = null;
 				/* set explicit member */
 				while (explicitMemberElementListIterator.hasNext()) {
 					Element currExplicitMemberElement = (Element) explicitMemberElementListIterator
 							.next();
 
 					/* determine dimension element */
 					String dimensionAttribute = currExplicitMemberElement
 							.getAttributeValue("dimension");
 					String prefix = dimensionAttribute.substring(0,
 							dimensionAttribute.indexOf(":"));
 					String dimensionElementName = dimensionAttribute.substring(
 							dimensionAttribute.indexOf(":") + 1,
 							dimensionAttribute.length());
 					if (instance.getSchemaForURI(currExplicitMemberElement
 							.getNamespace(prefix)) == null) {
 						throw new InstanceException(
 								ExceptionConstants.EX_INSTANCE_CREATION_NO_SCHEMA_PREFIX
 										+ prefix);
 					}
 					Concept dimensionElement = instance.getSchemaForURI(
 							currExplicitMemberElement.getNamespace(prefix))
 							.getConceptByName(dimensionElementName);
 
 					/* determine domain member element */
 					String value = currExplicitMemberElement.getValue();
 					String domainMemberElementName = value.substring(value
 							.indexOf(":") + 1, value.length());
 					Concept domainMemberElement = getConceptByName(domainMemberElementName);
 
 					if (dimensionElement == null || domainMemberElement == null) {
 						throw new InstanceException(
 								ExceptionConstants.EX_INSTANCE_CREATION_DIMENSIONS
 										+ id);
 					}
 
 					if (mdt == null) {
 						mdt = new MultipleDimensionType(dimensionElement,
 								domainMemberElement);
 					} else {
 						mdt
 								.addPredecessorDimensionDomain(new SingleDimensionType(
 										dimensionElement, domainMemberElement));
 					}
 				}
 				/* set typed member */
 				while (typedMemberElementListIterator.hasNext()) {
 					Element currTypedMemberElement = (Element) typedMemberElementListIterator
 							.next();
 
 					/* determine dimension element */
 					String dimensionAttribute = currTypedMemberElement
 							.getAttributeValue("dimension");
 					String prefix = dimensionAttribute.substring(0,
 							dimensionAttribute.indexOf(":"));
 					String dimensionElementName = dimensionAttribute.substring(
 							dimensionAttribute.indexOf(":") + 1,
 							dimensionAttribute.length());
 					Concept dimensionElement = instance.getSchemaForURI(
 							currTypedMemberElement.getNamespace(prefix))
 							.getConceptByName(dimensionElementName);
 
 					/*
 					 * SingleDimensionType represtents the typed dimension
 					 * element and its content
 					 */
 					SingleDimensionType sdt = null;
 
 					/* set typed dimension element */
 					if (currTypedMemberElement.getChildren().size() != 0) {
 						Element childElement = (Element) currTypedMemberElement
 								.getChildren().get(0);
 						sdt = new SingleDimensionType(dimensionElement,
 								childElement);
 					}
 
 					if (mdt == null) {
 						mdt = new MultipleDimensionType(sdt);
 					} else {
 						mdt.addPredecessorDimensionDomain(sdt);
 					}
 				}
 
 				if (mdt != null && currElement.getName().equals("scenario")) {
 					currContext.setDimensionalInformation(mdt,
 							GeneralConstants.DIM_SCENARIO);
 				} else if (mdt != null
 						&& currElement.getName().equals("segment")) {
 					currContext.setDimensionalInformation(mdt,
 							GeneralConstants.DIM_SEGMENT);
 				}
 			}
 
 			contextMap.put(id, currContext);
 			instance.addContext(currContext);
 		}
 
 	}
 
 	/**
 	 * Sets facts of the instance.
 	 * 
 	 * @throws InstanceException
 	 */
 	private void setFacts() throws InstanceException {
 		List factElementList = xmlInstance.getRootElement().getChildren();
 		Iterator factElementListIterator = factElementList.iterator();
 		while (factElementListIterator.hasNext()) {
 			Element currFactElement = (Element) factElementListIterator.next();
 			if (!currFactElement.getName().equals("context")
 					&& !currFactElement.getName().equals("schemaRef")
 					&& !currFactElement.getName().equals("unit")) {
 
 				String factElementName = currFactElement.getName();
 				TaxonomySchema schema = instance
 						.getSchemaForURI(currFactElement.getNamespace());
 				Concept currFactXBRLElement = schema
 						.getConceptByName(factElementName);
 
 				if (currFactXBRLElement == null) {
 					throw new InstanceException(
 							ExceptionConstants.EX_INSTANCE_CREATION_FACT
 									+ factElementName);
 				}
 
 				/* now it is a fact element */
 				Fact newFact = new Fact(currFactXBRLElement);
 
 				/* check if it refers to a valid context and unit */
 				String contextID = currFactElement
 						.getAttributeValue("contextRef");
 				String unitID = currFactElement.getAttributeValue("unitRef");
 				InstanceContext ctx = (InstanceContext) contextMap
 						.get(contextID);
 				InstanceUnit unit = (InstanceUnit) unitMap.get(unitID);
 				if (ctx == null) {
 					throw new InstanceException(
 							ExceptionConstants.EX_INSTANCE_CREATION_NO_CONTEXT
 									+ factElementName);
 				}
 
 				newFact.setInstanceContext(ctx);
 				newFact.setInstanceUnit(unit);
 
 				/* set remaining information */
 				if (currFactElement.getAttributeValue("decimals") != null) {
 					newFact.setDecimals(currFactElement
 							.getAttributeValue("decimals"));
 				}
 				if (currFactElement.getAttributeValue("precision") != null) {
 					newFact.setPrecision(currFactElement
 							.getAttributeValue("precision"));
 				}
 				newFact.setValue(currFactElement.getValue());
 
 				/* finally, add the fact to the instance */
 				instance.addFact(newFact);
 			}
 		}
 	}
 }
