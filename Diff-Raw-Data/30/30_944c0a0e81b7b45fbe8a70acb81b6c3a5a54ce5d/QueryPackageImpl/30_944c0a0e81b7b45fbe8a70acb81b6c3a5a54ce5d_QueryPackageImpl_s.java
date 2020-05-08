 /*******************************************************************************
 * Copyright (c) 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.extended.query.impl;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.impl.EPackageImpl;
 import org.eclipse.emf.eef.components.ComponentsPackage;
 import org.eclipse.emf.eef.extended.editor.EditorPackage;
 import org.eclipse.emf.eef.extended.editor.impl.EditorPackageImpl;
 import org.eclipse.emf.eef.extended.query.EEFUnderstandableQuery;
 import org.eclipse.emf.eef.extended.query.ExplicitPathQuery;
 import org.eclipse.emf.eef.extended.query.OCLQuery;
 import org.eclipse.emf.eef.extended.query.QueryFactory;
 import org.eclipse.emf.eef.extended.query.QueryPackage;
 import org.eclipse.emf.eef.mapping.navigation.NavigationPackage;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model <b>Package</b>.
  * <!-- end-user-doc -->
  * @generated
  */
 public class QueryPackageImpl extends EPackageImpl implements QueryPackage {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass eefUnderstandableQueryEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass oclQueryEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass explicitPathQueryEClass = null;
 
 	/**
 	 * Creates an instance of the model <b>Package</b>, registered with
 	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
 	 * package URI value.
 	 * <p>Note: the correct way to create the package is via the static
 	 * factory method {@link #init init()}, which also performs
 	 * initialization of the package, or returns the registered package,
 	 * if one already exists.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.ecore.EPackage.Registry
 	 * @see org.eclipse.emf.eef.extended.query.QueryPackage#eNS_URI
 	 * @see #init()
 	 * @generated
 	 */
 	private QueryPackageImpl() {
 		super(eNS_URI, QueryFactory.eINSTANCE);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private static boolean isInited = false;
 
 	/**
 	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
 	 * 
 	 * <p>This method is used to initialize {@link QueryPackage#eINSTANCE} when that field is accessed.
 	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #eNS_URI
 	 * @see #createPackageContents()
 	 * @see #initializePackageContents()
 	 * @generated
 	 */
 	public static QueryPackage init() {
 		if (isInited) return (QueryPackage)EPackage.Registry.INSTANCE.getEPackage(QueryPackage.eNS_URI);
 
 		// Obtain or create and register package
 		QueryPackageImpl theQueryPackage = (QueryPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof QueryPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new QueryPackageImpl());
 
 		isInited = true;
 
 		// Initialize simple dependencies
 		ComponentsPackage.eINSTANCE.eClass();
 
 		// Obtain or create and register interdependencies
 		EditorPackageImpl theEditorPackage = (EditorPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(EditorPackage.eNS_URI) instanceof EditorPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(EditorPackage.eNS_URI) : EditorPackage.eINSTANCE);
 
 		// Create package meta-data objects
 		theQueryPackage.createPackageContents();
 		theEditorPackage.createPackageContents();
 
 		// Initialize created meta-data
 		theQueryPackage.initializePackageContents();
 		theEditorPackage.initializePackageContents();
 
 		// Mark meta-data to indicate it can't be changed
 		theQueryPackage.freeze();
 
   
 		// Update the registry and return the package
 		EPackage.Registry.INSTANCE.put(QueryPackage.eNS_URI, theQueryPackage);
 		return theQueryPackage;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getEEFUnderstandableQuery() {
 		return eefUnderstandableQueryEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getOCLQuery() {
 		return oclQueryEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getOCLQuery_Query() {
 		return (EAttribute)oclQueryEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getOCLQuery_Context() {
 		return (EReference)oclQueryEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getExplicitPathQuery() {
 		return explicitPathQueryEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getExplicitPathQuery_Query() {
 		return (EReference)explicitPathQueryEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public QueryFactory getQueryFactory() {
 		return (QueryFactory)getEFactoryInstance();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private boolean isCreated = false;
 
 	/**
 	 * Creates the meta-model objects for the package.  This method is
 	 * guarded to have no affect on any invocation but its first.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void createPackageContents() {
 		if (isCreated) return;
 		isCreated = true;
 
 		// Create classes and their features
 		eefUnderstandableQueryEClass = createEClass(EEF_UNDERSTANDABLE_QUERY);
 
 		oclQueryEClass = createEClass(OCL_QUERY);
 		createEAttribute(oclQueryEClass, OCL_QUERY__QUERY);
 		createEReference(oclQueryEClass, OCL_QUERY__CONTEXT);
 
 		explicitPathQueryEClass = createEClass(EXPLICIT_PATH_QUERY);
 		createEReference(explicitPathQueryEClass, EXPLICIT_PATH_QUERY__QUERY);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private boolean isInitialized = false;
 
 	/**
 	 * Complete the initialization of the package and its meta-model.  This
 	 * method is guarded to have no affect on any invocation but its first.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void initializePackageContents() {
 		if (isInitialized) return;
 		isInitialized = true;
 
 		// Initialize package
 		setName(eNAME);
 		setNsPrefix(eNS_PREFIX);
 		setNsURI(eNS_URI);
 
 		// Obtain other dependent packages
 		EditorPackage theEditorPackage = (EditorPackage)EPackage.Registry.INSTANCE.getEPackage(EditorPackage.eNS_URI);
 		NavigationPackage theNavigationPackage = (NavigationPackage)EPackage.Registry.INSTANCE.getEPackage(NavigationPackage.eNS_URI);
 
 		// Create type parameters
 
 		// Set bounds for type parameters
 
 		// Add supertypes to classes
 		eefUnderstandableQueryEClass.getESuperTypes().add(theEditorPackage.getReferenceableObject());
 		oclQueryEClass.getESuperTypes().add(this.getEEFUnderstandableQuery());
 		explicitPathQueryEClass.getESuperTypes().add(this.getEEFUnderstandableQuery());
 
 		// Initialize classes and features; add operations and parameters
 		initEClass(eefUnderstandableQueryEClass, EEFUnderstandableQuery.class, "EEFUnderstandableQuery", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(oclQueryEClass, OCLQuery.class, "OCLQuery", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getOCLQuery_Query(), ecorePackage.getEString(), "query", null, 1, 1, OCLQuery.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getOCLQuery_Context(), ecorePackage.getEClassifier(), null, "context", null, 1, 1, OCLQuery.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(explicitPathQueryEClass, ExplicitPathQuery.class, "ExplicitPathQuery", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getExplicitPathQuery_Query(), theNavigationPackage.getModelNavigation(), null, "query", null, 1, 1, ExplicitPathQuery.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		// Create resource
 		createResource(eNS_URI);
 	}
 
 } //QueryPackageImpl
