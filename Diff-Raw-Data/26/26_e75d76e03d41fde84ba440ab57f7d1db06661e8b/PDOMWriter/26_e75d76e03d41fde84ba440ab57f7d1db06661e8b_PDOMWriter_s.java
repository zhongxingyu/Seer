 /*******************************************************************************
  * Copyright (c) 2007 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Markus Schorn - initial API and implementation
  *******************************************************************************/ 
 
 package org.eclipse.cdt.internal.core.pdom;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
 import org.eclipse.cdt.core.dom.ast.IASTName;
 import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
 import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
 import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
 import org.eclipse.cdt.core.dom.ast.IBinding;
 import org.eclipse.cdt.core.dom.ast.IProblemBinding;
 import org.eclipse.cdt.core.index.IIndexFile;
 import org.eclipse.cdt.core.index.IIndexFileLocation;
 import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
 import org.eclipse.cdt.internal.core.index.IWritableIndex;
 import org.eclipse.cdt.internal.core.pdom.dom.PDOMASTAdapter;
 import org.eclipse.cdt.internal.core.pdom.indexer.IndexerASTVisitor;
 import org.eclipse.cdt.internal.core.pdom.indexer.IndexerStatistics;
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 
 /**
  * Abstract class to write information from AST 
  * @since 4.0
  */
 abstract public class PDOMWriter {
 	protected boolean fShowActivity;
 	protected boolean fShowProblems;
 	protected IndexerStatistics fStatistics;
 	
 	private IndexerProgress fInfo= new IndexerProgress();
 
 	public PDOMWriter() {
 		fStatistics= new IndexerStatistics();
 	}
 	
 	public void setShowActivity(boolean val) {
 		fShowActivity= val;
 	}
 	
 	public void setShowProblems(boolean val) {
 		fShowProblems= val;
 	}
 	
 	/**
 	 * Called to check whether a translation unit still needs to be updated.
 	 * @see #addSymbols(IASTTranslationUnit, IWritableIndex, int, IProgressMonitor)
 	 * @since 4.0
 	 */
 	protected abstract boolean needToUpdate(IIndexFileLocation location) throws CoreException;
 
 	/**
 	 * Called after a file was added to the index. 
 	 * @return whether the file was actually requested by the indexer.
 	 * @see #addSymbols(IASTTranslationUnit, IWritableIndex, int, IProgressMonitor)
 	 * @since 4.0
 	 */
 	protected abstract boolean postAddToIndex(IIndexFileLocation location, IIndexFile file) throws CoreException;
 
 	/**
 	 * Called to resolve an absolute path to an index file location. 
 	 * @since 4.0
 	 */
 	protected abstract IIndexFileLocation findLocation(String absolutePath);
 	
 	/**
 	 * Extracts symbols from the given ast and adds them to the index. It will
 	 * make calls to 	  
 	 * {@link #needToUpdate(IIndexFileLocation)},
	 * {@link #postAddToIndex(IIndexFileLocation, IIndexFile)} and
 	 * {@link #findLocation(String)} to obtain further information.
 	 * @since 4.0
 	 */
 	public void addSymbols(IASTTranslationUnit ast, IWritableIndex index, int readlockCount, IProgressMonitor pm) throws InterruptedException, CoreException {
 		final Map symbolMap= new HashMap();
 		try {
 			IIndexFileLocation[] orderedPaths= extractSymbols(ast, symbolMap);
 			for (int i=0; i<orderedPaths.length; i++) {
 				if (pm.isCanceled()) {
 					return;
 				}
 				IIndexFileLocation path= orderedPaths[i];
 				ArrayList[] arrayLists = ((ArrayList[]) symbolMap.get(path));
 
 				// resolve the names
 				long start= System.currentTimeMillis();
 				ArrayList names= arrayLists[2];
 				for (int j=0; j<names.size(); j++) {
 					final IASTName name = ((IASTName[]) names.get(j))[0];
 					final IBinding binding= name.resolveBinding();
 					if (binding instanceof IProblemBinding)
 						reportProblem((IProblemBinding) binding);
 					else if (name.isReference()) 
 						fStatistics.fReferenceCount++;
 					else 
 						fStatistics.fDeclarationCount++;
 				}
 				fStatistics.fResolutionTime += System.currentTimeMillis()-start;
 			}
 
 			boolean isFirstRequest= true;
 			boolean isFirstAddition= true;
 			index.acquireWriteLock(readlockCount);
 			long start= System.currentTimeMillis();
 			try {
 				for (int i=0; i<orderedPaths.length; i++) {
 					if (pm.isCanceled()) 
 						return;
 
 					IIndexFileLocation path = orderedPaths[i];
 					if (path != null) {
 						if (fShowActivity) {
 							System.out.println("Indexer: adding " + path.getURI()); //$NON-NLS-1$
 						}
 						IIndexFile file= addToIndex(index, path, symbolMap);
 						boolean wasRequested= postAddToIndex(path, file);
 
 						synchronized(fInfo) {
 							if (wasRequested) {
 								if (isFirstRequest) 
 									isFirstRequest= false;
 								else 
 									fInfo.fTotalSourcesEstimate--;
 							}
 							if (isFirstAddition) 
 								isFirstAddition= false;
 							else
 								fInfo.fCompletedHeaders++;
 						}
 					}
 				}
 			} finally {
 				index.releaseWriteLock(readlockCount);
 			}
 			fStatistics.fAddToIndexTime+= System.currentTimeMillis()-start;
 		}
 		finally {
 			synchronized(fInfo) {
 				fInfo.fCompletedSources++;
 			}
 		}
 	}
 
 	private IIndexFileLocation[] extractSymbols(IASTTranslationUnit ast, final Map symbolMap) throws CoreException {
 		LinkedHashSet/*<IIndexFileLocation>*/ orderedIncludes= new LinkedHashSet/*<IIndexFileLocation>*/();
 		ArrayList/*<IIndexFileLocation>*/ stack= new ArrayList/*<IIndexFileLocation>*/();
 
 
 		final IIndexFileLocation astLocation = findLocation(ast.getFilePath());
 		IIndexFileLocation currentPath = astLocation;
 
 		IASTPreprocessorIncludeStatement[] includes = ast.getIncludeDirectives();
 		for (int i= 0; i < includes.length; i++) {
 			IASTPreprocessorIncludeStatement include = includes[i];
 			IASTFileLocation sourceLoc = include.getFileLocation();
 			IIndexFileLocation newPath= sourceLoc != null ? findLocation(sourceLoc.getFileName()) : astLocation; // command-line includes
 			while (!stack.isEmpty() && !currentPath.equals(newPath)) {
 				if (needToUpdate(currentPath)) {
 					prepareInMap(symbolMap, currentPath);
 					orderedIncludes.add(currentPath);
 				}
 				currentPath= (IIndexFileLocation) stack.remove(stack.size()-1);
 			}
 			if (needToUpdate(newPath)) {
 				prepareInMap(symbolMap, newPath);
 				addToMap(symbolMap, 0, newPath, include);
 			}
 			stack.add(currentPath);
 			currentPath= findLocation(include.getPath());
 		}
 		stack.add(currentPath);
 		while (!stack.isEmpty()) {
 			currentPath= (IIndexFileLocation) stack.remove(stack.size()-1);
 			if (needToUpdate(currentPath)) {
 				prepareInMap(symbolMap, currentPath);
 				orderedIncludes.add(currentPath);
 			}
 		}
 
 		// macros
 		IASTPreprocessorMacroDefinition[] macros = ast.getMacroDefinitions();
 		for (int i2 = 0; i2 < macros.length; ++i2) {
 			IASTPreprocessorMacroDefinition macro = macros[i2];
 			IASTFileLocation sourceLoc = macro.getFileLocation();
 			if (sourceLoc != null) { // skip built-ins and command line macros
 				IIndexFileLocation path2 = findLocation(sourceLoc.getFileName());
 				addToMap(symbolMap, 1, path2, macro);
 			}
 		}
 
 		// names
 		ast.accept(new IndexerASTVisitor() {
 			public void visit(IASTName name, IASTName caller) {
 				// assign a location to anonymous types.
 				name= PDOMASTAdapter.getAdapterIfAnonymous(name);
 				IASTFileLocation nameLoc = name.getFileLocation();
 				
 				if (nameLoc != null) {
 					IIndexFileLocation location = findLocation(nameLoc.getFileName());
 					addToMap(symbolMap, 2, location, new IASTName[]{name, caller});
 				}
 			}
 		});
 		return (IIndexFileLocation[]) orderedIncludes.toArray(new IIndexFileLocation[orderedIncludes.size()]);
 	}
 	
 	private void reportProblem(IProblemBinding problem) {
 		fStatistics.fProblemBindingCount++;
 		if (fShowProblems) {
 			String msg= "Indexer problem at "+ problem.getFileName() + ": " + problem.getLineNumber();  //$NON-NLS-1$//$NON-NLS-2$
 			String pmsg= problem.getMessage();
 			if (pmsg != null && pmsg.length() > 0) 
 				msg+= "; " + problem.getMessage(); //$NON-NLS-1$
 			System.out.println(msg);
 		}
 	}
 
 
 	private void addToMap(Map map, int idx, IIndexFileLocation location, Object thing) {
 		List[] lists= (List[]) map.get(location);
 		if (lists != null) 
 			lists[idx].add(thing);
 	}		
 
 	private boolean prepareInMap(Map map, IIndexFileLocation location) {
 		if (map.get(location) == null) {
 			Object lists= new ArrayList[]{new ArrayList(), new ArrayList(), new ArrayList()};
 			map.put(location, lists);
 		}
 		return false;
 	}
 
 	private IIndexFragmentFile addToIndex(IWritableIndex index, IIndexFileLocation location, Map symbolMap) throws CoreException {
 		IIndexFragmentFile file= (IIndexFragmentFile) index.getFile(location);
 		if (file != null) {
 			index.clearFile(file);
 		} else {
 			file= index.addFile(location);
 		}
		file.setTimestamp(EFS.getStore(location.getURI()).fetchInfo().getLastModified());
 		ArrayList[] lists= (ArrayList[]) symbolMap.get(location);
 		if (lists != null) {
 			ArrayList list= lists[0];
 			IASTPreprocessorIncludeStatement[] includes= (IASTPreprocessorIncludeStatement[]) list.toArray(new IASTPreprocessorIncludeStatement[list.size()]);
 			list= lists[1];
 			IASTPreprocessorMacroDefinition[] macros= (IASTPreprocessorMacroDefinition[]) list.toArray(new IASTPreprocessorMacroDefinition[list.size()]);
 			list= lists[2];
 			IASTName[][] names= (IASTName[][]) list.toArray(new IASTName[list.size()][]);
 
 			IIndexFileLocation[] includeLocations = new IIndexFileLocation[includes.length];
 			for(int i=0; i<includes.length; i++) {
 				includeLocations[i] = findLocation(includes[i].getPath());
 			}
 			index.setFileContent(file, includes, includeLocations, macros, names);
 		}
 		return file;
 	}
 	
 	/**
 	 * Makes a copy of the current progress information and returns it.
 	 * @since 4.0
 	 */
 	protected IndexerProgress getProgressInformation() {
 		synchronized (fInfo) {
 			return new IndexerProgress(fInfo);
 		}
 	}
 
 	/**
 	 * Updates current progress information with the provided delta.
 	 * @since 4.0
 	 */
 	protected void updateInfo(int completedSources, int completedHeaders, int totalEstimate) {
 		synchronized(fInfo) {
 			fInfo.fCompletedHeaders+= completedHeaders;
 			fInfo.fCompletedSources+= completedSources;
 			fInfo.fTotalSourcesEstimate+= totalEstimate;
 		}
 	}
 }
