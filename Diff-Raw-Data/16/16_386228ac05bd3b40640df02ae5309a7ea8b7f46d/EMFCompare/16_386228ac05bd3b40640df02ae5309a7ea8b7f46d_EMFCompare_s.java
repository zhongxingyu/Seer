 /*******************************************************************************
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare;
 
 import java.util.Iterator;
 import java.util.Set;
 
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.compare.conflict.DefaultConflictDetector;
 import org.eclipse.emf.compare.conflict.IConflictDetector;
 import org.eclipse.emf.compare.diff.DefaultDiffEngine;
 import org.eclipse.emf.compare.diff.DiffBuilder;
 import org.eclipse.emf.compare.diff.IDiffEngine;
 import org.eclipse.emf.compare.diff.IDiffProcessor;
 import org.eclipse.emf.compare.equi.DefaultEquiEngine;
 import org.eclipse.emf.compare.equi.IEquiEngine;
 import org.eclipse.emf.compare.extension.EMFCompareExtensionRegistry;
 import org.eclipse.emf.compare.extension.IPostProcessor;
 import org.eclipse.emf.compare.extension.PostProcessorDescriptor;
 import org.eclipse.emf.compare.match.DefaultMatchEngine;
 import org.eclipse.emf.compare.match.IMatchEngine;
 import org.eclipse.emf.compare.req.DefaultReqEngine;
 import org.eclipse.emf.compare.req.IReqEngine;
 import org.eclipse.emf.compare.scope.DefaultComparisonScope;
 import org.eclipse.emf.compare.scope.IComparisonScope;
 
 // FIXME progress monitor!
 /**
  * This class provides various utility methods that can be used to call EMF Compare on various notifiers.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public final class EMFCompare {
 	/**
 	 * This class does not need to be instantiated.
 	 */
 	private EMFCompare() {
 		// hides default constructor
 	}
 
 	/**
 	 * Launch a two-way comparison on the two given {@link Notifier}s and their direct children according to
 	 * the semantics of the {@link DefaultComparisonScope}. The result of this comparison will be returned in
 	 * the form of a {@link Comparison} instance.
 	 * 
 	 * @param left
 	 *            The notifier that is to be consider as the left side of this comparison.
 	 * @param right
 	 *            The notifier that is to be consider as the right side of this comparison.
 	 * @return The result of the two-way comparison of these two notifiers.
 	 */
 	public static Comparison compare(Notifier left, Notifier right) {
 		return compare(left, right, null, EMFCompareConfiguration.builder().build());
 	}
 
 	/**
 	 * Launch a two-way comparison on the two given {@link Notifier}s and their direct children according to
 	 * the semantics of the {@link DefaultComparisonScope}. The result of this comparison will be returned in
 	 * the form of a {@link Comparison} instance.
 	 * 
 	 * @param left
 	 *            The notifier that is to be consider as the left side of this comparison.
 	 * @param right
 	 *            The notifier that is to be consider as the right side of this comparison.
 	 * @param configuration
 	 *            The configuration object from which compare engines will be configured.
 	 * @return The result of the two-way comparison of these two notifiers.
 	 */
 	public static Comparison compare(Notifier left, Notifier right, EMFCompareConfiguration configuration) {
 		return compare(left, right, null, configuration);
 	}
 
 	/**
 	 * According to the value of <code>origin</code>, this will launch either a two-way or a three-way
 	 * comparison on the given {@link Notifier}s and their direct content (according to the semantics of the
 	 * {@link DefaultComparisonScope}). The result of this comparison will be returned in the form of a
 	 * {@link Comparison} instance.
 	 * 
 	 * @param left
 	 *            The notifier that is to be consider as the left side of this comparison.
 	 * @param right
 	 *            The notifier that is to be consider as the right side of this comparison.
 	 * @param origin
 	 *            The notifier that should be considered as the common ancestor of <code>left</code> and
 	 *            <code>right</code>. If <code>null</code>, a two-way comparison will be performed instead.
 	 * @return The result of the three-way comparison of these notifiers if <code>origin</code> is not
 	 *         <code>null</code>, the result of the two-way comparison of <code>left</code> and
 	 *         <code>right</code> otherwise.
 	 */
 	public static Comparison compare(Notifier left, Notifier right, Notifier origin) {
 		return compare(left, right, origin, EMFCompareConfiguration.builder().build());
 	}
 
 	/**
 	 * According to the value of <code>origin</code>, this will launch either a two-way or a three-way
 	 * comparison on the given {@link Notifier}s and their direct content (according to the semantics of the
 	 * {@link DefaultComparisonScope}). The result of this comparison will be returned in the form of a
 	 * {@link Comparison} instance.
 	 * 
 	 * @param left
 	 *            The notifier that is to be consider as the left side of this comparison.
 	 * @param right
 	 *            The notifier that is to be consider as the right side of this comparison.
 	 * @param origin
 	 *            The notifier that should be considered as the common ancestor of <code>left</code> and
 	 *            <code>right</code>. If <code>null</code>, a two-way comparison will be performed instead.
 	 * @param configuration
 	 *            The configuration object from which compare engines will be configured.
 	 * @return The result of the three-way comparison of these notifiers if <code>origin</code> is not
 	 *         <code>null</code>, the result of the two-way comparison of <code>left</code> and
 	 *         <code>right</code> otherwise.
 	 */
 	public static Comparison compare(Notifier left, Notifier right, Notifier origin,
 			EMFCompareConfiguration configuration) {
 		final IComparisonScope scope = new DefaultComparisonScope(left, right, origin);
 
 		return compare(scope, configuration);
 	}
 
 	/**
 	 * Launches the comparison for the given comparison scope.
 	 * 
 	 * @param scope
 	 *            The scope on which a comparison is to be performed.
 	 * @return The result of this comparison.
 	 */
 	public static Comparison compare(IComparisonScope scope) {
 		return compare(scope, EMFCompareConfiguration.builder().build());
 	}
 
 	/**
 	 * Launches the comparison for the given comparison scope.
 	 * 
 	 * @param scope
 	 *            The scope on which a comparison is to be performed.
 	 * @param configuration
 	 *            The configuration object from which compare engines will be configured.
 	 * @return The result of this comparison.
 	 */
 	public static Comparison compare(IComparisonScope scope, EMFCompareConfiguration configuration) {
 
 		// TODO allow extension of the default match engine
 		final IMatchEngine matchEngine = new DefaultMatchEngine();
 		Comparison comparison = matchEngine.match(scope, configuration);
 
 		IPostProcessor postProcessor = getPostProcessor(scope);
 		if (postProcessor != null) {
 			postProcessor.postMatch(comparison);
 		}
 
 		final IDiffProcessor diffBuilder = new DiffBuilder();
 
 		// TODO allow extension of the default diff engine
 		final IDiffEngine diffEngine = new DefaultDiffEngine(diffBuilder);
 		diffEngine.diff(comparison);
 
 		if (postProcessor != null) {
 			postProcessor.postDiff(comparison);
 		}
 
 		final IReqEngine reqEngine = new DefaultReqEngine();
 		reqEngine.computeRequirements(comparison);
 
 		if (postProcessor != null) {
 			postProcessor.postRequirements(comparison);
 		}
 
 		final IEquiEngine equiEngine = new DefaultEquiEngine();
 		equiEngine.computeEquivalences(comparison);
 
 		if (postProcessor != null) {
 			postProcessor.postEquivalences(comparison);
 		}
 
 		if (comparison.isThreeWay()) {
 			final IConflictDetector conflictDetector = new DefaultConflictDetector();
			conflictDetector.detect(comparison);
 
 			if (postProcessor != null) {
 				postProcessor.postConflicts(comparison);
 			}
 		}
 
 		return comparison;
 	}
 
 	/**
 	 * Retrieve the post processor from a given <code>scope</code>. The scope provides the set of the scanned
 	 * namespace and resource uris. If one of them matches with the regex of a
 	 * "org.eclipse.emf.compare.postProcessor" extension point, then the related post processor is returned.
 	 * 
 	 * @param scope
 	 *            The given scope.
 	 * @return The post processor.
 	 */
 	private static IPostProcessor getPostProcessor(IComparisonScope scope) {
 		IPostProcessor postProcessor = null;
 		final Iterator<PostProcessorDescriptor> postProcessorIterator = EMFCompareExtensionRegistry
 				.getRegisteredPostProcessors().iterator();
 		while (postProcessorIterator.hasNext()) {
 			final PostProcessorDescriptor descriptor = postProcessorIterator.next();
 			if (descriptor.getNsUri() != null && descriptor.getNsUri().trim().length() != 0) {
 				final Set<String> nsUris = scope.getNsURIs();
 				for (String nsUri : nsUris) {
 					if (nsUri.matches(descriptor.getNsUri())) {
 						postProcessor = descriptor.getPostProcessor();
 						break;
 					}
 				}
 			} else if (descriptor.getResourceUri() != null
 					&& descriptor.getResourceUri().trim().length() != 0) {
 				final Set<String> resourceUris = scope.getResourceURIs();
 				for (String resourceUri : resourceUris) {
 					if (resourceUri.matches(descriptor.getResourceUri())) {
 						postProcessor = descriptor.getPostProcessor();
 						break;
 					}
 				}
 			} else {
 				continue;
 			}
 			break;
 		}
 		return postProcessor;
 	}
 }
