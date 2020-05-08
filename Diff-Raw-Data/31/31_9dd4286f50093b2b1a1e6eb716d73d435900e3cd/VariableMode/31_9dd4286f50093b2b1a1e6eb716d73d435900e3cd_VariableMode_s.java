 /**
  */
 package org.openetcs.model.ertmsformalspecs;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.emf.common.util.Enumerator;
 
 /**
  * <!-- begin-user-doc -->
  * A representation of the literals of the enumeration '<em><b>Variable Mode</b></em>',
  * and utility methods for working with them.
  * <!-- end-user-doc -->
  * @see org.openetcs.model.ertmsformalspecs.ModelPackage#getVariableMode()
  * @model
  * @generated
  */
 public enum VariableMode implements Enumerator {
 	/**
 	 * The '<em><b>Incoming</b></em>' literal object.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #INCOMING_VALUE
 	 * @generated
 	 * @ordered
 	 */
 	INCOMING(0, "Incoming", "Incoming"),
 
 	/**
 	 * The '<em><b>Outgoing</b></em>' literal object.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #OUTGOING_VALUE
 	 * @generated
 	 * @ordered
 	 */
	OUTGOING(0, "Outgoing", "Outgoing"),
 
 	/**
 	 * The '<em><b>Internal</b></em>' literal object.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #INTERNAL_VALUE
 	 * @generated
 	 * @ordered
 	 */
	INTERNAL(0, "Internal", ""),
 
 	/**
 	 * The '<em><b>In Out</b></em>' literal object.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #IN_OUT_VALUE
 	 * @generated
 	 * @ordered
 	 */
	IN_OUT(0, "InOut", "InOut"),
 
 	/**
 	 * The '<em><b>Constant</b></em>' literal object.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #CONSTANT_VALUE
 	 * @generated
 	 * @ordered
 	 */
	CONSTANT(0, "Constant", "Constant");
 
 	/**
 	 * The '<em><b>Incoming</b></em>' literal value.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of '<em><b>Incoming</b></em>' literal object isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @see #INCOMING
 	 * @model name="Incoming"
 	 * @generated
 	 * @ordered
 	 */
 	public static final int INCOMING_VALUE = 0;
 
 	/**
 	 * The '<em><b>Outgoing</b></em>' literal value.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of '<em><b>Outgoing</b></em>' literal object isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @see #OUTGOING
 	 * @model name="Outgoing"
 	 * @generated
 	 * @ordered
 	 */
	public static final int OUTGOING_VALUE = 0;
 
 	/**
 	 * The '<em><b>Internal</b></em>' literal value.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of '<em><b>Internal</b></em>' literal object isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @see #INTERNAL
	 * @model name="Internal" literal=""
 	 * @generated
 	 * @ordered
 	 */
	public static final int INTERNAL_VALUE = 0;
 
 	/**
 	 * The '<em><b>In Out</b></em>' literal value.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of '<em><b>In Out</b></em>' literal object isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @see #IN_OUT
 	 * @model name="InOut"
 	 * @generated
 	 * @ordered
 	 */
	public static final int IN_OUT_VALUE = 0;
 
 	/**
 	 * The '<em><b>Constant</b></em>' literal value.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of '<em><b>Constant</b></em>' literal object isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @see #CONSTANT
 	 * @model name="Constant"
 	 * @generated
 	 * @ordered
 	 */
	public static final int CONSTANT_VALUE = 0;
 
 	/**
 	 * An array of all the '<em><b>Variable Mode</b></em>' enumerators.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private static final VariableMode[] VALUES_ARRAY =
 		new VariableMode[] {
 			INCOMING,
 			OUTGOING,
 			INTERNAL,
 			IN_OUT,
 			CONSTANT,
 		};
 
 	/**
 	 * A public read-only list of all the '<em><b>Variable Mode</b></em>' enumerators.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final List<VariableMode> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));
 
 	/**
 	 * Returns the '<em><b>Variable Mode</b></em>' literal with the specified literal value.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static VariableMode get(String literal) {
 		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
 			VariableMode result = VALUES_ARRAY[i];
 			if (result.toString().equals(literal)) {
 				return result;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the '<em><b>Variable Mode</b></em>' literal with the specified name.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static VariableMode getByName(String name) {
 		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
 			VariableMode result = VALUES_ARRAY[i];
 			if (result.getName().equals(name)) {
 				return result;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the '<em><b>Variable Mode</b></em>' literal with the specified integer value.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static VariableMode get(int value) {
 		switch (value) {
 			case INCOMING_VALUE: return INCOMING;
 		}
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private final int value;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private final String name;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private final String literal;
 
 	/**
 	 * Only this class can construct instances.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private VariableMode(int value, String name, String literal) {
 		this.value = value;
 		this.name = name;
 		this.literal = literal;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public int getValue() {
 	  return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getName() {
 	  return name;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getLiteral() {
 	  return literal;
 	}
 
 	/**
 	 * Returns the literal value of the enumerator, which is its string representation.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		return literal;
 	}
 	
 } //VariableMode
