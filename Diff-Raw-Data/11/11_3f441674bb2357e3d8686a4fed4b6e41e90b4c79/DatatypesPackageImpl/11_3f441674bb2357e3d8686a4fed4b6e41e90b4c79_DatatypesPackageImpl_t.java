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
 package org.eclipse.rmf.reqif10.datatypes.impl;
 
 import java.math.BigInteger;
 
 import java.util.List;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EValidator;
 
 import org.eclipse.emf.ecore.impl.EPackageImpl;
 
import org.eclipse.emf.ecore.xml.namespace.XMLNamespacePackage;
 import org.eclipse.rmf.reqif10.datatypes.DatatypesFactory;
 import org.eclipse.rmf.reqif10.datatypes.DatatypesPackage;
 import org.eclipse.rmf.reqif10.datatypes.FrameTargetMember0;
 
 import org.eclipse.rmf.reqif10.datatypes.util.DatatypesValidator;
 
 import org.eclipse.rmf.reqif10.xhtml.XhtmlPackage;
 
 import org.eclipse.rmf.reqif10.xhtml.impl.XhtmlPackageImpl;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model <b>Package</b>.
  * <!-- end-user-doc -->
  * @generated
  */
 public class DatatypesPackageImpl extends EPackageImpl implements DatatypesPackage {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum frameTargetMember0EEnum = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType cdataEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType characterEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType charsetEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType charsetsEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType colorEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType colorMember1EDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType contentTypeEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType contentTypesEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType curieEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType curiEsEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType datetimeEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType fpiEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType frameTargetEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType frameTargetMember0ObjectEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType frameTargetMember1EDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType languageCodeEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType languageCodesEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType lengthEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType lengthMember1EDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType linkTypesEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType mediaDescEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType multiLengthEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType multiLengthMember1EDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType multiLengthsEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType numberEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType pixelsEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType safeCURIEEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType safeCURIEsEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType scriptEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType textEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType uriEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType urIorSafeCURIEEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType urIorSafeCURIEsEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType urirefEDataType = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType urIsEDataType = null;
 
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
 	 * @see org.eclipse.rmf.reqif10.datatypes.DatatypesPackage#eNS_URI
 	 * @see #init()
 	 * @generated
 	 */
 	private DatatypesPackageImpl() {
 		super(eNS_URI, DatatypesFactory.eINSTANCE);
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
 	 * <p>This method is used to initialize {@link DatatypesPackage#eINSTANCE} when that field is accessed.
 	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #eNS_URI
 	 * @see #createPackageContents()
 	 * @see #initializePackageContents()
 	 * @generated
 	 */
 	public static DatatypesPackage init() {
 		if (isInited) return (DatatypesPackage)EPackage.Registry.INSTANCE.getEPackage(DatatypesPackage.eNS_URI);
 
 		// Obtain or create and register package
 		DatatypesPackageImpl theDatatypesPackage = (DatatypesPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof DatatypesPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new DatatypesPackageImpl());
 
 		isInited = true;
 
		// Initialize simple dependencies
		XMLNamespacePackage.eINSTANCE.eClass();

 		// Obtain or create and register interdependencies
 		XhtmlPackageImpl theXhtmlPackage = (XhtmlPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI) instanceof XhtmlPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(XhtmlPackage.eNS_URI) : XhtmlPackage.eINSTANCE);
 
 		// Load packages
 		theXhtmlPackage.loadPackage();
 
 		// Create package meta-data objects
 		theDatatypesPackage.createPackageContents();
 
 		// Initialize created meta-data
 		theDatatypesPackage.initializePackageContents();
 
 		// Fix loaded packages
 		theXhtmlPackage.fixPackageContents();
 
 		// Register package validator
 		EValidator.Registry.INSTANCE.put
 			(theDatatypesPackage, 
 			 new EValidator.Descriptor() {
 				 public EValidator getEValidator() {
 					 return DatatypesValidator.INSTANCE;
 				 }
 			 });
 
 		// Mark meta-data to indicate it can't be changed
 		theDatatypesPackage.freeze();
 
   
 		// Update the registry and return the package
 		EPackage.Registry.INSTANCE.put(DatatypesPackage.eNS_URI, theDatatypesPackage);
 		return theDatatypesPackage;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getFrameTargetMember0() {
 		return frameTargetMember0EEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getCDATA() {
 		return cdataEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getCharacter() {
 		return characterEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getCharset() {
 		return charsetEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getCharsets() {
 		return charsetsEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getColor() {
 		return colorEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getColorMember1() {
 		return colorMember1EDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getContentType() {
 		return contentTypeEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getContentTypes() {
 		return contentTypesEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getCURIE() {
 		return curieEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getCURIEs() {
 		return curiEsEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getDatetime() {
 		return datetimeEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getFPI() {
 		return fpiEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getFrameTarget() {
 		return frameTargetEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getFrameTargetMember0Object() {
 		return frameTargetMember0ObjectEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getFrameTargetMember1() {
 		return frameTargetMember1EDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getLanguageCode() {
 		return languageCodeEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getLanguageCodes() {
 		return languageCodesEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getLength() {
 		return lengthEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getLengthMember1() {
 		return lengthMember1EDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getLinkTypes() {
 		return linkTypesEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getMediaDesc() {
 		return mediaDescEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getMultiLength() {
 		return multiLengthEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getMultiLengthMember1() {
 		return multiLengthMember1EDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getMultiLengths() {
 		return multiLengthsEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getNumber() {
 		return numberEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getPixels() {
 		return pixelsEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getSafeCURIE() {
 		return safeCURIEEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getSafeCURIEs() {
 		return safeCURIEsEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getScript() {
 		return scriptEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getText() {
 		return textEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getURI() {
 		return uriEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getURIorSafeCURIE() {
 		return urIorSafeCURIEEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getURIorSafeCURIEs() {
 		return urIorSafeCURIEsEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getURIREF() {
 		return urirefEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getURIs() {
 		return urIsEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DatatypesFactory getDatatypesFactory() {
 		return (DatatypesFactory)getEFactoryInstance();
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
 
 		// Create enums
 		frameTargetMember0EEnum = createEEnum(FRAME_TARGET_MEMBER0);
 
 		// Create data types
 		cdataEDataType = createEDataType(CDATA);
 		characterEDataType = createEDataType(CHARACTER);
 		charsetEDataType = createEDataType(CHARSET);
 		charsetsEDataType = createEDataType(CHARSETS);
 		colorEDataType = createEDataType(COLOR);
 		colorMember1EDataType = createEDataType(COLOR_MEMBER1);
 		contentTypeEDataType = createEDataType(CONTENT_TYPE);
 		contentTypesEDataType = createEDataType(CONTENT_TYPES);
 		curieEDataType = createEDataType(CURIE);
 		curiEsEDataType = createEDataType(CURI_ES);
 		datetimeEDataType = createEDataType(DATETIME);
 		fpiEDataType = createEDataType(FPI);
 		frameTargetEDataType = createEDataType(FRAME_TARGET);
 		frameTargetMember0ObjectEDataType = createEDataType(FRAME_TARGET_MEMBER0_OBJECT);
 		frameTargetMember1EDataType = createEDataType(FRAME_TARGET_MEMBER1);
 		languageCodeEDataType = createEDataType(LANGUAGE_CODE);
 		languageCodesEDataType = createEDataType(LANGUAGE_CODES);
 		lengthEDataType = createEDataType(LENGTH);
 		lengthMember1EDataType = createEDataType(LENGTH_MEMBER1);
 		linkTypesEDataType = createEDataType(LINK_TYPES);
 		mediaDescEDataType = createEDataType(MEDIA_DESC);
 		multiLengthEDataType = createEDataType(MULTI_LENGTH);
 		multiLengthMember1EDataType = createEDataType(MULTI_LENGTH_MEMBER1);
 		multiLengthsEDataType = createEDataType(MULTI_LENGTHS);
 		numberEDataType = createEDataType(NUMBER);
 		pixelsEDataType = createEDataType(PIXELS);
 		safeCURIEEDataType = createEDataType(SAFE_CURIE);
 		safeCURIEsEDataType = createEDataType(SAFE_CURI_ES);
 		scriptEDataType = createEDataType(SCRIPT);
 		textEDataType = createEDataType(TEXT);
 		uriEDataType = createEDataType(URI);
 		urIorSafeCURIEEDataType = createEDataType(UR_IOR_SAFE_CURIE);
 		urIorSafeCURIEsEDataType = createEDataType(UR_IOR_SAFE_CURI_ES);
 		urirefEDataType = createEDataType(URIREF);
 		urIsEDataType = createEDataType(UR_IS);
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
 
 		// Initialize enums and add enum literals
 		initEEnum(frameTargetMember0EEnum, FrameTargetMember0.class, "FrameTargetMember0");
 		addEEnumLiteral(frameTargetMember0EEnum, FrameTargetMember0.BLANK);
 		addEEnumLiteral(frameTargetMember0EEnum, FrameTargetMember0.SELF);
 		addEEnumLiteral(frameTargetMember0EEnum, FrameTargetMember0.PARENT);
 		addEEnumLiteral(frameTargetMember0EEnum, FrameTargetMember0.TOP);
 
 		// Initialize data types
 		initEDataType(cdataEDataType, String.class, "CDATA", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(characterEDataType, String.class, "Character", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(charsetEDataType, String.class, "Charset", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(charsetsEDataType, List.class, "Charsets", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(colorEDataType, String.class, "Color", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(colorMember1EDataType, String.class, "ColorMember1", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(contentTypeEDataType, String.class, "ContentType", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(contentTypesEDataType, String.class, "ContentTypes", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(curieEDataType, String.class, "CURIE", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(curiEsEDataType, List.class, "CURIEs", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(datetimeEDataType, XMLGregorianCalendar.class, "Datetime", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(fpiEDataType, String.class, "FPI", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(frameTargetEDataType, Object.class, "FrameTarget", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(frameTargetMember0ObjectEDataType, FrameTargetMember0.class, "FrameTargetMember0Object", IS_SERIALIZABLE, IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(frameTargetMember1EDataType, String.class, "FrameTargetMember1", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(languageCodeEDataType, String.class, "LanguageCode", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(languageCodesEDataType, String.class, "LanguageCodes", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(lengthEDataType, Object.class, "Length", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(lengthMember1EDataType, String.class, "LengthMember1", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(linkTypesEDataType, List.class, "LinkTypes", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(mediaDescEDataType, String.class, "MediaDesc", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(multiLengthEDataType, Object.class, "MultiLength", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(multiLengthMember1EDataType, String.class, "MultiLengthMember1", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(multiLengthsEDataType, String.class, "MultiLengths", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(numberEDataType, BigInteger.class, "Number", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(pixelsEDataType, BigInteger.class, "Pixels", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(safeCURIEEDataType, String.class, "SafeCURIE", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(safeCURIEsEDataType, List.class, "SafeCURIEs", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(scriptEDataType, String.class, "Script", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(textEDataType, String.class, "Text", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(uriEDataType, String.class, "URI", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(urIorSafeCURIEEDataType, String.class, "URIorSafeCURIE", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(urIorSafeCURIEsEDataType, List.class, "URIorSafeCURIEs", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(urirefEDataType, String.class, "URIREF", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 		initEDataType(urIsEDataType, List.class, "URIs", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
 
 		// Create resource
 		createResource(eNS_URI);
 
 		// Create annotations
 		// http:///org/eclipse/emf/ecore/util/ExtendedMetaData
 		createExtendedMetaDataAnnotations();
 	}
 
 	/**
 	 * Initializes the annotations for <b>http:///org/eclipse/emf/ecore/util/ExtendedMetaData</b>.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void createExtendedMetaDataAnnotations() {
 		String source = "http:///org/eclipse/emf/ecore/util/ExtendedMetaData";			
 		addAnnotation
 		  (cdataEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "CDATA",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#string"
 		   });		
 		addAnnotation
 		  (characterEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "Character",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#string",
 			 "length", "1"
 		   });		
 		addAnnotation
 		  (charsetEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "Charset",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#string"
 		   });		
 		addAnnotation
 		  (charsetsEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "Charsets",
 			 "itemType", "Charset"
 		   });		
 		addAnnotation
 		  (colorEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "Color",
 			 "memberTypes", "http://www.eclipse.org/emf/2003/XMLType#NMTOKEN Color_._member_._1"
 		   });		
 		addAnnotation
 		  (colorMember1EDataType, 
 		   source, 
 		   new String[] {
 			 "name", "Color_._member_._1",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#token",
 			 "pattern", "#[0-9a-fA-F]{3}([0-9a-fA-F]{3})?"
 		   });		
 		addAnnotation
 		  (contentTypeEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "ContentType",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#string"
 		   });		
 		addAnnotation
 		  (contentTypesEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "ContentTypes",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#string"
 		   });		
 		addAnnotation
 		  (curieEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "CURIE",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#string",
 			 "minLength", "1",
 			 "pattern", "(([\\i-[:]][\\c-[:]]*)?:)?.+"
 		   });		
 		addAnnotation
 		  (curiEsEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "CURIEs",
 			 "itemType", "CURIE"
 		   });		
 		addAnnotation
 		  (datetimeEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "Datetime",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#dateTime"
 		   });		
 		addAnnotation
 		  (fpiEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "FPI",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#normalizedString"
 		   });		
 		addAnnotation
 		  (frameTargetEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "FrameTarget",
 			 "memberTypes", "FrameTarget_._member_._0 FrameTarget_._member_._1"
 		   });		
 		addAnnotation
 		  (frameTargetMember0EEnum, 
 		   source, 
 		   new String[] {
 			 "name", "FrameTarget_._member_._0"
 		   });		
 		addAnnotation
 		  (frameTargetMember0ObjectEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "FrameTarget_._member_._0:Object",
 			 "baseType", "FrameTarget_._member_._0"
 		   });		
 		addAnnotation
 		  (frameTargetMember1EDataType, 
 		   source, 
 		   new String[] {
 			 "name", "FrameTarget_._member_._1",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#string",
 			 "pattern", "[a-zA-Z].*"
 		   });		
 		addAnnotation
 		  (languageCodeEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "LanguageCode",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#language"
 		   });		
 		addAnnotation
 		  (languageCodesEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "LanguageCodes",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#string"
 		   });		
 		addAnnotation
 		  (lengthEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "Length",
 			 "memberTypes", "http://www.eclipse.org/emf/2003/XMLType#nonNegativeInteger Length_._member_._1"
 		   });		
 		addAnnotation
 		  (lengthMember1EDataType, 
 		   source, 
 		   new String[] {
 			 "name", "Length_._member_._1",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#token",
 			 "pattern", "\\d+[%25]|\\d*\\.\\d+[%25]"
 		   });		
 		addAnnotation
 		  (linkTypesEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "LinkTypes",
 			 "itemType", "http://www.eclipse.org/emf/2003/XMLType#NMTOKEN"
 		   });		
 		addAnnotation
 		  (mediaDescEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "MediaDesc",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#string"
 		   });		
 		addAnnotation
 		  (multiLengthEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "MultiLength",
 			 "memberTypes", "Length MultiLength_._member_._1"
 		   });		
 		addAnnotation
 		  (multiLengthMember1EDataType, 
 		   source, 
 		   new String[] {
 			 "name", "MultiLength_._member_._1",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#token",
 			 "pattern", "\\d*\\*"
 		   });		
 		addAnnotation
 		  (multiLengthsEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "MultiLengths",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#string"
 		   });		
 		addAnnotation
 		  (numberEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "Number",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#nonNegativeInteger"
 		   });		
 		addAnnotation
 		  (pixelsEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "Pixels",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#nonNegativeInteger"
 		   });		
 		addAnnotation
 		  (safeCURIEEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "SafeCURIE",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#string",
 			 "minLength", "3",
 			 "pattern", "\\[(([\\i-[:]][\\c-[:]]*)?:)?.+\\]"
 		   });		
 		addAnnotation
 		  (safeCURIEsEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "SafeCURIEs",
 			 "itemType", "SafeCURIE"
 		   });		
 		addAnnotation
 		  (scriptEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "Script",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#string"
 		   });		
 		addAnnotation
 		  (textEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "Text",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#string"
 		   });		
 		addAnnotation
 		  (uriEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "URI",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#anyURI"
 		   });		
 		addAnnotation
 		  (urIorSafeCURIEEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "URIorSafeCURIE",
 			 "memberTypes", "http://www.eclipse.org/emf/2003/XMLType#anyURI SafeCURIE"
 		   });		
 		addAnnotation
 		  (urIorSafeCURIEsEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "URIorSafeCURIEs",
 			 "itemType", "URIorSafeCURIE"
 		   });		
 		addAnnotation
 		  (urirefEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "URIREF",
 			 "baseType", "http://www.eclipse.org/emf/2003/XMLType#string",
 			 "minLength", "1",
 			 "pattern", "#\\c*"
 		   });		
 		addAnnotation
 		  (urIsEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "URIs",
 			 "itemType", "http://www.eclipse.org/emf/2003/XMLType#anyURI"
 		   });
 	}
 
 } //DatatypesPackageImpl
