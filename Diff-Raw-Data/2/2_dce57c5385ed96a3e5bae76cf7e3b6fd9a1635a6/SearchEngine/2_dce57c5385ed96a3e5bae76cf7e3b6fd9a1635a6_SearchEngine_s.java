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
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IBuiltinModuleProvider;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.core.search.indexing.IndexManager;
 import org.eclipse.dltk.internal.compiler.env.AccessRuleSet;
 import org.eclipse.dltk.internal.core.ModelManager;
 import org.eclipse.dltk.internal.core.Openable;
 import org.eclipse.dltk.internal.core.search.IndexQueryRequestor;
 import org.eclipse.dltk.internal.core.search.PatternSearchJob;
 import org.eclipse.dltk.internal.core.search.TypeNameMatchRequestorWrapper;
 import org.eclipse.dltk.internal.core.search.TypeNameRequestorWrapper;
 import org.eclipse.dltk.internal.core.search.matching.MixinPattern;
 import org.eclipse.dltk.internal.core.util.HandleFactory;
 
 
 /**
  * A {@link SearchEngine} searches for Script elements following a search pattern.
  * The search can be limited to a search scope.
  * <p>
  * Various search patterns can be created using the factory methods 
  * {@link SearchPattern#createPattern(String, int, int, int)}, {@link SearchPattern#createPattern(IModelElement, int)},
  * {@link SearchPattern#createOrPattern(SearchPattern, SearchPattern)}.
  * </p>
  * <p>For example, one can search for references to a method in the hierarchy of a type, 
  * or one can search for the declarations of types starting with "Abstract" in a project.
  * </p>
  * <p>
  * This class may be instantiated; it is not intended to be subclassed.
  * </p>
  */
 public class SearchEngine {
 		
 	private static final String SPECIAL_MIXIN = "#special#mixin:";
 	// Search engine now uses basic engine functionalities
 	private BasicSearchEngine basicEngine;
 
 	/**
 	 * Creates a new search engine.
 	 */
 	public SearchEngine() {
 		this.basicEngine = new BasicSearchEngine();
 	}
 	
 	/**
 	 * Creates a new search engine with a list of working copies that will take precedence over 
 	 * their original compilation units in the subsequent search operations.
 	 * <p>
 	 * Note that passing an empty working copy will be as if the original compilation
 	 * unit had been deleted.</p>
 	 * <p>
 	 * The given working copies take precedence over primary working copies (if any).
 	 * 
 	 * @param workingCopies the working copies that take precedence over their original compilation units
 	 *
 	 */
 	public SearchEngine(ISourceModule[] workingCopies) {
 		int length = workingCopies.length;
 		ISourceModule[] units = new ISourceModule[length];
 		System.arraycopy(workingCopies, 0, units, 0, length);
 		this.basicEngine = new BasicSearchEngine(units);
 	}
 	
 	/**
 	 * Creates a new search engine with the given working copy owner.
 	 * The working copies owned by this owner will take precedence over 
 	 * the primary compilation units in the subsequent search operations.
 	 * 
 	 * @param workingCopyOwner the owner of the working copies that take precedence over their original compilation units
 	 *
 	 */
 	public SearchEngine(WorkingCopyOwner workingCopyOwner) {
 		this.basicEngine = new BasicSearchEngine(workingCopyOwner);
 	}
 
 	/**
 	 * Returns a Script search scope limited to the hierarchy of the given type.
 	 * The Script elements resulting from a search with this scope will
 	 * be types in this hierarchy, or members of the types in this hierarchy.
 	 *
 	 * @param type the focus of the hierarchy scope
 	 * @return a new hierarchy scope
 	 * @exception ModelException if the hierarchy could not be computed on the given type
 	 */
 	public static IDLTKSearchScope createHierarchyScope(IType type) throws ModelException {
 		return BasicSearchEngine.createHierarchyScope(type);
 	}
 	
 	/**
 	 * Returns a Script search scope limited to the hierarchy of the given type.
 	 * When the hierarchy is computed, the types defined in the working copies owned
 	 * by the given owner take precedence over the original compilation units.
 	 * The Script elements resulting from a search with this scope will
 	 * be types in this hierarchy, or members of the types in this hierarchy.
 	 *
 	 * @param type the focus of the hierarchy scope
 	 * @param owner the owner of working copies that take precedence over original compilation units
 	 * @return a new hierarchy scope
 	 * @exception ModelException if the hierarchy could not be computed on the given type
 	 *
 	 */
 	public static IDLTKSearchScope createHierarchyScope(IType type, WorkingCopyOwner owner) throws ModelException {
 		return BasicSearchEngine.createHierarchyScope(type, owner);
 	}
 
 	/**
 	 * Returns a Script search scope limited to the given Script elements.
 	 * The Script elements resulting from a search with this scope will
 	 * be children of the given elements.
 	 * <p>
 	 * If an element is an IScriptProject, then the project's source folders, 
 	 * its jars (external and internal) and its referenced projects (with their source 
 	 * folders and jars, recursively) will be included.
 	 * If an element is an IProjectFragment, then only the package fragments of 
 	 * this package fragment root will be included.
 	 * If an element is an IScriptFolder, then only the compilation unit and class 
 	 * files of this package fragment will be included. Subpackages will NOT be 
 	 * included.</p>
 	 * <p>
 	 * In other words, this is equivalent to using SearchEngine.createJavaSearchScope(elements, true).</p>
 	 *
 	 * @param elements the Script elements the scope is limited to
 	 * @return a new Script search scope
 	 *
 	 */
 	public static IDLTKSearchScope createSearchScope(IModelElement[] elements) {
 		return BasicSearchEngine.createSearchScope(elements);
 	}
 
 	/**
 	 * Returns a Script search scope limited to the given Script elements.
 	 * The Script elements resulting from a search with this scope will
 	 * be children of the given elements.
 	 * 
 	 * If an element is an IScriptProject, then the project's source folders, 
 	 * its jars (external and internal) and - if specified - its referenced projects 
 	 * (with their source folders and jars, recursively) will be included.
 	 * If an element is an IProjectFragment, then only the package fragments of 
 	 * this package fragment root will be included.
 	 * If an element is an IScriptFolder, then only the compilation unit and class 
 	 * files of this package fragment will be included. Subpackages will NOT be 
 	 * included.
 	 *
 	 * @param elements the Script elements the scope is limited to
 	 * @param includeReferencedProjects a flag indicating if referenced projects must be 
 	 * 									 recursively included
 	 * @return a new Script search scope
 	 *
 	 */
 	public static IDLTKSearchScope createSearchScope(IModelElement[] elements, boolean includeReferencedProjects) {
 		return BasicSearchEngine.createSearchScope(elements, includeReferencedProjects);
 	}
 
 	/**
 	 * Returns a Script search scope limited to the given Script elements.
 	 * The Script elements resulting from a search with this scope will
 	 * be children of the given elements.
 	 * 
 	 * If an element is an IScriptProject, then it includes:
 	 * - its source folders if IJavaSearchScope.SOURCES is specified, 
 	 * - its application libraries (internal and external jars, class folders that are on the raw buildpath, 
 	 *   or the ones that are coming from a buildpath path variable,
 	 *   or the ones that are coming from a buildpath container with the K_APPLICATION kind)
 	 *   if IJavaSearchScope.APPLICATION_LIBRARIES is specified
 	 * - its system libraries (internal and external jars, class folders that are coming from an 
 	 *   IBuildpathContainer with the K_SYSTEM kind) 
 	 *   if IJavaSearchScope.APPLICATION_LIBRARIES is specified
 	 * - its referenced projects (with their source folders and jars, recursively) 
 	 *   if IJavaSearchScope.REFERENCED_PROJECTS is specified.
 	 * If an element is an IProjectFragment, then only the package fragments of 
 	 * this package fragment root will be included.
 	 * If an element is an IScriptFolder, then only the compilation unit and class 
 	 * files of this package fragment will be included. Subpackages will NOT be 
 	 * included.
 	 *
 	 * @param elements the Script elements the scope is limited to
 	 * @param includeMask the bit-wise OR of all include types of interest
 	 * @return a new Script search scope
 	 * @see IJavaSearchScope#SOURCES
 	 * @see IJavaSearchScope#APPLICATION_LIBRARIES
 	 * @see IJavaSearchScope#SYSTEM_LIBRARIES
 	 * @see IJavaSearchScope#REFERENCED_PROJECTS
 	 *
 	 */
 	public static IDLTKSearchScope createSearchScope(IModelElement[] elements, int includeMask) {
 		return BasicSearchEngine.createSearchScope(elements, includeMask);
 	}
 	
 	/**
 	 * Create a type name match on a given type with specific modifiers.
 	 * 
 	 * @param type The java model handle of the type
 	 * @param modifiers Modifiers of the type
 	 * @return A non-null match on the given type.
 	 */
 	public static TypeNameMatch createTypeNameMatch(IType type, int modifiers) {
 		return BasicSearchEngine.createTypeNameMatch(type, modifiers);
 	}
 	
 	/**
 	 * Returns a Script search scope with the workspace as the only limit.
 	 *
 	 * @return a new workspace scope
 	 */
 	public static IDLTKSearchScope createWorkspaceScope(IDLTKLanguageToolkit toolkit) {
 		return BasicSearchEngine.createWorkspaceScope(toolkit);
 	}
 	/**
 	 * Returns a new default Script search participant.
 	 * 
 	 * @return a new default Script search participant
 	 *
 	 */
 	public static SearchParticipant getDefaultSearchParticipant() {
 		return BasicSearchEngine.getDefaultSearchParticipant();
 	}
 	
 	/**
 	 * Searches for matches of a given search pattern. Search patterns can be created using helper
 	 * methods (from a String pattern or a Script element) and encapsulate the description of what is
 	 * being searched (for example, search method declarations in a case sensitive way).
 	 *
 	 * @param pattern the pattern to search
 	 * @param participants the particpants in the search
 	 * @param scope the search scope
 	 * @param requestor the requestor to report the matches to
 	 * @param monitor the progress monitor used to report progress
 	 * @exception CoreException if the search failed. Reasons include:
 	 *	<ul>
 	 *		<li>the buildpath is incorrectly set</li>
 	 *	</ul>
 	 *
 	 */
 	public void search(SearchPattern pattern, SearchParticipant[] participants, IDLTKSearchScope scope, SearchRequestor requestor, IProgressMonitor monitor) throws CoreException {
 		this.basicEngine.search(pattern, participants, scope, requestor, monitor);
 	}
 	
 	public List searchSourceOnly(SearchPattern pattern, SearchParticipant[] participants, IDLTKSearchScope scope, IProgressMonitor monitor) throws CoreException {
 		return this.basicEngine.searchSourceOnly(pattern, participants, scope, monitor);
 	}
 
 	/**
 	 * Searches for all top-level types and member types in the given scope.
 	 * The search can be selecting specific types (given a package exact full name or
 	 * a type name with specific match mode). 
 	 * 
 	 * @param packageExactName the exact package full name of the searched types.<br>
 	 * 					If you want to use a prefix or a wild-carded string for package, you need to use
 	 * 					{@link #searchAllTypeNames(char[], int, char[], int, int, IJavaSearchScope, TypeNameRequestor, int, IProgressMonitor)} method  instead.
 	 * @param typeName the dot-separated qualified name of the searched type (the qualification include
 	 *					the enclosing types if the searched type is a member type), or a prefix
 	 *					for this type, or a wild-carded string for this type.
 	 * @param matchRule type name match rule one of
 	 * <ul>
 	 *		<li>{@link SearchPattern#R_EXACT_MATCH} if the package name and type name are the full names
 	 *			of the searched types.</li>
 	 *		<li>{@link SearchPattern#R_PREFIX_MATCH} if the package name and type name are prefixes of the names
 	 *			of the searched types.</li>
 	 *		<li>{@link SearchPattern#R_PATTERN_MATCH} if the package name and type name contain wild-cards.</li>
 	 *		<li>{@link SearchPattern#R_CAMELCASE_MATCH} if type name are camel case of the names of the searched types.</li>
 	 * </ul>
 	 * combined with {@link SearchPattern#R_CASE_SENSITIVE},
 	 *   e.g. {@link SearchPattern#R_EXACT_MATCH} | {@link SearchPattern#R_CASE_SENSITIVE} if an exact and case sensitive match is requested, 
 	 *   or {@link SearchPattern#R_PREFIX_MATCH} if a prefix non case sensitive match is requested.
 	 * @param searchFor determines the nature of the searched elements
 	 *	<ul>
 	 * 	<li>{@link IDLTKSearchConstants#CLASS}: only look for classes</li>
 	 *		<li>{@link IDLTKSearchConstants#INTERFACE}: only look for interfaces</li>
 	 * 	<li>{@link IDLTKSearchConstants#ENUM}: only look for enumeration</li>
 	 *		<li>{@link IDLTKSearchConstants#ANNOTATION_TYPE}: only look for annotation type</li>
 	 * 	<li>{@link IDLTKSearchConstants#CLASS_AND_ENUM}: only look for classes and enumerations</li>
 	 *		<li>{@link IDLTKSearchConstants#CLASS_AND_INTERFACE}: only look for classes and interfaces</li>
 	 * 	<li>{@link IDLTKSearchConstants#TYPE}: look for all types (ie. classes, interfaces, enum and annotation types)</li>
 	 *	</ul>
 	 * @param scope the scope to search in
 	 * @param nameRequestor the requestor that collects the results of the search
 	 * @param waitingPolicy one of
 	 * <ul>
 	 *		<li>{@link IDLTKSearchConstants#FORCE_IMMEDIATE_SEARCH} if the search should start immediately</li>
 	 *		<li>{@link IDLTKSearchConstants#CANCEL_IF_NOT_READY_TO_SEARCH} if the search should be cancelled if the
 	 *			underlying indexer has not finished indexing the workspace</li>
 	 *		<li>{@link IDLTKSearchConstants#WAIT_UNTIL_READY_TO_SEARCH} if the search should wait for the
 	 *			underlying indexer to finish indexing the workspace</li>
 	 * </ul>
 	 * @param progressMonitor the progress monitor to report progress to, or <code>null</code> if no progress
 	 *							monitor is provided
 	 * @exception ModelException if the search failed. Reasons include:
 	 *	<ul>
 	 *		<li>the buildpath is incorrectly set</li>
 	 *	</ul>
 	 *
 	 */
 	public void searchAllTypeNames(
 		final char[] packageExactName, 
 		final char[] typeName,
 		final int matchRule, 
 		int searchFor, 
 		IDLTKSearchScope scope, 
 		final TypeNameRequestor nameRequestor,
 		int waitingPolicy,
 		IProgressMonitor progressMonitor)  throws ModelException {
 		
 		searchAllTypeNames(packageExactName, SearchPattern.R_EXACT_MATCH, typeName, matchRule, searchFor, scope, nameRequestor, waitingPolicy, progressMonitor);
 	}
 
 	/**
 	 * Searches for all top-level types and member types in the given scope.
 	 * The search can be selecting specific types (given a package name using specific match mode
 	 * and/or a type name using another specific match mode). 
 	 * 
 	 * @param packageName the full name of the package of the searched types, or a prefix for this
 	 *						package, or a wild-carded string for this package.
 	 * @param typeName the dot-separated qualified name of the searched type (the qualification include
 	 *					the enclosing types if the searched type is a member type), or a prefix
 	 *					for this type, or a wild-carded string for this type.
 	 * @param packageMatchRule one of
 	 * <ul>
 	 *		<li>{@link SearchPattern#R_EXACT_MATCH} if the package name and type name are the full names
 	 *			of the searched types.</li>
 	 *		<li>{@link SearchPattern#R_PREFIX_MATCH} if the package name and type name are prefixes of the names
 	 *			of the searched types.</li>
 	 *		<li>{@link SearchPattern#R_PATTERN_MATCH} if the package name and type name contain wild-cards.</li>
 	 *		<li>{@link SearchPattern#R_CAMELCASE_MATCH} if type name are camel case of the names of the searched types.</li>
 	 * </ul>
 	 * combined with {@link SearchPattern#R_CASE_SENSITIVE},
 	 *   e.g. {@link SearchPattern#R_EXACT_MATCH} | {@link SearchPattern#R_CASE_SENSITIVE} if an exact and case sensitive match is requested, 
 	 *   or {@link SearchPattern#R_PREFIX_MATCH} if a prefix non case sensitive match is requested.
 	 * @param typeMatchRule one of
 	 * <ul>
 	 *		<li>{@link SearchPattern#R_EXACT_MATCH} if the package name and type name are the full names
 	 *			of the searched types.</li>
 	 *		<li>{@link SearchPattern#R_PREFIX_MATCH} if the package name and type name are prefixes of the names
 	 *			of the searched types.</li>
 	 *		<li>{@link SearchPattern#R_PATTERN_MATCH} if the package name and type name contain wild-cards.</li>
 	 *		<li>{@link SearchPattern#R_CAMELCASE_MATCH} if type name are camel case of the names of the searched types.</li>
 	 * </ul>
 	 * combined with {@link SearchPattern#R_CASE_SENSITIVE},
 	 *   e.g. {@link SearchPattern#R_EXACT_MATCH} | {@link SearchPattern#R_CASE_SENSITIVE} if an exact and case sensitive match is requested, 
 	 *   or {@link SearchPattern#R_PREFIX_MATCH} if a prefix non case sensitive match is requested.
 	 * @param searchFor determines the nature of the searched elements
 	 *	<ul>
 	 * 	<li>{@link IDLTKSearchConstants#CLASS}: only look for classes</li>
 	 *		<li>{@link IDLTKSearchConstants#INTERFACE}: only look for interfaces</li>
 	 * 	<li>{@link IDLTKSearchConstants#ENUM}: only look for enumeration</li>
 	 *		<li>{@link IDLTKSearchConstants#ANNOTATION_TYPE}: only look for annotation type</li>
 	 * 	<li>{@link IDLTKSearchConstants#CLASS_AND_ENUM}: only look for classes and enumerations</li>
 	 *		<li>{@link IDLTKSearchConstants#CLASS_AND_INTERFACE}: only look for classes and interfaces</li>
 	 * 	<li>{@link IDLTKSearchConstants#TYPE}: look for all types (ie. classes, interfaces, enum and annotation types)</li>
 	 *	</ul>
 	 * @param scope the scope to search in
 	 * @param nameRequestor the requestor that collects the results of the search
 	 * @param waitingPolicy one of
 	 * <ul>
 	 *		<li>{@link IDLTKSearchConstants#FORCE_IMMEDIATE_SEARCH} if the search should start immediately</li>
 	 *		<li>{@link IDLTKSearchConstants#CANCEL_IF_NOT_READY_TO_SEARCH} if the search should be cancelled if the
 	 *			underlying indexer has not finished indexing the workspace</li>
 	 *		<li>{@link IDLTKSearchConstants#WAIT_UNTIL_READY_TO_SEARCH} if the search should wait for the
 	 *			underlying indexer to finish indexing the workspace</li>
 	 * </ul>
 	 * @param progressMonitor the progress monitor to report progress to, or <code>null</code> if no progress
 	 *							monitor is provided
 	 * @exception ModelException if the search failed. Reasons include:
 	 *	<ul>
 	 *		<li>the buildpath is incorrectly set</li>
 	 *	</ul>
 	 *
 	 */
 	public void searchAllTypeNames(
 		final char[] packageName, 
 		final int packageMatchRule, 
 		final char[] typeName,
 		final int typeMatchRule, 
 		int searchFor, 
 		IDLTKSearchScope scope, 
 		final TypeNameRequestor nameRequestor,
 		int waitingPolicy,
 		IProgressMonitor progressMonitor)  throws ModelException {
 		
 		TypeNameRequestorWrapper requestorWrapper = new TypeNameRequestorWrapper(nameRequestor);
 		this.basicEngine.searchAllTypeNames(packageName, packageMatchRule, typeName, typeMatchRule, searchFor, scope, requestorWrapper, waitingPolicy, progressMonitor);
 	}
 
 	/**
 	 * Searches for all top-level types and member types in the given scope.
 	 * The search can be selecting specific types (given a package name using specific match mode
 	 * and/or a type name using another specific match mode).
 	 * <p>
 	 * Provided {@link TypeNameMatchRequestor} requestor will collect {@link TypeNameMatch}
 	 * matches found during the search.
 	 * </p>
 	 * 
 	 * @param packageName the full name of the package of the searched types, or a prefix for this
 	 *						package, or a wild-carded string for this package.
 	 *						May be <code>null</code>, then any package name is accepted.
 	 * @param packageMatchRule one of
 	 * <ul>
 	 *		<li>{@link SearchPattern#R_EXACT_MATCH} if the package name and type name are the full names
 	 *			of the searched types.</li>
 	 *		<li>{@link SearchPattern#R_PREFIX_MATCH} if the package name and type name are prefixes of the names
 	 *			of the searched types.</li>
 	 *		<li>{@link SearchPattern#R_PATTERN_MATCH} if the package name and type name contain wild-cards.</li>
 	 *		<li>{@link SearchPattern#R_CAMELCASE_MATCH} if type name are camel case of the names of the searched types.</li>
 	 * </ul>
 	 * combined with {@link SearchPattern#R_CASE_SENSITIVE},
 	 *   e.g. {@link SearchPattern#R_EXACT_MATCH} | {@link SearchPattern#R_CASE_SENSITIVE} if an exact and case sensitive match is requested, 
 	 *   or {@link SearchPattern#R_PREFIX_MATCH} if a prefix non case sensitive match is requested.
 	 * @param typeName the dot-separated qualified name of the searched type (the qualification include
 	 *					the enclosing types if the searched type is a member type), or a prefix
 	 *					for this type, or a wild-carded string for this type.
 	 *					May be <code>null</code>, then any type name is accepted.
 	 * @param typeMatchRule one of
 	 * <ul>
 	 *		<li>{@link SearchPattern#R_EXACT_MATCH} if the package name and type name are the full names
 	 *			of the searched types.</li>
 	 *		<li>{@link SearchPattern#R_PREFIX_MATCH} if the package name and type name are prefixes of the names
 	 *			of the searched types.</li>
 	 *		<li>{@link SearchPattern#R_PATTERN_MATCH} if the package name and type name contain wild-cards.</li>
 	 *		<li>{@link SearchPattern#R_CAMELCASE_MATCH} if type name are camel case of the names of the searched types.</li>
 	 * </ul>
 	 * combined with {@link SearchPattern#R_CASE_SENSITIVE},
 	 *   e.g. {@link SearchPattern#R_EXACT_MATCH} | {@link SearchPattern#R_CASE_SENSITIVE} if an exact and case sensitive match is requested, 
 	 *   or {@link SearchPattern#R_PREFIX_MATCH} if a prefix non case sensitive match is requested.
 	 * @param searchFor determines the nature of the searched elements
 	 *	<ul>
 	 * 	<li>{@link IJavaSearchConstants#CLASS}: only look for classes</li>
 	 *		<li>{@link IJavaSearchConstants#INTERFACE}: only look for interfaces</li>
 	 * 	<li>{@link IJavaSearchConstants#ENUM}: only look for enumeration</li>
 	 *		<li>{@link IJavaSearchConstants#ANNOTATION_TYPE}: only look for annotation type</li>
 	 * 	<li>{@link IJavaSearchConstants#CLASS_AND_ENUM}: only look for classes and enumerations</li>
 	 *		<li>{@link IJavaSearchConstants#CLASS_AND_INTERFACE}: only look for classes and interfaces</li>
 	 * 	<li>{@link IJavaSearchConstants#TYPE}: look for all types (ie. classes, interfaces, enum and annotation types)</li>
 	 *	</ul>
 	 * @param scope the scope to search in
 	 * @param nameMatchRequestor the {@link TypeNameMatchRequestor requestor} that collects
 	 * 				{@link TypeNameMatch matches} of the search.
 	 * @param waitingPolicy one of
 	 * <ul>
 	 *		<li>{@link IJavaSearchConstants#FORCE_IMMEDIATE_SEARCH} if the search should start immediately</li>
 	 *		<li>{@link IJavaSearchConstants#CANCEL_IF_NOT_READY_TO_SEARCH} if the search should be cancelled if the
 	 *			underlying indexer has not finished indexing the workspace</li>
 	 *		<li>{@link IJavaSearchConstants#WAIT_UNTIL_READY_TO_SEARCH} if the search should wait for the
 	 *			underlying indexer to finish indexing the workspace</li>
 	 * </ul>
 	 * @param progressMonitor the progress monitor to report progress to, or <code>null</code> if no progress
 	 *							monitor is provided
 	 * @exception JavaModelException if the search failed. Reasons include:
 	 *	<ul>
 	 *		<li>the classpath is incorrectly set</li>
 	 *	</ul>
 	 * @since 3.3
 	 */
 	public void searchAllTypeNames(
 		final char[] packageName, 
 		final int packageMatchRule, 
 		final char[] typeName,
 		final int typeMatchRule, 
 		int searchFor, 
 		IDLTKSearchScope scope, 
 		final TypeNameMatchRequestor nameMatchRequestor,
 		int waitingPolicy,
 		IProgressMonitor progressMonitor)  throws ModelException {
 		
 		TypeNameMatchRequestorWrapper requestorWrapper = new TypeNameMatchRequestorWrapper(nameMatchRequestor, scope);
 		this.basicEngine.searchAllTypeNames(packageName, packageMatchRule, typeName, typeMatchRule, searchFor, scope, requestorWrapper, waitingPolicy, progressMonitor);
 	}
 	/**
 	 * Searches for all top-level types and member types in the given scope matching any of the given qualifications
 	 * and type names in a case sensitive way.
 	 * 
 	 * @param qualifications the qualified name of the package/enclosing type of the searched types
 	 * @param typeNames the simple names of the searched types
 	 * @param scope the scope to search in
 	 * @param nameRequestor the requestor that collects the results of the search
 	 * @param waitingPolicy one of
 	 * <ul>
 	 *		<li>{@link IDLTKSearchConstants#FORCE_IMMEDIATE_SEARCH} if the search should start immediately</li>
 	 *		<li>{@link IDLTKSearchConstants#CANCEL_IF_NOT_READY_TO_SEARCH} if the search should be cancelled if the
 	 *			underlying indexer has not finished indexing the workspace</li>
 	 *		<li>{@link IDLTKSearchConstants#WAIT_UNTIL_READY_TO_SEARCH} if the search should wait for the
 	 *			underlying indexer to finish indexing the workspace</li>
 	 * </ul>
 	 * @param progressMonitor the progress monitor to report progress to, or <code>null</code> if no progress
 	 *							monitor is provided
 	 * @exception ModelException if the search failed. Reasons include:
 	 *	<ul>
 	 *		<li>the buildpath is incorrectly set</li>
 	 *	</ul>
 	 *
 	 */
 	public void searchAllTypeNames(
 		final char[][] qualifications, 
 		final char[][] typeNames,
 		IDLTKSearchScope scope, 
 		final TypeNameRequestor nameRequestor,
 		int waitingPolicy,
 		IProgressMonitor progressMonitor)  throws ModelException {
 
 		TypeNameRequestorWrapper requestorWrapper = new TypeNameRequestorWrapper(nameRequestor);
 		this.basicEngine.searchAllTypeNames(
 			qualifications,
 			typeNames,
 			SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE,
 			IDLTKSearchConstants.TYPE,
 			scope,
 			requestorWrapper,
 			waitingPolicy,
 			progressMonitor);
 	}
 
 	/**
 	 * Searches for all top-level types and member types in the given scope matching any of the given qualifications
 	 * and type names in a case sensitive way.
 	 * <p>
 	 * Provided {@link TypeNameMatchRequestor} requestor will collect {@link TypeNameMatch}
 	 * matches found during the search.
 	 * </p>
 	 * 
 	 * @param qualifications the qualified name of the package/enclosing type of the searched types.
 	 *					May be <code>null</code>, then any package name is accepted.
 	 * @param typeNames the simple names of the searched types.
 	 *					If this parameter is <code>null</code>, then no type will be found.
 	 * @param scope the scope to search in
 	 * @param nameMatchRequestor the {@link TypeNameMatchRequestor requestor} that collects
 	 * 				{@link TypeNameMatch matches} of the search.
 	 * @param waitingPolicy one of
 	 * <ul>
 	 *		<li>{@link IJavaSearchConstants#FORCE_IMMEDIATE_SEARCH} if the search should start immediately</li>
 	 *		<li>{@link IJavaSearchConstants#CANCEL_IF_NOT_READY_TO_SEARCH} if the search should be cancelled if the
 	 *			underlying indexer has not finished indexing the workspace</li>
 	 *		<li>{@link IJavaSearchConstants#WAIT_UNTIL_READY_TO_SEARCH} if the search should wait for the
 	 *			underlying indexer to finish indexing the workspace</li>
 	 * </ul>
 	 * @param progressMonitor the progress monitor to report progress to, or <code>null</code> if no progress
 	 *							monitor is provided
 	 * @exception JavaModelException if the search failed. Reasons include:
 	 *	<ul>
 	 *		<li>the classpath is incorrectly set</li>
 	 *	</ul>
 	 */
 	public void searchAllTypeNames(
 		final char[][] qualifications, 
 		final char[][] typeNames,
 		IDLTKSearchScope scope, 
 		final TypeNameMatchRequestor nameMatchRequestor,
 		int waitingPolicy,
 		IProgressMonitor progressMonitor)  throws ModelException {
 
 		TypeNameMatchRequestorWrapper requestorWrapper = new TypeNameMatchRequestorWrapper(nameMatchRequestor, scope);
 		this.basicEngine.searchAllTypeNames(
 			qualifications,
 			typeNames,
 			SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE,
 			IDLTKSearchConstants.TYPE,
 			scope,
 			requestorWrapper,
 			waitingPolicy,
 			progressMonitor);
 	}
 
 
 	/**
 	 * Searches for all declarations of the fields accessed in the given element.
 	 * The element can be a compilation unit, a source type, or a source method.
 	 * Reports the field declarations using the given requestor.
 	 * <p>
 	 * Consider the following code:
 	 * <code>
 	 * <pre>
 	 *		class A {
 	 *			int field1;
 	 *		}
 	 *		class B extends A {
 	 *			String value;
 	 *		}
 	 *		class X {
 	 *			void test() {
 	 *				B b = new B();
 	 *				System.out.println(b.value + b.field1);
 	 *			};
 	 *		}
 	 * </pre>
 	 * </code>
 	 * then searching for declarations of accessed fields in method 
 	 * <code>X.test()</code> would collect the fields
 	 * <code>B.value</code> and <code>A.field1</code>.
 	 * </p>
 	 *
 	 * @param enclosingElement the method, type, or compilation unit to be searched in
 	 * @param requestor a callback object to which each match is reported
 	 * @param monitor the progress monitor used to report progress
 	 * @exception ModelException if the search failed. Reasons include:
 	 *	<ul>
 	 *		<li>the element doesn't exist</li>
 	 *		<li>the buildpath is incorrectly set</li>
 	 *	</ul>
 	 *
 	 */	
 	public void searchDeclarationsOfAccessedFields(IModelElement enclosingElement, SearchRequestor requestor, IProgressMonitor monitor) throws ModelException {
 		this.basicEngine.searchDeclarationsOfAccessedFields(enclosingElement, requestor, monitor);
 	}
 		
 	/**
 	 * Searches for all declarations of the types referenced in the given element.
 	 * The element can be a compilation unit, a source type, or a source method.
 	 * Reports the type declarations using the given requestor.
 	 * <p>
 	 * Consider the following code:
 	 * <code>
 	 * <pre>
 	 *		class A {
 	 *		}
 	 *		class B extends A {
 	 *		}
 	 *		interface I {
 	 *		  int VALUE = 0;
 	 *		}
 	 *		class X {
 	 *			void test() {
 	 *				B b = new B();
 	 *				this.foo(b, I.VALUE);
 	 *			};
 	 *		}
 	 * </pre>
 	 * </code>
 	 * then searching for declarations of referenced types in method <code>X.test()</code>
 	 * would collect the class <code>B</code> and the interface <code>I</code>.
 	 * </p>
 	 *
 	 * @param enclosingElement the method, type, or compilation unit to be searched in
 	 * @param requestor a callback object to which each match is reported
 	 * @param monitor the progress monitor used to report progress
 	 * @exception ModelException if the search failed. Reasons include:
 	 *	<ul>
 	 *		<li>the element doesn't exist</li>
 	 *		<li>the buildpath is incorrectly set</li>
 	 *	</ul>
 	 *
 	 */	
 	public void searchDeclarationsOfReferencedTypes(IModelElement enclosingElement, SearchRequestor requestor, IProgressMonitor monitor) throws ModelException {
 		this.basicEngine.searchDeclarationsOfReferencedTypes(enclosingElement, requestor, monitor);
 	}
 		
 	/**
 	 * Searches for all declarations of the methods invoked in the given element.
 	 * The element can be a compilation unit, a source type, or a source method.
 	 * Reports the method declarations using the given requestor.
 	 * <p>
 	 * Consider the following code:
 	 * <code>
 	 * <pre>
 	 *		class A {
 	 *			void foo() {};
 	 *			void bar() {};
 	 *		}
 	 *		class B extends A {
 	 *			void foo() {};
 	 *		}
 	 *		class X {
 	 *			void test() {
 	 *				A a = new B();
 	 *				a.foo();
 	 *				B b = (B)a;
 	 *				b.bar();
 	 *			};
 	 *		}
 	 * </pre>
 	 * </code>
 	 * then searching for declarations of sent messages in method 
 	 * <code>X.test()</code> would collect the methods
 	 * <code>A.foo()</code>, <code>B.foo()</code>, and <code>A.bar()</code>.
 	 * </p>
 	 *
 	 * @param enclosingElement the method, type, or compilation unit to be searched in
 	 * @param requestor a callback object to which each match is reported
 	 * @param monitor the progress monitor used to report progress
 	 * @exception ModelException if the search failed. Reasons include:
 	 *	<ul>
 	 *		<li>the element doesn't exist</li>
 	 *		<li>the buildpath is incorrectly set</li>
 	 *	</ul>
 	 *
 	 */	
 	public void searchDeclarationsOfSentMessages(IModelElement enclosingElement, SearchRequestor requestor, IProgressMonitor monitor) throws ModelException {
 		this.basicEngine.searchDeclarationsOfSentMessages(enclosingElement, requestor, monitor);
 	}
 	
 	public static ISourceModule[] searchMixinSources(String key, IDLTKLanguageToolkit toolkit ) {
 		final IDLTKSearchScope scope = SearchEngine.createWorkspaceScope(toolkit); 
 		// Index requestor
 		final HandleFactory factory = new HandleFactory();
 		final List modules = new ArrayList();
 		IndexQueryRequestor searchRequestor = new IndexQueryRequestor(){
 			public boolean acceptIndexMatch(String documentPath,
 					SearchPattern indexRecord, SearchParticipant participant,
 					AccessRuleSet access) {
 				if( documentPath.startsWith(SPECIAL_MIXIN)) {
 					documentPath = documentPath.substring(SPECIAL_MIXIN.length());
 				}
 				String s = IBuildpathEntry.BUILDIN_EXTERNAL_ENTRY.toString();
				if( documentPath.contains(s)) {
 					documentPath = documentPath.substring(documentPath.indexOf(s));
 				}
 				Openable createOpenable = factory.createOpenable(documentPath, scope);
 				if( createOpenable instanceof ISourceModule ) {
 					modules.add(createOpenable);
 				}
 				
 				return true;
 			}
 		};
 		IndexManager indexManager = ModelManager.getModelManager().getIndexManager();
 		
 		MixinPattern pattern = new MixinPattern(key.toCharArray(), SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE |  SearchPattern.R_PATTERN_MATCH);
 		// add type names from indexes
 		indexManager.performConcurrentJob(
 			new PatternSearchJob(
 				pattern, 
 				SearchEngine.getDefaultSearchParticipant(), // Script search only
 				scope, 
 				searchRequestor),
 				IDLTKSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
 			null);	
 		return (ISourceModule[])modules.toArray(new ISourceModule[modules.size()]);
 	}
 	
 	public static String[] searchMixinPatterns(String key, IDLTKLanguageToolkit toolkit) {
 		final IDLTKSearchScope scope = SearchEngine.createWorkspaceScope(toolkit); 
 		// Index requestor
 		final List result = new ArrayList();
 		IndexQueryRequestor searchRequestor = new IndexQueryRequestor(){
 			public boolean acceptIndexMatch(String documentPath,
 					SearchPattern indexRecord, SearchParticipant participant,
 					AccessRuleSet access) {
 				String val = new String( indexRecord.getIndexKey() );
 				if( !result.contains(val)) {
 					result.add(val);
 				}
 				
 				return true;
 			}
 		};
 		IndexManager indexManager = ModelManager.getModelManager().getIndexManager();
 		
 		int flags = SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE;
 		if (key.indexOf('*') != -1 || key.indexOf('?') != -1)
 			flags |= SearchPattern.R_PATTERN_MATCH;
 		
 		MixinPattern pattern = new MixinPattern(key.toCharArray(), flags);
 		// add type names from indexes
 		indexManager.performConcurrentJob(
 			new PatternSearchJob(
 				pattern, 
 				SearchEngine.getDefaultSearchParticipant(), // Script search only
 				scope, 
 				searchRequestor),
 				IDLTKSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
 			null);	
 		return (String[])result.toArray(new String[result.size()]);
 	}
 }
