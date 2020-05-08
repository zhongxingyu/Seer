 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.archive.operations;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jem.util.emf.workbench.WorkbenchByteArrayOutputStream;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.File;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.SaveFailureException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveManifest;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.util.ArchiveUtil;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.util.ZipFileExporter;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFile;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 
 public abstract class J2EEComponentSaveStrategyImpl extends ComponentSaveStrategyImpl {
 
 	public J2EEComponentSaveStrategyImpl(IVirtualComponent vComponent) {
 		super(vComponent);
 	}
 
 	protected void saveFiles() throws SaveFailureException {
 		setImportedClassFilesIfNecessary();
 		super.saveFiles();
 	}
 
 	public boolean endsWithClassType(String aFileName) {
 		if (aFileName.endsWith(".class")) //$NON-NLS-1$
 			return true;
 		else
 			return false;
 	}
 
 
 	protected boolean shouldSave(File aFile) {
 		String uri = aFile.getURI();
 		if (endsWithClassType(uri))
 			return false;
 		return super.shouldSave(aFile);
 	}
 
 	public void save(ArchiveManifest aManifest) throws SaveFailureException {
 		IVirtualFolder rootFolder = vComponent.getRootFolder();
 		IVirtualFile vFile = rootFolder.getFile(new Path(J2EEConstants.MANIFEST_URI));
 		IFile iFile = vFile.getUnderlyingFile();
 		validateEdit(iFile);
 		OutputStream out = new WorkbenchByteArrayOutputStream(iFile);
 		try {
 			aManifest.write(out);
 		} catch (IOException e) {
 			Logger.getLogger().logError(e);
 		} finally {
 			try {
 				out.close();
 			} catch (IOException e) {
 				Logger.getLogger().logError(e);
 			}
 		}
 	}
 
 	protected Map importedClassFiles;
 
 	/**
 	 * Import class files into the project.
 	 */
 	protected void setImportedClassFilesIfNecessary() throws SaveFailureException {
 		Map classFiles = getClassFilesWithoutSource();
 
 		if (classFiles == null || classFiles.isEmpty())
 			return;
 
 		importedClassFiles = classFiles;
 
 		IContainer jarParent = vComponent.getRootFolder().getUnderlyingFolder().getParent();
 		String importedClassesComponentName = vComponent.getName() + "_ImportedClasses";
 		IFolder folder = jarParent.getFolder(new Path(importedClassesComponentName));
 		try {
 			folder.create(true, true, null);
 		} catch (CoreException e1) {
 			Logger.getLogger().logError(e1);
 		}
 		IFile importedClassesJar = folder.getFile(new Path("importedClasses.jar"));
 		ZipFileExporter zipFileExporter = null;
 		try {
 			zipFileExporter = new ZipFileExporter(importedClassesJar.getRawLocation().toOSString(), true);
 			Iterator keys = importedClassFiles.keySet().iterator();
 			String uri = null;
 			File file = null;
 			InputStream inputStream = null;
 			while (keys.hasNext()) {
 				uri = (String) keys.next();
 				file = (File) importedClassFiles.get(uri);
 				try {
 					inputStream = file.getInputStream();
 					zipFileExporter.write(inputStream, uri);
 				} catch (CoreException e) {
 					Logger.getLogger().logError(e);
 				} finally {
					if (null != inputStream) {
 						inputStream.close();
 					}
 				}
 			}
 			folder.refreshLocal(1, null);
			IJavaProject javaProject = JavaCore.create(vComponent.getProject());
 			IClasspathEntry[] javaClasspath = javaProject.getRawClasspath();
 			IClasspathEntry[] newJavaClasspath = new IClasspathEntry[javaClasspath.length + 1];
 			System.arraycopy(javaClasspath, 0, newJavaClasspath, 0, javaClasspath.length);
 			newJavaClasspath[newJavaClasspath.length - 1] = JavaCore.newLibraryEntry(importedClassesJar.getFullPath(), null, null, true);
 			javaProject.setRawClasspath(newJavaClasspath, new NullProgressMonitor());
 
 			IVirtualComponent importedClassesComponent = ComponentCore.createArchiveComponent(vComponent.getProject(), "lib/" + importedClassesJar.getRawLocation().toString());
 			// importedClassesComponent.create(0, null);
 			// importedClassesComponent.getRootFolder().createLink(new
 			// Path(importedClassesComponentName), 0, null);
 			// importedClassesComponent.setComponentTypeId(IModuleConstants.JST_UTILITY_IMPORTED_CLASSES_MODULE);
 			IVirtualReference ref = ComponentCore.createReference(vComponent, importedClassesComponent, getImportedClassesRuntimePath());
 			ref.setDependencyType(IVirtualReference.DEPENDENCY_TYPE_CONSUMES);
 			ref.create(0, null);
 		} catch (IOException e) {
 			Logger.getLogger().logError(e);
 		} catch (JavaModelException e) {
 			Logger.getLogger().logError(e);
 		} catch (CoreException e) {
 			Logger.getLogger().logError(e);
 		} finally {
 			if (zipFileExporter != null) {
 				try {
 					zipFileExporter.finished();
 				} catch (IOException e) {
 					Logger.getLogger().logError(e);
 				}
 			}
 		}
 
 	}
 
 	protected IPath getImportedClassesRuntimePath() {
 		return new Path("/");
 	}
 
 	protected Map getClassFilesWithoutSource() {
 		List files = archive.getFiles();
 		Map result = new HashMap();
 		for (int i = 0; i < files.size(); i++) {
 			File aFile = (File) files.get(i);
 			if (isClassWithoutSource(aFile)){
 				result.put(getImportedClassesURI(aFile), aFile);
 			}
 		}
 		return result;
 	}
 	
 	protected String getImportedClassesURI(File aFile){
 		return aFile.getURI();
 	}
 	
 
 	protected boolean isClassWithoutSource(File aFile) {
 		String javaUri = ArchiveUtil.classUriToJavaUri(aFile.getURI());
 		if (javaUri == null)
 			return false;
 		return !archive.containsFile(javaUri);
 	}
 }
