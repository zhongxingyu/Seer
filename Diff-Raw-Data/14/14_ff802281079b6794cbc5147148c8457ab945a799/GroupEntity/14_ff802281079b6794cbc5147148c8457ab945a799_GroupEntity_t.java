 package no.sag.activitymanager.entity;
 
import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.Table;
 
 
 @Entity
 @Table(name = "`Group`")
 public class GroupEntity {
 
 	@Id
 	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	@Column(name = "id")
 	private Long id;
 	
 	@Column(name = "userid")
 	private Long owner;
 	
 	@Column(name = "name")
 	private String name;
 
 	@OneToMany(cascade=CascadeType.PERSIST, fetch=FetchType.EAGER)
 	@JoinTable(name="`Member`", 
 		joinColumns=@JoinColumn(name="groupid", referencedColumnName="id"),
 		inverseJoinColumns=@JoinColumn(name="userid", referencedColumnName="id")
 	)
 	@OrderBy("firstName, lastName")
 	private Set<UserEntity> members = new HashSet<UserEntity>();
 	
 	
 	public GroupEntity() {
 	}
 	
 	
 	public void addMember(UserEntity user) {
 		members.add(user);
 	}
 	
 	public void removeMember(Long id) {
 		UserEntity memberToBeDeleted = null;
 		for (UserEntity member : members) {
 			if (member.getId().equals(id)) {
 				memberToBeDeleted = member;
 				break;
 			}
 		}
 		if (memberToBeDeleted != null) {
 			members.remove(memberToBeDeleted);
 		}
 	}
 	
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Long getOwner() {
 		return owner;
 	}
 
 	public void setOwner(Long userId) {
 		this.owner = userId;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public Set<UserEntity> getMembers() {
 		return members;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		GroupEntity other = (GroupEntity) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 	
 }
 
 class IDComparator implements Comparator<UserEntity> {
 
 	@Override
 	public int compare(UserEntity arg0, UserEntity arg1) {
 		return arg0.getId().compareTo(arg1.getId());
 	}
 	
 }
