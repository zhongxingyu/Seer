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
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.EcoreEMap;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.emfstore.common.model.IdEObjectCollection;
 import org.eclipse.emf.emfstore.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.CreateDeleteOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.OperationsFactory;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.OperationsPackage;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.ReferenceOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.UnkownFeatureException;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object ' <em><b>Create Delete Operation</b></em>'. <!--
  * end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  * <li>
  * {@link org.eclipse.emf.emfstore.server.model.versioning.operations.impl.CreateDeleteOperationImpl#isDelete
  * <em>Delete</em>}</li>
  * <li>
  * {@link org.eclipse.emf.emfstore.server.model.versioning.operations.impl.CreateDeleteOperationImpl#getModelElement
  * <em>Model Element</em>}</li>
  * <li>
  * {@link org.eclipse.emf.emfstore.server.model.versioning.operations.impl.CreateDeleteOperationImpl#getSubOperations
  * <em>Sub Operations</em>}</li>
  * <li>
  * {@link org.eclipse.emf.emfstore.server.model.versioning.operations.impl.CreateDeleteOperationImpl#getEObjectToIdMap
  * <em>EObject To Id Map</em>}</li>
  * </ul>
  * </p>
  * 
  * @generated
  */
 public class CreateDeleteOperationImpl extends AbstractOperationImpl implements CreateDeleteOperation {
 
 	public void apply(IdEObjectCollection project) {
 		if (isDelete()) {
 			if (!project.contains(getModelElementId())) {
 				// silently fail
 				return;
 			}
 
 			EObject localModelElement = project.getModelElement(getModelElementId());
 
 			for (AbstractOperation op : getSubOperations()) {
 				op.apply(project);
 			}
 
 			// remove model element from its parent, this should only apply if
 			// the local model element in directly contained in the project
 			if (localModelElement.eContainmentFeature() != null) {
 				EReference eContainmentFeature = localModelElement.eContainmentFeature();
 				if (eContainmentFeature.isMany()) {
 					((List<?>) localModelElement.eContainer().eGet(eContainmentFeature)).remove(localModelElement);
 				} else {
 					localModelElement.eContainer().eSet(eContainmentFeature, null);
 				}
 			}
 		} else {
 			if (project.contains(getModelElementId())) {
 				// silently fail
 				return;
 			}
 
 			// clone operation in order to retrieve the model element
 			CreateDeleteOperationImpl clone = ModelUtil.clone(this);
 
 			EObject element = getModelElement();
 			List<EObject> allContainedModelElements = ModelUtil.getAllContainedModelElementsAsList(element, false);
 			allContainedModelElements.add(element);
 			EObject copiedElement = ModelUtil.clone(element);
 			List<EObject> copiedAllContainedModelElements = ModelUtil.getAllContainedModelElementsAsList(copiedElement,
 				false);
 			copiedAllContainedModelElements.add(copiedElement);
 			clone.getEObjectToIdMap().clear();
 
 			for (int i = 0; i < allContainedModelElements.size(); i++) {
 				EObject child = allContainedModelElements.get(i);
 				EObject copiedChild = copiedAllContainedModelElements.get(i);
 				ModelElementId childId = ModelUtil.clone(getEObjectToIdMap().get(child));
 
 				if (ModelUtil.isIgnoredDatatype(child)) {
 					continue;
 				}
 
 				if (childId.equals(clone.getModelElementId())) {
 					clone.setModelElement(copiedChild);
 				}
 				clone.getEObjectToIdMap().put(copiedChild, childId);
 			}
 
 			project.addModelElement(clone.getModelElement(), clone.getEObjectToIdMap().map());
 
 			for (ReferenceOperation operation : getSubOperations()) {
 				operation.apply(project);
 			}
 		}
 	}
 
 	@Override
 	public AbstractOperation reverse() {
 		// TODO: see comment in checkValidity
 		// checkValidity();
 		CreateDeleteOperation createDeleteOperation = OperationsFactory.eINSTANCE.createCreateDeleteOperation();
 		super.reverse(createDeleteOperation);
 		createDeleteOperation.setDelete(!this.isDelete());
 
 		EObject element = getModelElement();
 		List<EObject> allContainedModelElements = ModelUtil.getAllContainedModelElementsAsList(element, false);
 		allContainedModelElements.add(element);
 		EObject copiedElement = ModelUtil.clone(element);
 		createDeleteOperation.setModelElement(copiedElement);
 		createDeleteOperation.setModelElementId(ModelUtil.clone(this.getModelElementId()));
 		List<EObject> copiedAllContainedModelElements = ModelUtil.getAllContainedModelElementsAsList(copiedElement,
 			false);
 		copiedAllContainedModelElements.add(copiedElement);
 
 		for (int i = 0; i < allContainedModelElements.size(); i++) {
 			EObject child = allContainedModelElements.get(i);
 			EObject copiedChild = copiedAllContainedModelElements.get(i);
 			ModelElementId childId = ModelUtil.clone(getEObjectToIdMap().get(child));
 			((CreateDeleteOperationImpl) createDeleteOperation).getEObjectToIdMap().put(copiedChild, childId);
 		}
 
 		EList<ReferenceOperation> clonedSubOperations = createDeleteOperation.getSubOperations();
 		for (ReferenceOperation operation : getSubOperations()) {
 			clonedSubOperations.add(0, (ReferenceOperation) operation.reverse());
 		}
 		return createDeleteOperation;
 	}
 
 	/**
 	 * The default value of the '{@link #isDelete() <em>Delete</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #isDelete()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean DELETE_EDEFAULT = false;
 
 	/**
 	 * The cached value of the '{@link #isDelete() <em>Delete</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #isDelete()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean delete = DELETE_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getModelElement()
 	 * <em>Model Element</em>}' containment reference. <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getModelElement()
 	 * @generated
 	 * @ordered
 	 */
 	protected EObject modelElement;
 
 	/**
 	 * The cached value of the '{@link #getSubOperations()
 	 * <em>Sub Operations</em>}' containment reference list. <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * 
 	 * @see #getSubOperations()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<ReferenceOperation> subOperations;
 
 	/**
 	 * The cached value of the '{@link #getEObjectToIdMap()
 	 * <em>EObject To Id Map</em>}' map. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getEObjectToIdMap()
 	 * @generated
 	 * @ordered
 	 */
 	protected EMap<EObject, ModelElementId> eObjectToIdMap;
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected CreateDeleteOperationImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return OperationsPackage.Literals.CREATE_DELETE_OPERATION;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public boolean isDelete() {
 		return delete;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setDelete(boolean newDelete) {
 		boolean oldDelete = delete;
 		delete = newDelete;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, OperationsPackage.CREATE_DELETE_OPERATION__DELETE,
 				oldDelete, delete));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EObject getModelElement() {
 		if (modelElement != null && modelElement.eIsProxy()) {
 			InternalEObject oldModelElement = (InternalEObject) modelElement;
 			modelElement = eResolveProxy(oldModelElement);
 			if (modelElement != oldModelElement) {
 				InternalEObject newModelElement = (InternalEObject) modelElement;
 				NotificationChain msgs = oldModelElement.eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- OperationsPackage.CREATE_DELETE_OPERATION__MODEL_ELEMENT, null, null);
 				if (newModelElement.eInternalContainer() == null) {
 					msgs = newModelElement.eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 						- OperationsPackage.CREATE_DELETE_OPERATION__MODEL_ELEMENT, null, msgs);
 				}
 				if (msgs != null)
 					msgs.dispatch();
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE,
 						OperationsPackage.CREATE_DELETE_OPERATION__MODEL_ELEMENT, oldModelElement, modelElement));
 			}
 		}
 		return modelElement;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EObject basicGetModelElement() {
 		return modelElement;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public NotificationChain basicSetModelElement(EObject newModelElement, NotificationChain msgs) {
 		EObject oldModelElement = modelElement;
 		modelElement = newModelElement;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
 				OperationsPackage.CREATE_DELETE_OPERATION__MODEL_ELEMENT, oldModelElement, newModelElement);
 			if (msgs == null)
 				msgs = notification;
 			else
 				msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setModelElement(EObject newModelElement) {
 		if (newModelElement != modelElement) {
 			NotificationChain msgs = null;
 			if (modelElement != null)
 				msgs = ((InternalEObject) modelElement).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- OperationsPackage.CREATE_DELETE_OPERATION__MODEL_ELEMENT, null, msgs);
 			if (newModelElement != null)
 				msgs = ((InternalEObject) newModelElement).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 					- OperationsPackage.CREATE_DELETE_OPERATION__MODEL_ELEMENT, null, msgs);
 			msgs = basicSetModelElement(newModelElement, msgs);
 			if (msgs != null)
 				msgs.dispatch();
 		} else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET,
 				OperationsPackage.CREATE_DELETE_OPERATION__MODEL_ELEMENT, newModelElement, newModelElement));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EList<ReferenceOperation> getSubOperations() {
 		if (subOperations == null) {
 			subOperations = new EObjectContainmentEList.Resolving<ReferenceOperation>(ReferenceOperation.class, this,
 				OperationsPackage.CREATE_DELETE_OPERATION__SUB_OPERATIONS);
 		}
 		return subOperations;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EMap<EObject, ModelElementId> getEObjectToIdMap() {
 		if (eObjectToIdMap == null) {
 			eObjectToIdMap = new EcoreEMap<EObject, ModelElementId>(
 				OperationsPackage.Literals.EOBJECT_TO_MODEL_ELEMENT_ID_MAP, EObjectToModelElementIdMapImpl.class, this,
 				OperationsPackage.CREATE_DELETE_OPERATION__EOBJECT_TO_ID_MAP);
 		}
 		return eObjectToIdMap;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 		case OperationsPackage.CREATE_DELETE_OPERATION__MODEL_ELEMENT:
 			return basicSetModelElement(null, msgs);
 		case OperationsPackage.CREATE_DELETE_OPERATION__SUB_OPERATIONS:
 			return ((InternalEList<?>) getSubOperations()).basicRemove(otherEnd, msgs);
 		case OperationsPackage.CREATE_DELETE_OPERATION__EOBJECT_TO_ID_MAP:
 			return ((InternalEList<?>) getEObjectToIdMap()).basicRemove(otherEnd, msgs);
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
 		switch (featureID) {
 		case OperationsPackage.CREATE_DELETE_OPERATION__DELETE:
 			return isDelete();
 		case OperationsPackage.CREATE_DELETE_OPERATION__MODEL_ELEMENT:
 			if (resolve)
 				return getModelElement();
 			return basicGetModelElement();
 		case OperationsPackage.CREATE_DELETE_OPERATION__SUB_OPERATIONS:
 			return getSubOperations();
 		case OperationsPackage.CREATE_DELETE_OPERATION__EOBJECT_TO_ID_MAP:
 			if (coreType)
 				return getEObjectToIdMap();
 			else
 				return getEObjectToIdMap().map();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 		case OperationsPackage.CREATE_DELETE_OPERATION__DELETE:
 			setDelete((Boolean) newValue);
 			return;
 		case OperationsPackage.CREATE_DELETE_OPERATION__MODEL_ELEMENT:
 			setModelElement((EObject) newValue);
 			return;
 		case OperationsPackage.CREATE_DELETE_OPERATION__SUB_OPERATIONS:
 			getSubOperations().clear();
 			getSubOperations().addAll((Collection<? extends ReferenceOperation>) newValue);
 			return;
 		case OperationsPackage.CREATE_DELETE_OPERATION__EOBJECT_TO_ID_MAP:
 			((EStructuralFeature.Setting) getEObjectToIdMap()).set(newValue);
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
 		switch (featureID) {
 		case OperationsPackage.CREATE_DELETE_OPERATION__DELETE:
 			setDelete(DELETE_EDEFAULT);
 			return;
 		case OperationsPackage.CREATE_DELETE_OPERATION__MODEL_ELEMENT:
 			setModelElement((EObject) null);
 			return;
 		case OperationsPackage.CREATE_DELETE_OPERATION__SUB_OPERATIONS:
 			getSubOperations().clear();
 			return;
 		case OperationsPackage.CREATE_DELETE_OPERATION__EOBJECT_TO_ID_MAP:
 			getEObjectToIdMap().clear();
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
 		switch (featureID) {
 		case OperationsPackage.CREATE_DELETE_OPERATION__DELETE:
 			return delete != DELETE_EDEFAULT;
 		case OperationsPackage.CREATE_DELETE_OPERATION__MODEL_ELEMENT:
 			return modelElement != null;
 		case OperationsPackage.CREATE_DELETE_OPERATION__SUB_OPERATIONS:
 			return subOperations != null && !subOperations.isEmpty();
 		case OperationsPackage.CREATE_DELETE_OPERATION__EOBJECT_TO_ID_MAP:
 			return eObjectToIdMap != null && !eObjectToIdMap.isEmpty();
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy())
 			return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (delete: ");
 		result.append(delete);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * @see org.eclipse.emf.emfstore.server.model.versioning.operations.impl.AbstractOperationImpl#getOtherInvolvedModelElements()
 	 */
 	@Override
 	public Set<ModelElementId> getOtherInvolvedModelElements() {
 		Set<ModelElementId> result = new HashSet<ModelElementId>();
 		result.addAll(getEObjectToIdMap().values());
 		for (ReferenceOperation operation : getSubOperations()) {
 			result.addAll(operation.getAllInvolvedModelElements());
 		}
 		return result;
 	}
 
 	// public Set<ModelElementId> getAllDeletedModelElements() {
 	// Set<ModelElementId> result = new HashSet<ModelElementId>();
 	// for (EObject modelElement :
 	// ModelUtil.getAllContainedModelElements(getModelElement(), false)) {
 	//
 	// Project p = ModelUtil.getProject(modelElement);
 	// if (p != null) {
 	// result.add(p.getModelElementId(modelElement));
 	// }
 	//
 	// }
 	// for (ModelElementId id : getEobjectsIdMap().values()) {
 	// result.add(id);
 	// }
 	// return result;
 	// }
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation#getLeafOperations()
 	 */
 	public List<AbstractOperation> getLeafOperations() {
 		List<AbstractOperation> result = new ArrayList<AbstractOperation>(getSubOperations().size() + 1);
 		CreateDeleteOperation createDeleteClone = ModelUtil.clone(this);
 		createDeleteClone.getSubOperations().clear();
 		result.add(createDeleteClone);
 		result.addAll(getSubOperations());
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @generated NOT
 	 * @see org.eclipse.emf.emfstore.server.model.versioning.operations.CreateDeleteOperation#getParentElement(org.eclipse.emf.emfstore.common.model.Project)
 	 */
 	public ModelElementId getParentofDeletedElement(Project project) {
 
 		EList<ReferenceOperation> referenceOperations = getSubOperations();
 		if (referenceOperations.size() == 0) {
 			return null;
 		}
 
 		ReferenceOperation lastReferenceOperation = referenceOperations.get(referenceOperations.size() - 1);
 
 		try {
 			EStructuralFeature feature = lastReferenceOperation.getFeature(project);
 			if (!(feature instanceof EReference)) {
 				return null;
 			}
 			EReference reference = (EReference) feature;
 			// reference is from parent side, so parent is the element that is
 			// changed by the last ref op
 			if (reference.isContainment()) {
 				if (lastReferenceOperation.getOtherInvolvedModelElements().contains(getModelElementId())) {
 					return lastReferenceOperation.getModelElementId();
 				}
 				return null;
 				// reference is from child side, so parent is the only element
 				// in other involved of the ref op
 			} else if (reference.isContainer()) {
 				if (lastReferenceOperation.getModelElementId().equals(getModelElementId())) {
 					Set<ModelElementId> otherInvolvedModelElements = lastReferenceOperation
 						.getOtherInvolvedModelElements();
 					if (otherInvolvedModelElements.size() > 0) {
 						return otherInvolvedModelElements.iterator().next();
 					}
 				}
 			}
 			return null;
 
 		} catch (UnkownFeatureException e) {
 			// parent does not exist any more or feature does not exist
 			return null;
 		}
 	}
 
 } // CreateDeleteOperationImpl
