 /**
  */
 package com.github.kanafghan.welipse.joomlagen.impl;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.ETypedElement;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 
 import com.github.kanafghan.welipse.joomlagen.GenReference;
 import com.github.kanafghan.welipse.joomlagen.JoomlaGenPackage;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Gen Reference</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.GenReferenceImpl#getEcoreReference <em>Ecore Reference</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.GenReferenceImpl#getOpposite <em>Opposite</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.GenReferenceImpl#isOneToMany <em>One To Many</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.GenReferenceImpl#isManyToMany <em>Many To Many</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.GenReferenceImpl#isMany <em>Many</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class GenReferenceImpl extends GenFeatureImpl implements GenReference {
 	/**
 	 * The cached value of the '{@link #getEcoreReference() <em>Ecore Reference</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEcoreReference()
 	 * @generated
 	 * @ordered
 	 */
 	protected EReference ecoreReference;
 
 	/**
 	 * The cached value of the '{@link #getOpposite() <em>Opposite</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getOpposite()
 	 * @generated
 	 * @ordered
 	 */
 	protected GenReference opposite;
 
 	/**
 	 * The default value of the '{@link #isOneToMany() <em>One To Many</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isOneToMany()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean ONE_TO_MANY_EDEFAULT = false;
 
 	/**
 	 * The default value of the '{@link #isManyToMany() <em>Many To Many</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isManyToMany()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean MANY_TO_MANY_EDEFAULT = false;
 
 	/**
 	 * The default value of the '{@link #isMany() <em>Many</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isMany()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean MANY_EDEFAULT = false;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected GenReferenceImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return JoomlaGenPackage.Literals.GEN_REFERENCE;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEcoreReference() {
 		if (ecoreReference != null && ecoreReference.eIsProxy()) {
 			InternalEObject oldEcoreReference = (InternalEObject)ecoreReference;
 			ecoreReference = (EReference)eResolveProxy(oldEcoreReference);
 			if (ecoreReference != oldEcoreReference) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, JoomlaGenPackage.GEN_REFERENCE__ECORE_REFERENCE, oldEcoreReference, ecoreReference));
 			}
 		}
 		return ecoreReference;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference basicGetEcoreReference() {
 		return ecoreReference;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setEcoreReference(EReference newEcoreReference) {
 		EReference oldEcoreReference = ecoreReference;
 		ecoreReference = newEcoreReference;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.GEN_REFERENCE__ECORE_REFERENCE, oldEcoreReference, ecoreReference));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public GenReference getOpposite() {
 		if (opposite != null && opposite.eIsProxy()) {
 			InternalEObject oldOpposite = (InternalEObject)opposite;
 			opposite = (GenReference)eResolveProxy(oldOpposite);
 			if (opposite != oldOpposite) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, JoomlaGenPackage.GEN_REFERENCE__OPPOSITE, oldOpposite, opposite));
 			}
 		}
 		return opposite;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public GenReference basicGetOpposite() {
 		return opposite;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setOpposite(GenReference newOpposite) {
 		GenReference oldOpposite = opposite;
 		opposite = newOpposite;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.GEN_REFERENCE__OPPOSITE, oldOpposite, opposite));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean isManyToMany() {
 		return getEcoreReference().isMany() 
 				&& getEcoreReference().getEOpposite() != null
 				&& getEcoreReference().getEOpposite().isMany();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void setManyToMany(boolean newManyToMany) {
 		// Not Relevant
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean isMany() {
 		return getEcoreReference().isMany();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void setMany(boolean newMany) {
 		// NOT Relevant
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean isOneToMany() {
 		return (getEcoreReference().isMany() 
 				&& getEcoreReference().getEOpposite() == null
 				) 
 				|| 
 				(!getEcoreReference().isMany()
 				 && getEcoreReference().getEOpposite() != null 
 				 && getEcoreReference().getEOpposite().isMany()
 				)
 				||
 				(getEcoreReference().isMany()
 				 && getEcoreReference().getEOpposite() != null 
 				 && !getEcoreReference().getEOpposite().isMany()
 				);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void setOneToMany(boolean newOneToMany) {
 		// Not Relevant
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case JoomlaGenPackage.GEN_REFERENCE__ECORE_REFERENCE:
 				if (resolve) return getEcoreReference();
 				return basicGetEcoreReference();
 			case JoomlaGenPackage.GEN_REFERENCE__OPPOSITE:
 				if (resolve) return getOpposite();
 				return basicGetOpposite();
 			case JoomlaGenPackage.GEN_REFERENCE__ONE_TO_MANY:
 				return isOneToMany();
 			case JoomlaGenPackage.GEN_REFERENCE__MANY_TO_MANY:
 				return isManyToMany();
 			case JoomlaGenPackage.GEN_REFERENCE__MANY:
 				return isMany();
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
 			case JoomlaGenPackage.GEN_REFERENCE__ECORE_REFERENCE:
 				setEcoreReference((EReference)newValue);
 				return;
 			case JoomlaGenPackage.GEN_REFERENCE__OPPOSITE:
 				setOpposite((GenReference)newValue);
 				return;
 			case JoomlaGenPackage.GEN_REFERENCE__ONE_TO_MANY:
 				setOneToMany((Boolean)newValue);
 				return;
 			case JoomlaGenPackage.GEN_REFERENCE__MANY_TO_MANY:
 				setManyToMany((Boolean)newValue);
 				return;
 			case JoomlaGenPackage.GEN_REFERENCE__MANY:
 				setMany((Boolean)newValue);
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
 			case JoomlaGenPackage.GEN_REFERENCE__ECORE_REFERENCE:
 				setEcoreReference((EReference)null);
 				return;
 			case JoomlaGenPackage.GEN_REFERENCE__OPPOSITE:
 				setOpposite((GenReference)null);
 				return;
 			case JoomlaGenPackage.GEN_REFERENCE__ONE_TO_MANY:
 				setOneToMany(ONE_TO_MANY_EDEFAULT);
 				return;
 			case JoomlaGenPackage.GEN_REFERENCE__MANY_TO_MANY:
 				setManyToMany(MANY_TO_MANY_EDEFAULT);
 				return;
 			case JoomlaGenPackage.GEN_REFERENCE__MANY:
 				setMany(MANY_EDEFAULT);
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
 			case JoomlaGenPackage.GEN_REFERENCE__ECORE_REFERENCE:
 				return ecoreReference != null;
 			case JoomlaGenPackage.GEN_REFERENCE__OPPOSITE:
 				return opposite != null;
 			case JoomlaGenPackage.GEN_REFERENCE__ONE_TO_MANY:
 				return isOneToMany() != ONE_TO_MANY_EDEFAULT;
 			case JoomlaGenPackage.GEN_REFERENCE__MANY_TO_MANY:
 				return isManyToMany() != MANY_TO_MANY_EDEFAULT;
 			case JoomlaGenPackage.GEN_REFERENCE__MANY:
 				return isMany() != MANY_EDEFAULT;
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	@Override
 	public String getName() {
 		return getEcoreReference().getName();
 	}
 
 	@Override
 	public EStructuralFeature getEcoreFeature() {
 		return getEcoreReference();
 	}
 
 	@Override
 	public void initialize(EReference eReference) {
 		if (getEcoreReference() != eReference) {
 			setEcoreReference(eReference);
 			setFormFieldLabel(getName());
 			setFormFieldDescription("Spacify the value of "+ getName());
 			setNullable(true);
 		}
 	}
 
 	@Override
 	public boolean reconcile(GenReference oldGenReferenceVersion) {
 		if (getEcoreReference().getName().equals(oldGenReferenceVersion.getEcoreReference().getName())) {
 			reconsileSettings(oldGenReferenceVersion);
 			return true;
 		}
 		return false;
 	}
 
 	protected void reconsileSettings(GenReference oldGenReferenceVersion) {
 		setFormFieldDescription(oldGenReferenceVersion.getFormFieldDescription());
 		setFormFieldLabel(oldGenReferenceVersion.getFormFieldLabel());
 		setNullable(oldGenReferenceVersion.isNullable());
		setOpposite(oldGenReferenceVersion.getOpposite());
 		setType(oldGenReferenceVersion.getType());
 	}
 
 	@Override
 	public boolean reconcile() {
 		EReference eReference = getEcoreReference();
		if (eReference == null || eReference.eIsProxy() || eReference.eResource() == null) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public ETypedElement getEcoreTypedElement() {
 		return getEcoreReference();
 	}
 
 } //GenReferenceImpl
