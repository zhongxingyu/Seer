 package edu.ucla.loni.server;
 
 import java.io.File;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Timestamp;
 
 import java.util.ArrayList;
 
 import edu.ucla.loni.client.FileService;
 import edu.ucla.loni.shared.*;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 import org.jdom2.Document;
 
 
 @SuppressWarnings("serial")
 public class FileServiceImpl extends RemoteServiceServlet implements FileService {
 	////////////////////////////////////////////////////////////
 	// Private Variables
 	////////////////////////////////////////////////////////////
 	private Connection db_connection;
 	private String db_name = "jdbc:hsqldb:hsql://localhost/xdb";
 	private String db_username = "SA";
 	private String db_password = "";
 	
 	////////////////////////////////////////////////////////////
 	// Private Database Functions
 	////////////////////////////////////////////////////////////
 	/**
 	 *  Returns a connection to the database
 	 */
 	private Connection getDatabaseConnection() throws Exception {
 		if (db_connection == null){
 			Class.forName("org.hsqldb.jdbcDriver");
 			db_connection = DriverManager.getConnection(db_name, db_username, db_password);
 		}
 		
 		return db_connection;
 	}
 	
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
 		int ret = selectDirectoryId(absolutePath);
 		if(ret == -1){
 			insertDirectoryId(absolutePath);
 			ret = selectDirectoryId(absolutePath);
 		}
 		return ret;
 	}
 	
 	/** 
 	 * @param absolutePath absolute path of the root directory  
 	 * @return directoryID of the root directory, or -1 if not found
 	 */
 	private int selectDirectoryId(String absolutePath) throws Exception{
 		Connection con = getDatabaseConnection();
 		
 		PreparedStatement stmt = con.prepareStatement(
 			"SELECT directoryID " +
 			"FROM directory " +		
 			"WHERE absolutePath = ?"		
 		);
 		stmt.setString(1, absolutePath);
 		ResultSet rs = stmt.executeQuery();
 		
 		if (rs.next()){
 			return rs.getInt(1);
 		} else {
 			return -1;
 		}
 	}
 	
 	
 	/** 
 	 * Inserts directory into the database, directoryID is automatically generated
 	 * 
 	 * @param absolutePath absolute path of the root directory  
 	 */
 	private void insertDirectoryId(String absolutePath) throws Exception{
 		Connection con = getDatabaseConnection();
 		
 		PreparedStatement stmt = con.prepareStatement(
 			"INSERT INTO directory (absolutePath) " +
 			"VALUES (?)" 		
 		);
 		stmt.setString(1, absolutePath);
 		stmt.executeUpdate();
 	}
 	
 	/**
 	 * ResultSet is from a query with the following form 
 	 *   SELECT * FROM pipefile WHERE ...
 	 */
 	private Pipefile[] resultSetToPipefileArray(ResultSet rs) throws Exception{
 		ArrayList<Pipefile> list = new ArrayList<Pipefile>();
 		while (rs.next()) {
 			Pipefile p = new Pipefile();
 			
 			// directoryID at index 1
 			p.absolutePath = rs.getString(2);
 			// lastModified at index 3
 			p.name = rs.getString(4);
 			p.type = rs.getString(5);
 			p.packageName = rs.getString(6);
 			p.description = rs.getString(7);
 			p.tags = rs.getString(8);
 			p.location = rs.getString(9);
 			p.uri = rs.getString(10);
 			
 			p.access = rs.getString(11);
 			
 			list.add(p);
 		}
 		
 		Pipefile[] ret = new Pipefile[list.size()];
 		return list.toArray(ret);
 	}
 	
 	/**
 	 *  Update the database for this root folder 
 	 *  @param root absolute path of the root directory
 	 */
 	private void updateDatabase(File rootDir) throws Exception {
 		// Get all pipefiles recursively under this folder
 		ArrayList<File> files = getAllPipefiles(new ArrayList<File>(), rootDir);
 		
 		if (files.size() > 0){
 			Connection con = getDatabaseConnection();
 			 
 			int dirID = getDirectoryId(rootDir.getAbsolutePath());
 			
 			// For each pipefile
 			for (File file : files){			    
 			    // Get the lastModified of this pipefile to determine if database is up-to-date
 			    PreparedStatement stmt = con.prepareStatement(
 			    	"SELECT lastModified " +
 					"FROM pipefile " +
 					"WHERE absolutePath = ?" 		
 				);
 			    stmt.setString(1, file.getAbsolutePath());
 				ResultSet rs = stmt.executeQuery();
 			    
 				// Determine if the row needs to be updated or inserted
 			    boolean update = false;
 			    boolean insert = true;
 			    
 			    Timestamp fs_lastModified = new Timestamp(file.lastModified());
 				
 			    if (rs.next()){
 					insert = false;
 					
 					Timestamp db_lastModified = rs.getTimestamp(1);
 					
 					// If file has been modified
 					if (db_lastModified.equals(fs_lastModified) == false){
 						update = true;
 					}
 				}
 				
 				// If we need to update or insert a row
 			    if (update || insert){			    	
 			    	Pipefile pipe = ServerUtils.parseFile(file);
 					
 					if (insert){
 						/*
 						 * database schema for pipefile
 						 */
 						
 						stmt = con.prepareStatement(
 							"INSERT INTO pipefile (" +
 								"directoryID, absolutePath, lastModified, " +
 								"name, type, packageName, description, tags, " +
 								"location, uri, access) " +
 							"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
 						);
 						stmt.setInt(1, dirID);
 						stmt.setString(2, pipe.absolutePath);
 						stmt.setTimestamp(3, fs_lastModified);
 						stmt.setString(4, pipe.name);
 						stmt.setString(5, pipe.type);
 						stmt.setString(6, pipe.packageName);
 						stmt.setString(7, pipe.description);
 						stmt.setString(8, pipe.tags);
 						stmt.setString(9, pipe.location);
 						stmt.setString(10, pipe.uri);
 						stmt.setString(11, ""); // access
 					} else {
 						// directoryID and access are not based on the file in the system
 						stmt = con.prepareStatement(
 							"UPDATE pipefile " +
 						    "SET name = ?, type = ?, packageName = ?, description = ?, tags = ?, " +
 						    "location = ?, uri = ?, lastModified = ? " +
 							"WHERE absolutePath = ?"
 						);
 						stmt.setString(1, pipe.name);
 						stmt.setString(2, pipe.type);
 						stmt.setString(3, pipe.packageName);
 						stmt.setString(4, pipe.description);
 						stmt.setString(5, pipe.tags);
 						stmt.setString(6, pipe.location);
 						stmt.setString(7, pipe.uri);
 						stmt.setTimestamp(8, fs_lastModified);
 						stmt.setString(9, pipe.absolutePath);
 					}
 					stmt.executeUpdate();
 	 		    }
 			}
 		}
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
 				updateDatabase(rootDir);
 				
 				int dirID = getDirectoryId(root);
 				
 				Connection con = getDatabaseConnection();
 				PreparedStatement stmt = con.prepareStatement(
 			    	"SELECT * " +
 					"FROM pipefile " +
 					"WHERE directoryID = ?" 		
 				);
 			    stmt.setInt(1, dirID);
 				ResultSet rs = stmt.executeQuery();
 				
 				return resultSetToPipefileArray(rs);
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
 			// Convert to lower case so can test case insensitive
 			query = query.toLowerCase();
 			
 			Connection con = getDatabaseConnection();
 			int dirID = getDirectoryId(root);
 			PreparedStatement stmt = con.prepareStatement(
 				"SELECT * " +
 				"FROM pipefile " +
 				"WHERE directoryID = ? " +
 					"AND (LCASE(name) LIKE '%" + query + "%' " +
 					     "OR LCASE(packageName) LIKE '%" + query + "%'" +
 					     "OR LCASE(description) LIKE '%" + query + "%'" +
 					     "OR LCASE(tags) LIKE '%" + query + "%')" 
 					//DO NOT CHANGE
 					//for some reason on my computer making later setString substitution
 					//was not producing the right result, i.e. not finding items in
 					//database. My guess the setString was not formatting correctly.
 					     
 					// setString should now have single quotes surrounding it
 					// Right now this code is subject to SQL injection, need to use setString
 			);
 			stmt.setInt(1, dirID);
 			//stmt.setString(2, "'%" + query + "%'");
 			ResultSet rs = stmt.executeQuery();
 			
 			return resultSetToPipefileArray(rs);
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
 			
 			Document doc = ServerUtils.parseXML(file);
 			doc = ServerUtils.update(doc, pipe, false);
 			ServerUtils.write(file, doc);
 			
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
 	 *  Removes a file from the server
 	 *  @param filename absolute path of the file
 	 */
 	private void removeFile(Pipefile pipe) throws Exception {		
 		File f = new File(pipe.absolutePath);
 		if (f.exists()){
 			f.delete(); // TODO: check return status
 			Connection con = getDatabaseConnection();
 			
 			PreparedStatement stmt = con.prepareStatement(
 				"DELETE FROM pipefile " +
 				"WHERE absolutePath = ?" 		
 			);
 			stmt.setString(1, pipe.absolutePath);
 			stmt.executeUpdate();
 			//TODO: update access restrictions file
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
 	 *  Copy a file from the server to the proper package
 	 *  @param filename absolute path of the file
 	 *  @param packageName absolute path of the package
 	 */
 	private void copyFile(Pipefile pipe, String packageName) throws Exception {
 		// TODO
 		// If the file exists
 		//   Copy the file to the new destination
 		//     File must be changed update the package
 		//   Insert a row corresponding to this file in the database
 		// do we insert or update? isnt this file already in db?
 		return;	
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
 	 *  Move a file from the server to the proper package
 	 *  @param filename absolute path of the file = source path of file
 	 *  @param packageName is the name of the package as it appears in the Database in column PACKAGENAME
 	 *  @throws Exception 
 	 */
 	public void moveFile(Pipefile pipe, String packageName) throws Exception{		
 		// Get destination directory, create if necessary
 		String root = ServerUtils.extractDirName(ServerUtils.extractDirName(ServerUtils.extractDirName(pipe.absolutePath)));
 		String filename = ServerUtils.extractFileName(pipe.absolutePath);
 		
 		String dest_dir = root + File.separatorChar + packageName.replace(" " , "_") + File.separatorChar + pipe.type;
 		
 		File dir = new File(dest_dir);
 		if(dir.exists() == false){
 			dir.mkdir();
 		}
 		
 		String oldAbsolutePath = pipe.absolutePath;
 		String newAbsolutePath = dest_dir + File.separatorChar + filename;
 		
 		// Move the file
 		File source_file = new File(oldAbsolutePath);
 		File dest_file = new File(newAbsolutePath);
 		
 		boolean success = source_file.renameTo(dest_file);
 		if(success == false) {
 			throw new Exception("File could not be moved");
 		}
 
 		// Something to check with Petros about
 		
 		//My initial thought was that package_name is related to directory_name by the following pattern:
 		//replace all white space characters (' ') by underscore characters ('_') and that is your directory_name. But it does not work...
 		//Proponents for the rule : "JHU DTI" package => "JHU_DTI" directory_name
 		//Opponents for the rule : "Automatic Registration Toolbox" package => "AutomaticRegistrationToolbox"
 		//In addition package names do not even spell out the same all the time
 		//check out Diffusion ToolKit package (make a note of upper case 'K' in word ToolKit)
 		//now look at the directory name : Diffusion_Toolkit (kit => k is lower case)
 		//examples of opponents are rather numerous, so instead of hardcoding or trying to find heuristic behind it, I just will extract
 		//name of directory from absolute path by first searching existing packageName, which suppose to have at least 1 file in it
 		//or else I would not be able to get the absolute path...
 		//then I will extract the directory name from it. If anyone complains, I am open to negotiations on this minor issue.
 		
 		// Update XML
 		Document doc = ServerUtils.parseXML(dest_file);
 		
 		pipe.packageName = packageName;
 		doc = ServerUtils.update(doc, pipe, true);
 		
 		ServerUtils.write(dest_file, doc);
 		
 		// Update Database
 		Connection con = getDatabaseConnection();
 		PreparedStatement stmt = con.prepareStatement(
 			"UPDATE pipefile " +
			"SET absolutePath = ? , packageName = ? " +
			"WHERE absolutePath = ?"	
 		);
		stmt.setString(1, "'" + oldAbsolutePath + "'");
		stmt.setString(2, "'" + packageName + "'");
		stmt.setString(3, "'" + newAbsolutePath + "'");
 		stmt.executeUpdate();
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
 		return null;
 	}
 	
 	/**
 	 *  Updates a group on the server (also used for creating groups)
 	 *  @param group group to be updated
 	 */
 	public void	updateGroup(Group group) throws Exception{
 		// TODO
 		// Update the row in the database corresponding to this group
 		return;
 	}
 }
 
