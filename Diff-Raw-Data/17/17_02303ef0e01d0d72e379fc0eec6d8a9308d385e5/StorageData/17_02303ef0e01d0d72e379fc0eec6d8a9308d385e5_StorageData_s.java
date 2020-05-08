 package ch.bergturbenthal.raoa.server.model;
 
import java.util.Collection;
 import java.util.TreeSet;
 
 import lombok.Data;
 
 @Data
 public class StorageData {
	private final Collection<String> albumList = new TreeSet<>();
 	private int mBytesAvailable = Integer.MAX_VALUE;
 	private boolean takeAllRepositories = false;
 }
