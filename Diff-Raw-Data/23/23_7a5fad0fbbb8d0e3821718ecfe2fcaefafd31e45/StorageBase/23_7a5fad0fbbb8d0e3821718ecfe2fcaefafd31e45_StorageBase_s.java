 package org.neuro4j.storage;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Properties;
 
 import org.neuro4j.core.Entity;
 import org.neuro4j.core.Network;
 import org.neuro4j.core.Relation;
 import org.neuro4j.utils.ClassloaderUtil;
 import org.neuro4j.utils.KVUtils;
 
 public abstract class StorageBase implements NeuroStorage {
 
 	protected Properties properties;
 	protected static final String REPRESENTATIONS_DEFAULT_DIR = "reps";
 	protected static final String LIBS_DEFAULT_DIR = "lib";
 
 	protected String storageHomeDirStr;
 	protected String representationsDirectory;
 	protected String libsDirectory;
 
 	public Properties getConfig() 
 	{
 		return properties;
 	}
 	
 	public void init(Properties properties) throws StorageException 
 	{
 		this.properties = properties;
 		
 		storageHomeDirStr = KVUtils.getStringProperty(properties, StorageConfig.STORAGE_HOME_DIR);
 		
 		// get representations dir
 		representationsDirectory = KVUtils.getStringProperty(properties, StorageConfig.STORAGE_REPRESENTATIONS_DIR);
 		// if empty -> set default
 		if (null == representationsDirectory || 0 == representationsDirectory.trim().length())
 			representationsDirectory = REPRESENTATIONS_DEFAULT_DIR;
 			
 		representationsDirectory = checkForRelativeFilePath(representationsDirectory);
 		
 		// get libraries directory
 		libsDirectory = KVUtils.getStringProperty(properties, StorageConfig.STORAGE_LIB_DIR);
 		
 		// if empty -> set default
 		if (null == libsDirectory || 0 == libsDirectory.trim().length())
 			libsDirectory = LIBS_DEFAULT_DIR;
 
 		libsDirectory = checkForRelativeFilePath(libsDirectory);
 		extendClassLoader(libsDirectory);
 		
 		return;
 	}
 	
 	/**
 	 * Check if file path relative (to storage home) or absolute. 
 	 * Check the file under storage_home. If found - return it. If not - return input assuming it's absolute path
 	 * 
 	 * @param path
 	 * @return
 	 */
 	protected String checkForRelativeFilePath(String path)
 	{
 		if (null != storageHomeDirStr)
 		{
 			File storageHomeDir = new File(storageHomeDirStr);
 			File f = new File(storageHomeDir, path);
 			if (f.exists())
 				return f.getAbsolutePath();
 		}
 		
 		return path;
 	}
 
 	/**
 	 * 
 	 * 
 	 * @param directoryWithJarExtensions
 	 * @throws StorageException
 	 */
 	protected void extendClassLoader(String directoryWithJarExtensions) throws StorageException
 	{
 		if (null == directoryWithJarExtensions || 0 == directoryWithJarExtensions.trim().length())
 			return;
 		
 		File jarDir = new File(directoryWithJarExtensions);
 		
 		FilenameFilter jarFilter = new FilenameFilter() {
 			public boolean accept(File dir, String name) {
 				String lowercaseName = name.toLowerCase();
 				if (lowercaseName.endsWith(".jar")) {
 					return true;
 				} else {
 					return false;
 				}
 			}
 		};
 		
 		for (File file : jarDir.listFiles(jarFilter)) {
 			if (!file.isDirectory()) {
 				try {
 					ClassloaderUtil.addSoftwareLibrary(file);
 				} catch (Exception e1) {
 					throw new StorageException("Can't load  " + directoryWithJarExtensions + " to application classloader", e1);
 				}
 
 			}
 		}
 		
 		return;
 	}
 	
 	public Entity getEntityByUUID(String entityUUID) throws StorageException
 	{
 		Entity e = null;
 		Network net;
 		try {
 			net = query("select e(id=?)", new String[]{entityUUID});
 			e = net.getEntityByUUID(entityUUID);
 		} catch (NQLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		return e;
 	}
 	
 
 	public Relation getRelationByUUID(String relationUUID) throws StorageException {
 		Relation r = null;
 		Network net;
 		try {
 			net = query("select r(id=?)", new String[]{relationUUID});
 			r = net.getRelationByUUID(relationUUID);
 		} catch (NQLException e) {
 			e.printStackTrace();
 		}
 		return r;
 	}	
 	
 	/**
 	 * "select e[?] depth 1", new String[]{entityUUID}
 	 * 
 	 * 
 	 */
 	public Network query(String q, String[] parameters)  throws NQLException, StorageException {
 		for (String param : parameters)
 			q = q.replaceFirst("\\?", "'" + param + "'");
 		
 		return query(q);
 	}	
 	
 	public InputStream getRepresentationInputStream(String id) throws StorageException
 	{
 		
 		File dir = new File(representationsDirectory);
 		if (!dir.exists())
			throw new StorageException("Home directory is not specified for " + this);
 			
 		File repFile = new File(dir, id);
 		if (!repFile.exists())
 			throw new StorageException("Representation " + id + " not found " + repFile.getAbsolutePath());
 		
 		InputStream is;
 		try {
 			is = new FileInputStream(repFile);
 		} catch (FileNotFoundException e) {
 			throw new StorageException("Representation " + id + " not found " + repFile.getAbsolutePath(), e);
 		}
 		
 		return is;
 	}
 	
 	public OutputStream getRepresentationOutputStream(String id) throws StorageException
 	{
 		File dir = new File(representationsDirectory);
 		if (!dir.exists())
			throw new StorageException("Home directory is not specified for " + this);
 			
 		File repFile = new File(dir, id);
 		if (!repFile.exists())
 		{
 			try {
 				repFile.createNewFile();
 			} catch (IOException e1) {
 				throw new StorageException("Can't create new file for pepresentation " + id + " Path: " + repFile.getAbsolutePath(), e1);
 			}
 		}
 		
 		OutputStream out;
 		try {
 			out = new FileOutputStream(repFile);
 		} catch (FileNotFoundException e) {
 			throw new StorageException("Representation " + id + " not found " + repFile.getAbsolutePath(), e);
 		}
 		
 		return out;
 	}
 	
 	
 }
