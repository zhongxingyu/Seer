 package com.szas.server;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 import com.google.appengine.api.datastore.Key;
 import com.szas.sync.SyncedElementsHolder;
 
 import flexjson.JSONDeserializer;
 import flexjson.JSONSerializer;
 
 @PersistenceCapable
 public class PersistentRemoteTuple implements Serializable{
 	private static final long serialVersionUID = 1L;
 	@PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private Key key;
 	
 	@Persistent
 	private boolean deleted;
 	
 	@Persistent
	private String element;
 	
 	@Persistent
 	private long timestamp;
 	
 	@Persistent
 	private long insertionTimestamp;
 	
 	@Persistent 
 	private String className;
 	
 	@Persistent
 	private long id;
 	
 	public PersistentRemoteTuple() {
 	}
 	public void setDeleted(boolean deleted) {
 		this.deleted = deleted;
 	}
 
 	public boolean isDeleted() {
 		return deleted;
 	}
 	
 	public void setElement(Object element) {
		this.element = new JSONSerializer().include("*").serialize(element);
 	}
 	public Object getElement() {
		return new JSONDeserializer<ArrayList<SyncedElementsHolder>>().deserialize(element);
 	}
 
 	public void setTimestamp(long timestamp) {
 		this.timestamp = timestamp;
 	}
 
 	public long getTimestamp() {
 		return timestamp;
 	}
 	public void setKey(Key key) {
 		this.key = key;
 	}
 	public Key getKey() {
 		return key;
 	}
 	public void setClassName(String className) {
 		this.className = className;
 	}
 	public String getClassName() {
 		return className;
 	}
 	public void setId(long id) {
 		this.id = id;
 	}
 	public long getId() {
 		return id;
 	}
 	public void setInsertionTimestamp(long insertionTimestamp) {
 		this.insertionTimestamp = insertionTimestamp;
 	}
 	public long getInsertionTimestamp() {
 		return insertionTimestamp;
 	}
 }
