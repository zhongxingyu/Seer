 package ch.bergturbenthal.raoa.data.model;
 
import java.util.Set;
import java.util.TreeSet;
 
 import lombok.Data;
 
 @Data
 public class StorageEntry {
	private final Set<String> albumList = new TreeSet<String>();
 	private Long mBytesAvailable;
 	private String storageId;
 	private String storageName;
 	private boolean takeAllRepositories;
 }
