 package com.technosophos.rhizome.repository.fs;
 
 import java.io.InputStream;
 
 import com.technosophos.rhizome.document.RhizomeDocument;
 import com.technosophos.rhizome.document.RhizomeDocumentBuilder;
 import com.technosophos.rhizome.document.RhizomeParseException;
 import com.technosophos.rhizome.repository.DocumentRepository;
 import com.technosophos.rhizome.repository.RepositoryContext;
 import com.technosophos.rhizome.repository.RhizomeInitializationException;
 import com.technosophos.rhizome.repository.RepositoryAccessException;
 import com.technosophos.rhizome.repository.DocumentNotFoundException;
 import com.technosophos.rhizome.repository.DocumentExistsException;
 import java.io.File;
 import java.io.FileWriter;
 //import java.io.FileReader;
 import java.io.FileFilter;
 import java.io.IOException;
 import org.xml.sax.SAXException;
 import com.technosophos.rhizome.document.DocumentID;
 
 /**
  * File system-backed Document Repository.
  * <p>
  * This is an implementation of the DocumentRepository that uses 
  * the system's file system to store documents.
  * </p>
  * <p>
  * Documents are stored on the file system, where the document ID 
  * is the file name, and the document is stored as the file contents
  * (in XML, presumably).
  * </p>
  * @author mbutcher
  *
  */
 public class FileSystemRepository implements DocumentRepository {
 	
 	/**
 	 * The name of the value in the hash map that contains the
	 * path information for the file system path. (fs_repo_path)
 	 */
	public static final String FILE_SYSTEM_PATH_NAME = "fs_repo_path";
 	
 	// The config for this repository
 	private RepositoryContext cxt;
 	private String fileSystemPath;
 	private boolean isConfigured = false;
 	
 	private String repoName = null;
 	
 	/**
 	 * Construct a new repository.
 	 * <p>
 	 * The context should contain all of the settings for the 
 	 * repository. In particular, the respository's file 
 	 * system path ought to be passed in as:
 	 * </p>
 	 * <pre>
 	 * fileSystemPath
 	 * </pre>
 	 * <p>
 	 * The value should be the full (absolute) path in the 
 	 * file system of the repository directory.
 	 * </p>
 	 * @param cxt
 	 * @throws RhizomeIntializationException if fileSystemPath key is not in the context, or if
 	 * the key points to a nonexistent directory.
 	 * @deprecated use {@link #FileSystemRepository(String, RepositoryContext)} instead.
 	 */
 	public FileSystemRepository(RepositoryContext cxt) throws RhizomeInitializationException {
 		this.cxt = cxt;
 		if(cxt.hasKey(FileSystemRepository.FILE_SYSTEM_PATH_NAME)) {
 			this.fileSystemPath = getFullPath("", cxt); //cxt.getParam(FileSystemRepository.FILE_SYSTEM_PATH_NAME);
 			this.isConfigured = true;
 		}
 	}
 	
 	public FileSystemRepository(String name, RepositoryContext cxt) {
 		this.cxt = cxt;
 		this.repoName = name;
 		if(cxt.hasKey(FileSystemRepository.FILE_SYSTEM_PATH_NAME)) {
 			this.fileSystemPath = getFullPath(name, cxt); //cxt.getParam(FileSystemRepository.FILE_SYSTEM_PATH_NAME);
 			this.isConfigured = true;
 		}
 	}
 	
 	/**
 	 * This is the constructor most likely to be called.
 	 * After calling this constructor, you <b>MUST</b> setConfiguration().
 	 */
 	public FileSystemRepository() {
 		this.cxt = new RepositoryContext();
 	}
 
 	/**
 	 * Return the number of documents in the repository.
 	 */
 	public long countDocumentIDs() throws RepositoryAccessException {
 		File repoDir = this.getRepoDir(); // This throws exception
 		FileFilter ff = (FileFilter)new DocumentID();
 		File[] files = repoDir.listFiles(ff);
 		return files.length;
 	}
 
 	/**
 	 * Get an array of all document IDs. 
 	 * If no files are found, an empty array is returned.
 	 */
 	public String[] getAllDocumentIDs() throws RepositoryAccessException {
 		File repoDir = this.getRepoDir(); // This throws exception
 		
 		FileFilter ff = (FileFilter)new DocumentID();
 		File[] files = repoDir.listFiles(ff);
 		if(files.length == 0) return new String[0];
 		
 		String[] filenames = new String[files.length];
 		int i;
 		
 		for (i = 0; i < files.length; ++i) {
 			filenames[i] = files[i].getName();
 		}
 		return filenames;
 	}
 
 	/**
 	 * Return the repository context.
 	 */
 	public RepositoryContext getConfiguration() {
 		return this.cxt;
 	}
 	
 	public String getRepositoryName() { return this.repoName; }
 
 	/**
 	 * Get a Rhizome Document from the repository.
 	 * This can throw RepositoryAccessExceptions when file IO failed or the file could not be 
 	 * found, and it will throw a parse exception if anything goes wrong while parsing the file.
 	 */
 	public RhizomeDocument getDocument(String docID) 
 			throws DocumentNotFoundException, RepositoryAccessException, RhizomeParseException {
 		File doc = new File(this.getRepoDir(), docID);
 		if(!doc.exists()) 
 			throw new DocumentNotFoundException("File not found: " + doc.toString());
 		if(!doc.isFile())
 			throw new RepositoryAccessException("Item not a file: " + doc.toString());
 		RhizomeDocumentBuilder rdb = new RhizomeDocumentBuilder();
 		RhizomeDocument rdoc;
 		try {
 			synchronized(this) {
 				// FIXME: Work on synchronization! This is not finished!!!
 				// Should probably migrate to java.util.concurrent.lock.*
 				if(!FileSystemLocks.getInstance().isLocked(doc.getName()))
 					rdoc = rdb.fromXMLDocument(doc);
 				else
 					throw new RepositoryAccessException("Document is locked: " + doc.getName());
 			}
 		} catch (IOException ioe) {
 			throw new RepositoryAccessException("IO Exception: " + ioe.getMessage());
 		} catch (SAXException saxe) {
 			throw new RhizomeParseException("SAX Exception: " + saxe.toString());
 		}
 		return rdoc;
 	}
 
 	/**
 	 * Gets a file from the file system and returns it as a raw input stream.
 	 * The file is expected to be XML, but in this case the XML is unparsed.
 	 * This will throw a RepositoryAccessException if the file is not found, 
 	 * is not a valid file, or causes an IO error when opened.
 	 */
 	public InputStream getRawDocument(String docID) 
 			throws DocumentNotFoundException, RepositoryAccessException {
 		File doc = new File(this.getRepoDir(), docID);
 		if(!doc.exists()) 
 			throw new DocumentNotFoundException("File not found: " + doc.toString());
 		if(!doc.isFile())
 			throw new RepositoryAccessException("Item not a file: " + doc.toString());
 		InputStream is;
 		try {
 			is = (InputStream) new java.io.FileInputStream(doc);
 		} catch (IOException ioe) {
 			throw new RepositoryAccessException("IO Exception: " + ioe.getMessage());
 		}
 		return is;
 	}
 
 	/**
 	 * This checks to see if an item by the name of docID exists, and if 
 	 * that object happens to be a file. If so, it returns true.
 	 * This will throw an expcetion if the base directory cannot be opened 
 	 * for reading.
 	 */
 	public boolean hasDocument(String docID) throws RepositoryAccessException {
 		File doc = new File(this.getRepoDir(), docID);
 		if(doc.exists() && doc.isFile()) return true;
 		return false;
 	}
 
 	/**
 	 * Remove a document from the repository.
 	 * 
 	 * <p>This completely deletes the document from the repository.</p>
 	 * <p>This method contains synchronized sections.</p>
 	 * @param docID of the document to be deleted
 	 * @return true if the document was deleted.
 	 * @throws RepositoryAccessException if there is a problem with accessing the repository.
 	 */
 	public boolean removeDocument(String docID) throws RepositoryAccessException {
 		File doc = new File(this.getRepoDir(), docID);
 		if(!doc.exists() || !doc.isFile()) return false;
 		
 		boolean isDel = false;
 		String dn = doc.getName();
 		synchronized(this) {
 			// FIXME: Synchronization: This is not finished!!!
 			// Should probably migrate to java.util.concurrent.lock.*
 			if(FileSystemLocks.getInstance().acquireLock(dn, 4))
 				try {
 					isDel = doc.delete();
 				}finally{
 					FileSystemLocks.getInstance().removeLock(dn);
 				}
 			else
 				throw new RepositoryAccessException("Document is locked: " + dn);
 		}
 		return isDel;
 	}
 	
 	/**
 	 * This component is reusable.
 	 */
 	public boolean isReusable() {
 		return true;
 	}
 	
 	/**
 	 * @deprecated use {@link #setConfiguration(String, RepositoryContext)}
 	 */
 	public void setConfiguration(RepositoryContext cxt) throws RhizomeInitializationException {
 		this.setConfiguration(null, cxt);
 	}
 
 	/**
 	 * This must be called after the constructor if the Repository Context was not set.
 	 * @param name Name of the repository.
 	 * @param the repository context, which must have the fileSystemPath key, 
 	 * and a value that points to a directory on the file system.
 	 */
 	public void setConfiguration(String name, RepositoryContext cxt) throws RhizomeInitializationException {
 		this.repoName = name;
 		if(cxt == null)
 			throw new RhizomeInitializationException("RhizomeContext cannot be NULL");
 		this.cxt = cxt;
 		
 		if(!cxt.hasKey(FILE_SYSTEM_PATH_NAME))
 			throw new RhizomeInitializationException("File System Path info not found in context");	
 		this.fileSystemPath = getFullPath(name, cxt); //this.cxt.getParam(FILE_SYSTEM_PATH_NAME);
 		//if()
 		
 		this.isConfigured = true;
 	}
 
 	/**
 	 * Store a document in the repository.
 	 * 
 	 * This is a convenience method for calling storeDocument(RhizomeDocument, true).
 	 * It is synchronized.
 	 */
 	public String storeDocument(RhizomeDocument doc) throws RepositoryAccessException {
 		try {
 			return this.storeDocument(doc, true);
 		} catch(DocumentExistsException dee) {
 			// Can't get here. DEE is only thrown if flag is false.
 		}
 		return null;
 	}
 	
 	/**
 	 * Store the document in the repository.
 	 * 
 	 * Performs synchronous writes to the file repository. The RhizomeDocument will be 
 	 * converted to the underlying format (XML), and then written to the repository
 	 * (as a file on the file system). If the <code>overwrite</code> is false and the 
 	 * file already exists, this will throw a DocumentExistsException.
 	 */
 	public synchronized String storeDocument(RhizomeDocument doc, boolean overwrite) 
 			throws RepositoryAccessException, DocumentExistsException {
 		File repoDir = this.getRepoDir();
 		File docPath = new File(repoDir, doc.getDocumentID());
 		if(!overwrite && docPath.exists())
 			throw new DocumentExistsException("Document exists: "+doc.getDocumentID());
 		
 		/*
 		 * What we want to do:
 		 * 1. Lock the file. Ideally, this info should be stored on the file system,
 		 *    not in an object.
 		 * 2. Write the file.
 		 * 3. (Finally) unlock the file.
 		 */
 		if(FileSystemLocks.getInstance().acquireLock(doc.getDocumentID(), 4)) {
 			//FileSystemLocks.getInstance().lock(doc.getDocumentID());
 			FileWriter fout = null;
 			try {
 				fout = new FileWriter(docPath);
 				doc.toXML(fout);
 				//doc.toXML(System.out);
 				fout.flush();
 				fout.close();	
 			} catch (IOException ioe) {
 				try {
 					if(fout != null) fout.close();
 				} catch (Exception e) {
 					System.out.println("Error closing output stream.");
 				}
 				
 				throw new RepositoryAccessException("Could not write file: "
 						+ doc.getDocumentID()
 						+ " (IO Error: " + doc.getDocumentID() + ")");
 			} catch(javax.xml.parsers.ParserConfigurationException pce) {
 				try{ fout.close(); } catch (IOException e) {}
 				throw new RepositoryAccessException(
 						"Could not get contents of RhizomeDocument: " + doc.getDocumentID() 
 						+ "(Error: " + pce.getMessage() + ")");
 			} finally {
 				FileSystemLocks.getInstance().removeLock(doc.getDocumentID());
 			}
 		} else {
 			throw new RepositoryAccessException("Document is locked: " + doc.getDocumentID());
 		}
 		return null;
 	}
 	
 	/*
 	private boolean canAccess(String docID) {
 		return !FileSystemLocks.getInstance().isLocked(docID);
 	}
 	*/
 	
 	/**
 	 * Get the directory (as a {@link File}) for this repository.
 	 */
 	private File getRepoDir() throws RepositoryAccessException {
 		if(!this.isConfigured)
 			throw new RepositoryAccessException("Repository is not configured.");
 		File dir = new File(this.fileSystemPath);
 		if(!dir.isAbsolute())
 			this.fileSystemPath = dir.getAbsolutePath();
 		if(!dir.exists())
 			throw new RepositoryAccessException("Directory does not exist: " + this.fileSystemPath);
 		if(!dir.isDirectory())
 			throw new RepositoryAccessException("Not a directory: " + this.fileSystemPath);
 		if(!(dir.canRead() && dir.canWrite()))
 			throw new RepositoryAccessException("Must be able to read and write to " 
 					+ this.fileSystemPath);
 		
 		return dir;
 	}
 	
 	/** 
 	 * Get the path to the named repository.
 	 * @param name
 	 * @param cxt
 	 * @return
 	 */
 	public static String getFullPath(String name, RepositoryContext cxt) {
 		if(!cxt.hasKey(FileSystemRepository.FILE_SYSTEM_PATH_NAME))
 			return null;
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append(cxt.getParam(FileSystemRepository.FILE_SYSTEM_PATH_NAME));
 		if(sb.lastIndexOf(File.separator) != sb.length() - 1) 
 			sb.append(File.separatorChar);
 		sb.append(name);
 		return sb.toString();
 	}
 
 }
