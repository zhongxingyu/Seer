 package controller.merge.xmi.xclass;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import uml2parser.ModelFileInfo;
 import uml2parser.XmiElement;
 import controller.comparer.xmi.XmiAssociationElement;
 import controller.comparer.xmi.XmiAttributeElement;
 import controller.comparer.xmi.XmiBaseElement;
 import controller.comparer.xmi.XmiClassDiagramComparer;
 import controller.comparer.xmi.XmiClassDiagramParser;
 import controller.comparer.xmi.XmiClassElement;
 import controller.comparer.xmi.XmiGeneralizationElement;
 import controller.comparer.xmi.XmiMemberEndElement;
 import controller.comparer.xmi.XmiOperationElement;
 import controller.comparer.xmi.XmiParameterElement;
 import controller.comparer.xmi.XmiValueElement;
 
 public class XmiMergeClassProcessor {
 
 	// Tags
 	private final static String PAPYRUS_UML_MODEL = "uml:Model";
 	private final static String PAPYRUS_NOTATION_DIAGRAM = "notation:Diagram";
 	private final static String PAPYRUS_PACKAGED_ELEM = "packagedElement";
 	private final static String PAPYRUS_OPERATION_ELEM = "ownedOperation";
 	private final static String PAPYRUS_PROPERTY_ELEM = "ownedAttribute";
 	private final static String PAPYRUS_PARAMETER_ELEM = "ownedParameter";
 	private final static String PAPYRUS_GENERALIZATION_ELEM = "generalization";
 	private final static String PAPYRUS_MEMBER_END = "ownedEnd";
 	private final static String PAPYRUS_LOWER_VALUE = "lowerValue";
 	private final static String PAPYRUS_UPPER_VALUE = "upperValue";
 	private final static String PAPYRUS_DEFAULT_VALUE = "defaultValue";
 	private final static String PAPYRUS_VALUE = "value";
 
 	// UML Types
 	private final static String PAPYRUS_PACKAGE_TYPE_CLASS = "uml:Class";
 	private final static String PAPYRUS_PACKAGE_TYPE_INTERFACE = "uml:Interface";
 	private final static String PAPYRUS_PACKAGE_TYPE_PRIMITIVE = "uml:PrimitiveType";
 	private final static String PAPYRUS_PACKAGE_TYPE_ASSOCIATION = "uml:Association";
 	private final static String PAPYRUS_PACKAGE_TYPE_LITERNAL_INTEGER = "uml:LiteralInteger";
 	private final static String PAPYRUS_PACKAGE_TYPE_LITERNAL_UNLILMITED = "uml:LiteralUnlimitedNatural";
 
 	// Attributes
 	private final static String PAPYRUS_XMI_ATTRIBUTE_TYPE = "xmi:type";
 	private final static String PAPYRUS_ATTRIBUTE_NAME = "name";
 	private final static String PAPYRUS_ATTRIBUTE_ID = "xmi:id";
 	private final static String PAPYRUS_ATTRIBUTE_TYPE = "type";
 	private final static String PAPYRUS_ATTRIBUTE_VISIBILITY = "visibility";
 	private final static String PAPYRUS_ATTRIBUTE_DIRECTION = "direction";
 	private final static String PAPYRUS_GENERALIZATION_DIRECTION = "general";
 	private final static String PAPYRUS_ATTRIBUTE_ASSOCIATION = "association";
 	private final static String PAPYRUS_ATTRIBUTE_VALUE = "value";
 	private final static String PAPYRUS_AGGREGATION = "aggregation";
 	private final static String PAPYRUS_XSI_NIL = "xsi:nil";
 	private final static String PAPYRUS_ATTRIBUTE_GENERAL = "general";
 
 	// Queue stores the notatinoElements that will be used by the notatino
 	// processor to generation noation file.
 	private LinkedList<XmiNotationElement> notationElements = new LinkedList<XmiNotationElement>();
 	
 	// Generalization is built seperately and attached to the end of notationElements
 	private LinkedList<XmiNotationElement> notationGeneral = new LinkedList<XmiNotationElement>();
 	
 	private DocumentBuilderFactory docFactory = DocumentBuilderFactory
 			.newInstance();
 	private DocumentBuilder docBuilder;
 
 	private Document umlDoc;
 	private Element umlRootElement;
 
 	private String fileName;
 	private String fileId;
 	private String fileNotationId;
 	private String fileNotationName;
 
 	private HashMap<String, String> replaceClass2Id = new HashMap<String, String>();
 	private HashMap<String, String> mapGeneralToParent = new HashMap<String, String>();
 	private HashMap<String, String> mapParentToSource = new HashMap<String, String>();
 	
 	private NotationData notationData;
 	
 	private XmiClassDiagramComparer comparer;
 
 	
 	public void Process(XmiClassDiagramComparer comparer) {
 
 		this.comparer = comparer;
 
 		try {
 			Initialize();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}
 
 		ArrayList<XmiMergedClass> mergedList = comparer.getSameClass();
 
 		for (XmiMergedClass mergedClass : mergedList) {
 			createClass(mergedClass, umlDoc, umlRootElement);
 		}
 		
 		for (XmiMergedAssociation mergedAssociation : comparer.getAssociationUml()) {
 			createAssociation(mergedAssociation, umlDoc, umlRootElement);
 		}
 		
 		setupFileInformation();
 		
 		// Create notation data
 		for (XmiNotationElement element : notationGeneral) {
 			notationElements.addLast(element);
 		}
 		
 		notationData = new NotationData(
 				notationElements, mapGeneralToParent, mapParentToSource, fileName, fileId, fileNotationName,
 				fileNotationId);
 	}
 
 	/**
 	 * Retrieves the uml andn notation file ids and names
 	 */
 	private void setupFileInformation() {
 		XmiClassDiagramParser extractNameAndId;
 		if (comparer.getClassDiagram1() != null) {
 			extractNameAndId = comparer.getClassDiagram1();
 		} else {
 			extractNameAndId = comparer.getClassDiagram2();
 		}
 
 		fileName = extractNameAndId.getUmlFile().getFileNameNoExtension();
 		fileId = extractNameAndId.getUmlModelId();
 
 		XmiElement notationElement = extractNameAndId.getNotationFile()
 				.findElementsByName(PAPYRUS_NOTATION_DIAGRAM).get(0);
 		fileNotationId = notationElement
 				.getAttributeValue(this.PAPYRUS_ATTRIBUTE_ID);
 		fileNotationName = notationElement
 				.getAttributeValue(this.PAPYRUS_ATTRIBUTE_NAME);
 	}
 
 	private void setupIdTracker() {
 		for (XmiMergedClass element : comparer.getSameClass()) {
 			if (element.getClass1() != null && element.getClass2() != null) {
 				replaceClass2Id.put(element.getClass2().getId(), element.getClass1().getId());
 			}
 		}
 	}
 	/**
 	 * Creates the initial file and header. Also invokes a method to obtain the
 	 * file names and ids for uml and notation files.
 	 * 
 	 * @throws ParserConfigurationException
 	 */
 	private void Initialize() throws ParserConfigurationException {
 		setupFileInformation();
 		setupIdTracker();
 		
 		docBuilder = docFactory.newDocumentBuilder();
 
 		// UML Header
 		umlDoc = docBuilder.newDocument();
 		umlRootElement = umlDoc.createElement("uml:Model");
 		umlRootElement.setAttribute("xmi:version", "20110701");
 		umlRootElement.setAttribute("xmlns:xmi",
 				"http://www.omg.org/spec/XMI/20110701");
 		umlRootElement.setAttribute("xmlns:xsi",
 				"http://www.w3.org/2001/XMLSchema-instance");
 		umlRootElement.setAttribute("xmlns:uml",
 				"http://www.eclipse.org/uml2/4.0.0/UML");
 
 		umlRootElement.setAttribute("name", fileName);
 		umlRootElement.setAttribute("xmi:id", fileId);
 
 		umlDoc.appendChild(umlRootElement);
 	}
 
 	/**
 	 * Creates the class element
 	 * 
 	 * @param mergedClass
 	 * @param doc
 	 * @param rootElement
 	 */
 	private void createClass(XmiMergedClass mergedClass, Document doc,
 			Element rootElement) {
 
 		String newClassName = mergedClass.getNewName();
 		String classId;
 
 		XmiClassElement class1 = mergedClass.getClass1();
 		XmiClassElement class2 = mergedClass.getClass2();
 
 		if (class1 != null) {
 			classId = class1.getId();
 			// Add a notation element for notation file creation
 			notationElements.addLast(new XmiNotationElement(class1.getId(),
 					class1.getId(), comparer.getClassDiagram1()
 							.getNotationFile(), XmiNotationElement.TYPE_CLASS));
 		} else if (class2 != null) {
 			classId = class2.getId();
 			// Add a notation element for notation file creation
 			notationElements.addLast(new XmiNotationElement(class2.getId(),
 					class2.getId(), comparer.getClassDiagram2()
 							.getNotationFile(), XmiNotationElement.TYPE_CLASS));
 		} else {
 			classId = "NO ID";
 		}
 
 		// Base element
 		Element classElement = doc.createElement(PAPYRUS_PACKAGED_ELEM);
 		rootElement.appendChild(classElement);
 		classElement.setAttribute(PAPYRUS_XMI_ATTRIBUTE_TYPE,
 				PAPYRUS_PACKAGE_TYPE_CLASS);
 		classElement.setAttribute(PAPYRUS_ATTRIBUTE_ID, classId);
 		classElement.setAttribute(PAPYRUS_ATTRIBUTE_NAME, newClassName);
 
 		// create attributes
 		if (mergedClass.getAttributes() != null) {
 			for (XmiAttributeElement attributeElement : mergedClass
 					.getAttributes()) {
 				appendAttribute(attributeElement, doc, classElement);
 				// Add a notation element for notation file creation
 				notationElements.getLast().addElement(
 						new XmiNotationElement(attributeElement.getId(),
 								attributeElement.getId(), comparer
 										.getClassDiagram1().getNotationFile(),
 								XmiNotationElement.TYPE_ATTRIBUTE));
 			}
 		}
 
 		if (mergedClass.getAttributes2() != null) {
 			ArrayList<XmiAttributeElement> att2 = mergedClass.getAttributes2();
 
 			// Remove attributes that were already added from class1
 			if (mergedClass.getAttributes() != null) {
 				att2.removeAll(mergedClass.getAttributes());
 			}
 
 			for (XmiAttributeElement attributeElement : att2) {
 				appendAttribute(attributeElement, doc, classElement);
 				// Add a notation element for notation file creation
 				notationElements.getLast().addElement(
 						new XmiNotationElement(attributeElement.getId(),
 								attributeElement.getId(), comparer
 										.getClassDiagram2().getNotationFile(),
 								XmiNotationElement.TYPE_ATTRIBUTE));
 
 			}
 		}
 
 		// Create operations
 				if (mergedClass.getOperations() != null) {
 
 					for (XmiOperationElement operationElement : mergedClass
 							.getOperations()) {
 						appendOperation(operationElement, doc, classElement);
 						
 						notationElements.getLast().addElement(
 								new XmiNotationElement(operationElement.getId(),
 										operationElement.getId(), comparer
 												.getClassDiagram1().getNotationFile(),
 										XmiNotationElement.TYPE_OPERATION));
 					}
 				}
 
 				if (mergedClass.getOperations2() != null) {
 
 					// Remove operations that were already added from class1
 					ArrayList<XmiOperationElement> op = mergedClass.getOperations2();
 
 					if (mergedClass.getAttributes() != null) {
 						op.removeAll(mergedClass.getAttributes());
 					}
 
 					for (XmiOperationElement operationElement : (ArrayList<XmiOperationElement>) op) {
 						appendOperation(operationElement, doc, classElement);
 						notationElements.getLast().addElement(
 								new XmiNotationElement(operationElement.getId(),
 										operationElement.getId(), comparer
 												.getClassDiagram2().getNotationFile(),
 										XmiNotationElement.TYPE_OPERATION));
 					}
 				}
 
 		// Create generalization notation elements
 		if (mergedClass.getGeneralizations() != null) {
 
 			for (XmiGeneralizationElement generalization : mergedClass
 					.getGeneralizations()) {
 				appendGeneralization(generalization, doc, classElement, classId);
 
 				// Add a notation element for notation file creation
 				notationGeneral.addLast(new XmiNotationElement(generalization
 						.getId(), generalization.getId(), comparer
 						.getClassDiagram1().getNotationFile(),
 						XmiNotationElement.TYPE_GENERALIZATION));
 			}
 		}
 
 		if (mergedClass.getGeneralizations2() != null) {
 
 			// Remove generalizations that were already added from class1
 			ArrayList<XmiGeneralizationElement> gen = mergedClass
 					.getGeneralizations2();
 
 			if (mergedClass.getGeneralizations() != null) {
 				gen.removeAll(mergedClass.getGeneralizations());
 			}
 
 			for (XmiGeneralizationElement generalization : gen) {
 				appendGeneralization(generalization, doc, classElement, classId);
 							
 				// Add a notation element for notation file creation
 				notationGeneral.addLast(new XmiNotationElement(generalization
 						.getId(), generalization.getId(), comparer
 						.getClassDiagram2().getNotationFile(),
 						XmiNotationElement.TYPE_GENERALIZATION));
 			}
 		}
 
 	}
 
 	/**
 	 * Append the attribute Node
 	 * 
 	 * @param attribute
 	 * @param doc
 	 * @param rootElement
 	 */
 	private void appendAttribute(XmiAttributeElement attribute, Document doc,
 			Element rootElement) {
 
 		// Base element
 		Element attributeElement = doc.createElement(PAPYRUS_PROPERTY_ELEM);
 		rootElement.appendChild(attributeElement);
 
 		// If the attribute value is doesn't exist or has the default <DEFAULT>
 		// value, don't
 		// add it to the XML.
 		if (attribute.getUmlType() == null) {
 			if (!attribute.getUmlType().equals(XmiBaseElement.DEFAULT_TYPE)) {
 				attributeElement.setAttribute(PAPYRUS_XMI_ATTRIBUTE_TYPE,
 						attribute.getUmlType());
 			}
 		}
 
 		attributeElement.setAttribute(PAPYRUS_ATTRIBUTE_ID, attribute.getId());
 		attributeElement.setAttribute(PAPYRUS_ATTRIBUTE_NAME,
 				attribute.getName());
 		attributeElement.setAttribute(PAPYRUS_ATTRIBUTE_VISIBILITY,
 				attribute.getVisibility());
 
 		if (attribute.getLowerValue() != null) {
 			appendLowerValue(attribute.getLowerValue(), doc, attributeElement);
 		}
 
 		if (attribute.getUpperValue() != null) {
 			appendUpperValue(attribute.getUpperValue(), doc, attributeElement);
 		}
 
 		if (attribute.getDefaultValue() != null) {
 			appendDefaultValue(attribute.getDefaultValue(), doc,
 					attributeElement);
 		}
 	}
 
 	/**
 	 * Append LowerValue node
 	 * 
 	 * @param element
 	 * @param doc
 	 * @param rootElement
 	 */
 	private void appendLowerValue(XmiValueElement element, Document doc,
 			Element rootElement) {
 
 		Element attributeElement = doc.createElement(PAPYRUS_LOWER_VALUE);
 		rootElement.appendChild(attributeElement);
 		attributeElement.setAttribute(PAPYRUS_XMI_ATTRIBUTE_TYPE,
 				element.getUmlType());
 		attributeElement.setAttribute(PAPYRUS_ATTRIBUTE_ID, element.getId());
		attributeElement.setAttribute(PAPYRUS_ATTRIBUTE_VALUE,
				element.getValue());
 	}
 
 	private void appendUpperValue(XmiValueElement element, Document doc,
 			Element rootElement) {
 
 		Element attributeElement = doc.createElement(PAPYRUS_UPPER_VALUE);
 		rootElement.appendChild(attributeElement);
 		attributeElement.setAttribute(PAPYRUS_XMI_ATTRIBUTE_TYPE,
 				element.getUmlType());
 		attributeElement.setAttribute(PAPYRUS_ATTRIBUTE_ID, element.getId());
		attributeElement.setAttribute(PAPYRUS_ATTRIBUTE_VALUE,
				element.getValue());
 	}
 
 	private void appendDefaultValue(XmiValueElement element, Document doc,
 			Element rootElement) {
 
 		Element attributeElement = doc.createElement(PAPYRUS_DEFAULT_VALUE);
 		rootElement.appendChild(attributeElement);
 		attributeElement.setAttribute(PAPYRUS_XMI_ATTRIBUTE_TYPE,
 				element.getUmlType());
 		attributeElement.setAttribute(PAPYRUS_ATTRIBUTE_ID, element.getId());
 
 		if (element.isNull()) {
 			Element nilElement = doc.createElement(PAPYRUS_DEFAULT_VALUE);
 			attributeElement.appendChild(nilElement);
 			nilElement.setAttribute(PAPYRUS_XSI_NIL, "true");
 		} else {
 			attributeElement.setAttribute(PAPYRUS_ATTRIBUTE_VALUE,
 					element.getValue());
 			attributeElement.setAttribute(PAPYRUS_ATTRIBUTE_NAME,
 					element.getName());
 		}
 	}
 
 	/**
 	 * Append operation node
 	 * 
 	 * @param operation
 	 * @param doc
 	 * @param rootElement
 	 */
 	private void appendOperation(XmiOperationElement operation, Document doc,
 			Element rootElement) {
 
 		// Base element
 		Element operationElement = doc.createElement(PAPYRUS_OPERATION_ELEM);
 		rootElement.appendChild(operationElement);
 
 		// Only add the "type" attribute if it exist and not the default blank
 		// value
 		// Papyrus will throw an error if the type shouldn't exist.
 		if (operation.getUmlType() != null) {
 			if (!operation.getUmlType().equals(XmiBaseElement.DEFAULT_TYPE)) {
 				operationElement.setAttribute(PAPYRUS_XMI_ATTRIBUTE_TYPE,
 						operation.getUmlType());
 			}
 		}
 
 		operationElement.setAttribute(PAPYRUS_ATTRIBUTE_ID, operation.getId());
 		operationElement.setAttribute(PAPYRUS_ATTRIBUTE_NAME,
 				operation.getName());
 		operationElement.setAttribute(PAPYRUS_ATTRIBUTE_VISIBILITY,
 				operation.getVisibility());
 
 		for (XmiParameterElement parameter : operation.getParameters()) {
 			appendParameter(parameter, doc, operationElement);
 		}
 	}
 
 	/**
 	 * Append parameter node
 	 * 
 	 * @param element
 	 * @param doc
 	 * @param rootElement
 	 */
 	private void appendParameter(XmiParameterElement element, Document doc,
 			Element rootElement) {
 
 		Element paramElement = doc.createElement(PAPYRUS_PARAMETER_ELEM);
 		rootElement.appendChild(paramElement);
 
 		// Only add the "type" attribute if it exist and not the default blank
 		// value
 		// Papyrus will throw an error if the type shouldn't exist.
 		if (element.getUmlType() != null) {
 			if (!element.getUmlType().equals(XmiBaseElement.DEFAULT_TYPE)) {
 				paramElement.setAttribute(PAPYRUS_XMI_ATTRIBUTE_TYPE,
 						element.getUmlType());
 			}
 		}
 
 		// Only add the "direction" attribute if it exist, not "in" and not
 		// empty
 		if (element.getDirection() != null) {
 			if (!element.getDirection()
 					.equals(XmiParameterElement.DIRECTION_IN)
 					&& !element.getDirection().isEmpty()) {
 				paramElement.setAttribute(PAPYRUS_ATTRIBUTE_DIRECTION,
 						element.getDirection());
 			}
 		}
 
 		paramElement.setAttribute(PAPYRUS_ATTRIBUTE_VISIBILITY,
 				element.getVisibility());
 
 		paramElement.setAttribute(PAPYRUS_ATTRIBUTE_NAME, element.getName());
 
 		paramElement.setAttribute(PAPYRUS_ATTRIBUTE_ID, element.getId());
 
 	}
 
 	/**
 	 * Append operation node
 	 * 
 	 * @param generalization
 	 * @param doc
 	 * @param rootElement
 	 */
 	private void appendGeneralization(XmiGeneralizationElement generalization,
 			Document doc, Element rootElement, String classId) {
 
 		// Base element
 		Element operationElement = doc
 				.createElement(PAPYRUS_GENERALIZATION_ELEM);
 		rootElement.appendChild(operationElement);
 
 		operationElement.setAttribute(PAPYRUS_ATTRIBUTE_ID,
 				generalization.getId());
 
 		String parentId;
 		
 		if (replaceClass2Id.containsKey(generalization.getParent())) {
 			parentId = replaceClass2Id.get(generalization.getParent());
 		} else {
 			parentId = generalization.getParent();
 		}
 		
 		operationElement.setAttribute(PAPYRUS_ATTRIBUTE_GENERAL,
 				parentId);
 		
 		mapGeneralToParent.put(generalization.getId(), parentId);
 		mapParentToSource.put(generalization.getId(), classId);
 
 	}
 
 	
 	
 	/**
 	 * create association node
 	 * 
 	 * @param generalization
 	 * @param doc
 	 * @param rootElement
 	 */
 	private void createAssociation(XmiMergedAssociation mergedAssociation,
 			Document doc, Element rootElement) {
 
 		XmiAssociationElement association = mergedAssociation.getAssociationElement();
 		String class1Id = mergedAssociation.getClass1Id();
 		String class2Id = mergedAssociation.getClass2Id();
 		String class1Name = mergedAssociation.getClass1Name();
 		String class2Name = mergedAssociation.getClass2Name();
 		
 		ModelFileInfo diagram;
 		
 		if (mergedAssociation.getDiagramnum() == 1) {
 			diagram = comparer.getClassDiagram1().getNotationFile();
 		} else {
 			diagram = comparer.getClassDiagram2().getNotationFile();
 		}
 		
 		// Base element
 		Element associationElement = doc.createElement(PAPYRUS_PACKAGED_ELEM);
 		rootElement.appendChild(associationElement);
 
 		String source = null;
 		String target = null;
 		// Association only set up for non-classifier which has 2 member ends
 		if (association.getMemberEnds().size() == 2) {
 			appendOwnedEnd(association.getMemberEnds().get(0), doc, associationElement, class1Id, class1Name);
 			appendOwnedEnd(association.getMemberEnds().get(1), doc, associationElement, class2Id, class2Name);
 			
 			source = association.getMemberEnds().get(0).getId();
 			target = association.getMemberEnds().get(1).getId();
 		}
 		
 		associationElement.setAttribute(PAPYRUS_XMI_ATTRIBUTE_TYPE,
 				PAPYRUS_PACKAGE_TYPE_ASSOCIATION);
 		associationElement.setAttribute(PAPYRUS_ATTRIBUTE_ID, association.getId());
 		associationElement.setAttribute(PAPYRUS_ATTRIBUTE_NAME, association.getName());
 		
 		if (source != null && target != null) {
 			associationElement.setAttribute(PAPYRUS_MEMBER_END, source + " " + target);
 		}
 		
 		XmiNotationElement notationElement  = new XmiNotationElement(association.getId(),
 				association.getId(), diagram, XmiNotationElement.TYPE_ASSOCIATION);
 		
 		notationElement.setSource(class1Id);
 		notationElement.setTarget(class2Id);
 		
 		notationElements.addLast(notationElement);
 	}
 	
 	/**
 	 * Append OwnedEnd node
 	 * 
 	 * @param element
 	 * @param doc
 	 * @param rootElement
 	 * @param typeId of the class at the end of the association
 	 * @param name is referring to the end of the association
 	 */
 	private void appendOwnedEnd(XmiMemberEndElement element, Document doc,
 			Element rootElement, String typeId, String name) {
 
 		Element ownedEndElement = doc.createElement(PAPYRUS_MEMBER_END);
 		rootElement.appendChild(ownedEndElement);
 		
 		ownedEndElement.setAttribute(PAPYRUS_ATTRIBUTE_ID, element.getId());
 		ownedEndElement.setAttribute(PAPYRUS_ATTRIBUTE_TYPE,
 				typeId);
 		ownedEndElement.setAttribute(PAPYRUS_ATTRIBUTE_NAME,
 				name);
 		ownedEndElement.setAttribute(PAPYRUS_ATTRIBUTE_ASSOCIATION,
 				element.getAssociationId());
 		
 		if (element.getAggregation() != null) {
 			if (!element.getAggregation().toString().isEmpty()) {
 				ownedEndElement.setAttribute(PAPYRUS_AGGREGATION,
 						element.getAggregation().name().toLowerCase());
 			}
 		}
 		
 		if (element.getLowerValue() != null) {
 			appendLowerValue(element.getLowerValue(), doc, ownedEndElement);
 		}
 
 		if (element.getUpperValue() != null) {
 			appendUpperValue(element.getUpperValue(), doc, ownedEndElement);
 		}
 	}
 	
 	
 	public String getFileName() {
 		return this.fileName;
 	}
 	
 	public String getNotationFileName() {
 		return this.fileNotationName;
 	}
 	
 	public String getNotationFileId() {
 		return this.fileNotationId;
 	}
 	
 	public NotationData getNotationData() {
 		return this.notationData;
 	}
 	
 	/**
 	 * Generate the uml file
 	 * 
 	 * @param fileName
 	 */
 	public File GenerateFile(String fileName) {
 		try {
 			// write the content into xml file
 			TransformerFactory transformerFactory = TransformerFactory
 					.newInstance();
 			Transformer transformer = transformerFactory.newTransformer();
 			DOMSource source = new DOMSource(umlDoc);
 			File file = new File("C:\\temp\\" + fileName + ".uml");
 			StreamResult result = new StreamResult(file);
 			System.out.println("uml file created!");
 			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 			transformer.transform(source, result);
 			
 			return file;
 		} catch (TransformerException e) {
 			System.out.println("Failed uml file");
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
