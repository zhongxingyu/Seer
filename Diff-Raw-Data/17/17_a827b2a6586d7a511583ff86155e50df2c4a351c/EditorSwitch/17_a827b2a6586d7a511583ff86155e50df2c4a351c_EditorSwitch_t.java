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
 
 import java.util.List;
 
 import org.eclipse.emf.ecore.EClass;
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
  * The <b>Switch</b> for the model's inheritance hierarchy.
  * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
  * to invoke the <code>caseXXX</code> method for each class of the model,
  * starting with the actual class of the object
  * and proceeding up the inheritance hierarchy
  * until a non-null result is returned,
  * which is the result of the switch.
  * <!-- end-user-doc -->
  * @see org.eclipse.emf.eef.extended.editor.EditorPackage
  * @generated
  */
 public class EditorSwitch<T> {
 	/**
 	 * The cached model package
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected static EditorPackage modelPackage;
 
 	/**
 	 * Creates an instance of the switch.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EditorSwitch() {
 		if (modelPackage == null) {
 			modelPackage = EditorPackage.eINSTANCE;
 		}
 	}
 
 	/**
 	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the first non-null result returned by a <code>caseXXX</code> call.
 	 * @generated
 	 */
 	public T doSwitch(EObject theEObject) {
 		return doSwitch(theEObject.eClass(), theEObject);
 	}
 
 	/**
 	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the first non-null result returned by a <code>caseXXX</code> call.
 	 * @generated
 	 */
 	protected T doSwitch(EClass theEClass, EObject theEObject) {
 		if (theEClass.eContainer() == modelPackage) {
 			return doSwitch(theEClass.getClassifierID(), theEObject);
 		}
 		else {
 			List<EClass> eSuperTypes = theEClass.getESuperTypes();
 			return
 				eSuperTypes.isEmpty() ?
 					defaultCase(theEObject) :
 					doSwitch(eSuperTypes.get(0), theEObject);
 		}
 	}
 
 	/**
 	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the first non-null result returned by a <code>caseXXX</code> call.
 	 * @generated
 	 */
 	protected T doSwitch(int classifierID, EObject theEObject) {
 		switch (classifierID) {
 			case EditorPackage.EEF_EDITOR_CONTRIBUTION: {
 				EEFEditorContribution eefEditorContribution = (EEFEditorContribution)theEObject;
 				T result = caseEEFEditorContribution(eefEditorContribution);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case EditorPackage.EEF_PAGE: {
 				EEFPage eefPage = (EEFPage)theEObject;
 				T result = caseEEFPage(eefPage);
 				if (result == null) result = caseView(eefPage);
 				if (result == null) result = caseContainer(eefPage);
 				if (result == null) result = caseViewElement(eefPage);
 				if (result == null) result = caseIdentifiedElement(eefPage);
 				if (result == null) result = caseDocumentedElement(eefPage);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case EditorPackage.STANDARD_FORM_PAGE: {
 				StandardFormPage standardFormPage = (StandardFormPage)theEObject;
 				T result = caseStandardFormPage(standardFormPage);
 				if (result == null) result = caseEEFPage(standardFormPage);
 				if (result == null) result = caseView(standardFormPage);
 				if (result == null) result = caseContainer(standardFormPage);
 				if (result == null) result = caseViewElement(standardFormPage);
 				if (result == null) result = caseIdentifiedElement(standardFormPage);
 				if (result == null) result = caseDocumentedElement(standardFormPage);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case EditorPackage.MASTER_DETAILS_PAGE: {
 				MasterDetailsPage masterDetailsPage = (MasterDetailsPage)theEObject;
 				T result = caseMasterDetailsPage(masterDetailsPage);
 				if (result == null) result = caseEEFPage(masterDetailsPage);
 				if (result == null) result = caseView(masterDetailsPage);
 				if (result == null) result = caseContainer(masterDetailsPage);
 				if (result == null) result = caseViewElement(masterDetailsPage);
 				if (result == null) result = caseIdentifiedElement(masterDetailsPage);
 				if (result == null) result = caseDocumentedElement(masterDetailsPage);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case EditorPackage.EEF_MASTER_PAGE: {
 				EEFMasterPage eefMasterPage = (EEFMasterPage)theEObject;
 				T result = caseEEFMasterPage(eefMasterPage);
 				if (result == null) result = caseMasterDetailsPage(eefMasterPage);
 				if (result == null) result = caseEEFPage(eefMasterPage);
 				if (result == null) result = caseView(eefMasterPage);
 				if (result == null) result = caseContainer(eefMasterPage);
 				if (result == null) result = caseViewElement(eefMasterPage);
 				if (result == null) result = caseIdentifiedElement(eefMasterPage);
 				if (result == null) result = caseDocumentedElement(eefMasterPage);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case EditorPackage.TREE_MASTER_PAGE: {
 				TreeMasterPage treeMasterPage = (TreeMasterPage)theEObject;
 				T result = caseTreeMasterPage(treeMasterPage);
 				if (result == null) result = caseMasterDetailsPage(treeMasterPage);
 				if (result == null) result = caseEEFPage(treeMasterPage);
 				if (result == null) result = caseView(treeMasterPage);
 				if (result == null) result = caseContainer(treeMasterPage);
 				if (result == null) result = caseViewElement(treeMasterPage);
 				if (result == null) result = caseIdentifiedElement(treeMasterPage);
 				if (result == null) result = caseDocumentedElement(treeMasterPage);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case EditorPackage.EEF_PAGE_INPUT: {
 				EEFPageInput eefPageInput = (EEFPageInput)theEObject;
 				T result = caseEEFPageInput(eefPageInput);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case EditorPackage.ALL_RESOURCES_ROOTS_RELATIVE_INPUT: {
 				AllResourcesRootsRelativeInput allResourcesRootsRelativeInput = (AllResourcesRootsRelativeInput)theEObject;
 				T result = caseAllResourcesRootsRelativeInput(allResourcesRootsRelativeInput);
 				if (result == null) result = caseEEFPageInput(allResourcesRootsRelativeInput);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case EditorPackage.FIRST_RESOURCE_ROOT_RELATIVE_INPUT: {
 				FirstResourceRootRelativeInput firstResourceRootRelativeInput = (FirstResourceRootRelativeInput)theEObject;
 				T result = caseFirstResourceRootRelativeInput(firstResourceRootRelativeInput);
 				if (result == null) result = caseEEFPageInput(firstResourceRootRelativeInput);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case EditorPackage.PART_FILTER: {
 				PartFilter partFilter = (PartFilter)theEObject;
 				T result = casePartFilter(partFilter);
 				if (result == null) result = caseBindingFilter(partFilter);
 				if (result == null) result = caseMapping_DocumentedElement(partFilter);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case EditorPackage.EEF_EDITOR_CONTRIBUTIONS: {
 				EEFEditorContributions eefEditorContributions = (EEFEditorContributions)theEObject;
 				T result = caseEEFEditorContributions(eefEditorContributions);
 				if (result == null) result = caseCategory(eefEditorContributions);
 				if (result == null) result = caseMapping_DocumentedElement(eefEditorContributions);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case EditorPackage.EEF_EDITOR_PAGES: {
 				EEFEditorPages eefEditorPages = (EEFEditorPages)theEObject;
 				T result = caseEEFEditorPages(eefEditorPages);
 				if (result == null) result = caseViews_Category(eefEditorPages);
 				if (result == null) result = caseDocumentedElement(eefEditorPages);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case EditorPackage.STATIC_EEF_EDITOR_CONTRIBUTION: {
 				StaticEEFEditorContribution staticEEFEditorContribution = (StaticEEFEditorContribution)theEObject;
 				T result = caseStaticEEFEditorContribution(staticEEFEditorContribution);
 				if (result == null) result = caseAbstractElementBinding(staticEEFEditorContribution);
 				if (result == null) result = caseEEFEditorContribution(staticEEFEditorContribution);
 				if (result == null) result = caseMapping_DocumentedElement(staticEEFEditorContribution);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case EditorPackage.DYNAMIC_EEF_EDITOR_CONTRIBUTION: {
 				DynamicEEFEditorContribution dynamicEEFEditorContribution = (DynamicEEFEditorContribution)theEObject;
 				T result = caseDynamicEEFEditorContribution(dynamicEEFEditorContribution);
 				if (result == null) result = caseEMFElementBinding(dynamicEEFEditorContribution);
 				if (result == null) result = caseEEFEditorContribution(dynamicEEFEditorContribution);
 				if (result == null) result = caseAbstractElementBinding(dynamicEEFEditorContribution);
 				if (result == null) result = caseMapping_DocumentedElement(dynamicEEFEditorContribution);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			case EditorPackage.REFERENCEABLE_OBJECT: {
 				ReferenceableObject referenceableObject = (ReferenceableObject)theEObject;
 				T result = caseReferenceableObject(referenceableObject);
 				if (result == null) result = defaultCase(theEObject);
 				return result;
 			}
 			default: return defaultCase(theEObject);
 		}
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>EEF Editor Contribution</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>EEF Editor Contribution</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseEEFEditorContribution(EEFEditorContribution object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>EEF Page</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>EEF Page</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseEEFPage(EEFPage object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Standard Form Page</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Standard Form Page</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseStandardFormPage(StandardFormPage object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Master Details Page</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Master Details Page</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseMasterDetailsPage(MasterDetailsPage object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>EEF Master Page</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>EEF Master Page</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseEEFMasterPage(EEFMasterPage object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Tree Master Page</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Tree Master Page</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseTreeMasterPage(TreeMasterPage object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>EEF Page Input</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>EEF Page Input</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseEEFPageInput(EEFPageInput object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>All Resources Roots Relative Input</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>All Resources Roots Relative Input</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseAllResourcesRootsRelativeInput(AllResourcesRootsRelativeInput object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>First Resource Root Relative Input</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>First Resource Root Relative Input</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseFirstResourceRootRelativeInput(FirstResourceRootRelativeInput object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Part Filter</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Part Filter</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T casePartFilter(PartFilter object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>EEF Editor Contributions</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>EEF Editor Contributions</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseEEFEditorContributions(EEFEditorContributions object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>EEF Editor Pages</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>EEF Editor Pages</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseEEFEditorPages(EEFEditorPages object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Static EEF Editor Contribution</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Static EEF Editor Contribution</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseStaticEEFEditorContribution(StaticEEFEditorContribution object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Dynamic EEF Editor Contribution</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Dynamic EEF Editor Contribution</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseDynamicEEFEditorContribution(DynamicEEFEditorContribution object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Referenceable Object</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Referenceable Object</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseReferenceableObject(ReferenceableObject object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Documented Element</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Documented Element</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseDocumentedElement(DocumentedElement object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>View Element</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>View Element</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseViewElement(ViewElement object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Identified Element</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Identified Element</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseIdentifiedElement(IdentifiedElement object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Container</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Container</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseContainer(Container object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>View</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>View</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseView(View object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Documented Element</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Documented Element</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseMapping_DocumentedElement(org.eclipse.emf.eef.mapping.DocumentedElement object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Binding Filter</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Binding Filter</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseBindingFilter(BindingFilter object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Category</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Category</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseCategory(Category object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Category</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Category</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseViews_Category(org.eclipse.emf.eef.views.Category object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Abstract Element Binding</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Abstract Element Binding</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseAbstractElementBinding(AbstractElementBinding object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>EMF Element Binding</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>EMF Element Binding</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseEMFElementBinding(EMFElementBinding object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch, but this is the last case anyway.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
 	 * @generated
 	 */
 	public T defaultCase(EObject object) {
 		return null;
 	}
 
 } //EditorSwitch
