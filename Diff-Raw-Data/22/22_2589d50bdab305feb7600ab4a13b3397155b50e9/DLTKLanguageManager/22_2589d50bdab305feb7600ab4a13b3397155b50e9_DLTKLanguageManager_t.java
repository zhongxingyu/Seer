 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.core;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.dltk.ast.declarations.ISourceParser;
 import org.eclipse.dltk.codeassist.ICompletionEngine;
 import org.eclipse.dltk.codeassist.ISelectionEngine;
 import org.eclipse.dltk.compiler.problem.DefaultProblemFactory;
 import org.eclipse.dltk.compiler.problem.IProblemFactory;
 import org.eclipse.dltk.core.search.DLTKSearchParticipant;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.IMatchLocatorParser;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.core.search.SearchRequestor;
 import org.eclipse.dltk.core.search.indexing.SourceIndexerRequestor;
 import org.eclipse.dltk.core.search.matching.MatchLocator;
 import org.eclipse.dltk.internal.core.ClassBasedDLTKExtensionManager;
 import org.eclipse.dltk.internal.core.NewInstanceClassBasedDLTKExtensionManager;
 import org.eclipse.dltk.internal.core.BasicDLTKExtensionManager.ElementInfo;
 
 public class DLTKLanguageManager {
 	private final static String LANGUAGE_EXTPOINT = DLTKCore.PLUGIN_ID
 			+ ".language";
 	private final static String SOURCE_ELEMENT_PARSERS_EXTPOINT = DLTKCore.PLUGIN_ID
 			+ ".sourceElementParsers";
 	private final static String SOURCE_PARSERS_EXTPOINT = DLTKCore.PLUGIN_ID
 			+ ".sourceParsers";
 	private final static String PROBLEM_FACTORY_EXTPOINT = DLTKCore.PLUGIN_ID
 			+ ".problemFactory";
 	private final static String COMPLETION_ENGINE_EXTPOINT = DLTKCore.PLUGIN_ID
 			+ ".completionEngine";
 	private final static String SELECTION_ENGINE_EXTPOINT = DLTKCore.PLUGIN_ID
 			+ ".selectionEngine";
 	private final static String SEARCH_EXTPOINT = DLTKCore.PLUGIN_ID
 			+ ".search";
 	private final static String CALLHIERARCHY_EXTPOINT = DLTKCore.PLUGIN_ID
 			+ ".callHierarchy";
 
 	private static ClassBasedDLTKExtensionManager instance = new ClassBasedDLTKExtensionManager(
 			LANGUAGE_EXTPOINT);
 
 	// Inner managers
 	private static ClassBasedDLTKExtensionManager sourceElementParsersManager = new NewInstanceClassBasedDLTKExtensionManager(
 			SOURCE_ELEMENT_PARSERS_EXTPOINT);
 	private static ClassBasedDLTKExtensionManager problemFactoryManager = new ClassBasedDLTKExtensionManager(
 			PROBLEM_FACTORY_EXTPOINT);
 
 	private static ClassBasedDLTKExtensionManager selectionEngineManager = new NewInstanceClassBasedDLTKExtensionManager(
 			SELECTION_ENGINE_EXTPOINT);
 	private static ClassBasedDLTKExtensionManager completionEngineManager = new NewInstanceClassBasedDLTKExtensionManager(
 			COMPLETION_ENGINE_EXTPOINT);
 	private static ClassBasedDLTKExtensionManager sourceParsersManager = new NewInstanceClassBasedDLTKExtensionManager(
 			SOURCE_PARSERS_EXTPOINT);
 
 	private static ClassBasedDLTKExtensionManager searchManager = new ClassBasedDLTKExtensionManager(
 			SEARCH_EXTPOINT);
 	private static ClassBasedDLTKExtensionManager callHierarchyManager = new ClassBasedDLTKExtensionManager(
 			CALLHIERARCHY_EXTPOINT);
 
 	public static IDLTKLanguageToolkit getLanguageToolkit(String natureId)
 			throws CoreException {
 
 		return (IDLTKLanguageToolkit) instance.getObject(natureId);
 	}
 
 	private static IDLTKLanguageToolkit findAppropriateToolkitByObject(
 			Object object) {
 		ElementInfo[] elementInfos = instance.getElementInfos();
 		for (int j = 0; j < elementInfos.length; j++) {
 			IDLTKLanguageToolkit toolkit = (IDLTKLanguageToolkit) instance
 					.getInitObject(elementInfos[j]);
 			if (object instanceof IResource) {
 				if (toolkit.validateSourceModule((IResource) object)
 						.getSeverity() == Status.OK) {
 					return toolkit;
 				}
 			} else if (object instanceof IPath) {
 				if (toolkit.validateSourceModule((IPath) object).getSeverity() == Status.OK) {
 					return toolkit;
 				}
 			} else {
 				return null;
 			}
 		}
 		return null;
 	}
 
 	public static boolean hasScriptNature(IProject project) {
 		return instance.findScriptNature(project) != null;
 	}
 
 	public static IDLTKLanguageToolkit getLanguageToolkit(IModelElement element)
 			throws CoreException {
 		return (IDLTKLanguageToolkit) instance.getObject(element);
 	}
 
 	public static IDLTKLanguageToolkit findToolkit(IResource resource) {
 		return findAppropriateToolkitByObject(resource);
 	}
 
 	public static IDLTKLanguageToolkit findToolkit(IPath path) {
 		return findAppropriateToolkitByObject(path);
 	}
 
 	public static ISourceElementParser getSourceElementParser(String nature)
 			throws CoreException {
 		return (ISourceElementParser) sourceElementParsersManager
 				.getObject(nature);
 	}
 
 	public static ISourceElementParser getSourceElementParser(
 			IModelElement element) throws CoreException {
 		return (ISourceElementParser) sourceElementParsersManager
 				.getObject(element);
 	}
 
	// public static ISourceParser getSourceParser( String nature ) throws
	// CoreException {
	// return (ISourceElementParser) sourceParsersManager.getObject(nature);
	// }
	//	
	// public static ISourceParser getSourceParser( IModelElement element )
	// throws
	// CoreException {
	// return (ISourceElementParser) sourceParsersManager.getObject(element);
	// }
 
 	public static IProblemFactory getProblemFactory(String natureID)
 			throws CoreException {
 		IProblemFactory factory = (IProblemFactory) problemFactoryManager
 				.getObject(natureID);
 		if (factory != null) {
 			return factory;
 		}
 		return new DefaultProblemFactory();
 	}
 
 	public static IProblemFactory getProblemFactory(IModelElement element)
 			throws CoreException {
 		IProblemFactory factory = (IProblemFactory) problemFactoryManager
 				.getObject(element);
 		if (factory != null) {
 			return factory;
 		}
 		return new DefaultProblemFactory();
 	}
 
 	public static ICompletionEngine getCompletionEngine(String natureID)
 			throws CoreException {
 		return (ICompletionEngine) completionEngineManager.getObject(natureID);
 	}
 
 	public static ISelectionEngine getSelectionEngine(String natureID)
 			throws CoreException {
 		return (ISelectionEngine) selectionEngineManager.getObject(natureID);
 	}
 
 	public static ISourceParser getSourceParser(String natureID)
 			throws CoreException {
 		return (ISourceParser) sourceParsersManager.getObject(natureID);
 	}
 
 	/**
 	 * Return source parser witch is one level lower from top. If this is only
	 * one source parser for selected nature then return null.
 	 * 
 	 */
 	public static ISourceParser getSourceParserLower(String natureID)
 			throws CoreException {
 		return (ISourceParser) sourceParsersManager.getObjectLower(natureID);
 	}
 
 	public static DLTKSearchParticipant createSearchParticipant(String natureID) {
 		ISearchFactory factory = getSearchFactory(natureID);
 		if (factory != null) {
 			DLTKSearchParticipant participant = factory
 					.createSearchParticipant();
 			if (participant != null) {
 				return participant;
 			}
 		}
 		return new DLTKSearchParticipant();
 	}
 
 	private static ISearchFactory getSearchFactory(String natureId) {
 		return (ISearchFactory) searchManager.getObject(natureId);
 	}
 
 	public static MatchLocator createMatchLocator(String natureID,
 			SearchPattern pattern, SearchRequestor requestor,
 			IDLTKSearchScope scope, SubProgressMonitor subProgressMonitor) {
 		ISearchFactory factory = getSearchFactory(natureID);
 		if (factory != null) {
 			MatchLocator locator = factory.createMatchLocator(pattern,
 					requestor, scope, subProgressMonitor);
 			if (locator != null) {
 				return locator;
 			}
 		}
 		return new MatchLocator(pattern, requestor, scope, subProgressMonitor);
 	}
 
 	public static SourceIndexerRequestor createSourceRequestor(String natureID) {
 		ISearchFactory factory = getSearchFactory(natureID);
 		if (factory != null) {
 			SourceIndexerRequestor requestor = factory.createSourceRequestor();
 			if (requestor != null) {
 				return requestor;
 			}
 		}
 		return new SourceIndexerRequestor();
 	}
 
 	public static IMatchLocatorParser createMatchParser(String natureID,
 			MatchLocator matchLocator) {
 		ISearchFactory factory = getSearchFactory(natureID);
 		if (factory != null) {
 			return factory.createMatchParser(matchLocator);
 		}
 		return null;
 	}
 
 	public static ICalleeProcessor createCalleeProcessor(String natureID,
 			IMethod member, IProgressMonitor progressMonitor,
 			IDLTKSearchScope scope) {
 		ICallHierarchyFactory factory = getCallHierarchyFactory(natureID);
 		if (factory != null) {
 			ICalleeProcessor processor = factory.createCalleeProcessor(member,
 					progressMonitor, scope);
 			return processor;
 		}
 		return null;
 	}
 
 	private static ICallHierarchyFactory getCallHierarchyFactory(String natureId) {
 		return (ICallHierarchyFactory) callHierarchyManager.getObject(natureId);
 	}
 
 	public static ICallProcessor createCallProcessor(String natureID) {
 		ICallHierarchyFactory factory = getCallHierarchyFactory(natureID);
 		if (factory != null) {
 			return factory.createCallProcessor();
 		}
 		return null;
 	}
 }
