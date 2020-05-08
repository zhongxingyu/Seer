 /**
  */
 package org.eclipse.emf.diffmerge.tests.elements.Elements.impl;
 
 import org.eclipse.emf.diffmerge.tests.elements.Elements.*;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 
 import org.eclipse.emf.ecore.impl.EFactoryImpl;
 
 import org.eclipse.emf.ecore.plugin.EcorePlugin;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model <b>Factory</b>.
  * <!-- end-user-doc -->
  * @generated
  */
 public class ElementsFactoryImpl extends EFactoryImpl implements ElementsFactory {
 	/**
 	 * Creates the default factory implementation.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static ElementsFactory init() {
 		try {
			ElementsFactory theElementsFactory = (ElementsFactory)EPackage.Registry.INSTANCE.getEFactory("http://www.eclipse.org/emf/diffmerge/tests/elements/1.0.0"); 
 			if (theElementsFactory != null) {
 				return theElementsFactory;
 			}
 		}
 		catch (Exception exception) {
 			EcorePlugin.INSTANCE.log(exception);
 		}
 		return new ElementsFactoryImpl();
 	}
 
 	/**
 	 * Creates an instance of the factory.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ElementsFactoryImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public EObject create(EClass eClass) {
 		switch (eClass.getClassifierID()) {
 			case ElementsPackage.ROOT: return createRoot();
 			case ElementsPackage.ELEMENT: return createElement();
 			case ElementsPackage.STRICT_ELEMENT: return createStrictElement();
 			case ElementsPackage.NODE: return createNode();
 			case ElementsPackage.EDGE: return createEdge();
 			case ElementsPackage.REFERENCING_NODE: return createReferencingNode();
 			default:
 				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Root createRoot() {
 		RootImpl root = new RootImpl();
 		return root;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Element createElement() {
 		ElementImpl element = new ElementImpl();
 		return element;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public StrictElement createStrictElement() {
 		StrictElementImpl strictElement = new StrictElementImpl();
 		return strictElement;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Node createNode() {
 		NodeImpl node = new NodeImpl();
 		return node;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Edge createEdge() {
 		EdgeImpl edge = new EdgeImpl();
 		return edge;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ReferencingNode createReferencingNode() {
 		ReferencingNodeImpl referencingNode = new ReferencingNodeImpl();
 		return referencingNode;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ElementsPackage getElementsPackage() {
 		return (ElementsPackage)getEPackage();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @deprecated
 	 * @generated
 	 */
 	@Deprecated
 	public static ElementsPackage getPackage() {
 		return ElementsPackage.eINSTANCE;
 	}
 
 } //ElementsFactoryImpl
