 package com.gatech.faceme.entity;
 
 import java.util.ArrayList;

 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 import com.google.appengine.api.datastore.Key;
 
 public class PairTableEntity {
 	@PrimaryKey
 	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
 	private Key key;
 	
 	@Persistent
 	private ArrayList<Key> faceList;
 	
	public PairTableEntity(Key key, ArrayList<Key> faceList){
		this.setKey(key);
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
