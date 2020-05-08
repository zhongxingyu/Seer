 /*
  * Copyright (c) 2005, 2007 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.tests.gen;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Random;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.codegen.ecore.genmodel.GenClass;
 import org.eclipse.emf.codegen.ecore.genmodel.GenFeature;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModelFactory;
 import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
 import org.eclipse.emf.codegen.util.CodeGenUtil;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.BasicEList.UnmodifiableEList;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcoreFactory;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.gmf.codegen.gmfgen.Behaviour;
 import org.eclipse.gmf.codegen.gmfgen.ElementType;
 import org.eclipse.gmf.codegen.gmfgen.FeatureLabelModelFacet;
 import org.eclipse.gmf.codegen.gmfgen.FeatureLinkModelFacet;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenFactory;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenPackage;
 import org.eclipse.gmf.codegen.gmfgen.GenApplication;
 import org.eclipse.gmf.codegen.gmfgen.GenAuditContainer;
 import org.eclipse.gmf.codegen.gmfgen.GenAuditRoot;
 import org.eclipse.gmf.codegen.gmfgen.GenAuditRule;
 import org.eclipse.gmf.codegen.gmfgen.GenChildContainer;
 import org.eclipse.gmf.codegen.gmfgen.GenChildLabelNode;
 import org.eclipse.gmf.codegen.gmfgen.GenChildNode;
 import org.eclipse.gmf.codegen.gmfgen.GenCommonBase;
 import org.eclipse.gmf.codegen.gmfgen.GenCompartment;
 import org.eclipse.gmf.codegen.gmfgen.GenContainerBase;
 import org.eclipse.gmf.codegen.gmfgen.GenDiagram;
 import org.eclipse.gmf.codegen.gmfgen.GenDiagramUpdater;
 import org.eclipse.gmf.codegen.gmfgen.GenEditorGenerator;
 import org.eclipse.gmf.codegen.gmfgen.GenEditorView;
 import org.eclipse.gmf.codegen.gmfgen.GenExpressionInterpreter;
 import org.eclipse.gmf.codegen.gmfgen.GenExpressionProviderContainer;
 import org.eclipse.gmf.codegen.gmfgen.GenExternalNodeLabel;
 import org.eclipse.gmf.codegen.gmfgen.GenLabel;
 import org.eclipse.gmf.codegen.gmfgen.GenLink;
 import org.eclipse.gmf.codegen.gmfgen.GenLinkLabel;
 import org.eclipse.gmf.codegen.gmfgen.GenNavigator;
 import org.eclipse.gmf.codegen.gmfgen.GenNode;
 import org.eclipse.gmf.codegen.gmfgen.GenNodeLabel;
 import org.eclipse.gmf.codegen.gmfgen.GenPlugin;
 import org.eclipse.gmf.codegen.gmfgen.GenPropertySheet;
 import org.eclipse.gmf.codegen.gmfgen.GenTopLevelNode;
 import org.eclipse.gmf.codegen.gmfgen.LinkModelFacet;
 import org.eclipse.gmf.codegen.gmfgen.MetamodelType;
 import org.eclipse.gmf.codegen.gmfgen.Palette;
 import org.eclipse.gmf.codegen.gmfgen.SpecializationType;
 import org.eclipse.gmf.codegen.gmfgen.TypeLinkModelFacet;
 import org.eclipse.gmf.codegen.gmfgen.TypeModelFacet;
 import org.eclipse.gmf.codegen.gmfgen.Viewmap;
 import org.eclipse.gmf.codegen.gmfgen.ViewmapLayoutType;
 import org.eclipse.gmf.tests.ConfiguredTestCase;
 import org.eclipse.jdt.core.JavaConventions;
 import org.eclipse.jdt.core.JavaCore;
 
 /**
  * Tests for handcoded method implementations in GMFGen model
  * @author artem
  */
 public class HandcodedImplTest extends ConfiguredTestCase {
 	
 	private static final String INVALID_JAVA_CHARS = "<>?#!. =\"'\n\t\\";
 	
 	private static final Random RANDOM_GENERATOR = new Random();
 	
 	private GenDiagram myGenModel;
 
 	private final String javaLevel = JavaCore.VERSION_1_4;
 
 	public HandcodedImplTest(String name) {
 		super(name);
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		// FIXME need complex genmodel with a lot of nodes and links to make tests effective 
 		myGenModel = getSetup().getGenModel().getGenDiagram();
 		// additional elements to check (XXX perhaps, move to SessionSetup.mapping?
 		if (myGenModel.getEditorGen().getAudits() == null) {
 			GenAuditRoot root;
 			myGenModel.getEditorGen().setAudits(root = GMFGenFactory.eINSTANCE.createGenAuditRoot());
 			GenAuditContainer cat;
 			root.getCategories().add(cat = GMFGenFactory.eINSTANCE.createGenAuditContainer());
 			GenAuditRule rule;
 			root.getRules().add(rule = GMFGenFactory.eINSTANCE.createGenAuditRule());
 			rule.setCategory(cat);
 			rule.setTarget(GMFGenFactory.eINSTANCE.createGenDomainElementTarget());
 		}
 	}
 
 	public void testUniqueIdentifier_IsUnique() {
 		HashSet<String> allIds = new HashSet<String>(1<<7);
 		for (GenCommonBaseIterator it = new GenCommonBaseIterator(myGenModel); it.hasNext();) {
 			GenCommonBase next = it.next();
 			assertFalse("There should be no two same 'unique' identifiers in GMFGen", allIds.contains(next.getUniqueIdentifier()));
 			allIds.add(next.getUniqueIdentifier());
 		}
 		assertTrue("Test is not valid unless few elements were checked", allIds.size() > 1);
 		allIds.clear();
 	}
 
 	public void testUniqueIdentifier_IsConstant() {
 		LinkedList<String> allIdsOrdered = new LinkedList<String>();
 		for (GenCommonBaseIterator it = new GenCommonBaseIterator(myGenModel); it.hasNext();) {
 			GenCommonBase next = it.next();
 			allIdsOrdered.add(next.getUniqueIdentifier());
 		}
 		assertTrue("Test is not valid unless there are few elements to check", allIdsOrdered.size() > 1);
 		Iterator<String> itSaved = allIdsOrdered.iterator();
 		GenCommonBaseIterator it = new GenCommonBaseIterator(myGenModel);
 		for (; it.hasNext() && itSaved.hasNext();) {
 			GenCommonBase next = it.next();
 			String savedID = itSaved.next().toString();
 			assertEquals("Subsequent invocations of getUniqueIdentifier produce different results", savedID, next.getUniqueIdentifier());
 		}
 		assertEquals("Lists are not equal in size", itSaved.hasNext(), it.hasNext());
 		allIdsOrdered.clear();
 	}
 
 	public void testGenEditorGenerator_DomainFileExtension(){
 		GenEditorGenerator generator = GMFGenFactory.eINSTANCE.createGenEditorGenerator();
 		generator.setDomainFileExtension("AAA");
 		assertEquals("AAA", generator.getDomainFileExtension());
 		
 		generator.setDomainFileExtension(null);
 		assertNotNull(generator.getDomainFileExtension());
 		
 		GenModel genModel = GenModelFactory.eINSTANCE.createGenModel();
 		GenPackage genPackage = GenModelFactory.eINSTANCE.createGenPackage();
 		genPackage.setPrefix("CBA");
 		genModel.getGenPackages().add(genPackage);
 		generator.setDomainGenModel(genModel);
 		assertNotNull(generator.getDomainFileExtension());
 		assertEquals("cba", generator.getDomainFileExtension());
 
 		generator.setDomainFileExtension("");
 		assertNotNull(generator.getDomainFileExtension());
 		assertEquals("cba", generator.getDomainFileExtension());
 
 		generator.setDomainFileExtension(" ");
 		assertNotNull(generator.getDomainFileExtension());
 		assertEquals("cba", generator.getDomainFileExtension());
 	}
 	
 	public void testGenEditorGenerator_DiagramFileExtension(){
 		GenEditorGenerator generator = GMFGenFactory.eINSTANCE.createGenEditorGenerator();
 		generator.setDomainFileExtension("AAA");
 		checkStringPropertyWithDefault(generator, GMFGenPackage.eINSTANCE.getGenEditorGenerator_DiagramFileExtension());
 	}
 	
 	public void testGenEditorGenerator_ModelId(){
 		GenEditorGenerator generator = GMFGenFactory.eINSTANCE.createGenEditorGenerator();
 		checkStringPropertyWithDefault(generator, GMFGenPackage.eINSTANCE.getGenEditorGenerator_ModelID());
 		
 		generator = GMFGenFactory.eINSTANCE.createGenEditorGenerator();
 		GenModel mockGenModel = GenModelFactory.eINSTANCE.createGenModel();
 		mockGenModel.setModelName("CBA");
 		generator.setDomainGenModel(mockGenModel);
 		assertEquals("CBA", generator.getModelID());
 	}
 	
 	public void testGenEditorGenerator_PackageNamePrefix() {
 		GenEditorGenerator generator = GMFGenFactory.eINSTANCE.createGenEditorGenerator();
 		generator.setPackageNamePrefix(null);
 		assertNotNull(generator.getPackageNamePrefix());
 
 		generator.setPackageNamePrefix("ABC");
 		assertEquals("ABC", generator.getPackageNamePrefix());
 
 		GenModel genModel = GenModelFactory.eINSTANCE.createGenModel();
 		GenPackage genPackage = GenModelFactory.eINSTANCE.createGenPackage();
 		genPackage.setBasePackage("CBA");
 		genModel.getGenPackages().add(genPackage);
 		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
 		ePackage.setName("DEF");
 		genPackage.setEcorePackage(ePackage);
 		generator.setDomainGenModel(genModel);
 		assertEquals("ABC", generator.getPackageNamePrefix());
 		
 		checkStringPropertyWithDefault(generator, GMFGenPackage.eINSTANCE.getGenEditorGenerator_PackageNamePrefix());
 	}
 	
 	public void testGenEditorGenerator_getAllDomainGenPackages() {
 		GenModel genModel = GenModelFactory.eINSTANCE.createGenModel();
 		genModel.setModelPluginID("plugin1");
 		genModel.setModelDirectory("modelDir1");
 		genModel.setEditDirectory("dir1");
 		genModel.setEditPluginClass("EditPluginClassName");
 		GenPackage genPackage1 = GenModelFactory.eINSTANCE.createGenPackage();
 		GenClass genClass = GenModelFactory.eINSTANCE.createGenClass();
 		genPackage1.getGenClasses().add(genClass);
 		genModel.getGenPackages().add(genPackage1);
 		GenPackage genPackage2 = GenModelFactory.eINSTANCE.createGenPackage();
 		genModel.getGenPackages().add(genPackage2);
 		
 		GenEditorGenerator generator = GMFGenFactory.eINSTANCE.createGenEditorGenerator();
 		generator.setDomainGenModel(genModel);
 		
 		assertTrue(generator.getAllDomainGenPackages(false).size() == 1);
 		assertEquals(genPackage1, generator.getAllDomainGenPackages(false).get(0));
 		
 		GenModel genModel2 = GenModelFactory.eINSTANCE.createGenModel();
 		genModel2.setModelPluginID("plugin2");
 		genModel2.setModelDirectory("modelDir2");
 		genModel2.setEditDirectory("dir2");
 		genModel2.setEditPluginClass("EditPluginClassName2");
 		GenPackage genPackage3 = GenModelFactory.eINSTANCE.createGenPackage();
 		genClass = GenModelFactory.eINSTANCE.createGenClass();
 		genPackage3.getGenClasses().add(genClass);
 		genModel2.getGenPackages().add(genPackage3);
 		GenPackage genPackage4 = GenModelFactory.eINSTANCE.createGenPackage();
 		genModel2.getGenPackages().add(genPackage4);
 		genModel.getUsedGenPackages().add(genPackage3);
 		genModel.getUsedGenPackages().add(genPackage4);
 		
 		assertTrue(generator.getAllDomainGenPackages(false).size() == 1);
 		assertEquals(genPackage1, generator.getAllDomainGenPackages(false).get(0));		
 
 		assertTrue(generator.getAllDomainGenPackages(true).size() == 2);
 		assertTrue(generator.getAllDomainGenPackages(true).contains(genPackage1));		
 		assertTrue(generator.getAllDomainGenPackages(true).contains(genPackage3));
 	}
 	
 	public void testGenPlugin_RequiredPluginIds(){
 		final String BUNDLE_EXPRESSIONS = "com.mycompany.expressions";
 		final String[] BUNDLE_VIEWMAPS_MANY = {"com.mycompany.viewmapsA", "com.mycompany.viewmapsB"};
 		final String BUNDLE_VIEWMAPS_ONE = "com.mycompany.viewmapsC";
 
 		GenEditorGenerator mockGenerator = GMFGenFactory.eINSTANCE.createGenEditorGenerator();
 		
 		GenExpressionProviderContainer expressionProviderContainer = GMFGenFactory.eINSTANCE.createGenExpressionProviderContainer();
 		mockGenerator.setExpressionProviders(expressionProviderContainer);
 		GenExpressionInterpreter expressionProvider = GMFGenFactory.eINSTANCE.createGenExpressionInterpreter();
 		expressionProviderContainer.getProviders().add(expressionProvider);
 		
 		GenPlugin mockPlugin = GMFGenFactory.eINSTANCE.createGenPlugin();
 		mockPlugin.getRequiredPlugins().add(BUNDLE_EXPRESSIONS);
 		mockGenerator.setPlugin(mockPlugin);
 		
 		GenDiagram mockDiagram = GMFGenFactory.eINSTANCE.createGenDiagram();
 		mockGenerator.setDiagram(mockDiagram);
 		
 		GenTopLevelNode mockNodeA = GMFGenFactory.eINSTANCE.createGenTopLevelNode();
 		mockDiagram.getTopLevelNodes().add(mockNodeA);
 		Viewmap mockViewmapA = GMFGenFactory.eINSTANCE.createFigureViewmap();
 		mockViewmapA.getRequiredPluginIDs().add(BUNDLE_VIEWMAPS_ONE);
 		mockNodeA.setViewmap(mockViewmapA);
 		
 		GenTopLevelNode mockNodeB = GMFGenFactory.eINSTANCE.createGenTopLevelNode();
 		mockDiagram.getTopLevelNodes().add(mockNodeB);
 		Viewmap mockViewmapB = GMFGenFactory.eINSTANCE.createFigureViewmap();
 		mockViewmapB.getRequiredPluginIDs().addAll(Arrays.asList(BUNDLE_VIEWMAPS_MANY));
 		mockNodeB.setViewmap(mockViewmapB);
 		
 		List<String> allRequired = mockPlugin.getAllRequiredPlugins();  
 		assertTrue(allRequired.contains(BUNDLE_EXPRESSIONS));
 		assertTrue(allRequired.contains(BUNDLE_VIEWMAPS_ONE));
 		assertTrue(allRequired.containsAll(Arrays.asList(BUNDLE_VIEWMAPS_MANY)));
 	}
 	
 	public void testGenPlugin_ID() {
 		GenPlugin genPlugin = GMFGenFactory.eINSTANCE.createGenPlugin();
 		GenEditorGenerator editorGen = GMFGenFactory.eINSTANCE.createGenEditorGenerator();
 		editorGen.setPlugin(genPlugin);
 		checkStringPropertyWithDefault(genPlugin, GMFGenPackage.eINSTANCE.getGenPlugin_ID());
 	}
 	
 	public void testGenPlugin_Name() {
 		GenEditorGenerator genEditorGenerator = GMFGenFactory.eINSTANCE.createGenEditorGenerator();
 		GenPlugin genPlugin = GMFGenFactory.eINSTANCE.createGenPlugin();
 		genEditorGenerator.setPlugin(genPlugin);
 		checkStringPropertyWithDefault(genPlugin, GMFGenPackage.eINSTANCE.getGenPlugin_Name());
 	}
 	
 	public void testGenDiagram_EditingDomainID() {
 		GenDiagram genDiagram = GMFGenFactory.eINSTANCE.createGenDiagram();
 		GenEditorGenerator editorGen = GMFGenFactory.eINSTANCE.createGenEditorGenerator();
 		editorGen.setDiagram(genDiagram);
 		editorGen.setPlugin(GMFGenFactory.eINSTANCE.createGenPlugin());
 		checkStringPropertyWithDefault(genDiagram, GMFGenPackage.eINSTANCE.getEditorCandies_EditingDomainID());
 	}
 	
 	public void testGenDiagram_generateCreateShortcutAction() {
 		GenDiagram genDiagram = GMFGenFactory.eINSTANCE.createGenDiagram();
 		genDiagram.getContainsShortcutsTo().clear();
 		assertFalse(genDiagram.generateCreateShortcutAction());
 		
 		genDiagram.getContainsShortcutsTo().add("ecore");
 		assertTrue(genDiagram.generateCreateShortcutAction());
 	}
 	
 	public void testGenDiagram_generateShortcutIcon() {
 		GenDiagram genDiagram = GMFGenFactory.eINSTANCE.createGenDiagram();
 		genDiagram.getShortcutsProvidedFor().clear();
 		assertFalse(genDiagram.generateShortcutIcon());
 		
 		genDiagram.getShortcutsProvidedFor().add("Ecore");
 		assertTrue(genDiagram.generateShortcutIcon());
 	}
 	
 	public void testGenDiagram_generateInitDiagramAction() {
 		GenDiagram genDiagram = GMFGenFactory.eINSTANCE.createGenDiagram();
 		assertFalse(genDiagram.generateInitDiagramAction());
 		
 		genDiagram.setDomainDiagramElement(GenModelFactory.eINSTANCE.createGenClass());
 		assertTrue(genDiagram.generateInitDiagramAction());
 	}
 	
 	public void testGenDiagram_hasLinkCreationConstraints() {
 		GenDiagram genDiagram = GMFGenFactory.eINSTANCE.createGenDiagram();
 		GenLink link = GMFGenFactory.eINSTANCE.createGenLink();
 		genDiagram.getLinks().add(link);
 		assertFalse(genDiagram.hasLinkCreationConstraints());
 		
 		link = GMFGenFactory.eINSTANCE.createGenLink();
 		genDiagram.getLinks().add(link);
 		link.setCreationConstraints(GMFGenFactory.eINSTANCE.createGenLinkConstraints());
 		assertTrue(genDiagram.hasLinkCreationConstraints());
 	}
 	
 	public void testGenDiagram_getAllNodes() {
 		GenDiagram genDiagram = GMFGenFactory.eINSTANCE.createGenDiagram();
 		GenTopLevelNode topLevelNode = GMFGenFactory.eINSTANCE.createGenTopLevelNode();
 		genDiagram.getTopLevelNodes().add(topLevelNode);
 		GenChildNode childNode = GMFGenFactory.eINSTANCE.createGenChildNode();
 		genDiagram.getChildNodes().add(childNode);
 		GenCompartment genCompartment = GMFGenFactory.eINSTANCE.createGenCompartment();
 		genDiagram.getCompartments().add(genCompartment);
 		
 		Collection<GenNode> nodes = genDiagram.getAllNodes();
 		assertTrue(nodes.contains(topLevelNode));
 		assertTrue(nodes.contains(childNode));
 		assertFalse(nodes.contains(genCompartment));
 		assertFalse(nodes.contains(genDiagram));
 	}
 	
 	public void testGenDiagram_getAllChildContainers() {
 		GenDiagram genDiagram = GMFGenFactory.eINSTANCE.createGenDiagram();
 		GenTopLevelNode topLevelNode = GMFGenFactory.eINSTANCE.createGenTopLevelNode();
 		genDiagram.getTopLevelNodes().add(topLevelNode);
 		GenChildNode childNode = GMFGenFactory.eINSTANCE.createGenChildNode();
 		genDiagram.getChildNodes().add(childNode);
 		GenCompartment genCompartment = GMFGenFactory.eINSTANCE.createGenCompartment();
 		genDiagram.getCompartments().add(genCompartment);
 		
 		Collection<GenChildContainer> nodes = genDiagram.getAllChildContainers();
 		assertTrue(nodes.contains(topLevelNode));
 		assertTrue(nodes.contains(childNode));
 		assertTrue(nodes.contains(genCompartment));
 		assertFalse(nodes.contains(genDiagram));
 	}
 	
 	public void testGenDiagram_getAllContainers() {
 		GenDiagram genDiagram = GMFGenFactory.eINSTANCE.createGenDiagram();
 		GenTopLevelNode topLevelNode = GMFGenFactory.eINSTANCE.createGenTopLevelNode();
 		genDiagram.getTopLevelNodes().add(topLevelNode);
 		GenChildNode childNode = GMFGenFactory.eINSTANCE.createGenChildNode();
 		genDiagram.getChildNodes().add(childNode);
 		GenCompartment genCompartment = GMFGenFactory.eINSTANCE.createGenCompartment();
 		genDiagram.getCompartments().add(genCompartment);
 		
 		Collection<GenContainerBase> nodes = genDiagram.getAllContainers();
 		assertTrue(nodes.contains(topLevelNode));
 		assertTrue(nodes.contains(childNode));
 		assertTrue(nodes.contains(genCompartment));
 		assertTrue(nodes.contains(genDiagram));
 	}
 	
 	public void testGenDiagram_getCreationWizardIconPathX() {
 		GenDiagram genDiagram = GMFGenFactory.eINSTANCE.createGenDiagram();
 		genDiagram.setCreationWizardIconPath(null);
 		assertNotNull(genDiagram.getCreationWizardIconPathX());
 		assertTrue(genDiagram.getCreationWizardIconPathX().length() > 0);
 
 		genDiagram.setCreationWizardIconPath("");
 		assertNotNull(genDiagram.getCreationWizardIconPathX());
 		assertTrue(genDiagram.getCreationWizardIconPathX().length() > 0);
 		
 		genDiagram.setCreationWizardIconPath("myPath");
 		assertNotNull(genDiagram.getCreationWizardIconPathX());
 		assertEquals("myPath", genDiagram.getCreationWizardIconPathX());
 
 		genDiagram.setCreationWizardIconPath("{reuseEMFIcon}");
 		assertNotNull(genDiagram.getCreationWizardIconPathX());
 		assertFalse("{reuseEMFIcon}".equals(genDiagram.getCreationWizardIconPathX()));
 		assertTrue(genDiagram.getCreationWizardIconPathX().length() > 0);
 	}
 	
 	public void testGenEditorView_ID() {
 		GenEditorView editorView = GMFGenFactory.eINSTANCE.createGenEditorView();
 		editorView.setPackageName("myPackage");
 		editorView.setClassName("MyClass");
 		checkStringPropertyWithDefault(editorView, GMFGenPackage.eINSTANCE.getGenEditorView_ID());
 	}
 	
 	private void checkStringPropertyWithDefault(EObject propertyHolder, EAttribute property) {
 		propertyHolder.eSet(property, null);
 		assertNotNull(propertyHolder.eGet(property));
 		assertTrue(((String) propertyHolder.eGet(property)).trim().length() > 0);
 		
 		propertyHolder.eSet(property, "");
 		assertNotNull(propertyHolder.eGet(property));
 		assertTrue(((String) propertyHolder.eGet(property)).trim().length() > 0);
 
 		propertyHolder.eSet(property, " ");
 		assertNotNull(propertyHolder.eGet(property));
 		assertTrue(((String) propertyHolder.eGet(property)).trim().length() > 0);
 
 		String value = "Value_" + String.valueOf(RANDOM_GENERATOR.nextInt());
 		propertyHolder.eSet(property, value);
 		assertNotNull(propertyHolder.eGet(property));
 		assertEquals(value, propertyHolder.eGet(property));
 	}
 	
 	public void testGenEditorView_getIconPathX() {
 		GenEditorView editorView = GMFGenFactory.eINSTANCE.createGenEditorView();
 		GenEditorGenerator genEditorGenerator = GMFGenFactory.eINSTANCE.createGenEditorGenerator();
 		genEditorGenerator.setEditor(editorView);
 		editorView.setIconPath(null);
 		assertNotNull(editorView.getIconPathX());
 		assertTrue(editorView.getIconPathX().length() > 0);
 
 		editorView.setIconPath("");
 		assertNotNull(editorView.getIconPathX());
 		assertTrue(editorView.getIconPathX().length() > 0);
 		
 		editorView.setIconPath("myPath");
 		assertNotNull(editorView.getIconPathX());
 		assertEquals("myPath", editorView.getIconPathX());
 
 		editorView.setIconPath("{reuseEMFIcon}");
 		assertNotNull(editorView.getIconPathX());
 		assertFalse("{reuseEMFIcon}".equals(editorView.getIconPathX()));
 		assertTrue(editorView.getIconPathX().length() > 0);
 	}
 	
 	public void testGenCommonBase_ClassNameSuffux() {
 		assertClassNameSuffix(GMFGenFactory.eINSTANCE.createGenDiagram());
 		assertClassNameSuffix(GMFGenFactory.eINSTANCE.createGenChildNode());
 		assertClassNameSuffix(GMFGenFactory.eINSTANCE.createGenChildLabelNode());
 		assertClassNameSuffix(GMFGenFactory.eINSTANCE.createGenTopLevelNode());
 		assertClassNameSuffix(GMFGenFactory.eINSTANCE.createGenCompartment());
 		assertClassNameSuffix(GMFGenFactory.eINSTANCE.createGenLinkLabel());
 		assertClassNameSuffix(GMFGenFactory.eINSTANCE.createGenNodeLabel());
 		assertClassNameSuffix(GMFGenFactory.eINSTANCE.createGenExternalNodeLabel());
 		assertClassNameSuffix(GMFGenFactory.eINSTANCE.createGenLink());
 	}
 	
 	private void assertClassNameSuffix(GenCommonBase commonBase) {
 		assertNotNull(commonBase.getClassNameSuffux());
 		assertTrue(commonBase.getClassNameSuffux().length() == 0);
 	}
 
 	public void testGenCommonBase_ClassNamePrefix() {
 		GenClass domainElement = GenModelFactory.eINSTANCE.createGenClass();
 		EClass eClass = EcoreFactory.eINSTANCE.createEClass();
 		eClass.setName("DomainModelClassName" + INVALID_JAVA_CHARS);
 		domainElement.setEcoreClass(eClass);
 		TypeModelFacet typeModelFacet = GMFGenFactory.eINSTANCE.createTypeModelFacet();
 		typeModelFacet.setMetaClass(domainElement);
 		TypeLinkModelFacet typeLinkModelFacet = GMFGenFactory.eINSTANCE.createTypeLinkModelFacet();
 		typeLinkModelFacet.setMetaClass(domainElement);
 		EReference reference = EcoreFactory.eINSTANCE.createEReference();
 		reference.setName("Reference" + INVALID_JAVA_CHARS);
 		eClass.getEStructuralFeatures().add(reference);
 		GenFeature genFeature = GenModelFactory.eINSTANCE.createGenFeature();
 		genFeature.setEcoreFeature(reference);
 		domainElement.getGenFeatures().add(genFeature);
 		FeatureLinkModelFacet featureLinkModelFacet = GMFGenFactory.eINSTANCE.createFeatureLinkModelFacet();
 		featureLinkModelFacet.setMetaFeature(genFeature);
 		
 		GenDiagram diagram = GMFGenFactory.eINSTANCE.createGenDiagram();
 		assertClassNamePrefix(diagram);
 		
 		diagram.setDomainDiagramElement(domainElement);
 		assertClassNamePrefix(diagram);
 		
 		GenChildNode genChildNode = GMFGenFactory.eINSTANCE.createGenChildNode();
 		assertClassNamePrefix(genChildNode);
 		
 		genChildNode.setModelFacet(typeModelFacet);
 		assertClassNamePrefix(genChildNode);
 		
 		GenChildLabelNode genChildLabelNode = GMFGenFactory.eINSTANCE.createGenChildLabelNode();
 		assertClassNamePrefix(genChildLabelNode);
 		
 		genChildLabelNode.setModelFacet(typeModelFacet);
 		assertClassNamePrefix(genChildLabelNode);
 		
 		GenTopLevelNode genTopLevelNode = GMFGenFactory.eINSTANCE.createGenTopLevelNode();
 		assertClassNamePrefix(genTopLevelNode);
 		
 		genTopLevelNode.setModelFacet(typeModelFacet);
 		assertClassNamePrefix(genTopLevelNode);
 
 		GenCompartment compartment = GMFGenFactory.eINSTANCE.createGenCompartment();
 		genTopLevelNode.getCompartments().add(compartment);
 		assertClassNamePrefix(compartment);
 		
 		compartment.setTitle("Title:" + INVALID_JAVA_CHARS);
 		assertClassNamePrefix(compartment);
 		
 		GenLink genLink = GMFGenFactory.eINSTANCE.createGenLink();
 		assertClassNamePrefix(genLink);
 		
 		genLink.setModelFacet(typeLinkModelFacet);
 		assertClassNamePrefix(genLink);
 		
 		genLink.setModelFacet(featureLinkModelFacet);
 		assertClassNamePrefix(genLink);
 		
 		GenLinkLabel genLinkLabel = GMFGenFactory.eINSTANCE.createGenLinkLabel();
 		genLink.getLabels().add(genLinkLabel);
 		assertClassNamePrefix(genLinkLabel);
 		
 		genLinkLabel.getMetaFeatures().add(genFeature);
 		assertClassNamePrefix(genLinkLabel);
 		
 		GenNodeLabel genNodeLabel = GMFGenFactory.eINSTANCE.createGenNodeLabel();
 		genTopLevelNode.getLabels().add(genNodeLabel);
 		assertClassNamePrefix(genNodeLabel);
 
 		genNodeLabel.getMetaFeatures().add(genFeature);
 		assertClassNamePrefix(genNodeLabel);
 
 		GenExternalNodeLabel genExternalNodeLabel = GMFGenFactory.eINSTANCE.createGenExternalNodeLabel();
 		genTopLevelNode.getLabels().add(genExternalNodeLabel);
 		assertClassNamePrefix(genExternalNodeLabel);
 
 		genExternalNodeLabel.getMetaFeatures().add(genFeature);
 		assertClassNamePrefix(genExternalNodeLabel);
 	}
 	
 	private void assertClassNamePrefix(GenCommonBase commonBase) {
 		assertNotNull(commonBase.getClassNamePrefix());
 		assertTrue(commonBase.getClassNamePrefix().length() > 0);
 		IStatus s = JavaConventions.validateJavaTypeName(commonBase.getClassNamePrefix(), JavaCore.VERSION_1_4, JavaCore.VERSION_1_4);
 		assertTrue("Default prefix: " + s.getMessage(), s.getSeverity() != IStatus.ERROR);		
 	}
 	
 	public void testGenCommonBase_getLayoutType() {
 		checkLayoutType(GMFGenFactory.eINSTANCE.createGenDiagram());
 		checkLayoutType(GMFGenFactory.eINSTANCE.createGenChildNode());
 		checkLayoutType(GMFGenFactory.eINSTANCE.createGenChildLabelNode());
 		checkLayoutType(GMFGenFactory.eINSTANCE.createGenTopLevelNode());
 		checkLayoutType(GMFGenFactory.eINSTANCE.createGenCompartment());
 		checkLayoutType(GMFGenFactory.eINSTANCE.createGenLinkLabel());
 		checkLayoutType(GMFGenFactory.eINSTANCE.createGenNodeLabel());
 		checkLayoutType(GMFGenFactory.eINSTANCE.createGenExternalNodeLabel());
 		checkLayoutType(GMFGenFactory.eINSTANCE.createGenLink());
 	}
 	
 	private void checkLayoutType(GenCommonBase commonBase) {
 		assertEquals(ViewmapLayoutType.UNKNOWN_LITERAL, commonBase.getLayoutType());
 		
 		Viewmap viewmap = GMFGenFactory.eINSTANCE.createFigureViewmap();
 		ViewmapLayoutType layoutType = ViewmapLayoutType.get(RANDOM_GENERATOR.nextInt(3));
 		viewmap.setLayoutType(layoutType);
 		commonBase.setViewmap(viewmap);
 		assertEquals(layoutType, commonBase.getLayoutType());
 	}
 	
 	public void testGenCommonBase_getDiagram() {
 		GenDiagram diagram = GMFGenFactory.eINSTANCE.createGenDiagram();
 		assertEquals(diagram, diagram.getDiagram());
 		
 		assertEquals(GMFGenPackage.eINSTANCE.getGenChildNode().getEStructuralFeature("diagram"), GMFGenPackage.eINSTANCE.getGenDiagram_ChildNodes().getEOpposite());
 		assertEquals(GMFGenPackage.eINSTANCE.getGenChildLabelNode().getEStructuralFeature("diagram"), GMFGenPackage.eINSTANCE.getGenDiagram_ChildNodes().getEOpposite());
 		assertEquals(GMFGenPackage.eINSTANCE.getGenTopLevelNode().getEStructuralFeature("diagram"), GMFGenPackage.eINSTANCE.getGenDiagram_TopLevelNodes().getEOpposite());
 		assertEquals(GMFGenPackage.eINSTANCE.getGenCompartment().getEStructuralFeature("diagram"), GMFGenPackage.eINSTANCE.getGenDiagram_Compartments().getEOpposite());
 		
 		GenLinkLabel genLinkLabel = GMFGenFactory.eINSTANCE.createGenLinkLabel();
 		GenLink genLink = GMFGenFactory.eINSTANCE.createGenLink();
 		diagram.getLinks().add(genLink);
 		genLink.getLabels().add(genLinkLabel);
 		assertEquals(diagram, genLinkLabel.getDiagram());
 		
 		GenNodeLabel genNodeLabel = GMFGenFactory.eINSTANCE.createGenNodeLabel();
 		GenTopLevelNode genNode = GMFGenFactory.eINSTANCE.createGenTopLevelNode();
 		diagram.getTopLevelNodes().add(genNode);
 		genNode.getLabels().add(genNodeLabel);
 		assertEquals(diagram, genNodeLabel.getDiagram());
 
 		GenExternalNodeLabel genExternalNodeLabel = GMFGenFactory.eINSTANCE.createGenExternalNodeLabel();
 		genNode.getLabels().add(genExternalNodeLabel);
 		assertEquals(diagram, genExternalNodeLabel.getDiagram());
 		
 		assertEquals(GMFGenPackage.eINSTANCE.getGenLink().getEStructuralFeature("diagram"), GMFGenPackage.eINSTANCE.getGenDiagram_Links().getEOpposite());
 	}
 	
 	public void testGenContainerBase_getContainedNodes() {
 		GenDiagram diagram = GMFGenFactory.eINSTANCE.createGenDiagram();
 		assertTrue(diagram.getContainedNodes().isEmpty());
 		GenTopLevelNode genTopLevelNode = GMFGenFactory.eINSTANCE.createGenTopLevelNode();
 		diagram.getTopLevelNodes().add(genTopLevelNode);
 		GenChildNode genChildNode = GMFGenFactory.eINSTANCE.createGenChildNode();
 		diagram.getChildNodes().add(genChildNode);
 		GenChildLabelNode genChildLabelNode = GMFGenFactory.eINSTANCE.createGenChildLabelNode();
 		diagram.getChildNodes().add(genChildLabelNode);
 		GenCompartment genCompartment = GMFGenFactory.eINSTANCE.createGenCompartment();
 		diagram.getCompartments().add(genCompartment);
 		GenChildNode commonChild = GMFGenFactory.eINSTANCE.createGenChildNode();
 		diagram.getChildNodes().add(commonChild);
 		
 		assertTrue(diagram.getContainedNodes().size() == 1);
 		assertEquals(genTopLevelNode, diagram.getContainedNodes().get(0));
 		
 		assertTrue(genTopLevelNode.getContainedNodes().size() == 0);
 		assertTrue(genChildNode.getContainedNodes().size() == 0);
 		assertTrue(genChildLabelNode.getContainedNodes().size() == 0);
 		assertTrue(genCompartment.getContainedNodes().size() == 0);
 
 		genTopLevelNode.getChildNodes().add(commonChild);
 		assertTrue(genTopLevelNode.getContainedNodes().size() == 1);
 		assertEquals(commonChild, genTopLevelNode.getContainedNodes().get(0));
 		
 		genChildNode.getChildNodes().add(commonChild);
 		assertTrue(genChildNode.getContainedNodes().size() == 1);
 		assertEquals(commonChild, genChildNode.getContainedNodes().get(0));
 		
 		genChildLabelNode.getChildNodes().add(commonChild);
 		assertTrue(genChildLabelNode.getContainedNodes().size() == 1);
 		assertEquals(commonChild, genChildLabelNode.getContainedNodes().get(0));
 		
 		genCompartment.getChildNodes().add(commonChild);
 		assertTrue(genCompartment.getContainedNodes().size() == 1);
 		assertEquals(commonChild, genCompartment.getContainedNodes().get(0));
 	}
 	
 	public void testGenNode_getDomainMetaClass() {
 		TypeModelFacet modelFacet = GMFGenFactory.eINSTANCE.createTypeModelFacet();
 		GenClass genClass = GenModelFactory.eINSTANCE.createGenClass();
 		modelFacet.setMetaClass(genClass);
 		
 		GenTopLevelNode topLevelNode = GMFGenFactory.eINSTANCE.createGenTopLevelNode();
 		assertNull(topLevelNode.getDomainMetaClass());
 		
 		topLevelNode.setModelFacet(modelFacet);
 		assertEquals(genClass, topLevelNode.getDomainMetaClass());
 		
 		GenChildNode childNode = GMFGenFactory.eINSTANCE.createGenChildNode();
 		assertNull(childNode.getDomainMetaClass());
 
 		childNode.setModelFacet(modelFacet);
 		assertEquals(genClass, childNode.getDomainMetaClass());
 		
 		GenChildLabelNode childLabelNode = GMFGenFactory.eINSTANCE.createGenChildLabelNode();
 		assertNull(childLabelNode.getDomainMetaClass());
 		
 		childLabelNode.setModelFacet(modelFacet);
 		assertEquals(genClass, childLabelNode.getDomainMetaClass());
 	}
 	
 	public void testGenChildLabelNode_getLabelMetaFeatures() {
 		GenChildLabelNode childLabelNode = GMFGenFactory.eINSTANCE.createGenChildLabelNode();
 		assertTrue(childLabelNode.getLabelMetaFeatures().size() == 0);
 		
 		GenFeature genFeature = GenModelFactory.eINSTANCE.createGenFeature();
 		
 		FeatureLabelModelFacet compositeModelFacet = GMFGenFactory.eINSTANCE.createFeatureLabelModelFacet();
 		childLabelNode.setLabelModelFacet(compositeModelFacet);
 		assertTrue(childLabelNode.getLabelMetaFeatures().size() == 0);
 		
 		compositeModelFacet.getMetaFeatures().add(genFeature);
 		assertTrue(childLabelNode.getLabelMetaFeatures().size() == 1);
 		assertEquals(genFeature, childLabelNode.getLabelMetaFeatures().get(0));
 
 		GenFeature nextGenFeature = GenModelFactory.eINSTANCE.createGenFeature();
 		compositeModelFacet.getMetaFeatures().add(nextGenFeature);
 		assertTrue(childLabelNode.getLabelMetaFeatures().size() == 2);
 		assertTrue(childLabelNode.getLabelMetaFeatures().contains(genFeature));
 		assertTrue(childLabelNode.getLabelMetaFeatures().contains(nextGenFeature));
 	}
 	
 	public void testGenLink_getSources_getTargets() {
 		GenDiagram genDiagram = GMFGenFactory.eINSTANCE.createGenDiagram();
 		GenLink genLink = GMFGenFactory.eINSTANCE.createGenLink();
 		genDiagram.getLinks().add(genLink);
 		GenTopLevelNode topLevelNode = GMFGenFactory.eINSTANCE.createGenTopLevelNode();
 		genDiagram.getTopLevelNodes().add(topLevelNode);
 		TypeModelFacet typeModelFacet = GMFGenFactory.eINSTANCE.createTypeModelFacet();
 		topLevelNode.setModelFacet(typeModelFacet);
 		GenClass genClass1 = GenModelFactory.eINSTANCE.createGenClass();
 		typeModelFacet.setMetaClass(genClass1);
 		GenChildNode childNode = GMFGenFactory.eINSTANCE.createGenChildNode();
 		genDiagram.getChildNodes().add(childNode);
 		typeModelFacet = GMFGenFactory.eINSTANCE.createTypeModelFacet();
 		childNode.setModelFacet(typeModelFacet);
 		GenClass genClass2 = GenModelFactory.eINSTANCE.createGenClass();
 		typeModelFacet.setMetaClass(genClass2);
 		
 		assertTrue(genLink.getAssistantSources().size() == 0);
 		assertTrue(genLink.getAssistantTargets().size() == 0);
 		
 		genLink.setModelFacet(new CustomLinkModelFacet(new GenClass[] {genClass1, genClass2, GenModelFactory.eINSTANCE.createGenClass()}));
 		assertTrue(genLink.getAssistantSources().size() == 2);
 		assertTrue(genLink.getAssistantSources().contains(topLevelNode));
 		assertTrue(genLink.getAssistantSources().contains(childNode));
 
 		assertTrue(genLink.getAssistantTargets().size() == 2);
 		assertTrue(genLink.getAssistantTargets().contains(topLevelNode));
 		assertTrue(genLink.getAssistantTargets().contains(childNode));
 	}
 	
 	public void testGenLabel_getMetaFeatures() {
 		checkMetaFeatures(GMFGenFactory.eINSTANCE.createGenLinkLabel());
 		checkMetaFeatures(GMFGenFactory.eINSTANCE.createGenNodeLabel());
 		checkMetaFeatures(GMFGenFactory.eINSTANCE.createGenExternalNodeLabel());
 	}
 	
 	private void checkMetaFeatures(GenLabel genLabel) {
 		assertTrue(genLabel.getMetaFeatures().size() == 0);
 		
 		GenFeature genFeature = GenModelFactory.eINSTANCE.createGenFeature();
 		
 		FeatureLabelModelFacet compositeModelFacet = GMFGenFactory.eINSTANCE.createFeatureLabelModelFacet();
 		genLabel.setModelFacet(compositeModelFacet);
 		assertTrue(genLabel.getMetaFeatures().size() == 0);
 		
 		compositeModelFacet.getMetaFeatures().add(genFeature);
 		assertTrue(genLabel.getMetaFeatures().size() == 1);
 		assertEquals(genFeature, genLabel.getMetaFeatures().get(0));
 
 		GenFeature nextGenFeature = GenModelFactory.eINSTANCE.createGenFeature();
 		compositeModelFacet.getMetaFeatures().add(nextGenFeature);
 		assertTrue(genLabel.getMetaFeatures().size() == 2);
 		assertTrue(genLabel.getMetaFeatures().contains(genFeature));
 		assertTrue(genLabel.getMetaFeatures().contains(nextGenFeature));
 	}
 	
 	public void testMetamodelType_getMetaClass() {
 		MetamodelType metamodelType = GMFGenFactory.eINSTANCE.createMetamodelType();
 		assertNull(metamodelType.getMetaClass());
 		
 		GenNode genNode = GMFGenFactory.eINSTANCE.createGenTopLevelNode();
 		TypeModelFacet typeModelFacet = GMFGenFactory.eINSTANCE.createTypeModelFacet();
 		genNode.setModelFacet(typeModelFacet);
 		GenClass genClass = GenModelFactory.eINSTANCE.createGenClass();
 		typeModelFacet.setMetaClass(genClass);
 		metamodelType.setDiagramElement(genNode);
 		assertEquals(genClass, metamodelType.getMetaClass());
 		
 		GenLink genLink = GMFGenFactory.eINSTANCE.createGenLink();
 		TypeLinkModelFacet typeLinkModelFacet = GMFGenFactory.eINSTANCE.createTypeLinkModelFacet();
 		genLink.setModelFacet(typeLinkModelFacet);
 		typeLinkModelFacet.setMetaClass(genClass);
 		metamodelType.setDiagramElement(genLink);
 		assertEquals(genClass, metamodelType.getMetaClass());
 		
 		GenDiagram genDiagram = GMFGenFactory.eINSTANCE.createGenDiagram();
 		genDiagram.setDomainDiagramElement(genClass);
 		metamodelType.setDiagramElement(genDiagram);
 		assertEquals(genClass, metamodelType.getMetaClass());
 	}
 	
 	public void testLinkModelFacet_getSourceType_getTargetType() {
 		GenModel genModel = GenModelFactory.eINSTANCE.createGenModel();
 		GenPackage genPackage = GenModelFactory.eINSTANCE.createGenPackage();
 		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
 		genPackage.setEcorePackage(ePackage);
 		genModel.getGenPackages().add(genPackage);
 		
 		TypeLinkModelFacet typeLinkModelFacet = GMFGenFactory.eINSTANCE.createTypeLinkModelFacet();
 		assertTrue(typeLinkModelFacet.getSourceType() == null);
 		
 		GenClass genClass1 = GenModelFactory.eINSTANCE.createGenClass();
 		EClass eClass1 = EcoreFactory.eINSTANCE.createEClass();
 		genClass1.setEcoreClass(eClass1);
 		genPackage.getGenClasses().add(genClass1);
 		GenFeature genFeature1 = GenModelFactory.eINSTANCE.createGenFeature();
 		genClass1.getGenFeatures().add(genFeature1);
 		typeLinkModelFacet.setContainmentMetaFeature(genFeature1);
 		assertEquals(genClass1, typeLinkModelFacet.getSourceType());
 		
 		GenFeature genFeature2 = GenModelFactory.eINSTANCE.createGenFeature();
 		genClass1.getGenFeatures().add(genFeature2);
 		GenClass genClass2 = GenModelFactory.eINSTANCE.createGenClass();
 		EClass eClass2 = EcoreFactory.eINSTANCE.createEClass();
 		eClass2.setName("ClassName");
 		genClass2.setEcoreClass(eClass2);
 		genPackage.getGenClasses().add(genClass2);
 		ePackage.getEClassifiers().add(eClass2);
 		EReference eReference = EcoreFactory.eINSTANCE.createEReference();
 		eReference.setEType(eClass2);
 		genFeature2.setEcoreFeature(eReference);
 		typeLinkModelFacet.setSourceMetaFeature(genFeature2);
 		assertEquals(genClass2, typeLinkModelFacet.getSourceType());
 		
 		assertTrue(typeLinkModelFacet.getTargetType() == null);
 		
 		typeLinkModelFacet.setTargetMetaFeature(genFeature2);
 		assertEquals(genClass2, typeLinkModelFacet.getTargetType());
 		
 		FeatureLinkModelFacet featureLinkModelFacet = GMFGenFactory.eINSTANCE.createFeatureLinkModelFacet();
 		assertTrue(featureLinkModelFacet.getSourceType() == null);
 		assertTrue(featureLinkModelFacet.getTargetType() == null);
 		
 		featureLinkModelFacet.setMetaFeature(genFeature2);
 		assertEquals(genClass1, featureLinkModelFacet.getSourceType());
 		
 		assertEquals(genClass2, featureLinkModelFacet.getTargetType());
 	}
 	
 	public void testTypeModelFacet_isPhantomElement() {
 		GenTopLevelNode topLevelNode = GMFGenFactory.eINSTANCE.createGenTopLevelNode();
 		GenChildNode childNode = GMFGenFactory.eINSTANCE.createGenChildNode();
 		TypeModelFacet typeModelFacet = GMFGenFactory.eINSTANCE.createTypeModelFacet();
 		childNode.setModelFacet(typeModelFacet);
 		assertFalse(typeModelFacet.isPhantomElement());
 		
 		topLevelNode.setModelFacet(typeModelFacet);
 		assertTrue(typeModelFacet.isPhantomElement());
 		typeModelFacet.setContainmentMetaFeature(GenModelFactory.eINSTANCE.createGenFeature());
 		assertFalse(typeModelFacet.isPhantomElement());
 		
 		GenLink genLink = GMFGenFactory.eINSTANCE.createGenLink();
 		TypeLinkModelFacet typeLinkModelFacet = GMFGenFactory.eINSTANCE.createTypeLinkModelFacet();
 		genLink.setModelFacet(typeLinkModelFacet);
 		assertFalse(typeLinkModelFacet.isPhantomElement());
 		typeLinkModelFacet.setContainmentMetaFeature(GenModelFactory.eINSTANCE.createGenFeature());
 		assertFalse(typeModelFacet.isPhantomElement());
 	}
 	
 	public void testPackageNames() {
 		GenDiagram genDiagram = myGenModel;
 		Set<String> state = new HashSet<String>();
 
 		// package names check
 		checkPackageName(state, "PackageNames:editCommands", genDiagram.getEditCommandsPackageName());
 		checkPackageName(state, "PackageNames:editHelpers", genDiagram.getEditHelpersPackageName());
 		checkPackageName(state, "PackageNames:editParts", genDiagram.getEditPartsPackageName());
 		checkPackageName(state, "PackageNames:editPolicies", genDiagram.getEditPoliciesPackageName());
 		checkPackageName(state, "GenEditorView:packageName", genDiagram.getEditorGen().getEditor().getPackageName());
 		checkPackageName(state, "PackageNames:providers", genDiagram.getProvidersPackageName());
 		checkPackageName(state, "PackageNames:parsers", genDiagram.getParsersPackageName());
 		checkPackageName(state, "PackageNames:preferences", genDiagram.getPreferencesPackageName());
 		checkPackageName(state, "PackageNames:notationViewFactories", genDiagram.getNotationViewFactoriesPackageName());
 		GenApplication application = genDiagram.getEditorGen().getApplication();
 		if (application != null) {
 			checkPackageName(state, "GenApplication:application", application.getPackageName());
 		} else {
 			state.add("GenApplication:application");
 		}
 		if (genDiagram.getEditorGen().getExpressionProviders() != null) {
 			GenExpressionProviderContainer providers = genDiagram.getEditorGen().getExpressionProviders();
 			checkPackageName(state, "GenExpressionProviderContainer:expressions", providers.getExpressionsPackageName());
 		} else {
 			state.add("GenExpressionProviderContainer:expressions");
 		}
 
 		// coverage check
 		for (Object next : GMFGenPackage.eINSTANCE.getEClassifiers()) {
 			if (next instanceof EClass) {
 				checkPackageNamesCoverage(state, (EClass) next);
 			}
 		}
 	}
 
 	public void testClassNames() {
 		GenDiagram genDiagram = myGenModel;
 		Set<String> state = new HashSet<String>();
 		GenEditorView genEditor = myGenModel.getEditorGen().getEditor();
 
 		// class names check
 		checkClassName(state, "EditPartCandies:ReorientConnectionViewCommand", genDiagram.getReorientConnectionViewCommandClassName(), genDiagram.getReorientConnectionViewCommandQualifiedClassName());
 		checkClassName(state, "EditPartCandies:BaseEditHelper", genDiagram.getBaseEditHelperClassName(), genDiagram.getBaseEditHelperQualifiedClassName());
 		checkClassName(state, "EditPartCandies:EditPartFactory", genDiagram.getEditPartFactoryClassName(), genDiagram.getEditPartFactoryQualifiedClassName());
 		checkClassName(state, "EditPartCandies:BaseExternalNodeLabelEditPart", genDiagram.getBaseExternalNodeLabelEditPartClassName(), genDiagram.getBaseExternalNodeLabelEditPartQualifiedClassName());
 		checkClassName(state, "EditPartCandies:BaseItemSemanticEditPolicy", genDiagram.getBaseItemSemanticEditPolicyClassName(), genDiagram.getBaseItemSemanticEditPolicyQualifiedClassName());
 		checkClassName(state, "EditPartCandies:BaseGraphicalNodeEditPolicy", genDiagram.getBaseGraphicalNodeEditPolicyClassName(), genDiagram.getBaseGraphicalNodeEditPolicyQualifiedClassName());
 		checkClassName(state, "GenContainerBase:CanonicalEditPolicy", genDiagram.getCanonicalEditPolicyClassName(), genDiagram.getCanonicalEditPolicyQualifiedClassName());
 		checkClassName(state, "EditPartCandies:TextSelectionEditPolicy", genDiagram.getTextSelectionEditPolicyClassName(), genDiagram.getTextSelectionEditPolicyQualifiedClassName());
 		checkClassName(state, "EditPartCandies:TextNonResizableEditPolicy", genDiagram.getTextNonResizableEditPolicyClassName(), genDiagram.getTextNonResizableEditPolicyQualifiedClassName());
 		checkClassName(state, "ProviderClassNames:ElementTypes", genDiagram.getElementTypesClassName(), genDiagram.getElementTypesQualifiedClassName());
 		checkClassName(state, "ProviderClassNames:NotationViewProvider", genDiagram.getNotationViewProviderClassName(), genDiagram.getNotationViewProviderQualifiedClassName());
 		checkClassName(state, "ProviderClassNames:EditPartProvider", genDiagram.getEditPartProviderClassName(), genDiagram.getEditPartProviderQualifiedClassName());
 		checkClassName(state, "ProviderClassNames:ModelingAssistantProvider", genDiagram.getModelingAssistantProviderClassName(), genDiagram.getModelingAssistantProviderQualifiedClassName());
 		checkClassName(state, "ProviderClassNames:IconProvider", genDiagram.getIconProviderClassName(), genDiagram.getIconProviderQualifiedClassName());
 		checkClassName(state, "ProviderClassNames:ParserProvider", genDiagram.getParserProviderClassName(), genDiagram.getParserProviderQualifiedClassName());
 		checkClassName(state, "ProviderClassNames:ContributionItemProvider", genDiagram.getContributionItemProviderClassName(), genDiagram.getContributionItemProviderQualifiedClassName());
 		checkClassName(state, "GenEditorView:ActionBarContributor", genEditor.getActionBarContributorClassName(), genEditor.getActionBarContributorQualifiedClassName());
 		checkClassName(state, "EditorCandies:CreationWizard", genDiagram.getCreationWizardClassName(), genDiagram.getCreationWizardQualifiedClassName());
 		checkClassName(state, "EditorCandies:CreationWizardPage", genDiagram.getCreationWizardPageClassName(), genDiagram.getCreationWizardPageQualifiedClassName());
 		checkClassName(state, "EditorCandies:DiagramEditorUtil", genDiagram.getDiagramEditorUtilClassName(), genDiagram.getDiagramEditorUtilQualifiedClassName());
 		checkClassName(state, "EditorCandies:DocumentProvider", genDiagram.getDocumentProviderClassName(), genDiagram.getDocumentProviderQualifiedClassName());
 		checkClassName(state, "GenEditorView:className", genEditor.getClassName(), genEditor.getQualifiedClassName());
 		checkClassName(state, "EditorCandies:InitDiagramFileAction", genDiagram.getInitDiagramFileActionClassName(), genDiagram.getInitDiagramFileActionQualifiedClassName());
 		checkClassName(state, "EditorCandies:NewDiagramFileWizard", genDiagram.getNewDiagramFileWizardClassName(), genDiagram.getNewDiagramFileWizardQualifiedClassName());
 		checkClassName(state, "EditorCandies:DiagramContentInitializer", genDiagram.getDiagramContentInitializerClassName(), genDiagram.getDiagramContentInitializerQualifiedClassName());
 		checkClassName(state, "EditorCandies:MatchingStrategy", genDiagram.getMatchingStrategyClassName(), genDiagram.getMatchingStrategyQualifiedClassName());
 		checkClassName(state, "EditorCandies:VisualIDRegistry", genDiagram.getVisualIDRegistryClassName(), genDiagram.getVisualIDRegistryQualifiedClassName());
 		checkClassName(state, "EditorCandies:LoadResourceAction", genDiagram.getLoadResourceActionClassName(), genDiagram.getLoadResourceActionQualifiedClassName());
 		checkClassName(state, "LinkConstraints:LinkCreationConstraints", genDiagram.getLinkCreationConstraintsClassName(), genDiagram.getLinkCreationConstraintsQualifiedClassName());
 		checkClassName(state, "Shortcuts:CreateShortcutAction", genDiagram.getCreateShortcutActionClassName(), genDiagram.getCreateShortcutActionQualifiedClassName());
 		checkClassName(state, "Shortcuts:ShortcutsDecoratorProvider", genDiagram.getShortcutsDecoratorProviderClassName(), genDiagram.getShortcutsDecoratorProviderQualifiedClassName());
 		checkClassName(state, "Shortcuts:CreateShortcutDecorationsCommand", genDiagram.getCreateShortcutDecorationsCommandClassName(), genDiagram.getCreateShortcutDecorationsCommandQualifiedClassName());
 		checkClassName(state, "Shortcuts:ShortcutPropertyTester", genDiagram.getShortcutPropertyTesterClassName(), genDiagram.getShortcutPropertyTesterQualifiedClassName());
 		checkClassName(state, "EditorCandies:ElementChooser", genDiagram.getElementChooserClassName(), genDiagram.getElementChooserQualifiedClassName());
 		checkClassName(state, "BatchValidation:ValidationProvider", genDiagram.getValidationProviderClassName(), genDiagram.getValidationProviderQualifiedClassName());
		checkClassName(state, "BatchValidation:ValidationDecoratorProvider", genDiagram.getValidationDecoratorProviderClassName(), genDiagram.getValidationDecoratorProviderQualifiedClassName());
 		checkClassName(state, "BatchValidation:MarkerNavigationProvider", genDiagram.getMarkerNavigationProviderClassName(), genDiagram.getMarkerNavigationProviderQualifiedClassName());
 		checkClassName(state, "BatchValidation:MetricProvider", genDiagram.getMetricProviderClassName(), genDiagram.getMetricProviderQualifiedClassName());
 		GenApplication application = genDiagram.getEditorGen().getApplication();
 		if (application != null) {
 			checkClassName(state, "GenApplication:Application", application.getClassName(), application.getQualifiedClassName());
 			checkClassName(state, "GenApplication:WorkbenchAdvisor", application.getWorkbenchAdvisorClassName(), application.getWorkbenchAdvisorQualifiedClassName());
 			checkClassName(state, "GenApplication:WorkbenchWindowAdvisor", application.getWorkbenchWindowAdvisorClassName(), application.getWorkbenchWindowAdvisorQualifiedClassName());
 			checkClassName(state, "GenApplication:ActionBarAdvisor", application.getActionBarAdvisorClassName(), application.getActionBarAdvisorQualifiedClassName());
 			checkClassName(state, "GenApplication:Perspective", application.getPerspectiveClassName(), application.getPerspectiveQualifiedClassName());
 		} else {
 			state.add("GenApplication:Application");
 			state.add("GenApplication:WorkbenchAdvisor");
 			state.add("GenApplication:WorkbenchWindowAdvisor");
 			state.add("GenApplication:ActionBarAdvisor");
 			state.add("GenApplication:Perspective");
 		}
 		if (genDiagram.getEditorGen().getExpressionProviders() != null) {
 			GenExpressionProviderContainer providers = genDiagram.getEditorGen().getExpressionProviders();
 			checkClassName(state, "GenExpressionProviderContainer:AbstractExpression", providers.getAbstractExpressionClassName(), providers.getAbstractExpressionQualifiedClassName());
 		} else {
 			state.add("GenExpressionProviderContainer:AbstractExpression");
 		}
 		Palette palette = genDiagram.getPalette();
 		if (palette != null) {
 			checkClassName(state, "Palette:Factory", palette.getFactoryClassName(), palette.getFactoryQualifiedClassName());
 		} else {
 			state.add("Palette:Factory");
 		}
 		GenNavigator navigator = genDiagram.getEditorGen().getNavigator();
 		if (navigator != null) {
 			checkClassName(state, "GenNavigator:ContentProvider", navigator.getContentProviderClassName(), navigator.getContentProviderQualifiedClassName());
 			checkClassName(state, "GenNavigator:LabelProvider", navigator.getLabelProviderClassName(), navigator.getLabelProviderQualifiedClassName());
 			checkClassName(state, "GenNavigator:Sorter", navigator.getSorterClassName(), navigator.getSorterQualifiedClassName());
 			checkClassName(state, "GenNavigator:ActionProvider", navigator.getActionProviderClassName(), navigator.getActionProviderQualifiedClassName());
 			checkClassName(state, "GenNavigator:LinkHelper", navigator.getLinkHelperClassName(), navigator.getLinkHelperQualifiedClassName());
 			checkClassName(state, "GenNavigator:AbstractNavigatorItem", navigator.getAbstractNavigatorItemClassName(), navigator.getAbstractNavigatorItemQualifiedClassName());
 			checkClassName(state, "GenNavigator:NavigatorGroup", navigator.getNavigatorGroupClassName(), navigator.getNavigatorGroupQualifiedClassName());
 			checkClassName(state, "GenNavigator:NavigatorItem", navigator.getNavigatorItemClassName(), navigator.getNavigatorItemQualifiedClassName());
 			checkClassName(state, "GenNavigator:UriInputTester", navigator.getUriInputTesterClassName(), navigator.getUriInputTesterQualifiedClassName());			
 			checkClassName(state, "GenDomainModelNavigator:DomainContentProvider", navigator.getDomainContentProviderClassName(), navigator.getDomainContentProviderQualifiedClassName());
 			checkClassName(state, "GenDomainModelNavigator:DomainLabelProvider", navigator.getDomainLabelProviderClassName(), navigator.getDomainLabelProviderQualifiedClassName());
 			checkClassName(state, "GenDomainModelNavigator:DomainModelElementTester", navigator.getDomainModelElementTesterClassName(), navigator.getDomainModelElementTesterQualifiedClassName());
 			checkClassName(state, "GenDomainModelNavigator:DomainNavigatorItem", navigator.getDomainNavigatorItemClassName(), navigator.getDomainNavigatorItemQualifiedClassName());
 		} else {
 			state.add("GenNavigator:ContentProvider");
 			state.add("GenNavigator:LabelProvider");
 			state.add("GenNavigator:GroupWrapper");
 		}
 		GenDiagramUpdater updater = genDiagram.getEditorGen().getDiagramUpdater();
 		if (updater != null) {
 			checkClassName(state, "GenDiagramUpdater:DiagramUpdater", updater.getDiagramUpdaterClassName(), updater.getDiagramUpdaterQualifiedClassName());
 			checkClassName(state, "GenDiagramUpdater:NodeDescriptor", updater.getNodeDescriptorClassName(), updater.getNodeDescriptorQualifiedClassName());
 			checkClassName(state, "GenDiagramUpdater:LinkDescriptor", updater.getLinkDescriptorClassName(), updater.getLinkDescriptorQualifiedClassName());
 			checkClassName(state, "GenDiagramUpdater:UpdateCommand", updater.getUpdateCommandClassName(), updater.getUpdateCommandQualifiedClassName());
 		} else {
 			state.add("GenDiagramUpdater:DiagramUpdater");
 			state.add("GenDiagramUpdater:NodeDescriptor");
 			state.add("GenDiagramUpdater:LinkDescriptor");
 			state.add("GenDiagramUpdater:UpdateCommand");
 		}
 		
 		GenPropertySheet propSheet = genDiagram.getEditorGen().getPropertySheet();
 		if (propSheet != null) {
 			checkPackageName(state, "GenPropertySheet:packageName", propSheet.getPackageName());
 			checkClassName(state, "GenPropertySheet:LabelProvider", propSheet.getLabelProviderClassName(), propSheet.getLabelProviderQualifiedClassName());
 		} else {
 			state.add("GenPropertySheet:LabelProvider");
 		}
 		GenPlugin genPlugin = genDiagram.getEditorGen().getPlugin();
 		checkClassName(state, "GenPlugin:Activator", genPlugin.getActivatorClassName(), genPlugin.getActivatorQualifiedClassName());
 		for (GenCommonBaseIterator entities = new GenCommonBaseIterator(genDiagram); entities.hasNext();) {
 			GenCommonBase nextEntity = entities.next();
 			checkClassName(state, "GenCommonBase:EditPart", nextEntity.getEditPartClassName(), nextEntity.getEditPartQualifiedClassName());
 			checkClassName(state, "GenCommonBase:ItemSemanticEditPolicy", nextEntity.getItemSemanticEditPolicyClassName(), nextEntity.getItemSemanticEditPolicyQualifiedClassName());
 			checkClassName(state, "GenCommonBase:NotationViewFactory", nextEntity.getNotationViewFactoryClassName(), nextEntity.getNotationViewFactoryQualifiedClassName());
 			checkEditSupport(state, nextEntity);
 			if (nextEntity instanceof GenChildContainer) {
 				GenChildContainer genContainer = (GenChildContainer) nextEntity;
 				checkClassName(state, "GenContainerBase:CanonicalEditPolicy", genContainer.getCanonicalEditPolicyClassName(), genContainer.getCanonicalEditPolicyQualifiedClassName());
 			}
 			if (nextEntity instanceof GenNode) {
 				GenNode genNode = (GenNode) nextEntity;
 				checkClassName(state, "GenNode:GraphicalNodeEditPolicy", genNode.getGraphicalNodeEditPolicyClassName(), genNode.getGraphicalNodeEditPolicyQualifiedClassName());
 				checkClassName(state, "GenNode:CreateCommand", genNode.getCreateCommandClassName(), genNode.getCreateCommandQualifiedClassName());
 			}
 			if (nextEntity instanceof GenLink) {
 				GenLink genLink = (GenLink) nextEntity;
 				checkClassName(state, "GenLink:CreateCommand", genLink.getCreateCommandClassName(), genLink.getCreateCommandQualifiedClassName());
 				checkClassName(state, "GenLink:ReorientCommand", genLink.getReorientCommandClassName(), genLink.getReorientCommandQualifiedClassName());
 			}
 			for (Behaviour nextB : nextEntity.getBehaviour()) {
 				String epClassName = CodeGenUtil.getSimpleClassName(nextB.getEditPolicyQualifiedClassName()); // just for checkClassName to be happy
 				checkClassName(state, "Behaviour:EditPolicy", epClassName, nextB.getEditPolicyQualifiedClassName());
 			}
 		}
 		GenAuditRoot audits = genDiagram.getEditorGen().getAudits();
 		assertTrue("Need AuditRoot instance with rules to check handcoded methods", audits != null && audits.getRules().size() > 0);
 		Set<String> checkedContexts = new HashSet<String>();
 		for (GenAuditRule nextAudit : audits.getRules()) {
 			if (!checkedContexts.contains(nextAudit.getContextSelectorQualifiedClassName())) {
 				checkClassName(state, "GenAuditRule:ContextSelector", nextAudit.getContextSelectorClassName(), nextAudit.getContextSelectorQualifiedClassName());
 				checkedContexts.add(nextAudit.getContextSelectorQualifiedClassName());
 				checkClassName(state, "GenAuditRule:ContextSelectorLocal", nextAudit.getContextSelectorLocalClassName(), nextAudit.getContextSelectorClassName());
 			}
 			checkClassName(state, "GenAuditRule:ConstraintAdapter", nextAudit.getConstraintAdapterClassName(), nextAudit.getConstraintAdapterQualifiedClassName());
 		}
 
 		// test model may not contain them
 		state.add("GenCommonBase:EditPart");
 		state.add("GenCommonBase:ItemSemanticEditPolicy");
 		state.add("GenCommonBase:NotationViewFactory");
 		state.add("GenContainer:CanonicalEditPolicy");
 		state.add("GenNode:GraphicalNodeEditPolicy");
 		state.add("GenNode:CreateCommand");
 		state.add("TypeLinkModelFacet:CreateCommand");
 		state.add("MetamodelType:EditHelper");
 		state.add("SpecializationType:EditHelperAdvice");
 		state.add("Behaviour:EditPolicy");
 		state.add("OpenDiagramBehaviour:EditPolicy");
 
 		// disable explicitly
 		state.add("ElementType:EditHelper");
 		state.add("ElementType:EditHelper");
 		state.add("FigureViewmap:Figure");
 		state.add("ExternalLabel:TextEditPart");
 		state.add("ExternalLabel:TextNotationViewFactory");
 		state.add("GenCustomPreferencePage:Qualified");
 		
 		// coverage check
 		for (Object next : GMFGenPackage.eINSTANCE.getEClassifiers()) {
 			if (next instanceof EClass) {
 				checkClassNamesCoverage(state, (EClass) next);
 			}
 		}
 	}
 
 	protected void checkEditSupport(Set<String> state, GenCommonBase diagramElement) {
 		ElementType genType = diagramElement.getElementType();
 		if (genType instanceof MetamodelType) {
 			MetamodelType metamodelType = (MetamodelType) genType;
 			checkClassName(state, "MetamodelType:EditHelper", metamodelType.getEditHelperClassName(), metamodelType.getEditHelperQualifiedClassName());
 		} else if (genType instanceof SpecializationType) {
 			SpecializationType specializationType = (SpecializationType) genType;
 			checkClassName(state, "SpecializationType:EditHelperAdvice", specializationType.getEditHelperAdviceClassName(), specializationType.getEditHelperAdviceQualifiedClassName());
 		}
 	}
 
 	protected void checkPackageName(Set<String> state, String id, String packageName) {
 		IStatus s = JavaConventions.validatePackageName(packageName, javaLevel, javaLevel);
 		assertTrue(id + " package name is not valid : " + s.getMessage(), s.getSeverity() != IStatus.ERROR);
 		state.add(packageName); // for unique package name check
 		state.add(id); // for coverage check
 	}
 
 	protected void checkClassName(Set<String> state, String id, String simpleClassName, String qualifiedClassName) {
 		IStatus s = JavaConventions.validateJavaTypeName(simpleClassName, javaLevel, javaLevel);
 		assertTrue(id + " simple class name is not valid : " + s.getMessage(), s.getSeverity() != IStatus.ERROR);
 		s = JavaConventions.validateJavaTypeName(qualifiedClassName, javaLevel, javaLevel);
 		assertTrue(id + " qualified class name is not valid : " + s.getMessage(), s.getSeverity() != IStatus.ERROR);
 		assertTrue(id + " simple class name does not match the qualified one : '" + simpleClassName + "', '" + qualifiedClassName + "'", qualifiedClassName.endsWith('.' + simpleClassName) || qualifiedClassName.endsWith('$' + simpleClassName));
 		assertFalse(qualifiedClassName + " is not unique", state.contains(qualifiedClassName));
 		state.add(qualifiedClassName); // for unique class name check
 		state.add(id); // for coverage check
 	}
 
 	protected void checkPackageNamesCoverage(Set<String> state, EClass eClass) {
 		final String PN = "PackageName";
 		for (EAttribute attribute : eClass.getEAttributes()) {
 			if (attribute.getName().endsWith(PN) && attribute.getName().length() > PN.length()) {
 				String packageName = attribute.getName();
 				packageName = packageName.substring(0, packageName.length() - PN.length());
 				String id = eClass.getName() + ':' + packageName;
 				//System.err.println("check " + id);
 				assertTrue(id + " package name is not checked", state.contains(id));
 			}
 		}
 	}
 
 	protected void checkClassNamesCoverage(Set<String> state, EClass eClass) {
 		final String CN = "ClassName";
 		final String QCN = "QualifiedClassName";
 		final String GET = "get";
 		for (EAttribute attribute : eClass.getEAttributes()) {
 			if (attribute.getName().endsWith(QCN) && attribute.getName().length() > QCN.length()) {
 				// TODO : attribute with fqn; ignore for now
 			} else if (attribute.getName().endsWith(CN) && attribute.getName().length() > CN.length()) {
 				String className = attribute.getName();
 				className = className.substring(0, className.length() - CN.length());
 				className = Character.toUpperCase(className.charAt(0)) + className.substring(1);
 				String id = eClass.getName() + ':' + className;
 				//System.err.println("check simple " + id);
 				assertTrue(id + " simple class name is not checked", state.contains(id));
 			}
 		}
 		for (EOperation operation : eClass.getEOperations()) {
 			if (operation.getName().startsWith(GET) && operation.getName().endsWith(QCN)
 					&& operation.getName().length() > GET.length() + QCN.length()) {
 				String className = operation.getName();
 				className = className.substring(GET.length(), className.length() - QCN.length());
 				String id = eClass.getName() + ':' + className;
 				//System.err.println("check qualified " + id);
 				assertTrue(id + " qualified class name is not checked", state.contains(id));
 			}
 		}
 	}
 
 	private static class GenCommonBaseIterator implements Iterator<GenCommonBase> {
 		private GenCommonBase nextBase;
 		private Iterator<?> wrappedIterator;
 
 		public GenCommonBaseIterator(GenDiagram genDiagram) {
 			assert genDiagram != null;
 			nextBase = genDiagram;
 			wrappedIterator = genDiagram.eAllContents();
 		}
 		public boolean hasNext() {
 			return nextBase != null;
 		}
 
 		public GenCommonBase next() {
 			if (nextBase == null) {
 				throw new NoSuchElementException();
 			}
 			GenCommonBase rv = nextBase;
 			advance();
 			return rv;
 		}
 
 		private void advance() {
 			nextBase = null;
 			while (wrappedIterator.hasNext()) {
 				Object next = wrappedIterator.next();
 				if (next instanceof GenCommonBase) {
 					nextBase = (GenCommonBase) next;
 					break;
 				}
 			}
 		}
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 	}
 	
 	private class CustomLinkModelFacet extends EObjectImpl implements LinkModelFacet {
 		private EList<GenClass> myTypes;
 		protected CustomLinkModelFacet(GenClass[] types) {
 			myTypes = new UnmodifiableEList<GenClass>(types.length, types);
 		}
 		public EList<GenClass> getAssistantSourceTypes() {
 			return myTypes;
 		}
 		public EList<GenClass> getAssistantTargetTypes() {
 			return myTypes;
 		}
 		public GenClass getSourceType() {
 			return null;
 		}
 		public GenClass getTargetType() {
 			return null;
 		}
 		public TreeIterator<EObject> eAllContents() {
 			return null;
 		}
 		public EClass eClass() {
 			return null;
 		}
 		public EObject eContainer() {
 			return null;
 		}
 		public EStructuralFeature eContainingFeature() {
 			return null;
 		}
 		public EReference eContainmentFeature() {
 			return null;
 		}
 		public EList<EObject> eContents() {
 			return null;
 		}
 		public EList<EObject> eCrossReferences() {
 			return null;
 		}
 		public Object eGet(EStructuralFeature feature) {
 			return null;
 		}
 		public Object eGet(EStructuralFeature feature, boolean resolve) {
 			return null;
 		}
 		public boolean eIsProxy() {
 			return false;
 		}
 		public boolean eIsSet(EStructuralFeature feature) {
 			return false;
 		}
 		public Resource eResource() {
 			return null;
 		}
 		public void eSet(EStructuralFeature feature, Object newValue) {}
 		public void eUnset(EStructuralFeature feature) {}
 		public EList<Adapter> eAdapters() {
 			return null;
 		}
 		public boolean eDeliver() {
 			return false;
 		}
 		public void eNotify(Notification notification) {}
 		public void eSetDeliver(boolean deliver) {}
 	}
 }
