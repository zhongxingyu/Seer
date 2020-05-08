 package icloude.contents;
 
 
 /**
  * @author DimaTWL
  * This class represents "FileContent" entity from protocol
  */
 public class FileContent {
 	private String type;
 	private String fileName;
 	private String fileID;
 	private String text;
 	private String fileType;
 	private String ownerID;
 	private String revisionID;
 	private Long creationDate;
 	private Long modificationDate;
 	
 	/**
 	 * @param type
 	 * @param fileName
 	 * @param fileID
 	 * @param text
 	 * @param fileType
 	 * @param ownerID
 	 * @param revisionID
 	 * @param creationDate
 	 * @param modificationDate
 	 */
 	public FileContent(String type, String fileName, String fileID,
 			String text, String fileType, String ownerID, String revisionID,
 			Long creationDate, Long modificationDate) {
 		super();
 		this.type = type;
 		this.fileName = fileName;
 		this.fileID = fileID;
 		this.text = text;
 		this.fileType = fileType;
 		this.ownerID = ownerID;
 		this.revisionID = revisionID;
 		this.creationDate = creationDate;
 		this.modificationDate = modificationDate;
 	}
 
 	/**
 	 * @return the type
 	 */
 	public String getType() {
 		return type;
 	}
 
 	/**
 	 * @return the fileName
 	 */
 	public String getFileName() {
 		return fileName;
 	}
 
 	/**
 	 * @return the fileID
 	 */
 	public String getFileID() {
 		return fileID;
 	}
 
 	/**
 	 * @return the text
 	 */
 	public String getText() {
 		return text;
 	}
 
 	/**
 	 * @return the fileType
 	 */
 	public String getFileType() {
 		return fileType;
 	}
 
 	/**
 	 * @return the ownerID
 	 */
 	public String getOwnerID() {
 		return ownerID;
 	}
 
 	/**
 	 * @return the revisionID
 	 */
 	public String getRevisionID() {
 		return revisionID;
 	}
 
 	/**
 	 * @return the creationDate
 	 */
 	public Long getCreationDate() {
 		return creationDate;
 	}
 
 	/**
 	 * @return the modificationDate
 	 */
 	public Long getModificationDate() {
 		return modificationDate;
 	}
 	
 
 
 }
