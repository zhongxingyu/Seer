 package edu.ucla.loni.server;
 
 import java.io.File;
 
 import java.sql.Timestamp;
 
 import java.util.ArrayList;
 
 import edu.ucla.loni.client.FileService;
 import edu.ucla.loni.shared.*;
 
 import com.google.gwt.thirdparty.guava.common.io.Files;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 import org.jdom2.Document;
 
 
 @SuppressWarnings("serial")
 public class FileServiceImpl extends RemoteServiceServlet implements FileService {	
 	////////////////////////////////////////////////////////////
 	// Private Functions
 	////////////////////////////////////////////////////////////
 	/**
 	 *  Recursively get all pipefiles
 	 */
 	private ArrayList<File> getAllPipefiles(ArrayList<File> files, File dir){
 	    if (dir.isDirectory()){
 	    	for (File file : dir.listFiles()){
 	    		getAllPipefiles(files, file);
 	    	}
 	    } else {
 	    	String name = dir.getName();
 	    	if (name.endsWith(".pipe")){
 	    		files.add(dir);	
 	    	}
 	    }
 	    
 	    return files;
 	}
 	
 	/** 
 	 * Gets the directoryID of the root directory by selecting it from the database,
 	 * inserts the directory into the database if needed
 	 * 
 	 * @param absolutePath absolute path of the root directory  
 	 * @return directoryID of the root directory
 	 */
 	private int getDirectoryId(String absolutePath) throws Exception{
 		int ret = Database.selectDirectory(absolutePath);
 		if(ret == -1){
 			Database.insertDirectory(absolutePath);
 			ret = Database.selectDirectory(absolutePath);
 		}
 		return ret;
 	}
 	
 	/**
 	 *  Remove files from the database in the case that they were deleted
 	 */
 	private void removeFiles(int dirId) throws Exception{
 		Pipefile[] pipes = Database.selectPipefiles(dirId);
 		if (pipes != null){
 			for(Pipefile pipe : pipes){
 				File file = new File(pipe.absolutePath);
 				if (file.exists() == false){
 					Database.deletePipefile(pipe);
 				}
 			}
 		}
 	}
 	
 	/**
 	 *  Update the database for this root folder 
 	 *  @param root absolute path of the root directory
 	 */
 	private void updateFiles(File rootDir) throws Exception {		
 		// Clean the database
 		int dirId = getDirectoryId(rootDir.getAbsolutePath());	
 		removeFiles(dirId);
 		
 		// Update all files
 		ArrayList<File> files = getAllPipefiles(new ArrayList<File>(), rootDir);
 				
 		// For each pipefile
 		for (File file : files){	
 			Timestamp db_lastModified = Database.selectPipefileLastModified(file.getAbsolutePath());
 		    
 			// Determine if the row needs to be updated or inserted
 		    boolean update = false;
 		    boolean insert = true;
 		    
 		    Timestamp fs_lastModified = new Timestamp(file.lastModified());
 			
 		    if (db_lastModified != null){
 				insert = false;
 				
 				// If file has been modified
 				if (db_lastModified.equals(fs_lastModified) == false){
 					update = true;
 				}
 			}
 			
 			// If we need to update or insert a row
 		    if (update || insert){			    	
 		    	Pipefile pipe = ServerUtils.parseFile(file);
 				
 				if (insert){
 					Database.insertPipefile(dirId, pipe, fs_lastModified);
 				} else {
 					Database.updatePipefile(pipe, fs_lastModified);
 				}
  		    }
 		}
 	}
 	
 	/**
 	 *  Removes a file from the server
 	 *  @param filename absolute path of the file
 	 */
 	private void removeFile(Pipefile pipe) throws Exception {		
 		File f = new File(pipe.absolutePath);
 		if (f.exists()){
 			f.delete(); // TODO: check return status
 			
 			Database.deletePipefile(pipe);
 			
 			// remove parent if this directory is empty
 			ServerUtils.recursiveRemoveDir(f.getParentFile());
 			
 			//TODO: update access restrictions file
 		}
 	}
 	
 	/**
 	 *  Copy a file from the server to the proper package
 	 *  @param filename absolute path of the file
 	 *  @param packageName absolute path of the package
 	 */
 	private void copyFile(Pipefile pipe, String packageName) throws Exception {
 		// If the file exists
 		//   Copy the file to the new destination
 		//     File must be changed update the package
 		//   Insert a row corresponding to this file in the database
 		
 		
 		
 		// Get old and new absolute path directory
 		String oldAbsolutePath = pipe.absolutePath;
 		File src = new File(oldAbsolutePath);
 		
 		if (!src.exists()) return;
 		
 		String newAbsolutePath = ServerUtils.newAbsolutePath(pipe.absolutePath, packageName, pipe.type);
 		File dest = new File(newAbsolutePath);
 		Files.copy(src, dest);
 		
 		
 		Pipefile newPipe = pipe;
 		
 		// Update Pipefile
 		newPipe.packageName = packageName;
 		newPipe.absolutePath = newAbsolutePath;
 		
 		// Update XML
 		Document doc = ServerUtils.readXML(dest);
 		doc = ServerUtils.update(doc, newPipe, true);
 		ServerUtils.writeXML(dest, doc);
 		
 		// Update Database
 		Database.insertPipefile(
				getDirectoryId(ServerUtils.extractDirName(newAbsolutePath)),
 				newPipe, new Timestamp(dest.lastModified()));
 	}
 	
 	/**
 	 *  Move a file from the server to the proper package
 	 *  @param filename absolute path of the file = source path of file
 	 *  @param packageName is the name of the package as it appears in the Database in column PACKAGENAME
 	 *  @throws Exception 
 	 */
 	public void moveFile(Pipefile pipe, String packageName) throws Exception{		
 		// Get old and new absolute path directory
 		String oldAbsolutePath = pipe.absolutePath;
 		String newAbsolutePath = ServerUtils.newAbsolutePath(pipe.absolutePath, packageName, pipe.type);
 		
 		File src = new File(oldAbsolutePath);
 		File dest = new File(newAbsolutePath);
 		
 		// If the destination directory does not exist, create it and necessary parent directories
 		File destDir = dest.getParentFile();
 		if (destDir.exists() == false){
 			boolean success = destDir.mkdirs();
 			if (!success){
 				throw new Exception("Destination folders could not be created");
 			}
 		}
 		
 		// Move the file
 		boolean success = src.renameTo(dest);
 		if(success == false) {
 			throw new Exception("File could not be moved");
 		}
 		
 		// Update Pipefile
 		pipe.packageName = packageName;
 		pipe.absolutePath = newAbsolutePath;
 		
 		// Update XML
 		Document doc = ServerUtils.readXML(dest);
 		doc = ServerUtils.update(doc, pipe, true);
 		ServerUtils.writeXML(dest, doc);
 		
 		// Update Database
 		Database.updatePipefile(pipe, new Timestamp(dest.lastModified()));
 	}
 	
 	////////////////////////////////////////////////////////////
 	// Public Functions
 	////////////////////////////////////////////////////////////
 	
 	/**
 	 *  Returns a FileTree that represents the root directory
 	 *  <br>
 	 *  Thus the children are the packages
 	 *  @param root the absolute path of the root directory
 	 */
 	public Pipefile[] getFiles(String root) throws Exception {
 		try {
 			File rootDir = new File(root);
 			if (rootDir.exists() && rootDir.isDirectory()){
 				updateFiles(rootDir);
 				
 				int dirId = getDirectoryId(root);
 				
 				return Database.selectPipefiles(dirId);
 			} else {
 				return null;
 			}
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getMessage());
 		}
 	}
 	
 	/**
 	 *  Returns a FileTree where the children are all files and are the search results
 	 *  @param root the absolute path of the root directory
 	 *  @param query what the user is searching for
 	 */
 	public Pipefile[] getSearchResults(String root, String query) throws Exception{
 		try {
 			int dirId = getDirectoryId(root); 
 			return Database.selectPipefilesSearch(dirId, query);
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getMessage());
 		}
 	}
 	
 	/**
 	 *  Updates the file on the server
 	 *  @param pipe Pipefile representing the updated file
 	 */
 	public void updateFile(Pipefile pipe) throws Exception{
 		try {
 			// Update the XML
 			File file = new File(pipe.absolutePath);
 			if (!file.exists() || !file.canRead())
 				return;
 			
 			Document doc = ServerUtils.readXML(file);
 			doc = ServerUtils.update(doc, pipe, false);
 			ServerUtils.writeXML(file, doc);
 			
 			// TODO if packageChanged, move file
 			// TODO update the database
 			// TODO rewrite the access file
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getMessage());
 		}
 
 	}
 	
 	/**
 	 *  Removes files from the server
 	 *  @param filenames absolute paths of the files
 	 * @throws SQLException 
 	 */
 	public void removeFiles(Pipefile[] pipes) throws Exception {
 		try {
 			for (Pipefile pipe : pipes) {
 				removeFile(pipe);
 			}
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getMessage());
 		}
 	}
 	
 	/**
 	 *  Copies files from the server to the proper package
 	 *  @param filenames absolute paths of the files
 	 *  @param packageName absolute path of the package
 	 */
 	public void copyFiles(Pipefile[] pipes, String packageName) throws Exception {		
 		try {
 			for (Pipefile pipe : pipes) {
 				copyFile(pipe, packageName);
 			}
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getMessage());
 		}
 	}
 	
 	/**
 	 *  Moves files from the server to the proper package
 	 *  @param filenames absolute paths of the files
 	 *  @param packageName absolute path of the package
 	 *  @throws Exception 
 	 */
 	public void moveFiles(Pipefile[] pipes, String packageName) throws Exception{
 		try {
 			for (Pipefile pipe : pipes) {
 				moveFile(pipe, packageName);
 			}
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new Exception(e.getMessage());
 		}
 	}
 	
 	/**
 	 *  Returns an array of all the groups
 	 */
 	public Group[] getGroups() throws Exception {
 		// TODO
 		// Call the database for a list of groups
 		// Convert to proper format
 		return Database.selectGroups();
 	}
 	
 	/**
 	 *  Updates a group on the server (also used for creating groups)
 	 *  @param group group to be updated
 	 */
 	public void	updateGroup(Group group) throws Exception{
 		// TODO
 		// Update the row in the database corresponding to this group
 		Database.updateGroup(group);
 	}
 }
 
