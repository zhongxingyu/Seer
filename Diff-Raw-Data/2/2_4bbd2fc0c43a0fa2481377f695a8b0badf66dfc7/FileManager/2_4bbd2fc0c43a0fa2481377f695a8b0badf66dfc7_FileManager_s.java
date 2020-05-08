 package org.makumba.parade.model.managers;
 
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.makumba.parade.model.File;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.model.interfaces.DirectoryRefresher;
 import org.makumba.parade.model.interfaces.ParadeManager;
 import org.makumba.parade.model.interfaces.RowRefresher;
 import org.makumba.parade.tools.SimpleFileFilter;
 
 public class FileManager implements RowRefresher, DirectoryRefresher, ParadeManager {
 
 	static Logger logger = Logger.getLogger(FileManager.class.getName());
 	
 	private FileFilter filter = new SimpleFileFilter();
 	
 	/* Creates a first File for the row which is its root dir 
 	 * and invokes its refresh() method
 	 */
 	public void rowRefresh(Row row) {
 		
 		File root = new File();
 		
 		try {
 			java.io.File rootPath = new java.io.File(row.getRowpath());
 			root.setName("_root_");
 			root.setPath(rootPath.getCanonicalPath());
 			root.setRow(row);
 			root.setDate(new Long(new java.util.Date().getTime()));
 			root.setFiledata(new HashMap());
 			root.setSize(new Long(0));
 			root.setOnDisk(false);
 			row.getFiles().clear();
 			root.setIsDir(true);
 			row.getFiles().put(root.getPath(), root);
 			
 		} catch(Throwable t) {
 			logger.error("Couldn't access row path of row "+row.getRowname(),t);
 		}
 		
 		root.refresh();
 		
 	}
 
 	public void directoryRefresh(Row row, String path) {
 		java.io.File currDir = new java.io.File(path);
 		
 		if(currDir.isDirectory()) {
 			
 			java.io.File[] dir = currDir.listFiles();
 	        for (int i = 0; i < dir.length; i++) {
 	            if (filter.accept(dir[i]) && !(dir[i].getName() == null)) {
 	            
 	            	java.io.File file = dir[i];
 	            
 		            if(file.isDirectory()) {
 		            	File dirData = setFileData(row, file, true);
 		            	addFile(row, dirData);
 		            
 		            	dirData.refresh();
 		            	
 		            } else if(file.isFile()) {
 		            	File fileData = setFileData(row, file, false);
 		            	addFile(row, fileData);
 	            	}	
 	            }
 	        }
 		}
 	}
 	
 	
 	/* adding file to Row files */
 	private void addFile(Row row, File fileData) {
 		
 		row.getFiles().put(fileData.getPath(),fileData);
 		
 		//logger.warn("Added file: "+fileData.getName());
 	}
 
 	/* setting File informations */
 	private File setFileData(Row row, java.io.File file, boolean isDir) {
 		File fileData = new File();
 		fileData.setIsDir(isDir);
 		fileData.setRow(row);
 		try {
 			fileData.setPath(file.getCanonicalPath());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		fileData.setName(file.getName());
 		fileData.setDate(new Long(file.lastModified()));
 		fileData.setSize(new Long(file.length()));
 		fileData.setOnDisk(true);
 		return fileData;
 	}
 	
 	
 	public static File setVirtualFileData(Row r, File path, String name, boolean dir) {
 		File cvsfile = new File();
 		cvsfile.setName(name);
 		cvsfile.setPath(path.getPath() + java.io.File.separator + name);
 		cvsfile.setOnDisk(false);
 		cvsfile.setIsDir(dir);
 		cvsfile.setRow(r);
 		cvsfile.setDate(new Long((new Date()).getTime()));
 		cvsfile.setSize(new Long(0));
 		return cvsfile;
 	}
 	
 	/* returns a List of the keys of the subdirs of a given path */
 	public static List getSubdirs(Row r, String path) {
 		
 		Set keySet = r.getFiles().keySet();
 		
 		List subDirs = new LinkedList();
 		
 		for(Iterator i = keySet.iterator(); i.hasNext();) {
 			String currentKey = (String) i.next();
 			if(currentKey.startsWith(path) && currentKey.substring(0,path.length()).length() > 0) {
 				File f = (File) r.getFiles().get(currentKey);
 				if (f.getIsDir()) {
 					subDirs.add(currentKey);
 				}
 			}
 		}
 		
 		return subDirs;
 		
 	}
 	
 	/* returns a List of the direct children (files, dirs) of a given Path */
 	public static List getChildren(Row r, String path) {
 		String keyPath = path;
 		
 		String absoulteRowPath = (new java.io.File(r.getRowpath()).getAbsolutePath());
 		if(keyPath == null || keyPath=="") keyPath=absoulteRowPath;
 		keyPath=keyPath.replace('/',java.io.File.separatorChar);
 		
 		Set keySet = r.getFiles().keySet();
 		
 		List children = new LinkedList();
 		
 		for(Iterator i = keySet.iterator(); i.hasNext();) {
 			String currentKey = (String) i.next();
 			boolean isChild = currentKey.startsWith(keyPath);
 			if(isChild)  {
 				boolean isNotRoot = currentKey.length() - keyPath.length() > 0;
 				if (isNotRoot) {
 					String childKey = currentKey.substring(keyPath.length()+1,currentKey.length());
 					boolean isDirectChild = childKey.indexOf(java.io.File.separator) == -1;
 					if(isDirectChild) {
 						children.add(r.getFiles().get(currentKey));
 					}
 				}
 			}
 		}
 		
 		return children;
 	}
 
 	public void newRow(String name, Row r, Map m) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public String newFile(Row r, String path, String entry) {
 		java.io.File f = new java.io.File((path+"/"+entry).replace('/',java.io.File.separatorChar));
 		if(f.exists() && f.isFile()) return "This file already exists";
 		boolean success=false;
 		try {
 			success = f.createNewFile();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return("Error while trying to create file "+entry);
 		}
 		if(success) {
 			File newFile = setFileData(r, f, false);
 			try {
 				r.getFiles().put(f.getCanonicalPath(), newFile);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			return "OK#"+f.getAbsolutePath();
 		}
 		return "Error while trying to create file "+entry;
 	}
 
 	public String newDir(Row r, String path, String entry) {
 		System.out.println("newDir: "+entry + "path: "+path);
		java.io.File f = new java.io.File((path+"/"+entry+"/.").replace('/',java.io.File.separatorChar));
 		if(f.exists() && f.isDirectory()) return "This directory already exists";
 		
 		boolean success = f.mkdir();
 		
 		if(success) {
 			File newFile = setFileData(r, f, true);		
 			try {
 				r.getFiles().put(f.getCanonicalPath(), newFile);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			return "OK#"+f.getAbsolutePath();
 		}
 		
 		return "Error while trying to create directory "+entry;
 		
 	}
 
 }
