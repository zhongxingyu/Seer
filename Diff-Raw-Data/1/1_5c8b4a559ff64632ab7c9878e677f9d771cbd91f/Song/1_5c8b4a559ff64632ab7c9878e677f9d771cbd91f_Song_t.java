 package com.jin.tpdb.entities;
 
 import java.util.Date;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.ManyToOne;
import javax.persistence.ManyToMany;
 import javax.persistence.Column;
 import com.jin.tpdb.entities.Artist;
 import com.jin.tpdb.entities.Album;
 import com.jin.tpdb.entities.User;
 
 @Entity
 public class Song {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	int id;
 
 	@ManyToMany
 	private Album album;
 
 	private int number;
 
 	private String name;
 
 	private String length;
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public Album getAlbum() {
 		return album;
 	}
 
 	public void setAlbum(Album album) {
 		this.album = album;
 	}
 
 	public int getNumber() {
 		return number;
 	}
 
 	public void setNumber(int number) {
 		this.number = number;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getLength() {
 		return length;
 	}
 
 	public void setLength(String length) {
 		this.length = length;
 	}
 
 }
