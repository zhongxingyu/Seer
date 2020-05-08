 /* ValaProjectBuilder.java
  *
  * Copyright (C) 2008  Johann Prieur <johann.prieur@gmail.com>
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  */
 package valable.builder;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.preference.IPreferenceStore;
 
 import valable.ValaPlugin;
 import valable.job.ValaBuildJob;
 import valable.preferences.PreferenceConstants;
 
 public class ValaProjectBuilder extends IncrementalProjectBuilder {
 
 	public static final String ID = "valable.builder.ValaProjectBuilder";
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int,
 	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
 	 */
	@SuppressWarnings("unchecked")
 	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
 			throws CoreException {
 
 		IResourceDelta delta = null;
 		
 		if (kind == AUTO_BUILD || kind == INCREMENTAL_BUILD)
 			delta = getDelta(getProject());
 
 		fullBuild(monitor, delta);
 
 		return new IProject[0];
 	}
 
 	/**
 	 * Performs a full build of the project, reporting to <code>monitor</code>.
 	 * 
 	 * @param monitor
 	 * @throws CoreException
 	 */
 	private void fullBuild(IProgressMonitor monitor, IResourceDelta delta) throws CoreException {
 		Set<IFile> filesToCompile = new HashSet<IFile>();
 
 		monitor.beginTask("ValaProjectBuilding", IProgressMonitor.UNKNOWN);
 		IResource[] members = getProject().members();
 
 		for (IResource resource : members) {
 			if (resource.getType() == IResource.FILE) {
 				addValaCompilerCompliantFile((IFile) resource, filesToCompile, delta);
 			} else if (resource.getType() == IResource.FOLDER) {
 				for (IFile file : getFileChildren((IFolder) resource)) {
 					addValaCompilerCompliantFile(file, filesToCompile, delta);
 				}
 			}
 		}
 
 		IPreferenceStore store = ValaPlugin.getDefault().getPreferenceStore();
 		IFolder folder = getProject().getFolder(
 				store.getString(PreferenceConstants.P_OUTPUT_FOLDER));
 		if (!folder.exists()) {
 			folder.create(true, true, monitor);
 		}
 
 		String valac  = store.getString(PreferenceConstants.P_VALAC_EXE);
 		String vapi   = store.getString(PreferenceConstants.P_VAPI_PATH);
 		String output = folder.getLocation().toString();
 
 		ValaBuildJob job = new ValaBuildJob(filesToCompile, valac, vapi, output);
 		job.schedule();
 
 		monitor.done();
 	}
 
 	/**
 	 * Adds the given <code>file</code> resource to the given list only if its
 	 * extension complies with the possible inputs for the valac compiler and
 	 * it was affected by a given delta.
 	 * 
 	 * @param file
 	 *            the file resource
 	 * @param files
 	 *            the list in which the file should be added
 	 * @param delta
 	 *            specifies which files have been changed, or <var>null</var> if all.
 	 */
 	private static void addValaCompilerCompliantFile(IFile file,
 			Collection<IFile> files, IResourceDelta delta) {
 		if (file.getName().endsWith(".vala")
 				|| file.getName().endsWith(".vapi")
 				|| file.getName().endsWith(".c")) {
 			if (delta == null || delta.findMember(file.getProjectRelativePath()) != null)
 				files.add((IFile) file);
 		}
 	}
 
 	/**
 	 * Computes a list of all children files in a folder.
 	 * 
 	 * @param folder
 	 *            the folder resource
 	 * @return the list of children files
 	 */
 	private static List<IFile> getFileChildren(IFolder folder) {
 		final ArrayList<IFile> files = new ArrayList<IFile>();
 		try {
 			folder.accept(new IResourceVisitor() {
 
 				public boolean visit(IResource resource) {
 					if (resource instanceof IFile) {
 						files.add((IFile) resource);
 						return false;
 					}
 					return true;
 				}
 
 			});
 		} catch (CoreException e) {
 			throw new RuntimeException(e);
 		}
 		return files;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#clean(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	protected void clean(IProgressMonitor monitor) throws CoreException {
 		// Cleaning consists in deleting the content of the output folder
 		IPreferenceStore store = ValaPlugin.getDefault().getPreferenceStore();
 		String output = store.getString(PreferenceConstants.P_OUTPUT_FOLDER);
 
 		IFolder folder = getProject().getFolder(output);
 		if (!folder.exists()) {
 			return;
 		}
 
 		for (IResource resource : folder.members()) {
 			resource.delete(true, monitor);
 		}
 	}
 
 }
