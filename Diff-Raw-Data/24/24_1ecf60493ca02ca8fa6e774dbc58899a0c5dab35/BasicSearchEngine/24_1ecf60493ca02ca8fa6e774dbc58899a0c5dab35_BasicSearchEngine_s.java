 /*******************************************************************************
  * Copyright (c) 2000, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.core.search;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.dltk.ast.declarations.TypeDeclaration;
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IMember;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.core.search.indexing.IIndexConstants;
 import org.eclipse.dltk.core.search.indexing.IndexManager;
 import org.eclipse.dltk.core.search.matching.MatchLocator;
 import org.eclipse.dltk.internal.compiler.env.AccessRestriction;
 import org.eclipse.dltk.internal.compiler.env.AccessRuleSet;
 import org.eclipse.dltk.internal.core.DLTKProject;
 import org.eclipse.dltk.internal.core.DefaultWorkingCopyOwner;
 import org.eclipse.dltk.internal.core.ModelManager;
 import org.eclipse.dltk.internal.core.SourceModule;
 import org.eclipse.dltk.internal.core.search.DLTKSearchDocument;
 import org.eclipse.dltk.internal.core.search.DLTKSearchScope;
 import org.eclipse.dltk.internal.core.search.DLTKSearchTypeNameMatch;
 import org.eclipse.dltk.internal.core.search.IRestrictedAccessTypeRequestor;
 import org.eclipse.dltk.internal.core.search.IndexQueryRequestor;
 import org.eclipse.dltk.internal.core.search.PathCollector;
 import org.eclipse.dltk.internal.core.search.PatternSearchJob;
 import org.eclipse.dltk.internal.core.search.matching.DLTKSearchPattern;
 import org.eclipse.dltk.internal.core.search.matching.QualifiedTypeDeclarationPattern;
 import org.eclipse.dltk.internal.core.search.matching.TypeDeclarationPattern;
 import org.eclipse.dltk.internal.core.util.Messages;
 import org.eclipse.dltk.internal.core.util.Util;
 
 
 /**
  * Search basic engine. Public search engine now uses basic engine functionalities.
  * Note that serch basic engine does not implement deprecated functionalities...
  */
 public class BasicSearchEngine {
 
 	/*
 	 * A default parseor to parse non-reconciled working copies
 	 */
 		
 	/*
 	 * A list of working copies that take precedence over their original 
 	 * compilation units.
 	 */
 	private ISourceModule[] workingCopies;
 	
 	/*
 	 * A working copy owner whose working copies will take precedent over 
 	 * their original compilation units.
 	 */
 	private WorkingCopyOwner workingCopyOwner;
 
 	/**
 	 * For tracing purpose.
 	 */	
 	public static boolean VERBOSE = DLTKCore.VERBOSE_SEARCH;
 
 	/*
 	 * Creates a new search basic engine.
 	 */
 	public BasicSearchEngine() {
 		// will use working copies of PRIMARY owner
 	}
 	
 	/**
 	 * @see SearchEngine#SearchEngine(ISourceModule[]) for detailed comment.
 	 */
 	public BasicSearchEngine(ISourceModule[] workingCopies) {
 		this.workingCopies = workingCopies;
 	}
 
 	char convertTypeKind(int typeDeclarationKind) {
 		switch(typeDeclarationKind) {
 			case TypeDeclaration.D_CLASS : return IIndexConstants.TYPE_SUFFIX;			
 			default : return IIndexConstants.TYPE_SUFFIX;
 		}
 	}
 	/**
 	 * @see SearchEngine#SearchEngine(WorkingCopyOwner) for detailed comment.
 	 */
 	public BasicSearchEngine(WorkingCopyOwner workingCopyOwner) {
 		this.workingCopyOwner = workingCopyOwner;
 	}
 
 	/**
 	 * @see SearchEngine#createHierarchyScope(IType) for detailed comment.
 	 */
 	public static IDLTKSearchScope createHierarchyScope(IType type) throws ModelException {
 		return createHierarchyScope(type, DefaultWorkingCopyOwner.PRIMARY);
 	}
 	
 	/**
 	 * @see SearchEngine#createHierarchyScope(IType,WorkingCopyOwner) for detailed comment.
 	 */
 	public static IDLTKSearchScope createHierarchyScope(IType type, WorkingCopyOwner owner) throws ModelException {
 		//return new HierarchyScope(type, owner);
 		//TODO: Add HierarchyScope
 		return null;
 	}
 
 	/**
 	 * @see SearchEngine#createSearchScope(IModelElement[]) for detailed comment.
 	 */
 	public static IDLTKSearchScope createSearchScope(IModelElement[] elements) {
 		return createSearchScope(elements, true);
 	}
 
 	/**
 	 * @see SearchEngine#createSearchScope(IModelElement[], boolean) for detailed comment.
 	 */
 	public static IDLTKSearchScope createSearchScope(IModelElement[] elements, boolean includeReferencedProjects) {
 		int includeMask = IDLTKSearchScope.SOURCES | IDLTKSearchScope.APPLICATION_LIBRARIES | IDLTKSearchScope.SYSTEM_LIBRARIES;
 		if (includeReferencedProjects) {
 			includeMask |= IDLTKSearchScope.REFERENCED_PROJECTS;
 		}
 		return createSearchScope(elements, includeMask);
 	}
 
 	/**
 	 * @see SearchEngine#createSearchScope(IModelElement[], int) for detailed comment.
 	 */
 	public static IDLTKSearchScope createSearchScope(IModelElement[] elements, int includeMask) {
 		IDLTKLanguageToolkit toolkit = null;
 		if( elements.length > 0 ) {
 			try {
 				toolkit = DLTKLanguageManager.getLangaugeToolkit(elements[0]);
 			} catch (CoreException e) {
 				if( DLTKCore.DEBUG ) {
 					e.printStackTrace();
 				}
 				return null;
 			}
 		}
 		if( toolkit == null ) {			
 			return null;
 		}
 		DLTKSearchScope scope = new DLTKSearchScope(toolkit);
 		HashSet visitedProjects = new HashSet(2);
 		for (int i = 0, length = elements.length; i < length; i++) {
 			IModelElement element = elements[i];
 			if (element != null) {
 				try {
 					if (element instanceof DLTKProject) {
 						scope.add((DLTKProject)element, includeMask, visitedProjects);
 					} else {
 						scope.add(element);
 					}
 				} catch (ModelException e) {
 					// ignore
 				}
 			}
 		}
 		return scope;
 	}
 	
 	/**
 	 * @see SearchEngine#createTypeNameMatch(IType, int) for detailed comment.
 	 */
 	public static TypeNameMatch createTypeNameMatch(IType type, int modifiers) {
 		return new DLTKSearchTypeNameMatch(type, modifiers);
 	}
 	
 	/**
 	 * @see SearchEngine#createWorkspaceScope() for detailed comment.
 	 */
 	public static IDLTKSearchScope createWorkspaceScope(IDLTKLanguageToolkit toolkit) {
 		return ModelManager.getModelManager().getWorkspaceScope(toolkit);
 	}
 	
 	/**
 	 * Searches for matches to a given query. Search queries can be created using helper
 	 * methods (from a String pattern or a Script element) and encapsulate the description of what is
 	 * being searched (for example, search method declarations in a case sensitive way).
 	 *
 	 * @param scope the search result has to be limited to the given scope
 	 * @param requestor a callback object to which each match is reported
 	 */
 	void findMatches(SearchPattern pattern, SearchParticipant[] participants, IDLTKSearchScope scope, SearchRequestor requestor, IProgressMonitor monitor) throws CoreException {
 		if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
 	
 		/* initialize progress monitor */
 		if (monitor != null)
 			monitor.beginTask(Messages.engine_searching, 100); 
 		if (VERBOSE) {
 			Util.verbose("Searching for pattern: " + pattern.toString()); //$NON-NLS-1$
 			Util.verbose(scope.toString());
 		}
 		if (participants == null) {
 			if (VERBOSE) Util.verbose("No participants => do nothing!"); //$NON-NLS-1$
 			return;
 		}
 	
 		IndexManager indexManager = ModelManager.getModelManager().getIndexManager();
 		try {
 			requestor.beginReporting();
 			for (int i = 0, l = participants.length; i < l; i++) {
 				if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
 	
 				SearchParticipant participant = participants[i];
 				SubProgressMonitor subMonitor= monitor==null ? null : new SubProgressMonitor(monitor, 1000);
 				if (subMonitor != null) subMonitor.beginTask("", 1000); //$NON-NLS-1$
 				try {
 					if (subMonitor != null) subMonitor.subTask(Messages.bind(Messages.engine_searching_indexing, new String[] {participant.getDescription()})); 
 					participant.beginSearching();
 					requestor.enterParticipant(participant);
 					PathCollector pathCollector = new PathCollector();
 					indexManager.performConcurrentJob(
 						new PatternSearchJob(pattern, participant, scope, pathCollector),
 						IDLTKSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
 						subMonitor);
 					if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
 	
 					// locate index matches if any (note that all search matches could have been issued during index querying)
 					if (subMonitor != null) subMonitor.subTask(Messages.bind(Messages.engine_searching_matching, new String[] {participant.getDescription()})); 
 					String[] indexMatchPaths = pathCollector.getPaths();
 					if (indexMatchPaths != null) {
 						pathCollector = null; // release
 						int indexMatchLength = indexMatchPaths.length;
 						SearchDocument[] indexMatches = new SearchDocument[indexMatchLength];
 						for (int j = 0; j < indexMatchLength; j++) {
 							indexMatches[j] = participant.getDocument(indexMatchPaths[j]);
 						}
 						SearchDocument[] matches = MatchLocator.addWorkingCopies(pattern, indexMatches, getWorkingCopies(), participant);
						participant.locateMatches(matches, pattern, scope, requestor, subMonitor);
 					}
 				}
 				catch ( Exception e ) {
 						e.printStackTrace();
 				}
 				finally {		
 					requestor.exitParticipant(participant);
 					participant.doneSearching();
 				}
 			}
 		}	
 		catch ( Exception e ) {
 				e.printStackTrace();
 		}finally {
 			requestor.endReporting();
 			if (monitor != null)
 				monitor.done();
 		}
 	}
 	List findMatchesSourceOnly(SearchPattern pattern, SearchParticipant[] participants, IDLTKSearchScope scope, IProgressMonitor monitor) throws CoreException {
 		if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
 	
 		/* initialize progress monitor */
 		if (monitor != null)
 			monitor.beginTask(Messages.engine_searching, 100); 
 		if (VERBOSE) {
 			Util.verbose("Searching for pattern: " + pattern.toString()); //$NON-NLS-1$
 			Util.verbose(scope.toString());
 		}
 		if (participants == null) {
 			if (VERBOSE) Util.verbose("No participants => do nothing!"); //$NON-NLS-1$
 			return null;
 		}
 	
 		IndexManager indexManager = ModelManager.getModelManager().getIndexManager();
 		try {
 			List documents = new ArrayList();
 			for (int i = 0, l = participants.length; i < l; i++) {
 				if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
 	
 				SearchParticipant participant = participants[i];
 				SubProgressMonitor subMonitor= monitor==null ? null : new SubProgressMonitor(monitor, 1000);
 				if (subMonitor != null) subMonitor.beginTask("", 1000); //$NON-NLS-1$
 				try {
 					if (subMonitor != null) subMonitor.subTask(Messages.bind(Messages.engine_searching_indexing, new String[] {participant.getDescription()})); 
 					participant.beginSearching();
 					PathCollector pathCollector = new PathCollector();
 					indexManager.performConcurrentJob(
 						new PatternSearchJob(pattern, participant, scope, pathCollector),
 						IDLTKSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
 						subMonitor);
 					if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
 	
 					// locate index matches if any (note that all search matches could have been issued during index querying)
 					if (subMonitor != null) subMonitor.subTask(Messages.bind(Messages.engine_searching_matching, new String[] {participant.getDescription()})); 
 					String[] indexMatchPaths = pathCollector.getPaths();
 					if (indexMatchPaths != null) {
 						pathCollector = null; // release
 						int indexMatchLength = indexMatchPaths.length;
 						SearchDocument[] indexMatches = new SearchDocument[indexMatchLength];
 						for (int j = 0; j < indexMatchLength; j++) {
 							indexMatches[j] = participant.getDocument(indexMatchPaths[j]);
 						}
 						SearchDocument[] matches = MatchLocator.addWorkingCopies(pattern, indexMatches, getWorkingCopies(), participant);
 						ISourceModule[] modules = participant.locateModules(matches, pattern, scope,  subMonitor);
 						for( int k = 0; k < modules.length; ++k ) {
 							if( !documents.contains(modules[k])) {
 								documents.add(modules[k]);
 							}
 						}
 					}
 				}
 				catch ( Exception e ) {
 						e.printStackTrace();
 				}
 				finally {
 					participant.doneSearching();
 				}
 				return documents;
 			}
 			
 		}	
 		catch ( Exception e ) {
 				e.printStackTrace();
 		}finally {
 			if (monitor != null)
 				monitor.done();
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns a new default Script search participant.
 	 * 
 	 * @return a new default Script search participant
 	 *
 	 */
 	public static SearchParticipant getDefaultSearchParticipant() {
 		return new DLTKSearchParticipant();		
 	}
 
 
 	/**
 	 * @param matchRule
 	 */
 	public static String getMatchRuleString(final int matchRule) {
 		if (matchRule == 0) {
 			return "R_EXACT_MATCH"; //$NON-NLS-1$
 		}
 		StringBuffer buffer = new StringBuffer();
 		for (int i=1; i<=8; i++) {
 			int bit = matchRule & (1<<(i-1));
 			if (bit != 0 && buffer.length()>0) buffer.append(" | "); //$NON-NLS-1$
 			switch (bit) {
 				case SearchPattern.R_PREFIX_MATCH:
 					buffer.append("R_PREFIX_MATCH"); //$NON-NLS-1$
 					break;
 				case SearchPattern.R_CASE_SENSITIVE:
 					buffer.append("R_CASE_SENSITIVE"); //$NON-NLS-1$
 					break;
 				case SearchPattern.R_EQUIVALENT_MATCH:
 					buffer.append("R_EQUIVALENT_MATCH"); //$NON-NLS-1$
 					break;
 				case SearchPattern.R_ERASURE_MATCH:
 					buffer.append("R_ERASURE_MATCH"); //$NON-NLS-1$
 					break;
 				case SearchPattern.R_FULL_MATCH:
 					buffer.append("R_FULL_MATCH"); //$NON-NLS-1$
 					break;
 				case SearchPattern.R_PATTERN_MATCH:
 					buffer.append("R_PATTERN_MATCH"); //$NON-NLS-1$
 					break;
 				case SearchPattern.R_REGEXP_MATCH:
 					buffer.append("R_REGEXP_MATCH"); //$NON-NLS-1$
 					break;
 				case SearchPattern.R_CAMELCASE_MATCH:
 					buffer.append("R_CAMELCASE_MATCH"); //$NON-NLS-1$
 					break;
 			}
 		}
 		return buffer.toString();
 	}
 
 	/**
 	 * Return kind of search corresponding to given value.
 	 * 
 	 * @param searchFor
 	 */
 	public static String getSearchForString(final int searchFor) {
 		switch (searchFor) {
 			case IDLTKSearchConstants.TYPE:
 				return ("TYPE"); //$NON-NLS-1$
 			case IDLTKSearchConstants.METHOD:
 				return ("METHOD"); //$NON-NLS-1$
 //			case IDLTKSearchConstants.PACKAGE:
 //				return ("PACKAGE"); //$NON-NLS-1$
 //			case IDLTKSearchConstants.CONSTRUCTOR:
 //				return ("CONSTRUCTOR"); //$NON-NLS-1$
 			case IDLTKSearchConstants.FIELD:
 				return ("FIELD"); //$NON-NLS-1$		
 			case IDLTKSearchConstants.ANNOTATION_TYPE:
 				return ("ANNOTATION_TYPE"); //$NON-NLS-1$			
 		}
 		return "UNKNOWN"; //$NON-NLS-1$
 	}
 
 //	private Parser getParser() {
 //		if (this.parser == null) {
 //			this.compilerOptions = new CompilerOptions(JavaCore.getOptions());
 //			ProblemReporter problemReporter =
 //				new ProblemReporter(
 //					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
 //					this.compilerOptions,
 //					new DefaultProblemFactory());
 //			this.parser = new Parser(problemReporter, true);
 //		}
 //		return this.parser;
 //	}
 
 	/*
 	 * Returns the list of working copies used by this search engine.
 	 * Returns null if none.
 	 */
 	private ISourceModule[] getWorkingCopies() {
 		ISourceModule[] copies;
 		if (this.workingCopies != null) {
 			if (this.workingCopyOwner == null) {
 				copies = ModelManager.getModelManager().getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, false/*don't add primary WCs a second time*/);
 				if (copies == null) {
 					copies = this.workingCopies;
 				} else {
 					HashMap pathToCUs = new HashMap();
 					for (int i = 0, length = copies.length; i < length; i++) {
 						ISourceModule unit = copies[i];
 						pathToCUs.put(unit.getPath(), unit);
 					}
 					for (int i = 0, length = this.workingCopies.length; i < length; i++) {
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
 			copies = ModelManager.getModelManager().getWorkingCopies(this.workingCopyOwner, true/*add primary WCs*/);
 		} else {
 			copies = ModelManager.getModelManager().getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, false/*don't add primary WCs a second time*/);
 		}
 		if (copies == null) return null;
 		
 		// filter out primary working copies that are saved
 		ISourceModule[] result = null;
 		int length = copies.length;
 		int index = 0;
 		for (int i = 0; i < length; i++) {
 			SourceModule copy = (SourceModule)copies[i];
 			try {
 				if (!copy.isPrimary()
 						|| copy.hasUnsavedChanges()
 						|| copy.hasResourceChanged()) {
 					if (result == null) {
 						result = new ISourceModule[length];
 					}
 					result[index++] = copy;
 				}
 			}  catch (ModelException e) {
 				// copy doesn't exist: ignore
 			}
 		}
 		if (index != length && result != null) {
 			System.arraycopy(result, 0, result = new ISourceModule[index], 0, index);
 		}
 		return result;
 	}
 	
 	/*
 	 * Returns the list of working copies used to do the search on the given Script element.
 	 */
 	private ISourceModule[] getWorkingCopies(IModelElement element) {
 		if (element instanceof IMember) {
 			ISourceModule cu = ((IMember)element).getSourceModule();
 			if (cu != null && cu.isWorkingCopy()) {
 				ISourceModule[] copies = getWorkingCopies();
 				int length = copies == null ? 0 : copies.length;
 				if (length > 0) {
 					ISourceModule[] newWorkingCopies = new ISourceModule[length+1];
 					System.arraycopy(copies, 0, newWorkingCopies, 0, length);
 					newWorkingCopies[length] = cu;
 					return newWorkingCopies;
 				} 
 				return new ISourceModule[] {cu};
 			}
 		}
 		return getWorkingCopies();
 	}
 
 	boolean match(char patternTypeSuffix, int modifiers) {
 		switch(patternTypeSuffix) {
 			case IIndexConstants.TYPE_SUFFIX :
 				return true; // TODO: add conditions here when needed
 //			case IIndexConstants.ANNOTATION_TYPE_SUFFIX :
 //				return (modifiers & Flags.AccAnnotation) != 0;
 		}
 		return true;
 	}
 
 	boolean match(char patternTypeSuffix, char[] patternPkg, char[] patternTypeName, int matchRule, int typeKind, char[] typeName) {
 	
 		boolean isCaseSensitive = (matchRule & SearchPattern.R_CASE_SENSITIVE) != 0;
 //		if (patternPkg != null && !CharOperation.equals(patternPkg, pkg, isCaseSensitive))
 //				return false;
 		
 		if (patternTypeName != null) {
 			boolean isCamelCase = (matchRule & SearchPattern.R_CAMELCASE_MATCH) != 0;
 			int matchMode = matchRule & DLTKSearchPattern.MATCH_MODE_MASK;
 			if (!isCaseSensitive && !isCamelCase) {
 				patternTypeName = CharOperation.toLowerCase(patternTypeName);
 			}
 			boolean matchFirstChar = !isCaseSensitive || patternTypeName[0] == typeName[0];
 			if (isCamelCase && matchFirstChar && CharOperation.camelCaseMatch(patternTypeName, typeName)) {
 				return true;
 			}
 			switch(matchMode) {
 				case SearchPattern.R_EXACT_MATCH :
 					if (!isCamelCase) {
 						return matchFirstChar && CharOperation.equals(patternTypeName, typeName, isCaseSensitive);
 					}
 					// fall through next case to match as prefix if camel case failed
 				case SearchPattern.R_PREFIX_MATCH :
 					return matchFirstChar && CharOperation.prefixEquals(patternTypeName, typeName, isCaseSensitive);
 				case SearchPattern.R_PATTERN_MATCH :
 					return CharOperation.match(patternTypeName, typeName, isCaseSensitive);
 				case SearchPattern.R_REGEXP_MATCH :
 					// TODO (frederic) implement regular expression match
 					break;
 			}
 		}
 		return true;
 	
 	}	
 	
 	/**
 	 * Searches for matches of a given search pattern. Search patterns can be created using helper
 	 * methods (from a String pattern or a Script element) and encapsulate the description of what is
 	 * being searched (for example, search method declarations in a case sensitive way).
 	 *
 	 * @see SearchEngine#search(SearchPattern, SearchParticipant[], IJavaSearchScope, SearchRequestor, IProgressMonitor)
 	 * 	for detailed comment
 	 */
 	public void search(SearchPattern pattern, SearchParticipant[] participants, IDLTKSearchScope scope, SearchRequestor requestor, IProgressMonitor monitor) throws CoreException {
 		if (VERBOSE) {
 			Util.verbose("BasicSearchEngine.search(SearchPattern, SearchParticipant[], IJavaSearchScope, SearchRequestor, IProgressMonitor)"); //$NON-NLS-1$
 		}
 		findMatches(pattern, participants, scope, requestor, monitor);
 	}
 	
 	/**
 	 * Searches for matches of a given search pattern. Search patterns can be created using helper
 	 * methods (from a String pattern or a Script element) and encapsulate the description of what is
 	 * being searched (for example, search method declarations in a case sensitive way).
 	 *
 	 * @see SearchEngine#search(SearchPattern, SearchParticipant[], IJavaSearchScope, SearchRequestor, IProgressMonitor)
 	 * 	for detailed comment
 	 */
 	public List searchSourceOnly(SearchPattern pattern, SearchParticipant[] participants, IDLTKSearchScope scope, IProgressMonitor monitor) throws CoreException {
 		if (VERBOSE) {
 			Util.verbose("BasicSearchEngine.search(SearchPattern, SearchParticipant[], IJavaSearchScope, SearchRequestor, IProgressMonitor)"); //$NON-NLS-1$
 		}
 		return findMatchesSourceOnly(pattern, participants, scope, monitor);
 	}
 
 	/**
 	 * Searches for all secondary types in the given scope.
 	 * The search can be selecting specific types (given a package or a type name
 	 * prefix and match modes). 
 	 */
 	public void searchAllSecondaryTypeNames(
 			IProjectFragment[] sourceFolders,
 			final IRestrictedAccessTypeRequestor nameRequestor,
 			boolean waitForIndexes,
 			IProgressMonitor progressMonitor)  throws ModelException {
 //
 //		if (VERBOSE) {
 //			Util.verbose("BasicSearchEngine.searchAllSecondaryTypeNames(IProjectFragment[], IRestrictedAccessTypeRequestor, boolean, IProgressMonitor)"); //$NON-NLS-1$
 //			StringBuffer buffer = new StringBuffer("	- source folders: "); //$NON-NLS-1$
 //			int length = sourceFolders.length;
 //			for (int i=0; i<length; i++) {
 //				if (i==0) {
 //					buffer.append('[');
 //				} else {
 //					buffer.append(',');
 //				}
 //				buffer.append(sourceFolders[i].getElementName());
 //			}
 //			buffer.append("]\n	- waitForIndexes: "); //$NON-NLS-1$
 //			buffer.append(waitForIndexes);
 //			Util.verbose(buffer.toString());
 //		}
 //
 //		IndexManager indexManager = ModelManager.getModelManager().getIndexManager();
 //		final TypeDeclarationPattern pattern = new SecondaryTypeDeclarationPattern();
 //
 //		// Get working copy path(s). Store in a single string in case of only one to optimize comparison in requestor
 //		final HashSet workingCopyPaths = new HashSet();
 //		String workingCopyPath = null;
 //		ISourceModule[] copies = getWorkingCopies();
 //		final int copiesLength = copies == null ? 0 : copies.length;
 //		if (copies != null) {
 //			if (copiesLength == 1) {
 //				workingCopyPath = copies[0].getPath().toString();
 //			} else {
 //				for (int i = 0; i < copiesLength; i++) {
 //					ISourceModule workingCopy = copies[i];
 //					workingCopyPaths.add(workingCopy.getPath().toString());
 //				}
 //			}
 //		}
 //		final String singleWkcpPath = workingCopyPath;
 //
 //		// Index requestor
 //		IndexQueryRequestor searchRequestor = new IndexQueryRequestor(){
 //			public boolean acceptIndexMatch(String documentPath, SearchPattern indexRecord, SearchParticipant participant, AccessRuleSet access) {
 //				// Filter unexpected types
 //				TypeDeclarationPattern record = (TypeDeclarationPattern)indexRecord;
 //				if (!record.secondary) {
 //					return true; // filter maint types
 //				}
 //				if (record.enclosingTypeNames == IIndexConstants.ONE_ZERO_CHAR) {
 //					return true; // filter out local and anonymous classes
 //				}
 //				switch (copiesLength) {
 //					case 0:
 //						break;
 //					case 1:
 //						if (singleWkcpPath.equals(documentPath)) {
 //							return true; // fliter out *the* working copy
 //						}
 //						break;
 //					default:
 //						if (workingCopyPaths.contains(documentPath)) {
 //							return true; // filter out working copies
 //						}
 //						break;
 //				}
 //
 //				// Accept document path
 //				AccessRestriction accessRestriction = null;
 //				if (access != null) {
 //					// Compute document relative path
 //					int pkgLength = (record.pkg==null || record.pkg.length==0) ? 0 : record.pkg.length+1;
 //					int nameLength = record.simpleName==null ? 0 : record.simpleName.length;
 //					char[] path = new char[pkgLength+nameLength];
 //					int pos = 0;
 //					if (pkgLength > 0) {
 //						System.arraycopy(record.pkg, 0, path, pos, pkgLength-1);
 //						CharOperation.replace(path, '.', '/');
 //						path[pkgLength-1] = '/';
 //						pos += pkgLength;
 //					}
 //					if (nameLength > 0) {
 //						System.arraycopy(record.simpleName, 0, path, pos, nameLength);
 //						pos += nameLength;
 //					}
 //					// Update access restriction if path is not empty
 //					if (pos > 0) {
 //						accessRestriction = access.getViolatedRestriction(path);
 //					}
 //				}
 //				nameRequestor.acceptType(record.modifiers, record.pkg, record.simpleName, record.enclosingTypeNames, documentPath, accessRestriction);
 //				return true;
 //			}
 //		};
 //
 //		// add type names from indexes
 //		if (progressMonitor != null) {
 //			progressMonitor.beginTask(Messages.engine_searching, 100); 
 //		}
 //		try {
 //			indexManager.performConcurrentJob(
 //				new PatternSearchJob(
 //					pattern, 
 //					getDefaultSearchParticipant(), // Script search only
 //					createJavaSearchScope(sourceFolders), 
 //					searchRequestor),
 //				waitForIndexes
 //					? IDLTKSearchConstants.WAIT_UNTIL_READY_TO_SEARCH
 //					: IDLTKSearchConstants.FORCE_IMMEDIATE_SEARCH,
 //				progressMonitor == null ? null : new SubProgressMonitor(progressMonitor, 100));
 //		}
 //		catch (OperationCanceledException oce) {
 //			// do nothing
 //		}
 	}
 
 	/**
 	 * Searches for all top-level types and member types in the given scope.
 	 * The search can be selecting specific types (given a package or a type name
 	 * prefix and match modes). 
 	 * 
 	 * @see SearchEngine#searchAllTypeNames(char[], char[], int, int, IJavaSearchScope, TypeNameRequestor, int, IProgressMonitor)
 	 * 	for detailed comment
 	 */
 	public void searchAllTypeNames(
 		final char[] packageName, 
 		final int packageMatchRule, 
 		final char[] typeName,
 		final int typeMatchRule, 
 		int searchFor, 
 		IDLTKSearchScope scope, 
 		final IRestrictedAccessTypeRequestor nameRequestor,
 		int waitingPolicy,
 		IProgressMonitor progressMonitor)  throws ModelException {
 
 		if (VERBOSE) {
 			Util.verbose("BasicSearchEngine.searchAllTypeNames(char[], char[], int, int, IJavaSearchScope, IRestrictedAccessTypeRequestor, int, IProgressMonitor)"); //$NON-NLS-1$
 			Util.verbose("	- package name: "+(packageName==null?"null":new String(packageName))); //$NON-NLS-1$ //$NON-NLS-2$
 			Util.verbose("	- match rule: "+getMatchRuleString(packageMatchRule)); //$NON-NLS-1$
 			Util.verbose("	- type name: "+(typeName==null?"null":new String(typeName))); //$NON-NLS-1$ //$NON-NLS-2$
 			Util.verbose("	- match rule: "+getMatchRuleString(typeMatchRule)); //$NON-NLS-1$
 			Util.verbose("	- search for: "+searchFor); //$NON-NLS-1$
 			Util.verbose("	- scope: "+scope); //$NON-NLS-1$
 		}
 
 		// Return on invalid combination of package and type names
 		if (packageName == null || packageName.length == 0) {
 			if (typeName != null && typeName.length == 0) {
 				// TODO (frederic) Throw a JME instead?
 				if (VERBOSE) {
 					Util.verbose("	=> return no result due to invalid empty values for package and type names!"); //$NON-NLS-1$
 				}
 				return;
 			}
 		}
 
 		// Create pattern
 		IndexManager indexManager = ModelManager.getModelManager().getIndexManager();
 		final char typeSuffix;
 		switch(searchFor){
 			case IDLTKSearchConstants.TYPE :
 				typeSuffix = IIndexConstants.TYPE_SUFFIX;
 				break;
 			case IDLTKSearchConstants.ANNOTATION_TYPE :
 				typeSuffix = IIndexConstants.ANNOTATION_TYPE_SUFFIX;
 				break;
 			default : 
 				typeSuffix = IIndexConstants.TYPE_SUFFIX;
 				break;
 		}
 		final TypeDeclarationPattern pattern = packageMatchRule == SearchPattern.R_EXACT_MATCH
 			? new TypeDeclarationPattern(
 				packageName,
 				null,
 				typeName,
 				typeSuffix,
 				typeMatchRule)
 			: new QualifiedTypeDeclarationPattern(
 				packageName,
 				packageMatchRule,
 				typeName,
 				typeSuffix,
 				typeMatchRule);
 
 		// Get working copy path(s). Store in a single string in case of only one to optimize comparison in requestor
 		final HashSet workingCopyPaths = new HashSet();
 		String workingCopyPath = null;
 		ISourceModule[] copies = getWorkingCopies();
 		final int copiesLength = copies == null ? 0 : copies.length;
 		if (copies != null) {
 			if (copiesLength == 1) {
 				workingCopyPath = copies[0].getPath().toString();
 			} else {
 				for (int i = 0; i < copiesLength; i++) {
 					ISourceModule workingCopy = copies[i];
 					workingCopyPaths.add(workingCopy.getPath().toString());
 				}
 			}
 		}
 		final String singleWkcpPath = workingCopyPath;
 		final List documentPathFilter = new ArrayList();
 		// Index requestor
 		IndexQueryRequestor searchRequestor = new IndexQueryRequestor(){
 			public boolean acceptIndexMatch(String documentPath,
 					SearchPattern indexRecord, SearchParticipant participant,
 					AccessRuleSet access) {
 				IPath fullPath = new Path(documentPath);
 				if( documentPathFilter.contains(fullPath)) {
 					return true;
 				}
 				documentPathFilter.add(fullPath);
 				// Filter unexpected types
 				TypeDeclarationPattern record = (TypeDeclarationPattern)indexRecord;
 				if (record.enclosingTypeNames == IIndexConstants.ONE_ZERO_CHAR) {
 					return true; // filter out local and anonymous classes
 				}
 				switch (copiesLength) {
 					case 0:
 						break;
 					case 1:
 						if (singleWkcpPath.equals(documentPath)) {
 							return true; // fliter out *the* working copy
 						}
 						break;
 					default:
 						if (workingCopyPaths.contains(documentPath)) {
 							return true; // filter out working copies
 						}
 						break;
 				}
 
 				// Accept document path
 				AccessRestriction accessRestriction = null;
 				if (access != null) {
 					// Compute document relative path
 					int pkgLength = (record.pkg==null || record.pkg.length==0) ? 0 : record.pkg.length+1;
 					int nameLength = record.simpleName==null ? 0 : record.simpleName.length;
 					char[] path = new char[pkgLength+nameLength];
 					int pos = 0;
 					if (pkgLength > 0) {
 						System.arraycopy(record.pkg, 0, path, pos, pkgLength-1);
 						CharOperation.replace(path, '.', '/');
 						path[pkgLength-1] = '/';
 						pos += pkgLength;
 					}
 					if (nameLength > 0) {
 						System.arraycopy(record.simpleName, 0, path, pos, nameLength);
 						pos += nameLength;
 					}
 					// Update access restriction if path is not empty
 					if (pos > 0) {
 						accessRestriction = access.getViolatedRestriction(path);
 					}
 				}
 				if (match(record.typeSuffix, record.modifiers)) {
 					nameRequestor.acceptType(record.modifiers, record.pkg, record.simpleName, record.enclosingTypeNames, documentPath, accessRestriction);
 				}
 				return true;
 			}
 		};
 	
 		try {
 			if (progressMonitor != null) {
 				progressMonitor.beginTask(Messages.engine_searching, 100); 
 			}
 			// add type names from indexes
 			indexManager.performConcurrentJob(
 				new PatternSearchJob(
 					pattern, 
 					getDefaultSearchParticipant(), // Script search only
 					scope, 
 					searchRequestor),
 				waitingPolicy,
 				progressMonitor == null ? null : new SubProgressMonitor(progressMonitor, 100));	
 				
 			// add type names from working copies
 			if (copies != null) {
 				for (int i = 0; i < copiesLength; i++) {
 					ISourceModule workingCopy = copies[i];
 					if (!scope.encloses(workingCopy)) continue;
 					final String path = workingCopy.getPath().toString();
 //					if (workingCopy.isConsistent()) {
 //						IPackageDeclaration[] packageDeclarations = workingCopy.getPackageDeclarations();
 //						char[] packageDeclaration = packageDeclarations.length == 0 ? CharOperation.NO_CHAR : packageDeclarations[0].getElementName().toCharArray();
 						IType[] allTypes = workingCopy.getTypes();
 						for (int j = 0, allTypesLength = allTypes.length; j < allTypesLength; j++) {
 							IType type = allTypes[j];
 							IModelElement parent = type.getParent();
 							char[][] enclosingTypeNames;
 							if (parent instanceof IType) {
 								char[] parentQualifiedName = ((IType)parent).getTypeQualifiedName().toCharArray();
 								enclosingTypeNames = CharOperation.splitOn('.', parentQualifiedName);
 							} else {
 								enclosingTypeNames = CharOperation.NO_CHAR_CHAR;
 							}
 							char[] simpleName = type.getElementName().toCharArray();
 							int kind = 0;
 							if (match(typeSuffix, packageName, typeName, typeMatchRule, kind, /*packageDeclaration,*/ simpleName)) {
 								nameRequestor.acceptType(type.getFlags(), new char[0], /*packageDeclaration,*/ simpleName, enclosingTypeNames, path, null);
 							}
 						}
 //					}
 				}
 			}	
 		} finally {
 			if (progressMonitor != null) {
 				progressMonitor.done();
 			}
 		}
 	}
 
 	/**
 	 * Searches for all top-level types and member types in the given scope using  a case sensitive exact match
 	 * with the given qualified names and type names.
 	 * 
 	 * @see SearchEngine#searchAllTypeNames(char[][], char[][], IJavaSearchScope, TypeNameRequestor, int, IProgressMonitor)
 	 * 	for detailed comment
 	 */
 	public void searchAllTypeNames(
 		final char[][] qualifications, 
 		final char[][] typeNames,
 		final int matchRule, 
 		int searchFor, 
 		IDLTKSearchScope scope, 
 		final IRestrictedAccessTypeRequestor nameRequestor,
 		int waitingPolicy,
 		IProgressMonitor progressMonitor)  throws ModelException {
 
 		if (VERBOSE) {
 			Util.verbose("BasicSearchEngine.searchAllTypeNames(char[][], char[][], int, int, IJavaSearchScope, IRestrictedAccessTypeRequestor, int, IProgressMonitor)"); //$NON-NLS-1$
 			Util.verbose("	- package name: "+(qualifications==null?"null":new String(CharOperation.concatWith(qualifications, ',')))); //$NON-NLS-1$ //$NON-NLS-2$
 			Util.verbose("	- type name: "+(typeNames==null?"null":new String(CharOperation.concatWith(typeNames, ',')))); //$NON-NLS-1$ //$NON-NLS-2$
 			Util.verbose("	- match rule: "+matchRule); //$NON-NLS-1$
 			Util.verbose("	- search for: "+searchFor); //$NON-NLS-1$
 			Util.verbose("	- scope: "+scope); //$NON-NLS-1$
 		}
 //		IndexManager indexManager = ModelManager.getModelManager().getIndexManager();
 //
 //		final char typeSuffix;
 //		switch(searchFor){
 //			case IDLTKSearchConstants.CLASS :
 //				typeSuffix = IIndexConstants.CLASS_SUFFIX;
 //				break;
 //			case IDLTKSearchConstants.CLASS_AND_INTERFACE :
 //				typeSuffix = IIndexConstants.CLASS_AND_INTERFACE_SUFFIX;
 //				break;
 //			case IDLTKSearchConstants.CLASS_AND_ENUM :
 //				typeSuffix = IIndexConstants.CLASS_AND_ENUM_SUFFIX;
 //				break;
 //			case IDLTKSearchConstants.INTERFACE :
 //				typeSuffix = IIndexConstants.INTERFACE_SUFFIX;
 //				break;
 //			case IDLTKSearchConstants.ENUM :
 //				typeSuffix = IIndexConstants.ENUM_SUFFIX;
 //				break;
 //			case IDLTKSearchConstants.ANNOTATION_TYPE :
 //				typeSuffix = IIndexConstants.ANNOTATION_TYPE_SUFFIX;
 //				break;
 //			default : 
 //				typeSuffix = IIndexConstants.TYPE_SUFFIX;
 //				break;
 //		}
 //		final MultiTypeDeclarationPattern pattern = new MultiTypeDeclarationPattern(qualifications, typeNames, typeSuffix, matchRule);
 //
 //		// Get working copy path(s). Store in a single string in case of only one to optimize comparison in requestor
 //		final HashSet workingCopyPaths = new HashSet();
 //		String workingCopyPath = null;
 //		ISourceModule[] copies = getWorkingCopies();
 //		final int copiesLength = copies == null ? 0 : copies.length;
 //		if (copies != null) {
 //			if (copiesLength == 1) {
 //				workingCopyPath = copies[0].getPath().toString();
 //			} else {
 //				for (int i = 0; i < copiesLength; i++) {
 //					ISourceModule workingCopy = copies[i];
 //					workingCopyPaths.add(workingCopy.getPath().toString());
 //				}
 //			}
 //		}
 //		final String singleWkcpPath = workingCopyPath;
 //
 //		// Index requestor
 //		IndexQueryRequestor searchRequestor = new IndexQueryRequestor(){
 //			public boolean acceptIndexMatch(String documentPath, SearchPattern indexRecord, SearchParticipant participant, AccessRuleSet access) {
 //				// Filter unexpected types
 //				switch (copiesLength) {
 //					case 0:
 //						break;
 //					case 1:
 //						if (singleWkcpPath.equals(documentPath)) {
 //							return true; // fliter out *the* working copy
 //						}
 //						break;
 //					default:
 //						if (workingCopyPaths.contains(documentPath)) {
 //							return true; // filter out working copies
 //						}
 //						break;
 //				}
 //
 //				// Accept document path
 //				QualifiedTypeDeclarationPattern record = (QualifiedTypeDeclarationPattern) indexRecord;
 //				AccessRestriction accessRestriction = null;
 //				if (access != null) {
 //					// Compute document relative path
 //					int qualificationLength = (record.qualification == null || record.qualification.length == 0) ? 0 : record.qualification.length + 1;
 //					int nameLength = record.simpleName == null ? 0 : record.simpleName.length;
 //					char[] path = new char[qualificationLength + nameLength];
 //					int pos = 0;
 //					if (qualificationLength > 0) {
 //						System.arraycopy(record.qualification, 0, path, pos, qualificationLength - 1);
 //						CharOperation.replace(path, '.', '/');
 //						path[qualificationLength-1] = '/';
 //						pos += qualificationLength;
 //					}
 //					if (nameLength > 0) {
 //						System.arraycopy(record.simpleName, 0, path, pos, nameLength);
 //						pos += nameLength;
 //					}
 //					// Update access restriction if path is not empty
 //					if (pos > 0) {
 //						accessRestriction = access.getViolatedRestriction(path);
 //					}
 //				}
 //				nameRequestor.acceptType(record.modifiers, record.pkg, record.simpleName, record.enclosingTypeNames, documentPath, accessRestriction);
 //				return true;
 //			}
 //		};
 //	
 //		try {
 //			if (progressMonitor != null) {
 //				progressMonitor.beginTask(Messages.engine_searching, 100); 
 //			}
 //			// add type names from indexes
 //			indexManager.performConcurrentJob(
 //				new PatternSearchJob(
 //					pattern, 
 //					getDefaultSearchParticipant(), // Script search only
 //					scope, 
 //					searchRequestor),
 //				waitingPolicy,
 //				progressMonitor == null ? null : new SubProgressMonitor(progressMonitor, 100));	
 //				
 //			// add type names from working copies
 //			if (copies != null) {
 //				for (int i = 0, length = copies.length; i < length; i++) {
 //					ISourceModule workingCopy = copies[i];
 //					final String path = workingCopy.getPath().toString();
 //					if (workingCopy.isConsistent()) {
 //						IPackageDeclaration[] packageDeclarations = workingCopy.getPackageDeclarations();
 //						char[] packageDeclaration = packageDeclarations.length == 0 ? CharOperation.NO_CHAR : packageDeclarations[0].getElementName().toCharArray();
 //						IType[] allTypes = workingCopy.getAllTypes();
 //						for (int j = 0, allTypesLength = allTypes.length; j < allTypesLength; j++) {
 //							IType type = allTypes[j];
 //							IModelElement parent = type.getParent();
 //							char[][] enclosingTypeNames;
 //							char[] qualification = packageDeclaration;
 //							if (parent instanceof IType) {
 //								char[] parentQualifiedName = ((IType)parent).getTypeQualifiedName('.').toCharArray();
 //								enclosingTypeNames = CharOperation.splitOn('.', parentQualifiedName);
 //								qualification = CharOperation.concat(qualification, parentQualifiedName);
 //							} else {
 //								enclosingTypeNames = CharOperation.NO_CHAR_CHAR;
 //							}
 //							char[] simpleName = type.getElementName().toCharArray();
 //							char suffix = IIndexConstants.TYPE_SUFFIX;
 //							if (type.isClass()) {
 //								suffix = IIndexConstants.CLASS_SUFFIX;
 //							} else if (type.isInterface()) {
 //								suffix = IIndexConstants.INTERFACE_SUFFIX;
 //							} else if (type.isEnum()) {
 //								suffix = IIndexConstants.ENUM_SUFFIX;
 //							} else if (type.isAnnotation()) {
 //								suffix = IIndexConstants.ANNOTATION_TYPE_SUFFIX;
 //							}
 //							if (pattern.matchesDecodedKey(new QualifiedTypeDeclarationPattern(qualification, simpleName, suffix, matchRule))) {
 //								nameRequestor.acceptType(type.getFlags(), packageDeclaration, simpleName, enclosingTypeNames, path, null);
 //							}
 //						}
 //					} else {
 //						Parser basicParser = getParser();
 //						org.eclipse.dltk.internal.compiler.env.ISourceModule unit = (org.eclipse.dltk.internal.compiler.env.ISourceModule) workingCopy;
 //						CompilationResult compilationUnitResult = new CompilationResult(unit, 0, 0, this.compilerOptions.maxProblemsPerUnit);
 //						SourceModuleDeclaration parsedUnit = basicParser.dietParse(unit, compilationUnitResult);
 //						if (parsedUnit != null) {
 //							final char[] packageDeclaration = parsedUnit.currentPackage == null
 //								? CharOperation.NO_CHAR
 //								: CharOperation.concatWith(parsedUnit.currentPackage.getImportName(), '.');
 //							class AllTypeDeclarationsVisitor extends ASTVisitor {
 //								public boolean visit(TypeDeclaration typeDeclaration, BlockScope blockScope) {
 //									return false; // no local/anonymous type
 //								}
 //								public boolean visit(TypeDeclaration typeDeclaration, SourceModuleScope compilationUnitScope) {
 //									SearchPattern decodedPattern =
 //										new QualifiedTypeDeclarationPattern(packageDeclaration, typeDeclaration.name, convertTypeKind(TypeDeclaration.kind(typeDeclaration.modifiers)), matchRule);
 //									if (pattern.matchesDecodedKey(decodedPattern)) {
 //										nameRequestor.acceptType(typeDeclaration.modifiers, packageDeclaration, typeDeclaration.name, CharOperation.NO_CHAR_CHAR, path, null);
 //									Parser basicParser = getParser();
 //						org.eclipse.dltk.internal.compiler.env.ISourceModule unit = (org.eclipse.dltk.internal.compiler.env.ISourceModule) workingCopy;
 //						CompilationResult compilationUnitResult = new CompilationResult(unit, 0, 0, this.compilerOptions.maxProblemsPerUnit);
 //						SourceModuleDeclaration parsedUnit = basicParser.dietParse(unit, compilationUnitResult);
 //						if (parsedUnit != null) {
 //							final char[] packageDeclaration = parsedUnit.currentPackage == null
 //								? CharOperation.NO_CHAR
 //								: CharOperation.concatWith(parsedUnit.currentPackage.getImportName(), '.');
 //							class AllTypeDeclarationsVisitor extends ASTVisitor {
 //								public boolean visit(TypeDeclaration typeDeclaration, BlockScope blockScope) {
 //									return false; // no local/anonymous type
 //								}
 //								public boolean visit(TypeDeclaration typeDeclaration, SourceModuleScope compilationUnitScope) {
 //									SearchPattern decodedPattern =
 //										new QualifiedTypeDeclarationPattern(packageDeclaration, typeDeclaration.name, convertTypeKind(TypeDeclaration.kind(typeDeclaration.modifiers)), matchRule);
 //									if (pattern.matchesDecodedKey(decodedPattern)) {
 //										nameRequestor.acceptType(typeDeclaration.modifiers, packageDeclaration, typeDeclaration.name, CharOperation.NO_CHAR_CHAR, path, null);
 //									}
 //									return true;
 //								}
 //								public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope classScope) {
 //									// compute encloising type names
 //									char[] qualification = packageDeclaration;
 //									TypeDeclaration enclosing = memberTypeDeclaration.enclosingType;
 //									char[][] enclosingTypeNames = CharOperation.NO_CHAR_CHAR;
 //									while (enclosing != null) {
 //										qualification = CharOperation.concat(qualification, enclosing.name, '.');
 //										enclosingTypeNames = CharOperation.arrayConcat(new char[][] {enclosing.name}, enclosingTypeNames);
 //										if ((enclosing.bits & ASTNode.IsMemberType) != 0) {
 //											enclosing = enclosing.enclosingType;
 //										} else {
 //											enclosing = null;
 //										}
 //									}
 //									SearchPattern decodedPattern =
 //										new QualifiedTypeDeclarationPattern(qualification, memberTypeDeclaration.name, convertTypeKind(TypeDeclaration.kind(memberTypeDeclaration.modifiers)), matchRule);
 //									if (pattern.matchesDecodedKey(decodedPattern)) {
 //										nameRequestor.acceptType(memberTypeDeclaration.modifiers, packageDeclaration, memberTypeDeclaration.name, enclosingTypeNames, path, null);
 //									}
 //									return true;
 //								}
 //							}
 //							parsedUnit.traverse(new AllTypeDeclarationsVisitor(), parsedUnit.scope);}
 //									return true;
 //								}
 //								public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope classScope) {
 //									// compute encloising type names
 //									char[] qualification = packageDeclaration;
 //									TypeDeclaration enclosing = memberTypeDeclaration.enclosingType;
 //									char[][] enclosingTypeNames = CharOperation.NO_CHAR_CHAR;
 //									while (enclosing != null) {
 //										qualification = CharOperation.concat(qualification, enclosing.name, '.');
 //										enclosingTypeNames = CharOperation.arrayConcat(new char[][] {enclosing.name}, enclosingTypeNames);
 //										if ((enclosing.bits & ASTNode.IsMemberType) != 0) {
 //											enclosing = enclosing.enclosingType;
 //										} else {
 //											enclosing = null;
 //										}
 //									}
 //									SearchPattern decodedPattern =
 //										new QualifiedTypeDeclarationPattern(qualification, memberTypeDeclaration.name, convertTypeKind(TypeDeclaration.kind(memberTypeDeclaration.modifiers)), matchRule);
 //									if (pattern.matchesDecodedKey(decodedPattern)) {
 //										nameRequestor.acceptType(memberTypeDeclaration.modifiers, packageDeclaration, memberTypeDeclaration.name, enclosingTypeNames, path, null);
 //									}
 //									return true;
 //								}
 //							}
 //							parsedUnit.traverse(new AllTypeDeclarationsVisitor(), parsedUnit.scope);
 //						}
 //					}
 //				}
 //			}	
 //		} finally {
 //			if (progressMonitor != null) {
 //				progressMonitor.done();
 //			}
 //		}
 	}
 	public DLTKSearchParticipant getSearchParticipant(IModelElement element) {
 		IDLTKLanguageToolkit toolkit = null;
 		try {
 			toolkit = DLTKLanguageManager.getLangaugeToolkit(element);
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if( toolkit != null ) {
 			DLTKSearchParticipant par = toolkit.createSearchParticipant();
 			if(par != null ) {
 				return par;
 			}
 		}		
 		return new DLTKSearchParticipant();
 	}
 	
 	public void searchDeclarations(IModelElement enclosingElement, SearchRequestor requestor, SearchPattern pattern, IProgressMonitor monitor) throws ModelException {
 		if (VERBOSE) {
 			Util.verbose("	- script element: "+enclosingElement); //$NON-NLS-1$
 		}
 		IDLTKSearchScope scope = createSearchScope(new IModelElement[] {enclosingElement});
 		IResource resource = enclosingElement.getResource();
 		if (enclosingElement instanceof IMember) {
 			IMember member = (IMember) enclosingElement;
 			ISourceModule cu = member.getSourceModule();
 			if (cu != null) {
 				resource = cu.getResource();
 			} else if (((IProjectFragment)member.getAncestor(IModelElement.PROJECT_FRAGMENT)).isExternal()) {
 				// binary member resource cannot be used as this
 				// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=148215
 				resource = null;
 			}
 		}
 		try {
 			if (resource instanceof IFile) {
 				try {
 					requestor.beginReporting();
 					if (VERBOSE) {
 						Util.verbose("Searching for " + pattern + " in " + resource.getFullPath()); //$NON-NLS-1$//$NON-NLS-2$
 					}
 					SearchParticipant participant = getSearchParticipant(enclosingElement);
 					boolean external = false;
 					IProjectFragment fragment = (IProjectFragment)enclosingElement.getAncestor(IModelElement.PROJECT_FRAGMENT);
 					if( fragment != null ) {
 						external = fragment.isExternal();
 					}
 					SearchDocument[] documents = MatchLocator.addWorkingCopies(
 						pattern,						
 						new SearchDocument[] {new DLTKSearchDocument(enclosingElement.getPath().toString(), participant, external)},
 						getWorkingCopies(enclosingElement),
 						participant);
 					participant.locateMatches(
 						documents, 
 						pattern, 
 						scope, 
 						requestor, 
 						monitor);
 				} finally {
 					requestor.endReporting();
 				}
 			} else {
 				search(
 					pattern, 
 					new SearchParticipant[] {getDefaultSearchParticipant()}, 
 					scope, 
 					requestor, 
 					monitor);
 			}
 		} catch (CoreException e) {
 			if (e instanceof ModelException)
 				throw (ModelException) e;
 			throw new ModelException(e);
 		}
 	}
 
 	/**
 	 * Searches for all declarations of the fields accessed in the given element.
 	 * The element can be a compilation unit, a source type, or a source method.
 	 * Reports the field declarations using the given requestor.
 	 *
 	 * @see SearchEngine#searchDeclarationsOfAccessedFields(IModelElement, SearchRequestor, IProgressMonitor)
 	 * 	for detailed comment
 	 */	
 	public void searchDeclarationsOfAccessedFields(IModelElement enclosingElement, SearchRequestor requestor, IProgressMonitor monitor) throws ModelException {
 //		if (VERBOSE) {
 //			Util.verbose("BasicSearchEngine.searchDeclarationsOfAccessedFields(IModelElement, SearchRequestor, SearchPattern, IProgressMonitor)"); //$NON-NLS-1$
 //		}
 //		SearchPattern pattern = new DeclarationOfAccessedFieldsPattern(enclosingElement);
 //		searchDeclarations(enclosingElement, requestor, pattern, monitor);
 	}
 	
 	/**
 	 * Searches for all declarations of the types referenced in the given element.
 	 * The element can be a compilation unit, a source type, or a source method.
 	 * Reports the type declarations using the given requestor.
 	 * 
 	 * @see SearchEngine#searchDeclarationsOfReferencedTypes(IModelElement, SearchRequestor, IProgressMonitor)
 	 * 	for detailed comment
 	 */	
 	public void searchDeclarationsOfReferencedTypes(IModelElement enclosingElement, SearchRequestor requestor, IProgressMonitor monitor) throws ModelException {
 //		if (VERBOSE) {
 //			Util.verbose("BasicSearchEngine.searchDeclarationsOfReferencedTypes(IModelElement, SearchRequestor, SearchPattern, IProgressMonitor)"); //$NON-NLS-1$
 //		}
 //		SearchPattern pattern = new DeclarationOfReferencedTypesPattern(enclosingElement);
 //		searchDeclarations(enclosingElement, requestor, pattern, monitor);
 	}
 	
 	/**
 	 * Searches for all declarations of the methods invoked in the given element.
 	 * The element can be a compilation unit, a source type, or a source method.
 	 * Reports the method declarations using the given requestor.
 	 * 
 	 * @see SearchEngine#searchDeclarationsOfSentMessages(IModelElement, SearchRequestor, IProgressMonitor)
 	 * 	for detailed comment
 	 */	
 	public void searchDeclarationsOfSentMessages(IModelElement enclosingElement, SearchRequestor requestor, IProgressMonitor monitor) throws ModelException {
 //		if (VERBOSE) {
 //			Util.verbose("BasicSearchEngine.searchDeclarationsOfSentMessages(IModelElement, SearchRequestor, SearchPattern, IProgressMonitor)"); //$NON-NLS-1$
 //		}
 //		SearchPattern pattern = new DeclarationOfReferencedMethodsPattern(enclosingElement);
 //		searchDeclarations(enclosingElement, requestor, pattern, monitor);
 	}
 }
