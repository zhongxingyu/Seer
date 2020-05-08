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
 /*
  * Created on Aug 28, 2003
  *
  * To change the template for this generated file go to
  * Window>Preferences>Java>Code Generation>Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.project;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.URIConverterImpl;
 import org.eclipse.jem.util.emf.workbench.JavaProjectUtilities;
 import org.eclipse.jem.util.emf.workbench.ProjectResourceSet;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.emf.workbench.ResourceSetWorkbenchSynchronizer;
 import org.eclipse.jst.j2ee.applicationclient.internal.creation.ApplicationClientNatureRuntime;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.CommonarchiveFactory;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.OpenFailureException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveTypeDiscriminator;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.strategy.AppClient12ImportStrategyImpl;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.strategy.EjbJar11ImportStrategyImpl;
 import org.eclipse.jst.j2ee.internal.common.impl.J2EEResouceFactorySaxRegistry;
 import org.eclipse.jst.j2ee.internal.common.impl.J2EEResourceFactoryRegistry;
 
 /**
  * @author jlanuti
  * 
  * To change the template for this generated type comment go to Window>Preferences>Java>Code
  * Generation>Code and Comments
  */
 public class J2EEModuleWorkbenchURIConverterImpl extends J2EEWorkbenchURIConverterImpl {
 
 	/** For binary projects, this is the file system absolute location of the input JAR */
 	protected IPath inputJARLocation;
 	/** For binary projects, this is the project relative location of the input JAR */
 	protected IPath inputJARProjectRelativePath;
 
 
 	/**
 	 * @param aNature
 	 * @param aSynchronizer
 	 */
 	public J2EEModuleWorkbenchURIConverterImpl(J2EEModuleNature aNature, ResourceSetWorkbenchSynchronizer aSynchronizer) {
 		super(aNature, aSynchronizer);
 	}
 
 	protected void initialize() {
 		recomputeInputsIfNecessary();
 		super.initialize();
 	}
 
 	protected ArchiveTypeDiscriminator getArchiveDiscriminator() {
 		if (nature.getNatureID().equals(IEJBNatureConstants.NATURE_ID))
 			return EjbJar11ImportStrategyImpl.getDiscriminator();
 		else if (nature instanceof ApplicationClientNatureRuntime)
 			return AppClient12ImportStrategyImpl.getDiscriminator();
 		return null;
 	}
 
 	protected boolean isJ2EEArchive(IResource resource) {
 		String uri = resource.getLocation().toOSString();
 		ArchiveTypeDiscriminator disc = getArchiveDiscriminator();
 		if (disc == null)
 			return false;
		Archive anArchive = null;
 		try {
 			anArchive = getArchiveFactory().primOpenArchive(uri);
 			return disc.canImport(anArchive);
 		} catch (OpenFailureException ex) {
 			return false;
 		} finally {
 			if (anArchive != null)
 				anArchive.close();
 		}
 	}
 
 	protected CommonarchiveFactory getArchiveFactory() {
 		return CommonarchiveFactory.eINSTANCE;
 	}
 
 	public boolean recomputeInputsIfNecessary() {
 		boolean containersChanged = recomputeContainersIfNecessary();
 		boolean inputJARChanged = recomputeInputJARLocation();
 		return containersChanged || inputJARChanged;
 	}
 
 	/**
 	 * recomputes the input containers and the binary JAR location
 	 * 
 	 * @return boolean if a change was detected
 	 */
 	public boolean recomputeContainersIfNecessary() {
 		IContainer input = getInputContainer();
 		IContainer newInput = nature.getEMFRoot();
 		boolean changed = !objectsEqual(input, newInput);
 		if (changed && (getNature() != null && getNature().getProject() != null && getNature().getProject().isAccessible())) {
 			List resources = getNature().getResourceSet().getResources();
 			deNormalize(resources);
 			getInputContainers().clear();
 			setOutputContainer(null);
 			resetNormalizedResourceCache();
 			if (newInput != null)
 				addInputContainer(newInput);
 			normalize(resources);
 		}
 		return changed;
 	}
 
 	private void resetNormalizedResourceCache() {
 		J2EENature aNature = getNature();
 		if (aNature != null) {
 			ResourceSet set = aNature.getResourceSet();
 			if (set instanceof ProjectResourceSet)
 				((ProjectResourceSet) set).resetNormalizedURICache();
 		}
 	}
 
 	/**
 	 * Applies only if the nature is a "binary" project
 	 * 
 	 * @return boolean if the input JAR location changed
 	 */
 	public boolean recomputeInputJARLocation() {
 		if (!((J2EEModuleNature) nature).canBeBinary())
 			return false;
 		IPath existing = inputJARLocation;
 		IPath newLoc = null;
 		boolean changed = false;
 		IResource resource = null;
 		if (JavaProjectUtilities.isBinaryProject(nature.getProject())) {
 			resource = getInputJARResource();
 			newLoc = resource == null ? null : resource.getLocation();
 		}
 		if (!objectsEqual(existing, newLoc)) {
 			changed = true;
 			inputJARLocation = newLoc;
 			inputJARProjectRelativePath = resource == null ? null : resource.getProjectRelativePath();
 			nature.getResourceSet().setResourceFactoryRegistry((null == inputJARLocation) ? J2EEResourceFactoryRegistry.INSTANCE : J2EEResouceFactorySaxRegistry.INSTANCE);
 		}
 		return changed;
 	}
 
 	protected IResource getInputJARResource() {
 		IProject project = nature.getProject();
 		if (project == null)
 			return null;
 		List jarPaths = JavaProjectUtilities.getLocalJARPathsFromClasspath(project);
 		for (int i = 0; i < jarPaths.size(); i++) {
 			IPath path = (IPath) jarPaths.get(i);
 			IResource resource = project.findMember(path);
 			if (resource != null && resource.exists() && (resource instanceof IFile) && isJ2EEArchive(resource))
 				return resource;
 		}
 		return null;
 	}
 
 	/**
 	 * @see org.eclipse.emf.ecore.resource.impl.URIConverterImpl#createInputStream(URI)
 	 */
 	public InputStream createInputStream(URI uri) throws IOException {
 		if (isBinary())
 			return createBinaryInputStream(uri);
 		return super.createInputStream(uri);
 	}
 
 	protected InputStream createBinaryInputStream(URI uri) throws IOException {
 		//if it's a primitives doc then let the super implementation handle it
 		if (!(((URIConverterImpl.URIMap) getURIMap()).getURI(uri).equals(uri)))
 			return super.createInputStream(uri);
 		ZipFile zip = null;
 		boolean forceClose = true;
 		try {
 			zip = new ZipFile(inputJARLocation.toOSString());
 			InputStream result = searchZipFile(zip, uri.toString());
 			if (result == null) {
 				throw new FileNotFoundException(uri.toString());
 			}
 			ZipFileEntryInputStream inputStream = new ZipFileEntryInputStream(result, zip);
 			forceClose = false;
 			return inputStream;
 		} finally {
 			//We don't want to close it here, or the IS can't be
 			//read; the ZipFileEntryInputStream will do the close
 			// when it is closed
 			//if (zip != null)
 			//	zip.close();
 
 			//Only close the zip file here if we fail to return an inputstream
 			if (forceClose && zip != null) {
 				zip.close();
 			}
 		}
 	}
 
 	/**
 	 * For binary projects, this is the file system absolute location of the input JAR
 	 */
 	public IPath getInputJARLocation() {
 		return inputJARLocation;
 	}
 
 	/**
 	 * For binary projects, this is the project relative location of the input JAR
 	 */
 	public IPath getInputJARProjectRelativePath() {
 		return inputJARProjectRelativePath;
 	}
 
 	protected boolean isBroken() {
 		return super.isBroken() && getInputJARLocation() == null;
 	}
 
 	protected boolean isBinary() {
 		return inputJARLocation != null;
 	}
 
 	/**
 	 * @see org.eclipse.wst.common.internal.emfworkbench.WorkbenchURIConverterImpl#canGetUnderlyingResource(String)
 	 */
 	public boolean canGetUnderlyingResource(String aFileName) {
 		if (isBinary())
 			return hasZipEntry(aFileName);
 		return super.canGetUnderlyingResource(aFileName);
 	}
 
 	protected boolean isClasspathChanged(IResourceDelta delta) {
 		IResourceDelta child = delta.findMember(new Path(ProjectUtilities.DOT_CLASSPATH));
 		return (child != null) && (child.getFlags() != IResourceDelta.MARKERS);
 	}
 
 	private boolean hasZipEntry(String aFileName) {
 		//URI converted = URI.createURI(applyURIMapping(aFileName.toString()));
 		//dcb - if normalize is required it should be done prior to this method
 		//being called.
 		ZipFile zip = null;
 		try {
 			zip = new ZipFile(inputJARLocation.toOSString());
 			return zip.getEntry(aFileName) != null;
 		} catch (IOException e) {
 		} finally {
 			if (zip != null)
 				try {
 					zip.close();
 				} catch (IOException e) {
 				}
 		}
 		return false;
 	}
 
 	/**
 	 * This method determines whether there is a ZipEntry whose name is filename in the given
 	 * ZipFile. If not, it returns null; otherwise, it returns an input source to read from the
 	 * ZipEntry.
 	 * 
 	 * @param zip
 	 *            java.util.zip.ZipFile
 	 * @return java.io.InputSource
 	 */
 	protected InputStream searchZipFile(ZipFile zip, String filename) throws IOException {
 		ZipEntry entry = zip.getEntry(filename);
 
 		if (entry == null)
 			return null;
 
 		return zip.getInputStream(entry);
 	}
 
 	protected boolean shouldNotifyChangedListeners(IResourceDelta delta) {
 		if (isClasspathChanged(delta) || super.shouldNotifyChangedListeners(delta)) {
 			if (recomputeInputsIfNecessary())
 				return true;
 		}
 		return false;
 	}
 
 
 }
