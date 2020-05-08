 package org.eclipse.rmf.reqif10.tests.uc000.tc4000;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EcoreFactory;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.rmf.reqif10.ReqIF;
 import org.eclipse.rmf.reqif10.common.util.ReqIFToolExtensionUtil;
 import org.eclipse.rmf.reqif10.tests.util.MinimalModelBuilder;
 
 public class TC0004000ToolExtensionWithEcoreMetamodelModelBuilder extends MinimalModelBuilder {
 
 	public TC0004000ToolExtensionWithEcoreMetamodelModelBuilder() throws Exception {
 		super();
 	}
 
 	@Override
 	public void createToolExtensions() throws Exception {
 		ReqIF reqIF = getReqIF();
 
 		// get the actual tool extension
 		EPackage rootEPackage = EcoreFactory.eINSTANCE.createEPackage();
 		rootEPackage.setName("RootPackge");
 
 		EPackage nestedEPackage = EcoreFactory.eINSTANCE.createEPackage();
 		nestedEPackage.setName("NodesPackge");
 		rootEPackage.getESubpackages().add(nestedEPackage);
 
 		EClass rootNode = EcoreFactory.eINSTANCE.createEClass();
 		rootNode.setName("RootNode");
 		nestedEPackage.getEClassifiers().add(rootNode);
 		EAttribute eAttribute = EcoreFactory.eINSTANCE.createEAttribute();
 		eAttribute.setName("rootAttribute");
 		eAttribute.setEType(EcorePackage.eINSTANCE.getEString());
 
 		EClass childNode = EcoreFactory.eINSTANCE.createEClass();
 		childNode.setName("ChildNode");
 		nestedEPackage.getEClassifiers().add(childNode);
 
 		EReference eReference = EcoreFactory.eINSTANCE.createEReference();
 		eReference.setName("children");
 		eReference.setLowerBound(0);
 		eReference.setUpperBound(-1);
 		eReference.setContainment(true);
 		eReference.setEType(childNode);
 		rootNode.getEStructuralFeatures().add(eReference);
 
 		// hook the tool extension model into the tool extension class
 		ReqIFToolExtensionUtil.addToolExtension(reqIF, rootEPackage);
 	}
 }
