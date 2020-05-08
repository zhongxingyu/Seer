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
 package org.eclipse.mylyn.docs.intent.collab.common.query;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.mylyn.docs.intent.collab.common.location.IntentLocations;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.core.indexer.IntentIndex;
 import org.eclipse.mylyn.docs.intent.core.indexer.IntentIndexEntry;
 import org.eclipse.mylyn.docs.intent.core.indexer.IntentIndexerFactory;
 
 /**
  * An utility class allowing to query the {@link IntentIndex}.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class IndexQuery extends AbstractIntentQuery {
 
 	private IntentIndex intentIndex;
 
 	/**
 	 * Creates the query.
 	 * 
 	 * @param repositoryAdapter
 	 *            the {@link RepositoryAdapter} to use for querying the repository.
 	 */
 	public IndexQuery(RepositoryAdapter repositoryAdapter) {
 		super(repositoryAdapter);
 	}
 
 	/**
 	 * Returns the {@link IntentIndex} of the queried repository. If none find, creates it.
 	 * 
 	 * @return the {@link IntentIndex} index of the queried repository. If none find, creates it
 	 */
 	public IntentIndex getOrCreateIntentIndex() {
 		if (intentIndex == null) {
 			final Resource indexResource = repositoryAdapter.getResource(IntentLocations.GENERAL_INDEX_PATH);
 			if (indexResource.getContents().isEmpty()) {
 				indexResource.getContents().add(IntentIndexerFactory.eINSTANCE.createIntentIndex());
 			}
 			intentIndex = (IntentIndex)indexResource.getContents().get(0);
 		}
 		return intentIndex;
 	}
 
 	/**
 	 * Returns the {@link IntentIndexEntry} located at the given level (e.g. "3.2.1"), or null if none found.
 	 * 
 	 * @param level
 	 *            the level of the searched {@link IntentIndexEntry} (e.g. "3.2.1")
 	 * @throws NumberFormatException
	 *             if the given level is not syntactically correct
 	 * @return the {@link IntentIndexEntry} located at the given level, or null if none found
 	 */
 	public IntentIndexEntry getIndexEntryAtLevel(String level) throws NumberFormatException {
 		IntentIndexEntry entryAtLevel = null;
 		IntentIndexEntry currentCandidate = null;
 
 		// We split the level to get each identifier separately
 		String[] levels = level.split(".");
 		try {
 
 			// Get the root index entry matching the first identifier
 			if (getOrCreateIntentIndex().getEntries().size() > Integer.valueOf(levels[0]) - 1) {
 				currentCandidate = getOrCreateIntentIndex().getEntries().get(Integer.valueOf(levels[0]) - 1);
 			}
 			int i = 1;
 			while (i < levels.length && entryAtLevel == null && currentCandidate != null) {
 				// If the current candidate has the expected level, we stop
 				if (currentCandidate.getName().startsWith(level)) {
 					entryAtLevel = currentCandidate;
 				} else {
 					// Otherwise, we consider the next identifier
 					EList<IntentIndexEntry> entries = currentCandidate.getSubEntries();
 					int currentSearchedLevel = Integer.valueOf(levels[i]) - 1;
 					if (entries.size() > currentSearchedLevel) {
 						currentCandidate = entries.get(currentSearchedLevel);
 					}
 				}
 			}
 		} catch (NumberFormatException e) {
 			throw new NumberFormatException("Invalid level for search '" + level + "'");
 		}
 		return entryAtLevel;
 	}
 
 }
