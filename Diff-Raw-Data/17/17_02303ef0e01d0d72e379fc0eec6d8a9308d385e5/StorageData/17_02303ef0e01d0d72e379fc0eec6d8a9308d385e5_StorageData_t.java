 package ch.bergturbenthal.raoa.server.model;
 
import java.util.Set;
 import java.util.TreeSet;
 
 import lombok.Data;
 
 @Data
 public class StorageData {
	private final Set<String> albumList = new TreeSet<>();
 	private int mBytesAvailable = Integer.MAX_VALUE;
 	private boolean takeAllRepositories = false;
 }
