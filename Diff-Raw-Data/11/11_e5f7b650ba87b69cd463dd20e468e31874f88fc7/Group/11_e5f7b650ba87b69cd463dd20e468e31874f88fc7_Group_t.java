 package Entity;
 
 import java.io.Serializable;
 import javax.persistence.*;
 import java.util.Set;
 import com.sun.org.apache.xalan.internal.xsltc.compiler.util.NamedMethodGenerator;
 
 @Entity
 @Table(name = "groups")
 @NamedQuery(name = Group.GROUP_BY_GROUP_NAME_QUERY, query = "SELECT g FROM Group g where g.name = :name")
 public class Group implements Serializable {
 
    public static final String GROUP_BY_GROUP_NAME_QUERY = "Entity.User.GROUP_BY_GROUP_NAME_QUERY";
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Column(name = "group_id", unique = true, nullable = false)
     private int groupId;
 
     @Column(name = "name", nullable = false, length = 32)
     private String name;
 
     //bi-directional many-to-many association to User
     @ManyToMany(mappedBy = "groups")
     private Set<User> users;
 
     public int getGroupId() {
         return this.groupId;
     }
 
     public void setGroupId(int groupId) {
         this.groupId = groupId;
     }
 
     public String getName() {
         return this.name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Set<User> getUsers() {
         return this.users;
     }
 
     public void setUsers(Set<User> users) {
         this.users = users;
     }
 
 }
