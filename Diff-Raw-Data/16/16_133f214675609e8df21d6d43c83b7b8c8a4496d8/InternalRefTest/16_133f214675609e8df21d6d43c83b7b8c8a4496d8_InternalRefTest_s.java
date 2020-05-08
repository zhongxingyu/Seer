 /*******************************************************************************
  * Copyright (c) 2010 Technical University of Denmark.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors:
  *    Patrick Koenemann, DTU Informatics - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.mpatch.test.junit;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.compare.mpatch.IndepAddRemElementChange;
 import org.eclipse.emf.compare.mpatch.IndepChange;
 import org.eclipse.emf.compare.mpatch.MPatchModel;
 import org.eclipse.emf.compare.mpatch.apply.generic.util.InternalReferencesTransformation;
 import org.eclipse.emf.compare.mpatch.apply.generic.util.MPatchDependencyTransformation;
 import org.eclipse.emf.compare.mpatch.common.util.ExtensionManager;
 import org.eclipse.emf.compare.mpatch.common.util.MPatchConstants;
 import org.eclipse.emf.compare.mpatch.descriptor.EMFModelDescriptor;
 import org.eclipse.emf.compare.mpatch.extension.IModelDescriptorCreator;
 import org.eclipse.emf.compare.mpatch.extension.ISymbolicReferenceCreator;
 import org.eclipse.emf.compare.mpatch.extension.MPatchApplicationResult;
 import org.eclipse.emf.compare.mpatch.extension.ResolvedSymbolicReferences;
 import org.eclipse.emf.compare.mpatch.symrefs.ElementSetReference;
 import org.eclipse.emf.compare.mpatch.symrefs.OclCondition;
 import org.eclipse.emf.compare.mpatch.test.util.CompareTestHelper;
 import org.eclipse.emf.compare.mpatch.test.util.TestConstants;
 import org.eclipse.emf.compare.mpatch.transform.util.GeneralizeTransformation;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.junit.Test;
 
 /**
  * The test first creates mpatches and then calls the following transformations:
  * <ol>
  * <li>dependency graph
  * <li>string weakening
  * <li>cardinality weakening
  * <li>internal reference replacement
  * </ol>
  * 
  * Then the mpatch is modified such that internal references are required to resolve correctly.
  * 
  * The modified changes are applied to a prepared model and the application result and the changed model are checked.
  * 
  * @author Patrick Koenemann (pk@imm.dtu.dk)
  */
 public class InternalRefTest {
 
 	/**
 	 * See test description here: {@link InternalRefTest}.
 	 */
 	@Test
 	public void testInternalRef() {
 		// get creators
 		final ISymbolicReferenceCreator symrefCreator = ExtensionManager.getAllSymbolicReferenceCreators().get(
 				"Condition-based");
 		final IModelDescriptorCreator descriptorCreator = ExtensionManager.getAllModelDescriptorCreators().get(
 				"Default");
 		final String info = "symrefCreator: " + symrefCreator.getLabel() + ", descriptorCreator: "
 				+ descriptorCreator.getLabel();
 
 		// create mpatch
 		final MPatchModel mpatch = CompareTestHelper.getMPatchFromUris(TestConstants.INTERNAL_REF_URI2,
 				TestConstants.INTERNAL_REF_URI1, symrefCreator, descriptorCreator);
 
 		doTransformations(mpatch);
 		modifyDiff(mpatch);
 
 		// resolve references to other model
 		final EPackage applyModel = (EPackage) CompareTestHelper.loadModel(TestConstants.INTERNAL_REF_URI3,
 				new ResourceSetImpl()).get(0);
 		final ResolvedSymbolicReferences resolvedReferences = CompareTestHelper.resolveReferences(mpatch, applyModel, info);
 
 		// apply differences
 		final MPatchApplicationResult result = TestConstants.DIFF_APPLIER.applyMPatch(resolvedReferences, true);
 
 		// check application status
 		assertTrue("Some changes failed to apply: " + result.failed, result.failed.isEmpty());
 		assertTrue("Cross reference restoring failed for: " + result.crossReferences, result.crossReferences.isEmpty());
 		assertEquals("Application result was not successful!", MPatchApplicationResult.ApplicationStatus.SUCCESSFUL,
 				result.status);
 
 		// check model!
 		assertEquals("Different number of elements in root package than expected: " + applyModel.getEClassifiers(), 3,
 				applyModel.getEClassifiers().size());
 		assertEquals("Different number of elements in sub package than expected: "
 				+ applyModel.getESubpackages().get(0).getEClassifiers(), 1, applyModel.getESubpackages().get(0)
 				.getEClassifiers().size());
 		for (EClassifier classifier : applyModel.getEClassifiers()) {
 			if (!classifier.getName().equals("C")) {
 				final EClass aClass = (EClass) classifier;
 				assertEquals("Cross references (eSuperType) does not refer to the two new classes 'C', but: "
 						+ aClass.getESuperTypes(), 2, aClass.getESuperTypes().size());
 			}
 		}
 	}
 
 	private void modifyDiff(MPatchModel mpatch) {
 		assertEquals("Number of changes doesn't match!", 3, mpatch.getChanges().size());
 
 		// get model descriptor of add class 'B' change
 		final EMFModelDescriptor subModel = getAddChangeModelDescriptor(mpatch.getChanges(), "B");
 		final EList<Object> attribute = subModel.getAttributes().get(EcorePackage.Literals.ENAMED_ELEMENT__NAME);
 		final Object name = attribute.get(0);
 		assertEquals("Attribute name differs!", "B", name);
 
 		// change to 'C' and verify
 		attribute.set(0, "C");
 		final Object newName = subModel.getAttributes().get(EcorePackage.Literals.ENAMED_ELEMENT__NAME).get(0);
 		assertEquals("Attribute name should have changed to 'C'!", "C", newName);
 
 		// get model descriptor of add class 'A2' change
 		final IndepChange addChange = (IndepChange) getAddChangeModelDescriptor(mpatch.getChanges(), "A2").eContainer();
 		final ElementSetReference corrReference = (ElementSetReference) addChange.getCorrespondingElement();
 		final OclCondition condition = (OclCondition) corrReference.getConditions().get(0);
 		condition.setExpression("name = 'root'"); // no weakening here!
 	}
 
 	private EMFModelDescriptor getAddChangeModelDescriptor(EList<IndepChange> changes, String name) {
 		for (IndepChange indepChange : changes) {
 			if (indepChange instanceof IndepAddRemElementChange) {
 				final EMFModelDescriptor subModel = (EMFModelDescriptor) ((IndepAddRemElementChange) indepChange)
 						.getSubModel();
 				final EList<Object> attr = subModel.getAttributes().get(EcorePackage.Literals.ENAMED_ELEMENT__NAME);
 				if (attr != null && attr.size() == 1 && name.equals(attr.get(0)))
 					return subModel;
 			}
 		}
 		fail("EClass named '" + name + "' not found in: " + changes);
 		return null;
 	}
 
 	private void doTransformations(MPatchModel mpatch) {
 		final int deps = MPatchDependencyTransformation.calculateDependencies(mpatch);
 		assertNull(MPatchConstants.MPATCH_SHORT_NAME + " is not valid!", CompareTestHelper.validateMPatch(mpatch));
 		assertEquals("Dependencies were not calculated correctly!", 2, deps);
 		final int refs = InternalReferencesTransformation.createInternalReferences(mpatch);
 		assertNull(MPatchConstants.MPATCH_SHORT_NAME + " is not valid!", CompareTestHelper.validateMPatch(mpatch));
 		assertEquals("Internal references were not created correctly!", 2, refs);
 		final int card = GeneralizeTransformation.unboundSymbolicReferences(mpatch);
 		assertNull(MPatchConstants.MPATCH_SHORT_NAME + " is not valid!", CompareTestHelper.validateMPatch(mpatch));
		assertEquals("Cardinality weakening was not performed correctly!", 5, card);
 		final int str = GeneralizeTransformation.expandScope(mpatch);
 		assertNull(MPatchConstants.MPATCH_SHORT_NAME + " is not valid!", CompareTestHelper.validateMPatch(mpatch));
 		assertTrue("String weakening was not performed correctly!", str >= 3);
 	}
 }
