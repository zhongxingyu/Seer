 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.common.jdt.internal.integration;
 
 
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.internal.resources.Workspace;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.wst.common.frameworks.internal.ISaveHandler;
 import org.eclipse.wst.common.frameworks.internal.SaveFailedException;
 import org.eclipse.wst.common.frameworks.internal.SaveHandlerHeadless;
 import org.eclipse.wst.common.frameworks.internal.SaveHandlerRegister;
 import org.eclispe.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 
 /**
  * Insert the type's description here. Creation date: (4/27/2001 4:14:30 PM)
  * 
  * @author: Administrator
  */
 public class WTPWorkingCopyManager implements WorkingCopyManager {
 
 	//New CUs that will need to be deleted upon dispose
 	private List originalNewCompilationUnits;
 	//New CUs that were created that need saved immediately (after each gen)
 	private List needsSavingCompilationUnits;
 	//A complete list of new CUs that is only cleared on save and dispose
 	private List newCompilationUnits;
 	private HashMap deletedCompilationUnits;
 	protected static final Class IRESOURCE_CLASS = IResource.class;
 
 	/**
 	 * WTPWorkingCopyManager constructor comment.
 	 */
 	public WTPWorkingCopyManager() {
 		super();
 	}
 
 	protected void addDeletedCompilationUnit(ICompilationUnit cu) {
 		getNeedsSavingCompilationUnits().remove(cu);
 		if (!getOriginalNewCompilationUnits().contains(cu) && !getDeletedCompilationUnits().containsKey(cu))
 			primAddDeletedCompilationUnit(cu);
 		getOriginalNewCompilationUnits().remove(cu);
 	}
 
 	protected void addNewCompilationUnit(ICompilationUnit cu, ICompilationUnit workingCopy) {
 		getNewCompilationUnits().add(cu);
 		getNeedsSavingCompilationUnits().add(workingCopy);
 		if (!getDeletedCompilationUnits().containsKey(cu))
 			getOriginalNewCompilationUnits().add(cu);
 	}
 
 	/**
 	 * This will save all of the new CompilationUnits to be saved.
 	 */
 	protected void commitWorkingCopy(ICompilationUnit wc, IProgressMonitor monitor) {
 		try {
 			try {
 				wc.commitWorkingCopy(false, monitor);
 			} catch (JavaModelException e) {
 				if (isFailedWriteFileFailure(e) && shouldSaveReadOnly(wc))
 					wc.commitWorkingCopy(false, monitor);
 				else
 					throw e;
 			}
 		} catch (JavaModelException e) {
 			org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(e);
 			throw new SaveFailedException(e);
 		} finally {
 			try {
 				wc.discardWorkingCopy();
 			} catch (JavaModelException e1) {
 				org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(e1);
 				throw new SaveFailedException(e1);
 			}
 		}
 	}
 
 	/**
 	 * This will delete
 	 * 
 	 * @cu from the workbench and fix the internal references for this working copy manager.
 	 */
 	public void delete(ICompilationUnit cu, IProgressMonitor monitor) {
 		if (cu.isWorkingCopy())
 			cu = cu.getPrimary();
 		addDeletedCompilationUnit(cu);
 		try {
 			cu.delete(false, monitor);
 		} catch (JavaModelException e) {
 			if (e.getStatus().getCode() != org.eclipse.jdt.core.IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST)
 				org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(e);
 		}
 	}
 
 	protected void discardOriginalNewCompilationUnits() {
 		if (getOriginalNewCompilationUnits().isEmpty())
 			return;
 		List cus = getOriginalNewCompilationUnits();
 		ICompilationUnit cu;
 		ICompilationUnit wc = null;
 		for (int i = 0; i < cus.size(); i++) {
 			cu = (ICompilationUnit) cus.get(i);
 			if (cu.isWorkingCopy()) {
 				wc = cu;
 				cu = wc.getPrimary();
 			}
 			primDelete(cu);
 			if (wc != null)
 				try {
 					wc.discardWorkingCopy();
 				} catch (JavaModelException e) {
 					Logger.getLogger().logError(e);
 				}
 		}
 	}
 
 	public void dispose() {
 		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 			public void run(IProgressMonitor aMonitor) {
 				primDispose();
 			}
 		};
 		runOperation(runnable, null, true);
 	}
 
 	public void revert() {
 		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 			public void run(IProgressMonitor aMonitor) {
 				primRevert();
 			}
 		};
 		runOperation(runnable, null, true);
 	}
 
 	public Set getAffectedFiles() {
 		return Collections.EMPTY_SET;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (7/11/2001 6:43:37 PM)
 	 * 
 	 * @return java.util.HashMap
 	 */
 	protected HashMap getDeletedCompilationUnits() {
 		if (deletedCompilationUnits == null)
 			deletedCompilationUnits = new HashMap();
 		return deletedCompilationUnits;
 	}
 
 	/**
 	 * Returns the working copy remembered for the compilation unit encoded in the given editor
 	 * input. Does not connect the edit model to the working copy.
 	 * 
 	 * @param input
 	 *            ICompilationUnit
 	 * @return the working copy of the compilation unit, or <code>null</code> if the input does
 	 *         not encode an editor input, or if there is no remembered working copy for this
 	 *         compilation unit
 	 */
 	public org.eclipse.jdt.core.ICompilationUnit getExistingWorkingCopy(ICompilationUnit cu) throws CoreException {
 		ICompilationUnit newCU = getNewCompilationUnitWorkingCopy(cu);
 		if (newCU != null)
 			return newCU;
 		return null;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (7/19/2001 11:00:19 AM)
 	 * 
 	 * @return java.util.List
 	 */
 	protected java.util.List getNeedsSavingCompilationUnits() {
 		if (needsSavingCompilationUnits == null)
 			needsSavingCompilationUnits = new ArrayList();
 		return needsSavingCompilationUnits;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (4/26/2001 3:49:05 PM)
 	 * 
 	 * @return java.util.List
 	 */
 	protected java.util.List getNewCompilationUnits() {
 		if (newCompilationUnits == null)
 			newCompilationUnits = new ArrayList();
 		return newCompilationUnits;
 	}
 
 	/**
 	 * It is possible that we have already created this CompilationUnit and its working copy. If
 	 * this is the case, return our new working copy and do not create a new one.
 	 */
 	protected ICompilationUnit getNewCompilationUnitWorkingCopy(ICompilationUnit cu) {
 		if (hasNewCompilationUnit(cu)) {
 			List list = getNeedsSavingCompilationUnits();
 			ICompilationUnit copy;
 			for (int i = 0; i < list.size(); i++) {
 				copy = (ICompilationUnit) list.get(i);
 				if (cu.equals(copy.getPrimary()))
 					return copy;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (4/26/2001 3:49:05 PM)
 	 * 
 	 * @return java.util.List
 	 */
 	protected java.util.List getOriginalNewCompilationUnits() {
 		if (originalNewCompilationUnits == null)
 			originalNewCompilationUnits = new ArrayList();
 		return originalNewCompilationUnits;
 	}
 
 	/**
 	 * Return the IPackageFragment for the given ICompilationUnit.
 	 */
 	protected IPackageFragment getPackageFragment(ICompilationUnit cu) {
 		if (cu == null)
 			return null;
 		IJavaElement parent = cu;
 		int elementType = cu.getElementType();
 		while (parent != null && elementType != IJavaElement.PACKAGE_FRAGMENT) {
 			parent = parent.getParent();
 			if (parent != null)
 				elementType = parent.getElementType();
 			else
 				elementType = -1;
 		}
 		return (IPackageFragment) parent;
 	}
 
 	protected ISaveHandler getSaveHandler() {
 		return SaveHandlerRegister.getSaveHandler();
 	}
 
 	/**
 	 * Returns the working copy remembered for the compilation unit.
 	 * 
 	 * @param input
 	 *            ICompilationUnit
 	 * @return the working copy of the compilation unit, or <code>null</code> if there is no
 	 *         remembered working copy for this compilation unit
 	 */
 	public ICompilationUnit getWorkingCopy(ICompilationUnit cu, boolean forNewCU) throws org.eclipse.core.runtime.CoreException {
 		if (cu == null || cu.isWorkingCopy())
 			return cu;
 		ICompilationUnit newCU = getNewCompilationUnitWorkingCopy(cu);
 		if (newCU != null)
 			return newCU;
 		ICompilationUnit workingCopy = cu.getWorkingCopy(null);
 		addNewCompilationUnit(cu, workingCopy);
 		return workingCopy;
 	}
 
 	/**
 	 * Has a new compilation unit already been created.
 	 */
 	protected boolean hasNewCompilationUnit(ICompilationUnit cu) {
 		return getNewCompilationUnits().contains(cu);
 	}
 
 	protected boolean isFailedWriteFileFailure(Exception ex) {
 		return SaveHandlerHeadless.isFailedWriteFileFailure(ex);
 	}
 
 	protected void primAddDeletedCompilationUnit(ICompilationUnit cu) {
 		if (cu == null)
 			return;
 		Object[] info = new Object[2];
 		info[0] = getPackageFragment(cu);
 		try {
 			info[1] = cu.getSource();
 		} catch (JavaModelException e) {
 			info[1] = null;
 		}
 		getDeletedCompilationUnits().put(cu, info);
 	}
 
 	// This is an internal delete call.
 	protected void primDelete(ICompilationUnit cu) {
 		try {
 			if (cu.exists())
 				cu.delete(true, new org.eclipse.core.runtime.NullProgressMonitor());
 		} catch (JavaModelException e) {
 			org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(e);
 			//What to do here?
 		}
 	}
 
 	protected void primDispose() {
 		discardOriginalNewCompilationUnits();
 		reviveDeletedCompilationUnits();
 		newCompilationUnits = null;
 		needsSavingCompilationUnits = null;
 		originalNewCompilationUnits = null;
 		deletedCompilationUnits = null;
 	}
 
 	protected void primRevert() {
 		discardOriginalNewCompilationUnits();
 		reviveDeletedCompilationUnits();
 		newCompilationUnits = null;
 		needsSavingCompilationUnits = null;
 		originalNewCompilationUnits = null;
 		deletedCompilationUnits = null;
 	}
 
 	/**
 	 * Returns the working copy remembered for the compilation unit encoded in the given editor
 	 * input.
 	 * 
 	 * @param input
 	 *            ICompilationUnit
 	 * @return the working copy of the compilation unit, or <code>null</code> if the input does
 	 *         not encode an editor input, or if there is no remembered working copy for this
 	 *         compilation unit
 	 */
	protected ICompilationUnit primGetWorkingCopy(ICompilationUnit cu) throws CoreException {
 		return null;
 	}
 
 	/**
 	 * This will save all of the referenced CompilationUnits to be saved.
 	 */
 	protected void primSaveCompilationUnits(org.eclipse.core.runtime.IProgressMonitor monitor) {
 		saveNewCompilationUnits(monitor);
 		getDeletedCompilationUnits().clear();
 	}
 
 	/**
 	 * This will save all of the new CompilationUnits to be saved.
 	 */
 	protected void primSaveOnlyNewCompilationUnits(org.eclipse.core.runtime.IProgressMonitor monitor) {
 		List cus = getNeedsSavingCompilationUnits();
 		ICompilationUnit wc;
 		for (int i = 0; i < cus.size(); i++) {
 			wc = (ICompilationUnit) cus.get(i);
 			commitWorkingCopy(wc, monitor);
 		}
 		cus.clear();
 	}
 
 	protected void removeDeletedCompilationUnit(ICompilationUnit cu) {
 		if (getDeletedCompilationUnits().remove(cu) != null) {
 			if (cu.isWorkingCopy()) {
 				ICompilationUnit original, nextCU, testCU;
 				original = cu.getPrimary();
 				Set cus = getDeletedCompilationUnits().keySet();
 				Iterator it = cus.iterator();
 				while (it.hasNext()) {
 					nextCU = (ICompilationUnit) it.next();
 					testCU = nextCU.isWorkingCopy() ? (ICompilationUnit) nextCU.getPrimary() : nextCU;
 					if (testCU.equals(original)) {
 						cus.remove(nextCU);
 						return;
 					}
 				}
 			}
 		}
 	}
 
 	protected void reviveDeletedCompilationUnit(ICompilationUnit cu, Object[] info, IProgressMonitor pm) {
 		if (info[0] != null && info[1] != null) {
 			String typeName = cu.getElementName();
 			IPackageFragment pack = (IPackageFragment) info[0];
 			String source = (String) info[1];
 			try {
 				ICompilationUnit existingCU = pack.getCompilationUnit(typeName);
 				if (existingCU.exists() && getNewCompilationUnits().contains(existingCU))
 					existingCU.delete(false, pm);
 				pack.createCompilationUnit(typeName, source, false, pm);
 			} catch (JavaModelException e) {
 				org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(e);
 			}
 		}
 	}
 
 	protected void reviveDeletedCompilationUnits() {
 		if (getDeletedCompilationUnits().isEmpty())
 			return;
 		IProgressMonitor pm = new org.eclipse.core.runtime.NullProgressMonitor();
 		Iterator it = getDeletedCompilationUnits().entrySet().iterator();
 		Map.Entry entry;
 		ICompilationUnit cu;
 		Object[] info;
 		while (it.hasNext()) {
 			entry = (Map.Entry) it.next();
 			cu = (ICompilationUnit) entry.getKey();
 			info = (Object[]) entry.getValue();
 			reviveDeletedCompilationUnit(cu, info, pm);
 		}
 
 	}
 
 	protected void runOperation(IWorkspaceRunnable aRunnable, IProgressMonitor monitor, boolean validate) {
 		primRunOperation(aRunnable, monitor);
 
 		// TODO Break the validator depedency
 		//	if (validate)
 		//		primRunOperation(aRunnable, monitor);
 		//	else {
 		//		IProject proj = getValidationProject();
 		//			
 		//		ValidatorManager mgr = ValidatorManager.getManager();
 		//		boolean disableValidators = proj != null;
 		//		boolean wasSuspended = false;
 		//		if (disableValidators) {
 		//			wasSuspended = mgr.isSuspended(proj);
 		//			if (!wasSuspended)
 		//				mgr.suspendValidation(proj, true);
 		//		}
 		//		try {
 		//			primRunOperation(aRunnable, monitor);
 		//		} finally {
 		//			if (disableValidators && !wasSuspended)
 		//				mgr.suspendValidation(proj, false);
 		//		}
 		//	}
 	}
 
 	protected void primRunOperation(IWorkspaceRunnable aRunnable, IProgressMonitor monitor) {
 		Workspace workspace = (Workspace) WTPCommonPlugin.getWorkspace();
 		if (aRunnable != null) {
 			//if (workspace.isTreeLocked())
 			//Logger.getLogger().logTrace(ResourceHandler.getString("Cannot_run_J2EEUIWorkingCo_ERROR_"));
 			// //$NON-NLS-1$ = "Cannot run J2EEUIWorkingCopyManager operation because the Workspace
 			// tree is locked."
 			//else {
 			if (!workspace.isTreeLocked()) {
 				try {
 					WTPCommonPlugin.getWorkspace().run(aRunnable, monitor);
 				} catch (CoreException e) {
 					throw new SaveFailedException(e);
 				}
 			}
 		}
 	}
 
 	/**
 	 * This will save all of the referenced CompilationUnits to be saved.
 	 */
 	public void saveCompilationUnits(org.eclipse.core.runtime.IProgressMonitor monitor) {
 		getSaveHandler().access();
 		try {
 			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 				public void run(IProgressMonitor aMonitor) {
 					primSaveCompilationUnits(aMonitor);
 				}
 			};
 			runOperation(runnable, monitor, true);
 		} catch (SaveFailedException ex) {
 			getSaveHandler().handleSaveFailed(ex, monitor);
 		} finally {
 			getSaveHandler().release();
 		}
 	}
 
 	/**
 	 * This will save all of the referenced CompilationUnits to be saved.
 	 */
 	protected void saveNewCompilationUnits(IProgressMonitor monitor) {
 		primSaveOnlyNewCompilationUnits(monitor);
 		getOriginalNewCompilationUnits().clear();
 		getNewCompilationUnits().clear();
 	}
 
 	/**
 	 * This will save all of the new CompilationUnits to be saved.
 	 */
 	public void saveOnlyNewCompilationUnits(org.eclipse.core.runtime.IProgressMonitor monitor) {
 		getSaveHandler().access();
 		try {
 			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 				public void run(IProgressMonitor aMonitor) {
 					primSaveOnlyNewCompilationUnits(aMonitor);
 				}
 			};
 			runOperation(runnable, monitor, false);
 		} catch (SaveFailedException ex) {
 			getSaveHandler().handleSaveFailed(ex, monitor);
 		} finally {
 			getSaveHandler().release();
 		}
 	}
 
 	protected boolean shouldSaveReadOnly(ICompilationUnit wc) {
 		IResource resource = null;
 
 		resource = (IResource) wc.getPrimary().getAdapter(IRESOURCE_CLASS);
 
 		if (resource == null || resource.getType() != IResource.FILE || !resource.isReadOnly())
 			return false;
 
 		return getSaveHandler().shouldContinueAndMakeFileEditable((IFile) resource);
 	}
 
 	/**
 	 * @see com.ibm.etools.j2ee.workbench.IJ2EEWorkingCopyManager#hasWorkingCopies()
 	 */
 	public boolean hasWorkingCopies() {
 		return (deletedCompilationUnits != null && !deletedCompilationUnits.isEmpty()) || (needsSavingCompilationUnits != null && !needsSavingCompilationUnits.isEmpty()) || (newCompilationUnits != null && !newCompilationUnits.isEmpty());
 	}
 
 }
