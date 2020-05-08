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
 package org.eclipse.dltk.internal.core.hierarchy;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IFileHierarchyInfo;
 import org.eclipse.dltk.core.IFileHierarchyResolver;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.search.IDLTKSearchConstants;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.core.search.SearchMatch;
 import org.eclipse.dltk.core.search.SearchParticipant;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.core.search.SearchRequestor;
 import org.eclipse.dltk.internal.core.Openable;
 
 public class HierarchyResolver {
 
 	private HierarchyBuilder hierarchyBuilder;
 	private SearchEngine engine;
 
 	public HierarchyResolver(HierarchyBuilder hierarchy) {
 		this.hierarchyBuilder = hierarchy;
 		this.engine = new SearchEngine();
 	}
 
 	public void resolve(boolean computeSubtypes) throws CoreException {
 
 		IType focusType = hierarchyBuilder.getType();
 
 		hierarchyBuilder.hierarchy.initialize(0);
 
 		if (computeSubtypes) {
 			computeSubtypes(focusType);
 		}
 
 		computeSupertypes(focusType);
 	}
 
 	protected void computeSubtypes(IType focusType) throws CoreException {
 
 		// Collect all inheritance information:
 		final Map superTypeToExtender = new HashMap();
 		SearchRequestor typesCollector = new SearchRequestor() {
 			public void acceptSearchMatch(SearchMatch match)
 					throws CoreException {
 
 				IType element = (IType) match.getElement();
 				String[] superClasses = element.getSuperClasses();
 				if (superClasses != null) {
 					for (int i = 0; i < superClasses.length; i++) {
 						String s = superClasses[i];
 						if (!superTypeToExtender.containsKey(s)) {
 							superTypeToExtender.put(s, new LinkedList());
 						}
 						List extenders = (List) superTypeToExtender.get(s);
 						extenders.add(element.getElementName());
 					}
 				}
 			}
 		};
 		SearchPattern pattern = SearchPattern.createPattern(
 				"*", //$NON-NLS-1$
 				IDLTKSearchConstants.TYPE, IDLTKSearchConstants.DECLARATIONS,
 				SearchPattern.R_REGEXP_MATCH, hierarchyBuilder.hierarchy.scope
 						.getLanguageToolkit());
 		engine.search(pattern, new SearchParticipant[] { SearchEngine
 				.getDefaultSearchParticipant() },
 				hierarchyBuilder.hierarchy.scope, typesCollector,
 				hierarchyBuilder.hierarchy.progressMonitor);
 
 		IFileHierarchyResolver fileHierarchyResolver = createFileHierarchyResolver(focusType);
 		IFileHierarchyInfo hierarchyInfo = null;
 		if (fileHierarchyResolver != null) {
 			hierarchyInfo = fileHierarchyResolver.resolveDown(focusType
 					.getSourceModule(),
 					hierarchyBuilder.hierarchy.progressMonitor);
 		}
 
 		computeSubtypesFor(focusType, superTypeToExtender, new HashMap(),
				hierarchyInfo, new HashSet());
 	}
 
 	protected void computeSubtypesFor(IType focusType, Map superTypeToExtender,
			Map subTypesCache, IFileHierarchyInfo hierarchyInfo, Set processedTypes)
 			throws CoreException {
		
		processedTypes.add(focusType);
		
 		List extenders = (List) superTypeToExtender.get(focusType
 				.getElementName());
 		if (extenders != null) {
 			IType[] subTypes = searchTypes((String[]) extenders
 					.toArray(new String[extenders.size()]), subTypesCache,
 					hierarchyInfo);
 			for (int i = 0; i < subTypes.length; i++) {
 				IType subType = subTypes[i];
 				hierarchyBuilder.hierarchy.addSubtype(focusType, subType);
 			}
 
 			for (int i = 0; i < subTypes.length; i++) {
 				IType subType = subTypes[i];
				if (!processedTypes.contains(subType)) {
					computeSubtypesFor(subType, superTypeToExtender, subTypesCache,
						hierarchyInfo, processedTypes);
				}
 			}
 		}
 	}
 
 	protected void computeSupertypes(IType focusType) throws CoreException {
 		IFileHierarchyResolver fileHierarchyResolver = createFileHierarchyResolver(focusType);
 		IFileHierarchyInfo hierarchyInfo = null;
 		if (fileHierarchyResolver != null) {
 			hierarchyInfo = fileHierarchyResolver.resolveUp(focusType
 					.getSourceModule(),
 					hierarchyBuilder.hierarchy.progressMonitor);
 		}
 
		computeSupertypesFor(focusType, hierarchyInfo, new HashSet());
 	}
 
 	protected void computeSupertypesFor(IType focusType,
			IFileHierarchyInfo hierarchyInfo, Set processedTypes) throws CoreException {
		
		processedTypes.add(focusType);
		
 		// Build superclasses hieararchy:
 		String[] superClasses = focusType.getSuperClasses();
 		if (superClasses != null && superClasses.length > 0) {
 			IType[] searchTypes = searchTypes(superClasses, hierarchyInfo);
 
 			for (int i = 0; i < searchTypes.length; i++) {
 				IType superclass = searchTypes[i];
 				hierarchyBuilder.hierarchy.cacheSuperclass(focusType,
 						superclass);
 			}
 
 			for (int i = 0; i < searchTypes.length; i++) {
 				IType superclass = searchTypes[i];
				if (!processedTypes.contains(superclass)) {
					computeSupertypesFor(superclass, hierarchyInfo, processedTypes);
				}
 			}
 		} else {
 			if (!hierarchyBuilder.hierarchy.contains(focusType)) {
 				hierarchyBuilder.hierarchy.addRootClass(focusType);
 			}
 		}
 	}
 
 	protected IType[] searchTypes(String[] types,
 			IFileHierarchyInfo hierarchyInfo) throws CoreException {
 		return searchTypes(types, null, hierarchyInfo);
 	}
 
 	protected IType[] searchTypes(String[] types, Map cache,
 			IFileHierarchyInfo hierarchyInfo) throws CoreException {
 		List result = new LinkedList();
 		for (int i = 0; i < types.length; i++) {
 			String type = types[i];
 			result.addAll(Arrays
 					.asList(searchTypes(type, cache, hierarchyInfo)));
 		}
 		return (IType[]) result.toArray(new IType[result.size()]);
 	}
 
 	protected IType[] searchTypes(String type, IFileHierarchyInfo hierarchyInfo)
 			throws CoreException {
 		return searchTypes(type, null, hierarchyInfo);
 	}
 
 	protected IType[] searchTypes(String type, Map cache,
 			final IFileHierarchyInfo hierarchyInfo) throws CoreException {
 		if (cache != null && cache.containsKey(type)) {
 			return (IType[]) cache.get(type);
 		}
 
 		final List result = new LinkedList();
 		final List filteredTypes = new LinkedList();
 
 		SearchRequestor typesCollector = new SearchRequestor() {
 			public void acceptSearchMatch(SearchMatch match)
 					throws CoreException {
 				IType type = (IType) match.getElement();
 				if (hierarchyInfo != null
 						&& !hierarchyInfo.exists(type.getSourceModule())) {
 					filteredTypes.add(type);
 					return;
 				}
 				result.add(type);
 			}
 		};
 		SearchPattern pattern = SearchPattern.createPattern(type,
 				IDLTKSearchConstants.TYPE, IDLTKSearchConstants.DECLARATIONS,
 				SearchPattern.R_EXACT_MATCH, hierarchyBuilder.hierarchy.scope.getLanguageToolkit());
 		engine.search(pattern, new SearchParticipant[] { SearchEngine
 				.getDefaultSearchParticipant() },
 				hierarchyBuilder.hierarchy.scope, typesCollector,
 				hierarchyBuilder.hierarchy.progressMonitor);
 
 		// If all results where filtered that means we could find a path to any
 		// of elements.
 		// In this case return all elements.
 		if (result.isEmpty()) {
 			result.addAll(filteredTypes);
 		}
 
 		IType[] types = (IType[]) result.toArray(new IType[result.size()]);
 		if (cache != null) {
 			cache.put(type, types);
 		}
 		return types;
 	}
 
 	public void resolve(Openable[] openables, HashSet localTypes) {
 		try {
 			resolve(true);
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private static IFileHierarchyResolver createFileHierarchyResolver(IType type)
 			throws CoreException {
 		IFileHierarchyResolver fileHierarchyResolver = null;
 		IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 				.getLanguageToolkit(type);
 		if (toolkit != null) {
 			fileHierarchyResolver = DLTKLanguageManager
 					.getFileHierarchyResolver(toolkit.getNatureId());
 		}
 		return fileHierarchyResolver;
 	}
 }
