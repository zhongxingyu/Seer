 package com.gatech.faceme.entity;
 
 import java.util.ArrayList;
 import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 import com.google.appengine.api.datastore.Key;
 
@PersistenceCapable(identityType = IdentityType.APPLICATION)
 public class PairTableEntity {
 	@PrimaryKey
 	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
 	private Key key;
 	
 	@Persistent
 	private ArrayList<Key> faceList;
 	
	public PairTableEntity(ArrayList<Key> faceList){
		
 		this.setFaceList(faceList);
 	}
 
 	public Key getKey() {
 		return key;
 	}
 
 	public void setKey(Key key) {
 		this.key = key;
 	}
 
 	public ArrayList<Key> getFaceList() {
 		return faceList;
 	}
 
 	public void setFaceList(ArrayList<Key> faceList) {
 		this.faceList = faceList;
 	}
 	
 	
 }
