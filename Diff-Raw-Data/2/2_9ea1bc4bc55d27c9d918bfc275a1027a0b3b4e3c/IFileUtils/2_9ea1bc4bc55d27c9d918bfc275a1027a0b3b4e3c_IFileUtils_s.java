 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.common.util.io;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourceAttributes;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 
 public class IFileUtils {
 
 	/**
 	 * Recursively change 
 	 * @param parent
 	 * @param isReadOnly
 	 * @throws CoreException 
 	 */
 	public static void setReadOnly(final IContainer parent, boolean isReadOnly) throws CoreException {
 		
 		final IResource[] res = parent.members();
 		for (int i = 0; i < res.length; i++) {
 			if (res[i] instanceof IFile) {
 				final IFile file = (IFile)res[i];
 				final ResourceAttributes att = file.getResourceAttributes();
 				if (!att.isHidden() && !file.isHidden() && !file.getName().startsWith(".")) {
 					att.setReadOnly(true);
 					((IFile)res[i]).setResourceAttributes(att);
 				}
 			} else if (res[i] instanceof IContainer) {
 				IFileUtils.setReadOnly((IContainer)res[i], isReadOnly);
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param cont
 	 * @param rootName
 	 * @param ext -  without the "." in front
 	 * @return
 	 */
 	public static IFile getUniqueIFile(final IContainer cont, final String rootName, final String ext) {
         if (cont instanceof IFolder) {
         	return getUniqueIFile((IFolder)cont, rootName, ext);
         } else if (cont instanceof IProject) {
         	return getUniqueIFile((IProject)cont, rootName, ext);
         } else {
         	return null;
         }
 	}
         
 	/**
 	 * Simply gets a unqiue file in the folder.
 	 * @param edna
 	 * @param rootName
 	 * @param ext
 	 * @return
 	 */
 	public static IFile getUniqueIFile(final IFolder edna, final String rootName, final String ext) {
 		
         return getUniqueIFile(edna, rootName, 1, ext);
 	}
 
 	private static IFile getUniqueIFile(final IFolder edna, final String rootName, final int i, final String ext) {
 		
         final IFile file = edna.getFile(rootName+i+"."+ext);
         if (!file.exists()) return file;
         
         return getUniqueIFile(edna, rootName, i+1, ext);
         
 	}
 	
 
 	/**
 	 * Simply gets a unqiue file in the folder.
 	 * @param edna
 	 * @param rootName
 	 * @param ext
 	 * @return
 	 */
 	public static IFile getUniqueIFile(final IProject edna, final String rootName, final String ext) {
 		
         return getUniqueIFile(edna, rootName, 1, ext);
 	}
 
 	private static IFile getUniqueIFile(final IProject edna, final String rootName, final int i, final String ext) {
 		
         final IFile file = edna.getFile(rootName+i+"."+ext);
         if (!file.exists()) return file;
         
         return getUniqueIFile(edna, rootName, i+1, ext);
         
 	}
 
 	private static final Pattern PATH_PATTERN = Pattern.compile("\\/?(\\w+)(\\/.+)");
 
 	public static String getPathWithoutProject(final String path) {
 		final Matcher matcher = PATH_PATTERN.matcher(path);
 		if (matcher.matches()) return matcher.group(2);
 		return path;
 	}
 	/**
 	 * Attempts to find or create a new folder at the given path
 	 * @param path
 	 * @return
 	 */
 	public static IContainer getContainer(final String path, final String projectDefault, final String containerDefault) throws Exception {
 		
 		IContainer cont = (IContainer)ResourcesPlugin.getWorkspace().getRoot().findMember(path, true);
 		if (cont!=null) return cont;
 		
 		final Matcher matcher = PATH_PATTERN.matcher(path);
 		if (matcher.matches()) {
 			final String projectName = matcher.group(1);
 			IProject project = (IProject)ResourcesPlugin.getWorkspace().getRoot().findMember(projectName, true);
 			
 			final String   folderPath = matcher.group(2);
 			IContainer folder;
 			if (("/"+containerDefault+"/").equals(folderPath)) {
 				folder = project.getFolder(containerDefault);
 				
 			} else {
 				IResource find = project.findMember(folderPath);
 				if (find == null) find = project.getFolder(folderPath);
 				folder = find instanceof IContainer ? (IContainer)find : null;
 				
 				if (folder==null) {
 					folder  = project.getFolder(new Path(folderPath));
 					if (folder!=null&&!folder.exists()) {
 						((IFolder)folder).create(true, true, new NullProgressMonitor());
 					}
 				}
 			}
 			
 			if (folder==null) {
 				folder = project.getFolder(containerDefault);
 			}
 			
 			cont = folder;
 			if (folder!=null && !folder.exists()) {
 				if (folder instanceof IFolder) {
 					try {
 						mkdirs((IFolder)folder);
 					} catch (CoreException e) {
 						throw e;
 					}
 				}
 			}
 		}
 
 		if (cont==null) {
 			IProject workflows = (IProject)ResourcesPlugin.getWorkspace().getRoot().findMember(projectDefault, true);
 			cont = workflows.getFolder(containerDefault);
 			if (!cont.exists()) {
 				try {
 					((IFolder)cont).create(true, true, new NullProgressMonitor());
 				} catch (CoreException e) {
 					throw e;
 				}
 			}
 		}
 		return cont;
 	}
 	
 	/**
 	 * Creates the specified IFolder
 	 * 
 	 * @param folder
 	 * @throws CoreException
 	 */
 	public static void mkdirs(IFolder folder) throws CoreException {
 		IContainer container = folder.getParent();
 		if (!container.exists()) {
 			mkdirs((IFolder) container);
 		}
 		folder.create(true, true, null);
 	}
 
 }
