 /*******************************************************************************
  * Copyright (c) 2006-2009
  * Software Technology Group, Dresden University of Technology
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Software Technology Group - TU Dresden, Germany
  *      - initial API and implementation
  ******************************************************************************/
 package org.reuseware.coconut.test.applications;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.Map.Entry;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.URIConverter;
 import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.uml2.uml.UMLPackage;
 import org.eclipse.uml2.uml.resource.UMLResource;
 import org.emftext.language.java.JavaClasspath;
 import org.emftext.language.java.JavaPackage;
 import org.emftext.language.java.resource.JavaSourceOrClassFileResourceFactoryImpl;
 import org.emftext.language.java.resource.java.IJavaOptions;
 import org.emftext.language.java.resource.util.JavaPostProcessor;
 import org.emftext.language.modelquery.resource.CustomModelqueryResourceFactory;
 import org.emftext.language.secprop.SecpropPackage;
 import org.emftext.language.secprop.resource.SecpropResourceFactoryDelegator;
 import org.emftext.language.usecaseinvariant.UsecaseinvariantPackage;
 import org.emftext.language.usecaseinvariant.resource.ucinv.mopp.UcinvResourceFactoryDelegator;
 import org.emftext.language.valueflow.ValueflowPackage;
 import org.junit.Test;
 import org.oasisopen.names.tc.opendocument.xmlns.office.OfficePackage;
 import org.oasisopen.names.tc.opendocument.xmlns.office.util.OfficeResourceFactoryImpl;
import org.reuseware.coconut.test.AbstractReusewareCompositionTest;
 import org.reuseware.coconut.test.AbstractReusewareTestHelper;
 import org.reuseware.coconut.test.sokan.SokanReusewareTestHelper;
 import org.reuseware.sokan.index.util.IndexUtil;
 
public class ReusewareModelSoCCompositionTest extends AbstractReusewareCompositionTest {
 
 	protected final String exampleProject = "org.reuseware.application.ticketshop";
 	protected final String testProject = "org.reuseware.application.ticketshop.test";
 	
 	protected final String[] fileExtensions = {"odt", "uml", "ucinv", "valueflow", "secprop", "java"};
 	
 	@Override
 	public AbstractReusewareTestHelper getTestHelper() {
 		return new SokanReusewareTestHelper();
 		//return new ResourceSetReusewareTestHelper();
 	}
 	 
 	@Test
 	public void testInit() throws Exception {
 		
 		for(Entry<Object, Object> entry : newResourceSet().getLoadOptions().entrySet()) {
 			IndexUtil.INSTANCE.addLoadOption(entry.getKey().toString(), entry.getValue());
 		}
 		// store with default fracol
 		assertRegisterFragmentStore(
 				"org.reuseware.coconut.resource", 
 				"store", 
 				new String[]{});
 		
 		initOutStore(exampleProject);
 		
 		setUpUML();
 		setUpJava();
 		setUpODT();
 		setUpUcinv();
 		setUpValueFlow();
 		setUpSecProp();
 	}
 
 	/* TODO #1667: activate when fixed
 	@Test
 	public void testUseCase() throws Exception {
 		
 		assertRegisterFragmentStore(exampleProject, 
 				"independent-concern-dimensions/concern-dimension-usecase/compositionsystem", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"independent-concern-dimensions/concern-dimension-usecase/instantiation-odt", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"independent-concern-dimensions/concern-dimension-usecase/variant-java", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"independent-concern-dimensions/concern-dimension-usecase/variant-odt", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"independent-concern-dimensions/concern-dimension-usecase/variant-ucinv", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"independent-concern-dimensions/concern-dimension-usecase/variant-usecase_uml", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"independent-concern-dimensions/concern-dimension-usecase/variant-valueflow", fileExtensions);
 		
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-participation/compositionsystem", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-participation/instantiation-odt", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-participation/instantiation-ucinv", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-participation/instantiation-usecase_uml", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-participation/variant-java", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-participation/variant-odt", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-participation/variant-ucinv", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-participation/variant-usecase_uml", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-participation/variant-valueflow", fileExtensions);
 						
 		assertRegisterFragmentStore(exampleProject, 
 				"src", fileExtensions);
 		
 		assertRegisterFragmentStore(exampleProject, 
 				"store", fileExtensions, true);
 		
 		assertFolderStructureContainedIn(exampleProject, "out", testProject, "out_usecase", true);
 	}
 	
 	@Test
 	public void testComplete() throws Exception {
 		assertRegisterFragmentStore(exampleProject, 
 				"independent-concern-dimensions/concern-dimension-class/compositionsystem", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"independent-concern-dimensions/concern-dimension-class/instantiation-odt", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"independent-concern-dimensions/concern-dimension-class/instantiation-ucinv", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"independent-concern-dimensions/concern-dimension-class/instantiation-usecase_uml", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"independent-concern-dimensions/concern-dimension-class/variant-java", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"independent-concern-dimensions/concern-dimension-class/variant-uml", fileExtensions);
 		
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-dataclass/compositionsystem", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-dataclass/instantiation-ucinv", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-dataclass/instantiation-usecase_uml", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-dataclass/variant-java", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-dataclass/variant-uml", fileExtensions);
 		
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-associate/compositionsystem", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-associate/instantiation-ucinv", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-associate/instantiation-usecase_uml", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-associate/variant-java", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-associate/variant-uml", fileExtensions);	
 		
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-exchange/compositionsystem", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-exchange/instantiation-ucinv", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-exchange/instantiation-usecase_uml", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-exchange/variant-java", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-exchange/variant-ucinv", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-exchange/variant-usecase_uml", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-exchange/variant-valueflow", fileExtensions);
 
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-flow/compositionsystem", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-flow/instantiation-ucinv", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-flow/variant-java", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-flow/variant-valueflow", fileExtensions);
 		
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-trigger/compositionsystem", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-trigger/instantiation-valueflow", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-trigger/variant-java", fileExtensions);
 		
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-factory/compositionsystem", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-factory/instantiation-odt", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-factory/instantiation-ucinv", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-factory/instantiation-usecase_uml", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-factory/variant-java", fileExtensions);
 		
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-typebinding/compositionsystem", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-typebinding/instantiation-odt", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-typebinding/instantiation-ucinv", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-typebinding/instantiation-usecase_uml", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-typebinding/variant-java", fileExtensions);
 		
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-app/compositionsystem", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-app/instantiation-odt", fileExtensions);		
 		assertRegisterFragmentStore(exampleProject, 
 				"dependent-concern-dimensions/concern-dimension-app/variant-java", fileExtensions);
 		
 		assertFolderStructureContainedIn(exampleProject, "out", testProject, "out_complete", true);
 	}
 	
 	@Test
 	public void testSecurity() throws Exception {
 		
 		assertRegisterFragmentStore(exampleProject, 
 				"additional-security-concern-dimension/concern-dimension-usecase/variant-secprop", fileExtensions);
 		
 		assertRegisterFragmentStore(exampleProject, 
 				"additional-security-concern-dimension/concern-dimension-participation/variant-secprop", fileExtensions);
 		
 		assertRegisterFragmentStore(exampleProject, 
 				"additional-security-concern-dimension/concern-dimension-exchange/variant-secprop", fileExtensions);
 		
 		assertRegisterFragmentStore(exampleProject, 
 				"additional-security-concern-dimension/concern-dimension-security/compositionsystem", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"additional-security-concern-dimension/concern-dimension-security/instantiation-secprop", fileExtensions);		
 		assertRegisterFragmentStore(exampleProject, 
 				"additional-security-concern-dimension/concern-dimension-security/variant-java", fileExtensions);
 		assertRegisterFragmentStore(exampleProject, 
 				"additional-security-concern-dimension/concern-dimension-security/variant-secprop", fileExtensions);
 		
 		assertRegisterFragmentStore(exampleProject, 
 				"store_security", fileExtensions);
 		
 		assertFolderStructureContainedIn(exampleProject, "out", testProject, "out_security", true);
 	} */
 
 
 	protected void initOutStore(String projectID) throws Exception {
 		// out store
 		File outStoreFolder = new File("../" + projectID + "/out");
 		AbstractReusewareTestHelper.deleteDirectory(outStoreFolder, false, false);
 		outStoreFolder.mkdir();
 		assertRegisterFragmentStore( 
 				projectID, 
 				"out",
 				new String[] {});
 	}
 	
 	protected void setUpJava() {
 		EPackage.Registry.INSTANCE.put("http://www.emftext.org/java",
 				JavaPackage.eINSTANCE);
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
 				"java", new JavaSourceOrClassFileResourceFactoryImpl());
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
 				"modelquery", new CustomModelqueryResourceFactory());
 		
 	}
 	
 	protected void setUpUML() {
 		//register all supported UML versions
 		EPackage.Registry.INSTANCE.put("http://www.eclipse.org/uml2/2.0.0/UML",
 				UMLPackage.eINSTANCE);
 		EPackage.Registry.INSTANCE.put("http://www.eclipse.org/uml2/2.1.0/UML",
 				UMLPackage.eINSTANCE);
 		EPackage.Registry.INSTANCE.put("http://www.eclipse.org/uml2/3.0.0/UML",
 				UMLPackage.eINSTANCE);
 		
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
 				"uml", UMLResource.Factory.INSTANCE);
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
 				"ecore", new EcoreResourceFactoryImpl());
 
 		URIConverter.URI_MAP.put(
 				URI.createURI("pathmap:/UML2_METAMODELS/"),
 				URI.createPlatformPluginURI("org.eclipse.uml2.resources/metamodels/", false));
 		URIConverter.URI_MAP.put(
 				URI.createURI("pathmap:/UML2_PROFILES/"),
 				URI.createPlatformPluginURI("org.eclipse.uml2.resources/profiles/", false));
 		URIConverter.URI_MAP.put(
 				URI.createURI("pathmap:/UML2_LIBRARIES/"),
 				URI.createPlatformPluginURI("org.eclipse.uml2.resources/libraries/", false));
 	
 	}
 
 	protected void setUpODT() {
 		EPackage.Registry.INSTANCE.put(OfficePackage.eNS_URI,
 				OfficePackage.eINSTANCE);
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
 				"odt", new OfficeResourceFactoryImpl());	
 	}
 
 	protected void setUpUcinv() {
 		EPackage.Registry.INSTANCE.put(UsecaseinvariantPackage.eNS_URI,
 				UsecaseinvariantPackage.eINSTANCE);
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
 				"ucinv", new UcinvResourceFactoryDelegator());
 	}
 	
 	protected void setUpValueFlow() {
 		EPackage.Registry.INSTANCE.put(ValueflowPackage.eNS_URI,
 				ValueflowPackage.eINSTANCE);
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
 				"valueflow", new XMIResourceFactoryImpl());
 	}
 	
 	protected void setUpSecProp() {
 		EPackage.Registry.INSTANCE.put(SecpropPackage.eNS_URI,
 				SecpropPackage.eINSTANCE);
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
 				"secprop", new SecpropResourceFactoryDelegator());
 	}
 	
 	protected ResourceSet newResourceSet() {
 		ResourceSet resourceSet = super.newResourceSet();
 		JavaPostProcessor javaPostProcessor = new JavaPostProcessor();
 		resourceSet.getLoadOptions().put(IJavaOptions.RESOURCE_POSTPROCESSOR_PROVIDER,
 				Collections.singletonList(javaPostProcessor));
 		resourceSet.getLoadOptions().put(JavaClasspath.OPTION_ALWAYS_USE_FULLY_QUALIFIED_NAMES,
 				Boolean.TRUE);
 		return resourceSet;
 	}
 }
