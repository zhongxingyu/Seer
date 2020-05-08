 /***************************************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  **************************************************************************************************/
 package org.eclipse.jst.j2ee.application.internal.operations;
 
 
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.EARFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.File;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.util.ArchiveUtil;
 import org.eclipse.jst.j2ee.internal.archive.operations.J2EEImportConstants;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.wst.common.componentcore.UnresolveableURIException;
 import org.eclipse.wst.common.componentcore.internal.impl.ModuleURIUtil;
 
 
 public class ClasspathElement {
 	public final static int UTIL_JAR = 0;
 	public final static int EJB_JAR = 1;
 	public final static int EJB_CLIENT_JAR = 2;
 	protected int jarType = UTIL_JAR;
 	/** The owner archive of this entry */
 	protected Archive archive;
 
 	/** The archive this entry references, if it exists */
 	protected Archive targetArchive;
 
 	/** The display text for this item */
 	protected String text;
 	/** Indicates if this is selected in the view */
 	protected boolean selected;
 	protected boolean valid;
 	/**
 	 * The text that is an actual Class-Path entry in the Manifest; in the case of multi-segment
 	 * uris, might look like ../xxx
 	 */
 	protected String relativeText;
 	/** The project that corresponds to the dependent module, when it exists */
 	protected IProject project;
 	/**
 	 * If the project is not null, there may be imported jars in the project List of IPath
 	 */
 	protected List importedJarPaths;
 	protected IProject earProject;
 	protected ClassPathSelection parentSelection;
 	protected URI archiveURI;
 
 	public ClasspathElement(Archive anArchive) {
 		super();
 		archive = anArchive;
 	}
 	
 	public ClasspathElement(IProject project) {
 		super();
 		this.project = project;
 	}
 	
 	public ClasspathElement(URI  aArchiveURI) {
 		super();
 		archiveURI = aArchiveURI;
 	}	
 
 	protected void computeRelativeText() {
 		if (archive != null) {
 			relativeText = J2EEProjectUtilities.computeRelativeText(archive.getURI(), getText());
 			if (relativeText == null)
 				relativeText = getText();
 		}
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/28/2001 5:07:26 PM)
 	 * 
 	 * @return org.eclipse.core.resources.IProject
 	 */
 	public org.eclipse.core.resources.IProject getEarProject() {
 		return earProject;
 	}
 
 	protected IFile getImportedJarAsIFile() {
 		if (getProject() != null) {
 			if (getText() != null && getText().endsWith(J2EEImportConstants.IMPORTED_JAR_SUFFIX)) {
 				IFile file = getProject().getFile(getText());
 				if (file != null && file.exists())
 					return file;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/28/2001 4:33:35 PM)
 	 * 
 	 * @return java.util.List
 	 */
 	public java.util.List getImportedJarPaths() {
 		return importedJarPaths;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/27/2001 1:14:04 PM)
 	 * 
 	 * @return int
 	 */
 	public int getJarType() {
 		return jarType;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/25/2001 6:21:01 PM)
 	 * 
 	 * @return org.eclipse.core.resources.IProject
 	 */
 	public org.eclipse.core.resources.IProject getProject() {
 		return project;
 	}
 
 	public String getProjectName() {
 		return project == null ? null : project.getName();
 	}
 
 	public java.lang.String getRelativeText() {
 		if (relativeText == null)
 			computeRelativeText();
 		return relativeText;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/22/2001 11:00:36 AM)
 	 * 
 	 * @return java.lang.String
 	 */
 	public java.lang.String getText() {
 		return text;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/22/2001 11:01:46 AM)
 	 * 
 	 * @return boolean
 	 */
 	public boolean isSelected() {
 		return selected;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/27/2001 1:04:35 PM)
 	 * 
 	 * @return boolean
 	 */
 	public boolean isValid() {
 		return valid;
 	}
 
 	public IResource getResource() {
 		if (project != null)
 			return project;
 		return earProject.getFile(getText());
 	}
 
 	public IClasspathEntry[] newClasspathEntriesForArchive() {
 		if( !archiveURI.equals("")){
 			String resourcePath = "";
 			try {
 				resourcePath = ModuleURIUtil.getArchiveName(archiveURI);
 			} catch (UnresolveableURIException e) {
 				Logger.getLogger().logError(e);
 			}
 			java.io.File file = new java.io.File(resourcePath);
 			if( file.exists()){
 				return new IClasspathEntry[]{JavaCore.newLibraryEntry( new Path(resourcePath), null, null)};
 			}else{
 				return new IClasspathEntry[]{JavaCore.newVariableEntry( new Path(resourcePath), null, null)};
 			}
 		}	
 		return new IClasspathEntry[0];
 	}
 	
 	
 	/**
 	 * Adapter method to convert this manifest class path element to zero or more classpath entries
 	 * for a java build path
 	 */
 	public IClasspathEntry[] newClasspathEntries(Set visited) {
 		if (visited.contains(this))
 			return new IClasspathEntry[0];
 		visited.add(this);
 		if (representsImportedJar())
 			return new IClasspathEntry[]{JavaCore.newLibraryEntry(getImportedJarAsIFile().getFullPath(), null, null)};
 		
 		if( archiveURI != null && !archiveURI.equals("") ){
 			return newClasspathEntriesForArchive();
 		}
 		if (!valid && isSelected())
 			return new IClasspathEntry[0];
 
 		if (project == null)
 			return newClasspathEntriesFromEARProject(visited);
 
 		IClasspathEntry projectEntry = JavaCore.newProjectEntry(getProject().getFullPath(), true);
 		if (importedJarPaths == null || importedJarPaths.isEmpty())
 			return new IClasspathEntry[]{projectEntry};
 
 		List result = new ArrayList(2);
 		result.add(projectEntry);
 		for (int i = 0; i < importedJarPaths.size(); i++) {
 			IPath path = (IPath) importedJarPaths.get(i);
 			result.add(JavaCore.newLibraryEntry(path, null, null));
 		}
 		return (IClasspathEntry[]) result.toArray(new IClasspathEntry[result.size()]);
 	}
 
 	public IClasspathEntry[] newClasspathEntries() {
 		return newClasspathEntries(new HashSet());
 	}
 
 	protected IClasspathEntry newClasspathEntryFromEARProj() {
		//String text = getText();
		IPath path = earProject.getFile(getText()).getFullPath();
 		return JavaCore.newLibraryEntry(path, path, null, true);
 	}
 
 	/**
 	 * The archive is in the EAR and not in a project.
 	 */
 	protected IClasspathEntry[] newClasspathEntriesFromEARProject(Set visited) {
 		List cpEntries = new ArrayList();
 		cpEntries.add(newClasspathEntryFromEARProj());
 		traverseClasspaths(cpEntries, visited);
 		return (IClasspathEntry[]) cpEntries.toArray(new IClasspathEntry[cpEntries.size()]);
 	}
 
 	/*
 	 * If you have a dependency to a JAR in the EAR project, and the JAR depends on another JAR in
 	 * the EAR; you want to compile cleanly after import, so you need both those JARs on your build
 	 * path
 	 */
 	protected void traverseClasspaths(List projectCpEntries, Set visited) {
 
 		File aFile = null;
 		try {
 			aFile = getEARFile().getFile(getText());
 		} catch (FileNotFoundException notThere) {
 		}
 		if (aFile == null || !aFile.isArchive())
 			return;
 
 		Archive depArchive = (Archive) aFile;
 		String[] manifestCpEntries = depArchive.getManifest().getClassPathTokenized();
 		for (int i = 0; i < manifestCpEntries.length; i++) {
 			String uri = ArchiveUtil.deriveEARRelativeURI(manifestCpEntries[i], depArchive);
 			if (uri == null)
 				continue;
 			ClasspathElement other = parentSelection.getClasspathElement(uri);
 			//If the other element is already selected, then
 			// we don't need to add it again
 			if (other == null || other.isSelected())
 				continue;
 			IClasspathEntry[] cpEntries = other.newClasspathEntries(visited);
 			for (int j = 0; j < cpEntries.length; j++) {
 				if (!projectCpEntries.contains(cpEntries[j]))
 					projectCpEntries.add(cpEntries[j]);
 			}
 		}
 	}
 
 
 	public boolean representsImportedJar() {
 		return getImportedJarAsIFile() != null;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/28/2001 5:07:26 PM)
 	 * 
 	 * @param newEarProject
 	 *            org.eclipse.core.resources.IProject
 	 */
 	public void setEarProject(org.eclipse.core.resources.IProject newEarProject) {
 		earProject = newEarProject;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/28/2001 4:33:35 PM)
 	 * 
 	 * @param newImportedJarPaths
 	 *            java.util.List
 	 */
 	public void setImportedJarPaths(java.util.List newImportedJarPaths) {
 		importedJarPaths = newImportedJarPaths;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/27/2001 1:14:04 PM)
 	 * 
 	 * @param newJarType
 	 *            int
 	 */
 	public void setJarType(int newJarType) {
 		jarType = newJarType;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/25/2001 6:21:01 PM)
 	 * 
 	 * @param newProject
 	 *            org.eclipse.core.resources.IProject
 	 */
 	public void setProject(org.eclipse.core.resources.IProject newProject) {
 		project = newProject;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/22/2001 4:20:55 PM)
 	 * 
 	 * @param newRelativeText
 	 *            java.lang.String
 	 */
 	public void setRelativeText(java.lang.String newRelativeText) {
 		relativeText = newRelativeText;
 	}
 
 	public void setSelected(Archive referencingJar, Archive referencedJar, List classPath) {
 		for (int i = 0; i < classPath.size(); i++) {
 			String cpEntry = (String) classPath.get(i);
 			String uri = ArchiveUtil.deriveEARRelativeURI(cpEntry, referencingJar);
 			if (uri != null && uri.equals(referencedJar.getURI())) {
 				setSelected(true);
 				return;
 			}
 		}
 		setSelected(false);
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/22/2001 11:01:46 AM)
 	 * 
 	 * @param newSelected
 	 *            boolean
 	 */
 	public void setSelected(boolean newSelected) {
 		boolean oldSelected = selected;
 		selected = newSelected;
 		if (oldSelected != newSelected && parentSelection != null)
 			parentSelection.setModified(true);
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/22/2001 11:00:36 AM)
 	 * 
 	 * @param newText
 	 *            java.lang.String
 	 */
 	public void setText(java.lang.String newText) {
 		text = newText;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/27/2001 1:04:35 PM)
 	 * 
 	 * @param newValid
 	 *            boolean
 	 */
 	public void setValid(boolean newValid) {
 		valid = newValid;
 	}
 
 	public void setValuesSelected(String cpEntry) {
 		setSelected(true);
 		setRelativeText(cpEntry);
 	}
 
 	public String toString() {
 		return "ClasspatheElement(" + getText() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	protected EARFile getEARFile() {
 		return (EARFile) archive.getContainer();
 	}
 
 	void setParentSelection(ClassPathSelection selection) {
 		parentSelection = selection;
 	}
 
 	/**
 	 * Returns the archive.
 	 * 
 	 * @return Archive
 	 */
 	public Archive getArchive() {
 		return archive;
 	}
 
 	public boolean isEJBJar() {
 		return jarType == EJB_JAR;
 	}
 
 	public boolean isEJBClientJar() {
 		return jarType == EJB_CLIENT_JAR;
 	}
 
 	/**
 	 * @return
 	 */
 	public Archive getTargetArchive() {
 		return targetArchive;
 	}
 
 	/**
 	 * @param archive
 	 */
 	public void setTargetArchive(Archive archive) {
 		targetArchive = archive;
 	}
 
 }
