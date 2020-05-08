 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.core.search.indexing.core;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementVisitor;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.core.search.index.Index;
 import org.eclipse.dltk.core.search.indexing.IProjectIndexer;
 import org.eclipse.dltk.core.search.indexing.ReadWriteMonitor;
 import org.eclipse.dltk.internal.core.BuiltinSourceModule;
 import org.eclipse.dltk.internal.core.ExternalSourceModule;
 import org.eclipse.dltk.internal.core.ModelManager;
 
 public class ExternalProjectFragmentRequest extends IndexRequest {
 
 	protected final IProjectFragment fragment;
 	protected final IDLTKLanguageToolkit toolkit;
 
 	public ExternalProjectFragmentRequest(IProjectIndexer indexer,
 			IProjectFragment fragment, IDLTKLanguageToolkit toolkit) {
 		super(indexer);
 		this.fragment = fragment;
 		this.toolkit = toolkit;
 	}
 
 	protected String getName() {
 		return fragment.getElementName();
 	}
 
 	protected void run() throws CoreException, IOException {
 		IEnvironment environment = EnvironmentManager.getEnvironment(fragment
 				.getScriptProject());
 		if (environment == null || !environment.connect()) {
 			return;
 		}
 		final Set<ISourceModule> modules = getExternalSourceModules();
 		final Index index = getIndexer().getProjectFragmentIndex(fragment);
 		if (index == null) {
 			return;
 		}
 		final IPath containerPath = fragment.getPath();
 		Set<IFileHandle> parentFolders = new HashSet<IFileHandle>();
 		final List<Object> changes = checkChanges(index, modules,
 				containerPath, getEnvironment(), parentFolders);
 		if (DEBUG) {
 			log("changes.size=" + changes.size()); //$NON-NLS-1$
 		}
 		if (changes.isEmpty()) {
 			return;
 		}
 		final ReadWriteMonitor imon = index.monitor;
 		imon.enterWrite();
 		try {
 			for (Iterator<Object> i = changes.iterator(); !isCancelled
 					&& i.hasNext();) {
 				final Object change = i.next();
 				if (change instanceof String) {
 					index.remove((String) change);
 				} else if (change instanceof ISourceModule) {
 					ISourceModule module = (ISourceModule) change;
 					IFileHandle file = EnvironmentPathUtils.getFile(module,
 							false);
 					if (file != null && changes.size() > 1) {
 						IFileHandle parentHandle = file.getParent();
						if (parentFolders.add(parentHandle.getParent())) {
 							ModelManager.getModelManager().getCoreCache()
 									.updateFolderTimestamps(parentHandle);
 						}
 					}
 					getIndexer().indexSourceModule(index, toolkit, module,
 							containerPath);
 				}
 			}
 
 		} catch (Throwable e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				index.save();
 			} catch (IOException e) {
 				DLTKCore.error("error saving index", e); //$NON-NLS-1$
 			} finally {
 				imon.exitWrite();
 			}
 		}
 	}
 
 	protected IEnvironment getEnvironment() {
 		return EnvironmentManager.getEnvironment(fragment);
 	}
 
 	static class ExternalModuleVisitor implements IModelElementVisitor {
 		final Set<ISourceModule> modules = new HashSet<ISourceModule>();
 
 		public boolean visit(IModelElement element) {
 			if (element.getElementType() == IModelElement.SOURCE_MODULE) {
 				if (element instanceof ExternalSourceModule
 						|| element instanceof BuiltinSourceModule) {
 					modules.add((ISourceModule) element);
 				}
 				return false;
 			}
 			return true;
 		}
 	}
 
 	private Set<ISourceModule> getExternalSourceModules() throws ModelException {
 		final ExternalModuleVisitor visitor = new ExternalModuleVisitor();
 		fragment.accept(visitor);
 		return visitor.modules;
 	}
 
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((fragment == null) ? 0 : fragment.hashCode());
 		return result;
 	}
 
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		ExternalProjectFragmentRequest other = (ExternalProjectFragmentRequest) obj;
 		if (fragment == null) {
 			if (other.fragment != null)
 				return false;
 		} else if (!fragment.equals(other.fragment))
 			return false;
 		return true;
 	}
 }
