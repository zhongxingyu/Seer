 /**
  */
 package kompren;
 
 import org.eclipse.emf.common.util.EList;
 
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * <!-- begin-user-doc -->
  * A representation of the model object '<em><b>Radius</b></em>'.
  * <!-- end-user-doc -->
  *
  * <p>
  * The following features are supported:
  * <ul>
  *   <li>{@link kompren.Radius#getFocusedClasses <em>Focused Classes</em>}</li>
  * </ul>
  * </p>
  *
  * @see kompren.KomprenPackage#getRadius()
  * @model
  * @generated
  */
 public interface Radius extends EObject {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	String copyright = "Inria/IRISA Diverse Team";
 
 	/**
 	 * Returns the value of the '<em><b>Focused Classes</b></em>' reference list.
 	 * The list contents are of type {@link kompren.SlicedClass}.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Focused Classes</em>' reference list isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Focused Classes</em>' reference list.
 	 * @see kompren.KomprenPackage#getRadius_FocusedClasses()
	 * @model required="true"
 	 * @generated
 	 */
 	EList<SlicedClass> getFocusedClasses();
 
 } // Radius
