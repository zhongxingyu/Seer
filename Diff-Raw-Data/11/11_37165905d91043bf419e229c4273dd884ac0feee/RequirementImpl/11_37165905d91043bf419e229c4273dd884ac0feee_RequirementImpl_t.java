 /**
  * Copyright (c) 2006-2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  */
 package org.eclipse.b3.p2.impl;
 
 import org.eclipse.b3.p2.P2Package;
 import org.eclipse.b3.p2.Requirement;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.metadata.expression.IMatchExpression;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Requirement</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  * <li>{@link org.eclipse.b3.p2.impl.RequirementImpl#getFilter <em>Filter</em>}</li>
  * <li>{@link org.eclipse.b3.p2.impl.RequirementImpl#getMax <em>Max</em>}</li>
  * <li>{@link org.eclipse.b3.p2.impl.RequirementImpl#getMin <em>Min</em>}</li>
  * <li>{@link org.eclipse.b3.p2.impl.RequirementImpl#getMatches <em>Matches</em>}</li>
  * <li>{@link org.eclipse.b3.p2.impl.RequirementImpl#isGreedy <em>Greedy</em>}</li>
  * <li>{@link org.eclipse.b3.p2.impl.RequirementImpl#getDescription <em>Description</em>}</li>
  * </ul>
  * </p>
  * 
  * @generated
  */
 public class RequirementImpl extends MinimalEObjectImpl.Container implements Requirement {
 	/**
 	 * A set of bit flags representing the values of boolean attributes and whether unsettable features have been set.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 * @ordered
 	 */
 	protected int eFlags = 0;
 
 	/**
 	 * The cached value of the '{@link #getFilter() <em>Filter</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getFilter()
 	 * @generated
 	 * @ordered
 	 */
 	protected IMatchExpression<IInstallableUnit> filter;
 
 	/**
 	 * The default value of the '{@link #getMax() <em>Max</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getMax()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int MAX_EDEFAULT = 0;
 
 	/**
 	 * The cached value of the '{@link #getMax() <em>Max</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getMax()
 	 * @generated
 	 * @ordered
 	 */
 	protected int max = MAX_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getMin() <em>Min</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getMin()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int MIN_EDEFAULT = 0;
 
 	/**
 	 * The cached value of the '{@link #getMin() <em>Min</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getMin()
 	 * @generated
 	 * @ordered
 	 */
 	protected int min = MIN_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getMatches() <em>Matches</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getMatches()
 	 * @generated
 	 * @ordered
 	 */
 	protected IMatchExpression<IInstallableUnit> matches;
 
 	/**
 	 * The default value of the '{@link #isGreedy() <em>Greedy</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #isGreedy()
 	 * @generated
 	 * @ordered
 	 */
	protected static final boolean GREEDY_EDEFAULT = true;
 
 	/**
 	 * The flag representing the value of the '{@link #isGreedy() <em>Greedy</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #isGreedy()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int GREEDY_EFLAG = 1 << 0;
 
 	/**
 	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DESCRIPTION_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected String description = DESCRIPTION_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected RequirementImpl() {
 		super();
		eFlags |= GREEDY_EFLAG;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch(featureID) {
 			case P2Package.REQUIREMENT__FILTER:
 				return getFilter();
 			case P2Package.REQUIREMENT__MAX:
 				return getMax();
 			case P2Package.REQUIREMENT__MIN:
 				return getMin();
 			case P2Package.REQUIREMENT__MATCHES:
 				return getMatches();
 			case P2Package.REQUIREMENT__GREEDY:
 				return isGreedy();
 			case P2Package.REQUIREMENT__DESCRIPTION:
 				return getDescription();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch(featureID) {
 			case P2Package.REQUIREMENT__FILTER:
 				return filter != null;
 			case P2Package.REQUIREMENT__MAX:
 				return max != MAX_EDEFAULT;
 			case P2Package.REQUIREMENT__MIN:
 				return min != MIN_EDEFAULT;
 			case P2Package.REQUIREMENT__MATCHES:
 				return matches != null;
 			case P2Package.REQUIREMENT__GREEDY:
 				return ((eFlags & GREEDY_EFLAG) != 0) != GREEDY_EDEFAULT;
 			case P2Package.REQUIREMENT__DESCRIPTION:
 				return DESCRIPTION_EDEFAULT == null
 						? description != null
 						: !DESCRIPTION_EDEFAULT.equals(description);
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch(featureID) {
 			case P2Package.REQUIREMENT__FILTER:
 				setFilter((IMatchExpression<IInstallableUnit>) newValue);
 				return;
 			case P2Package.REQUIREMENT__MAX:
 				setMax((Integer) newValue);
 				return;
 			case P2Package.REQUIREMENT__MIN:
 				setMin((Integer) newValue);
 				return;
 			case P2Package.REQUIREMENT__MATCHES:
 				setMatches((IMatchExpression<IInstallableUnit>) newValue);
 				return;
 			case P2Package.REQUIREMENT__GREEDY:
 				setGreedy((Boolean) newValue);
 				return;
 			case P2Package.REQUIREMENT__DESCRIPTION:
 				setDescription((String) newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return P2Package.Literals.REQUIREMENT;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch(featureID) {
 			case P2Package.REQUIREMENT__FILTER:
 				setFilter((IMatchExpression<IInstallableUnit>) null);
 				return;
 			case P2Package.REQUIREMENT__MAX:
 				setMax(MAX_EDEFAULT);
 				return;
 			case P2Package.REQUIREMENT__MIN:
 				setMin(MIN_EDEFAULT);
 				return;
 			case P2Package.REQUIREMENT__MATCHES:
 				setMatches((IMatchExpression<IInstallableUnit>) null);
 				return;
 			case P2Package.REQUIREMENT__GREEDY:
 				setGreedy(GREEDY_EDEFAULT);
 				return;
 			case P2Package.REQUIREMENT__DESCRIPTION:
 				setDescription(DESCRIPTION_EDEFAULT);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public IMatchExpression<IInstallableUnit> getFilter() {
 		return filter;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public IMatchExpression<IInstallableUnit> getMatches() {
 		return matches;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public int getMax() {
 		return max;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public int getMin() {
 		return min;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public boolean isGreedy() {
 		return (eFlags & GREEDY_EFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean isMatch(IInstallableUnit installableUnit) {
 		return getMatches().isMatch(installableUnit);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setDescription(String newDescription) {
 		String oldDescription = description;
 		description = newDescription;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(
 				this, Notification.SET, P2Package.REQUIREMENT__DESCRIPTION, oldDescription, description));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setFilter(IMatchExpression<IInstallableUnit> newFilter) {
 		IMatchExpression<IInstallableUnit> oldFilter = filter;
 		filter = newFilter;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, P2Package.REQUIREMENT__FILTER, oldFilter, filter));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setGreedy(boolean newGreedy) {
 		boolean oldGreedy = (eFlags & GREEDY_EFLAG) != 0;
 		if(newGreedy)
 			eFlags |= GREEDY_EFLAG;
 		else
 			eFlags &= ~GREEDY_EFLAG;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, P2Package.REQUIREMENT__GREEDY, oldGreedy, newGreedy));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setMatches(IMatchExpression<IInstallableUnit> newMatches) {
 		IMatchExpression<IInstallableUnit> oldMatches = matches;
 		matches = newMatches;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, P2Package.REQUIREMENT__MATCHES, oldMatches, matches));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setMax(int newMax) {
 		int oldMax = max;
 		max = newMax;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, P2Package.REQUIREMENT__MAX, oldMax, max));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setMin(int newMin) {
 		int oldMin = min;
 		min = newMin;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, P2Package.REQUIREMENT__MIN, oldMin, min));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public String toString() {
 		if(eIsProxy())
 			return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(min);
 		result.append("..");
 		if(max < Integer.MAX_VALUE)
 			result.append(max);
 		else
 			result.append('*');
 
 		result.append(" [");
 		result.append(filter);
 		result.append("]");
 		if(isGreedy())
 			result.append(", greedy");
 		return result.toString();
 	}
 
 } // RequirementImpl
