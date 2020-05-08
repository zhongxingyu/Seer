 /**
  * <copyright>
  *
  * Copyright (c) 2011 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.ocl.examples.test.xtext;
 
 import java.io.IOException;
 
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EValidator;
 import org.eclipse.emf.ecore.plugin.EcorePlugin;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EObjectValidator;
 import org.eclipse.ocl.examples.domain.messages.EvaluatorMessages;
 import org.eclipse.ocl.examples.domain.values.Bag;
 import org.eclipse.ocl.examples.domain.values.impl.BagImpl;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManagerResourceAdapter;
import org.eclipse.ocl.examples.pivot.manager.MetaModelManagerResourceSetAdapter;
 import org.eclipse.ocl.examples.pivot.utilities.PivotDiagnostician;
 import org.eclipse.ocl.examples.pivot.utilities.PivotEnvironmentFactory;
 import org.eclipse.ocl.examples.xtext.base.utilities.BaseCSResource;
 import org.eclipse.ocl.examples.xtext.base.utilities.CS2PivotResourceAdapter;
 import org.eclipse.ocl.examples.xtext.completeocl.validation.CompleteOCLEObjectValidator;
 import org.eclipse.ocl.examples.xtext.oclinecore.validation.OCLinEcoreEObjectValidator;
 import org.eclipse.ocl.examples.xtext.tests.XtextTestCase;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Tests that OCL for model validation works.
  */
 public class ValidateTests extends XtextTestCase
 {	
 	protected MetaModelManager metaModelManager;
 
 	public static void checkValidationDiagnostics(EObject testInstance, int severity, String... expectedMessage) {
 		Bag<String> expectedMessages = new BagImpl<String>();
 		for (String message : expectedMessage) {
 			expectedMessages.add(message);
 		}
 		Diagnostic diagnostics = PivotDiagnostician.INSTANCE.validate(testInstance);
 		Bag<String> actualMessages = new BagImpl<String>();
 		for (Diagnostic diagnostic : diagnostics.getChildren()) {
 			assertEquals(severity, diagnostic.getSeverity());
 			actualMessages.add(diagnostic.getMessage());
 		}
 		String s = formatMessageDifferences(expectedMessages, actualMessages);
 		if (s != null) {
 			fail("Inconsistent validation: (expected/actual) message" + s);
 		}
 	}	
 
 	public Resource doLoadOCLinEcore(String stem) throws IOException {
 		String inputName = stem + ".oclinecore";
 		String ecoreName = stem + ".ecore";
 		URI inputURI = getProjectFileURI(inputName);
 		URI ecoreURI = getProjectFileURI(ecoreName);
 		MetaModelManager metaModelManager = new MetaModelManager();
 		try {
 //			ResourceSet resourceSet = new ResourceSetImpl();
 			BaseCSResource xtextResource = (BaseCSResource) resourceSet.createResource(inputURI);
 			MetaModelManagerResourceAdapter.getAdapter(xtextResource, metaModelManager);
 			xtextResource.load(null);
 			assertNoResourceErrors("Load failed", xtextResource);
 			CS2PivotResourceAdapter adapter = CS2PivotResourceAdapter.getAdapter(xtextResource, metaModelManager);
 			Resource pivotResource = adapter.getPivotResource(xtextResource);
 			assertNoUnresolvedProxies("Unresolved proxies", xtextResource);
 			assertNoValidationErrors("Pivot validation errors", pivotResource.getContents().get(0));
 			Resource ecoreResource = savePivotAsEcore(metaModelManager, pivotResource, ecoreURI, true);
 			return ecoreResource;
 		}
 		finally {
 			metaModelManager.dispose();
 		}
 	}
 
 	protected EObject eCreate(EPackage ePackage, String className) {
 		EClass eClass = (EClass) ePackage.getEClassifier(className);
 		EFactory eFactoryInstance = ePackage.getEFactoryInstance();
 		return eFactoryInstance.create(eClass);
 	}
 
 	protected void eSet(EObject eObject, String eFeatureName, Object value) {
 		EClass eClass = eObject.eClass();
 		EStructuralFeature eFeature = eClass.getEStructuralFeature(eFeatureName);
 		assert eFeature != null;
 		eObject.eSet(eFeature, value);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		metaModelManager = new MetaModelManager();
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		if (metaModelManager != null) {
 			metaModelManager.dispose();
 			metaModelManager = null;
 		}
 		PivotEnvironmentFactory.disposeGlobalRegistryInstance();
 		super.tearDown();
 	}
 
 	public void testValidate_Bug366229_oclinecore() throws IOException, InterruptedException {
 		//
 		//	Create model
 		//
 		Resource ecoreResource = doLoadOCLinEcore("Bug366229");
		MetaModelManagerResourceSetAdapter.getAdapter(ecoreResource.getResourceSet(), metaModelManager);
 		EPackage overloadsPackage = (EPackage) ecoreResource.getContents().get(0);
 		EObject testInstance = eCreate(overloadsPackage, "SubClass");
 		//
 		//	Check EObjectValidator errors
 		//
 		EValidator.Registry.INSTANCE.put(overloadsPackage, EObjectValidator.INSTANCE);
 		checkValidationDiagnostics(testInstance, Diagnostic.ERROR);
 	}
 
 	public void testValidate_Validate_completeocl() throws IOException, InterruptedException {
 		//
 		//	Create model
 		//
 		Resource ecoreResource = doLoadOCLinEcore("Validate");
 		EPackage validatePackage = (EPackage) ecoreResource.getContents().get(0);
 		URI oclURI = getProjectFileURI("Validate.ocl");
 		CompleteOCLEObjectValidator completeOCLEObjectValidator = new CompleteOCLEObjectValidator(validatePackage, oclURI, metaModelManager);
 		EValidator.Registry.INSTANCE.put(validatePackage, completeOCLEObjectValidator);
 		try {
 			EObject testInstance = eCreate(validatePackage, "Level3");
 			String template = EvaluatorMessages.ValidationConstraintIsNotSatisfied_ERROR_;
 			//
 			//	No errors
 			//
 			eSet(testInstance, "ref", "xx");
 			eSet(testInstance, "l1", "xx");
 			eSet(testInstance, "l2a", "xx");
 			eSet(testInstance, "l2b", "xx");
 			eSet(testInstance, "l3", "xx");
 			String objectLabel = PivotDiagnostician.INSTANCE.getObjectLabel(testInstance);
 			checkValidationDiagnostics(testInstance, Diagnostic.WARNING);
 			//
 			//	CompleteOCL errors all round
 			//
 			eSet(testInstance, "ref", "xxx");
 			eSet(testInstance, "l1", "xxx");
 			eSet(testInstance, "l2a", "xxx");
 			eSet(testInstance, "l2b", "xxx");
 			eSet(testInstance, "l3", "xxx");
 			checkValidationDiagnostics(testInstance, Diagnostic.WARNING,
 				NLS.bind(template,  "V1", objectLabel),
 				NLS.bind(template,  "V2a", objectLabel),
 				NLS.bind(template,  "V2b", objectLabel),
 				NLS.bind(template,  "V3", objectLabel));
 			//
 			//	One CompleteOCl and one OCLinEcore
 			//
 			eSet(testInstance, "ref", "ok");
 			eSet(testInstance, "l1", "ok");
 			eSet(testInstance, "l2a", "bad");
 			eSet(testInstance, "l2b", "ok");
 			eSet(testInstance, "l3", "ok");
 			checkValidationDiagnostics(testInstance, Diagnostic.WARNING,
 				NLS.bind(template,  "L2a", objectLabel),
 				NLS.bind(template,  "V2a", objectLabel));
 		}
 		finally {
 			EValidator.Registry.INSTANCE.remove(validatePackage);			
 		}
 	}
 
 	public void testValidate_Validate_oclinecore() throws IOException, InterruptedException {
 		//
 		//	Create model
 		//
 		Resource ecoreResource = doLoadOCLinEcore("Validate");
		MetaModelManagerResourceSetAdapter.getAdapter(ecoreResource.getResourceSet(), metaModelManager);
 		EPackage validatePackage = (EPackage) ecoreResource.getContents().get(0);
 		EObject testInstance = eCreate(validatePackage, "Level3");
 		eSet(testInstance, "ref", "ref");
 		eSet(testInstance, "l1", "l1");
 		eSet(testInstance, "l2a", "l2a");
 		eSet(testInstance, "l2b", "l2b");
 		eSet(testInstance, "l3", "l3");
 		String objectLabel = PivotDiagnostician.INSTANCE.getObjectLabel(testInstance);
 		//
 		//	Check EObjectValidator errors
 		//
 		EValidator.Registry.INSTANCE.put(validatePackage, EObjectValidator.INSTANCE);
 		try {
 			String template = EcorePlugin.INSTANCE.getString("_UI_GenericConstraint_diagnostic");
 			checkValidationDiagnostics(testInstance, Diagnostic.ERROR,
 				NLS.bind(template,  "L1", objectLabel),
 				NLS.bind(template,  "L2a", objectLabel),
 	//BUG355184		NLS.bind(template,  "L2b", objectLabel),
 				NLS.bind(template,  "L3", objectLabel));
 			//
 			//	Check OCLinEcoreEObjectValidator warnings and distinct message
 			//
 			EValidator.Registry.INSTANCE.put(validatePackage, new OCLinEcoreEObjectValidator());
 			template = EvaluatorMessages.ValidationConstraintIsNotSatisfied_ERROR_;
 			checkValidationDiagnostics(testInstance, Diagnostic.WARNING,
 				NLS.bind(template,  "L1", objectLabel),
 				NLS.bind(template,  "L2a", objectLabel),
 	//BUG355184		NLS.bind(template,  "L2b", objectLabel),
 				NLS.bind(template,  "L3", objectLabel));
 			//
 			//	No errors
 			//
 			eSet(testInstance, "ref", "ok");
 			eSet(testInstance, "l1", "ok");
 			eSet(testInstance, "l2a", "ok");
 			eSet(testInstance, "l2b", "ok");
 			eSet(testInstance, "l3", "ok");
 			objectLabel = PivotDiagnostician.INSTANCE.getObjectLabel(testInstance);
 			checkValidationDiagnostics(testInstance, Diagnostic.WARNING);
 			//
 			//	Just one error
 			//
 			eSet(testInstance, "ref", "ok");
 			eSet(testInstance, "l1", "bad");
 			eSet(testInstance, "l2a", "ok");
 			eSet(testInstance, "l2b", "ok");
 			eSet(testInstance, "l3", "ok");
 			objectLabel = PivotDiagnostician.INSTANCE.getObjectLabel(testInstance);
 			checkValidationDiagnostics(testInstance, Diagnostic.WARNING,
 				NLS.bind(template,  "L1", objectLabel));
 		} finally {
 			EValidator.Registry.INSTANCE.remove(validatePackage);
 		}
 	}
 }
