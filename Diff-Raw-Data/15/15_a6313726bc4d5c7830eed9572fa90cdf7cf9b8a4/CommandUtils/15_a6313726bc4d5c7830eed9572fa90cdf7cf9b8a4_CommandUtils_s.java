 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2010 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements various utility methods used in the context-
  * sensitive commands
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.utils;
 
 import java.io.InputStream;
 import java.net.URISyntaxException;
 
 import org.eclipse.cdt.core.model.ICElement;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EContextType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileVersion;
 import org.eclipse.mylyn.reviews.r4e.core.model.RModelFactory;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.IRFSRegistry;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.RFSRegistryFactory;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.ReviewsFileStorageException;
 import org.eclipse.mylyn.reviews.r4e.core.utils.ResourceUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.Activator;
 import org.eclipse.mylyn.reviews.r4e.ui.editors.R4ECompareEditorInput;
 import org.eclipse.mylyn.reviews.r4e.ui.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.model.R4EUITextPosition;
 import org.eclipse.mylyn.versions.core.ChangeType;
 import org.eclipse.mylyn.versions.core.ScmArtifact;
 import org.eclipse.mylyn.versions.core.ScmCore;
 import org.eclipse.mylyn.versions.core.spi.ScmConnector;
 import org.eclipse.team.core.TeamException;
 import org.eclipse.team.core.history.IFileRevision;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class CommandUtils {
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 	
 	/**
 	 * Method getTargetFileURI.
 	 * @return URI 
 	 * @throws URISyntaxException 
 	 * @throws CoreException
 	 * @throws ReviewsFileStorageException 
 	 */
 	public static R4EFileVersion getTargetFileData() throws CoreException, ReviewsFileStorageException {
 		final IEditorInput input = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput(); // $codepro.audit.disable methodChainLength
 		IFile editorFile = null;   //The file we use in the current editor/workspace
 		if (input instanceof IFileEditorInput) {
 			editorFile = ((IFileEditorInput) input).getFile();
 			//Now we have the file in editor, we need to use the versions interface to see if we should copy it
 			//to the local repo and update model info
 			return updateTargetFile(editorFile);
 		} else if (input instanceof R4ECompareEditorInput) {
 			//If we get here, this is because we are trying to act on the compare editor contents
 			//this means that the file we are acting on is already in the local repository
 			//in this case, we only need to provide the versionId of this file
 			return ((R4ECompareEditorInput)input).getLeftElementVersion();
 		} else {
 			//Should never happen
 			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, 
 					"Invalid input " + input.getClass().toString() , null));
 		}
 	}
 	
 	/**
 	 * Method updateTargetFile.
 	 * @param aFile IFile
 	 * @return IFile 
 	 * @throws CoreException
 	 * @throws ReviewsFileStorageException 
 	 */
 	public static R4EFileVersion updateTargetFile(IFile aFile) throws CoreException,
 			ReviewsFileStorageException {
 
 		// Get handle to local storage repository
 		final IRFSRegistry localRepository = RFSRegistryFactory
 				.getRegistry(R4EUIModelController.getActiveReview().getReview());
 		final String workspaceFileVersion = localRepository.blobIdFor(aFile
 				.getContents());
 
 		// Get Remote repository file info
 		final ScmConnector connector = ScmCore.getConnector(aFile.getProject());
 		if (null != connector) {
 
 			final ScmArtifact artifact = connector.getArtifact(aFile);
 			if (null != artifact) { // No remote file detected, just return the
 									// workspace file
 
 				// Check if the file in workspace if the same as the file in the
 				// remote repository
 				if (artifact.getId().equals(workspaceFileVersion)) {
 					// Files are different, so fetch file from remote repo and
 					// copy it to the temp work area
					//TODO create temp R4EFileVersion
 					return copyRemoteFileToLocalRepository(localRepository, artifact);
 				}
 				// else Files are the same so we can copy the file in the
 				// workspace to the local repository, see below
 			}
 		} // else this is the case where files are not in source control
 
 		// The current file was modified by the user, so we need to copy the
 		// current file in the workspace to the work area
		//TODO create temp R4EFileVersion
 		return copyWorkspaceFileToLocalRepository(localRepository, aFile);
 	}
 
 	/**
 	 * Method getBaseFileURI.
 	 * @return IFile 
 	 * @throws URISyntaxException
 	 * @throws CoreException
 	 * @throws ReviewsFileStorageException 
 	 */
 	public static R4EFileVersion getBaseFileData() throws CoreException, ReviewsFileStorageException {
 		final IEditorInput input = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput(); // $codepro.audit.disable methodChainLength
 		IFile editorFile = null;   //The file we use in the current editor/workspace
 		if (input instanceof IFileEditorInput) {
 			editorFile = ((IFileEditorInput) input).getFile();
 			//Now we have the file in editor, we need to use the versions interface to see if we should copy it
 			//to the local repo and update model info
 			return updateBaseFile(editorFile);
 		} else if (input instanceof R4ECompareEditorInput) {
 			//If we get here, this is because we are trying to act on the compare editor contents
 			//this means that the file we are acting on is already in the local repository
 			//in this case, we only need to provide the versionId of this file
 			return ((R4ECompareEditorInput)input).getRightElementVersion();
 		} else {
 			//Should never happen
 			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, 
 					"Invalid input " + input.getClass().toString() , null));
 		}
 	}
 	
 	/**
 	 * Method updateBaseFile.
 	 * @param aFile IFile
 	 * @return IFile 
 	 * @throws CoreException
 	 * @throws ReviewsFileStorageException 
 	 */
 	public static R4EFileVersion updateBaseFile(IFile aFile) throws CoreException, ReviewsFileStorageException {
 		if (!isFileInSynch(aFile)) { 
 
 			// Get handle to local storage repository
 			final IRFSRegistry localRepository = RFSRegistryFactory.getRegistry(R4EUIModelController.getActiveReview().getReview());
 
 			//Get Remote repository file info
 			final ScmConnector connector = ScmCore.getConnector(aFile.getProject());
 			if (null != connector) {
 				final ScmArtifact artifact = connector.getArtifact(aFile);
 				if (null != artifact) {
 					//File was modified, so we need to fetch the base file from the versions repository and copy it to our own local repository
					//TODO create temp R4EFileVersion
 					return copyRemoteFileToLocalRepository(localRepository, artifact);
 				}
 			}
 		}
 		//File was not modified, or No Version Control System or Remote File detected, so there is no base
 		return null;
 	}
 
 	/**
 	 * Method isFileInSynch.
 	 * @param aFile IFile
 	 * @return boolean 
 	 * @throws TeamException
 	 */
 	public static boolean isFileInSynch(IFile aFile) {
 		// TODO: Later optimization
 		// RepositoryProvider provider =
 		// RepositoryProvider.getProvider(aFile.getProject());
 		// Subscriber subscriber = provider.getSubscriber();
 		// SyncInfo info = subscriber.getSyncInfo(aFile);
 		// return SyncInfo.isInSync(info.getKind());
 		return false;
 	}
 	
 	/**
 	 * Method copyRemoteFileToLocalRepository.
 	 * @param aLocalRepository IRFSRegistry
 	 * @param aArtifact ScmArtifact
 	 * @return IFile 
 	 * @throws CoreException
 	 * @throws ReviewsFileStorageException 
 	 */
 	public static R4EFileVersion copyRemoteFileToLocalRepository(IRFSRegistry aLocalRepository, ScmArtifact aArtifact) 
 	throws CoreException, ReviewsFileStorageException {
 
 		final IFileRevision fileRev = aArtifact.getFileRevision(null);
 		
 		// Pull file from the version control system
 		final InputStream iStream = fileRev.getStorage(null).getContents();
 		
 		//Create and Set value in temporary File version
 		final R4EFileVersion tmpFileVersion = RModelFactory.eINSTANCE.createR4EFileVersion();
 		updateFileVersion(tmpFileVersion, aArtifact);
 		
 		// Push a local copy to local review repository, and obtain the local id
 		tmpFileVersion.setLocalVersionID(aLocalRepository.registerReviewBlob(iStream));
 
 		return tmpFileVersion;
 	}
 	
 	/**
 	 * Method copyWorkspaceFileToLocalRepository.
 	 * @param aLocalRepository IRFSRegistry
 	 * @param aFile IFile
 	 * @return IFile 
 	 * @throws CoreException
 	 * @throws ReviewsFileStorageException 
 	 */
 	public static R4EFileVersion copyWorkspaceFileToLocalRepository(IRFSRegistry aLocalRepository, IFile aFile)
 	throws CoreException, ReviewsFileStorageException {
 		
 		//Create and Set value in temporary File version
 		final R4EFileVersion tmpFileVersion = RModelFactory.eINSTANCE.createR4EFileVersion();
 		updateFileVersion(tmpFileVersion, aFile);
 
 		// Push a local copy to local review repository, and obtain the local id
 		tmpFileVersion.setLocalVersionID(aLocalRepository.registerReviewBlob(aFile.getContents()));
 		
 		return tmpFileVersion;
 	}
 	
 	/**
 	 * Method copyWorkspaceFileToLocalRepository.
 	 * @param aStream InputStream
 	 * @param aFilename String
 	 * @return IFile 
 	 * @throws CoreException
 	 */
 	public static IFile createTempFile(InputStream aStream, String aFilename) throws CoreException {
 		
 		IFile file = null;
 		
 		//TODO For now we use a dummy project in the workspace to store the temp files.  This should be improved later
 		//IPath path = Activator.getDefault().getStateLocation().addTrailingSeparator().append("temp");
 		//IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
 		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(R4EUIConstants.R4E_TEMP_PROJECT);
 		if (!project.exists()) project.create(null);
 		if (!project.isOpen()) project.open(null);
 		final IFolder folder = project.getFolder(R4EUIConstants.R4E_TEMP_FOLDER);
 		if (!folder.exists()) folder.create(IResource.NONE, true, null);
 		file = folder.getFile(aFilename);
 		
 		//Always start from fresh copy because we never know what the temp file version is
 		if (file.exists()) file.delete(true, null);
 		file.create(aStream, IResource.NONE, null);
 		
 		return file;
 	}
 	
 	/**
 	 * Method getPosition.
 	 * @param aSelection ITextSelection
 	 * @return TextPosition
 	 */
 	public static R4EUITextPosition getPosition(ITextSelection aSelection) {
 		return new R4EUITextPosition(aSelection.getOffset(), (aSelection).getLength(), 
 				(aSelection).getStartLine(), (aSelection).getEndLine());
 	}
 	
 	/**
 	 * Method getPosition.
 	 * 		Get position for generic workspace files
 	 * @param aSelectedElement IFile
 	 * @return TextPosition
 	 * @throws CoreException
 	 */
 	public static R4EUITextPosition getPosition(IFile aSelectedElement) throws CoreException { // $codepro.audit.disable overloadedMethods
 		final R4EUITextPosition position = new R4EUITextPosition(R4EUIConstants.NO_OFFSET, R4EUIConstants.INVALID_VALUE, aSelectedElement);
 		position.setName(aSelectedElement.getName());
 		return position;
 	}
 	
 	/**
 	 * Method getPosition.
 	 * 		Get position for workspace java source files
 	 * @param aSelectedElement org.eclipse.jdt.core.ISourceReference
 	 * @param aFile IFile
 	 * @return TextPosition
 	 * @throws CoreException
 	 */
 	public static R4EUITextPosition getPosition(org.eclipse.jdt.core.ISourceReference aSelectedElement, IFile aFile) 
 		throws CoreException {
 		final R4EUITextPosition position = new R4EUITextPosition(aSelectedElement.getSourceRange().getOffset(), 
 				aSelectedElement.getSourceRange().getLength(), aFile);
 		position.setName(((IJavaElement)aSelectedElement).getElementName());
 		return position;	
 	}
 
 	/**
 	 * Method getPosition.
 	 * 		Get position for workspace C and C++ source files
 	 * @param aSelectedElement org.eclipse.cdt.core.model.ISourceReference
 	 * @param aFile IFile
 	 * @return TextPosition
 	 * @throws CoreException
 	 */
 	public static R4EUITextPosition getPosition(org.eclipse.cdt.core.model.ISourceReference aSelectedElement, IFile aFile)  // $codepro.audit.disable overloadedMethods
 		throws CoreException {
 		final R4EUITextPosition position = new R4EUITextPosition(aSelectedElement.getSourceRange().getStartPos(), 
 				aSelectedElement.getSourceRange().getLength(), aFile);
 		position.setStartLine(aSelectedElement.getSourceRange().getStartLine());
 		position.setEndLine(aSelectedElement.getSourceRange().getEndLine());
 		position.setName(((ICElement)aSelectedElement).getElementName());
 		return position;
 	}
 	
 	/**
 	 * Adapt change types from Mylyn Versions to R4E model
 	 * @param changeType ChangeType
 	 * @return
 	 */
 	public static R4EContextType adaptType(ChangeType changeType) {
 		R4EContextType dtype = null;
 		switch (changeType) {
 			case ADDED:
 				dtype = R4EContextType.R4E_ADDED;
 				break;
 			case DELETED:
 				dtype = R4EContextType.R4E_DELETED;
 				break;
 			case MODIFIED:
 				dtype = R4EContextType.R4E_MODIFIED;
 				break;
 			case REPLACED:
 				dtype = R4EContextType.R4E_REPLACED;
 				break;
 			default:
 				break;
 		}
 		return dtype;
 	}
 	
 	
 	/**
 	 * Resolve and transfer values from ScmArtifact to R4EFileVersion
 	 * @param aTargetFileVer R4EFileVersion
 	 * @param aSourceFileVer R4EFileVersion
 	 * @param localId
 	 */
 	public static void copyFileVersionData(R4EFileVersion aTargetFileVer, R4EFileVersion aSourceFileVer) {
 		
 		aTargetFileVer.setName(aSourceFileVer.getName());
 		aTargetFileVer.setVersionID(aSourceFileVer.getVersionID());
 		aTargetFileVer.setRepositoryPath(aSourceFileVer.getRepositoryPath());
 		aTargetFileVer.setLocalVersionID(aSourceFileVer.getLocalVersionID());
 		aTargetFileVer.setPlatformURI(aSourceFileVer.getPlatformURI());
 		aTargetFileVer.setResource(aSourceFileVer.getResource());
 	}
 	
 	/**
 	 * Method updateFileVersion
 	 * @param aTargetFileVer R4EFileVersion
 	 * @param aScmArt ScmArtifact
 	 */
 	public static void updateFileVersion(R4EFileVersion aTargetFileVer, ScmArtifact aScmArt) {
 
 		aTargetFileVer.setName(aScmArt.getFileRevision(null).getName());
 		aTargetFileVer.setVersionID(aScmArt.getId());
 		aTargetFileVer.setRepositoryPath(aScmArt.getPath());
 
		final String projPath = aScmArt.getProjectRelativePath();
		if (null == projPath) {
			Activator.Ftracer.traceDebug("Invalid relative project path in scmArtifact with path: " + 
 					aScmArt.getPath());
 		}
 		final IProject project = ResourceUtils.getProject(aScmArt.getProjectName());
		final IResource resource = ResourceUtils.findResource(project, projPath);
 
 		aTargetFileVer.setPlatformURI(ResourceUtils.toPlatformURIStr(resource));
 		aTargetFileVer.setResource(resource);
 
 		final String projPlatformURI = ResourceUtils.toPlatformURIStr(project);
 		if (null == projPlatformURI) {
 			Activator.Ftracer.traceDebug("Unable to resolve the project: " + aScmArt.getProjectName() + 
 					" platform's URI, in scmArtifact with path: " + aScmArt.getPath());
 		}
 	}
 	
 	/**
 	 * Method updateFileVersion
 	 * @param aTargetFileVer R4EFileVersion
 	 * @param aSrcFile IFile
 	 */
 	public static void updateFileVersion(R4EFileVersion aTargetFileVer, IFile aSrcFile) {
 
 		aTargetFileVer.setName(aSrcFile.getName());
 		aTargetFileVer.setRepositoryPath(aSrcFile.getLocation().toPortableString());
 		aTargetFileVer.setResource(aSrcFile);
 		aTargetFileVer.setPlatformURI(ResourceUtils.toPlatformURIStr(aSrcFile));
 	}
 }
