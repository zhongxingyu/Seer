 /*******************************************************************************
  * Copyright (c) 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.archive.operations;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.jee.archive.ArchiveException;
 import org.eclipse.jst.jee.archive.ArchiveSaveFailureException;
 import org.eclipse.jst.jee.archive.internal.ArchiveUtil;
 import org.eclipse.wst.common.componentcore.internal.flat.FlatFolder;
 import org.eclipse.wst.common.componentcore.internal.flat.FlatVirtualComponent;
 import org.eclipse.wst.common.componentcore.internal.flat.IChildModuleReference;
 import org.eclipse.wst.common.componentcore.internal.flat.IFlatFile;
 import org.eclipse.wst.common.componentcore.internal.flat.IFlatFolder;
 import org.eclipse.wst.common.componentcore.internal.flat.IFlatResource;
 import org.eclipse.wst.common.componentcore.internal.flat.IFlatVirtualComponent;
 import org.eclipse.wst.common.componentcore.internal.flat.IFlattenParticipant;
 import org.eclipse.wst.common.componentcore.internal.flat.VirtualComponentFlattenUtility;
 import org.eclipse.wst.common.componentcore.internal.flat.FlatVirtualComponent.FlatComponentTaskModel;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 
 public class FlatComponentArchiver {
 	private IFlatVirtualComponent flatComponent;
 	private OutputStream destinationStream;
 	private ZipOutputStream zipOutputStream;
 	private IVirtualComponent component;
 	private List<IFlattenParticipant> participants;
 	private ComponentExportCallback callbackHandler;
 	private List<IPath> zipEntries = new ArrayList<IPath>();
 	
 	public interface ComponentExportCallback {
 		public boolean canSave(IVirtualComponent component);
 		public void saveArchive(IVirtualComponent component, ZipOutputStream zipOutputStream) throws ArchiveException;
 	}
 
 	public FlatComponentArchiver(IVirtualComponent aComponent, OutputStream out, List<IFlattenParticipant> fParticipants) {
 		participants = fParticipants;
 		component = aComponent;
 		destinationStream = out;
 		zipOutputStream = new ZipOutputStream(out);
 		flatComponent = getFlatComponent(aComponent);
 	}
 	
 	public FlatComponentArchiver(IVirtualComponent aComponent, OutputStream out, List<IFlattenParticipant> fParticipants, ComponentExportCallback callback) {
 		participants = fParticipants;
 		component = aComponent;
 		destinationStream = out;
 		callbackHandler = callback;
 		zipOutputStream = new ZipOutputStream(out);
 		flatComponent = getFlatComponent(aComponent);
 	}
 	
 	public void close() throws IOException {
 		getDestinationStream().close();
 	}
 
 	public void finish() throws IOException {
 		getZipOutputStream().finish();
 		//If this is not nested, close the stream to free up the resource
 		//otherwise, don't close it because the parent may not be done
 		if (!(getDestinationStream() instanceof ZipOutputStream))
 			getDestinationStream().close();
 	}
 	
 	protected void saveArchive() throws ArchiveSaveFailureException {
 		Exception caughtException = null;
 		try {
 			if (callbackHandler != null && callbackHandler.canSave(getComponent())) {
 				callbackHandler.saveArchive(getComponent(), getZipOutputStream());
 			}
 			else {	
 				IFlatResource[] resources = getFlatComponent().fetchResources();
 				saveManifest(Arrays.asList(resources));
 				saveChildModules(getFlatComponent().getChildModules());
 				saveFlatResources(resources);
 			}
 		} catch (Exception e){
 			caughtException = e;
 		} finally {
 			try {
 				finish();
 			} catch (IOException e) {
 				throw new ArchiveSaveFailureException(e);
 			} finally {
 				if (caughtException != null){
 					throw new ArchiveSaveFailureException(caughtException);
 				}
 			}
 		}
 	}
 	
 	protected FlatComponentArchiver saveNestedArchive(IVirtualComponent component, IPath entry) throws IOException {
 		ZipEntry nest = new ZipEntry(entry.toString());
 		getZipOutputStream().putNextEntry(nest);
 		return new FlatComponentArchiver(component, getZipOutputStream(), getParticipants(), callbackHandler);
 	}
 
 	protected void saveFlatResources(IFlatResource[] resources) throws ArchiveSaveFailureException {
 		for (int i = 0; i < resources.length; i++) {
 			IFlatResource resource = resources[i];
 			IPath entryPath = resource.getModuleRelativePath().append(resource.getName());
 			if (resource instanceof IFlatFile) {
 				if (!isManifest(entryPath) && !zipEntries.contains(entryPath)) {
 					addZipEntry(resource, entryPath);
 				}
 			} else if (resource instanceof IFlatFolder) {
 				if (shouldInclude(entryPath)) {
 					addZipEntry(resource, entryPath);
 					saveFlatResources(((IFlatFolder)resource).members());
 				}
 			}
 		}
 	}
 
 	protected boolean shouldInclude(IPath entryPath) {
 		if (entryPath.equals(new Path(IModuleConstants.DOT_SETTINGS))) {
 			return false;
 		}
 		return true;
 	}
 
 	protected void saveChildModules(IChildModuleReference[] childModules) throws ArchiveSaveFailureException, IOException {
 		for (int i = 0; i < childModules.length; i++) {
 			IChildModuleReference childModule = childModules[i];
 			IPath entryPath = childModule.getRelativeURI();
 			zipEntries.add(entryPath);
 			FlatComponentArchiver saver = saveNestedArchive(childModule.getComponent(), entryPath);
 			saver.saveArchive();
 		}
 	}
 
 	protected void addZipEntry(IFlatResource f, IPath entryPath) throws ArchiveSaveFailureException {
 		try {
 			IPath path = entryPath;
 			boolean isFolder = false;
 			long lastModified;
 			
 			if (f instanceof IFlatFolder) {
 				isFolder = true;
 				File folder = (File)((IFlatFolder)f).getAdapter(File.class);
 				lastModified = folder.lastModified();
 				if (!path.hasTrailingSeparator())
 					path = path.addTrailingSeparator();
 			}
 			else {
 				lastModified = ((IFlatFile) f).getModificationStamp();
 			}
 			ZipEntry entry = new ZipEntry(path.toString());
 			if (lastModified > 0)
 				entry.setTime(lastModified);
 			
 			getZipOutputStream().putNextEntry(entry);
 			if (!isFolder) {
 				ArchiveUtil.copy((InputStream) f.getAdapter(InputStream.class), getZipOutputStream());
 			}
 			getZipOutputStream().closeEntry();
 		} catch (IOException e) {
 			throw new ArchiveSaveFailureException(e);
 		}
 	}
 	
 
 	/**
 	 * The FlatVirtualComponent model is what does the grunt of the work
 	 * @return
 	 */
 	protected IFlatVirtualComponent getFlatComponent(IVirtualComponent component) {
 		FlatComponentTaskModel options = new FlatComponentTaskModel();
 		options.put(FlatVirtualComponent.PARTICIPANT_LIST, getParticipants());
 		return new FlatVirtualComponent(component, options);
 	}
 	
 	protected List<IFlattenParticipant> getParticipants() {
 		return participants;
 	}
 
 	protected java.util.zip.ZipOutputStream getZipOutputStream() {
 		return zipOutputStream;
 	}
 
 	private void saveManifest(List<IFlatResource> resources) throws ArchiveSaveFailureException {
 		IFlatFolder metainf = (FlatFolder)VirtualComponentFlattenUtility.getExistingModuleResource(resources, new Path(J2EEConstants.META_INF));
 		IFlatFile manifest = null;
 		
 		if (metainf != null) {
 			IFlatResource[] children = metainf.members();
 			for (int i = 0; i < children.length; i++) {
 				if (children[i].getName().equals(J2EEConstants.MANIFEST_SHORT_NAME)) {
 					manifest = (IFlatFile) children[i];
 					IPath entryPath = manifest.getModuleRelativePath().append(manifest.getName());
 					addZipEntry(manifest, entryPath);
 					break;
 				}
 			}
 		}
 		if (manifest == null) {
 			createManifest();
 		}
 	}
 
 	private void createManifest() throws ArchiveSaveFailureException {
 		String manifestContents = "Manifest-Version: 1.0\r\n\r\n"; //$NON-NLS-1$
 		try {
 			ZipEntry entry = new ZipEntry(J2EEConstants.MANIFEST_URI);
 			getZipOutputStream().putNextEntry(entry);
 			ArchiveUtil.copy(new ByteArrayInputStream(manifestContents.getBytes()), getZipOutputStream());
 		} catch (IOException e) {
 			throw new ArchiveSaveFailureException(e);
 		}
 	}
 
 	private boolean isManifest(IPath path) {
 		if (path.equals(new Path(J2EEConstants.MANIFEST_URI))) {
 			return true;
 		}
 		return false;
 	}
 
 	public java.io.OutputStream getDestinationStream() {
 		return destinationStream;
 	}
 
 	public IVirtualComponent getComponent() {
 		return component;
 	}
 
 	public IFlatVirtualComponent getFlatComponent() {
 		return flatComponent;
 	}
 
 
 }
