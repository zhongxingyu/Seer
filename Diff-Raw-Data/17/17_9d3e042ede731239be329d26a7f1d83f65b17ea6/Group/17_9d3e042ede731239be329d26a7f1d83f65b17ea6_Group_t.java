 package models;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 import org.apache.commons.lang3.builder.EqualsBuilder;
 
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 
 import com.avaje.ebean.Page;
 
 
 /**
  * Group entity managed by Ebean
  */
 @Entity 
 @Table(name="groups")
 public class Group extends Model {
 
 	@Id
     public Long id;
     
     @Constraints.Required
     public String name;
     
     /*@ManyToMany(cascade=CascadeType.ALL)
     @JoinTable(name="groups_users", joinColumns=
             @JoinColumn(name="group_id", referencedColumnName="id"),
             inverseJoinColumns=
                 @JoinColumn(name="user_id", referencedColumnName="id"))*/
    @OneToMany(mappedBy="group", cascade={CascadeType.REMOVE})
     public List<GroupUserMap> members = new ArrayList<GroupUserMap>();
     
     /**
      * Generic query helper for entity User with id Long
      */
     public static Model.Finder<Long,Group> find = new Model.Finder<Long,Group>(Long.class, Group.class);
 
     public static Map<String,String> options() {
         LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
         for(Group c: Group.find.orderBy("name").findList()) {
             options.put(c.id.toString(), c.name);
         }
         return options;
     }
     
 
     /**
      * Retrieve all users.
      */
     public static List<Group> findAll() {
         return find.all();
     }
 
     
     public String toString() {
         return "Group(" + id + ")";
     }
     
 
     /**
      * Return a page of user
      *
      * @param page Page to display
      * @param pageSize Number of users per page
      * @param sortBy User property used for sorting
      * @param order Sort order (either or asc or desc)
      * @param filter Filter applied on the name column
      */
     public static Page<Group> page(int page, int pageSize, String sortBy, String order, String filter) {
         return 
             find.where()
                 .ilike("name", "%" + filter + "%")
                 .orderBy(sortBy + " " + order)
                 .findPagingList(pageSize)
                 .getPage(page);
     }
 
 
     @Override
 	public boolean equals(Object object) {
     	if (!(object instanceof Group)) {
     		return false;
     	}
     	Group rhs = (Group) object;
     	return new EqualsBuilder()
     		.appendSuper(super.equals(object))
     		.append(id, rhs.id)
     		.isEquals();
 	}
 }
 
