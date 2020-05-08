 /**
  */
 package kompren;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.ENamedElement;
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * <!-- begin-user-doc -->
  * A representation of the model object '<em><b>Slicer</b></em>'.
  * <!-- end-user-doc -->
  *
  * <p>
  * The following features are supported:
  * <ul>
  *   <li>{@link kompren.Slicer#getName <em>Name</em>}</li>
  *   <li>{@link kompren.Slicer#getUriMetamodel <em>Uri Metamodel</em>}</li>
  *   <li>{@link kompren.Slicer#isActive <em>Active</em>}</li>
  *   <li>{@link kompren.Slicer#isStrict <em>Strict</em>}</li>
  *   <li>{@link kompren.Slicer#getHelper <em>Helper</em>}</li>
  *   <li>{@link kompren.Slicer#getOnStart <em>On Start</em>}</li>
  *   <li>{@link kompren.Slicer#getOnEnd <em>On End</em>}</li>
  *   <li>{@link kompren.Slicer#getRadius <em>Radius</em>}</li>
  *   <li>{@link kompren.Slicer#getSlicedElements <em>Sliced Elements</em>}</li>
  *   <li>{@link kompren.Slicer#getInputClasses <em>Input Classes</em>}</li>
  *   <li>{@link kompren.Slicer#getLogVisitClass <em>Log Visit Class</em>}</li>
  *   <li>{@link kompren.Slicer#getLogVisitRelation <em>Log Visit Relation</em>}</li>
  * </ul>
  * </p>
  *
  * @see kompren.KomprenPackage#getSlicer()
  * @model
  * @generated
  */
 public interface Slicer extends EObject {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	String copyright = "Inria/IRISA Diverse Team";
 
 	/**
 	 * Returns the value of the '<em><b>Name</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Name</em>' attribute.
 	 * @see #setName(String)
 	 * @see kompren.KomprenPackage#getSlicer_Name()
 	 * @model required="true"
 	 * @generated
 	 */
 	String getName();
 
 	/**
 	 * Sets the value of the '{@link kompren.Slicer#getName <em>Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Name</em>' attribute.
 	 * @see #getName()
 	 * @generated
 	 */
 	void setName(String value);
 
 	/**
 	 * Returns the value of the '<em><b>Uri Metamodel</b></em>' attribute list.
 	 * The list contents are of type {@link java.lang.String}.
 	 * <!-- begin-user-doc -->
 	 * <p>
	 * If the meaning of the '<em>Uri Metamodel</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Uri Metamodel</em>' attribute list.
 	 * @see kompren.KomprenPackage#getSlicer_UriMetamodel()
 	 * @model required="true"
 	 * @generated
 	 */
 	EList<String> getUriMetamodel();
 
 	/**
 	 * Returns the value of the '<em><b>Active</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Active</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Active</em>' attribute.
 	 * @see #setActive(boolean)
 	 * @see kompren.KomprenPackage#getSlicer_Active()
 	 * @model
 	 * @generated
 	 */
 	boolean isActive();
 
 	/**
 	 * Sets the value of the '{@link kompren.Slicer#isActive <em>Active</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Active</em>' attribute.
 	 * @see #isActive()
 	 * @generated
 	 */
 	void setActive(boolean value);
 
 	/**
 	 * Returns the value of the '<em><b>Strict</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Strict</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Strict</em>' attribute.
 	 * @see #setStrict(boolean)
 	 * @see kompren.KomprenPackage#getSlicer_Strict()
 	 * @model
 	 * @generated
 	 */
 	boolean isStrict();
 
 	/**
 	 * Sets the value of the '{@link kompren.Slicer#isStrict <em>Strict</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Strict</em>' attribute.
 	 * @see #isStrict()
 	 * @generated
 	 */
 	void setStrict(boolean value);
 
 	/**
 	 * Returns the value of the '<em><b>Helper</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Helper</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Helper</em>' attribute.
 	 * @see #setHelper(String)
 	 * @see kompren.KomprenPackage#getSlicer_Helper()
 	 * @model
 	 * @generated
 	 */
 	String getHelper();
 
 	/**
 	 * Sets the value of the '{@link kompren.Slicer#getHelper <em>Helper</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Helper</em>' attribute.
 	 * @see #getHelper()
 	 * @generated
 	 */
 	void setHelper(String value);
 
 	/**
 	 * Returns the value of the '<em><b>On Start</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>On Start</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>On Start</em>' attribute.
 	 * @see #setOnStart(String)
 	 * @see kompren.KomprenPackage#getSlicer_OnStart()
 	 * @model
 	 * @generated
 	 */
 	String getOnStart();
 
 	/**
 	 * Sets the value of the '{@link kompren.Slicer#getOnStart <em>On Start</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>On Start</em>' attribute.
 	 * @see #getOnStart()
 	 * @generated
 	 */
 	void setOnStart(String value);
 
 	/**
 	 * Returns the value of the '<em><b>On End</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>On End</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>On End</em>' attribute.
 	 * @see #setOnEnd(String)
 	 * @see kompren.KomprenPackage#getSlicer_OnEnd()
 	 * @model
 	 * @generated
 	 */
 	String getOnEnd();
 
 	/**
 	 * Sets the value of the '{@link kompren.Slicer#getOnEnd <em>On End</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>On End</em>' attribute.
 	 * @see #getOnEnd()
 	 * @generated
 	 */
 	void setOnEnd(String value);
 
 	/**
 	 * Returns the value of the '<em><b>Radius</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Radius</em>' containment reference isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Radius</em>' containment reference.
 	 * @see #setRadius(Radius)
 	 * @see kompren.KomprenPackage#getSlicer_Radius()
 	 * @model containment="true"
 	 * @generated
 	 */
 	Radius getRadius();
 
 	/**
 	 * Sets the value of the '{@link kompren.Slicer#getRadius <em>Radius</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Radius</em>' containment reference.
 	 * @see #getRadius()
 	 * @generated
 	 */
 	void setRadius(Radius value);
 
 	/**
 	 * Returns the value of the '<em><b>Sliced Elements</b></em>' containment reference list.
 	 * The list contents are of type {@link kompren.SlicedElement}&lt;?>.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Sliced Elements</em>' containment reference list isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Sliced Elements</em>' containment reference list.
 	 * @see kompren.KomprenPackage#getSlicer_SlicedElements()
 	 * @model containment="true"
 	 */
 	EList<SlicedElement<? extends ENamedElement>> getSlicedElements();
 
 	/**
 	 * Returns the value of the '<em><b>Input Classes</b></em>' reference list.
 	 * The list contents are of type {@link org.eclipse.emf.ecore.EClass}.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Input Classes</em>' reference list isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Input Classes</em>' reference list.
 	 * @see kompren.KomprenPackage#getSlicer_InputClasses()
 	 * @model required="true"
 	 * @generated
 	 */
 	EList<EClass> getInputClasses();
 
 	/**
 	 * Returns the value of the '<em><b>Log Visit Class</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Log Visit Class</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Log Visit Class</em>' attribute.
 	 * @see #setLogVisitClass(String)
 	 * @see kompren.KomprenPackage#getSlicer_LogVisitClass()
 	 * @model
 	 * @generated
 	 */
 	String getLogVisitClass();
 
 	/**
 	 * Sets the value of the '{@link kompren.Slicer#getLogVisitClass <em>Log Visit Class</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Log Visit Class</em>' attribute.
 	 * @see #getLogVisitClass()
 	 * @generated
 	 */
 	void setLogVisitClass(String value);
 
 	/**
 	 * Returns the value of the '<em><b>Log Visit Relation</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Log Visit Relation</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Log Visit Relation</em>' attribute.
 	 * @see #setLogVisitRelation(String)
 	 * @see kompren.KomprenPackage#getSlicer_LogVisitRelation()
 	 * @model
 	 * @generated
 	 */
 	String getLogVisitRelation();
 
 	/**
 	 * Sets the value of the '{@link kompren.Slicer#getLogVisitRelation <em>Log Visit Relation</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Log Visit Relation</em>' attribute.
 	 * @see #getLogVisitRelation()
 	 * @generated
 	 */
 	void setLogVisitRelation(String value);
 
 } // Slicer
