 /**
  * 
  */
 package org.eclipse.bpmn2.tests;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.bpmn2.Bpmn2Factory;
 import org.eclipse.bpmn2.Definitions;
 import org.eclipse.bpmn2.ExtensionAttributeValue;
 import org.eclipse.bpmn2.Lane;
 import org.eclipse.bpmn2.Process;
 import org.eclipse.bpmn2.ProcessType;
 import org.eclipse.bpmn2.RootElement;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.impl.EAttributeImpl;
 import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl.SimpleFeatureMapEntry;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.ExtendedMetaData;
 import org.eclipse.emf.ecore.util.FeatureMap;
 import org.eclipse.emf.ecore.util.FeatureMap.Entry;
 import org.eclipse.emf.ecore.xml.type.AnyType;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author Reiner Hille
  *
  */
 public class ExtensibilityTests extends Bpmn2SerializationTest {
 
     protected Definitions model;
 
     /**
      * Prepares a test run by initializing all fields.
      * 
      * A basic BPMN2 model is created in {@link #model}, thereby initializing the BPMN2 package.
      */
     @Before
     public void setUpModel() {
         model = TestHelper.initBasicModel("urn:tns1");
     }
 
     /**
      * The extension for all files that are created.
      * @return File extension, i.e. {@code "bpmn2"}.
      */
     @Override
     protected String getFileExtension() {
         return EXTENSION_BPMN2_XML;
     }
 
     @Test
     public void testAttributeExtension() throws IOException {
         Process p = Bpmn2Factory.eINSTANCE.createProcess();
         p.setProcessType(ProcessType.NONE);
         p.setIsExecutable(false);
         ExtendedMetaData metadata = ExtendedMetaData.INSTANCE;
         EAttributeImpl extensionAttribute = (EAttributeImpl) metadata.demandFeature(
                 "http://example.org", "packageName", false, false);
         SimpleFeatureMapEntry extensionEntry = new SimpleFeatureMapEntry(extensionAttribute,
                 "myPackage");
         p.getAnyAttribute().add(extensionEntry);
         model.getRootElements().add(p);
         // Checks
         Resource res = saveAndLoadModel("extensionAttributeTest", model);
         Definitions d = TestHelper.getRootDefinitionElement(res);
         assertEquals(d.getRootElements().size(), 1);
         assertTrue(d.getRootElements().get(0) instanceof Process);
         Process p2 = (Process) d.getRootElements().get(0);
         assertEquals(p2.getAnyAttribute().size(), 1);
         FeatureMap.Entry extAttribute2 = p2.getAnyAttribute().get(0);
         assertEquals(extAttribute2.getEStructuralFeature().getName(), "packageName");
         assertEquals(extAttribute2.getValue(), "myPackage");
     }
 
     @Test
     public void testElementExtension() throws IOException {
         Process p = Bpmn2Factory.eINSTANCE.createProcess();
         p.setProcessType(ProcessType.NONE);
         p.setIsExecutable(false);
         ExtendedMetaData metadata = ExtendedMetaData.INSTANCE;
         EAttributeImpl extensionAttribute = (EAttributeImpl) metadata.demandFeature(
                 "http://example.org", "foo", true, false);
         SimpleFeatureMapEntry extensionEntry = new SimpleFeatureMapEntry(extensionAttribute, "bar");
         ExtensionAttributeValue extension = Bpmn2Factory.eINSTANCE.createExtensionAttributeValue();
         extension.getValue().add(extensionEntry);
 
         p.getExtensionValues().add(extension);
         // It looks odd that you need to add an extensionAttributeValue that contains a list of 
         // extension to a list here.
         // However, the XSD allows only 0..1 element here, while CMOF allows multiple. 
 
         model.getRootElements().add(p);
         // Checks
         Resource res = saveAndLoadModel("extensionElementTest", model);
         Definitions d = TestHelper.getRootDefinitionElement(res);
         assertEquals(d.getRootElements().size(), 1);
         assertTrue(d.getRootElements().get(0) instanceof Process);
         Process p2 = (Process) d.getRootElements().get(0);
         assertEquals(p2.getExtensionValues().size(), 1);
         ExtensionAttributeValue ext2 = p2.getExtensionValues().get(0);
         assertEquals(ext2.getValue().size(), 1);
         FeatureMap.Entry extAttribute2 = ext2.getValue().get(0);
         assertEquals(extAttribute2.getEStructuralFeature().getName(), "foo");
         assertTrue(extAttribute2.getValue() instanceof AnyType);
         AnyType any = (AnyType) extAttribute2.getValue();
         assertEquals(any.getMixed().getValue(0), "bar"); // Simple content
     }
 
     @Test
     public void testExternalElementExtension() {
         // Tests an external file if the extension attributes can be read correctly.
         Resource res = TestHelper.getResource(URI
                 .createFileURI("res/extensibility/SimpleExtensions.bpmn2"));
         EObject sample = res.getEObject("sid-C400BBFB-71BB-43BD-B826-DF671E131A39");
         assertTrue(sample instanceof Lane);
         Lane lane = (Lane) sample;
         assertEquals(lane.getExtensionValues().size(), 1);
         ExtensionAttributeValue ext = lane.getExtensionValues().get(0);
         assertEquals(ext.getValue().size(), 1);
         FeatureMap.Entry entry = ext.getValue().get(0);
         assertTrue(entry.getValue() instanceof AnyType);
         assertEquals(entry.getEStructuralFeature().getName(), "signavioMetaData");
         AnyType anyType = (AnyType) entry.getValue();
         assertEquals(anyType.getAnyAttribute().size(), 2);
         Entry colorAttribute = anyType.getAnyAttribute().get(0);
         assertEquals(colorAttribute.getEStructuralFeature().getName(), "metaKey");
         assertEquals(colorAttribute.getValue(), "bgcolor");
     }
 
     @Test
    // This tests currently raises an ERROR - since commit 640ecc... (never succeeded) [HH, 2011-03-16]
     public void testExternalTypedExtension() {
         // Tests an external file if the extension attributes can be read correctly.
         Resource res = TestHelper.getResource(URI.createFileURI(new File(
                 "res/extensibility/TypedExtension.bpmn2").getAbsolutePath()));
         // TOFIX: Extension Schema loading currently only work with absolute URIs. 
         // Else the Schema location is considered relative to current working directory and not relative to BPMN file.
         EObject sample = res.getEObject("ID_1");
         assertTrue(sample instanceof RootElement);
         assertEquals(sample.eClass().getName(), "TSubclassExample1");
     }
 }
