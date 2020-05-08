 package it.unitn.disi.unagi.application.internal.services;
 
 import it.unitn.disi.unagi.application.exceptions.CouldNotCreateFileException;
 import it.unitn.disi.unagi.application.exceptions.CouldNotDeleteFileException;
 import it.unitn.disi.unagi.application.exceptions.CouldNotReadFileException;
 import it.unitn.disi.unagi.application.exceptions.CouldNotSaveFileException;
 import it.unitn.disi.unagi.application.services.IManageFilesService;
 import it.unitn.disi.util.io.FileIOUtil;
 import it.unitn.disi.util.logging.LogUtil;
 
 import java.io.IOException;
 import java.net.URL;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 
 /**
  * Abstract class that implements methods related to file management. Can be extended by any service class whose purpose
  * is to manipulate some kind of file type.
  * 
  * Notice that some methods are protected and should be called by the specific service class, whereas some are public
  * which allow GUI classes to treat any file management service the same way (i.e., to use polymorphism).
  * 
  * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
  * @version 1.0
  */
 public abstract class ManageFilesService implements IManageFilesService {
 	/**
 	 * Internal (protected) method for creating a new file.
 	 * 
 	 * @param progressMonitor
 	 *          The workbench's progress monitor, in case the operation takes a long time.
 	 * @param file
 	 *          The file descriptor that represents a file in an Eclipse project.
 	 * @return The same file descriptor that was passed as argument, but now the actual file has been created.
 	 * @throws CouldNotCreateFileException
 	 *           If there are any problems in the creation of the file.
 	 */
 	protected IFile createNewFile(IProgressMonitor progressMonitor, IFile file) throws CouldNotCreateFileException {
 		String filePath = file.getFullPath().toString();
 		LogUtil.log.info("Creating new file {0}.", filePath); //$NON-NLS-1$
 
 		// Creates the new file in the project.
 		try {
 			file.create(null, true, progressMonitor);
 		}
 		catch (CoreException e) {
 			LogUtil.log.error("Unagi caught an Eclipse exception while trying to create a file {0}.", e, filePath); //$NON-NLS-1$
 			throw new CouldNotCreateFileException(file, e);
 		}
 
 		// Returns the newly created file.
 		return file;
 	}
 
 	/**
 	 * Internal (protected) method for generating a file descriptor for a file that can be created, i.e., the method
 	 * checks if all necessary conditions for file creation are present.
 	 * 
 	 * @param folder
 	 *          The folder descriptor that indicates where the file should be created.
 	 * @param fileName
 	 *          The name of the file to be created.
 	 * @return The file descriptor for a file whose conditions for creation have been verified.
 	 * @throws CouldNotCreateFileException
 	 *           If some condition for creation fails the verification.
 	 */
 	protected IFile generateCreatableFileDescriptor(IFolder folder, String fileName) throws CouldNotCreateFileException {
 		// Creates an IFile reference to it in the specified folder.
 		IFile file = folder.getFile(fileName);
 
 		// Checks that the file can be created.
 		checkCreatableFile(file);
 
 		// If passed the check, returns the file.
 		LogUtil.log.debug("Returning file object after checking it can be created: {0}.", file.getFullPath()); //$NON-NLS-1$
 		return file;
 	}
 
 	/**
 	 * Internal (protected) method for verifying the conditions for file creation: a file with the same name must not
 	 * exist in the folder, the folder should exist and be accessible and, finally, the Eclipse project should exist and
 	 * be open.
 	 * 
 	 * @param file
 	 *          The file descriptor representing a file that we want to check if it can be created.
 	 * @throws CouldNotCreateFileException
 	 *           If some condition for creation fails the verification.
 	 */
 	protected void checkCreatableFile(IFile file) throws CouldNotCreateFileException {
 		IProject project = file.getProject();
 		String filePath = file.getFullPath().toString();
 
 		// Checks if the project really exists.
 		if (!project.exists() || !project.isOpen()) {
 			LogUtil.log.error("Cannot create new file {0}, project doesn't exist: {1}.", filePath, project.getName()); //$NON-NLS-1$
 			throw new CouldNotCreateFileException(file);
 		}
 
 		// Checks that the folder in which the file should be created exists and is accessible.
 		IContainer folder = file.getParent();
 		if ((!(folder instanceof IFolder)) || (!folder.exists()) || (!folder.isAccessible())) {
 			LogUtil.log.error("Cannot create new file {0}. Containing folder doesn't exist or is not accessible.", filePath); //$NON-NLS-1$
 			throw new CouldNotCreateFileException(file);
 		}
 
 		// Checks that a file in this path doesn't yet exist.
 		if (file.exists()) {
 			LogUtil.log.error("Cannot create new file {0}. The file already exists.", filePath); //$NON-NLS-1$
 			throw new CouldNotCreateFileException(file);
 		}
 	}
 
 	/** @see it.unitn.disi.unagi.application.services.IManageFilesService#readFile(org.eclipse.core.resources.IFile) */
 	@Override
 	public String readFile(IFile file) throws CouldNotReadFileException {
 		// Tries to read the contents of the file from its location.
 		try {
 			URL fileURL = file.getLocationURI().toURL();
 			StringBuilder contents = FileIOUtil.readFile(fileURL);
 			return contents.toString();
 		}
 
 		// In case of I/O errors, throws an application exception.
 		catch (IOException e) {
 			throw new CouldNotReadFileException(file, e);
 		}
 	}
 
 	/**
 	 * @see it.unitn.disi.unagi.application.services.IManageFilesService#saveFile(org.eclipse.core.resources.IFile,
 	 *      java.lang.String)
 	 */
 	@Override
 	public void saveFile(IFile file, String contents) throws CouldNotSaveFileException {
 		// Tries to save the contents to the file at its location.
 		try {
 			FileIOUtil.saveFile(file.getLocation().toString(), contents);
 		}
 
 		// In case of I/O errors, throws an application exception.
 		catch (IOException e) {
 			throw new CouldNotSaveFileException(file, e);
 		}
 	}
 
 	/**
 	 * Internal (protected) method for deleting an existing file.
 	 * 
 	 * @param progressMonitor
 	 *          The workbench's progress monitor, in case the operation takes a long time.
 	 * @param file
 	 *          The file descriptor that represents a file in an Eclipse project.
 	 * @throws CouldNotDeleteFileException
 	 *           If there are any problems in the deletion of the file.
 	 */
 	protected void deleteFile(IProgressMonitor progressMonitor, IFile file) throws CouldNotDeleteFileException {
 		// Deletes the file from the workspace.
 		try {
 			file.delete(true, progressMonitor);
 		}
 		catch (CoreException e) {
 			LogUtil.log.error("Unagi caught an Eclipse exception while trying to delete file {0}.", e, file.getFullPath()); //$NON-NLS-1$
 			throw new CouldNotDeleteFileException(file, e);
 		}
 	}
 }
