 package darep.repos;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.Date;
 
 public class Metadata implements Serializable {
 	private static final long serialVersionUID = 1L;
 	private String name;
 	private String originalName;
 	private Date timestamp;
 	private String description;
 	private int numberOfFiles;
 	private long size;
 	private File path;
 	
 	private static int maxNameLength = "Name".length();
 	private static int maxOrigNameLength = "Original Name".length();
 	private static int maxTimestampLength = "Timestamp".length();
 	private static int maxNumFilesLength = "Number of Files".length();
 	private static int maxSizeLength = "Size".length();
 	private static int maxDescriptionLength = "Description".length();
 	
 	public static int getMaxNameLength() {
 		return maxNameLength;
 	}
 
 	public static int getMaxOrigNameLength() {
 		return maxOrigNameLength;
 	}
 
 	public static int getMaxTimestampLength() {
 		return maxTimestampLength;
 	}
 
 	public static int getMaxNumFilesLength() {
 		return maxNumFilesLength;
 	}
 
 	public static int getMaxSizeLength() {
 		return maxSizeLength;
 	}
 
 	public static int getMaxDescriptionLength() {
 		return maxDescriptionLength;
 	}
 	
 	public static int getTotalMaxWidth() {
 		return (maxNameLength
 				+ maxOrigNameLength
 				+ maxTimestampLength
 				+ maxNumFilesLength
 				+ maxSizeLength
 				+ maxDescriptionLength);
 	}
 
 	public Metadata() {
 		timestamp = new Date();
 		description = "";
 		name = "";
 		originalName = "";
 		path = null;
 	}
 	
 	public Metadata(String name, String origName, String descr, int nrFiles, int size, String reposPath) {
 		this.setTimestamp(new Date());
 		this.setDescription(descr);
 		this.setName(name);
 		this.setOriginalName(origName);
 		this.setNumberOfFiles(nrFiles);
 		this.setFileSize(size);
 		this.path = new File(reposPath+"/metadata/"+name);
		
		setMaxLengthValues(this);
 	}
 	
 	private static void setMaxLengthValues(Metadata m) {
		
 		if (maxTimestampLength < m.timestamp.toString().length()) {
 			maxTimestampLength = m.timestamp.toString().length();
 		}
 		if (maxNameLength < m.name.length()) {
 			maxNameLength = m.name.length();
 		}
 		if (maxOrigNameLength < m.originalName.length()) {
 			maxOrigNameLength = m.originalName.length();
 		}
 		if (maxDescriptionLength < m.description.length()) {
 			maxDescriptionLength = m.description.length();
 		}
 		if (maxSizeLength < String.valueOf(m.size).length()) {
 			maxSizeLength = String.valueOf(m.size).length();
 		}
 		if (maxNumFilesLength < String.valueOf(m.numberOfFiles).length()) {
 			maxNumFilesLength = String.valueOf(m.numberOfFiles).length();
 		}
 	}
 
 	private void setTimestamp(Date date) {
 		this.timestamp = date;
 	}
 
 	public static Metadata readFile(File file) throws RepositoryException {
 		try {
 			FileInputStream fileIn = new FileInputStream(file);
 			ObjectInputStream in = new ObjectInputStream(fileIn);
 			
 			try {
 				 Object obj = in.readObject();
 				 if (obj instanceof Metadata) {
 					 Metadata m = (Metadata) obj;
 					 setMaxLengthValues(m);
 					 return m;
 				 } else {
 					 throw new RepositoryException("File " + file.getName()
 							 + "is not a Metadata-file");
 				 }
 			} finally {
 				in.close();
 				fileIn.close();
 			}
 			
 		} catch (ClassNotFoundException e) {
 			throw new RepositoryException("Could not read Metadata for "
 					+ file.getName());
 		} catch (IOException e) {
 			throw new RepositoryException("Could not read Metadata for "
 					+ file.getName());
 		}
 	}
 	
 	public boolean delete() {
 		return ((path == null)
 				|| !this.path.exists()
 				|| this.path.delete());
 	}
 
 	public void saveAt(String pathStr) throws RepositoryException {
 		path = new File(pathStr);
 		save();
 	}
 	
 	public void save() throws RepositoryException {
 		if (path == null)
 			throw new RepositoryException("attempt to save metadata without path");
 		if (name.length() < 1)
 			throw new RepositoryException("attempt to save metadata without name");
 		if (originalName.length() < 1)
 			throw new RepositoryException("attempt to save metadata without originalname");
 		try {
 			FileOutputStream fileOut = new FileOutputStream(path);
 			ObjectOutputStream out = new ObjectOutputStream(fileOut);
 			try {
 				out.writeObject(this);
 			} finally {
 				out.close();
 				fileOut.close();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new RepositoryException("could not save metadata");
 		}
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void setOriginalName(String originalName) {
 		this.originalName = originalName;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getOriginalName() {
 		return originalName;
 	}
 	
 	@Override
 	public String toString() {
 		String filePath = null;
 		if(path != null)
 			filePath = path.getAbsolutePath();
 		else
 			filePath = "";
 			
 		return "METADATA:\nname: " + name + "\noriginalName: " + originalName + "\ntimestamp: " + timestamp.toString() +
 				"\ndescription: " + description + "\nnumberOfFiles: " + numberOfFiles + "\nsize: " + size + "\npath: " + filePath;
 	}
 
 	public long getTimeStamp() {
 		return timestamp.getTime();
 	}
 
 	public int getNumberOfFiles() {
 		return numberOfFiles;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public long getSize() {
 		return size;
 	}
 
 	public void saveInDir(File dir) throws RepositoryException {
 		path = new File(dir, getName());
 		this.save();
 	}
 
 	public void setFileSize(long fileSize) {
 		this.size = fileSize;
 	}
 
 	public void setNumberOfFiles(int countFiles) {
 		this.numberOfFiles = countFiles;
 	}
 
 }
