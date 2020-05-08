 /**
  * (c) Fachhochschule Potsdam
  *
  * $Id$
  */
 package org.fritzing.fritzing.impl;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
 import org.fritzing.fritzing.FritzingPackage;
 import org.fritzing.fritzing.ILegConnection;
 import org.fritzing.fritzing.Leg;
 import org.fritzing.fritzing.Terminal;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Leg</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.fritzing.fritzing.impl.LegImpl#getParent <em>Parent</em>}</li>
  *   <li>{@link org.fritzing.fritzing.impl.LegImpl#getTarget <em>Target</em>}</li>
  *   <li>{@link org.fritzing.fritzing.impl.LegImpl#getSource <em>Source</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class LegImpl extends ConnectionImpl implements Leg {
 	/**
 	 * The cached value of the '{@link #getTarget() <em>Target</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTarget()
 	 * @generated
 	 * @ordered
 	 */
 	protected ILegConnection target;
 
 	/**
 	 * The cached value of the '{@link #getSource() <em>Source</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSource()
 	 * @generated
 	 * @ordered
 	 */
 	protected ILegConnection source;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected LegImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return FritzingPackage.Literals.LEG;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Terminal getParent() {
 		if (eContainerFeatureID != FritzingPackage.LEG__PARENT) return null;
 		return (Terminal)eContainer();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetParent(Terminal newParent, NotificationChain msgs) {
 		msgs = eBasicSetContainer((InternalEObject)newParent, FritzingPackage.LEG__PARENT, msgs);
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setParent(Terminal newParent) {
 		if (newParent != eInternalContainer() || (eContainerFeatureID != FritzingPackage.LEG__PARENT && newParent != null)) {
 			if (EcoreUtil.isAncestor(this, newParent))
 				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
 			NotificationChain msgs = null;
 			if (eInternalContainer() != null)
 				msgs = eBasicRemoveFromContainer(msgs);
 			if (newParent != null)
 				msgs = ((InternalEObject)newParent).eInverseAdd(this, FritzingPackage.TERMINAL__LEG, Terminal.class, msgs);
 			msgs = basicSetParent(newParent, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, FritzingPackage.LEG__PARENT, newParent, newParent));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ILegConnection getTarget() {
 		if (target != null && target.eIsProxy()) {
 			InternalEObject oldTarget = (InternalEObject)target;
 			target = (ILegConnection)eResolveProxy(oldTarget);
 			if (target != oldTarget) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, FritzingPackage.LEG__TARGET, oldTarget, target));
 			}
 		}
 		return target;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ILegConnection basicGetTarget() {
 		return target;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTarget(ILegConnection newTarget) {
 		ILegConnection oldTarget = target;
 		target = newTarget;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, FritzingPackage.LEG__TARGET, oldTarget, target));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ILegConnection getSource() {
 		if (source != null && source.eIsProxy()) {
 			InternalEObject oldSource = (InternalEObject)source;
 			source = (ILegConnection)eResolveProxy(oldSource);
 			if (source != oldSource) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, FritzingPackage.LEG__SOURCE, oldSource, source));
 			}
 		}
 		return source;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ILegConnection basicGetSource() {
 		return source;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSource(ILegConnection newSource) {
 		ILegConnection oldSource = source;
 		source = newSource;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, FritzingPackage.LEG__SOURCE, oldSource, source));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case FritzingPackage.LEG__PARENT:
 				if (eInternalContainer() != null)
 					msgs = eBasicRemoveFromContainer(msgs);
 				return basicSetParent((Terminal)otherEnd, msgs);
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
 			case FritzingPackage.LEG__PARENT:
 				return basicSetParent(null, msgs);
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
 		switch (eContainerFeatureID) {
 			case FritzingPackage.LEG__PARENT:
 				return eInternalContainer().eInverseRemove(this, FritzingPackage.TERMINAL__LEG, Terminal.class, msgs);
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
 			case FritzingPackage.LEG__PARENT:
 				return getParent();
 			case FritzingPackage.LEG__TARGET:
 				if (resolve) return getTarget();
 				return basicGetTarget();
 			case FritzingPackage.LEG__SOURCE:
 				if (resolve) return getSource();
 				return basicGetSource();
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
 			case FritzingPackage.LEG__PARENT:
 				setParent((Terminal)newValue);
 				return;
 			case FritzingPackage.LEG__TARGET:
 				setTarget((ILegConnection)newValue);
 				return;
 			case FritzingPackage.LEG__SOURCE:
 				setSource((ILegConnection)newValue);
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
 			case FritzingPackage.LEG__PARENT:
 				setParent((Terminal)null);
 				return;
 			case FritzingPackage.LEG__TARGET:
				setTarget((ILegConnection)null);
 				return;
 			case FritzingPackage.LEG__SOURCE:
 				setSource((ILegConnection)null);
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
 			case FritzingPackage.LEG__PARENT:
 				return getParent() != null;
 			case FritzingPackage.LEG__TARGET:
 				return target != null;
 			case FritzingPackage.LEG__SOURCE:
 				return source != null;
 		}
 		return super.eIsSet(featureID);
 	}
 
 } //LegImpl
