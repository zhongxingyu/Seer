 /*******************************************************************************
 * Copyright (c) 2008, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.extended.editor.util;
 
 
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.eef.extended.editor.AllResourcesRootsRelativeInput;
 import org.eclipse.emf.eef.extended.editor.DynamicEEFEditorContribution;
 import org.eclipse.emf.eef.extended.editor.EEFEditorContribution;
 import org.eclipse.emf.eef.extended.editor.EEFEditorContributions;
 import org.eclipse.emf.eef.extended.editor.EEFEditorPages;
 import org.eclipse.emf.eef.extended.editor.EEFMasterPage;
 import org.eclipse.emf.eef.extended.editor.EEFPage;
 import org.eclipse.emf.eef.extended.editor.EEFPageInput;
 import org.eclipse.emf.eef.extended.editor.EditorPackage;
 import org.eclipse.emf.eef.extended.editor.FirstResourceRootRelativeInput;
 import org.eclipse.emf.eef.extended.editor.MasterDetailsPage;
 import org.eclipse.emf.eef.extended.editor.PartFilter;
 import org.eclipse.emf.eef.extended.editor.ReferenceableObject;
 import org.eclipse.emf.eef.extended.editor.StandardFormPage;
 import org.eclipse.emf.eef.extended.editor.StaticEEFEditorContribution;
 import org.eclipse.emf.eef.extended.editor.TreeMasterPage;
 import org.eclipse.emf.eef.mapping.AbstractElementBinding;
 import org.eclipse.emf.eef.mapping.Category;
 import org.eclipse.emf.eef.mapping.EMFElementBinding;
 import org.eclipse.emf.eef.mapping.filters.BindingFilter;
 import org.eclipse.emf.eef.views.Container;
 import org.eclipse.emf.eef.views.DocumentedElement;
 import org.eclipse.emf.eef.views.IdentifiedElement;
 import org.eclipse.emf.eef.views.View;
 import org.eclipse.emf.eef.views.ViewElement;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Adapter Factory</b> for the model.
  * It provides an adapter <code>createXXX</code> method for each class of the model.
  * <!-- end-user-doc -->
  * @see org.eclipse.emf.eef.extended.editor.EditorPackage
  * @generated
  */
 public class EditorAdapterFactory extends AdapterFactoryImpl {
 	/**
 	 * The cached model package.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected static EditorPackage modelPackage;
 
 	/**
 	 * Creates an instance of the adapter factory.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EditorAdapterFactory() {
 		if (modelPackage == null) {
 			modelPackage = EditorPackage.eINSTANCE;
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
 	protected EditorSwitch<Adapter> modelSwitch =
 		new EditorSwitch<Adapter>() {
 			@Override
 			public Adapter caseEEFEditorContribution(EEFEditorContribution object) {
 				return createEEFEditorContributionAdapter();
 			}
 			@Override
 			public Adapter caseEEFPage(EEFPage object) {
 				return createEEFPageAdapter();
 			}
 			@Override
 			public Adapter caseStandardFormPage(StandardFormPage object) {
 				return createStandardFormPageAdapter();
 			}
 			@Override
 			public Adapter caseMasterDetailsPage(MasterDetailsPage object) {
 				return createMasterDetailsPageAdapter();
 			}
 			@Override
 			public Adapter caseEEFMasterPage(EEFMasterPage object) {
 				return createEEFMasterPageAdapter();
 			}
 			@Override
 			public Adapter caseTreeMasterPage(TreeMasterPage object) {
 				return createTreeMasterPageAdapter();
 			}
 			@Override
 			public Adapter caseEEFPageInput(EEFPageInput object) {
 				return createEEFPageInputAdapter();
 			}
 			@Override
 			public Adapter caseAllResourcesRootsRelativeInput(AllResourcesRootsRelativeInput object) {
 				return createAllResourcesRootsRelativeInputAdapter();
 			}
 			@Override
 			public Adapter caseFirstResourceRootRelativeInput(FirstResourceRootRelativeInput object) {
 				return createFirstResourceRootRelativeInputAdapter();
 			}
 			@Override
 			public Adapter casePartFilter(PartFilter object) {
 				return createPartFilterAdapter();
 			}
 			@Override
 			public Adapter caseEEFEditorContributions(EEFEditorContributions object) {
 				return createEEFEditorContributionsAdapter();
 			}
 			@Override
 			public Adapter caseEEFEditorPages(EEFEditorPages object) {
 				return createEEFEditorPagesAdapter();
 			}
 			@Override
 			public Adapter caseStaticEEFEditorContribution(StaticEEFEditorContribution object) {
 				return createStaticEEFEditorContributionAdapter();
 			}
 			@Override
 			public Adapter caseDynamicEEFEditorContribution(DynamicEEFEditorContribution object) {
 				return createDynamicEEFEditorContributionAdapter();
 			}
 			@Override
 			public Adapter caseReferenceableObject(ReferenceableObject object) {
 				return createReferenceableObjectAdapter();
 			}
 			@Override
 			public Adapter caseDocumentedElement(DocumentedElement object) {
 				return createDocumentedElementAdapter();
 			}
 			@Override
 			public Adapter caseViewElement(ViewElement object) {
 				return createViewElementAdapter();
 			}
 			@Override
 			public Adapter caseIdentifiedElement(IdentifiedElement object) {
 				return createIdentifiedElementAdapter();
 			}
 			@Override
 			public Adapter caseContainer(Container object) {
 				return createContainerAdapter();
 			}
 			@Override
 			public Adapter caseView(View object) {
 				return createViewAdapter();
 			}
 			@Override
 			public Adapter caseMapping_DocumentedElement(org.eclipse.emf.eef.mapping.DocumentedElement object) {
 				return createMapping_DocumentedElementAdapter();
 			}
 			@Override
 			public Adapter caseBindingFilter(BindingFilter object) {
 				return createBindingFilterAdapter();
 			}
 			@Override
 			public Adapter caseCategory(Category object) {
 				return createCategoryAdapter();
 			}
 			@Override
 			public Adapter caseViews_Category(org.eclipse.emf.eef.views.Category object) {
 				return createViews_CategoryAdapter();
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
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.extended.editor.EEFEditorContribution <em>EEF Editor Contribution</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.extended.editor.EEFEditorContribution
 	 * @generated
 	 */
 	public Adapter createEEFEditorContributionAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.extended.editor.EEFPage <em>EEF Page</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.extended.editor.EEFPage
 	 * @generated
 	 */
 	public Adapter createEEFPageAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.extended.editor.StandardFormPage <em>Standard Form Page</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.extended.editor.StandardFormPage
 	 * @generated
 	 */
 	public Adapter createStandardFormPageAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.extended.editor.MasterDetailsPage <em>Master Details Page</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.extended.editor.MasterDetailsPage
 	 * @generated
 	 */
 	public Adapter createMasterDetailsPageAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.extended.editor.EEFMasterPage <em>EEF Master Page</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.extended.editor.EEFMasterPage
 	 * @generated
 	 */
 	public Adapter createEEFMasterPageAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.extended.editor.TreeMasterPage <em>Tree Master Page</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.extended.editor.TreeMasterPage
 	 * @generated
 	 */
 	public Adapter createTreeMasterPageAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.extended.editor.EEFPageInput <em>EEF Page Input</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.extended.editor.EEFPageInput
 	 * @generated
 	 */
 	public Adapter createEEFPageInputAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.extended.editor.AllResourcesRootsRelativeInput <em>All Resources Roots Relative Input</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.extended.editor.AllResourcesRootsRelativeInput
 	 * @generated
 	 */
 	public Adapter createAllResourcesRootsRelativeInputAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.extended.editor.FirstResourceRootRelativeInput <em>First Resource Root Relative Input</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.extended.editor.FirstResourceRootRelativeInput
 	 * @generated
 	 */
 	public Adapter createFirstResourceRootRelativeInputAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.extended.editor.PartFilter <em>Part Filter</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.extended.editor.PartFilter
 	 * @generated
 	 */
 	public Adapter createPartFilterAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.extended.editor.EEFEditorContributions <em>EEF Editor Contributions</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.extended.editor.EEFEditorContributions
 	 * @generated
 	 */
 	public Adapter createEEFEditorContributionsAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.extended.editor.EEFEditorPages <em>EEF Editor Pages</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.extended.editor.EEFEditorPages
 	 * @generated
 	 */
 	public Adapter createEEFEditorPagesAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.extended.editor.StaticEEFEditorContribution <em>Static EEF Editor Contribution</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.extended.editor.StaticEEFEditorContribution
 	 * @generated
 	 */
 	public Adapter createStaticEEFEditorContributionAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.extended.editor.DynamicEEFEditorContribution <em>Dynamic EEF Editor Contribution</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.extended.editor.DynamicEEFEditorContribution
 	 * @generated
 	 */
 	public Adapter createDynamicEEFEditorContributionAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.extended.editor.ReferenceableObject <em>Referenceable Object</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.extended.editor.ReferenceableObject
 	 * @generated
 	 */
 	public Adapter createReferenceableObjectAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.views.DocumentedElement <em>Documented Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.views.DocumentedElement
 	 * @generated
 	 */
 	public Adapter createDocumentedElementAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.views.ViewElement <em>View Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.views.ViewElement
 	 * @generated
 	 */
 	public Adapter createViewElementAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.views.IdentifiedElement <em>Identified Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.views.IdentifiedElement
 	 * @generated
 	 */
 	public Adapter createIdentifiedElementAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.views.Container <em>Container</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.views.Container
 	 * @generated
 	 */
 	public Adapter createContainerAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.views.View <em>View</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.views.View
 	 * @generated
 	 */
 	public Adapter createViewAdapter() {
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
 	public Adapter createMapping_DocumentedElementAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.mapping.filters.BindingFilter <em>Binding Filter</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.mapping.filters.BindingFilter
 	 * @generated
 	 */
 	public Adapter createBindingFilterAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.mapping.Category <em>Category</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.mapping.Category
 	 * @generated
 	 */
 	public Adapter createCategoryAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.eclipse.emf.eef.views.Category <em>Category</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.eclipse.emf.eef.views.Category
 	 * @generated
 	 */
 	public Adapter createViews_CategoryAdapter() {
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
 
 } //EditorAdapterFactory
