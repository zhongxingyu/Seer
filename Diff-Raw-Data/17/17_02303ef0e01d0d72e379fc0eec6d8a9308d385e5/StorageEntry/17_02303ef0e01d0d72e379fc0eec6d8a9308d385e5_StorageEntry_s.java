 package ch.bergturbenthal.raoa.data.model;
 
import java.util.ArrayList;
import java.util.Collection;
 
 import lombok.Data;
 
 @Data
 public class StorageEntry {
	private final Collection<String> albumList = new ArrayList<String>();
 	private Long mBytesAvailable;
 	private String storageId;
 	private String storageName;
 	private boolean takeAllRepositories;
 }
