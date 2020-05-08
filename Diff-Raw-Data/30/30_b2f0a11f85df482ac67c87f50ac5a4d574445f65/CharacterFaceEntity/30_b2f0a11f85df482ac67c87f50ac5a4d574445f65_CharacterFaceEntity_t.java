 package com.gatech.faceme.entity;
 
 import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 import com.google.appengine.api.datastore.Key;
 

@PersistenceCapable(identityType = IdentityType.APPLICATION)
 public class CharacterFaceEntity {
 	@PrimaryKey
 	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
 	private Key key;
 	
 	@Persistent
 	private String imageKey;
 	
 	@Persistent
 	private String name;
 	
 	//image position
 	@Persistent
 	private float positionX; //upper left point
 	@Persistent
 	private float postionY;
 	@Persistent
 	private float width; //image width and height
 	@Persistent
 	private float height;
 	@Persistent
 	private Key postID;
 	@Persistent
 	private int index;
 	
	public CharacterFaceEntity(String imageKey, String name,
 			float positionX, float postionY,float width, float height,
 			Key postID, int index){
 		this.imageKey = imageKey;
 		this.name = name;
 		this.positionX = positionX;
 		this.postionY = postionY;
 		this.width = width;
 		this.height = height;
 		this.postID = postID;
 		this.index = index;
 	}
 	public String getImageKey() {
 		return imageKey;
 	}
 	public void setImageKey(String imageKey) {
 		this.imageKey = imageKey;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public float getPositionX() {
 		return positionX;
 	}
 	public void setPositionX(float positionX) {
 		this.positionX = positionX;
 	}
 	public float getPostionY() {
 		return postionY;
 	}
 	public void setPostionY(float postionY) {
 		this.postionY = postionY;
 	}
 	public Key getPostID() {
 		return postID;
 	}
 	public void setPostID(Key postID) {
 		this.postID = postID;
 	}
 	public float getWidth() {
 		return width;
 	}
 	public void setWidth(float width) {
 		this.width = width;
 	}
 	public float getHeight() {
 		return height;
 	}
 	public void setHeight(float height) {
 		this.height = height;
 	}
 	public int getIndex() {
 		return index;
 	}
 	public void setIndex(int index) {
 		this.index = index;
 	}
 	public Key getKey() {
 		return key;
 	}
 }
