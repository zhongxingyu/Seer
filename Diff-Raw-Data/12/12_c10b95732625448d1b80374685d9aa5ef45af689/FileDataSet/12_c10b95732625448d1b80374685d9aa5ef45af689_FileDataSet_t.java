 package darep.repos.fileStorage;
 
 import java.io.File;
 import java.io.IOException;
 
 import darep.Helper;
 import darep.repos.DataSet;
 import darep.repos.Metadata;
 import darep.repos.StorageException;
 
 /**
  * Represents a Dataset consisting of BOTH the file itself and the Metadata.
  */
 class FileDataSet implements DataSet {
 	private File file;
 	private Metadata metadata;
 	
 //	/**
 //	 * Creates a new Dataset that can later be saved to the Database.
 //	 * @param file
 //	 * @param meta
 //	 * @param db
 //	 * @throws RepositoryException 
 //	 * @throws IOException 
 //	 */
 //	public static FileDataSet createNewDataset(File file, Metadata metadata, FileStorage db) throws RepositoryException {
 //		FileDataSet ds = new FileDataSet();
 //		
 //		File canonicalFile;
 //		try {
 //			canonicalFile = file.getCanonicalFile();
 //		} catch (IOException e) {
 //			throw new RepositoryException("Could not get canonical File for " +
 //					file.getAbsolutePath());
 //		}
 //		
 //		ds.file = canonicalFile;
 //		ds.metadata = metadata;
 //		ds.db = db;
 //		
 //		metadata.setFileSize(ds.calculateFileSize());
 //		metadata.setNumberOfFiles(ds.countFiles());
 //		
 //		return ds;
 //	}
 	
 	
 //	private long calculateFileSize() {
 //		return calculateFileSize(file);
 //	}
 
	public FileDataSet() { }
	public FileDataSet(File file, Metadata meta) {
		this.file = file;
		this.metadata = meta;
	}
 	
 	/**
 	 * Returns the {@link Metadata} object belonging to this Dataset
 	 * @return
 	 */
 	@Override
 	public Metadata getMetadata() {
 		return this.metadata;
 	}
 	
 	@Override
 	public String toString() {
 		return (metadata.getName() + "\t" + metadata.getOriginalName() + "\t"
 				+ metadata.getTimeStamp() + "\t" + metadata.getNumberOfFiles()
 				+ "\t" + metadata.getFileSize() + "\t" + metadata.getDescription());
 	}
 
 
 	@Override
 	public void copyFileTo(File path) throws StorageException {
 		try {
 			Helper.copyRecursive(file, path);
 		} catch (IOException e) {
 			throw new StorageException("Could not copy file to " + path, e);
 		}
 	}
 
 	void setMetaData(Metadata meta) {
 		this.metadata = meta;
 	}
 
 	void setFile(File file) {
 		this.file = file;
 	}
 
 	@Override
 	public Type getType() {
 		if (file.exists()) {
 			return file.isDirectory() ? DataSet.Type.folder : DataSet.Type.file;
 		} else {
 			return null;
 		}
 	}
 	
 }
