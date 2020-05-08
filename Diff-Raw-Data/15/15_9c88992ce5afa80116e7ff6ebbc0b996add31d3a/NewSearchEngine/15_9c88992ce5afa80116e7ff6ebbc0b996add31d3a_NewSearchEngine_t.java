 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Zend Technologies
  *******************************************************************************/
 package org.eclipse.dltk.core.index2.search;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.core.index2.search.ISearchEngine.MatchRule;
 import org.eclipse.dltk.core.index2.search.ISearchEngine.SearchFor;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchDocument;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.core.search.SearchParticipant;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.core.search.SearchRequestor;
 import org.eclipse.dltk.core.search.matching.ModuleFactory;
 import org.eclipse.dltk.internal.core.DefaultWorkingCopyOwner;
 import org.eclipse.dltk.internal.core.ModelManager;
 import org.eclipse.dltk.internal.core.SourceModule;
 import org.eclipse.dltk.internal.core.search.matching.AndPattern;
 import org.eclipse.dltk.internal.core.search.matching.FieldPattern;
 import org.eclipse.dltk.internal.core.search.matching.MethodDeclarationPattern;
 import org.eclipse.dltk.internal.core.search.matching.MethodPattern;
 import org.eclipse.dltk.internal.core.search.matching.OrPattern;
 import org.eclipse.dltk.internal.core.search.matching.TypeDeclarationPattern;
 import org.eclipse.dltk.internal.core.search.matching.TypeReferencePattern;
 import org.eclipse.dltk.internal.core.util.Messages;
 
 /**
  * This is an implementation of search engine using new indexing infrastructure.
  * 
  * @author michael
  * @since 2.0
  * 
  */
 public class NewSearchEngine {
 
 	/**
 	 * A list of working copies that take precedence over their original
 	 * compilation units.
 	 */
 	private ISourceModule[] workingCopies;
 
 	/**
 	 * A working copy owner whose working copies will take precedent over their
 	 * original compilation units.
 	 */
 	private WorkingCopyOwner workingCopyOwner;
 
 	/**
 	 * Searches for matches of a given search pattern. Search patterns can be
 	 * created using helper methods (from a String pattern or a Script element)
 	 * and encapsulate the description of what is being searched (for example,
 	 * search method declarations in a case sensitive way).
 	 * 
 	 * @see SearchEngine#search(SearchPattern, SearchParticipant[],
 	 *      IJavaSearchScope, SearchRequestor, IProgressMonitor) for detailed
 	 *      comment
 	 */
 	public void search(SearchPattern pattern, SearchParticipant[] participants,
 			IDLTKSearchScope scope, SearchRequestor requestor,
 			IProgressMonitor monitor) throws CoreException {
 		findMatches(pattern, participants, scope, requestor, monitor);
 	}
 
 	/**
 	 * Searches for matches to a given query. Search queries can be created
 	 * using helper methods (from a String pattern or a Script element) and
 	 * encapsulate the description of what is being searched (for example,
 	 * search method declarations in a case sensitive way).
 	 * 
 	 * @param pattern
 	 *            Old-style search pattern
 	 * @param participants
 	 *            Search participants array
 	 * @param scope
 	 *            the search result has to be limited to the given scope
 	 * @param requestor
 	 *            a callback object to which each match is reported
 	 * @param monitor
 	 *            Progress monitor
 	 */
 	void findMatches(SearchPattern pattern, SearchParticipant[] participants,
 			IDLTKSearchScope scope, SearchRequestor requestor,
 			IProgressMonitor monitor) throws CoreException {
 
 		if (monitor != null && monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 
 		if (participants == null) {
 			return;
 		}
 
 		int length = participants.length;
 		if (monitor != null)
 			monitor.beginTask(Messages.engine_searching, 100 * length);
 
 		try {
 			requestor.beginReporting();
 			for (int i = 0; i < participants.length; i++) {
 				SearchParticipant participant = participants[i];
 				if (monitor != null && monitor.isCanceled()) {
 					throw new OperationCanceledException();
 				}
 
 				try {
 					if (monitor != null)
 						monitor.subTask(Messages.bind(
 								Messages.engine_searching_indexing,
 								new String[] { participant.getDescription() }));
 
 					participant.beginSearching();
 					requestor.enterParticipant(participant);
 
 					Set<String> indexMatchPathSet = new HashSet<String>();
 					collectPaths(pattern, scope, indexMatchPathSet, monitor);
 					String[] indexMatchPaths = indexMatchPathSet
 							.toArray(new String[indexMatchPathSet.size()]);
 
 					if (monitor != null && monitor.isCanceled())
 						throw new OperationCanceledException();
 
 					// locate index matches if any (note that all search matches
 					// could have been issued during index querying)
 					if (monitor != null)
 						monitor.subTask(Messages.bind(
 								Messages.engine_searching_matching,
 								new String[] { participant.getDescription() }));
 
 					if (indexMatchPaths.length != 0) {
 						int indexMatchLength = indexMatchPaths.length;
 						SearchDocument[] indexMatches = new SearchDocument[indexMatchLength];
 						for (int j = 0; j < indexMatchLength; j++) {
 							indexMatches[j] = participant.getDocument(
 									indexMatchPaths[j], null);
 						}
 						SearchDocument[] matches = ModuleFactory
 								.addWorkingCopies(pattern, indexMatches,
 										getWorkingCopies(), participant);
 
 						final Set<IPath> paths = new HashSet<IPath>();
 						List<SearchDocument> filteredMatches = new ArrayList<SearchDocument>();
 						for (int q = 0; q < matches.length; ++q) {
 							IPath path = new Path(matches[q].getPath());
 							if (paths.add(path)) {
 								filteredMatches.add(matches[q]);
 							}
 						}
 						SearchDocument[] fmatches = filteredMatches
 								.toArray(new SearchDocument[filteredMatches
 										.size()]);
 
 						participant.locateMatches(fmatches, pattern, scope,
 								requestor, monitor == null ? null
 										: new SubProgressMonitor(monitor, 50));
 					}
 				} catch (Exception e) {
 					DLTKCore.error(
 							"An exception was thrown when locating matches", e); //$NON-NLS-1$
 				} finally {
 					requestor.exitParticipant(participant);
 					participant.doneSearching();
 				}
 			}
 		} catch (Exception e) {
 			DLTKCore.error("An exception was thrown when locating matches", e); //$NON-NLS-1$
 		} finally {
 			requestor.endReporting();
 			if (monitor != null) {
 				monitor.done();
 			}
 		}
 	}
 
 	protected void collectPaths(SearchPattern pattern, IDLTKSearchScope scope,
 			final Collection<String> paths, IProgressMonitor monitor) {
 
 		int elementType = 0;
 		String qualifier = null;
 		String elementName = null;
 		SearchFor searchFor = null;
 		MatchRule matchRule = null;
 
 		if (pattern instanceof TypeDeclarationPattern) {
 			elementType = IModelElement.TYPE;
 			elementName = new String(
 					((TypeDeclarationPattern) pattern).simpleName);
 			matchRule = ModelAccess.convertSearchRule(pattern.getMatchRule());
 			searchFor = SearchFor.DECLARATIONS;
 
 		} else if (pattern instanceof TypeReferencePattern) {
 			elementType = IModelElement.TYPE;
 			elementName = new String(
 					((TypeReferencePattern) pattern).simpleName);
 			matchRule = ModelAccess.convertSearchRule(pattern.getMatchRule());
 			searchFor = SearchFor.REFERENCES;
 
 		} else if (pattern instanceof MethodDeclarationPattern) {
 			elementType = IModelElement.METHOD;
 			elementName = new String(
 					((MethodDeclarationPattern) pattern).simpleName);
 			matchRule = ModelAccess.convertSearchRule(pattern.getMatchRule());
 			searchFor = SearchFor.DECLARATIONS;
 
 		} else if (pattern instanceof MethodPattern) {
 			elementType = IModelElement.METHOD;
 			MethodPattern methodPattern = (MethodPattern) pattern;
 			elementName = new String(methodPattern.selector);
 			matchRule = ModelAccess.convertSearchRule(pattern.getMatchRule());
 			searchFor = SearchFor.REFERENCES;
 
 		} else if (pattern instanceof FieldPattern) {
 			elementType = IModelElement.FIELD;
 			FieldPattern fieldPattern = (FieldPattern) pattern;
 			elementName = new String(fieldPattern.name);
 			matchRule = ModelAccess.convertSearchRule(pattern.getMatchRule());
 			if (fieldPattern.findDeclarations && fieldPattern.findReferences) {
				searchFor = SearchFor.ALL_OCCURRENCES;
 			} else if (fieldPattern.findDeclarations) {
 				searchFor = SearchFor.DECLARATIONS;
 			} else if (fieldPattern.findReferences) {
 				searchFor = SearchFor.REFERENCES;
 			}
 
 		} else if (pattern instanceof AndPattern) {
 			AndPattern andPattern = (AndPattern) pattern;
 			do {
 				SearchPattern p = andPattern.currentPattern();
 				collectPaths(p, scope, paths, monitor);
 			} while (andPattern.hasNextQuery());
 
 		} else if (pattern instanceof OrPattern) {
 			OrPattern orPattern = (OrPattern) pattern;
 			for (SearchPattern p : orPattern.getPatterns()) {
 				collectPaths(p, scope, paths, monitor);
 			}
 
 		}
 
 		if (elementType > 0 && elementName != null && searchFor != null
 				&& matchRule != null) {
 			ISearchEngine searchEngine = ModelAccess.getSearchEngine(scope
 					.getLanguageToolkit());
 
 			if (searchEngine != null) {
 				ISearchRequestor requestor = new ISearchRequestor() {
 					public void match(int elementType, int flags, int offset,
 							int length, int nameOffset, int nameLength,
 							String elementName, String metadata, String doc,
 							String qualifier, String parent,
 							ISourceModule sourceModule, boolean isReference) {
 
 						paths.add(sourceModule.getPath().toString());
 					}
 				};
 
 				searchEngine.search(elementType, qualifier, elementName, 0, 0,
 						0, searchFor, matchRule, scope, requestor, monitor);
 
 				if (matchRule == MatchRule.CAMEL_CASE) {
 					// Search also for prefix (workaround to the way original
 					// search engine worked)
 					searchEngine.search(elementType, qualifier, elementName, 0,
 							0, 0, searchFor, MatchRule.PREFIX, scope,
 							requestor, monitor);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Returns the list of working copies used by this search engine. Returns
 	 * null if none.
 	 */
 	private ISourceModule[] getWorkingCopies() {
 		ISourceModule[] copies;
 		if (this.workingCopies != null) {
 			if (this.workingCopyOwner == null) {
 				copies = ModelManager.getModelManager().getWorkingCopies(
 						DefaultWorkingCopyOwner.PRIMARY, false/*
 															 * don't add primary
 															 * WCs a second time
 															 */);
 				if (copies == null) {
 					copies = this.workingCopies;
 				} else {
 					Map<IPath, ISourceModule> pathToCUs = new HashMap<IPath, ISourceModule>();
 					for (int i = 0; i < copies.length; i++) {
 						ISourceModule unit = copies[i];
 						pathToCUs.put(unit.getPath(), unit);
 
 					}
 					for (int i = 0; i < this.workingCopies.length; i++) {
 						ISourceModule unit = this.workingCopies[i];
 						pathToCUs.put(unit.getPath(), unit);
 					}
 					int length = pathToCUs.size();
 					copies = new ISourceModule[length];
 					pathToCUs.values().toArray(copies);
 				}
 			} else {
 				copies = this.workingCopies;
 			}
 		} else if (this.workingCopyOwner != null) {
 			copies = ModelManager.getModelManager().getWorkingCopies(
 					this.workingCopyOwner, true/* add primary WCs */);
 		} else {
 			copies = ModelManager.getModelManager().getWorkingCopies(
 					DefaultWorkingCopyOwner.PRIMARY, false/*
 														 * don't add primary WCs
 														 * a second time
 														 */);
 		}
 		if (copies == null) {
 			return null;
 		}
 
 		// filter out primary working copies that are saved
 		ISourceModule[] result = null;
 		int length = copies.length;
 		int index = 0;
 		for (int i = 0; i < length; i++) {
 			SourceModule copy = (SourceModule) copies[i];
 			try {
 				if (!copy.isPrimary() || copy.hasUnsavedChanges()
 						|| copy.hasResourceChanged()) {
 					if (result == null) {
 						result = new ISourceModule[length];
 					}
 					result[index++] = copy;
 				}
 			} catch (ModelException e) {
 				// copy doesn't exist: ignore
 			}
 		}
 		if (index != length && result != null) {
 			System.arraycopy(result, 0, result = new ISourceModule[index], 0,
 					index);
 		}
 		return result;
 	}
 
 	public boolean isEnabled(IDLTKLanguageToolkit toolkit) {
 		/*
 		 * XXX indexer is contributed per language, while indexer and
 		 * searchEngine are global.
 		 */
 		return ModelAccess.getIndexerParticipant(toolkit) != null;
 	}
 }
