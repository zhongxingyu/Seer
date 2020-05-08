 package com.titankingdoms.nodinchan.titanchat.util.displayname;
 
 import javax.persistence.Entity;
 import javax.persistence.Table;
 
 import com.avaje.ebean.validation.NotEmpty;
 
 @Entity
 @Table(name = "display_names")
 public class DisplayName {
 	
 	@NotEmpty
 	private String displayname;
 	
 	@NotEmpty
 	private String name;
 	
 	public String getDisplayName() {
 		return displayname;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setDisplayName(String displayname) {
 		this.displayname = displayname;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 }
