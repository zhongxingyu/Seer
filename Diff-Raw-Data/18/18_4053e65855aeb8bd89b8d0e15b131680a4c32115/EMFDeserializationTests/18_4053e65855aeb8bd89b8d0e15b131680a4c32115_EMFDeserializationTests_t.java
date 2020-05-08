 /**
  * Copyright (c) 2013 itemis AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   itemis AG - initial API and implementation
  */
 package org.eclipse.rmf.tests.serialization.env.emf;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.xerces.impl.Constants;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.XMLOptions;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMLOptionsImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
 import org.eclipse.emf.ecore.xml.namespace.XMLNamespacePackage;
 import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
 import org.eclipse.emf.ecore.xml.type.impl.AnyTypeImpl;
 import org.eclipse.emf.ecore.xml.type.impl.XMLTypeDocumentRootImpl;
 import org.eclipse.rmf.tests.serialization.env.emf.myreqif.CORECONTENTType;
 import org.eclipse.rmf.tests.serialization.env.emf.myreqif.DocumentRoot;
 import org.eclipse.rmf.tests.serialization.env.emf.myreqif.MyreqifPackage;
 import org.eclipse.rmf.tests.serialization.env.emf.myreqif.REQIF;
 import org.eclipse.rmf.tests.serialization.env.emf.myreqif.REQIFTOOLEXTENSION;
 import org.eclipse.rmf.tests.serialization.env.emf.myreqif.TOOLEXTENSIONSType;
 import org.eclipse.rmf.tests.serialization.env.emf.myreqif.util.MyreqifResourceFactoryImpl;
 import org.eclipse.xsd.XSDPackage;
 import org.eclipse.xsd.ecore.XSDEcoreBuilder;
 import org.eclipse.xsd.util.XSDResourceFactoryImpl;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 @SuppressWarnings("nls")
 public class EMFDeserializationTests {
 	public static final String DATA_BASEDIR = "resources/input/org.eclipse.rmf.tests.serialization.env/data/";
 	public static final String MODEL_BASEDIR = "resources/model/";
 
 	private static HashMap<String, Object> backupRegistry = new HashMap<String, Object>();
 
 	@BeforeClass
 	public static void setupOnce() {
 		backupRegistry = new HashMap<String, Object>();
 		backupRegistry.putAll(EPackage.Registry.INSTANCE);
 	}
 
 	@Before
 	public void setUp() {
 		EPackage.Registry.INSTANCE.clear();
		EPackage.Registry.INSTANCE.put(XMLTypePackage.eNS_URI, XMLTypePackage.eINSTANCE);
		EPackage.Registry.INSTANCE.put(XMLNamespacePackage.eNS_URI, XMLNamespacePackage.eINSTANCE);

 	}
 
 	@AfterClass
 	public static void tearDownOnce() {
 		EPackage.Registry.INSTANCE.clear();
 		EPackage.Registry.INSTANCE.putAll(backupRegistry);
 	}
 
 	/**
 	 * Expected behavior: create instances of generated classes
 	 * 
 	 * @throws IOException
 	 */
 
 	@Test
 	public void testPositiveloadResourceWithPreregisteredGeneratedPackage() throws IOException {
 		String fileName = DATA_BASEDIR + "bare.xml";
 
 		// prepare resource set
 		ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getPackageRegistry().put(MyreqifPackage.eNS_URI, MyreqifPackage.eINSTANCE);
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml", new MyreqifResourceFactoryImpl());
 
 		// load data
 		URI emfURI = URI.createURI(fileName, true);
 		Resource resource = resourceSet.createResource(emfURI);
 		HashMap<String, Object> options = new HashMap<String, Object>();
 		enableNewMethods(options);
 
 		resource.load(options);
 
 		// validate data
 		validateLoadErrors(resource);
 		validateBareDataWithGeneratedPackage(resource);
 	}
 
 	@Test
 	public void testPositiveloadResourceWithPreregisteredGeneratedPackageAndNestedAnyTypesWithRedefinedNsPrefix() throws IOException {
 		String fileName = DATA_BASEDIR + "simple_toolExtensions_redefinedNsPrefix.xml";
 
 		// prepare resource set
 		ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getPackageRegistry().put(MyreqifPackage.eNS_URI, MyreqifPackage.eINSTANCE);
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml", new MyreqifResourceFactoryImpl());
 
 		// load data
 		URI emfURI = URI.createURI(fileName, true);
 		Resource resource = resourceSet.createResource(emfURI);
 		HashMap<String, Object> options = new HashMap<String, Object>();
 		enableNewMethods(options);
 
 		resource.load(options);
 
 		// validate data
 		validateLoadErrors(resource);
 		validateBareDataWithGeneratedPackage(resource);
 		validateAnyTypeInGeneratedPackage(resource, 1);
 	}
 
 	@Test
 	public void testPositiveloadResourceWithPreregisteredGeneratedPackageAndNestedAnyAndEcoreTypes() throws IOException {
 		String fileName = DATA_BASEDIR + "bare_toolExtensions_AnyTypeAndEcoreType.xml";
 
 		// prepare resource set
 		ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getPackageRegistry().put(MyreqifPackage.eNS_URI, MyreqifPackage.eINSTANCE);
 		resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml", new MyreqifResourceFactoryImpl());
 
 		// load data
 		URI emfURI = URI.createURI(fileName, true);
 		Resource resource = resourceSet.createResource(emfURI);
 		HashMap<String, Object> options = new HashMap<String, Object>();
 		enableNewMethods(options);
 
 		resource.load(options);
 
 		// validate data
 		validateLoadErrors(resource);
 		validateBareDataWithGeneratedPackage(resource);
 		validateAnyTypeInGeneratedPackage(resource, 2);
 		validateEcoreInGeneratedPackage(resource, 2);
 	}
 
 	@Test
 	public void testPositiveloadResourceWithPreregisteredGeneratedPackageCheckOptionsSideEffects() throws IOException {
 		String fileName = DATA_BASEDIR + "bare.xml"; //$NON-NLS-1$
 
 		// prepare resource set
 		ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getPackageRegistry().put(MyreqifPackage.eNS_URI, MyreqifPackage.eINSTANCE);
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml", new MyreqifResourceFactoryImpl()); //$NON-NLS-1$
 
 		// load data
 		URI emfURI = URI.createURI(fileName, true);
 		Resource resource = resourceSet.createResource(emfURI);
 		HashMap<String, Object> options = new HashMap<String, Object>();
 		XMLOptions xmlOptions = new XMLOptionsImpl();
 		xmlOptions.setProcessAnyXML(true);
 		xmlOptions.setProcessSchemaLocations(true);
 		options.put(XMLResource.OPTION_XML_OPTIONS, xmlOptions);
 		enableNewMethods(options);
 
 		resource.load(options);
 
 		// validate data
 		validateLoadErrors(resource);
 		validateBareDataWithGeneratedPackage(resource);
 	}
 
 	@Test
 	public void testPositiveloadResourceWithPreregisteredPackageInEcoreFile() throws IOException {
 		String fileName = DATA_BASEDIR + "bare.xml"; //$NON-NLS-1$
 
 		// prepare resource set
 		ResourceSet resourceSet = new ResourceSetImpl();
 
 		// get the ecore
 		// NOTE:
 		// since the ecore is created from schema we have to register XMLTypeType and XMLNamespacePackage in the
 		// global package registry. It is important to register XMLTypePackage since XMLNamespacePackage requires it for
 		// initialization and searches the global registry only
 		resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl()); //$NON-NLS-1$
 		Resource ecoreResource = resourceSet.createResource(URI.createURI(MODEL_BASEDIR + "myreqif.ecore", true)); //$NON-NLS-1$
 		ecoreResource.load(null);
 		EPackage reqifPackage = (EPackage) ecoreResource.getContents().get(0);
 
 		// get the data
 		resourceSet.getPackageRegistry().put(MyreqifPackage.eNS_URI, reqifPackage);
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml", new XMLResourceFactoryImpl()); //$NON-NLS-1$
 
 		// load data
 		// note: loading an instance against an ecore that is generated from xsd requires extended meta data switched on
 		URI emfURI = URI.createURI(fileName, true);
 		Resource resource = resourceSet.createResource(emfURI);
 		HashMap<String, Object> options = new HashMap<String, Object>();
 		enableNewMethods(options);
 		options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
 		resource.load(options);
 
 		// validate data
 		validateLoadErrors(resource);
 		validateBareDataWithDynamicEcore(resource, reqifPackage);
 	}
 
 	@Test
 	public void testPositiveloadResourceWithPreregisteredPackageInXSDFile() throws IOException {
 		String fileName = DATA_BASEDIR + "bare.xml"; //$NON-NLS-1$
 
 		// prepare resource set
 		XSDEcoreBuilder xsdEcoreBuilder = new XSDEcoreBuilder();
 		Collection<EObject> reqifPackages = xsdEcoreBuilder.generate(URI.createFileURI(MODEL_BASEDIR + "myreqif.xsd")); //$NON-NLS-1$
 		EPackage reqifPackage = (EPackage) reqifPackages.iterator().next();
 
 		ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getPackageRegistry().put(MyreqifPackage.eNS_URI, reqifPackage);
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml", new XMLResourceFactoryImpl()); //$NON-NLS-1$
 
 		// load data
 		// note: loading an instance against an ecore that is generated from xsd requires extended meta data switched on
 		URI emfURI = URI.createURI(fileName, true);
 		Resource resource = resourceSet.createResource(emfURI);
 		HashMap<String, Object> options = new HashMap<String, Object>();
 		enableNewMethods(options);
 		options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
 		resource.load(options);
 
 		// validate data
 		validateLoadErrors(resource);
 		validateBareDataWithDynamicEcore(resource, reqifPackage);
 	}
 
 	@Test
 	public void testPositiveloadResourceWithoutPackageInfo() throws IOException {
 		String fileName = DATA_BASEDIR + "bare.xml"; //$NON-NLS-1$
 
 		// prepare resource set
 		ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml", new XMLResourceFactoryImpl()); //$NON-NLS-1$
 
 		// load data
 		// note: loading of XML files with unknown structure requires OPTION_RECORD_UNKNOWN_FEATURE
 		URI emfURI = URI.createURI(fileName, true);
 		Resource resource = resourceSet.createResource(emfURI);
 		HashMap<String, Object> options = new HashMap<String, Object>();
 		enableNewMethods(options);
 		XMLOptions xmlOptions = new XMLOptionsImpl();
 		xmlOptions.setProcessAnyXML(true);
 		options.put(XMLResource.OPTION_XML_OPTIONS, xmlOptions);
 
 		resource.load(options);
 
 		// validate data
 		assertEquals(1, resource.getContents().size());
 		EObject root = resource.getContents().get(0);
 
 		assertTrue(root instanceof XMLTypeDocumentRootImpl);
 		XMLTypeDocumentRootImpl documentRoot = (XMLTypeDocumentRootImpl) root;
 		org.eclipse.emf.ecore.util.FeatureMap.Entry reqIFEntry = documentRoot.getMixed().get(0);
 		assertEquals("REQ-IF", reqIFEntry.getEStructuralFeature().getName()); //$NON-NLS-1$
 
 		Object reqifObject = reqIFEntry.getValue();
 		assertTrue(reqifObject instanceof AnyTypeImpl);
 		AnyTypeImpl reqif = (AnyTypeImpl) reqifObject;
 		org.eclipse.emf.ecore.util.FeatureMap.Entry coreContentEntry = reqif.getAny().get(0);
 		assertEquals("CORE-CONTENT", coreContentEntry.getEStructuralFeature().getName()); //$NON-NLS-1$
 
 		Object coreContentObject = coreContentEntry.getValue();
 		assertTrue(coreContentObject instanceof AnyTypeImpl);
 		AnyTypeImpl coreContent = (AnyTypeImpl) coreContentObject;
 		org.eclipse.emf.ecore.util.FeatureMap.Entry reqifCoreContentEntry = coreContent.getAny().get(0);
 		assertEquals("REQ-IF-CONTENT", reqifCoreContentEntry.getEStructuralFeature().getName()); //$NON-NLS-1$
 
 		validateLoadErrors(resource);
 
 	}
 
 	@Test
 	public void testPositiveloadResourceWithLocationHintPackageInEcoreFile() throws IOException {
 		String fileName = DATA_BASEDIR + "bareWithEcoreHint.xml"; //$NON-NLS-1$
 
 		// prepare resource set
 		// NOTE:
 		// The following packages must be registered globally. Local definition doesn't work.
 		EPackage.Registry.INSTANCE.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
 
 		ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml", new XMLResourceFactoryImpl());
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
 		resourceSet.getPackageRegistry().put(XSDPackage.eNS_URI, XSDPackage.eINSTANCE);
 
 		// load data
 		// NOTE:
 		// loading an instance against an ecore that is generated from xsd requires extended meta data switched on. For
 		// considering the schema location hint, the XMLOptions need to enable it
 		URI emfURI = URI.createURI(fileName, true);
 		Resource resource = resourceSet.createResource(emfURI);
 		HashMap<String, Object> options = new HashMap<String, Object>();
 		enableNewMethods(options);
 		options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
 		resource.load(options);
 
 		EPackage reqifPackage = resourceSet.getPackageRegistry().getEPackage(MyreqifPackage.eNS_URI);
 
 		// validate data
 		validateLoadErrors(resource);
 		validateBareDataWithDynamicEcore(resource, reqifPackage);
 
 	}
 
 	@Test
 	public void testPositiveloadResourceWithLocationHintPackageInXSDFile() throws IOException {
 		String fileName = DATA_BASEDIR + "bareWithSchemaHint.xml"; //$NON-NLS-1$
 
 		// prepare resource set
 		ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml", new XMLResourceFactoryImpl());
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xsd", new XSDResourceFactoryImpl());
 
 		// load data
 		// note: loading an instance against an ecore that is generated from xsd requires extended meta data switched on
 		URI emfURI = URI.createURI(fileName, true);
 		Resource resource = resourceSet.createResource(emfURI);
 		HashMap<String, Object> options = new HashMap<String, Object>();
 		enableNewMethods(options);
 		options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
 
 		XMLOptions xmlOptions = new XMLOptionsImpl();
 		xmlOptions.setProcessSchemaLocations(true);
 		options.put(XMLResource.OPTION_XML_OPTIONS, xmlOptions);
 
 		resource.load(options);
 
 		EPackage reqifPackage = resourceSet.getPackageRegistry().getEPackage(MyreqifPackage.eNS_URI);
 
 		// validate data
 		validateLoadErrors(resource);
 		validateBareDataWithDynamicEcore(resource, reqifPackage);
 
 	}
 
 	private void validateBareDataWithDynamicEcore(Resource resource, EPackage reqifPackage) {
 		assertEquals(1, resource.getContents().size());
 		EObject documentRoot = resource.getContents().get(0);
 
 		assertFalse(documentRoot instanceof DocumentRoot);
 
 		EClass documentRootEClass = (EClass) reqifPackage.getEClassifier("DocumentRoot");
 		assertEquals(documentRootEClass, documentRoot.eClass());
 
 		EStructuralFeature reqifFeature = documentRootEClass.getEStructuralFeature("rEQIF");
 		Object reqifObject = documentRoot.eGet(reqifFeature);
 		assertNotNull(reqifObject);
 		assertTrue(reqifObject instanceof EObject);
 
 		EObject reqif = (EObject) reqifObject;
 		EClass reqifEClass = (EClass) reqifPackage.getEClassifier("REQIF");
 		assertEquals(reqifEClass, reqif.eClass());
 
 		EStructuralFeature coreContentFeature = reqifEClass.getEStructuralFeature("cORECONTENT");
 		Object coreContentObject = reqif.eGet(coreContentFeature);
 		assertNotNull(coreContentObject);
 
 	}
 
 	private void validateBareDataWithGeneratedPackage(Resource resource) {
 		// test data
 		assertEquals(1, resource.getContents().size());
 		EObject root = resource.getContents().get(0);
 
 		assertTrue(root instanceof DocumentRoot);
 		DocumentRoot documentRoot = (DocumentRoot) root;
 
 		assertNotNull(documentRoot.getREQIF());
 		REQIF reqif = documentRoot.getREQIF();
 
 		assertNotNull(reqif.getCORECONTENT());
 		CORECONTENTType coreContent = reqif.getCORECONTENT();
 
 		assertNotNull(coreContent.getREQIFCONTENT());
 	}
 
 	private void validateAnyTypeInGeneratedPackage(Resource resource, int expectedExtensionSize) {
 		// test data
 		assertEquals(1, resource.getContents().size());
 		EObject root = resource.getContents().get(0);
 
 		assertTrue(root instanceof DocumentRoot);
 		DocumentRoot documentRoot = (DocumentRoot) root;
 
 		assertNotNull(documentRoot.getREQIF());
 		REQIF reqif = documentRoot.getREQIF();
 
 		assertNotNull(reqif.getTOOLEXTENSIONS());
 		TOOLEXTENSIONSType toolExtensions = reqif.getTOOLEXTENSIONS();
 
 		assertNotNull(toolExtensions.getREQIFTOOLEXTENSION());
 		assertEquals(1, toolExtensions.getREQIFTOOLEXTENSION().size());
 		REQIFTOOLEXTENSION reqifToolExtension = toolExtensions.getREQIFTOOLEXTENSION().get(0);
 
 		assertNotNull(reqifToolExtension.getAny());
 		assertEquals(expectedExtensionSize, reqifToolExtension.getAny().size());
 		org.eclipse.emf.ecore.util.FeatureMap.Entry myExtensionEntry = reqifToolExtension.getAny().get(0);
 
 		Object myExtensionObject = myExtensionEntry.getValue();
 		assertTrue(myExtensionObject instanceof AnyTypeImpl);
 		assertEquals("myExtension", myExtensionEntry.getEStructuralFeature().getName());
 	}
 
 	private void validateEcoreInGeneratedPackage(Resource resource, int expectedExtensionSize) {
 		// test data
 		assertEquals(1, resource.getContents().size());
 		EObject root = resource.getContents().get(0);
 
 		assertTrue(root instanceof DocumentRoot);
 		DocumentRoot documentRoot = (DocumentRoot) root;
 
 		assertNotNull(documentRoot.getREQIF());
 		REQIF reqif = documentRoot.getREQIF();
 
 		assertNotNull(reqif.getTOOLEXTENSIONS());
 		TOOLEXTENSIONSType toolExtensions = reqif.getTOOLEXTENSIONS();
 
 		assertNotNull(toolExtensions.getREQIFTOOLEXTENSION());
 		assertEquals(1, toolExtensions.getREQIFTOOLEXTENSION().size());
 		REQIFTOOLEXTENSION reqifToolExtension = toolExtensions.getREQIFTOOLEXTENSION().get(0);
 
 		assertNotNull(reqifToolExtension.getAny());
 		assertEquals(expectedExtensionSize, reqifToolExtension.getAny().size());
 		org.eclipse.emf.ecore.util.FeatureMap.Entry myExtensionEntry = reqifToolExtension.getAny().get(1);
 
 		Object ePackageObject = myExtensionEntry.getValue();
 		assertTrue(ePackageObject instanceof EPackage);
 		EPackage ePackage = (EPackage) ePackageObject;
 		assertEquals(1, ePackage.getEClassifiers().size());
 	}
 
 	private void validateLoadErrors(Resource resource) {
 		assertEquals(0, resource.getErrors().size());
 		assertEquals(0, resource.getWarnings().size());
 	}
 
 	protected void enableNewMethods(Map<String, Object> options) {
 		// Retrieve application-defined XMLReader features (see http://xerces.apache.org/xerces2-j/features.html for
 		// available features and their details)
 		Map<String, Boolean> parserFeatures = new HashMap<String, Boolean>();
 
 		// Retrieve application-defined XMLReader properties (see http://xerces.apache.org/xerces2-j/properties.html
 		// for available properties and their details)
 		Map<String, Object> parserProperties = new HashMap<String, Object>();
 
 		// Perform namespace processing (prefixes will be stripped off element and attribute names and replaced with the
 		// corresponding namespace URIs) but do not report attributes used for namespace declarations, and do not report
 		// original prefixed names
 		parserFeatures.put(Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE, true);
 		parserFeatures.put(Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACE_PREFIXES_FEATURE, false);
 
 		options.put(XMLResource.OPTION_PARSER_FEATURES, parserFeatures);
 		options.put(XMLResource.OPTION_PARSER_PROPERTIES, parserProperties);
 		options.put(XMLResource.OPTION_USE_DEPRECATED_METHODS, Boolean.FALSE);
 
 	}
 
 }
