 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.server.model.versioning.operations.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.emfstore.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.FeatureOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.OperationsPackage;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.UnkownFeatureException;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object '<em><b>Feature Operation</b></em>'. <!-- end-user-doc
  * -->
  * <p>
  * The following features are implemented:
  * <ul>
  * <li>{@link org.eclipse.emf.emfstore.server.model.versioning.operations.impl.FeatureOperationImpl#getFeatureName <em>
  * Feature Name</em>}</li>
  * </ul>
  * </p>
  * 
  * @generated
  */
 public abstract class FeatureOperationImpl extends AbstractOperationImpl implements FeatureOperation {
 
 	@Override
 	public boolean canApply(Project project) {
 		if (!super.canApply(project)) {
 			return false;
 		}
 		EObject element = project.getModelElement(getModelElementId());
 		try {
 			getFeature(element);
 		} catch (UnkownFeatureException e) {
 			return false;
 		}
 		for (ModelElementId otherElementId : getOtherInvolvedModelElements()) {
 			if (!project.contains(otherElementId)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	protected void reverse(AbstractOperation abstractOperation) {
 		super.reverse(abstractOperation);
 		if (!(abstractOperation instanceof FeatureOperation)) {
 			throw new IllegalArgumentException("Given operation is not a feature operation.");
 		}
 		FeatureOperation featureOperation = (FeatureOperation) abstractOperation;
 		featureOperation.setFeatureName(getFeatureName());
 	}
 
 	/**
 	 * The default value of the '{@link #getFeatureName() <em>Feature Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getFeatureName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String FEATURE_NAME_EDEFAULT = "";
 	/**
 	 * The cached value of the '{@link #getFeatureName() <em>Feature Name</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getFeatureName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String featureName = FEATURE_NAME_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected FeatureOperationImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return OperationsPackage.Literals.FEATURE_OPERATION;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getFeatureName() {
 		return featureName;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setFeatureName(String newFeatureName) {
 		String oldFeatureName = featureName;
 		featureName = newFeatureName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, OperationsPackage.FEATURE_OPERATION__FEATURE_NAME, oldFeatureName, featureName));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case OperationsPackage.FEATURE_OPERATION__FEATURE_NAME:
 				return getFeatureName();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case OperationsPackage.FEATURE_OPERATION__FEATURE_NAME:
 				setFeatureName((String)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case OperationsPackage.FEATURE_OPERATION__FEATURE_NAME:
 				setFeatureName(FEATURE_NAME_EDEFAULT);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case OperationsPackage.FEATURE_OPERATION__FEATURE_NAME:
 				return FEATURE_NAME_EDEFAULT == null ? featureName != null : !FEATURE_NAME_EDEFAULT.equals(featureName);
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (featureName: ");
 		result.append(featureName);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.server.model.versioning.operations.FeatureOperation#getFeature(org.eclipse.emf.emfstore.common.model.Project)
 	 * @generated NOT
 	 */
 	public EStructuralFeature getFeature(Project project) throws UnkownFeatureException {
 		EObject modelElement = project.getModelElement(getModelElementId());
 		if (modelElement == null) {
 			throw new IllegalArgumentException("Model Element is not in the given project");
 		}
 		return getFeature(modelElement);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.server.model.versioning.operations.impl.AbstractOperationImpl#getDescription()
 	 */
 	@Override
 	public String getDescription() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.server.model.versioning.operations.impl.AbstractOperationImpl#getName()
 	 */
 	@Override
 	public String getName() {
		return "Unknown Operation";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.server.model.versioning.operations.impl.AbstractOperationImpl#reverse()
 	 */
 	@Override
 	public AbstractOperation reverse() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @generated NOT
 	 * @see org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation#getLeafOperations()
 	 */
 	public List<AbstractOperation> getLeafOperations() {
 		List<AbstractOperation> result = new ArrayList<AbstractOperation>();
 		result.add(this);
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @generated NOT
 	 * @see org.eclipse.emf.emfstore.server.model.versioning.operations.FeatureOperation#getFeature(org.eclipse.emf.emfstore.common.model.ModelElement)
 	 */
 	public EStructuralFeature getFeature(EObject modelElement) throws UnkownFeatureException {
 		EList<EStructuralFeature> features = modelElement.eClass().getEAllStructuralFeatures();
 		for (EStructuralFeature feature : features) {
 			if (feature.getName().equals(this.getFeatureName())) {
 				return feature;
 			}
 		}
 		throw new UnkownFeatureException(modelElement.eClass(), getFeatureName());
 	}
 
 } // FeatureOperationImpl
