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
 package org.eclipse.emf.compare.mpatch.test.util;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.compare.diff.metamodel.ComparisonResourceSnapshot;
 import org.eclipse.emf.compare.diff.metamodel.ComparisonSnapshot;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.mpatch.ChangeGroup;
 import org.eclipse.emf.compare.mpatch.IndepChange;
 import org.eclipse.emf.compare.mpatch.MPatchModel;
 import org.eclipse.emf.compare.mpatch.UnknownChange;
 import org.eclipse.emf.compare.mpatch.apply.generic.util.InternalReferencesTransformation;
 import org.eclipse.emf.compare.mpatch.apply.generic.util.MPatchDependencyTransformation;
 import org.eclipse.emf.compare.mpatch.apply.util.MPatchResolver;
 import org.eclipse.emf.compare.mpatch.apply.util.MPatchValidator;
 import org.eclipse.emf.compare.mpatch.common.util.CommonUtils;
 import org.eclipse.emf.compare.mpatch.emfdiff2mpatch.util.TransformationLauncher;
 import org.eclipse.emf.compare.mpatch.extension.IModelDescriptorCreator;
 import org.eclipse.emf.compare.mpatch.extension.ISymbolicReferenceCreator;
 import org.eclipse.emf.compare.mpatch.extension.MPatchApplicationResult;
 import org.eclipse.emf.compare.mpatch.extension.ResolvedSymbolicReferences;
 import org.eclipse.emf.compare.mpatch.extension.MPatchApplicationResult.ApplicationStatus;
 import org.eclipse.emf.compare.mpatch.extension.ResolvedSymbolicReferences.ValidationResult;
 import org.eclipse.emf.compare.mpatch.symrefs.OclCondition;
 import org.eclipse.emf.compare.mpatch.symrefs.SymrefsPackage;
 import org.eclipse.emf.compare.mpatch.symrefs.util.OCLConditionHelper;
 import org.eclipse.emf.compare.mpatch.transform.util.GroupingTransformation;
 import org.eclipse.emf.compare.mpatch.util.ExtEcoreUtils;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.Diagnostician;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.query.conditions.eobjects.EObjectCondition;
 
 /**
  * Common test operations (test logic) for multiple test cases.
  * 
  * @author Patrick Koenemann (pk@imm.dtu.dk)
  * 
  */
 public class CommonTestOperations {
 
 	/**
 	 * This performs the following test cycle for a given model:
 	 * <ol>
 	 * <li>Compare the changed with the unchanged version using emf compare
 	 * <li>Transform the emfdiff to mpatch
 	 * <li>Compute dependency graph for the mpatch
 	 * <li>Resolve mpatch to unchanged version of the model
 	 * <li>Apply mpatch to unchanged version of the model
 	 * <li>Use emf compare to calculate differences between changed version of the model from the parameters and the
 	 * model version to which the differences have been applied
 	 * <li>The latter comparison must not differ! If it does, the test fails and the differences are given in the assert
 	 * log.
 	 * </ol>
 	 * 
 	 * @param unchanged
 	 *            The URI of the unchanged version of a model.
 	 * @param changed
 	 *            The URI of the changed version of a model.
 	 * @param symrefCreator
 	 *            The symbolic reference creator used for this transformation.
 	 * @param descriptorCreator
 	 *            The model descriptor creator used for this transformation.
 	 */
 	public static void applyMPatchAndValidate(String unchanged, String changed,
 			ISymbolicReferenceCreator symrefCreator, IModelDescriptorCreator descriptorCreator) {
 		final String info = "symrefCreator: " + symrefCreator.getLabel() + ", descriptorCreator: "
 				+ descriptorCreator.getLabel();
 
 		// prepare models
 		final MPatchModel mpatch = createMPatch(unchanged, changed, symrefCreator, descriptorCreator, info);
 
 		ResourceSet resourceSet = new ResourceSetImpl(); // get new resource to not conflict with original models!
 		final EObject rightModel = CompareTestHelper.loadModel(unchanged, resourceSet).get(0);
 		final EObject leftModel = CompareTestHelper.loadModel(changed, resourceSet).get(0);
 
 		// apply the differences and validate the result against the leftModel
 		createAndApplyMPatch(mpatch, rightModel, leftModel, null, info);
 	}
 
 	/**
 	 * Create MPatch from URIs and check that it is not <code>null</code>. No other checks are performed.
 	 * 
 	 * @param unchanged
 	 *            The URI of the unchanged version of a model.
 	 * @param changed
 	 *            The URI of the changed version of a model.
 	 * @param symrefCreator
 	 *            The symbolic reference creator used for this transformation.
 	 * @param descriptorCreator
 	 *            The model descriptor creator used for this transformation.
 	 * @param info
 	 *            Additional information to print in assert messages.
 	 * @return The calculated MPatch.
 	 */
 	public static MPatchModel createMPatch(String unchanged, String changed, ISymbolicReferenceCreator symrefCreator,
 			IModelDescriptorCreator descriptorCreator, String info) {
 
 		// prepare models
 		final MPatchModel mpatch = CompareTestHelper.getMPatchFromUris(changed, unchanged, symrefCreator,
 				descriptorCreator);
 		assertNotNull("Preceeding transformation failed! Make sure mpatch can be produced for: " + unchanged + " and "
 				+ changed + " and " + info, mpatch);
 		return mpatch;
 	}
 
 	/**
 	 * Check the application of differences by comparing the result with a given model, usually the changed version from
 	 * which the mpatch was created from. There should be no differences between these two - if there are, then the test
 	 * fails and the differences are given in the assert log message.
 	 * 
 	 * (So this is the self-checking part of the test case, to make Fowler happy :o) [Refactoring, Fowler])
 	 * 
 	 * @param appliedModel
 	 *            A model to which differences have been applied.
 	 * @param originalChangedModel
 	 *            The changed version of the model from which the differences have been created.
 	 */
 	public static void checkAppliedModel(EObject appliedModel, EObject originalChangedModel, String info) {
 
 		// compare with target model and check whether they differ
 		final ComparisonSnapshot differences = CommonUtils.createEmfdiff(appliedModel, originalChangedModel, false);
 		final Collection<DiffElement> violations = CommonUtils.analyzeDiff(differences, CommonUtils.DIFF_ORDERINGS);
 		assertTrue("difference application was not successful! " + info + " - Violations: " + violations,
 				violations.isEmpty());
 	}
 
 	/**
 	 * The following steps are performed here:
 	 * <ol>
 	 * <li>Group the given mpatch
 	 * <li>Add dependency graph to the given mpatch
 	 * <li>Perform initial symbolic reference resolution of the given mpatch to the model given in the parameters
 	 * <li>Copy the given model and apply all differences on the given model.
 	 * <li>Compare the changed model with the copy using EMF Compare
 	 * <li>Return that {@link ComparisonSnapshot} created by EMF Compare which contains a review of all applied
 	 * differences
 	 * </ol>
 	 * 
 	 * Optionally, each step can be measured in time - if you don't want to measure the time, set the parameter to
 	 * <code>null</code>. The current system time is put into a field in the array:
 	 * <ol start="0">
 	 * <li>Starting time
 	 * <li>Time after grouping
 	 * <li>Time after difference calculation
 	 * <li>Time after symbolic reference resolution
 	 * <li>Time after creating a copy of the given model
 	 * <li>Time after difference application
 	 * <li>Time after comparing the model copy with the changed model
 	 * </ol>
 	 * So make sure the given array has at least length 7!
 	 * 
 	 * @param mpatch
 	 *            Ungrouped (!) mpatch.
 	 * @param model
 	 *            A model to which the mpatch should be applied.
 	 * @param expectedModel
 	 *            The expected result of the difference application.
 	 * @param times
 	 *            An array in which the times are stored. May be <code>null</code>.
 	 * @param info
 	 *            Show additional information about the test case in the assert message, e.g. the use of particular
 	 *            creators for the transformation.
 	 * @return A review of all applied changes.
 	 */
 	public static ComparisonSnapshot createAndApplyMPatch(MPatchModel mpatch, EObject model, EObject expectedModel,
 			PerformanceTimes times, String info) {
 
 		if (times != null)
 			times.tick(); // start timing
 
 		doTransformations(mpatch, times, info);
 		final ResolvedSymbolicReferences resolved = resolveAndValidate(mpatch, model, times, info);
 		final EObject newModel = applyMPatchToModel(mpatch, model, resolved, times, info);
 		final ComparisonResourceSnapshot emfdiff = compareChangedAndUnchangedModels(model, newModel, times, info);
 		checkAppliedModel(model, expectedModel, info);
 		checkBinding(resolved.getMPatchModelBinding(), info);
 
 		if (times != null)
 			times.setCheck(); // final check
 
 		return emfdiff;
 	}
 
 	/**
 	 * Validate an eObject using the EMF Validation Framework ({@link Diagnostician}).
 	 * 
 	 * @param eObject
 	 *            An eobject to validate.
 	 * @param info
 	 *            Some addition information for the assert messages.
 	 */
 	public static void checkBinding(EObject eObject, String info) {
 		// validate the given element using the emf validation framework!
 		final Diagnostic diagnostic = Diagnostician.INSTANCE.validate(eObject);
 		assertNull("Validation was not successful! (" + info + "): " + eObject, diagnostic.getException());
 		assertEquals("Validation was not successful! (" + info + "): " + eObject + "\n" + getMessage(diagnostic),
 				Diagnostic.OK, diagnostic.getSeverity());
 	}
 
 	/**
 	 * Compare the two given models and return their differences as an emfdiff.
 	 * 
 	 * @param changedModel
 	 *            The changed version of a model.
 	 * @param unchangedModel
 	 *            The unchanged version of the model.
 	 * @param times
 	 *            For performance check (optional, can be <code>null</code>).
 	 * @param info
 	 *            Additional information for assert Strings.
 	 * @return An emfdiff containing all differences between these two model versions.
 	 */
 	public static ComparisonResourceSnapshot compareChangedAndUnchangedModels(EObject changedModel,
 			EObject unchangedModel, PerformanceTimes times, String info) {
 		// run emf compare again to return review all applied differences
 		final ComparisonResourceSnapshot emfdiff = CommonUtils.createEmfdiff(changedModel, unchangedModel, false);
 		if (times != null)
 			times.setChanges(); // final comparison
 
 		// check the modified source model
 		final EObject appliedModel = CommonUtils.getModelFromEmfdiff(emfdiff, true); // get left model
 		assertNotNull("Model with applied diff must not be null! (" + info + ")", appliedModel);
 		assertEquals("The model to which we applied the diff must be our original model! (" + info + ")", changedModel,
 				appliedModel);
 		return emfdiff;
 	}
 
 	/**
 	 * Create a copy of the given model and apply the MPatch to it. Then, check whether the application was successful
 	 * by check the {@link MPatchApplicationResult}.
 	 * 
 	 * @param mpatch
 	 *            The MPatch to apply.
 	 * @param model
 	 *            The model to which the MPatch should be applied.
 	 * @param resolved
 	 *            The resolution of symbolic references.
 	 * @param times
 	 *            For performance check (optional, can be <code>null</code>).
 	 * @param info
 	 *            Additional information for assert Strings.
 	 * @return A copy of original <code>model</code>. Note that the MPatch was applied to the parameter
 	 *         <code>model</code>!
 	 */
 	public static EObject applyMPatchToModel(MPatchModel mpatch, EObject model, ResolvedSymbolicReferences resolved,
 			PerformanceTimes times, String info) {
 		// create a new target model - a copy as the 'right' model, whereas the original is the 'left' model
 		final EObject newModel = EcoreUtil.copy(model);
 		final ResourceImpl newModelResource = new ResourceImpl();
 		newModelResource.getContents().add(newModel);
 		newModelResource.setURI(model.eResource().getURI());
 		if (times != null)
 			times.setCopy(); // model copy time
 
 		// apply differences *with* calculating the binding!
 		final MPatchApplicationResult result = TestConstants.DIFF_APPLIER.applyMPatch(resolved, true);
 		if (times != null)
 			times.setApply(); // diff application time
 
 		// since we apply the diff to the unchanged model, we require all changes to be applied successfully!
 		assertTrue("There are changes which failed (" + info + "): " + result.failed, result.failed.isEmpty());
 		assertTrue("There are changes with unsufficient cross-reference restore (" + info + "): "
 				+ result.crossReferences, result.crossReferences.isEmpty());
 		assertEquals("Diff application result is not successful (" + info + ")!", ApplicationStatus.SUCCESSFUL,
 				result.status);
 		return newModel;
 	}
 
 	/**
 	 * Resolve the given MPatch to the given model and validate the resolution using the {@link MPatchValidator}.
 	 * 
 	 * @param mpatch
 	 *            The MPatch to resolve.
 	 * @param model
 	 *            The model to which the MPatch should be resolved.
 	 * @param times
 	 *            For performance check (optional, can be <code>null</code>).
 	 * @param info
 	 *            Additional information for assert Strings.
 	 * @return The resolution.
 	 */
 	public static ResolvedSymbolicReferences resolveAndValidate(MPatchModel mpatch, EObject model,
 			PerformanceTimes times, String info) {
 		// resolve symbolic references
 		final ResolvedSymbolicReferences resolved = MPatchResolver.resolveSymbolicReferences(mpatch, model,
 				ResolvedSymbolicReferences.RESOLVE_UNCHANGED);
 		if (times != null)
 			times.setResolve(); // resolution time
 
 		// validate all resolved references
 		final List<IndepChange> invalidResolution = MPatchValidator.validateResolutions(resolved);
 		assertTrue("The following changes did not resolve correctly (" + info + "): " + invalidResolution,
 				invalidResolution.isEmpty());
 		if (times != null)
 			times.setValidate(); // validation time
 		return resolved;
 	}
 
 	/**
 	 * Perform the following transformations:
 	 * <ul>
 	 * <li>Grouping (and check that there is at least one group)
 	 * <li>Dependency calculation
 	 * <li>Internal Reference Creation
 	 * </ul>
 	 * 
 	 * @param mpatch
 	 *            The MPatch to transform.
 	 * @param times
 	 *            For performance check (optional, can be <code>null</code>).
 	 * @param info
 	 *            Additional information for assert Strings.
 	 */
 	public static void doTransformations(MPatchModel mpatch, PerformanceTimes times, String info) {
 		// restructure differences, add dependencies, and replace internal references
 		try {
 			final int groups = GroupingTransformation.group(mpatch);
 			if (times != null)
 				times.setGroups(); // grouping time
 			assertTrue("We need at least one group after restructuring!", groups > 0);
 
 			MPatchDependencyTransformation.calculateDependencies(mpatch);
 			if (times != null)
 				times.setDeps(); // dependency calculation time
 
 			InternalReferencesTransformation.createInternalReferences(mpatch);
 			if (times != null)
 				times.setRefs(); // internal reference creation time
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail("Grouping or dependency calculation failed (" + info + "): " + e.getMessage());
 		}
 	}
 
 	private static String getMessage(Diagnostic diagnostic) {
 		String msg = diagnostic.getMessage();
 		for (Diagnostic d : diagnostic.getChildren()) {
 			msg += "\n" + d.getMessage();
 		}
 		return msg;
 	}
 
 	/**
 	 * Check whether <code>outModel</code> contains an mpatch and that is has <code>count</code> (ungrouped) changes in
 	 * total.
 	 * 
 	 * @param mpatch
 	 *            It is supposed to be the result of {@link TransformationLauncher#transform(List, StringBuffer)}.
 	 * @param count
 	 *            The expected number of changed which is contained in the result.
 	 */
 	public static void checkForDifferences(MPatchModel mpatch, int count) {
 		// ignore unknown changes and groups here!
 		int counter = 0;
 		for (IndepChange change : mpatch.getChanges()) {
 			if (!(change instanceof UnknownChange || change instanceof ChangeGroup)) {
 				counter++;
 			}
 		}
 		assertEquals("MPatch does not contain " + count + " elements as expected!", count, counter);
 	}
 
 	/**
 	 * This first performs an initial resolution of symbolic references in the given <code>diff</code>. Then, the
 	 * MPatchValidator is used to check whether all symbolic references resolved correctly and whether the state before
 	 * the changes can be found in the given model.
 	 * 
 	 * @param mpatch
 	 *            MPatch.
 	 * @param model
 	 *            A model for which the reference resolution should be performed.
 	 */
 	public static void validateSymbolicReferenceResolution(MPatchModel mpatch, EObject model) {
 		try {
 			// resolve symbolic references
 			ResolvedSymbolicReferences mapping = MPatchResolver.resolveSymbolicReferences(mpatch, model,
 					ResolvedSymbolicReferences.RESOLVE_UNCHANGED);
 			assertNotNull("Result of symbolic reference resolution must not be null!", mapping);
 
 			// validate resolution
 			final List<IndepChange> invalid = MPatchValidator.validateResolutions(mapping);
 			final List<IndepChange> refs = CommonUtils.filterByValue(mapping.getValidation(),
 					ValidationResult.REFERENCE);
 			final List<IndepChange> applied = CommonUtils.filterByValue(mapping.getValidation(),
 					ValidationResult.STATE_AFTER);
 			final List<IndepChange> state = CommonUtils.filterByValue(mapping.getValidation(),
 					ValidationResult.STATE_INVALID);
 			if (!invalid.isEmpty()) {
 				final String msg = "Resolution fail!%\nAlready applied: %s%\nInvalid state: %s%\nNot resolved: %s%\nOther reasons: %s";
 				invalid.removeAll(refs);
 				invalid.removeAll(applied);
 				invalid.removeAll(state);
 				fail(String.format(msg, applied.toString(), state.toString(), refs.toString(), invalid.toString()));
 			}
 
 		} catch (Exception e) {
 			fail("Symbolic reference resolution failed: " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Perform grouping on the given mpatch and compare the resulting number of groups with the given parameter.
 	 * 
 	 * @param mpatch
 	 *            Ungrouped (!) mpatch.
 	 * @param count
 	 *            The expected number of groups created.
 	 */
 	public static void groupingAndCheckForGroups(MPatchModel mpatch, int count) {
 		try {
 			final int groups = GroupingTransformation.group(mpatch);
 			assertTrue("Number of groups does not match!", count == groups || count + 1 == groups);
 
 			String diagnostic = CompareTestHelper.validateMPatch(mpatch);
 			assertNull(diagnostic, diagnostic);
 		} catch (final Exception e) {
 			fail("Restructuring failed. Cause: " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Check the validity of all ocl expressions by parsing them.
 	 * 
 	 * @param mpatch
 	 *            MPatch.
 	 */
 	public static void checkOclExpressions(MPatchModel mpatch) {
 
 		// get all ocl conditions from the mpatch
 		final List<EObject> oclConditions = ExtEcoreUtils.collectTypedElements(mpatch.getChanges(),
 				Collections.singleton(SymrefsPackage.Literals.OCL_CONDITION), true);
 
 		// iterate over them
 		for (EObject eObject : oclConditions) {
 			if (eObject instanceof OclCondition) {
 				OclCondition oclCondition = (OclCondition) eObject;
 
 				// check whether we can create a condition for them (i.e. the condition is parseable)
 				final EObjectCondition condition = OCLConditionHelper.getWhereCondition(oclCondition);
				if (condition == null)
					OCLConditionHelper.getWhereCondition(oclCondition);
 				assertNotNull("The OCL expression could not be processed: " + oclCondition.getExpression(), condition);
 
 			} else
 				fail("If you see this message, ExtEcoreUtils.collectTypedElements does not work correctly!");
 		}
 	}
 
 	/**
 	 * Test the resolutino of symbolic references by first creating the emfdiff and then mpatch of the two given models,
 	 * and then trying to resolve all symbolic references on the unchanged version of the given models. Hence, this must
 	 * be unique!
 	 * 
 	 * @param unchangedUri
 	 *            The unchanged version of the test model.
 	 * @param changedUri
 	 *            The changed version of the test model.
 	 * @param symrefCreator
 	 *            The symbolic reference creator for the transformation.
 	 * @param descriptorCreator
 	 *            The symbolic reference creator for the transformation.
 	 */
 	public static void checkSymbolicReferenceResolution(String unchangedUri, String changedUri,
 			ISymbolicReferenceCreator symrefCreator, IModelDescriptorCreator descriptorCreator) {
 		// prepare models
 		final MPatchModel mpatch = CompareTestHelper.getMPatchFromUris(changedUri, unchangedUri, symrefCreator,
 				descriptorCreator);
 		assertNotNull("Preceeding transformation emfdiff2mpatch failed with symrefCreator: " + symrefCreator.getLabel()
 				+ " and descriptorCreator: " + descriptorCreator.getLabel(), mpatch);
 
 		// we need to introduce internal references here!
 		InternalReferencesTransformation.createInternalReferences(mpatch);
 
 		ResourceSet resourceSet = new ResourceSetImpl(); // get new resource to not conflict with original models!
 		final EObject rightModel = CompareTestHelper.loadModel(unchangedUri, resourceSet).get(0);
 
 		// the real check whether everything was correctly resolved
 		validateSymbolicReferenceResolution(mpatch, rightModel);
 	}
 
 }
