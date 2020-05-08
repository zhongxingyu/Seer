 /*******************************************************************************
  * Copyright (c) 2008, 2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.components.util;
 
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.eef.components.ComponentsPackage;
 import org.eclipse.emf.eef.components.EEFElement;
 import org.eclipse.emf.eef.components.PropertiesEditionComponent;
 import org.eclipse.emf.eef.components.PropertiesEditionContext;
 import org.eclipse.emf.eef.components.PropertiesEditionElement;
 import org.eclipse.emf.eef.components.PropertiesMultiEditionElement;
 import org.eclipse.emf.eef.mapping.AbstractElementBinding;
 import org.eclipse.emf.eef.mapping.AbstractPropertyBinding;
 import org.eclipse.emf.eef.mapping.Databinding;
import org.eclipse.emf.eef.mapping.DocumentedElement;
 import org.eclipse.emf.eef.mapping.EMFElementBinding;
 import org.eclipse.emf.eef.mapping.EMFMultiPropertiesBinding;
 import org.eclipse.emf.eef.mapping.EMFPropertyBinding;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Adapter Factory</b> for the model.
  * It provides an adapter <code>createXXX</code> method for each class of the model.
  * <!-- end-user-doc -->
  * @see org.eclipse.emf.eef.components.ComponentsPackage
  * @generated
  */
 public class ComponentsAdapterFactory extends AdapterFactoryImpl {
 	/**
 	 * The cached model package.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected static ComponentsPackage modelPackage;
 
 	/**
 	 * Creates an instance of the adapter factory.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ComponentsAdapterFactory() {
 		if (modelPackage == null) {
 			modelPackage = ComponentsPackage.eINSTANCE;
 		}
 	}
 
 	/**
 	 * Returns whether this factory is applicable for the type of the object.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
 	 * <!-- end-user-doc -->
 	 * @return whether this factory is applicable for the type of the object.
 	 * @generated
 	 */
 	@Override
 	public boolean isFactoryForType(Object object) {
 		if (object == modelPackage) {
 			return true;
 		}
 		if (object instanceof EObject) {
 			return ((EObject)object).eClass().getEPackage() == modelPackage;
 		}
 		return false;
 	}
 
 	/**
 	 * The switch that delegates to the <code>createXXX</code> methods.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected ComponentsSwitch<Adapter> modelSwitch = new ComponentsSwitch<Adapter>() {
 		@Override
 		public Adapter casePropertiesEditionContext(PropertiesEditionContext object) {
 			return createPropertiesEditionContextAdapter();
 		}
 
 		@Override
 		public Adapter casePropertiesEditionComponent(PropertiesEditionComponent object) {
 			return createPropertiesEditionComponentAdapter();
 		}
 
 		@Override
 		public Adapter casePropertiesEditionElement(PropertiesEditionElement object) {
 			return createPropertiesEditionElementAdapter();
 		}
 
 		@Override
 		public Adapter casePropertiesMultiEditionElement(PropertiesMultiEditionElement object) {
 			return createPropertiesMultiEditionElementAdapter();
 		}
 
 		@Override
 		public Adapter caseEEFElement(EEFElement object) {
 			return createEEFElementAdapter();
 		}
 
 		@Override
 		public Adapter caseDocumentedElement(DocumentedElement object) {
 			return createDocumentedElementAdapter();
 		}
 
 		@Override
 		public Adapter caseDatabinding(Databinding object) {
 			return createDatabindingAdapter();
 		}
 
 		@Override
 		public Adapter caseAbstractElementBinding(AbstractElementBinding object) {
 			return createAbstractElementBindingAdapter();
 		}
 
 		@Override
 		public Adapter caseEMFElementBinding(EMFElementBinding object) {
 			return createEMFElementBindingAdapter();
 		}
 
 		@Override
 		public Adapter caseAbstractPropertyBinding(AbstractPropertyBinding object) {
 			return createAbstractPropertyBindingAdapter();
 		}
 
 		@Override
 		public Adapter caseEMFPropertyBinding(EMFPropertyBinding object) {
 			return createEMFPropertyBindingAdapter();
 		}
 
 		@Override
 		public Adapter caseEMFMultiPropertiesBinding(EMFMultiPropertiesBinding object) {
 			return createEMFMultiPropertiesBindingAdapter();
 		}
 
 		@Override
 		public Adapter defaultCase(EObject object) {
 			return createEObjectAdapter();
 		}
 	};
 
 	/**
 	 * Creates an adapter for the <code>target</code>.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param target the object to adapt.
 	 * @return the adapter for the <code>target</code>.
 	 * @generated
 	 */
 	@Override
 	public Adapter createAdapter(Notifier target) {
 		return modelSwitch.doSwitch((EObject)target);
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.components.PropertiesEditionContext <em>Properties Edition Context</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.components.PropertiesEditionContext
 	 * @generated
 	 */
 	public Adapter createPropertiesEditionContextAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.components.PropertiesEditionComponent <em>Properties Edition Component</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.components.PropertiesEditionComponent
 	 * @generated
 	 */
 	public Adapter createPropertiesEditionComponentAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.components.PropertiesEditionElement <em>Properties Edition Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.components.PropertiesEditionElement
 	 * @generated
 	 */
 	public Adapter createPropertiesEditionElementAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.components.PropertiesMultiEditionElement <em>Properties Multi Edition Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.components.PropertiesMultiEditionElement
 	 * @generated
 	 */
 	public Adapter createPropertiesMultiEditionElementAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.components.EEFElement <em>EEF Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.components.EEFElement
 	 * @generated
 	 */
 	public Adapter createEEFElementAdapter() {
 		return null;
 	}
 
 	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.mapping.DocumentedElement <em>Documented Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
	 * @see org.eclipse.emf.eef.mapping.DocumentedElement
 	 * @generated
 	 */
 	public Adapter createDocumentedElementAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.mapping.Databinding <em>Databinding</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.mapping.Databinding
 	 * @generated
 	 */
 	public Adapter createDatabindingAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.mapping.AbstractElementBinding <em>Abstract Element Binding</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.mapping.AbstractElementBinding
 	 * @generated
 	 */
 	public Adapter createAbstractElementBindingAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.mapping.EMFElementBinding <em>EMF Element Binding</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.mapping.EMFElementBinding
 	 * @generated
 	 */
 	public Adapter createEMFElementBindingAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.mapping.AbstractPropertyBinding <em>Abstract Property Binding</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.mapping.AbstractPropertyBinding
 	 * @generated
 	 */
 	public Adapter createAbstractPropertyBindingAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.mapping.EMFPropertyBinding <em>EMF Property Binding</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.mapping.EMFPropertyBinding
 	 * @generated
 	 */
 	public Adapter createEMFPropertyBindingAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.mapping.EMFMultiPropertiesBinding <em>EMF Multi Properties Binding</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.mapping.EMFMultiPropertiesBinding
 	 * @generated
 	 */
 	public Adapter createEMFMultiPropertiesBindingAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for the default case.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @generated
 	 */
 	public Adapter createEObjectAdapter() {
 		return null;
 	}
 
 } //ComponentsAdapterFactory
