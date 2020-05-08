 package edu.upenn.nettercenter.auni.cookcompetition.models;
 
 import com.j256.ormlite.field.DatabaseField;
 import com.j256.ormlite.table.DatabaseTable;
 
 @DatabaseTable
 public class Student {
 
 	@DatabaseField(generatedId = true)
 	private Long id;
 	@DatabaseField
 	private String firstName;
 	@DatabaseField
 	private String lastInitial;
 	@DatabaseField
 	private String nickname;
     @DatabaseField(canBeNull = true, foreign = true, foreignAutoRefresh = true)
 	private Team team;
 	@DatabaseField
 	private boolean isActive;
     @DatabaseField(canBeNull = true, foreign = true, foreignAutoRefresh = true)
     private Role role;
 
 	public Student() {
 	}
 	
 	public Student(String firstName, String lastInitial, String nickname) {
 		this.firstName = firstName;
 		this.lastInitial = lastInitial;
 		this.nickname = nickname;
 		this.isActive = true;
 	}
 	
 	public Long getId() {
 		return id;
 	}
 	
 	public String getFirstName() {
 	    return firstName;
 	}
 	
 	public String getName() {
 		return firstName + " " + lastInitial + ".";
 	}
 	
 	public String getNickname() {
 	    return nickname;
 	}
 	
 	public Team getTeam() {
 	    return team;
 	}
 	
 	public String getLastInitial() {
 	    return lastInitial;
 	}
 	
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 	
 	public void setNickname(String nickname) {
 	    this.nickname = nickname;
 	}
 	
 	public void setTeam(Team team) {
 	    this.team = team;
 	}
 	
 	public boolean isActive() {
 		return isActive;
 	}
 	
 	public void setActive(boolean isActive) {
 		this.isActive = isActive;
 	}
 	
	public void setLastInitial(String lastInitial) {
	    this.lastInitial = lastInitial;
 	}
 
 	@Override
 	public String toString() {
 		return getName();
 	}
 
     public Role getRole() {
         return role;
     }
 
     public void setRole(Role role) {
         this.role = role;
     }
 }
