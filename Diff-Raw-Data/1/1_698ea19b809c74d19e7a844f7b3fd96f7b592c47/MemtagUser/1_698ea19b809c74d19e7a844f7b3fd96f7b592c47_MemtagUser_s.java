 package com.memtag.model;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.neo4j.graphdb.Direction;
 import org.springframework.data.annotation.Transient;
 import org.springframework.data.neo4j.annotation.GraphId;
 import org.springframework.data.neo4j.annotation.Indexed;
 import org.springframework.data.neo4j.annotation.NodeEntity;
 import org.springframework.data.neo4j.annotation.RelatedTo;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 
 @NodeEntity
 public class MemtagUser {
 
 	 @GraphId 
 	 Long id;
 	 @Indexed 
 	 private String login;
 	
      private String password;
 	 @RelatedTo(type="friend"  , direction = Direction.BOTH) 
 	 Set <MemtagUser> friends= new HashSet<MemtagUser>();
 	 @RelatedTo(type="owner"  , direction = Direction.BOTH) 
 	 Set<Memory> ownedMemories = new HashSet<Memory>();
 	 @RelatedTo(type="subscriber") 
 	 Set<Memory> subscribedMemories = new HashSet<Memory>();
 	 
 	 public MemtagUser() {
 	}
 	 public MemtagUser(String login) {
 		this.login = login;
 	}
 	 
 	public Set<MemtagUser> getFriends() {
 		return Collections.unmodifiableSet(friends);
 	}
 	public void setFriends(Set<MemtagUser> friends) {
 		this.friends = friends;
 	}
 	public void addFriend(MemtagUser friend){
 		friends.add(friend);
 	}
 	public Set<Memory> getOwnedMemories() {
 		return Collections.unmodifiableSet(ownedMemories);
 	}
 	public void setOwnedMemories(Set<Memory> ownedMemories) {
 		this.ownedMemories = ownedMemories;
 	}
 	public void addOwnedMemory(Memory memory){
 		ownedMemories.add(memory);
 	}
 	public Set<Memory> getSubscribedMemories() {
 		return  Collections.unmodifiableSet(subscribedMemories);
 	}
 	public void setSubscribedMemories(Set<Memory> subscribedMemories) {
 		this.subscribedMemories = subscribedMemories;
 	}
 	public void addSubscribedMemory(Memory memory){
 		subscribedMemories.add(memory);
 	}
 	public Long getId() {
 		return id;
 	}
 	public void setLogin(String login) {
 		this.login = login;
 	}
 	public String getLogin() {
 		return login;
 	}
 
     public String getUsername(){
         return login;
     }
     public void setUsername(String username){
         login = username;
     }
     @JsonIgnore
    @Transient
     public String getPassword(){
         return password;
     }
 
     public void setPassword(String newPassoword){
         password = newPassoword;
     }
 }
