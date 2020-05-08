 package com.nuclearw.adminpvptoggle;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import com.avaje.ebean.validation.NotEmpty;
 import com.avaje.ebean.validation.NotNull;
 
 @Entity
 @Table(name="apt_players")
 public class PVPPlayer {
 	@Id
 	@Column(name="id")
 	private int id;
 
 	@NotEmpty
 	@Column(name="name")
 	private String name;
 	
 	@NotNull
 	@Column(name="time_expire")
 	private long timeExpire;
 
 	public int getId() {
 		return id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
	public long getTimeApplied() {
 		return timeExpire;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void setTimeExpire(long timeExpire) {
 		this.timeExpire = timeExpire;
 	}
 }
