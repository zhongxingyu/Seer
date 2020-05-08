 package models;
 
 import play.Logger;
 import play.db.ebean.*;
 import play.mvc.Http.MultipartFormData.FilePart;
 import utils.ContentType;
 import utils.FileInfo;
 import utils.Files;
 
 import javax.persistence.*;
 
 import controllers.Application;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
import java.io.IOException;
 import java.util.*;
 
 @Entity
 public class Upload extends Model {
 	
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	public Long id;
 	
 	public String absolutePath;
 	public String contentType;
 	public Date uploaded;
 	public Long browserId; // browserId of the browser widnow that uploaded this upload
 	
 	@Column(name="user_id")
 	public Long user;
 	public String job;
 	
 	@Transient
 	private List<FileInfo> fileList;
 	
 	private Upload() {
 		this.uploaded = new Date();
 	}
 	
 	public static Long store(FilePart upload, User user, Long browserId) {
 		Upload u = new Upload();
 		u.save(Application.datasource); // saving here generates the unique id for this upload
 		
 		u.browserId = browserId;
 		
 		File uploadDir = new File(Setting.get("uploads")+u.id);
 		uploadDir.mkdirs();
 		u.absolutePath = uploadDir.getAbsolutePath();
 		
         File file = upload.getFile();
 		File newFile = new File(uploadDir, upload.getFilename());
		if (!file.renameTo(newFile)) {
			Logger.debug("Was unable to move uploaded file to the upload directory, will try to copy it instead...");
			try {
				Files.copy(file, newFile);
			} catch (IOException e) {
				Logger.error("Was unable to copy uploaded file to new file", e);
			}
		}
 		file = newFile;
 		Logger.debug("Stored uploaded file as: "+file.getAbsolutePath()+" (=="+new File(uploadDir, upload.getFilename()).getAbsolutePath()+")");
 		try {
 			u.contentType = ContentType.probe(file.getName(), new FileInputStream(file));
 		} catch (FileNotFoundException e) {
 			Logger.error("Was unable to probe "+file.getAbsolutePath()+" : "+e.getMessage());
 			u.contentType = upload.getContentType();
 		}
 		
 		u.user = user.id;
 		
 		u.save(Application.datasource);
 		
 		return u.id;
 	}
 	
 	public List<FileInfo> listFiles() {
 		if (fileList != null)
 			return fileList;
 		
 		fileList = new ArrayList<FileInfo>();
 		
 		Logger.debug("opening "+this.absolutePath);
 		File uploadDir = new File(this.absolutePath);
 		if (uploadDir.isDirectory()) {
 			Logger.debug(this.absolutePath+" is a directory...");
 			File[] dirContents = uploadDir.listFiles();
 			if (dirContents.length > 0) {
 				Logger.debug(this.absolutePath+" contains 1 or more files...");
 				File file = dirContents[0];
 				
 				if (isZip()) {
 					Logger.debug(this.absolutePath+" is a ZIP file, listing ZIP entries...");
 					fileList = Files.listZipFilesWithContentType(file);
 					
 				} else {
 					Logger.debug(this.absolutePath+" is a normal file...");
 					fileList.add(new FileInfo(file.getName(), this.contentType, file.length()));
 				}
 			}
 		}
 		
 		return fileList;
 	}
 	
 	public File getFile() {
 		File uploadDir = new File(this.absolutePath);
 		if (uploadDir.isDirectory()) {
 			File[] dirContents = uploadDir.listFiles();
 			if (dirContents.length > 0) {
 				return dirContents[0];
 			}
 		}
 		Logger.debug("Could not retrieve the upload with id "+this.id+" from "+this.absolutePath);
 		return null;
 	}
 	
 	public boolean isZip() {
 		return "application/zip".equals(this.contentType);
 	}
 	
 	// -- Queries
 	
 	public static Finder<Long,Upload> find = new Finder<Long,Upload>(Application.datasource, Long.class, Upload.class);
 	
 	/** Retrieve a Upload by its id. */
     public static Upload findById(Long id) {
         return find.where().eq("id", id).findUnique();
     }
     
 //    /** Delete all Uploads belonging to a user */
 //	public static void deleteUserUploads(User user) {
 //		for (Upload upload : find.where().eq("user", user.id).findList()) {
 //			upload.delete(Application.datasource);
 //		}
 //	}
     
     @Override
     public void delete(String datasource) {
     	File file = getFile();
     	if (file != null && file.exists())
     		file.delete();
     	super.delete(datasource);
     }
     
     @Override
 	public void save(String datasource) {
 		super.save(datasource);
 		
 		// refresh id after save
 		if (this.id == null) {
 			Upload upload = Upload.find.where().eq("uploaded", this.uploaded).eq("absolutePath", this.absolutePath).findUnique();
 			if (upload != null) {
 				this.id = upload.id;
 			}
 		}
 	}
 }
