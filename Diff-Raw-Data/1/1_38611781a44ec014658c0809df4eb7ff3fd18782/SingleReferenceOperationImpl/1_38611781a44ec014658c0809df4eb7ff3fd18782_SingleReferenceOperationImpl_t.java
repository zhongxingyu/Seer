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
 package org.eclipse.emf.emfstore.internal.server.model.versioning.operations.impl;
 
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.emfstore.internal.common.model.IdEObjectCollection;
 import org.eclipse.emf.emfstore.internal.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.internal.common.model.impl.IdEObjectCollectionImpl;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.OperationsFactory;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.OperationsPackage;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.SingleReferenceOperation;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.UnkownFeatureException;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.UnsetType;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object ' <em><b>Single Reference Operation</b></em>'. <!--
  * end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  * <li>
  * {@link org.eclipse.emf.emfstore.internal.server.model.versioning.operations.impl.SingleReferenceOperationImpl#getOldValue
  * <em>Old Value</em>}</li>
  * <li>
  * {@link org.eclipse.emf.emfstore.internal.server.model.versioning.operations.impl.SingleReferenceOperationImpl#getNewValue
  * <em>New Value</em>}</li>
  * </ul>
  * </p>
  * 
  * @generated
  */
 public class SingleReferenceOperationImpl extends ReferenceOperationImpl implements SingleReferenceOperation {
 	/**
 	 * The cached value of the '{@link #getOldValue() <em>Old Value</em>}' containment reference.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getOldValue()
 	 * @generated
 	 * @ordered
 	 */
 	protected ModelElementId oldValue;
 
 	/**
 	 * The cached value of the '{@link #getNewValue() <em>New Value</em>}' containment reference.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getNewValue()
 	 * @generated
 	 * @ordered
 	 */
 	protected ModelElementId newValue;
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected SingleReferenceOperationImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return OperationsPackage.Literals.SINGLE_REFERENCE_OPERATION;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ModelElementId getOldValue() {
 		if (oldValue != null && oldValue.eIsProxy())
 		{
 			final InternalEObject oldOldValue = (InternalEObject) oldValue;
 			oldValue = (ModelElementId) eResolveProxy(oldOldValue);
 			if (oldValue != oldOldValue)
 			{
 				final InternalEObject newOldValue = (InternalEObject) oldValue;
 				NotificationChain msgs = oldOldValue.eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- OperationsPackage.SINGLE_REFERENCE_OPERATION__OLD_VALUE, null, null);
 				if (newOldValue.eInternalContainer() == null)
 				{
 					msgs = newOldValue.eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 						- OperationsPackage.SINGLE_REFERENCE_OPERATION__OLD_VALUE, null, msgs);
 				}
 				if (msgs != null) {
 					msgs.dispatch();
 				}
 				if (eNotificationRequired()) {
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE,
 						OperationsPackage.SINGLE_REFERENCE_OPERATION__OLD_VALUE, oldOldValue, oldValue));
 				}
 			}
 		}
 		return oldValue;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ModelElementId basicGetOldValue() {
 		return oldValue;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public NotificationChain basicSetOldValue(ModelElementId newOldValue, NotificationChain msgs) {
 		final ModelElementId oldOldValue = oldValue;
 		oldValue = newOldValue;
 		if (eNotificationRequired())
 		{
 			final ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
 				OperationsPackage.SINGLE_REFERENCE_OPERATION__OLD_VALUE, oldOldValue, newOldValue);
 			if (msgs == null) {
 				msgs = notification;
 			} else {
 				msgs.add(notification);
 			}
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setOldValue(ModelElementId newOldValue) {
 		if (newOldValue != oldValue)
 		{
 			NotificationChain msgs = null;
 			if (oldValue != null) {
 				msgs = ((InternalEObject) oldValue).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- OperationsPackage.SINGLE_REFERENCE_OPERATION__OLD_VALUE, null, msgs);
 			}
 			if (newOldValue != null) {
 				msgs = ((InternalEObject) newOldValue).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 					- OperationsPackage.SINGLE_REFERENCE_OPERATION__OLD_VALUE, null, msgs);
 			}
 			msgs = basicSetOldValue(newOldValue, msgs);
 			if (msgs != null) {
 				msgs.dispatch();
 			}
 		}
 		else if (eNotificationRequired()) {
 			eNotify(new ENotificationImpl(this, Notification.SET,
 				OperationsPackage.SINGLE_REFERENCE_OPERATION__OLD_VALUE, newOldValue, newOldValue));
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ModelElementId getNewValue() {
 		if (newValue != null && newValue.eIsProxy())
 		{
 			final InternalEObject oldNewValue = (InternalEObject) newValue;
 			newValue = (ModelElementId) eResolveProxy(oldNewValue);
 			if (newValue != oldNewValue)
 			{
 				final InternalEObject newNewValue = (InternalEObject) newValue;
 				NotificationChain msgs = oldNewValue.eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- OperationsPackage.SINGLE_REFERENCE_OPERATION__NEW_VALUE, null, null);
 				if (newNewValue.eInternalContainer() == null)
 				{
 					msgs = newNewValue.eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 						- OperationsPackage.SINGLE_REFERENCE_OPERATION__NEW_VALUE, null, msgs);
 				}
 				if (msgs != null) {
 					msgs.dispatch();
 				}
 				if (eNotificationRequired()) {
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE,
 						OperationsPackage.SINGLE_REFERENCE_OPERATION__NEW_VALUE, oldNewValue, newValue));
 				}
 			}
 		}
 		return newValue;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ModelElementId basicGetNewValue() {
 		return newValue;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public NotificationChain basicSetNewValue(ModelElementId newNewValue, NotificationChain msgs) {
 		final ModelElementId oldNewValue = newValue;
 		newValue = newNewValue;
 		if (eNotificationRequired())
 		{
 			final ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
 				OperationsPackage.SINGLE_REFERENCE_OPERATION__NEW_VALUE, oldNewValue, newNewValue);
 			if (msgs == null) {
 				msgs = notification;
 			} else {
 				msgs.add(notification);
 			}
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setNewValue(ModelElementId newNewValue) {
 		if (newNewValue != newValue)
 		{
 			NotificationChain msgs = null;
 			if (newValue != null) {
 				msgs = ((InternalEObject) newValue).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- OperationsPackage.SINGLE_REFERENCE_OPERATION__NEW_VALUE, null, msgs);
 			}
 			if (newNewValue != null) {
 				msgs = ((InternalEObject) newNewValue).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 					- OperationsPackage.SINGLE_REFERENCE_OPERATION__NEW_VALUE, null, msgs);
 			}
 			msgs = basicSetNewValue(newNewValue, msgs);
 			if (msgs != null) {
 				msgs.dispatch();
 			}
 		}
 		else if (eNotificationRequired()) {
 			eNotify(new ENotificationImpl(this, Notification.SET,
 				OperationsPackage.SINGLE_REFERENCE_OPERATION__NEW_VALUE, newNewValue, newNewValue));
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID)
 		{
 		case OperationsPackage.SINGLE_REFERENCE_OPERATION__OLD_VALUE:
 			return basicSetOldValue(null, msgs);
 		case OperationsPackage.SINGLE_REFERENCE_OPERATION__NEW_VALUE:
 			return basicSetNewValue(null, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID)
 		{
 		case OperationsPackage.SINGLE_REFERENCE_OPERATION__OLD_VALUE:
 			if (resolve) {
 				return getOldValue();
 			}
 			return basicGetOldValue();
 		case OperationsPackage.SINGLE_REFERENCE_OPERATION__NEW_VALUE:
 			if (resolve) {
 				return getNewValue();
 			}
 			return basicGetNewValue();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID)
 		{
 		case OperationsPackage.SINGLE_REFERENCE_OPERATION__OLD_VALUE:
 			setOldValue((ModelElementId) newValue);
 			return;
 		case OperationsPackage.SINGLE_REFERENCE_OPERATION__NEW_VALUE:
 			setNewValue((ModelElementId) newValue);
 			return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID)
 		{
 		case OperationsPackage.SINGLE_REFERENCE_OPERATION__OLD_VALUE:
 			setOldValue((ModelElementId) null);
 			return;
 		case OperationsPackage.SINGLE_REFERENCE_OPERATION__NEW_VALUE:
 			setNewValue((ModelElementId) null);
 			return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID)
 		{
 		case OperationsPackage.SINGLE_REFERENCE_OPERATION__OLD_VALUE:
 			return oldValue != null;
 		case OperationsPackage.SINGLE_REFERENCE_OPERATION__NEW_VALUE:
 			return newValue != null;
 		}
 		return super.eIsSet(featureID);
 	}
 
 	@SuppressWarnings("serial")
 	public void apply(IdEObjectCollection project) {
 		final EObject modelElement = getModelElement(project, getModelElementId());
 		EObject oldModelElement = getModelElement(project, getOldValue());
 		final EObject newModelElement = getModelElement(project, getNewValue());
 
 		if (modelElement == null) {
 			// silently fail
 			return;
 		}
 
 		EReference reference;
 
 		try {
 			// FIXME: updating of old value should be replaced by re-recording mechanism
 			reference = (EReference) this.getFeature(modelElement);
 			final EObject oldModelElementReferenceValue = (EObject) modelElement.eGet(reference);
 			if (oldModelElementReferenceValue != null) {
 				final ModelElementId updatedOldValue = project.getModelElementId(oldModelElementReferenceValue);
 				if (!updatedOldValue.equals(oldValue)) {
 					oldModelElement = oldModelElementReferenceValue;
 					setOldValue(updatedOldValue);
 				}
 			} else if (oldValue != null) {
				oldModelElement = null;
 				setOldValue(null);
 			}
 
 			switch (getUnset().getValue()) {
 			case UnsetType.IS_UNSET_VALUE:
 				modelElement.eUnset(reference);
 				break;
 			case UnsetType.NONE_VALUE:
 				modelElement.eSet(reference, newModelElement);
 				break;
 			case UnsetType.WAS_UNSET_VALUE:
 				modelElement.eSet(reference, newModelElement);
 				break;
 			}
 
 			// keep elements in the project if they are disconnected, if they
 			// really need to be deleted there will be a
 			// delete operation
 			if (reference.isContainer() && newModelElement == null) {
 				project.allocateModelElementIds(new LinkedHashMap<EObject, ModelElementId>() {
 					{
 						put(modelElement, getModelElementId());
 					}
 				});
 				project.addModelElement(modelElement);
 			} else if (reference.isContainment() && newModelElement == null && oldModelElement != null) {
 				final LinkedHashMap<EObject, ModelElementId> ids = new LinkedHashMap<EObject, ModelElementId>();
 				ids.put(oldModelElement, getOldValue());
 				project.allocateModelElementIds(ids);
 				project.addModelElement(oldModelElement);
 			}
 		} catch (final UnkownFeatureException e) {
 			// silently fail
 			return;
 		}
 
 	}
 
 	private EObject getModelElement(IdEObjectCollection collection, ModelElementId modelElementId) {
 		EObject modelElement = collection.getModelElement(modelElementId);
 
 		if (modelElement == null) {
 			modelElement = ((IdEObjectCollectionImpl) collection).getDeletedModelElement(modelElementId);
 		}
 
 		return modelElement;
 	}
 
 	@Override
 	public AbstractOperation reverse() {
 		final SingleReferenceOperation singleReferenceOperation = OperationsFactory.eINSTANCE
 			.createSingleReferenceOperation();
 		reverse(singleReferenceOperation);
 		if (getOldValue() != null) {
 			singleReferenceOperation.setNewValue(ModelUtil.clone(getOldValue()));
 		}
 		if (getNewValue() != null) {
 			singleReferenceOperation.setOldValue(ModelUtil.clone(getNewValue()));
 		}
 
 		setUnsetForReverseOperation(singleReferenceOperation);
 
 		return singleReferenceOperation;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.server.model.versioning.operations.ReferenceOperation#getOtherInvolvedModelElements()
 	 */
 	@Override
 	public Set<ModelElementId> getOtherInvolvedModelElements() {
 		final HashSet<ModelElementId> result = new LinkedHashSet<ModelElementId>();
 		final ModelElementId newValue = getNewValue();
 		if (newValue != null) {
 			result.add(newValue);
 		}
 		final ModelElementId oldValue = getOldValue();
 		if (oldValue != null) {
 			result.add(oldValue);
 		}
 		return result;
 	}
 
 } // SingleReferenceOperationImpl
