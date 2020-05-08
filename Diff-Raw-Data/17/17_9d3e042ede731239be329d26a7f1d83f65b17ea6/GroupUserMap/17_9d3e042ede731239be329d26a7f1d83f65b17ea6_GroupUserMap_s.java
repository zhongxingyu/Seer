 package models;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import org.apache.commons.lang3.builder.EqualsBuilder;
 
 import play.db.ebean.Model;
 
 @Entity
 @Table(name = "groups_users")
 public class GroupUserMap extends Model {
 
 	@Id
 	public Long id;
 
 	@ManyToOne
 	@JoinColumn(name = "group_id", referencedColumnName = "id")
 	public Group group;
 
 	@ManyToOne
 	@JoinColumn(name = "user_id", referencedColumnName = "id")
 	public User user;
 
 	/**
 	 * Generic query helper for entity GroupUserMap with id Long
 	 */
 	public static Model.Finder<Long, GroupUserMap> find = new Model.Finder<Long, GroupUserMap>(
 			Long.class, GroupUserMap.class);
 
 	/**
 	 * Retrieve a User from email.
 	 */
 	public static GroupUserMap findByGroupIdAndUserId(Long groupId, Long userId) {
 		return find.where().eq("group.id", groupId).eq("user.id", userId)
 				.findUnique();
 	}
 	
 
     @Override
 	public boolean equals(Object object) {
     	if (!(object instanceof GroupUserMap)) {
     		return false;
     	}
     	GroupUserMap rhs = (GroupUserMap) object;
     	return new EqualsBuilder()
     		.append(id, rhs.id)
     		.isEquals();
 	}
 }
