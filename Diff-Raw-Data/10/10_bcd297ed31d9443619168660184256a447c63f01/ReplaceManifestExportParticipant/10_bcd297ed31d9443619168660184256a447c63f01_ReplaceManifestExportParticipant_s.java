 /*******************************************************************************
  * Copyright (c) 2009 Red Hat and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.common.internal.modulecore;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jst.common.frameworks.CommonFrameworksPlugin;
 import org.eclipse.jst.common.internal.modulecore.util.ManifestUtilities;
 import org.eclipse.wst.common.componentcore.internal.flat.AbstractFlattenParticipant;
 import org.eclipse.wst.common.componentcore.internal.flat.FlatFile;
 import org.eclipse.wst.common.componentcore.internal.flat.IFlatFolder;
 import org.eclipse.wst.common.componentcore.internal.flat.IFlatResource;
 import org.eclipse.wst.common.componentcore.internal.flat.VirtualComponentFlattenUtility;
 import org.eclipse.wst.common.componentcore.internal.flat.FlatVirtualComponent.FlatComponentTaskModel;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 
 /**
  * This class is solely responsible for replacing manifest.mf files
  * which require updating before being exported
  * 
  * @author rob
  *
  */
 public class ReplaceManifestExportParticipant extends AbstractFlattenParticipant {
 	//protected static final IPath MANIFEST_PATH = new Path(J2EEConstants.MANIFEST_URI);
 	private IPath manifestPath;
 	
 	public ReplaceManifestExportParticipant(IPath manifestPath) {
 		this.manifestPath = manifestPath;
 	}
 	
 	@Override
 	public void finalize(IVirtualComponent component,
 			FlatComponentTaskModel dataModel, List<IFlatResource> resources) {
 		forceUpdate(component, dataModel, resources);
 	}
 	
 	public void forceUpdate(IVirtualComponent component,
 			FlatComponentTaskModel dataModel, List<IFlatResource> resources) {
 		List<String> javaClasspathURIs = getClasspathURIs(component);
 		if( !javaClasspathURIs.isEmpty()) {
 			// find the old manifest
 			IFlatFolder parent = (IFlatFolder)VirtualComponentFlattenUtility.getExistingModuleResource(resources, manifestPath.removeLastSegments(1));
 			if( parent != null ) {
 				IFlatResource[] children = parent.members();
 				IFile original = null;
 				int originalIndex = 0;
 				for( int i = 0; i < children.length; i++) {
 					if( children[i].getName().equals(manifestPath.lastSegment())) {
 						original = (IFile)children[i].getAdapter(IFile.class);
 						originalIndex = i;
 						File newManifest = getNewManifest(component.getProject(), original, javaClasspathURIs);
 						FlatFile newManifestExportable = new FlatFile(newManifest, newManifest.getName(), manifestPath.removeLastSegments(1));
 						children[originalIndex] = newManifestExportable;
 						parent.setMembers(children);
 						return;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Return whichever File is the new one, even if it's the same as the old one
 	 * @return
 	 */
 	public File getNewManifest(IProject project, IFile originalManifest, List<String> javaClasspathURIs) {
 		final IPath workingLocation = project.getWorkingLocation(CommonFrameworksPlugin.PLUGIN_ID);
 		// create path to temp MANIFEST.MF 
 		final IPath tempManifestPath = workingLocation.append(manifestPath);
 		final File tempFile = tempManifestPath.toFile();
 		if (!tempFile.exists()) {
 			// create parent dirs for temp MANIFEST.MF
 			final File parent = tempFile.getParentFile();
 			if (!parent.exists()) {
 				if (!parent.mkdirs()) {
 					return originalManifest != null ? originalManifest.getLocation().toFile() : null;
 				}
 			}
 		}
 		try {	
 			ManifestUtilities.updateManifestClasspath(originalManifest, javaClasspathURIs, tempFile);
 		} catch (FileNotFoundException e) {
 		} catch (IOException e) {
 		}
 		return tempFile.exists() ? tempFile :  
 			originalManifest != null ? originalManifest.getLocation().toFile() : null;
 	}
 	
 	public static List<String> getClasspathURIs(IVirtualComponent component) {
 		ArrayList<String> uris = new ArrayList<String>();
 		uris = new ArrayList<String>();
 		if (component instanceof IClasspathDependencyProvider) {
 			final IClasspathDependencyProvider j2eeComp = (IClasspathDependencyProvider) component;
 			final IVirtualReference[] refs = j2eeComp.getJavaClasspathReferences();
 			if (refs != null) {
 				for (int i = 0; i < refs.length; i++) {
					if (refs[i].getRuntimePath().equals(IClasspathDependencyReceiver.RUNTIME_MAPPING_INTO_CONTAINER)) {
 						uris.add(refs[i].getArchiveName());
 					}
 				}
 			}
 		}
 		return uris;
 	}
 	
 	
 }
