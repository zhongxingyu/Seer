 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.acu.xservice.xythos;
 
 import com.xythos.common.api.XythosException;
 import edu.acu.xservice.api.DirectoryEntry;
 import com.xythos.storageServer.api.FileSystemEntry;
 import com.xythos.storageServer.api.StorageServerException;
 import edu.acu.xservice.EntryException;
 import java.util.Date;
 
 /**
  *
  * @author hgm02a
  */
 public class XythosDirectoryEntry implements DirectoryEntry {
 	
 	private final XythosFileManager manager;
 	private final FileSystemEntry entry;
 	
 	public XythosDirectoryEntry(FileSystemEntry entry, XythosFileManager manager) throws EntryException {
 
 		this.entry = (com.xythos.fileSystem.DirectoryEntry) entry;
 
 		this.manager = manager;
 	}
 
 	public String getPath() {
 		return this.entry.getName();
 	}
 	
 	public String getType() {
 		try {
			String type = entry.getFileContentType();
			return (type == null) ? "folder" : type;
 		} catch (XythosException ex) {
 			return "";
 		}
 	}
 	
 	public String getOwner() throws EntryException {
 		try {
 			return manager.getDisplayName(entry.getEntryOwnerPrincipalID());
 		} catch (XythosException ex) {
 			throw new EntryException(ex);
 		}
 	}
 
 	public String getCreator() throws EntryException {
 		try {
 			return manager.getDisplayName(entry.getCreatedByPrincipalID());
 		} catch (XythosException ex) {
 			throw new EntryException(ex);
 		}
 	}
 
 	public Date getCreated() {
 		return entry.getCreationTimestamp();
 	}
 
 	public String getDescription() throws EntryException {
 		try {
 			return entry.getDescription();
 		} catch (XythosException ex) {
 			throw new EntryException(ex);
 		} 
 	}
 
 	public String getLastUpdater() throws EntryException {
 		try {
 			return manager.getDisplayName(entry.getLastUpdatedByPrincipalID());
 		} catch (XythosException ex) {
 			throw new EntryException(ex);
 		} 
 	}
 	
 	public Date getLastUpdated() {
 		return entry.getLastUpdateTimestamp();
 	}
 	
 }
