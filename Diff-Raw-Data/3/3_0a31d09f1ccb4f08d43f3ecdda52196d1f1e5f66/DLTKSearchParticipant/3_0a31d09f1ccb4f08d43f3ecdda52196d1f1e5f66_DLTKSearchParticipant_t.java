 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.core.search;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.search.index.Index;
 import org.eclipse.dltk.core.search.index.MixinIndex;
 import org.eclipse.dltk.core.search.indexing.IndexManager;
 import org.eclipse.dltk.core.search.matching.IMatchLocator;
 import org.eclipse.dltk.core.search.matching.MatchLocator;
 import org.eclipse.dltk.core.search.matching.MatchLocator.WorkingCopyDocument;
 import org.eclipse.dltk.internal.core.Model;
 import org.eclipse.dltk.internal.core.Openable;
 import org.eclipse.dltk.internal.core.search.IndexSelector;
 import org.eclipse.dltk.internal.core.search.LazyDLTKSearchDocument;
 import org.eclipse.dltk.internal.core.util.HandleFactory;
 import org.eclipse.dltk.internal.core.util.Util;
 
 /**
  * A search participant describes a particular extension to a generic search
  * mechanism, allowing thus to perform combined search actions which will
  * involve all required participants
  * 
  * A search scope defines which participants are involved.
  * 
  * A search participant is responsible for holding index files, and selecting
  * the appropriate ones to feed to index queries. It also can map a document
  * path to an actual document (note that documents could live outside the
  * workspace or no exist yet, and thus aren't just resources).
  */
 public class DLTKSearchParticipant extends SearchParticipant {
 	private IndexSelector indexSelector;
 	private boolean bOnlyMixin = false;
 
 	@Override
 	public void beginSearching() {
 		super.beginSearching();
 		this.indexSelector = null;
 	}
 
 	@Override
 	public void doneSearching() {
 		this.indexSelector = null;
 		super.doneSearching();
 	}
 
 	@Override
 	public String getDescription() {
 		return "DLTK"; //$NON-NLS-1$
 	}
 
 	@Override
 	public SearchDocument getDocument(String documentPath, IProject project) {
 		return new LazyDLTKSearchDocument(documentPath, this,
 				isExternal(documentPath), project);
 	}
 
 	private boolean isExternal(String documentPath) {
 		Object target = Model.getTarget(ResourcesPlugin.getWorkspace()
 				.getRoot(), new Path(documentPath), true);
 		if (target instanceof IResource)
 			return false;
 		else
 			return true;
 
 	}
 
 	@Override
 	public void indexDocument(SearchDocument document, IPath indexPath) {
 		// TODO must verify that the document + indexPath match, when this is
 		// not called from scheduleDocumentIndexing
 		document.removeAllIndexEntries(); // in case the document was already
 		// indexed
 	}
 
 	@Override
 	public void locateMatches(SearchDocument[] indexMatches,
 			SearchPattern pattern, IDLTKSearchScope scope,
 			SearchRequestor requestor, IProgressMonitor monitor)
 			throws CoreException {
 		IMatchLocator matchLocator = createMatchLocator(scope
 				.getLanguageToolkit());
		matchLocator.initialize(pattern, scope);
		matchLocator.setRequestor(requestor);
 		matchLocator.setProgressMonitor(monitor == null ? null
 				: new SubProgressMonitor(monitor, 95));
 		/* eliminating false matches and locating them */
 		if (monitor != null && monitor.isCanceled())
 			throw new OperationCanceledException();
 		matchLocator.locateMatches(indexMatches);
 		if (monitor != null && monitor.isCanceled())
 			throw new OperationCanceledException();
 		// matchLocator.locatePackageDeclarations(this);
 	}
 
 	@Override
 	public ISourceModule[] locateModules(SearchDocument[] indexMatches,
 			IDLTKSearchScope scope, IProgressMonitor monitor)
 			throws CoreException {
 		if (monitor != null && monitor.isCanceled())
 			throw new OperationCanceledException();
 		return doLocateModules(indexMatches, scope, monitor);
 	}
 
 	private ISourceModule[] doLocateModules(SearchDocument[] searchDocuments,
 			IDLTKSearchScope scope, IProgressMonitor progressMonitor) {
 		final List<ISourceModule> modules = new ArrayList<ISourceModule>(
 				searchDocuments.length);
 		int docsLength = searchDocuments.length;
 		if (BasicSearchEngine.VERBOSE) {
 			System.out.println("Locating matches in documents ["); //$NON-NLS-1$
 			for (int i = 0; i < docsLength; i++)
 				System.out.println("\t" + searchDocuments[i]); //$NON-NLS-1$
 			System.out.println("]"); //$NON-NLS-1$
 		}
 		// init infos for progress increasing
 		int n = docsLength < 1000 ? Math.min(Math.max(docsLength / 200 + 1, 2),
 				4) : 5 * (docsLength / 1000);
 		// step should not be 0
 		final int progressStep = docsLength < n ? 1 : docsLength / n;
 		int progressWorked = 0;
 		// initialize handle factory
 		final HandleFactory handleFactory = new HandleFactory();
 		if (progressMonitor != null) {
 			progressMonitor.beginTask("", searchDocuments.length); //$NON-NLS-1$
 		}
 		Util.sort(searchDocuments, new Util.Comparer() {
 			public int compare(Object a, Object b) {
 				return ((SearchDocument) a).getPath().compareTo(
 						((SearchDocument) b).getPath());
 			}
 		});
 		String previousPath = null;
 		for (int i = 0; i < docsLength; i++) {
 			if (progressMonitor != null && progressMonitor.isCanceled()) {
 				throw new OperationCanceledException();
 			}
 			if (progressMonitor != null) {
 				progressWorked++;
 				if ((progressWorked % progressStep) == 0)
 					progressMonitor.worked(progressStep);
 			}
 			SearchDocument searchDocument = searchDocuments[i];
 			searchDocuments[i] = null; // free current document
 			String pathString = searchDocument.getPath();
 			// skip duplicate paths
 			if (i > 0 && pathString.equals(previousPath)) {
 				continue;
 			}
 			previousPath = pathString;
 			Openable openable;
 			if (searchDocument instanceof WorkingCopyDocument) {
 				openable = (Openable) ((WorkingCopyDocument) searchDocument).workingCopy;
 			} else {
 				openable = handleFactory.createOpenable(pathString, scope);
 			}
 			if (openable == null) {
 				continue; // match is outside buildpath
 			}
 			modules.add((ISourceModule) openable);
 		}
 		if (progressMonitor != null)
 			progressMonitor.done();
 		return modules.toArray(new ISourceModule[modules.size()]);
 	}
 
 	protected IMatchLocator createMatchLocator(IDLTKLanguageToolkit toolkit) {
 		if (toolkit != null) {
 			return DLTKLanguageManager
 					.createMatchLocator(toolkit.getNatureId());
 		} else {
 			return new MatchLocator();
 		}
 	}
 
 	@Override
 	public IPath[] selectIndexes(SearchPattern pattern, IDLTKSearchScope scope) {
 		if (this.indexSelector == null) {
 			this.indexSelector = new IndexSelector(scope, pattern);
 			this.indexSelector.setMixinOnly(this.bOnlyMixin);
 		}
 		return this.indexSelector.getIndexLocations();
 	}
 
 	@Override
 	public IPath[] selectMixinIndexes(SearchPattern query,
 			IDLTKSearchScope scope) {
 		this.skipNotMixin();
 		return selectIndexes(query, scope);
 	}
 
 	@Override
 	public void skipNotMixin() {
 		this.bOnlyMixin = true;
 	}
 
 	@Override
 	public boolean isSkipped(Index index) {
 		final boolean mixinIndex = index instanceof MixinIndex
 				|| index.containerPath.startsWith(IndexManager.SPECIAL_MIXIN);
 		return this.bOnlyMixin != mixinIndex;
 	}
 }
