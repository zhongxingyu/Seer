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
 import java.util.Map.Entry;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.URIConverter;
 import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
 import org.eclipse.uml2.uml.UMLPackage;
 import org.eclipse.uml2.uml.resource.UMLResource;
 import org.emftext.language.java.JavaClasspath;
 import org.emftext.language.java.JavaPackage;
 import org.emftext.language.java.resource.JavaSourceOrClassFileResourceFactoryImpl;
 import org.emftext.language.java.resource.java.IJavaOptions;
 import org.emftext.language.java.resource.util.JavaPostProcessor;
 import org.emftext.language.java.reusejava.ReusejavaPackage;
import org.emftext.language.java.reusejava.resource.reusejava.IReusejavaOptions;
 import org.emftext.language.java.reusejava.resource.reusejava.mopp.ReusejavaResourceFactory;
 import org.emftext.language.java.reusejava.resource.util.ReuseJavaPostProcessor;
 import org.emftext.language.modelquery.resource.CustomModelqueryResourceFactory;
 import org.emftext.language.simpleweave.SimpleweavePackage;
 import org.emftext.language.simpleweave.WModel;
 import org.emftext.language.simpleweave.resource.simpleweave.mopp.SimpleweaveResourceFactory;
 import org.junit.Test;
 import org.reuseware.application.taipan.TaiPanPackage;
 import org.reuseware.coconut.fragment.Fragment;
 import org.reuseware.coconut.test.AbstractReusewareCompositionTest;
 import org.reuseware.coconut.test.AbstractReusewareTestHelper;
 import org.reuseware.coconut.test.sokan.SokanReusewareTestHelper;
 import org.reuseware.sokan.index.util.IndexUtil;
 
 import es.tid.cim.CimPackage;
 
 public class ReusewareApplicationsCompositionTest extends AbstractReusewareCompositionTest {
 
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
 	}
 
 	@Test
 	public void testClassWeaving() throws Exception {
 		String classWeavingProject = "org.reuseware.application.classweaving";
 		
 		//UML 
 		setUpUML();
 		//Java
 		setUpJava();
 		//Ecore is registered naturally
 		
 		initOutStore(classWeavingProject);
 		
 		assertRegisterFragmentStore( 
 				classWeavingProject, 
 				"store",
 				new String[] {});
 		
 		//UML 
 		assertRegisterFragmentStore( 
 				classWeavingProject, 
 				"storeUML",
 				new String[] {"uml"});
 		
 		//Ecore
 		assertRegisterFragmentStore( 
 				classWeavingProject, 
 				"storeEcore",
 				new String[] {"ecore"});
 		
 		//Java
 		assertRegisterFragmentStore( 
 				classWeavingProject, 
 				"storeJava",
 				new String[] {"java","modelquery"});
 		
 		assertCompositionCorrect(
 				new String [] {"ClassWeaving","uml","models","core_composed","FileSystem.uml"},
 				new String [] {"ClassWeaving","uml","models","core_reference","FileSystem.uml"});
 		
 		assertCompositionCorrect(
 				new String [] {"ClassWeaving","ecore","models","core_composed","FileSystem.ecore"},
 				new String [] {"ClassWeaving","ecore","models","core_reference","FileSystem.ecore"});
         
 		//Note: we can not compare the single Java files (e.g. FSFile.java) since the different cross-resource
 		//      references lead to compare errors 
 		assertCompositionCorrect(
 				new String [] {"ClassWeaving","java","models","core_composed","FileSystem.modelquery"},
 				new String [] {"ClassWeaving","java","models","core_reference","FileSystem.modelquery"},
 				new String[] {});
 		
 		
 		//With simple weave composition language
 		setUpSimpleWeave();
 		assertRegisterFragmentStore( 
 				classWeavingProject, 
 				"storeSimpleWeave",
 				new String[] {"simpleweave"});		
 		
 		//get the simple weave composition program
 		Fragment swFragment = getFragment(new String [] {"ClassWeaving","simpleweave","cps","fsObserver.simpleweave"});
 		assertFalse("Simpleweave composition not found", swFragment.getContents().isEmpty());
 		
 		WModel wModel = (WModel) swFragment.getContents().get(0);
 
 		//initially it is set to uml
 		assertTrue("Unexpected type setting: " + wModel.getType(),
 				wModel.getType().equals("uml"));
 		
 		assertCompositionCorrect(
 				new String [] {"ClassWeaving","simpleweave","cps","fsObserver.uml"},
 				new String [] {"ClassWeaving","uml","models","core_reference","FileSystem.uml"});
 	
 		//switch to ecore
 		wModel.setType("ecore");
 
 		//inform index about change
 		updateArtifact(wModel);		
 		
 		assertCompositionCorrect(
 				new String [] {"ClassWeaving","simpleweave","cps","fsObserver.ecore"},
 				new String [] {"ClassWeaving","ecore","models","core_reference","FileSystem.ecore"});
 		
 		
 		//and back to uml (original state)
 		wModel.setType("uml");
 		updateArtifact(wModel);	
 		
 		assertCompositionCorrect(
 				new String [] {"ClassWeaving","simpleweave","cps","fsObserver.uml"},
 				new String [] {"ClassWeaving","uml","models","core_reference","FileSystem.uml"});
 	}
 	
 	@Test
 	public void testReuseUML() throws Exception {
 		String reuseumlProject = "org.reuseware.application.reuseuml";
 		
 		setUpUML();
 		
 		initOutStore(reuseumlProject);
 		
 		assertRegisterFragmentStore( 
 				reuseumlProject, 
 				"store",
 				new String[] {"uml"});
 		assertRegisterFragmentStore( 
 				reuseumlProject, 
 				"test",
 				new String[] {"uml"});
 		
 		assertCompositionCorrect(
 				new String [] {"org","reuseware","application","reuseuml","example","composed","OrderProcessing.uml"},
 				new String [] {"OrderProcessing.uml"});
 		
 	}
 	
 	@Test
 	public void testReuseJava() throws Exception {
 		String reusejavaProject = "org.reuseware.application.reusejava";
 		
 		setUpJava();
 		
 		initOutStore(reusejavaProject);
 		
 		assertRegisterFragmentStore( 
 				reusejavaProject, 
 				"store",
 				new String[] {"java"});
 		assertRegisterFragmentStore( 
 				reusejavaProject, 
 				"test",
 				new String[] {"java"});
 		
 		assertCompositionCorrect(
 				new String [] {"org","reuseware","application","reusejava","example","SecureCustomer.java"},
 				new String [] {"SecureCustomer.java"});
 		
 	}
 	
 	@Test
 	public void testUML2Java() throws Exception {
 		String uml2javaProject = "org.reuseware.application.uml2java";
 		
 		setUpJava();
 		setUpUML();
 		setUpReuseJava();
 		
 		initOutStore(uml2javaProject);
 
 		assertRegisterFragmentStore( 
 				uml2javaProject, 
 				"transformation",
 				new String[] {"reusejava"});
 		assertRegisterFragmentStore( 
 				uml2javaProject, 
 				"models",
 				new String[] {"uml"});
 		assertRegisterFragmentStore( 
 				uml2javaProject, 
 				"code",
 				new String[] {"reusejava"});
 		assertRegisterFragmentStore( 
 				uml2javaProject, 
 				"test",
 				new String[] {"java"});
 		
 		assertCompositionCorrect(
 				new String [] {"org","reuseware","application","uml2java","example","Item.java"},
 				new String [] {"Item.java"});
 		assertCompositionCorrect(
 				new String [] {"org","reuseware","application","uml2java","example","Bakery.java"},
 				new String [] {"Bakery.java"});
 		
 	}
 
 	@Test
 	public void testReuseTaipan() throws Exception {
 		String taosdExampleProject = "org.reuseware.application.taipan.example.taosd";
 		
 		EPackage.Registry.INSTANCE.put(
 				TaiPanPackage.eNS_PREFIX, TaiPanPackage.eINSTANCE);
 		
 		initOutStore(taosdExampleProject);
 
 		assertRegisterFragmentStore( 
 				"org.reuseware.application.taipan", 
 				"store",
 				new String[] {"taipan"});
 		
 		assertRegisterFragmentStore( 
 				taosdExampleProject, 
 				"store",
 				new String[] {"taipan"});
 		
 		assertCompositionCorrect(
 				new String [] {"org","reuseware","application","taipan","example","taosd","composed","TravelPlan.taipan"},
 				new String [] {"org","reuseware","application","taipan","example","taosd","reference.taipan"});
 		
 	}
 		
 	@Test
 	public void testCBSESolution() throws Exception {
 		String cbseSolutionProject = "org.reuseware.application.cbse.solution";
 		String cbseExerciseProject = "org.reuseware.application.cbse.exercise";
 		
 		setUpUML();
 		
 		//check that the files contained in the exercise are also contained in the solution
 		//to ensure that the exercise was updated along with the solution.
 		//the composition itself is then checked on the solution below.
 		assertFolderStructureContainedIn(cbseExerciseProject, "store", cbseSolutionProject, "store", false);
 		
 		initOutStore(cbseSolutionProject);
 		
 		assertRegisterFragmentStore(
 				cbseSolutionProject, 
 				"store",
 				new String[] {"uml"});
 		
 		assertCompositionCorrect(
 				new String [] {"CBSE","reference","TicketSale","StateMachine_composed","TicketSaleStateMachine.uml"},
 				new String [] {"CBSE","reference","TicketSale","StateMachine_reference","TicketSaleStateMachine.uml"});
 		
 		assertCompositionCorrect(
 				new String [] {"CBSE","exercise","StateMachine_composed","PizzaShopStateMachine.uml"},
 				new String [] {"CBSE","exercise","StateMachine_reference","PizzaShopStateMachine.uml"});
 		
 		assertCompositionCorrect(
 				new String [] {"CBSE","exercise","ClassDiagrams_composed","PizzaShop.uml"},
 				new String [] {"CBSE","exercise","ClassDiagrams_reference","PizzaShop.uml"});
 		
 	}
 
 	@Test
 	public void testSWT2Solution() throws Exception {
 		String swt2SolutionProject = "org.reuseware.application.swt2.solution";
 		String swt2ExerciseProject = "org.reuseware.application.swt2.exercise";
 		
 		setUpUML();
 		
 		//check that the files contained in the exercise are also contained in the solution
 		//to ensure that the exercise was updated along with the solution.
 		//the composition itself is then checked on the solution below.
 		assertFolderStructureContainedIn(swt2ExerciseProject, "store", swt2SolutionProject, "store", false);
 		
 		initOutStore(swt2SolutionProject);
 		
 		assertRegisterFragmentStore(
 				swt2SolutionProject, 
 				"store",
 				new String[] {"uml"});
 		
 		assertCompositionCorrect(
 				new String [] {"SWT2","reference","TicketSale","StateMachine_composed","TicketSaleStateMachine.uml"},
 				new String [] {"SWT2","reference","TicketSale","StateMachine_reference","TicketSaleStateMachine.uml"});
 		assertCompositionCorrect(
 				new String [] {"SWT2","reference","TicketSale","ClassDiagrams_composed","TicketSaleDataModel.uml"},
 				new String [] {"SWT2","reference","TicketSale","ClassDiagrams_reference","TicketSaleDataModel.uml"});
 		
 		assertCompositionCorrect(
 				new String [] {"SWT2","exercise","PizzaShop","StateMachine_composed","PizzaShopStateMachine.uml"},
 				new String [] {"SWT2","exercise","PizzaShop","StateMachine_reference","PizzaShopStateMachine.uml"});
 		assertCompositionCorrect(
 				new String [] {"SWT2","exercise","PizzaShop","ClassDiagrams_composed","PizzaShopDataModel.uml"},
 				new String [] {"SWT2","exercise","PizzaShop","ClassDiagrams_reference","PizzaShopDataModel.uml"});	
 	}
 	
 	@Test
 	public void testCIMLibrary() throws Exception {
 		EPackage.Registry.INSTANCE.put(
 				CimPackage.eNS_URI, CimPackage.eINSTANCE);
 
 		initOutStore("org.modelplex.cim.tidlibrary");
 		
 		assertRegisterFragmentStore(
 				"org.reuseware.application.cim", 
 				"store",
 				new String[] {"cim"});
 		
 		assertRegisterFragmentStore( 
 				"org.modelplex.cim.tidlibrary", 
 				"store",
 				new String[] {"cim"});
 		
 		assertRegisterFragmentStore( 
 				"org.modelplex.cim.tidlibrary", 
 				"store_test",
 				new String[] {"cim"});
 		
 		//level2
 		assertCompositionCorrect(
 				new String [] {"org","modelplex","cim","tidlibrary","level2","logicalInterfaces","ethernetDynamicIPInterface","ethernetDynamicIPInterface.cim"},
 				new String [] {"org","modelplex","cim","tidlibrary","level2","logicalInterfaces","ethernetDynamicIPInterface","reference.cim"});
 		assertCompositionCorrect(
 				new String [] {"org","modelplex","cim","tidlibrary","level2","logicalInterfaces","ethernetIPInterface","ethernetIPInterface.cim"},
 				new String [] {"org","modelplex","cim","tidlibrary","level2","logicalInterfaces","ethernetIPInterface","reference.cim"});
 		assertCompositionCorrect(
 				new String [] {"org","modelplex","cim","tidlibrary","level2","logicalInterfaces","wirelessDynamicIPInterface","wirelessDynamicIPInterface.cim"},
 				new String [] {"org","modelplex","cim","tidlibrary","level2","logicalInterfaces","wirelessDynamicIPInterface","reference.cim"});
 
 		//level3
 		assertCompositionCorrect(
 				new String [] {"org","modelplex","cim","tidlibrary","level3","networkDevices","providerRouter","providerRouter.cim"},
 				new String [] {"org","modelplex","cim","tidlibrary","level3","networkDevices","providerRouter","reference.cim"});
 		assertCompositionCorrect(
 				new String [] {"org","modelplex","cim","tidlibrary","level3","networkDevices","routerGateway","routerGateway.cim"},
 				new String [] {"org","modelplex","cim","tidlibrary","level3","networkDevices","routerGateway","reference.cim"});
 		assertCompositionCorrect(
 				new String [] {"org","modelplex","cim","tidlibrary","level3","networkDevices","wiredHomeDevice","wiredHomeDevice.cim"},
 				new String [] {"org","modelplex","cim","tidlibrary","level3","networkDevices","wiredHomeDevice","reference.cim"});
 		assertCompositionCorrect(
 				new String [] {"org","modelplex","cim","tidlibrary","level3","networkDevices","wirelessHomeDevice","wirelessHomeDevice.cim"},
 				new String [] {"org","modelplex","cim","tidlibrary","level3","networkDevices","wirelessHomeDevice","reference.cim"});
 
 		//level4
 		assertCompositionCorrect(
 				new String [] {"org","modelplex","cim","tidlibrary","level4","networkTopologies","homeADSLService","homeADSLService.cim"},
 				new String [] {"org","modelplex","cim","tidlibrary","level4","networkTopologies","homeADSLService","reference.cim"});
 		
 	}
 
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
 		EPackage.Registry.INSTANCE.put(JavaPackage.eNS_URI,
 				JavaPackage.eINSTANCE);
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
 				"java", new JavaSourceOrClassFileResourceFactoryImpl());
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
 				"modelquery", new CustomModelqueryResourceFactory());	
 	}
 	
 	protected void setUpReuseJava() {
 		EPackage.Registry.INSTANCE.put(ReusejavaPackage.eNS_URI,
 				ReusejavaPackage.eINSTANCE);
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
 				"reusejava", new ReusejavaResourceFactory());	
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
 	
 	protected void setUpSimpleWeave() {
 		EPackage.Registry.INSTANCE.put(SimpleweavePackage.eNS_URI,
 				SimpleweavePackage.eINSTANCE);
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
 				"simpleweave", new SimpleweaveResourceFactory());
 	}
 	
 	protected ResourceSet newResourceSet() {
 		ResourceSet resourceSet = super.newResourceSet();
 		JavaPostProcessor javaPostProcessor = new JavaPostProcessor();
 		ReuseJavaPostProcessor reuseJavaPostProcessor = new ReuseJavaPostProcessor();
 		
 		resourceSet.getLoadOptions().put(IJavaOptions.RESOURCE_POSTPROCESSOR_PROVIDER,
				javaPostProcessor);
		resourceSet.getLoadOptions().put(IReusejavaOptions.RESOURCE_POSTPROCESSOR_PROVIDER,
				reuseJavaPostProcessor);
 		resourceSet.getLoadOptions().put(JavaClasspath.OPTION_ALWAYS_USE_FULLY_QUALIFIED_NAMES,
 				Boolean.TRUE);
 		return resourceSet;
 	}
 
 }
