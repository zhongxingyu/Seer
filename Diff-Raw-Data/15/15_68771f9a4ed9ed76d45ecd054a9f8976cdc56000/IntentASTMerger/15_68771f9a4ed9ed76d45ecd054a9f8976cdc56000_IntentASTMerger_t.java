 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.compare;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.diff.merge.service.MergeService;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffModel;
 import org.eclipse.emf.compare.diff.service.DiffService;
 import org.eclipse.emf.compare.match.MatchOptions;
 import org.eclipse.emf.compare.match.engine.IMatchScopeProvider;
 import org.eclipse.emf.compare.match.metamodel.MatchModel;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
 import org.eclipse.mylyn.docs.intent.compare.match.IntentMatchEngine;
 import org.eclipse.mylyn.docs.intent.compare.match.IntentScopeProvider;
 
 /**
  * Merges local and repository asts using EMF Compare.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class IntentASTMerger {
 
	private static final boolean OVERRIDE = false;
 
 	/**
 	 * Modify the repository elements according to the local elements ; this operation should occure during a
 	 * transaction and be committed to be effective.
 	 * 
 	 * @param localRoot
 	 *            the local element to commit
 	 * @param repositoryRoot
 	 *            the repository element to update
 	 * @throws MergingException
 	 *             if the mergin has encountered a problem.
 	 */
 	public void mergeFromLocalToRepository(EObject localRoot, EObject repositoryRoot) throws MergingException {
 		if (OVERRIDE) {
 			for (EStructuralFeature feature : repositoryRoot.eClass().getEAllStructuralFeatures()) {
 				repositoryRoot.eSet(feature, localRoot.eGet(feature));
 			}
 		} else {
 
 			// Step 0 : match preparation
 			// Step 0.1 : we create a sample resource and add the localRoot to
 			// its content.
 			Resource sampleResource = new ResourceImpl(URI.createURI("http://mysampleuri.com"));
 			sampleResource.getContents().add(localRoot);
 
 			// Step 0.2 Defining a scope provider
 			MatchModel match = null;
 			IMatchScopeProvider scopeProvider = new IntentScopeProvider(localRoot, repositoryRoot);
 			Map<String, Object> optionsMap = new HashMap<String, Object>();
 			optionsMap.put(MatchOptions.OPTION_MATCH_SCOPE_PROVIDER, scopeProvider);
 
 			// Step 1 : matching the local and the repository root using a custom
 			// MatcheEngine.
 			match = new IntentMatchEngine(localRoot, repositoryRoot).contentMatch(localRoot, repositoryRoot,
 					optionsMap);
 
 			DiffModel diff = DiffService.doDiff(match, false);
 			// Step 3 : Merges all differences from local to repository
 			List<DiffElement> differences = new ArrayList<DiffElement>(diff.getOwnedElements());
 
 			MergeService.merge(differences, true);
 		}
 	}
 
 	/**
 	 * For now on, does nothing.
 	 */
 	public void mergeFromRepositoryToLocal() {
 
 	}
 }
