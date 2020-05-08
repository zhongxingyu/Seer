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
 package org.eclipse.emf.emfstore.common.model.impl;
 
 import java.util.Collection;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.emfstore.common.model.ModelPackage;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.common.model.util.IdEObjectCollectionChangeObserver;
 
 /**
  * @author koegel
  * @author naughton
  * @author emueller
  * 
  * @generated NOT
  */
 public class ProjectImpl extends NotifiableIdEObjectCollectionImpl implements Project {
 
 	/**
 	 * The cached value of the '{@link #getModelElements()
 	 * <em>Model Elements</em>}' containment reference list. <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * 
 	 * @see #getModelElements()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<EObject> modelElements;
 
 	/**
 	 * The cached value of the '{@link #getCutElements() <em>Cut Elements</em>}'
 	 * containment reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getCutElements()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<EObject> cutElements;
 
 	// begin of custom code
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected ProjectImpl() {
 		super();
 	}
 
 	// end of custom code
 
 	/**
 	 * <!-- begin-user-doc --> .<!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return ModelPackage.Literals.PROJECT;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 
 	@Override
 	public EList<EObject> getModelElements() {
 		if (modelElements == null) {
 			modelElements = new EObjectContainmentEList.Resolving<EObject>(EObject.class, this,
 				ModelPackage.PROJECT__MODEL_ELEMENTS);
 		}
 		return modelElements;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EList<EObject> getCutElements() {
 		if (cutElements == null) {
 			cutElements = new EObjectContainmentEList.Resolving<EObject>(EObject.class, this,
 				ModelPackage.PROJECT__CUT_ELEMENTS);
 		}
 		return cutElements;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 		case ModelPackage.PROJECT__MODEL_ELEMENTS:
 			return ((InternalEList<?>) getModelElements()).basicRemove(otherEnd, msgs);
 		case ModelPackage.PROJECT__CUT_ELEMENTS:
 			return ((InternalEList<?>) getCutElements()).basicRemove(otherEnd, msgs);
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
 		case ModelPackage.PROJECT__MODEL_ELEMENTS:
 			return getModelElements();
 		case ModelPackage.PROJECT__CUT_ELEMENTS:
 			return getCutElements();
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
 		case ModelPackage.PROJECT__MODEL_ELEMENTS:
 			getModelElements().clear();
 			getModelElements().addAll((Collection<? extends EObject>) newValue);
 			return;
 		case ModelPackage.PROJECT__CUT_ELEMENTS:
 			getCutElements().clear();
 			getCutElements().addAll((Collection<? extends EObject>) newValue);
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
 		case ModelPackage.PROJECT__MODEL_ELEMENTS:
 			getModelElements().clear();
 			return;
 		case ModelPackage.PROJECT__CUT_ELEMENTS:
 			getCutElements().clear();
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
 		case ModelPackage.PROJECT__MODEL_ELEMENTS:
 			return modelElements != null && !modelElements.isEmpty();
 		case ModelPackage.PROJECT__CUT_ELEMENTS:
 			return cutElements != null && !cutElements.isEmpty();
 		}
 		return super.eIsSet(featureID);
 	}
 
 	// begin of custom code
 
 	/**
 	 * this methods implements the adapter interface which is needed by the
 	 * navigator.
 	 * 
 	 * @param adapter
 	 *            the adapter class
 	 * @return the adapter
 	 * @author shterev
 	 */
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
 		return Platform.getAdapterManager().getAdapter(this, adapter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.model.Project#delete()
 	 */
 	public void delete() {
 		EObjectChangeObserverNotificationCommand command = new EObjectChangeObserverNotificationCommand() {
 			public void run(IdEObjectCollectionChangeObserver projectChangeObserver) {
 				IdEObjectCollectionChangeObserver observer = projectChangeObserver;
 				observer.collectionDeleted(ProjectImpl.this);
 			}
 		};
 		notifyIdEObjectCollectionChangeObservers(command);
 		dispose();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.model.impl.IdEObjectCollectionImpl#setModelElements(java.util.Collection)
	 * @generated NOT
 	 */
 	@Override
 	protected void setModelElements(EList<EObject> modelElements) {
 		this.modelElements = modelElements;
 	}
 
 }
