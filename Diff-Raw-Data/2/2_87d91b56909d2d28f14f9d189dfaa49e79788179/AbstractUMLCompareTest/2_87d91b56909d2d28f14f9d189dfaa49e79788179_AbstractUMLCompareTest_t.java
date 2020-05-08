 /*******************************************************************************
  * Copyright (c) 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.uml2.diff.test;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.diff.merge.service.MergeService;
 import org.eclipse.emf.compare.diff.metamodel.AbstractDiffExtension;
 import org.eclipse.emf.compare.diff.metamodel.ComparisonResourceSetSnapshot;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffGroup;
 import org.eclipse.emf.compare.diff.metamodel.DiffModel;
 import org.eclipse.emf.compare.diff.metamodel.DiffPackage;
 import org.eclipse.emf.compare.diff.metamodel.DiffResourceSet;
 import org.eclipse.emf.compare.diff.service.DiffEngineRegistry;
 import org.eclipse.emf.compare.diff.service.DiffService;
 import org.eclipse.emf.compare.match.metamodel.MatchResourceSet;
 import org.eclipse.emf.compare.match.service.MatchEngineRegistry;
 import org.eclipse.emf.compare.match.service.MatchService;
 import org.eclipse.emf.compare.uml2.diff.UML2DiffEngine;
 import org.eclipse.emf.compare.uml2.match.UML2MatchEngine;
 import org.eclipse.emf.compare.util.ModelUtils;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 
 /**
  * AbstractUMLCompareTest.
  * 
  * @author Mickael Barbero <a href="mailto:mickael.barbero@obeo.fr">mickael.barbero@obeo.fr</a>
  */
 public abstract class AbstractUMLCompareTest {
 
 	/** MODIFIED_MODEL_UML. */
 	static final String MODIFIED_MODEL_UML = "/modified/model.uml"; //$NON-NLS-1$
 
 	/** ORIGINAL_MODEL_UML. */
 	static final String ORIGINAL_MODEL_UML = "/original/model.uml"; //$NON-NLS-1$
 
 	/** EXPECTED_EMFDIFF. */
 	static final String EXPECTED_EMFDIFF = "result.emfdiff"; //$NON-NLS-1$
 
 	/** EXPECTED_SUFFIX. */
 	static final String EXPECTED_SUFFIX = "/expected/result.emfdiff"; //$NON-NLS-1$
 
 	/** BACK_PATH. */
 	static final String BACK_PATH = ".."; //$NON-NLS-1$
 
 	private Set<ResourceSet> sets = new LinkedHashSet<ResourceSet>();
 
 	/**
 	 * Before treatment.
 	 */
 	@Before
 	public void before() {
 		DiffEngineRegistry.INSTANCE.putValue("uml", new UML2DiffEngine()); //$NON-NLS-1$
 		MatchEngineRegistry.INSTANCE.putValue("uml", new UML2MatchEngine()); //$NON-NLS-1$
 	}
 
 	/**
 	 * Get the path of the diagram.
 	 * 
 	 * @return The path.
 	 */
 	protected abstract String getDiagramKindPath();
 
 	/**
 	 * test Compare.
 	 * 
 	 * @param testFolderPath
 	 *            folder path.
 	 * @throws IOException
 	 *             exception.
 	 * @throws InterruptedException
 	 *             exception.
 	 */
 	protected final void testCompare(String testFolderPath) throws IOException, InterruptedException {
 		final DiffModel expectedDiff = firstNonEmptyDiffModel(getExpectedDiff(testFolderPath));
 		final DiffModel computedDiff = firstNonEmptyDiffModel(getComputedDiff(testFolderPath));
 
 		Assert.assertEquals(ModelUtils.serialize(expectedDiff), ModelUtils.serialize(computedDiff));
 	}
 
 	/**
 	 * test Compare 2 way.
 	 * 
 	 * @param testFolderPath
 	 *            folder path.
 	 * @throws IOException
 	 *             exception.
 	 * @throws InterruptedException
 	 *             exception.
 	 */
 	protected final void testCompare3Way(String testFolderPath) throws IOException, InterruptedException {
 		final DiffModel expectedDiff = firstNonEmptyDiffModel(getComputed3WayDiff(testFolderPath));
 		final DiffModel computedDiff = firstNonEmptyDiffModel(getComputed3WayDiff(testFolderPath));
 
 		Assert.assertEquals(ModelUtils.serialize(expectedDiff), ModelUtils.serialize(computedDiff));
 	}
 
 	/**
 	 * Find the first non empty difference model from a resource set.
 	 * 
 	 * @param diffResourceSet
 	 *            The resource set.
 	 * @return The difference model.
 	 */
 	private DiffModel firstNonEmptyDiffModel(DiffResourceSet diffResourceSet) {
 		for (DiffModel diffModel : diffResourceSet.getDiffModels()) {
 			if (diffModel.getOwnedElements().size() > 1
 					|| (diffModel.getOwnedElements().get(0) instanceof DiffGroup && !diffModel
 							.getOwnedElements().get(0).getSubDiffElements().isEmpty())) {
 				return diffModel;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Test merge.
 	 * 
 	 * @param testFolderPath
 	 *            the folder path.
 	 * @throws IOException
 	 *             exception.
 	 * @throws InterruptedException
 	 *             exception.
 	 */
 	protected final void testMerge(String testFolderPath) throws IOException, InterruptedException {
 		testMerge(testFolderPath, true);
 		testMerge(testFolderPath, false);
 	}
 
 	// protected final void testMerge(String testFolderPath, Class<? extends DiffElement> diffKind)
 	// throws IOException, InterruptedException {
 	// testMerge(testFolderPath, true, diffKind, expectedDifferencesCount);
 	// testMerge(testFolderPath, false, diffKind, expectedDifferencesCount);
 	// }
 
 	/**
 	 * Test merge.
 	 * 
 	 * @param testFolderPath
 	 *            the folder path.
 	 * @param leftToRight
 	 *            the direction of merge.
 	 * @throws IOException
 	 *             exception.
 	 * @throws InterruptedException
 	 *             exception.
 	 */
 	private void testMerge(String testFolderPath, boolean leftToRight) throws IOException,
 			InterruptedException {
 		final DiffResourceSet computedDiff = getComputedDiff(testFolderPath);
 		merge(leftToRight, computedDiff);
 		testMerge(testFolderPath, leftToRight, computedDiff);
 	}
 
 	/**
 	 * Test merge.
 	 * 
 	 * @param testFolderPath
 	 *            the folder path.
 	 * @param leftToRight
 	 *            the direction of merge.
 	 * @param diffKind
 	 *            The kind of difference.
 	 * @param expectedDifferencesCount
 	 *            the expected number of differences after merge.
 	 * @throws IOException
 	 *             exception.
 	 * @throws InterruptedException
 	 *             exception.
 	 */
 	protected void testMerge(String testFolderPath, boolean leftToRight,
 			Class<? extends DiffElement> diffKind, int expectedDifferencesCount) throws IOException,
 			InterruptedException {
 		final DiffResourceSet computedDiff = getComputedDiff(testFolderPath);
 		final Iterator<EObject> diffs = computedDiff.eAllContents();
 		while (diffs.hasNext()) {
 			final EObject next = diffs.next();
 			if (diffKind.isAssignableFrom(next.getClass())) {
 				merge(leftToRight, (DiffElement)next);
 				break;
 			}
 		}
 		if (expectedDifferencesCount == 0) {
 			testMerge(testFolderPath, leftToRight, computedDiff);
 		} else {
 			Assert.assertEquals(expectedDifferencesCount, countVisibleDiffs(computedDiff));
 		}
 	}
 
 	/**
 	 * Count the number of differences in the given difference resource set.
 	 * 
 	 * @param computedDiff
 	 *            The difference resource set.
 	 * @return The count.
 	 */
 	private int countVisibleDiffs(DiffResourceSet computedDiff) {
 		int result = 0;
 		final Iterator<EObject> diffs = computedDiff.eAllContents();
 		while (diffs.hasNext()) {
 			final EObject next = diffs.next();
 			if (next instanceof AbstractDiffExtension) {
 				result++;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Test merge.
 	 * 
 	 * @param testFolderPath
 	 *            the folder path.
 	 * @param leftToRight
 	 *            The direction of merge.
 	 * @param computedDiff
 	 *            The difference resource set.
 	 * @throws IOException
 	 *             exception.
 	 */
 	private void testMerge(String testFolderPath, boolean leftToRight, DiffResourceSet computedDiff)
 			throws IOException {
 		final String modifiedModelUmlUri = BACK_PATH + MODIFIED_MODEL_UML;
 		final String originalModelUmlUri = BACK_PATH + ORIGINAL_MODEL_UML;
 		Resource referenceResource;
 		Resource mergedResource;
 		if (leftToRight) {
 			final ResourceSet referenceResourceSet = getModelResourceSet(testFolderPath, MODIFIED_MODEL_UML);
 			referenceResource = referenceResourceSet.getResource(URI.createURI(modifiedModelUmlUri), true);
 
 			final ResourceSet mergedResourceSet = getRightResourceSet(computedDiff);
 			mergedResource = mergedResourceSet.getResource(URI.createURI(originalModelUmlUri), true);
 		} else {
 			final ResourceSet referenceResourceSet = getModelResourceSet(testFolderPath, ORIGINAL_MODEL_UML);
 			referenceResource = referenceResourceSet.getResource(URI.createURI(originalModelUmlUri), true);
 
 			final ResourceSet mergedResourceSet = getLeftResourceSet(computedDiff);
 			mergedResource = mergedResourceSet.getResource(URI.createURI(modifiedModelUmlUri), true);
 		}
 
 		Assert.assertEquals(referenceResource.getContents().size(), mergedResource.getContents().size());
 
 		Assert.assertEquals(ModelUtils.serialize(referenceResource.getContents().get(0)),
 				ModelUtils.serialize(mergedResource.getContents().get(0)));
 		cleanup(referenceResource.getResourceSet());
 		cleanup(mergedResource.getResourceSet());
 	}
 
 	private void cleanup(ResourceSet resourceSet) {
 		for (Resource res : resourceSet.getResources()) {
 			res.unload();
 		}
 		resourceSet.getResources().clear();
 	}
 
 	/**
 	 * Merge the difference resource set.
 	 * 
 	 * @param leftToRight
 	 *            direction of merge.
 	 * @param computedDiff
 	 *            the difference resource set.
 	 */
 	private void merge(boolean leftToRight, DiffResourceSet computedDiff) {
 		for (DiffModel diffModel : computedDiff.getDiffModels()) {
 			MergeService.merge(diffModel.getOwnedElements(), leftToRight);
 		}
 	}
 
 	/**
 	 * Merge the difference element.
 	 * 
 	 * @param leftToRight
 	 *            direction of merge.
 	 * @param computedDiff
 	 *            the difference element.
 	 */
 	private void merge(boolean leftToRight, DiffElement computedDiff) {
 		MergeService.merge(computedDiff, leftToRight);
 	}
 
 	/**
 	 * Get the left resource set from the difference resource set.
 	 * 
 	 * @param diffResourceSet
 	 *            the difference resource set.
 	 * @return the left resource set.
 	 */
 	private ResourceSet getLeftResourceSet(DiffResourceSet diffResourceSet) {
 		return getResourceSet(diffResourceSet, DiffPackage.Literals.DIFF_MODEL__LEFT_ROOTS);
 	}
 
 	/**
 	 * Get the right resource set from the difference resource set.
 	 * 
 	 * @param diffResourceSet
 	 *            the difference resource set.
 	 * @return the right resource set.
 	 */
 	private ResourceSet getRightResourceSet(DiffResourceSet diffResourceSet) {
 		return getResourceSet(diffResourceSet, DiffPackage.Literals.DIFF_MODEL__RIGHT_ROOTS);
 	}
 
 	/**
 	 * Get the resource set from the difference resource set and the expected side.
 	 * 
 	 * @param diffResourceSet
 	 *            The difference resource set.
 	 * @param sideRoots
 	 *            The expected side.
 	 * @return The resource set.
 	 */
 	@SuppressWarnings("unchecked")
 	private ResourceSet getResourceSet(DiffResourceSet diffResourceSet, EReference sideRoots) {
 		for (DiffModel diffModel : diffResourceSet.getDiffModels()) {
 			for (EObject element : (List<EObject>)diffModel.eGet(sideRoots)) {
 				if (element.eResource() != null && element.eResource().getResourceSet() != null) {
 					return element.eResource().getResourceSet();
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Compute the difference between two models in the given folder path and returns the difference resource
 	 * set result.
 	 * 
 	 * @param testFolderPath
 	 *            The folder path.
 	 * @return The difference resource set.
 	 * @throws IOException
 	 *             exception.
 	 * @throws InterruptedException
 	 *             exception.
 	 */
 	private DiffResourceSet getComputedDiff(String testFolderPath) throws IOException, InterruptedException {
 		final ResourceSet originalResourceSet = getModelResourceSet(testFolderPath, ORIGINAL_MODEL_UML);
 		final ResourceSet modifiedResourceSet = getModelResourceSet(testFolderPath, MODIFIED_MODEL_UML);
 
 		final Map<String, Object> matchOptions = new HashMap<String, Object>();
 
 		final MatchResourceSet computedMatch = MatchService.doResourceSetMatch(modifiedResourceSet,
 				originalResourceSet, matchOptions);
 		final DiffResourceSet computedDiff = DiffService.doDiff(computedMatch);
 
 		final ResourceSet computedResourceSet = createResourceSet();
 		final Resource computedResource = computedResourceSet.createResource(URI.createURI(EXPECTED_EMFDIFF));
 		computedResource.getContents().add(computedDiff);
 		return computedDiff;
 	}
 
 	protected ResourceSetImpl createResourceSet() {
 		ResourceSetImpl newSet = new ResourceSetImpl();
 		sets.add(newSet);
 		return newSet;
 	}
 
 	@After
 	public void cleanResourceSets() {
 		for (ResourceSet set : sets) {
 			cleanup(set);
 		}
 	}
 
 	/**
 	 * Compute the difference between three models in the given folder path and returns the difference
 	 * resource set result.
 	 * 
 	 * @param testFolderPath
 	 *            The folder path.
 	 * @return The difference resource set.
 	 * @throws IOException
 	 *             exception.
 	 * @throws InterruptedException
 	 *             exception.
 	 */
 	private DiffResourceSet getComputed3WayDiff(String testFolderPath) throws IOException,
 			InterruptedException {
 		final ResourceSet originalResourceSet = getModelResourceSet(testFolderPath, "/origin/model.uml"); //$NON-NLS-1$
 		final ResourceSet localResourceSet = getModelResourceSet(testFolderPath, "/local/model.uml"); //$NON-NLS-1$
 		final ResourceSet remoteResourceSet = getModelResourceSet(testFolderPath, "/remote/model.uml"); //$NON-NLS-1$
 
 		final Map<String, Object> matchOptions = new HashMap<String, Object>();
 
 		final MatchResourceSet computedMatch = MatchService.doResourceSetMatch(remoteResourceSet,
 				localResourceSet, originalResourceSet, matchOptions);
 		final DiffResourceSet computedDiff = DiffService.doDiff(computedMatch);
 
 		final ResourceSet computedResourceSet = createResourceSet();
 		final Resource computedResource = computedResourceSet.createResource(URI.createURI(EXPECTED_EMFDIFF));
 		computedResource.getContents().add(computedDiff);
 		return computedDiff;
 	}
 
 	/**
 	 * Get a model resource set from the folder path and model name.
 	 * 
 	 * @param testFolderPath
 	 *            the folder path.
 	 * @param model
 	 *            The model name.
 	 * @return The resource set.
 	 * @throws IOException
 	 *             exception.
 	 */
 	private ResourceSet getModelResourceSet(String testFolderPath, String model) throws IOException {
 		final ResourceSet resourceSet = createResourceSet();
 		final Resource original = resourceSet.createResource(URI.createURI(BACK_PATH + model));
 		original.load(
				this.getClass().getResourceAsStream(getDiagramKindPath() + testFolderPath
 						+ model), Collections.emptyMap());
 		return resourceSet;
 	}
 
 	/**
 	 * Get the difference resource set from the folder path.
 	 * 
 	 * @param testFolderPath
 	 *            The folder path.
 	 * @return The difference resource set.
 	 * @throws IOException
 	 *             exception.
 	 */
 	private DiffResourceSet getExpectedDiff(String testFolderPath) throws IOException {
 		final ResourceSet expectedResourceSet = createResourceSet();
 		final ComparisonResourceSetSnapshot expectedDiffSnapshot = (ComparisonResourceSetSnapshot)ModelUtils
 				.load(this.getClass().getResourceAsStream(
 						getDiagramKindPath() + testFolderPath + EXPECTED_SUFFIX), EXPECTED_EMFDIFF,
 						expectedResourceSet);
 		return expectedDiffSnapshot.getDiffResourceSet();
 	}
 }
