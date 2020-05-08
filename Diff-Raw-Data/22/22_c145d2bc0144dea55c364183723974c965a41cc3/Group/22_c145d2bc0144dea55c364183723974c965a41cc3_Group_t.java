 package com.youthministry.domain;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 
 @Entity(name="GROUP_DETAILS")
 public class Group {
 	@Id
 	@GeneratedValue(strategy=GenerationType.AUTO)
 	private Long groupId;
 	@Column(name = "GROUP_NAME", unique = true, nullable = false)
 	private String groupName;
 	private String groupDesc;
 	
	public String getIdAsString() {
 		return new Long(groupId).toString();
	}
 	public Long getGroupId() {
 		return groupId;
 	}
 	public void setGroupId(Long groupId) {
 		this.groupId = groupId;
 	}
 	public String getGroupName() {
 		return groupName;
 	}
 	public void setGroupName(String groupName) {
 		this.groupName = groupName;
 	}
 	public String getGroupDesc() {
 		return groupDesc;
 	}
 	public void setGroupDesc(String groupDesc) {
 		this.groupDesc = groupDesc;
 	}
 	
 }
