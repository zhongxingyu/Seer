 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelProvider;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.internal.core.util.MementoTokenizer;
 import org.eclipse.dltk.internal.core.util.Messages;
 import org.eclipse.dltk.internal.core.util.Util;
 import org.eclipse.dltk.utils.CorePrinter;
 
 public class ScriptFolder extends Openable implements IScriptFolder {
 
 	protected final IPath path;
 	private final String elementName;
 
 	protected ScriptFolder(ModelElement parent, IPath path) {
 		super(parent);
 		this.path = path;
 
 		elementName = folderPathToString(path);
 	}
 
 	private static String folderPathToString(IPath path) {
 		final int segmentCount = path.segmentCount();
 		if (segmentCount == 0) {
 			return org.eclipse.dltk.compiler.util.Util.EMPTY_STRING;
 		} else if (segmentCount == 1) {
 			return path.segment(0);
 		} else {
 			int resultSize = (segmentCount - 1)
 			/* x PACKAGE_DELIMETER_STR.length() */;
 			for (int i = 0; i < segmentCount; ++i) {
 				resultSize += path.segment(i).length();
 			}
 			char[] result = new char[resultSize];
 			int index = 0;
 			for (int i = 0; i < segmentCount; ++i) {
 				if (i != 0) {
 					result[index++] = PACKAGE_DELIMITER;
 				}
 				final String segment = path.segment(i);
 				segment.getChars(0, segment.length(), result, index);
 				index += segment.length();
 			}
 			return new String(result);
 		}
 	}
 
 	/**
 	 * @see ModelElement
 	 */
 	protected Object createElementInfo() {
 		return new ScriptFolderInfo();
 	}
 
 	public int getElementType() {
 		return SCRIPT_FOLDER;
 	}
 
 	/**
 	 * @see IModelElement#getPath()
 	 */
 	public IPath getPath() {
 		IProjectFragment root = this.getProjectFragment();
 		if (root.isArchive()) {
 			return root.getPath();
 		} else {
 			return root.getPath().append(path);
 		}
 	}
 
 	/**
 	 * @see IModelElement#getResource()
 	 */
 	public IResource getResource() {
 		IProjectFragment root = this.getProjectFragment();
 		if (root.isArchive()) {
 			return root.getResource();
 		} else {
 			if (path.segmentCount() == 0)
 				return root.getResource();
 			IContainer container = (IContainer) root.getResource();
 			if (container != null) {
 				return container.getFolder(path);
 			}
 			return null;
 		}
 	}
 
 	public boolean equals(Object o) {
 		if (this == o)
 			return true;
 		if (!(o instanceof ScriptFolder))
 			return false;
 
 		ScriptFolder other = (ScriptFolder) o;
 		return this.path.equals(other.path) && this.parent.equals(other.parent);
 	}
 
 	public int hashCode() {
 		return Util.combineHashCodes(parent.hashCode(), path.hashCode());
 	}
 
 	public int getKind() throws ModelException {
 		return getProjectFragment().getKind();
 	}
 
 	protected boolean buildStructure(OpenableElementInfo info,
 			IProgressMonitor pm, Map newElements, IResource underlyingResource)
 			throws ModelException {
 		// check whether this folder can be opened
 		if (!underlyingResource.isAccessible())
 			throw newNotPresentException();
 
 		int kind = getKind();
 
 		if (kind == IProjectFragment.K_SOURCE && Util.isExcluded(this))
 			throw newNotPresentException();
 
 		// add modules from resources
 		HashSet vChildren = new HashSet();
 		try {
 			IProjectFragment root = getProjectFragment();
 			IResource[] members = ((IContainer) underlyingResource).members();
 			for (int i = 0, max = members.length; i < max; i++) {
 				IResource child = members[i];
 				if (child.getType() != IResource.FOLDER
 						&& !Util.isExcluded(child, root)) {
 					IModelElement childElement;
 					if (kind == IProjectFragment.K_SOURCE
 							&& Util.isValidSourceModule(this, child)) {
 						childElement = getSourceModule(child.getName());
 						vChildren.add(childElement);
 					}
 				}
 			}
 		} catch (CoreException e) {
 			throw new ModelException(e);
 		}
 
 		if (kind == IProjectFragment.K_SOURCE) {
 			// add primary source modules
 			ISourceModule[] primarySourceModules = getSourceModules(DefaultWorkingCopyOwner.PRIMARY);
 			for (int i = 0, length = primarySourceModules.length; i < length; i++) {
 				ISourceModule primary = primarySourceModules[i];
 				vChildren.add(primary);
 			}
 		}
 
 		// IModelElement[] children = new IModelElement[vChildren.size()];
 		// vChildren.toArray(children);
 		List childrenSet = new ArrayList(vChildren);
 		// Call for extra model providers
 		IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 				.getLanguageToolkit(this);
 		IModelProvider[] providers = ModelProviderManager.getProviders(toolkit
 				.getNatureId());
 		if (providers != null) {
 			for (int i = 0; i < providers.length; i++) {
 				providers[i].provideModelChanges(this, childrenSet);
 			}
 		}
 		info.setChildren((IModelElement[]) childrenSet
 				.toArray(new IModelElement[childrenSet.size()]));
 		return true;
 	}
 
 	public ISourceModule[] getSourceModules(WorkingCopyOwner owner) {
 		ISourceModule[] workingCopies = ModelManager.getModelManager()
 				.getWorkingCopies(owner, false/* don't add primary */);
 		if (workingCopies == null)
 			return ModelManager.NO_WORKING_COPY;
 		int length = workingCopies.length;
 		ISourceModule[] result = new ISourceModule[length];
 		int index = 0;
 		for (int i = 0; i < length; i++) {
 			ISourceModule wc = workingCopies[i];
 			IResource res = wc.getResource();
 			boolean valid;
 			if (res != null)
 				valid = Util.isValidSourceModule(this, res);
 			else
 				valid = Util.isValidSourceModule(this, wc.getPath());
 			if (equals(wc.getParent()) && !Util.isExcluded(wc) && valid) {
 				result[index++] = wc;
 			}
 		}
 		if (index != length) {
 			System.arraycopy(result, 0, result = new ISourceModule[index], 0,
 					index);
 		}
 		return result;
 	}
 
 	public ISourceModule getSourceModule(String name) {
 		// We need to check for element providers and if provider are declared
 		// we need to build structure to return correct handle here.
 		IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 				.getLanguageToolkit(this);
 		if (toolkit != null) {
 			IModelProvider[] providers = ModelProviderManager
 					.getProviders(toolkit.getNatureId());
 			if (providers != null) {
 				boolean provides = false;
 				for (int i = 0; i < providers.length; i++) {
 					if (providers[i].isModelChangesProvidedFor(this, name)) {
 						provides = true;
 						break;
 					}
 				}
 				if (provides) {
 					try {
 						IModelElement[] children = getChildren();
 						IPath fullPath = getPath().append(name);
 						for (int i = 0; i < children.length; i++) {
 							IModelElement child = children[i];
 							if (child instanceof IScriptFolder) {
 								IPath childPath = child.getPath();
 								if (fullPath.equals(childPath)) {
 									return (ISourceModule) child;
 								}
 							}
 						}
 					} catch (ModelException e) {
 						DLTKCore.error(
 								"Could not obtain model element childrens.", e);
 					}
 				}
 			}
 		}
 		return new SourceModule(this, name, DefaultWorkingCopyOwner.PRIMARY);
 	}
 
 	/**
 	 * @see IScriptFolder
 	 */
 	public ISourceModule createSourceModule(String cuName, String contents,
 			boolean force, IProgressMonitor monitor) throws ModelException {
 		CreateSourceModuleOperation op = new CreateSourceModuleOperation(this,
 				cuName, contents, force);
 		op.runOperation(monitor);
 		return new SourceModule(this, cuName, DefaultWorkingCopyOwner.PRIMARY);
 	}
 
 	public final IProjectFragment getProjectFragment() {
 		return (IProjectFragment) getParent();
 	}
 
 	/**
 	 * Debugging purposes
 	 */
 	protected void toStringName(StringBuffer buffer) {
 		String elementName = getElementName();
 		if (elementName.length() == 0) {
 			buffer.append("<default>"); //$NON-NLS-1$
 		} else {
 			buffer.append(elementName);
 		}
 	}
 
 	public String getElementName() {
 		return elementName;
 	}
 
 	public boolean isRootFolder() {
 		return path.segmentCount() == 0;
 	}
 
 	public void printNode(CorePrinter output) {
 		output.formatPrint("DLTK Script folder:" + getElementName()); //$NON-NLS-1$
 		output.indent();
 		try {
 			IModelElement modelElements[] = this.getChildren();
 			for (int i = 0; i < modelElements.length; ++i) {
 				IModelElement element = modelElements[i];
 				if (element instanceof ModelElement) {
 					((ModelElement) element).printNode(output);
 				} else {
 					output.print("Unknown element:" + element); //$NON-NLS-1$
 				}
 			}
 		} catch (ModelException ex) {
 			output.formatPrint(ex.getLocalizedMessage());
 		}
 		output.dedent();
 	}
 
 	public ISourceModule[] getSourceModules() throws ModelException {
 		List<IModelElement> list = getChildrenOfType(SOURCE_MODULE);
 		ISourceModule[] array = new ISourceModule[list.size()];
 		list.toArray(array);
 		return array;
 	}
 
 	public Object[] getForeignResources() throws ModelException {
 		if (this.isRootFolder()) {
 			return ModelElementInfo.NO_NON_SCRIPT_RESOURCES;
 		} else {
 			if (getProjectFragment() instanceof IProjectFragment) {
 				return ((ScriptFolderInfo) getElementInfo())
 						.getForeignResources(getResource(),
 								(IProjectFragment) getProjectFragment());
 			}
 			return ModelElementInfo.NO_NON_SCRIPT_RESOURCES;
 		}
 	}
 
 	public boolean hasSubfolders() throws ModelException {
 		IModelElement[] packages = ((IProjectFragment) getParent())
 				.getChildren();
 		int namesLength = this.path.segmentCount();
 		nextPackage: for (int i = 0, length = packages.length; i < length; i++) {
			IPath otherNames = ((ScriptFolder) packages[i]).path;
			if (otherNames.segmentCount() <= namesLength)
				continue nextPackage;
			for (int j = 0; j < namesLength; j++)
				if (!this.path.segment(j).equals(otherNames.segment(j)))
 					continue nextPackage;
			return true;
 		}
 		return false;
 	}
 
 	public IModelElement getHandleFromMemento(String token,
 			MementoTokenizer memento, WorkingCopyOwner owner) {
 		switch (token.charAt(0)) {
 		case JEM_SOURCEMODULE:
 			if (!memento.hasMoreTokens())
 				return this;
 			String classFileName = memento.nextToken();
 			ModelElement classFile = (ModelElement) getSourceModule(classFileName);
 			return classFile.getHandleFromMemento(memento, owner);
 		case JEM_USER_ELEMENT:
 			return MementoModelElementUtil.getHandleFromMemento(memento, this,
 					owner);
 		}
 		return null;
 	}
 
 	protected char getHandleMementoDelimiter() {
 		return JEM_SCRIPTFOLDER;
 	}
 
 	public boolean containsScriptResources() throws ModelException {
 		Object elementInfo = getElementInfo();
 		if (!(elementInfo instanceof ScriptFolderInfo))
 			return false;
 		ScriptFolderInfo scriptElementInfo = (ScriptFolderInfo) elementInfo;
 		return scriptElementInfo.containsScriptResources();
 	}
 
 	public boolean hasChildren() throws ModelException {
 		return getChildren().length > 0;
 	}
 
 	public void copy(IModelElement container, IModelElement sibling,
 			String rename, boolean replace, IProgressMonitor monitor)
 			throws ModelException {
 		if (container == null) {
 			throw new IllegalArgumentException(Messages.operation_nullContainer);
 		}
 		IModelElement[] elements = new IModelElement[] { this };
 		IModelElement[] containers = new IModelElement[] { container };
 		IModelElement[] siblings = null;
 		if (sibling != null) {
 			siblings = new IModelElement[] { sibling };
 		}
 		String[] renamings = null;
 		if (rename != null) {
 			renamings = new String[] { rename };
 		}
 		getModel().copy(elements, containers, siblings, renamings, replace,
 				monitor);
 	}
 
 	public void delete(boolean force, IProgressMonitor monitor)
 			throws ModelException {
 		IModelElement[] elements = new IModelElement[] { this };
 		getModel().delete(elements, force, monitor);
 	}
 
 	public void move(IModelElement container, IModelElement sibling,
 			String rename, boolean replace, IProgressMonitor monitor)
 			throws ModelException {
 		if (container == null) {
 			throw new IllegalArgumentException(Messages.operation_nullContainer);
 		}
 		IModelElement[] elements = new IModelElement[] { this };
 		IModelElement[] containers = new IModelElement[] { container };
 		IModelElement[] siblings = null;
 		if (sibling != null) {
 			siblings = new IModelElement[] { sibling };
 		}
 		String[] renamings = null;
 		if (rename != null) {
 			renamings = new String[] { rename };
 		}
 		getModel().move(elements, containers, siblings, renamings, replace,
 				monitor);
 	}
 
 	public void rename(String newName, boolean force, IProgressMonitor monitor)
 			throws ModelException {
 		if (newName == null) {
 			throw new IllegalArgumentException(Messages.element_nullName);
 		}
 		IModelElement[] elements = new IModelElement[] { this };
 		IModelElement[] dests = new IModelElement[] { this.getParent() };
 		String[] renamings = new String[] { newName };
 		getModel().rename(elements, dests, renamings, force, monitor);
 	}
 
 	public IPath getRelativePath() {
 		return this.path;
 	}
 }
