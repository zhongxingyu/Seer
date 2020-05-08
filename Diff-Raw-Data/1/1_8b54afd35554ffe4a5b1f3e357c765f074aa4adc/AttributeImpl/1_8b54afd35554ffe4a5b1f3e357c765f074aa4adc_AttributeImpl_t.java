 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.obeonetwork.dsl.entityrelation.impl;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.obeonetwork.dsl.entityrelation.Attribute;
 import org.obeonetwork.dsl.entityrelation.Entity;
 import org.obeonetwork.dsl.entityrelation.EntityRelationFactory;
 import org.obeonetwork.dsl.entityrelation.EntityRelationPackage;
 import org.obeonetwork.dsl.entityrelation.Identifier;
 import org.obeonetwork.dsl.typeslibrary.Type;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Attribute</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.obeonetwork.dsl.entityrelation.impl.AttributeImpl#getOwner <em>Owner</em>}</li>
  *   <li>{@link org.obeonetwork.dsl.entityrelation.impl.AttributeImpl#getType <em>Type</em>}</li>
  *   <li>{@link org.obeonetwork.dsl.entityrelation.impl.AttributeImpl#isRequired <em>Required</em>}</li>
  *   <li>{@link org.obeonetwork.dsl.entityrelation.impl.AttributeImpl#getUsedInIdentifier <em>Used In Identifier</em>}</li>
  *   <li>{@link org.obeonetwork.dsl.entityrelation.impl.AttributeImpl#isInPrimaryIdentifier <em>In Primary Identifier</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class AttributeImpl extends NamedElementImpl implements Attribute {
 	/**
 	 * The cached value of the '{@link #getType() <em>Type</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getType()
 	 * @generated
 	 * @ordered
 	 */
 	protected Type type;
 
 	/**
 	 * The default value of the '{@link #isRequired() <em>Required</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isRequired()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean REQUIRED_EDEFAULT = false;
 
 	/**
 	 * The cached value of the '{@link #isRequired() <em>Required</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isRequired()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean required = REQUIRED_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getUsedInIdentifier() <em>Used In Identifier</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getUsedInIdentifier()
 	 * @generated
 	 * @ordered
 	 */
 	protected Identifier usedInIdentifier;
 
 	/**
 	 * The default value of the '{@link #isInPrimaryIdentifier() <em>In Primary Identifier</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isInPrimaryIdentifier()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean IN_PRIMARY_IDENTIFIER_EDEFAULT = false;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected AttributeImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return EntityRelationPackage.Literals.ATTRIBUTE;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Entity getOwner() {
 		if (eContainerFeatureID() != EntityRelationPackage.ATTRIBUTE__OWNER) return null;
 		return (Entity)eContainer();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetOwner(Entity newOwner, NotificationChain msgs) {
 		msgs = eBasicSetContainer((InternalEObject)newOwner, EntityRelationPackage.ATTRIBUTE__OWNER, msgs);
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setOwner(Entity newOwner) {
 		if (newOwner != eInternalContainer() || (eContainerFeatureID() != EntityRelationPackage.ATTRIBUTE__OWNER && newOwner != null)) {
 			if (EcoreUtil.isAncestor(this, newOwner))
 				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
 			NotificationChain msgs = null;
 			if (eInternalContainer() != null)
 				msgs = eBasicRemoveFromContainer(msgs);
 			if (newOwner != null)
 				msgs = ((InternalEObject)newOwner).eInverseAdd(this, EntityRelationPackage.ENTITY__ATTRIBUTES, Entity.class, msgs);
 			msgs = basicSetOwner(newOwner, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, EntityRelationPackage.ATTRIBUTE__OWNER, newOwner, newOwner));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Type getType() {
 		return type;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetType(Type newType, NotificationChain msgs) {
 		Type oldType = type;
 		type = newType;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, EntityRelationPackage.ATTRIBUTE__TYPE, oldType, newType);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setType(Type newType) {
 		if (newType != type) {
 			NotificationChain msgs = null;
 			if (type != null)
 				msgs = ((InternalEObject)type).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - EntityRelationPackage.ATTRIBUTE__TYPE, null, msgs);
 			if (newType != null)
 				msgs = ((InternalEObject)newType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - EntityRelationPackage.ATTRIBUTE__TYPE, null, msgs);
 			msgs = basicSetType(newType, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, EntityRelationPackage.ATTRIBUTE__TYPE, newType, newType));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isRequired() {
 		return required;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRequired(boolean newRequired) {
 		boolean oldRequired = required;
 		required = newRequired;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, EntityRelationPackage.ATTRIBUTE__REQUIRED, oldRequired, required));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Identifier getUsedInIdentifier() {
 		if (usedInIdentifier != null && usedInIdentifier.eIsProxy()) {
 			InternalEObject oldUsedInIdentifier = (InternalEObject)usedInIdentifier;
 			usedInIdentifier = (Identifier)eResolveProxy(oldUsedInIdentifier);
 			if (usedInIdentifier != oldUsedInIdentifier) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, EntityRelationPackage.ATTRIBUTE__USED_IN_IDENTIFIER, oldUsedInIdentifier, usedInIdentifier));
 			}
 		}
 		return usedInIdentifier;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Identifier basicGetUsedInIdentifier() {
 		return usedInIdentifier;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetUsedInIdentifier(Identifier newUsedInIdentifier, NotificationChain msgs) {
 		Identifier oldUsedInIdentifier = usedInIdentifier;
 		usedInIdentifier = newUsedInIdentifier;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, EntityRelationPackage.ATTRIBUTE__USED_IN_IDENTIFIER, oldUsedInIdentifier, newUsedInIdentifier);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setUsedInIdentifier(Identifier newUsedInIdentifier) {
 		if (newUsedInIdentifier != usedInIdentifier) {
 			NotificationChain msgs = null;
 			if (usedInIdentifier != null)
 				msgs = ((InternalEObject)usedInIdentifier).eInverseRemove(this, EntityRelationPackage.IDENTIFIER__ATTRIBUTES, Identifier.class, msgs);
 			if (newUsedInIdentifier != null)
 				msgs = ((InternalEObject)newUsedInIdentifier).eInverseAdd(this, EntityRelationPackage.IDENTIFIER__ATTRIBUTES, Identifier.class, msgs);
 			msgs = basicSetUsedInIdentifier(newUsedInIdentifier, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, EntityRelationPackage.ATTRIBUTE__USED_IN_IDENTIFIER, newUsedInIdentifier, newUsedInIdentifier));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean isInPrimaryIdentifier() {
 		if (getOwner() != null) {
 			Identifier primaryIdentifier = getOwner().getPrimaryIdentifier();
 			if (primaryIdentifier != null) {
 				return primaryIdentifier.getAttributes().contains(this);
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void addToPrimaryIdentifier() {
 		// Do nothing if the attribute is already in the primary identifier or if it does not belong to a table
 		if (isInPrimaryIdentifier() == false
 				&& getOwner() != null) {
 			Entity entity = getOwner();
 			// First, ensure there is a primary identifier defined on this entity
 			Identifier primaryIdentifier = entity.getPrimaryIdentifier();
 			if (primaryIdentifier == null) {
 				// Create a primary identifier
 				primaryIdentifier = EntityRelationFactory.eINSTANCE.createIdentifier();
 				primaryIdentifier.setName(entity.getName() + "_ID");
				entity.getIdentifiers().add(primaryIdentifier);
 				entity.setPrimaryIdentifier(primaryIdentifier);
 			}
 			
 			// Then attach the attribute to the primary identifier
 			primaryIdentifier.getAttributes().add(this);
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void removeFromPrimaryIdentifier() {
 		if (isInPrimaryIdentifier() == true) {
 			getOwner().getPrimaryIdentifier().getAttributes().remove(this);
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case EntityRelationPackage.ATTRIBUTE__OWNER:
 				if (eInternalContainer() != null)
 					msgs = eBasicRemoveFromContainer(msgs);
 				return basicSetOwner((Entity)otherEnd, msgs);
 			case EntityRelationPackage.ATTRIBUTE__USED_IN_IDENTIFIER:
 				if (usedInIdentifier != null)
 					msgs = ((InternalEObject)usedInIdentifier).eInverseRemove(this, EntityRelationPackage.IDENTIFIER__ATTRIBUTES, Identifier.class, msgs);
 				return basicSetUsedInIdentifier((Identifier)otherEnd, msgs);
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
 			case EntityRelationPackage.ATTRIBUTE__OWNER:
 				return basicSetOwner(null, msgs);
 			case EntityRelationPackage.ATTRIBUTE__TYPE:
 				return basicSetType(null, msgs);
 			case EntityRelationPackage.ATTRIBUTE__USED_IN_IDENTIFIER:
 				return basicSetUsedInIdentifier(null, msgs);
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
 			case EntityRelationPackage.ATTRIBUTE__OWNER:
 				return eInternalContainer().eInverseRemove(this, EntityRelationPackage.ENTITY__ATTRIBUTES, Entity.class, msgs);
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
 			case EntityRelationPackage.ATTRIBUTE__OWNER:
 				return getOwner();
 			case EntityRelationPackage.ATTRIBUTE__TYPE:
 				return getType();
 			case EntityRelationPackage.ATTRIBUTE__REQUIRED:
 				return isRequired();
 			case EntityRelationPackage.ATTRIBUTE__USED_IN_IDENTIFIER:
 				if (resolve) return getUsedInIdentifier();
 				return basicGetUsedInIdentifier();
 			case EntityRelationPackage.ATTRIBUTE__IN_PRIMARY_IDENTIFIER:
 				return isInPrimaryIdentifier();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case EntityRelationPackage.ATTRIBUTE__OWNER:
 				setOwner((Entity)newValue);
 				return;
 			case EntityRelationPackage.ATTRIBUTE__TYPE:
 				setType((Type)newValue);
 				return;
 			case EntityRelationPackage.ATTRIBUTE__REQUIRED:
 				setRequired((Boolean)newValue);
 				return;
 			case EntityRelationPackage.ATTRIBUTE__USED_IN_IDENTIFIER:
 				setUsedInIdentifier((Identifier)newValue);
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
 			case EntityRelationPackage.ATTRIBUTE__OWNER:
 				setOwner((Entity)null);
 				return;
 			case EntityRelationPackage.ATTRIBUTE__TYPE:
 				setType((Type)null);
 				return;
 			case EntityRelationPackage.ATTRIBUTE__REQUIRED:
 				setRequired(REQUIRED_EDEFAULT);
 				return;
 			case EntityRelationPackage.ATTRIBUTE__USED_IN_IDENTIFIER:
 				setUsedInIdentifier((Identifier)null);
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
 			case EntityRelationPackage.ATTRIBUTE__OWNER:
 				return getOwner() != null;
 			case EntityRelationPackage.ATTRIBUTE__TYPE:
 				return type != null;
 			case EntityRelationPackage.ATTRIBUTE__REQUIRED:
 				return required != REQUIRED_EDEFAULT;
 			case EntityRelationPackage.ATTRIBUTE__USED_IN_IDENTIFIER:
 				return usedInIdentifier != null;
 			case EntityRelationPackage.ATTRIBUTE__IN_PRIMARY_IDENTIFIER:
 				return isInPrimaryIdentifier() != IN_PRIMARY_IDENTIFIER_EDEFAULT;
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
 		result.append(" (required: ");
 		result.append(required);
 		result.append(')');
 		return result.toString();
 	}
 
 } //AttributeImpl
