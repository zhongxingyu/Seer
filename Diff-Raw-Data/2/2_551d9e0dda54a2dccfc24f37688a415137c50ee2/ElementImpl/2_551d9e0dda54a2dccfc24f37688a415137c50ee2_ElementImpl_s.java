 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.lh.dmlj.schema.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.lh.dmlj.schema.Element;
 import org.lh.dmlj.schema.KeyElement;
 import org.lh.dmlj.schema.OccursSpecification;
 import org.lh.dmlj.schema.SchemaPackage;
 import org.lh.dmlj.schema.SchemaRecord;
 import org.lh.dmlj.schema.Usage;
 import org.lh.dmlj.schema.common.PictureAnalyzer;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Element</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.lh.dmlj.schema.impl.ElementImpl#getBaseName <em>Base Name</em>}</li>
  *   <li>{@link org.lh.dmlj.schema.impl.ElementImpl#getChildren <em>Children</em>}</li>
  *   <li>{@link org.lh.dmlj.schema.impl.ElementImpl#getKeyElements <em>Key Elements</em>}</li>
  *   <li>{@link org.lh.dmlj.schema.impl.ElementImpl#getLength <em>Length</em>}</li>
  *   <li>{@link org.lh.dmlj.schema.impl.ElementImpl#getLevel <em>Level</em>}</li>
  *   <li>{@link org.lh.dmlj.schema.impl.ElementImpl#getName <em>Name</em>}</li>
  *   <li>{@link org.lh.dmlj.schema.impl.ElementImpl#isNullable <em>Nullable</em>}</li>
  *   <li>{@link org.lh.dmlj.schema.impl.ElementImpl#getOccursSpecification <em>Occurs Specification</em>}</li>
  *   <li>{@link org.lh.dmlj.schema.impl.ElementImpl#getOffset <em>Offset</em>}</li>
  *   <li>{@link org.lh.dmlj.schema.impl.ElementImpl#getParent <em>Parent</em>}</li>
  *   <li>{@link org.lh.dmlj.schema.impl.ElementImpl#getPicture <em>Picture</em>}</li>
  *   <li>{@link org.lh.dmlj.schema.impl.ElementImpl#getRecord <em>Record</em>}</li>
  *   <li>{@link org.lh.dmlj.schema.impl.ElementImpl#getRedefines <em>Redefines</em>}</li>
  *   <li>{@link org.lh.dmlj.schema.impl.ElementImpl#getSyntaxLength <em>Syntax Length</em>}</li>
  *   <li>{@link org.lh.dmlj.schema.impl.ElementImpl#getSyntaxPosition <em>Syntax Position</em>}</li>
  *   <li>{@link org.lh.dmlj.schema.impl.ElementImpl#getUsage <em>Usage</em>}</li>
  *   <li>{@link org.lh.dmlj.schema.impl.ElementImpl#getValue <em>Value</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class ElementImpl extends EObjectImpl implements Element {
 	/**
 	 * The default value of the '{@link #getBaseName() <em>Base Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getBaseName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String BASE_NAME_EDEFAULT = null;
 	/**
 	 * The cached value of the '{@link #getBaseName() <em>Base Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getBaseName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String baseName = BASE_NAME_EDEFAULT;
 	/**
 	 * The cached value of the '{@link #getChildren() <em>Children</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getChildren()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Element> children;
 	/**
 	 * The cached value of the '{@link #getKeyElements() <em>Key Elements</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getKeyElements()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<KeyElement> keyElements;
 	/**
 	 * The default value of the '{@link #getLength() <em>Length</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLength()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final short LENGTH_EDEFAULT = 0;
 	/**
 	 * The default value of the '{@link #getLevel() <em>Level</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLevel()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final short LEVEL_EDEFAULT = 0;
 	/**
 	 * The cached value of the '{@link #getLevel() <em>Level</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLevel()
 	 * @generated
 	 * @ordered
 	 */
 	protected short level = LEVEL_EDEFAULT;
 	/**
 	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String NAME_EDEFAULT = null;
 	/**
 	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String name = NAME_EDEFAULT;
 	/**
 	 * The default value of the '{@link #isNullable() <em>Nullable</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isNullable()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean NULLABLE_EDEFAULT = false;
 	/**
 	 * The cached value of the '{@link #isNullable() <em>Nullable</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isNullable()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean nullable = NULLABLE_EDEFAULT;
 	/**
 	 * The cached value of the '{@link #getOccursSpecification() <em>Occurs Specification</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getOccursSpecification()
 	 * @generated
 	 * @ordered
 	 */
 	protected OccursSpecification occursSpecification;
 	/**
 	 * The default value of the '{@link #getOffset() <em>Offset</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getOffset()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final short OFFSET_EDEFAULT = 0;
 	/**
 	 * The cached value of the '{@link #getParent() <em>Parent</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getParent()
 	 * @generated
 	 * @ordered
 	 */
 	protected Element parent;
 	/**
 	 * The default value of the '{@link #getPicture() <em>Picture</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getPicture()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String PICTURE_EDEFAULT = null;
 	/**
 	 * The cached value of the '{@link #getPicture() <em>Picture</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getPicture()
 	 * @generated
 	 * @ordered
 	 */
 	protected String picture = PICTURE_EDEFAULT;
 	/**
 	 * The cached value of the '{@link #getRedefines() <em>Redefines</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getRedefines()
 	 * @generated
 	 * @ordered
 	 */
 	protected Element redefines;
 	/**
 	 * The default value of the '{@link #getSyntaxLength() <em>Syntax Length</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSyntaxLength()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final short SYNTAX_LENGTH_EDEFAULT = 0;
 	/**
 	 * The default value of the '{@link #getSyntaxPosition() <em>Syntax Position</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSyntaxPosition()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final short SYNTAX_POSITION_EDEFAULT = 0;
 	/**
 	 * The default value of the '{@link #getUsage() <em>Usage</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getUsage()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final Usage USAGE_EDEFAULT = Usage.DISPLAY;
 	/**
 	 * The cached value of the '{@link #getUsage() <em>Usage</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getUsage()
 	 * @generated
 	 * @ordered
 	 */
 	protected Usage usage = USAGE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getValue() <em>Value</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getValue()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String VALUE_EDEFAULT = null;
 	/**
 	 * The cached value of the '{@link #getValue() <em>Value</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getValue()
 	 * @generated
 	 * @ordered
 	 */
 	protected String value = VALUE_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected ElementImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return SchemaPackage.Literals.ELEMENT;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getBaseName() {
 		return baseName;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setBaseName(String newBaseName) {
 		String oldBaseName = baseName;
 		baseName = newBaseName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, SchemaPackage.ELEMENT__BASE_NAME, oldBaseName, baseName));
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
 	public void setName(String newName) {
 		String oldName = name;
 		name = newName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, SchemaPackage.ELEMENT__NAME, oldName, name));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public short getLevel() {
 		return level;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLevel(short newLevel) {
 		short oldLevel = level;
 		level = newLevel;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, SchemaPackage.ELEMENT__LEVEL, oldLevel, level));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Usage getUsage() {
 		return usage;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setUsage(Usage newUsage) {
 		Usage oldUsage = usage;
 		usage = newUsage == null ? USAGE_EDEFAULT : newUsage;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, SchemaPackage.ELEMENT__USAGE, oldUsage, usage));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getValue() {
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setValue(String newValue) {
 		String oldValue = value;
 		value = newValue;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, SchemaPackage.ELEMENT__VALUE, oldValue, value));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public short getOffset() {		
 		if (getLevel() == 88) {
 			return getParent().getOffset();
 		} else if (getRedefines() != null) {
 			return getRedefines().getOffset();
 		} else {
 			List<Element> elements = getRecord().getElements();
 			int elementIndex = elements.indexOf(this);
 			if (elementIndex == 0) {
 				return 0;
 			}
 			while (elements.get(--elementIndex).getLevel() > getLevel()) {				
 				// finding the previous element with the same or higher level
 				// number
 			}
 			if (elements.get(elementIndex) == parent) {
 				return getParent().getOffset();
 			} else {
 				Element sameLevelElement = elements.get(elementIndex); 
 				// make sure we have the original element and not a redefined
 				// one; we can only trust the length of the original...
 				while (sameLevelElement.getRedefines() != null) {
 					sameLevelElement = sameLevelElement.getRedefines();
 				}
 				// take a possible OCCURS clause into account...
 				int factor = 1;
 				if (sameLevelElement.getOccursSpecification() != null) {
 					factor = 
 						sameLevelElement.getOccursSpecification().getCount();
 				}
 				// for catalog records, a null indicator byte might be there too				
 				int nullIndicatorByte = 0;
 				if (isNullable()) {
 					nullIndicatorByte = 1;
 				}
 				// compute the offset (the formula might not seem completely
 				// correct regarding the null indicator byte for 
 				// sameLevelElement and the multiplication factor, but remember 
 				// that an occurs clause is not supported in the relational 
 				// world...				
 				int result = sameLevelElement.getOffset() +
 							 sameLevelElement.getLength() * factor +
 							 nullIndicatorByte;
 				// 
 				if (getUsage() == Usage.COMPUTATIONAL && 
 					getPicture().endsWith(" SYNC")) {
 								
 					result += result % getLength();
 				}
 				// ... and return it
 				return (short)result;
 			}
 		}		
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public short getLength() {
 		// *********************************************************************
 		// This method does not guarantee to be able to return a length for each
 		// element, so it needs some fine-tuning (read: it has to be redesigned
 		// from scratch).  It will throw an UnsupportedOperationException in the 
 		// case an element's length cannot be derived.
 		// *********************************************************************
 		if (getUsage() == Usage.COMPUTATIONAL_1) {
 			return 4;
 		} else if (getUsage() == Usage.CONDITION_NAME) {
 			return 0;
 		} else if ((getChildren().isEmpty() || 
 					getChildren().get(0).getLevel() == 88) && 
 				   getPicture() != null) {			
 			
 			if (getUsage() == Usage.BIT) {
 				if (getPicture().toUpperCase().startsWith("X(") &&
 					getPicture().toUpperCase().endsWith(")")) {
 						
 					String p = 
 						getPicture().substring(2, getPicture().indexOf(")"));
 					return (short)(Short.valueOf(p) / 8);
 				}
 			} else if (getUsage() == Usage.COMPUTATIONAL) {
 				if (getPicture().equals("9(2)") ||
 					getPicture().equals("S9(4)") ||
 					getPicture().equals("S9(4) SYNC")) {
 					
 					return 2;
 				} else if (getPicture().equals("S9(8)") ||
 						   getPicture().equals("S9(8) SYNC")) {
 					
 					return 4;
 				}
 			} else if (getUsage() == Usage.COMPUTATIONAL_3) {
 				
 				// we now use a modified copy of DMLJ CORE's PictureAnalyzer...
 				int digits = PictureAnalyzer.getDigitCount(getPicture());
 				return (short) (digits / 2 + 1);
 				
 			} else if (getUsage() == Usage.DISPLAY) {
 				if (getPicture().equals("S9(9)V9(6)")) {
 					return 15;
 				} else if (getPicture().equals("9(6)V9(4)")) {
 						return 10;
 				} else if (getPicture().equals("S9(7)V9(6)") ||
 						   getPicture().equals("S9(11)V9(2)")) {
 					
 					return 13;
 				} else if ((getPicture().toUpperCase().startsWith("X(") ||
 					 getPicture().toUpperCase().startsWith("9(") ||
 					 getPicture().toUpperCase().startsWith("S9(")) &&
 					getPicture().toUpperCase().endsWith(")")) {
 					
 					int i = 
 						getPicture().toUpperCase().startsWith("S9(") ? 3 : 2;
 					String p = 
 						getPicture().substring(i, getPicture().indexOf(")"));
 					return Short.valueOf(p);
 				} else if (getPicture().toUpperCase().startsWith("X") &&
 						   getPicture().toUpperCase().endsWith("X")) {
 					
 					return (short)getPicture().length();
 				} else if (getPicture().startsWith("9") &&
 						   !getPicture().startsWith("9(") &&						
 						   getPicture().endsWith("9")) {
 					
 					if (getPicture().toUpperCase().indexOf("V") > -1) {
 						return (short)(getPicture().length() - 1);
 					} else {
 						return (short)getPicture().length();
 					}
 				} else if (getPicture().startsWith("S9") &&
 						   !getPicture().startsWith("S9(") &&
 						   getPicture().endsWith("9")) {
 					
 					if (getPicture().toUpperCase().indexOf("V") > -1) {
 						return (short)(getPicture().length() - 2);
 					} else {
 						return (short)(getPicture().length() - 1);
 					}
 				} else if (getPicture().startsWith("S9(") &&						   
 						   getPicture().endsWith("9") &&
 						   getPicture().toUpperCase().indexOf("V") > -1) {
 										
 					int i = getPicture().length() -
 							getPicture().toUpperCase().indexOf("V") - 1;
 					String p = 
 						getPicture().substring(3, getPicture().indexOf(")"));
 					return (short) (Short.valueOf(p) + i);					
 				} else if (getPicture().equals("9V9(02)")) {
 					return 3;
 				} else if (getPicture().equals("9(3)V99") ||
 						   getPicture().equals("9(03)V99") ||
 						   getPicture().equals("S9V9(4)")) {
 					
 					return 5;
 				} else if (getPicture().equals("9V9(6)")) {
 					return 7;
 				} else if (getPicture().equals("S9V9(8)")) {
 					return 9;
 				} else if (getPicture().equals("9V9(4)")) {
 					return 5;
 				} else if (getPicture().equals("9(07)V99")) {
 					return 9;
 				} else if (getPicture().equals("9(01)V99")) {
 					return 3;
 				} else if (getPicture().equals("9(05)V99")) {
					return 3;
 				} else if (getPicture().equals("9V9(04)")) {
 					return 5;
 				} else if (getPicture().equals("9V9(10)")) {
 					return 11;
 				} else if (getPicture().equals("S9V9(6)")) {
 					return 7;
 				}
 			}
 			throw new UnsupportedOperationException(getName() + " PIC " +
 												    getPicture() + " " + 
 												    getUsage());
 		} else {
 			short i = 0;
 			for (Element child : getChildren()) {
 				if (child.getRedefines() == null) {
 					// ignore elements that redefine another element
 					short factor = 1;
 					if (child.getOccursSpecification() != null) {
 						factor = child.getOccursSpecification().getCount();
 					}
 					i += child.getLength() * factor;
 				}
 			}
 			return i;
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getPicture() {
 		return picture;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setPicture(String newPicture) {
 		String oldPicture = picture;
 		picture = newPicture;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, SchemaPackage.ELEMENT__PICTURE, oldPicture, picture));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isNullable() {
 		return nullable;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setNullable(boolean newNullable) {
 		boolean oldNullable = nullable;
 		nullable = newNullable;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, SchemaPackage.ELEMENT__NULLABLE, oldNullable, nullable));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public SchemaRecord getRecord() {
 		if (eContainerFeatureID() != SchemaPackage.ELEMENT__RECORD) return null;
 		return (SchemaRecord)eContainer();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetRecord(SchemaRecord newRecord, NotificationChain msgs) {
 		msgs = eBasicSetContainer((InternalEObject)newRecord, SchemaPackage.ELEMENT__RECORD, msgs);
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRecord(SchemaRecord newRecord) {
 		if (newRecord != eInternalContainer() || (eContainerFeatureID() != SchemaPackage.ELEMENT__RECORD && newRecord != null)) {
 			if (EcoreUtil.isAncestor(this, newRecord))
 				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
 			NotificationChain msgs = null;
 			if (eInternalContainer() != null)
 				msgs = eBasicRemoveFromContainer(msgs);
 			if (newRecord != null)
 				msgs = ((InternalEObject)newRecord).eInverseAdd(this, SchemaPackage.SCHEMA_RECORD__ELEMENTS, SchemaRecord.class, msgs);
 			msgs = basicSetRecord(newRecord, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, SchemaPackage.ELEMENT__RECORD, newRecord, newRecord));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<KeyElement> getKeyElements() {
 		if (keyElements == null) {
 			keyElements = new EObjectWithInverseResolvingEList<KeyElement>(KeyElement.class, this, SchemaPackage.ELEMENT__KEY_ELEMENTS, SchemaPackage.KEY_ELEMENT__ELEMENT);
 		}
 		return keyElements;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Element> getChildren() {
 		if (children == null) {
 			children = new EObjectWithInverseResolvingEList<Element>(Element.class, this, SchemaPackage.ELEMENT__CHILDREN, SchemaPackage.ELEMENT__PARENT);
 		}
 		return children;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Element getRedefines() {
 		if (redefines != null && redefines.eIsProxy()) {
 			InternalEObject oldRedefines = (InternalEObject)redefines;
 			redefines = (Element)eResolveProxy(oldRedefines);
 			if (redefines != oldRedefines) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, SchemaPackage.ELEMENT__REDEFINES, oldRedefines, redefines));
 			}
 		}
 		return redefines;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Element basicGetRedefines() {
 		return redefines;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRedefines(Element newRedefines) {
 		Element oldRedefines = redefines;
 		redefines = newRedefines;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, SchemaPackage.ELEMENT__REDEFINES, oldRedefines, redefines));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public short getSyntaxLength() {
 		short length = getLength();
 		short result = length;
 		if (getOccursSpecification() != null) {
 			result *= getOccursSpecification().getCount();
 		}
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getSyntaxName() {		
 		return getBaseName();		
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public short getSyntaxPosition() {		
 		List<Element> parentElementsWithAnOccurs = new ArrayList<>();
 		Element parent = getParent();
 		while (parent != null) {
 			if (parent.getOccursSpecification() != null) {
 				parentElementsWithAnOccurs.add(parent);
 			}
 			parent = parent.getParent();
 		}
 		if (!parentElementsWithAnOccurs.isEmpty()) {
 			if (parentElementsWithAnOccurs.size() > 1) {
 				// multi-dimensional table		
 				return (short) (getOffset() - parentElementsWithAnOccurs.get(0).getOffset() + 1);
 			} else {
 				// single-dimensional table
 				return (short) (getOffset() + 2 - 
 						    	parentElementsWithAnOccurs.get(0)
 							    						  .getSyntaxPosition());
 			}
 		} else {
 			return (short) (getOffset() + 1);
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public OccursSpecification getOccursSpecification() {
 		return occursSpecification;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetOccursSpecification(OccursSpecification newOccursSpecification, NotificationChain msgs) {
 		OccursSpecification oldOccursSpecification = occursSpecification;
 		occursSpecification = newOccursSpecification;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, SchemaPackage.ELEMENT__OCCURS_SPECIFICATION, oldOccursSpecification, newOccursSpecification);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setOccursSpecification(OccursSpecification newOccursSpecification) {
 		if (newOccursSpecification != occursSpecification) {
 			NotificationChain msgs = null;
 			if (occursSpecification != null)
 				msgs = ((InternalEObject)occursSpecification).eInverseRemove(this, SchemaPackage.OCCURS_SPECIFICATION__ELEMENT, OccursSpecification.class, msgs);
 			if (newOccursSpecification != null)
 				msgs = ((InternalEObject)newOccursSpecification).eInverseAdd(this, SchemaPackage.OCCURS_SPECIFICATION__ELEMENT, OccursSpecification.class, msgs);
 			msgs = basicSetOccursSpecification(newOccursSpecification, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, SchemaPackage.ELEMENT__OCCURS_SPECIFICATION, newOccursSpecification, newOccursSpecification));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case SchemaPackage.ELEMENT__CHILDREN:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getChildren()).basicAdd(otherEnd, msgs);
 			case SchemaPackage.ELEMENT__KEY_ELEMENTS:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getKeyElements()).basicAdd(otherEnd, msgs);
 			case SchemaPackage.ELEMENT__OCCURS_SPECIFICATION:
 				if (occursSpecification != null)
 					msgs = ((InternalEObject)occursSpecification).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - SchemaPackage.ELEMENT__OCCURS_SPECIFICATION, null, msgs);
 				return basicSetOccursSpecification((OccursSpecification)otherEnd, msgs);
 			case SchemaPackage.ELEMENT__PARENT:
 				if (parent != null)
 					msgs = ((InternalEObject)parent).eInverseRemove(this, SchemaPackage.ELEMENT__CHILDREN, Element.class, msgs);
 				return basicSetParent((Element)otherEnd, msgs);
 			case SchemaPackage.ELEMENT__RECORD:
 				if (eInternalContainer() != null)
 					msgs = eBasicRemoveFromContainer(msgs);
 				return basicSetRecord((SchemaRecord)otherEnd, msgs);
 		}
 		return super.eInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case SchemaPackage.ELEMENT__CHILDREN:
 				return ((InternalEList<?>)getChildren()).basicRemove(otherEnd, msgs);
 			case SchemaPackage.ELEMENT__KEY_ELEMENTS:
 				return ((InternalEList<?>)getKeyElements()).basicRemove(otherEnd, msgs);
 			case SchemaPackage.ELEMENT__OCCURS_SPECIFICATION:
 				return basicSetOccursSpecification(null, msgs);
 			case SchemaPackage.ELEMENT__PARENT:
 				return basicSetParent(null, msgs);
 			case SchemaPackage.ELEMENT__RECORD:
 				return basicSetRecord(null, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
 		switch (eContainerFeatureID()) {
 			case SchemaPackage.ELEMENT__RECORD:
 				return eInternalContainer().eInverseRemove(this, SchemaPackage.SCHEMA_RECORD__ELEMENTS, SchemaRecord.class, msgs);
 		}
 		return super.eBasicRemoveFromContainerFeature(msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case SchemaPackage.ELEMENT__BASE_NAME:
 				return getBaseName();
 			case SchemaPackage.ELEMENT__CHILDREN:
 				return getChildren();
 			case SchemaPackage.ELEMENT__KEY_ELEMENTS:
 				return getKeyElements();
 			case SchemaPackage.ELEMENT__LENGTH:
 				return getLength();
 			case SchemaPackage.ELEMENT__LEVEL:
 				return getLevel();
 			case SchemaPackage.ELEMENT__NAME:
 				return getName();
 			case SchemaPackage.ELEMENT__NULLABLE:
 				return isNullable();
 			case SchemaPackage.ELEMENT__OCCURS_SPECIFICATION:
 				return getOccursSpecification();
 			case SchemaPackage.ELEMENT__OFFSET:
 				return getOffset();
 			case SchemaPackage.ELEMENT__PARENT:
 				if (resolve) return getParent();
 				return basicGetParent();
 			case SchemaPackage.ELEMENT__PICTURE:
 				return getPicture();
 			case SchemaPackage.ELEMENT__RECORD:
 				return getRecord();
 			case SchemaPackage.ELEMENT__REDEFINES:
 				if (resolve) return getRedefines();
 				return basicGetRedefines();
 			case SchemaPackage.ELEMENT__SYNTAX_LENGTH:
 				return getSyntaxLength();
 			case SchemaPackage.ELEMENT__SYNTAX_POSITION:
 				return getSyntaxPosition();
 			case SchemaPackage.ELEMENT__USAGE:
 				return getUsage();
 			case SchemaPackage.ELEMENT__VALUE:
 				return getValue();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case SchemaPackage.ELEMENT__BASE_NAME:
 				setBaseName((String)newValue);
 				return;
 			case SchemaPackage.ELEMENT__CHILDREN:
 				getChildren().clear();
 				getChildren().addAll((Collection<? extends Element>)newValue);
 				return;
 			case SchemaPackage.ELEMENT__KEY_ELEMENTS:
 				getKeyElements().clear();
 				getKeyElements().addAll((Collection<? extends KeyElement>)newValue);
 				return;
 			case SchemaPackage.ELEMENT__LEVEL:
 				setLevel((Short)newValue);
 				return;
 			case SchemaPackage.ELEMENT__NAME:
 				setName((String)newValue);
 				return;
 			case SchemaPackage.ELEMENT__NULLABLE:
 				setNullable((Boolean)newValue);
 				return;
 			case SchemaPackage.ELEMENT__OCCURS_SPECIFICATION:
 				setOccursSpecification((OccursSpecification)newValue);
 				return;
 			case SchemaPackage.ELEMENT__PARENT:
 				setParent((Element)newValue);
 				return;
 			case SchemaPackage.ELEMENT__PICTURE:
 				setPicture((String)newValue);
 				return;
 			case SchemaPackage.ELEMENT__RECORD:
 				setRecord((SchemaRecord)newValue);
 				return;
 			case SchemaPackage.ELEMENT__REDEFINES:
 				setRedefines((Element)newValue);
 				return;
 			case SchemaPackage.ELEMENT__USAGE:
 				setUsage((Usage)newValue);
 				return;
 			case SchemaPackage.ELEMENT__VALUE:
 				setValue((String)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case SchemaPackage.ELEMENT__BASE_NAME:
 				setBaseName(BASE_NAME_EDEFAULT);
 				return;
 			case SchemaPackage.ELEMENT__CHILDREN:
 				getChildren().clear();
 				return;
 			case SchemaPackage.ELEMENT__KEY_ELEMENTS:
 				getKeyElements().clear();
 				return;
 			case SchemaPackage.ELEMENT__LEVEL:
 				setLevel(LEVEL_EDEFAULT);
 				return;
 			case SchemaPackage.ELEMENT__NAME:
 				setName(NAME_EDEFAULT);
 				return;
 			case SchemaPackage.ELEMENT__NULLABLE:
 				setNullable(NULLABLE_EDEFAULT);
 				return;
 			case SchemaPackage.ELEMENT__OCCURS_SPECIFICATION:
 				setOccursSpecification((OccursSpecification)null);
 				return;
 			case SchemaPackage.ELEMENT__PARENT:
 				setParent((Element)null);
 				return;
 			case SchemaPackage.ELEMENT__PICTURE:
 				setPicture(PICTURE_EDEFAULT);
 				return;
 			case SchemaPackage.ELEMENT__RECORD:
 				setRecord((SchemaRecord)null);
 				return;
 			case SchemaPackage.ELEMENT__REDEFINES:
 				setRedefines((Element)null);
 				return;
 			case SchemaPackage.ELEMENT__USAGE:
 				setUsage(USAGE_EDEFAULT);
 				return;
 			case SchemaPackage.ELEMENT__VALUE:
 				setValue(VALUE_EDEFAULT);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case SchemaPackage.ELEMENT__BASE_NAME:
 				return BASE_NAME_EDEFAULT == null ? baseName != null : !BASE_NAME_EDEFAULT.equals(baseName);
 			case SchemaPackage.ELEMENT__CHILDREN:
 				return children != null && !children.isEmpty();
 			case SchemaPackage.ELEMENT__KEY_ELEMENTS:
 				return keyElements != null && !keyElements.isEmpty();
 			case SchemaPackage.ELEMENT__LENGTH:
 				return getLength() != LENGTH_EDEFAULT;
 			case SchemaPackage.ELEMENT__LEVEL:
 				return level != LEVEL_EDEFAULT;
 			case SchemaPackage.ELEMENT__NAME:
 				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
 			case SchemaPackage.ELEMENT__NULLABLE:
 				return nullable != NULLABLE_EDEFAULT;
 			case SchemaPackage.ELEMENT__OCCURS_SPECIFICATION:
 				return occursSpecification != null;
 			case SchemaPackage.ELEMENT__OFFSET:
 				return getOffset() != OFFSET_EDEFAULT;
 			case SchemaPackage.ELEMENT__PARENT:
 				return parent != null;
 			case SchemaPackage.ELEMENT__PICTURE:
 				return PICTURE_EDEFAULT == null ? picture != null : !PICTURE_EDEFAULT.equals(picture);
 			case SchemaPackage.ELEMENT__RECORD:
 				return getRecord() != null;
 			case SchemaPackage.ELEMENT__REDEFINES:
 				return redefines != null;
 			case SchemaPackage.ELEMENT__SYNTAX_LENGTH:
 				return getSyntaxLength() != SYNTAX_LENGTH_EDEFAULT;
 			case SchemaPackage.ELEMENT__SYNTAX_POSITION:
 				return getSyntaxPosition() != SYNTAX_POSITION_EDEFAULT;
 			case SchemaPackage.ELEMENT__USAGE:
 				return usage != USAGE_EDEFAULT;
 			case SchemaPackage.ELEMENT__VALUE:
 				return VALUE_EDEFAULT == null ? value != null : !VALUE_EDEFAULT.equals(value);
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (baseName: ");
 		result.append(baseName);
 		result.append(", level: ");
 		result.append(level);
 		result.append(", name: ");
 		result.append(name);
 		result.append(", nullable: ");
 		result.append(nullable);
 		result.append(", picture: ");
 		result.append(picture);
 		result.append(", usage: ");
 		result.append(usage);
 		result.append(", value: ");
 		result.append(value);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Element getParent() {
 		if (parent != null && parent.eIsProxy()) {
 			InternalEObject oldParent = (InternalEObject)parent;
 			parent = (Element)eResolveProxy(oldParent);
 			if (parent != oldParent) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, SchemaPackage.ELEMENT__PARENT, oldParent, parent));
 			}
 		}
 		return parent;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Element basicGetParent() {
 		return parent;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetParent(Element newParent, NotificationChain msgs) {
 		Element oldParent = parent;
 		parent = newParent;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, SchemaPackage.ELEMENT__PARENT, oldParent, newParent);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setParent(Element newParent) {
 		if (newParent != parent) {
 			NotificationChain msgs = null;
 			if (parent != null)
 				msgs = ((InternalEObject)parent).eInverseRemove(this, SchemaPackage.ELEMENT__CHILDREN, Element.class, msgs);
 			if (newParent != null)
 				msgs = ((InternalEObject)newParent).eInverseAdd(this, SchemaPackage.ELEMENT__CHILDREN, Element.class, msgs);
 			msgs = basicSetParent(newParent, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, SchemaPackage.ELEMENT__PARENT, newParent, newParent));
 	}
 
 } //ElementImpl
