 package org.eclipse.dltk.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.core.search.IDLTKSearchConstants;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.core.search.TypeNameMatch;
 import org.eclipse.dltk.core.search.TypeNameMatchRequestor;
 
 
 public class DLTKModelUtil {
 	/**
 	 * Force a reconcile of a compilation unit.
 	 * @param unit
 	 */
 	public static void reconcile(ISourceModule unit) throws ModelException {
 		unit.reconcile(				
 				false /* don't force problem detection */, 
 				null /* use primary owner */, 
 				null /* no progress monitor */);
 	}
 	
 	public static boolean isPrimary(ISourceModule unit) {
 		return unit.getOwner() == null;
 	}
 
 	public static boolean isExcludedPath(IPath resourcePath,
 			IPath[] exclusionPatterns) {
 		char[] path = resourcePath.toString().toCharArray();
 		for (int i = 0, length = exclusionPatterns.length; i < length; i++) {
 			char[] pattern = exclusionPatterns[i].toString().toCharArray();
 			if (CharOperation.pathMatch(pattern, path, true, '/')) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns the package fragment root of <code>IModelElement</code>. If
 	 * the given element is already a package fragment root, the element itself
 	 * is returned.
 	 */
 	public static IProjectFragment getProjectFragment(IModelElement element) {
 		return (IProjectFragment) element
 				.getAncestor(IModelElement.PROJECT_FRAGMENT);
 	}
 
 	public static IModelElement findInSourceModule(ISourceModule cu,
 			IModelElement element) {
 		if (DLTKCore.DEBUG) {
 			System.err.println("TODO: Implement find in source module...");
 		}
 		return null;
 	}
 	
 	
 	private static void searchTypeDeclarations (IDLTKProject project, String patternString, 
 			TypeNameMatchRequestor requestor) {
 		final List types = new ArrayList ();
 		
 		patternString = "*" + patternString + "*";
 		
		IDLTKLanguageToolkit langaugeToolkit;
		try {
			langaugeToolkit = DLTKLanguageManager.getLangaugeToolkit(project);
		} catch (CoreException e1) {
			return;
		}
		//FIXME: use another search		
		IDLTKSearchScope scope = SearchEngine.createWorkspaceScope(langaugeToolkit);//createSearchScope(new IModelElement[] {project});		
 		try {
 			SearchEngine engine = new SearchEngine();
 			engine.searchAllTypeNames(
 					null, 
 					SearchPattern.R_PATTERN_MATCH,
 					patternString.toCharArray(), 
 					SearchPattern.R_PATTERN_MATCH, 
 					IDLTKSearchConstants.TYPE, 
 					scope, 
 					requestor, 
 					IDLTKSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, 
 					null);
 		} catch (CoreException e) {			
 			if (DLTKCore.DEBUG)
 				e.printStackTrace();
 		}		
 	}
 	
 	/**
 	 * Returns all the types with a given qualified name, that are located as childs of element.
 	 * @param element
 	 * @param qualifiedName
 	 * @param delimeter
 	 * @return
 	 */
	public static IType[] getAllTypes(final IDLTKProject project, final String qualifiedName, final String delimeter) {
 		final List types = new ArrayList ();
 
 		TypeNameMatchRequestor requestor = new TypeNameMatchRequestor() {
 
 			public void acceptTypeNameMatch(TypeNameMatch match) {
 				IType type = (IType) match.getType();
				if (!type.getScriptProject().equals(project))
					return;
 				if (type.getTypeQualifiedName(delimeter).equals(qualifiedName)) {
 					types.add(type);
 				}
 			}
 			
 			
 		};
 		
 		String[] names = qualifiedName.split(delimeter);
 		if (names == null || names.length == 0)
 			return new IType[0];
 		String patternString = names[names.length - 1];
 		
 		searchTypeDeclarations(project, patternString, requestor);	
 		
 		return (IType[]) types.toArray(new IType[types.size()]);
 	}
 	
 	
 	/**
 	 * Returns all ITypes, which fqn ends with nameEnding.
 	 * @param project
 	 * @param nameEnding
 	 * @param delimeter
 	 * @return
 	 */
	public static IType[] getAllTypesWithFQNEnding(final IDLTKProject project, final String nameEnding, final String delimeter) {
 		final List types = new ArrayList ();
 
 		TypeNameMatchRequestor requestor = new TypeNameMatchRequestor() {
 			
 			public void acceptTypeNameMatch(TypeNameMatch match) {				
 				IType type = (IType) match.getType();
				if (!type.getScriptProject().equals(project))
					return;
 				if (type.getTypeQualifiedName(delimeter).endsWith(nameEnding)) {
 					types.add(type);
 				}
 			}
 		};		
 		String[] names = nameEnding.split(delimeter);
 		if (names == null || names.length == 0)
 			return new IType[0];
 		String patternString = names[names.length - 1];		
 		searchTypeDeclarations(project, patternString, requestor);
 		
 		return (IType[]) types.toArray(new IType[types.size()]);
 	}
 	
 	public static IType[] getAllScopedTypes(IDLTKProject project, 
 			String typeName, 
 			String delimeter,
 			String scopeFqn) {
 		List types = new ArrayList ();
 		int curLength = -1;
 
 		IType[] allTypes = getAllTypesWithFQNEnding(project, typeName, delimeter);
 		
 		if (!typeName.startsWith(delimeter))	
 			typeName = delimeter + typeName;
 		for (int i = 0; i < allTypes.length; i++) {
 			String name = allTypes[i].getTypeQualifiedName(delimeter);
 			if (!name.endsWith(typeName))
 				continue;
 			String start = name.substring(0, name.length() - typeName.length());
 			//String start = name.substring(0, name.lastIndexOf(delimeter));
 			int length = start.length();
 			if  (length < curLength)
 				continue;
 			if (scopeFqn.startsWith(start)) {
 				if (length > curLength)  {
 					types.clear();
 					curLength = length;
 				}
 				types.add(allTypes[i]);
 			}
 		}
 		
 		return (IType[]) types.toArray(new IType[types.size()]);
 	}
 	
 	public static IModelElement findType(IModelElement module, 
 			String qualifiedName, String delimeter) {
 		
 		if (module instanceof IType) {
 			IType type = (IType)module;
 			String tmpFqn = type.getTypeQualifiedName(delimeter);
 			if (!tmpFqn.startsWith(delimeter))
 				tmpFqn = delimeter + tmpFqn;
 			if (tmpFqn.equals(qualifiedName)) 
 				return type;			
 		}
 		
 		if (module instanceof IParent) {
 			IModelElement el = null;
 			IParent p = (IParent)module;
 			IModelElement[] childs;
 			try {
 				childs = p.getChildren();
 				for (int i = 0; i < childs.length; i++) {
 					el = findType(childs[i], qualifiedName, delimeter);
 					if (el != null)
 						return el;
 				}
 			} catch (ModelException e) {
 				if (DLTKCore.DEBUG)
 					e.printStackTrace();
 				return null;
 			}
 		}
 		
 		return null;
 	}
 
 	public static boolean isExceptionToBeLogged(CoreException exception) {
 		if (!(exception instanceof ModelException))
 			return true;
 		ModelException je = (ModelException) exception;
 		if (!je.isDoesNotExist())
 			return true;
 		if (DLTKCore.DEBUG) {
 			System.err
 					.println("TODO: ModelUtil: isExceptionToBeLogged. Require addition of ModelStatus.getgetElements method...");
 		}
 		// IModelElement[] elements= je.getModelStatus().getElements();
 		// for (int i= 0; i < elements.length; i++) {
 		// IModelElement element= elements[i];
 		// // if the element is already a compilation unit don't log
 		// // does not exist exceptions. See bug
 		// // https://bugs.eclipse.org/bugs/show_bug.cgi?id=75894
 		// // for more details
 		// if (element.getElementType() == IScriptElement.COMPILATION_UNIT)
 		// continue;
 		// ISourceModule unit=
 		// (ISourceModule)element.getAncestor(IScriptElement.COMPILATION_UNIT);
 		// if (unit == null)
 		// return true;
 		// if (!unit.isWorkingCopy())
 		// return true;
 		// }
 		return true;// false
 	}
 
 	public static String getFullyQualifiedName(IType type) {
 		return type.getFullyQualifiedName();
 	}
 
 	public static String getRenamedCUName(ISourceModule cu, String newMainName) {
 		String oldName = cu.getElementName();
 		int i = oldName.lastIndexOf('.');
 		if (i != -1) {
 			return newMainName + oldName.substring(i);
 		} else {
 			return newMainName;
 		}
 	}
 	
 	public static boolean isSuperType(ITypeHierarchy hierarchy, IType possibleSuperType, IType type) {
 		// filed bug 112635 to add this method to ITypeHierarchy
 		IType[] superClass= hierarchy.getSuperclass(type);
 		if (superClass != null ) {
 			for( int q = 0; q < superClass.length; ++q ) {
 				if( (possibleSuperType.equals(superClass[q]) || isSuperType(hierarchy, possibleSuperType, superClass[q]))) {
 					return true;
 				}	
 			}
 		}
 		return false;
 	}
 	
 }
