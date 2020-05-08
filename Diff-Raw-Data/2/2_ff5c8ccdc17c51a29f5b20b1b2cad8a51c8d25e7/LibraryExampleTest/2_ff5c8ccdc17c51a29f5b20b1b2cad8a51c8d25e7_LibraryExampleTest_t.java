 /*******************************************************************************
  * Copyright (c) 2010, 2011 Technical University of Denmark.
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
 
 import org.eclipse.emf.compare.mpatch.MPatchModel;
 import org.eclipse.emf.compare.mpatch.apply.generic.util.InternalReferencesTransformation;
 import org.eclipse.emf.compare.mpatch.apply.generic.util.MPatchDependencyTransformation;
 import org.eclipse.emf.compare.mpatch.common.util.ExtensionManager;
 import org.eclipse.emf.compare.mpatch.common.util.MPatchConstants;
 import org.eclipse.emf.compare.mpatch.extension.IModelDescriptorCreator;
 import org.eclipse.emf.compare.mpatch.extension.ISymbolicReferenceCreator;
 import org.eclipse.emf.compare.mpatch.extension.MPatchApplicationResult;
 import org.eclipse.emf.compare.mpatch.extension.ResolvedSymbolicReferences;
 import org.eclipse.emf.compare.mpatch.test.util.CompareTestHelper;
 import org.eclipse.emf.compare.mpatch.test.util.TestConstants;
 import org.eclipse.emf.compare.mpatch.transform.util.GeneralizeTransformation;
 import org.eclipse.emf.compare.mpatch.transform.util.GroupingTransformation;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.junit.Test;
 
 /**
  * Testing the library example:
  * <ol>
  * <li> Compare library.ecore and library_karl.ecore with EMF Compare
  * <li> Create MPatch (condition-based only)
  * <li> Perform all available transformations
  * <li> Apply all changes to library_eve.ecore
  * </ol>
  * 
  * @author Patrick Koenemann (pk@imm.dtu.dk)
  */
 public class LibraryExampleTest {
 
 	/**
 	 * Test description: {@link LibraryExampleTest}
 	 */
 	@Test
 	public void testLibraryExample() {
 		// get creators
 		final ISymbolicReferenceCreator symrefCreator = ExtensionManager.getAllSymbolicReferenceCreators().get(
 				"Condition-based");
 		final IModelDescriptorCreator descriptorCreator = ExtensionManager.getAllModelDescriptorCreators().get(
 				"Default");
 		final String info = "symrefCreator: " + symrefCreator.getLabel() + ", descriptorCreator: "
 				+ descriptorCreator.getLabel();
 
 		// create mpatch
 		final MPatchModel mpatch = CompareTestHelper.getMPatchFromUris(TestConstants.LIBRARY_URI2,
 				TestConstants.LIBRARY_URI1, symrefCreator, descriptorCreator);
 
 		doTransformations(mpatch);
 
 		// resolve references to other model
 		final EPackage applyModel = (EPackage) CompareTestHelper.loadModel(TestConstants.LIBRARY_URI3,
 				new ResourceSetImpl()).get(0);
 		final ResolvedSymbolicReferences resolvedReferences = CompareTestHelper.resolveReferences(mpatch, applyModel, info);
 
 		// apply differences
 		final MPatchApplicationResult result = TestConstants.DIFF_APPLIER.applyMPatch(resolvedReferences, true);
 
 		// check application status
 		assertTrue("Some changes failed to apply: " + result.failed, result.failed.isEmpty());
 		assertTrue("Cross reference restoring failed for: " + result.crossReferences, result.crossReferences.isEmpty());
 		assertEquals("Application result was not successful!", MPatchApplicationResult.ApplicationStatus.SUCCESSFUL,
 				result.status);
 	}
 
 	private void doTransformations(MPatchModel mpatch) {
 		final int groups = GroupingTransformation.group(mpatch);
 		assertNull(MPatchConstants.MPATCH_SHORT_NAME + " is not valid!", CompareTestHelper.validateMPatch(mpatch));
		assertTrue("Groups were not created correctly!", groups > 0);
 		final int deps = MPatchDependencyTransformation.calculateDependencies(mpatch);
 		assertNull(MPatchConstants.MPATCH_SHORT_NAME + " is not valid!", CompareTestHelper.validateMPatch(mpatch));
 		assertEquals("Dependencies were not calculated correctly!", 4, deps);
 		final int refs = InternalReferencesTransformation.createInternalReferences(mpatch);
 		assertNull(MPatchConstants.MPATCH_SHORT_NAME + " is not valid!", CompareTestHelper.validateMPatch(mpatch));
 		assertEquals("Internal references were not created correctly!", 6, refs);
 		final int card = GeneralizeTransformation.unboundSymbolicReferences(mpatch);
 		assertNull(MPatchConstants.MPATCH_SHORT_NAME + " is not valid!", CompareTestHelper.validateMPatch(mpatch));
 		assertEquals("Cardinality weakening was not performed correctly!", 16, card);
 		final int str = GeneralizeTransformation.expandScope(mpatch);
 		assertNull(MPatchConstants.MPATCH_SHORT_NAME + " is not valid!", CompareTestHelper.validateMPatch(mpatch));
 		assertTrue("String weakening was not performed correctly!", str >= 12);
 	}
 }
