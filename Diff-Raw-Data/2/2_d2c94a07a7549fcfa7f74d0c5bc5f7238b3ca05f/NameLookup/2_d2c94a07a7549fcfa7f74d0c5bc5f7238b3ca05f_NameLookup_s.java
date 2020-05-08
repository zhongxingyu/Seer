 /*******************************************************************************
  * Copyright (c) 2000, 2012 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     eBay Inc - modification
  *******************************************************************************/
 package org.eclipse.dltk.mod.internal.core;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.mod.compiler.CharOperation;
 import org.eclipse.dltk.mod.core.DLTKCore;
 import org.eclipse.dltk.mod.core.IBuildpathEntry;
 import org.eclipse.dltk.mod.core.IModelElement;
 import org.eclipse.dltk.mod.core.IProjectFragment;
 import org.eclipse.dltk.mod.core.IScriptFolder;
 import org.eclipse.dltk.mod.core.IScriptProject;
 import org.eclipse.dltk.mod.core.ISourceModule;
 import org.eclipse.dltk.mod.core.IType;
 import org.eclipse.dltk.mod.core.ModelException;
 import org.eclipse.dltk.mod.internal.compiler.env.AccessRestriction;
 import org.eclipse.dltk.mod.internal.compiler.env.AccessRuleSet;
 import org.eclipse.dltk.mod.internal.core.util.HashtableOfArrayToObject;
 import org.eclipse.dltk.mod.internal.core.util.Messages;
 import org.eclipse.dltk.mod.internal.core.util.Util;
 
 /**
  * A <code>NameLookup</code> provides name resolution within a Script project.
  * The name lookup facility uses the project's classpath to prioritize the order
  * in which package fragments are searched when resolving a name.
  * 
  * <p>
  * Name lookup only returns a handle when the named element actually exists in
  * the model; otherwise <code>null</code> is returned.
  * 
  * <p>
  * There are two logical sets of methods within this interface. Methods which
  * start with <code>find*</code> are intended to be convenience methods for
  * quickly finding an element within another element; for instance, for finding
  * a class within a package. The other set of methods all begin with
  * <code>seek*</code>. These methods do comprehensive searches of the
  * <code>IScriptProject</code> returning hits in real time through an
  * <code>IModelElementRequestor</code>.
  * 
  */
 public class NameLookup {
 	public static class Answer {
 		public IType type;
 		AccessRestriction restriction;
 
 		Answer(IType type, AccessRestriction restriction) {
 			this.type = type;
 			this.restriction = restriction;
 		}
 
 		public boolean ignoreIfBetter() {
 			return this.restriction != null
 					&& this.restriction.ignoreIfBetter();
 		}
 
 		/*
 		 * Returns whether this answer is better than the other awswer.
 		 * (accessible is better than discouraged, which is better than
 		 * non-accessible)
 		 */
 		public boolean isBetter(Answer otherAnswer) {
 			if (otherAnswer == null)
 				return true;
 			if (this.restriction == null)
 				return true;
 			return otherAnswer.restriction != null
 					&& this.restriction.getProblemId() < otherAnswer.restriction
 							.getProblemId();
 		}
 	}
 
 	/*
 	 * Accept flag for all kinds of types
 	 */
 
 	public static boolean VERBOSE = DLTKCore.VERBOSE_SEARCH_NAMELOOKUP;
 
 	private static final IType[] NO_TYPES = {};
 
 	public static final int ACCEPT_ALL = 0;
 
 	/**
 	 * The <code>IScriptFolderRoot</code>'s associated with the buildpath of
 	 * this NameLookup facility's project.
 	 */
 	protected IProjectFragment[] projectFragments;
 
 	/**
 	 * Table that maps package names to lists of package fragment roots that
 	 * contain such a package known by this name lookup facility. To allow > 1
 	 * package fragment with the same name, values are arrays of package
 	 * fragment roots ordered as they appear on the buildpath. Note if the list
 	 * is of size 1, then the IScriptFolderRoot object replaces the array.
 	 */
 	protected HashtableOfArrayToObject scriptFolders;
 
 	/*
 	 * A set of names (String[]) that are known to be package names. Value is
 	 * not null for known package.
 	 */
 	protected HashtableOfArrayToObject isPackageCache;
 
 	/**
 	 * Reverse map from root path to corresponding resolved CP entry (so as to
 	 * be able to figure inclusion/exclusion rules)
 	 */
 	protected Map rootToResolvedEntries;
 
 	/**
 	 * A map from package handles to a map from type name to an IType or an
 	 * IType[]. Allows working copies to take precedence over compilation units.
 	 */
 	protected HashMap typesInWorkingCopies;
 
 	public long timeSpentInSeekTypesInSourcePackage = 0;
 	public long timeSpentInSeekTypesInBinaryPackage = 0;
 
 	public NameLookup(IProjectFragment[] ProjectFragments,
 			HashtableOfArrayToObject ScriptFolders,
 			HashtableOfArrayToObject isPackage, ISourceModule[] workingCopies,
 			Map rootToResolvedEntries) {
 		long start = -1;
 		if (VERBOSE) {
 			Util.verbose(" BUILDING NameLoopkup"); //$NON-NLS-1$
 			Util.verbose(" -> pkg roots size: " + (ProjectFragments == null ? 0 : ProjectFragments.length)); //$NON-NLS-1$
 			Util.verbose(" -> pkgs size: " + (ScriptFolders == null ? 0 : ScriptFolders.size())); //$NON-NLS-1$
 			Util.verbose(" -> working copy size: " + (workingCopies == null ? 0 : workingCopies.length)); //$NON-NLS-1$
 			start = System.currentTimeMillis();
 		}
 		this.projectFragments = ProjectFragments;
 		if (workingCopies == null) {
 			this.scriptFolders = ScriptFolders;
 			this.isPackageCache = isPackage;
 		} else {
 			// clone tables as we're adding packages from working copies
 			try {
 				this.scriptFolders = (HashtableOfArrayToObject) ScriptFolders
 						.clone();
 				this.isPackageCache = (HashtableOfArrayToObject) isPackage
 						.clone();
 			} catch (CloneNotSupportedException e1) {
 				// ignore (implementation of HashtableOfArrayToObject supports
 				// cloning)
 			}
 			this.typesInWorkingCopies = new HashMap();
 			for (int i = 0, length = workingCopies.length; i < length; i++) {
 				ISourceModule workingCopy = workingCopies[i];
 				ScriptFolder pkg = (ScriptFolder) workingCopy.getParent();
 				HashMap typeMap = (HashMap) this.typesInWorkingCopies.get(pkg);
 				if (typeMap == null) {
 					typeMap = new HashMap();
 					this.typesInWorkingCopies.put(pkg, typeMap);
 				}
 				try {
 					IType[] types = workingCopy.getTypes();
 					int typeLength = types.length;
 					if (typeLength == 0) {
 						String typeName = workingCopy.getElementName();
 						typeMap.put(typeName, NO_TYPES);
 					} else {
 						for (int j = 0; j < typeLength; j++) {
 							IType type = types[j];
 							String typeName = type.getElementName();
 							Object existing = typeMap.get(typeName);
 							if (existing == null) {
 								typeMap.put(typeName, type);
 							} else if (existing instanceof IType) {
 								typeMap.put(typeName, new IType[] {
 										(IType) existing, type });
 							} else {
 								IType[] existingTypes = (IType[]) existing;
 								int existingTypeLength = existingTypes.length;
 								System.arraycopy(
 										existingTypes,
 										0,
 										existingTypes = new IType[existingTypeLength + 1],
 										0, existingTypeLength);
 								existingTypes[existingTypeLength] = type;
 								typeMap.put(typeName, existingTypes);
 							}
 						}
 					}
 				} catch (ModelException e) {
 					// working copy doesn't exist -> ignore
 				}
 
 				// add root of package fragment to cache
 				IProjectFragment root = (IProjectFragment) pkg.getParent();
 				String[] pkgName = pkg.path.segments();
 				Object existing = this.scriptFolders.get(pkgName);
 				if (existing == null) {
 					this.scriptFolders.put(pkgName, root);
 					// cache whether each package and its including packages
 					// (see
 					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=119161)
 					// are actual packages
 					ProjectElementInfo.addNames(pkgName, this.isPackageCache);
 				} else {
 					if (existing instanceof ProjectFragment) {
 						if (!existing.equals(root))
 							this.scriptFolders.put(pkgName,
 									new IProjectFragment[] {
 											(ProjectFragment) existing, root });
 					} else {
 						IProjectFragment[] roots = (IProjectFragment[]) existing;
 						int rootLength = roots.length;
 						boolean containsRoot = false;
 						for (int j = 0; j < rootLength; j++) {
 							if (roots[j].equals(root)) {
 								containsRoot = true;
 								break;
 							}
 						}
 						if (containsRoot) {
 							System.arraycopy(
 									roots,
 									0,
 									roots = new IProjectFragment[rootLength + 1],
 									0, rootLength);
 							roots[rootLength] = root;
 							this.scriptFolders.put(pkgName, roots);
 						}
 					}
 				}
 			}
 		}
 
 		this.rootToResolvedEntries = rootToResolvedEntries;
 		if (VERBOSE) {
 			Util.verbose(" -> spent: " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 
 	/**
 	 * Returns true if:
 	 * <ul>
 	 * <li>the given type is an existing class and the flag's
 	 * <code>ACCEPT_CLASSES</code> bit is on
 	 * <li>the given type is an existing interface and the
 	 * <code>ACCEPT_INTERFACES</code> bit is on
 	 * <li>neither the <code>ACCEPT_CLASSES</code> or
 	 * <code>ACCEPT_INTERFACES</code> bit is on
 	 * </ul>
 	 * Otherwise, false is returned.
 	 */
 	protected boolean acceptType(IType type, int acceptFlags,
 			boolean isSourceType) {
 		return true;
 	}
 
 	/**
 	 * Finds every type in the project whose simple name matches the prefix,
 	 * informing the requestor of each hit. The requestor is polled for
 	 * cancellation at regular intervals.
 	 * 
 	 * <p>
 	 * The <code>partialMatch</code> argument indicates partial matches should
 	 * be considered.
 	 */
 	private void findAllTypes(String prefix, boolean partialMatch,
 			int acceptFlags, IModelElementRequestor requestor) {
 		int count = this.projectFragments.length;
 		for (int i = 0; i < count; i++) {
 			if (requestor.isCanceled())
 				return;
 			IProjectFragment root = this.projectFragments[i];
 			IModelElement[] packages = null;
 			try {
 				packages = root.getChildren();
 			} catch (ModelException npe) {
 				continue; // the root is not present, continue;
 			}
 			if (packages != null) {
 				for (int j = 0, packageCount = packages.length; j < packageCount; j++) {
 					if (requestor.isCanceled())
 						return;
 					seekTypes(prefix, (IScriptFolder) packages[j],
 							partialMatch, acceptFlags, requestor);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Returns the <code>ISourceModule</code> which defines the type named
 	 * <code>qualifiedTypeName</code>, or <code>null</code> if none exists. The
 	 * domain of the search is bounded by the classpath of the
 	 * <code>IScriptProject</code> this <code>NameLookup</code> was obtained
 	 * from.
 	 * <p>
 	 * The name must be fully qualified (eg "java.lang.Object",
 	 * "java.util.Hashtable$Entry")
 	 */
 	public ISourceModule findSourceModule(String qualifiedTypeName) {
 		String[] pkgName = CharOperation.NO_STRINGS;
 		String cuName = qualifiedTypeName;
 
 		int index = qualifiedTypeName.lastIndexOf('.');
 		if (index != -1) {
 			pkgName = Util.splitOn('.', qualifiedTypeName, 0, index);
 			cuName = qualifiedTypeName.substring(index + 1);
 		}
 		index = cuName.indexOf('$');
 		if (index != -1) {
 			cuName = cuName.substring(0, index);
 		}
 		Object value = this.scriptFolders.get(pkgName);
 		if (value != null) {
 			if (value instanceof ProjectFragment) {
 				return findSourceModule(pkgName, cuName,
 						(ProjectFragment) value);
 			} else {
 				IProjectFragment[] roots = (IProjectFragment[]) value;
 				for (int i = 0; i < roots.length; i++) {
 					ProjectFragment root = (ProjectFragment) roots[i];
 					ISourceModule cu = findSourceModule(pkgName, cuName, root);
 					if (cu != null)
 						return cu;
 				}
 			}
 		}
 		return null;
 	}
 
 	private IPath toPath(String pkgName[]) {
 		IPath path = new Path(""); //$NON-NLS-1$
 		for (int i = 0; i < pkgName.length; ++i) {
 			path = path.append(pkgName[i]);
 		}
 		return path;
 	}
 
 	private ISourceModule findSourceModule(String[] pkgName, String cuName,
 			ProjectFragment root) {
 		// EBAY MOD START
 		IScriptFolder pkg = root.getScriptFolder(toPath(pkgName));
 		try {
 			ISourceModule[] cus = pkg.getSourceModules();
 			for (int j = 0, length = cus.length; j < length; j++) {
 				ISourceModule cu = cus[j];
 				if (Util.equalsIgnoreExtension(cu.getElementName(), cuName))
 					return cu;
 			}
 		} catch (ModelException e) {
 			// pkg does not exist
 			// -> try next package
 		}
 		// EBAY MOD END
 		return null;
 	}
 
 	/**
 	 * Returns the package fragment whose path matches the given (absolute)
 	 * path, or <code>null</code> if none exist. The domain of the search is
 	 * bounded by the buildpath of the <code>IScriptProject</code> this
 	 * <code>NameLookup</code> was obtained from. The path can be: - internal to
 	 * the workbench: "/Project/src" - external to the workbench:
 	 * "c:/jdk/classes.zip/java/lang"
 	 */
 	public IScriptFolder findScriptFolder(IPath path) {
 		if (!path.isAbsolute()) {
 			throw new IllegalArgumentException(Messages.path_mustBeAbsolute);
 		}
 		/*
 		 * TODO (jerome) this code should rather use the package fragment map to
 		 * find the candidate package, then check if the respective enclosing
 		 * root maps to the one on this given IPath.
 		 */
 		IResource possibleFragment = ResourcesPlugin.getWorkspace().getRoot()
 				.findMember(path);
 		if (possibleFragment == null) {
 			// external jar
 			for (int i = 0; i < this.projectFragments.length; i++) {
 				IProjectFragment root = this.projectFragments[i];
 				if (!root.isExternal()) {
 					continue;
 				}
 				IPath rootPath = root.getPath();
 				int matchingCount = rootPath.matchingFirstSegments(path);
 				if (matchingCount != 0) {
 					String name = path.toOSString();
 					// + 1 is for the File.separatorChar
 					name = name.substring(rootPath.toOSString().length() + 1,
 							name.length());
 					name = name.replace(File.separatorChar, '.');
 					IModelElement[] list = null;
 					try {
 						list = root.getChildren();
 					} catch (ModelException npe) {
 						continue; // the package fragment root is not present;
 					}
 					int elementCount = list.length;
 					for (int j = 0; j < elementCount; j++) {
 						IScriptFolder scriptFolder = (IScriptFolder) list[j];
 						if (nameMatches(name, scriptFolder, false)) {
 							return scriptFolder;
 						}
 					}
 				}
 			}
 		} else {
 			IModelElement fromFactory = DLTKCore.create(possibleFragment);
 			if (fromFactory == null) {
 				return null;
 			}
 			switch (fromFactory.getElementType()) {
 			case IModelElement.SCRIPT_FOLDER:
 				return (IScriptFolder) fromFactory;
 			case IModelElement.SCRIPT_PROJECT:
 				// default package in a default root
 				ScriptProject project = (ScriptProject) fromFactory;
 				try {
 					IBuildpathEntry entry = project.getBuildpathEntryFor(path);
 					if (entry != null) {
 						IProjectFragment root = project
 								.getProjectFragment(project.getResource());
 						Object defaultPkgRoot = this.scriptFolders
 								.get(CharOperation.NO_STRINGS);
 						if (defaultPkgRoot == null) {
 							return null;
 						}
 						if (defaultPkgRoot instanceof ProjectFragment
 								&& defaultPkgRoot.equals(root))
 							return ((ProjectFragment) root)
 									.getScriptFolder(Path.EMPTY);
 						else {
 							IProjectFragment[] roots = (IProjectFragment[]) defaultPkgRoot;
 							for (int i = 0; i < roots.length; i++) {
 								if (roots[i].equals(root)) {
 									return ((ProjectFragment) root)
 											.getScriptFolder(Path.EMPTY);
 								}
 							}
 						}
 					}
 				} catch (ModelException e) {
 					return null;
 				}
 				return null;
 			case IModelElement.PROJECT_FRAGMENT:
 				return ((ProjectFragment) fromFactory)
 						.getScriptFolder(Path.EMPTY);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the package fragments whose name matches the given (qualified)
 	 * name, or <code>null</code> if none exist.
 	 * 
 	 * The name can be: - empty: "" - qualified: "pack.pack1.pack2"
 	 * 
 	 * @param partialMatch
 	 *            partial name matches qualify when <code>true</code>, only
 	 *            exact name matches qualify when <code>false</code>
 	 */
 	public IScriptFolder[] findScriptFolders(String name, boolean partialMatch) {
 		if (partialMatch) {
 			String[] splittedName = Util.splitOn('.', name, 0, name.length());
 			IScriptFolder[] oneFragment = null;
 			ArrayList pkgs = null;
 			Object[][] keys = this.scriptFolders.keyTable;
 			for (int i = 0, length = keys.length; i < length; i++) {
 				String[] pkgName = (String[]) keys[i];
 				if (pkgName != null
 						&& Util.startsWithIgnoreCase(pkgName, splittedName)) {
 					Object value = this.scriptFolders.valueTable[i];
 					if (value instanceof ProjectFragment) {
 						IScriptFolder pkg = ((ProjectFragment) value)
 								.getScriptFolder(toPath(pkgName));
 						if (oneFragment == null) {
 							oneFragment = new IScriptFolder[] { pkg };
 						} else {
 							if (pkgs == null) {
 								pkgs = new ArrayList();
 								pkgs.add(oneFragment[0]);
 							}
 							pkgs.add(pkg);
 						}
 					} else {
 						IProjectFragment[] roots = (IProjectFragment[]) value;
 						for (int j = 0, length2 = roots.length; j < length2; j++) {
 							ProjectFragment root = (ProjectFragment) roots[j];
 							IScriptFolder pkg = root
 									.getScriptFolder(toPath(pkgName));
 							if (oneFragment == null) {
 								oneFragment = new IScriptFolder[] { pkg };
 							} else {
 								if (pkgs == null) {
 									pkgs = new ArrayList();
 									pkgs.add(oneFragment[0]);
 								}
 								pkgs.add(pkg);
 							}
 						}
 					}
 				}
 			}
 			if (pkgs == null)
 				return oneFragment;
 			int resultLength = pkgs.size();
 			IScriptFolder[] result = new IScriptFolder[resultLength];
 			pkgs.toArray(result);
 			return result;
 		} else {
 			String[] splittedName = Util.splitOn('.', name, 0, name.length());
 			Object value = this.scriptFolders.get(splittedName);
 			if (value == null)
 				return null;
 			if (value instanceof ProjectFragment) {
 				return new IScriptFolder[] { ((ProjectFragment) value)
 						.getScriptFolder(toPath(splittedName)) };
 			} else {
 				IProjectFragment[] roots = (IProjectFragment[]) value;
 				IScriptFolder[] result = new IScriptFolder[roots.length];
 				for (int i = 0; i < roots.length; i++) {
 					result[i] = ((ProjectFragment) roots[i])
 							.getScriptFolder(toPath(splittedName));
 				}
 				return result;
 			}
 		}
 	}
 
 	/**
 	 * Find type considering secondary types but without waiting for indexes. It
 	 * means that secondary types may be not found under certain
 	 * circumstances...
 	 * 
 	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=118789"
 	 */
 	public Answer findType(String typeName, String packageName,
 			boolean partialMatch, int acceptFlags, boolean checkRestrictions) {
 		return findType(typeName, packageName, partialMatch, acceptFlags,
 				true/* consider secondary types */, false/*
 														 * do NOT wait for
 														 * indexes
 														 */, checkRestrictions,
 				null);
 	}
 
 	/**
 	 * Find type. Considering secondary types and waiting for indexes depends on
 	 * given corresponding parameters.
 	 */
 	public Answer findType(String typeName, String packageName,
 			boolean partialMatch, int acceptFlags,
 			boolean considerSecondaryTypes, boolean waitForIndexes,
 			boolean checkRestrictions, IProgressMonitor monitor) {
 		if (packageName == null || packageName.length() == 0) {
 			packageName = IScriptFolder.DEFAULT_FOLDER_NAME;
 		} else if (typeName.length() > 0
 				&& Character.isLowerCase(typeName.charAt(0))) {
 			// see if this is a known package and not a type
 			if (findScriptFolders(packageName + "." + typeName, false) != null)return null; //$NON-NLS-1$
 		}
 
 		// Look for concerned package fragments
 		ModelElementRequestor elementRequestor = new ModelElementRequestor();
 		seekScriptFolders(packageName, false, elementRequestor);
 		IScriptFolder[] packages = elementRequestor.getScriptFolders();
 
 		// Try to find type in package fragments list
 		IType type = null;
 		int length = packages.length;
 		HashSet projects = null;
 		IScriptProject scriptProject = null;
 		Answer suggestedAnswer = null;
 		for (int i = 0; i < length; i++) {
 			type = findType(typeName, packages[i], partialMatch, acceptFlags);
 			if (type != null) {
 				AccessRestriction accessRestriction = null;
 				if (checkRestrictions) {
 					accessRestriction = getViolatedRestriction(typeName,
 							packageName, type, accessRestriction);
 				}
 				Answer answer = new Answer(type, accessRestriction);
 				if (!answer.ignoreIfBetter()) {
 					if (answer.isBetter(suggestedAnswer))
 						return answer;
 				} else if (answer.isBetter(suggestedAnswer))
 					// remember suggestion and keep looking
 					suggestedAnswer = answer;
 			} else if (suggestedAnswer == null && considerSecondaryTypes) {
 				if (scriptProject == null) {
 					scriptProject = packages[i].getScriptProject();
 				} else if (projects == null) {
 					if (!scriptProject.equals(packages[i].getScriptProject())) {
 						projects = new HashSet(3);
 						projects.add(scriptProject);
 						projects.add(packages[i].getScriptProject());
 					}
 				} else {
 					projects.add(packages[i].getScriptProject());
 				}
 			}
 		}
 		if (suggestedAnswer != null)
 			// no better answer was found
 			return suggestedAnswer;
 
 		return type == null ? null : new Answer(type, null);
 	}
 
 	private AccessRestriction getViolatedRestriction(String typeName,
 			String packageName, IType type, AccessRestriction accessRestriction) {
 		ProjectFragment root = (ProjectFragment) type
 				.getAncestor(IModelElement.PROJECT_FRAGMENT);
 		BuildpathEntry entry = (BuildpathEntry) this.rootToResolvedEntries
 				.get(root);
 		if (entry != null) { // reverse map always contains resolved CP entry
 			AccessRuleSet accessRuleSet = entry.getAccessRuleSet();
 			if (accessRuleSet != null) {
 				// TODO (philippe) improve char[] <-> String conversions to
 				// avoid performing them on the fly
 				char[][] packageChars = CharOperation.splitOn('.',
 						packageName.toCharArray());
 				char[] typeChars = typeName.toCharArray();
 				accessRestriction = accessRuleSet
 						.getViolatedRestriction(CharOperation.concatWith(
 								packageChars, typeChars, '/'));
 			}
 		}
 		return accessRestriction;
 	}
 
 	/**
 	 * Returns the first type in the given package whose name matches the given
 	 * (unqualified) name, or <code>null</code> if none exist. Specifying a
 	 * <code>null</code> package will result in no matches. The domain of the
 	 * search is bounded by the Script project from which this name lookup was
 	 * obtained.
 	 * 
 	 * @param name
 	 *            the name of the type to find
 	 * @param pkg
 	 *            the package to search
 	 * @param partialMatch
 	 *            partial name matches qualify when <code>true</code>, only
 	 *            exact name matches qualify when <code>false</code>
 	 * @param acceptFlags
 	 *            a bit mask describing if classes, interfaces or both classes
 	 *            and interfaces are desired results. If no flags are specified,
 	 *            all types are returned.
 	 * @param considerSecondaryTypes
 	 *            flag to know whether secondary types has to be considered
 	 *            during the search
 	 * 
 	 * @see #ACCEPT_CLASSES
 	 * @see #ACCEPT_INTERFACES
 	 * @see #ACCEPT_ENUMS
 	 * @see #ACCEPT_ANNOTATIONS
 	 */
 	public IType findType(String name, IScriptFolder pkg, boolean partialMatch,
 			int acceptFlags, boolean considerSecondaryTypes) {
 		IType type = findType(name, pkg, partialMatch, acceptFlags);
 		return type;
 	}
 
 	/**
 	 * Returns the first type in the given package whose name matches the given
 	 * (unqualified) name, or <code>null</code> if none exist. Specifying a
 	 * <code>null</code> package will result in no matches. The domain of the
 	 * search is bounded by the Script project from which this name lookup was
 	 * obtained. <br>
 	 * Note that this method does not find secondary types. <br>
 	 * 
 	 * @param name
 	 *            the name of the type to find
 	 * @param pkg
 	 *            the package to search
 	 * @param partialMatch
 	 *            partial name matches qualify when <code>true</code>, only
 	 *            exact name matches qualify when <code>false</code>
 	 * @param acceptFlags
 	 *            a bit mask describing if classes, interfaces or both classes
 	 *            and interfaces are desired results. If no flags are specified,
 	 *            all types are returned.
 	 * 
 	 * @see #ACCEPT_CLASSES
 	 * @see #ACCEPT_INTERFACES
 	 * @see #ACCEPT_ENUMS
 	 * @see #ACCEPT_ANNOTATIONS
 	 */
 	public IType findType(String name, IScriptFolder pkg, boolean partialMatch,
 			int acceptFlags) {
 		if (pkg == null)
 			return null;
 
 		// Return first found (ignore duplicates).
 		SingleTypeRequestor typeRequestor = new SingleTypeRequestor();
 		seekTypes(name, pkg, partialMatch, acceptFlags, typeRequestor);
 		return typeRequestor.getType();
 	}
 
 	/**
 	 * Returns the type specified by the qualified name, or <code>null</code> if
 	 * none exist. The domain of the search is bounded by the Script project
 	 * from which this name lookup was obtained.
 	 * 
 	 * @param name
 	 *            the name of the type to find
 	 * @param partialMatch
 	 *            partial name matches qualify when <code>true</code>, only
 	 *            exact name matches qualify when <code>false</code>
 	 * @param acceptFlags
 	 *            a bit mask describing if classes, interfaces or both classes
 	 *            and interfaces are desired results. If no flags are specified,
 	 *            all types are returned.
 	 * 
 	 * @see #ACCEPT_CLASSES
 	 * @see #ACCEPT_INTERFACES
 	 * @see #ACCEPT_ENUMS
 	 * @see #ACCEPT_ANNOTATIONS
 	 */
 	public IType findType(String name, boolean partialMatch, int acceptFlags) {
 		NameLookup.Answer answer = findType(name, partialMatch, acceptFlags,
 				false/* don't check restrictions */);
 		return answer == null ? null : answer.type;
 	}
 
 	public Answer findType(String name, boolean partialMatch, int acceptFlags,
 			boolean checkRestrictions) {
 		return findType(name, partialMatch, acceptFlags, true/*
 															 * consider
 															 * secondary types
 															 */, true/*
 																	 * wait for
 																	 * indexes
 																	 */,
 				checkRestrictions, null);
 	}
 
 	public Answer findType(String name, boolean partialMatch, int acceptFlags,
 			boolean considerSecondaryTypes, boolean waitForIndexes,
 			boolean checkRestrictions, IProgressMonitor monitor) {
 		int index = name.lastIndexOf('.');
 		String className = null, packageName = null;
 		if (index == -1) {
 			packageName = IScriptFolder.DEFAULT_FOLDER_NAME;
 			className = name;
 		} else {
 			packageName = name.substring(0, index);
 			className = name.substring(index + 1);
 		}
 		return findType(className, packageName, partialMatch, acceptFlags,
 				considerSecondaryTypes, waitForIndexes, checkRestrictions,
 				monitor);
 	}
 
 	private IType getMemberType(IType type, String name, int dot) {
 		while (dot != -1) {
 			int start = dot + 1;
 			dot = name.indexOf('.', start);
 			String typeName = name.substring(start, dot == -1 ? name.length()
 					: dot);
 			type = type.getType(typeName);
 		}
 		return type;
 	}
 
 	public boolean isPackage(String[] pkgName) {
 		return this.isPackageCache.get(pkgName) != null;
 	}
 
 	/**
 	 * Returns true if the given element's name matches the specified
 	 * <code>searchName</code>, otherwise false.
 	 * 
 	 * <p>
 	 * The <code>partialMatch</code> argument indicates partial matches should
 	 * be considered. NOTE: in partialMatch mode, the case will be ignored, and
 	 * the searchName must already have been lowercased.
 	 */
 	protected boolean nameMatches(String searchName, IModelElement element,
 			boolean partialMatch) {
 		if (partialMatch) {
 			// partial matches are used in completion mode, thus case
 			// insensitive mode
 			return element.getElementName().toLowerCase()
 					.startsWith(searchName);
 		} else {
 			return element.getElementName().equals(searchName);
 		}
 	}
 
 	/**
 	 * Returns true if the given cu's name matches the specified
 	 * <code>searchName</code>, otherwise false.
 	 * 
 	 * <p>
 	 * The <code>partialMatch</code> argument indicates partial matches should
 	 * be considered. NOTE: in partialMatch mode, the case will be ignored, and
 	 * the searchName must already have been lowercased.
 	 */
 	protected boolean nameMatches(String searchName, ISourceModule cu,
 			boolean partialMatch) {
 		if (partialMatch) {
 			// partial matches are used in completion mode, thus case
 			// insensitive mode
 			return cu.getElementName().toLowerCase().startsWith(searchName);
 		} else {
 			return Util.equalsIgnoreExtension(cu.getElementName(), searchName);
 		}
 	}
 
 	/**
 	 * Notifies the given requestor of all package fragments with the given
 	 * name. Checks the requestor at regular intervals to see if the requestor
 	 * has canceled. The domain of the search is bounded by the
 	 * <code>IScriptProject</code> this <code>NameLookup</code> was obtained
 	 * from.
 	 * 
 	 * @param partialMatch
 	 *            partial name matches qualify when <code>true</code>; only
 	 *            exact name matches qualify when <code>false</code>
 	 */
 	public void seekScriptFolders(String name, boolean partialMatch,
 			IModelElementRequestor requestor) {
 		/*
 		 * if (VERBOSE) { Util.verbose(" SEEKING PACKAGE FRAGMENTS");
 		 * //$NON-NLS-1$ Util.verbose(" -> name: " + name); //$NON-NLS-1$
 		 * Util.verbose(" -> partial match:" + partialMatch); //$NON-NLS-1$ }
 		 */if (partialMatch) {
 			String[] splittedName = Util.splitOn('.', name, 0, name.length());
 			Object[][] keys = this.scriptFolders.keyTable;
 			for (int i = 0, length = keys.length; i < length; i++) {
 				if (requestor.isCanceled())
 					return;
 				String[] pkgName = (String[]) keys[i];
 				if (pkgName != null
 						&& Util.startsWithIgnoreCase(pkgName, splittedName)) {
 					Object value = this.scriptFolders.valueTable[i];
 					if (value instanceof ProjectFragment) {
 						ProjectFragment root = (ProjectFragment) value;
 						requestor.acceptScriptFolder(root
 								.getScriptFolder(toPath(pkgName)));
 					} else {
 						IProjectFragment[] roots = (IProjectFragment[]) value;
 						for (int j = 0, length2 = roots.length; j < length2; j++) {
 							if (requestor.isCanceled())
 								return;
 							ProjectFragment root = (ProjectFragment) roots[j];
 							requestor.acceptScriptFolder(root
 									.getScriptFolder(toPath(pkgName)));
 						}
 					}
 				}
 			}
 		} else {
 			String[] splittedName = Util.splitOn('.', name, 0, name.length());
 			Object value = this.scriptFolders.get(splittedName);
 			if (value instanceof ProjectFragment) {
 				requestor.acceptScriptFolder(((ProjectFragment) value)
 						.getScriptFolder(toPath(splittedName)));
 			} else {
 				IProjectFragment[] roots = (IProjectFragment[]) value;
 				if (roots != null) {
 					for (int i = 0, length = roots.length; i < length; i++) {
 						if (requestor.isCanceled())
 							return;
 						ProjectFragment root = (ProjectFragment) roots[i];
 						requestor.acceptScriptFolder(root
 								.getScriptFolder(toPath(splittedName)));
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Notifies the given requestor of all types (classes and interfaces) in the
 	 * given package fragment with the given (unqualified) name. Checks the
 	 * requestor at regular intervals to see if the requestor has canceled. If
 	 * the given package fragment is <code>null</code>, all types in the project
 	 * whose simple name matches the given name are found.
 	 * 
 	 * @param name
 	 *            The name to search
 	 * @param pkg
 	 *            The corresponding package fragment
 	 * @param partialMatch
 	 *            partial name matches qualify when <code>true</code>; only
 	 *            exact name matches qualify when <code>false</code>
 	 * @param acceptFlags
 	 *            a bit mask describing if classes, interfaces or both classes
 	 *            and interfaces are desired results. If no flags are specified,
 	 *            all types are returned.
 	 * @param requestor
 	 *            The requestor that collects the result
 	 * 
 	 * @see #ACCEPT_CLASSES
 	 * @see #ACCEPT_INTERFACES
 	 * @see #ACCEPT_ENUMS
 	 * @see #ACCEPT_ANNOTATIONS
 	 */
 	public void seekTypes(String name, IScriptFolder pkg, boolean partialMatch,
 			int acceptFlags, IModelElementRequestor requestor) {
 		/*
 		 * if (VERBOSE) { Util.verbose(" SEEKING TYPES"); //$NON-NLS-1$
 		 * Util.verbose(" -> name: " + name); //$NON-NLS-1$
 		 * Util.verbose(" -> pkg: " + ((ModelElement)
 		 * pkg).toStringWithAncestors()); //$NON-NLS-1$
 		 * Util.verbose(" -> partial match:" + partialMatch); //$NON-NLS-1$ }
 		 */
 		String matchName = partialMatch ? name.toLowerCase() : name;
 		if (pkg == null) {
 			findAllTypes(matchName, partialMatch, acceptFlags, requestor);
 			return;
 		}
 		IProjectFragment root = (IProjectFragment) pkg.getParent();
 		try {
 
 			// look in working copies first
 			int firstDot = -1;
 			String topLevelTypeName = null;
 			int packageFlavor = root.getKind();
 			if (this.typesInWorkingCopies != null
 					|| packageFlavor == IProjectFragment.K_SOURCE) {
 				firstDot = matchName.indexOf('.');
 				if (!partialMatch)
 					topLevelTypeName = firstDot == -1 ? matchName : matchName
 							.substring(0, firstDot);
 			}
 			if (this.typesInWorkingCopies != null) {
 				if (seekTypesInWorkingCopies(matchName, pkg, firstDot,
 						partialMatch, topLevelTypeName, acceptFlags, requestor))
 					return;
 			}
 
 			// look in model
 			switch (packageFlavor) {
 			case IProjectFragment.K_SOURCE:
 				seekTypesInSourcePackage(matchName, pkg, firstDot,
 						partialMatch, topLevelTypeName, acceptFlags, requestor);
 				break;
 			default:
 				return;
 			}
 		} catch (ModelException e) {
 			return;
 		}
 	}
 
 	/**
 	 * Performs type search in a source package.
 	 */
 	protected void seekTypesInSourcePackage(String name, IScriptFolder pkg,
 			int firstDot, boolean partialMatch, String topLevelTypeName,
 			int acceptFlags, IModelElementRequestor requestor) {
 
 		long start = -1;
 		if (VERBOSE)
 			start = System.currentTimeMillis();
 		try {
 			if (!partialMatch) {
 				try {
 					IModelElement[] compilationUnits = pkg.getChildren();
 					for (int i = 0, length = compilationUnits.length; i < length; i++) {
 						if (requestor.isCanceled())
 							return;
 						IModelElement cu = compilationUnits[i];
 						String cuName = cu.getElementName();
 						int lastDot = cuName.lastIndexOf('.');
 						if (lastDot != topLevelTypeName.length()
 								|| !topLevelTypeName.regionMatches(0, cuName,
 										0, lastDot))
 							continue;
 						IType type = ((ISourceModule) cu)
 								.getType(topLevelTypeName);
 						type = getMemberType(type, name, firstDot);
 						if (acceptType(type, acceptFlags, true/* a source type */)) { // accept
 							// type
 							// checks
 							// for
 							// existence
 							requestor.acceptType(type);
 							break; // since an exact match was requested, no
 							// other matching type can exist
 						}
 					}
 				} catch (ModelException e) {
 					// package doesn't exist -> ignore
 				}
 			} else {
 				try {
 					String cuPrefix = firstDot == -1 ? name : name.substring(0,
 							firstDot);
 					IModelElement[] compilationUnits = pkg.getChildren();
 					for (int i = 0, length = compilationUnits.length; i < length; i++) {
 						if (requestor.isCanceled())
 							return;
 						IModelElement cu = compilationUnits[i];
 						if (!cu.getElementName().toLowerCase()
 								.startsWith(cuPrefix))
 							continue;
 						try {
 							IType[] types = ((ISourceModule) cu).getTypes();
 							for (int j = 0, typeLength = types.length; j < typeLength; j++)
 								seekTypesInTopLevelType(name, firstDot,
 										types[j], requestor, acceptFlags);
 						} catch (ModelException e) {
 							// cu doesn't exist -> ignore
 						}
 					}
 				} catch (ModelException e) {
 					// package doesn't exist -> ignore
 				}
 			}
 		} finally {
 			if (VERBOSE)
 				this.timeSpentInSeekTypesInSourcePackage += System
 						.currentTimeMillis() - start;
 		}
 	}
 
 	/**
 	 * Notifies the given requestor of all types (classes and interfaces) in the
 	 * given type with the given (possibly qualified) name. Checks the requestor
 	 * at regular intervals to see if the requestor has canceled.
 	 */
 	protected boolean seekTypesInType(String prefix, int firstDot, IType type,
 			IModelElementRequestor requestor, int acceptFlags) {
 		IType[] types = null;
 		try {
 			types = type.getTypes();
 		} catch (ModelException npe) {
 			return false; // the enclosing type is not present
 		}
 		int length = types.length;
 		if (length == 0)
 			return false;
 
 		String memberPrefix = prefix;
 		boolean isMemberTypePrefix = false;
 		if (firstDot != -1) {
 			memberPrefix = prefix.substring(0, firstDot);
 			isMemberTypePrefix = true;
 		}
 		for (int i = 0; i < length; i++) {
 			if (requestor.isCanceled())
 				return false;
 			IType memberType = types[i];
 			if (memberType.getElementName().toLowerCase()
 					.startsWith(memberPrefix))
 				if (isMemberTypePrefix) {
 					String subPrefix = prefix.substring(firstDot + 1,
 							prefix.length());
 					return seekTypesInType(subPrefix, subPrefix.indexOf('.'),
 							memberType, requestor, acceptFlags);
 				} else {
 					if (acceptType(memberType, acceptFlags, true/* a source type */)) {
 						requestor.acceptMemberType(memberType);
 						return true;
 					}
 				}
 		}
 		return false;
 	}
 
 	protected boolean seekTypesInTopLevelType(String prefix, int firstDot,
 			IType topLevelType, IModelElementRequestor requestor,
 			int acceptFlags) {
 		if (!topLevelType.getElementName().toLowerCase().startsWith(prefix))
 			return false;
 		if (firstDot == -1) {
 			if (acceptType(topLevelType, acceptFlags, true/* a source type */)) {
 				requestor.acceptType(topLevelType);
 				return true;
 			}
 		} else {
 			return seekTypesInType(prefix, firstDot, topLevelType, requestor,
 					acceptFlags);
 		}
 		return false;
 	}
 
 	/*
 	 * Seeks the type with the given name in the map of types with precedence
 	 * (coming from working copies) Return whether a type has been found.
 	 */
 	protected boolean seekTypesInWorkingCopies(String name, IScriptFolder pkg,
 			int firstDot, boolean partialMatch, String topLevelTypeName,
 			int acceptFlags, IModelElementRequestor requestor) {
 
 		if (!partialMatch) {
 			HashMap typeMap = (HashMap) (this.typesInWorkingCopies == null ? null
 					: this.typesInWorkingCopies.get(pkg));
 			if (typeMap != null) {
 				Object object = typeMap.get(topLevelTypeName);
 				if (object instanceof IType) {
 					IType type = getMemberType((IType) object, name, firstDot);
 					if (acceptType(type, acceptFlags, true/* a source type */)) {
 						requestor.acceptType(type);
 						return true; // don't continue with compilation unit
 					}
 				} else if (object instanceof IType[]) {
 					if (object == NO_TYPES)
 						return true; // all types where deleted -> type is
 					// hidden
 					IType[] topLevelTypes = (IType[]) object;
 					for (int i = 0, length = topLevelTypes.length; i < length; i++) {
 						if (requestor.isCanceled())
 							return false;
 						IType type = getMemberType(topLevelTypes[i], name,
 								firstDot);
 						if (acceptType(type, acceptFlags, true/* a source type */)) {
 							requestor.acceptType(type);
 							return true; // return the first one
 						}
 					}
 				}
 			}
 		} else {
 			HashMap typeMap = (HashMap) (this.typesInWorkingCopies == null ? null
 					: this.typesInWorkingCopies.get(pkg));
 			if (typeMap != null) {
 				Iterator iterator = typeMap.values().iterator();
 				while (iterator.hasNext()) {
 					if (requestor.isCanceled())
 						return false;
 					Object object = iterator.next();
 					if (object instanceof IType) {
 						seekTypesInTopLevelType(name, firstDot, (IType) object,
 								requestor, acceptFlags);
 					} else if (object instanceof IType[]) {
 						IType[] topLevelTypes = (IType[]) object;
 						for (int i = 0, length = topLevelTypes.length; i < length; i++)
 							seekTypesInTopLevelType(name, firstDot,
 									topLevelTypes[i], requestor, acceptFlags);
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 }
