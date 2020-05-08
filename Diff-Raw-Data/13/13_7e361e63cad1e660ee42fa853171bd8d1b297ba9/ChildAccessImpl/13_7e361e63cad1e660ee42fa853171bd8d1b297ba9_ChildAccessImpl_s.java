 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.gmf.gmfgraph.impl;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.gmf.gmfgraph.ChildAccess;
 import org.eclipse.gmf.gmfgraph.Figure;
 import org.eclipse.gmf.gmfgraph.FigureAccessor;
 import org.eclipse.gmf.gmfgraph.FigureDescriptor;
 import org.eclipse.gmf.gmfgraph.FigureRef;
 import org.eclipse.gmf.gmfgraph.GMFGraphPackage;
 import org.eclipse.gmf.gmfgraph.RealFigure;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Child Access</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.gmf.gmfgraph.impl.ChildAccessImpl#getOwner <em>Owner</em>}</li>
  *   <li>{@link org.eclipse.gmf.gmfgraph.impl.ChildAccessImpl#getAccessor <em>Accessor</em>}</li>
  *   <li>{@link org.eclipse.gmf.gmfgraph.impl.ChildAccessImpl#getFigure <em>Figure</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class ChildAccessImpl extends EObjectImpl implements ChildAccess {
 	/**
 	 * The default value of the '{@link #getAccessor() <em>Accessor</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAccessor()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String ACCESSOR_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getAccessor() <em>Accessor</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAccessor()
 	 * @generated
 	 * @ordered
 	 */
 	protected String accessor = ACCESSOR_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getFigure() <em>Figure</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getFigure()
 	 * @generated
 	 * @ordered
 	 */
 	protected Figure figure;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected ChildAccessImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return GMFGraphPackage.eINSTANCE.getChildAccess();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FigureDescriptor getOwner() {
 		if (eContainerFeatureID != GMFGraphPackage.CHILD_ACCESS__OWNER) return null;
 		return (FigureDescriptor)eContainer();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getAccessorGen() {
 		return accessor;
 	}
 
 	public String getAccessor() {
 		if (getAccessorGen() == null || getAccessorGen().trim().length() == 0) {
 			final Figure fig = getFigure();
 			if (fig == null) {
				return String.valueOf((Object) null);
 			}
 			if (fig.eContainer() instanceof FigureAccessor) {
 				return ((FigureAccessor) fig.eContainer()).getAccessor();
 			}
 			if (fig instanceof FigureRef) { // XXX bad, perhaps, ChildAccess
				return String.valueOf(((FigureRef) fig).getFigure().getName());
 			}
 			assert fig instanceof RealFigure; // no more options here
			return String.valueOf(((RealFigure) fig).getName());
 		}
		return getAccessorGen();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAccessor(String newAccessor) {
 		String oldAccessor = accessor;
 		accessor = newAccessor;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGraphPackage.CHILD_ACCESS__ACCESSOR, oldAccessor, accessor));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Figure getFigure() {
 		if (figure != null && figure.eIsProxy()) {
 			InternalEObject oldFigure = (InternalEObject)figure;
 			figure = (Figure)eResolveProxy(oldFigure);
 			if (figure != oldFigure) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, GMFGraphPackage.CHILD_ACCESS__FIGURE, oldFigure, figure));
 			}
 		}
 		return figure;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Figure basicGetFigure() {
 		return figure;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setFigure(Figure newFigure) {
 		Figure oldFigure = figure;
 		figure = newFigure;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGraphPackage.CHILD_ACCESS__FIGURE, oldFigure, figure));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case GMFGraphPackage.CHILD_ACCESS__OWNER:
 				if (eInternalContainer() != null)
 					msgs = eBasicRemoveFromContainer(msgs);
 				return eBasicSetContainer(otherEnd, GMFGraphPackage.CHILD_ACCESS__OWNER, msgs);
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
 			case GMFGraphPackage.CHILD_ACCESS__OWNER:
 				return eBasicSetContainer(null, GMFGraphPackage.CHILD_ACCESS__OWNER, msgs);
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
 			case GMFGraphPackage.CHILD_ACCESS__OWNER:
 				return eInternalContainer().eInverseRemove(this, GMFGraphPackage.FIGURE_DESCRIPTOR__ACCESSORS, FigureDescriptor.class, msgs);
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
 			case GMFGraphPackage.CHILD_ACCESS__OWNER:
 				return getOwner();
 			case GMFGraphPackage.CHILD_ACCESS__ACCESSOR:
 				return getAccessor();
 			case GMFGraphPackage.CHILD_ACCESS__FIGURE:
 				if (resolve) return getFigure();
 				return basicGetFigure();
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
 			case GMFGraphPackage.CHILD_ACCESS__ACCESSOR:
 				setAccessor((String)newValue);
 				return;
 			case GMFGraphPackage.CHILD_ACCESS__FIGURE:
 				setFigure((Figure)newValue);
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
 			case GMFGraphPackage.CHILD_ACCESS__ACCESSOR:
 				setAccessor(ACCESSOR_EDEFAULT);
 				return;
 			case GMFGraphPackage.CHILD_ACCESS__FIGURE:
 				setFigure((Figure)null);
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
 			case GMFGraphPackage.CHILD_ACCESS__OWNER:
 				return getOwner() != null;
 			case GMFGraphPackage.CHILD_ACCESS__ACCESSOR:
 				return ACCESSOR_EDEFAULT == null ? accessor != null : !ACCESSOR_EDEFAULT.equals(accessor);
 			case GMFGraphPackage.CHILD_ACCESS__FIGURE:
 				return figure != null;
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
 		result.append(" (accessor: ");
 		result.append(accessor);
 		result.append(')');
 		return result.toString();
 	}
 
 } //ChildAccessImpl
