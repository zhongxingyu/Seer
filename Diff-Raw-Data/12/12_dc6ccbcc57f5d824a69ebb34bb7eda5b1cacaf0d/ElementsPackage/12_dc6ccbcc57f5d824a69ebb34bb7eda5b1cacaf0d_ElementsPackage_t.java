 /**
  */
 package org.eclipse.emf.diffmerge.tests.elements.Elements;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Package</b> for the model.
  * It contains accessors for the meta objects to represent
  * <ul>
  *   <li>each class,</li>
  *   <li>each feature of each class,</li>
  *   <li>each enum,</li>
  *   <li>and each data type</li>
  * </ul>
  * <!-- end-user-doc -->
  * @see org.eclipse.emf.diffmerge.tests.elements.Elements.ElementsFactory
  * @model kind="package"
  * @generated
  */
 public interface ElementsPackage extends EPackage {
 	/**
 	 * The package name.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	String eNAME = "Elements";
 
 	/**
 	 * The package namespace URI.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
	String eNS_URI = "http://www.eclipse.org/emf/diffmerge/tests/elements/1.0.0";
 
 	/**
 	 * The package namespace name.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
	String eNS_PREFIX = "org.eclipse.emf.diffmerge.tests.elements";
 
 	/**
 	 * The singleton instance of the package.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	ElementsPackage eINSTANCE = org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementsPackageImpl.init();
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.impl.IdentifiedElementImpl <em>Identified Element</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.IdentifiedElementImpl
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementsPackageImpl#getIdentifiedElement()
 	 * @generated
 	 */
 	int IDENTIFIED_ELEMENT = 0;
 
 	/**
 	 * The feature id for the '<em><b>Id</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int IDENTIFIED_ELEMENT__ID = 0;
 
 	/**
 	 * The number of structural features of the '<em>Identified Element</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int IDENTIFIED_ELEMENT_FEATURE_COUNT = 1;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.impl.NamedElementImpl <em>Named Element</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.NamedElementImpl
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementsPackageImpl#getNamedElement()
 	 * @generated
 	 */
 	int NAMED_ELEMENT = 1;
 
 	/**
 	 * The feature id for the '<em><b>Id</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int NAMED_ELEMENT__ID = IDENTIFIED_ELEMENT__ID;
 
 	/**
 	 * The feature id for the '<em><b>Name</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int NAMED_ELEMENT__NAME = IDENTIFIED_ELEMENT_FEATURE_COUNT + 0;
 
 	/**
 	 * The number of structural features of the '<em>Named Element</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int NAMED_ELEMENT_FEATURE_COUNT = IDENTIFIED_ELEMENT_FEATURE_COUNT + 1;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.impl.RootImpl <em>Root</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.RootImpl
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementsPackageImpl#getRoot()
 	 * @generated
 	 */
 	int ROOT = 2;
 
 	/**
 	 * The feature id for the '<em><b>Id</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ROOT__ID = IDENTIFIED_ELEMENT__ID;
 
 	/**
 	 * The feature id for the '<em><b>Name</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ROOT__NAME = IDENTIFIED_ELEMENT_FEATURE_COUNT + 0;
 
 	/**
 	 * The feature id for the '<em><b>Content</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ROOT__CONTENT = IDENTIFIED_ELEMENT_FEATURE_COUNT + 1;
 
 	/**
 	 * The number of structural features of the '<em>Root</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ROOT_FEATURE_COUNT = IDENTIFIED_ELEMENT_FEATURE_COUNT + 2;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementImpl <em>Element</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementImpl
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementsPackageImpl#getElement()
 	 * @generated
 	 */
 	int ELEMENT = 3;
 
 	/**
 	 * The feature id for the '<em><b>Id</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ELEMENT__ID = NAMED_ELEMENT__ID;
 
 	/**
 	 * The feature id for the '<em><b>Name</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ELEMENT__NAME = NAMED_ELEMENT__NAME;
 
 	/**
 	 * The feature id for the '<em><b>Value</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ELEMENT__VALUE = NAMED_ELEMENT_FEATURE_COUNT + 0;
 
 	/**
 	 * The feature id for the '<em><b>Values</b></em>' attribute list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ELEMENT__VALUES = NAMED_ELEMENT_FEATURE_COUNT + 1;
 
 	/**
 	 * The feature id for the '<em><b>Many Content</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ELEMENT__MANY_CONTENT = NAMED_ELEMENT_FEATURE_COUNT + 2;
 
 	/**
 	 * The feature id for the '<em><b>Single Content</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ELEMENT__SINGLE_CONTENT = NAMED_ELEMENT_FEATURE_COUNT + 3;
 
 	/**
 	 * The feature id for the '<em><b>Many Ref</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ELEMENT__MANY_REF = NAMED_ELEMENT_FEATURE_COUNT + 4;
 
 	/**
 	 * The feature id for the '<em><b>Single Ref</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ELEMENT__SINGLE_REF = NAMED_ELEMENT_FEATURE_COUNT + 5;
 
 	/**
 	 * The feature id for the '<em><b>Many From Single Ref</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ELEMENT__MANY_FROM_SINGLE_REF = NAMED_ELEMENT_FEATURE_COUNT + 6;
 
 	/**
 	 * The feature id for the '<em><b>Single From Many Ref</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ELEMENT__SINGLE_FROM_MANY_REF = NAMED_ELEMENT_FEATURE_COUNT + 7;
 
 	/**
 	 * The feature id for the '<em><b>Many From Many Ref1</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ELEMENT__MANY_FROM_MANY_REF1 = NAMED_ELEMENT_FEATURE_COUNT + 8;
 
 	/**
 	 * The feature id for the '<em><b>Many From Many Ref2</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ELEMENT__MANY_FROM_MANY_REF2 = NAMED_ELEMENT_FEATURE_COUNT + 9;
 
 	/**
 	 * The number of structural features of the '<em>Element</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ELEMENT_FEATURE_COUNT = NAMED_ELEMENT_FEATURE_COUNT + 10;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.impl.StrictElementImpl <em>Strict Element</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.StrictElementImpl
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementsPackageImpl#getStrictElement()
 	 * @generated
 	 */
 	int STRICT_ELEMENT = 4;
 
 	/**
 	 * The feature id for the '<em><b>Id</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__ID = ELEMENT__ID;
 
 	/**
 	 * The feature id for the '<em><b>Name</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__NAME = ELEMENT__NAME;
 
 	/**
 	 * The feature id for the '<em><b>Value</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__VALUE = ELEMENT__VALUE;
 
 	/**
 	 * The feature id for the '<em><b>Values</b></em>' attribute list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__VALUES = ELEMENT__VALUES;
 
 	/**
 	 * The feature id for the '<em><b>Many Content</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__MANY_CONTENT = ELEMENT__MANY_CONTENT;
 
 	/**
 	 * The feature id for the '<em><b>Single Content</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__SINGLE_CONTENT = ELEMENT__SINGLE_CONTENT;
 
 	/**
 	 * The feature id for the '<em><b>Many Ref</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__MANY_REF = ELEMENT__MANY_REF;
 
 	/**
 	 * The feature id for the '<em><b>Single Ref</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__SINGLE_REF = ELEMENT__SINGLE_REF;
 
 	/**
 	 * The feature id for the '<em><b>Many From Single Ref</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__MANY_FROM_SINGLE_REF = ELEMENT__MANY_FROM_SINGLE_REF;
 
 	/**
 	 * The feature id for the '<em><b>Single From Many Ref</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__SINGLE_FROM_MANY_REF = ELEMENT__SINGLE_FROM_MANY_REF;
 
 	/**
 	 * The feature id for the '<em><b>Many From Many Ref1</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__MANY_FROM_MANY_REF1 = ELEMENT__MANY_FROM_MANY_REF1;
 
 	/**
 	 * The feature id for the '<em><b>Many From Many Ref2</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__MANY_FROM_MANY_REF2 = ELEMENT__MANY_FROM_MANY_REF2;
 
 	/**
 	 * The feature id for the '<em><b>SValue</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__SVALUE = ELEMENT_FEATURE_COUNT + 0;
 
 	/**
 	 * The feature id for the '<em><b>SValues</b></em>' attribute list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__SVALUES = ELEMENT_FEATURE_COUNT + 1;
 
 	/**
 	 * The feature id for the '<em><b>SMany Content</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__SMANY_CONTENT = ELEMENT_FEATURE_COUNT + 2;
 
 	/**
 	 * The feature id for the '<em><b>SSingle Content</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__SSINGLE_CONTENT = ELEMENT_FEATURE_COUNT + 3;
 
 	/**
 	 * The feature id for the '<em><b>SMany Ref</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__SMANY_REF = ELEMENT_FEATURE_COUNT + 4;
 
 	/**
 	 * The feature id for the '<em><b>SSingle Ref</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__SSINGLE_REF = ELEMENT_FEATURE_COUNT + 5;
 
 	/**
 	 * The feature id for the '<em><b>SMany From Single Ref</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__SMANY_FROM_SINGLE_REF = ELEMENT_FEATURE_COUNT + 6;
 
 	/**
 	 * The feature id for the '<em><b>SSingle From Many Ref</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__SSINGLE_FROM_MANY_REF = ELEMENT_FEATURE_COUNT + 7;
 
 	/**
 	 * The feature id for the '<em><b>SMany From Many Ref1</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__SMANY_FROM_MANY_REF1 = ELEMENT_FEATURE_COUNT + 8;
 
 	/**
 	 * The feature id for the '<em><b>SMany From Many Ref2</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT__SMANY_FROM_MANY_REF2 = ELEMENT_FEATURE_COUNT + 9;
 
 	/**
 	 * The number of structural features of the '<em>Strict Element</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int STRICT_ELEMENT_FEATURE_COUNT = ELEMENT_FEATURE_COUNT + 10;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.impl.NodeImpl <em>Node</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.NodeImpl
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementsPackageImpl#getNode()
 	 * @generated
 	 */
 	int NODE = 5;
 
 	/**
 	 * The feature id for the '<em><b>Id</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int NODE__ID = NAMED_ELEMENT__ID;
 
 	/**
 	 * The feature id for the '<em><b>Name</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int NODE__NAME = NAMED_ELEMENT__NAME;
 
 	/**
 	 * The feature id for the '<em><b>Incoming</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int NODE__INCOMING = NAMED_ELEMENT_FEATURE_COUNT + 0;
 
 	/**
 	 * The feature id for the '<em><b>Outgoing</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int NODE__OUTGOING = NAMED_ELEMENT_FEATURE_COUNT + 1;
 
 	/**
 	 * The feature id for the '<em><b>Sub Nodes</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int NODE__SUB_NODES = NAMED_ELEMENT_FEATURE_COUNT + 2;
 
 	/**
 	 * The number of structural features of the '<em>Node</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int NODE_FEATURE_COUNT = NAMED_ELEMENT_FEATURE_COUNT + 3;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.impl.EdgeImpl <em>Edge</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.EdgeImpl
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementsPackageImpl#getEdge()
 	 * @generated
 	 */
 	int EDGE = 6;
 
 	/**
 	 * The feature id for the '<em><b>Id</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int EDGE__ID = NAMED_ELEMENT__ID;
 
 	/**
 	 * The feature id for the '<em><b>Name</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int EDGE__NAME = NAMED_ELEMENT__NAME;
 
 	/**
 	 * The feature id for the '<em><b>Target</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int EDGE__TARGET = NAMED_ELEMENT_FEATURE_COUNT + 0;
 
 	/**
 	 * The feature id for the '<em><b>Source</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int EDGE__SOURCE = NAMED_ELEMENT_FEATURE_COUNT + 1;
 
 	/**
 	 * The number of structural features of the '<em>Edge</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int EDGE_FEATURE_COUNT = NAMED_ELEMENT_FEATURE_COUNT + 2;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ReferencingNodeImpl <em>Referencing Node</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ReferencingNodeImpl
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementsPackageImpl#getReferencingNode()
 	 * @generated
 	 */
 	int REFERENCING_NODE = 7;
 
 	/**
 	 * The feature id for the '<em><b>Id</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int REFERENCING_NODE__ID = NODE__ID;
 
 	/**
 	 * The feature id for the '<em><b>Name</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int REFERENCING_NODE__NAME = NODE__NAME;
 
 	/**
 	 * The feature id for the '<em><b>Incoming</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int REFERENCING_NODE__INCOMING = NODE__INCOMING;
 
 	/**
 	 * The feature id for the '<em><b>Outgoing</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int REFERENCING_NODE__OUTGOING = NODE__OUTGOING;
 
 	/**
 	 * The feature id for the '<em><b>Sub Nodes</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int REFERENCING_NODE__SUB_NODES = NODE__SUB_NODES;
 
 	/**
 	 * The feature id for the '<em><b>Referenced</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int REFERENCING_NODE__REFERENCED = NODE_FEATURE_COUNT + 0;
 
 	/**
 	 * The number of structural features of the '<em>Referencing Node</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int REFERENCING_NODE_FEATURE_COUNT = NODE_FEATURE_COUNT + 1;
 
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.IdentifiedElement <em>Identified Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Identified Element</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.IdentifiedElement
 	 * @generated
 	 */
 	EClass getIdentifiedElement();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.IdentifiedElement#getId <em>Id</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Id</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.IdentifiedElement#getId()
 	 * @see #getIdentifiedElement()
 	 * @generated
 	 */
 	EAttribute getIdentifiedElement_Id();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.NamedElement <em>Named Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Named Element</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.NamedElement
 	 * @generated
 	 */
 	EClass getNamedElement();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.NamedElement#getName <em>Name</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Name</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.NamedElement#getName()
 	 * @see #getNamedElement()
 	 * @generated
 	 */
 	EAttribute getNamedElement_Name();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Root <em>Root</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Root</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Root
 	 * @generated
 	 */
 	EClass getRoot();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Root#getName <em>Name</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Name</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Root#getName()
 	 * @see #getRoot()
 	 * @generated
 	 */
 	EAttribute getRoot_Name();
 
 	/**
 	 * Returns the meta object for the containment reference list '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Root#getContent <em>Content</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference list '<em>Content</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Root#getContent()
 	 * @see #getRoot()
 	 * @generated
 	 */
 	EReference getRoot_Content();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Element <em>Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Element</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Element
 	 * @generated
 	 */
 	EClass getElement();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getValue <em>Value</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Value</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getValue()
 	 * @see #getElement()
 	 * @generated
 	 */
 	EAttribute getElement_Value();
 
 	/**
 	 * Returns the meta object for the attribute list '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getValues <em>Values</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute list '<em>Values</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getValues()
 	 * @see #getElement()
 	 * @generated
 	 */
 	EAttribute getElement_Values();
 
 	/**
 	 * Returns the meta object for the containment reference list '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getManyContent <em>Many Content</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference list '<em>Many Content</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getManyContent()
 	 * @see #getElement()
 	 * @generated
 	 */
 	EReference getElement_ManyContent();
 
 	/**
 	 * Returns the meta object for the containment reference '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getSingleContent <em>Single Content</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference '<em>Single Content</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getSingleContent()
 	 * @see #getElement()
 	 * @generated
 	 */
 	EReference getElement_SingleContent();
 
 	/**
 	 * Returns the meta object for the reference list '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getManyRef <em>Many Ref</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference list '<em>Many Ref</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getManyRef()
 	 * @see #getElement()
 	 * @generated
 	 */
 	EReference getElement_ManyRef();
 
 	/**
 	 * Returns the meta object for the reference '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getSingleRef <em>Single Ref</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference '<em>Single Ref</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getSingleRef()
 	 * @see #getElement()
 	 * @generated
 	 */
 	EReference getElement_SingleRef();
 
 	/**
 	 * Returns the meta object for the reference list '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getManyFromSingleRef <em>Many From Single Ref</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference list '<em>Many From Single Ref</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getManyFromSingleRef()
 	 * @see #getElement()
 	 * @generated
 	 */
 	EReference getElement_ManyFromSingleRef();
 
 	/**
 	 * Returns the meta object for the reference '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getSingleFromManyRef <em>Single From Many Ref</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference '<em>Single From Many Ref</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getSingleFromManyRef()
 	 * @see #getElement()
 	 * @generated
 	 */
 	EReference getElement_SingleFromManyRef();
 
 	/**
 	 * Returns the meta object for the reference list '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getManyFromManyRef1 <em>Many From Many Ref1</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference list '<em>Many From Many Ref1</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getManyFromManyRef1()
 	 * @see #getElement()
 	 * @generated
 	 */
 	EReference getElement_ManyFromManyRef1();
 
 	/**
 	 * Returns the meta object for the reference list '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getManyFromManyRef2 <em>Many From Many Ref2</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference list '<em>Many From Many Ref2</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Element#getManyFromManyRef2()
 	 * @see #getElement()
 	 * @generated
 	 */
 	EReference getElement_ManyFromManyRef2();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement <em>Strict Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Strict Element</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement
 	 * @generated
 	 */
 	EClass getStrictElement();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSValue <em>SValue</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>SValue</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSValue()
 	 * @see #getStrictElement()
 	 * @generated
 	 */
 	EAttribute getStrictElement_SValue();
 
 	/**
 	 * Returns the meta object for the attribute list '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSValues <em>SValues</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute list '<em>SValues</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSValues()
 	 * @see #getStrictElement()
 	 * @generated
 	 */
 	EAttribute getStrictElement_SValues();
 
 	/**
 	 * Returns the meta object for the containment reference list '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSManyContent <em>SMany Content</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference list '<em>SMany Content</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSManyContent()
 	 * @see #getStrictElement()
 	 * @generated
 	 */
 	EReference getStrictElement_SManyContent();
 
 	/**
 	 * Returns the meta object for the containment reference '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSSingleContent <em>SSingle Content</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference '<em>SSingle Content</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSSingleContent()
 	 * @see #getStrictElement()
 	 * @generated
 	 */
 	EReference getStrictElement_SSingleContent();
 
 	/**
 	 * Returns the meta object for the reference list '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSManyRef <em>SMany Ref</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference list '<em>SMany Ref</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSManyRef()
 	 * @see #getStrictElement()
 	 * @generated
 	 */
 	EReference getStrictElement_SManyRef();
 
 	/**
 	 * Returns the meta object for the reference '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSSingleRef <em>SSingle Ref</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference '<em>SSingle Ref</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSSingleRef()
 	 * @see #getStrictElement()
 	 * @generated
 	 */
 	EReference getStrictElement_SSingleRef();
 
 	/**
 	 * Returns the meta object for the reference list '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSManyFromSingleRef <em>SMany From Single Ref</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference list '<em>SMany From Single Ref</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSManyFromSingleRef()
 	 * @see #getStrictElement()
 	 * @generated
 	 */
 	EReference getStrictElement_SManyFromSingleRef();
 
 	/**
 	 * Returns the meta object for the reference '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSSingleFromManyRef <em>SSingle From Many Ref</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference '<em>SSingle From Many Ref</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSSingleFromManyRef()
 	 * @see #getStrictElement()
 	 * @generated
 	 */
 	EReference getStrictElement_SSingleFromManyRef();
 
 	/**
 	 * Returns the meta object for the reference list '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSManyFromManyRef1 <em>SMany From Many Ref1</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference list '<em>SMany From Many Ref1</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSManyFromManyRef1()
 	 * @see #getStrictElement()
 	 * @generated
 	 */
 	EReference getStrictElement_SManyFromManyRef1();
 
 	/**
 	 * Returns the meta object for the reference list '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSManyFromManyRef2 <em>SMany From Many Ref2</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference list '<em>SMany From Many Ref2</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.StrictElement#getSManyFromManyRef2()
 	 * @see #getStrictElement()
 	 * @generated
 	 */
 	EReference getStrictElement_SManyFromManyRef2();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Node <em>Node</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Node</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Node
 	 * @generated
 	 */
 	EClass getNode();
 
 	/**
 	 * Returns the meta object for the reference list '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Node#getIncoming <em>Incoming</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference list '<em>Incoming</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Node#getIncoming()
 	 * @see #getNode()
 	 * @generated
 	 */
 	EReference getNode_Incoming();
 
 	/**
 	 * Returns the meta object for the reference list '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Node#getOutgoing <em>Outgoing</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference list '<em>Outgoing</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Node#getOutgoing()
 	 * @see #getNode()
 	 * @generated
 	 */
 	EReference getNode_Outgoing();
 
 	/**
 	 * Returns the meta object for the containment reference list '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Node#getSubNodes <em>Sub Nodes</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference list '<em>Sub Nodes</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Node#getSubNodes()
 	 * @see #getNode()
 	 * @generated
 	 */
 	EReference getNode_SubNodes();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Edge <em>Edge</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Edge</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Edge
 	 * @generated
 	 */
 	EClass getEdge();
 
 	/**
 	 * Returns the meta object for the reference '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Edge#getTarget <em>Target</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference '<em>Target</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Edge#getTarget()
 	 * @see #getEdge()
 	 * @generated
 	 */
 	EReference getEdge_Target();
 
 	/**
 	 * Returns the meta object for the reference '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.Edge#getSource <em>Source</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference '<em>Source</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.Edge#getSource()
 	 * @see #getEdge()
 	 * @generated
 	 */
 	EReference getEdge_Source();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.ReferencingNode <em>Referencing Node</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Referencing Node</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.ReferencingNode
 	 * @generated
 	 */
 	EClass getReferencingNode();
 
 	/**
 	 * Returns the meta object for the reference '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.ReferencingNode#getReferenced <em>Referenced</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference '<em>Referenced</em>'.
 	 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.ReferencingNode#getReferenced()
 	 * @see #getReferencingNode()
 	 * @generated
 	 */
 	EReference getReferencingNode_Referenced();
 
 	/**
 	 * Returns the factory that creates the instances of the model.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the factory that creates the instances of the model.
 	 * @generated
 	 */
 	ElementsFactory getElementsFactory();
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Defines literals for the meta objects that represent
 	 * <ul>
 	 *   <li>each class,</li>
 	 *   <li>each feature of each class,</li>
 	 *   <li>each enum,</li>
 	 *   <li>and each data type</li>
 	 * </ul>
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	interface Literals {
 		/**
 		 * The meta object literal for the '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.impl.IdentifiedElementImpl <em>Identified Element</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.IdentifiedElementImpl
 		 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementsPackageImpl#getIdentifiedElement()
 		 * @generated
 		 */
 		EClass IDENTIFIED_ELEMENT = eINSTANCE.getIdentifiedElement();
 
 		/**
 		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute IDENTIFIED_ELEMENT__ID = eINSTANCE.getIdentifiedElement_Id();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.impl.NamedElementImpl <em>Named Element</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.NamedElementImpl
 		 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementsPackageImpl#getNamedElement()
 		 * @generated
 		 */
 		EClass NAMED_ELEMENT = eINSTANCE.getNamedElement();
 
 		/**
 		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute NAMED_ELEMENT__NAME = eINSTANCE.getNamedElement_Name();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.impl.RootImpl <em>Root</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.RootImpl
 		 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementsPackageImpl#getRoot()
 		 * @generated
 		 */
 		EClass ROOT = eINSTANCE.getRoot();
 
 		/**
 		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute ROOT__NAME = eINSTANCE.getRoot_Name();
 
 		/**
 		 * The meta object literal for the '<em><b>Content</b></em>' containment reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference ROOT__CONTENT = eINSTANCE.getRoot_Content();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementImpl <em>Element</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementImpl
 		 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementsPackageImpl#getElement()
 		 * @generated
 		 */
 		EClass ELEMENT = eINSTANCE.getElement();
 
 		/**
 		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute ELEMENT__VALUE = eINSTANCE.getElement_Value();
 
 		/**
 		 * The meta object literal for the '<em><b>Values</b></em>' attribute list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute ELEMENT__VALUES = eINSTANCE.getElement_Values();
 
 		/**
 		 * The meta object literal for the '<em><b>Many Content</b></em>' containment reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference ELEMENT__MANY_CONTENT = eINSTANCE.getElement_ManyContent();
 
 		/**
 		 * The meta object literal for the '<em><b>Single Content</b></em>' containment reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference ELEMENT__SINGLE_CONTENT = eINSTANCE.getElement_SingleContent();
 
 		/**
 		 * The meta object literal for the '<em><b>Many Ref</b></em>' reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference ELEMENT__MANY_REF = eINSTANCE.getElement_ManyRef();
 
 		/**
 		 * The meta object literal for the '<em><b>Single Ref</b></em>' reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference ELEMENT__SINGLE_REF = eINSTANCE.getElement_SingleRef();
 
 		/**
 		 * The meta object literal for the '<em><b>Many From Single Ref</b></em>' reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference ELEMENT__MANY_FROM_SINGLE_REF = eINSTANCE.getElement_ManyFromSingleRef();
 
 		/**
 		 * The meta object literal for the '<em><b>Single From Many Ref</b></em>' reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference ELEMENT__SINGLE_FROM_MANY_REF = eINSTANCE.getElement_SingleFromManyRef();
 
 		/**
 		 * The meta object literal for the '<em><b>Many From Many Ref1</b></em>' reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference ELEMENT__MANY_FROM_MANY_REF1 = eINSTANCE.getElement_ManyFromManyRef1();
 
 		/**
 		 * The meta object literal for the '<em><b>Many From Many Ref2</b></em>' reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference ELEMENT__MANY_FROM_MANY_REF2 = eINSTANCE.getElement_ManyFromManyRef2();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.impl.StrictElementImpl <em>Strict Element</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.StrictElementImpl
 		 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementsPackageImpl#getStrictElement()
 		 * @generated
 		 */
 		EClass STRICT_ELEMENT = eINSTANCE.getStrictElement();
 
 		/**
 		 * The meta object literal for the '<em><b>SValue</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute STRICT_ELEMENT__SVALUE = eINSTANCE.getStrictElement_SValue();
 
 		/**
 		 * The meta object literal for the '<em><b>SValues</b></em>' attribute list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute STRICT_ELEMENT__SVALUES = eINSTANCE.getStrictElement_SValues();
 
 		/**
 		 * The meta object literal for the '<em><b>SMany Content</b></em>' containment reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference STRICT_ELEMENT__SMANY_CONTENT = eINSTANCE.getStrictElement_SManyContent();
 
 		/**
 		 * The meta object literal for the '<em><b>SSingle Content</b></em>' containment reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference STRICT_ELEMENT__SSINGLE_CONTENT = eINSTANCE.getStrictElement_SSingleContent();
 
 		/**
 		 * The meta object literal for the '<em><b>SMany Ref</b></em>' reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference STRICT_ELEMENT__SMANY_REF = eINSTANCE.getStrictElement_SManyRef();
 
 		/**
 		 * The meta object literal for the '<em><b>SSingle Ref</b></em>' reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference STRICT_ELEMENT__SSINGLE_REF = eINSTANCE.getStrictElement_SSingleRef();
 
 		/**
 		 * The meta object literal for the '<em><b>SMany From Single Ref</b></em>' reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference STRICT_ELEMENT__SMANY_FROM_SINGLE_REF = eINSTANCE.getStrictElement_SManyFromSingleRef();
 
 		/**
 		 * The meta object literal for the '<em><b>SSingle From Many Ref</b></em>' reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference STRICT_ELEMENT__SSINGLE_FROM_MANY_REF = eINSTANCE.getStrictElement_SSingleFromManyRef();
 
 		/**
 		 * The meta object literal for the '<em><b>SMany From Many Ref1</b></em>' reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference STRICT_ELEMENT__SMANY_FROM_MANY_REF1 = eINSTANCE.getStrictElement_SManyFromManyRef1();
 
 		/**
 		 * The meta object literal for the '<em><b>SMany From Many Ref2</b></em>' reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference STRICT_ELEMENT__SMANY_FROM_MANY_REF2 = eINSTANCE.getStrictElement_SManyFromManyRef2();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.impl.NodeImpl <em>Node</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.NodeImpl
 		 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementsPackageImpl#getNode()
 		 * @generated
 		 */
 		EClass NODE = eINSTANCE.getNode();
 
 		/**
 		 * The meta object literal for the '<em><b>Incoming</b></em>' reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference NODE__INCOMING = eINSTANCE.getNode_Incoming();
 
 		/**
 		 * The meta object literal for the '<em><b>Outgoing</b></em>' reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference NODE__OUTGOING = eINSTANCE.getNode_Outgoing();
 
 		/**
 		 * The meta object literal for the '<em><b>Sub Nodes</b></em>' containment reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference NODE__SUB_NODES = eINSTANCE.getNode_SubNodes();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.impl.EdgeImpl <em>Edge</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.EdgeImpl
 		 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementsPackageImpl#getEdge()
 		 * @generated
 		 */
 		EClass EDGE = eINSTANCE.getEdge();
 
 		/**
 		 * The meta object literal for the '<em><b>Target</b></em>' reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference EDGE__TARGET = eINSTANCE.getEdge_Target();
 
 		/**
 		 * The meta object literal for the '<em><b>Source</b></em>' reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference EDGE__SOURCE = eINSTANCE.getEdge_Source();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ReferencingNodeImpl <em>Referencing Node</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ReferencingNodeImpl
 		 * @see org.eclipse.emf.diffmerge.tests.elements.Elements.impl.ElementsPackageImpl#getReferencingNode()
 		 * @generated
 		 */
 		EClass REFERENCING_NODE = eINSTANCE.getReferencingNode();
 
 		/**
 		 * The meta object literal for the '<em><b>Referenced</b></em>' reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference REFERENCING_NODE__REFERENCED = eINSTANCE.getReferencingNode_Referenced();
 
 	}
 
 } //ElementsPackage
