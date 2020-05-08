 package com.youthministry.domain;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.OneToMany;
 
 @Entity
 @Inheritance (strategy=InheritanceType.JOINED)
 public class PageContent {
 	@Id
 	@GeneratedValue(strategy=GenerationType.AUTO)
 	private Long pageContentId;
 	@Column(name="PAGE_CONTENT_NAME", nullable=false)
 	private String pageContentName;
 	@Column(name="LOCATION", nullable=false)
 	private String location;
 		
	@OneToMany(cascade=CascadeType.REMOVE)
 	private Collection<Group> groups = new ArrayList<Group>();
 	
 	public Long getPageContentId() {
 		return pageContentId;
 	}
 	public void setPageContentId(Long pageContentId) {
 		this.pageContentId = pageContentId;
 	}
 	public String getPageContentName() {
 		return pageContentName;
 	}
 	public void setPageContentName(String pageContentName) {
 		this.pageContentName = pageContentName;
 	}
 	public String getLocation() {
 		return location;
 	}
 	public void setLocation(String location) {
 		this.location = location;
 	}
 	public Collection<Group> getGroups() {
 		return groups;
 	}
 	public void setGroups(Collection<Group> groups) {
 		this.groups = groups;
 	}
 	
 }
