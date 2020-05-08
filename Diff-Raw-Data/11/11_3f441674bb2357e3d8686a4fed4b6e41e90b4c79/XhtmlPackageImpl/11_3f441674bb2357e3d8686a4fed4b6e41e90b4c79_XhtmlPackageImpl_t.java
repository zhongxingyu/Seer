 /**
  * Copyright (c) 2013 itemis AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Mark Broerkens - initial API and implementation
  * 
  */
 package org.eclipse.rmf.reqif10.xhtml.impl;
 
 import java.io.IOException;
 
 import java.net.URL;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.common.util.WrappedException;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 
 import org.eclipse.emf.ecore.impl.EPackageImpl;
 
 import org.eclipse.emf.ecore.resource.Resource;
 
 import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
 
 import org.eclipse.emf.ecore.xml.namespace.XMLNamespacePackage;
 
 import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
 
 import org.eclipse.rmf.reqif10.datatypes.DatatypesPackage;
 
 import org.eclipse.rmf.reqif10.datatypes.impl.DatatypesPackageImpl;
 
 import org.eclipse.rmf.reqif10.xhtml.XhtmlFactory;
 import org.eclipse.rmf.reqif10.xhtml.XhtmlPackage;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model <b>Package</b>.
  * <!-- end-user-doc -->
  * @generated
  */
 public class XhtmlPackageImpl extends EPackageImpl implements XhtmlPackage {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected String packageFilename = "xhtml.ecore";
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass documentRootEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlAbbrTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlAcronymTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlAddressTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlATypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlBlockquoteTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlBrTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlCaptionTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlCiteTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlCodeTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlColgroupTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlColTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlDdTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlDfnTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlDivTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlDlTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlDtTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlEditTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlEmTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlH1TypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlH2TypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlH3TypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlH4TypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlH5TypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlH6TypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlHeadingTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlHrTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlInlPresTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlKbdTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlLiTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlObjectTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlOlTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlParamTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlPreTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlPTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlQTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlSampTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlSpanTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlStrongTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlTableTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlTbodyTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlTdTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlTfootTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlTheadTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlThTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlTrTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlUlTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass xhtmlVarTypeEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum alignTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum declareTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum frameTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum rulesTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum scopeTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum valignTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum valuetypeTypeEEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType alignTypeObjectEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType declareTypeObjectEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType frameTypeObjectEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType rulesTypeObjectEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType scopeTypeObjectEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType valignTypeObjectEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType valuetypeTypeObjectEDataType = null;
 
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
 	 * @see org.eclipse.rmf.reqif10.xhtml.XhtmlPackage#eNS_URI
 	 * @see #init()
 	 * @generated
 	 */
 	private XhtmlPackageImpl() {
 		super(eNS_URI, XhtmlFactory.eINSTANCE);
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
 	 * <p>This method is used to initialize {@link XhtmlPackage#eINSTANCE} when that field is accessed.
 	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #eNS_URI
 	 * @generated
 	 */
 	public static XhtmlPackage init() {
 		if (isInited) return (XhtmlPackage)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI);
 
 		// Obtain or create and register package
 		XhtmlPackageImpl theXhtmlPackage = (XhtmlPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof XhtmlPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new XhtmlPackageImpl());
 
 		isInited = true;
 
 		// Initialize simple dependencies
		XMLNamespacePackage.eINSTANCE.eClass();
 		XMLTypePackage.eINSTANCE.eClass();
 		XMLNamespacePackage.eINSTANCE.eClass();
 
 		// Obtain or create and register interdependencies
 		DatatypesPackageImpl theDatatypesPackage = (DatatypesPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(DatatypesPackage.eNS_URI) instanceof DatatypesPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(DatatypesPackage.eNS_URI) : DatatypesPackage.eINSTANCE);
 
 		// Load packages
 		theXhtmlPackage.loadPackage();
 
 		// Create package meta-data objects
 		theDatatypesPackage.createPackageContents();
 
 		// Initialize created meta-data
 		theDatatypesPackage.initializePackageContents();
 
 		// Fix loaded packages
 		theXhtmlPackage.fixPackageContents();
 
 		// Mark meta-data to indicate it can't be changed
 		theXhtmlPackage.freeze();
 
   
 		// Update the registry and return the package
 		EPackage.Registry.INSTANCE.put(XhtmlPackage.eNS_URI, theXhtmlPackage);
 		return theXhtmlPackage;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getDocumentRoot() {
 		if (documentRootEClass == null) {
 			documentRootEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(4);
 		}
 		return documentRootEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getDocumentRoot_Mixed() {
         return (EAttribute)getDocumentRoot().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getDocumentRoot_XMLNSPrefixMap() {
         return (EReference)getDocumentRoot().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getDocumentRoot_XSISchemaLocation() {
         return (EReference)getDocumentRoot().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getDocumentRoot_Div() {
         return (EReference)getDocumentRoot().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getDocumentRoot_P() {
         return (EReference)getDocumentRoot().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getDocumentRoot_Class() {
         return (EAttribute)getDocumentRoot().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getDocumentRoot_Id() {
         return (EAttribute)getDocumentRoot().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getDocumentRoot_Title() {
         return (EAttribute)getDocumentRoot().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlAbbrType() {
 		if (xhtmlAbbrTypeEClass == null) {
 			xhtmlAbbrTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(15);
 		}
 		return xhtmlAbbrTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAbbrType_Mixed() {
         return (EAttribute)getXhtmlAbbrType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAbbrType_XhtmlInlineMix() {
         return (EAttribute)getXhtmlAbbrType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Br() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Span() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Em() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Strong() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Dfn() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Code() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Samp() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Kbd() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Var() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Cite() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Abbr() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Acronym() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Q() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Tt() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_I() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_B() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Big() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Small() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Sub() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Sup() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_A() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Object() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Ins() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAbbrType_Del() {
         return (EReference)getXhtmlAbbrType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAbbrType_Class() {
         return (EAttribute)getXhtmlAbbrType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAbbrType_Id() {
         return (EAttribute)getXhtmlAbbrType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAbbrType_Lang() {
         return (EAttribute)getXhtmlAbbrType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAbbrType_Space() {
         return (EAttribute)getXhtmlAbbrType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAbbrType_Style() {
         return (EAttribute)getXhtmlAbbrType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAbbrType_Title() {
         return (EAttribute)getXhtmlAbbrType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlAcronymType() {
 		if (xhtmlAcronymTypeEClass == null) {
 			xhtmlAcronymTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(16);
 		}
 		return xhtmlAcronymTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAcronymType_Mixed() {
         return (EAttribute)getXhtmlAcronymType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAcronymType_XhtmlInlineMix() {
         return (EAttribute)getXhtmlAcronymType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Br() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Span() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Em() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Strong() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Dfn() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Code() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Samp() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Kbd() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Var() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Cite() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Abbr() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Acronym() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Q() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Tt() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_I() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_B() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Big() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Small() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Sub() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Sup() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_A() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Object() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Ins() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAcronymType_Del() {
         return (EReference)getXhtmlAcronymType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAcronymType_Class() {
         return (EAttribute)getXhtmlAcronymType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAcronymType_Id() {
         return (EAttribute)getXhtmlAcronymType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAcronymType_Lang() {
         return (EAttribute)getXhtmlAcronymType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAcronymType_Space() {
         return (EAttribute)getXhtmlAcronymType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAcronymType_Style() {
         return (EAttribute)getXhtmlAcronymType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAcronymType_Title() {
         return (EAttribute)getXhtmlAcronymType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlAddressType() {
 		if (xhtmlAddressTypeEClass == null) {
 			xhtmlAddressTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(17);
 		}
 		return xhtmlAddressTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAddressType_Mixed() {
         return (EAttribute)getXhtmlAddressType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAddressType_XhtmlInlineMix() {
         return (EAttribute)getXhtmlAddressType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Br() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Span() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Em() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Strong() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Dfn() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Code() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Samp() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Kbd() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Var() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Cite() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Abbr() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Acronym() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Q() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Tt() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_I() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_B() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Big() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Small() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Sub() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Sup() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_A() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Object() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Ins() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAddressType_Del() {
         return (EReference)getXhtmlAddressType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAddressType_Class() {
         return (EAttribute)getXhtmlAddressType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAddressType_Id() {
         return (EAttribute)getXhtmlAddressType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAddressType_Lang() {
         return (EAttribute)getXhtmlAddressType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAddressType_Space() {
         return (EAttribute)getXhtmlAddressType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAddressType_Style() {
         return (EAttribute)getXhtmlAddressType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAddressType_Title() {
         return (EAttribute)getXhtmlAddressType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlAType() {
 		if (xhtmlATypeEClass == null) {
 			xhtmlATypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(18);
 		}
 		return xhtmlATypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAType_Mixed() {
         return (EAttribute)getXhtmlAType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAType_XhtmlInlNoAnchorMix() {
         return (EAttribute)getXhtmlAType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Br() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Span() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Em() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Strong() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Dfn() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Code() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Samp() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Kbd() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Var() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Cite() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Abbr() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Acronym() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Q() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Tt() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_I() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_B() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Big() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Small() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Sub() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Sup() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Object() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Ins() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlAType_Del() {
         return (EReference)getXhtmlAType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAType_Accesskey() {
         return (EAttribute)getXhtmlAType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAType_Charset() {
         return (EAttribute)getXhtmlAType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAType_Class() {
         return (EAttribute)getXhtmlAType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAType_Href() {
         return (EAttribute)getXhtmlAType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAType_Hreflang() {
         return (EAttribute)getXhtmlAType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAType_Id() {
         return (EAttribute)getXhtmlAType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAType_Lang() {
         return (EAttribute)getXhtmlAType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAType_Rel() {
         return (EAttribute)getXhtmlAType().getEStructuralFeatures().get(32);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAType_Rev() {
         return (EAttribute)getXhtmlAType().getEStructuralFeatures().get(33);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAType_Space() {
         return (EAttribute)getXhtmlAType().getEStructuralFeatures().get(34);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAType_Style() {
         return (EAttribute)getXhtmlAType().getEStructuralFeatures().get(35);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAType_Tabindex() {
         return (EAttribute)getXhtmlAType().getEStructuralFeatures().get(36);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAType_Title() {
         return (EAttribute)getXhtmlAType().getEStructuralFeatures().get(37);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlAType_Type() {
         return (EAttribute)getXhtmlAType().getEStructuralFeatures().get(38);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlBlockquoteType() {
 		if (xhtmlBlockquoteTypeEClass == null) {
 			xhtmlBlockquoteTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(19);
 		}
 		return xhtmlBlockquoteTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlBlockquoteType_XhtmlBlockMix() {
         return (EAttribute)getXhtmlBlockquoteType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlBlockquoteType_H1() {
         return (EReference)getXhtmlBlockquoteType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlBlockquoteType_H2() {
         return (EReference)getXhtmlBlockquoteType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlBlockquoteType_H3() {
         return (EReference)getXhtmlBlockquoteType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlBlockquoteType_H4() {
         return (EReference)getXhtmlBlockquoteType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlBlockquoteType_H5() {
         return (EReference)getXhtmlBlockquoteType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlBlockquoteType_H6() {
         return (EReference)getXhtmlBlockquoteType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlBlockquoteType_Ul() {
         return (EReference)getXhtmlBlockquoteType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlBlockquoteType_Ol() {
         return (EReference)getXhtmlBlockquoteType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlBlockquoteType_Dl() {
         return (EReference)getXhtmlBlockquoteType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlBlockquoteType_P() {
         return (EReference)getXhtmlBlockquoteType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlBlockquoteType_Div() {
         return (EReference)getXhtmlBlockquoteType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlBlockquoteType_Pre() {
         return (EReference)getXhtmlBlockquoteType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlBlockquoteType_Blockquote() {
         return (EReference)getXhtmlBlockquoteType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlBlockquoteType_Address() {
         return (EReference)getXhtmlBlockquoteType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlBlockquoteType_Hr() {
         return (EReference)getXhtmlBlockquoteType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlBlockquoteType_Table() {
         return (EReference)getXhtmlBlockquoteType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlBlockquoteType_Ins() {
         return (EReference)getXhtmlBlockquoteType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlBlockquoteType_Del() {
         return (EReference)getXhtmlBlockquoteType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlBlockquoteType_Cite() {
         return (EAttribute)getXhtmlBlockquoteType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlBlockquoteType_Class() {
         return (EAttribute)getXhtmlBlockquoteType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlBlockquoteType_Id() {
         return (EAttribute)getXhtmlBlockquoteType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlBlockquoteType_Lang() {
         return (EAttribute)getXhtmlBlockquoteType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlBlockquoteType_Space() {
         return (EAttribute)getXhtmlBlockquoteType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlBlockquoteType_Style() {
         return (EAttribute)getXhtmlBlockquoteType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlBlockquoteType_Title() {
         return (EAttribute)getXhtmlBlockquoteType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlBrType() {
 		if (xhtmlBrTypeEClass == null) {
 			xhtmlBrTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(20);
 		}
 		return xhtmlBrTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlBrType_Class() {
         return (EAttribute)getXhtmlBrType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlBrType_Id() {
         return (EAttribute)getXhtmlBrType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlBrType_Space() {
         return (EAttribute)getXhtmlBrType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlBrType_Title() {
         return (EAttribute)getXhtmlBrType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlCaptionType() {
 		if (xhtmlCaptionTypeEClass == null) {
 			xhtmlCaptionTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(21);
 		}
 		return xhtmlCaptionTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCaptionType_Mixed() {
         return (EAttribute)getXhtmlCaptionType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCaptionType_XhtmlInlineMix() {
         return (EAttribute)getXhtmlCaptionType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Br() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Span() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Em() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Strong() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Dfn() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Code() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Samp() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Kbd() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Var() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Cite() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Abbr() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Acronym() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Q() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Tt() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_I() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_B() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Big() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Small() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Sub() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Sup() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_A() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Object() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Ins() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCaptionType_Del() {
         return (EReference)getXhtmlCaptionType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCaptionType_Class() {
         return (EAttribute)getXhtmlCaptionType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCaptionType_Id() {
         return (EAttribute)getXhtmlCaptionType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCaptionType_Lang() {
         return (EAttribute)getXhtmlCaptionType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCaptionType_Space() {
         return (EAttribute)getXhtmlCaptionType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCaptionType_Style() {
         return (EAttribute)getXhtmlCaptionType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCaptionType_Title() {
         return (EAttribute)getXhtmlCaptionType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlCiteType() {
 		if (xhtmlCiteTypeEClass == null) {
 			xhtmlCiteTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(22);
 		}
 		return xhtmlCiteTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCiteType_Mixed() {
         return (EAttribute)getXhtmlCiteType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCiteType_XhtmlInlineMix() {
         return (EAttribute)getXhtmlCiteType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Br() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Span() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Em() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Strong() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Dfn() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Code() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Samp() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Kbd() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Var() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Cite() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Abbr() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Acronym() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Q() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Tt() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_I() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_B() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Big() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Small() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Sub() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Sup() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_A() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Object() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Ins() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCiteType_Del() {
         return (EReference)getXhtmlCiteType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCiteType_Class() {
         return (EAttribute)getXhtmlCiteType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCiteType_Id() {
         return (EAttribute)getXhtmlCiteType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCiteType_Lang() {
         return (EAttribute)getXhtmlCiteType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCiteType_Space() {
         return (EAttribute)getXhtmlCiteType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCiteType_Style() {
         return (EAttribute)getXhtmlCiteType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCiteType_Title() {
         return (EAttribute)getXhtmlCiteType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlCodeType() {
 		if (xhtmlCodeTypeEClass == null) {
 			xhtmlCodeTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(23);
 		}
 		return xhtmlCodeTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCodeType_Mixed() {
         return (EAttribute)getXhtmlCodeType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCodeType_XhtmlInlineMix() {
         return (EAttribute)getXhtmlCodeType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Br() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Span() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Em() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Strong() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Dfn() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Code() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Samp() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Kbd() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Var() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Cite() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Abbr() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Acronym() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Q() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Tt() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_I() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_B() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Big() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Small() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Sub() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Sup() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_A() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Object() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Ins() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlCodeType_Del() {
         return (EReference)getXhtmlCodeType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCodeType_Class() {
         return (EAttribute)getXhtmlCodeType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCodeType_Id() {
         return (EAttribute)getXhtmlCodeType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCodeType_Lang() {
         return (EAttribute)getXhtmlCodeType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCodeType_Space() {
         return (EAttribute)getXhtmlCodeType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCodeType_Style() {
         return (EAttribute)getXhtmlCodeType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlCodeType_Title() {
         return (EAttribute)getXhtmlCodeType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlColgroupType() {
 		if (xhtmlColgroupTypeEClass == null) {
 			xhtmlColgroupTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(24);
 		}
 		return xhtmlColgroupTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlColgroupType_Col() {
         return (EReference)getXhtmlColgroupType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColgroupType_Align() {
         return (EAttribute)getXhtmlColgroupType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColgroupType_Char() {
         return (EAttribute)getXhtmlColgroupType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColgroupType_Charoff() {
         return (EAttribute)getXhtmlColgroupType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColgroupType_Class() {
         return (EAttribute)getXhtmlColgroupType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColgroupType_Id() {
         return (EAttribute)getXhtmlColgroupType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColgroupType_Lang() {
         return (EAttribute)getXhtmlColgroupType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColgroupType_Space() {
         return (EAttribute)getXhtmlColgroupType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColgroupType_Span() {
         return (EAttribute)getXhtmlColgroupType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColgroupType_Style() {
         return (EAttribute)getXhtmlColgroupType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColgroupType_Title() {
         return (EAttribute)getXhtmlColgroupType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColgroupType_Valign() {
         return (EAttribute)getXhtmlColgroupType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColgroupType_Width() {
         return (EAttribute)getXhtmlColgroupType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlColType() {
 		if (xhtmlColTypeEClass == null) {
 			xhtmlColTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(25);
 		}
 		return xhtmlColTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColType_Align() {
         return (EAttribute)getXhtmlColType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColType_Char() {
         return (EAttribute)getXhtmlColType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColType_Charoff() {
         return (EAttribute)getXhtmlColType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColType_Class() {
         return (EAttribute)getXhtmlColType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColType_Id() {
         return (EAttribute)getXhtmlColType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColType_Lang() {
         return (EAttribute)getXhtmlColType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColType_Space() {
         return (EAttribute)getXhtmlColType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColType_Span() {
         return (EAttribute)getXhtmlColType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColType_Style() {
         return (EAttribute)getXhtmlColType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColType_Title() {
         return (EAttribute)getXhtmlColType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColType_Valign() {
         return (EAttribute)getXhtmlColType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlColType_Width() {
         return (EAttribute)getXhtmlColType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlDdType() {
 		if (xhtmlDdTypeEClass == null) {
 			xhtmlDdTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(26);
 		}
 		return xhtmlDdTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDdType_Mixed() {
         return (EAttribute)getXhtmlDdType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDdType_XhtmlFlowMix() {
         return (EAttribute)getXhtmlDdType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_H1() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_H2() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_H3() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_H4() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_H5() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_H6() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Ul() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Ol() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Dl() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_P() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Div() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Pre() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Blockquote() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Address() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Hr() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Table() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Br() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Span() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Em() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Strong() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Dfn() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Code() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Samp() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Kbd() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Var() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Cite() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Abbr() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Acronym() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Q() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Tt() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_I() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(32);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_B() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(33);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Big() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(34);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Small() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(35);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Sub() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(36);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Sup() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(37);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_A() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(38);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Object() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(39);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Ins() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(40);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDdType_Del() {
         return (EReference)getXhtmlDdType().getEStructuralFeatures().get(41);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDdType_Class() {
         return (EAttribute)getXhtmlDdType().getEStructuralFeatures().get(42);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDdType_Id() {
         return (EAttribute)getXhtmlDdType().getEStructuralFeatures().get(43);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDdType_Lang() {
         return (EAttribute)getXhtmlDdType().getEStructuralFeatures().get(44);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDdType_Space() {
         return (EAttribute)getXhtmlDdType().getEStructuralFeatures().get(45);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDdType_Style() {
         return (EAttribute)getXhtmlDdType().getEStructuralFeatures().get(46);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDdType_Title() {
         return (EAttribute)getXhtmlDdType().getEStructuralFeatures().get(47);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlDfnType() {
 		if (xhtmlDfnTypeEClass == null) {
 			xhtmlDfnTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(27);
 		}
 		return xhtmlDfnTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDfnType_Mixed() {
         return (EAttribute)getXhtmlDfnType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDfnType_XhtmlInlineMix() {
         return (EAttribute)getXhtmlDfnType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Br() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Span() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Em() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Strong() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Dfn() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Code() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Samp() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Kbd() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Var() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Cite() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Abbr() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Acronym() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Q() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Tt() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_I() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_B() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Big() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Small() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Sub() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Sup() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_A() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Object() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Ins() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDfnType_Del() {
         return (EReference)getXhtmlDfnType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDfnType_Class() {
         return (EAttribute)getXhtmlDfnType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDfnType_Id() {
         return (EAttribute)getXhtmlDfnType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDfnType_Lang() {
         return (EAttribute)getXhtmlDfnType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDfnType_Space() {
         return (EAttribute)getXhtmlDfnType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDfnType_Style() {
         return (EAttribute)getXhtmlDfnType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDfnType_Title() {
         return (EAttribute)getXhtmlDfnType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlDivType() {
 		if (xhtmlDivTypeEClass == null) {
 			xhtmlDivTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(28);
 		}
 		return xhtmlDivTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDivType_Mixed() {
         return (EAttribute)getXhtmlDivType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDivType_XhtmlFlowMix() {
         return (EAttribute)getXhtmlDivType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_H1() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_H2() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_H3() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_H4() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_H5() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_H6() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Ul() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Ol() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Dl() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_P() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Div() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Pre() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Blockquote() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Address() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Hr() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Table() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Br() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Span() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Em() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Strong() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Dfn() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Code() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Samp() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Kbd() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Var() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Cite() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Abbr() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Acronym() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Q() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Tt() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_I() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(32);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_B() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(33);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Big() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(34);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Small() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(35);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Sub() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(36);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Sup() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(37);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_A() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(38);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Object() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(39);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Ins() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(40);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDivType_Del() {
         return (EReference)getXhtmlDivType().getEStructuralFeatures().get(41);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDivType_Class() {
         return (EAttribute)getXhtmlDivType().getEStructuralFeatures().get(42);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDivType_Id() {
         return (EAttribute)getXhtmlDivType().getEStructuralFeatures().get(43);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDivType_Lang() {
         return (EAttribute)getXhtmlDivType().getEStructuralFeatures().get(44);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDivType_Space() {
         return (EAttribute)getXhtmlDivType().getEStructuralFeatures().get(45);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDivType_Style() {
         return (EAttribute)getXhtmlDivType().getEStructuralFeatures().get(46);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDivType_Title() {
         return (EAttribute)getXhtmlDivType().getEStructuralFeatures().get(47);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlDlType() {
 		if (xhtmlDlTypeEClass == null) {
 			xhtmlDlTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(29);
 		}
 		return xhtmlDlTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDlType_Group() {
         return (EAttribute)getXhtmlDlType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDlType_Dt() {
         return (EReference)getXhtmlDlType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDlType_Dd() {
         return (EReference)getXhtmlDlType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDlType_Class() {
         return (EAttribute)getXhtmlDlType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDlType_Id() {
         return (EAttribute)getXhtmlDlType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDlType_Lang() {
         return (EAttribute)getXhtmlDlType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDlType_Space() {
         return (EAttribute)getXhtmlDlType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDlType_Style() {
         return (EAttribute)getXhtmlDlType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDlType_Title() {
         return (EAttribute)getXhtmlDlType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlDtType() {
 		if (xhtmlDtTypeEClass == null) {
 			xhtmlDtTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(30);
 		}
 		return xhtmlDtTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDtType_Mixed() {
         return (EAttribute)getXhtmlDtType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDtType_XhtmlInlineMix() {
         return (EAttribute)getXhtmlDtType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Br() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Span() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Em() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Strong() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Dfn() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Code() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Samp() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Kbd() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Var() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Cite() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Abbr() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Acronym() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Q() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Tt() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_I() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_B() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Big() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Small() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Sub() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Sup() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_A() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Object() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Ins() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlDtType_Del() {
         return (EReference)getXhtmlDtType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDtType_Class() {
         return (EAttribute)getXhtmlDtType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDtType_Id() {
         return (EAttribute)getXhtmlDtType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDtType_Lang() {
         return (EAttribute)getXhtmlDtType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDtType_Space() {
         return (EAttribute)getXhtmlDtType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDtType_Style() {
         return (EAttribute)getXhtmlDtType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlDtType_Title() {
         return (EAttribute)getXhtmlDtType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlEditType() {
 		if (xhtmlEditTypeEClass == null) {
 			xhtmlEditTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(31);
 		}
 		return xhtmlEditTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlEditType_Mixed() {
         return (EAttribute)getXhtmlEditType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlEditType_XhtmlFlowMix() {
         return (EAttribute)getXhtmlEditType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_H1() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_H2() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_H3() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_H4() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_H5() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_H6() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Ul() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Ol() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Dl() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_P() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Div() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Pre() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Blockquote() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Address() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Hr() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Table() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Br() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Span() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Em() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Strong() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Dfn() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Code() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Samp() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Kbd() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Var() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Cite() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Abbr() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Acronym() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Q() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Tt() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_I() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(32);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_B() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(33);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Big() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(34);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Small() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(35);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Sub() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(36);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Sup() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(37);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_A() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(38);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Object() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(39);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Ins() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(40);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEditType_Del() {
         return (EReference)getXhtmlEditType().getEStructuralFeatures().get(41);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlEditType_Cite1() {
         return (EAttribute)getXhtmlEditType().getEStructuralFeatures().get(42);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlEditType_Class() {
         return (EAttribute)getXhtmlEditType().getEStructuralFeatures().get(43);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlEditType_Datetime() {
         return (EAttribute)getXhtmlEditType().getEStructuralFeatures().get(44);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlEditType_Id() {
         return (EAttribute)getXhtmlEditType().getEStructuralFeatures().get(45);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlEditType_Lang() {
         return (EAttribute)getXhtmlEditType().getEStructuralFeatures().get(46);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlEditType_Space() {
         return (EAttribute)getXhtmlEditType().getEStructuralFeatures().get(47);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlEditType_Style() {
         return (EAttribute)getXhtmlEditType().getEStructuralFeatures().get(48);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlEditType_Title() {
         return (EAttribute)getXhtmlEditType().getEStructuralFeatures().get(49);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlEmType() {
 		if (xhtmlEmTypeEClass == null) {
 			xhtmlEmTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(32);
 		}
 		return xhtmlEmTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlEmType_Mixed() {
         return (EAttribute)getXhtmlEmType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlEmType_XhtmlInlineMix() {
         return (EAttribute)getXhtmlEmType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Br() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Span() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Em() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Strong() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Dfn() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Code() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Samp() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Kbd() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Var() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Cite() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Abbr() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Acronym() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Q() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Tt() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_I() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_B() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Big() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Small() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Sub() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Sup() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_A() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Object() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Ins() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlEmType_Del() {
         return (EReference)getXhtmlEmType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlEmType_Class() {
         return (EAttribute)getXhtmlEmType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlEmType_Id() {
         return (EAttribute)getXhtmlEmType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlEmType_Lang() {
         return (EAttribute)getXhtmlEmType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlEmType_Space() {
         return (EAttribute)getXhtmlEmType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlEmType_Style() {
         return (EAttribute)getXhtmlEmType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlEmType_Title() {
         return (EAttribute)getXhtmlEmType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlH1Type() {
 		if (xhtmlH1TypeEClass == null) {
 			xhtmlH1TypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(33);
 		}
 		return xhtmlH1TypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH1Type_Mixed() {
         return (EAttribute)getXhtmlH1Type().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH1Type_XhtmlInlineMix() {
         return (EAttribute)getXhtmlH1Type().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Br() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Span() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Em() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Strong() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Dfn() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Code() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Samp() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Kbd() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Var() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Cite() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Abbr() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Acronym() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Q() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Tt() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_I() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_B() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Big() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Small() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Sub() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Sup() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_A() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Object() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Ins() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH1Type_Del() {
         return (EReference)getXhtmlH1Type().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH1Type_Class() {
         return (EAttribute)getXhtmlH1Type().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH1Type_Id() {
         return (EAttribute)getXhtmlH1Type().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH1Type_Lang() {
         return (EAttribute)getXhtmlH1Type().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH1Type_Space() {
         return (EAttribute)getXhtmlH1Type().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH1Type_Style() {
         return (EAttribute)getXhtmlH1Type().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH1Type_Title() {
         return (EAttribute)getXhtmlH1Type().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlH2Type() {
 		if (xhtmlH2TypeEClass == null) {
 			xhtmlH2TypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(34);
 		}
 		return xhtmlH2TypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH2Type_Mixed() {
         return (EAttribute)getXhtmlH2Type().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH2Type_XhtmlInlineMix() {
         return (EAttribute)getXhtmlH2Type().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Br() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Span() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Em() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Strong() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Dfn() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Code() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Samp() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Kbd() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Var() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Cite() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Abbr() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Acronym() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Q() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Tt() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_I() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_B() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Big() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Small() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Sub() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Sup() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_A() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Object() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Ins() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH2Type_Del() {
         return (EReference)getXhtmlH2Type().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH2Type_Class() {
         return (EAttribute)getXhtmlH2Type().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH2Type_Id() {
         return (EAttribute)getXhtmlH2Type().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH2Type_Lang() {
         return (EAttribute)getXhtmlH2Type().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH2Type_Space() {
         return (EAttribute)getXhtmlH2Type().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH2Type_Style() {
         return (EAttribute)getXhtmlH2Type().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH2Type_Title() {
         return (EAttribute)getXhtmlH2Type().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlH3Type() {
 		if (xhtmlH3TypeEClass == null) {
 			xhtmlH3TypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(35);
 		}
 		return xhtmlH3TypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH3Type_Mixed() {
         return (EAttribute)getXhtmlH3Type().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH3Type_XhtmlInlineMix() {
         return (EAttribute)getXhtmlH3Type().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Br() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Span() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Em() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Strong() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Dfn() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Code() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Samp() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Kbd() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Var() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Cite() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Abbr() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Acronym() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Q() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Tt() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_I() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_B() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Big() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Small() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Sub() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Sup() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_A() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Object() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Ins() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH3Type_Del() {
         return (EReference)getXhtmlH3Type().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH3Type_Class() {
         return (EAttribute)getXhtmlH3Type().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH3Type_Id() {
         return (EAttribute)getXhtmlH3Type().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH3Type_Lang() {
         return (EAttribute)getXhtmlH3Type().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH3Type_Space() {
         return (EAttribute)getXhtmlH3Type().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH3Type_Style() {
         return (EAttribute)getXhtmlH3Type().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH3Type_Title() {
         return (EAttribute)getXhtmlH3Type().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlH4Type() {
 		if (xhtmlH4TypeEClass == null) {
 			xhtmlH4TypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(36);
 		}
 		return xhtmlH4TypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH4Type_Mixed() {
         return (EAttribute)getXhtmlH4Type().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH4Type_XhtmlInlineMix() {
         return (EAttribute)getXhtmlH4Type().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Br() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Span() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Em() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Strong() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Dfn() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Code() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Samp() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Kbd() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Var() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Cite() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Abbr() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Acronym() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Q() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Tt() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_I() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_B() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Big() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Small() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Sub() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Sup() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_A() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Object() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Ins() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH4Type_Del() {
         return (EReference)getXhtmlH4Type().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH4Type_Class() {
         return (EAttribute)getXhtmlH4Type().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH4Type_Id() {
         return (EAttribute)getXhtmlH4Type().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH4Type_Lang() {
         return (EAttribute)getXhtmlH4Type().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH4Type_Space() {
         return (EAttribute)getXhtmlH4Type().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH4Type_Style() {
         return (EAttribute)getXhtmlH4Type().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH4Type_Title() {
         return (EAttribute)getXhtmlH4Type().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlH5Type() {
 		if (xhtmlH5TypeEClass == null) {
 			xhtmlH5TypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(37);
 		}
 		return xhtmlH5TypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH5Type_Mixed() {
         return (EAttribute)getXhtmlH5Type().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH5Type_XhtmlInlineMix() {
         return (EAttribute)getXhtmlH5Type().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Br() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Span() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Em() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Strong() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Dfn() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Code() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Samp() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Kbd() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Var() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Cite() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Abbr() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Acronym() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Q() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Tt() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_I() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_B() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Big() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Small() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Sub() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Sup() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_A() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Object() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Ins() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH5Type_Del() {
         return (EReference)getXhtmlH5Type().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH5Type_Class() {
         return (EAttribute)getXhtmlH5Type().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH5Type_Id() {
         return (EAttribute)getXhtmlH5Type().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH5Type_Lang() {
         return (EAttribute)getXhtmlH5Type().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH5Type_Space() {
         return (EAttribute)getXhtmlH5Type().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH5Type_Style() {
         return (EAttribute)getXhtmlH5Type().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH5Type_Title() {
         return (EAttribute)getXhtmlH5Type().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlH6Type() {
 		if (xhtmlH6TypeEClass == null) {
 			xhtmlH6TypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(38);
 		}
 		return xhtmlH6TypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH6Type_Mixed() {
         return (EAttribute)getXhtmlH6Type().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH6Type_XhtmlInlineMix() {
         return (EAttribute)getXhtmlH6Type().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Br() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Span() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Em() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Strong() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Dfn() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Code() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Samp() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Kbd() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Var() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Cite() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Abbr() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Acronym() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Q() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Tt() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_I() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_B() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Big() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Small() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Sub() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Sup() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_A() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Object() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Ins() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlH6Type_Del() {
         return (EReference)getXhtmlH6Type().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH6Type_Class() {
         return (EAttribute)getXhtmlH6Type().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH6Type_Id() {
         return (EAttribute)getXhtmlH6Type().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH6Type_Lang() {
         return (EAttribute)getXhtmlH6Type().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH6Type_Space() {
         return (EAttribute)getXhtmlH6Type().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH6Type_Style() {
         return (EAttribute)getXhtmlH6Type().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlH6Type_Title() {
         return (EAttribute)getXhtmlH6Type().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlHeadingType() {
 		if (xhtmlHeadingTypeEClass == null) {
 			xhtmlHeadingTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(39);
 		}
 		return xhtmlHeadingTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlHeadingType_Mixed() {
         return (EAttribute)getXhtmlHeadingType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlHeadingType_XhtmlInlineMix() {
         return (EAttribute)getXhtmlHeadingType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Br() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Span() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Em() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Strong() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Dfn() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Code() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Samp() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Kbd() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Var() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Cite() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Abbr() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Acronym() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Q() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Tt() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_I() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_B() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Big() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Small() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Sub() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Sup() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_A() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Object() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Ins() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlHeadingType_Del() {
         return (EReference)getXhtmlHeadingType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlHeadingType_Class() {
         return (EAttribute)getXhtmlHeadingType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlHeadingType_Id() {
         return (EAttribute)getXhtmlHeadingType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlHeadingType_Lang() {
         return (EAttribute)getXhtmlHeadingType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlHeadingType_Space() {
         return (EAttribute)getXhtmlHeadingType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlHeadingType_Style() {
         return (EAttribute)getXhtmlHeadingType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlHeadingType_Title() {
         return (EAttribute)getXhtmlHeadingType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlHrType() {
 		if (xhtmlHrTypeEClass == null) {
 			xhtmlHrTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(40);
 		}
 		return xhtmlHrTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlHrType_Class() {
         return (EAttribute)getXhtmlHrType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlHrType_Id() {
         return (EAttribute)getXhtmlHrType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlHrType_Lang() {
         return (EAttribute)getXhtmlHrType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlHrType_Space() {
         return (EAttribute)getXhtmlHrType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlHrType_Style() {
         return (EAttribute)getXhtmlHrType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlHrType_Title() {
         return (EAttribute)getXhtmlHrType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlInlPresType() {
 		if (xhtmlInlPresTypeEClass == null) {
 			xhtmlInlPresTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(41);
 		}
 		return xhtmlInlPresTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlInlPresType_Mixed() {
         return (EAttribute)getXhtmlInlPresType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlInlPresType_XhtmlInlineMix() {
         return (EAttribute)getXhtmlInlPresType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Br() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Span() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Em() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Strong() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Dfn() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Code() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Samp() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Kbd() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Var() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Cite() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Abbr() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Acronym() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Q() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Tt() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_I() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_B() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Big() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Small() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Sub() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Sup() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_A() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Object() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Ins() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlInlPresType_Del() {
         return (EReference)getXhtmlInlPresType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlInlPresType_Class() {
         return (EAttribute)getXhtmlInlPresType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlInlPresType_Id() {
         return (EAttribute)getXhtmlInlPresType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlInlPresType_Lang() {
         return (EAttribute)getXhtmlInlPresType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlInlPresType_Space() {
         return (EAttribute)getXhtmlInlPresType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlInlPresType_Style() {
         return (EAttribute)getXhtmlInlPresType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlInlPresType_Title() {
         return (EAttribute)getXhtmlInlPresType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlKbdType() {
 		if (xhtmlKbdTypeEClass == null) {
 			xhtmlKbdTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(42);
 		}
 		return xhtmlKbdTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlKbdType_Mixed() {
         return (EAttribute)getXhtmlKbdType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlKbdType_XhtmlInlineMix() {
         return (EAttribute)getXhtmlKbdType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Br() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Span() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Em() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Strong() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Dfn() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Code() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Samp() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Kbd() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Var() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Cite() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Abbr() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Acronym() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Q() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Tt() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_I() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_B() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Big() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Small() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Sub() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Sup() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_A() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Object() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Ins() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlKbdType_Del() {
         return (EReference)getXhtmlKbdType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlKbdType_Class() {
         return (EAttribute)getXhtmlKbdType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlKbdType_Id() {
         return (EAttribute)getXhtmlKbdType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlKbdType_Lang() {
         return (EAttribute)getXhtmlKbdType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlKbdType_Space() {
         return (EAttribute)getXhtmlKbdType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlKbdType_Style() {
         return (EAttribute)getXhtmlKbdType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlKbdType_Title() {
         return (EAttribute)getXhtmlKbdType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlLiType() {
 		if (xhtmlLiTypeEClass == null) {
 			xhtmlLiTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(43);
 		}
 		return xhtmlLiTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlLiType_Mixed() {
         return (EAttribute)getXhtmlLiType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlLiType_XhtmlFlowMix() {
         return (EAttribute)getXhtmlLiType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_H1() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_H2() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_H3() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_H4() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_H5() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_H6() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Ul() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Ol() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Dl() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_P() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Div() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Pre() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Blockquote() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Address() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Hr() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Table() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Br() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Span() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Em() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Strong() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Dfn() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Code() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Samp() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Kbd() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Var() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Cite() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Abbr() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Acronym() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Q() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Tt() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_I() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(32);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_B() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(33);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Big() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(34);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Small() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(35);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Sub() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(36);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Sup() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(37);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_A() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(38);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Object() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(39);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Ins() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(40);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlLiType_Del() {
         return (EReference)getXhtmlLiType().getEStructuralFeatures().get(41);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlLiType_Class() {
         return (EAttribute)getXhtmlLiType().getEStructuralFeatures().get(42);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlLiType_Id() {
         return (EAttribute)getXhtmlLiType().getEStructuralFeatures().get(43);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlLiType_Lang() {
         return (EAttribute)getXhtmlLiType().getEStructuralFeatures().get(44);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlLiType_Space() {
         return (EAttribute)getXhtmlLiType().getEStructuralFeatures().get(45);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlLiType_Style() {
         return (EAttribute)getXhtmlLiType().getEStructuralFeatures().get(46);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlLiType_Title() {
         return (EAttribute)getXhtmlLiType().getEStructuralFeatures().get(47);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlObjectType() {
 		if (xhtmlObjectTypeEClass == null) {
 			xhtmlObjectTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(44);
 		}
 		return xhtmlObjectTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Mixed() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Group() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Param() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_H1() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_H2() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_H3() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_H4() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_H5() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_H6() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Ul() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Ol() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Dl() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_P() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Div() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Pre() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Blockquote() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Address() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Hr() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Table() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Br() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Span() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Em() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Strong() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Dfn() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Code() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Samp() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Kbd() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Var() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Cite() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Abbr() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Acronym() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Q() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Tt() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(32);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_I() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(33);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_B() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(34);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Big() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(35);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Small() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(36);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Sub() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(37);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Sup() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(38);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_A() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(39);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Object() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(40);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Ins() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(41);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlObjectType_Del() {
         return (EReference)getXhtmlObjectType().getEStructuralFeatures().get(42);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Archive() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(43);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Class() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(44);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Classid() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(45);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Codebase() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(46);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Codetype() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(47);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Data() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(48);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Declare() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(49);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Height() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(50);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Id() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(51);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Lang() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(52);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Name() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(53);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Space() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(54);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Standby() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(55);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Style() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(56);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Tabindex() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(57);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Title() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(58);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Type() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(59);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlObjectType_Width() {
         return (EAttribute)getXhtmlObjectType().getEStructuralFeatures().get(60);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlOlType() {
 		if (xhtmlOlTypeEClass == null) {
 			xhtmlOlTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(45);
 		}
 		return xhtmlOlTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlOlType_Li() {
         return (EReference)getXhtmlOlType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlOlType_Class() {
         return (EAttribute)getXhtmlOlType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlOlType_Id() {
         return (EAttribute)getXhtmlOlType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlOlType_Lang() {
         return (EAttribute)getXhtmlOlType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlOlType_Space() {
         return (EAttribute)getXhtmlOlType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlOlType_Style() {
         return (EAttribute)getXhtmlOlType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlOlType_Title() {
         return (EAttribute)getXhtmlOlType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlParamType() {
 		if (xhtmlParamTypeEClass == null) {
 			xhtmlParamTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(46);
 		}
 		return xhtmlParamTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlParamType_Id() {
         return (EAttribute)getXhtmlParamType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlParamType_Name() {
         return (EAttribute)getXhtmlParamType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlParamType_Type() {
         return (EAttribute)getXhtmlParamType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlParamType_Value() {
         return (EAttribute)getXhtmlParamType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlParamType_Valuetype() {
         return (EAttribute)getXhtmlParamType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlPreType() {
 		if (xhtmlPreTypeEClass == null) {
 			xhtmlPreTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(47);
 		}
 		return xhtmlPreTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlPreType_Mixed() {
         return (EAttribute)getXhtmlPreType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlPreType_XhtmlInlinePreMix() {
         return (EAttribute)getXhtmlPreType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPreType_Br() {
         return (EReference)getXhtmlPreType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPreType_Span() {
         return (EReference)getXhtmlPreType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPreType_Em() {
         return (EReference)getXhtmlPreType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPreType_Strong() {
         return (EReference)getXhtmlPreType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPreType_Dfn() {
         return (EReference)getXhtmlPreType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPreType_Code() {
         return (EReference)getXhtmlPreType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPreType_Samp() {
         return (EReference)getXhtmlPreType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPreType_Kbd() {
         return (EReference)getXhtmlPreType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPreType_Var() {
         return (EReference)getXhtmlPreType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPreType_Cite() {
         return (EReference)getXhtmlPreType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPreType_Abbr() {
         return (EReference)getXhtmlPreType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPreType_Acronym() {
         return (EReference)getXhtmlPreType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPreType_Q() {
         return (EReference)getXhtmlPreType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPreType_Tt() {
         return (EReference)getXhtmlPreType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPreType_I() {
         return (EReference)getXhtmlPreType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPreType_B() {
         return (EReference)getXhtmlPreType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPreType_A() {
         return (EReference)getXhtmlPreType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPreType_Ins() {
         return (EReference)getXhtmlPreType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPreType_Del() {
         return (EReference)getXhtmlPreType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlPreType_Class() {
         return (EAttribute)getXhtmlPreType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlPreType_Id() {
         return (EAttribute)getXhtmlPreType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlPreType_Lang() {
         return (EAttribute)getXhtmlPreType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlPreType_Space() {
         return (EAttribute)getXhtmlPreType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlPreType_Style() {
         return (EAttribute)getXhtmlPreType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlPreType_Title() {
         return (EAttribute)getXhtmlPreType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlPType() {
 		if (xhtmlPTypeEClass == null) {
 			xhtmlPTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(48);
 		}
 		return xhtmlPTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlPType_Mixed() {
         return (EAttribute)getXhtmlPType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlPType_XhtmlInlineMix() {
         return (EAttribute)getXhtmlPType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Br() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Span() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Em() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Strong() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Dfn() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Code() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Samp() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Kbd() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Var() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Cite() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Abbr() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Acronym() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Q() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Tt() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_I() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_B() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Big() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Small() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Sub() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Sup() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_A() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Object() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Ins() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlPType_Del() {
         return (EReference)getXhtmlPType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlPType_Class() {
         return (EAttribute)getXhtmlPType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlPType_Id() {
         return (EAttribute)getXhtmlPType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlPType_Lang() {
         return (EAttribute)getXhtmlPType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlPType_Space() {
         return (EAttribute)getXhtmlPType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlPType_Style() {
         return (EAttribute)getXhtmlPType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlPType_Title() {
         return (EAttribute)getXhtmlPType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlQType() {
 		if (xhtmlQTypeEClass == null) {
 			xhtmlQTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(49);
 		}
 		return xhtmlQTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlQType_Mixed() {
         return (EAttribute)getXhtmlQType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlQType_XhtmlInlineMix() {
         return (EAttribute)getXhtmlQType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Br() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Span() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Em() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Strong() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Dfn() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Code() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Samp() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Kbd() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Var() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Cite() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Abbr() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Acronym() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Q() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Tt() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_I() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_B() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Big() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Small() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Sub() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Sup() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_A() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Object() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Ins() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlQType_Del() {
         return (EReference)getXhtmlQType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlQType_Cite1() {
         return (EAttribute)getXhtmlQType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlQType_Class() {
         return (EAttribute)getXhtmlQType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlQType_Id() {
         return (EAttribute)getXhtmlQType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlQType_Lang() {
         return (EAttribute)getXhtmlQType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlQType_Space() {
         return (EAttribute)getXhtmlQType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlQType_Style() {
         return (EAttribute)getXhtmlQType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlQType_Title() {
         return (EAttribute)getXhtmlQType().getEStructuralFeatures().get(32);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlSampType() {
 		if (xhtmlSampTypeEClass == null) {
 			xhtmlSampTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(50);
 		}
 		return xhtmlSampTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlSampType_Mixed() {
         return (EAttribute)getXhtmlSampType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlSampType_XhtmlInlineMix() {
         return (EAttribute)getXhtmlSampType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Br() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Span() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Em() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Strong() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Dfn() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Code() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Samp() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Kbd() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Var() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Cite() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Abbr() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Acronym() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Q() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Tt() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_I() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_B() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Big() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Small() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Sub() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Sup() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_A() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Object() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Ins() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSampType_Del() {
         return (EReference)getXhtmlSampType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlSampType_Class() {
         return (EAttribute)getXhtmlSampType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlSampType_Id() {
         return (EAttribute)getXhtmlSampType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlSampType_Lang() {
         return (EAttribute)getXhtmlSampType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlSampType_Space() {
         return (EAttribute)getXhtmlSampType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlSampType_Style() {
         return (EAttribute)getXhtmlSampType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlSampType_Title() {
         return (EAttribute)getXhtmlSampType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlSpanType() {
 		if (xhtmlSpanTypeEClass == null) {
 			xhtmlSpanTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(51);
 		}
 		return xhtmlSpanTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlSpanType_Mixed() {
         return (EAttribute)getXhtmlSpanType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlSpanType_XhtmlInlineMix() {
         return (EAttribute)getXhtmlSpanType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Br() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Span() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Em() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Strong() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Dfn() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Code() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Samp() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Kbd() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Var() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Cite() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Abbr() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Acronym() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Q() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Tt() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_I() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_B() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Big() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Small() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Sub() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Sup() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_A() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Object() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Ins() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlSpanType_Del() {
         return (EReference)getXhtmlSpanType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlSpanType_Class() {
         return (EAttribute)getXhtmlSpanType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlSpanType_Id() {
         return (EAttribute)getXhtmlSpanType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlSpanType_Lang() {
         return (EAttribute)getXhtmlSpanType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlSpanType_Space() {
         return (EAttribute)getXhtmlSpanType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlSpanType_Style() {
         return (EAttribute)getXhtmlSpanType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlSpanType_Title() {
         return (EAttribute)getXhtmlSpanType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlStrongType() {
 		if (xhtmlStrongTypeEClass == null) {
 			xhtmlStrongTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(52);
 		}
 		return xhtmlStrongTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlStrongType_Mixed() {
         return (EAttribute)getXhtmlStrongType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlStrongType_XhtmlInlineMix() {
         return (EAttribute)getXhtmlStrongType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Br() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Span() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Em() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Strong() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Dfn() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Code() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Samp() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Kbd() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Var() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Cite() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Abbr() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Acronym() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Q() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Tt() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_I() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_B() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Big() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Small() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Sub() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Sup() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_A() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Object() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Ins() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlStrongType_Del() {
         return (EReference)getXhtmlStrongType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlStrongType_Class() {
         return (EAttribute)getXhtmlStrongType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlStrongType_Id() {
         return (EAttribute)getXhtmlStrongType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlStrongType_Lang() {
         return (EAttribute)getXhtmlStrongType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlStrongType_Space() {
         return (EAttribute)getXhtmlStrongType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlStrongType_Style() {
         return (EAttribute)getXhtmlStrongType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlStrongType_Title() {
         return (EAttribute)getXhtmlStrongType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlTableType() {
 		if (xhtmlTableTypeEClass == null) {
 			xhtmlTableTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(53);
 		}
 		return xhtmlTableTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTableType_Caption() {
         return (EReference)getXhtmlTableType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTableType_Col() {
         return (EReference)getXhtmlTableType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTableType_Colgroup() {
         return (EReference)getXhtmlTableType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTableType_Thead() {
         return (EReference)getXhtmlTableType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTableType_Tfoot() {
         return (EReference)getXhtmlTableType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTableType_Tbody() {
         return (EReference)getXhtmlTableType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTableType_Tr() {
         return (EReference)getXhtmlTableType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTableType_Border() {
         return (EAttribute)getXhtmlTableType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTableType_Cellpadding() {
         return (EAttribute)getXhtmlTableType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTableType_Cellspacing() {
         return (EAttribute)getXhtmlTableType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTableType_Class() {
         return (EAttribute)getXhtmlTableType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTableType_Frame() {
         return (EAttribute)getXhtmlTableType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTableType_Id() {
         return (EAttribute)getXhtmlTableType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTableType_Lang() {
         return (EAttribute)getXhtmlTableType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTableType_Rules() {
         return (EAttribute)getXhtmlTableType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTableType_Space() {
         return (EAttribute)getXhtmlTableType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTableType_Style() {
         return (EAttribute)getXhtmlTableType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTableType_Summary() {
         return (EAttribute)getXhtmlTableType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTableType_Title() {
         return (EAttribute)getXhtmlTableType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTableType_Width() {
         return (EAttribute)getXhtmlTableType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlTbodyType() {
 		if (xhtmlTbodyTypeEClass == null) {
 			xhtmlTbodyTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(54);
 		}
 		return xhtmlTbodyTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTbodyType_Tr() {
         return (EReference)getXhtmlTbodyType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTbodyType_Align() {
         return (EAttribute)getXhtmlTbodyType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTbodyType_Char() {
         return (EAttribute)getXhtmlTbodyType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTbodyType_Charoff() {
         return (EAttribute)getXhtmlTbodyType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTbodyType_Class() {
         return (EAttribute)getXhtmlTbodyType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTbodyType_Id() {
         return (EAttribute)getXhtmlTbodyType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTbodyType_Lang() {
         return (EAttribute)getXhtmlTbodyType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTbodyType_Space() {
         return (EAttribute)getXhtmlTbodyType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTbodyType_Style() {
         return (EAttribute)getXhtmlTbodyType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTbodyType_Title() {
         return (EAttribute)getXhtmlTbodyType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTbodyType_Valign() {
         return (EAttribute)getXhtmlTbodyType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlTdType() {
 		if (xhtmlTdTypeEClass == null) {
 			xhtmlTdTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(55);
 		}
 		return xhtmlTdTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTdType_Mixed() {
         return (EAttribute)getXhtmlTdType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTdType_XhtmlFlowMix() {
         return (EAttribute)getXhtmlTdType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_H1() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_H2() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_H3() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_H4() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_H5() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_H6() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Ul() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Ol() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Dl() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_P() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Div() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Pre() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Blockquote() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Address() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Hr() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Table() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Br() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Span() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Em() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Strong() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Dfn() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Code() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Samp() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Kbd() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Var() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Cite() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Abbr() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Acronym() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Q() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Tt() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_I() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(32);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_B() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(33);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Big() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(34);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Small() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(35);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Sub() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(36);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Sup() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(37);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_A() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(38);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Object() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(39);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Ins() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(40);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTdType_Del() {
         return (EReference)getXhtmlTdType().getEStructuralFeatures().get(41);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTdType_Abbr1() {
         return (EAttribute)getXhtmlTdType().getEStructuralFeatures().get(42);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTdType_Align() {
         return (EAttribute)getXhtmlTdType().getEStructuralFeatures().get(43);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTdType_Axis() {
         return (EAttribute)getXhtmlTdType().getEStructuralFeatures().get(44);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTdType_Char() {
         return (EAttribute)getXhtmlTdType().getEStructuralFeatures().get(45);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTdType_Charoff() {
         return (EAttribute)getXhtmlTdType().getEStructuralFeatures().get(46);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTdType_Class() {
         return (EAttribute)getXhtmlTdType().getEStructuralFeatures().get(47);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTdType_Colspan() {
         return (EAttribute)getXhtmlTdType().getEStructuralFeatures().get(48);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTdType_Headers() {
         return (EAttribute)getXhtmlTdType().getEStructuralFeatures().get(49);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTdType_Id() {
         return (EAttribute)getXhtmlTdType().getEStructuralFeatures().get(50);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTdType_Lang() {
         return (EAttribute)getXhtmlTdType().getEStructuralFeatures().get(51);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTdType_Rowspan() {
         return (EAttribute)getXhtmlTdType().getEStructuralFeatures().get(52);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTdType_Scope() {
         return (EAttribute)getXhtmlTdType().getEStructuralFeatures().get(53);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTdType_Space() {
         return (EAttribute)getXhtmlTdType().getEStructuralFeatures().get(54);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTdType_Style() {
         return (EAttribute)getXhtmlTdType().getEStructuralFeatures().get(55);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTdType_Title() {
         return (EAttribute)getXhtmlTdType().getEStructuralFeatures().get(56);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTdType_Valign() {
         return (EAttribute)getXhtmlTdType().getEStructuralFeatures().get(57);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlTfootType() {
 		if (xhtmlTfootTypeEClass == null) {
 			xhtmlTfootTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(56);
 		}
 		return xhtmlTfootTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTfootType_Tr() {
         return (EReference)getXhtmlTfootType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTfootType_Align() {
         return (EAttribute)getXhtmlTfootType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTfootType_Char() {
         return (EAttribute)getXhtmlTfootType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTfootType_Charoff() {
         return (EAttribute)getXhtmlTfootType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTfootType_Class() {
         return (EAttribute)getXhtmlTfootType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTfootType_Id() {
         return (EAttribute)getXhtmlTfootType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTfootType_Lang() {
         return (EAttribute)getXhtmlTfootType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTfootType_Space() {
         return (EAttribute)getXhtmlTfootType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTfootType_Style() {
         return (EAttribute)getXhtmlTfootType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTfootType_Title() {
         return (EAttribute)getXhtmlTfootType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTfootType_Valign() {
         return (EAttribute)getXhtmlTfootType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlTheadType() {
 		if (xhtmlTheadTypeEClass == null) {
 			xhtmlTheadTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(57);
 		}
 		return xhtmlTheadTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTheadType_Tr() {
         return (EReference)getXhtmlTheadType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTheadType_Align() {
         return (EAttribute)getXhtmlTheadType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTheadType_Char() {
         return (EAttribute)getXhtmlTheadType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTheadType_Charoff() {
         return (EAttribute)getXhtmlTheadType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTheadType_Class() {
         return (EAttribute)getXhtmlTheadType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTheadType_Id() {
         return (EAttribute)getXhtmlTheadType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTheadType_Lang() {
         return (EAttribute)getXhtmlTheadType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTheadType_Space() {
         return (EAttribute)getXhtmlTheadType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTheadType_Style() {
         return (EAttribute)getXhtmlTheadType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTheadType_Title() {
         return (EAttribute)getXhtmlTheadType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTheadType_Valign() {
         return (EAttribute)getXhtmlTheadType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlThType() {
 		if (xhtmlThTypeEClass == null) {
 			xhtmlThTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(58);
 		}
 		return xhtmlThTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlThType_Mixed() {
         return (EAttribute)getXhtmlThType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlThType_XhtmlFlowMix() {
         return (EAttribute)getXhtmlThType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_H1() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_H2() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_H3() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_H4() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_H5() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_H6() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Ul() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Ol() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Dl() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_P() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Div() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Pre() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Blockquote() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Address() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Hr() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Table() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Br() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Span() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Em() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Strong() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Dfn() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Code() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Samp() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Kbd() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Var() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Cite() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Abbr() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Acronym() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Q() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Tt() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_I() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(32);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_B() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(33);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Big() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(34);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Small() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(35);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Sub() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(36);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Sup() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(37);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_A() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(38);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Object() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(39);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Ins() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(40);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlThType_Del() {
         return (EReference)getXhtmlThType().getEStructuralFeatures().get(41);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlThType_Abbr1() {
         return (EAttribute)getXhtmlThType().getEStructuralFeatures().get(42);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlThType_Align() {
         return (EAttribute)getXhtmlThType().getEStructuralFeatures().get(43);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlThType_Axis() {
         return (EAttribute)getXhtmlThType().getEStructuralFeatures().get(44);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlThType_Char() {
         return (EAttribute)getXhtmlThType().getEStructuralFeatures().get(45);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlThType_Charoff() {
         return (EAttribute)getXhtmlThType().getEStructuralFeatures().get(46);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlThType_Class() {
         return (EAttribute)getXhtmlThType().getEStructuralFeatures().get(47);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlThType_Colspan() {
         return (EAttribute)getXhtmlThType().getEStructuralFeatures().get(48);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlThType_Headers() {
         return (EAttribute)getXhtmlThType().getEStructuralFeatures().get(49);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlThType_Id() {
         return (EAttribute)getXhtmlThType().getEStructuralFeatures().get(50);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlThType_Lang() {
         return (EAttribute)getXhtmlThType().getEStructuralFeatures().get(51);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlThType_Rowspan() {
         return (EAttribute)getXhtmlThType().getEStructuralFeatures().get(52);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlThType_Scope() {
         return (EAttribute)getXhtmlThType().getEStructuralFeatures().get(53);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlThType_Space() {
         return (EAttribute)getXhtmlThType().getEStructuralFeatures().get(54);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlThType_Style() {
         return (EAttribute)getXhtmlThType().getEStructuralFeatures().get(55);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlThType_Title() {
         return (EAttribute)getXhtmlThType().getEStructuralFeatures().get(56);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlThType_Valign() {
         return (EAttribute)getXhtmlThType().getEStructuralFeatures().get(57);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlTrType() {
 		if (xhtmlTrTypeEClass == null) {
 			xhtmlTrTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(59);
 		}
 		return xhtmlTrTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTrType_Group() {
         return (EAttribute)getXhtmlTrType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTrType_Th() {
         return (EReference)getXhtmlTrType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlTrType_Td() {
         return (EReference)getXhtmlTrType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTrType_Align() {
         return (EAttribute)getXhtmlTrType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTrType_Char() {
         return (EAttribute)getXhtmlTrType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTrType_Charoff() {
         return (EAttribute)getXhtmlTrType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTrType_Class() {
         return (EAttribute)getXhtmlTrType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTrType_Id() {
         return (EAttribute)getXhtmlTrType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTrType_Lang() {
         return (EAttribute)getXhtmlTrType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTrType_Space() {
         return (EAttribute)getXhtmlTrType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTrType_Style() {
         return (EAttribute)getXhtmlTrType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTrType_Title() {
         return (EAttribute)getXhtmlTrType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlTrType_Valign() {
         return (EAttribute)getXhtmlTrType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlUlType() {
 		if (xhtmlUlTypeEClass == null) {
 			xhtmlUlTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(60);
 		}
 		return xhtmlUlTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlUlType_Li() {
         return (EReference)getXhtmlUlType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlUlType_Class() {
         return (EAttribute)getXhtmlUlType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlUlType_Id() {
         return (EAttribute)getXhtmlUlType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlUlType_Lang() {
         return (EAttribute)getXhtmlUlType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlUlType_Space() {
         return (EAttribute)getXhtmlUlType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlUlType_Style() {
         return (EAttribute)getXhtmlUlType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlUlType_Title() {
         return (EAttribute)getXhtmlUlType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getXhtmlVarType() {
 		if (xhtmlVarTypeEClass == null) {
 			xhtmlVarTypeEClass = (EClass)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(61);
 		}
 		return xhtmlVarTypeEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlVarType_Mixed() {
         return (EAttribute)getXhtmlVarType().getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlVarType_XhtmlInlineMix() {
         return (EAttribute)getXhtmlVarType().getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Br() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Span() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Em() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Strong() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Dfn() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Code() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Samp() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Kbd() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Var() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(10);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Cite() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(11);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Abbr() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(12);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Acronym() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(13);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Q() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(14);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Tt() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(15);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_I() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(16);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_B() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(17);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Big() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(18);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Small() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(19);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Sub() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(20);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Sup() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(21);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_A() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(22);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Object() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(23);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Ins() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(24);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getXhtmlVarType_Del() {
         return (EReference)getXhtmlVarType().getEStructuralFeatures().get(25);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlVarType_Class() {
         return (EAttribute)getXhtmlVarType().getEStructuralFeatures().get(26);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlVarType_Id() {
         return (EAttribute)getXhtmlVarType().getEStructuralFeatures().get(27);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlVarType_Lang() {
         return (EAttribute)getXhtmlVarType().getEStructuralFeatures().get(28);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlVarType_Space() {
         return (EAttribute)getXhtmlVarType().getEStructuralFeatures().get(29);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlVarType_Style() {
         return (EAttribute)getXhtmlVarType().getEStructuralFeatures().get(30);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getXhtmlVarType_Title() {
         return (EAttribute)getXhtmlVarType().getEStructuralFeatures().get(31);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getAlignType() {
 		if (alignTypeEEnum == null) {
 			alignTypeEEnum = (EEnum)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(0);
 		}
 		return alignTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getDeclareType() {
 		if (declareTypeEEnum == null) {
 			declareTypeEEnum = (EEnum)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(2);
 		}
 		return declareTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getFrameType() {
 		if (frameTypeEEnum == null) {
 			frameTypeEEnum = (EEnum)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(5);
 		}
 		return frameTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getRulesType() {
 		if (rulesTypeEEnum == null) {
 			rulesTypeEEnum = (EEnum)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(7);
 		}
 		return rulesTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getScopeType() {
 		if (scopeTypeEEnum == null) {
 			scopeTypeEEnum = (EEnum)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(9);
 		}
 		return scopeTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getValignType() {
 		if (valignTypeEEnum == null) {
 			valignTypeEEnum = (EEnum)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(11);
 		}
 		return valignTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getValuetypeType() {
 		if (valuetypeTypeEEnum == null) {
 			valuetypeTypeEEnum = (EEnum)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(13);
 		}
 		return valuetypeTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getAlignTypeObject() {
 		if (alignTypeObjectEDataType == null) {
 			alignTypeObjectEDataType = (EDataType)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(1);
 		}
 		return alignTypeObjectEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getDeclareTypeObject() {
 		if (declareTypeObjectEDataType == null) {
 			declareTypeObjectEDataType = (EDataType)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(3);
 		}
 		return declareTypeObjectEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getFrameTypeObject() {
 		if (frameTypeObjectEDataType == null) {
 			frameTypeObjectEDataType = (EDataType)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(6);
 		}
 		return frameTypeObjectEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getRulesTypeObject() {
 		if (rulesTypeObjectEDataType == null) {
 			rulesTypeObjectEDataType = (EDataType)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(8);
 		}
 		return rulesTypeObjectEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getScopeTypeObject() {
 		if (scopeTypeObjectEDataType == null) {
 			scopeTypeObjectEDataType = (EDataType)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(10);
 		}
 		return scopeTypeObjectEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getValignTypeObject() {
 		if (valignTypeObjectEDataType == null) {
 			valignTypeObjectEDataType = (EDataType)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(12);
 		}
 		return valignTypeObjectEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getValuetypeTypeObject() {
 		if (valuetypeTypeObjectEDataType == null) {
 			valuetypeTypeObjectEDataType = (EDataType)EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI).getEClassifiers().get(14);
 		}
 		return valuetypeTypeObjectEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public XhtmlFactory getXhtmlFactory() {
 		return (XhtmlFactory)getEFactoryInstance();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private boolean isLoaded = false;
 
 	/**
 	 * Laods the package and any sub-packages from their serialized form.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void loadPackage() {
 		if (isLoaded) return;
 		isLoaded = true;
 
 		URL url = getClass().getResource(packageFilename);
 		if (url == null) {
 			throw new RuntimeException("Missing serialized package: " + packageFilename);
 		}
 		URI uri = URI.createURI(url.toString());
 		Resource resource = new EcoreResourceFactoryImpl().createResource(uri);
 		try {
 			resource.load(null);
 		}
 		catch (IOException exception) {
 			throw new WrappedException(exception);
 		}
 		initializeFromLoadedEPackage(this, (EPackage)resource.getContents().get(0));
 		createResource(eNS_URI);
 	}
 
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private boolean isFixed = false;
 
 	/**
 	 * Fixes up the loaded package, to make it appear as if it had been programmatically built.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void fixPackageContents() {
 		if (isFixed) return;
 		isFixed = true;
 		fixEClassifiers();
 	}
 
 	/**
 	 * Sets the instance class on the given classifier.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected void fixInstanceClass(EClassifier eClassifier) {
 		if (eClassifier.getInstanceClassName() == null) {
 			eClassifier.setInstanceClassName("org.eclipse.rmf.reqif10.xhtml." + eClassifier.getName());
 			setGeneratedClassName(eClassifier);
 		}
 	}
 
 } //XhtmlPackageImpl
