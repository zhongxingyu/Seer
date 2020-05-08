 /*
  * Copyright (c) 2006 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Michael Golubev (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.tests.gen;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.gmf.codegen.gmfgen.Attributes;
 import org.eclipse.gmf.codegen.gmfgen.DefaultSizeAttributes;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenFactory;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenPackage;
 import org.eclipse.gmf.codegen.gmfgen.GenCompartment;
 import org.eclipse.gmf.codegen.gmfgen.GenDiagram;
 import org.eclipse.gmf.codegen.gmfgen.GenEditorGenerator;
 import org.eclipse.gmf.codegen.gmfgen.GenNode;
 import org.eclipse.gmf.codegen.gmfgen.GenPlugin;
 import org.eclipse.gmf.codegen.gmfgen.Viewmap;
 import org.eclipse.gmf.internal.codegen.GMFGenConfig;
 import org.eclipse.gmf.internal.common.reconcile.DefaultDecisionMaker;
 import org.eclipse.gmf.internal.common.reconcile.Reconciler;
 import org.eclipse.gmf.internal.common.reconcile.ReconcilerConfigBase;
 import org.eclipse.gmf.tests.ConfiguredTestCase;
 import org.eclipse.gmf.tests.setup.DiaGenSource;
 
 public class CodegenReconcileTest extends ConfiguredTestCase {
 
 	public CodegenReconcileTest(String name) {
 		super(name);
 	}
 
 	protected final GenEditorGenerator getOriginal() {
 		return getSetup().getGenModel().getGenDiagram().getEditorGen();
 	}
 
 	protected final GenEditorGenerator createCopy() {
 		return (GenEditorGenerator) EcoreUtil.copy(getOriginal());
 	}
 
 	public void testLoadGMFGen() throws Exception {
 		GenEditorGenerator original = getOriginal();
 		assertNotNull(original);
 		GenEditorGenerator copy = createCopy();
 		assertNotNull(copy);
 
 		assertFalse(original == copy);
 		assertFalse(original.equals(copy));
 
 		assertEquals(original.getCopyrightText(), copy.getCopyrightText());
 		assertEquals(original.isSameFileForDiagramAndModel(), copy.isSameFileForDiagramAndModel());
 		assertEquals(original.getPackageNamePrefix(), copy.getPackageNamePrefix());
 
 		final String NEW_VALUE = "New Value";
 		copy.setCopyrightText(NEW_VALUE);
 		assertEquals(copy.getCopyrightText(), NEW_VALUE);
 		assertFalse(copy.getCopyrightText().equals(original.getCopyrightText()));
 	}
 
 	public void testReconcileDeepElementWithAlwaysMatcher() throws Exception {
 		class GenPluginChange extends Assert implements UserChange {
 			private final String NEW_PROVIDER = "NewProviderValue";
 			private final String NEW_VERSION = "NewVersionValue";
 			private final String NEW_ID = "NewPluginID";
 			private final String NEW_ACTIVATOR = "NewActivator";
 			private final String NEW_NAME = "NewName With Space";
 			private boolean myExpectedPrintingEnabled;
 
 			public void applyChanges(GenEditorGenerator old) {
 				GenPlugin genPlugin = old.getPlugin();
 				assertNotNull(genPlugin.getProvider());
 				assertNotNull(genPlugin.getVersion());
 				assertNotNull(genPlugin.getID());
 				assertNotNull(genPlugin.getActivatorClassName());
 				assertNotNull(genPlugin.getName());
 				assertFalse(genPlugin.isPrintingEnabled());
 				
 				myExpectedPrintingEnabled = !genPlugin.isPrintingEnabled();
 
 				genPlugin.setProvider(NEW_PROVIDER);
 				genPlugin.setVersion(NEW_VERSION);
 				genPlugin.setID(NEW_ID);
 				genPlugin.setActivatorClassName(NEW_ACTIVATOR);
 				genPlugin.setPrintingEnabled(myExpectedPrintingEnabled);
 				genPlugin.setName(NEW_NAME);
 			}
 			
 			public void assertChangesPreserved(GenEditorGenerator current) {
 				GenPlugin genPlugin = current.getPlugin();
 				assertEquals(NEW_PROVIDER, genPlugin.getProvider());
 				assertEquals(NEW_VERSION, genPlugin.getVersion());
 				assertEquals(NEW_ID, genPlugin.getID());
 				assertEquals(NEW_ACTIVATOR, genPlugin.getActivatorClassName());
 				assertEquals(myExpectedPrintingEnabled, genPlugin.isPrintingEnabled());
 				assertEquals(NEW_NAME, genPlugin.getName());
 			}
 			
 			public ReconcilerConfigBase getReconcilerConfig() {
 				return new GMFGenConfig();
 			}
 		}
 		
 		checkUserChange(new GenPluginChange());
 	}
 	
 	public void testReconcileCompartmentIsListLayout(){
 		class CompartmentChange extends Assert implements UserChange {
 			private int myCompartmentsTotalCount;
 			private final EStructuralFeature myGenCompartmentFeature;
 			private final Boolean myExpectedValue;
 
 			public CompartmentChange(EStructuralFeature genCompartmentFeature, boolean expectedValue){
 				assertEquals(EcorePackage.eINSTANCE.getEBoolean(), genCompartmentFeature.getEType());
 				myGenCompartmentFeature = genCompartmentFeature;
 				myExpectedValue = Boolean.valueOf(expectedValue);
 			}
 			
 			public final void applyChanges(GenEditorGenerator old) {
 				GenDiagram diagram = old.getDiagram();
 				assertFalse("Precondition, we need some nodes", diagram.getChildNodes().isEmpty());
 				assertFalse("Precondition, we need some nodes", diagram.getTopLevelNodes().isEmpty());
 				
 				myCompartmentsTotalCount = 0;
 				for (Iterator allNodes = diagram.getAllNodes().iterator(); allNodes.hasNext();){
 					GenNode next = (GenNode) allNodes.next();
 					for (Iterator compartments = next.getCompartments().iterator(); compartments.hasNext();){
 						GenCompartment nextCompartment = (GenCompartment)compartments.next();
 						myCompartmentsTotalCount++;
 						nextCompartment.eSet(myGenCompartmentFeature, myExpectedValue);
 					}
 				}
 				
 				assertTrue(myCompartmentsTotalCount > 0);
 			}
 			
 			public final void assertChangesPreserved(GenEditorGenerator current) {
 				GenDiagram diagram = current.getDiagram();
 				assertFalse(diagram.getChildNodes().isEmpty());
 				assertFalse(diagram.getTopLevelNodes().isEmpty());
 				
 				int actualCompartmentsTotalCount = 0;
 				for (Iterator allNodes = diagram.getAllNodes().iterator(); allNodes.hasNext();){
 					GenNode next = (GenNode) allNodes.next();
 					for (Iterator compartments = next.getCompartments().iterator(); compartments.hasNext();){
 						GenCompartment nextCompartment = (GenCompartment)compartments.next();
 						actualCompartmentsTotalCount++;
 						Boolean actualValue = (Boolean)nextCompartment.eGet(myGenCompartmentFeature);
 						assertEquals(getChangeDescription(), myExpectedValue, actualValue);
 					}
 				}
 				
 				assertEquals(myCompartmentsTotalCount, actualCompartmentsTotalCount);
 			}
 			
 			public ReconcilerConfigBase getReconcilerConfig() {
 				return new GMFGenConfig();
 			}
 			
 			private String getChangeDescription() {
 				return "CompartmentChange: " + myGenCompartmentFeature.getName() + ":" + myExpectedValue;
 			}
 		}
 		
 		DiaGenSource diaGenSource = getSetup().getGenModel();
 		assertFalse(diaGenSource.getNodeA().getCompartments().isEmpty());
 		assertFalse(diaGenSource.getNodeB().getCompartments().isEmpty());
 		
 		final GMFGenPackage GMFGEN = GMFGenPackage.eINSTANCE;
 		
 //		checkUserChange(new CompartmentChange(GMFGEN.getGenCompartment_CanCollapse(), true));
 //		checkUserChange(new CompartmentChange(GMFGEN.getGenCompartment_CanCollapse(), false));
 		checkUserChange(new CompartmentChange(GMFGEN.getGenCompartment_HideIfEmpty(), true));
 		checkUserChange(new CompartmentChange(GMFGEN.getGenCompartment_HideIfEmpty(), false));
 //		checkUserChange(new CompartmentChange(GMFGEN.getGenCompartment_NeedsTitle(), true));
 //		checkUserChange(new CompartmentChange(GMFGEN.getGenCompartment_NeedsTitle(), false));
		checkUserChange(new CompartmentChange(GMFGEN.getGenCompartment_ListLayout(), true));
 		checkUserChange(new CompartmentChange(GMFGEN.getGenCompartment_ListLayout(), false));
 	}
 	
 	public void testReconcileGenNodes() throws Exception {			
 		class ListLayoutChange extends Assert implements UserChange {
 			private final String NEW_CANONICAL_EP = "MyCanonicalPolicy";
 			private final String NEW_GRAPHICAL_EP = "MyGraphicalPolicy";
 			private final String BAD_CANONICAL_EP = "MyCanonicalEditPolicy"; //changed but still follows "(.*)CanonicalEditPolicy" pattern
 			private final String BAD_GRAPHICAL_EP = "MyGraphicalNodeEditPolicy"; //changed but still follows "(.*)GraphicalNodeEditPolicy" pattern
 			
 			public void applyChanges(GenEditorGenerator old) {
 				EList oldNodes = old.getDiagram().getTopLevelNodes();
 				assertEquals(2, oldNodes.size());
 				GenNode nodeA = (GenNode) oldNodes.get(0);
 				GenNode nodeB = (GenNode) oldNodes.get(1);
 				
 				nodeA.setCanonicalEditPolicyClassName(NEW_CANONICAL_EP);
 				nodeA.setGraphicalNodeEditPolicyClassName(NEW_GRAPHICAL_EP);
 				
 				nodeB.setCanonicalEditPolicyClassName(BAD_CANONICAL_EP);
 				nodeB.setGraphicalNodeEditPolicyClassName(BAD_GRAPHICAL_EP);
 			}
 			
 			public void assertChangesPreserved(GenEditorGenerator current) {
 				EList currentNodes = current.getDiagram().getTopLevelNodes();
 				assertEquals(2, currentNodes.size());
 				GenNode nodeA = (GenNode) currentNodes.get(0);
 				GenNode nodeB = (GenNode) currentNodes.get(1);
 				
 				assertEquals(NEW_CANONICAL_EP, nodeA.getCanonicalEditPolicyClassName());
 				assertEquals(NEW_GRAPHICAL_EP, nodeA.getGraphicalNodeEditPolicyClassName());
 				
 				//FIXME: the checks below handle the problem with string-pattern reconciling approach
 				//remove this checks when we will be able to reconcile this correctly
 				assertFalse(BAD_CANONICAL_EP.equals(nodeB.getCanonicalEditPolicyClassName()));
 				assertFalse(BAD_GRAPHICAL_EP.equals(nodeB.getGraphicalNodeEditPolicyClassName()));
 			}
 			
 			public ReconcilerConfigBase getReconcilerConfig() {
 				return new GMFGenConfig();
 			}
 		}
 		
 		checkUserChange(new ListLayoutChange());
 	}
 	
 	public void testReconcileGenDiagram(){
 		final class DiagramChange extends SingleChange {
 			public DiagramChange(EAttribute attribute, String valueToSet) {
 				super(attribute, valueToSet);
 			}
 
 			public DiagramChange(EAttribute attribute, boolean valueToSet) {
 				super(attribute, valueToSet);
 			}
 
 			protected EObject findChangeSubjet(GenEditorGenerator root) {
 				return root.getDiagram();
 			}
 		}
 		
 		GMFGenPackage GMF = GMFGenPackage.eINSTANCE;
 		checkUserChange(new DiagramChange(GMF.getGenDiagram_Synchronized(), true));
 		checkUserChange(new DiagramChange(GMF.getGenDiagram_Synchronized(), false));
 		
 		checkUserChange(new DiagramChange(GMF.getEditorCandies_CreationWizardIconPath(), null));
 		checkUserChange(new DiagramChange(GMF.getEditorCandies_CreationWizardIconPath(), ""));
 		checkUserChange(new DiagramChange(GMF.getEditorCandies_CreationWizardIconPath(), "\\..\\a\\B\\c"));
 		checkUserChange(new DiagramChange(GMF.getEditorCandies_CreationWizardIconPath(), "http://localhost:8080/"));
 		
 		checkUserChange(new DiagramChange(GMF.getEditorCandies_CreationWizardCategoryID(), null));
 		checkUserChange(new DiagramChange(GMF.getEditorCandies_CreationWizardCategoryID(), ""));
 		checkUserChange(new DiagramChange(GMF.getEditorCandies_CreationWizardCategoryID(), "   "));
 		checkUserChange(new DiagramChange(GMF.getEditorCandies_CreationWizardCategoryID(), "org.eclipse.ui.Examples")); //actual default value
 		checkUserChange(new DiagramChange(GMF.getEditorCandies_CreationWizardCategoryID(), "com.myCompany.TheBestCategory"));
 	}
 
 	public void testReconcileGenDiagram_Shortcuts(){
 		final String[] PROVIDED_FOR = {"ModelA", "ModelB", "ModelC"}; 
 		final String[] CONTAINS_TO = {"txt", "mdm", "taipan"};
 		final String[] EMPTY = new String[0];
 		
 		class ShortcutChange extends Assert implements UserChange {
 			private final String[] myProvidedFor;
 			private final String[] myContainsTo;
 			
 			public ShortcutChange(String[] providedFor, String[] containsTo){
 				myProvidedFor = providedFor;
 				myContainsTo = containsTo;
 			}
 			
 			public void applyChanges(GenEditorGenerator old) {
 				GenDiagram diagram = old.getDiagram();
 				assertNotNull(diagram);
 				diagram.getShortcutsProvidedFor().addAll(Arrays.asList(myProvidedFor));
 				diagram.getContainsShortcutsTo().addAll(Arrays.asList(myContainsTo));
 				
 				assertEqualsLists(Arrays.asList(myProvidedFor), diagram.getShortcutsProvidedFor());
 				assertEqualsLists(Arrays.asList(myContainsTo), diagram.getContainsShortcutsTo());
 			}
 
 			public void assertChangesPreserved(GenEditorGenerator current) {
 				GenDiagram diagram = current.getDiagram();
 				assertEqualsLists(Arrays.asList(myProvidedFor), diagram.getShortcutsProvidedFor());
 				assertEqualsLists(Arrays.asList(myContainsTo), diagram.getContainsShortcutsTo());
 			}
 			
 			public ReconcilerConfigBase getReconcilerConfig() {
 				return new GMFGenConfig();
 			}
 			
 			private void assertEqualsLists(List expected, List actual){
 				assertEquals(new ArrayList(expected), new ArrayList(actual));
 			}
 		}
 		
 		ShortcutChange someChange = new ShortcutChange(PROVIDED_FOR, CONTAINS_TO); 
 		ShortcutChange emptyChange = new ShortcutChange(EMPTY, EMPTY);
 		
 		checkUserChange(someChange);
 		checkUserChange(emptyChange);
 	}
 
 	public void testReconcileGenEditorGenerator_LimitedConfig() throws Exception {
 		class UserChangeImpl extends Assert implements UserChange {
 			private boolean mySameFile;
 			private final boolean myExpectingCopyrightPreserved;
 			
 			public UserChangeImpl(boolean reconcileCopyright){
 				myExpectingCopyrightPreserved = reconcileCopyright;
 			}
 			
 			public void applyChanges(GenEditorGenerator old){
 				old.setCopyrightText("AAA");
 				old.setPackageNamePrefix("BBB");
 				old.setDiagramFileExtension("CCC");
 				
 				mySameFile = !old.isSameFileForDiagramAndModel();
 
 				old.setSameFileForDiagramAndModel(mySameFile);
 
 				// we do not reconcile this with limited config
 				old.setTemplateDirectory("DDD");
 				assertEquals("DDD", old.getTemplateDirectory());
 			}
 			
 			public void assertChangesPreserved(GenEditorGenerator current){
 				if (myExpectingCopyrightPreserved){
 					assertEquals("AAA", current.getCopyrightText());
 				} else {
 					assertFalse("AAA".equals(current.getCopyrightText()));
 				}
 
 				assertEquals("BBB", current.getPackageNamePrefix());
 				assertEquals("CCC", current.getDiagramFileExtension());
 				assertEquals(mySameFile, current.isSameFileForDiagramAndModel());
 				
 				//not expected to be reconciled -- limited config
 				assertFalse("DDD".equals(current.getTemplateDirectory()));
 			}
 			
 			public ReconcilerConfigBase getReconcilerConfig(){
 				return new LimitedGMFGenConfig(myExpectingCopyrightPreserved);
 			}
 		}
 		
 		checkUserChange(new UserChangeImpl(false));
 		checkUserChange(new UserChangeImpl(true));
 		
 	}
 	
 	public void testReconcileGenEditorGenerator(){
 		class GenEditorGeneratorChange extends SingleChange {
 			public GenEditorGeneratorChange(EAttribute attribute, boolean expectedValue) {
 				super(attribute, expectedValue);
 			}
 
 			public GenEditorGeneratorChange(EAttribute attribute, Object expectedValue) {
 				super(attribute, expectedValue);
 			}
 
 			protected final EObject findChangeSubjet(GenEditorGenerator root) {
 				return root;
 			}
 		}
 
 		class TemplateDirectoryChange extends GenEditorGeneratorChange {
 			public TemplateDirectoryChange(String value){
 				super(GMFGenPackage.eINSTANCE.getGenEditorGenerator_TemplateDirectory(), value);
 			}
 		}
 		
 		class DynamicTemplatesChange extends GenEditorGeneratorChange {
 			public DynamicTemplatesChange(boolean value) {
 				super(GMFGenPackage.eINSTANCE.getGenEditorGenerator_DynamicTemplates(), value);
 			}
 		}
 		
 		class ModelIdChange extends GenEditorGeneratorChange {
 			public ModelIdChange(String value){
 				super(GMFGenPackage.eINSTANCE.getGenEditorGenerator_ModelID(), value);
 			}
 		}
 		
 		checkUserChange(new DynamicTemplatesChange(true));
 		checkUserChange(new DynamicTemplatesChange(false));
 		
 		checkUserChange(new TemplateDirectoryChange(null));
 		checkUserChange(new TemplateDirectoryChange(""));
 		checkUserChange(new TemplateDirectoryChange("\\a\\b\\c"));
 		checkUserChange(new TemplateDirectoryChange("c:/my-folder/my templates with space/"));
 
 		checkUserChange(new ModelIdChange("ABC"));
 		checkUserChange(new ModelIdChange("ABC   "));
 		checkUserChange(new ModelIdChange(""));
 		checkUserChange(new ModelIdChange(" "));
 		checkUserChange(new ModelIdChange(null));
 
 		checkUserChange(new GenEditorGeneratorChange(GMFGenPackage.eINSTANCE.getGenEditorGenerator_DomainFileExtension(), "xxx"));
 	}
 	
 	public void testReconcileGenEditorView(){
 		final class EditorChange extends SingleChange {
 			public EditorChange(EAttribute attribute, String valueToSet) {
 				super(attribute, valueToSet);
 			}
 
 			protected EObject findChangeSubjet(GenEditorGenerator root) {
 				return root.getEditor();
 			}
 		}
 		
 		GMFGenPackage GMF = GMFGenPackage.eINSTANCE;
 		checkUserChange(new EditorChange(GMF.getGenEditorView_IconPath(), null));
 		checkUserChange(new EditorChange(GMF.getGenEditorView_IconPath(), ""));
 		checkUserChange(new EditorChange(GMF.getGenEditorView_IconPath(), "//a//b//c"));
 		checkUserChange(new EditorChange(GMF.getGenEditorView_IconPath(), "c:\\myIconsFolder"));
 
 		checkUserChange(new EditorChange(GMF.getGenEditorView_ClassName(), null));
 		checkUserChange(new EditorChange(GMF.getGenEditorView_ClassName(), ""));
 		checkUserChange(new EditorChange(GMF.getGenEditorView_ClassName(), "MyClass"));
 		checkUserChange(new EditorChange(GMF.getGenEditorView_ClassName(), "org.eclipse.MyClass"));
 		
 		checkUserChange(new EditorChange(GMF.getGenEditorView_ID(), null));
 		checkUserChange(new EditorChange(GMF.getGenEditorView_ID(), ""));
 		checkUserChange(new EditorChange(GMF.getGenEditorView_ID(), "my.editor.id"));
 	}
 	
 	public void testReconcileViewmapAttributes(){
 		abstract class AbstractAttributesChange implements UserChange {
 			private int myAffectedViewmapsCount;
 			
 			protected abstract Attributes findAttributes(Viewmap viewmap); 
 			protected abstract Attributes createUserAttributes();
 			protected abstract void assertChanges(Attributes attributes);
 			
 			public final void applyChanges(GenEditorGenerator old) {
 				myAffectedViewmapsCount = 0;
 				for (Iterator allNodes = old.getDiagram().getAllNodes().iterator(); allNodes.hasNext();){
 					GenNode next = (GenNode) allNodes.next();
 					Viewmap nextViewmap = next.getViewmap();
 					if (nextViewmap == null){
 						continue;
 					}
 					Attributes attributes = findAttributes(nextViewmap);
 					assertNull("Reconciler is intended to work with attributes that are created only by user", attributes);
 					attributes = createUserAttributes();
 					nextViewmap.getAttributes().add(attributes);
 					myAffectedViewmapsCount++;
 				}
 				assertTrue(myAffectedViewmapsCount > 0);
 			}
 			
 			public final void assertChangesPreserved(GenEditorGenerator current) {
 				int checkedViewmapsCount = 0;
 				for (Iterator allNodes = current.getDiagram().getAllNodes().iterator(); allNodes.hasNext();){
 					GenNode next = (GenNode)allNodes.next();
 					Viewmap nextViewmap = next.getViewmap();
 					if (nextViewmap == null){
 						continue;
 					}
 					Attributes attributes = findAttributes(nextViewmap);
 					assertNotNull(attributes);
 					assertChanges(attributes);
 					checkedViewmapsCount++;
 				}
 				assertEquals(myAffectedViewmapsCount, checkedViewmapsCount);
 			}
 			
 			public final ReconcilerConfigBase getReconcilerConfig() {
 				return new GMFGenConfig();
 			}
 		}
 		
 		class DefaultSizeChange extends AbstractAttributesChange {
 			private static final int HEIGHT = 23;
 			private static final int WIDTH = 32;
 			
 			protected void assertChanges(Attributes attributes) {
 				DefaultSizeAttributes defaultSize = (DefaultSizeAttributes)attributes;
 				assertEquals(HEIGHT, defaultSize.getHeight());
 				assertEquals(WIDTH, defaultSize.getWidth());
 			}
 			
 			protected Attributes createUserAttributes() {
 				DefaultSizeAttributes defaultSize = GMFGenFactory.eINSTANCE.createDefaultSizeAttributes();
 				defaultSize.setHeight(HEIGHT);
 				defaultSize.setWidth(WIDTH);
 				return defaultSize;
 			}
 			
 			protected Attributes findAttributes(Viewmap viewmap) {
 				return viewmap.find(DefaultSizeAttributes.class);
 			}
 		}
 		
 		checkUserChange(new DefaultSizeChange());
 	}
 	
 	private void checkUserChange(UserChange userChange){
 		GenEditorGenerator old = createCopy();
 		GenEditorGenerator current = createCopy();
 		
 		userChange.applyChanges(old);
 		new Reconciler(userChange.getReconcilerConfig()).reconcileTree(current, old);
 		userChange.assertChangesPreserved(current);
 	}
 	
 	private static interface UserChange {
 		public void applyChanges(GenEditorGenerator old);
 		public void assertChangesPreserved(GenEditorGenerator current);
 		public ReconcilerConfigBase getReconcilerConfig();
 	}
 
 	private static class LimitedGMFGenConfig extends ReconcilerConfigBase {
 		public LimitedGMFGenConfig(boolean reconcileCopyright){
 			final GMFGenPackage GMFGEN = GMFGenPackage.eINSTANCE;
 
 			setMatcher(GMFGEN.getGenEditorGenerator(), ALWAYS_MATCH);
 			if (reconcileCopyright){
 				preserveIfSet(GMFGEN.getGenEditorGenerator(), GMFGEN.getGenEditorGenerator_CopyrightText());
 			}
 			preserveIfSet(GMFGEN.getGenEditorGenerator(), GMFGEN.getGenEditorGenerator_PackageNamePrefix());
 			preserveIfSet(GMFGEN.getGenEditorGenerator(), GMFGEN.getGenEditorGenerator_DiagramFileExtension());
 			preserveIfSet(GMFGEN.getGenEditorGenerator(), GMFGEN.getGenEditorGenerator_SameFileForDiagramAndModel());
 			
 			setMatcher(GMFGEN.getGenPlugin(), ALWAYS_MATCH); //exactly one feature for ALWAYS_MATCH GenEditorGenerator
 			preserveIfSet(GMFGEN.getGenPlugin(), GMFGEN.getGenPlugin_Provider());
 			preserveIfSet(GMFGEN.getGenPlugin(), GMFGEN.getGenPlugin_Version());
 		}
 		
 		private void preserveIfSet(EClass eClass, EAttribute feature){
 			//FIXME: only attributes for now, allow references
 			addDecisionMaker(eClass, new DefaultDecisionMaker(feature));
 		}
 	}
 	
 	private abstract static class SingleChange implements UserChange {
 		private final EAttribute myAttribute;
 		private final Object myValueToSet;
 		private Object myExpectedValue;
 		
 		public SingleChange(EAttribute attribute, boolean valueToSet){
 			this(attribute, Boolean.valueOf(valueToSet));
 		}
 
 		public SingleChange(EAttribute attribute, Object valueToSet){
 			myAttribute = attribute;
 			myValueToSet = valueToSet;
 		}
 		
 		protected abstract EObject findChangeSubjet(GenEditorGenerator root);
 		
 		public void applyChanges(GenEditorGenerator old) {
 			EObject subject = findChangeSubjet(old);
 			assertNotNull(subject);
 			subject.eSet(myAttribute, myValueToSet);
 			myExpectedValue = subject.eGet(myAttribute);
 		}
 		
 		public void assertChangesPreserved(GenEditorGenerator current) {
 			EObject subject = findChangeSubjet(current);
 			assertNotNull(subject);
 			assertEquals(myExpectedValue, subject.eGet(myAttribute));
 		}
 		
 		public ReconcilerConfigBase getReconcilerConfig() {
 			return new GMFGenConfig();
 		}
 	}
 	
 	
 }
