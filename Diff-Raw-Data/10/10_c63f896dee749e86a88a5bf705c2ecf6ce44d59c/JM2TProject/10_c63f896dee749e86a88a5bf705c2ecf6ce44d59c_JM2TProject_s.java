 /*******************************************************************************
  * Copyright (c) 2010 Angelo ZERR.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:      
  *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
  *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2010 Angelo ZERR.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:      
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
 package org.eclipse.gmt.modisco.jm2t.internal.core;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectNature;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.gmt.modisco.jm2t.core.IJM2TProject;
 import org.eclipse.gmt.modisco.jm2t.core.JM2TCore;
 import org.eclipse.gmt.modisco.jm2t.core.generator.IGeneratorConfiguration;
 import org.eclipse.gmt.modisco.jm2t.core.util.ResourcesUtils;
 import org.eclipse.gmt.modisco.jm2t.internal.core.generator.GeneratorConfiguration;
 import org.eclipse.gmt.modisco.jm2t.internal.core.util.Util;
 import org.eclipse.gmt.modisco.jm2t.internal.core.xml.XMLSettingsLoader;
 import org.eclipse.gmt.modisco.jm2t.internal.core.xml.XMLWriter;
 
 /**
  * Implementation for Java M2T project {@link IJM2TProject}.
  * 
  */
 public class JM2TProject implements IJM2TProject, IProjectNature {
 
 	public static final String SHARED_PROPERTIES_DIRECTORY = ".settings"; //$NON-NLS-1$
 
 	/**
 	 * Name of file containing project classpath
 	 */
 	public static final String JM2T_SETTINGS_FILENAME = ".jm2tsettings"; //$NON-NLS-1$
 
 	static final QualifiedName JM2T_PROJECT = new QualifiedName(
 			JM2TCore.PLUGIN_ID + ".sessionprops", "jm2tProject");
 
 	/**
 	 * Value of the project's raw generator configuration if the .jm2tsettings
 	 * file contains invalid entries.
 	 */
 	public static final Collection<IGeneratorConfiguration> INVALID_CONFIG = new ArrayList<IGeneratorConfiguration>();
 
 	private Collection<IGeneratorConfiguration> generatorConfigurationsCache = null;
 
 	private IProject project;
 
 	public IFile getJM2TSettingsFile() {
 		return getJM2TSettingsFile(false);
 	}
 
 	public IFile getJM2TSettingsFile(boolean forceCreate) {
 
 		// Return the projects .jm2tsettings file
 		IFolder rscPath = this.project.getFolder(SHARED_PROPERTIES_DIRECTORY);
 		if (!rscPath.exists() && forceCreate)
 			try {
 				rscPath.create(true, true, new NullProgressMonitor());
 			} catch (CoreException e) {
 			}
 
 		return rscPath.getFile(JM2T_SETTINGS_FILENAME);
 
 	}
 
 	public void configure() throws CoreException {
 		project.setSessionProperty(JM2T_PROJECT, this);
 	}
 
 	public void deconfigure() throws CoreException {
 		project.setSessionProperty(JM2T_PROJECT, null);
 	}
 
 	public IProject getProject() {
 		return project;
 	}
 
 	public void setProject(IProject project) {
 		this.project = project;
 	}
 
 	public Collection<IGeneratorConfiguration> getGeneratorConfigurations() {
 		if (generatorConfigurationsCache == null) {
 			generatorConfigurationsCache = readGeneratorConfigurations();
 		}
 		if (generatorConfigurationsCache == null) {
 			return Collections.emptyList();
 		}
 		return generatorConfigurationsCache;
 	}
 
 	public Collection<IGeneratorConfiguration> readGeneratorConfigurations() {
 		return readFileEntries(null);
 	}
 
 	/**
 	 * 
 	 * Reads the generator configuration file entries of this project's
 	 * .jm2tsettings file. This includes the output entry. As a side effect,
 	 * unknown elements are stored in the given map (if not null)
 	 */
 	private Collection<IGeneratorConfiguration> readFileEntries(
 			Map unkwownElements) {
 		try {
 			return readFileEntriesWithException(unkwownElements);
 		} catch (CoreException e) {
 			Trace.trace(
 					Trace.INFO,
 					"Exception while reading " + getPath().append(JM2T_SETTINGS_FILENAME), e); //$NON-NLS-1$
 			return INVALID_CONFIG;
 		} catch (IOException e) {
 			Trace.trace(
 					Trace.INFO,
 					"Exception while reading " + getPath().append(JM2T_SETTINGS_FILENAME), e); //$NON-NLS-1$
 			return INVALID_CONFIG;
 		}
 	}
 
 	/**
 	 * Reads the classpath file entries of this project's .jm2tsettings file.
 	 * This includes the output entry. As a side effect, unknown elements are
 	 * stored in the given map (if not null) Throws exceptions if the file
 	 * cannot be accessed or is malformed.
 	 */
 	public Collection<IGeneratorConfiguration> readFileEntriesWithException(
 			Map unknownElements) throws CoreException, IOException {
 		IFile rscFile = getJM2TSettingsFile();
 
 		URI location = rscFile.getLocationURI();
 		if (location == null)
 			throw new IOException("Cannot obtain a location URI for " + rscFile); //$NON-NLS-1$
 		File file = ResourcesUtils.toLocalFile(location, null/*
 															 * no progress
 															 * monitor available
 															 */);
 		if (file == null)
 			throw new IOException("Unable to fetch file from " + location); //$NON-NLS-1$
 		XMLSettingsLoader settingsLoader = new XMLSettingsLoader();
 		settingsLoader.load(file);
 		return settingsLoader.getGeneratorConfigurations();
 	}
 
 	public void setRawGeneratorConfiguration(
 			Collection<IGeneratorConfiguration> entries,
 			IProgressMonitor monitor) throws CoreException {
 
 		setRawIncludepath(entries, getPath()/* don't change output */,
 				true/* can change resource (as per API contract) */, monitor);
 	}
 
 	public void setRawIncludepath(
 			Collection<IGeneratorConfiguration> newRawClasspath,
 			IPath newOutputLocation, boolean canModifyResources,
 			IProgressMonitor monitor) throws CoreException {
 
 		// if (newRawClasspath == null) //are we already with the default
 		// classpath
 		// newRawClasspath = defaultClasspath();
 
 		// SetClasspathOperation op =
 		// new SetClasspathOperation(
 		// this,
 		// newRawClasspath,
 		// newOutputLocation,
 		// canModifyResources);
 		// op.runOperation(monitor);
 
 		saveClasspath(newRawClasspath, newOutputLocation);
 
 	}
 
 	/**
 	 * Saves the classpath in a shareable format (VCM-wise) only when necessary,
 	 * that is, if it is semantically different from the existing one in file.
 	 * Will never write an identical one.
 	 * 
 	 * @param newClasspath
 	 *            IIncludePathEntry[]
 	 * @param newOutputLocation
 	 *            IPath
 	 * @return boolean Return whether the .classpath file was modified.
 	 * @throws JavaScriptModelException
 	 */
 	public boolean saveClasspath(
 			Collection<IGeneratorConfiguration> newClasspath,
 			IPath newOutputLocation) throws CoreException {
 
 		if (!this.project.isAccessible())
 			return false;
 
 		Map unknownElements = new HashMap();
 		Collection<IGeneratorConfiguration> fileEntries = readFileEntries(unknownElements);
 		if (fileEntries != INVALID_CONFIG
 				&& areClasspathsEqual(newClasspath, newOutputLocation,
 						fileEntries)) {
 			// no need to save it, it is the same
 			return false;
 		}
 
 		// actual file saving
 		// try {
 		setSharedProperty(
 				getJM2TSettingsFile(true).getProjectRelativePath().toString(),
 				encodeClasspath(newClasspath, newOutputLocation, true,
 						unknownElements));
 		generatorConfigurationsCache = newClasspath;
 		return true;
 		// } catch (CoreException e) {
 		// //throw new CoreException(e);
 		// }
 	}
 
 	private String encodeClasspath(
 			Collection<IGeneratorConfiguration> classpath,
 			IPath outputLocation, boolean indent, Map unknownElements) {
 		try {
 			ByteArrayOutputStream s = new ByteArrayOutputStream();
 			OutputStreamWriter writer = new OutputStreamWriter(s, Util.UTF_8); //$NON-NLS-1$
 			XMLWriter xmlWriter = new XMLWriter(writer, this, true/*
 																 * print XML
 																 * version
 																 */);
 
 			xmlWriter.startTag(GeneratorConfiguration.TAG_JM2T, indent);
 			for (IGeneratorConfiguration iGeneratorConfiguration : classpath) {
 				((GeneratorConfiguration) iGeneratorConfiguration)
 						.elementEncode(xmlWriter, this.project.getFullPath(),
 								indent, true, unknownElements);
 			}
 
 			// if (outputLocation != null) {
 			// outputLocation = outputLocation.removeFirstSegments(1);
 			// outputLocation = outputLocation.makeRelative();
 			// HashMap parameters = new HashMap();
 			// parameters.put(ClasspathEntry.TAG_KIND,
 			// ClasspathEntry.kindToString(ClasspathEntry.K_OUTPUT));
 			// parameters.put(ClasspathEntry.TAG_PATH,
 			// String.valueOf(outputLocation));
 			// xmlWriter.printTag(ClasspathEntry.TAG_CLASSPATHENTRY, parameters,
 			// indent, true, true);
 			// }
 
 			xmlWriter.endTag(GeneratorConfiguration.TAG_JM2T, indent, true/*
 																		 * insert
 																		 * new
 																		 * line
 																		 */);
 			writer.flush();
 			writer.close();
 			return s.toString(Util.UTF_8);//$NON-NLS-1$
 		} catch (IOException e) {
 			// throw new JavaScriptModelException(e,
 			// IJavaScriptModelStatusConstants.IO_EXCEPTION);
 			return null;
 		}
 	}
 
 	private boolean areClasspathsEqual(
 			Collection<IGeneratorConfiguration> newClasspath,
 			IPath newOutputLocation,
 			Collection<IGeneratorConfiguration> fileEntries) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/**
 	 * Record a shared persistent property onto a project. Note that it is
 	 * orthogonal to IResource persistent properties, and client code has to
 	 * decide which form of storage to use appropriately. Shared properties
 	 * produce real resource files which can be shared through a VCM onto a
 	 * server. Persistent properties are not shareable.
 	 * 
 	 * shared properties end up in resource files, and thus cannot be modified
 	 * during delta notifications (a CoreException would then be thrown).
 	 * 
 	 * @param key
 	 *            String
 	 * @param value
 	 *            String
 	 * @see
 	 * @throws CoreException
 	 */
 	public void setSharedProperty(String key, String value)
 			throws CoreException {
 
 		// IFile rscFile = this.project.getFile(key);
 		IFile rscFile = this.project.getFile(key);
 		byte[] bytes = null;
 		try {
 			bytes = value.getBytes(Util.UTF_8); // .classpath always encoded
 												// with UTF-8
 		} catch (UnsupportedEncodingException e) {
 			Trace.trace(Trace.SEVERE,
 					"Could not write .jsdtscope with UTF-8 encoding ", e); //$NON-NLS-1$
 			// fallback to default
 			bytes = value.getBytes();
 		}
 		InputStream inputStream = new ByteArrayInputStream(bytes);
 		// update the resource content
 		if (rscFile.exists()) {
 			if (rscFile.isReadOnly()) {
 				// provide opportunity to checkout read-only .classpath file
 				// (23984)
 				ResourcesPlugin.getWorkspace().validateEdit(
 						new IFile[] { rscFile }, null);
 			}
 			rscFile.setContents(inputStream, IResource.FORCE, null);
 		} else {
 			rscFile.create(inputStream, IResource.FORCE, null);
 		}
 	}
 
 	public IGeneratorConfiguration findGeneratorConfiguration(String name) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IPath getPath() {
 		return this.project.getFullPath();
 	}
 }
