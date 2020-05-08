 /*******************************************************************************
  * Copyright (c) 2006, 2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.match.engine;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.common.EMFPlugin;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.BasicMonitor;
 import org.eclipse.emf.common.util.Monitor;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.compare.EMFComparePlugin;
 import org.eclipse.emf.compare.FactoryException;
 import org.eclipse.emf.compare.match.EMFCompareMatchMessages;
 import org.eclipse.emf.compare.match.engine.internal.DistinctEcoreSimilarityChecker;
 import org.eclipse.emf.compare.match.engine.internal.EcoreIDSimilarityChecker;
 import org.eclipse.emf.compare.match.engine.internal.GenericMatchEngineToCheckerBridge;
 import org.eclipse.emf.compare.match.engine.internal.StatisticBasedSimilarityChecker;
 import org.eclipse.emf.compare.match.engine.internal.XMIIDSimilarityChecker;
 import org.eclipse.emf.compare.match.internal.statistic.NameSimilarity;
 import org.eclipse.emf.compare.match.metamodel.Match2Elements;
 import org.eclipse.emf.compare.match.metamodel.Match3Elements;
 import org.eclipse.emf.compare.match.metamodel.MatchElement;
 import org.eclipse.emf.compare.match.metamodel.MatchFactory;
 import org.eclipse.emf.compare.match.metamodel.MatchModel;
 import org.eclipse.emf.compare.match.metamodel.Side;
 import org.eclipse.emf.compare.match.metamodel.UnmatchElement;
 import org.eclipse.emf.compare.match.statistic.MetamodelFilter;
 import org.eclipse.emf.compare.util.EFactory;
 import org.eclipse.emf.compare.util.EMFCompareMap;
 import org.eclipse.emf.compare.util.EclipseModelUtils;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
 /**
  * These services are useful when one wants to compare models more precisely using the method modelDiff.
  * 
  * @author <a href="mailto:cedric.brun@obeo.fr">Cedric Brun</a>
  */
 public class GenericMatchEngine implements IMatchEngine {
 	/** Containment reference for the matched elements root. */
 	private static final String MATCH_ELEMENT_NAME = "matchedElements"; //$NON-NLS-1$
 
 	/** Containment reference for {@link MatchElement}s' submatches. */
 	private static final String SUBMATCH_ELEMENT_NAME = "subMatchElements"; //$NON-NLS-1$
 
 	/** Containment reference for the {@link MatchModel}'s unmatched elements. */
 	private static final String UNMATCH_ELEMENT_NAME = "unmatchedElements"; //$NON-NLS-1$
 
 	/**
 	 * {@link MetamodelFilter} used for filtering unused features of the objects we're computing the
 	 * similarity for.
 	 */
 	protected MetamodelFilter filter = new MetamodelFilter();
 
 	/**
 	 * Contains the options given to the match procedure. This method is deprecated, if you want to handle
 	 * specific options for your match engine you should override the updateSettings() method.
 	 */
 	@Deprecated
 	protected Map<String, Object> options = new EMFCompareMap<String, Object>();
 
 	/** Contains the options given to the match procedure. */
 	private MatchSettings structuredOptions;
 
 	/**
 	 * This list allows us to memorize the unmatched elements for a three-way comparison.<br/>
 	 * <p>
 	 * More specifically, we will populate this list with the {@link UnmatchElement}s created by the
 	 * comparison between the left and the ancestor model, followed by the {@link UnmatchElement}s created by
 	 * the comparison between the right and the ancestor model.<br/>
 	 * Those {@link UnmatchElement}s will then be filtered to retain only those that actually cannot be
 	 * matched.
 	 * </p>
 	 */
 	private final Set<EObject> remainingUnmatchedElements = new HashSet<EObject>();
 
 	/**
 	 * This list will be intensively used while matching elements to keep track of the unmatched ones from the
 	 * left model.
 	 */
 	private final List<EObject> stillToFindFromModel1 = new ArrayList<EObject>();
 
 	/**
 	 * This list will be intensively used while matching elements to keep track of the unmatched ones from the
 	 * right model.
 	 */
 	private final List<EObject> stillToFindFromModel2 = new ArrayList<EObject>();
 
 	/**
 	 * Current checker used by the engine to determine whether an element has the same identity as another one
 	 * or not.
 	 */
 	private AbstractSimilarityChecker checker;
 
 	/**
 	 * This list is used while matching elements to keep track of matched reference targets, being outside the
 	 * provided match scope.
 	 */
 	private List<Match2Elements> externalRefMappings = new ArrayList<Match2Elements>();
 
 	/**
 	 * The options map must be initialized to avoid potential NPEs. This initializer will take care of this
 	 * issue.
 	 */
 	{
 		structuredOptions = new MatchSettings();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.match.engine.IMatchEngine#contentMatch(org.eclipse.emf.ecore.EObject,
 	 *      org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, java.util.Map)
 	 */
 	public MatchModel contentMatch(EObject leftObject, EObject rightObject, EObject ancestor,
 			Map<String, Object> optionMap) {
 		updateSettings(structuredOptions, optionMap);
 		checker = prepareChecker();
 
 		// see if scope provider was passed in via option, otherwise create default one
 		final IMatchScopeProvider scopeProvider = MatchScopeProviderUtil.getScopeProvider(optionMap,
 				leftObject, rightObject, ancestor);
 
 		final IMatchScope leftScope = scopeProvider.getLeftScope();
 		final IMatchScope rightScope = scopeProvider.getRightScope();
 		final IMatchScope ancestorScope = scopeProvider.getAncestorScope();
 
 		MatchModel result = null;
 		if (leftScope.isInScope(leftObject) && rightScope.isInScope(rightObject)
 				&& ancestorScope.isInScope(ancestor)) {
 			result = doContentMatch(leftObject, leftScope, rightObject, rightScope, ancestor, ancestorScope);
 		}
 		return result;
 	}
 
 	/**
 	 * This method will compare three {@link EObject}s and their direct content, ignoring the given objects'
 	 * siblings and parents, as well as all objects not being part of the scope (indeed the given ones will
 	 * also not be compared, if they are not included in the scope), for the match. It will however compute
 	 * external mappings for all those objects outside the scope, being referenced from those that are
 	 * processed.
 	 * 
 	 * @param leftObject
 	 *            Left of the two objects to get compared.
 	 * @param leftScope
 	 *            The scope to restrict which content of the left object is processed.
 	 * @param rightObject
 	 *            Right of the two objects to compare.
 	 * @param rightScope
 	 *            The scope to restrict which content of the left object is processed.
 	 * @param ancestor
 	 *            Common ancestor of the two others.
 	 * @param ancestorScope
 	 *            The scope to restrict which content of the ancestor is processed.
 	 * @return {@link MatchModel} for these two objects' comparison.
 	 */
 	private MatchModel doContentMatch(EObject leftObject, IMatchScope leftScope, EObject rightObject,
 			IMatchScope rightScope, EObject ancestor, IMatchScope ancestorScope) {
 		MatchModel root = null;
 
 		// proceed if input elements are within scope
 		if (leftScope.isInScope(leftObject) && rightScope.isInScope(rightObject)
 				&& ancestorScope.isInScope(ancestor)) {
 			root = MatchFactory.eINSTANCE.createMatchModel();
 
 			setModelRoots(root, leftObject, rightObject, ancestor);
 
 			final Monitor monitor = createProgressMonitor();
 
 			// perform content match
 			final MatchModel leftObjectAncestorMatch = doContentMatch(leftObject, leftScope, ancestor,
 					ancestorScope);
 			// remove those external mappings added by call to contentMatch
 			leftObjectAncestorMatch.getMatchedElements().removeAll(externalRefMappings);
 			final List<Match2Elements> leftExternal2WayMappings = new ArrayList<Match2Elements>(
 					externalRefMappings);
 
 			// perform content match
 			final MatchModel rightObjectAncestorMatch = doContentMatch(rightObject, rightScope, ancestor,
 					ancestorScope);
 			// remove those external mappings added by call to contentMatch
 			rightObjectAncestorMatch.getMatchedElements().removeAll(externalRefMappings);
 			final List<Match2Elements> rightExternal2WayMappings = new ArrayList<Match2Elements>(
 					externalRefMappings);
 
 			final List<MatchElement> leftObjectMatchedElements = new ArrayList<MatchElement>(
 					leftObjectAncestorMatch.getMatchedElements());
 			final List<MatchElement> rightObjectMatchedElements = new ArrayList<MatchElement>(
 					rightObjectAncestorMatch.getMatchedElements());
 
 			// populates the unmatched elements list for later use
 			for (final Object unmatch : leftObjectAncestorMatch.getUnmatchedElements()) {
 				remainingUnmatchedElements.add(((UnmatchElement)unmatch).getElement());
 			}
 			for (final Object unmatch : rightObjectAncestorMatch.getUnmatchedElements()) {
 				remainingUnmatchedElements.add(((UnmatchElement)unmatch).getElement());
 			}
 			try {
 				Match3Elements subMatchRoot = null;
 				if (leftObjectMatchedElements.size() > 0 && rightObjectMatchedElements.size() > 0) {
 					final Match2Elements leftObjectMatchRoot = (Match2Elements)leftObjectMatchedElements
 							.get(0);
 					final Match2Elements rightObjectMatchRoot = (Match2Elements)rightObjectMatchedElements
 							.get(0);
 					subMatchRoot = MatchFactory.eINSTANCE.createMatch3Elements();
 
 					subMatchRoot.setSimilarity(set3WaySimilarity(leftObjectMatchRoot.getLeftElement(),
 							rightObjectMatchRoot.getLeftElement(), rightObjectMatchRoot.getRightElement()));
 					subMatchRoot.setLeftElement(leftObjectMatchRoot.getLeftElement());
 					subMatchRoot.setRightElement(rightObjectMatchRoot.getLeftElement());
 					subMatchRoot.setOriginElement(rightObjectMatchRoot.getRightElement());
 					redirectedAdd(root, MATCH_ELEMENT_NAME, subMatchRoot);
 					createSub3Match(root, subMatchRoot, leftObjectMatchRoot, rightObjectMatchRoot);
 				} else {
 					for (final EObject left : leftObjectMatchedElements) {
 						stillToFindFromModel1.add(left);
 					}
 					for (final EObject right : rightObjectMatchedElements) {
 						stillToFindFromModel2.add(right);
 					}
 				}
 				// We will now check through the unmatched object for matches.
 				processNotFoundElements(root, subMatchRoot);
 				// #createSub3Match(MatchModel, Match3Element, Match2Elements,
 				// Match2Elements) will have updated "remainingUnmatchedElements"
 				final Set<EObject> remainingLeft = new HashSet<EObject>();
 				final Set<EObject> remainingRight = new HashSet<EObject>();
 				for (final EObject unmatched : remainingUnmatchedElements) {
 					if (unmatched.eResource() == leftObject.eResource()) {
 						remainingLeft.add(unmatched);
 						final TreeIterator<EObject> iterator = unmatched.eAllContents();
 						while (iterator.hasNext()) {
 							remainingLeft.add(iterator.next());
 						}
 					} else if (unmatched.eResource() == rightObject.eResource()) {
 						remainingRight.add(unmatched);
 						final TreeIterator<EObject> iterator = unmatched.eAllContents();
 						while (iterator.hasNext()) {
 							remainingRight.add(iterator.next());
 						}
 					}
 				}
 				stillToFindFromModel1.clear();
 				stillToFindFromModel2.clear();
 				final List<Match2Elements> mappings = mapLists(new ArrayList<EObject>(remainingLeft),
 						new ArrayList<EObject>(remainingRight), structuredOptions.getSearchWindow(), monitor);
 				for (final Match2Elements map : mappings) {
 					final Match3Elements subMatch = MatchFactory.eINSTANCE.createMatch3Elements();
 					subMatch.setLeftElement(map.getLeftElement());
 					subMatch.setRightElement(map.getRightElement());
 					if (subMatchRoot == null) {
 						redirectedAdd(root, MATCH_ELEMENT_NAME, subMatch);
 					} else {
 						redirectedAdd(subMatchRoot, SUBMATCH_ELEMENT_NAME, subMatch);
 					}
 				}
 				final Map<EObject, Boolean> unmatchedElements = new EMFCompareMap<EObject, Boolean>();
 				for (final EObject unmatch : stillToFindFromModel1) {
 					unmatchedElements.put(unmatch, false);
 					createThreeWayUnmatchElements(root, unmatchedElements, true);
 				}
 				unmatchedElements.clear();
 				for (final EObject remoteUnmatch : stillToFindFromModel2) {
 					unmatchedElements.put(remoteUnmatch, true);
 					createThreeWayUnmatchElements(root, unmatchedElements, false);
 				}
 			} catch (final FactoryException e) {
 				EMFComparePlugin.log(e, false);
 			} catch (final InterruptedException e) {
 				// Cannot be thrown since we have no monitor
 			}
 
 			// create mappings for external references
 			create3WayMatches(leftExternal2WayMappings, rightExternal2WayMappings);
 
 		}
 		return root;
 	}
 
 	/**
 	 * Build the best checker depending on the options.
 	 * 
 	 * @since 1.1
 	 */
 	protected AbstractSimilarityChecker prepareChecker() {
 		AbstractSimilarityChecker checker = null;
 		GenericMatchEngineToCheckerBridge bridge = new GenericMatchEngineToCheckerBridge() {
 
 			@Override
 			public double nameSimilarity(EObject obj1, EObject obj2) {
 				return GenericMatchEngine.this.nameSimilarity(obj1, obj2);
 			}
 
 			@Override
 			public double contentSimilarity(EObject obj1, EObject obj2) throws FactoryException {
 				return GenericMatchEngine.this.contentSimilarity(obj1, obj2);
 			}
 		};
 		if (!structuredOptions.shouldMatchDistinctMetamodels()) {
 			checker = new DistinctEcoreSimilarityChecker(filter, bridge);
 		} else {
 			checker = new StatisticBasedSimilarityChecker(filter, bridge);
 		}
 		if (!structuredOptions.isIgnoringID()) {
 			checker = new EcoreIDSimilarityChecker(filter, checker);
 			disableMetamodelFilter();
 		}
 		if (!structuredOptions.isIgnoringXMIID()) {
 			checker = new XMIIDSimilarityChecker(filter, checker);
 			disableMetamodelFilter();
 		}
 		return checker;
 
 	}
 
 	/**
 	 * replace the metamodel filter with one doing nothing.
 	 */
 	private void disableMetamodelFilter() {
 		filter = new MetamodelFilter() {
 
 			@Override
 			public void analyseModel(EObject root) {
 				/*
 				 * do nothing
 				 */
 			}
 
 			@Override
 			public List<EStructuralFeature> getFilteredFeatures(EObject eObj) {
				return eObj.eClass().getEAllStructuralFeatures();
 			}
 		};
 
 	}
 
 	/**
 	 * prepare the engine with the options.
 	 * 
 	 * @param settings
 	 *            the settings to update.
 	 * @param optionMap
 	 *            the match options.
 	 *@since 1.1
 	 */
 	protected void updateSettings(MatchSettings settings, Map<String, Object> optionMap) {
 		if (optionMap != null && optionMap.size() > 0) {
 			settings.update(optionMap);
 		}
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.match.engine.IMatchEngine#contentMatch(org.eclipse.emf.ecore.EObject,
 	 *      org.eclipse.emf.ecore.EObject, java.util.Map)
 	 */
 	public MatchModel contentMatch(EObject leftObject, EObject rightObject, Map<String, Object> optionMap) {
 		externalRefMappings.clear();
 		updateSettings(structuredOptions, optionMap);
 		checker = prepareChecker();
 
 		// see if scope provider was passed in via option, otherwise create default one
 		final IMatchScopeProvider scopeProvider = MatchScopeProviderUtil.getScopeProvider(optionMap,
 				leftObject, rightObject);
 
 		final IMatchScope leftScope = scopeProvider.getLeftScope();
 		final IMatchScope rightScope = scopeProvider.getRightScope();
 
 		MatchModel result = null;
 		if (leftScope.isInScope(leftObject) && rightScope.isInScope(rightObject)) {
 			result = doContentMatch(leftObject, leftScope, rightObject, rightScope);
 		}
 		return result;
 	}
 
 	/**
 	 * This method will compare two {@link EObject}s and their direct content, ignoring the given objects'
 	 * siblings and parents, as well as all objects not being part of the scope (indeed the given ones will
 	 * also not be compared, if they are not included in the scope), for the match. It will however compute
 	 * external mappings for all those objects outside the scope, being referenced from those that are
 	 * processed.
 	 * 
 	 * @param leftObject
 	 *            Left of the two objects to get compared.
 	 * @param leftScope
 	 *            The scope to restrict which content of the left object is processed.
 	 * @param rightObject
 	 *            Right of the two objects to compare.
 	 *@param rightScope
 	 *            The scope to restrict which content of the left object is processed.
 	 * @return {@link MatchModel} for these two objects' comparison.
 	 */
 	private MatchModel doContentMatch(EObject leftObject, IMatchScope leftScope, EObject rightObject,
 			IMatchScope rightScope) {
 		final Monitor monitor = createProgressMonitor();
 
 		MatchModel root = null;
 		if (leftScope.isInScope(leftObject) && rightScope.isInScope(rightObject)) {
 			root = MatchFactory.eINSTANCE.createMatchModel();
 
 			setModelRoots(root, leftObject, rightObject);
 			/*
 			 * As we could very well be passed two EClasses (as opposed to modelMatch which compares all roots
 			 * of a resource), we cannot filter the model.
 			 */
 
 			final Set<EObject> still1 = new HashSet<EObject>();
 			final Set<EObject> still2 = new HashSet<EObject>();
 
 			// navigate through both objects at the same time and realize mappings..
 			try {
 				checker.init(leftObject, rightObject);
 				if (isSimilar(leftObject, rightObject)) {
 					stillToFindFromModel1.clear();
 					stillToFindFromModel2.clear();
 					final Match2Elements matchModelRoot = recursiveMappings(leftObject, leftScope,
 							rightObject, rightScope, monitor);
 					redirectedAdd(root, MATCH_ELEMENT_NAME, matchModelRoot);
 					createSubMatchElements(matchModelRoot, new ArrayList<EObject>(stillToFindFromModel1),
 							leftScope, new ArrayList<EObject>(stillToFindFromModel2), rightScope, monitor);
 					still1.addAll(stillToFindFromModel1);
 					still2.addAll(stillToFindFromModel2);
 					createUnmatchElements(root, still1, true, false);
 					createUnmatchElements(root, still2, false, false);
 				} else {
 					// The two objects passed as this method's parameters are not
 					// similar. Creates unmatch root.
 					still1.add(leftObject);
 					still2.add(rightObject);
 					createUnmatchElements(root, still1, true, false);
 					createUnmatchElements(root, still2, false, false);
 				}
 			} catch (final FactoryException e) {
 				EMFComparePlugin.log(e, false);
 			} catch (final InterruptedException e) {
 				// Cannot be thrown since we have no monitor
 			}
 
 			root.getMatchedElements().addAll(externalRefMappings);
 		}
 		return root;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.match.engine.IMatchEngine#modelMatch(org.eclipse.emf.ecore.EObject,
 	 *      org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, java.util.Map)
 	 */
 	public MatchModel modelMatch(EObject leftRoot, EObject rightRoot, EObject ancestor,
 			Map<String, Object> optionMap) throws InterruptedException {
 		updateSettings(structuredOptions, optionMap);
 		checker = prepareChecker();
 
 		MatchModel result = null;
 		// Creates and sizes progress monitor
 		final Monitor monitor = createProgressMonitor();
 		int size = 1;
 		for (final EObject root : leftRoot.eResource().getContents()) {
 			final Iterator<EObject> rootContent = root.eAllContents();
 			while (rootContent.hasNext()) {
 				rootContent.next();
 				size++;
 			}
 		}
 		startMonitor(monitor, size << 1);
 
 		// see if scope provider was passed in via option, otherwise create default one
 		final IMatchScopeProvider scopeProvider = MatchScopeProviderUtil.getScopeProvider(optionMap, leftRoot
 				.eResource(), rightRoot.eResource(), ancestor.eResource());
 
 		final IMatchScope leftScope = scopeProvider.getLeftScope();
 		final IMatchScope rightScope = scopeProvider.getRightScope();
 		final IMatchScope ancestorScope = scopeProvider.getAncestorScope();
 
 		if (leftScope.isInScope(leftRoot.eResource()) && rightScope.isInScope(rightRoot.eResource())
 				&& ancestorScope.isInScope(ancestor.eResource())) {
 			result = doMatch(leftRoot.eResource(), leftScope, rightRoot.eResource(), rightScope, ancestor
 					.eResource(), ancestorScope, monitor);
 		}
 
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.match.engine.IMatchEngine#modelMatch(org.eclipse.emf.ecore.EObject,
 	 *      org.eclipse.emf.ecore.EObject, java.util.Map)
 	 */
 	public MatchModel modelMatch(EObject leftRoot, EObject rightRoot, Map<String, Object> optionMap)
 			throws InterruptedException {
 		updateSettings(structuredOptions, optionMap);
 		checker = prepareChecker();
 
 		MatchModel result = null;
 		// Creates and sizes progress monitor
 		final Monitor monitor = createProgressMonitor();
 		int size = 1;
 		if (leftRoot.eResource() != null && rightRoot.eResource() != null) {
 			for (final EObject root : leftRoot.eResource().getContents()) {
 				final Iterator<EObject> rootContent = root.eAllContents();
 				while (rootContent.hasNext()) {
 					rootContent.next();
 					size++;
 				}
 			}
 
 			startMonitor(monitor, size);
 
 			// see if scope provider was passed in via option, otherwise create default one
 			final IMatchScopeProvider scopeProvider = MatchScopeProviderUtil.getScopeProvider(optionMap,
 					leftRoot.eResource(), rightRoot.eResource());
 			final IMatchScope leftScope = scopeProvider.getLeftScope();
 			final IMatchScope rightScope = scopeProvider.getRightScope();
 
 			if (leftScope.isInScope(leftRoot.eResource()) && rightScope.isInScope(rightRoot.eResource())) {
 				result = doMatch(leftRoot.eResource(), leftScope, rightRoot.eResource(), rightScope, monitor);
 			}
 		} else {
 			final Iterator<EObject> rootContent = leftRoot.eAllContents();
 			while (rootContent.hasNext()) {
 				rootContent.next();
 				size++;
 			}
 			startMonitor(monitor, size);
 			IMatchScope alwaysInScope = new IMatchScope() {
 
 				public boolean isInScope(Resource resource) {
 					return true;
 				}
 
 				public boolean isInScope(EObject eObject) {
 					return true;
 				}
 			};
 			result = doContentMatch(leftRoot, alwaysInScope, rightRoot, alwaysInScope);
 
 		}
 
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.match.engine.IMatchEngine#reset()
 	 */
 	public void reset() {
 		filter.clear();
 		filter = new MetamodelFilter();
 		checker = null;
 
 		remainingUnmatchedElements.clear();
 		stillToFindFromModel1.clear();
 		stillToFindFromModel2.clear();
 		externalRefMappings.clear();
 		structuredOptions = new MatchSettings();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.match.engine.IMatchEngine#resourceMatch(org.eclipse.emf.ecore.resource.Resource,
 	 *      org.eclipse.emf.ecore.resource.Resource, java.util.Map)
 	 */
 	public MatchModel resourceMatch(Resource leftResource, Resource rightResource,
 			Map<String, Object> optionMap) throws InterruptedException {
 		updateSettings(structuredOptions, optionMap);
 		checker = prepareChecker();
 
 		MatchModel result = null;
 		// Creates and sizes progress monitor
 		final Monitor monitor = createProgressMonitor();
 		int size = 1;
 		for (final EObject root : leftResource.getContents()) {
 			final Iterator<EObject> rootContent = root.eAllContents();
 			while (rootContent.hasNext()) {
 				rootContent.next();
 				size++;
 			}
 		}
 
 		// see if scope provider was passed in via option, otherwise create default one
 		final IMatchScopeProvider scopeProvider = MatchScopeProviderUtil.getScopeProvider(optionMap,
 				leftResource, rightResource);
 
 		final IMatchScope leftScope = scopeProvider.getLeftScope();
 		final IMatchScope rightScope = scopeProvider.getRightScope();
 
 		startMonitor(monitor, size);
 
 		if (leftScope.isInScope(leftResource) && rightScope.isInScope(rightResource)) {
 			result = doMatch(leftResource, leftScope, rightResource, rightScope, monitor);
 		}
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.match.engine.IMatchEngine#resourceMatch(org.eclipse.emf.ecore.resource.Resource,
 	 *      org.eclipse.emf.ecore.resource.Resource, org.eclipse.emf.ecore.resource.Resource, java.util.Map)
 	 */
 	public MatchModel resourceMatch(Resource leftResource, Resource rightResource, Resource ancestorResource,
 			Map<String, Object> optionMap) throws InterruptedException {
 		updateSettings(structuredOptions, optionMap);
 		checker = prepareChecker();
 
 		MatchModel result = null;
 		// Creates and sizes progress monitor
 		final Monitor monitor = createProgressMonitor();
 		int size = 1;
 		for (final EObject root : leftResource.getContents()) {
 			final Iterator<EObject> rootContent = root.eAllContents();
 			while (rootContent.hasNext()) {
 				rootContent.next();
 				size++;
 			}
 		}
 		startMonitor(monitor, size << 1);
 
 		// see if scope provider was passed in via option, otherwise create default one
 		final IMatchScopeProvider scopeProvider = MatchScopeProviderUtil.getScopeProvider(optionMap,
 				leftResource, rightResource, ancestorResource);
 
 		final IMatchScope leftScope = scopeProvider.getLeftScope();
 		final IMatchScope rightScope = scopeProvider.getRightScope();
 		final IMatchScope ancestorScope = scopeProvider.getAncestorScope();
 
 		if (leftScope.isInScope(leftResource) && rightScope.isInScope(rightResource)
 				&& ancestorScope.isInScope(ancestorResource)) {
 			result = doMatch(leftResource, leftScope, rightResource, rightScope, ancestorResource,
 					ancestorScope, monitor);
 		}
 		return result;
 	}
 
 	/**
 	 * This will iterate through the given {@link List} and return its element which is most similar (as given
 	 * by {@link #absoluteMetric(EObject, EObject)}) to the given {@link EObject}.
 	 * 
 	 * @param eObj
 	 *            {@link EObject} we're searching a similar item for in the list.
 	 * @param list
 	 *            {@link List} in which we are to find an object similar to <code>eObj</code>.
 	 * @return The element from <code>list</code> which is the most similar to <code>eObj</code>.
 	 * @throws FactoryException
 	 *             Thrown if we cannot compute the {@link #absoluteMetric(EObject, EObject) absolute metric}
 	 *             between <code>eObj</code> and one of the list's objects.
 	 */
 	protected EObject findMostSimilar(EObject eObj, List<EObject> list) throws FactoryException {
 		double max = 0d;
 		EObject resultObject = null;
 		final Iterator<EObject> it = list.iterator();
 		while (it.hasNext() && max < 1.0d) {
 			final EObject next = it.next();
 			if (structuredOptions.shouldMatchDistinctMetamodels()
 					|| EcoreUtil.equals(eObj.eClass(), next.eClass())) {
 				final double similarity = checker.absoluteMetric(eObj, next);
 				if (similarity > max) {
 					max = similarity;
 					resultObject = next;
 				}
 			}
 		}
 		return resultObject;
 	}
 
 	/**
 	 * Creates the progress monitor that will be displayed to the user while the comparison lasts.
 	 * 
 	 * @return The progress monitor that will be displayed to the user while the comparison lasts.
 	 */
 	private Monitor createProgressMonitor() {
 		Monitor monitor = new BasicMonitor();
 		final Object delegateMonitor = structuredOptions.getProgressMonitor();
 		if (delegateMonitor != null && EMFPlugin.IS_ECLIPSE_RUNNING) {
 			monitor = EclipseModelUtils.createProgressMonitor(delegateMonitor);
 		}
 		return monitor;
 	}
 
 	/**
 	 * This will recursively create three-way submatches and add them under the given {@link MatchModel}. The
 	 * two {@link Match2Elements} we consider as parameters are the result of the two-way comparisons between
 	 * :
 	 * <ul>
 	 * <li>The left and origin model.</li>
 	 * <li>The right and origin model.</li>
 	 * </ul>
 	 * <br/>
 	 * <br/>
 	 * We can then consider that a {@link match3elements} would be :
 	 * 
 	 * <pre>
 	 * match.leftElement = left.getLeftElement();
 	 * match.originElement = left.getRightElement() = right.getRightElement();
 	 * match.rightElement = right.getLeftElement();
 	 * </pre>
 	 * 
 	 * @param root
 	 *            {@link MatchModel} under which to add our {@link match3elements}s.
 	 * @param matchElementRoot
 	 *            Root of the {@link Match3Elements}s' hierarchy for the current element to be created.
 	 * @param left
 	 *            Left {@link Match2Elements} to consider.
 	 * @param right
 	 *            Right {@link Match2Elements} to consider.
 	 * @throws FactoryException
 	 *             Thrown if we cannot compute the {@link #absoluteMetric(EObject, EObject, EObject) absolute
 	 *             metric} between the three elements or if we cannot add a {@link match3elements} under the
 	 *             given <code>matchElementRoot</code>.
 	 */
 	private void createSub3Match(MatchModel root, Match3Elements matchElementRoot, Match2Elements left,
 			Match2Elements right) throws FactoryException {
 		final List<MatchElement> leftSubMatches = left.getSubMatchElements();
 		final List<MatchElement> rightSubMatches = right.getSubMatchElements();
 		final List<MatchElement> leftNotFound = new ArrayList<MatchElement>(leftSubMatches);
 		final List<MatchElement> rightNotFound = new ArrayList<MatchElement>(rightSubMatches);
 
 		for (final MatchElement nextLeft : leftSubMatches) {
 			final Match2Elements nextLeftMatch = (Match2Elements)nextLeft;
 			Match2Elements correspondingMatch = null;
 
 			for (final MatchElement nextRight : rightNotFound) {
 				final Match2Elements nextRightMatch = (Match2Elements)nextRight;
 				if (nextRightMatch.getRightElement().equals(nextLeftMatch.getRightElement())) {
 					correspondingMatch = nextRightMatch;
 					break;
 				}
 			}
 
 			if (correspondingMatch != null) {
 				final Match3Elements match = MatchFactory.eINSTANCE.createMatch3Elements();
 				match.setSimilarity(set3WaySimilarity(nextLeftMatch.getLeftElement(), correspondingMatch
 						.getLeftElement(), correspondingMatch.getRightElement()));
 				match.setLeftElement(nextLeftMatch.getLeftElement());
 				match.setRightElement(correspondingMatch.getLeftElement());
 				match.setOriginElement(correspondingMatch.getRightElement());
 				redirectedAdd(matchElementRoot, SUBMATCH_ELEMENT_NAME, match);
 				createSub3Match(root, matchElementRoot, nextLeftMatch, correspondingMatch);
 				leftNotFound.remove(nextLeftMatch);
 				rightNotFound.remove(correspondingMatch);
 			}
 		}
 
 		for (final MatchElement nextLeftNotFound : leftNotFound) {
 			stillToFindFromModel1.add(nextLeftNotFound);
 		}
 		for (final MatchElement nextRightNotFound : rightNotFound) {
 			stillToFindFromModel2.add(nextRightNotFound);
 		}
 	}
 
 	/**
 	 * Creates the {@link Match2Elements submatch elements} corresponding to the mapping of objects from the
 	 * two given {@link List}s.
 	 * 
 	 * @param root
 	 *            Root of the {@link MatchModel} where to insert all these mappings.
 	 * @param list1
 	 *            First of the lists used to compute mapping.
 	 * @param list1Scope
 	 *            The scope to restrict the matching of sub elements in list1.
 	 * @param list2
 	 *            Second of the lists used to compute mapping.
 	 * @param list2Scope
 	 *            The scope to restrict the matching of sub elements in list2.
 	 * @param monitor
 	 *            {@link CompareProgressMonitor progress monitor} to display while the comparison lasts. Might
 	 *            be <code>null</code>, in which case we won't monitor progress.
 	 * @throws FactoryException
 	 *             Thrown if we cannot match the elements of the two lists or add submatch elements to
 	 *             <code>root</code>.
 	 * @throws InterruptedException
 	 *             Thrown if the operation is cancelled or fails somehow.
 	 */
 	private void createSubMatchElements(EObject root, List<EObject> list1, IMatchScope list1Scope,
 			List<EObject> list2, IMatchScope list2Scope, Monitor monitor) throws FactoryException,
 			InterruptedException {
 		stillToFindFromModel1.clear();
 		stillToFindFromModel2.clear();
 		final List<Match2Elements> mappings = mapLists(list1, list2, structuredOptions.getSearchWindow(),
 				monitor);
 
 		final Iterator<Match2Elements> it = mappings.iterator();
 		while (it.hasNext()) {
 			final Match2Elements map = it.next();
 			final Match2Elements match = recursiveMappings(map.getLeftElement(), list1Scope, map
 					.getRightElement(), list2Scope, monitor);
 			redirectedAdd(root, SUBMATCH_ELEMENT_NAME, match);
 		}
 	}
 
 	/**
 	 * Creates {@link UnmatchElement}s and {@link RemoteUnmatchElement}s wrapped around all the elements of
 	 * the given {@link List}.
 	 * 
 	 * @param root
 	 *            Root of the {@link MatchModel} under which to insert all these elements.
 	 * @param unmatchedElements
 	 *            {@link List} containing all the elements we haven't been able to match.
 	 * @param leftSide
 	 *            If set to <code>true</code>, the unmatched element will be set to be from the left side.
 	 * @throws FactoryException
 	 *             Thrown if we cannot add elements under the given {@link MatchModel root}.
 	 */
 	private void createThreeWayUnmatchElements(MatchModel root, Map<EObject, Boolean> unmatchedElements,
 			boolean leftSide) throws FactoryException {
 		for (final Map.Entry<EObject, Boolean> entry : unmatchedElements.entrySet()) {
 			// We will only consider the highest level of an unmatched element
 			// hierarchy
 			if (!unmatchedElements.containsKey(entry.getKey().eContainer())) {
 				final UnmatchElement unMap = MatchFactory.eINSTANCE.createUnmatchElement();
 				unMap.setElement(entry.getKey());
 				if (entry.getValue()) {
 					unMap.setRemote(true);
 				}
 				if (leftSide) {
 					unMap.setSide(Side.LEFT);
 				} else {
 					unMap.setSide(Side.RIGHT);
 				}
 				redirectedAdd(root, UNMATCH_ELEMENT_NAME, unMap);
 			}
 		}
 		unmatchedElements.clear();
 	}
 
 	/**
 	 * Creates {@link UnmatchElement}s wrapped around all the elements of the given {@link List}.
 	 * 
 	 * @param root
 	 *            Root of the {@link MatchModel} under which to insert all these {@link UnmatchElement}s.
 	 * @param unmatchedElements
 	 *            {@link Set} containing all the elements we haven't been able to match.
 	 * @param leftSide
 	 *            If set to <code>true</code>, the unmatched elements will be set to be from the left side.
 	 * @param remote
 	 *            If <code>true</code>, the unmatched elements will be set to reflect a remote change.
 	 * @throws FactoryException
 	 *             Thrown if we cannot add elements under the given {@link MatchModel root}.
 	 */
 	private void createUnmatchElements(MatchModel root, Set<EObject> unmatchedElements, boolean leftSide,
 			boolean remote) throws FactoryException {
 		for (final EObject element : unmatchedElements) {
 			final UnmatchElement unMap = MatchFactory.eINSTANCE.createUnmatchElement();
 			unMap.setElement(element);
 			unMap.setRemote(remote);
 			if (leftSide) {
 				unMap.setSide(Side.LEFT);
 			} else {
 				unMap.setSide(Side.RIGHT);
 			}
 			redirectedAdd(root, UNMATCH_ELEMENT_NAME, unMap);
 		}
 		unmatchedElements.clear();
 	}
 
 	/**
 	 * This method handles the creation and returning of a two way model match.
 	 * 
 	 * @param leftResource
 	 *            Left model for the comparison.
 	 * @param leftScope
 	 *            The {@link IMatchScope} restricting the left side of comparison.
 	 * @param rightResource
 	 *            Right model for the comparison.
 	 * @param rightScope
 	 *            The {@link IMatchScope} restricting the right side of comparison.
 	 * @param monitor
 	 *            Progress monitor to display while the comparison lasts.
 	 * @return The corresponding {@link MatchModel}.
 	 * @throws InterruptedException
 	 *             Thrown if the comparison is interrupted somehow.
 	 */
 	private MatchModel doMatch(Resource leftResource, IMatchScope leftScope, Resource rightResource,
 			IMatchScope rightScope, Monitor monitor) throws InterruptedException {
 		externalRefMappings.clear();
 		final MatchModel root = MatchFactory.eINSTANCE.createMatchModel();
 		EObject leftRoot = null;
 		EObject rightRoot = null;
 
 		final List<EObject> leftContents = getScopeInternalContents(leftResource, leftScope);
 		final List<EObject> rightContents = getScopeInternalContents(rightResource, rightScope);
 
 		if (leftContents.size() > 0) {
 			leftRoot = leftContents.get(0);
 		}
 
 		if (rightContents.size() > 0) {
 			rightRoot = rightContents.get(0);
 		}
 
 		setModelRoots(root, leftRoot, rightRoot);
 
 		// filters unused features
 		filterUnused(leftResource);
 		filterUnused(rightResource);
 
 		// navigate through both models at the same time and realize mappings..
 		try {
 			checker.init(leftResource, rightResource);
 
 			monitor.subTask(EMFCompareMatchMessages.getString("DifferencesServices.monitor.roots")); //$NON-NLS-1$
 			final List<Match2Elements> matchedRoots = mapLists(leftContents, rightContents, structuredOptions
 					.getSearchWindow(), monitor);
 			stillToFindFromModel1.clear();
 			stillToFindFromModel2.clear();
 			final List<EObject> unmatchedLeftRoots = new ArrayList<EObject>(leftContents);
 			final List<EObject> unmatchedRightRoots = new ArrayList<EObject>(rightContents);
 			// These sets will help us in keeping track of the yet to be found
 			// elements
 			final Set<EObject> still1 = new HashSet<EObject>();
 			final Set<EObject> still2 = new HashSet<EObject>();
 
 			// If one of the resources has no roots, considers it as deleted
 			if (leftContents.size() > 0 && rightContents.size() > 0) {
 				Match2Elements matchModelRoot = MatchFactory.eINSTANCE.createMatch2Elements();
 				// We haven't found any similar roots, we then consider the
 				// firsts to be similar.
 				if (matchedRoots.size() == 0) {
 					final Match2Elements rootMapping = MatchFactory.eINSTANCE.createMatch2Elements();
 					rootMapping.setLeftElement(leftContents.get(0));
 					EObject rightElement = findMostSimilar(leftContents.get(0), unmatchedRightRoots);
 					if (rightElement == null) {
 						rightElement = unmatchedRightRoots.get(0);
 					}
 					rootMapping.setRightElement(rightElement);
 					matchedRoots.add(rootMapping);
 				}
 				monitor.subTask(EMFCompareMatchMessages
 						.getString("DifferencesServices.monitor.rootsContents")); //$NON-NLS-1$
 				for (final Match2Elements matchedRoot : matchedRoots) {
 					final Match2Elements rootMapping = recursiveMappings(matchedRoot.getLeftElement(),
 							leftScope, matchedRoot.getRightElement(), rightScope, monitor);
 					// this is the first time we're here
 					if (matchModelRoot.getLeftElement() == null) {
 						matchModelRoot = rootMapping;
 						redirectedAdd(root, MATCH_ELEMENT_NAME, matchModelRoot);
 					} else {
 						redirectedAdd(matchModelRoot, SUBMATCH_ELEMENT_NAME, rootMapping);
 					}
 
 					// Synchronizes the two lists to avoid multiple elements
 					still1.removeAll(stillToFindFromModel1);
 					still2.removeAll(stillToFindFromModel2);
 					// checks for matches within the yet to found elements lists
 					createSubMatchElements(rootMapping, new ArrayList<EObject>(stillToFindFromModel1),
 							leftScope, new ArrayList<EObject>(stillToFindFromModel2), rightScope, monitor);
 					// Adds all unfound elements to the sets
 					still1.addAll(stillToFindFromModel1);
 					still2.addAll(stillToFindFromModel2);
 
 					unmatchedLeftRoots.remove(matchedRoot.getLeftElement());
 					unmatchedRightRoots.remove(matchedRoot.getRightElement());
 				}
 				// We'll iterate through the unmatchedRoots all contents
 				monitor.subTask(EMFCompareMatchMessages
 						.getString("DifferencesServices.monitor.unmatchedRoots")); //$NON-NLS-1$
 				createSubMatchElements(matchModelRoot, unmatchedLeftRoots, leftScope, unmatchedRightRoots,
 						rightScope, monitor);
 			} else {
 				// Roots are unmatched, this is either a file addition or
 				// deletion
 				still1.addAll(unmatchedLeftRoots);
 				still2.addAll(unmatchedRightRoots);
 			}
 
 			// Now takes care of remaining unfound elements
 			still1.addAll(stillToFindFromModel1);
 			still2.addAll(stillToFindFromModel2);
 			createUnmatchElements(root, still1, true, false);
 			createUnmatchElements(root, still2, false, false);
 		} catch (final FactoryException e) {
 			EMFComparePlugin.log(e, false);
 		}
 
 		root.getMatchedElements().addAll(externalRefMappings);
 		return root;
 	}
 
 	/**
 	 * This method handles the creation and returning of a three way model match.
 	 * 
 	 * @param leftResource
 	 *            Left model for the comparison.
 	 * @param leftScope
 	 *            The {@link IMatchScope} restricting the left side of comparison.
 	 * @param rightResource
 	 *            Right model for the comparison.
 	 * @param rightScope
 	 *            The {@link IMatchScope} restricting the right side of comparison.
 	 * @param ancestorResource
 	 *            Common ancestor of the right and left models.
 	 * @param ancestorScope
 	 *            The {@link IMatchScope} restricting the ancestor side of comparison.
 	 * @param monitor
 	 *            Progress monitor to display while the comparison lasts.
 	 * @return The corresponding {@link MatchModel}.
 	 * @throws InterruptedException
 	 *             Thrown if the comparison is interrupted somehow.
 	 */
 	private MatchModel doMatch(Resource leftResource, IMatchScope leftScope, Resource rightResource,
 			IMatchScope rightScope, Resource ancestorResource, IMatchScope ancestorScope, Monitor monitor)
 			throws InterruptedException {
 		final MatchModel root = MatchFactory.eINSTANCE.createMatchModel();
 		EObject leftRoot = null;
 		EObject rightRoot = null;
 		EObject ancestorRoot = null;
 
 		final List<EObject> leftContents = getScopeInternalContents(leftResource, leftScope);
 		final List<EObject> rightContents = getScopeInternalContents(rightResource, rightScope);
 		final List<EObject> ancestorContents = getScopeInternalContents(ancestorResource, ancestorScope);
 
 		if (leftContents.size() > 0) {
 			leftRoot = leftContents.get(0);
 		}
 		if (rightContents.size() > 0) {
 			rightRoot = rightContents.get(0);
 		}
 		if (ancestorContents.size() > 0) {
 			ancestorRoot = ancestorContents.get(0);
 		}
 		setModelRoots(root, leftRoot, rightRoot, ancestorRoot);
 
 		final MatchModel root1AncestorMatch = doMatch(leftResource, leftScope, ancestorResource,
 				ancestorScope, monitor);
 		// remove those external mappings added by call to doMatch
 		root1AncestorMatch.getMatchedElements().removeAll(externalRefMappings);
 		final List<Match2Elements> leftExternal2WayMappings = new ArrayList<Match2Elements>(
 				externalRefMappings);
 
 		final MatchModel root2AncestorMatch = doMatch(rightResource, rightScope, ancestorResource,
 				ancestorScope, monitor);
 		// remove those external mappings added by call to doMatch
 		root2AncestorMatch.getMatchedElements().removeAll(externalRefMappings);
 		final List<Match2Elements> rightExternal2WayMappings = new ArrayList<Match2Elements>(
 				externalRefMappings);
 
 		final List<MatchElement> root1MatchedElements = new ArrayList<MatchElement>(root1AncestorMatch
 				.getMatchedElements());
 		final List<MatchElement> root2MatchedElements = new ArrayList<MatchElement>(root2AncestorMatch
 				.getMatchedElements());
 
 		// populates the unmatched elements list for later use
 		// There cannot be any conflicts on those, as neither has an ancestor
 		for (final UnmatchElement unmatch : root1AncestorMatch.getUnmatchedElements()) {
 			remainingUnmatchedElements.add(unmatch);
 		}
 		for (final UnmatchElement unmatch : root2AncestorMatch.getUnmatchedElements()) {
 			remainingUnmatchedElements.add(unmatch);
 		}
 
 		try {
 			final Match3Elements subMatchRoot = MatchFactory.eINSTANCE.createMatch3Elements();
 			if (root2MatchedElements.size() > 0) {
 				final Match2Elements root1Match = (Match2Elements)root1MatchedElements.get(0);
 				final Match2Elements root2Match = (Match2Elements)root2MatchedElements.get(0);
 
 				subMatchRoot.setSimilarity(set3WaySimilarity(root1Match.getLeftElement(), root2Match
 						.getLeftElement(), root2Match.getRightElement()));
 				subMatchRoot.setLeftElement(root1Match.getLeftElement());
 				subMatchRoot.setRightElement(root2Match.getLeftElement());
 				subMatchRoot.setOriginElement(root2Match.getRightElement());
 				redirectedAdd(root, MATCH_ELEMENT_NAME, subMatchRoot);
 				createSub3Match(root, subMatchRoot, root1Match, root2Match);
 			} else if (root1MatchedElements.size() > 0) {
 				stillToFindFromModel1.add(root1MatchedElements.get(0));
 			}
 
 			// We will now check through the unmatched object for matches. This
 			// will allow for a more accurate
 			// difference detection when dealing with multiple roots models.
 			processNotFoundElements(root, subMatchRoot);
 
 			// #processNotFoundElements(MatchModel, Match3Element)
 			// will have updated "remainingUnmatchedElements"
 			/*
 			 * We'll have to make two passes : UnmatchElements are potential matches for one another (they
 			 * result in elements not having ancestors, yet we could still be able to match them through 2-way
 			 * handling). The second pass will try and match Match2Elements with one another. Note that these
 			 * can be matched simply through instance equality of their ancestor element.
 			 */
 			processSingleUnmatchedElements(leftResource, rightResource, root, subMatchRoot, monitor);
 			processUnmatchedMatch2Elements(leftResource, rightResource, root, subMatchRoot);
 		} catch (final FactoryException e) {
 			EMFComparePlugin.log(e, false);
 		}
 
 		// create three way mappings for external references
 		root.getMatchedElements().addAll(
 				create3WayMatches(leftExternal2WayMappings, rightExternal2WayMappings));
 		return root;
 	}
 
 	/**
 	 * Creates {@link Match3Elements} by merging those {@link Match2Elements}, having the same ancestor.
 	 * 
 	 * @param leftToAncestor
 	 *            matches between a left elements and their ancestors.
 	 * @param rightToAncestor
 	 *            matches between right elements and their ancestors.
 	 * @return A list of newly created {@link Match3Elements}, each created for a pair of
 	 *         {@link Match2Elements} from the leftToAncestor and rightToAncestor lists, sharing the same
 	 *         ancestor.
 	 */
 	private List<Match3Elements> create3WayMatches(List<Match2Elements> leftToAncestor,
 			List<Match2Elements> rightToAncestor) {
 		// create three way mappings for external references
 		final List<Match3Elements> threeWayMappings = new ArrayList<Match3Elements>();
 		for (Match2Elements leftExternalMapping : leftToAncestor) {
 			final Match2Elements rightExternalMapping = rightToAncestor.get(leftToAncestor
 					.indexOf(leftExternalMapping));
 
 			final Match3Elements mapping = MatchFactory.eINSTANCE.createMatch3Elements();
 			mapping.setLeftElement(leftExternalMapping.getLeftElement());
 			mapping.setRightElement(rightExternalMapping.getLeftElement());
 			mapping.setOriginElement(rightExternalMapping.getRightElement());
 			try {
 				mapping.setSimilarity(set3WaySimilarity(mapping.getLeftElement(), mapping.getRightElement(),
 						mapping.getOriginElement()));
 			} catch (FactoryException e) {
 				mapping.setSimilarity(1.0d);
 			}
 			threeWayMappings.add(mapping);
 		}
 		return threeWayMappings;
 	}
 
 	/**
 	 * Filters unused features of the resource.
 	 * 
 	 * @param resource
 	 *            Resource to be apply filter on.
 	 */
 	private void filterUnused(Resource resource) {
 		for (final EObject root : resource.getContents()) {
 			filter.analyseModel(root);
 		}
 	}
 
 	/**
 	 * This will return the value associated to the given key in the options map.
 	 * <p>
 	 * NOTE : Misuses of this method will easily throw {@link ClassCastException}s.
 	 * </p>
 	 * 
 	 * @param <T>
 	 *            Expected type of the value associated to <code>key</code>.
 	 * @param key
 	 *            Key of the value to retrieve.
 	 * @return Value associated to the given key in the options map.
 	 * @throws ClassCastException
 	 *             If the value isn't assignment compatible with the expected type.
 	 */
 	@SuppressWarnings("unchecked")
 	protected <T> T getOption(String key) throws ClassCastException {
 		return (T)options.get(key);
 	}
 
 	/**
 	 * This will lookup in the {@link #matchedByID} map and check if the two given objects have indeed been
 	 * matched by their ID. This method is no more used by the generic match engine implementation itself as
 	 * this logic moved to the AbstractSimilarityChecker
 	 * 
 	 * @param left
 	 *            Left of the two objects to check.
 	 * @param right
 	 *            Right of the two objects to check.
 	 * @return <code>True</code> these objects haven't been matched by their ID, <code>False</code> otherwise.
 	 * @throws FactoryException
 	 *             Thrown if we cannot compute the key for the object to match.
 	 */
 	@Deprecated
 	protected boolean haveDistinctID(EObject left, EObject right) throws FactoryException {
 		return checker.isSimilar(left, right);
 	}
 
 	/**
 	 * This will lookup in the {@link #matchedByXMIID} map and check if the two given objects have indeed been
 	 * matched by their XMI ID.
 	 * 
 	 * @param left
 	 *            Left of the two objects to check.
 	 * @param right
 	 *            Right of the two objects to check.
 	 * @return <code>True</code> these objects haven't been matched by their XMI ID, <code>False</code>
 	 *         otherwise.
 	 * @throws FactoryException
 	 *             Thrown if we cannot compute the key for the object to match.
 	 */
 	@Deprecated
 	protected boolean haveDistinctXMIID(EObject left, EObject right) throws FactoryException {
 		return checker.isSimilar(left, right);
 	}
 
 	/**
 	 * This will compute the similarity between two {@link EObject}s' names.
 	 * 
 	 * @param obj1
 	 *            First of the two {@link EObject}s.
 	 * @param obj2
 	 *            Second of the two {@link EObject}s.
 	 * @return <code>double</code> representing the similarity between the two {@link EObject}s' names. 0 &lt;
 	 *         value &lt; 1.
 	 * @see NameSimilarity#nameSimilarityMetric(String, String)
 	 */
 	@Deprecated
 	protected double nameSimilarity(EObject obj1, EObject obj2) {
 		double similarity = 0d;
 		try {
 
 			similarity = NameSimilarity.nameSimilarityMetric(NameSimilarity.findName(obj1), NameSimilarity
 					.findName(obj2));
 		} catch (final FactoryException e) {
 			// fails silently, will return a similarity of 0d
 		}
 		return similarity;
 	}
 
 	/**
 	 * This will compute the similarity between two {@link EObject}s' contents.
 	 * 
 	 * @param obj1
 	 *            First of the two {@link EObject}s.
 	 * @param obj2
 	 *            Second of the two {@link EObject}s.
 	 * @return <code>double</code> representing the similarity between the two {@link EObject}s' contents. 0
 	 *         &lt; value &lt; 1.
 	 * @throws FactoryException
 	 *             Thrown if we cannot compute the {@link EObject}s' contents similarity metrics.
 	 * @see NameSimilarity#contentValue(EObject, MetamodelFilter)
 	 */
 	@Deprecated
 	protected double contentSimilarity(EObject obj1, EObject obj2) throws FactoryException {
 		double similarity = 0d;
 		similarity = NameSimilarity.nameSimilarityMetric(NameSimilarity.contentValue(obj1), NameSimilarity
 				.contentValue(obj2));
 		return similarity;
 	}
 
 	/**
 	 * Returns a list containing mappings of the nodes of both given {@link List}s.
 	 * 
 	 * @param list1
 	 *            First of the lists from which we need to map the elements
 	 * @param list2
 	 *            Second list to map the elements from.
 	 * @param window
 	 *            Number of siblings to consider for the matching.
 	 * @param monitor
 	 *            {@link CompareProgressMonitor Progress monitor} to display while the comparison lasts. Might
 	 *            be <code>null</code>, in which case we won't monitor progress.
 	 * @return A {@link List} containing mappings of the nodes of both given {@link List}s.
 	 * @throws FactoryException
 	 *             Thrown if the metrics cannot be computed.
 	 * @throws InterruptedException
 	 *             Thrown if the matching process is interrupted somehow.
 	 */
 	private List<Match2Elements> mapLists(List<EObject> list1, List<EObject> list2, int window,
 			Monitor monitor) throws FactoryException, InterruptedException {
 		final List<Match2Elements> result = new ArrayList<Match2Elements>();
 		int curIndex = 0 - window / 2;
 		final List<EObject> notFoundList1 = new ArrayList<EObject>(list1);
 		final List<EObject> notFoundList2 = new ArrayList<EObject>(list2);
 
 		final Iterator<EObject> it1 = list1.iterator();
 		// then iterate over the 2 lists and compare the elements
 		while (it1.hasNext() && notFoundList2.size() > 0) {
 			final EObject obj1 = it1.next();
 			EObject obj2 = checker.fastLookup(obj1);
 
 			if (obj2 == null) {
 				// subtracts the difference between the notfound and the
 				// original list to avoid ArrayOutOfBounds
 				final int end = Math.min(curIndex + window - (list2.size() - notFoundList2.size()),
 						notFoundList2.size());
 				final int index = Math
 						.min(Math.max(curIndex - (list2.size() - notFoundList2.size()), 0), end);
 
 				obj2 = findMostSimilar(obj1, notFoundList2.subList(index, end));
 				if (obj2 != null) {
 					// checks if the most similar to obj2 is obj1
 					final EObject obj1Check = findMostSimilar(obj2, notFoundList1);
 					if (obj1Check != obj1 && obj1Check != null && isSimilar(obj1Check, obj2)) {
 						continue;
 					}
 				}
 			}
 
 			if (notFoundList1.contains(obj1) && notFoundList2.contains(obj2) && isSimilar(obj1, obj2)) {
 				final Match2Elements mapping = MatchFactory.eINSTANCE.createMatch2Elements();
 				final double metric = checker.absoluteMetric(obj1, obj2);
 
 				mapping.setLeftElement(obj1);
 				mapping.setRightElement(obj2);
 				mapping.setSimilarity(metric);
 				result.add(mapping);
 				notFoundList1.remove(obj1);
 				notFoundList2.remove(obj2);
 			}
 			curIndex += 1;
 			monitor.worked(1);
 			if (monitor.isCanceled()) {
 				throw new InterruptedException();
 			}
 		}
 
 		// now putting the not found elements aside for later
 		stillToFindFromModel1.addAll(notFoundList1);
 		stillToFindFromModel2.addAll(notFoundList2);
 		return result;
 	}
 
 	/**
 	 * Allows for a more accurate modifications detection for three way comparison with multiple roots models.
 	 * 
 	 * @param root
 	 *            Root of the {@link MatchModel}.
 	 * @param subMatchRoot
 	 *            Root of the {@link match3elements}' hierarchy for the current element to be created.
 	 * @throws FactoryException
 	 *             Thrown if we cannot compute {@link EObject}s similarity or if adding elements to either
 	 *             <code>root</code> or <code>subMatchRoot</code> fails somehow.
 	 */
 	private void processNotFoundElements(MatchModel root, Match3Elements subMatchRoot)
 			throws FactoryException {
 		for (final EObject obj1 : new ArrayList<EObject>(stillToFindFromModel1)) {
 			if (obj1 instanceof Match2Elements) {
 				final Match2Elements match1 = (Match2Elements)obj1;
 				for (final EObject obj2 : new ArrayList<EObject>(stillToFindFromModel2)) {
 					if (obj2 instanceof Match2Elements) {
 						final Match2Elements match2 = (Match2Elements)obj2;
 
 						if (match1.getRightElement() == match2.getRightElement()) {
 							final Match3Elements match = MatchFactory.eINSTANCE.createMatch3Elements();
 							match.setSimilarity(set3WaySimilarity(match1.getLeftElement(), match2
 									.getLeftElement(), match2.getRightElement()));
 							match.setLeftElement(match1.getLeftElement());
 							match.setRightElement(match2.getLeftElement());
 							match.setOriginElement(match2.getRightElement());
 							// This will happen if we couldn't match previously
 							if (subMatchRoot == null) {
 								redirectedAdd(root, MATCH_ELEMENT_NAME, match);
 								createSub3Match(root, match, match1, match2);
 							} else {
 								redirectedAdd(subMatchRoot, SUBMATCH_ELEMENT_NAME, match);
 								createSub3Match(root, subMatchRoot, match1, match2);
 							}
 							stillToFindFromModel1.remove(match1);
 							stillToFindFromModel2.remove(match2);
 						}
 					}
 				}
 			}
 		}
 
 		for (final EObject eObj : new ArrayList<EObject>(stillToFindFromModel1)) {
 			if (eObj instanceof Match2Elements) {
 				remainingUnmatchedElements.add(eObj);
 			}
 		}
 		for (final EObject eObj : new ArrayList<EObject>(stillToFindFromModel2)) {
 			if (eObj instanceof Match2Elements) {
 				remainingUnmatchedElements.add(eObj);
 			}
 		}
 	}
 
 	/**
 	 * Compute a three way similarity combining results from the pairs.
 	 * 
 	 * @param left
 	 *            left element.
 	 * @param right
 	 *            right element.
 	 * @param ancestor
 	 *            common ancestor.
 	 * @return a value representing their distance.
 	 * @throws FactoryException
 	 */
 	private double set3WaySimilarity(EObject left, EObject right, EObject ancestor) throws FactoryException {
 		final double metric1 = checker.absoluteMetric(left, right);
 		final double metric2 = checker.absoluteMetric(left, ancestor);
 		final double metric3 = checker.absoluteMetric(right, ancestor);
 		return (metric1 + metric2 + metric3) / 3;
 	}
 
 	/**
 	 * This will be used in three way matching to take care of all unmatched elements. Unmatched elements are
 	 * then comprised of two things :
 	 * <ul>
 	 * <li>Match2Elements between left and ancestor or right and ancestor that had no equivalent in
 	 * respectively the right or left model - elements that have been removed or remotely removed.</li>
 	 * <li>UnmatchElements in either the right or the left model - elements that have been added or remotely
 	 * added</li>
 	 * </ul>
 	 * <p>
 	 * We are here handling Match2Elements only. These particular elements cannot be conflicting.
 	 * UnmatchElements will be handled in
 	 * {@link #processSingleUnmatchedElements(Resource, Resource, MatchModel, Match3Elements, Monitor)} .
 	 * </p>
 	 * 
 	 * @param leftResource
 	 *            Left model for the comparison.
 	 * @param rightResource
 	 *            Right model for the comparison.
 	 * @param root
 	 *            Root of the {@link MatchModel}.
 	 * @param subMatchRoot
 	 *            root under which new match elements are to be added.
 	 * @throws FactoryException
 	 *             Thrown if we couldn't add the new UnmatchElements.
 	 */
 	private void processUnmatchedMatch2Elements(Resource leftResource, Resource rightResource,
 			MatchModel root, Match3Elements subMatchRoot) throws FactoryException {
 		final Set<Match2Elements> remainingLeft = new HashSet<Match2Elements>();
 		final Set<Match2Elements> remainingRight = new HashSet<Match2Elements>();
 		for (final EObject unmatched : new ArrayList<EObject>(remainingUnmatchedElements)) {
 			if (unmatched instanceof Match2Elements) {
 				final EObject element = ((Match2Elements)unmatched).getLeftElement();
 				if (element.eResource() == leftResource) {
 					remainingLeft.add((Match2Elements)unmatched);
 				} else if (element.eResource() == rightResource) {
 					remainingRight.add((Match2Elements)unmatched);
 				}
 				// unmatched in ancestor can be safely ignored.
 				remainingUnmatchedElements.remove(unmatched);
 			}
 		}
 
 		for (final Match2Elements left : new HashSet<Match2Elements>(remainingLeft)) {
 			for (final Match2Elements right : new HashSet<Match2Elements>(remainingRight)) {
 				if (left.getRightElement() == right.getRightElement()) {
 					final Match3Elements subMatch = MatchFactory.eINSTANCE.createMatch3Elements();
 					subMatch.setOriginElement(left.getRightElement());
 					subMatch.setLeftElement(left.getLeftElement());
 					subMatch.setRightElement(right.getLeftElement());
 					redirectedAdd(subMatchRoot, SUBMATCH_ELEMENT_NAME, subMatch);
 					remainingLeft.remove(left);
 					remainingRight.remove(right);
 					break;
 				}
 			}
 		}
 		for (final Match2Elements nextLeftUnmatch : remainingLeft) {
 			final UnmatchElement unmatch = MatchFactory.eINSTANCE.createUnmatchElement();
 			unmatch.setElement(nextLeftUnmatch.getLeftElement());
 			unmatch.setSide(Side.LEFT);
 			unmatch.setRemote(true);
 			redirectedAdd(root, UNMATCH_ELEMENT_NAME, unmatch);
 		}
 		for (final Match2Elements nextRightUnmatch : remainingRight) {
 			final UnmatchElement unmatch = MatchFactory.eINSTANCE.createUnmatchElement();
 			unmatch.setElement(nextRightUnmatch.getLeftElement());
 			unmatch.setSide(Side.RIGHT);
 			redirectedAdd(root, UNMATCH_ELEMENT_NAME, unmatch);
 		}
 	}
 
 	/**
 	 * This will be used in three way matching to take care of all unmatched elements. Unmatched elements are
 	 * then comprised of two things :
 	 * <ul>
 	 * <li>Match2Elements between left and ancestor or right and ancestor that had no equivalent in
 	 * respectively the right or left model - elements that have been removed or remotely removed.</li>
 	 * <li>UnmatchElements in either the right or the left model - elements that have been added or remotely
 	 * added</li>
 	 * </ul>
 	 * <p>
 	 * We are here handling UnmatchElements only. Note that these particular differences can be conflicting if
 	 * they are contained within elements that have themselves been removed from the model. Match2Elements
 	 * have been handled by
 	 * {@link #processUnmatchedMatch2Elements(Resource, Resource, MatchModel, Match3Elements)} .
 	 * </p>
 	 * 
 	 * @param leftResource
 	 *            Left model for the comparison.
 	 * @param rightResource
 	 *            Right model for the comparison.
 	 * @param root
 	 *            Root of the {@link MatchModel}.
 	 * @param subMatchRoot
 	 *            root under which new match elements are to be added.
 	 * @param monitor
 	 *            Progress monitor to display while the comparison lasts.
 	 * @throws InterruptedException
 	 *             Thrown if the comparison is interrupted somehow.
 	 * @throws FactoryException
 	 *             Thrown if we couldn't add the new UnmatchElements.
 	 */
 	private void processSingleUnmatchedElements(Resource leftResource, Resource rightResource,
 			MatchModel root, Match3Elements subMatchRoot, Monitor monitor) throws InterruptedException,
 			FactoryException {
 		final Set<EObject> remainingLeft = new HashSet<EObject>();
 		final Set<EObject> remainingRight = new HashSet<EObject>();
 		for (final EObject unmatched : new ArrayList<EObject>(remainingUnmatchedElements)) {
 			if (unmatched instanceof UnmatchElement) {
 				final EObject element = ((UnmatchElement)unmatched).getElement();
 				if (element.eResource() == leftResource) {
 					remainingLeft.add(element);
 				} else if (element.eResource() == rightResource) {
 					remainingRight.add(element);
 				}
 				// unmatched in ancestor can be safely ignored.
 				remainingUnmatchedElements.remove(unmatched);
 			}
 		}
 
 		stillToFindFromModel1.clear();
 		stillToFindFromModel2.clear();
 
 		final List<Match2Elements> mappings = mapLists(new ArrayList<EObject>(remainingLeft),
 				new ArrayList<EObject>(remainingRight), structuredOptions.getSearchWindow(), monitor);
 		for (final Match2Elements map : mappings) {
 			final Match3Elements subMatch = MatchFactory.eINSTANCE.createMatch3Elements();
 			subMatch.setLeftElement(map.getLeftElement());
 			subMatch.setRightElement(map.getRightElement());
 			redirectedAdd(subMatchRoot, SUBMATCH_ELEMENT_NAME, subMatch);
 		}
 		for (final EObject unmatch : stillToFindFromModel1) {
 			final UnmatchElement unMap = MatchFactory.eINSTANCE.createUnmatchElement();
 			unMap.setElement(unmatch);
 			unMap.setSide(Side.LEFT);
 			unMap.setRemote(false);
 			for (final EObject unmatched : remainingUnmatchedElements) {
 				if (unmatched instanceof Match2Elements) {
 					if (unmatch.eContainer() == ((Match2Elements)unmatched).getLeftElement()) {
 						unMap.setConflicting(true);
 						break;
 					}
 				}
 			}
 			redirectedAdd(root, UNMATCH_ELEMENT_NAME, unMap);
 		}
 		for (final EObject remoteUnmatch : stillToFindFromModel2) {
 			final UnmatchElement unMap = MatchFactory.eINSTANCE.createUnmatchElement();
 			unMap.setElement(remoteUnmatch);
 			unMap.setSide(Side.RIGHT);
 			unMap.setRemote(true);
 			for (final EObject unmatched : remainingUnmatchedElements) {
 				if (unmatched instanceof Match2Elements) {
 					if (remoteUnmatch.eContainer() == ((Match2Elements)unmatched).getLeftElement()) {
 						unMap.setConflicting(true);
 						break;
 					}
 				}
 			}
 			redirectedAdd(root, UNMATCH_ELEMENT_NAME, unMap);
 		}
 	}
 
 	/**
 	 * We consider here <code>current1</code> and <code>current2</code> are similar. This method creates the
 	 * mapping for the objects <code>current1</code> and <code>current2</code>, Then submappings for these two
 	 * elements' contents.
 	 * 
 	 * @param current1
 	 *            First element of the two elements mapping.
 	 * @param current1Scope
 	 *            The {@link IMatchScope} to restrict the contents of current1.
 	 * @param current2
 	 *            Second of the two elements mapping.
 	 * @param current2Scope
 	 *            The {@link IMatchScope} to restrict the contents of current2.
 	 * @param monitor
 	 *            {@link CompareProgressMonitor Progress monitor} to display while the comparison lasts. Might
 	 *            be <code>null</code>, in which case we won't monitor progress.
 	 * @return The mapping for <code>current1</code> and <code>current2</code> and their content.
 	 * @throws FactoryException
 	 *             Thrown when the metrics cannot be computed for <code>current1</code> and
 	 *             <code>current2</code>.
 	 * @throws InterruptedException
 	 *             Thrown if the matching process is interrupted somehow.
 	 */
 	private Match2Elements recursiveMappings(EObject current1, IMatchScope current1Scope, EObject current2,
 			IMatchScope current2Scope, Monitor monitor) throws FactoryException, InterruptedException {
 		Match2Elements mapping = null;
 		mapping = MatchFactory.eINSTANCE.createMatch2Elements();
 		mapping.setLeftElement(current1);
 		mapping.setRightElement(current2);
 		mapping.setSimilarity(checker.absoluteMetric(current1, current2));
 		final List<Match2Elements> mapList = mapLists(getScopeInternalContents(current1, current1Scope),
 				getScopeInternalContents(current2, current2Scope), structuredOptions.getSearchWindow(),
 				monitor);
 		// We can map other elements with mapLists; we iterate through them.
 		final Iterator<Match2Elements> it = mapList.iterator();
 		while (it.hasNext()) {
 			final Match2Elements subMapping = it.next();
 			// As we know source and target are similars, we call recursive
 			// mappings onto these objects
 			EFactory.eAdd(mapping, SUBMATCH_ELEMENT_NAME, recursiveMappings(subMapping.getLeftElement(),
 					current1Scope, subMapping.getRightElement(), current2Scope, monitor));
 		}
 
 		// we also have to match those elements, which are directly referenced but not contained in the
 		// specified match scope. Otherwise the diff engine will detect reference changes also in case
 		// there is no change
 		final List<EObject> current1ScopeExternalReferences = getScopeExternalReferences(current1,
 				current1Scope);
 		final List<EObject> current2ScopeExternalReferences = getScopeExternalReferences(current2,
 				current2Scope);
 		for (EObject leftRef : current1ScopeExternalReferences) {
 			final EObject rightRef = findMostSimilar(leftRef, current2ScopeExternalReferences);
 			if (rightRef != null && findMostSimilar(rightRef, current1ScopeExternalReferences) == leftRef) {
 				// create a mapping to indicate an exact match (as this is out of scope)
 				final Match2Elements externalRefMapping = MatchFactory.eINSTANCE.createMatch2Elements();
 				externalRefMapping.setLeftElement(leftRef);
 				externalRefMapping.setRightElement(rightRef);
 				externalRefMapping.setSimilarity(checker.absoluteMetric(leftRef, rightRef));
 				externalRefMappings.add(externalRefMapping);
 			}
 		}
 
 		return mapping;
 	}
 
 	/**
 	 * Obtain all EObjets, which are referenced by the given eObject ({@link EObject#getEAllReferences()}),
 	 * but are not part of the match scope.
 	 * 
 	 * @param eObject
 	 *            the eObject, whose references are to be regarded
 	 * @param scope
 	 *            the scope to decide whether the target of a given reference has to be included in the list
 	 *            of referenced objects or not
 	 * @return the list of all objects, referenced by the given one, which are not part of the scope
 	 */
 	@SuppressWarnings("unchecked")
 	private List<EObject> getScopeExternalReferences(EObject eObject, IMatchScope scope) {
 		final List<EObject> result = new ArrayList<EObject>();
 		// process all references to outside the scope
 		for (final EReference reference : eObject.eClass().getEAllReferences()) {
 			final Object value = eObject.eGet(reference);
 			if (value instanceof Collection) {
 				for (Object newValue : (Collection)value) {
 					if (!result.contains(newValue) && newValue instanceof EObject
 							&& !scope.isInScope((EObject)newValue))
 						result.add((EObject)newValue);
 				}
 			} else if (!result.contains(value) && value instanceof EObject
 					&& !scope.isInScope((EObject)value)) {
 				result.add((EObject)value);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * This method is an indirection for adding Mappings in the current MappingGroup.
 	 * 
 	 * @param object
 	 *            {@link EObject} to add a feature value to.
 	 * @param name
 	 *            Name of the feature to consider.
 	 * @param value
 	 *            Value to add to the feature <code>name</code> of <code>object</code>.
 	 * @throws FactoryException
 	 *             Thrown if the value's affectation fails.
 	 */
 	private void redirectedAdd(EObject object, String name, Object value) throws FactoryException {
 		final EStructuralFeature feature = object.eClass().getEStructuralFeature(name);
 		if (feature.isMany()) {
 			if (value != null) {
 				if (value instanceof EObject) {
 					((BasicEList)object.eGet(feature)).addUnique(value);
 				} else {
 					((List)object.eGet(feature)).add(value);
 				}
 			}
 		} else {
 			EFactory.eSet(object, name, value);
 		}
 	}
 
 	/**
 	 * Sets the values of the {@link MatchModel}'s left and right models.
 	 * 
 	 * @param modelRoot
 	 *            Root of the {@link MatchModel}.
 	 * @param left
 	 *            Element from which to resolve the left model URI.
 	 * @param right
 	 *            Element from which to resolve the right model URI.
 	 * @since 1.1
 	 */
 	protected void setModelRoots(MatchModel modelRoot, EObject left, EObject right) {
 		setModelRoots(modelRoot, left, right, null);
 	}
 
 	/**
 	 * Sets the values of the {@link MatchModel}'s left, right and ancestor models.
 	 * 
 	 * @param modelRoot
 	 *            Root of the {@link MatchModel}.
 	 * @param left
 	 *            Element from which to resolve the left model URI.
 	 * @param right
 	 *            Element from which to resolve the right model URI.
 	 * @param ancestor
 	 *            Element from which to resolve the ancestor model URI. Can be <code>null</code>.
 	 * @since 1.1
 	 */
 	protected void setModelRoots(MatchModel modelRoot, EObject left, EObject right, EObject ancestor) {
 		// Sets values of left, right and ancestor model roots
 		if (left != null && left.eResource() != null) {
 			modelRoot.getLeftRoots().addAll(left.eResource().getContents());
 		}
 		if (right != null && right.eResource() != null) {
 			modelRoot.getRightRoots().addAll(right.eResource().getContents());
 		}
 		if (ancestor != null && ancestor.eResource() != null) {
 			modelRoot.getAncestorRoots().addAll(ancestor.eResource().getContents());
 		}
 	}
 
 	/**
 	 * Starts the monitor for comparison progress. Externalized here to avoid multiple usage of the Strings.
 	 * 
 	 * @param monitor
 	 *            The monitor that need be started
 	 * @param size
 	 *            Size of the monitor
 	 */
 	private void startMonitor(Monitor monitor, int size) {
 		monitor.beginTask(EMFCompareMatchMessages.getString("DifferencesServices.monitor.task"), size); //$NON-NLS-1$
 		monitor.subTask(EMFCompareMatchMessages.getString("DifferencesServices.monitor.browsing")); //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns <code>True</code> if the 2 given {@link EObject}s are considered similar.
 	 * 
 	 * @param obj1
 	 *            The first {@link EObject} to compare.
 	 * @param obj2
 	 *            Second of the {@link EObject}s to compare.
 	 * @return <code>True</code> if both elements have the same serialization ID, <code>False</code>
 	 *         otherwise.
 	 * @throws FactoryException
 	 *             Thrown if we cannot compute one of the needed similarity.
 	 */
 	protected boolean isSimilar(EObject obj1, EObject obj2) throws FactoryException {
 		return checker.isSimilar(obj1, obj2);
 	}
 
 	/**
 	 * Workaround for bug #235606 : elements held by a reference with containment=true and derived=true are
 	 * not matched since not returned by {@link EObject#eContents()}. This allows us to return the list of all
 	 * contents from an EObject <u>including</u> those references.
 	 * 
 	 * @param eObject
 	 *            The EObject we seek the content of.
 	 * @param scope
 	 *            The scope to restrict the contents.
 	 * @return The list of all the content of a given EObject, derived containmnent references included.
 	 * @since 1.1
 	 */
 	@SuppressWarnings("unchecked")
 	protected List<EObject> getScopeInternalContents(EObject eObject, IMatchScope scope) {
 		// filter out those contained objects belonging to a fragment resource
 		final List<EObject> result = new ArrayList<EObject>();
 		// add contents within scope
 		for (EObject contents : eObject.eContents()) {
 			// only add direct "non-fragment" contents
 			if (!result.contains(contents) && scope.isInScope(contents)) {
 				result.add(contents);
 			}
 		}
 		// add derived containment references in scope (do not objects contained in fragments)
 		for (final EReference reference : eObject.eClass().getEAllReferences()) {
 			if (reference.isContainment() && reference.isDerived()) {
 				final Object value = eObject.eGet(reference);
 				if (value instanceof Collection) {
 					for (Object contents : (Collection)value) {
 						if (!result.contains(contents) && contents instanceof EObject) {
 							final EObject object = (EObject)contents;
 							if (scope.isInScope(object)) {
 								result.add(object);
 							}
 						}
 					}
 				} else if (!result.contains(value) && value instanceof EObject) {
 					final EObject object = (EObject)value;
 					if (scope.isInScope(object)) {
 						result.add(object);
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Returns all objects contained in the given resource (via {@link Resource#getContents()}), covered by
 	 * the provided scope.
 	 * 
 	 * @param resource
 	 *            The resource, whose contents is to be regarded.
 	 * @param scope
 	 *            The scope to restrict, which of the contents' objects is included in the returned list.
 	 * @return The list of conents' objects, covered within the given scope.
 	 */
 	private List<EObject> getScopeInternalContents(Resource resource, IMatchScope scope) {
 		final List<EObject> result = new ArrayList<EObject>();
 		for (EObject contents : resource.getContents()) {
 			if (!result.contains(contents) && scope.isInScope(contents)) {
 				result.add(contents);
 			}
 		}
 		return result;
 	}
 }
