 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.eclipse.codeassist;
 
 import java.net.URI;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.mod.ast.Modifiers;
 import org.eclipse.dltk.mod.compiler.env.ISourceModule;
 import org.eclipse.dltk.mod.core.CompletionProposal;
 import org.eclipse.dltk.mod.core.DLTKCore;
 import org.eclipse.dltk.mod.core.Flags;
 import org.eclipse.dltk.mod.core.IBuildpathEntry;
 import org.eclipse.dltk.mod.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.mod.core.IField;
 import org.eclipse.dltk.mod.core.IMember;
 import org.eclipse.dltk.mod.core.IMethod;
 import org.eclipse.dltk.mod.core.IModelElement;
 import org.eclipse.dltk.mod.core.IParent;
 import org.eclipse.dltk.mod.core.IScriptFolder;
 import org.eclipse.dltk.mod.core.IScriptProject;
 import org.eclipse.dltk.mod.core.IType;
 import org.eclipse.dltk.mod.core.ModelException;
 import org.eclipse.dltk.mod.internal.core.DefaultWorkingCopyOwner;
 import org.eclipse.dltk.mod.internal.core.ExternalFoldersManager;
 import org.eclipse.dltk.mod.internal.core.ExternalScriptFolder;
 import org.eclipse.dltk.mod.internal.core.ImportContainer;
 import org.eclipse.dltk.mod.internal.core.InternalDLTKLanguageManager;
 import org.eclipse.dltk.mod.internal.core.JSPackageDeclaration;
 import org.eclipse.dltk.mod.internal.core.JSSourceField;
 import org.eclipse.dltk.mod.internal.core.JSSourceFieldElementInfo;
 import org.eclipse.dltk.mod.internal.core.JSSourceMethod;
 import org.eclipse.dltk.mod.internal.core.JSSourceModule;
 import org.eclipse.dltk.mod.internal.core.JSSourceType;
 import org.eclipse.dltk.mod.internal.core.Model;
 import org.eclipse.dltk.mod.internal.core.ModelElement;
 import org.eclipse.dltk.mod.internal.core.ModelElementRequestor;
 import org.eclipse.dltk.mod.internal.core.ModelManager;
 import org.eclipse.dltk.mod.internal.core.NameLookup;
 import org.eclipse.dltk.mod.internal.core.NameLookup.Answer;
 import org.eclipse.dltk.mod.internal.core.NativeVjoSourceModule;
 import org.eclipse.dltk.mod.internal.core.OpenableElementInfo;
 import org.eclipse.dltk.mod.internal.core.ScriptFolder;
 import org.eclipse.dltk.mod.internal.core.ScriptProject;
 import org.eclipse.dltk.mod.internal.core.SourceModule;
 import org.eclipse.dltk.mod.internal.core.VjoLocalVariable;
 import org.eclipse.dltk.mod.internal.core.VjoSourceModule;
 import org.eclipse.dltk.mod.internal.core.VjoSourceType;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.util.JstBindingUtil;
 import org.eclipse.vjet.dsf.jst.BaseJstNode;
 import org.eclipse.vjet.dsf.jst.IJstMethod;
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstProperty;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.IJstTypeReference;
 import org.eclipse.vjet.dsf.jst.JstSource;
 import org.eclipse.vjet.dsf.jst.JstSource.IBinding;
 import org.eclipse.vjet.dsf.jst.SimpleBinding;
 import org.eclipse.vjet.dsf.jst.declaration.JstArg;
 import org.eclipse.vjet.dsf.jst.declaration.JstBlock;
 import org.eclipse.vjet.dsf.jst.declaration.JstConstructor;
 import org.eclipse.vjet.dsf.jst.declaration.JstMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstObjectLiteralType;
 import org.eclipse.vjet.dsf.jst.declaration.JstPackage;
 import org.eclipse.vjet.dsf.jst.declaration.JstProperty;
 import org.eclipse.vjet.dsf.jst.declaration.JstProxyMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstProxyType;
 import org.eclipse.vjet.dsf.jst.declaration.JstTypeRefType;
 import org.eclipse.vjet.dsf.jst.declaration.JstVar;
 import org.eclipse.vjet.dsf.jst.declaration.JstVars;
 import org.eclipse.vjet.dsf.jst.expr.AssignExpr;
 import org.eclipse.vjet.dsf.jst.expr.FieldAccessExpr;
 import org.eclipse.vjet.dsf.jst.expr.MtdInvocationExpr;
 import org.eclipse.vjet.dsf.jst.reserved.JsCoreKeywords;
 import org.eclipse.vjet.dsf.jst.stmt.RtnStmt;
 import org.eclipse.vjet.dsf.jst.term.JstIdentifier;
 import org.eclipse.vjet.dsf.jst.term.NV;
 import org.eclipse.vjet.dsf.jst.term.ObjLiteral;
 import org.eclipse.vjet.dsf.jst.token.IExpr;
 import org.eclipse.vjet.dsf.jst.ts.JstQueryExecutor;
 import org.eclipse.vjet.dsf.jst.ts.JstTypeSpaceMgr;
 import org.eclipse.vjet.dsf.ts.ITypeSpace;
 import org.eclipse.vjet.dsf.ts.TypeSpace;
 import org.eclipse.vjet.dsf.ts.group.IGroup;
 import org.eclipse.vjet.dsf.ts.type.TypeName;
 import org.eclipse.vjet.eclipse.codeassist.keywords.CompletionConstants;
 import org.eclipse.vjet.eclipse.codeassist.keywords.CompletionContext;
 import org.eclipse.vjet.eclipse.core.IJSMethod;
 import org.eclipse.vjet.eclipse.core.IJSType;
 import org.eclipse.vjet.eclipse.core.IVjoSourceModule;
 import org.eclipse.vjet.eclipse.core.VjoNature;
 import org.eclipse.vjet.eclipse.internal.codeassist.select.JstNodeDLTKElementResolver;
 import org.eclipse.vjet.vjo.lib.TsLibLoader;
 import org.eclipse.vjet.vjo.meta.VjoKeywords;
 import org.eclipse.vjet.vjo.tool.codecompletion.CodeCompletionUtils;
 import org.eclipse.vjet.vjo.tool.codecompletion.StringUtils;
 import org.eclipse.vjet.vjo.tool.typespace.SourceTypeName;
 import org.eclipse.vjet.vjo.tool.typespace.TypeSpaceMgr;
 
 /**
  * This class contains utilities methods for code completion and selection
  * functionality.
  * 
  * 
  * 
  */
 public class CodeassistUtils {
 	private static final String OBJECT_TYPE = "Object";
 
 	public static final String THIS_VJO_STATIC = CompletionConstants.THIS_VJO
 			+ CompletionConstants.DOT;
 
 	public static final String THIS_STATIC = CompletionConstants.THIS
 			+ CompletionConstants.DOT;
 
 	public static final String DOT = ".";
 
 	private static Map<String, IBuildpathEntry[]> buildPathMap = new HashMap<String, IBuildpathEntry[]>();
 
 	private static enum FieldSearchType {
 		INSTANCE, STATIC;
 
 		private int usegesCnt;
 	};
 
 	private static final String SUFFIX_ZIP = ".zip";
 
 	private static final String SUFFIX_JAR = ".jar";
 	private static final String SUFFIX_VJO = ".js";
 
 	private static Map<String, String> nativeTypeGroupMap = new HashMap<String, String>();
 
 	private static Map<String, NativeVjoSourceModule> nativeModuleMap = new HashMap<String, NativeVjoSourceModule>();
 
 	private static Map<String, ScriptFolder> defaultNativeScriptFolderMap = new HashMap<String, ScriptFolder>();
 
 	/**
 	 * Returns the type declared in this compilation unit.
 	 * 
 	 * @param SourceModule
 	 * @param name
 	 *            - type name
 	 * @return the type declared in this compilation unit
 	 */
 	public static IType getType(ISourceModule module, String name) {
 		org.eclipse.dltk.mod.core.ISourceModule sm = (org.eclipse.dltk.mod.core.ISourceModule) module;
 		try {
 			IType[] types = sm.getTypes();
 			if (types != null) {
 				for (IType type : types) {
 					if (name == null || type.getElementName().equals(name)) {
 						return type;
 					}
 				}
 			}
 		} catch (ModelException e) {
 			DLTKCore.error(e.toString(), e);
 		}
 
 		return null;
 	}
 
 	/**
 	 * Find the IType based on the jstType and IScriptProject The scene maybe:
 	 * 1. jstType is outer type 2. jstType is inner type, DLTK lookup code can
 	 * not take inner type correctly by type name.
 	 * 
 	 * @param scriptProject
 	 * @param jstType
 	 * @return
 	 */
 	public static IType findType(ScriptProject scriptProject, IJstType jstType) {
 		IJstType outerType = CodeCompletionUtils.getOuterJstType(jstType);
 		if (outerType == jstType) {
 			IType type = findType(scriptProject, jstType.getName());
 			if (type != null) {
 				return type;
 			} else {
 				// loop through alias names
 				String alias = jstType.getAlias();
 				return findType(scriptProject, alias);
 			}
 		} else {
 			String packageName = "";
 			if (outerType.getPackage() != null) {
 				packageName = outerType.getPackage().getName();
 			}
 			String typeName = jstType.getName();
 			if (typeName.contains(packageName)) {
 				if (StringUtils.isBlankOrEmpty(packageName)) {
 					packageName = "";
 				} else {
 					typeName = typeName.substring(packageName.length() + 1);
 				}
 				return findType(scriptProject, typeName, packageName);
 			} else {
 				return findType(scriptProject, jstType.getName());
 			}
 		}
 
 	}
 
 	// private static String getNameFromSource(JstSource source) {
 	// if(source!=null){
 	// IBinding fileBinding = source.getBinding();
 	// if(fileBinding instanceof FileBinding){
 	// File file = ((FileBinding) fileBinding).getFile();
 	// return file.getPath().replace("/", ".");
 	// }
 	// }
 	// return null;
 	// }
 
 	/**
 	 * Find IType from DLTK based on the package name and simple type name
 	 * 
 	 * @param scriptProject
 	 * @param typeName
 	 * @param packageName
 	 * @return
 	 */
 	public static IType findType(ScriptProject scriptProject, String typeName,
 			String packageName) {
 		if (scriptProject == null || typeName == null || packageName == null)
 			return null;
 
 		IType type = null;
 
 		// creation lookup
 		NameLookup lookup = null;
 		try {
 			lookup = scriptProject
 					.newNameLookup(DefaultWorkingCopyOwner.PRIMARY);
 		} catch (ModelException e) {
 			return null;
 			// DLTKCore.error(e.toString(), e);
 		}
 
 		// find type in current project
 		if (lookup != null) {
 			Answer answer = lookup.findType(typeName, packageName, false,
 					NameLookup.ACCEPT_ALL, true/* consider secondary types */,
 					true/* wait for indexes */, false, null);
 			if (answer != null) {
 				type = answer.type;
 			}
 		}
 
 		// find type in depends project
 		if (type == null) {
 			try {
 				type = findInDependsProjects(typeName, packageName,
 						scriptProject);
 			} catch (ModelException e) {
 				DLTKCore.error(e.toString(), e);
 			}
 		}
 
 		return type;
 	}
 
 	/**
 	 * Finds type within specified project by full type name.
 	 * 
 	 * @param scriptProject
 	 *            project to be investigated.
 	 * @param name
 	 *            type's name
 	 * @return requested type.
 	 */
 	public static IType findType(ScriptProject scriptProject, String name) {
 		// add by kevin to fix NPE
 		if (scriptProject == null || name == null)
 			return null;
 
 		IType type = null;
 
 		// creation lookup
 		if (scriptProject == null || !scriptProject.exists()) {
 			return null;
 		}
 		try {
 			NameLookup lookup = scriptProject
 					.newNameLookup(DefaultWorkingCopyOwner.PRIMARY);
 			if (lookup != null) {
 				type = lookup.findType(name, false, NameLookup.ACCEPT_ALL);
 			}
 		} catch (ModelException e) {
 			e.printStackTrace();
 		}
 
 		// find type in depends project
 		if (type == null) {
 			try {
 				type = findInDependsProjects(name, scriptProject);
 			} catch (ModelException e) {
 				DLTKCore.error(e.toString(), e);
 			}
 		}
 
 		return type;
 	}
 
 	/**
 	 * Find types within specified project of the {@link SourceModule} object by
 	 * specified word.
 	 * 
 	 * @param sourceModule
 	 *            {@link SourceModule} object.
 	 * @param word
 	 *            word which use for find types
 	 * @param requestor
 	 *            {@link ModelElementRequestor} object collect found types.
 	 */
 	public static void findTypes(SourceModule sourceModule, String word,
 			ModelElementRequestor requestor) {
 		ScriptProject project = (ScriptProject) sourceModule.getModelElement()
 				.getAncestor(IModelElement.SCRIPT_PROJECT);
 
 		// create lookup object
 		NameLookup lookup = null;
 		try {
 			lookup = project.newNameLookup(DefaultWorkingCopyOwner.PRIMARY);
 		} catch (ModelException e) {
 			DLTKCore.error(e.toString(), e);
 		}
 
 		// seek types and put their to requestor
 		lookup.seekTypes(word, (IScriptFolder) sourceModule.getScriptFolder(),
 				true, NameLookup.ACCEPT_ALL, requestor);
 
 		findNativeTypes(sourceModule, word, requestor);
 	}
 
 	/**
 	 * Find native types within {@link JstTypeSpaceMgr#JS_NATIVE_GRP} native
 	 * javascript group by specified word.
 	 * 
 	 * @param sourceModule
 	 *            {@link SourceModule} object.
 	 * @param word
 	 *            word which use for find types
 	 * @param requestor
 	 *            {@link ModelElementRequestor} object collect found types.
 	 */
 	private static void findNativeTypes(SourceModule sourceModule, String word,
 			ModelElementRequestor requestor) {
 
 		// start ebay code
 		TypeName global = new TypeName(JstTypeSpaceMgr.JS_NATIVE_GRP, "Global");
 		IJstType jsttype = TypeSpaceMgr.getInstance().getController()
 				.getJstTypeSpaceMgr().getQueryExecutor().findType(global);
 		Iterator<? extends IJstMethod> iter = jsttype.getMethods().iterator();
 		while (iter.hasNext()) {
 			IJstMethod m = iter.next();
 			if (m.getName().getName().startsWith(word)) {
 				IType type = createNativeType(sourceModule);
 				requestor.acceptType(type);
 				return;
 			}
 		}
 		Iterator<? extends IJstProperty> iter2 = jsttype.getProperties()
 				.iterator();
 		while (iter2.hasNext()) {
 			IJstProperty m = iter2.next();
 			if (m.getName().getName().startsWith(word)) {
 				IType type = createNativeType(sourceModule);
 				requestor.acceptType(type);
 				return;
 			}
 		}
 		// end ebay code
 
 		// search types in native group by word
 		IGroup<IJstType> nativeTypes = TypeSpaceMgr.getInstance()
 				.getController().getJstTypeSpaceMgr().getTypeSpace()
 				.getGroup(JstTypeSpaceMgr.JS_NATIVE_GRP);
 		for (IJstType jsttype2 : nativeTypes.getEntities().values()) {
 			String typeName = jsttype2.getName();
 			if (typeName.startsWith(word)) {
 				IType type = findNativeSourceType(sourceModule, typeName);
 				requestor.acceptType(type);
 			}
 		}
 
 	}
 
 	/**
 	 * Find type within specified project of the {@link SourceModule} object and
 	 * depends projects by specified name. Jack: if the name (packagename +
 	 * typeName) is not right, it will return null, because so when calling
 	 * this, should check if the result is null.
 	 * 
 	 * 
 	 * @param sourceModule
 	 *            {@link SourceModule} object
 	 * @param name
 	 *            name of the resource.
 	 * @return {@link IType} object.
 	 */
 	public static IType findResourceType(ISourceModule sourceModule, String name) {
 		IType type = null;
 		IScriptProject project = (IScriptProject) sourceModule
 				.getModelElement().getAncestor(IModelElement.SCRIPT_PROJECT);
 
 		// add by kevin, fix NPE
 		if (project.getProject() == null)
 			return null;
 
 		try {
 			NameLookup lookup = ((ScriptProject) project)
 					.newNameLookup(DefaultWorkingCopyOwner.PRIMARY);
 			if (name.indexOf('.') > 0) {
 				// full class name specified
 				type = lookup.findType(name, false, NameLookup.ACCEPT_ALL);
 			} else {
 				// only short type name was specified - need to find full class
 				// name
 				if (sourceModule.getModelElement() instanceof SourceModule) {
 					IModelElement[] children = ((SourceModule) sourceModule)
 							.getChildren();
 					ImportContainer importContainer = null;
 					for (int j = 0; j < children.length; j++) {
 						if (children[j] instanceof ImportContainer) {
 							importContainer = (ImportContainer) children[j];
 							break;
 						}
 					}
 
 					// find resources use import information
 					if (importContainer != null) {
 						IModelElement[] importDeclaration = importContainer
 								.getChildren();
 						for (int j = 0; j < importDeclaration.length; j++) {
 							if (importDeclaration[j].getElementName().contains(
 									".*")) {
 								String imp = importDeclaration[j]
 										.getElementName().replace(".*", "");
 								type = lookup.findType(
 										imp.concat(".").concat(name), false,
 										NameLookup.ACCEPT_ALL);
 								if (type != null) {
 									return type;
 								}
 							} else {
 								if (importDeclaration[j].getElementName()
 										.endsWith("." + name)) {
 									return lookup.findType(importDeclaration[j]
 											.getElementName(), false,
 											NameLookup.ACCEPT_ALL);
 								}
 							}
 						}
 					}
 					// is in same package
 					for (int j = 0; j < children.length; j++) {
 						if (children[j] instanceof JSPackageDeclaration) {
 							type = lookup.findType(
 									((JSPackageDeclaration) children[j])
 											.getElementName().concat(".")
 											.concat(name), false,
 									NameLookup.ACCEPT_ALL);
 							break;
 						}
 					}
 
 				}
 			}
 
 			// find types in depends projects.
 			if (type == null) {
 				type = findInDependsProjects(name, project);
 			}
 		} catch (ModelException e) {
 			DLTKCore.error(e.toString(), e);
 		}
 
 		return type;
 	}
 
 	private static IType findInDependsProjects(String typeName,
 			String packageName, IScriptProject project) throws ModelException {
 
 		NameLookup lookup;
 		IType type = null;
 
 		ModelManager manager = ModelManager.getModelManager();
 		Model model = manager.getModel();
 		IScriptProject[] prjs = model.getScriptProjects(VjoNature.NATURE_ID);
 
 		for (IScriptProject prj : prjs) {
 
 			type = findInDependsProjects(typeName, packageName, project, prj);
 
 			if (type != null) {
 				break;
 			}
 		}
 		return type;
 
 	}
 
 	/**
 	 * Find types in depends projects
 	 * 
 	 * @param name
 	 *            name of the type
 	 * @param project
 	 *            {@link IScriptProject} object
 	 * @return {@link IType} object
 	 * @throws ModelException
 	 */
 	private static IType findInDependsProjects(String name,
 			IScriptProject project) throws ModelException {
 
 		NameLookup lookup;
 		IType type = null;
 
 		ModelManager manager = ModelManager.getModelManager();
 		Model model = manager.getModel();
 		IScriptProject[] prjs = model.getScriptProjects(VjoNature.NATURE_ID);
 
 		for (IScriptProject prj : prjs) {
 
 			type = findInDependsProjects(name, project, prj);
 
 			if (type != null) {
 				break;
 			}
 		}
 		return type;
 	}
 
 	private static IType findInDependsProjects(String typeName,
 			String packageName, IScriptProject project,
 			IScriptProject dependentProject) throws ModelException {
 
 		IType type = null;
 		NameLookup lookup;
 
 		IBuildpathEntry[] entries = dependentProject.getResolvedBuildpath(true);
 
 		for (IBuildpathEntry entry : entries) {
 
 			if (entry.getEntryKind() == IBuildpathEntry.BPE_PROJECT) {
 
 				String prjName = entry.getPath().segment(0);
 
 				// find types if dependent project contains in required projects
 				// main project.
 				if (project.getElementName().equals(prjName)) {
 
 					lookup = ((ScriptProject) dependentProject)
 							.newNameLookup(DefaultWorkingCopyOwner.PRIMARY);
 
 					Answer answer = lookup.findType(typeName, packageName,
 							false, NameLookup.ACCEPT_ALL, true/*
 															 * consider
 															 * secondary types
 															 */, true/*
 																	 * wait for
 																	 * indexes
 																	 */, false,
 							null);
 					if (answer != null) {
 						type = answer.type;
 					}
 
 					if (type != null) {
 						break;
 					}
 				}
 			}
 		}
 
 		return type;
 
 	}
 
 	/**
 	 * At first get the resolved buildpath for the main project. Looks up for
 	 * the type for each of buildpath entries.
 	 * 
 	 * @param name
 	 *            requested type's name
 	 * @param dependentProject
 	 *            dependent project to be investigated.
 	 * @param project
 	 *            main project from which perform searching
 	 * @return type
 	 * @throws ModelException
 	 *             if an error occurred when searching type.
 	 */
 	private static IType findInDependsProjects(String name,
 			IScriptProject project, IScriptProject dependentProject)
 			throws ModelException {
 
 		IType type = null;
 		NameLookup lookup;
 
 		IBuildpathEntry[] entries = dependentProject.getResolvedBuildpath(true);
 
 		for (IBuildpathEntry entry : entries) {
 
 			if (entry.getEntryKind() == IBuildpathEntry.BPE_PROJECT) {
 
 				String prjName = entry.getPath().segment(0);
 
 				// find types if dependent project contains in required projects
 				// main project.
 				if (project.getElementName().equals(prjName)) {
 
 					lookup = ((ScriptProject) dependentProject)
 							.newNameLookup(DefaultWorkingCopyOwner.PRIMARY);
 
 					type = lookup.findType(name, false, NameLookup.ACCEPT_ALL);
 
 					if (type != null) {
 						break;
 					}
 				}
 			}
 		}
 
 		return type;
 	}
 
 	/**
 	 * Finds local element within source module
 	 * 
 	 * @param module
 	 *            source module to be investigated.
 	 * @param pos
 	 *            start position to search.
 	 * @return local element
 	 */
 	public static IModelElement findLocalElement(
 			org.eclipse.dltk.mod.core.ISourceModule module, int pos) {
 		IModelElement res = null;
 		try {
 			res = module.getElementAt(pos);
 		} catch (ModelException e) {
 			DLTKCore.error(e.toString(), e);
 		}
 		return res;
 	}
 
 	/**
 	 * Gets all script folders within source module whose name matches the given
 	 * name.
 	 * 
 	 * @param sourceModule
 	 *            source module to be investigated.
 	 * @param name
 	 *            name to compare.
 	 * @return script folders
 	 */
 	public static IScriptFolder[] findScriptFolder(ISourceModule sourceModule,
 			String name) {
 		IScriptProject project = (IScriptProject) sourceModule
 				.getModelElement().getAncestor(IModelElement.SCRIPT_PROJECT);
 		try {
 			NameLookup lookup = ((ScriptProject) project)
 					.newNameLookup(DefaultWorkingCopyOwner.PRIMARY);
 			return lookup.findScriptFolders(name, false);
 		} catch (Exception e) {
 			DLTKCore.error(e.toString(), e);
 			return null;
 		}
 	}
 
 	/**
 	 * Gets script folder by specified name.
 	 * 
 	 * @param project
 	 *            project to be investigated.
 	 * @param folderName
 	 *            folder name
 	 * @return script folder or null if doesn't exist.
 	 * @throws ModelException
 	 */
 	public static IScriptFolder getScriptFolder(IScriptProject project,
 			String folderName) throws ModelException {
 		IScriptFolder[] folders = project.getScriptFolders();
 
 		for (IScriptFolder folder : folders) {
 			if (folder.getElementName().equals(folderName))
 				return folder;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Gets field by name.
 	 * 
 	 * @param sourceModule
 	 *            source module to be investigated.
 	 * @param fieldName
 	 *            field's name
 	 * @param fieldSearch
 	 *            contains search scope
 	 * @return requested field or null
 	 */
 	public static IField getField(ISourceModule sourceModule, String fieldName,
 			FieldSearchType fieldSearch) {
 		IField result = null;
 		try {
 			IType type = getType(sourceModule, null);
 			if (type != null) {
 				IField[] fields = type.getFields();
 				for (IField field : fields) {
 					if (field.getElementName().equals(fieldName)) {
 						// TODO consider if we need this check - return only
 						// static fields when fieldSearch is set to STATIC and
 						// return instance field if fieldSearch is not static
 						if (!(fieldSearch != null && fieldSearch == FieldSearchType.STATIC
 								^ Flags.isStatic(field.getFlags()))) {
 							return field;
 						}
 					}
 				}
 				// search in super types
 				String[] superClassNames = type.getSuperClasses();
 				if (superClassNames != null && superClassNames.length > 0) {
 					IType superType = findResourceType(sourceModule,
 							superClassNames[0]);
 					if (superType != null) {
 						result = getField((SourceModule) superType.getParent(),
 								fieldName, fieldSearch);
 					}
 				}
 			}
 		} catch (ModelException e) {
 			DLTKCore.error(e.toString(), e);
 		}
 
 		return result;
 	}
 
 	/**
 	 * Populate list of the {@link IExpr} object from the qualifier object.
 	 * 
 	 * @param qualifier
 	 *            qualifier object
 	 * @param list
 	 *            list og the {@link IExpr} objects.
 	 */
 	public static void getQualifiersAsList(Object qualifier, List<IExpr> list) {
 		if (qualifier == null) {
 			return;
 		}
 		IExpr expression = null;
 		if (qualifier instanceof FieldAccessExpr) {
 			expression = ((FieldAccessExpr) qualifier).getExpr();
 		} else if (qualifier instanceof MtdInvocationExpr) {
 			expression = ((MtdInvocationExpr) qualifier).getQualifyExpr();
 		} else if (qualifier instanceof JstIdentifier) {
 			expression = ((JstIdentifier) qualifier).getQualifier();
 		}
 		if (expression != null) {
 			list.add(expression);
 			getQualifiersAsList(expression, list);
 		}
 		return;
 	}
 
 	/**
 	 * Returns {@link ISourceModule} of the {@link IModelElement} object in
 	 * current {@link SourceModule} module. For {@link JSSourceField} returns
 	 * type of the field , for {@link JSSourceMethod} returns return type of the
 	 * method, for {@link JSSourceType} return parent module.
 	 * 
 	 * @param element
 	 *            {@link IModelElement} object.
 	 * @param module
 	 *            {@link ISourceModule} current source module object.
 	 * @return {@link ISourceModule} object
 	 */
 	public static ISourceModule getElementSourceModule(IModelElement element,
 			ISourceModule module) {
 		ISourceModule sourceModule = null;
 
 		String typeName = null;
 		try {
 			// get parent of the model elements
 			if (element instanceof JSSourceField) {
 				JSSourceFieldElementInfo info = (JSSourceFieldElementInfo) ((JSSourceField) element)
 						.getElementInfo();
 				typeName = info.getType();
 			} else if (element instanceof JSSourceMethod) {
 				typeName = ((JSSourceMethod) element).getReturnType();
 			} else if (element instanceof JSSourceType) {
 				return (ISourceModule) element.getParent();
 			}
 
 			IType t = null;
 
 			// check on is not local variable
 			if (typeName.equals(element.getParent().getElementName())) {
 				t = ((JSSourceModule) module).getType(typeName);
 			} else {
 				t = findResourceType(module, typeName);
 			}
 
 			if (t != null) {
 				sourceModule = (ISourceModule) t.getParent();
 			}
 		} catch (ModelException e) {
 			DLTKCore.error(e.toString(), e);
 		}
 
 		return sourceModule;
 	}
 
 	/**
 	 * Returns {@link IMethod} object within {@link ISourceModule} module by
 	 * specified {@link JstMethod#getName()} information.
 	 * 
 	 * @param module
 	 *            {@link ISourceModule} current source module.
 	 * @param selection
 	 *            {@link JstMethod} object.
 	 * @return {@link IMethod} object.
 	 */
 	public static IModelElement[] getMethod(ISourceModule module,
 			JstMethod selection) {
 		try {
 			IType type = getType(module, null);
 			if (type != null) {
 				return getMethod(type, selection);
 			}
 		} catch (ModelException e) {
 			DLTKCore.error(e.toString(), e);
 		}
 
 		return null;
 	}
 
 	/**
 	 * find corresponding jstMethod by dltkMethod in JstType
 	 * 
 	 * @param jstType
 	 * @param dltkMethod
 	 * @return
 	 */
 	public static IJstMethod getJstMethod(IJstType jstType, IMethod dltkMethod) {
 		if (jstType != null && dltkMethod != null) {
 			List<? extends IJstMethod> jstMethods = jstType.getMethods();
 			for (IJstMethod jstMethod : jstMethods) {
 				if (jstMethod.getName().getName()
 						.equals(dltkMethod.getElementName())) {
 					// method name is OK, let's check return type
 					// IJSMethod jsMethod = (IJSMethod) method;
 					// continue with params
 					String[] paramsTypes = ((IJSMethod) dltkMethod)
 							.getParameterTypes();
 					List<JstArg> args = jstMethod.getArgs();
 					if (paramsTypes.length == args.size()) {
 						boolean paramsOK = true;
 						for (int i = 0; i < paramsTypes.length; i++) {
 							JstArg arg = args.get(i);
 							IJstType argType = arg.getType();
 							String argTypeName;
 							if (argType != null) {
 								argTypeName = argType.getName();
 							} else {
 								argTypeName = OBJECT_TYPE;
 							}
 							if (!paramsTypes[i].equals(argTypeName)) {
 								paramsOK = false;
 								break;
 							}
 						}
 						if (paramsOK) {
 							return jstMethod;
 						}
 					}
 				}
 
 			}
 		}
 		return null;
 	}
 
 	public static IMethod[] findMethodBySignature2(String methodName,
 			List<String> args, IType declareType) throws ModelException {
 		List<IMethod> mtds = new ArrayList<IMethod>();
 		IMethod[] methods = declareType.getMethods();
 		for (IMethod method : methods) {
 			if (method.getElementName().equals(methodName)) {
 				mtds.add(method);
 			}
 		}
 		return mtds.toArray(new IMethod[] {});
 	}
 
 	public static IMethod findMethodBySignature(String methodName,
 			List<String> args, IType declareType) throws ModelException {
 		IMethod[] methods = declareType.getMethods();
 		for (IMethod method : methods) {
 			if (method.getElementName().equals(methodName)) {
 				// method name is OK, let's check return type
 				IJSMethod jsMethod = (IJSMethod) method;
 				// shortcuted by huzhou@ebay.com as the dispatcher method
 				// doesn't have to match the parameter list of ast structure
 				// if the binding was correct, then the method should be F2/F3
 				// enabled despite the parameter matching
 				return jsMethod;
 				// // continue with params
 				// String[] paramsTypes = jsMethod.getParameterTypes();
 				// // List<JstArg> args = jstMethod.getArgs();
 				// if (paramsTypes.length == args.size()) {
 				// boolean paramsOK = true;
 				// for (int i = 0; i < paramsTypes.length; i++) {
 				//
 				// String argTypeName = args.get(i);
 				// if (!paramsTypes[i].equals(argTypeName)) {
 				// paramsOK = false;
 				// break;
 				// }
 				// }
 				// if (paramsOK) {
 				// return method;
 				// }
 				// }
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * find method by exact match
 	 * 
 	 * @param type
 	 * @param jstMethod
 	 * @return
 	 * @throws ModelException
 	 */
 	public static IMethod[] getMethod(IType type, IJstMethod jstMethod)
 			throws ModelException {
 		Assert.isNotNull(type);
 		Assert.isNotNull(jstMethod);
 
 		String selectedMethodName = jstMethod.getName().getName();
 		// if (VjoKeywords.CONSTRUCTS.equals(selectedMethodName)) {
 		// selectedMethodName = jstMethod.getOwnerType()
 		// .getSimpleName();
 		// }
 		List<JstArg> args = jstMethod.getArgs();
 		List<String> paramList = new ArrayList<String>();
 		for (JstArg jstArg : args) {
 			// JstArg arg = (JstArg) args.get(i);
 			IJstType argType = jstArg.getType();
 			String argTypeName;
 			if (argType != null) {
 				argTypeName = argType.getName();
 			} else {
 				argTypeName = OBJECT_TYPE;
 			}
 
 			paramList.add(argTypeName);
 		}
 		return findMethodBySignature2(selectedMethodName, paramList, type);
 	}
 
 	/**
 	 * Finds local variable.
 	 * 
 	 * @param module
 	 *            source module
 	 * @param name
 	 *            variable name
 	 * @param type
 	 *            container type
 	 * @param source
 	 *            jst source module
 	 * @return local variable if found otherwise null
 	 */
 	public static IModelElement[] getLocalVar(ISourceModule module,
 			String name, String type, JstSource source) {
 
 		// find element by offset
 		IModelElement element = findLocalElement(
 				(org.eclipse.dltk.mod.core.ISourceModule) module,
 				source.getStartOffSet());
 
 		// get method of the element if element is field
 		if (element != null && element.getElementType() == IModelElement.FIELD) {
 			element = element.getParent();
 		}
 
 		// search local variable in the method children list by name
 		IModelElement localVar = null;
 		if (element != null) {
 			IModelElement child = findChild(name, element);
 			if (child != null) {
 				return new IModelElement[] { child };
 
 			}
 			// special case for this
 			if (JsCoreKeywords.THIS.equals(name)) {
 				IType currentType = (IType) element
 						.getAncestor(IModelElement.TYPE);
 				type = currentType.getFullyQualifiedName().replace('/', '.');
 			} else if (OBJECT_TYPE.equals(type)) { // Jack_Code, here, if it is
 				// not defined as local var,
 				// and if it is an
 				// NativeType, return it
 				// directly
 				IJstType jstType = findNativeJstType(name);
 				if (jstType != null) {
 					IVjoSourceModule sourceModule = (IVjoSourceModule) module;
 					ScriptFolder folder = (ScriptFolder) sourceModule
 							.getParent();
 					NativeVjoSourceModule m = createNativeModule(folder,
 							jstType.getName());
 					return m != null ? new IModelElement[] { m }
 							: new IModelElement[0];
 
 					// type = jstType.getName();
 				}
 			}
 			localVar = new VjoLocalVariable((ModelElement) element, name,
 					source.getStartOffSet(), source.getEndOffSet(),
 					source.getStartOffSet(), source.getEndOffSet(), type);
 		}
 
 		return localVar != null ? new IModelElement[] { localVar }
 				: new IModelElement[0];
 	}
 
 	public static IModelElement findChild(String name, IModelElement parent) {
 		IModelElement element = null;
 		try {
 			IModelElement[] children = ((ModelElement) parent).getChildren();
 			if (children != null && children.length > 0) {
 				for (IModelElement child : children) {
 					if (child.getElementType() == IModelElement.FIELD
 							&& child.getElementName().equals(name)) {
 						element = child;
 						break;
 					}
 				}
 			}
 		} catch (ModelException e) {
 			DLTKCore.error(e.toString(), e);
 		}
 		return element;
 	}
 
 	/**
 	 * Returns list of the {@link IModelElement} object using specified
 	 * {@link SourceModule} object and expression object. Create
 	 * {@link IModelElement} from the {@link IJstNode} object and add to result
 	 * list.
 	 * 
 	 * 
 	 * @param expression
 	 *            expression object
 	 * @param module
 	 *            {@link ISourceModule} object.
 	 * @return list of the {@link IModelElement} object
 	 */
 	public static List<IModelElement> resolveQualifiers(Object expression,
 			ISourceModule module) {
 		List<IModelElement> modelElement = new ArrayList<IModelElement>();
 		if (expression != null) {
 			if (expression instanceof IJstType) {
 				// type declaration
 				IJstType jstType = (IJstType) expression;
 				modelElement.add(getType(module, jstType.getSimpleName()));
 			} else if (expression instanceof IJstTypeReference) {
 				// type reference
 				IJstType jstType = ((IJstTypeReference) expression)
 						.getReferencedType();
 				if (isEmpty(jstType)) {
 					return Collections.emptyList();
 				}
 				if (isNativeType(jstType) || isBinaryType(jstType)) {
 					modelElement.add(findNativeSourceType(jstType));
 				} else {
 					modelElement
 							.add(findResourceType(module, jstType.getName()));
 				}
 
 			} else if (expression instanceof JstIdentifier) {
 				// local var reference
 				JstIdentifier identifier = (JstIdentifier) expression;
 
 				// modelElement = getNativeElement(module, expression);
 
 				// local var reference
 				if (modelElement == null) {
 					modelElement.addAll(Arrays.asList(getLocalVar(module,
 							identifier.getName(), OBJECT_TYPE,
 							identifier.getSource())));
 				}
 				if (modelElement == null) {
 					// TODO If the selected type is 'vjo', need to get more
 					// information for it. Maybe need to generate the temporary
 					// file.
 					String nativeTypeName = "vjo";
 					IJstType jstType = TypeSpaceMgr
 							.getInstance()
 							.getController()
 							.getJstTypeSpaceMgr()
 							.getQueryExecutor()
 							.findType(
 									new TypeName("VjoBaseLib", nativeTypeName));
 					// modelElement = findNativeElement(module, nativeTypeName);
 				}
 
 			} else if (expression instanceof JstProperty) {
 				// field declaration
 				modelElement.add(getField(module, ((JstProperty) expression)
 						.getName().getName(), null));
 			} else if (expression instanceof JstVar) {
 				// TODO implement selection for local variable declaration here
 				JstVar jstVar = (JstVar) expression;
 				IJstType type = jstVar.getType();
 				String typeName = OBJECT_TYPE;
 				if (type != null) {
 					typeName = type.getName();
 				}
 				modelElement.addAll(Arrays.asList(getLocalVar(module,
 						jstVar.getName(), typeName, jstVar.getSource())));
 			} else if (expression instanceof JstMethod) {
 				// method declaration - no need for search using name lookup
 				modelElement.addAll(Arrays.asList(getMethod(module,
 						(JstMethod) expression)));
 			} else if (expression instanceof JstArg) {
 				JstArg arg = (JstArg) expression;
 				IJstType type = arg.getType();
 				String typeName = OBJECT_TYPE;
 				if (type != null) {
 					typeName = type.getName();
 				}
 				modelElement.addAll(Arrays.asList(getLocalVar(module,
 						arg.getName(), typeName, arg.getSource())));
 
 			} else if (expression instanceof FieldAccessExpr) {
 				FieldAccessExpr fieldAccExpr = (FieldAccessExpr) expression;
 				IExpr qualifier = fieldAccExpr.getExpr();
 				IJstType qualifierType = qualifier.getResultType();
 				if (qualifierType != null
 						&& "Vj$Type".equals(qualifierType.getName())) {
 					IJstType fieldType = fieldAccExpr.getResultType();
 					if (fieldType == null) {
 						return null;
 					}
 					if (fieldType instanceof JstTypeRefType) {
 						fieldType = ((JstTypeRefType) fieldType).getType();
 					}
 					modelElement.add(findResourceType(module,
 							fieldType.getName()));
 				} else {
 					modelElement.add(getModelElementByFieldAccessExpr(
 							fieldAccExpr, module));
 				}
 			} else if (expression instanceof MtdInvocationExpr) {
 				modelElement.addAll(Arrays.asList(getModelElementByMtdInvoExpr(
 						(MtdInvocationExpr) expression, module)));
 			}
 		}
 
 		return modelElement;
 	}
 
 	public static IModelElement getModelElementByFieldAccessExpr(
 			FieldAccessExpr expression, ISourceModule module) {
 		IModelElement modelElement = null;
 		IJstNode binding = expression.getName().getJstBinding();
 		if (binding == null) {
 			List children = expression.getChildren();
 			for (Object obj : children) {
 				if (obj instanceof JstIdentifier) {
 					String nativeTypeName = ((JstIdentifier) obj).getName();
 					if (findNativeSourceType(module, nativeTypeName) != null)
 						modelElement = findNativeSourceType(module,
 								nativeTypeName);
 
 				}
 			}
 		}
 
 		if (binding instanceof JstProperty) {
 			// get owner type of the property
 			final IJstType ownerType = ((JstProperty) binding).getOwnerType();
 			// get type of the property
 			final IJstType type = ((JstProperty) binding).getType();
 			// find result type by property owner type and property
 			// type.
 			IType resultType = findResultType(module, ownerType, type);
 			if (resultType != null
 					&& resultType.getParent() instanceof NativeVjoSourceModule) {
 				modelElement = resultType;
 			} else if (resultType != null) {
 				modelElement = resultType.getField(expression.getName()
 						.getName());
 			}
 
 		} else if (binding instanceof IJstType) {
 			final IJstType jstType = (IJstType) binding;
 			modelElement = findType(jstType);
 
 			// add to fix bug 5954
 			if (modelElement == null && jstType instanceof JstTypeRefType) {
 				JstPackage pack = jstType.getPackage();
 				String groupName = null;
 				if (pack != null) {
 					groupName = pack.getGroupName();
 				} else {
 					return null;
 				}
 
 				String typeName = ((JstTypeRefType) jstType)
 						.getReferencedNode().getName();
 
 				IType type = findType(getScriptProject(groupName), typeName);
 				if (type != null)
 					return type;
 				else
 					return findNativeSourceType(groupName, typeName, jstType);
 			}
 		} else if (isBase(expression)) {
 			// find element in base type
 			// FieldAccessExpr expr = (FieldAccessExpr) expression;
 			IJstType type = expression.getOwnerType();
 			IJstType baseType = type.getExtend();
 			// process if base type is not native vjo object.
 			if (baseType != null
 					&& !baseType.getName().equals(VjoKeywords.VJO_OBJECT)) {
 				modelElement = findType(baseType);
 			}
 		}
 		return modelElement;
 	}
 
 	/**
 	 * guess the selected modelElement by mtdInvocationExpr
 	 * 
 	 * @param expr
 	 * @param module
 	 * @return
 	 */
 	public static IModelElement[] getModelElementByMtdInvoExpr(
 			MtdInvocationExpr expr, ISourceModule module) {
 		/*
 		 * this method will effect update occurance, call hierarchy, search
 		 * method references also F3 function. Change this method should be much
 		 * more carefully
 		 */
 		IJstNode binding = getBinding(expr);
 		List<IMethod> methodElement = new ArrayList<IMethod>();
 		IType declareType = null;
 
 		if (binding instanceof JstMethod) {
 			// basically there should be a binding
 			IJstType ownerType = ((JstMethod) binding).getOwnerType();
 			if (isNativeType(ownerType)) {
 				return null;
 			}
 			declareType = CodeassistUtils.findType(ownerType);
 			if (declareType == null) {
 				declareType = CodeassistUtils.findNativeSourceType(ownerType);
 			}
 
 			if (declareType != null) {
 				try {
 					String funcName = getFunctionName(expr);
 					String[] paramTypes = getParameterTypes(expr);
 					// fix, also support parameters
 					if (declareType instanceof IJSType) {
 						methodElement.addAll(Arrays.asList(getMethod(
 								declareType, (JstMethod) binding)));
 
 						// native method exists always return false. a bug of
 						// Model Element?
 						if (!isNativeObject(methodElement.get(0))
 								&& !methodElement.get(0).exists()) {
 							// may be function d(int a), but called by d(),
 							// parameter is
 							// not passed.
 							methodElement.add(findMostMatchingMethod(
 									declareType, funcName, paramTypes));
 
 						}
 					} else {
 						// with no parameter
 						methodElement.add(declareType.getMethod(funcName));
 					}
 
 				} catch (Exception e) {
 					// e.printStackTrace();
 				}
 
 			}
 
 		} else {
 			// binding is null or JstType
 			// binding is JstType when met : this.vj$.B(); B is a constructor
 			// no binding only when vjo type?
 			IExpr methodIdentifier = expr.getMethodIdentifier();
 			if (methodIdentifier instanceof FieldAccessExpr) {
 				IJstType jstType = expr.getResultType();
 				if (jstType != null && jstType.getPackage() != null) {
 					FieldAccessExpr fae = (FieldAccessExpr) expr
 							.getMethodIdentifier();
 					IJstNode type = fae.getName().getJstBinding();
 					declareType = findType(jstType);
 					// create method element for constructor only else
 					// create type element
 					if (type instanceof JstConstructor) {
 						IType itype = findType(jstType);
 						if (itype != null) {
 							methodElement.add(itype.getMethod(itype
 									.getElementName()));
 						}
 					}
 
 				}
 			}
 
 			if (methodIdentifier instanceof JstIdentifier) {
 				List<IExpr> args = expr.getArgs();
 				IExpr qualify = expr.getQualifyExpr();
 				try {
 					if (qualify != null)
 						declareType = findTypeByQualify(module, qualify);
 
 					if (declareType != null) {
 						// without method type, have to guess method type
 						String[] paramTypes = getParameterTypes(expr);
 						methodElement.add(findMethodBySignature(
 								methodIdentifier.toString(),
 								Arrays.asList(paramTypes), declareType));
 					}
 
 					// Add by Oliver. 2009-06-11. A part of native type in
 					// method expression can't return Qualify expression and
 					// declareType.
 					// if (declareType == null && binding instanceof IJstType
 					// && isNativeType((IJstType) binding)) {
 					// IType type = CodeassistUtils
 					// .findNativeSourceType((IJstType) binding);
 					// methodElement = type.getMethod(type.getElementName());
 					// }
 				} catch (Exception e) {
 					// e.printStackTrace();
 					// this exception can eat, guess param type could
 					// not be accurate
 				}
 			}
 
 		}
 
 		return methodElement.toArray(new IModelElement[] {});
 
 	}
 
 	// original implementation
 	// private static IModelElement
 	// getModelElementByMtdInvoExpr(MtdInvocationExpr
 	// expr, ISourceModule module, IModelElement modelElement) {
 	// MtdInvocationExpr expr = (MtdInvocationExpr) expression;
 	// IJstNode binding = getBinding(expr);
 	//
 	// if (binding == null) {
 	// List children = expr.getChildren();
 	// for (Object obj : children) {
 	// if (obj instanceof FieldAccessExpr) {
 	// obj = ((FieldAccessExpr) obj).getExpr();
 	// }
 	// if (obj instanceof JstIdentifier) {
 	// String nativeTypeName = ((JstIdentifier) obj)
 	// .getName();
 	// if (findNativeSourceType(nativeTypeName) != null)
 	// modelElement = findNativeSourceType(nativeTypeName);
 	// }
 	// }
 	// }
 	// if (binding instanceof JstMethod) {
 	// // create model element by owner type and method name
 	// final IJstType ownerType = ((JstMethod) binding)
 	// .getOwnerType();
 	// final IJstType rtnType = ((JstMethod) binding).getRtnType();
 	// modelElement = getMethodElement(expression, module,
 	// modelElement, ownerType, rtnType);
 	//
 	// // Add by Oliver. Sometimes the native type is used in the
 	// // comment, here resolve it into IModelElement
 	// if (modelElement == null) {
 	//
 	// // find native source type if jst type hasn't package.
 	// modelElement = findNativeSourceType(ownerType);
 	// }
 	//
 	// } else if (expr.getResultType() != null) {
 	// // create model element by return type of the method and
 	// // method name
 	// IJstType jstType = expr.getResultType();
 	// if (jstType.getPackage() != null) {
 	// if (expr.getMethodIdentifier() instanceof FieldAccessExpr) {
 	// FieldAccessExpr fae = (FieldAccessExpr) expr
 	// .getMethodIdentifier();
 	// IJstNode type = fae.getName().getJstBinding();
 	// // create method element for constructor only else
 	// // create type element
 	// if (type instanceof JstConstructor) {
 	// IType itype = findType(jstType);
 	// if (itype != null) {
 	// modelElement = itype.getMethod(itype
 	// .getElementName());
 	// }
 	// } else {
 	// modelElement = findType(jstType);
 	// }
 	// } else {
 	// modelElement = findType(jstType);
 	// }
 	// } else {
 	// // find native source type if jst type hasn't package
 	// modelElement = findNativeSourceType(jstType);
 	// }
 	// }
 	// return modelElement;
 	// }
 
 	private static IJstNode getBinding(MtdInvocationExpr expr) {
 		IJstNode node = expr.getMethod();
 
 		if (node == null) {
 			if (expr.getMethodIdentifier() instanceof FieldAccessExpr) {
 				FieldAccessExpr accessExpr = (FieldAccessExpr) expr
 						.getMethodIdentifier();
 				node = accessExpr.getName().getJstBinding();
 			} else if (expr.getMethodIdentifier() instanceof JstIdentifier) {
 				node = ((JstIdentifier) expr.getMethodIdentifier())
 						.getJstBinding();
 
 			}
 
 		}
 
 		return node;
 	}
 
 	/**
 	 * Returns name of the function from the {@link MtdInvocationExpr} object.
 	 * Get name of the function from method identifier object of the
 	 * {@link MtdInvocationExpr} object.
 	 * 
 	 * @param expr
 	 *            {@link MtdInvocationExpr} object.
 	 * @return name of the function.
 	 */
 	private static String getFunctionName(MtdInvocationExpr expr) {
 		String name = null;
 
 		if (expr.getMethodIdentifier() instanceof JstIdentifier) {
 			JstIdentifier identifier = (JstIdentifier) expr
 					.getMethodIdentifier();
 			name = identifier.getName();
 		}
 
 		if (expr.getMethodIdentifier() instanceof FieldAccessExpr) {
 			FieldAccessExpr accessExpr;
 			accessExpr = (FieldAccessExpr) expr.getMethodIdentifier();
 			JstIdentifier identifier = accessExpr.getName();
 			name = identifier.getName();
 		}
 
 		return name;
 	}
 
 	/**
 	 * return parameter types of this function from {@link MtdInvocationExpr}
 	 * object.
 	 * 
 	 * @param expr
 	 * @return
 	 */
 	private static String[] getParameterTypes(MtdInvocationExpr expr) {
 		List<IExpr> params = expr.getArgs();
 		List args = new ArrayList();
 
 		for (IExpr param : params) {
 			if (param != null) {
 				if (param.getResultType() == null) {
 					args.add(OBJECT_TYPE);
 				} else {
 					args.add(param.getResultType().getName());
 				}
 			}
 
 		}
 		return (String[]) args.toArray(new String[args.size()]);
 
 	}
 
 	// private static IModelElement getMethodElement(Object expression,
 	// ISourceModule module, IModelElement modelElement,
 	// final IJstType ownerType, final IJstType rtnType) {
 	// IType resultType = findResultType(module, ownerType, rtnType);
 	// if (resultType != null
 	// && resultType.getParent() instanceof NativeVjoSourceModule) {
 	// modelElement = resultType;
 	// } else if (resultType != null) {
 	// MtdInvocationExpr expr = (MtdInvocationExpr) expression;
 	//
 	// String funcName = getFunctionName(expr);
 	// String[] paramTypes = getParameterTypes(expr);
 	// // fix, also support parameters
 	// if (resultType instanceof IJSType) {
 	// modelElement = ((IJSType) resultType).getMethod(funcName,
 	// paramTypes);
 	// if (!modelElement.exists()) {
 	// // may be function d(int a), but called by d(), parameter is
 	// // not passed.
 	// modelElement = findMostMatchingMethod(resultType, funcName,
 	// paramTypes);
 	//
 	// }
 	// } else {
 	// // with no parameter
 	// modelElement = resultType.getMethod(funcName);
 	// }
 	// }
 	// if (modelElement == null || modelElement.exists() == false) {
 	// return null;
 	// }
 	// return modelElement;
 	// }
 
 	private static IMethod findMostMatchingMethod(IType resultType,
 			String funcName, String[] paramTypes) {
 		IMethod mostMatchMethod = null;
 		float mostMatchRatio = 0f;
 		try {
 			IMethod[] methods = resultType.getMethods();
 			for (IMethod method : methods) {
 				if (method.getElementName().equals(funcName)) {
 					int matchCount = 0;
 					String[] params = ((JSSourceMethod) method)
 							.getParameterTypes();
 					int min = Math.min(paramTypes.length, params.length);
 					if (min == 0)
 						continue;
 					for (int i = 0; i < params.length; i++) {
 						String param = params[i];
 						if (i < min && paramTypes[i].equals(param)) {
 							matchCount++;
 						}
 					}
 
 					float matchRatio = matchCount / paramTypes.length + min;
 					if (matchRatio > mostMatchRatio) {
 						mostMatchMethod = method;
 						mostMatchRatio = matchRatio;
 					}
 
 				}
 			}
 
 		} catch (ModelException e) {
 			e.printStackTrace();
 		}
 
 		return mostMatchMethod;
 	}
 
 	public static IType findType(final IJstType jstType) {
 		if (isNativeType(jstType)) { // here obey the old cc code style,
 			// return null if it is native type
 			return null;
 		}
 		JstPackage pack = jstType.getPackage();
 		String groupName = null;
 		if (pack != null) {
 			groupName = pack.getGroupName();
 		} else {
 			return null;
 		}
 		ScriptProject scriptProject = getScriptProject(groupName);
 		// need to find by file name
 		IType typeByTypeNAme  = findType(scriptProject, jstType.getName());
 		
 		// in the case where scriptProject is empty and type is not found test external project
 		if(groupName!=null && groupName.endsWith(".zip")){
 //			ScriptProject externalScriptPRoject = getScriptProject(ExternalFoldersManager.EXTERNAL_PROJECT_NAME );
 //			
 //			typeByTypeNAme  = findType(externalScriptPRoject, jstType.getName());
 			NativeVjoSourceModule nativeModule = CodeassistUtils.findNativeModule(groupName, jstType.getName());
 			if(nativeModule!=null){
 				typeByTypeNAme = nativeModule.getVjoType();
 			}
 		}
 				
 		if(typeByTypeNAme==null && jstType.getSource()!=null){
 			IBinding binding = jstType.getSource().getBinding();
 			if(binding!=null && binding instanceof SimpleBinding && binding.getName()!=null &&  binding.getName().lastIndexOf(".")!=-1){
 				String pkg = binding.getName().substring(0, binding.getName().lastIndexOf("."));
 				String typeName = binding.getName().substring(binding.getName().lastIndexOf(".")+1, binding.getName().length());
 				typeByTypeNAme = findType(scriptProject, typeName, pkg );
 			}
 			
 		}
 		
 		return typeByTypeNAme;
 		
 	}
 
 	private static boolean isBase(Object expression) {
 		FieldAccessExpr expr = (FieldAccessExpr) expression;
 		return expr.toExprText().equals("this.base");
 	}
 
 	private static IType findResultType(ISourceModule module,
 			IJstType ownerType, IJstType rtnType) {
 
 		IType type = null;
 
 		if (ownerType != null && ownerType.getPackage() != null) {
 
 			type = findType(getScriptProject(ownerType.getPackage()
 					.getGroupName()), ownerType.getName());
 		}
 
 		if (type == null && ownerType != null) {
 
 			type = findNativeSourceType(rtnType);
 		}
 
 		if (type == null && rtnType != null) {
 
 			type = findNativeSourceType(module, rtnType.getName());
 		}
 
 		return type;
 	}
 
 	public static ScriptProject getScriptProject(String groupName) {
 		if (groupName == null || groupName.length() == 0) {
 			return null;
 		}
 		Model model = ModelManager.getModelManager().getModel();
 		if (!isNativeGroup(groupName)) {
 			IScriptProject project = model.getScriptProject(groupName);
 			if (project instanceof ScriptProject) {
 				return (ScriptProject) project;
 			}
 		}
 		return null;
 	}
 
 	public static IModelElement[] resolveQualifiedNameReference(
 			Object expression, ISourceModule module) {
 		List<IModelElement> elements = null;
 		try {
 			elements = resolveQualifiers(expression, module);
 		} catch (Exception e) {
 			DLTKCore.error("resolving selection node to IModelElement failed",
 					e);
 		}
 
 		if (elements != null && elements.size() > 0) {
 			return new IModelElement[] { elements.get(elements.size() - 1) };
 		}
 
 		return null;
 	}
 
 	public static String getClassName(IFile file) {
 		Path path = (Path) file.getProjectRelativePath();
 		String group = file.getProject().getName();
 		List<IPath> sourceFolders = getSourceFolders(group);
 		return getClassName(sourceFolders, path);
 	}
 
 	/**
 	 * Returns list of the source folders by specified group.
 	 * 
 	 * @param group
 	 *            specified group
 	 * @return list of the {@link IPath} object.
 	 */
 	private static List<IPath> getSourceFolders(String group) {
 		Model model = ModelManager.getModelManager().getModel();
 		IScriptProject project = model.getScriptProject(group);
 		List<IPath> sourceFolders = new ArrayList<IPath>();
 		try {
 			IBuildpathEntry[] entries = project.getResolvedBuildpath(true);
 			createSourceFolder(sourceFolders, entries);
 			buildPathMap.put(group, entries);
 		} catch (ModelException e) {
 			IBuildpathEntry[] entries = buildPathMap.get(group);
 			if (entries != null) {
 				createSourceFolder(sourceFolders, entries);
 			}
 		}
 		return sourceFolders;
 	}
 
 	private static void createSourceFolder(List<IPath> sourceFolders,
 			IBuildpathEntry[] entries) {
 		for (IBuildpathEntry buildpathEntry : entries) {
 			if (buildpathEntry.getEntryKind() == IBuildpathEntry.BPE_SOURCE) {
 				// remove project segment
 				sourceFolders.add(buildpathEntry.getPath().removeFirstSegments(
 						1));
 			}
 		}
 	}
 
 	/**
 	 * Return class name if the path object contains source folders information.
 	 * 
 	 * @param sourceFolders
 	 *            list of the {@link IPath} source filders object.
 	 * @param path
 	 *            path of the resource
 	 * @return class name.
 	 */
 	private static String getClassName(List<IPath> sourceFolders, IPath path) {
 
 		StringBuffer buffer = new StringBuffer();
 
 		// remove source folder segment
 		for (IPath path2 : sourceFolders) {
 			if (path2.toString().length() > 0 && path2.isPrefixOf(path)) {
 				path = path.removeFirstSegments(path2.segmentCount());
 				break;
 			}
 		}
 
 		String[] segmStrings = path.segments();
 
 		for (int i = 0; i < segmStrings.length; i++) {
 			String name = segmStrings[i];
 
 			// remove all characters after dot symbol
 			int dotIndex = name.lastIndexOf(DOT);
 			if (dotIndex != -1) {
 				name = name.substring(0, dotIndex);
 			}
 			buffer.append(name);
 
 			if (i + 1 != segmStrings.length) {
 				buffer.append(DOT);
 			}
 
 		}
 		return buffer.toString();
 	}
 
 	/**
 	 * Return the type name in back end Dsf style for Native type (has
 	 * correspondent eclipse resource)
 	 * 
 	 * @param resource
 	 * @return
 	 */
 	public static SourceTypeName getTypeName(IResource resource) {
 
 		URI locationURI = resource.getLocationURI();
 		if (locationURI !=null && locationURI.getScheme().equals("typespace")) {
 			String groupName = locationURI.getHost();
 			String typeName = locationURI.getPath();
 			typeName = typeName.replace("/", ".");
 			typeName = typeName.substring(1, typeName.length());
 			typeName = typeName.substring(0, typeName.indexOf(".js"));
 			return new SourceTypeName(groupName, typeName);
 		}
 
 		String project = resource.getProject().getName();
 		List<IPath> sourceFolders = getSourceFolders(project);
 		String className = getClassName(sourceFolders,
 				resource.getProjectRelativePath());
 		SourceTypeName name = new SourceTypeName(project, className);
 		name.setAction(SourceTypeName.CHANGED);
 		return name;
 	}
 
 	/**
 	 * Return the type name in back end Dsf style for Native type (has no
 	 * correspondent eclipse resource)
 	 * 
 	 * @param jstType
 	 * @return
 	 */
 	public static SourceTypeName getNativeTypeName(IJstType jstType) {
 		String className = jstType.getName();
 		String groupName = getNativeGroupName(className);
 		SourceTypeName name = new SourceTypeName(groupName, className);
 		name.setAction(SourceTypeName.CHANGED);
 		return name;
 	}
 
 	/**
 	 * Returns completion token by specified {@link IMember} element in current
 	 * {@link ISourceModule} module. Append before completion
 	 * {@link CodeassistUtils#THIS_VJO_STATIC} string if static completion in
 	 * variable context.
 	 * 
 	 * @param element
 	 *            {@link IMember} element.
 	 * @param module
 	 *            {@link ISourceModule} module.
 	 * @return completion token.
 	 */
 	public static char[] getCompletionToken(IMember element,
 			org.eclipse.dltk.mod.core.ISourceModule module) {
 		StringBuffer buffer = new StringBuffer();
 
 		String typeMemberName = element.getParent().getElementName();
 		if (module == null) {
 			module = element.getSourceModule();
 		}
 		String typeName = module.getElementName().replaceAll(".js", ""); // remove
 		// file
 		// extension
 
 		if (typeName == null || typeMemberName == null)
 			return null;
 		boolean isNative = element.getAncestor(IModelElement.SOURCE_MODULE) instanceof NativeVjoSourceModule;
 		if (!isNative && !CompletionContext.isCompletedContext()) {
 			if (typeName.equals(typeMemberName)
 					&& CompletionContext.isStaticContext()) {
 				if (!CompletionContext.isThisWithinStaticContext())
 					buffer.append(THIS_STATIC);
 			} else if (!CompletionContext.isVariableContext()) {
 				buffer.append(THIS_VJO_STATIC);
 				buffer.append(typeMemberName + '.');
 			}
 		}
 		char[] token = buffer.toString().toCharArray();
 		return token;
 	}
 
 	public static char[] getTypeCompletionToken(IType type) {
 
 		StringBuffer buffer = new StringBuffer();
 
 		String typeName = type.getElementName().replaceAll(".js", "");
 
 		if (typeName == null)
 			return null;
 
 		if (!(type.getAncestor(IType.SOURCE_MODULE) instanceof NativeVjoSourceModule)) {
 			buffer.append(THIS_VJO_STATIC);
 		}
 		buffer.append(typeName);
 
 		char[] token = buffer.toString().toCharArray();
 
 		return token;
 	}
 
 	public static String autoCreateTypeNameFor(ISourceModule sourceModule) {
 
 		IPath path = sourceModule.getModelElement().getPath()
 				.removeFirstSegments(1); // remove a project name
 
 		if (sourceModule instanceof JSSourceModule) {
 			IScriptProject project = ((JSSourceModule) sourceModule)
 					.getModelElement().getScriptProject();
 			List<IPath> sourceFolders = getSourceFolders(project
 					.getElementName());
 
 			return getClassName(sourceFolders, path);
 		}
 		return null;
 	}
 
 	private static IModelElement findNativeModule(ISourceModule module,
 			String name) {
 		SourceModule sourceModule = (SourceModule) module;
 		ScriptFolder project = (ScriptFolder) sourceModule.getParent();
 
 		IModelElement element = findNativeElement(name, project);
 		return element;
 	}
 
 	public static IType findNativeSourceType(ISourceModule module, String name) {
 		IType type = null;
 		NativeVjoSourceModule sourceModule = (NativeVjoSourceModule) findNativeModule(
 				module, name);
 		if (sourceModule != null) {
 			type = sourceModule.getVjoType();
 		}
 		return type;
 	}
 
 	private static IModelElement findNativeElement(String name,
 			ScriptFolder folder) {
 
 		NativeVjoSourceModule element = null;
 		TypeSpaceMgr mgr = TypeSpaceMgr.getInstance();
 		for (int i = 0; i < TsLibLoader.getDefaultLibNames().length; i++) {
 			String groupName = TsLibLoader.getDefaultLibNames()[i];
 			if (mgr.existType(groupName, name)) {
 				element = new NativeVjoSourceModule(folder, groupName, name);
 				break;
 			}
 
 		}
 
 		// if (TypeSpaceMgr.WINDOW_VAR.equals(name)) {
 		// element = createNativeModule(folder, TypeSpaceMgr.WINDOW);
 		// }
 
 		return element;
 	}
 
 	// private static IJstType findNativeJstType(ISourceModule module,
 	// String elementName) {
 	// SourceModule sourceModule = (SourceModule) module;
 	// // ScriptFolder parent = (ScriptFolder) sourceModule.getParent();
 	//
 	// return findNativeJstType(elementName);
 	// }
 
 	/**
 	 * Finds native {@link IJstType} object by specified {@link ScriptFolder}
 	 * object and name. Find type by next steps : in
 	 * {JstTypeSpaceMgr.JS_NATIVE_GRP} group in type space , in members of the
 	 * {@link TypeSpaceMgr#WINDOW} native object and members of the
 	 * {@link TypeSpaceMgr#GLOBAL} native object.
 	 * 
 	 * @param name
 	 *            name of the {@link IJstType} object.
 	 * @return {@link IJstType} object.
 	 */
 	public static IJstType findNativeJstType(String name) {
 
 		TypeSpaceMgr mgr = TypeSpaceMgr.getInstance();
 		for (int i = 0; i < TsLibLoader.getDefaultLibNames().length; i++) {
 			String groupName = TsLibLoader.getDefaultLibNames()[i];
 			if (mgr.existType(groupName, name)) {
 				IJstType jtype = mgr.findType(new TypeName(groupName, name));
 				nativeTypeGroupMap.put(name, groupName);
 				return jtype;
 			}
 
 		}
 
 		// find between window properties or methods
 		IJstType windowType = mgr.findType(new TypeName(
 				JstTypeSpaceMgr.JS_BROWSER_GRP, TypeSpaceMgr.WINDOW));
 		IJstType type = findMemberType(windowType, name);
 
 		if (type == null) {
 			IJstType globalType = mgr.findType(new TypeName(
 					JstTypeSpaceMgr.JS_NATIVE_GRP, TypeSpaceMgr.GLOBAL));
 			type = findMemberType(globalType, name);
 		}
 		return type;
 
 	}
 
 	public static IJstType findJstType(String groupName, String name) {
 		TypeSpaceMgr mgr = TypeSpaceMgr.getInstance();
 		return mgr.findType(new TypeName(groupName, name));
 	}
 
 	private static boolean isEmpty(IJstType jstType) {
 		return jstType == null || jstType.getName() == null;
 	}
 
 	private static IJstType findMemberType(IJstType type, String memberName) {
 		IJstProperty prop = type.getProperty(memberName);
 
 		if (prop != null) {
 			return prop.getType();
 		}
 
 		IJstMethod method = type.getMethod(memberName);
 		if (method != null) {
 			return method.getRtnType();
 		}
 		return null;
 	}
 
 	public static NativeVjoSourceModule createNativeModule(ScriptFolder folder,
 			String name) {
 		return new NativeVjoSourceModule(folder, JstTypeSpaceMgr.JS_NATIVE_GRP,
 				name);
 	}
 
 	public static IType findType(ISourceModule sm, String typeName) {
 		IType type = null;
 
 		if (sm instanceof NativeVjoSourceModule) {
 			NativeVjoSourceModule module = (NativeVjoSourceModule) sm;
 			type = module.getVjoType();
 		} else {
 			type = findResourceType(sm, typeName);
 		}
 
 		return type;
 	}
 
 	public static NativeVjoSourceModule createNativeModule(ISourceModule module) {
 		IVjoSourceModule sourceModule = (IVjoSourceModule) module;
 		ScriptFolder folder = (ScriptFolder) sourceModule.getParent();
 
 		return createNativeModule(folder, sourceModule.getElementName());
 	}
 
 	public static NativeVjoSourceModule createNativeModule(
 			ISourceModule module, String name) {
 		IVjoSourceModule sourceModule = (IVjoSourceModule) module;
 		ScriptFolder folder = (ScriptFolder) sourceModule.getParent();
 		return createNativeModule(folder, name);
 	}
 
 	public static IType createNativeType(ISourceModule module) {
 		NativeVjoSourceModule sourceModule = createNativeModule(module);
 		return sourceModule.getVjoType();
 	}
 
 	public static IType createNativeType(ISourceModule module, String name) {
 		NativeVjoSourceModule sourceModule = createNativeModule(module, name);
 		return sourceModule.getVjoType();
 	}
 
 	/**
 	 * Finds {@link IModelElement} element within {@link IParent} object by
 	 * specified name. If parent not contains child with specified name then
 	 * calls {@link CodeassistUtils#findResourceType(ISourceModule, String)}
 	 * method.
 	 * 
 	 * @param type
 	 *            {@link IParent} object.
 	 * @param name
 	 *            name of the {@link IModelElement} element for search.
 	 * @return {@link IModelElement} object.
 	 */
 	public static IModelElement findModelElementByName(IParent type, String name) {
 		IModelElement[] children = null;
 		IModelElement result = null;
 
 		if (type == null || name == null)
 			return null;
 
 		// get children of the element
 		try {
 			children = type.getChildren();
 		} catch (ModelException e) {
 			DLTKCore.error(e.getMessage(), e);
 		}
 
 		// search element in children list by name
 		if (children == null)
 			return null;
 		for (IModelElement child : children) {
 			if (name.equals(child.getElementName())) {
 				result = child;
 			} else {
 				continue;
 			}
 		}
 
 		// if result is null then search resource in project.
 		if (result == null)
 			result = findResourceType(
 					(ISourceModule) ((VjoSourceType) type).getSourceModule(),
 					name);
 		return result;
 	}
 
 	public static boolean isMethodHasParamByName(IMethod method, String name) {
 		String element = null;
 
 		try {
 			String[] params = method.getParameters();
 			String[] inits = method.getParameters();
 
 			for (String item : params) {
 				if (item.equals(name))
 					element = name;
 			}
 			for (String item : inits) {
 				if (item.equals(name))
 					element = name;
 			}
 		} catch (ModelException e) {
 			DLTKCore.error(e.getMessage(), e);
 		}
 
 		return (element != null) ? true : false;
 	}
 
 	public static boolean isVjoSourceModule(IModelElement element) {
 		if (!(element instanceof VjoSourceModule)) {
 			return false;
 		}
 		IVjoSourceModule module = (IVjoSourceModule) element;
 		return module.getScriptProject().getLanguageToolkit() != null
 				&& module.getScriptProject().getLanguageToolkit().getNatureId()
 						.equals(VjoNature.NATURE_ID);
 	}
 
 	public static IType getCurrentType(ISourceModule module, String typeName) {
 
 		IType currentType = findResourceType(module, typeName);
 		if (currentType == null) {
 			IVjoSourceModule sourceModule = (IVjoSourceModule) module;
 			try {
 				if (sourceModule.getTypes().length > 0) {
 					currentType = sourceModule.getTypes()[0];
 				}
 			} catch (ModelException e) {
 				DLTKCore.error(e.toString(), e);
 			}
 		}
 		return currentType;
 	}
 
 	/**
 	 * Finds a word by specified offset and returns the region of word.
 	 * 
 	 * @param source
 	 * @param offset
 	 * 
 	 * @return the region of found word
 	 */
 	public static int findWordOffset(char[] source, int offset) {
 
 		int start = -2;
 
 		int pos = offset;
 		char c;
 
 		// start searching by the left hand
 		while (pos > 0 && pos < source.length) {
 			c = source[pos];
 			// stop searching if character is whitespace or not a letter
 			if (!Character.isWhitespace(c) && Character.isLetter(c))
 				break;
 			--pos;
 		}
 
 		start = pos;
 		pos = offset;
 
 		// start searching by the right hand
 		while (pos < source.length - 1) {
 			c = source[pos];
 			// stop searching if character is whitespace
 			if (!Character.isWhitespace(c))
 				break;
 			++pos;
 		}
 
 		if (start >= -1 && start != offset)
 			return start;
 
 		return offset;
 	}
 
 	/**
 	 * Returns true if value is valid java identifier.
 	 * 
 	 * @param value
 	 *            string for check
 	 * @return true if value is valid java identifier.
 	 */
 	public static boolean isValidIdentifier(String value) {
 
 		boolean isValid = true;
 
 		if (value == null || value.length() == 0) {
 			return false;
 		}
 
 		// check that first symbol is java identifier
 		if (!Character.isJavaIdentifierStart(value.charAt(0))) {
 			return false;
 		}
 
 		// check that second to last symbols is java identifier part.
 		if (value.length() > 1) {
 
 			for (int i = 1; i < value.length(); i++) {
 				char c = value.charAt(i);
 				if (!Character.isJavaIdentifierPart(c)) {
 					isValid = false;
 					break;
 				}
 			}
 
 		}
 		return isValid;
 	}
 
 	/**
 	 * Returns relevance value for the {@link CompletionProposal} object.
 	 * 
 	 * @param proposal
 	 *            {@link CompletionProposal} object.
 	 * @return relevamce value.
 	 */
 	public static int getRelevance(CompletionProposal proposal) {
 		int baseRelevance = 60;
 		Object obj = proposal.extraInfo;
 		if (obj == null) { // Jack: used to judge if it is an keyword
 			baseRelevance = 16;
 		} else if (obj instanceof String) {
 			String s = (String) obj;
 			int index = s.lastIndexOf("-");
 			if (index > -1) {
 				s = s.substring(index + 1);
 				if (TypeSpaceMgr.WINDOW.equalsIgnoreCase(s.trim())
 						|| TypeSpaceMgr.GLOBAL.equalsIgnoreCase(s.trim())) {
 					baseRelevance = 48;
 				} else if (TypeSpaceMgr.OBJECT.equalsIgnoreCase(s.trim())) {
 					baseRelevance = 32;
 				}
 			} else {// for function parameter
 				baseRelevance = 72;
 			}
 		}
 		return baseRelevance;
 	}
 
 	public static boolean isNativeObject(IMember member) {
 		IModelElement element;
 		if (member instanceof IType) {
 			element = ((IType) member).getParent();
 			if (element instanceof NativeVjoSourceModule) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			if (member.getParent() == null
 					|| member.getParent().getParent() == null) {
 				return true;
 			} else {
 				return (member.getParent().getParent() instanceof NativeVjoSourceModule);
 			}
 		}
 	}
 
 	public static boolean isStatic(IMember member) {
 		int flags = 0;
 		try {
 			flags = member.getFlags();
 			return (flags & Modifiers.AccStatic) != 0;
 		} catch (ModelException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public static boolean isStaticPrefix(IMember member) {
 		return isStatic(member) && !CompletionContext.isStaticContext();
 	}
 
 	public static String getPackageName(IType type) {
 		if (isNativeObject(type)) {
 			return "";
 		}
 		IScriptFolder path = type.getScriptFolder();
 		String packageName = path.getElementName().replace('/', '.');
 		return packageName;
 	}
 
 	/**
 	 * @param jstType
 	 * @return if the type come from native and ExternalSourceModule
 	 */
 	public static boolean isNativeType(IJstType jstType) {
 		JstPackage pack = jstType.getPackage();
 		if (pack == null) {
 			return false;
 		} else if (true) {
 			return false;
 		}
 
 		// String gname = pack.getGroupName();
 		String gname = pack.getGroupName();
 		if (gname == null || gname.trim().length() == 0) {
 			// no group name
 			return true;
 		}
 
 		return isNativeGroup(gname);
 	}
 
 	public static boolean isNativeGroup(String groupName) {
 		return TsLibLoader.isDefaultLibName(groupName);
 	}
 
 	public static IType findNativeSourceType(String groupName, String name) {
 		return findNativeSourceType(groupName, name, null);
 	}
 
 	public static IType findNativeSourceType(String groupName, String name,
 			IJstType jtype) {
 		if (jtype instanceof JstTypeRefType) {
 			jtype = ((JstTypeRefType) jtype).getReferencedNode();
 		}
 		String actualName = name;
 		if (actualName == null) {
 			return null;
 		}
 		if (groupName == null) {
 			groupName = getNativeGroupName(actualName);
 		}
 		if (StringUtils.isBlankOrEmpty(groupName)) {
 			return null;
 		}
 		NativeVjoSourceModule nModule = findNativeModule(groupName, actualName);
 
 		// Modify by Oliver, 2009-12-01, fix findbugs bug.
 		if (nModule == null) {
 			return null;
 		}
 		if (jtype == null || !actualName.equals(jtype.getName())) {
 			IJstType jtype1 = findJstType(groupName, name);
 			nModule.setJstType(jtype1);
 
 		} else {
 			nModule.setJstType(jtype);
 
 		}
 
 		if (jtype == null || actualName.equals(jtype.getName())) {
 			return nModule.getVjoType();
 		} else {
 			// sub type
 			IType type = nModule.getVjoType();
 			IType subType = getSubType(type, jtype.getSimpleName());
 			if (subType == null) {
 				subType = nModule.getVjoType().getType(jtype.getSimpleName());
 			}
 			return subType;
 		}
 
 	}
 
 	private static IType getSubType(IType type, String simpleName) {
 		try {
 			IModelElement[] elements = type.getChildren();
 			for (int i = 0; i < elements.length; i++) {
 				IModelElement modelElement = elements[i];
 				if (modelElement instanceof IType) {
 					IType tType = (IType) modelElement;
 					if (tType.getElementName().equals(simpleName)) {
 						return tType;
 					}
 				}
 			}
 		} catch (ModelException e) {
 			return null;
 		}
 		return null;
 	}
 
 	public static IJstType getOuterType(IJstType type) {
 		IJstNode node = type;
 		while (node != null && node instanceof IJstType) {
 			type = (IJstType) node;
 			node = type.getParentNode();
 		}
 		return type;
 	}
 
 	/**
 	 * Return Native source type. for inner type, the outer type will be
 	 * returned. for example: vjo.sysout, input sysout, will return jstType:vjo
 	 * 
 	 * @param jtype
 	 * @return
 	 */
 	public static IType findNativeSourceType(IJstType jtype) {
 		IJstType outerType = getOuterType(jtype);
 		String name = outerType.getName();
 		String groupName = null;
 		JstPackage pack = jtype.getPackage();
 		if (pack != null) {
 			groupName = pack.getGroupName();
 		}
 		IType type = findNativeSourceType(groupName, name, jtype);
 		return type;
 	}
 
 	public static ScriptFolder getDefaultNativeSourceFolder(String groupName) {
 		if (!defaultNativeScriptFolderMap.containsKey(groupName)) {
 			createDefaultNativeSourceFolder(groupName);
 		}
 		return defaultNativeScriptFolderMap.get(groupName);
 
 	}
 
 	private static void createDefaultNativeSourceFolder(String groupName) {
 		final IScriptProject project = new ScriptProject(ResourcesPlugin
 				.getWorkspace().getRoot().getProject(groupName), ModelManager
 				.getModelManager().getModel()) {
 
 			IDLTKLanguageToolkit toolkit = null;
 
 			public IDLTKLanguageToolkit getLanguageToolkit() {
 				if (toolkit == null) {
 					toolkit = (IDLTKLanguageToolkit) InternalDLTKLanguageManager
 							.getLanguageToolkitsManager().getObject(
 									VjoNature.NATURE_ID);
 				}
 				return toolkit;
 			}
 
 			@Override
 			public boolean isOnBuildpath(IModelElement element) {
 				return true;
 			}
 
 			@Override
 			public String getElementName() {
 				return "Native project";
 			}
 
 			public int hashCode() {
 				return 1001;
 			}
 
 			@Override
 			protected boolean buildStructure(OpenableElementInfo info,
 					IProgressMonitor pm, Map newElements,
 					IResource underlyingResource) throws ModelException {
 				return true;
 			}
 
 		};
 
 		final ScriptFolder folder = new ExternalScriptFolder(null, new Path("")) {
 			@Override
 			public IScriptProject getScriptProject() {
 				return project;
 			}
 
 			@Override
 			public IModelElement getParent() {
 				return project;
 			}
 
 			@Override
 			public IPath getPath() {
 				return new Path("");
 			}
 
 			@Override
 			public int hashCode() {
 				return 100;
 			}
 
 			@Override
 			public boolean equals(Object o) {
 				return (o instanceof ExternalScriptFolder)
 						&& ((ExternalScriptFolder) o).getParent() == project;
 			}
 
 			@Override
 			protected void generateInfos(Object info, HashMap newElements,
 					IProgressMonitor pm) throws ModelException {
 				// do nothing
 			}
 
 			@Override
 			protected void getHandleMemento(StringBuffer buff) {
 				buff.append(getHandleMementoDelimiter());
 				escapeMementoName(buff, getElementName());
 			}
 
 			/**
 			 * add by kevin, in case of ClassCastExcetpion
 			 * 
 			 * @return
 			 */
 			public IResource getResource() {
 				return null;
 			}
 
 		};
 		defaultNativeScriptFolderMap.put(groupName, folder);
 	}
 
 	private static NativeVjoSourceModule findNativeModule(String groupName,
 			String name) {
 		String key = name + "@" + groupName;
 		if (!nativeModuleMap.containsKey(key)) {
 			NativeVjoSourceModule nModule = new NativeVjoSourceModule(
 					getDefaultNativeSourceFolder(groupName), groupName, name);
 			nativeModuleMap.put(key, nModule);
 		}
 		return nativeModuleMap.get(key);
 	}
 
 	public static String getNativeGroupName(String typeName) {
 		if (!nativeTypeGroupMap.containsKey(typeName)) {
 			TypeSpaceMgr mgr = TypeSpaceMgr.getInstance();
 			for (int i = 0; i < TsLibLoader.getDefaultLibNames().length; i++) {
 				String groupName = TsLibLoader.getDefaultLibNames()[i];
 				if (mgr.existType(groupName, typeName)) {
 					nativeTypeGroupMap.put(typeName, groupName);
 				}
 			}
 		}
 		return nativeTypeGroupMap.get(typeName);
 	}
 
 	private static IType findTypeByQualify(ISourceModule sourceModule,
 			IExpr qualify) {
 		Assert.isNotNull(qualify);
 		List al = qualify.getChildren();
 		if (al == null || al.size() == 0)
 			return null;
 
 		IJstType mainJstType = null;
 		IJstType type = null;
 		int mainTypeIndex = 0;
 		if (al.size() > 0) {
 			IJstNode jstNode = (IJstNode) al.get(al.size() - 1);
 			String mainType = "";
 			if (jstNode instanceof JstIdentifier) {
 				mainType = ((JstIdentifier) jstNode).toString();
 				type = CodeassistUtils.findNativeJstType(mainType);
 				mainTypeIndex = al.size() - 1;
 			} else if (jstNode instanceof FieldAccessExpr) {
 				String fieldExpr = ((FieldAccessExpr) jstNode).toExprText();
 				// happens when: Class A extends or needs B, and in an A method,
 				// call this.vj$.B.method().
 				if ("this.vj$".equals(fieldExpr)) {
 					if (al.size() > 1
 							&& al.get(al.size() - 2) instanceof JstIdentifier) {
 						mainType = ((JstIdentifier) al.get(al.size() - 2))
 								.toString();
 						IType iType = CodeassistUtils.findType(sourceModule,
 								mainType);
 						if (iType == null) {
 							iType = CodeassistUtils.findNativeSourceType(
 									sourceModule, mainType);
 						}
 
 						type = ((IVjoSourceModule) iType.getSourceModule())
 								.getJstType();
 						mainTypeIndex = al.size() - 2;
 					}
 
 				}
 			}
 
 			mainJstType = type;
 		}
 
 		if (type != null && al.size() > 1 && mainTypeIndex > 1) {
 			for (int i = mainTypeIndex - 1; i >= 0; i--) {
 				IExpr expr = (IExpr) al.get(i);
 				IJstType embedType = type.getEmbededType(expr.toString());
 				if (embedType == null) {
 					break;
 				} else {
 					type = embedType;
 				}
 			}
 		}
 
 		if (type != null) {
 			// FIXME inner type is not supported in type space!
 			// return CodeassistUtils.findNativeSourceType(type);
 		}
 
 		if (mainJstType == null)
 			return null;
 		if (mainJstType != null && isNativeType(mainJstType)) {
 			// temporary support main type only. when support inner type, change
 			// here!
 			return CodeassistUtils.findNativeSourceType(mainJstType);
 		} else {
 			return CodeassistUtils.findType(mainJstType);
 		}
 
 	}
 
 	public static boolean isBinaryType(IJstType jstType) {
 		String group = jstType.getPackage().getGroupName();
 		return isBinaryPath(group);
 
 	}
 
 	public static boolean isBinaryPath(String path) {
 		if (StringUtils.isBlankOrEmpty(path)) {
 			return false;
 		}
 		return path.endsWith(SUFFIX_ZIP) || path.endsWith(SUFFIX_JAR);
 	}
 
 	public static boolean isDefaultNativeSourceFolder(IModelElement parent) {
 		return defaultNativeScriptFolderMap.containsValue(parent);
 	}
 
 	// add by patrick
 	// for jst node handling
 
 	/**
 	 * True is the property belongs to "Vj$Type" type which is the type of
 	 * "this.vj$".
 	 * 
 	 * @param jstProperty
 	 * @return boolean
 	 */
 	public static boolean isVjDollarProp(IJstProperty jstProperty) {
 		if (jstProperty == null) {
 			return false;
 		}
 		return "Vj$Type".equals(jstProperty.getOwnerType().getName());
 	}
 
 	/**
 	 * Lookup the given IJstType, and find the target type if it is
 	 * JstProxyType. Or return the given type if others.
 	 * 
 	 * @param type
 	 * @return IJstType
 	 */
 	public static IJstType lookupJstType(IJstType type) {
 		if (type == null) {
 			return null;
 		}
 		if (type instanceof JstProxyType) {
 			return lookupJstType(((JstProxyType) type).getType());
 		}
 		return type;
 	}
 
 	/**
 	 * Find the jst method which contains the given node, or null if the given
 	 * node is null or is not under a method.
 	 * 
 	 * @param node
 	 * @return
 	 */
 	public static IJstMethod findDeclaringMethod(IJstNode node) {
 		while (node != null && !(node instanceof IJstType)) {
 			if (node instanceof JstMethod)
 				return (IJstMethod) node;
 			else
 				node = node.getParentNode();
 		}
 
 		return null;
 	}
 
 	/**
 	 * Quick way to find methods which are nested
 	 * 
 	 * @param dltkType
 	 * @param jstMethod
 	 * @return
 	 * @throws ModelException
 	 */
 	public static IModelElement findDeclaringMethodChain(IType dltkType,
 			IJstNode jstMethod) throws ModelException {
 		// TODO Auto-generated method stub
 		Deque<String> dq = new ArrayDeque<String>();
 		while (jstMethod != null && !(jstMethod instanceof IJstType)) {
 			if (jstMethod instanceof JstMethod) {
 				if(((JstMethod) jstMethod).getName()!=null){
 					dq.push(((JstMethod) jstMethod).getName().getName());
 					jstMethod = jstMethod.getParentNode();
 				}
 			} else {
 				jstMethod = jstMethod.getParentNode();
 			}
 		}
 
 		ModelElement elem = (ModelElement) dltkType;
 		Iterator<String> it = dq.iterator();
 		while (it.hasNext()) {
 
 			String string = it.next();
 
 			if (elem instanceof VjoSourceType) {
 				IMethod[] methods = dltkType.getMethods();
 				for (IMethod method : methods) {
 					if (method.getElementName().equals(string)) {
 						elem = (ModelElement) method;
 						break;
 
 					}
 				}
 			} else {
 				IModelElement[] children = elem.getChildren();
 				if (children.length == 0) {
 					return elem;
 				}
 				for (int i = 0; i < children.length; i++) {
 					if (IModelElement.METHOD == children[i].getElementType()
 							&& children[i].getElementName().equals(string)) {
 						elem = (JSSourceMethod) children[i];
 
 					}
 
 				}
 			}
 		}
 
 		return elem;
 	}
 
 	public static IModelElement findDeclaringObjectLiteralChain(
 			ModelElement startingBlock, IJstNode node) throws ModelException {
 		Deque<String> dq = new ArrayDeque<String>();
 		while (node != null && !(node instanceof IJstType)) {
 			if (node instanceof JstMethod && !(node instanceof JstProxyMethod)) {
 				break;
 			} else if (node instanceof JstProxyMethod) {
 				dq.push(((IJstMethod) node).getName().getName());
 				if(node.getParentNode() instanceof JstMethod){
 					node = node.getParentNode();
 				}
 				node = node.getParentNode();
 			} else if (node instanceof ObjLiteral) {
 
 				ObjLiteral objectLiteral = (ObjLiteral) node;
 				if(objectLiteral.getParentNode() instanceof AssignExpr){
 					String objLitName = ((AssignExpr) objectLiteral.getParentNode())
 							.getLHS().toLHSText();
 					dq.push(objLitName);
 					node = node.getParentNode();
				}else{
					node = null;
 				}
 			} else {
 				node = node.getParentNode();
 			}
 		}
 
 		Iterator<String> it = dq.iterator();
 		ModelElement element = startingBlock;
 		while (it.hasNext()) {
 
 			String string = it.next();
 
 			if (startingBlock instanceof VjoSourceType) {
 				// IMethod[] methods = startingBlock.getMethods();
 				// for (IMethod method : methods) {
 				// if (method.getElementName().equals(string)) {
 				// elem = (ModelElement) method;
 				// break;
 				//
 				// }
 				// }
 			} else {
 
 				IModelElement[] children = element.getChildren();
 				if (children.length == 0) {
 					return element;
 				}
 
 				for (int i = 0; i < children.length; i++) {
 					if (children[i].getElementName().equals(string)) {
 						element = ((ModelElement) children[i]);
 						break;
 
 					}
 				}
 			}
 		}
 
 		return element;
 	}
 
 	public static JstBlock findDeclaringBlock(IJstNode node) {
 		while (node != null && !(node instanceof IJstType)) {
 			if (node instanceof JstBlock)
 				return (JstBlock) node;
 			else
 				node = node.getParentNode();
 		}
 
 		return null;
 	}
 
 	/**
 	 * True if the identifier is in local variable declartion, false otherwise.
 	 * 
 	 * @param identifier
 	 * @return
 	 */
 	public static boolean isLocalVarDeclaration(JstIdentifier identifier) {
 		if (identifier == null) {
 			return false;
 		}
 		boolean isParentAssignExpr = identifier.getParentNode() instanceof AssignExpr;
 		if (!isParentAssignExpr)
 			return false;
 
 		boolean isGradParentJstVars = identifier.getParentNode()
 				.getParentNode() instanceof JstVars;
 		if (!isGradParentJstVars)
 			return false;
 
 		return true;
 	}
 
 	/**
 	 * Get the name of the first variable in jst vars.
 	 * 
 	 * @param jstVars
 	 * @return
 	 */
 	public static String getFirstVariableName(JstVars jstVars) {
 		if (jstVars == null) {
 			return "";
 		}
 		if (jstVars.getChildren().toArray()[1] instanceof AssignExpr) {
 			AssignExpr assignExpr = (AssignExpr) jstVars.getChildren()
 					.toArray()[1];
 
 			if (assignExpr.getChildren().toArray()[0] instanceof JstIdentifier) {
 				JstIdentifier identifier = (JstIdentifier) assignExpr
 						.getChildren().toArray()[0];
 				return identifier.getName();
 			}
 		}
 		return "";
 	}
 
 	/**
 	 * @param vjoSourceModule
 	 * @return Actual group name from VjoSourceModule
 	 */
 	public static String getGroupName(VjoSourceModule vjoSourceModule) {
 		String tName = vjoSourceModule.getGroupName();
 		TypeName typeName = vjoSourceModule.getTypeName();
 		if (typeName != null) {
 			String temp = typeName.typeName();
 			if (StringUtils.isBlankOrEmpty(temp)) {
 				tName = temp;
 			}
 		}
 		return tName;
 	}
 
 	// for jst node to dltk model or reverse
 	/**
 	 * Find the corresponding dltk type by the gvien jst type.
 	 * 
 	 * @param type
 	 * @return
 	 */
 	public static IType convert2DLTKType(IJstType type) {
 		if (type == null) {
 			return null;
 		}
 		IModelElement element = JstNodeDLTKElementResolver.convert(null, type)[0];
 		if (IModelElement.TYPE == element.getElementType()) {
 			return (IType) element;
 		}
 		return null;
 	}
 
 	/**
 	 * Get the root type of the given vjo source module.
 	 * 
 	 * @param typeSpace
 	 * @param module
 	 * @return IJstType
 	 */
 	public static IJstType getJstType(TypeSpace<IJstType, IJstNode> typeSpace,
 			IVjoSourceModule module) {
 		return new JstQueryExecutor(typeSpace).findType(module.getTypeName());
 	}
 
 	/**
 	 * Get the root type of the given vjo source module.
 	 * 
 	 * @param module
 	 * @return
 	 */
 	public static IJstType getJstType(IVjoSourceModule module) {
 		ITypeSpace<IJstType, IJstNode> typeSpace = TypeSpaceMgr.getInstance()
 				.getTypeSpace();
 		return getJstType((TypeSpace<IJstType, IJstNode>) typeSpace, module);
 	}
 
 	/**
 	 * Check if the file is a js file
 	 * 
 	 * @param fileName
 	 * @return
 	 */
 	public static boolean isVjetFileName(String fileName) {
 		return !StringUtils.isBlankOrEmpty(fileName)
 				&& fileName.endsWith(SUFFIX_VJO);
 	}
 
 	/**
 	 * Answer if the element is in project's build path
 	 * 
 	 * @param element
 	 * @return
 	 */
 	public static boolean isModuleInBuildPath(IModelElement element) {
 		IScriptProject project = element.getScriptProject();
 		if (project == null || !project.exists()) {
 			return false;
 		}
 		return project.isOnBuildpath(element);
 	}
 
 	public static String calculateRtnType(IJstMethod method) {
 		String defaultS = "void";
 		JstBlock block = method.getBlock();
 		if (block != null) {
 			List<BaseJstNode> list = block.getChildren();
 			if (list != null && !list.isEmpty()) {
 				BaseJstNode node = list.get(list.size() - 1);
 				if (node instanceof RtnStmt) {
 					RtnStmt rs = (RtnStmt) node;
 					IExpr expr = rs.getExpression();
 					if (expr != null) {
 						IJstType type = expr.getResultType();
 						if (type != null) {
 							defaultS = CodeCompletionUtils.getAliasOrTypeName(
 									method.getOwnerType(), type);
 						}
 					}
 				}
 			}
 		}
 		return defaultS;
 	}
 
 	public static IModelElement findDeclaringObjectLiteralFieldChain(
 			ModelElement modelElement, IJstNode node) throws ModelException {
 		
 		Deque<String> dq = new ArrayDeque<String>();
 		while (node != null && !(node instanceof IJstType)) {
 			if (node instanceof JstMethod && !(node instanceof JstProxyMethod)) {
 				break;
 			} else if (node instanceof JstProperty) {
 				dq.push(((JstProperty) node).getName().getName());
 				node = node.getParentNode();
 			} else if (node instanceof JstObjectLiteralType) {
 				dq.push(((JstObjectLiteralType)node).getSimpleName());
 			} else if (node instanceof ObjLiteral) {
 
 				ObjLiteral objectLiteral = (ObjLiteral) node;
 				
 				if(objectLiteral.getParentNode() instanceof AssignExpr){
 					String objLitName = ((AssignExpr) objectLiteral.getParentNode())
 							.getLHS().toLHSText();
 					dq.push(objLitName);
 					
 				}
 				
 				node = node.getParentNode();
 			} else if(node instanceof NV){
 				dq.push(((NV)node).getName());
 				node = node.getParentNode();
 			} else {
 				node = node.getParentNode();
 			}
 		}
 
 		Iterator<String> it = dq.iterator();
 		while (it.hasNext()) {
 
 			String string = it.next();
 			
 		IModelElement[] children = modelElement.getChildren();
 		if(children.length==0){
 			return modelElement;
 		}
 		for (int i = 0; i < children.length; i++) {
 			if (IModelElement.FIELD == children[i].getElementType()
 					&& children[i].getElementName().equals(string)) {
 				modelElement = ((JSSourceField) children[i]);
 				break;
 				
 				
 			}
 		}
 		}
 		return modelElement;
 
 	}
 
 }
