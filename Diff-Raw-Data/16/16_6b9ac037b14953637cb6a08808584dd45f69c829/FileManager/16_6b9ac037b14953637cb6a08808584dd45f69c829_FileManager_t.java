 package se.kth.ssvl.tslab.wsn.general.servlib.storage;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
import java.io.Serializable;
 
 import se.kth.ssvl.tslab.wsn.general.bpf.BPF;
 
public class FileManager implements Serializable{

	private static final long serialVersionUID = -7098934602546011778L;
 
 	/**
 	 * The directory for which this instance will operate within
 	 */
 	private File dir;
 
 	/**
 	 * The tag for the logger
 	 */
 	private String TAG;
 
 	/**
 	 * The constructor will create a class which can be used by any object to
 	 * manage its files. The dir used will be created if it doesn't exist.
 	 * 
 	 * @param subDir
 	 *            The directory to operate within, which is a subdir to the root
 	 *            path configured in the configuration.
 	 * @param tag
 	 *            The tag for the logger when the FileManager is used.
 	 *            Preferably something like: YouClass-FileManager
 	 * @throws FileNotFoundException
 	 *             Constructor will throw this when the directory wasn't able to
 	 *             be created.
 	 */
 	public FileManager(String subDir, String tag) throws FileNotFoundException {
 		dir = new File(trailingSlashPath(BPF.getInstance().getConfig()
 				.storage_setting().storage_path())
 				+ subDir);
 		TAG = tag + "/" + dir.getName();
 
 		if (!dir.exists()) {
 			if (!dir.mkdir()) {
 				throw new FileNotFoundException(
 						"Couldn't create the subdirectory");
 			}
 		}
 	}
 
 	/**
 	 * This method will create a file within this instance's directory. Will
 	 * return existing file if exists.
 	 * 
 	 * @param fileName
 	 *            The filename of the file created.
 	 * @return The file that will be created. If there was an error or if the
 	 *         file exists, the method will return null
 	 */
 	public File createFile(String fileName) {
 		File f = new File(dir, fileName);
 		try {
 			if (f.exists()) {
 				return f;
 			} else {
 				if (f.createNewFile()) {
 					BPF.getInstance().getBPFLogger().debug(TAG,
 							"Successfully created file :"+ f.getAbsolutePath());
 					// TODO: Check that this is returning the filename as well
 					return f;
 				} else {
 					BPF.getInstance().getBPFLogger()
 							.error(TAG, "Error creating the file: " + fileName);
 				}
 			}
 		} catch (IOException e) {
 			BPF.getInstance().getBPFLogger().error(TAG, e.toString());
 		}
 		return null;
 	}
 
 	/**
 	 * Create a temporary file.
 	 * 
 	 * @param prefix
 	 *            The prefix of the filename
 	 * @param suffix
 	 *            The suffix of the filename
 	 * @return Returns a File object if it was successful
 	 */
 	public File createTempFile(String prefix, String suffix) {
 		try {
 			BPF.getInstance().getBPFLogger()
 					.debug(TAG, "Creating temporary file: " + prefix + suffix);
 			return File.createTempFile(prefix, suffix, dir);
 		} catch (IOException e) {
 			BPF.getInstance().getBPFLogger().error(TAG, e.toString());
 		}
 		return null;
 	}
 
 	/**
 	 * Get an existing file.
 	 * 
 	 * @param fileName
 	 *            The name of the file to get
 	 * @return Returns the file or null if the files doesn't exist.
 	 */
 	public File getFile(String fileName) {
 		File f = new File(dir, fileName);
 
 		if (f.exists()) {
 			return f;
 		}
 		return null;
 	}
 
 	/**
 	 * Deletes a file if it exists
 	 * 
 	 * @param fileName
 	 *            The name of the file to delete
 	 * @return Returns true if the file existed and was successfully deleted
 	 *         otherwise false
 	 */
 	public boolean deleteFile(String fileName) {
 		File f = new File(dir, fileName);
 
 		if (f.exists()) {
 			return f.delete();
 		}
 		return false;
 	}
 
 	/**
 	 * This method will delete all files within the subdir of this FileManager.
 	 * 
 	 * @return Returns true if all files were deleted successfully, or otherwise
 	 *         false.
 	 */
 	public boolean deleteFiles() {
 		try {
 			if (dir.exists()) {
 				File[] files = dir.listFiles();
 				for (int i = 0; i < files.length; i++) {
 					files[i].delete();
 				}
 			}
 			return true;
 		} catch (Exception e) {
 			BPF.getInstance()
 					.getBPFLogger()
 					.error(TAG,
 							"Unable to delete files in subdir: "
 									+ dir.getName());
 		}
 		return false;
 	}
 
 	/**
 	 * Get the size of the directory handled by this FileManager
 	 * 
 	 * @return The size in bytes of the directory handled by this FileManager.
 	 *         If there was an error it will return 0.
 	 */
 	public long getSize() {
 		long result = 0;
 		if (dir.exists()) {
 			File[] files = dir.listFiles();
 			for (int i = 0; i < files.length; i++) {
 				result += files[i].length();
 			}
 
 			BPF.getInstance()
 					.getBPFLogger()
 					.debug(TAG,
 							"Total size of " + dir.getName() + " is "
 									+ dir.length() + " bytes");
 			return result;
 		}
 		BPF.getInstance()
 				.getBPFLogger()
 				.error(TAG,
 						"Size Not found for " + dir.getName()
 								+ " (-- SHOULD NOT REACH THIS POINT --)");
 		return 0;
 	}
 
 	/**
 	 * Gets the size of a file stored in the subdir of this FileManager
 	 * 
 	 * @param fileName
 	 *            The filename of the file to look up the size of.
 	 * @return The size of the file if it exists otherwise 0
 	 */
 	public long getFileSize(String fileName) {
 		File f = new File(dir, fileName);
 
 		if (f.exists()) {
 			return f.length();
 		}
 
 		return 0;
 	}
 
 	
 	private String trailingSlashPath(String path) {
 		if (path.endsWith("/")) {
 			return path;
 		} else {
 			return path + "/";
 		}
 	}
 	
 }
